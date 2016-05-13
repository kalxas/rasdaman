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
#include "raslib/mddtypes.hh"
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

#ifndef STR_EQUAL
#define STR_EQUAL(a, b) (strcmp(a, b) == 0)
#endif

r_Data_Format
GDALDataConverter::getDataFormat(char* formatArg)
{
	r_Data_Format ret = r_Array;

	if (formatArg)
	{
		char* f = strdup(formatArg);
		for (int i = 0; formatArg[i]; i++)
		{
			if (isalpha(formatArg[i]))
				f[i] = tolower(formatArg[i]);
		}

		if (STR_EQUAL(f, "png"))
			ret = r_PNG;
		else if (STR_EQUAL(f, "netcdf"))
			ret = r_NETCDF;
		else if (STR_EQUAL(f, "gtiff") || STR_EQUAL(f, "tiff"))
			ret = r_TIFF;
		else if (STR_EQUAL(f, "jpeg"))
			ret = r_JPEG;
		else if (STR_EQUAL(f, "jpeg2000") || STR_EQUAL(f, "jp2openjpeg"))
			ret = r_JP2;
		else if (STR_EQUAL(f, "nitf"))
			ret = r_NTF;
		else if (STR_EQUAL(f, "hdf") || STR_EQUAL(f, "hdf4") || STR_EQUAL(f, "hdf4image") || STR_EQUAL(f, "hdf5"))
			ret = r_HDF;
		else if (STR_EQUAL(f, "bmp"))
			ret = r_BMP;
		else if (STR_EQUAL(f, "csv"))
			ret = r_CSV;
		else if (STR_EQUAL(f, "grib"))
			ret = r_GRIB;
		free(f);
	}
	return ret;
}

r_Data_Format
GDALDataConverter::guessDataFormat(char* data, r_Bytes dataSize)
{
	r_Data_Format ret = r_Array;

	return ret;
}

GDALDataConverter::~GDALDataConverter()
{
}
