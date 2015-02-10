#ifndef RASMGR_X_SRC_CONSTANTS_HH
#define RASMGR_X_SRC_CONSTANTS_HH

#include <boost/cstdint.hpp>

namespace rasmgr
{
const boost::uint32_t MIN_ALIVE_SERVER_NO = 1;
const boost::uint32_t MIN_AVAILABLE_SERVER_NO = 1;
const boost::uint32_t MAX_IDLE_SERVER_NO = 10;
const bool AUTORESTART_SERVER = true;
const boost::uint32_t MAX_SERVER_SESSIONS = 200;
const boost::int32_t STARTING_SERVER_LIFETIME = 1000;//Number of milliseconds for which a starting server is considered alive
const boost::int32_t SERVER_MANAGER_CLEANUP_INTERVAL = 3000;
const boost::int32_t CLIENT_LIFETIME = 3000;
const boost::int32_t CLIENT_MANAGER_CLEANUP_INTERVAL = 3000;
}

#endif // RASMGR_X_SRC_CONSTANTS_HH
