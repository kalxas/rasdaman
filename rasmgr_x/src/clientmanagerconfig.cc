#include "clientmanagerconfig.hh"

namespace rasmgr
{

ClientManagerConfig::ClientManagerConfig(int32_t clientLifeTime, int32_t cleanupInterval)
{
    this->clientLifeTime=clientLifeTime;
    this->cleanupInterval=cleanupInterval;
}

ClientManagerConfig::~ClientManagerConfig()
{}


boost::int32_t ClientManagerConfig::getCleanupInterval() const
{
    return cleanupInterval;
}

void ClientManagerConfig::setCleanupInterval(const boost::int32_t &value)
{
    cleanupInterval = value;
}

boost::int32_t ClientManagerConfig::getClientLifeTime() const
{
    return clientLifeTime;
}

void ClientManagerConfig::setClientLifeTime(const boost::int32_t &value)
{
    clientLifeTime = value;
}

}
