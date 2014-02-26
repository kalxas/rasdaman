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

#ifndef _QTUTIL_
#define	_QTUTIL_

#include "raslib/error.hh"
#include "raslib/type.hh"
#include "config.h"

#include "raslib/minterval.hh"
#include "raslib/basetype.hh"
#include "relcatalogif/mddbasetype.hh"
#include <cpl_conv.h>

// GDAL headers
#include "gdal_priv.h"

#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

class QtEncodeDecode {
public:
	virtual ~QtEncodeDecode();
protected:
	QtEncodeDecode(char* format);
	QtEncodeDecode(char* format, char** fParams);

	void initParams(char* params);
	void setDouble(const char* paramName, double* value);
	void setString(const char* paramName, std::string* value);

	void setGDALParameters(GDALDataset *gdalDataSet, int width, int height, int nBands);

	char* format;
	char** fParams;

	struct GenericParams {
		double xmin;
		double xmax;
		double ymax;
		double ymin;

		std::string crs; // string representation of the coordinate reference system
		std::string metadata; // further metadata of the result
		std::vector<double> nodata; // nodata values of the result
	};

	GenericParams gParams;
};

#endif	/* QTUTIL_HH */
