package org.rasdaman.rasnet.communication;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import org.rasdaman.rasnet.service.ClientRassrvrServiceGrpc;
import org.rasdaman.rasnet.service.HealthServiceGrpc;
import org.rasdaman.rasnet.service.RasmgrClientServiceGrpc;

/**
 * Implementation for creating rasnet services and channels.
 */
public class RasnetServiceFactoryImpl implements RasnetServiceFactory {

    @Override
    public RasmgrClientServiceGrpc.RasmgrClientServiceBlockingStub createRasmgrClientService(Channel channel) {
        return RasmgrClientServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public ClientRassrvrServiceGrpc.ClientRassrvrServiceBlockingStub createClientRasServerService(Channel channel) {
        return ClientRassrvrServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public HealthServiceGrpc.HealthServiceBlockingStub createHealthService(Channel channel) {
        return HealthServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public ManagedChannel createChannel(String host, int port) {
        return NettyChannelBuilder.forAddress(host, port).usePlaintext(true).maxMessageSize(Integer.MAX_VALUE).build();
    }
}
