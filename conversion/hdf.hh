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
 * INCLUDE: hdf.hh
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_HDF
 *
 * COMMENTS:
 *
 * Provides interface to convert data to other formats.
 *
*/

#ifndef _R_CONV_HDF_HH_
#define _R_CONV_HDF_HH_

#include "conversion/convertor.hh"
#include "config.h"

//@ManMemo: Module {\bf conversion}

/*@Doc:
  HDF convertor class.
*/
class r_Conv_HDF : public r_Convertor
{
public:
    /// constructor using an r_Type object. Exception if the type isn't atomic.
    r_Conv_HDF(const char* src, const r_Minterval& interv, const r_Type* tp);
    /// constructor using convert_type_e shortcut
    r_Conv_HDF(const char* src, const r_Minterval& interv, int tp);
    /// destructor
    ~r_Conv_HDF(void);

    /// convert to HDF
    virtual r_Conv_Desc& convertTo(const char* options = NULL,
                                   const r_Range* nullValue = NULL);
    /// convert from HDF
    virtual r_Conv_Desc& convertFrom(const char* options = NULL);
    /// convert data in a specific format to array
    virtual r_Conv_Desc& convertFrom(r_Format_Params options);
    /// cloning
    virtual r_Convertor* clone(void) const;
    /// identification
    virtual const char* get_name(void) const;
    virtual r_Data_Format get_data_format(void) const;


private:
    /// init HDF module
    void initHDF(void);
    /// variables
    int datatype, datasize;
    /// parameters
    int skiphuff;
    int quality;
    char* compType;
#ifdef HAVE_HDF
    static const convert_string_t compNames[];
#endif
    static const int MaxSwapBufferSize;
};

#endif
