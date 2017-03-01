#include "clientmanagerconfig.hh"
#include "constants.hh"

namespace rasmgr
{

ClientManagerConfig::ClientManagerConfig()
{
    this->clientLifeTime = CLIENT_LIFETIME;
    this->cleanupInterval = CLIENT_MANAGER_CLEANUP_INTERVAL;
}

ClientManagerConfig::~ClientManagerConfig()
{}


boost::int32_t ClientManagerConfig::getCleanupInterval() const
{
    return cleanupInterval;
}

void ClientManagerConfig::setCleanupInterval(const boost::int32_t& value)
{
    cleanupInterval = value;
}

boost::int32_t ClientManagerConfig::getClientLifeTime() const
{
    return clientLifeTime;
}

void ClientManagerConfig::setClientLifeTime(const boost::int32_t& value)
{
    clientLifeTime = value;
}

}
