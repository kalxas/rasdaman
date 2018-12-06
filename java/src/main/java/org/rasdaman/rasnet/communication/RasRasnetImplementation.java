/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package org.rasdaman.rasnet.communication;

import com.google.protobuf.*;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.odmg.*;
import org.rasdaman.rasnet.service.ClientRassrvrServiceOuterClass.*;
import org.rasdaman.rasnet.service.ClientRassrvrServiceGrpc;
import org.rasdaman.rasnet.service.HealthServiceGrpc;
import org.rasdaman.rasnet.service.RasmgrClientServiceGrpc;
import org.rasdaman.rasnet.service.RasmgrClientServiceOuterClass.*;
import org.rasdaman.rasnet.util.Constants;
import org.rasdaman.rasnet.util.DigestUtils;
import org.rasdaman.rasnet.util.GrpcUtils;
import rasj.*;
import rasj.clientcommhttp.RasCommDefs;
import rasj.clientcommhttp.RasUtils;
import rasj.global.Debug;
import rasj.global.RasGlobalDefs;
import rasj.odmg.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class RasRasnetImplementation implements RasImplementationInterface, RasCommDefs, RasGlobalDefs {

    private RasmgrClientServiceGrpc.RasmgrClientServiceBlockingStub rasmgService;
    private ClientRassrvrServiceGrpc.ClientRassrvrServiceBlockingStub rasServerService;
    private HealthServiceGrpc.HealthServiceBlockingStub rasmgrHealthService;
    private HealthServiceGrpc.HealthServiceBlockingStub rasserverHealthService;
    private ManagedChannel rasmgrServiceChannel;
    private ManagedChannel rasServerServiceChannel;

    private RasnetServiceFactory serviceFactory;

    private String rasServerHost;
    private int rasServerPort;

    private String rasServer = "";
    private String rasMgrHost = "";
    private int rasMgrPort = 7001;
    private String databaseName = "";
    private int maxRetry = 5;    // was 120; -- PB 2003-nov-20

    private RasTransaction transaction = null;
    private RasDatabase database = null;
    private RasOQLQuery query = null;

    private int accessMode = 0;
    private boolean readWrite = false;
    private int dbIsOpen = 0;
    private int clientID = 0;
    private String clientUUID;
    private String sessionId;

    private String errorStatus = "";

    /* START - KEEP ALIVE */

    private long keepAliveTimeout = 3 * 1000;
    private volatile RasmgrKeepAlive rasmgrKeepAlive;
    private volatile RasserverKeepAlive rasserverKeepAlive;

    /* END - KEEP ALIVE */

    public RasRasnetImplementation(RasnetServiceFactory rasnetServiceFactory, String server) {

        if (rasnetServiceFactory == null) {
            throw new IllegalArgumentException("Service factory is null.");
        }

        Debug.enterVerbose("RasNetImplementation.RasNetImplementation start. server=" + server);
        try {
            StringTokenizer t = new StringTokenizer(server, "/");
            t.nextToken();
            this.rasMgrHost = t.nextToken("/:");
            String portStr = t.nextToken(":");
            this.rasMgrPort = Integer.parseInt(portStr);

            this.serviceFactory = rasnetServiceFactory;
        } catch (NoSuchElementException e) {
            Debug.talkCritical("RasNetImplementation.RasNetImplementation: " + e.getMessage());
            Debug.leaveVerbose("RasNetImplementation.RasNetImplementation done: " + e.getMessage());
            throw new RasConnectionFailedException(RasGlobalDefs.URL_FORMAT_ERROR, server);
        }
        Debug.leaveVerbose("RasNetImplementation.RasNetImplementation done.");
    }


    @Override
    public String getRasServer() {
        return rasServer;
    }

    @Override
    public int dbIsOpen() {
        return dbIsOpen;
    }

    @Override
    public int getClientID() {
        return clientID;
    }

    @Override
    public int getAccessMode() {
        return accessMode;
    }

    @Override
    public String getErrorStatus() {
        return errorStatus;
    }

    @Override
    public Transaction newTransaction() {
        transaction = new RasTransaction(this);
        return transaction;
    }

    @Override
    public Transaction currentTransaction() {
        return transaction;
    }

    @Override
    public Database newDatabase() {
        this.database = new RasDatabase(this);
        return this.database;
    }

    @Override
    public OQLQuery newOQLQuery() {
        this.query = new RasOQLQuery(this);
        return this.query;
    }

    @Override
    public DList newDList() {
        return new RasList();
    }

    @Override
    public DBag newDBag() {
        return new RasBag();
    }

    @Override
    public DSet newDSet() {
        return new RasSet();
    }

    @Override
    public DArray newDArray() {
        throw new NotImplementedException();
    }

    @Override
    public DMap newDMap() {
        throw new NotImplementedException();
    }

    @Override
    public String getObjectId(Object obj) {
        Debug.enterVerbose("RasNetImplementation.getObjectId start.");
        String oid = null;
        if (obj instanceof RasObject) {
            RasOID roid = ((RasObject) obj).getOID();
            oid = roid.toString();
            if (!((RasObject) obj).getOID().isValid()) {
                roid = executeGetNewObjectId();
                oid = roid.toString();
                ((RasObject) obj).setOID(roid);
            } else {
                Debug.leaveCritical("RasNetImplementation.getObjectId done. not yet implemented.");
                throw new NotImplementedException();
            }
        }
        Debug.leaveVerbose("RasNetImplementation.getObjectId done. oid=" + oid);
        return oid;
    }

    @Override
    public Database getDatabase(Object obj) {
        throw new NotImplementedException();
    }

    @Override
    public void openDB(String name, int accessMode) throws ODMGException {
        Debug.enterVerbose("RasNetImplementation.openDB start. db=" + name + ", accessMode=" + accessMode);
        this.databaseName = name;
        this.accessMode = accessMode;
        this.readWrite = (accessMode != Database.OPEN_READ_ONLY) ? true : false;

        try {
            OpenDbReq openDbReq = OpenDbReq.newBuilder()
                                  .setClientId(this.clientID)
                                  .setClientUUID(this.clientUUID)
                                  .setDatabaseName(this.databaseName)
                                  .build();

            OpenDbRepl openRasmgrDbRepl = this.getRasmgService().openDb(openDbReq);

            rasServerHost = openRasmgrDbRepl.getServerHostName();
            rasServerPort = openRasmgrDbRepl.getPort();
            sessionId = openRasmgrDbRepl.getDbSessionId();

            OpenServerDatabaseReq openServerDatabaseReq = OpenServerDatabaseReq.newBuilder()
                    .setClientId(clientID)
                    .setDatabaseName(databaseName)
                    .build();

            getRasServerService().openServerDatabase(openServerDatabaseReq);

            // The client has connected to the rasmgr and we can shutdown the keep alive.
            stopRasmgrKeepAlive();
            // We have to tell the server that the client is alive.
            startRasserverKeepAlive();

        } catch (StatusRuntimeException ex) {
            throw GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
        } catch (Exception ex) {
            //Rethrow the exception
            throw new ODMGException(ex.getMessage());
        }

        this.dbIsOpen = 1;
        Debug.leaveVerbose("RasNetImplementation.openDB done.");
    }

    @Override
    public void closeDB() throws ODMGException {
        try {

            CloseServerDatabaseReq closeServerDatabaseReq = CloseServerDatabaseReq.newBuilder()
                    .setClientId(this.clientID)
                    .build();

            CloseDbReq closeDbReq = CloseDbReq.newBuilder()
                                    .setClientId(this.clientID)
                                    .setClientUUID(this.clientUUID)
                                    .setDbSessionId(this.sessionId)
                                    .build();

            // First close the database on the server
            this.getRasServerService().closeServerDatabase(closeServerDatabaseReq);

            this.stopRasserverKeepAlive();

            //Close the database on rasmbr
            this.getRasmgService().closeDb(closeDbReq);

            Debug.talkVerbose("RasNetImplementation.closeDB.");

            dbIsOpen = 0;
            disconnectClient();
        } catch (StatusRuntimeException ex) {
            throw GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
        } catch (Exception ex) {
            throw new ODMGException(ex.getMessage());
        }
    }

    @Override
    public void beginTA() {
        try {
            BeginTransactionReq transactionReq = BeginTransactionReq.newBuilder()
                                                 .setClientId(this.clientID)
                                                 .setRw(readWrite)
                                                 .build();

            this.getRasServerService().beginTransaction(transactionReq);
        } catch (StatusRuntimeException ex) {
            throw GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
        } catch (Exception e) {
            throw new ODMGRuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean isOpenTA() {
        boolean isTransactionOpen = false;
        try {
            IsTransactionOpenReq isTransactionOpenReq = IsTransactionOpenReq.newBuilder()
                    .setClientId(clientID)
                    .build();
            IsTransactionOpenRepl reply = this.getRasServerService().isTransactionOpen(isTransactionOpenReq);
            isTransactionOpen = reply.getIsOpen();
        } catch (StatusRuntimeException ex) {
            throw GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
        } catch (Exception ex) {
            throw new ODMGRuntimeException(ex.getMessage());
        }

        return isTransactionOpen;
    }

    @Override
    public void commitTA() {
        try {
            CommitTransactionReq commitTransactionReq = CommitTransactionReq.newBuilder()
                    .setClientId(clientID)
                    .build();

            this.getRasServerService().commitTransaction(commitTransactionReq);
        } catch (StatusRuntimeException ex) {
            throw GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
        } catch (Exception e) {
            throw new ODMGRuntimeException(e.getMessage());
        }
    }

    @Override
    public void abortTA() {
        try {
            AbortTransactionReq abortTransactionReq = AbortTransactionReq.newBuilder()
                    .setClientId(clientID)
                    .build();

            this.getRasServerService().abortTransaction(abortTransactionReq);
        } catch (StatusRuntimeException ex) {
            throw GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
        } catch (Exception e) {
            throw new ODMGRuntimeException(e.getMessage());
        }
    }

    @Override
    public void setMaxRetry(int newRetry) {
        this.maxRetry = newRetry;
    }

    @Override
    public int getMaxRetry() {
        return this.maxRetry;
    }

    @Override
    public void setUserIdentification(String userName, String plainPass) {
        connectClient(userName, DigestUtils.MD5(plainPass));
    }

    private void connectClient(String userName, String passwordHash) {
        try {
            ConnectReq connectReq = ConnectReq.newBuilder()
                                    .setUserName(userName)
                                    .setPasswordHash(passwordHash)
                                    .build();

            ConnectRepl connectRepl = this.getRasmgService().connect(connectReq);

            this.clientID = connectRepl.getClientId();
            this.clientUUID = connectRepl.getClientUUID();
            this.keepAliveTimeout = connectRepl.getKeepAliveTimeout();

            startRasmgrKeepAlive();

        } catch (StatusRuntimeException ex) {
            throw GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
        } catch (Exception e) {
            throw new ODMGRuntimeException(e.getMessage());
        }
    }

    @Override
    public Object queryRequest(String parameters) throws RasQueryExecutionFailedException {
        try {
            byte[] bytes = parameters.getBytes("8859_1");

            BeginStreamedHttpQueryReq beginStreamedHttpQueryReq = BeginStreamedHttpQueryReq.newBuilder()
                    .setClientUuid(this.clientUUID)
                    .setData(ByteString.copyFrom(bytes))
                    .build();

            StreamedHttpQueryRepl streamedHttpQueryRepl = 
                    this.getRasServerService().beginStreamedHttpQuery(
                            beginStreamedHttpQueryReq);
            long bytesLeft = streamedHttpQueryRepl.getBytesLeft();
            String requestUUID = streamedHttpQueryRepl.getUuid();

            Debug.enterVerbose("RasNetImplementation.getResponse: start.");

            Object result = null;
            byte[] currentChunk = streamedHttpQueryRepl.getData().toByteArray();
            int currentChunkSize = currentChunk.length;
            ByteArrayInputStream dataByteStream = new ByteArrayInputStream(currentChunk);
            DataInputStream in = new DataInputStream(dataByteStream);
            
            byte[] b1 = new byte[1];
            byte[] b4 = new byte[4];
            byte[] b8 = new byte[8];
            byte endianess = 0;
            String collType = null;
            int numberOfResults = 0;
            int arraySize = 0;
            byte[] arrayData = null;
            int totalReadBytes = 0;
            int currentlyReadBytes = 0;
            DBag resultBag;
            RasGMArray res = null;
            try {
                currentChunkSize -= in.read(b1);
                int resultType = b1[0];
                switch (resultType) {
                    case RESPONSE_OK:
                    case RESPONSE_OK_NEGATIVE:
                        //Nothing todo
                        break;

                    // +++++++++++++++++++++++++++++++++++++++++++++++++
                    case RESPONSE_MDDS:
                        // read Endianess
                        while (in.read(b1) == 0) ;
                        --currentChunkSize;
                        endianess = b1[0];

                        // read Collection Type
                        collType = RasUtils.readString(in);
                        currentChunkSize -= (collType.length() + 1);

                        // read NumberOfResults
                        while (in.available() < 4) ;
                        currentChunkSize -= in.read(b4);
                        numberOfResults = RasUtils.ubytesToInt(b4, endianess);

                        // Initialize return-set and parameters
                        resultBag = new RasBag();
                        String mddBaseType = null;
                        String domain = null;
                        String oid = "";
                        RasOID roid = null;

                        // do this for each result
                        for (int x = 0; x < numberOfResults; x++) {
                            //read mddBaseType
                            mddBaseType = RasUtils.readString(in);
                            currentChunkSize -= (mddBaseType.length() + 1);

                            // read spatialDomain
                            domain = RasUtils.readString(in);
                            currentChunkSize -= (domain.length() + 1);

                            // read OID
                            oid = RasUtils.readString(in);
                            currentChunkSize -= (oid.length() + 1);
                            roid = new RasOID(oid);

                            // read size of binData
                            while (in.available() < 8) ;
                            currentChunkSize -= in.read(b8);

                            long tmpArraySize = RasUtils.ubytesToLong(b8, endianess);
                            if (tmpArraySize > Integer.MAX_VALUE) {
                                String msg = "Cannot handle array result from rasdaman of size " + tmpArraySize +
                                        " bytes; maximum supported size is " + Integer.MAX_VALUE + " bytes";
                                Debug.talkCritical("RasNetImplementation.queryRequest: " + msg);
                                Debug.leaveVerbose("RasNetImplementation.queryRequest: " + msg);
                                throw new RasClientInternalException("RasNetImplementation", "queryRequest()", msg);
                            }

                            arraySize = (int) tmpArraySize;
                            arrayData = new byte[arraySize];
                            totalReadBytes = 0;
                            currentlyReadBytes = 0;

                            while (totalReadBytes < arraySize) {
                                int bytesToReadFromChunk = Math.min(currentChunkSize, arraySize - totalReadBytes);
                                currentlyReadBytes = in.read(arrayData, totalReadBytes, bytesToReadFromChunk);
                                if (currentlyReadBytes == -1) {
                                    break;
                                }
                                totalReadBytes += currentlyReadBytes;
                                currentChunkSize -= currentlyReadBytes;
                                
                                // read next chunk if the current is fully processed already
                                if (currentChunkSize == 0 && bytesLeft > 0) {
                                    GetNextStreamedHttpQueryReq nextStreamedHttpQueryReq
                                            = GetNextStreamedHttpQueryReq.newBuilder()
                                                    .setUuid(requestUUID).build();
                                    StreamedHttpQueryRepl nextStreamedHttpQueryRepl
                                            = this.getRasServerService().getNextStreamedHttpQuery(
                                                    nextStreamedHttpQueryReq);
                                    bytesLeft = nextStreamedHttpQueryRepl.getBytesLeft();
                                    in.close();
                                    currentChunk = nextStreamedHttpQueryRepl.getData().toByteArray();
                                    currentChunkSize = currentChunk.length;
                                    in = new DataInputStream(new ByteArrayInputStream(currentChunk));
                                    System.gc();
                                }
                            }
                            
                            // to make sure it doesn't happen that we go into the next loop with a bit too small
                            // chunk, that cannot cover reading the base type, spatial domain, oid and data size,
                            // we extend the current chunk if it's smaller than 1000 bytes.
                            if (bytesLeft > 0 && currentChunkSize < 1000) {
                                GetNextStreamedHttpQueryReq nextStreamedHttpQueryReq
                                        = GetNextStreamedHttpQueryReq.newBuilder()
                                                .setUuid(requestUUID).build();
                                StreamedHttpQueryRepl nextStreamedHttpQueryRepl
                                        = this.getRasServerService().getNextStreamedHttpQuery(
                                                nextStreamedHttpQueryReq);
                                bytesLeft = nextStreamedHttpQueryRepl.getBytesLeft();
                                // get next chunk data
                                ByteString nextChunk = nextStreamedHttpQueryRepl.getData();
                                int nextChunkSize = nextChunk.size();
                                // set up new chunk, combined from current and next chunks
                                int newChunkSize = currentChunkSize + nextChunkSize;
                                currentChunk = new byte[newChunkSize];
                                // in first half read the current chunk
                                in.read(currentChunk, 0, currentChunkSize);
                                in.close();
                                // in second half read the next chunk
                                nextChunk.copyTo(currentChunk, currentChunkSize);
                                currentChunkSize = newChunkSize;
                                in = new DataInputStream(new ByteArrayInputStream(currentChunk));
                                System.gc();
                            }

                            RasType rType = RasType.getAnyType(mddBaseType);
                            RasBaseType rb = null;

                            if (rType.getClass().getName().equals("rasj.RasMArrayType")) {
                                RasMArrayType tmp = (RasMArrayType) rType;
                                rb = tmp.getBaseType();
                            } else {
                                Debug.talkCritical("RasNetImplementation.getResponse: collection element is no MArray.");
                                Debug.leaveVerbose("RasNetImplementation.getResponse: done, with exception.");
                                throw new RasClientInternalException("RasHttpRequest", 
                                        "execute()", "element of MDD Collection is no MArray");
                            }
                            if (rb.isBaseType()) {
                                if (rb.isStructType()) {
                                    RasStructureType sType = (RasStructureType) rb;
                                    res = new RasGMArray(new RasMInterval(domain), 0, false);
                                    res.setTypeLength(rb.getSize());
                                    res.setArraySize(arraySize);
                                    res.setArray(arrayData);
                                    res.setTypeStructure(mddBaseType);
                                    //insert into result set
                                    resultBag.add(res);
                                } else {
                                    // It is a primitiveType
                                    RasPrimitiveType pType = (RasPrimitiveType) rb;
                                    switch (pType.getTypeID()) {
                                        case RAS_BOOLEAN:
                                        case RAS_BYTE:
                                        case RAS_CHAR:
                                            res = new RasMArrayByte(new RasMInterval(domain), false);
                                            break;
                                        case RAS_SHORT:
                                            res = new RasMArrayShort(new RasMInterval(domain), false);
                                            break;

                                        case RAS_USHORT:
                                            byte[] tmData = new byte[arraySize * 2];
                                            for (int i = 0; i < arraySize * 2;) {
                                                tmData[i] = 0;
                                                tmData[i + 1] = 0;
                                                tmData[i + 2] = arrayData[i / 2];
                                                tmData[i + 3] = arrayData[i / 2 + 1];
                                                i = i + SIZE_OF_INTEGER;
                                            }
                                            arrayData = tmData;
                                            res = new RasMArrayInteger(new RasMInterval(domain), false);
                                            break;

                                        case RAS_INT:
                                        case RAS_LONG:
                                            res = new RasMArrayInteger(new RasMInterval(domain), false);
                                            break;
                                        case RAS_ULONG:
                                            byte[] tmpData = new byte[arraySize * 2];
                                            for (int i = 0; i < arraySize * 2;) {
                                                tmpData[i] = 0;
                                                tmpData[i + 1] = 0;
                                                tmpData[i + 2] = 0;
                                                tmpData[i + 3] = 0;
                                                tmpData[i + 4] = arrayData[i / 2];
                                                tmpData[i + 5] = arrayData[i / 2 + 1];
                                                tmpData[i + 6] = arrayData[i / 2 + 2];
                                                tmpData[i + 7] = arrayData[i / 2 + 3];
                                                i = i + SIZE_OF_LONG;
                                            }
                                            arrayData = tmpData;
                                            res = new RasMArrayLong(new RasMInterval(domain), false);
                                            break;
                                        case RAS_FLOAT:
                                            res = new RasMArrayFloat(new RasMInterval(domain), false);
                                            break;
                                        case RAS_DOUBLE:
                                            res = new RasMArrayDouble(new RasMInterval(domain), false);
                                            break;
                                        default:
                                            res = new RasGMArray(new RasMInterval(domain), pType.getSize(), false);
                                    }
                                    res.setArray(arrayData);
                                    res.setOID(roid);
                                    res.setTypeStructure(mddBaseType);
                                    resultBag.add(res);
                                }
                            } else {
                                Debug.talkCritical("RasNetImplementation.getResponse: type is not base type.");
                                Debug.leaveVerbose("RasNetImplementation.getResponse: done, type is not base type.");
                                throw new RasClientInternalException("RasHttpRequest", "execute()", "Type of MDD is no Base Type");
                            }
                        } // for

                        result = resultBag;
                        in.close();

                        break;

                    // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                    case RESPONSE_SKALARS:
                        // read Endianess
                        while (in.read(b1) == 0) ;
                        endianess = b1[0];
                        // read Collection Type
                        collType = RasUtils.readString(in);
                        RasType rt = new RasType();
                        try {
                            rt = rt.getAnyType(collType);
                        } catch (Exception e) {
                            Debug.talkCritical("RasNetImplementation.getResponse: type not supported: " + rt);
                            Debug.leaveVerbose("RasNetImplementation.getResponse: done, unsupported type");
                            throw new RasTypeNotSupportedException(rt + " as RasCollectionType");
                        }
                        if (rt.getTypeID() != RasGlobalDefs.RAS_COLLECTION) {
                            Debug.leaveCritical("RasNetImplementation.getResponse: done. type not supported: " + rt);
                            throw new RasTypeNotSupportedException(rt + " as RasCollectionType");
                        }

                        // read NumberOfResults
                        while (in.available() < 4)
                    ;
                        in.read(b4);
                        numberOfResults = RasUtils.ubytesToInt(b4, endianess);

                        // Initailize return-list
                        resultBag = new RasBag();

                        // do this for each result
                        for (int x = 0; x < numberOfResults; x++) {
                            // read elementType
                            String elementType = RasUtils.readString(in);
                            RasType et = new RasType();
                            et = ((RasCollectionType) rt).getElementType();
                            // read size of binData
                            while (in.available() < 4)
                        ;
                            in.read(b4);
                            arraySize = RasUtils.ubytesToInt(b4, endianess);
                            // read binData
                            arrayData = new byte[arraySize];
                            totalReadBytes = 0;
                            currentlyReadBytes = 0;
                            while ((currentlyReadBytes != -1) && (totalReadBytes < arraySize)) {
                                currentlyReadBytes = in.read(arrayData, totalReadBytes, arraySize - totalReadBytes);
                                totalReadBytes += currentlyReadBytes;
                            }

                            ByteArrayInputStream bis = new ByteArrayInputStream(arrayData);
                            DataInputStream dis = new DataInputStream(bis);
                            // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                            resultBag.add(getElement(dis, et, arrayData));
                        }
                        result = resultBag;
                        // close stream
                        in.close();
                        break;

                    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                    case RESPONSE_ERROR:
                        // read Endianess
                        while (in.read(b1) == 0) ;
                        endianess = b1[0];
                        // read Error Number
                        while (in.available() < 4) ;
                        in.read(b4);
                        int errNo = RasUtils.ubytesToInt(b4, endianess);
                        // read Line Number
                        while (in.available() < 4) ;
                        in.read(b4);
                        int lineNo = RasUtils.ubytesToInt(b4, endianess);
                        // read Column Number
                        while (in.available() < 4) ;
                        in.read(b4);
                        int colNo = RasUtils.ubytesToInt(b4, endianess);
                        // read token
                        String token = RasUtils.readString(in);
                        Debug.leaveCritical("RasNetImplementation.getResponse: query failed, errNo=" + errNo + ", lineNo=" + lineNo + ", colNo=" + colNo + ", token=" + token);
                        throw new RasQueryExecutionFailedException(errNo, lineNo, colNo, token);
                    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                    case RESPONSE_INT:
                        // read Integer Value
                        while (in.available() < 4)
                    ;
                        in.read(b4);
                        result = new Integer(RasUtils.ubytesToInt(b4, endianess));
                        break;

                    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                    case RESPONSE_OID:
                        // read Values
                        String sys = RasUtils.readString(in);
                        String base = RasUtils.readString(in);
                        double d = in.readDouble();
                        resultBag = new RasBag();
                        resultBag.add(new RasOID(sys, base, d));
                        result = resultBag;
                        // close stream
                        in.close();
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {
                Debug.talkCritical("RasNetImplementation.getResponse: " + e.getMessage());
                Debug.leaveVerbose("RasNetImplementation.getResponse: done, communication exception.");
                throw new RasClientInternalException("RasNetImplementation", "getResponse()", e.getMessage());
            } catch (RasResultIsNoIntervalException e) {
                Debug.talkCritical("RasNetImplementation.getResponse: " + e.getMessage());
                Debug.leaveVerbose("RasNetImplementation.getResponse: done, result not an interval.");
                throw new RasClientInternalException("RasNetImplementation", "getResponse()", e.getMessage());
            }

            Debug.leaveVerbose("RasNetImplementation.getResponse: done. result=" + result);
            return result;

        } catch (IOException e) {
            Debug.talkCritical("RasNetImplementation.executeQueryRequest: " + e.getMessage());
            Debug.leaveVerbose("RasNetImplementation.executeQueryRequest: done, " + e.getMessage());
            throw new RasClientInternalException("RasNetImplementation", "executeQueryRequest()", e.getMessage());
        } catch (StatusRuntimeException ex) {
            throw GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
        }
    }

    @Override
    public String getTypeStructure(String typename, int typetype) {
        try {
            GetTypeStructureReq getTypeStructureReq = GetTypeStructureReq.newBuilder()
                    .setClientId(clientID)
                    .setTypeName(typename)
                    .setTypeType(typetype)
                    .build();

            GetTypeStructureRepl getTypeStructureRepl = this.getRasServerService().getTypeStructure(getTypeStructureReq);

            return getTypeStructureRepl.getTypeStructure();

        } catch (StatusRuntimeException ex) {
            throw GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
        } catch (Exception e) {
            throw new ODMGRuntimeException(e.getMessage());
        }
    }

    private synchronized void initRasmgrServices() {
        if (this.rasmgService == null) {
            this.rasmgrServiceChannel = this.serviceFactory.createChannel(this.rasMgrHost, this.rasMgrPort);
            this.rasmgService = this.serviceFactory.createRasmgrClientService(this.rasmgrServiceChannel);
            this.rasmgrHealthService = this.serviceFactory.createHealthService(this.rasmgrServiceChannel);
        }
    }

    private RasmgrClientServiceGrpc.RasmgrClientServiceBlockingStub getRasmgService() {
        this.initRasmgrServices();

        if (!GrpcUtils.isServerAlive(rasmgrHealthService, Constants.SERVICE_CALL_TIMEOUT)) {
            throw new RasConnectionFailedException(MANAGER_CONN_FAILED, "");
        }

        return rasmgService;
    }

    private synchronized void initRasserverServices() {
        if (this.rasServerService == null) {
            this.rasServerServiceChannel = this.serviceFactory.createChannel(this.rasServerHost, this.rasServerPort);
            this.rasServerService = this.serviceFactory.createClientRasServerService(this.rasServerServiceChannel);
            this.rasserverHealthService = this.serviceFactory.createHealthService(this.rasServerServiceChannel);
        }
    }

    private synchronized ClientRassrvrServiceGrpc.ClientRassrvrServiceBlockingStub getRasServerService() {
        this.initRasserverServices();

        if (!GrpcUtils.isServerAlive(rasserverHealthService, Constants.SERVICE_CALL_TIMEOUT)) {
            throw new RasConnectionFailedException(MANAGER_CONN_FAILED, "");
        }

        return this.rasServerService;
    }

    private void disconnectClient() {
        try {
            DisconnectReq disconnectReq = DisconnectReq.newBuilder()
                                          .setClientUUID(clientUUID)
                                          .build();

            this.getRasmgService().disconnect(disconnectReq);
        } catch (StatusRuntimeException ex) {
            throw GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
        } catch (Exception ex) {
            throw new ODMGRuntimeException(ex.getMessage());
        }

        closeRasMgrService();
        closeRasServerService();
    }

    private synchronized void closeRasMgrService() {
        if (this.rasmgService != null) {

            this.rasmgService = null;

            try {
                this.rasmgrServiceChannel.shutdown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private synchronized void closeRasServerService() {
        if (this.rasServerService != null) {
            this.rasServerService = null;
            this.rasServerHost = null;
            this.rasServerPort = -1;

            try {
                this.rasServerServiceChannel.shutdown();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private RasOID executeGetNewObjectId() {
        try {
            GetNewOidReq getNewOidReq = GetNewOidReq.newBuilder()
                                        .setClientId(clientID)
                                        .setObjectType(1)
                                        .build();
            final RasOID[] rasOID = {null};

            GetNewOidRepl getNewOidRepl = this.getRasServerService().getNewOid(getNewOidReq);

            return new RasOID(getNewOidRepl.getOid());

        } catch (StatusRuntimeException ex) {
            throw GrpcUtils.convertStatusToRuntimeException(ex.getStatus());
        } catch (Exception e) {
            throw new ODMGRuntimeException(e.getMessage());
        }
    }

    private static Object getElement(DataInputStream dis, RasType et, byte[] binData) throws IOException, RasResultIsNoIntervalException {
        Object ret = null;
        switch (et.getTypeID()) {
        case RasGlobalDefs.RAS_MINTERVAL:
            ret = new RasMInterval(new String(binData));
            break;
        case RasGlobalDefs.RAS_SINTERVAL:
            ret = new RasSInterval(new String(binData));
            break;
        case RasGlobalDefs.RAS_POINT:
            ret = new RasPoint(new String(binData));
            break;
        case RasGlobalDefs.RAS_OID:
            ret = new RasOID(new String(binData));
            break;
        case RAS_BOOLEAN:
            if (dis.readUnsignedByte() == 1) {
                return true;
            } else {
                return false;
            }
        case RAS_CHAR:
            ret = dis.readUnsignedByte();
            break;
        case RAS_BYTE:
            ret = dis.readByte();
            break;
        case RAS_DOUBLE:
            double d = dis.readDouble();
            ret = new Double(d);
            break;
        case RAS_FLOAT:
            float f = dis.readFloat();
            ret = new Float(f);
            break;
        case RAS_ULONG:
            byte[] bu = new byte[8];
            bu[0] = 0;
            bu[1] = 0;
            bu[2] = 0;
            bu[3] = 0;
            bu[4] = dis.readByte();
            bu[5] = dis.readByte();
            bu[6] = dis.readByte();
            bu[7] = dis.readByte();
            ByteArrayInputStream bis2 = new ByteArrayInputStream(bu);
            DataInputStream dis2 = new DataInputStream(bis2);
            long ul = dis2.readLong();
            ret = new Long(ul);
            break;
        case RAS_LONG:
        case RAS_INT:
            int i = dis.readInt();
            ret = new Integer(i);
            break;
        case RAS_USHORT:
            int j = dis.readUnsignedShort();
            ret = new Integer(j);
            break;
        case RAS_SHORT:
            ret = new Short(dis.readShort());
            break;
        case RAS_STRUCTURE:
        case RAS_RGB:
            RasStructureType st = (RasStructureType) et;
            ret = new RasStructure(st, dis);
            break;
        default:
            Debug.talkCritical("RasNetImplementation.getResponse: type not supported: " + et);
            Debug.leaveVerbose("RasNetImplementation.getResponse: done, unsupported type.");
            throw new RasTypeNotSupportedException(et + " as ElementType ");
        }
        return ret;
    }

    private synchronized void startRasmgrKeepAlive() {
        if (this.rasmgrKeepAlive == null) {
            this.rasmgrKeepAlive = new RasmgrKeepAlive(this.getRasmgService(), this.clientUUID, keepAliveTimeout);
            this.rasmgrKeepAlive.start();
        } else {
            throw new AssertionError("Cannot call the startRasmgrKeepAlive method twice without calling stopRasmgrKeepAlive in between.");
        }
    }

    private synchronized void stopRasmgrKeepAlive() {
        this.rasmgrKeepAlive.stop();
        this.rasmgrKeepAlive = null;
    }


    private synchronized void startRasserverKeepAlive() {
        if (this.rasserverKeepAlive == null) {
            this.rasserverKeepAlive = new RasserverKeepAlive(this.getRasServerService(),
                    this.clientUUID,
                    this.sessionId,
                    this.keepAliveTimeout);
            this.rasserverKeepAlive.start();
        } else {
            throw new AssertionError("Cannot call the startRasserverKeepAlive method twice without calling startRasserverKeepAlive in between.");
        }
    }

    private synchronized void stopRasserverKeepAlive() {
        this.rasserverKeepAlive.stop();
        this.rasserverKeepAlive = null;
    }

}
