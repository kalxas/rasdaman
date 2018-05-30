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
package petascope.wcps.metadata.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.rasdaman.domain.cis.*;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.repository.service.CoverageRepostioryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.CrsDefinition;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.core.AxisTypes;
import petascope.core.AxisTypes.AxisDirection;
import petascope.util.CrsUtil;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;
import petascope.wcps.metadata.model.Axis;
import petascope.wcps.metadata.model.IrregularAxis;
import petascope.wcps.metadata.model.NumericSubset;
import petascope.wcps.metadata.model.NumericTrimming;
import petascope.wcps.metadata.model.RegularAxis;

/**
 * This class translates different types of metadata into WcpsCoverageMetadata.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class WcpsCoverageMetadataTranslator {

    @Autowired
    private CoverageRepostioryService persistedCoverageService;


    public WcpsCoverageMetadataTranslator() {

    }

    /**
     * Create a WCPS coverage metadata object from a Coverage CIS 1.1 model in
     * database.
     *
     * @param coverageId
     * @return
     * @throws PetascopeException
     * @throws SecoreException
     */
    public WcpsCoverageMetadata create(String coverageId) throws PetascopeException, SecoreException {
        // Only supports GeneralGridCoverage now
        Coverage coverage = this.persistedCoverageService.readCoverageFullMetadataByIdFromCache(coverageId);
        List<GeoAxis> geoAxes = ((GeneralGridCoverage) coverage).getGeoAxes();
        List<IndexAxis> indexAxes = ((GeneralGridCoverage) coverage).getIndexAxes();

        // wcpsCoverageMetadata axis
        List<Axis> axes = buildAxes(geoAxes, indexAxes);
        List<RangeField> rangeFields = buildRangeFields(coverage.getRangeType().getDataRecord().getFields());
        // parse extra metadata of coverage to map
        String extraMetadata = coverage.getMetadata();
        List<NilValue> nilValues = coverage.getAllUniqueNullValues();
        
        String rasdamanCollectionName = coverage.getRasdamanRangeSet().getCollectionName();

        WcpsCoverageMetadata wcpsCoverageMetadata = new WcpsCoverageMetadata(coverageId, rasdamanCollectionName,
                                                        coverage.getCoverageType(), axes, coverage.getEnvelope().getEnvelopeByAxis().getSrsName(), 
                                                        rangeFields, nilValues, extraMetadata);

        return wcpsCoverageMetadata;
    }

    /**
     * Translate a persisted coverage in database by coverageId to a
     * WcpsCoverageMetadata. NOTE: if a WcpsCoverageMetadata is already
     * translated, it will get from cache.
     *
     * @param coverageId
     * @return
     * @throws petascope.exceptions.PetascopeException
     * @throws petascope.exceptions.SecoreException
     */
    public WcpsCoverageMetadata translate(String coverageId) throws PetascopeException, SecoreException {
        // NOTE: cannot cache a translated WCPS coverage metadata as a WCPS request can slice, trim to the coverage metadata which is stored in cache
        // and the next request will not have the full metadata as the previous one which is big error.
        WcpsCoverageMetadata  wcpsCoverageMetadata = this.create(coverageId);

        return wcpsCoverageMetadata;
    }

    /**
     * Build list of RangeField for WcpsCoverageMetadata object
     *
     * @param fields
     * @return
     */
    private List<RangeField> buildRangeFields(List<Field> fields) {
        List<RangeField> rangeFields = new ArrayList<>();
        // each field contains one quantity
        for (Field field : fields) {
            Quantity quantity = field.getQuantity();
            RangeField rangeField = new RangeField();
            // Data type for each band of coverage
            rangeField.setDataType(quantity.getDataType());
            rangeField.setName(field.getName());
            rangeField.setDescription(quantity.getDescription());
            rangeField.setDefinition(quantity.getDefinition());
            rangeField.setNodata(quantity.getNilValuesList());
            rangeField.setUomCode(quantity.getUom().getCode());
            rangeField.setAllowedValues(quantity.getAllowedValues());

            rangeFields.add(rangeField);
        }

        return rangeFields;
    }

    /**
     * Build list of axes for WcpsCoverageMetadata from the coverage's axes
     *
     * @param geoDomains
     * @param gridDomains
     * @return
     */
    private List<Axis> buildAxes(List<GeoAxis> geoAxes, List<IndexAxis> indexAxes) throws PetascopeException, SecoreException {
        List<Axis> result = new ArrayList();
        for (int i = 0; i < geoAxes.size(); i++) {
            GeoAxis geoAxis = geoAxes.get(i);
            String axisLabel = geoAxis.getAxisLabel();

            // NOTE: the order of geo CRS axes could be different from the index axes
            // e.g: CRS is EPSG:4326&Ansidate so the geo order is: Lat, Long, t, but in rasdaman, grid stored as: t, Lat, Long
            IndexAxis indexAxis = null;
            for (int j = 0; j < indexAxes.size(); j++) {
                indexAxis = indexAxes.get(j);
                String indexAxisLabelTmp = indexAxis.getAxisLabel();
                if (axisLabel.equals(indexAxisLabelTmp)) {
                    break;
                }
            }

            // geoBounds is the geo bounds of axis in the coverage (but can be modified later by subsets)
            NumericSubset geoBounds = new NumericTrimming(geoAxis.getLowerBoundNumber(), geoAxis.getUpperBoundNumber());
            NumericSubset gridBounds = new NumericTrimming(new BigDecimal(indexAxis.getLowerBound()), new BigDecimal(indexAxis.getUpperBound()));
            String crsUri = geoAxis.getSrsName();

            CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(crsUri);
            // x, y, t,...
            String axisType = CrsUtil.getAxisType(crsDefinition, axisLabel);

            AxisDirection axisDirection = null;
            if (axisType.equals(AxisTypes.X_AXIS)) {
                axisDirection = AxisDirection.EASTING;
            } else if (axisType.equals(AxisTypes.Y_AXIS)) {
                axisDirection = AxisDirection.NORTHING;
            } else if (axisType.equals(AxisTypes.HEIGHT_AXIS)) {
                axisDirection = AxisDirection.UP;
            } else if (axisType.equals(AxisTypes.DEPTH_AXIS)) {
                axisDirection = AxisDirection.DOWN;
            } else if (axisType.equals(AxisTypes.T_AXIS)) {
                axisDirection = AxisDirection.FUTURE;
            } else {
                axisDirection = AxisDirection.UNKNOWN;
            }

            // Get the metadata of CRS (needed when using TimeCrs)
            String axisUoM = geoAxis.getUomLabel();
            // the order of geo axis stored in rasdaman as grid axis (e.g: CRS order EPSG:4326 Lat, Long, but in grid order is: Long, Lat)
            int gridAxisOrder = indexAxis.getAxisOrder();

            // NOTE: this needs the "sign" of offset vector as well
            BigDecimal scalarResolution = geoAxis.getResolution();
            BigDecimal originNumber = this.getOriginNumber(geoAxis);

            // Check domainElement's type
            if (geoAxis.isIrregular()) {
                // All stored coefficients for irregular axis in coverage
                List<BigDecimal> directPositions = ((org.rasdaman.domain.cis.IrregularAxis) geoAxis).getDirectPositionsAsNumbers();
                result.add(new IrregularAxis(axisLabel, geoBounds, gridBounds, axisDirection,
                        crsUri, crsDefinition, axisType, axisUoM, gridAxisOrder,
                        originNumber, scalarResolution, directPositions));
            } else {

                result.add(new RegularAxis(axisLabel, geoBounds, gridBounds, axisDirection,
                        crsUri, crsDefinition, axisType, axisUoM, gridAxisOrder,
                        originNumber, scalarResolution));
            }
        }
        return result;
    }

    private BigDecimal getOriginNumber(GeoAxis geoAxis) throws PetascopeException, SecoreException {
        BigDecimal origin;

        if (geoAxis.isIrregular()) {
            // Only supports irregular with positive resolution
            origin = geoAxis.getLowerBoundNumber();
        } else {
            BigDecimal resolution = geoAxis.getResolution();
            BigDecimal lowerBound = geoAxis.getLowerBoundNumber();
            BigDecimal upperBound = geoAxis.getUpperBoundNumber();
            //if axis is regular positive axis we apply formula: origin = (geoMinValue + 0.5) * resolution (> 0)
            if (resolution.compareTo(BigDecimal.ZERO) > 0) {
                origin = lowerBound.add(BigDecimal.valueOf(1.0 / 2)
                        .multiply(resolution)).stripTrailingZeros();
            } else {
                // if axis is regular negative axis, origin = (geoMaxValue + 0.5) * resolution (< 0)
                origin = upperBound.add(BigDecimal.valueOf(1.0 / 2)
                        .multiply(resolution)).stripTrailingZeros();
            }
        }

        return origin;
    }
}
