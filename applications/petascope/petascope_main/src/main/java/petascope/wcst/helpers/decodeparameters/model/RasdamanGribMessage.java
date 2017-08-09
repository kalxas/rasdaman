/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcst.helpers.decodeparameters.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import petascope.wcps.metadata.model.ParsedSubset;

import java.util.Map;

/**
 * Representation of a message, understandable by rasdaman.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class RasdamanGribMessage {
    public int msgId;

    //dim => subset corresponding to dim
    @JsonIgnore
    private Map<Integer, ParsedSubset<Long>> pixelIndices;

    private String domain;

    public RasdamanGribMessage(int msgId, Map<Integer, ParsedSubset<Long>> pixelIndices) {
        this.msgId = msgId;
        this.pixelIndices = pixelIndices;
    }

    public RasdamanGribMessage() {}

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getDomain() {
        return getAffectedDomain(pixelIndices);
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Map<Integer, ParsedSubset<Long>> getPixelIndices() {
        return pixelIndices;
    }

    public void setPixelIndices(Map<Integer, ParsedSubset<Long>> pixelIndices) {
        this.pixelIndices = pixelIndices;
    }

    private String getAffectedDomain(Map<Integer, ParsedSubset<Long>> pixelIndices) {
        String domain = "[";
        for (int i = 0; i < pixelIndices.size(); i++) {
            domain += String.valueOf(pixelIndices.get(i).getLowerLimit()) + ":" + String.valueOf(pixelIndices.get(i).getUpperLimit());
            //if not last, add a comma
            if (i < pixelIndices.keySet().size() - 1) {
                domain += ",";
            }
        }
        domain += "]";
        return domain;
    }
}
