#ifndef _LOCKMANAGER_HH_
#define _LOCKMANAGER_HH_
#define LOCKMANAGER_ON

#include "reladminif/oidif.hh"
#include "tilemgr/tile.hh"
#include "ecpg_lockmanager.hh"
#include <vector>

enum Lock {EXCLUSIVE_LOCK, SHARED_LOCK};

#define LOCKMANAGER_ON 1
//#undef LOCKMANAGER_ON

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
        void lockTileInternal(const char *, const char *, OId::OIdCounter, enum Lock);

        // private function for unlocking a tile with respect to a specific rasserver
        void unlockTileInternal(const char *, const char *, OId::OIdCounter);

        // private function for unlocking all tiles with respect to a specific rasserver and corresponding client
        void unlockAllTilesInternal(const char *, const char *);

        // private function for checking if a tile is locked or not by a specific type of lock
        bool isTileLockedInternal(OId::OIdCounter, enum Lock);

        // private function for clearing the locks from the lock table corresponding to a specific rasserver
        void clearLockTableInternal(const char *);

        // function for fetching the id of the current rasserver
        void generateServerId(char *);

        // function for fetching the id of the corresponding client
        void generateClientId(unsigned long, char *);

        // function for fetching the type of the operation / lock (i.e., shared or exclusive)
        enum Lock generateLockType();

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
        void lockTiles(unsigned long, std::vector<Tile *> *);

        // functions processes parameters and then call the corresponding private functions for locking a tile
        void lockTile(unsigned long, Tile *);

        // function processes parameters and then call the corresponding private functions for unlocking a tile
        void unlockTile(unsigned long, Tile *);

        // function processes parameters and then call the corresponding private functions for
        // unlocking all tiles corresponding to a specific rasserver
        void unlockAllTiles(unsigned long);

        // function processes parameters and then call the corresponding private functions for
        // checking if a tile is locked (shared or exclusive)
        bool isTileLocked(Tile *, enum Lock);

        // function processes parameters and then call the corresponding private functions for
        // clearing the locks from the lock table corresponding to a specific rasserver
        void clearLockTable();
};
#endif
