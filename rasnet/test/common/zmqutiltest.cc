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

#include <boost/shared_ptr.hpp>

#include "../../../common/src/unittest/gtest.h"
#include "../../src/common/zmqutil.hh"
#include "../../src/common/util.hh"
#include "../messages/testing.pb.h"


namespace rasnet
{
namespace test
{
class ZmqUtilTest:public ::testing::Test
{
protected:
    ZmqUtilTest():server(ctx, ZMQ_ROUTER)
        ,client(ctx, ZMQ_DEALER)
    {
        this->address = "inproc://clientping";
        this->clientID = "clientID";
        this->client.setsockopt(ZMQ_IDENTITY, clientID.c_str(),
                                strlen(clientID.c_str()));

        this->server.bind(address.c_str());
        this->client.connect(address.c_str());

        this->dummyData = "dummy";
        this->dummy.set_data(dummyData);
    }

    virtual ~ZmqUtilTest()
    {
        this->client.disconnect(address.c_str());
        this->client.close();

   //     this->server.unbind(address.c_str());
        this->server.close();
    }

    zmq::context_t ctx;
    Dummy dummy;
    std::string dummyData;
    std::string address;
    std::string clientID;
    zmq::socket_t server;
    zmq::socket_t client;
};

using rasnet::ZmqUtil;
TEST_F(ZmqUtilTest, zmqMessageToString)
{
    std::string testStr = "test";

    std::string conversion = ZmqUtil::messageToString(*ZmqUtil::stringToZmq(testStr));
    ASSERT_EQ(testStr, conversion);
}

TEST_F(ZmqUtilTest, toInprocAddress)
{
    ASSERT_EQ(address, ZmqUtil::toInprocAddress(address));
    ASSERT_EQ(address, ZmqUtil::toInprocAddress("clientping"));
}

TEST_F(ZmqUtilTest, send)
{
    ASSERT_NO_THROW(ZmqUtil::send(client,dummy));

    zmq::message_t id;
    server.recv(&id);

    zmq::message_t base;
    server.recv(&base);
    BaseMessage envelope;
    envelope.ParseFromArray(base.data(), base.size());

    ASSERT_EQ(Util::getMessageType(dummy), envelope.type());
    ASSERT_EQ(dummy.SerializeAsString(), envelope.data());
}

TEST_F(ZmqUtilTest, sendToPeer)
{
    //The client must first contact the server
    //so that the server can later send a message back
    zmq::message_t setupMessage;
    client.send(setupMessage);
    zmq::message_t id;
    server.recv(&id);
    zmq::message_t recvSetupMessage;
    server.recv(&recvSetupMessage);

    ZmqUtil::sendToPeer(server, clientID, dummy);

    zmq::message_t recvBase;
    client.recv(&recvBase);

    BaseMessage envelope;
    envelope.ParseFromArray(recvBase.data(), recvBase.size());


    ASSERT_EQ(Util::getMessageType(dummy), envelope.type());
    ASSERT_EQ(dummy.SerializeAsString(), envelope.data());

}

TEST_F(ZmqUtilTest, receive)
{
    //The client must first contact the server
    //so that the server can later send a message back
    zmq::message_t setupMessage;
    client.send(setupMessage);
    zmq::message_t id;
    server.recv(&id);
    zmq::message_t recvSetupMessage;
    server.recv(&recvSetupMessage);

    //Test success when received message is valid
    ZmqUtil::sendToPeer(server, clientID, dummy);

    boost::shared_ptr<BaseMessage> envelope;
    ASSERT_TRUE(ZmqUtil::receive(client, envelope));

    ASSERT_EQ(Util::getMessageType(dummy), envelope->type());
    ASSERT_EQ(dummy.SerializeAsString(), envelope->data());

    //Test when message is invalid
    zmq::message_t IDBackup;
    zmq::message_t a,b,c,d;
    IDBackup.copy(&id);
    a.copy(&id);
    b.copy(&id);
    c.copy(&id);
    d.copy(&id);

    server.send(id, ZMQ_SNDMORE);
    server.send(a, ZMQ_SNDMORE);
    server.send(b);

    //Will fail because message was 2 parts + clientID
    ASSERT_FALSE(ZmqUtil::receive(client, envelope));

    server.send(IDBackup,ZMQ_SNDMORE);
    server.send(d);

    //Will fail because the message is unparsable
    ASSERT_FALSE(ZmqUtil::receive(client, envelope));
}

TEST_F(ZmqUtilTest, receiveFromPeer)
{
    //Test success case
    ZmqUtil::send(client, dummy);
    boost::shared_ptr<BaseMessage> envelope;
    std::string peerId;

    ASSERT_TRUE(ZmqUtil::receiveFromPeer(server, envelope, peerId));
    ASSERT_EQ(clientID, peerId);
    ASSERT_EQ(Util::getMessageType(dummy), envelope->type());
    ASSERT_EQ(dummy.SerializeAsString(), envelope->data());

    //Test multipartmessage
    zmq::message_t a,b;
    client.send(a, ZMQ_SNDMORE);
    client.send(b);
    ASSERT_FALSE(ZmqUtil::receiveFromPeer(server, envelope, peerId));

    //Fail because message is unparsable
    zmq::message_t c;
    client.send(c);
    ASSERT_FALSE(ZmqUtil::receiveFromPeer(server, envelope, peerId));
}

TEST_F(ZmqUtilTest, sendCompositeMessageByType)
{
    ASSERT_TRUE(ZmqUtil::sendCompositeMessage(client,MessageType::ALIVE_PONG));

    zmq::message_t peerId;
    server.recv(&peerId);
    zmq::message_t message;
    server.recv(&message);

    MessageType type;
    ASSERT_TRUE(type.ParseFromArray(message.data(), message.size()));
    ASSERT_EQ(MessageType::ALIVE_PONG, type.type());
}

TEST_F(ZmqUtilTest, sendCompositeMessage)
{
    ASSERT_TRUE(ZmqUtil::sendCompositeMessage(client,MessageType::SERVICE_REQUEST, dummy));

    zmq::message_t peerId;
    server.recv(&peerId);
    zmq::message_t zMessageType;
    server.recv(&zMessageType);
    zmq::message_t zMessage;
    server.recv(&zMessage);

    MessageType type;
    ASSERT_TRUE(type.ParseFromArray(zMessageType.data(), zMessageType.size()));
    ASSERT_EQ(MessageType::SERVICE_REQUEST, type.type());

    Dummy d;
    ASSERT_TRUE(d.ParseFromArray(zMessage.data(),zMessage.size()));
    ASSERT_EQ(dummy.DebugString(),d.DebugString());
}

TEST_F(ZmqUtilTest, sendCompositeMessageToPeer)
{
    //Setup the connection
    ZmqUtil::send(client, dummy);
    boost::shared_ptr<BaseMessage> message;
    std::string peerId;
    ZmqUtil::receiveFromPeer(server, message, peerId);

    ASSERT_TRUE(ZmqUtil::sendCompositeMessageToPeer(server, clientID, MessageType::SERVICE_REQUEST, dummy));
    zmq::message_t zMessageType;
    client.recv(&zMessageType);
    zmq::message_t zMessage;
    client.recv(&zMessage);

    MessageType type;
    ASSERT_TRUE(type.ParseFromArray(zMessageType.data(), zMessageType.size()));
    ASSERT_EQ(MessageType::SERVICE_REQUEST, type.type());

    Dummy d;
    ASSERT_TRUE(d.ParseFromArray(zMessage.data(),zMessage.size()));
    ASSERT_EQ(dummy.DebugString(),d.DebugString());
}

TEST_F(ZmqUtilTest, receiveCompositeMessage)
{
    //Setup the connection
    ZmqUtil::send(client, dummy);

    std::vector<boost::shared_ptr<zmq::message_t> > messages;
    ZmqUtil::receiveCompositeMessage(server, messages);

    ASSERT_EQ(2, messages.size());
    std::string peerId = ZmqUtil::messageToString(*messages[0]);
    ASSERT_EQ(clientID, peerId);

    BaseMessage envelope;
    envelope.ParseFromArray(messages[1]->data(), messages[1]->size());

    ASSERT_EQ(Util::getMessageType(dummy), envelope.type());
    ASSERT_EQ(dummy.SerializeAsString(), envelope.data());
}

TEST_F(ZmqUtilTest, receiveCompositeMessageFromPeer)
{
    //Setup the connection
    ZmqUtil::send(client, dummy);

    std::string peerId;
    std::vector<boost::shared_ptr<zmq::message_t> > messages;
    ZmqUtil::receiveCompositeMessageFromPeer(server, peerId, messages);

    ASSERT_EQ(1, messages.size());

    BaseMessage envelope;
    envelope.ParseFromArray(messages[0]->data(), messages[0]->size());

    ASSERT_EQ(Util::getMessageType(dummy), envelope.type());
    ASSERT_EQ(dummy.SerializeAsString(), envelope.data());
}

TEST_F(ZmqUtilTest, sendServiceRequest)
{
    std::string callId ="callId";
    std::string methodName = "methodName";

    ASSERT_TRUE(ZmqUtil::sendServiceRequest(client, callId, methodName, &dummy));

    std::string peerId;
    std::vector<boost::shared_ptr<zmq::message_t> > messages;
    ZmqUtil::receiveCompositeMessageFromPeer(server, peerId, messages);

    ASSERT_EQ(clientID, peerId);

    MessageType type;
    type.ParseFromArray(messages[0]->data(), messages[0]->size());
    ASSERT_EQ(type.type(), MessageType::SERVICE_REQUEST);

    ASSERT_EQ(callId, ZmqUtil::messageToString(*messages[1]));
    ASSERT_EQ(methodName, ZmqUtil::messageToString(*messages[2]));

    Dummy d;
    d.ParseFromArray(messages[3]->data(), messages[3]->size());
    ASSERT_EQ(d.DebugString(), dummy.DebugString());

}
}
}
