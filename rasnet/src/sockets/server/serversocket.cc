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

/* SOURCE: ServerSocket.cc
 *
 * MODULE:
 * CLASS:   ServerSocket
 *
 * COMMENTS: ServerSocket is the class that is directly used by the application
 * to talk with multiple clients.
 *
 *
 */

#include <stdexcept>
#include <boost/thread.hpp>

#include "../../../../common/src/time/timer.hh"
#include "../../../../common/src/uuid/uuid.hh"
#include "../../../../common/src/logging/easylogging++.hh"
#include "../../../src/util/proto/protozmq.hh"
#include "../../../src/util/proto/zmqutil.hh"

#include "../../../src/messages/test_mess.pb.h"

#include "serversocket.hh"
#include "serverpinghandler.hh"
#include "serverponghandler.hh"
#include "connectrequesthandler.hh"

namespace rasnet
{
using std::string;
using std::runtime_error;

using boost::unique_lock;
using boost::thread;
using boost::shared_ptr;
using boost::mutex;

using common::UUID;
using base::BaseMessage;


  //TODO-AT: Remove this
ServerSocket::ServerSocket(std::string endpoint, ServerSocketConfig config) :
        endpoint(endpoint), config(config), context(2, 2048)
{
    // Generate a UI for this socket that is used internally for communication
    // between the different threads.
    this->identity = UUID::generateUUID();
    this->clientPool.reset(new ClientPool());

    shared_ptr<ServerCommunicationHandler> pingHandler(new ServerPingHandler());
    this->handlers.push_back(pingHandler);
    shared_ptr<ServerCommunicationHandler> pongHandler(new ServerPongHandler(this->clientPool));
    this->handlers.push_back(pongHandler);
    shared_ptr<ServerCommunicationHandler> connectRequestHandler(new ConnectRequestHandler(this->clientPool, this->config.getAliveRetryNo(),this->config.getAliveTimeout()));
    this->handlers.push_back(connectRequestHandler);

    //Address used internally to communicate between the two threads of the ServerSocket
    this->messagePipeAddr = ZmqUtil::toInprocAddress("message" + this->identity);
    this->controlPipeAddr = ZmqUtil::toInprocAddress("control" + this->identity);

    this->serverSocket.reset(new zmq::socket_t(this->context, ZMQ_PAIR));
    this->controlSocket.reset(new zmq::socket_t(this->context, ZMQ_PAIR));

    //Spawn the thread that manages communication related messaging
    this->proxyThread.reset(new thread( boost::bind( &ServerSocket::runProxy, this)));

    this->serverSocket->connect(this->messagePipeAddr.c_str());
    this->controlSocket->connect(this->controlPipeAddr.c_str());
}

ServerSocket::~ServerSocket()
{
    try
    {
        this->stopProxyThread();
        this->proxyThread->join();
        {
            unique_lock<mutex> lock(this->socketMutex);
            this->serverSocket->disconnect(this->messagePipeAddr.c_str());
            this->controlSocket->disconnect(this->controlPipeAddr.c_str());
        }
        this->handlers.clear();
        this->clientPool->removeAllClients();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
    }
    catch(...)
    {
        LERROR<<"ServerSocket destructor failed.";
    }
}

bool ServerSocket::pollIn(long timeout)
{
    unique_lock<mutex> lock(this->socketMutex);
    zmq::pollitem_t items[] = { { *(this->serverSocket.get()), 0, ZMQ_POLLIN, 0 } };
    return (zmq::poll(items, 1, timeout) > 0);
}

bool ServerSocket::pollOut(long timeout)
{
    unique_lock<mutex> lock(this->socketMutex);
    zmq::pollitem_t items[] = { { *(this->serverSocket.get()), 0, ZMQ_POLLOUT, 0 } };
    return (zmq::poll(items, 1, timeout) > 0);
}


void ServerSocket::receive(std::string& peerId, base::BaseMessage& message)
{
    unique_lock<mutex> lock(this->socketMutex);
    if(!ProtoZmq::zmqReceiveFromPeer(*(this->serverSocket.get()),peerId, message))
    {
        throw runtime_error("Could not receive message from peer");
    }
}

void ServerSocket::send(const std::string& peerId, google::protobuf::Message& message)
{
    unique_lock<mutex> lock(this->socketMutex);
    if(!ProtoZmq::zmqSendToPeer(*(this->serverSocket.get()), message, peerId))
    {
        throw runtime_error("Could not receive message from peer");
    }
}

bool ServerSocket::isPeerAlive(const std::string& peerId)
{
    return this->clientPool->isClientAlive(peerId);
}

bool ServerSocket::handle(base::BaseMessage& message, std::string identity,
                          zmq::socket_t& socket)
{
    bool handled = false;
    for (size_t i = 0; i < this->handlers.size(); i++)
    {
        if (this->handlers[i]->canHandle(message))
        {
            this->handlers[i]->handle(message, identity, socket);
            handled = true;
        }
    }
    return handled;
}

void ServerSocket::stopProxyThread()
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

void ServerSocket::runProxy()
{
    bool keepRunningThread=true;
    base::BaseMessage messageFromClient;
    base::BaseMessage messageFromServer;
    base::BaseMessage controlMessage;
    std::string inClientId;
    std::string outClientId;

    //Socket used to receive control messages
    zmq::socket_t control(this->context, ZMQ_PAIR);
    control.bind(this->controlPipeAddr.c_str());

    zmq::socket_t toServer(this->context, ZMQ_PAIR);
    toServer.bind(this->messagePipeAddr.c_str());

    zmq::socket_t fromClient(this->context, ZMQ_ROUTER);
    fromClient.bind(this->endpoint.c_str());

    zmq::pollitem_t items[] = { { fromClient, 0, ZMQ_POLLIN, 0 }, { toServer, 0,
                                ZMQ_POLLIN, 0 },{ control, 0, ZMQ_POLLIN, 0 } };

    zmq::pollitem_t items1[] = { { fromClient, 0, ZMQ_POLLOUT, 0 } };

    while(keepRunningThread)
    {
        //Wait forever for a message
        zmq::poll(items, 3, this->clientPool->getMinimumPollPeriod());

        if (items[0].revents & ZMQ_POLLIN)
        {
            if (ProtoZmq::zmqReceiveFromPeer(fromClient, inClientId,
                                             messageFromClient))
            {
                if (!this->handle(messageFromClient, inClientId, fromClient))
                {
                    ProtoZmq::zmqRawSendToPeer(toServer, messageFromClient,
                                               inClientId);
                }
                this->clientPool->resetClientStatus(inClientId);
            }
        }
        else
        {
            this->clientPool->pingAllClients(fromClient);
            this->clientPool->removeDeadClients();
        }


        if (items[1].revents & ZMQ_POLLIN)
        {
            if (ProtoZmq::zmqReceiveFromPeer(toServer, outClientId,
                                             messageFromServer))
            {
                //TODO What to do to prevent messages being dropped?
                //    zmq::poll(items1, 1, -1);
                ProtoZmq::zmqRawSendToPeer(fromClient, messageFromServer, outClientId);
            }
        }

        if (items[2].revents & ZMQ_POLLIN)
        {
            ProtoZmq::zmqReceive(control, controlMessage);
            if(controlMessage.type()==InternalDisconnectRequest::default_instance().GetTypeName())
            {
                keepRunningThread=false;
                InternalDisconnectReply disconnectReply;
                ProtoZmq::zmqSend(control, disconnectReply);
            }
        }


    }
}
}
