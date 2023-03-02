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

#include <logging.hh>
#include "common/crypto/crypto.hh"
#include "common/grpc/grpcutils.hh"
#include "loggingutils.hh"

#include "version.h"

#include "rascontrolconfig.hh"
#include "usercredentials.hh"
#include "rascontrolconstants.hh"
#include "rascontrol.hh"

#ifndef RMANVERSION
#error "Please specify RMANVERSION variable!"
#endif

INITIALIZE_EASYLOGGINGPP

int main(int argc, char **argv)
{
    //Default logging configuration
    el::Configurations defaultConf;
    defaultConf.setToDefault();
    defaultConf.parseFromText("*Global:\n Enabled = false");
    el::Loggers::reconfigureLogger("default", defaultConf);

    common::GrpcUtils::redirectGRPCLogToEasyLogging();

    rascontrol::RasControlConfig config;
    rascontrol::UserCredentials userCredentials;

    try
    {
        if (config.parseCommandLineParameters(argc, argv) == false)
        {
            return EXIT_FAILURE;
        }

        if (!config.getLogConfigFile().empty())
        {
            std::string configFile = config.getLogConfigFile();
            el::Configurations conf(configFile.c_str());
            el::Loggers::reconfigureAllLoggers(conf);
        }

        if (!config.isQuietMode())
        {
            std::cout << "rascontrol: rasdaman server remote control utility " << RMANVERSION << "." << std::endl;
            std::cout << "Copyright (c) 2003-2021 Peter Baumann rasdaman GmbH.\n"
                         "Rasdaman community is free software: you can redistribute it and/or modify "
                         "it under the terms of the GNU General Public License as published by "
                         "the Free Software Foundation, either version 3 of the License, or "
                         "(at your option) any later version.\n"
                         "Rasdaman community is distributed in the hope that it will be useful, "
                         "but WITHOUT ANY WARRANTY; without even the implied warranty of "
                         "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
                         "GNU General Public License for more details."
                      << std::endl;
        }

        if (config.isHelpRequested())
        {
            config.displayHelp();
            return EXIT_SUCCESS;
        }

        if (common::Crypto::isMessageDigestAvailable(DEFAULT_DIGEST) == false)
        {
            std::cout << "Error: Message Digest " << DEFAULT_DIGEST << " not available." << std::endl;
            return EXIT_FAILURE;
        }

        if (config.getLoginMode() == rascontrol::RasControlConfig::LGIINTERACTIV)
        {
            userCredentials.interactiveLogin();
        }
        else
        {
            userCredentials.environmentLogin();
        }

        rascontrol::RasControl control(config, userCredentials);

        control.start();
    }
    catch (std::exception &ex)
    {
        std::cout << ex.what() << std::endl;
        return EXIT_FAILURE;
    }
    catch (...)
    {
        std::cout << "rascontrol failed for an unknown reason."
                  << " Please contact the administrator with instructions to reproduce the failure" << std::endl;

        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}
