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
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann / rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#ifndef RASQL_ERROR_HH_
#define RASQL_ERROR_HH_

#include <string>

/// valid error codes:
#define ALLDONE                         -1
#define OK                              0
#define NOQUERY                         1
#define ERRORPARSINGCOMMANDLINE         2
#define ILLEGALOUTPUTTYPE               3
#define FILEINACCESSIBLE                4
#define UNABLETOCLAIMRESOURCEFORFILE    5
#define NOVALIDDOMAIN                   6
#define MDDTYPEINVALID                  7
#define FILESIZEMISMATCH                8
#define NOFILEWRITEPERMISSION           9
#define UNABLETOWRITETOFILE             10
#define FILEEMPTY                       11
#define FILEREADERROR                   12
#define FILEWRITEERROR                  13
#define NOCONNECTION					14
#define NOCONNECTSTRING                 17

class RasqlError
{
public:

    /// constructor receiving an error number
    explicit RasqlError(int e);
    
    /// get an error description
    const char* what();
    
private:
    /// error information
    int error_code;
    std::string msg;
};

#endif // _RASQL_ERROR_HH_
