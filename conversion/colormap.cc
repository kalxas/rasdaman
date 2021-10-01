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
#include "raslib/error.hh"
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

size_t r_ColorMap::getResultBandNumber() const
{
    return colorTable.begin()->second.size();
}

std::unique_ptr<unsigned char[]>
r_ColorMap::applyColorMap(const r_Type* srcType, const char* srcData, const r_Minterval& dimData, int& baseType)
{
    auto nrBands = getResultBandNumber();
    switch (nrBands)
    {
    case 1: baseType = ctype_char; break;
    case 3: baseType = ctype_rgb; break;
    case 4: baseType = ctype_struct; break;
    default:
        throw r_Error(r_Error::r_Error_Conversion,
                      "unsupported number of bands in color map: " + std::to_string(nrBands));
    }
    size_t nrPixels = dimData.cell_count();

    LDEBUG << "Applying color map to image with " << nrBands << " bands.";

    std::unique_ptr<unsigned char[]> img;
    img.reset(new unsigned char[nrBands * nrPixels]);
    
    MAKE_SWITCH_TYPEID(srcType->type_id(), T,
        CODE(
            applySpecificColorMap<T>(reinterpret_cast<const T*>(srcData), nrPixels, nrBands, img.get());
        ),
        CODE(
            std::stringstream s;
            s << "unsupported base type " << srcType->type_id() 
              << ", cannot perform color mapping";
            throw r_Error(r_Error::r_Error_Conversion, s.str());
        )
    )
    return img;
}

template <class T>
void r_ColorMap::applySpecificColorMap(const T* data, size_t nrPixels, size_t nrBands, unsigned char* res)
{
    switch (colorMapType)
    {
    case r_ColorMap::Type::VALUES:
        applyValuesColorMap<T>(data, nrPixels, nrBands, res);
        break;
    case r_ColorMap::Type::INTERVALS:
        applyIntervalsColorMap<T>(data, nrPixels, nrBands, res, false);
        break;
    case r_ColorMap::Type::RAMP:
        applyIntervalsColorMap<T>(data, nrPixels, nrBands, res, true);
        break;
    default:
      {
        std::stringstream s;
        s << "invalid color map type " << int(colorMapType);
        throw r_Error(r_Error::r_Error_Conversion, s.str());
      }
    }
}

template <class T>
void r_ColorMap::applyValuesColorMap(const T* data, size_t nrPixels, size_t nrBands, unsigned char* res)
{
    auto* p = res;
    for (size_t i = 0; i < nrPixels; i++)
    {
        auto el = uColorTable.find(static_cast<double>(data[i]));
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
}

const std::vector<unsigned char>* r_ColorMap::getUColor(double curr) const
{
    auto it = uColorTable.find(curr);
    return it != uColorTable.end() ? &it->second : nullptr;
}

template <class T>
void r_ColorMap::applyIntervalsColorMap(const T* data, size_t nrPixels, size_t nrBands, unsigned char* res, bool ramp)
{
    auto* p = res;

    double min = colorTable.begin()->first;
    double max = colorTable.rbegin()->first;

    for (size_t i = 0; i < nrPixels; ++i)
    {
        double curr = static_cast<double>(data[i]);

        // Get target color if it can be selected from exact values in the color map
        const std::vector<unsigned char>* color = nullptr;
        if (curr < min)
        {
            color = &colorTable.begin()->second;
        }
        else if (curr > max)
        {
            color = &colorTable.rbegin()->second;
        }
        else
        {
            color = getUColor(curr);
        }

        if (color != nullptr)
        {
            // Value matches exactly an entry or outside of color map range
            for (size_t j = 0; j < nrBands; ++j, ++p)
            {
                *p = (*color)[j];
            }
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
}

