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
 * INCLUDE: oid.hh
 *
 * MODULE:  raslib
 * CLASS:   r_OId
 *
 * COMMENTS:
 *      The class represents an object identifier (OId).
 *
*/

#ifndef D_OID_HH
#define D_OID_HH

#include <iosfwd>
#include <string>

//@ManMemo: Module: {\bf raslib}

/*@Doc: 
 * Class r_OId represents an object identifier.
 */
class r_OId
{
public:
    /// default constructor
    r_OId() = default;
    /// constructs an OId from the string representation
    r_OId(const char *);
    /// constructor getting oid parts
    r_OId(const char *initSystemName, const char *initBaseName, long long initLocalOId);
    r_OId(const r_OId &) = default;
    r_OId &operator=(const r_OId &o);
    
    virtual ~r_OId() = default;

    /// debug output
    void print_status(std::ostream &s) const;

    //@Man: Comparison operators:
    //@{
    /// operator for equality
    bool operator==(const r_OId &) const;
    /// operator for not equal
    bool operator!=(const r_OId &) const;
    /// operator for greater than
    bool operator> (const r_OId &) const;
    /// operator for less than
    bool operator< (const r_OId &) const;
    /// operator for greater or equal than
    bool operator>=(const r_OId &) const;
    /// operator for less than or equal
    bool operator<=(const r_OId &) const;
    //@}

    /// gets the oid's string representation
    const char *get_string_representation() const;
    /// get system name
    const char *get_system_name() const;
    /// get base name
    const char *get_base_name() const;
    /// get local oid
    long long get_local_oid() const;
    /// get local oid as double
    double get_local_oid_double() const;
    /// determines if oid is valid
    bool is_valid() const;

private:
    /// string representation
    std::string oidString;
    /// system name
    std::string systemName;
    /// base name
    std::string baseName;
    /// local oid
    long long localOId{};
};

//@ManMemo: Module: {\bf raslib}
/**
  Output stream operator for objects of type {\tt const} \Ref{r_Oid}.
*/
extern std::ostream &operator<<(std::ostream &s, const r_OId &oid);

#endif
