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
package petascope.core.gml.cis11.model.domainset;

import java.util.List;
import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_AXIS_LABELS;
import static petascope.core.XMLSymbols.ATT_SRS_NAME;
import static petascope.core.XMLSymbols.LABEL_GRID_LIMITS;
import static petascope.core.XMLSymbols.PREFIX_CIS11;
import petascope.core.gml.ISerializeToXMElement;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.XMLUtil;
import static petascope.core.XMLSymbols.NAMESPACE_CIS_11;

/**
 * Class to represent gridLimits element in CIS 1.1. e.g:
 
<cis11:gridLimits srsName="http://www.opengis.net/def/crs/OGC/0/Index3D" axisLabels="i j k">
    <cis11:indexAxis axisLabel="i" lowerBound="0" upperBound="2"/>
    <cis11:indexAxis axisLabel="j" lowerBound="0" upperBound="2"/>
    <cis11:indexAxis axisLabel="j" lowerBound="0" upperBound="2"/>
</cis11:gridLimits>
  
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class GridLimits implements ISerializeToXMElement {
    
    private String srsName;
    private String axisLabels;
    List<IndexAxis> indexAxes;

    public GridLimits(String srsName, String axisLabels, List<IndexAxis> indexAxes) {
        this.srsName = srsName;
        this.axisLabels = axisLabels;
        this.indexAxes = indexAxes;
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

    public List<IndexAxis> getIndexAxes() {
        return indexAxes;
    }

    public void setIndexAxes(List<IndexAxis> indexAxes) {
        this.indexAxes = indexAxes;
    }
    
    @Override
    public Element serializeToXMLElement() throws PetascopeException, SecoreException {
        Element gridLimitsElement = new Element(XMLUtil.createXMLLabel(PREFIX_CIS11, LABEL_GRID_LIMITS), NAMESPACE_CIS_11);
        
        Attribute srsNameAttribute = new Attribute(ATT_SRS_NAME, NAMESPACE_CIS_11);
        srsNameAttribute.setValue(this.srsName);
        
        Attribute axisLabelsAttribute = new Attribute(ATT_AXIS_LABELS, NAMESPACE_CIS_11);
        axisLabelsAttribute.setValue(this.axisLabels);
        
        gridLimitsElement.addAttribute(srsNameAttribute);
        gridLimitsElement.addAttribute(axisLabelsAttribute);
        
        for (IndexAxis indexAxis : this.indexAxes) {
            Element indexAxisElement = indexAxis.serializeToXMLElement();
            gridLimitsElement.appendChild(indexAxisElement);
        }
        
        return gridLimitsElement;
    }
}
