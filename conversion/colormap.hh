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
* Copyright 2003 - 2019 Peter Baumann / rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

#ifndef R_CONV_COLORMAP_HH
#define R_CONV_COLORMAP_HH

#include <vector>
#include <string>
#include <map>
#include <unordered_map>

#include "raslib/minterval.hh"
#include "raslib/basetype.hh"
#include "raslib/type.hh"

class r_ColorMap
{
public:
    r_ColorMap();

    enum Type
    {
        VALUES,
        INTERVALS,
        RAMP
    };

    const char *applyColorMap(const r_Type *srcType, const char *srcData, const r_Minterval &dimData, int &baseType);

    void setColorMapType(r_ColorMap::Type type);
    
    void setColorTable(std::map<double, std::vector<unsigned char>> colorTableMap);

    void setUColorTable(std::unordered_map<double, std::vector<unsigned char>> uColorTableMap);
private:
    r_ColorMap::Type colorMapType;

    /// pixelValues as keys and colorValues as values
    std::map<double, std::vector<unsigned char>> colorTable;

    /// same colorTable, but in an unordered_map
    std::unordered_map<double, std::vector<unsigned char>> uColorTable;

    template <class T>
    const char *prepareColorMap(const char *srcData, const r_Minterval &dimData);

    template <class T>
    const char *applyValuesColorMap(const char *srcData, size_t nrPixels);

    template <class T>
    const char *applyIntervalsColorsMap(const char *srcData, size_t nrPixels);

    template <class T>
    const char *applyRampColorMap(const char *srcData, size_t nrPixels);
};

#endif /* R_CONV_COLORMAP_HH */