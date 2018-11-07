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
import static petascope.core.XMLSymbols.LABEL_GENERAL_GRID;
import static petascope.core.XMLSymbols.PREFIX_CIS11;
import petascope.core.gml.ISerializeToXMElement;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.XMLUtil;
import static petascope.core.XMLSymbols.NAMESPACE_CIS_11;

/**
 * Class to represent generalGrid element in CIS 1.1. e.g:
 
<cis11:generalGrid srsName="http://www.opengis.net/def/crs/EPSG/0/4979" axisLabels="Lat Long h">
    <cis11:regularAxis axisLabel="Lat" uomLabel="deg" lowerBound="-80" upperBound="-70" resolution="5"/>
    <cis11:regularAxis axisLabel="Long" uomLabel="deg" lowerBound="0" upperBound="10" resolution="5"/>
    <cis11:irregularAxis axisLabel="h" uomLabel="m">
        <cis11:C>10</cis11:C>
        <cis11:C>50</cis11:C>
        <cis11:C>100</cis11:C>
    </cis11:irregularAxis>
    <cis11:gridLimits srsName="http://www.opengis.net/def/crs/OGC/0/Index3D" axisLabels="i j k">
        <cis11:indexAxis axisLabel="i" lowerBound="0" upperBound="2"/>
        <cis11:indexAxis axisLabel="j" lowerBound="0" upperBound="2"/>
        <cis11:indexAxis axisLabel="j" lowerBound="0" upperBound="2"/>
    </cis11:gridLimits>
</cis11:generalGrid> 

 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class GeneralGrid implements ISerializeToXMElement {
    
    String srsName;
    String axisLabels;
    List<RegularAxis> regularAxes;
    List<IrregularAxis> irregularAxes;
    GridLimits gridLimits;

    public GeneralGrid(String srsName, String axisLabels, List<RegularAxis> regularAxes, List<IrregularAxis> irregularAxes, GridLimits gridLimits) {
        this.srsName = srsName;
        this.axisLabels = axisLabels;
        this.regularAxes = regularAxes;
        this.irregularAxes = irregularAxes;
        this.gridLimits = gridLimits;
    }

    public List<RegularAxis> getRegularAxes() {
        return regularAxes;
    }

    public void setRegularAxes(List<RegularAxis> regularAxes) {
        this.regularAxes = regularAxes;
    }

    public List<IrregularAxis> getIrregularAxes() {
        return irregularAxes;
    }

    public void setIrregularAxes(List<IrregularAxis> irregularAxes) {
        this.irregularAxes = irregularAxes;
    }

    public GridLimits getGridLimits() {
        return gridLimits;
    }

    public void setGridLimits(GridLimits gridLimits) {
        this.gridLimits = gridLimits;
    }
    
    @Override
    public Element serializeToXMLElement() throws PetascopeException, SecoreException {
        Element generalGridElement = new Element(XMLUtil.createXMLLabel(PREFIX_CIS11, LABEL_GENERAL_GRID), NAMESPACE_CIS_11);
        
        Attribute srsNameAttribute = new Attribute(ATT_SRS_NAME, NAMESPACE_CIS_11);
        srsNameAttribute.setValue(this.srsName);
        
        Attribute axisLabelsAttribute = new Attribute(ATT_AXIS_LABELS, NAMESPACE_CIS_11);
        axisLabelsAttribute.setValue(this.axisLabels);
        
        generalGridElement.addAttribute(srsNameAttribute);
        generalGridElement.addAttribute(axisLabelsAttribute);
        
        for (RegularAxis regularAxis : this.regularAxes) {
            Element regularAxisElement = regularAxis.serializeToXMLElement();
            generalGridElement.appendChild(regularAxisElement);
        }
        
        for (IrregularAxis irregularAxis : this.irregularAxes) {
            Element irregularAxisElement = irregularAxis.serializeToXMLElement();
            generalGridElement.appendChild(irregularAxisElement);
        }
        
        Element gridLimitsElement = this.gridLimits.serializeToXMLElement();
        generalGridElement.appendChild(gridLimitsElement);
        
        return generalGridElement;
    }
}
