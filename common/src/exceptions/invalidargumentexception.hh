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
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#ifndef COMMON_SRC_EXCEPTIONS_INVALIDARGUMENTEXCEPTION_HH
#define COMMON_SRC_EXCEPTIONS_INVALIDARGUMENTEXCEPTION_HH

#include <string>

#include "logicexception.hh"

namespace common
{

/**
 * @brief The InvalidArgumentException class defines a type of object to be thrown as exception.
 *  It reports errors that arise because an argument value has not been accepted.
 */
class InvalidArgumentException : public LogicException
{
public:
    /**
     * @brief InvalidArgumentException Constructs the exception object with whatArg as explanatory string that can be accessed through what().
     * @param whatArg explanatory string
     */
    InvalidArgumentException(const std::string& whatArg);

    virtual ~InvalidArgumentException() noexcept;
};

}
#endif // COMMON_SRC_EXCEPTIONS_INVALIDARGUMENTEXCEPTION_HH
