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
#include <cstdlib>
#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>
#include <unistd.h>

#include "raslib/rminit.hh"
#include "relblobif/blobfs.hh"
#include "relblobif/blobfstransaction.hh"
#include "relblobif/blobfile.hh"
#include "reladminif/adminif.hh"
#include "reladminif/databaseif.hh"
#include "reladminif/transactionif.hh"
#include "testing.h"

#include "loggingutils.hh"

using namespace std;

// define external vars
char globalConnectId[256] = "/tmp/rasdata/RASBASE";
char globalDbUser[255] = {0};
char globalDbPasswd[255] = {0};
class MDDColl;
MDDColl *mddConstants = 0;              // used in QtMDD
unsigned long maxTransferBufferSize = 4000000;
int noTimeOut = 0;

INITIALIZE_EASYLOGGINGPP

RMINITGLOBALS('C')

class TestBlobFS
{

public:

    TestBlobFS()
        : config("", "", "")
    {
    }

    void testNestedRootPath()
    {
        const string expected("/tmp/rasdata/TILES/");
        EXPECT_EQ(BlobFS::getInstance().config.tilesPath, expected);
    }

    void testGetPathNested()
    {
        try
        {
            BlobFS::getInstance().insertTransaction->getFinalBlobPath(-1);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }

        try
        {
            BlobFS::getInstance().insertTransaction->getFinalBlobPath(0);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }

        EXPECT_EQ(BlobFS::getInstance().insertTransaction->getFinalBlobPath(1000), "/tmp/rasdata/TILES/0/0/1000");
        EXPECT_EQ(BlobFS::getInstance().insertTransaction->getFinalBlobPath(16383), "/tmp/rasdata/TILES/0/0/16383");
        EXPECT_EQ(BlobFS::getInstance().insertTransaction->getFinalBlobPath(16384), "/tmp/rasdata/TILES/0/1/16384");
        EXPECT_EQ(BlobFS::getInstance().insertTransaction->getFinalBlobPath(32768), "/tmp/rasdata/TILES/0/2/32768");
        EXPECT_EQ(BlobFS::getInstance().insertTransaction->getFinalBlobPath(268435455), "/tmp/rasdata/TILES/0/16383/268435455");
        EXPECT_EQ(BlobFS::getInstance().insertTransaction->getFinalBlobPath(268435456), "/tmp/rasdata/TILES/1/16384/268435456");
        EXPECT_EQ(BlobFS::getInstance().insertTransaction->getFinalBlobPath(4294967296), "/tmp/rasdata/TILES/16/262144/4294967296");
        EXPECT_EQ(BlobFS::getInstance().insertTransaction->getFinalBlobPath(9223372036854775807ll), "/tmp/rasdata/TILES/34359738367/562949953421311/9223372036854775807");
    }

    void testInsert()
    {
        BlobData blobData(4294967296, 4, "test");
        BlobFS::getInstance().insert(blobData);
        commit();

        const char* expectedFilePath = "/tmp/rasdata/TILES/16/262144/4294967296";
        struct stat status;
        EXPECT_TRUE(stat(expectedFilePath, &status) == 0);
        EXPECT_TRUE(unlink(expectedFilePath) == 0);
    }

    void testInvalidOidInsert()
    {
        try
        {
            BlobData blobData(-100, 4, "test");
            BlobFS::getInstance().insert(blobData);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }
    }

    void testInvalidDataInsert()
    {
        try
        {
            BlobData blobData(100, 4, NULL);
            BlobFS::getInstance().insert(blobData);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), FAILEDWRITINGTODISK);
        }
    }

    void testRetrieve()
    {
        char* testData = "test";
        r_Bytes testDataSize = 4;
        long long blobId = 4294967296;

        BlobData blobData(blobId, testDataSize, testData);
        BlobFS::getInstance().insert(blobData);
        commit();

        BlobData retreivedBlobData(blobId);
        BlobFS::getInstance().select(retreivedBlobData);
        EXPECT_EQ(testDataSize, retreivedBlobData.size);
        EXPECT_TRUE(memcmp(testData, retreivedBlobData.data, testDataSize) == 0);
        free(retreivedBlobData.data);
        const char* expectedFilePath = "/tmp/rasdata/TILES/16/262144/4294967296";
        EXPECT_TRUE(unlink(expectedFilePath) == 0);
    }

    void testNonExistingTileRetrieve()
    {
        try
        {
            BlobData blobData(4294967297);
            BlobFS::getInstance().select(blobData);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }
    }

    void testInvalidOidRetrieve()
    {
        try
        {
            BlobData blobData(-10000);
            BlobFS::getInstance().select(blobData);
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }
    }

    void testUpdate()
    {
        long long blobId = 4294967296;

        BlobData blobData(blobId, 4, "test");
        BlobFS::getInstance().insert(blobData);
        commit();

        const char* expectedFilePath = "/tmp/rasdata/TILES/16/262144/4294967296";
        struct stat status;
        EXPECT_TRUE(stat(expectedFilePath, &status) == 0);

        char* newData = "test2";
        r_Bytes newDataSize = 5;
        BlobData newBlobData(blobId, newDataSize, newData);
        BlobFS::getInstance().update(newBlobData);
        commit();

        BlobData retreivedBlobData(blobId);
        BlobFS::getInstance().select(retreivedBlobData);
        EXPECT_EQ(retreivedBlobData.size, newDataSize);
        EXPECT_TRUE(memcmp(newData, retreivedBlobData.data, newDataSize) == 0);
        free(retreivedBlobData.data);

        EXPECT_TRUE(unlink(expectedFilePath) == 0);
    }

    void testNonExistingOidUpdate()
    {
        long long blobId = 4294967295;

        try
        {
            auto expectedFilePath = BlobFS::getInstance().insertTransaction->getFinalBlobPath(blobId);
            unlink(expectedFilePath.c_str());
            BlobData blobData(blobId, 4, "test");
            BlobFS::getInstance().update(blobData);
            commit();
            struct stat status;
            EXPECT_TRUE(BlobFile::fileExists(expectedFilePath));
        }
        catch (r_Error& err)
        {
            TEST_FAIL();
        }
    }

    void testInvalidDataUpdate()
    {
        long long blobId = 4294967296;

        BlobData blobData(blobId, 4, "test");
        BlobFS::getInstance().insert(blobData);
        commit();

        const char* expectedFilePath = "/tmp/rasdata/TILES/16/262144/4294967296";
        try
        {
            BlobData newBlobData(blobId, 5, NULL);
            BlobFS::getInstance().update(newBlobData);
            commit();
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), FAILEDWRITINGTODISK);
        }
        EXPECT_TRUE(unlink(expectedFilePath) == 0);
    }

    void testRemove()
    {
        BlobData blobData(4294967296, 4, "test");
        BlobFS::getInstance().insert(blobData);
        commit();

        BlobFS::getInstance().remove(blobData);
        commit();

        struct stat status;
        EXPECT_FALSE(stat("/tmp/rasdata/TILES/16/262144/4294967296", &status) == 0);
    }

    void testNonExistingTileRemove()
    {
        try
        {
            BlobData blobData(1092893937192);
            BlobFS::getInstance().remove(blobData);
            commit();
//            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }
    }

    void testInvalidOidRemove()
    {
        try
        {
            BlobData blobData(-1);
            BlobFS::getInstance().remove(blobData);
            commit();
            TEST_FAIL();
        }
        catch (r_Error& err)
        {
            EXPECT_EQ(err.get_errorno(), BLOBFILENOTFOUND);
        }
    }

    void testGetPathPerformance()
    {
        Test::startTimer();
        for (long long blobId = 1; blobId < 10000000; blobId++)
        {
            const string blobPath = BlobFS::getInstance().insertTransaction->getFinalBlobPath(blobId);
        }
        double runtime = Test::stopTimer();
        LOG << "test running time: " << runtime << endl;
    }

    void testGetTmpBlobPath()
    {
        try
        {
            const string ret1 = BlobFS::getInstance().insertTransaction->getTmpBlobPath(1);
            LOG << "insert subdir: " << ret1 << endl;
            const string exp1("/tmp/rasdata/TRANSACTIONS/insert.");
            EXPECT_EQ(ret1.substr(0, exp1.length()), exp1);

            const string ret2 = BlobFS::getInstance().updateTransaction->getTmpBlobPath(1);
            LOG << "update subdir: " << ret2 << endl;
            const string exp2("/tmp/rasdata/TRANSACTIONS/update.");
            EXPECT_EQ(ret2.substr(0, exp2.length()), exp2);

            const string ret3 = BlobFS::getInstance().removeTransaction->getTmpBlobPath(1);
            LOG << "remove subdir: " << ret3 << endl;
            const string exp3("/tmp/rasdata/TRANSACTIONS/remove.");
            EXPECT_EQ(ret3.substr(0, exp3.length()), exp3);
        }
        catch (r_Error& err)
        {
            TEST_FAIL();
        }
    }

    void testFinalizeUncompletedTransactions_InsertTransactionInProgress()
    {
        long long blobId = 4294967294;
        BlobData blobData(blobId, 4, "test");
        BlobFS::getInstance().insert(blobData);

        const string expectedFilePath = BlobFS::getInstance().insertTransaction->getFinalBlobPath(blobId);
        const string tmpFilePath = BlobFS::getInstance().insertTransaction->getTmpBlobPath(blobId);
        EXPECT_FALSE(BlobFile::fileExists(expectedFilePath));
        EXPECT_TRUE(BlobFile::fileExists(tmpFilePath));

        BlobFS::getInstance().finalizeUncompletedTransactions();
        EXPECT_FALSE(BlobFile::fileExists(expectedFilePath));
        EXPECT_TRUE(BlobFile::fileExists(tmpFilePath));

        abort();
    }

    void testFinalizeUncompletedTransactions_InsertTransactionCrash()
    {
        long long blobId = 4294967293;
        BlobData blobData(blobId, 4, "test");
        BlobFS::getInstance().insert(blobData);

        const string expectedFilePath = BlobFS::getInstance().insertTransaction->getFinalBlobPath(blobId);
        const string tmpFilePath = BlobFS::getInstance().insertTransaction->getTmpBlobPath(blobId);
        EXPECT_FALSE(BlobFile::fileExists(expectedFilePath));
        EXPECT_TRUE(BlobFile::fileExists(tmpFilePath));

        BlobFS::getInstance().insertTransaction->transactionLock->clear(TransactionLockType::General);

        BlobFS::getInstance().finalizeUncompletedTransactions();
        EXPECT_FALSE(BlobFile::fileExists(expectedFilePath));
        EXPECT_FALSE(BlobFile::fileExists(tmpFilePath));

        BlobFS::getInstance().insertTransaction.reset(new BlobFSInsertTransaction(config));
    }

    void testFinalizeUncompletedTransactions_InsertTransactionCrash2()
    {
        long long blobId = 12294967292;
        BlobData blobData(blobId, 4, "test");
        BlobFS::getInstance().insert(blobData);

        const string expectedFilePath = BlobFS::getInstance().insertTransaction->getFinalBlobPath(blobId);
        const string tmpFilePath = BlobFS::getInstance().insertTransaction->getTmpBlobPath(blobId);
        EXPECT_FALSE(BlobFile::fileExists(expectedFilePath));
        EXPECT_TRUE(BlobFile::fileExists(tmpFilePath));

        BlobFS::getInstance().insertTransaction->transactionLock->clear(TransactionLockType::General);
        string lockFilePath = BlobFS::getInstance().insertTransaction->transactionPath +
                              BlobFSTransactionLock::TRANSACTION_LOCK;
        int fd = open(lockFilePath.c_str(), O_CREAT | O_WRONLY, 0660);
        close(fd);

        BlobFS::getInstance().finalizeUncompletedTransactions();
        EXPECT_FALSE(BlobFile::fileExists(expectedFilePath));
        EXPECT_FALSE(BlobFile::fileExists(tmpFilePath));

        BlobFS::getInstance().insertTransaction.reset(new BlobFSInsertTransaction(config));
    }

    void testFinalizeUncompletedTransactions_InsertTransactionCrashDuringCommit()
    {
        long long blobId = 12294967292;
        BlobData blobData(blobId, 4, "test");
        BlobFS::getInstance().insert(blobData);

        const string expectedFilePath = BlobFS::getInstance().insertTransaction->getFinalBlobPath(blobId);
        const string tmpFilePath = BlobFS::getInstance().insertTransaction->getTmpBlobPath(blobId);
        EXPECT_FALSE(BlobFile::fileExists(expectedFilePath));
        EXPECT_TRUE(BlobFile::fileExists(tmpFilePath));

        // create fake (unlocked) commit lock simulating crash in the server during the commit
        BlobFS::getInstance().insertTransaction->transactionLock->clear(TransactionLockType::Commit);
        string lockFilePath = BlobFS::getInstance().insertTransaction->transactionPath +
                              BlobFSTransactionLock::TRANSACTION_COMMIT_LOCK;
        int fd = open(lockFilePath.c_str(), O_CREAT | O_WRONLY, 0660);
        close(fd);
        BlobFS::getInstance().insertTransaction->transactionLock->clear(TransactionLockType::General);
        string lockTransactionFilePath = BlobFS::getInstance().insertTransaction->transactionPath +
                                         BlobFSTransactionLock::TRANSACTION_LOCK;
        fd = open(lockTransactionFilePath.c_str(), O_CREAT | O_WRONLY, 0660);
        close(fd);

        BlobFS::getInstance().finalizeUncompletedTransactions();
        EXPECT_TRUE(BlobFile::fileExists(expectedFilePath)); // commit is finalized now
        EXPECT_FALSE(BlobFile::fileExists(tmpFilePath));
        EXPECT_FALSE(BlobFile::fileExists(lockFilePath));

        BlobFS::getInstance().insertTransaction.reset(new BlobFSInsertTransaction(config));

        unlink(expectedFilePath.c_str());
    }

    void testFinalizeUncompletedTransactions_InsertTransactionCrashDuringAbort()
    {
        long long blobId = 12294967291;
        BlobData blobData(blobId, 4, "test");
        BlobFS::getInstance().insert(blobData);

        const string expectedFilePath = BlobFS::getInstance().insertTransaction->getFinalBlobPath(blobId);
        const string tmpFilePath = BlobFS::getInstance().insertTransaction->getTmpBlobPath(blobId);
        EXPECT_FALSE(BlobFile::fileExists(expectedFilePath));
        EXPECT_TRUE(BlobFile::fileExists(tmpFilePath));

        // create fake (unlocked) commit lock simulating crash in the server during the commit
        BlobFS::getInstance().insertTransaction->transactionLock->clear(TransactionLockType::Commit);
        string lockFilePath = BlobFS::getInstance().insertTransaction->transactionPath +
                              BlobFSTransactionLock::TRANSACTION_ABORT_LOCK;
        int fd = open(lockFilePath.c_str(), O_CREAT | O_WRONLY, 0660);
        close(fd);
        BlobFS::getInstance().insertTransaction->transactionLock->clear(TransactionLockType::General);
        string lockTransactionFilePath = BlobFS::getInstance().insertTransaction->transactionPath +
                                         BlobFSTransactionLock::TRANSACTION_LOCK;
        fd = open(lockTransactionFilePath.c_str(), O_CREAT | O_WRONLY, 0660);
        close(fd);

        BlobFS::getInstance().finalizeUncompletedTransactions();
        EXPECT_FALSE(BlobFile::fileExists(expectedFilePath));
        EXPECT_FALSE(BlobFile::fileExists(tmpFilePath));
        EXPECT_FALSE(BlobFile::fileExists(lockFilePath));

        BlobFS::getInstance().insertTransaction.reset(new BlobFSInsertTransaction(config));

        unlink(expectedFilePath.c_str());
    }

    void prepareRun()
    {
        system("rm -rf /tmp/rasdata");
        char* rasdataEnvVar = const_cast<char*>("RASDATA=/tmp/rasdata");
        putenv(rasdataEnvVar);
        mkdir("/tmp/rasdata", 0770);
        unlink(globalConnectId);
        db.createDB("", "", "");
        AdminIf* myAdmin = AdminIf::instance();
        db.open(globalConnectId);
        ta.begin(&db);

        BlobFSConfig tmpConfig(BlobFS::getInstance().config.rootPath,
                               BlobFS::getInstance().config.tilesPath,
                               BlobFS::getInstance().config.transactionsPath);
        config = tmpConfig;
    }

    void finishRun()
    {
        ta.commit();
        db.close();
    }

    void commit()
    {
        BlobFS::getInstance().preRasbaseCommit();
        BlobFS::getInstance().postRasbaseCommit();
    }

    void abort()
    {
        BlobFS::getInstance().postRasbaseAbort();
    }

private:
    BlobFSConfig config;
    DatabaseIf db;
    TransactionIf ta;
};

int main(int argc, char** argv)
{
#ifndef BASEDB_SQLITE
    cerr << "testsuite runs only on SQLite / Filestorage rasdaman." << endl;
    return 0;
#endif
    common::LogConfiguration defaultConf;
    defaultConf.configClientLogging();

    TestBlobFS test;
    test.prepareRun();

    RUN_TEST(test.testNestedRootPath());
    RUN_TEST(test.testGetPathNested());
    RUN_TEST(test.testGetTmpBlobPath());

    RUN_TEST(test.testInsert());
    RUN_TEST(test.testInvalidOidInsert());
    RUN_TEST(test.testInvalidDataInsert());

    RUN_TEST(test.testRetrieve());
    RUN_TEST(test.testNonExistingTileRetrieve());
    RUN_TEST(test.testInvalidOidRetrieve());

    RUN_TEST(test.testUpdate());
    RUN_TEST(test.testNonExistingOidUpdate());
    RUN_TEST(test.testInvalidDataUpdate());

    RUN_TEST(test.testRemove());
    RUN_TEST(test.testNonExistingTileRemove());
    RUN_TEST(test.testInvalidOidRemove());

//    RUN_TEST(test.testGetPathPerformance());

    RUN_TEST(test.testFinalizeUncompletedTransactions_InsertTransactionInProgress());
    RUN_TEST(test.testFinalizeUncompletedTransactions_InsertTransactionCrash());
    RUN_TEST(test.testFinalizeUncompletedTransactions_InsertTransactionCrash2());
    RUN_TEST(test.testFinalizeUncompletedTransactions_InsertTransactionCrashDuringCommit());
    RUN_TEST(test.testFinalizeUncompletedTransactions_InsertTransactionCrashDuringAbort());

    test.finishRun();

    return Test::getResult();
}
