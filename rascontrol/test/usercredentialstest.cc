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

#include <logging.hh>
#include <gtest/gtest.h>
#include "common/crypto/crypto.hh"
#include "include/globals.hh"

#include "rascontrol/src/usercredentials.hh"
#include "rascontrol/src/rascontrolconstants.hh"

namespace rascontrol
{
namespace test
{
TEST(RasControlTest, UserCredentialsConstructor)
{
    std::string testPassword = "testPass";
    std::string testUser = "testUser";

    UserCredentials configuredUserCredentials(testUser, testPassword);
    UserCredentials defaultCredentials;

    ASSERT_EQ(DEFAULT_USER, defaultCredentials.getUserName());
    ASSERT_EQ(common::Crypto::messageDigest(DEFAULT_PASSWD, DEFAULT_DIGEST), defaultCredentials.getUserPassword());

    ASSERT_EQ(testUser, configuredUserCredentials.getUserName());
    ASSERT_EQ(common::Crypto::messageDigest(testPassword, DEFAULT_DIGEST), configuredUserCredentials.getUserPassword());

}

TEST(RasControlTest, UserCredentialsEnvironmentLogin)
{
    std::string testPassword = "testPass";
    std::string testUser = "testUser";

    char* envVar = new char[30];
    std::string envVarString = RASLOGIN + "=" + testUser + ":" + testPassword;
    strcpy(envVar, envVarString.c_str());
    UserCredentials credentials;

    unsetenv("RASLOGIN");
    ASSERT_ANY_THROW(credentials.environmentLogin());

    putenv(envVar);
    char* s = getenv("RASLOGIN");
    printf("%s\n", s);

    ASSERT_NO_THROW(credentials.environmentLogin());
    ASSERT_EQ(testUser, credentials.getUserName());
    ASSERT_EQ(testPassword, credentials.getUserPassword());

    delete[] envVar;
}

}
}
