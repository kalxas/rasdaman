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

#include "blobfile.hh"
#include "blobfscommon.hh"
#include "blobfstransactionlock.hh"

#include <string>  // for string
#include <vector>  // for vector
#include <memory>  // for unique_ptr

class BlobFSConfig;


/**
 * Handles blob file storage transactions: insert, remove, update. This is a
 * base class holding common functionality; subclasses implement the specific
 * commit/abort procedures.
 *
 * Every transaction has a separate working directory under
 * $RASDATA/TRANSACTIONS, e.g. insert.XXXXXX, update.XXXXXX, ...
 *
 * Commit/abort are atomic -- in case of a crash a broken state is accordingly
 * completed (either fully committed/aborted or rolled back) when the server is
 * restarted. Every rasserver process at startup scans the TRANSACTION directory
 * at startup, the first to notice an issue does the cleanup.
 */
class BlobFSTransaction
{
    friend class TestBlobFSTransaction;
    friend class TestBlobFS;

public:
    /// Initialize given storage paths. The transaction directory trPath is
    /// typically empty, and is initialized here.
    BlobFSTransaction(BlobFSConfig &config,
                      const std::string &transactionDir,
                      const std::string &trPath);

    /// Clears any transaction locks and removes the transaction directory in
    /// $RASDATA/TRANSACTIONS.
    virtual ~BlobFSTransaction();

    /// Add blob data to the list of pending operations to be executed in this
    /// transaction.
    virtual void add(BlobData &blobData) = 0;

    /**
     * To be called before committing to RASBASE. This is utilized only on
     * remove transactions -- files are moved from TILES to the transaction dir
     * in this step.
     */
    virtual void preRasbaseCommit();

    /// To be called after committing to RASBASE; all file operations are
    /// finalized here.
    virtual void postRasbaseCommit();

    /// To be called after ROLLBACK on RASBASE.
    virtual void postRasbaseAbort();

    /// Finalize an interrupted transaction (e.g. by a crash).
    void finalizeUncompleted();

    /// Given a blob ID return its absolute file path in the final 
    /// $RASDATA/TILES location.
    std::string getFinalBlobPath(long long blobId);

    /// Given a blob ID return its absolute file path in the temporary 
    /// transaction directory (under $RASDATA/TRANSACTIONS).
    std::string getTmpBlobPath(long long blobId);

    /// Return the right transaction object, based on the given transaction path;
    /// Return NULL in case of invalid path.
    static BlobFSTransaction *getBlobFSTransaction(
        const std::string &transactionPath, BlobFSConfig &config);

protected:
    BlobFSTransaction(BlobFSConfig &config);

    /**
     * Finalize a transaction that crashed during a commit/abort on RASBASE.
     * Determining if a commit/abort on RASBASE has actually succeeded or
     * rollbacked during a crash does not seem possible, so this checks whether
     * every blob in the transaction directory exists in RASBASE as well, in
     * order to decide whether to remove it or revert it.
     */
    void finalizeRasbaseCrash();

    /// Add the blob files in the temp transaction directory to the pending 
    /// blobIds.
    void collectBlobIds();

    /// Add a blob file path to the list of pending blobIds; returns true if
    /// successful (blob file is a number > 0)
    bool addBlobId(const std::string &blobPath);

    /// Create and lock a temporary transaction directory under 
    /// $RASDATA/TRANSACTIONS for a given transaction type
    void initTransactionDirectory(const std::string &transactionType);
    
    /// Create temporary transaction directory under $RASDATA/TRANSACTIONS
    /// for a given transaction type
    /// @throws r_Error if it failes to create the temp directory.
    void createTransactionDir(const std::string &trSubdir);
    
    void createGeneralLock();
    
    /// Check if the created transaction dir is valid.
    void validateTransactionDir(const std::string &trSubdir, bool first);

    BlobFSConfig &config;

    /// Root file storage path, tiles can be organized in a flat-file scheme, or
    /// nested in subdirectories in subdir TILES
    std::string transactionPath;

    /// Blob ids participating in the current transaction.
    std::vector<long long> blobIds;

    /// Underlying transaction lock handler, used in the commit/abort handlers.
    std::unique_ptr<BlobFSTransactionLock> transactionLock;

    static const std::string INSERT_TRANSACTIONS_SUBDIR;
    static const std::string UPDATE_TRANSACTIONS_SUBDIR;
    static const std::string REMOVE_TRANSACTIONS_SUBDIR;
    
    // number of directories to create per directory in TILES
    static const long tileDirsPerDir; 
    // number of tiles to store per leaf directory in TILES
    static const long tilesPerDir;

    static const long long INVALID_DIR_INDEX = -1;

};

class BlobFSInsertTransaction : public BlobFSTransaction
{
public:
    BlobFSInsertTransaction(BlobFSConfig &config,
                            const std::string &trPath = std::string());
    void add(BlobData &blobData) override;
    /// To be called after commit to RASBASE
    void postRasbaseCommit() override;
    /// To be called before abort to RASBASE
    void postRasbaseAbort() override;
};

class BlobFSUpdateTransaction : public BlobFSTransaction
{
public:
    BlobFSUpdateTransaction(BlobFSConfig &config,
                            const std::string &trPath = std::string());
    void add(BlobData &blobData) override;
    /// To be called after commit to RASBASE
    void postRasbaseCommit() override;
    /// To be called before abort to RASBASE
    void postRasbaseAbort() override;
};

class BlobFSRemoveTransaction : public BlobFSTransaction
{
public:
    BlobFSRemoveTransaction(BlobFSConfig &config,
                            const std::string &trPath = std::string());
    void add(BlobData &blobData) override;
    /// To be called before commit to RASBASE
    void preRasbaseCommit() override;
    /// To be called after commit to RASBASE
    void postRasbaseCommit() override;
    /// To be called before abort to RASBASE
    void postRasbaseAbort() override;
};

class BlobFSSelectTransaction : public BlobFSTransaction
{
public:
    BlobFSSelectTransaction(BlobFSConfig &config);
    void add(BlobData &blobData) override;
};

