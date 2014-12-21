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
#include <google/protobuf/service.h>
#include <google/protobuf/stubs/common.h>

#include "../../../../common/src/unittest/gtest.h"
#include "../../../../common/src/logging/easylogging++.hh"
#include "../../../src/service/server/servicemanager.hh"
#include "../../../src/service/client/clientcontroller.hh"

#include "../../../src/messages/test_mess.pb.h"
#include "../../../src/messages/service.pb.h"

using namespace rasnet;
using namespace rnp_test;
using namespace boost;
using namespace google::protobuf;

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

class ServiceManagerTest: public ::testing::Test
{
protected:
    void SetUp()
    {}

    void TearDown()
    {}
}
;

TEST_F(ServiceManagerTest, constructor)
{
    ServiceManager* service;
    EXPECT_NO_THROW(service = new ServiceManager());
    EXPECT_NO_THROW(delete service);
}

TEST_F(ServiceManagerTest, addService)
{
    ServiceManager* manager = new ServiceManager();
    shared_ptr<SearchService> service(new SearchServiceImpl());
    //Adding a service for the first time must not throw an exception
    EXPECT_NO_THROW(manager->addService(service));
    //Adding the same service a second time will throw an exception
    EXPECT_ANY_THROW(manager->addService(service));
    delete manager;
    manager = NULL;

    //Adding a service while the ServiceManager is running
    //will throw an exception
    manager = new ServiceManager();
    manager->serve("tcp://*", 7002);
    EXPECT_ANY_THROW(manager->addService(service));
    delete manager;
}

TEST_F(ServiceManagerTest, serve)
{
    ServiceManager manager;

    EXPECT_NO_THROW(manager.serve("tcp://*", 7002));

    EXPECT_ANY_THROW(manager.serve("tcp://*", 7002));
}
