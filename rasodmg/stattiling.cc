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

#include "rasodmg/stattiling.hh"
#include "rasodmg/interesttiling.hh"
#include "rasodmg/alignedtiling.hh"
#include "raslib/error.hh"

#include <cmath>
#include <cstdlib>

#include <logging.hh>

const char *
r_Stat_Tiling::description = "dimensions, access patterns, border threshold, interesting threshold, tile size (in bytes) (ex: \"2;[0:9,0:9],3;[100:109,0:9],2;2;0.3;100\")";

const
r_Area r_Stat_Tiling::DEF_BORDER_THR = 50L;
const r_Double
r_Stat_Tiling::DEF_INTERESTING_THR  = 0.20;

r_Stat_Tiling::r_Stat_Tiling(const char *encoded)
    :   r_Dimension_Tiling(0, 0)
{
    check_nonempty_tiling(encoded);

    const char *pStart = encoded;
    const char *pEnd = pStart + strlen(pStart);
    const char *pTemp = pStart;
    const char *pRes = advance_to_next_char(pTemp, TCOLON);

//deal with dimension
    auto pToConvertPtr = copy_buffer(pTemp, static_cast<r_Bytes>(pRes - pTemp));
    dimension = parse_unsigned(pToConvertPtr.get());

//skip COLON && free buffer
    check_premature_stream_end(pRes, pEnd);
    pRes++;

//deal with access informations
    pTemp = pRes;
    pRes = advance_to_next_char(pTemp, TCOLON);
    
    while (pRes)
    {
        //is access info?
        if (*pTemp != *LSQRBRA)
            break;

        //copy substring in buffer
        pToConvertPtr = copy_buffer(pTemp, static_cast<r_Bytes>(pRes - pTemp));

        //deal with access Interval
        const char *pInEnd = pToConvertPtr.get() + strlen(pToConvertPtr.get());
        const char *pInRes = advance_to_next_char(pToConvertPtr.get(), RSQRBRA);

        auto lenInToConvert = static_cast<r_Bytes>(pInRes - pToConvertPtr.get() + 1); //1 for ]
        auto pInToConvertPtr = copy_buffer(pToConvertPtr.get(), lenInToConvert);
        
        r_Minterval accessInterv;
        try
        {
            accessInterv = r_Minterval(pInToConvertPtr.get());
        }
        catch (r_Error &err)
        {
            LERROR << "Error decoding access interval \"" << pInToConvertPtr.get() << "\" from \"" 
                   << pToConvertPtr.get() << "\", error " << err.get_errorno() << " : " << err.what();
            throw r_Error(TILINGPARAMETERNOTCORRECT);
        }
        
        //deal with access Times
        pInRes = advance_to_next_char(pInRes, TCOMMA);
        check_premature_stream_end(pInRes, pEnd);
        pInRes++;
        
        auto accessTimes = parse_long(pInRes);
        stat_info.push_back(r_Access(accessInterv, accessTimes));

        //skip COLON && free buffer
        check_premature_stream_end(pRes, pEnd);
        pRes++;

        //deal with next item
        pTemp = pRes;
        pRes = advance_to_next_char(pTemp, TCOLON);
    }

    if (stat_info.empty())
    {
        LERROR << "Error decoding access informations, no access informations specified.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//deal with borderTH
    pToConvertPtr = copy_buffer(pTemp, static_cast<r_Bytes>(pRes - pTemp));
    border_thr = parse_unsigned_long(pToConvertPtr.get());

//skip COLON && free buffer
    check_premature_stream_end(pRes, pEnd);
    pRes++;

//deal with interestTH
    pTemp = pRes;
    pRes = advance_to_next_char(pTemp, TCOLON);

//copy substring into buffer
    pToConvertPtr = copy_buffer(pTemp, static_cast<r_Bytes>(pRes - pTemp));
    const auto *pToConvert = pToConvertPtr.get();

    interesting_thr = parse_double(pToConvert);
    if (interesting_thr == 0.0)
    {
        LERROR << "Error decoding interesting threshold \"" << pToConvert << "\".";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
    if (interesting_thr < 0.)
    {
        LERROR << "Error decoding interesting threshold \"" << pToConvert << "\", negative number.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
    if (interesting_thr > 1.)
    {
        LERROR << "Error decoding interesting threshold \"" << pToConvert << "\", not in [0,1] interval.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//skip COLON && free buffer
    check_premature_stream_end(pRes, pEnd);
    pRes++;

//deal with tilesize
    pTemp = pRes;
    tile_size = parse_unsigned(pTemp);
}


r_Stat_Tiling::r_Stat_Tiling(r_Dimension dim, const std::vector<r_Access> &stat_info2, 
                             r_Bytes ts, r_Area border_threshold, r_Double interesting_threshold)
    :   r_Dimension_Tiling(dim, ts),
        interesting_thr(interesting_threshold),
        border_thr(border_threshold),
        stat_info(stat_info2)
{
    // Filter accesses all areas have the same dimension if successfull else exception
    filter(stat_info);
    LTRACE << "done\n";

    // Count total accesses
    r_ULong total_accesses = 0;
    for (auto areas_it = stat_info.begin(); areas_it != stat_info.end(); areas_it++)
    {
        if ((*areas_it).get_pattern().dimension() != dim)
        {
            LERROR << "r_Stat_Tiling::r_Stat_Tiling(" << dim << ", " << &stat_info
                   << ", " << ts << ", " << border_threshold << ", " << interesting_threshold
                   << ") dimension (" << dim << ") does not match dimension of access patterns ("
                   << (*areas_it).get_pattern().dimension() << ")";
            throw r_Edim_mismatch(dim, (*areas_it).get_pattern().dimension());
        }
        total_accesses += (*areas_it).get_times();
    }

    LTRACE << "Defining interest areas... ";

    // Mininum number of accesses for being interesting
    auto critical_accesses = static_cast<r_ULong>(interesting_thr * total_accesses);

    iareas.clear();
    for (auto areas_it = stat_info.begin(); areas_it != stat_info.end(); areas_it++)
    {
        if ((*areas_it).get_times() >= critical_accesses) // Threshold exceeded or equal
            iareas.push_back(areas_it->get_pattern());           // count this area in
    }
}

r_Tiling *r_Stat_Tiling::clone() const
{
    return new r_Stat_Tiling(dimension, stat_info, tile_size, border_thr, interesting_thr);
}
void r_Stat_Tiling::print_status(std::ostream &os) const
{
    os << "r_Stat_Tiling[ ";
    r_Dimension_Tiling::print_status(os);
    os << " border threshold = " << border_thr << ", interesting threshold = " << interesting_thr << " ]";
}

const std::vector<r_Minterval> &
r_Stat_Tiling::get_interesting_areas() const
{
    return iareas;
}
r_Tiling_Scheme
r_Stat_Tiling::get_tiling_scheme() const
{
    return r_StatisticalTiling;
}
r_Area
r_Stat_Tiling::get_border_threshold() const
{
    return border_thr;
}
r_Double
r_Stat_Tiling::get_interesting_threshold() const
{
    return interesting_thr;
}

r_Access
r_Stat_Tiling::merge(const std::vector<r_Access> &patterns) const
{
    // Create an interator for list of patterns
    auto it = patterns.begin();
    // The result (initialy updated to the first element of patterns)
    auto result = (*it);
    it++;
    for (; it != patterns.end(); it++)
        result.merge_with(*it);

    return result;                                     // Return the result
}

void r_Stat_Tiling::filter(std::vector<r_Access> &patterns) const
{
    std::vector<r_Access> result;
    // List to hold the clusters
    std::vector<r_Access> cluster;

    // For all elements in pattern table
    while (!patterns.empty())
    {
        cluster.clear();
        // Cluster with first element of pattern list
        cluster.push_back(patterns.back());
        patterns.pop_back();

        // For all elements in the cluster
        for (auto cluster_it = cluster.begin(); cluster_it != cluster.end(); cluster_it++)
        {
            // For all remaining patterns
            for (auto pattern_it = patterns.begin(); pattern_it != patterns.end(); pattern_it++)
            {
                // Pattern near an element from the cluster
                if ((*cluster_it).is_near(*pattern_it, border_thr))
                {
                    // Add pattern to the cluster
                    cluster.push_back(*pattern_it);
                    // Remove pattern from list
                    patterns.erase(pattern_it);
                }
            }
        }
        // Merge cluster and add to result
        result.push_back(merge(cluster));
    }
    // Filtered table
    patterns = result;
}

std::vector<r_Minterval>
r_Stat_Tiling::compute_tiles(const r_Minterval &domain, r_Bytes typelen) const
{
    auto num_dims = domain.dimension();                   // Dimensionality of dom
    if (domain.dimension() != dimension)
    {
        LERROR << "r_Stat_Tiling::compute_tiles(" << domain << ", " << typelen 
               << ") dimension (" << dimension << ") does not match dimension of object to tile (" << num_dims << ")";
        throw r_Edim_mismatch(dimension, num_dims);
    }
    if (typelen > tile_size)
    {
        LERROR << "r_Stat_Tiling::compute_tiles(" << domain << ", " << typelen 
               << ") tile size (" << tile_size << ") is smaller than type length (" << typelen << ")";
        throw r_Error(TILESIZETOOSMALL);
    }

    if (iareas.empty())                               // No interest areas
    {
        // Perform regular tiling
        return r_Size_Tiling::compute_tiles(domain, typelen);
    }
    else                                              // We have interest areas
    {
        // Use interest areas for tiling the domain
        r_Interest_Tiling tiling(dimension, iareas, get_tile_size(), r_Interest_Tiling::SUB_TILING);
        return tiling.compute_tiles(domain, typelen);
    }
}

//***************************************************************************

r_Access::r_Access(const r_Minterval &pattern, r_ULong accesses) :
    interval(pattern), times(accesses)
{
}
const r_Minterval &r_Access::get_pattern() const
{
    return interval;
}
void r_Access::set_pattern(const r_Minterval &pattern)
{
    interval = pattern;
}
r_ULong r_Access::get_times() const
{
    return times;
}
void r_Access::set_times(r_ULong accesses)
{
    times = accesses;
}
bool r_Access::is_near(const r_Access &other, r_ULong border_threshold) const
{
    const auto &a = this->interval;
    const auto &b = other.interval;
    const auto num_dims = interval.dimension();
    if (num_dims != b.dimension())
    {
        LERROR << "r_Access::is_near(" << other << ", " << border_threshold 
               << ") parameter 1 does not match my dimension (" << num_dims << ")";
        throw r_Edim_mismatch(num_dims, b.dimension());
    }
    // For all dimensions
    bool the_same = true;
    for (r_Dimension i = 0; i < num_dims; i++)
    {
        // Higher/lower limit does not exceed border threshold
        if (labs(a[i].high() - b[i].high()) > border_threshold ||
            labs(a[i].low() - b[i].low()) > border_threshold)
        {
            the_same = false;
            break;
        }
    }
    return the_same;
}

void r_Access::merge_with(const r_Access &other)
{
    interval.closure_with(other.interval);
    times += other.times;
}

void r_Access::print_status(std::ostream &os) const
{
    os << "{" << times << "x: " << interval << "}";
}
std::ostream &operator<<(std::ostream &os, const r_Access &access)
{
    access.print_status(os);
    return os;
}
bool r_Access::operator==(const r_Access &other) const
{
    return ((this->interval == other.interval) && (this->times == other.times));
}
bool r_Access::operator!=(const r_Access &other) const
{
    return !(*this == other);
}
