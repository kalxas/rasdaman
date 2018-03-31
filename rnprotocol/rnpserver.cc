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
/*************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 * - called from rasserver_main.cc
 * - startRnpServer() is twin to startRpcServer and startHttpServer()
 *
 ************************************************************/

#include "config.h"
#include <iostream>
#include <signal.h>
#include "rnpserver.hh"
#include "srvrasmgrcomm.hh"
#include "server/rasserver_config.hh"
#include "rnpservercomm.hh"
#include "relblobif/blobtile.hh"

#include "debug-srv.hh"

#include <logging.hh>

// only for access control
#include "servercomm/servercomm.hh"

#include "server/rasserver_entry.hh"

RnpRasDaManComm rnpServerComm;
RasserverCommunicator communicator(&rnpServerComm);

extern "C"
{
    void rnpSignalHandler(int sig);
}

void startRnpServer()
{
    signal(SIGTERM, rnpSignalHandler);

    LINFO << "Initializing control connections...";
    rasmgrComm.init(static_cast<unsigned int>(configuration.getTimeout()), configuration.getServerName(), configuration.getRasmgrHost(), configuration.getRasmgrPort());

    accessControl.setServerName(configuration.getServerName());

    LINFO << "informing rasmgr: server available...";
    rasmgrComm.informRasmgrServerAvailable();
    LINFO << "ok";

    //##################

    LINFO << "Initializing job control...";
    communicator.initJobs(1);
    communicator.setTimeout(RNP_TIMEOUT_LISTEN, 0); // the select loop!

    communicator.setListenPort(configuration.getListenPort());

    rnpServerComm.setServerJobs(1);
    rnpServerComm.connectToCommunicator(communicator);
    rnpServerComm.setTransmitterBufferSize(configuration.getMaxTransferBufferSize());

    LINFO << "setting timeout to " << configuration.getTimeout() << " secs...";

    rnpServerComm.setTimeoutInterval(configuration.getTimeout());
    NbJob::setTimeoutInterval(configuration.getTimeout());

    LINFO << "connecting to base DBMS...";
    RasServerEntry& rasserver = RasServerEntry::getInstance();
    rasserver.compat_connectToDBMS();

    LINFO << "ok, waiting for clients.\n";
    communicator.runServer();

    LINFO << "RNP server shutdown in progress...";
    if (TileCache::cacheLimit > 0)
    {
        TileCache::clear();
    }
    rnpServerComm.disconnectFromCommunicator();

    //##################

    LINFO << "informing rasmgr...";
    rasmgrComm.informRasmgrServerDown();

    LINFO << "server stopped.";
}

void stopRnpServer()
{
    communicator.shouldExit();
}


void rnpSignalHandler(__attribute__((unused)) int sig)
{
    static int in_progress = 0; // sema for signal-in-signal

    if (in_progress)        // routine already active?
    {
        return;    // ...then don't interfere
    }

    in_progress = 1;        // block further signals

    for (long j = 0; j < 1000000; j++) // make sure server notices shutdown
        ;           // NB: why this large number? doesn't seem to be thought over carefully -- PB 2003-nov-23

    stopRnpServer();        // send shutdown request
}


