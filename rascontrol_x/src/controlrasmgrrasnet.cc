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

#include <grpc++/grpc++.h>
#include <grpc++/security/credentials.h>

#include "rasnet/messages/rasmgr_rasctrl_service.pb.h"

#include <logging.hh>
#include "common/src/grpc/grpcutils.hh"
#include "common/src/exceptions/rasexceptions.hh"

#include "controlrasmgrrasnet.hh"

namespace rascontrol
{

using common::GrpcUtils;
using common::ConnectionFailedException;

using std::runtime_error;
using std::unique_ptr;
using std::shared_ptr;

using grpc::Channel;
using grpc::ClientContext;
using grpc::Status;

using rasnet::service::RasCtrlRequest;
using rasnet::service::RasCtrlResponse;
using rasnet::service::RasMgrRasCtrlService;


ControlRasMgrRasnet::ControlRasMgrRasnet(const UserCredentials& userCredentials, RasControlConfig& config):
    userCredentials(userCredentials), config(config)
{
    try
    {
        string serverAddress = common::GrpcUtils::constructAddressString(config.getRasMgrHost(), config.getRasMgrPort());
        std::shared_ptr<Channel> channel(grpc::CreateChannel(serverAddress, grpc::InsecureChannelCredentials()));

        this->rasmgrService.reset(new RasMgrRasCtrlService::Stub(channel));
        this->healthService.reset(new common::HealthService::Stub(channel));
    }
    catch (std::exception& ex)
    {
        //Failed to connect.
        LERROR << ex.what();

        throw ConnectionFailedException(ex.what());
    }
}


std::string ControlRasMgrRasnet::processCommand(const std::string& command)
{
    RasCtrlRequest request;
    RasCtrlResponse response;

    //Default message that will be displayed to the user.
    std::string responseMessage;

    std::string userName = this->userCredentials.getUserName();
    std::string password = this->userCredentials.getUserPassword();

    request.set_user_name(userName);
    request.set_password_hash(password);
    request.set_command(command);

    if (!common::GrpcUtils::isServerAlive(this->healthService, SERVICE_CALL_TIMEOUT))
    {
        throw common::ConnectionFailedException();
    }

    ClientContext context;
    Status status = this->rasmgrService->ExecuteCommand(&context, request, &response);

    if (!status.ok())
    {
        GrpcUtils::convertStatusToExceptionAndThrow(status);
    }

    if (!response.message().empty())
    {
        responseMessage = response.message();
    }

    return responseMessage;
}
} /* namespace rascontrol */
