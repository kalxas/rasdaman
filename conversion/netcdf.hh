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
 * INCLUDE: netcdf.hh
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_NETCDF
 *
 * COMMENTS:
 *
 * Provides functions to convert data to NETCDF and back.
 *
 * Limitations:
 *  1. Dimension data is not preserved when importing netcdf, so when exporting
 *     it back to netcdf the dimension is written as a series 1..dimSize
 *  2. Metadata is not preserved
 */

#ifndef _R_CONV_NETCDF_HH_
#define _R_CONV_NETCDF_HH_

#include "conversion/convertor.hh"

#ifdef HAVE_NETCDF
#include <netcdfcpp.h>
#endif

#include <json/json.h>
#include <string>
#include <vector>
#include <memory>

//@ManMemo: Module {\bf conversion}

/*@Doc:
 NETCDF convertor class.

  No compression method is supported yet

 */
class r_Conv_NETCDF : public r_Convert_Memory
{
public:
    /// constructor using an r_Type object. Exception if the type isn't atomic.
    r_Conv_NETCDF(const char *src, const r_Minterval &interv, const r_Type *tp) throw (r_Error);
    /// constructor using convert_type_e shortcut
    r_Conv_NETCDF(const char *src, const r_Minterval &interv, int tp) throw (r_Error);
    /// destructor
    ~r_Conv_NETCDF(void);

    /// convert to NETCDF
    virtual r_Conv_Desc &convertTo(const char *options = NULL) throw (r_Error);
    /// convert from NETCDF
    virtual r_Conv_Desc &convertFrom(const char *options = NULL) throw (r_Error);

    /// cloning
    virtual r_Convertor *clone(void) const;

    /// identification
    virtual const char *get_name(void) const;
    virtual r_Data_Format get_data_format(void) const;

private:
    
    struct RasType
    {
        unsigned int cellSize;
        std::string cellType;
    };

    /**
     * Read data from tmpFile into desc.dest and return the file size.
     */
    void parseDecodeOptions(const char* options) throw (r_Error);
    
    void validateDecodeOptions(const NcFile& dataFile) throw (r_Error);
    
    void parseEncodeOptions(const char* options) throw (r_Error);
    
    void validateJsonEncodeOptions() throw (r_Error);
    
    /**
     * read single variable data
     */
    void readSingleVar(const NcFile &dataFile) throw (r_Error);
    
    /**
     * read multiple variable data into a struct
     */
    void readMultipleVars(const NcFile &dataFile) throw (r_Error);
    
    /**
     * read single variable data
     */
    template <class T>
    void readData(NcVar *var, convert_type_e) throw (r_Error);
    
    /**
     * read struct variable data
     */
    template <class T>
    void readDataStruct(NcVar *var, long* dimSizes, size_t dataSize, 
                        size_t structSize, size_t &offset) throw (r_Error);
    
    /**
     * Build struct type
     */
    std::unique_ptr<long[]> buildStructType(const NcFile &dataFile, size_t& dataSize, size_t& structSize, int& numDims) throw (r_Error);
    
    /**
     * Get a rasdaman type from a netcdf variable type.
     */
    RasType getRasType(NcVar *var);
    
    /**
     * write single variable data
     */
    void writeSingleVar(NcFile &dataFile, int dimNo, 
                        const NcDim** dims, long* dimSizes, size_t dataSize) throw (r_Error);
    
    /**
     * write multiple variables from a struct
     */
    void writeMultipleVars(NcFile &dataFile, int dimNo, 
                           const NcDim** dims, long* dimSizes, size_t dataSize) throw (r_Error);
    
    /**
     * write extra metadata (specified by json parameters)
     */
    void writeMetadata(NcFile &dataFile) throw (r_Error);
    
    /**
     * add metadata attributes to var if not null, otherwise to dataFile.
     */
    void addJsonAttributes(NcFile &dataFile, const Json::Value& metadata, NcVar* var = NULL) throw (r_Error);
    
    /**
     * Convert type to a NcType; returns ncNoType in case of invalid type.
     */
    NcType stringToNcType(std::string type);
    
    /**
     * Add json array values to a netCDF variable.
     */
    void jsonArrayToNcVar(NcVar* var, Json::Value jsonArray);
    
    /**
     * Unsigned data has to be transformed to data of 2x more bytes
     * as NetCDF only supports exporting signed data;
     * so unsigned char is transformed to short for example, and we add the
     * valid_min/valid_max attributes to describe the range.
     */
    template <class S, class T>
    void writeData(NcFile &dataFile, std::string& varName, int dimNo, const NcDim** dims, long* dimSizes, 
                   size_t dataSize, NcType ncType, long validMin, long validMax,
                   const char* missingValue = NULL) throw (r_Error);
    
    /**
     * write struct variable data
     */
    template <class S, class T>
    void writeDataStruct(NcFile &dataFile, std::string& varName, int dimNo, const NcDim** dims, long* dimSizes, 
                         size_t dataSize, size_t structSize, size_t offset, NcType ncType, 
                         long validMin, long validMax, const char* missingValue = NULL) throw (r_Error);
    
    /**
     * @return dimension name given it's index
     */
    std::string getDimensionName(unsigned int dimId) throw (r_Error);
    
    /**
     * @return single variable name for exporting to netcdf
     */
    std::string getVariableName() throw (r_Error);
    
    // variable names
    std::vector<std::string> varNames;
    
    // dimension names
    std::vector<std::string> dimNames;
    
    // dimension variables
    std::vector<std::string> dimVarNames;
    
    Json::Value encodeOptions;
    
    

    static const char* DEFAULT_VAR;
    static const char* DEFAULT_DIM_NAME_PREFIX;
    static const char* VAR_SEPARATOR_STR;
    static const char* VARS_KEY;
    static const char* VALID_MIN;
    static const char* VALID_MAX;
    static const char* MISSING_VALUE;
    
    static const char* JSON_KEY_DIMS;
    static const char* JSON_KEY_VARS;
    static const char* JSON_KEY_GLOBAL;
    static const char* JSON_KEY_NAME;
    static const char* JSON_KEY_DATA;
    static const char* JSON_KEY_METADATA;
    static const char* JSON_KEY_TYPE;
};

#endif
