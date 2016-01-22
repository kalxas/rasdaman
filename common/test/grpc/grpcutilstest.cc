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
#include "../../src/grpc/grpcutils.hh"

namespace common
{
namespace test
{

TEST(GrpcUtilsTest, AddressToStringTest)
{
    std::string host="localhost";
    boost::uint32_t port = 5002;

    ASSERT_EQ("localhost:5002", GrpcUtils::constructAddressString(host, port));
}

//TODO-GM: create a mock server instead of using the HTTP port
TEST(GrpcUtilsTest, IsPortBusyHostNameTest)
{
    std::string host = "localhost";
    // HTTP port should be taken
    boost::uint16_t port = 80;

    ASSERT_TRUE(GrpcUtils::isPortBusy(host, port));
}

TEST(GrpcUtilsTest, IsPortFreeHostNameTest)
{
    std::string host = "localhost";
    // port = 0 will assign a random free port
    boost::uint16_t port = 0;

    ASSERT_FALSE(GrpcUtils::isPortBusy(host, port));
}

TEST(GrpcUtilsTest, IsPortFreeIpV4Test)
{
    std::string host = "127.0.0.1";
    // port = 0 will assign a random free port
    boost::uint16_t port = 0;

    ASSERT_FALSE(GrpcUtils::isPortBusy(host, port));
}

TEST(GrpcUtilsTest, IsPortFreeIpV6Test)
{
    std::string host = "::1";
    // port = 0 will assign a random free port
    boost::uint16_t port = 0;

    ASSERT_FALSE(GrpcUtils::isPortBusy(host, port));
}

TEST(GrpcUtilsTest, IsPortBusyIpV4Test)
{
    std::string host = "127.0.0.1";
    // HTTP port should be taken
    boost::uint16_t port = 80;

    ASSERT_TRUE(GrpcUtils::isPortBusy(host, port));
}

TEST(GrpcUtilsTest, IsPortBusyIpV6Test)
{
    std::string host = "::1";
    // HTTP port should be taken
    boost::uint16_t port = 80;

    ASSERT_TRUE(GrpcUtils::isPortBusy(host, port));
}

TEST(GrpcUtilsTest, IsPortBusyInvalidHostNameTest)
{
    std::string host = "qwertyuiolp;[";
    // HTTP port should be taken
    boost::uint16_t port = 0;

    ASSERT_THROW(GrpcUtils::isPortBusy(host, port), std::runtime_error);
}
}
}
