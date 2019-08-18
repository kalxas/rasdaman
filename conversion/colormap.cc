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

#include "colormap.hh"
#include "conversion/convtypes.hh"
#include "raslib/odmgtypes.hh"
#include <logging.hh>
#include <cmath>

r_ColorMap::r_ColorMap()
{}

void r_ColorMap::setColorMapType(r_ColorMap::Type type)
{
    colorMapType = type;
}

void r_ColorMap::setColorTable(ColorTableMap colorTableMap)
{
    this->colorTable = std::move(colorTableMap);
}

void r_ColorMap::setUColorTable(ColorTableUMap uColorTableMap)
{
    this->uColorTable = std::move(uColorTableMap);
}

size_t r_ColorMap::getResultBandNumber() const {
    return colorTable.begin()->second.size();
}

const char *r_ColorMap::applyColorMap(const r_Type *srcType, const char *srcData, const r_Minterval &dimData, int &baseType)
{
    auto nrBands = getResultBandNumber();
    switch (nrBands)
    {
    case 1: baseType = ctype_char; break;
    case 3: baseType = ctype_rgb; break;
    case 4: baseType = ctype_struct; break;
    default:
        LERROR << "Unsupported number of bands in color map: " << nrBands;
        throw r_Error(r_Error::r_Error_Conversion);
    }
    size_t nrPixels = dimData.cell_count();
    
    LDEBUG << "Applying color map to image with " << nrBands << " bands.";
    
    unsigned char *img = new unsigned char[nrBands * nrPixels];

    switch (srcType->type_id())
    {
    case r_Type::ULONG:
        return applySpecificColorMap<r_ULong>(srcData, nrPixels, nrBands, img);
    case r_Type::LONG:
        return applySpecificColorMap<r_Long>(srcData, nrPixels, nrBands, img);
    case r_Type::CHAR:
        return applySpecificColorMap<r_Char>(srcData, nrPixels, nrBands, img);
    case r_Type::USHORT:
        return applySpecificColorMap<r_UShort>(srcData, nrPixels, nrBands, img);
    case r_Type::SHORT:
        return applySpecificColorMap<r_Short>(srcData, nrPixels, nrBands, img);
    case r_Type::OCTET:
        return applySpecificColorMap<r_Octet>(srcData, nrPixels, nrBands, img);
    case r_Type::FLOAT:
        return applySpecificColorMap<r_Float>(srcData, nrPixels, nrBands, img);
    case r_Type::DOUBLE:
        return applySpecificColorMap<r_Double>(srcData, nrPixels, nrBands, img);
    default:
        LERROR << "Unsupported base type " << srcType->type_id() << ", cannot perform color mapping.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
}

template <class T>
const char *r_ColorMap::applySpecificColorMap(const char *data, size_t nrPixels, size_t nrBands, unsigned char* res)
{
    auto *srcData = reinterpret_cast<const T*>(data);
    switch (colorMapType)
    {
    case r_ColorMap::Type::VALUES:
        return applyValuesColorMap<T>(srcData, nrPixels, nrBands, res);
    case r_ColorMap::Type::INTERVALS:
        return applyIntervalsColorMap<T>(srcData, nrPixels, nrBands, res, false);
    case r_ColorMap::Type::RAMP:
        return applyIntervalsColorMap<T>(srcData, nrPixels, nrBands, res, true);
    default:
        return data;
    }
}

template <class T>
const char *r_ColorMap::applyValuesColorMap(const T *data, size_t nrPixels, size_t nrBands, unsigned char* res)
{
    auto *p = res;
    for (size_t i = 0; i < nrPixels; i++)
    {
        auto el = uColorTable.find(data[i]);
        if (el != uColorTable.end())
        {
            for (size_t j = 0; j < nrBands; ++j, ++p)
            {
                *p = el->second[j];
            }
        }
        else
        {
            std::fill(p, p + nrBands, 0);
            p += nrBands;
        }
    }
    return reinterpret_cast<const char*>(res);
}

const std::vector<unsigned char> *r_ColorMap::getUColor(double curr) const
{
    auto it = uColorTable.find(curr);
    return it != uColorTable.end() ? &it->second : nullptr;
}

template <class T>
const char *r_ColorMap::applyIntervalsColorMap(const T *data, size_t nrPixels, size_t nrBands, unsigned char* res, bool ramp)
{
    auto *p = res;
    
    double min = colorTable.begin()->first;
    double max = colorTable.rbegin()->first;

    for (size_t i = 0; i < nrPixels; ++i)
    {
        double curr = static_cast<double>(data[i]);
        
        // Get target color if it can be selected from exact values in the color map
        const std::vector<unsigned char> *color = nullptr;
        if (curr < min)
            color = &colorTable.begin()->second;
        else if (curr > max)
            color = &colorTable.rbegin()->second;
        else
            color = getUColor(curr);
        
        if (color != nullptr)
        {
            // Value matches exactly an entry or outside of color map range
            for (size_t j = 0; j < nrBands; ++j, ++p)
                *p = (*color)[j];
        }
        else
        {
            // Value between two entries
            auto larger = (curr - min < fabs(max - curr))
                    ? colorTable.lower_bound(curr)
                    : colorTable.upper_bound(curr);
            auto smaller = larger;
            smaller--;
            
            if (!ramp)
            {
                for (size_t j = 0; j < nrBands; ++j, ++p)
                {
                    auto interpolatedColor = smaller->second[j];
                    *p = interpolatedColor;
                }
            }
            else
            {
                // ramp -> do linear interpolation
                double linIntS = (larger->first - curr) / (larger->first - smaller->first);
                double linIntL = (curr - smaller->first) / (larger->first - smaller->first);
                for (size_t j = 0; j < nrBands; ++j, ++p)
                {
                    auto interpolatedColor = static_cast<unsigned char>(
                        (smaller->second[j] * linIntS) + (larger->second[j] * linIntL));
                    *p = static_cast<unsigned char>(interpolatedColor);
                }
            }
        }
    }

    return reinterpret_cast<const char*>(res);
}

