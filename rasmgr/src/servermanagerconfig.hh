#ifndef RASMGR_X_SRC_SERVERMANAGERCONFIG_HH
#define RASMGR_X_SRC_SERVERMANAGERCONFIG_HH

#include <cstdint>

namespace rasmgr
{
class ServerManagerConfig
{
public:
    ServerManagerConfig();
    virtual ~ServerManagerConfig();

    std::int32_t getCleanupInterval() const;
    void setCleanupInterval(const std::int32_t &value);

private:
    std::int32_t cleanupInterval;
};

}


#endif // RASMGR_X_SRC_SERVERMANAGERCONFIG_HH
