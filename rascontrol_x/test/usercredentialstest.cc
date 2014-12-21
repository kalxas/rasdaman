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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009,2010,2011,2012,2013,2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#include <cstdlib>

#include "../../common/src/logging/easylogging++.hh"
#include "../../common/src/unittest/gtest.h"
#include "../../common/src/crypto/crypto.hh"
#include "../../include/globals.hh"

#include "../../rascontrol_x/src/usercredentials.hh"

using rascontrol::UserCredentials;

TEST(RasControlTest, UserCredentialsConstructor)
{
    UserCredentials defaultCredentials;
    UserCredentials configuredUserCredentials("test","testpass");

    ASSERT_EQ(DEFAULT_USER, defaultCredentials.getUserName());
    ASSERT_EQ(DEFAULT_PASSWD, defaultCredentials.getUserPassword());

    ASSERT_EQ("test", configuredUserCredentials.getUserName());
    ASSERT_EQ(common::Crypto::messageDigest("testpass","MD5"), configuredUserCredentials.getUserPassword());

}

TEST(RasControlTest, UserCredentialsEnvironmentLogin)
{
    char* envVar = new char[25];
    strcpy(envVar,"RASLOGIN=user:testpass");
    UserCredentials credentials;

    ASSERT_ANY_THROW(credentials.environmentLogin());

    putenv(envVar);

    ASSERT_NO_THROW(credentials.environmentLogin());
    ASSERT_EQ("user",credentials.getUserName());
    ASSERT_EQ(common::Crypto::messageDigest("testpass","MD5"),credentials.getUserPassword());

    delete []envVar;

}
