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

#include "adminif.hh"       // for AdminIf, SYSTEMNAME_MAXLEN
#include "objectbroker.hh"  // for ObjectBroker

#include <logging.hh>  // for Writer, CTRACE, LTRACE
#include <string.h>    // for strcpy
#include <errno.h>     // for errno
#include <unistd.h>    // for gethostname

AdminIf *AdminIf::myInstance = nullptr;
bool AdminIf::validConnection = false;
bool AdminIf::readOnlyTA = false;
DatabaseIf *AdminIf::myDatabaseIf = nullptr;
char AdminIf::systemName[SYSTEMNAME_MAXLEN + 1];
const char DEFAULT_SYSTEM_NAME[SYSTEMNAME_MAXLEN] = "localhost";
bool AdminIf::_isAborted = false;

bool AdminIf::isAborted()
{
    return _isAborted;
}

void AdminIf::setAborted(bool newAborted)
{
    _isAborted = newAborted;
}

DatabaseIf *AdminIf::getCurrentDatabaseIf()
{
    return myDatabaseIf;
}

void AdminIf::setCurrentDatabaseIf(DatabaseIf *db)
{
    myDatabaseIf = db;
}

AdminIf *AdminIf::instance(bool createDb)
{
    AdminIf *retval = nullptr;

    int hostResult = gethostname(systemName, sizeof(systemName));
    if (hostResult != 0)
    {
        LDEBUG << "Error: cannot obtain hostname, using 'localhost'; " << strerror(errno);
        (void)strcpy(systemName, DEFAULT_SYSTEM_NAME);
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
    myInstance = nullptr;
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

void AdminIf::setReadOnlyTA(bool newReadOnlyTA)
{
    readOnlyTA = newReadOnlyTA;
}

bool AdminIf::isReadOnlyTA()
{
    return readOnlyTA;
}

char *AdminIf::getSystemName()
{
    return systemName;
}
