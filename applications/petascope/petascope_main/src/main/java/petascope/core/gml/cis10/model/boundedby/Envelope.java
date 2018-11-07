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
package petascope.core.gml.cis10.model.boundedby;

import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_AXIS_LABELS;
import static petascope.core.XMLSymbols.ATT_SRS_DIMENSION;
import static petascope.core.XMLSymbols.ATT_SRS_NAME;
import static petascope.core.XMLSymbols.ATT_UOM_LABELS;
import static petascope.core.XMLSymbols.LABEL_ENVELOPE;
import static petascope.core.XMLSymbols.LABEL_LOWER_CORNER;
import static petascope.core.XMLSymbols.LABEL_UPPER_CORNER;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;

/**
 * Class to represent Envelope element in CIS 1.0. e.g:

<gml:Envelope srsName="http://www.opengis.net/def/crs/EPSG/0/23700" axisLabels="Y X" uomLabels="m m" srsDimension="2">
    <gml:lowerCorner>415142.866168 4896.338775410026</gml:lowerCorner>
    <gml:upperCorner>943642.866168 408646.338775</gml:upperCorner>
</gml:Envelope>

* @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Envelope implements ISerializeToXMElement {
    
    String axisLabels;
    String srsDimension;
    String srsName;
    String uomLabels;
    
    String lowerCorner;
    String upperCorner;

    public String getAxisLabels() {
        return axisLabels;
    }

    public void setAxisLabels(String axisLabels) {
        this.axisLabels = axisLabels;
    }

    public String getSrsDimension() {
        return srsDimension;
    }

    public void setSrsDimension(String srsDimension) {
        this.srsDimension = srsDimension;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

    public String getUomLabels() {
        return uomLabels;
    }

    public void setUomLabels(String uomLabels) {
        this.uomLabels = uomLabels;
    }

    public String getLowerCorner() {
        return lowerCorner;
    }

    public void setLowerCorner(String lowerCorner) {
        this.lowerCorner = lowerCorner;
    }

    public String getUpperCorner() {
        return upperCorner;
    }

    public void setUpperCorner(String upperCorner) {
        this.upperCorner = upperCorner;
    }

    @Override
    public Element serializeToXMLElement() {
        Element envelopeElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_ENVELOPE), NAMESPACE_GML);
        
        Attribute srsNameAttribute = new Attribute(ATT_SRS_NAME, srsName);
        Attribute axisLabelsAttribute = new Attribute(ATT_AXIS_LABELS, axisLabels);
        Attribute uomLabelsAttribute = new Attribute(ATT_UOM_LABELS, uomLabels);
        Attribute srsDimensionAttribute = new Attribute(ATT_SRS_DIMENSION, srsDimension);
        
        envelopeElement.addAttribute(srsNameAttribute);
        envelopeElement.addAttribute(axisLabelsAttribute);
        envelopeElement.addAttribute(uomLabelsAttribute);
        envelopeElement.addAttribute(srsDimensionAttribute);
        
        Element lowerCornerElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_LOWER_CORNER), NAMESPACE_GML);
        lowerCornerElement.appendChild(lowerCorner);
        
        Element upperCornerElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_UPPER_CORNER), NAMESPACE_GML);
        upperCornerElement.appendChild(upperCorner);
        
        envelopeElement.appendChild(lowerCornerElement);
        envelopeElement.appendChild(upperCornerElement);
        
        return envelopeElement;
    }
}
