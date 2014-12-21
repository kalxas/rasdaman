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

#ifndef RASMGR_X_SRC_RASCONTROL_HH
#define RASMGR_X_SRC_RASCONTROL_HH

#include <boost/smart_ptr.hpp>

#include "messages/rasmgrmess.pb.h"

#include "usermanager.hh"
#include "databasehostmanager.hh"
#include "databasemanager.hh"
#include "servermanager.hh"

namespace rasmgr
{
class RasManager;

class RasControl
{
public:
    RasControl ( boost::shared_ptr<UserManager> userManager, boost::shared_ptr<DatabaseHostManager> dbHostManager,boost::shared_ptr<DatabaseManager> dbManager,boost::shared_ptr<ServerManager> serverManager, RasManager* rasmanager);

    std::string deprecatedCommand();

    std::string defineDbHost ( const DefineDbHost& dbHostData );
    std::string changeDbHost ( const ChangeDbHost& dbHostData );
    std::string removeDbHost ( const RemoveDbHost& dbHostData );
    std::string listDbHost();
    std::string helpDbHost();

    std::string defineDb ( const DefineDb& dbData );
    std::string changeDb ( const ChangeDb& dbData );
    std::string removeDb ( const RemoveDb& dbData );
    std::string listDb ( const ListDb& listDbData );
    std::string helpDb();

    std::string defineUser ( const DefineUser& userData );
    std::string removeUser ( const RemoveUser& userData );
    std::string changeUser ( const ChangeUser& userData );
    std::string listUser ( const ListUser& userData );
    std::string helpUser();

    std::string defineInpeer ( std::string hostName );
    std::string removeInpeer ( std::string hostName );
    std::string listInpeer();
    std::string helpInpeer();

    std::string defineOutpeer ( const DefineOutpeer& outpeerData );
    std::string removeOutpeer ( std::string hostName );
    std::string listOutpeer();
    std::string helpOutpeer();

    std::string defineServerGroup ( const DefineServerGroup& groupData );
    std::string changeServerGroup ( const ChangeServerGroup& groupData );
    std::string removeServerGroup ( std::string groupName );
    std::string listServerGroup ( const ListServerGroup& listData );
    std::string helpServerGroup();

    std::string startServerGroup ( const StartServerGroup& upSrv );
    std::string stopServerGroup ( const StopServerGroup& downSrv );

    std::string stopRasMgr();

    std::string listVersion();

	std::string login();

    /**
     * @brief Save all the configuration and authentication information to file.
     *
     * @return std::string
     */
    std::string save();

    std::string exit();


    bool hasInfoRights ( std::string userName,std::string password );
    bool hasConfigRights ( std::string userName,std::string password );
    bool hasUserAdminRights ( std::string userName,std::string password );
    bool hasServerAdminRights ( std::string userName,std::string password );
	bool isValidUser(std::string userName, std::string password);
private:
    boost::shared_ptr<UserManager> userManager;
    boost::shared_ptr<DatabaseHostManager> dbHostManager;
    boost::shared_ptr<DatabaseManager> dbManager;
    boost::shared_ptr<ServerManager> serverManager;
	RasManager* rasmanager;

    std::string convertAdminRights ( const UserAdminRights& adminRights );
    std::string convertDbRights ( const UserDatabaseRights& dbRights );

};
}

#endif // RASCONTROL_HH
