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
 * SOURCE: convertor.cc
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Convertor, r_Convert_Memory
 *
 * PURPOPSE:
 * Provides interface to convert data to other formats.
 *
 * COMMENTS:
 * - FIXME: r_Convertor::get_internal_type(): every structType is recognised as RGB!
 *
*/

#include "conversion/memfs.hh"
#include "raslib/error.hh"
#include "raslib/parseparams.hh"
#include "raslib/primitivetype.hh"
#include "raslib/structuretype.hh"
#include "convertor.hh"

#include <logging.hh>


/*
 *  r_Convertor class
 */

void r_Convertor::initShare(const char* src, const r_Minterval& interv)
{
    desc.src = src;
    desc.srcInterv = interv;
    desc.srcType = NULL;
    desc.destType = NULL;
    desc.dest = NULL;
    params = NULL;
    destroySrc = false;
}


r_Convertor::r_Convertor(void)
{
    desc.srcType = NULL;
    desc.destType = NULL;
    desc.dest = NULL;
    desc.src = NULL;
    desc.baseType = ctype_void;
    params = NULL;
}


r_Convertor::r_Convertor(const char* src, const r_Minterval& interv, const r_Type* tp, bool fullTypes)
{
    initShare(src, interv);

    desc.srcType = tp;

    if (tp == NULL)
    {
        throw r_Error(r_Error::r_Error_Conversion, "source type is null");
    }

    // Initialise desc.baseType from desc.srcType
    desc.baseType = get_internal_type(tp, fullTypes);

    if (!fullTypes)
    {
        switch (tp->type_id())
        {
        case r_Type::FLOAT:
            this->applyColorScheme<float>();
            break;
        case r_Type::DOUBLE:
            applyColorScheme<double>();
            break;
        case r_Type::USHORT:
            applyColorScheme<unsigned short>();
            break;
        case r_Type::SHORT:
            applyColorScheme<short>();
            break;
        default:
            break;
        }
    }
}


r_Convertor::r_Convertor(const char* src, const r_Minterval& interv, int type)
{
    initShare(src, interv);

    desc.baseType = type;
}


r_Convertor::~r_Convertor(void)
{
    LTRACE << "r_Convertor destructor.";
    if (params != NULL)
    {
        delete params;
        params = NULL;
    }
    if (destroySrc)
    {
        delete desc.src;
        destroySrc = false;
    }
    // delete unless there was a releaseDest() call beforehand
    if (desc.dest)
    {
        free(desc.dest);
        desc.dest = NULL;
    }
    if (desc.destType)
    {
        delete desc.destType;
        desc.destType = NULL;
    }
}

void r_Convertor::releaseDest()
{
    desc.dest = NULL;
    desc.destType = NULL;
}


void r_Convertor::set_format(const std::string& formatArg)
{
    format = formatArg;
}


void r_Convertor::set_storage_handler(const r_Storage_Man& newStore)
{
    mystore = newStore;
}


const r_Storage_Man&
r_Convertor::get_storage_handler() const
{
    return mystore;
}

std::string r_Convertor::type_to_string(int ctype)
{
    switch (ctype)
    {
    case ctype_bool:
        return "boolean";
    case ctype_char:
    case ctype_uint8:
        return "char";
    case ctype_int8:
        return "octet";
    case ctype_int16:
        return "short";
    case ctype_uint16:
        return "ushort";
    case ctype_int32:
        return "long";
    case ctype_uint32:
        return "ulong";
    case ctype_int64:
        return "double";  // currently unsupported
    case ctype_uint64:
        return "double";  // currently unsupported
    case ctype_float32:
        return "float";
    case ctype_float64:
        return "double";
    case ctype_rgb:
        return "struct {char, char, char}";
    case ctype_complex1:
        return "complex";
    case ctype_complex2:
        return "complexd";
    case ctype_cint16:
        return "cint16";
    case ctype_cint32:
        return "cint32";
    case ctype_struct:
        return "struct {char, char, char, char}";
    default:
      {
        std::stringstream s;
        s << "unsupported base type " << ctype;
        throw r_Error(r_Error::r_Error_Conversion, s.str());
      }
    }
}

r_Type* r_Convertor::get_external_type(int ctype)
{
    return r_Type::get_any_type(type_to_string(ctype).c_str());
}


template <class baseType>
void r_Convertor::applyColorScheme()
{
    baseType* data = (baseType*)const_cast<char*>(desc.src);
    baseType min = data[0], max = data[0];
    int i, size = desc.srcInterv.cell_count();
    unsigned char* t, *img = new unsigned char[size * 3];
    for (i = 1; i < size; ++i)
    {
        if (min > data[i])
        {
            min = data[i];
        }
        if (max < data[i])
        {
            max = data[i];
        }
    }
    for (i = 0, t = img; i < size; ++i)
    {
        float n = (data[i] - min) / (max - min);
        if (n < 0.5)
        {
            *t = static_cast<unsigned char>((0.5 - n) * 500);
            t++;
            *t = static_cast<unsigned char>(n * 500);
            t++;
            *t = 0;
            t++;
        }
        else
        {
            *t = 0;
            t++;
            *t = static_cast<unsigned char>((1 - n) * 500);
            t++;
            *t = static_cast<unsigned char>((n - 0.5) * 500);
            t++;
        }
    }
    destroySrc = true;
    desc.src = (char*)img;
}

convert_type_e
r_Convertor::get_internal_type(const r_Type* tp, bool fullTypes)
{
    convert_type_e retval = ctype_void;

    if (tp == NULL)
    {
        return retval;
    }

    //check if tp is structure type
    if (tp->isStructType())
    {
        // make life easy and always interpret as RGB
        // add case for structs -- DM 2011-nov-10
//        retval = ctype_rgb;
        int bands = 0;
        for (const auto& att : static_cast<const r_Structure_Type*>(tp)->getAttributes())
        {
            ++bands;
            if (att.type_of().type_id() != r_Type::CHAR || bands > 3)
            {
                return ctype_struct;
            }
        }
        if (bands != 3)
        {
            return ctype_struct;
        }
        else
        {
            return ctype_rgb;
        }
    }
    else if (tp->isComplexType())
    {
        switch (tp->type_id())
        {
        case r_Type::COMPLEXTYPE1:
            return ctype_complex1;
        case r_Type::COMPLEXTYPE2:
            return ctype_complex2;
        case r_Type::CINT16:
            return ctype_cint16;
        case r_Type::CINT32:
            return ctype_cint32;
        default:
            LERROR << "Error: in conversion: unknown type " << tp->type_id() << ", setting to void.";
            return ctype_void;
        }
    }
    else
    {
        //is primitive type
        if (fullTypes == false)
        {
            // restricted types for ``classic'' image formats
            switch (tp->type_id())
            {
            case r_Type::BOOL:
                retval = ctype_bool;
                break;
            case r_Type::CHAR:
                retval = ctype_char;
                break;
            case r_Type::OCTET:
                // fixed OCTET to be int8 instead of char -- DM 2011-nov-23
                retval = ctype_int8;
                break;
            // added (U)LONG -- PB 2005-apr-27
            case r_Type::LONG:
                retval = ctype_int32;
                break;
            case r_Type::ULONG:
                retval = ctype_uint32;
                break;
            // set to defined value (FIXME: still not good) -- PB 2005-apr-27
            default:
                LERROR << "Error: in conversion: unknown type " << tp->type_id() << ", setting to void.";
                retval = ctype_rgb;
                //retval = ctype_char;

                break;
            }
        }
        else
        {
            // full types for more generic convertors
            switch (tp->type_id())
            {
            case r_Type::BOOL:
                //FIXME that was in hdf.cc retval = ctype_uint8; break;
                retval = ctype_bool;
                break;
            case r_Type::CHAR:
                retval = ctype_uint8;
                break;
            case r_Type::OCTET:
                retval = ctype_int8;
                break;
            case r_Type::SHORT:
                retval = ctype_int16;
                break;
            case r_Type::USHORT:
                retval = ctype_uint16;
                break;
            case r_Type::LONG:
                retval = ctype_int32;
                break;
            case r_Type::ULONG:
                retval = ctype_uint32;
                break;
            case r_Type::FLOAT:
                retval = ctype_float32;
                break;
            case r_Type::DOUBLE:
                retval = ctype_float64;
                break;
            default:
                break;
            }
        }//endif fullTypes
        if (retval == ctype_void)
        {
            LWARNING << "Warning: in conversion: this type overrides base type: " << tp->type_id() << "; using char.";
            retval = ctype_char;
        }
    }//endif structuretype

    return retval;
}

void r_Convertor::updateNodataValue(const r_Range* nullValue)
{
    if (formatParams.getNodata().empty() && nullValue != NULL)
    {
        formatParams.addNodata(static_cast<double>(*nullValue));
    }
}

std::ostream& operator<<(std::ostream& os, convert_type_e& cte)
{
    switch (cte)
    {
    case ctype_bool:
        os << "bool";
        break;
    case ctype_char:
        os << "char";
        break;
    case ctype_uint8:
        os << "uint8";
        break;
    case ctype_int8:
        os << "int8";
        break;
    case ctype_int16:
        os << "int16";
        break;
    case ctype_uint16:
        os << "uint16";
        break;
    case ctype_int32:
        os << "int32";
        break;
    case ctype_uint32:
        os << "uint32";
        break;
    case ctype_int64:
        os << "int64";  // currently unsupported
        break;
    case ctype_uint64:
        os << "uint64"; // currently unsupported
        break;
    case ctype_float32:
        os << "float32";
        break;
    case ctype_float64:
        os << "float64";
        break;
    case ctype_rgb:
        os << "rgb";
        break;
    case ctype_complex1:
        os << "cfloat32";
        break;
    case ctype_complex2:
        os << "cfloat64";
        break;
    case ctype_cint16:
        os << "cint16";
        break;
    case ctype_cint32:
        os << "cint32";
        break;
    case ctype_struct:
        os << "struct";
        break;
    default:
        os  << "convert_type_e unknown type: " << cte << std::endl;
        break;
    }

    return os;
}

/*
 *  r_Convert_Memory class
 */

void r_Convert_Memory::initMemory(void)
{
    int status = -1;

    memFS = NULL;
    handle = NULL;

    memFS = new memFSContext;
    if (memFS != NULL)
    {
        handle = static_cast<void*>(memFS);
        if (memfs_initfs(handle) >= 0)
        {
            status = 0;
        }
    }
    if (status < 0)
    {
        throw r_Error(r_Error::r_Error_MemoryAllocation,
                      "cannot allocate memory for conversion");
    }
}


r_Convert_Memory::r_Convert_Memory(const char* src, const r_Minterval& interv, const r_Type* tp, int fullTypes)
    : r_Convertor(src, interv, tp, fullTypes)
{
    initMemory();
}


r_Convert_Memory::r_Convert_Memory(const char* src, const r_Minterval& interv, int type)
    : r_Convertor(src, interv, type)
{
    initMemory();
}


r_Convert_Memory::~r_Convert_Memory(void)
{
    memfs_killfs(handle);
    if (memFS != NULL)
    {
        delete memFS;
        memFS = NULL;
    }
    handle = NULL;
}
