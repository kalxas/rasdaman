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
#ifdef HAVE_NETCDF

#include "conversion/netcdf.hh"
#include "conversion/tmpfile.hh"
#include "raslib/error.hh"
#include "raslib/parseparams.hh"
#include "raslib/primitivetype.hh"
#include "raslib/attribute.hh"
#include "raslib/structuretype.hh"
#include "raslib/odmgtypes.hh"

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

const char* r_Conv_NETCDF::DEFAULT_VAR("data");
const char* r_Conv_NETCDF::DEFAULT_DIM_NAME_PREFIX("dim_");
const char* r_Conv_NETCDF::VAR_SEPARATOR_STR(";");
const char* r_Conv_NETCDF::VARS_KEY("vars");
const char* r_Conv_NETCDF::VALID_MIN("valid_min");
const char* r_Conv_NETCDF::VALID_MAX("valid_max");
const char* r_Conv_NETCDF::MISSING_VALUE("missing_value");

const char* r_Conv_NETCDF::JSON_KEY_DIMS("dimensions");
const char* r_Conv_NETCDF::JSON_KEY_VARS("variables");
const char* r_Conv_NETCDF::JSON_KEY_GLOBAL("global");
const char* r_Conv_NETCDF::JSON_KEY_NAME("name");
const char* r_Conv_NETCDF::JSON_KEY_DATA("data");
const char* r_Conv_NETCDF::JSON_KEY_METADATA("metadata");
const char* r_Conv_NETCDF::JSON_KEY_TYPE("type");

/// constructor using an r_Type object.
r_Conv_NETCDF::r_Conv_NETCDF(const char *src, const r_Minterval &interv, const r_Type *tp) throw (r_Error)
    : r_Convert_Memory(src, interv, tp, true)
{
}

/// constructor using convert_type_e shortcut
r_Conv_NETCDF::r_Conv_NETCDF(const char *src, const r_Minterval &interv, int tp) throw (r_Error)
    : r_Convert_Memory(src, interv, tp)
{
}

/// destructor
r_Conv_NETCDF::~r_Conv_NETCDF(void)
{
}

/// convert to NETCDF
r_convDesc &r_Conv_NETCDF::convertTo(const char *options) throw (r_Error)
{
    parseEncodeOptions(options);

    r_TmpFile tmpFileObj;
    string tmpFilePath = tmpFileObj.getFileName();
    NcFile dataFile(tmpFilePath.c_str(), NcFile::Replace);
    if (!dataFile.is_valid())
    {
        LFATAL << "invalid netCDF file.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    
    // Create netCDF dimensions
    int dimNo = static_cast<int>(desc.srcInterv.dimension());
    size_t dataSize = 1;
    unique_ptr<const NcDim*[]> dims(new const NcDim*[dimNo]);
    unique_ptr<long[]> dimSizes(new long[dimNo]);
    for (unsigned int i = 0; i < static_cast<unsigned int>(dimNo); i++)
    {
        dimSizes[i] = desc.srcInterv[i].get_extent();
        dataSize *= (size_t) dimSizes[i];
        dims[i] = dataFile.add_dim(getDimensionName(i).c_str(), dimSizes[i]);
    }

    // Write rasdaman data to netcdf variables in the dataFile
    if (desc.baseType == ctype_struct || desc.baseType == ctype_rgb)
    {
        writeMultipleVars(dataFile, dimNo, dims.get(), dimSizes.get(), dataSize);
    }
    else
    {
        writeSingleVar(dataFile, dimNo, dims.get(), dimSizes.get(), dataSize);
    }
    writeMetadata(dataFile);
    dataFile.close();
    
    size_t fileSize = readTmpFile(tmpFilePath.c_str());
    
    // Set the interval and type
    desc.destInterv = r_Minterval(1);
    desc.destInterv << r_Sinterval((r_Range) 0, (r_Range) fileSize - 1);
    desc.destType = r_Type::get_any_type("char");

    return desc;
}

/// convert from NETCDF
r_convDesc &r_Conv_NETCDF::convertFrom(const char *options) throw (r_Error)
{
    parseDecodeOptions(options);

    // write the data to temp file, netcdf wants a file path unfortunately
    r_TmpFile tmpFileObj;
    string tmpFilePath = tmpFileObj.getFileName();
    ofstream file(tmpFilePath);
    file.write(desc.src, (streamsize) desc.srcInterv.cell_count());
    file.close();
    
    NcFile dataFile(tmpFilePath.c_str(), NcFile::ReadOnly);
    if (!dataFile.is_valid())
    {
        LFATAL << "invalid netcdf file: '" << tmpFilePath << "'.";
        throw r_Error(r_Error::r_Error_Conversion);
    }
    validateDecodeOptions(dataFile);

    if (varNames.size() == 1)
    {
        readSingleVar(dataFile);
    }
    else
    {
        readMultipleVars(dataFile);
    }
    
    return desc;
}

void r_Conv_NETCDF::parseDecodeOptions(const char* options) throw (r_Error)
{
    if (params == NULL)
        params = new r_Parse_Params();
    char *varNamesParam = NULL;
    params->add(VARS_KEY, &varNamesParam, r_Parse_Params::param_type_string);
    
    int paramNo = params->process(options);
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

void r_Conv_NETCDF::validateDecodeOptions(const NcFile& dataFile) throw (r_Error)
{
    size_t foundVarNames = 0;
    for (int i = 0; i < dataFile.num_vars(); i++)
    {
        NcVar *var = dataFile.get_var(i);
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

void r_Conv_NETCDF::parseEncodeOptions(const char* options) throw (r_Error)
{
    if (options != NULL)
    {
        string json(options);
        // rasql transmits \" from the cmd line literally; this doesn't work
        // in json, so we unescape them below
        boost::algorithm::replace_all(json, "\\\"", "\"");

        Json::Reader reader;
        if (!reader.parse(json, encodeOptions))
        {
            LWARNING << "failed parsing the JSON options: " << reader.getFormattedErrorMessages();
            // try to parse the old-style key-value format in any case
            parseDecodeOptions(options);
        }
        else
        {
            validateJsonEncodeOptions();
        }
    }
}

void r_Conv_NETCDF::validateJsonEncodeOptions() throw (r_Error)
{
    if (!encodeOptions.isMember(JSON_KEY_DIMS))
    {
        LERROR << "mandatory format options field missing: " << JSON_KEY_DIMS;
        throw r_Error(r_Error::r_Error_Conversion);
    }
    if (!encodeOptions.isMember(JSON_KEY_VARS))
    {
        LERROR << "mandatory format options field missing: variables" << JSON_KEY_VARS;
        throw r_Error(r_Error::r_Error_Conversion);
    }
    Json::Value dims = encodeOptions[JSON_KEY_DIMS];
    for (int i = 0; i < dims.size(); i++)
    {
        dimNames.push_back(dims[i].asString());
    }
    Json::Value vars = encodeOptions[JSON_KEY_VARS];
    for (auto const& varName : vars.getMemberNames()) {
        if (find(dimNames.begin(), dimNames.end(), varName) == dimNames.end() && !vars[varName].isMember(JSON_KEY_DATA))
        {
            varNames.push_back(varName);
        }
        else
        {
            dimVarNames.push_back(varName);
        }
    }
}

size_t r_Conv_NETCDF::readTmpFile(const char* tmpFile) throw (r_Error)
{
    // Pass the NetCDF file as a stream of char
    FILE *fp = NULL;
    if ((fp = fopen(tmpFile, "rb")) == NULL)
    {
        LFATAL << "unable to open temporary file: " << tmpFile;
        LFATAL << "reason: " << strerror(errno);
        throw r_Error(r_Error::r_Error_Conversion);
    }
    // Get the file size
    long tmpFileSize = 0;
    if (fseek(fp, 0, SEEK_END) != 0 || (tmpFileSize = ftell(fp)) < 0)
    {
        LFATAL << "unable to read temporary file: " << tmpFile;
        LFATAL << "reason: " << strerror(errno);
        fclose(fp);
        throw r_Error(r_Error::r_Error_Conversion);
    }
    size_t fileSize = (size_t) tmpFileSize;
    if ((desc.dest = (char*) mystore.storage_alloc(fileSize)) == NULL)
    {
        LFATAL << "failed allocating " << fileSize << " bytes of memory.";
        fclose(fp);
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    // Set the desc.dest content
    if (fseek(fp, 0, SEEK_SET) != 0 || fread(desc.dest, 1, fileSize, fp) < fileSize)
    {
        LFATAL << "failed reading temporary file: " << tmpFile;
        LFATAL << "reason: " << strerror(errno);
        fclose(fp);
        throw r_Error(r_Error::r_Error_Conversion);
    }
    fclose(fp);
    return fileSize;
}

void r_Conv_NETCDF::readSingleVar(const NcFile &dataFile) throw (r_Error)
{
    NcVar *var = dataFile.get_var(varNames[0].c_str());
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

void r_Conv_NETCDF::readMultipleVars(const NcFile &dataFile) throw (r_Error)
{
    size_t structSize = 0;
    size_t dataSize = 0;
    int numDims = 0;
    unique_ptr<long[]> dimSizesPtr = buildStructType(dataFile, dataSize, structSize, numDims);
    long* dimSizes = dimSizesPtr.get();

    if ((desc.dest = (char*) mystore.storage_alloc(dataSize * structSize)) == NULL)
    {
        LFATAL << "failed allocating " << (dataSize * structSize) << " bytes.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }

    size_t offset = 0; // offset of the attribute within the struct type
    for (size_t i = 0; i < varNames.size(); i++)
    {
        NcVar *var = dataFile.get_var(varNames[i].c_str());
        switch (var->type())
        {
        case ncByte:
        {
            readDataStruct<ncbyte>(var, dimSizes, dataSize, structSize, offset);
            break;
        }
        case ncChar:
        {
            readDataStruct<char>(var, dimSizes, dataSize, structSize, offset);
            break;
        }
        case ncShort:
        {
            readDataStruct<short>(var, dimSizes, dataSize, structSize, offset);
            break;
        }
        case ncInt:
        {
            readDataStruct<int>(var, dimSizes, dataSize, structSize, offset);
            break;
        }
        case ncDouble:
        {
            readDataStruct<double>(var, dimSizes, dataSize, structSize, offset);
            break;
        }
        case ncFloat:
        {
            readDataStruct<float>(var, dimSizes, dataSize, structSize, offset);
            break;
        }
        default:
        {
            LFATAL << "unsupported netcdf base type: " << var->type();
            throw r_Error(r_Error::r_Error_Conversion);
        }
        }
    }

    desc.destInterv = r_Minterval(static_cast<r_Dimension> (numDims));
    for (size_t i = 0; i < (size_t)numDims; i++)
        desc.destInterv << r_Sinterval((r_Range) 0, (r_Range) dimSizes[i] - 1);
}

unique_ptr<long[]> r_Conv_NETCDF::buildStructType(const NcFile &dataFile, size_t& dataSize, size_t& structSize, int& numDims) throw (r_Error)
{
    structSize = 0;        // size of the struct type, used for offset computations in memcpy
    dataSize = 1;
    size_t alignSize = 1;  // alignment of size (taken from StructType::calcSize())
    int firstDim = 0;      // get the dimensionality of first variable to import and check if all other have the same dimensionality
    unique_ptr<long[]> dimSizes;

    stringstream destType(stringstream::out); // build the struct type string
    destType << "struct { ";
    for (size_t i = 0; i < varNames.size(); i++)
    {
        NcVar *var = dataFile.get_var(varNames[i].c_str());
        if (i > 0)
            destType << ", ";
        RasType rasType = getRasType(var);
        destType << rasType.cellType;
        structSize += rasType.cellSize;
        if (rasType.cellSize > alignSize)
            alignSize = rasType.cellSize;

        numDims = var->num_dims();
        if (i == 0)
        {
            firstDim = numDims;
            dimSizes.reset(new long[numDims]);
        }
        else
        {
            if (numDims != firstDim) {
                LFATAL << "variables have different dimension.";
                throw r_Error(r_Error::r_Error_Conversion);
            }
        }

        for (size_t j = 0; j < numDims; j++)
        {
            NcDim *dim = var->get_dim(j);
            if (i == 0)
            {
                dimSizes[j] = dim->size();
                dataSize *= static_cast<size_t>(dim->size());
            }
            else if (dim->size() != dimSizes[j])
            {
                LFATAL << "variables have different dimension sizes.";
                throw r_Error(r_Error::r_Error_Conversion);
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
    return dimSizes;
}

r_Conv_NETCDF::RasType r_Conv_NETCDF::getRasType(NcVar *var)
{
    RasType ret;
    switch (var->type())
    {
    case ncByte:
    {
        ret.cellSize = sizeof (r_Octet);
        ret.cellType = string("octet");
        break;
    }
    case ncChar:
    {
        ret.cellSize = sizeof (r_Char);
        ret.cellType = string("char");
        break;
    }
    case ncDouble:
    {
        ret.cellSize = sizeof (r_Double);
        ret.cellType = string("double");
        break;
    }
    case ncFloat:
    {
        ret.cellSize = sizeof (r_Float);
        ret.cellType = string("float");
        break;
    }
    case ncInt:
    {
        ret.cellSize = sizeof (r_Long);
        ret.cellType = string("long");
        break;
    }
    case ncShort:
    {
        ret.cellSize = sizeof (r_Short);
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
void r_Conv_NETCDF::readData(NcVar *var, r_Convertor::convert_type_e ctype) throw (r_Error)
{
    size_t numDims = (size_t)var->num_dims();
    unique_ptr<long[]> dimSizes(new long[numDims]);
    
    size_t dataSize = 1;
    desc.destInterv = r_Minterval(static_cast<r_Dimension>(numDims));
    for (size_t i = 0; i < numDims; i++)
    {
        NcDim *dim = var->get_dim(i);
        dimSizes[i] = dim->size();
        dataSize *= static_cast<size_t>(dim->size());
        desc.destInterv << r_Sinterval((r_Range) 0, (r_Range) dim->size() - 1);
    }
    
    // get variable data
    desc.dest = (char*) mystore.storage_alloc(dataSize * sizeof(T));
    if (desc.dest == NULL)
    {
        LFATAL << "failed allocating " << (dataSize * sizeof(T)) << " bytes of memory.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    var->get((T*)desc.dest, dimSizes.get());
    
    desc.destType = get_external_type(ctype);
}

template<class T>
void r_Conv_NETCDF::readDataStruct(NcVar *var, long* dimSizes, size_t dataSize, size_t structSize, size_t &offset) throw (r_Error)
{
    unique_ptr<T[]> data(new (nothrow) T[dataSize]);
    if (!data)
    {
        LFATAL << "failed allocating " << (dataSize * sizeof(T)) << " bytes of memory.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    var->get(data.get(), dimSizes);

    T *dst = (T*) (desc.dest + offset);
    unsigned int structElements = structSize / sizeof(T);
    for (unsigned int j = 0; j < dataSize; j++, dst += structElements)
        dst[0] = data[j];

    offset += sizeof(T);
}
    
/// write single variable data
void r_Conv_NETCDF::writeSingleVar(NcFile &dataFile, int dimNo, const NcDim** dims, long* dimSizes, size_t dataSize) throw (r_Error)
{
    string varName = getVariableName();
    switch (desc.baseType)
    {
    case ctype_int8:
    {
        writeData<r_Octet, ncbyte>(dataFile, varName, dimNo, dims, dimSizes, dataSize, ncByte, SCHAR_MIN, SCHAR_MAX);
        break;
    }
    case ctype_char:
    case ctype_uint8:
    {
        writeData<r_Char, short>(dataFile, varName, dimNo, dims, dimSizes, dataSize, ncShort, 0, UCHAR_MAX);
        break;
    }
    case ctype_int16:
    {
        writeData<r_Short, short>(dataFile, varName, dimNo, dims, dimSizes, dataSize, ncShort, SHRT_MIN, SHRT_MAX);
        break;
    }
    case ctype_uint16:
    {
        writeData<r_UShort, int>(dataFile, varName, dimNo, dims, dimSizes, dataSize, ncInt, 0, USHRT_MAX);
        break;
    }
    case ctype_int32:
    {
        writeData<r_Long, int>(dataFile, varName, dimNo, dims, dimSizes, dataSize, ncInt, INT_MIN, INT_MAX);
        break;
    }
    case ctype_uint32:
    {
        LWARNING << "cannot upscale type (UInt32 to Int32), overflow may happen.";
        writeData<r_ULong, int>(dataFile, varName, dimNo, dims, dimSizes, dataSize, ncInt, INT_MIN, INT_MAX);
        break;
    }
    case ctype_float32:
    {
        writeData<r_Float, float>(dataFile, varName, dimNo, dims, dimSizes, dataSize, ncFloat, 0, 0, "NaNf");
        break;
    }
    case ctype_int64:
    case ctype_uint64:
    case ctype_float64:
    {
        writeData<r_Double, double>(dataFile, varName, dimNo, dims, dimSizes, dataSize, ncDouble, 0, 0, "NaN");
        break;
    }
    default:
        LERROR << "unsupported base data type: " << desc.baseType;
        throw r_Error(r_Error::r_Error_Conversion);
    }
}

/// write multiple variables
void r_Conv_NETCDF::writeMultipleVars(NcFile &dataFile, int dimNo, const NcDim** dims, long* dimSizes, size_t dataSize) throw (r_Error)
{
    r_Structure_Type *st = static_cast<r_Structure_Type*> (const_cast<r_Type*> (desc.srcType));
    if (st == NULL)
    {
        LFATAL << "MDD object type could not be cast to struct.";
        throw r_Error(r_Error::r_Error_RefInvalid);
    }
    if (varNames.size() != static_cast<int> (st->count_elements()))
    {
        LFATAL << "mismatch in #variables between query and MDD object type.";
        throw r_Error(r_Error::r_Error_QueryParameterCountInvalid);
    }

    size_t structSize = 0; // size of the struct type, used for offset computation in memcpy
    for (r_Structure_Type::attribute_iterator ite(st->defines_attribute_begin()); ite != st->defines_attribute_end(); ite++)
    {
        r_Primitive_Type *pt = static_cast<r_Primitive_Type*> (const_cast<r_Base_Type*> (&(*ite).type_of()));
        structSize += static_cast<size_t> (pt->size());
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
            writeDataStruct<r_Octet, char>(dataFile, varName, dimNo, dims, dimSizes, dataSize, structSize, offset, ncByte, SCHAR_MIN, SCHAR_MAX);
            break;
        }
        case r_Type::CHAR:
        {
            // unsigned types are up-scaled to the next bigger signed type
            writeDataStruct<r_Char, short>(dataFile, varName, dimNo, dims, dimSizes, dataSize, structSize, offset, ncShort, 0, UCHAR_MAX);
            break;
        }
        case r_Type::SHORT:
        {
            writeDataStruct<r_Short, short>(dataFile, varName, dimNo, dims, dimSizes, dataSize, structSize, offset, ncShort, SHRT_MIN, SHRT_MAX);
            break;
        }
        case r_Type::USHORT:
        {
            // unsigned types are up-scaled to the next bigger signed type
            writeDataStruct<r_UShort, int>(dataFile, varName, dimNo, dims, dimSizes, dataSize, structSize, offset, ncInt, 0, USHRT_MAX);
            break;
        }
        case r_Type::LONG:
        {
            writeDataStruct<r_Long, int>(dataFile, varName, dimNo, dims, dimSizes, dataSize, structSize, offset, ncInt, INT_MIN, INT_MAX);
            break;
        }
        case r_Type::ULONG:
        {
            LWARNING << "cannot upscale type (UInt32 to Int32), overflow may happen.";
            writeDataStruct<r_ULong, int>(dataFile, varName, dimNo, dims, dimSizes, dataSize, structSize, offset, ncInt, INT_MIN, INT_MAX);
            break;
        }
        case r_Type::FLOAT:
        {
            writeDataStruct<r_Float, float>(dataFile, varName, dimNo, dims, dimSizes, dataSize, structSize, offset, ncFloat, 0, 0, "NaNf");
            break;
        }
        case r_Type::DOUBLE:
        {
            writeDataStruct<r_Double, double>(dataFile, varName, dimNo, dims, dimSizes, dataSize, structSize, offset, ncDouble, 0, 0, "NaN");
            break;
        }
        default:
        {
            LFATAL << "unsupported type '" << desc.baseType << "'.";
            throw r_Error(r_Error::r_Error_Conversion);
        }
        }
        r_Primitive_Type *pt = static_cast<r_Primitive_Type*> (const_cast<r_Base_Type*> (&(*iter).type_of()));
        offset += static_cast<size_t> (pt->size());
    }
}

void r_Conv_NETCDF::writeMetadata(NcFile &dataFile) throw (r_Error)
{
    if (encodeOptions.isNull())
    {
        return;
    }
    // add global
    if (encodeOptions.isMember(JSON_KEY_GLOBAL) && encodeOptions[JSON_KEY_GLOBAL].isMember(JSON_KEY_METADATA))
    {
        Json::Value globalMetadata = encodeOptions[JSON_KEY_GLOBAL][JSON_KEY_METADATA];
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
    
    if (encodeOptions.isMember(JSON_KEY_VARS))
    {
        Json::Value vars = encodeOptions[JSON_KEY_VARS];

        // add missing variables
        if (!dimVarNames.empty())
        {
            for (size_t dimVarNameId = 0; dimVarNameId < dimVarNames.size(); dimVarNameId++)
            {
                string dimVarName = dimVarNames[dimVarNameId];
                const NcDim* dim = dataFile.get_dim(dimVarName.c_str());
                Json::Value jsonVar = vars[dimVarName];
                if (jsonVar.isMember(JSON_KEY_TYPE))
                {
                    if (jsonVar.isMember(JSON_KEY_DATA))
                    {
                        NcType ncType = stringToNcType(jsonVar[JSON_KEY_TYPE].asCString());
                        if (ncType != ncNoType)
                        {
                            NcVar* var = dataFile.add_var(dimVarName.c_str(), ncType, dim);
                            Json::Value jsonDataArray = jsonVar[JSON_KEY_DATA];
                            if (jsonDataArray.isArray())
                            {
                                jsonArrayToNcVar(var, jsonDataArray);
                            }
                            else
                            {
                                LWARNING << "invalid value of field '" << JSON_KEY_DATA <<
                                    "' of variable '" << dimVarName << "', expected an array.";
                            }
                        }
                        else
                        {
                            LWARNING << "unknown netCDF variable type '" << jsonVar[JSON_KEY_TYPE] <<
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
                if (jsonVar.isMember(JSON_KEY_METADATA))
                {
                    Json::Value jsonVarMetadata = jsonVar[JSON_KEY_METADATA];
                    addJsonAttributes(dataFile, jsonVarMetadata, var);
                }
            }
        }
    }
}

#define ADD_ATTRIBUTE(v) \
    { (var != NULL) ? var->add_att(key, v) : dataFile.add_att(key, v); }

void r_Conv_NETCDF::addJsonAttributes(NcFile &dataFile, const Json::Value& metadata, NcVar* var) throw (r_Error)
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
        return ncByte;
    else if (type == string("char"))
        return ncChar;
    else if (type == string("short"))
        return ncShort;
    else if (type == string("int"))
        return ncInt;
    else if (type == string("float"))
        return ncFloat;
    else if (type == string("double"))
        return ncDouble;
    else
        return ncNoType;
}

void r_Conv_NETCDF::jsonArrayToNcVar(NcVar* var, Json::Value jsonArray)
{
    long dataSize = jsonArray.size();
    switch (var->type())
    {
    case ncByte:
    {
        unique_ptr<ncbyte[]> data(new ncbyte[dataSize]);
        for (int i = 0; i < dataSize; i++)
        {
            if (jsonArray[i].isInt())
                data[(size_t)i] = (ncbyte) jsonArray[i].asInt();
            else
                LWARNING << JSON_KEY_VARS << "." << var->name() << "." << JSON_KEY_DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), dataSize);
        break;
    }
    case ncChar:
    {
        unique_ptr<char[]> data(new char[dataSize]);
        for (int i = 0; i < dataSize; i++)
        {
            if (jsonArray[i].isInt())
                data[(size_t)i] = (char) jsonArray[i].asInt();
            else
                LWARNING << JSON_KEY_VARS << "." << var->name() << "." << JSON_KEY_DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), dataSize);
        break;
    }
    case ncShort:
    {
        unique_ptr<short[]> data(new short[dataSize]);
        for (int i = 0; i < dataSize; i++)
        {
            if (jsonArray[i].isInt())
                data[(size_t)i] = (short) jsonArray[i].asInt();
            else
                LWARNING << JSON_KEY_VARS << "." << var->name() << "." << JSON_KEY_DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), dataSize);
        break;
    }
    case ncInt:
    {
        unique_ptr<int[]> data(new int[dataSize]);
        for (int i = 0; i < dataSize; i++)
        {
            if (jsonArray[i].isInt())
                data[(size_t)i] = jsonArray[i].asInt();
            else
                LWARNING << JSON_KEY_VARS << "." << var->name() << "." << JSON_KEY_DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), dataSize);
        break;
    }
    case ncFloat:
    {
        unique_ptr<float[]> data(new float[dataSize]);
        for (int i = 0; i < dataSize; i++)
        {
            if (jsonArray[i].isDouble())
                data[(size_t)i] = (float) jsonArray[i].asFloat();
            else
                LWARNING << JSON_KEY_VARS << "." << var->name() << "." << JSON_KEY_DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), dataSize);
        break;
    }
    case ncDouble:
    {
        unique_ptr<double[]> data(new double[dataSize]);
        for (int i = 0; i < dataSize; i++)
        {
            if (jsonArray[i].isDouble())
                data[(size_t)i] = (double) jsonArray[i].asDouble();
            else
                LWARNING << JSON_KEY_VARS << "." << var->name() << "." << JSON_KEY_DATA << "[" << i << "] has an invalid data type.";
        }
        var->put(data.get(), dataSize);
        break;
    }
    default:
        LWARNING << "invalid netCDF type " << var->type();
        break;
    }
}

template <class S, class T>
void r_Conv_NETCDF::writeData(NcFile &dataFile, string& varName, int dimNo, const NcDim** dims, long* dimSizes, 
                              size_t dataSize, NcType ncType, long validMin, long validMax, 
                              const char* missingValue) throw (r_Error)
{
    NcVar *ncVar = dataFile.add_var(varName.c_str(), ncType, dimNo, dims);
    if (validMax != 0)
    {
        ncVar->add_att(VALID_MIN, (T) validMin);
        ncVar->add_att(VALID_MAX, (T) validMax);
    }
    if (missingValue != NULL)
    {
        ncVar->add_att(MISSING_VALUE, missingValue);
    }
    
    S *val = (S* &) desc.src;
    bool needToUpscaleDataType = sizeof(S) != sizeof(T);
    if (needToUpscaleDataType) // upscale data to output type
    {
        unique_ptr<T[]> data(new (nothrow) T[dataSize]);
        if (!data)
        {
            LFATAL << "failed allocating " << (dataSize * sizeof(T)) << " bytes of memory.";
            throw r_Error(r_Error::r_Error_MemoryAllocation);
        }
        for (size_t i = 0; i < dataSize; i++, val++)
        {
            data[i] = (T) val[0];
        }
        ncVar->put(data.get(), dimSizes);
    }
    else // take the input data, no type change
    {
        ncVar->put((T*) val, dimSizes);
    }
}

template <class S, class T>
void r_Conv_NETCDF::writeDataStruct(NcFile &dataFile, string& varName, int dimNo, const NcDim** dims, long* dimSizes, 
                                    size_t dataSize, size_t structSize, size_t offset, NcType ncType,
                                    long validMin, long validMax, const char* missingValue) throw (r_Error)
{
    unique_ptr<T[]> buff(new (nothrow) T[dataSize]);
    if (!buff)
    {
        LFATAL << "failed allocating " << (dataSize * sizeof(T)) << " bytes of memory.";
        throw r_Error(r_Error::r_Error_MemoryAllocation);
    }
    S* src = (S*) (desc.src + offset);
    unsigned int structElements = structSize / sizeof(S);
    for (size_t j = 0; j < dataSize; j++, src += structElements)
    {
        buff[j] = (T) *(src);
    }
    NcVar *ncVar = dataFile.add_var(varName.c_str(), ncType, dimNo, dims);
    ncVar->put(buff.get(), dimSizes);
    if (validMax != 0)
    {
        ncVar->add_att(VALID_MIN, (T) validMin);
        ncVar->add_att(VALID_MAX, (T) validMax);
    }
    if (missingValue != NULL)
    {
        ncVar->add_att(MISSING_VALUE, missingValue);
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

r_Convertor *r_Conv_NETCDF::clone(void) const
{
    return new r_Conv_NETCDF(desc.src, desc.srcInterv, desc.baseType);
}

const char *r_Conv_NETCDF::get_name(void) const
{
    return format_name_netcdf;
}

r_Data_Format r_Conv_NETCDF::get_data_format(void) const
{
    return r_NETCDF;
}

#endif
