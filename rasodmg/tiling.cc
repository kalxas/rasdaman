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
 * SOURCE: tiling.cc
 *
 * MODULE: rasodmg
 * CLASS:   r_Tiling
 *
 * COMMENTS:
 *          None
 *
*/

#include "rasodmg/tiling.hh"
#include "raslib/minterval.hh"
#include "raslib/error.hh"

#include <logging.hh>

#include <algorithm>
#include <cmath>
#include <iostream>
#include <string>

// ------------- tiling base ---------------------------------------------------

const char *r_Tiling::ASTERIX = "*";
const char *r_Tiling::TCOLON = ";";
const char *r_Tiling::TCOMMA = ",";
const char *r_Tiling::LSQRBRA = "[";
const char *r_Tiling::RSQRBRA = "]";
const int r_Tiling::DefaultBase = 10;
const r_Bytes r_Tiling::defaultTileSize = 4000000;

unsigned int r_Tiling::parse_unsigned(const char *encoded) const
{
    return static_cast<r_Dimension>(parse_unsigned_long(encoded));
}
unsigned long r_Tiling::parse_unsigned_long(const char *encoded) const
{
    auto ret = parse_long(encoded);
    if (ret <= 0)
    {
        LERROR << "Expected non-negative number, got: " << encoded;
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
    return static_cast<r_Bytes>(ret);
}
long r_Tiling::parse_long(const char *encoded) const
{
    try
    {
        size_t numChars;
        return std::stol(encoded, &numChars, DefaultBase);
    }
    catch (...)
    {
        LERROR << "Failed decoding number '" << encoded << "'.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
}
double r_Tiling::parse_double(const char *encoded) const
{
    try
    {
        size_t numChars;
        return std::stod(encoded, &numChars);
    }
    catch (...)
    {
        LERROR << "Failed decoding floating-point number '" << encoded << "'.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
}
r_Minterval r_Tiling::parse_minterval(const char *encoded) const
{
    try
    {
        return r_Minterval(encoded);
    }
    catch (r_Error &err)
    {
        LERROR << "Failed decoding tile configuration \"" << encoded
               << "\" from tileparams, reason: " << err.get_errorno() << " - " << err.what();
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
}
void r_Tiling::check_nonempty_tiling(const char *encoded) const
{
    if (!encoded)
    {
        LERROR << "no tiling specified.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
}
void r_Tiling::check_premature_stream_end(const char *currPos, const char *endPos) const
{
    if (currPos > endPos)
    {
        LERROR << "Failed decoding tiling, premature end of stream.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
}
std::unique_ptr<char[]> r_Tiling::copy_buffer(const char *buf, size_t len) const
{
    std::unique_ptr<char[]> ret(new char[len + 1]);
    memcpy(ret.get(), buf, len);
    ret[len] = '\0';
    return ret;
}
const char *r_Tiling::advance_to_next_char(const char *p, const char *search) const
{
    const char *res = strstr(p, search);
    if (!res)
    {
        LERROR << "Error decoding tiling parameter, could not find '" << search << "' in '" << p << "'.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
    return res;
}

// ------------- no tiling -----------------------------------------------------

const char *
    r_No_Tiling::description = "no parameters";

r_No_Tiling::r_No_Tiling(const char *)
{
}
void r_No_Tiling::print_status(std::ostream &os) const
{
    os << "r_No_Tiling[ ]";
}
bool r_No_Tiling::is_compatible(const r_Minterval &, r_Bytes) const
{
    return true;
}
std::vector<r_Minterval>
r_No_Tiling::compute_tiles(const r_Minterval &obj_domain, r_Bytes) const
{
    return std::vector<r_Minterval>{obj_domain};
}
r_Tiling *
r_No_Tiling::clone() const
{
    return new r_No_Tiling();
}
r_Tiling_Scheme
r_No_Tiling::get_tiling_scheme() const
{
    return r_NoTiling;
}
std::ostream &
operator<<(std::ostream &os, const r_Tiling &t)
{
    t.print_status(os);
    return os;
}

// ------------- size tiling ---------------------------------------------------

const char *
    r_Size_Tiling::description = "tile configuration or tile dimension and tile size (in bytes) (ex: \"[0:9,0:9];100\" or \"2;100\")";

r_Size_Tiling::r_Size_Tiling(const char *encoded)
    : r_Size_Tiling{parse_unsigned(encoded)}
{
}
r_Size_Tiling::r_Size_Tiling(r_Bytes ts)
    : tile_size(ts)
{
}

r_Bytes
r_Size_Tiling::get_tile_size() const
{
    return tile_size;
}
void r_Size_Tiling::print_status(std::ostream &os) const
{
    os << "r_Size_Tiling[ tile size = " << tile_size << " ]";
}
bool r_Size_Tiling::is_compatible(const r_Minterval &obj_domain, r_Bytes cellTypeSize) const
{
    return cellTypeSize <= tile_size && obj_domain.dimension() != 0;
}

std::vector<r_Minterval>
r_Size_Tiling::compute_tiles(const r_Minterval &obj_domain, r_Bytes cellTypeSize) const
{
    if (cellTypeSize > tile_size)
    {
        LERROR << "tile size (" << tile_size << ") is smaller than type length (" << cellTypeSize << ")";
        throw r_Error(TILESIZETOOSMALL);
    }
    auto bigDom = obj_domain;
    auto dim = bigDom.dimension();
    // compute the domain of the small tiles
    // tiles are n-dimensional cubes with edge length n-th root of max tile size
    LTRACE << "tile size " << get_tile_size();
    auto edgeLength = std::max(static_cast<r_Range>(floor(pow(get_tile_size() / cellTypeSize, 1.0 / dim))),
                               static_cast<r_Range>(1));
    r_Minterval tileDom(dim);
    for (r_Dimension dimcnt = 0; dimcnt < dim; dimcnt++)
        tileDom << r_Sinterval(0l, edgeLength - 1);

    r_Minterval currDom(dim);
    r_Point cursor(dim);

    // calculate size of Tiles
    auto tileSize = tileDom.get_extent();
    // origin of bigTile
    auto origin = bigDom.get_origin();
    // initialize currDom
    for (r_Dimension dimcnt = 0; dimcnt < dim; dimcnt++)
        currDom << r_Sinterval(origin[dimcnt], origin[dimcnt] + tileSize[dimcnt] - 1);

    // resets tileDom to lower left side of bigTile
    tileDom = currDom;
    // intersect with bigTile
    currDom.intersection_with(bigDom);

    // iterate with smallTile over bigTile
    std::vector<r_Minterval> result;
    bool done = false;
    while (!done)
    {
        currDom.intersection_with(bigDom);
        // insert small tile in set
        result.push_back(currDom);

        // increment cursor, start with highest dimension
        auto i = cursor.dimension() - 1;
        cursor[i] += tileSize[i];
        // move cursor
        currDom = tileDom.create_translation(cursor);
        while (!currDom.intersects_with(bigDom))
        {
            cursor[i] = 0;
            if (i == 0)
            {
                done = true;
                break;
            }
            i--;
            cursor[i] += tileSize[i];
            // move cursor
            currDom = tileDom.create_translation(cursor);
        }
    }
    return result;
}

r_Tiling *
r_Size_Tiling::clone() const
{
    return new r_Size_Tiling(tile_size);
}
r_Tiling_Scheme
r_Size_Tiling::get_tiling_scheme() const
{
    return r_SizeTiling;
}

// ------------- size tiling ---------------------------------------------------

r_Dimension_Tiling::r_Dimension_Tiling(r_Dimension dim, r_Bytes ts)
    : r_Size_Tiling(ts), dimension(dim)
{
}
r_Dimension
r_Dimension_Tiling::get_dimension() const
{
    return dimension;
}
void r_Dimension_Tiling::print_status(std::ostream &os) const
{
    os << "r_Dimension_Tiling[ ";
    r_Size_Tiling::print_status(os);
    os << " dimension = " << dimension << " ]";
}
bool r_Dimension_Tiling::is_compatible(const r_Minterval &obj_domain, r_Bytes cellTypeSize) const
{
    return obj_domain.dimension() == dimension &&
           r_Size_Tiling::is_compatible(obj_domain, cellTypeSize);
}
