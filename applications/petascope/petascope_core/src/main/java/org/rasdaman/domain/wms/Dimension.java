/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.wms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import nu.xom.Attribute;
import nu.xom.Element;
import static org.rasdaman.domain.wms.Layer.TABLE_PREFIX;
import petascope.core.XMLSymbols;
import static petascope.core.XMLSymbols.NAMESPACE_WMS;

/**
 * The optional <Dimension> element is used in service metadata to declare that
 * one or more dimensional parameters are relevant to a layer or group of
 * layers.
 *
 * Dimension elements shall comply with the following rules: - Dimension names
 * shall be interpreted case-insensitively and should contain no whitespace. -
 * If the dimensional quantity has no units (e.g. band number in a
 * multi-wavelength sensor), use the null string: units="".
 *
 * If a data object has an Elevation dimension defined, then operation requests
 * to retrieve that object may include the parameter ELEVATION=value. If a Layer
 * has a Time dimension defined, then requests may include the parameter
 * TIME=value e.g: Irregular axis (list of values in extent):
 * <Dimension name="temperature" units="Kelvin" unitSymbol="K" default="300">230,300,400</Dimension>
 * Regular axis (min, max and resolution in extent):
 * <Dimension name="elevation" units="CRS:88" unitSymbol="m" default="0">0/10000/100</Dimension>
 *
 * NOTE: only use when a layer has more than 2 dimensions and if a dimensions is
 * not TIME or ELVATION, a GetMap request needs to concatenate with "dim_"
 * prefix, such as:
 *
 * A WMS Layer is described as having an extent along a dimension named
 * “wavelength” as follows:
 * <Dimension name="wavelength" units="Angstrom" unitSymbol="Ao">3000,4000,5000,6000<Dimension>.
 * A GetMap request for a portrayal of the data at 4000 Angstroms would include
 * the parameter “DIM_WAVELENGTH=4000”.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = Dimension.TABLE_NAME)
public class Dimension implements Serializable {

    public static final String TABLE_NAME = TABLE_PREFIX + "_dimension";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    // Table C.1 - Contents of a dimension element 
    // One, Mandatory (dimension name)
    @Column(name = "name")
    private String name;

    // One, Mandatory (it can CRS for elevation (EPSG:5030), ISO8601 for time)
    @Column(name = "units")
    private String units;

    // One, Optional, specifying unit's symbol
    @Column(name = "unit_symbol")
    private String unitSymbol;

    // One, Optional, if GetMap request does not specify a value, then use this value
    // If attribute is absent, then shall respond with a service exception if request
    // does not include a value for that dimension.     
    // e.g: TIME=2000-08-03 
    // NOTE: by default set it to the geo upper bound of axis so a request without the dimension value still could return the (newest) highest 2D XY geo axes.
    @Column(name = "default_value")
    private String defaultValue;

    // One, Optional, indicating whether multiple values of the axis may be requested
    // Default is: 0 (single values only); 1 (multiple values permitted)
    @Column(name = "multiple_values")
    private int multipleValues = 0;

    // One, Optional, indicating whether nearest value of axis will be returned for a request with nearby value
    // Default is: 0 (request values must be correspond exactly to declared extent value(s))
    // Set to: 1 (request values may be approximate)
    @Column(name = "nearest_value")
    private int nearestValue = 0;

    // One, Optional, only for temporal extents (i.e: if dimension name is time)
    // Default is: 0 (request with TIME=current does not support)
    // Set to 1: The expression "TIME=current" means "send the most current data available" (last value of time axis)
    // The expression "TIME=start_time/current" means "send data from start_time up to the most current data available" (full extent of time axis)
    @Column(name = "current")
    private int current = 0;

    // One, Mandatory for displaying in GetCapabilities
    // Display the values of the geo bound of axis (Table C.2)
    // + min/max/resolution An interval defined by its lower and upper bounds and its resolution. 
    // If axis is regular, extent=lowerBound/upperBound/resolution
    // + value1,value2,value3,... a A list of multiple values
    // If axis is irregular, extent=value0, value1, value2,...,valueN
    @Lob
    @Column(name = "extent")
    private String extent;
    
    public Dimension() {

    }
    
    // Constructor with mandatory parameters
    public Dimension(String name, String units, String extent) {
        this.name = name;
        this.units = units;
        this.extent = extent;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getUnitSymbol() {
        return unitSymbol;
    }

    public void setUnitSymbol(String unitSymbol) {
        this.unitSymbol = unitSymbol;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getMultipleValues() {
        return multipleValues;
    }

    public void setMultipleValues(int multipleValues) {
        this.multipleValues = multipleValues;
    }

    public int getNearestValue() {
        return nearestValue;
    }

    public void setNearestValue(int nearestValue) {
        this.nearestValue = nearestValue;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public String getExtent() {
        return extent;
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }

    /**
     * Return the Dimension XML element representation
     *
     * @return
     */
    @JsonIgnore
    public String getRepresentation() {
        return getElement().toXML();
    }
    
    @JsonIgnore
    public Element getElement() {
        
        Element dimensionElement = new Element(XMLSymbols.LABEL_WMS_DIMENSION, NAMESPACE_WMS);
        Attribute nameAttribute = new Attribute(XMLSymbols.ATT_WMS_NAME, this.name);
        
        // Optional values according to Table C.1 WMS 1.3 document
//        Attribute unitsAttribute = new Attribute(XMLSymbols.ATT_WMS_UNITS, this.units);
//        Attribute defaultAttribute = new Attribute(XMLSymbols.ATT_WMS_DEFAULT, this.defaultValue);
//        Attribute multipleValuesAttribute = new Attribute(XMLSymbols.ATT_WMS_MULTIPLE_VALUES, String.valueOf(this.multipleValues));
//        Attribute nearestValueAttribute = new Attribute(XMLSymbols.ATT_WMS_NEAREST_VALUES, String.valueOf(this.nearestValue));
//        Attribute currentAttribute = new Attribute(XMLSymbols.ATT_WMS_CURRENT, String.valueOf(this.current));        
        
        dimensionElement.addAttribute(nameAttribute);
        
        // Optional values
//        dimensionElement.addAttribute(unitsAttribute);
//        dimensionElement.addAttribute(defaultAttribute);
//        dimensionElement.addAttribute(multipleValuesAttribute);
//        dimensionElement.addAttribute(nearestValueAttribute);
//        dimensionElement.addAttribute(currentAttribute);
        
        // Extent (e.g: 0,1000,3000,5000,10000), built when Petascope inserts layers
        dimensionElement.appendChild(this.extent);

        return dimensionElement;
    }    

}
