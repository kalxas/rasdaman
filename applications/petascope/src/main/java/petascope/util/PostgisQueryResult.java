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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package petascope.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import static petascope.core.DbMetadataSource.MULTIPOINT_COORDINATE;
import static petascope.core.DbMetadataSource.MULTIPOINT_VALUE;
/**
 *
 * @author alireza
 */
public class PostgisQueryResult {

    private final List<String> coordinates;
    private final List<String> values;


    public PostgisQueryResult(Object result) throws SQLException {
        coordinates = new ArrayList<String>();
        values = new ArrayList<String>();
        if (result instanceof ResultSet) {
            String coord = "";
            while (((ResultSet) result).next()) {
                    coordinates.add(((ResultSet)result).getString(MULTIPOINT_COORDINATE));
                    values.add(((ResultSet)result).getString(MULTIPOINT_VALUE));
            }
        }

    }

    public List<String> getCoordinates() {
        return coordinates;
    }

    public List<String> getValues() {
        return values;
    }

    public String toCSV(List<String> data){
        StringBuilder csv = new StringBuilder();
        String delim = "";
        for(String d: data){
            csv.append(delim).append(d);
            delim = ",";
        }
        return csv.toString();
    }
}
