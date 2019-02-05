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
package petascope.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;

/**
 * Class to host the info of a (GML) Coordinate Reference System (CRS) definition
 * that will be used by Petascope to handle incoming HTTP requests.
 * In particular, the semantic and order of the axes and the metadata of the CRS are stored.
 *
 * NOTE: this class represents an /atomic/ CRS, whereas Compound-CRSs (CCRS) are
 * contemplated by the composition of a CoverageMetadata with N CrsDefinition objects.
 *
 * @author <a href="mailto:p.campalani@jacobs-university.de">Piero Campalani</a>
 */
public class CrsDefinition {

    // List of case-sensitive EPSG aliases of axis abbreviation of the same type (grouped in petascope.util.AxisTypes interface)
    // src: http://www.epsg-registry.org/
    public static final List<String> X_ALIASES = Arrays.asList(
                "x",    // eg CS EPSG:6507 \
                "e",    // eg CS EPSG:4400  |
                "m",    // eg CS EPSG:1024  |- CARTESIAN CS
                "e(x)", // eg CS EPSG:4496  |
                "long", // eg CS EPSG:6422 ELLISPOIDAL CS
                "lon",  // eg PS:1 (gdalsrsinfo)
                "i"     // eg ImageCRS
            );
    public static final List<String> Y_ALIASES = Arrays.asList(
                "y",    // eg CS EPSG:6507 \
                "n",    // eg CS EPSG:4400  |
                "p",    // eg CS EPSG:1024  |- CARTESIAN CS
                "e(y)", // eg CS EPSG:4496  |
                "lat",  // eg CS EPSG:6422 ELLISPOIDAL CS
                "j"     // eg ImageCRS
            );
    
    public static final String LONGITUDE_AXIS_LABEL_EPGS_VERSION_85 = "Long";
    public static final String LONGITUDE_AXIS_LABEL_EPGS_VERSION_0 = "Lon";
    
    // eg CS EPSG:6423 ELLIPSOIDAL-3D / ImageCRS, CS EPSG:1030
    public static final List<String> ELEVATION_UP_ALIASES = Arrays.asList("h");
    // eg CS EPSG:6495 |-VERTICAL
    public static final List<String> ELEVATION_DOWN_ALIASES = Arrays.asList("d");

    /* Constants */
    // GML values
    public static final String POSITIVE_AXIS_DIRECTION = "positive";
    public static final String NEGATIVE_AXIS_DIRECTION = "negative";
    public static final String DEFAULT_AXIS_DIRECTION  = POSITIVE_AXIS_DIRECTION;

    // Members
    private static final Logger log = LoggerFactory.getLogger(CrsDefinition.class);
    private final String     authority;
    private final String     version;
    private final String     code;
    private final String     type;        // Geodetic, Geographic, Vertical, Temporal, etc.
    private final List<Axis> axes;
    private String     datumOrigin; // for TemporalCRSs

    // Constructor (each axis is defined later on by the GML parser)
    public CrsDefinition(String auth, String vers, String code, String type) {
        authority = auth;
        version   = vers;
        this.code = code;
        this.type = type;
        axes      = new ArrayList<Axis>();
    }

    public void addAxis(String dir, String abbr, String uom) {
        axes.add(new Axis(dir, abbr, uom));
    }

    // Origin of the datum (currently used for TemporalCRSs only)
    public void setDatumOrigin(String origin) {
        datumOrigin = origin;
    }

    // Access (getters only, once a CrsDefinition is created it should not change)
    public String getAuthority() {
        return authority;
    }
    public String getVersion() {
        return version;
    }
    public String getCode() {
        return code;
    }
    public String getType() {
        return type;
    }
    public List<Axis> getAxes() {
        return axes;
    }
    public List<String> getAxesLabels() {
        List<String> abbrevs = new ArrayList<String>();
        for (Axis axis : axes) {
            abbrevs.add(axis.getAbbreviation());
        }
        return abbrevs;
    }
    public int getDimensions() {
        return axes.size();
    }
    public String getDatumOrigin() {
        return datumOrigin;
    }

    // Methods
    @Override
    public String toString() {
        return (version.equals(CrsUtil.CRS_DEFAULT_VERSION))
               ? authority + ":" + code
               : authority + ":" + code + "(" + version + ")";
    }

    /* NOTE: "In the EPSG Dataset codes are assigned to CRSs, coordinate transformations,
     * and their component entities (datums, projections, etc.).
     * Within each domain type, every record has a unique code".
     */
    public boolean equals(CrsDefinition crs) {
        boolean out = (crs.getAuthority().equals(authority) &&
                       crs.getVersion().equals(version)            &&
                       crs.getCode().equals(code));

        // Consistency check
        if (out && !crs.getType().equals(type)) {
            log.warn("(!) Found two CRS definitions of " + this + " of different types:"
                     + type + " != " + crs.getType());
        }
        return out;
    }

    /**
     * Ultility function to get axis type (x or y)
     * @param axisName
     * @return
     */
    public static String getAxisTypeByName(String axisName) {
        // e.g: Lat -> lat
        axisName = axisName.toLowerCase();
        
        for (String str : X_ALIASES) {
            if (str.equals(axisName)) {
                return AxisTypes.X_AXIS;
            }
        }

        for (String str : Y_ALIASES) {
            if (str.equals(axisName)) {
                return AxisTypes.Y_AXIS;
            }
        }
        return AxisTypes.UNKNOWN;
    }

    // Inner class
    public class Axis implements Cloneable {
        private final String direction;
        private final String abbreviation;
        private final String uom;
        private final String type;

        // Constructor
        // (NOTE) Only the outer class can call it: an Axis must be always put inside a CRS definition.
        private Axis(String dir, String abbr, String uom) {
            direction    = dir;
            abbreviation = abbr;
            this.uom     = uom;

            // Projected/Geographic CRS can have multiple axes
            type = getAxisTypeByLabel(this.getCrsDefinition(), abbr);
        }

        // Access
        public String getDirection() {
            return direction;
        }
        public String getAbbreviation() {
            return abbreviation;
        }
        public String getUoM() {
            return uom;
        }
        // Important: this is the /unique/, the category of the axis, abbreviations can have aliases (X,lon,easting, etc.)
        public String getType() {
            return type;
        }
        public CrsDefinition getCrsDefinition() {
            return CrsDefinition.this;
        }

        @Override
        public CrsDefinition.Axis clone() {
            return new CrsDefinition.Axis(direction, abbreviation, uom);
        }
        
        /**
        * Discover which is the type of the specified (CRS) axis.
        *
        * @param crs An ordered list of single CRS URIs
        * @param axisName The order of the axis (//CoordinateSystemAxis) in the
        * (C)CRS [0 is first]
        * @return The type of the specified axis
        */
       private String getAxisTypeByLabel(CrsDefinition crs, String axisName) {

           String type;
           // e.g: Lat -> lat
           axisName = axisName.toLowerCase();

           // init
           if (X_ALIASES.contains(axisName)) {
               type = AxisTypes.X_AXIS;
           } else if (Y_ALIASES.contains(axisName)) {
               type = AxisTypes.Y_AXIS;
           } else if (ELEVATION_UP_ALIASES.contains(axisName)) {
               type = AxisTypes.HEIGHT_AXIS;
           } else if (ELEVATION_DOWN_ALIASES.contains(axisName)) {
               type = AxisTypes.DEPTH_AXIS;
           } else if (crs.getType().equals(XMLSymbols.LABEL_TEMPORALCRS)) {
                // A TemporalCRS has just one axis:
               type = AxisTypes.T_AXIS;
           } else {
               type = AxisTypes.OTHER;
           }

           return type;
       }
    }
}
