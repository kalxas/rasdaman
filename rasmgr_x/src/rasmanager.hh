#ifndef RASMGR_X_SRC_RASMANAGER_HH
#define RASMGR_X_SRC_RASMANAGER_HH

// rasmgr return codes
#define RASMGR_RESULT_OK        0
#define RASMGR_RESULT_NO_MD5        1
#define RASMGR_RESULT_ILL_ARGS      2
#define RASMGR_RESULT_LICENSE_FAIL  3
#define RASMGR_RESULT_NOT_ALONE     4
#define RASMGR_RESULT_AUTH_CORRUPT  5
#define RASMGR_RESULT_AUTH_OTHERHOST    6
#define RASMGR_RESULT_AUTH_INCOMPAT 7
#define RASMGR_RESULT_NO_SLAVE_IN_TEST  8
#define RASMGR_EXIT_FAILURE     9
#define RASMGR_RESULT_INTERNAL      10

#include <signal.h>

#include <boost/shared_ptr.hpp>

#include "rasnet/src/server/servicemanager.hh"

#include "configurationmanager.hh"
#include "configuration.hh"
namespace rasmgr
{

class RasManager
{
public:
    RasManager ( Configuration& config );
    virtual ~RasManager();

    void start();
    void stop();

    /**
     * @brief saveConfiguration Save this rasmanager's configuration to rasmgr.conf and rasmgr.auth
     */
    void saveConfiguration();
private:
    boost::shared_ptr<rasnet::ServiceManager> serviceManager;
    boost::shared_ptr<ConfigurationManager> configManager;

    sig_atomic_t running; /*!<True if the rasmgr is running, false otherwise */
    boost::uint32_t port; /*!< Port on which this rasmgr instance will be running */
};

}

#endif // RASMGR_X_SRC_RASMANAGER_HH
