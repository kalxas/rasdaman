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
/*************************************************************************
 *
 *
 * PURPOSE:
 *   Code common to all DBMS interface implementations
 */

#include "transactionif.hh"  // for TransactionIf
#include "databaseif.hh"     // for DatabaseIf
#include "raslib/error.hh"   // for r_Error

DatabaseIf *TransactionIf::lastBase = nullptr;

DatabaseIf *TransactionIf::getDatabaseIf()
{
    return lastBase;
}

void TransactionIf::begin(DatabaseIf *currBase, bool readOnly)
{
    try
    {
        currBase->baseDBMSOpen();
    }
    catch (r_Error &err)
    {
        currBase->baseDBMSClose();
        throw;
    }
    lastBase = currBase;
    begin(readOnly);
}
