#include "resourcebusyexception.hh"

namespace common
{
ResourceBusyException::ResourceBusyException(const std::string &whatArg)
    :RuntimeException(whatArg)
{}

ResourceBusyException::~ResourceBusyException() throw()
{}

}
