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


#include "keyobject.hh"
#include "tilemgr/tile.hh"        // for Tile
#include "relblobif/tileid.hh"    // for DBTileId
#include "raslib/error.hh"

#include <boost/make_shared.hpp>  // for shared_ptr::operator bool
#include <ostream>                // for operator<<, ostream, basic_ostream


std::ostream &operator<<(std::ostream &in, const KeyObject &d)
{
    if (d.isPersCarrier())
    {
        in << "Carrier{" << d.getDomain() << ", " << d.getObject().getOId() << "}";
    }
    else
    {
        in << "Carrier{" << d.getDomain() << ", TransTile}";
    }
    return in;
}

KeyObject::KeyObject() = default;

KeyObject::KeyObject(const KeyObject &old) = default;

std::string KeyObject::toString() const
{
    if (isPersCarrier())
    {
        return "Carrier{" + getDomain().to_string() + ", " + std::to_string(getObject().getOId().getCounter()) + "}";
    }
    else
    {
        return "Carrier{" + getDomain().to_string() + ", TransTile}";
    }
}

KeyObject::KeyObject(boost::shared_ptr<Tile> tile)
    : persobject(), domain(tile->getDomain())
{
    if (tile->isPersistent())
    {
        persobject = static_cast<const DBObjectId &>(tile->getDBTile());
    }
    else
    {
        transobject = tile;
    }
}

KeyObject::KeyObject(const DBObjectId &obj, const r_Minterval &dom)
    : persobject(obj), domain(dom) {}

KeyObject::~KeyObject() noexcept(false)
{
    transobject.reset();
}

void KeyObject::setDomain(const r_Minterval &dom)
{
    domain = dom;
}

void KeyObject::setTransObject(boost::shared_ptr<Tile> tile)
{
    domain = tile->getDomain();
    transobject = tile;
}

void KeyObject::setObject(const DBObjectId &obj)
{
    persobject = obj;
}

bool KeyObject::isInitialised() const
{
    if (transobject)
    {
        return true;
    }
    if (persobject.isInitialised())
    {
        return true;
    }
    return false;
}

bool KeyObject::isPersCarrier() const
{
    return (transobject == nullptr);
}

boost::shared_ptr<Tile> KeyObject::getTransObject() const
{
    return transobject;
}

const DBObjectId &KeyObject::getObject() const
{
    return persobject;
}

const r_Minterval &KeyObject::getDomain() const
{
    return domain;
}
