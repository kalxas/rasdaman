/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef COMMON_SRC_EXCEPTIONS_INVALIDSTATEEXCEPTION_HH_
#define COMMON_SRC_EXCEPTIONS_INVALIDSTATEEXCEPTION_HH_

#include "logicexception.hh"


namespace common
{

/**
 * @brief The InvalidStateException class defines a type of object to be thrown as exception.
 *  It reports errors that arise because an invariant has been violated by the programmer.
 */
class InvalidStateException : public LogicException
{
public:
    /**
     * @brief InvalidStateException Constructs the exception object with whatArg as explanatory string that can be accessed through what().
     * @param whatArg explanatory string
     */
    InvalidStateException(const std::string& whatArg);

    virtual ~InvalidStateException();
};

} /* namespace common */

#endif /* COMMON_SRC_EXCEPTIONS_INVALIDSTATEEXCEPTION_HH_ */
