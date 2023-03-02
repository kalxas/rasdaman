
#include "constants.hh"
#include "servermanagerconfig.hh"

namespace rasmgr
{
ServerManagerConfig::ServerManagerConfig()
    : cleanupInterval(SERVER_MANAGER_CLEANUP_INTERVAL),
      restartDelay(SERVER_MANAGER_RESTART_DELAY)
{
}

std::int32_t ServerManagerConfig::getCleanupInterval() const
{
    return cleanupInterval;
}

void ServerManagerConfig::setCleanupInterval(std::int32_t value)
{
    cleanupInterval = value;
}

unsigned int ServerManagerConfig::getRestartDelay() const
{
    return restartDelay;
}

void ServerManagerConfig::setRestartDelay(unsigned int value)
{
    restartDelay = value;
}

}  // namespace rasmgr
