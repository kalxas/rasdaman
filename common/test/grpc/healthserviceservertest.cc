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

#include <memory>
#include <chrono>

#include <boost/cstdint.hpp>
#include <grpc++/grpc++.h>

#include "../../src/unittest/gtest.h"
#include "../../src/grpc/healthserviceimpl.hh"

namespace common
{
namespace test
{
class HealthServiceServerTest:public ::testing::Test
{
protected:
    HealthServiceServerTest()
    {
        this->serverAddress="localhost:9000";
        this->serverService = new HealthServiceImpl();

        grpc::ServerBuilder builder;
        builder.AddListeningPort(this->serverAddress, grpc::InsecureServerCredentials());

        builder.RegisterService(this->serverService);

        this->server = builder.BuildAndStart();

        std::shared_ptr<grpc::Channel> channel( grpc::CreateChannel(serverAddress, grpc::InsecureChannelCredentials()));
        this->service.reset(new HealthService::Stub(channel));
    }

    ~HealthServiceServerTest()
    {
        this->server->Shutdown();
        delete this->serverService;
    }

    std::string serverAddress;
    std::unique_ptr<grpc::Server> server;
    boost::shared_ptr<HealthService::Stub> service;
    HealthServiceImpl* serverService;
};

TEST_F(HealthServiceServerTest, serviceIsRunning)
{
    std::string serviceId = "grpc.test.TestService";
    this->serverService->setStatus(serviceId, HealthCheckResponse::SERVING);

    HealthCheckResponse response;
    HealthCheckRequest request;

    request.set_service(serviceId);

    grpc::ClientContext context;
    grpc::Status status = this->service->Check(&context, request, &response);

    ASSERT_TRUE(status.ok());
    ASSERT_EQ(HealthCheckResponse::SERVING, response.status());
}

TEST_F(HealthServiceServerTest, serviceIsNotRunning)
{
    std::string serviceId = "grpc.test.TestService";
    this->serverService->setStatus(serviceId, HealthCheckResponse::SERVING);

    HealthCheckResponse response;
    HealthCheckRequest request;

    request.set_service(serviceId);

    grpc::ClientContext context;

    std::chrono::system_clock::time_point deadline = std::chrono::system_clock::now() + std::chrono::milliseconds(100);
    context.set_deadline(deadline);

    this->server->Shutdown();
    grpc::Status status = this->service->Check(&context, request, &response);

    ASSERT_FALSE(status.ok());
}

}
}
