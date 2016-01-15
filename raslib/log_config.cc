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
 * SOURCE: log_config.cc
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

using namespace std;

#include "log_config.hh"
#include <easylogging++.h>

#include "globals.hh"

LogConfiguration::LogConfiguration()
{
}

// constructor which sets the path of the configuration file
LogConfiguration::LogConfiguration(string confDir, string confFileName)
{
    configFilePath = confDir + "/" + confFileName;
}

// check if the configuration file exists
bool LogConfiguration::existsConfigFile()
{
    ifstream f(configFilePath.c_str());
    if (f.good())
    {
        f.close();
        return true;
    }
    else
    {
        f.close();
        return false;
    }
}

// configure easylogging in the client: to standard output, LDEBUG and LTRACE disabled
void LogConfiguration::configClientLogging()
{
    if(existsConfigFile())
    {
        easyloggingpp::Configurations defaultConf(configFilePath);
        easyloggingpp::Loggers::setDefaultConfigurations(defaultConf, true);
        easyloggingpp::Loggers::reconfigureAllLoggers(defaultConf);
        defaultConf.clear();
    }
    else        // if configuration file is not found, use inline configuration
    {
        easyloggingpp::Configurations defaultConf;
        defaultConf.setToDefault();
        defaultConf.set(easyloggingpp::Level::All,
                easyloggingpp::ConfigurationType::Format, "[%level] - %log");
        defaultConf.set(easyloggingpp::Level::Info,
                easyloggingpp::ConfigurationType::Format, " [%level] - %log");
        defaultConf.set(easyloggingpp::Level::Warning,
                easyloggingpp::ConfigurationType::Format, " [%level] - %log");
        defaultConf.set(easyloggingpp::Level::Debug,
                easyloggingpp::ConfigurationType::Format, "[%level] - %datetime, %loc: %log");
        defaultConf.set(easyloggingpp::Level::Trace,
                easyloggingpp::ConfigurationType::Format, "[%level] - %datetime, %loc: %log");

        defaultConf.set(easyloggingpp::Level::All,
                easyloggingpp::ConfigurationType::ToFile, "false");
        defaultConf.set(easyloggingpp::Level::All,
                easyloggingpp::ConfigurationType::ToStandardOutput, "true");
        defaultConf.set(easyloggingpp::Level::Debug,
                easyloggingpp::ConfigurationType::Enabled, "false");
        defaultConf.set(easyloggingpp::Level::Trace,
                easyloggingpp::ConfigurationType::Enabled, "false");

        easyloggingpp::Loggers::setDefaultConfigurations(defaultConf, true);
        easyloggingpp::Loggers::reconfigureAllLoggers(defaultConf);
        defaultConf.clear();
        LWARNING << "Configuration file not found.";
    }
}

// configure easylogging in the server: to file, LDEBUG and LTRACE disabled
void LogConfiguration::configServerLogging(const char* logFileName)
{
    if(existsConfigFile())
    {
        easyloggingpp::Configurations defaultConf(configFilePath);
        defaultConf.set(easyloggingpp::Level::All ,
            easyloggingpp::ConfigurationType::Filename, logFileName);
        easyloggingpp::Loggers::setDefaultConfigurations(defaultConf, true);
        easyloggingpp::Loggers::reconfigureAllLoggers(defaultConf);
        defaultConf.clear();
    }
    else        // if configuration file is not found, use inline configuration
    {
        easyloggingpp::Configurations defaultConf;
        defaultConf.setToDefault();
        defaultConf.set(easyloggingpp::Level::All ,
            easyloggingpp::ConfigurationType::Filename, logFileName);
        defaultConf.set(easyloggingpp::Level::All,
                easyloggingpp::ConfigurationType::Format, "[%level] - %datetime, %loc: %log");
        defaultConf.set(easyloggingpp::Level::Info,
                easyloggingpp::ConfigurationType::Format, " [%level] - %datetime: %log");
        defaultConf.set(easyloggingpp::Level::Warning,
                easyloggingpp::ConfigurationType::Format, " [%level] - %datetime, %loc: %log");

        defaultConf.set(easyloggingpp::Level::All,
                easyloggingpp::ConfigurationType::ToFile, "true");
        defaultConf.set(easyloggingpp::Level::All,
                easyloggingpp::ConfigurationType::ToStandardOutput, "false");
        defaultConf.set(easyloggingpp::Level::Debug,
                easyloggingpp::ConfigurationType::Enabled, "false");
        defaultConf.set(easyloggingpp::Level::Trace,
                easyloggingpp::ConfigurationType::Enabled, "false");

        easyloggingpp::Loggers::setDefaultConfigurations(defaultConf, true);
        easyloggingpp::Loggers::reconfigureAllLoggers(defaultConf);
        defaultConf.clear();
        LWARNING << "Configuration file not found.";
    }
}
