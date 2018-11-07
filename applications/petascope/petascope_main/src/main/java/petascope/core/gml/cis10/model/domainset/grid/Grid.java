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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.gml.cis10.model.domainset.grid;

import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_AXIS_LABELS;
import static petascope.core.XMLSymbols.ATT_DIMENSION;
import static petascope.core.XMLSymbols.ATT_ID;
import static petascope.core.XMLSymbols.LABEL_GRID;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to represent Grid element of Grid coverage. e.g:
 
<gml:Grid dimension="4" gml:id="test_float_4d-grid">
    <gml:limits>
        <gml:GridEnvelope>
            <gml:low>0 0 0 -20</gml:low>
            <gml:high>0 0 39 19</gml:high>
        </gml:GridEnvelope>
    </gml:limits>
    <gml:axisLabels>i j k m</gml:axisLabels>
</gml:Grid>
 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Grid implements ISerializeToXMElement {
    
    protected String id;
    protected String dimension;    
    protected Limits limits;
    protected String axisLabels;

    public Grid(String id, String dimension, Limits limits, String axisLabels) {
        this.id = id;
        this.dimension = dimension;        
        this.limits = limits;
        this.axisLabels = axisLabels;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Limits getLimits() {
        return limits;
    }

    public void setLimits(Limits limits) {
        this.limits = limits;
    }

    public String getAxisLabels() {
        return axisLabels;
    }

    public void setAxisLabels(String axisLabels) {
        this.axisLabels = axisLabels;
    }

    @Override
    public Element serializeToXMLElement() {
        Element gridElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_GRID), NAMESPACE_GML);
        
        Attribute dimensionAttribute = new Attribute(ATT_DIMENSION, dimension);
        Attribute idAttribute = XMLUtil.createXMLAttribute(NAMESPACE_GML, PREFIX_GML, ATT_ID, id);
        gridElement.addAttribute(dimensionAttribute);
        gridElement.addAttribute(idAttribute);
        
        Element limitsElement = this.limits.serializeToXMLElement();
        Element axisLabelsElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, ATT_AXIS_LABELS), NAMESPACE_GML);
        axisLabelsElement.appendChild(this.axisLabels);
        
        gridElement.appendChild(limitsElement);
        gridElement.appendChild(axisLabelsElement);
        
        return gridElement;
    }
}
