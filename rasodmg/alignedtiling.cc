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
/**
 * SOURCE: alignedtiling.cc
 *
 * MODULE: rasodmg
 * CLASS:  r_AlignedTiling, r_DefaultTiling
 *
 * COMMENTS:
 *          None
*/

#include "rasodmg/alignedtiling.hh"
#include <logging.hh>

#include <vector>
#include <cmath>
#include <cstring>
#include <cstdlib>
#include <sstream>

const char *
    r_Aligned_Tiling::description = "tile configuration or tile dimension and tile size (in bytes) (ex: \"[0:9,0:9];100\" or \"2;100\")";

r_Aligned_Tiling::r_Aligned_Tiling(const char *encoded)
    : r_Dimension_Tiling(0, 0)
{
    check_nonempty_tiling(encoded);

    const char *pStart = encoded;
    const char *pEnd = pStart + strlen(pStart);
    const char *pRes = advance_to_next_char(pStart, TCOLON);

    // 1 param
    auto pToConvertPtr = copy_buffer(pStart, static_cast<r_Bytes>(pRes - pStart));
    const auto pToConvert = pToConvertPtr.get();
    if (*pToConvert == *LSQRBRA)
    {
        tile_config = parse_minterval(pToConvert);
        dimension = tile_config.dimension();
    }
    else
    {
        dimension = parse_unsigned(pToConvert);
        tile_config = r_Minterval(dimension);
    }
    check_premature_stream_end(pRes, pEnd);
    //skip COLON
    ++pRes;

    // 2 param
    tile_size = parse_unsigned(pRes);
}

r_Aligned_Tiling::r_Aligned_Tiling(r_Dimension dim, r_Bytes ts)
    : r_Dimension_Tiling(dim, ts), tile_config(dim)
{
    /// Default tile configuration - equal sides
    for (r_Dimension i = 0; i < dim; i++)
        tile_config << r_Sinterval(0l, 1l);
}

r_Aligned_Tiling::r_Aligned_Tiling(const r_Minterval &tc, r_Bytes ts)
    : r_Dimension_Tiling(tc.dimension(), ts), tile_config(tc)
{
}

r_Tiling *
r_Aligned_Tiling::clone() const
{
    return new r_Aligned_Tiling(tile_config, tile_size);
}

const r_Minterval &
r_Aligned_Tiling::get_tile_config() const
{
    return tile_config;
}

r_Minterval
r_Aligned_Tiling::compute_tile_domain(const r_Minterval &dom, r_Bytes cell_size) const
{
    // Minimum optimal tile size. Below this value, the waste will be too big.
    r_Bytes optMinTileSize = get_min_opt_tile_size();
    // number of cells per tile according to storage options
    r_Area numCellsTile = tile_size / cell_size;
    // For final result.
    r_Minterval tileDomain(dimension);

    int startIx = -1;
    for (r_Dimension i = 0; i < dimension; i++)
    {
        if (!tile_config[i].is_low_fixed() || !tile_config[i].is_high_fixed())
            startIx = static_cast<int>(i);
    }
    if (startIx >= 0)  // Some limits are nonfixed
    {
        auto size = cell_size;

        for (int i = startIx; i >= 0; i--)  // treat the non fixed limits first
        {
            const auto dim = static_cast<r_Dimension>(i);
            // If any of the limits is non-fixed along this direction, tiles
            // will extend from one side to the other along this direction.
            if (!tile_config[dim].is_low_fixed() || !tile_config[dim].is_high_fixed())
            {
                auto l = dom[dim].low();
                auto h = dom[dim].high();

                /*
                   Alternative interpretation of tile_config with non fixed limits
                   For the time being is useless because the splittile algorithm
                   doesn't take into account the origin of the tile
                   
                    if (tile_config[i].is_low_fixed() == 0)
                      l = contentsDomain[i].low();
                    else
                      l = tile_config[i].low();
                    if (tileconfig[i].is_high_fixed() == 0)
                      h = contentsDomain[i].high();
                    else
                      h = tile_config[i].high();
                */

                if (size * static_cast<r_Bytes>(h - l + 1) > tile_size)
                {
                    h = static_cast<r_Range>(tile_size / size) + l - 1;
                }
                size = size * static_cast<r_Bytes>(h - l + 1);
                tileDomain[dim] = r_Sinterval(r_Range(l), r_Range(h));
            }
        }
        for (int i = static_cast<int>(dimension) - 1; i >= 0; i--)  // treat fixed limits now
        {
            const auto dim = static_cast<r_Dimension>(i);
            // If any of the limits is non-fixed along this direction, tiles
            // will extend from one side to the other along this direction.
            if (tile_config[dim].is_low_fixed() && tile_config[dim].is_high_fixed())
            {
                auto l = tile_config[dim].low();
                auto h = tile_config[dim].high();

                if (size * static_cast<r_Bytes>(h - l + 1) > tile_size)
                {
                    h = static_cast<r_Range>(tile_size / size) + l - 1;
                }
                size = size * static_cast<r_Bytes>(h - l + 1);
                tileDomain[dim] = r_Sinterval(r_Range(l), r_Range(h));
            }
        }

        return tileDomain;
    }
    else  // tile_config has only fixed limits
    {
        auto numCellsTileConfig = tile_config.cell_count();
        auto sizeTileConfig = numCellsTileConfig * cell_size;

        if (sizeTileConfig > get_min_opt_tile_size() && sizeTileConfig < tile_size)
        {
            return tile_config;
        }
        else
        {
            float sizeFactor = static_cast<float>(numCellsTile) / numCellsTileConfig;

            float f = float(1 / float(dimension));
            float dimFactor = std::pow(sizeFactor, f);
            LTRACE << "dim factor == " << dimFactor;

            // extending the bound of each r_Sinterval of tile_config by
            // using the factor dimFactor
            for (unsigned int i = 0; i < dimension; i++)
            {
                auto l = tile_config[i].low();
                auto h = tile_config[i].high();
                auto newWidth = (h - l + 1) * dimFactor;
                if (newWidth < 1)
                    newWidth = 1;
                tileDomain << r_Sinterval(l, static_cast<r_Range>(l + newWidth - 1));
            }

            // Approximate the resulting tile size to the target one:

            /*
            r_Minterval tmpTileDomain = get_opt_size(tileDomain, cell_size);
            tileDomain = tmpTileDomain;
            
            unsigned long sz = tileDomain.cell_count() * cell_size;

            LTRACE << "cell_size " << cell_size << " tileDomain "<< tileDomain;
            LTRACE << "cell_count == " << tileDomain.cell_count() << " sz == " << sz;

            unsigned long newSz = sz;
            for(i = dimension-1; i >= 0 && newSz < tile_size ; i--)
            {
                LTRACE << "inside the cycle ";
                unsigned long deltaSz = cell_size;
                for (int j = 0 ; j < dimension ; j++)
                 if (j != i)
                   deltaSz *= (tileDomain[j].high()-tileDomain[j].low()+1);

                h = tileDomain[i].high();
                if (deltaSz + newSz <= tile_size)
                {
                  tileDomain[i].set_high(r_Range(h + 1));
                  newSz += deltaSz;
                }
            }
            */

            if (tileDomain.cell_count() * cell_size > tile_size)
                LTRACE << "calculateTileDomain() ";
            if (tileDomain.cell_count() * cell_size < optMinTileSize)
                LTRACE << "calculateTileDomain() result non optimal ";
            return tileDomain;
        }
    }
}

std::vector<r_Minterval>
r_Aligned_Tiling::compute_tiles(const r_Minterval &obj_domain, r_Bytes cell_size) const
{
    auto bigDom = obj_domain;
    auto dim = tile_config.dimension();
    auto tileDom = compute_tile_domain(obj_domain, cell_size);

    r_Minterval currDom(tileDom.dimension());
    r_Point cursor(tileDom.dimension());

    // initialize cursor
    for (dim = 0; dim < cursor.dimension(); dim++)
        cursor[dim] = 0;

    // calculate size of Tiles
    auto tileSize = tileDom.get_extent();
    // origin of bigTile
    auto origin = bigDom.get_origin();
    // initialize currDom
    for (dim = 0; dim < cursor.dimension(); dim++)
        currDom << r_Sinterval(origin[dim], origin[dim] + tileSize[dim] - 1);

    // resets tileDom to lower left side of bigTile
    tileDom = currDom;
    // intersect with bigTile
    currDom.intersection_with(bigDom);

    // iterate with smallTile over bigTile
    std::vector<r_Minterval> result;
    bool done = false;
    while (!done)
    {
        currDom.intersection_with(bigDom);
        // insert small tile in set
        result.push_back(currDom);

        // increment cursor, start with highest dimension
        auto i = cursor.dimension() - 1;
        cursor[i] += tileSize[i];
        // move cursor
        currDom = tileDom.create_translation(cursor);
        while (!currDom.intersects_with(bigDom))
        {
            cursor[i] = 0;
            if (i == 0)
            {
                done = true;
                break;
            }
            i--;
            cursor[i] += tileSize[i];
            // move cursor
            currDom = tileDom.create_translation(cursor);
        }
    }
    return result;
}

r_Minterval
r_Aligned_Tiling::get_opt_size(const r_Minterval &tileDomain, r_Bytes cellSize) const
{
    auto tileSize = get_tile_size();
    auto newSize = tileDomain.cell_count() * cellSize;
    auto result = tileDomain;
    auto dim = tileDomain.dimension();
    auto tmpResult = result;

    std::vector<int> ixArr(dim);
    for (r_Dimension j = 0; j < dim; j++)
        ixArr[j] = static_cast<int>(j);

    for (int j = static_cast<int>(dim) - 1; j >= 0 && newSize < tileSize; j--)
    {
        r_Range h{}, wd{};
        auto minWidth = tileDomain[0].high() - tileDomain[0].low() + 1;
        r_Dimension minWidthIx{};

        for (int k = j; k >= 0; k--)
        {
            auto ii = static_cast<r_Dimension>(ixArr[static_cast<size_t>(k)]);
            h = result[ii].high() + 1;
            wd = result[ii].high() - result[ii].low() + 1;
            if (wd < minWidth)
            {
                minWidth = wd;
                minWidthIx = ii;
            }
        }

        auto tmpIx = ixArr[static_cast<size_t>(j)];
        ixArr[minWidthIx] = tmpIx;

        tmpResult[minWidthIx].set_high(h);
        newSize = tmpResult.cell_count() * cellSize;
        if (newSize > tileSize)
        {
            for (int i = static_cast<int>(dim) - 1; i >= 0; i--)
            {
                auto ii = static_cast<r_Dimension>(i);
                h = result[ii].high() + 1;
                wd = result[ii].high() - result[ii].low() + 1;
                if (wd < minWidth)
                {
                    minWidth = wd;
                    minWidthIx = ii;
                }
            }
        }

        result[minWidthIx].set_high(h);
        newSize = result.cell_count() * cellSize;
        if (newSize > tileSize)
            result[minWidthIx].set_high(h - 1);
    }
    return result;
}

r_Tiling_Scheme
r_Aligned_Tiling::get_tiling_scheme() const
{
    return r_AlignedTiling;
}

r_Bytes
r_Aligned_Tiling::get_min_opt_tile_size() const
{
    return get_tile_size() - get_tile_size() / 10;
}

std::string
r_Aligned_Tiling::get_string_representation() const
{
    std::ostringstream domainStream;
    print_status(domainStream);
    auto returnString = domainStream.str();
    return returnString;
}

void r_Aligned_Tiling::print_status(std::ostream &os) const
{
    os << "r_Aligned_Tiling[ ";
    r_Dimension_Tiling::print_status(os);
    os << " tile configuration = " << tile_config << " ]";
}
