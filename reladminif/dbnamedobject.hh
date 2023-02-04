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
#pragma once

#include <iosfwd>                  // for cout, ostream

#include "dbobject.hh"             // for DBObject
#include "oidif.hh"                // for OId
#include "raslib/mddtypes.hh"  // for r_Bytes

class DBNamedObject;
class DBObject;

//@ManMemo: Module: {\bf reladminif}.
/*@Doc:
Has functionality for setting the name of itsself from VARCHAR structures.
Takes care of too long names.
Implements set/getName functionality.
*/
/**
  * \ingroup Reladminifs
  */
class DBNamedObject : public DBObject
{
public:
    
    static unsigned int MAXNAMELENGTH;
    /*@Doc:
    the maximum length of a name.
    */
    
    DBNamedObject();
    /*@Doc:
    sets Name to defaultName
    */

    explicit DBNamedObject(const OId &id);
    /*@Doc:
    only initializes itself
    */

    DBNamedObject(const DBNamedObject &old);
    /*@Doc:
    sets myName to the name of the old object
    */

    explicit DBNamedObject(const char *name);
    /*@Doc:
    sets myName to name
    */

    DBNamedObject(const OId &id, const char *name);
    /*@Doc:
    sets myName to name and calls DBObject(OId).  this is needed by MDDSet.
    */

    ~DBNamedObject() noexcept(false) override = default;
    
    DBNamedObject &operator=(const DBNamedObject &old);
    /*@Doc:
    takes care of the name
    */

    const char *getName() const;
    /*@Doc:
    returns a pointer to myName.
    */

    r_Bytes getMemorySize() const override;
    /*@Doc:
    Should be revised not to include attribute sizes
    */

    void printStatus(unsigned int level,
                     std::ostream &stream) const override;
    /*@Doc:
    prints the status of DBObject + Name: myName
    */

protected:
    void setName(const char *newname);
    /*@Doc:
    renames the object
    */

    void setName(const short length, const char *data);
    /*@Doc:
    sets the name from a VARCHAR structure
    */

    std::string myName;
    /*@Doc:
    the name of the object
    */

    static const char *defaultName;
};

