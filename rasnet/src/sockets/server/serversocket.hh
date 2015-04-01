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

/* INCLUDE: ServerSocket.hh
 *
 * MODULE:  zmq_components/server
 * CLASS:   ServerSocket
 *
 * COMMENTS: ServerSocket is the class that is directly used by the application
 * to talk with multiple clients.
 *
 *
 */
#ifndef RASNET_SRC_SOCKETS_SERVER_SERVERSOCKET_HH_
#define RASNET_SRC_SOCKETS_SERVER_SERVERSOCKET_HH_

#include <string>
#include <vector>

#include <boost/cstdint.hpp>
#include <boost/smart_ptr.hpp>
#include <boost/thread.hpp>

#include <google/protobuf/message.h>

#include "../../../../common/src/zeromq/zmq.hh"

#include "../../../src/messages/base.pb.h"

#include "clientpool.hh"
#include "serversocketconfig.hh"
#include "servercommunicationhandler.hh"

namespace rasnet
{
class ServerSocket
{
public:
    /**
     * Initialize an instance of the ServerSocket class and bind the server to an endpoint.
     * When the contstructor returns, the socket is bound to the endpoint and can receive messages.
     * @param endpoint Endpoint to which this socket should bind.
     * @param config Configuration object used to transfer information about the sockets properties
     */
    ServerSocket(std::string endpoint, ServerSocketConfig config);

    virtual ~ServerSocket();

    /**
     * Receive a message from a peer. Receiving fair-queues messages from all the connected peers.
     * The id of the peer is saved in the peer_id parameter and the message is saved in the message parameter.
     * This call is blocking.
     * @param peer_id
     * @param message
     */
    void receive(std::string& peerId, base::BaseMessage& message);

    /**
     * Send a message to the peer with the given peer_id.
     * This call will block if the queue of outgoing messages is full.
     * @param peer_id
     * @param message
     */
    void send(const std::string& peerId, google::protobuf::Message& message);

    /**
     * Poll the socket for incoming messages
     * @param timeout Number of milliseconds until the poll will return if there are no messages. -1 means that the poll will block forever.
     * @return true if a message is waiting to be read, false otherwise
     */
    bool pollIn(long timeout = -1);

    /**
     * Poll the socket for outgoing messages i.e. check if we can send a message without blocking
     * @param timeout Number of milliseconds until the poll will return if there are no messages. -1 means that the poll will block forever.
     * @return true if a message can be sent without blocking, false otherwise
     */
    bool pollOut(long timeout = -1);

    /**
     * Determine if the peer with the given id is responding to requests.
     * @param peer_id
     * @return true if the peer is responding to ping requests, false otherwise
     */
    bool isPeerAlive(const std::string& peerId);

private:
    ServerSocketConfig config; /*!<Configuration objecct used to setup socket parameters */
    zmq::context_t context;/*!< ZeroMQ context that owns the sockets used for communicating with the client and the second thread of the server socket*/
    boost::shared_ptr<zmq::socket_t> serverSocket; /*!< ZMQ socket used to forward messages to the proxy thread which forwards them to the client */
    boost::shared_ptr<zmq::socket_t> controlSocket; /*!< Used to send commands from the main thread to the proxy thread. */
    boost::mutex socketMutex; /*!< Mutex used to synchronize access to the serverSocket object as zmq sockets are not thread safe*/
    std::string endpoint;/*!< Address to which the server socket will bind */

    std::string identity; /*!< Identity used internally by the server socket for communicating between threads */
    std::string messagePipeAddr; /*! inproc:// address of the sockets used for inter-thread communication */
    std::string controlPipeAddr; /*! inproc:// address of the sockets used for inter-thread communication */
    boost::scoped_ptr<boost::thread> proxyThread;

    boost::shared_ptr<ClientPool> clientPool;

    std::vector<boost::shared_ptr<ServerCommunicationHandler> > handlers;

    /**
     * Go through all the message ServerProxyHandlers and check if at least one of them can handle the message.
     * @param socket Socket used to send a reply message
     * @param identity The identity of the peer that sent the message
     * @param message BaseMessage containing a protobuf message that must be handled
     * @return true if the message was handled, false otherwise
     */
    bool handle(base::BaseMessage& message, std::string identity,
                zmq::socket_t& socket);

    void stopProxyThread();

    /**
     * Run the proxy thread responsible for handling communication related messages
     * and only forwarding non-communication messages to the serverSocket.
     */
    void runProxy();
};
}

#endif /* RASNET_SRC_SOCKETS_SERVER_SERVERSOCKET_HH_ */
