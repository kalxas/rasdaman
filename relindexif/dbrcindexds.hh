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
/*************************************************************
 *
 *
 * PURPOSE:
 * RC index RDBMS adaptor include file.
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef _DBRCINDEXDS_HH_
#define _DBRCINDEXDS_HH_

#include "reladminif/dbobject.hh"
#include "indexmgr/indexds.hh"
#include "relcatalogif/inlineminterval.hh"

//@ManMemo: Module: {\bf relindexif}
/*@Doc:
Can't have DBRCIndex and DBHierIndex under a common father class because the
compiler has problems resolving the DBRef<>
functions correctly.

Persistent class for storing data on regular computed indexes.

Beware of the cache when droping the IndexDS classes!

See indexmgr/indexds.hh for documentation.

Data to store:
RAS_RCINDEXDYN
    OId     bigint
    Count   integer
    DynData VARCHAR(3990)
NB: under Oracle 9i, Dyndata is defined as:
    DynData BLOB NOT NULL
This should be done for the other systems too,
as VARCHAR usually is subject to charset translation
which we do not want on our binary data.

    // Old Blob format rasdaman < 9.0
    dimension       int                         (r_Dimension is int)
    myBaseOIdType   short                       (enum is int)
    myBaseCounter   int                         (old OId::OIdCounter is int)
    mySize          int                         (unsigned int)
    boundssize      sizeof(r_Range) * dimension (r_Range is int)
    fixessize       sizeof(char) * dimension

    // New Blob format (optimized for 64-bit alignment, rasdaman >= 9.0)
    header          int32                       (>=1008 is new format, otherwise considered to be old format)
    dimension       int32                       (r_Dimension is int)
    myBaseOIdType   long long                   (enum is int)
    myBaseCounter   long long                   (OId::OIdCounter is long long)
    mySize          long long                   (long long)
    boundssize      sizeof(r_Range) * dimension (r_Range is int)
    fixessize       sizeof(char) * dimension

*/
/**
  * \defgroup Relindexifs Relindexif Classes
  */

/**
  * \ingroup Relindexifs
  */

class DBRCIndexDS : public IndexDS
{
public:
    DBRCIndexDS(const r_Minterval &definedDomain,
                unsigned int numberTiles,
                OId::OIdType theEntryType = OId::BLOBOID);
    /*@Doc:
        Create a new index which handles the domain definedDomain, with tiles of domain
        tileConfig.  As soon as you create this index it will check if the tileConfig fits
        the definedDomain (the tileConfig must completely cover the definedDomain) and then
        allocate as many oids as are necessary to fill the definedDomain.
    */

    r_Minterval getCoveredDomain() const override;
    /// return defined domain

    r_Minterval getAssignedDomain() const override;
    /// return defined domain

    r_Minterval getObjectDomain(unsigned int pos) const override;
    /// throw r_Error_FeatureNotSupported

    r_Dimension getDimension() const override;

    void setAssignedDomain(const r_Minterval &domain) override;
    /// throw r_Error_FeatureNotSupported

    unsigned int getSize() const override;
    /// this will return the maximum number of tiles that can be stored in the
    /// definedDomain.

    r_Bytes getTotalStorageSize() const override;

    bool isValid() const override;
    /// returns true

    bool isUnderFull() const override;
    /// returns false

    bool isOverFull() const override;
    /// returns false

    bool isSameAs(const IndexDS *pix) const override;

    bool removeObject(unsigned int pos) override;
    /// throw r_Error_FeatureNotSupported

    bool removeObject(const KeyObject &theKey) override;
    /// throw r_Error_FeatureNotSupported

    void insertObject(const KeyObject &theKey, unsigned int pos) override;
    /// throw r_Error_FeatureNotSupported

    void setObject(const KeyObject &theKey, unsigned int pos) override;
    /// throw r_Error_FeatureNotSupported

    void setObjectDomain(const r_Minterval &dom, unsigned int pos) override;
    /// throw r_Error_FeatureNotSupported

    const KeyObject &getObject(unsigned int pos) const override;
    /// throw r_Error_FeatureNotSupported

    void getObjects(KeyObjectVector &objs) const override;
    /// throw r_Error_FeatureNotSupported

    unsigned int getOptimalSize() const override;
    /// returns the maximum number of entries that can be stored in this index

    void freeDS() override;

    OId::OIdPrimitive getIdentifier() const override;

    static r_Bytes BytesPerTuple;
    /*@Doc:
        tuning parameter.  used to calculate the optimal size of
        an index.  this is also the number of bytes written to the
        database.
    */
    void printStatus(unsigned int level, std::ostream &stream) const override;

    ~DBRCIndexDS() noexcept(false) override;

    void destroy() override;

    IndexDS *getNewInstance() const override;
    /// throw r_Error_FeatureNotSupported

    virtual OId::OIdType getBaseOIdType() const;

    virtual OId::OIdCounter getBaseCounter() const;

protected:
    friend class ObjectBroker;
    /*@Doc:
        ObjectBroker needs to access OId constructor
    */

    DBRCIndexDS(const OId &id);
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

    OId::OIdCounter myBaseCounter;
    /*@Doc:
        The first oid that will be used to store entries.
    */

    OId::OIdType myBaseOIdType;
    /*@Doc:
        The type of objects to store in this index (most of the time this will be OId::BLOBOID).
    */

    OId::OIdCounter mySize;

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
