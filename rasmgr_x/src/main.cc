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

#include <sys/prctl.h>
#include <signal.h>

#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/thread.hpp>

#include "../../include/globals.hh"
#include "../../config.h"
#include "../../version.h"

#include <easylogging++.h>
#include "../../common/src/crypto/crypto.hh"
#include "../../common/src/logging/loggingutils.hh"

#include "configuration.hh"
#include "rasmanager.hh"

#define RASMGR_RESULT_OK            0
#define RASMGR_RESULT_NO_MD5        1
#define RASMGR_RESULT_ILL_ARGS      2
#define RASMGR_RESULT_FAILED        3


_INITIALIZE_EASYLOGGINGPP

void installSignalHandlers();
void sigIntHandler ( int sig );

using common::Crypto;
using rasmgr::Configuration;
using rasmgr::RasManager;

//RasManager object that orchestrates
boost::shared_ptr<rasmgr::RasManager> manager;

int main ( int argc, char** argv )
{
    Configuration config;

    installSignalHandlers();

    if ( Crypto::isMessageDigestAvailable ( DEFAULT_DIGEST ) ==false )
    {
        std::cerr<<"Error: Message Digest MD5 not available."<<std::endl;
        return RASMGR_RESULT_NO_MD5;
    }

    bool result  = config.parseCommandLineParameters ( argc,argv );
    if ( result == false )
    {
        return RASMGR_RESULT_ILL_ARGS;
    }

    common::LoggingUtils::redirectGRPCLogToEasyLogging();

    std::string logConfigFilePath(std::string(CONFDIR)+"/"+RASMGR_LOG_CONF);
    if (!config.getLogFile().empty())
    {
        easyloggingpp::Configurations easyloggingConf = common::LoggingUtils::getServerLoggingConfiguration(logConfigFilePath, config.getLogFile());
        easyloggingpp::Loggers::reconfigureAllLoggers ( easyloggingConf );
    }
    else
    {
        std::string logOutputFile = std::string(LOGDIR)+"/rasmgr."+boost::to_string(::getpid())+".log";
        easyloggingpp::Configurations easyloggingConf = common::LoggingUtils::getServerLoggingConfiguration(logConfigFilePath, logOutputFile);
        easyloggingpp::Loggers::reconfigureAllLoggers ( easyloggingConf );
    }

    if ( config.isQuiet() )
    {
        // If quiet is set, it overwrites any log configuration
        easyloggingpp::Loggers::disableAll();
    }
    else
    {
        std::cout<< "rasmgr: rasdaman server manager tool. rasdaman "
                 << RMANVERSION << " -- generated on " << COMPDATE << "." << std::endl
                 << "Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann, rasdaman GmbH.\n"
                 << "Rasdaman community is free software: you can redistribute it and/or modify "
                 "it under the terms of the GNU General Public License as published by "
                 "the Free Software Foundation, either version 3 of the License, or "
                 "(at your option) any later version. \n"
                 "Rasdaman community is distributed in the hope that it will be useful, "
                 "but WITHOUT ANY WARRANTY; without even the implied warranty of "
                 "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
                 "GNU General Public License for more details. \n\n"<<std::endl;

        std::cout << "rasmgr running on " << config.getHostName() << ", listening on port " << config.getPort() << std::endl;
    }

    manager.reset ( new RasManager ( config ) );

    try
    {
        manager->start();
    }
    catch ( std::exception& ex )
    {
        std::cerr << "rasmanager failed with exception:"<<ex.what() << std::endl;
        return RASMGR_RESULT_FAILED;
    }
    catch ( ... )
    {
        std::cerr << "rasmanager failed for an unknown reason." << std::endl;
        return RASMGR_RESULT_FAILED;
    }

    return RASMGR_RESULT_OK;
}

void sigIntHandler ( int sig )
{
    if ( manager )
    {
        manager->stop();
    }
}

void installSignalHandlers()
{
    //TODO: This is deprecated.
    signal ( SIGINT, sigIntHandler );
    signal ( SIGTERM, sigIntHandler );
    signal ( SIGHUP, SIG_IGN );
    signal ( SIGPIPE,SIG_IGN );
    signal ( SIGTTOU,SIG_IGN );
}
