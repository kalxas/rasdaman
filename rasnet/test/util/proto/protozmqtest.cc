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
#include "../../../../common/src/unittest/gtest.h"
#include "../../../../common/src/zeromq/zmq.hh"
#include "../../../../common/src/logging/easylogging++.hh"
#include "../../../src/messages/communication.pb.h"
#include "../../../src/messages/base.pb.h"
#include "../../../src/util/proto/protozmq.hh"

using zmq::context_t;
using zmq::socket_t;
using rasnet::AlivePing;
using rasnet::AlivePong;
using base::BaseMessage;
using std::string;
using rasnet::ProtoZmq;

TEST(ProtoUtils, zmqSend)
{
    AlivePing ping;
    BaseMessage base;
    zmq::message_t msg;
    zmq::context_t context;

    const char* addr = "inproc://proto_test";

    zmq::socket_t peer1(context, ZMQ_PAIR);
    zmq::socket_t peer2(context, ZMQ_PAIR);

    peer1.bind(addr);
    peer2.connect(addr);

    ASSERT_TRUE(ProtoZmq::zmqSend(peer1, ping));

    peer2.recv(&msg);

    ASSERT_TRUE(base.ParseFromArray(msg.data(), msg.size()));
    ASSERT_EQ(base.type(), AlivePing::default_instance().GetTypeName());
}

TEST(ProtoUtils, zmqSendBase)
{
    AlivePing ping;
    BaseMessage base_send;
    BaseMessage base_recv;
    zmq::message_t msg;
    zmq::context_t context;
    const char* addr = "inproc://proto_test";

    zmq::socket_t peer1(context, ZMQ_PAIR);
    zmq::socket_t peer2(context, ZMQ_PAIR);

    peer1.bind(addr);
    peer2.connect(addr);

    base_send.set_type(ping.GetTypeName());
    base_send.set_data(ping.SerializeAsString());

    ASSERT_TRUE(ProtoZmq::zmqRawSend(peer1, base_send));

    peer2.recv(&msg);
    ASSERT_TRUE(base_recv.ParseFromArray(msg.data(), msg.size()));
    ASSERT_EQ(base_recv.type(), AlivePing::default_instance().GetTypeName());
}

TEST(ProtoUtils, zmqSendToPeer)
{
    const char* addr = "inproc://proto_test";
    string identity = "my_id";
    AlivePing ping;
    AlivePong pong;
    BaseMessage base_recv;
    zmq::message_t msg;
    zmq::context_t context;

    zmq::socket_t client(context, ZMQ_DEALER);
    zmq::socket_t server(context, ZMQ_ROUTER);

    client.setsockopt(ZMQ_IDENTITY, identity.c_str(), strlen(identity.c_str()));
    server.bind(addr);
    client.connect(addr);

    //Send from client to server
    ASSERT_TRUE(ProtoZmq::zmqSend(client, ping));

    //Send from server to client
    ASSERT_TRUE(ProtoZmq::zmqSendToPeer(server, pong, identity));

    //Receive at client
    client.recv(&msg);

    ASSERT_TRUE(base_recv.ParseFromArray(msg.data(), msg.size()));
    ASSERT_EQ(base_recv.type(), AlivePong::default_instance().GetTypeName());
}


TEST(ProtoUtils, zmqSendBaseToPeer)
{
    const char* addr = "inproc://proto_test";
    string identity = "my_id";
    AlivePing ping;
    AlivePong pong;
    BaseMessage base_recv;
    BaseMessage base_send;
    zmq::message_t msg;
    zmq::context_t context;

    zmq::socket_t client(context, ZMQ_DEALER);
    zmq::socket_t server(context, ZMQ_ROUTER);

    client.setsockopt(ZMQ_IDENTITY, identity.c_str(), strlen(identity.c_str()));

    server.bind(addr);
    client.connect(addr);

    //Send message from client to server
    ASSERT_TRUE(ProtoZmq::zmqSend(client, ping));
    base_send.set_type(pong.GetTypeName());
    base_send.set_data(pong.SerializeAsString());

    //Send message from server to client
    ASSERT_TRUE(ProtoZmq::zmqRawSendToPeer(server, base_send, identity));

    //Receive message at client
    client.recv(&msg);
    ASSERT_TRUE(base_recv.ParseFromArray(msg.data(), msg.size()));
    ASSERT_EQ(base_recv.type(), AlivePong::default_instance().GetTypeName());
}

TEST(ProtoUtils, zmqReceive)
{
    const char* addr = "inproc://proto_test";
    AlivePing ping;
    BaseMessage base;
    zmq::message_t msg;
    zmq::context_t context;

    zmq::socket_t peer1(context, ZMQ_PAIR);
    zmq::socket_t peer2(context, ZMQ_PAIR);

    peer1.bind(addr);
    peer2.connect(addr);
    //THis is already tested above
    ASSERT_TRUE(ProtoZmq::zmqSend(peer1, ping));

    ASSERT_TRUE(ProtoZmq::zmqReceive(peer2, base));

    ASSERT_EQ(base.type(), AlivePing::default_instance().GetTypeName());
}


TEST(ProtoUtils, zmqReceiveFromPeer)
{
    zmq::context_t context;
    AlivePing ping;
    string id;
    BaseMessage base_recv;
    //Silence google protobuf logs for this test
    google::protobuf::LogSilencer logSilencer;
    const char* addr = "inproc://proto_test";
    string identity = "my_id";

    zmq::socket_t client(context, ZMQ_DEALER);
    zmq::socket_t server(context, ZMQ_ROUTER);

    client.setsockopt(ZMQ_IDENTITY, identity.c_str(), strlen(identity.c_str()));
    server.bind(addr);
    client.connect(addr);

    ASSERT_TRUE(ProtoZmq::zmqSend(client, ping));

    ASSERT_TRUE(ProtoZmq::zmqReceiveFromPeer(server, id, base_recv));

    ASSERT_EQ(identity, id);
    ASSERT_EQ(base_recv.type(), ping.GetTypeName());

    std::string str="dasda";
    zmq::message_t message(str.size());

    memcpy(message.data(), str.data(), str.size());
    client.send(message);

    ASSERT_FALSE(ProtoZmq::zmqReceiveFromPeer(server, id, base_recv));
}
