#ifndef RASMGR_X_SRC_CONFIGURATIONMANAGER_HH
#define RASMGR_X_SRC_CONFIGURATIONMANAGER_HH

#include <memory>

namespace rasmgr
{

class ControlCommandExecutor;
class DatabaseHostManager;
class DatabaseManager;
class PeerManager;
class ServerManager;
class UserManager;

/**
 * @brief The ConfigurationManager class Used to save/load the list of users and their
 * associated information and the rasmgr.conf file.
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
    \
    void savePeers(std::ofstream &out);
};
}
#endif // RASMGR_X_SRC_CONFIGURATIONMANAGER_HH
