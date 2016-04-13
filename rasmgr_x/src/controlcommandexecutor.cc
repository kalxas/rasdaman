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
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

/**
 * AUTHOR:  rasdaman
 * SOURCE: controlcommandexecutor.cc
 *
 * MODULE:
 * CLASS:
 *
 * PURPOSE:
 *
 * COMMENTS:
*/

#define BOOST_SPIRIT_USE_PHOENIX_V3
#include <boost/spirit/include/qi.hpp>
#include <boost/spirit/include/phoenix.hpp>

#include <easylogging++.h>

#include "rascontrol.hh"

#include "controlcommandexecutor.hh"

namespace rasmgr
{
ControlCommandExecutor::ControlCommandExecutor ( boost::shared_ptr<RasControl> control )
    :grammar ( control ), rascontrol ( control )
{}

ControlCommandExecutor::~ControlCommandExecutor()
{}


std::string ControlCommandExecutor::executeCommand ( const std::string& command, const std::string& userName, const std::string& userPass )
{
    std::string resultMessage;

    if ( this->canRunCommand ( userName, userPass, command ) )
    {
        resultMessage = this->sudoExecuteCommand ( command );
    }
    else
    {
        resultMessage= "The user is not authorized to run this command.";
    }

    return resultMessage;
}

std::string ControlCommandExecutor::sudoExecuteCommand ( const std::string& command )
{
    std::string::const_iterator first = command.begin();
    std::string::const_iterator last = command.end();
    std::string parseResult;
    std::string resultMessage;

    //The grammar must be protected by a mutex.
    boost::unique_lock<boost::mutex> lock ( this->mut );

    bool r = qi::phrase_parse ( first, last, this->grammar, boost::spirit::ascii::space, parseResult );
    if ( r && first==last )
    {
        resultMessage=parseResult;
        LDEBUG<<"The result of rascontrol:"<<resultMessage;
    }
    else
    {
        resultMessage = "Could not execute command.";
        LDEBUG<<resultMessage;
    }

    return resultMessage;
}

bool ControlCommandExecutor::canRunCommand ( const std::string& userName, const std::string& password, const std::string& command )
{
    bool result=false;

    //The grammar must be protected by a mutex.
    boost::unique_lock<boost::mutex> lock ( this->mut );

    if ( this->grammar.isInfoCommand ( command.begin(), command.end() ) )
    {
        LDEBUG<<command << " is a command requesting information.";
        result = this->rascontrol->hasInfoRights ( userName, password );
    }
    else if ( this->grammar.isServerAdminCommand ( command.begin(), command.end() ) )
    {
        LDEBUG<<command << " is a command requesting a change in server status.";
        result = this->rascontrol->hasServerAdminRights ( userName, password );
    }
    else if ( this->grammar.isUserAdminCommand ( command.begin(), command.end() ) )
    {
        LDEBUG<<command << " is a command requesting a user administration.";
        result = this->rascontrol->hasUserAdminRights ( userName, password );
    }
    else if ( this->grammar.isSystemConfigCommand ( command.begin(), command.end() ) )
    {
        LDEBUG<<command << " is a command requesting system configuration.";
        result = this->rascontrol->hasConfigRights ( userName, password );
    }
    else if ( this->grammar.isLoginCommand ( command.begin(), command.end() ) )
    {
        LDEBUG<<command << " is a a login command.";
        result = this->rascontrol->isValidUser ( userName, password );
    }
    else
    {
        //It is safe to run any command that is not one of the commands already checked
        result = true;
        LDEBUG<<"UNKNOWN command:"<<command;
    }

    return result;
}

}
