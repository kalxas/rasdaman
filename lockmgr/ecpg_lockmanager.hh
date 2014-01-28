#ifndef _ECPG_LOCKMANAGER_HH_
#define _ECPG_LOCKMANAGER_HH_

/**
 * This class contains the SQL-specific implementation of the lock manager.
 */

class ECPG_LockManager
{
private:
    // constructor private so that it cannot be called
    ECPG_LockManager();

    // copy constructor is private such that it cannot be called
    ECPG_LockManager(ECPG_LockManager const&);

    // assignment operator is private as above
    ECPG_LockManager& operator=(ECPG_LockManager const&);

    // a static lock manager object used to call the function of the lock manager
    static ECPG_LockManager *ECPG_LM_Instance;

public:
    // function creating an instance of the lock manager. There should be no other
    // possibility for creating one.
    static ECPG_LockManager * Instance();

    // function for connecting to RASBASE via a parameter-specified connection
    bool connect(const char *, const char *, const char *, const char *);

    // function for disconnecting from RASBASE via the above connection
    bool disconnect(const char *);

    // function for beginning a new transaction
    void beginTransaction(const char *);

    // function for ending the current transaction
    void endTransaction(const char *);

    // function for setting a shared lock in the lock table according to the parameters
    void lockTileShared(const char *, const char *, const char *, long long);

    // function for setting an exclusive lock in the lock table according to the parameters
    void lockTileExclusive(const char *, const char *, const char *, long long);

    // function for removing a lock from lock table according to the parameters
    void unlockTile(const char *, const char *, const char *, long long);

    // function for unlocking all tiles corresponding to a server and client
    void unlockAllTiles(const char *, const char *, const char *);

    // function for checking if a tile is locked or not (shared or exclusive)
    bool isTileLocked(const char *, const char *, const char *, long long);

    // function for checking if a tile has a shared lock
    bool isTileLockedShared(const char *, const char *, const char *, long long);

    // function for checking if a tile has an exclusive lock
    bool isTileLockedExclusive(const char *, const char *, const char *, long long);

    // function for deleting all entries of the lock table corresponding to a given server
    // but without port specification
    void clearWLikeLockTable(const char *, const char *);

    // function for deleting all entries of the lock table corresponding to a given server
    void clearLockTable(const char *, const char *);
};
#endif
