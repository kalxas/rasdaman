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
 * COMMENTS:
 *
 ************************************************************/

#include "qlparser/qtatomicdata.hh"
#include "relcatalogif/basetype.hh"
#include "relcatalogif/alltypes.hh"
#include "relcatalogif/typefactory.hh"
#include <logging.hh>

#include <iostream>
#include <string>
using namespace std;

QtAtomicData::QtAtomicData()
    : QtScalarData()
{
}

QtAtomicData::QtAtomicData(r_Long value, unsigned short byteLength)
    : QtScalarData()
{
    switch (byteLength)
    {
    case 1:
        valueType = TypeFactory::mapType("Octet");
        break;
    case 2:
        valueType = TypeFactory::mapType("Short");
        break;
    case 4:
        valueType = TypeFactory::mapType("Long");
        break;

    default:
        LERROR << "Error: QtAtomicData::QtAtomicData() - signed integer value with length "
               << byteLength << " is not supported.";
    }

    if (valueType)
    {
        r_Long temp = value;
        valueBuffer = new char[valueType->getSize()];
        valueType->makeFromCLong(valueBuffer, &temp);
    }
}

QtAtomicData::QtAtomicData(r_ULong value, unsigned short byteLength)
    : QtScalarData()
{
    switch (byteLength)
    {
    case 1:
        valueType = TypeFactory::mapType("Char");
        break;
    case 2:
        valueType = TypeFactory::mapType("UShort");
        break;
    case 4:
        valueType = TypeFactory::mapType("ULong");
        break;

    default:
        LERROR << "Error: QtAtomicData::QtAtomicData() - unsigned integer value with length "
               << byteLength << " is not supported.";
    }

    if (valueType)
    {
        r_ULong temp = value;
        valueBuffer = new char[valueType->getSize()];
        valueType->makeFromCULong(valueBuffer, &temp);
    }
}

QtAtomicData::QtAtomicData(bool value)
    : QtScalarData()
{
    r_ULong valueULong = static_cast<r_ULong>(value);

    valueType = TypeFactory::mapType("Bool");
    valueBuffer = new char[valueType->getSize()];
    valueType->makeFromCULong(valueBuffer, &valueULong);
}

QtAtomicData::QtAtomicData(double value, unsigned short byteLength)
    : QtScalarData()
{
    switch (byteLength)
    {
    case 4:
        valueType = TypeFactory::mapType("Float");
        break;
    case 8:
        valueType = TypeFactory::mapType("Double");
        break;

    default:
        LERROR << "Error: QtAtomicData::QtAtomicData() - float value with length "
               << byteLength << " is not supported.";
    }

    if (valueType)
    {
        valueBuffer = new char[valueType->getSize()];
        valueType->makeFromCDouble(valueBuffer, &value);
    }
}

QtAtomicData::QtAtomicData(const QtAtomicData &obj)
    : QtScalarData(obj)
{
}

QtAtomicData::~QtAtomicData()
{
}

r_ULong
QtAtomicData::getUnsignedValue() const
{
    r_ULong value = 0;

    if (valueType)
    {
        valueType->convertToCULong(valueBuffer, &value);
    }

    return value;
}

r_Long
QtAtomicData::getSignedValue() const
{
    r_Long value = 0;

    if (valueType)
    {
        valueType->convertToCLong(valueBuffer, &value);
    }

    return value;
}

double
QtAtomicData::getDoubleValue() const
{
    double value = 0;

    if (valueType)
    {
        valueType->convertToCDouble(valueBuffer, &value);
    }

    return value;
}

void QtAtomicData::printStatus(ostream &stream) const
{
    stream << "atomic, " << flush;

    QtScalarData::printStatus(stream);
}

// for complex types
QtAtomicData::QtAtomicData(double valRe, double valIm, unsigned short size)
    : QtScalarData()
{
    double dummyRe = valRe;
    double dummyIm = valIm;

    if (size == 2 * sizeof(float))
    {
        valueType = TypeFactory::mapType("Complex");
    }
    else
    {
        valueType = TypeFactory::mapType("Complexd");
    }

    valueBuffer = new char[valueType->getSize()];
    valueType->makeFromCDouble(valueBuffer + (static_cast<GenericComplexType *>(const_cast<BaseType *>(valueType)))->getReOffset(), &dummyRe);
    valueType->makeFromCDouble(valueBuffer + (static_cast<GenericComplexType *>(const_cast<BaseType *>(valueType)))->getImOffset(), &dummyIm);
}

QtAtomicData::QtAtomicData(r_Long valRe, r_Long valIm, unsigned short size)
    : QtScalarData()
{
    r_Long dummyRe = valRe;
    r_Long dummyIm = valIm;

    if (size == 2 * sizeof(r_Long))
    {
        valueType = TypeFactory::mapType("CInt32");
    }
    else
    {
        valueType = TypeFactory::mapType("CInt16");
    }

    valueBuffer = new char[valueType->getSize()];
    valueType->makeFromCLong(valueBuffer + (static_cast<GenericComplexType *>(const_cast<BaseType *>(valueType)))->getReOffset(), &dummyRe);
    valueType->makeFromCLong(valueBuffer + (static_cast<GenericComplexType *>(const_cast<BaseType *>(valueType)))->getImOffset(), &dummyIm);
}

QtAtomicData::QtAtomicData(r_ULong valRe, r_ULong valIm, unsigned short size)
    : QtScalarData()
{
    r_ULong dummyRe = valRe;
    r_ULong dummyIm = valIm;

    if (size == 2 * sizeof(r_ULong))
    {
        valueType = TypeFactory::mapType("CInt32");
    }
    else
    {
        valueType = TypeFactory::mapType("CInt16");
    }

    valueBuffer = new char[valueType->getSize()];
    valueType->makeFromCULong(valueBuffer + (static_cast<GenericComplexType *>(const_cast<BaseType *>(valueType)))->getReOffset(), &dummyRe);
    valueType->makeFromCULong(valueBuffer + (static_cast<GenericComplexType *>(const_cast<BaseType *>(valueType)))->getImOffset(), &dummyIm);
}

QtAtomicData::QtAtomicData(r_Long valRe, r_ULong valIm, unsigned short size)
    : QtScalarData()
{
    r_Long dummyRe = valRe;
    r_ULong dummyIm = valIm;
    if (size == 2 * sizeof(r_ULong))
    {
        valueType = TypeFactory::mapType("CInt32");
    }
    else
    {
        valueType = TypeFactory::mapType("CInt16");
    }

    valueBuffer = new char[valueType->getSize()];
    valueType->makeFromCLong(valueBuffer + (static_cast<GenericComplexType *>(const_cast<BaseType *>(valueType)))->getReOffset(), &dummyRe);
    valueType->makeFromCULong(valueBuffer + (static_cast<GenericComplexType *>(const_cast<BaseType *>(valueType)))->getImOffset(), &dummyIm);
}

QtAtomicData::QtAtomicData(r_ULong valRe, r_Long valIm, unsigned short size)
    : QtScalarData()
{
    r_ULong dummyRe = valRe;
    r_Long dummyIm = valIm;

    if (size == 2 * sizeof(r_ULong))
    {
        valueType = TypeFactory::mapType("CInt32");
    }
    else
    {
        valueType = TypeFactory::mapType("CInt16");
    }

    valueBuffer = new char[valueType->getSize()];
    valueType->makeFromCULong(valueBuffer + (static_cast<GenericComplexType *>(const_cast<BaseType *>(valueType)))->getReOffset(), &dummyRe);
    valueType->makeFromCLong(valueBuffer + (static_cast<GenericComplexType *>(const_cast<BaseType *>(valueType)))->getImOffset(), &dummyIm);
}
