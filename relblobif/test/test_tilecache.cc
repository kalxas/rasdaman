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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#include "config.h"
#include "version.h"
#include <cstdlib>

#ifdef EARLY_TEMPLATE
#define __EXECUTABLE__
#define DEBUG_MAIN

#include "../../server/template_inst.hh"
#include "../../raslib/template_inst.hh"
#endif

#include "relblobif/blobtile.hh"
#include "relblobif/tilecache.hh"
#include "reladminif/oidif.hh"
#include "reladminif/adminif.hh"
#include "reladminif/databaseif.hh"
#include "reladminif/transactionif.hh"
#include "servercomm/servercomm.hh"
#include "servercomm/cliententry.hh"
#include "commline/cmlparser.hh"
#include "testing.h"

#include "loggingutils.hh"

using namespace std;

#define DEBUG_MAIN
#include "debug-clt.hh"

// define external vars
char globalConnectId[256] = "RASBASE";
char globalDbUser[255] = {0};
char globalDbPasswd[255] = {0};

class MDDColl;
MDDColl *mddConstants = 0; // used in QtMDD
extern unsigned long maxTransferBufferSize = 4000000;
extern int noTimeOut = 0;

#define CLIENT_ID 2
#define RASADMIN_USER "rasadmin"
#define RASMGR_HOST "localhost"
#define RASMGR_PORT 7001
#define RASSERVER_PORT 7013
#define RASSERVER_NAME "NT1"
#define RASSERVER_TIMEOUT 300
#define RASSERVER_MGMT_INTERVAL 120

#define CRUNCH_CAPABILITY "$I1$ERW$BRASBASE$T1:3:2008:23:39:24$NNT1$D3839d047344677ddb1ff1a24dada286e$K"

// normal cache limit
#define CACHE_LIMIT 10000000
// cache limit that triggers cache readjustment
#define CACHE_LIMIT_READJUST 400

INITIALIZE_EASYLOGGINGPP

/*
 * Global variables
 */

TransactionIf ta;
DatabaseIf db;
ServerComm* server;
ClientTblElt* r;
ExecuteUpdateRes result;

void prepareRun()
{
    server = new ServerComm(RASSERVER_PORT, RASMGR_HOST, RASMGR_PORT, RASSERVER_NAME);
    db.open(globalConnectId);

    r = new ClientTblElt(ClientType::Regular, CLIENT_ID);
    server->addClientTblEntry(r);
    accessControl.setServerName(RASSERVER_NAME);
    server->openDB(CLIENT_ID, globalConnectId, RASADMIN_USER);

    AdminIf* myAdmin = AdminIf::instance();
    accessControl.crunchCapability(CRUNCH_CAPABILITY);
    result.token = NULL;
}

void executeQuery(char* query)
{
    ta.begin(&db);
    server->executeUpdate(CLIENT_ID, query, result);
    ta.commit();
}

void finishRun()
{
    db.close();

    delete server;
    delete r;
}

int main(int argc, char** argv)
{
    common::LogConfiguration defaultConf;
    defaultConf.configClientLogging();

    TileCache::cacheLimit = CACHE_LIMIT_READJUST;

    prepareRun();

    executeQuery("drop collection test_tilecache");
    executeQuery("create collection test_tilecache GreySet3");
    executeQuery("insert into test_tilecache values marray x in [0:0,0:0,0:0] values 0c tiling regular [0:9,0:9,0:3] tile size 400 index rpt_index");

    executeQuery("update test_tilecache as m set m[*:*,*:*,0] assign marray x in [0:19,0:9] values (char) 4");
    executeQuery("update test_tilecache as m set m[*:*,*:*,1] assign marray x in [0:19,0:9] values (char) 5");
    executeQuery("update test_tilecache as m set m[*:*,*:*,2] assign marray x in [0:19,0:9] values (char) 6");
    executeQuery("update test_tilecache as m set m[*:*,*:*,3] assign marray x in [0:19,0:9] values (char) 7");
    executeQuery("update test_tilecache as m set m[*:*,*:*,4] assign marray x in [0:19,0:9] values (char) 7");
    executeQuery("update test_tilecache as m set m[*:*,*:*,5] assign marray x in [0:19,0:9] values (char) 7");
    executeQuery("update test_tilecache as m set m[*:*,*:*,6] assign marray x in [0:19,0:9] values (char) 7");
    executeQuery("update test_tilecache as m set m[*:*,*:*,7] assign marray x in [0:19,0:9] values (char) 7");
    executeQuery("update test_tilecache as m set m[*:*,*:*,8] assign marray x in [0:19,0:9] values (char) 7");
    executeQuery("update test_tilecache as m set m[*:*,*:*,9] assign marray x in [0:19,0:9] values (char) 7");
    executeQuery("commit");

    executeQuery("drop collection test_tilecache");

    finishRun();
}
