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

#ifndef D_POINT_HH
#define D_POINT_HH

#include "raslib/mddtypes.hh"  // for r_Range, r_Dimension
#include <vector>
#include <iosfwd>

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**

 Class r_Point represents an n-dimensional point vector.

*/

class r_Point
{
public:
    // the coordinates underlying type
    using value_type = r_Range;
    using DimType = r_Dimension;

    /// default constructor
    r_Point() = default;
    /// constructor getting dimensionality for stream initializing
    explicit r_Point(r_Dimension);

    /// constructor taking string representation (e.g. [ 1, 2, 3])
    explicit r_Point(char *);

    //@Man: 'easy-to-use' constructors
    //@{
    explicit r_Point(r_Range);
    r_Point(r_Range, r_Range);
    r_Point(r_Range, r_Range, r_Range);
    r_Point(r_Range, r_Range, r_Range, r_Range);
    r_Point(r_Range, r_Range, r_Range, r_Range, r_Range);
    explicit r_Point(std::vector<r_Range> pointArg);
    //@}

    /// copy constructor
    r_Point(const r_Point &) = default;

    /// destructor: cleanup dynamic memory
    ~r_Point() = default;

    /// stream-input operator for stream initializing
    r_Point &operator<<(r_Range);

    /// subscriptor for read access
    r_Range operator[](r_Dimension) const;
    /// subscriptor for write access
    r_Range &operator[](r_Dimension);

    /// subscriptor for read access with bound-checking
    r_Range at(r_Dimension) const;
    /// subscriptor for write access with bound-checking
    r_Range &at(r_Dimension);

    /// assignment: cleanup + copy
    r_Point &operator=(const r_Point &) = default;

    /// compares this point with the given point.
    /// @return -2 if dimensions do not match, -1 if this point is smaller,
    /// 0 if equal, 1 if greater than p.
    int compare_with(const r_Point &p) const;
    /**
      Returns 0 if this == p, -1 if this < p, 1 if this > p (considering
      the coordinates in decreasing order of magnitude).
    */

    /// equal operator
    bool operator==(const r_Point &) const;

    /**
      Two points are equal if they have the same number of dimensions and
      the same values.
    */

    /// non equal operator - negation of equal operator
    bool operator!=(const r_Point &) const;
    bool operator<(const r_Point &) const;
    bool operator>(const r_Point &) const;
    bool operator<=(const r_Point &) const;
    bool operator>=(const r_Point &) const;

    /// vector addition
    r_Point operator+(const r_Point &) const;

    /// vector subtraction
    r_Point operator-(const r_Point &) const;

    /// vector multiplication
    r_Point operator*(const r_Point &) const;

    /// vector multiplication with a scalar
    r_Point operator*(const r_Range newElement) const;

    /// get dimensionality
    r_Dimension dimension() const;

    /// get vectorized version of the stored point
    std::vector<r_Range> get_coordinates() const;

    /// writes the state of the object to the specified stream
    void print_status(std::ostream &s) const;

    /// gives back the string representation
    char *get_string_representation() const;
    /**
      The string representation delivered by this method is allocated using <tt> malloc()</tt> and
      has to be free using <tt> free()</tt> in the end. It can be used to construct a <tt> r_Point</tt>
      again with a special constructor provided. The string representation is build using
      <tt> print_status()</tt>.
    */

    /**
     * If you want the output of <tt> get_string_representation()</tt>,
     * but you do not want to worry about memory allocation/deallocation.
     */
    std::string to_string(bool wkt = false) const;

private:
    void checkDimensionMatch(const r_Point &pt) const;

    /// array holding the point coordinates
    std::vector<r_Range> points;
    size_t streamIndex{};
};

extern std::ostream &operator<<(std::ostream &s, const r_Point &d);

#endif
