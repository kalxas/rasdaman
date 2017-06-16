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
import org.rasdaman.domain.cis.IrregularAxis;
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
import org.rasdaman.repository.interfaces.CoverageRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test persist a coverage with 3D axes containing time axis with Hibernate
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {Application.class})
public class TestCoverage3DTimeseriesMultipart {

    public static final String ENVELOPE_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4327&2=http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/AnsiDate";
    public static final String GENERAL_GRID_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4326&2=http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/AnsiDate";
    public static final String GRID_LIMITS_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/Index4D";

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

        //Test for envelope
        assertEquals(ENVELOPE_NAME,coverageFetched.getEnvelope().getEnvelopeByAxis().getSrsName());
        assertEquals(3,coverageFetched.getEnvelope().getEnvelopeByAxis().getSrsDimension());
        assertEquals("Lat Long ansi",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisLabels());
        assertEquals("Lat",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getAxisLabel());
        assertEquals("-90",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getLowerBound());
        assertEquals("-80",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getUpperBound());
        assertEquals("deg",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getUomLabel());

        assertEquals("Long",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getAxisLabel());
        assertEquals("0",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getLowerBound());
        assertEquals("10",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getUpperBound());
        assertEquals("deg",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getUomLabel());

        assertEquals("ansi",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(2).getAxisLabel());
        assertEquals("2015-12-01",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(2).getLowerBound());
        assertEquals("2015-12-31",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(2).getUpperBound());
        assertEquals("d",coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(2).getUomLabel());


        //Test for RangeSet
        assertEquals("MddTypeCoverage3DTimeseriesMultipart",coverageFetched.getRasdamanRangeSet().getMddType());
        assertEquals(BigInteger.ONE,coverageFetched.getRasdamanRangeSet().getOid());
        assertEquals("CollectionNameCoverage3DTimeseriesMultipart",coverageFetched.getRasdamanRangeSet().getCollectionName());
        assertEquals("CollectionTypeCoverage3DTimeseriesMultipart",coverageFetched.getRasdamanRangeSet().getCollectionType());

        //Test for RangeType
        assertEquals("http://www.opengis.net/def/dataType/OGC/0/unsignedInt",coverageFetched.getRangeType().getDataRecord().getFields().get(0).getQuantity().getDefinition());
        assertEquals("panchromatic",coverageFetched.getRangeType().getDataRecord().getFields().get(0).getName());
        assertEquals("10^0",coverageFetched.getRangeType().getDataRecord().getFields().get(0).getQuantity().getUom().getCode());

        //Test for Domain List
        GeneralGrid generalGrid = ((GeneralGridDomainSet)coverageFetched.getDomainSet()).getGeneralGrid();
        RegularAxis regularAxisFetched1 = (RegularAxis)generalGrid.getGeoAxes().get(0);
        RegularAxis regularAxisFetched2 = (RegularAxis)generalGrid.getGeoAxes().get(1);

        //Test for regular axis 1
        assertEquals(0,new BigDecimal("5").compareTo(regularAxisFetched1.getResolution()));
        assertEquals("Lat",regularAxisFetched1.getAxisLabel());
        assertEquals(new BigDecimal("-90"),regularAxisFetched1.getLowerBound());
        assertEquals(new BigDecimal("-80"),regularAxisFetched1.getUpperBound());
        assertEquals("deg",regularAxisFetched1.getUomLabel());

        //Test for regular axis 2
        assertEquals(0,new BigDecimal("5").compareTo(regularAxisFetched2.getResolution()));
        assertEquals("Long",regularAxisFetched2.getAxisLabel());
        assertEquals(new String("0"),regularAxisFetched2.getLowerBound());
        assertEquals(new String("10"),regularAxisFetched2.getUpperBound());
        assertEquals("deg",regularAxisFetched2.getUomLabel());

        IrregularAxis irregularAxisFetched = (IrregularAxis)generalGrid.getGeoAxes().get(2);

        assertEquals("ansi", irregularAxisFetched.getAxisLabel());
        assertEquals("d", irregularAxisFetched.getUomLabel());        

//        for(int i=1;i<=31;++i)
//        {
//            if(i>9)
//                assertEquals("2015-12-" + String.valueOf(i), irregularAxisFetched.getDirectPositions().get(i-1));
//            else
//                assertEquals("2015-12-0" + String.valueOf(i), irregularAxisFetched.getDirectPositions().get(i-1));
//        }
        for (int i = 1; i<=31; ++i){
            assertEquals(new BigDecimal(String.valueOf(i)), irregularAxisFetched.getDirectPositionsNumber().get(i-1));
        }

        IndexAxis indexAxis1 = generalGrid.getGridLimits().getIndexAxes().get(0);
        IndexAxis indexAxis2 = generalGrid.getGridLimits().getIndexAxes().get(1);
        IndexAxis indexAxis3 = generalGrid.getGridLimits().getIndexAxes().get(2);

        assertEquals("i",indexAxis1.getAxisLabel());
        assertEquals(new String("0"),indexAxis1.getLowerBound());
        assertEquals(new String("2"),indexAxis1.getUpperBound());

        assertEquals("j",indexAxis2.getAxisLabel());
        assertEquals(new String("0"),indexAxis2.getLowerBound());
        assertEquals(new String("2"),indexAxis2.getUpperBound());

        assertEquals("k",indexAxis3.getAxisLabel());
        assertEquals(new String("0"),indexAxis3.getLowerBound());
        assertEquals(new String("30"),indexAxis3.getUpperBound());

        //Grid Limits
        assertEquals(GRID_LIMITS_NAME,generalGrid.getGridLimits().getSrsName());
 
        //GeneralGrid        
        assertEquals(GENERAL_GRID_NAME, generalGrid.getSrsName());
    }


    private EnvelopeByAxis createEnvelopeByAxis()
    {
        List<AxisExtent> axisExtentList = new LinkedList<>();

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

        AxisExtent axisExtent3 = new AxisExtent();
        axisExtent3.setAxisLabel("ansi");
        axisExtent3.setUomLabel("d");
        axisExtent3.setLowerBound("2015-12-01");
        axisExtent3.setUpperBound("2015-12-31");

        axisExtentList.add(axisExtent);
        axisExtentList.add(axisExtent2);
        axisExtentList.add(axisExtent3);

        EnvelopeByAxis envelope = new EnvelopeByAxis();
        envelope.setSrsName(ENVELOPE_NAME);
        envelope.setSrsDimension(3);
        envelope.setAxisLabels("Lat Long ansi");
        envelope.setAxisExtents(axisExtentList);

        return envelope;
    }

    private DomainSet createDomainSet()
    {
        RegularAxis regularAxis = new RegularAxis();
        regularAxis.setAxisLabel("Lat");
        regularAxis.setLowerBound(new String("-90"));
        regularAxis.setUpperBound(new String("-80"));
        regularAxis.setResolution(new BigDecimal("5"));
        regularAxis.setUomLabel("deg");

        RegularAxis regularAxis2 = new RegularAxis();
        regularAxis2.setAxisLabel("Long");
        regularAxis2.setLowerBound(new String("0"));
        regularAxis2.setUpperBound(new String("10"));
        regularAxis2.setResolution(new BigDecimal("5"));
        regularAxis2.setUomLabel("deg");

        IrregularAxis irregularAxis = new IrregularAxis();
        irregularAxis.setAxisLabel("ansi");
        irregularAxis.setUomLabel("d");        

        List<BigDecimal> directPositionsList = new LinkedList<>();
//        for(int i = 1; i<=31; ++i)
//            if(i>9)
//                directPositionsList.add(new PetaDecimal("2015-12-" + String.valueOf(i)));
//            else
//                directPositionsList.add(new PetaDecimal("2015-12-0" + String.valueOf(i)));
        for(int i = 1; i<=31; ++i){
            directPositionsList.add(new BigDecimal(String.valueOf(i)));
        }

        irregularAxis.setDirectPositions(directPositionsList);

        List<GeoAxis> AxisList = new LinkedList<>();
        AxisList.add(regularAxis);
        AxisList.add(regularAxis2);
        AxisList.add(irregularAxis);

        List<IndexAxis> axisList = new LinkedList<>();
        IndexAxis indexAxis = new IndexAxis();
        indexAxis.setAxisLabel("i");
        indexAxis.setLowerBound(new Long("0"));
        indexAxis.setUpperBound(new Long("2"));

        IndexAxis indexAxis2 = new IndexAxis();
        indexAxis2.setAxisLabel("j");
        indexAxis2.setLowerBound(new Long("0"));
        indexAxis2.setUpperBound(new Long("2"));

        IndexAxis indexAxis3 = new IndexAxis();
        indexAxis3.setAxisLabel("k");
        indexAxis3.setLowerBound(new Long("0"));
        indexAxis3.setUpperBound(new Long("30"));

        axisList.add(indexAxis);
        axisList.add(indexAxis2);
        axisList.add(indexAxis3);

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

    private RasdamanRangeSet createRangeSet()
    {
        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
        rasdamanRangeSet.setMddType("MddTypeCoverage3DTimeseriesMultipart");
        rasdamanRangeSet.setOid(new Long("1"));
        rasdamanRangeSet.setCollectionName("CollectionNameCoverage3DTimeseriesMultipart");
        rasdamanRangeSet.setCollectionType("CollectionTypeCoverage3DTimeseriesMultipart");

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
        field.setName("panchromatic");
        field.setQuantity(quantity);

        List<Field> fieldList= new LinkedList<>();
        fieldList.add(field);
        DataRecord dataRecord = new DataRecord(fieldList);

        RangeType rangeType = new RangeType();
        rangeType.setDataRecord(dataRecord);

        return rangeType;
    }

}
