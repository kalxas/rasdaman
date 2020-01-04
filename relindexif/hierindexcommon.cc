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
 * maintain the hierarchical index in the DBMS.
 *
 *
 * COMMENTS:
 * - relies on the same-name DBMS preprocessor sources for the
 *   various DBMSs supported.
 *
 ************************************************************/

#include "config.h"
#include "hierindex.hh"                 // for DBHierIndex
#include "reladminif/dbobject.hh"       // for DBObjectId, DBObject
#include "reladminif/dbref.hh"
#include "reladminif/lists.h"           // for KeyObjectVector
#include "reladminif/objectbroker.hh"   // for ObjectBroker
#include "reladminif/oidif.hh"          // for OId, operator<<, OId::OIdCounter
#include "relblobif/blobtile.hh"
#include "indexmgr/hierindexds.hh"      // for HierIndexDS
#include "indexmgr/indexds.hh"          // for IndexDS
#include "indexmgr/keyobject.hh"        // for KeyObject, operator<<
#include "storagemgr/sstoragelayout.hh" // for StorageLayout, StorageLayout:...
#include "relindexif/indexid.hh"        // for DBHierIndexId
#include "relcatalogif/inlineminterval.hh"  // for InlineMinterval
#include "raslib/error.hh"              // for r_Error
#include "raslib/mddtypes.hh"           // for r_Bytes, r_Range, r_Dimension
#include "raslib/minterval.hh"          // for operator<<, r_Minterval
#include "raslib/endian.hh"
#include "mymalloc/mymalloc.h"
#include <logging.hh>                   // for Writer, CTRACE, LTRACE, CDEBUG

#include <cassert>
#include <algorithm>                    // for max
#include <cstring>                      // for memcpy, memset, memcmp, strcmp
#include <memory>                       // for allocator_traits<>::value_type
#include <ostream>                      // for operator<<, ostream, basic_os...
#include <vector>                       // for vector


// old format, contains 13 in first byte
const int DBHierIndex::BLOB_FORMAT_V1 = 8;
const int DBHierIndex::BLOB_FORMAT_V1_HEADER_SIZE = 1;
const int DBHierIndex::BLOB_FORMAT_V1_HEADER_MAGIC = 13;
// OIDcounter is now long, but r_Range is still int
const int DBHierIndex::BLOB_FORMAT_V2 = 9;
const int DBHierIndex::BLOB_FORMAT_V2_HEADER_SIZE = 8;
const long long DBHierIndex::BLOB_FORMAT_V2_HEADER_MAGIC = 1009;
// blobFormat == 10: r_Range is long as well
const int DBHierIndex::BLOB_FORMAT_V3 = 10;
const int DBHierIndex::BLOB_FORMAT_V3_HEADER_SIZE = 8;
const long long DBHierIndex::BLOB_FORMAT_V3_HEADER_MAGIC = 1010;


DBHierIndex::DBHierIndex(const OId &id)
    : HierIndexDS(id), myDomain(0u)
{
    if (id.getType() == OId::MDDHIERIXOID)
        readFromDb();
    
    maxSize = DBHierIndex::getOptimalSize(getDimension());
    myKeyObjects.reserve(maxSize);
}

DBHierIndex::DBHierIndex(r_Dimension dim, bool isNODE, bool makePersistent)
    : HierIndexDS(), _isNode(isNODE), myDomain(dim), currentDbRows(-1)
{
    objecttype = OId::MDDHIERIXOID;
    if (makePersistent)
        setPersistent(true);

    maxSize = getOptimalSize(dim);
    myKeyObjects.reserve(maxSize);
    setCached(true);
}

IndexDS *DBHierIndex::getNewInstance() const
{
    return new DBHierIndex(getDimension(), !isLeaf(), true);
}

OId::OIdPrimitive DBHierIndex::getIdentifier() const
{
    return myOId;
}

bool DBHierIndex::removeObject(const KeyObject &entry)
{
    bool found = false;
    OId oid(entry.getObject().getOId());
    for (auto i = myKeyObjects.begin(); i != myKeyObjects.end();)
    {
        if (oid == (*i).getObject().getOId())
        {
            LTRACE << "remove object " << oid << " from index.";
            found = true;
            i = myKeyObjects.erase(i);
            setModified();
        }
        else
        {
            ++i;
        }
    }
    if (!found)
    {
        LDEBUG << "object " << oid << " to remove not found in index.";
    }
    return found;
}

bool DBHierIndex::removeObject(unsigned int pos)
{
    bool found = false;
    if (pos <= myKeyObjects.size())
    {
        found = true;
        myKeyObjects.erase(myKeyObjects.begin() + pos);
        setModified();
    }
    else
    {
        LWARNING << "cannot remove object at position " << pos << ", index node "
                 << "contains only " << myKeyObjects.size() << " entries.";
    }
    return found;
}

void DBHierIndex::insertObject(const KeyObject &theKey, unsigned int pos)
{
    if (!isLeaf())
    {
        DBHierIndexId(theKey.getObject())->setParent(this);
    }
    if (myKeyObjects.size() == 0)
    {
        // first tile to be inserted in the index, initialize domain
        myDomain = theKey.getDomain();
    }
    else
    {
        extendCoveredDomain(theKey.getDomain());
    }
    myKeyObjects.insert(myKeyObjects.begin() + pos, theKey);
    LTRACE << "after inserting object now have " 
           << myKeyObjects.size() << " objects in key object vector.";
    setModified();
}

void DBHierIndex::setObjectDomain(const r_Minterval &dom, unsigned int pos)
{
    myKeyObjects[pos].setDomain(dom);
    // might be unneccessary/harmfull, check later
    extendCoveredDomain(dom);

    // setModified(); done in domain extension
    if (!isLeaf())
    {
        DBHierIndexId t(myKeyObjects[pos].getObject());
        t->setAssignedDomain(dom);
    }
}

void DBHierIndex::setObject(const KeyObject &theKey, unsigned int pos)
{
    myKeyObjects[pos] = theKey;
    setModified();
    if (!isLeaf())
    {
        DBHierIndexId(theKey.getObject())->setParent(this);
    }
}

r_Minterval DBHierIndex::getCoveredDomain() const
{
    return myDomain;
}

r_Dimension DBHierIndex::getDimension() const
{
    return myDomain.dimension();
}

r_Bytes DBHierIndex::getTotalStorageSize() const
{
    r_Bytes sz = 0;
    for (auto i = myKeyObjects.begin(); i != myKeyObjects.end(); i++)
    {
        sz = sz +
             (static_cast<DBObject *>(
                  ObjectBroker::getObjectByOId(i->getObject().getOId())))
             ->getTotalStorageSize();
    }
    return sz;
}

bool DBHierIndex::isValid() const
{
    LTRACE << "check if hierarchical index " << myOId << " is valid...";
    
    bool valid = true;
    // may not be unsigned int (r_Area) because of error check
    long long area = 0;
    if (!isLeaf())
    {
        area = static_cast<long long>(myDomain.cell_count());
        DBHierIndexId tempIx;
        LTRACE << "inspecting " << myKeyObjects.size() << " objects in key object vector.";
        for (auto i = myKeyObjects.begin(); i != myKeyObjects.end(); i++)
        {
            if (myDomain.covers((*i).getDomain()))
            {
                // ok
                area = area - static_cast<long long>((*i).getDomain().cell_count());
            }
            else
            {
                if (myDomain == (*i).getDomain())
                {
                    // ok
                    area = area - static_cast<long long>((*i).getDomain().cell_count());
                    tempIx = DBHierIndexId((*i).getObject());
                    if (!tempIx->isValid())
                    {
                        LTRACE << "invalid entry which equals the index domain " << myDomain;
                        valid = false;
                        break;
                    }
                }
                else
                {
                    LTRACE << "invalid, key " << *i << " does not cover domain " << myDomain;
                    valid = false;
                    break;
                }
            }
        }
        if (valid && area < 0)
        {
            LTRACE << "invalid, there are double entries";
            valid = false;
        }
    }
    else
    {
        area = static_cast<long long>(myDomain.cell_count());
        for (auto i = myKeyObjects.begin(); i != myKeyObjects.end(); i++)
        {
            if (myDomain.intersects_with((*i).getDomain()))
            {
                // ok
                area = area - static_cast<long long>(
                            (*i).getDomain().create_intersection(myDomain).cell_count());
            }
            else
            {
                LTRACE << "invalid, key " << *i << " does not intersect domain " << myDomain;
                valid = false;
                break;
            }
        }
        if (!valid && area < 0)
        {
            LTRACE << "invalid, there are double entries";
            valid = false;
        }
    }

    return valid;
}

void DBHierIndex::printStatus(unsigned int level, std::ostream &stream) const
{
    DBObjectId t;
    auto *indent = new char[level * 2 + 1];
    for (unsigned int j = 0; j < level * 2; j++) indent[j] = ' ';
    indent[level * 2] = '\0';

    stream << indent << "DBHierIndex ";
    if (isRoot())
        stream << "is Root ";
    else
        stream << "Parent " << parent << " ";
    if (isLeaf())
        stream << "Leaf ";
    else
        stream << "Node ";
    DBObject::printStatus(level, stream);
    stream << " size " << myKeyObjects.size() << " domain " << myDomain << std::endl;
    int count = 0;
    LTRACE << "inspecting " << myKeyObjects.size() << " objects in key object vector.";
    for (auto i = myKeyObjects.begin(); i != myKeyObjects.end(); i++)
    {
        stream << indent << " entry #" << count << " is " << *i << std::endl;
        if (!isLeaf())
        {
            t = DBObjectId((*i).getObject());
            if (t.is_null())
                stream << indent << " entry is null";
            else
                t->printStatus(level + 1, stream);
        }
        stream << std::endl;
        count++;
    }
    delete[] indent;
}

unsigned int DBHierIndex::getSize() const
{
    return static_cast<unsigned int>(myKeyObjects.size());
}

bool DBHierIndex::isUnderFull() const
{
    //redistribute in srptindexlogic has to be checked first before any other return value may be assigned
    return false;
}

bool DBHierIndex::isOverFull() const
{
    return getSize() >= maxSize;
}

unsigned int DBHierIndex::getOptimalSize(r_Dimension dim)
{
    if (StorageLayout::DefaultIndexSize != 0)
        return StorageLayout::DefaultIndexSize;
    
    // BLOCKSIZE
    unsigned int blocksize = 0;
    unsigned int useablespace = 0;
    // dimension * (upperbound + upperfixed + lowerbound + lowerfixed) + entryid +
    // entryoidtype
    static constexpr unsigned int onedim = sizeof(r_Range) * 2 + sizeof(char) * 2;
    unsigned int onedom = dim * onedim;
    unsigned int oneentry = onedom + sizeof(OId::OIdCounter) + sizeof(char);
    
#ifdef BASEDB_ORACLE
    blocksize = 2048;
    // BLOCKSIZE - (BLOCK OVERHEAD + ROW OVERHEAD + 1 * largerow + number(15,0) + short)
    useablespace = blocksize - (130 + 3 + 1 * 3 + 12 + 2);
#elseif BASEDB_DB2
    blocksize = 4096;
    // from the manual
    useablespace = 3990;
#elseif BASEDB_INFORMIX
    blocksize = 4096;
    // from the manual
    useablespace = 3990;
#elseif BASEDB_PGSQL
    blocksize = 8192;   // default only!!!;
    useablespace = 7000;    // no indication for any less space available, but to be sure we go a little lower -- PB 2005-jan-10
#elseif BASEDB_SQLITE
    // blocksize = 4096;
    useablespace = 3990;
#else
    blocksize = 8192;
    useablespace = 7000;
#endif

    // remove mydomain size and header
    useablespace = useablespace - onedom - BLOB_FORMAT_V3_HEADER_SIZE;
    
    // minimum size is 8-lucky guess(good for 1,2,3,4 dimensions)
    return std::max(8u, useablespace / oneentry);
}

unsigned int DBHierIndex::getOptimalSize() const
{
    return maxSize;
}

r_Minterval DBHierIndex::getAssignedDomain() const
{
    return myDomain;
}

void DBHierIndex::setAssignedDomain(const r_Minterval &newDomain)
{
    myDomain = newDomain;
    setModified();
}

void DBHierIndex::extendCoveredDomain(const r_Minterval &newTilesExtents)
{
    myDomain.closure_with(newTilesExtents);
    setModified();
}

void DBHierIndex::setParent(const HierIndexDS *newPa)
{
    assert(newPa);
    if (static_cast<OId::OIdPrimitive>(parent) != newPa->getIdentifier())
    {
        parent = newPa->getIdentifier();
        setModified();
    }
}

HierIndexDS *DBHierIndex::getParent() const
{
    DBHierIndexId t(parent);
    return static_cast<HierIndexDS *>(t);
}

bool DBHierIndex::isRoot() const
{
    return parent.getType() == OId::INVALID;
}

bool DBHierIndex::isLeaf() const
{
    return !_isNode;
}

void DBHierIndex::setIsNode(bool isNodea)
{
    _isNode = isNodea;
}

void DBHierIndex::freeDS()
{
    setPersistent(false);
}

bool DBHierIndex::isSameAs(const IndexDS *other) const
{
    assert(other);
    return other->isPersistent() && myOId == other->getIdentifier();
}

double DBHierIndex::getOccupancy() const
{
    return 0;
}

const KeyObject &DBHierIndex::getObject(unsigned int pos) const
{
    return myKeyObjects.at(pos);
}

void DBHierIndex::getObjects(KeyObjectVector &objs) const
{
    for (auto keyIt = myKeyObjects.begin(); keyIt != myKeyObjects.end(); keyIt++)
        objs.push_back(*keyIt);
}

r_Minterval DBHierIndex::getObjectDomain(unsigned int pos) const
{
    return getObject(pos).getDomain();
}

unsigned int DBHierIndex::getHeight() const
{
    return getHeightToLeaf();
}

unsigned int DBHierIndex::getHeightOfTree() const
{
    return getHeightToLeaf() + getHeightToRoot();
}

unsigned int DBHierIndex::getHeightToRoot() const
{
    return isRoot() ? 0u : DBHierIndexId(parent)->getHeightToRoot() + 1;
}

unsigned int DBHierIndex::getHeightToLeaf() const
{
    return isLeaf() ? 0u : DBHierIndexId(parent)->getHeightToLeaf() + 1;
}

unsigned int DBHierIndex::getTotalLeafCount() const
{
    unsigned int retval = 0;
    if (!isLeaf())
    {
        // i am not a leaf
        assert(!myKeyObjects.empty());
        if (DBHierIndexId(myKeyObjects.begin()->getObject())->isLeaf())
        {
            // i contain only leafs, so i return the number of entries i contain
            retval = getSize();
        }
        else
        {
            // i contain only nodes, so i ask my children how many leafs there are
            for (auto keyIt = myKeyObjects.begin(); keyIt != myKeyObjects.end(); keyIt++)
            {
                retval = retval + DBHierIndexId((*keyIt).getObject())->getTotalLeafCount();
            }
        }
    }
    else
    {
        retval = 1;
    }
    return retval;
}

unsigned int DBHierIndex::getTotalNodeCount() const
{
    unsigned int retval = 0;
    if (!isLeaf())
    {
        // i am not a leaf
        assert(!myKeyObjects.empty());
        if (DBHierIndexId(myKeyObjects.begin()->getObject())->isLeaf())
        {
            // i contain only nodes, add the nodes i contain
            retval = getSize();
            // i add the nodes my children contain
            for (auto keyIt = myKeyObjects.begin(); keyIt != myKeyObjects.end(); keyIt++)
            {
                retval = retval + DBHierIndexId((*keyIt).getObject())->getTotalNodeCount();
            }
        }
    }
    // else : a leaf does not contain nodes

    return retval;
}

unsigned int DBHierIndex::getTotalEntryCount() const
{
    unsigned int retval = 0;
    if (isLeaf())
    {
        // i contain only entries
        // i return the number of entries i contain
        retval = getSize();
    }
    else
    {
        // i contain only nodes, no entries
        // i ask my children how many entries they contain
        for (auto keyIt = myKeyObjects.begin(); keyIt != myKeyObjects.end(); keyIt++)
        {
            retval = retval + DBHierIndexId((*keyIt).getObject())->getTotalEntryCount();
        }
    }
    return retval;
}

void DBHierIndex::destroy()
{
    DBObject::destroy();
}

DBHierIndex::~DBHierIndex() noexcept(false)
{
    validate();
    currentDbRows = 0;
    parent = OId(0);
    myKeyObjects.clear();
    maxSize = 0;
    _isNode = true;
}

/*
Encoding:
name  :
    type_oid.raw
value :
    common
    1 byte  version
    1 byte  endianness
    2 bytes type
    4 bytes oid
    1 byte  flags
    2 byte  reserved
    special
    4 bytes size
    2 bytes dimension
    8 bytes parent oid
    1 byte  subtype
    x bytes database layout
*/

// HST this might need an update for long long oid, but as the only user is
// text/exportindex.cc, this will be done later
//
BinaryRepresentation DBHierIndex::getBinaryRepresentation() const
{
    BinaryRepresentation brp;
    brp.binaryName = getBinaryName();
    brp.binaryData = nullptr;
    brp.binaryLength = 0;
    unsigned int dimension2 = myDomain.dimension();
    size_t size2 = myKeyObjects.size();
    short subtype = _isNode;
    long long parentid2 = 0;

    if (parent.getType() == OId::INVALID)
        parentid2 = 0;
    else
        parentid2 = parent;

    // INSERTINTO

    // number of bytes for bounds for "size" entries and mydomain
    r_Bytes boundssize = sizeof(r_Range) * (size2 + 1) * dimension2;
    // number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixessize = sizeof(char) * (size2 + 1) * dimension2;
    // number of bytes for ids of entries
    r_Bytes idssize = sizeof(OId::OIdCounter) * size2;
    // number of bytes for types of entries
    r_Bytes typessize = sizeof(char) * size2;
    // number of bytes for the dynamic data
    r_Bytes completesize = boundssize * 2 + fixessize * 2 + idssize + typessize;

    auto *completebuffer = new char[completesize];
    auto *upperboundsbuf = new r_Range[boundssize];
    auto *lowerboundsbuf = new r_Range[boundssize];
    auto *upperfixedbuf = new char[fixessize];
    auto *lowerfixedbuf = new char[fixessize];
    auto *entryidsbuf = new OId::OIdCounter[idssize];
    auto *entrytypesbuf = new char[typessize];

    LTRACE << "complete " << completesize << " bounds " << boundssize << " fixes "
           << fixessize << " ids " << idssize << " types " << typessize;

    // counter which keeps track of the bytes that have been written to the db
    //    r_Bytes byteswritten = 0;
    // counter which keeps track of the bytes that have to be written to the db
    //    r_Bytes bytestowrite = 0;

    myDomain.insertInDb(&(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    LTRACE << "domain " << myDomain << " stored as "
           << InlineMinterval(dimension2, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    // populate the buffers with data
    auto it = myKeyObjects.begin();
    InlineMinterval indom;
    for (unsigned int i = 0; i < size2; i++, it++)
    {
        indom = (*it).getDomain();
        indom.insertInDb(&(lowerboundsbuf[(i + 1) * dimension2]),
                         &(upperboundsbuf[(i + 1) * dimension2]),
                         &(lowerfixedbuf[(i + 1) * dimension2]),
                         &(upperfixedbuf[(i + 1) * dimension2]));
        entryidsbuf[i] = (*it).getObject().getOId().getCounter();
        entrytypesbuf[i] = static_cast<char>((*it).getObject().getOId().getType());
        LTRACE << "entry " << entryidsbuf[i] << " "
               << (OId::OIdType)entrytypesbuf[i] << " at "
               << InlineMinterval(dimension2,
                                  &(lowerboundsbuf[(i + 1) * dimension2]),
                                  &(upperboundsbuf[(i + 1) * dimension2]),
                                  &(lowerfixedbuf[(i + 1) * dimension2]),
                                  &(upperfixedbuf[(i + 1) * dimension2]));
    }

    // write the buffers in the complete buffer
    // this indirection is neccessary because of memory alignement of longs...
    memcpy(completebuffer, lowerboundsbuf, boundssize);
    delete [] lowerboundsbuf;
    memcpy(&completebuffer[boundssize], upperboundsbuf, boundssize);
    delete [] upperboundsbuf;
    memcpy(&completebuffer[boundssize * 2], lowerfixedbuf, fixessize);
    delete [] lowerfixedbuf;
    memcpy(&completebuffer[boundssize * 2 + fixessize], upperfixedbuf, fixessize);
    delete [] upperfixedbuf;
    memcpy(&completebuffer[boundssize * 2 + fixessize * 2], entryidsbuf, idssize);
    delete [] entryidsbuf;
    memcpy(&completebuffer[boundssize * 2 + fixessize * 2 + idssize], entrytypesbuf, typessize);
    delete [] entrytypesbuf;

    /*
        5 bytes tag
        1 byte  version
        1 byte  endianness
        8 bytes oid
    */
    // version + endianness + oid + size + dimension + parentoid + subtype
    brp.binaryLength = 7 + sizeof(OId::OIdCounter) + sizeof(int) + sizeof(short) +
                           sizeof(OId::OIdCounter) + sizeof(char) + completesize;
    brp.binaryData = new char[brp.binaryLength];
    memcpy(brp.binaryData, BinaryRepresentation::fileTag, 5);
    memset(&brp.binaryData[5], 1, 1);
    if (r_Endian::get_endianness() == r_Endian::r_Endian_Little)
    {
        memset(&brp.binaryData[6], 1, 1);
    }
    else
    {
        memset(&brp.binaryData[6], 0, 1);
    }
    double tempd = myOId;
    memcpy(&brp.binaryData[7], &tempd, sizeof(OId::OIdCounter));
    /*
        special
        4 bytes size
        2 bytes dimension
        8 bytes parent oid
        1 byte  subtype
        x bytes database layout
    */
    int tempi = size2;
    memcpy(&brp.binaryData[7 + sizeof(OId::OIdCounter)], &tempi, sizeof(int));
    short temps = dimension2;
    memcpy(&brp.binaryData[7 + sizeof(OId::OIdCounter) + sizeof(int)], &temps, sizeof(short));
    memcpy(&brp.binaryData[7 + sizeof(OId::OIdCounter) + sizeof(int) + sizeof(short)], &parentid2, sizeof(OId::OIdCounter));
    char tempc = subtype;
    memcpy(&brp.binaryData[7 + sizeof(OId::OIdCounter) + sizeof(int) + sizeof(short) + sizeof(OId::OIdCounter)], &tempc, sizeof(char));
    memcpy(&brp.binaryData[7 + sizeof(OId::OIdCounter) + sizeof(int) + sizeof(short) + sizeof(OId::OIdCounter) + sizeof(char)], completebuffer, completesize);

    delete [] completebuffer;

    return brp;
}

void DBHierIndex::setBinaryRepresentation(const BinaryRepresentation &brp)
{
    // This format is not efficient (but also not in use..), it should be reviewed
    // against alignment issues
    if (memcmp(brp.binaryData, BinaryRepresentation::fileTag, 5) != 0)
    {
        LERROR << "binary representation " << brp.binaryName << " - incorrect data set " << brp.binaryData;
        throw r_Error();
    }
    if (brp.binaryData[5] != 1)
    {
        LERROR << "binary representation " << brp.binaryName << " - unknown export version " << static_cast<int>(brp.binaryData[5]);
        throw r_Error();
    }
    if (brp.binaryData[6] != (r_Endian::get_endianness() == r_Endian::r_Endian_Little))
    {
        LERROR << "binary representation " << brp.binaryName << " - endianess conversion not supported";
        throw r_Error();
    }
    size_t size1;
    unsigned int dimension1;
    OId::OIdCounter parentid1;
    unsigned int tempi;
    unsigned int temps;
    char tempc;
    OId::OIdCounter tempd;

    memcpy((char *)&tempd, &brp.binaryData[7], sizeof(OId::OIdCounter));
    myOId = tempd;
    char *temp = getBinaryName();
    if (strcmp(temp, brp.binaryName) != 0)
    {
        LERROR << "binary representation " << brp.binaryName << " - expected name " << temp;
        delete [] temp;
        throw r_Error();
    }
    delete [] temp;
    temp = NULL;
    memcpy(&tempi, &brp.binaryData[7 + sizeof(OId::OIdCounter)], sizeof(int));
    size1 = tempi;
    memcpy(&temps, &brp.binaryData[7 + sizeof(OId::OIdCounter) + sizeof(int)], sizeof(short));
    dimension1 = temps;
    memcpy(&parentid1, &brp.binaryData[7 + sizeof(OId::OIdCounter) + sizeof(int) + sizeof(short)], sizeof(double));
    memcpy(&tempc, &brp.binaryData[7 + sizeof(double) + sizeof(int) + sizeof(short) + sizeof(double)], sizeof(char));
    _isNode = tempc;

    if (parentid1)
        parent = OId(parentid1);
    else
        parent = OId(0, OId::INVALID);

    // number of bytes for bounds for "size" entries and mydomain
    r_Bytes boundssize = sizeof(r_Range) * (size1 + 1) * dimension1;
    // number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixessize = sizeof(char) * (size1 + 1) * dimension1;
    // number of bytes for ids of entries
    r_Bytes idssize = sizeof(OId::OIdCounter) * size1;
    // number of bytes for types of entries
    r_Bytes typessize = sizeof(char) * size1;
    // number of bytes for the dynamic data
    r_Bytes completesize = boundssize * 2 + fixessize * 2 + idssize + typessize;

    LTRACE << "size " << size1 << " dimension " << dimension1 << " fixes "
           << fixessize << " ids " << idssize << " types " << typessize;


    auto *completebuffer = new char[completesize];
    auto *upperboundsbuf = new r_Range[boundssize];
    auto *lowerboundsbuf = new r_Range[boundssize];
    auto *upperfixedbuf = new char[fixessize];
    auto *lowerfixedbuf = new char[fixessize];
    auto *entryidsbuf = new OId::OIdCounter[idssize];
    auto *entrytypesbuf = new char[typessize];
    memcpy(completebuffer,
           &brp.binaryData[7 + sizeof(OId::OIdCounter) + sizeof(int) +
                               sizeof(short) + sizeof(OId::OIdCounter) + sizeof(char)],
           completesize);

    // all dynamic data is in completebuffer
    // put that stuff in the correct buffers
    memcpy(lowerboundsbuf, completebuffer, boundssize);
    memcpy(upperboundsbuf, &completebuffer[boundssize], boundssize);
    memcpy(lowerfixedbuf, &completebuffer[boundssize * 2], fixessize);
    memcpy(upperfixedbuf, &completebuffer[boundssize * 2 + fixessize], fixessize);
    memcpy(entryidsbuf, &completebuffer[boundssize * 2 + fixessize * 2], idssize);
    memcpy(entrytypesbuf, &completebuffer[boundssize * 2 + fixessize * 2 + idssize], typessize);
    // all dynamic data is in its buffer
    delete[] completebuffer;
    completebuffer = nullptr;
    unsigned int i = 0;
    // rebuild the attributes from the buffers
    myDomain = InlineMinterval(dimension1, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[i * dimension1]));
    LTRACE << "domain " << myDomain << " constructed from "
           << InlineMinterval(dimension1, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    KeyObject theKey = KeyObject(DBObjectId(), myDomain);
    for (i = 0; i < size1; i++)
    {
        theKey.setDomain(InlineMinterval(dimension1,
                                         &(lowerboundsbuf[(i + 1) * dimension1]),
                                         &(upperboundsbuf[(i + 1) * dimension1]),
                                         &(lowerfixedbuf[(i + 1) * dimension1]),
                                         &(upperfixedbuf[(i + 1) * dimension1])));
        theKey.setObject(
            OId(entryidsbuf[i], static_cast<OId::OIdType>(entrytypesbuf[i])));
        myKeyObjects.push_back(theKey);
        LTRACE << "entry " << entryidsbuf[i] << " "
               << (OId::OIdType)entrytypesbuf[i] << " at "
               << InlineMinterval(dimension1,
                                  &(lowerboundsbuf[(i + 1) * dimension1]),
                                  &(upperboundsbuf[(i + 1) * dimension1]),
                                  &(lowerfixedbuf[(i + 1) * dimension1]),
                                  &(upperfixedbuf[(i + 1) * dimension1]));
    }

    delete [] upperboundsbuf;
    delete [] lowerboundsbuf;
    delete [] upperfixedbuf;
    delete [] lowerfixedbuf;
    delete [] entryidsbuf;
    delete [] entrytypesbuf;
    _isInDatabase = true;
    _isPersistent = true;
    _isModified = true;
    currentDbRows = 1;
}


std::unique_ptr<char[]> 
DBHierIndex::writeToBlobBuffer(int &completeSizeOut, long long &parentid)
{
    // (0) --- prepare variables
    auto dimension = myDomain.dimension();
    auto numEntries = getSize();
    parentid = parent.getType() == OId::INVALID
             ? 0
             : static_cast<long long>(parent);

    // (1) -- prepare buffer
    static const auto header = BLOB_FORMAT_V3_HEADER_MAGIC;
    static const auto headerSize = BLOB_FORMAT_V3_HEADER_SIZE;
    static const auto idSize = sizeof(OId::OIdCounter);
    static const auto boundSize = sizeof(r_Range);
    static const auto blobFormat = BLOB_FORMAT_V3;
    
    // number of bytes for ids of entries
    r_Bytes idsSize = idSize * numEntries;
    // number of bytes for bounds for "size" entries and mydomain
    r_Bytes boundsSize = boundSize * (numEntries + 1) * dimension;
    // number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixesSize = sizeof(char) * (numEntries + 1) * dimension;
    // number of bytes for types of entries
    r_Bytes typesSize = sizeof(char) * numEntries;
    // number of bytes for the dynamic data
    auto completeSize =
        headerSize + boundsSize * 2 + fixesSize * 2 + idsSize + typesSize;
    completeSizeOut = static_cast<int>(completeSize);
    
    LTRACE << "inserting index blob, format=" << blobFormat
           << " complete=" << completeSize << " bounds=" << boundsSize
           << " fixes=" << fixesSize << " ids=" << idsSize
           << " types=" << typesSize << ", entries=" << numEntries
           << " dimension=" << dimension;
    
    /*
      Node buffer format, N = number of entries:
      
      magic header
      N * dimension lower bounds
      N * dimension upper bounds
      N * dimension lower fixes
      N * dimension upper fixes
      N * oids
      N * oid types
      
     */
    
    std::unique_ptr<char[]> completeBufPtr;
    completeBufPtr.reset(new char[completeSize]);
    char *completeBuf = completeBufPtr.get();
    *reinterpret_cast<long long*>(completeBuf) = header;
    completeBuf += headerSize;
    
    auto *lowerBoundsBuf = reinterpret_cast<r_Range*>(
                           &completeBuf[0]);
    auto *upperBoundsBuf = reinterpret_cast<r_Range*>(
                           &completeBuf[boundsSize]);
    char *lowerFixedBuf  = &completeBuf[boundsSize * 2];
    char *upperFixedBuf  = &completeBuf[boundsSize * 2 + fixesSize];
    auto *entryIdsBuf    = reinterpret_cast<OId::OIdCounter*>(
                           &completeBuf[boundsSize * 2 + fixesSize * 2]);
    char *entryTypesBuf  = &completeBuf[boundsSize * 2 + fixesSize * 2 + idsSize];    
    
    // populate the buffers with data
    *reinterpret_cast<long long *>(completeBuf) = header;
    myDomain.insertInDb(&(lowerBoundsBuf[0]), &(upperBoundsBuf[0]),
                        &(lowerFixedBuf[0]),  &(upperFixedBuf[0]));
    
    auto it = myKeyObjects.begin();
    InlineMinterval indom;
    for (size_t i = 0; i < numEntries; ++i, ++it)
    {
        indom = (*it).getDomain();
        indom.insertInDb(&(lowerBoundsBuf[(i + 1) * dimension]),
                         &(upperBoundsBuf[(i + 1) * dimension]),
                          &(lowerFixedBuf[(i + 1) * dimension]),
                          &(upperFixedBuf[(i + 1) * dimension]));
        entryIdsBuf[i]   = (*it).getObject().getOId().getCounter();
        entryTypesBuf[i] = static_cast<char>((*it).getObject().getOId().getType());
    }
    
    return completeBufPtr;
}

void DBHierIndex::readFromBlobBuffer(r_Bytes numEntries, r_Dimension dimension, 
                                     long long parentOid, const char *blobBuffer, 
                                     r_Bytes blobSize)
{
    parent = parentOid ? OId(parentOid) : OId(0, OId::INVALID);

    static const size_t newIdSize = sizeof(OId::OIdCounter);
    size_t idSize = newIdSize;
    static const size_t newBoundSize = sizeof(r_Range);
    size_t boundSize = newBoundSize;

    int blobFormat{};
    r_Bytes headerSize{};
    if (blobBuffer[0] == BLOB_FORMAT_V1_HEADER_MAGIC)
    {
        // old format
        blobFormat = BLOB_FORMAT_V1;
        idSize = sizeof(int);
        boundSize = sizeof(int);
        headerSize = BLOB_FORMAT_V1_HEADER_SIZE;
    }
    else
    {
        // new format (v2 or v3)
        headerSize = BLOB_FORMAT_V2_HEADER_SIZE;
        auto header = *reinterpret_cast<const long long *>(&blobBuffer[0]);
        blobFormat = header == BLOB_FORMAT_V2_HEADER_MAGIC ? BLOB_FORMAT_V2
                     : BLOB_FORMAT_V3;
    }
    r_Bytes idsSize = idSize * numEntries;
    r_Bytes boundsSize = boundSize * (numEntries + 1) * dimension;
    r_Bytes newBoundsSize = newBoundSize * (numEntries + 1) * dimension;
    // number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixesSize = sizeof(char) * (numEntries + 1) * dimension;
    // number of bytes for types of entries
    r_Bytes typesSize = sizeof(char) * numEntries;
    // number of bytes for the dynamic data
    r_Bytes completeSize =
        headerSize + boundsSize * 2 + fixesSize * 2 + idsSize + typesSize;

    LTRACE << "reading index blob, format=" << blobFormat
           << " complete=" << completeSize << " bounds=" << boundsSize
           << " fixes=" << fixesSize << " ids=" << idsSize
           << " types=" << typesSize << ", entries=" << numEntries
           << " dimension=" << dimension;

    if (completeSize != blobSize)  // this because I don't trust computations
    {
        LTRACE << "BLOB (" << myOId.getCounter() << ") read: completeSize=" 
               << completeSize << ", but blobSize=" << blobSize;
        throw r_Error(r_Error::r_Error_LimitsMismatch);
    }
    
    /*
      Node buffer format, N = number of entries:
      
      magic header
      N * dimension lower bounds
      N * dimension upper bounds
      N * dimension lower fixes
      N * dimension upper fixes
      N * oids
      N * oid types
      
     */

    const char *completeBuf = blobBuffer;
    completeBuf += headerSize;

    const char *lowerBoundsBuf = &completeBuf[0];
    const char *upperBoundsBuf = &completeBuf[boundsSize];
    const char *lowerFixedBuf  = &completeBuf[boundsSize * 2];
    const char *upperFixedBuf  = &completeBuf[boundsSize * 2 + fixesSize];
    const char *entryIdsBuf    = &completeBuf[boundsSize * 2 + fixesSize * 2];
    const char *entryTypesBuf  = &completeBuf[boundsSize * 2 + fixesSize * 2 + idsSize];

    // (4) --- copy data into buffers

#define GET_BOUND(buf) \
    (blobFormat <= BLOB_FORMAT_V2) \
    ? static_cast<r_Range>(*reinterpret_cast<int*>(buf)) \
    : *reinterpret_cast<r_Range*>(buf)

    auto lowerBounds = std::unique_ptr<r_Range[]>(new r_Range[newBoundsSize]);
    auto upperBounds = std::unique_ptr<r_Range[]>(new r_Range[newBoundsSize]);

    char buf[sizeof(r_Range)];
    for (size_t i = 0; i < (numEntries + 1) * dimension; ++i)
    {
        memcpy(buf, lowerBoundsBuf, boundSize);
        lowerBounds[i] = GET_BOUND(buf);
        lowerBoundsBuf += boundSize;
        memcpy(buf, upperBoundsBuf, boundSize);
        upperBounds[i] = GET_BOUND(buf);
        upperBoundsBuf += boundSize;
    }

    // rebuild the attributes from the buffers
    myDomain = InlineMinterval(dimension,
                               &lowerBounds[0], &upperBounds[0], 
                               lowerFixedBuf, upperFixedBuf);
    KeyObject theKey = KeyObject(DBObjectId(), myDomain);
    for (r_Bytes i = 0; i < numEntries; ++i)
    {
        lowerFixedBuf += dimension;
        upperFixedBuf += dimension;
        theKey.setDomain(InlineMinterval(dimension,
                                         &lowerBounds[(i + 1) * dimension], 
                                         &upperBounds[(i + 1) * dimension],
                                         lowerFixedBuf, upperFixedBuf));

        memcpy(buf, entryIdsBuf, idSize);
        auto entryId = (blobFormat == BLOB_FORMAT_V1)
                       ? static_cast<OId::OIdCounter>(*reinterpret_cast<unsigned int *>(buf))
                       : *reinterpret_cast<OId::OIdCounter *>(buf);
        entryIdsBuf += idSize;
        auto entryType = static_cast<OId::OIdType>(entryTypesBuf[i]);

        theKey.setObject(OId(entryId, entryType));
        myKeyObjects.push_back(theKey);
    }
}
