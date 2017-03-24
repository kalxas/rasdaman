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

//includes only necessary for the server below, so they are also commented out here
//#include <stdio.h>
//#include <stdlib.h>
//#include <string.h>
//#include <unistd.h>
//#include <sys/types.h> 
//#include <sys/socket.h>
//#include <netinet/in.h>

#include <gtest/gtest.h>
#include "../../src/grpc/grpcutils.hh"

namespace common
{
namespace test
{
    /*Problem below:
     
     "accept" during SetUp() hangs while it waits for a connection, so we
     need to set up multiple threads in order to properly test "isPortBusy"
     
     */
//    // test fixture for listening over a port, for testing isPortBusy
//    class GrpcUtilsTestWithListeningServer : public testing::Test {
//    protected:
//        virtual void SetUp() 
//        {
//            //a few simple initializations
//            sockfd = socket(AF_INET, SOCK_STREAM, 0);
//            bzero((char *) &serv_addr, sizeof(serv_addr));
//            listeningPort = 12094;
//            //setting up server address -- see socket.h for more info
//            serv_addr.sin_family = AF_INET;
//            serv_addr.sin_addr.s_addr = INADDR_ANY;
//            serv_addr.sin_port = htons(listeningPort);
//            //binds socket to address
//            bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr));
//            //start listening
//            listen(sockfd,5);
//            //accept connections
//            newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &cli_len);
//        }
//        
//        virtual void TearDown(){
//            //stop accepting
//            close(newsockfd);
//            //stop listening
//            close(sockfd);
//        }
//    private:
//        //port # for server
//        boost::uint16_t listeningPort;
//        //int to verify socket creation;
//        int sockfd;
//        //int to accept socket connections;
//        int newsockfd;
//        //server socket address structure
//        struct sockaddr_in serv_addr;
//        //socket address for client
//        struct sockaddr_in cli_addr;
//        //sizeof cli_addr, necessary for setting up "accept"
//        unsigned int cli_len = sizeof(cli_addr);
//    };

TEST(GrpcUtilsTest, AddressToStringTest)
{
    std::string host = "localhost";
    boost::uint32_t port = 5002;

    ASSERT_EQ("localhost:5002", GrpcUtils::constructAddressString(host, port));
}

//TODO-GM: create a mock server instead of using the HTTP port
//TEST_F(GrpcUtilsTestWithListeningServer, IsPortBusyHostNameTest)
//{
//    std::string host = "localhost";
//    // HTTP port should be taken
//    boost::uint16_t port = 12094;
//
//    ASSERT_TRUE(GrpcUtils::isPortBusy(host, port));
//}

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

//TEST_F(GrpcUtilsTestWithListeningServer, IsPortBusyIpV4Test)
//{
//    std::string host = "127.0.0.1";
//    // HTTP port should be taken
//    boost::uint16_t port = 12094;
//
//    ASSERT_TRUE(GrpcUtils::isPortBusy(host, port));
//}
//
//TEST_F(GrpcUtilsTestWithListeningServer, IsPortBusyIpV6Test)
//{
//    std::string host = "::1";
//    // HTTP port should be taken
//    boost::uint16_t port = 12094;
//
//    ASSERT_TRUE(GrpcUtils::isPortBusy(host, port));
//}

TEST(GrpcUtilsTest, IsPortBusyInvalidHostNameTest)
{
    std::string host = "qwertyuiolp;[";
    // HTTP port should be taken
    boost::uint16_t port = 0;

    ASSERT_THROW(GrpcUtils::isPortBusy(host, port), std::runtime_error);
}
}// namespace test
}// namespace common
