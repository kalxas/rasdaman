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

#include "networkutils.hh"
#include "unistd.h"
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <string>

using std::string;

namespace networkutils
{

string getLocalHostName()
{
    char hostname[MAX_HOSTNAME_SIZE];
    hostname[MAX_HOSTNAME_SIZE - 1] = '\0';
    gethostname(hostname, MAX_HOSTNAME_SIZE - 1);
    return string(hostname);
}

bool isAddressValid(const std::string &hostName, int32_t port)
{
    struct addrinfo *addr = NULL;
    std::string portStr = std::to_string(port);
    int ret = getaddrinfo(hostName.c_str(), portStr.c_str(), NULL, &addr);
    if (addr)
    {
        freeaddrinfo(addr);
        addr = NULL;
    }
    return ret == 0;
}

}  // namespace networkutils
