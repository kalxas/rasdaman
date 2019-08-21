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

#include <string>
#include <boost/cstdint.hpp>
#include <boost/shared_ptr.hpp>

#include <gtest/gtest.h>
#include <gmock/gmock.h>
#include <logging.hh>

#include "rasmgr/src/user.hh"
#include "rasmgr/src/useradminrights.hh"
#include "rasmgr/src/userdatabaserights.hh"
#include "rasmgr/src/usermanager.hh"

#include "rasmgr/src/messages/rasmgrmess.pb.h"

#include "util/testutil.hh"

namespace rasmgr
{
namespace test
{
using rasmgr::User;
using rasmgr::UserProto;
using rasmgr::UserAdminRightsProto;
using rasmgr::UserDatabaseRightsProto;
using rasmgr::UserMgrProto;
using rasmgr::test::TestUtil;

class UserManagerTest: public ::testing::Test
{
protected:
    UserManagerTest(): userName("userName"), password("password")
    {}

    UserProto createUser()
    {
        UserProto user;
        user.set_name(userName);
        user.set_password(password);

        UserAdminRightsProto* adminRights = new UserAdminRightsProto();
        UserDatabaseRightsProto* dbRights = new UserDatabaseRightsProto();
        dbRights->set_read(true);
        dbRights->set_write(false);

        user.set_allocated_admin_rights(adminRights);
        user.set_allocated_default_db_rights(dbRights);

        return user;
    }

    std::string userName;
    std::string password;
    rasmgr::UserManager userManager;
};

TEST_F(UserManagerTest, defineUserFailsWhenUserDoesNotHaveName)
{
    UserProto user;
    //Fail because the user does not have a name
    ASSERT_ANY_THROW(userManager.defineUser(user));
}

TEST_F(UserManagerTest, defineUserFailsWhenUserHasInvalidName)
{
    UserProto user;
    user.set_name("");

    //Fail because the user does not have a name
    ASSERT_ANY_THROW(userManager.defineUser(user));
}

TEST_F(UserManagerTest, defineUserSucceedsWhenUserIsValid)
{
    UserMgrProto proto;
    UserProto user;
    user.set_name(userName);

    //Succeed
    ASSERT_NO_THROW(userManager.defineUser(user));

    proto = userManager.serializeToProto();
    ASSERT_EQ(1, proto.users_size());
    ASSERT_EQ(userName, proto.users(0).name());

    //Fail due to duplication
    ASSERT_ANY_THROW(userManager.defineUser(createUser()));
}

TEST_F(UserManagerTest, defineUserFailsWhenUserAlreadyExists)
{
    UserProto user;
    user.set_name(userName);

    //Succeed
    userManager.defineUser(user);

    //Fail due to duplication
    ASSERT_ANY_THROW(userManager.defineUser(createUser()));
}

TEST_F(UserManagerTest, removeUserFailsWhenUserDoesNotExist)
{
    //Fail because the user does not exist
    ASSERT_ANY_THROW(userManager.removeUser(userName));
}

TEST_F(UserManagerTest, removeUserSuceedsWhenUserExists)
{
    //Create user
    userManager.defineUser(createUser());

    //Valid call
    ASSERT_NO_THROW(userManager.removeUser(userName));

    //Fail because the user was already removed
    ASSERT_ANY_THROW(userManager.removeUser(userName));
}

TEST_F(UserManagerTest, removeUserFailsWhenUserHasAlreadyBeenRemoved)
{
    //Create user
    userManager.defineUser(createUser());

    //Valid call
    ASSERT_NO_THROW(userManager.removeUser(userName));

    //Fail because the user was already removed
    ASSERT_ANY_THROW(userManager.removeUser(userName));
}


TEST_F(UserManagerTest, changeUserFailsWhenUserDoesNotExist)
{
    UserProto newUserProp;
    std::string newUserName = "newUser";
    std::string newPassword = "newPass";

    newUserProp.set_name(newUserName);
    newUserProp.set_password(newPassword);

    UserAdminRightsProto* adminRights = new UserAdminRightsProto();
    adminRights->set_access_control_rights(TestUtil::randomBool());
    adminRights->set_info_rights(TestUtil::randomBool());
    adminRights->set_server_admin_rights(TestUtil::randomBool());
    adminRights->set_system_config_rights(TestUtil::randomBool());

    UserDatabaseRightsProto* dbRights = new UserDatabaseRightsProto();
    dbRights->set_read(TestUtil::randomBool());
    dbRights->set_write(TestUtil::randomBool());

    newUserProp.set_allocated_admin_rights(adminRights);
    newUserProp.set_allocated_default_db_rights(dbRights);

    //Fail because there is no user
    ASSERT_ANY_THROW(userManager.changeUser(userName, newUserProp));
}

TEST_F(UserManagerTest, changeUserSucceeds)
{
    UserProto newUserProp;
    std::string newUserName = "newUser";
    std::string newPassword = "newPass";

    newUserProp.set_name(newUserName);
    newUserProp.set_password(newPassword);

    UserAdminRightsProto* adminRights = new UserAdminRightsProto();
    adminRights->set_access_control_rights(TestUtil::randomBool());
    adminRights->set_info_rights(TestUtil::randomBool());
    adminRights->set_server_admin_rights(TestUtil::randomBool());
    adminRights->set_system_config_rights(TestUtil::randomBool());

    UserDatabaseRightsProto* dbRights = new UserDatabaseRightsProto();
    dbRights->set_read(TestUtil::randomBool());
    dbRights->set_write(TestUtil::randomBool());

    newUserProp.set_allocated_admin_rights(adminRights);
    newUserProp.set_allocated_default_db_rights(dbRights);

    //Define the user
    ASSERT_NO_THROW(userManager.defineUser(createUser()));

    //Valid call
    ASSERT_NO_THROW(userManager.changeUser(userName, newUserProp));

    //Test that the change was valid.
    boost::shared_ptr<User> out_user;
    ASSERT_TRUE(userManager.tryGetUser(newUserName, out_user));
    ASSERT_EQ(newUserName, out_user->getName());
    ASSERT_EQ(newPassword, out_user->getPassword());

    ASSERT_EQ(adminRights->access_control_rights(), out_user->getAdminRights().hasAccessControlRights());
    ASSERT_EQ(adminRights->info_rights(), out_user->getAdminRights().hasInfoRights());
    ASSERT_EQ(adminRights->server_admin_rights(), out_user->getAdminRights().hasServerAdminRights());
    ASSERT_EQ(adminRights->system_config_rights(), out_user->getAdminRights().hasSystemConfigRights());

    ASSERT_EQ(dbRights->read(), out_user->getDefaultDbRights().hasReadAccess());
    ASSERT_EQ(dbRights->write(), out_user->getDefaultDbRights().hasWriteAccess());
}

TEST_F(UserManagerTest, tryGetUserFailsWhenUserDoesNotExist)
{
    boost::shared_ptr<User> out_user;

    ASSERT_FALSE(userManager.tryGetUser(userName, out_user));
}

TEST_F(UserManagerTest, tryGetUserSucceedsWhenUserExists)
{
    boost::shared_ptr<User> out_user;
    //Define the user
    ASSERT_NO_THROW(userManager.defineUser(createUser()));

    ASSERT_TRUE(userManager.tryGetUser(userName, out_user));

    ASSERT_EQ(userName, out_user->getName());
    ASSERT_EQ(password, out_user->getPassword());
}

TEST_F(UserManagerTest, serializeToProto)
{
    UserMgrProto proto;

    proto = userManager.serializeToProto();
    ASSERT_EQ(0, proto.users_size());

    ASSERT_NO_THROW(userManager.defineUser(createUser()));

    proto = userManager.serializeToProto();
    boost::shared_ptr<User> out_user;
    userManager.tryGetUser(userName, out_user);

    ASSERT_EQ(proto.users(0).DebugString(), User::serializeToProto(*out_user).DebugString());
}

}
}
