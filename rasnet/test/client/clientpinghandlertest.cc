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

#include "../../../common/src/unittest/gtest.h"
#include "../src/client/clientpinghandler.hh"
#include "../src/common/zmqutil.hh"
#include "../testutilities.hh"

namespace rasnet
{
namespace test
{
class ClientPingHandlerTest:public ::testing::Test
{
protected:
    ClientPingHandlerTest():server(ctx, ZMQ_ROUTER)
        ,client(ctx, ZMQ_DEALER)
    {
        this->address = "inproc://clientping";
        this->server.bind(address.c_str());
        this->client.connect(address.c_str());

        handler.reset(new ClientPingHandler(client));
    }

    virtual ~ClientPingHandlerTest()
    {
        this->client.disconnect(address.c_str());
        this->client.close();

    //    this->server.unbind(address.c_str());
        this->server.close();
    }

    zmq::context_t ctx;
    std::string address;
    zmq::socket_t server;
    zmq::socket_t client;
    boost::shared_ptr<ClientPingHandler> handler;
};

TEST_F(ClientPingHandlerTest, canHandle )
{
    //Create a good message and test if everything goes according to plan
    std::vector<boost::shared_ptr<zmq::message_t> > goodMessage;
    boost::shared_ptr<zmq::message_t>alivePingType(TestUtilities::typeToZmq(rasnet::MessageType::ALIVE_PING));

    goodMessage.push_back(alivePingType);

    ASSERT_TRUE(handler->canHandle(goodMessage));
}

TEST_F(ClientPingHandlerTest, canHandleFail )
{
    std::vector<boost::shared_ptr<zmq::message_t> > badMessage;
    boost::shared_ptr<zmq::message_t>alivePongType(TestUtilities::typeToZmq(rasnet::MessageType::ALIVE_PONG));

    badMessage.push_back(alivePongType);

    ASSERT_FALSE(handler->canHandle(badMessage));
}

TEST_F(ClientPingHandlerTest, handleFail)
{
    std::vector<boost::shared_ptr<zmq::message_t> > badMessage;
    boost::shared_ptr<zmq::message_t>alivePongType(TestUtilities::typeToZmq(rasnet::MessageType::ALIVE_PONG));
    badMessage.push_back(alivePongType);

    ASSERT_ANY_THROW(handler->handle(badMessage));
}

TEST_F(ClientPingHandlerTest, handle )
{
    std::vector<boost::shared_ptr<zmq::message_t> > goodMessage;
    boost::shared_ptr<zmq::message_t>alivePingType(TestUtilities::typeToZmq(rasnet::MessageType::ALIVE_PING));

    goodMessage.push_back(alivePingType);

    //The message must be handled without any exception being thrown
    ASSERT_NO_THROW(handler->handle(goodMessage));

    //Receive the message at the other end and check for validity
    std::string peerId;
    std::vector<boost::shared_ptr<zmq::message_t> > messages;
    rasnet::ZmqUtil::receiveCompositeMessageFromPeer(server, peerId, messages);

    //The message must have only one element, the type, being ALIVE_PONG
    ASSERT_EQ(1, messages.size());

    rasnet::MessageType messageType;
    messageType.ParseFromArray(messages[0]->data(), messages[0]->size());
    ASSERT_EQ(rasnet::MessageType::ALIVE_PONG, messageType.type());
}

}
}
