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

#include "rasmgrcomm.hh"
#include "common/grpc/grpcutils.hh"
#include "common/util/system.hh"
#include "common/exceptions/connectionfailedexception.hh"
#include "rasnet/messages/rasmgr_rassrvr_service.grpc.pb.h"
#include "raslib/error.hh"
#include "include/globals.hh"

#include <logging.hh>

#include <chrono>
#include <grpc++/grpc++.h>
#include <algorithm>

namespace rasserver
{

using common::GrpcUtils;
using rasnet::service::Void;
using rasnet::service::RegisterServerReq;

RasmgrComm::RasmgrComm(const std::string &_rasmgrHost, const uint32_t rasmgrPort)
{
    this->rasmgrHost = GrpcUtils::constructAddressString(_rasmgrHost, rasmgrPort);
}

void RasmgrComm::registerServerWithRasmgr(const std::string &_serverId)
{
    this->serverId = _serverId;
    grpc::ClientContext context;
    this->configureDeadline(context, SERVICE_CALL_TIMEOUT);
    RegisterServerReq request;
    request.set_serverid(this->serverId);
    Void response;

    auto status = this->getRasmgrService(true)->RegisterServer(&context, request, &response);
    if (!status.ok())
    {
        LERROR << "Could not register rasserver with rasmgr: " << status.error_message();
        GrpcUtils::convertStatusToExceptionAndThrow(status);
    }
}

std::shared_ptr<rasnet::service::RasMgrRasServerService::Stub>
RasmgrComm::getRasmgrService(bool throwIfConnectionFailed)
{
    this->initRasmgrService();

    if (!GrpcUtils::isServerAlive(this->rasmgrHealthService, SERVICE_CALL_TIMEOUT))
    {
        LDEBUG << "The client failed to connect to rasmgr within " << SERVICE_CALL_TIMEOUT << " ms.";
        if (throwIfConnectionFailed)
        {
            throw common::ConnectionFailedException("The client failed to connect to rasmgr.");
        }
    }

    return this->rasmgrService;
}

void RasmgrComm::initRasmgrService()
{
    if (!this->rasmgrServiceInitialized)
    {
        try
        {
            boost::unique_lock<boost::shared_mutex> lock(this->rasmgrServiceMutex);
            auto channelArgs = GrpcUtils::getDefaultChannelArguments();
            auto channelCred = grpc::InsecureChannelCredentials();
            auto channel = grpc::CreateCustomChannel(this->rasmgrHost, channelCred, channelArgs);
            this->rasmgrService.reset(new ::rasnet::service::RasMgrRasServerService::Stub(channel));
            this->rasmgrHealthService.reset(new common::HealthService::Stub(channel));
            this->rasmgrServiceInitialized = true;
        }
        catch (std::exception &ex)
        {
            LERROR << "Failed initializing rasmgr service: " << ex.what();
            throw r_EGeneral(ex.what());
        }
    }
}

void RasmgrComm::configureDeadline(grpc::ClientContext &context, int deadline)
{
    using namespace std::chrono;
    context.set_deadline(system_clock::now() + milliseconds(deadline));
}

}
