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
package petascope.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import nu.xom.Document;
import nu.xom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.ConfigManager;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.exceptions.WCSException;
import petascope.ows.Description;
import petascope.swe.datamodel.AbstractSimpleComponent;
import petascope.swe.datamodel.NilValue;
import petascope.swe.datamodel.Quantity;
import petascope.util.*;

import petascope.wcps.metadata.Bbox;
import petascope.wcps.metadata.CellDomainElement;
import petascope.wcps.metadata.DomainElement;
import petascope.wcps.server.core.InterpolationMethod;
import petascope.wcps.server.core.RangeElement;
import petascope.wcs.server.core.TimeString;

/**
 * This class implements coverage metadata. For information on what each field
 * means, see the W*S standard. This class provides extensive error checking
 * and well as various utility functions.
 *
 * TODO : class which takes DomainSet/RangeSet/RangeType objects, which in turn would
 * be extended by eg. GriddedDomainSet or MultiPointDomainSet, for general gml:coverage metadata.
 */
public class CoverageMetadata implements Cloneable {

    private static Logger log = LoggerFactory.getLogger(CoverageMetadata.class);

    private int coverageId = -1;    // Used when reading metadata from the DB
    private List<CellDomainElement> cellDomain;
    private List<DomainElement>     domain;
    private String coverageName;
    private String coverageType;
    private String nativeFormat;
    private List<String> crsUris; // 1+ single CRS URIs
    private Set<Pair<String,String>> extraMetadata; // {metadata_type,metadata_value}
    private List<RangeElement> range;
    private List<AbstractSimpleComponent> sweComponents;
    Pair<BigInteger, String> rasdamanCollection;
    //rasdaman collection type information mddType:collectionType
    private String rasdamanCollectionType;
    private Bbox bbox = null;
    private Description owsDescription = null;
    private List<BigDecimal> gridOrigin;
    private LinkedHashMap<List<BigDecimal>, BigDecimal> gridAxes;

    // legacy petascope.wcs
    private InterpolationMethod      interpolationDefault;
    private Set<InterpolationMethod> interpolationSet;
    private String      nullDefault;
    private Set<String> nullSet = new HashSet<String>();

    // Overload for empty metadata object
    public CoverageMetadata() {}

    // Constructor overload: when domain is given by origin plus offset-vector
    public CoverageMetadata(
            String                        coverageName,
            String                        coverageType,
            String                        nativeFormat,
            Set<Pair<String,String>>      extraMeta,
            List<Pair<CrsDefinition.Axis,String>> crsAxes, // axis -> URI
            List<CellDomainElement>       cellDomain,
            List<BigDecimal>              gridOrigin,
            LinkedHashMap<List<BigDecimal>,BigDecimal> gridAxes, // offsetVector -> coefficients (must be LinkedHash: preserve order of insertion)
            Pair<BigInteger, String>      rasdamanCollection,
            List<Pair<RangeElement,Quantity>> rangeElementQuantities
            ) throws PetascopeException, SecoreException {
        this.gridOrigin = gridOrigin;
        this.gridAxes = gridAxes;
        // Build domain elements from origin and vectors
        // Note: i-th grid axis need not be aligned with i-th CRS axis
        List<DomainElement> domainElements = new ArrayList<DomainElement>();
        Iterator<CellDomainElement> cDom   = cellDomain.iterator();
        int axisOrder = 0;

        for (Entry<List<BigDecimal>,BigDecimal> axis : gridAxes.entrySet()) {
            // Check consistency: (vectors || CRS axis) [rotated grids not yet supported]
            List<Integer> axisNonZeroIndices = Vectors.nonZeroComponentsIndices(
                    axis.getKey().toArray(new BigDecimal[axis.getKey().size()])
                    );
            if (axisNonZeroIndices.size() > 1) {
                throw new PetascopeException(ExceptionCode.UnsupportedCoverageConfiguration,
                        axis.getKey() + " offset vector: currently only CRS-aligned offset-vectors are supported.");
            }

            // Get the correspondent CRS axis definition
            boolean isIrregular = !(null == axis.getValue());
            DomainElement domEl = null;
            Integer crsAxisOrder = axisNonZeroIndices.get(0);
            String crsUri = crsAxes.get(crsAxisOrder).snd;
            CrsDefinition.Axis crsAxis = crsAxes.get(crsAxisOrder).fst;

            // Get correspondent cellDomain element (grid axes in object gridAxes follow rasdaman storage order)
            CellDomainElement cEl = cDom.next();

            // compute min-max bounds of this axis
            // NOTE1: grid origin is centre of sample space (e.g. pixel)
            // NOTE2: grid-axis and CRS-axis are aligned
            // (!) domain.lo = min(origin, origin+N*offsetVector)  => grid-point is point (not pixel)
            //     domain.hi = max(origin, origin+N*offsetVector)
            BigDecimal resolution     = axis.getKey().get(axisNonZeroIndices.get(0));
            BigDecimal sspaceShift    = WcsUtil.getSampleSpaceShift(resolution, isIrregular, crsAxis.getCrsDefinition().getCode(), coverageType);
            BigDecimal axisLo         = gridOrigin.get(axisNonZeroIndices.get(0)).add(sspaceShift);
            //the grid can also have negative values, case in which axisLo is not in the origin, but it needs to shift to the lowest point on this axis
            if (cEl.getLoInt() < 0 && !coverageType.equals(XMLSymbols.LABEL_GRID_COVERAGE)){
                //jump back resolution * number of negative pixels
                //number of negative pixels is -1 * lower bound of domain
                int negativePixels = -1 * cEl.getLoInt();
                axisLo = axisLo.subtract(BigDecimal.valueOf(negativePixels).multiply(resolution));
            }
            BigInteger gridAxisPoints = BigInteger.valueOf(1).add(BigInteger.valueOf(cEl.getHiInt()-cEl.getLoInt()));
            BigDecimal axisHi;
            if (!isIrregular) {
                // use the resolution: for Indexed CRSs, the formula is different than non-indexed CRSs (+1 term in the denominator)
                // linear CRS: axisHi = (axisLo + #GridPoints)
                // linear CRS: axisHi = (axisLo + #GridPoints - 1)
                if (CrsUtil.isGridCrs(crsAxis.getCrsDefinition().getCode()) || coverageType.equals(XMLSymbols.LABEL_GRID_COVERAGE)) {
                    // CRS:1
                    axisHi = axisLo.add(resolution.multiply(new BigDecimal(gridAxisPoints).add(BigDecimal.valueOf(-1))));
                } else {
                    // linear CRS
                    axisHi = axisLo.add(resolution.multiply(new BigDecimal(gridAxisPoints)));
                }
            } else {
                // get the greatest coefficient
                axisHi = axisLo.add(resolution.multiply(axis.getValue()));
            }

            domEl = new DomainElement(
                    axisLo.compareTo(axisHi) <= 0 ? axisLo : axisHi,    // offset-vector can be negative,
                    axisLo.compareTo(axisHi) <= 0 ? axisHi : axisLo,    // then (axisLo>axisHi)
                    crsAxis.getAbbreviation(),
                    crsAxis.getType(),
                    crsAxis.getUoM(),
                    crsUri,
                    axisOrder,
                    gridAxisPoints,
                    resolution.compareTo(BigDecimal.ZERO) > 0,
                    isIrregular
                    );
            domEl.setAxisDef(crsAxes.get(axisNonZeroIndices.get(0)).fst); // added utilities from domain elements
            if (isIrregular) {
                // Set the offset vector: DomainElement can compute it only if the axis is regular (max-min/cells)
                domEl.setScalarResolution(resolution);
            }
            domainElements.add(domEl);
            log.debug("Added WCPS `domain' element: " + domEl);
            axisOrder += 1;
        }

        // Extract WCPS range elements and quantities
        List<RangeElement> rangeEls = new ArrayList<RangeElement>(rangeElementQuantities.size());
        sweComponents = new ArrayList<AbstractSimpleComponent>(rangeElementQuantities.size());
        for (Pair<RangeElement,Quantity> rangeQuantity : rangeElementQuantities) {
            rangeEls.add(rangeQuantity.fst);
            sweComponents.add(rangeQuantity.snd);
        }

        //construct the list of unique crs uris, in the order of the geo axes
        List<String> uris   = new ArrayList<String>();
        for(Pair<CrsDefinition.Axis, String> crsAxis: crsAxes){
            if(!uris.contains(crsAxis.snd)){
                uris.add(crsAxis.snd);
            }
        }

        // fill up the metadata
        setupMetadata(
                coverageName,
                coverageType,
                nativeFormat,
                extraMeta,
                uris,
                cellDomain,
                domainElements,
                rasdamanCollection,
                rangeEls
            );
    }

    // Constructor : domain is given as WCPS domainElement
    // TODO : domain/cellDomain coupling must be changed for irregular and rotated grids.
    public CoverageMetadata(
            String                   coverageName,
            String                   coverageType,
            String                   nativeFormat,
            Set<Pair<String,String>> extraMeta,
            List<String>             crsUris,
            List<CellDomainElement>  cellDomain,
            List<DomainElement>      domain,
            Pair<BigInteger, String> rasdamanCollection,
            List<RangeElement>       rangeElements
            ) throws PetascopeException, SecoreException {

        // use helper so that constructor overload do not need to call this() as first command
        setupMetadata(
                coverageName,
                coverageType,
                nativeFormat,
                extraMeta,
                crsUris,
                cellDomain,
                domain,
                rasdamanCollection,
                rangeElements
                );
    }

    // Constrctor for Multi* Coverage

    public CoverageMetadata(String coverageName,
            String coverageType,
            String nativeFormat,
            Set<Pair<String,String>> extraMeta,
            List<Pair<CrsDefinition.Axis,String>> crsAxes, // axis -> URI
            List<CellDomainElement> cellDomain,
            List<Pair<RangeElement,Quantity>> rangeElementQuantities,
            ArrayList<BigDecimal> covLowerLeft,
            ArrayList<BigDecimal> covUpperRight
            ) throws WCPSException{

        crsUris = new ArrayList<String>();
        this.coverageName = coverageName;
        this.coverageType = coverageType;
        this.nativeFormat = nativeFormat;
        this.extraMetadata = extraMeta;


        List<CellDomainElement> cellDomainList = new LinkedList<CellDomainElement>();
        List<RangeElement> rangeList = new LinkedList<RangeElement>();
        List<DomainElement> domainList = new LinkedList<DomainElement>();

        for(int i=0; i < crsAxes.size(); i++){
            // Get CRS
            String crsUri = crsAxes.get(i).snd;
            // Build the list of CRS URIs
            if (!crsUris.contains(crsUri)) {
                crsUris.add(crsUri);
            }

            // Build domain metadata
            cellDomainList.add(new CellDomainElement("1", "1", 0));
            domainList.add( new DomainElement(
                covLowerLeft.get(i),
                covUpperRight.get(i),
                crsAxes.get(i).fst.getAbbreviation(),
                crsAxes.get(i).fst.getType(),
                crsAxes.get(i).fst.getUoM(),
                crsUri,
                0,
                BigInteger.ONE,
                true, true)
                );
        }

        // "unsigned int" is default datatype
        rangeList.add(new RangeElement(WcpsConstants.MSG_DYNAMIC_TYPE, WcpsConstants.MSG_UNSIGNED_INT, null));

        this.domain = domainList;
        this.cellDomain = cellDomainList;

        // Extract WCPS range elements and quantities
        range = new ArrayList<RangeElement>(rangeElementQuantities.size());
        sweComponents = new ArrayList<AbstractSimpleComponent>(rangeElementQuantities.size());
        for (Pair<RangeElement,Quantity> rangeQuantity : rangeElementQuantities) {
            range.add(rangeQuantity.fst);
            sweComponents.add(rangeQuantity.snd);
        }
    }

    // constructor's helper
    private void setupMetadata(
            String                   coverageName,
            String                   coverageType,
            String                   nativeFormat,
            Set<Pair<String,String>> extraMeta,
            List<String>             crsUris,
            List<CellDomainElement>  cellDomain,
            List<DomainElement>      domain,
            Pair<BigInteger, String> rasdamanCollection,
            List<RangeElement>       rangeElements
            ) throws PetascopeException, SecoreException {

        this.coverageName = coverageName;
        this.coverageType = coverageType;
        this.crsUris = crsUris;
        this.nativeFormat = nativeFormat;
        this.extraMetadata = extraMeta;
        this.rasdamanCollection = rasdamanCollection;

        // Check if some value is missing
        if (
                coverageName  == null ||
                coverageType  == null ||
                nativeFormat  == null ||
                cellDomain    == null ||
                domain        == null ||
                rangeElements == null ||
                crsUris       == null) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata,
                    "Either coverage name, type, format, (Cell-)domain, range type/set, or CRS be null for coverage " + coverageName);
        }

        // CellDomain
        if (cellDomain.isEmpty()) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid cell domain: At least "
                    + "one element is required for coverage " + coverageName);
        }
        this.cellDomain = cellDomain;

        // Domain axes: consitency checks
        if (domain.isEmpty()) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Invalid domain: At least "
                    + "one element is required for coverage " + coverageName);
        } else {

            if (domain.size() != cellDomain.size()) {
                throw new PetascopeException(ExceptionCode.InvalidMetadata, "Domain and cell domain "
                        + "must have equal number of elements for coverage " + coverageName);
            }

            Iterator<DomainElement>     i  = domain.iterator();
            Iterator<CellDomainElement> ci = cellDomain.iterator();
            while (i.hasNext() && ci.hasNext()) {
                DomainElement           next = i.next();
                Iterator<DomainElement> j    = domain.iterator();
                while (j.hasNext()) {
                    DomainElement previous = j.next();
                    // don't compare same objects
                    if (next == previous) {
                        continue;
                    }
                    // TODO: use .contains() in place of .equals to let aliases on axis types.
                    if (previous.getLabel().equals(next.getLabel())) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Duplicate domain "
                                + "element name encountered for coverage " + coverageName);
                    }
                    if (previous.getType().equals(AxisTypes.ELEV_AXIS) && next.getType().equals(AxisTypes.ELEV_AXIS)) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Domain can contain"
                                + " at most one elevation axis for coverage " + coverageName);
                    }
                    if (previous.getType().equals(AxisTypes.X_AXIS) && next.getType().equals(AxisTypes.X_AXIS)) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Domain can contain"
                                + " at most one x axis for coverage " + coverageName);
                    }
                    if (previous.getType().equals(AxisTypes.Y_AXIS) && next.getType().equals(AxisTypes.Y_AXIS)) {
                        throw new PetascopeException(ExceptionCode.InvalidMetadata, "Domain can contain"
                                + " at most one y axis for coverage " + coverageName);
                    }
                }
            }
        }
        this.domain = domain;

        // Range
        if (rangeElements.isEmpty()) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "At least one range element is "
                    + "required for coverage " + coverageName);
        }
        this.range = new ArrayList<RangeElement>(rangeElements.size());
        Iterator<RangeElement> ir = rangeElements.iterator();
        while (ir.hasNext()) {
            RangeElement next = ir.next();
            Iterator<RangeElement> j = this.range.iterator();

            while (j.hasNext()) {
                if (j.next().getName().equals(next.getName())) {
                    throw new PetascopeException(ExceptionCode.InvalidMetadata, "Duplicate range element"
                            + " name encountered for coverage " + coverageName);
                }
            }

            this.range.add(next);
        }

        // Define the bounding-box
        // Store the native (C)CRS from the list of single CRSs associated to the coverage
        String crsName = CrsUtil.CrsUri.createCompound(crsUris);
        bbox = new Bbox(crsName, domain, coverageName);
    }

    @Override
    public CoverageMetadata clone() {
        try {
            // cell domain
            List<CellDomainElement> cloneCellDomain = new ArrayList<CellDomainElement>(cellDomain.size());
            Iterator<CellDomainElement> i = cellDomain.iterator();
            while (i.hasNext()) {
                cloneCellDomain.add(i.next().clone());
            }

            // domain
            List<DomainElement> cloneDom = new ArrayList<DomainElement>(domain.size());
            Iterator<DomainElement> k = domain.iterator();
            while (k.hasNext()) {
                cloneDom.add(k.next().clone());
            }

            // CRS URIs
            List<String> cloneUris = new ArrayList<String>(crsUris.size());
            for (String uri : crsUris) {
                cloneUris.add(uri.toString());
            }

            // Range
            List<RangeElement> cloneRange = new ArrayList<RangeElement>(range.size());
            Iterator<RangeElement> j = range.iterator();

            // Extra metadata
            Iterator<Pair<String,String>> extraMetaIt = extraMetadata.iterator();
            Set<Pair<String,String>> cloneMetadata = new HashSet<Pair<String,String>>(extraMetadata.size());
            while (extraMetaIt.hasNext()) {
                Pair<String,String> metadataPair = extraMetaIt.next();
                cloneMetadata.add(Pair.of(metadataPair.fst, metadataPair.snd));
            }


            while (j.hasNext()) {
                cloneRange.add(j.next().clone());
            }

            return new CoverageMetadata(
                    coverageName.toString(),
                    coverageType.toString(),
                    nativeFormat.toString(),
                    cloneMetadata,
                    cloneUris,
                    cloneCellDomain,
                    cloneDom,
                    Pair.of(rasdamanCollection.fst, rasdamanCollection.snd),
                    cloneRange
                    );
        } catch (PetascopeException ime) {
            throw new RuntimeException("Invalid metadata while cloning "
                    + "Metadata. This is a software bug in WCPS.", ime);
        } catch (SecoreException sEx) {
            log.error("SECORE error while cloning: ", sEx.getMessage());
            return new CoverageMetadata();
        }
    }

    public LinkedHashMap<List<BigDecimal>, BigDecimal> getGridAxes(){
        return gridAxes;
    }

    public List<BigDecimal> getGridOrigin(){
        return gridOrigin;
    }

    public int getCoverageId() {
        return coverageId;
    }

    public String getCoverageName() {
        return coverageName;
    }

    public String getCoverageType() {
        return coverageType;
    }

    public String getNativeFormat() {
        return nativeFormat;
    }

    public String getInterpolationDefault() {
        return interpolationDefault.getInterpolationType();
    }

    public Iterator<InterpolationMethod> getInterpolationMethodIterator() {
        return interpolationSet.iterator();
    }

    public void setInterpolationSet(Set<InterpolationMethod> interpolationSet) {
        this.interpolationSet = interpolationSet;
    }

    public void setDefaultInterpolation(InterpolationMethod interpolationDefault) {
        this.interpolationDefault = interpolationDefault;
    }

    public String getNullResistanceDefault() {
        return interpolationDefault.getNullResistance();
    }

    public Iterator<String> getNullSetIterator() {
        return nullSet.iterator();
    }

    public Description getDescription() {
        return null == owsDescription ? new Description() : owsDescription;
    }

    public String getAbstract() {
        return ListUtil.printList(getDescription().getAbstracts(), ". ");
    }

    public String getTitle() {
        return ListUtil.printList(owsDescription.getTitles(), ". ");
    }

    public String getKeywords() {
        return ListUtil.printList(owsDescription.getKeywordGroups(), ". ");
    }

    public Iterator<CellDomainElement> getCellDomainIterator() {
        return cellDomain.iterator();
    }

    public List<CellDomainElement> getCellDomainList() {
        return cellDomain;
    }

    public Iterator<DomainElement> getDomainIterator() {
        return domain.iterator();
    }

    /**
     * Gets an array list containing all null values, on all bands and removes the duplicates.
     * This is used for passing the values further to rasdaman (rasdaman doesn't have nil values per band, but per mdd set).
     * @return
     */
    public ArrayList<String> getAllUniqueNullValues(){
        ArrayList<String> result = new ArrayList<String>();
        //iterate over the swe quantities
        Iterator<AbstractSimpleComponent> sweIterator = this.getSweComponentsIterator();
        while (sweIterator.hasNext()){
            Iterator<NilValue> nilIterator = sweIterator.next().getNilValuesIterator();
            while (nilIterator.hasNext()){
                String currentNull = nilIterator.next().getValue();
                //add it if it is not in already
                if(!result.contains(currentNull) && !currentNull.isEmpty()){
                    result.add(currentNull);
                }
            }
        }
        return result;
    }

    /**
     * Returns the scalar (signed) resolution of the grid axis of specified order.
     * @param order  The order of the grid axis (rasdaman order).
     * @return The non-zero component of the offset vector of the axis.
     */
    public BigDecimal getDomainDirectionalResolution(int order) {
        // guards
        if (null != getDomainList() && null != getDomainList().get(order)) {
            return getDomainList().get(order).getDirectionalResolution();
        }
        return BigDecimal.valueOf(-1);
    }

    /**
     * Returns the scalar (signed) resolution of the grid axis of specified name.
     * @param name  The name of the grid axis (rasdaman order).
     * @return The non-zero component of the offset vector of the axis.
     */
    public BigDecimal getDomainDirectionalResolution(String name) {
        return getDomainDirectionalResolution(getDomainIndexByName(name));
    }

    public Iterator<RangeElement> getRangeIterator() {
        return range.iterator();
    }

    /**
     * Get the rasdaman index of a given range field.
     * @param inputName
     * @return The index (0,1,2,__) of the corresponding range/struct field in rasdaman.
     * @throws PetascopeException
     */
    public Integer getRangeIndexByName(String inputName) throws PetascopeException {
        Iterator<RangeElement> rIt = getRangeIterator();
        String rangeElementName = "";
        int index = 0;
        while (rIt.hasNext() && !inputName.equals(rangeElementName)) {
            rangeElementName = rIt.next().getName();
            index++;
        }
        if (!inputName.equals(rangeElementName)) {
            throw new PetascopeException(ExceptionCode.NoSuchField, "No range field \"" + inputName + "\" for coverage " + getCoverageName());
        }
        return --index;
    }

    /**
     * Get the coverage range field name of the given order.
     * @param index
     * @return The label (name) of the corresponding range field.
     * @throws PetascopeException
     */
    public String getRangeNameByIndex(int index) throws PetascopeException {
        String fieldName;
        RangeElement rangeEl;
        try {
            rangeEl = range.get(index);
            fieldName = rangeEl.getName();
        } catch (IndexOutOfBoundsException ex) {
            throw new PetascopeException(ExceptionCode.NoSuchField, "No range field with index " + index + " for coverage " + getCoverageName());
        }
        return fieldName;
    }

    public Iterator<AbstractSimpleComponent> getSweComponentsIterator() {
        return sweComponents.iterator();
    }

    public int getDimension() {
        return cellDomain.size();
    }

    /**
     * Returns the cell domain with the given order. In case no domain with the give order is found, it throws an exception.
     * @param order the iOrder of the searched cellDomain.
     * @return the cellDomain with the given order.
     */
    public CellDomainElement getCellDomainByOrder(int order) throws PetascopeException {
        CellDomainElement ret = null;
        for(CellDomainElement i : cellDomain){
            if(i.getOrder() == order){
                ret = i;
                break;
            }
        }
        if(ret == null){
            throw new PetascopeException(ExceptionCode.NoSuchField, "No cell domain with order " + order + " for coverage " + getCoverageName());
        }
        return ret;
    }

    public int getDomainIndexByType(String type) {
        Iterator<DomainElement> i = domain.iterator();
        for (int index = 0; i.hasNext(); index++) {
            if (i.next().getType().equals(type)) {
                return index;
            }
        }
        return -1;
    }

    public DomainElement getDomainByType(String type) {
        Iterator<DomainElement> i = domain.iterator();
        for (int index = 0; i.hasNext(); index++) {
            DomainElement dom = i.next();
            if (dom.getType().equals(type)) {
                return dom;
            }
        }
        return null;
    }

        public int getDomainIndexByName(String name) {
        Iterator<DomainElement> i = domain.iterator();
        for (int index = 0; i.hasNext(); index++) {
            if (i.next().getLabel().equals(name)) {
                return index;
            }
        }
        return -1;
    }

    public DomainElement getDomainByName(String name) {
        Iterator<DomainElement> i = domain.iterator();
        for (int index = 0; i.hasNext(); index++) {
            DomainElement dom = i.next();
            if (dom.getLabel().equals(name)) {
                return dom;
            }
        }
        return null;
    }

    /**
     * Returns the list of domain elements whose names are specified as an ordered input list.
     * @param names
     * @return DomainElement objects with the specified labels.
     */
    public List<DomainElement> getDomainsByNames(List<String> names) {
        List<DomainElement> domElements = new ArrayList<DomainElement>(names.size());
        for (String domainName : names) {
                domElements.add(getDomainByName(domainName));
        }
        return domElements;
    }

    public CellDomainElement getCellDomainByName(String name) {

        for (DomainElement axis : domain) {
            if (axis.getLabel().equalsIgnoreCase(name)) {
                return cellDomain.get(axis.getOrder());
            }
        }
        return null;
    }

    public List<DomainElement> getDomainList() {
        return domain;
    }

    public Bbox getBbox() {
        return bbox;
    }

    /**
     * @return the specified cellDomain
     */
    public CellDomainElement getCellDomain(int i) {
        return cellDomain.get(i);
    }

    /**
     * @return the cellDomain associated to the specified domain
     */
    public CellDomainElement getCellDomain(String domainLabel) {
        for (DomainElement dEl : domain) {
            if (dEl.getLabel().equals(domainLabel)) {
                return cellDomain.get(domain.indexOf(dEl));
            }
        }
        return null;
    }


    /**
     * @return the CRS URI of the whole coverage.
     */
    public List<String> getCrsUris() {
        return crsUris;
    }

    /**
     * @return The set of (optional) extra metadata for this coverage (eg GMLCOV, OWS, etc.)
     * Pair of metadata type, metadata value
     */
    public Set<Pair<String,String>> getExtraMetadata() {
        return (null==extraMetadata ? new HashSet<Pair<String,String>>() : extraMetadata);
    }

    /**
     * @return The set of (optional) extra metadata for this coverage of the specified type
     * See TABLE_EXTRAMETADATA_TYPE for the dictionary of metadata types.
     */
    /**
     * Get the set of (optional) extra metadata for this coverage of the specified type.
     * See TABLE_EXTRAMETADATA_TYPE for the dictionary of metadata types.
     * @param metadataType
     * @return The set of extra metadata of the specified type.
     */
    public Set<String> getExtraMetadata(String metadataType) {
        Set<String> selectedExtraMetadata = null;
        if(extraMetadata != null) {
            selectedExtraMetadata = new HashSet<String>();
            for (Pair<String, String> metadataPair : extraMetadata) {
                if (metadataPair.fst.equals(metadataType)) {
                    selectedExtraMetadata.add(metadataPair.snd);
                }
            }
        }
        return selectedExtraMetadata;
    }


    /**
     * Gets the oid and collection name of the rasdaman collection
     * corresponding to this coverage.
     * @return pair containing the oid and the collection name.
     */
    public Pair<BigInteger, String> getRasdamanCollection() {
        return this.rasdamanCollection;
    }

    /**
     * The rasdaman collection type information in the form mddType:collectionType.
     * @return rasdaman type information.
     */
    public String getRasdamanCollectionType() { return this.rasdamanCollectionType; }

    /**
     * Setter for the collectionType attribute.
     * @param collectiontype the rasdaman collection type in the form mddType:collectiontype.
     */
    public void setRasdamanCollectionType(String collectiontype) { this.rasdamanCollectionType = collectiontype; }

    /**
     * Gets the number of bands of this coverage.
     * @return number of bands for this coverage.
     */
    public int getNumberOfBands(){
        return this.sweComponents.size();
    }
    /*
     * setters
     */
    public void setRasdamanCollection(Pair<BigInteger, String> rasdamanCollection){
        this.rasdamanCollection = rasdamanCollection;
    }

    protected void setCoverageId(int id) {
        this.coverageId = id;
    }

    public void setMetadata(Set<Pair<String,String>> metadata) {
        this.extraMetadata = metadata;
    }

    public void setCoverageName(String coverageName) throws PetascopeException {
        if (coverageName == null) {
            throw new PetascopeException(ExceptionCode.InvalidMetadata, "Metadata transformation: Coverage name cannot be null");
        }

        this.coverageName = coverageName;
    }

    public void setDescription(Description descr) {
        owsDescription = descr;
    }

    public void setRangeType(String type) throws PetascopeException {
        Iterator<RangeElement> i = range.iterator();

        while (i.hasNext()) {
            i.next().setType(type);
        }
    }

    /**
     * @param titleStr the titleStr to set
     */
    public void setTitle(String titleStr) {
        owsDescription.addTitle(titleStr);
    }

    /**
     * @param abstractStr the abstractStr to set
     */
    public void setAbstract(String abstractStr) {
        owsDescription.addAbstract(abstractStr);
    }

    /**
     * @param keywordsStr the keywordsStr to set
     */
    public void setKeywords(String keywordsStr) {
        owsDescription.addKeywordGroup(Arrays.asList(Pair.of(keywordsStr, "")));
    }

    /**
     * @param cellDomain the cellDomain to set
     */
    public void setCellDomain(List<CellDomainElement> cellDomain) {
        this.cellDomain = cellDomain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(List<DomainElement> domain) {
        this.domain = domain;
    }

    /**
     * @param range the range to set
     */
    public void setRange(List<RangeElement> range) {
        this.range = range;
    }


    /*
     * utilities
     */

    /**
     *  Returns the maximal time position of the current coverage in ISO 8601 format, as string.
     * If there is no time-axis, returns null
     */
    public String getTimePeriodBeginning() {
        // Extract the specified domain from cellDomain object, if it exists
        for (DomainElement axis : domain) {
            if (AxisTypes.T_AXIS.equals(axis.getType()))
                return axis.getMinValue().toString();
        }
        log.warn("Requesting time info on " + coverageName + ", which is missing.");
        return null;
    }

    /**
     *  Returns the minimal time position of the current coverage in ISO 8601 format, as string.
     * If there is no time-axis, returns null
     */
    public String getTimePeriodEnd() {
        // Extract the specified domain from cellDomain object, if it exists
        for (DomainElement axis : domain) {
            if (AxisTypes.T_AXIS.equals(axis.getType()))
                return axis.getMaxValue().toString();
        }
        log.warn("Requesting time info on " + coverageName + ", which is missing.");
        return null;
    }

    /**
     * Returns the time span of the current coverage, as described in the metadata (in miliseconds).
     * If there is no metadata, returns -1.
     * Note that this function returns the absolute difference. It is the administrator's
     * responsibility to make sure that the metadata values are correct.
     */
    public long getTimeSpan() {
        // Extract the specified domain from cellDomain object, if it exists
        for (DomainElement axis : domain) {
            if (AxisTypes.T_AXIS.equals(axis.getType())) {
                long result = TimeString.difference(getTimePeriodEnd(), getTimePeriodBeginning());
                return Math.abs(result);
            }
        }
        log.warn("Requesting time info on " + coverageName + ", which is missing.");
        return -1;
    }

    /* Returns the difference between the maximum and the minimum time axis index.
    Returns -1 if there is no metadata. */
    public long getTimeIndexesSpan() {
        // Extract the specified domain from cellDomain object, if it exists
        for (DomainElement axis : domain) {
            if (AxisTypes.T_AXIS.equals(axis.getType())) {
                BigInteger big = BigInteger.valueOf(getCellDomain(axis.getOrder()).getHiInt()-getCellDomain(axis.getOrder()).getLoInt());
                return big.longValue();
            }
        }
        log.warn("Requesting time info on " + coverageName + ", which is missing.");
        return -1;
    }

    public boolean isRangeBoolean() {
        Iterator<RangeElement> i = range.iterator();
        while (i.hasNext()) {
            if (!i.next().isBoolean()) {
                return false;
            }
        }
        return true;
    }

    public boolean isRangeComplex() {
        Iterator<RangeElement> i = range.iterator();
        while (i.hasNext()) {
            if (!i.next().isComplex()) {
                return false;
            }
        }
        return true;
    }

    public boolean isRangeIntegral() {
        Iterator<RangeElement> i = range.iterator();
        while (i.hasNext()) {
            if (!i.next().isIntegral()) {
                return false;
            }
        }
        return true;
    }

    public boolean isRangeFloating() {
        Iterator<RangeElement> i = range.iterator();
        while (i.hasNext()) {
            if (!i.next().isFloating()) {
                return false;
            }
        }
        return true;
    }

    public boolean isRangeNumeric() {
        Iterator<RangeElement> i = range.iterator();

        while (i.hasNext()) {
            if (!i.next().isNumeric()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Takes a GML coverage and translates it into a CoverageMetadata object.
     * The created coverage doesn't have a coverageId or a rasdamanCollection yet.
     *
     * @param gmlCoverage
     * @return
     * @throws WCSException
     * @throws PetascopeException
     * @throws SecoreException
     */
    public static CoverageMetadata fromGML(Document gmlCoverage) throws WCSException, PetascopeException, SecoreException {
        Element root = gmlCoverage.getRootElement();
        //cov type
        String coverageType = GMLParserUtil.parseCoverageType(root);

        //cov id
        String id = root.getAttributeValue(XMLSymbols.ATT_ID, XMLSymbols.NAMESPACE_GML);
        //native format
        String nativeFormat = NATIVE_FORMAT;
        //crs list
        List<Pair<CrsDefinition.Axis, String>> crsAxes = GMLParserUtil.parseCrsList(root);
        //domain set
        Element domainSet = GMLParserUtil.parseDomainSet(root);
        //from the domain set extract the grid type
        Element gridType = GMLParserUtil.parseGridType(domainSet);
        List<CellDomainElement> cellDomainElements = GMLParserUtil.parseRectifiedGridCellDomain(gridType);
        List<BigDecimal> originPoints = new ArrayList<BigDecimal>();
        LinkedHashMap<List<BigDecimal>, BigDecimal> gridAxes = null;
        //rectified grids
        if (!gridType.getLocalName().equals(XMLSymbols.LABEL_GRID)) {
            String[] stringOriginPoints = GMLParserUtil.parseGridOrigin(gridType);
            gridAxes = GMLParserUtil.parseGridAxes(gridType, stringOriginPoints.length);
            //transform origin points into actual coordinates
            for(int i = 0; i < stringOriginPoints.length; i++){
                //check if time
                if(stringOriginPoints[i].contains("\"")){
                    if(crsAxes.size() > i) {
                        String datumOrigin = crsAxes.get(i).fst.getCrsDefinition().getDatumOrigin();
                        String uom = crsAxes.get(i).fst.getUoM();
                        Double numericCoordinate = TimeUtil.countOffsets(datumOrigin, stringOriginPoints[i], uom, 1D); //don't normalize here, absolute time value needed
                        originPoints.add(new BigDecimal(numericCoordinate));
                    }
                }
                else{
                    //numeric
                    originPoints.add(new BigDecimal(stringOriginPoints[i]));
                }
            }
        }
        else{
            // Simple GridCoverage type: geometry is not defined:
            // [#760] Assign IndexCrs and versor-vectors to a GridCoverage by default (needed for BBOX, which is WCS Req.1)
            int dimensionNo = cellDomainElements.size();
            String uri = ConfigManager.SECORE_URLS.get(0) + '/' +
                    CrsUtil.KEY_RESOLVER_CRS + '/' +
                    CrsUtil.OGC_AUTH + '/' +
                    CrsUtil.CRS_DEFAULT_VERSION + '/' +
                    CrsUtil.INDEX_CRS_PATTERN.replace("%d", "" + dimensionNo);
            log.debug("Assigning " + uri + " CRS to " + id + "by default.");
            CrsDefinition crsDef = CrsUtil.getGmlDefinition(uri);
            for (CrsDefinition.Axis axis : crsDef.getAxes()) {
                crsAxes.add(Pair.of(axis, uri));
            }
            // Grid geometry
            gridAxes = new LinkedHashMap<List<BigDecimal>, BigDecimal>();
            for (int i = 0; i < dimensionNo; i++) {
                originPoints.add(new BigDecimal(cellDomainElements.get(i).getLo()));
                BigDecimal[] uv = Vectors.unitVector(dimensionNo, i);
                gridAxes.put(Arrays.asList(uv), null);
            }
        }
        //parse the range type information
        List<Pair<RangeElement, Quantity>> rangeQuantities = GMLParserUtil.parseRangeElementQuantities(root);

        //parse extra metadata
        String extraMetadata = GMLParserUtil.parseExtraMetadata(root);
        //wrap it in the required data structure
        Set<Pair<String, String>> extraMetadataStructure = null;
        if(extraMetadata != null) {
            extraMetadataStructure = new HashSet<Pair<String, String>>();
            extraMetadataStructure.add(Pair.of(DbMetadataSource.EXTRAMETADATA_TYPE_GMLCOV, extraMetadata));
        }

        CoverageMetadata result = new CoverageMetadata(
            id,
            coverageType,
            nativeFormat,
            extraMetadataStructure,
            crsAxes,
            cellDomainElements,
            originPoints,
            gridAxes,
            null, //no rasdaman collection for now
            rangeQuantities);

        return result;
    }

    private final static String NATIVE_FORMAT = "application/x-ogc-rasdaman";
}
