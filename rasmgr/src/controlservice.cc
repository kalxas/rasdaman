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

#include <logging.hh>
#include "common/grpc/grpcutils.hh"
#include "common/exceptions/exception.hh"

#include "controlservice.hh"
#include "controlcommandexecutor.hh"

namespace rasmgr
{

ControlService::ControlService(std::shared_ptr<ControlCommandExecutor> commandExecutor):
    commandExecutor(commandExecutor)
{}

ControlService::~ControlService()
{}


grpc::Status rasmgr::ControlService::ExecuteCommand(__attribute__((unused)) grpc::ServerContext *context,
        const rasnet::service::RasCtrlRequest *request,
        rasnet::service::RasCtrlResponse *response)
{
    auto status = grpc::Status::OK;
    const auto &cmd = request->command();
    try
    {
        std::string result;
        if (request->user_name().empty() || request->password_hash().empty())
        {
            result = "The user's credentials are not set, cannot execute command '" + cmd + "'.";
        }
        else
        {
            result = this->commandExecutor->executeCommand(cmd, request->user_name(), request->password_hash());
        }

        response->set_message(result);
    }
    catch (std::exception &ex)
    {
        LERROR << "Failed executing rascontrol command '" << cmd << "': " << ex.what();
        status = common::GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (common::Exception &ex)
    {
        LERROR << "Failed executing rascontrol command '" << cmd << "': " << ex.what();
        status = common::GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        auto err = "Failed executing rascontrol command '" + cmd + "'.";
        LERROR << err;
        status = common::GrpcUtils::convertExceptionToStatus(err);
    }

    return status;
}
}
/* namespace rasmgr */
