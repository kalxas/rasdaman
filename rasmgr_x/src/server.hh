#ifndef RASMGR_X_SRC_SERVER_HH_
#define RASMGR_X_SRC_SERVER_HH_

#include <string>

#include <boost/cstdint.hpp>

#include "rasmgr_x/src/messages/rasmgrmess.pb.h"

namespace rasmgr
{

class UserDatabaseRights;

/**
 * @brief The Server class Abstract class representing the interface to a server.
 */
class Server
{
public:
    virtual ~Server();

    /**
     * Start the RasServer Process.
     */
    virtual void startProcess() = 0;

    /**
     * @return TRUE if the server replies to pings, false otherwise.
     */
    virtual bool isAlive() = 0;

    /**
     * Check if the client with the given ID is alive.
     * @param clientId UUID of the client.
     * @return true if the client is alive, false otherwise
     * @throws runtime_error if the server cannot be contacted.
     */
    virtual bool isClientAlive(const std::string &clientId) = 0;

    /**
     * Allocate the client with the given ID and session ID to the server and respective database.
     * @param clientId UUID of the client
     * @param sessionId UUID of the session.
     * @param dbName name of the database which will be opened.
     * @param dbRights rights the client has on this database
     */
    virtual void allocateClientSession(const std::string &clientId, const std::string &sessionId, const std::string &dbName, const UserDatabaseRights &dbRights) = 0;

    /**
     * Remove the client with the given ID and session ID from the server.
     * @param clientId
     * @param sessionId
     */
    virtual void deallocateClientSession(const std::string &clientId, const std::string &sessionId) = 0;

    /**
     * Register this server and transfer it to the FREE state.
     * This is called when the server is initalized.
     * The serverId must be the same as the one used to initialize the object.
     * @param serverId UUID used to identify the server
     */
    virtual void registerServer(const std::string &serverId) = 0;

    /**
     * @brief getTransactionNo Get the number of client sessions processed
     * by this server throughout its lifetime.
     * This method is used by the ServerGroup to restart a server once
     * it has reached a number of sessions
     * (to prevent memory leaks from getting out of control)
     * @return
     */
    virtual boost::uint32_t getTotalSessionNo() = 0;

    /**
     * Stop the RasServer process.
     * @param force TRUE if the server should abort any running transaction and terminate,
     * FALSE if the server should terminate after it finishes all running transactions.
     * The server will not accept any more clients from this point.
     */
    virtual void stop(KillLevel level) = 0;

    /**
     * @return True if the server process was started but the server has not registered with RasMgr
     */
    virtual bool isStarting() = 0;
    /**
     * @return True if the server does not have any clients assigned, false otherwise.
     */
    virtual bool isFree() = 0;

    /**
     * @return True if the server has available capacity, false otherwise
     */
    virtual bool isAvailable() = 0;

    /**
     * @return the port on which the server is running.
     */
    virtual boost::int32_t getPort() const = 0 ;

    /**
     * @return the name of the host on which the server is running
     */
    virtual std::string getHostName() const = 0;

    /**
     * @return the UUID of the server
     */
    virtual std::string getServerId() const = 0;
};
}

#endif // SERVER_HH
