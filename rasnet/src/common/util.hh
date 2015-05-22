#ifndef RASNET_SRC_COMMON_UTIL_HH
#define RASNET_SRC_COMMON_UTIL_HH

#include <string>
#include <google/protobuf/message.h>

namespace rasnet
{
class Util
{
public:
    /**
     * @brief getMessageType
     * @param message
     * @return Fully qualified message name
     */
    static std::string getMessageType(const ::google::protobuf::Message& message);

    /**
     * @brief getMethodName
     * @param method
     * @return Fully qualified method name
     */
    static std::string getMethodName(const ::google::protobuf::MethodDescriptor *method);
};
}
#endif // RASNET_SRC_COMMON_UTIL_HH
