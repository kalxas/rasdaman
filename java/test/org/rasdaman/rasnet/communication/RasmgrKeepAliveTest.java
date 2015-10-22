package org.rasdaman.rasnet.communication;

import org.rasdaman.rasnet.service.RasMgrClientServiceGrpc;
import org.rasdaman.rasnet.service.RasmgrClientService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RasmgrKeepAliveTest {
    private RasMgrClientServiceGrpc.RasMgrClientServiceBlockingStub rasmgrService;
    private String clientUUID;
    private RasmgrKeepAlive keepAliveService;
    private int heartBeatInterval = 10;

    @org.junit.Before
    public void setUp() throws Exception {
        this.rasmgrService = mock(RasMgrClientServiceGrpc.RasMgrClientServiceBlockingStub.class);
        this.clientUUID = "clientUUID";
        this.keepAliveService = new RasmgrKeepAlive(this.rasmgrService, this.clientUUID, heartBeatInterval);
    }

    @org.junit.Test
    public void testStart() throws Exception {
        this.keepAliveService.start();
        //The process must have started
        assertTrue(this.keepAliveService.isRunning());

        //After waiting for a certain amount of time, the service must have been called once
        Thread.sleep(heartBeatInterval);
        verify(this.rasmgrService).keepAlive(any(RasmgrClientService.KeepAliveReq.class));

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