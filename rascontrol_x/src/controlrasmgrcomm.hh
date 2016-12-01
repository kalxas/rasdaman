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

#ifndef RASCONTROL_X_SRC_CONTROLRASMGRCOMM_HH
#define RASCONTROL_X_SRC_CONTROLRASMGRCOMM_HH

#include <string>

namespace rascontrol
{
/**
 * @brief The ControlRasMgrComm class Abstract class that should be implemented by a class that wants
 * to process rascontrol commands.
 */
class ControlRasMgrComm
{
public:
    virtual ~ControlRasMgrComm();

    /**
     * Process the given command and return a response that will be displayed to the user.
     * @param command String representing a command from the user
     * @return The answer to the command from RasMgr
     */
    virtual std::string processCommand(const std::string& command) = 0;
};
}

#endif // RASCONTROL_X_SRC_CONTROLRASMGRCOMM_HH
