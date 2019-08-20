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
#ifndef __QTCOLLECTION_HH__
#define __QTCOLLECTION_HH__

#include <string>

//forward declaration
class QtCollection;


//@ManMemo: Module: {\bf qlparser}

/*@Doc:

This class encapsulates a namedCollection. It's primary use is support for remote
collections.

*/

class QtCollection
{
public:
    /// copy constructor
    QtCollection(const QtCollection &collection);

    /// constructor getting only the collection name
    QtCollection(const std::string &collectionNameNew);

    /// constructor getting all the parameters
    QtCollection(const std::string &hostnameNew, int portNew,
                 const std::string &collectionNameNew);

    /// destructor
    ~QtCollection();

    /// get methods
    inline std::string getHostname() const;
    inline int getPort() const;
    inline const std::string &getCollectionName() const;

private:
    std::string hostname;
    int port;
    std::string collectionName;
};

inline std::string
QtCollection::getHostname() const
{
    return hostname;
}

inline int
QtCollection::getPort() const
{
    return port;
}

inline const std::string &
QtCollection::getCollectionName() const
{
    return collectionName;
}

#endif
