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

#ifndef RASNET_SRC_SERVER_SERVICEREQUESTHANDLER_HH_
#define RASNET_SRC_SERVER_SERVICEREQUESTHANDLER_HH_

#include <deque>
#include <string>

#include <google/protobuf/service.h>
#include <google/protobuf/stubs/common.h>

#include <boost/smart_ptr.hpp>
#include <boost/thread.hpp>
#include <boost/tuple/tuple.hpp>

#include "../../../common/src/zeromq/zmq.hh"

namespace rasnet
{
/**
 * @brief The ServiceRequestHandler class Handles service requests from
 * client Channels by calling the appropriate server implementation.
 */
class ServiceRequestHandler
{
public:
    /**
     * @brief ServiceRequestHandler
     * @param context ZMQ context used by the ServerManager that owns this object
     * for internal communication
     * @param bridgeAddress Address of the bridge used to forward service responses
     * from this thread, to the worker thread in the ServerManager to the client
     */
    ServiceRequestHandler(zmq::context_t& context, std::string& bridgeAddress);

    virtual ~ServiceRequestHandler();

    /**
     * Add the service to the list of available services.
     * @param service
     * @throws DuplicateServiceException If the service is already in the list, an exception will be thrown.
     */
    void addService(boost::shared_ptr<google::protobuf::Service> service);

    /**
     * Check if the given message represents a service request
     * @param message
     * @return true if the message can be processed, false otherwise
     */
    virtual bool canHandle(const std::vector<boost::shared_ptr<zmq::message_t> >&  message);

    /**
     * Process the message if it contains a service request, throw an exception otherwise.
     * The exception is thrown only in case of programmer error i.e. Calling the method
     * on a message which has not been verified with canHandle
     * Call the appropriate method and return a result to the requesting client.
     * @param message BaseMessage containing a ServiceRequest
     * @param peer_id ID of the peer that requested the service
     * and which can be used to reply to the service call.
     */
    virtual void handle(const std::vector<boost::shared_ptr<zmq::message_t> >&  message,
                        const std::string& peerId);

    /**
     * @brief getResponse Get the next available service response
     * @return <PeerID, CallID, Success, ErrorMessage, Serialized Response, Serialized Response Size>
     * Success is false if an error occured during the execution of the method, true otherwise
     * ErrorMessage is non-empty only if Success is false
     * Serializedresponse is non-null only if Success is true
     */
    boost::tuple<std::string,std::string, bool, std::string, ::google::protobuf::uint8*, int> getResponse();
private:
    zmq::context_t& context; /*! Context of the socket where replies can be sent */
    std::string bridgeAddress;/*! Address of the socket where replies can be sent*/
    google::protobuf::Closure* doNothingClosure;
    std::map<std::string, boost::shared_ptr<google::protobuf::Service> > serviceMap;/*! Map between the service's fully qualified name and the service */
    std::map<std::string, const ::google::protobuf::MethodDescriptor*> serviceMethodMap;/*! Map between the fully qualified name of the method and the method descriptor*/
    boost::shared_mutex serviceMutex;

    boost::mutex responseMutex;
    //<peerId, callId, success, errorMessage, serialied response, serialized response size>
    std::deque<boost::tuple<std::string,std::string, bool, std::string, ::google::protobuf::uint8*, int> > responseQueue;

    /**
     * Create a ZeroMQ DEALER socket and connect it to the bridge_addr
     * and then send the message to that address
     * @param peerId ID of the peer that sent the message that is being handled.
     */
    void sendMessageToBridge(const std::string& peerId);
};

} /* namespace rasnet */

#endif /* RASNET_SERVER_SERVICEREQUESTHANDLER_HH_ */
