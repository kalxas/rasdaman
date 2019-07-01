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
 * MERCHANTrABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

#ifndef CONVTYPES_HH
#define CONVTYPES_HH

/**
   convert_type_e is an enumeration that acts as a shortcut to base types
   relevant for DEFs. The values and what they correspond to are listed
   below (the types below the line are for HDF, netCDF, etc.):

   \begin{tabular}{ll}
   ctype_void && No type, used for errors\\
   ctype_bool && bool\\
   ctype_char && char\\
   ctype_rgb && struct {char, char, char}\\
   \hline
   ctype_int8 && signed char\\
   ctype_uint8 && unsigned char\\
   ctype_int16 && short\\
   ctype_uint16 && unsigned short\\
   ctype_int32 && int\\
   ctype_uint32 && unsigned int\\
   ctype_int64 && (unsupported)\\
   ctype_uint64 && (unsupported)\\
   ctype_float32 && float\\
   ctype_float64 && double\\
   ctype_struct && struct \\
   ctype_complex1 && single precision complex \\
   ctype_complex2 && double precision complex
   ctype_cint16 && short complex \\
   ctype_cint32 && long complex
   \end{tabular}
 */
enum convert_type_e
{
    // undefined type
    ctype_void,
    // Shortcut for the three important base types r_Boolean, r_Char and RGBPixel
    ctype_bool,
    ctype_char,
    ctype_rgb,
    // More generic types for HDF
    ctype_int8,
    ctype_uint8,
    ctype_int16,
    ctype_uint16,
    ctype_int32,
    ctype_uint32,
    ctype_int64,
    ctype_uint64,
    ctype_float32,
    ctype_float64,
    // shortcut for structures
    ctype_struct,
    // complex types
    ctype_complex1,
    ctype_complex2,
    ctype_cint16,
    ctype_cint32
};

#endif /* CONVTYPES_HH */
