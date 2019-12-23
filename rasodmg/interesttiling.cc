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
 * SOURCE: interesttiling.cc
 *
 * MODULE: rasodmg
 * CLASS:  r_Interest_Tiling
 *
 * COMMENTS:
 *      None
*/

#include "raslib/error.hh"
#include "rasodmg/interesttiling.hh"
#include "rasodmg/alignedtiling.hh"
#include "rasodmg/dirdecompose.hh"
#include "rasodmg/dirtiling.hh"

#include <logging.hh>

#include <cmath>
#include <cstdlib>

// This is a data structure for internal use within interesttiling.cc.
// It is defined here because of problems with ptrepository.
// -------------
// This structure consists of a r_Minterval and a counter. Also, it has
// some auxiliary constructors.

class Classified_Block
{
public:
    int intersection_count;                         // Intersection count
    r_Minterval block;                              // The actual block
    // Default constructor
    Classified_Block(int count = 0);
    // Constructor with the actual block and the counter
    Classified_Block(const r_Minterval &b, int count = 0);
    // Same data structure (this operator is needed because of dlist)
    bool operator==(const Classified_Block &other) const;
    // Different data structure (this operator is needed because of dlist)
    bool operator!=(const Classified_Block &other) const;
    // Friend of std::ostream (this operator is needed because of dlist)
    friend std::ostream &operator<<(std::ostream &os, const Classified_Block &block);
};

std::ostream &operator<<(std::ostream &os, const Classified_Block block);

Classified_Block::Classified_Block(int count)
    :   intersection_count(count)
{
}
Classified_Block::Classified_Block(const r_Minterval &b, int count)
    :   intersection_count(count),
        block(b)
{
}
bool Classified_Block::operator==(const Classified_Block &other) const
{
    return block == other.block && intersection_count == other.intersection_count;
}
bool Classified_Block::operator!=(const Classified_Block &other) const
{
    return !(*this == other);
}
std::ostream &operator<<(std::ostream &os, const Classified_Block block)
{
    os << "CBlock(" << block.intersection_count << "x: " << block.block << ")";
    return os;
}

// -----------------------------------------------------------------------------

const char *
r_Interest_Tiling::description = "dimensions, areas of interest, tile size (in bytes) and tile size limit [NOLIMIT|REGROUP|SUBTILING|REGROUPSUBTILING] (ex: \"2;[0:9,0:9];[100:109,0:9];100;REGROUPSUBTILING\")";

const char *r_Interest_Tiling::tilesizelimit_name_nolimit       = "NOLIMIT";
const char *r_Interest_Tiling::tilesizelimit_name_regroup       = "REGROUP";
const char *r_Interest_Tiling::tilesizelimit_name_subtiling     = "SUBTILING";
const char *r_Interest_Tiling::tilesizelimit_name_regroupandsubtiling   = "REGROUPSUBTILING";
const char *r_Interest_Tiling::all_tilesizelimit_names[r_Interest_Tiling::NUMBER] =
{
    tilesizelimit_name_nolimit,
    tilesizelimit_name_regroup,
    tilesizelimit_name_subtiling,
    tilesizelimit_name_regroupandsubtiling
};

r_Interest_Tiling::Tilesize_Limit
r_Interest_Tiling::get_tilesize_limit_from_name(const char *name)
{
    if (!name)
    {
        LWARNING << "no name specified.";
        return r_Interest_Tiling::NUMBER;
    }
    unsigned int i = r_Interest_Tiling::NUMBER;
    for (i = 0; i < static_cast<unsigned int>(r_Interest_Tiling::NUMBER); i++)
        if (strcasecmp(name, all_tilesizelimit_names[i]) == 0)
            break;
    return static_cast<r_Interest_Tiling::Tilesize_Limit>(i);
}

const char *
r_Interest_Tiling::get_name_from_tilesize_limit(Tilesize_Limit tsl)
{
    static const char *unknown = "UNKNOWN";
    auto idx = static_cast<unsigned int>(tsl);

    if (idx >= static_cast<unsigned int>(r_Interest_Tiling::NUMBER))
        return unknown;

    return all_tilesizelimit_names[idx];
}

r_Interest_Tiling::r_Interest_Tiling(const char *encoded)
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
    pTemp = pRes;

//parse interest areas
    pRes = advance_to_next_char(pTemp, TCOLON);
    
    while (pRes)
    {
        //is interest areas?
        if (*pTemp != *LSQRBRA)
            break;

        //copy parsed interest area
        pToConvertPtr = copy_buffer(pTemp, static_cast<r_Bytes>(pRes - pTemp));
        try
        {
            iareas.emplace_back(pToConvertPtr.get());
        }
        catch (r_Error &err)
        {
            LERROR << "Error decoding interest area from \"" << pToConvertPtr.get() 
                   << "\", error " << err.get_errorno() << " : " << err.what();
            throw r_Error(TILINGPARAMETERNOTCORRECT);
        }

        //skip COLON
        check_premature_stream_end(pRes, pEnd);
        pRes++;
        pTemp = pRes;
        pRes = advance_to_next_char(pTemp, TCOLON);
    }

    if (iareas.empty())
    {
        LERROR << "Error decoding interest areas, no interest areas specified.";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }

//deal with tile size
    pToConvertPtr = copy_buffer(pTemp, static_cast<r_Bytes>(pRes - pTemp));
    tile_size = parse_unsigned(pToConvertPtr.get());

//skip COLON
    check_premature_stream_end(pRes, pEnd);
    pRes++;
    pTemp = pRes;
    ts_strat = r_Interest_Tiling::get_tilesize_limit_from_name(pTemp);
    if (ts_strat == r_Interest_Tiling::NUMBER)
    {
        LERROR << "Error decoding tile size limit from \"" << pTemp << "\".";
        throw r_Error(TILINGPARAMETERNOTCORRECT);
    }
}

r_Interest_Tiling::r_Interest_Tiling(r_Dimension dim, const std::vector<r_Minterval> &interest_areas, r_Bytes ts, Tilesize_Limit strat)
    :   r_Dimension_Tiling(dim, ts),
        ts_strat(strat),
        iareas(interest_areas)
{
    for (auto it = iareas.begin(); it != iareas.end(); it++)
    {
        if (it->dimension() != dimension)
        {
            LERROR << "r_Interest_Tiling::r_Interest_Tiling(" << dim << ", " 
                   << interest_areas << ", " << ts << ", " << static_cast<int>(strat) 
                   << ") the interest area domain " << *it << " does not match the dimension of this tiling scheme (" << dimension << ")";
            throw r_Edim_mismatch(dimension, it->dimension());
        }
    }
}

r_Tiling *r_Interest_Tiling::clone() const
{
    return new r_Interest_Tiling(dimension, iareas, tile_size, ts_strat);
}

void r_Interest_Tiling::print_status(std::ostream &os) const
{
    os << "r_Interest_Tiling[ ";
    r_Dimension_Tiling::print_status(os);
    os << " interest areas = " << iareas << ", tiling strategy = " << static_cast<int>(ts_strat) << " ]";
}

r_Tiling_Scheme
r_Interest_Tiling::get_tiling_scheme() const
{
    return r_InterestTiling;
}

static int r_Range_comp(const void *elem1, const void *elem2)
{
    r_Range e1 = *(static_cast<const r_Range *>(elem1));
    r_Range e2 = *(static_cast<const r_Range *>(elem2));
    return e1 == e2 ? 0 : (e1 < e2 ? -1 : 1);
}

std::vector<r_Dir_Decompose>
r_Interest_Tiling::make_partition(const r_Minterval &domain) const
{
    const auto dim = domain.dimension();
    const auto total = 2 * iareas.size();

    // We need one decomp from each dimension
    std::vector<r_Dir_Decompose> part(dim);

    // We have at most (number of interest areas + 2) intervals
    std::unique_ptr<r_Range[]> intervals(new r_Range[total + 2]);

    // For all dimensions
    for (r_Dimension i = 0; i < dim; i++)
    {
        auto it = iareas.begin();
        intervals[0] = domain[i].low();               // Input lower domain limit
        intervals[total + 1] = domain[i].high();      // Input higher domain limit

        for (unsigned int j = 1; j < total + 1; j += 2, ++it)          // For all possible intervals
        {
            if ((*it)[i].low() - 1 <= domain[i].low())  // Input low iarea limit
                intervals[j] = domain[i].low();
            else
                intervals[j] = (*it)[i].low() - 1;

            intervals[j + 1] = (*it)[i].high();         // Input higher iarea limit
        }

        // Sort the table
        qsort(static_cast<void *>(intervals.get()), total + 2, sizeof(r_Range), r_Range_comp);

        // Create partition using the limits table
        for (unsigned int k = 0; k < total + 2; k++)    // all limits must be checked
        {
            if (k == total + 1)                         // if on the last limit...
                part[i] << intervals[k];
            else if (intervals[k] != intervals[k + 1])  //   if it is unique
                part[i] << intervals[k];
        }
    }

    // Return result
    return part;
}

std::vector<r_Minterval>
r_Interest_Tiling::group(std::vector<r_Minterval> &blocks, r_Bytes typelen, Blocks_Type btype) const
{
    int joins = 0;

    // The list of threated blocks
    std::vector<r_Minterval> treated;

    // For all the blocks in list
    while (!blocks.empty())
    {
        // Get first block from list
        auto current_block = blocks.back();
        blocks.pop_back();

        //this is neccessary when the compiler optimizes out the .end() check
        auto numberOfLevels = blocks.size();
        for (auto blocks_it = blocks.begin(); blocks_it != blocks.end(); blocks_it++)
        {
            if (numberOfLevels == 0)
            {
                LERROR << "the for loop was incorrectly optimized, breaking the loop.";
                break;
            }
            const auto &aux = *blocks_it;

            // In principle two blocks can't be merged
            bool group_blocks = false;

            // If they can be merged
            if (current_block.is_mergeable(aux))
            {
                switch (btype)
                {
                case BLOCKS_A:

                    group_blocks = true;

                    // Check if the two blocks belong exaclty to the same iareas
                    for (auto ia_it = blocks.begin(); ia_it != blocks.end(); ia_it++)
                    {
                        if (aux.intersects_with(*ia_it) != current_block.intersects_with(*ia_it))
                        {
                            group_blocks = false;
                            break;
                        }
                    }
                    break;

                case BLOCKS_B:

                    for (auto ia_it = blocks.begin(); ia_it != blocks.end(); ia_it++) // For all iareas
                    {
                        // Find the one this block intersects
                        if (current_block.intersects_with(*ia_it))
                        {
                            group_blocks = aux.intersects_with(*ia_it);
                            break;
                        }
                    }
                    if (!group_blocks)
                        break;
                    group_blocks = false;

                case BLOCKS_C: // Falls in (this is, also applies to B);

                    // Only on this two strategies, tilesize should be looked at
                    if (ts_strat == REGROUP || ts_strat == REGROUP_AND_SUBTILING)
                    {
                        // If the resulting size isn't larger than tilesize
                        if ((current_block.cell_count() + aux.cell_count()) * typelen < get_tile_size())
                            group_blocks = true;
                    }
                    else
                    {
                        group_blocks = true;
                    }
                    break;
                    
                default:
                    break;
                }
            }

            // take care of the iterator advance, if is possible
            if (group_blocks)
            {
                // take care of the size of the blocks
                numberOfLevels--;
                current_block.closure_with(aux);
                ++joins;
                // Merge them
                blocks.erase(blocks_it);
            }
            else
            {
                numberOfLevels--;
            }
        }

        // Update the treated list with the current block
        treated.push_back(current_block);
    }

    // If there were joins, the algoritm must be repeted
    return joins > 0 ? group(treated, typelen, btype) : treated;
}

std::vector<r_Minterval>
r_Interest_Tiling::compute_tiles(const r_Minterval &domain, r_Bytes typelen) const
{
    auto num_dims = domain.dimension();                   // Dimensionality of dom
    if (domain.dimension() != dimension)
    {
        LERROR << "r_Interest_Tiling::compute_tiles(" << domain << ", " << typelen 
               << ") dimension (" << dimension << ") does not match dimension of object to tile (" << num_dims << ")";
        throw r_Edim_mismatch(dimension, num_dims);
    }
    if (typelen > tile_size)
    {
        LERROR << "r_Interest_Tiling::compute_tiles(" << domain << ", " << typelen 
               << ") tile size (" << tile_size << ") is smaller than type length (" << typelen << ")";
        throw r_Error(TILESIZETOOSMALL);
    }

    // *** Main algoritm ***

    // The result
    std::vector<r_Minterval> result;

    // Create a partition for dir tiling
    auto part = make_partition(domain);

    // Perform dirtiling
    r_Dir_Tiling dir_tiling(num_dims, part, tile_size, r_Dir_Tiling::WITHOUT_SUBTILING);
    auto dir_domain = dir_tiling.compute_tiles(domain, typelen);

    // Create a list for holding the classifed blocks
    std::vector<Classified_Block> part_domain;
    // Finds how many intersections exist between a block an the interest areas
    for (const auto &dir_block: dir_domain)
    {
        Classified_Block b(dir_block, 0);
        for (const auto &interest_area: iareas)
        {
            if (b.block.intersects_with(interest_area))
                ++b.intersection_count;
        }
        part_domain.push_back(b);
    }

    // Lists used for grouping blocks
    std::vector<r_Minterval> Out;
    std::vector<r_Minterval> In_Unique;
    std::vector<r_Minterval> In_Common;
    // Divide blocks into lists according to their number of intersections
    for (const auto &class_block: part_domain)
    {
        switch (class_block.intersection_count)
        {
        case 0: Out.push_back(class_block.block); break;
        case 1: In_Unique.push_back(class_block.block); break;
        default: In_Common.push_back(class_block.block); break;
        }
    }

    // Group blocks
    auto Blocks_A = group(In_Common, typelen, BLOCKS_A);
    auto Blocks_B = group(In_Unique, typelen, BLOCKS_B);
    auto Blocks_C = group(Out, typelen, BLOCKS_C);
    std::vector<r_Minterval> *blocks_vec[3] = {&Blocks_A, &Blocks_B, &Blocks_C};
    
    // For all the lists (Blocs_A, Blocks_B and Blocks_C)
    for (int j = 0; j < 3; j++)
    {
        // If may be necessary to perform sub-tiling
        if (ts_strat == SUB_TILING || ts_strat == REGROUP_AND_SUBTILING)
        {
            // Tile each block if necessary
            for (auto it = blocks_vec[j]->begin(); it != blocks_vec[j]->end(); it++)
            {
                if (it->cell_count() * typelen > get_tile_size())
                {
                    // Create a specification of a regular n-dim cube grid
                    r_Minterval specs(num_dims);
                    for (r_Dimension i = 0; i < num_dims; i++)
                        specs << r_Sinterval(0ll, (*it)[i].high() - (*it)[i].low());
    
                    // Class for performing sub-tiling
                    r_Aligned_Tiling subtiling(specs, get_tile_size());
    
                    auto subtiles = subtiling.compute_tiles(*it, typelen);
                    for (const auto &subtile: subtiles)
                        result.push_back(subtile);
                }
                else // No subtiling needed
                {
                    // Insert block as it is
                    result.push_back(*it);
                }
            }
        }
        else
        {
            // The result is just the sum of all blocks
            result.insert(result.end(), blocks_vec[j]->begin(), blocks_vec[j]->end());
        }
    }

    // Return result
    return result;
}
