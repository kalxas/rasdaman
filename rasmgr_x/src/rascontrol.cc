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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#include <set>
#include <sstream>

#include <boost/cstdint.hpp>
#include <boost/format.hpp>

#include "../../common/src/crypto/crypto.hh"
#include <easylogging++.h>
#include "../../include/globals.hh"

#include "databasehostmanager.hh"
#include "databasemanager.hh"
#include "peermanager.hh"
#include "rasmanager.hh"
#include "servermanager.hh"
#include "usermanager.hh"
#include "user.hh"

#include "rascontrol.hh"
#include "version.h"


namespace rasmgr
{
using common::Crypto;

using boost::shared_ptr;
using boost::format;

using std::list;

RasControl::RasControl ( boost::shared_ptr<UserManager> userManager,
                         boost::shared_ptr<DatabaseHostManager> dbHostManager,
                         boost::shared_ptr<DatabaseManager> dbManager,
                         boost::shared_ptr<ServerManager> serverManager,
                         boost::shared_ptr<PeerManager> peerManager,
                         RasManager* rasmanager )
{
    this->userManager = userManager;
    this->dbHostManager = dbHostManager;
    this->dbManager = dbManager;
    this->serverManager = serverManager;
    this->peerManager = peerManager;
    this->rasmanager = rasmanager;
}

std::string RasControl::deprecatedCommand()
{
    std::string message = "This command is deprecated and will be removed in the future.";

    return message;
}

std::string RasControl::defineDbHost ( const DefineDbHost& dbHostData )
{
    std::string message="";

    LDEBUG<<"Definining database host:"<<dbHostData.DebugString();

    try
    {
        DatabaseHostPropertiesProto dbhProp;
        dbhProp.set_host_name(dbHostData.host_name());

        if ( dbHostData.has_connect() )
        {
            dbhProp.set_connect_string(dbHostData.connect());
        }

        if ( dbHostData.has_user() )
        {
            dbhProp.set_user_name(dbHostData.user());
        }

        if ( dbHostData.has_passwd() )
        {
            dbhProp.set_password(dbHostData.passwd());
        }

        this->dbHostManager->defineDatabaseHost(dbhProp);
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Defining database host failed for unknown reason.");
    }

    return message;
}

std::string RasControl::changeDbHost ( const ChangeDbHost& dbHostData )
{
    std::string message="";

    LDEBUG<<"Changing database host:"<<dbHostData.DebugString();

    try
    {
        DatabaseHostPropertiesProto dbhProto;

        if ( dbHostData.has_n_connect() )
        {
            dbhProto.set_connect_string(dbHostData.n_connect());
        }

        if ( dbHostData.has_n_user() )
        {
            dbhProto.set_user_name(dbHostData.n_user());
        }

        if ( dbHostData.has_n_passwd() )
        {
            dbhProto.set_password(dbHostData.n_passwd());
        }

        if ( dbHostData.has_n_name() )
        {
            dbhProto.set_host_name(dbHostData.n_name());
        }

        this->dbHostManager->changeDatabaseHost ( dbHostData.host_name(), dbhProto);
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Changing database host properties failed for unknown reason");
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
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Removing database host failed for unknown reason.");
    }

    return message;
}

std::string RasControl::listDbHost()
{
    std::stringstream ss;
    int counter = 1;
    DatabaseHostMgrProto dbhMgrData = this->dbHostManager->serializeToProto();

    ss<<"List of database hosts:\r\n";
    ss<<"    Database Host    Connection String             Databases";

    for(int i=0; i<dbhMgrData.database_hosts_size(); ++i)
    {
        ss<< ( format ("\r\n%2d. %-15s  %-30s" ) % counter % dbhMgrData.database_hosts(i).host_name() % dbhMgrData.database_hosts(i).connect_string());
        for(int j=0; j<dbhMgrData.database_hosts(i).databases_size(); ++j)
        {
            ss<< ( format ( "%s" ) % dbhMgrData.database_hosts(i).databases(j).name());
        }
        counter++;
    }

    return ss.str();
}

std::string RasControl::helpDbHost()
{
    return this->showHelp();
}

std::string RasControl::defineDb ( const DefineDb& dbData )
{
    std::string message="";

    LDEBUG<<"Definining database:"<<dbData.DebugString();

    try
    {
        this->dbManager->defineDatabase (dbData.dbhost_name(), dbData.db_name());
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Removing user failed for an unknown reason.");
    }

    return message;
}

std::string RasControl::changeDb ( const ChangeDb& dbData )
{
    std::string message="";

    LDEBUG<<"Changing database:"<<dbData.DebugString();

    try
    {
        DatabasePropertiesProto dbProp;
        dbProp.set_n_name(dbData.n_db_name());

        this->dbManager->changeDatabase ( dbData.db_name(), dbProp);
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Changing database failed for unknown reason");
    }

    return message;
}

std::string RasControl::removeDb ( const RemoveDb& dbData )
{
    std::string message="";

    LDEBUG<<"Removing database:"<<dbData.DebugString();

    try
    {
        this->dbManager->removeDatabase(dbData.dbhost_name() ,dbData.db_name());
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Removing database failed for unknown reason.");
    }

    return message;
}

std::string RasControl::listDb ( const ListDb& listDbData )
{
    std::stringstream ss;

    LDEBUG<<listDbData.DebugString();

    //If the Database name was given, list information about this database
    if(listDbData.has_db_name())
    {
        DatabaseMgrProto dbMgrData = this->dbManager->serializeToProto();

        ss<<"List database: "<<listDbData.db_name() <<"\r\n";
        ss<<"    Database Name         Open Trans.";

        for(int i=0; i<dbMgrData.databases_size(); ++i)
        {
            if(dbMgrData.databases(i).database().name() == listDbData.db_name())
            {
                DatabaseProto dbProto = dbMgrData.databases(i).database();

                ss<< ( format ( "\r\n%2d. %-20s  %d" ) % 1 %  dbProto.name() % dbProto.sessions_size());
            }
        }
    }
    else if(listDbData.has_dbh_name())
    {
        DatabaseMgrProto dbMgrData = this->dbManager->serializeToProto();
        bool found = false;

        for(int i=0; i<dbMgrData.databases_size(); ++i)
        {
            if(dbMgrData.databases(i).database_host() == listDbData.dbh_name())
            {
                found=true;
                break;
            }
        }

        if(found)
        {
            ss<< ( format ( "List of databases on host: %s\r\n" ) % listDbData.dbh_name() );
            ss<<"    Database Name         Open Trans.";

            int counter = 1;
            for(int i=0; i<dbMgrData.databases_size(); ++i)
            {
                if(dbMgrData.databases(i).database_host() == listDbData.dbh_name())
                {
                    DatabaseProto dbProto = dbMgrData.databases(i).database();
                    ss<< ( format ( "\r\n%2d. %-20s  %d" ) % counter % dbProto.name() % dbProto.sessions_size());
                    counter++;
                }
            }
        }
        else
        {
            ss<<this->formatErrorMessage("Invalid database host name.");
        }
    }//assume -all was passed in
    else
    {
        DatabaseMgrProto dbMgrData = this->dbManager->serializeToProto();

        ss<<"List database: \r\n";
        ss<<"    Database Name         Open Trans.";

        for(int i=0; i<dbMgrData.databases_size(); ++i)
        {
            DatabaseProto dbProto = dbMgrData.databases(i).database();

            ss<< ( format ( "\r\n%2d. %-20s  %d" ) % (i+1) %  dbProto.name() % dbProto.sessions_size());
        }
    }

    return ss.str();
}

std::string RasControl::helpDb()
{
    return this->showHelp();
}

std::string RasControl::defineUser ( const DefineUser& userData )
{
    std::string result;

    UserAdminRightsProto* adminRights = new UserAdminRightsProto();
    UserDatabaseRightsProto* dbRights = new UserDatabaseRightsProto();
    UserProto userProp;

    if ( userData.has_access_rights() )
    {
        adminRights->set_access_control_rights(userData.access_rights() );
    }

    if ( userData.has_config_rights() )
    {
        adminRights->set_system_config_rights(userData.config_rights());
    }

    if ( userData.has_info_rights() )
    {
        adminRights->set_info_rights(userData.info_rights());
    }

    if ( userData.has_server_admin_rights() )
    {
        adminRights->set_server_admin_rights(userData.server_admin_rights() );
    }

    if ( userData.has_dbread_rights() )
    {
        dbRights->set_read( userData.dbread_rights() );
    }

    if ( userData.has_dbwrite_rights() )
    {
        dbRights->set_write( userData.dbwrite_rights() );
    }

    userProp.set_name(userData.user_name());

    //Set the password
    std::string password;
    if ( userData.has_passwd() )
    {
        password = userData.passwd();

    }
    else
    {
        password= userData.user_name();
    }

    //If there is no password passed in, set the user name as the password
    if ( Crypto::isMessageDigestAvailable ( DEFAULT_DIGEST ) )
    {
        userProp.set_password(Crypto::messageDigest ( password,DEFAULT_DIGEST ));
    }
    else
    {
        LWARNING<<DEFAULT_DIGEST<<" digest not available. Storing password in plain text.";
        userProp.set_password(password);
    }

    userProp.set_allocated_admin_rights(adminRights);
    userProp.set_allocated_default_db_rights(dbRights);

    try
    {
        this->userManager->defineUser(userProp);
    }
    catch ( std::exception& ex )
    {
        result= this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        result = this->formatErrorMessage("Defining user failed for unknown reason");
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
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Removing user failed for unknown reason.");
    }

    return message;
}

std::string RasControl::changeUser ( const ChangeUser& userData )
{
    UserAdminRightsProto* adminRights = new UserAdminRightsProto();
    bool hasAdminRights=false;
    UserDatabaseRightsProto* dbRights = new UserDatabaseRightsProto();
    bool hasDbRights =  false;
    UserProto userProp;

    std::string message = "";

    try
    {
        if(userData.has_n_name())
        {
            userProp.set_name(userData.n_name());
        }

        if(userData.has_n_passwd())
        {
            if (Crypto::isMessageDigestAvailable ( DEFAULT_DIGEST ) )
            {
                userProp.set_password(Crypto::messageDigest ( userData.n_passwd(), DEFAULT_DIGEST));
            }
            else
            {
                LWARNING<<DEFAULT_DIGEST<<" digest not available. Storing password in plain text.";
                userProp.set_password(userData.n_passwd());
            }
        }

        if(userData.has_n_access_rights())
        {
            adminRights->set_access_control_rights(userData.n_access_rights());
            hasAdminRights = true;
        }

        if(userData.has_n_config_rights())
        {
            adminRights->set_system_config_rights(userData.n_config_rights());
            hasAdminRights = true;
        }

        if(userData.has_n_info_rights())
        {
            adminRights->set_info_rights(userData.n_info_rights());
            hasAdminRights = true;
        }

        if(userData.has_n_server_admin_rights())
        {
            adminRights->set_server_admin_rights(userData.n_server_admin_rights());
            hasAdminRights = true;
        }

        if(userData.has_n_dbread_rights())
        {
            dbRights->set_read(userData.n_dbread_rights());
            hasDbRights = true;
        }

        if(userData.has_n_dbwrite_rights())
        {
            dbRights->set_write(userData.n_dbwrite_rights());
            hasDbRights = true;
        }

        if(hasAdminRights)
        {
            userProp.set_allocated_admin_rights(adminRights);
        }

        if(hasDbRights)
        {
            userProp.set_allocated_default_db_rights(dbRights);
        }

        this->userManager->changeUser(userData.user_name(), userProp);
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Changing user properties failed for an unknown reason.");
    }

    return message;
}

std::string RasControl::listUser ( const ListUser& userData )
{
    std::stringstream ss;
    UserMgrProto userMgrData = this->userManager->serializeToProto();

    bool displayRights = userData.has_diplay_rights();
    int counter=1;

    ss<<"List of defined users:";
    for(int i=0; i<userMgrData.users_size(); ++i)
    {
        ss<<"\r\n"<<std::setw ( 2 ) <<counter<<". "<< userMgrData.users(i).name();
        if ( displayRights )
        {
            ss<<" ["<<this->convertAdminRights ( userMgrData.users(i).admin_rights() ) <<"]";
            ss<<" -["<<this->convertDbRights ( userMgrData.users(i).default_db_rights() ) <<"]";
        }

        counter++;
    }

    return ss.str();
}

std::string RasControl::helpUser()
{
    return this->showHelp();
}

std::string RasControl::defineInpeer ( std::string hostName )
{
    std::string message;

    try
    {
        this->peerManager->defineInPeer(hostName);
        message = "Defining inpeer rasmgr " + hostName;
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Defining inpeer failed for an unknown reason.");
    }

    return message;
}

std::string RasControl::removeInpeer ( std::string hostName )
{
    std::string message;

    try
    {
        this->peerManager->removeInPeer(hostName);
        message = "Peer "+hostName+ " removed";
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Removing inpeer failed for an unknown reason.");
    }

    return message;
}

std::string RasControl::listInpeer()
{
    std::stringstream ss;
    PeerMgrProto peerMgrData = this->peerManager->serializeToProto();

    ss<<"List of inpeers:\r\n";
    for(int i=0; i<peerMgrData.inpeers_size(); ++i)
    {
        ss<<"\r\n"<<std::setw(2)<<(i+1)<<". "<<peerMgrData.inpeers(i).host_name();
    }

    return ss.str();
}

std::string RasControl::helpInpeer()
{
    return this->showHelp();
}

std::string RasControl::defineOutpeer ( const DefineOutpeer& outpeerData )
{
    std::string message;

    try
    {
        this->peerManager->defineOutPeer(outpeerData.host_name(), outpeerData.port());
        message = "Defining outpeer rasmgr "+outpeerData.host_name()+" port="+std::to_string(outpeerData.port());
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Defining outpeer failed for an unknown reason.");
    }

    return message;
}

std::string RasControl::removeOutpeer ( std::string hostName )
{
    std::string message;

    try
    {
        this->peerManager->removeOutPeer(hostName);
        message = "Peer "+hostName+ " removed";
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Removing outpeer failed for an unknown reason.");
    }

    return message;
}

std::string RasControl::listOutpeer()
{
    std::stringstream ss;
    PeerMgrProto peerMgrData = this->peerManager->serializeToProto();

    ss<<"List of outpeers:\r\n";
    for(int i=0; i<peerMgrData.outpeers_size(); ++i)
    {
        ss<<"\r\n"<<std::setw(2)<<(i+1)<<". "<<peerMgrData.outpeers(i).host_name()<<" "<<peerMgrData.outpeers(i).port();
    }

    return ss.str();
}

std::string RasControl::helpOutpeer()
{
    return this->showHelp();
}

std::string RasControl::defineServerGroup ( const DefineServerGroup &groupData )
{
    std::string message;

    LDEBUG<<"Defining server group with the following configuration:"
          <<groupData.DebugString();
    try
    {
        ServerGroupConfigProto serverConfig;

        if(groupData.has_group_name())
        {
            serverConfig.set_name(groupData.group_name());
        }

        if(groupData.has_host())
        {
            serverConfig.set_host(groupData.host());
        }

        if(groupData.has_db_host())
        {
            serverConfig.set_db_host(groupData.db_host());
        }

        for(int i=0; i<groupData.ports_size(); ++i)
        {
            serverConfig.add_ports(groupData.ports(i));
        }

        if(groupData.has_min_alive_servers())
        {
            serverConfig.set_min_alive_server_no(groupData.min_alive_servers());
        }

        if(groupData.has_min_available_servers())
        {
            serverConfig.set_min_available_server_no(groupData.min_available_servers());
        }

        if(groupData.has_max_idle_servers())
        {
            serverConfig.set_max_idle_server_no(groupData.max_idle_servers());
        }

        if(groupData.has_autorestart())
        {
            serverConfig.set_autorestart(groupData.autorestart());
        }

        if(groupData.has_countdown())
        {
            serverConfig.set_countdown(groupData.countdown());
        }

        if(groupData.has_options())
        {
            serverConfig.set_server_options(groupData.options());
        }

        this->serverManager->defineServerGroup(serverConfig);
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Defining server group failed for unknown reason.");
    }

    return message;
}

std::string RasControl::changeServerGroup ( const ChangeServerGroup &groupData )
{
    std::string message;

    LDEBUG<<"Changing server group with the following configuration:"<<groupData.DebugString();

    try
    {
        ServerGroupConfigProto newConfig;

        if(groupData.has_n_group_name())
        {
            newConfig.set_name(groupData.n_group_name());
        }

        if(groupData.has_n_host())
        {
            newConfig.set_host(groupData.n_host());
        }

        if(groupData.has_n_db_host())
        {
            newConfig.set_db_host(groupData.n_db_host());
        }

        if(groupData.n_ports_size()!=0)
        {
            for(int i=0; i<groupData.n_ports_size(); ++i)
            {
                newConfig.add_ports(groupData.n_ports(i));
            }
        }

        if(groupData.has_n_min_alive_servers())
        {
            newConfig.set_min_alive_server_no(groupData.n_min_alive_servers());
        }

        if(groupData.has_n_min_available_servers())
        {
            newConfig.set_min_available_server_no(groupData.n_min_available_servers());
        }

        if(groupData.has_n_max_idle_servers())
        {
            newConfig.set_max_idle_server_no(groupData.n_max_idle_servers());
        }

        if(groupData.has_n_autorestart())
        {
            newConfig.set_autorestart(groupData.n_autorestart());
        }

        if(groupData.has_n_countdown())
        {
            newConfig.set_countdown(groupData.n_countdown());
        }

        if(groupData.has_n_options())
        {
            newConfig.set_server_options(groupData.n_options());
        }

        this->serverManager->changeServerGroup(groupData.group_name(), newConfig);
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Changing the server properties failed for unknown reason.");
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
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Removing server failed for unknown reason.");
    }

    return message;
}

std::string RasControl::listServerGroup ( const ListServerGroup &listData )
{
    std::stringstream ss;

    bool extraInfo = listData.has_extra_info();
    ServerMgrProto serverMgrData = this->serverManager->serializeToProto();

    if(listData.has_group_name())
    {
        ss<<(format("Status of server %s:\r\n") % listData.group_name());

    }
    else if(listData.has_host())
    {
        ss<<(format("List of servers on host %s:\r\n") % listData.host());

    }
    else
    {
        ss<<"List of servers:\r\n";
    }

    if(extraInfo)
    {
        ss<< (format("    %-20s %-8s    %-20s  %-10s") % "Server Name" % "Type" % "Host" % "  Ports");
    }
    else
    {
        ss<<(format("    %-20s %-8s    %-20s   %-20s  %-4s   %-3s") % "Server Name" % "Type" % "Host" % "Db Host" % "Stat" % "Av");
    }

    ss<<"\r\n";


    //List information about a particular group
    if (listData.has_group_name())
    {
        for(int i=0; i<serverMgrData.server_groups_size(); ++i)
        {
            if(serverMgrData.server_groups(i).name() == listData.group_name())
            {
                ServerGroupProto groupData =  serverMgrData.server_groups(i);
                std::string isRunning = groupData.running()?"UP":"DOWN";
                std::string isAvailable = groupData.available()?"YES":"NO";

                if(extraInfo)
                {
                    std::stringstream portsData;
                    for(int j=0; j<groupData.ports_size(); ++j)
                    {
                        portsData<<groupData.ports(j)<<" ";
                    }

                    ss<<(format("%2d. %-20s %-8s    %-20s    %-10s\r\n") % 1 % groupData.name() % "(RASNET)" % groupData.host() % portsData.str());
                }
                else
                {
                    ss<<(format("%2d. %-20s %-8s    %-20s   %-20s  %-4s   %-3s\r\n") % 1 % groupData.name() % "(RASNET)" % groupData.host() % groupData.db_host() %  isRunning % isAvailable );
                }

                break;
            }
        }
    }
    else if (listData.has_host()) //list information about servers running on a host
    {
        int counter = 1;
        for(int i=0; i<serverMgrData.server_groups_size(); ++i)
        {
            if(serverMgrData.server_groups(i).name() == listData.host())
            {
                ServerGroupProto groupData =  serverMgrData.server_groups(i);
                std::string isRunning = groupData.running()?"UP":"DOWN";
                std::string isAvailable = groupData.available()?"YES":"NO";

                if(extraInfo)
                {
                    std::stringstream portsData;
                    for(int j=0; j<groupData.ports_size(); ++j)
                    {
                        portsData<<groupData.ports(j)<<" ";
                    }

                    ss<<(format("%2d. %-20s %-8s    %-20s    %-10s\r\n") % counter % groupData.name() % "(RASNET)" % groupData.host() % portsData.str());
                }
                else
                {
                    ss<<(format("%2d. %-20s %-8s    %-20s   %-20s  %-4s   %-3s\r\n") % counter % groupData.name() % "(RASNET)" % groupData.host() % groupData.db_host() %  isRunning % isAvailable );
                }

                counter++;
            }
        }
    }
    else   //list information about all the servers
    {
        int counter = 1;
        for(int i=0; i<serverMgrData.server_groups_size(); ++i)
        {
            ServerGroupProto groupData =  serverMgrData.server_groups(i);
            std::string isRunning = groupData.running()?"UP":"DOWN";
            std::string isAvailable = groupData.available()?"YES":"NO";

            if(extraInfo)
            {
                std::stringstream portsData;
                for(int j=0; j<groupData.ports_size(); ++j)
                {
                    portsData<<groupData.ports(j)<<" ";
                }

                ss<<(format("%2d. %-20s %-8s    %-20s    %-10s\r\n") % counter % groupData.name() % "(RASNET)" % groupData.host() % portsData.str());
            }
            else
            {
                ss<<(format("%2d. %-20s %-8s    %-20s   %-20s  %-4s   %-3s\r\n") % counter % groupData.name() % "(RASNET)" % groupData.host() % groupData.db_host() %  isRunning % isAvailable );
            }

            counter++;
        }
    }

    return ss.str();
}

std::string RasControl::helpServerGroup()
{
    return this->showHelp();
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
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Starting servers failed for unknown reason.");
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
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Stoping servers failed for unknown reason.");
    }

    return message;
}

std::string RasControl::stopRasMgr()
{
    std::string message ="";

    LDEBUG<<"Stopping rasmgr.";

    try
    {
        if(this->serverManager->hasRunningServers())
        {
            message="This rasmgr instance has running server groups. Down all the server groups before stoping rasmgr.";
        }
        else
        {
            // I assume there is no problem if stopRasmgrAsync is called multiple times
            this->stopRasmgrThread.reset(new boost::thread(&RasControl::stopRasmgrAsync, this));
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

std::string RasControl::login()
{
    return "Welcome!";
}

std::string RasControl::save()
{
    std::string message ="";

    try
    {
        this->rasmanager->saveConfiguration();
    }
    catch ( std::exception& ex )
    {
        message = this->formatErrorMessage(ex.what());
    }
    catch ( ... )
    {
        message = this->formatErrorMessage("Saving data failed for an unknown reason.");
    }

    return message;
}

std::string RasControl::exit()
{
    return "";
}

std::string RasControl::convertAdminRights (const UserAdminRightsProto &adminRights )
{
    std::stringstream ss;
    if ( adminRights.has_access_control_rights() && adminRights.access_control_rights())
    {
        ss<<"A";
    }
    else
    {
        ss<<".";
    }

    if ( adminRights.has_info_rights() && adminRights.info_rights() )
    {
        ss<<'I';
    }
    else
    {
        ss<<'.';
    }

    if ( adminRights.has_server_admin_rights() && adminRights.server_admin_rights())
    {
        ss<<'S';
    }
    else
    {
        ss<<'.';
    }

    if ( adminRights.has_system_config_rights() && adminRights.system_config_rights())
    {
        ss<<'C';
    }
    else
    {
        ss<<'.';
    }

    return ss.str();

}
std::string RasControl::convertDbRights (const UserDatabaseRightsProto &dbRights )
{
    std::stringstream ss;

    if (dbRights.has_read() && dbRights.read() )
    {
        ss<<'R';
    }
    else
    {
        ss<<'.';
    }

    if (dbRights.has_write() && dbRights.write() )
    {
        ss<<'W';
    }
    else
    {
        ss<<'.';
    }

    return ss.str();
}

std::string RasControl::formatErrorMessage(const std::string &message)
{
    return std::string("Error:")+message;
}

std::string RasControl::getNotImplementedMes()
{
    return "This functionality is not implemented.";
}

std::string RasControl::showHelp()
{
    std::stringstream ss;

    ss<<"Help for rascontrol command language\r\n";
    ss<<"rasdaman uses the following terms:\r\n";
    ss<<"  host (server host)    - a computer running a RasManager (rasmgr), with or without currently active servers\r\n";
    ss<<"  srv  (server)         - the rasdaman server (rasserver)\r\n";
    ss<<"  dbh  (data base host) - a computer running the database software\r\n";
    ss<<"  db   (database)       - the rasdaman database, hosted by the underlying database instance\r\n";
    ss<<"  user                  - a person registered by rasdaman through user name and password\r\n";
    ss<<"  inpeer                - a peer which can forward client requests to the current RasManager\r\n";
    ss<<"  outpeer               - a peer to which the current RasManager can forward client requests\r\n";
    ss<<"\r\nThe rascontrol utility allows to configure and do run-time administration work for the rasdaman system\r\n";
    ss<<"Commands:\r\n";
    ss<<"   >help       ...this help\r\n";
    ss<<"   >exit       ...exit rascontrol\r\n";
    ss<<"   >list       ...list information about the current status of the system\r\n";
    ss<<"   >up         ...start servers\r\n";
    ss<<"   >down       ...stop servers and rasmanagers\r\n";
    ss<<"   >define     ...define a new object\r\n";
    ss<<"   >remove     ...remove an object\r\n";
    ss<<"   >change     ...change parameters of objects\r\n";
    ss<<"   >save       ...make changes permanent\r\n";
    ss<<"   >check      ...checks the current status of a slave rasmgr\r\n";
    ss<<"Type 'help command' to get specific information about command\r\n";

    return ss.str();
}

void RasControl::stopRasmgrAsync()
{
    this->rasmanager->stop();
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
bool RasControl::isValidUser (std::string userName, std::string password )
{
    boost::shared_ptr<User> user;

    return this->userManager->tryGetUser ( userName, user ) && user->getPassword() == password ;
}


}
