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
#include "../src/common/zmqutil.hh"
#include "../src/client/serviceresponsehandler.hh"
#include "../src/messages/internal.pb.h"

#include "../testutilities.hh"

namespace rasnet
{
namespace test
{
using std::map;
using std::string;
using std::make_pair;
using std::pair;
using boost::shared_ptr;

class ServiceResponseHandlerTest:public ::testing::Test
{

protected:
    ServiceResponseHandlerTest():
        server(ctx, ZMQ_ROUTER), client(ctx,ZMQ_DEALER)
    {
        this->address = "inproc://clientping";
        this->peerId = "peerId";
        this->errorMessage = "errorMessage";

        client.setsockopt(ZMQ_IDENTITY, this->peerId.c_str(),
                          strlen(this->peerId.c_str()));

        this->server.bind(address.c_str());
        this->client.connect(address.c_str());

        this->dummyReply.set_lifetime(1);
        this->dummyReply.set_retries(2);

        this->serviceRequests.reset(new map<string, pair<const google::protobuf::Message*, string> >());
        this->serviceResponses.reset(new map<string, pair<bool,shared_ptr<zmq::message_t> > >);
        this->serviceMutex.reset(new boost::mutex());

        this->handler.reset(new rasnet::ServiceResponseHandler(server, serviceRequests,serviceResponses, serviceMutex));
    }

    virtual ~ServiceResponseHandlerTest()
    {
        this->client.disconnect(address.c_str());
        this->client.close();

      //  this->server.unbind(address.c_str());
        this->server.close();
    }

    std::vector<boost::shared_ptr<zmq::message_t> > createErrorResponse()
    {
        std::vector<boost::shared_ptr<zmq::message_t> > result;
        MessageType type;
        type.set_type(MessageType::SERVICE_RESPONSE);

        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::protoToZmq(type)));

        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::stringToZmq(peerId)));

        ServiceCallStatus status;
        status.set_success(false);
        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::protoToZmq(status)));

        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::stringToZmq(errorMessage)));

        return result;
    }

    std::vector<boost::shared_ptr<zmq::message_t> > createSuccessResponse()
    {
        std::vector<boost::shared_ptr<zmq::message_t> > result;
        MessageType type;
        type.set_type(MessageType::SERVICE_RESPONSE);

        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::protoToZmq(type)));

        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::stringToZmq(peerId)));

        ServiceCallStatus status;
        status.set_success(true);
        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::protoToZmq(status)));

        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::protoToZmq(this->dummyReply)));

        return result;
    }


    std::vector<boost::shared_ptr<zmq::message_t> > createInvalidResponse()
    {
        std::vector<boost::shared_ptr<zmq::message_t> > result;
        MessageType type;
        type.set_type(MessageType::ALIVE_PING);

        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::protoToZmq(type)));

        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::stringToZmq(peerId)));

        ServiceCallStatus status;
        status.set_success(false);
        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::protoToZmq(status)));

        result.push_back(boost::shared_ptr<zmq::message_t>(TestUtilities::stringToZmq("error")));

        return result;
    }

    zmq::context_t ctx;
    std::string address;
    std::string peerId;
    std::string errorMessage;
    zmq::socket_t server;
    zmq::socket_t client;

    //Just for testing, if the class is not used, use something else
    ConnectReply dummyReply;

    boost::shared_ptr<std::map<std::string, std::pair<const google::protobuf::Message*, std::string> > > serviceRequests;
    boost::shared_ptr<std::map<std::string, std::pair<bool,boost::shared_ptr<zmq::message_t> > > > serviceResponses;
    boost::shared_ptr<boost::mutex> serviceMutex;

    boost::shared_ptr<rasnet::ServiceResponseHandler> handler;
};

TEST_F(ServiceResponseHandlerTest, canHandle)
{
    std::vector<boost::shared_ptr<zmq::message_t> > message = this->createErrorResponse();
    ASSERT_TRUE(this->handler->canHandle(message));
}

TEST_F(ServiceResponseHandlerTest, canHandleFail)
{
    std::vector<boost::shared_ptr<zmq::message_t> > message;
    ASSERT_FALSE(this->handler->canHandle(message));

    message = this->createInvalidResponse();
    ASSERT_FALSE(this->handler->canHandle(message));
}

TEST_F(ServiceResponseHandlerTest, handleThrowException)
{
    std::vector<boost::shared_ptr<zmq::message_t> > message = this->createInvalidResponse();;
    ASSERT_ANY_THROW(this->handler->handle(message));
}

TEST_F(ServiceResponseHandlerTest, handleError)
{
    this->serviceRequests->insert(make_pair(peerId, pair<const google::protobuf::Message*, string>(NULL,"method")));

    std::vector<boost::shared_ptr<zmq::message_t> > message = this->createErrorResponse();
    ASSERT_NO_THROW(this->handler->handle(message));

    ASSERT_TRUE(this->serviceRequests->empty());
    ASSERT_EQ(1, this->serviceResponses->size());

    std::map<std::string, std::pair<bool,boost::shared_ptr<zmq::message_t> > > ::iterator it;
    it=this->serviceResponses->find(peerId);
    ASSERT_TRUE(it!=this->serviceResponses->end());

    //The peerId must be the same
    ASSERT_EQ(peerId, it->first);

    //We must have an error
    ASSERT_FALSE(it->second.first);
    //Parse the error
    std::string errorResult = ZmqUtil::messageToString(*(it->second.second));
    ASSERT_EQ(errorResult, errorMessage);
}

TEST_F(ServiceResponseHandlerTest, handle)
{
    //Send the request available message from the main thread to the bridge socket
    internal::ServiceRequestAvailable reqAvailable;
    ZmqUtil::send(client, reqAvailable);
    std::string clientId;
    boost::shared_ptr<BaseMessage> baseMessage;
    ZmqUtil::receiveFromPeer(server, baseMessage, clientId);

    //Add the service request to the list of available requests
    this->serviceRequests->insert(make_pair(peerId, pair<const google::protobuf::Message*, string>(NULL,"method")));

    std::vector<boost::shared_ptr<zmq::message_t> > message = this->createSuccessResponse();
    ASSERT_NO_THROW(this->handler->handle(message));

    ASSERT_TRUE(this->serviceRequests->empty());
    ASSERT_EQ(1, this->serviceResponses->size());

    std::map<std::string, std::pair<bool,boost::shared_ptr<zmq::message_t> > > ::iterator it;
    it=this->serviceResponses->find(peerId);
    ASSERT_TRUE(it!=this->serviceResponses->end());

    //The peerId must be the same
    ASSERT_EQ(peerId, it->first);

    //We must have a success message
    ASSERT_TRUE(it->second.first);
    //Parse the response
    ConnectReply repl;
    boost::shared_ptr<zmq::message_t> r=it->second.second;
    repl.ParseFromArray(r->data(), r->size());

    ASSERT_EQ(repl.lifetime(), dummyReply.lifetime());
    ASSERT_EQ(repl.retries(), dummyReply.retries());

    //Check if the ServiceResponse was received through the bridge
    boost::shared_ptr<BaseMessage> serviceResponseAvailableEnv;
    ASSERT_TRUE(ZmqUtil::receive(client, serviceResponseAvailableEnv));
    internal::ServiceResponseAvailable responseAvailable;
    ASSERT_TRUE(responseAvailable.ParseFromString(serviceResponseAvailableEnv->data()));
}
}
}
