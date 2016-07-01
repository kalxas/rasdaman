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
package petascope.wcps2.metadata.service;

import petascope.core.CoverageMetadata;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps2.metadata.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import petascope.core.CrsDefinition;
import petascope.wcps.server.core.RangeElement;

/**
 * This class translates different types of metadata into WcpsCoverageMetadata.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class WcpsCoverageMetadataTranslator {

    public WcpsCoverageMetadataTranslator() {}

    public WcpsCoverageMetadata translate(CoverageMetadata metadata) {
        List<Axis> axes = buildAxes(metadata.getDomainList(), metadata.getCellDomainList());
        List<RangeField> rangeFields = buildRangeFields(metadata.getRangeIterator());
        return new WcpsCoverageMetadata(metadata.getCoverageName(), axes,
                                        CrsUtil.CrsUri.createCompound(metadata.getCrsUris()),
                                        CrsUtility.getImageCrsUri(axes),
                                        rangeFields);
    }

    private List<RangeField> buildRangeFields(Iterator<RangeElement> rangeIterator){
        List<RangeField> rangeFields = new ArrayList<RangeField>();
        while(rangeIterator.hasNext()) {
            rangeFields.add(new RangeField(rangeIterator.next().getName()));
        }

        return rangeFields;
    }

    private List<Axis> buildAxes(List<DomainElement> geoDomains, List<CellDomainElement> gridDomains) {
        List<Axis> result = new ArrayList();
        for (int i = 0; i < geoDomains.size(); i++) {
            DomainElement currentGeo = geoDomains.get(i);
            CellDomainElement currentGrid = gridDomains.get(i);

            // geoBounds is the geo bounds of axis in the coverage (but can be modified later by subsets)
            NumericSubset geoBounds = new NumericTrimming(currentGeo.getMinValue(), currentGeo.getMaxValue());
            NumericSubset gridBounds = new NumericTrimming(new BigDecimal(currentGrid.getLo()), new BigDecimal(currentGrid.getHi()));
            String crsUri = currentGeo.getNativeCrs();
            AxisDirection axisDirection;

            // x, y, t,...
            String axisType = currentGeo.getAxisDef().getType();

            if (axisType.equals(AxisTypes.X_AXIS)) {
                axisDirection = AxisDirection.EASTING;
            } else if (axisType.equals(AxisTypes.Y_AXIS)) {
                axisDirection = AxisDirection.NORTHING;
            } else if (axisType.equals(AxisTypes.T_AXIS)) {
                axisDirection = AxisDirection.FUTURE;
            } else {
                axisDirection = AxisDirection.UNKNOWN;
            }

            // Get the metadata of CRS (needed when using TimeCrs)
            CrsDefinition crsDefinition = currentGeo.getAxisDef().getCrsDefinition();
            BigDecimal scalarResolution = currentGeo.getScalarResolution();
            String axisUoM = currentGeo.getUom();
            int rasdamanOrder = currentGeo.getOrder();
            // Check domainElement's type
            if (currentGeo.isIrregular()) {
                // Need the iOder of axis to query coeffcients
                int iOrder = currentGeo.getOrder();
                result.add(new IrregularAxis(currentGeo.getLabel(), geoBounds, gridBounds, axisDirection,
                                             crsUri, crsDefinition, axisType, axisUoM, iOrder, scalarResolution, rasdamanOrder));
            }
            else{
                BigDecimal resolution = currentGeo.getDirectionalResolution();
                result.add(new RegularAxis(currentGeo.getLabel(), geoBounds, gridBounds, axisDirection,
                                             crsUri, crsDefinition, resolution, axisType, axisUoM, scalarResolution, rasdamanOrder));
            }
        }
        return result;
    }

}
