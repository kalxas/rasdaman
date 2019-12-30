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

#include "raslib/mddtypes.hh"
#include "raslib/minterval.hh"

#include <map>
#include <set>
#include <vector>


class DBObject;
class OId;
class InlineMinterval;
class KeyObject;
class IndexDS;
class HierIndexDS;
class Tile;

// used to hold oids for indexes, blobs, and dbmintervals.  no double entries
using OIdSet = std::set<OId, std::less<OId>>;

// used to hold oids for indexes, blobs, and dbmintervals.  no double entries
using OIdConstSet = std::set<const OId, std::less<OId>>;

using HierIndexDSPVector = std::vector<HierIndexDS *>;

// used to hold DBObject*.  e.g. in objectbroker to temporarily store them
// before deletion
using DBObjectPVector = std::vector<DBObject *>;

// used to hold DBObject*.  e.g. in objectbroker to temporarily store them
// before deletion
using DBObjectPConstVector = std::vector<const DBObject *>;

// used to hold oids for indexes, blobs, and dbmintervals
using OIdVector = std::vector<OId>;

// used to hold oids for indexes, blobs, and dbmintervals
using OIdConstVector = std::vector<const OId>;

// holds type information on specific blobs which are stored in above oidlists
using CompTypeVector = std::vector<r_Data_Format>;

using IntervalPConstVector = std::vector<const InlineMinterval *>;

using IntervalPVector = std::vector<InlineMinterval *>;

using IntervalConstVector = std::vector<const InlineMinterval>;

using IntervalVector = std::vector<InlineMinterval>;

using KeyObjectPConstVector = std::vector<const KeyObject *>;

using KeyObjectPVector = std::vector<KeyObject *>;

using KeyObjectConstVector = std::vector<const KeyObject>;

using KeyObjectVector = std::vector<KeyObject>;

using DomainVector = std::vector<r_Minterval>;

using DomainPVector = std::vector<r_Minterval *>;

using DomainPConstVector = std::vector<const r_Minterval *>;

using IndexPVector = std::vector<IndexDS *>;

using TilePVector = std::vector<Tile *>;

using DBObjectPMap = std::map<double, DBObject *, std::less<double>>;
using DBObjectPPair = std::pair<double, DBObject *>;
using ConstDBObjectPPair = std::pair<const double, DBObject *>;

using DBObjectPConstMap = std::map<double, const DBObject *, std::less<double>>;
using DBObjectPConstPair = std::pair<double, const DBObject *>;
using ConstDBObjectPConstPair = std::pair<const double, const DBObject *>;

using OIdMap = std::map<double, OId, std::less<double>>;
using OIdPair = std::pair<const double, OId>;
using OIdConstPair = std::pair<const double, const OId>;

using DomainMap = std::map<double, r_Minterval, std::less<double>>;
using DomainPair = std::pair<const double, r_Minterval>;
using DomainConstPair = std::pair<const double, const r_Minterval>;
