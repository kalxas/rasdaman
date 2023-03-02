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
* Copyright 2003-2016 Peter Baumann / rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/**
 * SOURCE: json.cc
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_JSON
 *
 * COMMENTS:
 *
 * Provides functions to convert data to JSON and back.
 *
*/

#include "conversion/json.hh"

using namespace std;

void r_Conv_JSON::initJSON(void)
{
    trueValue = "true";
    falseValue = "false";
    nullValue = "null";
    dimensionStart = "[";
    dimensionEnd = "]";
    dimensionSeparator = ",";
    valueSeparator = ",";
    componentSeparator = " ";
    structValueStart = "\"";
    structValueEnd = "\"";
    outerDelimiters = true;
    enableNull = true;
}

r_Conv_JSON::r_Conv_JSON(const char *src, const r_Minterval &interv, const r_Type *tp)
    : r_Conv_CSV(src, interv, tp)
{
    initJSON();
}

r_Conv_JSON::r_Conv_JSON(const char *src, const r_Minterval &interv, int tp)
    : r_Conv_CSV(src, interv, tp)
{
    initJSON();
}

r_Conv_Desc &r_Conv_JSON::convertTo(const char *options,
                                    const r_Range *nullVal)
{
    return r_Conv_CSV::convertTo(options, nullVal);
}

r_Conv_Desc &r_Conv_JSON::convertFrom(const char *options)
{
    return r_Conv_CSV::convertFrom(options);
}

r_Conv_Desc &r_Conv_JSON::convertFrom(r_Format_Params options)
{
    return r_Conv_CSV::convertFrom(options);
}

const char *r_Conv_JSON::get_name(void) const
{
    return format_name_json;
}

r_Data_Format r_Conv_JSON::get_data_format(void) const
{
    return r_JSON;
}

r_Convertor *r_Conv_JSON::clone(void) const
{
    return new r_Conv_JSON(desc.src, desc.srcInterv, desc.baseType);
}
