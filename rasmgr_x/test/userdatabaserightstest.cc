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
#include "rasmgr_x/src/userdatabaserights.hh"
#include "rasmgr_x/src/messages/rasmgrmess.pb.h"
#include "util/testutil.hh"

namespace rasmgr
{
namespace test
{
TEST(UserDatabaseRightsTest, serializeToProto)
{
    using rasmgr::UserDatabaseRights;
    using rasmgr::UserDatabaseRightsProto;
    using rasmgr::test::TestUtil;

    bool readRights = TestUtil::generateRandomElement(false, true);
    bool writeRights = TestUtil::generateRandomElement(false, true);

    UserDatabaseRights dbRights(readRights, writeRights);

    UserDatabaseRightsProto proto = UserDatabaseRights::serializeToProto(dbRights);
    ASSERT_EQ(readRights, proto.read());
    ASSERT_EQ(writeRights, proto.write());
}

TEST(UserDatabaseRightsTest, parseFromProto)
{
    using rasmgr::UserDatabaseRights;
    using rasmgr::UserDatabaseRightsProto;
    using rasmgr::test::TestUtil;

    bool readRights = TestUtil::generateRandomElement(false, true);
    bool writeRights = TestUtil::generateRandomElement(false, true);

    UserDatabaseRightsProto proto;
    proto.set_read(readRights);
    proto.set_write(writeRights);

    UserDatabaseRights dbRights = UserDatabaseRights::parseFromProto(proto);

    ASSERT_EQ(readRights, dbRights.hasReadAccess());
    ASSERT_EQ(writeRights, dbRights.hasWriteAccess());
}

}
}
