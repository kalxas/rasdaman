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

#include <iostream>
#include <cstdio>
#include <ctime>

#include <google/protobuf/service.h>
#include <google/protobuf/stubs/common.h>
#include <boost/smart_ptr.hpp>
#include <boost/timer/timer.hpp>

#include "../../../common/src/unittest/gtest.h"
#include "../../../common/src/logging/easylogging++.hh"
#include "../../../common/src/thread/fixedthreadpool.hh"
#include "../../src/messages/test_mess.pb.h"
#include "../../src/service/client/channel.hh"
#include "../../src/service/client/clientcontroller.hh"
#include "../../src/service/server/servicemanager.hh"

using namespace google::protobuf;
using namespace rnp_test;
using namespace rasnet;
using namespace std;
using namespace boost;
using namespace common;

class SearchServiceImpl: public rnp_test::SearchService
{
public:
    bool destructorCalled;

    SearchServiceImpl()
    {
        destructorCalled = false;

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
        destructorCalled = true;
    }

};


class BigDataSearchServiceImpl: public rnp_test::SearchService
{
public:
    BigDataSearchServiceImpl()
    {
    }
    virtual void Search(::google::protobuf::RpcController* controller,
                        const RequestType* request, ResponseType* response,
                        ::google::protobuf::Closure* done)
    {
        response->set_repl(request->req());
    }
    virtual ~BigDataSearchServiceImpl()
    {}

}
;

class ClientTask
{
public:
    ClientTask(SearchService& client) :
            client_(client)
    {}
    void run()
    {
        unique_lock<mutex> lock(ClientTask::mut);
        counter++;
        lock.unlock();
        Closure* doNothing = NewPermanentCallback(&DoNothing);
        //prepare the client controller
        ClientController controller;
        ResponseType response;
        RequestType request;
        request.set_req("Helloooo");

        client_.Search(&controller, &request, &response, doNothing);
        ASSERT_FALSE(controller.Failed());
        ASSERT_EQ("Bye", response.repl());

        delete doNothing;
    }
    static int counter;
    static boost::mutex mut;
private:
    SearchService& client_;
};


class BigDataClientTask
{
public:
    BigDataClientTask(SearchService& client) :
            client_(client)
    {

        //TODO-GM: increase to 50 Mb
        request.mutable_req()->assign(std::string(5, 'a'));

        doNothing = NewPermanentCallback(&DoNothing);
    }

    ~BigDataClientTask()
    {
    }

    void run()
    {

        //prepare the client controller
        ClientController controller;
        ResponseType response;

        client_.Search(&controller, &request, &response, doNothing);
    }
    RequestType request;
private:
    SearchService& client_;
    Closure* doNothing ;

};


int ClientTask::counter = 0;
boost::mutex ClientTask::mut;

TEST(ServiceTest, clientCall)
{
    ClientController controller;
    Closure* doNothing = NewPermanentCallback(&DoNothing);
    boost::shared_ptr<SearchService> service (new SearchServiceImpl());

    //Prepare the service manager
    boost::scoped_ptr<ServiceManager> manager(new ServiceManager());
    manager->addService(service);
    manager->serve("tcp://*", 7002);

    //prepare the client controller

    Channel* channel = new Channel("tcp://localhost", 7002);
    boost::scoped_ptr<SearchService> client(new SearchService_Stub(channel));
    ResponseType response;
    RequestType request;

    request.set_req("Helloooo");

    client->Search(&controller, &request, &response, doNothing);
    ASSERT_FALSE(controller.Failed());
    ASSERT_EQ("Bye", response.repl());

    delete channel;
    channel=NULL;
    delete doNothing;
    doNothing=NULL;

}


TEST(ServiceTest, serialClients)
{
    boost::timer::cpu_timer timer;
    shared_ptr<SearchService> service (new SearchServiceImpl());
    int call_no = 1000;

    //Prepare the service manager
    boost::scoped_ptr<ServiceManager> manager(new ServiceManager(2,2));
    manager->addService(service);
    manager->serve("tcp://*", 7002);

    Channel* channel = new Channel("tcp://localhost", 7002);
    boost::scoped_ptr<SearchService> client(new SearchService_Stub(channel));
    Closure* doNothing = NewPermanentCallback(&DoNothing);
    //prepare the client controller
    ClientController controller;
    ResponseType response;
    RequestType request;

    request.set_req("Helloooo");

    PINFO<<"Started serial client service test performance:";

    timer.start();
    for (int i = 0; i < call_no; i++)
    {
        client->Search(&controller, &request, &response, doNothing);
    }

    timer.stop();

    PINFO<<call_no<<" service calls took "<<timer.format(3)<<" milliseconds";

    delete channel;
    channel=NULL;
    delete doNothing;
    doNothing=NULL;
}


TEST(ServiceTest, concurrentClients)
{
    boost::timer::cpu_timer timer;
    shared_ptr<SearchService> service (new SearchServiceImpl());
    //TODO-GM: chaange to 10000
    int call_no = 10;

    FixedThreadPool pool(50);
    //Prepare the service manager
    boost::scoped_ptr<ServiceManager> manager(new ServiceManager(2, 8));
    manager->addService(service);
    manager->serve("tcp://*", 7002);

    Channel channel("tcp://localhost", 7002);

    SearchService_Stub client(&channel);

    PINFO<<"Starting concurrent service call performance test.";
    timer.start();
    for (int i = 0; i < call_no; i++)
    {
        ClientTask task(client);
        pool.submit(boost::bind(&ClientTask::run, task));
    }
    pool.awaitTermination();
    timer.stop();

    EXPECT_EQ(call_no, ClientTask::counter);
    PINFO<<"Counter "<<ClientTask::counter;
    PINFO<<call_no<<" service calls took "<<timer.format(3)<<" milliseconds";

}

TEST(ServiceTest, bigDataSerialTest)
{
    boost::timer::cpu_timer timer;
    ResponseType response;
    RequestType request;
    ClientController controller;
    Closure* doNothing = NewPermanentCallback(&DoNothing);
    int call_no = 100;
    //TODO-GM: increase to 50 Mb
    request.mutable_req()->assign(std::string(5, 'a'));

    //Prepare the service manager
    shared_ptr<SearchService> service (new BigDataSearchServiceImpl());

    boost::scoped_ptr<ServiceManager> manager(new ServiceManager(4, 4));
    manager->addService(service);
    manager->serve("tcp://*", 7002);

    Channel channel("tcp://localhost", 7002);

    boost::scoped_ptr<SearchService> client(new SearchService_Stub(&channel));

    PINFO<<"Starting serial big data service call performance test.";
    timer.start();
    for (int i = 0; i < call_no; i++)
    {
        client->Search(&controller, &request, &response, doNothing);
    }
    timer.stop();

    PINFO<<call_no<<" service calls took "<<timer.format(3)<<" milliseconds";

    delete doNothing;
    doNothing=NULL;

}

TEST(ServiceTest, bigDataConcurrentTest)
{
    boost::timer::cpu_timer timer;
    shared_ptr<SearchService> service (new SearchServiceImpl());
    int call_no = 100;

    FixedThreadPool pool(50);
    //Prepare the service manager
    boost::scoped_ptr<ServiceManager> manager(new ServiceManager(4, 4));
    manager->addService(service);
    manager->serve("tcp://*", 7002);

    Channel channel("tcp://localhost", 7002);

    SearchService_Stub client(&channel);

    PINFO<<"Starting concurrent big data service performance test.";
    timer.start();
    for (int i = 0; i < call_no; i++)
    {
        BigDataClientTask task(client);
        pool.submit(boost::bind(&BigDataClientTask::run, task));
    }
    pool.awaitTermination();
    timer.stop();

    PINFO<<call_no<<" service calls took "<<timer.format(3)<<" milliseconds";

}
