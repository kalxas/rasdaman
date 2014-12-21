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

#include <string>
#include "../../../../common/src/unittest/gtest.h"
#include "../../../../common/src/logging/easylogging++.hh"
#include "../../../../common/src/zeromq/zmq.hh"
#include "../../../src/sockets/server/serversocket.hh"
#include "../../../src/sockets/client/clientsocket.hh"
#include "../../../src/util/proto/protozmq.hh"

#include "../../../src/messages/communication.pb.h"
#include "../../../src/messages/test_mess.pb.h"


using namespace std;
using namespace zmq;
using namespace rasnet;
using base::BaseMessage;
using rnp_test::DummyReply;
using rnp_test::DummyRequest;

class ServerSocketTest: public ::testing::Test
{
protected:
    virtual void SetUp()
    {
        hostAddr = "tcp://*:7001";
        serverAddr = "tcp://localhost:7001";
        clientId = "server_socket_test";
        clientPeriod =10;
        clientLives=1;

        client = new zmq::socket_t(client_ctx, ZMQ_DEALER);
        client->setsockopt(ZMQ_IDENTITY, clientId.c_str(),
                           strlen(clientId.c_str()));
        client->connect(serverAddr.c_str());

    }

    void connectClient()
    {
        ExternalConnectRequest req;
        req.set_period(this->clientPeriod);
        req.set_retries(this->clientLives);
        ProtoZmq::zmqSend(*client, req);
    }

    void sendMessageFromClient()
    {
        DummyRequest req;
        ProtoZmq::zmqSend(*client, req);
    }

    virtual void TearDown()
    {
        delete client;
    }
    boost::int32_t clientPeriod;
    boost::int32_t clientLives;
    string hostAddr;
    string serverAddr;
    string clientId;
    zmq::socket_t* client;
    zmq::context_t client_ctx;
    zmq::context_t server_ctx;

};

TEST_F(ServerSocketTest, Constructor)
{
    ServerSocket *socket;
    ServerSocketConfig config;

    ASSERT_NO_THROW(socket = new ServerSocket(this->hostAddr, config));
    ASSERT_NO_THROW(delete socket);
}

TEST_F(ServerSocketTest, pollOut)
{
    ServerSocketConfig config;
    ServerSocket server(this->hostAddr, config);

    ASSERT_TRUE(server.pollOut(1000));

    ASSERT_TRUE(server.pollOut(0));

    ASSERT_TRUE(server.pollOut(-1));
}

TEST_F(ServerSocketTest, pollIn)
{
    ServerSocketConfig config;
    ServerSocket server(this->hostAddr, config);

    ASSERT_FALSE(server.pollIn(0));

    connectClient();
    sendMessageFromClient();

    ASSERT_TRUE(server.pollIn(-1));
}

TEST_F(ServerSocketTest, receive)
{
    string peerId;
    BaseMessage envelope;
    ServerSocketConfig config;
    ServerSocket server(this->hostAddr, config);

    connectClient();
    sendMessageFromClient();

    server.receive(peerId, envelope);

    ASSERT_EQ(this->clientId, peerId);
    ASSERT_EQ(DummyRequest::default_instance().GetTypeName(), envelope.type());
}


TEST_F(ServerSocketTest, send)
{
    string peerId;
    DummyReply repl;
    BaseMessage envelope;
    ServerSocketConfig config;
    ServerSocket server(this->hostAddr, config);

    connectClient();
    sendMessageFromClient();
    server.receive(peerId, envelope);
    server.send(peerId, repl);

    envelope.Clear();

    while (repl.GetTypeName() != envelope.type())
    {
        ProtoZmq::zmqReceive(*client, envelope);
    }
    ASSERT_EQ(repl.GetTypeName(), envelope.type());
}

TEST_F(ServerSocketTest, isPeerAlive)
{
    string peerId;
    DummyReply repl;
    BaseMessage envelope;
    ServerSocketConfig config;
    ServerSocket server(this->hostAddr, config);

    connectClient();
    sendMessageFromClient();
    server.receive(peerId, envelope);

    ASSERT_TRUE(server.isPeerAlive(peerId));

    //Sleeping enough for the client to be declared dead.
    usleep(this->clientPeriod*this->clientLives*1000*2);

    ASSERT_FALSE(server.isPeerAlive(peerId));

}
