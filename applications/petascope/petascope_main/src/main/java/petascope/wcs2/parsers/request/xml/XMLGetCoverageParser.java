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
package petascope.wcs2.parsers.request.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import nu.xom.Element;
import nu.xom.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_COVERAGEID;
import petascope.util.ListUtil;
import petascope.core.XMLSymbols;
import petascope.core.gml.GMLGetCapabilitiesBuilder;
import petascope.exceptions.PetascopeException;
import petascope.util.XMLUtil;
import petascope.wcs2.parsers.subsets.AbstractSubsetDimension;
import petascope.wcs2.parsers.subsets.SlicingSubsetDimension;
import petascope.wcs2.parsers.subsets.TrimmingSubsetDimension;

/**
 * Parse a WCS GetCoverage from request body in XML to map of keys, values as
 * KVP request
 *
 @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class XMLGetCoverageParser extends XMLAbstractParser {

    @Override
    public Map<String, String[]> parse(String requestBody) throws WCSException, PetascopeException {
        // Only used when xml_validation=true in petascope.properties
        this.validateXMLRequestBody(requestBody);
        
        Element rootElement = XMLUtil.parseInput(requestBody);
        // e.g: <wcs:GetCoverage ... version="2.0.1">...</wcs:GetCoverage>        
        String version = rootElement.getAttributeValue(XMLSymbols.ATT_VERSION);
        this.validateRequestVersion(version);
        
        kvpParameters.put(KVPSymbols.KEY_SERVICE, new String[]{KVPSymbols.WCS_SERVICE});
        kvpParameters.put(KVPSymbols.KEY_VERSION, new String[]{version});
        kvpParameters.put(KVPSymbols.KEY_REQUEST, new String[]{KVPSymbols.VALUE_DESCRIBE_COVERAGE});

        // parse the coverageId
        this.parseCoverageId(rootElement);

        // parse the request format (e.g: image/tiff, application/gml+xml, image/png,...)
        this.parseFormatType(rootElement);

        // parse the mediaType (i.e: mediaType=multipart/related) which returns 1 result in GML and 1 result in requested format (multipart).        
        this.parseMediaType(rootElement);

        // parse the subset elements
        this.parseSubset(rootElement);

        Element extensionElement = XMLUtil.firstChild(rootElement, XMLSymbols.LABEL_EXTENSION);
        if (extensionElement != null) {
            // parse the range elements extension
            this.parseRangeSubsetExtension(extensionElement);
            // parse the interpolation extension
            this.parseInterpolationExtension(extensionElement);
            // parse the scale extension
            this.parseScaleExtesion(extensionElement);
        }

        // parse subsets extension
        return kvpParameters;
    }

    /**
     * Parse the coverageId from WCS request body
     *
     * @param rootElement
     * @throws WCSException
     */
    private void parseCoverageId(Element rootElement) throws WCSException {
        // parse the coverageId        
        List<Element> coverageIdElements = XMLUtil.getChildElements(rootElement, XMLSymbols.LABEL_COVERAGE_ID);
        if (coverageIdElements.isEmpty()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "A GetCoverage request must specify at least one " + KEY_COVERAGEID + ".");
        }

        List<String> coverageIds = new ArrayList<>();
        for (Element coverageIdElement : coverageIdElements) {
            // e.g: <wcs:CoverageId>test_mr</wcs:CoverageId> return test_mr
            coverageIds.add(XMLUtil.getText(coverageIdElement));
        }
        String coverageId = ListUtil.join(coverageIds, ",");
        kvpParameters.put(KVPSymbols.KEY_COVERAGEID, new String[]{coverageId});
    }

    /**
     * Parse the format from WCS request body
     *
     * @param rootElement
     * @throws WCSException
     */
    private void parseFormatType(Element rootElement) {
        Element formatElement = XMLUtil.firstChildRecursive(rootElement, XMLSymbols.LABEL_FORMAT);
        if (formatElement != null) {
            kvpParameters.put(KVPSymbols.KEY_FORMAT, new String[]{XMLUtil.getText(formatElement)});
        }
    }

    /**
     * Parse the mediaType from WCS request body
     *
     * @param rootElement
     */
    private void parseMediaType(Element rootElement) {
        Element mediaTypeElement = XMLUtil.firstChildRecursive(rootElement, XMLSymbols.LABEL_MEDIATYPE);
        if (mediaTypeElement != null) {
            kvpParameters.put(KVPSymbols.KEY_MEDIATYPE, new String[]{XMLUtil.getText(mediaTypeElement)});
        }
    }

    /**
     * Parse the subset from WCS request body e.g:
     * <wcs:DimensionTrim>
     * <wcs:Dimension>Lat</wcs:Dimension>
     * <wcs:TrimLow>10</wcs:TrimLow>
     * <wcs:TrimHigh>20</wcs:TrimHigh>
     * </wcs:DimensionTrim>
     * <wcs:DimensionSlice>
     * <wcs:Dimension>Long</wcs:Dimension>
     * <wcs:SlicePoint>30</wcs:SlicePoint>
     * </wcs:DimensionSlice>
     *
     * and it is the same as KVP request: subset=Lat(10,20)&subset=Long(30)
     *
     * @param rootElement
     */
    private void parseSubset(Element rootElement) {
        List<Element> childElements = XMLUtil.getChildElements(rootElement);
        List<AbstractSubsetDimension> subsetDimensions = new ArrayList<>();
        for (Element childElement : childElements) {
            String elementName = childElement.getLocalName();
            // Trimming on dimension
            if (elementName.equalsIgnoreCase(XMLSymbols.LABEL_DIMENSION_TRIM)) {
                Element dimensionElement = XMLUtil.firstChild(childElement, XMLSymbols.LABEL_DIMENSION);
                String dimensionName = XMLUtil.getText(dimensionElement);

                Element trimLowElement = XMLUtil.firstChild(childElement, XMLSymbols.LABEL_TRIM_LOW);
                String lowerBound = XMLUtil.getText(trimLowElement);

                Element trimHighElement = XMLUtil.firstChild(childElement, XMLSymbols.LABEL_TRIM_HIGH);
                String upperBound = XMLUtil.getText(trimHighElement);

                TrimmingSubsetDimension trimmingDimension = new TrimmingSubsetDimension(dimensionName, lowerBound, upperBound);
                subsetDimensions.add(trimmingDimension);
            } else if (elementName.equalsIgnoreCase(XMLSymbols.LABEL_DIMENSION_SLICE)) {
                // Slicing on dimension
                Element dimensionElement = XMLUtil.firstChild(childElement, XMLSymbols.LABEL_DIMENSION);
                String dimensionName = XMLUtil.getText(dimensionElement);

                Element slicePointElement = XMLUtil.firstChild(childElement, XMLSymbols.LABEL_SLICE_POINT);
                String bound = XMLUtil.getText(slicePointElement);

                SlicingSubsetDimension slicingDimension = new SlicingSubsetDimension(dimensionName, bound);
                subsetDimensions.add(slicingDimension);
            }
        }

        if (subsetDimensions.size() > 0) {
            String[] subsets = new String[subsetDimensions.size()];
            int i = 0;
            // NOTE: in GetCoverage POST XML, crs parameter is not included inside Trim or Slice elements
            // so the output of subset in KVP is only like: i(20,30) for trimming or Lat(2.3434) for slicing
            for (AbstractSubsetDimension subsetDimension : subsetDimensions) {
                subsets[i] = subsetDimension.toString();
                i++;
            }
            kvpParameters.put(KVPSymbols.KEY_SUBSET, subsets);
        }
    }

    /**
     * Parse the range extension from WCS request body, e.g: select band1,
     * band3,band4,band5 (band3:band5) from the coverage
     * <rsub:RangeSubset>
     * <rsub:RangeItem>
     * <rsub:RangeComponent>band1</rsub:RangeComponent>
     * </rsub:RangeItem>
     * <rsub:RangeItem>
     * <rsub:RangeInterval>
     * <rsub:startComponent>band3</rsub:startComponent>
     * <rsub:endComponent>band5</rsub:endComponent>
     * </rsub:RangeInterval>
     * </rsub:RangeItem>
     * </rsub:RangeSubset>
     *
     * @param extensionElement
     * @throws WCSException
     */
    private void parseRangeSubsetExtension(Element extensionElement) throws WCSException {
        Element rangeSubsetElement = XMLUtil.firstChild(extensionElement, XMLSymbols.LABEL_RANGE_SUBSET);
        // Only when RangeSubset element exist
        if (rangeSubsetElement != null) {
            List<String> rangeSubsets = new ArrayList<>();

            List<Element> rangeItemElements = XMLUtil.getChildElements(rangeSubsetElement, XMLSymbols.LABEL_RANGE_ITEM);
            for (Element rangeItemElement : rangeItemElements) {
                // RangeItem can contain 1 RangeComponent for 1 band or an interval of ranges (startComponent:endComponent)
                Element childElement = rangeItemElement.getChildElements().get(0);
                // <rsub:RangeComponent>band1</rsub:RangeComponent>
                if (childElement.getLocalName().equalsIgnoreCase(XMLSymbols.LABEL_RANGE_COMPONENT)) {
                    String rangeName = XMLUtil.getText(childElement);

                    rangeSubsets.add(rangeName);
                } else if (childElement.getLocalName().equalsIgnoreCase(XMLSymbols.LABEL_RANGE_INTERVAL)) {
                    Element startComponentElement = XMLUtil.firstChild(childElement, XMLSymbols.LABEL_START_COMPONENT);
                    Element endComponentElement = XMLUtil.firstChild(childElement, XMLSymbols.LABEL_END_COMPONENT);

                    if (startComponentElement == null || endComponentElement == null) {
                        throw new WCSException(ExceptionCode.InvalidRequest,
                                "A RangeInterval element needs to have exactly one startComponent and one endComponent");
                    }

                    String startRangeName = XMLUtil.getText(startComponentElement);
                    String endRangeName = XMLUtil.getText(endComponentElement);

                    rangeSubsets.add(startRangeName + ":" + endRangeName);
                }
            }

            // Add rangeSubsets as a KVP parameter
            String rangeSubsetParam = ListUtil.join(rangeSubsets, ",");
            kvpParameters.put(KVPSymbols.KEY_RANGESUBSET, new String[]{rangeSubsetParam});
        }
    }

    /**
     * Parse the interpolation extension from WCS request body, e.g:
     * <int:Interpolation>
     * <int:globalInterpolation>http://www.opengis.net/def/interpolation/OGC/0/nearest-neighbor</int:globalInterpolation>
     * </int:Interpolation>
     * NOTE: only support globalInterpolation now
     *
     * @param extensionElement
     */
    private void parseInterpolationExtension(Element extensionElement) throws WCSException {        
        Element interpolationElement = XMLUtil.firstChild(extensionElement, XMLSymbols.LABEL_INTERPOLATION);
        if (interpolationElement != null) {
            // (interpolation-per-axis is not supported currently)        
            Elements childElements = interpolationElement.getChildElements();
            if (childElements.size() != 1) {
                throw new WCSException(ExceptionCode.InvalidRequest,
                        "One <" + XMLSymbols.LABEL_GLOBAL_INTERPOLATION + "> element is required inside an <"
                        + XMLSymbols.LABEL_INTERPOLATION + "> element.");
            } else {
                Element globalInterpolationElement = childElements.get(0);
                String interpolationValue = globalInterpolationElement.getValue();
                if (!GMLGetCapabilitiesBuilder.SUPPORTED_INTERPOLATIONS.contains(interpolationValue)) {
                    throw new WCSException(ExceptionCode.InterpolationMethodNotSupported, "Received interpolation URL: " + interpolationValue + " is not supported.");
                } else {
                    // as the globalInterpolation URI as a KVP parameter
                    kvpParameters.put(KVPSymbols.KEY_INTERPOLATION, new String[]{interpolationValue});
                }
            }
        }
    }

    /**
     * Parse the scale extension from WCS request body, it has 4 types of scale:
     * scaleFactor, scaleAxes, scaleSize, scaleExtent, e.g:
     * <scal:Scaling>...</scal:Scaling>
     *
     * @param extensionElement
     * @throws WCSException
     */
    private void parseScaleExtesion(Element extensionElement) throws WCSException {
        Element scalingElement = XMLUtil.firstChild(extensionElement, XMLSymbols.LABEL_SCALING);
        if (scalingElement != null) {
            Element firstChildElement = scalingElement.getChildElements().get(0);
            String scaleTypeName = firstChildElement.getLocalName();
            if (scaleTypeName.equalsIgnoreCase(XMLSymbols.LABEL_SCALEBYFACTOR)) {
                this.parseScaleByFactor(scalingElement);
            } else if (scaleTypeName.equalsIgnoreCase(XMLSymbols.LABEL_SCALEAXESBYFACTOR)) {
                this.parseScaleAxesByFactor(scalingElement);
            } else if (scaleTypeName.equalsIgnoreCase(XMLSymbols.LABEL_SCALETOSIZE)) {
                this.parseScaleToSize(scalingElement);
            } else if (scaleTypeName.equalsIgnoreCase(XMLSymbols.LABEL_SCALETOEXTENT)) {
                this.parseScaleToExtent(scalingElement);
            }
        }
    }

    /**
     * Parse a scaling extension when it is a scaleFactor request, e.g:
     * <scal:ScaleByFactor>
     * <scal:scaleFactor>0.5</scal:scaleFactor>
     * </scal:ScaleByFactor>
     * So all the grid axes's grid pixels will divide by 0.5
     *
     * @param scalingElement
     */
    private void parseScaleByFactor(Element scalingElement) {
        Element scaleByFactorElement = scalingElement.getChildElements().get(0);
        Element scaleFactorElement = scaleByFactorElement.getChildElements().get(0);
        String scaleFactor = scaleFactorElement.getValue();

        kvpParameters.put(KVPSymbols.KEY_SCALEFACTOR, new String[]{scaleFactor});
    }

    /**
     * Parse a scaling extension when it is a scaleAxes request, e.g:
     * <scal:ScaleAxesByFactor>
     * <scal:ScaleAxis>
     * <scal:axis>i</scal:axis>
     * <scal:scaleFactor>0.02</scal:scaleFactor>
     * </scal:ScaleAxis>
     * </scal:ScaleAxesByFactor>
     * So the mentioned axis's grid pixels will divide by 0.02, unmentioned axes
     * are not changed.
     *
     * @param scalingElement
     */
    private void parseScaleAxesByFactor(Element scalingElement) {
        Element scaleAxesByFactorElement = scalingElement.getChildElements().get(0);
        List<Element> scaleAxisElements = XMLUtil.getChildElements(scaleAxesByFactorElement);
        List<String> scaleParams = new ArrayList<>();
        for (Element scaleAxisElement : scaleAxisElements) {
            Element axisElement = XMLUtil.firstChild(scaleAxisElement, XMLSymbols.LABEL_AXIS);
            Element scaleFactorElement = XMLUtil.firstChild(scaleAxisElement, XMLSymbols.LABEL_SCALEFACTOR);

            String axisName = axisElement.getValue();
            String scaleFactor = scaleFactorElement.getValue();
            String scaleParam = axisName + "(" + scaleFactor + ")";

            scaleParams.add(scaleParam);
        }

        // e.g: i(0.5), j(0.25),...
        String[] scaleParamValues = scaleParams.toArray(new String[0]);

        // add the scale params to the KVP request
        kvpParameters.put(KVPSymbols.KEY_SCALEAXES, scaleParamValues);
    }

    /**
     * Parse a scaling extension when it is a scaleSize request, e.g:
     * <scal:ScaleToSize>
     * <scal:TargetAxisSize>
     * <scal:axis>j</scal:axis>
     * <scal:targetSize>16</scal:targetSize>
     * </scal:TargetAxisSize>
     * </scal:ScaleToSize>
     * So the mentioned axis's grid pixels will be set to 16, unmentioned axes
     * are not changed.
     *
     * @param scalingElement
     */
    private void parseScaleToSize(Element scalingElement) throws WCSException {
        Element scaleToSizeElement = scalingElement.getChildElements().get(0);
        List<Element> targetAxisSizeElements = XMLUtil.getChildElements(scaleToSizeElement);
        List<String> scaleParams = new ArrayList<>();

        for (Element targetAxisSizeElement : targetAxisSizeElements) {
            Element axisElement = XMLUtil.firstChild(targetAxisSizeElement, XMLSymbols.LABEL_AXIS);
            Element targetSizeElement = XMLUtil.firstChild(targetAxisSizeElement, XMLSymbols.LABEL_TARGETSIZE);

            String axisName = axisElement.getValue();
            String targetSizeName = targetSizeElement.getValue();

            String scaleParam = axisName + "(" + targetSizeName + ")";
            scaleParams.add(scaleParam);
        }

        // e.g: i(20), j(25),...
        String[] scaleParamValues = scaleParams.toArray(new String[0]);

        // add the scale params to the KVP request
        kvpParameters.put(KVPSymbols.KEY_SCALESIZE, scaleParamValues);
    }

    /**
     * Parse a scaling extension when it is a scaleExtent request, e.g:
     * <scal:ScaleToExtent>
     * <scal:TargetAxisExtent>
     * <scal:axis>j</scal:axis>
     * <scal:low>16</scal:low>
     * <scal:high>34</scal:high>
     * </scal:TargetAxisExtent>
     * </scal:ScaleToExtent>
     * So the mentioned axis's grid pixels will be set to (16,34), unmentioned
     * axes are not changed.
     *
     * @param scalingElement
     */
    private void parseScaleToExtent(Element scalingElement) {
        Element scaleToExtentElement = scalingElement.getChildElements().get(0);
        List<Element> targetAxisExtentElements = XMLUtil.getChildElements(scaleToExtentElement);
        List<String> scaleParams = new ArrayList<>();

        for (Element targetAxisExtentElement : targetAxisExtentElements) {
            Element axisElement = XMLUtil.firstChild(targetAxisExtentElement, XMLSymbols.LABEL_AXIS);
            Element lowElement = XMLUtil.firstChild(targetAxisExtentElement, XMLSymbols.LABEL_LOW);
            Element highElement = XMLUtil.firstChild(targetAxisExtentElement, XMLSymbols.LABEL_HIGH);

            String axisName = axisElement.getValue();
            String lowerBound = lowElement.getValue();
            String upperBound = highElement.getValue();

            // NOTE: although subset is separated as WCS, e.g: subset=Lat(0,20),
            // but this scaleExtent is separated as WCPS, e.g: scaleExtent=Lat(0:20)
            String scaleParam = axisName + "(" + lowerBound + ":" + upperBound + ")";
            scaleParams.add(scaleParam);
        }

        // e.g: i(0:1), j(0:20),...
        String[] scaleParamValues = scaleParams.toArray(new String[0]);

        // add the scale params to the KVP request
        kvpParameters.put(KVPSymbols.KEY_SCALEEXTENT, scaleParamValues);
    }
}
