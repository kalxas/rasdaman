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

import org.apache.commons.lang3.StringUtils;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.swe.datamodel.*;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps2.metadata.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        List<RangeField> rangeFields = buildRangeFields(metadata.getRangeIterator(), metadata.getSweComponentsIterator());        
        Set<String> metadataList = metadata.getExtraMetadata(DbMetadataSource.EXTRAMETADATA_TYPE_GMLCOV);
        // parse extra metadata of coverage to map
        String extraMetadata = StringUtils.join(metadataList, "");
        List<NilValue> nodata = metadata.getAllUniqueNullValues();
        return new WcpsCoverageMetadata(metadata.getCoverageName(), metadata.getCoverageType(), axes,
                                        CrsUtil.CrsUri.createCompound(metadata.getCrsUris()),
                                        rangeFields, extraMetadata);
    }

    private List<RangeField> buildRangeFields(Iterator<RangeElement> rangeIterator, Iterator<AbstractSimpleComponent> sweIterator) {
        List<RangeField> rangeFields = new ArrayList<RangeField>();
        while (rangeIterator.hasNext()) {
            RangeElement rangeElement = rangeIterator.next();
            Quantity quantity = (Quantity) sweIterator.next();

            rangeFields.add(new RangeField(rangeElement.getType(), rangeElement.getName(), quantity.getDescription(),
                                           parseNodataValues(quantity.getNilValuesIterator()), quantity.getUom(), quantity.getDefinition(),
                                           quantity.getAllowedValues()));
        }

        return rangeFields;
    }

    private List<Interval<BigDecimal>> parseAllowedValues(AllowedValues allowedValues) {
        List<Interval<BigDecimal>> ret = new ArrayList<Interval<BigDecimal>>();
        Iterator<RealPair> allowedValuesIterator = allowedValues.getIntervalIterator();
        while (allowedValuesIterator.hasNext()) {
            RealPair nextInterval = allowedValuesIterator.next();
            ret.add(new Interval<BigDecimal>(nextInterval.getMin(), nextInterval.getMax()));
        }
        return ret;
    }

    private List<BigDecimal> parseNodataValues(List<NilValue> nullValues) {
        List<BigDecimal> result = new ArrayList<BigDecimal>();
        for (NilValue nullValue : nullValues) {
            try {
                result.add(new BigDecimal(nullValue.getValue()));
            } catch (Exception e) {
                //failed converting to double, don't add it
                Logger.getLogger(WcpsCoverageMetadataTranslator.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return result;
    }

    private List<NilValue> parseNodataValues(Iterator<NilValue> nilValueIterator) {
        List<NilValue> ret = new ArrayList<NilValue>();
        while (nilValueIterator.hasNext()) {
            NilValue number = nilValueIterator.next();
            ret.add(number);
        }
        return ret;
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
            String axisUoM = currentGeo.getUom();
            int rasdamanOrder = currentGeo.getOrder();

            // NOTE: this needs the "sign" of offset vector as well
            BigDecimal scalarResolution = currentGeo.getDirectionalResolution();


            // Check domainElement's type
            if (currentGeo.isIrregular()) {
                // Need the iOder of axis to query coeffcients
                result.add(new IrregularAxis(currentGeo.getLabel(), geoBounds, gridBounds, axisDirection,
                                             crsUri, crsDefinition, axisType, axisUoM, scalarResolution, rasdamanOrder, getOrigin(currentGeo), currentGeo.getDirectionalResolution()));
            } else {

                result.add(new RegularAxis(currentGeo.getLabel(), geoBounds, gridBounds, axisDirection,
                                           crsUri, crsDefinition, axisType, axisUoM, scalarResolution, rasdamanOrder, getOrigin(currentGeo), currentGeo.getDirectionalResolution()));
            }
        }
        return result;
    }

    private BigDecimal getOrigin(DomainElement currentGeo){
        BigDecimal origin;

        if(currentGeo.isIrregular()) {
            if (currentGeo.getDirectionalResolution().compareTo(BigDecimal.ZERO) > 0) {
                // longitude with offset vector > 0, min is origin
                origin = currentGeo.getMinValue().stripTrailingZeros();
            } else {
                // latitude with offset vector < 0 (max is origin)
                origin = currentGeo.getMaxValue().stripTrailingZeros();
            }
        } else {
            //if axis is regular we apply formula: origin =(geoMinValue + 0.5) * scalarResolution
            if (currentGeo.getDirectionalResolution().compareTo(BigDecimal.ZERO) > 0) {
                origin = currentGeo.getMinValue().add(BigDecimal.valueOf(1.0 / 2)
                        .multiply(currentGeo.getDirectionalResolution())).stripTrailingZeros();
            } else {
                // e.g: origin =(geoMaxValue + 0.5) * scalarResolution
                origin = currentGeo.getMaxValue().add(BigDecimal.valueOf(1.0 / 2)
                        .multiply(currentGeo.getDirectionalResolution())).stripTrailingZeros();
            }
        }

        return origin;
    }


}
