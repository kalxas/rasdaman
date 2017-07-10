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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import org.rasdaman.ApplicationMain;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.DataRecord;
import org.rasdaman.domain.cis.Envelope;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.rasdaman.domain.cis.GeneralGridDomainSet;
import org.rasdaman.domain.cis.GeoAxis;
import org.rasdaman.repository.interfaces.CoverageRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test persist a 1D coverage with regular axis with Hibernate
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {ApplicationMain.class})
public class TestCoverage1DRegular {


    private String ENVELOPE_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/AnsiDate";
    private String GENERAL_GRID_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/AnsiDate";
    private String GRID_LIMITS_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/Index1D";


    @Autowired private
    CoverageRepository abstractCoverageRepository;

    @Test
    public void checkCoverage() {
        EnvelopeByAxis envelopeByAxis = createEnvelopeByAxis();
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
        domainSet = coverageFetched.getDomainSet();

        GeneralGrid generalGrid = ((GeneralGridDomainSet)(domainSet)).getGeneralGrid();

        //Test for RangeSet
        assertEquals("MddTypeCoverage1DRegular",coverageFetched.getRasdamanRangeSet().getMddType());
        assertEquals(BigInteger.ONE,coverageFetched.getRasdamanRangeSet().getOid());
        assertEquals("CollectionNameCoverage1DRegular",coverageFetched.getRasdamanRangeSet().getCollectionName());
        assertEquals("CollectionTypeCoverage1DRegular",coverageFetched.getRasdamanRangeSet().getCollectionType());

        //Test for RangeType
        Field field = coverageFetched.getRangeType().getDataRecord().getFields().get(0);
        assertEquals("http://www.opengis.net/def/dataType/OGC/0/unsignedInt", field.getQuantity().getDefinition());
        assertEquals("adventskalendertuerchen", field.getName());


        //Test for envelope
        assertEquals(ENVELOPE_NAME,coverageFetched.getEnvelope().getEnvelopeByAxis().getSrsName());
        assertEquals(1,coverageFetched.getEnvelope().getEnvelopeByAxis().getSrsDimension());
        assertEquals("ansi",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisLabels());
        AxisExtent axisExtent = coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0);
        assertEquals("ansi", axisExtent.getAxisLabel());
        assertEquals("2015-12-01", axisExtent.getLowerBound());
        assertEquals("2015-12-31", axisExtent.getUpperBound());
        assertEquals("d", axisExtent.getUomLabel());

        //Test for DomainSet
        
        RegularAxis regularAxisFetched = (RegularAxis)generalGrid.getGeoAxes().get(0);
        //Test for regular axis

        assertEquals(0, new BigDecimal("1").compareTo(regularAxisFetched.getResolution()));
        assertEquals("ansi",regularAxisFetched.getAxisLabel());
        assertEquals(new String("1"),regularAxisFetched.getLowerBound());
        assertEquals(new String("31"),regularAxisFetched.getUpperBound());

        GridLimits gridLimitsTest = generalGrid.getGridLimits();
        //Test for indexAxis in GridLimits
        IndexAxis indexAxis = gridLimitsTest.getIndexAxes().iterator().next();
        assertEquals("i", indexAxis.getAxisLabel());
        assertEquals(new String("1"), indexAxis.getLowerBound());
        assertEquals(new String("31"), indexAxis.getUpperBound());

        assertEquals(GRID_LIMITS_NAME,gridLimitsTest.getSrsName());

        //Test for GeneralGrid
        GeneralGrid GeneralGridTest = generalGrid;
        assertEquals(GENERAL_GRID_NAME,GeneralGridTest.getSrsName());        
    }

    private EnvelopeByAxis createEnvelopeByAxis()
    {
        List<AxisExtent> axisExtentList = new ArrayList<>();

        AxisExtent axisExtent = new AxisExtent();
        axisExtent.setAxisLabel("ansi");
        axisExtent.setUomLabel("d");
        axisExtent.setLowerBound("2015-12-01");
        axisExtent.setUpperBound("2015-12-31");

        axisExtentList.add(axisExtent);

        EnvelopeByAxis envelope = new EnvelopeByAxis();
        envelope.setSrsName(ENVELOPE_NAME);
        envelope.setSrsDimension(1);
        envelope.setAxisLabels("ansi");
        envelope.setAxisExtents(axisExtentList);

        return envelope;
    }

    private DomainSet createDomainSet()
    {
        RegularAxis regularAxis = new RegularAxis();
        regularAxis.setAxisLabel("ansi");
        regularAxis.setLowerBound(new String("1"));
        regularAxis.setUpperBound(new String("31"));
        regularAxis.setResolution(new BigDecimal("1"));

        List<GeoAxis> regularAxisList = new ArrayList<>();
        regularAxisList.add(regularAxis);


        List<IndexAxis> axisList = new ArrayList<>();
        IndexAxis indexAxis = new IndexAxis();
        indexAxis.setAxisLabel("i");
        indexAxis.setLowerBound(new Long("1"));
        indexAxis.setUpperBound(new Long("31"));
        axisList.add(indexAxis);

        GridLimits gridLimits = new GridLimits();
        gridLimits.setSrsName(GRID_LIMITS_NAME);
        gridLimits.setIndexAxes(axisList);

        GeneralGrid generalGrid = new GeneralGrid();
        generalGrid.setSrsName(GENERAL_GRID_NAME);        
        generalGrid.setGeoAxes(regularAxisList);
        generalGrid.setGridLimits(gridLimits);

        GeneralGridDomainSet domainSet = new GeneralGridDomainSet();
        domainSet.setGeneralGrid(generalGrid);

        return domainSet;
    }

    private RasdamanRangeSet createRangeSet()
    {
        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
        rasdamanRangeSet.setMddType("MddTypeCoverage1DRegular");
        rasdamanRangeSet.setOid(new Long("1"));
        rasdamanRangeSet.setCollectionName("CollectionNameCoverage1DRegular");
        rasdamanRangeSet.setCollectionType("CollectionTypeCoverage1DRegular");

        return rasdamanRangeSet;
    }

    private RangeType createRangeType()
    {
        Uom uom = new Uom();

        Quantity quantity = new Quantity();
        quantity.setDefinition("http://www.opengis.net/def/dataType/OGC/0/unsignedInt");
        quantity.setUom(uom);

        Field field = new Field();
        field.setName("adventskalendertuerchen");
        field.setQuantity(quantity);

        List<Field> fieldList= new ArrayList<>();
        fieldList.add(field);
        DataRecord dataRecord = new DataRecord(fieldList);

        RangeType rangeType = new RangeType();
        rangeType.setDataRecord(dataRecord);

        return rangeType;
    }

}
