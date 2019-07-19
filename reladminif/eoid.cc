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
 *       EOId is optimized for maps that contain only EOId of one system/database.
 *
 *
 * COMMENTS:
 *
 **********************************************************************/

#include "eoid.hh"
#include "adminif.hh"           // for AdminIf
#include "databaseif.hh"        // for ostream, DatabaseIf
#include "raslib/error.hh"      // for r_Error, r_Error::r_Error_Transaction...
#include <ostream>              // for operator<<, basic_ostream, ostream

EOId::EOId() : OId()
{
    if (AdminIf::getCurrentDatabaseIf())
    {
        systemName = AdminIf::getSystemName();
        databaseName = AdminIf::getCurrentDatabaseIf()->getName();
    }
    else
    {
        throw r_Error(r_Error::r_Error_TransactionNotOpen);
    }
}

EOId::EOId(const char *systemname, const char *dbname, OId::OIdCounter id,
           OId::OIdType type)
    : OId(id, type), databaseName(dbname), systemName(systemname)
{
}

EOId::EOId(const OId &id) : OId(id)
{
    if (AdminIf::getCurrentDatabaseIf())
    {
        systemName = AdminIf::getSystemName();
        databaseName = AdminIf::getCurrentDatabaseIf()->getName();
    }
    else
    {
        throw r_Error(r_Error::r_Error_TransactionNotOpen);
    }
}

const char *EOId::getSystemName() const
{
    return systemName.c_str();
}

const char *EOId::getBaseName() const
{
    return databaseName.c_str();
}

OId EOId::getOId() const
{
    return static_cast<OId>(*this);
}

void EOId::allocateEOId(EOId &eoid, OId::OIdType t)
{
    if (AdminIf::getCurrentDatabaseIf())
    {
        eoid.systemName = AdminIf::getSystemName();
        eoid.databaseName = AdminIf::getCurrentDatabaseIf()->getName();
    }
    else
    {
        throw r_Error(r_Error::r_Error_TransactionNotOpen);
    }
    allocateOId(eoid, t);
}

bool EOId::operator==(const EOId &one) const
{
    return OId::operator==(one) && systemName == one.systemName && databaseName == one.databaseName;
}
bool EOId::operator!=(const EOId &one) const
{
    return !EOId::operator==(one);
}
bool EOId::operator<(const EOId &old) const
{
    return OId::operator<(old) || databaseName < old.databaseName || systemName < old.systemName;
}
bool EOId::operator>(const EOId &old) const
{
    return !OId::operator>(old) || databaseName > old.databaseName || systemName > old.systemName;
}
bool EOId::operator<=(const EOId &old) const
{
    return operator<(old) || operator==(old);
}
bool EOId::operator>=(const EOId &old) const
{
    return operator>(old) || operator==(old);
}

void EOId::print_status(std::ostream &s) const
{
    s << systemName.c_str() << "|" << databaseName.c_str() << "|";
    OId::print_status(s);
}

std::ostream &operator<<(std::ostream &s, const EOId &d)
{
    s << "EOId(" << d.getSystemName() << "|" << d.getBaseName() << "|" << d.getOId() << ")";
    return s;
}

std::ostream &operator<<(std::ostream &s, EOId &d)
{
    s << "EOId(" << d.getSystemName() << "|" << d.getBaseName() << "|" << d.getOId() << ")";
    return s;
}
