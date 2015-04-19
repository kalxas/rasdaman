#include "util.hh"
namespace rasnet
{
std::string rasnet::Util::getMessageType(const google::protobuf::Message &message)
{
    return message.GetTypeName();
}

std::string Util::getMethodName(const google::protobuf::MethodDescriptor *method)
{
    return method->full_name();
}
}
