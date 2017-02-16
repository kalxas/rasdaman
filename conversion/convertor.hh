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
 * INCLUDE: convertor.hh
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Convertor, r_Convert_Memory
 *
  * COMMENTS:
 *
 * Provides interface to convert data to other formats.
 *
*/



#ifndef _R_CONVERTOR_
#define _R_CONVERTOR_

#include "config.h"

#include "conversion/convtypes.hh"
#include "conversion/formatparams.hh"
#include "raslib/error.hh"
#include "raslib/minterval.hh"
#include "raslib/type.hh"
#include "raslib/mddtypes.hh"
#include "raslib/storageman.hh"
#include <string>


// Declare to avoid including memfs.h (and with that tiffio.h)
struct memFSContext;

class r_Parse_Params;


typedef struct r_Conv_Desc
{
    const char* src;              // pointer to source data
    char* dest;                   // pointer to destination data
    r_Minterval srcInterv;        // dimensions of source data
    r_Minterval destInterv;       // dimensions of destination data
    int baseType;                 // shortcut to src basetype
    const r_Type* srcType;        // basetypes of src data
    r_Type* destType;             // basetypes of dest data
} r_Conv_Desc;
//@ManMemo: Module {\bf conversion}


/*@Doc:
  Conversion classes from and to data exchange formats. Can also be used for
  tile compression of special MDD types (= images)

  \begin{itemize}
  \item
  the member function convertTo() performs the conversion MDD -> DEF
  \item
  the member function convertFrom() performs the conversion DEF -> MDD
  \item
  the r_Conv_Desc reference returned from this call is only valid while
  the convertor object is.
  \item
  after successful execution the returned r_Conv_Desc structure contains
  the following information:
    \begin{itemize}
    \item
    dest: pointer to converted data, allocated by the configured heap
    storage object which will use malloc() by default (see
    set_storage_handler()); must be deallocated by the caller.
    \item
    destInterv: r_Minterval describing the converted object's domain.
    \item
    destType: pointer to an r_Type object describing the converted
    object's type; must be deallocated by the caller.
    \end{itemize}
  \item
  on failure an exception is thrown.
  \end{itemize}

  The member function convertTo() receives a parameter string as argument
  which is NULL for default parameters. The format of the string is such
  that it can be parsed by r_Parse_Params. The params member variable is
  initialized to NULL in this class. Derived classes that wish to add
  parameters must first check whether params is NULL and create a new
  object if so, then add their parameters. This makes it possible to
  accumulate parameters over all class hierarchies.
*/

class r_Convertor
{
public:
    /// default constructor (should not be used)
    r_Convertor(void);
    /// constructor using an r_Type object
    r_Convertor(const char* src, const r_Minterval& interv, const r_Type* tp,
                bool fullTypes = false) throw(r_Error);
    /// constructor using convert_type_e shortcut
    r_Convertor(const char* src, const r_Minterval& interv, int type) throw(r_Error);

    /// destructor
    virtual ~r_Convertor(void);

    //@Man: Interface
    /// convert array to a specific format
    virtual r_Conv_Desc& convertTo(const char* options = NULL) throw(r_Error) = 0;

    /// convert data in a specific format to array
    virtual r_Conv_Desc& convertFrom(const char* options = NULL) throw(r_Error) = 0;

    /// convert data in a specific format to array
    virtual r_Conv_Desc& convertFrom(r_Format_Params options) throw(r_Error) = 0;


    /// cloning
    virtual r_Convertor* clone(void) const = 0;

    /// identification
    virtual const char* get_name(void) const = 0;
    virtual r_Data_Format get_data_format(void) const = 0;

    /// set conversion format, used only by r_Conv_GDAL at the moment
    virtual void set_format(const std::string& formatArg);

    /// set storage handler, default is malloc/free
    void set_storage_handler(const r_Storage_Man& newStore);

    /// get storage handler, default is malloc/free
    const r_Storage_Man& get_storage_handler() const;

    //@{ helper structure for encoding string-to-int parameters
    typedef struct convert_string_s
    {
        const char* key;
        int id;
    } convert_string_t;
    //@}

    /// get a string representation of the internal type
    static std::string type_to_string(int ctype) throw(r_Error);

    /// get a r_Type from an internal type
    static r_Type* get_external_type(int ctype) throw(r_Error);

    /// get a internal type from a r_Type
    static convert_type_e get_internal_type(const r_Type* type, bool fullTypes = false) throw(r_Error);

protected:
    /// initialize internal structures
    void initShare(const char* src, const r_Minterval& interv);

    /// transpose src 2D array of size NxM to dst of size MxN
    template <class baseType>
    void transpose(baseType* src, baseType* dst, const int N, const int M)
    {
        for (int n = 0; n < N * M; n++)
        {
            int i = n / N;
            int j = n % N;
            dst[n] = src[M * j + i];
        }
    }

    /// convert unsupported type to rgb by applying the default color scheme
    template <class baseType>
    void applyColorScheme();

    /// true if we should free the src area (in case the input was converted to rgb)
    bool destroySrc;

    /// conversion context
    r_Conv_Desc desc;

    /// parameter parser
    r_Parse_Params* params;

    /// new-style format params
    r_Format_Params formatParams;

    /// storage manager
    r_Storage_Man mystore;

    // format identifier, used only by the GDAL converter
    std::string format;


};

///ostream operator for convert_type_e
std::ostream& operator<<(std::ostream& os, convert_type_e& cte);

/*@Doc:
  Abstract base class for all memory-to-memory conversion classes,
  uses memfs for of data with unknown size at the time of creation.
*/

class r_Convert_Memory : public r_Convertor
{
public:
    /// constructor using an r_Type object
    r_Convert_Memory(const char* src, const r_Minterval& interv, const r_Type* tp,
                     int fullTypes = 0) throw(r_Error);
    /// constructur using convert_type_e shortcut
    r_Convert_Memory(const char* src, const r_Minterval& interv, int type) throw(r_Error);
    /// destructor
    virtual ~r_Convert_Memory(void);


protected:
    /// init memfs
    void initMemory(void) throw(r_Error);

    /// variables
    memFSContext* memFS;
    void* handle;
};

#endif
