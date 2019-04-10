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

using namespace std;

#include "config.h"
#include <unistd.h>
#include <netdb.h>
#include <sys/stat.h>
#include <iostream>
#include <string>
#include <sstream>

#include "globals.hh"   // DEFAULT_PORT, LOGDIR, LOG_SUFFIX
#include "rasserver_config.hh"

#include "storagemgr/sstoragelayout.hh"
#include "servercomm/httpserver.hh"

#include "debug/debug.hh"
#include "debug.hh"
#include "loggingutils.hh"

// -- directql section start
#include "rasserver_error.hh"

#define PARAM_HELP_FLAG 'h'
#define PARAM_HELP  "help"
#define HELP_HELP   "show command line switches"

#define PARAM_SERV_FLAG 's'
#define PARAM_SERV  "server"
#define HELP_SERV   "<host-name> rasdaman server"
#define DEFAULT_SERV    "localhost"

#define PARAM_PORT_FLAG 'p'
#define PARAM_PORT  "port"
#define HELP_PORT   "<p> rasmgr port number"
#define DEFAULT_PORT    7001
#define DEFAULT_PORT_STR "7001"

#define PARAM_DB_FLAG   'd'
#define PARAM_DB    "database"
#define HELP_DB     "<db-name> name of database"
#define DEFAULT_DB  "RASBASE"

#define PARAM_USER  "user"
#define HELP_USER   "<user-name> name of user"
#define DEFAULT_USER    "rasguest"

#define PARAM_PASSWD    "passwd"
#define HELP_PASSWD "<user-passwd> password of user"
#define DEFAULT_PASSWD  "rasguest"

#define PARAM_FILE_FLAG 'f'
#define PARAM_FILE  "file"
#define HELP_FILE   "<f> file name for upload through $i parameters within queries; each $i needs its own file parameter, in proper sequence. Requires --mdddomain and --mddtype"

#define PARAM_DOMAIN    "mdddomain"
#define HELP_DOMAIN "<mdd-domain> domain of marray, format: \'[x0:x1,y0:y1]\' (required only if --file specified and file is in data format r_Array)"

#define PARAM_MDDTYPE   "mddtype"
// this is for display only; internally MDD_STRINGTYPE is used
#define DEFAULT_MDDTYPE "byte string"
#define HELP_MDDTYPE    "<mdd-type> type of marray (required only if --file specified and file is in data format r_Array)"

#define PARAM_QUERY_FLAG 'q'
#define PARAM_QUERY "query"
#define HELP_QUERY  "<q> query string to be sent to the rasdaman server for execution"

#define PARAM_OUT   "out"
#define HELP_OUT    "<t> use display method t for cell values of result MDDs where t is one of none, file, formatted, string, hex. Implies --content"
#define DEFAULT_OUT OUT_NONE
#define PARAM_OUT_FILE  "file"
#define PARAM_OUT_STRING "string"
#define PARAM_OUT_HEX   "hex"
#define PARAM_OUT_FORMATTED "formatted"
#define PARAM_OUT_NONE  "none"
#define DEFAULT_OUT_STR PARAM_OUT_NONE

#define PARAM_CONTENT   "content"
#define HELP_CONTENT    "display result, if any (see also --out and --type for output formatting)"

#define PARAM_TYPE  "type"
#define HELP_TYPE   "display type information for results"

#define PARAM_OUTFILE_FLAG 'o'
#define PARAM_OUTFILE   "outfile"
#define HELP_OUTFILE    "<of> file name template for storing result images (ignored for scalar results). Use '%d' to indicate auto numbering position, like with printf(1). For well-known file types, a proper suffix is appended to the resulting file name. Implies --out file."
#define DEFAULT_OUTFILE "rasql_%d"

#define PARAM_QUIET "quiet"
#define HELP_QUIET  "print no ornament messages, only results and errors"

#define PARAM_DEBUG "debug"
#define HELP_DEBUG  "generate diagnostic output"
// -- directql section end

// -- rasdl section start
#define FLAG_CREATE     'c'
#define PARAM_CREATE    "createdatabase"
#define HELP_CREATE     "create database and initialize schema information"

#define PARAM_DELDB     "deldatabase"
#define HELP_DELDB      "delete database"

Configuration configuration;

Configuration::Configuration()
{
    logToStdOut = true;
    logFileName = 0;
    queryStringOn = false;
    rasdlOn = false;
    outputType = DEFAULT_OUT;
    progMode = M_INVALID;
}


bool Configuration::parseCommandLine(int argc, char** argv)
{
    CommandLineParser& cmlInter  = CommandLineParser::getInstance();
    initParameters();

    try
    {
        myExecutable = argv[0];
        cmlInter.processCommandLine(argc, argv);

        if (cmlHelp->isPresent())
        {
            printHelp();
            exit(-2);   // Unix code for 'help' -- PB 2005-sep-18
        }
        checkParameters();
    }

    catch (CmlException& ex)
    {
        cout << "Error: " << ex.what() << endl;

        if (!logToStdOut)
        {
            LERROR << "Error: " << ex.what();
        }
        return false;
    }
    return true;
}

// just for shorter lines...
#define NSN CommandLineParser::noShortName

void Configuration::initParameters()
{
    CommandLineParser& cmlInter  = CommandLineParser::getInstance();

    cmlHelp     = &cmlInter.addFlagParameter('h', "help", "print this help");

    //connection
    cmlRsn      = &cmlInter.addStringParameter(NSN, "rsn", "<srv-name> rasserver instance name", DEFAULT_SERVERNAME);
    cmlPort     = &cmlInter.addStringParameter(NSN, "lport", "<nnnn> rasserver listen port (RPC or HTTP)");
    cmlMgr      = &cmlInter.addStringParameter(NSN, "mgr", "<mgr-host> name of RasMGR host", DEFAULT_HOSTNAME);
    cmlMgrPort  = &cmlInter.addLongParameter(NSN, "mgrport", "<nnnn> rasmgr port", DEFAULT_PORT);
    cmlMgrSync  = &cmlInter.addStringParameter(NSN, "sync", ""); // deprecated

    cmlTransBuffer = &cmlInter.addLongParameter(NSN, "transbuffer", "<nnnn> maximal size of the transfer buffer in bytes", MAX_BUFFER_SIZE);
    cmlTimeOut     = &cmlInter.addLongParameter(NSN, "timeout", "<nnn> client time out in seconds.\n\t\tif it is set to 0 server doesn't check for client timeouts", CLIENT_TIMEOUT);
    cmlMgmntInt    = &cmlInter.addStringParameter(NSN, "mgmntint", ""); // deprecated
    cmlHttp        = &cmlInter.addFlagParameter(NSN, "http", "start HTTP version of rasserver");
    cmlRnp         = &cmlInter.addFlagParameter(NSN, "rnp", "start RNP version of rasserver");
    cmlRasnet      = &cmlInter.addFlagParameter(NSN, "rasnet", "start RASNET version of rasserver");

    cmlLockMgrOn   = &cmlInter.addFlagParameter(NSN, "enable-tilelocking", "enables fine grained locking of tiles");

    cmlConnectStr  = &cmlInter.addStringParameter(NSN, "connect", "<connect-str> connect string for underlying database(e.g. test/test@julep)", "/");
    cmlUserStr  = &cmlInter.addStringParameter('u', "user", "<username> database connection user (empty by default)", "");
    cmlPasswdStr  = &cmlInter.addStringParameter('p', "passwd", "<password> database connection password (empty by default)", "");
    cmlLog     = &cmlInter.addStringParameter('l', "log", "<log-file> log is printed to <log-file>\n\t\tif <log-file> is stdout , log output is printed to standard out", "$RMANHOME/log/<srv-name>.<pid>.log");

    cmlTileSize = &cmlInter.addLongParameter(NSN, "tilesize", "<nnnn> specifies maximal size of tiles in bytes\n\t\t-regular tiles with equal edge lengthes",  4194304);
    cmlPctMin   = &cmlInter.addLongParameter(NSN, "pctmin", "<nnnn> specifies minimal size of blobtiles in bytes",  2048);
    cmlPctMax   = &cmlInter.addLongParameter(NSN, "pctmax", "<nnnn> specifies maximal size of inlinetiles in bytes",  4096);
    cmlUseTC    = &cmlInter.addFlagParameter(NSN, "usetc", "use TileContainerIndex");
    cmlTileConf = &cmlInter.addStringParameter(NSN, "tileconf", "<dom> default tile configuration (e.g. [0:1,0:2])", "[0:1023,0:1023]");

    cmlNewServerId = &cmlInter.addStringParameter(NSN, "serverId", "serverID", NULL);
    string tilingDesc = string("<tiling-name> retiling strategy, specified as:") + CommandLineParameter::descLineSep +
                        "  " + tiling_name_notiling          + "," + CommandLineParameter::descLineSep +
                        "  " + tiling_name_regulartiling;
    cmlTiling   = &cmlInter.addStringParameter(NSN, "tiling", tilingDesc.c_str(), tiling_name_alignedtiling);

    string indexDesc  = string("<index-name> index for created objects, specified as:") + CommandLineParameter::descLineSep +
                        "  " + index_name_auto              + "," + CommandLineParameter::descLineSep +
                        "  " + index_name_directory         + "," + CommandLineParameter::descLineSep +
                        "  " + index_name_regdirectory      + "," + CommandLineParameter::descLineSep +
                        "  " + index_name_rplustree         + "," + CommandLineParameter::descLineSep +
                        "  " + index_name_regrplustree      + "," + CommandLineParameter::descLineSep +
                        "  " + index_name_tilecontainer     + "," + CommandLineParameter::descLineSep +
                        "  " + index_name_regcomputed;
    cmlIndex    = &cmlInter.addStringParameter(NSN, "index", indexDesc.c_str(), index_name_rplustree);

    // for systemtest use e.g.3 together with tileSize 12
    string indexsizeDesc = string("<nnnn> make the index use n nodes");
    cmlIndexSize = &cmlInter.addLongParameter(NSN, "indexsize", indexsizeDesc.c_str(), 0L);

    cmlCacheLimit = &cmlInter.addLongParameter(NSN, "cachelimit", "<limit> specifies upper limit in bytes on using memory for caching", 0L);

    // directql 
    cmlQuery = &cmlInter.addStringParameter(PARAM_QUERY_FLAG, PARAM_QUERY, HELP_QUERY);
    cmlFile = &cmlInter.addStringParameter(PARAM_FILE_FLAG, PARAM_FILE, HELP_FILE);

    cmlContent = &cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_CONTENT, HELP_CONTENT);
    cmlOut = &cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_OUT, HELP_OUT, DEFAULT_OUT_STR);
    cmlOutfile = &cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_OUTFILE, HELP_OUTFILE, DEFAULT_OUTFILE);
    cmlMddDomain = &cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_DOMAIN, HELP_DOMAIN);
    cmlMddType = &cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_MDDTYPE, HELP_MDDTYPE, DEFAULT_MDDTYPE);
    cmlType = &cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_TYPE, HELP_TYPE);

    cmlDatabase = &cmlInter.addStringParameter(PARAM_DB_FLAG, PARAM_DB, HELP_DB, baseName);
    cmlUser = &cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_USER, HELP_USER, DEFAULT_USER);
    cmlPasswd = &cmlInter.addStringParameter(CommandLineParser::noShortName, PARAM_PASSWD, HELP_PASSWD, DEFAULT_PASSWD);
    cmlQuiet = &cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_QUIET, HELP_QUIET);

    // rasdl
    cmlCreateDb = &cmlInter.addFlagParameter(FLAG_CREATE, PARAM_CREATE, HELP_CREATE);
    cmlDelDb = &cmlInter.addFlagParameter(CommandLineParser::noShortName, PARAM_DELDB, HELP_DELDB);


#ifdef RMANDEBUG
    cmlDbg   = &cmlInter.addStringParameter('d', "debug", "<dgb-file> debug output is printed to <dbg-file>; if <dbg-file> is stdout, debug output is printed to standard out", "<srv-name>.log");
    cmlDbgLevel  = &cmlInter.addLongParameter(NSN, "dl", "<nn> debug level (0-4; 0 = no / 4 = maximal debug information)", 0L);
#endif // RMANDEBUG

    if (cmlInter.isPresent(PARAM_QUERY))
    {
        queryStringOn = true;
    }
}
#undef NSN

void Configuration::checkParameters()
{
    serverName = cmlRsn->getValueAsString();

    initLogFiles();

    rasmgrHost = cmlMgr->getValueAsString();
    rasmgrPort = cmlMgrPort->getValueAsLong();
    LDEBUG << "rasmgrHost = " << rasmgrHost << ", rasmgrPort = " << rasmgrPort;

    deprecated(cmlMgrSync);

    maxTransferBufferSize = cmlTransBuffer->getValueAsLong();
    timeout               = cmlTimeOut->getValueAsLong();
    deprecated(cmlMgmntInt);
    httpServ              = cmlHttp->isPresent();
    rnpServ               = cmlRnp->isPresent();
    rasnetServ            = cmlRasnet->isPresent();

    lockmgrOn             = cmlLockMgrOn->isPresent();

    dbConnection = cmlConnectStr->getValueAsString();
    dbUser       = cmlUserStr->getValueAsString();
    dbPasswd     = cmlPasswdStr->getValueAsString();

    tileSize   = cmlTileSize->getValueAsLong();
    pctMin     = cmlPctMin->getValueAsLong();
    pctMax     = cmlPctMax->getValueAsLong();
    useTC      = cmlUseTC->isPresent();


    tileConf   = cmlTileConf->getValueAsString();//(r_Minterval..)
    tilingName = cmlTiling->getValueAsString();
    indexType  = cmlIndex->getValueAsString();
    indexSize  = cmlIndexSize->getValueAsLong();

    cacheLimit = cmlCacheLimit->getValueAsLong();

    newServerId = cmlNewServerId->getValueAsString();
#ifdef RMANDEBUG
    //  deprecated(cmlDbg);     // will certainly not remove this... -- PB 2007-may-07
    // SET_OUTPUT( cmlDbg->isPresent() );   // enable trace macros depending on --debug parameter
    SET_OUTPUT(true);
    // dbgLevel   = cmlDbgLevel->getValueAsLong();
    dbgLevel   = 4;
#endif

// -- directql section start

    queryString = cmlQuery->getValueAsString();

    if (queryString != NULL)
    {
        queryStringOn = true;
        if (cmlPort->isPresent())
        {
            listenPort = cmlPort->getValueAsLong();
        }
        else
        {
            listenPort = DEFAULT_PORT;
        }
    }
    else
    {
       listenPort = cmlPort->getValueAsLong();
    }
   

    // check optional parameters ====================================================

    // evaluate optional parameter file --------------------------------------
    if (cmlFile->isPresent())
    {
        fileName = cmlFile->getValueAsString();
    }

    // evaluate optional parameter database --------------------------------------
    
    baseName = cmlDatabase->getValueAsString();
    

    // evaluate optional parameter user --------------------------------------
    
    user = cmlUser->getValueAsString();
    

    // evaluate optional parameter passwd --------------------------------------
    
    passwd = cmlPasswd->getValueAsString();
    

    // evaluate optional parameter content --------------------------------------
    
    output = cmlOut->isPresent();
    

    // evaluate optional parameter type --------------------------------------
   
    displayType = cmlType->isPresent();
    
    // evaluate optional parameter outfile --------------------------------------
        
        
    outputType = OUT_FILE;
        
    // evaluate optional parameter hex --------------------------------------
    if (output)
    {
        const char* val = cmlOut->getValueAsString();
        if (val != 0 && strcmp(val, PARAM_OUT_STRING) == 0)
        {
            outputType = OUT_STRING;
        }
        else if (val != 0 && strcmp(val, PARAM_OUT_FILE) == 0)
        {
            outputType = OUT_FILE;
        }
        else if (val != 0 && strcmp(val, PARAM_OUT_FORMATTED) == 0)
        {
            outputType = OUT_FORMATTED;
        }
        else if (val != 0 && strcmp(val, PARAM_OUT_HEX) == 0)
        {
            outputType = OUT_HEX;
        }
        else if (val != 0 && strcmp(val, PARAM_OUT_NONE) == 0)
       {
            outputType = OUT_NONE;
        }
        else
        {
            throw RasqlError(ILLEGALOUTPUTTYPE);
        }
    } 


    // evaluate optional parameter domain --------------------------------------
    if (cmlMddDomain->isPresent())
    {
        try
        {
            mddDomain = r_Minterval(cmlMddDomain->getValueAsString());
            mddDomainDef = true;
        }
        catch (r_Error& e) // Minterval constructor had syntax problems
        {
            throw RasqlError(NOVALIDDOMAIN);
        }
    }

    // evaluate optional parameter MDD type name --------------------------------------
    if (cmlMddType->isPresent())
    {
        mddTypeName = cmlMddType->getValueAsString();
        mddTypeNameDef = true;
    }
    

    // evaluate optional parameter 'quiet' --------------------------------------------
    
    quietLog = cmlQuiet->isPresent();
// -- directql section end

// -- rasdl section start

    //create database
    if (cmlCreateDb->isPresent())
    {
        progMode = M_CREATEDATABASE;
    }

    //delete database
    if (cmlDelDb->isPresent())
    {
        progMode = M_DELDATABASE;
    }

    rasdlOn = (cmlCreateDb->isPresent() || cmlDelDb->isPresent());
    
}

void Configuration::printHelp()
{
    CommandLineParser& cmlInter  = CommandLineParser::getInstance();

    cout << "Usage:   rasserver [options]" << endl;
    cout << "Options:" << endl;
    cmlInter.printHelp();

    cout << endl;

}

void
Configuration::initLogFiles()
{

    if (cmlLog->isPresent())
    {
        if (strcasecmp(cmlLog->getValueAsString(), "stdout") != 0)
        {
            logFileName = cmlLog->getValueAsString();
            logToStdOut = false;
        }
        else
        {
            logFileName = "stdout";
            logToStdOut = true;
        }
    }
    else
    {
        // default
        logFileName = makeLogFileName(serverName, LOG_SUFFIX);
        logToStdOut = false;
    }


    // Default logging configuration
    common::LogConfiguration defaultConf(string(CONFDIR), SERVER_LOG_CONF);
    defaultConf.configServerLogging(logFileName);
}

const char*
Configuration::makeLogFileName(const char* srvName, const char* desExt)
{
    static char logfilePath[ FILENAME_MAX ];
    int pid = getpid();
    mkdir(LOGDIR, S_IRWXU + S_IRGRP + S_IXGRP + S_IROTH + S_IXOTH);   // create if not exist, rwxr-xr-x
    int pathLen = snprintf(logfilePath, FILENAME_MAX, "%s/rasserver.%s.%06d.%s", LOGDIR, srvName, pid, desExt);
    if (pathLen >= FILENAME_MAX)
    {
        logfilePath[FILENAME_MAX - 1] = '\0'; // force-terminate string before printing
        cerr << "Warning: path name longer than allowed by OS, likely log file cannot be written: " << logfilePath << endl;
    }
    return logfilePath;
}

void
Configuration::deprecated(CommandLineParameter* cml)
{
    if (cml->isPresent())
    {
        cout << "WARNING: parameter '" << cml->calledName() << "' is deprecated, will be removed in next version!" << endl;
    }
}

const char* Configuration::getServerName()
{
    return serverName;
}
int         Configuration::getListenPort()
{
    return listenPort;
}
bool        Configuration::isHttpServer()
{
    return httpServ;
}
bool        Configuration::isRnpServer()
{
    return rnpServ;
}

bool Configuration::isRasnetServer()
{
    return rasnetServ;
}

const char* Configuration::getRasmgrHost()
{
    return rasmgrHost;
}
int         Configuration::getRasmgrPort()
{
    return rasmgrPort;
}

bool        Configuration::isLogToStdOut()
{
    return logToStdOut;
}

int         Configuration::getMaxTransferBufferSize()
{
    return maxTransferBufferSize;
}
int         Configuration::getTimeout()
{
    return timeout;
}
const char* Configuration::getDbConnectionID()
{
    return dbConnection;
}
const char* Configuration::getDbUser()
{
    return dbUser;
}
const char* Configuration::getDbPasswd()
{
    return dbPasswd;
}

bool        Configuration::isLockMgrOn()
{
    return lockmgrOn;
}

int         Configuration::getDefaultTileSize()
{
    return tileSize;
}
int         Configuration::getDefaultPCTMin()
{
    return pctMin;
}
int         Configuration::getDefaultPCTMax()
{
    return pctMax;
}

int         Configuration::getDefaultIndexSize()
{
    return indexSize;
}

#ifdef RMANDEBUG
int         Configuration::getDebugLevel()
{
    return dbgLevel;
}
#endif

const char* Configuration::getDefaultTileConfig()
{
    return tileConf;
}
const char* Configuration::getTilingScheme()
{
    return tilingName;
}
const char* Configuration::getIndexType()
{
    return indexType;
}
bool        Configuration::useTileContainer()
{
    return useTC;
}
long        Configuration::getCacheLimit()
{
    return cacheLimit;
}

const char* Configuration::getNewServerId()
{
    return newServerId;
}

// -- directql section start
const char* Configuration::getQueryString()
{
    return queryString;
}

const char* Configuration::getUser()
{
    return user;
}

const char* Configuration::getPasswd()
{
    return passwd;
}

const char* Configuration::getOutFileMask()
{
    return outFileMask;
}

const r_Minterval& Configuration::getMddDomain()
{
    return mddDomain;
}

const char* Configuration::getMddTypeName()
{
    return mddTypeName;
}

const char* Configuration::getFileName()
{
    return fileName;
}

bool        Configuration::isMddDomainDef()
{
    return mddDomainDef;
}

bool        Configuration::isMddTypeNameDef()
{
    return mddTypeNameDef;
}

bool        Configuration::isQuietLogOn()
{
    return quietLog;
}

bool        Configuration::hasQueryString()
{
    return queryStringOn;
}

OUTPUT_TYPE Configuration::getOutputType()
{
    return outputType;
}

bool        Configuration::isOutputOn()
{
    return output;
}

void        Configuration::setMddTypeName(const char* mddtn)
{
    this->mddTypeName = mddtn;
}
// -- directql section end

// -- directql section start

bool        Configuration::usesRasdl()
{
    return rasdlOn;
}

ProgModes   Configuration::getProgMode()
{
    return progMode;
}