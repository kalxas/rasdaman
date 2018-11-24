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

#include "common/crypto/crypto.hh"
#include "common/grpc/grpcutils.hh"
#include "common/logging/signalhandler.hh"
#include "loggingutils.hh"

#include "configuration.hh"
#include "rasmanager.hh"

#define RASMGR_RESULT_OK            0
#define RASMGR_RESULT_NO_MD5        1
#define RASMGR_RESULT_ILL_ARGS      2
#define RASMGR_RESULT_FAILED        3


INITIALIZE_EASYLOGGINGPP

void crashHandler(int sig, siginfo_t* info, void* ucontext);
void shutdownHandler(int sig, siginfo_t* info, void* ucontext);

using common::Crypto;
using rasmgr::Configuration;
using rasmgr::RasManager;

//RasManager object that orchestrates
boost::shared_ptr<rasmgr::RasManager> manager;

void shutdownHandler(__attribute__ ((unused)) int sig,
        __attribute__ ((unused)) siginfo_t* info, __attribute__ ((unused)) void* ucontext)
{
    static bool alreadyExecuting{false};
    if (!alreadyExecuting)
    {
        alreadyExecuting = true;
        if (manager)
        {
            manager->stop();
        }
    }
}

void crashHandler(int sig, siginfo_t* info, __attribute__ ((unused)) void* ucontext)
{
    static bool alreadyExecuting{false};
    if (!alreadyExecuting)
    {
        alreadyExecuting = true;
        LERROR << "Interrupted by signal " << common::SignalHandler::toString(info)
               << "... stacktrace:\n" << common::SignalHandler::getStackTrace();
        if (manager)
        {
            manager->stop();
        }
        exit(sig);
    }
}

int main(int argc, char** argv)
{
    // handle abort signals and ignore irrelevant signals
    common::SignalHandler::handleAbortSignals(crashHandler);
    common::SignalHandler::ignoreStandardSignals();

    Configuration config;

    if (Crypto::isMessageDigestAvailable(DEFAULT_DIGEST) == false)
    {
        std::cerr << "Error: Message Digest MD5 not available." << std::endl;
        return RASMGR_RESULT_NO_MD5;
    }

    bool result  = config.parseCommandLineParameters(argc, argv);
    if (result == false)
    {
        return RASMGR_RESULT_ILL_ARGS;
    }

    auto outputLogFilePath = config.getLogFile();
    if (outputLogFilePath.empty())
    {
        outputLogFilePath = std::string(LOGDIR);
        if (outputLogFilePath[outputLogFilePath.length() - 1] != '/')
            outputLogFilePath += "/";
        outputLogFilePath += string("rasmgr.") + std::to_string(::getpid()) + ".log";
    }

    // setup log config
    common::LogConfiguration logConfig(string(CONFDIR), RASMGR_LOG_CONF);
    logConfig.configServerLogging(outputLogFilePath, config.isQuiet());
    common::GrpcUtils::redirectGRPCLogToEasyLogging();

    // should come after the log config as it logs msgs
    common::SignalHandler::handleShutdownSignals(shutdownHandler);

    LINFO << "rasmgr: rasdaman server manager tool. rasdaman "
          << RMANVERSION << ".\n"
          << "Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann, rasdaman GmbH.\n"
          << "Rasdaman community is free software: you can redistribute it and/or modify "
          "it under the terms of the GNU General Public License as published by "
          "the Free Software Foundation, either version 3 of the License, or "
          "(at your option) any later version.\n"
          "Rasdaman community is distributed in the hope that it will be useful, "
          "but WITHOUT ANY WARRANTY; without even the implied warranty of "
          "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
          "GNU General Public License for more details.\n";

    manager.reset(new RasManager(config));
    try
    {
        manager->start();
    }
    catch (std::exception& ex)
    {
        LERROR << "rasmanager failed with exception: " << ex.what();
        std::cerr << "rasmanager failed with exception: " << ex.what() << std::endl;
        return RASMGR_RESULT_FAILED;
    }
    catch (...)
    {
        LERROR << "rasmanager failed for an unknown reason.";
        std::cerr << "rasmanager failed for an unknown reason." << std::endl;
        return RASMGR_RESULT_FAILED;
    }

    return RASMGR_RESULT_OK;
}
