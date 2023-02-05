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

#include "version.h"
#include "config.h"
#include "configuration.hh"
#include "include/globals.hh"

#include <unistd.h>
#include <errno.h>
#include <cstring>
#include <boost/algorithm/string/predicate.hpp>
#include <boost/algorithm/string/trim.hpp>
#include <boost/lexical_cast.hpp>
#include <logging.hh>
#include <common/util/networkutils.hh>


namespace rasmgr
{
/// host/domain name size (See man gethostname)
const std::uint32_t Configuration::HOSTNAME_SIZE = 255;
const std::string Configuration::RASMGR_LOG_PREFIX = "rasmgr";
const std::uint32_t Configuration::MAXMSGOUTBUFF = 20000;

Configuration::Configuration():
    cmlInter(CommandLineParser::getInstance()),
    cmlHelp(cmlInter.addFlagParameter('h', "help", "print this help")),
    cmlHostName(cmlInter.addStringParameter(CommandLineParser::noShortName, "hostname", "<name> the advertized host name (master only, default: same as UNIX command 'hostname')")),
    cmlPort(cmlInter.addLongParameter(CommandLineParser::noShortName, "port", "<port> listen port number", DEFAULT_PORT)),
    cmlName(cmlInter.addStringParameter(CommandLineParser::noShortName, "name", "<name> symbolic name of this rasmgr (slave only, default: the host name)")),
    cmlQuiet(cmlInter.addFlagParameter('q', CommandLineParser::noLongName, "quiet: don't log requests (default: log requests to stdout)")),
    cmlLog(cmlInter.addStringParameter('l', "log", "<log-file> log is printed to <log-file>\n\t\tif <log-file> is stdout , log output is printed to standard out", "log/rasmgr.<pid>.log"))
{
    char hName[HOSTNAME_SIZE];
    int ghnResult = gethostname(hName, sizeof(hName));
    if (ghnResult != 0) // cannot get hostname?
    {
        int ghnErrno = errno;
        LERROR << "Error: cannot get hostname of my machine: error " << ghnErrno << "; will use '" << DEFAULT_HOSTNAME << "' as heuristic.";
        this->hostName = DEFAULT_HOSTNAME;
    }

    this->name = this->hostName;
    this->port = DEFAULT_PORT;
    this->quiet = false;
}

Configuration::~Configuration()
{

}

bool Configuration::parseCommandLineParameters(int argc, char **argv)
{
    bool result = true;

    try
    {
        this->cmlInter.processCommandLine(argc, argv);
    }
    catch (CmlException &err)
    {
        std::cerr << "Error parsing command line: " << err.what();
        this->printHelp();
        result = false;
    }

    if ((result == true) && cmlHelp.isPresent())
    {
        printHelp();
        result = false;
    }

    if ((result == true) && cmlQuiet.isPresent())
    {
        this->quiet = true;
        result = true;
    }

    if ((result == true) && cmlPort.isPresent())
    {
        try
        {
            this->port = cmlPort.getValueAsLong();
        }
        catch (CmlException &err)
        {
            std::cerr << "Error converting port parameter " << cmlPort.getLongName() << " to integer: " << err.what() << std::endl;
            result = false;
        }
    }

    if ((result == true) && cmlLog.isPresent())
    {
        try
        {
            this->logFile = cmlLog.getValueAsString();
        }
        catch (CmlException &err)
        {
            std::cerr << "Error converting logconf parameter " << cmlLog.getValueAsString() << " to string: " << err.what() << std::endl;
            result = false;
        }
    }

    if ((result == true) && cmlHostName.isPresent())
    {
        if (HOSTNAME_SIZE > strlen(cmlHostName.getValueAsString()))
        {
            this->hostName = cmlHostName.getValueAsString();
        }
        else
        {
            std::cerr << "Error: host name exceeds length limit of " << sizeof(hostName) << " characters." << std::endl;
            result = false;
        }
    }

    return result;
}

void Configuration::printHelp()
{
    std::cout << "rasmgr: rasdaman server " << RMANVERSION << " on base DBMS " << BASEDBSTRING << "."<<std::endl;
    std::cout << "Usage: rasmgr [options]" << std::endl;
    std::cout << "Options:" << std::endl;
    this->cmlInter.printHelp();
    std::cout << std::endl;
}

std::uint32_t Configuration::getPort() const
{
    return port;
}
const std::string &Configuration::getHostName() const
{
    return hostName;
}
const std::string &Configuration::getName() const
{
    return name;
}
bool Configuration::isQuiet() const
{
    return quiet;
}
const std::string &Configuration::getLogFile() const
{
    return logFile;
}

}
