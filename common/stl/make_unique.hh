//
// Created by Dimitar Misev
// Copyright (c) 2017 rasdaman GmbH. All rights reserved.
//

#ifndef _COMMON_MAKE_UNIQUE_HH_
#define _COMMON_MAKE_UNIQUE_HH_

#include <cstddef>
#include <memory>
#include <type_traits>

namespace common {

/**
 * This is an implementation of make_unique copied straight from
 * https://stackoverflow.com/a/17902439/1499165
 *
 * make_unique is only available in C++14, so we use our own implementation
 * as long as we guarantee to comply with C++11.
 */

template<class T>
struct _Unique_if {
  typedef std::unique_ptr <T> _Single_object;
};

template<class T>
struct _Unique_if<T[]> {
  typedef std::unique_ptr<T[]> _Unknown_bound;
};

template<class T, size_t N>
struct _Unique_if<T[N]> {
  typedef void _Known_bound;
};

template<class T, class... Args>
typename _Unique_if<T>::_Single_object
make_unique(Args &&... args) {
  return std::unique_ptr<T>(new T(std::forward<Args>(args)...));
}

template<class T>
typename _Unique_if<T>::_Unknown_bound
make_unique(size_t n) {
  typedef typename std::remove_extent<T>::type U;
  return std::unique_ptr<T>(new U[n]);
}

template<class T, class... Args>
typename _Unique_if<T>::_Known_bound
make_unique(Args &&...) = delete;

}


#endif

