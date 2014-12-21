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

#ifndef RASNET_SRC_SOCKETS_CLIENT_CLIENTSOCKET_HH_
#define RASNET_SRC_SOCKETS_CLIENT_CLIENTSOCKET_HH_

#include <string>
#include <vector>
#include <boost/cstdint.hpp>
#include <boost/smart_ptr.hpp>
#include <boost/thread.hpp>
#include <google/protobuf/message.h>

#include "../../../../common/src/zeromq/zmq.hh"
#include "../../messages/base.pb.h"
#include "../common/peerstatus.hh"

#include "clientcommunicationhandler.hh"
#include "clientsocketconfig.hh"

namespace rasnet
{
class ClientSocket
{
public:
    /**
     * Create a ClientSocket within the specified ZeroMQ context
     * @param serverAddress A string of the type: tcp://address.
     * @param the number of IO threads to be used by the client socket. As a rule of thumb, 1 thread for each 1GB/s
     * @throw std::runtime_error An error is thrown if the object state cannot be initialized.
     */
    ClientSocket(const std::string& serverAddress, ClientSocketConfig config);

    ~ClientSocket();

    /**
     * Receive a message from the endpoint the socket is connected to and store it
     * into the \a message argument. If there are no messages available on the
     * specified socket the method shall block until the request can be satisfied
     * or until the peer is detected as dead.
     * This method is thread-safe.
     * @param message
     * @throws zmq::error_t
     */
    void receive(base::BaseMessage& message);

    /**
     * The method shall queue the message referenced by the \a message argument
     * to be sent to the endpoint the socket is connected to.
     * This method is thread-safe. It throws exceptions if it detects that the peers is dead.
     * @param message
     * @throws zmq::error_t, std::runtime_error
     */
    void send(google::protobuf::Message& message);

    /**
     * Poll the socket for incoming messages
     * This method is thread-safe.
     * @param timeout Number of milliseconds until the poll will return if there are no messages. -1 means that the poll will block forever.
     * @return TRUE if a message is waiting to be read, FALSE otherwise
     */
    bool pollIn(long timeout);

    /**
     * Poll the socket for outgoing messages i.e. check if we can send a message without blocking
     * This method is thread-safe.
     * @param timeout Number of milliseconds until the poll will return if there are no messages. -1 means that the poll will block forever.
     * @return true if a message can be sent without blocking, false otherwise
     */
    bool pollOut(long timeout);

    bool isPeerAlive();

private:
    int linger;
    zmq::context_t context;/*!< ZeroMQ context that owns the socket communicating to the ClientProxy and the sockets used in the ClientProxy*/
    ClientSocketConfig config;
    boost::scoped_ptr<zmq::socket_t> clientSocket; /*!< ZeroMQ PAIR socket used to connect with the ClientProxy*/
    boost::scoped_ptr<zmq::socket_t> controlSocket; /*!< ZeroMQ PAIR socket used to connect with the ClientProxy*/

    std::string serverAddress;
    std::string identity;/*!< Unique identity that is used as a socket address for interthread communication between the ClientProxy and the ClientSocket*/

    std::string messagePipeAddr;
    std::string controlPipeAddr;

    boost::scoped_ptr<boost::thread>  proxyThread;

    boost::mutex socketMutex; /*!< Mutex used to synchronize access to the client socket*/

    boost::mutex statusMutex;
    boost::scoped_ptr<PeerStatus> serverStatus;

    std::vector<boost::shared_ptr<ClientCommunicationHandler> > connectionHandlers;

    void runProxy();
    void connectToServer();
    void disconnectFromServer();
    bool handleServerMessage(const base::BaseMessage& message,zmq::socket_t& replySocket);
};
}
#endif /* SOCKETS_CLIENT_CLIENTSOCKET_HH_ */
