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
 * Implements the adminif interface using the SQLite DBMS.
 *
 * AUTHOR:
 * Dimitar Misev <misev@rasdaman.com>
 *
 ***********************************************************************/

#include <sqlite3.h>

#include "config.h"
#include "debug-srv.hh"
#include "sqlerror.hh"
#include "sqlglobals.h"
#include "sqlitewrapper.hh"

#include "adminif.hh"
#include "raslib/rmdebug.hh"
#include "objectbroker.hh"

extern char globalConnectId[256];

const char AdminIf::dbmsName[SYSTEMNAME_MAXLEN]="SQLite";

// global connection variable
sqlite3 *sqliteConn = NULL;

/**
 * Check if a counter value matches the actual column value in the respective table.
 */
void checkCounter(const char* counterName, const char* column,
                  const char* table, const char* tableDescr, bool& retval)
{
    if (retval)
    {
        long nextoid = 0;
        long checkoid = 0;
        SQLiteQuery query("SELECT NextValue FROM RAS_COUNTERS WHERE CounterName = '%s'", counterName);
        if (query.nextRow())
        {
            nextoid = query.nextColumnLong();

            SQLiteQuery queryTable("SELECT %s FROM %s ORDER BY %s DESC LIMIT 1", column, table, column);
            if (queryTable.nextRow())
            {
                checkoid = queryTable.nextColumnLong();
                if (checkoid > nextoid)
                {
                    RMInit::logOut << "The administrative tables for " << tableDescr << " is inconsistent." << endl;
                    RMInit::logOut << "Counter " << column << " in table " << table << ": " << checkoid << endl;
                    RMInit::logOut << "Counter in table RAS_COUNTERS with name '" << counterName << "': " << nextoid << std::endl;
                    retval = false;
                }
            }
        }
        else
        {
            RMInit::logOut << "Error: value for counter '" << counterName << "' not found in the database." << endl;
            LEAVE("Error: value for counter '" << counterName << "' not found in the database.");
            throw r_Error(r_Error::r_Error_ObjectUnknown);
        }
    }
}

AdminIf::AdminIf() throw (r_Error)
{
    RMDBGENTER(4, RMDebug::module_adminif, "Adminif", "AdminIf()");
    ENTER( "AdminIf::AdminIf" );

    int error = sqlite3_open(globalConnectId, &sqliteConn);
    RMInit::logOut << "Connecting to " << globalConnectId << endl;
    if (error != SQLITE_OK)
    {
        validConnection = false;
        TALK( "connect unsuccessful; wrong connect string?" );
        cout << "Error: connect unsuccessful; wrong connect string '" << globalConnectId << "'?" << endl;
        throw r_Error( 830 );
    }
    else
    {
        validConnection = true;
        TALK( "connect ok" );
    }

    ObjectBroker::init();

    // check database consistency
    bool consistent = true;
    SQLiteQuery checkTable("SELECT name FROM sqlite_master WHERE type='table' AND name='RAS_COUNTERS'");
    if (checkTable.nextRow())
    {
        checkCounter(OId::counterNames[OId::DBMINTERVALOID], "DomainId", "RAS_DOMAINS", "domain data", consistent);
        checkCounter(OId::counterNames[OId::MDDOID], "MDDId", "RAS_MDDOBJECTS", "MDD objects", consistent);
        checkCounter(OId::counterNames[OId::MDDCOLLOID], "MDDCollId", "RAS_MDDCOLLNAMES", "MDD collections", consistent);
        checkCounter(OId::counterNames[OId::MDDTYPEOID], "MDDTypeOId", "RAS_MDDTYPES", "MDD types", consistent);
        checkCounter(OId::counterNames[OId::MDDBASETYPEOID], "MDDBaseTypeOId", "RAS_MDDBASETYPES", "MDD base types", consistent);
        checkCounter(OId::counterNames[OId::MDDDIMTYPEOID], "MDDDimTypeOId", "RAS_MDDDIMTYPES", "MDD dimension types", consistent);
        checkCounter(OId::counterNames[OId::MDDDOMTYPEOID], "MDDDomTypeOId", "RAS_MDDDOMTYPES", "MDD domain types", consistent);
        checkCounter(OId::counterNames[OId::STRUCTTYPEOID], "BaseTypeId", "RAS_BASETYPENAMES", "base types", consistent);
        checkCounter(OId::counterNames[OId::SETTYPEOID], "SetTypeId", "RAS_SETTYPES", "collection types", consistent);
        checkCounter(OId::counterNames[OId::BLOBOID], "BlobId", "RAS_TILES", "tiles", consistent);
        checkCounter(OId::counterNames[OId::MDDHIERIXOID], "MDDObjIxOId", "RAS_HIERIX", "hierarchical MDD indexes", consistent);
        checkCounter(OId::counterNames[OId::STORAGEOID], "StorageId", "RAS_STORAGE", "MDD storage structures", consistent);
    }
    if (!consistent)
    {
        RMInit::logOut << "Database inconsistent." << std::endl;
        throw r_Error(DATABASE_INCONSISTENT);
    }

    LEAVE( "AdminIf::AdminIf" );
    RMDBGEXIT(4, RMDebug::module_adminif, "Adminif", "AdminIf() " << validConnection);
}
