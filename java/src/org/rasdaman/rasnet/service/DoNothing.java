package org.rasdaman.rasnet.service;

import com.google.protobuf.RpcCallback;

public class DoNothing<T> implements RpcCallback<T> {

    private DoNothing() {

    }

    @Override
    public void run(T t) {

    }

    public static <T> DoNothing<T> get() {
        return new DoNothing<T>();
    }

}