/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#include "turboqueryresult.hh"

TurboQueryResult::TurboQueryResult(char *rawData, size_t rawDataSize, r_Data_Format data_format, std::string domain):
    rawData(rawData),
    rawDataSize(rawDataSize),
    data_format(data_format),
    domain(domain)
{}

TurboQueryResult::~TurboQueryResult()
{

}

char *TurboQueryResult::getRawData()
{
    return rawData;
}

std::string TurboQueryResult::getDomain()
{
    return domain;
}

r_Data_Format TurboQueryResult::getDataFormat()
{
    return data_format;
}

size_t TurboQueryResult::getRawDataSize() const
{
    return rawDataSize;
}

