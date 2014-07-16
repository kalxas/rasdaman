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
 * - reconsider this 604 ignorance!
 *
 ***********************************************************************/

#include "config.h"
#include "debug.hh"
#include "sqlglobals.h"
#include "sqlitewrapper.hh"

#include "transactionif.hh"
#include "raslib/rmdebug.hh"
#include "adminif.hh"
#include "oidif.hh"
#include "catalogmgr/typefactory.hh"
#include "sqlerror.hh"
#include "objectbroker.hh"
#include "databaseif.hh"
#include "dbobject.hh"

void
TransactionIf::begin( bool readOnly ) throw ( r_Error )
{
    RMDBGENTER(2, RMDebug::module_adminif, "TransactionIf", "begin(" << readOnly << ")");
    ENTER( "TransactionIf::begin, readOnly=" << readOnly );

    isReadOnly = readOnly;
    AdminIf::setAborted(false);
    AdminIf::setReadOnlyTA(readOnly);

    // if a transaction is already started, the commit it first
    if (SQLiteQuery::isTransactionActive())
    {
        SQLiteQuery::execute("COMMIT TRANSACTION");
    }

    SQLiteQuery::execute("BEGIN TRANSACTION");

#ifdef RMANBENCHMARK
    DBObject::readTimer.start();
    DBObject::readTimer.pause();

    DBObject::updateTimer.start();
    DBObject::updateTimer.pause();

    DBObject::deleteTimer.start();
    DBObject::deleteTimer.pause();

    DBObject::insertTimer.start();
    DBObject::insertTimer.pause();

    OId::oidAlloc.start();
    OId::oidAlloc.pause();

    OId::oidResolve.start();
    OId::oidResolve.pause();
#endif

    OId::initialize();
    TypeFactory::initialize();

    LEAVE( "TransactionIf::begin" );
    RMDBGEXIT(2, RMDebug::module_adminif, "TransactionIf", "begin(" << readOnly << ") ");
}

void
TransactionIf::commit() throw (  r_Error  )
{
    RMDBGENTER(2, RMDebug::module_adminif, "TransactionIf", "commit()");
    ENTER( "TransactionIf::commit" );

    if (isReadOnly)
    {
        RMDBGMIDDLE(9, RMDebug::module_adminif, "TransactionIf", "read only: aborting");
        TALK( "TA is readonly: aborting" );
        abort();
    }
    else
    {
        AdminIf::setAborted(false);
        RMDBGMIDDLE(9, RMDebug::module_adminif, "TransactionIf", "set aborted false");
        TypeFactory::freeTempTypes();
        RMDBGMIDDLE(9, RMDebug::module_adminif, "TransactionIf", "freed temp types");
        ObjectBroker::clearBroker();
        RMDBGMIDDLE(9, RMDebug::module_adminif, "TransactionIf", "cleared broker");
        OId::deinitialize();
        RMDBGMIDDLE(9, RMDebug::module_adminif, "TransactionIf", "wrote oid counters");
        AdminIf::setReadOnlyTA(false);
        RMDBGMIDDLE(9, RMDebug::module_adminif, "TransactionIf", "committing");

        SQLiteQuery::execute("COMMIT TRANSACTION");
        if (lastBase)
        {
            RMDBGMIDDLE(9, RMDebug::module_adminif, "TransactionIf", "closing dbms");
            lastBase->baseDBMSClose();
        }
    }

#ifdef RMANBENCHMARK
    DBObject::readTimer.stop();

    DBObject::updateTimer.stop();

    DBObject::deleteTimer.stop();

    DBObject::insertTimer.stop();

    OId::oidAlloc.stop();

    OId::oidResolve.stop();
#endif

    LEAVE( "TransactionIf::commit" );
    RMDBGEXIT(2, RMDebug::module_adminif, "TransactionIf", "commit() " << endl << endl);
}

void
TransactionIf::abort()
{
    RMDBGENTER(2, RMDebug::module_adminif, "TransactionIf", "abort()");
    ENTER( "TransactionIf::abort" );

    SQLiteQuery::execute("ROLLBACK TRANSACTION");

    AdminIf::setAborted(true);
    TypeFactory::freeTempTypes();
    ObjectBroker::clearBroker();
    OId::deinitialize();
    AdminIf::setReadOnlyTA(false);

    if(lastBase)
        lastBase->baseDBMSClose();

#ifdef RMANBENCHMARK
    DBObject::readTimer.stop();

    DBObject::updateTimer.stop();

    DBObject::deleteTimer.stop();

    DBObject::insertTimer.stop();

    OId::oidAlloc.stop();

    OId::oidResolve.stop();
#endif

    LEAVE( "TransactionIf::abort" );
    RMDBGEXIT(2, RMDebug::module_adminif, "TransactionIf", "abort() " << endl << endl);
}
