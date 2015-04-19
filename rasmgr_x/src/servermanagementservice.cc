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

#include "common/src/logging/easylogging++.hh"

#include "servermanagementservice.hh"

namespace rasmgr
{
using boost::shared_ptr;
using std::string;

ServerManagementService::ServerManagementService(shared_ptr<ServerManager> serverManager)
{
    this->serverManager=serverManager;
}

ServerManagementService::~ServerManagementService()
{}

void ServerManagementService::RegisterServer(::google::protobuf::RpcController* controller,
        const ::rasnet::service::RegisterServerReq* request,
        ::rasnet::service::Void* response,
        ::google::protobuf::Closure* done)
{
    try
    {
        LINFO<<"Registering server with ID:"<<request->serverid();

        this->serverManager->registerServer(request->serverid());

        LINFO<<"Finished registering server with ID:"<<request->serverid();
    }
    catch(std::exception& ex)
    {
        LERROR<<ex.what();
        controller->SetFailed(ex.what());
    }
    catch(...)
    {
        string failureReason="Failed to register server for unknown reason.";
        LERROR<<failureReason;
        controller->SetFailed(failureReason);
    }
}

} /* namespace rasmgr */
