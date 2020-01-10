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

#include "raslib/minterval.hh"
#include "raslib/basetype.hh"
#include "raslib/type.hh"

#include <vector>
#include <string>
#include <map>
#include <unordered_map>
#include <memory>

using ColorTableMap = std::map<double, std::vector<unsigned char>>;
using ColorTableUMap = std::unordered_map<double, std::vector<unsigned char>>;

class r_ColorMap
{
public:
    enum Type
    {
        VALUES,
        INTERVALS,
        RAMP
    };

    r_ColorMap();

    std::unique_ptr<unsigned char[]> applyColorMap(
        const r_Type* srcType, const char* srcData, const r_Minterval& dimData, int& baseType);

    void setColorMapType(r_ColorMap::Type type);

    void setColorTable(ColorTableMap colorTableMap);

    void setUColorTable(ColorTableUMap uColorTableMap);

    size_t getResultBandNumber() const;
private:
    r_ColorMap::Type colorMapType{r_ColorMap::Type::VALUES};

    /// pixelValues as keys and colorValues as values
    ColorTableMap colorTable;

    /// same colorTable, but in an unordered_map
    ColorTableUMap uColorTable;

    template <class T>
    void applySpecificColorMap(const T* srcData, size_t nrPixels, size_t nrBands, unsigned char* res);

    template <class T>
    void applyValuesColorMap(const T* srcData, size_t nrPixels, size_t nrBands, unsigned char* res);

    template <class T>
    void applyIntervalsColorMap(const T* srcData, size_t nrPixels, size_t nrBands, unsigned char* res, bool ramp);

    const std::vector<unsigned char>* getUColor(double curr) const;
};

#endif /* R_CONV_COLORMAP_HH */
