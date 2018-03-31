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

#include <sys/stat.h>
#include <unistd.h>
#include "globals.hh"
#include "config.h"
#include "loggingutils.hh"
#include "raslib/commonutil.hh"

namespace common
{
using namespace std;

LogConfiguration::LogConfiguration()
{
}

// constructor which sets the path of the configuration file
LogConfiguration::LogConfiguration(string confDir, string confFileName)
{
    auto sep = confDir[confDir.length() - 1] == '/' ? "" : "/";
    configFilePath = confDir + sep + confFileName;
}

void LogConfiguration::configClientLogging(bool quiet)
{
    initConfig("", quiet);
}

void LogConfiguration::configServerLogging(const string& outputLogFilePath, bool quiet)
{
    initConfig(outputLogFilePath, quiet);
}

void LogConfiguration::initConfig(const string& outputLogFilePath, bool quiet)
{
    bool client = outputLogFilePath.empty();
    
    struct stat buffer;
    auto configFileExists = stat(configFilePath.c_str(), &buffer) == 0;
    
    if (configFileExists)
    {
        conf = el::Configurations(configFilePath);
    }
    else
    {
        conf.setToDefault();
        auto globalFmt = client ? "%msg" : "[%level] - %datetime, %loc: %msg";
        conf.set(el::Level::Global, el::ConfigurationType::Format, globalFmt);
        if (!client)
            conf.set(el::Level::Info, el::ConfigurationType::Format,
                     " [%level] - %datetime: %msg");

        conf.set(el::Level::Global, el::ConfigurationType::ToFile,
                 client ? "false" : "true");
        conf.set(el::Level::Global, el::ConfigurationType::ToStandardOutput,
                 client ? "true" : "false");
        conf.set(el::Level::Debug, el::ConfigurationType::Enabled, "false");
        conf.set(el::Level::Trace, el::ConfigurationType::Enabled, "false");
    }
    
    el::Loggers::addFlag(el::LoggingFlag::LogDetailedCrashReason);
    el::Loggers::addFlag(el::LoggingFlag::CreateLoggerAutomatically);
    el::Loggers::addFlag(el::LoggingFlag::DisableApplicationAbortOnFatalLog);
    // if it's a client and the output is on a terminal then color the output
    if (client && isatty(1))
        el::Loggers::addFlag(el::LoggingFlag::ColoredTerminalOutput);
    // if it's a server and the log filepath is not set -> set to outputLogFilePath
    if (!client && !conf.get(el::Level::Global, el::ConfigurationType::Filename))
        conf.set(el::Level::Global, el::ConfigurationType::Filename, outputLogFilePath);
    if (quiet)
        conf.set(el::Level::Global, el::ConfigurationType::Enabled, "false");
    el::Loggers::setDefaultConfigurations(conf, true);
    
    if (!configFileExists)
    {
        LWARNING << StackTrace();
        LWARNING << "Using default log configuration as the config file '" 
                 << configFilePath << "' was not found.";
    }
}

}
