// This is -*- C++ -*-
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

#include "basetype.hh"                // for BaseType
#include "structtype.hh"              // for StructType
#include "compositetype.hh"           // for CompositeType
#include "type.hh"                    // for Type
#include "reladminif/oidif.hh"        // for OId
#include "reladminif/dbnamedobject.hh"// for DBNamedObject
#include "reladminif/objectbroker.hh" // for ObjectBroker
#include "raslib/odmgtypes.hh"        // for STRUCT
#include "raslib/mddtypes.hh"         // for r_Bytes
#include "mymalloc/mymalloc.h"
#include <logging.hh>

#include <cstring>                    // for strlen, strcat, strcpy, strdup
#include <ostream>                    // for operator<<, basic_ostream, ostr...
#include <string>                     // for char_traits, string
#include <vector>                     // for vector

StructType::StructType()
    : StructType("unnamed structtype", 0)
{
    myType = STRUCT;
    objecttype = OId::STRUCTTYPEOID;
}

StructType::StructType(const char *newTypeName, unsigned int numElemArg)
    : CompositeType(newTypeName, 0),
      elements(numElemArg),
      elementNames(numElemArg),
      elementOffsets(numElemArg)
{
    myType = STRUCT;
    objecttype = OId::STRUCTTYPEOID;
}

StructType::StructType(const StructType &old)
    : CompositeType(old), elements{old.elements},
      elementOffsets{old.elementOffsets}, numElems{old.numElems},
      align{old.align}
{
    elementNames.reserve(numElems);
    for (unsigned int i = 0; i < numElems; ++i)
        elementNames[i] = strdup(old.elementNames[i]);
}

StructType::StructType(const OId &structtypeid)
    : CompositeType(structtypeid),
      elements(0),
      elementNames(0),
      elementOffsets(0)
{
    myType = STRUCT;
    objecttype = OId::STRUCTTYPEOID;
    readFromDb();
}

StructType::~StructType() noexcept(false)
{
    ObjectBroker::deregisterDBObject(myOId);
    validate();
    for (unsigned int i = 0; i < getNumElems(); i++)
        free(static_cast<void *>(elementNames[i]));  // it is ok because noone is using it
}

void StructType::printCell(std::ostream &stream, const char *cell) const
{
    stream << "\t|";
    for (unsigned int i = 0; i < numElems; i++)
    {
        stream << elementNames[i] << "\t: ";
        elements[i]->printCell(stream, cell + elementOffsets[i]);
        stream << "\t ";
    }
    stream << "\t| ";
}

/// generate equivalent C type names
void StructType::generateCTypeName(std::vector<const char *> &names) const
{
    for (unsigned int i = 0; i < numElems; i++)
    {
        elements[i]->generateCTypeName(names);
    }
}

void StructType::generateCTypePos(std::vector<int> &positions, int offset) const
{
    for (unsigned int i = 0; i < numElems; i++)
    {
        elements[i]->generateCTypePos(positions, offset +
                                      static_cast<int>(elementOffsets[i]));
    }
}

// liniarizes types
void StructType::getTypes(std::vector<const BaseType *> &types) const
{
    for (unsigned int i = 0; i < numElems; i++)
    {
        elements[i]->getTypes(types);
    }
}

char *StructType::getTypeStructure() const
{
    std::ostringstream ss;
    ss << "struct { ";
    for (unsigned int i = 0; i < numElems; i++)
    {
        if (i > 0)
            ss << ", ";

        char *elType = elements[i]->getTypeStructure();
        ss << elType << " " << elementNames[i];
        free(elType);
    }
    ss << " }";

    std::string result = ss.str();
    return strdup(result.c_str());
}

char *StructType::getNewTypeStructure() const
{
    std::ostringstream ss;
    ss << "(";
    for (unsigned int i = 0; i < numElems; i++)
    {
        if (i > 0)
            ss << ", ";

        char *elType = elements[i]->getTypeStructure();
        ss << elementNames[i] << " " << elType;
        free(elType);
    }
    ss << ")";

    std::string result = ss.str();
    return strdup(result.c_str());
}

unsigned int StructType::addElement(const char *elemName, const char *elemType)
{
    auto *typeByName = static_cast<BaseType *>(ObjectBroker::getObjectByName(OId::ATOMICTYPEOID, elemType));
    if (typeByName == nullptr)
        typeByName = static_cast<BaseType *>(ObjectBroker::getObjectByName(OId::STRUCTTYPEOID, elemType));

    return addElement(elemName, typeByName);
}

unsigned int StructType::addElement(const char *elemName, const BaseType *newType)
{
    if (newType && !_isPersistent)
        addElementPriv(elemName, newType);

    return numElems;
}

unsigned int StructType::addElementPriv(const char *elemName, const BaseType *newType)
{
    unsigned int currPos = 0;
    char *myElemName = strdup(elemName);

    if (numElems + 1 > elements.size())
    {
        BaseType *dummyB = nullptr;
        char *dummyN = nullptr;
        unsigned int dummyO = 0;
        elements.push_back(dummyB);
        elementNames.push_back(dummyN);
        elementOffsets.push_back(dummyO);
    }
    if (numElems == 0)
    {
        // first element
        elementOffsets[currPos] = 0;
        ++numElems;
    }
    else
    {
        // All cases have to set currPos and numElems correctly!
        // The array has to be ordered by offsets.
        if (newType->getType() == STRUCT)
        {
            //kept for potential future use
            unsigned int myAlign = static_cast<const StructType *>(newType)->getAlignment();
            if (align < myAlign)
            {
                align = myAlign;
            }
            // append at the end, align offset to 4 bytes.
            currPos = numElems;
            ++numElems;
            elementOffsets[currPos] = elementOffsets[currPos - 1] +
                                      elements[currPos - 1]->getSize();
        }
        else
        {
            if (newType->getSize() >= 4)
            {
                if (align < 4)
                {
                    align = 4;
                }
                // append at the end, align offset to 4 bytes.
                currPos = numElems;
                ++numElems;
                elementOffsets[currPos] = elementOffsets[currPos - 1] +
                                          elements[currPos - 1]->getSize();
            }
            else
            {
                if (newType->getSize() == 2)
                {
                    if (align < 2)
                    {
                        align = 2;
                    }
                    currPos = numElems;
                    numElems++;
                    elementOffsets[currPos] = elementOffsets[currPos - 1] +
                                              elements[currPos - 1]->getSize();
                }
                else
                {
                    if (newType->getSize() == 1)
                    {
                        currPos = numElems;
                        numElems++;
                        elementOffsets[currPos] = elementOffsets[currPos - 1] +
                                                  elements[currPos - 1]->getSize();
                    }
                    else
                    {
                        LTRACE << "addType() ERROR!";
                        // for debugging purposes only, shouldn't happen.
                    }
                }
            }
        }
    }
    // actually insert type and name of element
    elements[currPos] = newType;
    elementNames[currPos] = myElemName;
    // calculate current size
    calcSize();
    return numElems;
}

unsigned int StructType::getOffset(const char *elemName) const
{
    for (unsigned int i = 0; i < numElems; i++)
        if (strcmp(elementNames[i], elemName) == 0)
            return elementOffsets[i];

    LERROR << "Band '" << elemName << "' not found in struct '" << getName() << "'.";
    throw r_Error(STRUCTTYPE_ELEMENT_UNKNOWN);
}

unsigned int StructType::getOffset(unsigned int num) const
{
    if (num < numElems)
    {
        return elementOffsets[num];
    }
    else
    {
        LERROR << "Band index " << num << " is out of bounds in struct '" << getName() << "', cannot get band offset.";
        throw r_Error(STRUCTTYPE_ELEMENT_OUT_OF_BOUNDS);
    }
}

const BaseType *StructType::getElemType(const char *elemName) const
{
    for (unsigned int i = 0; i < numElems; i++)
        if (strcmp(elementNames[i], elemName) == 0)
            return elements[i];

    LERROR << "Band '" << elemName << "' not found in struct '" << getName() << "'.";
    throw r_Error(STRUCTTYPE_ELEMENT_UNKNOWN);
}

const BaseType *StructType::getElemType(unsigned int num) const
{
    if (num < numElems)
    {
        return elements[num];
    }
    else
    {
        LERROR << "Band index " << num << " is out of bounds in struct '" << getName() << "', cannot get band type.";
        throw r_Error(STRUCTTYPE_ELEMENT_OUT_OF_BOUNDS);
    }
}

const char *StructType::getElemName(unsigned int num) const
{
    if (num < numElems)
    {
        return elementNames[num];
    }
    else
    {
        LERROR << "Band index " << num << " is out of bounds in struct '" << getName() << "', cannot get band name.";
        throw r_Error(STRUCTTYPE_ELEMENT_OUT_OF_BOUNDS);
    }
}

unsigned int StructType::getNumElems() const
{
    return numElems;
}

unsigned int StructType::getAlignment() const
{
    return align;
}

int StructType::contains(const StructType *aStruct) const
{
    for (unsigned int i = 0; i < numElems; i++)
    {
        if (elements[i] == aStruct ||
            (elements[i]->getType() == STRUCT &&
             static_cast<const StructType *>(elements[i])->contains(aStruct)))
        {
            return 1;
        }
    }
    return 0;
}

int StructType::compatibleWith(const Type *aType) const
{
    int retval;
    if (aType->getType() != STRUCT)
    {
        return 0;
    }
    else
    {
        const auto *stype = static_cast<const StructType *>(aType);
        if (elements.size() != stype->elements.size())
        {
            return 0;
        }
        else
        {
            for (unsigned int i = 0; i < elements.size(); i++)
                if (!elements[i]->compatibleWith(stype->elements[i]))
                    return 0;
        }
    }
    return 1;
}

bool StructType::operator==(const Type &o) const
{
  if (o.getType() != STRUCT)
    return false;
  const auto &stype = static_cast<const StructType &>(o);
  if (getNumElems() != stype.getNumElems())
    return false;
  for (size_t i = 0; i < elements.size(); ++i)
    if (!(*getElemType(i) == *stype.getElemType(i)))
      return false;
  return true;
}

void StructType::calcSize()
{
    unsigned int tempSize = 0;
    for (unsigned int i = 0; i < numElems; i++)
        tempSize += elements[i]->getSize();

    size = tempSize;
}

r_Bytes StructType::getMemorySize() const
{
    r_Bytes retval = DBNamedObject::getMemorySize() + sizeof(int) +
                     sizeof(int) + 
                     sizeof(std::vector<BaseType *>) +
                     sizeof(std::vector<unsigned int>) +
                     sizeof(std::vector<char *>) + 
                     sizeof(int) * numElems +
                     sizeof(BaseType *) * numElems;
    for (unsigned int i = 0; i < numElems; i++)
        retval = retval + 1 + strlen(elementNames[i]);

    return retval;
}
