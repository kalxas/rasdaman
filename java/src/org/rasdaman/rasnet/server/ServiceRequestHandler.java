package org.rasdaman.rasnet.server;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.Service;
import org.rasdaman.rasnet.exception.DuplicateService;
import org.rasdaman.rasnet.message.Internal;
import org.rasdaman.rasnet.util.ContainerMessage;
import org.rasdaman.rasnet.util.ProtoZmq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.rasdaman.rasnet.message.Service.ServiceRequest;
import static org.rasdaman.rasnet.message.Service.ServiceResponse;

public class ServiceRequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceRequestHandler.class);

    private String acceptedMessageType;
    private String bridgeAddress;
    private ZMQ.Context context;
    private Map<String, Service> serviceMap;
    private Map<String, Descriptors.MethodDescriptor> serviceMethodMap;
    private ConcurrentLinkedQueue<ServiceResponse> completedTasksResults;

    public ServiceRequestHandler(ZMQ.Context context, String bridgeAddress) {
        this.context = context;
        this.bridgeAddress = bridgeAddress;
        this.acceptedMessageType = ProtoZmq.getType(ServiceRequest.getDefaultInstance());
        this.serviceMap = Collections.synchronizedMap(new HashMap<String, Service>());
        this.serviceMethodMap = Collections.synchronizedMap(new HashMap<String, Descriptors.MethodDescriptor>());
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

    /**
     * Get the next service response in the queue.
     *
     * @return The next ServiceResponse object or null if there is none
     */
    ServiceResponse getTaskResult() {
        return this.completedTasksResults.poll();
    }

    public boolean canHandle(ContainerMessage message) {
        return (this.acceptedMessageType.equals(message.getType()));
    }

    public void handle(ContainerMessage message, String peerId) {
        //The response that must be sent to the client.
        final ServiceResponse.Builder serviceResponse = ServiceResponse.newBuilder();

        //boolean which indicates if the callback was called.
        //If the callback was not called, an error is reported to the user.
        final boolean[] callbackCalled = {false};

        try {
            if (this.canHandle(message)) {
                //Will fail if the request is invalid
                ServiceRequest request = ServiceRequest.parseFrom(message.getData());

                //The id for the response must be the same as the one in the request.
                serviceResponse.setId(request.getId());

                Descriptors.MethodDescriptor methodDescriptor = this.serviceMethodMap.get(request.getMethodName());
                if (methodDescriptor != null) {
                    //Retrieve the service associated with the method descriptor
                    Service service = this.serviceMap.get(methodDescriptor.getService().getFullName());

                    if (service != null) {
                        ServerController controller = new ServerController();
                        final String replyDataType = ProtoZmq.getType(service.getResponsePrototype(methodDescriptor).newBuilderForType().buildPartial());

                        Message.Builder requestDataBuilder = service.getRequestPrototype(methodDescriptor).newBuilderForType();
                        Message requestData = requestDataBuilder.mergeFrom(request.getInputValue()).build();

                        service.callMethod(methodDescriptor, controller, requestData, new RpcCallback<Message>() {
                            @Override
                            public void run(Message parameter) {
                                callbackCalled[0] = true;

                                //Sanity check
                                if (replyDataType.equals(ProtoZmq.getType(parameter))) {
                                    serviceResponse.setOutputValue(parameter.toByteString());
                                } else {
                                    //Invalid message received.
                                    serviceResponse.setError("The response set in the callback is invalid. Contact the service implementer.");
                                }
                            }
                        });

                        if (!callbackCalled[0]) {
                            serviceResponse.setError("Invalid method implementation. Contact the service implementer.");
                        }

                    } else {
                        serviceResponse.setError(
                                "This service is not offered by the server.");
                        LOG.error("Call to inexisting service:" + methodDescriptor.getService().getFullName());
                    }
                } else {
                    serviceResponse.setError("This method " + request.getMethodName() + " is not offered by the server.");
                    LOG.error("Call to inexisting method:" + request.getMethodName());
                }
            } else {
                LOG.error("Invalid call to ServiceRequestHandler.");
            }
        } catch (Exception ex) {
            serviceResponse.setError(ex.getMessage());
        } finally {
            //If all the fields have been set, return the error.
            if (serviceResponse.isInitialized()) {
                this.completedTasksResults.add(serviceResponse.build());
                this.notifyPeer(peerId);
            }
        }
    }

    private void notifyPeer(String peerId) {
        ZMQ.Socket socket = this.context.socket(ZMQ.DEALER);
        socket.setLinger(0);
        socket.connect(this.bridgeAddress);

        try {
            Internal.ResponseAvailable response = Internal.ResponseAvailable.getDefaultInstance();
            //The socket will get the random identity of the socket
            //the peer id, and finally, the serialized response
            ProtoZmq.sendToPeer(socket, peerId, response);
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        } finally {
            socket.disconnect(this.bridgeAddress);
            socket.close();
        }
    }
}
