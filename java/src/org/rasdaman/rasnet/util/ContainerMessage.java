package org.rasdaman.rasnet.util;

import com.google.protobuf.ByteString;

/**
 * Created by rasdaman on 2/22/15.
 */
public class ContainerMessage {
    private ByteString data;
    private String type;

    public String getType() {
        return type;
    }

    public ByteString getData() {
        return data;
    }

    public void setData(ByteString data) {
        this.data = data;
    }

    public void setType(String type) {
        this.type = type;
    }
}
