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

import com.google.protobuf.*;
import org.rasdaman.rasnet.common.Constants;
import org.rasdaman.rasnet.exception.DuplicateService;
import org.rasdaman.rasnet.exception.UnsupportedMessageType;
import org.rasdaman.rasnet.message.Communication;
import org.rasdaman.rasnet.message.internal.Internal.ServiceResponseAvailable;
import org.rasdaman.rasnet.util.ZmqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServiceRequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRequestHandler.class);

    private ZMQ.Context context;
    private String bridgeAddress;
    private ConcurrentHashMap<String, Service> serviceMap;
    private ConcurrentHashMap<String, Descriptors.MethodDescriptor> serviceMethodMap;
    private ConcurrentLinkedQueue<ServiceResponse> completedTasksResults;

    public ServiceRequestHandler(ZMQ.Context context, String bridgeAddress) {
        this.context = context;
        this.bridgeAddress = bridgeAddress;
        this.serviceMap = new ConcurrentHashMap<String, Service>();
        this.serviceMethodMap = new ConcurrentHashMap<String, Descriptors.MethodDescriptor>();
        this.completedTasksResults = new ConcurrentLinkedQueue<ServiceResponse>();
    }

    public void addService(Service service) throws DuplicateService {
        if (service == null) {
            throw new IllegalArgumentException("service");
        }

        String serviceName = service.getDescriptorForType().getFullName();
        Service oldService = this.serviceMap.put(serviceName, service);

        if (oldService != null) {
            throw new DuplicateService();
        } else {
            List<Descriptors.MethodDescriptor> methods = service.getDescriptorForType().getMethods();
            Iterator<Descriptors.MethodDescriptor> it = methods.iterator();

            while (it.hasNext()) {
                Descriptors.MethodDescriptor method = it.next();
                String methodName = method.getFullName();
                serviceMethodMap.put(methodName, method);
            }
        }
    }

    ServiceResponse getTaskResult() {
        return this.completedTasksResults.poll();
    }

    public boolean canHandle(ArrayList<byte[]> message) {
        boolean success = false;

        if (message.size() == 4) {
            Communication.MessageType type;
            try {
                type = Communication.MessageType.parseFrom(message.get(0));
                success = (type.getType() == Communication.MessageType.Types.SERVICE_REQUEST);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
                success = false;
            }
        }

        return success;
    }

    public void handle(ArrayList<byte[]> message, String peerId) throws UnsupportedMessageType {
        //boolean which indicates if the callback was called.
        //If the callback was not called, an error is reported to the user.
        final boolean[] callbackCalled = {false};
        if (this.canHandle(message)) {
            //| MessageType = SERVICE_REQUEST | Call ID | Method Name | Serialized Input Data|
            String callId = new String(message.get(1));
            String methodName = new String(message.get(2));
            final ServiceResponse serviceResponse = new ServiceResponse(callId);

            final ServerController controller = new ServerController();
            Descriptors.MethodDescriptor methodDescriptor = this.serviceMethodMap.get(methodName);
            if (methodDescriptor != null) {
                //Retrieve the service associated with the method descriptor
                Service service = this.serviceMap.get(methodDescriptor.getService().getFullName());
                if (service != null) {
                    try {
                        final String replyDataType = ZmqUtil.getType(service.getResponsePrototype(methodDescriptor).newBuilderForType().buildPartial());

                        Message requestData = service.getRequestPrototype(methodDescriptor).getParserForType().parseFrom(message.get(3));

                        //Call the method and set the output
                        service.callMethod(methodDescriptor, controller, requestData, new RpcCallback<Message>() {
                            @Override
                            public void run(Message parameter) {
                                callbackCalled[0] = true;

                                //Sanity check
                                if (replyDataType.equals(ZmqUtil.getType(parameter))) {
                                    serviceResponse.setOutputValue(parameter.toByteString());
                                } else {
                                    //Invalid message received.
                                    controller.setFailed("The response set in the callback is invalid. Contact the service implementer.");
                                }
                            }
                        });

                        if (!callbackCalled[0] && !controller.failed()) {
                            controller.setFailed("Invalid method implementation. Contact the service implementer.");
                        }

                    } catch (InvalidProtocolBufferException e) {
                        LOG.error(e.getLocalizedMessage());
                        controller.setFailed("The input data is unparsable.");
                    } catch (Exception ex) {
                        LOG.error(ex.getLocalizedMessage());
                        controller.setFailed(ex.getLocalizedMessage());
                    }
                } else {
                    String errorMessage = "There is no service with the given name offered by this server.";
                    LOG.error(errorMessage);
                    controller.setFailed(errorMessage);
                }
            } else {
                String errorMessage = "There is no method with the given name on this server.";
                LOG.error(errorMessage);
                controller.setFailed(errorMessage);
            }

            if (controller.failed()) {
                serviceResponse.setError(controller.errorText());
            }

            this.completedTasksResults.add(serviceResponse);

            this.sendMessageToBridge(peerId);

        } else {
            throw new UnsupportedMessageType();
        }
    }

    private void sendMessageToBridge(String peerId) {
        try {
            ZMQ.Socket socket = this.context.socket(ZMQ.DEALER);
            socket.setLinger(0);
            socket.setIdentity(peerId.getBytes(Constants.DEFAULT_ENCODING));
            socket.connect(this.bridgeAddress);

            ServiceResponseAvailable responseAvailable = ServiceResponseAvailable.getDefaultInstance();
            ZmqUtil.send(socket, responseAvailable);

            socket.disconnect(this.bridgeAddress);
            socket.close();
        } catch (org.zeromq.ZMQException ex) {
            LOG.error(ex.getMessage());
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        }
    }
}
