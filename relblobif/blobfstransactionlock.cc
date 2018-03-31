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

#include "config.h"
#include "blobfstransactionlock.hh"
#include "blobfscommon.hh"
#include "dirwrapper.hh"
#include <logging.hh>

using namespace std;
using namespace blobfs;

const std::string BlobFSTransactionLock::TRANSACTION_LOCK = "transaction.lock";
const std::string BlobFSTransactionLock::TRANSACTION_COMMIT_LOCK = "transaction_commit.lock";
const std::string BlobFSTransactionLock::TRANSACTION_ABORT_LOCK = "transaction_abort.lock";

BlobFSTransactionLock::BlobFSTransactionLock(const std::string& path, bool checkArg)
    : fileStorageTransactionPath(DirWrapper::convertToCanonicalPath(path)), checkOnly(checkArg),
      transactionGeneralLock(fileStorageTransactionPath + TRANSACTION_LOCK),
      transactionCommitLock(fileStorageTransactionPath + TRANSACTION_COMMIT_LOCK),
      transactionAbortLock(fileStorageTransactionPath + TRANSACTION_ABORT_LOCK)
{
}

BlobFSTransactionLock::~BlobFSTransactionLock()
{
    if (!checkOnly)
    {
        clearCommitLock();
        clearAbortLock();
        clearTransactionLock();
    }
}

bool BlobFSTransactionLock::lockForTransaction()
{
    return transactionGeneralLock.lock();
}

bool BlobFSTransactionLock::clearTransactionLock()
{
    return transactionGeneralLock.unlock();
}

bool BlobFSTransactionLock::lockedForTransaction()
{
    return transactionGeneralLock.isLocked();
}

bool BlobFSTransactionLock::transactionLockValid()
{
    return transactionGeneralLock.isValid();
}

bool BlobFSTransactionLock::lockForCommit()
{
    return transactionCommitLock.lock();
}

bool BlobFSTransactionLock::clearCommitLock()
{
    return transactionCommitLock.unlock();
}

bool BlobFSTransactionLock::lockedForCommit()
{
    return transactionCommitLock.isLocked();
}

bool BlobFSTransactionLock::commitLockValid()
{
    return transactionCommitLock.isValid();
}

bool BlobFSTransactionLock::lockForAbort()
{
    return transactionAbortLock.lock();
}

bool BlobFSTransactionLock::clearAbortLock()
{
    return transactionAbortLock.unlock();
}

bool BlobFSTransactionLock::lockedForAbort()
{
    return transactionAbortLock.isLocked();
}

bool BlobFSTransactionLock::abortLockValid()
{
    return transactionAbortLock.isValid();
}
