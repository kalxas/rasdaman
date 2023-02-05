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
// This is -*- C++ -*-
/*************************************************************************
 *
 *
 * PURPOSE:
 * Code with embedded SQL for SQLite
 *
 * AUTHOR:
 * Dimitar Misev <misev@rasdaman.com>
 *
 ***********************************************************************/

#include "version.h"

#include "adminif.hh"
#include "databaseif.hh"
#include "oidif.hh"
#include "sqlglobals.h"
#include "sqlerror.hh"
#include "sqlitewrapper.hh"
#include "raslib/error.hh"

#include <logging.hh>
#include <fmt/core.h>
#include <sqlite3.h>
#include <climits>
#include <cstdlib>
#include <cstring>
#include <iostream>

using namespace std;

extern char globalConnectId[PATH_MAX];

#define UPDATE_QUERY(c) { \
    sqlite3_exec(SQLiteQuery::getConnection(), c, 0, 0, 0); \
    SQLiteQuery::failOnError(c); }
#define DROP_TABLE(table_name) \
    UPDATE_QUERY("DROP TABLE IF EXISTS " #table_name);
#define DROP_VIEW(view_name) \
    UPDATE_QUERY("DROP VIEW IF EXISTS "#view_name);

// size of ARCHITECTURE attribute in RAS_ADMIN:
#define SIZE_ARCH_RASADMIN 20

void DatabaseIf::connect()
{
    SQLiteQuery::openConnection(globalConnectId);
}

void DatabaseIf::disconnect()
{
    SQLiteQuery::closeConnection();
}

void DatabaseIf::checkCompatibility() {}

bool DatabaseIf::isConsistent()
{
    // done once at rasserver startup, in AdminIf
    return true;
}

void DatabaseIf::createDB(const char *, const char *, const char *)
{
    try
    {
        if (AdminIf::getCurrentDatabaseIf() != 0)
        {
            LERROR << "Another database is open already.";
            throw r_Error(r_Error::r_Error_DatabaseOpen);
        }

        connect();

        if (SQLiteQuery::returnsRows("SELECT name FROM sqlite_master WHERE type='table' AND name='RAS_COUNTERS'"))
        {
            LERROR << "Database exists already.";
            disconnect();
            throw r_Error(DATABASE_EXISTS_ALREADY);
        }

        // --- start table/index creation ------------------------------

        // no index here because there is only one entry in the table
        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_ADMIN ( "
                     "IFVersion INTEGER NOT NULL, "
                     "Architecture VARCHAR(20) NOT NULL, "
                     "ServerVersion INTEGER NOT NULL)");

        SQLiteQuery::execute(fmt::format(
            "INSERT INTO RAS_ADMIN (IFVersion, Architecture, ServerVersion) VALUES ({}, '{}', {})",
            RASSCHEMAVERSION, RASARCHITECTURE, DatabaseIf::rmanverToLong()));

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_COUNTERS ("
                     "CounterId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "NextValue INTEGER NOT NULL,"
                     "CounterName VARCHAR(20) NOT NULL);")
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_COUNTERS_IX "
                     "ON RAS_COUNTERS ( countername );")

        // initialising RAS_COUNTERS
        for (unsigned int i = 1; i < OId::maxCounter; i++)
        {
            SQLiteQuery::execute(fmt::format(
                "INSERT INTO RAS_COUNTERS (CounterName, NextValue) VALUES ('{}', 1)",
                OId::counterNames[i]));
        }

        // relblobif
        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_TILES ("
                     "BlobId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "DataFormat INTEGER)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS "
                     "RAS_TILES_IX ON RAS_TILES (BlobId)");

        //ras_itiles
        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_ITILES ("
                     "ITileId INTEGER PRIMARY KEY AUTOINCREMENT)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_ITILES_IX "
                     "ON RAS_ITILES (ITileId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_ITMAP ("
                     "TileId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "IndexId INTEGER NOT NULL)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_ITMAP_IX "
                     "ON RAS_ITMAP (TileId)");

        // relcatalogif
        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_MDDTYPES ("
                     "MDDTypeOId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "MDDTypeName VARCHAR(254) NOT NULL)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_MDDTYPES_IX "
                     "ON RAS_MDDTYPES (MDDTypeOId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_MDDBASETYPES ("
                     "MDDBaseTypeOId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "BaseTypeId INTEGER NOT NULL REFERENCES RAS_BASETYPES,"
                     "MDDTypeName VARCHAR(254) NOT NULL)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_MDDBASETYPES_IX "
                     "ON RAS_MDDBASETYPES (MDDBaseTypeOId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_MDDDIMTYPES ("
                     "MDDDimTypeOId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "BaseTypeId INTEGER NOT NULL REFERENCES RAS_BASETYPES,"
                     "Dimension INTEGER NOT NULL,"
                     "MDDTypeName VARCHAR(254) NOT NULL)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_MDDDIMTYPES_IX "
                     "ON RAS_MDDDIMTYPES (MDDDimTypeOId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_MDDDOMTYPES ("
                     "MDDDomTypeOId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "BaseTypeId INTEGER NOT NULL REFERENCES RAS_BASETYPES,"
                     "DomainId INTEGER NOT NULL REFERENCES RAS_DOMAINS,"
                     "MDDTypeName VARCHAR(254) NOT NULL)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_MDDDOMTYPES_IX "
                     "ON RAS_MDDDOMTYPES (MDDDomTypeOId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_SETTYPES ("
                     "SetTypeId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "MDDTypeOId INTEGER NOT NULL,"
                     "SetTypeName VARCHAR(254) NOT NULL)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_SETTYPES_IX "
                     "ON RAS_SETTYPES (SetTypeId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_BASETYPENAMES ("
                     "BaseTypeId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "BaseTypeName VARCHAR (254) NOT NULL)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_BASETYPENAMES_IX "
                     "ON RAS_BASETYPENAMES (BaseTypeId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_LOCKEDTILES ("
                     "TileID bigint NOT NULL,"
                     "RasServerID varchar(40) NOT NULL,"
                     "SharedLock integer NOT NULL,"
                     "ExclusiveLock integer,"
                     "UNIQUE(TileID, ExclusiveLock))");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_BASETYPES ("
                     "BaseTypeId INTEGER NOT NULL,"
                     "Count SMALLINT NOT NULL,"
                     "ContentType INTEGER NOT NULL,"
                     "ContentTypeName VARCHAR (254) NOT NULL)");
        UPDATE_QUERY("CREATE INDEX IF NOT EXISTS RAS_BASETYPESC_IX "
                     "ON RAS_BASETYPES (BaseTypeId)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_BASETYPES_IX "
                     "ON RAS_BASETYPES (BaseTypeId, Count)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_PYRAMIDS ("
                     "PyramidName VARCHAR(240) NOT NULL,"
                     "CollectionName VARCHAR(240) NOT NULL,"
                     "MDDOId VARCHAR(240) NOT NULL,"
                     "ScaleFactor DEC NOT NULL)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_DOMAINS ("
                     "DomainId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "Dimension INTEGER NOT NULL)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_DOMAINS_IX "
                     "ON RAS_DOMAINS (DomainId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_DOMAINVALUES ("
                     "DomainId INTEGER NOT NULL,"
                     "DimensionCount INTEGER NOT NULL,"
                     "Low INTEGER,"
                     "High INTEGER,"
                     "AxisName TEXT)");
        UPDATE_QUERY("CREATE INDEX IF NOT EXISTS RAS_DOMAINVALUESC_IX "
                     "ON RAS_DOMAINVALUES (DomainId)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_DOMAINVALUES_IX "
                     "ON RAS_DOMAINVALUES (DomainId, DimensionCount)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_NULLVALUES ("
                     "SetTypeOId INTEGER NOT NULL REFERENCES RAS_SETTYPES (SetTypeId),"
                     "NullValueOId INTEGER PRIMARY KEY AUTOINCREMENT)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_NULLVALUES_IX "
                     "ON RAS_NULLVALUES (SetTypeOId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_NULLVALUEPAIRS ("
                     "NullValueOId INTEGER NOT NULL REFERENCES RAS_NULLVALUES,"
                     "Count INTEGER NOT NULL,"
                     "Low REAL," // NULL = nan
                     "High REAL)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_NULLVALUEPAIRS_IX "
                     "ON RAS_NULLVALUEPAIRS (NullValueOId, Count)");

        // relmddif
        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_MDDCOLLECTIONS ("
                     "MDDId INTEGER NOT NULL REFERENCES RAS_MDDOBJECTS,"
                     "MDDCollId INTEGER)");
        UPDATE_QUERY("CREATE INDEX IF NOT EXISTS RAS_COLLECTIONSC_IX "
                     "ON RAS_MDDCOLLECTIONS (MDDCOllId)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_COLLECTIONS_IX "
                     "ON RAS_MDDCOLLECTIONS (MDDCOllId, MDDId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_MDDCOLLNAMES ("
                     "MDDCollId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "SetTypeId INTEGER NOT NULL REFERENCES RAS_SETTYPES,"
                     "MDDCollName VARCHAR(254))");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_MDDCOLLNAMES_IX "
                     "ON RAS_MDDCOLLNAMES (MDDCollId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_MDDOBJECTS ("
                     "MDDId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "BaseTypeOId INTEGER NOT NULL,"
                     "DomainId INTEGER NOT NULL,"
                     "PersRefCount INTEGER NOT NULL,"
                     "StorageOId INTEGER NOT NULL,"
                     "NodeOId INTEGER)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_MDDOBJECTS_IX "
                     "ON RAS_MDDOBJECTS (MDDId)");

        //relstorageif
        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_STORAGE ("
                     "StorageId   INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "DomainId    INTEGER,"
                     "TileSize    INTEGER,"
                     "PCTMin      INTEGER,"
                     "PCTMax      INTEGER,"
                     "IndexSize   INTEGER,"
                     "IndexType   SMALLINT,"
                     "TilingScheme    SMALLINT,"
                     "DataFormat  SMALLINT)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_STORAGE_IX "
                     "ON RAS_STORAGE (StorageId)");

        // relindexif
        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_HIERIX ("
                     "MDDObjIxOId INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "NumEntries  INTEGER NOT NULL,"
                     "Dimension   INTEGER NOT NULL,"
                     "ParentOId   INTEGER NOT NULL,"
                     "IndexSubType SMALLINT NOT NULL,"
                     "DynData     BLOB)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_HIERIX_IX "
                     "ON RAS_HIERIX (MDDObjIxOId)");

        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_RCINDEXDYN ("
                     "Id      INTEGER PRIMARY KEY AUTOINCREMENT,"
                     "Count   INTEGER NOT NULL,"
                     "DynData BLOB)");
        UPDATE_QUERY("CREATE INDEX IF NOT EXISTS RAS_RCINDEXDYNC_IX "
                     "ON RAS_RCINDEXDYN (Id)");
        UPDATE_QUERY("CREATE UNIQUE INDEX IF NOT EXISTS RAS_RCINDEXDYN_IX "
                     "ON RAS_RCINDEXDYN (Id, Count)");

        UPDATE_QUERY("CREATE VIEW IF NOT EXISTS RAS_MDDTYPES_VIEW "
                     "AS"
                     "  SELECT"
                     "        MDDTypeOId * 512 + 3 AS MDDTypeOId, MDDTypeName"
                     "    FROM"
                     "        RAS_MDDTYPES "
                     "UNION"
                     "  SELECT"
                     "        MDDBaseTypeOId * 512 + 4, MDDTypeName"
                     "    FROM"
                     "        RAS_MDDBASETYPES "
                     "UNION"
                     "  SELECT"
                     "        MDDDimTypeOId * 512 + 5, MDDTypeName"
                     "    FROM"
                     "        RAS_MDDDIMTYPES "
                     "UNION"
                     "  SELECT"
                     "        MDDDomTypeOId * 512 + 6, MDDTypeName"
                     "    FROM"
                     "        RAS_MDDDOMTYPES");

        // database updates
        UPDATE_QUERY("CREATE TABLE IF NOT EXISTS RAS_DBUPDATES ("
                     "UpdateType VARCHAR(5) PRIMARY KEY,"
                     "UpdateNumber INTEGER)");
        UPDATE_QUERY("INSERT INTO RAS_DBUPDATES values ('rc', 6)");

        disconnect();
    }
    catch (r_Error &err)
    {
        disconnect();
        throw; // rethrow exception
    }
}

void DatabaseIf::destroyDB(const char *dbName)
{
    if (AdminIf::getCurrentDatabaseIf() != 0)
    {
        LERROR << "Another database is already open, cannot destroy database " << dbName << ".";
        throw r_Error(r_Error::r_Error_DatabaseOpen);
    }
    connect();

    try
    {
        DROP_VIEW("RAS_MDDTYPES_VIEW");
        DROP_TABLE("RAS_TILES");
        DROP_TABLE("RAS_ITILES");
        DROP_TABLE("RAS_MDDTYPES");
        DROP_TABLE("RAS_ITMAP");
        DROP_TABLE("RAS_MDDBASETYPES");
        DROP_TABLE("RAS_MDDDIMTYPES");
        DROP_TABLE("RAS_MDDDOMTYPES");
        DROP_TABLE("RAS_SETTYPES");
        DROP_TABLE("RAS_BASETYPENAMES");
        DROP_TABLE("RAS_BASETYPES");
        DROP_TABLE("RAS_DOMAINS");
        DROP_TABLE("RAS_DOMAINVALUES");
        DROP_TABLE("RAS_MDDCOLLECTIONS");
        DROP_TABLE("RAS_MDDCOLLNAMES");
        DROP_TABLE("RAS_MDDOBJECTS");
        DROP_TABLE("RAS_HIERIX");
        DROP_TABLE("RAS_HIERIXDYN");
        DROP_TABLE("RAS_STORAGE");
        DROP_TABLE("RAS_COUNTERS");
        DROP_TABLE("RAS_PYRAMIDS");
        DROP_TABLE("RAS_LOCKEDTILES");
        DROP_TABLE("RAS_RCINDEXDYN");
        DROP_TABLE("RAS_ADMIN");
        DROP_TABLE("RAS_DBUPDATES");
        DROP_TABLE("RAS_NULLVALUES");
        DROP_TABLE("RAS_NULLVALUEPAIRS");
        
        disconnect();
    }
    catch (r_Error &err)
    {
        disconnect();
        throw; // rethrow exception
    }
}

#ifndef RMANVERSION
#define RMANVERSION "v10.2.0-unknown"
#endif
#define LONGVER 10200

long DatabaseIf::rmanverToLong()
{
    string s(RMANVERSION);
    // return default version if RMANVERSION length is 0
    if (s.empty())
    {
        return LONGVER;
    }
    string versionstr;
    string final;
    long longver = 0;
    size_t first = s.find_first_of("0123456789;");
    // return default version if no number found in s
    if (first == string::npos)
    {
        return LONGVER;
    }
    size_t last = s.find_first_of("-;");
    versionstr = s.substr(first, last - first);

    // split the versionstr using . as delimiter
    string delim = ".";
    size_t start = 0;
    size_t end = versionstr.find(delim);
    int mulfactor = 1000;
    double verpart;
    while (end != string::npos)
    {
        // convert the split part to double
        verpart = atol(versionstr.substr(start, end - start).c_str());
        longver += verpart * mulfactor;
        mulfactor /= 10;
        start = end + delim.length();
        end = versionstr.find(delim, start);
    }
    verpart = atol(versionstr.substr(start, versionstr.length()).c_str());
    longver += verpart * mulfactor;
    return longver;
}
