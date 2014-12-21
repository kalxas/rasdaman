package org.rasdaman.rasnet.channel;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

public class ClientController implements RpcController {

    private boolean failed;
    private String failureReason;

    public ClientController() {
        reset();
    }

    @Override
    public void reset() {
        this.failed = false;
        this.failureReason = "";
    }

    @Override
    public boolean failed() {
        return this.failed;
    }

    @Override
    public String errorText() {
        return this.failureReason;
    }

    @Override
    public void startCancel() {

    }

    @Override
    public void setFailed(String s) {
        this.failed = true;
        this.failureReason = s;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void notifyOnCancel(RpcCallback<Object> rpcCallback) {

    }
}
