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

#include "config.h"
#include "adminif.hh"
#include "sqlerror.hh"
#include "sqlglobals.h"
#include "sqlitewrapper.hh"
#include "objectbroker.hh"
#include "relblobif/blobfs.hh"
#include <logging.hh>

#include <sqlite3.h>
#include <climits>

extern char globalConnectId[PATH_MAX];

const char AdminIf::dbmsName[SYSTEMNAME_MAXLEN] = "SQLite";

/**
 * Check if a counter value matches the actual column value in the respective
 * table.
 */
void checkCounter(const char *counterName, const char *column,
                  const char *table, const char *tableDescr, bool &retval)
{
    if (retval)
    {
        SQLiteQuery query("SELECT NextValue FROM RAS_COUNTERS WHERE CounterName = '%s'", counterName);
        if (query.nextRow())
        {
            auto nextoid = query.nextColumnLong();

            SQLiteQuery queryTable("SELECT %s FROM %s ORDER BY %s DESC LIMIT 1", column, table, column);
            if (queryTable.nextRow())
            {
                auto checkoid = queryTable.nextColumnLong();
                if (checkoid > nextoid)
                {
                    LWARNING << "The administrative tables for " << tableDescr << " are inconsistent.";
                    LWARNING << "Counter " << column << " in table " << table << ": " << checkoid;
                    LWARNING << "Counter in table RAS_COUNTERS with name '" << counterName << "': " << nextoid;
                    retval = false;
                }
            }
        }
        else
        {
            LERROR << "Value for counter '" << counterName << "' not found in the RAS_COUNTERS table in RASBASE. "
                   << "Most likely you need to run update_db.sh to update the database schema.";
            closeDbConnection();
            throw r_Error(DATABASE_INCOMPATIBLE);
        }
    }
}

void closeDbConnection()
{
    SQLiteQuery::closeConnection();
}

AdminIf::AdminIf(bool createDb)
{
    if (!createDb)
    {
        struct stat status;
        if (stat(globalConnectId, &status) == -1)
        {
            LERROR << "Base DBMS file not found at '" << globalConnectId << "', please run create_db.sh first.";
            throw r_Error(DATABASE_NOTFOUND);
        }
    }

    validConnection = SQLiteQuery::openConnection(globalConnectId);

    ObjectBroker::init();

    // check database consistency
    if (!createDb && SQLiteQuery::isConnected())
    {
        if (!SQLiteQuery::returnsRows("SELECT name FROM sqlite_master WHERE type='table' AND name='RAS_COUNTERS'"))
        {
            LERROR << "No tables found in " << globalConnectId << ", please run create_db.sh first.";
            closeDbConnection();
            throw r_Error(DATABASE_NOTFOUND);
        }

        bool consistent = true;
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

        if (SQLiteQuery::returnsRows("SELECT name FROM sqlite_master WHERE type='table' AND name='RAS_NULLVALUEPAIRS'"))
        {
            checkCounter(OId::counterNames[OId::DBNULLVALUESOID], "NullValueOId", "RAS_NULLVALUES", "null value data", consistent);
        }
        else
        {
            LERROR << "Database schema out of date. Please stop rasdaman, run update_db.sh, and start rasdaman again.";
            closeDbConnection();
            throw r_Error(DATABASE_INCOMPATIBLE);
        }

        if (!consistent)
        {
            LERROR << "Database OId counters inconsistent.";
            closeDbConnection();
            throw r_Error(DATABASE_INCONSISTENT);
        }
    }

    BlobFS::getInstance();

    closeDbConnection();
}
