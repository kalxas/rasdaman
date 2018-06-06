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
    r_Conv_NETCDF(const char* src, const r_Minterval& interv, const r_Type* tp);
    /// constructor using convert_type_e shortcut
    r_Conv_NETCDF(const char* src, const r_Minterval& interv, int tp);
    /// destructor
    ~r_Conv_NETCDF(void);

    /// convert to NETCDF
    virtual r_Conv_Desc& convertTo(const char* options = NULL,
                                   const r_Range* nullValue = NULL);
    /// convert from NETCDF
    virtual r_Conv_Desc& convertFrom(const char* options = NULL);
    /// convert data in a specific format to array
    virtual r_Conv_Desc& convertFrom(r_Format_Params options);

    /// cloning
    virtual r_Convertor* clone(void) const;

    /// identification
    virtual const char* get_name(void) const;
    virtual r_Data_Format get_data_format(void) const;

private:

#ifdef HAVE_NETCDF
    struct RasType
    {
        unsigned int cellSize;
        std::string cellType;
    };

    /**
     * Read data from tmpFile into desc.dest and return the file size.
     */
    void parseDecodeOptions(const std::string& options);


    void validateDecodeOptions(const NcFile& dataFile);

    void parseEncodeOptions(const std::string& options);

    void validateJsonEncodeOptions();

    /**
     * read single variable data
     */
    void readDimSizes(const NcFile& dataFile);

    /**
     * read single variable data
     */
    void readSingleVar(const NcFile& dataFile);

    /**
     * read multiple variable data into a struct
     */
    void readMultipleVars(const NcFile& dataFile);

    /**
     * read single variable data
     */
    template <class T>
    void readData(NcVar* var, convert_type_e);

    /**
     * read struct variable data
     *
     * @param bandOffset offset bytes at the current variable.
     */
    template <class T>
    void readDataStruct(NcVar* var, size_t structSize, size_t& bandOffset);

    /**
     * Build struct type
     */
    size_t buildStructType(const NcFile& dataFile);

    /**
     * Get a rasdaman type from a netcdf variable type.
     */
    RasType getRasType(NcVar* var);

    /**
     * write single variable data
     */
    void writeSingleVar(NcFile& dataFile, const NcDim** dims);

    /**
     * write multiple variables from a struct
     */
    void writeMultipleVars(NcFile& dataFile, const NcDim** dims);

    /**
     * write extra metadata (specified by json parameters)
     */
    void writeMetadata(NcFile& dataFile);

    /**
     * add metadata attributes to var if not null, otherwise to dataFile.
     */
    void addJsonAttributes(NcFile& dataFile, const Json::Value& metadata, NcVar* var = NULL);

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
    void writeData(NcFile& dataFile, std::string& varName, const NcDim** dims, NcType ncType,
                   long validMin, long validMax, const char* missingValue = NULL);

    /**
     * write struct variable data
     *
     * @param bandOffset offset bytes in the rasdaman struct at the current variable.
     */
    template <class S, class T>
    void writeDataStruct(NcFile& dataFile, std::string& varName, const NcDim** dims, size_t structSize, size_t bandOffset, NcType ncType,
                         long validMin, long validMax, const char* missingValue = NULL, size_t dimNum = 0);

    /**
     * @return dimension name given it's index
     */
    std::string getDimensionName(unsigned int dimId);

    /**
     * @return single variable name for exporting to netcdf
     */
    std::string getVariableName();

    // variable names
    std::vector<std::string> varNames;

    // dimension names
    std::vector<std::string> dimNames;

    // dimension variables
    std::vector<std::string> dimVarNames;

    Json::Value encodeOptions;

    size_t numDims;
    size_t dataSize;
    std::vector<long> dimSizes;
    std::vector<long> dimOffsets;
#endif

    static const std::string DEFAULT_VAR;
    static const std::string DEFAULT_DIM_NAME_PREFIX;
    static const std::string VAR_SEPARATOR_STR;
    static const std::string VARS_KEY;
    static const std::string VALID_MIN;
    static const std::string VALID_MAX;
    static const std::string MISSING_VALUE;
};

#endif
