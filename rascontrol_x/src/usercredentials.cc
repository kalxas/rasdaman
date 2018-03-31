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

#include <unistd.h>

#include <cstring>
#include <cstdlib>

#include <iostream>
#include <stdexcept>

#include "common/src/crypto/crypto.hh"
#include <logging.hh>

#include "../include/globals.hh"

#include "usercredentials.hh"
#include "rascontrolconstants.hh"

namespace rascontrol
{

UserCredentials::UserCredentials(const std::string& userName, const std::string& userPassword):
    userName(userName)
{
    this->userPassword = common::Crypto::messageDigest(userPassword, DEFAULT_DIGEST);
}

void UserCredentials::interactiveLogin()
{
    std::cerr << "Login name: ";
    std::cin >> this->userName;

    //Remove any illegal characters
    char chars[] = " \t\r\n";
    std::size_t found = this->userName.find_first_of(chars);
    if (found != std::string::npos)
    {
        this->userName.erase(0, found);
    }

    //TODO:Ticket #997 remove getpass from the code
    //TODO:Ticket #998 make it possible to choose the encryption algorithm

    char* plainPass = getpass("  Password: ");
    std::string clearTextPass(plainPass);
    this->userPassword = common::Crypto::messageDigest(clearTextPass, DEFAULT_DIGEST);

    //Make sure we don't leave the password in the buffer
    for (size_t i = 0; i < strlen(plainPass); i++)
    {
        plainPass[i] = 0;
    }

    std::cerr << std::endl;
}

void UserCredentials::environmentLogin()
{
    char auxUserName[rascontrol::MAX_USERNAME_LENGTH];
    unsigned int i;

    char* s = getenv(RASLOGIN.c_str());

    if (s == NULL)
    {
        throw std::runtime_error("RASLOGIN environment variable is not set.");
    }

    for (i = 0; i < rascontrol::MAX_USERNAME_LENGTH - 1u && *s != ':' && *s ; i++, s++)
    {
        auxUserName[i] = *s;
    }

    auxUserName[i] = 0;

    this->userName = std::string(auxUserName);

    if (*s != ':')
    {
        throw std::runtime_error("Invalid environment variable:" + std::string(s));
    }
    s++;

    this->userPassword = std::string(s);
}

std::string UserCredentials::getUserName() const
{
    return this->userName;
}

std::string UserCredentials::getUserPassword() const
{
    return this->userPassword;
}
}
