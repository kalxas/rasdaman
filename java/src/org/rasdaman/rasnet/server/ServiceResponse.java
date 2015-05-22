/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package org.rasdaman.rasnet.server;

import com.google.protobuf.ByteString;

/**
 * Used for holding the result of a service call before it is forwarded to the peer.
 */
public class ServiceResponse {
    /**
     * Serialized Google Protobuf message that must be sent to the peer
     * that made the service call
     * This member is null, if success == false
     */
    private ByteString outputValue;

    /**
     * String representing an error message that must be sent to the peer
     * that made the service call.
     */
    private String error;

    /**
     * Unique ID of the call
     */
    private String callId;

    /**
     * True if the service call was successful which means that outputValue contains the result message
     * False if the service call failed which means that error contains the error message that must be forwarded to the peer
     */
    private boolean success;

    public ServiceResponse(String callId) {
        this.callId = callId;
        this.success = true;
    }

    public void setOutputValue(ByteString outputValue) {
        this.outputValue = outputValue;
    }

    public void setError(String error) {
        this.error = error;
        this.success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCallId() {
        return callId;
    }

    public ByteString getOutputValue() {
        return outputValue;
    }

    public String getError() {
        return error;
    }
}
