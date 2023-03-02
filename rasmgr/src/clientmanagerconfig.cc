#include "clientmanagerconfig.hh"
#include "constants.hh"

namespace rasmgr
{

ClientManagerConfig::ClientManagerConfig()
{
    this->clientLifeTime = CLIENT_LIFETIME;
    this->cleanupInterval = CLIENT_MANAGER_CLEANUP_INTERVAL;
    this->maxClientQueueSize = MAX_CLIENT_QUEUE_SIZE;
}

ClientManagerConfig::~ClientManagerConfig()
{
}

std::int32_t ClientManagerConfig::getCleanupInterval() const
{
    return cleanupInterval;
}

void ClientManagerConfig::setCleanupInterval(const std::int32_t &value)
{
    cleanupInterval = value;
}

std::int32_t ClientManagerConfig::getClientLifeTime() const
{
    return clientLifeTime;
}

void ClientManagerConfig::setClientLifeTime(const std::int32_t &value)
{
    clientLifeTime = value;
}

int32_t ClientManagerConfig::getMaxClientQueueSize() const
{
    return maxClientQueueSize;
}

void ClientManagerConfig::setMaxClientQueueSize(const int32_t &value)
{
    maxClientQueueSize = value;
}

}  // namespace rasmgr
