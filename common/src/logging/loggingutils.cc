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

#include <string>
#include <fstream>

#include <grpc/support/log.h>

#include <easylogging++.h>

#include "globals.hh"

#include "loggingutils.hh"

namespace common
{
using std::string;

void gpr_replacement_log(gpr_log_func_args *args)
{
    string prefix = "GRPC:";
    string separator = " ";

    switch(args->severity)
    {
    case GPR_LOG_SEVERITY_DEBUG:
    {
        LDEBUG<<prefix<<separator
              <<args->file<<separator
              <<args->line<<separator
              <<args->message;
    }
    break;
    case GPR_LOG_SEVERITY_INFO:
    {
        LINFO<<prefix<<separator
             <<args->file<<separator
             <<args->line<<separator
             <<args->message;
    }
    break;
    case GPR_LOG_SEVERITY_ERROR:
    {
        LERROR<<prefix<<separator
              <<args->file<<separator
              <<args->line<<separator
              <<args->message;
    }
    break;
    default:
    {
        LERROR<<prefix<<separator
              <<args->file<<separator
              <<args->line<<separator
              <<args->message;
    }
    }
}

void common::LoggingUtils::redirectGRPCLogToEasyLogging()
{
    gpr_set_log_function(gpr_replacement_log);
}

easyloggingpp::Configurations LoggingUtils::getClientLoggingConfiguration()
{
    std::string configFilePath(std::string(CONFDIR)+"/"+CLIENT_LOG_CONF);

    if(doesFileExist(configFilePath))
    {
        easyloggingpp::Configurations defaultConf(configFilePath);

        LINFO<<"Loaded log configuration file:"<<configFilePath;

        return defaultConf;
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

#ifdef RMANDEBUG
        defaultConf.set(easyloggingpp::Level::Debug,
                        easyloggingpp::ConfigurationType::Enabled, "true");
        defaultConf.set(easyloggingpp::Level::Trace,
                        easyloggingpp::ConfigurationType::Enabled, "true");
#else
        defaultConf.set(easyloggingpp::Level::Debug,
                        easyloggingpp::ConfigurationType::Enabled, "false");
        defaultConf.set(easyloggingpp::Level::Trace,
                        easyloggingpp::ConfigurationType::Enabled, "false");
#endif
        LWARNING << "Configuration file not found.";

        return defaultConf;
    }
}

easyloggingpp::Configurations LoggingUtils::getServerLoggingConfiguration(const std::string& configFilePath)
{
    if(doesFileExist(configFilePath))
    {
        easyloggingpp::Configurations defaultConf(configFilePath);

        LINFO<<"Loaded log configuration file:"<<configFilePath;

        return defaultConf;
    }
    else        // if configuration file is not found, use inline configuration
    {
        LWARNING << "Configuration file not found.";

        return getDefaultEasyloggingConfig();
    }
}

easyloggingpp::Configurations LoggingUtils::getServerLoggingConfiguration(const std::string& configFilePath, const std::string& outputLogFilePath)
{
    if(doesFileExist(configFilePath))
    {
        easyloggingpp::Configurations defaultConf(configFilePath);
        defaultConf.set(easyloggingpp::Level::All ,
                        easyloggingpp::ConfigurationType::Filename, outputLogFilePath);

        LINFO<<"Loaded log configuration file:"<<configFilePath;

        return defaultConf;
    }
    else        // if configuration file is not found, use inline configuration
    {

        easyloggingpp::Configurations defaultConf = getDefaultEasyloggingConfig();
        defaultConf.set(easyloggingpp::Level::All ,
                        easyloggingpp::ConfigurationType::Filename, outputLogFilePath);
        LWARNING << "Configuration file not found.";

        return defaultConf;
    }
}

bool LoggingUtils::doesFileExist(const std::string &filePath)
{
    std::ifstream f(filePath);
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

easyloggingpp::Configurations LoggingUtils::getDefaultEasyloggingConfig()
{
    easyloggingpp::Configurations defaultConf;
    defaultConf.setToDefault();

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
#ifdef RMANDEBUG
    defaultConf.set(easyloggingpp::Level::Debug,
                    easyloggingpp::ConfigurationType::Enabled, "true");
    defaultConf.set(easyloggingpp::Level::Trace,
                    easyloggingpp::ConfigurationType::Enabled, "true");
#endif

    defaultConf.set(easyloggingpp::Level::Debug,
                    easyloggingpp::ConfigurationType::Enabled, "false");
    defaultConf.set(easyloggingpp::Level::Trace,
                    easyloggingpp::ConfigurationType::Enabled, "false");

    return defaultConf;
}
}
