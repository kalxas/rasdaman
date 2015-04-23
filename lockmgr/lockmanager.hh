/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
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
 * INCLUDE: lockmanager.hh
 *
 * MODULE: lockmgr
 *
 * PURPOSE: Header file of the lockmanger.
 *
 ************************************************************/

#ifndef _LOCKMANAGER_HH_
#define _LOCKMANAGER_HH_

#include "reladminif/oidif.hh"
#include "tilemgr/tile.hh"
#include "ecpg_lockmanager.hh"
#include <vector>
#include <boost/shared_ptr.hpp>

enum Lock {EXCLUSIVE_LOCK, SHARED_LOCK};

/**
 * This class contains the C++-part implementation of the lock manager.
 */

class LockManager
{
    private:
        // constructor private such that it can not be called from the outside
        LockManager();

        // copy constructor is private such that it cannot be called from the outside
        LockManager(LockManager const&);

        // destructor is private such that it cannot be called from the outside
        ~LockManager();

        // assignment operator is private such that it cannot be called from the outside
        LockManager& operator=(LockManager const&);

        // a static lock manager object used to call the function of the lock manager
        static LockManager *LM_instance;

        // an ECPG lock manager object used to call the ECPG-functions of the lock manager
        ECPG_LockManager *ecpg_LockManager;

        // the name of the lockmanager's own connection
        const char * connectionName;

        // function for beginning a new transaction
        void beginTransaction();

        // function for ending the current transaction
        void endTransaction();

        // private function for locking a tile (shared or exclusive)
        void lockTileInternal(const char *, OId::OIdCounter, enum Lock);

        // private function for unlocking a tile with respect to a specific rasserver
        void unlockTileInternal(const char *, OId::OIdCounter);

        // private function for unlocking all tiles with respect to a specific rasserver
        void unlockAllTilesInternal(const char *);

        // private function for checking if a tile is locked or not by a specific type of lock
        bool isTileLockedInternal(OId::OIdCounter, enum Lock);

        // private function for clearing the locks from the lock table corresponding to a specific rasserver
        void clearLockTableInternal(const char *);

        // function for fetching the id of the current rasserver
        void generateServerId(char *);

        // function for fetching the type of the operation / lock (i.e., shared or exclusive)
        enum Lock generateLockType();

        // compare function need by qsort from stdlib.h which is used to sort the sequence of tile ids
        static int compareIds(const void *, const void *);

        // function for locking an array of tile ids at "once"
        void lockTilesInternal(const char *, long long [], int, enum Lock);

    public:
        // function creating an instance of the lock manager. There should be no other
        // possibility for creating one.
        static LockManager * Instance();

        // function for opening the database connection
        void connect();

        // function for closing the connection
        void disconnect();

        // function processes parameters and then call the corresponding private functions for
        // a vector of tiles
        void lockTiles(std::vector< boost::shared_ptr<Tile> > *);

        // function for locking all tiles corresponding to the ids in the array
        void lockTiles(long long [], int);

        // functions processes parameters and then call the corresponding private functions for locking a tile
        void lockTile(Tile *);

        // function processes parameters and then call the corresponding private functions for unlocking a tile
        void unlockTile(Tile *);

        // function processes parameters and then call the corresponding private functions for
        // unlocking all tiles corresponding to a specific rasserver
        void unlockAllTiles();

        // function processes parameters and then call the corresponding private functions for
        // checking if a tile is locked (shared or exclusive)
        bool isTileLocked(Tile *, enum Lock);

        // function processes parameters and then call the corresponding private functions for
        // clearing the locks from the lock table corresponding to a specific rasserver
        void clearLockTable();
};
#endif
