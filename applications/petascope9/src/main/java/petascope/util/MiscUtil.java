/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU  General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.util;

import java.util.ArrayList;
import java.util.List;
import petascope.wcps.server.core.IRasNode;

/**
 * Various utility methods.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class MiscUtil {

    public static <T> List<T> toList(T... e) {
         List<T> ret = new ArrayList<T>();
         if (e != null)
             for (T o : e) {
                ret.add(o);
            }
         return ret;
    }
    
    /**
     * Recursive generic method to extract nodes of a specified class into the XML tree of a WCPS query.
     * @param <T>   The generic type
     * @param node  The root node
     * @param type  Explicit target type argument
     * @return  A list of the children of `node` of class `type`.
     */     
    public static <T extends IRasNode> List<T> childrenOfType(IRasNode root, Class<T> type) {
        List<T> targetChilds = new ArrayList<T>();
        
        // Recursive depth-first search
        if (root.hasChildren()) {
            // pre-order op
            List<IRasNode> children = root.getChildren();
                        
            for (int i=0; i<children.size(); i++) {
                // visit children
                targetChilds.addAll(childrenOfType(children.get(i), type));
                // in-order op
                if (type.isInstance(children.get(i))) {
                    targetChilds.add((T)children.get(i));
                }
            }
        }
        
        return targetChilds;
    }
    
         
    /**
     * Inner class which gathers the required geo-parameters for GTiff/JPEG2000 encoding.
     */
    public class CrsProperties {
        /* Encoding parameters */
        private static final String CRS_PARAM  = "crs";
        private static final String XMAX_PARAM = "xmax";
        private static final String XMIN_PARAM = "xmin";
        private static final String YMAX_PARAM = "ymax";
        private static final String YMIN_PARAM = "ymin";
        private static final char PS = ';'; // parameter separator
        
        /* Members */
        private double lowX;
        private double highX;
        private double lowY;
        private double highY;
        private String crs;
        
        /* Constructors */
        // Unreferenced gml:Grid
        public CrsProperties() {
            lowX  = 0.0D;
            highX = 0.0D;
            lowY  = 0.0D;
            highY = 0.0D;
            crs   = "";
        }
        // Georeferenced gml:RectifiedGrid
        public CrsProperties(double xMin, double xMax, double yMin, double yMax, String crs) {
            lowX  = xMin;
            highX = xMax;
            lowY  = yMin;
            highY = yMax;
            this.crs = crs;
        }
        public CrsProperties(String xMin, String xMax, String yMin, String yMax, String crs) {
            this(Double.parseDouble(xMin), Double.parseDouble(xMax),
                    Double.parseDouble(yMin), Double.parseDouble(yMax), crs);
        }
        
        // Interface
        public double getXmin() {
            return lowX;
        }
        public double getXmax() {
            return highX;
        }
        public double getYmin() {
            return lowY;
        }
        public double getYmax() {
            return highY;
        }
        public String getCrs() {
            return crs;
        }
        
        // Methods
        // Returns the paramters as they are exptected from rasql encode() function
        @Override
        public String toString() {
            return XMIN_PARAM  + "=" + lowX  + PS +
                    XMAX_PARAM + "=" + highX + PS +
                    YMIN_PARAM + "=" + lowY  + PS +
                    YMAX_PARAM + "=" + highY + PS +
                    CRS_PARAM  + "=" + CrsUtil.CrsUri.getAuthority(crs) + ":" + CrsUtil.CrsUri.getCode(crs);
        }
    }
}
