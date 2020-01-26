// -*-C++-*- (for Emacs)

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

#ifndef _STRUCTTYPE_HH_
#define _STRUCTTYPE_HH_

#include <iosfwd>
#include <vector>

#include "compositetype.hh"

class BaseType;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
StructType is the base type used for user defined structures. It
stores the names and BaseTypes of the elements. The size of a
StructType in chars depends on the elements.

StructType now uses alignment for structures with elements of
different sizes. One byte types are aligned to one byte, two byte
types to two, all other types to four. The Size is padded to 4 byte
alignment except for types with only chars (one byte alignment) or
type with only shorts (two byte alignment). Structs as elements of
structs are aligned with the minimum byte alignment needed for the
struct.
*/

/**
  * \ingroup Relcatalogifs
  */
class StructType : public CompositeType
{
public:
    StructType();
    StructType(const char *newTypeName, unsigned int numElem);
    StructType(const OId &structtypeid);
    StructType(const StructType &);
    StructType &operator=(const StructType &) = delete;
    ~StructType() noexcept(false) override;

    void printCell(std::ostream &stream, const char *cell) const override;

    char *getTypeStructure() const override;
    char *getNewTypeStructure() const override;

    /// generate equivalent C type names
    void generateCTypeName(std::vector<const char *> &names) const override;
    void generateCTypePos(std::vector<int> &positions,
                          int offset = 0) const override;
    void getTypes(std::vector<const BaseType *> &types) const override;

    /// add new element to struct
    unsigned int addElement(const char *elemName, const char *elemType);

    /// add new element to struct using pointer to BaseType
    unsigned int addElement(const char *elemName, const BaseType *elemType);

    /// get offset for an element by name of element.
    unsigned int getOffset(const char *elemName) const;

    /// get offset for an element by number of element (0 based).
    unsigned int getOffset(unsigned int num) const;

    /// get type of an element by name of element.
    const BaseType *getElemType(const char *elemName) const;

    /// get name of an element by number of element (0 based).
    const char *getElemName(unsigned int num) const;

    /// get type of an element by number of element (0 based).
    const BaseType *getElemType(unsigned int num) const;

    /// get number of elements.
    unsigned int getNumElems() const;

    /// get alignment needed for structure to be embedded in another structure.
    unsigned int getAlignment() const;

    /// checks if a certain StructType is contained in this StructType
    int contains(const StructType *aStruct) const;

    int compatibleWith(const Type *aType) const override;
    
    bool operator==(const Type &o) const override;

    r_Bytes getMemorySize() const override;

private:
    // those inherited from BaseType aren't useful at all for StructType
    // made them private to preven calling them
    r_ULong *convertToCULong(const char *cell, r_ULong *value) const override;
    char *makeFromCULong(char *cell, const r_ULong *value) const override;
    r_Long *convertToCLong(const char *cell, r_Long *value) const override;
    char *makeFromCLong(char *cell, const r_Long *value) const override;
    r_Double *convertToCDouble(const char *cell, r_Double *value) const override;
    char *makeFromCDouble(char *cell, const r_Double *value) const override;

protected:
    void insertInDb() override;
    void deleteFromDb() override;
    void readFromDb() override;

    // moves back one step all elements all elements behind pos
    void moveBack(int pos);

    // calculates and sets current size of type with alignment
    void calcSize();

    /// Array containing references to base types of elements.
    std::vector<const BaseType *> elements;

    /// Array containing names of elements.
    std::vector<char *> elementNames;

    /// Array containing offsets to elements
    std::vector<unsigned int> elementOffsets;

    /// The number of elements.
    unsigned int numElems{0};

    /// Alignment needed for structure if embedded in other structures.
    unsigned int align{1};

    /// add new element to struct using pointer to BaseType
    /// does the actuall adding.  the public method will not let a persitent type
    /// be changed
    unsigned int addElementPriv(const char *elemName, const BaseType *elemType);
};

#include "structtype.icc"

#endif
