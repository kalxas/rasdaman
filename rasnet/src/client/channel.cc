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
#include <stdexcept>

#include <boost/thread.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/lexical_cast.hpp>

#include "../../../common/src/uuid/uuid.hh"
#include "../../../common/src/logging/easylogging++.hh"

#include "../exception/networkingexception.hh"

#include "../common/zmqutil.hh"
#include "../common/util.hh"

#include "../messages/internal.pb.h"

#include "channel.hh"
#include "clientponghandler.hh"
#include "clientpinghandler.hh"
#include "serviceresponsehandler.hh"

namespace rasnet
{
using boost::thread;
using boost::mutex;
using boost::unique_lock;
using boost::shared_ptr;
using common::UUID;

using rasnet::internal::InternalConnectRequest;
using rasnet::internal::InternalConnectReply;
using rasnet::internal::InternalDisconnectRequest;
using rasnet::internal::InternalDisconnectReply;
using rasnet::internal::ServiceRequestAvailable;
using rasnet::internal::ServiceResponseAvailable;

using std::map;
using std::string;
using std::pair;
using std::make_pair;

Channel::Channel(const std::string &serverAddress, const ChannelConfig &config):
    serverAddress(serverAddress),
    config(config), context(config.getNumberOfIoThreads(), config.getMaxOpenSockets())
{
    //The linger determines the amount of time a socket will hang before destruction
    //trying to send queued messages to peers
    this->linger = 0;
    this->callCounter = 0ul;

    this->serviceRequests.reset(new map<string, pair<const google::protobuf::Message*, string> >());
    this->serviceResponses.reset(new map<string, pair<bool,shared_ptr<zmq::message_t> > >);
    this->serviceMutex.reset(new boost::mutex());

    this->channelIdentity = UUID::generateUUID();
    this->controlAddress = ZmqUtil::toInprocAddress("control-" + this->channelIdentity);
    this->bridgeAddress = ZmqUtil::toInprocAddress("bridge-" + this->channelIdentity);

    this->controlSocket.reset(new zmq::socket_t(this->context, ZMQ_PAIR));
    this->controlSocket->setsockopt(ZMQ_LINGER, &linger, sizeof(linger));
    this->controlSocket->bind(this->controlAddress.c_str());

    this->workerThread.reset(new thread(boost::bind( &Channel::workerMethod, this)));

    this->connectToServer();
}

Channel::~Channel()
{
    try
    {
//        LDEBUG<<"Disconnecting channel from server.";
        this->disconnectFromServer();
//        LDEBUG<<"Disconnected channel from server.";

//        LDEBUG<<"Joining channel worker thread.";
        this->workerThread->join();
//        LDEBUG<<"Joined channel worker thread.";

        // https://github.com/zeromq/libzmq/issues/949
        // this->controlSocket->unbind(this->controlAddress.c_str());
        this->controlSocket->close();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
    }
    catch(...)
    {
        LERROR<<"Unexpected exception in Channel destructor.";
    }
}

void Channel::CallMethod(const google::protobuf::MethodDescriptor *method, google::protobuf::RpcController *controller, const google::protobuf::Message *request, google::protobuf::Message *response, google::protobuf::Closure *done)
{
    //Sanity check
    if (method == NULL || controller == NULL || request == NULL
            || response == NULL || done == NULL)
    {
        throw std::runtime_error("Invalid input parameters.");
    }

    try
    {
        callCounterMutex.lock();
        ++this->callCounter;
        boost::uint64_t currentCallCounter = this->callCounter;
        callCounterMutex.unlock();

        //1. Create DEALER socket for inter-thread communication
        std::string callId = UUID::generateUUID() + boost::lexical_cast<string>(currentCallCounter);

        zmq::socket_t bridge(this->context, ZMQ_DEALER);
        bridge.setsockopt(ZMQ_IDENTITY, callId.c_str(),
                          strlen(callId.c_str()));
        bridge.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
        bridge.connect(this->bridgeAddress.c_str());

        //2. Add the request to the list of pending requests
        //TODO-AT:@Optimization We could serialize the request here
        this->serviceMutex->lock();
        this->serviceRequests->insert(std::make_pair(callId, std::make_pair(request, Util::getMethodName(method))));
        this->serviceMutex->unlock();

        ServiceRequestAvailable requestAvailable;
        ServiceResponseAvailable responseAvailable;
        boost::shared_ptr<BaseMessage> internalResponseEnvelope;

        //3. Send request and wait for response
        //TODO:Refactor. Should I keep this check here?
        //If the control socket is not writeable,
        if(!ZmqUtil::send(bridge, requestAvailable) || !ZmqUtil::receive(bridge, internalResponseEnvelope))
        {
            controller->SetFailed("Network failure: Inter-thread communication failed.");
        }
        else if(internalResponseEnvelope->type() != Util::getMessageType(responseAvailable)
                ||!responseAvailable.ParseFromString(internalResponseEnvelope->data()))
        {
            controller->SetFailed("Internal logic error.");
        }
        else
        {
            unique_lock<mutex> responseLock(*this->serviceMutex);
            std::map<std::string, std::pair<bool,shared_ptr<zmq::message_t> > >::iterator it;
            it = this->serviceResponses->find(callId);

            if(it!=this->serviceResponses->end())
            {
                bool success = it->second.first;
                shared_ptr<zmq::message_t> message = it->second.second;

                if(success)
                {
                    //Success
                    if(!response->ParseFromArray(message->data(), message->size()))
                    {
                        controller->SetFailed("Failed to parse response message.");
                    }
                }
                else
                {
                    //Failure
                    controller->SetFailed(std::string(static_cast<char *>(message->data()), message->size()));
                }

                //Remove the response
                this->serviceResponses->erase(it);
            }
            else
            {
                controller->SetFailed("Internal logic error.");
            }
        }

        bridge.disconnect(this->bridgeAddress.c_str());
        bridge.close();
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

    done->Run();
}

void Channel::connectToServer()
{
    boost::shared_ptr<BaseMessage> message;
    InternalConnectRequest request = InternalConnectRequest::default_instance();

    //Send the inter-thread connection request
    //TODO:Refactor
    if(!ZmqUtil::isSocketWritable(*controlSocket, 100) || !ZmqUtil::send(*this->controlSocket, request))
    {
        throw NetworkingException("Failed to send inter-thread connection request.");
    }

    //Wait for a predefined number of milliseconds before giving up contacting the server
    //Will fail if no message has arrived in the predefined time frame
    if(!(ZmqUtil::isSocketReadable(*controlSocket,  2 * this->config.getConnectionTimeout()))
            || !ZmqUtil::receive(*(this->controlSocket), message))
    {
        LERROR<<"The server at the endpoint:"<<this->serverAddress<<" is not responding to queries";
        throw NetworkingException("Failed to receive inter-thread connection reply.");
    }
    else
    {
        //   LDEBUG<<"Received internal connection request";

        InternalConnectReply reply;
        if(message->type() != Util::getMessageType(reply)
                || !reply.ParseFromString(message->data()))
        {
            throw std::logic_error("Invalid connection reply.");
        }
        if(!reply.success())
        {
            throw NetworkingException("Unable to establish connection to server.");
        }
    }
}

void Channel::disconnectFromServer()
{
//    LDEBUG<<"Sending internal disconnect message";

    InternalDisconnectRequest request = InternalDisconnectRequest::default_instance();
    boost::shared_ptr<BaseMessage> reply;

    if(!ZmqUtil::isSocketWritable(*controlSocket, 0))
    {
        LERROR<<"Unable to write to control socket.";
    }
    else if(!ZmqUtil::send(*this->controlSocket, request))
    {
        LERROR<<"Inter-thread messaging error.";
    }
    else
    {
        //Will fail if no message has arrived in the predefined time frame
        if(!ZmqUtil::isSocketReadable(*controlSocket, 2 * this->config.getConnectionTimeout()) || !ZmqUtil::receive(*(this->controlSocket), reply))
        {
            LERROR<<"Failed to receive internal disconnect reply.";
        }
        else if(reply->type()!=Util::getMessageType(InternalDisconnectReply::default_instance()))
        {
            LERROR<<"Received invalid internal disconnect reply.";
        }
    }
}

bool Channel::tryConnectToServer(zmq::socket_t &controlSocket, zmq::socket_t &serverSocket, boost::shared_ptr<PeerStatus> &out_serverStatus, int32_t &out_timeout)
{
    boost::shared_ptr<BaseMessage> controlMessage;
    bool success = false;

    //Wait for the InternalConnectRequest and check the type
    if(ZmqUtil::receive(controlSocket, controlMessage)
            && controlMessage->type()==Util::getMessageType(InternalConnectRequest::default_instance()))
    {
        //Prepare the connect request to the server
        ConnectRequest connectionReq;
        connectionReq.set_lifetime(this->config.getAliveTimeout());
        connectionReq.set_retries(this->config.getAliveRetryNo());

        //Send the composite message
        ZmqUtil::sendCompositeMessage(serverSocket, MessageType::CONNECT_REQUEST, connectionReq);
        zmq::pollitem_t items[] = { { serverSocket, 0, ZMQ_POLLIN, 0 } };
        //Wait for a predefined number of milliseconds before giving up contacting the server
        zmq::poll(items, 1, this->config.getConnectionTimeout());

        if(items[0].revents & ZMQ_POLLIN)
        {
            //Read the connect reply from the server
            MessageType responseType;
            std::vector<boost::shared_ptr<zmq::message_t> > messageComponents;
            ZmqUtil::receiveCompositeMessage(serverSocket, messageComponents);

            ConnectReply connectReply;
            //The structure of the message should be:
            // | Type | Serialized ConnectReply |
            if(messageComponents.size()==2
                    && responseType.ParseFromArray(messageComponents[0]->data(), messageComponents[0]->size())
                    && responseType.type() == MessageType::CONNECT_REPLY
                    && connectReply.ParseFromArray(messageComponents[1]->data(), messageComponents[1]->size()))
            {
                success=true;
                out_serverStatus.reset(new PeerStatus(connectReply.retries(), connectReply.lifetime()));
                out_timeout = connectReply.lifetime();
            }
            else
            {
                LERROR<<"Invalid server response to connection request.";
            }
        }
        else
        {
            LERROR<<"Could not establish connection to server. The server did not respond within "<<this->config.getConnectionTimeout()<<" milliseconds";
        }
    }
    else
    {
        LERROR<<"Received invalid internal connect request from control socket.";
    }

    //Notify the main thread about the connection status
    InternalConnectReply internalConnectReply;
    internalConnectReply.set_success(success);
    ZmqUtil::send(controlSocket, internalConnectReply);

    return success;
}

void Channel::workerMethod()
{
    try
    {
        // The number of milliseconds to poll before giving up
        // -1 means that the poll will block until there is something on
        // the polled sockets
        boost::int32_t timeout = -1;
        boost::shared_ptr<PeerStatus> serverStatus;
        bool running = false;

        //Socket connected to the server
        zmq::socket_t server(this->context, ZMQ_DEALER);
        //Set the identity for the DEALER socket
        server.setsockopt(ZMQ_IDENTITY, this->channelIdentity.c_str(),
                          strlen(this->channelIdentity.c_str()));
        server.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
        server.connect(this->serverAddress.c_str());

        //Socket through which we pass control messages from the client to this thread
        zmq::socket_t control(this->context, ZMQ_PAIR);
        control.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
        control.connect(this->controlAddress.c_str());

        //Socket through which we pass control messages from the client to this thread
        zmq::socket_t bridgeSocket(this->context, ZMQ_ROUTER);
        bridgeSocket.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
        bridgeSocket.bind(this->bridgeAddress.c_str());

        //Try to connect to the server. An exception will be thrown if this fails
        running = this->tryConnectToServer(control, server, serverStatus, timeout);

        zmq::pollitem_t items[] =
        {
            {control,0,ZMQ_POLLIN,0},
            {server, 0, ZMQ_POLLIN, 0 },
            {bridgeSocket, 0, ZMQ_POLLIN, 0}
        };

        ClientPongHandler pongHandler(serverStatus);
        ClientPingHandler pingHandler(server);
        ServiceResponseHandler responseHandler(bridgeSocket, serviceRequests, serviceResponses, serviceMutex);

        while(running)
        {
            zmq::poll(items, 3, timeout);

            if (items[0].revents & ZMQ_POLLIN)
            {
                boost::shared_ptr<BaseMessage> controlMessage;
                if(!ZmqUtil::receive(control, controlMessage))
                {
                    LERROR<<"Received invalid message from control socket.";
                }
                else if(controlMessage->type()!=Util::getMessageType(InternalDisconnectRequest::default_instance()))
                {
                    LERROR<<"Received message of invalid type from control socket."<<controlMessage->DebugString();
                }
                else
                {
                    running = false;

                    InternalDisconnectReply disconnectReply;
                    ZmqUtil::send(control, disconnectReply);
                }
            }

            if(items[1].revents & ZMQ_POLLIN)
            {
                serverStatus->reset();

                std::vector<shared_ptr<zmq::message_t> > messages;
                ZmqUtil::receiveCompositeMessage(server, messages);

                if(pingHandler.canHandle(messages))
                {
                    pingHandler.handle(messages);
                }
                else if(pongHandler.canHandle(messages))
                {
                    pongHandler.handle(messages);
                }
                else if(responseHandler.canHandle(messages))
                {
                    responseHandler.handle(messages);
                }
                else
                {
                    LERROR<<"Invalid message from server.";
                }
            }
            else if(serverStatus->decreaseLiveliness())
            {
                //If the server's life has expired, ping it
                if(!ZmqUtil::sendCompositeMessage(server, MessageType::ALIVE_PING))
                {
                    LERROR<<"Failed to send ping message to server.";
                }
            }

            if(items[2].revents& ZMQ_POLLIN)
            {
                std::string callId;
                ServiceRequestAvailable requestAvailable;
                boost::shared_ptr<BaseMessage> internalRequest;

                if(!ZmqUtil::receiveFromPeer(bridgeSocket, internalRequest, callId))
                {
                    LERROR<<"Invalid internal service request.";
                }
                else if(!requestAvailable.ParseFromString(internalRequest->data()))
                {
                    LERROR<<"Invalid request:"<<internalRequest->DebugString();
                }
                else
                {
                    unique_lock<mutex> requestLock(*this->serviceMutex);
                    std::map<std::string, std::pair<const google::protobuf::Message*, std::string> >::iterator it;
                    it = this->serviceRequests->find(callId);

                    if(it!=this->serviceRequests->end())
                    {
                        std::string methodName = it->second.second;
                        const google::protobuf::Message* inputData = it->second.first;
                        //Send the request to the server
                        ZmqUtil::sendServiceRequest(server, callId, methodName, inputData);
                    }
                    else
                    {
                        LERROR<<"Logical error in passing service request between threads.";
                    }
                }
            }

            if(!serverStatus->isAlive())
            {
                std::map<std::string, std::pair<const google::protobuf::Message*, std::string> >::iterator it;
                boost::shared_ptr<zmq::message_t> errorMessage=ZmqUtil::stringToZmq("Connection to server timeout.");
                ServiceResponseAvailable responseAvailable;

                unique_lock<mutex> serviceLock(*this->serviceMutex);

                for(it=serviceRequests->begin(); it!=serviceRequests->end(); ++it)
                {

                    this->serviceResponses->insert(make_pair(it->first,make_pair(false, errorMessage)));
                    ZmqUtil::sendToPeer(bridgeSocket,it->first,responseAvailable);
                }

                this->serviceRequests->clear();
            }
        }

        server.disconnect(this->serverAddress.c_str());
        server.close();

        // https://github.com/zeromq/libzmq/issues/949
        //bridgeSocket.unbind(this->bridgeAddress.c_str());
        bridgeSocket.close();
        control.disconnect(this->controlAddress.c_str());
        control.close();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
    }
    catch(...)
    {
        LERROR<<"Unexpected error in channel worker thread.";
    }

//    LDEBUG<<"Exiting worker thread.";

}

}
