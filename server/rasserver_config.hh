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
#ifndef RASSERVER_CONFIG_HH
#define RASSERVER_CONFIG_HH


#include "raslib/minterval.hh"

// -- directql
typedef enum
{
    OUT_UNDEF,
    OUT_FILE,
    OUT_NONE,
    OUT_STRING,
    OUT_HEX,
    OUT_FORMATTED
} OUTPUT_TYPE;

// -- rasdl
enum ProgModes
{
    M_INVALID,
    M_CREATEDATABASE,
    M_DELDATABASE
};

class CommandLineParameter;

/**
  * \ingroup Servers
  */
class Configuration
{
public:

    Configuration() = default;

    bool parseCommandLine(int argc, char** argv);
    
    // -- rasserver
    const char* getServerName();
    int         getListenPort();
    bool        isRasserver();

    const char* getRasmgrHost();
    int         getRasmgrPort();
    bool        isLogToStdOut();

    int         getMaxTransferBufferSize();
    const char* getDbConnectionID();

    int         getDefaultTileSize();
    int         getDefaultPCTMin();
    int         getDefaultPCTMax();
    int         getDefaultIndexSize();
    const char* getDefaultTileConfig();
    const char* getTilingScheme();
    const char* getIndexType();
    
    const char* getNewServerId();

#ifdef RMANDEBUG
    int         getDebugLevel();
#endif

    // -- directql
    const char* getQueryString();
    const char* getFileName();
    const char* getUser();
    const char* getPasswd();
    const char* getOutFileMask();
    const r_Minterval& getMddDomain();
    const char* getMddTypeName();
    bool        isMddDomainDef();
    bool        isMddTypeNameDef();
    bool        isQuietLogOn();
    bool        isOutputOn();
    bool        hasQueryString();
    OUTPUT_TYPE getOutputType();
    
    void        setMddTypeName(const char* mddtn);

    // -- rasdl
    bool        usesRasdl();
    ProgModes   getProgMode();

     const char *getBaseName() const;

private:
    void printHelp();

    void initParameters();
    void checkParameters();
    void initLogFiles();
    void deprecated(CommandLineParameter*);

    const char* makeLogFileName(const char* srvName, const char* desExt);

    // Parameters
    CommandLineParameter* cmlHelp{};
    CommandLineParameter* cmlRsn{};
    CommandLineParameter* cmlPort{};
    CommandLineParameter* cmlMgr{};
    CommandLineParameter* cmlMgrPort{};

    CommandLineParameter* cmlTransBuffer{};

    CommandLineParameter* cmlConnectStr{};
    CommandLineParameter* cmlLog{};

    CommandLineParameter* cmlTileSize{};
    CommandLineParameter* cmlPctMin{};
    CommandLineParameter* cmlPctMax{};
    CommandLineParameter* cmlTileConf{};
    CommandLineParameter* cmlTiling{};
    CommandLineParameter* cmlIndex{};
    CommandLineParameter* cmlIndexSize{};
    CommandLineParameter* cmlNewServerId{}; // required by rasnet

    // directql parameters
    CommandLineParameter* cmlQuery{};
    CommandLineParameter* cmlQueryFile{};
    CommandLineParameter* cmlFile{};

    CommandLineParameter* cmlContent{};
    CommandLineParameter* cmlOut{};
    CommandLineParameter* cmlOutfile{};
    CommandLineParameter* cmlMddDomain{};
    CommandLineParameter* cmlMddType{};
    CommandLineParameter* cmlType{};

    CommandLineParameter* cmlDatabase{};
    CommandLineParameter* cmlUser{};
    CommandLineParameter* cmlPasswd{};
    CommandLineParameter* cmlQuiet{};

    // rasdl parameters
    CommandLineParameter* cmlCreateDb{};
    CommandLineParameter* cmlDelDb{};
    
    // deprecated, kept for backwards compatibility
    CommandLineParameter* cmlTimeOut{};
    CommandLineParameter* cmlUseTC{};
    CommandLineParameter* cmlLockMgrOn{};
    CommandLineParameter* cmlCacheLimit{};
#ifdef RMANDEBUG
    CommandLineParameter* cmlDbg{};
    CommandLineParameter* cmlDbgLevel{};
#endif

    const char* myExecutable{};
    
    const char* newServerId{}; // required by rasnet
    const char* serverName{};
    const char* rasmgrHost{};
    
    const char* dbConnection{};
    
    const char* logFileName{}; // == 0 if stdout
    const char* tileConf{};
    const char* tilingName{};
    const char* indexType{};
    
    int         rasmgrPort{};
    int         listenPort{};
    int         tileSize{};
    int         pctMin{};
    int         pctMax{};
    int         indexSize{};
    int         maxTransferBufferSize{};

    // directql
    const char* queryString{};
    const char* fileName{};
    const char* baseName{};
    const char* user{};
    const char* passwd{};
    const char* outFileMask{};
    const char* mddTypeName{};
    r_Minterval mddDomain;
    
    OUTPUT_TYPE outputType{OUT_NONE};
    ProgModes   progMode{M_INVALID};
    
    bool        output{false};
    bool        displayType{false};
    bool        quietLog{false};
    bool        mddDomainDef{false};
    bool        mddTypeNameDef{false};
    bool        queryStringOn{false};
    bool        logToStdOut{true};
};

extern Configuration configuration;

#endif
