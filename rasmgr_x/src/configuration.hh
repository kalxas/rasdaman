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

#ifndef RASMGR_X_SRC_CONFIGURATION_HH
#define RASMGR_X_SRC_CONFIGURATION_HH

#include <string>

#include <boost/cstdint.hpp>


#include "../../commline/cmlparser.hh"

namespace rasmgr
{
/**
 * @brief The Configuration class Configuration object used to initialize rasmgr
 * from the command line.
 */
class Configuration
{
public:
    Configuration();

    virtual ~Configuration();

    bool parseCommandLineParameters(int argc, char** argv);

    void printHelp();

    boost::uint32_t getPort() const;

    std::string getHostName() const;

    std::string getName() const;

    bool isQuiet() const;

    std::string getLogFile() const;

private:
    static const boost::uint32_t HOSTNAME_SIZE;
    static const std::string RASMGR_LOG_PREFIX;
    static const boost::uint32_t MAXMSGOUTBUFF;

    //interface program
    CommandLineParser    &cmlInter;
    CommandLineParameter &cmlHelp, &cmlHostName, &cmlPort;
    CommandLineParameter &cmlName, &cmlQuiet, &cmlLog;

    bool quiet;
    std::string name; /*!< symbolic name of this rasmgr  */
    std::string hostName;/*!< the advertized host name (master only, default: same as UNIX command 'hostname')" */
    boost::uint32_t port;/*!< Port number */
    std::string logFile;/*!< The file to which to output the log */

};
}

#endif // CONFIGURATION_HH
