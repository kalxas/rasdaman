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
#include <easylogging++.h>

int dataTypeSize(const r_Type* base_type){
    //size of the data type, used for offset computation in memcpy
    r_Base_Type* st = static_cast<r_Base_Type*>(const_cast<r_Type*>(base_type));
    return (int) st->size();
}

void transposeLastTwo(char* data, r_Minterval& dimData, const r_Type* dataType) {
    int sizeOfDataType = dataTypeSize(dataType);
    //the number of 2D slices we need to transpose
    unsigned int s = 1;
    for (unsigned int i = 0; i < dimData.dimension() - 2; i++) {
        s *= (dimData[i].get_extent());
    }    
    //the number of rows in the 2d slices
    int n = dimData[dimData.dimension() - 2].get_extent();
    //the number of columns in the 2d slices
    int m = dimData[dimData.dimension() - 1].get_extent();
    //a relatively small placeholder for storing the locally transposed data
    char* dataTemp;
    dataTemp = new char [m*n*sizeOfDataType];
    //a loop for changing each 2D data slice
    for (unsigned int v = 0; v < s; v++) {
        //change the current 2D data slice
        for (int k = 0; k < m * n; k++) {
            //column
            int a = k / n;
            //row
            int b = k % n;
            for(int l = 0; l < sizeOfDataType; l++){
                dataTemp[k * sizeOfDataType + l] = data[(m * b * sizeOfDataType) + a * sizeOfDataType + l];
            }
        }
        for (int k = 0; k < m * n; k++) {
            for(int l = 0; l < sizeOfDataType; l++){
                data[k * sizeOfDataType + l] = dataTemp[k * sizeOfDataType + l];
            }
        }

        //move to the next 2D data slice
        data += m*n*sizeOfDataType;
    }   
    //cleanup that small placeholder
    delete [] dataTemp;
    //swap the index assignments in the corresponding r_Minterval
    dimData.transpose(dimData.dimension()-2, dimData.dimension()-1);
}

void transpose(char* data, r_Minterval& dimData, const r_Type* dataType, const std::pair<int, int> transposeParams) throw (r_Error)
{
    int dims = static_cast<int>(dimData.dimension());
    int tParam0 = std::get<0>(transposeParams);
    int tParam1 = std::get<1>(transposeParams);
    
    if( ( dims-1 == tParam0 || dims-1 == tParam1 ) 
     && ( dims-2 == tParam0 || dims-2 == tParam1 ) 
     && ( tParam0 != tParam1 ) )
    {
        transposeLastTwo(data, dimData, dataType);
    }
    else
    {
        LERROR << "Selected transposition dimensions do not coincide with the last two dimensions of your MDD.";
        throw r_Error(TRANSPOSEPARAMETERSINVALID);
    }
}