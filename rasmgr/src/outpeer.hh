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

#include "clientserverrequest.hh"
#include "clientserversession.hh"
#include "remoteclientsession.hh"

#include "rasnet/messages/rasmgr_rasmgr_service.grpc.pb.h"
#include "common/grpc/messages/health_service.grpc.pb.h"

#include <string>
#include <set>
#include <cstdint>

namespace rasmgr
{
/**
 * Represents a remote rasmgr to which this rasmgr can forward requests, in
 * particular hostname and port.
 * 
 * This class is not thread-safe, the thread-safety is managed in PeerManager.
 */
class OutPeer
{
public:
    /**
     * Identification of the remote rasmgr: hostname and port.
     */
    OutPeer(const std::string &hostName, const std::uint32_t port);

    const std::string &getHostName() const;

    std::uint32_t getPort() const;

    /**
     * Check if the peer has active client sessions i.e. a server 
     * was requested and acquired from the remote host
     * @return TRUE if the peer has at least one server running, FALSE otherwise.
     */
    bool isBusy() const;

    /**
     * Try to acquire an available server from the remote rasmgr.
     * @param request Object containing the necessary information for acquiring a server
     * @param out_reply Object containing the necessary information for the client to 
     * connect to the server and for the rasmgr to identify the connection
     * @return TRUE if a server was acquired, FALSE otherwise.
     */
    bool tryGetRemoteServer(const ClientServerRequest &request,
                            ClientServerSession &out_reply);

    /**
     * Release a previously acquired remote server
     */
    void releaseServer(const RemoteClientSession &clientSession);

private:
    std::set<std::string> openSessions; /*!< Set of open sessions */
    std::string hostName;               /*!< Name of the host on which the rasmgr is running.*/
    std::uint32_t port;                 /*!< Port on which the rasmgr is running on the given host */

    std::shared_ptr<::rasnet::service::RasmgrRasmgrService::Stub> rasmgrService;
    std::shared_ptr<::common::HealthService::Stub> healthService;

    std::string createSessionId(const RemoteClientSession &clientSession);
};
}  // namespace rasmgr

#endif  // OUTPEER_HH
