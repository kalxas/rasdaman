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

#include <gtest/gtest.h>
#include "../src/useradminrights.hh"
#include "../src/messages/rasmgrmess.pb.h"
#include "util/testutil.hh"

namespace rasmgr
{
namespace test
{
TEST(UserAdminRightsTest, serializeToProto)
{
    using rasmgr::UserAdminRights;
    using rasmgr::UserAdminRightsProto;
    using rasmgr::test::TestUtil;

    bool controlRights = TestUtil::generateRandomElement(false, true);
    bool infoRights = TestUtil::generateRandomElement(false, true);
    bool adminRights = TestUtil::generateRandomElement(false, true);
    bool configRights = TestUtil::generateRandomElement(false, true);

    UserAdminRights rights;
    rights.setAccessControlRights(controlRights);
    rights.setInfoRights(infoRights);
    rights.setServerAdminRights(adminRights);
    rights.setSystemConfigRights(configRights);

    UserAdminRightsProto proto =  UserAdminRights::serializeToProto(rights);

    ASSERT_EQ(controlRights, proto.access_control_rights());
    ASSERT_EQ(infoRights, proto.info_rights());
    ASSERT_EQ(adminRights, proto.server_admin_rights());
    ASSERT_EQ(configRights, proto.system_config_rights());
}

TEST(UserAdminRightsTest, parseFromProto)
{
    using rasmgr::UserAdminRights;
    using rasmgr::UserAdminRightsProto;
    using rasmgr::test::TestUtil;

    bool controlRights = TestUtil::generateRandomElement(false, true);
    bool infoRights = TestUtil::generateRandomElement(false, true);
    bool adminRights = TestUtil::generateRandomElement(false, true);
    bool configRights = TestUtil::generateRandomElement(false, true);

    UserAdminRightsProto proto;
    proto.set_access_control_rights(controlRights);
    proto.set_info_rights(infoRights);
    proto.set_server_admin_rights(adminRights);
    proto.set_system_config_rights(configRights);

    UserAdminRights rights = UserAdminRights::parseFromProto(proto);

    ASSERT_EQ(controlRights, rights.hasAccessControlRights());
    ASSERT_EQ(infoRights, rights.hasInfoRights());
    ASSERT_EQ(adminRights, rights.hasServerAdminRights());
    ASSERT_EQ(configRights, rights.hasSystemConfigRights());
}

}
}
