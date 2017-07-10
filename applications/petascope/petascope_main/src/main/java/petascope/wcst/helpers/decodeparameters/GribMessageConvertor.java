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
package petascope.wcst.helpers.decodeparameters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rasdaman.domain.cis.Coverage;
import petascope.wcps2.metadata.model.ParsedSubset;
import petascope.core.service.CrsComputerService;
import petascope.wcst.helpers.decodeparameters.model.RasdamanDecodeParams;
import petascope.wcst.helpers.decodeparameters.model.RasdamanGribInternalStructure;
import petascope.wcst.helpers.decodeparameters.model.RasdamanGribMessage;
import petascope.wcst.helpers.grib.model.GribAxis;
import petascope.wcst.helpers.grib.model.GribMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.rasdaman.domain.cis.GeneralGridCoverage;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;

/**
 * Class for representing a list of grib messages associated with a grib
 * coverage. The information is needed for forwarding it to rasdaman, after it
 * is translated into Cartesian subsets.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class GribMessageConvertor implements RangeParametersConvertor {

    private String messages;
    private Coverage coverage;

    public GribMessageConvertor(String messages, Coverage coverage) {
        this.messages = messages;
        this.coverage = coverage;
    }

    @Override
    public String toRasdamanDecodeParameters() throws IOException, WCSException, PetascopeException, SecoreException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Deserialize from JSON string in rangeParameters GML into list of grib message
        List<GribMessage> gribMessages = objectMapper.readValue(this.messages, new TypeReference<List<GribMessage>>() {
        });
        RasdamanGribInternalStructure rasdamanGribInternalStructure = new RasdamanGribInternalStructure();

        List<RasdamanGribMessage> rasdamanGribMessages = new ArrayList<RasdamanGribMessage>();
        for (GribMessage gribMessage : gribMessages) {
            // Translate the geo (lower, upper) bounds to grid (lower, upper) bounds for each message in all axes
            RasdamanGribMessage rasdamanGribMessage = this.convertToRasdamanMessage(gribMessage);
            rasdamanGribMessages.add(rasdamanGribMessage);
        }
        translateGribMessageDomainsToOrigin(rasdamanGribMessages);

        rasdamanGribInternalStructure.addAll(rasdamanGribMessages);
        RasdamanDecodeParams rasdamanDecodeParams = new RasdamanDecodeParams();
        rasdamanDecodeParams.setInternalStructure(rasdamanGribInternalStructure);

        // Serialize the grib params object to JSON string as part of rasql update query
        return objectMapper.writeValueAsString(rasdamanDecodeParams);
    }

    /**
     * Convert the Grib message for all axes from geo (lower, upper) bounds to
     * grid (lower, upper) bounds for example: message of coverage which has 4
     * axes isobaric: ("min": "887.5", "max": "912.5"), ansi: ("min":
     * "2006-11-28T06:00:00"), Lat: ("min": "-90.25", "max": "90.25"), Long:
     * ("min": "-0.25", "max": "359.75")
     *
     * will be translated to rasdaman message (grid intervals)
     * {\"msgId\":5,\"domain\":\"[0:0,0:0,0:719,0:360]\"}
     *
     * @param gribMessage
     * @return
     * @throws WCSException
     */
    private RasdamanGribMessage convertToRasdamanMessage(GribMessage gribMessage) throws WCSException, PetascopeException, SecoreException {
        // Translated domain intervals for each axis (e.g: 0 -> [0:0], .. 3 -> [0:360])
        Map<Integer, ParsedSubset<Long>> gridIntervalsMap = new TreeMap<Integer, ParsedSubset<Long>>();

        // Iterate all the axes from the list of grib messages and calculate the min, max in grid integer values
        for (GribAxis gribAxis : gribMessage.getAxes()) {
            String axisName = gribAxis.getName();
            // Only support GeneralGridCoverage now      
            String crs = ((GeneralGridCoverage) this.coverage).getGeoAxisByName(axisName).getSrsName();
            ParsedSubset<String> subset = new ParsedSubset<>(gribAxis.getMin(), gribAxis.getMax());

            // If axis does not have max value, considere max = min (e.g: TimeAxis which time value is fetched from file name)            
            if (gribAxis.getMax() == null) {
                subset = new ParsedSubset<>(gribAxis.getMin(), gribAxis.getMin());
            }

            CrsComputerService crsComputer = new CrsComputerService(gribAxis.getName(), crs, subset, this.coverage);
            // Calculate the min, max in grid integer values 
            ParsedSubset<Long> pixelIndices = crsComputer.getPixelIndices();
            // Get the grid domain index by axis name
            Integer domainIndex = ((GeneralGridCoverage) this.coverage).getIndexAxisByName(axisName).getAxisOrder();
            gridIntervalsMap.put(domainIndex, pixelIndices);
        }

        return new RasdamanGribMessage(gribMessage.getMessageId(), gridIntervalsMap);
    }

    /**
     * Find the minimum (origin) of each dimension (axis) in each message of the
     * importing coverage in Grib, normally they are 0 for each dimension. That
     * means the interval for each axis is the same.
     *
     * @param gribMessages
     */
    private void translateGribMessageDomainsToOrigin(List<RasdamanGribMessage> gribMessages) {
        Map<Integer, Long> dimensionMinimums = new TreeMap<Integer, Long>();

        // As grib contains multiple messages and each message contains different interval values, so need to find
        // the minimum on each dimension (axis) of all messages, normally they are 0        
        for (RasdamanGribMessage gribMessage : gribMessages) {
            Map<Integer, ParsedSubset<Long>> pixelIndices = gribMessage.getPixelIndices();
            for (Integer dimension : pixelIndices.keySet()) {
                Long lowerLimit = pixelIndices.get(dimension).getLowerLimit();
                // Find the lowest value for this dimension (origin)
                if (!dimensionMinimums.containsKey(dimension) || dimensionMinimums.get(dimension) > lowerLimit) {
                    dimensionMinimums.put(dimension, lowerLimit);
                }
            }
        }

        // translate by the minimum (origin)        
        for (RasdamanGribMessage gribMessage : gribMessages) {
            Map<Integer, ParsedSubset<Long>> pixelIndices = gribMessage.getPixelIndices();
            // Origin normally is 0, so the values of grid intervals in each message are the same
            for (Integer dimension : pixelIndices.keySet()) {
                // (lowerGrid, upperGrid) - origin
                Long origin = dimensionMinimums.get(dimension);
                Long lowerLimit = pixelIndices.get(dimension).getLowerLimit() - origin;
                Long upperLimit = pixelIndices.get(dimension).getUpperLimit() - origin;

                pixelIndices.put(dimension, new ParsedSubset<Long>(lowerLimit, upperLimit));
            }
        }
    }

}
