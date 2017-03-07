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
#include "formatparamkeys.hh"

#include "conversion/transpose.hh"

#include <easylogging++.h>

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

using namespace std;

const string r_Conv_NETCDF::DEFAULT_VAR{"data"};
const string r_Conv_NETCDF::DEFAULT_DIM_NAME_PREFIX{"dim_"};
const string r_Conv_NETCDF::VAR_SEPARATOR_STR{";"};
const string r_Conv_NETCDF::VARS_KEY{"vars"};
const string r_Conv_NETCDF::VALID_MIN{"valid_min"};
const string r_Conv_NETCDF::VALID_MAX{"valid_max"};
const string r_Conv_NETCDF::MISSING_VALUE{"missing_value"};

/// constructor using an r_Type object.
r_Conv_NETCDF::r_Conv_NETCDF(const char* src, const r_Minterval& interv, const r_Type* tp) throw (r_Error)
    : r_Convert_Memory(src, interv, tp, true)
{
}

/// constructor using convert_type_e shortcut
r_Conv_NETCDF::r_Conv_NETCDF(const char* src, const r_Minterval& interv, int tp) throw (r_Error)
    : r_Convert_Memory(src, interv, tp)
{
}

/// destructor
r_Conv_NETCDF::~r_Conv_NETCDF(void)
{
}

/// convert to NETCDF
r_Conv_Desc& r_Conv_NETCDF::convertTo(const char* options) throw (r_Error)
{
#ifdef HAVE_NETCDF
    if (options)
    {
        parseEncodeOptions(string{options});
    }
    
    //if selected, transpose rasdaman data prior to writing to netcdf.
    //requires the transpose parameters to be passed to the function.
    if(formatParams.isTranspose())
    {
        transpose((char*) desc.src, desc.srcInterv, desc.srcType, formatParams.getTranspose());
    }
    
    r_TmpFile tmpFileObj;
    string tmpFilePath = tmpFileObj.getFileName();
    NcFile dataFile(tmpFilePath.c_str(), NcFile::Replace);
    if (!dataFile.is_valid())
    {
        LFATAL << "invalid netCDF file.";
        throw r_Error(r_Error::r_Error_Conversion);
    }

    // Create netCDF dimensions
    numDims = desc.srcInterv.dimension();
    dataSize = 1;
    dimSizes.reserve(numDims);
    unique_ptr<const NcDim*[]> dims(new const NcDim*[numDims]);
    for (unsigned int i = 0; i < static_cast<unsigned int>(numDims); i++) 
    {
            dimSizes[i] = desc.srcInterv[i].get_extent();
            dataSize *= (size_t) dimSizes[i];
            dims[i] = dataFile.add_dim(getDimensionName(i).c_str(), dimSizes[i]);
    }
        
    // Write rasdaman data to netcdf variables in the dataFile
    if (desc.baseType == ctype_struct || desc.baseType == ctype_rgb)
    {
        writeMultipleVars(dataFile, dims.get());
    }
    else
    {
        writeSingleVar(dataFile, dims.get());
    }
    writeMetadata(dataFile);
    dataFile.close();

    long fileSize = 0;
    desc.dest = tmpFileObj.readData(fileSize);
    desc.destInterv = r_Minterval(1);
    desc.destInterv << r_Sinterval((r_Range) 0, (r_Range) fileSize - 1);
    desc.destType = r_Type::get_any_type("char");

    return desc;

#else

    LERROR << "encoding netCDF is not supported; rasdaman should be configured with option --with-netcdf to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);

#endif
}

/// convert from NETCDF
r_Conv_Desc& r_Conv_NETCDF::convertFrom(const char* options) throw (r_Error)
{
#ifdef HAVE_NETCDF
    if (options)
    {
        parseDecodeOptions(string{options});
    }
    return this->convertFrom(formatParams);
#else
    LERROR << "decoding netCDF is not supported; rasdaman should be configured with option --with-netcdf to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);
#endif
}

r_Conv_Desc& r_Conv_NETCDF::convertFrom(r_Format_Params options) throw(r_Error)
{
#ifdef HAVE_NETCDF
    formatParams = options;

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
    NcFile dataFile(tmpFilePath.c_str(), NcFile::ReadOnly);
    if (!dataFile.is_valid())
    {
        LFATAL << "invalid netcdf file: '" << tmpFilePath << "'.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    if (!formatParams.getVariables().empty())
    {
        varNames = formatParams.getVariables();
    }
    validateDecodeOptions(dataFile);
    if (varNames.empty())
    {
        LFATAL << "no variables specified to decode.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    readDimSizes(dataFile);

    if (varNames.size() == 1)
    {
        readSingleVar(dataFile);
    }
    else
    {
        readMultipleVars(dataFile);
    }
    
    //if selected, transposes rasdaman data after converting from netcdf
    if(formatParams.isTranspose())
    {
        transpose(desc.dest, desc.destInterv, (const r_Type*) desc.destType, formatParams.getTranspose());
    }

    return desc;

#else

    LERROR << "decoding netCDF is not supported; rasdaman should be configured with option --with-netcdf to enable it.";
    throw r_Error(r_Error::r_Error_FeatureNotSupported);

#endif
}

#ifdef HAVE_NETCDF

void r_Conv_NETCDF::readDimSizes(const NcFile& dataFile) throw (r_Error)
{
    NcVar* var = dataFile.get_var(varNames[0].c_str());
    numDims = (size_t)var->num_dims();

    r_Minterval subsetDomain = formatParams.getSubsetDomain();
    if (subsetDomain.dimension() != 0 && subsetDomain.dimension() != numDims)
    {
        LERROR << "invalid 'subsetDomain' parameter '" << subsetDomain << "' of dimension " <<
               subsetDomain.dimension() << " given, input variable is of dimension " << numDims;
        throw r_Error(INVALIDFORMATPARAMETER);
    }

    dimSizes.reserve(numDims);
    dimOffsets.reserve(numDims);
    dataSize = 1;
    desc.destInterv = r_Minterval(static_cast<r_Dimension>(numDims));
    for (size_t i = 0; i < numDims; i++)
    {
        NcDim* dim = var->get_dim(i);
        if (subsetDomain.dimension() != 0)
        {
            dimSizes[i] = subsetDomain[i].get_extent();
            dimOffsets[i] = subsetDomain[i].low();
        }
        else
        {
            dimSizes[i] = dim->size();
            dimOffsets[i] = 0;
        }
        dataSize *= static_cast<size_t>(dimSizes[i]);
        desc.destInterv << r_Sinterval((r_Range) dimOffsets[i], (r_Range)(dimOffsets[i] + dimSizes[i] - 1));
    }
}

void r_Conv_NETCDF::parseDecodeOptions(const string& options) throw (r_Error)
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
            LERROR << "no variable names given, at least one variable name must be " <<
                   "specified in the format options as 'vars=var1;var2;..'";
            throw r_Error(r_Error::r_Error_Conversion);
        }
        if (paramNo == -1)
        {
            LERROR << "failed processing format options '" << options << "';" <<
                   " make sure options are of the format 'vars=var1;var2;..'";
            throw r_Error(r_Error::r_Error_Conversion);
        }

        string varNamesStr(varNamesParam);
        free(varNamesParam);
        boost::split(varNames, varNamesStr, boost::is_any_of(VAR_SEPARATOR_STR));
    }
}

void r_Conv_NETCDF::validateDecodeOptions(const NcFile& dataFile) throw (r_Error)
{
    size_t foundVarNames = 0;
    for (int i = 0; i < dataFile.num_vars(); i++)
    {
        NcVar* var = dataFile.get_var(i);
        if (find(varNames.begin(), varNames.end(), string(var->name())) != varNames.end())
        {
            ++foundVarNames;
        }
    }
    if (foundVarNames != varNames.size())
    {
        LERROR << "no variable found to import.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
}

void r_Conv_NETCDF::parseEncodeOptions(const string& options) throw (r_Error)
{
    if (!formatParams.parse(options))
    {
        LWARNING << "failed parsing the JSON options, error: " << formatParams.getParseErrorMsg();
        LINFO << "attempting to parse key/value style options.";
        // try to parse the old-style key-value format in any case
        parseDecodeOptions(options);
    }
    else
    {
        encodeOptions = formatParams.getParams();
        validateJsonEncodeOptions();
    }
}

void r_Conv_NETCDF::validateJsonEncodeOptions() throw (r_Error)
{
    if (!encodeOptions.isMember(FormatParamKeys::Encode::NetCDF::DIMENSIONS))
    {
        LERROR << "mandatory format options field missing: " << FormatParamKeys::Encode::NetCDF::DIMENSIONS;
        throw r_Error(r_Error::r_Error_Conversion);
    }
    if (!encodeOptions.isMember(FormatParamKeys::General::VARIABLES))
    {
        LERROR << "mandatory format options field missing: variables" << FormatParamKeys::General::VARIABLES;
        throw r_Error(r_Error::r_Error_Conversion);
    }
    Json::Value dims = encodeOptions[FormatParamKeys::Encode::NetCDF::DIMENSIONS];
    for (int i = 0; i < dims.size(); i++)
    {
        //create the vector of dimension metadata names and swap the last two in case transposition is selected as an option
        if(formatParams.isTranspose() && i == dims.size()-2)
        {
            dimNames.push_back(dims[dims.size()-1].asString());
        }
        else if(formatParams.isTranspose() && i == dims.size()-1)
        {
            dimNames.push_back(dims[dims.size()-2].asString());
        }
        else{
            dimNames.push_back(dims[i].asString());
        }
    }
    Json::Value vars = encodeOptions[FormatParamKeys::General::VARIABLES];
    for (auto const& varName : vars.getMemberNames())
    {
        if (find(dimNames.begin(), dimNames.end(), varName) == dimNames.end() &&
                !vars[varName].isMember(FormatParamKeys::Encode::NetCDF::DATA))
        {
            varNames.push_back(varName);
        }
        else
        {
            dimVarNames.push_back(varName);
        }
    }
}

void r_Conv_NETCDF::readSingleVar(const NcFile& dataFile) throw (r_Error)
{
    NcVar* var = dataFile.get_var(varNames[0].c_str());
    switch (var->type())
    {
    case ncByte:
    {
        readData<ncbyte>(var, ctype_int8);
        break;
    }
    case ncChar:
    {
        readData<char>(var, ctype_char);
        break;
    }
    case ncShort:
    {
        readData<short>(var, ctype_int16);
        break;
    }
    case ncInt:
    {
        readData<int>(var, ctype_int32);
        break;
    }
    case ncFloat:
    {
        readData<float>(var, ctype_float32);
        break;
    }
    case ncDouble:
    {
        readData<double>(var, ctype_float64);
        break;
    }
    default:
    {
        LERROR << "unsupported netcdf variable type: " << var->type() << ".";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    }
}

void r_Conv_NETCDF::readMultipleVars(const NcFile& dataFile) throw (r_Error)
{
    size_t structSize = buildStructType(dataFile);

    if ((desc.dest = (char*) mystore.storage_alloc(dataSize * structSize)) == NULL)
    {
        LFATAL << "failed allocating " << (dataSize * structSize) << " bytes.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }

    size_t offset = 0; // offset of the attribute within the struct type
    for (size_t i = 0; i < varNames.size(); i++)
    {
        NcVar* var = dataFile.get_var(varNames[i].c_str());
        switch (var->type())
        {
        case ncByte:
        {
            readDataStruct<ncbyte>(var, structSize, offset);
            break;
        }
        case ncChar:
        {
            readDataStruct<char>(var, structSize, offset);
            break;
        }
        case ncShort:
        {
            readDataStruct<short>(var, structSize, offset);
            break;
        }
        case ncInt:
        {
            readDataStruct<int>(var, structSize, offset);
            break;
        }
        case ncDouble:
        {
            readDataStruct<double>(var, structSize, offset);
            break;
        }
        case ncFloat:
        {
            readDataStruct<float>(var, structSize, offset);
            break;
        }
        default:
        {
            LFATAL << "unsupported netcdf base type: " << var->type();
            throw r_Error(r_Error::r_Error_Conversion);
        }
        }
    }
}

size_t r_Conv_NETCDF::buildStructType(const NcFile& dataFile) throw (r_Error)
{
    size_t structSize = 0;        // size of the struct type, used for offset computations in memcpy
    size_t alignSize = 1;  // alignment of size (taken from StructType::calcSize())

    stringstream destType(stringstream::out); // build the struct type string
    destType << "struct { ";
    NcVar* firstVar = dataFile.get_var(varNames[0].c_str());
    for (size_t i = 0; i < varNames.size(); i++)
    {
        NcVar* var = dataFile.get_var(varNames[i].c_str());
        if (numDims != var->num_dims())
        {
            LFATAL << "variable '" << varNames[i] << "' has different dimensionality from the first variable '" << varNames[0] << "'.";
            throw r_Error(r_Error::r_Error_Conversion);
        }

        if (i > 0)
        {
            destType << ", ";
        }
        RasType rasType = getRasType(var);
        destType << rasType.cellType;
        structSize += rasType.cellSize;
        if (rasType.cellSize > alignSize)
        {
            alignSize = rasType.cellSize;
        }

        if (i > 0)
        {
            for (size_t j = 0; j < numDims; j++)
            {
                NcDim* dim = var->get_dim(j);
                NcDim* firstDim = firstVar->get_dim(j);
                if (dim->size() != firstDim->size())
                {
                    LFATAL << "variable '" << varNames[i] << "' has different dimension sizes from the first variable '" << varNames[0] <<
                           "': dimension " << j << " expected size: " << firstDim->size() << ", got: " << dim->size() << ".";
                    throw r_Error(r_Error::r_Error_Conversion);
                }
            }
        }
    }

    // align struct size to the member type of biggest size
    if (structSize % alignSize != 0)
    {
        structSize = (structSize / alignSize + 1) * alignSize;
    }

    destType << " }";
    desc.destType = r_Type::get_any_type(destType.str().c_str());
    desc.baseType = ctype_struct;
    return structSize;
}

r_Conv_NETCDF::RasType r_Conv_NETCDF::getRasType(NcVar* var)
{
    RasType ret;
    switch (var->type())
    {
    case ncByte:
    {
        ret.cellSize = sizeof(r_Octet);
        ret.cellType = string("octet");
        break;
    }
    case ncChar:
    {
        ret.cellSize = sizeof(r_Char);
        ret.cellType = string("char");
        break;
    }
    case ncDouble:
    {
        ret.cellSize = sizeof(r_Double);
        ret.cellType = string("double");
        break;
    }
    case ncFloat:
    {
        ret.cellSize = sizeof(r_Float);
        ret.cellType = string("float");
        break;
    }
    case ncInt:
    {
        ret.cellSize = sizeof(r_Long);
        ret.cellType = string("long");
        break;
    }
    case ncShort:
    {
        ret.cellSize = sizeof(r_Short);
        ret.cellType = string("short");
        break;
    }
    default:
    {
        LERROR << "unsupported netcdf variable type: " << var->type() << ".";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    }
    return ret;
}

template<class T>
void r_Conv_NETCDF::readData(NcVar* var, convert_type_e ctype) throw (r_Error)
{
    // get variable data
    desc.dest = (char*) mystore.storage_alloc(dataSize * sizeof(T));
    if (desc.dest == NULL)
    {
        LFATAL << "failed allocating " << (dataSize * sizeof(T)) << " bytes of memory.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    var->set_cur(dimOffsets.data());
    var->get((T*)desc.dest, dimSizes.data());

    desc.destType = get_external_type(ctype);
}

template<class T>
void r_Conv_NETCDF::readDataStruct(NcVar* var, size_t structSize, size_t& bandOffset) throw (r_Error)
{
    unique_ptr<T[]> data(new(nothrow) T[dataSize]);
    if (!data)
    {
        LFATAL << "failed allocating " << (dataSize * sizeof(T)) << " bytes of memory.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    var->set_cur(dimOffsets.data());
    var->get(data.get(), dimSizes.data());

    T* dst = (T*)(desc.dest + bandOffset);
    unsigned int structElements = structSize / sizeof(T);
    for (unsigned int j = 0; j < dataSize; j++, dst += structElements)
    {
        dst[0] = data[j];
    }

    bandOffset += sizeof(T);
}

/// write single variable data
void r_Conv_NETCDF::writeSingleVar(NcFile& dataFile, const NcDim** dims) throw (r_Error)
{
    string varName = getVariableName();
    switch (desc.baseType)
    {
    case ctype_int8:
    {
        writeData<r_Octet, ncbyte>(dataFile, varName, dims, ncByte, SCHAR_MIN, SCHAR_MAX);
        break;
    }
    case ctype_char:
    case ctype_uint8:
    {
        writeData<r_Char, short>(dataFile, varName, dims, ncShort, 0, UCHAR_MAX);
        break;
    }
    case ctype_int16:
    {
        writeData<r_Short, short>(dataFile, varName, dims, ncShort, SHRT_MIN, SHRT_MAX);
        break;
    }
    case ctype_uint16:
    {
        writeData<r_UShort, int>(dataFile, varName, dims, ncInt, 0, USHRT_MAX);
        break;
    }
    case ctype_int32:
    {
        writeData<r_Long, int>(dataFile, varName, dims, ncInt, INT_MIN, INT_MAX);
        break;
    }
    case ctype_uint32:
    {
        LWARNING << "cannot upscale type (UInt32 to Int32), overflow may happen.";
        writeData<r_ULong, int>(dataFile, varName, dims, ncInt, INT_MIN, INT_MAX);
        break;
    }
    case ctype_float32:
    {
        writeData<r_Float, float>(dataFile, varName, dims, ncFloat, 0, 0, "NaNf");
        break;
    }
    case ctype_int64:
    case ctype_uint64:
    case ctype_float64:
    {
        writeData<r_Double, double>(dataFile, varName, dims, ncDouble, 0, 0, "NaN");
        break;
    }
    default:
        LERROR << "unsupported base data type: " << desc.baseType;
        throw r_Error(r_Error::r_Error_Conversion);
    }
}

/// write multiple variables
void r_Conv_NETCDF::writeMultipleVars(NcFile& dataFile, const NcDim** dims) throw (r_Error)
{
    r_Structure_Type* st = static_cast<r_Structure_Type*>(const_cast<r_Type*>(desc.srcType));
    if (st == NULL)
    {
        LFATAL << "MDD object type could not be cast to struct.";
        throw r_Error(r_Error::r_Error_RefInvalid);
    }
    if (varNames.size() != static_cast<int>(st->count_elements()))
    {
        LFATAL << "mismatch in #variables between query and MDD object type.";
        throw r_Error(r_Error::r_Error_QueryParameterCountInvalid);
    }

    size_t structSize = 0; // size of the struct type, used for offset computation in memcpy
    for (r_Structure_Type::attribute_iterator ite(st->defines_attribute_begin()); ite != st->defines_attribute_end(); ite++)
    {
        r_Primitive_Type* pt = static_cast<r_Primitive_Type*>(const_cast<r_Base_Type*>(&(*ite).type_of()));
        structSize += static_cast<size_t>(pt->size());
    }

    r_Structure_Type::attribute_iterator iter(st->defines_attribute_begin());
    size_t offset = 0; // offset of the attribute within the struct type
    for (size_t i = 0; i < varNames.size(); i++, iter++)
    {
        string varName = varNames[i];
        switch ((*iter).type_of().type_id())
        {
        case r_Type::OCTET:
        {
            writeDataStruct<r_Octet, char>(dataFile, varName, dims, structSize, offset, ncByte, SCHAR_MIN, SCHAR_MAX, 0, i);
            break;
        }
        case r_Type::CHAR:
        {
            // unsigned types are up-scaled to the next bigger signed type
            writeDataStruct<r_Char, short>(dataFile, varName, dims, structSize, offset, ncShort, 0, UCHAR_MAX, 0, i);
            break;
        }
        case r_Type::SHORT:
        {
            writeDataStruct<r_Short, short>(dataFile, varName, dims, structSize, offset, ncShort, SHRT_MIN, SHRT_MAX, 0, i);
            break;
        }
        case r_Type::USHORT:
        {
            // unsigned types are up-scaled to the next bigger signed type
            writeDataStruct<r_UShort, int>(dataFile, varName, dims, structSize, offset, ncInt, 0, USHRT_MAX, 0, i);
            break;
        }
        case r_Type::LONG:
        {
            writeDataStruct<r_Long, int>(dataFile, varName, dims, structSize, offset, ncInt, INT_MIN, INT_MAX, 0, i);
            break;
        }
        case r_Type::ULONG:
        {
            LWARNING << "cannot upscale type (UInt32 to Int32), overflow may happen.";
            writeDataStruct<r_ULong, int>(dataFile, varName, dims, structSize, offset, ncInt, INT_MIN, INT_MAX, 0, i);
            break;
        }
        case r_Type::FLOAT:
        {
            writeDataStruct<r_Float, float>(dataFile, varName, dims, structSize, offset, ncFloat, 0, 0, "NaNf", i);
            break;
        }
        case r_Type::DOUBLE:
        {
            writeDataStruct<r_Double, double>(dataFile, varName, dims, structSize, offset, ncDouble, 0, 0, "NaN", i);
            break;
        }
        default:
        {
            LFATAL << "unsupported type '" << desc.baseType << "'.";
            throw r_Error(r_Error::r_Error_Conversion);
        }
        }
        r_Primitive_Type* pt = static_cast<r_Primitive_Type*>(const_cast<r_Base_Type*>(&(*iter).type_of()));
        offset += static_cast<size_t>(pt->size());
    }
}

void r_Conv_NETCDF::writeMetadata(NcFile& dataFile) throw (r_Error)
{
    if (encodeOptions.isNull())
    {
        return;
    }
    // add global
    if (encodeOptions.isMember(FormatParamKeys::Encode::METADATA))
    {
        const Json::Value& globalMetadata = encodeOptions[FormatParamKeys::Encode::METADATA];
        addJsonAttributes(dataFile, globalMetadata, NULL);
    }
    // rename dimensions
    if (!dimNames.empty())
    {
        for (size_t i = 0; i < dimNames.size(); i++)
        {
            string dimName = dimNames[i];
            NcDim* dim = dataFile.get_dim(i);
            if (dim != NULL)
            {
                dim->rename(dimName.c_str());
            }
        }
    }

    if (encodeOptions.isMember(FormatParamKeys::General::VARIABLES))
    {
        const Json::Value& vars = encodeOptions[FormatParamKeys::General::VARIABLES];

        // add missing variables
        if (!dimVarNames.empty())
        {
            for (size_t dimVarNameId = 0; dimVarNameId < dimVarNames.size(); dimVarNameId++)
            {
                string dimVarName = dimVarNames[dimVarNameId];
                const NcDim* dim = dataFile.get_dim(dimVarName.c_str());
                Json::Value jsonVar = vars[dimVarName];
                if (jsonVar.isMember(FormatParamKeys::Encode::NetCDF::TYPE))
                {
                    if (jsonVar.isMember(FormatParamKeys::Encode::NetCDF::DATA))
                    {
                        NcType ncType = stringToNcType(jsonVar[FormatParamKeys::Encode::NetCDF::TYPE].asCString());
                        if (ncType != ncNoType)
                        {
                            NcVar* var = dataFile.add_var(dimVarName.c_str(), ncType, dim);
                            Json::Value jsonDataArray = jsonVar[FormatParamKeys::Encode::NetCDF::DATA];
                            if (jsonDataArray.isArray())
                            {
                                jsonArrayToNcVar(var, jsonDataArray);
                            }
                            else
                            {
                                LWARNING << "invalid value of field '" << FormatParamKeys::Encode::NetCDF::DATA <<
                                         "' of variable '" << dimVarName << "', expected an array.";
                            }                   
                        }
                        else
                        {
                            LWARNING << "unknown netCDF variable type '" << jsonVar[FormatParamKeys::Encode::NetCDF::TYPE] <<
                                     "' in variable '" << dimVarName << "', expected one of: byte, char, short, int, float, double.";
                        }
                    }
                    else
                    {
                        LWARNING << "variable '" << dimVarName << "' has no data array, it will not be added to the exported netCDF file.";
                    }
                }
                else
                {
                    LWARNING << "variable '" << dimVarName << "' has no type, it will not be added to the exported netCDF file.";
                }
            }
        }


        for (int i = 0; i < dataFile.num_vars(); i++)
        {
            NcVar* var = dataFile.get_var(i);
            const char* varName = var->name();
            if (vars.isMember(varName))
            {
                Json::Value jsonVar = vars[varName];
                if (jsonVar.isMember(FormatParamKeys::Encode::METADATA)) 
                {
                    Json::Value jsonVarMetadata = jsonVar[FormatParamKeys::Encode::METADATA];
                    addJsonAttributes(dataFile, jsonVarMetadata, var);
                }
            }
        }
    }    
}

#define ADD_ATTRIBUTE(v) \
    { (var != NULL) ? var->add_att(key, v) : dataFile.add_att(key, v); }

void r_Conv_NETCDF::addJsonAttributes(NcFile& dataFile, const Json::Value& metadata, NcVar* var) throw (r_Error)
{
    vector<string> m = metadata.getMemberNames();
    for (const string& keyStr : m)
    {
        Json::Value value = metadata[keyStr];
        const char* key = keyStr.c_str();
        if (value.isString())
        {
            ADD_ATTRIBUTE(value.asCString());
        }
        else if (value.isInt())
        {
            ADD_ATTRIBUTE(value.asInt());
        }
        else if (value.isDouble())
        {
            ADD_ATTRIBUTE(value.asDouble());
        }
        else if (value.isConvertibleTo(Json::ValueType::stringValue))
        {
            string tmpVal = value.asString();
            ADD_ATTRIBUTE(tmpVal.c_str());
        }
    }
}

NcType r_Conv_NETCDF::stringToNcType(string type)
{
    if (type == string("byte"))
    {
        return ncByte;
    }
    else if (type == string("char"))
    {
        return ncChar;
    }
    else if (type == string("short"))
    {
        return ncShort;
    }
    else if (type == string("int"))
    {
        return ncInt;
    }
    else if (type == string("float"))
    {
        return ncFloat;
    }
    else if (type == string("double"))
    {
        return ncDouble;
    }
    else
    {
        return ncNoType;
    }
}

void r_Conv_NETCDF::jsonArrayToNcVar(NcVar* var, Json::Value jsonArray)
{
    long varDataSize = jsonArray.size();
    switch (var->type())
    {
    case ncByte:
    {
        unique_ptr<ncbyte[]> data(new ncbyte[varDataSize]);
        for (int i = 0; i < varDataSize; i++)
        {
            if (jsonArray[i].isInt())
            {
                data[(size_t)i] = (ncbyte) jsonArray[i].asInt();
            }
            else
                LWARNING << FormatParamKeys::General::VARIABLES << "." << var->name() << "." <<
                         FormatParamKeys::Encode::NetCDF::DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), varDataSize);
        break;
    }
    case ncChar:
    {
        unique_ptr<char[]> data(new char[varDataSize]);
        for (int i = 0; i < varDataSize; i++)
        {
            if (jsonArray[i].isInt())
            {
                data[(size_t)i] = (char) jsonArray[i].asInt();
            }
            else
                LWARNING << FormatParamKeys::General::VARIABLES << "." << var->name() << "." <<
                         FormatParamKeys::Encode::NetCDF::DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), varDataSize);
        break;
    }
    case ncShort:
    {
        unique_ptr<short[]> data(new short[varDataSize]);
        for (int i = 0; i < varDataSize; i++)
        {
            if (jsonArray[i].isInt())
            {
                data[(size_t)i] = (short) jsonArray[i].asInt();
            }
            else
                LWARNING << FormatParamKeys::General::VARIABLES << "." << var->name() << "." <<
                         FormatParamKeys::Encode::NetCDF::DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), varDataSize);
        break;
    }
    case ncInt:
    {
        unique_ptr<int[]> data(new int[varDataSize]);
        for (int i = 0; i < varDataSize; i++)
        {
            if (jsonArray[i].isInt())
            {
                data[(size_t)i] = jsonArray[i].asInt();
            }
            else
                LWARNING << FormatParamKeys::General::VARIABLES << "." << var->name() << "." <<
                         FormatParamKeys::Encode::NetCDF::DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), varDataSize);
        break;
    }
    case ncFloat:
    {
        unique_ptr<float[]> data(new float[varDataSize]);
        for (int i = 0; i < varDataSize; i++)
        {
            if (jsonArray[i].isDouble())
            {
                data[(size_t)i] = (float) jsonArray[i].asFloat();
            }
            else
                LWARNING << FormatParamKeys::General::VARIABLES << "." << var->name() << "." <<
                         FormatParamKeys::Encode::NetCDF::DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), varDataSize);
        break;
    }
    case ncDouble:
    {
        unique_ptr<double[]> data(new double[varDataSize]);
        for (int i = 0; i < varDataSize; i++)
        {
            if (jsonArray[i].isDouble())
            {
                data[(size_t)i] = (double) jsonArray[i].asDouble();
            }
            else
                LWARNING << FormatParamKeys::General::VARIABLES << "." << var->name() << "." <<
                         FormatParamKeys::Encode::NetCDF::DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), varDataSize);
        break;
    }
    default:
        LWARNING << "invalid netCDF type " << var->type();
        break;
    }
}

template <class S, class T>
void r_Conv_NETCDF::writeData(NcFile& dataFile, string& varName, const NcDim** dims, NcType ncType,
                              long validMin, long validMax, const char* missingValue) throw (r_Error)
{
    NcVar* ncVar = dataFile.add_var(varName.c_str(), ncType, numDims, dims);
    if (validMax != 0)
    {
        ncVar->add_att(VALID_MIN.c_str(), (T) validMin);
        ncVar->add_att(VALID_MAX.c_str(), (T) validMax);
    }
    if (encodeOptions.isMember(FormatParamKeys::Encode::NODATA))
    {
        double noDataVal = formatParams.getNodata()[0];
        ncVar->add_att(MISSING_VALUE.c_str(), (T) noDataVal);
        ncVar->add_att("_FillValue", (T) noDataVal);
    }
    else if(missingValue != NULL)
    {
        ncVar->add_att(MISSING_VALUE.c_str(), missingValue);
    }

    S* val = (S*&) desc.src;
    bool needToUpscaleDataType = sizeof(S) != sizeof(T);
    if (needToUpscaleDataType) // upscale data to output type
    {
        unique_ptr<T[]> data(new(nothrow) T[dataSize]);
        if (!data)
        {
            LFATAL << "failed allocating " << (dataSize * sizeof(T)) << " bytes of memory.";
            throw r_Error(r_Error::r_Error_MemoryAllocation);
        }
        for (size_t i = 0; i < dataSize; i++, val++)
        {
            data[i] = (T) val[0];
        }
        ncVar->put(data.get(), dimSizes.data());
    }
    else // take the input data, no type change
    {
        ncVar->put((T*) val, dimSizes.data());
    }
}

template <class S, class T>
void r_Conv_NETCDF::writeDataStruct(NcFile& dataFile, string& varName, const NcDim** dims, size_t structSize, size_t bandOffset,
                                    NcType ncType, long validMin, long validMax, const char* missingValue, size_t dimNum) throw (r_Error)
{
    unique_ptr<T[]> buff(new(nothrow) T[dataSize]);
    if (!buff)
    {
        LFATAL << "failed allocating " << (dataSize * sizeof(T)) << " bytes of memory.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    S* src = (S*)(desc.src + bandOffset);
    unsigned int structElements = structSize / sizeof(S);
    for (size_t j = 0; j < dataSize; j++, src += structElements)
    {
        buff[j] = (T) * (src);
    }
    NcVar* ncVar = dataFile.add_var(varName.c_str(), ncType, numDims, dims);
    ncVar->put(buff.get(), dimSizes.data());
    if (validMax != 0)
    {
        ncVar->add_att(VALID_MIN.c_str(), (T) validMin);
        ncVar->add_att(VALID_MAX.c_str(), (T) validMax);
    }
    if (encodeOptions.isMember(FormatParamKeys::Encode::NODATA))
    {
        double noDataVal = formatParams.getNodata()[dimNum];
        ncVar->add_att(MISSING_VALUE.c_str(), (T) noDataVal);
        ncVar->add_att("_FillValue", (T) noDataVal);
    }
    else if(missingValue != NULL)
    {
        ncVar->add_att(MISSING_VALUE.c_str(), missingValue);
    }
}

string r_Conv_NETCDF::getDimensionName(unsigned int dimId) throw (r_Error)
{
    static string dimNamePrefix(DEFAULT_DIM_NAME_PREFIX);
    stringstream dimName;
    dimName << dimNamePrefix << dimId;
    return dimName.str();
}

string r_Conv_NETCDF::getVariableName() throw (r_Error)
{
    string ret;
    if (desc.baseType != ctype_struct && varNames.size() > 1)
    {
        LERROR << "mismatch in #variables specified in format options and the MDD object type.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    if (varNames.size() == 1)
    {
        ret = varNames[0];
    }
    else if (varNames.empty())
    {
        ret = string(DEFAULT_VAR);
    }
    return ret;
}

#endif

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

