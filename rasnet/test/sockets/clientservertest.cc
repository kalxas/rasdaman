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

#include <boost/timer/timer.hpp>
#include <boost/thread.hpp>
#include "../../../common/src/unittest/gtest.h"
#include "../../../common/src/logging/easylogging++.hh"
#include "../../../common/src/zeromq/zmq.hh"

#include "../../src/sockets/client/clientsocket.hh"
#include "../../src/sockets/server/serversocket.hh"
#include "../../src/messages/test_mess.pb.h"
#include "../../src/messages/base.pb.h"

using namespace rasnet;
using namespace rnp_test;
using namespace base;

class ClientServerTest: public ::testing::Test
{
public:

    void sendServerMessage(int messageNo, std::string peerId)
    {
        LINFO<<"Started sending messages in server thread.";
        DummyRequest sentRequest;
        for (int i = 0; i < messageNo; i++)
        {
            sentRequest.set_requestno(i);
            server->send(peerId, sentRequest);
        }
        LINFO<<"Finished sending messages in server thread.";
    }

    void receiveClientMessage(int messageNo)
    {
        LINFO<<"Started receiving messages in client thread.";
        BaseMessage clientEnv;
        for (int i = 0; i < messageNo; i++)
        {
            client->receive(clientEnv);
        }
        LINFO<<"Finished receiving messages in client thread.";
    }
protected:
    void SetUp()
    {
        ClientSocketConfig clientConfig;
        ServerSocketConfig serverConfig;
        server = new ServerSocket("tcp://*:7001", serverConfig);
        client = new ClientSocket("tcp://localhost:7001", clientConfig);
    }

    void TearDown()
    {
        delete server;
        delete client;
    }



    zmq::context_t server_ctx;
    zmq::context_t client_ctx;
    ClientSocket* client;
    ServerSocket* server;
};


/**
 * Test if the peers detect if each one is alive
 */
TEST_F(ClientServerTest, clientConnect)
{
    ASSERT_TRUE(client->isPeerAlive());
    ASSERT_FALSE(client->pollIn(0));
    ASSERT_TRUE(client->pollOut(0));
}

/**
 *
 */
TEST_F(ClientServerTest, serverConnect)
{
    ASSERT_FALSE(server->pollIn(0));
    ASSERT_TRUE(server->pollOut(0));
}

TEST_F(ClientServerTest, sendReceive)
{
    DummyRequest request;
    DummyReply reply;
    BaseMessage client_env;
    BaseMessage server_env;
    std::string peer_id;

    //Send a message from the client to the server
    client->send(request);

    ASSERT_TRUE(server->pollIn(-1));
    server->receive(peer_id, server_env);
    //Test that the correct message was received
    ASSERT_EQ(request.GetTypeName(), server_env.type());

    //Checking the isAlive functionality
    ASSERT_TRUE(server->isPeerAlive(peer_id));

    //Send a message from the server to the client
    server->send(peer_id, reply);
    ASSERT_TRUE(client->pollIn(-1));
    client->receive(client_env);
    ASSERT_EQ(reply.GetTypeName(), client_env.type());
}

TEST_F(ClientServerTest, performanceTest)
{

    std::string peer_id;
    BaseMessage server_env;
    BaseMessage client_env;
    DummyRequest sentRequest;
    DummyRequest receivedRequest;
    DummyReply reply;

    int mess_no = 1000;
    short timerPrecision = 5;
    boost::timer::cpu_timer timer;

    PINFO<<"Starting client-server sockets performance test";

    PINFO<<"Client started sending messages";
    timer.start();

    for (int i = 0; i < mess_no; i++)
    {
        sentRequest.set_requestno(i);
        client->send(sentRequest);
    }

    timer.stop();
    PINFO<<"The client sent "<<mess_no<<" messages in :"<<timer.format(timerPrecision)<<" seconds";

    timer.start();
    PINFO<<"Server started receiving messages";

    for (int i = 0; i < mess_no; i++)
    {
        server->receive(peer_id, server_env);
    }

    timer.stop();
    PINFO<<"The server received "<< mess_no<<" messages in :"<<timer.format(timerPrecision)<<" seconds";


    PINFO<<"Started server-client serial communication.";
    timer.start();
    for (int i = 0; i < mess_no; i++)
    {
        sentRequest.set_requestno(i);
        server->send(peer_id, sentRequest);
        client->receive(client_env);
    }
    timer.stop();
    PINFO<<"Finished server-client serial communication."<<timer.format(timerPrecision)<<" seconds";

/*    PINFO<<"Started server-client parallel communication";
    timer.start();

    boost::thread_group group;

    boost::thread* srv=new   boost::thread(&ClientServerTest::sendServerMessage, this, mess_no, peer_id);
    group.add_thread(srv);

    boost::thread* cl=new boost::thread(&ClientServerTest::receiveClientMessage, this, mess_no);
    group.add_thread(cl);

    group.join_all();

    timer.stop();
    PINFO<<"Started server-client parallel communication"<<timer.format(timerPrecision)<<" seconds";*/
    PINFO<<"Finished client-server sockets performance test.";
}
