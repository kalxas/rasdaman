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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009,2010,2011,2012,2013,2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#include <stdexcept>

#include "../../common/src/logging/easylogging++.hh"

#include "controlrasmgrrasnet.hh"

namespace rascontrol
{
using std::runtime_error;

using rasnet::Channel;
using rasnet::service::RasMgrRasCtrlService_Stub;
using rasnet::service::RasCtrlRequest;
using rasnet::service::RasCtrlResponse;

using ::google::protobuf::NewPermanentCallback;
using ::google::protobuf::DoNothing;
using ::google::protobuf::Service;

ControlRasMgrRasnet::ControlRasMgrRasnet(const UserCredentials& userCredentials, RasControlConfig& config):userCredentials(userCredentials), config(config)
{
    this->doNothing = ::google::protobuf::NewPermanentCallback(&DoNothing);

    try
    {
        std::string host = config.getRasMgrHost();
        //TODO-AT:Should I be doing this? Should this be factored into a protocol related file?
        if(host.find("tcp://")!=0)
        {
            host="tcp://"+host;
        }

        Channel* channel =  new Channel(host,config.getRasMgrPort());
        this->rasmgrService.reset(new RasMgrRasCtrlService_Stub(channel));
    }
    catch(std::exception& ex)
    {
        //Failed to connect.
        LERROR<<ex.what();
        throw runtime_error("Could not connect to rasmgr.");
    }
}

ControlRasMgrRasnet::~ControlRasMgrRasnet()
{
    delete this->doNothing;
}

std::string ControlRasMgrRasnet::processCommand(const std::string& command)
{
    RasCtrlRequest request;
    RasCtrlResponse response;

    //Default message that will be displayed to the user.
    std::string responseMessage = "The command could not be processed.";

    this->controller.Reset();

    std::string userName = this->userCredentials.getUserName();
    std::string password = this->userCredentials.getUserPassword();

    request.set_user_name(userName);
    request.set_password_hash(password);
    request.set_command(command);

    this->rasmgrService->ExecuteCommand(&this->controller, &request, &response, this->doNothing);

    if(this->controller.Failed())
    {
        throw runtime_error(this->controller.ErrorText());
    }

    if(response.has_message())
    {
        responseMessage = response.message();
    }

    return responseMessage;
}

} /* namespace rascontrol */
