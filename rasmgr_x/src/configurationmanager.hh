#ifndef RASMGR_X_SRC_CONFIGURATIONMANAGER_HH
#define RASMGR_X_SRC_CONFIGURATIONMANAGER_HH

#include <boost/smart_ptr.hpp>
#include <boost/thread.hpp>

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
    ConfigurationManager(boost::shared_ptr<ControlCommandExecutor> commandExecutor,
                         boost::shared_ptr<DatabaseHostManager> dbhManager,
                         boost::shared_ptr<DatabaseManager> dbManager,
                         boost::shared_ptr<PeerManager> peerManager,
                         boost::shared_ptr<ServerManager> serverManager,
                         boost::shared_ptr<UserManager> userManager);

    virtual ~ConfigurationManager();

    void saveConfiguration(bool backup=false);

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
    boost::shared_ptr<ControlCommandExecutor> commandExecutor_;
    boost::shared_ptr<DatabaseHostManager> dbhManager_;
    boost::shared_ptr<DatabaseManager> dbManager_;
    boost::shared_ptr<PeerManager> peerManager_;
    boost::shared_ptr<ServerManager> serverManager_;
    boost::shared_ptr<UserManager> userManager_;

    std::string rasmgrConfFilePath;
    bool isDirty_;

    void loadRasMgrConf();
    void saveRasMgrConf(bool backup=false);

    void saveDatabaseHosts(std::ofstream& out);
    void saveDatabases(std::ofstream& out);
    void saveServers(std::ofstream& out);\
    void savePeers(std::ofstream& out);
};
}
#endif // RASMGR_X_SRC_CONFIGURATIONMANAGER_HH
