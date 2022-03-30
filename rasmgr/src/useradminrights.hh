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

#ifndef RASMGR_X_SRC_USERADMINRIGHTS_HH_
#define RASMGR_X_SRC_USERADMINRIGHTS_HH_

namespace rasmgr
{

class UserAdminRightsProto;

/**
 * @brief The UserAdminRights class represents the administration rights of the
 * user. @see Installation and Administration Guide
 */
class UserAdminRights
{
public:
    /**
     * Initialize a new instance of the UserAdminRights object
     * with NO rights
     */
    UserAdminRights() = default;

    virtual ~UserAdminRights() = default;

    bool hasAccessControlRights() const;
    void setAccessControlRights(bool hasAccessControlRights);

    bool hasInfoRights() const;
    void setInfoRights(bool hasInfoRights);

    bool hasServerAdminRights() const;
    void setServerAdminRights(bool hasServerAdminRights);

    bool hasSystemConfigRights() const;
    void setSystemConfigRights(bool hasSystemConfigRights);

    static UserAdminRights parseFromProto(const UserAdminRightsProto &rights);
    static UserAdminRightsProto serializeToProto(const UserAdminRights &rights);

private:
    bool systemConfigRights{false};/*!< Rights to configure the system*/
    bool accessControlRights{false};/*!< Rights to modify access control*/
    bool serverAdminRights{false};/*!< Rights to administer servers*/
    bool infoRights{false};/*!< Rights to list information about this rasdaman instance*/
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_USERADMINRIGHTS_HH_ */
