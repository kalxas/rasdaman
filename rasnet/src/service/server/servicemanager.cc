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
#include <stdexcept>

#include "../../../../common/src/uuid/uuid.hh"
#include "../../../../common/src/thread/fixedthreadpool.hh"
#include "../../../../common/src/logging/easylogging++.hh"

#include "../../../src/service/server/servicemanager.hh"
#include "../../../src/service/server/servicemanagerconfig.hh"
#include "../../../src/messages/service.pb.h"
#include "../../../src/messages/base.pb.h"
#include "../../../src/messages/communication.pb.h"
#include "../../../src/sockets/server/serversocket.hh"
#include "../../../src/util/proto/protozmq.hh"
#include "../../../src/util/string/stringutils.hh"


namespace rasnet
{
using google::protobuf::Service;
using google::protobuf::MethodDescriptor;
using std::string;
using std::map;
using std::runtime_error;
using std::pair;
using rasnet::ServiceRequest;
using rasnet::ServiceResponse;
using base::BaseMessage;
using common::UUID;
using common::ThreadPool;
using common::FixedThreadPool;
using boost::thread;
using boost::scoped_ptr;

ServiceManager::ServiceManager(boost::int32_t ioThreads, boost::uint32_t cpuThreads) :
        context(ioThreads)
{
    if(cpuThreads<1 || ioThreads<1)
    {
        throw runtime_error("The service manager needs at least one CPU thread for the clients");
    }

    //TODO-AT: Add (void) in front of the members instead of initializing
    this->runnning = false;
    this->cpuThreads = cpuThreads;
    this->serverPort = 7001; //Default value
    this->serverHost = "tcp://*";
    this->bridgeAddr = "inproc://" + UUID::generateUUID();
    this->requestHandler.reset(new ServiceRequestHandler(context, bridgeAddr));

}
ServiceManager::~ServiceManager()
{
    if (this->runnning == true && this->serviceThread)
    {
        this->runnning = false;
        this->serviceThread->join();
    }
}

void ServiceManager::addService(boost::shared_ptr<google::protobuf::Service> service)
{
    if (this->runnning)
    {
        throw runtime_error("Server is already running.");
    }
    else
    {
        requestHandler->addService(service);
    }
}

void ServiceManager::serve(std::string host, boost::uint32_t port)
{
    if (this->runnning)
    {
        throw runtime_error("Service is already running.");
    }
    else
    {
        this->serverHost = host;
        this->serverPort = port;
        this->runnning = true;
        this->serviceThread.reset(new thread(boost::bind(&ServiceManager::run, this)));
    }
}

void ServiceManager::run()
{
    string serviceResponseType =
        ServiceResponse::default_instance().GetTypeName();

    BaseMessage message;
    string peerId;

    try
    {
        ServerSocketConfig config;
        scoped_ptr<ThreadPool> threadPool(new FixedThreadPool(this->cpuThreads));
        string endpoint=this->serverHost + ":"+ stringutils::intToString(this->serverPort);
        ServerSocket server(endpoint, config);

        zmq::socket_t bridge(this->context, ZMQ_DEALER);
        bridge.bind(this->bridgeAddr.c_str());

        zmq::pollitem_t items[] = { { bridge, 0, ZMQ_POLLIN, 0 } };

        while(this->runnning)
        {
            while (zmq::poll(items, 1, 0) >  0)
            {

                BaseMessage forwardMessage;
                std::string forwardPeerId;
                ServiceResponse response;

                ProtoZmq::zmqReceiveFromPeer(bridge, forwardPeerId,
                                             forwardMessage);

                if (forwardMessage.type() == serviceResponseType)
                {
                    response.ParseFromString(forwardMessage.data());
                    server.send(forwardPeerId, response);
                }
                else
                {
                    throw runtime_error("An unexpected message has arrived at the internal ServiceManager socket.");
                }
            }

            //TODO: Configure the polling time
            if (server.pollIn(1))
            {
                server.receive(peerId, message);
                if (this->requestHandler->canHandle(message))
                {
                    threadPool->submit(boost::bind(&ServiceRequestHandler::handle, this->requestHandler.get(), message, peerId));
                }
            }
        }
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
    }
    catch(...)
    {
        LERROR<<"Unknown exception was encounted. The ServiceManager is shutting down.";
    }

}

}
