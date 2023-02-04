/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#ifndef RASSERVER_X_SRC_RASMGRCOMM_HH
#define RASSERVER_X_SRC_RASMGRCOMM_HH

#include "rasnet/messages/rasmgr_rassrvr_service.grpc.pb.h"
#include "common/grpc/messages/health_service.grpc.pb.h"

#include <string>
#include <cstdint>
#include <memory>
#include <boost/thread/shared_mutex.hpp>

namespace rasserver
{

/**
 * Class for communicating requests from rasserver to rasmgr.
 */
class RasmgrComm
{
public:
    RasmgrComm(const std::string &rasmgrHost, const std::uint32_t rasmgrPort);
    ~RasmgrComm() = default;
    
    void registerServerWithRasmgr(const std::string &serverId);
    
private:
    
    std::string rasmgrHost;
    std::string serverId;
    
    /// Service stub used to communicate with the rasmgr process
    std::shared_ptr<rasnet::service::RasMgrRasServerService::Stub> rasmgrService;
    /// Flag used to indicate if the service was initialized
    bool rasmgrServiceInitialized{false};
    /// Mutex to serialize access to the rasmgrService from multiple threads
    boost::shared_mutex rasmgrServiceMutex;
    
    std::shared_ptr<common::HealthService::Stub> rasmgrHealthService;
    
    // -------------------------------------------------------------------------
    
    std::shared_ptr<rasnet::service::RasMgrRasServerService::Stub>
    getRasmgrService(bool throwIfConnectionFailed);
    
    void initRasmgrService();
    
    void configureDeadline(grpc::ClientContext &context, int deadline);
};

}
#endif // RASSERVER_X_SRC_RASMGRCOMM_HH
