#ifndef RASNET_SRC_EXCEPTION_EXCEPTIONMESSAGES_HH
#define RASNET_SRC_EXCEPTION_EXCEPTIONMESSAGES_HH

#include <string>

namespace rasnet
{
const std::string INVALID_METHOD_NAME = "There is no method with the given name on this server.";
const std::string INVALID_SERVICE_NAME = "There is no service with the given name offered by this server.";
const std::string INVALID_INPUT_DATA = "The input data is unparsable.";
const std::string UNKOWN_SERVICE_CALL_FAILURE = "The method call failed for an unknown reason.";
}


#endif // RASNET_SRC_EXCEPTION_EXCEPTIONMESSAGES_HH
