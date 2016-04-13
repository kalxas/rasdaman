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
 * MERCHANTrABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

#include "qlparser/gdalincludes.hh"
#include "qlparser/gdaldataconverter.hh"
#include "mymalloc/mymalloc.h"


GDALDataConverter::GDALDataConverter()
{
}

void GDALDataConverter::getTileCells(GDALDataset* poDataset, /* out */ r_Bytes& size, /* out */ char*& contents)
{
	GDALDataType dataType = poDataset->GetRasterBand(1)->GetRasterDataType();
	switch (dataType)
	{
		case GDT_Byte:
		{//   Eight bit unsigned integer
            return resolveTileCellsByType<r_Char>(poDataset, size, contents);
		}
		case GDT_UInt16:
		{//     Sixteen bit unsigned integer
            return resolveTileCellsByType<r_UShort>(poDataset, size, contents);
		}
		case GDT_Int16:
		{ //  Sixteen bit signed integer
            return resolveTileCellsByType<r_Short>(poDataset, size, contents);
		}
		case GDT_UInt32:
		{//     Thirty two bit unsigned integer
            return resolveTileCellsByType<r_ULong>(poDataset, size, contents);
		}
		case GDT_Int32:
		{ //  Thirty two bit signed integer
            return resolveTileCellsByType<r_Long>(poDataset, size, contents);
		}
		case GDT_Float32:
		{//    Thirty two bit floating point
            return resolveTileCellsByType<r_Float>(poDataset, size, contents);
		}
		case GDT_Float64:
		{//    Sixty four bit floating point
            return resolveTileCellsByType<r_Double>(poDataset, size, contents);
		}
		default:
			throw r_Error(r_Error::r_Error_FeatureNotSupported);
			break;
	}
}

template<typename T>
void GDALDataConverter::resolveTileCellsByType(GDALDataset* poDataset, /* out */ r_Bytes& size, /* out */ char*& contents)
{
	int noOfBands = poDataset->GetRasterCount();
	int startCoordX = 0, startCoordY = 0, width = poDataset->GetRasterXSize(), height = poDataset->GetRasterYSize();
    GDALDataType gdalType = poDataset->GetRasterBand(1)->GetRasterDataType();

    size = static_cast<r_Bytes>(width) * static_cast<r_Bytes>(height) * static_cast<r_Bytes>(noOfBands) * sizeof(T);

    T *tileCells = (T*) mymalloc(size);
    contents = (char *) tileCells;

    T *gdalBand = (T*) mymalloc(static_cast<r_Bytes>(width) * static_cast<r_Bytes>(height) * sizeof(T));

	if (gdalBand == NULL || tileCells == NULL)
	{
		throw r_Error(r_Error::r_Error_MemoryAllocation);
	}


	//copy data from all GDAL bands to rasdaman
	for (int band = 0; band < noOfBands; band++)
	{
        CPLErr error = poDataset->GetRasterBand(band + 1)->RasterIO(GF_Read, startCoordX, startCoordY, width, height, gdalBand, width, height, gdalType, 0, 0);
		if (error != CE_None)
		{
			CPLError(error, 0, "Error copying band to rasdaman.");
			GDALClose(poDataset);
			free(gdalBand);
			throw r_Error(r_Error::r_Error_FeatureNotSupported);
		}

		//Transform the band data into rasdaman binary format
        T *pos = gdalBand;
		T *tilePos = tileCells + band;
		int tPos = 0 + band;
		for (int col = 0; col < width; col++)
		{
			for (int row = 0; row < height; row++, tilePos += noOfBands, tPos += noOfBands)
			{
				*tilePos = *(pos + (row * width + col));
			}
		}
	}
	//Free resources
	GDALClose(poDataset);
    free(gdalBand);
}

GDALDataConverter::~GDALDataConverter()
{
}
