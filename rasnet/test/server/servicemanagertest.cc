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
#include "../../src/common/zmqutil.hh"
#include "../../src/common/util.hh"
#include "../../src/messages/internal.pb.h"
#include "../../src/messages/communication.pb.h"
#include "../../src/exception/exceptionmessages.hh"
#include "../../src/server/servicemanager.hh"

#include "../messages/testing.pb.h"

namespace rasnet
{
namespace test
{

class DummyServiceImpl:public DummyService
{
public:
    bool destructorCalled;

    DummyServiceImpl()
    {
        destructorCalled = false;
    }

    virtual ~DummyServiceImpl()
    {
        destructorCalled = true;
    }

    void DummyCommand(google::protobuf::RpcController *controller, const DummyRequest *request, DummyReply *response, google::protobuf::Closure *done)
    {
        if(request->data()=="hello")
        {
            response->set_data("hello");
        }
        else if(request->data()=="fail")
        {
            throw std::runtime_error("fail");
        }
        else if(request->data()=="unknown")
        {
            int error = 3;
            throw error;
        }
        else
        {
            controller->SetFailed("controller");
        }

        done->Run();
    }
};

class ServiceManagerTest:public ::testing::Test
{
protected :
    ServiceManagerTest():
        client(ctx, ZMQ_DEALER),
        manager(config)
    {
        this->serviceEndpoint = "tcp://*:10000";
        this->externalServiceEndpoint = "tcp://localhost:10000";

        int linger = 0;
        this->client.connect(this->externalServiceEndpoint.c_str());
        client.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
    }

    virtual ~ServiceManagerTest()
    {
        this->client.disconnect(this->externalServiceEndpoint.c_str());
    }

    zmq::context_t ctx;
    zmq::socket_t client;
    std::string serviceEndpoint;
    std::string externalServiceEndpoint;
    ServiceManagerConfig config;
    ServiceManager manager;
};

TEST_F(ServiceManagerTest, constructor)
{
    ServiceManagerConfig config;
    ServiceManager *manager;

    ASSERT_NO_THROW(manager=new ServiceManager(config));
    ASSERT_NO_THROW(delete manager);

    //serve
    ASSERT_NO_THROW(manager=new ServiceManager(config));
    //Server on invalid address
    manager->serve("address");
    ASSERT_NO_THROW(delete manager);

    ASSERT_NO_THROW(manager=new ServiceManager(config));
    //Server on invalid address
    manager->serve("tcp://*:10000");
    ASSERT_NO_THROW(delete manager);
}

TEST_F(ServiceManagerTest, serve)
{
    ASSERT_NO_THROW(manager.serve(this->serviceEndpoint));
    ASSERT_ANY_THROW(manager.serve(this->serviceEndpoint));
}

TEST_F(ServiceManagerTest, connectRequest)
{
    ConnectRequest request;
    request.set_lifetime(100);
    request.set_retries(2);

    manager.serve(this->serviceEndpoint);
    ZmqUtil::sendCompositeMessage(client, MessageType::CONNECT_REQUEST, request);

    std::vector<boost::shared_ptr<zmq::message_t> >  message;
    ZmqUtil::receiveCompositeMessage(client, message);

    ASSERT_EQ(2, message.size());

    MessageType type;
    ASSERT_TRUE(type.ParseFromArray(message[0]->data(), message[0]->size()));
    ASSERT_EQ(MessageType::CONNECT_REPLY, type.type());

    ConnectReply reply;
    ASSERT_TRUE(reply.ParseFromArray(message[1]->data(), message[1]->size()));
    ASSERT_EQ(config.getAliveTimeout(), reply.lifetime());
    ASSERT_EQ(config.getAliveRetryNo(), reply.retries());
}

TEST_F(ServiceManagerTest, ping)
{
    manager.serve(this->serviceEndpoint);
    ZmqUtil::sendCompositeMessage(client, MessageType::ALIVE_PING);

    std::vector<boost::shared_ptr<zmq::message_t> >  message;
    ZmqUtil::receiveCompositeMessage(client, message);

    ASSERT_EQ(1, message.size());

    MessageType type;
    ASSERT_TRUE(type.ParseFromArray(message[0]->data(), message[0]->size()));
    ASSERT_EQ(MessageType::ALIVE_PONG, type.type());
}

TEST_F(ServiceManagerTest, pong)
{
    manager.serve(this->serviceEndpoint);
    ASSERT_TRUE(ZmqUtil::sendCompositeMessage(client, MessageType::ALIVE_PONG));
}

TEST_F(ServiceManagerTest, requestHandler)
{
    boost::shared_ptr<DummyService> service (new DummyServiceImpl());
    std::string methodName = Util::getMethodName(service->descriptor()->method(0));
    std::string callId = "callId";
    DummyRequest request;
    request.set_data("hello");

    manager.addService(service);
    manager.serve(this->serviceEndpoint);

    ZmqUtil::sendServiceRequest(client, callId, methodName, &request);

    std::vector<boost::shared_ptr<zmq::message_t> >  message;
    ZmqUtil::receiveCompositeMessage(client, message);

    ASSERT_EQ(4, message.size());

    MessageType type;
    ASSERT_TRUE(type.ParseFromArray(message[0]->data(), message[0]->size()));
    ASSERT_EQ(MessageType::SERVICE_RESPONSE, type.type());

    std::string receivedCallID = ZmqUtil::messageToString(*message[1]);
    ASSERT_EQ(callId, receivedCallID);

    ServiceCallStatus status;
    ASSERT_TRUE(status.ParseFromArray(message[2]->data(), message[2]->size()));
    ASSERT_EQ(true, status.success());

    DummyReply reply;
    ASSERT_TRUE(reply.ParseFromArray(message[3]->data(), message[3]->size()));
    ASSERT_EQ(request.data(), reply.data());
}
}
}
