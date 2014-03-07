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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.RasdamanException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.exceptions.WCSException;
import petascope.util.BigDecimalUtil;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.Pair;
import petascope.util.StringUtil;
import petascope.util.TimeUtil;
import petascope.util.WcsUtil;
import petascope.util.ras.RasUtil;
import petascope.wcps.server.core.CellDomainElement;
import petascope.wcps.server.core.DomainElement;
import petascope.wcps.server.core.Wcps;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;
import static petascope.wcs2.parsers.GetCoverageRequest.ASTERISK;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionSlice;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionSubset;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionTrim;
import static petascope.wcs2.parsers.GetCoverageRequest.QUOTED_SUBSET;
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
        String axesLabels = "";
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
                            axesLabels += subset.getDimension() + " ";
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
                            lower = WcsUtil.fitToSampleSpace(lower, domainEl, false);
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
                            upper = WcsUtil.fitToSampleSpace(upper, domainEl, true);
                            upperGisDom += upper + " ";
                            // The map is automatically sorted by key value (axis order in the CRS definition)
                            upperDom.put(CrsUtil.getCrsAxisOrder(meta.getCrsUris(), domainEl.getLabel()), upper);
                            // Append updated pixel bounds
                            String decimalsExp = "\\.[0-9]+";
                            long[] cellDom = (CrsUtil.GRID_CRS.equals(subset.getCrs()) || // : subset=x,CRS:1(x1,x2) || subsettingCrs=CRS:1
                                    (request.getCrsExt() != null && CrsUtil.GRID_CRS.equals(request.getCrsExt().getSubsettingCrs())))
                                    ? new long[] { // NOTE: e.g. parseInt("10.0") throws exception: need to remove decimals.
                                        Integer.parseInt(trimLow.replaceAll( decimalsExp, "").trim()),
                                        Integer.parseInt(trimHigh.replaceAll(decimalsExp, "").trim())} // subsets are alsready grid indexes
                                    : CrsUtil.convertToInternalGridIndices(m.getMetadata(), dbMeta, domainEl.getLabel(),
                                        trimLow,   !trimLow.matches(QUOTED_SUBSET),
                                        trimHigh, !trimHigh.matches(QUOTED_SUBSET));
                            // In any case, properly trim the bounds by the image extremes
                            int cellDomainElLo = cellDomainEl.getLoInt();
                            int cellDomainElHi = cellDomainEl.getHiInt();
                            lowerCellDom += (cellDomainElLo > cellDom[0]) ? cellDomainElLo + " " : cellDom[0] + " ";
                            upperCellDom += (cellDomainElHi < cellDom[1]) ? cellDomainElHi + " " : cellDom[1] + " ";

                        } else if (subset instanceof DimensionSlice) {
                            log.info("Axis " + domainEl.getLabel() + " has been sliced: remove it from the boundedBy element and track the axis for CRS slicing.");
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
                axesLabels += domainEl.getLabel() + " ";
                lowerCellDom += cellDomainEl.getLo() + " ";
                upperCellDom += cellDomainEl.getHi() + " ";
                lowerGisDom += BigDecimalUtil.stripDecimalZeros(domainEl.getMinValue()) + " ";
                upperGisDom += BigDecimalUtil.stripDecimalZeros(domainEl.getMaxValue()) + " ";
                // The map is automatically sorted by key value (axis order in the CRS definition)
                lowerDom.put(
                        CrsUtil.getCrsAxisOrder(meta.getCrsUris(), domainEl.getLabel()),
                        BigDecimalUtil.stripDecimalZeros(domainEl.getMinValue()).toPlainString());
                upperDom.put(
                        CrsUtil.getCrsAxisOrder(meta.getCrsUris(), domainEl.getLabel()),
                        BigDecimalUtil.stripDecimalZeros(domainEl.getMaxValue()).toPlainString());
            }
        } // END domains iterator

        // Update axes labels
        m.setAxisLabels(axesLabels);
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
            CoverageMetadata covmeta, DbMetadataSource meta, String format, String params)
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
        try {
            pair = constructWcpsQuery(request, meta, covmeta, format, params);
            rquery = RasUtil.abstractWCPSToRasql(pair.fst, wcps);
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
     * @param cov coverage metadata
     * @param dbMeta
     * @param format
     * @param params
     * @return (WCPS query in abstract syntax, axes)
     * @throws WCSException
     * @throws PetascopeException
     */
    protected Pair<String, String> constructWcpsQuery(GetCoverageRequest req, DbMetadataSource dbMeta,
            CoverageMetadata cov, String format, String params)
            throws WCSException, PetascopeException {
        String axes = "";
        //keep a list of the axes defined in the coverage
        ArrayList<String> axesList = new ArrayList<String>();
        Iterator<DomainElement> dit = cov.getDomainIterator();
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
            DomainElement de = cov.getDomainByName(dim);

            //Check if the supplied axis is in the coverage axes and throw exception if not
            if (!axesList.contains(dim)) {
                throw new WCSException(ExceptionCode.InvalidAxisLabel,
                        "The axis label " + dim + " was not found in the list of available axes");
            }

            // Parametrized CRSs can have quotes and other reserved entities which break abstract WCPS queries (and XML)
            String crs = StringUtil.escapeXmlPredefinedEntities(de.getNativeCrs());

            if (subset instanceof DimensionTrim) {
                DimensionTrim trim = (DimensionTrim) subset;
                proc = "trim(" + proc + ",{" + dim + ":\"" + crs + "\" ("
                        + trim.getTrimLow() + ":" + trim.getTrimHigh() + ")})";
                newdim.put(dim, new Pair(trim.getTrimLow(), trim.getTrimHigh()));

            } else if (subset instanceof DimensionSlice) {
                DimensionSlice slice = (DimensionSlice) subset;
                proc = "slice(" + proc + ",{" + dim + ":\"" + crs + "\" (" + slice.getSlicePoint() + ")})";
                newdim.put(dim, new Pair(slice.getSlicePoint(), slice.getSlicePoint()));
                log.debug("Dimension" + dim);
                log.debug(axes);
                axes = axes.replaceFirst(dim + " ?", ""); // remove axis
            }
        }

        if (req.isScaled()) {
            if (!WcsUtil.isGrid(cov.getCoverageType())) {
                throw new WCSException(ExceptionCode.InvalidCoverageType.locator(req.getCoverageId()));
            }
            Scaling s = req.getScaling();
            int axesNumber = 0; // for checking if all axes in the query were used
            proc = "scale(" + proc + ", {";
            Iterator<DomainElement> it = cov.getDomainIterator();
            Iterator<CellDomainElement> cit = cov.getCellDomainIterator();
            while (it.hasNext() && cit.hasNext()) {
                DomainElement el = it.next();
                CellDomainElement cel = cit.next();
                long lo = cel.getLoInt();
                long hi = cel.getHiInt();
                String dim = el.getLabel();
                String crs = el.getNativeCrs();
                if (newdim.containsKey(dim)) {
                    long[] lohi = CrsUtil.convertToInternalGridIndices(cov, dbMeta, dim,
                            newdim.get(dim).fst, req.getSubset(dim).isNumeric(),
                            newdim.get(dim).snd, req.getSubset(dim).isNumeric());
                    lo = lohi[0];
                    hi = lohi[1];
                }
                switch (s.getType()) {
                    case 1:
                            proc = proc + dim + ":\"" + crs + "\"(" + Math.round(Math.floor(lo/s.getFactor()))
                                    + ":" + Math.round(Math.floor(hi/s.getFactor())) + "),";
                    break;
                    case 2:
                        if (s.isPresentFactor(dim)) {
                            proc = proc + dim + ":\"" + crs + "\"(" + Math.round(Math.floor(lo/s.getFactor(dim)))
                                    + ":" + Math.round(Math.floor(hi/s.getFactor(dim))) + "),";
                            axesNumber++;
                        } else {
                            proc = proc + dim + ":\"" + crs + "\"(" + lo + ":" + hi + "),";
                        }
                    break;
                    case 3:
                        if (s.isPresentSize(dim)) {
                            proc = proc + dim + ":\"" + crs + "\"(" + lo
                                    + ":" + (lo + s.getSize(dim)-1) + "),";
                            axesNumber++;
                        } else {
                            proc = proc + dim + ":\"" + crs + "\"(" + lo + ":" + hi + "),";
                        }
                    break;
                    case 4:
                        if (s.isPresentExtent(dim)) {
                            proc = proc + dim + ":\"" + crs + "\"(" + s.getExtent(dim).fst
                                    + ":" + s.getExtent(dim).snd + "),";
                            axesNumber++;
                        } else {
                            proc = proc + dim + ":\"" + crs + "\"(" + lo + ":" + hi + "),";
                        }
                    break;
                }
            }
            if (axesNumber != s.getAxesNumber()) {
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

        String query = "for c in (" + req.getCoverageId() + ") return encode(" + proc + ", \"" + format + "\")";
        log.debug("==========================================================");
        log.debug(query);
        log.debug("==========================================================");
        return Pair.of(query, axes.trim());
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
