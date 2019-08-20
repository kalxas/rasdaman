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

using namespace std;

#include "qlparser/qtnullvaluesdata.hh"
#include <cstring>

QtNullvaluesData::QtNullvaluesData(const r_Nullvalues &nullvaluesArg)
    : QtData(), nullvalues(nullvaluesArg)
{
}


QtDataType
QtNullvaluesData::getDataType() const
{
    return QT_NULLVALUES;
}



bool
QtNullvaluesData::equal(const QtData *obj) const
{
    bool returnValue = false;
    return returnValue;
}



std::string
QtNullvaluesData::getSpelling() const
{
    std::string result;
    return result;
}



char *QtNullvaluesData::getTypeStructure() const
{
    return strdup("nullvalues");
}

void QtNullvaluesData::printStatus(std::ostream &stream) const
{

    stream << "null values: " << std::flush;
    stream << "[" << std::flush;
    bool printComma = false;
    for (const auto &p : nullvalues.getNullvalues())
    {
        if (printComma)
        {
            stream << ",";
        }
        printComma = true; // add a comma before the second entry and on

        stream << p.first << ":" << p.second;
    }
    stream << "]" << std::flush;

    QtData::printStatus(stream);
}
