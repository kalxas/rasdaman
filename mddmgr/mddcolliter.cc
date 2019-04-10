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
/**
 * SOURCE: persmddcolliter.cc
 *
 * MODULE: cachetamgr
 * CLASS:   MDDCollIter
 *
 * COMMENTS:
 *  none
 *
*/


#include "mddcolliter.hh"
#include "mddcoll.hh"            // for MDDColl
#include "relmddif/dbmddset.hh"  // for DBMDDSet
#include "relmddif/dbmddobj.hh"  // for DBMDDObj
#include "relmddif/mddid.hh"
#include "reladminif/dbref.hh"   // for DBRef
#include "reladminif/dbobjectiditerator.hh"
#include "logging.hh"          // for LTRACE

#include <iostream>            // for operator<<, ostream

MDDCollIter::MDDCollIter(MDDColl *targetColl)
    : dbIter(nullptr), persColl(targetColl)
{
    LTRACE << "MDDCollIter(" << targetColl->getName() << ")";
    dbColl = targetColl->getDBMDDSet();
    dbIter = dbColl->newIterator();
}

void MDDCollIter::printStatus(__attribute__((unused)) unsigned int level,
                              std::ostream &stream) const
{
    stream << "   MDDCollIter printStatus:  ";
}

MDDObj *MDDCollIter::getElement() const
{
    // Initialization to null: make sure null pointer is returned if the
    // collection is empty or if the iterator has come to the end already

    MDDObj *persEl = nullptr;

    if (dbIter->not_done())
    {
        DBMDDObjId el = dbIter->get_element();

        if (!el.is_null())
        {
            persEl = persColl->getMDDObj(static_cast<DBMDDObj *>(el));
        }
    }

    // persEl is null if there is nothing to return
    return persEl;
}

void MDDCollIter::reset()
{
    LTRACE << "reset()";
    dbIter->reset();
}

bool MDDCollIter::notDone() const
{
    LTRACE << "notDone()";
    return dbIter->not_done();
}

void MDDCollIter::advance()
{
    LTRACE << "advance()";
    dbIter->advance();
}

MDDCollIter::~MDDCollIter()
{
    LTRACE << "~MDDCollIter()";
    delete dbIter;
    dbIter = nullptr;
}

