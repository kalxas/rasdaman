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

r_ColorMap::r_ColorMap()
{}

void r_ColorMap::setColorMapType(r_ColorMap::Type type)
{
    this->colorMapType = type;
}

void r_ColorMap::setColorTable(std::map<double, std::vector<unsigned char>> colorTableMap)
{
    this->colorTable = std::move(colorTableMap);
}

void r_ColorMap::setUColorTable(std::unordered_map<double, std::vector<unsigned char>> uColorTableMap)
{
    this->uColorTable = std::move(uColorTableMap);
}

const char *r_ColorMap::applyColorMap(const r_Type *srcType, const char *srcData, const r_Minterval &dimData, int &baseType)
{

    int nrBands = static_cast<int>(colorTable.begin()->second.size());        
    if (nrBands == 1)
    {
        baseType = ctype_char;
    }
    else if (nrBands == 3)
    {
        baseType = ctype_rgb;
    }
    else if (nrBands == 4)
    {
        baseType = ctype_struct;
    }

    switch (srcType->type_id())
    {
    case r_Type::ULONG:
        return prepareColorMap<r_ULong>(srcData, dimData);
    case r_Type::LONG:
        return prepareColorMap<r_Long>(srcData, dimData);
    case r_Type::CHAR:
        return prepareColorMap<r_Char>(srcData, dimData);
    case r_Type::USHORT:
        return prepareColorMap<r_UShort>(srcData, dimData);
    case r_Type::SHORT:
        return prepareColorMap<r_Short>(srcData, dimData);
    case r_Type::OCTET:
        return prepareColorMap<r_Octet>(srcData, dimData);
    case r_Type::FLOAT:
        return prepareColorMap<r_Float>(srcData, dimData);
    case r_Type::DOUBLE:
        return prepareColorMap<r_Double>(srcData, dimData);
    default:
        LERROR << "Unsupported base type " << srcType->type_id() << ", cannot perform color mapping.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
}

template <class T>
const char *r_ColorMap::prepareColorMap(const char *srcData, const r_Minterval &dimData)
{
    int width  = static_cast<int>(dimData[0].high() - dimData[0].low() + 1);
    int height = static_cast<int>(dimData[1].high() - dimData[1].low() + 1);

    size_t nrPixels = static_cast<size_t>(dimData.cell_count());

    LDEBUG << "Size of image is " << width << " x " << height << " pixels.";
    LDEBUG << "Total number of pixels: " << nrPixels << " pixels.";

    switch (colorMapType)
    {
    case r_ColorMap::Type::VALUES:
        LDEBUG << "Beginning values color mapping";
        return applyValuesColorMap<T>(srcData, nrPixels);
    case r_ColorMap::Type::INTERVALS:
        LDEBUG << "Beginning intervals color mapping";
        return applyIntervalsColorsMap<T>(srcData, nrPixels);
    case r_ColorMap::Type::RAMP:
        LDEBUG << "Beginning ramp color mapping";
        return applyRampColorMap<T>(srcData, nrPixels);
    default:
        return srcData;
    }
}

template <class T>
const char *r_ColorMap::applyValuesColorMap(const char *srcData, size_t nrPixels)
{
    const T *data = reinterpret_cast<const T*>(srcData);

    // We are sure colorTable.begin() will never cause trouble
    // because it is verified to be not empty beforehand in formatparams.cc::parseColorMap()
    int nrBands = static_cast<int>(colorTable.begin()->second.size());
    unsigned char *p = NULL;
    unsigned char *img = new unsigned char[static_cast<size_t>(nrBands) * nrPixels];

    p = img;
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

    return reinterpret_cast<const char*>(img);
}

template <class T>
const char *r_ColorMap::applyIntervalsColorsMap(const char *srcData, size_t nrPixels)
{
    const T *data = reinterpret_cast<const T*>(srcData);

    // We are sure colorTable.begin() will never cause trouble
    // because it is verified to be not empty beforehand in formatparams.cc::parseColorMap()
    int nrBands = static_cast<int>(colorTable.begin()->second.size());
    unsigned char *p = NULL;
    unsigned char *img = new unsigned char[static_cast<size_t>(nrBands) * nrPixels];

    double min = colorTable.begin()->first;
    double max = colorTable.rbegin()->first;

    p = img;
    for (size_t i = 0; i < nrPixels; i++)
    {
        double curr = static_cast<double>(data[i]);
        auto el = colorTable.find(curr);

        if (el != colorTable.end())
        {
            for (size_t j = 0; j < nrBands; ++j, ++p)
            {
                *p = el->second[j];
            }
        }
        else
        {   
            if (curr < min)
            {
                // Value less than the first entry in the color table
                for (size_t j = 0; j < nrBands; ++j, ++p)
                {
                    *p = colorTable.begin()->second[j];
                }
            }
            else if (curr > max)
            {
                // Value more than the last entry
                for (size_t j = 0; j < nrBands; ++j, ++p)
                {
                    *p = colorTable.rbegin()->second[j];
                }
            }
            else if (curr - min < abs(max - curr))
            {
                // Closer to minimum value
                std::map<double, std::vector<unsigned char>>::iterator it;
                it = colorTable.lower_bound(curr);
                it--;

                for (size_t j = 0; j < nrBands; ++j, ++p)
                {
                    colorTable[curr].push_back(it->second[j]);
                    *p = it->second[j];
                }
            }
            else
            {
                // Closer to maximum value
                std::map<double, std::vector<unsigned char>>::iterator it;
                it = colorTable.upper_bound(curr);
                it--;

                for (size_t j = 0; j < nrBands; ++j, ++p)
                {
                    colorTable[curr].push_back(it->second[j]);
                    *p = it->second[j];
                }
            }
        }
    }

    return reinterpret_cast<const char*>(img);
}

template <class T>
const char *r_ColorMap::applyRampColorMap(const char *srcData, size_t nrPixels)
{
    // TODO: Ramp support is similar in implementation with the support for intervals,
    // so it needs to be refactored
    const T *data = reinterpret_cast<const T*>(srcData);

    // We are sure colorTable.begin() will never cause trouble
    // because it is verified to be not empty beforehand in formatparams.cc::parseColorMap()
    int nrBands = static_cast<int>(colorTable.begin()->second.size());
    unsigned char *p = NULL;
    unsigned char *img = new unsigned char[static_cast<size_t>(nrBands) * nrPixels];

    double min = colorTable.begin()->first;
    double max = colorTable.rbegin()->first;

    p = img;
    for (unsigned int i = 0; i < nrPixels; i++)
    {
        double curr = static_cast<double>(data[i]);
        auto el = colorTable.find(curr);

        if (el != colorTable.end())
        {
            for (size_t j = 0; j < nrBands; ++j, ++p)
            {
                *p = el->second[j];
            }
        }
        else
        {   
            if (curr < min)
            {
                // Value less than the first entry in the color table
                for (size_t j = 0; j < nrBands; ++j, ++p)
                {
                    *p = colorTable.begin()->second[j];
                }
            }
            else if (curr > max)
            {
                // Value more than the last entry
                for (size_t j = 0; j < nrBands; ++j, ++p)
                {
                    *p = colorTable.rbegin()->second[j];
                }
            }
            else if (curr - min < abs(max - curr))
            {
                // Closer to minimum value
                std::map<double, std::vector<unsigned char>>::iterator larger;
                std::map<double, std::vector<unsigned char>>::iterator smaller;
                larger = colorTable.lower_bound(curr);
                smaller = larger;
                smaller--;

                double linIntS = (larger->first - curr) / (larger->first - smaller->first);
                double linIntL = (curr - smaller->first) / (larger->first - smaller->first);

                for (size_t j = 0; j < nrBands; ++j, ++p)
                {
                    int color = ((smaller->second[j] * linIntS) + (larger->second[j] * linIntL));

                    colorTable[curr].push_back(static_cast<unsigned char>(color));
                    *p = static_cast<unsigned char>(color);
                }
            }
            else
            {
                // Closer to maximum value
                std::map<double, std::vector<unsigned char>>::iterator larger;
                std::map<double, std::vector<unsigned char>>::iterator smaller;
                larger = colorTable.upper_bound(curr);
                smaller = larger;
                smaller--;

                double linIntS = (larger->first - curr) / (larger->first - smaller->first);
                double linIntL = (curr - smaller->first) / (larger->first - smaller->first);

                for (size_t j = 0; j < nrBands; ++j, ++p)
                {
                    int color = ((smaller->second[j] * linIntS) + (larger->second[j] * linIntL));

                    colorTable[curr].push_back(static_cast<unsigned char>(color));
                    *p = static_cast<unsigned char>(color);
                }
            }
        }
    }

    return reinterpret_cast<const char*>(img);
}