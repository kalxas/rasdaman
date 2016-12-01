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
/**
 * SOURCE: log_config.hh
 *
 * MODULE: common
 * CLASS:  LogConfiguration
 *
 * PURPOSE:
 *   Configure easylogging
 *
 * COMMENTS:
 *   none
 *
*/

#ifndef LOG_CONFIG_HH
#define LOG_CONFIG_HH

#include <iostream>

#ifdef __APPLE__
#include <limits.h>
#else
#include <linux/limits.h>   // PATH_MAX
#endif


class LogConfiguration
{
public:
    LogConfiguration();

    // constructor which sets the path of the configuration file,
    // using the path of the directory and the name of the file
    LogConfiguration(string confDir, string confFileName);

    // configure easylogging in the client: to standard output, LDEBUG and LTRACE disabled
    void configClientLogging();

    // configure easylogging in the server: to file, LDEBUG and LTRACE disabled
    void configServerLogging(const char* logFileName);


private:
    //check if the configuration file exists
    bool existsConfigFile();

    // name of the configuration file
    string configFilePath;
};


#endif  /* LOG_CONFIG_HH */
