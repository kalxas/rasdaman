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
/**
 * SOURCE: servercomm.cc
 *
 * MODULE: servercomm
 * CLASS:  ServerComm
 *
 * COMMENTS:
 *          None
*/

#include "config.h"
#include "mymalloc/mymalloc.h"

// --- these defs should go into a central constant definition section,
// as they define externally observable behavior -- PB 2003-nov-15

// waiting period until client is considered dead [secs]
#define CLIENT_TIMEOUT  3600

// timeout for select() call at server startup [secs]
#define TIMEOUT_SELECT  30

// period after which the next garbage collection is scheduled [secs]
#define GARBCOLL_INTERVAL 600

// console output describing successful/unsuccessful actions
#define MSG_OK      "ok"
#define MSG_FAILED  "failed"

// rasserver exit codes (selection of value sometimes unclear :-(
#define EXITCODE_ZERO       0
#define EXITCODE_ONE        1
#define EXITCODE_RASMGR_FAILED  10 // Why 10 ?
// ---

static const char rcsid[] = "@(#)servercomm, ServerComm: $Id: servercomm.cc,v 1.146 2006/01/03 00:23:53 rasdev Exp $";

#include<openssl/evp.h>

#include <iostream>
#ifdef __APPLE__
#include <sys/malloc.h>
#else
#include <malloc.h>
#endif
#include <time.h>      // for time()
#include <stdlib.h>
#include <string.h>

#include <signal.h>    // for signal()
#include <unistd.h>    // for alarm(), gethostname()
#include <iomanip>

#ifdef SOLARIS
#define PORTMAP        // define to use function declarations for old interfaces
#include <rpc/rpc.h>

int _rpcpmstart = 0;

// function prototype with C linkage
extern "C" int gethostname(char *name, int namelen);
#else  // HPUX
#include <rpc/rpc.h>
#endif

#include <rpc/pmap_clnt.h>

#include "raslib/rmdebug.hh"
#include "raslib/rminit.hh"
#include "raslib/error.hh"
#include "raslib/minterval.hh"
#include "raslib/parseparams.hh"

#include "servercomm/servercomm.hh"
#include "qlparser/qtnode.hh"
#include "qlparser/qtdata.hh"
#include "catalogmgr/typefactory.hh"

#include "tilemgr/tile.hh"

#include "relcatalogif/mddtype.hh"
#include "relcatalogif/mdddomaintype.hh"
#include "relcatalogif/settype.hh"
#include "mddmgr/mddcoll.hh"
#include "mddmgr/mddcolliter.hh"
#include "mddmgr/mddobj.hh"

#include "debug.hh"

#ifdef PURIFY
#include <purify.h>
#endif
#include<stdio.h>
#include<stdlib.h>
#include<sys/types.h>
#include<sys/socket.h>
#include<netinet/in.h>
#include<netdb.h>
#include<iostream>

#include <easylogging++.h>

using namespace std;
// init globals for server initialization
// RMINITGLOBALS('S')

// Once again a function prototype. The first one is for the RPC dispatcher
// function located in the server stub file rpcif_svc.c and the second one
// is for the garbage collection function pointed to by the signal handler.
extern "C"
{
    // void rpcif_1( struct svc_req*, register SVCXPRT* );
    void garbageCollection( int );
}
extern char* rpcif_1( struct svc_req*, register SVCXPRT* );
extern bool bMemFailed;

// this is a temporary thing
extern unsigned long maxTransferBufferSize;

// At the beginning, no servercomm object exists.
ServerComm* ServerComm::actual_servercomm = 0;

list<ServerComm::ClientTblElt*>  ServerComm::clientTbl;
unsigned long ServerComm::clientCount = 0;

#include "rnprotocol/srvrasmgrcomm.hh"

extern SrvRasmgrComm rasmgrComm;

/*************************************************************************
 * Method name...: ServerComm()   (constructor)
 ************************************************************************/
ServerComm::ServerComm()
    : clientTimeout( rasmgrComm.getTimeout() ),
      garbageCollectionInterval( GARBCOLL_INTERVAL ),
      transactionActive( 0 ),
      memUsed( 0 ),
      admin( 0 ),
      errorText( 0 )
{
    if( actual_servercomm )
    {
        LFATAL << "Internal Error: Tried to instantiate more than one ServerComm object.";
        exit( EXITCODE_ONE );
    }

    // uniqueClientContext = NULL;
    isHttpServer        = false;

    actual_servercomm = this;
}

ServerComm::ServerComm( unsigned long timeOut, unsigned long managementInterval , unsigned long newListenPort, char* newRasmgrHost, unsigned int newRasmgrPort,char* newServerName)
    : clientTimeout( timeOut ),
      garbageCollectionInterval( managementInterval ),
      transactionActive( 0 ),
      memUsed( 0 ),
      admin( 0 ),
      errorText( 0 )
{
    if( actual_servercomm )
    {
        LFATAL << "Internal Error: Tried to instantiate more than one ServerComm object.";
        exit( EXITCODE_ONE );
    }

    actual_servercomm = this;
    this->listenPort  = newListenPort;
    this->rasmgrHost  = newRasmgrHost;
    this->rasmgrPort  = newRasmgrPort;
    this->serverName  = newServerName;

    isHttpServer        = false;
    //uniqueClientContext = NULL;
}



/*************************************************************************
 * Method name...: ~ServerComm()    (destructor)
 ************************************************************************/
ServerComm::~ServerComm()
{
// delete communication object
    if( admin ) delete admin;

    actual_servercomm = 0;
}

/*************************************************************************
 * Method name...: startRpcServer()
 ************************************************************************/
void rpcSignalHandler(int sig);
void our_svc_run();

void
ServerComm::startRpcServer()
throw( r_Error )
{
    // create administraion object (O2 session is initialized)
    admin = AdminIf::instance();
    if( !admin )
        throw r_Error( r_Error::r_Error_BaseDBMSFailed );

    register SVCXPRT *transp;

    // install a signal handler to catch alarm signals for garbage collection
    signal( SIGALRM, garbageCollection );

    LINFO << "Testing if no other rasdaman RPC server with my listenPort (0x"<< hex << listenPort << dec <<") is already running on this CPU...";


    char* myName = new char[256];

    if( gethostname( myName, 256 ) )
        LWARNING << "Unable to determine my own hostname. Skipping this test.";
    else if( clnt_create( myName, listenPort, RPCIFVERS, "tcp" ) )
    {
        LFATAL << MSG_FAILED;
        exit( EXITCODE_ZERO );
    }
    else
        LINFO << MSG_OK;

    delete[] myName;

    (void) pmap_unset(listenPort, RPCIFVERS);

    LINFO << "Creating UDP services...";
    transp = svcudp_create(RPC_ANYSOCK);
    if (transp == NULL)
    {
        LFATAL << MSG_FAILED;
        throw r_Error( r_Error::r_Error_General );
    }

    LINFO << "registering UDP interface...";
    if (!svc_register(transp, listenPort, RPCIFVERS, rpcif_1_caller, IPPROTO_UDP))
    {
        LFATAL << MSG_FAILED;
        throw r_Error( r_Error::r_Error_General );
    }
    LINFO << MSG_OK;

    LINFO << "Creating TCP services...";
    transp = svctcp_create(RPC_ANYSOCK, 0, 0);
    if (transp == NULL)
    {
        LFATAL << MSG_FAILED;
        throw r_Error( r_Error::r_Error_General );
    }

    LINFO << "registering TCP interface...";
    if (!svc_register(transp, listenPort, RPCIFVERS, rpcif_1_caller, IPPROTO_TCP))
    {
        LFATAL << MSG_FAILED;
        throw r_Error( r_Error::r_Error_General );
    }
    LINFO << MSG_OK;

    LINFO << "Setting alarm clock for next garbage collection to " << garbageCollectionInterval
                   << " secs...";
    alarm( static_cast<unsigned int>(garbageCollectionInterval) );
    LINFO << MSG_OK;

    signal (SIGTERM, rpcSignalHandler);
    informRasMGR(SERVER_AVAILABLE);
    LINFO << "rasdaman server "<<serverName<<" is up.";

    // now wait for client calls (exception guarded)
#ifdef PURIFY
    purify_printf( "Server startup finished." );
    purify_new_leaks();
#endif
    // svc_run();
    our_svc_run();
}

extern "C" {
    void garbageCollectionDummy ( int );
}

void our_svc_run()
{
    fd_set read_fd_set;

    // LDEBUG << "tcp_socket="<<get_socket(&svc_fdset,0);

    struct timeval timeout;
    timeout.tv_sec= TIMEOUT_SELECT;
    timeout.tv_usec=0;

    while(1)
    {
        read_fd_set=svc_fdset;
        LDEBUG << "RPC Server is waiting...";

        int rasp=select(FD_SETSIZE,&read_fd_set,NULL,NULL,&timeout);
        LDEBUG << "RPC select returns: " << rasp;
        if(rasp>0)
        {
            svc_getreqset(&read_fd_set);
            LDEBUG << "RPC Request executed.";
        }

        if(rasp<=0)
        {
            LDEBUG << "our_svc_run(): Error: Timeout."; // or a signal
            // execute all pending callbacks. Redirect alarm signal first to make sure no
            // reentrance is possible!

            // these should abort any pending transaction
            signal( SIGALRM, garbageCollectionDummy );
            ServerComm::actual_servercomm->callback_mgr.executePending();
            signal( SIGALRM, garbageCollection );
            timeout.tv_sec= TIMEOUT_SELECT;
            timeout.tv_usec=0;

            if(accessControl.isClient()==false)
            {
                // regularly tell the rasmgr that we are available. There is a scenario for DoS-attack (or bug, or firewall-problem)
                // when a client allocates itself a server and never calls, so the server is not usable any more.
                // but we have to find a smarter way of doing this, we need rasmgr-rasserver communication!
                ServerComm::actual_servercomm->informRasMGR(SERVER_REGULARSIG);
            }
            if(bMemFailed)
            {
                // no reason to continue
                LFATAL << "Internal error: rasserver: memory exhausted.";
                exit( EXITCODE_ONE );
            }
        }
    }
}


void rpcSignalHandler(__attribute__ ((unused)) int sig)
{
    static int in_progress=0;   // our sema to prevent signal-in-signal

    if (in_progress)
        return;

    in_progress = 1;

    // busy wait
#define SIGNAL_WAIT_CYCLES  1000000
    for(long j=0; j<SIGNAL_WAIT_CYCLES; j++)
        ;

    // if we have a valid server, stop it
    if(ServerComm::actual_servercomm)
    {
        ServerComm::actual_servercomm->stopRpcServer();
    }
} // rpcSignalHandler()

void
ServerComm::stopRpcServer()
{
    LINFO << "Shutdown request received.";
    // Determine when next garbage collection would have occurred
    unsigned long nextGarbColl = static_cast<unsigned long>(time( NULL ));
    struct itimerval rttimer;
    getitimer( ITIMER_REAL, &rttimer );
    nextGarbColl += static_cast<unsigned long>(rttimer.it_value.tv_sec);
    LINFO << "Next garbage collection would have been in " << rttimer.it_value.tv_sec << " sec, at "
                   << ctime((time_t*)&nextGarbColl);

    LINFO << "Unregistering interface...";
    svc_unregister( listenPort, RPCIFVERS );

    LINFO << "shutting down services...";
    abortEveryThingNow();

    LINFO << "informing rasmgr...";
    informRasMGR(SERVER_DOWN);
    LINFO << MSG_OK;

    LINFO << "rasdaman server " << serverName <<" is down.";

    exit(0);
    //  svc_exit();
}

// quick hack function used when stopping server to abort transaction and close db
void
ServerComm::abortEveryThingNow()
{
    list<ServerComm::ClientTblElt*>::iterator iter;
    ServerComm *sc=ServerComm::actual_servercomm;
    iter = sc->clientTbl.begin();

    while ( iter != sc->clientTbl.end() )
    {
        ServerComm::ClientTblElt *clnt = *iter;

        clnt->transaction.abort();
        clnt->database.close();

        iter++;
    }
}

/*************************************************************************
 * rpcif_1_caller(struct svc_req *rqstp, SVCXPRT *transp)
 * indirect caller for rpcif_1, which is automaticaly generated and can't
 * be hand-edited. It provides services for parallel-processing
 *************************************************************************/
void rpcif_1_caller(struct svc_req *rqstp, SVCXPRT *transp)
{
    bool flagTransactionReady=false;

    bool isGetExtendedError=false;

    switch (rqstp->rq_proc)
    {
        //case RPCCOMMITTA: no, closeDB only, because abortTA and commitTA
        //case RPCABORTTA:  do automatically a closeDB and both reporting to RasMGR "available" brings problems

    case      RPCCLOSEDB:
        flagTransactionReady=true;
        break;

    case RPCGETERRORINFO:
        isGetExtendedError = true;
        break;
    default: break;
    }

    if(isGetExtendedError == false) ServerComm::actual_servercomm->clearExtendedErrorInfo();

    char* errTxt = rpcif_1(rqstp,transp);

    if(isGetExtendedError && bMemFailed)
    {
        // the client got the message
        // no reason to continue

        signal( SIGALRM, garbageCollectionDummy );
        ServerComm::actual_servercomm->callback_mgr.executePending();
        LFATAL << "Internal error: rasserver panic: memory exhausted, terminating forcefully.";
        exit(1);
    }

    if (errTxt)
    {
        // this is not necessary, since the client gets an error code '42'
        //LERROR << "rasserver: general exception from server caught by rpcif_1_caller!\n" << errTxt;
        //cerr << "rasserver: general exception from server caught by rpcif_1_caller!" << endl << errTxt << endl;
        ServerComm::actual_servercomm->setExtendedErrorInfo(errTxt);
        free(errTxt);
        errTxt = 0;

        //Answer "Remote system error " to the client
        //svcerr_systemerr (transp); don't think that's necessary any more, for the same reason
    }

    ServerComm::actual_servercomm->clientEndRequest();

    if(flagTransactionReady)
        ServerComm::actual_servercomm->informRasMGR(SERVER_AVAILABLE);
}


/*************************************************************************
 * Method name...: informRasMGR( int what ) with a helper function
 ************************************************************************/
#include <errno.h>

// writeWholeMessage:
//  send message via (open, connected) socket to rasmgr
// called by informRasMGR.
// return values:
//  -1  error
//  >0  success

int writeWholeMessage(int socket,char *destBuffer,int buffSize)
{
    // we write the whole message, including the ending '\0', which is already in
    // the buffSize provided by the caller
    int totalLength=0;
    int writeNow;
    while(1)
    {
        writeNow = write(socket,destBuffer+totalLength,static_cast<size_t>(buffSize-totalLength));
        if(writeNow == -1)
        {
            LDEBUG << "writeWholeMessage: bad socket write returned " << writeNow << ", errno=" << errno;
            if(errno == EINTR)
                continue; // read was interrupted by signal (on bad SO's)
            return -1; // another error
        }
        totalLength+=writeNow;

        if( totalLength==buffSize )
            break; // THE END
    }
    return totalLength;
}


long infCount=0; // for debug only

// ServerComm::informRasMGR:
//  send message code to rasmgr (e.g., "still alive"), incl socket management
// return code:
//  none, but function does exit() on error 8-()
void ServerComm::informRasMGR( int what )
{
    LDEBUG << "Informing dispatcher " << rasmgrHost << " port=" << rasmgrPort << " that server is available";
    //what: 0 - going down
    //      1 - available
    //      2 - regular signal

    if (what == SERVER_AVAILABLE)
        accessControl.resetForNewClient();

    struct protoent* getprotoptr = getprotobyname("tcp");
    struct hostent *hostinfo = gethostbyname(rasmgrHost);
    if (hostinfo==NULL)
    {
        cerr<< serverName<<": informRasMGR: cannot locate RasMGR host "<<rasmgrHost<<" ("<<strerror(errno)<<')'<<endl;
        LERROR << "informRasMGR: cannot locate RasMGR host "<<rasmgrHost<<" ("<<strerror(errno)<<')';
        return;
    }

    sockaddr_in internetSocketAddress;
    internetSocketAddress.sin_family = AF_INET;
    internetSocketAddress.sin_port=htons(rasmgrPort);
    internetSocketAddress.sin_addr=*(struct in_addr*)hostinfo->h_addr;

    int sock;       // socket for rasmgr communication
    bool ok=false;      // did we get a socket?

    // FIXME: This is _extremely_ ugly. Should be something like
    // 10 retries, with exponentially growing waits, such as 0, 1ms, 2ms, 4ms, ...
    // have prepared defs, but code is the original one. -- PB 2003-nov-15
    // long maxRetry = NUM_RETRIES_SERVER_ALIVE;
    // #define NUM_RETRIES_SERVER_ALIVE 10

    long retry =0;
    long maxRetry=10000000; // ten millions!
    long talkInterval=100000;
    // creating socket
    for(retry=0; retry<maxRetry; retry++)
    {
        int result;

        // BEWARE, is this relevant? -- PB 2003-nov-15
        // man 7 socket says:
        // "Under some circumstances (e.g. multiple processes
        // accessing a single socket), the condition  that  caused
        // the  SIGIO may have already disappeared when the process
        // reacts to the signal.  If this happens, the process
        // should wait again because Linux will resend the signal later."

        sock=socket(PF_INET,SOCK_STREAM,getprotoptr->p_proto);
        LDEBUG << "Socket=" << sock << " protocol(tcp)=" << getprotoptr->p_proto;

        if(sock<0)
        {
            // if ( (retry%talkInterval) == 0)
            {
                cerr << "Error in server '" << serverName << "': cannot open socket to rasmgr, (" << strerror(errno) << ')' << " still retrying..." << endl;
                LDEBUG << "ServerComm::informRasMGR: cannot open socket to RasMGR: " << strerror(errno) << "; retry " << retry << " of " << maxRetry;
                LERROR << "Error: cannot open socket to rasmgr, (" << strerror(errno) << ')';
            }
            continue;
        }
        result = connect(sock,(struct sockaddr*)&internetSocketAddress,sizeof(internetSocketAddress));
        LDEBUG << "connect(socket=" << sock << ",htons(" << rasmgrPort << ")=" << internetSocketAddress.sin_port << ",in_addr=" << internetSocketAddress.sin_addr.s_addr << ")->" << result;
        if (result < 0)
        {
            // if( (retry%talkInterval) == 0)
            {
                cerr << "Error in server '" << serverName << "': Connection to rasmgr failed (still retrying): "<<strerror(errno);
                cerr << ". retry #" << retry << " of " << maxRetry << endl;
                LDEBUG << "ServerComm::informRasMGR: cannot connect to RasMGR: " << strerror(errno) << "; retry " << retry << " of " << maxRetry;
                LERROR <<"Error: Cannot connect to rasmgr ("<<strerror(errno)<<')';
                LERROR << " retry #" << retry << " of " << maxRetry;
            }
            close(sock); //yes, some SO require this, like Tru64
            continue;
        }
        ok = true;
        break;
    } // for

    if ( !ok )
    {
        cerr << "Error in server '" << serverName << "': Giving up on connecting, terminating." << endl;
        LERROR << "Error: Giving up on connecting, terminating.";
        if (sock)
            close(sock);

        // FIXME: see below
        exit( EXITCODE_RASMGR_FAILED );
    }

    // create HTTP message
    char message[200];
    sprintf(message,"%s%lu\r\n\r\n%s %d %ld ","POST rasservernewstatus HTTP/1.1\r\nUserAgent: RasServer/1.0\r\nContent-length: ",strlen(serverName)+3,serverName,what,infCount++);

    // writing message;
    if(writeWholeMessage(sock,message,strlen(message)+1)<0)
    {
        LERROR <<"Error: Connection to rasmgr failed. ("<<strerror(errno)<<')';
        close(sock);

        // FIXME: extremely ugly. replace by retcode which
        // prevents rasserver from going into svc_run loop
        // -- PB 2003-nov-15
        exit( EXITCODE_RASMGR_FAILED );
    }
    close(sock);
} // ServerComm::informRasMGR()


/*************************************************************************
 * Method name...: getClientContext( unsigned long clientId )
 ************************************************************************/
ServerComm::ClientTblElt*
ServerComm::getClientContext( unsigned long clientId )
{
    ClientTblElt* returnValue=0;

    if( !clientTbl.empty() )
    {
        list<ClientTblElt*>::iterator iter;

        iter = clientTbl.begin();
        while( iter != clientTbl.end() && (*iter)->clientId != clientId )
        {
            LDEBUG << "  inspecting entry with clientID " << (*iter)->clientId;
            iter++;
        }

        if( iter != clientTbl.end() && clientId && (*iter)->clientId == clientId )
        {
            returnValue = *iter;

            // Valid entry was found, so increase the number of current users and
            // reset the client's lastActionTime to now.

            (*iter)->currentUsers++;
            (*iter)->lastActionTime = static_cast<long unsigned int>(time( NULL ));
            LDEBUG << "valid entry found, current users now: " << (*iter)->currentUsers;
        }
    }

    // this output will be done lateron, in the caller:
    // if( returnValue == 0 )
    //  LERROR << "Error: client not registered.";

    // this trick did not work, broke the HTTP server
    //  if(isHttpServer==false ) uniqueClientContext = returnValue;

#ifdef RMANDEBUG
    ServerComm::printServerStatus( RMInit::logOut ); // pretty verbose
#endif
    return returnValue;
}

void
ServerComm::clientEndRequest()
{
#ifdef RMANDEBUG
    printServerStatus( RMInit::dbgOut );        // pretty verbose
#endif

    // this trick did not work, broke the HTTp server

    //  if(isHttpServer==false && uniqueClientContext != NULL)
    //    uniqueClientContext->endRequest();
}

/*************************************************************************
 * Method name...: printServerStatus( )
 ************************************************************************/
void
ServerComm::printServerStatus( ostream& s )
{
    return; // clutters the logs way too much.. -- DM 22-nov-2011
    unsigned long currentTime = static_cast<long unsigned int>(time( NULL ));

    s << "Server state information at " << endl; // << ctime((time_t*)&currentTime) << endl;
    s << "  Inactivity time out of clients.: " << clientTimeout << " sec" << endl;
    s << "  Server management interval.....: " << garbageCollectionInterval << " sec" << endl;
    s << "  Transaction active.............: " << ( transactionActive ? "yes" : "no" ) << endl;
    s << "  Max. transfer buffer size......: " << maxTransferBufferSize << " bytes" << endl;
    s << "  Next available cliend id.......: " << clientCount+1 << endl;
    s << "  No. of client table entries....: " << clientTbl.size() << endl << endl;

    if( !clientTbl.empty() )
    {
        list<ClientTblElt*>::iterator iter;

        // display contents of client table
        s << "client table dump---------------------------------------------" << endl;
        for( iter = clientTbl.begin(); iter != clientTbl.end(); iter++ )
        {
            if (*iter==NULL)
            {
                s << "Error: null context found." << endl;
                LERROR << "Error: null context found.";
                continue;
            }

            s << "Client ID      : " << (*iter)->clientId << endl
              << "Current Users  : " << (*iter)->currentUsers << endl
              << "Client location: " << (*iter)->clientIdText << endl
              << "User name      : " << (*iter)->userName << endl
              << "Database in use: " << (*iter)->baseName << endl
              << "Creation time  : " << endl // ctime((time_t*)&(*iter)->creationTime)
              << "Last action at : " << endl // ctime((time_t*)&(*iter)->lastActionTime)
              << "MDD collection : " << (*iter)->transferColl << endl
              << "MDD iterator   : " << (*iter)->transferCollIter << endl
              << "Current PersMDD: " << (*iter)->assembleMDD << endl
              << "Current MDD    : " << (*iter)->transferMDD << endl
              << "Tile vector    : " << (*iter)->transTiles << endl
              << "Tile iterator  : " << (*iter)->tileIter << endl
              << "Block byte cntr: " << (*iter)->bytesToTransfer << endl << endl;
        }
        s << "end client table dump-----------------------------------------" << endl;

    }

    /*
    s << "memory map----------------------------------------------------" << endl;

    // memorymap(1);

    struct mallinfo meminfo = mallinfo();

    s << "space in arena                 : " << meminfo.arena << endl;
    s << "number of small blocks         : " << meminfo.smblks << endl;
    s << "number of ordinary blocks      : " << meminfo.ordblks << endl;
    s << "space in free ordinary blocks  : " << meminfo.fordblks << endl;
    s << "space in used ordinary blocks  : " << meminfo.uordblks << endl;

    s << "additional space from last call: " << meminfo.uordblks - memUsed << endl;

    memUsed = meminfo.uordblks;

    s << "end memory map------------------------------------------------" << endl << endl;
    */
}



/*************************************************************************
 * Method name...: getServerStatus( )
 ************************************************************************/
void
ServerComm::getServerStatus( ServerStatRes& returnStruct )
{
    returnStruct.inactivityTimeout   = clientTimeout;
    returnStruct.managementInterval = garbageCollectionInterval;
    returnStruct.transactionActive   = transactionActive;
    returnStruct.maxTransferBufferSize = maxTransferBufferSize;
    returnStruct.nextClientId   = clientCount+1;
    returnStruct.clientNumber   = clientTbl.size();

    if( !clientTbl.empty() )
    {
        list<ClientTblElt*>::iterator iter;
        int i;

        returnStruct.clientTable.clientTable_len = clientTbl.size();
        returnStruct.clientTable.clientTable_val = static_cast<RPCClientEntry*>(mymalloc( sizeof(RPCClientEntry) * clientTbl.size() ));

        for( iter = clientTbl.begin(), i=0; iter != clientTbl.end(); iter++, i++ )
        {
            returnStruct.clientTable.clientTable_val[i].clientId = (*iter)->clientId;
            returnStruct.clientTable.clientTable_val[i].clientIdText = strdup( (*iter)->clientIdText );
            returnStruct.clientTable.clientTable_val[i].userName = strdup( (*iter)->userName );
            returnStruct.clientTable.clientTable_val[i].baseName        = strdup( (*iter)->baseName );
            returnStruct.clientTable.clientTable_val[i].creationTime    = (*iter)->creationTime;
            returnStruct.clientTable.clientTable_val[i].lastActionTime  = (*iter)->lastActionTime;
            returnStruct.clientTable.clientTable_val[i].transferColl    = (unsigned long)((*iter)->transferColl);
            returnStruct.clientTable.clientTable_val[i].transferIter    = (unsigned long)((*iter)->transferCollIter);
            returnStruct.clientTable.clientTable_val[i].assembleMDD     = (unsigned long)((*iter)->assembleMDD);
            returnStruct.clientTable.clientTable_val[i].transferMDD     = (unsigned long)((*iter)->transferMDD);
            returnStruct.clientTable.clientTable_val[i].transTiles      = (unsigned long)((*iter)->transTiles);
            returnStruct.clientTable.clientTable_val[i].tileIter        = (unsigned long)((*iter)->tileIter);
            returnStruct.clientTable.clientTable_val[i].bytesToTransfer = (*iter)->bytesToTransfer;
        }
    }

#ifndef __APPLE__
    struct mallinfo meminfo = mallinfo();

    returnStruct.memArena    = static_cast<u_long>(meminfo.arena);
    returnStruct.memSmblks   = static_cast<u_long>(meminfo.smblks);
    returnStruct.memOrdblks  = static_cast<u_long>(meminfo.ordblks);
    returnStruct.memFordblks = static_cast<u_long>(meminfo.fordblks);
    returnStruct.memUordblks = static_cast<u_long>(meminfo.uordblks);
#endif
}


/*************************************************************************
 * Method name...: addClientTblEntry( ClientTblElt *context )
 ************************************************************************/
void
ServerComm::addClientTblEntry( ClientTblElt *context ) throw ( r_Error )
{
    if (context==NULL)
    {
        LFATAL << "Error: ServerComm::addClientTblEntry(): client context is NULL.";
        throw r_Error( r_Error::r_Error_RefNull );
    }

    clientTbl.push_back( context );

#ifdef RMANDEBUG
    ServerComm::printServerStatus( RMInit::logOut ); // quite verbose
#endif
}


/*************************************************************************
 * Method name...: deleteClientTblEntry( unsigned long clientId )
 ************************************************************************/
unsigned short
ServerComm::deleteClientTblEntry( unsigned long clientId )
{
    unsigned short returnValue = 0;

    ClientTblElt* context = getClientContext( clientId );

    if( !context )
    {
        LDEBUG << "Warning: in ServerComm::deleteClientTblEntry(): null context, " << "client " << clientId << " not found.";
        return 1;  // desired client id was not found in the client table
    }

    if( context->currentUsers > 1 )
    {
        // In this case, the client table entry was under use before our getClientContext() call.
        context->release();
        LDEBUG << "Client context of user "<<clientId<<" has current users ="<<context->currentUsers;
        return 2;
    }

    // The transaction contained in the client table element is aborted here.
    // This is reasonable because at this point, the transaction is either
    // already committed (This is the case if an rpcCloseDB call arrives.
    // In this case, abort doesn't do anything harmful.) or the communication
    // has broken down before a rpcCommitTA or a rpcAbortTA (In this case this
    // function is called by the garbage collection and aborting the transaction
    // is advisable.).

    context->releaseTransferStructures();

    // If the current transaction belongs to this client, abort it.
    if( transactionActive == clientId )
    {
        LINFO << "aborting transaction...";
        context->transaction.abort();
        transactionActive = 0;
    }

    // close the database if it isn't already closed
    // (e.g. after connection breakdowns)
    if( strcmp( context->baseName, "none" ) != 0 )
    {
        LINFO << "closing database...";

        context->database.close();

        // reset database name
        delete[] context->baseName;
        context->baseName = new char[5];
        strcpy( context->baseName, "none" );
    }

#ifdef RMANDEBUG
    ServerComm::printServerStatus( RMInit::logOut );    // can be pretty verbose
#endif

    // remove the entry from the client table
    list<ClientTblElt*>::iterator iter;
    for( iter=clientTbl.begin(); iter != clientTbl.end() && (*iter)->clientId != clientId; iter++ )
        ;
    if ( iter != clientTbl.end() )
        clientTbl.erase( iter );

    // delete the client table entry data itself
    // (delete is controlled by the destructor of the ClientTblElt object)
    delete context;

    LDEBUG << "client table now has " << clientTbl.size() << " entries.";
    return returnValue;
}

/*************************************************************************
 * Method name...: ServerComm::getExtendedErrorInfo()
 ************************************************************************/

const char *
ServerComm::getExtendedErrorInfo()
{
    if(errorText == NULL)
        return "(no error info)";

    return errorText;
}

/*************************************************************************
 * Method name...: ServerComm::getExtendedErrorInfo()
 ************************************************************************/

void ServerComm::setExtendedErrorInfo(const char *newErrorText)
{
    clearExtendedErrorInfo();

    errorText = new char [strlen(newErrorText)+1];
    strcpy(errorText,newErrorText);
}


/*************************************************************************
 * Method name...: ServerComm::getExtendedErrorInfo()
 ************************************************************************/

void ServerComm::clearExtendedErrorInfo()
{
    if(errorText) delete[] errorText;

    errorText = NULL;
}

/*************************************************************************
 * Method name...: ClientTblElt( const char* clientText,
 *                               unsigned long client )  (constructor)
 ************************************************************************/
ServerComm::ClientTblElt::ClientTblElt( const char* clientText, unsigned long client )
    : clientId( client ),
      currentUsers(0),
      clientIdText(0),
      userName(0),
      baseName(0),
      creationTime(0),
      lastActionTime(0),
      transferFormat(r_Array),
      transferFormatParams(0),
      exactFormat(1),
      storageFormat(r_Array),
      storageFormatParams(0),
      encodedData(0),
      encodedSize(0),
      totalRawSize(0),
      totalTransferedSize(0),
      transferColl(0),
      transferCollIter(0),
      transferData(0),
      transferDataIter(0),
      assembleMDD(0),
      transferMDD(0),
      transTiles(0),
      tileIter(0),
      deletableTiles(0),
      bytesToTransfer(0),
      persMDDCollections(0),
      taTimer(0),
      transferTimer(0),
      evaluationTimer(0),
      clientParams(0)
{
    creationTime = static_cast<long unsigned int>(time( NULL ));

    clientIdText = new char[strlen(clientText)+1];
    strcpy( clientIdText, clientText );

    baseName = new char[5];
    strcpy( baseName, "none" );

    userName = new char[8];
    strcpy( userName, "unknown" );

    clientParams = new r_Parse_Params();
    clientParams->add("exactformat", &exactFormat, r_Parse_Params::param_type_int);
}


ServerComm::ClientTblElt::~ClientTblElt()
{
    releaseTransferStructures();

    // delete the clientIdText member
    delete[] clientIdText;
    // delete the baseName member
    delete[] baseName;
    // delete user name
    delete[] userName;
    // delete transfer format parameters
    if (transferFormatParams != NULL)
        delete [] transferFormatParams;

    if (storageFormatParams != NULL)
        delete [] storageFormatParams;

    if(clientParams)
        delete clientParams;
}


void
ServerComm::ClientTblElt::release()
{
    if( currentUsers == 0 )
        LWARNING << "Warning: releasing a non-active client.";

    currentUsers--;
    lastActionTime = static_cast<long unsigned int>(time( NULL ));
}

void
ServerComm::ClientTblElt::endRequest()
{
    if(currentUsers != 0)
        LWARNING << "Warning: Client ended request without releasing context. Forcing release now.";

    currentUsers=0;
    lastActionTime = static_cast<long unsigned int>(time( NULL ));
}

void
ServerComm::ClientTblElt::releaseTransferStructures()
{
    // delete the transfer iterator

    if( transferCollIter )
    {
        LTRACE << "release transferCollIter";
        delete transferCollIter;
        transferCollIter = 0;
    }

    // delete transfer data
    if( transferData )
    {
        LTRACE << "release transferData";

        QtNode::QtDataList::iterator dataIter;

        // delete list elements
        for( dataIter=transferData->begin(); dataIter!=transferData->end(); dataIter++ )
            if( *dataIter )
            {
                // Note: The following consistency check does not hold for the case when data objects occur
                //       more than once in the result set (e.g., constants).

                // Consistency Check: should be the last reference.
                //        if( (*dataIter)->getRefNo() > 1 )
                //        {
                //          LERROR << "Internal error in releaseTransferStructures: references left, object ";
                //        }

                // Just tupel elements which are not further referenced are deleted.
                if (*dataIter)
                {
                    (*dataIter)->deleteRef();
                    (*dataIter) = 0;
                }
            }
        delete transferData;
        transferData = 0;
    }

    // delete the transfer collection
    // the transferData will check objects because of the bugfix.  therefore the objects may deleted only after the check.
    if( transferColl )
    {
        LTRACE << "release transferColl";
        transferColl->releaseAll();
        delete transferColl;
        transferColl = 0;
    }

    // delete transfer data iterator
    if( transferDataIter )
    {
        LTRACE << "release transferDataIter";
        delete transferDataIter;
        transferDataIter = 0;
    }

    // delete the temporary PersMDDObj
    if( assembleMDD )
    {
        LTRACE << "release assembleMDD";
        delete assembleMDD;
        assembleMDD = 0;
    }

    // delete the transfer MDDobj
    if( transferMDD )
    {
        LTRACE << "release transferMDD";
        delete transferMDD;
        transferMDD = 0;
    }

    // vector< Tile* >* transTiles;
    if( transTiles )
    {
        LTRACE << "release transTiles";
        // Tiles are deleted by the MDDObject owing them.
        // release( transTiles->begin(), transTiles->end() );
        delete transTiles;
        transTiles = 0;
    }

    // vector< Tile* >::iterator* tileIter;
    if( tileIter )
    {
        LTRACE << "release tileIter";
        delete tileIter;
        tileIter = 0;
    }

    // delete deletable tiles
    if( deletableTiles )
    {
        LTRACE << "release deletableTiles";

        vector<Tile*>::iterator iter;

        for( iter=deletableTiles->begin(); iter!=deletableTiles->end(); iter++ )
            if( *iter )
                delete *iter;

        delete deletableTiles;
        deletableTiles = 0;
    }

    // delete persistent MDD collections
    if( persMDDCollections )
    {
        LTRACE << "release persMDDCollections";

        vector<MDDColl*>::iterator collIter;

        for( collIter=persMDDCollections->begin(); collIter!=persMDDCollections->end(); collIter++ )
            if( *collIter )
            {
                LTRACE << "before PersMDDColl::releaseAll()";
                (*collIter)->releaseAll();
                LTRACE << "after   PersMDDColl::releaseAll()";
                delete *collIter;
            }

        delete persMDDCollections;
        persMDDCollections = 0;
    }

    // transfer compression
    if (encodedData != NULL)
    {
        free(encodedData);
        encodedData = NULL;
        encodedSize = 0;
    }

#ifdef RMANBENCHMARK
    // Attention: taTimer is deleted in either commitTA() or abortTA().

    if( evaluationTimer ) delete evaluationTimer;
    evaluationTimer = 0;

    if( transferTimer ) delete transferTimer;
    transferTimer = 0;
#endif
}


/******************************************************************************************
***  This class shouldn't be here, later it will be put in its own file
******************************************************************************************/

#ifdef LINUX
extern "C" {
    extern char *strptime __P ((__const char *__s, __const char *__fmt, struct tm *__tp));
}
#endif

AccessControl accessControl;


AccessControl::AccessControl()
{
    initDeltaT=0;
    resetForNewClient();
}

AccessControl::~AccessControl()
{
}

void AccessControl::setServerName(const char *newServerName)
{
    strcpy(this->serverName,newServerName);
}

void AccessControl::initSyncro(const char *syncroString)
{
    struct tm brokentime;
    strptime(syncroString,"%d:%m:%Y:%H:%M:%S",&brokentime);
    initDeltaT= difftime (time(NULL) ,mktime(&brokentime) );
    // cout<<"DeltaT="<<initDeltaT<<endl;
}

void AccessControl::resetForNewClient()
{
    okToRead=false;
    okToWrite=false;
    weHaveClient=false;
}

bool AccessControl::isClient()
{
    return weHaveClient;
}

int AccessControl::crunchCapability(const char *capability)
{
    // verify capability is original
    char capaQ[200];
    strcpy(capaQ,"$Canci");
    strcat(capaQ,capability);

    char *digest=strstr(capaQ,"$D");
    if(digest==NULL)
    {
        return CAPABILITY_REFUSED;
    }

    *digest=0;
    digest++;
    digest++;
    digest[32]=0;
    LDEBUG << "Digest="<<digest;

    char testdigest[50];
    messageDigest(capaQ,testdigest,"MD5");
    LDEBUG << "testdg="<<testdigest;
    if(strcmp(testdigest,digest)!=0)
    {
        return CAPABILITY_REFUSED;
    }

    char *rights=strstr(capaQ,"$E")+2;
    char *timeout=strstr(capaQ,"$T")+2;
    char *cServerName=strstr(capaQ,"$N")+2;
    // end of cServername is $D, $->0 by digest

    struct tm brokentime;
    memset(&brokentime, 0, sizeof(struct tm));
    strptime(timeout,"%d:%m:%Y:%H:%M:%S",&brokentime);
    double DeltaT= difftime (mktime(&brokentime),time(NULL) );

    //for the  moment, DEC makes trouble
    // if(DeltaT < initDeltaT) return CAPABILITY_REFUSED; //!!! Capability too old
    //  cout<<"DeltaT="<<DeltaT<<"  initDeltaT="<<initDeltaT<<(DeltaT >= initDeltaT ? " ok":" fail")<<endl;

    if(strcmp(serverName,cServerName)!=0)
    {
        return CAPABILITY_REFUSED; //!!! Call is not for me
    }

    okToRead  = false;  // looks like a 'true' never gets reset: -- PB 2006-jan-02
    okToWrite = false;  // -dito-
    for(int i=0; *rights!='$' && *rights && i<2; rights++,i++)
    {
        //We only have 2 rights defined now
        if(*rights=='R')
            okToRead = true;
        if(*rights=='W')
            okToWrite= true;
    }

    weHaveClient=true;

    LDEBUG << "capability crunched: digest=" << digest << ", rights=" << rights << ", timeout=" << timeout << "(remaining time: " << DeltaT << "), cServerName=" << cServerName << ", okToRead=" << okToRead << ", okToWrite=" << okToWrite << "";

    return 0; // OK for now
}

int AccessControl::messageDigest(const char *input,char *output,const char *mdName)
{
    EVP_MD_CTX mdctx;
    const EVP_MD *md;
    unsigned int md_len, i;
    unsigned char md_value[100];

    OpenSSL_add_all_digests();

    md = EVP_get_digestbyname(mdName);

    if(!md)
        return 0;

    EVP_DigestInit(&mdctx, md);
    EVP_DigestUpdate(&mdctx,input, strlen(input));
    EVP_DigestFinal(&mdctx, md_value, &md_len);

    for(i = 0; i < md_len; i++)
        sprintf(output+i+i,"%02x", md_value[i]);

    return strlen(output);
}

void AccessControl::wantToRead()
{
    if(okToRead==false)
    {
        LFATAL << "Error: no permission for read operation.";
        throw r_Eno_permission(); //r_Error(NO_PERMISSION_FOR_OPERATION);
    }
}

void AccessControl::wantToWrite()
{
    if(okToWrite==false)
    {
        LFATAL << "Error: no permission for write operation.";
        throw r_Eno_permission(); //r_Error(NO_PERMISSION_FOR_OPERATION);
    }

}


