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

#ifndef _R_TILING_HH_
#define _R_TILING_HH_

#include "raslib/mddtypes.hh"

#include <vector>
#include <iosfwd>
#include <memory>

class r_Tiling;
class r_No_Tiling;
class r_Size_Tiling;
class r_Dimension_Tiling;
class r_Minterval;

//@ManMemo: Module {\bf rasodmg}

/**
  The r_Tiling class is used to specify in which way the tiling is done
  by the system. The core method that does that is called decomposeMDD()
  and must be implemented by all derived classes. It takes an object that
  hasn't yet been split and divides it into tiles. Each derived class
  implements a diferent decomposition method.
*/

/**
  * \ingroup Rasodmgs
  */
class r_Tiling
{
public:
    virtual ~r_Tiling() = default;

    /// Prints the current status of the object
    virtual void print_status(std::ostream &os) const = 0;

    /// Check compatibility of object domain with this tiling
    virtual bool is_compatible(const r_Minterval &obj_domain, r_Bytes cellTypeSize) const = 0;

    /// Decompose an object in tiles
    virtual std::vector<r_Minterval> compute_tiles(const r_Minterval &obj_domain, r_Bytes cell_size) const = 0;
    /**
    This method provides the core funcionality of this class. All derived
    classes must implement it. As input parameters it takes the big object to
    be decomposed and returns a set of tiles that compose the big object.
            This method throws an exeception when the dimension specified, extend or
            the cell_size are incompatible with the current tiling.  You can check
            compatibility by invoking is_compatible.
    */

    /// Clones this object
    virtual r_Tiling *clone() const = 0;
    /**
    This method is similar to a copy constructor, this is, is returns a copy of
    the current object. Derived classes must explicitly implement this method.
    */

    virtual r_Tiling_Scheme get_tiling_scheme() const = 0;
    /**
    return the type of tiling scheme
    */

    static const char *ASTERIX;
    static const char *TCOLON;
    static const char *TCOMMA;
    static const char *LSQRBRA;
    static const char *RSQRBRA;
    static const int DefaultBase;
    static const r_Bytes defaultTileSize;

protected:
    void check_nonempty_tiling(const char *encoded) const;
    void check_premature_stream_end(const char *currPos, const char *endPos) const;
    r_Minterval parse_minterval(const char *encoded) const;
    unsigned int parse_unsigned(const char *encoded) const;
    unsigned long parse_unsigned_long(const char *encoded) const;
    long parse_long(const char *encoded) const;
    double parse_double(const char *encoded) const;
    std::unique_ptr<char[]> copy_buffer(const char *buf, size_t len) const;
    const char *advance_to_next_char(const char *p, const char *search) const;
};

/**
  * \ingroup Rasodmgs
  */
class r_No_Tiling : public r_Tiling
{
public:
    /// Constructor that reads everything from a string e.g."100"
    /// This string is ignored in the constructor, it is present in order to have an uniform interface
    explicit r_No_Tiling(const char *encoded);
    r_No_Tiling() = default;
    virtual ~r_No_Tiling() = default;

    /// Prints the current status of the object
    void print_status(std::ostream &os) const override;

    /// Check compatibility of object domain with this tiling
    bool is_compatible(const r_Minterval &obj_domain, r_Bytes cellTypeSize) const override;
    /// returns true

    /// Decompose an object in tiles
    std::vector<r_Minterval> compute_tiles(const r_Minterval &obj_domain, r_Bytes cellTypeSize) const override;
    /// returns obj_domain

    r_Tiling *clone() const override;

    r_Tiling_Scheme get_tiling_scheme() const override;

    static const char *description;
};

/**
  * \ingroup Rasodmgs
  */
class r_Size_Tiling : public r_Tiling
{
public:
    /// Constructor that reads everything from a string e.g."100"
    explicit r_Size_Tiling(const char *encoded);
    r_Size_Tiling(r_Bytes ts = r_Tiling::defaultTileSize);
    virtual ~r_Size_Tiling() = default;

    void print_status(std::ostream &os) const override;

    /// returns true if the cellTypeSize is smaller or equal to the tile size and obj_domain has more than 0 dimensions
    bool is_compatible(const r_Minterval &obj_domain, r_Bytes cellTypeSize) const override;

    std::vector<r_Minterval> compute_tiles(const r_Minterval &obj_domain, r_Bytes cellTypeSize) const override;

    r_Bytes get_tile_size() const;
    r_Tiling *clone() const override;
    r_Tiling_Scheme get_tiling_scheme() const override;

    static const char *description;

protected:
    /// Tile size
    r_Bytes tile_size{};
};

/**
  * \ingroup Rasodmgs
  */
class r_Dimension_Tiling : public r_Size_Tiling
{
public:
    /// Constructor for this object (Takes dim (no of dimension) and tile size as parameter)
    r_Dimension_Tiling(r_Dimension dim, r_Bytes ts = r_Tiling::defaultTileSize);
    virtual ~r_Dimension_Tiling() = default;

    void print_status(std::ostream &os) const override;
    /// returns true if the cellTypeSize is smaller or equal to the tile size and the dimension fits the obj_domain
    bool is_compatible(const r_Minterval &obj_domain, r_Bytes cellTypeSize) const override;

    r_Dimension get_dimension() const;
    std::vector<r_Minterval> compute_tiles(const r_Minterval &obj_domain, r_Bytes cellTypeSize) const override = 0;
    r_Tiling *clone() const override = 0;

protected:
    /// dimension the mdd must have
    r_Dimension dimension{};
};

extern std::ostream &operator<<(std::ostream &os, const r_Tiling &t);

#endif
