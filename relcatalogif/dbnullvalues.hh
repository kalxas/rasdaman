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
#ifndef _DBNULLVALUES_HH_
#define _DBNULLVALUES_HH_

class DBNullvalues;

template<class T> class DBRef;
typedef DBRef<DBNullvalues> DBNullvaluesId;

#include "reladminif/dbobject.hh"
#include "raslib/nullvalues.hh"


//@ManMemo: Module: {\bf relcatalogif}.
/*@Doc:
Persistent version of r_Nullvalues.
*/

/**
  * \ingroup Relcatalogifs
  */
class DBNullvalues : public DBObject, public r_Nullvalues
{
public:
    DBNullvalues();

    DBNullvalues(const OId &id);

    DBNullvalues(const r_Nullvalues &old);

    DBNullvalues(const DBNullvalues &old);

    ~DBNullvalues() noexcept(false);
    /*@Doc:
    validates the object in the database.
    */

    virtual DBNullvalues &operator=(const DBNullvalues &old);
    /*@Doc:
    replaces only the r_Minterval part of the object
    */

    virtual DBNullvalues &operator=(const r_Nullvalues &old);
    /*@Doc:
    replaces only the r_Minterval part of the object
    */

    virtual r_Bytes getMemorySize() const;
    /*@Doc:
    estimates the space taken up by this object
    */

protected:

    virtual void insertInDb();
    /*@Doc:
    inserts the object into the database.  it uses one table
    for the fixed length attributes (oid, size, dimension) and
    another for dynamic data (lower/upper bounds/fixed ranges)
    */

    virtual void updateInDb();
    /*@Doc:
    */

    virtual void deleteFromDb();
    /*@Doc:
    */

    virtual void readFromDb();
    /*@Doc:
    */
};

#endif
