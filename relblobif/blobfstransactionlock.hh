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
 * Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#pragma once

#include <string>
#include "lockfile.hh"

/**
 * @brief Distinguish the transaction lock type.
 * 
 * A transaction lock can be one of:
 * 
 *  1. *General*: distinguish whether rasserver process is running or not
 *  2. *Commit*: a transaction is being committed
 *  3. *Abort*: a transaction is being aborted
 */
enum class TransactionLockType
{
    General,
    Commit,
    Abort,
    Invalid // always (!) keep as last item (used in BlobFSTransactionLock)
};

/**
 * Manage locking on a particular transaction.
 *
 * The transaction is continuously alive while the server is alive, and during
 * this time a general lock A is held.
 *
 * While the transaction is being committed, lock A is extended with lock B.
 * If the server crashes, and a cleanup function finds a lock B, then it should
 * complete the commit in order to restore to a valid database state.
 *
 * While the transaction is being aborted, lock A is extended with lock C.
 * If the server crashes, and a later cleanup function finds a lock C, then
 * it should complete the abort in order to restore to a valid database state.
 */
class BlobFSTransactionLock
{
    friend class TestBlobFS;
    friend class TestBlobFSTransaction;
    friend class TestBlobFSTransactionLock;

public:
    /**
     * Initialize lock files in the transaction directory trPath.
     *
     * @param trPath the root directory of the transaction.
     * @param trLocksPath path to the lock file
     * @param check use this object for checking (do not clear locks in destructor)
     */
    BlobFSTransactionLock(const std::string &trPath,
                          const std::string &trLocksPath,
                          bool check = false);

    /**
     * Clears the transaction lock, which should be a general lock (A)
     */
    ~BlobFSTransactionLock();

    /**
     * Enable the specified lock.
     * @return true on success, false otherwise.
     */
    bool lock(TransactionLockType lockType);
    /**
     * Unlock and remove the specified lock.
     * @return true on success, false otherwise.
     */
    bool clear(TransactionLockType lockType);
    /**
     * @return true if the specified lock is in place, false otherwise.
     */
    bool isLocked(TransactionLockType lockType);
    /**
     * @return true if the specified lock is valid:
     *  - lock file exists and is locked, or
     *  - lock file !exists
     */
    bool isValid(TransactionLockType lockType);

private:
    LockFile &getLock(TransactionLockType lockType);

    // transaction root path
    std::string transactionPath;
    
    // /tmp/rasdaman_transaction_locks/<transactionPath>
    std::string transactionLockPath;

    // check only locks, do not clear them in destructor
    bool checkOnly{false};

    // locks (General, Commit, Abort)
    LockFile locks[static_cast<size_t>(TransactionLockType::Invalid)];

    // general transaction in progress lock (A)
    static const std::string TRANSACTION_LOCK;
    // commit transaction lock (B)
    static const std::string TRANSACTION_COMMIT_LOCK;
    // abort transaction lock (C)
    static const std::string TRANSACTION_ABORT_LOCK;
};
