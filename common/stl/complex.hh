#pragma once

#ifdef ENABLE_COMPLEX

#include <complex>

#include "src/common/types/model/typetraits.hh"

namespace std {

/**
 * In this file we extend the capabilities of the standard complex<T> class by
 * adding new operators.
 * As of C++14, std::complex<T> is only defined when T is a float, double or
 * long double.
 * The operators defined in this file are meant to allow operations of the type:
 * (complex<T> op R) where R is a primitive, arithmetic type.
 * The result of any operation is always complex<T>.
 */

template <typename L, typename R>
auto operator+(const complex<L>& lhs, R rhs) -> complex<decltype(L{} + R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);

  using ResultType = decltype(L{} + R{});

  return complex<ResultType>{lhs.real() + rhs, lhs.imag() + rhs};
}

template <typename L, typename R>
auto operator+(L lhs, const complex<R>& rhs) -> complex<decltype(L{} + R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);

  using ResultType = decltype(L{} + R{});

  return complex<ResultType>{lhs + rhs.real(), lhs + rhs.imag()};
}

template <typename L, typename R>
auto operator+(const complex<L>& lhs, const complex<R>& rhs)
    -> complex<decltype(L{} + R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);
  ASSERT_IS_DIFFERENT_TYPES(L, R);

  using ResultType = decltype(L{} + R{});

  return complex<ResultType>{lhs.real() + rhs.real(), lhs.imag() + rhs.imag()};
}

template <typename L, typename R>
auto operator-(const complex<L>& lhs, R rhs) -> complex<decltype(L{} - R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);

  using ResultType = decltype(L{} - R{});

  return complex<ResultType>{lhs.real() - rhs, lhs.imag() - rhs};
}

template <typename L, typename R>
auto operator-(L lhs, const complex<R>& rhs) -> complex<decltype(L{} - R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);

  using ResultType = decltype(L{} - R{});

  return complex<ResultType>{lhs - rhs.real(), lhs - rhs.imag()};
}

template <typename L, typename R>
auto operator-(const complex<L>& lhs, const complex<R>& rhs)
    -> complex<decltype(L{} - R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);
  ASSERT_IS_DIFFERENT_TYPES(L, R);

  using ResultType = decltype(L{} - R{});

  return complex<ResultType>{lhs.real() - rhs.real(), lhs.imag() - rhs.imag()};
}

template <typename L, typename R>
auto operator*(const complex<L>& lhs, R rhs) -> complex<decltype(L{} * R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);

  using ResultType = decltype(L{} * R{});

  return complex<ResultType>{lhs.real() * rhs, lhs.imag() * rhs};
}

template <typename L, typename R>
auto operator*(L lhs, const complex<R>& rhs) -> complex<decltype(L{} * R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);

  using ResultType = decltype(L{} * R{});

  return complex<ResultType>{lhs * rhs.real(), lhs * rhs.imag()};
}

template <typename L, typename R>
auto operator*(const complex<L>& lhs, const complex<R>& rhs)
    -> complex<decltype(L{} * R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);
  ASSERT_IS_DIFFERENT_TYPES(L, R);

  using ResultType = decltype(L{} * R{});

  // TODO: Benchmark different multiplication algorithms
  ResultType realPart = lhs.real() * rhs.real() - lhs.imag() * rhs.imag();
  ResultType imagPart = lhs.real() * rhs.imag() + lhs.imag() * rhs.real();

  return complex<ResultType>{realPart, imagPart};
}

template <typename L, typename R>
auto operator/(L lhs, const complex<R>& rhs) -> complex<decltype(L{} / R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);

  using ResultType = decltype(L{} / R{});

  return complex<ResultType>{lhs / rhs.real(), lhs / rhs.imag()};
}

template <typename L, typename R>
auto operator/(const complex<L>& lhs, R rhs) -> complex<decltype(L{} / R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);

  using ResultType = decltype(L{} / R{});

  return complex<ResultType>{lhs.real() / rhs, lhs.imag() / rhs};
}

template <typename L, typename R>
auto operator/(const complex<L>& lhs, const complex<R>& rhs)
    -> complex<decltype(L{} / R{})> {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);
  ASSERT_IS_DIFFERENT_TYPES(L, R);

  using ResultType = decltype(L{} / R{});

  // TODO: Benchmark different multiplication algorithms
  ResultType divisor = rhs.real() * rhs.real() + rhs.imag() * rhs.imag();

  ResultType realPart =
      (lhs.real() * rhs.real() - lhs.imag() * rhs.imag()) / divisor;
  ResultType imagPart =
      (lhs.real() * rhs.imag() + lhs.imag() * rhs.real()) / divisor;

  return complex<ResultType>{realPart, imagPart};
}

template <typename L, typename R>
common::bool_t operator==(const complex<L>& lhs, R rhs) {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);

  return (lhs.real() == rhs) && (lhs.imag() == rhs);
}

template <typename L, typename R>
common::bool_t operator==(L lhs, const complex<R>& rhs) {
  ASSERT_IS_ARITHMETIC_TYPE(L);
  ASSERT_IS_ARITHMETIC_TYPE(R);

  return (lhs == rhs.real()) && (lhs == rhs.imag());
}

template <typename L, typename R>
common::bool_t operator==(const complex<L>& lhs, const complex<R>& rhs) {
  ASSERT_IS_DIFFERENT_TYPES(L, R);

  return (lhs.real() == rhs.real()) && (lhs.imag() == rhs.imag());
}

template <typename L, typename R>
common::bool_t operator!=(const complex<L>& lhs, R rhs) {
  return !(lhs == rhs);
}

template <typename L, typename R>
common::bool_t operator!=(L lhs, const complex<R>& rhs) {
  return !(lhs == rhs);
}

template <typename L, typename R>
common::bool_t operator!=(const complex<L>& lhs, const complex<R>& rhs) {
  ASSERT_IS_DIFFERENT_TYPES(L, R);

  return !(lhs == rhs);
}
} // namespace std

#endif
