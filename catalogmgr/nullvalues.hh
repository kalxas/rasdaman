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
  * INCLUDE: nullvalues.hh
 *
 * MODULE:  rasodmg
 * CLASS:   NullValuesHandler
 *
 * COMMENTS:
 *      None
*/

#ifndef _NULLVALUESHANDLER_HH_
#define _NULLVALUESHANDLER_HH_

#include "typeenum.hh"
#include "raslib/nullvalues.hh"
#include "raslib/odmgtypes.hh"

class BaseType;

//@ManMemo: Module: {\bf rasodmg}

/*@Doc:

 This class enables handling of null values in MDDs.

*/
class NullValuesHandler
{
public:
    NullValuesHandler();

    NullValuesHandler(r_Nullvalues *nullValues);

    virtual ~NullValuesHandler() noexcept(false);

    /// clone data from obj to this object
    void cloneNullValues(const NullValuesHandler *obj);

    /// get null values
    r_Nullvalues *getNullValues() const;

    /// get the first valid null value (fixed lower/upper bound)
    r_Double getNullValue() const;

    /// set null values
    virtual void setNullValues(r_Nullvalues *newNullValues);
    /// get the number of null values
    unsigned long getNullValuesCount() const;

    /// set the number of null values
    void setNullValuesCount(unsigned long count);
    /// make union of two nullvalues vectors
    r_Nullvalues *unionNullValues(r_Nullvalues *nullValues1, r_Nullvalues *nullValues2);

    /*
     * Functions to check if a value is null. If it is the null values counter
     * is increased automatically.
     */

    inline bool isNull(r_Char value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_Char>(value))
        {
            incCount();
            return true;
        }
        return false;
    }

    inline bool isNull(r_Octet value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_Octet>(value))
        {
            incCount();
            return true;
        }
        return false;
    }

    inline bool isNull(r_Short value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_Short>(value))
        {
            incCount();
            return true;
        }
        return false;
    }

    inline bool isNull(r_UShort value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_UShort>(value))
        {
            incCount();
            return true;
        }
        return false;
    }

    inline bool isNull(r_Long value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_Long>(value))
        {
            incCount();
            return true;
        }
        return false;
    }

    inline bool isNull(r_ULong value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_ULong>(value))
        {
            incCount();
            return true;
        }
        return false;
    }

    inline bool isNull(float value)
    {
        if (nullValues != NULL && nullValues->isNullFloat<float>(value))
        {
            incCount();
            return true;
        }
        return false;
    }

    inline bool isNull(double value)
    {
        if (nullValues != NULL && nullValues->isNullFloat<double>(value))
        {
            incCount();
            return true;
        }
        return false;
    }

    /*
     * Functions to check if a value is null. This does not increase the null
     * value counter.
     */

    inline bool isNullOnly(r_Char value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_Char>(value))
        {
            return true;
        }
        return false;
    }

    inline bool isNullOnly(r_Octet value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_Octet>(value))
        {
            return true;
        }
        return false;
    }

    inline bool isNullOnly(r_Short value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_Short>(value))
        {
            return true;
        }
        return false;
    }

    inline bool isNullOnly(r_UShort value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_UShort>(value))
        {
            return true;
        }
        return false;
    }

    inline bool isNullOnly(r_Long value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_Long>(value))
        {
            return true;
        }
        return false;
    }

    inline bool isNullOnly(r_ULong value)
    {
        if (nullValues != NULL && nullValues->isNullNonFloat<r_ULong>(value))
        {
            return true;
        }
        return false;
    }

    inline bool isNullOnly(float value)
    {
        if (nullValues != NULL && nullValues->isNullFloat<float>(value))
        {
            return true;
        }
        return false;
    }

    inline bool isNullOnly(double value)
    {
        if (nullValues != NULL && nullValues->isNullFloat<double>(value))
        {
            return true;
        }
        return false;
    }

    /// given a tile and total # cells in that tile = cellCount, this method
    /// initializes the entire tile with the first null value if any is available;
    /// otherwise nothing is done.
    void fillTileWithNullvalues(char *resDataPtr, size_t cellCount, const BaseType *cellType) const;

protected:
    /// given a tile and total # cells in that tile = cellCount, this method
    /// initializes the entire tile with the first null value if any is available;
    /// otherwise nothing is done.
    void fillMultibandTileWithNullvalues(char *resDataPtr, size_t cellCount, const BaseType *cellType) const;

    /// given a tile and total # cells in that tile = cellCount, this method
    /// initializes the entire tile with the first null value if any is available;
    /// otherwise nothing is done.
    void fillSinglebandTileWithNullvalues(char *resDataPtr, size_t cellCount, TypeEnum cellType) const;

    /// increase null values count by one
    inline void incCount()
    {
        ++nullValuesCount;
    }

    /// null values
    r_Nullvalues *nullValues;

    /// count of null values
    unsigned long nullValuesCount;
};

#endif
