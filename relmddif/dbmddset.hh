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
#ifndef _MDDSET_HH_
#define _MDDSET_HH_

#include "mddid.hh"
#include "reladminif/lists.h"
#include "reladminif/dbnamedobject.hh"
#include "reladminif/dbref.hh"

class r_Error;
class DBMDDObj;
class CollectionType;
class MDDSet;
class OId;
class EOId;

//@ManMemo: Module: relmddif
/**
 * MDDSet is the persistent class for collections of MDD objects.
 *
 * Each instance of MDDSet represents a collection stored in the base DBMS.
 *
 * This class should only be used by DBMDDColl.
*/
/**
  * \ingroup Relmddifs
  */
class DBMDDSet : public DBNamedObject
{
public:
    DBMDDSet(const char *name, const CollectionType *type);

    DBMDDSet(const char *name, const OId &id, const CollectionType *type);

    ~DBMDDSet() noexcept(false) override;

    static DBMDDSetId getDBMDDSet(const char *name);

    static DBMDDSetId getDBMDDSet(const OId &id);

    static DBMDDSetId getDBMDDSetContainingDBMDDObj(const OId &id);

    static bool deleteDBMDDSet(const OId &id);

    static bool deleteDBMDDSet(const char *name);

    void printStatus(unsigned int level, std::ostream &stream) const override;

    void setPersistent(bool state) override;
    /*@Doc:
        throws r_Error when the mdd set may not be made persistent.
    */

    void insert(DBMDDObjId newObj);
    /*@Doc:
        Inserts an object into the MDD Collection.
        The persistent reference count of this DBMDDObj is updated.
    */

    DBMDDObjIdIter *newIterator() const;
    /*@Doc:
        Returns a new iterator for this collection.
    */

    unsigned int getCardinality() const;
    /*@Doc:
        Returns the number of elements in the collection.
    */

    void remove(DBMDDObjId &obj);
    /*@Doc:
        Removes an object from the MDD Collection.
        The persistent reference count of this DBMDDObj is updated.
    */

    void removeAll();
    /*@Doc:
        Removes all objects from the MDD Collection.
        The persistent reference count of the DBMDDObjs is updated.
    */

    void releaseAll();
    /*@Doc:
        Releases all dynamic memory used by this collection.
    */

    bool contains_element(const DBMDDObjId &elem) const;

    void deleteName();
    /*@Doc:
        sets the name of this object to a null string.
        used by DatabaseIf::destroyRoot
    */

    r_Bytes getMemorySize() const override;

    const CollectionType *getCollType() const;

    void setCollType(const CollectionType *collTypeArg);

protected:
    friend class ObjectBroker;

    using DBMDDObjIdSet = std::set<DBMDDObjId, std::less<DBMDDObjId>>;

    DBMDDSet(const OId &id);
    /*@Doc:
        gets an existing coll from the db
    */

    void updateInDb() override;
    void insertInDb() override;
    void readFromDb() override;
    void deleteFromDb() override;

private:
    DBMDDObjIdSet mySet;
    /*@Doc:
        Memory representation of the list of oids of DBMDDObjs.
    */

    const CollectionType *collType{NULL};
};

#endif
