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

#include "qlparser/gdaldataconverter.hh"

// GDAL headers
#include "ogr_spatialref.h"
#include "cpl_conv.h"
#include "cpl_string.h"
#include "vrtdataset.h"

GDALDataConverter::GDALDataConverter()
{
}

char* GDALDataConverter::getTileCells(GDALDataset *poDataset)
{
	GDALDataType dataType = poDataset->GetRasterBand(1)->GetRasterDataType();
	switch (dataType)
	{
		case GDT_Byte:
		{//   Eight bit unsigned integer
			return reolveCellsTemplate<r_Char>(poDataset);
		}
		case GDT_UInt16:
		{//     Sixteen bit unsigned integer
			return reolveCellsTemplate<r_UShort>(poDataset);
		}
		case GDT_Int16:
		{ //  Sixteen bit signed integer
			return reolveCellsTemplate<r_Short>(poDataset);
		}
		case GDT_UInt32:
		{//     Thirty two bit unsigned integer
			return reolveCellsTemplate<r_ULong>(poDataset);
		}
		case GDT_Int32:
		{ //  Thirty two bit signed integer
			return reolveCellsTemplate<r_Long>(poDataset);
		}
		case GDT_Float32:
		{//    Thirty two bit floating point
			return reolveCellsTemplate<r_Float>(poDataset);
		}
		case GDT_Float64:
		{//    Sixty four bit floating point
			return reolveCellsTemplate<r_Double>(poDataset);
		}
		default:
			throw r_Error(r_Error::r_Error_FeatureNotSupported);
			break;
	}
}

template<typename T>
char* GDALDataConverter::reolveCellsTemplate(GDALDataset* poDataset)
{
	int noOfBands = poDataset->GetRasterCount();
	int startCoordX = 0, startCoordY = 0, width = poDataset->GetRasterXSize(), height = poDataset->GetRasterYSize();
	T *tileCells = (T*) malloc(width * height * sizeof (T) * noOfBands);
	char *gdalBand = (char*) malloc(width * height * sizeof (T));
	if (gdalBand == NULL || tileCells == NULL)
	{
		throw r_Error(r_Error::r_Error_MemoryAllocation);
	}


	//copy data from all GDAL bands to rasdaman
	for (int band = 0; band < noOfBands; band++)
	{
		CPLErr error = poDataset->GetRasterBand(band + 1)->RasterIO(GF_Read, startCoordX, startCoordY, width, height, gdalBand, width, height, GDT_Byte, 0, 0);
		if (error != CE_None)
		{
			CPLError(error, 0, "");
			GDALClose(poDataset);
			free(gdalBand);
			throw r_Error(r_Error::r_Error_FeatureNotSupported);
		}

		//Transform the band data into rasdaman binary format
		char *pos = gdalBand;
		T *tilePos = tileCells + band;
		int tPos = 0 + band;
		for (int col = 0; col < width; col++)
		{
			for (int row = 0; row < height; row++, tilePos += noOfBands, tPos += noOfBands)
			{
				tilePos[0] = pos[row * width + col];
			}
		}
	}
	//Free resources
	GDALClose(poDataset);
	free(gdalBand);
	return (char *) tileCells;
}

GDALDataConverter::~GDALDataConverter()
{
}
