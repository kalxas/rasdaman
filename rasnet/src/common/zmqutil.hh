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

#ifndef RASNET_SRC_COMMON_ZMQUTIL_HH
#define RASNET_SRC_COMMON_ZMQUTIL_HH

#include <vector>

#include <boost/shared_ptr.hpp>
#include <boost/cstdint.hpp>

#include <google/protobuf/message.h>

#include "../../../common/src/zeromq/zmq.hh"

#include "../messages/communication.pb.h"

namespace rasnet
{
/**
 * @brief The ZmqUtil class Contains utility functions for working with ZMQ sockets
 * and Google Protobuf messages within the rasnet protocol.
 */
class ZmqUtil
{
public:
    static const std::string ALL_LOCAL_INTERFACES;

    /**
     * @brief isSocketReadable
     * Poll the socket for the given number of milliseconds and check
     * if reading is possible
     * @param socket
     * @param timeout
     * @return TRUE if the socket is readable withing the given timeout,
     * FALSE otherwise
     */
    static bool isSocketReadable(zmq::socket_t& socket, long timeout);

    /**
     * @brief isSocketWritable
     * Poll the socket for the given number of milliseconds and check
     * if writing is possible
     * @param socket
     * @param timeout
     * @return TRUE if the socket is writable withing the given timeout,
     * FALSE otherwise
     */
    static bool isSocketWritable(zmq::socket_t& socket, long timeout);

    /**
     * @brief messageToString Convert the data of the ZMQ message to a string.
     * @param message
     * @return String representation of the byte contents of the message
     */
    static std::string messageToString(zmq::message_t& message);

    /**
     * @brief stringToZmq Serialize the string into a ZMQ message
     * @param str
     * @return
     */
    static boost::shared_ptr<zmq::message_t> stringToZmq(const std::string& str);

    /**
     * @brief toInprocAddress Add the improc:// prefix if it is missing
     * @param address
     * @return
     */
    static std::string toInprocAddress(const std::string& address);

    /**
     * @brief toTcpAddress Add the tcp:// prefix if it is missing
     * @param address
     * @return
     */
    static std::string toTcpAddress(const std::string& address);

    /**
     * @brief toEndpoint Converts address and port to an endopoint which is of
     * the form "address:port"
     * @param address
     * @param port
     * @return address+ ENDPOINT_SEPARATOR +  port
     */
    static std::string toEndpoint(const std::string& address, u_int16_t port);

    /**
    * Serialize the given protobuffer message and send it using zero-copy over the ZeroMQ socket.
    * The message is wrapped into a BaseMessage
     * @param sock The socket used to send the message
     * @param mess The protobuf message we want to send
     * @return TRUE if the sending was successful, FALSE otherwise
     */
    static bool send(zmq::socket_t &sock, const google::protobuf::Message &mess);

    /**
     * @brief sendToPeer Send the protobuf message to the peer identified by the given ID.
     * @param sock ZMQ_ROUTER socket that acts as the server
     * @param peerId Identity of the ZMQ_DEALER socket that uniquely identifies the client
     * relative to the ZMQ_ROUTER socket
     * @param message The Google protobuf message
     * @return TRUE if the sending was successful, FALSE otherwise
     */
    static bool sendToPeer(zmq::socket_t &sock, const std::string& peerId,const google::protobuf::Message &message);

    /**
    * Receive a message from the socket.
    * @param socket The socket from which to read.
    * @param out_message Container for storing the message
    * @return TRUE if the receiving was successful, FALSE otherwise
    */
    static bool receive(zmq::socket_t &sock, boost::shared_ptr<BaseMessage>& out_message);

    /**
    * Receive a message from the socket and save the identity in the identity parameter and the
    * message in the message parameter. This call is blocking.
    * @param socket Socket to receive the message
    * @param out_peerId Save the identity of the peer in this string
    * @param out_message Save the incoming message in this string
    * @return true if it successfully received a message, false otherwise
    */
    static bool receiveFromPeer(zmq::socket_t &socket, boost::shared_ptr<BaseMessage>& out_message, std::string& out_peerId);

    /**
     * @brief sendCompositeMessage Send a message entirely defined by its type.
     * E.g. ALIVE_PONG, ALIVE_PING
     * A MessageType message will be created, serialized and sent through the socket
     * @param socket
     * @param type
     * @return true if the message was sent successfuly
     */
    static bool sendCompositeMessage(zmq::socket_t &socket, const MessageType::Types& type);

    /**
     * @brief sendCompositeMessage
     * Create a MessageType with the given type and send the following message
     * through the socket:
     * |Serialized MessageType | Serialized message |
     * @param socket Socket through which to send the message
     * @param type
     * @param message Protobuf message corresponding to the type
     * @return
     */
    static bool sendCompositeMessage(zmq::socket_t &socket, const MessageType::Types& type,
                                     const ::google::protobuf::Message& message);

    static bool sendCompositeMessageToPeer(zmq::socket_t &socket, const std::string &peerId,
                                           const MessageType::Types& type);
    /**
     * @brief sendCompositeMessageToPeer
     * | Peer ID | Serialized MessageType | Serialized Message|
     * @param socket
     * @param peerId
     * @param type
     * @param message
     * @return
     */
    static bool sendCompositeMessageToPeer(zmq::socket_t &socket, const std::string &peerId,
                                           const MessageType::Types& type, const ::google::protobuf::Message& message);

    /**
     * @brief sendServiceRequest
     * | MessageType = SERVICE_REQUEST | Call ID | Method Name | Serialized Input Data|
     * @param socket
     * @param message
     * @param methodName
     * @return
     */
    static bool sendServiceRequest(zmq::socket_t& socket, const std::string& callId,
                                   const std::string& methodName, const google::protobuf::Message *inputValue );

    /**
     * @brief sendServiceResponseSuccess Send a message of the format
     * |MessageType| Call ID | ServiceCallStatus | outputValue|
     * ServiceCallStatus.success = true
     * to the peer uniquely identified by peerId relative to the socket
     * @param socket ZMQ_ROUTER socket (no checks are made)
     * @param peerId
     * @param callId ID of the call, unique to the Channel that sent the request
     * @param outputValue Pointer to the serialized output value.
     * The data is owned by this method
     * @param outputValueSize The number of bytes in the outputValue
     * @return TRUE for success, FALSE in case of failure
     */
    static bool sendServiceResponseSuccess(zmq::socket_t& socket, const std::string& peerId, const std::string& callId,
                                           ::google::protobuf::uint8 *outputValue,
                                           int outputValueSize);

    /**
     * @brief sendServiceResponseFailure Send a message of the format
     * |MessageType| Call ID | ServiceCallStatus | failureMessage|
     * ServiceCallStatus.success = false
     * @param socket ZMQ_ROUTER socket (no checks are made)
     * @param peerId
     * @param callId ID of the call, unique to the Channel that sent the request
     * @param failureMessage Message representing failure to be sent to the peer
     * @return
     */
    static bool sendServiceResponseFailure(zmq::socket_t& socket, const std::string& peerId, const std::string& callId,
                                           const std::string& failureMessage );

    /**
     * @brief receiveCompositeMessage Read a multipart message from the socket.
     * @param socket
     * @param out_messages Vector of zmq::message_t, one for each message part
     */
    static void receiveCompositeMessage(zmq::socket_t &socket, std::vector<boost::shared_ptr<zmq::message_t> >& out_messages);

    /**
     * @brief receiveCompositeMessageFromPeer Read a multipart message from the socket.
     * The first part of the message is considered to be the ID of the peer.
     * @param socket ZMQ_ROUTER socket
     * @param out_peerId The peer ID
     * @param out_messages Vector of zmq::message_t, one for each message part
     */
    static void receiveCompositeMessageFromPeer(zmq::socket_t &socket, std::string& out_peerId, std::vector<boost::shared_ptr<zmq::message_t> >& out_messages);
private:
    static const std::string TCP_PREFIX;
    static const std::string INPROC_PREFIX;
    static const std::string ENDPOINT_SEPARATOR;

    /**
     * @brief freeByteArray Callback function used by ZMQ to send a message
     * using zero-copy
     * @param data
     * @param hint
     */
    static void freeByteArray(void *data, void *hint);

    static std::string addPrefixIfMissing(std::string str, std::string prefix);

    static bool sendString(zmq::socket_t &socket, const std::string &string, int flags=0);
    static std::string receiveString(zmq::socket_t &socket);
    static void stringDump(zmq::socket_t &socket);
    static bool rawSend(zmq::socket_t& socket, const google::protobuf::Message &message, int flags=0);
};
}
#endif // RASNET_SRC_COMMON_ZMQUTIL_HH
