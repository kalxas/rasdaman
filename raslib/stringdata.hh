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

#ifndef D_STRINGDATA_HH
#define D_STRINGDATA_HH

#include <iosfwd>
#include <string>

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**
 * A string value wrapper.
 */
class r_String
{
public:
    /// default constructor
    r_String() = default;
    /// constructs a string from the string representation
    explicit r_String(const char *v);
    
    ~r_String() = default;

    /// debug output
    void print_status(std::ostream &s) const;
    
    const std::string get_value() const;

private:
    /// string representation
    std::string value;
};

//@ManMemo: Module: {\bf raslib}
/**
  Output stream operator for objects of type <tt>const</tt> r_Oid.
*/
extern std::ostream &operator<<(std::ostream &s, const r_String &oid);

#endif
