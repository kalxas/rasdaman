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
#include <iomanip>

#include <boost/algorithm/string.hpp>

#include "zmqutil.hh"
#include "util.hh"

#include "../../../common/src/logging/easylogging++.hh"

namespace rasnet
{

const std::string ZmqUtil::TCP_PREFIX = "tcp://";
const std::string ZmqUtil::INPROC_PREFIX = "inproc://";
const std::string ZmqUtil::ENDPOINT_SEPARATOR = ":";

const std::string ZmqUtil::ALL_LOCAL_INTERFACES = ZmqUtil::TCP_PREFIX + "*";

void ZmqUtil::freeByteArray(void *data, void *hint)
{
    delete[] ((::google::protobuf::uint8 *) data);
}

bool ZmqUtil::isSocketReadable(zmq::socket_t &socket, long timeout)
{
    bool readable = false;
    try
    {
        zmq::pollitem_t items[] = { { socket, 0, ZMQ_POLLIN, 0 } };
        //Wait for a predefined number of milliseconds before giving up contacting the server
        readable = zmq::poll(items, 1, timeout) == 1;
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
    }

    return readable;
}

bool ZmqUtil::isSocketWritable(zmq::socket_t &socket, long timeout)
{
    bool writeable = false;
    try
    {
        zmq::pollitem_t items[] = { { socket, 0, ZMQ_POLLOUT, 0 } };
        //Wait for a predefined number of milliseconds before giving up contacting the server
        writeable = zmq::poll(items, 1, timeout) == 1;
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
    }

    return writeable;
}

std::string ZmqUtil::messageToString(zmq::message_t &message)
{
    return std::string(static_cast<char *>(message.data()), message.size());
}

boost::shared_ptr<zmq::message_t> ZmqUtil::stringToZmq(const std::string &str)
{
    boost::shared_ptr<zmq::message_t>  result(new zmq::message_t(str.size()));
    memcpy(result->data(), str.data(), str.size());

    return result;
}


std::string  ZmqUtil::toInprocAddress(const std::string &address)
{
    return addPrefixIfMissing(address, ZmqUtil::INPROC_PREFIX);
}

std::string ZmqUtil::toTcpAddress(const std::string &address)
{
    return addPrefixIfMissing(address, ZmqUtil::TCP_PREFIX);
}

std::string ZmqUtil::toEndpoint(const std::string &address, u_int16_t port)
{
    std::stringstream endpoint;

    endpoint << address << ZmqUtil::ENDPOINT_SEPARATOR << port;

    return endpoint.str();
}

bool ZmqUtil::send(zmq::socket_t &socket, const google::protobuf::Message &mess)
{
    BaseMessage envelope;
    envelope.set_data(mess.SerializeAsString());
    envelope.set_type(Util::getMessageType(mess));

    return ZmqUtil::rawSend(socket, envelope);
}

bool ZmqUtil::sendToPeer(zmq::socket_t &socket, const std::string &peerId, const google::protobuf::Message &message)
{
    bool success=true;
    try
    {
        success = ZmqUtil::sendString(socket, peerId, ZMQ_SNDMORE)  && ZmqUtil::send(socket, message);
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        success=false;
    }

    return success;
}

bool ZmqUtil::receive(zmq::socket_t &socket, boost::shared_ptr<BaseMessage> &out_message)
{
    bool success=true;
    try
    {
        //The first part of the message must be the serialized BaseMessage
        zmq::message_t data;
        success = socket.recv(&data);

        int more = 0; //  Multipart detection
        size_t more_size = sizeof(more);
        socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);
        //If the message has more than one part, it is invalid
        if(more)
        {
            LERROR<<"The message has more parts than expected.";
            success = false;
            ZmqUtil::stringDump(socket);
        }
        else
        {
            out_message.reset(new BaseMessage());
            success =  success && out_message->ParseFromArray(data.data(), data.size());
        }
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        success=false;
    }

    return success;
}

bool ZmqUtil::receiveFromPeer(zmq::socket_t &sock, boost::shared_ptr<BaseMessage> &out_message, std::string &out_peerId)
{
    bool success=false;
    zmq::message_t content;
    int more = 0; //  Multipart detection
    size_t more_size = sizeof(more);

    try
    {
        out_peerId = ZmqUtil::receiveString(sock);

        sock.getsockopt(ZMQ_RCVMORE, &more, &more_size);
        //If the message doesn't have at least two parts, it is invalid.
        //The structure of the received messages on this socket is
        // | Sender ID | Serialized Protobuf message |
        if (more)
        {
            sock.recv(&content);
            sock.getsockopt(ZMQ_RCVMORE, &more, &more_size);
            if (more)
            {
                //The message has more than two parts
                //Dump it and return false
                LERROR<<"Message from peer has more than 2 parts";
                ZmqUtil::stringDump(sock);
            }
            else
            {
                out_message.reset(new BaseMessage());
                success = out_message->ParseFromArray(content.data(), content.size());
            }
        }
        else
        {
            LERROR<<"Message from peer has less than 2 parts.";
        }
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
    }

    return success;
}

bool ZmqUtil::sendCompositeMessage(zmq::socket_t &socket, const MessageType::Types &type)
{
    rasnet::MessageType messageType;
    messageType.set_type(type);

    return ZmqUtil::rawSend(socket, messageType);
}

bool ZmqUtil::sendCompositeMessage(zmq::socket_t &socket, const MessageType::Types &type, const google::protobuf::Message &message)
{
    bool success = true;

    rasnet::MessageType messageType;
    messageType.set_type(type);

    success = success && ZmqUtil::rawSend(socket, messageType, ZMQ_SNDMORE);
    success = success && ZmqUtil::rawSend(socket, message);

    return success;
}

bool rasnet::ZmqUtil::sendCompositeMessageToPeer(zmq::socket_t &socket, const std::string &peerId, const rasnet::MessageType::Types &type)
{
    rasnet::MessageType messageType;
    messageType.set_type(type);

    bool success = ZmqUtil::sendString(socket, peerId, ZMQ_SNDMORE);
    success = success && ZmqUtil::rawSend(socket, messageType);

    return success;
}

bool ZmqUtil::sendCompositeMessageToPeer(zmq::socket_t &socket, const std::string &peerId, const MessageType::Types &type, const google::protobuf::Message &message)
{
    bool success = ZmqUtil::sendString(socket, peerId, ZMQ_SNDMORE);
    success = success && ZmqUtil::sendCompositeMessage(socket, type, message);

    return success;
}

bool ZmqUtil::sendServiceRequest(zmq::socket_t &socket, const std::string &callId, const std::string &methodName, const google::protobuf::Message *inputValue)
{
    rasnet::MessageType messageType;
    messageType.set_type(MessageType::SERVICE_REQUEST);

    bool success = ZmqUtil::rawSend(socket, messageType, ZMQ_SNDMORE);;
    success = success && ZmqUtil::sendString(socket,callId, ZMQ_SNDMORE);
    success = success && ZmqUtil::sendString(socket, methodName, ZMQ_SNDMORE);
    success = success && ZmqUtil::rawSend(socket, *inputValue);

    return success;
}

bool ZmqUtil::sendServiceResponseSuccess(zmq::socket_t &socket, const std::string &peerId, const std::string &callId, google::protobuf::uint8 *outputValue, int outputValueSize)
{
    if(outputValue==NULL)
    {
        LERROR<<"Invalid output value.";
        return false;
    }

    rasnet::MessageType messageType;
    messageType.set_type(MessageType::SERVICE_RESPONSE);
    rasnet::ServiceCallStatus status;
    status.set_success(true);

    bool success = ZmqUtil::sendString(socket, peerId, ZMQ_SNDMORE);
    success = ZmqUtil::rawSend(socket, messageType, ZMQ_SNDMORE);;
    success = success && ZmqUtil::sendString(socket,callId, ZMQ_SNDMORE);
    success = ZmqUtil::rawSend(socket, status, ZMQ_SNDMORE);

    if(success)
    {
        zmq::message_t zMessage(outputValue, outputValueSize, ZmqUtil::freeByteArray);
        try
        {
            success=socket.send(zMessage);
        }
        catch(std::exception& ex)
        {
            LERROR<<ex.what();
            success=false;
        }
    }
    else
    {
        delete[] outputValue;
    }

    return success;
}

bool ZmqUtil::sendServiceResponseFailure(zmq::socket_t &socket, const std::string &peerId, const std::string &callId, const std::string &failureMessage)
{
    rasnet::MessageType messageType;
    messageType.set_type(MessageType::SERVICE_RESPONSE);
    rasnet::ServiceCallStatus status;
    status.set_success(false);

    bool success = ZmqUtil::sendString(socket, peerId, ZMQ_SNDMORE);
    success =  success && ZmqUtil::rawSend(socket, messageType, ZMQ_SNDMORE);;
    success = success && ZmqUtil::sendString(socket,callId, ZMQ_SNDMORE);
    success =  success && ZmqUtil::rawSend(socket, status, ZMQ_SNDMORE);
    success = success && ZmqUtil::sendString(socket, failureMessage);

    return success;
}

void ZmqUtil::receiveCompositeMessage(zmq::socket_t &socket, std::vector<boost::shared_ptr<zmq::message_t> > &out_messages)
{
    out_messages.clear();

    int more = 0; //  Multipart detection
    size_t more_size = sizeof(more);
    do
    {
        zmq::message_t *message = new zmq::message_t();
        socket.recv(message);
        boost::shared_ptr<zmq::message_t> messagePtr(message);
        out_messages.push_back(messagePtr);

        socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);
    }
    while(more);
}

void ZmqUtil::receiveCompositeMessageFromPeer(zmq::socket_t &socket, std::string &out_peerId, std::vector<boost::shared_ptr<zmq::message_t> > &out_messages)
{
    out_messages.clear();

    out_peerId = ZmqUtil::receiveString(socket);

    int more = 0; //  Multipart detection
    size_t more_size = sizeof(more);
    socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);

    while(more)
    {
        more = 0;

        zmq::message_t *message = new zmq::message_t();
        socket.recv(message);
        boost::shared_ptr<zmq::message_t> messagePtr(message);
        out_messages.push_back(messagePtr);

        socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);
    }
}

std::string ZmqUtil::receiveString(zmq::socket_t &socket)
{
    zmq::message_t message;
    socket.recv(&message);

    return std::string(static_cast<char *>(message.data()), message.size());
}

void ZmqUtil::stringDump(zmq::socket_t &socket)
{
    std::cout << "----------------------------------------" << std::endl;

    while (1)
    {
        //  Process all parts of the message
        zmq::message_t message;
        socket.recv(&message);

        //  Dump the message as text or binary
        int size = message.size();
        std::string data(static_cast<char *>(message.data()), size);

        bool is_text = true;

        int char_nbr;
        unsigned char byte;
        for (char_nbr = 0; char_nbr < size; char_nbr++)
        {
            byte = data[char_nbr];
            if (byte < 32 || byte > 127)
                is_text = false;
        }
        std::cout << "[" << std::setfill('0') << std::setw(3) << size << "]";
        for (char_nbr = 0; char_nbr < size; char_nbr++)
        {
            if (is_text)
                std::cout << (char) data[char_nbr];
            else
                std::cout << std::setfill('0') << std::setw(2)
                          << std::hex << (unsigned int) data[char_nbr];
        }
        std::cout << std::endl;

        int more = 0;           //  Multipart detection
        size_t more_size = sizeof(more);
        socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);
        if (!more)
            break;              //  Last message part
    }
}

bool ZmqUtil::rawSend(zmq::socket_t &socket, const google::protobuf::Message &message, int flags)
{
    bool success = true;

    //Get the size of the envelope and allocate an array
    int messageSize = message.ByteSize();
    ::google::protobuf::uint8 *messageData =
        new ::google::protobuf::uint8[messageSize];
    message.SerializeWithCachedSizesToArray(messageData);
    //Use Zero-Copy to send the message

    zmq::message_t zMessage(messageData, messageSize, ZmqUtil::freeByteArray);
    try
    {
        success=socket.send(zMessage, flags);
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        success=false;
    }

    return success;
}

std::string ZmqUtil::addPrefixIfMissing(std::string str, std::string prefix)
{
    if (!boost::algorithm::starts_with(str, prefix))
    {
        str = prefix + str;
    }

    return str;
}

bool ZmqUtil::sendString(zmq::socket_t &socket, const std::string &string, int flags)
{
    zmq::message_t message(string.size());
    memcpy(message.data(), string.data(), string.size());

    return socket.send(message, flags);
}

}
