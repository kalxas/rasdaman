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

#ifndef TYPERESOLVERUTIL_HH
#define	TYPERESOLVERUTIL_HH

#include "relcatalogif/basetype.hh"
// GDAL headers
#include "gdal_priv.h"

/**
 * Helper class transforming the provided types into rasdaman types.
 */
class TypeResolverUtil {
public:
	TypeResolverUtil();

	/**
	 * Converts the GDALDataType to a literal rasdaman supported type.
	 * @param dataType The GDALDataType of the current raster band.
	 * @return A literal type supported by rasdaman.
	 */
	static const char* getLiteralTypeFromGDAL(GDALDataType dataType);
	/**
	 * Decides the base type the MDD should have before inserting in the database.
	 * The type decision is made based on the GDALDataType of the bands and of the number of bands.
	 *
	 * @param poDataSet GDALDataSet read from the temporary file.
	 * @return A BaseType for the inserted MDD.
	 */
	static BaseType* getBaseType(GDALDataset* poDataSet);

	virtual ~TypeResolverUtil();

private:

};

#endif	/* TYPERESOLVERUTIL_HH */
