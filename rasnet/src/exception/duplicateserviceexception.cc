#include "duplicateserviceexception.hh"

namespace rasnet
{
DuplicateServiceException::DuplicateServiceException():
    std::runtime_error("The service is already part of the collection.")
{}

DuplicateServiceException::DuplicateServiceException(const std::string &what_arg)
    :  std::runtime_error(what_arg)
{}

}
