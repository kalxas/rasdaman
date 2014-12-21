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
    this->loadAuthenticationFile();
}

UserManager::~UserManager()
{}

void UserManager::defineUser ( const std::string& userName, const std::string& password,
                               const UserDatabaseRights& defaultRights,
                               const UserAdminRights& adminRights )
{
    bool duplicate=false;
    list<boost::shared_ptr<User> >::iterator it;

    unique_lock<mutex> lock ( this->mut );
    for ( it = this->userList.begin(); it != this->userList.end(); ++it )
    {
        if ( ( *it )->getName() == userName )
        {
            duplicate= true;
            break;
        }
    }

    if ( duplicate )
    {
        throw runtime_error ( "ERROR:There already exists a user named:"+userName );
    }
    else
    {
        this->userList.push_back ( boost::shared_ptr<User> ( new User ( userName, password, defaultRights, adminRights ) ) );
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

void UserManager::changeUserName ( const std::string& oldUserName,const std::string& newUserName )
{
    list<boost::shared_ptr<User> >::iterator it;
    bool changed=false;

    unique_lock<mutex> lock ( this->mut );
    for ( it = this->userList.begin(); it != this->userList.end(); ++it )
    {
        if ( ( *it )->getName() == oldUserName )
        {
            ( *it )->setName ( newUserName );
            changed=true;
            break;
        }
    }

    if ( !changed )
    {
        throw runtime_error ( "There doesn't exist a user named:"+oldUserName );
    }
}

void UserManager::changeUserPassword ( const std::string& userName,const std::string& newPassword )
{
    list<boost::shared_ptr<User> >::iterator it;
    bool changed=false;

    unique_lock<mutex> lock ( this->mut );
    for ( it = this->userList.begin(); it != this->userList.end(); ++it )
    {
        if ( ( *it )->getName() == userName )
        {
            ( *it )->setPassword ( newPassword );
            changed=true;
            break;
        }
    }

    if ( !changed )
    {
        throw runtime_error ( "There doesn't exist a user named:"+userName );
    }
}

void UserManager::changeUserAdminRights ( const std::string& userName, const UserAdminRights& newRights )
{
    list<boost::shared_ptr<User> >::iterator it;
    bool changed=false;

    unique_lock<mutex> lock ( this->mut );
    for ( it = this->userList.begin(); it != this->userList.end(); ++it )
    {
        if ( ( *it )->getName() == userName )
        {
            ( *it )->setAdminRights ( newRights );
            changed=true;
        }
    }

    if ( !changed )
    {
        throw runtime_error ( "There doesn't exist a user named:"+userName );
    }
}

void UserManager::changeUserDatabaseRights ( const std::string& userName, const UserDatabaseRights& newRights )
{
    list<boost::shared_ptr<User> >::iterator it;
    bool changed=false;

    unique_lock<mutex> lock ( this->mut );
    for ( it = this->userList.begin(); it != this->userList.end(); ++it )
    {
        if ( ( *it )->getName() == userName )
        {
            ( *it )->setDefaultDbRights ( newRights );
            changed=true;
        }
    }

    if ( !changed )
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

list<boost::shared_ptr<User> > UserManager::getUserList() const
{
    return this->userList;
}

void UserManager::saveToAuthenticationFile()
{
    char authFileName[PATH_MAX];
    UserListProto list;

    unique_lock<mutex> lock ( this->mut );

    //This checks if the path to the RASMGR_AUTH_FILE is longer thant the maximum file path.
    int pathLen = snprintf ( authFileName, PATH_MAX, "%s/%s", getenv ( "HOME" ), RASMGR_AUTH_FILE );
    if ( pathLen >= PATH_MAX )
    {
        authFileName[PATH_MAX-1] = '\0';    // force-terminate string before printing
        LWARNING << "Warning: authentication file path longer than allowed by OS, file likely cannot be accessed: " << authFileName;
    }

    LDEBUG<<"Writing to authentication file:"<<authFileName;

    for ( std::list<shared_ptr<User> >::iterator it = this->userList.begin(); it!=this->userList.end(); ++it )
    {
        list.add_users()->CopyFrom ( User::serializeToProto ( * ( *it ).get() ) );
    }

    //Create the authentication file.
    std::ofstream ofs ( authFileName );

    if ( !ofs )
    {
        LERROR<<"Could not open authentication file for writing";
        throw runtime_error ( "Could not open authentication file for writing.File path:" + std::string ( authFileName ) );
    }
    else
    {
        LDEBUG<<"Created users"<<list.DebugString();

        OstreamOutputStream outstream ( &ofs );
        CodedOutputStream codedOutStream ( &outstream );

        bool r =list.SerializeToCodedStream ( &codedOutStream );
        if ( r==false )
        {
            LERROR<<"Could not write to authentication file";
            throw runtime_error ( "Could not write to authentication file.File path:" + std::string ( authFileName ) );
        }
    }

    ofs.close();
}

void UserManager::loadAuthenticationFile()
{
    //TODO-AT: Add a level of security here.
    char authFileName[PATH_MAX];
    //True if reading the elements from file was successful.
    bool success=true;

    //This checks if the path to the RASMGR_AUTH_FILE is longer thant the maximum file path.
    int pathLen = snprintf ( authFileName, PATH_MAX, "%s/%s", getenv ( "HOME" ), RASMGR_AUTH_FILE );
    if ( pathLen >= PATH_MAX )
    {
        authFileName[PATH_MAX-1] = '\0';    // force-terminate string before printing
        LWARNING << "Warning: authentication file path longer than allowed by OS, file likely cannot be accessed: " << authFileName;
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

        UserListProto list;
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
                User user =  User::parseFromProto ( list.users ( i ) );
                this->defineUser ( user.getName(), user.getPassword(), user.getDefaultDbRights(), user.getAdminRights() );
            }
        }
    }

    if ( !success )
    {
        LWARNING<<"Could not open authentication file. Creating default users.";
        UserDatabaseRights fullDbRights ( true,true );
        UserDatabaseRights guestDbRights ( true,false );

        UserAdminRights adminRights;
        UserAdminRights guestAdminRights;

        adminRights.setAccessControlRights ( true );
        adminRights.setInfoRights ( true );
        adminRights.setServerAdminRights ( true );
        adminRights.setSystemConfigRights ( true );

        this->defineUser ( DEFAULT_ADMIN,common::Crypto::messageDigest ( DEFAULT_ADMIN_PASSWD, DEFAULT_DIGEST ),fullDbRights, adminRights );
        this->defineUser ( DEFAULT_USER,common::Crypto::messageDigest ( DEFAULT_USER, DEFAULT_DIGEST ),guestDbRights, guestAdminRights );
    }

    ifs.close();
}


} /* namespace rasmgr */
