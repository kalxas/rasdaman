#ifndef RASMGR_X_SRC_RASMANAGER_HH
#define RASMGR_X_SRC_RASMANAGER_HH

#include <signal.h>
#include <memory>
#include <string>
#include <cstdint>

namespace grpc
{
class Server;
}

namespace rasmgr
{

class Configuration;
class ConfigurationManager;

/**
 * Central class of rasmgr that performs initialization of the other
 * submodules.
 */
class RasManager
{
public:
    /**
     * Initialize using configuration obtained from cmd-line parameters/
     */
    RasManager(Configuration &config
               );

    virtual ~RasManager() = default;

    /**
     * Initialize rasmgr components and publish the services to the network.
     */
    void start();

    /**
     * Save the current configuration and stop the instance.
     */
    void stop();

    /**
     * Save this rasmanager's configuration to rasmgr.conf and rasmgr.auth
     */
    void saveConfiguration();

    void setIsConfigurationDirty(bool isDirty);
private:
    std::shared_ptr<ConfigurationManager> configManager;
    std::unique_ptr<grpc::Server> server;

    sig_atomic_t running; /*!<True if the rasmgr is running, false otherwise */
    std::uint32_t port; /*!< Port on which this rasmgr instance will be running */
};

}

#endif // RASMGR_X_SRC_RASMANAGER_HH
