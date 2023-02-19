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

#ifndef NETWORKUTILS_HH
#define NETWORKUTILS_HH

#include <string>

/**
 * Namespace containing utility functions for network interactions
 */
namespace networkutils
{

//The maximum length of a hostname. See https://tools.ietf.org/html/rfc1034
static const int MAX_HOSTNAME_SIZE = 256;

/**
 * Returns the cannonical host name of the machine that is running this code
 * @return a string containing the host name
 */
std::string getLocalHostName();

/**
 * @return true if the hostName:port is valid, false otherwise.
 */
bool isAddressValid(const std::string &hostName, std::int32_t port);

}



#endif  /* NETWORKUTILS_HH */

