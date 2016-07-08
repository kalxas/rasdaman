/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.handlers.wcst.helpers.decodeparameters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.wcps.metadata.CoverageInfo;
import petascope.wcps2.metadata.legacy.Coverage;
import petascope.wcps2.metadata.legacy.CoverageRegistry;
import petascope.wcps2.metadata.model.ParsedSubset;
import petascope.wcps2.util.CrsComputer;
import petascope.wcs2.handlers.wcst.helpers.decodeparameters.model.RasdamanDecodeParams;
import petascope.wcs2.handlers.wcst.helpers.decodeparameters.model.RasdamanGribInternalStructure;
import petascope.wcs2.handlers.wcst.helpers.decodeparameters.model.RasdamanGribMessage;
import petascope.wcs2.handlers.wcst.helpers.grib.model.GribAxis;
import petascope.wcs2.handlers.wcst.helpers.grib.model.GribMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Class for representing a list of grib messages associated with a grib coverage.
 * The information is needed for forwarding it to rasdaman, after it is translated into Cartesian subsets.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class GribMessageConvertor implements RangeParametersConvertor {

    private String messages;

    private CoverageMetadata coverage;

    private DbMetadataSource meta;


    public GribMessageConvertor(String messages, CoverageMetadata coverage, DbMetadataSource meta) {
        this.messages = messages;
        this.coverage = coverage;
        this.meta = meta;
    }

    public String toRasdamanDecodeParameters() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<GribMessage> gribMessages = objectMapper.readValue(this.messages, new TypeReference<List<GribMessage>>(){});
        RasdamanGribInternalStructure rasdamanGribInternalStructure = new RasdamanGribInternalStructure();

        for(GribMessage gribMessage: gribMessages){
            rasdamanGribInternalStructure.add(convertToRasdamanMessage(gribMessage));
        }
        RasdamanDecodeParams rasdamanDecodeParams = new RasdamanDecodeParams();
        rasdamanDecodeParams.setInternalStructure(rasdamanGribInternalStructure);
        return objectMapper.writeValueAsString(rasdamanDecodeParams);
    }

    private RasdamanGribMessage convertToRasdamanMessage(GribMessage gribMessage){
        Map<Integer, ParsedSubset<Long>> result = new TreeMap<Integer, ParsedSubset<Long>>();
        CoverageInfo coverageInfo = new CoverageInfo(this.coverage);
        Coverage currentWcpsCoverage = new Coverage(this.coverage.getCoverageName(), coverageInfo, this.coverage);
        CoverageRegistry coverageRegistry = new CoverageRegistry(meta);
        for(GribAxis gribAxis: gribMessage.getAxes()){
            String crs = this.coverage.getDomainByName(gribAxis.getName()).getNativeCrs();
            ParsedSubset<String> subset = new ParsedSubset<String>(gribAxis.getMin(), gribAxis.getMax());
            
            if(gribAxis.getMax() == null){
                subset = new ParsedSubset<String>(gribAxis.getMin(), gribAxis.getMin());
            }
            CrsComputer crsComputer = new CrsComputer(gribAxis.getName(), crs, subset, currentWcpsCoverage, coverageRegistry);
            ParsedSubset<Long> pixelIndices = crsComputer.getPixelIndices(true);
            result.put(currentWcpsCoverage.getCoverageMetadata().getDomainIndexByName(gribAxis.getName()), pixelIndices);
        }
        return new RasdamanGribMessage(gribMessage.getMessageId(), getAffectedDomain(result));
    }

    private String getAffectedDomain(Map<Integer, ParsedSubset<Long>> pixelIndices){
        String domain = "[";
        for(int i = 0; i < pixelIndices.size(); i++){
            domain += String.valueOf(pixelIndices.get(i).getLowerLimit()) + ":" + String.valueOf(pixelIndices.get(i).getUpperLimit());
            //if not last, add a comma
            if(i < pixelIndices.keySet().size() - 1){
                domain += ",";
            }
        }
        domain += "]";
        return domain;
    }
}
