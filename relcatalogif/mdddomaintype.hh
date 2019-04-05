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
/*************************************************************
 *
 *
 * PURPOSE:
 *   The MDDBaseType class is used as a type for MDDs where
 *   the base type and the domain is specified.
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef _MDDDOMAINTYPE_HH_
#define _MDDDOMAINTYPE_HH_


#include <iosfwd>
#include "raslib/minterval.hh"
#include "catalogmgr/ops.hh"
#include "mddbasetype.hh"
#include "raslib/mddtypes.hh"  //for r_Range

class DBMinterval;
class OId;
class MDDDomainType;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
  The MDDBaseType class is used as a type for MDDs where
  the base type and the domain is specified.
*/

/**
  * \ingroup Relcatalogifs
  */
class MDDDomainType : public MDDBaseType
{
public:
    char *getTypeStructure() const override;
    /*@Doc:
    looks like:
        marray <myBaseType->getTypeStructure(), myDomain->get_string_representation()>
    */

    char *getNewTypeStructure() const override;

    MDDDomainType(const OId &id);

    MDDDomainType(const char *newTypeName, const BaseType *newBaseType, const r_Minterval &newDomain);

    MDDDomainType();
    /*@Doc:
    default constructor, cannot be used.
    */

    MDDDomainType(const MDDDomainType &old);
    /*@Doc:
    copy constructor.
    */

    MDDDomainType &operator=(const MDDDomainType &old);
    /*@Doc:
    assignment operator.
    */

    const r_Minterval *getDomain() const;
    /*@Doc:
    returns domain.
    */

    void print_status(std::ostream &s) const override;
    /*@Doc:
    writes the state of the object to the specified stream.
    looks like: \tr_Marray<myBaseType->getTypeName(), myDomain->print_status()\t>
    */

    ~MDDDomainType() noexcept(false) override;
    /*@Doc:
    virtual destructor.
    calls validate and deletes myDomain
    */

    void setPersistent(bool t) override;
    /*@Doc:
        this method from DBObject is overridden to make sure that
        the dbminterval is also made persistent/deleted from db.
    */

    int compatibleWith(const Type *aType) const override;
    /*@Doc:
    aType is compatible if:
        aType is a MDDDomainType and
        the basetypes are compatible
    */

    r_Bytes getMemorySize() const override;
    /*@Doc:
    memory space is computed by
        MDDBaseType::getMemorySize() + sizeof(DBMinterval*)
            + myDomain->getMemorySize();
    */

protected:
    DBMinterval *myDomain;
    /*@Doc:
    persistent domain.
    */

    void insertInDb() override;

    void readFromDb() override;

    void deleteFromDb() override;
};

#endif
