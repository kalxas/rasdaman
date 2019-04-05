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
 *  Contains all code that is shared by the database interface implementations
 *
 *
 * COMMENTS:
 * - schema version depending on release doesn't make sense; rather change
 *   it when the schema _really_ changes!
 *
 ***********************************************************************/


#include "config.h"
#include <string.h>
#ifdef __APPLE__
#include <sys/malloc.h>
#else
#include <malloc.h>
#endif

#include "globals.hh"   // DEFAULT_DBNAME

#include "databaseif.hh"
#include "adminif.hh"
#include "externs.h"
#include "sqlerror.hh"
#include "raslib/error.hh"
#include "relcatalogif/alltypes.hh"

#include <logging.hh>

// defined in rasserver.cc
extern char globalConnectId[256];

const char *DatabaseIf::DefaultDatabaseName = DEFAULT_DBNAME;

DatabaseIf::~DatabaseIf()
{
    if (isConnected())
    {
        baseDBMSClose();
    }
    if (myName)
    {
        free(myName);
        myName = nullptr;
    }

    connected = false;
    opened = false;
}

bool DatabaseIf::isConnected() const
{
    return connected;
}

bool DatabaseIf::isOpen() const
{
    return opened;
}

void DatabaseIf::open(const char *dbName)
{
    if (opened)
    {
        LTRACE << "another database is already open";
        throw r_Error(r_Error::r_Error_DatabaseOpen);
    }
    // cannot do any further error checking
    if (0)   // we allow any other database name -- strcmp(dbName, DefaultDatabaseName))
    {
        LTRACE << "database name unknown";
        LERROR << "b_DatabaseIf::open(" << dbName << ") dbName=" << dbName;
        throw r_Error(r_Error::r_Error_DatabaseUnknown);
    }
    else
    {
        opened = true;
        myName = strdup(dbName);
        connect();
        connected = true;
    }
}

void DatabaseIf::baseDBMSOpen()
{
#ifdef RMANDEBUG
    if (AdminIf::getCurrentDatabaseIf())
    {
        LERROR << "There seems to be another database connection active.";
        throw r_Error(DATABASE_OPEN);
    }
#endif
    AdminIf::setCurrentDatabaseIf(this);

#ifdef DBMS_PGSQL // cannot have this check in PostgreSQL -- PB 2005-jan-09
    if (!databaseExists(myName))
    {
        LERROR << "Database " << ((myName) ? myName : "NULL") << " unknown";
        throw r_Error(r_Error::r_Error_DatabaseUnknown);
    }
#endif // DBMS_PGSQL

#ifdef BASEDB_SQLITE
// done on rasserver startup for sqlite
#define FASTCONNECT
#endif

#ifndef FASTCONNECT
    checkCompatibility();
    if (!isConsistent())
    {
        LERROR << "Database " << ((myName) ? myName : "NULL") << " inconsistent";
        throw r_Error(DATABASE_INCONSISTENT);
    }
#endif
}

void DatabaseIf::close()
{
    opened = false;
    if (myName)
    {
        free(myName);
        myName = nullptr;
    }
    if (connected)
    {
        disconnect();
        connected = false;
    }
}

void DatabaseIf::baseDBMSClose()
{
#ifdef RMANDEBUG
    if (AdminIf::getCurrentDatabaseIf() == this)
    {
#endif
        AdminIf::setCurrentDatabaseIf(nullptr);
#ifdef RMANDEBUG
    }
    else
    {
        // this happens when a transaction is killed by the server
        LTRACE << "baseDBMSClose() current DatabaseIf != this";
    }
#endif
}

DatabaseIf::DatabaseIf()
    : opened(false), myName(nullptr), connected(false)

{}

const char *DatabaseIf::getName() const
{
    return myName;
}

std::ostream &operator<<(std::ostream &stream, DatabaseIf &db)
{
    stream << "DatabaseIf" << std::endl;
    stream << "\tConnected To\t: " << ((db.getName()) ? db.getName() : " ") << std::endl;
    if (db.opened)
    {
        if (db.connected)
        {
            stream << "\tDatabase is really ONLINE" << std::endl;
        }
        else
        {
            stream << "\tDatabase is only FAKE ONLINE" << std::endl;
        }
    }
    else
    {
        stream << "\tDatabase is OFFLINE" << std::endl;
    }
    return stream;
}

