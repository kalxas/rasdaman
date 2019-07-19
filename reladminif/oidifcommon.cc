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
// This is -*- C++ -*-

#include "oidif.hh"             // for OId, OId::OIdCounter, OId::OIdType
#include "databaseif.hh"        // for ostream
#include "raslib/error.hh"      // for r_Error, r_Error::r_Error_CreatingOId...
#include <logging.hh>           // for Writer, CTRACE, LTRACE, CFATAL, LERROR
#include <ostream>              // for operator<<, ostream, basic_ostream
#include <cassert>

long long OId::ID_MULTIPLIER = 512;

OId::OIdCounter OId::nextMDDOID = 0;
OId::OIdCounter OId::nextMDDCOLLOID = 0;
OId::OIdCounter OId::nextMDDTYPEOID = 0;
OId::OIdCounter OId::nextMDDBASETYPEOID = 0;
OId::OIdCounter OId::nextMDDDIMTYPEOID = 0;
OId::OIdCounter OId::nextMDDDOMTYPEOID = 0;
OId::OIdCounter OId::nextSTRUCTTYPEOID = 0;
OId::OIdCounter OId::nextSETTYPEOID = 0;
OId::OIdCounter OId::nextBLOBOID = 0;
OId::OIdCounter OId::nextDBMINTERVALOID = 0;
OId::OIdCounter OId::nextSTORAGEOID = 0;
OId::OIdCounter OId::nextMDDHIERIXOID = 0;
OId::OIdCounter OId::nextATOMICTYPEOID = 0;
OId::OIdCounter OId::nextMDDRCIXOID = 0;
OId::OIdCounter OId::nextUDFOID = 0;
OId::OIdCounter OId::nextUDFPACKAGEOID = 0;
OId::OIdCounter OId::nextFILETILEOID = 0;
OId::OIdCounter OId::nextDBNULLVALUESOID = 0;

unsigned int OId::maxCounter = 22;

const char *OId::counterNames[] =
{
    "INVALID", "MDDOID", "MDDCOLLOID", "MDDTYPEOID", "MDDBASETYPEOID",
    "MDDDIMTYPEOID", "MDDDOMTYPEOID", "STRUCTTYPEOID", "SETTYPEOID", "BLOBOID",
    "DBMINTERVALOID", "STORAGEOID", "MDDHIERIXOID", "INLINEINDEXOID",
    "INLINETILEOID", "INNEROID", "ATOMICTYPEOID", "UDFOID", "UDFPACKAGEOID",
    "MDDRCIXOID", "FILETILEOID", "DBNULLVALUESOID"
};

OId::OIdCounter *OId::counterIds[] =
{
    nullptr, &nextMDDOID, &nextMDDCOLLOID, &nextMDDTYPEOID, &nextMDDBASETYPEOID,
    &nextMDDDIMTYPEOID, &nextMDDDOMTYPEOID, &nextSTRUCTTYPEOID, &nextSETTYPEOID,
    &nextBLOBOID, &nextDBMINTERVALOID, &nextSTORAGEOID, &nextMDDHIERIXOID,
    &nextMDDHIERIXOID, &nextBLOBOID, &nextBLOBOID, &nextATOMICTYPEOID,
    &nextUDFOID, &nextUDFPACKAGEOID, &nextMDDRCIXOID, &nextFILETILEOID,
    &nextDBNULLVALUESOID
};

bool OId::loadedOk = false;

OId::OId(OIdPrimitive newId)
{
    oid = newId / OId::ID_MULTIPLIER;
    oidtype = static_cast<OId::OIdType>(newId - (oid * OId::ID_MULTIPLIER));
    assert(oidtype <= OId::maxCounter);
}

OId::OId(OIdCounter newId, OIdType type)
    : oid{newId}, oidtype{type}
{
    assert(oidtype <= OId::maxCounter);
}

OId::OIdCounter OId::getCounter() const
{
    return oid;
}

OId::OIdType OId::getType() const
{
    return oidtype;
}

OId::operator long long() const
{
    return oid * OId::ID_MULTIPLIER + oidtype;
}

void OId::print_status(std::ostream &s) const
{
    s << this;
}

void OId::allocateOId(OId &id, OIdType type, OIdCounter howMany)
{
    if (howMany == 0)
    {
        LERROR << "attempting to allocate zero oids of type " << type;
        throw r_Error(r_Error::r_Error_CreatingOIdFailed);
    }
    if (type == INVALID || type == INNEROID || type == ATOMICTYPEOID ||
        type > static_cast<int>(maxCounter))
    {
        LERROR << "OIDs of type " << type << " cannot be allocated.";
        throw r_Error(r_Error::r_Error_CreatingOIdFailed);
    }

    id.oid = *counterIds[type];
    id.oidtype = type;
    *counterIds[type] = *counterIds[type] + howMany;
}

bool OId::operator!=(const OId &one) const
{
    return !(OId::operator==(one));
}
bool OId::operator==(const OId &one) const
{
    return oidtype == one.oidtype && oid == one.oid;
}
bool OId::operator<(const OId &old) const
{
    return oidtype < old.oidtype || (oidtype == old.oidtype && oid < old.oid);
}
bool OId::operator>(const OId &old) const
{
    return oidtype > old.oidtype || (oidtype == old.oidtype && oid > old.oid);
}
bool OId::operator<=(const OId &old) const
{
    return operator<(old) || operator==(old);
}
bool OId::operator>=(const OId &old) const
{
    return operator>(old) || operator==(old);
}
bool operator==(const long long one, const OId &two)
{
    return one == static_cast<long long>(two);
}
bool operator==(const OId &two, const long long one)
{
    return one == static_cast<long long>(two);
}

std::ostream &operator<<(std::ostream &s, const OId &d)
{
    s << "OId(" << d.getCounter() << ":" << d.getType() << ")";
    return s;
}
std::ostream &operator<<(std::ostream &s, OId::OIdType d)
{
    switch (d)
    {
    case OId::INVALID:          s << "INVALID"; break;
    case OId::MDDOID:           s << "MDDOID"; break;
    case OId::MDDCOLLOID:       s << "MDDCOLLOID"; break;
    case OId::MDDTYPEOID:       s << "MDDTYPEOID"; break;
    case OId::MDDBASETYPEOID:   s << "MDDBASETYPEOID"; break;
    case OId::MDDDIMTYPEOID:    s << "MDDDIMTYPEOID"; break;
    case OId::MDDDOMTYPEOID:    s << "MDDDOMTYPEOID"; break;
    case OId::STRUCTTYPEOID:    s << "STRUCTTYPEOID"; break;
    case OId::SETTYPEOID:       s << "SETTYPEOID"; break;
    case OId::BLOBOID:          s << "BLOBOID"; break;
    case OId::DBMINTERVALOID:   s << "DBMINTERVALOID"; break;
    case OId::DBNULLVALUESOID:  s << "DBNULLVALUESOID"; break;
    case OId::STORAGEOID:       s << "STORAGEOID"; break;
    case OId::MDDHIERIXOID:     s << "MDDHIERIXOID"; break;
    case OId::DBTCINDEXOID:     s << "DBTCINDEXOID"; break;
    case OId::INLINETILEOID:    s << "INLINETILEOID"; break;
    case OId::INNEROID:         s << "INNEROIDOID"; break;
    case OId::ATOMICTYPEOID:    s << "ATOMICTYPEOID"; break;
    case OId::UDFOID:           s << "UDFOID"; break;
    case OId::UDFPACKAGEOID:    s << "UDFPACKAGEOID"; break;
    case OId::MDDRCIXOID:       s << "MDDRCIXOID"; break;
    case OId::FILETILEOID:      s << "FILETILEOID"; break;
    default:
        s << "UNKNOWN: " << static_cast<int>(d);
        break;
    }
    return s;
}

