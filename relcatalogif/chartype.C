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
/*************************************************************
 *
 *
 * PURPOSE:
 *   uses ODMG-conformant O2 classes
 *
 *
 * COMMENTS:
 *   none
 *
 ************************************************************/

#include "chartype.hh"
#include "reladminif/oidif.hh"       // for OId, OId::ATOMICTYPEOID
#include <limits.h>       // for UCHAR_MAX
#include <iomanip>        // for operator<<, setw

/*************************************************************
 * Method name...: CharType();
 *
 * Arguments.....: none
 * Return value..: none
 * Description...: initializes member variables for an
 *                 CharType.
 ************************************************************/

CharType::CharType() : UIntegralType(CharType::Name, 1)
{
    myType = CHAR;
    myOId = OId(CHAR, OId::ATOMICTYPEOID);
}

/*************************************************************
 * Method name...: CharType(const CharType& old);
 *
 * Arguments.....: none
 * Return value..: none
 * Description...: copy constructor
 ************************************************************/

CharType::CharType(const CharType &old)
    : UIntegralType(CharType::Name, old.size) {}

CharType::CharType(__attribute__((unused)) const OId &id)
    : UIntegralType(OId(CHAR, OId::ATOMICTYPEOID))
{
    readFromDb();
}

/*************************************************************
 * Method name...: operator=(const CharType&);
 *
 * Arguments.....: none
 * Return value..: none
 * Description...: copy constructor
 ************************************************************/

CharType &CharType::operator=(const CharType &old)
{
    // Gracefully handle self assignment
    if (this == &old)
    {
        return *this;
    }
    AtomicType::operator=(old);
    return *this;
}

void CharType::readFromDb()
{
    setName(CharType::Name);
    myType = CHAR;
    myOId = OId(CHAR, OId::ATOMICTYPEOID);
    size = 1;
}

/*************************************************************
 * Method name...: ~CharType();
 *
 * Arguments.....: none
 * Return value..: none
 * Description...: virtual destructor
 ************************************************************/

CharType::~CharType() = default;

/*************************************************************
 * Method name...: void printCell( ostream& stream,
 *                                 const char* cell )
 *
 * Arguments.....:
 *   stream: stream to print on
 *   cell:   pointer to cell to print
 * Return value..: none
 * Description...: prints a cell cell in hex on stream
 *                 followed by a space.
 *                 Assumes that Char is stored MSB..LSB
 *                 on HP.
 ************************************************************/

void CharType::printCell(std::ostream &stream, const char *cell) const
{
    // !!!! HP specific, assumes 1 Byte char
    stream << std::setw(4) << (r_Long)(*(unsigned char *)const_cast<char *>(cell));
}

r_ULong *CharType::convertToCULong(const char *cell, r_ULong *value) const
{
    // !!!! HP specific, assumes 4 Byte long and MSB..LSB
    // byte order
    *value = *(unsigned char *)const_cast<char *>(cell);
    return value;
}

char *CharType::makeFromCULong(char *cell, const r_ULong *value) const
{
    r_ULong myLong = *value;
    // restricting long to value range of short
    myLong = myLong > UCHAR_MAX ? UCHAR_MAX : myLong;
    // !!!! HP specific, assumes 4 Byte long and MSB..LSB
    // byte order
    *(unsigned char *)(cell) = (unsigned char)myLong;
    return cell;
}
