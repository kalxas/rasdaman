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

package org.rasdaman.rasnet.server;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import org.junit.After;
import org.junit.Before;
import org.rasdaman.rasnet.common.Constants;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.message.Communication.MessageType;
import org.rasdaman.rasnet.message.Test;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.rasdaman.rasnet.TestUtilities.randInt;
import static org.rasdaman.rasnet.message.Communication.*;
import static org.rasdaman.rasnet.message.Communication.MessageType.Types.*;

public class ServiceManagerTest{
    private class SearchServiceImpl extends org.rasdaman.rasnet.message.Test.SearchService {

        @Override
        public void search(RpcController controller,
                           Test.TestRequest request,
                           RpcCallback<Test.TestReply> done) {

            if (request.getData().equals("hello")) {
                Test.TestReply reply = Test.TestReply.newBuilder().setData(request.getData()).build();
                done.run(reply);

            } else if (request.getData().equals("fail")) {
                throw new RuntimeException("runtime");
            } else {
                controller.setFailed("controller");
            }
        }
    }

    private ZMQ.Context context;
    private ZMQ.Socket client;
    private String serviceEndpoint;
    private String externalServiceEndpoint;
    private ServiceManagerConfig config;
    private ServiceManager manager;

    @Before
    public void setUp() {
        int port = randInt(8000, 40000);
        this.serviceEndpoint = "tcp://*:"+ port;
        this.externalServiceEndpoint = "tcp://localhost:"+port;

        this.config=new ServiceManagerConfig();
        this.manager = new ServiceManager(this.config);

        this.context= ZMQ.context(1);
        this.client = this.context.socket(ZMQ.DEALER);
        this.client.setLinger(0);
        client.setIdentity((new String("client")).getBytes(Constants.DEFAULT_ENCODING));
        this.client.connect(this.externalServiceEndpoint);
    }

    @After
    public void tearDown() {
        try {
            manager.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.client.disconnect(this.externalServiceEndpoint);
        this.client.close();

        this.context.close();
    }

    @org.junit.Test
    public void constructorTestNoThrow()throws Exception {
        ServiceManagerConfig config = new ServiceManagerConfig();
        ServiceManager manager = new ServiceManager(config);

        manager.close();

        //closing again will have no effect
        manager.close();
    }

    @org.junit.Test
    public void serveTestInvalidAddress()throws Exception {
        //TODO:In the future, make this fail
        manager.serve("address");
    }

    @org.junit.Test
    public void serveTestValidAddress()throws Exception {
        manager.serve(this.serviceEndpoint);
    }

    @org.junit.Test
    public void connectRequestTest() throws Exception{
        ConnectRequest request = ConnectRequest.newBuilder()
                .setLifetime(100)
                .setRetries(2)
                .build();
        manager.serve(this.serviceEndpoint);

        ZmqUtil.sendCompositeMessage(client, CONNECT_REQUEST, request);

        ArrayList<byte[]> message = new ArrayList<>();
        ZmqUtil.receiveCompositeMessage(client, message);

        assertEquals(2, message.size());
        MessageType type = MessageType.parseFrom(message.get(0));
        assertEquals(CONNECT_REPLY, type.getType());

        ConnectReply reply = ConnectReply.parseFrom(message.get(1));
        assertEquals(config.getAliveTimeout(), reply.getLifetime());
        assertEquals(config.getAliveRetryNo(), reply.getRetries());
    }

    @org.junit.Test
    public void pingTest() throws Exception{
        manager.serve(this.serviceEndpoint);

        ZmqUtil.sendCompositeMessage(client, ALIVE_PING);

        ArrayList<byte[]> message = new ArrayList<>();
        ZmqUtil.receiveCompositeMessage(client, message);

        assertEquals(1, message.size());
        MessageType type = MessageType.parseFrom(message.get(0));
        assertEquals(ALIVE_PONG, type.getType());
    }

    @org.junit.Test
    public void requestHandlerTest() throws Exception{
        Service service =  new SearchServiceImpl();
        String methodName = ZmqUtil.getMethodName(service.getDescriptorForType().getMethods().get(0));
        String callId = "callId";

        Test.TestRequest request = Test.TestRequest.newBuilder().setData("hello").build();

        manager.addService(service);
        manager.serve(serviceEndpoint);

        ZmqUtil.sendServiceRequest(client, callId, methodName, request);
        ArrayList<byte[]> message = new ArrayList<>();
        ZmqUtil.receiveCompositeMessage(client, message);


        assertEquals(4, message.size());
        MessageType type = MessageType.parseFrom(message.get(0));
        assertEquals(SERVICE_RESPONSE, type.getType());

        String recvCallId = new String(message.get(1));
        assertEquals(callId, recvCallId);

        ServiceCallStatus status = ServiceCallStatus.parseFrom(message.get(2));
        assertEquals(true, status.getSuccess());

        Test.TestReply reply = Test.TestReply.parseFrom(message.get(3));
        assertEquals(request.getData(), reply.getData());
    }
}