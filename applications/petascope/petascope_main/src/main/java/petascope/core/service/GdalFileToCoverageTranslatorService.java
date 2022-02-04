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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.osr.SpatialReference;
import org.rasdaman.domain.cis.Coverage;
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
import org.rasdaman.domain.cis.NilValue;
import org.rasdaman.domain.cis.Quantity;
import org.rasdaman.domain.cis.RangeType;
import org.rasdaman.domain.cis.RasdamanRangeSet;
import org.rasdaman.domain.cis.Uom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.AxisTypes;
import petascope.core.CrsDefinition;
import petascope.core.gml.cis10.GMLCIS10ParserService;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.util.CrsUtil;
import petascope.util.StringUtil;

/**
 * Given a 2D readable file by gdal, generates a RectifiedGrid coverage object
 * with regular X and Y axes from it.
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class GdalFileToCoverageTranslatorService {

    @Autowired
    private GMLCIS10ParserService gmlCIS10ParserService;

    private static final String FIELD_NAME_PREFIX = "band";
    private static final String DEFAUL_BAND_UOM = "10^0";

    // 2D geo-referenced coverage with regular axes
    private static final String COVERAGE_TYPE = "RectifiedGridCoverage";
    private static final String FILE_PATH_PLACE_HOLDER = "$FILE_PATH";
    private static final String RASDAMAN_COLLETION_NAME_TEMPLATE = "decode(<[0:0] 1c>, "
            + "\"GDAL\", "
            + "\"{\\\"filePaths\\\":[\\\"" + FILE_PATH_PLACE_HOLDER + "\\\"]}\")";
    
    // Only supports 2D geo-referenced formats
    private static final List<String> UNSUPPORTED_FORMATS = Arrays.asList("GRIB", "netCDF");

    /**
     * Translate a 2D gdal file to a Coverage object
     */
    public Coverage translate(String filePath) throws PetascopeException, SecoreException {
        Dataset dataset = gdal.Open(filePath);
        if (dataset == null) {
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    "Cannot decode input file '" + filePath + "' by gdal. Make sure this file is gdal readable.");
        }
        
        String mimeType = dataset.GetDriver().getShortName();
        if (this.UNSUPPORTED_FORMATS.contains(mimeType)) {
            throw new PetascopeException(ExceptionCode.NoApplicableCode, 
                    "MIME type of the uploaded file '" + filePath + "' is not supported. Given '" + mimeType + "'.");
        }

        // Try to detect the CRS of the input file first
        String wktCRS = dataset.GetProjection();
        SpatialReference spatialReference = new SpatialReference();
        spatialReference.ImportFromWkt(wktCRS);

        try {
            spatialReference.AutoIdentifyEPSG();
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError,
                    "Cannot detect EPSG CRS code from the input file '" + filePath + "' by gdal. Reason: " + ex.getMessage()
                    + "Hint: try to set the proper EPSG CRS to the file first.");
        }
        // e.g: 4326
        String epsgCode = spatialReference.GetAuthorityCode(null);
        String srsName = CrsUtil.getEPSGFullUriByCode(epsgCode);

        double[] gdalValues = dataset.GetGeoTransform();
        long gridWidth = dataset.GetRasterXSize();
        long gridHeight = dataset.GetRasterYSize();

        BigDecimal upperLeftGeoX = new BigDecimal(String.valueOf(gdalValues[0]));
        BigDecimal upperLeftGeoY = new BigDecimal(String.valueOf(gdalValues[3]));

        BigDecimal geoXResolution = new BigDecimal(String.valueOf(gdalValues[1]));
        BigDecimal geoYResolution = new BigDecimal(String.valueOf(gdalValues[5]));

        String coverageId = StringUtil.createTempCoverageId(filePath);
        Coverage coverage = this.buildCoverage(dataset, filePath, coverageId, srsName, gridWidth, gridHeight,
                upperLeftGeoX, upperLeftGeoY,
                geoXResolution, geoYResolution);
        return coverage;
    }

    private Coverage buildCoverage(Dataset dataset, String filePath, String coverageId, String srsName, long gridWidth, long gridHeight,
            BigDecimal upperLeftGeoX, BigDecimal upperLeftGeoY,
            BigDecimal geoXResolution, BigDecimal geoYResolution) throws PetascopeException, SecoreException {
        Coverage coverage = new GeneralGridCoverage();
        coverage.setCoverageId(coverageId);
        coverage.setCoverageType(COVERAGE_TYPE);

        GeneralGrid generalGrid = this.buildGeneralGrid(srsName, gridWidth, gridHeight,
                upperLeftGeoX, upperLeftGeoY,
                geoXResolution, geoYResolution);
        GeneralGridDomainSet domainSet = new GeneralGridDomainSet(generalGrid);
        coverage.setDomainSet(domainSet);

        EnvelopeByAxis envelopeByAxis = this.gmlCIS10ParserService.createEnvelopeByAxis(generalGrid);
        Envelope envelope = new Envelope();
        envelope.setEnvelopeByAxis(envelopeByAxis);
        coverage.setEnvelope(envelope);

        RangeType rangeType = this.buildRangeType(dataset);
        coverage.setRangeType(rangeType);

        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
        String decodeExpression = this.buildDecodeExpression(filePath);
        rasdamanRangeSet.setDecodeExpression(decodeExpression);
        coverage.setRasdamanRangeSet(rasdamanRangeSet);

        return coverage;
    }

    /**
     *
     * Create a decoded file path from a local file path. e.g:
     * decode(<[0:0] 1c>, "GDAL",
     * "{\"filePaths\":[\"mean_summer_airtemp.tif\"]}"))
     *
     *
     */
    private String buildDecodeExpression(String filePath) {
        String result = RASDAMAN_COLLETION_NAME_TEMPLATE.replace(FILE_PATH_PLACE_HOLDER, filePath);

        return result;
    }

    private GeneralGrid buildGeneralGrid(String srsName, long gridWidth, long gridHeight,
            BigDecimal geoLowerBoundX, BigDecimal geoUpperBoundY,
            BigDecimal geoXResolution, BigDecimal geoYResolution) throws PetascopeException, SecoreException {
        CrsDefinition crsDefinition = CrsUtil.getCrsDefinition(srsName);

        // e.g: EPSG:3857 - XY order
        String axisLabelX = crsDefinition.getAxesLabels().get(0);
        String axisLabelY = crsDefinition.getAxesLabels().get(1);
        String uomX = crsDefinition.getAxes().get(0).getUoM();
        String uomY = crsDefinition.getAxes().get(1).getUoM();

        if (!CrsUtil.isXYAxesOrder(srsName)) {
            // e.g: EPSG:4326 - YX order
            axisLabelX = crsDefinition.getAxesLabels().get(1);
            axisLabelY = crsDefinition.getAxesLabels().get(0);

            uomX = crsDefinition.getAxes().get(1).getUoM();
            uomY = crsDefinition.getAxes().get(0).getUoM();
        }

        // Grid domains
        List<IndexAxis> indexAxes = new ArrayList<>();
        IndexAxis indexAxisX = new IndexAxis(axisLabelX, 0L, gridWidth - 1, 0);
        IndexAxis indexAxisY = new IndexAxis(axisLabelY, 0L, gridHeight - 1, 1);
        indexAxes.add(indexAxisX);
        indexAxes.add(indexAxisY);
        GridLimits gridLimits = this.gmlCIS10ParserService.createGridLimits(indexAxes);

        // Geo domains                
        List<GeoAxis> geoAxes = new ArrayList<>();
        // positive axis resolution
        BigDecimal geoUpperBoundX = geoLowerBoundX.add(new BigDecimal(gridWidth).multiply(geoXResolution));
        GeoAxis geoAxisX = new GeoAxis(axisLabelX, uomX, srsName, geoLowerBoundX.toPlainString(), geoUpperBoundX.toPlainString(), 
                                       geoXResolution.toPlainString(), AxisTypes.X_AXIS);

        // negative axis resolution
        BigDecimal geoLowerBoundY = geoUpperBoundY.add(new BigDecimal(gridHeight).multiply(geoYResolution));
        GeoAxis geoAxisY = new GeoAxis(axisLabelY, uomY, srsName, geoLowerBoundY.toPlainString(), geoUpperBoundY.toPlainString(),
                                       geoYResolution.toPlainString(), AxisTypes.Y_AXIS);
        
        if (CrsUtil.isXYAxesOrder(srsName)) {
            // e.g: EPSG:3857 with XY order
            geoAxes.add(geoAxisX);
            geoAxes.add(geoAxisY);
        } else {
            // e.g: EPSG:4326 with XY order
            geoAxes.add(geoAxisY);
            geoAxes.add(geoAxisX);
        }

        GeneralGrid generalGrid = this.gmlCIS10ParserService.createGeneralGrid(srsName, geoAxes, gridLimits);
        return generalGrid;
    }

    private RangeType buildRangeType(Dataset dataset) {
        List<Field> fields = new ArrayList<>();

        int numberOfBands = dataset.getRasterCount();
        for (int i = 1; i <= numberOfBands; i++) {
            Band band = dataset.GetRasterBand(i);
            Double[] nullValues = {null};
            band.GetNoDataValue(nullValues);
            // e.g: -9999.0
            Double nullValue = nullValues[0];

            // e.g: Float32, Byte
            String dataType = gdal.GetDataTypeName(band.getDataType());

            String fieldName = FIELD_NAME_PREFIX + i;

            List<NilValue> nilValues = new ArrayList<>();
            if (nullValue != null) {
                // e.g: -9999
                nilValues.add(new NilValue(String.valueOf(nullValue), null));
            }
            Uom uom = new Uom(DEFAUL_BAND_UOM);

            Quantity quantity = new Quantity(null, null, null, nilValues, uom, dataType);
            Field field = new Field(fieldName, quantity);

            fields.add(field);
        }

        DataRecord dataRecord = new DataRecord();
        dataRecord.setFields(fields);
        // RangeType object
        RangeType rangeType = new RangeType();
        rangeType.setDataRecord(dataRecord);

        return rangeType;

    }

}
