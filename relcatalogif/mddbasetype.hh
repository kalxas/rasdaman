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
// -*-C++-*- (for Emacs)

/*************************************************************
 *
 *
 * PURPOSE:
 *   The MDDBaseType class is used as a type for MDDs where
 *   only the base type is specified.
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef _MDDBASETYPE_HH_
#define _MDDBASETYPE_HH_

#include "mddtype.hh"
#include <iosfwd>

class MDDBaseType;

class OId;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
  The MDDBaseType class is used as a type for MDDs when
  only the base type is specified.
*/

/**
  * \ingroup Relcatalogifs
  */
class MDDBaseType : public MDDType
{
public:
    char *getTypeStructure() const override;
    /*@Doc:
    returns a string: marray < myBaseType->getTypeStructure >
    */

    char *getNewTypeStructure() const override;

    MDDBaseType(const OId &id);
    /*@Doc:
    constructs a MDDBaseType out of the database.
    */

    MDDBaseType(const char *newTypeName, const BaseType *newBaseType);
    /*@Doc:
    constructor.
    */

    MDDBaseType();
    /*@Doc:
    default constructor, cannot be used.
    */

    MDDBaseType(const char *newtypename);
    /*@Doc:
    */

    MDDBaseType(const MDDBaseType &old);
    /*@Doc:
    copy constructor.
    */

    MDDBaseType &operator=(const MDDBaseType &old);
    /*@Doc:
    assignment operator.
    */

    const BaseType *getBaseType() const;
    /*@Doc:
    returns base type.
    */

    void print_status(std::ostream &s) const override;
    /*@Doc:
    writes the state of the object to the specified stream:
    \tr_Marray < myBaseType->getTypeName() \t>
    */

    ~MDDBaseType() noexcept(false) override;
    /*@Doc:
    virtual destructor.
    validates the object.
    */

    int compatibleWith(const Type *aType) const override;
    /*@Doc:
    to be compatible the following must be true:
        aType must be MDDBASETYPE or subclass and
        myBaseType must be compatible with aType->myBaseType
    */

    int compatibleWithDomain(const r_Minterval *aDomain) const override;
    /*@Doc:
    create a new MDDDomainType with itself and aDomain, then it
    checks compatibility with self.
    */

    r_Bytes getMemorySize() const override;
    /*@Doc:
    computes memory size by:
    MDDType::getMemorySize() + myBaseType->getMemorySize() + sizeof(BaseType*);
    */

protected:
    void insertInDb() override;

    void readFromDb() override;

    void deleteFromDb() override;

    const BaseType *myBaseType;
    /*@Doc:
    reference to the basetype
    */
};

#endif
