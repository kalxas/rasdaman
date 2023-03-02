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

#ifndef RASMGR_X_SRC_EXCEPTIONS_DUPLICATEDBSESSIONEXCEPTION_HH
#define RASMGR_X_SRC_EXCEPTIONS_DUPLICATEDBSESSIONEXCEPTION_HH

#include "common/exceptions/runtimeexception.hh"

namespace rasmgr
{
/**
 * @brief The DuplicateDbSessionException class defines a type of object to be thrown as exception.
 *  It reports errors that arise because a user tried to add the same client session twice to the same database.
 */
class DuplicateDbSessionException : public common::RuntimeException
{
public:
    /**
     * @brief DuplicateDbSessionException
     * @param dbName Name of the database that was being modified.
     * @param sessionUID String uniquely identifying the client session.
     */
    DuplicateDbSessionException(const std::string &dbName, std::uint32_t sessionUID);

    /**
     * @brief DuplicateDbSessionException
     * @param dbName Name of the database that was being modified.
     * @param clientId client id
     * @param sessionId client session id
     */
    DuplicateDbSessionException(const std::string &dbName, std::uint32_t clientId, std::uint32_t sessionId);

    virtual ~DuplicateDbSessionException() noexcept;
};
}  // namespace rasmgr

#endif  // RASMGR_X_SRC_EXCEPTIONS_DUPLICATEDBSESSIONEXCEPTION_HH
