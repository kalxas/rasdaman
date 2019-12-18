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
 * SOURCE: dirtiling.cc
 *
 * MODULE: rasodmg
 * CLASS:  r_DirTiling
 *
 * COMMENTS:
 *          None
*/

#include "rasodmg/dirtiling.hh"
#include "rasodmg/dirdecompose.hh"
#include "rasodmg/alignedtiling.hh"

#include <logging.hh>

#include <cmath>
#include <cstdlib>

const char *
r_Dir_Tiling::description = "dimensions, decomposision patterns, tile size(in bytes) and subtiling [SUBTILING|NOSUBTILING] (ex: \"3;[0,2,4,5],[*],[0,10,15];100;NOSUBTILING\")";

const char *r_Dir_Tiling::subtiling_name_withoutsubtiling = "NOSUBTILING";
const char *r_Dir_Tiling::subtiling_name_withsubtiling    = "SUBTILING";
const char *r_Dir_Tiling::all_subtiling_names[r_Dir_Tiling::NUMBER] =
{
    subtiling_name_withoutsubtiling,
    subtiling_name_withsubtiling
};

r_Dir_Tiling::SubTiling
r_Dir_Tiling::get_subtiling_from_name(const char *name)
{
    if (!name)
    {
        LWARNING << "no name specified, returning default.";
        return r_Dir_Tiling::NUMBER;
    }
    unsigned int i = r_Dir_Tiling::NUMBER;
    for (i = 0; i < static_cast<unsigned int>(r_Dir_Tiling::NUMBER); i++)
    {
        if (strcasecmp(name, all_subtiling_names[i]) == 0)
        {
            break;
        }
    }
    return static_cast<r_Dir_Tiling::SubTiling>(i);
}

const char *
r_Dir_Tiling::get_name_from_subtiling(SubTiling tsl)
{
    static const char *unknown = "UNKNOWN";
    unsigned int idx = static_cast<unsigned int>(tsl);
    if (idx >= static_cast<unsigned int>(r_Dir_Tiling::NUMBER))
        return unknown;
    return all_subtiling_names[idx];
}

r_Dir_Tiling::r_Dir_Tiling(const char *encoded)
    :   r_Dimension_Tiling(0, 0)
{
    check_nonempty_tiling(encoded);

//initialisation
    const char *pStart = encoded;
    const char *pTemp = pStart;
    const char *pEnd = pTemp + strlen(pStart);

//deal with dimension
    const char *pRes = advance_to_next_char(pTemp, TCOLON);
    {
        auto pToConvertPtr = copy_buffer(pTemp, static_cast<r_Bytes>(pRes - pTemp));
        dimension = parse_unsigned(pToConvertPtr.get());
    }
    std::vector<r_Dir_Decompose> vectDirDecomp(dimension);
    
    check_premature_stream_end(pRes, pEnd);
    pRes++;
    pTemp = pRes;

//deal with directional decompose
    pRes = advance_to_next_char(pTemp, TCOLON);
    auto pToConvertPtr = copy_buffer(pTemp, static_cast<size_t>(pRes - pTemp));
    const char *pToConvertEnd = pToConvertPtr.get() + strlen(pToConvertPtr.get());
    const char *pDirTemp = pToConvertPtr.get();
    
    // helper for multiple checks below
#define CHECK_PARAM(cond) \
    if (cond) { \
        LERROR << "Error decoding directional decompose for dimension " << dirIndex + 1 << " from \"" << pToConvertPtr.get() << "\"."; \
        throw r_Error(TILINGPARAMETERNOTCORRECT); \
    }
    
    r_Dimension dirIndex = 0;
    while (dirIndex < dimension)
    {
        const char *pDirRes = strstr(pDirTemp, LSQRBRA);
        CHECK_PARAM(!pDirRes)
        const char *pDirStart = pDirRes;
        
        pDirRes = strstr(pDirTemp, RSQRBRA);
        CHECK_PARAM(!pDirRes)
        const char *pDirEnd = pDirRes;

        auto lenDirToConvert = static_cast<r_Bytes>(pDirEnd - pDirStart);
        CHECK_PARAM(lenDirToConvert == 1)

        auto pDirToConvertPtr = copy_buffer(pDirStart + 1, lenDirToConvert - 1);
        char *pDirToConvert = pDirToConvertPtr.get();
        
        pDirTemp = pDirToConvert;
        pDirStart = pDirEnd;
        pDirEnd = pDirToConvert + strlen(pDirToConvert);

        if (*pDirToConvert != *ASTERIX)
        {
            pDirRes = strstr(pDirToConvert, TCOMMA);
            CHECK_PARAM(!pDirRes)
            while (pDirRes)
            {
                auto lenDecomp = static_cast<r_Bytes>(pDirRes - pDirTemp);
                auto pDecompPtr = copy_buffer(pDirTemp, lenDecomp);
                vectDirDecomp[dirIndex] << static_cast<r_Range>(parse_unsigned_long(pDecompPtr.get()));

                //skip COMMA & free buffer
                check_premature_stream_end(pDirRes, pDirEnd);
                pDirRes++;
                pDirTemp = pDirRes;

                //next decomp
                pDirRes = strstr(pDirTemp, TCOMMA);
                if (!pDirRes)
                {
                    vectDirDecomp[dirIndex] << static_cast<r_Range>(parse_unsigned_long(pDirTemp));
                    break;
                }
            }
        }

        dirIndex++;
        if (dirIndex < dimension)
        {
            CHECK_PARAM(pDirStart == pToConvertEnd - 1)
            pDirStart++;
            pDirRes = strstr(pDirStart, TCOMMA);
            CHECK_PARAM(!pDirRes)
            CHECK_PARAM(pDirRes == pToConvertEnd - 1)
            pDirRes++;
            pDirTemp = pDirRes;
        }
    }

//skip COLON & free buffer
    check_premature_stream_end(pRes, pEnd);
    pRes++;
    pTemp = pRes;

//deal with tilesize
    pRes = advance_to_next_char(pTemp, TCOLON);
    pToConvertPtr = copy_buffer(pTemp, static_cast<size_t>(pRes - pTemp));
    tile_size = parse_unsigned(pToConvertPtr.get());

//skip COLON & free buffer
    check_premature_stream_end(pRes, pEnd);
    pRes++;
    pTemp = pRes;
//deal with subtilig
    auto subTiling = r_Dir_Tiling::get_subtiling_from_name(pTemp);
    if (subTiling == r_Dir_Tiling::NUMBER)
    {
        LERROR << "Error decoding subtiling from \"" << pTemp << "\".";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
    dim_decomp = vectDirDecomp;
    sub_tile = subTiling;
}

r_Dir_Tiling::r_Dir_Tiling(r_Dimension dims, const std::vector<r_Dir_Decompose> &decomp, r_Bytes ts, SubTiling sub)
    :   r_Dimension_Tiling(dims, ts),
        dim_decomp(decomp),
        sub_tile(sub)
{
    if (dim_decomp.size() != dimension)
    {
        LERROR << "r_Dir_Tiling::r_Dir_Tiling(" << dims << ", " << ts 
               << ", " << static_cast<int>(sub) << ") number of dimensions (" << dimension 
               << ") does not match number of decomposition entries (" << decomp.size() << ")";
        throw r_Edim_mismatch(dimension, static_cast<r_Dimension>(dim_decomp.size()));
    }
}
r_Tiling_Scheme
r_Dir_Tiling::get_tiling_scheme() const
{
    return r_DirectionalTiling;
}
r_Tiling *r_Dir_Tiling::clone() const
{
    return new r_Dir_Tiling(dimension, dim_decomp, tile_size, sub_tile);
}

void r_Dir_Tiling::print_status(std::ostream &os) const
{
    os << "r_Dir_Tiling[ ";
    r_Dimension_Tiling::print_status(os);
    os << " sub tiling = " << static_cast<int>(sub_tile) << " decompose = { ";
    for (r_Dimension i = 0; i < dim_decomp.size(); i++)
        os << "dim #" << i << " : " << dim_decomp[i] << " ";
    os << "} ";

}

std::vector<r_Minterval>
r_Dir_Tiling::compute_tiles(const r_Minterval &domain, r_Bytes typelen) const
{
    // Check dims
    if (dimension != domain.dimension())
    {
        LERROR << "r_Dir_Tiling::compute_tiles(" << domain << ", " << typelen 
               << ") dimensions of domain (" << domain.dimension() 
               << ") do not match dimensions of tiling strategy (" << dimension << ")";
        throw r_Edim_mismatch(dimension, domain.dimension());
    }
    
    // The result
    std::vector<r_Minterval> decomp_result;
    // An alias to result
    auto &result = decomp_result;
    auto temp_dim_decomp = dim_decomp;

    // Undefined dims
    std::vector<bool> undef_dim(dimension);
    // Count of undef dims
    r_Dimension total_undef = 0;

    // Check if limits ok
    for (r_Dimension i = 0; i < dimension; i++)
    {
        // Restric defined
        if (temp_dim_decomp[i].get_num_intervals() > 0)
        {
            undef_dim[i] = false;
            auto lim1 = domain[i].high();
            auto lim2 = temp_dim_decomp[i].get_partition(temp_dim_decomp[i].get_num_intervals() - 1);
            if (lim1 != lim2)
            {
                LERROR << "r_Dir_Tiling::compute_tiles(" << domain << ", " << typelen 
                       << ") upper limit of domain (" << domain.dimension() 
                       << ") at dimension " << i << " (" << domain[i] << ") does not partition " 
                       << temp_dim_decomp[i].get_partition(temp_dim_decomp[i].get_num_intervals() - 1);
                throw r_Elimits_mismatch(lim1, lim2);
            }

            lim1 = domain[i].low();
            lim2 = temp_dim_decomp[i].get_partition(0);
            if (lim1 != lim2)
            {
                LERROR << "r_Dir_Tiling::compute_tiles(" << domain << ", " << typelen 
                       << ") lower limit of domain (" << domain.dimension() 
                       << ") at dimension " << i << " (" << domain[i] << ") does not partition " 
                       << temp_dim_decomp[i].get_partition(0);
                throw r_Elimits_mismatch(lim1, lim2);
            }
        }
        else                                               // Restric not defined
        {
            // Dim unspecified
            undef_dim[i] = true;
            total_undef++;
            temp_dim_decomp[i] << domain[i].low() << domain[i].high();
        }
    }

    // Create a counter for each dimension
    std::vector<r_Dimension> dim_counter(dimension, 0);

    // Iterate over the all space
    bool done = false;
    while (!done)
    {
        // Determine tile coordinates
        r_Minterval tile(dimension);

        for (r_Dimension i = 0; i < dimension; i++)
        {
            r_Range origin = temp_dim_decomp[i].get_partition(dim_counter[i]);
            if (dim_counter[i] != 0)
                origin++;

            r_Range limit;
            if (temp_dim_decomp[i].get_num_intervals() <= (dim_counter[i] + 1))
                limit = origin;
            else
                limit = temp_dim_decomp[i].get_partition(dim_counter[i] + 1);

            tile << r_Sinterval(origin, limit);
        }

        // Do something with tile coordinates (decompose big object)
        LRDEBUG("(DirTiling::compute_tiles(): Tile: " << tile);

        // Check if sub-tiling should be done and calculate edgesize
        if ((tile.cell_count() * typelen > tile_size) && (sub_tile == WITH_SUBTILING))
        {
            // Create a specification for the partition
            r_Minterval partition(dimension);

            if (total_undef == 0)
            {
                // No unspecified dimensions --- create block cross sections
                for (r_Dimension i = 0; i < dimension; i++)
                    partition << r_Sinterval(0ll, tile[i].high() - tile[i].low());
            }
            else
            {
                // Some unspecified dimensions

                // Compute edgesize
                auto edgesize = static_cast<r_Range>(tile_size);
                for (r_Dimension i = 0; i < dimension; i++)
                    if (!undef_dim[i])
                        edgesize /= (tile[i].high() - tile[i].low() + 1) * static_cast<r_Range>(typelen);

                edgesize = floor(pow(static_cast<double>(edgesize) / typelen, 1.0 / total_undef));

                // Create specification
                for (r_Dimension i = 0; i < dimension; i++)
                    partition << r_Sinterval(0ll, undef_dim[i] ? edgesize - 1 : tile[i].high() - tile[i].low());
            }

            LRDEBUG("(DirTiling::compute_tiles(): Tile size = " << get_tile_size() << " Specs = " << partition)

            // Create subtiles and insert them in the result
            r_Aligned_Tiling subtiling(partition, get_tile_size());
            auto subtiles = subtiling.compute_tiles(tile, typelen);
            for (const auto &subtile: subtiles)
                result.push_back(subtile);
        }
        else
        {
            result.push_back(tile);
        }

        // Update dimension counters
        for (r_Dimension i = 0; i < dimension; i++)
        {
            dim_counter[i]++;
            if (dim_counter[i] >= temp_dim_decomp[i].get_num_intervals() - 1)
                dim_counter[i] = 0;
            else
                break;
        }

        // See if we are done
        done = true;
        for (r_Dimension i = 0; i < dimension; i++)
        {
            if (dim_counter[i] != 0)
            {
                done = false;
                break;
            }
        }
    }

    return result;
}

bool
r_Dir_Tiling::is_compatible(const r_Minterval &domain, r_Bytes type_len) const
{
    if (!r_Dimension_Tiling::is_compatible(domain, type_len))
    {
        return false;
    }
    else
    {
        // Check if limits ok
        for (r_Dimension i = 0; i < dimension; i++)
        {
            if (dim_decomp[i].get_num_intervals() > 0)
            {
                if (domain[i].high() != dim_decomp[i].get_partition(dim_decomp[i].get_num_intervals() - 1) ||
                    domain[i].low() != dim_decomp[i].get_partition(0))
                {
                    return false;
                }
            }
        }
    }
    return true;
}

