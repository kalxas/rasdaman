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

#ifndef RASNET_SRC_CLIENT_SERVICERESPONSEHANDLER_HH
#define RASNET_SRC_CLIENT_SERVICERESPONSEHANDLER_HH

#include <string>
#include <map>

#include <google/protobuf/message.h>

#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>

#include "../../../common/src/zeromq/zmq.hh"

namespace rasnet
{
/**
 * @brief The ServiceResponseHandler class Handles ServiceResponse messages sent by the
 * server as a response to a service request
 */
class ServiceResponseHandler
{
public:
    /**
     * @brief ServiceResponseHandler
     * @param bridge Socket through which the thread that called CallMethod in the channel
     * can be notified that the result to the call is available
     * @param serviceRequests List of pending service requests
     * @param serviceResponses List that will contain the processed service response
     * @param serviceMutex Mutex used to synchronize access to the serviceRequests and serviceresponses lists
     */
    ServiceResponseHandler(zmq::socket_t& bridge,
                           boost::shared_ptr<std::map<std::string, std::pair<const google::protobuf::Message*, std::string> > > serviceRequests,
                           boost::shared_ptr<std::map<std::string, std::pair<bool, boost::shared_ptr<zmq::message_t> > > > serviceResponses,
                           boost::shared_ptr<boost::mutex> serviceMutex);

    virtual ~ServiceResponseHandler();

    /**
     * @brief canHandle Check if the message can be handled by this message handler
     * This handler accepts messages of the format:
     * | rasnet.MessageType | Call ID | ServiceCallStatus | Response data|
     * with MessageType.type() == MessageType::SERVICE_RESPONSE
     * Call ID is the ID of the call
     * @param message
     * @return TRUE if the messages can be handled, FALSE otherwise
     */
    bool canHandle(const std::vector<boost::shared_ptr<zmq::message_t> >& message);

    /**
     * @brief handle See inline documentation for a thorough explanation
     * @param message
     * @throws UnsupportedMessageException if an invalid message is passed in.
     * i.e. one for which canHandle returns false
     */
    void handle(const std::vector<boost::shared_ptr<zmq::message_t> >&  message);

private:
    boost::shared_ptr<std::map<std::string, std::pair<const google::protobuf::Message*, std::string> > > serviceRequests;
    boost::shared_ptr<std::map<std::string, std::pair<bool,boost::shared_ptr<zmq::message_t> > > > serviceResponses;
    boost::shared_ptr<boost::mutex> serviceMutex;
    zmq::socket_t& bridge;
};

}
#endif // RASNET_SRC_CLIENT_SERVICERESPONSEHANDLER_HH
