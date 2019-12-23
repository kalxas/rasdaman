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
 * INCLUDE: point.hh
 *
 * MODULE:  raslib
 * CLASS:   r_PointDouble
 *
 * COMMENTS:
 *
*/

#ifndef R_DOUBLEPOINT_HH
#define R_DOUBLEPOINT_HH


#include "raslib/mddtypes.hh"
#include "raslib/point.hh"

#include <iosfwd>
#include <vector>

//@ManMemo: Module: {\bf raslib}

/*@Doc:

 Class \Ref{r_Point} represents an n-dimensional point std::vector.

*/

class r_PointDouble
{
public:
    /// constructor getting dimensionality for stream initializing
    r_PointDouble(r_Dimension);

    r_PointDouble(r_Dimension dim, double value);

    //@Man: 'variading constructors'
    ///
    //@}

    r_PointDouble(const r_Point &pt);

    r_PointDouble(const std::vector<double> &content);

    /// copy constructor
    r_PointDouble(const r_PointDouble &vectorArg);

    /// destructor: cleanup dynamic memory
    ~r_PointDouble();

    /// subscriptor for read access
    double  operator[](size_t) const ;
    /// subscriptor for write access
    double &operator[](size_t) ;

    /// assignment: cleanup + copy
    const r_PointDouble &operator= (const r_PointDouble &);

    /// compares this point with the given point.
    int compare_with(const  r_PointDouble &p) const;
    /**
      Returns 0 if this == p, -1 if this < p, 1 if this > p (considering
      the coordinates in lexicographic order).
    */

    /// equal operator
    bool operator==(const r_PointDouble &vectorArg) const;

    bool operator!=(const r_PointDouble &vectorArg) const;

    bool operator<(const r_PointDouble &vectorArg) const;
    /**
      Two points are equal if they have the same number of dimensions and
      the same values.
    */

    /// std::vector addition
    r_PointDouble operator+(const r_PointDouble &vectorArg) const;

    /// std::vector subtraction
    r_PointDouble operator-(const r_PointDouble &vectorArg) const;

    /// diagonal extension of multiplication across the cartesian product
    r_PointDouble operator*(const r_PointDouble &vectorArg) const;

    /// scalar multiplication
    r_PointDouble operator*(const double scalarArg) const;

    /// scalar product
    double dotProduct(const r_PointDouble &r) const;

    /// same as std::vector::size()
    size_t dimension() const;

    /// writes the state of the object to the specified stream
    void print_status(std::ostream &s) const;

    /// returns the stored vector
    const std::vector<double> &getVectorContent() const;

    /// converts the stored vector to an r_Point (integer vertices)
    r_Point toIntPoint() const;


private:
    /// array holding the point coordinates
    std::vector<double> points;
};

#endif
