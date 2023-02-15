
#include "invalidtokenexception.hh"

rasmgr::InvalidTokenException::InvalidTokenException() : RuntimeException("The token provided by the client is invalid.") {}

rasmgr::InvalidTokenException::InvalidTokenException(const std::string &details)
: RuntimeException("The token provided by the client is invalid: " + details) {}

rasmgr::InvalidTokenException::~InvalidTokenException() {}
