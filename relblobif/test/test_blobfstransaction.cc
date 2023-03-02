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

#include "relblobif/blobfstransactionlock.hh"
#include "relblobif/blobfile.hh"
#include "relblobif/dirwrapper.hh"
#include "relblobif/blobfs.hh"
#include "testing.h"

#include "loggingutils.hh"

using namespace std;

// define external vars
char globalConnectId[256] = "/tmp/rasdata/RASBASE";
char globalDbUser[255] = {0};
char globalDbPasswd[255] = {0};

class MDDColl;
MDDColl *mddConstants = 0;  // used in QtMDD
int noTimeOut = 0;
char testData[] = {'t', 'e', 's', 't'};

INITIALIZE_EASYLOGGINGPP

class TestBlobFSTransaction
{
public:
    TestBlobFSTransaction()
        : config(string("/tmp/rasdata/"), string("/tmp/rasdata/TILES/"),
                 string("/tmp/rasdata/TRANSACTIONS/"), "/tmp/rasdata")
    {
        system("rm -rf /tmp/rasdata");
        mkdir(config.rootPath.c_str(), 0770);
        mkdir(config.transactionsPath.c_str(), 0770);
        mkdir(config.tilesPath.c_str(), 0770);
    }

    void testFinalBlobPath()
    {
        BlobFSTransaction *transaction = new BlobFSSelectTransaction(config);
        long long blobId = 139364;

        auto expectedFinalBlobPath = config.tilesPath + "0/8/139364";
        auto finalBlobPath = transaction->getFinalBlobPath(blobId);
        EXPECT_EQ(finalBlobPath, expectedFinalBlobPath);
        finalBlobPath = transaction->getFinalBlobPath(blobId);
        EXPECT_EQ(finalBlobPath, expectedFinalBlobPath);
        finalBlobPath = transaction->getFinalBlobPath(blobId);
        EXPECT_EQ(finalBlobPath, expectedFinalBlobPath);
    }

    void testInsertTransactionCommit()
    {
        BlobFSTransaction *transaction = new BlobFSInsertTransaction(config);
        string transactionPath = transaction->transactionPath;
        string trLockPath = transaction->transactionLock->transactionLockPath;
        string transactionLockFile = trLockPath + BlobFSTransactionLock::TRANSACTION_LOCK;
        EXPECT_TRUE(BlobFile::fileExists(transactionLockFile));

        long long blobId = 4294967296;
        BlobData blobData(blobId, 4, testData);
        transaction->add(blobData);

        string expectedTmpBlobPath = transactionPath + "4294967296";
        EXPECT_TRUE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_EQ(transaction->getTmpBlobPath(blobId), expectedTmpBlobPath);

        // check contents of tmp file
        BlobFile readBlobFile1(expectedTmpBlobPath);
        BlobData readBlobData1(blobId);
        readBlobFile1.readData(readBlobData1);
        EXPECT_EQ_MEM(readBlobData1.data, blobData.data, blobData.size);

        transaction->preRasbaseCommit();
        EXPECT_TRUE(BlobFile::fileExists(expectedTmpBlobPath));

        transaction->postRasbaseCommit();
        EXPECT_FALSE(BlobFile::fileExists(trLockPath + BlobFSTransactionLock::TRANSACTION_COMMIT_LOCK));
        EXPECT_FALSE(BlobFile::fileExists(expectedTmpBlobPath));

        string expectedFinalBlobPath = config.tilesPath + "16/262144/4294967296";
        EXPECT_EQ(transaction->getFinalBlobPath(blobId), expectedFinalBlobPath);
        EXPECT_TRUE(BlobFile::fileExists(expectedFinalBlobPath));

        // check contents of final updated file
        BlobFile readBlobFile2(expectedFinalBlobPath);
        BlobData readBlobData2(blobId);
        readBlobFile2.readData(readBlobData2);
        EXPECT_EQ_MEM(readBlobData2.data, blobData.data, blobData.size);

        delete transaction;
        unlink(expectedFinalBlobPath.c_str());

        EXPECT_FALSE(BlobFile::fileExists(transactionLockFile));
        EXPECT_FALSE(BlobFile::fileExists(transactionPath));
    }

    void testInsertTransactionAbort()
    {
        BlobFSTransaction *transaction = new BlobFSInsertTransaction(config);
        string transactionPath = transaction->transactionPath;
        string trLockPath = transaction->transactionLock->transactionLockPath;
        string transactionLockFile = trLockPath + BlobFSTransactionLock::TRANSACTION_LOCK;
        EXPECT_TRUE(BlobFile::fileExists(transactionLockFile));

        long long blobId = 4294967296;
        BlobData blobData(blobId, 4, testData);
        transaction->add(blobData);

        string expectedTmpBlobPath = transactionPath + "4294967296";
        EXPECT_TRUE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_EQ(transaction->getTmpBlobPath(blobId), expectedTmpBlobPath);

        transaction->postRasbaseAbort();
        EXPECT_FALSE(BlobFile::fileExists(trLockPath + BlobFSTransactionLock::TRANSACTION_ABORT_LOCK));
        EXPECT_FALSE(BlobFile::fileExists(expectedTmpBlobPath));

        string expectedFinalBlobPath = config.tilesPath + "16/262144/4294967296";
        EXPECT_EQ(transaction->getFinalBlobPath(blobId), expectedFinalBlobPath);
        EXPECT_FALSE(BlobFile::fileExists(expectedFinalBlobPath));

        delete transaction;

        EXPECT_FALSE(BlobFile::fileExists(transactionLockFile));
        EXPECT_FALSE(BlobFile::fileExists(transactionPath));
    }

    void testUpdateTransactionCommit()
    {
        long long blobId = 4294967296;
        BlobData blobData(blobId, 4, testData);
        insertTestdata(blobData);

        BlobFSTransaction *transaction = new BlobFSUpdateTransaction(config);
        string transactionPath = transaction->transactionPath;
        string trLockPath = transaction->transactionLock->transactionLockPath;
        string expectedFinalBlobPath = config.tilesPath + "16/262144/4294967296";
        string expectedTmpBlobPath = transactionPath + "4294967296";
        string transactionLockFile = trLockPath + BlobFSTransactionLock::TRANSACTION_LOCK;
        EXPECT_TRUE(BlobFile::fileExists(transactionLockFile));

        blobData.size = 5;
        char newData[] = {'t', 'e', 's', 't', '2'};
        blobData.data = newData;
        transaction->add(blobData);

        EXPECT_TRUE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_EQ(transaction->getTmpBlobPath(blobId), expectedTmpBlobPath);

        // check contents of tmp file
        BlobFile readBlobFile1(expectedTmpBlobPath);
        BlobData readBlobData1(blobId);
        readBlobFile1.readData(readBlobData1);
        EXPECT_EQ_MEM(readBlobData1.data, blobData.data, blobData.size);

        transaction->preRasbaseCommit();
        EXPECT_TRUE(BlobFile::fileExists(expectedTmpBlobPath));

        transaction->postRasbaseCommit();
        EXPECT_FALSE(BlobFile::fileExists(trLockPath + BlobFSTransactionLock::TRANSACTION_COMMIT_LOCK));
        EXPECT_FALSE(BlobFile::fileExists(expectedTmpBlobPath));

        EXPECT_EQ(transaction->getFinalBlobPath(blobId), expectedFinalBlobPath);
        EXPECT_TRUE(BlobFile::fileExists(expectedFinalBlobPath));

        // check contents of final updated file
        BlobFile readBlobFile2(expectedFinalBlobPath);
        BlobData readBlobData2(blobId);
        readBlobFile2.readData(readBlobData2);
        EXPECT_EQ_MEM(readBlobData2.data, blobData.data, blobData.size);

        delete transaction;
        unlink(expectedFinalBlobPath.c_str());

        EXPECT_FALSE(BlobFile::fileExists(transactionLockFile));
        EXPECT_FALSE(BlobFile::fileExists(transactionPath));
    }

    void testUpdateTransactionAbort()
    {
        long long blobId = 4294967296;
        BlobData blobData(blobId, 4, testData);
        insertTestdata(blobData);

        BlobFSTransaction *transaction = new BlobFSUpdateTransaction(config);
        string transactionPath = transaction->transactionPath;
        string trLockPath = transaction->transactionLock->transactionLockPath;
        string expectedFinalBlobPath = config.tilesPath + "16/262144/4294967296";
        string expectedTmpBlobPath = transactionPath + "4294967296";
        string transactionLockFile = trLockPath + BlobFSTransactionLock::TRANSACTION_LOCK;
        EXPECT_TRUE(BlobFile::fileExists(transactionLockFile));

        blobData.size = 5;
        char newData[] = {'t', 'e', 's', 't', '2'};
        blobData.data = newData;
        transaction->add(blobData);

        EXPECT_TRUE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_EQ(transaction->getTmpBlobPath(blobId), expectedTmpBlobPath);

        // check contents of tmp file
        BlobFile readBlobFile1(expectedTmpBlobPath);
        BlobData readBlobData1(blobId);
        readBlobFile1.readData(readBlobData1);
        EXPECT_EQ_MEM(readBlobData1.data, blobData.data, blobData.size);

        transaction->postRasbaseAbort();
        EXPECT_FALSE(BlobFile::fileExists(trLockPath + BlobFSTransactionLock::TRANSACTION_COMMIT_LOCK));
        EXPECT_FALSE(BlobFile::fileExists(expectedTmpBlobPath));

        EXPECT_EQ(transaction->getFinalBlobPath(blobId), expectedFinalBlobPath);
        EXPECT_TRUE(BlobFile::fileExists(expectedFinalBlobPath));

        // check contents of final updated file
        BlobFile readBlobFile2(expectedFinalBlobPath);
        BlobData readBlobData2(blobId);
        readBlobFile2.readData(readBlobData2);
        EXPECT_EQ_MEM(readBlobData2.data, testData, 4);

        delete transaction;

        EXPECT_FALSE(BlobFile::fileExists(transactionLockFile));
        EXPECT_FALSE(BlobFile::fileExists(transactionPath));
    }

    void testRemoveTransactionCommit()
    {
        long long blobId = 4294967296;
        BlobData blobData(blobId, 4, testData);
        insertTestdata(blobData);

        BlobFSTransaction *transaction = new BlobFSRemoveTransaction(config);
        string transactionPath = transaction->transactionPath;
        string trLockPath = transaction->transactionLock->transactionLockPath;
        string expectedFinalBlobPath = config.tilesPath + "16/262144/4294967296";
        string expectedTmpBlobPath = transactionPath + "4294967296";
        string transactionLockFile = trLockPath + BlobFSTransactionLock::TRANSACTION_LOCK;
        EXPECT_TRUE(BlobFile::fileExists(transactionLockFile));

        transaction->add(blobData);

        EXPECT_FALSE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_TRUE(BlobFile::fileExists(expectedFinalBlobPath));
        EXPECT_EQ(transaction->getTmpBlobPath(blobId), expectedTmpBlobPath);

        transaction->preRasbaseCommit();
        EXPECT_TRUE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_FALSE(BlobFile::fileExists(expectedFinalBlobPath));
        EXPECT_FALSE(BlobFile::fileExists(trLockPath + BlobFSTransactionLock::TRANSACTION_ABORT_LOCK));

        transaction->postRasbaseCommit();
        EXPECT_FALSE(BlobFile::fileExists(expectedFinalBlobPath));
        EXPECT_FALSE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_FALSE(BlobFile::fileExists(trLockPath + BlobFSTransactionLock::TRANSACTION_COMMIT_LOCK));

        delete transaction;

        EXPECT_FALSE(BlobFile::fileExists(transactionLockFile));
        EXPECT_FALSE(BlobFile::fileExists(transactionPath));
    }

    void testRemoveTransactionAbort()
    {
        long long blobId = 4294967296;
        BlobData blobData(blobId, 4, testData);
        insertTestdata(blobData);

        BlobFSTransaction *transaction = new BlobFSRemoveTransaction(config);
        string transactionPath = transaction->transactionPath;
        string trLockPath = transaction->transactionLock->transactionLockPath;
        string expectedFinalBlobPath = config.tilesPath + "16/262144/4294967296";
        string expectedTmpBlobPath = transactionPath + "4294967296";
        string transactionLockFile = trLockPath + BlobFSTransactionLock::TRANSACTION_LOCK;
        EXPECT_TRUE(BlobFile::fileExists(transactionLockFile));

        transaction->add(blobData);

        EXPECT_FALSE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_TRUE(BlobFile::fileExists(expectedFinalBlobPath));
        EXPECT_EQ(transaction->getTmpBlobPath(blobId), expectedTmpBlobPath);

        transaction->postRasbaseAbort();
        EXPECT_FALSE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_TRUE(BlobFile::fileExists(expectedFinalBlobPath));
        EXPECT_FALSE(BlobFile::fileExists(trLockPath + BlobFSTransactionLock::TRANSACTION_ABORT_LOCK));

        delete transaction;

        EXPECT_FALSE(BlobFile::fileExists(transactionLockFile));
        EXPECT_FALSE(BlobFile::fileExists(transactionPath));
    }

    void testRemoveTransactionPreRasbaseCommitAbort()
    {
        long long blobId = 4294967296;
        BlobData blobData(blobId, 4, testData);
        insertTestdata(blobData);

        BlobFSTransaction *transaction = new BlobFSRemoveTransaction(config);
        string transactionPath = transaction->transactionPath;
        string trLockPath = transaction->transactionLock->transactionLockPath;
        string expectedFinalBlobPath = config.tilesPath + "16/262144/4294967296";
        string expectedTmpBlobPath = transactionPath + "4294967296";
        string transactionLockFile = trLockPath + BlobFSTransactionLock::TRANSACTION_LOCK;
        EXPECT_TRUE(BlobFile::fileExists(transactionLockFile));

        transaction->add(blobData);

        EXPECT_FALSE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_TRUE(BlobFile::fileExists(expectedFinalBlobPath));
        EXPECT_EQ(transaction->getTmpBlobPath(blobId), expectedTmpBlobPath);

        transaction->preRasbaseCommit();
        EXPECT_TRUE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_FALSE(BlobFile::fileExists(expectedFinalBlobPath));
        EXPECT_FALSE(BlobFile::fileExists(trLockPath + BlobFSTransactionLock::TRANSACTION_ABORT_LOCK));

        transaction->postRasbaseAbort();
        EXPECT_FALSE(BlobFile::fileExists(expectedTmpBlobPath));
        EXPECT_TRUE(BlobFile::fileExists(expectedFinalBlobPath));
        EXPECT_FALSE(BlobFile::fileExists(trLockPath + BlobFSTransactionLock::TRANSACTION_ABORT_LOCK));

        // check contents of final updated file
        BlobFile readBlobFile2(expectedFinalBlobPath);
        BlobData readBlobData2(blobId);
        readBlobFile2.readData(readBlobData2);
        EXPECT_EQ_MEM(readBlobData2.data, testData, 4);

        delete transaction;

        EXPECT_FALSE(BlobFile::fileExists(transactionLockFile));
        EXPECT_FALSE(BlobFile::fileExists(transactionPath));
    }

    void finishRun()
    {
        system("rm -rf /tmp/rasdata");
    }

private:
    void insertTestdata(BlobData &blobData)
    {
        BlobFSInsertTransaction insertTransaction(config);
        insertTransaction.add(blobData);
        insertTransaction.postRasbaseCommit();
    }

    BlobFSConfig config;
};

int main(int argc, char **argv)
{
#ifndef BASEDB_SQLITE
    cerr << "testsuite runs only on SQLite / Filestorage rasdaman." << endl;
    return 0;
#endif

    common::LogConfiguration defaultConf;
    defaultConf.configClientLogging();

    TestBlobFSTransaction test;

    RUN_TEST(test.testFinalBlobPath());
    RUN_TEST(test.testInsertTransactionCommit());
    RUN_TEST(test.testInsertTransactionAbort());
    RUN_TEST(test.testUpdateTransactionCommit());
    RUN_TEST(test.testUpdateTransactionAbort());
    RUN_TEST(test.testRemoveTransactionCommit());
    RUN_TEST(test.testRemoveTransactionAbort());
    RUN_TEST(test.testRemoveTransactionPreRasbaseCommitAbort());

    test.finishRun();

    return Test::getResult();
}
