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

#ifndef _R_DIRDECOMPOSE_HH_
#define _R_DIRDECOMPOSE_HH_

#include "raslib/sinterval.hh"
#include <vector>

//@ManMemo: Module {\bf rasodmg}
/**
  * \ingroup Rasodmgs
  */

/**
  The r_Dir_Decompose class is used to specify a decomposition on
  an n-dimensional cube (for use in <tt>r_Dir_Tiling</tt>). For instance, to
  specify a tiling restriction on a dimension with the form: [0, 2, 4, 5],
  the following code would apply:

    r_Dir_Decompose decomp;

    decomp << 0 << 2 << 4 << 5;

  Note that the first and the last elements input into the object must be
  the origin and limit of that dimension or else a cross-section of the domain
  will occur (as if the elements outside the specification wouldn't mind).

  If one dimension is considered to be a prefered access direction, then
  the r_Dir_Decompose should be empty, this is, no restriction should be
  entered.
*/
class r_Dir_Decompose
{
public:
    r_Dir_Decompose() = default;
    ~r_Dir_Decompose() = default;
    r_Dir_Decompose(const r_Dir_Decompose &) = default;

    /// Reads a new limit for the current dimension
    r_Dir_Decompose &operator<<(r_Range limit);

    /// Reads a new limit for the current dimension and prepends it to the list of limits
    r_Dir_Decompose &prepend(r_Range limit);

    /// Gets the number of intervals the dimension is to be split into
    size_t get_num_intervals() const;

    /// Gets a restriction
    r_Range get_partition(size_t number) const;

    /// Prints the current status of the object
    void print_status(std::ostream &os) const;

protected:
    r_Sinterval get_total_interval();

    /// The buffer that holds the information
    std::vector<r_Range> intervals;
};

//@ManMemo: Module: {\bf rasodmg}
/**
    Prints the status of an r_Dir_Decompose object to a stream
*/
extern std::ostream &operator<<(std::ostream &os, const r_Dir_Decompose &d);

#endif
