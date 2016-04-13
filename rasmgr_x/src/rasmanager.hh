#ifndef RASMGR_X_SRC_RASMANAGER_HH
#define RASMGR_X_SRC_RASMANAGER_HH

#include <signal.h>

#include <memory>
#include <string>

#include <boost/shared_ptr.hpp>
#include <boost/cstdint.hpp>

namespace grpc
{
class Server;
}

namespace rasmgr
{

class Configuration;
class ConfigurationManager;

/**
 * @brief The RasManager class Central class of rasmgr that performs
 * initialization of the other submodules.
 */
class RasManager
{
public:
    /**
     * @brief RasManager Initialize the class using configuration obtained from command
     * line parameters
     * @param config
     */
    RasManager ( Configuration& config );

    virtual ~RasManager();

    /**
     * @brief start Initialize rasmgr components and publish the services to
     * the network.
     */
    void start();

    /**
     * @brief stop Save the current configuration and stop the instance.
     */
    void stop();

    /**
     * @brief saveConfiguration Save this rasmanager's configuration to rasmgr.conf and rasmgr.auth
     */
    void saveConfiguration();

    void setIsConfigurationDirty(bool isDirty);
private:
    boost::shared_ptr<ConfigurationManager> configManager;
    std::unique_ptr<grpc::Server> server;

    sig_atomic_t running; /*!<True if the rasmgr is running, false otherwise */
    boost::uint32_t port; /*!< Port on which this rasmgr instance will be running */
};

}

#endif // RASMGR_X_SRC_RASMANAGER_HH
