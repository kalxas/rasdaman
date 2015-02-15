#ifndef RASMGR_X_SRC_CONFIGURATIONMANAGER_HH
#define RASMGR_X_SRC_CONFIGURATIONMANAGER_HH

#include "controlcommandexecutor.hh"
#include "usermanager.hh"

namespace rasmgr
{
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
