package org.rasdaman.rasnet.communication;

import org.rasdaman.rasnet.service.ClientRasServerService;
import org.rasdaman.rasnet.service.ClientRassrvrServiceGrpc;
import org.rasdaman.rasnet.service.RasmgrClientService;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by rasdaman on 10/13/15.
 */
public class RasserverKeepAliveTest {

    private ClientRassrvrServiceGrpc.ClientRassrvrServiceBlockingStub rasserverService;
    private String clientUUID;
    private String sessionId;
    private RasserverKeepAlive keepAliveService;
    private int heartBeatInterval = 10;

    @org.junit.Before
    public void setUp() throws Exception {
        this.rasserverService = mock(ClientRassrvrServiceGrpc.ClientRassrvrServiceBlockingStub.class);
        this.clientUUID = "clientUUID";
        this.sessionId = "sessionId";
        this.keepAliveService = new RasserverKeepAlive(this.rasserverService, this.clientUUID,this.sessionId,heartBeatInterval);
    }

    @org.junit.Test
    public void testStart() throws Exception {
        this.keepAliveService.start();
        //The process must have started
        assertTrue(this.keepAliveService.isRunning());

        //After waiting for a certain amount of time, the service must have been called once
        Thread.sleep(heartBeatInterval);
        verify(this.rasserverService).keepAlive(any(ClientRasServerService.KeepAliveRequest.class));

        //Stop the service
        this.keepAliveService.stop();
    }

    @org.junit.Test
    public void testIsRunning() throws Exception {
        this.keepAliveService.start();
        //The process must have started
        assertTrue(this.keepAliveService.isRunning());

        //Stop the service
        this.keepAliveService.stop();
        assertFalse(this.keepAliveService.isRunning());
    }
}