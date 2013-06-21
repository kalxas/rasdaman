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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.extensions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.CoverageMetadata;
import petascope.core.CrsDefinition;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.CrsUtil;
import petascope.wcps.server.core.DomainElement;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionSlice;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionSubset;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionTrim;

/**
 * Manage WCS CrsExt Extension (OGC 11-053).
 * Reproject subsets in the WCS request where needed and create a 
 * dictionary (axisName, CrsExt) for output reprojection to correctly
 * forward the task to WCPS.
 * 
 * @author <a href="mailto:cmppri@unife.it">Piero Campalani</a>
 */
public class CRSExtension implements Extension {
    
    private static final Logger log = LoggerFactory.getLogger(CRSExtension.class);
    public static final String REST_SUBSETTING_PARAM = "subsettingcrs";
    public static final String REST_OUTPUT_PARAM = "outputcrs";
    
    /* Constants */  // TODO: USE KVPParser class constants
    //public static final String KEY_SUBSETTINGCRS = "subsettingcrs";
    //public static final String KEY_OUTPUTCRS = "outputcrs";  
    
    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.CRS_IDENTIFIER;
    }
    
    /**
     * Method for the handling of possible subsets in case of subsettingCrs not 
     * correspondent to the one which the desired collection is natively stored.
     * 
     * @param request The WCS request, which is directly modified.
     * @param m       Metadata of the GetCoverage request 
     */
    protected void handle (GetCoverageRequest request, GetCoverageMetadata m) 
            throws WCSException, PetascopeException, SecoreException {
        /**
         * NOTE1 (campalani): CrsExt transform cannot be done when only easting or
         *  only northing is known, so it must be done *outside* DimensionIntervalElement.
         *  Translation to pixel indices is moved outside DimensionInterval as well
         *  since it must be computed a posteriori of (possible) CrsExt transforms.
         * NOTE2 (campalani): at the same time, CrsExt is read only inside DimensionInterval
         *  due to the syntax of WCPS language: trim(x:<crs>(xMin,xMax),y:<crs>(yMin,yMax).
         */
                
        // Read the subsetting CrsExt (if specified): use a dictionary (no order) so link axes labels to *atomic* CRSs in the request
        HashMap<String, String> subsettingCrsMap = new HashMap<String, String>();
        List<DimensionSubset> subsetsList = request.getSubsets();
        
        // Create a list of subsets axis labels
        List<String> subsetsLabels = new ArrayList<String>();
        for (DimensionSubset subset : subsetsList) {
            subsetsLabels.add(subset.getDimension());
        }
                
        // Coverage metadata
        CoverageMetadata cmeta = m.getMetadata();
        
        // Create a dictionary (label, uri) for the specified subsetting CrsExt(s)
        String subsettingCrs = request.getCrsExt().getSubsettingCrs();
        if (subsettingCrs != null) {
            for (String crsUri : CrsUtil.CrsUri.decomposeUri(subsettingCrs)) {
                // Decode URI from either resolver query or cache:
                log.info("Decoding " + crsUri + " ...");
                CrsDefinition crsUriDef = CrsUtil.parseGmlDefinition(crsUri);
                for (CrsDefinition.Axis axis : crsUriDef.getAxes()) {
                    // If this axis is specified in the subsets, then keep it; discard it otherwise.
                    if (subsetsLabels.contains(axis.getAbbreviation())) {
                        subsettingCrsMap.put(axis.getAbbreviation(), crsUri);
                    }
                }
            }
            
            // Check if dimensionality of the subsettingCrs coincide with dimensionality of the coverage
            if (subsettingCrsMap.size() != cmeta.getDomainList().size()) {
                log.error("subsettingCrs dimensionality does not coincide with coverage dimensionality.");
                throw new WCSException(ExceptionCode.InvalidRequest, "subsettingCrs defines " + subsettingCrsMap.size() +
                        " dimensions, whereas the coverage has " + cmeta.getDomainList().size() + " axes.");
            }
        }
        
        // Check if a CrsExt transform is needed for _spatial_ axis: 
        /* NOTE: there are different cases, e.g. 1D, 2D, 2D+1D, 3D in source and target CRSs. 
         * At first, let's accept 2D and 3D transforms.
         */
        
        // Firstly: check if the requests specified at least one spatial subset (either trim or slice):
        // TODO: need to check if it is Linear1D, Linear2D or Linear3D instead of CrsExt:1, and 
        // in case use CrsUtil.areEquivalent() instead of simple .equals.
        if (!subsettingCrsMap.isEmpty()) {
                    try {
                        // Loop through each subset axis (which might belong to different spatial CrsExt)
                        // and check if subsettingCrs actually specifies some non-native CrsExt
                        Set<Entry<String, String>> entrySet = subsettingCrsMap.entrySet();
                        Object[] entryArray = entrySet.toArray();
                        
                        // Loop through the dictionary, i.e. through the axes of the subsettingCrs
                        // NOTE: don't use Iterator so allow bidirectional access to the Set
                        for (int i = 0; i < entryArray.length; i++) {
                            Map.Entry<String,String> mapEntry = (Entry<String, String>)entryArray[i]; 
                            String requestedCrs = mapEntry.getValue();
                            
                            DomainElement domEl = cmeta.getDomainByName(mapEntry.getKey()); // GET BY TYPE? LABELS ARE DIFFERENT!!
                            if (domEl == null) {
                                log.error("Could not couple axis " + mapEntry.getKey() + " with any coverage axis. Wrong subsettingCrs or labels?");
                                throw new PetascopeException(ExceptionCode.InternalComponentError);
                            }
                            String nativeCrs    = domEl.getCrs();
                            String currentAxis  = mapEntry.getKey();
                            
                            // Create list of axes that have the same CrsExt (and may need to be collect for subsetting reprojection)
                            List<String> involvedAxes = new ArrayList();
                            involvedAxes.add(mapEntry.getKey());
                            
                            if (CrsUtil.CrsUri.areEquivalent(requestedCrs, nativeCrs)) {
                                log.info("Requested CRS (" + requestedCrs + ") and native CRS on axis " + 
                                        currentAxis + " coincide: no tranform needed.");
                            } else {
                            
                                log.info("Requested CRS (" + requestedCrs + ") and native CRS (" + nativeCrs + ") on axis " +
                                        currentAxis + " differ: reprojection needed.");
                                
                                // Collect the axes which are under the same CrsExt 
                                // NOTE: In this case I can use .equals() instead of areEquivalent(), see above how subsettingCrsMap is created.
                                while (i+1 < entryArray.length && ((Entry<String, String>)entryArray[i+1]).getValue().equals(requestedCrs)) {
                                    involvedAxes.add(((Entry<String, String>)entryArray[i+1]).getKey());
                                    log.info(requestedCrs + " involves as well axis " + involvedAxes.get(involvedAxes.size()-1));
                                    i++; // increment so that an axis is not read twice!
                                }
                            
                                // crsTool.transform() takes an array of Strings: [X1min, X2min, ..., X1max, X2max, ...]
                                String[] srcCoords = new String[2*involvedAxes.size()];
                                
                                // For any axes **defined in the current single CrsExt** check 
                                // either subset values or bbox values (if a certain dimension does not appear in the request)                                
                                for (String axisName : involvedAxes) {
                                    if (subsetsLabels.contains(axisName)) {
                                        log.debug(axisName + " is involved in a subset: extract it(s) value(s).");
                                        // this dimension is subset in the request: get values
                                        int axisIndex = involvedAxes.indexOf(axisName);
                                        String loBound = (subsetsList.get(axisIndex) instanceof DimensionTrim) ?
                                                ((DimensionTrim)subsetsList.get(axisIndex)).getTrimLow()  :
                                                ((DimensionSlice)subsetsList.get(axisIndex)).getSlicePoint();
                                        String hiBound = (subsetsList.get(axisIndex) instanceof DimensionTrim) ?
                                                ((DimensionTrim)subsetsList.get(axisIndex)).getTrimHigh() :
                                                ((DimensionSlice)subsetsList.get(axisIndex)).getSlicePoint();
                                        // Add to global container of source coordinates for this CrsExt
                                        srcCoords[axisIndex]                     = loBound;
                                        srcCoords[involvedAxes.size()+axisIndex] = hiBound;
                                    } else {
                                        log.debug(axisName + " is not involved in a subset: reproject the BBOX and extract the reprojected values.");
                                        // this dimension is not subset, need to take bbox values (but in the same CrsExt of the subsets!)
                                        // Reproject the whole BBOX then take what I need:                                        
                                        int axisIndex = involvedAxes.indexOf(axisName);
                                        String[] srcCoordsBbox = new String[2*involvedAxes.size()];
                                        for (String axisNameBbox : involvedAxes) {
                                            int bboxAxisIndex = involvedAxes.indexOf(axisNameBbox);
                                            // collect extremes for the axes defined by this CrsExt
                                            srcCoordsBbox[bboxAxisIndex]                 = cmeta.getDomainByName(axisNameBbox).getMinValue().toString();
                                            srcCoordsBbox[involvedAxes.size()+axisIndex] = cmeta.getDomainByName(axisNameBbox).getMaxValue().toString();
                                        }                                        
                                        // reproject to subsettingCrs
                                        CrsUtil crsTool = new CrsUtil(nativeCrs, requestedCrs);
                                        List<Double> trgCoordsBbox = crsTool.transform(srcCoordsBbox);
                                        log.debug("Reprojected BBOX values: [" + trgCoordsBbox + "]");
                                        // extract only values of the current axis
                                        String loBound = trgCoordsBbox.get(axisIndex) + "";
                                        String hiBound = trgCoordsBbox.get(involvedAxes.size()+axisIndex) + "";
                                        // Add to global container of source coordinates for this CrsExt
                                        srcCoords[axisIndex]                     = loBound;
                                        srcCoords[involvedAxes.size()+axisIndex] = hiBound;                                                                
                                    }                                
                                }
                            
                                // Now all source coordinates are filled: transform
                                CrsUtil crsTool = new CrsUtil(requestedCrs, nativeCrs);
                                List<Double> trgCoords = crsTool.transform(srcCoords);
                                log.debug("Reprojected subsetting values: [" + trgCoords + "]");
                                
                                // Update subsettings values and CrsExt: now they are in native coordinates
                                for (DimensionSubset subset : subsetsList) {
                                    // Need to properly extract reprojected values from the array
                                    int axisIndex = involvedAxes.indexOf(subset.getDimension());
                                    // Update values and CrsExt
                                    if (subset instanceof DimensionTrim) {
                                        ((DimensionTrim)subset).setTrimLow(trgCoords.get(axisIndex));
                                        ((DimensionTrim)subset).setTrimHigh(trgCoords.get(involvedAxes.size()+axisIndex));
                                    } else {
                                        /* In case of slicing, the transformed slice point turns to two points:
                                         *
                                         *  [  source CrsExt   ]     [  target CrsExt  ]
                                         *    o                     o
                                         *    |                      \
                                         *    |                       \
                                         *    |                        \
                                         *    o                         o
                                         *  --x--------> LONG     --x---x------> EAST
                                         *  slice                   slice
                                         *  point                   points
                                         *
                                         * -> Take the *average* as value for the naticeCrs slicing.
                                         */
                                        ((DimensionSlice)subset).setSlicePoint(
                                                (trgCoords.get(axisIndex) + trgCoords.get(involvedAxes.size()+axisIndex))/2
                                                );
                                    }
                                    subset.setCrs(nativeCrs);
                                }
                                
                                log.debug("Transformed Interval values: [" + trgCoords + "]");
                            }
                        }
                    } catch (WCSException e) {
                        if (((PetascopeException)e).getExceptionCode().getExceptionCode().equalsIgnoreCase(ExceptionCode.InvalidMetadata.getExceptionCode())) {
                            throw new WCSException(ExceptionCode.SubsettingCrsNotSupported,
                                    "subsetting Crs " + subsettingCrs + " is not supported by the server.");
                        } else {
                            throw new WCSException("Error while comparing requested subsettingCRS and native CRS of coverage " + m.getCoverageId());
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new WCSException("Error while comparing requested subsettingCRS and native CRS of coverage " + m.getCoverageId());
                    }
        } else {
            // No subsettingCrs was specified
            // Req7: /req/crs/getCoverage-subsettingCrs-default
            // NOTE: if no CrsExt instance is presence, hence both subsettingCRS and outputCRS were not specified.
            request.getCrsExt().setSubsettingCrs(CrsUtil.CrsUri.createCompound(cmeta.getCrsUris()));
        }

        // Req10: /req/crs/getCoverage-outputCrs-default
        if (request.getCrsExt().getOutputCrs() == null) {
            request.getCrsExt().setOutputCrs(request.getCrsExt().getSubsettingCrs()); 
        }
        
        // Need to parse the outputCrs and understand which axes are involved: to properly translate to WCPS (crsTransformExpr)
        // Register to the CrsExt object only the dimensions that need to be reprojected
        String outputCrs = request.getCrsExt().getOutputCrs();
        for (String crsUri : CrsUtil.CrsUri.decomposeUri(outputCrs)) {
            CrsDefinition crsUriDef = CrsUtil.parseGmlDefinition(crsUri);
            for (CrsDefinition.Axis axis : crsUriDef.getAxes()) {
                if (!CrsUtil.CrsUri.areEquivalent(cmeta.getDomainByName(axis.getAbbreviation()).getCrs(), crsUri)) {
                    // Output Reprojection requested on this axis
                    log.info("Output reprojection was specified for axis " + axis.getAbbreviation());
                    request.getCrsExt().addAxisOutputReprojection(axis.getAbbreviation(), crsUri);
                }  else {
                    log.info("No output reprojection was specified for axis " + axis.getAbbreviation());
                }
            }
        }            
        
        // Check intersection with coverage Bbox:
        Set<Entry<String, String>> entrySet = subsettingCrsMap.entrySet();
        for (Entry<String, String> axisCrsEntry : entrySet) {
            if (!axisDomainIntersection(request.getSubset(axisCrsEntry.getKey()), m, axisCrsEntry.getValue())) {
                throw new WCSException(ExceptionCode.InvalidSubsetting,
                        "Requested subset \"" + request.getSubset(axisCrsEntry.getKey()) + "\" is out of coverage bounds \"" +
                        m.getBbox().getMinValue(axisCrsEntry.getKey()) + ":" + m.getBbox().getMaxValue(axisCrsEntry.getKey()) + "\".");
            }
        }
        
        // Request object modified by reference: no need to return a new Request.
    }
    
    /**
     * @param   DimensionSubset subset  A subset from the WCS GetCoverage request
     * @param   Bbox bbox    Bbox of the requested coverage
     * @return  boolean      Does request subset intersect with coverage Bbox?.
     */
    public boolean axisDomainIntersection (DimensionSubset subset, GetCoverageMetadata meta, String subsettingCrs) {
        
        if (subset == null) {
            return true;
        }
        
        // If subsettingCrs is CrsExt:1, need to check cellDomains instead of geo-Bbox.
        boolean cellSpace = subsettingCrs.equals(CrsUtil.GRID_CRS);
        
        String subsetAxisName = subset.getDimension();
        
        // Set bounds
        double bboxMin = cellSpace ?
                meta.getMetadata().getCellDomainByName(subsetAxisName).getLo().doubleValue() :
                new Double(meta.getBbox().getMinValue(subsetAxisName));
        double bboxMax = cellSpace ?
                meta.getMetadata().getCellDomainByName(subsetAxisName).getHi().doubleValue() :
                new Double(meta.getBbox().getMaxValue(subsetAxisName));
        
        // Check overlap: to avoid leaks, check assumes subsets might not be well ordered (e.g. subsetMin>subsetMax):
        if (subset instanceof DimensionTrim) {
            double subsetMin = new Double(((DimensionTrim)subset).getTrimLow());
            double subsetMax = new Double(((DimensionTrim)subset).getTrimHigh());            
            if ((subsetMin < bboxMin && subsetMax < bboxMin) || 
                    (subsetMin > bboxMax && subsetMax > bboxMax))
                return false;
        } else if (subset instanceof DimensionSlice &&
                (new Double(((DimensionSlice)subset).getSlicePoint()) < bboxMin ||
                 new Double(((DimensionSlice)subset).getSlicePoint()) > bboxMax))
            return false;
        
        return true;
    }
}
