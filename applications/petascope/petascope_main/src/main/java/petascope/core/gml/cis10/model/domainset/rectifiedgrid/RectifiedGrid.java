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
package petascope.core.gml.cis10.model.domainset.rectifiedgrid;

import java.util.List;
import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_AXIS_LABELS;
import static petascope.core.XMLSymbols.ATT_DIMENSION;
import static petascope.core.XMLSymbols.ATT_ID;
import static petascope.core.XMLSymbols.LABEL_RECTIFIED_GRID;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.PREFIX_GML;
import petascope.core.gml.ISerializeToXMElement;
import petascope.core.gml.cis10.model.domainset.grid.Grid;
import petascope.util.XMLUtil;

/**
 * Class to represent RectifiedGrid element of RectifiedGrid coverage. e.g:
 
<gml:RectifiedGrid gml:id="Hungary__HU_SSAND-grid" dimension="2">
    <gml:limits>
        <gml:GridEnvelope>
            <gml:low>0 0</gml:low>
            <gml:high>2113 1614</gml:high>
        </gml:GridEnvelope>
    </gml:limits>
    <gml:axisLabels>i j</gml:axisLabels>
    <gml:origin>
        <gml:Point gml:id="p00_Hungary__HU_SSAND" srsName="http://www.opengis.net/def/crs/EPSG/0/23700">
            <gml:pos>415267.866168 408521.3387750001</gml:pos>
        </gml:Point>
    </gml:origin>
    <gml:offsetVector srsName="http://www.opengis.net/def/crs/EPSG/0/23700">250.0 0.0</gml:offsetVector>
    <gml:offsetVector srsName="http://www.opengis.net/def/crs/EPSG/0/23700">0.0 -249.9999999997461</gml:offsetVector>
</gml:RectifiedGrid>
  
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class RectifiedGrid extends Grid implements ISerializeToXMElement {
    
    public RectifiedGrid(Grid grid) {
        super(grid.getId(), grid.getDimension(), grid.getLimits(), grid.getAxisLabels());
    }
    
    public RectifiedGrid(RectifiedGrid rectifiedGrid) {
        super(rectifiedGrid.getId(), rectifiedGrid.getDimension(), rectifiedGrid.getLimits(), rectifiedGrid.getAxisLabels());
        this.origin = rectifiedGrid.getOrigin();
        this.offsetVectors = rectifiedGrid.getOffsetVectors();
    }
    
    protected Origin origin;
    protected List<OffsetVector> offsetVectors;

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public List<OffsetVector> getOffsetVectors() {
        return offsetVectors;
    }

    public void setOffsetVectors(List<OffsetVector> offsetVectors) {
        this.offsetVectors = offsetVectors;
    }    

    @Override
    public Element serializeToXMLElement() {
        Element rectifiedGridElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, LABEL_RECTIFIED_GRID), NAMESPACE_GML);
        
        Attribute dimensionAttribute = new Attribute(ATT_DIMENSION, dimension);
        Attribute idAttribute = XMLUtil.createXMLAttribute(NAMESPACE_GML, PREFIX_GML, ATT_ID, id);
        rectifiedGridElement.addAttribute(dimensionAttribute);
        rectifiedGridElement.addAttribute(idAttribute);
        
        Element limitsElement = this.limits.serializeToXMLElement();
        Element axisLabelsElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, ATT_AXIS_LABELS), NAMESPACE_GML);
        axisLabelsElement.appendChild(this.axisLabels);
        
        rectifiedGridElement.appendChild(limitsElement);
        rectifiedGridElement.appendChild(axisLabelsElement);
        
        Element originElement = this.origin.serializeToXMLElement();
        rectifiedGridElement.appendChild(originElement);
        
        for (OffsetVector offsetVector : this.offsetVectors) {
            Element offsetVectorElement = offsetVector.serializeToXMLElement();
            rectifiedGridElement.appendChild(offsetVectorElement);
        }
        
        return rectifiedGridElement;
    }
}
