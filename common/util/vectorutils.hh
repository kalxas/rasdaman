/*
* This file is part of rasdaman community.
*
* Rasdaman community is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Rasdaman community is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
#ifndef _COMMON_VECTORUTILS_HH_
#define _COMMON_VECTORUTILS_HH_

#include <boost/algorithm/string.hpp>
#include <boost/lexical_cast.hpp>
#include <string>
#include <vector>
#include <algorithm>

namespace common {
class VectorUtils final {
 public:
  template<typename T>
  static std::string join(const std::vector<T> &list,
                          const std::string &separator) {
    std::vector<std::string> strList(list.size());
    std::transform(std::begin(list), std::end(list), std::begin(strList),
                   [](T d) { return boost::lexical_cast<std::string>(d); });

    std::string joinedString = boost::algorithm::join(strList, separator);
    return joinedString;
  }

  /**
   * Appends the elements of src to dst by copying them.
   */
  template<typename T>
  static void append(const std::vector<T> &src, std::vector<T> &dst) {
    if (dst.empty()) {
      dst = src;
    } else {
      dst.reserve(src.size() + dst.size());
      for (const auto &el: src) {
        dst.push_back(el);
      }
    }
  }

  /**
   * Appends the elements of src to dst by moving them, i.e. src is empty
   * afterwards.
   */
  template<typename T>
  static void append(std::vector<T> &&src, std::vector<T> &dst) {
    if (dst.empty()) {
      dst = std::move(src);
    } else {
      dst.reserve(src.size() + dst.size());
      for (auto &el: src) {
        dst.push_back(std::move(el));
      }
    }
  }

 private:
  VectorUtils() = delete;
};
}

#endif
