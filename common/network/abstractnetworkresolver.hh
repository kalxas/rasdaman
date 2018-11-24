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

#ifndef ABSTRACTNETWORKFACTORY_HH
#define ABSTRACTNETWORKFACTORY_HH

#include <boost/cstdint.hpp>

namespace common
{

/**
 * @brief The AbstractNetworkResolver class
 */
class AbstractNetworkResolver
{
public:
    /**
     * @brief isPortBusy Checks whether a network port is busy.
     * @return True if the port is busy, false otherwise.
     */
    virtual bool isPortBusy() = 0;

    virtual ~AbstractNetworkResolver() = default;

protected:
    /**
     * @brief AbstractNetworkResolver Protected constructor for preventing the creation of this kind of objects.
     * @param portArg The port to be checked if it is busy.
     */
    AbstractNetworkResolver(boost::uint32_t portArg);

    /**
     * @brief port The port to be checked if it is busy.
     */
    boost::uint32_t port;
};

}
#endif // ABSTRACTNETWORKFACTORY_HH
