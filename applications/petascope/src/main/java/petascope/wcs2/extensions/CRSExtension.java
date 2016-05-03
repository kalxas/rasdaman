// enterprise start
package petascope.wcs2.extensions;

import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import static petascope.util.AxisTypes.ELEV_AXIS;
import static petascope.util.AxisTypes.X_AXIS;
import static petascope.util.AxisTypes.Y_AXIS;
import petascope.util.CrsUtil;
import petascope.util.WcsUtil;
import petascope.wcps.metadata.Bbox;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;
import petascope.wcs2.parsers.subsets.DimensionSlice;
import petascope.wcs2.parsers.subsets.DimensionSubset;
import petascope.wcs2.parsers.subsets.DimensionTrim;

/**
 * Manage CRS Extension (OGC 11-053).
 *
 * @author <a href="mailto:cmppri@unife.it">Piero Campalani</a>
 * @author Dimitar Misev
 */
public class CRSExtension implements Extension {

    private static final Logger log = LoggerFactory.getLogger(CRSExtension.class);
    public static final String REST_SUBSETTING_PARAM = "subsettingcrs";
    public static final String REST_OUTPUT_PARAM = "outputcrs";

    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.CRS_IDENTIFIER;
    }

    /**
     * Method for the handling of possible subsets in case of subsettingCrs not
     * corrispondent to the one which the desired collection is natively stored.
     *
     * @param request The WCS request, which is directly modified.
     * @param meta Metadata of the underlying petascopedb.
     *
     */
    protected void handle(GetCoverageRequest request, GetCoverageMetadata m, DbMetadataSource meta) throws WCSException {

        // determine spatial bbox
        Bbox bbox = m.getBbox();
        GetCoverageRequest.CrsExt crsExt = request.getCrsExt();

        /**
         * NOTE1 (campalani): CRS transform cannot be done when only easting or
         * only northing is known, so it must be done *outside*
         * DimensionIntervalElement. Translation to pixel indices is moved
         * outside DimensionInterval as well since it must be computed a
         * posteriori of (possible) CRS transforms. NOTE2 (campalani): at the
         * same time, CRS is read only inside DimensionInterval due to the
         * syntax of WCPS language: trim(x:<crs>(xMin,xMax),y:<crs>(yMin,yMax).
         * TODO: need to enable 3D spatial transform (x,y,elev): *Bbox* now is
         * fixed to 2D but need to include elevation in case of 3D collections.
         */
        // Check if a CRS transform is needed for X and Y axis.
        if (bbox != null && !bbox.getCrsName().equals(CrsUtil.GRID_CRS)) {
            if (request.getSubset(X_AXIS) == null && request.getSubset(Y_AXIS) == null) {  // No subsetting at all was specified
                log.warn("A subsettingCrs is stated but no subsettings were found: ignore it.");
            } else {
                DimensionSubset xSubset = request.getSubset(X_AXIS);
                DimensionSubset ySubset = request.getSubset(Y_AXIS);
                DimensionSubset zSubset = request.getSubset(ELEV_AXIS);

                if (xSubset != null || ySubset != null) {
                    try {
                        if (crsExt.getSubsettingCrs() != null
                                && crsExt.getSubsettingCrs().equals(CrsUtil.GRID_CRS)) {
                            log.info("Subset(s) defined in pixel coordinates: no CRS reprojection need to be made.");

                        } else if (crsExt.getSubsettingCrs() == null
                                || CrsUtil.CrsUri.areEquivalent(crsExt.getSubsettingCrs(), bbox.getCrsName())) {
                            log.info("Requested CRS (" + crsExt.getSubsettingCrs() + ") and native CRS coincide: no tranform needed.");
                            // Req7: /req/crs/getCoverage-subsettingCrs-default
                            if (crsExt.getSubsettingCrs() == null) {
                                crsExt.setSubsettingCrs(bbox.getCrsName());
                            }
                            // Fill in crs of subsets, in case they were not embedded in the trimming expression of the request (es. KVP)
                            if (xSubset != null) {
                                xSubset.setCrs(bbox.getCrsName());
                            }
                            if (ySubset != null) {
                                ySubset.setCrs(bbox.getCrsName());
                            }
                        } else {
                            log.info("Requested CRS (" + crsExt.getSubsettingCrs() + ") and native CRS (" + bbox.getCrsName() + ") differ: tranform needed.");

                            // If elevation is required to be transformed then throw exception: currently only 2D
                            if (zSubset != null) {
                                throw new WCSException(ExceptionCode.OperationNotSupported, "3D spatial transforms are currently not supported.");
                            }

                            // Extrapolate values independently of Trim/Slice request
                            String[] subX, subY;
                            CrsUtil crsTool;
                            List<Double> temp;
                            if (xSubset == null) {
                                subX = new String[2];
                            } else {
                                subX = (xSubset instanceof DimensionTrim)
                                        ? new String[]{((DimensionTrim) xSubset).getTrimLow(), ((DimensionTrim) xSubset).getTrimHigh()}
                                        : new String[]{((DimensionSlice) xSubset).getSlicePoint(), ((DimensionSlice) xSubset).getSlicePoint()};
                            }
                            if (ySubset == null) {
                                subY = new String[2];
                            } else {
                                subY = (ySubset instanceof DimensionTrim)
                                        ? new String[]{((DimensionTrim) ySubset).getTrimLow(), ((DimensionTrim) ySubset).getTrimHigh()}
                                        : new String[]{((DimensionSlice) ySubset).getSlicePoint(), ((DimensionSlice) ySubset).getSlicePoint()};
                            }

                            // If only one subsetting was set in the request, fill the other dimension with bbox values (transformed in subsettingCrs)
                            if (subX[0] == null || subY[0] == null) {
                                crsTool = new CrsUtil(bbox.getCrsName(), crsExt.getSubsettingCrs());
                                temp = crsTool.transform(new double[]{bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY()});
                                subX = (subX[0] == null) ? new String[]{"" + temp.get(0), "" + temp.get(2)} : subX;
                                subY = (subY[0] == null) ? new String[]{"" + temp.get(1), "" + temp.get(3)} : subY;
                            }

                            log.debug("Interval values: [" + subX[0] + ":" + subX[1] + "," + subY[0] + ":" + subY[1] + "]");

                            // Now all values are filled: transform
                            crsTool = new CrsUtil(crsExt.getSubsettingCrs(), bbox.getCrsName());
                            // crsTool.transform(xMin,yMin,xMax,yMax):
                            temp = crsTool.transform(new String[]{subX[0], subY[0], subX[1], subY[1]});

                            // Update dimension intervals nodes: one dimension might not be trimmed/sliced!
                            if (xSubset != null) {
                                if (xSubset instanceof DimensionTrim) {
                                    ((DimensionTrim) xSubset).setTrimLow(temp.get(0));
                                    ((DimensionTrim) xSubset).setTrimHigh(temp.get(2));
                                } else {
                                    ((DimensionSlice) xSubset).setSlicePoint(temp.get(0)); //~temp.get(2)
                                }
                            }
                            if (ySubset != null) {
                                if (ySubset instanceof DimensionTrim) {
                                    ((DimensionTrim) ySubset).setTrimLow(temp.get(1));
                                    ((DimensionTrim) ySubset).setTrimHigh(temp.get(3));
                                } else {
                                    ((DimensionSlice) ySubset).setSlicePoint(temp.get(1)); //~temp.get(3)
                                }
                            }
                            // Change crs
                            if (xSubset != null) {
                                xSubset.setCrs(bbox.getCrsName());
                            }
                            if (ySubset != null) {
                                ySubset.setCrs(bbox.getCrsName());
                            }

                            log.debug("Transformed Interval values: [" + temp.get(0) + ":" + temp.get(2) + "," + temp.get(1) + ":" + temp.get(3) + "]");
                        }
                    } catch (WCSException e) {
                        if (((PetascopeException) e).getExceptionCode().getExceptionCode().equalsIgnoreCase(ExceptionCode.InvalidMetadata.getExceptionCode())) {
                            throw new WCSException(ExceptionCode.SubsettingCrsNotSupported,
                                    "subsetting Crs " + crsExt.getSubsettingCrs() + " is not supported by the server.");
                        } else {
                            throw new WCSException("Error while comparing requested subsettingCRS and native CRS of coverage " + m.getCoverageId());
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new WCSException("Error while comparing requested subsettingCRS and native CRS of coverage " + m.getCoverageId());
                    }
                }
            } // else: CRS transform already done (if necessary) by setBounds call.
        } else {

            // Req7: /req/crs/getCoverage-subsettingCrs-default
            // NOTE: if no CRS instance is presence, hence both subsettingCRS and outputCRS were not specified.
            crsExt.setSubsettingCrs(WcsUtil.getSrsName(m));
        }

        // Req10: /req/crs/getCoverage-outputCrs-default
        if (crsExt.getOutputCrs() == null) {
            crsExt.setOutputCrs(crsExt.getSubsettingCrs());
        }

        // Check intersection with coverage Bbox:
        // X axis
        if (!axisDomainIntersection(request.getSubset(X_AXIS), m, crsExt.getSubsettingCrs())) {
            throw new WCSException("Requested subset \"" + request.getSubset(X_AXIS) + "\" is out of coverage bounds \""
                    + bbox.getMinX() + ":" + bbox.getMaxX() + "\".");
        }
        // Y axis
        if (!axisDomainIntersection(request.getSubset(Y_AXIS), m, crsExt.getSubsettingCrs())) {
            throw new WCSException("Requested subset \"" + request.getSubset(Y_AXIS) + "\" is out of coverage bounds \""
                    + bbox.getMinY() + ":" + bbox.getMaxY() + "\".");
        }

        // Request object modified by reference: no need to return a new Request.
        return;
    }

    /**
     * @param DimensionSubset subset A subset from the WCS GetCoverage request
     * @param Bbox bbox Bbox of the requested coverage
     * @return boolean Does request subset intersect with coverage Bbox?.
     */
    public boolean axisDomainIntersection(DimensionSubset subset, GetCoverageMetadata meta, String subsettingCrs) {
        if (subset == null) {
            return true;
        }
        
        // If subsettingCrs is CRS:1, need to check cellDomains instead of geo-Bbox.
        boolean cellSpace = CrsUtil.GRID_CRS.equals(subsettingCrs);
        Bbox bbox = meta.getBbox();
        String subsetDim = subset.getDimension();
        
        // determine cell min/max, needed in case we have a CRS:1 subsetting CRS
        int cMin = 0, cMax = 0;
        if (cellSpace) {
            Iterator<CellDomainElement> it = meta.getMetadata().getCellDomainIterator();
            while (it.hasNext()) {
                CellDomainElement cde = it.next();
                if (subsetDim.equals(cde.getSubsetElement().getDimension())) {
                    cMin = cde.getLoInt();
                    cMax = cde.getHiInt();
                    break;
                }
            }
        }
        
        if (Y_AXIS.equals(subsetDim) || X_AXIS.equals(subsetDim)) {
            // Set bounds
            double bboxMin = cellSpace ? cMin : bbox.getMinY();
            double bboxMax = cellSpace ? cMax : bbox.getMaxY();

            if (subset instanceof DimensionTrim) {
                double subsetYMin = new Double(((DimensionTrim) subset).getTrimLow());
                double subsetYMax = new Double(((DimensionTrim) subset).getTrimHigh());
                if ((subsetYMin < bboxMin && subsetYMax < bboxMin)
                        || (subsetYMin > bboxMax && subsetYMax > bboxMax)) {
                    return false;
                }
            } else if (subset instanceof DimensionSlice
                    && (new Double(((DimensionSlice) subset).getSlicePoint()) < bboxMin
                    || new Double(((DimensionSlice) subset).getSlicePoint()) > bboxMax)) {
                return false;
            }
        }

        return true;
    }

    public Boolean hasParent() {
        return false;
    }

    public String getParentExtensionIdentifier() {
        return "";
    }
}
// enterprise end