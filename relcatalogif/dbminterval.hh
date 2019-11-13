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
#ifndef _DBMINTERVAL_HH_
#define _DBMINTERVAL_HH_

#include "reladminif/dbobject.hh"
#include "raslib/minterval.hh"

class DBMinterval;

template <class T>
class DBRef;
using DBMintervalId = DBRef<DBMinterval>;

//@ManMemo: Module: {\bf relcatalogif}.

/*@Doc:
Persistent version of r_Minterval. It is used by DBMDDObj and MDDDomainType.
For a more efficient version refer to InlineMInterval.
*/

/**
  * \ingroup Relcatalogifs
  */
class DBMinterval : public DBObject, public r_Minterval
{
public:
    DBMinterval();

    DBMinterval(const OId &id);

    DBMinterval(r_Dimension dim);

    DBMinterval(const char *dom);

    DBMinterval(const r_Minterval &old);

    DBMinterval(const r_Minterval &old, const std::vector<std::string> *axisNames2);

    DBMinterval(const DBMinterval &old);

    ~DBMinterval() noexcept(false) override;
    /*@Doc:
    validates the object in the database.
    */

    virtual DBMinterval &operator=(const DBMinterval &old);
    /*@Doc:
    replaces only the r_Minterval part of the object
    */

    virtual DBMinterval &operator=(const r_Minterval &old);
    /*@Doc:
    replaces only the r_Minterval part of the object
    */

    r_Bytes getMemorySize() const override;
    /*@Doc:
    estimates the space taken up by this object with:
    DBObject::getMemorySize() + sizeof(r_Minterval)
        + dimensionality * (4 + 4 + 1 + 1)
    */

protected:
    /**
     * @return the current dimension in RAS_DOMAINS.
     */
    r_Dimension getDimensionInDb() const;
    
    /**
     * Set high and low based on the interval at dimension count.
     */
    void setBounds(r_Dimension count, std::string &high, std::string &low) const;

    void insertInDb() override;
    /*@Doc:
    inserts the object into the database.  it uses one table
    for the fixed length attributes (oid, size, dimension) and
    another for dynamic data (lower/upper bounds/fixed ranges)
    */

    void updateInDb() override;

    void deleteFromDb() override;

    void readFromDb() override;
};

#endif
