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
 * INCLUDE: gmarray.hh
 *
 * MODULE:  rasodmg
 * CLASS:   r_GMarray
 *
 * COMMENTS:
 *      None
*/

#ifndef _D_GMARRAY_
#define _D_GMARRAY_

#include "rasodmg/object.hh"
#include "rasodmg/set.hh"
#include "raslib/minterval.hh"
#include "raslib/mddtypes.hh"
#include "raslib/banditerator.hh"
#include <iosfwd>
#include <vector>

// forward declarations
class r_Storage_Layout;
class r_Point;
class r_Base_Type;
class r_Transaction;

//@ManMemo: Module: {\bf rasodmg}

/*@Doc:

  The class represents a generic MDD in the sense that it
  is independent of the cell base type. The only information
  available is the length in bytes of the base type.
  More specific MDDs including base type information for more
  type safety are represented by the template subclass r_Marray.
  Class r_Marray provides a constructor to convert to the base
  type specific class.
*/

/**
  * \ingroup Rasodmgs
  */
class r_GMarray : public r_Object
{
public:
    /// default constructor (no memory is allocated!)
    r_GMarray(r_Transaction *transaction = NULL);

    /// constructor for uninitialized MDD objects
    r_GMarray(const r_Minterval &init_domain, r_Bytes type_length, r_Storage_Layout *stl = 0,
              r_Transaction *transaction = NULL, bool initialize = true);
    /**
      If a storage layout pointer is provided, the object refered to is
      taken and memory control moves to the r_GMarray class.
      The user has to take care, that each creation of r_GMarray
      objects get a new storage layout object.
      r_Error is throw if the storage layout does not fit the type length or the 
      dimension of the init domain and when the dimension of the domain is 0 (uninitialised).
    */

    /// copy constructor
    r_GMarray(const r_GMarray &);

    /// constructor which doesn't copy the data
    r_GMarray(r_GMarray &);

    /// destructor
    virtual ~r_GMarray();

    /// it is called when an object leaves transient memory (internal use only)
    virtual void r_deactivate();

    /// assignment: cleanup + copy
    r_GMarray &operator= (const r_GMarray &);

    /// subscript operator for read access of a cell.
    /// Not supported on channel-interleaved arrays, use r_Band_Iterator in that case.
    const char *operator[](const r_Point &) const;

    /// Returns a r_GMarray that is the intersection of the current domain with the specified interval
    r_GMarray *intersect(const r_Minterval &where) const;

    //@Man: Read methods
    //@{
    ///

    /// gets a pointer to the storage layout object
    const r_Storage_Layout *get_storage_layout() const;
    /// getting the spatial domain
    const r_Minterval &spatial_domain() const;
    /// get the internal representation of the array
    char         *get_array();
    /// get the internal representation of the array for reading
    const char   *get_array() const;
    /// get the internal representation of the array
    r_Set<r_GMarray *>        *get_tiled_array();
    /// get the internal representation of the array for reading
    const r_Set<r_GMarray *>  *get_tiled_array() const;
    /// get size of internal array representation in byets
    r_Bytes get_array_size() const;
    /// get length of cell type in bytes
    r_Bytes get_type_length() const;
    /// get current data format
    r_Data_Format get_current_format() const;

    /// get base type schema
    const r_Base_Type *get_base_type_schema();
    
    /// get a band iterator 
    r_Band_Iterator get_band_iterator(unsigned int band);
    
    /// @return band linearization, relevant in case of multi-band array
    r_Band_Linearization get_band_linearization() const;    
    /// @return cell linearization
    r_Cell_Linearization get_cell_linearization() const;

    ///
    //@}

    //@Man: Write methods
    //@{
    ///
    /// sets the storage layout object and checks compatibility with the domain
    void set_storage_layout(r_Storage_Layout *);
    /// set spatial domain
    void  set_spatial_domain(const r_Minterval &domain);
    /// set the internal representation of the array
    void  set_array(char *);
    /// set the internal representation of the array
    void  set_tiled_array(r_Set<r_GMarray *> *newData);
    /// set size of internal memory representation in bytes
    void  set_array_size(r_Bytes);
    /// set length of cell type in bytes
    void  set_type_length(r_Bytes);
    /// set current data format
    void  set_current_format(r_Data_Format);
    /// set band linearization to pixel-interleaved or channel-interleaved in
    /// case of multi-band array
    void  set_band_linearization(r_Band_Linearization);
    /// set cell linearization. Only ColumnMajor is supported currently.
    void  set_cell_linearization(r_Cell_Linearization);

    ///
    //@}

    //@Man: Methods for database communication (internal use only)
    //@{
    ///

    /// inserts an object into the database
    virtual void insert_obj_into_db();
    /// insert myself into a specific collection in the database
    void insert_obj_into_db(const char *collName);

    ///
    //@}

    /// writes the state of the object to the specified stream
    virtual void print_status(std::ostream &s);

    /// writes the state of the object to the specified stream
    void print_status(std::ostream &s, int hexoutput);
    

protected:
    /// spatial domain
    r_Minterval domain;

    /// pointer to the internal array representation
    char *data{NULL};

    /// array internally sub-tiled
    r_Set<r_GMarray *> *tiled_data{NULL};

    /// size of internal array representation in bytes
    r_Bytes data_size{};

    /// length of the cell base type in bytes
    r_Bytes type_length{};

    /// store current data format
    r_Data_Format current_format{r_Array};

    /// pointer to storage layout object
    r_Storage_Layout *storage_layout{NULL};
    
    /// relevant if data has multiple bands (channels) of data
    r_Band_Linearization band_linearization{r_Band_Linearization::PixelInterleaved};
    
    /// cell linearization.
    /// Note: only ColumnMajor supported currently.
    r_Cell_Linearization cell_linearization{r_Cell_Linearization::ColumnMajor};
};

#endif
