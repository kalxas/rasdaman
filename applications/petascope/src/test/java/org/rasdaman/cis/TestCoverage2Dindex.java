package org.rasdaman.cis;

import org.rasdaman.domain.cis.*;
import org.rasdaman.Application;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.repository.interfaces.CoverageRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test persist a coverage with 2D Index Axes with Hibernate
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {Application.class})
public class TestCoverage2Dindex {

    private static final String DEFINITION = "http://www.opengis.net/def/dataType/OGC/0/unsignedInt";
    private static final String ENVELOPE_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/Index2D";
    private static final String UOM_LABEL = "GridSpacing";

    @Autowired
    private CoverageRepository abstractCoverageRepositroy;

    @Test
    public void checkCoverage() {
        Coverage coverage = createCoverage();
        abstractCoverageRepositroy.save(coverage);
        Assert.assertNotNull(coverage.getCoverageId());
        long coverageCount = abstractCoverageRepositroy.count();
        Assert.assertEquals(coverageCount, 1);
        Coverage coverageTest = abstractCoverageRepositroy.findOne(coverage.getCoverageId());
        Assert.assertEquals(coverageTest.getEnvelope().getEnvelopeByAxis().getSrsName(), ENVELOPE_NAME);
        Assert.assertEquals(coverageTest.getEnvelope().getEnvelopeByAxis().getSrsDimension(), 2);

        List<AxisExtent> axisListTest = coverageTest.getEnvelope().getEnvelopeByAxis().getAxisExtents();
        Assert.assertEquals(axisListTest.get(0).getAxisLabel(), "i");
        Assert.assertEquals(axisListTest.get(0).getUomLabel(), UOM_LABEL);
        Assert.assertEquals(axisListTest.get(0).getLowerBound(), "0");
        Assert.assertEquals(axisListTest.get(0).getUpperBound(), "2");
        Assert.assertEquals(axisListTest.get(1).getAxisLabel(), "j");
        Assert.assertEquals(axisListTest.get(1).getUomLabel(), UOM_LABEL);
        Assert.assertEquals(axisListTest.get(1).getLowerBound(), "0");
        Assert.assertEquals(axisListTest.get(1).getUpperBound(), "2");

        GeneralGrid generalGridTest = ((GeneralGridDomainSet) coverageTest.getDomainSet()).getGeneralGrid();
        Assert.assertEquals(generalGridTest.getSrsName(), ENVELOPE_NAME);

        RegularAxis regularAxis0 = (RegularAxis) (generalGridTest.getGeoAxes().get(0));
        Assert.assertEquals(regularAxis0.getAxisLabel(), "i");
        Assert.assertEquals(regularAxis0.getLowerBound(), new String("0"));
        Assert.assertEquals(regularAxis0.getUpperBound(), new String("2"));

        RegularAxis regularAxis1 = (RegularAxis) (generalGridTest.getGeoAxes().get(1));
        Assert.assertEquals(regularAxis1.getAxisLabel(), "j");
        Assert.assertEquals(regularAxis1.getLowerBound(), new String("0"));
        Assert.assertEquals(regularAxis1.getUpperBound(), new String("2"));

        RangeType rangeTypeTest = coverageTest.getRangeType();
        Assert.assertEquals(rangeTypeTest.getDataRecord().getFields().get(0).getName(), "singleBand");
        Assert.assertEquals(rangeTypeTest.getDataRecord().getFields().get(0).getQuantity().getDefinition(), DEFINITION);

    }

    public Coverage createCoverage() {
        EnvelopeByAxis envelopeByAxis = new EnvelopeByAxis();
        envelopeByAxis.setSrsName("http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/Index2D");
        List<AxisExtent> axisExtentList = new LinkedList<>();
        AxisExtent axis1 = new AxisExtent("i", "GridSpacing", "0", "2");
        AxisExtent axis2 = new AxisExtent("j", "GridSpacing", "0", "2");
        axisExtentList.add(axis1);
        axisExtentList.add(axis2);
        envelopeByAxis.setSrsDimension(2);
        envelopeByAxis.setAxisExtents(axisExtentList);

        Envelope envelope = new Envelope(envelopeByAxis);

        List<IndexAxis> axisList = new LinkedList<>();
        IndexAxis indexAxis1 = new IndexAxis();
        indexAxis1.setAxisLabel("i");
        indexAxis1.setLowerBound(new Long("0"));
        indexAxis1.setUpperBound(new Long("2"));
        axisList.add(indexAxis1);
        IndexAxis indexAxis2 = new IndexAxis();
        indexAxis2.setAxisLabel("j");
        indexAxis2.setLowerBound(new Long("0"));
        indexAxis2.setUpperBound(new Long("2"));
        axisList.add(indexAxis2);

        GridLimits gridLimits = new GridLimits();
        gridLimits.setIndexAxes(axisList);

        GeneralGrid generalGrid
                = new GeneralGrid("http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/Index2D",
                        null, gridLimits);
        DomainSet domainSet = new GeneralGridDomainSet(generalGrid);

        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
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

        Coverage coverage = new GeneralGridCoverage();
        coverage.setEnvelope(envelope);
        coverage.setDomainSet(domainSet);
        coverage.setRangeType(rangeType);
        coverage.setRasdamanRangeSet(rasdamanRangeSet);
        return coverage;
    }
}
