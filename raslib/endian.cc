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
 * INCLUDE: endian.cc
 *
 * MODULE:  raslib
 * CLASS:   r_Endian
 *
 * COMMENTS:
 *      None
*/

#include "config.h"
#include <string.h>


#include "raslib/endian.hh"
#include "raslib/minterval.hh"
#include "raslib/primitivetype.hh"
#include "raslib/structuretype.hh"
#include "raslib/miter.hh"

#include <logging.hh>


/*
 *  Inline code used in several places in the r_Endian class
 */

static inline r_Octet eswap(r_Octet val)
{
    return val;
}

static inline void eswap(r_Octet val, void *dest)
{
    r_Char *d = static_cast<r_Char *>(dest);
    *d = static_cast<r_Char>(val);
}

static inline r_Short eswap(r_Short val)
{
    return static_cast<r_Short>(((val & 0xff) << 8) | ((val >> 8) & 0xff));
}

static inline r_UShort eswap(r_UShort val)
{
    return static_cast<r_UShort>(((val & 0xff) << 8) | ((val >> 8) & 0xff));
}

static inline void eswap(r_Short val, void *dest)
{
    r_Short *d = static_cast<r_Short *>(dest);
    *d = eswap(val);
}

static inline void eswap(r_UShort val, void *dest)
{
    r_UShort *d = static_cast<r_UShort *>(dest);
    *d = eswap(val);
}

static inline r_Long eswap(r_Long val)
{
    return static_cast<r_Long>(((val & 0xff) << 24) | ((val & 0xff00) << 8) |
                               ((val >> 8) & 0xff00) | ((val >> 24) & 0xff));
}

static inline r_ULong eswap(r_ULong val)
{
    return static_cast<r_ULong>(((val & 0xff) << 24) | ((val & 0xff00) << 8) |
                                ((val >> 8) & 0xff00) | ((val >> 24) & 0xff));
}

static inline void eswap(r_Long val, void *dest)
{
    r_Long *d = static_cast<r_Long *>(dest);
    *d = eswap(val);
}

static inline void eswap(r_ULong val, void *dest)
{
    r_ULong *d = static_cast<r_ULong *>(dest);
    *d = eswap(val);
}

/*
these types are not supported
static inline long eswap( long val )
{
  return (long)(((val & 0xff) << 24) | ((val & 0xff00) << 8) |
        ((val >> 8) & 0xff00) | ((val >> 24) & 0xff));
}

static inline r_ULong eswap( r_ULong val )
{
  return (r_ULong)(((val & 0xff) << 24) | ((val & 0xff00) << 8) |
        ((val >> 8) & 0xff00) | ((val >> 24) & 0xff));
}

static inline void eswap( long val, void *dest )
{
  long *d = (long *)dest;
  *d = eswap(val);
}

static inline void eswap( r_ULong val, void *dest )
{
  r_ULong *d = (r_ULong *)dest;
  *d = eswap(val);
}
*/

static inline r_Float eswap(r_Float val)
{
    r_Long *ptr = (r_Long *)&val;
    r_Float result;

    eswap(*ptr, &result);

    return result;
}

static inline void eswap(r_Float val, void *dest)
{
    r_Long *ptr = (r_Long *)&val;

    eswap(*ptr, dest);
}


#ifndef __GNUG__
inline
#endif
static r_Double eswap(r_Double val)
{
    r_Long *ptr = (r_Long *)&val;
    r_Double result;
    eswap(ptr[0], ((r_Octet *)&result) + sizeof(r_Long));
    eswap(ptr[1], (r_Octet *)&result);
    /*
    #ifndef DECALPHA
      eswap(ptr[0], ((r_Octet*)&result)+sizeof(long));
      eswap(ptr[1], (r_Octet*)&result);
    #else
      eswap(*ptr, &result);
    #endif
    */
    return result;
}

#ifndef __GNUG__
inline
#endif
static void eswap(r_Double val, void *dest)
{
    r_Long *ptr = (r_Long *)&val;
    eswap(ptr[0], (static_cast<r_Octet *>(dest)) + sizeof(r_Long));
    eswap(ptr[1], dest);
    /*
    #ifndef DECALPHA
      eswap(ptr[0], ((r_Octet*)dest)+sizeof(long));
      eswap(ptr[1], dest);
    #else
      eswap(*ptr, dest);
    #endif
    */
}


/*
 *  Template functions used throughout the code
 */

// template for special linear iteration
template<class T>
void swap_array_templ(r_Bytes size, T *dest, const T *src)
{
    const T *sptr = src;
    T *dptr = dest;
    r_Bytes ctr;

    for (ctr = 0; ctr < size; ctr += sizeof(T), sptr++, dptr++)
    {
        eswap(*sptr, dptr);
    }
}


// template for identical domains for src and dest
template<class T>
void swap_array_templ(r_Miter &iter, T *destBase, const T *srcBase)
{
    while (!iter.isDone())
    {
        const T *src = (const T *)iter.nextCell();
        eswap(*src, destBase + (src - srcBase));
    }
}


// template for generic iteration (src is just a dummy here)
template<class T>
void swap_array_templ(r_Miter &siter, r_Miter &diter, __attribute__((unused)) const T *srcBase)
{
    while (!siter.isDone())
    {
        const T *src = (const T *)siter.nextCell();
        T *dest = (T *)diter.nextCell();
        eswap(*src, dest);
    }
}

// force the instantiations; we only need the templates r_Longernally anyway
template void swap_array_templ(r_Bytes, r_Short *, const r_Short *);
template void swap_array_templ(r_Bytes, r_UShort *, const r_UShort *);
template void swap_array_templ(r_Bytes, r_Long *, const r_Long *);
template void swap_array_templ(r_Bytes, r_ULong *, const r_ULong *);
/*
template void swap_array_templ(r_Bytes, long *, const long *);
template void swap_array_templ(r_Bytes, r_ULong *, const r_ULong *);
*/
template void swap_array_templ(r_Bytes, r_Float *, const r_Float *);
template void swap_array_templ(r_Bytes, r_Double *, const r_Double *);
template void swap_array_templ(r_Miter &, r_Octet *, const r_Octet *);
template void swap_array_templ(r_Miter &, r_Short *, const r_Short *);
template void swap_array_templ(r_Miter &, r_Long *, const r_Long *);
template void swap_array_templ(r_Miter &, r_Float *, const r_Float *);
template void swap_array_templ(r_Miter &, r_Double *, const r_Double *);
template void swap_array_templ(r_Miter &, r_Miter &, const r_Octet *);
template void swap_array_templ(r_Miter &, r_Miter &, const r_Short *);
template void swap_array_templ(r_Miter &, r_Miter &, const r_Long *);
template void swap_array_templ(r_Miter &, r_Miter &, const r_Float *);
template void swap_array_templ(r_Miter &, r_Miter &, const r_Double *);





/*
 *  r_Endian members
 */

r_Short r_Endian::swap(r_Short val)
{
    return eswap(val);
}

r_UShort r_Endian::swap(r_UShort val)
{
    return eswap(val);
}

r_Long r_Endian::swap(r_Long val)
{
    return eswap(val);
}

r_ULong r_Endian::swap(r_ULong val)
{
    return eswap(val);
}

r_Float r_Endian::swap(r_Float val)
{
    return eswap(val);
}

r_Double r_Endian::swap(r_Double val)
{
    return eswap(val);
}


r_Endian::r_Endianness r_Endian::get_endianness(void)
{
#ifdef IS_LITTLE_ENDIAN
    return r_Endian_Little;
#else
    return r_Endian_Big;
#endif
}



/*
 *  Simplest array swapping: linear buffer filled with atomic type
 */

void r_Endian::swap_array(const r_Primitive_Type *type, r_Bytes size, const void *src, void *dest)
{
    switch (type->type_id())
    {
    case r_Primitive_Type::BOOL:
    case r_Primitive_Type::CHAR:
    case r_Primitive_Type::OCTET:
        if (src != static_cast<const void *>(dest))
        {
            // change of endianness on 1 byte data is a NULL operation.
            // if src and dest differ, we have to _copy_, though.
            memcpy(dest, src, size);
        }
        break;
    case r_Primitive_Type::SHORT:
        swap_array_templ(size, static_cast<r_Short *>(dest), static_cast<const r_Short *>(src));
        break;
    case r_Primitive_Type::USHORT:
        swap_array_templ(size, static_cast<r_UShort *>(dest), static_cast<const r_UShort *>(src));
        break;
    case r_Primitive_Type::LONG:
        swap_array_templ(size, static_cast<r_Long *>(dest), static_cast<const r_Long *>(src));
        break;
    case r_Primitive_Type::ULONG:
        swap_array_templ(size, static_cast<r_ULong *>(dest), static_cast<const r_ULong *>(src));
        break;
    case r_Primitive_Type::FLOAT:
    case r_Primitive_Type::COMPLEXTYPE1:
        swap_array_templ(size, static_cast<r_Float *>(dest), static_cast<const r_Float *>(src));
        break;
    case r_Primitive_Type::DOUBLE:
    case r_Primitive_Type::COMPLEXTYPE2:
        swap_array_templ(size, static_cast<r_Double *>(dest), static_cast<const r_Double *>(src));
        break;
    default:
        LTRACE << "swap_array(type, size, src, dest) bad typeId " << type->type_id();
        break;
    }
}


/*
 *  Same functionality as function above, but with the types given implicitly
 *  by the parameters
 */

// Dummies, but useful when templates are used
void r_Endian::swap_array(__attribute__((unused)) r_Bytes size, __attribute__((unused)) const r_Octet *src, __attribute__((unused)) r_Octet *dest)
{
}

void r_Endian::swap_array(__attribute__((unused)) r_Bytes size, __attribute__((unused)) const r_Char *src, __attribute__((unused)) r_Char *dest)
{
}

// Here go the real ones...
void r_Endian::swap_array(r_Bytes size, const r_Short *src, r_Short *dest)
{
    swap_array_templ(size, dest, src);
}

void r_Endian::swap_array(r_Bytes size, const r_UShort *src, r_UShort *dest)
{
    swap_array_templ(size, dest, src);
}

void r_Endian::swap_array(r_Bytes size, const r_Long *src, r_Long *dest)
{
    swap_array_templ(size, dest, src);
}

void r_Endian::swap_array(r_Bytes size, const r_ULong *src, r_ULong *dest)
{
    swap_array_templ(size, dest, src);
}
/*
void r_Endian::swap_array( r_Bytes size, const long *src, long *dest )
{
  swap_array_templ(size, dest, src);
}

void r_Endian::swap_array( r_Bytes size, const r_ULong *src, r_ULong *dest )
{
  swap_array_templ(size, dest, src);
}
*/
void r_Endian::swap_array(r_Bytes size, const r_Float *src, r_Float *dest)
{
    swap_array_templ(size, dest, src);
}

void r_Endian::swap_array(r_Bytes size, const r_Double *src, r_Double *dest)
{
    swap_array_templ(size, dest, src);
}


/*
 *  Same functionality as above, but with the type size given as parameter
 */

void r_Endian::swap_array(r_Bytes size, r_Bytes tsize, const void *src, void *dest)
{
    switch (tsize)
    {
    case 2:
        swap_array_templ(size, static_cast<r_Short *>(dest), static_cast<const r_Short *>(src));
        break;
    case 4:
        swap_array_templ(size, static_cast<r_Long *>(dest), static_cast<const r_Long *>(src));
        break;
    case 8:
    case 16:    // complexd
        swap_array_templ(size, static_cast<r_Double *>(dest), static_cast<const r_Double *>(src));
        break;
    default:
        break;
    }
}



/*
 *  ``Half-generic'': array with arbitrary total domain and iteration
 *  domain, but same domains for src and dest
 */
void r_Endian::swap_array(const r_Primitive_Type *type, const r_Minterval &srcDom, const r_Minterval &srcIterDom, const void *src, void *dest, r_ULong step)
{
    if ((srcDom == srcIterDom) && (step == type->size()))
    {
        r_ULong size = step * srcDom.cell_count();

        swap_array(type, size, src, dest);
    }
    else
    {
        r_Miter iter(&srcIterDom, &srcDom, step, static_cast<const char *>(src));

        switch (type->type_id())
        {
        case r_Primitive_Type::BOOL:
        case r_Primitive_Type::CHAR:
        case r_Primitive_Type::OCTET:
            if (src != static_cast<const void *>(dest))
            {
                swap_array_templ(iter, static_cast<r_Octet *>(dest), static_cast<const r_Octet *>(src));
            }
            break;
        case r_Primitive_Type::SHORT:
            swap_array_templ(iter, static_cast<r_Short *>(dest), static_cast<const r_Short *>(src));
            break;
        case r_Primitive_Type::USHORT:
            swap_array_templ(iter, static_cast<r_UShort *>(dest), static_cast<const r_UShort *>(src));
            break;
        case r_Primitive_Type::LONG:
            swap_array_templ(iter, static_cast<r_Long *>(dest), static_cast<const r_Long *>(src));
            break;
        case r_Primitive_Type::ULONG:
            swap_array_templ(iter, static_cast<r_ULong *>(dest), static_cast<const r_ULong *>(src));
            break;
        case r_Primitive_Type::FLOAT:
        case r_Primitive_Type::COMPLEXTYPE1:
            swap_array_templ(iter, static_cast<r_Float *>(dest), static_cast<const r_Float *>(src));
            break;
        case r_Primitive_Type::DOUBLE:
        case r_Primitive_Type::COMPLEXTYPE2:
            swap_array_templ(iter, static_cast<r_Double *>(dest), static_cast<const r_Double *>(src));
            break;
        default:
            LTRACE << "swap_array(type, srcdom, srciterdom, src,  dest, step) bad typeId " << type->type_id();
            break;
        }
    }
}


static void swap_array_struct(const r_Structure_Type *structType, const r_Minterval &srcDom, const r_Minterval &srcIterDom, const void *src, void *dest, r_ULong step)
{
    r_Structure_Type::attribute_iterator iter(structType->defines_attribute_begin());

    while (iter != structType->defines_attribute_end())
    {
        r_Type *newType = (*iter).type_of().clone();
        const void *srcPtr = static_cast<const void *>((static_cast<const r_Octet *>(src)) + (*iter).offset());
        void *destPtr = static_cast<void *>((static_cast<r_Octet *>(dest)) + (*iter).offset());
        if (newType->isStructType())
        {
            swap_array_struct(static_cast<const r_Structure_Type *>(newType), srcDom, srcIterDom, srcPtr, destPtr, step);
        }
        else
        {
            r_Endian::swap_array(static_cast<const r_Primitive_Type *>(newType), srcDom, srcIterDom, srcPtr, destPtr, step);
        }
        delete newType;
        iter++;
    }
}


void r_Endian::swap_array(const r_Base_Type *type, const r_Minterval &srcDom, const r_Minterval &srcIterDom, const void *src, void *dest)
{
    if (type->isStructType())
    {
        swap_array_struct(static_cast<const r_Structure_Type *>(type), srcDom, srcIterDom, src, dest, type->size());
    }
    else
    {
        swap_array(static_cast<const r_Primitive_Type *>(type), srcDom, srcIterDom, src, dest, type->size());
    }
}




/*
 *  Fully generic: different total domain and iteration domain for both
 *  src and dest. Beware that the number of cells in the iteration domains
 *  for src and dest must be identical!
 */

void r_Endian::swap_array(const r_Primitive_Type *type, const r_Minterval &srcDom, const r_Minterval &srcIterDom, const r_Minterval &destDom, const r_Minterval &destIterDom, const void *src, void *dest, r_ULong step)
{
    /// check if the whole thing reduces to a linear scan
    if ((srcDom == srcIterDom) && (step == type->size()))
    {
        r_ULong size = step * srcDom.cell_count();

        swap_array(type, size, src, dest);
    }
    /// no, generic code...
    else
    {
        r_Miter siter(&srcIterDom, &srcDom, step, static_cast<const char *>(src));
        r_Miter diter(&destIterDom, &destDom, step, static_cast<const char *>(dest));

        switch (type->type_id())
        {
        case r_Primitive_Type::BOOL:
        case r_Primitive_Type::CHAR:
        case r_Primitive_Type::OCTET:
            if (src != static_cast<const void *>(dest))
            {
                swap_array_templ(siter, diter, static_cast<const r_Octet *>(src));
            }
            break;
        case r_Primitive_Type::SHORT:
            swap_array_templ(siter, diter, static_cast<const r_Short *>(src));
            break;
        case r_Primitive_Type::USHORT:
            swap_array_templ(siter, diter, static_cast<const r_UShort *>(src));
            break;
        case r_Primitive_Type::LONG:
            swap_array_templ(siter, diter, static_cast<const r_Long *>(src));
            break;
        case r_Primitive_Type::ULONG:
            swap_array_templ(siter, diter, static_cast<const r_ULong *>(src));
            break;
        case r_Primitive_Type::FLOAT:
        case r_Primitive_Type::COMPLEXTYPE1:
            swap_array_templ(siter, diter, static_cast<const r_Float *>(src));
            break;
        case r_Primitive_Type::DOUBLE:
        case r_Primitive_Type::COMPLEXTYPE2:
            swap_array_templ(siter, diter, static_cast<const r_Double *>(src));
            break;
        default:
            LTRACE << "swap_array(type, srcdom, srciterdom, destdom, destiterdom, src,  dest, step) bad typeId " << type->type_id();
            break;
        }
    }
}


static void swap_array_struct(const r_Structure_Type *structType, const r_Minterval &srcDom, const r_Minterval &srcIterDom, const r_Minterval &destDom, const r_Minterval &destIterDom, const void *src, void *dest, r_ULong step)
{
    r_Structure_Type::attribute_iterator iter(structType->defines_attribute_begin());

    while (iter != structType->defines_attribute_end())
    {
        r_Type *newType = (*iter).type_of().clone();
        const void *srcPtr = static_cast<const void *>((static_cast<const r_Octet *>(src)) + (*iter).offset());
        void *destPtr = static_cast<void *>((static_cast<r_Octet *>(dest)) + (*iter).offset());
        if (newType->isStructType())
        {
            swap_array_struct(static_cast<const r_Structure_Type *>(newType), srcDom, srcIterDom, destDom, destIterDom, srcPtr, destPtr, step);
        }
        else
        {
            r_Endian::swap_array(static_cast<const r_Primitive_Type *>(newType), srcDom, srcIterDom, destDom, destIterDom, srcPtr, destPtr, step);
        }
        delete newType;
        iter++;
    }
}

void r_Endian::swap_array(const r_Base_Type *type, const r_Minterval &srcDom, const r_Minterval &srcIterDom, const r_Minterval &destDom, const r_Minterval &destIterDom, const void *src, void *dest)
{
    if (type->isStructType())
    {
        swap_array_struct(static_cast<const r_Structure_Type *>(type), srcDom, srcIterDom, destDom, destIterDom, src, dest, type->size());
    }
    else
    {
        swap_array(static_cast<const r_Primitive_Type *>(type), srcDom, srcIterDom, destDom, destIterDom, src, dest, type->size());
    }
}

std::ostream &operator<<(std::ostream &s, r_Endian::r_Endianness &e)
{
    switch (e)
    {
    case r_Endian::r_Endian_Little:
        s << "Little_Endian";
        break;
    case r_Endian::r_Endian_Big:
        s << "Big_Endian";
        break;
    default:
        s << "Unkown r_Endiannes";
        break;
    }
    return s;
}
