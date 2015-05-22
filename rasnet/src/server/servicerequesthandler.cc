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

#include <google/protobuf/stubs/common.h>
#include <google/protobuf/service.h>

#include "../../../common/src/logging/easylogging++.hh"

#include "../exception/duplicateserviceexception.hh"
#include "../exception/unsupportedmessageexception.hh"
#include "../exception/exceptionmessages.hh"
#include "../messages/communication.pb.h"
#include "../messages/internal.pb.h"
#include "../common/util.hh"
#include "../common/zmqutil.hh"

#include "servercontroller.hh"

#include "servicerequesthandler.hh"

namespace rasnet
{
using std::runtime_error;
using std::string;
using std::pair;
using std::map;
using google::protobuf::Service;
using google::protobuf::MethodDescriptor;
using google::protobuf::Closure;
using google::protobuf::Message;
using google::protobuf::DoNothing;
using google::protobuf::NewCallback;

using boost::mutex;
using boost::unique_lock;
using boost::shared_lock;
using boost::shared_mutex;
using boost::shared_ptr;


ServiceRequestHandler::ServiceRequestHandler(zmq::context_t& context, std::string& bridgeAddress) :
    context(context), bridgeAddress(bridgeAddress)
{
    this->doNothingClosure = google::protobuf::NewPermanentCallback(&DoNothing);
}

ServiceRequestHandler::~ServiceRequestHandler()
{
    delete this->doNothingClosure;

    this->serviceMethodMap.clear();
    this->serviceMap.clear();
}


void ServiceRequestHandler::addService(boost::shared_ptr<google::protobuf::Service> service)
{
    unique_lock<shared_mutex> lock(this->serviceMutex);

    //Get the full name of the service and use it as a key
    string serviceName = service->GetDescriptor()->full_name();
    pair<map<string, shared_ptr<google::protobuf::Service> >::iterator, bool> insertRes =
        this->serviceMap.insert(pair<string, shared_ptr<google::protobuf::Service> >(serviceName, service));

    if(insertRes.second==true)
    {
        //mark the ownership of the
        for (int i = 0; i < service->GetDescriptor()->method_count(); i++)
        {
            const MethodDescriptor* method =
                service->GetDescriptor()->method(i);
            //Use the full name of the method as a key
            string methodName = Util::getMethodName(method);
            this->serviceMethodMap[methodName] =method;
        }
    }
    else
    {
        throw DuplicateServiceException();
    }
}

bool ServiceRequestHandler::canHandle(const std::vector<boost::shared_ptr<zmq::message_t> > &message)
{
    //Message format :
    // | MessageType = SERVICE_REQUEST | Call ID | Method Name | Serialized Input Data|
    MessageType type;
    if(message.size()==4
            && type.ParseFromArray(message[0]->data(), message[0]->size())
            && type.type() == MessageType::SERVICE_REQUEST)
    {
        return true;
    }
    else
    {
        return false;
    }
}

void ServiceRequestHandler::handle(const std::vector<boost::shared_ptr<zmq::message_t> > &message, const std::string &peerId)
{
    if(this->canHandle(message))
    {
        //| MessageType = SERVICE_REQUEST | Call ID | Method Name | Serialized Input Data|
        std::string callId = ZmqUtil::messageToString(*message[1]);
        std::string methodName = ZmqUtil::messageToString(*message[2]);
        Message* requestData = NULL;
        Message* replyData = NULL;

        ServerController controller;
        shared_ptr<Service> service;

        shared_lock<shared_mutex> lock(this->serviceMutex);
        map<string, const MethodDescriptor*>::iterator it;
        it = this->serviceMethodMap.find(methodName);

        if(it!=this->serviceMethodMap.end())
        {
            map<string, shared_ptr<Service> >::iterator serviceIterator;
            const MethodDescriptor* methodDesc = it->second;
            serviceIterator = this->serviceMap.find(methodDesc->service()->full_name());

            if(serviceIterator != this->serviceMap.end())
            {
                service = serviceIterator->second;
                //From this point on, we don't need exclusive access to the
                //private datastructures so unlock the mutex

                lock.unlock();

                //Create objects to hold the request and response data
                requestData = service->GetRequestPrototype(
                                  methodDesc).New();
                replyData = service->GetResponsePrototype(
                                methodDesc).New();

                if(requestData->ParseFromArray(message[3]->data(), message[3]->size()))
                {
                    //call the method
                    try
                    {
                        service->CallMethod(methodDesc, &controller,
                                            requestData, replyData, this->doNothingClosure);
                    }
                    catch(std::exception& ex)
                    {
                        //If the method call fails, set the error
                        LERROR<<ex.what();
                        controller.SetFailed(ex.what());
                    }
                    catch(...)
                    {
                        LERROR<<"Service call to method \"" <<service->GetDescriptor()->full_name()<<"\" with input data" << requestData->SerializeAsString()<<" failed for unknown reason";
                        controller.SetFailed(UNKOWN_SERVICE_CALL_FAILURE);
                    }
                }
                else
                {
                    //Could not parse input data.
                    LERROR<<INVALID_INPUT_DATA;
                    controller.SetFailed(INVALID_INPUT_DATA);
                }
            }
            else
            {
                //There is no service with this name
                LERROR<<INVALID_SERVICE_NAME<<"Service name:"<<methodDesc->service()->full_name();
                controller.SetFailed(INVALID_SERVICE_NAME);
            }
        }
        else
        {
            //There is no method with the given name
            LERROR<<INVALID_METHOD_NAME<<"Method name:"<<methodName;
            controller.SetFailed(INVALID_METHOD_NAME);
        }

        boost::tuple<std::string, std::string, bool, std::string, ::google::protobuf::uint8*, int> responseData;
        responseData.get<0>() = peerId;
        responseData.get<1>() = callId;

        if(controller.Failed())
        {
            responseData.get<2>() = false;
            responseData.get<3>() = controller.ErrorText();
            responseData.get<4>() = NULL;
            responseData.get<5>() = 0;
        }
        else
        {
            int messageSize = replyData->ByteSize();
            ::google::protobuf::uint8 *messageData =
                new ::google::protobuf::uint8[messageSize];
            replyData->SerializeWithCachedSizesToArray(messageData);

            responseData.get<2>() = true;
            responseData.get<3>() = std::string();
            responseData.get<4>() = messageData;
            responseData.get<5>() = messageSize;
        }

        //Cleanup the data
        if(requestData!=NULL)
        {
            delete requestData;
        }

        if(replyData !=NULL)
        {
            delete replyData;
        }

        //Add the response in the map
        unique_lock<mutex> responseLock(this->responseMutex);
        this->responseQueue.push_back(responseData);
        responseLock.unlock();

        this->sendMessageToBridge(peerId);
    }
    else
    {
        throw UnsupportedMessageException();
    }
}

boost::tuple<std::string, std::string, bool, std::string, google::protobuf::uint8 *, int> ServiceRequestHandler::getResponse()
{
    unique_lock<mutex> responseLock(this->responseMutex);

    if(this->responseQueue.empty())
    {
        throw std::runtime_error("There is no response in the list.");
    }

    boost::tuple<std::string, std::string, bool, std::string, ::google::protobuf::uint8*, int> result;
    result = this->responseQueue.front();
    this->responseQueue.pop_front();

    return result;
}

void ServiceRequestHandler::sendMessageToBridge(const std::string& peerId)
{
    internal::ServiceResponseAvailable responseAvailable;

    zmq::socket_t peer(context, ZMQ_DEALER);
    peer.connect(bridgeAddress.c_str());

    ZmqUtil::send(peer, responseAvailable);

    peer.disconnect(bridgeAddress.c_str());
    peer.close();
}
}
