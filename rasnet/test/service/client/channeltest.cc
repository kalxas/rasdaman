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

#include "../../../src/service/client/clientcontroller.hh"
#include "../../../src/sockets/server/serversocket.hh"
#include "../../../../common/src/unittest/gtest.h"
#include "../../../../common/src/logging/easylogging++.hh"
#include "../../../src/messages/service.pb.h"
#include "../../../src/messages/test_mess.pb.h"
#include "../../../src/service/client/channel.hh"

using namespace std;
using namespace rasnet;
using namespace rnp_test;
using namespace base;
using namespace google::protobuf;

class ChannelTest: public ::testing::Test
{
public:
    void replyToServiceCall()
    {
        string peerId;
        BaseMessage message;
        ServiceResponse response;

        this->server->receive(peerId, message);
        if (message.type()
                == ServiceRequest::default_instance().GetTypeName())
        {
            ServiceRequest req;
            req.ParseFromString(message.data());

            RequestType request_message;
            request_message.ParseFromString(req.input_value());

            ResponseType reply_message;
            reply_message.set_repl(ChannelTest::replyMessage);
            response.set_id(req.id());
            response.set_output_value(reply_message.SerializeAsString());

            this->server->send(peerId, response);
        }
        else
        {
            LERROR<<message.DebugString();
        }
    }

    void replyWithError()
    {
        string peerId;
        BaseMessage message;
        ServiceResponse response;

        this->server->receive(peerId, message);
        LINFO<<"Reply with error"<<message.DebugString();
        if (message.type()
                == ServiceRequest::default_instance().GetTypeName())
        {

            ServiceRequest req;
            req.ParseFromString(message.data());

            response.set_id(req.id());
            response.set_error(this->errorMessage);
            this->server->send(peerId, response);
        }
        else
        {
            LERROR<<message.DebugString();
        }
    }

protected:
    virtual void SetUp()
    {
        hostAddr = "tcp://*:7001";
        serverAddr = "tcp://localhost:7001";
        replyMessage = "Who's there?";
        errorMessage = "Big ugly fail.";
        ServerSocketConfig config;
        server = new ServerSocket(hostAddr, config);
    }

    virtual void TearDown()
    {
        delete server;
    }


    ServerSocket* server;
    string errorMessage;
    string replyMessage;
    zmq::context_t ctx;
    string serverAddr;
    string hostAddr;
};

TEST_F(ChannelTest, constructor)
{
    Channel* channel;
    ASSERT_NO_THROW(channel = new Channel("tcp://localhost", 7001));
    delete channel;
    channel=NULL;
}


TEST_F(ChannelTest, CallMethodInvalid)
{
    RequestType req;
    ResponseType resp;
    ClientController controller;
    Channel* channel = new Channel("tcp://localhost", 7001);
    Closure* doNothing = NewPermanentCallback(&DoNothing);
    SearchService* service = new SearchService_Stub(channel);

    req.set_req("Knock Knock");

    ASSERT_ANY_THROW(service->Search(NULL, &req, &resp, doNothing));
    ASSERT_ANY_THROW(service->Search(&controller, NULL, &resp, doNothing));
    ASSERT_ANY_THROW(service->Search(&controller, &req, NULL, doNothing));
    ASSERT_ANY_THROW(service->Search(&controller, &req, &resp, NULL));

    delete channel;
    channel=NULL;
    delete doNothing;
    doNothing=NULL;
    delete service;
    service=NULL;
}

TEST_F(ChannelTest, CallMethodFail)
{
    Channel* channel = new Channel("tcp://localhost", 7001);
    ClientController controller;
    Closure* doNothing = NewCallback(&DoNothing);
    RequestType req;
    ResponseType resp;
    SearchService* service = new SearchService_Stub(channel);
    req.set_req("Knock Knock");

    boost::thread thr(&ChannelTest::replyWithError, this);

    ASSERT_NO_THROW(service->Search(&controller, &req, &resp, doNothing));
    ASSERT_TRUE(controller.Failed());

    ASSERT_EQ(this->errorMessage, controller.ErrorText());

    thr.join();
    delete channel;
    channel=NULL;
    delete service;
    service=NULL;
}


TEST_F(ChannelTest, CallMethod)
{
    RequestType req;
    ResponseType resp;
    Channel* channel = new Channel("tcp://localhost", 7001);
    ClientController controller;
    Closure* doNothing = NewCallback(&DoNothing);
    SearchService* service = new SearchService_Stub(channel);
    req.set_req("Knock Knock");

    boost::thread thr(&ChannelTest::replyToServiceCall, this);

    ASSERT_NO_THROW(service->Search(&controller, &req, &resp, doNothing));
    ASSERT_FALSE(controller.Failed());
    ASSERT_EQ(this->replyMessage, resp.repl());

    thr.join();
    delete channel;
    channel=NULL;
    delete service;
    service=NULL;
}

//TEST_F(ChannelTest,  DeadPeer){
//  RequestType req;
//  ResponseType resp;
//  Channel* channel = new Channel("tcp://localhost", 7001);
//  ClientController controller;
//  Closure* doNothing = NewCallback(&DoNothing);
//  SearchService* service = new SearchService_Stub(channel);
//  req.set_req("Knock Knock");

//  boost::thread thr(&ChannelTest::replyToServiceCall, this);

//  ASSERT_NO_THROW(service->Search(&controller, &req, &resp, doNothing));
// // ASSERT_TRUE(controller.Failed());
//  LINFO<<controller.ErrorText();

//  doNothing = NewCallback(&DoNothing);
//  ASSERT_NO_THROW(service->Search(&controller, &req, &resp, doNothing));
////  ASSERT_FALSE(controller.Failed());
//  LINFO<<controller.ErrorText();

//  thr.join();
//  delete channel;
//  channel=NULL;
//  delete service;
//  service=NULL;
//}
