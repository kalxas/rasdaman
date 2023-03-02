#ifndef RASMGR_X_SRC_SERVERMANAGERCONFIG_HH
#define RASMGR_X_SRC_SERVERMANAGERCONFIG_HH

#include <cstdint>

namespace rasmgr
{
/**
 * Configuration for the ServerManager, in particular:
 * - number of milliseconds between consecutive cleanup runs for dead servers
 */
class ServerManagerConfig
{
public:
    ServerManagerConfig();
    virtual ~ServerManagerConfig() = default;

    /**
     * @return number of milliseconds between consecutive cleanup runs for dead servers
     */
    std::int32_t getCleanupInterval() const;

    void setCleanupInterval(std::int32_t value);

    /**
     * @return number of seconds to delay before restarting rasservers.
     */
    unsigned int getRestartDelay() const;

    void setRestartDelay(unsigned int value);

private:
    std::int32_t cleanupInterval;
    unsigned int restartDelay;
};

}  // namespace rasmgr

#endif  // RASMGR_X_SRC_SERVERMANAGERCONFIG_HH
