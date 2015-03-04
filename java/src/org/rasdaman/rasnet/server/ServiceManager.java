package org.rasdaman.rasnet.server;

import com.google.protobuf.Service;
import org.rasdaman.rasnet.exception.AlreadyRunningException;
import org.rasdaman.rasnet.exception.DuplicateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServiceManager implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceManager.class);

    private final ExecutorService executor;
    private final ServiceManagerCommunication communication;
    private boolean running;

    public ServiceManager(ServiceManagerCommunication communication) {
        this.executor = Executors.newSingleThreadExecutor();
        this.communication = communication;
        this.running = false;
    }

    public void addService(Service service) throws AlreadyRunningException, DuplicateService {
        if (this.running) {
            throw new AlreadyRunningException();
        } else {
            this.communication.addService(service);
        }
    }

    public void serve(String address) throws AlreadyRunningException {
        if (this.running) {
            throw new AlreadyRunningException();
        } else {
            this.running = true;
            this.communication.setHost(address);
            executor.execute(this.communication);
        }
    }

    public void close() throws InterruptedException {
        if (this.running) {
            this.running = false;

            this.communication.close();

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (this.running) {
            LOG.error("Programming error. The ServerManager must be closed before being garbage collected.");
            this.close();
        }
    }
}
