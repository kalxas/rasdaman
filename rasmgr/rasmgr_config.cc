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
 * SOURCE: rasmgr_config.cc
 *
 * MODULE: rasmgr
 * CLASS:  Configuration, RasmgrLicense
 *
 * PURPOSE:
 *   Config info from commandline, environment and license
 *
 * COMMENTS:
 *   none
 *
*/

using namespace std;

#include "config.h"
#include "globals.hh"       // DEFAULT_HOSTNAME, DEFAULT_PORT, RASMGR_CONF_FILE

#include "rasmgr_config.hh"
#include "rasmgr.hh"
#include "rasmgr_host.hh"
#include "rasmgr_dbm.hh"
#include "rasmgr_srv.hh"
#include "ras_crypto.hh"
#include "rasmgr_users.hh"
#include "rasmgr_rascontrol.hh"

#include <sys/stat.h>
#include <sys/types.h>      // mkdir()

#include <stdlib.h>     // mkstemp()
#include <iomanip>
#include <time.h>
#include <bits/list.tcc>
#ifdef __APPLE__
#include <sys/stat.h> // mkdir()
#endif

#include "raslib/rminit.hh"
#include "debug/debug-srv.hh"

#include "raslib/log_config.hh"
#include <easylogging++.h>

extern bool hostCmp(const char* h1, const char* h2);


Configuration::Configuration():
    cmlInter(CommandLineParser::getInstance()),
    cmlHelp(cmlInter.addFlagParameter('h', RASMGRCMD_HELP, "print this help")),
    cmlHostName(cmlInter.addStringParameter(CommandLineParser::noShortName, "hostname", "<name> the advertized host name (master only, default: same as UNIX command 'hostname')")),
    cmlPort(cmlInter.addLongParameter(CommandLineParser::noShortName, "port", "<port> listen port number", DEFAULT_PORT)),
    cmlPollFrequ(cmlInter.addLongParameter(CommandLineParser::noShortName, "poll", "<poll> polling timeout (in seconds) for rasmgr listen port", DEFAULT_POLLING_FREQUENCY)),
    cmlName(cmlInter.addStringParameter(CommandLineParser::noShortName, "name", "<name> symbolic name of this rasmgr (slave only, default: the host name)")),
    cmlQuiet(cmlInter.addFlagParameter('q', CommandLineParser::noLongName, "quiet: don't log requests (default: log requests to stdout)")),
#ifdef RMANDEBUG    // was: NO_OFFICIAL_RELEASE
    cmlTest(cmlInter.addFlagParameter(CommandLineParser::noShortName, "test", "test mode")),
    cmlDSup(cmlInter.addFlagParameter(CommandLineParser::noShortName, "dsup", "debug mode")),
    cmlRandTest(cmlInter.addFlagParameter(CommandLineParser::noShortName, "rgt",  "random generator test")),
    cmlRth(cmlInter.addFlagParameter(CommandLineParser::noShortName, "rth", "disable rthl test")),
    cmlMultiWT(cmlInter.addFlagParameter(CommandLineParser::noShortName, "amw", "allow multiple write transactions")),
#endif          // RMANDEBUG
    cmlLog(cmlInter.addStringParameter('l', "log", "<log-file> log is printed to <log-file>\n\t\tif <log-file> is stdout , log output is printed to standard out", "log/rasmgr.<pid>.log"))
{

    int ghnResult = gethostname(hostName, sizeof(hostName));
    if (ghnResult != 0) // cannot get hostname?
    {
        int ghnErrno = errno;
        LERROR << "Error: cannot get hostname of my machine: error " << ghnErrno << "; will use '" << DEFAULT_HOSTNAME << "' as heuristic.";
        strcpy(hostName, DEFAULT_HOSTNAME);
    }
    strcpy(publicHostName, hostName);
    listenPort = DEFAULT_PORT;

    pollFrequency = DEFAULT_POLLING_FREQUENCY;

    configFileName[0] = 0;

    if (sizeof(configFileName) < strlen(CONFDIR) + strlen(RASMGR_CONF_FILE) + 2)
    {
        LERROR << "Error: configuration path length exceeds system limits: '" << CONFDIR << "/" << RASMGR_CONF_FILE << "'";
        return;
    }
    sprintf(configFileName, "%s/%s", CONFDIR, RASMGR_CONF_FILE);
    altConfigFileName[0] = '\0';

    testModus    = false;
    debugSupport = false;
    rtHlTest     = true;  // by default RasMgr tests at runtime if it's the only one
    allowMultiWT = false; // rasmgr allow multiple write transactions for a db as default

}

bool Configuration::readConfigFile()
{
    //insert internal host, it's not in config file
    hostmanager.insertInternalHost();

    char inBuffer[MAXMSG];
    char outBuffer[MAXMSG];
    bool result = true;
    bool fileIsOpen = false;

    LDEBUG << "Inspecting config file " << configFileName << "...";

    std::ifstream ifs(configFileName);      // open config file

    if (ifs)
    {
        fileIsOpen = true;
    }
    else
    {
        LDEBUG << "Configuration::readConfigFile: cannot open config file." ;
        LWARNING << "Warning: cannot open config file " << configFileName;
        fileIsOpen = false;
    }
    result = true;              // was: false, but I want to allow a missing file

    if (fileIsOpen)
    {
        authorization.startConfigFile();

        while (! ifs.eof())         // was: while(1), I simplified this
            // processRequest() will get an additional empty line at eof, but this is harmless
        {
            ifs.getline(inBuffer, MAXMSG);
            // if(!strlen(inBuffer) && ifs.eof())   // FIXME: what happens if last line in file is empty?
            // {
            // LDEBUG << "Configuration::readConfigFile: strlen(inBuffer)=" << strlen(inBuffer) << ", eof=" << ifs.eof();
            // break;
            // }

            LDEBUG << "Configuration::readConfigFile: processing line: " << inBuffer ;
            rascontrol.processRequest(inBuffer, outBuffer);
        }

        authorization.endConfigFile();

        ifs.close();                // close config file handle
    }

    if (result == true && fileIsOpen)
    {
        LDEBUG << "ok";
    }

    return true;
}

// return name of alternate config file;
// takes value from preceding saveAltConfigFile() call.
const char* Configuration::getAltConfigFileName()
{
    return altConfigFileName;
}

// in future this is not used directly, but through saveOrigConfigFile() and saveAltConfigFile() wrappers below
bool Configuration::saveConfigFile()
{
    std::ofstream ofs(configFileName);
    if (!ofs)
    {
        return false;
    }

    ofs << "# rasmgr config file (v1.1)" << std::endl;
    ofs << "# warning: do not edit this file, it may be overwritten by rasmgr!" << std::endl;
    ofs << "#" << std::endl;

    int i;
    //serverhosts
    for (i = 0; i < hostmanager.countHosts(); i++)
    {
        ServerHost& xx = hostmanager[i];

        if (i > 0)
        {
            ofs << "define host " << xx.getName() << " -net " << xx.getNetworkName() << " -port " << xx.getListenPort() << std::endl;
        }
        else
        {
            //by default the master RasMgr is init with the hostname as name => if we have to we change it here
            if (! hostCmp(xx.getName(), config.getHostName()))
            {
                ofs << "change host " << config.getHostName() << " -name " << xx.getName() << std::endl;
            }

            if (xx.useLocalHost() == false)
            {
                ofs << "change host " << xx.getName() << " -uselocalhost off" << std::endl;
            }
        }
    }
    //databaseHosts
    for (i = 0; i < dbHostManager.countHosts(); i++)
    {
        DatabaseHost& xx = dbHostManager[i];
        ofs << "define dbh " << xx.getName() << " -connect " << xx.getConnectionString();
        if (strlen(xx.getUser()) > 0)
        {
            ofs << " -user " << xx.getUser();
        }
        if (strlen(xx.getPasswd()) > 0)
        {
            ofs << " -passwd " << xx.getPasswd();
        }
        ofs << std::endl;
    }
    //rasservers
    for (i = 0; i < rasManager.countServers(); i++)
    {
        RasServer& xx = rasManager[i];

        ofs << "define srv " << xx.getName() << " -host " << xx.getHostName() << " -type " << xx.getType();
        if (xx.getType() == SERVERTYPE_FLAG_HTTP)
        {
            ofs << " -port " << xx.getPort();
        }
        else
        {
            ofs << " -port 0x" << std::hex << xx.getPort() << std::dec;
        }
        if (xx.isConnectedToDBHost())
        {
            ofs << " -dbh " << xx.getDBHostName();
        }
        ofs << std::endl;

        ofs << "change srv " << xx.getName() << " -countdown " << xx.getCountDown();

        if (strcmp(xx.getExecutableName(), RASEXECUTABLE))
        {
            ofs << " -exec " << xx.getExecutableName();
        }

        ofs << " -autorestart " << (xx.isAutoRestart() ? "on" : "off") << " -xp " << xx.getExtraParams() << std::endl;
    }

    //databases
    for (i = 0; i < dbManager.countDatabases(); i++)
    {
        Database& xx = dbManager[i];

        for (int j = 0; j < xx.countConnectionsToDBHosts(); j++)
        {
            ofs << "define db " << xx.getName() << " -dbh " << xx.getDBHostName(j) << std::endl;
        }
    }
    unsigned int j;
    for (j = 0; j < outpeers.size(); j++)
    {
        ofs << "define outpeer " << outpeers[j] << " -port " << outports[j] << std::endl;
    }

    for (j = 0; j < inpeers.size(); j++)
    {
        ofs << "define inpeer " << inpeers[j] << std::endl;
    }

    ofs.close();        // this was missing, therefore sometimes the config file was cleared -- PB 2003-jun-06

    return true;
} // saveConfigFile()

// save config file at original place, i.e., under the name of configFile
bool Configuration::saveOrigConfigFile()
{
    bool result = saveConfigFile();

    return result;
}

// save configuration file in another file, same dir as config file
bool Configuration::saveAltConfigFile()
{
    bool result = true;
    char origFileName[ sizeof(configFileName) ];        // temp copy of origFileName

    // save original file name
    (void) strcpy(origFileName, configFileName);

    // build temp file by appending a unique string
    (void) strcpy(altConfigFileName, configFileName);
    (void) strcat(altConfigFileName, ".XXXXXX");         // 6 * 'X', see man mkstemp()

    int altFile = mkstemp(altConfigFileName);           // replaces the Xs by unique string
    if (altFile < 0)                                    // error in creating file name
    {
        int tempError = errno;
        LDEBUG << "Configuration::saveAltConfigFile: error creating alternate file name: " << strerror(tempError);
        result = false;
    }

    if (result == true)
    {
        // now we have a valid + open file, but we can't use it like that, because we open down below.
        // so close it again, being happy that we have a valid file name. bad hack, though.
        int closeResult = close(altFile);
        if (closeResult != 0)
        {
            LDEBUG << "Configuration::saveAltConfigFile: error in temporary closing file, ignoring that.";
        }
    }

    if (result == true)
    {
        (void) strcpy(configFileName, altConfigFileName);   // set file to be written to alternate name
        result = saveConfigFile();                      // save file, name has been substituted successfully
        (void) strcpy(configFileName, origFileName);    // restore original config file name
    }

    return result;
}

bool Configuration::interpretArguments(int argc, char** argv, __attribute__((unused)) char** envp)
{
    bool result = true;
    //errorCode=0;

    //process command line
    try
    {
        cmlInter.processCommandLine(argc, argv);
    }
    catch (CmlException& err)
    {
        LERROR << "Error parsing command line: " << err.what();
        printHelp();
        result = false;
    }

    initLogFiles();

    verbose = true;         // by default, be verbose
    if ((result == true) && cmlQuiet.isPresent())
    {
        // debugOutput = true;          // done via the above macro
        verbose = false;            // only minimum messages
        result = true;
    }

    if ((result == true) && cmlHelp.isPresent())
    {
        printHelp();
        result = false;
    }

    if ((result == true) && cmlHostName.isPresent())
    {
        if (sizeof(hostName) > strlen(cmlHostName.getValueAsString()))
        {
            strcpy(publicHostName, cmlHostName.getValueAsString());
        }
        else
        {
            LDEBUG << "Error: host name exceeds length limit of " << sizeof(hostName) << " characters.";
            result = false;
        }
    }

    if ((result == true) && cmlPort.isPresent())
    {
        try
        {
            listenPort = cmlPort.getValueAsLong();
        }
        catch (CmlException& err)
        {
            LDEBUG << "Error converting port parameter " << cmlPort.getLongName() << " to integer: " << err.what();
            result = false;
        }
    }

    if ((result == true) && cmlPollFrequ.isPresent())
    {
        try
        {
            pollFrequency = cmlPollFrequ.getValueAsLong();
        }
        catch (CmlException& err)
        {
            LDEBUG << "Error converting " << cmlPollFrequ.getLongName() << " to integer: " << err.what();
            result = false;
        }
        if (result == true && pollFrequency <= 0)
        {
            LDEBUG << "Error: poll frequency must be a positive integer.";
            result = false;
        }
    }

#ifdef RMANDEBUG    // was: NO_OFFICIAL_RELEASE
    testModus = cmlTest.isPresent();
    debugSupport = cmlDSup.isPresent();
    rtHlTest = cmlRth.isPresent();

    if ((result == true) && cmlRandTest.isPresent())
    {
        LINFO << "Random generator test..." << (randomGenerator.insideTest() ? "PASSED" : "FAILED");
        result = false;
    }

    allowMultiWT = cmlMultiWT.isPresent();

#endif       // RMANDEBUG

    return result;
}

bool Configuration::allowMultipleWriteTransactions()
{
    return allowMultiWT;
}

bool Configuration::isDebugSupport()
{
    return debugSupport;
}

bool Configuration::isVerbose()
{
    return verbose;
}

bool Configuration::isLogToStdOut()
{
    return logToStdOut;
}

bool Configuration::isTestModus()
{
    return testModus;
}

const char* Configuration::getHostName()
{
    return hostName;
}

const char* Configuration::getPublicHostName()
{
    return publicHostName;
}

int Configuration::getListenPort()
{
    return listenPort;
}

int Configuration::getPollFrequency()
{
    return pollFrequency;
}

void Configuration::printHelp()
{
    LINFO << "Usage: rasmgr [options]";
    LINFO << "Options:";
    cmlInter.printHelp();
    return;
}

void Configuration::printStatus()
{
    LINFO << "rasmgr configuration parameter settings:";
    LINFO << "   symb name   = " << publicHostName;
    LINFO << "   hostname    = " << publicHostName;
    LINFO << "   port        = " << listenPort    ;
    LINFO << "   poll        = " << pollFrequency ;
    LINFO << "   quiet     = "   << (!verbose);
    LINFO << "   log file  = "   << logFileName;
    return;
}

void Configuration::initLogFiles()
{
    if (cmlLog.isPresent())
    {
        if (strcasecmp(cmlLog.getValueAsString(), "stdout") != 0)
        {
            logFileName = cmlLog.getValueAsString();
            logToStdOut = false;
        }
        else
        {
            logFileName = "stdout";
            logToStdOut = true;
        }
    }
    else     // default
    {
        logFileName = makeLogFileName(LOG_SUFFIX);
        logToStdOut = false;
    }
    // Default logging configuration
    LogConfiguration defaultConf(CONFDIR, SERVER_LOG_CONF);
    defaultConf.configServerLogging(logFileName);

    cout << "rasmgr log file is: " << logFileName << endl;

    if (logToStdOut == true)
    {
        RMInit::logOut.rdbuf(cout.rdbuf());
        RMInit::dbgOut.rdbuf(cout.rdbuf());
        RMInit::bmOut.rdbuf(cout.rdbuf());
    }
    else
    {
        if (RMInit::logFileOut.is_open())
        {
            RMInit::logFileOut.close();
        }

        RMInit::logFileOut.open(logFileName, ios::out | ios::app);
        RMInit::logOut.rdbuf(RMInit::logFileOut.rdbuf());
        RMInit::dbgOut.rdbuf(RMInit::logFileOut.rdbuf());
        RMInit::bmOut.rdbuf(RMInit::logFileOut.rdbuf());
    }
}

const char* Configuration::makeLogFileName(const char* desExt)
{
    static char buffer[ FILENAME_MAX ];
    int pid = getpid();
    mkdir(LOGDIR, S_IRWXU + S_IRGRP + S_IXGRP + S_IROTH + S_IXOTH); // create if not exist, rwxr-xr-x
    int pathLen = snprintf(buffer, FILENAME_MAX, "%s/%s.%06d.%s", LOGDIR, RASMGR_LOG_PREFIX, pid, desExt);
    if (pathLen >= FILENAME_MAX)
    {
        buffer[FILENAME_MAX - 1] = '\0'; // force-terminate string before printing
        cerr << "Warning: path name longer than allowed by OS, likely log file cannot be written: " << buffer << endl;
    }
    return buffer;
}
