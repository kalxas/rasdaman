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
 * INCLUDE: httpserver.hh
 *
 * MODULE:  servercomm
 * CLASS:   HttpServer
 *
 * COMMENTS:
 *      No Comments
*/

#ifndef _HTTPSERVER_
#define _HTTPSERVER_

#include "servercomm/servercomm.hh"
#include "servercomm/cliententry.hh"

//@ManMemo: Module: {\bf servercomm}

/*@Doc:
  The class HttpServer describes the one and only server communication object
  that can exist in a RasDaMan server. It manages listening for clients and
  maps incoming calls to the respective procedures (which reside in the
  file manager.cc).

  This class implements the functions useful for HTTP communication
  and is based on a copy of servercomm.hh (Version 1.48).
*/


class HttpServer : public ServerComm
{
public:
    /// this class represents an MDD in HTTP transfer encoding
    struct MDDEncoding
    {
        int      objectType{0};
        char*    objectTypeName{NULL};
        char*    typeStructure{NULL};
        unsigned long typeLength{0};
        char*    domain{NULL};
        char*    tileSize{NULL};
        char*    oidString{NULL};
        unsigned long dataSize{0};
        char*    binData{NULL};

        MDDEncoding() = default;
        ~MDDEncoding();

        std::string toString() const;
    };

    // the class uses the class ClientTblElt from ServerComm because it is used
    // in some other files of the server, e.g., qlparser/qtmddaccess.cc or
    // qlparser/qtcommand.cc or qlparser/qtinsert.cc all include servercomm.hh

    /// default constructor
    HttpServer();

    // the acual constructor
    HttpServer(unsigned long timeOut, unsigned long managementInterval , unsigned long listenPort, char* rasmgrHost, unsigned int rasmgrPort, char* serverName);

    /// destructor
    virtual ~HttpServer();

    /// print server status to {\tt s}
    void printServerStatus() override;

    /// Executes a retrieval query and prepare the result for HTTP transer.
    virtual long processRequest(unsigned long callingClientId, char* baseName,
                                int rascommand, char* query, int binDataSize, char* binData,
                                int Endianess, char*& result, char* capability);
    /**
       Executes a query and prepares the complete result for transfer via
       HTTP. The length of the result is returned. The first parameter is
       the unique client id for which the query should be executed. The
       second parameter The third parameter is the query itself represented
       as a string. {\tt result} will contain a pointer to the result as
       needed for HTTP transfer. This pointer has to be freed by the caller
       using free.

       Return values on Error:
       \begin{tabular}{lll}
       -1 && parse errror\\
       -2 && execution error\\
       -3 && unknown error\\
       \end{tabular}

       Question: How to transfer the result?
    */

    /// returns a pointer to the context of the calling client, 0 it there is no context
    virtual ClientTblElt* getClientContext(unsigned long ClientId);
    /**
       Returns a pointer to the context of the calling client. Currently always
       the same global context is returned.
    */
private:

    long encodeResult(unsigned short execResult, unsigned long callingClientId,
                      char *&result, ExecuteQueryRes &resultError);

    long encodeMDDs(unsigned long callingClientId, char*& result);

    long encodeScalars(unsigned long callingClientId, char*& result, const char* typeStructure);

    long encodeEmpty(char*& result);

    long encodeError(char*& result, const r_ULong  errorNo,
                     const r_ULong lineNo, const r_ULong columnNo, const char* text);

    size_t getHeaderSize(const char* collType) const;

    void encodeHeader(char** dst, int responseType, int endianess,
            r_Long numObjects, const char* collType, const char* dstStart, size_t totalLength) const;

    void swapArrayIfNeeded(const std::unique_ptr<Tile> &tile, const r_Minterval &dom) const;

    void releaseContext(ClientTblElt* context) const;

    void skipWhitespace(char** s) const;
    void skipWord(char** s) const;

    long insertIfNeeded(unsigned long callingClientId, char* query,
                        int binDataSize, char* binData, int Endianess,
                        char*& result, bool& isPersistent);

    unsigned short startInsertMDD(unsigned long callingClientId, char* query,
                                  const vector<HttpServer::MDDEncoding*> &transferredMDDs, bool &isPersistent);

    unsigned short insertMDD(unsigned long callingClientId,
                             vector<HttpServer::MDDEncoding*> &transferredMDDs, bool isPersistent);

    long encodeInsertError(char*& result, unsigned short execResult, vector<HttpServer::MDDEncoding*> &transferredMDDs);


    static vector<MDDEncoding*> getMDDs(int binDataSize, char* binData, int endianess);
    static int encodeAckn(char*& result, int ackCode);
    static void resetExecuteQueryRes(ExecuteQueryRes& res);
    static void cleanExecuteQueryRes(ExecuteQueryRes& res);

    // client requests allowed; this should be in sync with RasODMGGlobal.java
    static const int commOpenDB;
    static const int commCloseDB;
    static const int commBeginTAreadOnly;
    static const int commBeginTAreadWrite;
    static const int commCommitTA;
    static const int commAbortTA;
    static const int commIsOpenTA;
    static const int commQueryExec;
    static const int commUpdateQueryExec;
    static const int commGetNewOID;
    static const int commInsertQueryExec;

    // processRequest returns this value in case of an unknown error
    static const long unknownError;
};

#endif
