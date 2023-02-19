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

#ifndef D_ODMGTYPES_HH
#define D_ODMGTYPES_HH

// for type-limits
#include <limits.h>
#include <float.h>
#include <cstdint>

using r_Boolean = std::uint8_t;
using r_Char = std::uint8_t;
using r_Octet = std::int8_t;
using r_UShort = std::uint16_t;
using r_Short = std::int16_t;
using r_Long = std::int32_t;
using r_ULong = std::uint32_t;
using r_Float = float;
using r_Double = double;

inline void get_limits_octet(double &min, double &max)
{
    min = static_cast<double>(SCHAR_MIN);
    max = static_cast<double>(SCHAR_MAX);
}
inline void get_limits_char(double &min, double &max)
{
    min = 0.0;
    max = static_cast<double>(UCHAR_MAX);
}

inline void get_limits_short(double &min, double &max)
{
    min = static_cast<double>(SHRT_MIN);
    max = static_cast<double>(SHRT_MAX);
}

inline void get_limits_Ushort(double &min, double &max)
{
    min = 0.0;
    max = static_cast<double>(USHRT_MAX);
}

inline void get_limits_long(double &min, double &max)
{
    min = static_cast<double>(INT_MIN);
    max = static_cast<double>(INT_MAX);
}

inline void get_limits_Ulong(double &min, double &max)
{
    min = 0.0;
    max = static_cast<double>(UINT_MAX);
}

inline void get_limits_float(double &min, double &max)
{
    min = -static_cast<double>(FLT_MAX);
    max = static_cast<double>(FLT_MAX);
}

inline void get_limits_double(double &min, double &max)
{
    min = -DBL_MAX;
    max = DBL_MAX;
}

#endif
