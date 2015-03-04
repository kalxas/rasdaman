package org.rasdaman.rasnet.server;

import com.google.protobuf.RpcCallback;

public class ServerController implements com.google.protobuf.RpcController {
    private String errorText;
    private boolean failed;

    public ServerController() {
        this.reset();
    }

    @Override
    public void reset() {
        this.errorText = "";
        this.failed = false;
    }

    @Override
    public boolean failed() {
        return this.failed;
    }

    @Override
    public String errorText() {
        return errorText;
    }

    @Override
    public void startCancel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFailed(String reason) {
        this.failed = true;
        this.errorText = reason;
    }

    @Override
    public boolean isCanceled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notifyOnCancel(RpcCallback<Object> callback) {
        throw new UnsupportedOperationException();
    }
}
