package org.rasdaman.rasnet.communication;

import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import org.odmg.*;
import org.rasdaman.rasnet.channel.Channel;
import org.rasdaman.rasnet.channel.ClientController;
import org.rasdaman.rasnet.exception.RasnetException;
import org.rasdaman.rasnet.message.CommonService;
import org.rasdaman.rasnet.service.ClientRasServerService.*;
import org.rasdaman.rasnet.service.DoNothing;
import org.rasdaman.rasnet.service.RasmgrClientService.*;
import rasj.*;
import rasj.clientcommhttp.RasCommDefs;
import rasj.clientcommhttp.RasUtils;
import rasj.global.Debug;
import rasj.global.RasGlobalDefs;
import rasj.odmg.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class RasRasnetImplementation implements RasImplementationInterface, RasCommDefs, RasGlobalDefs {

    private RasMgrClientService.Stub rasmgService;
    private ClientRassrvrService.Stub rasServerService;
    private Channel rasmgrServiceChannel;
    private Channel rasServiceServiceChannel;

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

    public RasRasnetImplementation(String server) {
        Debug.enterVerbose( "RasRNPImplementation.RasRNPImplementation start. server=" + server );
        try
        {
            StringTokenizer t=new StringTokenizer (server,"/");
            String xxx=t.nextToken();
            this.rasMgrHost ="tcp://" + t.nextToken("/:");
            String portStr = t.nextToken(":");
            this.rasMgrPort = Integer.parseInt(portStr);
            this.controller = new ClientController();
        }
        catch(NoSuchElementException e)
        {
            Debug.talkCritical( "RasRNPImplementation.RasRNPImplementation: " + e.getMessage()  );
            Debug.leaveVerbose( "RasRNPImplementation.RasRNPImplementation done: " + e.getMessage()  );
            throw new  RasConnectionFailedException(RasGlobalDefs.URL_FORMAT_ERROR, server);
        }
        Debug.leaveVerbose( "RasRNPImplementation.RasRNPImplementation done." );
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

        this.getRasmgService().openDb(this.controller, openDbReq, new RpcCallback<OpenDbRepl>() {
            @Override
            public void run(OpenDbRepl openDbRepl) {
                if (!controller.failed()) {
                    rasServerHost = openDbRepl.getServerHostName();
                    rasServerPort = openDbRepl.getPort();
                    sessionId = openDbRepl.getDbSessionId();

                    OpenServerDatabaseReq openServerDatabaseReq = OpenServerDatabaseReq.newBuilder()
                            .setClientId(clientID)
                            .setDatabaseName(databaseName)
                            .build();
                    getRasServerService().openServerDatabase(controller, openServerDatabaseReq, DoNothing.<OpenServerDatabaseRepl>get());

                    if (controller.failed()) {
                        throw new RasnetException(controller.errorText());
                    }
                }
            }
        });

        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }

        dbIsOpen = 1;
        Debug.leaveVerbose("RasRNPImplementation.openDB done.");
    }

    @Override
    public void closeDB() throws ODMGException {

        CloseServerDatabaseReq closeServerDatabaseReq = CloseServerDatabaseReq.newBuilder()
                .setClientId(this.clientID)
                .build();

        CloseDbReq closeDbReq = CloseDbReq.newBuilder()
                .setClientId(this.clientID)
                .setClientUUID(this.clientUUID)
                .setDbSessionId(this.sessionId)
                .build();

        this.getRasServerService().closeServerDatabase(controller, closeServerDatabaseReq, DoNothing.<CommonService.Void>get());
        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }

        this.getRasmgService().closeDb(controller, closeDbReq, DoNothing.<CommonService.Void>get());
        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }

        Debug.talkVerbose("RasRNPImplementation.closeDB.");

        dbIsOpen = 0;
        disconnectClient();
    }

    @Override
    public void beginTA() {
        BeginTransactionReq transactionReq = BeginTransactionReq.newBuilder()
                .setClientId(this.clientID)
                .setRw(readWrite)
                .build();

        this.getRasServerService().beginTransaction(controller, transactionReq, DoNothing.<BeginTransactionRepl>get());
        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }
    }

    @Override
    public boolean isOpenTA() {
        IsTransactionOpenReq isTransactionOpenReq = IsTransactionOpenReq.newBuilder()
                .setClientId(clientID)
                .build();

        final boolean[] isOpen = {false};
        this.getRasServerService().isTransactionOpen(controller, isTransactionOpenReq, new RpcCallback<IsTransactionOpenRepl>() {
            @Override
            public void run(IsTransactionOpenRepl isTransactionOpenRepl) {
                isOpen[0] = isTransactionOpenRepl.getIsOpen();
            }
        });
        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }

        return isOpen[0];
    }

    @Override
    public void commitTA() {
        CommitTransactionReq commitTransactionReq = CommitTransactionReq.newBuilder()
                .setClientId(clientID)
                .build();

        this.getRasServerService().commitTransaction(controller, commitTransactionReq, DoNothing.<CommitTransactionRepl>get());
        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }
    }

    @Override
    public void abortTA() {
        AbortTransactionReq abortTransactionReq = AbortTransactionReq.newBuilder()
                .setClientId(clientID)
                .build();
        this.getRasServerService().abortTransaction(controller, abortTransactionReq, DoNothing.<AbortTransactionRepl>get());
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
        connectClient(userName, plainPass);
    }

    private void connectClient(String userName, String plainPass) {
        ConnectReq connectReq = ConnectReq.newBuilder()
                .setUserName(userName)
                .setPasswordHash(plainPass)
                .build();
        this.getRasmgService().connect(controller, connectReq, new RpcCallback<ConnectRepl>() {
            @Override
            public void run(ConnectRepl connectRepl) {
                clientID = connectRepl.getClientId();
                clientUUID = connectRepl.getClientUUID();
            }
        });
        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
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

            final Object[] result = new Object[1];

            this.getRasServerService().executeHttpQuery(controller, executeHttpQueryReq, new RpcCallback<ExecuteHttpQueryRepl>() {
                @Override
                public void run(ExecuteHttpQueryRepl executeHttpQueryRepl) {
                    if (!controller.failed()) {
                        try {
                            result[0] = getResponse(executeHttpQueryRepl.getData().toByteArray());
                        } catch (RasQueryExecutionFailedException e) {
                            throw new RasnetException(e);
                        }
                    }
                }
            });

            if (controller.failed()) {
                throw new RasnetException(controller.errorText());
            }

            return result[0];
        } catch (UnsupportedEncodingException e) {
            Debug.talkCritical("RasRNPImplementation.executeQueryRequest: " + e.getMessage());
            Debug.leaveVerbose("RasRNPImplementation.executeQueryRequest: done, " + e.getMessage());
            throw new RasClientInternalException("RasRNPImplementation", "executeQueryRequest()", e.getMessage());
        }
    }

    @Override
    public String getTypeStructure(String typename, int typetype) {
        GetTypeStructureReq getTypeStructureReq = GetTypeStructureReq.newBuilder()
                .setClientId(clientID)
                .setTypeName(typename)
                .setTypeType(typetype)
                .build();

        final String[] typeStructure = new String[1];

        this.getRasServerService().getTypeStructure(controller, getTypeStructureReq, new RpcCallback<GetTypeStructureRepl>() {
            @Override
            public void run(GetTypeStructureRepl getTypeStructureRepl) {
                typeStructure[0] = getTypeStructureRepl.getTypeStructure();
            }
        });

        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }

        return typeStructure[0];
    }

    private RasMgrClientService.Stub getRasmgService() {
        synchronized (this) {
            if (rasmgService == null) {
                rasmgrServiceChannel = new Channel(this.rasMgrHost, this.rasMgrPort);
                rasmgService = RasMgrClientService.newStub(rasmgrServiceChannel);
            }
        }
        return rasmgService;
    }

    private ClientRassrvrService.Stub getRasServerService() {

        synchronized (this) {
            if (rasServerService == null) {
                rasServiceServiceChannel = new Channel(this.rasServerHost, this.rasServerPort);
                rasServerService = ClientRassrvrService.newStub(rasServiceServiceChannel);
            }
        }
        return rasServerService;
    }

    private void disconnectClient() {
        DisconnectReq disconnectReq = DisconnectReq.newBuilder()
                .setClientUUID(clientUUID)
                .build();
        this.getRasmgService().disconnect(controller, disconnectReq, DoNothing.<CommonService.Void>get());
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
            } catch (IOException e) {
                //TODO-GM: change this
                e.printStackTrace();
            }
        }
    }

    private synchronized void closeRasServerService() {
        if (rasServerService != null) {
            rasServerService = null;
            rasServerHost = null;
            rasServerPort = -1;
            try {
                rasServiceServiceChannel.close();
            } catch (IOException e) {
                //TODO-GM: change this
                e.printStackTrace();
            }
        }
    }

    private RasOID executeGetNewObjectId() {
        GetNewOidReq getNewOidReq = GetNewOidReq.newBuilder()
                .setClientId(clientID)
                .setObjectType(1)
                .build();
        final RasOID[] rasOID = {null};

        this.getRasServerService().getNewOid(controller, getNewOidReq, new RpcCallback<GetNewOidRepl>() {
            @Override
            public void run(GetNewOidRepl getNewOidRepl) {
                rasOID[0] = new RasOID(getNewOidRepl.getOid());
            }
        });

        if (controller.failed()) {
            throw new RasnetException(controller.errorText());
        }

        return rasOID[0];
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

}
