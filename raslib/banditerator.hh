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

#ifndef _BAND_ITERATOR_
#define _BAND_ITERATOR_

#include "raslib/error.hh"
#include "raslib/mddtypes.hh"
#include <assert.h>

class r_Base_Type;

//@ManMemo: Module: {\bf rasodmg}


/**
  This class abstracts away iteration over the cells of a (multi-band) array.
  Bands in pixel-interleaved and channel-interleaved linearizations are 
  supported.
  
  It's different from r_Miter* in raslib, which focuses on full cell/dimension
  iteration.
*/

/**
  * \ingroup Rasodmgs
  */
class r_Band_Iterator
{
public:
    /// @param _data the array cells
    /// @param _type the array base type
    /// @param _size number of cells in the array
    /// @param _band the band to be iterated
    /// @param _bandLinearization the band interleaving (pixel/channel)
    r_Band_Iterator(const char* _data, const r_Base_Type* _type, r_Bytes _size,
                   unsigned int _band, r_Band_Linearization _bandLinearization);

    /// destructor
    virtual ~r_Band_Iterator() = default;
    
    /// @return true if all band cells have been iterated. If this method
    /// returns true, then calling `advance()` or `get()` is undefined behavior.
    bool done() const {
      return currCell == dataEnd;
    }
    
    /// Move to the next band cell. Calling this method when `done()` returns
    /// true leads to undefined behaviour.
    void advance() {
      assert(currCell != dataEnd);
      currCell += cellSize;
    }
    
    /// Move by cellCount cells in the band. cellCount must be less than the
    /// remaining number of cells in this band.
    void advance(r_Bytes cellCount) {
      assert(currCell + (cellSize * cellCount) <= dataEnd);
      currCell += (cellSize * cellCount);
    }
    
    /// @return a pointer to the current cell.
    /// No check is performed on whether the returned pointer is at the end
    /// of the array, so before calling make sure to check with a call to `done()`.
    const char *get() {
      assert(currCell != dataEnd);
      return currCell;
    }
    
    /// @return a pointer to the targetCell in the band.
    /// targetCell must be less than the total number of cells in the array,
    /// otherwise this method will lead to undefined behavior.
    const char *get(r_Bytes targetCell) {
      assert(targetCell < size);
      return (data + bandOffset) + (targetCell * cellSize);
    }
    
    void reset() {
      currCell = data + bandOffset;
    }
    
    /// Copy the band data to the given dst pointer; dst must not be null, and
    /// must be sufficiently large (use `getBandSize()` to determine the size).
    void copyBand(char* __restrict__ dst);
    
    /// Return the size of band data in bytes.
    r_Bytes getBandSize() const;

protected:
    
    /// array data
    const char* data{NULL};
    
    /// current cell in the array to be returned by dereferencing the iterator
    const char* currCell{NULL};
    
    /// end of data = data + (size * cellSize)
    const char* dataEnd{NULL};
    
    /// array cell type
    const r_Base_Type *type{NULL};
    
    /// array size in number of cells
    r_Bytes size{};
    
    /// offset in bytes to the first cell in data of the iterated band
    r_Bytes bandOffset{};
    
    /// the band to be iterated
    unsigned int band{};
    
    /// size of the band component of one cell in bytes
    unsigned int cellBandSize{};
    
    /// full cell size in bytes
    unsigned int cellSize{1};
    
    /// band linearization (pixel/channel)
    r_Band_Linearization bandLinearization{r_Band_Linearization::PixelInterleaved};
};

#endif
