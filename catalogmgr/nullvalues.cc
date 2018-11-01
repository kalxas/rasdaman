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
/**
  * INCLUDE: nullvalues.hh
 *
 * MODULE:  rasodmg
 * CLASS:   NullValuesHandler
 *
 * COMMENTS:
 *      None
*/

#include "nullvalues.hh"
#include "debug/debug-srv.hh"
#include "raslib/nullvalues.hh"
#include <logging.hh>

NullValuesHandler::NullValuesHandler()
    : nullValues(NULL), nullValuesCount(0)
{
}

NullValuesHandler::NullValuesHandler(r_Nullvalues* newNullValues)
    : nullValues(newNullValues), nullValuesCount(0)
{
}

NullValuesHandler::~NullValuesHandler() noexcept(false)
{
}

r_Nullvalues*
NullValuesHandler::getNullValues() const
{
    if (nullValues != NULL)
    {
        LDEBUG << "returning null values " << nullValues->toString();
    }
    return nullValues;
}

r_Double 
NullValuesHandler::getNullValue() const
{
    if (nullValues != NULL)
    {
        const auto& nulls = nullValues->getNullvalues();
        if (!nulls.empty())
            return nulls[0].first;
    }
    return r_Double{};
}

void
NullValuesHandler::setNullValues(r_Nullvalues* newNullValues)
{
    if (newNullValues != NULL)
    {
        LDEBUG << "setting to " << newNullValues->toString();
    }
    nullValues = newNullValues;
}

unsigned long
NullValuesHandler::getNullValuesCount() const
{
    return nullValuesCount;
}

void
NullValuesHandler::setNullValuesCount(unsigned long count)
{
    nullValuesCount = count;
}

void
NullValuesHandler::cloneNullValues(const NullValuesHandler* obj)
{
    if (this != obj)
    {
        nullValues = obj->nullValues;
        nullValuesCount = obj->nullValuesCount;
    }
}
