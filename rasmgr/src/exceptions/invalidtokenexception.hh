#pragma once
#include "common/exceptions/rasexceptions.hh"

namespace rasmgr
{

class InvalidTokenException : public common::RuntimeException
{
public:
    InvalidTokenException();
    InvalidTokenException(const std::string &details);

    virtual ~InvalidTokenException() noexcept;
};

}  // namespace rasmgr
