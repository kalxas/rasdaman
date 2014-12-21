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

#ifndef RASCONTROL_X_SRC_COMMANDEXECUTOR_HH
#define RASCONTROL_X_SRC_COMMANDEXECUTOR_HH

#include <boost/shared_ptr.hpp>

#include "controlrasmgrcomm.hh"

namespace rascontrol
{

class CommandExecutor
{
public:
    /**
      * @brief CommandExecutor Initialize an instance of the CommandExecutor class.
      * @param communication Communication object used to send messages to the server.
      */
    CommandExecutor(boost::shared_ptr<ControlRasMgrComm> communication);

    /**
     * @brief executeCommand Parse the command given as a string and execute it.
     * If the command is invalid or if the communication with the server fails, an exception is thrown.
     * @param command command to execute
     * @param reply message sent back by the server that will be displayed to the user
     * @throw std::exception if the command is invalid or communication with the server fails.
     */
    void executeCommand(const std::string& command, std::string& reply);

    /**
     * @brief isExitCommand Check if the string represents an exit command.
     * @param command
     * @return TRUE if the command is equal to 'exit', 'quit' or 'bye', FALSE otherwise.
     */
    bool isExitCommand(std::string command);

    /**
     * Send a login message and check if the client credentials are valid.
     * @param reply The reply from the rasmgr
     */
    void executeLogin(std::string& reply);

private:
    boost::shared_ptr<ControlRasMgrComm> communication; /*!< Communication object used to forward messages to the server and receive replies */
};

}
#endif // COMMANDEXECUTOR_HH
