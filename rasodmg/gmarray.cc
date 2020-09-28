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
 * SOURCE: gmarray.cc
 *
 * MODULE: rasodmg
 * CLASS:  r_GMarray
 *
 * COMMENTS:
 *      None
*/

#include "rasodmg/marray.hh"
#include "rasodmg/database.hh"
#include "rasodmg/storagelayout.hh"
#include "rasodmg/alignedtiling.hh"
#include "rasodmg/transaction.hh"
#include "clientcomm/clientcomm.hh"
#include "mymalloc/mymalloc.h"

#include "raslib/type.hh"
#include "raslib/marraytype.hh"
#include "raslib/structuretype.hh"
#include "raslib/point.hh"
#include "raslib/error.hh"

#include <logging.hh>

#include <string.h>  // for memcpy()
#include <iostream>
#include <iomanip>
#include <vector>

r_GMarray::r_GMarray(r_Transaction *ta)
    : r_Object(1, ta), storage_layout{new r_Storage_Layout()}
{
}

r_GMarray::r_GMarray(const r_Minterval &initDomain, r_Bytes initLength, r_Storage_Layout *stl,
                     r_Transaction *ta, bool initialize)
    : r_Object(1, ta), domain(initDomain), type_length(initLength), storage_layout(stl)
{
    int error = 0;
    if (domain.dimension() == 0)
    {
        LERROR << "empty domain: " << initDomain;
        delete storage_layout;
        throw r_Error(DOMAINUNINITIALISED);
    }
    else
    {
        if (storage_layout == NULL)
        {
            storage_layout = new r_Storage_Layout(new r_Aligned_Tiling(initDomain.dimension()));
        }
        if (!storage_layout->is_compatible(initDomain, initLength))
        {
            LERROR << "storage layout is not compatible";
            delete storage_layout;
            throw r_Error(STORAGERLAYOUTINCOMPATIBLEWITHGMARRAY);
        }
    }
    data_size = domain.cell_count() * initLength;
    if (initialize)
    {
        data = new char[data_size];
        memset(data, 0, data_size);
    }
}

r_GMarray::r_GMarray(const r_GMarray &obj)
    : r_Object(obj, 1), domain(obj.spatial_domain()),
      type_length(obj.type_length), current_format(obj.current_format)
{
    if (obj.data)
    {
        data_size = obj.data_size;
        data = new char[data_size];
        memcpy(data, obj.data, data_size);
    }
    if (obj.storage_layout)
        storage_layout = obj.storage_layout->clone();
}

r_GMarray::r_GMarray(r_GMarray &obj)
    : r_Object(obj, 1),
      domain(obj.spatial_domain()), data(obj.data), tiled_data(obj.tiled_data),
      data_size(obj.data_size), type_length(obj.type_length), current_format(obj.current_format)
{
    obj.data_size      = 0;
    obj.data           = 0;
    obj.tiled_data     = 0;
    obj.domain         = r_Minterval();
    obj.type_length    = 0;
    if (obj.storage_layout)
    {
        storage_layout = obj.storage_layout->clone();
        obj.storage_layout = 0;
    }
}

r_GMarray::~r_GMarray()
{
    r_deactivate();
}

void
r_GMarray::r_deactivate()
{
    if (data)
    {
        delete[] data;
        data = 0;
    }
    if (tiled_data)
    {
        delete tiled_data;
        tiled_data = NULL;
    }
    if (storage_layout)
    {
        delete storage_layout;
        storage_layout = 0;
    }
}

const char *
r_GMarray::operator[](const r_Point &point) const
{
    return &(data[domain.cell_offset(point) * type_length]);
}

const r_Storage_Layout *
r_GMarray::get_storage_layout() const
{
    return storage_layout;
}

void
r_GMarray::set_storage_layout(r_Storage_Layout *stl)
{
    if (!stl->is_compatible(domain, type_length))
    {
        LERROR << "r_GMarray::set_storage_layout(" << *stl << ") gmarray is not compatible with tiling"
               << "\n\tgmarray domain   : " << spatial_domain()
               << "\n\tgmarray type size: " << get_type_length();
        throw r_Error(STORAGERLAYOUTINCOMPATIBLEWITHGMARRAY);
    }
    if (storage_layout != NULL)
        delete storage_layout;
    storage_layout = stl;
}

r_GMarray &
r_GMarray::operator=(const r_GMarray &marray)
{
    if (this != &marray)
    {
        if (data)
        {
            delete[] data;
            data = 0;
        }
        if (marray.data)
        {
            data_size = marray.data_size;
            data = new char[data_size];
            memcpy(data, marray.data, data_size);
        }
        if (storage_layout)
        {
            delete storage_layout;
            storage_layout = 0;
        }
        if (marray.storage_layout)
        {
            storage_layout = marray.storage_layout->clone();
        }
        domain         = marray.domain;
        type_length    = marray.type_length;
        current_format = marray.current_format;
    }
    return *this;
}

void
r_GMarray::insert_obj_into_db()
{
    // Nothing is done in that case. r_Marray objects can just be inserted as elements
    // of a collection which invokes r_GMarray::insert_obj_into_db(const char* collName)
    // of the r_Marray objects.
}

void
r_GMarray::insert_obj_into_db(const char *collName)
{
    update_transaction();

    // Insert myself in database only if I have a type name
    if (!type_name || !transaction || !transaction->getDatabase())
        throw r_Error(r_Error::r_Error_DatabaseClassUndefined);

    transaction->getDatabase()->getComm()->insertMDD(collName, this);
}

void
r_GMarray::print_status(std::ostream &s)
{
    const r_Type       *typeSchema     = get_type_schema();
    const r_Base_Type  *baseTypeSchema = get_base_type_schema();

    s << "GMarray";
    s << "\n  Oid...................: " << get_oid();
    s << "\n  Type Structure........: " << (type_structure ? type_structure : "<nn>");
    s << "\n  Type Schema...........: ";
    if (typeSchema)
        typeSchema->print_status(s);
    else
        s << "<nn>";
    s << "\n  Domain................: " << domain;
    s << "\n  Base Type Schema......: ";
    if (baseTypeSchema)
        baseTypeSchema->print_status(s);
    else
        s << "<nn>" << std::flush;
    s << "\n  Base Type Length......: " << type_length;
    s << "\n  Data format.......... : " << current_format;
    s << "\n  Data size (bytes).... : " << data_size << std::endl;
}

void
r_GMarray::print_status(std::ostream &s, int hexoutput)
{
    print_status(s);
    const r_Type       *typeSchema     = get_type_schema();
    const r_Base_Type  *baseTypeSchema = get_base_type_schema();

    if (domain.dimension())
    {
        auto p = domain.get_origin();
        bool done = false;

        // iterate over all cells
        while (!done)
        {
            // get cell address
            char *cell = data + domain.cell_offset(p) * type_length;

            if (hexoutput)
            {
                for (r_Bytes j = 0; j < type_length; j++)
                    s << std::hex << static_cast<int>(cell[j]);
            }
            else
            {
                if (baseTypeSchema)
                    baseTypeSchema->print_value(cell,  s);
                else
                    s << "<nn>" << std::flush;
            }

            s << "   ";

            // increment coordinate
            r_Dimension i = 0;
            while (++p[i] > domain[i].high())
            {
                s << std::endl;
                p[i] = domain[i].low();
                i++;
                if (i >= domain.dimension())
                {
                    done = true;
                    break;
                }
            }
            if (i > 1)
                s << std::endl;
        }
    }
    else
    {
        s << "Cell value ";

        // print cell
        if (hexoutput || !baseTypeSchema)
        {
            for (unsigned int j = 0; j < type_length; j++)
                s << std::hex << static_cast<int>(data[j]);
        }
        else
        {
            if (baseTypeSchema)
                baseTypeSchema->print_value(data,  s);
            else
                s << "<nn>";
        }
        s << std::endl;
    }
    // turn off hex mode again
    s << std::dec << std::flush;
}

r_GMarray *r_GMarray::intersect(const r_Minterval &where) const
{
    r_GMarray *tile = new r_GMarray(get_transaction());

    const auto &obj_domain = spatial_domain();
    const auto num_dims = obj_domain.dimension();
    const auto tlength = get_type_length();

    char *obj_data = new char[where.cell_count() * tlength];
    tile->set_spatial_domain(where);
    tile->set_type_length(tlength);
    tile->set_array(obj_data);
    tile->set_array_size(where.cell_count() * tlength);

    r_Bytes block_length = static_cast<r_Bytes>(where[num_dims - 1].high() - where[num_dims - 1].low() + 1);
    r_Bytes total = where.cell_count() / block_length;

    for (r_Area cell = 0; cell < total; cell++)
    {
        r_Point p = where.cell_point(cell * block_length);

        char *dest_off = obj_data;
        const char *source_off = get_array();

        memcpy(dest_off + where.cell_offset(p) * tlength,
               source_off + obj_domain.cell_offset(p) * tlength,
               block_length * tlength);
    }
    
    return tile;
}

const r_Base_Type *
r_GMarray::get_base_type_schema()
{
    const r_Type      *typePtr     = r_Object::get_type_schema();
    const r_Base_Type *baseTypePtr = 0;

    if (typePtr)
    {
        if (typePtr->type_id() == r_Type::MARRAYTYPE)
        {
            const r_Marray_Type *marrayTypePtr = static_cast<const r_Marray_Type *>(typePtr);
            baseTypePtr = &(marrayTypePtr->base_type());
        }
        else
        {
            LERROR << "the type retrieved (" << typePtr->name() << ") was not an marray type";
            throw r_Error(NOTANMARRAYTYPE);
        }
    }

    return baseTypePtr;
}

const r_Minterval &
r_GMarray::spatial_domain() const
{
    return domain;
}

char *
r_GMarray::get_array()
{
    return data;
}

const char *
r_GMarray::get_array() const
{
    return data;
}

r_Set< r_GMarray * > *
r_GMarray::get_tiled_array()
{
    return tiled_data;
}

const r_Set< r_GMarray * > *
r_GMarray::get_tiled_array() const
{
    return tiled_data;
}

void
r_GMarray::set_array(char *newData)
{
    // In case the array already has an array allocated, free it first.
//  if (data != NULL) delete [] data;
    data = newData;
}

void
r_GMarray::set_tiled_array(r_Set< r_GMarray * > *newData)
{
    tiled_data = newData;
}

void
r_GMarray::set_current_format(r_Data_Format newFormat)
{
    current_format = newFormat;
}

r_Bytes
r_GMarray::get_type_length() const
{
    return type_length;
}

r_Bytes
r_GMarray::get_array_size() const
{
    return data_size;
}

r_Data_Format
r_GMarray::get_current_format() const
{
    return current_format;
}

void
r_GMarray::set_spatial_domain(const r_Minterval &dom)
{
    domain = dom;
}

void
r_GMarray::set_type_length(r_Bytes newValue)
{
    type_length = newValue;
}

void
r_GMarray::set_array_size(r_Bytes newValue)
{
    data_size = newValue;
}
