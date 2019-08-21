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

#include "blobfstransactionlock.hh"
#include "dirwrapper.hh"  // for DirWrapper

using namespace std;

const std::string BlobFSTransactionLock::TRANSACTION_LOCK = "transaction.lock";
const std::string BlobFSTransactionLock::TRANSACTION_COMMIT_LOCK = "transaction_commit.lock";
const std::string BlobFSTransactionLock::TRANSACTION_ABORT_LOCK = "transaction_abort.lock";

BlobFSTransactionLock::BlobFSTransactionLock(const std::string &path, bool check)
    : transactionPath(DirWrapper::toCanonicalPath(path)), checkOnly(check),
      locks{LockFile{transactionPath + TRANSACTION_LOCK},
            LockFile{transactionPath + TRANSACTION_COMMIT_LOCK},
            LockFile{transactionPath + TRANSACTION_ABORT_LOCK}}
{
}

BlobFSTransactionLock::~BlobFSTransactionLock()
{
    if (!checkOnly)
    {
        clear(TransactionLockType::Commit);
        clear(TransactionLockType::Abort);
        clear(TransactionLockType::General);
    }
}

LockFile &BlobFSTransactionLock::getLock(TransactionLockType lockType)
{
    return locks[static_cast<size_t>(lockType)];
}

bool BlobFSTransactionLock::lock(TransactionLockType lockType)
{
    return getLock(lockType).lock();
}

bool BlobFSTransactionLock::clear(TransactionLockType lockType)
{
    return getLock(lockType).unlock();
}

bool BlobFSTransactionLock::isLocked(TransactionLockType lockType)
{
    return getLock(lockType).isLocked();
}

bool BlobFSTransactionLock::isValid(TransactionLockType lockType)
{
    return getLock(lockType).isValid();
}
