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

#include "networkresolverfactory.hh"

#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sstream>
#include <string.h>
#include <unistd.h>
#include <stdexcept>

#include "ipv4resolver.hh"
#include "ipv6resolver.hh"


namespace common
{

std::shared_ptr<AbstractNetworkResolver> NetworkResolverFactory::getNetworkResolver(const std::string& host, std::uint32_t port)
{
    std::shared_ptr<AbstractNetworkResolver> resolver;

    struct addrinfo hint, *res = NULL;

    memset(&hint, '\0', sizeof hint);

    hint.ai_family = PF_UNSPEC;
    hint.ai_socktype = SOCK_STREAM;
    hint.ai_flags = AI_PASSIVE;

    if (getaddrinfo(host.c_str(), NULL, &hint, &res) != 0)
    {
        std::ostringstream error;
        error << "Invalid host: " << host;
        throw std::runtime_error(error.str());
    }

    if (res->ai_family == AF_INET)
    {
        struct sockaddr_in* ipv4 = reinterpret_cast<struct sockaddr_in*>(res->ai_addr);
        resolver.reset(new IPv4Resolver(ipv4->sin_addr, port));
    }
    else if (res->ai_family == AF_INET6)
    {
        struct sockaddr_in6* ipv6 = reinterpret_cast<struct sockaddr_in6*>(res->ai_addr);
        resolver.reset(new IPv6Resolver(ipv6->sin6_addr, port));
    }
    else
    {
        // free the address info resources before throwing an exception
        freeaddrinfo(res);
        std::ostringstream error;
        error << "Address famility not supported for host: " << host;
        throw std::runtime_error(error.str());
    }

    freeaddrinfo(res);
    return resolver;
}

} // namespace common
