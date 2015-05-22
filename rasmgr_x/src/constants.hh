#ifndef RASMGR_X_SRC_CONSTANTS_HH
#define RASMGR_X_SRC_CONSTANTS_HH

#include <boost/cstdint.hpp>
#include "config.h"

namespace rasmgr
{
/**
 * @brief MIN_ALIVE_SERVER_NO Minimum number of alive(running) servers each server group
 * should keep by default while respecting the maximum number
 * of servers it can start.
 */
const boost::uint32_t MIN_ALIVE_SERVER_NO = 1;

/**
 * @brief MIN_AVAILABLE_SERVER_NO Minimum number of available servers i.e. with capacity to
 * handle requests each server group should maintain while respecting the maximum number
 * of servers it can start.
 */
const boost::uint32_t MIN_AVAILABLE_SERVER_NO = 1;

/**
 * @brief MAX_IDLE_SERVER_NO The default maximum number of idle servers
 * that each server group can keep
 */
const boost::uint32_t MAX_IDLE_SERVER_NO = 10;

/**
 * @brief AUTORESTART_SERVER Autorestart a server in case of failure
 */
const bool AUTORESTART_SERVER = true;

/**
 * @brief MAX_SERVER_SESSIONS Default maximum number of transactions a server can have
 * before being restarted
 */
const boost::uint32_t MAX_SERVER_SESSIONS = 1000;

/**
 * @brief STARTING_SERVER_LIFETIME Number of milliseconds for which a starting server is considered alive
 */
const boost::int32_t STARTING_SERVER_LIFETIME = 10000;

/**
 * @brief SERVER_MANAGER_CLEANUP_INTERVAL Number of milliseconds between consecutive cleanup runs
 * of the server manager.
 */
const boost::int32_t SERVER_MANAGER_CLEANUP_INTERVAL = 3000;

/**
 * @brief CLIENT_LIFETIME Default number of milliseconds after which a client that has not sent
 * a KeepAlive request is removed from the list of active clients.
 */
const boost::int32_t CLIENT_LIFETIME = 3000;

/**
 * @brief CLIENT_MANAGER_CLEANUP_INTERVAL Number of milliseconds between consecutive cleanup runs
 * of the client manager. This value should be larger or equal that CLIENT_LIFETIME
 */
const boost::int32_t CLIENT_MANAGER_CLEANUP_INTERVAL = 3000;

/**
 * @brief MAX_CONTROL_COMMAND_LENGTH Maximum length of a command from rascontrol.
 */
const boost::uint32_t MAX_CONTROL_COMMAND_LENGTH = 1024;
}

#endif // RASMGR_X_SRC_CONSTANTS_HH
