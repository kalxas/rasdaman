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
#include <string>
#include <utility>

#include "../../../../common/src/logging/easylogging++.hh"

#include "../../../src/service/server/servicerequesthandler.hh"
#include "../../../src/messages/service.pb.h"
#include "../../../src/service/server/servercontroller.hh"
#include "../../../src/util/proto/protozmq.hh"

namespace rasnet
{
using std::runtime_error;
using rasnet::ServiceRequest;
using rasnet::ServiceResponse;
using std::string;
using std::pair;
using std::map;
using google::protobuf::Service;
using google::protobuf::MethodDescriptor;
using google::protobuf::Closure;
using google::protobuf::Message;
using google::protobuf::DoNothing;
using google::protobuf::NewCallback;
using boost::unique_lock;
using boost::shared_lock;
using boost::shared_mutex;
using boost::shared_ptr;


ServiceRequestHandler::ServiceRequestHandler(zmq::context_t& context,
        std::string& bridgeAddress) : context(context),bridgeAddress(bridgeAddress)
{
    this->messageType=ServiceRequest::default_instance().GetTypeName();
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
            string methodName = ProtoZmq::getServiceMethodName(method);
            this->serviceMethodMap[methodName] =method;
        }
    }
    else
    {
        throw runtime_error("Service already exists");
    }
}

bool ServiceRequestHandler::canHandle(const base::BaseMessage& message)
{
    return (message.type() == this->messageType);
}

void ServiceRequestHandler::handle(const base::BaseMessage& message,
                                   const std::string& peerId)
{
    ServerController controller;
    ServiceResponse response;
    shared_ptr<Service> service;

    if(this->canHandle(message))
    {
        ServiceRequest request;
        if(request.ParseFromString(message.data()))
        {
            map<string, const MethodDescriptor*>::iterator it;
            //Set the it for the response
            response.set_id(request.id());

            shared_lock<shared_mutex> lock(this->serviceMutex);

            it = this->serviceMethodMap.find(request.method_name());
            if(it!= this->serviceMethodMap.end())
            {
                map<string, shared_ptr<Service> >::iterator serviceIterator;
                const MethodDescriptor* methodDesc = it->second;
                serviceIterator=this->serviceMap.find(methodDesc->service()->full_name());

                if(serviceIterator!=this->serviceMap.end())
                {
                    service=serviceIterator->second;
                    //From this point on, we don't need exclusive access to the
                    //private datastructures so unlock the mutex

                    lock.unlock();

                    //Create objects to hold the request and response data
                    Message* replyData = service->GetResponsePrototype(
                                             methodDesc).New();
                    Message* requestData = service->GetRequestPrototype(
                                               methodDesc).New();

                    //Deserialize the input value
                    if (requestData->ParseFromString(request.input_value()))
                    {
                        //call the method
                        try
                        {
                            service->CallMethod(methodDesc, &controller,
                                                requestData, replyData, this->doNothingClosure);
                        }
                        catch(std::exception& ex)
                        {
                            LERROR<<ex.what();
                            response.set_error(ex.what());
                        }
                        catch(...)
                        {
                            LERROR<<"Service call to method" <<service->GetDescriptor()->full_name()<<" with input data" << requestData->SerializeAsString()<<" failed for unknown reason";
                            response.set_error("Function call failed on server");
                        }

                        if (controller.Failed())
                        {
                            response.set_error(controller.ErrorText());
                        }
                        else
                        {
                            response.set_output_value(
                                replyData->SerializeAsString());
                        }
                    }
                    else
                    {
                        LERROR<<"Invalid input message: "+message.DebugString();
                        response.set_error("Invalid input message: "+message.DebugString());
                    }

                    delete requestData;
                    delete replyData;
                }
                else
                {
                    response.set_error(
                        "This service is not offered by the server.");
                }
            }
            else
            {
                response.set_error(
                    "This method "+request.method_name() +" is not offered by the server.");
                LERROR<<"Call to inexisting method"<<request.method_name();
            }
        }
        else
        {
            //TODO:Document the fact that 0 is a special message ID.
            response.set_id(0);
            response.set_error("Could not parse message from client"+request.DebugString());
            LERROR<<"Could not parse message from client"<<request.DebugString();
        }

        //Finally, send the response
        this->sendMessageToBridge(response, peerId);
    }
    else
    { //Programmer error
        throw runtime_error("The handler cannot process this type of message:"+message.GetTypeName());
    }
}

void ServiceRequestHandler::sendMessageToBridge(
    google::protobuf::Message& mess, const std::string& peerId)
{
    zmq::socket_t peer(context, ZMQ_DEALER);
    peer.connect(bridgeAddress.c_str());
    ProtoZmq::zmqSendToPeer(peer, mess,peerId);
}
}
