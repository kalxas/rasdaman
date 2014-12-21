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

#include <fstream>
#include <stdexcept>

#include "../../common/src/logging/easylogging++.hh"

#include "controlrasmgrrasnet.hh"
#include "rascontrol.hh"

namespace rascontrol
{
RasControl::RasControl(RasControlConfig& config, const UserCredentials& userCredentials):config(config),userCredentials(userCredentials)
{
    boost::shared_ptr<ControlRasMgrRasnet> rasnet(new ControlRasMgrRasnet(this->userCredentials, this->config));

    this->comm.reset(new CommandExecutor(rasnet));
}

void RasControl::start()
{
    switch(config.getWorkMode())
    {
    case RasControlConfig::WKMINTERACTIV:
    {
        this->startInteractiveMode();
    }
    break;

    case RasControlConfig::WKMLOGIN:
    {
        this->startLoginOnlyMode();
    }
    break;

    case RasControlConfig::WKMBATCH:
    {
        this->startBatchMode();
    }
    break;

    case RasControlConfig::WKMTESTLOGIN:
    {
        this->startTestLogin();
    }
    break;

    default:
    {
        throw std::runtime_error("Invalid work mode.");
    }
    break;
    }
}

void RasControl::startInteractiveMode()
{
    std::ofstream history;
    bool recordHistory = config.isHistoryRequested();
    std::string serverReply;
    std::string command;
    bool done=false;
    std::string prompt = this->config.getPrompt(this->userCredentials.getUserName());

    try
    {
        this->comm->executeLogin(serverReply);
        std::cout<<"    "<<serverReply<<std::endl;

        if(recordHistory)
        {
            LINFO<<"Using history file." << config.getHistoryFileName();
            history.open(config.getHistoryFileName().c_str(),std::ios::out|std::ios::trunc);
        }

        while(!done)
        {
            LINFO<<"Entering new request cycle."<<prompt;

            const char* commandLine = this->editLine.interactiveCommand(prompt.c_str());
            if(commandLine == NULL || strlen(commandLine)==0)
            {
                continue;
            }

            command = std::string(commandLine);

            LINFO<<"Received command"<<command;
            if(recordHistory)
            {
                history<<command<<std::endl;
            }

            if(this->comm->isExitCommand(command))
            {
                LINFO<<"Closing rascontrol.";
                done=true;
            }
            else
            {
                this->comm->executeCommand(command, serverReply);

                if(!serverReply.empty())
                {
                    std::cout<<"    "<<serverReply<<std::endl;
                }
            }

            LINFO<<"Exiting request cycle.";
        }
    }
    catch(std::exception& ex)
    {
        std::cerr<<ex.what();
    }
    catch(...)
    {
        std::cerr<<"rascontrol::startInteractiveMode failed for an unknown reason.";
    }

    if(recordHistory)
    {
        history.close();
    }

}

void RasControl::startBatchMode()
{
    bool redirStdin = !isatty(STDIN_FILENO);
    bool redirStdout = !isatty(STDOUT_FILENO);
    bool fromCommandLine = !config.getCommand().empty();
    bool printCommand = false;
    bool done = false;
    std::string command;
    std::string serverReply;
    std::string prompt = redirStdin ? "": this->config.getPrompt(this->userCredentials.getUserName());

    LINFO<<"Starting batch mode";

    if( redirStdout )
    {
        printCommand =  true;
    }
    else if(redirStdin)
    {
        printCommand =  true;
    }


    try
    {
        while(!done)
        {
            if(fromCommandLine)
            {
                //After executing this command we exit.
                command = config.getCommand();
                done = true;
            }
            else
            {
                const char* commandLine = this->editLine.fromStdinCommand(prompt.c_str());
                if(commandLine == NULL)
                {
                    throw std::runtime_error("Invalid command.");
                }
                else
                {
                    command = std::string(commandLine);
                }
            }

            LINFO<<"Command:"<<command;
            if(command.empty())
            {
                if(fromCommandLine)
                {
                    throw std::runtime_error("Invalid command.");
                }
                else
                {
                    continue;
                }
            }

            if(printCommand == true)
            {
                std::cout<<config.getPrompt()<<command<<std::endl;
            }

            if(this->comm->isExitCommand(command))
            {
                done = true;
                LINFO<<"Exiting command processing loop.";
            }
            else
            {
                this->comm->executeCommand(command, serverReply);

                if(!serverReply.empty())
                {
                    std::cout<<"    "<<serverReply<<std::endl;
                }
            }
        }
    }
    catch(std::exception& ex)
    {
        std::cerr<<ex.what();
    }
    catch(...)
    {
        std::cerr<<"rascontrol::startBatchMode failed for an unknown reason.";
    }
}

void RasControl::startLoginOnlyMode()
{
    std::cout<<this->userCredentials.getUserName()<<':'<<this->userCredentials.getUserPassword()<<std::endl;
}

void RasControl::startTestLogin()
{
    std::string serverReply;
    try
    {
        this->comm->executeLogin(serverReply);

        std::cout<<serverReply;
    }
    catch(std::exception& ex)
    {
        std::cerr<<ex.what();
    }
    catch(...)
    {
        std::cerr<<"rascontrol::startTestLogin failed for an unknown reason.";
    }
}
}
