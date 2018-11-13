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
#include <cstring>

#include "mymalloc/mymalloc.h"
#include "hierindex.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/dbref.hh"
#include "reladminif/lists.h"
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "relblobif/blobtile.hh"
#include "indexmgr/keyobject.hh"
#include "storagemgr/sstoragelayout.hh"
#include "raslib/endian.hh"
#include "debug.hh"
#include <logging.hh>

DBHierIndex::DBHierIndex(const OId& id)
    :   HierIndexDS(id),
        parent(0),
        _isNode(false),
        maxSize(0),
        myDomain(static_cast<r_Dimension>(0)),
        currentDbRows(0)
{
    if (id.getType() == OId::MDDHIERIXOID)
    {
        readFromDb();
    }
    maxSize = DBHierIndex::getOptimalSize(getDimension());
    myKeyObjects.reserve(maxSize);
}

DBHierIndex::DBHierIndex(r_Dimension dim, bool isNODE, bool makePersistent)
    :   HierIndexDS(),
        parent(0),
        _isNode(isNODE),
        maxSize(0),
        myDomain(dim),
        currentDbRows(-1)
{
    objecttype = OId::MDDHIERIXOID;
    if (makePersistent)
    {
        setPersistent(true);
    }

    maxSize = getOptimalSize(dim);
    myKeyObjects.reserve(maxSize);
    setCached(true);
}

IndexDS*
DBHierIndex::getNewInstance() const
{
    return new DBHierIndex(getDimension(), !isLeaf(), true);
}

OId::OIdPrimitive
DBHierIndex::getIdentifier() const
{
    return myOId;
}

bool
DBHierIndex::removeObject(const KeyObject& entry)
{
    bool found = false;
    unsigned int pos = 0;
    OId oid(entry.getObject().getOId());
    for (KeyObjectVector::iterator i = myKeyObjects.begin(); i != myKeyObjects.end(); i++)
    {
        LTRACE << "remove object in vector at pos " << pos << " of " << myKeyObjects.size();
        if (oid == (*i).getObject().getOId())
        {
            found = true;
            myKeyObjects.erase(i);
            setModified();
            break;
        }
        else
        {
            LTRACE << "did not match " << oid << " with " << *i;
        }
    }

    return found;
}

bool
DBHierIndex::removeObject(unsigned int pos)
{
    bool found = false;
    if (pos <= myKeyObjects.size())
    {
        found = true;
        myKeyObjects.erase(myKeyObjects.begin() + pos);
        setModified();
    }

    return found;
}


void
DBHierIndex::insertObject(const KeyObject& theKey, unsigned int pos)
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
    LTRACE << "after inserting object now have " << myKeyObjects.size() << " objects in key object vector.";
    setModified();
}

void
DBHierIndex::setObjectDomain(const r_Minterval& dom, unsigned int pos)
{
    myKeyObjects[pos].setDomain(dom);
    //might be unneccessary/harmfull, check later
    extendCoveredDomain(dom);

    //setModified(); done in domain extension
    if (!isLeaf())
    {
        DBHierIndexId t(myKeyObjects[pos].getObject());
        t->setAssignedDomain(dom);
    }
}

void
DBHierIndex::setObject(const KeyObject& theKey, unsigned int pos)
{
    myKeyObjects[pos] = theKey;
    setModified();
    if (!isLeaf())
    {
        DBHierIndexId(theKey.getObject())->setParent(this);
    }
}

r_Minterval
DBHierIndex::getCoveredDomain() const
{
    LTRACE << "DBHierIndex::getCoveredDomain( " << myOId << ") -> " << myDomain;
    return myDomain;
}

r_Dimension
DBHierIndex::getDimension() const
{
    LTRACE << "DBHierIndex::getDimension( " << myOId << ") -> " << myDomain.dimension();
    return myDomain.dimension();
}

r_Bytes
DBHierIndex::getTotalStorageSize() const
{
    r_Bytes sz = 0;

    for (KeyObjectVector::const_iterator i = myKeyObjects.begin(); i != myKeyObjects.end(); i++)
    {
        sz = sz + (static_cast<DBObject*>(ObjectBroker::getObjectByOId(i->getObject().getOId())))->getTotalStorageSize();
    }

    return sz;
}

bool
DBHierIndex::isValid() const
{
    bool valid = true;
    //may not be unsigned int (r_Area) because of error check
    int area = 0;
    if (!isLeaf())
    {
        area = myDomain.cell_count();
        DBHierIndexId tempIx;
        LTRACE << "inspecting " << myKeyObjects.size() << " objects in key object vector.";
        for (KeyObjectVector::const_iterator i = myKeyObjects.begin(); i != myKeyObjects.end(); i++)
        {
            if (myDomain.covers((*i).getDomain()))
            {
                //ok
                area = area - static_cast<int>((*i).getDomain().cell_count());
            }
            else
            {
                if (myDomain == (*i).getDomain())
                {
                    //ok
                    area = area - static_cast<int>((*i).getDomain().cell_count());
                    tempIx = DBHierIndexId((*i).getObject());
                    if (!tempIx->isValid())
                    {
                        valid = false;
                        break;
                    }
                }
                else
                {
                    LTRACE << "isValid() " << myOId << " key does not cover domain: myDomain " << myDomain << " key " << *i;
                    valid = false;
                    break;
                }
            }
        }
        if (valid)
        {
            if (area < 0)
            {
                LTRACE << "isValid() " << myOId << " there are double entries";
                valid = false;
            }
        }
    }
    else
    {
        area = myDomain.cell_count();
        valid = true;
        for (KeyObjectVector::const_iterator i = myKeyObjects.begin(); i != myKeyObjects.end(); i++)
        {
            if (myDomain.intersects_with((*i).getDomain()))
            {
                //ok
                area = area - static_cast<int>((*i).getDomain().create_intersection(myDomain).cell_count());
            }
            else
            {
                LTRACE << "isValid() " << myOId << " key does not intersect domain: myDomain " << myDomain << " key " << *i;
                valid = false;
                break;
            }
        }
        if (!valid)
        {
            if (area < 0)
            {
                LTRACE << "isValid() " << myOId << " there are double entries";
                valid = false;
            }
        }
    }

    return valid;
}

void
DBHierIndex::printStatus(unsigned int level, std::ostream& stream) const
{
    DBObjectId t;
    char* indent = new char[level * 2 + 1];
    for (unsigned int j = 0; j < level * 2 ; j++)
    {
        indent[j] = ' ';
    }
    indent[level * 2] = '\0';

    stream << indent << "DBHierIndex ";
    if (isRoot())
    {
        stream << "is Root ";
    }
    else
    {
        stream << "Parent " << parent << " ";
    }
    if (isLeaf())
    {
        stream << "Leaf ";
    }
    else
    {
        stream << "Node ";
    }
    DBObject::printStatus(level, stream);
    stream << " size " << myKeyObjects.size() << " domain " << myDomain << std::endl;
    int count = 0;
    LTRACE << "inspecting " << myKeyObjects.size() << " objects in key object vector.";
    for (KeyObjectVector::const_iterator i = myKeyObjects.begin(); i != myKeyObjects.end(); i++)
    {
        stream << indent << " entry #" << count << " is " << *i << std::endl;
        if (!isLeaf())
        {
            t = DBObjectId((*i).getObject());
            if (t.is_null())
            {
                stream << indent << " entry is null";
            }
            else
            {
                t->printStatus(level + 1, stream);
            }
        }
        stream << std::endl;
        count++;
    }
    delete[] indent;
}

unsigned int
DBHierIndex::getSize() const
{
    LTRACE << "getSize() " << myOId << " " << myKeyObjects.size();
    return myKeyObjects.size();
}

bool
DBHierIndex::isUnderFull() const
{
    //redistribute in srptindexlogic has to be checked first before any other return value may be assigned
    LTRACE << "DBHierIndex::isUnderFull() -> false";
    return false;
}

bool
DBHierIndex::isOverFull() const
{
    bool retval = false;
    if (getSize() >= maxSize)
    {
        retval = true;
    }

    LTRACE << "isOverFull() " << myOId << " maxSize " << maxSize << " size " << getSize() << " retval " << retval;
    return retval;
}

unsigned int
DBHierIndex::getOptimalSize(r_Dimension dim)
{
    unsigned int retval = 0;
    //BLOCKSIZE
    unsigned int blocksize = 0;
    unsigned int useablespace = 0;
    //dimension * (upperbound + upperfixed + lowerbound + lowerfixed) + entryid + entryoidtype
    unsigned int oneentry = dim * (sizeof(r_Range) * 2 + sizeof(char) * 2) + sizeof(OId::OIdCounter) + sizeof(long long);

#ifdef BASEDB_ORACLE
    blocksize = 2048;
    //BLOCKSIZE - (BLOCK OVERHEAD + ROW OVERHEAD + 1 * largerow + number(15,0) + short)
    useablespace = blocksize - (130 + 3 + 1 * 3 + 12 + 2);
#else
#ifdef BASEDB_DB2
    blocksize = 4096;
    //from the manual
    useablespace = 3990;
#else
#ifdef BASEDB_INFORMIX
    blocksize = 4096;
    //from the manual
    useablespace = 3990;
#else
#ifdef BASEDB_PGSQL
    blocksize = 8192;   // default only!!!;
    useablespace = 7000;    // no indication for any less space available, but to be sure we go a little lower -- PB 2005-jan-10
#else
#ifdef BASEDB_SQLITE
    blocksize = 8192;   // default only!!!;
    useablespace = 7000;    // no indication for any less space available, but to be sure we go a little lower -- PB 2005-jan-10
#else
#error "BASEDB not defined! please check for BASEDB_XXX define in Makefile.inc!"
#endif // pgsql
#endif // informix
#endif // db2
#endif // oracle
#endif

    //remove mydomain size
    useablespace = useablespace - dim * (sizeof(r_Range) * 2 + sizeof(char) * 2);
    //minimum size is 8-lucky guess(good for 1,2,3,4 dimensions)
    retval = std::max(static_cast<unsigned int>(8), useablespace / oneentry);
    if (StorageLayout::DefaultIndexSize != 0)
    {
        retval = StorageLayout::DefaultIndexSize;
    }

    LTRACE << "getOptimalSize(" << dim << ") maxSize " << retval;
    return retval;
}

unsigned int
DBHierIndex::getOptimalSize() const
{
    LTRACE << "DBHierIndex::getOptimalSize() -> " << maxSize;
    return maxSize;
}

r_Minterval
DBHierIndex::getAssignedDomain() const
{
    LTRACE << "DBHierIndex::getAssignedDomain() -> " << myDomain;
    return myDomain;
}

void
DBHierIndex::setAssignedDomain(const r_Minterval& newDomain)
{
    myDomain = newDomain;
    setModified();
}

void
DBHierIndex::extendCoveredDomain(const r_Minterval& newTilesExtents)
{
    myDomain.closure_with(newTilesExtents);
    setModified();
}

void
DBHierIndex::setParent(const HierIndexDS* newPa)
{
    if (static_cast<OId::OIdPrimitive>(parent) != newPa->getIdentifier())
    {
        parent = newPa->getIdentifier();
        setModified();
    }
}

HierIndexDS*
DBHierIndex::getParent() const
{
    LTRACE << "getParent() const " << myOId << " " << parent << " " << parent;
    DBHierIndexId t(parent);

    return static_cast<HierIndexDS*>(t);
}

bool
DBHierIndex::isRoot() const
{
    LTRACE << "isRoot() const " << myOId << " " << (int)(parent.getType() == OId::INVALID);
    return (parent.getType() == OId::INVALID);
}

bool
DBHierIndex::isLeaf() const
{
    LTRACE << "isLeaf() const " << myOId << " " << (int)(!_isNode);
    //LDEBUG << "DBHierIndex::isLeaf() -> " <<  !_isNode;
    return !_isNode;
}

void
DBHierIndex::setIsNode(bool isNodea)
{
    LTRACE << "setIsNode(" << isNodea << ") " << myOId << " was " << _isNode;
    _isNode = isNodea;
}

void
DBHierIndex::freeDS()
{
    LTRACE << "DBHierIndex::freeDS()";
    setPersistent(false);
}
bool
DBHierIndex::isSameAs(const IndexDS* other) const
{
    bool result = false;
    if (other->isPersistent())
        if (myOId == other->getIdentifier())
        {
            result = true;
        }

    return result;
}


double
DBHierIndex::getOccupancy() const
{
    LTRACE << "DBHierIndex::getOccupancy() NOT IMPLEMENTED";
    return 0;
}

const KeyObject&
DBHierIndex::getObject(unsigned int pos) const
{
    LTRACE << "getObject(" << pos << ") " << myOId << " " << myKeyObjects[pos];
    return myKeyObjects[pos];
}

void
DBHierIndex::getObjects(KeyObjectVector& objs) const
{
    for (KeyObjectVector::const_iterator keyIt = myKeyObjects.begin(); keyIt != myKeyObjects.end(); keyIt++)
    {
        objs.push_back(*keyIt);
    }
}

r_Minterval
DBHierIndex::getObjectDomain(unsigned int pos) const
{
    LTRACE << "getObjectDomain(" <<  pos << ") " << myOId << " " << myKeyObjects[pos];
    return myKeyObjects[pos].getDomain();
}

unsigned int
DBHierIndex::getHeight() const
{
    LTRACE << "DBHierIndex::getHeight() -> " << getHeightToLeaf();
    return getHeightToLeaf();
}

unsigned int
DBHierIndex::getHeightOfTree() const
{
    unsigned int retval = getHeightToLeaf() + getHeightToRoot();
    LTRACE << "getHeightOfTree() const " << myOId << " " << retval;
    return retval;
}

unsigned int
DBHierIndex::getHeightToRoot() const
{
    unsigned int retval = 0;
    if (isRoot())
    {
        retval = 0;
    }
    else
    {
        DBHierIndexId t(parent);
        const DBHierIndex* tp = static_cast<DBHierIndex*>(t.ptr());
        retval = tp->getHeightToRoot() + 1;
    }

    LTRACE << "getHeightToRoot() const " << myOId << " " << retval;
    return retval;
}

unsigned int
DBHierIndex::getHeightToLeaf() const
{

    unsigned int retval = 0;
    if (isLeaf())
    {
        retval = 0;
    }
    else
    {
        DBHierIndexId t(parent);
        const DBHierIndex* tp = static_cast<DBHierIndex*>(t.ptr());
        retval = tp->getHeightToLeaf() + 1;
    }

    LTRACE << "getHeightToLeaf() const " << myOId << " " << retval;
    return retval;
}

unsigned int
DBHierIndex::getTotalLeafCount() const
{
    unsigned int retval = 0;
    if (!isLeaf())
    {
        //i am not a leaf
        if (DBHierIndexId(myKeyObjects.begin()->getObject())->isLeaf())
        {
            //i contain only leafs, so i return the number of entries i contain
            retval = getSize();
        }
        else
        {
            //i contain only nodes, so i ask my children how many leafs there are
            for (KeyObjectVector::const_iterator keyIt = myKeyObjects.begin(); keyIt != myKeyObjects.end(); keyIt++)
            {
                DBHierIndexId accessedIx((*keyIt).getObject());
                retval = retval + accessedIx->getTotalLeafCount();
            }
        }
    }
    else
    {
        retval = 1;
    }

    return retval;
}

unsigned int
DBHierIndex::getTotalNodeCount() const
{
    unsigned int retval = 0;
    if (!isLeaf())
    {
        //i am not a leaf
        if (DBHierIndexId(myKeyObjects.begin()->getObject())->isLeaf())
        {
            //i contain only nodes
            //i add the nodes i contain
            retval = getSize();
            //i add the nodes my children contain
            for (KeyObjectVector::const_iterator keyIt = myKeyObjects.begin(); keyIt != myKeyObjects.end(); keyIt++)
            {
                DBHierIndexId accessedIx((*keyIt).getObject());
                retval = retval + accessedIx->getTotalNodeCount();
            }
        }
    }
    //else : a leaf does not contain nodes

    return retval;
}

unsigned int
DBHierIndex::getTotalEntryCount() const
{
    unsigned int retval = 0;
    if (isLeaf())
    {
        //i contain only entries
        //i return the number of entries i contain
        retval = getSize();
    }
    else
    {
        //i contain only nodes, no entries
        //i ask my children how many entries they contain
        for (KeyObjectVector::const_iterator keyIt = myKeyObjects.begin(); keyIt != myKeyObjects.end(); keyIt++)
        {
            DBHierIndexId accessedIx((*keyIt).getObject());
            retval = retval + accessedIx->getTotalEntryCount();
        }
    }

    return retval;
}

void
DBHierIndex::destroy()
{
    LTRACE << "DBObject::destroy()";
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
BinaryRepresentation
DBHierIndex::getBinaryRepresentation() const
{
    BinaryRepresentation brp;
    brp.binaryName = getBinaryName();
    brp.binaryData = NULL;
    brp.binaryLength = 0;
    unsigned int dimension2 = myDomain.dimension();
    size_t size2 = myKeyObjects.size();
    short subtype = _isNode;
    long long parentid2 = 0;

    if (parent.getType() == OId::INVALID)
    {
        parentid2 = 0;
    }
    else
    {
        parentid2 = parent;
    }

    //INSERTINTO

    //number of bytes for bounds for "size" entries and mydomain
    r_Bytes boundssize = sizeof(r_Range) * (size2 + 1) * dimension2;
    //number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixessize = sizeof(char) * (size2 + 1) * dimension2;
    //number of bytes for ids of entries
    r_Bytes idssize = sizeof(OId::OIdCounter) * size2;
    //number of bytes for types of entries
    r_Bytes typessize = sizeof(char) * size2;
    //number of bytes for the dynamic data
    r_Bytes completesize = boundssize * 2 + fixessize * 2 + idssize + typessize;

    char* completebuffer = new char[completesize];
    r_Range* upperboundsbuf = new r_Range[boundssize];
    r_Range* lowerboundsbuf = new r_Range[boundssize];
    char* upperfixedbuf = new char[fixessize];
    char* lowerfixedbuf = new char[fixessize];
    OId::OIdCounter* entryidsbuf = new OId::OIdCounter[idssize];
    char* entrytypesbuf = new char[typessize];

    LTRACE << "complete " << completesize << " bounds " << boundssize << " fixes " << fixessize << " ids " << idssize << " types " << typessize;

    //counter which keeps track of the bytes that have been written to the db
    r_Bytes byteswritten = 0;
    //counter which keeps track of the bytes that have to be written to the db
    r_Bytes bytestowrite = 0;

    myDomain.insertInDb(&(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    LTRACE << "domain " << myDomain << " stored as " << InlineMinterval(dimension2, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    //populate the buffers with data
    KeyObjectVector::const_iterator it = myKeyObjects.begin();
    InlineMinterval indom;
    for (unsigned int i = 0; i < size2; i++, it++)
    {
        indom = (*it).getDomain();
        indom.insertInDb(&(lowerboundsbuf[(i + 1)*dimension2]), &(upperboundsbuf[(i + 1)*dimension2]), &(lowerfixedbuf[(i + 1)*dimension2]), &(upperfixedbuf[(i + 1)*dimension2]));
        entryidsbuf[i] = (*it).getObject().getOId().getCounter();
        entrytypesbuf[i] = static_cast<char>((*it).getObject().getOId().getType());
        LTRACE << "entry " << entryidsbuf[i] << " " << (OId::OIdType)entrytypesbuf[i] << " at " << InlineMinterval(dimension2, &(lowerboundsbuf[(i + 1)*dimension2]), &(upperboundsbuf[(i + 1)*dimension2]), &(lowerfixedbuf[(i + 1)*dimension2]), &(upperfixedbuf[(i + 1)*dimension2]));
    }

    //write the buffers in the complete buffer
    //this indirection is neccessary because of memory alignement of longs...
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
    //version + endianness + oid + size + dimension + parentoid + subtype
    brp.binaryLength = 7 + sizeof(OId::OIdCounter) + sizeof(int) + sizeof(short) + sizeof(OId::OIdCounter) + sizeof(char) + completesize;
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

void
DBHierIndex::setBinaryRepresentation(const BinaryRepresentation& brp)
{
    // This format is not efficient (but also not in use..), it should be reviewed against alignment issues
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

    memcpy((char*)&tempd, &brp.binaryData[7], sizeof(OId::OIdCounter));
    myOId = tempd;
    char* temp = getBinaryName();
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
    {
        parent = OId(parentid1);
    }
    else
    {
        parent = OId(0, OId::INVALID);
    }

    //number of bytes for bounds for "size" entries and mydomain
    r_Bytes boundssize = sizeof(r_Range) * (size1 + 1) * dimension1;
    //number of bytes for fixes for "size" entries and mydomain
    r_Bytes fixessize = sizeof(char) * (size1 + 1) * dimension1;
    //number of bytes for ids of entries
    r_Bytes idssize = sizeof(OId::OIdCounter) * size1;
    //number of bytes for types of entries
    r_Bytes typessize = sizeof(char) * size1;
    //number of bytes for the dynamic data
    r_Bytes completesize = boundssize * 2 + fixessize * 2 + idssize + typessize;

    LTRACE << "size " << size1 << " dimension " << dimension1 << " fixes " << fixessize << " ids " << idssize << " types " << typessize;

    char* completebuffer = new char[completesize];
    r_Range* upperboundsbuf = new r_Range[boundssize];
    r_Range* lowerboundsbuf = new r_Range[boundssize];
    char* upperfixedbuf = new char[fixessize];
    char* lowerfixedbuf = new char[fixessize];
    OId::OIdCounter* entryidsbuf = new OId::OIdCounter[idssize];
    char* entrytypesbuf = new char[typessize];
    memcpy(completebuffer, &brp.binaryData[7 + sizeof(OId::OIdCounter) + sizeof(int) + sizeof(short) + sizeof(OId::OIdCounter) + sizeof(char)], completesize);

    //all dynamic data is in completebuffer
    //put that stuff in the correct buffers
    memcpy(lowerboundsbuf, completebuffer, boundssize);
    memcpy(upperboundsbuf, &completebuffer[boundssize], boundssize);
    memcpy(lowerfixedbuf, &completebuffer[boundssize * 2], fixessize);
    memcpy(upperfixedbuf, &completebuffer[boundssize * 2 + fixessize], fixessize);
    memcpy(entryidsbuf, &completebuffer[boundssize * 2 + fixessize * 2], idssize);
    memcpy(entrytypesbuf, &completebuffer[boundssize * 2 + fixessize * 2 + idssize], typessize);
    //all dynamic data is in its buffer
    delete [] completebuffer;
    completebuffer = NULL;
    unsigned int i = 0;
    //rebuild the attributes from the buffers
    myDomain = InlineMinterval(dimension1, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[i * dimension1]));
    LTRACE << "domain " << myDomain << " constructed from " << InlineMinterval(dimension1, &(lowerboundsbuf[0]), &(upperboundsbuf[0]), &(lowerfixedbuf[0]), &(upperfixedbuf[0]));
    KeyObject theKey = KeyObject(DBObjectId(), myDomain);
    for (i = 0; i < size1; i++)
    {
        theKey.setDomain(InlineMinterval(dimension1, &(lowerboundsbuf[(i + 1)*dimension1]), &(upperboundsbuf[(i + 1)*dimension1]), &(lowerfixedbuf[(i + 1)*dimension1]), &(upperfixedbuf[(i + 1)*dimension1])));
        theKey.setObject(OId(entryidsbuf[i], static_cast<OId::OIdType>(entrytypesbuf[i])));
        myKeyObjects.push_back(theKey);
        LTRACE << "entry " << entryidsbuf[i] << " " << (OId::OIdType)entrytypesbuf[i] << " at " << InlineMinterval(dimension1, &(lowerboundsbuf[(i + 1)*dimension1]), &(upperboundsbuf[(i + 1)*dimension1]), &(lowerfixedbuf[(i + 1)*dimension1]), &(upperfixedbuf[(i + 1)*dimension1]));
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

