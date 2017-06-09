package org.rasdaman.cis;

import java.math.BigDecimal;

import org.rasdaman.domain.cis.*;
import org.rasdaman.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.repository.interfaces.AbstractCoverageRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test persist a 2D coverage with interpolation restrictions with Hibernate
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {Application.class})
public class TestCoverage2DInterpolation {

    private static final String ENVELOPE_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4326";
    private static final String GRID_LIMITS_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/Index2D";
    private static final String GENERAL_GRID_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4326";
    @Autowired private
    AbstractCoverageRepository abstractCoverageRepository;

    @Test
    public void checkCoverage() {
        EnvelopeByAxis envelopeByAxis = createEnvelopeByAxis();
        Envelope envelope = new Envelope(envelopeByAxis);
        DomainSet domainSet = createDomainSet();
        RasdamanRangeSet rasdamanRangeSet = createRangeSet();
        RangeType rangeType = createRangeType();
        InterpolationRestriction interpolationRestriction = createInterpolationRestriction();
        rangeType.setInterpolationRestriction(interpolationRestriction);

        Coverage coverage = new GeneralGridCoverage();
        coverage.setEnvelope(envelope);
        coverage.setDomainSet(domainSet);
        coverage.setRangeType(rangeType);
        coverage.setRasdamanRangeSet(rasdamanRangeSet);

        abstractCoverageRepository.save(coverage);

        Coverage coverageFetched = abstractCoverageRepository.findOneByCoverageId(coverage.getCoverageId());
        domainSet = coverageFetched.getDomainSet();

        GeneralGrid generalGrid = ((GeneralGridDomainSet)domainSet).getGeneralGrid();

        //Test for envelope
        assertEquals(ENVELOPE_NAME,coverageFetched.getEnvelope().getEnvelopeByAxis().getSrsName());
        assertEquals(2,coverageFetched.getEnvelope().getEnvelopeByAxis().getSrsDimension());
        assertEquals("Lat Long",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisLabels());
        
        AxisExtent axisExtent = coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0);
        
        assertEquals("Lat", axisExtent.getAxisLabel());
        assertEquals("-90", axisExtent.getLowerBound());
        assertEquals("-80", axisExtent.getUpperBound());
        assertEquals("deg", axisExtent.getUomLabel());
        
        axisExtent = coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1);

        assertEquals("Long", axisExtent.getAxisLabel());
        assertEquals("0", axisExtent.getLowerBound());
        assertEquals("10", axisExtent.getUpperBound());
        assertEquals("deg", axisExtent.getUomLabel());

        //Test for RangeSet
        assertEquals("MddTypeCoverage2DInterpolation",coverageFetched.getRasdamanRangeSet().getMddType());
        assertEquals(BigInteger.ONE,coverageFetched.getRasdamanRangeSet().getOid());
        assertEquals("CollectionNameCoverage2DInterpolation",coverageFetched.getRasdamanRangeSet().getCollectionName());
        assertEquals("CollectionTypeCoverage2DInterpolation",coverageFetched.getRasdamanRangeSet().getCollectionType());

        //Test for RangeType
        Field field = coverageFetched.getRangeType().getDataRecord().getFields().get(0);
        assertEquals("http://www.opengis.net/def/dataType/OGC/0/unsignedInt", field.getQuantity().getDefinition());
        assertEquals("singleBand", field.getName());
        assertEquals("10^0", field.getQuantity().getUom().getCode());

        //Test for Domain List        
        RegularAxis regularAxisFetched1 = (RegularAxis)generalGrid.getGeoAxes().get(0);
        RegularAxis regularAxisFetched2 = (RegularAxis)generalGrid.getGeoAxes().get(1);

        //Test for regular axis 1
        assertEquals(0,new BigDecimal("5").compareTo(regularAxisFetched1.getResolution()));
        assertEquals("Lat",regularAxisFetched1.getAxisLabel());
        assertEquals(new BigDecimal("-90"),regularAxisFetched1.getLowerBound());
        assertEquals(new BigDecimal("-80"),regularAxisFetched1.getUpperBound());

        //Test for regular axis 2
        assertEquals(0,new BigDecimal("5").compareTo(regularAxisFetched2.getResolution()));
        assertEquals("Long",regularAxisFetched2.getAxisLabel());
        assertEquals(new String("0"),regularAxisFetched2.getLowerBound());
        assertEquals(new String("10"),regularAxisFetched2.getUpperBound());
        
        IndexAxis indexAxis1 = generalGrid.getGridLimits().getIndexAxes().get(0);
        IndexAxis indexAxis2 = generalGrid.getGridLimits().getIndexAxes().get(1);

        assertEquals("i",indexAxis1.getAxisLabel());
        assertEquals(new String("0"),indexAxis1.getLowerBound());
        assertEquals(new String("2"),indexAxis1.getUpperBound());

        assertEquals("j",indexAxis2.getAxisLabel());
        assertEquals(new String("0"),indexAxis2.getLowerBound());
        assertEquals(new String("2"),indexAxis2.getUpperBound());


        //Grid Limits
        assertEquals(GRID_LIMITS_NAME,generalGrid.getGridLimits().getSrsName());

        //GeneralGrid        
        assertEquals(GENERAL_GRID_NAME,generalGrid.getSrsName());

        //Interpolation
        assertEquals("http://www.opengis.net/def/interpolation/OGC/1/linear",
                     coverageFetched.getRangeType().getInterpolationRestriction().getAllowedInterpolations().get(0));
        assertEquals("http://www.opengis.net/def/interpolation/OGC/1/nearest-neighbor",
                     coverageFetched.getRangeType().getInterpolationRestriction().getAllowedInterpolations().get(1));

    }


    private DomainSet createDomainSet()
    {
        RegularAxis regularAxis = new RegularAxis();
        regularAxis.setAxisLabel("Lat");
        regularAxis.setLowerBound(new String("-90"));
        regularAxis.setUpperBound(new String("-80"));
        regularAxis.setResolution(new BigDecimal("5"));

        RegularAxis regularAxis2 = new RegularAxis();
        regularAxis2.setAxisLabel("Long");
        regularAxis2.setLowerBound(new String("0"));
        regularAxis2.setUpperBound(new String("10"));
        regularAxis2.setResolution(new BigDecimal("5"));

        List<GeoAxis> AxisList = new ArrayList<>();
        AxisList.add(regularAxis);
        AxisList.add(regularAxis2);

        List<IndexAxis> axisList = new ArrayList<>();
        IndexAxis indexAxis = new IndexAxis();
        indexAxis.setAxisLabel("i");
        indexAxis.setLowerBound(new Long("0"));
        indexAxis.setUpperBound(new Long("2"));

        IndexAxis indexAxis2 = new IndexAxis();
        indexAxis2.setAxisLabel("j");
        indexAxis2.setLowerBound(new Long("0"));
        indexAxis2.setUpperBound(new Long("2"));

        axisList.add(indexAxis);
        axisList.add(indexAxis2);

        GridLimits gridLimits = new GridLimits();
        gridLimits.setSrsName(GRID_LIMITS_NAME);
        gridLimits.setIndexAxes(axisList);

        GeneralGrid generalGrid = new GeneralGrid();
        generalGrid.setSrsName(GENERAL_GRID_NAME);
        generalGrid.setGeoAxes(AxisList);
        generalGrid.setGridLimits(gridLimits);

        DomainSet domainSet = new GeneralGridDomainSet();
        ((GeneralGridDomainSet)domainSet).setGeneralGrid(generalGrid);

        return domainSet;
    }

    private EnvelopeByAxis createEnvelopeByAxis()
    {
        List<AxisExtent> axisExtentList = new ArrayList<>();

        AxisExtent axisExtent = new AxisExtent();
        axisExtent.setAxisLabel("Lat");
        axisExtent.setUomLabel("deg");
        axisExtent.setLowerBound("-90");
        axisExtent.setUpperBound("-80");

        AxisExtent axisExtent2 = new AxisExtent();
        axisExtent2.setAxisLabel("Long");
        axisExtent2.setUomLabel("deg");
        axisExtent2.setLowerBound("0");
        axisExtent2.setUpperBound("10");

        axisExtentList.add(axisExtent);
        axisExtentList.add(axisExtent2);

        EnvelopeByAxis envelope = new EnvelopeByAxis();
        envelope.setSrsName(ENVELOPE_NAME);
        envelope.setSrsDimension(2);
        envelope.setAxisLabels("Lat Long");
        envelope.setAxisExtents(axisExtentList);

        return envelope;
    }

    private RasdamanRangeSet createRangeSet()
    {
        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
        rasdamanRangeSet.setMddType("MddTypeCoverage2DInterpolation");
        rasdamanRangeSet.setOid(new Long("1"));
        rasdamanRangeSet.setCollectionName("CollectionNameCoverage2DInterpolation");
        rasdamanRangeSet.setCollectionType("CollectionTypeCoverage2DInterpolation");

        return rasdamanRangeSet;
    }


    private RangeType createRangeType()
    {
        Uom uom = new Uom();
        uom.setCode("10^0");

        Quantity quantity = new Quantity();
        quantity.setDefinition("http://www.opengis.net/def/dataType/OGC/0/unsignedInt");
        quantity.setUom(uom);

        Field field = new Field();
        field.setName("singleBand");
        field.setQuantity(quantity);

        List<Field> fieldList= new ArrayList<>();
        fieldList.add(field);
        DataRecord dataRecord = new DataRecord(fieldList);

        RangeType rangeType = new RangeType();
        rangeType.setDataRecord(dataRecord);

        return rangeType;
    }

    private InterpolationRestriction createInterpolationRestriction()
    {
        InterpolationRestriction interpolationRestriction = new InterpolationRestriction();

        List<String> allowedInterpolation = new ArrayList<>();
        allowedInterpolation.add("http://www.opengis.net/def/interpolation/OGC/1/linear");
        allowedInterpolation.add("http://www.opengis.net/def/interpolation/OGC/1/nearest-neighbor");        

        interpolationRestriction.setAllowedInterpolations(allowedInterpolation);

        return interpolationRestriction;
    }

}
