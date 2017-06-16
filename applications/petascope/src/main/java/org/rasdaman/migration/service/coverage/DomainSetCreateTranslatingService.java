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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.migration.service.coverage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.rasdaman.domain.cis.DomainSet;
import org.rasdaman.domain.cis.GeneralGrid;
import org.rasdaman.domain.cis.GeneralGridDomainSet;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.GridLimits;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.cis.IrregularAxis;
import org.rasdaman.domain.cis.RegularAxis;
import org.rasdaman.migration.domain.legacy.LegacyCellDomainElement;
import org.rasdaman.migration.domain.legacy.LegacyCoverageMetadata;
import petascope.util.CrsUtil;
import org.rasdaman.migration.domain.legacy.LegacyDbMetadataSource;
import org.rasdaman.migration.domain.legacy.LegacyDomainElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.util.TimeUtil;
import org.rasdaman.migration.domain.legacy.LegacyAxisTypes;

/**
 * Create a DomainSet object from legacy CoverageMetadata object
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class DomainSetCreateTranslatingService {

    @Autowired
    private LegacyDbMetadataSource meta;

    /**
     * Create a DomainSet(GeneralGridDomainSet) object for legacy coverage
     * metadata which does not exist in database
     *
     * @param coverageMetadata
     * @return
     * @throws Exception
     */
    public DomainSet create(LegacyCoverageMetadata coverageMetadata) throws Exception {

        // This is list of geo axes
        List<GeoAxis> geoAxes = this.createGeoAxes(coverageMetadata);
        List<IndexAxis> indexAxes = this.createIndexAxes(coverageMetadata);
        GridLimits gridLimits = this.createGridLimits(indexAxes);
        GeneralGrid generalGrid = this.createGeneralGrid(coverageMetadata, geoAxes, gridLimits);

        // As old coverages's type is grid only
        DomainSet domainSet = new GeneralGridDomainSet(generalGrid);

        return domainSet;
    }

    /**
     * Create a list of geo axis (regular, irregular) from coverageMetadata
     *
     * @param coverageMetadata
     * @return
     */
    private List<GeoAxis> createGeoAxes(LegacyCoverageMetadata coverageMetadata) throws Exception {
        List<GeoAxis> geoAxes = new ArrayList<>();
        for (LegacyDomainElement domainElement : coverageMetadata.getDomainList()) {
            String lowerBound;
            String upperBound;
            if (domainElement.getType().equals(LegacyAxisTypes.T_AXIS)) {
                // The domain already calculated with resolution > 1 (if any)
                lowerBound = TimeUtil.valueToISODateTime(BigDecimal.ZERO, domainElement.getMinValue(), domainElement.getCrsDef());
                upperBound = TimeUtil.valueToISODateTime(BigDecimal.ZERO, domainElement.getMaxValue(), domainElement.getCrsDef());
            } else {
                lowerBound = domainElement.getMinValue().toPlainString();
                upperBound = domainElement.getMaxValue().toPlainString();
            }
            if (!domainElement.isIrregular()) {
                // Regular axis
                RegularAxis regularAxis = new RegularAxis();
                regularAxis.setAxisLabel(domainElement.getLabel());
                regularAxis.setLowerBound(lowerBound);
                regularAxis.setUpperBound(upperBound);
                regularAxis.setResolution(domainElement.getDirectionalResolution());
                regularAxis.setUomLabel(domainElement.getUom());
                regularAxis.setSrsName(domainElement.getNativeCrs());

                geoAxes.add(regularAxis);
            } else {
                // Irregular axis (only supports independent irregular axis, not correlated grid axes (nest))
                IrregularAxis irregularAxis = new IrregularAxis();
                Integer iOrder = domainElement.getOrder();

                // There is a big disadvantage from legacy domainElement which could not get the directPositions (coefficients)
                // so must query from database to get all the coefficient for an axis
                List<BigDecimal> directPositions = meta.getAllCoefficients(coverageMetadata.getCoverageName(), iOrder);
                irregularAxis.setAxisLabel(domainElement.getLabel());
                irregularAxis.setLowerBound(lowerBound);
                irregularAxis.setUpperBound(upperBound);
                irregularAxis.setDirectPositions(directPositions);
                // Default it is 1
                irregularAxis.setResolution(domainElement.getDirectionalResolution());
                irregularAxis.setUomLabel(domainElement.getUom());
                irregularAxis.setSrsName(domainElement.getNativeCrs());

                geoAxes.add(irregularAxis);
            }
        }

        return geoAxes;
    }

    /**
     * Create a list of index Axes (grid axes)
     *
     * @param coverageMetadata
     * @return
     */
    private List<IndexAxis> createIndexAxes(LegacyCoverageMetadata coverageMetadata) throws Exception {
        List<IndexAxis> indexAxes = new ArrayList<>();

        for (LegacyDomainElement domainElement : coverageMetadata.getDomainList()) {
            LegacyCellDomainElement cellDomainElement = coverageMetadata.getCellDomainByOrder(domainElement.getOrder());
            IndexAxis indexAxis = new IndexAxis();

            // NOTE: it is better to kept geoAxis and indexAxis (grid axis) has same name as gridAxis is just a convenient label (e.g: i, j, k) 
            // Then, we could get the indexAxis by geoAxis name
            indexAxis.setAxisLabel(domainElement.getLabel());
            // also  grid axis order is good to store
            indexAxis.setAxisOrder(domainElement.getOrder());
            indexAxis.setLowerBound(new Long(cellDomainElement.getLo()));
            indexAxis.setUpperBound(new Long(cellDomainElement.getHi()));
            indexAxis.setUomLabel(IndexAxis.UOM_LABEL);
            // e.g: coverage with 2 Index axes (then CRS is Index2D)
            indexAxis.setSrsName(domainElement.getNativeCrs());

            indexAxes.add(indexAxis);
        }

        return indexAxes;
    }

    /**
     * Create GridLimits object from indexAxes
     *
     * @param indexAxes
     * @return
     */
    private GridLimits createGridLimits(List<IndexAxis> indexAxes) {

        int dimensions = indexAxes.size();
        // Create IndexCRS (indexND) from the number of axes (2 axes -> Index2D)
        String indexCrs = CrsUtil.OPENGIS_INDEX_URI.replace("$N", String.valueOf(dimensions));

        GridLimits gridLimits = new GridLimits();
        gridLimits.setIndexAxes(indexAxes);
        gridLimits.setSrsName(indexCrs);

        return gridLimits;
    }

    /**
     * Create a general grid for DomainSet object
     *
     * @return
     */
    private GeneralGrid createGeneralGrid(LegacyCoverageMetadata coverageMetadata, List<GeoAxis> geoAxes, GridLimits gridLimits) throws Exception {
        GeneralGrid generalGrid = new GeneralGrid();

        generalGrid.setSrsName(coverageMetadata.getCompoundCrs());
        generalGrid.setGeoAxes(geoAxes);
        generalGrid.setGridLimits(gridLimits);

        return generalGrid;
    }
}
