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
/*************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#include <iostream>

#include "qlparser/parseinfo.hh"
#include "raslib/error.hh"

using namespace std;

ParseInfo::ParseInfo()
    : errorNo(0),
      lineNo(0),
      columnNo(0),
      token("")
{
}

ParseInfo::ParseInfo(const ParseInfo &old)
    : errorNo(0),
      lineNo(0),
      columnNo(0),
      token("")
{
    errorNo = old.errorNo;
    lineNo = old.lineNo;
    columnNo = old.columnNo;
    token = old.token;
}

ParseInfo::ParseInfo(const char *initToken, unsigned int initLineNo, unsigned initColumnNo)
    : errorNo(0),
      lineNo(initLineNo),
      columnNo(initColumnNo),
      token("")
{
    if (initToken)
    {
        token = initToken;
    }
}

ParseInfo::ParseInfo(unsigned long initErrorNo, const char *initToken, unsigned int initLineNo, unsigned initColumnNo)
    : errorNo(initErrorNo),
      lineNo(initLineNo),
      columnNo(initColumnNo),
      token("")
{
    if (initToken)
    {
        token = initToken;
    }
}

ParseInfo &
ParseInfo::operator=(const ParseInfo &old)
{
    if (this != &old)
    {
        errorNo = old.errorNo;
        lineNo = old.lineNo;
        columnNo = old.columnNo;
        token = old.token;
    }
    return *this;
}

void ParseInfo::printStatus(ostream &s) const
{
    r_Equery_execution_failed e(errorNo,
                                lineNo, columnNo, token.c_str());
    s << "rasdaman error " << e.get_errorno() << ": " << e.what() << endl;
}
