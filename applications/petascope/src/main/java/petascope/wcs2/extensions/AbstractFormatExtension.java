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
import petascope.ConfigManager;
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
import petascope.util.CrsProjectionUtil;
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
import static petascope.wcs2.parsers.GetCoverageRequest.Scaling.SupportedTypes.SCALE_EXTENT;
import static petascope.wcs2.parsers.GetCoverageRequest.Scaling.SupportedTypes.SCALE_SIZE;

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
    public void updateGetCoverageMetadata(GetCoverageRequest request, GetCoverageMetadata m, DbMetadataSource dbMeta)
    throws PetascopeException, SecoreException, WCSException {

        // Init variables, to be then filled scanning the request subsets.

        // Grid axis labels, and grid bounds : grid (rasdaman) order
        Map<Integer, String> axesLabels = new TreeMap<Integer, String>();
        String lowerCellDom = "";
        String upperCellDom = "";
        // Tuples of external CRS bounds : CRS order
        // Create a key-ordered map {axisLabel->dom} so that the correct order can be reconstructed automatically.
        Map<Integer, String> lowerDom = new TreeMap<Integer, String>();
        Map<Integer, String> upperDom = new TreeMap<Integer, String>();
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
        List<DimensionSubset> subsList = request.getSubsets();

        // NOTE: sourceCrs can be from subsettingCrs parameter
        String sourceCrs = m.getSubsettingCrs();
        // or extract from subset parameter
        // (only for X, Y axes: e.g: subset=E,http://www.opengis.net/def/crs/EPSG/0/3857(-1.3637472939075228E7,-1.3636585328807762E7))
        if (StringUtils.isEmpty(sourceCrs)) {
            List<String> crsList = new ArrayList<String>();
            boolean hasCrsParam = false;
            for (DimensionSubset subset : subsList) {
                // subset can be (subset=E,http://../3857,(0:100) or subset=Long,CRS:1(400:401))
                if (!StringUtils.isEmpty(subset.getCrs()) && !subset.getCrs().equals(CrsUtil.GRID_CRS)) {
                    hasCrsParam = true;
                    crsList.add(subset.getCrs());
                }
            }

            // Request has CRS parameter in subset
            if (hasCrsParam) {
                if (crsList.size() != 2) {
                    throw new WCSException(ExceptionCode.InvalidRequest, "Only support 2 subsets with geo-referenced CRS parameters.");
                } else {
                    if (!crsList.get(0).equals(crsList.get(1))) {
                        throw new WCSException(ExceptionCode.InvalidRequest, "Subsets are requested with different geo-referenced CRS parameters.");
                    }
                    // then can consider the CRS parameters in subset as same as subsettingCRS
                    sourceCrs = crsList.get(0);
                }
            }
        }

        // subsettingCrs is not null, then need to translate the subset from subsettingCrs to nativeCrs
        if (!StringUtils.isEmpty(sourceCrs)) {
            this.updateSubsetsListByCrs(domsIt, subsList, sourceCrs);
            // reset the iterator to first position.
            domsIt = meta.getDomainIterator();
            // after translating the subsets from subsettingCrs to nativeCrs, subsettingCrs
        }
       
               
        // NOTE: OGC CITE scale (req:14, 15 tests on axes and their orders must be as same as from CRS order)
        // i.e: <GridEnvelop> </GridEnvelop> for coverage imported with CRS: 4326 will need to rewrite as Lat, Long as from CRS not by default Long, Lat (as stored in Rasdaman).
        // Only when testing OGC CITE
        if (ConfigManager.OGC_CITE_OUTPUT_OPTIMIZATION) {
            this.reorderGridAxesByCrsOrder(meta);
        }
        
        List<DomainElement> domainElements = meta.getDomainList();
        List<CellDomainElement> cellDomainElements = meta.getCellDomainList();

        // NOTE: single loop since still N=M for Petascope, being N = dim(grid) and M=dim(CRS).
        // Keep domainElement order (grid order), but need to re-order the coordinates in the tuple for lowerDom/upperDom
        for (int i = 0; i < domainElements.size(); i++) {
            // Check if one subset trims on /this/ dimension:
            // Order and quantity of subsets not necessarily coincide with domain of the coverage
            // (e.g. single subset on Y over a nD coverage)
            domUpdated = false;
            DomainElement domainEl     = domainElements.get(i);
            CellDomainElement cellDomainEl = cellDomainElements.get(i);
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
                                                 Integer.parseInt(trimLow.replaceAll(decimalsExp, "").trim()),
                                                 Integer.parseInt(trimHigh.replaceAll(decimalsExp, "").trim())
                                             } // subsets are already grid indexes
                                             : CrsUtil.convertToInternalGridIndices(m.getMetadata(), dbMeta, domainEl.getLabel(),
                                                     trimLow,   !trimLow.matches(QUOTED_SUBSET),
                                                     trimHigh, !trimHigh.matches(QUOTED_SUBSET));
                            // If SCALING on this dimension, fix upperCellDom and offset vector by scale factor
                            // In any case, properly trim the bounds by the image extremes
                            int cellDomainElLo = cellDomainEl.getLoInt();
                            // If axis is not scaled then it will keep the original scale domain
                            long cellDomainElHi = this.domainElementScaleHandle(request, m, domainEl, cellDomainEl);

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
                // If axis is not scaled then it will keep the original scale domain
                long hiCellDom = this.domainElementScaleHandle(request, m, domainEl, cellDomainEl);

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
     *
     * Check if domainElement is scaled then calculate the new interval values correctly
     * @param m
     * @param cellDomainEl
     * @return highCellDom
     */
    private long domainElementScaleHandle(GetCoverageRequest request, GetCoverageMetadata m,
                                          DomainElement domainEl, CellDomainElement cellDomainEl) throws WCSException {
        long loCellDom = cellDomainEl.getLoInt();
        long hiCellDom = cellDomainEl.getHiInt();
        Scaling scaling = request.getScaling();
        String axisLabel = domainEl.getLabel();
        if (scaling.isScaled(axisLabel)) {
            if (scaling.getType() == SCALE_SIZE) {
                // NOTE: Scalesize means scale to grid pixels, so just apply scale on the axes (i.e: SCALESIZE=Lat(1.0),Long(2.0)) then output should be Lat with 1 pixel and Long with 2 pixels.
                // it cannot be 0 for pixel
                ScalingExtension.validateScalePositive(new BigDecimal(scaling.getSize(axisLabel)));
                hiCellDom = (long) (scaling.getSize(axisLabel)) - 1;
            } else if (scaling.getType() == SCALE_EXTENT) {
                // i.e: SCALEEXTENT=Lat(10:20),Long(100:200) then Lat's grid domain is: 0:10
                long size = scaling.getExtent(axisLabel).snd - scaling.getExtent(axisLabel).fst;
                hiCellDom = size;
            } else {
                // SCALE_FACTOR or SCALE_AXES
                BigDecimal scalingFactor = ScalingExtension.computeScalingFactor(scaling, axisLabel, BigDecimal.valueOf(loCellDom), BigDecimal.valueOf(hiCellDom));
                hiCellDom = this.calculateHighCellDomScaleFactor(loCellDom, hiCellDom, scalingFactor);
                // [!] NOTE: do *not* use domainEl.setScalarResolution since world2pixel conversions are cached.
                m.setScalingFactor(axisLabel, scalingFactor);
            }
        }
        return hiCellDom;
    }

    /**
     * Calculate the highCellDom when axis is scaled by ScaleFactor
     * @param  loCellDom
     * @param hiCellDom
     * @param scalingFactor
     * @return
     */
    private long calculateHighCellDomScaleFactor(long loCellDom, long hiCellDom, BigDecimal scalingFactor) {
        BigDecimal pixels = new BigDecimal((hiCellDom - loCellDom + 1) / scalingFactor.floatValue());
        // If it is >= 0.5 pixel then shift it to 1 (OGC CITE test SCALE req 13)
        BigDecimal fraction = pixels.remainder(BigDecimal.ONE);

        if (fraction.compareTo(new BigDecimal(0.5)) >= 0) {
            pixels = pixels.add(BigDecimal.ONE);
        }

        long scaledExtent = pixels.toBigInteger().longValue();
        hiCellDom = loCellDom + scaledExtent - 1;
        return hiCellDom;
    }

    /**
     * Translate from WCS query to WCPS then it will execute rasql query.
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
    protected Pair<Object, String> executeWcsRequest(GetCoverageRequest request,
            GetCoverageMetadata covmeta, DbMetadataSource meta, String format, String params)
    throws PetascopeException, RasdamanException, WCPSException, WCSException {

        // WCS -> WCPS then use WCPS 1.5 to process the generated WCPS query
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
        List<String> axesList = new ArrayList<String>();
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
            String nativeCrs = StringUtil.escapeXmlPredefinedEntities(de.getNativeCrs());

            // in-subset CRS specification (standard inconsistency CRS hanlding in KVP/XML: see OGC 12-167 change request #257)
            // accept direct internal index subsets even if no CRS extension is provided (this is not a geo-reprojection)
            if (null != subset.getCrs() && CrsUtil.isGridCrs(subset.getCrs())) {
                nativeCrs = subset.getCrs(); // replace native with grid crs
            }

            if (subset instanceof DimensionTrim) {
                DimensionTrim trim = (DimensionTrim) subset;
                proc = "trim(" + proc + ",{" + dim + ":\"" + nativeCrs + "\" ("
                       + trim.getTrimLow() + ":" + trim.getTrimHigh() + ")})";
                newdim.put(dim, new Pair(trim.getTrimLow(), trim.getTrimHigh()));

            } else if (subset instanceof DimensionSlice) {
                DimensionSlice slice = (DimensionSlice) subset;
                proc = "slice(" + proc + ",{" + dim + ":\"" + nativeCrs + "\" (" + slice.getSlicePoint() + ")})";
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
                    // Note: scalefactor value: 1.0 leaves the coverage unscaled, scalefactor value: between 0 and 1 scales down
                    // (reduces target domain), scalefactor value: greater than 1 scales up (enlarges target domain).
                    BigDecimal pixels;
                    BigDecimal fraction;
                    BigDecimal scalingFactor;
                    switch (scaling.getType()) {
                        case SCALE_FACTOR:
                            // SCALE-BY-FACTOR: divide extent by global scaling factor
                            scalingFactor = covMeta.getScalingFactor(el.getLabel());
                            hiAfterScale = calculateHighCellDomScaleFactor(lo, hi, scalingFactor);
                            if (lo > hiAfterScale) {
                                long temp = lo;
                                lo = hiAfterScale;
                                hiAfterScale = temp;
                            }
                            proc = proc + dim + ":\"" + crs + "\"(" + lo
                                    + ":" + hiAfterScale + "),";
                            break;
                        case SCALE_AXIS:
                            // SCALE-AXES: divide extent by axis scaling factor
                            if (scaling.isPresentFactor(dim)) {
                                scalingFactor = covMeta.getScalingFactor(el.getLabel());
                                hiAfterScale = calculateHighCellDomScaleFactor(lo, hi, scalingFactor);

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
                        case SCALE_SIZE:
                            // SCALE-SIZE: set extent of dimension
                            if (scaling.isPresentSize(dim)) {
                                hiAfterScale = (lo + scaling.getSize(dim) - 1);
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
                        case SCALE_EXTENT:
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
        // TODO: crstranform() only support to translate 2D coverage, consider encode (crstranform(3D) in csv) as it has error, need to fix when it is possible.
        if (covMeta.getOutputCrs() != null || covMeta.getSubsettingCrs() != null) {
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
        for (DomainElement domainElement : covMeta.getMetadata().getDomainList()) {
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

    /**
     * When WCS request has parameters: subsettingCrs then it will need both X, Y
     * axes translated from subsettingCrs to coverage's nativeCrs.
     * NOTE: Must translate by pair (X,Y), e.g: (Lat, Long) not by subset (X1, X2), e.g: (Long1, Long2)
     * as it will have large error in coordinates when translate to UTM (e.g: 32633).
     */
    private void updateSubsetsListByCrs(Iterator<DomainElement> domainElements, List<DimensionSubset> subsList, String sourceCrs) throws WCSException {
        DomainElement domainElement;
        List<String> subsetX = new ArrayList<String>();
        List<String> subsetY = new ArrayList<String>();

        int indexX = -1;
        int indexY = -1;

        String targetCrs = null;

        // Get the subset from X and Y axes which is projected in subsettingCrs (e.g: E(0,20), N(25:30) & subsettingCrs=EPSG:3857)
        while (domainElements.hasNext()) {
            domainElement = domainElements.next();

            // Loop through each subsets in the request and check if this axis is involved
            Iterator<DimensionSubset> subsIt = subsList.iterator();
            DimensionSubset subset;
            int index = 0;
            while (subsIt.hasNext()) {
                subset = subsIt.next();
                if (subset.getDimension().equals(domainElement.getLabel())) {
                    if (domainElement.getType().equals(AxisTypes.X_AXIS)) {
                        subsetX = this.parseSubsets(subset);
                        targetCrs = domainElement.getNativeCrs();
                        indexX = index;
                    } else if (domainElement.getType().equals(AxisTypes.Y_AXIS)) {
                        subsetY = this.parseSubsets(subset);
                        indexY = index;
                    }
                }
                index++;
            }
        }

        // validate subsets
        if (subsetX.size() != subsetY.size()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "SubsettingCrs requires X and Y subsets have same type (trimming/slicing).");
        } else if (subsetX.isEmpty()) {
            throw new WCSException(ExceptionCode.InvalidRequest, "SubsettingCrs requires X and Y subsets as parameters in request.");
        }

        // Translate from subsettingCrs to nativeCrs.
        this.translateSubsetsByCrs(subsetX, subsetY, sourceCrs, targetCrs);

        // Update the translated subsets to subsets list.
        this.updateSubsetsList(subsList, subsetX, subsetY, indexX, indexY);
    }

    /**
     * Translate subsets from subsettingCrs to nativeCrs.
     * @param subsetX
     * @param subsetY
     * @param sourceCrs
     * @param targetCrs
     * @throws WCSException
     */
    private void translateSubsetsByCrs(List<String> subsetX, List<String> subsetY,
                                       String sourceCrs, String targetCrs) throws WCSException {
        // if both of X, Y subsets are trimming or slicing, then translate from sourceCrs to targetCrs
        for (int i = 0; i < subsetX.size(); i++) {
            String x = subsetX.get(i);
            String y = subsetY.get(i);

            double[] srcCoords = new double[] { Double.parseDouble(x), Double.parseDouble(y) };
            List<BigDecimal> output = CrsProjectionUtil.transform(sourceCrs, targetCrs, srcCoords, false);
            String newX = output.get(0).toPlainString();
            String newY = output.get(1).toPlainString();

            // update the translated subset X,Y
            subsetX.set(i, newX);
            subsetY.set(i, newY);
        }
    }

    /**
     * After we translated the subsets from subsettingCrs to nativeCrs, this method will update the input subsList with new values.
     *
     */
    private void updateSubsetsList(List<DimensionSubset> subsList, List<String> subsetX, List<String> subsetY,
                                   int indexX, int indexY) throws WCSException {

        // update the DimensionSubset list
        if (subsetX.size() == 1) {
            // slicing in subset (e.g: E(75042.727))

            DimensionSlice sliceX = ((DimensionSlice)subsList.get(indexX));
            sliceX.setSlicePoint(subsetX.get(0));
            // replace the subset coordinate from subettingCrs to nativeCrs
            subsList.set(indexX, sliceX);

            DimensionSlice sliceY = ((DimensionSlice)subsList.get(indexY));
            sliceY.setSlicePoint(subsetY.get(0));
            // replace the subset coordinate from subettingCrs to nativeCrs
            subsList.set(indexY, sliceY);
        } else {
            // trimming in subset (e.g: E(75042.727, 705042.727))

            DimensionTrim trimX = ((DimensionTrim)subsList.get(indexX));
            trimX.setTrimLow(subsetX.get(0));
            trimX.setTrimHigh(subsetX.get(1));
            // replace the subset coordinate from subettingCrs to nativeCrs
            subsList.set(indexX, trimX);

            DimensionTrim trimY = ((DimensionTrim)subsList.get(indexY));
            trimY.setTrimLow(subsetY.get(0));
            trimY.setTrimHigh(subsetY.get(1));
            // replace the subset coordinate from subettingCrs to nativeCrs
            subsList.set(indexY, trimY);
        }
    }

    /**
     * Parse the trimming / slicing subset to list.
     * @param subset
     * @return
     */
    private List<String> parseSubsets(DimensionSubset subset) {
        List<String> subsets = new ArrayList<String>();
        if (subset instanceof DimensionTrim) {
            String trimLow = ((DimensionTrim)subset).getTrimLow();
            String trimHigh = ((DimensionTrim)subset).getTrimHigh();
            subsets.add(trimLow);
            subsets.add(trimHigh);
        } else if (subset instanceof DimensionSlice) {
            String slicePoint = ((DimensionSlice)subset).getSlicePoint();
            subsets.add(slicePoint);
        }
        return subsets;
    }
    
    /**
     * OGC CITE scale (req:14, 15 test on grid axes and their orders must be as same as from CRS order)
     * i.e: Coverage imported by EPSG:4326 should be listed lat, long in <GridAxis> not by long, lat by default.
     * @param meta
     * @throws PetascopeException
     * @throws SecoreException 
     */
    private void reorderGridAxesByCrsOrder(CoverageMetadata meta) throws PetascopeException, SecoreException {
        List<DomainElement> domainElementsTmp = new ArrayList<DomainElement>();
        List<CellDomainElement> cellDomainElementsTmp = new ArrayList<CellDomainElement>();

        // Iterate the axes by the CRS's order and reorder <GridEvenlope> </GridEnvelope> accordingly.
        List<String> axisLabels = CrsUtil.getAxesLabels(meta.getCrsUris());
        for (String axisLabel:axisLabels) {
            DomainElement domElTmp = meta.getDomainByName(axisLabel);
            domainElementsTmp.add(domElTmp);
            CellDomainElement cellDomElTmp = meta.getCellDomainByName(axisLabel);
            cellDomainElementsTmp.add(cellDomElTmp);
        }

        // Set the ordered lists by CRS back to the original lists
        meta.setDomain(domainElementsTmp);
        meta.setCellDomain(cellDomainElementsTmp);            
    }
}
