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

#ifndef COMMON_SRC_EXCEPTIONS_RESOURCEBUSYEXCEPTION_HH
#define COMMON_SRC_EXCEPTIONS_RESOURCEBUSYEXCEPTION_HH

#include "runtimeexception.hh"

namespace common
{
/**
 * @brief The ResourceBusyException class defines a type of object to be thrown
 * as exception.
 *  It reports errors that arise because an operation on an object is attempted
 * but it cannot be completed
 * because the object is being used.
 */
class ResourceBusyException : public RuntimeException
{
public:
    /**
   * @brief ResourceBusyException
   * @param message Information detailing the cause of the exception.
   */
    ResourceBusyException(const std::string &message);

    ~ResourceBusyException() noexcept override;
};
}  // namespace common
#endif  // COMMON_SRC_EXCEPTIONS_RESOURCEBUSYEXCEPTION_HH
