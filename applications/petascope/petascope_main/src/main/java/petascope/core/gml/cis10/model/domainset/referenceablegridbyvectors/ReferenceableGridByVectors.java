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
package petascope.core.gml.cis10.model.domainset.referenceablegridbyvectors;

import java.util.List;
import nu.xom.Attribute;
import nu.xom.Element;
import static petascope.core.XMLSymbols.ATT_AXIS_LABELS;
import static petascope.core.XMLSymbols.ATT_DIMENSION;
import static petascope.core.XMLSymbols.ATT_ID;
import static petascope.core.XMLSymbols.LABEL_REFERENCEABLE_GRID_BY_VECTORS;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import static petascope.core.XMLSymbols.NAMESPACE_GMLRGRID;
import static petascope.core.XMLSymbols.PREFIX_GML;
import static petascope.core.XMLSymbols.PREFIX_GMLRGRID;
import petascope.core.gml.ISerializeToXMElement;
import petascope.util.XMLUtil;
import petascope.core.gml.cis10.model.domainset.rectifiedgrid.RectifiedGrid;


/**
 * Class to represent ReferenceableGridByVectors element of ReferenceableGrid coverage. e.g:
 
<gmlrgrid:ReferenceableGridByVectors dimension="3" gml:id="test_irr_cube_2-grid">
    <gml:limits>
        <gml:GridEnvelope>
            <gml:low>0 0 0</gml:low>
            <gml:high>62 35 3</gml:high>
        </gml:GridEnvelope>
    </gml:limits>
    <gml:axisLabels>E N ansi</gml:axisLabels>
    <gmlrgrid:origin>
        <gmlrgrid:Point gml:id="test_irr_cube_2-origin" srsName="http://localhost:8080/def/crs/OGC/0/AnsiDate">
            <gmlrgrid:pos>80042.7273594 5449865.55794 "2008-01-01T02:01:20.000Z"</gmlrgrid:pos>
        </gmlrgrid:Point>
    </gmlrgrid:origin>
    <gmlrgrid:generalGridAxis>
        ...
        <gmlrgrid:GeneralGridAxis>
            <gmlrgrid:offsetVector srsName="http://localhost:8080/def/crs/OGC/0/AnsiDate">0 0 1</gmlrgrid:offsetVector>
            <gmlrgrid:coefficients>"2008-01-01T02:01:20.000Z" "2008-01-03T23:59:55.000Z" "2008-01-05T01:58:30.000Z" "2008-01-08T00:02:58.000Z"</gmlrgrid:coefficients>
            <gmlrgrid:gridAxesSpanned>ansi</gmlrgrid:gridAxesSpanned>
            <gmlrgrid:sequenceRule axisOrder="+1">Linear</gmlrgrid:sequenceRule>
        </gmlrgrid:GeneralGridAxis>
    <gmlrgrid:generalGridAxis>
</gmlrgrid>
  
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class ReferenceableGridByVectors extends RectifiedGrid implements ISerializeToXMElement {
    
    List<GeneralGridAxis> generalGridAxes;
    
    public ReferenceableGridByVectors(RectifiedGrid rectifiedGrid) {
        super(rectifiedGrid);
    }

    public List<GeneralGridAxis> getGeneralGridAxes() {
        return generalGridAxes;
    }

    public void setGeneralGridAxes(List<GeneralGridAxis> generalGridAxes) {
        this.generalGridAxes = generalGridAxes;
    }

    @Override
    public Element serializeToXMLElement() {
        Element referenceableGridByVectorsElement = new Element(XMLUtil.createXMLLabel(PREFIX_GMLRGRID, LABEL_REFERENCEABLE_GRID_BY_VECTORS), NAMESPACE_GMLRGRID);
        
        Attribute dimensionAttribute = new Attribute(ATT_DIMENSION, dimension);
        Attribute idAttribute = XMLUtil.createXMLAttribute(NAMESPACE_GMLRGRID, PREFIX_GMLRGRID, ATT_ID, id);
        referenceableGridByVectorsElement.addAttribute(dimensionAttribute);
        referenceableGridByVectorsElement.addAttribute(idAttribute);
        
        Element limitsElement = this.limits.serializeToXMLElement();
        Element axisLabelsElement = new Element(XMLUtil.createXMLLabel(PREFIX_GML, ATT_AXIS_LABELS), NAMESPACE_GML);
        axisLabelsElement.appendChild(this.axisLabels);
        
        referenceableGridByVectorsElement.appendChild(limitsElement);
        referenceableGridByVectorsElement.appendChild(axisLabelsElement);
        
        Element originElement = this.origin.serializeToXMLElement();
        referenceableGridByVectorsElement.appendChild(originElement);
        
        for (GeneralGridAxis generalGridAxis : this.generalGridAxes) {
            Element generalGridAxisElement = generalGridAxis.serializeToXMLElement();
            referenceableGridByVectorsElement.appendChild(generalGridAxisElement);
        }
        
        return referenceableGridByVectorsElement;
    }
    
}
