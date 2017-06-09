package org.rasdaman.cis;

import org.rasdaman.domain.cis.*;
import org.rasdaman.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.repository.interfaces.AbstractCoverageRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test persist a MultiPoint cloud coverage with Hibernate
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {Application.class})
public class TestCoveragePointCloud {

    public static final String ENVELOPE_NAME = "http://www.opengis.net/def/crs/EPSG/0/4979";
    public static final String GENERAL_GRID_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.eecs.jacobs-university.de:8080/def/crs/EPSG/0/4326&2=http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/AnsiDate";
    public static final String GRID_LIMITS_NAME = "http://kahlua.eecs.jacobs-university.de:8080/def/crs/OGC/0/Index4D";

    @Autowired private
    AbstractCoverageRepository abstractCoverageRepository;

    @Test
    public void checkCoverage() {
        EnvelopeByAxis envelopeByAxis = createEnvelopeByAxis();
        Envelope envelope = new Envelope(envelopeByAxis);
        DomainSet domainSet = createMultiPointDomainSet();
        RasdamanRangeSet rasdamanRangeSet = createRangeSet();
        RangeType rangeType = createRangeType();

        Coverage coverage = new MultiPointCoverage();
        coverage.setEnvelope(envelope);
        coverage.setDomainSet(domainSet);
        coverage.setRangeType(rangeType);
        coverage.setRasdamanRangeSet(rasdamanRangeSet);

        abstractCoverageRepository.save(coverage);

        Coverage coverageFetched = abstractCoverageRepository.findOneByCoverageId(coverage.getCoverageId());

        //Test for envelope
        assertEquals(ENVELOPE_NAME, coverageFetched.getEnvelope().getEnvelopeByAxis().getSrsName());
        assertEquals(3, coverageFetched.getEnvelope().getEnvelopeByAxis().getSrsDimension());
        assertEquals("Lat Long h", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisLabels());

        assertEquals("Lat", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getAxisLabel());
        assertEquals("456377.5", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getLowerBound());
        assertEquals("456377.7", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getUpperBound());
        assertEquals("deg", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(0).getUomLabel());

        assertEquals("Long", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getAxisLabel());
        assertEquals("339866.8", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getLowerBound());
        assertEquals("339867.2", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getUpperBound());
        assertEquals("deg", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(1).getUomLabel());

        assertEquals("h", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(2).getAxisLabel());
        assertEquals("52", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(2).getLowerBound());
        assertEquals("53", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(2).getUpperBound());
        assertEquals("deg", coverageFetched.getEnvelope().getEnvelopeByAxis().getAxisExtents().get(2).getUomLabel());

        //Test for RangeSet
        assertEquals("MddTypeCoveragePointCloud", coverageFetched.getRasdamanRangeSet().getMddType());
        assertEquals(BigInteger.ONE, coverageFetched.getRasdamanRangeSet().getOid());
        assertEquals("CollectionNameCoveragePointCloud", coverageFetched.getRasdamanRangeSet().getCollectionName());
        assertEquals("CollectionTypeCoveragePointCloud", coverageFetched.getRasdamanRangeSet().getCollectionType());

        //Test for RangeType
        assertEquals("urn:ogc:def:dataType:OGC:1.1:unsigned char", coverageFetched.getRangeType().getDataRecord().getFields().get(0).getQuantity().getDefinition());
        assertEquals("urn:ogc:def:dataType:OGC:1.1:unsigned char", coverageFetched.getRangeType().getDataRecord().getFields().get(1).getQuantity().getDefinition());
        assertEquals("urn:ogc:def:dataType:OGC:1.1:unsigned char", coverageFetched.getRangeType().getDataRecord().getFields().get(2).getQuantity().getDefinition());
        assertEquals("red", coverageFetched.getRangeType().getDataRecord().getFields().get(0).getName());
        assertEquals("green", coverageFetched.getRangeType().getDataRecord().getFields().get(1).getName());
        assertEquals("blue", coverageFetched.getRangeType().getDataRecord().getFields().get(2).getName());
        assertEquals("10^0", coverageFetched.getRangeType().getDataRecord().getFields().get(0).getQuantity().getUom().getCode());
        assertEquals("10^0", coverageFetched.getRangeType().getDataRecord().getFields().get(1).getQuantity().getUom().getCode());
        assertEquals("10^0", coverageFetched.getRangeType().getDataRecord().getFields().get(2).getQuantity().getUom().getCode());

        AllowedValue allowedValue = (AllowedValue) coverageFetched.getRangeType().getDataRecord().getFields().get(0).getQuantity().getAllowedValues().get(0);
        assertEquals("0 255", allowedValue);

        AllowedValue allowedValue1 = (AllowedValue) coverageFetched.getRangeType().getDataRecord().getFields().get(1).getQuantity().getAllowedValues().get(0);
        assertEquals("0 255", allowedValue1.getValues());

        AllowedValue allowedValue2 = (AllowedValue) coverageFetched.getRangeType().getDataRecord().getFields().get(2).getQuantity().getAllowedValues().get(0);
        assertEquals(0, allowedValue2.getValues());

        //Test for DomainSet
        domainSet = coverageFetched.getDomainSet();
        assertEquals("456377.56257493998", ((MultiPointDomainSet) domainSet).getDirectMultiPoint().getPositions().get(0));
    }

    private EnvelopeByAxis createEnvelopeByAxis() {
        List<AxisExtent> axisExtentList = new LinkedList<>();

        AxisExtent axisExtent = new AxisExtent();
        axisExtent.setAxisLabel("Lat");
        axisExtent.setUomLabel("deg");
        axisExtent.setLowerBound("456377.5");
        axisExtent.setUpperBound("456377.7");

        AxisExtent axisExtent2 = new AxisExtent();
        axisExtent2.setAxisLabel("Long");
        axisExtent2.setUomLabel("deg");
        axisExtent2.setLowerBound("339866.8");
        axisExtent2.setUpperBound("339867.2");

        AxisExtent axisExtent3 = new AxisExtent();
        axisExtent3.setAxisLabel("h");
        axisExtent3.setUomLabel("deg");
        axisExtent3.setLowerBound("52");
        axisExtent3.setUpperBound("53");

        axisExtentList.add(axisExtent);
        axisExtentList.add(axisExtent2);
        axisExtentList.add(axisExtent3);

        EnvelopeByAxis envelope = new EnvelopeByAxis();
        envelope.setSrsName(ENVELOPE_NAME);
        envelope.setSrsDimension(3);
        envelope.setAxisLabels("Lat Long h");
        envelope.setAxisExtents(axisExtentList);

        return envelope;
    }

    private DomainSet createMultiPointDomainSet() {

        List<String> positions = new LinkedList<>();
        positions.add("456377.56257493998");

        DirectMultiPoint directMultiPoint = new DirectMultiPoint();
        directMultiPoint.setPositions(positions);

        DomainSet domainSet = new MultiPointDomainSet(directMultiPoint);

        return domainSet;
    }

    private RangeType createRangeType() {
        Uom uom = new Uom();
        uom.setCode("10^0");

        List<AllowedValue> allowedValues = new LinkedList<>();
        AllowedValue allowedValue = new AllowedValue();
        allowedValue.setValues("0 255");
        allowedValues.add(allowedValue);

        Quantity quantity = new Quantity();
        quantity.setDescription("");
        quantity.setDefinition("urn:ogc:def:dataType:OGC:1.1:unsigned char");
        quantity.setUom(uom);
        quantity.setAllowedValues(allowedValues);

        Field field1 = new Field();
        field1.setName("red");
        field1.setQuantity(quantity);

        Field field2 = new Field();
        field2.setName("green");
        field2.setQuantity(quantity);

        Field field3 = new Field();
        field3.setName("blue");
        field3.setQuantity(quantity);

        List<Field> fieldList = new LinkedList<>();
        fieldList.add(field1);
        fieldList.add(field2);
        fieldList.add(field3);

        DataRecord dataRecord = new DataRecord(fieldList);

        RangeType rangeType = new RangeType();
        rangeType.setDataRecord(dataRecord);

        return rangeType;
    }

    private RasdamanRangeSet createRangeSet() {
        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
        rasdamanRangeSet.setMddType("MddTypeCoveragePointCloud");
        rasdamanRangeSet.setOid(new Long("1"));
        rasdamanRangeSet.setCollectionName("CollectionNameCoveragePointCloud");
        rasdamanRangeSet.setCollectionType("CollectionTypeCoveragePointCloud");

        return rasdamanRangeSet;
    }

}
