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

#include "rasmgr_x/src/messages/rasmgrmess.pb.h"
#include "useradminrights.hh"

namespace rasmgr
{

UserAdminRights::UserAdminRights()
{
    this->accessControlRights = false;
    this->infoRights = false;
    this->serverAdminRights = false;
    this->systemConfigRights = false;
}

UserAdminRights::~UserAdminRights()
{}

bool UserAdminRights::hasAccessControlRights() const
{
    return this->accessControlRights;
}

void UserAdminRights::setAccessControlRights(bool hasAccessControlRights)
{
    this->accessControlRights = hasAccessControlRights;
}

bool UserAdminRights::hasInfoRights() const
{
    return this->infoRights;
}

void UserAdminRights::setInfoRights(bool hasInfoRights)
{
    this->infoRights = hasInfoRights;
}

bool UserAdminRights::hasServerAdminRights() const
{
    return this->serverAdminRights;
}

void UserAdminRights::setServerAdminRights(bool hasServerAdminRights)
{
    this->serverAdminRights = hasServerAdminRights;
}

bool UserAdminRights::hasSystemConfigRights() const
{
    return this->systemConfigRights;
}

void UserAdminRights::setSystemConfigRights(bool hasSystemConfigRights)
{
    this->systemConfigRights = hasSystemConfigRights;
}

UserAdminRights UserAdminRights::parseFromProto(
    const UserAdminRightsProto& rights)
{

    UserAdminRights adminRights;

    adminRights.accessControlRights = rights.has_access_control_rights()
                                      && rights.access_control_rights();
    adminRights.infoRights = rights.has_info_rights() && rights.info_rights();
    adminRights.serverAdminRights = rights.has_server_admin_rights()
                                    && rights.server_admin_rights();
    adminRights.systemConfigRights = rights.has_system_config_rights()
                                     && rights.system_config_rights();

    return adminRights;
}

UserAdminRightsProto UserAdminRights::serializeToProto(
    const UserAdminRights& rights)
{
    UserAdminRightsProto protoRights;

    protoRights.set_access_control_rights(rights.accessControlRights);
    protoRights.set_info_rights(rights.infoRights);
    protoRights.set_server_admin_rights(rights.serverAdminRights);
    protoRights.set_system_config_rights(rights.systemConfigRights);

    return protoRights;
}

}
