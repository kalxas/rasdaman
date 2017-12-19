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

#ifndef RASMGR_X_SRC_CONTROLCOMMANDEXECUTOR_HH_
#define RASMGR_X_SRC_CONTROLCOMMANDEXECUTOR_HH_

#include <string>

#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>

#include "rascontrolgrammar.hh"

namespace rasmgr
{

class RasControl;

/**
 * @brief The ControlCommandExecutor class Executes rascontrol string commands
 */
class ControlCommandExecutor
{
public:
    ControlCommandExecutor(boost::shared_ptr<RasControl> control);

    virtual ~ControlCommandExecutor();

    /**
     * Execute a rascontrol command and return a reply. The command is only executed if the user has valid credentials
     * and the syntax of the command is correct.
     * @param userName Name of the user requesting that this command is executed
     * @param userPass Password of the user requesting that this command is executed
     * @return Message that will be displayed to the user.
     */
    std::string executeCommand(const std::string& command, const std::string& userName, const std::string& userPass);

    /**
     * Execute a rascontrol command with super-user privilege.
     * This method must be used for executing commands of verified users.
     * @param command Command that conforms to the rascontrol grammar.
     * @return Empty string if the command is successful, error message otherwise;
     */
    std::string sudoExecuteCommand(const std::string& command);

private:
    RasControlGrammar grammar;
    boost::shared_ptr<RasControl> rascontrol;
    boost::mutex mut;

    /**
     * @brief canRunCommand Check if the user can run the command.
     * @param userName
     * @param password
     * @param command
     * @return
     */
    bool canRunCommand(const std::string& userName, const std::string& password, const std::string& command);
};

}
#endif /* RASMGR_X_SRC_CONTROLCOMMANDEXECUTOR_HH_ */
