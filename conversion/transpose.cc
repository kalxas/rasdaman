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
 * INCLUDE: convertor.hh
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Convertor, r_Convert_Memory
 *
  * COMMENTS:
 *
 * Provides interface to convert data to other formats.
 *
*/
#include "transpose.hh"
#include "raslib/error.hh"
#include <logging.hh>


void transposeLastTwo(char *data, r_Minterval &dimData, const r_Type *dataType)
{
    LDEBUG << "transposing last two dimensions...";
    const auto dim = dimData.dimension();
    size_t typeSize = static_cast<r_Base_Type *>(const_cast<r_Type *>(dataType))->size();
    //the number of 2D slices we need to transpose
    size_t s = 1;
    for (size_t i = 0; i < static_cast<size_t>(dim - 2); i++)
    {
        s *= static_cast<size_t>(dimData[i].get_extent());
    }
    //the number of rows in the 2d slices
    size_t n = static_cast<size_t>(dimData[dim - 2].get_extent());
    //the number of columns in the 2d slices
    size_t m = static_cast<size_t>(dimData[dim - 1].get_extent());
    //a relatively small placeholder for storing the locally transposed data
    const size_t sliceSize = m * n * typeSize;
    std::unique_ptr<char[]> dataTempPtr;
    dataTempPtr.reset(new char [sliceSize]);
    char *dataTemp = dataTempPtr.get();
    //a loop for changing each 2D data slice
    for (size_t v = 0; v < s; v++)
    {
        //change the current 2D data slice
        for (size_t k = 0; k < m * n; k++)
        {
            //column
            size_t col = k / n;
            //row
            size_t row = k % n;
            memcpy(dataTemp + k * typeSize,
                   data + (m * row * typeSize) + col * typeSize,
                   typeSize);
        }
        //copy transposed data back to original ptr
        memcpy(data, dataTemp, sliceSize);
        //move to the next 2D data slice
        data += sliceSize;
    }
    //swap the index assignments in the corresponding r_Minterval
    dimData.transpose(dim - 2, dim - 1);
}

void transpose(char *data, r_Minterval &dimData, const r_Type *dataType, const std::pair<int, int> transposeParams)
{
    int dims = static_cast<int>(dimData.dimension());
    int tParam0 = std::get<0>(transposeParams);
    int tParam1 = std::get<1>(transposeParams);
    LDEBUG << "dims: " << dims << ", transpose dim 1: " << tParam0 << ", transpose dim 2: " << tParam1;

    if ((dims - 1 == tParam0 || dims - 1 == tParam1)
            && (dims - 2 == tParam0 || dims - 2 == tParam1)
            && (tParam0 != tParam1))
    {
        transposeLastTwo(data, dimData, dataType);
    }
    else
    {
        LERROR << "Selected transposition dimensions do not coincide with the last two MDD dimensions.";
        throw r_Error(TRANSPOSEPARAMETERSINVALID);
    }
}