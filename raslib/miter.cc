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
 * SOURCE: miter.cc
 *
 * MODULE: raslib
 * CLASS:  r_Miter
 *
*/

#include "raslib/miter.hh"
#include "raslib/minterval.hh"
//#include <logging.hh>

r_Miter::r_Miter(const r_Minterval *newAreaIter,
                 const r_Minterval *newAreaTile,
                 r_Bytes newCellSize, const char *newFirstCell)
    : areaIter(newAreaIter), areaTile(newAreaTile),
      firstCell(newFirstCell), cellSize(newCellSize), done(false)
{
    // the following initializes incArrIter and calculates the first offset
    size_t tIncIter = 1;        // total increment for current dimension
    size_t prevTIncIter = 1;    // total increment for previous dimension
    size_t incIter = cellSize;  // current increment
    size_t firstOff = 0;

    //LTRACE << "area for iteration: " << *newAreaIter;
    //LTRACE << "whole area: " << *newAreaTile;

    // dimensionality of both areaIter and areaTile
    r_Dimension dim = areaIter->dimension();
    // stores the increments
    incArrIter = new incArrElem[dim];

    for (r_Dimension i = 0; i < dim; ++i)
    {
        // in RasDaMan the order of dimensions is the other way round!
        r_Dimension r = dim - 1 - i;
        // used for counting in iteration, initialize with 0
        incArrIter[i].curr = 0;
        // how often is the increment added?
        incArrIter[i].repeat = areaIter->at_unsafe(r).high() - areaIter->at_unsafe(r).low() + 1;
        //LTRACE << "repeat dim " << i << ": " << incArrIter[i].repeat ;
        // the increment for the result tile (higher dimensions calculated further down)
        incArrIter[i].inc = static_cast<int>(incIter);
        //LTRACE << "incIter dim " << i << ": " << incIter;

        // calculate starting offset and increments for higher dimensions
        //
        // firstOff is the offset in chars of the first cell
        firstOff += static_cast<size_t>(areaIter->at_unsafe(r).low() - areaTile->at_unsafe(r).low()) * prevTIncIter * cellSize;
        // tInc is the increment if the dimension would be skipped
        tIncIter = static_cast<size_t>(areaTile->at_unsafe(r).high() - areaTile->at_unsafe(r).low() + 1) * prevTIncIter;
        // inc is the real increment, after some cells in the dimensions have been iterated through.
        incIter = (tIncIter - static_cast<size_t>(incArrIter[i].repeat) * prevTIncIter) * cellSize;
        // remember total increment of last dimension
        prevTIncIter = tIncIter;
    }
    firstCell += firstOff;
    reset();
}

r_Miter::~r_Miter()
{
    delete[] incArrIter;
}

void r_Miter::reset()
{
    currCell = const_cast<char *>(firstCell);
    done = false;
    lowCount = 0;
    for (r_Dimension i = 0; i < areaIter->dimension(); i++)
        incArrIter[i].curr = 0;
}
