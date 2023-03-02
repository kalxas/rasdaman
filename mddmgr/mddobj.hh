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
 * INCLUDE: mddobj.hh
 *
 * MODULE:  cachetamgr
 * CLASS:   MDDObj
 *
 * COMMENTS:
 *
*/

#ifndef _MDDOBJ_HH_
#define _MDDOBJ_HH_

#include "reladminif/oidif.hh"  // for OId
#include "reladminif/dbref.hh"
#include "relmddif/mddid.hh"    // for DBMDDObjId
#include "raslib/mddtypes.hh"   // for r_Dimension
#include "raslib/minterval.hh"  // for r_Minterval
#include "raslib/point.hh"      // for r_Point
#include "catalogmgr/nullvalues.hh"
#include "catalogmgr/typeenum.hh"
#include "common/tree/model/dbinfo.hh"

#include <vector>  // for vector
#include <iosfwd>  // for cout, ostream
#include <string>  // for string
#include <memory>  // for string

class Tile;
class BaseType;
class MDDBaseType;
class MDDObjIx;
class StorageLayout;
class EOId;
class CollectionType;

//@ManMemo: Module: {\bf cachetamgr}
/*@Doc:

An MDDObj object (Multidimensional Discrete Data Object) is a multidimensional
array of cells of a fixed base type.
Each MDDObj object keeps information about its cell base type, definition
domain, the storage layout and the index.
Actual data is stored in tiles which are linked to the MDDObj via the index.

When the object is first created, a spatial {\bf definition domain} for the
object is given, which specifies the extents
of the object array. This is expressed through an interval which may have open
bounds along some (or all) directions.
An open bound along a direction specifies that the object may grow arbitrarily
along this direction.

At each point in time, an MDDObj has a fixed {\bf current domain} which
specifies the actual extent of the object at the
moment.
The current domain is an interval with fixed bounds corresponding to the
coverage of all the tiles already inserted in
the MDDObj.
The current domain should  be a subinterval of the definition domain, so that
tiles inserted in the object should always
be completely contained in the definition domain of the object.  This is not
checked here!

Objects of this class provide the needed functionality of MDDObjs to the
RasDaMan server, namely, access to the tiles
(which are the actual units of execution, processing and management internally
at the server).

Even though tiles are the units of execution in RasDaMan, once a tile is
inserted in an MDDObj, it is no longer an
independent entity.  It should only be deleted from its storage domain through
the MDD object it belongs to. Each MDDObj
is responsible for managing its own tiles, including deallocation of memory
occupied by the tiles.
The memory management is delegated to the index.  Only when the MDDObjIx is
deleted the tiles that were accessed during
the transaction will be removed from memory.

*/
class MDDObj : public NullValuesHandler
{
public:
    //@Man: Constructors
    //@{

    MDDObj(const MDDBaseType *mddType, const r_Minterval &domain);
    /**
        Creates a new transient MDD object with definition domain <tt>domain </tt> and type <tt>mddType</tt>.
        The newly created object has no tiles.
    */
    MDDObj(const MDDBaseType *mddType, const r_Minterval &domain,
           r_Nullvalues *newNullValues);
    /**
        Creates a new transient MDD object with definition domain <tt>domain </tt>,
       type <tt>mddType</tt> and null values
       newNullValues.
        The newly created object has no tiles.
    */

    /// Creates a new persistent MDD object using preallocated OId <tt>newOId</tt>.
    MDDObj(const MDDBaseType *mddType, const r_Minterval &domain,
           const OId &newOId, const StorageLayout &ms);
    /**
        Creates a new persistent MDD object with definition domaini <tt>domain</tt>
       and type <tt>mddType</tt>.
        The newly created object has no tiles.
        <tt>newOId </tt> must have been previously allocated with <tt>OIdIf::allocateOId() </tt>
        Throws an exception if the object already exists or if the OId is not valid.
    */

    MDDObj(const MDDBaseType *mddType, const r_Minterval &domain, const OId &newOId);
    /**
        Creates a new persistent MDD object with definition domaini <tt>domain</tt> and type <tt>mddType</tt>.
        The newly created object has no tiles.
        <tt>newOId </tt> must have been previously allocated with <tt>OIdIf::allocateOId() </tt>
        Throws an exception if the object already exists or if the OId is not valid.
        This should only be used  by mdds that contain file tiles
    */

    /// Opens an existent transient/persistent MDD object
    MDDObj(const DBMDDObjId &dbmddobj);
    /**
        Throws an exception if the object does not exists.
    */

    /// Opens an existent persistent MDD object which has the OIdi <tt>givenOId </tt>
    MDDObj(const OId &givenOId);
    /**
        Throws an exception if the object does not exists.
    */

    ///
    MDDObj(const MDDBaseType *mddType, const r_Minterval &domain, const StorageLayout &ms);
    /**
        Creates a new persistent MDD object with definition domain <tt>domain</tt>, 
        storage layout <tt>ms</tt> and type <tt>mddType</tt>.
        The newly created object has no tiles.
    */

    //@}

    //@Man: Object Identification:
    //@{
    int getOId(OId *pOId) const;
    /**
    returns 0 if object has an OId.
    */

    int getEOId(EOId *pEOId) const;
    /**
    returns 0 if object has an EOId.
    */
    //@}

    //@Man: Printing the status of the object.
    //@{
    /// Prints current status of the object.
    void printStatus(unsigned int level, std::ostream &stream) const;
    //@}

    //@Man: Insertion of new tiles in the MDD object:
    //@{
    /**
        After insertion of tiles in an MDDObj, the object becomes responsible for managing the memory allocated for the tiles inserted.
        Deallocation is done by the destructor of the index which is called in ~MDDObj.
        When new tiles are inserted in an MDDObj, the current domain for the object is updated.  This information is kept in the index.
        Neither type nor domain compatibility is checked.
    */

    /// Inserts new tile into the object.
    void insertTile(std::shared_ptr<Tile> insertTile);

    /// Inserts new tile into the object. MDDObj becomes the owner of the pointer.
    void insertTile(Tile *newTile);

    //@}

    //@Man: Removal of tiles from the MDD object:
    //@{
    /**
    */
    void removeTile(std::shared_ptr<Tile> &tileToRemove);
    /**
        Removes tile from the object.
        This functon is not implemented yet.
    */
    //@}

    //@Man: Retrieval of tiles from the MDD object:
    //@{
    /**
        The methods which allow access to tiles of the MDDObj return pointers to
       tiles in the object, which continue being
       managed by the MDDObject. For that reason, the caller should not free the
       returned pointers to tiles.
    */

    /// Finds all tiles of the object which intersect searchInter.
    std::vector<std::shared_ptr<Tile>> *intersect(const r_Minterval &searchInter) const;
    /**
        Returns a vector of pointers to the intersected tiles which belong to the MDDObj.
        The returned vector but not the tiles must be freed by the caller.
    */

    /// Returns all the tiles belonging to the object.
    std::vector<std::shared_ptr<Tile>> *getTiles() const;
    /**
        Returns a vector with all the tiles which belong to the MDDObj.
        The returned vector but not the tiles must be freed by the caller.
    */

    /// Gets the cell with coordinates <tt>searchPoint</tt> in the MDD.
    const char *pointQuery(const r_Point &searchPoint) const;
    /**
        Returns null pointer if cell doesnt exist in the object.
    */

    //@}

    //@Man: Cell and domain properties of the MDD Object:
    //@{

    /// Returns the MDDBaseType of the object.
    const MDDBaseType *getMDDBaseType() const;

    /// Returns the domain of the object as it was given in the definition.
    r_Minterval getDefinitionDomain() const;

    /// Returns the current domain for the object.
    r_Minterval getCurrentDomain() const;

    /// Get cell type name.
    const char *getCellTypeName() const;

    /// Get base type.
    const BaseType *getCellType() const;

    /// Returns the dimensionality of the object.
    r_Dimension getDimension() const;
    //@}

    //@Man: Miscellaneous Methods
    //@{
    ///This method is used to get around a bug in the qlparser.
    //      void setDoNotUseThisMethodItIsABugFix(bool yes);

    ///This method is used to get around a bug in the qlparser.
    //      bool isDoNotUseThisMethodItIsABugFix() const;

    /// Tells if object is persistent.
    bool isPersistent() const;

    /// Returns a pointer to the actual object in the base DBMS.
    DBMDDObjId getDBMDDObjId() const;
    //@}

    //@Man: Destructor
    //@{
    /// Destructor - frees dynamic memory.
    ~MDDObj() noexcept(false);
    //@}

    /// release all tiles from the index
    void releaseTiles();

    /// Return the storage layout of this object
    StorageLayout *getStorageLayout() const;

    /// Override method in NullValuesHandler, in order to set null values to the
    /// underlying database object
    void setUpdateNullValues(r_Nullvalues *newNullValues);

    /// write nullValues to a given tile...
    /// given a tile from (op) with the result of getContents() being resDataPtr,
    /// and total # cells in that tile = cellCount, this method initializes
    /// the entire tile with nullValues derived from (op).
    /// A templated helper function addresses the stored data type.
    void fillTileWithNullvalues(char *resDataPtr, size_t cellCount) const;

    std::string getArrayInfo(common::PrintTiles printTiles) const;
    std::string getEmbeddedArrayInfo(common::PrintTiles printTiles) const;
    std::string getJsonArrayInfo() const;
    std::string getSvgArrayInfo() const;
    void printArrayTiles(const std::unique_ptr<std::vector<std::shared_ptr<Tile>>> &tiles,
                         std::ostream &os) const;

    /// sets the Collection Type
    void setCollType(const CollectionType *newCollType);
    /// returns pointer to the collection type
    const CollectionType *getCollType() const;

    /// sets the domain in the database
    void setDbDomain(const r_Minterval &domain);

protected:
    /// does some consistency checks for regular tiling with rc index
    const r_Minterval &checkStorage(const r_Minterval &domain);

    /// The data class that holds all information
    DBMDDObjId myDBMDDObj;

    /// The index class that is used to access tile, before deleting thems
    MDDObjIx *myMDDIndex;

    /// The storage class which is reponsible for the tiling
    StorageLayout *myStorageLayout;

    //collection type
    const CollectionType *collType{NULL};

    //      bool doNotUseThisBugFix;
    /**
        The qlparser deletes transient mdd objects also in some cases (when
       passing transient mddobjs to a transient
       collection) this is bad.
        Therefore the qlparser checks for transient mdds if they have this switch
       set to on, before deleting them.
    */
};

#endif
