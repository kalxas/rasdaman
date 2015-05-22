package org.rasdaman.rasnet.communication;

import com.google.protobuf.*;
import org.odmg.*;
import org.rasdaman.rasnet.client.Channel;
import org.rasdaman.rasnet.client.ChannelConfig;
import org.rasdaman.rasnet.client.ClientController;
import org.rasdaman.rasnet.exception.ConnectionTimeoutException;
import org.rasdaman.rasnet.exception.RasnetException;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.message.internal.Internal;
import org.rasdaman.rasnet.service.ClientRasServerService.*;
import org.rasdaman.rasnet.service.RasmgrClientService.*;
import org.rasdaman.rasnet.util.MessageContainer;
import org.rasdaman.rasnet.util.DigestUtils;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.zeromq.ZMQ;
import rasj.*;
import rasj.clientcommhttp.RasCommDefs;
import rasj.clientcommhttp.RasUtils;
import rasj.global.Debug;
import rasj.global.RasGlobalDefs;
import rasj.odmg.*;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.UUID;

public class RasRasnetImplementation implements RasImplementationInterface, RasCommDefs, RasGlobalDefs {

    private RasMgrClientService.BlockingInterface rasmgService;
    private ClientRassrvrService.BlockingInterface rasServerService;
    private Channel rasmgrServiceChannel;
    private Channel rasServerServiceChannel;

    private String rasServerHost;
    private int rasServerPort;

    private String rasServer = "";
    private String rasMgrHost = "";
    private int rasMgrPort = 7001;
    private String userIdentification = "rasguest:8e70a429be359b6dace8b5b2500dedb0";
    private String databaseName = "";
    private String capability = "dummy";
    private int maxRetry = 5;    // was 120; -- PB 2003-nov-20

    private RasTransaction transaction = null;
    private RasDatabase database = null;
    private RasOQLQuery query = null;

    private int accessMode = 0;
    private boolean readWrite = false;
    private int dbIsOpen = 0;
    private int taIsOpen = 0;
    private int clientID = 0;
    private String clientUUID;
    private String sessionId;

    private String errorStatus = "";

    private ClientController controller;

    /* START - KEEP ALIVE */

    private long keepAliveTimeout;

    private ZMQ.Context rasmgrKeepAliveContext;
    private ZMQ.Socket rasmgrKeepAliveSocket;
    private String rasmgrKeepAliveEndpoint;
    private RasmgrKeepAliveRunner rasmgrKeepAliveRunner;

    private ZMQ.Context rasserverKeepAliveContext;
    private ZMQ.Socket rasserverKeepAliveSocket;
    private String rasserverKeepAliveEndpoint;
    private RasserverKeepAliveRunner rasserverKeepAliveRunner;

    /* END - KEEP ALIVE */

    public RasRasnetImplementation(String server) {
        Debug.enterVerbose("RasRNPImplementation.RasRNPImplementation start. server=" + server);
        try {
            StringTokenizer t = new StringTokenizer(server, "/");
            String xxx = t.nextToken();
            this.rasMgrHost = ZmqUtil.toTcpAddress(t.nextToken("/:"));
            String portStr = t.nextToken(":");
            this.rasMgrPort = Integer.parseInt(portStr);
            this.controller = new ClientController();
        } catch (NoSuchElementException e) {
            Debug.talkCritical("RasRNPImplementation.RasRNPImplementation: " + e.getMessage());
            Debug.leaveVerbose("RasRNPImplementation.RasRNPImplementation done: " + e.getMessage());
            throw new RasConnectionFailedException(RasGlobalDefs.URL_FORMAT_ERROR, server);
        }
        Debug.leaveVerbose("RasRNPImplementation.RasRNPImplementation done.");
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
        Debug.enterVerbose("RasRNPImplementation.getObjectId start.");
        String oid = null;
        if (obj instanceof RasObject) {
            RasOID roid = ((RasObject) obj).getOID();
            oid = roid.toString();
            if (!((RasObject) obj).getOID().isValid()) {
                roid = executeGetNewObjectId();
                oid = roid.toString();
                ((RasObject) obj).setOID(roid);
            } else {
                Debug.leaveCritical("RasRNPImplementation.getObjectId done. not yet implemented.");
                throw new NotImplementedException();
            }
        }
        Debug.leaveVerbose("RasRNPImplementation.getObjectId done. oid=" + oid);
        return oid;
    }

    @Override
    public Database getDatabase(Object obj) {
        throw new NotImplementedException();
    }

    @Override
    public void openDB(String name, int accessMode) throws ODMGException {
        Debug.enterVerbose("RasRNPImplementation.openDB start. db=" + name + ", accessMode=" + accessMode);
        databaseName = name;
        this.accessMode = accessMode;
        readWrite = (accessMode != Database.OPEN_READ_ONLY) ? true : false;

        OpenDbReq openDbReq = OpenDbReq.newBuilder()
                .setClientId(this.clientID)
                .setClientUUID(this.clientUUID)
                .setDatabaseName(this.databaseName)
                .build();

        try {

            OpenDbRepl openRasmgrDbRepl = this.getRasmgService().openDb(controller, openDbReq);
            if (controller.failed()) {
                throw new RasnetException(controller.errorText());
            }

            rasServerHost = openRasmgrDbRepl.getServerHostName();
            rasServerPort = openRasmgrDbRepl.getPort();
            sessionId = openRasmgrDbRepl.getDbSessionId();

            OpenServerDatabaseReq openServerDatabaseReq = OpenServerDatabaseReq.newBuilder()
                    .setClientId(clientID)
                    .setDatabaseName(databaseName)
                    .build();

            getRasServerService().openServerDatabase(controller, openServerDatabaseReq);

            stopRasmgrKeepAlive();

            if (controller.failed()) {
                throw new RasnetException(controller.errorText());
            }

            startRasserverKeepAlive();

        } catch (ServiceException ex) {
            throw new RasnetException(ex);
        } catch (ConnectionTimeoutException e) {
            e.printStackTrace();
        }


        dbIsOpen = 1;
        Debug.leaveVerbose("RasRNPImplementation.openDB done.");
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

            this.getRasServerService().closeServerDatabase(controller, closeServerDatabaseReq);

            this.stopRasserverKeepAlive();
            if (controller.failed()) {
                throw new RasnetException(controller.errorText());
            }

            this.getRasmgService().closeDb(controller, closeDbReq);
            if (controller.failed()) {
                throw new RasnetException(controller.errorText());
            }

            Debug.talkVerbose("RasRNPImplementation.closeDB.");

            dbIsOpen = 0;
            disconnectClient();
        } catch (ServiceException ex) {
            throw new ODMGException(ex.getMessage());
        } catch (ConnectionTimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beginTA() {
        BeginTransactionReq transactionReq = BeginTransactionReq.newBuilder()
                .setClientId(this.clientID)
                .setRw(readWrite)
                .build();

        try {
            this.getRasServerService().beginTransaction(controller, transactionReq);
        } catch (ServiceException e) {
            throw new RasnetException(controller.errorText());
        } catch (ConnectionTimeoutException e) {
            e.printStackTrace();
        }
        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }
    }

    @Override
    public boolean isOpenTA() {
        IsTransactionOpenReq isTransactionOpenReq = IsTransactionOpenReq.newBuilder()
                .setClientId(clientID)
                .build();
        IsTransactionOpenRepl reply;
        try {
            reply = this.getRasServerService().isTransactionOpen(controller, isTransactionOpenReq);
        } catch (ServiceException e) {
            throw new RasnetException(e);
        } catch (ConnectionTimeoutException e) {
            throw new RasnetException(e);
        }

        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }

        return reply.getIsOpen();
    }

    @Override
    public void commitTA() {
        CommitTransactionReq commitTransactionReq = CommitTransactionReq.newBuilder()
                .setClientId(clientID)
                .build();

        try {
            this.getRasServerService().commitTransaction(controller, commitTransactionReq);
        } catch (ServiceException e) {
            throw new RasnetException(e);
        } catch (ConnectionTimeoutException e) {
            e.printStackTrace();
        }

        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }
    }

    @Override
    public void abortTA() {
        AbortTransactionReq abortTransactionReq = AbortTransactionReq.newBuilder()
                .setClientId(clientID)
                .build();
        try {
            this.getRasServerService().abortTransaction(controller, abortTransactionReq);
        } catch (ServiceException e) {
            throw new RasnetException(e);
        } catch (ConnectionTimeoutException e) {
            e.printStackTrace();
        }

        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
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
        ConnectReq connectReq = ConnectReq.newBuilder()
                .setUserName(userName)
                .setPasswordHash(passwordHash)
                .build();

        try {
            ConnectRepl connectRepl = this.getRasmgService().connect(controller, connectReq);
            if (controller.failed()) {
                throw new RasnetException(controller.errorText());
            }

            clientID = connectRepl.getClientId();
            clientUUID = connectRepl.getClientUUID();
            keepAliveTimeout = connectRepl.getKeepAliveTimeout();
            startRasmgrKeepAlive();

        } catch (ServiceException e) {
            throw new RasnetException(e);
        } catch (ConnectionTimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object queryRequest(String parameters) throws RasQueryExecutionFailedException {
        try {
            byte[] bytes = parameters.getBytes("8859_1");
            ExecuteHttpQueryReq executeHttpQueryReq = ExecuteHttpQueryReq.newBuilder()
                    .setClientId(clientID)
                    .setData(ByteString.copyFrom(bytes))
                    .setDataLength(bytes.length)
                    .build();

            ExecuteHttpQueryRepl executeHttpQueryRepl = this.getRasServerService().executeHttpQuery(controller, executeHttpQueryReq);
            if (controller.failed()) {
                throw new RasnetException(controller.errorText());
            }
            return getResponse(executeHttpQueryRepl.getData().toByteArray());

        } catch (ServiceException e) {
            throw new RasnetException(e);
        } catch (UnsupportedEncodingException e) {
            Debug.talkCritical("RasRNPImplementation.executeQueryRequest: " + e.getMessage());
            Debug.leaveVerbose("RasRNPImplementation.executeQueryRequest: done, " + e.getMessage());
            throw new RasClientInternalException("RasRNPImplementation", "executeQueryRequest()", e.getMessage());
        } catch (ConnectionTimeoutException e) {
            throw new RasnetException(e);
        }
    }

    @Override
    public String getTypeStructure(String typename, int typetype) {
        GetTypeStructureReq getTypeStructureReq = GetTypeStructureReq.newBuilder()
                .setClientId(clientID)
                .setTypeName(typename)
                .setTypeType(typetype)
                .build();

        try {
            GetTypeStructureRepl getTypeStructureRepl = this.getRasServerService().getTypeStructure(controller, getTypeStructureReq);
            if (controller.failed()) {
                throw new RasnetException(controller.errorText());
            }

            return getTypeStructureRepl.getTypeStructure();

        } catch (ServiceException e) {
            throw new RasnetException(e);
        } catch (ConnectionTimeoutException e) {
            throw new RasnetException(e);
        }
    }

    private RasMgrClientService.BlockingInterface getRasmgService() throws ConnectionTimeoutException {
        synchronized (this) {
            if (rasmgService == null) {
                rasmgrServiceChannel = new Channel(ZmqUtil.toAddressPort(this.rasMgrHost, this.rasMgrPort), new ChannelConfig());
                rasmgService = RasMgrClientService.newBlockingStub(rasmgrServiceChannel);
            }
        }
        return rasmgService;
    }

    private ClientRassrvrService.BlockingInterface getRasServerService() throws ConnectionTimeoutException {

        synchronized (this) {
            if (rasServerService == null) {
                rasServerServiceChannel = new Channel(ZmqUtil.toAddressPort(this.rasServerHost, this.rasServerPort), new ChannelConfig());
                rasServerService = ClientRassrvrService.newBlockingStub(rasServerServiceChannel);
            }
        }
        return rasServerService;
    }

    private void disconnectClient() {
        DisconnectReq disconnectReq = DisconnectReq.newBuilder()
                .setClientUUID(clientUUID)
                .build();
        try {
            this.getRasmgService().disconnect(controller, disconnectReq);
        } catch (ServiceException e) {
            throw new RasnetException(e);
        } catch (ConnectionTimeoutException e) {
            e.printStackTrace();
        }
        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }

        closeRasMgrService();
        closeRasServerService();
    }

    private synchronized void closeRasMgrService() {
        if (rasmgService != null) {
            rasmgService = null;
            try {
                rasmgrServiceChannel.close();
            } catch (Exception e) {
                throw new RasnetException(e);
            }
        }
    }

    private synchronized void closeRasServerService() {
        if (rasServerService != null) {
            rasServerService = null;
            rasServerHost = null;
            rasServerPort = -1;
            try {
                rasServerServiceChannel.close();
            } catch (Exception e) {
                //TODO-GM
                throw new RasnetException(e);
            }
        }
    }

    private RasOID executeGetNewObjectId() {
        GetNewOidReq getNewOidReq = GetNewOidReq.newBuilder()
                .setClientId(clientID)
                .setObjectType(1)
                .build();
        final RasOID[] rasOID = {null};

        try {
            GetNewOidRepl getNewOidRepl = this.getRasServerService().getNewOid(controller, getNewOidReq);
            if (controller.failed()) {
                throw new RasnetException(controller.errorText());
            }

            return new RasOID(getNewOidRepl.getOid());
        } catch (ServiceException e) {
            throw new RasnetException(e);
        } catch (ConnectionTimeoutException e) {
            throw new RasnetException(e);
        }
    }

    private Object getResponse(byte[] opaqueAnswer)
            throws RasQueryExecutionFailedException {
        Debug.enterVerbose("RasRNPImplementation.getResponse: start.");

        Object result = null;
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(opaqueAnswer));
        byte[] b1 = new byte[1];
        byte[] b4 = new byte[4];
        byte endianess = 0;
        String collType = null;
        int numberOfResults = 0;
        int dataSize = 0;
        byte[] binData = null;
        int readBytes = 0;
        int readBytesTmp = 0;
        DBag resultBag;
        RasGMArray res = null;
        try {
            in.read(b1);
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
                    endianess = b1[0];

                    // read Collection Type
                    collType = RasUtils.readString(in);

                    // read NumberOfResults
                    while (in.available() < 4) ;
                    in.read(b4);
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

                        // read spatialDomain
                        domain = RasUtils.readString(in);

                        // read OID
                        oid = RasUtils.readString(in);
                        roid = new RasOID(oid);

                        // read size of binData
                        while (in.available() < 4) ;
                        in.read(b4);

                        dataSize = RasUtils.ubytesToInt(b4, endianess);

                        // read binData
                        binData = new byte[dataSize];
                        readBytes = 0;
                        readBytesTmp = 0;

                        while ((readBytesTmp != -1) && (readBytes < dataSize)) {
                            readBytesTmp = in.read(binData, readBytes, dataSize - readBytes);
                            readBytes += readBytesTmp;
                        }

                        RasType rType = RasType.getAnyType(mddBaseType);
                        RasBaseType rb = null;

                        if (rType.getClass().getName().equals("rasj.RasMArrayType")) {
                            RasMArrayType tmp = (RasMArrayType) rType;
                            rb = tmp.getBaseType();
                        } else {
                            Debug.talkCritical("RasRNPImplementation.getResponse: collection element is no MArray.");
                            Debug.leaveVerbose("RasRNPImplementation.getResponse: done, with exception.");
                            throw new RasClientInternalException("RasHttpRequest", "execute()", "element of MDD Collection is no MArray");
                        }
                        if (rb.isBaseType()) {
                            if (rb.isStructType()) {
                                RasStructureType sType = (RasStructureType) rb;
                                res = new RasGMArray(new RasMInterval(domain), 0);
                                res.setTypeLength(rb.getSize());
                                res.setArraySize(dataSize);
                                res.setArray(binData);
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
                                        res = new RasMArrayByte(new RasMInterval(domain));
                                        break;
                                    case RAS_SHORT:
                                        res = new RasMArrayShort(new RasMInterval(domain));
                                        break;

                                    case RAS_USHORT:
                                        byte[] tmData = new byte[dataSize * 2];
                                        for (int i = 0; i < dataSize * 2; ) {
                                            tmData[i] = 0;
                                            tmData[i + 1] = 0;
                                            tmData[i + 2] = binData[i / 2];
                                            tmData[i + 3] = binData[i / 2 + 1];
                                            i = i + SIZE_OF_INTEGER;
                                        }
                                        binData = tmData;
                                        res = new RasMArrayInteger(new RasMInterval(domain));
                                        break;

                                    case RAS_INT:
                                    case RAS_LONG:
                                        res = new RasMArrayInteger(new RasMInterval(domain));
                                        break;
                                    case RAS_ULONG:
                                        byte[] tmpData = new byte[dataSize * 2];
                                        for (int i = 0; i < dataSize * 2; ) {
                                            tmpData[i] = 0;
                                            tmpData[i + 1] = 0;
                                            tmpData[i + 2] = 0;
                                            tmpData[i + 3] = 0;
                                            tmpData[i + 4] = binData[i / 2];
                                            tmpData[i + 5] = binData[i / 2 + 1];
                                            tmpData[i + 6] = binData[i / 2 + 2];
                                            tmpData[i + 7] = binData[i / 2 + 3];
                                            i = i + SIZE_OF_LONG;
                                        }
                                        binData = tmpData;
                                        res = new RasMArrayLong(new RasMInterval(domain));
                                        break;
                                    case RAS_FLOAT:
                                        res = new RasMArrayFloat(new RasMInterval(domain));
                                        break;
                                    case RAS_DOUBLE:
                                        res = new RasMArrayDouble(new RasMInterval(domain));
                                        break;
                                    default:
                                        res = new RasGMArray(new RasMInterval(domain), pType.getSize());
                                }
                                res.setArray(binData);
                                res.setOID(roid);
                                res.setTypeStructure(mddBaseType);
                                resultBag.add(res);
                            }
                        } else {
                            Debug.talkCritical("RasRNPImplementation.getResponse: type is not base type.");
                            Debug.leaveVerbose("RasRNPImplementation.getResponse: done, type is not base type.");
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
                        Debug.talkCritical("RasRNPImplementation.getResponse: type not supported: " + rt);
                        Debug.leaveVerbose("RasRNPImplementation.getResponse: done, unsupported type");
                        throw new RasTypeNotSupportedException(rt + " as RasCollectionType");
                    }
                    if (rt.getTypeID() != RasGlobalDefs.RAS_COLLECTION) {
                        Debug.leaveCritical("RasRNPImplementation.getResponse: done. type not supported: " + rt);
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
                        dataSize = RasUtils.ubytesToInt(b4, endianess);
                        // read binData
                        binData = new byte[dataSize];
                        readBytes = 0;
                        readBytesTmp = 0;
                        while ((readBytesTmp != -1) && (readBytes < dataSize)) {
                            readBytesTmp = in.read(binData, readBytes, dataSize - readBytes);
                            readBytes += readBytesTmp;
                        }

                        ByteArrayInputStream bis = new ByteArrayInputStream(binData);
                        DataInputStream dis = new DataInputStream(bis);
                        // ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                        resultBag.add(getElement(dis, et, binData));
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
                    while (in.available() < 4)
                        ;
                    in.read(b4);
                    int errNo = RasUtils.ubytesToInt(b4, endianess);
                    // read Line Number
                    while (in.available() < 4)
                        ;
                    in.read(b4);
                    int lineNo = RasUtils.ubytesToInt(b4, endianess);
                    // read Column Number
                    while (in.available() < 4)
                        ;
                    in.read(b4);
                    int colNo = RasUtils.ubytesToInt(b4, endianess);
                    // read token
                    String token = RasUtils.readString(in);
                    Debug.leaveCritical("RasRNPImplementation.getResponse: query failed, errNo=" + errNo + ", lineNo=" + lineNo + ", colNo=" + colNo + ", token=" + token);
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
            Debug.talkCritical("RasRNPImplementation.getResponse: " + e.getMessage());
            Debug.leaveVerbose("RasRNPImplementation.getResponse: done, communication exception.");
            throw new RasClientInternalException("RasRNPImplementation", "getResponse()", e.getMessage());
        } catch (RasResultIsNoIntervalException e) {
            Debug.talkCritical("RasRNPImplementation.getResponse: " + e.getMessage());
            Debug.leaveVerbose("RasRNPImplementation.getResponse: done, result not an interval.");
            throw new RasClientInternalException("RasRNPImplementation", "getResponse()", e.getMessage());
        }

        Debug.leaveVerbose("RasRNPImplementation.getResponse: done. result=" + result);
        return result;
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
                Debug.talkCritical("RasRNPImplementation.getResponse: type not supported: " + et);
                Debug.leaveVerbose("RasRNPImplementation.getResponse: done, unsupported type.");
                throw new RasTypeNotSupportedException(et + " as ElementType ");
        }
        return ret;
    }

    private void startRasmgrKeepAlive() {

        this.rasmgrKeepAliveContext = ZMQ.context(1);
        this.rasmgrKeepAliveEndpoint = ZmqUtil.toInprocAddress(UUID.randomUUID().toString());

        this.rasmgrKeepAliveRunner = new RasmgrKeepAliveRunner();
        this.rasmgrKeepAliveRunner.start();

        this.rasmgrKeepAliveSocket = this.rasmgrKeepAliveContext.socket(ZMQ.PAIR);
        this.rasmgrKeepAliveSocket.connect(this.rasmgrKeepAliveEndpoint);
    }

    private void stopRasmgrKeepAlive() {
        Internal.InternalDisconnectRequest request = Internal.InternalDisconnectRequest.getDefaultInstance();
        MessageContainer reply = new MessageContainer();

        boolean success;

        try {

            success = ZmqUtil.isSocketWritable(this.rasmgrKeepAliveSocket, 0);

            if (success) {
                success = ZmqUtil.send(this.rasmgrKeepAliveSocket, request);
            }

            if (success) {
                success = ZmqUtil.isSocketReadable(this.rasmgrKeepAliveSocket, 0);
            }

            if (success) {
                success = ZmqUtil.receive(this.rasmgrKeepAliveSocket, reply);
            }

            if (success) {
                success = ZmqUtil.getType(Internal.InternalDisconnectReply.getDefaultInstance()).equals(reply.getType());
            }

            if (success && this.rasmgrKeepAliveRunner.isAlive()) {
                this.rasmgrKeepAliveRunner.join();
                this.rasmgrKeepAliveSocket.close();
                this.rasmgrKeepAliveContext.close();
            } else {
                this.rasmgrKeepAliveRunner.interrupt();
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class RasmgrKeepAliveRunner extends Thread {

        @Override
        public void run() {

            boolean keepRunning = true;
            ZMQ.Socket control = rasmgrKeepAliveContext.socket(ZMQ.PAIR);
            control.bind(rasmgrKeepAliveEndpoint);

            ZMQ.PollItem[] pollItems = new ZMQ.PollItem[]{
                    new ZMQ.PollItem(control, ZMQ.Poller.POLLIN)
            };

            while (keepRunning) {
                ZMQ.poll(pollItems, keepAliveTimeout);
                if (pollItems[0].isReadable()) {
                    MessageContainer containerMessage = new MessageContainer();

                    ZmqUtil.receive(control, containerMessage);
                    if (ZmqUtil.getType(Internal.InternalDisconnectRequest.getDefaultInstance()).equals(containerMessage.getType())) {
                        keepRunning = false;
                        Internal.InternalDisconnectReply reply = Internal.InternalDisconnectReply.getDefaultInstance();
                        ZmqUtil.send(control, reply);
                        control.close();
                    }
                } else {
                    KeepAliveReq keepAliveReq = KeepAliveReq.newBuilder()
                            .setClientUUID(clientUUID)
                            .build();
                    try {
                        getRasmgService().keepAlive(controller, keepAliveReq);
                    } catch (ServiceException e) {
                        //TODO-GM
                        e.printStackTrace();
                    } catch (ConnectionTimeoutException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    private void startRasserverKeepAlive() {

        this.rasserverKeepAliveContext = ZMQ.context(1);
        this.rasserverKeepAliveEndpoint = ZmqUtil.toInprocAddress(UUID.randomUUID().toString());

        this.rasserverKeepAliveRunner = new RasserverKeepAliveRunner();
        this.rasserverKeepAliveRunner.start();

        this.rasserverKeepAliveSocket = this.rasserverKeepAliveContext.socket(ZMQ.PAIR);
        this.rasserverKeepAliveSocket.connect(this.rasserverKeepAliveEndpoint);
    }

    private void stopRasserverKeepAlive() {
        Internal.InternalDisconnectRequest request = Internal.InternalDisconnectRequest.getDefaultInstance();
        MessageContainer reply = new MessageContainer();

        boolean success;
        try {

            success = ZmqUtil.isSocketWritable(this.rasserverKeepAliveSocket, 0);

            if (success) {
                success = ZmqUtil.send(this.rasserverKeepAliveSocket, request);
            }

            if (success) {
                success = ZmqUtil.isSocketReadable(this.rasserverKeepAliveSocket, 0);
            }
            if (success) {
                success = ZmqUtil.receive(this.rasserverKeepAliveSocket, reply);
            }

            if (success) {
                success = ZmqUtil.getType(Internal.InternalDisconnectReply.getDefaultInstance()).equals(reply.getType());
            }

            if (success && this.rasserverKeepAliveRunner.isAlive()) {
                this.rasserverKeepAliveRunner.join();
                this.rasserverKeepAliveSocket.close();
                this.rasserverKeepAliveContext.close();
            } else {
                this.rasserverKeepAliveRunner.interrupt();
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class RasserverKeepAliveRunner extends Thread {

        @Override
        public void run() {
            Communication.BaseMessage controlMessage = Communication.BaseMessage.getDefaultInstance();
            boolean keepRunning = true;
            ZMQ.Socket control = rasserverKeepAliveContext.socket(ZMQ.PAIR);
            control.bind(rasserverKeepAliveEndpoint);

            ZMQ.PollItem[] pollItems = new ZMQ.PollItem[]{
                    new ZMQ.PollItem(control, ZMQ.Poller.POLLIN)
            };

            while (keepRunning) {
                ZMQ.poll(pollItems, keepAliveTimeout);
                if (pollItems[0].isReadable()) {
                    MessageContainer containerMessage = new MessageContainer();

                    ZmqUtil.receive(control, containerMessage);
                    if (ZmqUtil.getType(Internal.InternalDisconnectRequest.getDefaultInstance()).equals(containerMessage.getType())) {
                        keepRunning = false;
                        Internal.InternalDisconnectReply reply = Internal.InternalDisconnectReply.getDefaultInstance();
                        ZmqUtil.send(control, reply);
                        control.close();
                    }
                } else {
                    KeepAliveRequest keepAliveReq = KeepAliveRequest.newBuilder()
                            .setClientUuid(clientUUID)
                            .setSessionId(sessionId)
                            .build();
                    try {
                        getRasServerService().keepAlive(controller, keepAliveReq);
                    } catch (ServiceException e) {
                        //TODO-GM
                        e.printStackTrace();
                    } catch (ConnectionTimeoutException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

}
