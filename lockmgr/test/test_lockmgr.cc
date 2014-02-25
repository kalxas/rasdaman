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
 * INCLUDE: test_lockmgr.cc
 *
 * MODULE: lockmgr/test
 *
 * PURPOSE: To test the correctness of the locking mechanism (shared
 * and exclusive locks).
 *
 *
 * COMMENTS: 13 test cases (scenarios) are defined, each in one
 * function and can be also run together by calling one single
 * function (test_allCases()) or separate functions (test_$name()).
 *
 ************************************************************/
#include <stdio.h>
#include <stdlib.h>

#include "lockmgr/ecpg_lockmanager.hh"
#include <string.h>
#include <iostream>

using namespace std;

const char * connectionName = "testConn";

/**
 * Function which tries to lock a tile exclusively and then it checks if the locking was done or not.
 *
 * @param pecpg_lockmanager
 *     pointer to the lockmanager object
 * @param pTestServerId
 *     string representing the serverId
 * @param pTest_tileID
 *     id of the tile to be locked
 * @param state
 *     bool value which represents if the test is a positive or negative test (check for locked or not locked)
 */
void lockTestTileExclusive(ECPG_LockManager * pecpg_lockmanager, char * pTestServerId, long long pTest_tileID, bool state)
{
    pecpg_lockmanager->lockTileExclusive(connectionName, pTestServerId, pTest_tileID);
    if (pecpg_lockmanager->isTileLockedExclusive(connectionName, pTestServerId, pTest_tileID))
    {
        if (state)
        {
            std::cout << "Ok: Test tile is locked (exclusive)" << endl;
        }
        else
        {
            std::cout << "Error: Test tile is locked (exclusive)" << endl;
        }
    }
    else
    {
        if (state)
        {
            std::cerr << "Error: Test tile cannot be locked (exclusive)" << endl;
        }
        else
        {
		std::cerr << "Ok: Test tile cannot be locked (exclusive)" << endl;
        }
    }
}

/**
 * Function which tries to lock a tile shared and then it checks if the locking was done or not.
 *
 * @param pecpg_lockmanager
 *     pointer to the lockmanager object
 * @param pTestServerId
 *     string representing the serverId
 * @param pTest_tileID
 *     id of the tile to be locked
 * @param state
 *     bool value which represents if the test is a positive or negative test (check for locked or not locked)
 */
void lockTestTileShared(ECPG_LockManager * pecpg_lockmanager, char * pTestServerId, long long pTest_tileID, bool state)
{
    pecpg_lockmanager->lockTileShared(connectionName, pTestServerId, pTest_tileID);
    if (pecpg_lockmanager->isTileLockedShared(connectionName, pTestServerId, pTest_tileID))
    {
        if (state)
        {
            std::cout << "Ok: Test tile is locked (shared)" << endl;
        }
        else
        {
            std::cout << "Error: Test tile is locked (shared)" << endl;
        }
    }
    else
    {
        if (state)
        {
            std::cerr << "Error: Test tile cannot be locked (shared)" << endl;
        }
        else
        {
		std::cerr << "Ok: Test tile is cannot be locked (shared)" << endl;
        }
    }
}

/**
 * Function which tries to unlock a tile and then it checks if the unlocking was done or not.
 *
 * @param pecpg_lockmanager
 *     pointer to the lockmanager object
 * @param pTestServerId
 *     string representing the serverId
 * @param pTest_tileID
 *     id of the tile to be unlocked
 */
void unlockTestTile(ECPG_LockManager * pecpg_lockmanager, char * pTestServerId, long long pTest_tileID)
{
    pecpg_lockmanager->unlockTile(connectionName, pTestServerId, pTest_tileID);
    if (!pecpg_lockmanager->isTileLocked(connectionName, pTestServerId, pTest_tileID))
    {
        std::cout << "Ok: Test tile is unlocked" << endl;
    }
    else
    {
        std::cerr << "Error: Test tile is not unlocked" << endl;
    }
}

/**
 * Function implementing test case 1: create and then delete an exclusive lock
 * using test values for serverId and tileID.
 */
void test_createDeleteExclusiveLock()
{
    std::cout << "test_createDeleteExclusiveLock: begin" << endl;
    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    // create an exclusive lock
    long long test_tileID = -1;
    char* testServerId = (char*)"test_rasServer";
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileExclusive(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    // call unlockAllTiles to make sure that no tiles remain locked
    ecpg_lockmanager->beginTransaction(connectionName);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId);
    ecpg_lockmanager->endTransaction(connectionName);
    std::cout << "test_createDeleteExclusiveLock: end" << endl;
}

/**
 * Function implementing test case 2: create and then delete a shared lock
 * using test values for serverId and tileID.
 */
void test_createDeleteSharedLock()
{
    std::cout << "test_createDeleteSharedLock: begin" << endl;
    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    // create a shared lock
    long long test_tileID = -2;
    char* testServerId = (char*)"test_rasServer";
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileShared(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    // call unlockAllTiles to make sure that no tiles remain locked
    ecpg_lockmanager->beginTransaction(connectionName);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId);
    ecpg_lockmanager->endTransaction(connectionName);
    std::cout << "test_createDeleteSharedLock: end" << endl;
}

/**
 * Function implementing test case 3: create two exclusive locks
 * on the same tile but coming from two different servers.
 * The first lock should be created, the second one should fail.
 * At the end the locks are deleted.
 */
void test_createDelete2ExclusiveLocks()
{
    std::cout << "test_createDelete2ExclusiveLocks: begin" << endl;
    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    // create two exclusive locks
    // first lock
    long long test_tileID = -1;
    char* testServerId = (char*)"test_rasServer";
    char* testServerId2 = (char*)"test_rasServer2";
    lockTestTileExclusive(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    // second lock
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileExclusive(ecpg_lockmanager, testServerId2, test_tileID, false);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId2, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    // call unlockAllTiles to make sure that no tiles remain locked
    ecpg_lockmanager->beginTransaction(connectionName);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId2);
    ecpg_lockmanager->endTransaction(connectionName);
    std::cout << "test_createDelete2ExclusiveLocks: end" << endl;
}

/**
 * Function implementing test case 4: create two shared locks
 * on the same tile but coming from two different servers.
 * The first lock should be created, the second one should fail.
 * At the end the locks are deleted.
 */
void test_createDelete2SharedLocks()
{
    std::cout << "test_createDelete2SharedLocks: begin" << endl;
    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    // create two shared locks
    // first lock
    long long test_tileID = -2;
    char* testServerId = (char*)"test_rasServer";
    char* testServerId2 = (char*)"test_rasServer2";
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileShared(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    // second lock
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileShared(ecpg_lockmanager, testServerId2, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId2, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    // call unlockAllTiles to make sure that no tiles remain locked
    ecpg_lockmanager->beginTransaction(connectionName);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId2);
    ecpg_lockmanager->endTransaction(connectionName);
    std::cout << "test_createDelete2SharedLocks: end" << endl;
}

/**
 * Function implementing test case 5: create and then delete an exclusive lock
 * using test values for serverId and tileID 5 times sequentially.
 */
void test_createDelete5ExclusiveLocks()
{
   int i;
   std::cout << "test_createDelete5ExclusiveLocks: begin" << endl;
   for(i=0; i<5; i++)
       test_createDeleteExclusiveLock();
   std::cout << "test_createDelete5ExclusiveLocks: end" << endl;
}

/**
 * Function implementing test case 6: create and then delete a shared lock
 * using test values for serverId and tileID 5 times sequentially.
 */
void test_createDelete5SharedLocks()
{
   int i;
   std::cout << "test_createDelete5SharedLocks: begin" << endl;
   for(i=0; i<5; i++)
       test_createDeleteSharedLock();
   std::cout << "test_createDelete5SharedLocks: end" << endl;
}

/**
 * Function implementing test case 7: create a shared lock
 * and then try to create an exclusive lock on the same tile
 * but coming from two different servers.
 * The first lock should be created, the second one should fail.
 * At the end the locks are deleted.
 */
void test_createDeleteSharedExclusiveLock()
{
    std::cout << "test_createDeleteSharedExclusiveLock: begin" << endl;
    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    // create one shared lock by a server and try to get an exclusive one by another server
    // shared lock
    long long test_tileID = -2;
    char* testServerId = (char*)"test_rasServer";
    char* testServerId2 = (char*)"test_rasServer2";
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileShared(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    // exclusive lock
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileExclusive(ecpg_lockmanager, testServerId2, test_tileID, false);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId2, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    // call unlockAllTiles to make sure that no tiles remain locked
    ecpg_lockmanager->beginTransaction(connectionName);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId2);
    ecpg_lockmanager->endTransaction(connectionName);
    std::cout << "test_createDeleteSharedExclusiveLock: end" << endl;
}

/**
 * Function implementing test case 8: create an exclusive lock
 * and then try to create a shared lock on the same tile
 * but coming from two different servers.
 * The first lock should be created, the second one should fail.
 * At the end the locks are deleted.
 */
void test_createDeleteExclusiveSharedLock()
{
    std::cout << "test_createDeleteExclusiveSharedLock: begin" << endl;
    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    // create one exclusive lock by a server and try to get a shared one by another server
    // exclusive lock
    long long test_tileID = -2;
    char* testServerId = (char*)"test_rasServer";
    char* testServerId2 = (char*)"test_rasServer2";
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileExclusive(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    // shared lock
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileShared(ecpg_lockmanager, testServerId2, test_tileID, false);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId2, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    // call unlockAllTiles to make sure that no tiles remain locked
    ecpg_lockmanager->beginTransaction(connectionName);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId2);
    ecpg_lockmanager->endTransaction(connectionName);
    std::cout << "test_createDeleteExclusiveSharedLock: end" << endl;
}

/**
 * Function implementing test case 9: create a shared lock
 * and then try to create an exclusive lock on the same tile
 * and coming from the same server.
 * The first lock should be created, the second one should fail.
 * At the end the lock is deleted.
 */
void test_createDeleteSharedExclusiveLockSameServer()
{
    std::cout << "test_createDeleteSharedExclusiveLockSameServer: begin" << endl;
    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    // create one shared lock by a server and try to get an exclusive one by same server
    // shared lock
    long long test_tileID = -2;
    char* testServerId = (char*)"test_rasServer";
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileShared(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    // exclusive lock
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileExclusive(ecpg_lockmanager, testServerId, test_tileID, false);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    // call unlockAllTiles to make sure that no tiles remain locked
    ecpg_lockmanager->beginTransaction(connectionName);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId);
    ecpg_lockmanager->endTransaction(connectionName);
    std::cout << "test_createDeleteSharedExclusiveLockSameServer: end" << endl;
}

/**
 * Function implementing test case 10: create an exclusive lock
 * and then try to create a shared lock on the same tile
 * and coming from the same server.
 * The first lock should be created, the second one should fail.
 * At the end the lock is deleted.
 */
void test_createDeleteExclusiveSharedLockSameServer()
{
    std::cout << "test_createDeleteExclusiveSharedLockSameServer: begin" << endl;
    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    // create one exclusive lock by a server and try to get a shared one by same server
    // exclusive lock
    long long test_tileID = -2;
    char* testServerId = (char*)"test_rasServer";
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileExclusive(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    // shared lock
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileShared(ecpg_lockmanager, testServerId, test_tileID, false);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    // call unlockAllTiles to make sure that no tiles remain locked
    ecpg_lockmanager->beginTransaction(connectionName);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId);
    ecpg_lockmanager->endTransaction(connectionName);
    std::cout << "test_createDeleteExclusiveSharedLockSameServer: end" << endl;
}

/**
 * Function implementing test case 11: create two exclusive locks
 * on the same tile and coming from the same server.
 * The first lock should be created, the second one should fail.
 * At the end the lock is deleted.
 */
void test_createDelete2ExclusiveLocksSameServer()
{
    std::cout << "test_createDelete2ExclusiveLocksSameServer: begin" << endl;
    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    // create two exclusive locks from the same server
    // first lock
    long long test_tileID = -1;
    char* testServerId = (char*)"test_rasServer";
    lockTestTileExclusive(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    // second lock
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileExclusive(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    // call unlockAllTiles to make sure that no tiles remain locked
    ecpg_lockmanager->beginTransaction(connectionName);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId);
    ecpg_lockmanager->endTransaction(connectionName);
    std::cout << "test_createDelete2ExclusiveLocksSameServer: end" << endl;
}

/**
 * Function implementing test case 12: create two shared locks
 * on the same tile and coming from the same server.
 * The first lock should be created, the second one should fail.
 * At the end the lock is deleted.
 */
void test_createDelete2SharedLocksSameServer()
{
    std::cout << "test_createDelete2SharedLocksSameServer: begin" << endl;
    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    // create two shared locks from the same server
    // first lock
    long long test_tileID = -2;
    char* testServerId = (char*)"test_rasServer";
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileShared(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    // second lock
    ecpg_lockmanager->beginTransaction(connectionName);
    lockTestTileShared(ecpg_lockmanager, testServerId, test_tileID, true);
    ecpg_lockmanager->endTransaction(connectionName);
    ecpg_lockmanager->beginTransaction(connectionName);
    unlockTestTile(ecpg_lockmanager, testServerId, test_tileID);
    ecpg_lockmanager->endTransaction(connectionName);
    // call unlockAllTiles to make sure that no tiles remain locked
    ecpg_lockmanager->beginTransaction(connectionName);
    ecpg_lockmanager->unlockAllTiles(connectionName, testServerId);
    ecpg_lockmanager->endTransaction(connectionName);
    std::cout << "test_createDelete2SharedLocksSameServer: end" << endl;
}

/**
 * Function implementing test case 13: connect to the database via
 * another connection called "otherConn" and the disconnect from it.
 *
 * This is testing that two different connections on the database
 * can run on parallel.
 */
void test_otherDatabaseConnection()
{
    std::cout << "test_otherDatabaseConnection: begin" << endl;
    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    bool connect_ok = ecpg_lockmanager->connect("RASBASE:5432", "otherConn", (const char *)NULL, (const char *)NULL);
    if (!connect_ok)
    {
        std::cout << "Error: Connect to database via connection otherConn not successful." << endl;
    }
    else
    {
        std::cout << "Ok: Connect to database via connection otherConn successful." << endl;
    }
    bool disconnect_ok = ecpg_lockmanager->disconnect("otherConn");
    if (!disconnect_ok)
    {
        std::cout << "Error: Disconnect from database via otherConn not successful." << endl;
    }
    else
    {
        std::cout << "Ok: Disconnect from database via connection otherConn successful." << endl;
    }
    std::cout << "test_otherDatabaseConnection: end" << endl;
}

/**
 * Function implementing the call of all 13 test cases sequentially.
 */
void test_allCases()
{
    test_createDeleteExclusiveLock();
    test_createDeleteSharedLock();
    test_createDelete2ExclusiveLocks();
    test_createDelete2SharedLocks();
    test_createDelete5ExclusiveLocks();
    test_createDelete5SharedLocks();
    test_createDeleteSharedExclusiveLock();
    test_createDeleteExclusiveSharedLock();
    test_createDeleteSharedExclusiveLockSameServer();
    test_createDeleteExclusiveSharedLockSameServer();
    test_createDelete2ExclusiveLocksSameServer();
    test_createDelete2SharedLocksSameServer();
    test_otherDatabaseConnection();
}

int main( int ac, char** av )
{
    // connect to database using user and password
    //bool connect_ok = database_connect("RASBASE:5432", "rasdaman", "rasdaman");

    ECPG_LockManager *ecpg_lockmanager = ECPG_LockManager::Instance();
    // connect to the database without user and password
    bool connect_ok = ecpg_lockmanager->connect("RASBASE:5432", connectionName, (const char *)NULL, (const char *)NULL);
    if (!connect_ok)
    {
        std::cout << "Error: Connect to database not successful." << endl;
    }
    // Test 1: create and delete one exclusive lock
    //test_createDeleteExclusiveLock();

    // Test 2: create and delete one shared lock
    //test_createDeleteSharedLock();

    // Test 3: try to create two exclusive locks on the same tile, delete lock at end
    //test_createDelete2ExclusiveLocks();

    // Test 4: try to create two shared locks on the same tile, delete lock at end
    //test_createDelete2SharedLocks();

    // Test 5: create and delete an exclusive lock 5 times sequentially on the same tile, delete lock at end
    //test_createDelete5ExclusiveLocks();

    // Test 6: create and delete an shared lock 5 times sequentially on the same tile, delete lock at end
    //test_createDelete5SharedLocks();

    // Test 7: create one shared lock, then try to get an exclusive one by another server
    //test_createDeleteSharedExclusiveLock();

    // Test 8: create one exclusive lock, then try to get a shared one by another server
    //test_createDeleteExclusiveSharedLock();

    // Test 9: create one shared lock, then try to get an exclusive one by same server
    //test_createDeleteSharedExclusiveLockSameServer();

    // Test 10: create one exclusive lock, then try to get a shared one by same server
    //test_createDeleteExclusiveSharedLockSameServer();

    // Test 11: try to create two exclusive locks by same server on the same tile, delete lock at end
    //test_createDelete2ExclusiveLocksSameServer();

    // Test 12: try to create two shared locks by same server on the same tile, delete lock at end
    //test_createDelete2SharedLocksSameServer();

    // Test 13: open and close the database via another connection "otherConn"
    //test_databaseConnection();

    // Test all: execute all tests sequentially
    test_allCases();

    bool disconnect_ok = ecpg_lockmanager->disconnect(connectionName);
    if (!disconnect_ok)
    {
        std::cout << "Error: Disconnect from database not successful." << endl;
    }
    return 0;
}
