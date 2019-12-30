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
#include "version.h"
#include <string>
#include <cstdlib>
#include <sys/stat.h>
#include <unistd.h>

#include "raslib/rminit.hh"
#include "relblobif/blobfstransactionlock.hh"
#include "relblobif/blobfile.hh"
#include "relblobif/dirwrapper.hh"
#include "testing.h"

#include "loggingutils.hh"

using namespace std;

// define external vars
char globalConnectId[256] = "/tmp/rasdata/RASBASE";
char globalDbUser[255] = {0};
char globalDbPasswd[255] = {0};

class MDDColl;
MDDColl *mddConstants = 0; // used in QtMDD
unsigned long maxTransferBufferSize = 4000000;
int noTimeOut = 0;

INITIALIZE_EASYLOGGINGPP

RMINITGLOBALS('C')

class TestBlobFSTransactionLock
{

public:

    void testLockFile()
    {
        string lockFilePath("/tmp/rasdata/test.lock");
        LockFile lockFile(lockFilePath);
        EXPECT_EQ(lockFile.fd, -1);
        EXPECT_EQ(lockFile.lockFilePath, lockFilePath);

        EXPECT_TRUE(lockFile.lock());
        EXPECT_TRUE(BlobFile::fileExists(lockFilePath));

        EXPECT_FALSE(lockFile.lock());
        EXPECT_TRUE(lockFile.isLocked());

        EXPECT_TRUE(lockFile.unlock());
        EXPECT_FALSE(BlobFile::fileExists(lockFilePath));
        EXPECT_FALSE(lockFile.isLocked());
        EXPECT_FALSE(BlobFile::fileExists(lockFilePath));
        EXPECT_FALSE(lockFile.unlock());
        EXPECT_FALSE(BlobFile::fileExists(lockFilePath));
    }

    void testLockFileInvalidPath()
    {
        string lockFilePath("/tmp/rasdata_notexisting/test.lock");
        LockFile lockFile(lockFilePath);

        EXPECT_FALSE(lockFile.lock());
        EXPECT_FALSE(BlobFile::fileExists(lockFilePath));
        EXPECT_FALSE(lockFile.isLocked());
        EXPECT_FALSE(lockFile.unlock());
    }

    void testBlobFileStorageTransactionLock()
    {
        string transactionPath("/tmp/rasdata/insert.1234567");
        DirWrapper::createDirectory(transactionPath);

        string trLockPath(transactionPath + "/" + BlobFSTransactionLock::TRANSACTION_LOCK);
        string commitLockPath(transactionPath + "/" + BlobFSTransactionLock::TRANSACTION_COMMIT_LOCK);
        string abortLockPath(transactionPath + "/" + BlobFSTransactionLock::TRANSACTION_ABORT_LOCK);

        BlobFSTransactionLock* transactionLock = new BlobFSTransactionLock(transactionPath);

        EXPECT_FALSE(BlobFile::fileExists(trLockPath));
        EXPECT_FALSE(BlobFile::fileExists(commitLockPath));
        EXPECT_FALSE(BlobFile::fileExists(abortLockPath));

        EXPECT_TRUE(transactionLock->lock(TransactionLockType::General));
        EXPECT_TRUE(BlobFile::fileExists(trLockPath));
        EXPECT_TRUE(transactionLock->isLocked(TransactionLockType::General));

        EXPECT_TRUE(transactionLock->lock(TransactionLockType::Commit));
        EXPECT_TRUE(BlobFile::fileExists(commitLockPath));
        EXPECT_TRUE(transactionLock->isLocked(TransactionLockType::Commit));

        EXPECT_TRUE(transactionLock->lock(TransactionLockType::Abort));
        EXPECT_TRUE(BlobFile::fileExists(abortLockPath));
        EXPECT_TRUE(transactionLock->isLocked(TransactionLockType::Abort));

        EXPECT_TRUE(transactionLock->clear(TransactionLockType::General));
        EXPECT_TRUE(transactionLock->clear(TransactionLockType::Commit));
        EXPECT_TRUE(transactionLock->clear(TransactionLockType::Abort));

        EXPECT_FALSE(BlobFile::fileExists(trLockPath));
        EXPECT_FALSE(BlobFile::fileExists(commitLockPath));
        EXPECT_FALSE(BlobFile::fileExists(abortLockPath));

        EXPECT_TRUE(transactionLock->lock(TransactionLockType::General));
        EXPECT_TRUE(transactionLock->lock(TransactionLockType::Commit));
        EXPECT_TRUE(transactionLock->lock(TransactionLockType::Abort));

        delete transactionLock;

        EXPECT_FALSE(BlobFile::fileExists(trLockPath));
        EXPECT_FALSE(BlobFile::fileExists(commitLockPath));
        EXPECT_FALSE(BlobFile::fileExists(abortLockPath));

        DirWrapper::removeDirectory(transactionPath);
    }

    void testBlobFileStorageTransactionLockInvalidTransactionPath()
    {
        string transactionPath("/tmp/rasdata/insert.1234567");
        string trLockPath(transactionPath + "/" + BlobFSTransactionLock::TRANSACTION_LOCK);
        string commitLockPath(transactionPath + "/" + BlobFSTransactionLock::TRANSACTION_COMMIT_LOCK);
        string abortLockPath(transactionPath + "/" + BlobFSTransactionLock::TRANSACTION_ABORT_LOCK);

        BlobFSTransactionLock* transactionLock = new BlobFSTransactionLock(transactionPath);

        EXPECT_FALSE(transactionLock->lock(TransactionLockType::General));
        EXPECT_FALSE(BlobFile::fileExists(trLockPath));
        EXPECT_FALSE(transactionLock->isLocked(TransactionLockType::General));

        EXPECT_FALSE(transactionLock->lock(TransactionLockType::Commit));
        EXPECT_FALSE(BlobFile::fileExists(commitLockPath));
        EXPECT_FALSE(transactionLock->isLocked(TransactionLockType::Commit));

        EXPECT_FALSE(transactionLock->lock(TransactionLockType::Abort));
        EXPECT_FALSE(BlobFile::fileExists(abortLockPath));
        EXPECT_FALSE(transactionLock->isLocked(TransactionLockType::Abort));

        EXPECT_FALSE(transactionLock->clear(TransactionLockType::General));
        EXPECT_FALSE(transactionLock->clear(TransactionLockType::Commit));
        EXPECT_FALSE(transactionLock->clear(TransactionLockType::Abort));

        EXPECT_FALSE(BlobFile::fileExists(trLockPath));
        EXPECT_FALSE(BlobFile::fileExists(commitLockPath));
        EXPECT_FALSE(BlobFile::fileExists(abortLockPath));

        EXPECT_FALSE(transactionLock->lock(TransactionLockType::General));
        EXPECT_FALSE(transactionLock->lock(TransactionLockType::Commit));
        EXPECT_FALSE(transactionLock->lock(TransactionLockType::Abort));

        delete transactionLock;

        EXPECT_FALSE(BlobFile::fileExists(trLockPath));
        EXPECT_FALSE(BlobFile::fileExists(commitLockPath));
        EXPECT_FALSE(BlobFile::fileExists(abortLockPath));
    }

    void prepareRun()
    {
        system("rm -rf /tmp/rasdata");
        mkdir("/tmp/rasdata", 0770);
    }

    void finishRun()
    {
        system("rm -rf /tmp/rasdata");
    }

};

int main(int argc, char** argv)
{
#ifndef BASEDB_SQLITE
    cerr << "testsuite runs only on SQLite / Filestorage rasdaman." << endl;
    return 0;
#endif

    common::LogConfiguration defaultConf;
    defaultConf.configClientLogging();

    TestBlobFSTransactionLock test;
    test.prepareRun();

    RUN_TEST(test.testLockFile());
    RUN_TEST(test.testLockFileInvalidPath());

    RUN_TEST(test.testBlobFileStorageTransactionLock());
    RUN_TEST(test.testBlobFileStorageTransactionLockInvalidTransactionPath());

    test.finishRun();

    return Test::getResult();
}
