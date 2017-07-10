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
package petascope.wcst.helpers.grib.model;

import java.util.List;

/**
 * Representation of a grib message, used by jackson to map the information from json.
 * A Grib message in wcst_import GML example (coverage with 4 axes):
 * 
 * {"axes": [{"name": "isobaric", "min": "887.5", "max": "912.5", "resolution": "25", "type": "number", "order": "0"}, 
 *           {"type": "date", "order": "1", "resolution": "1", "name": "ansi", "min": "2006-11-28T06:00:00"},
 *           {"name": "Lat", "min": "-90.25", "max": "90.25", "resolution": "-0.5", "type": "number", "order": "3"},
 *           {"name": "Long", "min": "-0.25", "max": "359.75", "resolution": "0.5", "type": "number", "order": "2"}], 
 * "messageId": 5}
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class GribMessage {
    private int messageId;
    private List<GribAxis> axes;

    public GribMessage() {}

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public List<GribAxis> getAxes() {
        return axes;
    }

    public void setAxes(List<GribAxis> axes) {
        this.axes = axes;
    }
}
