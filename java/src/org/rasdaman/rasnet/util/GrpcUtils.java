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

package org.rasdaman.rasnet.util;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Status;
import org.odmg.ODMGRuntimeException;
import org.rasdaman.rasnet.service.Error;

import java.nio.charset.Charset;

public class GrpcUtils {
    private static RuntimeException convertStatusToGenericException(Status status) {
        StringBuilder builder = new StringBuilder();
        builder.append("GRPC Exception:\n");
        builder.append("Status code:");
        builder.append(status.getCode());
        builder.append("\n");
        builder.append("Status description:");
        builder.append(status.getDescription());
        builder.append("\n");

        return new RuntimeException(builder.toString());
    }

    public static RuntimeException convertStatusToRuntimeException(Status status) {
        RuntimeException result;

        if (status.getCode() == Status.Code.UNKNOWN) {
            try {
                Error.ErrorMessage message = Error.ErrorMessage.parseFrom(status.getDescription().getBytes(Charset.forName("UTF-8")));
                switch (message.getType()) {
                    case STL: {
                        result = new RuntimeException(message.getErrorText());
                    }
                    break;
                    case RERROR: {
                        result = new ODMGRuntimeException(message.getErrorText());
                    }
                    break;
                    case UNKNOWN: {
                        result = new RuntimeException(message.getErrorText());
                    }
                    break;
                    default: {
                        result = new RuntimeException(message.getErrorText());
                    }
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
                result = GrpcUtils.convertStatusToGenericException(status);
            }
        } else {
            result = GrpcUtils.convertStatusToGenericException(status);
        }

        if (result != null) {
            return result;
        } else {
            return new RuntimeException("Invalid exception message received.");
        }
    }
}
