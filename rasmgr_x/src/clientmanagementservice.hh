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

#include "../../rasnet/src/messages/rasmgr_client_service.pb.h"

#include "clientmanager.hh"
#include "servermanager.hh"

namespace rasmgr
{

class ClientManagementService : public rasnet::service::RasMgrClientService
{
public:

    ClientManagementService(boost::shared_ptr<ClientManager> clientManager,boost::shared_ptr<ServerManager> serverManager);

    virtual
    ~ClientManagementService();

    virtual void
    Connect(::google::protobuf::RpcController* controller,
            const ::rasnet::service::ConnectReq* request,
            ::rasnet::service::ConnectRepl* response,
            ::google::protobuf::Closure* done);

    virtual void
    Disconnect(::google::protobuf::RpcController* controller,
               const ::rasnet::service::DisconnectReq* request,
               ::rasnet::service::Void* response,
               ::google::protobuf::Closure* done);

    virtual void
    OpenDb(::google::protobuf::RpcController* controller,
           const ::rasnet::service::OpenDbReq* request,
           ::rasnet::service::OpenDbRepl* response,
           ::google::protobuf::Closure* done);

    virtual void
    CloseDb(::google::protobuf::RpcController* controller,
            const ::rasnet::service::CloseDbReq* request,
            ::rasnet::service::Void* response,
            ::google::protobuf::Closure* done);

    virtual void
    KeepAlive(::google::protobuf::RpcController* controller,
              const ::rasnet::service::KeepAliveReq* request,
              ::rasnet::service::Void* response,
              ::google::protobuf::Closure* done);
private:
    boost::shared_ptr<ClientManager> clientManager;/*! Instance of the ClientManager class used for adding clients and client sessions */
    boost::shared_ptr<ServerManager> serverManager;/*! Instance of the ServerManager class used for retrieving available servers*/
    boost::mutex assignServerMutex; /*! Used for synchronizing access to the area where we assign a free server */
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_CLIENTMANAGEMENTSERVICE_HH_ */
