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
#ifndef RASMGR_X_SRC_CONFIGURATIONMANAGER_HH
#define RASMGR_X_SRC_CONFIGURATIONMANAGER_HH

#include <memory>
#include <string>
#include <iosfwd>

namespace rasmgr
{

class ControlCommandExecutor;
class DatabaseHostManager;
class DatabaseManager;
class PeerManager;
class ServerManager;
class UserManager;

/**
 * Used to save/load the list of users and their associated information and the
 * rasmgr.conf file.
 */
class ConfigurationManager
{
public:
    ConfigurationManager(std::shared_ptr<ControlCommandExecutor> commandExecutor,
                         std::shared_ptr<DatabaseHostManager> dbhManager,
                         std::shared_ptr<DatabaseManager> dbManager,
                         std::shared_ptr<PeerManager> peerManager,
                         std::shared_ptr<ServerManager> serverManager,
                         std::shared_ptr<UserManager> userManager);

    virtual ~ConfigurationManager();

    void saveConfiguration(bool backup = false);

    /**
     * @brief loadConfiguration Load the configuration stored in rasmgr.auth and rasmgr.conf
     */
    void loadConfiguration();

    /**
     * @brief isDirty This method should return true when ramgr's configuration was changed,
     * false when it was not
     * @return
     */
    bool isDirty() const;

    /**
     * @brief setIsDirty This method should be called from rascontrol.
     * @param isDirty
     */
    void setIsDirty(bool isDirty);

private:
    std::shared_ptr<ControlCommandExecutor> commandExecutor_;
    std::shared_ptr<DatabaseHostManager> dbhManager_;
    std::shared_ptr<DatabaseManager> dbManager_;
    std::shared_ptr<PeerManager> peerManager_;
    std::shared_ptr<ServerManager> serverManager_;
    std::shared_ptr<UserManager> userManager_;

    std::string rasmgrConfFilePath;
    bool isDirty_;

    void loadRasMgrConf();
    void saveRasMgrConf(bool backup = false);

    void saveDatabaseHosts(std::ofstream &out);
    void saveDatabases(std::ofstream &out);
    void saveServers(std::ofstream &out);

    void savePeers(std::ofstream &out);
};
}  // namespace rasmgr
#endif  // RASMGR_X_SRC_CONFIGURATIONMANAGER_HH
