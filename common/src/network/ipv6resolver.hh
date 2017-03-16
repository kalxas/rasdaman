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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef IPV6RESOLVER_HH
#define IPV6RESOLVER_HH

#include "abstractnetworkresolver.hh"

#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <unistd.h>

namespace common
{

/**
 * @brief The IPv6Resolver class Handler for ipv6 addresss.
 */
class IPv6Resolver : public AbstractNetworkResolver
{
public:
    /**
     * @brief IPv6Resolver Constructor taking an in6_addr and a port.
     * @param address The in6_addr representation of the host.
     * @param port The port.
     */
    IPv6Resolver(const struct in6_addr& addressArg, boost::uint32_t portArg);

    /**
     * @brief isPortBusy Checks if the port is busy.
     * @return True if the port is busy, false otherwise.
     */
    virtual bool isPortBusy();

private:
    /**
     * @brief address The in6_addr representaiton of the host.
     */
    struct in6_addr address;
};

} // namespace common
#endif // IPV6RESOLVER_HH
