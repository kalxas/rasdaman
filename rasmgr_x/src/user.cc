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

#include "messages/rasmgrmess.pb.h"
#include "user.hh"

namespace rasmgr
{
using std::pair;
using std::map;
using std::string;

User::User(std::string name, std::string password,const UserDatabaseRights& defaultDbRights, const UserAdminRights& adminRights) :
    name(name), password(password), defaultDbRights(defaultDbRights), adminRights(adminRights)
{}

User::~User()
{}

UserAdminRights User::getAdminRights() const
{
    return this->adminRights;
}

void User::setAdminRights(const UserAdminRights& adminRights)
{
    this->adminRights = adminRights;
}

void User::setPassword(const std::string& password)
{
    this->password = password;
}

const std::string& User::getName() const
{
    return name;
}

void User::setName(const std::string& name)
{
    this->name = name;
}

const std::string& User::getPassword() const
{
    return password;
}

const UserDatabaseRights& User::getDefaultDbRights() const
{
    return defaultDbRights;
}

void User::setDefaultDbRights(const UserDatabaseRights& defaultDbRights)
{
    this->defaultDbRights = defaultDbRights;
}

User User::parseFromProto(const UserProto& user)
{
    UserDatabaseRights defaultDbRights = UserDatabaseRights::parseFromProto(user.default_db_rights());
    UserAdminRights adminRights = UserAdminRights::parseFromProto(user.admin_rights());
    User result(user.name(), user.password(), defaultDbRights, adminRights);

    return result;
}

UserProto User::serializeToProto(const User& user)
{
    UserProto result;

    result.set_name(user.getName());
    result.set_password(user.getPassword());
    result.mutable_admin_rights()->CopyFrom(UserAdminRights::serializeToProto(user.adminRights));
    result.mutable_default_db_rights()->CopyFrom(UserDatabaseRights::serializeToProto(user.defaultDbRights));

    return result;
}

} /* namespace rasmgr */
