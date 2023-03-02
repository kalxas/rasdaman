#ifndef COMMON_MATH_HH
#define COMMON_MATH_HH

#include "common/types/model/types.hh"
#include <cmath>

namespace common
{

/**
 * isnan which handles both primitive and complex numbers.
 */
template <typename T>
bool isnan(const common::complex<T> &value)
{
    return std::isnan(value.real()) || std::isnan(value.imag());
}

template <typename T>
bool isnan(T value)
{
    return std::isnan(value);
}

}  // namespace common

#endif  // COMMON_MATH_HH
