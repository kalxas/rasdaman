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
 *   Code with embedded SQL for relational DBMS
 *
 *
 * COMMENTS:
 *   none
 *
 ***********************************************************************/

#include "config.h"
#include <string.h>
#if defined(DECALPHA) || defined(LINUX) || defined(AIX) || defined(SOLARIS)
//#include <algorith.h>
//#elif defined(LINUX) || defined(AIX) || defined(SOLARIS)
#include <algorithm>
#endif

#include "raslib/error.hh"
#include "adminif.hh"
#include "dbobject.hh"

#include "sqlerror.hh"
#include "objectbroker.hh"
#include <unistd.h>
#include <errno.h>

#include "externs.h"

#include <logging.hh>

// defined in rasserver.cc
extern char globalConnectId[256];


AdminIf *AdminIf::myInstance = NULL;

bool AdminIf::validConnection = false;

bool AdminIf::readOnlyTA = false;

DatabaseIf *AdminIf::myDatabaseIf = NULL;

char AdminIf::systemName[SYSTEMNAME_MAXLEN + 1];
const char DEFAULT_SYSTEM_NAME[SYSTEMNAME_MAXLEN] = "localhost";

bool AdminIf::_isAborted = false;

bool
AdminIf::isAborted()
{
    bool retval = false;

#ifdef READ_ONLY_RMAN
    retval = true;
#else
    retval = _isAborted;
#endif
    return retval;
}

void
AdminIf::setAborted(bool newAborted)
{
    _isAborted = newAborted;
}

DatabaseIf *
AdminIf::getCurrentDatabaseIf()
{
    return myDatabaseIf;
}

void
AdminIf::setCurrentDatabaseIf(DatabaseIf *db)
{
    myDatabaseIf = db;
}

AdminIf *
AdminIf::instance(bool createDb)
{
    AdminIf *retval = NULL;

    int hostResult = gethostname(systemName, sizeof(systemName));
    if (hostResult != 0)
    {
        LTRACE << "Error: cannot obtain hostname, using 'localhost'; errno=" << errno;
        (void) strcpy(systemName, DEFAULT_SYSTEM_NAME);
    }
    if (!myInstance)
    {
        myInstance = new AdminIf(createDb);
    }
    if (validConnection)
    {
        retval = myInstance;
    }

    return retval;
}

AdminIf::~AdminIf()
{
    myInstance = NULL;
    ObjectBroker::deinit();

#ifdef RMANBENCHMARK
    DBObject::readTimer.setOutput(0);

    DBObject::updateTimer.setOutput(0);

    DBObject::deleteTimer.setOutput(0);

    DBObject::insertTimer.setOutput(0);

    OId::oidAlloc.setOutput(0);

    OId::oidResolve.setOutput(0);
#endif
}

void
AdminIf::setReadOnlyTA(bool newReadOnlyTA)
{
    readOnlyTA = newReadOnlyTA;
}

bool
AdminIf::isReadOnlyTA()
{
    return readOnlyTA;
}

char *
AdminIf::getSystemName()
{
    return systemName;
}

