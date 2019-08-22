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

#include "rasql_error.hh"

RasqlError::RasqlError(int e) : error_code{e}
{
}

/// print error message (including error code)
/// NB: not all messages can occur
const char* RasqlError::what()
{
    if (!msg.empty())
        return msg.c_str();
    
    msg = "Error " + std::to_string(error_code) + ": ";
    switch (error_code)
    {
    case NOQUERY:
        msg += "Mandatory parameter '--query' missing.";
        break;
    case ERRORPARSINGCOMMANDLINE:
        msg += "Command line syntax error.";
        break;
    case ILLEGALOUTPUTTYPE:
        msg += "Illegal output type specifier, must be one of none, file, formatted, string, hex.";
        break;
    case FILEEMPTY:
        msg += "The input file is empty.";
        break;
    case FILEINACCESSIBLE:
        msg += "Cannot read input file.";
        break;
    case UNABLETOCLAIMRESOURCEFORFILE:
        msg += "Cannot allocate memory for file read.";
        break;
    case NOVALIDDOMAIN:
        msg += "Syntax error in mdddomain specification, must be [x0:x1,y0:y1] (forgot to quote or escape?)";
        break;
    case MDDTYPEINVALID:
        msg += "MDD type invalid.";
        break;
    case FILESIZEMISMATCH:
        msg += "Input file size does not correspond with MDD domain specified.";
        break;
    case NOFILEWRITEPERMISSION:
        msg += "No file write permission.";
        break;
    case UNABLETOWRITETOFILE:
        msg += "Cannot write to file.";
        break;
    case FILEREADERROR:
        msg += "Failed reading from file.";
        break;
    case FILEWRITEERROR:
        msg += "Failed writing to file.";
        break;
    case NOCONNECTION:
        msg += "Failed connecting to the database.";
        break;
    default:
        msg += "Unknown error code.";
        break;
    case ALLDONE:
    case 0:
        msg += "No errors.";
    }
    return msg.c_str();
}
