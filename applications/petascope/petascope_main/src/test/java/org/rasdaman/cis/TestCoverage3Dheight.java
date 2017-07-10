package org.rasdaman.cis;

import org.rasdaman.domain.cis.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import org.rasdaman.ApplicationMain;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.repository.interfaces.CoverageRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test persist a 3D coverage with high axis with Hibernate
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {ApplicationMain.class})
public class TestCoverage3Dheight {

    private static final String DEFINITION = "http://www.opengis.net/def/dataType/OGC/0/unsignedInt";
    private static final String ENVELOPE_NAME_3D = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4327";
    private static final String GRID_LIMITS_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/Index3D";

    @Autowired
    private CoverageRepository abstractCoverageRepository;

    @Test
    public void checkCoverage() {
        Coverage coverage = createCoverage();
        abstractCoverageRepository.save(coverage);
        Assert.assertNotNull(coverage.getCoverageId());
        long coverageCount = abstractCoverageRepository.count();
        Assert.assertEquals(coverageCount, 1);
        Coverage coverageTest = abstractCoverageRepository.findOne(coverage.getCoverageId());
        Assert.assertEquals(coverageTest.getEnvelope().getEnvelopeByAxis().getSrsName(), ENVELOPE_NAME_3D);
        Assert.assertEquals(coverageTest.getEnvelope().getEnvelopeByAxis().getSrsDimension(), 3);

        List<AxisExtent> axisListTest = coverageTest.getEnvelope().getEnvelopeByAxis().getAxisExtents();
        Assert.assertEquals(axisListTest.get(0).getAxisLabel(), "Lat");
        Assert.assertEquals(axisListTest.get(0).getUomLabel(), "deg");
        Assert.assertEquals(axisListTest.get(0).getLowerBound(), "-90");
        Assert.assertEquals(axisListTest.get(0).getUpperBound(), "-80");
        Assert.assertEquals(axisListTest.get(1).getAxisLabel(), "Long");
        Assert.assertEquals(axisListTest.get(1).getUomLabel(), "deg");
        Assert.assertEquals(axisListTest.get(1).getLowerBound(), "0");
        Assert.assertEquals(axisListTest.get(1).getUpperBound(), "10");
        Assert.assertEquals(axisListTest.get(2).getAxisLabel(), "h");
        Assert.assertEquals(axisListTest.get(2).getUomLabel(), "m");
        Assert.assertEquals(axisListTest.get(2).getLowerBound(), "0");
        Assert.assertEquals(axisListTest.get(2).getUpperBound(), "1000");

        GeneralGrid generalGridTest = ((GeneralGridDomainSet) coverageTest.getDomainSet()).getGeneralGrid();
        Assert.assertEquals(generalGridTest.getSrsName(), ENVELOPE_NAME_3D);

        RegularAxis regularAxis = (RegularAxis) generalGridTest.getGeoAxes().get(0);
        Assert.assertEquals(regularAxis.getAxisLabel(), "Lat");
        Assert.assertEquals(regularAxis.getLowerBound(), new BigDecimal("-90"));
        Assert.assertEquals(regularAxis.getUpperBound(), new BigDecimal("-80"));
        Assert.assertEquals(0, regularAxis.getResolution().compareTo(new BigDecimal("5")));

        regularAxis = (RegularAxis) generalGridTest.getGeoAxes().get(1);
        Assert.assertEquals(regularAxis.getAxisLabel(), "Long");
        Assert.assertEquals(regularAxis.getLowerBound(), new BigDecimal("0"));
        Assert.assertEquals(regularAxis.getUpperBound(), new BigDecimal("10"));

        Assert.assertEquals(0, regularAxis.getResolution().compareTo(new BigDecimal("5")));

        IrregularAxis irregularAxis = (IrregularAxis) generalGridTest.getGeoAxes().get(2);
        Assert.assertEquals(irregularAxis.getAxisLabel(), "h");
        Assert.assertEquals(new BigDecimal("0"), irregularAxis.getDirectPositionsNumber().get(0));
        Assert.assertEquals(new BigDecimal("100"), irregularAxis.getDirectPositionsNumber().get(1));
        Assert.assertEquals(new BigDecimal("1000"), irregularAxis.getDirectPositionsNumber().get(2));

        Assert.assertEquals(generalGridTest.getGridLimits().getSrsName(), GRID_LIMITS_NAME);
        Assert.assertEquals(generalGridTest.getGridLimits().getIndexAxes().get(0).getAxisLabel(), "i");
        Assert.assertEquals(generalGridTest.getGridLimits().getIndexAxes().get(0).getLowerBound(),
                new BigDecimal("0"));
        Assert.assertEquals(generalGridTest.getGridLimits().getIndexAxes().get(0).getUpperBound(),
                new BigDecimal("4"));

        Assert.assertEquals(generalGridTest.getGridLimits().getIndexAxes().get(1).getAxisLabel(), "j");
        Assert.assertEquals(generalGridTest.getGridLimits().getIndexAxes().get(1).getLowerBound(),
                new BigDecimal("0"));
        Assert.assertEquals(generalGridTest.getGridLimits().getIndexAxes().get(2).getUpperBound(),
                new BigDecimal("3"));

        Assert.assertEquals(generalGridTest.getGridLimits().getIndexAxes().get(2).getAxisLabel(), "k");
        Assert.assertEquals(generalGridTest.getGridLimits().getIndexAxes().get(2).getLowerBound(),
                new BigDecimal("0"));
        Assert.assertEquals(generalGridTest.getGridLimits().getIndexAxes().get(2).getUpperBound(),
                new BigDecimal("3"));

        RangeType rangeTypeTest = coverageTest.getRangeType();
        Assert.assertEquals(rangeTypeTest.getDataRecord().getFields().get(0).getName(), "grey");
        Assert.assertEquals(rangeTypeTest.getDataRecord().getFields().get(0).getQuantity().getDefinition(), DEFINITION);

    }

    public Coverage createCoverage() {
        EnvelopeByAxis envelopeByAxis = new EnvelopeByAxis();
        envelopeByAxis.setSrsName(ENVELOPE_NAME_3D);
        List<AxisExtent> axisExtentList = new LinkedList<>();
        AxisExtent axis1 = new AxisExtent("Lat", "deg", "-90", "-80");
        AxisExtent axis2 = new AxisExtent("Long", "deg", "0", "10");
        AxisExtent axis3 = new AxisExtent("h", "m", "0", "1000");

        axisExtentList.add(axis1);
        axisExtentList.add(axis2);
        axisExtentList.add(axis3);
        envelopeByAxis.setSrsDimension(3);
        envelopeByAxis.setAxisExtents(axisExtentList);

        Envelope envelope = new Envelope(envelopeByAxis);

        List<GeoAxis> axisList = new LinkedList<>();
        RegularAxis regularAxis1 = new RegularAxis();
        regularAxis1.setAxisLabel("Lat");
        regularAxis1.setLowerBound(new String("-90"));
        regularAxis1.setUpperBound(new String("-80"));
        regularAxis1.setResolution(new BigDecimal("5"));
        axisList.add(regularAxis1);

        RegularAxis regularAxis2 = new RegularAxis();
        regularAxis2.setAxisLabel("Long");
        regularAxis2.setLowerBound(new String("0"));
        regularAxis2.setUpperBound(new String("10"));
        regularAxis2.setResolution(new BigDecimal("5"));
        axisList.add(regularAxis2);

        IrregularAxis irregularAxis1 = new IrregularAxis();
        irregularAxis1.setAxisLabel("h");
        List<BigDecimal> directPositionList = new LinkedList<>();

        directPositionList.add(new BigDecimal("0"));
        directPositionList.add(new BigDecimal("100"));
        directPositionList.add(new BigDecimal("1000"));

        irregularAxis1.setDirectPositions(directPositionList);
        axisList.add(irregularAxis1);

        GridLimits gridLimits = new GridLimits();
        List<IndexAxis> indexAxisesList = new LinkedList<>();
        IndexAxis indexAxis1 = new IndexAxis();
        indexAxis1.setAxisLabel("i");
        indexAxis1.setLowerBound(new Long("0"));
        indexAxis1.setUpperBound(new Long("4"));
        IndexAxis indexAxis2 = new IndexAxis();
        indexAxis2.setAxisLabel("j");
        indexAxis2.setLowerBound(new Long("0"));
        indexAxis2.setUpperBound(new Long("2"));
        IndexAxis indexAxis3 = new IndexAxis();
        indexAxis3.setAxisLabel("k");
        indexAxis3.setLowerBound(new Long("0"));
        indexAxis3.setUpperBound(new Long("3"));
        indexAxisesList.add(indexAxis1);
        indexAxisesList.add(indexAxis2);
        indexAxisesList.add(indexAxis3);
        gridLimits.setIndexAxes(indexAxisesList);
        gridLimits.setSrsName(GRID_LIMITS_NAME);
        GeneralGrid generalGrid
                = new GeneralGrid(ENVELOPE_NAME_3D, axisList);
        generalGrid.setGridLimits(gridLimits);
        DomainSet domainSet = new GeneralGridDomainSet(generalGrid);

        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
        Uom uom = new Uom();
        Quantity quantity = new Quantity();
        quantity.setDefinition("http://www.opengis.net/def/dataType/OGC/0/unsignedInt");
        quantity.setUom(uom);
        Field field = new Field();
        field.setName("grey");
        field.setQuantity(quantity);
        List<Field> fieldList = new LinkedList<>();
        fieldList.add(field);
        RangeType rangeType = new RangeType();
        DataRecord dataRecord = new DataRecord(fieldList);
        rangeType.setDataRecord(dataRecord);

        Coverage coverage = new GeneralGridCoverage();
        coverage.setEnvelope(envelope);
        coverage.setDomainSet(domainSet);
        coverage.setRangeType(rangeType);
        coverage.setRasdamanRangeSet(rasdamanRangeSet);

        return coverage;
    }
}
