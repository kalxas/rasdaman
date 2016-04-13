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

#include "qlparser/typeresolverutil.hh"
#include "catalogmgr/typefactory.hh"
#include "relcatalogif/structtype.hh"




TypeResolverUtil::TypeResolverUtil()
{
}

const char* TypeResolverUtil::getLiteralTypeFromGDAL(GDALDataType dataType)
{
	switch (dataType)
	{
		case GDT_Byte:
			//r_type::CHAR || r_type::BOOL
			return "Char";
			break;

		case GDT_UInt16:
			//r_type::USHORT
			return "UShort";
			break;
		case GDT_Int16:
			//r_type::SHORT
			return "Short";
			break;
		case GDT_UInt32:
			//r_type::ULONG
			return "ULong";
			break;
		case GDT_Int32:
			//r_type::LONG
			return "Long";
			break;
		case GDT_Float32:
			//r_type::FLOAT
			return "Float";
			break;
		case GDT_Float64:
			//r_type::DOUBLE
			return "Double";
			break;
		default:
			throw r_Error(r_Error::r_Error_FeatureNotSupported);
			break;
	}
}

BaseType* TypeResolverUtil::getBaseType(GDALDataset* poDataSet)
{

	int nBands = poDataSet->GetRasterCount();
	BaseType* baseType = NULL;
	if (nBands == 1)
	{//base type
		GDALDataType dataType = poDataSet->GetRasterBand(1)->GetRasterDataType();
		baseType = const_cast<BaseType*>(TypeFactory::mapType(getLiteralTypeFromGDAL(dataType)));
	} else if (nBands > 1)
	{//struct type
		StructType* tmpStructType = new StructType("tmp_str_name", static_cast<unsigned int>(nBands));

		for (int band = 1; band <= nBands; ++band)
		{
			GDALDataType dataType = poDataSet->GetRasterBand(band)->GetRasterDataType();
			const char* literalType = getLiteralTypeFromGDAL(dataType);
			char elementName[20];
			memset(elementName, '\0', 20);
			sprintf(elementName, "band%d", band);
			tmpStructType->addElement(elementName, literalType);
		}
		TypeFactory::addTempType(tmpStructType);
		baseType = tmpStructType;

	}

	if (baseType == NULL)
	{
		throw r_Error::r_Error_FeatureNotSupported;
	}

	return baseType;
}

TypeResolverUtil::~TypeResolverUtil()
{
}
