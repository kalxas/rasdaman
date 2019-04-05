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
#ifndef _DBHIERINDEX_HH_
#define _DBHIERINDEX_HH_

#include "reladminif/dbobject.hh"
#include "indexmgr/hierindexds.hh"
#include "relcatalogif/inlineminterval.hh"

//@ManMemo: Module: {\bf relindexif}
/*@Doc:
This class stores data of hierarchical indexes in the database.

There should be another interface to include the isLeaf/isRoot/...
functionality.

Beware of the cache when droping the IndexDS classes!

See indexmgr/hierindexds.hh and indexmgr/indexds.hh for documentation.

    // Old Blob format rasdaman < 9.0
    headerbyte      char with value 13
    lowerbounds     sizeof(r_Range) * (numEntries + 1) * dimension
    upperbounds     sizeof(r_Range) * (numEntries + 1) * dimension
    lowerfixed      sizeof(char) * (numEntries + 1) * dimension;
    upperfixed      sizeof(char) * (numEntries + 1) * dimension;
    entryids        sizeof(int) * numEntries
    entrytypes      sizeof(char) * numEntries

    // New Blob format (optimized for 64-bit alignment, rasdaman >= 9.0)
    header          long long  (>= 1009 is new format, otherwise considered to be old format)
    lowerbounds     sizeof(r_Range) * (numEntries + 1) * dimension
    upperbounds     sizeof(r_Range) * (numEntries + 1) * dimension
    lowerfixed      sizeof(char) * (numEntries + 1) * dimension;
    upperfixed      sizeof(char) * (numEntries + 1) * dimension;
    entryids        sizeof(int) * numEntries
    entrytypes      sizeof(char) * numEntries

    readfromDb() is able to read both formats
    updateInDb() and insertInDb will only write new format


*/
/**
  * \ingroup Relindexifs
  */

class DBHierIndex : public HierIndexDS
{
public:
    DBHierIndex(r_Dimension dim, bool isNode, bool makePersistent);
    /*@Doc:
        constructs a new index with type ixType, dimension dim.
        if isNode is true the index behaves as a node, else as
        a leaf instance is imediately persistent
    */

    double getOccupancy() const override;

    HierIndexDS *getParent() const override;

    void setParent(const HierIndexDS *newPa) override;

    void setIsNode(bool beNode) override;

    bool isLeaf() const override;

    bool isRoot() const override;
    /*@Doc:
        is a check for a valid myParent OId
    */

    unsigned int getHeight() const override;

    virtual unsigned int getHeightOfTree() const;
    /*@Doc:
        Recursive function to get height of the tree.
    */

    virtual unsigned int getHeightToRoot() const;
    /*@Doc:
        Recursive function to get the number of levels to the root.
    */

    virtual unsigned int getHeightToLeaf() const;
    /*@Doc:
        Recursive function to get the number of levels to the
        leafs.
    */

    unsigned int getTotalEntryCount() const override;

    unsigned int getTotalNodeCount() const override;

    unsigned int getTotalLeafCount() const override;

    r_Minterval getCoveredDomain() const override;

    r_Minterval getAssignedDomain() const override;

    r_Minterval getObjectDomain(unsigned int pos) const override;

    r_Dimension getDimension() const override;

    void setAssignedDomain(const r_Minterval &domain) override;

    unsigned int getSize() const override;

    r_Bytes getTotalStorageSize() const override;

    bool isValid() const override;

    bool isUnderFull() const override;

    bool isOverFull() const override;

    bool isSameAs(const IndexDS *pix) const override;

    bool removeObject(unsigned int pos) override;

    bool removeObject(const KeyObject &theKey) override;

    void insertObject(const KeyObject &theKey, unsigned int pos) override;

    void setObject(const KeyObject &theKey, unsigned int pos) override;

    void setObjectDomain(const r_Minterval &dom, unsigned int pos) override;

    const KeyObject &getObject(unsigned int pos) const override;

    void getObjects(KeyObjectVector &objs) const override;

    unsigned int getOptimalSize() const override;

    static unsigned int getOptimalSize(r_Dimension dim);
    /*@Doc:
        Used to calculate the optimal number of entries for
        that dimension
    */

    void freeDS() override;

    OId::OIdPrimitive getIdentifier() const override;

    static r_Bytes BytesPerTuple;
    /*@Doc:
        tuning parameter.  used to calculate the optimal size of
        an index.  this is also the number of bytes written to the
        database.
    */

    void printStatus(unsigned int level, std::ostream &stream) const override;

    ~DBHierIndex() noexcept(false) override;

    void destroy() override;

    IndexDS *getNewInstance() const override;

    BinaryRepresentation getBinaryRepresentation() const override;

    void setBinaryRepresentation(const BinaryRepresentation &) override;

protected:
    friend class ObjectBroker;
    /*@Doc:
        ObjectBroker needs to access OId constructor
    */

    DBHierIndex(const OId &id);
    /*@Doc:
    */

    void readFromDb() override;
    /*@Doc:
    */

    void updateInDb() override;
    /*@Doc:
    */

    void deleteFromDb() override;
    /*@Doc:
    */

    void insertInDb() override;
    /*@Doc:
    */

    void extendCoveredDomain(const r_Minterval &newTilesExtents);
    /*@Doc:
        Recalculates the current domain of this index to
        include newTilesExtents.
    */

    OId parent;
    /*@Doc:
        persistent, identifies the parent
    */

    bool _isNode;
    /*@Doc:
        persistent, tells the object what it is.
    */

    unsigned int maxSize;
    /*@Doc:
        Non persistent attribute.  a cache so the maxSize does not have to be
       calculated all the time.
    */

    KeyObjectVector myKeyObjects;

    InlineMinterval myDomain;
    /*@Doc:
        Defined domain of this index.
    */

    short currentDbRows;
    /*@Doc:
        is needed to support update of index in database
        keeps the number of rows currently taken up in the db by
        this instance
    */
};
#endif
