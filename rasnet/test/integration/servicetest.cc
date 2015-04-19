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
#include "../../src/client/channel.hh"
#include "../../src/client/clientcontroller.hh"

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

class ServiceTest: public ::testing::Test
{
protected:
    ServiceTest():manager(config)
    {
        this->address = "tcp://*:10000";
        this->externalAddress = "tcp://localhost:10000";
        this->dummyService.reset(new DummyServiceImpl());
    }

    virtual ~ServiceTest()
    {}

    std::string externalAddress;
    std::string address;
    ServiceManagerConfig config;
    ServiceManager manager;
    boost::shared_ptr<DummyService> dummyService;
};

TEST_F(ServiceTest, channelFailsToConnect)
{
    ChannelConfig channelConfig;
    ASSERT_ANY_THROW(Channel channel(externalAddress, channelConfig));
//    ClientController controller;
//

//    DummyRequest request;
//    request.set_data("hello");
//    DummyReply reply;


//    google::protobuf::Closure* doNothingClosure = google::protobuf::NewPermanentCallback(&google::protobuf::DoNothing);
//    DummyService* service = new DummyService_Stub(&channel);


//    service->DummyCommand(&controller, &request, &reply, doNothingClosure);

//    delete doNothingClosure;
//    delete service;
}

TEST_F(ServiceTest, successfulCall)
{
    manager.addService(dummyService);
    manager.serve(address);

    ChannelConfig channelConfig;
    Channel channel(externalAddress, channelConfig);

    ClientController controller;
    google::protobuf::Closure* doNothingClosure = google::protobuf::NewPermanentCallback(&google::protobuf::DoNothing);
    DummyService* service = new DummyService_Stub(&channel);

    DummyRequest request;
    request.set_data("hello");
    DummyReply reply;
    service->DummyCommand(&controller, &request, &reply, doNothingClosure);

    ASSERT_FALSE(controller.Failed());
    ASSERT_EQ(request.data(), reply.data());

    delete doNothingClosure;
    delete service;
}

TEST_F(ServiceTest, serviceManagerDies)
{
    ServiceManagerConfig serviceConfig;
    config.setAliveTimeout(10);
    config.setAliveRetryNo(2);

    ServiceManager* manager = new ServiceManager(serviceConfig);
    manager->addService(dummyService);
    manager->serve(address);

    //Channel connects to server
    ChannelConfig channelConfig;
    Channel channel(externalAddress, channelConfig);

    //Server gets shutdown
    delete manager;

    ClientController controller;
    google::protobuf::Closure* doNothingClosure = google::protobuf::NewPermanentCallback(&google::protobuf::DoNothing);
    DummyService* service = new DummyService_Stub(&channel);

    DummyRequest request;
    request.set_data("hello");
    DummyReply reply;
    service->DummyCommand(&controller, &request, &reply, doNothingClosure);

    ASSERT_TRUE(controller.Failed());

    delete doNothingClosure;
    delete service;
}
}
}
