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

#include <stdexcept>

#include <boost/thread.hpp>

#include "../../../common/src/uuid/uuid.hh"
#include "../../../common/src/logging/easylogging++.hh"
#include "../../../common/src/thread/fixedthreadpool.hh"

#include "../common/zmqutil.hh"
#include "../common/util.hh"
#include "../common/constants.hh"

#include "../messages/internal.pb.h"

#include "serverpinghandler.hh"
#include "serverponghandler.hh"
#include "connectrequesthandler.hh"
#include "clientpool.hh"
#include "servicerequesthandler.hh"
#include "servicemanager.hh"


namespace rasnet
{
using boost::shared_ptr;
using boost::thread;
using boost::scoped_ptr;

using common::UUID;
using common::FixedThreadPool;
using common::ThreadPool;

using internal::InternalDisconnectReply;
using internal::InternalDisconnectRequest;

using std::runtime_error;

ServiceManager::ServiceManager(const ServiceManagerConfig & config):
    config(config),
    context(config.getIoThreadsNo(), config.getMaxOpenSockets())
{
    if(config.getIoThreadsNo()<1 || config.getCpuThreadsNo()<1)
    {
        throw runtime_error("The service manager needs at least one CPU thread and one IO thread.");
    }

    this->linger = 0;
    this->runnning = false;

    std::string identity = UUID::generateUUID();
    this->controlAddr = ZmqUtil::toInprocAddress("control-" + identity);
    this->bridgeAddr = ZmqUtil::toInprocAddress("bridge-" + identity);

    this->controlSocket.reset(new zmq::socket_t(this->context, ZMQ_PAIR));
    this->controlSocket->setsockopt(ZMQ_LINGER, &linger, sizeof(linger));
    this->controlSocket->bind(this->controlAddr.c_str());

    this->requestHandler.reset(new ServiceRequestHandler(context, this->bridgeAddr));
}

ServiceManager::~ServiceManager()
{
    if (this->runnning && this->serviceThread)
    {
        this->stopWorkerThread();

        this->serviceThread->join();

        // https://github.com/zeromq/libzmq/issues/949
        // this->controlSocket->unbind(this->controlAddr.c_str());
        this->controlSocket->close();
    }
}

void ServiceManager::addService(boost::shared_ptr<google::protobuf::Service> service)
{
    requestHandler->addService(service);
}

void ServiceManager::serve(const std::string& endpoint)
{
    if (this->runnning)
    {
        throw runtime_error("ServiceManager is already running.");
    }
    else
    {
        this->runnning = true;
        this->endpointAddr = endpoint;
        this->serviceThread.reset(new thread(boost::bind(&ServiceManager::run, this, boost::ref(this->workerThreadExceptionPtr))));

        //If the thread was joined and an exception was throw, rethrow
        if(this->serviceThread->timed_join(boost::posix_time::milliseconds(INTER_THREAD_COMMUNICATION_TIMEOUT))
                && this->workerThreadExceptionPtr)
        {
            //The method will return early only if the ZMQ sockets throw an exception
            //and in that case we rethrow the exception
            boost::rethrow_exception(this->workerThreadExceptionPtr);
        }
    }
}

void ServiceManager::stopWorkerThread()
{
    LDEBUG<<"Sending internal disconnect message";

    InternalDisconnectRequest request = InternalDisconnectRequest::default_instance();
    boost::shared_ptr<BaseMessage> reply;

    if(!ZmqUtil::isSocketWritable(*controlSocket, INTER_THREAD_COMMUNICATION_TIMEOUT))
    {
        LERROR<<"Unable to write to control socket.";
    }
    else if(!ZmqUtil::send(*this->controlSocket, request))
    {
        LERROR<<"Inter-thread messaging error.";
    }
    else
    {
        //Will fail if no message has arrived in the predefined time frame
        if(!(ZmqUtil::isSocketReadable(*controlSocket, this->config.getAliveTimeout())) ||
                !ZmqUtil::receive(*(this->controlSocket), reply))
        {
            LERROR<<"Failed to receive internal disconnect reply.";
        }
        else if(reply->type()!=Util::getMessageType(InternalDisconnectReply::default_instance()))
        {
            LERROR<<"Received invalid internal disconnect reply.";
        }
    }

    this->runnning = false;
}

void ServiceManager::run(boost::exception_ptr &exceptionPtr)
{
    try
    {
        zmq::socket_t client(this->context, ZMQ_ROUTER);
        client.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
        client.bind(this->endpointAddr.c_str());

        zmq::socket_t bridge(this->context, ZMQ_ROUTER);
        bridge.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
        bridge.bind(this->bridgeAddr.c_str());

        zmq::socket_t control(this->context, ZMQ_PAIR);
        control.setsockopt(ZMQ_LINGER, &linger, sizeof (linger));
        control.connect(this->controlAddr.c_str());

        zmq::pollitem_t items[] =
        {
            { client, 0, ZMQ_POLLIN, 0 },
            { bridge, 0, ZMQ_POLLIN, 0 },
            { control, 0, ZMQ_POLLIN, 0}
        };

        boost::shared_ptr<ClientPool> clientPool(new ClientPool());
        ServerPingHandler pingHandler(clientPool, client);
        ServerPongHandler pongHandler(clientPool);
        ConnectRequestHandler connectRequestHandler(client, clientPool, config.getAliveRetryNo(), config.getAliveTimeout());
        scoped_ptr<ThreadPool> threadPool(new FixedThreadPool(config.getCpuThreadsNo()));

        bool keepRunning = true;

        while(keepRunning)
        {
            zmq::poll(items, 3, clientPool->getMinimumPollPeriod());

            if (items[0].revents & ZMQ_POLLIN)
            {
                std::vector<shared_ptr<zmq::message_t> > message;
                std::string peerId;

                ZmqUtil::receiveCompositeMessageFromPeer(client, peerId, message);

                if(pingHandler.canHandle(message))
                {
                    pingHandler.handle(message, peerId);
                }
                else if(pongHandler.canHandle(message))
                {
                    pongHandler.handle(message, peerId);
                }
                else if(connectRequestHandler.canHandle(message))
                {
                    connectRequestHandler.handle(message, peerId);
                }
                else if(requestHandler->canHandle(message))
                {
                    threadPool->submit(boost::bind(&ServiceRequestHandler::handle, this->requestHandler.get(), message, peerId));
                }
                else
                {
                    LERROR<<"Received invalid message from client with peer ID:"<<peerId;
                }
            }
            else
            {
                clientPool->pingAllClients(client);
                clientPool->removeDeadClients();
            }

            if (items[1].revents & ZMQ_POLLIN)
            {
                internal::ServiceResponseAvailable responseAvailable;
                boost::shared_ptr<BaseMessage> bridgeMessage;
                std::string peerId;
                if(!ZmqUtil::receiveFromPeer(bridge, bridgeMessage, peerId))
                {
                    LERROR<<"Received invalid message from bridge socket.";
                }
                else if(!responseAvailable.ParseFromString(bridgeMessage->data()))
                {
                    LERROR<<"Invalid message from bridge socket.";
                }
                else
                {
                    try
                    {
                        boost::tuple<std::string,std::string, bool, std::string, ::google::protobuf::uint8*, int>  result;
                        //<peerId, callId, success, errorMessage, serialied response, serialized response size>
                        result = requestHandler->getResponse();
                        std::string destinationId = result.get<0>();
                        std::string callId = result.get<1>();
                        bool success = result.get<2>();

                        if(success)
                        {
                            if(!ZmqUtil::sendServiceResponseSuccess(client, destinationId, callId, result.get<4>(), result.get<5>()))
                            {
                                LERROR<<"Failed to send service response message.";
                            }
                        }
                        else
                        {
                            std::string errorMessage = result.get<3>();
                            if(!ZmqUtil::sendServiceResponseFailure(client, destinationId, callId, errorMessage))
                            {
                                LERROR<<"Failed to send service response message.";
                            }

                        }
                    }
                    catch(std::exception& ex)
                    {
                        LERROR<<ex.what();
                    }
                }
            }

            if (items[2].revents & ZMQ_POLLIN)
            {
                boost::shared_ptr<BaseMessage> controlMessage;
                if(!ZmqUtil::receive(control, controlMessage))
                {
                    LERROR<<"Received invalid message from control socket.";
                }
                else if(controlMessage->type()!=Util::getMessageType(InternalDisconnectRequest::default_instance()))
                {
                    LERROR<<"Received message of invalid type from control socket."<<controlMessage->DebugString();
                }
                else
                {
                    keepRunning = false;

                    InternalDisconnectReply disconnectReply;
                    ZmqUtil::send(control, disconnectReply);
                }
            }

        }

        //bridge.unbind(this->bridgeAddr.c_str());
        bridge.close();
        //client.unbind(this->endpointAddr.c_str());
        client.close();
    }
    catch(...)
    {
        LERROR<<"Service manager worker method failed.";
        exceptionPtr = boost::current_exception();
    }
}

}
