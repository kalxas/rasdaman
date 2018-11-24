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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef RASMGR_X_SRC_EXCEPTIONS_INEXISTENTINPEEREXCEPTION_HH_
#define RASMGR_X_SRC_EXCEPTIONS_INEXISTENTINPEEREXCEPTION_HH_

#include "common/exceptions/rasexceptions.hh"

namespace rasmgr
{
/**
 * @brief The InexistentInPeerException class defines a type of object to be thrown as exception.
 *  It reports errors that arise because a user tried to operate on a InPeer that does not exist.
 */

class InexistentInPeerException: public common::MissingResourceException
{
public:
    InexistentInPeerException(const std::string& hostName);

    virtual ~InexistentInPeerException() noexcept;
};

}

#endif // INEXISTENTOUTPEEREXCEPTION_HH
