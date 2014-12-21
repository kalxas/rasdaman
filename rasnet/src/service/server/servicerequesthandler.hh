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

/* SOURCE: ServiceRequestHandler.hh
 *
 * MODULE:
 * CLASS:   ServiceRequestHandler
 *
 * COMMENTS: The ServiceRequestHandler is used to handle an incoming ServiceRequest message.
 * It contains a list of services which can be called.
 *
 *
 */
#ifndef RASNET_SRC_SERVICE_SERVER_SERVICEREQUESTHANDLER_HH_
#define RASNET_SRC_SERVICE_SERVER_SERVICEREQUESTHANDLER_HH_

#include <map>
#include <string>

#include <google/protobuf/service.h>
#include <google/protobuf/stubs/common.h>
#include <boost/smart_ptr.hpp>
#include <boost/thread.hpp>

#include "../../../src/messages/base.pb.h"
#include "../../../../common/src/zeromq/zmq.hh"

namespace rasnet
{

class ServiceRequestHandler
{
public:
    ServiceRequestHandler(zmq::context_t& context, std::string& bridgeAddress);

    virtual ~ServiceRequestHandler();

    /**
     * Add the service to the list of available services.
     * If the service is already in the list, an exception will be thrown.
     * @param service
     * @param ownership true if the memory of the service object must be managed by the handler,
     * false otherwise
     */
    void addService(boost::shared_ptr<google::protobuf::Service> service);

    /**
     * Check if the handler can process the message contained in the BaseMessage
     * @param message
     * @return true if the message can be processed, false otherwise
     */
    virtual bool canHandle(const base::BaseMessage& message);

    /**
     * Process the ServiceRequest message contained in the BaseMessage.
     * Call the appropriate method and return a result to the requesting client.
     * @param message BaseMessage containing a ServiceRequest
     * @param peer_id ID of the peer that requested the service
     * and which can be used to reply to the service call.
     */
    virtual void handle(const base::BaseMessage& message,
                        const std::string& peerId);

private:
    zmq::context_t& context; /*! Context of the socket where replies can be sent */
    std::string bridgeAddress;/*! Address of the socket where replies can be sent*/
    std::string messageType;
    google::protobuf::Closure* doNothingClosure;
    std::map<std::string, boost::shared_ptr<google::protobuf::Service> > serviceMap;/*! Map between the service's fully qualified name and the service */
    std::map<std::string, const ::google::protobuf::MethodDescriptor*> serviceMethodMap;/*! Map between the fully qualified name of the method and the method descriptor*/
    boost::shared_mutex serviceMutex;

    /**
     * Create a ZeroMQ PAIR socket and connect it to the bridge_addr
     * and then send the message to that address
     * @param mess Message that we want to put into a BaseMessage and send to the bridge
     * @param peer_id ID of the peer that sent the message that is being handled.
     */
    void sendMessageToBridge(google::protobuf::Message& mess,
                             const std::string& peerId);
};

} /* namespace rasnet */

#endif /* RASNET_SERVICE_SERVER_SERVICEREQUESTHANDLER_HH_ */
