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

#ifndef COMMON_EXCEPTION_HH
#define COMMON_EXCEPTION_HH

#include <string>  // for string

namespace common {
/**
 * @brief The Exception class provides a consistent interface to handle errors
 * in rasdaman.
 * All exceptions defined in rasdaman should inherit from Exception.
 */
class Exception {
public:
    /**
     * @brief Exception Constructs an instance of the Exception class with no
     * associated information.
     */
    Exception() = default;

    /**
     * @brief Exception Constructs the exception with an explanatory message given by whatArg.
     * @param whatArg Explanatory message that can be retrieved through the what() method.
     */
    Exception(const std::string& whatArg);

    virtual ~Exception() noexcept = default;

    /**
   * @return exception information
     */
    virtual const std::string &what() const;

 private:
  std::string msg;
};
} // namespace common

#endif
