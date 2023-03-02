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

#ifndef RASMGR_X_SRC_REMOTECLIENTSESSION_HH
#define RASMGR_X_SRC_REMOTECLIENTSESSION_HH

#include <string>

namespace rasmgr
{
/**
 * Contains information identifying a remote client session: client session id 
 * and a database session id.
 */
class RemoteClientSession
{
public:
    RemoteClientSession(std::uint32_t clientSessionId,
                        std::uint32_t dbSessionId);

    std::uint32_t getClientSessionId() const;
    std::uint32_t getDbSessionId() const;

private:
    std::uint32_t clientSessionId; /*!< String identifying the client session on the remote rasmgr */
    std::uint32_t dbSessionId;     /*!< String identifying the database session on the remote rasmgr*/
};

}  // namespace rasmgr

#endif  // REMOTECLIENTSESSION_HH
