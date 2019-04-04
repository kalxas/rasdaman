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
 * C++ part (i.e., RDBMS independent) of the RC index adaptor.
 *
 *
 * COMMENTS:
 *   uses embedded SQL
 *
 ************************************************************/

#include "config.h"
#include "mymalloc/mymalloc.h"
#include "dbrcindexds.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/dbref.hh"
#include "reladminif/lists.h"
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "relblobif/blobtile.hh"
#include "indexmgr/keyobject.hh"
#include "storagemgr/sstoragelayout.hh"
#include <logging.hh>

DBRCIndexDS::DBRCIndexDS(const OId &id)
    :   IndexDS(id),
        myBaseCounter(0),
        myBaseOIdType(OId::INVALID),
        mySize(0),
        myDomain(0),
        currentDbRows(0)
{
    objecttype = OId::MDDRCIXOID;
    readFromDb();
}

DBRCIndexDS::DBRCIndexDS(const r_Minterval &definedDomain, unsigned int size, OId::OIdType theEntryType)
    :   IndexDS(),
        myBaseCounter(0),
        myBaseOIdType(theEntryType),
        mySize(size),
        myDomain(definedDomain),
        currentDbRows(-1)
{
    objecttype = OId::MDDRCIXOID;
    OId t;
    OId::allocateOId(t, myBaseOIdType, mySize);
    myBaseCounter = t.getCounter();
    LTRACE << "base counter " << myBaseCounter;
    setPersistent(true);
    setCached(true);
}

IndexDS *
DBRCIndexDS::getNewInstance() const
{
    LERROR << "DBRCIndexDS::getNewInstance() not suported";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

OId::OIdPrimitive
DBRCIndexDS::getIdentifier() const
{
    return myOId;
}

bool
DBRCIndexDS::removeObject(const KeyObject &entry)
{
    LERROR << "DBRCIndexDS::removeObject(" << entry << ") not suported";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

bool
DBRCIndexDS::removeObject(unsigned int pos)
{
    LERROR << "DBRCIndexDS::removeObject(" << pos << ") not suported";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}


void
DBRCIndexDS::insertObject(const KeyObject &theKey, unsigned int pos)
{
    LERROR << "DBRCIndexDS::insertObject(" << theKey << ", " << pos << ") not suported";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

void
DBRCIndexDS::setObjectDomain(const r_Minterval &dom, unsigned int pos)
{
    LERROR << "DBRCIndexDS::setObjectDomain(" << dom << ", " << pos << ") not suported";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

void
DBRCIndexDS::setObject(const KeyObject &theKey, unsigned int pos)
{
    LERROR << "DBRCIndexDS::setObject(" << theKey << ", " << pos << ") not suported";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

r_Minterval
DBRCIndexDS::getCoveredDomain() const
{
    LTRACE << "getCoveredDomain() const " << myOId << " " << myDomain;
    return myDomain;
}

r_Dimension
DBRCIndexDS::getDimension() const
{
    LTRACE << "getDimension() const " << myOId << " " << myDomain.dimension();
    return myDomain.dimension();
}

r_Bytes
DBRCIndexDS::getTotalStorageSize() const
{
    r_Bytes sz = 0;

    return sz;
}

bool
DBRCIndexDS::isValid() const
{
    return true;
}

void
DBRCIndexDS::printStatus(unsigned int level, std::ostream &stream) const
{
    char *indent = new char[level * 2 + 1];
    for (unsigned int j = 0; j < level * 2 ; j++)
    {
        indent[j] = ' ';
    }
    indent[level * 2] = '\0';

    stream << indent << "DBRCIndexDS" << std::endl;
    stream << indent << " current db rows   " << currentDbRows << std::endl;
    stream << indent << " domain            " << myDomain << std::endl;
    stream << indent << " base oid counter  " << myBaseCounter << std::endl;
    stream << indent << " base oid type     " << myBaseOIdType << std::endl;
    stream << indent << " size              " << mySize << std::endl;
    DBObject::printStatus(level, stream);
    delete[] indent;
}

OId::OIdCounter
DBRCIndexDS::getBaseCounter() const
{
    return myBaseCounter;
}

OId::OIdType
DBRCIndexDS::getBaseOIdType() const
{
    return myBaseOIdType;
}

unsigned int
DBRCIndexDS::getSize() const
{
    return mySize;
}

bool
DBRCIndexDS::isUnderFull() const
{
    //redistribute in srptindexlogic has to be checked first before any other return value may be assigned
    return false;
}

bool
DBRCIndexDS::isOverFull() const
{
    return false;
}

unsigned int
DBRCIndexDS::getOptimalSize() const
{
    return getSize();
}

r_Minterval
DBRCIndexDS::getAssignedDomain() const
{
    return myDomain;
}

void
DBRCIndexDS::setAssignedDomain(const r_Minterval &newDomain)
{
    LERROR << "DBRCIndexDS::setAssignedDomain(" << newDomain << ") not suported";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

void
DBRCIndexDS::freeDS()
{
    setPersistent(false);
}

bool
DBRCIndexDS::isSameAs(const IndexDS *other) const
{
    if (other->isPersistent())
        if (myOId == other->getIdentifier())
        {
            return true;
        }
    return false;
}

const KeyObject &
DBRCIndexDS::getObject(unsigned int pos) const
{
    LERROR << "DBRCIndexDS::getObject(" << pos << ") not suported";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

void
DBRCIndexDS::getObjects(__attribute__((unused)) KeyObjectVector &objs) const
{
    LERROR << "DBRCIndexDS::getObjects(vec) not suported";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

r_Minterval
DBRCIndexDS::getObjectDomain(unsigned int pos) const
{
    LERROR << "DBRCIndexDS::getObjectDomain(" << pos << ") not suported";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

void
DBRCIndexDS::destroy()
{
    DBObject::destroy();
}

DBRCIndexDS::~DBRCIndexDS() noexcept(false)
{
    validate();
    currentDbRows = 0;
}

void
DBRCIndexDS::updateInDb()
{
    // this operation is illegal
    LERROR << "DBRCIndexDS::updateInDb() update is not possible";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
}

