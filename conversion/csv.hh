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
 * INCLUDE: csv.hh
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_CSV
 *
 * COMMENTS:
 *
 * Provides interface to convert data to other formats.
 *
*/

#ifndef _R_CONV_CSV_HH_
#define _R_CONV_CSV_HH_

#include "raslib/minterval.hh"
#include "raslib/type.hh"
#include "conversion/convertor.hh"
#include <ostream>
#include <sstream>

//@ManMemo: Module {\bf conversion}

/*@Doc:
  CSV convertor class.
*/
class r_Conv_CSV : public r_Convertor
{
public:
    /// constructor using an r_Type object. Exception if the type isn't atomic.
    r_Conv_CSV(const char* src, const r_Minterval& interv, const r_Type* tp);
    /// constructor using convert_type_e shortcut
    r_Conv_CSV(const char* src, const r_Minterval& interv, int tp);
    /// destructor
    ~r_Conv_CSV(void);

    /// convert to CSV
    virtual r_Conv_Desc& convertTo(const char* options = NULL,
                                   const r_Range* nullValue = NULL);
    /// convert from CSV
    virtual r_Conv_Desc& convertFrom(const char* options = NULL);
    /// convert data in a specific format to array
    using r_Convertor::convertFrom;
    virtual r_Conv_Desc& convertFrom(r_Format_Params options);
    /// cloning
    virtual r_Convertor* clone(void) const;
    /// identification
    virtual const char* get_name(void) const;
    virtual r_Data_Format get_data_format(void) const;

    static const char* FALSE;
    static const char* TRUE;

protected:

    std::string leftParen;
    std::string rightParen;
    std::string valueSeparator;
    bool outerParens;

private:
    enum Order
    {
        OUTER_INNER,
        INNER_OUTER
    };

    /// init CSV class
    void initCSV(void);

    /// logic for displaying values
    //    each method has argument "val" - pointer to the beginning of the record
    //    and returns pointer to the end of the read record
    const char* printValue(std::stringstream& f, const r_Base_Type& type, const char* val);
    const char* printStructValue(std::stringstream& f, const char* val);
    const char* printComplexValue(std::stringstream& f, const r_Base_Type& type, const char* val);
    const char* printPrimitiveValue(std::stringstream& f, const r_Base_Type& type, const char* val);
    /// logic for displaying nested arrays
    //     dims  - array describing how many elements are in each dimension
    //     offsets - array describing memory offset between values in each dimension
    //     dim   - number of dimensions
    void printArray(std::stringstream& f, int* dims, size_t* offsets, int dim, const char* val,
                    const r_Base_Type& type);

    void processEncodeOptions(const std::string& options);
    void processDecodeOptions(const std::string& options);

    /// Description of constructStruct - Construct desc.dest when the type of the array
    // is a struct type.
    //      @param numElem - number of struct elements
    //      that will be read from the csv file.
    void constructStruct(unsigned int numElem);

    /// Description of constructDest - Construct desc.dest
    //      @param type - type of the elements in the csv file.
    //      @param numElem - number of values that will be read from the csv file.
    void constructDest(const r_Base_Type& type, unsigned int numElem);

    Order order;
    std::string basetype;
    std::string domain;

    static const std::string LEFT_PAREN;
    static const std::string RIGHT_PAREN;
    static const std::string SEPARATOR;
};

/// Description of constructPrimitive - Construct desc.dest when the type of the array
// is a primitive type.
//      @param dest - points to desc.dest.
//      @param src - the content of the csv file.
//      @param numElem - number of elements that will be read from the csv file.
template<class T>
void constructPrimitive(char* dest, const char* src, unsigned int numElem, size_t srcSize);

bool isValidCharacter(char c);
size_t skipToValueBegin(const char* src, size_t srcSize, size_t srcIndex);
size_t skipToValueEnd(const char* src, size_t srcSize, size_t srcIndex);

#endif
