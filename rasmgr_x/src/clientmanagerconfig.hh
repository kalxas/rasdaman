#ifndef RASMGR_X_SRC_CLIENTMANAGERCONFIG_HH
#define RASMGR_X_SRC_CLIENTMANAGERCONFIG_HH

#include <boost/cstdint.hpp>

namespace rasmgr
{
/**
 * @brief The ClientManagerConfig class Configuration object used to initialize
 * the ClientManager
 */
class ClientManagerConfig
{
public:
    ClientManagerConfig();
    virtual ~ClientManagerConfig();

    boost::int32_t getCleanupInterval() const;
    void setCleanupInterval(const boost::int32_t &value);

    boost::int32_t getClientLifeTime() const;
    void setClientLifeTime(const boost::int32_t &value);

private:
    boost::int32_t clientLifeTime;/*!< The number of milliseconds that defines the client's life time*/
    boost::int32_t cleanupInterval;/*!< The number of milliseconds between each successive run of the ClientManager's cleaup thread*/
};

}

#endif // RASMGR_X_SRC_CLIENTMANAGERCONFIG_HH
