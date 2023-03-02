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
 * INCLUDE: persmddcolliter.hh
 *
 * MODULE:  cachetamgr
 * CLASS:   MDDCollIter
 *
 * COMMENTS:
 *     Check List:
 *       - printStatus
 *       - Functionality: stream operator, inc/dec operators,...
 *
*/
#ifndef _MDDCOLLITER_HH_
#define _MDDCOLLITER_HH_

#include "mddobj.hh"
#include "relmddif/mddid.hh"  // for DBMDDObjIdIter, DBMDDSetId

#include <stdlib.h>
#include <iosfwd>  // for cout, ostream

class MDDColl;
class MDDCollIter;

//@ManMemo: Module: {\bf cachetamgr}
/*@Doc:
    A MDDCollIter represents an iterator for a persistent MDD collection.
    If a collection is changed (elements are removed or added to/from the
    collection) between creation of an iterator for it and the execution of
    other operations on the iterator, the behavior of the istent Iterator
    is undefined.
    The <tt>MDDColl::createIterator()</tt> for the object to be scanned should
    be used to create a new iterator object.
*/

class MDDCollIter
{
public:
    void printStatus(unsigned int level, std::ostream &stream) const;

    void reset();

    bool notDone() const;

    void advance();

    MDDObj *getElement() const;

    ~MDDCollIter();

protected:
    friend class MDDColl;

    MDDCollIter(MDDColl *targetColl);
    /**
        Constructor - to be used only by MDDColl
        The iterator is reset after it is created.
    */

private:
    // Corresponding iterator in the base DBMS.
    DBMDDObjIdIter *dbIter;

    // dbColl has to be kept because of error control.
    DBMDDSetId dbColl;

    // Collection to iterate.
    MDDColl *persColl;
};

#endif
