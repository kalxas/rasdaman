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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.util.ListUtil;
import petascope.util.Pair;
import petascope.util.StringUtil;
import petascope.util.ras.RasUtil;
import petascope.wcps.server.core.CellDomainElement;
import petascope.wcps.server.core.DomainElement;
import petascope.wcps.server.core.Wcps;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionSlice;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionSubset;
import petascope.wcs2.parsers.GetCoverageRequest.DimensionTrim;
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
     * Update m with the correct bounds and axes (mostly useful when there's
     * slicing/trimming in the request)
     */
    protected void updateGetCoverageMetadata(GetCoverageRequest request, GetCoverageMetadata m)
            throws PetascopeException, SecoreException, WCSException {

        // Init variables, to be then filled scanning the request subsets
        String axesLabels = "";
        String uomLabels = "";
        String lowerDom = "";
        String upperDom = "";
        String lowerCellDom = "";
        String upperCellDom = "";
        // CRS need to be sliced accordingly upon dimension slicings
        String crsName;
        Set<String> slicedAxes = new HashSet<String>();
        boolean domUpdated;
        CoverageMetadata meta = m.getMetadata();
        Iterator<DomainElement>         domsIt = meta.getDomainIterator();
        Iterator<CellDomainElement> cellDomsIt = meta.getCellDomainIterator();
        DomainElement domainEl;
        CellDomainElement cellDomainEl;
        List<DimensionSubset> subsList = request.getSubsets();
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
                            // Append axis/uom label
                            axesLabels += subset.getDimension() + " ";
                            uomLabels  += domainEl.getUom() + " ";
                            // Append updated bounds
                            // TODO: if request is specified via grid coords, need a backwards transform here
                            //       {cellDomain->domain} to show domain values in the WCS response:
                            //       Crs.convertToDomainCoords()
                            if (((DimensionTrim)subset).getTrimLow().contains("\"")) {
                                // TODO convert to domain values (TimeUtil.countPixels(datumOrigin, stringLo, axisUoM);
                                // ...
                            } else {
                                lowerDom += new BigDecimal(Math.max(
                                    Double.parseDouble(((DimensionTrim) subset).getTrimLow()),
                                    domainEl.getMinValue().doubleValue())).toPlainString() + " ";
                            }
                            if (((DimensionTrim)subset).getTrimHigh().contains("\"")) {
                                // TODO convert to domain values (TimeUtil.countPixels(datumOrigin, stringLo, axisUoM);
                                // ...
                            } else {
                                upperDom += new BigDecimal(Math.min(
                                    Double.parseDouble(((DimensionTrim) subset).getTrimHigh()),
                                    domainEl.getMaxValue().doubleValue())).toPlainString() + " ";
                            }
                            // Append updated pixel bounds
                            String decimalsExp = "\\.[0-9]+";
                            long[] cellDom = (CrsUtil.GRID_CRS.equals(subset.getCrs()) || // : subset=x,CRS:1(x1,x2) || subsettingCrs=CRS:1
                                    (request.getCrsExt() != null && CrsUtil.GRID_CRS.equals(request.getCrsExt().getSubsettingCrs())))
                                    ? new long[] { // NOTE: e.g. parseInt("10.0") throws exception: need to remove decimals.
                                        Integer.parseInt(((DimensionTrim) subset).getTrimLow().replaceAll( decimalsExp, "").trim()),
                                        Integer.parseInt(((DimensionTrim) subset).getTrimHigh().replaceAll(decimalsExp, "").trim())} // subsets are alsready grid indexes
                                    : new long[] {
                                        toPixels(Double.parseDouble(((DimensionTrim) subset).getTrimLow()),  domainEl, cellDomainEl), // otherwise, need to convert them
                                        toPixels(Double.parseDouble(((DimensionTrim) subset).getTrimHigh()), domainEl, cellDomainEl)
                                    };
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
                    } catch (PetascopeException ex) {
                        throw ex;
                    }
                }
            } // END subsets iterator
            if (!domUpdated) {
                // This dimension is not involved in any subset: use bbox bounds
                axesLabels += domainEl.getLabel() + " ";
                uomLabels  += domainEl.getUom()   + " ";
                lowerDom   += domainEl.getMinValue() + " ";
                upperDom   += domainEl.getMaxValue() + " ";
                lowerCellDom += cellDomainEl.getLo() + " ";
                upperCellDom += cellDomainEl.getHi() + " ";
            }
        } // END domains iterator

        // Update axes labels
        m.setAxisLabels(axesLabels);
        // Update **pixel-domain** bounds
        m.setLow(lowerCellDom);
        m.setHigh(upperCellDom);
        // Update **domain** bounds
        m.setDomLow(lowerDom);
        m.setDomHigh(upperDom);
        // Update the **CRS**
        if (!slicedAxes.isEmpty()) {
            crsName = CrsUtil.sliceAxesOut(meta.getCrsUris(), slicedAxes);
            m.setCrs(crsName);
            m.setUomLabels(uomLabels);
        }
    }

    /**
     * Execute rasql query, given GetCoverage request, request metadata, and
     * format of result. Request subsets values are pre-transformed if necessary
     * (e.g. CRS reprojection, timestamps).
     *
     * @return (result of executing the query, axes)
     * @throws WCSException
     */
    protected Pair<Object, String> executeRasqlQuery(GetCoverageRequest request,
            GetCoverageMetadata m, DbMetadataSource meta, String format, String params)
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
            pair = constructWcpsQuery(request, m.getMetadata(), format, params);
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
     * Method for converting CRS coordinates to pixel coordinates without checking limits (like in wcps.server.core.CRS.java)
     *
     * @param val the value to be converted
     * @param el the DomainElement for the dimension
     * @param cel the CellDomainElement for the dimension
     * @return the corresponding CRS coordinates
     */
    private long toPixels(double lo, DomainElement domEl, CellDomainElement cdomEl) {

        // Get cellDomain extremes
        long pxMin = cdomEl.getLoInt();
        long pxMax = cdomEl.getHiInt();

        // Get Domain extremes (real sdom)
        double domMin = domEl.getMinValue().doubleValue();
        double domMax = domEl.getMaxValue().doubleValue();

        // Get offset vector (= cell width in case of regular axis):
        double cellWidth = domEl.getOffsetVector().doubleValue(); // (domHi-domLo)/(double)((pxHi-pxLo)+1);

        // Conversion to pixel domain
        long indexValLo;
        long indexValHi;
        if (!domEl.getLabel().equals(AxisTypes.Y_AXIS)) {
            indexValLo = (long)Math.floor((lo - domMin) / cellWidth) + pxMin;
            //indexValHi = (long)Math.floor((hi - domMin) / cellWidth) + pxMin;
        } else {
            //indexValLo = (long)Math.floor((domMax - hi) / cellWidth) + pxMin;
            //indexValHi = (long)Math.floor((domMax - lo) / cellWidth) + pxMin;
            indexValLo = (long)Math.floor((domMax - lo) / cellWidth) + pxMin;
        }

        // If axis is irregular, the "pixel" domain needs to be translated
        if (!domEl.getCoefficients().isEmpty()) {
            indexValLo = ListUtil.minIndex(domEl.getCoefficients(), new BigDecimal(indexValLo));
            //indexValHi = ListUtil.minIndex(domEl.getCoefficients(), new BigDecimal(indexValHi));
        }

        // Check outside bounds:
        //indexValLo = (indexValLo<pxMin) ? pxMin : ((indexValLo>pxMax)?pxMax:indexValLo);
        //indexValHi = (indexValHi<pxMin) ? pxMin : ((indexValHi>pxMax)?pxMax:indexValHi);

        //return new long[]{indexValLo, indexValHi};
        return indexValLo;
    }

    /**
     * Given a GetCoverage request, construct an abstract WCPS query.
     *
     * @param req GetCoverage request
     * @param cov coverage metadata
     * @return (WCPS query in abstract syntax, axes)
     */
    protected Pair<String, String> constructWcpsQuery(GetCoverageRequest req, CoverageMetadata cov, String format, String params)
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
        /**
         * NOTE: trims and slices are nested in each dimension: this inhibits
         * WCPS subsetExpr to actually accept CRS != Native CRS of Image, since
         * both X and Y coordinates need to be known at time of reprojection:
         * CRS reprojection is hence done a priori, but this does not hurt that
         * much actually.
         */
        for (DimensionSubset subset : req.getSubsets()) {
            String dim = subset.getDimension();
            DomainElement de = cov.getDomainByName(dim);

            //Check if the supplied axis is in the coverage axes and throw exception if not
            if (!axesList.contains(dim)) {
                throw new WCSException(ExceptionCode.InvalidAxisLabel,
                        "The axis label " + dim + " was not found in the list of available axes");
            }

            // Parametrized CRSs can have quotes and other reserved entities which break abstract WCPS queries (and XML)
            String crs = StringUtil.escapeXmlPredefinedEntities(de.getCrs());

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
            if (!((cov.getCoverageType().equals("GridCoverage")) ||
                    (cov.getCoverageType().equals("RectifiedGridCoverage")) ||
                    (cov.getCoverageType().equals("ReferenceableGridCoverage")))) {
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
                String crs = CrsUtil.GRID_CRS;
                if (newdim.containsKey(dim)) {
                    lo = toPixels(Double.parseDouble(newdim.get(dim).fst), el, cel);
                    hi = toPixels(Double.parseDouble(newdim.get(dim).snd), el, cel);
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
}
