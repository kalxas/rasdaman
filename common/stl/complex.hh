#ifndef _COMMON_COMPLEX_HH_
#define _COMMON_COMPLEX_HH_

#ifdef ENABLE_COMPLEX

#include <complex>
#include <type_traits>

namespace common {

template<class T>
struct complex : std::complex<T> {
    complex() = default;
    
    // Allow casting from non-complex real argument; 
    // needed in physical/tile/cpu/binary.hh for example, in the case of
    // complex<int> - uint, see https://stackoverflow.com/questions/59894531/
    //
    // To remove this method for U = complex the enable_if trick is used,
    // see https://stackoverflow.com/a/17842519/1499165
    template<class U, typename std::enable_if<(
      !std::is_same<U, complex<int16_t>>{} && !std::is_same<U, complex<int32_t>>{} &&
      !std::is_same<U, complex<float>>{} && !std::is_same<U, complex<double>>{})>::type...>
    complex(U real) : complex<T>(static_cast<T>(real)) {}
    
    complex(T real) : complex<T>(real, T{}) {}
    
    template<class U>
    complex(const std::complex<U> &o) : complex<T>(static_cast<T>(o.real()),
                                                   static_cast<T>(o.imag())) {}
    complex(T real, T imag) : std::complex<T>(real, imag) {}
    
    template<class U>
    operator complex<U>() const {
        return complex{static_cast<U>(this->real()), static_cast<U>(this->imag())};
    }
};

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
  using ResultType = decltype(L{} + R{});
  return complex<ResultType>{
      static_cast<ResultType>(lhs.real()) + static_cast<ResultType>(rhs),
      static_cast<ResultType>(lhs.imag()) + static_cast<ResultType>(rhs)};
}
template <typename L, typename R>
auto operator+(L lhs, const complex<R>& rhs) -> complex<decltype(L{} + R{})> {
  using ResultType = decltype(L{} + R{});
  return complex<ResultType>{
      static_cast<ResultType>(lhs) + static_cast<ResultType>(rhs.real()),
      static_cast<ResultType>(lhs) + static_cast<ResultType>(rhs.imag())};
}
template <typename L, typename R>
auto operator+(const complex<L>& lhs, const complex<R>& rhs)
    -> complex<decltype(L{} + R{})> {
  using ResultType = decltype(L{} + R{});
  return complex<ResultType>{
      static_cast<ResultType>(lhs.real()) + static_cast<ResultType>(rhs.real()),
      static_cast<ResultType>(lhs.imag()) + static_cast<ResultType>(rhs.imag())};
}

template <typename L, typename R>
auto operator-(const complex<L>& lhs, R rhs) -> complex<decltype(L{} - R{})> {
  using ResultType = decltype(L{} - R{});
  return complex<ResultType>{
      static_cast<ResultType>(lhs.real()) - static_cast<ResultType>(rhs),
      static_cast<ResultType>(lhs.imag()) - static_cast<ResultType>(rhs)};
}
template <typename L, typename R>
auto operator-(L lhs, const complex<R>& rhs) -> complex<decltype(L{} - R{})> {
  using ResultType = decltype(L{} - R{});
  return complex<ResultType>{
      static_cast<ResultType>(lhs) - static_cast<ResultType>(rhs.real()), 
      static_cast<ResultType>(lhs) - static_cast<ResultType>(rhs.imag())};
}
template <typename L, typename R>
auto operator-(const complex<L>& lhs, const complex<R>& rhs)
    -> complex<decltype(L{} - R{})> {
  using ResultType = decltype(L{} - R{});
  return complex<ResultType>{
      static_cast<ResultType>(lhs.real()) - static_cast<ResultType>(rhs.real()), 
      static_cast<ResultType>(lhs.imag()) - static_cast<ResultType>(rhs.imag())};
}

template <typename L, typename R>
auto operator*(const complex<L>& lhs, R rhs) -> complex<decltype(L{} * R{})> {
  using ResultType = decltype(L{} * R{});
  return complex<ResultType>{
      static_cast<ResultType>(lhs.real()) * static_cast<ResultType>(rhs),
      static_cast<ResultType>(lhs.imag()) * static_cast<ResultType>(rhs)};
}
template <typename L, typename R>
auto operator*(L lhs, const complex<R>& rhs) -> complex<decltype(L{} * R{})> {
  using ResultType = decltype(L{} * R{});
  return complex<ResultType>{
      static_cast<ResultType>(lhs) * static_cast<ResultType>(rhs.real()),
      static_cast<ResultType>(lhs) * static_cast<ResultType>(rhs.imag())};
}
template <typename L, typename R>
auto operator*(const complex<L>& lhs, const complex<R>& rhs)
    -> complex<decltype(L{} * R{})> {
  using ResultType = decltype(L{} * R{});
  // TODO: Benchmark different multiplication algorithms
  ResultType realPart = lhs.real() * rhs.real() - lhs.imag() * rhs.imag();
  ResultType imagPart = lhs.real() * rhs.imag() + lhs.imag() * rhs.real();
  return complex<ResultType>{realPart, imagPart};
}

template <typename L, typename R>
auto operator/(L lhs, const complex<R>& rhs) -> complex<decltype(L{} / R{})> {
  using ResultType = decltype(L{} / R{});
  return complex<ResultType>{
      static_cast<ResultType>(lhs) / static_cast<ResultType>(rhs.real()),
      static_cast<ResultType>(lhs) / static_cast<ResultType>(rhs.imag())};
}
template <typename L, typename R>
auto operator/(const complex<L>& lhs, R rhs) -> complex<decltype(L{} / R{})> {
  using ResultType = decltype(L{} / R{});
  return complex<ResultType>{
      static_cast<ResultType>(lhs.real()) / static_cast<ResultType>(rhs),
      static_cast<ResultType>(lhs.imag()) / static_cast<ResultType>(rhs)};
}
template <typename L, typename R>
auto operator/(const complex<L>& lhs, const complex<R>& rhs)
    -> complex<decltype(L{} / R{})> {
  using ResultType = decltype(L{} / R{});
  // TODO: Benchmark different multiplication algorithms
  ResultType divisor = rhs.real() * rhs.real() + rhs.imag() * rhs.imag();
  ResultType realPart = (lhs.real() * rhs.real() - lhs.imag() * rhs.imag()) / divisor;
  ResultType imagPart = (lhs.real() * rhs.imag() + lhs.imag() * rhs.real()) / divisor;
  return complex<ResultType>{realPart, imagPart};
}

template <typename L, typename R>
bool operator==(const complex<L>& lhs, R rhs) {
  return (lhs.real() == rhs) &&
         (lhs.imag() == rhs);
}
template <typename L, typename R>
bool operator==(L lhs, const complex<R>& rhs) {
  return (lhs == rhs.real()) &&
         (lhs == rhs.imag());
}
template <typename L, typename R>
bool operator==(const complex<L>& lhs, const complex<R>& rhs) {
  return (lhs.real() == rhs.real()) &&
         (lhs.imag() == rhs.imag());
}

template <typename L, typename R>
bool operator!=(const complex<L>& lhs, R rhs) {
  return !(lhs == rhs);
}
template <typename L, typename R>
bool operator!=(L lhs, const complex<R>& rhs) {
  return !(lhs == rhs);
}
template <typename L, typename R>
bool operator!=(const complex<L>& lhs, const complex<R>& rhs) {
  return !(lhs == rhs);
}

} // namespace std

#endif

#endif

