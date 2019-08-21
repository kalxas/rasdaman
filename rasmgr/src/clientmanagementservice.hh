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

#ifndef RASMGR_X_SRC_CLIENTMANAGEMENTSERVICE_HH_
#define RASMGR_X_SRC_CLIENTMANAGEMENTSERVICE_HH_

#include <boost/smart_ptr.hpp>
#include <boost/thread.hpp>

#include "rasnet/messages/rasmgr_client_service.grpc.pb.h"

namespace rasmgr
{

class ClientManager;

/**
 * @brief The ClientManagementService class Handles requests from clients,
 * keeps track of active clients sessions and removes clients that have
 * not reported activity in a predefined amount of time.
 */
class ClientManagementService : public rasnet::service::RasmgrClientService::Service
{
public:

    ClientManagementService(boost::shared_ptr<ClientManager> clientManager);

    virtual ~ClientManagementService();

    virtual grpc::Status Connect(grpc::ServerContext *context, const rasnet::service::ConnectReq *request, rasnet::service::ConnectRepl *response) override;

    virtual grpc::Status Disconnect(grpc::ServerContext *context, const rasnet::service::DisconnectReq *request, rasnet::service::Void *response) override;

    virtual grpc::Status OpenDb(grpc::ServerContext *context, const rasnet::service::OpenDbReq *request, rasnet::service::OpenDbRepl *response) override;

    virtual grpc::Status CloseDb(grpc::ServerContext *context, const rasnet::service::CloseDbReq *request, rasnet::service::Void *response) override;

    virtual grpc::Status KeepAlive(grpc::ServerContext *context, const rasnet::service::KeepAliveReq *request, rasnet::service::Void *response) override;

private:
    boost::shared_ptr<ClientManager> clientManager;/*! Instance of the ClientManager class used for adding clients and client sessions */
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_CLIENTMANAGEMENTSERVICE_HH_ */
