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
/*************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 *  code common to all database interface implementations
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

#include <stdlib.h>                   // for free, malloc
#include <cstring>                    // for strlen, strcat, strcpy, strdup
#include <ostream>                    // for operator<<, basic_ostream, ostr...
#include <string>                     // for char_traits, string
#include <vector>                     // for vector

/*************************************************************
 * Method name...: StructType();
 *
 * Arguments.....: none
 * Return value..: none
 * Description...: initializes member variables for an
 *                 StructType.
 ************************************************************/

r_Bytes StructType::getMemorySize() const
{
    r_Bytes retval = DBNamedObject::getMemorySize() + sizeof(int) +
                     sizeof(int) + sizeof(std::vector<BaseType *>) +
                     sizeof(std::vector<unsigned int>) +
                     sizeof(std::vector<char *>) + sizeof(int) * numElems +
                     sizeof(BaseType *) * numElems;
    for (unsigned int i = 0; i < numElems; i++)
    {
        retval = retval + 1 + strlen(elementNames[i]);
    }
    return retval;
}

StructType::StructType()
    : CompositeType("unnamed structtype", 0),
      elements(0),
      elementNames(0),
      elementOffsets(0),
      numElems(0),
      align(1)
{
    myType = STRUCT;
    objecttype = OId::STRUCTTYPEOID;
}

StructType::StructType(const char *newTypeName, unsigned int numElem)
    : CompositeType(newTypeName, 0),
      elements(numElem),
      elementNames(numElem),
      elementOffsets(numElem),
      numElems(0),
      align(1)
{
    myType = STRUCT;
    objecttype = OId::STRUCTTYPEOID;
}

/*************************************************************
 * Method name...: StructType(const StructType& old);
 *
 * Arguments.....: none
 * Return value..: none
 * Description...: copy constructor
 ************************************************************/

StructType::StructType(const StructType &old) : CompositeType(old)
{
    elements = old.elements;
    elementOffsets = old.elementOffsets;
    numElems = old.numElems;
    elementNames.reserve(numElems);
    for (unsigned int i = 0; i < numElems; ++i)
    {
        elementNames[i] = strdup(old.elementNames[i]);
    }
    align = old.align;
}

StructType::StructType(const OId &structtypeid)
    : CompositeType(structtypeid),
      elements(0),
      elementNames(0),
      elementOffsets(0),
      numElems(0),
      align(1)
{
    myType = STRUCT;
    objecttype = OId::STRUCTTYPEOID;
    readFromDb();
}

/*************************************************************
 * Method name...: operator=(const StructType&);
 *
 * Arguments.....: none
 * Return value..: none
 * Description...: copy constructor
 ************************************************************/

StructType &StructType::operator=(const StructType &old)
{
    // Gracefully handle self assignment
    // FIXME memory leak with char* in elementNames and elements
    if (this == &old)
    {
        return *this;
    }
    CompositeType::operator=(old);
    elements = old.elements;
    elementNames = old.elementNames;
    elementOffsets = old.elementOffsets;
    numElems = old.numElems;
    align = old.align;
    return *this;
}

StructType::~StructType() noexcept(false)
{
    ObjectBroker::deregisterDBObject(myOId);
    validate();
    for (unsigned int i = 0; i < getNumElems(); i++)
        free(static_cast<void *>(
                 elementNames[i]));  // is ok because noone is using it
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
 *                 Assumes that Struct is stored MSB..LSB
 *                 on HP.
 ************************************************************/

void StructType::printCell(std::ostream &stream, const char *cell) const
{
    unsigned int i;

    stream << "\t|";
    for (i = 0; i < numElems; i++)
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
    unsigned int i;
    for (i = 0; i < numElems; i++)
    {
        elements[i]->generateCTypeName(names);
    }
}

void StructType::generateCTypePos(std::vector<int> &positions,
                                  int offset) const
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
    unsigned int i;
    for (i = 0; i < numElems; i++)
    {
        elements[i]->getTypes(types);
    }
}

char *StructType::getTypeStructure() const
{
    // this implementation is not very clever, perhaps should use
    // an intelligent string class
    char *result = static_cast<char *>(mymalloc(10));
    char *newResult;
    unsigned int i;

    strcpy(result, "struct { ");
    if (numElems == 0)
    {
        newResult = static_cast<char *>(mymalloc(strlen(result) + 1 + 2));
        strcpy(newResult, result);
        strcat(newResult, " }");
        free(result);
        return newResult;
    }
    for (i = 0; i < numElems; i++)
    {
        char *dummy = elements[i]->getTypeStructure();
        newResult = static_cast<char *>(mymalloc(
            strlen(result) + strlen(elementNames[i]) + strlen(dummy) + 1 + 3));
        strcpy(newResult, result);

        strcat(newResult, dummy);
        strcat(newResult, " ");
        strcat(newResult, elementNames[i]);

        strcat(newResult, ", ");
        free(result);
        free(dummy);
        result = newResult;
    }
    newResult = static_cast<char *>(mymalloc(strlen(result) + 1));
    strcpy(newResult, result);
    newResult[strlen(newResult) - 2] = ' ';
    newResult[strlen(newResult) - 1] = '}';
    free(result);
    result = newResult;
    return result;
}

char *StructType::getNewTypeStructure() const
{
    std::ostringstream ss;
    ss << "(";

    bool isFirst = true;

    for (unsigned int i = 0; i < numElems; i++)
    {
        if (!isFirst)
        {
            ss << ", ";
        }

        char *dummy = elements[i]->getTypeStructure();

        ss << elementNames[i]
           << " "
           << dummy;

        isFirst = false;
    }

    ss << ")";

    std::string result = ss.str();

    return strdup(result.c_str());
}

unsigned int StructType::addElement(const char *elemName,
                                    const char *elemType)
{
    BaseType *typeByName = nullptr;
    typeByName = static_cast<BaseType *>(ObjectBroker::getObjectByName(OId::ATOMICTYPEOID, elemType));
    if (typeByName == nullptr)
    {
        typeByName = static_cast<BaseType *>(ObjectBroker::getObjectByName(OId::STRUCTTYPEOID, elemType));
    }
    return addElement(elemName, typeByName);
}

unsigned int StructType::addElement(const char *elemName,
                                    const BaseType *newType)
{
    if (newType)
    {
        if (!_isPersistent)
        {
            addElementPriv(elemName, newType);
        }
    }
    return numElems;
}

unsigned int StructType::addElementPriv(const char *elemName,
                                        const BaseType *newType)
{
    unsigned int currPos = 0;
    char *myElemName = nullptr;

    myElemName = static_cast<char *>(mymalloc(strlen(elemName) + 1));
    strcpy(myElemName, elemName);

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
    unsigned int i;
    unsigned int retval = 0;
    for (i = 0; i < numElems; i++)
    {
        if (strcmp(elementNames[i], elemName) == 0)
        {
            retval = elementOffsets[i];
            break;
        }
    }
#ifdef DEBUG
    if (found == false)
    {
        LERROR << "ERROR in StructType::getOffset(" << elemName
               << ") name not found " << getName() << " " << myOId << " retval "
               << retval;
        throw r_Error(STRUCTTYPE_ELEMENT_UNKNOWN);
    }
#endif

    return retval;
    // should raise exception!
}

unsigned int StructType::getOffset(unsigned int num) const
{
    if (num >= numElems)
    {
        LTRACE << "ERROR in StructType::getOffset(" << num
               << ") offset out of bounds " << getName() << " retval " << 0;
#ifdef DEBUG
        throw r_Error(STRUCTTYPE_ELEMENT_OUT_OF_BOUNDS);
#endif
        return 0;
    }
    return elementOffsets[num];
}

const BaseType *StructType::getElemType(const char *elemName) const
{
    const BaseType *retval = nullptr;
    unsigned int i;

    for (i = 0; i < numElems; i++)
    {
        if (strcmp(elementNames[i], elemName) == 0)
        {
            retval = elements[i];
            break;
        }
    }
#ifdef DEBUG
    if (retval == 0)
    {
        LERROR << "ERROR in StructType::getElemType(" << elemName
               << ") name not found " << getName() << " " << myOId << " retval "
               << retval;
        throw r_Error(STRUCTTYPE_ELEMENT_UNKNOWN);
    }
#endif
    return retval;
}

const BaseType *StructType::getElemType(unsigned int num) const
{
    if (!(num < numElems))
    {
        LTRACE << "ERROR in StructType::getElemType(" << num
               << ") offset out of bounds " << getName() << " retval " << 0;
#ifdef DEBUG
        throw r_Error(STRUCTTYPE_ELEMENT_OUT_OF_BOUNDS);
#endif
        return nullptr;
    }
    return elements[num];
}

const char *StructType::getElemName(unsigned int num) const
{
    if (!(num < numElems))
    {
        LTRACE << "ERROR in StructType::getElemName(" << num
               << ") offset out of bounds " << getName() << " retval " << 0;
#ifdef DEBUG
        throw r_Error(STRUCTTYPE_ELEMENT_OUT_OF_BOUNDS);
#endif
        return nullptr;
    }
    return elementNames[num];
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
    unsigned int i;

    for (i = 0; i < numElems; i++)
    {
        if (elements[i] == aStruct)
        {
            return 1;
        }
        else if (elements[i]->getType() == STRUCT)
            if (static_cast<const StructType *>(elements[i])->contains(aStruct))
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
        LTRACE << "no structtype";
        retval = 0;
    }
    else
    {
        if (elements.size() != static_cast<const StructType *>(aType)->elements.size())
        {
            LTRACE << "not the same size";
            retval = 0;
        }
        else
        {
            const BaseType *myBaseType;
            const BaseType *otherBaseType;

            unsigned int i;

            retval = 1;
            for (i = 0; i < elements.size(); i++)
            {
                myBaseType = elements[i];
                otherBaseType = (static_cast<const StructType *>(aType))->elements[i];
                if (!myBaseType->compatibleWith(otherBaseType))
                {
                    LTRACE << i << ". element " << otherBaseType->getName() << " does not match " << myBaseType;
                    retval = 0;
                    break;
                }
            }
        }
    }
    return retval;
}

void StructType::calcSize()
{
    unsigned int tempSize = 0;

    for (unsigned int i = 0; i < numElems; i++)
    {
        tempSize += elements[i]->getSize();
    }

    size = tempSize;
}

