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

#ifndef D_MITER_HH
#define D_MITER_HH

#include "raslib/mddtypes.hh"
#include "raslib/minterval.hh"

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**
  r_Miter is used for iterating through parts of
  multidimensional intervals. It is given the domain of
  the object to be iterated through, the size of the base
  base type, the address of the first cell in the Tile and
  an Minterval specifying the area to be iterated through.

  Going to the next cell is done with nextCell() which
  returns the address of the next cell. Test for the end
  is done with isDone(). The iterator can be reset with
  reset().
*/
class r_Miter
{
public:
    /**
      The pointers are stored, do not delete the objects as long
      as the iterator is used!
    */
    r_Miter(const r_Minterval *newAreaIter,
            const r_Minterval *newAreaTile, r_Bytes newCellSize,
            const char *newFirstCell);
    ~r_Miter();
    /// resets iterator to first cell.
    void reset();
    /// returns current cell and sets iterator to next cell.
    inline char *nextCell();
    /// returns TRUE if iteration is finished.
    inline bool isDone();
protected:
    // structure storing information on iteration for each dimension
    // (perhaps add dimension for reordering later)
    struct incArrElem
    {
        r_Range repeat; // total number of repeats
        int inc;    // increment per repeat
        int curr;   // current repeat
    };
    /// area to be iterated through
    const r_Minterval *areaIter{NULL};
    /// area of tile.
    const r_Minterval *areaTile{NULL};
    /// offset of first cell in tile.
    const char *firstCell{NULL};
    /// array with increments
    incArrElem *incArrIter{NULL};
    /// current cell for iteration;
    char *currCell{NULL};
    /// size of base type.
    r_Bytes cellSize{};
    /// counter for position in lowest dimension.
    int lowCount{};
    /// flag set if iteration is finished.
    bool done{false};
};

#include "miter.icc"

#endif
