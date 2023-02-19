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
 * SOURCE: storagelayout.cc
 *
 * MODULE: rasodmg
 * CLASS:  r_StorageLayout, r_Domain_Storage_Layout
 *
 * COMMENTS:
 *          None
 *
*/

#include "rasodmg/storagelayout.hh"
#include "rasodmg/tiling.hh"
#include "rasodmg/gmarray.hh"
#include "rasodmg/set.hh"
#include "raslib/minterval.hh"
#include "raslib/error.hh"

#include <logging.hh>

r_Storage_Layout::r_Storage_Layout(r_Data_Format init_format, const char *formatParams)
    :   storage_format(init_format)
{
    til = new r_Size_Tiling();
    if (formatParams != NULL)
        storage_params = formatParams;
}

r_Storage_Layout::r_Storage_Layout(r_Tiling *ts, r_Data_Format init_format, const char *formatParams)
    :   til(ts), storage_format(init_format)
{
    if (til == NULL)
        til = new r_Size_Tiling();
    if (formatParams != NULL)
        storage_params = formatParams;
}

r_Storage_Layout::r_Storage_Layout(const r_Storage_Layout &sl)
    :   til(sl.get_tiling()->clone()),
        storage_format(sl.get_storage_format()),
        storage_params(sl.storage_params)
{
}

r_Storage_Layout *
r_Storage_Layout::clone() const
{
    return new r_Storage_Layout(til->clone(), storage_format, storage_params.c_str());
}

r_Storage_Layout::~r_Storage_Layout()
{
    delete til;
    til = NULL;
}

const r_Tiling *
r_Storage_Layout::get_tiling() const
{
    return til;
}

r_Data_Format
r_Storage_Layout::get_storage_format() const
{
    return storage_format;
}

const char *
r_Storage_Layout::get_storage_format_params() const
{
    return storage_params.c_str();
}

r_Set<r_GMarray *>
r_Storage_Layout::decomposeMDD(const r_GMarray *mar) const
{
    r_Bytes cell_size = mar->get_type_length();
    auto tiles = decomposeMDD(mar->spatial_domain(), cell_size);
    r_Set<r_GMarray *> result;
    for (const auto &tile: tiles)
        result.insert_element(mar->intersect(tile));
    return result;
}

std::vector<r_Minterval>
r_Storage_Layout::decomposeMDD(const r_Minterval &domain, const r_Bytes cell_size) const
{
    if (!til->is_compatible(domain, cell_size))
    {
        LERROR << "r_Storage_Layout::decomposeMDD() gmarray is not compatible with tiling"
               << "\n\tgmarray domain   : " << domain
               << "\n\tgmarray type size: " << cell_size
               << "\n\tstorage layout   : " << *this;
        throw r_Error(STORAGERLAYOUTINCOMPATIBLEWITHGMARRAY);
    }
    return til->compute_tiles(domain, cell_size);
}

void
r_Storage_Layout::print_status(std::ostream &os) const
{
    os << "r_Storage_Layout[ tiling = " << *til << " storage format = " << storage_format 
       << " storage parameters = " << (!storage_params.empty() ? storage_params : "none defined") << " ]";
}

bool
r_Storage_Layout::is_compatible(const r_Minterval &obj_domain, r_Bytes cellTypeSize) const
{
    return til->is_compatible(obj_domain, cellTypeSize);
}

std::ostream &
operator<<(std::ostream &s, const r_Storage_Layout &sl)
{
    sl.print_status(s);
    return s;
}

