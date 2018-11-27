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
#include <netcdf.h>
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
        RasType(unsigned int cellSizeArg, std::string cellTypeArg, convert_type_e ct)
        : cellType(std::move(cellTypeArg)), cellSize(cellSizeArg), convertType{ct} {}

        std::string cellType;
        unsigned int cellSize;
        convert_type_e convertType;
    };

    /**
     * Read data from tmpFile into desc.dest and return the file size.
     */
    void parseDecodeOptions(const std::string& options);


    void validateDecodeOptions();

    void parseEncodeOptions(const std::string& options);

    void validateJsonEncodeOptions();

    /**
     * read single variable data
     */
    void readDimSizes();

    /**
     * read variable data into a struct
     */
    void readVars();

    /**
     * read variable data
     *
     * @param bandOffset offset bytes at the current variable.
     */
    template <class T>
    void readVarData(int var, size_t cellSize, size_t& bandOffset, bool isStruct);

    /**
     * Build struct type
     */
    size_t buildCellType();

    /**
     * Get a rasdaman type from a netcdf variable type.
     */
    RasType getRasType(int var);

    /**
     * write single variable data
     */
    void writeSingleVar(const std::vector<int> &dims);

    /**
     * write multiple variables from a struct
     */
    void writeMultipleVars(const std::vector<int> &dims);

    /**
     * write extra metadata (specified by json parameters)
     */
    void addMetadata();

    /**
     * add metadata attributes to var if not null, otherwise to dataFile.
     */
    void addJsonAttributes(const Json::Value& metadata, int var = NC_GLOBAL);

    template <class T>
    void addVarAttributes(int var, nc_type nctype, T validMin, T validMax,
                          size_t dimNum);

    // return true if attribute att exists, false otherwise
    bool attExists(int var, const char *att) const;

    /**
     * Convert type to a nc_type; returns ncNoType in case of invalid type.
     */
    nc_type stringToNcType(std::string type);

    /**
     * Add json array values to a netCDF variable.
     */
    void jsonArrayToNcVar(int var, int dimid, Json::Value jsonArray);

    template <class T>
    void writeData(const std::string& varName, const std::vector<int> &dims,
                   const char *src, nc_type nctype,
                   T validMin, T validMax, size_t dimNum = 0);

    /**
     * write struct variable data
     *
     * @param bandOffset offset bytes in the rasdaman struct at the current variable.
     */
    template <class T>
    void writeDataStruct(const std::string& varName,
                         const std::vector<int> &dims,
                         size_t structSize, size_t bandOffset, nc_type nctype,
                         T validMin, T validMax, size_t dimNum = 0);

    /**
     * @return dimension name given it's index
     */
    std::string getDimName(unsigned int dimId);

    /**
     * @return single variable name for exporting to netcdf
     */
    const std::string &getVariableName();

    // close the netCDF dataFile
    void closeDataFile();

    Json::Value encodeOptions;

    // variable names
    std::vector<std::string> varNames;

    // dimension names
    std::vector<std::string> dimNames;

    // dimension variables
    std::vector<std::string> dimVarNames;

    // length of each dimension
    std::vector<size_t> dimSizes;

    // offset at each dimension (subset read/write)
    std::vector<size_t> dimOffsets;

    // number of dimensions
    size_t numDims;

    // cell count
    size_t dataSize;

#endif

    int dataFile{invalidDataFile};

    static const int invalidDataFile;
    static const std::string DEFAULT_VAR;
    static const std::string DEFAULT_DIM_NAME_PREFIX;
    static const std::string VAR_SEPARATOR_STR;
    static const std::string VARS_KEY;
    static const std::string VALID_MIN;
    static const std::string VALID_MAX;
    static const std::string MISSING_VALUE;
    static const std::string FILL_VALUE;
};

#endif
