<?xml version="1.0" encoding="UTF-8"?>
<wcs:CoverageDescriptions xsi:schemaLocation="http://www.opengis.net/wcs/2.0 http://schemas.opengis.net/wcs/2.0/wcsAll.xsd" xmlns:wcs="http://www.opengis.net/wcs/2.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:crs="http://www.opengis.net/wcs/service-extension/crs/1.0" xmlns:ows="http://www.opengis.net/ows/2.0" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:xlink="http://www.w3.org/1999/xlink">
  <wcs:CoverageDescription gml:id="mr" xmlns="http://www.opengis.net/gml/3.2" xmlns:gmlcov="http://www.opengis.net/gmlcov/1.0" xmlns:swe="http://www.opengis.net/swe/2.0">
    <boundedBy>
      <Envelope srsName="http://localhost:8090/def/crs/OGC/0/Index2D" axisLabels="i j" uomLabels="GridSpacing GridSpacing" srsDimension="2">
        <lowerCorner>0 0</lowerCorner>
        <upperCorner>255 210</upperCorner>
      </Envelope>
    </boundedBy>
    <wcs:CoverageId>mr</wcs:CoverageId>
    <domainSet>
      <RectifiedGrid dimension="2" gml:id="mr-grid">
        <limits>
          <GridEnvelope>
            <low>0 0</low>
            <high>255 210</high>
          </GridEnvelope>
        </limits>
        <axisLabels>i j</axisLabels>
        <origin>
          <Point gml:id="mr-origin" srsName="http://localhost:8090/def/crs/OGC/0/Index2D" axisLabels="i j" uomLabels="GridSpacing GridSpacing" srsDimension="2">
            <pos>0 210</pos>
          </Point>
        </origin>
        <offsetVector srsName="http://localhost:8090/def/crs/OGC/0/Index2D" axisLabels="i j" uomLabels="GridSpacing GridSpacing" srsDimension="2">1 0</offsetVector>
        <offsetVector srsName="http://localhost:8090/def/crs/OGC/0/Index2D" axisLabels="i j" uomLabels="GridSpacing GridSpacing" srsDimension="2">0 -1</offsetVector>
      </RectifiedGrid>
    </domainSet>
    <gmlcov:rangeType>
      <swe:DataRecord>
        <swe:field name="value">
          <swe:Quantity definition="http://www.opengis.net/def/dataType/OGC/0/unsignedByte">
            <swe:label>unsigned char</swe:label>
            <swe:description>primitive</swe:description>
            <swe:uom code="10^0"/>
            <swe:constraint>
              <swe:AllowedValues>
                <swe:interval>0 255</swe:interval>
              </swe:AllowedValues>
            </swe:constraint>
          </swe:Quantity>
        </swe:field>
      </swe:DataRecord>
    </gmlcov:rangeType>
    <wcs:ServiceParameters>
      <wcs:CoverageSubtype>RectifiedGridCoverage</wcs:CoverageSubtype>
      <CoverageSubtypeParent xmlns="http://www.opengis.net/wcs/2.0">
        <CoverageSubtype>AbstractDiscreteCoverage</CoverageSubtype>
        <CoverageSubtypeParent>
          <CoverageSubtype>AbstractCoverage</CoverageSubtype>
        </CoverageSubtypeParent>
      </CoverageSubtypeParent>
      <wcs:nativeFormat>application/octet-stream</wcs:nativeFormat>
    </wcs:ServiceParameters>
  </wcs:CoverageDescription>
</wcs:CoverageDescriptions>
