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

#ifndef RASMGR_X_SRC_USER_HH_
#define RASMGR_X_SRC_USER_HH_

#include <string>
#include <map>

#include "userdatabaserights.hh"
#include "useradminrights.hh"

namespace rasmgr
{

class UserProto;

/**
 * @brief The User class holds information about a user of the rasdaman system.
 */
class User
{
public:
    /**
     * Initialize a new instance of the User class.
     * @param name Name of the user
     * @param password User's encrypted password.
     * @param rights The user's rights on this rasdaman instance.
     */
    User(std::string name, std::string password, const UserDatabaseRights& defaultDbRights, const UserAdminRights& adminRights);

    virtual ~User();

    /**
     * @return UserAdminRights object that is a copy of the user's rights of administrating
     * this rasdaman instance.
     */
    UserAdminRights getAdminRights() const;
    void setAdminRights(const UserAdminRights& adminRights);

    const std::string& getName() const;
    void setName(const std::string& name);

    const std::string& getPassword() const;
    void setPassword(const std::string& password);

    const UserDatabaseRights& getDefaultDbRights() const;
    void setDefaultDbRights(const UserDatabaseRights& defaultDbRights);

    /**
     * @brief Create a User object from its protobuf representation.
     * This function is useful for reading data from a file.
     *
     * @param user protobuf representation of the User
     * @return rasmgr::User
     */
    static User parseFromProto(const UserProto& user);


    /**
     * @brief Serialize the data of this object to protobuf representation.
     *
     * @param user Object to serialize.
     * @return rasmgr::UserProto
     */
    static UserProto serializeToProto(const User& user);

private:
    std::string name;
    std::string password;
    UserDatabaseRights defaultDbRights;
    UserAdminRights adminRights;
};

} /* namespace rasmgr */

#endif /* RASMGR_X_SRC_USER_HH_ */
