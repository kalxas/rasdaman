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
#include <boost/thread.hpp>
#include <boost/timer/timer.hpp>

#include "../../../../common/src/unittest/gtest.h"
#include "../../../../common/src/logging/easylogging++.hh"
#include "../../../../common/src/zeromq/zmq.hh"

#include "../../../src/messages/communication.pb.h"
#include "../../../src/messages/test_mess.pb.h"
#include "../../../src/util/proto/protozmq.hh"

#include "../../../src/sockets/client/clientsocket.hh"
#include "../../../src/sockets/client/clientsocketconfig.hh"

using namespace rasnet;
using namespace std;
using namespace zmq;
using namespace boost;

using base::BaseMessage;
using rnp_test::DummyReply;
using rnp_test::DummyRequest;

class ClientSocketTest: public ::testing::Test
{
public:
    boost::int32_t serverPeriod;
    boost::int32_t serverLives;

    void replyToConnect()
    {
        BaseMessage connectReq;
        std::string id;
        ExternalConnectReply reply;
        reply.set_period(this->serverPeriod);
        reply.set_retries(this->serverLives);

        ProtoZmq::zmqReceiveFromPeer(*server, id, connectReq);
        ProtoZmq::zmqSendToPeer(*server, reply, id);
    }
    void sendMessToClient()
    {
        string id;
        BaseMessage mess;
        DummyReply repl;
        ProtoZmq::zmqReceiveFromPeer(*server, id, mess);
        ProtoZmq::zmqSendToPeer(*server, repl, id);
    }

protected:
    virtual void SetUp()
    {
        this->serverPeriod=100;
        this->serverLives=3;
        this->server_addr = "tcp://*:7001";
        this->client_addr = "tcp://localhost:7001";
        this->server = new socket_t(ctx, ZMQ_ROUTER);
        this->server->bind(this->server_addr.c_str());
    }


    virtual void TearDown()
    {
        this->server->disconnect(server_addr.c_str());
        delete server;
    }

    context_t ctx;
    string server_addr;
    string client_addr;
    socket_t* server;
};

TEST_F(ClientSocketTest, Constructor)
{
    ClientSocket* socketFail;
    ClientSocketConfig config;
    ASSERT_ANY_THROW(socketFail = new ClientSocket("inproc://test",config));
    (void) socketFail;

    ClientSocket* socketsuccess;
    thread t(&ClientSocketTest::replyToConnect, this);

    boost::timer::auto_cpu_timer timer;
    ASSERT_NO_THROW(socketsuccess = new ClientSocket(this->client_addr,config));
    t.join();

    ASSERT_NO_THROW(delete socketsuccess);
}

TEST_F(ClientSocketTest, receive)
{
    DummyRequest req;
    string id;
    BaseMessage mess;
    ClientSocketConfig config;
    const char* replyType=DummyReply::default_instance().GetTypeName().c_str();

    thread t(&ClientSocketTest::replyToConnect, this);
    ClientSocket client(this->client_addr, config);
    t.join();

    sendMessToClient();
    ASSERT_NO_THROW(client.receive(mess));

    const char* type=mess.type().c_str();

    ASSERT_STREQ(replyType,type);
}

TEST_F(ClientSocketTest, send)
{
    DummyRequest req;
    BaseMessage mess;
    string id;
    ClientSocketConfig config;

    thread t(&ClientSocketTest::replyToConnect, this);
    ClientSocket client(this->client_addr,config);
    t.join();

    ASSERT_NO_THROW(client.send(req));
    ASSERT_TRUE(ProtoZmq::zmqReceiveFromPeer(*(this->server), id, mess));

}

TEST_F(ClientSocketTest, pollOut)
{
    ClientSocketConfig config;
    thread t(&ClientSocketTest::replyToConnect, this);
    ClientSocket client(this->client_addr, config);
    t.join();

    ASSERT_TRUE(client.pollOut(0));
}


TEST_F(ClientSocketTest, pollIn)
{
    DummyRequest req;
    ClientSocketConfig config;
    thread t(&ClientSocketTest::replyToConnect, this);
    ClientSocket client(this->client_addr, config);
    t.join();

    ASSERT_NO_THROW(client.send(req));
    sendMessToClient();

    ASSERT_FALSE(client.pollIn(0));

    sendMessToClient();
    ASSERT_TRUE(client.pollIn(-1));
}


TEST_F(ClientSocketTest, isPeerAlive)
{
    ClientSocketConfig config;
    thread t(&ClientSocketTest::replyToConnect, this);
    ClientSocket client(this->client_addr, config);
    t.join();

    replyToConnect();
    ASSERT_TRUE(client.isPeerAlive());
    usleep((this->serverLives+1)*this->serverPeriod*1000);
    ASSERT_FALSE(client.isPeerAlive());

}
