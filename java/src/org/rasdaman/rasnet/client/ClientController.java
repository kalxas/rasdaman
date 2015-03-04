package org.rasdaman.rasnet.client;

import com.google.protobuf.RpcCallback;

public class ClientController implements com.google.protobuf.RpcController {
    private String errorText;
    private boolean failed;

    public ClientController() {
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
        return this.errorText;
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
