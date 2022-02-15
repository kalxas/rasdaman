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
 * SOURCE: netcdf.cc
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_NETCDF
 *
 * COMMENTS:
 *
 * Provides functions to convert data to NETCDF and back.
 */

#include "config.h"

#include "conversion/netcdf.hh"
#include "conversion/tmpfile.hh"
#include "raslib/error.hh"
#include "raslib/parseparams.hh"
#include "raslib/primitivetype.hh"
#include "raslib/attribute.hh"
#include "raslib/structuretype.hh"
#include "raslib/odmgtypes.hh"
#include "mymalloc/mymalloc.h"
#include "formatparamkeys.hh"

#include "conversion/transpose.hh"

#include <logging.hh>

#include <sstream>
#include <iostream>
#include <algorithm>
#include <string>
#include <vector>
#include <errno.h>
#include <string.h>
#include <climits>
#include <cfloat>
#include <boost/algorithm/string.hpp>
#include <boost/optional/optional.hpp>

using namespace std;


#ifdef HAVE_NETCDF

const int r_Conv_NETCDF::invalidDataFile{-1};
const string r_Conv_NETCDF::DEFAULT_VAR{"data"};
const string r_Conv_NETCDF::DEFAULT_DIM_NAME_PREFIX{"dim_"};
const string r_Conv_NETCDF::VAR_SEPARATOR_STR{";"};
const string r_Conv_NETCDF::VARS_KEY{"vars"};
const string r_Conv_NETCDF::VALID_MIN{"valid_min"};
const string r_Conv_NETCDF::VALID_MAX{"valid_max"};
const string r_Conv_NETCDF::MISSING_VALUE{"missing_value"};
const string r_Conv_NETCDF::FILL_VALUE{"_FillValue"};

int status{NC_NOERR};

#define throwOnError(errcode, msg) \
    if ((errcode) != NC_NOERR) { \
        std::stringstream s; s << msg << ", " << nc_strerror(errcode); \
        throw r_Error(r_Error::r_Error_Conversion, s.str()); \
    }

#define warnOnError(errcode, msg) \
    if ((errcode) != NC_NOERR) \
        LWARNING << msg << ", reason: " << nc_strerror(errcode);

#endif

/// constructor using an r_Type object.
r_Conv_NETCDF::r_Conv_NETCDF(const char* src, const r_Minterval& interv, const r_Type* tp)
    : r_Convert_Memory(src, interv, tp, true)
{
}

/// constructor using convert_type_e shortcut
r_Conv_NETCDF::r_Conv_NETCDF(const char* src, const r_Minterval& interv, int tp)
    : r_Convert_Memory(src, interv, tp)
{
}

/// destructor
r_Conv_NETCDF::~r_Conv_NETCDF(void)
{
}

r_Convertor* r_Conv_NETCDF::clone(void) const
{
    return new r_Conv_NETCDF(desc.src, desc.srcInterv, desc.baseType);
}

const char* r_Conv_NETCDF::get_name(void) const
{
    return format_name_netcdf;
}

r_Data_Format r_Conv_NETCDF::get_data_format(void) const
{
    return r_NETCDF;
}

/// convert to NETCDF
r_Conv_Desc& r_Conv_NETCDF::convertTo(const char* options, const r_Range* nullValue)
{
#ifdef HAVE_NETCDF
    LDEBUG << "converting array with sdom " << desc.srcInterv << " to netCDF...";

    // handle format params
    if (options)
        parseEncodeOptions(string{options});
    updateNodataValue(nullValue);

    // transpose if necessary
    if (formatParams.isTranspose())
    {
        LDEBUG << "transposing array before encoding to netCDF...";
        transpose(const_cast<char*>(desc.src), desc.srcInterv, desc.srcType,
                  formatParams.getTranspose());
    }

    // create temp netcdf file
    r_TmpFile tmpFileObj;
    string tmpFilePath = tmpFileObj.getFileName();

    status = nc_create(tmpFilePath.c_str(), NC_CLOBBER | NC_NETCDF4, &dataFile);
    throwOnError(status, "failed creating temporary netCDF file" << tmpFilePath);

    // create netCDF dimensions
    numDims = desc.srcInterv.dimension();
    dimSizes.resize(numDims);
    dataSize = 1;

    std::vector<int> dimids(numDims);
    for (size_t i = 0; i < numDims; i++)
    {
        dimSizes[i] = static_cast<size_t>(desc.srcInterv[i].get_extent());
        dataSize *= dimSizes[i];
        auto dimName = getDimName(i);
        status = nc_def_dim(dataFile, dimName.c_str(), dimSizes[i], &dimids[i]);
        throwOnError(status, "failed creating dimension " << i << " with name " << dimName
                     << " and length " << dimSizes[i] << " in netCDF file");
    }

    // write rasdaman data to netcdf variables in the dataFile
    if (desc.baseType == ctype_struct || desc.baseType == ctype_rgb)
        writeMultipleVars(dimids);
    else
        writeSingleVar(dimids);

    addMetadata();

    closeDataFile();

    // read data from temp file and return result
    long fileSize = 0;
    desc.dest = tmpFileObj.readData(fileSize);
    desc.destInterv = r_Minterval(1) << r_Sinterval(0ll, static_cast<r_Range>(fileSize - 1));
    desc.destType = r_Type::get_any_type("char");
    return desc;

#else
    LERROR << "encoding netCDF is not supported; rasdaman should be configured with option -DUSE_NETCDF=ON to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
#endif
}

/// convert from NETCDF
r_Conv_Desc& r_Conv_NETCDF::convertFrom(const char* options)
{
#ifdef HAVE_NETCDF
    if (options)
        parseDecodeOptions(string{options});
    return this->convertFrom(formatParams);
#else
    LERROR << "decoding netCDF is not supported; rasdaman should be configured with option -DUSE_NETCDF=ON to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
#endif
}

r_Conv_Desc& r_Conv_NETCDF::convertFrom(r_Format_Params options)
{
#ifdef HAVE_NETCDF
    formatParams = options;
    if (varNames.empty())
    {
        varNames = formatParams.getVariables();
    }

    // write the data to temp file, netcdf wants a file path unfortunately
    string tmpFilePath;
    r_TmpFile tmpFileObj;
    if (formatParams.getFilePaths().empty())
    {
        tmpFileObj.writeData(desc.src, (size_t) desc.srcInterv.cell_count());
        tmpFilePath = tmpFileObj.getFileName();
    }
    else
    {
        tmpFilePath = formatParams.getFilePath();
    }

    status = nc_open(tmpFilePath.c_str(), NC_NOWRITE, &dataFile);
    throwOnError(status, "failed opening netCDF file for reading " << tmpFilePath);

    validateDecodeOptions();
    readDimSizes();
    readVars();
    closeDataFile();

    // if selected, transposes rasdaman data after converting from netcdf
    if (formatParams.isTranspose())
    {
        LDEBUG << "transposing decoded data of sdom: " << desc.destInterv;
        transpose(desc.dest, desc.destInterv, (const r_Type*) desc.destType,
                  formatParams.getTranspose());
    }

    return desc;
#else
    LERROR << "decoding netCDF is not supported; rasdaman should be configured with option -DUSE_NETCDF=ON to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
#endif
}

#ifdef HAVE_NETCDF

void r_Conv_NETCDF::readDimSizes()
{
    int var0id{};
    status = nc_inq_varid(dataFile, varNames[0].c_str(), &var0id);
    throwOnError(status, "failed reading variable " << varNames[0] << " from netCDF file");

    // get dimension ids and number of dimensions
    int dimids[NC_MAX_VAR_DIMS];
    int ndims{};
    status = nc_inq_var(dataFile, var0id, NULL, NULL, &ndims, dimids, NULL);
    throwOnError(status, "failed reading variable information from netCDF file");
    numDims = static_cast<size_t>(ndims);

    const auto& subsetDomain = formatParams.getSubsetDomain();
    if (subsetDomain.dimension() != 0 && subsetDomain.dimension() != static_cast<r_Dimension>(ndims))
    {
        std::stringstream s;
        s << "invalid 'subsetDomain' parameter '" << subsetDomain << "' of dimension " <<
             subsetDomain.dimension() << " given, but netCDF variable is of dimension " << numDims;
        throw r_Error(r_Error::r_Error_Conversion, s.str());
    }

    dimSizes.resize(numDims);
    dimOffsets.resize(numDims);
    dataSize = 1;
    desc.destInterv = r_Minterval(static_cast<r_Dimension>(numDims));
    for (size_t i = 0; i < numDims; i++)
    {
        if (subsetDomain.dimension() != 0)
        {
            dimSizes[i] = static_cast<size_t>(subsetDomain[i].get_extent());
            dimOffsets[i] = static_cast<size_t>(subsetDomain[i].low());
        }
        else
        {
            status = nc_inq_dimlen(dataFile, dimids[i], &dimSizes[i]);
            throwOnError(status, "failed reading dimension length from netCDF file");
            dimOffsets[i] = 0;
        }
        dataSize *= dimSizes[i];
        desc.destInterv << r_Sinterval(static_cast<r_Range>(dimOffsets[i]),
                                       static_cast<r_Range>(dimOffsets[i] + dimSizes[i] - 1));
    }
}

void r_Conv_NETCDF::parseDecodeOptions(const string& options)
{
    if (formatParams.parse(options))
    {
        varNames = formatParams.getVariables();
    }
    else
    {
        if (params == NULL)
        {
            params = new r_Parse_Params();
        }
        char* varNamesParam = NULL;
        params->add(VARS_KEY, &varNamesParam, r_Parse_Params::param_type_string);

        int paramNo = params->process(options.c_str());
        if (paramNo == 0)
        {
            throw r_Error(r_Error::r_Error_Conversion,
                          "no variable names given, at least one variable name must be "
                          "specified in the format options as 'vars=var1;var2;..'");
        }
        if (paramNo == -1)
        {
            throw r_Error(r_Error::r_Error_Conversion,
                          "failed processing format options '" + std::string(options) + 
                          "'; make sure options are of the format 'vars=var1;var2;..'");
        }

        string varNamesStr(varNamesParam);
        delete [] varNamesParam;
        boost::split(varNames, varNamesStr, boost::is_any_of(VAR_SEPARATOR_STR));
    }
}

void r_Conv_NETCDF::validateDecodeOptions()
{
    if (varNames.empty())
    {
        throw r_Error(r_Error::r_Error_Conversion,
                      "no netCDF variables variables specified to decode");
    }
    for (const auto& varName : varNames)
    {
        int tmpvarid{};
        status = nc_inq_varid(dataFile, varName.c_str(), &tmpvarid);
        throwOnError(status, "variable " << varName << " not found in netCDF file.");
    }
}

void r_Conv_NETCDF::parseEncodeOptions(const string& options)
{
    if (!formatParams.parse(options))
    {
        // try to parse the old-style key-value format in any case
        parseDecodeOptions(options);
    }
    else
    {
        encodeOptions = formatParams.getParams();
        validateJsonEncodeOptions();
    }
}

void r_Conv_NETCDF::validateJsonEncodeOptions()
{
    if (!encodeOptions.isMember(FormatParamKeys::Encode::NetCDF::DIMENSIONS))
    {
        throw r_Error(r_Error::r_Error_Conversion,
                      "mandatory format options field missing " + 
                      std::string{FormatParamKeys::Encode::NetCDF::DIMENSIONS});
    }
    if (!encodeOptions.isMember(FormatParamKeys::General::VARIABLES))
    {
        throw r_Error(r_Error::r_Error_Conversion,
                      "mandatory format options field missing " + 
                      std::string{FormatParamKeys::General::VARIABLES});
    }
    
    Json::Value dims = encodeOptions[FormatParamKeys::Encode::NetCDF::DIMENSIONS];
    auto dim = dims.size();
    dimNames.resize(dim);
    for (decltype(dim) i = 0; i < dim; i++)
    {
        //create the vector of dimension metadata names and swap the last two in case transposition is selected as an option
        if (formatParams.isTranspose() && i == dim - 2)
            dimNames[i] = dims[dim - 1].asString();
        else if (formatParams.isTranspose() && i == dim - 1)
            dimNames[i] = dims[dim - 2].asString();
        else
            dimNames[i] = dims[i].asString();
    }
    
    Json::Value vars = encodeOptions[FormatParamKeys::General::VARIABLES];
    for (const auto& varName : vars.getMemberNames())
    {
        bool isDimName = find(dimNames.begin(), dimNames.end(), varName) != dimNames.end();
        bool hasDataAttribute = vars[varName].isMember(FormatParamKeys::Encode::NetCDF::DATA);
        if (hasDataAttribute)
        {
            if (isDimName)
                dimVarNames.push_back(varName);
            else
                nondataVarNames.push_back(varName);
        }
        else
        {
            varNames.push_back(varName);
        }
    }
}

void r_Conv_NETCDF::readVars()
{
    const auto isStruct = varNames.size() > 1;

    size_t cellSize = buildCellType();

    desc.dest = static_cast<char*>(mymalloc(dataSize * cellSize));

    // offset of the attribute within the struct type; it is updated by the readVarData
    size_t offset{};
    for (size_t i = 0; i < varNames.size(); i++)
    {
        const auto& varName = varNames[i];

        int varid{};
        status = nc_inq_varid(dataFile, varName.c_str(), &varid);
        throwOnError(status, "failed reading variable " << varName << " from netCDF file");

        nc_type vartype{};
        status = nc_inq_vartype(dataFile, varid, &vartype);
        throwOnError(status, "failed reading type of variable " << varName << " from netCDF file");

        switch (vartype)
        {
        case NC_BYTE:
            readVarData<signed char>(varid, cellSize, offset, isStruct);
            break;
        case NC_CHAR:
        case NC_UBYTE:
            readVarData<unsigned char>(varid, cellSize, offset, isStruct);
            break;
        case NC_SHORT:
            readVarData<short>(varid, cellSize, offset, isStruct);
            break;
        case NC_USHORT:
            readVarData<unsigned short>(varid, cellSize, offset, isStruct);
            break;
        case NC_INT:
            readVarData<int>(varid, cellSize, offset, isStruct);
            break;
        case NC_UINT:
            readVarData<unsigned int>(varid, cellSize, offset, isStruct);
            break;
        case NC_FLOAT:
            readVarData<float>(varid, cellSize, offset, isStruct);
            break;
        case NC_DOUBLE:
            readVarData<double>(varid, cellSize, offset, isStruct);
            break;
        default:
        {
            std::stringstream s;
            s << "variable " << varName << " has an unsupported netCDF variable type " << vartype;
            throw r_Error(r_Error::r_Error_Conversion, s.str());
        }
        }
    }
}

size_t r_Conv_NETCDF::buildCellType()
{
    auto isStruct = varNames.size() > 1;

    size_t cellSize = 0; // size of the struct type, used for offset computations in memcpy
    size_t alignSize = 1;  // alignment of size (taken from StructType::calcSize())

    stringstream destType; // build the type string
    if (isStruct)
    {
        destType << "struct { ";
    }

    int var0id{};
    status = nc_inq_varid(dataFile, varNames[0].c_str(), &var0id);
    throwOnError(status, "failed reading variable " << varNames[0] << " from netCDF file");

    int var0dims[NC_MAX_VAR_DIMS];
    status = nc_inq_vardimid(dataFile, var0id, var0dims);
    throwOnError(status, "failed reading dimensions of variable " << varNames[0] << " from netCDF file");

    for (size_t i = 0; i < varNames.size(); i++)
    {
        int varid{};
        status = nc_inq_varid(dataFile, varNames[i].c_str(), &varid);
        throwOnError(status, "failed reading variable " << varNames[i] << " from netCDF file");

        int varndims{};
        status = nc_inq_varndims(dataFile, varid, &varndims);
        throwOnError(status, "failed reading dimensionality of variable " << varNames[i] << " from netCDF file");

        if (static_cast<int>(numDims) != varndims)
        {
            std::stringstream s;
            s << "variable " << varNames[i] << " has different dimension from the first variable "
                   << varNames[0] << ".";
            throw r_Error(r_Error::r_Error_Conversion, s.str());
        }
        if (i > 0)
        {
            destType << ", ";
        }

        auto rasType = getRasType(varid);

        destType << rasType.cellType;
        desc.baseType = rasType.convertType;

        cellSize += rasType.cellSize;
        if (rasType.cellSize > alignSize)
        {
            alignSize = rasType.cellSize;
        }

        // check that dimension extents of all variables are matching
        if (i > 0)
        {
            int vardims[NC_MAX_VAR_DIMS];
            status = nc_inq_vardimid(dataFile, varid, vardims);
            throwOnError(status, "failed reading dimensions of variable " << varNames[i] << " from netCDF file");

            // for each dimension
            for (size_t j = 0; j < numDims; j++)
            {
                size_t var0dimlen{};
                status = nc_inq_dimlen(dataFile, var0dims[j], &var0dimlen);
                throwOnError(status, "failed reading dimension length of variable " << varNames[0] << " from netCDF file");

                size_t vardimlen{};
                status = nc_inq_dimlen(dataFile, vardims[j], &vardimlen);
                throwOnError(status, "failed reading dimension length of variable " << varNames[i] << " from netCDF file");

                if (vardimlen != var0dimlen)
                {
                    std::stringstream s;
                    s << "variable " << varNames[i] << " has different dimension lengths from the first variable "
                           << varNames[0] << ": dimension " << j << " expected length " << var0dimlen
                           << ", got " << vardimlen << ".";
                    throw r_Error(r_Error::r_Error_Conversion, s.str());
                }
            }
        }
    }

    // align struct size to the member type of biggest size
    if (cellSize % alignSize != 0)
    {
        cellSize = (cellSize / alignSize + 1) * alignSize;
    }

    if (isStruct)
    {
        destType << " }";
    }

    desc.destType = r_Type::get_any_type(destType.str().c_str());
    if (isStruct)
    {
        desc.baseType = ctype_struct;
    }

    return cellSize;
}

r_Conv_NETCDF::RasType r_Conv_NETCDF::getRasType(int var)
{
    nc_type vartype{};
    status = nc_inq_vartype(dataFile, var, &vartype);
    throwOnError(status, "failed reading variable type from netCDF file");

    switch (vartype)
    {
    case NC_BYTE:
        return RasType{sizeof(r_Octet), "octet", ctype_int8};
    case NC_CHAR:
    case NC_UBYTE:
        return RasType{sizeof(r_Char), "char", ctype_uint8};
    case NC_SHORT:
        return RasType{sizeof(r_Short), "short", ctype_int16};
    case NC_USHORT:
        return RasType{sizeof(r_UShort), "ushort", ctype_uint16};
    case NC_INT:
        return RasType{sizeof(r_Long), "long", ctype_int32};
    case NC_UINT:
        return RasType{sizeof(r_ULong), "ulong", ctype_uint32};
    case NC_FLOAT:
        return RasType{sizeof(r_Float), "float", ctype_float32};
    case NC_DOUBLE:
        return RasType{sizeof(r_Double), "double", ctype_float64};
    default:
    {
        std::stringstream s;
        s << "unsupported netCDF variable type " << vartype;
        throw r_Error(r_Error::r_Error_Conversion, s.str());
    }
    }
}

template<class T>
void r_Conv_NETCDF::readVarData(int var, size_t cellSize, size_t& bandOffset, bool isStruct)
{
    // needs to be freed later if isStruct == true
    std::unique_ptr<T[]> src;
    if (isStruct)
    {
        src.reset(new T[dataSize]);
        status = nc_get_vara(dataFile, var, dimOffsets.data(), dimSizes.data(), (void*) src.get());
    }
    else
    {
        status = nc_get_vara(dataFile, var, dimOffsets.data(), dimSizes.data(), (void*) desc.dest);
    }
    throwOnError(status, "failed reading variable data from netCDF file");

    if (isStruct)
    {
        char* dst = desc.dest + bandOffset;
        for (size_t j = 0; j < dataSize; ++j, dst += cellSize)
        {
            *(reinterpret_cast<T*>(dst)) = src[j];
        }

        bandOffset += sizeof(T);
    }
}

/// write single variable data
void r_Conv_NETCDF::writeSingleVar(const std::vector<int>& dims)
{
    string varName = getVariableName();
    LDEBUG << "writing data for variable " << varName;
    switch (desc.baseType)
    {
    case ctype_int8:
        writeData<r_Octet>(varName, dims, desc.src, NC_BYTE, NC_MIN_BYTE, NC_MAX_BYTE);
        break;
    case ctype_char:
    case ctype_uint8:
        writeData<r_Char>(varName, dims, desc.src, NC_UBYTE, 0, NC_MAX_UBYTE);
        break;
    case ctype_int16:
        writeData<r_Short>(varName, dims, desc.src, NC_SHORT, NC_MIN_SHORT, NC_MAX_SHORT);
        break;
    case ctype_uint16:
        writeData<r_UShort>(varName, dims, desc.src, NC_USHORT, 0, NC_MAX_USHORT);
        break;
    case ctype_int32:
        writeData<r_Long>(varName, dims, desc.src, NC_INT, NC_MIN_INT, NC_MAX_INT);
        break;
    case ctype_uint32:
        writeData<r_ULong>(varName, dims, desc.src, NC_UINT, 0, NC_MAX_UINT);
        break;
    case ctype_float32:
        writeData<r_Float>(varName, dims, desc.src, NC_FLOAT, NC_MIN_FLOAT, NC_MAX_FLOAT);
        break;
    case ctype_int64:
    case ctype_uint64:
    case ctype_float64:
        writeData<r_Double>(varName, dims, desc.src, NC_DOUBLE, NC_MIN_DOUBLE, NC_MAX_DOUBLE);
        break;
    default:
    {
        std::stringstream s;
        s << "failed writing data for variable " << varName << " to netCDF file, "
               << "unsupported rasdaman base type: " << desc.baseType;
        throw r_Error(r_Error::r_Error_Conversion, s.str());
    }
    }
}

/// write multiple variables
void r_Conv_NETCDF::writeMultipleVars(const std::vector<int>& dims)
{
    LDEBUG << "writing data for " << varNames.size() << " variables";
    if (!desc.srcType->isStructType())
    {
        throw r_Error(r_Error::r_Error_Conversion,
                      "MDD object type is not a struct type");
    }
    const r_Structure_Type* st = dynamic_cast<const r_Structure_Type*>(desc.srcType);
    if (varNames.size() != st->count_elements())
    {
        throw r_Error(r_Error::r_Error_Conversion,
                      "mismatch in variable count between query and MDD object type");
    }

    // size of the struct type, used for offset computation in memcpy
    const auto structSize = static_cast<size_t>(st->size());

    // offset of the attribute within the struct type
    size_t offset = 0;
    auto it = st->getAttributes().begin();
    for (size_t i = 0; i < varNames.size(); i++, it++)
    {
        const string& varName = varNames[i];
        LDEBUG << "writing data for variable " << varName;
        switch ((*it).type_of().type_id())
        {
        case r_Type::OCTET:
            writeDataStruct<r_Octet>(varName, dims, structSize, offset, NC_BYTE, NC_MIN_BYTE, NC_MAX_BYTE, i);
            break;
        case r_Type::CHAR:
            writeDataStruct<r_Char>(varName, dims, structSize, offset, NC_UBYTE, 0, NC_MAX_UBYTE, i);
            break;
        case r_Type::SHORT:
            writeDataStruct<r_Short>(varName, dims, structSize, offset, NC_SHORT, NC_MIN_SHORT, NC_MAX_SHORT, i);
            break;
        case r_Type::USHORT:
            writeDataStruct<r_UShort>(varName, dims, structSize, offset, NC_USHORT, 0, NC_MAX_USHORT, i);
            break;
        case r_Type::LONG:
            writeDataStruct<r_Long>(varName, dims, structSize, offset, NC_INT, NC_MIN_INT, NC_MAX_INT, i);
            break;
        case r_Type::ULONG:
            writeDataStruct<r_ULong>(varName, dims, structSize, offset, NC_UINT, 0, NC_MAX_UINT, i);
            break;
        case r_Type::FLOAT:
            writeDataStruct<r_Float>(varName, dims, structSize, offset, NC_FLOAT, NC_MIN_FLOAT, NC_MAX_FLOAT, i);
            break;
        case r_Type::DOUBLE:
            writeDataStruct<r_Double>(varName, dims, structSize, offset, NC_DOUBLE, NC_MIN_DOUBLE, NC_MAX_DOUBLE, i);
            break;
        default:
        {
            std::stringstream s;
            s << "unsupported rasdaman band type: " << (*it).type_of().name();
            throw r_Error(r_Error::r_Error_Conversion, s.str());
        }
        }
        offset += static_cast<size_t>((*it).type_of().size());
    }
}

void r_Conv_NETCDF::addMetadata()
{
    if (encodeOptions.isNull())
    {
        return;
    }

    // add global
    if (encodeOptions.isMember(FormatParamKeys::Encode::METADATA))
    {
        const Json::Value& globalMetadata = encodeOptions[FormatParamKeys::Encode::METADATA];
        addJsonAttributes(globalMetadata);
    }

    if (encodeOptions.isMember(FormatParamKeys::General::VARIABLES))
    {
        const Json::Value& jvars = encodeOptions[FormatParamKeys::General::VARIABLES];

        // add missing dimension variables
        for (const auto& dimVarName : dimVarNames)
        {
            int dimid{};
            status = nc_inq_dimid(dataFile, dimVarName.c_str(), &dimid);
            throwOnError(status, "failed reading dimension " << dimVarName << " from netCDF file");

            Json::Value jsonVar = jvars[dimVarName];
            if (!jsonVar.isMember(FormatParamKeys::Encode::NetCDF::TYPE))
            {
                LWARNING << "variable " << dimVarName << " has no type, it will not be added to the exported netCDF file.";
                continue;
            }
            if (!jsonVar.isMember(FormatParamKeys::Encode::NetCDF::DATA))
            {
                LWARNING << "variable " << dimVarName
                         << " has no data array, it will not be added to the exported netCDF file.";
                continue;
            }

            nc_type nctype = stringToNcType(jsonVar[FormatParamKeys::Encode::NetCDF::TYPE].asCString());
            if (nctype == NC_NAT)
            {
                LWARNING << "unknown netCDF variable type '" << jsonVar[FormatParamKeys::Encode::NetCDF::TYPE] <<
                         "' in variable " << dimVarName
                         << ", expected one of: byte/char, short/ushort, int/uint, float, double.";
                continue;
            }

            int newVar{};
            status = nc_def_var(dataFile, dimVarName.c_str(), nctype, 1, &dimid, &newVar);
            throwOnError(status, "failed creating dimension variable " << dimVarName << " in netCDF file");

            Json::Value jsonDataArray = jsonVar[FormatParamKeys::Encode::NetCDF::DATA];
            if (jsonDataArray.isArray())
            {
                jsonArrayToNcVar(newVar, dimid, jsonDataArray);
            }
            else
            {
                LWARNING << "invalid value of field '" << FormatParamKeys::Encode::NetCDF::DATA <<
                         "' of variable " << dimVarName << ", expected an array.";
            }
        }
        
        // add non-data variables
        for (const auto& varName : nondataVarNames)
        {
            Json::Value jsonVar = jvars[varName];
            if (!jsonVar.isMember(FormatParamKeys::Encode::NetCDF::TYPE))
            {
                LWARNING << "variable " << varName << " has no type, it will not be added to the exported netCDF file.";
                continue;
            }

            nc_type nctype = stringToNcType(jsonVar[FormatParamKeys::Encode::NetCDF::TYPE].asCString());
            if (nctype == NC_NAT)
            {
                LWARNING << "unknown netCDF variable type '" << jsonVar[FormatParamKeys::Encode::NetCDF::TYPE] <<
                         "' in variable " << varName
                         << ", expected one of: byte/char, short/ushort, int/uint, float, double.";
                continue;
            }

            int newVar{};
            status = nc_def_var(dataFile, varName.c_str(), nctype, 0, NULL, &newVar);
            throwOnError(status, "failed creating non-data variable " << varName << " in netCDF file");
            
            if (jsonVar.isMember(FormatParamKeys::Encode::METADATA))
            {
                Json::Value jsonVarMetadata = jsonVar[FormatParamKeys::Encode::METADATA];
                addJsonAttributes(jsonVarMetadata, newVar);
            }
        }

        int nvars{};
        int vars[NC_MAX_VARS];
        status = nc_inq_varids(dataFile, &nvars, vars);
        throwOnError(status, "failed reading variables from netCDF file");

        for (int i = 0; i < nvars; i++)
        {
            char varName[MAX_NC_NAME];
            status = nc_inq_varname(dataFile, vars[i], varName);
            throwOnError(status, "failed reading variable name from netCDF file");

            if (jvars.isMember(varName))
            {
                Json::Value jsonVar = jvars[varName];
                if (jsonVar.isMember(FormatParamKeys::Encode::METADATA))
                {
                    Json::Value jsonVarMetadata = jsonVar[FormatParamKeys::Encode::METADATA];
                    addJsonAttributes(jsonVarMetadata, vars[i]);
                }
            }
        }
    }
}

void r_Conv_NETCDF::addJsonAttributes(const Json::Value& metadata, int var)
{
    vector<string> m = metadata.getMemberNames();
    for (const string& keyStr : m)
    {
        status = NC_NOERR;

        const char* key = keyStr.c_str();
        const Json::Value& value = metadata[keyStr];
        LDEBUG << "variable " << var << ", adding json attribute " << key << ": " << value;
        if (attExists(var, key))
        {
            LDEBUG << "attribute exists already, will be skipped";
            continue;
        }

        if (value.isInt())
        {
            const auto val = static_cast<int>(value.asInt());
            status = nc_put_att_int(dataFile, var, key, NC_INT, 1, &val);
        }
        else if (value.isUInt())
        {
            const auto val = value.asUInt();
            status = nc_put_att_uint(dataFile, var, key, NC_UINT, 1, &val);
        }
        if (value.isInt64())
        {
            const auto val = static_cast<long long>(value.asInt64());
            status = nc_put_att_longlong(dataFile, var, key, NC_INT64, 1, &val);
        }
        else if (value.isUInt64())
        {
            const auto val = value.asUInt64();
            status = nc_put_att_ulonglong(dataFile, var, key, NC_UINT64, 1, &val);
        }
        else if (value.isBool())
        {
            const auto val = static_cast<int>(value.asBool());
            status = nc_put_att_int(dataFile, var, key, NC_INT, 1, &val);
        }
        else if (value.isDouble() || value.isConvertibleTo(Json::ValueType::realValue))
        {
            const double val = value.asDouble();
            status = nc_put_att_double(dataFile, var, key, NC_DOUBLE, 1, &val);
        }
        else if (value.isString() || value.isConvertibleTo(Json::ValueType::stringValue))
        {
            const string valStr = value.asString();
            const char* val = valStr.c_str();
            status = nc_put_att_text(dataFile, var, key, strlen(val), val);
        }
        else
        {
            LWARNING << "unsupported type for attribute " << key << " / value " << value;
        }

        if (status != NC_NOERR)
        {
            warnOnError(status, "failed adding an attribute in netCDF file");
            nc_del_att(dataFile, var, key);
        }
    }
}

bool r_Conv_NETCDF::attExists(int var, const char* att) const
{
    int attid;
    return nc_inq_attid(dataFile, var, att, &attid) == NC_NOERR;
}

nc_type r_Conv_NETCDF::stringToNcType(string type)
{
    if (type == string("byte"))
        return NC_BYTE;
    else if (type == string("char"))
        return NC_UBYTE;
    else if (type == string("short"))
        return NC_SHORT;
    else if (type == string("ushort"))
        return NC_USHORT;
    else if (type == string("int"))
        return NC_INT;
    else if (type == string("uint"))
        return NC_UINT;
    else if (type == string("float"))
        return NC_FLOAT;
    else if (type == string("double"))
        return NC_DOUBLE;
    else
        return NC_NAT;
}

#define PUT_VAR(method) \
    unique_ptr<T[]> data(new T[varDataSize]{}); \
    for (int i = 0; i < static_cast<int>(varDataSize); i++) { \
        if (jsonArray[i].is##method()) \
            data[static_cast<size_t>(i)] = static_cast<T>(jsonArray[i].as##method()); \
        else \
            LWARNING << FormatParamKeys::General::VARIABLES << "." << varname << "." \
                     << FormatParamKeys::Encode::NetCDF::DATA << "[" << i << "] has an invalid data type."; \
    } \
    size_t offset{}; \
    status = nc_put_vara(dataFile, var, &offset, &varDataSize, data.get()); \
    throwOnError(status, "failed writing data for variable " << varname << " to netCDF file");

void r_Conv_NETCDF::jsonArrayToNcVar(int var, int dimid, Json::Value jsonArray)
{
    nc_type vartype{};
    status = nc_inq_vartype(dataFile, var, &vartype);
    throwOnError(status, "failed reading type of variable " << var << " from netCDF file");

    char varname[NC_MAX_NAME];
    status = nc_inq_varname(dataFile, var, varname);
    throwOnError(status, "failed reading name of variable " << var << " from netCDF file");

    size_t dimlen{};
    status = nc_inq_dimlen(dataFile, dimid, &dimlen);
    throwOnError(status, "failed reading length of dimension " << varname << " from netCDF file");

    size_t varDataSize = static_cast<size_t>(jsonArray.size());
    if (varDataSize > dimlen)
    {
        std::stringstream s;
        s << "provided more values in format parameters (" << varDataSize
          << ") than the dimension length (" << dimlen << ")";
        throw r_Error(r_Error::r_Error_Conversion, s.str());
    }
    else if (varDataSize < dimlen)
    {
        LWARNING << "provided less values in format parameters (" << varDataSize
                 << ") than the dimension length (" << dimlen << ").";
        varDataSize = dimlen;
    }

    switch (vartype)
    {
    case NC_CHAR:
    case NC_UBYTE:
    {
        using T = unsigned char;
        PUT_VAR(UInt);
        break;
    }
    case NC_BYTE:
    {
        using T = signed char;
        PUT_VAR(Int);
        break;
    }
    case NC_SHORT:
    {
        using T = signed short;
        PUT_VAR(Int);
        break;
    }
    case NC_USHORT:
    {
        using T = unsigned short;
        PUT_VAR(UInt);
        break;
    }
    case NC_INT:
    {
        using T = signed int;
        PUT_VAR(Int);
        break;
    }
    case NC_UINT:
    {
        using T = unsigned int;
        PUT_VAR(UInt);
        break;
    }
    case NC_FLOAT:
    {
        using T = float;
        PUT_VAR(Double);
        break;
    }
    case NC_DOUBLE:
    {
        using T = double;
        PUT_VAR(Double);
        break;
    }
    default:
        LWARNING << "cannot add data to netCDF variable " << varname
                 << ", unsupported netCDF type " << vartype;
        break;
    }
}

template <class T>
void r_Conv_NETCDF::addVarAttributes(int var, nc_type nctype, T validMin, T validMax, size_t dimNum)
{
    LDEBUG << "add nodata for dim " << dimNum << ", var " << var;
    if (validMax != T{})
    {
        status = nc_put_att(dataFile, var, VALID_MIN.c_str(), nctype, 1, &validMin);
        warnOnError(status, "failed adding valid_min attribute for variable to netCDF file");
        status = nc_put_att(dataFile, var, VALID_MAX.c_str(), nctype, 1, &validMax);
        warnOnError(status, "failed adding valid_max attribute for variable to netCDF file");
    }
    if (encodeOptions.isMember(FormatParamKeys::Encode::NODATA) || formatParams.getNodata().size() > 0)
    {
        boost::optional<T> nodataVal;
        if (formatParams.getNodata().size() > dimNum)
        {
            nodataVal = static_cast<T>(formatParams.getNodata()[dimNum]);
        }
        else if (formatParams.getNodata().size() == 1)
        {
            nodataVal = static_cast<T>(formatParams.getNodata()[0]);
        }
        else
        {
            LWARNING << "number of null values != number of array bands, will not add nodata attributes in netCDF file.";
        }

        if (nodataVal)
        {
            T tmp = *nodataVal;
            status = nc_put_att(dataFile, var, MISSING_VALUE.c_str(), nctype, 1, &tmp);
            throwOnError(status, "failed adding missing_value attribute to netCDF file");
            status = nc_put_att(dataFile, var, FILL_VALUE.c_str(), nctype, 1, &tmp);
            throwOnError(status, "failed adding _FillValue attribute to netCDF file");
        }
    }
}

template <class T>
void r_Conv_NETCDF::writeData(const string& varName, const std::vector<int>& dims, const char* src,
                              nc_type nctype, T validMin, T validMax, size_t dimNum)
{
    int var{};
    status = nc_def_var(dataFile, varName.c_str(), nctype, numDims, dims.data(), &var);
    throwOnError(status, "failed creating variable " << varName << " in netCDF file");

    addVarAttributes(var, nctype, validMin, validMax, dimNum);

    status = nc_put_var(dataFile, var, src);
    throwOnError(status, "failed writing data for variable " << varName << " to netCDF file");
}

template <class T>
void r_Conv_NETCDF::writeDataStruct(const string& varName, const std::vector<int>& dims,
                                    size_t structSize, size_t bandOffset,
                                    nc_type nctype, T validMin, T validMax, size_t dimNum)
{
    unique_ptr<T[]> dst(new T[dataSize]);
    const char* src = desc.src + bandOffset;
    for (size_t i = 0; i < dataSize; ++i, src += structSize)
    {
        dst[i] = *const_cast<T*>(reinterpret_cast<const T*>(src));
    }

    writeData<T>(varName, dims, reinterpret_cast<const char*>(dst.get()),
                 nctype, validMin, validMax, dimNum);
}

string r_Conv_NETCDF::getDimName(unsigned int dimId)
{
    return dimId < dimNames.size()
           ? dimNames[dimId]
           : DEFAULT_DIM_NAME_PREFIX + std::to_string(dimId);
}

const string& r_Conv_NETCDF::getVariableName()
{
    if (desc.baseType != ctype_struct && desc.baseType != ctype_rgb && varNames.size() > 1)
    {
        throw r_Error(r_Error::r_Error_Conversion,
                      "multiple variables specified for netCDF conversion but "
                      "the base type in rasdaman is not composite");
    }

    return varNames.size() >= 1 ? varNames[0] : DEFAULT_VAR;
}

void r_Conv_NETCDF::closeDataFile()
{
    if (dataFile != invalidDataFile)
    {
        status = nc_close(dataFile);
        warnOnError(status, "failed closing handle to netCDF file");
        dataFile = invalidDataFile;
    }
}

#endif
