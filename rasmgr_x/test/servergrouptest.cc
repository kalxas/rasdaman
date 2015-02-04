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

#include <string>
#include <utility>
#include <set>

#include <boost/cstdint.hpp>
#include <boost/shared_ptr.hpp>

#include "../../common/src/unittest/gtest.h"
#include "../../common/src/mock/gmock.h"
#include "../../common/src/logging/easylogging++.hh"

#include "../../rasnet/src/service/server/servicemanager.hh"
#include "../../rasnet/src/messages/rassrvr_rasmgr_service.pb.h"
#include "../src/serverrasnet.hh"
#include "../src/servergroupconfig.hh"
#include "../src/servergroup.hh"
#include "../src/databasehostmanager.hh"
#include "../src/rasmgrconfig.hh"

#include "mocks/dummyprocessmanager.hh"

using ::testing::AtLeast;                     // #1
using ::testing::_;
using ::testing::Return;

using rasmgr::ServerRasNet;
using rasnet::ServiceManager;
using rasnet::service::RasServerService;
using rasmgr::ServerGroup;
using rasmgr::ServerGroupConfig;
using rasmgr::RasMgrConfig;


class PhonyRasServerService:public rasnet::service::RasServerService
{
public:

    virtual void AllocateClient(::google::protobuf::RpcController* controller,
                                const ::rasnet::service::AllocateClientReq* request,
                                ::rasnet::service::Void* response,
                                ::google::protobuf::Closure* done)
    {
        std::pair<std::set<std::pair<std::string, std::string> >::iterator, bool> result;
        std::pair<std::string, std::string> data(request->clientid(),request->sessionid());

        result = this->clientList.insert(data);
        if(!result.second)
        {
            controller->SetFailed("Client already in list");
        }
    }

    virtual void DeallocateClient(::google::protobuf::RpcController* controller,
                                  const ::rasnet::service::DeallocateClientReq* request,
                                  ::rasnet::service::Void* response,
                                  ::google::protobuf::Closure* done)
    {
        this->clientList.erase(std::make_pair(request->clientid(), request->sessionid()));
    }

    virtual void Close(::google::protobuf::RpcController* controller,
                       const ::rasnet::service::CloseServerReq* request,
                       ::rasnet::service::Void* response,
                       ::google::protobuf::Closure* done)
    {}

    virtual void GetClientStatus(::google::protobuf::RpcController* controller,
                                 const ::rasnet::service::ClientStatusReq* request,
                                 ::rasnet::service::ClientStatusRepl* response,
                                 ::google::protobuf::Closure* done)
    {
        response->set_status(rasnet::service::ClientStatusRepl_Status_ALIVE);
    }

    virtual void GetServerStatus(::google::protobuf::RpcController* controller,
                                 const ::rasnet::service::ServerStatusReq* request,
                                 ::rasnet::service::ServerStatusRepl* response,
                                 ::google::protobuf::Closure* done)
    {
        response->set_clientqueuesize(this->clientList.size());
    }

private:
    std::set<std::pair<std::string, std::string> > clientList;
}
;


class ServerGroupTest:public ::testing::Test
{
protected:
    ServerGroupTest()
    {
        hostName = "tcp://localhost";
        dbName= "dbName";
        port = 7001;
        dbHostName = "dbHostName";
        rasmgr::Database db(dbName);

        this->processManager.reset(new DummyProcessManager());

        boost::shared_ptr<RasServerService> service (new PhonyRasServerService());

        manager.reset((new ServiceManager()));
        manager->addService(service);
        manager->serve("tcp://*", port);

        ports.insert(port);

        dbHostManager.reset(new rasmgr::DatabaseHostManager());
        dbHostManager->defineDatabaseHost(dbHostName, "connectString", "user","passwd");

//        boost::shared_ptr<rasmgr::DatabaseHost> dbHost=dbHostManager->getDatabaseHost(dbHostName);
//        dbHost->addDbToHost(db);

        ServerGroupConfig config("serverGroup", 1, "tcp://localhost", ports,dbHostName);
        serverGroup.reset(new ServerGroup(config, this->dbHostManager, this->processManager));
    }

    std::string hostName;
    std::string dbName;
    boost::int32_t port;
    std::string dbHostName;
    std::set<boost::int32_t> ports;

    boost::scoped_ptr<ServiceManager> manager;
    boost::shared_ptr<rasmgr::DatabaseHostManager> dbHostManager;
    boost::shared_ptr<rasmgr::ServerGroup> serverGroup;
    boost::shared_ptr<common::IProcessManager> processManager;
};

TEST_F(ServerGroupTest, constructorValidation)
{
    //TODO: Make this fail.
    ServerGroup* group;

    ServerGroupConfig config("serverGroup", 1, "tcp://localhost", ports,dbHostName);

    ASSERT_NO_THROW(group=new ServerGroup(config,dbHostManager,processManager));
    ASSERT_TRUE(this->dbHostManager->getDatabaseHost(dbHostName)->isBusy());

    delete group;
}

TEST_F(ServerGroupTest, isBusy)
{
    boost::int32_t timeout = 10; //milliseconds
    RasMgrConfig::getInstance()->setRasServerTimeout(timeout);
    ASSERT_FALSE(this->serverGroup->isBusy());

    ASSERT_NO_THROW(this->serverGroup->start());

    ASSERT_TRUE(this->serverGroup->isBusy());

    ASSERT_NO_THROW(this->serverGroup->stop());

    usleep(timeout*1000*2);
    //This should remove the dead process
    this->serverGroup->evaluateServerGroup();

    //The group should be not busy now.
    ASSERT_FALSE(this->serverGroup->isBusy());
}

TEST_F(ServerGroupTest, start)
{
    //Start the server and check that it is busy

    ASSERT_NO_THROW(this->serverGroup->start());

    ASSERT_TRUE(this->serverGroup->isBusy());

    //TODO:
    //  ASSERT_NO_THROW(this->serverGroup->getAvailableServer(dbName));
}

TEST_F(ServerGroupTest, isStopped)
{
    ASSERT_TRUE(this->serverGroup->isStopped());

    ASSERT_NO_THROW(this->serverGroup->start());

    ASSERT_FALSE(this->serverGroup->isStopped());
}

TEST_F(ServerGroupTest, stop)
{}

TEST_F(ServerGroupTest, registerServer)
{
	 ASSERT_NO_THROW(this->serverGroup->start());

	 ASSERT_FALSE(this->serverGroup->registerServer("test"));
}

TEST_F(ServerGroupTest, evaluateServerGroup)
{}

TEST_F(ServerGroupTest, hasAvailableServer)
{}

TEST_F(ServerGroupTest, getAvailableServer)
{}
