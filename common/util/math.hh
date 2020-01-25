#ifndef COMMON_MATH_HH
#define COMMON_MATH_HH

#ifdef ENABLE_COMPLEX
#include "common/types/model/types.hh"
#endif
#include <cmath>


namespace common
{

/**
 * isnan which handles both primitive and complex numbers.
 */
#ifdef ENABLE_COMPLEX
template <typename T>
bool isnan(const common::complex<T> &value) {
  return std::isnan(value.real()) || std::isnan(value.imag());
}
#endif
template <typename T>
bool isnan(T value) {
  return std::isnan(value);
}

}

#endif // COMMON_MATH_HH
