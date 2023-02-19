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

#ifndef D_MITERF_HH
#define D_MITERF_HH

#include "raslib/mddtypes.hh"

class r_Minterval;
class Tile;

/**
  * \ingroup raslib
  */

class r_FixedPointNumber
{
public:
    r_FixedPointNumber() = default;
    explicit r_FixedPointNumber(const double &);

    r_FixedPointNumber &operator=(const r_FixedPointNumber &) = default;
    r_FixedPointNumber &operator=(const double &);
    
    // returns carry of fracPart
    inline bool    stepForwardFlag(const r_FixedPointNumber &);

    inline r_Range getIntPart();

private:
    void init(const double &);

    r_Range intPart{};
    r_Range fracPart{};

    static const int FIXPREC;
    static const r_Range carryPos;
    static const r_Range fracMask;
    static const double fixOne;

    friend std::ostream &operator<<(std::ostream &, r_FixedPointNumber &);
};

//@ManMemo: Module: {\bf raslib}
/**
  * \ingroup raslib
  */

/**
  r_MiterFloat is used for iterating through parts of
  multidimensional intervals with arbitrary stepping size using
  nearest neighbours. It is given the tile, the source domain
  and the destination domain
  Apart from that behaviour is exactly as in r_Miter.

*/
class r_MiterFloat
{
public:
    /// Constructor getting the source tile, the source domain and the destination domain
    r_MiterFloat(r_Bytes srcCellSize,
                 const char* srcTile,
                 const r_Minterval &tileDomain,
                 const r_Minterval &srcDomain,
                 const r_Minterval &destDomain);
    ~r_MiterFloat();

    /// iterator reset
    void reset();
    /// get the next cell
    inline char *nextCell();
    /// true if done
    inline bool isDone();

protected:
    struct iter_desc
    {
        r_FixedPointNumber min;
        r_FixedPointNumber pos;
        r_FixedPointNumber step;

        r_Range countSteps;
        r_Range maxSteps;

        r_Range dimStep;
        r_Range scaleStep;
        char    *cell;
    };

    char        *currentCell{NULL};
    const char  *firstCell{NULL};
    iter_desc   *iterDesc{NULL};
    r_Dimension dim{};
    bool done{false};
};

#include "miterf.icc"

#endif
