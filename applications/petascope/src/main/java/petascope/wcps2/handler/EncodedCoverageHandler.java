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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.handler;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang3.StringUtils;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;
import petascope.wcps2.decodeparameters.model.NetCDFExtraParams;
import petascope.wcps2.decodeparameters.service.CovToCFTranslationService;
import petascope.wcps2.decodeparameters.service.NetCDFParametersFactory;
import petascope.wcps2.error.managed.processing.MetadataSerializationException;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.AxisDirection;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.result.WcpsResult;

import java.util.ArrayList;
import java.util.List;
import petascope.exceptions.WCSException;
import petascope.util.CrsProjectionUtil;
import petascope.wcps2.error.managed.processing.InvalidBoundingBoxInCrsTransformException;
import petascope.wcps2.error.managed.processing.NotGeoReferencedCoverageInCrsTransformException;
import petascope.wcps2.metadata.model.NumericTrimming;
import petascope.wcps2.metadata.service.CoverageRegistry;
import petascope.wcs2.extensions.FormatExtension;

/**
 * Class to translate an encoded coverage
 * <code>
 * encode($c, "image/png", "NODATA=0")
 * </code>
 * translates to
 * <code>
 * encode(c, "png", "NODATA=0")
 * </code>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class EncodedCoverageHandler {

    public static WcpsResult handle(WcpsResult coverageExpression, String format, List<String> otherParams, CoverageRegistry coverageRegistry) throws PetascopeException {
        String resultRasql, otherParamsString = "";

        if (otherParams == null) {
            otherParams = new ArrayList<String>();
        }

        //strip first part of the mime type because rasdaman encode function does not support proper mime types yet
        //e.g. image/png -> png; text/csv -> csv and so on.
        String extractedFormat = format.split("/").length > 1 ? ('"' + format.split("/")[1]) : format;

        // then get the Gdal code according to the extracted format (e.g: tiff -> GTiff).
        // NOTE: csv, dem does not exist in GDAL code
        String adaptedFormat = coverageRegistry.getMetadataSource().formatToGdalid(extractedFormat.replace("\"", ""));
        if (adaptedFormat == null) {
            adaptedFormat = extractedFormat.replace("\"", "");
        }

        adaptedFormat = adaptedFormat.toLowerCase();
        // set parameters for dem()
        if (adaptedFormat.equals(FormatExtension.DEM_ENCODING)) {
            // keep the arguments without need to calculate anything else
            otherParamsString = StringUtils.join(otherParams, ";");
        } else if (adaptedFormat.equals(FormatExtension.CSV_ENCODING)) {
            // csv()
            otherParamsString = "";
        } else if(adaptedFormat.equals(FormatExtension.NETCDF_ENCODING)){
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            XmlMapper xmlMapper = new XmlMapper();
            CovToCFTranslationService covToCFTranslationService = new CovToCFTranslationService();

            //netcdf
            NetCDFParametersFactory netCDFParametersFactory = new NetCDFParametersFactory(xmlMapper, objectMapper,
                    coverageRegistry.getMetadataSource(), covToCFTranslationService);
            NetCDFExtraParams netCDFExtraParams = netCDFParametersFactory.getParameters(coverageExpression.getMetadata());
            //serialize as json
            //for now ignore the extra params, in the future we need to merge them with the one deduced from the metadata
            try {
                otherParamsString = ", \"" + objectMapper.writeValueAsString(netCDFExtraParams).replace("\"", "\\\"") + "\"";
            } catch (JsonProcessingException e) {
                throw new MetadataSerializationException();
            }
        } else {
            // get all the output parameters to encode
            otherParams.addAll(getExtraParams(coverageExpression.getMetadata()));
            otherParamsString = ", \"" + StringUtils.join(otherParams, ";").replace("\"", "") + "\"";
        }

        //get the right template
        String template = getTemplate(adaptedFormat);
        resultRasql = template.replace("$arrayOps", coverageExpression.getRasql())
                              .replace("$format", '"' + adaptedFormat + '"')
                              .replace("$otherParams", otherParamsString);
        WcpsResult result = new WcpsResult(coverageExpression.getMetadata(), resultRasql);
        return result;
    }

    /**
     * Adds the required extra params (if grid coverage then not set crs= param)
     */
    private static List<String> getExtraParams(WcpsCoverageMetadata metadata) {
        // NOTE: if metadata is null (e.g: range of scalar values encode({red: 0, green: 0, blue: 0}, "png") then just return empty list)
        List<String> result = new ArrayList();
        if (metadata == null) {
            return result;
        }
        List<String> bbox = getBoundingBox(metadata);
        // check if should set bounding box or not (in case of mixing 1 axis is Time, 1 axis is Lat)
        // then it should leave empty bounding box as it will set the grid CRS by default.
        if (!isSetBoundingBox) {
            return result;
        }
        String xMin = bbox.get(0);
        String yMin = bbox.get(1);
        String xMax = bbox.get(2);
        String yMax = bbox.get(3);

        // If outputCrs is not null then should set encode in this crs
        // NOTE: crsUri can be compoundCrs (e.g: irr_cube_2), need to get only the geo-referenced axis Crs (EPSG:32633)
        String crsUri = "";
        for (Axis axis:metadata.getXYAxes()) {
            crsUri = axis.getCrsUri();
            // e.g: scale(c, {Lat:"CRS:1"(0:20), Long:"CRS:1"(0:50) when CRS of c is 4326 then it must use as outputCRS, instead of CRS:1
            if (CrsUtil.isGridCrs(crsUri) || CrsUtil.isIndexCrs(crsUri)) {
                crsUri = metadata.getCrsUri();
            }
            break;
        }

        String outputCrsUri = "";
        if (metadata.getOutputCrsUri() != null) {
            // NOTE: not allow to transform from CRS:1 or IndexND to a geo-referenced CRS
            if (CrsUtil.isGridCrs(crsUri) || CrsUtil.isIndexCrs(crsUri)) {
                throw new NotGeoReferencedCoverageInCrsTransformException();
            }
            outputCrsUri = metadata.getOutputCrsUri();
            // Also need to convert the bounding box (xmin,ymin,xmax,ymax) to outputCrsUri
            double[] srcCoords = new double[] { Double.parseDouble(xMin), Double.parseDouble(yMin),
                Double.parseDouble(xMax), Double.parseDouble(yMax) };
                List<BigDecimal> transformedBBox;
            try {
                // get the transformed coordinate for the bouding box
                transformedBBox = CrsProjectionUtil.transformBoundingBox(crsUri, outputCrsUri, srcCoords);
                xMin = transformedBBox.get(0).toPlainString();
                yMin = transformedBBox.get(1).toPlainString();
                xMax = transformedBBox.get(2).toPlainString();
                yMax = transformedBBox.get(3).toPlainString();

            } catch (WCSException ex) {
                String bboxStr = "xmin=" + xMin + "," + "ymin=" + yMin + ","
                               + "xmax=" + xMax + "," + "ymax=" + yMax;
                throw new InvalidBoundingBoxInCrsTransformException(bboxStr, outputCrsUri, ex.getMessage());
            }
        } else {
            // no crsTransform() then outputCrs is crsUri
            outputCrsUri = crsUri;
        }

        // We don't set EPSG CRS or bounding box when outputCrs is CRS:1 or IndexND
        // or the output CRS is different for X and Y axis
        if (CrsUtil.isGridCrs(outputCrsUri) || CrsUtil.isIndexCrs(outputCrsUri) || !isSameOutputCrs) {
            return result;
        }

        // e.g: http://opengis.net/def/crs/epsg/0/4326 -> epsg:4326
        String crs = CrsUtil.CrsUri.getAuthorityCode(outputCrsUri);

        // add the params (e.g: xmin=0,xmax=10,ymin=20,ymax=25,crs=epsg:4326)
        for (String param: EXTRA_PARAMS) {
            result.add(param.replace("$xmin", xMin)
                            .replace("$ymin", yMin)
                            .replace("$xmax", xMax)
                            .replace("$ymax", yMax));
        }

        // NOTE: crs here can be like: crs=OGC:AnsiDate?axis-label="time"
        // it is not valid in Rasql then need to replace "" in the parameters as well
        crs = crs.replace("\"", "");
        result.add("crs=" + crs);

        return result;
    }

    /**
     * Return the List of bounding box values (xmin,ymin,xmax,ymax)
     */
    private static List<String> getBoundingBox(WcpsCoverageMetadata metadata) {
       String xMin = "", yMin = "", xMax = "", yMax = "";

       List<String> bbox = new ArrayList<String>();
       int i = 0;

       // NOTE: axis to get bounding box is trimming and only has 2 axes for making bounding box.
       // If no output axes (e.g: no scale($c, {}) or extend($c, {}))
       if (metadata.getAxesBBox().isEmpty()) {
            // Get the calculated bounding box from the coverage
            for (Axis axis: metadata.getAxes()) {
                if (axis.getGeoBounds() instanceof NumericTrimming) {
                    if (axis.getDirection().equals(AxisDirection.EASTING)){
                        xMin = ((NumericTrimming)axis.getGeoBounds()).getLowerLimit().toString();
                        xMax = ((NumericTrimming)axis.getGeoBounds()).getUpperLimit().toString();
                    }
                    if (axis.getDirection().equals(AxisDirection.NORTHING)){
                        yMin = ((NumericTrimming)axis.getGeoBounds()).getLowerLimit().toString();
                        yMax = ((NumericTrimming)axis.getGeoBounds()).getUpperLimit().toString();
                    }
                    i++;
                    if (i == 2) {
                        break;
                    }
                }
            }
       } else {
            // Get the bounding box from extend() or scale()
            String xCrsUri = "";
            String yCrsUri = "";

            for (Axis axis: metadata.getAxesBBox()){
                if (axis.getGeoBounds() instanceof NumericTrimming) {
                    if (axis.getDirection().equals(AxisDirection.EASTING)){
                        xMin = ((NumericTrimming)axis.getGeoBounds()).toString();
                        xMax = ((NumericTrimming)axis.getGeoBounds()).toString();
                        xCrsUri = axis.getCrsUri();
                    }
                    if (axis.getDirection().equals(AxisDirection.NORTHING)){
                        yMin = ((NumericTrimming)axis.getGeoBounds()).toString();
                        yMax = ((NumericTrimming)axis.getGeoBounds()).toString();
                        yCrsUri = axis.getCrsUri();
                    }
                }
                i++;
                if (i == 2) {
                    break;
                }
            }

            // Check if X and Y has same crs, if not then don't set crs to Rasql
            String xCrsUriCode = CrsUtil.CrsUri.getCode(xCrsUri);
            String yCrsUriCode = CrsUtil.CrsUri.getCode(yCrsUri);

            if (!xCrsUriCode.equals(yCrsUriCode)) {
                isSameOutputCrs = false;
            }
       }

       bbox.add(xMin);
       bbox.add(yMin);
       bbox.add(xMax);
       bbox.add(yMax);

       isSetBoundingBox = true;

       // Check if the coverage can be set bounding box (e.g: geo-reference crs)
       for (String str : bbox) {
           if (str.equals("")) {
               isSetBoundingBox = false;
               break;
           }
       }

       return bbox;
    }


    /**
     * Rerturns the right template, depending on the operation that ahs been executed.
     * @param operation
     * @return
     */
    private static String getTemplate(String operation){
        if (operation.toLowerCase().replaceAll("\"", "").equals(DEM_OPERATION)){
            return NON_GDAL_OPERATION_TEMPLATE
                    .replace("$operation", DEM_OPERATION);
        }
        return TEMPLATE;
    }

    // otherParams can be empty then no add "," after $format
    private final static String TEMPLATE = "encode($arrayOps, $format $otherParams)";
    // in case of (scale($c, {}) or extend($c, {})) with domain intervals { (t(0:5), Lat(0:20) }
    // then it is different crs and don't need to set crs to Rasql
    private static boolean isSameOutputCrs = true;
    // check if coverage has 2 axis in grid or geo-referenced Crs
    // e.g: (in case of scale( {imageCrsdomain(c[t(0:50), Long(0:20), Lat(0)] }) the value is false and cannot set bounding box in geo-referenced CRS
    // should leave it empty as normal grid bounding box
    private static boolean isSetBoundingBox = true;

    private final static List<String> EXTRA_PARAMS = new ArrayList<String>(5);
    static {
        EXTRA_PARAMS.add("xmin=$xmin");
        EXTRA_PARAMS.add("ymin=$ymin");
        EXTRA_PARAMS.add("xmax=$xmax");
        EXTRA_PARAMS.add("ymax=$ymax");
        // handle crs=$crs later
    }

    // e.g: dem(): for a in (mr) return encode(a[ i(0:100), j(0:100) ],
    //      "dem", "startx=0,starty=0,endx=100,endy=100,resx=0.1222,resy=0.15")
    private final static String NON_GDAL_OPERATION_TEMPLATE = "$operation($arrayOps, $otherParams)";
    private final static String DEM_OPERATION = "dem";
    private final static List<String> DEM_EXTRA_PARAMS = new ArrayList<String>(6);
    static {
        DEM_EXTRA_PARAMS.add("startX=$startX");
        DEM_EXTRA_PARAMS.add("startY=$starY");
        DEM_EXTRA_PARAMS.add("endX=$endX");
        DEM_EXTRA_PARAMS.add("endY=$endY");
        DEM_EXTRA_PARAMS.add("resX=$resX");
        DEM_EXTRA_PARAMS.add("resY=$resY");
    }
}
