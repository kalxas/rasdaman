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

#include "servermanagementservice.hh"
#include "servermanager.hh"
#include "clientmanager.hh"
#include "server.hh"
#include "common/grpc/grpcutils.hh"
#include "common/exceptions/exception.hh"
#include <logging.hh>

namespace rasmgr
{

ServerManagementService::ServerManagementService(std::shared_ptr<ServerManager> sm,
                                                 std::shared_ptr<ClientManager> cm)
    : serverManager{sm}, clientManager{cm}
{}

grpc::Status ServerManagementService::RegisterServer(
    grpc::ServerContext *, const rasnet::service::RegisterServerReq *request, rasnet::service::Void *)
{
    const auto &serverId = request->serverid();
    grpc::Status status = grpc::Status::OK;
    try
    {
        LDEBUG << "Registering server " << serverId;
        this->serverManager->registerServer(serverId);
    }
    catch (common::Exception &ex)
    {
        LDEBUG << "Failed registering server " << serverId << ": " << ex.what();
        status = common::GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (std::exception &ex)
    {
        LDEBUG << "Failed registering server " << serverId << ": " << ex.what();
        status = common::GrpcUtils::convertExceptionToStatus(ex);
    }
    catch (...)
    {
        LDEBUG << "Failed registering server " << serverId;
        status = common::GrpcUtils::convertExceptionToStatus("Failed registering server " + serverId);
    }

    return status;
}

} /* namespace rasmgr */
