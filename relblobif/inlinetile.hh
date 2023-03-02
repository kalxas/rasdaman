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
// -*-C++-*- (for Emacs)

/*************************************************************
 *
 *
 * PURPOSE:
 *   The inlinetile class is used to store small tiles efficiently.
 *   Potentially many inlinetiles are grouped together in a blob and
 *   stored in the database. highly dependend on DBTCIndex.
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#pragma once

#include "blobtile.hh"  // for BLOBTile
#include "tileid.hh"
#include "reladminif/oidif.hh"  // for OId
#include "raslib/mddtypes.hh"   // for r_Bytes, r_Data_Format, r_Array

#include <iosfwd>  // for cout, ostream

class r_Error;
class OId;

//@ManMemo: Module: {\bf relblobif}.

/*@Doc:

InlineTile is the persistent class for storing the contents of MDD tiles
in the database.  it can be stored as a blobtile or inlined:
in inlined mode multiple inlinetiles are stored as one blob in the database.
memory management and modification management is critical.
there are special functions in objectbroker to retrieve inlinetiles.
they can only be inlined by a dbtcindex.

*/

/**
  * \ingroup Relblobifs
  */
class InlineTile : public BLOBTile
{
public:
    //@Man: constructors
    //@{

    explicit InlineTile(r_Data_Format dataformat = r_Array);
    /*@Doc:
    constructs a new empty InlineTile and gets an id for it.
    */

    explicit InlineTile(const OId &BlobId);
    /*@Doc:
    constructs a InlineTile out of the database
    */

    InlineTile(r_Bytes newSize, r_Data_Format dataformat);
    /*@Doc:
    constructs a new InlineTile of size newSize, not initialized with any value.
    */

    InlineTile(r_Bytes newSize, char c, r_Data_Format dataformat);
    /*@Doc:
    constructs a new InlineTile of size newSize filled with c.
    */

    InlineTile(r_Bytes newSize, const char *newCells, r_Data_Format dataformat);
    /*@Doc:
    constructs a new InlineTile of size newSize filled with the contents of newCells.
    The newCells are copied if takeNewCellsOwnership is false, otherwise the pointer
    newCells is directly owned by DBTile.
    */

    InlineTile(r_Bytes newSize, bool takeOwnershipOfNewCells,
               char *newCells, r_Data_Format dataformat);
    /*@Doc:
    constructs a new InlineTile of size newSize filled with the contents of newCells.
    */

    ~InlineTile() override = default;
    /*@Doc:
        no functionality.  if it is inlined the dbtcindex will take care of storing it.
        if it is not inlined the blobtile functionality will take over.
    */

    //@}
    void destroy() override;
    /*@Doc:
    may not destroy the object because it is inlined and therefore depending on
    its parent index.
    */

    const OId &getIndexOId() const;
    /*@Doc:
    returns the oid of the index which contains the inlined tile.  if the tile is
    outlined then this oid is invalid.
    */

    void setIndexOId(const OId &oid);
    /*@Doc:
    make the inlinetile use this index as its parent and storage structure.
    */

    r_Bytes getStorageSize() const;
    /*@Doc:
    returns the size this tile will consume in as an inlined array.
    */

    virtual char *insertInMemBlock(char *test);
    /*@Doc:
        inserts the Blob into the char.
        the returned pointer is after the end of this tiles data.
    */

    void setModified() override;
    /*@Doc:
        does not only set itself modified but also informs its parent of changes.
    */

    virtual bool isCached() const;
    /*@Doc:
        returns true if it is inlined.
    */

    virtual void inlineTile(const OId &ixOId);
    /*@Doc:
        do everything so that this tile is inlined and uses ixOId as its index parent.
        it will not check if this tile is already inlined.
    */

    virtual void outlineTile();
    /*@Doc:
        does everything necessary to act as a blobtile:
        remove it from the index parent.
    */

    virtual bool isInlined() const;
    /*@Doc:
        checks if it has a valid index parent.
    */

    void printStatus(unsigned int level, std::ostream &stream) const override;

protected:
    OId myIndexOId;
    /*@Doc:
        when this inlinetile is in inlined mode the myIndexOId points to the parent index.
        if this oid is invalid the inlinetile is not in inline mode.
    */
};
