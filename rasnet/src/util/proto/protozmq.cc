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

#include "../../../../common/src/logging/easylogging++.hh"

#include "protozmq.hh"

namespace rasnet
{

bool ProtoZmq::zmqSend(zmq::socket_t &sock, google::protobuf::Message &mess)
{
    bool success=true;
    //Encapsulate the message
    base::BaseMessage envelope;
    envelope.set_type(mess.GetTypeName());
    envelope.set_data(mess.SerializeAsString());

    //Get the size of the envelope and allocate an array
    int envelopeSize = envelope.ByteSize();
    ::google::protobuf::uint8 *target =
        new ::google::protobuf::uint8[envelopeSize];
    envelope.SerializeWithCachedSizesToArray(target);
    //Use Zero-Copy to send the message
    zmq::message_t message(target, envelopeSize, freeArray);
    try
    {
        success=sock.send(message);
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        success=false;
    }

    return success;
}

bool ProtoZmq::zmqReceive(zmq::socket_t &socket, base::BaseMessage &message)
{
    bool success=true;
    zmq::message_t data;
    try
    {
        success= socket.recv(&data) && message.ParseFromArray(data.data(), data.size());
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        success=false;
    }

    return success;
}

bool ProtoZmq::zmqReceiveFromPeer(zmq::socket_t &socket,
                                  std::string &identity, base::BaseMessage &message)
{
    bool success=true;
    zmq::message_t content;
    int more = 0; //  Multipart detection
    size_t more_size = sizeof(more);

    try
    {
        identity = ProtoZmq::receiveString(socket);

        socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);
        //If the message doesn't have at least two parts, it is invalid.
        //The structure of the received messages on this socket is
        // | Sender ID | Serialized Protobuf message |
        if (more)
        {
            socket.recv(&content);
            socket.getsockopt(ZMQ_RCVMORE, &more, &more_size);
            if (more)
            {
                //The message has more than two parts
                //Dump it and return false
                ProtoZmq::stringDump(socket);
                success = false;
            }
            else
            {
                success = message.ParseFromArray(content.data(), content.size());
            }
        }
        else
        {
            success = false;
        }
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        success=false;
    }

    return success;
}


bool ProtoZmq::zmqRawSend(zmq::socket_t &sock, base::BaseMessage &mess)
{
    bool success=true;
    //Get the size of the envelope and allocate an array
    int messageSize = mess.ByteSize();
    ::google::protobuf::uint8 *target = new ::google::protobuf::uint8[messageSize];
    mess.SerializeWithCachedSizesToArray(target);

    //Use Zero-Copy to send the message
    zmq::message_t message((void *) target, messageSize, freeArray);

    try
    {
        success= sock.send(message);
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        success=false;
    }

    return success;
}

bool ProtoZmq::zmqSendToPeer(zmq::socket_t &sock,
                             google::protobuf::Message &mess, const std::string &peer_id)
{
    bool success=true;
    try
    {
        success = ProtoZmq::stringSendMore(sock, peer_id)  && ProtoZmq::zmqSend(sock, mess);
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        success=false;
    }

    return success;
}

bool ProtoZmq::zmqRawSendToPeer(zmq::socket_t &sock, base::BaseMessage &mess,
                                const std::string &peer_id)
{
    bool success=true;
    try
    {
        success = ProtoZmq::stringSendMore(sock, peer_id) && ProtoZmq::zmqRawSend(sock, mess);
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        success=false;
    }

    return success;
}

std::string ProtoZmq::getServiceMethodName(
    const ::google::protobuf::MethodDescriptor *method)
{
    return method->full_name();
}

void ProtoZmq::freeArray(void *data, void *hint)
{
    delete[] ((::google::protobuf::uint8 *) data);
}

void ProtoZmq::freeString(void *data, void *hint)
{
    delete[] ((char *) data);
}

std::string ProtoZmq::receiveString(zmq::socket_t &socket)
{
    zmq::message_t message;
    socket.recv(&message);

    return std::string(static_cast<char *>(message.data()), message.size());
}

bool ProtoZmq::stringSendMore(zmq::socket_t &socket, const std::string &string)
{
    zmq::message_t message(string.size());
    memcpy(message.data(), string.data(), string.size());

    return socket.send(message, ZMQ_SNDMORE);
}

void ProtoZmq::stringDump(zmq::socket_t &socket)
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
}
