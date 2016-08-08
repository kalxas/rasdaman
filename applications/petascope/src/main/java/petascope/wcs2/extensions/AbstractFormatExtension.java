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
package petascope.wcs2.extensions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.rasdaman.RasdamanException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.exceptions.WCSException;
import petascope.util.AxisTypes;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.Pair;
import petascope.util.StringUtil;
import petascope.util.TimeUtil;
import petascope.util.WcsUtil;
import petascope.util.XMLSymbols;
import petascope.util.ras.RasUtil;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps.server.core.Wcps;
import petascope.wcps2.metadata.service.CoordinateTranslationService;
import petascope.wcps2.metadata.service.CoverageRegistry;
import petascope.wcps2.metadata.service.RasqlTranslationService;
import petascope.wcps2.metadata.service.SubsetParsingService;
import petascope.wcps2.metadata.service.WcpsCoverageMetadataService;
import petascope.wcps2.parser.WcpsTranslator;
import petascope.wcps2.result.VisitorResult;
import petascope.wcps2.result.WcpsMetadataResult;
import petascope.wcps2.result.WcpsResult;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;
import static petascope.wcs2.parsers.subsets.DimensionSubset.ASTERISK;
import petascope.wcs2.parsers.subsets.DimensionSlice;
import petascope.wcs2.parsers.subsets.DimensionSubset;
import petascope.wcs2.parsers.subsets.DimensionTrim;
import static petascope.wcs2.parsers.subsets.DimensionSubset.QUOTED_SUBSET;
import petascope.wcs2.parsers.GetCoverageRequest.Scaling;

/**
 * An abstract implementation of {@link FormatExtension}, which provides some
 * convenience methods to concrete implementations.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public abstract class AbstractFormatExtension implements FormatExtension {

    private static final Logger log = LoggerFactory.getLogger(AbstractFormatExtension.class);

    /**
     * Update m with the correct bounds and axes (mostly useful when there is slicing/trimming in the request)
     *
     * @param request
     * @param m
     * @param dbMeta
     * @throws PetascopeException
     * @throws SecoreException
     * @throws WCSException
     */
    protected void updateGetCoverageMetadata(GetCoverageRequest request, GetCoverageMetadata m, DbMetadataSource dbMeta)
            throws PetascopeException, SecoreException, WCSException {

        // Init variables, to be then filled scanning the request subsets.

        // Grid axis labels, and grid bounds : grid (rasdaman) order
        Map<Integer, String> axesLabels = new TreeMap<Integer, String>();
        String lowerCellDom = "";
        String upperCellDom = "";
        // Tuples of external CRS bounds : CRS order
        // Create a key-ordered map {axisLabel->dom} so that the correct order can be reconstructed automatically.
        Map<Integer,String> lowerDom = new TreeMap<Integer,String>();
        Map<Integer,String> upperDom = new TreeMap<Integer,String>();
        // Same as lowerDom/upperDom but forcing easting first, for GIS binary formats encoding.
        String lowerGisDom = "";
        String upperGisDom = "";

        // CRS need to be sliced accordingly upon dimension slicings
        String crsName;
        Set<String> slicedAxes = new HashSet<String>();

        // miscellanea
        CoverageMetadata meta = m.getMetadata();
        boolean domUpdated;
        Iterator<DomainElement>         domsIt = meta.getDomainIterator();
        Iterator<CellDomainElement> cellDomsIt = meta.getCellDomainIterator();
        DomainElement domainEl;
        CellDomainElement cellDomainEl;
        List<DimensionSubset> subsList = request.getSubsets();

        // NOTE: single loop since still N=M for Petascope, being N = dim(grid) and M=dim(CRS).
        // Keep domainElement order (grid order), but need to re-order the coordinates in the tuple for lowerDom/upperDom
        while (domsIt.hasNext()) {
            // Check if one subset trims on /this/ dimension:
            // Order and quantity of subsets not necessarily coincide with domain of the coverage
            // (e.g. single subset on Y over a nD coverage)
            domUpdated = false;
            domainEl     = domsIt.next();
            cellDomainEl = cellDomsIt.next();
            // Loop through each subsets in the request and check if this axis is involved
            Iterator<DimensionSubset> subsIt = subsList.iterator();
            DimensionSubset subset;
            while (subsIt.hasNext()) {
                subset = subsIt.next();
                if (subset.getDimension().equals(domainEl.getLabel())) {
                    try {
                        // Compare subset with domain borders and update
                        if (subset instanceof DimensionTrim) {

                            // Replace asterisks and fetch subset bounds
                            stars2bounds((DimensionTrim)subset, domainEl);
                            String trimLow = ((DimensionTrim)subset).getTrimLow();
                            String trimHigh = ((DimensionTrim)subset).getTrimHigh();

                            // Append axis/uom label
                            axesLabels.put(CrsUtil.getCrsAxisOrder(meta.getCrsUris(), domainEl.getLabel()), subset.getDimension());
                            // Append updated bounds
                            // TODO: if request is specified via grid coords, need a backwards transform here
                            //       {cellDomain->domain} to show domain values in the WCS response:
                            //       Crs.convertToDomainCoords()
                            if (trimLow.contains("\"")) {
                                // Convert timestamp to temporal numeric coordinate
                                String datumOrigin = domainEl.getAxisDef().getCrsDefinition().getDatumOrigin();
                                trimLow = "" + (TimeUtil.countOffsets(datumOrigin, trimLow, domainEl.getUom(), 1D)); // do not normalize by vector here: absolute time coords needed
                            }
                            String lower = new BigDecimal(Math.max(
                                    Double.parseDouble(trimLow),
                                    domainEl.getMinValue().doubleValue())).toPlainString();
                            // align with sample space of grid points:
                            lower = WcsUtil.fitToSampleSpace(lower, domainEl, false, m.getCoverageType());
                            lowerGisDom += lower + " ";
                            // The map is automatically sorted by key value (axis order in the CRS definition)
                            lowerDom.put(CrsUtil.getCrsAxisOrder(meta.getCrsUris(), domainEl.getLabel()), lower);
                            if (trimHigh.contains("\"")) {
                                // Convert timestamp to temporal numeric coordinate
                                String datumOrigin = domainEl.getAxisDef().getCrsDefinition().getDatumOrigin();
                                String stringHi = trimHigh;
                                trimHigh = "" + (TimeUtil.countOffsets(datumOrigin, stringHi, domainEl.getUom(), 1D)); // do not normalize by vector here: absolute time coords needed
                            }
                            String upper = new BigDecimal(Math.min(
                                    Double.parseDouble(trimHigh),
                                    domainEl.getMaxValue().doubleValue())).toPlainString();
                            // align with sample space of grid points:
                            upper = WcsUtil.fitToSampleSpace(upper, domainEl, true, m.getCoverageType());
                            upperGisDom += upper + " ";
                            // The map is automatically sorted by key value (axis order in the CRS definition)
                            upperDom.put(CrsUtil.getCrsAxisOrder(meta.getCrsUris(), domainEl.getLabel()), upper);
                            // Append updated pixel bounds
                            String decimalsExp = "\\.[0-9]+";
                            long[] cellDom = (CrsUtil.GRID_CRS.equals(subset.getCrs()) || // : subset=x,CRS:1(x1,x2) || subsettingCrs=CRS:1
                                    m.getCoverageType().equals(XMLSymbols.LABEL_GRID_COVERAGE) ||
                                    (request.getCrsExt() != null && CrsUtil.GRID_CRS.equals(request.getCrsExt().getSubsettingCrs())))
                                    ? new long[] { // NOTE: e.g. parseInt("10.0") throws exception: need to remove decimals.
                                        Integer.parseInt(trimLow.replaceAll( decimalsExp, "").trim()),
                                        Integer.parseInt(trimHigh.replaceAll(decimalsExp, "").trim())} // subsets are already grid indexes
                                    : CrsUtil.convertToInternalGridIndices(m.getMetadata(), dbMeta, domainEl.getLabel(),
                                        trimLow,   !trimLow.matches(QUOTED_SUBSET),
                                        trimHigh, !trimHigh.matches(QUOTED_SUBSET));
                            // If SCALING on this dimension, fix upperCellDom and offset vector by scale factor
                            if (request.isScaled()) {
                                // SCALING EXTENSION: geometry changes
                                Scaling scaling = request.getScaling();
                                String axisLabel = subset.getDimension();
                                if (scaling.isScaled(axisLabel)) {
                                    BigDecimal scalingFactor = ScalingExtension.computeScalingFactor(scaling, axisLabel, new BigDecimal(cellDom[0]), new BigDecimal(cellDom[1]));
                                    // update grid envelope
                                    long scaledExtent = Math.round(Math.floor((cellDom[1]-cellDom[0]+1)/scalingFactor.floatValue()));
                                    cellDom[1] = (long)(cellDom[0] + scaledExtent - 1);
                                    // update offset vectors
                                    // [!] NOTE: do *not* use domainEl.setScalarResolution since world2pixel conversions are cached.
                                    m.setScalingFactor(axisLabel, scalingFactor);
                                }
                            }
                            // In any case, properly trim the bounds by the image extremes
                            int cellDomainElLo = cellDomainEl.getLoInt();
                            int cellDomainElHi = cellDomainEl.getHiInt();
                            lowerCellDom += (cellDomainElLo > cellDom[0]) ? cellDomainElLo + " " : cellDom[0] + " ";
                            upperCellDom += (cellDomainElHi < cellDom[1]) ? cellDomainElHi + " " : cellDom[1] + " ";

                        } else if (subset instanceof DimensionSlice) {
                            log.debug("Axis " + domainEl.getLabel() + " has been sliced: remove it from the boundedBy element and track the axis for CRS slicing.");
                            slicedAxes.add(subset.getDimension());
                        } else {
                            throw new WCSException(ExceptionCode.InternalComponentError,
                                    "Subset '" + subset + "' is not recognized as trim nor slice.");
                        }
                        // flag: if no subset has updated the bounds, then need to append the bbox value
                        domUpdated = true;
                    } catch (NumberFormatException ex) {
                        String message = "Error while casting a subset to numeric format for comparison: " + ex.getMessage();
                        log.error(message);
                        throw new WCSException(ExceptionCode.InvalidRequest, message);
                    } catch (WCSException ex) {
                        throw ex;
                    }
                }
            } // END subsets iterator
            if (!domUpdated) {
                // This dimension is not involved in any subset: use bbox bounds
                axesLabels.put(CrsUtil.getCrsAxisOrder(meta.getCrsUris(), domainEl.getLabel()), domainEl.getLabel());
                lowerGisDom += BigDecimalUtil.stripDecimalZeros(domainEl.getMinValue()) + " ";
                upperGisDom += BigDecimalUtil.stripDecimalZeros(domainEl.getMaxValue()) + " ";
                // The map is automatically sorted by key value (axis order in the CRS definition)
                lowerDom.put(
                        CrsUtil.getCrsAxisOrder(meta.getCrsUris(), domainEl.getLabel()),
                        BigDecimalUtil.stripDecimalZeros(domainEl.getMinValue()).toPlainString());
                upperDom.put(
                        CrsUtil.getCrsAxisOrder(meta.getCrsUris(), domainEl.getLabel()),
                        BigDecimalUtil.stripDecimalZeros(domainEl.getMaxValue()).toPlainString());

                // SCALING: geometry changes
                long loCellDom = cellDomainEl.getLoInt();
                long hiCellDom = cellDomainEl.getHiInt();
                Scaling scaling = request.getScaling();
                String axisLabel = domainEl.getLabel();
                if (scaling.isScaled(axisLabel)) {
                    BigDecimal scalingFactor = ScalingExtension.computeScalingFactor(scaling, axisLabel, BigDecimal.valueOf(loCellDom), BigDecimal.valueOf(hiCellDom));
                    // update grid envelope
                    long scaledExtent = Math.round(Math.floor((hiCellDom-loCellDom+1)/scalingFactor.floatValue()));
                    hiCellDom = (long)(loCellDom + scaledExtent - 1);
                    // update offset vectors
                    // [!] NOTE: do *not* use domainEl.setScalarResolution since world2pixel conversions are cached.
                    m.setScalingFactor(axisLabel, scalingFactor);
                }
                if (hiCellDom < loCellDom) {
                    lowerCellDom += hiCellDom + " ";
                    upperCellDom += loCellDom + " ";
                } else {
                    lowerCellDom += loCellDom + " ";
                    upperCellDom += hiCellDom + " ";
                }
            }
        } // END domains iterator

        // Update axes labels
        m.setAxisLabels(StringUtils.join(axesLabels.values(), " "));
        // Update **pixel-domain** bounds
        m.setLow(lowerCellDom);
        m.setHigh(upperCellDom);
        // Update **domain** bounds (GIS- and CRS-induced)
         m.setDomLow(ListUtil.printList(new ArrayList<String>(lowerDom.values()), " "));
        m.setDomHigh(ListUtil.printList(new ArrayList<String>(upperDom.values()), " "));
        m.setGisDomLow(lowerGisDom);
        m.setGisDomHigh(upperGisDom);
        // Update the **CRS**
        if (!slicedAxes.isEmpty()) {
            crsName = CrsUtil.sliceAxesOut(meta.getCrsUris(), slicedAxes);
            m.setCrs(crsName);
        }

        if (request.hasRangeSubsetting()) {
            // RANGE TYPE update
            m.setRangeFields(request.getRangeSubset().getSelectedComponents());
        }
    }

    /**
     * Execute rasql query, given GetCoverage request, request metadata, and
     * format of result. Request subsets values are pre-transformed if necessary
     * (e.g. CRS reprojection, timestamps).
     * @param request
     * @param covmeta
     * @param meta
     * @param format
     * @param params
     * @return (result of executing the query, axes)
     * @throws PetascopeException
     * @throws RasdamanException
     * @throws WCPSException
     * @throws WCSException
     */
    protected Pair<Object, String> executeRasqlQuery(GetCoverageRequest request,
            GetCoverageMetadata covmeta, DbMetadataSource meta, String format, String params)
            throws PetascopeException, RasdamanException, WCPSException, WCSException {

        //This variable is now local to the method to avoid concurrency problems
        Wcps wcps;

        try {
            wcps = new Wcps(meta);
        } catch (Exception ex) {
            throw new WCSException(ExceptionCode.InternalComponentError, "Error initializing WCPS engine", ex);
        }

        // Add double quotes in possible timestamp-based temporal subsets:
        // WCPS needs then to recognize a StringExpression from a NumericalExpression:
        // --> set quotes directly in the WCS request if timestamps want to be used (' = %27, " = %22)
        // since there are cases which can create conflict (eg 2010 is year 2010 or numeric temporal coordinate 2010?)

        // Proceed to WCPS:
        String rquery = null;
        Pair<String, String> pair;

        CoverageRegistry coverageRegistry = new CoverageRegistry(meta);
        CoordinateTranslationService coordinateTranslationService = new CoordinateTranslationService(coverageRegistry);
        WcpsCoverageMetadataService wcpsCoverageMetadataService = new WcpsCoverageMetadataService(coordinateTranslationService);
        RasqlTranslationService rasqlTranslationService = new RasqlTranslationService();
        SubsetParsingService subsetParsingService = new SubsetParsingService();
        WcpsTranslator wcpsTranslator = new WcpsTranslator(coverageRegistry, wcpsCoverageMetadataService,
                                                           rasqlTranslationService, subsetParsingService);

        try {
            pair = constructWcpsQuery(request, meta, covmeta, format, params);
            String wcpsQuery = pair.fst;

            VisitorResult wcpsResult = wcpsTranslator.translate(wcpsQuery);
            // NOTE: result should be Rasql Query which is generated from WCPS 1.5
            if (wcpsResult instanceof WcpsMetadataResult) {
                throw new WCSException(ExceptionCode.InvalidRequest, "This server does not support return metadata value for the request.");
            } else {
                rquery = ((WcpsResult)wcpsResult).getRasql();
            }
        } catch (PetascopeException ex) {
            throw new PetascopeException(ex.getExceptionCode(), "Error converting WCPS query to rasql query: " + ex.getMessage(), ex);
        }

        Object res = null;
        try {
            if ("sdom".equals(format) && !rquery.contains(":")) {
                res = null;
            } else {
                res = RasUtil.executeRasqlQuery(rquery);
            }
        } catch (RasdamanException ex) {
            throw new WCSException(ExceptionCode.RasdamanRequestFailed, "Error executing rasql query: " + ex.getMessage(), ex);
        }

        return Pair.of(res, pair.snd);
    }

    /**
     * Given a GetCoverage request, construct an abstract WCPS query.
     *
     * @param req GetCoverage request
     * @param covMeta
     * @param dbMeta
     * @param format
     * @param params
     * @return (WCPS query in abstract syntax, axes)
     * @throws WCSException
     * @throws PetascopeException
     */
    protected Pair<String, String> constructWcpsQuery(GetCoverageRequest req, DbMetadataSource dbMeta,
            GetCoverageMetadata covMeta, String format, String params)
            throws WCSException, PetascopeException {

        String axes = "";
        //keep a list of the axes defined in the coverage
        ArrayList<String> axesList = new ArrayList<String>();
        Iterator<DomainElement> dit = covMeta.getMetadata().getDomainIterator();
        while (dit.hasNext()) {
            String axis = dit.next().getLabel();
            axes += axis + " ";
            axesList.add(axis);
        }
        String proc = "c";

        //Process rangesubsetting based on the coverage alias
        if (req.hasRangeSubsetting()) {
            proc = RangeSubsettingExtension.processWCPSRequest(proc, req.getRangeSubset());
        }
        //End range subsetting processing

        HashMap<String, Pair<String, String>> newdim = new HashMap<String, Pair<String, String>>(); // saves the new limits of the axes after trimming or slicing

        // process subsetting operations
        for (DimensionSubset subset : req.getSubsets()) {
            String dim = subset.getDimension();
            DomainElement de = covMeta.getMetadata().getDomainByName(dim);

            //Check if the supplied axis is in the coverage axes and throw exception if not
            if (!axesList.contains(dim)) {
                throw new WCSException(ExceptionCode.InvalidAxisLabel,
                        "The axis label " + dim + " was not found in the list of available axes");
            }

            // Parametrized CRSs can have quotes and other reserved entities which break abstract WCPS queries (and XML)
            String crs = StringUtil.escapeXmlPredefinedEntities(de.getNativeCrs());

            // in-subset CRS specification (standard inconsistency CRS hanlding in KVP/XML: see OGC 12-167 change request #257)
            // accept direct internal index subsets even if no CRS extension is provided (this is not a geo-reprojection)
            if (null != subset.getCrs() && CrsUtil.isGridCrs(subset.getCrs())) {
                crs = subset.getCrs(); // replace native with grid crs
            } else if (covMeta.getSubsettingCrs() != null) {
                // CRSExestion with subsettingCrs parameters then all subsets need to be used with this CRS
                crs = covMeta.getSubsettingCrs();
            }

            if (subset instanceof DimensionTrim) {
                DimensionTrim trim = (DimensionTrim) subset;
                proc = "trim(" + proc + ",{" + dim + ":\"" + crs + "\" ("
                        + trim.getTrimLow() + ":" + trim.getTrimHigh() + ")})";
                newdim.put(dim, new Pair(trim.getTrimLow(), trim.getTrimHigh()));

            } else if (subset instanceof DimensionSlice) {
                DimensionSlice slice = (DimensionSlice) subset;
                proc = "slice(" + proc + ",{" + dim + ":\"" + crs + "\" (" + slice.getSlicePoint() + ")})";
                newdim.put(dim, new Pair(slice.getSlicePoint(), slice.getSlicePoint()));
                log.debug("Dimension " + dim);
                log.debug(axes);
                axes = axes.replaceFirst(dim + " ?", ""); // remove axis
            }
        }

        if (req.isScaled()) {
            if (!WcsUtil.isGrid(covMeta.getMetadata().getCoverageType())) {
                throw new WCSException(ExceptionCode.InvalidCoverageType.locator(req.getCoverageId()));
            }
            Scaling scaling = req.getScaling();
            int axesNumber = 0; // for checking if all axes in the query were used
            String crs = CrsUtil.GRID_CRS; // scaling involves pixels
            proc = "scale(" + proc + ", {";
            Iterator<DomainElement> it = covMeta.getMetadata().getDomainIterator();
            Iterator<CellDomainElement> cit = covMeta.getMetadata().getCellDomainIterator();
            // Need to loop through all dimensions to set scaling dims for un-trimmed axes too
            while (it.hasNext() && cit.hasNext()) {
                DomainElement el = it.next();
                CellDomainElement cel = cit.next();
                String dim = el.getLabel();

                //FIXME: hack for ticket #823 - number 5. to be fixed with ticket #824
                /*if (el.isIrregular() && !req.isSliced(dim) && scaling.isScaled(dim)) {
                    log.error("Trying to scale an irregular axis but we cannot scale coefficients' values.");
                    throw new PetascopeException(ExceptionCode.UnsupportedCombination,
                            "Scaling on irregular axis is not supported.");
                }*/
                // Sliced dimensions shall not be referenced by the scaling parameters
                if (!req.isSliced(dim)) {
                    long lo = cel.getLoInt();
                    long hi = cel.getHiInt();
                    long scaledExtent;
                    if (newdim.containsKey(dim)) {
                        long[] lohi = CrsUtil.convertToInternalGridIndices(covMeta.getMetadata(), dbMeta, dim,
                                newdim.get(dim).fst, req.getSubset(dim).isNumeric(),
                                newdim.get(dim).snd, req.getSubset(dim).isNumeric());
                        lo = lohi[0];
                        hi = lohi[1];
                    }
                    long hiAfterScale;
                    switch (scaling.getType()) {
                        case 1:
                            // SCALE-BY-FACTOR: divide extent by global scaling factor
                            scaledExtent = Math.round(Math.floor((hi-lo+1)/scaling.getFactor()));

                            hiAfterScale = Math.round(Math.floor(lo + scaledExtent - 1));
                            if (lo > hiAfterScale) {
                                long temp = lo;
                                lo = hiAfterScale;
                                hiAfterScale = temp;
                            }

                            proc = proc + dim + ":\"" + crs + "\"(" + lo
                                    + ":" + hiAfterScale + "),";
                            break;
                        case 2:
                            // SCALE-AXES: divide extent by axis scaling factor
                            if (scaling.isPresentFactor(dim)) {
                                scaledExtent = Math.round(Math.floor((hi-lo+1)/scaling.getFactor(dim)));

                                hiAfterScale = Math.round(Math.floor(lo + scaledExtent - 1));
                                if (lo > hiAfterScale) {
                                    long temp = lo;
                                    lo = hiAfterScale;
                                    hiAfterScale = temp;
                                }
                                proc = proc + dim + ":\"" + crs + "\"(" + lo
                                        + ":" + hiAfterScale + "),";
                                axesNumber++;
                            } else {
                                if (lo > hi) {
                                    long temp = lo;
                                    lo = hi;
                                    hi = temp;
                                }
                                proc = proc + dim + ":\"" + crs + "\"(" + lo + ":" + hi + "),";
                            }
                            break;
                        case 3:
                            // SCALE-SIZE: set extent of dimension
                            if (scaling.isPresentSize(dim)) {
                                hiAfterScale = (lo + scaling.getSize(dim)-1);
                                if (lo > hiAfterScale) {
                                    long temp = lo;
                                    lo = hiAfterScale;
                                    hiAfterScale = temp;
                                }
                                proc = proc + dim + ":\"" + crs + "\"(" + lo
                                        + ":" + hiAfterScale + "),";
                                axesNumber++;
                            } else {
                                if (lo > hi) {
                                    long temp = lo;
                                    lo = hi;
                                    hi = temp;
                                }
                                proc = proc + dim + ":\"" + crs + "\"(" + lo + ":" + hi + "),";
                            }
                            break;
                        case 4:
                            // SCALE-EXTENT: set extent of dimension
                            if (scaling.isPresentExtent(dim)) {
                                proc = proc + dim + ":\"" + crs + "\"(" + scaling.getExtent(dim).fst
                                        + ":" + scaling.getExtent(dim).snd + "),";
                                axesNumber++;
                            } else {
                                if (lo > hi) {
                                    long temp = lo;
                                    lo = hi;
                                    hi = temp;
                                }
                                proc = proc + dim + ":\"" + crs + "\"(" + lo + ":" + hi + "),";
                            }
                            break;
                    }
                }
            }
            if (axesNumber != scaling.getAxesNumber()) {
                throw new WCSException(ExceptionCode.ScaleAxisUndefined);
            }
            //TODO find out which axis was not found and add the locator to scaleFactor or scaleExtent or scaleDomain
            proc = proc.substring(0, proc.length() - 1);
            proc += "})";

        }
        log.trace(proc); // query after scaling

        if (params != null) {
            // Additional paramters (e.g. bbox/crs in case of GTiff encoding)
            // NOTE: the whole format string is eventually wrapped into quotes (see below)
            format += "\", \"" + params;
        }


        String wcpsQuery = "";
        // If outputCrs is not null then use crsTransform() with this CRS
        if(covMeta.getOutputCrs() != null || covMeta.getSubsettingCrs() != null) {
            String crs = covMeta.getOutputCrs() != null ? covMeta.getOutputCrs() : covMeta.getSubsettingCrs();
            String outputCrsAxes = getOutputCrsAxes(covMeta, crs);
            wcpsQuery =  "for c in (" + req.getCoverageId() + ") return encode( crsTransform( " + proc + ", { " + outputCrsAxes + " }, {} ), \"" + format + "\")";
        } else {
            // Otherwise use the default native CRS without crsTransform()
            wcpsQuery =  "for c in (" + req.getCoverageId() + ") return encode(" + proc + ", \"" + format + "\")";
        }

        log.debug("==========================================================");
        log.debug(wcpsQuery);
        log.debug("==========================================================");
        return Pair.of(wcpsQuery, axes.trim());
    }

    /**
     * If subsettingCrs or outputCrs is used in WCS then add this parameter as outputCrs in WCPS crsTransform() as well
     * @return String
     */
    private String getOutputCrsAxes(GetCoverageMetadata covMeta, String crs) throws WCSException {
        String outputCrs = "AXIS_X: \" "  + crs + " \", AXIS_Y: \" " + crs + " \" ";

        // Then create a 2D output CRS for 2 axes (e.g: Long/E, Lat/N)
        for (DomainElement domainElement:covMeta.getMetadata().getDomainList()) {
            String axisLabel = domainElement.getLabel();
            if (domainElement.getType().equals(AxisTypes.X_AXIS)) {
                outputCrs = outputCrs.replace("AXIS_X", axisLabel);
            } else if (domainElement.getType().equals(AxisTypes.Y_AXIS)) {
                outputCrs = outputCrs.replace("AXIS_Y", axisLabel);
            }
        }

        return outputCrs;
    }

    /**
     * Replaces asterisk in a trimming with real numeric bounds.
     * @param trim  the input subsetting (trimming)
     * @param domEl The axis on which the subset is requested
     */
    private void stars2bounds(DimensionTrim trim, DomainElement domEl) throws WCSException {
        String lo = trim.getTrimLow();
        String hi = trim.getTrimHigh();
        try {
            if (lo.equals(ASTERISK)) {
                if (hi.matches(QUOTED_SUBSET) && TimeUtil.isValidTimestamp(hi)) {
                    // other end of interval is a timestamp: need to make a uniform subset
                    trim.setTrimLow(StringUtil.quote(
                        TimeUtil.coordinate2timestamp(
                            domEl.getMinValue().multiply(domEl.getScalarResolution()).doubleValue(),
                            domEl.getCrsDef().getDatumOrigin(),
                            domEl.getUom())
                        ));
                } else {
                    trim.setTrimLow(domEl.getMinValue().toPlainString());
                }
            }
            if (hi.equals(ASTERISK)) {
                if (lo.matches(QUOTED_SUBSET) && TimeUtil.isValidTimestamp(lo)) {
                    // other end of interval is a timestamp: need to make a uniform subset
                    trim.setTrimHigh(StringUtil.quote(
                         TimeUtil.coordinate2timestamp(
                            domEl.getMaxValue().multiply(domEl.getScalarResolution()).doubleValue(),
                            domEl.getCrsDef().getDatumOrigin(),
                            domEl.getUom())
                         ));
                } else {
                    trim.setTrimHigh(domEl.getMaxValue().toPlainString());
                }
            }
        } catch (PetascopeException ex) {
            log.debug("Error while converting asterisk to time instant equivalent.");
            throw new WCSException(ExceptionCode.InternalComponentError, ex);
        }
    }
}
