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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009,2010,2011,2012,2013,2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef RASCONTROL_X_SRC_CONTROLRASMGRRASNET_HH_
#define RASCONTROL_X_SRC_CONTROLRASMGRRASNET_HH_

#include <memory>
#include "rasnet/messages/rasmgr_rasctrl_service.grpc.pb.h"
#include "common/grpc/messages/health_service.grpc.pb.h"

#include "controlrasmgrcomm.hh"
#include "usercredentials.hh"
#include "rascontrolconfig.hh"

namespace rascontrol
{
/**
 * @brief The ControlRasMgrRasnet class Implementation of the ControlRasMgrComm that uses the rasnet protocol.
 */
class ControlRasMgrRasnet : public ControlRasMgrComm
{
public:
    ControlRasMgrRasnet(const rascontrol::UserCredentials &userCredentials, rascontrol::RasControlConfig &config);

    /**
     * @brief processCommand Process a command and return the response to that command.
     * @param command
     * @return The result from processing the command.
     */
    virtual std::string processCommand(const std::string &command);

private:
    const UserCredentials &userCredentials;
    rascontrol::RasControlConfig &config;

    std::shared_ptr<::rasnet::service::RasMgrRasCtrlService::Stub> rasmgrService;
    std::shared_ptr<::common::HealthService::Stub> healthService;
};

} /* namespace rascontrol */

#endif /* RASCONTROL_X_SRC_CONTROLRASMGRRASNET_HH_ */
