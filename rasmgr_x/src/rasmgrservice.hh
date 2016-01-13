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

#ifndef RASMGR_X_SRC_RASMGRSERVICE_HH
#define RASMGR_X_SRC_RASMGRSERVICE_HH

#include <boost/smart_ptr.hpp>
#include <boost/thread.hpp>

#include "../../rasnet/messages/rasmgr_rasmgr_service.grpc.pb.h"

namespace rasmgr
{

class ClientManager;
class ServerManager;

/**
 * @brief The RasmgrService class represents a GRPC service that is offered by a rasmgr
 * to other rasmgrs. It allows a remote rasmgr to acquire and release servers from clients.
 */
class RasmgrService:public rasnet::service::RasmgrRasmgrService::Service
{
public:
    RasmgrService(boost::shared_ptr<ClientManager> clientManager);

    virtual ~RasmgrService();

    virtual grpc::Status TryGetRemoteServer(grpc::ServerContext *context, const rasnet::service::GetRemoteServerRequest *request, rasnet::service::GetRemoteServerReply *response);

    virtual ::grpc::Status ReleaseServer(::grpc::ServerContext* context, const ::rasnet::service::ReleaseServerRequest* request, ::rasnet::service::Void* response);

private:
    boost::shared_ptr<ClientManager> clientManager;/*! Instance of the ClientManager class used for adding clients and client sessions */

};

}

#endif // RASMGR_X_SRC_RASMGRSERVICE_HH
