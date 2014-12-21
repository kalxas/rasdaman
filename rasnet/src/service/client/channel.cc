/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#include <map>
#include <utility>


#include <boost/lexical_cast.hpp>
#include <boost/bind.hpp>

#include "../../../../common/src/logging/easylogging++.hh"
#include "../../../../common/src/uuid/uuid.hh"

#include "../../util/proto/protozmq.hh"
#include "../../util/proto/zmqutil.hh"
#include "../../messages/communication.pb.h"
#include "../../../src/messages/service.pb.h"

#include "channel.hh"

namespace rasnet
{

using common::UUID;
using base::BaseMessage;
using boost::unique_lock;
using boost::mutex;
using boost::thread;
using boost::uint64_t;
using std::runtime_error;

Channel::Channel(std::string host, boost::uint32_t port):context(1){
	this->init(host,port);
}

Channel::Channel(std::string host, boost::uint32_t port, ChannelConfig config):config(config),context(config.getNumberOfIoThreads())
{
	this->init(host,port);
}

Channel::~Channel()
{

    try
    {
        this->disconnectFromServer();
        this->controlSocket->disconnect(this->controlPipeAddr.c_str());
        this->proxyThread->join();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
    }
    catch(...)
    {
        LERROR<<"ClientSocket: Unexpected exception";
    }
}

void Channel::CallMethod(const  google::protobuf::MethodDescriptor * method,
                         google::protobuf::RpcController * controller, const google::protobuf::Message * request,
                         google::protobuf::Message * response,  google::protobuf::Closure * done)
{
    /**
     * 1. Increase the counter value
     * 2. Prepare the request.
     * 3. Create DEALER socket, set identity, connect to message pipe
     * 4. Send the call counter value, send the serialized request
     */
    ServiceRequest serviceRequest;
    ServiceResponse serviceResponse;
    BaseMessage serializedReply;

    //Sanity check
    if (method == NULL || controller == NULL || request == NULL
            || response == NULL || done == NULL)
    {
        throw std::runtime_error("Invalid input parameters.");
    }

    try
    {
        //1. Increase the counter value
        unique_lock<mutex>  lock(this->counterMutex);
        boost::uint64_t counterValue = this->counter;
        this->counter++;
        lock.unlock();

        //2.Prepare the request

        serviceRequest.set_input_value(request->SerializeAsString());
        serviceRequest.set_id(counterValue);
        serviceRequest.set_method_name(ProtoZmq::getServiceMethodName(method));

        //Create dealer socket
        zmq::socket_t channelSocket(this->context, ZMQ_DEALER);
        std::string identity = UUID::generateUUID();
        //Set the identity for the DEALER socket and connect
        channelSocket.setsockopt(ZMQ_IDENTITY, identity.c_str(),
                                 strlen(identity.c_str()));
        channelSocket.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
        channelSocket.connect(this->messagePipeAddr.c_str());

        //Send the data using zeromq zero copy
        //Send the id of the call first and then the serialized request
        zmq::message_t callId(sizeof(boost::uint64_t));
        memcpy(callId.data(), &counterValue, sizeof(boost::uint64_t));
        channelSocket.send(callId, ZMQ_SNDMORE);

        int envelopeSize = serviceRequest.ByteSize();
        ::google::protobuf::uint8 *target =
            new ::google::protobuf::uint8[envelopeSize];
        serviceRequest.SerializeWithCachedSizesToArray(target);
        zmq::message_t message(target, envelopeSize, ZmqUtil::freeByteArray);
        channelSocket.send(message);
        //Finished sending data, wait for reply

        //Receive service response
        zmq::message_t data;
        channelSocket.recv(&data);
        int parseResult = serviceResponse.ParseFromArray(data.data(), data.size());
        if(!parseResult)
        {
            throw runtime_error("Invalid service respoonse.");
        }
        //Finished receiving

        assert(serviceResponse.id() == serviceRequest.id());

        if (serviceResponse.has_error())
        {
            controller->SetFailed(serviceResponse.error());
        }
        else if (serviceResponse.has_output_value())
        {
            if(!response->ParseFromString(serviceResponse.output_value()))
            {
                controller->SetFailed(
                    "Service call received unparsable response.");
            }
        }
        else
        {
            controller->SetFailed(
                "Service call failed for an unknown reason.");
        }

        channelSocket.disconnect(this->messagePipeAddr.c_str());

        //Run the callback
        done->Run();
    }
    catch(std::exception& ex)
    {
        controller->SetFailed(ex.what());
        LERROR<<controller->ErrorText();
    }
    catch(...)
    {
        controller->SetFailed("Method call failed for unknown reason.");
        LERROR<<controller->ErrorText();
    }
}



void Channel::listenerMethod()
{
    /**
     * 1. Wait for connect request from the constructor.
     * 2. Connect to the server by sending a ConnectRequest
     * 3. Forward the ConnectReply to the controlSocket
     * 4. Find a way to gracefully exit if the server has died.
     */
    //TODO-AT: What should we do if any of the connect calls fail?

    //Map between call ids and socket identities
    boost::scoped_ptr<PeerStatus> serverStatus;
    std::map<uint64_t, std::string > requests;
    std::map<uint64_t, std::string >::iterator it;
    std::pair<std::map<uint64_t, std::string >::iterator, bool> insertResult;
    std::string serviceRequestType = ServiceRequest::default_instance().GetTypeName();

    base::BaseMessage controlMessage;
    base::BaseMessage frontMessage;
    base::BaseMessage internalReq;
    AlivePing ping;

    // The number of milliseconds to poll before giving up
    // -1 means that the poll will block until there is something on
    // the polled sockets
    boost::int32_t timeout = -1;

    bool keepRunning=false;

    //Socket through which we pass control messages from the client to this thread
    zmq::socket_t internalChannelSock(this->context, ZMQ_ROUTER);
    internalChannelSock.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
    internalChannelSock.bind(this->messagePipeAddr.c_str());

    //Socket through which we pass control messages from the client to this thread
    zmq::socket_t control(this->context, ZMQ_PAIR);
    control.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
    control.bind(this->controlPipeAddr.c_str());

    //Socket connected to the server
    zmq::socket_t frontend(this->context, ZMQ_DEALER);
    //Set the identity for the DEALER socket
    frontend.setsockopt(ZMQ_IDENTITY, this->socketIdentity.c_str(),
                        strlen(this->socketIdentity.c_str()));
    frontend.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
    frontend.connect(this->serverAddr.c_str());

    //Connect to the server
    if(ProtoZmq::zmqReceive(control, controlMessage) && controlMessage.type()==InternalConnectRequest::default_instance().GetTypeName() )
    {
        ExternalConnectRequest connectionReq;
        connectionReq.set_period(this->config.getAliveTimeout());
        connectionReq.set_retries(this->config.getAliveRetryNo());

        ProtoZmq::zmqSend(frontend, connectionReq);

        zmq::pollitem_t items[] = { { frontend, 0, ZMQ_POLLIN, 0 } };
        //Wait for a predefined number of milliseconds before giving up and closing this
        zmq::poll(items, 1, this->config.getConnectionTimeout());

        if(items[0].revents & ZMQ_POLLIN)
        {
            BaseMessage mess;
            ExternalConnectReply connectionReply;

            if(ProtoZmq::zmqReceive(frontend, mess))
            {
                if(mess.type()==connectionReply.GetTypeName() && connectionReply.ParseFromString(mess.data()))
                {
                    serverStatus.reset(new PeerStatus(connectionReply.retries(), connectionReply.period()));
                    timeout = connectionReply.period();
                    keepRunning=true;
                }
            }
        }
    }

    //Send the status to the controlling thread.
    InternalConnectReply reply;
    reply.set_success(keepRunning);
    ProtoZmq::zmqSend(control, reply);

    zmq::pollitem_t items[] = {
                                  {control,0,ZMQ_POLLIN,0},
                                  {frontend, 0, ZMQ_POLLIN, 0 },
                                  {internalChannelSock, 0, ZMQ_POLLIN, 0}
                              };

    while(keepRunning)
    {
        zmq::poll(items, 3, timeout);

        if (items[0].revents & ZMQ_POLLIN)
        {
            ProtoZmq::zmqReceive(control, controlMessage);
            if(controlMessage.type()==InternalDisconnectRequest::default_instance().GetTypeName())
            {
                keepRunning=false;
                InternalDisconnectReply disconnectReply;
                ProtoZmq::zmqSend(control, disconnectReply);
            }
        }

        if (items[1].revents & ZMQ_POLLIN)
        {
            //If we receive a message from the server, reset its status
            serverStatus->reset();

            if(ProtoZmq::zmqReceive(frontend, frontMessage))
            {
                std::string messageType = frontMessage.type();

                if(messageType ==  this->ping.GetTypeName())
                {
                    ProtoZmq::zmqSend(frontend, this->pong);
                }
                else if(messageType == ServiceResponse::default_instance().GetTypeName())
                {
                    ServiceResponse serviceResponse;
                    if(serviceResponse.ParseFromString(frontMessage.data()))
                    {
                        it =requests.find(serviceResponse.id());
                        if(it!=requests.end())
                        {
                            int serviceResponseSize = serviceResponse.ByteSize();
                            ::google::protobuf::uint8 *target =
                                new ::google::protobuf::uint8[serviceResponseSize];
                            serviceResponse.SerializeWithCachedSizesToArray(target);
                            zmq::message_t message(target, serviceResponseSize, ZmqUtil::freeByteArray);

                            ProtoZmq::stringSendMore(internalChannelSock, it->second);
                            internalChannelSock.send(message);
                            requests.erase(it);
                        }
                    }
                }
                else if(messageType == this->pong.GetTypeName())
                {
                    //Ignore it
                }
            }
        }
        else
        {
            //Decrease liveliness and send out a ping to see if the server is alive
            if(serverStatus->decreaseLiveliness())
            {
                ProtoZmq::zmqSend(frontend, ping);
            }
        }

        if (items[2].revents & ZMQ_POLLIN)
        {
            //We will only receive service request and we need to forward
            //them to the appropriate server and keep track of
            //Receive an integer representing the
            zmq::message_t content;
            int more = 0; //  Multipart detection
            size_t more_size = sizeof(more);

            //Get the identity of the peer
            std::string identity = ProtoZmq::receiveString(internalChannelSock);
            //Get the id of the call
            internalChannelSock.getsockopt(ZMQ_RCVMORE, &more, &more_size);
            if (more)
            {
                zmq::message_t callIdMessage;
                internalChannelSock.recv(&callIdMessage);
                boost::uint64_t callId = *(static_cast<boost::uint64_t*>(callIdMessage.data()));

                internalChannelSock.getsockopt(ZMQ_RCVMORE, &more, &more_size);
                if(more)
                {
                    internalChannelSock.recv(&content);
                    insertResult=requests.insert(std::pair<boost::uint64_t, std::string>(callId, identity));

                    if(insertResult.second)
                    {
                        internalReq.set_data(content.data(), content.size());
                        internalReq.set_type(serviceRequestType);
                        ProtoZmq::zmqRawSend(frontend, internalReq);
                    }
                }
            }
        }

        if(!serverStatus->isAlive())
        {
            for(it=requests.begin(); it!=requests.end(); ++it)
            {
                ServiceResponse failureResponse;
                failureResponse.set_id(it->first);
                failureResponse.set_error("The service call failed.");

                int serviceResponseSize = failureResponse.ByteSize();
                ::google::protobuf::uint8 *target =
                    new ::google::protobuf::uint8[serviceResponseSize];
                failureResponse.SerializeWithCachedSizesToArray(target);
                zmq::message_t message(target, serviceResponseSize, ZmqUtil::freeByteArray);

                ProtoZmq::stringSendMore(internalChannelSock, it->second);
                internalChannelSock.send(message);

            }
            requests.clear();
        }
    }

    frontend.disconnect(serverAddr.c_str());
    //control.unbind(this->controlPipeAddr.c_str());
  //  internalChannelSock.unbind(this->messagePipeAddr.c_str());
}

void Channel::init(std::string host, boost::uint32_t port)
{
    this->linger = 0;
    this->counter = 1 ;
    this->serverAddr =  host + ":" + boost::lexical_cast<std::string>(port);
    this->socketIdentity = UUID::generateUUID();
    this->controlPipeAddr = "inproc://control-" + this->socketIdentity;
    this->messagePipeAddr = "inproc://message-" + this->socketIdentity;

    this->proxyThread.reset(new thread(boost::bind( &Channel::listenerMethod, this)));

    this->controlSocket.reset(new zmq::socket_t(this->context, ZMQ_PAIR));
    this->controlSocket->setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
    this->controlSocket->connect(this->controlPipeAddr.c_str());

    this->connectToServer();
}

void Channel::connectToServer()
{
    InternalConnectRequest request;
    InternalConnectReply reply;
    BaseMessage baseReply;

    if(!ProtoZmq::zmqSend(*(this->controlSocket.get()), request))
    {
        throw runtime_error("Connection to server timed out.");
    }

    if(! ProtoZmq::zmqReceive(*(this->controlSocket.get()), baseReply))
    {
        throw runtime_error("Connection to server timed out.");
    }

    if(baseReply.type()!=reply.GetTypeName() || (reply.ParseFromString(baseReply.data()) && !reply.success()))
    {
        throw runtime_error("Connection to server timed out.");
    }
}

void Channel::disconnectFromServer()
{
    InternalDisconnectRequest request = InternalDisconnectRequest::default_instance();
    BaseMessage reply;

    ProtoZmq::zmqSend(*(this->controlSocket.get()), request);
    ProtoZmq::zmqReceive(*(this->controlSocket.get()), reply);

    if(reply.type()!=InternalDisconnectReply::default_instance().GetTypeName())
    {
        LERROR<<"Unexpected message received from control socket."<<reply.DebugString();
    }
}

} /* namespace rasnet */
