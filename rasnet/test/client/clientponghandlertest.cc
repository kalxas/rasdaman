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
#include <boost/pointer_cast.hpp>

#include "../../../common/src/unittest/gtest.h"
#include "../src/client/clientponghandler.hh"
#include "../mock/peerstatusmock.hh"
#include "../testutilities.hh"

namespace rasnet
{
namespace test
{
class ClientPongHandlerTest: public ::testing::Test
{
    virtual void SetUp()
    {
        peerStatusMock.reset(new PeerStatusMock());
        pongHandler.reset(new rasnet::ClientPongHandler(peerStatusMock));
    }

protected:
    boost::shared_ptr<rasnet::PeerStatus> peerStatusMock;
    boost::shared_ptr<rasnet::ClientPongHandler> pongHandler;
};

TEST_F(ClientPongHandlerTest, canHandle)
{
    //Create a good message and test if everything goes according to plan
    std::vector<boost::shared_ptr<zmq::message_t> > goodMessage;
    boost::shared_ptr<zmq::message_t>alivePongType(TestUtilities::typeToZmq(rasnet::MessageType::ALIVE_PONG));
    goodMessage.push_back(alivePongType);

    ASSERT_TRUE(pongHandler->canHandle(goodMessage));
}

TEST_F(ClientPongHandlerTest, canHandleFail)
{
    //Create a good message and test if everything goes according to plan
    std::vector<boost::shared_ptr<zmq::message_t> >  badMessage;
    boost::shared_ptr<zmq::message_t>alivePingType(TestUtilities::typeToZmq(rasnet::MessageType::ALIVE_PING));
    badMessage.push_back(alivePingType);

    ASSERT_FALSE(pongHandler->canHandle(badMessage));

    //Try with a message of length different than 1
    badMessage.clear();
    badMessage.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::typeToZmq(rasnet::MessageType::ALIVE_PONG)));
    badMessage.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::typeToZmq(rasnet::MessageType::ALIVE_PING)));

    ASSERT_FALSE(pongHandler->canHandle(badMessage));
}

TEST_F(ClientPongHandlerTest, handleFail)
{
    std::vector<boost::shared_ptr<zmq::message_t> >  badMessage;
    boost::shared_ptr<zmq::message_t>alivePingType(TestUtilities::typeToZmq(rasnet::MessageType::ALIVE_PING));
    badMessage.push_back(alivePingType);

    ASSERT_ANY_THROW(pongHandler->handle(badMessage));
}

TEST_F(ClientPongHandlerTest, handle)
{
    std::vector<boost::shared_ptr<zmq::message_t> > goodMessage;
    boost::shared_ptr<zmq::message_t>alivePongType(TestUtilities::typeToZmq(rasnet::MessageType::ALIVE_PONG));
    goodMessage.push_back(alivePongType);

    //The server's status will be reset if a PONG message arrives
    PeerStatusMock& peerStatusMock = *boost::dynamic_pointer_cast<PeerStatusMock>(this->peerStatusMock);
    EXPECT_CALL(peerStatusMock , reset());

    ASSERT_NO_THROW(pongHandler->handle(goodMessage));
}

}
}
