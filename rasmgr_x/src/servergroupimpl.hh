#ifndef RASMGR_X_SRC_SERVERGROUPIMPL_HH
#define RASMGR_X_SRC_SERVERGROUPIMPL_HH

#include <boost/cstdint.hpp>

#include <boost/thread.hpp>

#include "common/time/timer.hh"

#include "rasmgr_x/src/messages/rasmgrmess.pb.h"

#include "servergroup.hh"

namespace rasmgr
{

class DatabaseHost;
class DatabaseHostManager;
class Server;
class ServerFactory;

class ServerGroupImpl: public ServerGroup
{
public:
    /**
      * @brief ServerGroup Initialize a new instance of the ServerGroup class.
      * @param config ServerGroupConfigProto used to initialize this group.
      * Default values will be set and a validation will be performed on the configuration
      * @param dbhManager Database Host Manager used to retrieve the database host
      * used by servers of this server group.
      * @param serverFactory
      */
    ServerGroupImpl(const ServerGroupConfigProto &config, boost::shared_ptr<DatabaseHostManager> dbhManager, boost::shared_ptr<ServerFactory> serverFactory);

    virtual ~ServerGroupImpl();

    /**
     * @brief start Mark this server group as active and start the minimum number of
     * servers as specified in the server group config
     */
    virtual void start();

    /**
     * @brief isStopped Check if the server group has been stopped.
     * @return TRUE if the server group was stopped, FALSE otherwise
     */
    virtual bool isStopped();

    /**
     * @brief stop Stop the running servers of this group and
     * prevent any other servers from being started.
     */
    virtual void stop(KillLevel level);

    /**
     * @brief tryRegisterServer Register the server with the given ID as running.
     * @param serverId UUID that uniquely identifies the RasServer instance
     * @return TRUE if there was a server with the given serverId that was
     * starting and was successfully started, FALSE otherwise
     */
    virtual bool tryRegisterServer(const std::string &serverId);

    /**
     * @brief evaluateServerGroup Evaluate each server in this server group.
     * 1. Remove servers that are not responding to ping requests.
     * 2. Keep the configured minimum number of available servers
     * 3. Keep the configured minimum number of running servers
     * 4. Remove servers if there is extra, unused capacity
     */
    virtual void evaluateServerGroup();

    /**
     * @brief getAvailableServer If this server group has a server containing the
     * database given by dbName which has capacity for at least one more client,
     * assign a reference to RasServer to out_server. If the method returns TRUE,
     * out_server will contain a reference to the server, otherwise, the contents of
     * out_server are unknown.
     * @param dbName Name of the database that the client needs to run queries on
     * @param out_server shared_ptr to the RasServer instance
     * @return TRUE if there is a free server, false otherwise.
     */
    virtual bool tryGetAvailableServer(const std::string &dbName, boost::shared_ptr<Server> &out_server);

    /**
     * @brief getConfig Get a copy of the ServerGroupConfig
     * object used to create this ServerGroup.
     * @return
     */
    virtual ServerGroupConfigProto getConfig() const;

    /**
     * @brief setConfig Set a new configuration for
     * this ServerGroup object.
     * @param value
     */
    virtual void changeGroupConfig(const ServerGroupConfigProto &value) ;

    /**
     * @brief getGroupName Get the name of this group.
     * @return
     */
    virtual std::string getGroupName() const;

    virtual ServerGroupProto serializeToProto();

private:
    ServerGroupConfigProto config;/*!< Configuration for this group */

    boost::shared_ptr<DatabaseHostManager> dbhManager; /*!< Reference to the DBH manager for retrieving dbh*/

    boost::shared_ptr<ServerFactory> serverFactory;

    std::list<boost::shared_ptr<Server>> runningServers;

    std::list<boost::shared_ptr<Server>> restartingServers;/*!< List of servers that were restarted*/

    boost::shared_ptr<DatabaseHost> databaseHost;

    /**
     * @brief startingServers List of servers that are starting but have not yet registered
     */
    std::map<std::string, std::pair<boost::shared_ptr<Server>, common::Timer>> startingServers;

    boost::shared_mutex groupMutex;

    bool stopped;/*!< TRUE if the group is stopped, FALSE otherwise*/

    std::set<boost::int32_t> availablePorts;/*!< */

    int failedRegistrations{};/*!< number of consecutive times a server failed to register */

    /**
     * @brief hasAvailableServers
     * @return TRUE if there is at least one server with capacity for one more client,
     * FALSE otherwise
     */
    bool hasAvailableServers();

    void evaluateGroup();

    void evaluateRestartingServers();

    /**
     * @brief cleanupServerList Remove dead servers and return the configuration
     * data to the pool.
     */
    void removeDeadServers();

    /**
     * @brief startServer Start a new server if there are any servers
     * that has not already been started.
     * @return Reference to the RasServer object that represents the
     * RasServer process.
     */
    void startServer();

    void stopActiveServers(KillLevel level);

    void validateAndInitConfig(ServerGroupConfigProto &config);
};
}

#endif // RASMGR_X_SRC_SERVERGROUPIMPL_HH
