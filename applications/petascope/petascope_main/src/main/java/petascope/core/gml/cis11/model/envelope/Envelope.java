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
package petascope.core.gml.cis11.model.envelope;

import java.util.List;
import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_AXIS_LABELS;
import static petascope.core.XMLSymbols.ATT_SRS_DIMENSION;
import static petascope.core.XMLSymbols.ATT_SRS_NAME;
import static petascope.core.XMLSymbols.LABEL_ENVELOPE;
import static petascope.core.XMLSymbols.PREFIX_CIS11;
import petascope.core.gml.ISerializeToXMElement;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.XMLUtil;
import static petascope.core.XMLSymbols.NAMESPACE_CIS_11;

/**
 * Class to represent envelope element in CIS 1.1. e.g:

<cis11:envelope srsName=http://www.opengis.net/def/crs/EPSG/0/4326
    axisLabels="Lat Long" srsDimension="2"> 
    <cis11:axisExtent axisLabel="Lat"  uomLabel="deg" lowerBound="1" upperBound="5"/>
    <cis11:axisExtent axisLabel="Long" uomLabel="deg" lowerBound="1" upperBound="3"/>
</cis11:envelope>

* @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Envelope implements ISerializeToXMElement {
    
    private String srsName;
    private String axisLabels;
    private String srsDimension;
    List<AxisExtent> axisExtents;

    public Envelope(String srsName, String axisLabels, String srsDimension, List<AxisExtent> axisExtents) {
        this.srsName = srsName;
        this.axisLabels = axisLabels;
        this.srsDimension = srsDimension;
        this.axisExtents = axisExtents;
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String srsName) {
        this.srsName = srsName;
    }

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

    public List<AxisExtent> getAxisExtents() {
        return axisExtents;
    }

    public void setAxisExtents(List<AxisExtent> axisExtents) {
        this.axisExtents = axisExtents;
    }
    

    @Override
    public Element serializeToXMLElement() throws PetascopeException {
        Element envelopeElement = new Element(XMLUtil.createXMLLabel(PREFIX_CIS11, LABEL_ENVELOPE), NAMESPACE_CIS_11);
        
        Attribute srsNameAttribute = new Attribute(ATT_SRS_NAME, NAMESPACE_CIS_11);
        srsNameAttribute.setValue(this.srsName);
        
        Attribute axisLabelsAttribute = new Attribute(ATT_AXIS_LABELS, NAMESPACE_CIS_11);
        axisLabelsAttribute.setValue(this.axisLabels);
        
        Attribute srsDimensionAttribute = new Attribute(ATT_SRS_DIMENSION, NAMESPACE_CIS_11);
        srsDimensionAttribute.setValue(this.srsDimension);
        
        envelopeElement.addAttribute(srsNameAttribute);
        envelopeElement.addAttribute(axisLabelsAttribute);
        envelopeElement.addAttribute(srsDimensionAttribute);
        
        for (AxisExtent axisExtent : this.axisExtents) {
            Element axisExtentElement = axisExtent.serializeToXMLElement();
            envelopeElement.appendChild(axisExtentElement);
        }
        
        return envelopeElement;
    }
    
}
