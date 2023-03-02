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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009,2010,2011,2012,2013,2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#include <iostream>
#include <cstring>

#include "../../include/globals.hh"

#include "rascontrolconstants.hh"
#include "rascontrolconfig.hh"

namespace rascontrol
{

using std::string;

RasControlConfig::RasControlConfig()
    : cmlInter(CommandLineParser::getInstance()),
      cmlHelp(cmlInter.addFlagParameter('h', "help", "this help")),
      cmlHost(cmlInter.addStringParameter(CommandLineParser::noShortName, "host", "<name> name of host where master rasmgr runs", DEFAULT_HOSTNAME)),
      cmlPort(cmlInter.addLongParameter(CommandLineParser::noShortName, "port", "<nnnn> rasmgr port", DEFAULT_PORT)),
      cmlLogin(cmlInter.addFlagParameter('l', "login", "just login prompt, used to set the environment variable RASLOGIN")),
      cmlHist(cmlInter.addStringParameter(CommandLineParser::noShortName, "hist", "<file-name> used to store your commands in file, as help for batch file.")),
      cmlLogFile(cmlInter.addStringParameter(CommandLineParser::noShortName, "log", "<file> easylogging configuration file.")),
      cmlPrompt(cmlInter.addStringParameter(CommandLineParser::noShortName, "prompt", "<nnn> change rascontrol prompt as following:\n\t\t 0 - prompt '>'\n\t\t 1 - prompt 'rasc>'\n\t\t 2 - prompt 'user:host>'", "2")),
      cmlTestLogin(cmlInter.addFlagParameter('t', "testlogin", "test if environment variable RASLOGIN is OK to login")),
      cmlInteractive(cmlInter.addFlagParameter('e', "interactive", "interactive mode, login from environment variable RASLOGIN")),
      cmlQuiet(cmlInter.addFlagParameter('q', "quiet", "quiet, don't print header (default on for -login and -testlogin)")),
      cmlExecute(cmlInter.addFlagParameter('x', "execute", "batch mode, login from environment variable RASLOGIN\n   <rasmgr-cmd>\ta rasmgr command (only in batch mode)\n\t\tif no command if provided, command is read from stdin\n\t\t(used for batch mode with '<inputfile')"))
{
    this->workMode = WKMINTERACTIV;
    this->loginMode = LGIINTERACTIV;
    this->historyFileName = COMMAND_HISTORY_FILE;
    this->promptMode = PROMPTFULL;
    this->isHistoryRequired = false;
    this->quiet = false;
    this->isHelpReq = false;
    this->logConfigFile = "";
}

RasControlConfig::~RasControlConfig()
{
}

bool RasControlConfig::parseCommandLineParameters(int argc, char **argv)
{
    //TODO-AT:FIXME workarround for batch mode commands given to rascontrol
    //in current version it is generated an error if a parameter has many values:
    //e.g. -a c d f where c,d,f are values(not parameters)
    int lastArg = argc;
    string shortX = string("") + CommandLineParser::ShortSign + cmlExecute.getShortName();
    string longX = string("") + CommandLineParser::LongSign + cmlExecute.getLongName();

    for (lastArg = 1; lastArg < argc; lastArg++)
    {
        if ((strcmp(argv[lastArg], shortX.c_str()) == 0) ||
            (strcmp(argv[lastArg], longX.c_str()) == 0))
        {
            lastArg++;
            break;
        }
    }

    try
    {
        if (lastArg != argc)
        {
            cmlInter.processCommandLine(lastArg, argv);
        }
        else
        {
            cmlInter.processCommandLine(argc, argv);
        }
    }
    catch (CmlException &err)
    {
        std::cout << "Command Line Parsing Error:" << std::endl
                  << err.what() << std::endl;
        return false;
    }

    if (cmlHelp.isPresent())
    {
        //we stop processing
        this->isHelpReq = true;
        return true;
    }

    try
    {
        this->rasMgrPort = cmlPort.getValueAsLong();
    }
    catch (CmlException &err)
    {
        std::cout << "Command Line Parsing Error:" << std::endl
                  << err.what() << std::endl;
        return false;
    }

    if (cmlLogFile.isPresent())
    {
        this->logConfigFile = cmlLogFile.getValueAsString();
    }

    this->rasMgrHost = cmlHost.getValueAsString();

    if (cmlLogin.isPresent())
    {
        this->workMode = WKMLOGIN;
        this->quiet = true;
    }

    if (cmlQuiet.isPresent())
    {
        this->quiet = true;
    }

    if (cmlTestLogin.isPresent())
    {
        if (this->workMode == WKMINTERACTIV)
        {
            this->workMode = WKMTESTLOGIN;
            this->loginMode = LGIENVIRONM;
        }
        else
        {
            return paramError();
        }
        this->quiet = true;
    }

    if (cmlInteractive.isPresent())
    {
        if (this->workMode == WKMINTERACTIV)
        {
            this->loginMode = LGIENVIRONM;
        }
        else
        {
            return paramError();
        }
    }

    if (cmlExecute.isPresent())
    {
        if (this->workMode == WKMINTERACTIV)
        {
            this->loginMode = LGIENVIRONM;
            this->workMode = WKMBATCH;

            for (int i = lastArg; i < argc; i++)
            {
                this->command = this->command.append(argv[i]);
                this->command.append(" ");
            }
        }
        else
        {
            return paramError();
        }
    }

    if (cmlHist.isPresent())
    {
        this->isHistoryRequired = true;
        this->historyFileName = cmlHist.getValueAsString();
    }

    try
    {
        this->promptMode = cmlPrompt.getValueAsLong();
    }
    catch (CmlException &err)
    {
        std::cout << "Command Line Parsing Error:" << std::endl
                  << err.what() << std::endl;
        return false;
    }

    if (this->promptMode < PROMPTSING || this->promptMode > PROMPTFULL)
    {
        this->promptMode = PROMPTFULL;
    }

    return true;
}

std::int32_t RasControlConfig::getWorkMode() const
{
    return this->workMode;
}

std::int32_t RasControlConfig::getLoginMode() const
{
    return this->loginMode;
}

std::string RasControlConfig::getRasMgrHost() const
{
    return this->rasMgrHost;
}

bool RasControlConfig::isHistoryRequested() const
{
    return this->isHistoryRequired;
}

bool RasControlConfig::isQuietMode() const
{
    return this->quiet;
}

bool RasControlConfig::isHelpRequested() const
{
    return this->isHelpReq;
}

std::string RasControlConfig::getHistoryFileName() const
{
    return this->historyFileName;
}

std::string RasControlConfig::getPrompt(const std::string &userName)
{
    if (this->prompt.empty())
    {
        switch (this->promptMode)
        {
        case PROMPTSING:
            this->prompt = "> ";
            break;
        case PROMPTRASC:
            this->prompt = "rasc> ";
            break;
        case PROMPTFULL:
            this->prompt = userName + ":" + this->rasMgrHost + "> ";
            break;
        default:
            this->prompt = "> ";
            break;
        }
    }

    return this->prompt;
}

std::string RasControlConfig::getCommand() const
{
    return this->command;
}

std::uint16_t RasControlConfig::getRasMgrPort() const
{
    return this->rasMgrPort;
}

void RasControlConfig::displayHelp() const
{
    std::cout << "Usage: " << std::endl
              << "\trascontrol\t["
              << CommandLineParser::LongSign << cmlHelp.getLongName() << "]["
              << CommandLineParser::LongSign << cmlHost.getLongName() << "<mainhost>]["
              << CommandLineParser::LongSign << cmlPort.getLongName() << " <nn>]["
              << CommandLineParser::LongSign << cmlHist.getLongName() << " <file>]["
              << CommandLineParser::LongSign << cmlPrompt.getLongName() << " <n>]["
              << CommandLineParser::LongSign << cmlQuiet.getLongName() << "]\n\t\t\t["
              << CommandLineParser::LongSign << cmlLogin.getLongName() << "|"
              << CommandLineParser::LongSign << cmlTestLogin.getLongName() << "|"
              << CommandLineParser::LongSign << cmlInteractive.getLongName() << "|"
              << CommandLineParser::LongSign << cmlExecute.getLongName() << " <rasmgr-command>]"
              << std::endl;
    std::cout << "Option description:" << std::endl;
    cmlInter.printHelp();
    std::cout << std::endl;
}

std::string RasControlConfig::getLogConfigFile() const
{
    return logConfigFile;
}

bool RasControlConfig::paramError()
{
    std::cout << "Invalid command line parameters!" << std::endl;
    return false;
}
}  // namespace rascontrol
