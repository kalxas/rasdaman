#include "zmqutil.hh"

namespace rasnet {

const std::string ZmqUtil::TCP_PREFIX = "tcp://";
const std::string ZmqUtil::INPROC_PREFIX = "inproc://";

const std::string ZmqUtil::ALL_LOCAL_INTERFACES = ZmqUtil::TCP_PREFIX + "*";

} /* namespace rasnet */
