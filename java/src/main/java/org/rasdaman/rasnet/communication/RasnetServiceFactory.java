package org.rasdaman.rasnet.communication;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import org.rasdaman.rasnet.service.ClientRassrvrServiceGrpc;
import org.rasdaman.rasnet.service.HealthServiceGrpc;
import org.rasdaman.rasnet.service.RasmgrClientServiceGrpc;

/**
 * Factory for creating channels and services for ranset.
 */
public interface RasnetServiceFactory {

    /**
     * Create a communication service between client and rasmgr.
     * @param channel Channel holding the connection between the client and rasmgr
     * @return A grpc service.
     */
    RasmgrClientServiceGrpc.RasmgrClientServiceBlockingStub createRasmgrClientService(Channel channel);

    /**
     * Create a communication service between client and rasserver.
     * @param channel Channel holing the connection between the client and rasserver.
     * @return A grpc service.
     */
    ClientRassrvrServiceGrpc.ClientRassrvrServiceBlockingStub createClientRasServerService(Channel channel);

    /**
     * Create a health service uses to check the liveliness between a server (rasmgr or rasserver) and the client.
     * @param channel Channel holing the connection between the client and rasserver.
     * @return A grpc service.
     */
    HealthServiceGrpc.HealthServiceBlockingStub createHealthService(Channel channel);

    /**
     * Create a channel for connecting to a server.
     * @param host The server host.
     * @param port The server port.
     * @return A channel.
     */
    ManagedChannel createChannel(String host, int port);
}
