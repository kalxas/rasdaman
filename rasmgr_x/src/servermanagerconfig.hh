#ifndef RASMGR_X_SRC_SERVERMANAGERCONFIG_HH
#define RASMGR_X_SRC_SERVERMANAGERCONFIG_HH

#include "boost/cstdint.hpp"

namespace rasmgr
{
class ServerManagerConfig
{
public:
    ServerManagerConfig();
    virtual ~ServerManagerConfig();

    boost::int32_t getCleanupInterval() const;
    void setCleanupInterval(const boost::int32_t &value);

private:
    boost::int32_t cleanupInterval;
};

}


#endif // RASMGR_X_SRC_SERVERMANAGERCONFIG_HH
