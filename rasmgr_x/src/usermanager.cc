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

#include "../../common/src/logging/easylogging++.hh"
#include "../../common/src/crypto/crypto.hh"
#include "globals.hh"

#include "usermanager.hh"

namespace rasmgr
{
using std::runtime_error;
using std::list;
using boost::shared_ptr;
using boost::mutex;
using boost::unique_lock;
using google::protobuf::io::CodedInputStream;
using google::protobuf::io::CodedOutputStream;
using google::protobuf::io::IstreamInputStream;
using google::protobuf::io::OstreamOutputStream;

UserManager::UserManager()
{
}

UserManager::~UserManager()
{}

void UserManager::defineUser (const UserProto &userInfo)
{
    bool duplicate=false;
    list<boost::shared_ptr<User> >::iterator it;

    if(!userInfo.has_name())
    {
        throw runtime_error("The user information must contain a name value.");
    }

    unique_lock<mutex> lock ( this->mut );
    for ( it = this->userList.begin(); it != this->userList.end(); ++it )
    {
        if ( ( *it )->getName() == userInfo.name())
        {
            duplicate= true;
            break;
        }
    }

    if ( duplicate )
    {
        throw runtime_error ( "There already exists a user named:\""+userInfo.name() +"\"");
    }
    else
    {
        std::string empty;
        boost::shared_ptr<User> user( new User ( userInfo.name(),
                                      userInfo.password(),
                                      UserDatabaseRights::parseFromProto(userInfo.default_db_rights()),
                                      UserAdminRights::parseFromProto(userInfo.admin_rights())));

        this->userList.push_back (user);
    }
}

void UserManager::changeUser(const std::string &userName, const UserProto &newUserInfo)
{
    list<boost::shared_ptr<User> >::iterator it;
    bool changed=false;

    unique_lock<mutex> lock ( this->mut );
    for ( it = this->userList.begin(); it != this->userList.end(); ++it )
    {
        if ( ( *it )->getName() == userName )
        {
            changed=true;

            if(newUserInfo.has_name())
            {
                ( *it )->setName(newUserInfo.name());
            }

            if(newUserInfo.has_password())
            {
                ( *it )->setPassword(newUserInfo.password());
            }

            if(newUserInfo.has_admin_rights())
            {
                ( *it )->setAdminRights(UserAdminRights::parseFromProto(newUserInfo.admin_rights()));
            }

            if(newUserInfo.has_default_db_rights())
            {
                ( *it )->setDefaultDbRights(UserDatabaseRights::parseFromProto(newUserInfo.default_db_rights()));
            }

            break;
        }
    }

    if ( !changed )
    {
        throw runtime_error ( "There doesn't exist a user named:\""+userName +"\"");
    }
}

void UserManager::removeUser ( const std::string& userName )
{
    list<boost::shared_ptr<User> >::iterator it;
    bool removed=false;

    unique_lock<mutex> lock ( this->mut );
    for ( it = this->userList.begin(); it != this->userList.end(); ++it )
    {
        if ( ( *it )->getName() == userName )
        {
            this->userList.erase ( it );
            removed=true;
            break;
        }
    }

    if ( !removed )
    {
        throw runtime_error ( "There doesn't exist a user named:"+userName );
    }
}

bool UserManager::tryGetUser ( const std::string& userName, boost::shared_ptr<User>& out_user )
{
    list<boost::shared_ptr<User> >::iterator it;

    unique_lock<mutex> lock ( this->mut );
    for ( it = this->userList.begin(); it != this->userList.end(); ++it )
    {
        if ( ( *it )->getName() == userName )
        {
            out_user = ( *it );
            return true;
        }
    }

    return false;
}


void UserManager::saveUserInformation()
{
    UserMgrProto userData = this->serializeToProto();

    unique_lock<mutex> lock ( this->mut );
    char authFileName[PATH_MAX];

    //This checks if the path to the RASMGR_AUTH_FILE is longer thant the maximum file path.
    int pathLen = snprintf ( authFileName, PATH_MAX, "%s/%s", getenv ( "HOME" ), RASMGR_AUTH_FILE );
    if ( pathLen >= PATH_MAX )
    {
        authFileName[PATH_MAX-1] = '\0';    // force-terminate string before printing
        throw runtime_error("Authentication file path longer than maximum allowed by OS:"+std::string(authFileName));
    }

    LDEBUG<<"Writing to authentication file:"<<authFileName;

    //Create the authentication file.
    std::ofstream ofs ( authFileName );

    if ( !ofs )
    {
        throw runtime_error ( "Could not open authentication file for writing. File path:" + std::string ( authFileName ) );
    }
    else
    {
        OstreamOutputStream outstream ( &ofs );
        CodedOutputStream codedOutStream ( &outstream );

        bool r =userData.SerializeToCodedStream ( &codedOutStream );
        if ( r==false )
        {
            LERROR<<"Could not write to authentication file";
            throw runtime_error ( "Could not write to authentication file.File path:" + std::string ( authFileName ) );
        }
    }

    ofs.close();
}

void UserManager::loadUserInformation()
{
    char authFileName[PATH_MAX];
    //True if reading the elements from file was successful.
    bool success=true;

    //This checks if the path to the RASMGR_AUTH_FILE is longer thant the maximum file path.
    int pathLen = snprintf ( authFileName, PATH_MAX, "%s/%s", getenv ( "HOME" ), RASMGR_AUTH_FILE );
    if ( pathLen >= PATH_MAX )
    {
        authFileName[PATH_MAX-1] = '\0';    // force-terminate string before printing
        throw runtime_error("Authentication file path longer than maximum allowed by OS:"+std::string(authFileName));
    }

    LDEBUG<<"Opening authentication file:"<<authFileName;

    std::ifstream ifs ( authFileName );

    if ( !ifs )
    {
        //Could not open the file
        success = false;
    }
    else
    {
        IstreamInputStream inputStream ( &ifs );
        CodedInputStream codedInStream ( &inputStream );

        UserMgrProto list;
        if ( !list.ParseFromCodedStream ( &codedInStream ) )
        {
            //Parsing the file failed.
            LERROR<<"Invalid authentication file";
            success =false;
        }
        else
        {
            for ( int i=0; i<list.users_size(); i++ )
            {
                this->defineUser(list.users(i));
            }
        }
    }

    if ( !success )
    {
        LWARNING<<"Could not open authentication file. Creating default users.";
        UserDatabaseRightsProto* fullDbRights = new UserDatabaseRightsProto();
        fullDbRights->set_read(true);
        fullDbRights->set_write(true);

        UserDatabaseRightsProto* guestDbRights  = new UserDatabaseRightsProto();
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
        admin.set_password(common::Crypto::messageDigest ( DEFAULT_ADMIN_PASSWD, DEFAULT_DIGEST ));
        admin.set_allocated_admin_rights(adminRights);
        admin.set_allocated_default_db_rights(fullDbRights);

        UserProto guest;
        guest.set_name(DEFAULT_USER);
        guest.set_password(common::Crypto::messageDigest ( DEFAULT_USER, DEFAULT_DIGEST ));
        guest.set_allocated_admin_rights(guestAdminRights);
        guest.set_allocated_default_db_rights(guestDbRights);

        this->defineUser(admin);
        this->defineUser(guest);
    }

    ifs.close();
}

UserMgrProto UserManager::serializeToProto()
{
    UserMgrProto result;

    unique_lock<mutex> lock ( this->mut );

    for ( std::list<shared_ptr<User> >::iterator it = this->userList.begin(); it!=this->userList.end(); ++it )
    {
        result.add_users()->CopyFrom ( User::serializeToProto ( * ( *it ).get() ) );
    }

    return result;
}

} /* namespace rasmgr */
