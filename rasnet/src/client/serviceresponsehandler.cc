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
#include "../../../common/src/logging/easylogging++.hh"

#include "../common/zmqutil.hh"
#include "../exception/unsupportedmessageexception.hh"
#include "../messages/communication.pb.h"
#include "../messages/internal.pb.h"

#include "serviceresponsehandler.hh"

namespace rasnet
{
using boost::mutex;
using boost::shared_ptr;

using rasnet::internal::ServiceResponseAvailable;

using std::map;
using std::make_pair;
using std::pair;
using std::string;

using zmq::message_t;
using zmq::socket_t;


ServiceResponseHandler::ServiceResponseHandler(zmq::socket_t &bridge,
                                               boost::shared_ptr<std::map<std::string, std::pair<const google::protobuf::Message *, std::string> > > serviceRequests,
                                               boost::shared_ptr<std::map<std::string, std::pair<bool, boost::shared_ptr<zmq::message_t> > > > serviceResponses,
                                               boost::shared_ptr<boost::mutex> serviceMutex):
    bridge(bridge)
{
    this->serviceRequests = serviceRequests;
    this->serviceResponses = serviceResponses;
    this->serviceMutex = serviceMutex;
}

ServiceResponseHandler::~ServiceResponseHandler()
{

}

bool ServiceResponseHandler::canHandle(const std::vector<boost::shared_ptr<zmq::message_t> > &message)
{
    //Accepted message format:
    //Type | ID | error | data
    MessageType messageType;

    if(message.size() == 4
            && messageType.ParseFromArray(message[0]->data(), message[0]->size())
            && messageType.type() == MessageType::SERVICE_RESPONSE)
    {
        return true;
    }
    else
    {
        return false;
    }
}

void ServiceResponseHandler::handle(const std::vector<boost::shared_ptr<zmq::message_t> > &message)
{
    //Accepted message format:
    //Type | ID | error | data
    if(this->canHandle(message))
    {
        //1. Extract the data and check if the data is valid
        std::string peerId = ZmqUtil::messageToString(*message[1]);
        ServiceCallStatus status;
        if(!status.ParseFromArray(message[2]->data(), message[2]->size()))
        {
            LERROR<<"Failed to parse service response status component.";
            return;
        }

        //2. Check if the requests exists
        boost::unique_lock<boost::mutex> serviceLock(*serviceMutex);

        std::map<std::string, std::pair<const google::protobuf::Message*, std::string> > ::iterator it;
        it = this->serviceRequests->find(peerId);
        if(it==this->serviceRequests->end())
        {
            LERROR<<"Received service response with invalid ID.";
            return;
        }

        //3. Add the service response to the list of pending responses
        this->serviceResponses->insert(make_pair(peerId,make_pair(status.success(), message[3])));

        //4. Notify the other thread that the response is ready.
        ServiceResponseAvailable responseAvailable;
        ZmqUtil::sendToPeer(bridge,peerId,responseAvailable);

        //5. Remove the request from the list of pending requests
        this->serviceRequests->erase(peerId);
    }
    else
    {
        throw UnsupportedMessageException();
    }
}

}
