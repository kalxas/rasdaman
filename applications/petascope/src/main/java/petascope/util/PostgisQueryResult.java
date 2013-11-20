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
