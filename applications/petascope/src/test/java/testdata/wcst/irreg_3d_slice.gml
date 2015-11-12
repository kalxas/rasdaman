<?xml version="1.0" encoding="UTF-8"?>
<gmlcov:ReferenceableGridCoverage
        xmlns='http://www.opengis.net/gml/3.2'
        xmlns:gml='http://www.opengis.net/gml/3.2'
        xmlns:gmlcov='http://www.opengis.net/gmlcov/1.0'
        xmlns:swe='http://www.opengis.net/swe/2.0'
        xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
        xmlns:gmlrgrid='http://www.opengis.net/gml/3.3/rgrid'
        gml:id="AverageChloroColorScaled3"
        >


<boundedBy>
    <Envelope srsName="http://kahlua.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.jacobs-university.de:8080/def/crs/EPSG/0/4326&amp;2=http://localhost:8080/def/crs/OGC/0/AnsiDate" axisLabels="Lat Long ansi" uomLabels="  " srsDimension="3">
        <lowerCorner>90.0 -180.0 0</lowerCorner>
        <upperCorner>89.6 -179.6 0</upperCorner>
    </Envelope>
</boundedBy>


<domainSet>
    <gml:ReferenceableGridByVectors dimension="3" gml:id="grid">
        <limits>
            <GridEnvelope>
                <low>0 0 0</low>
                <high>0 0 0</high>
            </GridEnvelope>
        </limits>
        <axisLabels>Long Lat ansi</axisLabels>
        <gml:origin>
            <Point gml:id="origin" srsName="http://kahlua.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.jacobs-university.de:8080/def/crs/EPSG/0/4326&amp;2=http://localhost:8080/def/crs/OGC/0/AnsiDate"
                   axisLabels="Lat Long ansi" uomLabels="  " srsDimension="3">
                <pos>89.8 -179.8 "2002-10-01T00:00:00+00:00"</pos>
            </Point>
        </gml:origin>

        <gmlrgrid:generalGridAxis>
            <gmlrgrid:GeneralGridAxis>
                <gmlrgrid:offsetVector srsName="http://kahlua.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.jacobs-university.de:8080/def/crs/EPSG/0/4326&amp;2=http://localhost:8080/def/crs/OGC/0/AnsiDate" axisLabels="Lat Long ansi" uomLabels="  " srsDimension="3">
                    0 0.4 0
                </gmlrgrid:offsetVector>
                <gmlrgrid:coefficients></gmlrgrid:coefficients>
                <gmlrgrid:gridAxesSpanned>None</gmlrgrid:gridAxesSpanned>
                <gmlrgrid:sequenceRule axisOrder="+1">None</gmlrgrid:sequenceRule>
            </gmlrgrid:GeneralGridAxis>
        </gmlrgrid:generalGridAxis>
         <gmlrgrid:generalGridAxis>
            <gmlrgrid:GeneralGridAxis>
                <gmlrgrid:offsetVector srsName="http://kahlua.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.jacobs-university.de:8080/def/crs/EPSG/0/4326&amp;2=http://localhost:8080/def/crs/OGC/0/AnsiDate" axisLabels="Lat Long ansi" uomLabels="  " srsDimension="3">
                    -0.4 0 0
                </gmlrgrid:offsetVector>
                <gmlrgrid:coefficients></gmlrgrid:coefficients>
                <gmlrgrid:gridAxesSpanned>None</gmlrgrid:gridAxesSpanned>
                <gmlrgrid:sequenceRule axisOrder="+1">None</gmlrgrid:sequenceRule>
            </gmlrgrid:GeneralGridAxis>
        </gmlrgrid:generalGridAxis>
         <gmlrgrid:generalGridAxis>
            <gmlrgrid:GeneralGridAxis>
                <gmlrgrid:offsetVector srsName="http://kahlua.jacobs-university.de:8080/def/crs-compound?1=http://kahlua.jacobs-university.de:8080/def/crs/EPSG/0/4326&amp;2=http://localhost:8080/def/crs/OGC/0/AnsiDate" axisLabels="Lat Long ansi" uomLabels="  " srsDimension="3">
                    0 0 1
                </gmlrgrid:offsetVector>
                <gmlrgrid:coefficients>0</gmlrgrid:coefficients>
                <gmlrgrid:gridAxesSpanned>None</gmlrgrid:gridAxesSpanned>
                <gmlrgrid:sequenceRule axisOrder="+1">None</gmlrgrid:sequenceRule>
            </gmlrgrid:GeneralGridAxis>
        </gmlrgrid:generalGridAxis>


    </gml:ReferenceableGridByVectors>
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


</gmlcov:ReferenceableGridCoverage>
