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

#include "../../common/src/unittest/gtest.h"
#include "../../common/src/mock/gmock.h"
#include "../../common/src/logging/easylogging++.hh"

#include "../../rasmgr_x/src/user.hh"
#include "../../rasmgr_x/src/useradminrights.hh"
#include "../../rasmgr_x/src/userdatabaserights.hh"
#include "../../rasmgr_x/src/usermanager.hh"

using rasmgr::User;

class UserManagerTest:public ::testing::Test
{
protected:
    UserManagerTest():userName("userName"),password("password"),dbRights(false,false)
    {}

    std::string userName;
    std::string password;
    rasmgr::UserDatabaseRights dbRights;
    rasmgr::UserAdminRights adminRights;
    rasmgr::UserManager userManager;
};


TEST_F(UserManagerTest, defineUser)
{
    ASSERT_NO_THROW(userManager.defineUser(userName, password, dbRights,adminRights));

    //Fail due to duplication
    ASSERT_ANY_THROW(userManager.defineUser(userName, password, dbRights,adminRights));
}

TEST_F(UserManagerTest, removeUser)
{
	//Fail because the user does not exist
	ASSERT_ANY_THROW(userManager.removeUser(userName));

    ASSERT_NO_THROW(userManager.defineUser(userName, password, dbRights,adminRights));

    //Valid call
    ASSERT_NO_THROW(userManager.removeUser(userName));

    //Fail because the user was already removed
    ASSERT_ANY_THROW(userManager.removeUser(userName));
}

TEST_F(UserManagerTest, changeUserName)
{
	std::string newValue = "newValue";
	boost::shared_ptr<User> out_user;
	//Fail because the user does not exist
	ASSERT_ANY_THROW(userManager.changeUserName(userName, newValue));

    ASSERT_NO_THROW(userManager.defineUser(userName, password, dbRights,adminRights));

    //Valid call
    ASSERT_NO_THROW(userManager.changeUserName(userName, newValue));

    ASSERT_FALSE(userManager.tryGetUser(userName, out_user));
    ASSERT_TRUE(userManager.tryGetUser(newValue, out_user));
}

TEST_F(UserManagerTest, changeUserPassword)
{
	std::string newValue = "newValue";
	boost::shared_ptr<User> out_user;
	//Fail because the user does not exist
	ASSERT_ANY_THROW(userManager.changeUserPassword(userName, newValue));

    ASSERT_NO_THROW(userManager.defineUser(userName, password, dbRights,adminRights));

    //Valid call
    ASSERT_NO_THROW(userManager.changeUserPassword(userName, newValue));


    ASSERT_TRUE(userManager.tryGetUser(userName, out_user));
    ASSERT_EQ(newValue, out_user->getPassword());
}

TEST_F(UserManagerTest, changeUserAdminRights)
{
	std::string newValue = "newValue";
	boost::shared_ptr<User> out_user;

	rasmgr::UserAdminRights newAdminRights;
	newAdminRights.setAccessControlRights(true);
	newAdminRights.setInfoRights(true);
	newAdminRights.setServerAdminRights(true);
	newAdminRights.setSystemConfigRights(true);

	//Fail because the user does not exist
	ASSERT_ANY_THROW(userManager.changeUserAdminRights(userName, newAdminRights));

    ASSERT_NO_THROW(userManager.defineUser(userName, password, dbRights,adminRights));

    //Check old user rights
    ASSERT_TRUE(userManager.tryGetUser(userName, out_user));
    ASSERT_FALSE(out_user->getAdminRights().hasAccessControlRights());
    ASSERT_FALSE(out_user->getAdminRights().hasInfoRights());
    ASSERT_FALSE(out_user->getAdminRights().hasServerAdminRights());
    ASSERT_FALSE(out_user->getAdminRights().hasSystemConfigRights());

    //Valid call
    ASSERT_NO_THROW(userManager.changeUserAdminRights(userName, newAdminRights));

    ASSERT_TRUE(userManager.tryGetUser(userName, out_user));
    ASSERT_TRUE(out_user->getAdminRights().hasAccessControlRights());
    ASSERT_TRUE(out_user->getAdminRights().hasInfoRights());
    ASSERT_TRUE(out_user->getAdminRights().hasServerAdminRights());
    ASSERT_TRUE(out_user->getAdminRights().hasSystemConfigRights());
}

TEST_F(UserManagerTest, changeUserDatabaseRights)
{
	rasmgr::UserDatabaseRights newRights(true, true);
	boost::shared_ptr<User> out_user;
	//Fail because the user does not exist
	ASSERT_ANY_THROW(userManager.changeUserDatabaseRights(userName, newRights));

    ASSERT_NO_THROW(userManager.defineUser(userName, password, dbRights,adminRights));

    ASSERT_TRUE(userManager.tryGetUser(userName, out_user));
    ASSERT_FALSE(out_user->getDefaultDbRights().hasReadAccess());
    ASSERT_FALSE(out_user->getDefaultDbRights().hasWriteAccess());

    //Valid call
    ASSERT_NO_THROW(userManager.changeUserDatabaseRights(userName, newRights));

    ASSERT_TRUE(userManager.tryGetUser(userName, out_user));
    ASSERT_TRUE(out_user->getDefaultDbRights().hasReadAccess());
    ASSERT_TRUE(out_user->getDefaultDbRights().hasWriteAccess());
}

TEST_F(UserManagerTest, tryGetUser){
	boost::shared_ptr<User> out_user;

	ASSERT_FALSE(userManager.tryGetUser(userName, out_user));

	ASSERT_NO_THROW(userManager.defineUser(userName, password, dbRights,adminRights));

	ASSERT_TRUE(userManager.tryGetUser(userName, out_user));

	ASSERT_EQ(userName, out_user->getName());
	ASSERT_EQ(password, out_user->getPassword());
}

TEST_F(UserManagerTest, getUserList){

	ASSERT_EQ(0, userManager.getUserList().size());

	ASSERT_NO_THROW(userManager.defineUser(userName, password, dbRights,adminRights));

	ASSERT_EQ(1, userManager.getUserList().size());

	boost::shared_ptr<User> user = userManager.getUserList().front();

	ASSERT_EQ(userName, user->getName());
	ASSERT_EQ(password, user->getPassword());
}
