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


#ifndef _FORMATPARAMKEYS_HH_
#define _FORMATPARAMKEYS_HH_

#include <string>

namespace FormatParamKeys
{

namespace General
{
static const std::string TRANSPOSE{"transpose"};
static const std::string COLORMAP{"colorMap"};
static const std::string VARIABLES{"variables"};
static const std::string FORMAT_PARAMETERS{"formatParameters"};
static const std::string CONFIG_OPTIONS{"configOptions"};
static const std::string CONFIG_OPTIONS_LEGACY{"config"};
}

namespace Encode
{
static const std::string GEO_REFERENCE{"geoReference"};
static const std::string BBOX{"bbox"};
static const std::string XMIN{"xmin"};
static const std::string YMIN{"ymin"};
static const std::string XMAX{"xmax"};
static const std::string YMAX{"ymax"};
static const std::string CRS{"crs"};
static const std::string NODATA{"nodata"};
static const std::string METADATA{"metadata"};

namespace ColorMap
{
static const std::string TYPE{"type"};
static const std::string COLORTABLE{"colorTable"};
}

namespace NetCDF
{
static const std::string DIMENSIONS{"dimensions"};
static const std::string NAME{"name"};
static const std::string DATA{"data"};
static const std::string TYPE{"type"};
}

namespace CSV
{
static const std::string ORDER{"order"};
static const std::string ENABLE_NULL{"enableNull"};
static const std::string TRUE_VALUE{"trueValue"};
static const std::string FALSE_VALUE{"falseValue"};
static const std::string NULL_VALUE{"nullValue"};
static const std::string DIMENSION_START{"dimensionStart"};
static const std::string DIMENSION_END{"dimensionEnd"};
static const std::string DIMENSION_SEPARATOR{"dimensionSeparator"};
static const std::string VALUE_SEPARATOR{"valueSeparator"};
static const std::string COMPONENT_SEPARATOR{"componentSeparator"};
static const std::string STRUCT_VALUE_START{"structValueStart"};
static const std::string STRUCT_VALUE_END{"structValueEnd"};
static const std::string OUTER_DELIMITERS{"outerDelimiters"};
}

namespace GDAL
{
static const std::string GCPS{"GCPs"};
static const std::string GCP_ID{"id"};
static const std::string GCP_INFO{"info"};
static const std::string GCP_PIXEL{"pixel"};
static const std::string GCP_LINE{"line"};
static const std::string GCP_X{"x"};
static const std::string GCP_Y{"y"};
static const std::string GCP_Z{"z"};

static const std::string COLOR_PALETTE{"colorPalette"};
static const std::string PALETTE_INTERP{"paletteInterp"};
static const std::string PALETTE_INTERP_VAL_GRAY{"Gray"};
static const std::string PALETTE_INTERP_VAL_RGB{"RGB"};
static const std::string PALETTE_INTERP_VAL_CMYK{"CMYK"};
static const std::string PALETTE_INTERP_VAL_HLS{"HLS"};
static const std::string COLOR_TABLE{"colorTable"};
static const std::string COLOR_INTERP{"colorInterp"};
}

}

namespace Decode
{
static const std::string FILEPATHS{"filePaths"};
static const std::string SUBSET_DOMAIN{"subsetDomain"};
static const std::string INTERNAL_STRUCTURE{"internalStructure"};

namespace Grib
{
static const std::string MESSAGE_DOMAINS{"messageDomains"};
static const std::string MESSAGE_ID{"msgId"};
static const std::string MESSAGE_DOMAIN{"domain"};
}

namespace CSV
{
static const std::string DATA_DOMAIN{"domain"};
static const std::string BASETYPE{"basetype"};
}

}

}

#endif
