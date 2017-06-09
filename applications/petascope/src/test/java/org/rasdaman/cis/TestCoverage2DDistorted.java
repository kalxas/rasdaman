package org.rasdaman.cis;

//package com.rasdaman.cis;
//
//
//
//import java.math.BigDecimal;
//import org.rasdaman.cis.domain.Coverage;
//import org.rasdaman.cis.domain.DomainSet;
//import org.rasdaman.cis.domain.Axis;
//import org.rasdaman.cis.domain.Uom;
//import org.rasdaman.cis.domain.RasdamanRangeSet;
//import org.rasdaman.cis.domain.RangeType;
//import org.rasdaman.cis.domain.GridLimits;
//import org.rasdaman.cis.domain.GeneralGrid;
//import org.rasdaman.cis.domain.IndexAxis;
//import org.rasdaman.cis.domain.DistortedAxis;
//import org.rasdaman.cis.domain.AxisExtent;
//import org.rasdaman.cis.domain.Field;
//import org.rasdaman.cis.domain.Quantity;
//import org.rasdaman.cis.domain.Displacement;
//import org.rasdaman.cis.domain.EnvelopeByAxis;
//import org.rasdaman.cis.Application;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import java.util.LinkedList;
//import java.util.List;
//import org.rasdaman.cis.domain.DataRecord;
//import org.rasdaman.cis.domain.Envelope;
//import org.rasdaman.cis.repository.AbstractCoverageRepository;
//
///**
// * Created by andreibadoi on 20/05/16.
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = Application.class)
//public class TestCoverage2DDistorted {
//
//    private static final String DEFINITION = "http://www.opengis.net/def/dataType/OGC/0/unsignedInt";
//    private static String ENVELOPE_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4327";
//    private static String GRIDLIMITS_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/Index2D";
//
//    @Autowired private
//    private AbstractCoverageRepository coverageRepositroy;
//
//    @Test
//    public void checkCoverage(){
//        Coverage coverage = createCoverage();
//        coverageRepositroy.save(coverage);
//        Assert.assertNotNull(coverage.getId());
//        long coverageCount = coverageRepositroy.count();
//        Assert.assertEquals(coverageCount, 1);
//        Coverage coverageTest = coverageRepositroy.findOne(coverage.getId());
//        Assert.assertEquals(coverageTest.getEnvelope().getEnvelopeByAxis().getSrsName(),ENVELOPE_NAME);
//        Assert.assertEquals(coverageTest.getEnvelope().getEnvelopeByAxis().getSrsDimension(),3);
//        Assert.assertEquals(coverageTest.getEnvelope().getEnvelopeByAxis().getAxisLabels(),"Lat Long h");
//
//        List<AxisExtent> axisListTest = coverageTest.getEnvelope().getEnvelopeByAxis().getAxisExtent();
//        Assert.assertEquals(axisListTest.get(0).getAxisLabel(),"Lat");
//        Assert.assertEquals(axisListTest.get(0).getUomLabel(), "deg");
//        Assert.assertEquals(axisListTest.get(0).getLowerBound(), "-90");
//        Assert.assertEquals(axisListTest.get(0).getUpperBound(), "-80");
//        Assert.assertEquals(axisListTest.get(1).getAxisLabel(),"Long");
//        Assert.assertEquals(axisListTest.get(1).getUomLabel(), "deg");
//        Assert.assertEquals(axisListTest.get(1).getLowerBound(), "0");
//        Assert.assertEquals(axisListTest.get(1).getUpperBound(), "10");
//        Assert.assertEquals(axisListTest.get(2).getAxisLabel(),"h");
//        Assert.assertEquals(axisListTest.get(2).getUomLabel(), "m");
//        Assert.assertEquals(axisListTest.get(2).getLowerBound(), "0");
//        Assert.assertEquals(axisListTest.get(2).getUpperBound(), "1000");
//
//        GeneralGrid generalGridTest = (GeneralGrid)coverageTest.getDomainSet().getGeneralGrid();
//        Assert.assertEquals(generalGridTest.getSrsName(),ENVELOPE_NAME);        
//        Assert.assertEquals(generalGridTest.getGeoAxes().get(0).getAxisName(), "Lat");
//        Assert.assertEquals(generalGridTest.getGeoAxes().get(0).getLowerBound(), new BigDecimal("-90"));
//        Assert.assertEquals(generalGridTest.getGeoAxes().get(0).getUpperBound(), new BigDecimal("-80"));
//        Assert.assertEquals(generalGridTest.getGeoAxes().get(1).getAxisName(), "Long");
//        Assert.assertEquals(generalGridTest.getGeoAxes().get(1).getLowerBound(), new BigDecimal("0"));
//        Assert.assertEquals(generalGridTest.getGeoAxes().get(1).getUpperBound(), new BigDecimal("10"));
//        Assert.assertEquals(generalGridTest.getGeoAxes().get(2).getAxisName(), "h");
//        Assert.assertEquals(generalGridTest.getGeoAxes().get(2).getLowerBound(), new BigDecimal("0"));
//        Assert.assertEquals(generalGridTest.getGeoAxes().get(2).getUpperBound(), new BigDecimal("1000"));
//        Assert.assertEquals(generalGridTest.getDisplacement().get().getDirectPositions().get(0), "lat1 long1 h1");
//        Assert.assertEquals(generalGridTest.getDisplacement().get().getDirectPositions().get(1), "lat2 long2 h2");
//        Assert.assertEquals(generalGridTest.getDisplacement().get().getSequenceRule(),"Hilbert");
//        Assert.assertEquals(generalGridTest.getGridLimits().get().getSrsName(),GRIDLIMITS_NAME);
//        Assert.assertEquals(generalGridTest.getGridLimits().get().getIndexAxes().get(0).getAxisName(),"i");
//        Assert.assertEquals(generalGridTest.getGridLimits().get().getIndexAxes().get(0).getLowerBound(),
//                new BigDecimal("0"));
//        Assert.assertEquals(generalGridTest.getGridLimits().get().getIndexAxes().get(0).getUpperBound(),
//                new BigDecimal("2"));
//
//        Assert.assertEquals(generalGridTest.getGridLimits().get().getIndexAxes().get(1).getAxisName(),"j");
//        Assert.assertEquals(generalGridTest.getGridLimits().get().getIndexAxes().get(1).getLowerBound(),
//                new BigDecimal("0"));
//        Assert.assertEquals(generalGridTest.getGridLimits().get().getIndexAxes().get(1).getUpperBound(),
//                new BigDecimal("2"));
//
//        RangeType rangeTypeTest = coverageTest.getRangeType();
//        Assert.assertEquals(rangeTypeTest.getDataRecord().getFields().get(0).getName(),"singleBand");
//        Assert.assertEquals(rangeTypeTest.getDataRecord().getFields().get(0).getQuantity().getDefinition(),DEFINITION);
//
//
//
//    }
//
//    public Coverage createCoverage() {
//        EnvelopeByAxis envelopeByAxis = new EnvelopeByAxis();
//        envelopeByAxis.setSrsName(ENVELOPE_NAME);
//        envelopeByAxis.setAxisLabels("Lat Long h");
//        List<AxisExtent> axisExtentList = new LinkedList<>();
//        AxisExtent axis1 = new AxisExtent("Lat","deg","-90","-80");
//        AxisExtent axis2 = new AxisExtent("Long","deg","0","10");
//        AxisExtent axis3 = new AxisExtent("h","m","0","1000");
//        axisExtentList.add(axis1);
//        axisExtentList.add(axis2);
//        axisExtentList.add(axis3);
//        envelopeByAxis.setSrsDimension(3);
//        envelopeByAxis.setAxisExtent(axisExtentList);
//        
//        Envelope envelope = new Envelope(envelopeByAxis);
//
//        List<Axis> axisList = new LinkedList<>();
//        DistortedAxis distortedAxis1 = new DistortedAxis();
//        distortedAxis1.setAxisName("Lat");
//        distortedAxis1.setLowerBound(new String("-90"));
//        distortedAxis1.setUpperBound(new String("-80"));
//        axisList.add(distortedAxis1);
//        DistortedAxis distortedAxis2 = new DistortedAxis();
//        distortedAxis2.setAxisName("Long");
//        distortedAxis2.setLowerBound(new String("0"));
//        distortedAxis2.setUpperBound(new String("10"));
//        axisList.add(distortedAxis2);
//        DistortedAxis distortedAxis3 = new DistortedAxis();
//        distortedAxis3.setAxisName("h");
//        distortedAxis3.setLowerBound(new String("0"));
//        distortedAxis3.setUpperBound(new String("1000"));
//        axisList.add(distortedAxis3);
//        Displacement displacement = new Displacement();
//        displacement.setSequenceRule("Hilbert");
//        List<String> directPosition = new LinkedList<>();
//        directPosition.add("lat1 long1 h1");
//        directPosition.add("lat2 long2 h2");
//        displacement.setDirectPositions(directPosition);
//
//        GridLimits gridLimits = new GridLimits();
//        gridLimits.setSrsName(GRIDLIMITS_NAME);
//        List<IndexAxis> indexAxisesList = new LinkedList<>();
//        IndexAxis indexAxis1 = new IndexAxis();
//        indexAxis1.setAxisName("i");
//        indexAxis1.setLowerBound(new String("0"));
//        indexAxis1.setUpperBound(new String("2"));
//        IndexAxis indexAxis2 = new IndexAxis();
//        indexAxis2.setAxisName("j");
//        indexAxis2.setLowerBound(new String("0"));
//        indexAxis2.setUpperBound(new String("2"));
//        indexAxisesList.add(indexAxis1);
//        indexAxisesList.add(indexAxis2);
//        gridLimits.setIndexAxes(indexAxisesList);
//
//        GeneralGrid generalGrid =
//                new GeneralGrid("http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4327",
//                        axisList);
//        generalGrid.setDisplacement(displacement);
//        generalGrid.setGridLimits(gridLimits);
//
//        DomainSet domainSet = new DomainSet(generalGrid);
//
//        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
//        Uom uom = new Uom();
//        Quantity quantity = new Quantity();
//        quantity.setDefinition(DEFINITION);
//        quantity.setUom(uom);
//        Field field = new Field();
//        field.setName("singleBand");
//        field.setQuantity(quantity);
//        List<Field> fieldList = new LinkedList<>();
//        fieldList.add(field);
//        DataRecord dataRecord = new DataRecord(fieldList);
//        
//        RangeType rangeType = new RangeType();
//        rangeType.setDataRecord(dataRecord);
//
//        Coverage coverage = new Coverage();
//        coverage.setEnvelope(envelope);
//        coverage.setDomainSet(domainSet);
//        coverage.setRangeType(rangeType);
//        coverage.setRasdamanRangeSet(rasdamanRangeSet);
//        return coverage;
//    }
//}
