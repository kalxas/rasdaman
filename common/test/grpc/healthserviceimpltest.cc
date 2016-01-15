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

#include <boost/cstdint.hpp>

#include <gtest/gtest.h>
#include "../../src/grpc/healthserviceimpl.hh"

namespace common
{
namespace test
{
static HealthCheckResponse::ServingStatus getStatusHelper(HealthServiceImpl& impl, std::string service)
{
    HealthCheckResponse response;
    HealthCheckRequest request;
    request.set_service(service);

    impl.Check(NULL, &request, &response);

    return response.status();
}

TEST(HealthServiceImplTest, setStatus)
{
    HealthServiceImpl impl;
    std::string serviceId = "grpc.test.TestService";

    ASSERT_EQ(HealthCheckResponse::SERVING, getStatusHelper(impl, ""));

    impl.setStatus(serviceId, HealthCheckResponse::SERVING);
    ASSERT_EQ(HealthCheckResponse::SERVING, getStatusHelper(impl, serviceId));

    impl.setStatus(serviceId, HealthCheckResponse::NOT_SERVING);
    ASSERT_EQ(HealthCheckResponse::NOT_SERVING, getStatusHelper(impl, serviceId));

    impl.setStatus(serviceId, HealthCheckResponse::UNKNOWN);
    ASSERT_EQ(HealthCheckResponse::UNKNOWN, getStatusHelper(impl, serviceId));
}


TEST(HealthServiceImplTest, clearStatus)
{
    HealthServiceImpl impl;
    std::string serviceId = "grpc.test.TestService";

    impl.setStatus(serviceId, HealthCheckResponse::SERVING);
    ASSERT_EQ(HealthCheckResponse::SERVING, getStatusHelper(impl, serviceId));

    impl.clearStatus(serviceId);

    ASSERT_EQ(HealthCheckResponse::UNKNOWN, getStatusHelper(impl, serviceId));
}

TEST(HealthServiceImplTest, clearAll)
{
    HealthServiceImpl impl;
    std::string firstServiceId = "grpc.test.FirstTestService";
    std::string secondServiceId = "grpc.test.SecondTestService";

    impl.setStatus(firstServiceId, HealthCheckResponse::SERVING);
    ASSERT_EQ(HealthCheckResponse::SERVING, getStatusHelper(impl, firstServiceId));
    impl.setStatus(secondServiceId, HealthCheckResponse::SERVING);
    ASSERT_EQ(HealthCheckResponse::SERVING, getStatusHelper(impl, secondServiceId));

    impl.clearAll();
    ASSERT_EQ(HealthCheckResponse::UNKNOWN, getStatusHelper(impl, firstServiceId));
    ASSERT_EQ(HealthCheckResponse::UNKNOWN, getStatusHelper(impl, secondServiceId));

}
}
}
