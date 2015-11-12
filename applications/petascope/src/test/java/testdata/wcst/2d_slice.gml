<?xml version="1.0" encoding="UTF-8"?>
<gmlcov:RectifiedGridCoverage
        xmlns='http://www.opengis.net/gml/3.2'
        xmlns:gml='http://www.opengis.net/gml/3.2'
        xmlns:gmlcov='http://www.opengis.net/gmlcov/1.0'
        xmlns:swe='http://www.opengis.net/swe/2.0'
        xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
        xmlns:gmlrgrid='http://www.opengis.net/gml/3.3/rgrid'
        gml:id="AverageChloroColorScaled3"
        >


<boundedBy>
    <Envelope srsName="http://kahlua.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.jacobs-university.de:8080/def/crs/EPSG/0/4326&amp;2=http://localhost:8080/def/crs/OGC/0/AnsiDate" axisLabels="Lat Long" uomLabels=" " srsDimension="2">
        <lowerCorner>90.0 -180.0</lowerCorner>
        <upperCorner>89.6 -179.6</upperCorner>
    </Envelope>
</boundedBy>


<domainSet>
    <gml:RectifiedGrid dimension="2" gml:id="grid">
        <limits>
            <GridEnvelope>
                <low>0 0</low>
                <high>0 0</high>
            </GridEnvelope>
        </limits>
        <axisLabels>Long Lat</axisLabels>
        <gml:origin>
            <Point gml:id="origin" srsName="http://kahlua.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.jacobs-university.de:8080/def/crs/EPSG/0/4326&amp;2=http://localhost:8080/def/crs/OGC/0/AnsiDate"
                   axisLabels="Lat Long" uomLabels=" " srsDimension="2">
                <pos>89.8 -179.8</pos>
            </Point>
        </gml:origin>

        <gml:offsetVector srsName="http://kahlua.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.jacobs-university.de:8080/def/crs/EPSG/0/4326&amp;2=http://localhost:8080/def/crs/OGC/0/AnsiDate" axisLabels="Lat Long" uomLabels=" " srsDimension="2">
            0 0.4
        </gml:offsetVector>
         <gml:offsetVector srsName="http://kahlua.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.jacobs-university.de:8080/def/crs/EPSG/0/4326&amp;2=http://localhost:8080/def/crs/OGC/0/AnsiDate" axisLabels="Lat Long" uomLabels=" " srsDimension="2">
            -0.4 0
        </gml:offsetVector>


    </gml:RectifiedGrid>
</domainSet>


<gml:rangeSet>
    <gml:DataBlock>
        <gml:rangeParameters/>
        <gml:tupleList>
            0,0,0
        </gml:tupleList>
    </gml:DataBlock>
</gml:rangeSet>


<gmlcov:rangeType>
    <swe:DataRecord>
        <swe:field name="Red">
            <swe:Quantity definition="">
                <swe:description></swe:description>
                <swe:nilValues>
                    <swe:NilValues>
                    <swe:nilValue reason="">

                    </swe:nilValue>
                    </swe:NilValues>
                </swe:nilValues>
                <swe:uom code="10^0"/>
            </swe:Quantity>
        </swe:field>
         <swe:field name="Green">
            <swe:Quantity definition="">
                <swe:description></swe:description>
                <swe:nilValues>
                    <swe:NilValues>
                    <swe:nilValue reason="">

                    </swe:nilValue>
                    </swe:NilValues>
                </swe:nilValues>
                <swe:uom code="10^0"/>
            </swe:Quantity>
        </swe:field>
         <swe:field name="Blue">
            <swe:Quantity definition="">
                <swe:description></swe:description>
                <swe:nilValues>
                    <swe:NilValues>
                    <swe:nilValue reason="">

                    </swe:nilValue>
                    </swe:NilValues>
                </swe:nilValues>
                <swe:uom code="10^0"/>
            </swe:Quantity>
        </swe:field>

    </swe:DataRecord>
</gmlcov:rangeType>


</gmlcov:RectifiedGridCoverage>
