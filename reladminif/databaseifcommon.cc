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

#include "config.h"        // for BASEDB_SQLITE
#include "globals.hh"      // DEFAULT_DBNAME
#include "adminif.hh"      // for AdminIf
#include "databaseif.hh"   // for DatabaseIf, ostream, operator<<
#include "raslib/error.hh" // for r_Error, r_Error::r_Error_DatabaseOpen

#include <logging.hh>           // for Writer, CTRACE, LTRACE, CERROR, CFATAL
#include <ostream>              // for operator<<, std::endl, ostream, basic_ostream
#include <stdlib.h>             // for free
#include <string.h>             // for strdup
#ifdef __APPLE__
#include <sys/malloc.h>
#else
#include <malloc.h>
#endif

#ifdef SPARC
#define RASARCHITECTURE "SPARC"
#elseif DECALPHA
#define RASARCHITECTURE "DECALPHA"
#elseif X86
#define RASARCHITECTURE "X86"
#elseif AIX
#define RASARCHITECTURE "AIX"
#else
#define RASARCHITECTURE "X86"
#endif

// schema version, change whenever a change is made to the relational schema -- PB 2005-oct-04
#ifndef RASSCHEMAVERSION
const int RASSCHEMAVERSION = 5; // currently still v5
#endif // RASSCHEMAVERSION

// defined in rasserver.cc
extern char globalConnectId[256];

const char *DatabaseIf::DefaultDatabaseName = DEFAULT_DBNAME;

DatabaseIf::~DatabaseIf()
{
    if (isConnected())
        baseDBMSClose();
    if (myName)
        free(myName), myName = nullptr;

    connected = false;
    opened = false;
}

void DatabaseIf::open(const char *dbName)
{
    if (opened)
    {
        LTRACE << "another database is already open";
        throw r_Error(r_Error::r_Error_DatabaseOpen);
    }
    opened = true;
    myName = strdup(dbName);
    connect();
    connected = true;
}

void DatabaseIf::close()
{
    opened = false;
    if (myName)
        free(myName), myName = nullptr;
    if (connected)
        disconnect(), connected = false;
}

bool DatabaseIf::isConnected() const
{
    return connected;
}

bool DatabaseIf::isOpen() const
{
    return opened;
}

const char *DatabaseIf::getName() const
{
    return myName;
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
#else
    checkCompatibility();
    if (!isConsistent())
    {
        LERROR << "Database " << ((myName) ? myName : "NULL") << " inconsistent";
        throw r_Error(DATABASE_INCONSISTENT);
    }
#endif
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
        LDEBUG << "current DatabaseIf != this";
    }
#endif
}

std::ostream &operator<<(std::ostream &stream, DatabaseIf &db)
{
    stream << "DatabaseIf\n"
           << "\tConnected To\t: " << ((db.getName()) ? db.getName() : "") << std::endl;
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

