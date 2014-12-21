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

#ifndef RASCONTROL_X_SRC_RASCONTROL_HH
#define RASCONTROL_X_SRC_RASCONTROL_HH

#include <boost/scoped_ptr.hpp>

#include "rascontrolconfig.hh"
#include "usercredentials.hh"
#include "commandexecutor.hh"
#include "editline.hh"

namespace rascontrol
{
/**
 * @brief The RasControl class This is the main class of the rascontrol component.
 * It starts the process of communicating with the server.
 */
class RasControl
{
public:
    /**
     * @brief RasControl Initialize a new instance of the RasControl class.
     * @param config
     * @param userCredentials
     */
    RasControl(RasControlConfig& config, const UserCredentials& userCredentials);

    void start();

private:
    RasControlConfig& config; /*!< Reference to the RasControlConfig object used for configuring the behavior of this object*/
    const UserCredentials& userCredentials; /*!< Reference to the user credentials object */
    EditLine editLine; /*!< Object used to retrieve input from the user */

    boost::scoped_ptr<CommandExecutor> comm; /*!< Executor of user commands */

    /**
     * @brief startInteractiveMode Start the interactive mode of the client.
     */
    void startInteractiveMode();

    /**
     * @brief startBatchMode Start the batch mode processing of commands.
     */
    void startBatchMode();

    /**
     * @brief startLoginOnlyMode Print the user's credentials
     */
    void startLoginOnlyMode();

    /**
     * @brief startTestLogin Connect to the rasmgr and check if the login credentials are valid.
     */
    void startTestLogin();
};
}

#endif // RASCONTROL_X_SRC_RASCONTROL_HH
