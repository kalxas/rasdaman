#ifndef RASMGR_X_SRC_SERVERGROUPIMPL_HH
#define RASMGR_X_SRC_SERVERGROUPIMPL_HH

#include <cstdint>
#include <list>
#include <boost/thread/shared_mutex.hpp>
#include "common/time/timer.hh"

#include "rasmgr/src/messages/rasmgrmess.pb.h"

#include "servergroup.hh"

namespace rasmgr
{

class DatabaseHost;
class DatabaseHostManager;
class Server;
class ServerFactory;

/**
 * A group of servers with the same properties, running on a set of predetermined ports.
 * The Group is responsible for managing its servers and maintaining the number of alive
 * and available servers.
 */
class ServerGroupImpl: public ServerGroup
{
public:
    /**
      * @brief ServerGroup Initialize a new instance of the ServerGroup class.
      * @param config ServerGroupConfigProto used to initialize this group.
      * Default values will be set and a validation will be performed on the configuration
      * @param dbhManager Database Host Manager used to retrieve the database host
      * used by servers of this server group.
      * @param serverFactory the server creationg factory
      */
    ServerGroupImpl(const ServerGroupConfigProto &config,
                    std::shared_ptr<DatabaseHostManager> dbhManager,
                    std::shared_ptr<ServerFactory> serverFactory);

    virtual ~ServerGroupImpl();

    /**
     * @brief start Mark this server group as active and start the minimum number of
     * servers as specified in the server group config
     */
    virtual void start() override;

    /**
     * @brief isStopped Check if the server group has been stopped.
     * @return TRUE if the server group was stopped, FALSE otherwise
     */
    virtual bool isStopped() override;

    /**
     * @brief stop Stop the running servers of this group and
     * prevent any other servers from being started.
     */
    virtual void stop(KillLevel level) override;

    /**
     * @brief tryRegisterServer Register the server with the given ID as running.
     * @param serverId UUID that uniquely identifies the RasServer instance
     * @return TRUE if there was a server with the given serverId that was
     * starting and was successfully started, FALSE otherwise
     */
    virtual bool tryRegisterServer(const std::string &serverId) override;

    /**
     * @brief evaluateServerGroup Evaluate each server in this server group.
     * 1. Remove servers that are not responding to ping requests.
     * 2. Keep the configured minimum number of available servers
     * 3. Keep the configured minimum number of running servers
     * 4. Remove servers if there is extra, unused capacity
     */
    virtual void evaluateServerGroup() override;

    /**
     * If this server group has a server containing the
     * database given by dbName which has capacity for at least one more client,
     * assign a reference to RasServer to out_server. If the method returns TRUE,
     * out_server will contain a reference to the server, otherwise, the contents of
     * out_server are unknown.
     * @param dbName Name of the database that the client needs to run queries on
     * @param out_server shared_ptr to the RasServer instance
     * @return TRUE if there is a free server, false otherwise.
     */
    virtual bool tryGetAvailableServer(const std::string &dbName, std::shared_ptr<Server> &out_server) override;

    virtual ServerGroupConfigProto getConfig() const override;

    virtual void changeGroupConfig(const ServerGroupConfigProto &value) override;

    virtual std::string getGroupName() const override;

    virtual ServerGroupProto serializeToProto() override;

private:
    ServerGroupConfigProto config;/*!< Configuration for this group */

    std::shared_ptr<DatabaseHostManager> dbhManager; /*!< Reference to the DBH manager for retrieving dbh*/

    std::shared_ptr<ServerFactory> serverFactory;

    std::list<std::shared_ptr<Server>> runningServers;

    std::list<std::shared_ptr<Server>> restartingServers;/*!< List of servers that were restarted*/

    std::shared_ptr<DatabaseHost> databaseHost;

    /**
     * List of servers that are starting but have not yet registered
     */
    std::map<std::string, std::pair<std::shared_ptr<Server>, common::Timer>> startingServers;

    boost::shared_mutex groupMutex;

    bool stopped;/*!< TRUE if the group is stopped, FALSE otherwise*/

    std::set<std::int32_t> availablePorts;/*!< */

    int failedRegistrations{};/*!< number of consecutive times a server failed to register */

    /**
     * @return TRUE if there is at least one server with capacity for one more client,
     * FALSE otherwise
     */
    bool hasAvailableServers();

    void evaluateGroup();

    /**
     * Check if any servers need to be restarted.
     */
    void evaluateServersToRestart();

    /**
     * Remove dead servers and return the configuration data to the pool.
     */
    void removeDeadServers();

    /**
     * Start a new server if there are any servers that has not already been started.
     */
    void startServer();

    void stopActiveServers(KillLevel level);

    void validateAndInitConfig(ServerGroupConfigProto &config);
};
}

#endif // RASMGR_X_SRC_SERVERGROUPIMPL_HH
