
#include "constants.hh"
#include "servermanagerconfig.hh"

namespace rasmgr
{
ServerManagerConfig::ServerManagerConfig()
{
    this->cleanupInterval = SERVER_MANAGER_CLEANUP_INTERVAL;
}

ServerManagerConfig::~ServerManagerConfig()
{
}


boost::int32_t ServerManagerConfig::getCleanupInterval() const
{
    return cleanupInterval;
}

void ServerManagerConfig::setCleanupInterval(const boost::int32_t& value)
{
    cleanupInterval = value;
}

}
