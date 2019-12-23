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
 * SOURCE:   oid.cc
 *
 * MODULE:   raslib
 * CLASS:    r_OId
 *
 * COMMENTS:
 *
*/

#include "raslib/oid.hh"
#include "raslib/error.hh"
#include <logging.hh>

#include <sstream>
#include <fstream>
#include <iomanip>
#include <string.h>
#include <stdlib.h>


r_OId::r_OId(const char *initOIdString)
    : oidString{initOIdString ? initOIdString : ""}
{
    // set oidString
    if (initOIdString)
    {
        // system name
        const char *endPtr;
        const char *startPtr = endPtr = initOIdString;
        while (*endPtr != '|' && *endPtr != '\0')
            endPtr++;
        if (endPtr - startPtr >= 1)
            systemName = std::string(startPtr, static_cast<size_t>(endPtr - startPtr));

        if (*endPtr != '\0')
        {
            // base name
            endPtr++;
            startPtr = endPtr;
            while (*endPtr != '|' && *endPtr != '\0')
                endPtr++;
            if (endPtr - startPtr >= 1)
                baseName = std::string(startPtr, static_cast<size_t>(endPtr - startPtr));

            if (*endPtr != '\0')
            {
                // local oid
                endPtr++;
                startPtr = endPtr;
                size_t pos;
                try {
                    localOId = std::stoll(startPtr, &pos);
                } catch (...) {
                    LERROR << "Failed parsing local oid from '" << startPtr << "'.";
                    throw r_Error(r_Error::r_Error_OIdInvalid);
                }
            }
        }
    }
}

r_OId::r_OId(const char *initSystemName, const char *initBaseName, long long initLocalOId)
    : systemName{initSystemName ? initSystemName : ""},
      baseName{initBaseName ? initBaseName : ""},
      localOId(initLocalOId)
{
    oidString.reserve(systemName.size() + baseName.size() + 10);
    oidString += systemName;
    oidString += "|";
    oidString += baseName;
    oidString += "|";
    oidString += std::to_string(localOId);
}
    
r_OId &r_OId::operator=(const r_OId &o)
{
    if (this == &o)
        return *this;
    if (!o.oidString.empty())
        oidString = o.oidString;
    if (!o.systemName.empty())
        systemName = o.systemName;
    if (!o.baseName.empty())
        baseName = o.baseName;
    localOId = o.localOId;
    return *this;
}

bool
r_OId::operator==(const r_OId &oid) const
{
    return oidString == oid.oidString;
}
bool
r_OId::operator!=(const r_OId &oid) const
{
    return !operator==(oid);
}
bool
r_OId::operator>(const r_OId &oid) const
{
    return systemName == oid.systemName && baseName == oid.baseName &&
           localOId > oid.localOId;
}
bool
r_OId::operator< (const r_OId &oid) const
{
    return systemName == oid.systemName && baseName == oid.baseName &&
           localOId < oid.localOId;
}
bool
r_OId::operator>=(const r_OId &oid) const
{
    return !operator<(oid);
}
bool
r_OId::operator<=(const r_OId &oid) const
{
    return !operator>(oid);
}
        
        
void
r_OId::print_status(std::ostream &s) const
{
    s << oidString;
}
std::ostream &operator<<(std::ostream &s, const r_OId &oid)
{
    oid.print_status(s);
    return s;
}
                        
const char *
r_OId::get_string_representation() const
{
    return oidString.c_str();
}

const char *
r_OId::get_system_name() const
{
    return systemName.c_str();
}

const char *
r_OId::get_base_name() const
{
    return baseName.c_str();
}

long long
r_OId::get_local_oid() const
{
    return localOId;
}

double
r_OId::get_local_oid_double() const
{
    return localOId;
}

bool
r_OId::is_valid() const
{
    return localOId != 0;
}
