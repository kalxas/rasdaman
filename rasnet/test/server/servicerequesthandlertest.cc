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
#include "../../src/server/servicerequesthandler.hh"

#include "../messages/testing.pb.h"

namespace rasnet
{
namespace test
{
using boost::scoped_ptr;
using boost::shared_ptr;

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

class ServiceRequestHandlerTest:public ::testing::Test
{
protected:
    ServiceRequestHandlerTest():bridge(ctx, ZMQ_ROUTER),
        client(ctx, ZMQ_DEALER),
        server(ctx, ZMQ_ROUTER)
    {
        this->bridgeAddress = "inproc://bridge_address";
        this->serverAddress = "tcp://localhost:10000";
        this->serverEndpoint = "tcp://*:10000";

        bridge.bind(this->bridgeAddress.c_str());
        server.bind(this->serverEndpoint.c_str());
        client.connect(this->serverAddress.c_str());
    }

    virtual ~ServiceRequestHandlerTest()
    {
        client.disconnect(this->serverAddress.c_str());
        client.close();

  //      server.unbind(this->serverEndpoint.c_str());
        server.close();

    //    bridge.unbind(this->bridgeAddress.c_str());
        bridge.close();
    }

    std::string serverAddress;
    std::string serverEndpoint;
    std::string bridgeAddress;
    zmq::context_t ctx;
    zmq::socket_t bridge;
    zmq::socket_t client;
    zmq::socket_t server;
};

TEST_F(ServiceRequestHandlerTest, constructor )
{
    ServiceRequestHandler* handler;

    ASSERT_NO_THROW(handler = new ServiceRequestHandler(ctx, bridgeAddress));
    ASSERT_NO_THROW(delete handler);
}

TEST_F(ServiceRequestHandlerTest, addService)
{
    scoped_ptr<ServiceRequestHandler> handler (new ServiceRequestHandler(this->ctx, this->bridgeAddress));

    shared_ptr<DummyService> service (new DummyServiceImpl());
    //The service should be added and no exception should be thrown
    ASSERT_NO_THROW(handler->addService(service));
    //Adding a service a second time throws an exception
    ASSERT_ANY_THROW(handler->addService(service));
}

TEST_F(ServiceRequestHandlerTest, canHandle)
{
    //Test valid message
    shared_ptr<DummyService> service (new DummyServiceImpl());
    std::string methodName = Util::getMethodName(service->descriptor()->method(0));
    std::string callId = "callId";
    DummyRequest request;
    request.set_data("Hello");

    ZmqUtil::sendServiceRequest(client, callId, methodName, &request);

    std::vector<boost::shared_ptr<zmq::message_t> > message;
    std::string peer;
    ZmqUtil::receiveCompositeMessageFromPeer(server, peer, message);

    scoped_ptr<ServiceRequestHandler> handler (new ServiceRequestHandler(this->ctx, this->bridgeAddress));
    ASSERT_TRUE(handler->canHandle(message));
}

TEST_F(ServiceRequestHandlerTest, canHandleFail)
{
    scoped_ptr<ServiceRequestHandler> handler (new ServiceRequestHandler(this->ctx, this->bridgeAddress));
    std::vector<boost::shared_ptr<zmq::message_t> > message;

    //Will fail because the message is not of size 4
    ASSERT_FALSE(handler->canHandle(message));

    //TODO:Improve the test
}

TEST_F(ServiceRequestHandlerTest, handleFail)
{
    std::vector<boost::shared_ptr<zmq::message_t> > message;
    std::string peer;
    scoped_ptr<ServiceRequestHandler> handler (new ServiceRequestHandler(this->ctx, this->bridgeAddress));

    //Will fail because the message is invalid
    ASSERT_ANY_THROW(handler->handle(message, peer));
}

TEST_F(ServiceRequestHandlerTest, handleInvalidMethodCall)
{
    shared_ptr<DummyService> service (new DummyServiceImpl());
    std::string methodName = Util::getMethodName(service->descriptor()->method(0));
    std::string callId = "callId";
    DummyRequest request;
    request.set_data("Hello");

    ZmqUtil::sendServiceRequest(client, callId, methodName, &request);

    std::vector<boost::shared_ptr<zmq::message_t> > message;
    std::string peer;
    ZmqUtil::receiveCompositeMessageFromPeer(server, peer, message);

    scoped_ptr<ServiceRequestHandler> handler (new ServiceRequestHandler(this->ctx, this->bridgeAddress));
    //No method with the given name will be found
    ASSERT_NO_THROW(handler->handle(message, peer));

    //Must be failure
    boost::tuple<std::string, std::string, bool, std::string, ::google::protobuf::uint8*, int>
    data = handler->getResponse();

    bool failure = data.get<2>();
    std::string failureMessage = data.get<3>();
    ASSERT_FALSE(failure);
    ASSERT_EQ(INVALID_METHOD_NAME ,failureMessage);

    //A message must be sent from the service request handler
    shared_ptr<BaseMessage> baseMessage;
    std::string bridgeId;
    internal::ServiceResponseAvailable responseAvailable;

    ASSERT_TRUE(ZmqUtil::receiveFromPeer(bridge, baseMessage, bridgeId));
    ASSERT_TRUE(responseAvailable.ParseFromString(baseMessage->data()));
}

TEST_F(ServiceRequestHandlerTest, handleInvalidInputData)
{
    shared_ptr<DummyService> service (new DummyServiceImpl());
    std::string methodName = Util::getMethodName(service->descriptor()->method(0));
    std::string callId = "callId";
    DummyRequest request;
    request.set_data("Hello");

    ZmqUtil::sendServiceRequest(client, callId, methodName, &request);

    std::vector<boost::shared_ptr<zmq::message_t> > message;
    std::string peer;
    ZmqUtil::receiveCompositeMessageFromPeer(server, peer, message);

    //Set the input data to something invalid
    message[3].reset(new zmq::message_t(callId.size()));
    memcpy(message[3]->data(), callId.data(), callId.size());

    scoped_ptr<ServiceRequestHandler> handler (new ServiceRequestHandler(this->ctx, this->bridgeAddress));
    handler->addService(service);

    ASSERT_NO_THROW(handler->handle(message, peer));

    //Must be failure
    boost::tuple<std::string, std::string, bool, std::string, ::google::protobuf::uint8*, int>
    data = handler->getResponse();

    bool failure = data.get<2>();
    std::string failureMessage = data.get<3>();
    ASSERT_FALSE(failure);
    //The failure message must be that the input data is invalid
    ASSERT_EQ(INVALID_INPUT_DATA ,failureMessage);

    //A message must be sent from the service request handler
    shared_ptr<BaseMessage> baseMessage;
    std::string bridgeId;
    internal::ServiceResponseAvailable responseAvailable;

    ASSERT_TRUE(ZmqUtil::receiveFromPeer(bridge, baseMessage, bridgeId));
    ASSERT_TRUE(responseAvailable.ParseFromString(baseMessage->data()));
}

TEST_F(ServiceRequestHandlerTest, handleUnknownException)
{
    shared_ptr<DummyService> service (new DummyServiceImpl());
    std::string methodName = Util::getMethodName(service->descriptor()->method(0));
    std::string callId = "callId";
    DummyRequest request;
    request.set_data("unknown");

    ZmqUtil::sendServiceRequest(client, callId, methodName, &request);

    std::vector<boost::shared_ptr<zmq::message_t> > message;
    std::string peer;
    ZmqUtil::receiveCompositeMessageFromPeer(server, peer, message);

    scoped_ptr<ServiceRequestHandler> handler (new ServiceRequestHandler(this->ctx, this->bridgeAddress));
    handler->addService(service);
    ASSERT_NO_THROW(handler->handle(message, peer));

    //Must be failure
    boost::tuple<std::string, std::string, bool, std::string, ::google::protobuf::uint8*, int>
    data = handler->getResponse();

    bool failure = data.get<2>();
    std::string failureMessage = data.get<3>();
    ASSERT_FALSE(failure);
    //The failure message must be that the input data is invalid
    ASSERT_EQ(UNKOWN_SERVICE_CALL_FAILURE ,failureMessage);

    //A message must be sent from the service request handler
    shared_ptr<BaseMessage> baseMessage;
    std::string bridgeId;
    internal::ServiceResponseAvailable responseAvailable;

    ASSERT_TRUE(ZmqUtil::receiveFromPeer(bridge, baseMessage, bridgeId));
    ASSERT_TRUE(responseAvailable.ParseFromString(baseMessage->data()));
}

TEST_F(ServiceRequestHandlerTest, handleRuntimeException)
{
    shared_ptr<DummyService> service (new DummyServiceImpl());
    std::string methodName = Util::getMethodName(service->descriptor()->method(0));
    std::string callId = "callId";
    DummyRequest request;
    request.set_data("fail");

    ZmqUtil::sendServiceRequest(client, callId, methodName, &request);

    std::vector<boost::shared_ptr<zmq::message_t> > message;
    std::string peer;
    ZmqUtil::receiveCompositeMessageFromPeer(server, peer, message);

    scoped_ptr<ServiceRequestHandler> handler (new ServiceRequestHandler(this->ctx, this->bridgeAddress));
    handler->addService(service);
    ASSERT_NO_THROW(handler->handle(message, peer));

    //Must be failure
    boost::tuple<std::string, std::string, bool, std::string, ::google::protobuf::uint8*, int>
    data = handler->getResponse();

    bool failure = data.get<2>();
    std::string failureMessage = data.get<3>();
    ASSERT_FALSE(failure);
    //The failure message must be that the input data is invalid
    ASSERT_EQ("fail" ,failureMessage);

    //A message must be sent from the service request handler
    shared_ptr<BaseMessage> baseMessage;
    std::string bridgeId;
    internal::ServiceResponseAvailable responseAvailable;

    ASSERT_TRUE(ZmqUtil::receiveFromPeer(bridge, baseMessage, bridgeId));
    ASSERT_TRUE(responseAvailable.ParseFromString(baseMessage->data()));
}

TEST_F(ServiceRequestHandlerTest, handleSuccess)
{
    shared_ptr<DummyService> service (new DummyServiceImpl());
    std::string methodName = Util::getMethodName(service->descriptor()->method(0));
    std::string callId = "callId";
    DummyRequest request;
    request.set_data("hello");

    ZmqUtil::sendServiceRequest(client, callId, methodName, &request);

    std::vector<boost::shared_ptr<zmq::message_t> > message;
    std::string peer;
    ZmqUtil::receiveCompositeMessageFromPeer(server, peer, message);

    scoped_ptr<ServiceRequestHandler> handler (new ServiceRequestHandler(this->ctx, this->bridgeAddress));
    handler->addService(service);
    ASSERT_NO_THROW(handler->handle(message, peer));

    //Must be failure
    boost::tuple<std::string, std::string, bool, std::string, ::google::protobuf::uint8*, int>
    data = handler->getResponse();

    bool success = data.get<2>();
    ASSERT_TRUE(success);

    DummyReply reply;
    ASSERT_TRUE(reply.ParseFromArray(data.get<4>(), data.get<5>()));

    ASSERT_EQ(reply.data(), request.data());

    delete[] data.get<4>();

    shared_ptr<BaseMessage> baseMessage;
    std::string bridgeId;
    internal::ServiceResponseAvailable responseAvailable;

    ASSERT_TRUE(ZmqUtil::receiveFromPeer(bridge, baseMessage, bridgeId));
    ASSERT_TRUE(responseAvailable.ParseFromString(baseMessage->data()));

}
}
}
