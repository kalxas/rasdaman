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

#include <limits.h>
#include <stdio.h>

#include <stdexcept>

#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>

#include <logging.hh>
#include "common/crypto/crypto.hh"
#include "common/uuid/uuid.hh"
#include "common/util/fileutils.hh"
#include "common/exceptions/rasexceptions.hh"
#include "include/globals.hh"

#include "rasmgr/src/messages/rasmgrmess.pb.h"
#include "exceptions/rasmgrexceptions.hh"

#include "user.hh"
#include "userauthconverter.hh"

#include "usermanager.hh"

namespace rasmgr
{
using std::list;
using std::mutex;
using std::runtime_error;
using std::shared_ptr;
using std::unique_lock;

using common::InvalidArgumentException;

using google::protobuf::io::CodedInputStream;
using google::protobuf::io::CodedOutputStream;
using google::protobuf::io::IstreamInputStream;
using google::protobuf::io::OstreamOutputStream;

UserManager::UserManager()
    : rasmgrAuthFilePath(std::string(CONFDIR) + RASMGR_AUTH_FILE)
{
}

UserManager::~UserManager()
{
}

void UserManager::defineUser(const UserProto &userInfo)
{
    bool duplicate = false;

    if (!userInfo.has_name() || userInfo.name().empty())
    {
        throw InvalidArgumentException("The user information must have a valid name value.");
    }

    unique_lock<mutex> lock(this->mut);
    for (auto it = this->userList.begin(); it != this->userList.end(); ++it)
    {
        if ((*it)->getName() == userInfo.name())
        {
            duplicate = true;
            break;
        }
    }

    if (duplicate)
    {
        throw UserAlreadyExistsException(userInfo.name());
    }

    std::shared_ptr<User> user(new User(userInfo.name(),
                                        userInfo.password(),
                                        UserDatabaseRights::parseFromProto(userInfo.default_db_rights()),
                                        UserAdminRights::parseFromProto(userInfo.admin_rights())));

    this->userList.push_back(user);
}

void UserManager::changeUser(const std::string &userName, const UserProto &newUserInfo)
{
    bool changed = false;

    unique_lock<mutex> lock(this->mut);
    for (auto it = this->userList.begin(); it != this->userList.end(); ++it)
    {
        if ((*it)->getName() == userName)
        {
            changed = true;

            if (newUserInfo.has_name() && !newUserInfo.name().empty())
            {
                (*it)->setName(newUserInfo.name());
            }

            if (newUserInfo.has_password())
            {
                (*it)->setPassword(newUserInfo.password());
            }

            if (newUserInfo.has_admin_rights())
            {
                (*it)->setAdminRights(UserAdminRights::parseFromProto(newUserInfo.admin_rights()));
            }

            if (newUserInfo.has_default_db_rights())
            {
                (*it)->setDefaultDbRights(UserDatabaseRights::parseFromProto(newUserInfo.default_db_rights()));
            }

            break;
        }
    }

    if (!changed)
    {
        throw InexistentUserException(userName);
    }
}

void UserManager::removeUser(const std::string &userName)
{
    bool removed = false;

    unique_lock<mutex> lock(this->mut);
    for (auto it = this->userList.begin(); it != this->userList.end(); ++it)
    {
        if ((*it)->getName() == userName)
        {
            this->userList.erase(it);
            removed = true;
            break;
        }
    }

    if (!removed)
    {
        throw InexistentUserException(userName);
    }
}

bool UserManager::tryGetUser(const std::string &userName, const std::string &passwordHash, std::shared_ptr<User> &out_user)
{
    unique_lock<mutex> lock(this->mut);
    for (auto it = this->userList.begin(); it != this->userList.end(); ++it)
    {
        if ((*it)->getName() == userName)
        {
            if ((*it)->getPassword() != passwordHash)
            {
                LERROR << "Invalid credentials for user " << userName;
                throw InvalidClientCredentialsException(userName);
            }
            out_user = (*it);
            return true;
        }
    }

    return false;
}

void UserManager::saveUserInformation(bool)
{
    UserMgrProto userData = this->serializeToProto();

    unique_lock<mutex> lock(this->mut);
    if (common::FileUtils::fileExists(rasmgrAuthFilePath))
    {
        std::string backupFile = rasmgrAuthFilePath + "." + common::UUID::generateUUID();

        common::FileUtils::copyFile(rasmgrAuthFilePath, backupFile);
    }

    std::string authFilePath = rasmgrAuthFilePath;

    //This checks if the path to the RASMGR_AUTH_FILE is longer than the maximum file path.
    if (authFilePath.length() >= PATH_MAX)
    {
        throw common::RuntimeException("Authentication file path longer than maximum allowed by OS:" + authFilePath);
    }

    LDEBUG << "Writing to authentication file:" << authFilePath;

    //Create the authentication file.
    std::ofstream ofs(authFilePath);

    if (!ofs)
    {
        throw common::RuntimeException("Could not open authentication file for writing. File path:" + authFilePath);
    }
    else
    {
        OstreamOutputStream outstream(&ofs);
        CodedOutputStream codedOutStream(&outstream);

        bool r = userData.SerializeToCodedStream(&codedOutStream);
        if (r == false)
        {
            LERROR << "Could not write to authentication file";
            throw common::RuntimeException("Could not write to authentication file. File path:" + authFilePath);
        }
    }

    ofs.close();
}

void UserManager::loadUserInformation()
{
    if (tryLoadUserAuthFromOldFile(rasmgrAuthFilePath))
    {
        LDEBUG << "Loaded user info from old style auth file.";
    }
    else if (tryLoadUserAuthFromFile(rasmgrAuthFilePath))
    {
        LDEBUG << "Loaded user info from new style auth file.";
    }
    else
    {
        this->loadDefaultUserAuth();
        LDEBUG << "Loaded default user auth.";
    }
}

UserMgrProto UserManager::serializeToProto()
{
    UserMgrProto result;

    unique_lock<mutex> lock(this->mut);

    for (auto it = this->userList.begin(); it != this->userList.end(); ++it)
    {
        result.add_users()->CopyFrom(User::serializeToProto(*(*it).get()));
    }

    return result;
}

void UserManager::setDatabaseManager(std::shared_ptr<DatabaseManager> dbManager)
{
    this->dbManager_ = dbManager;
}

bool UserManager::tryLoadUserAuthFromOldFile(const std::string &filePath)
{
    UserMgrProto oldUserData;
    if (UserAuthConverter::tryGetOldFormatAuthData(filePath, oldUserData))
    {
        for (int i = 0; i < oldUserData.users_size(); ++i)
        {
            this->defineUser(oldUserData.users(i));
        }

        LDEBUG << "Loaded old format auth data.";

        return true;
    }

    return false;
}

bool UserManager::tryLoadUserAuthFromFile(const std::string &filePath)
{
    bool success = false;

    if (filePath.length() >= PATH_MAX)
    {
        LERROR << "Authentication file path longer than maximum allowed by OS:" << filePath;
    }
    else
    {
        LDEBUG << "Opening authentication file:" << filePath;

        std::ifstream ifs(filePath);

        if (!ifs)
        {
            LWARNING << filePath << " was not found; using default credentials.";
        }
        else
        {
            IstreamInputStream inputStream(&ifs);
            CodedInputStream codedInStream(&inputStream);

            UserMgrProto list;
            if (!list.ParseFromCodedStream(&codedInStream))
            {
                //Parsing the file failed.
                LERROR << "Invalid authentication file";
            }
            else
            {
                for (int i = 0; i < list.users_size(); i++)
                {
                    this->defineUser(list.users(i));
                }

                success = true;
            }
        }

        ifs.close();
    }

    return success;
}

void UserManager::loadDefaultUserAuth()
{
    UserDatabaseRightsProto *fullDbRights = new UserDatabaseRightsProto();
    fullDbRights->set_read(true);
    fullDbRights->set_write(true);

    UserDatabaseRightsProto *guestDbRights = new UserDatabaseRightsProto();
    guestDbRights->set_read(true);
    guestDbRights->set_write(false);

    UserAdminRightsProto *adminRights = new UserAdminRightsProto();
    UserAdminRightsProto *guestAdminRights = new UserAdminRightsProto();

    adminRights->set_access_control_rights(true);
    adminRights->set_info_rights(true);
    adminRights->set_server_admin_rights(true);
    adminRights->set_system_config_rights(true);

    UserProto admin;
    admin.set_name(DEFAULT_ADMIN);
    admin.set_password(common::Crypto::messageDigest(DEFAULT_ADMIN_PASSWD, DEFAULT_DIGEST));
    admin.set_allocated_admin_rights(adminRights);
    admin.set_allocated_default_db_rights(fullDbRights);

    UserProto guest;
    guest.set_name(DEFAULT_USER);
    guest.set_password(common::Crypto::messageDigest(DEFAULT_USER, DEFAULT_DIGEST));
    guest.set_allocated_admin_rights(guestAdminRights);
    guest.set_allocated_default_db_rights(guestDbRights);

    this->defineUser(admin);
    this->defineUser(guest);
}

} /* namespace rasmgr */
