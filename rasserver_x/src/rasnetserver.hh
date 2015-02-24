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

#ifndef RASNETSERVER_HH
#define RASNETSERVER_HH

#include <boost/program_options.hpp>
#include <boost/scoped_ptr.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>
#include <google/protobuf/service.h>
#include <google/protobuf/stubs/common.h>
#include <iostream>
#include <unistd.h>


#include "common/src/logging/easylogging++.hh"
#include "rasnet/src/messages/rasmgr_rassrvr_service.pb.h"
#include "rasnet/src/messages/rassrvr_rasmgr_service.pb.h"
#include "rasnet/src/service/client/channel.hh"
#include "rasnet/src/service/client/clientcontroller.hh"
#include "rasnet/src/service/server/servicemanager.hh"
#include "server/rasserver_config.hh"

class RasnetServer
{
public:
    RasnetServer(Configuration configuration);
    void startRasnetServer();
    void stopRasnetServer();

private:
    boost::shared_ptr<rasnet::ServiceManager> serviceManager;
    int port;

    boost::mutex runMutex;
};

#endif // RASNETSERVER_HH
