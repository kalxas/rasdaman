package org.rasdaman.cis;

import java.math.BigDecimal;
import org.rasdaman.domain.cis.RegularAxis;
import org.rasdaman.domain.cis.DomainSet;
import org.rasdaman.domain.cis.Uom;
import org.rasdaman.domain.cis.RasdamanRangeSet;
import org.rasdaman.domain.cis.RangeType;
import org.rasdaman.domain.cis.GridLimits;
import org.rasdaman.domain.cis.GeneralGrid;
import org.rasdaman.domain.cis.IndexAxis;
import org.rasdaman.domain.cis.AxisExtent;
import org.rasdaman.domain.cis.Field;
import org.rasdaman.domain.cis.Quantity;
import org.rasdaman.domain.cis.EnvelopeByAxis;
import org.rasdaman.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.DataRecord;
import org.rasdaman.domain.cis.Envelope;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeneralGridDomainSet;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.repository.interfaces.AbstractCoverageRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test persist a 2D coverage with regular axes with Hibernate
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {Application.class})
public class TestCoverage2DRegular {

    private String ENVELOPE_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4326";
    private String GRID_LIMITS_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/Index2D";
    private String GENERAL_GRID_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4326";

    @Autowired private
    AbstractCoverageRepository abstractCoverageRepository;

    @Test
    public void checkCoverage() {
        EnvelopeByAxis envelopeByAxis = createEnvelopeByaxis();
        Envelope envelope = new Envelope(envelopeByAxis);
        DomainSet domainSet = createDomainSet();
        RasdamanRangeSet rasdamanRangeSet = createRangeSet();
        RangeType rangeType = createRangeType();

        Coverage coverage = new GeneralGridCoverage();
        coverage.setEnvelope(envelope);
        coverage.setDomainSet(domainSet);
        coverage.setRangeType(rangeType);
        coverage.setRasdamanRangeSet(rasdamanRangeSet);

        abstractCoverageRepository.save(coverage);

        Coverage coverageFetched = abstractCoverageRepository.findOneByCoverageId(coverage.getCoverageId());
        GeneralGrid generalGrid = ((GeneralGridDomainSet) coverageFetched.getDomainSet()).getGeneralGrid();
        //Test for axisExtent 1
        assertEquals("Lat", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getAxisLabel());
        assertEquals("deg", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getUomLabel());
        assertEquals("-90", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getLowerBound());
        assertEquals("-80", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getUpperBound());

        //Test for axisExtent 2
        assertEquals("Long", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getAxisLabel());
        assertEquals("deg", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getUomLabel());
        assertEquals("0", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getLowerBound());
        assertEquals("10", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getUpperBound());

        //Test for envelope
        assertEquals(ENVELOPE_NAME, coverageFetched.getEnvelope().getEnvelopeByAxis().getSrsName());
        assertEquals(2, coverageFetched.getEnvelope().getEnvelopeByAxis().getSrsDimension());

        //Test for RangeSet
        assertEquals("MddTypeCoverage2DRegular", coverageFetched.getRasdamanRangeSet().getMddType());
        assertEquals(BigInteger.ONE, coverageFetched.getRasdamanRangeSet().getOid());
        assertEquals("CollectionNameCoverage2DRegular", coverageFetched.getRasdamanRangeSet().getCollectionName());
        assertEquals("CollectionTypeCoverage2DRegular", coverageFetched.getRasdamanRangeSet().getCollectionType());

        //Test for RangeType
        assertEquals("http://www.opengis.net/def/dataType/OGC/0/unsignedInt", coverageFetched.getRangeType().getDataRecord().getFields().get(0).getQuantity().getDefinition());
        assertEquals("singleBand", coverageFetched.getRangeType().getDataRecord().getFields().get(0).getName());

        //Test for DomainSet
        RegularAxis regularAxisFetched1 = (RegularAxis) generalGrid.getGeoAxes().get(0);
        RegularAxis regularAxisFetched2 = (RegularAxis) generalGrid.getGeoAxes().get(1);
        //Test for regular axis 1

        assertEquals(0, new BigDecimal("5").compareTo(regularAxisFetched1.getResolution()));
        assertEquals("Lat", regularAxisFetched1.getAxisLabel());
        assertEquals(new BigDecimal("-90"), regularAxisFetched1.getLowerBound());
        assertEquals(new BigDecimal("-80"), regularAxisFetched1.getUpperBound());

        //Test for regular axis 2
        assertEquals(0, new BigDecimal("5").compareTo(regularAxisFetched2.getResolution()));
        assertEquals("Long", regularAxisFetched2.getAxisLabel());
        assertEquals(new BigDecimal("0"), regularAxisFetched2.getLowerBound());
        assertEquals(new BigDecimal("10"), regularAxisFetched2.getUpperBound());

        //Test for GridLimits
        GridLimits gridLimitsTest = generalGrid.getGridLimits();
        //Test for indexAxis in GridLimits
        assertEquals("i", gridLimitsTest.getIndexAxes().get(0).getAxisLabel());
        assertEquals(new BigDecimal("0"), gridLimitsTest.getIndexAxes().get(0).getLowerBound());
        assertEquals(new BigDecimal("2"), gridLimitsTest.getIndexAxes().get(0).getUpperBound());

        assertEquals("j", gridLimitsTest.getIndexAxes().get(1).getAxisLabel());
        assertEquals(new BigDecimal("0"), gridLimitsTest.getIndexAxes().get(1).getLowerBound());
        assertEquals(new BigDecimal("2"), gridLimitsTest.getIndexAxes().get(1).getUpperBound());

        assertEquals(GRID_LIMITS_NAME, gridLimitsTest.getSrsName());

        //Test for GeneralGrid
        GeneralGrid GeneralGridTest = generalGrid;
        assertEquals(GENERAL_GRID_NAME, GeneralGridTest.getSrsName());
    }

    private EnvelopeByAxis createEnvelopeByaxis() {
        List<AxisExtent> axisExtentList = new LinkedList<>();

        AxisExtent axisExtent1 = new AxisExtent();
        axisExtent1.setAxisLabel("Lat");
        axisExtent1.setUomLabel("deg");
        axisExtent1.setLowerBound("-90");
        axisExtent1.setUpperBound("-80");

        AxisExtent axisExtent2 = new AxisExtent();
        axisExtent2.setAxisLabel("Long");
        axisExtent2.setUomLabel("deg");
        axisExtent2.setLowerBound("0");
        axisExtent2.setUpperBound("10");

        axisExtentList.add(axisExtent1);
        axisExtentList.add(axisExtent2);

        EnvelopeByAxis envelope = new EnvelopeByAxis();
        envelope.setSrsName("http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4326");
        envelope.setSrsDimension(2);
        envelope.setAxisExtents(axisExtentList);

        return envelope;

    }

    private DomainSet createDomainSet() {
        List<GeoAxis> axisListRegular = new LinkedList<>();

        RegularAxis regularAxis1 = new RegularAxis();
        regularAxis1.setResolution(new BigDecimal("5"));
        regularAxis1.setAxisLabel("Lat");
        regularAxis1.setLowerBound(new String("-90"));
        regularAxis1.setUpperBound(new String("-80"));

        RegularAxis regularAxis2 = new RegularAxis();
        regularAxis2.setResolution(new BigDecimal("5"));
        regularAxis2.setAxisLabel("Long");
        regularAxis2.setLowerBound(new String("0"));
        regularAxis2.setUpperBound(new String("10"));

        axisListRegular.add(regularAxis1);
        axisListRegular.add(regularAxis2);

        List<IndexAxis> axisListIndex = new LinkedList<>();

        IndexAxis indexAxis1 = new IndexAxis();
        indexAxis1.setAxisLabel("i");
        indexAxis1.setLowerBound(new Long("0"));
        indexAxis1.setUpperBound(new Long("2"));

        IndexAxis indexAxis2 = new IndexAxis();
        indexAxis2.setAxisLabel("j");
        indexAxis2.setLowerBound(new Long("0"));
        indexAxis2.setUpperBound(new Long("2"));

        axisListIndex.add(indexAxis1);
        axisListIndex.add(indexAxis2);

        GridLimits gridLimits = new GridLimits();
        gridLimits.setIndexAxes(axisListIndex);
        gridLimits.setSrsName("http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/Index2D");

        GeneralGrid generalGrid = new GeneralGrid();
        generalGrid.setSrsName("http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4326");
        generalGrid.setGeoAxes(axisListRegular);
        generalGrid.setGridLimits(gridLimits);

        DomainSet domainSet = new GeneralGridDomainSet(generalGrid);

        return domainSet;
    }

    private RangeType createRangeType() {
        Uom uom = new Uom();

        Quantity quantity = new Quantity();
        quantity.setDefinition("http://www.opengis.net/def/dataType/OGC/0/unsignedInt");
        quantity.setUom(uom);

        Field field = new Field();
        field.setName("singleBand");
        field.setQuantity(quantity);

        List<Field> fieldList = new LinkedList<>();
        fieldList.add(field);
        DataRecord dataRecord = new DataRecord(fieldList);

        RangeType rangeType = new RangeType();
        rangeType.setDataRecord(dataRecord);

        return rangeType;
    }

    private RasdamanRangeSet createRangeSet() {
        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
        rasdamanRangeSet.setMddType("MddTypeCoverage2DRegular");
        rasdamanRangeSet.setOid(new Long("1"));
        rasdamanRangeSet.setCollectionName("CollectionNameCoverage2DRegular");
        rasdamanRangeSet.setCollectionType("CollectionTypeCoverage2DRegular");

        return rasdamanRangeSet;
    }

}
