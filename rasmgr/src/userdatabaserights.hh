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

#ifndef RASMGR_X_SRC_USERDATABASERIGHTS_HH_
#define RASMGR_X_SRC_USERDATABASERIGHTS_HH_

namespace rasmgr
{

class UserDatabaseRightsProto;

/**
 * @brief The UserDatabaseRights class represents the rights the user has on all
 * the databases in the rasdaman system.
 */
class UserDatabaseRights
{
public:
    UserDatabaseRights(bool readAccess, bool writeAccess = false);

    /**
     * Initialize a new instance of the UserDatabaseRights
     * class with read and write access set to false.
     */
    UserDatabaseRights() = default;

    virtual ~UserDatabaseRights() = default;

    bool hasReadAccess() const;
    void setReadAccess(bool readAccess);

    bool hasWriteAccess() const;
    void setWriteAccess(bool writeAccess);

    static UserDatabaseRights parseFromProto(const UserDatabaseRightsProto &rights);

    static UserDatabaseRightsProto serializeToProto(const UserDatabaseRights &userDatabaseRights);
private:
    bool readAccess{false};
    bool writeAccess{false};
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_USERDATABASERIGHTS_HH_ */
