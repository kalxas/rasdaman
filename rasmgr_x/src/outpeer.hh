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

#ifndef RASMGR_X_SRC_OUTPEER_HH_
#define RASMGR_X_SRC_OUTPEER_HH_

#include <string>
#include <set>

#include <boost/smart_ptr.hpp>
#include <boost/cstdint.hpp>

#include "rasnet/messages/rasmgr_rasmgr_service.grpc.pb.h"
#include "common/src/grpc/messages/health_service.grpc.pb.h"

#include "clientserverrequest.hh"
#include "clientserversession.hh"
#include "remoteclientsession.hh"

namespace rasmgr
{
/**
 * @brief The OutPeer class represents a remote rasmgr to which this rasmgr
 * can forward requests.
 */
class OutPeer
{
public:
    /**
     * @brief OutPeer Class representing a remote rasmgr to which we can send requests
     * @param hostName
     * @param port
     */
    OutPeer(const std::string& hostName, const boost::uint32_t port);

    std::string getHostName() const;

    boost::uint32_t getPort() const;

    /**
     * @brief isBusy Check if the peer has active client sessions i.e. a server was requested and acquired from the remote host
     * @return TRUE if the peer has at least one server running, FALSE otherwise.
     */
    bool isBusy() const;

    /**
     * @brief tryGetRemoteServer Try to acquire an available server from the remote rasmgr.
     * @param request Object containing the necessary information for acquiring a server
     * @param out_reply Object containing the necessary information for the client to connect to the server
     * and for the rasmgr to identify the connection
     * @return TRUE if a server was acquired, FALSE otherwise.
     */
    bool tryGetRemoteServer(const ClientServerRequest& request,
                            ClientServerSession& out_reply);

    /**
     * @brief releaseServer Release a previously acquired remote server
     * @param clientSession
     */
    void releaseServer(const RemoteClientSession& clientSession);

private:
    std::set<std::string> openSessions; /*!< Set of open sessions */
    std::string hostName; /*!< Name of the host on which the rasmgr is running.*/
    boost::uint32_t port; /*!< Port on which the rasmgr is running on the given host */

    boost::shared_ptr< ::rasnet::service::RasmgrRasmgrService::Stub> rasmgrService;
    boost::shared_ptr< ::common::HealthService::Stub> healthService;

    std::string createSessionId(const RemoteClientSession& clientSession);
};
}

#endif // OUTPEER_HH
