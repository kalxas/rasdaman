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

#include <boost/bind.hpp>

#include "../../../common/src/unittest/gtest.h"
#include "../../src/common/zmqutil.hh"
#include "../../src/client/channel.hh"
#include "../../src/client/clientcontroller.hh"
#include "../messages/testing.pb.h"

namespace rasnet
{
namespace test
{
using google::protobuf::Closure;

class ChannelTest:public ::testing::Test
{

public:
    void replyToConnect()
    {
        std::string peerId;
        std::vector<boost::shared_ptr<zmq::message_t> > message;
        ZmqUtil::receiveCompositeMessageFromPeer(server, peerId, message);

        MessageType type;
        type.ParseFromArray(message[0]->data(), message[0]->size());

        ConnectRequest request;
        request.ParseFromArray(message[1]->data(), message[1]->size());

        ConnectReply reply;
        reply.set_lifetime(request.lifetime());
        reply.set_retries(request.retries());

        ZmqUtil::sendCompositeMessageToPeer(server, peerId, MessageType::CONNECT_REPLY, reply);
    }

    void replyToServiceCall()
    {
        std::string peerId;
        std::vector<boost::shared_ptr<zmq::message_t> > message;
        ZmqUtil::receiveCompositeMessageFromPeer(server, peerId, message);

        //| MessageType = SERVICE_REQUEST | Call ID | Method Name | Serialized Input Data|
        ASSERT_EQ(4, message.size());
        MessageType type;
        type.ParseFromArray(message[0]->data(), message[0]->size());
        ASSERT_EQ(MessageType::SERVICE_REQUEST, type.type());
        std::string callId=ZmqUtil::messageToString(*message[1]);;

        DummyRequest request;
        request.ParseFromArray(message[3]->data(), message[3]->size());

        DummyReply reply;
        reply.set_data(request.data());

        int messageSize = reply.ByteSize();
        ::google::protobuf::uint8 *messageData =
            new ::google::protobuf::uint8[messageSize];
        reply.SerializeWithCachedSizesToArray(messageData);

        ZmqUtil::sendServiceResponseSuccess(server, peerId, callId, messageData, messageSize);
    }

    void replyWithError()
    {
        std::string peerId;
        std::vector<boost::shared_ptr<zmq::message_t> > message;
        ZmqUtil::receiveCompositeMessageFromPeer(server, peerId, message);

        //| MessageType = SERVICE_REQUEST | Call ID | Method Name | Serialized Input Data|
        ASSERT_EQ(4, message.size());
        MessageType type;
        type.ParseFromArray(message[0]->data(), message[0]->size());
        ASSERT_EQ(MessageType::SERVICE_REQUEST, type.type());
        std::string callId=ZmqUtil::messageToString(*message[1]);;

        DummyRequest request;
        request.ParseFromArray(message[3]->data(), message[3]->size());

        ZmqUtil::sendServiceResponseFailure(server, peerId, callId, request.data());
    }

protected:
    ChannelTest():server(ctx, ZMQ_ROUTER)
    {
        this->serverAddress = "tcp://localhost:10000";
        this->serverBindingPoint = "tcp://*:10000";
        this->server.bind(this->serverBindingPoint.c_str());
    }

    virtual ~ChannelTest()
    {
        this->server.unbind(this->serverBindingPoint.c_str());
        this->server.close();
    }

    zmq::context_t ctx;
    zmq::socket_t server;
    std::string serverAddress;
    std::string serverBindingPoint;
};

TEST_F(ChannelTest, successFullConnect)
{
    Channel* channel=NULL;
    ChannelConfig config;

    boost::thread thr(&ChannelTest::replyToConnect, this);

    ASSERT_NO_THROW(channel = new Channel(serverAddress, config));

    thr.join();
    delete channel;
}

//The constructor will fail because the server is not responding
TEST_F(ChannelTest, failConnect)
{
    Channel* channel=NULL;
    ChannelConfig config;
    config.setConnectionTimeout(1);

    //Because there is no server responding, the initialization will fail.
    ASSERT_ANY_THROW(channel = new Channel(serverAddress, config));
}

//Will fail because the server address is invalid
TEST_F(ChannelTest, failConnect2)
{
    Channel* channel=NULL;
    ChannelConfig config;
    config.setConnectionTimeout(1);

    //Because there is no server responding, the initialization will fail.
    ASSERT_ANY_THROW(channel = new Channel("serverAddress", config));
}

//Will test that the response form the server is processed well
TEST_F(ChannelTest, validReplySuccess)
{
    ChannelConfig config;

    //Initialize channel
    boost::thread thr(&ChannelTest::replyToConnect, this);
    Channel channel(this->serverAddress, config);
    thr.join();

    DummyRequest request;
    request.set_data("hello");

    DummyReply reply;
    ClientController controller;
    DummyService_Stub stub(&channel);
    Closure* doNothingClosure = google::protobuf::NewPermanentCallback(&google::protobuf::DoNothing);

    boost::thread serverThr(&ChannelTest::replyToServiceCall, this);
    channel.CallMethod(stub.GetDescriptor()->method(0),&controller, &request, &reply, doNothingClosure);
    serverThr.join();

    ASSERT_FALSE(controller.Failed());
    ASSERT_EQ(request.data(), reply.data());

    delete doNothingClosure;
}

TEST_F(ChannelTest, validReplyFailure)
{
    ChannelConfig config;

    //Initialize channel
    boost::thread thr(&ChannelTest::replyToConnect, this);
    Channel channel(this->serverAddress, config);
    thr.join();

    DummyRequest request;
    request.set_data("hello");

    DummyReply reply;
    ClientController controller;
    DummyService_Stub stub(&channel);
    Closure* doNothingClosure = google::protobuf::NewPermanentCallback(&google::protobuf::DoNothing);

    boost::thread serverThr(&ChannelTest::replyWithError, this);
    channel.CallMethod(stub.GetDescriptor()->method(0),&controller, &request, &reply, doNothingClosure);
    serverThr.join();

    ASSERT_TRUE(controller.Failed());
    ASSERT_EQ(request.data(), controller.ErrorText());

    delete doNothingClosure;
}
}
}
