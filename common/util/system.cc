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

#include "system.hh"
#include <sys/wait.h>
#include <signal.h>

bool common::SystemUtil::isProcessAlive(pid_t processId)
{
    // If processId died, its pid will be returned by waitpid(), otherwise the
    // returned pid is 0; WNOHANG prevents the call from blocking.
    int status;
    auto pid = waitpid(processId, &status, WNOHANG);
    if (pid == 0)
    {
        // double check if processId exists; return value 0 means yes
        return kill(processId, 0) == 0;
    }
    else
    {
        // the process has died
        return false;
    }
}
