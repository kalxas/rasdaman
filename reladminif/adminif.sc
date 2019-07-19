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
#include <memory>

extern char globalConnectId[PATH_MAX];

const char AdminIf::dbmsName[SYSTEMNAME_MAXLEN] = "SQLite";

/**
 * Check if a counter value matches the actual column value in the respective
 * table.
 */
void checkCounter(const char *counterName, const char *column,
                  const char *table, const char *tableDescr)
{
    SQLiteQuery query("SELECT NextValue, MAX(%s) FROM RAS_COUNTERS, %s "
                      "WHERE CounterName = '%s'", column, table, counterName);
    if (query.nextRow())
    {
        auto rasCountersOid = query.nextColumnLong();
        auto actualOid = query.nextColumnLong();

        if (actualOid > rasCountersOid)
        {
            // TODO: make a backup of RASBASE and attempt to "fix" the issue by updating the counter in RAS_COUNTERS?

            LERROR << "The administrative tables for " << tableDescr << " are inconsistent; "
                    << "counter " << column << " in table " << table << " is " << actualOid
                    << ", while counter " << counterName << " in table RAS_COUNTERS is " << rasCountersOid;
            throw r_Error(DATABASE_INCONSISTENT);
        }
    }
    else
    {
        LERROR << "Value for counter " << counterName << " not found in the RAS_COUNTERS table in RASBASE. "
                << "Most likely you need to stop rasdaman, run update_db.sh, and start rasdaman again.";
        throw r_Error(DATABASE_INCOMPATIBLE);
    }
}

AdminIf::AdminIf(bool createDb)
{
    if (!createDb)
    {
        struct stat status;
        if (stat(globalConnectId, &status) == -1)
        {
            LERROR << "Base DBMS file cannot be accessed at '" << globalConnectId << "', "
                   << "reason: " << strerror(errno);
            throw r_Error(DATABASE_NOTFOUND);
        }
    }

    SQLiteQuery::openConnection(globalConnectId);

    // cleanup: close DB connection automatically on function exit via RAII
    std::unique_ptr<SQLiteQuery, void(*)(SQLiteQuery*)> closeConnection(
        nullptr, [](SQLiteQuery*) { SQLiteQuery::closeConnection(); });

    ObjectBroker::init();

    // check database consistency
    if (!createDb)
    {
        if (!SQLiteQuery::returnsRows("SELECT name FROM sqlite_master WHERE type='table' AND name='RAS_COUNTERS'"))
        {
            LERROR << "No tables found in " << globalConnectId << ", please run create_db.sh first.";
            throw r_Error(DATABASE_NOTFOUND);
        }
        checkCounter(OId::counterNames[OId::DBMINTERVALOID], "DomainId", "RAS_DOMAINS", "domain data");
        checkCounter(OId::counterNames[OId::MDDOID], "MDDId", "RAS_MDDOBJECTS", "MDD objects");
        checkCounter(OId::counterNames[OId::MDDCOLLOID], "MDDCollId", "RAS_MDDCOLLNAMES", "MDD collections");
        checkCounter(OId::counterNames[OId::MDDTYPEOID], "MDDTypeOId", "RAS_MDDTYPES", "MDD types");
        checkCounter(OId::counterNames[OId::MDDBASETYPEOID], "MDDBaseTypeOId", "RAS_MDDBASETYPES", "MDD base types");
        checkCounter(OId::counterNames[OId::MDDDIMTYPEOID], "MDDDimTypeOId", "RAS_MDDDIMTYPES", "MDD dimension types");
        checkCounter(OId::counterNames[OId::MDDDOMTYPEOID], "MDDDomTypeOId", "RAS_MDDDOMTYPES", "MDD domain types");
        checkCounter(OId::counterNames[OId::STRUCTTYPEOID], "BaseTypeId", "RAS_BASETYPENAMES", "base types");
        checkCounter(OId::counterNames[OId::SETTYPEOID], "SetTypeId", "RAS_SETTYPES", "collection types");
        checkCounter(OId::counterNames[OId::BLOBOID], "BlobId", "RAS_TILES", "tiles");
        checkCounter(OId::counterNames[OId::MDDHIERIXOID], "MDDObjIxOId", "RAS_HIERIX", "hierarchical MDD indexes");
        checkCounter(OId::counterNames[OId::STORAGEOID], "StorageId", "RAS_STORAGE", "MDD storage structures");
        checkCounter(OId::counterNames[OId::DBNULLVALUESOID], "NullValueOId", "RAS_NULLVALUES", "null value data");
    }

    BlobFS::getInstance();
    
    validConnection = true;
}
