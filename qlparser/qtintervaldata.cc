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

#include "qlparser/qtintervaldata.hh"

#include <iostream>
#include <cstring>

using namespace std;

QtIntervalData::QtIntervalData(const r_Sinterval &interval)
    : QtData(), intervalData(interval)
{
}

QtDataType
QtIntervalData::getDataType() const
{
    return QT_INTERVAL;
}

bool QtIntervalData::equal(const QtData *obj) const
{
    int returnValue = false;  // not equal by initialization

    if (obj->getDataType() == QT_INTERVAL)
    {
        returnValue = (intervalData == (static_cast<QtIntervalData *>(const_cast<QtData *>(obj)))->getIntervalData());
    }

    return returnValue;
}

std::string
QtIntervalData::getSpelling() const
{
    std::string result;

    // buffer
    int bufferLen = 256;  // on the save side for two integers plus colon
    char *buffer = new char[bufferLen];
    // replaced deprecated ostrstream -- PB 2005-jan-14
    // ostrstream bufferStream( buffer, bufferLen );
    ostringstream bufferStream(buffer);

    bufferStream << intervalData << std::ends;

    result.append(std::string(buffer));

    delete[] buffer;
    buffer = NULL;

    return result;
}

char *QtIntervalData::getTypeStructure() const
{
    return strdup("interval");
}

void QtIntervalData::printStatus(std::ostream &stream) const
{
    stream << "interval, value: " << intervalData << std::flush;
    QtData::printStatus(stream);
}
