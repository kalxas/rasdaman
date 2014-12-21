/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

/*
 * SOURCE: ClientSocket.cc
 *
 * MODULE:  zmq_components/client
 * CLASS:   ClientSocket
 *
 * COMMENTS: ClientSocket is the class that is directly used by the application
 * to talk with the server.
 *
 *
 */
#include <iostream>
#include <stdexcept>

#include <zmq.h>

#include "../../../../common/src/logging/easylogging++.hh"
#include "../../../../common/src/uuid/uuid.hh"
#include "../../util/proto/protozmq.hh"
#include "../../messages/communication.pb.h"
#include "../../messages/test_mess.pb.h"

#include "clientsocketconfig.hh"
#include "clientsocket.hh"
#include "clientpinghandler.hh"
#include "clientponghandler.hh"

namespace rasnet
{
using std::string;
using std::runtime_error;
using boost::unique_lock;
using boost::mutex;
using boost::thread;
using boost::shared_ptr;
using boost::timed_mutex;
using common::UUID;
using base::BaseMessage;

ClientSocket::ClientSocket(const std::string& serverAddress, ClientSocketConfig config) :
        context(config.getNumberOfIoThreads())
{
    linger = 0;
    this->config=config;
    this->serverAddress = serverAddress;
    this->identity = UUID::generateUUID();

    shared_ptr<ClientCommunicationHandler> pingHandler(new ClientPingHandler());
    this->connectionHandlers.push_back(pingHandler);
    shared_ptr<ClientCommunicationHandler> pongHandler(new ClientPongHandler());
    this->connectionHandlers.push_back(pongHandler);

    //Create an internal address to be used between by the client and the proxy thread.
    this->messagePipeAddr = "inproc://message" + this->identity;
    this->controlPipeAddr = "inproc://control" + this->identity;

    this->proxyThread.reset(new thread(boost::bind( &ClientSocket::runProxy, this)));

    this->clientSocket.reset(new zmq::socket_t(this->context, ZMQ_PAIR));
    this->clientSocket->setsockopt(ZMQ_LINGER, &linger, sizeof (linger));

    this->controlSocket.reset(new zmq::socket_t(this->context, ZMQ_PAIR));
    this->controlSocket->setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
    this->clientSocket->connect(this->messagePipeAddr.c_str());
    this->controlSocket->connect(this->controlPipeAddr.c_str());

    //Wait for a predefined amount of time for the connection to succeed before
    //throwing an exception
    this->connectToServer();
}

ClientSocket::~ClientSocket()
{
    try
    {
        this->disconnectFromServer();
        this->proxyThread->join();
        {
            unique_lock<mutex> lock(this->socketMutex);
            this->clientSocket->disconnect(this->messagePipeAddr.c_str());
            this->controlSocket->disconnect(this->controlPipeAddr.c_str());
        }
        this->connectionHandlers.clear();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
    }
    catch(...)
    {
        LERROR<<"ClientSocket: Unexepcted exception";
    }
}

void ClientSocket::receive(base::BaseMessage& message)
{
    unique_lock<mutex> lock(this->socketMutex);
    //Receive message and throw exception on error
    if(ProtoZmq::zmqReceive(*(this->clientSocket.get()), message)==false)
    {
        throw runtime_error("Receiving of message failed");
    }
}

void ClientSocket::send(google::protobuf::Message& message)
{
    unique_lock<mutex> lock(this->socketMutex);
    if(ProtoZmq::zmqSend(*(this->clientSocket.get()), message)==false)
    {
        throw runtime_error("Sending of message failed");
    }
}

bool ClientSocket::pollIn(long timeout)
{
    unique_lock<mutex> lock(this->socketMutex);
    zmq::pollitem_t items[] = { { *(this->clientSocket.get()), 0, ZMQ_POLLIN, 0 } };
    return zmq::poll(items, 1, timeout) > 0;
}

bool ClientSocket::pollOut(long timeout)
{
    unique_lock<mutex> lock(this->socketMutex);
    zmq::pollitem_t items[] = { { *(this->clientSocket.get()), 0, ZMQ_POLLOUT, 0 } };
    return zmq::poll(items, 1, timeout) > 0;
}

bool ClientSocket::isPeerAlive()
{
    unique_lock<mutex> lock(this->statusMutex);
    this->serverStatus->decreaseLiveliness();

    return this->serverStatus->isAlive();
}

void ClientSocket::runProxy()
{
    base::BaseMessage frontMessage;
    base::BaseMessage backMessage;
    base::BaseMessage controlMessage;
    AlivePing ping;
    bool keepRunning=false;

    linger = 0;

    // The number of milliseconds to poll before giving up
    // -1 means that the poll will block until there is something on
    // the polled sockets
    boost::int32_t timeout = -1;

    //Socket through which we pass control messages from the client to this thread
    zmq::socket_t control(this->context, ZMQ_PAIR);
    control.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
    control.bind(this->controlPipeAddr.c_str());

    //Socket connected to the client socket through which we pass messages
    //to and from the server
    zmq::socket_t backend(this->context, ZMQ_PAIR);
    backend.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
    backend.bind(this->messagePipeAddr.c_str());


    //Socket connected to the server
    zmq::socket_t frontend(this->context, ZMQ_DEALER);
    //Set the identity for the DEALER socket
    frontend.setsockopt(ZMQ_IDENTITY, this->identity.c_str(),
                        strlen(this->identity.c_str()));
    frontend.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
    frontend.connect(this->serverAddress.c_str());


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
                    unique_lock<mutex> lock(this->statusMutex);
                    this->serverStatus.reset(new PeerStatus(connectionReply.retries(), connectionReply.period()));
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

    while(keepRunning)
    {
        zmq::pollitem_t items[] = { { frontend, 0, ZMQ_POLLIN, 0 },
                                    { backend, 0,ZMQ_POLLIN, 0 },
                                    {control,0,ZMQ_POLLIN,0},
                                  };

        zmq::poll(items, 3, timeout);

        if (items[0].revents & ZMQ_POLLIN)
        {
            //If we receive a message from the server, reset its status
            unique_lock<mutex> lock(this->statusMutex);
            this->serverStatus->reset();
            lock.unlock();

            if(ProtoZmq::zmqReceive(frontend, frontMessage))
            {
                //If we cannot handle the message here, pass it to the ClientSocket
                if(!this->handleServerMessage(frontMessage, frontend))
                {
                    ProtoZmq::zmqRawSend(backend, frontMessage);
                }
            }
        }
        else
        {
            unique_lock<mutex> lock(this->statusMutex);
            //Decrease liveliness and send out a ping to see if the server is alive
            if(this->serverStatus->decreaseLiveliness())
            {
                ProtoZmq::zmqSend(frontend, ping);
            }
        }

        if (items[1].revents & ZMQ_POLLIN)
        {

            if(ProtoZmq::zmqReceive(backend, backMessage))
            {
                ProtoZmq::zmqRawSend(frontend, backMessage);
            }
        }

        if (items[2].revents & ZMQ_POLLIN)
        {
            ProtoZmq::zmqReceive(control, controlMessage);
            if(controlMessage.type()==InternalDisconnectRequest::default_instance().GetTypeName())
            {
                keepRunning=false;
                InternalDisconnectReply disconnectReply;
                ProtoZmq::zmqSend(control, disconnectReply);
            }
        }
    }
}

bool ClientSocket::handleServerMessage(const base::BaseMessage& message, zmq::socket_t& replySocket)
{
    bool handled = false;
    for (size_t i = 0; i < this->connectionHandlers.size(); i++)
    {
        if (this->connectionHandlers[i]->canHandle(message))
        {
            this->connectionHandlers[i]->handle(message, replySocket);
            handled = true;
        }
    }

    return handled;
}

void ClientSocket::connectToServer()
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

void ClientSocket::disconnectFromServer()
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

}
