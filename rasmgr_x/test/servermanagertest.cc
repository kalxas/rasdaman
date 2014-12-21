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

#ifndef SERVERMANAGERTEST_CC
#define SERVERMANAGERTEST_CC

#include "../../comm/src/util/unittest/gtest.h"
#include "../../comm/src/util/logging/easylogging++.hh"
#include "../src/servermanager.hh"
#include "../src/servermanagementstrategy.hh"
#include <boost/shared_ptr.hpp>
#include "../../comm/src/messages/rasmgr_rassrvr_service.pb.h"
#include "../src/servermanagementservice.hh"
#include "../../comm/src/service/server/ServiceManager.hh"
#include "../src/rasserver.hh"


using boost::shared_ptr;
using rasmgr::ServerManagementStrategy;
using rasmgr::ServerManager;

class ServerManagerTest : public ::testing::Test
{
protected:
    void SetUp()
    {

        serverManager.reset(new rasmgr::ServerManager(boost::shared_ptr<rasmgr::ServerManagementStrategy>(new rasmgr::ServerManagementStrategy())));

        boost::shared_ptr<rasmgr_rassrvr_service::RasMgrRasServerService> rasServerService(new rasmgr::ServerManagementService(serverManager));

        boost::shared_ptr<rnp::ServiceManager> serviceManager (new rnp::ServiceManager());
        serviceManager->addService(rasServerService.get(), false);
        serviceManager->serve("tcp://*", 7000);

    }

    void TearDown()
    {

    }

    shared_ptr<ServerManager> serverManager;
};

TEST_F(ServerManagerTest, spawnOneServerTest)
{
    shared_ptr<rasmgr::RasServer> rasServer = serverManager->getFreeServer();
    ASSERT_TRUE(rasServer->isFree());
    rasServer->stop();
    ASSERT_TRUE(rasServer->isStopped());
}

#endif // SERVERMANAGERTEST_HH
