#ifndef RASMGR_X_SRC_CONFIGURATIONMANAGER_HH
#define RASMGR_X_SRC_CONFIGURATIONMANAGER_HH

#include <boost/smart_ptr.hpp>

namespace rasmgr
{

class ControlCommandExecutor;
class UserManager;

/**
 * @brief The ConfigurationManager class Used to save/load the list of users and their
 * associated information and the rasmgr.conf file.
 */
class ConfigurationManager
{
public:
    ConfigurationManager(boost::shared_ptr<ControlCommandExecutor> commandExecutor,
                         boost::shared_ptr<UserManager> userManager);

    virtual ~ConfigurationManager();

    void saveConfiguration();

    void loadConfiguration();
private:
    boost::shared_ptr<ControlCommandExecutor> commandExecutor;
    boost::shared_ptr<UserManager> userManager;

    void loadRasMgrConf();
};
}
#endif // RASMGR_X_SRC_CONFIGURATIONMANAGER_HH
