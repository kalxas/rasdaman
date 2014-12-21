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

#include <set>
#include <sstream>

#include <boost/cstdint.hpp>
#include <boost/format.hpp>

#include "../../common/src/crypto/crypto.hh"
#include "../../common/src/logging/easylogging++.hh"
#include "../../include/globals.hh"

#include "rascontrol.hh"
#include "userdatabaserights.hh"
#include "useradminrights.hh"
#include "rasmanager.hh"
#include "version.h"

namespace rasmgr
{
using common::Crypto;
using std::list;
using boost::shared_ptr;
using boost::format;

RasControl::RasControl ( boost::shared_ptr<UserManager> userManager, boost::shared_ptr<DatabaseHostManager> dbHostManager, boost::shared_ptr<DatabaseManager> dbManager, boost::shared_ptr<ServerManager> serverManager, RasManager* rasmanager )
{
    this->userManager=userManager;
    this->dbHostManager = dbHostManager;
    this->dbManager = dbManager;
    this->serverManager = serverManager;
    this->rasmanager = rasmanager;
}

std::string RasControl::deprecatedCommand()
{
    return "DEPRECATED";
}

std::string RasControl::defineDbHost ( const DefineDbHost& dbHostData )
{
    std::string message="";

    LDEBUG<<"Definining database host:"<<dbHostData.DebugString();

    try
    {
        std::string connect="";
        std::string user="";
        std::string passwd="";

        if ( dbHostData.has_connect() )
        {
            connect=dbHostData.connect();
        }

        if ( dbHostData.has_user() )
        {
            user=dbHostData.user();
        }

        if ( dbHostData.has_passwd() )
        {
            passwd=dbHostData.passwd();
        }

        this->dbHostManager->addNewDatabaseHost ( dbHostData.host_name(), connect, user, passwd );
    }
    catch ( std::exception& ex )
    {
        message = "ERROR"+std::string ( ex.what() );
    }
    catch ( ... )
    {
        message = "ERROR:Operation define db could not be completed.";
    }

    return message;

}

std::string RasControl::changeDbHost ( const ChangeDbHost& dbHostData )
{
    std::string message="";

    LDEBUG<<"Changing database host:"<<dbHostData.DebugString();

    try
    {
        boost::shared_ptr<DatabaseHost> dbHost=this->dbHostManager->getDatabaseHost ( dbHostData.host_name() );

        std::string connect = dbHost->getConnectString();
        std::string user = dbHost->getUserName();
        std::string passwd = dbHost->getPasswdString();
        std::string newName = dbHost->getHostName();

        if ( dbHostData.has_n_connect() )
        {
            connect = dbHostData.n_connect();
        }

        if ( dbHostData.has_n_user() )
        {
            user = dbHostData.n_user();
        }

        if ( dbHostData.has_n_passwd() )
        {
            passwd = dbHostData.n_passwd();
        }

        if ( dbHostData.has_n_name() )
        {
            newName = dbHostData.n_name();
        }

        this->dbHostManager->changeDatabaseHost ( dbHostData.host_name(),newName, connect,user, passwd );
    }
    catch ( std::exception& ex )
    {
        message = "ERROR"+std::string ( ex.what() );
    }
    catch ( ... )
    {
        message = "ERROR:Operation define db could not be completed.";
    }

    return message;

}

std::string RasControl::removeDbHost ( const RemoveDbHost& dbHostData )
{
    std::string message="";

    LDEBUG<<"Removing database host:"<<dbHostData.DebugString();

    try
    {
        this->dbHostManager->removeDatabaseHost ( dbHostData.host_name() );
    }
    catch ( std::exception& ex )
    {
        message = "ERROR"+std::string ( ex.what() );
    }
    catch ( ... )
    {
        message = "ERROR:Operation define db could not be completed.";
    }

    return message;
}

std::string RasControl::listDbHost()
{
    std::stringstream ss;
    int counter = 1;
    list<shared_ptr<DatabaseHost> > dbhList = this->dbHostManager->getDatabaseHostList();

    ss<<"List of database hosts:\r\n";
    ss<<"    Database Host     Connection String            Databases";
    for ( list<shared_ptr<DatabaseHost> > ::iterator it = dbhList.begin(); it!=dbhList.end(); ++it )
    {
        ss<< ( format ( "\r\n%2d. %-15s %-30s" ) %counter% ( *it )->getHostName() % ( *it )->getConnectString() );
        list<Database> dbList = ( *it )->getDatabaseList();

        for ( list<Database>::iterator db = dbList.begin(); db!=dbList.end(); ++db )
        {
            ss<< ( format ( " %s" ) % db->getDbName() );
        }

        counter++;
    }

    return ss.str();
}

std::string RasControl::helpDbHost()
{
    return "TEST";
}

std::string RasControl::defineDb ( const DefineDb& dbData )
{
    std::string message="";

    LDEBUG<<"Definining database:"<<dbData.DebugString();

    try
    {
        this->dbManager->defineDatabase ( dbData.db_name(), dbData.dbhost_name() );
    }
    catch ( std::exception& ex )
    {
        message = "ERROR"+std::string ( ex.what() );
    }
    catch ( ... )
    {
        message = "ERROR:Operation remove user could not be completed.";
    }

    return message;
}

std::string RasControl::changeDb ( const ChangeDb& dbData )
{
    std::string message="";

    LDEBUG<<"Changing database:"<<dbData.DebugString();

    try
    {
        this->dbManager->changeDatabaseName ( dbData.db_name(), dbData.n_db_name() );
    }
    catch ( std::exception& ex )
    {
        message = "ERROR"+std::string ( ex.what() );
    }
    catch ( ... )
    {
        message = "ERROR:Operation change db could not be completed.";
    }

    return message;
}

std::string RasControl::removeDb ( const RemoveDb& dbData )
{
    std::string message="";

    LDEBUG<<"Changing database:"<<dbData.DebugString();

    try
    {
        this->dbManager->removeDatabase ( dbData.db_name() );
    }
    catch ( std::exception& ex )
    {
        message = "ERROR"+std::string ( ex.what() );
    }
    catch ( ... )
    {
        message = "ERROR:Operation change db could not be completed.";
    }

    return message;
}

std::string RasControl::listDb ( const ListDb& listDbData )
{
    std::stringstream ss;

    LDEBUG<<listDbData.DebugString();

    //If the Database name was given, list information about this database
    if ( listDbData.has_db_name() )
    {
        Database db=this->dbManager->getDatabase ( listDbData.db_name() );

        ss<<"List database: "<<listDbData.db_name() <<"\r\n";
        ss<<"    Database Name         Open Trans.";


        ss<< ( format ( "\r\n%2d. %-20s  %d" ) % 1 %  db.getDbName() % db.getSessionCount() );
    }
    //If the database host was given, give information about the database host
    else if ( listDbData.has_dbh_name() )
    {
        shared_ptr<DatabaseHost> dbh = this->dbHostManager->getDatabaseHost ( listDbData.dbh_name() );
        list<Database> dbList =  dbh->getDatabaseList();

        ss<< ( format ( "List of databases on host: %s\r\n" ) % dbh->getHostName() );
        ss<<"    Database Name         Open Trans.";
        int counter = 1;
        for ( list<Database>::iterator it = dbList.begin(); it!=dbList.end(); ++it )
        {
            ss<< ( format ( "\r\n%2d. %-20s  %d" ) % counter % it->getDbName() % it->getSessionCount() );
            counter++;
        }
    }//otherwise assume -all was passed in
    else
    {
        list<Database> dbList = this->dbManager->getDatabaseList();

        ss<< ( format ( "List of databases:\r\n" ) );
        ss<<"    Database Name         Open Trans.";

        int counter = 1;
        for ( list<Database>::iterator it = dbList.begin(); it!=dbList.end(); ++it )
        {
            ss<< ( format ( "\r\n%2d. %-20s  %d" ) % counter % it->getDbName() % it->getSessionCount() );
            counter++;
        }
    }

    return ss.str();
}

std::string RasControl::helpDb()
{
    return "TEST";
}

std::string RasControl::defineUser ( const DefineUser& userData )
{
    std::string passwd;
    std::string result;
    UserAdminRights adminRights;
    UserDatabaseRights dbRights ( false,false );

    std::string userName = userData.user_name();

    if ( userData.has_passwd() )
    {
        passwd = userData.passwd();
    }
    else
    {
        if ( Crypto::isMessageDigestAvailable ( DEFAULT_DIGEST ) )
        {
            passwd = Crypto::messageDigest ( userData.user_name(),DEFAULT_DIGEST );
        }
        else
        {
            //TODO-AT: Add debug statements. Shoudl we store the user name unencrypted
            passwd = userData.user_name();
        }
    }

    if ( userData.has_access_rights() )
    {
        adminRights.setAccessControlRights ( userData.access_rights() );
    }
    if ( userData.has_config_rights() )
    {
        adminRights.setSystemConfigRights ( userData.config_rights() );
    }

    if ( userData.has_info_rights() )
    {
        adminRights.setInfoRights ( userData.info_rights() );
    }

    if ( userData.has_server_admin_rights() )
    {
        adminRights.setServerAdminRights ( userData.server_admin_rights() );
    }

    if ( userData.has_dbread_rights() )
    {
        dbRights.setReadAccess ( userData.dbread_rights() );
    }

    if ( userData.has_dbwrite_rights() )
    {
        dbRights.setWriteAccess ( userData.dbwrite_rights() );
    }

    try
    {
        this->userManager->defineUser ( userName,passwd, dbRights, adminRights );
    }
    catch ( std::exception& ex )
    {
        result=ex.what();
    }
    catch ( ... )
    {
        result = "ERROR:Operation add user could not be completed.";
    }

    return result;
}

std::string RasControl::removeUser ( const RemoveUser& userData )
{
    std::string message="";

    try
    {
        this->userManager->removeUser ( userData.user_name() );
    }
    catch ( std::exception& ex )
    {
        message = "ERROR"+std::string ( ex.what() );
    }
    catch ( ... )
    {
        message = "ERROR:Operation remove user could not be completed.";
    }

    return message;
}

std::string RasControl::changeUser ( const ChangeUser& userData )
{
    boost::shared_ptr<User> user;
    std::string message = "";

    try
    {
        if ( this->userManager->tryGetUser ( userData.user_name(),user ) )
        {
            if ( userData.has_n_passwd() )
            {
                this->userManager->changeUserPassword ( userData.user_name(),userData.n_passwd() );
            }

            UserDatabaseRights dbRights = user->getDefaultDbRights();

            if ( userData.has_n_dbread_rights() )
            {
                dbRights.setReadAccess ( userData.n_dbread_rights() );
            }

            if ( userData.has_n_dbwrite_rights() )
            {
                dbRights.setWriteAccess ( userData.n_dbwrite_rights() );
            }

            this->userManager->changeUserDatabaseRights ( userData.user_name(),dbRights );

            UserAdminRights adminRights = user->getAdminRights();

            if ( userData.has_n_config_rights() )
            {
                adminRights.setSystemConfigRights ( userData.n_config_rights() );
            }

            if ( userData.has_n_access_rights() )
            {
                adminRights.setAccessControlRights ( userData.n_access_rights() );
            }

            if ( userData.has_n_info_rights() )
            {
                adminRights.setInfoRights ( userData.n_info_rights() );
            }

            if ( userData.has_n_server_admin_rights() )
            {
                adminRights.setServerAdminRights ( userData.n_server_admin_rights() );
            }

            this->userManager->changeUserAdminRights ( userData.user_name(),adminRights );

            if ( userData.has_n_name() )
            {
                this->userManager->changeUserName ( userData.user_name(), userData.n_name() );
            }
        }
        else
        {
            message = "There is no user named: \""+userData.user_name() +"\" on this server";
        }
    }
    catch ( std::exception& ex )
    {
        message = "ERROR"+std::string ( ex.what() );
    }
    catch ( ... )
    {
        message = "ERROR:Operation change user could not be completed.";
    }

    return message;
}

std::string RasControl::listUser ( const ListUser& userData )
{
    std::stringstream ss;
    int counter=1;
    bool displayRights = userData.has_diplay_rights();
    list<shared_ptr<User> > userList =this->userManager->getUserList();

    ss<<"List of defined users:"<<std::endl;
    for ( list<shared_ptr<User> >::iterator it = userList.begin(); it!=userList.end(); ++it )
    {

        ss<<"\r\n"<<std::setw ( 2 ) <<counter<<". "<< ( *it )->getName();
        if ( displayRights )
        {
            ss<<" ["<<this->convertAdminRights ( ( *it )->getAdminRights() ) <<"]";
            ss<<" -["<<this->convertDbRights ( ( *it )->getDefaultDbRights() ) <<"]";
        }

        counter++;
    }

    return ss.str();
}

std::string RasControl::helpUser()
{
    return "TEST";
}

std::string RasControl::defineInpeer ( std::string hostName )
{
    return hostName;
}

std::string RasControl::removeInpeer ( std::string hostName )
{
    return hostName;
}

std::string RasControl::listInpeer()
{
    return "LIST";
}

std::string RasControl::helpInpeer()
{
    return "HELP INPEER";
}

std::string RasControl::defineOutpeer ( const DefineOutpeer& outpeerData )
{
    LDEBUG<<outpeerData.host_name();
    LDEBUG<<outpeerData.port();
    return "outpeerData.host_name()";
}

std::string RasControl::removeOutpeer ( std::string hostName )
{
    return hostName;
}

std::string RasControl::listOutpeer()
{
    return "LIST";
}

std::string RasControl::helpOutpeer()
{
    return "HELP";
}

std::string RasControl::defineServerGroup ( const DefineServerGroup &groupData )
{
    std::string message;
    LDEBUG<<"Defining server group with the following configuration:"<<groupData.DebugString();
    try
    {
        boost::uint32_t groupSize = 1;
        if ( groupData.has_group_size() )
        {
            groupSize = groupData.group_size();
        }
        std::set<boost::int32_t> ports;

        for ( int i=0; i<groupData.ports_size(); i++ )
        {
            ports.insert ( groupData.ports ( i ) );
        }

        ServerGroupConfig config ( groupData.group_name(),groupSize,groupData.host(),ports,groupData.db_host() );

        if ( groupData.has_autorestart() )
        {
            config.setAutorestart ( groupData.autorestart() );
        }

        if ( groupData.has_countdown() )
        {
            config.setCountdown ( groupData.countdown() );
        }

        if ( groupData.has_max_idle_servers() )
        {
            config.setMaxIdleServersNo ( groupData.max_idle_servers() );
        }

        if ( groupData.has_min_alive_servers() )
        {
            config.setMinAliveServers ( groupData.min_alive_servers() );
        }

        if ( groupData.has_min_available_servers() )
        {
            config.setMinAvailableServers ( groupData.min_available_servers() );
        }

        if ( groupData.has_options() )
        {
            config.setServerOptions ( groupData.options() );
        }

        this->serverManager->defineServerGroup ( config );
    }
    catch ( std::exception& ex )
    {
        message = "ERROR"+std::string ( ex.what() );
    }
    catch ( ... )
    {
        message = "ERROR:";
    }

    return message;
}

std::string RasControl::changeServerGroup ( const ChangeServerGroup &groupData )
{
    std::string message;
    LDEBUG<<"Changing server group with the following configuration:"<<groupData.DebugString();

    try
    {
        ServerGroupConfig oldConfig = this->serverManager->getServerGroupConfig ( groupData.group_name() );

        if ( groupData.has_n_group_name() )
        {
            oldConfig.setGroupName ( groupData.n_group_name() );
        }

        if ( groupData.has_n_host() )
        {
            oldConfig.setHost ( groupData.n_host() );
        }

        if ( groupData.n_ports_size() >0 )
        {
            std::set<boost::int32_t> ports;

            for ( int i=0; i<groupData.n_ports_size(); i++ )
            {
                ports.insert ( groupData.n_ports ( i ) );
            }

            oldConfig.setPorts ( ports );
        }

        if ( groupData.has_n_db_host() )
        {
            oldConfig.setDbHost ( groupData.n_db_host() );
        }

        if ( groupData.has_n_group_size() )
        {
            oldConfig.setGroupSize ( groupData.n_group_size() );
        }

        if ( groupData.has_n_min_alive_servers() )
        {
            oldConfig.setMinAliveServers ( groupData.n_min_alive_servers() );
        }

        if ( groupData.has_n_min_available_servers() )
        {
            oldConfig.setMinAvailableServers ( groupData.n_min_available_servers() );
        }

        if ( groupData.has_n_max_idle_servers() )
        {
            oldConfig.setMaxIdleServersNo ( groupData.n_max_idle_servers() );
        }

        if ( groupData.has_n_autorestart() )
        {
            oldConfig.setAutorestart ( groupData.n_autorestart() );
        }

        if ( groupData.has_n_countdown() )
        {
            oldConfig.setCountdown ( groupData.n_countdown() );
        }

        if ( groupData.has_n_options() )
        {
            oldConfig.setServerOptions ( groupData.n_options() );
        }

        this->serverManager->changeServerGroup ( groupData.group_name(), oldConfig );
    }
    catch ( std::exception& ex )
    {
        message = "ERROR"+std::string ( ex.what() );
    }
    catch ( ... )
    {
        message = "ERROR:Operation change server group could not be completed.";
    }

    return message;
}

std::string RasControl::removeServerGroup ( std::string groupName )
{
    std::string message="";

    LDEBUG<<"Removing server group:"<<groupName;

    try
    {
        this->serverManager->removeServerGroup ( groupName );
    }
    catch ( std::exception& ex )
    {
        message = "ERROR"+std::string ( ex.what() );
    }
    catch ( ... )
    {
        message = "ERROR:Operation remove server group could not be completed.";
    }

    return message;
}

std::string RasControl::listServerGroup ( const ListServerGroup &listData )
{
    std::string result="ERROR interpreting command.";

    //  bool extraInfo = listData.has_extra_info();

    //List information about a particular group
    if ( listData.has_group_name() )
    {
        result = this->serverManager->getServerGroupInfo ( listData.group_name() );
    }
    else if ( listData.has_host() ) //list information about servers running on a host
    {
        result = this->serverManager->getAllServerGroupsInfo ( false, listData.host() );
    }
    else   //list information about all the servers
    {
        result = this->serverManager->getAllServerGroupsInfo();
    }

    return result;
}

std::string RasControl::helpServerGroup()
{
    return "HELP SERVER GROUP";
}

std::string RasControl::startServerGroup ( const StartServerGroup &upSrv )
{
    std::string message ="";

    LDEBUG<<"Starting server:"<<upSrv.DebugString();

    try
    {
        this->serverManager->startServerGroup ( upSrv );
    }
    catch ( std::exception& ex )
    {
        message = ex.what();
    }
    catch ( ... )
    {
        message = "Starting servers failed for unknown reason.";
    }

    return message;
}

std::string RasControl::stopServerGroup ( const StopServerGroup &downSrv )
{
    std::string message ="";

    LDEBUG<<"Stopping server:"<<downSrv.DebugString();

    try
    {
        this->serverManager->stopServerGroup ( downSrv );
    }
    catch ( std::exception& ex )
    {
        message = ex.what();
    }
    catch ( ... )
    {
        message = "Stoping servers failed for unknown reason.";
    }

    return message;
}

std::string RasControl::stopRasMgr()
{
    std::string message ="";

    LDEBUG<<"Stopping rasmgr.";

    try
    {
        if ( this->serverManager->hasRunningServerGroup() )
        {
            message="This rasmgr instance has running server groups. Down all the server groups before stoping rasmgr.";
        }
        else
        {
            this->rasmanager->stop();
            message = "rasmanager is shutting down. Good Bye!";
        }
    }
    catch ( std::exception& ex )
    {
        message = ex.what();
    }
    catch ( ... )
    {
        message = "Stoping servers failed for unknown reason.";
    }

    return message;
}

std::string RasControl::listVersion()
{
    return RMANVERSION;
}

string RasControl::login()
{
    return "Welcome!";
}

std::string RasControl::save()
{
    std::string message ="";

    try
    {
        this->userManager->saveToAuthenticationFile();
    }
    catch ( std::exception& ex )
    {
        message = ex.what();
    }
    catch ( ... )
    {
        message = "Saving rasmgr data failed for an unknown reason.";
    }
}

std::string RasControl::exit()
{}

std::string RasControl::convertAdminRights ( const UserAdminRights& adminRights )
{
    std::stringstream ss;
    if ( adminRights.hasAccessControlRights() )
    {
        ss<<"A";
    }
    else
    {
        ss<<".";
    }

    if ( adminRights.hasInfoRights() )
    {
        ss<<'I';
    }
    else
    {
        ss<<'.';
    }

    if ( adminRights.hasServerAdminRights() )
    {
        ss<<'S';
    }
    else
    {
        ss<<'.';
    }

    if ( adminRights.hasSystemConfigRights() )
    {
        ss<<'C';
    }
    else
    {
        ss<<'.';
    }

    return ss.str();

}
std::string RasControl::convertDbRights ( const UserDatabaseRights& dbRights )
{
    std::stringstream ss;

    if ( dbRights.hasReadAccess() )
    {
        ss<<'R';
    }
    else
    {
        ss<<'.';
    }

    if ( dbRights.hasWriteAccess() )
    {
        ss<<'W';
    }
    else
    {
        ss<<'.';
    }

    return ss.str();
}

bool RasControl::hasInfoRights ( std::string userName, std::string password )
{
    boost::shared_ptr<User> user;

    if ( this->userManager->tryGetUser ( userName, user ) && user->getPassword() == password )
    {
        return user->getAdminRights().hasInfoRights();
    }
    else
    {
        return false;
    }
}

bool RasControl::hasConfigRights ( std::string userName,std::string password )
{
    boost::shared_ptr<User> user;

    if ( this->userManager->tryGetUser ( userName, user ) && user->getPassword() == password )
    {
        return user->getAdminRights().hasSystemConfigRights();
    }
    else
    {
        return false;
    }
}

bool RasControl::hasUserAdminRights ( std::string userName,std::string password )
{
    boost::shared_ptr<User> user;

    if ( this->userManager->tryGetUser ( userName, user ) && user->getPassword() == password )
    {
        return user->getAdminRights().hasAccessControlRights();
    }
    else
    {
        return false;
    }
}

bool RasControl::hasServerAdminRights ( std::string userName,std::string password )
{
    boost::shared_ptr<User> user;

    if ( this->userManager->tryGetUser ( userName, user ) && user->getPassword() == password )
    {
        return user->getAdminRights().hasServerAdminRights();
    }
    else
    {
        return false;
    }
}
bool RasControl::isValidUser ( string userName, string password )
{
    boost::shared_ptr<User> user;

    return this->userManager->tryGetUser ( userName, user ) && user->getPassword() == password ;
}


}
