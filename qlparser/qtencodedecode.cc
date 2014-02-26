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

#include "qlparser/qtencodedecode.hh"

#include "config.h"
#include "raslib/rmdebug.hh"
#include "debug.hh"
#include <float.h>

// GDAL headers
#include "ogr_spatialref.h"
#include "cpl_conv.h"
#include "cpl_string.h"
#include "vrtdataset.h"


#include "catalogmgr/typefactory.hh"
#include "tilemgr/tile.hh"
#include "debug/debug-srv.hh"
#include "raslib/rmdebug.hh"
#include "raslib/odmgtypes.hh"

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

#ifndef GDAL_PARAMS
#define GPDAL_PARAMS true

#define PARAM_XMIN "xmin"
#define PARAM_XMAX "xmax"
#define PARAM_YMIN "ymin"
#define PARAM_YMAX "ymax"

#define PARAM_CRS  "crs"
#define PARAM_METADATA "metadata"
#define PARAM_NODATA "nodata"

#define NODATA_VALUE_SEPARATOR " ,"
#define NODATA_DEFAULT_VALUE 0.0
#endif

QtEncodeDecode::QtEncodeDecode(char* format)
{
	this->format = format;
}

QtEncodeDecode::QtEncodeDecode(char* format, char** fParams)
{
	this->format = format;
	this->fParams = NULL;
}

QtEncodeDecode::~QtEncodeDecode()
{
	CSLDestroy(fParams);
}

void
QtEncodeDecode::initParams(char* paramsIn)
{
	// replace escaped characters
	string params("");
	int i = 0;
	while (paramsIn[i] != '\0')
	{
		char curr = paramsIn[i];
		char next = paramsIn[i + 1];
		++i;

		if (curr == '\\' && (next == '"' || next == '\'' || next == '\\'))
			continue;
		params += curr;
	}

	fParams = CSLTokenizeString2(params.c_str(), ";",
			CSLT_STRIPLEADSPACES |
			CSLT_STRIPENDSPACES);

	setDouble(PARAM_XMIN, &gParams.xmin);
	setDouble(PARAM_XMAX, &gParams.xmax);
	setDouble(PARAM_YMIN, &gParams.ymin);
	setDouble(PARAM_YMAX, &gParams.ymax);
	setString(PARAM_CRS, &gParams.crs);
	setString(PARAM_METADATA, &gParams.metadata);

	string nodata;
	setString(PARAM_NODATA, &nodata);

	if (!nodata.empty())
	{
		char* pch = (char*) nodata.c_str();
		pch = strtok(pch, NODATA_VALUE_SEPARATOR);
		while (pch != NULL)
		{
			double value = strtod(pch, NULL);
			gParams.nodata.push_back(value);
			pch = strtok(NULL, NODATA_VALUE_SEPARATOR);
		}
	}
}

void
QtEncodeDecode::setDouble(const char* paramName, double* value)
{
	int ind;
	if ((ind = CSLFindName(fParams, paramName)) != -1)
		*value = strtod(CSLFetchNameValue(fParams, paramName), NULL);
	else
		*value = DBL_MAX;
}

void
QtEncodeDecode::setString(const char* paramName, string* value)
{
	int ind;
	if ((ind = CSLFindName(fParams, paramName)) != -1)
		*value = CSLFetchNameValue(fParams, paramName);
	else
		*value = "";
}

void
QtEncodeDecode::setGDALParameters(GDALDataset *gdalDataSet, int width, int height, int nBands)
{
	if (gParams.xmin != DBL_MAX && gParams.xmax != DBL_MAX && gParams.ymin != DBL_MAX && gParams.ymax != DBL_MAX)
	{
		double adfGeoTransform[6];
		adfGeoTransform[0] = gParams.xmin;
		adfGeoTransform[1] = (gParams.xmax - gParams.xmin) / width;
		adfGeoTransform[2] = 0.0;
		adfGeoTransform[3] = gParams.ymax;
		adfGeoTransform[4] = 0.0;
		adfGeoTransform[5] = -(gParams.ymax - gParams.ymin) / height;
		gdalDataSet->SetGeoTransform(adfGeoTransform);
	}

	if (gParams.crs != "")
	{
		OGRSpatialReference srs;

		// setup input coordinate system. Try import from EPSG, Proj.4, ESRI and last, from a WKT string
		const char *crs = gParams.crs.c_str();
		char *wkt = NULL;

		OGRErr err = srs.SetFromUserInput(crs);
		if (err != OGRERR_NONE)
		{
			RMInit::logOut << "QtEncode::convertTileToDataset - Warning: GDAL could not understand coordinate reference system: '" << crs << "'" << endl;
		} else
		{
			srs.exportToWkt(&wkt);
			gdalDataSet->SetProjection(wkt);
		}
	}

	if (gParams.metadata != "")
	{
		char** metadata = NULL;
		metadata = CSLAddNameValue(metadata, "metadata", gParams.metadata.c_str());
		gdalDataSet->SetMetadata(metadata);
	}


	// set nodata value
	if (gParams.nodata.empty())
	{
		// if no nodata is specified, set default -- DM 2013-oct-01, ticket 477
		gParams.nodata.push_back(NODATA_DEFAULT_VALUE);
	}
	if (gParams.nodata.size() > 0)
	{
		for (int band = 0; band < nBands; band++)
		{
			GDALRasterBand* rasterBand = gdalDataSet->GetRasterBand(band + 1);

			// if only one value is provided use the same for all bands
			if (gParams.nodata.size() == 1)
			{
				rasterBand->SetNoDataValue(gParams.nodata.at(0));
			} else if (gParams.nodata.size() == nBands)
			{
				rasterBand->SetNoDataValue(gParams.nodata.at(band));
			} else
			{
				// warning, nodata value no != band no -- DM 2012-dec-10
				RMInit::logOut << "Warning: ignored setting NODATA value, number of NODATA values (" <<
						gParams.nodata.size() << ") doesn't match the number of bands (" << nBands << ")." << endl;
				break;
			}
		}
	}
}
