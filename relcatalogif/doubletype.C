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

#include "atomictype.hh"  // for AtomicType
#include "doubletype.hh"
#include "raslib/odmgtypes.hh"   // for DOUBLE
#include "reladminif/oidif.hh"    // for OId
#include <iomanip>
#include <limits>

DoubleType::DoubleType(const OId &id) : RealType(id)
{
    readFromDb();
}

/*************************************************************
 * Method name...: DoubleType();
 *
 * Arguments.....: none
 * Return value..: none
 * Description...: initializes member variables for an
 *                 DoubleType.
 ************************************************************/

DoubleType::DoubleType() : RealType(DoubleType::Name, 8)
{
    myType = DOUBLE;
    myOId = OId(DOUBLE, OId::ATOMICTYPEOID);
}

/*************************************************************
 * Method name...: DoubleType(const DoubleType& old);
 *
 * Arguments.....: none
 * Return value..: none
 * Description...: copy constructor
 ************************************************************/

DoubleType::DoubleType(const DoubleType &old)  = default;

/*************************************************************
 * Method name...: operator=(const DoubleType&);
 *
 * Arguments.....: none
 * Return value..: none
 * Description...: copy constructor
 ************************************************************/

DoubleType &DoubleType::operator=(const DoubleType &old)
{
    // Gracefully handle self assignment
    if (this == &old)
    {
        return *this;
    }
    AtomicType::operator=(old);
    return *this;
}

/*************************************************************
 * Method name...: ~DoubleType();
 *
 * Arguments.....: none
 * Return value..: none
 * Description...: virtual destructor
 ************************************************************/

DoubleType::~DoubleType() = default;

void DoubleType::readFromDb()
{
    size = 8;
    setName(DoubleType::Name);
    myType = DOUBLE;
    myOId = OId(DOUBLE, OId::ATOMICTYPEOID);
}

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
 *                 Assumes that Double is stored MSB..LSB
 *                 on HP.
 ************************************************************/

void DoubleType::printCell(std::ostream &stream, const char *cell) const
{
    // !!!! HP specific, assumes 4 Byte double and MSB..LSB
    // byte order
    stream << std::setprecision(std::numeric_limits<double>::digits10 + 1) << *reinterpret_cast<const double *>(cell);
}

double *DoubleType::convertToCDouble(const char *cell, double *value) const
{
    // !!!! HP specific, assumes 8 Byte double
    // byte order
    *value = *reinterpret_cast<const double *>(cell);
    return value;
}

char *DoubleType::makeFromCDouble(char *cell, const double *value) const
{
    // !!!! HP specific, assumes 4 Byte double and MSB..LSB
    // byte order
    *reinterpret_cast<double *>(cell) = *value;
    return cell;
}
