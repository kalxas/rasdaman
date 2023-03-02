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
#pragma once

#include "oidif.hh"  // for OId, OId::OIdType, OId::OIdCounter

#include <iosfwd>  // for ostream
#include <string>  // for string

class r_Error;
class EOId;

//@ManMemo: Module: {\bf reladminif}.
/*@Doc:
EOId is optimized for maps that contain only EOId of one system/database.
when true multiple connections are implemented the order of compare
statements in the operator"<" and ">" must be changed.
*/
/**
  * \ingroup Reladminifs
  */
class EOId : public OId
{
public:
    EOId();
    /*@Doc:
    uses the currently open database to get system and db name
    systemname and database name will be null string when the
    database is not really open.
    */

    EOId(const char *systemname, const char *dbname, OId::OIdCounter id, OIdType type);
    /*@Doc:
    constructs a complete EOId.
    */

    EOId(const OId &id);
    /*@Doc:
    uses the currently open database to get system and db name
    systemname and database name will be null string when the
    database is not really open.
    */

    ~EOId() = default;

    EOId &operator=(const EOId &) = default;

    const char *getSystemName() const;
    /*@Doc:
    returns the system name, which is the same as the
    one returned by databaseif.
    */

    const char *getBaseName() const;
    /*@Doc:
    returns the database name, which is the same as the
    one returned by databaseif
    */

    OId getOId() const;
    /*@Doc:
    returns the oid of this eoid
    */

    static void allocateEOId(EOId &eoid, OId::OIdType t);
    /*@Doc:
    Allocates a new logical MDD EOid in the currently opened base.
    throws an r_Error_DatabaseClosed when the database is not really open.
    */

    bool operator<(const EOId &old) const;
    bool operator>(const EOId &old) const;
    bool operator<=(const EOId &old) const;
    bool operator>=(const EOId &old) const;
    bool operator==(const EOId &one) const;
    bool operator!=(const EOId &one) const;

    void print_status(std::ostream &o) const;
    /*@Doc:
    returns the systemname|databasename|oid
    */

private:
    std::string databaseName;
    /*@Doc:
    the name of the database the oid of this eoid is valid for.
    stl std::string was used because of the compare functionality.
    */

    std::string systemName;
    /*@Doc:
    the name of the system above mentioned database runs on
    stl std::string was used because of the compare functionality.
    */
};

extern std::ostream &operator<<(std::ostream &s, EOId &d);

extern std::ostream &operator<<(std::ostream &s, const EOId &d);
