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
 * INCLUDE: transdirix.hh
 *
 * MODULE:  indexmgr
 * CLASS:   TransDirIx
 *
 * COMMENTS:
 *
*/

#ifndef _TRANSDIRIX_HH_
#define _TRANSDIRIX_HH_

#include "indexds.hh"           // for IndexDS
#include "keyobject.hh"         // for KeyObject
#include "reladminif/lists.h"   // for KeyObjectVector, DomainPVector
#include "reladminif/oidif.hh"  // for OId, OId::OIdPrimitive
#include "raslib/mddtypes.hh"   // for r_Dimension, r_Bytes
#include "raslib/minterval.hh"  // for r_Minterval

#include <iosfwd>                   // for cout, ostream

/**
 *  @file transdirix.hh
 *
 *  @ingroup indexmgr
 */

/*@Doc:

A TransDirIx object is the data structure for an index of a transient MDD
object. It is to be used with DirIndexLogic.
A transient directory index keeps track of the current domain of the object.
It is a very simple structure, consisting of the current domain of the object
and a set of entries, one for each object belonging to the mdd object.

For documentation on methods see IndexDS.
*/

class TransDirIx : public IndexDS
{
public:
    TransDirIx(r_Dimension dim);
    /*@Doc:
    Creates a new transient index for an object with dimensionality
    {\tt dim}.
    */

    void printStatus(unsigned int level, std::ostream &stream) const override;

    void insertObject(const KeyObject &newKeyObject, unsigned int pos) override;
    /*@Doc:
    Inserts a new tile in the index at position {\tt pos}, which must be
    between 0 and {\tt getNumberElems()} (that is, {\tt pos } is
    interpreted as an index in the new list. If {\tt pos} is getNumberElems(),
    the element is put at the end of the list. All elements at following
    positions are shifted to the right. The new tile to insert ({\tt newKeyObject})
    must be transient (of type TransKeyObject). The current domain is updated.
    */

    void setObject(const KeyObject &theKey, unsigned int pos) override;

    void setObjectDomain(const r_Minterval &dom, unsigned int pos) override;

    bool removeObject(unsigned int pos) override;

    bool removeObject(const KeyObject &theKey) override;

    bool isValid() const override;

    bool isUnderFull() const override;

    bool isOverFull() const override;

    bool isSameAs(const IndexDS *pix) const override;

    const KeyObject &getObject(unsigned int pos) const override;

    r_Minterval getObjectDomain(unsigned int pos) const override;

    DomainPVector *getObjectDomains() const;

    void getObjects(KeyObjectVector &) const override;

    r_Minterval getCoveredDomain() const override;

    r_Minterval getAssignedDomain() const override;

    r_Dimension getDimension() const override;

    void setAssignedDomain(const r_Minterval &domain) override;

    unsigned int getSize() const override;

    r_Bytes getTotalStorageSize() const override;

    bool isPersistent() const;

    ~TransDirIx() override;
    /*@Doc:
        Destructor - deletes tiles from main memory.
    */

    unsigned int getOptimalSize() const override;

    void freeDS() override;
    /*@Doc:
        does not do anything - there is no persistent data structure
    */

    OId::OIdPrimitive getIdentifier() const override;

    IndexDS *getNewInstance() const override;

private:
    r_Minterval currDomain;
    /**
    Always set automatically to the MBR of the tiles in {\tt tiles}.
    If the number of tiles is zero, the currDomain is invalid (may have any
    values). All methods dealing with the currDomain must then check whether
    the object has tiles in order to operate on the currDomain.
    */

    KeyObjectVector tiles;
};


#endif
