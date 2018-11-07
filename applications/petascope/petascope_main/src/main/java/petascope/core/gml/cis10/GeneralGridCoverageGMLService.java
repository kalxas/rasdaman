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
package petascope.core.gml.cis10;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.rasdaman.domain.cis.Axis;
import org.rasdaman.domain.cis.AxisExtent;
import org.rasdaman.domain.cis.DataRecord;
import org.rasdaman.domain.cis.Envelope;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.domain.cis.Field;
import org.rasdaman.domain.cis.GeneralGrid;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeneralGridDomainSet;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.domain.cis.GridLimits;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.cis.IrregularAxis;
import org.rasdaman.domain.cis.RangeType;
import org.rasdaman.domain.cis.RegularAxis;
import petascope.util.CrsUtil;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.wcst.exceptions.WCSTLowHighDifferentSizes;
import petascope.wcst.exceptions.WCSTMissingBoundedBy;
import petascope.wcst.exceptions.WCSTMissingDomainSet;
import petascope.wcst.exceptions.WCSTMissingEnvelope;
import petascope.wcst.exceptions.WCSTMissingGridEnvelope;
import petascope.wcst.exceptions.WCSTMissingGridType;
import petascope.wcst.exceptions.WCSTMissingHigh;
import petascope.wcst.exceptions.WCSTMissingLimits;
import petascope.wcst.exceptions.WCSTMissingLow;
import petascope.wcst.exceptions.WCSTMissingLowerCorner;
import petascope.wcst.exceptions.WCSTMissingPoint;
import petascope.wcst.exceptions.WCSTMissingPos;
import petascope.wcst.exceptions.WCSTMissingUpperCorner;
import petascope.wcst.exceptions.WCSTUnsupportedCoverageTypeException;
import petascope.core.XMLSymbols;

/**
 *
 * Build a General Grid Coverage object from GML of CIS 1.0 coverages (used in wcst_import)
 *
 @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
public class GeneralGridCoverageGMLService {

    public GeneralGridCoverageGMLService() {

    }

    /**
     *
     * Build a General Grid Coverage object from string GMLCov
     *
     * @param gmlCoverageDocument
     * @return
     * @throws java.io.IOException
     * @throws nu.xom.ParsingException
     * @throws petascope.wcst.exceptions.WCSTUnsupportedCoverageTypeException
     * @throws petascope.wcst.exceptions.WCSTMissingLimits
     * @throws petascope.wcst.exceptions.WCSTMissingGridEnvelope
     * @throws petascope.wcst.exceptions.WCSTMissingLow
     * @throws petascope.wcst.exceptions.WCSTMissingHigh
     * @throws petascope.wcst.exceptions.WCSTLowHighDifferentSizes
     * @throws petascope.wcst.exceptions.WCSTMissingDomainSet
     * @throws petascope.wcst.exceptions.WCSTMissingGridType
     * @throws petascope.wcst.exceptions.WCSTMissingPoint
     * @throws petascope.wcst.exceptions.WCSTMissingPos
     * @throws petascope.wcst.exceptions.WCSTMissingBoundedBy
     * @throws petascope.wcst.exceptions.WCSTMissingEnvelope
     * @throws petascope.wcst.exceptions.WCSTMissingLowerCorner
     * @throws petascope.wcst.exceptions.WCSTMissingUpperCorner
     * @throws petascope.exceptions.SecoreException
     */
    public GeneralGridCoverage buildCoverage(Document gmlCoverageDocument) throws IOException, ParsingException,
            WCSTUnsupportedCoverageTypeException, WCSTMissingLimits, WCSTMissingGridEnvelope,
            WCSTMissingLow, WCSTMissingHigh, WCSTLowHighDifferentSizes, WCSTMissingDomainSet, WCSTMissingGridType, WCSTMissingPoint, WCSTMissingPos, PetascopeException, WCSTMissingBoundedBy, WCSTMissingEnvelope, WCSTMissingLowerCorner, WCSTMissingUpperCorner, SecoreException {

        GeneralGridCoverage coverage = new GeneralGridCoverage();
        Element rootElement = gmlCoverageDocument.getRootElement();

        // coverage id
        String coverageId = rootElement.getAttributeValue(XMLSymbols.ATT_ID, XMLSymbols.NAMESPACE_GML);
        coverage.setCoverageId(coverageId);
        // coverage type
        String coverageType = GMLParserService.parseCoverageType(rootElement);
        coverage.setCoverageType(coverageType);
        // coverage extra metadata
        String extraMetadata = GMLParserService.parseExtraMetadata(rootElement);
        coverage.setMetadata(extraMetadata.trim());
                
        // coverage compundCrs
        String srsName = GMLParserService.parseSrsName(rootElement);
        
        // + Build DomainSet element        
        Element domainSetElement = GMLParserService.parseDomainSet(rootElement);
        // from the domain set extract the grid type
        Element gridTypeElement = GMLParserService.parseGridType(domainSetElement);
        // Grid domains (cellDomainElements)
        List<IndexAxis> indexAxes = GMLParserService.parseIndexAxes(gridTypeElement);
        GridLimits gridLimits = this.createGridLimits(indexAxes);
        // Geo domains (DomainElements)
        List<GeoAxis> geoAxes = GMLParserService.parseGeoAxes(rootElement);
        // Build GeneralGrid object containing geoAxes, indexAxes and compound Crs from all geoAxes
        GeneralGrid generalGrid = this.createGeneralGrid(srsName, geoAxes, gridLimits);
        // coverage origin
        String origin = GMLParserService.parseCoverageOrigin(gridTypeElement);
        GeneralGridDomainSet domainSet = new GeneralGridDomainSet(generalGrid);
        domainSet.setOrigin(origin);
        // DomainSet object
        coverage.setDomainSet(domainSet);

        // + Build Envelope element which contains List<AxisExtent> from geoAxes
        EnvelopeByAxis envelopeByAxis = this.createEnvelopeByAxis(generalGrid);
        Envelope envelope = new Envelope();
        envelope.setEnvelopeByAxis(envelopeByAxis);
        coverage.setEnvelope(envelope);

        // + Build RangeType element
        List<Field> fields = GMLParserService.parseFields(rootElement);
        DataRecord dataRecord = new DataRecord();
        dataRecord.setFields(fields);
        // RangeType object
        RangeType rangeType = new RangeType();
        rangeType.setDataRecord(dataRecord);
        coverage.setRangeType(rangeType);

        return coverage;
    }

    /**
     *
     * Create the EnvelopeByAxis object from List of geoAxis which was parsed in
     * creating DomainElement object
     *
     * @param geoAxes
     * @return
     */
    private EnvelopeByAxis createEnvelopeByAxis(GeneralGrid generalGrid) throws PetascopeException, SecoreException {
        EnvelopeByAxis envelopeByAxis = new EnvelopeByAxis();
        envelopeByAxis.setSrsName(generalGrid.getSrsName());
        int numberOfDimensions = generalGrid.getGeoAxes().size();
        envelopeByAxis.setSrsDimension(numberOfDimensions);

        List<AxisExtent> axesExtent = new ArrayList<>();

        for (int i = 0; i < numberOfDimensions; i++) {
            // Regular, Irregular
            Axis axis = generalGrid.getGeoAxes().get(i);
            AxisExtent axisExtent = new AxisExtent();
            axisExtent.setAxisLabel(axis.getAxisLabel());
            axisExtent.setSrsName(axis.getSrsName());

            String lowerBound = null;
            String upperBound = null;
            BigDecimal resolution = null;
            String uom = null;
            if (axis.getClass().equals(RegularAxis.class)) {
                lowerBound = ((RegularAxis) axis).getLowerBound();
                upperBound = ((RegularAxis) axis).getUpperBound();
                resolution = ((RegularAxis) axis).getResolution();
                uom = ((RegularAxis) axis).getUomLabel();
            } else if (axis.getClass().equals(IrregularAxis.class)) {
                lowerBound = ((IrregularAxis) axis).getLowerBound();
                upperBound = ((IrregularAxis) axis).getUpperBound();
                resolution = ((IrregularAxis) axis).getResolution();
                uom = ((IrregularAxis) axis).getUomLabel();
            }

            axisExtent.setLowerBound(lowerBound);
            axisExtent.setUpperBound(upperBound);
            axisExtent.setResolution(resolution);
            axisExtent.setUomLabel(uom);

            axesExtent.add(axisExtent);
        }

        envelopeByAxis.setAxisExtents(axesExtent);
        // Create the envelope axis labels from the name of geo axes
        envelopeByAxis.setAxisLabels(envelopeByAxis.getAxisLabelsRepresentation());

        return envelopeByAxis;
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
    private GeneralGrid createGeneralGrid(String srsName, List<GeoAxis> geoAxes, GridLimits gridLimits) {
        GeneralGrid generalGrid = new GeneralGrid();

        generalGrid.setSrsName(srsName);
        generalGrid.setGeoAxes(geoAxes);
        generalGrid.setGridLimits(gridLimits);

        return generalGrid;
    }
}
