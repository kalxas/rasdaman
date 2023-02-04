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

#include "raslib/banditerator.hh"
#include "raslib/basetype.hh"
#include "raslib/structuretype.hh"
#include <cstring>

using namespace std;

r_Band_Iterator::r_Band_Iterator(const char *_data, const r_Base_Type *_type, 
                               r_Bytes _size, unsigned int _band, 
                               r_Band_Linearization _bandLinearization)
    : data{_data}, type{_type}, size{_size},
      band{_band}, bandLinearization{_bandLinearization}
{
  assert(data && type);
  
  cellSize = unsigned(type->size());
  dataEnd = data + (cellSize * size);
  
  bandOffset = 0;
  if (type->isStructType())
  {
    auto *stype = static_cast<const r_Structure_Type *>(type);
    for (unsigned int i = 0; i < band; ++i)
    {
      const r_Attribute &att = stype->resolve_attribute(i);
      bandOffset += att.type_of().size();
    }
    
    const r_Attribute &att = stype->resolve_attribute(band);
    cellBandSize = unsigned(att.type_of().size());
    
    if (bandLinearization == r_Band_Linearization::ChannelInterleaved)
    {
      // channel-interleaved: the band is a contiguous chunk following
      // the chunks of all previous bands
      bandOffset *= size;
      // the cell size can be set to the band size in this case, as each
      // cell of this band is contiguous one after another, and advancing
      // is done by adding cellSize to the currCell pointer
      cellSize = cellBandSize;
    }
  }
  else
  {
    cellBandSize = unsigned(type->size());
  }
  reset();
}

void r_Band_Iterator::copyBand(char* __restrict__ dst)
{
  assert(dst);
  
  const char* src = data + bandOffset;
  
  if (bandLinearization == r_Band_Linearization::ChannelInterleaved || !type->isStructType())
  {
    // channel-interleaved
    memcpy(dst, src, getBandSize());
  }
  else
  {
    // pixel-interleaved
    if (cellBandSize == 1)
    {
      // optimization for 1-byte bands
      for (size_t i = 0; i < size; ++i, src += cellSize, ++dst)
        *dst = *src;
    }
    else
    {
      // multi-byte copying with memcpy
      for (size_t i = 0; i < size; ++i, src += cellSize, dst += cellBandSize)
        memcpy(dst, src, cellBandSize);
    }
  }
}

r_Bytes r_Band_Iterator::getBandSize() const
{
  return size * cellBandSize;
}
