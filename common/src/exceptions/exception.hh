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

#ifndef COMMON_SRC_EXCEPTIONS_EXCEPTION_HH
#define COMMON_SRC_EXCEPTIONS_EXCEPTION_HH

#include <stdexcept>

namespace common
{
/**
 * @brief The Exception class provides a consistent interface to handle errors in rasdaman.
 * Exception inherits all its memebers from std::exception.
 * All exceptions defined in rasdaman should inherit from Exception.
 */
class Exception : public std::exception
{
public:
    /**
     * @brief Exception Constructs the exception with an empty explanatory message.
     */
    Exception();

    /**
     * @brief Exception Constructs the exception with an explanatory message given by whatArg.
     * @param whatArg Explanatory message that can be retrieved through the what() method.
     */
    Exception(const std::string& whatArg);

    virtual ~Exception();
};
}

#endif // COMMON_SRC_EXCEPTIONS_EXCEPTION_HH
