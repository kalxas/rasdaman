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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef COMMON_SRC_LOGGING_LOGGINGUTILS_HH
#define COMMON_SRC_LOGGING_LOGGINGUTILS_HH

#include <logging.hh>
#include <string>

namespace common
{
class LogConfiguration
{
public:
    LogConfiguration();

    // constructor which sets the path of the configuration file,
    // using the path of the directory and the name of the file
    LogConfiguration(std::string confDir, std::string confFileName);

    void configClientLogging(bool quiet = false);

    void configServerLogging(const std::string &outputLogFilePath, bool quiet = false);

    el::Configurations &getConfig();

private:
    void initConfig(const std::string &outputLogFilePath, bool quiet);

    std::string configFilePath;

    el::Configurations conf;
};
}  // namespace common

#endif  // COMMON_SRC_LOGGING_LOGGINGUTILS_HH
