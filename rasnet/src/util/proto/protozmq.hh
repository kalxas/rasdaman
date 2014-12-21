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

#ifndef RASNET_SRC_UTIL_PROTO_PROTOUTILS_HH_
#define RASNET_SRC_UTIL_PROTO_PROTOUTILS_HH_

#include <string>

#include <google/protobuf/message.h>
#include <google/protobuf/descriptor.h>

#include "../../../../common/src/zeromq/zmq.hh"
#include "../../messages/base.pb.h"

namespace rasnet
{
class ProtoZmq
{
public:
    /**
    * Encapsulate the given protobuffer message into a BaseMessage and send it using zero-copy over the ZeroMQ socket.
     * @param sock The socket used to send the message
     * @param mess The protobuf message we want to send
     * @return TRUE if the sending was successful, FALSE otherwise
     */
    static bool zmqSend(zmq::socket_t &sock, google::protobuf::Message &mess);

    /**
    * Receive a message from the socket.
    * @param socket The socket from which to read.
    * @param message Container for storing the message
    * @return TRUE if the receiving was successful, FALSE otherwise
    */
    static bool zmqReceive(zmq::socket_t &socket, base::BaseMessage &message);

    /**
    * Encapsulate the given protobuffer message into a BaseMessage and send it using zero-copy
    * over the ZeroMQ socket to the peer given by the peer_id.
    * This method assumes that sock is a ROUTER socket which has previously received a message from
    * a peer with the given id.
     * @param sock The socket used to send the message
     * @param mess The protobuf message we want to send
     * @param peer_id The identity of the peer we want to send this to.
     * @return TRUE if the sending was successful, FALSE otherwise
     */
    static bool zmqSendToPeer(zmq::socket_t &sock,
                              google::protobuf::Message &mess, const std::string &peer_id);

    /**
    *Send the BaseMessage directly to the socket without any extra encapsulation using zero-copy
    * over the ZeroMQ socket to the peer given by the peer_id.
    * This method assumes that sock is a ROUTER socket which has previously received a message from
    * a peer with the given id.
     * @param sock The socket used to send the message
     * @param mess The protobuf message we want to send
     * @param peer_id The identity of the peer we want to send this to.
     * @return TRUE if the sending was successful, FALSE otherwise
     */
    static bool zmqRawSendToPeer(zmq::socket_t &sock, base::BaseMessage &mess,
                                 const std::string &peer_id);

    /**
    * Send the BaseMessage directly to the socket without any extra encapsulation
    * over the ZeroMQ socket.
    * @param sock The socket used to send the message
    * @param mess The protobuf message we want to send
    * @return TRUE if the sending was successful, FALSE otherwise
    */
    static bool zmqRawSend(zmq::socket_t &sock, base::BaseMessage &mess);

    /**
    * Obtain a string that uniquely identifies the method in its namespace.
    * @param method
    * @return string that uniquely identifies the method in its namespace.
    */
    static std::string getServiceMethodName(
        const ::google::protobuf::MethodDescriptor *method);



    /**
    * Receive a message from the socket and save the identity in the identity parameter and the
    * message in the message parameter. This call is blocking.
    * @param socket Socket to receive the message
    * @param identity Save the identity of the peer in this string
    * @param message Save the incoming message in this string
    * @return true if it successfully received a message, false otherwise
    */
    static bool zmqReceiveFromPeer(zmq::socket_t &socket, std::string &identity,
                                   base::BaseMessage &message);

    static std::string receiveString(zmq::socket_t &socket);

    static bool stringSendMore(zmq::socket_t &socket, const std::string &string);

private:
    /**
    * Custom deallocation function used for the Zero-Copy sending of messages
    * @param data The data that must be deallocated
    * @param hint Extra information passed to the deallocation function for a custom effect.
    * It is currently not used.
    */
    static void freeArray(void *data, void *hint);

    static void freeString(void *data, void *hint);



    static void stringDump(zmq::socket_t &socket);


};
}

#endif /* RASNET_UTIL_PROTO_PROTOUTILS_HH_ */
