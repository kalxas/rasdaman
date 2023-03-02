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

#ifndef RASCONTROL_X_SRC_USERCREDENTIALS_HH
#define RASCONTROL_X_SRC_USERCREDENTIALS_HH

#include <string>

#include "../include/globals.hh"

namespace rascontrol
{

/**
 * @brief The UserCredentials class is used for acquiring the user's credentials by interactive input
 * or from the environment variable.
 */
class UserCredentials
{
public:
    /**
    * @brief UserCredentials Initialize an instance of the UserCredentials class.
    * @param userName User name.
    * @param userPassword User password.
    */
    UserCredentials(const std::string &userName = DEFAULT_USER, const std::string &userPassword = DEFAULT_PASSWD);

    /**
     * @brief interactiveLogin Require the user to enter his credentials.
     */
    void interactiveLogin();

    /**
     * @brief environmentLogin Acquire the credentials from the environment variable RASLOGIN.
     */
    void environmentLogin();

    std::string getUserName() const;
    std::string getUserPassword() const;

private:
    std::string userName;
    std::string userPassword;
};
}  // namespace rascontrol

#endif  // USERCREDENTIALS_HH
