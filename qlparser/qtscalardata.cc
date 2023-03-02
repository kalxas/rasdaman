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

#include "qlparser/qtscalardata.hh"

#include "relcatalogif/basetype.hh"

#include "raslib/rmdebug.hh"
#include <logging.hh>

#include <iostream>
#include <string>
#include <cstring>
#include <cstdlib>
using namespace std;

// deprecated, not available any longer -- PB 2005-jan-14
// #include <strstream.h>

QtScalarData::QtScalarData()
    : valueType(NULL),
      valueBuffer(NULL),
      ownCells(true)
{
}

QtScalarData::QtScalarData(const QtScalarData &obj)
    : QtData(obj),
      ownCells(true)
{
    setLifetime(obj.getLifetime());

    // Pointer received from TypeFactory just can be copied.
    valueType = obj.valueType;

    if (valueType && obj.valueBuffer)
    {
        valueBuffer = new char[valueType->getSize()];
        memcpy(valueBuffer, obj.valueBuffer, valueType->getSize());
    }
    else
    {
        valueBuffer = NULL;
    }
}

QtScalarData::~QtScalarData()
{
    // valueType is not deleted because it is maintained by the typeFactory
    if (ownCells)
    {
        if (valueBuffer)
        {
            delete[] valueBuffer;
            valueBuffer = NULL;
        }
    }
}

QtDataType
QtScalarData::getDataType() const
{
    QtDataType returnValue = QT_BOOL;

    if (valueType)
        switch (valueType->getType())
        {
        case BOOLTYPE:
            returnValue = QT_BOOL;
            break;
        case CHAR:
            returnValue = QT_CHAR;
            break;
        case OCTET:
            returnValue = QT_OCTET;
            break;
        case USHORT:
            returnValue = QT_USHORT;
            break;
        case SHORT:
            returnValue = QT_SHORT;
            break;
        case ULONG:
            returnValue = QT_ULONG;
            break;
        case LONG:
            returnValue = QT_LONG;
            break;
        case FLOAT:
            returnValue = QT_FLOAT;
            break;
        case DOUBLE:
            returnValue = QT_DOUBLE;
            break;
        case COMPLEXTYPE1:
            returnValue = QT_COMPLEXTYPE1;
            break;
        case COMPLEXTYPE2:
            returnValue = QT_COMPLEXTYPE2;
            break;
        case CINT16:
            returnValue = QT_CINT16;
            break;
        case CINT32:
            returnValue = QT_CINT32;
            break;
        case STRUCT:
            returnValue = QT_COMPLEX;
            break;
        default:
            LTRACE << "getDataType() bad type " << valueType->getType();
            break;
        }

    return returnValue;
}

bool QtScalarData::isScalarData() const
{
    return true;
}

bool QtScalarData::equal(const QtData *obj) const
{
    int returnValue = false;  // not equal by initialization

    if (obj->isScalarData())
    {
        QtScalarData *scalarObj = static_cast<QtScalarData *>(const_cast<QtData *>(obj));

        if (getDataType() == scalarObj->getDataType())  // Attention: this is not correct for structs
            // compare value buffers
        {
            returnValue = (memcmp(valueBuffer, scalarObj->valueBuffer, valueType->getSize()) == 0);
        }
    }

    return returnValue;
}

string
QtScalarData::getSpelling() const
{
    string result;

    /*// buffer for hex representation of chars
    int        bufferLen = valueType->getSize() * 2 + 1  + 2; // added final "+2" to remove memleak -- PB 2006-aug-17
    char*      buffer    = new char[ bufferLen ];
    // replaced deprecated ostrstream -- PB 2005-jan-14
    // ostrstream bufferStream( buffer, bufferLen );
    ostringstream bufferStream( buffer );

    for( int i=0; i<valueType->getSize(); i++ )
        bufferStream << hex << valueBuffer[i];

    bufferStream << ends;

    result.append( string( buffer ) );*/

    ostringstream stream;
    if (valueType)
    {
        valueType->printCell(stream, valueBuffer);
    }
    else
    {
        for (unsigned int i = 0; i < valueType->getSize(); i++)
        {
            stream << hex << valueBuffer[i];
        }
        stream << ends;
    }
    result.append(stream.str());

    /*delete[] buffer;
    buffer = NULL;*/
    return result;
}

char *QtScalarData::getTypeStructure() const
{
    if (valueType)
    {
        return strdup(valueType->getTypeStructure().c_str());
    }
    else
    {
        return NULL;
    }
}

void QtScalarData::printStatus(ostream &stream) const
{
    if (valueType)
    {
        auto typeStructure = valueType->getTypeStructure();

        stream << "type: " << flush << valueType->getTypeName()
               << ", structure: " << flush << typeStructure
               << ", value: " << flush;

        valueType->printCell(stream, valueBuffer);

        QtData::printStatus(stream);
    }
    else
    {
        stream << "<no type>";
    }
}
