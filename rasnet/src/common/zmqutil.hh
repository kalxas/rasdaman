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
class ZmqUtil
{
public:
    static const std::string ALL_LOCAL_INTERFACES;

    static void freeByteArray(void *data, void *hint);

    static bool isSocketReadable(zmq::socket_t& socket, long timeout);

    static bool isSocketWritable(zmq::socket_t& socket, long timeout);

    /**
     * @brief messageToString Convert the data of the ZMQ message to a string.
     * @param message
     * @return
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
     * @brief toEndpoint Converts address and port to endopoint which is of form "address:port"
     * @param address
     * @param port
     * @return
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
     * @param peerId Identity of the ZMQ_DEALER socket that represents the client
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

    //Type | ID | error | data
    static bool sendServiceResponseSuccess(zmq::socket_t& socket, const std::string& peerId, const std::string& callId,
                                           ::google::protobuf::uint8 *outputValue,
                                           int outputValueSize);

    static bool sendServiceResponseFailure(zmq::socket_t& socket, const std::string& peerId, const std::string& callId,
                                           const std::string& failureMessage );

    static void receiveCompositeMessage(zmq::socket_t &socket, std::vector<boost::shared_ptr<zmq::message_t> >& out_messages);

    static void receiveCompositeMessageFromPeer(zmq::socket_t &socket, std::string& out_peerId, std::vector<boost::shared_ptr<zmq::message_t> >& out_messages);

private:
    static const std::string TCP_PREFIX;
    static const std::string INPROC_PREFIX;
    static const std::string ENDPOINT_SEPARATOR;

    static std::string addPrefixIfMissing(std::string str, std::string prefix);

    static bool sendString(zmq::socket_t &socket, const std::string &string, int flags=0);
    static std::string receiveString(zmq::socket_t &socket);
    static void stringDump(zmq::socket_t &socket);
    static bool rawSend(zmq::socket_t& socket, const google::protobuf::Message &message, int flags=0);
};
}
#endif // RASNET_SRC_COMMON_ZMQUTIL_HH
