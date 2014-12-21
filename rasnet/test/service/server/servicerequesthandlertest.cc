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

#include <boost/shared_ptr.hpp>

#include "../../../../common/src/unittest/gtest.h"
#include "../../../../common/src/logging/easylogging++.hh"

#include "../../../src/util/proto/protozmq.hh"
#include "../../../src/sockets/client/clientsocket.hh"
#include "../../../src/sockets/server/serversocket.hh"

#include "../../../src/messages/service.pb.h"
#include "../../../src/messages/base.pb.h"
#include "../../../src/messages/test_mess.pb.h"

#include "../../../src/service/server/servicerequesthandler.hh"

using namespace rasnet;
using namespace boost;
using namespace base;
using namespace rnp_test;

class SearchServiceImpl: public rnp_test::SearchService
{
public:
    bool destructor_called;

    SearchServiceImpl()
    {
        destructor_called = false;

    }
    virtual void Search(::google::protobuf::RpcController* controller,
                        const RequestType* request, ResponseType* response,
                        ::google::protobuf::Closure* done)
    {
        if (request->req() == "Hello")
        {
            response->set_repl("Hello");
        }
        else
        {
            response->set_repl("Bye");
        }
        done->Run();
    }
    virtual ~SearchServiceImpl()
    {
        destructor_called = true;
    }

};

class ServiceRequestHandlerTest: public ::testing::Test
{

protected:

    virtual void SetUp()
    {
        this->bridgeAddr="inproc://bridge";
    }

    virtual void TearDown()
    {}

    ServiceRequest createSearchServiceRequest()
    {
        SearchService* stub = new rnp_test::SearchService_Stub(NULL);
        ServiceRequest request;
        RequestType input;
        input.set_req("Hellooooo");

        request.set_id(1);
        request.set_method_name(
            ProtoZmq::getServiceMethodName(
                stub->GetDescriptor()->method(0)));
        request.set_input_value(input.SerializeAsString());
        delete stub;
        return request;
    }

    zmq::context_t context;
    std::string bridgeAddr;
};


TEST_F(ServiceRequestHandlerTest, constructing)
{
    ServiceRequestHandler* handler;
    ASSERT_NO_THROW(handler = new ServiceRequestHandler(this->context, this->bridgeAddr));
    ASSERT_NO_THROW(delete handler);
}


TEST_F(ServiceRequestHandlerTest, canHandle)
{
    ServiceRequest req = this->createSearchServiceRequest();

    BaseMessage envelope;
    scoped_ptr<ServiceRequestHandler> handler (new ServiceRequestHandler(this->context, this->bridgeAddr));

    ASSERT_FALSE(handler->canHandle(envelope));

    envelope.set_type(req.GetTypeName());
    envelope.set_data(req.SerializeAsString());
    ASSERT_TRUE(handler->canHandle(envelope));
}


TEST_F(ServiceRequestHandlerTest, addService)
{
    scoped_ptr<ServiceRequestHandler> handler (new ServiceRequestHandler(this->context, this->bridgeAddr));

    shared_ptr<SearchService> service (new SearchServiceImpl());
    //The service should be added and no exception should be thrown
    ASSERT_NO_THROW(handler->addService(service));
    //Adding a service a second time throws an exception
    ASSERT_ANY_THROW(handler->addService(service));
}

TEST_F(ServiceRequestHandlerTest, handleFailures)
{
    ServerSocketConfig serverConfig;
    ClientSocketConfig clientConfig;
    ServiceResponse resp =  ServiceResponse::default_instance();
    ServiceRequestHandler handler(this->context, this->bridgeAddr );
    BaseMessage incoming_mess;
    BaseMessage service_reply;
    zmq::context_t ctx;
    std::string peer_id;

    zmq::socket_t client(this->context,ZMQ_DEALER);
    client.bind(this->bridgeAddr.c_str());

    //Invalid message => handle will throw an exception
    ASSERT_ANY_THROW(handler.handle(incoming_mess, peer_id));

    //Valid message but no service
    ServiceRequest request = this->createSearchServiceRequest();
    incoming_mess.set_type(request.GetTypeName());
    incoming_mess.set_data(request.SerializeAsString());
    ASSERT_NO_THROW(handler.handle(incoming_mess, peer_id));

    ProtoZmq::zmqReceiveFromPeer(client,peer_id, service_reply);
    ASSERT_EQ(service_reply.type(), resp.GetTypeName());
    ASSERT_TRUE(resp.ParseFromString(service_reply.data()));
    ASSERT_TRUE(resp.has_error());
    ASSERT_FALSE(resp.has_output_value());
    ASSERT_EQ("This method SearchService.Search is not offered by the server.", resp.error());

    //Add service
    shared_ptr<SearchService> service (new SearchServiceImpl());
    handler.addService(service);

    //Valid message with service
    request = this->createSearchServiceRequest();
    incoming_mess.set_type(request.GetTypeName());
    incoming_mess.set_data(request.SerializeAsString());

    ASSERT_NO_THROW(handler.handle(incoming_mess, peer_id));

    ProtoZmq::zmqReceiveFromPeer(client,peer_id, service_reply);

    ASSERT_EQ(service_reply.type(), resp.GetTypeName());
    ASSERT_TRUE(resp.ParseFromString(service_reply.data()));
    ASSERT_TRUE(resp.has_output_value());

    ResponseType ret_val;
    ret_val.ParseFromString(resp.output_value());
    ASSERT_EQ("Bye", ret_val.repl());
}
