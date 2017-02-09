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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps2.metadata.service;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.ConfigManager;
import petascope.core.CoverageMetadata;
import petascope.core.CrsDefinition;
import petascope.core.DbMetadataSource;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.*;
import petascope.wcps2.error.managed.processing.CoverageMetadataException;
import petascope.wcps2.error.managed.processing.CoverageMetadataNotInitializedException;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.metadata.model.Axis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin wrapper around the coverage metadataSource functionality to insulate the parser code from future changes in metadataSource
 * classes
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class CoverageRegistry {

    private static final Logger log = LoggerFactory.getLogger(CoverageRegistry.class);

    /**
     * Constructor for the class
     */
    public CoverageRegistry() {
        metadataSource = null;
    }

    public CoverageRegistry(DbMetadataSource metadataSource) {
        this.metadataSource = metadataSource;
        this.wcpsCoverageMetadataTranslator = new WcpsCoverageMetadataTranslator();
    }

    /**
     * Returns a full coverage object based on the coverage name
     *
     * @param coverageName the name of the coverage
     * @return a full coverage object
     * @throws WCPSProcessingError
     */
    public WcpsCoverageMetadata lookupCoverage(String coverageName) throws WCPSProcessingError {
        try {
            initalizeMetadataSource();
            CoverageMetadata metadata = metadataSource.read(coverageName);
            return wcpsCoverageMetadataTranslator.translate(metadata);
        } catch (PetascopeException e) {
            throw new CoverageMetadataException(e);
        } catch (SecoreException e) {
            throw new CoverageMetadataException(e);
        }
    }

    /**
     * Checks if a coverage exists
     *
     * @param coverageName the name of the coverage
     * @return true if it exists, false otherwise
     */
    public boolean coverageExists(String coverageName) {
        initalizeMetadataSource();
        return metadataSource.existsCoverageName(coverageName);
    }

    /**
     * Returns the metadataSource source to offer access to static methods
     *
     * @return
     */
    public DbMetadataSource getMetadataSource() {
        initalizeMetadataSource();
        return metadataSource;
    }

    /**
     * Initializes the internal metadataSource object
     */
    private void initalizeMetadataSource() {
        if (metadataSource == null) {
            try {
                metadataSource = new DbMetadataSource(ConfigManager.METADATA_DRIVER, ConfigManager.METADATA_URL,
                                                      ConfigManager.METADATA_USER, ConfigManager.METADATA_PASS, false);
            } catch (Exception e) {
                e.printStackTrace();
                throw new CoverageMetadataNotInitializedException(e);
            }
        }
    }

    /**
     * Converts the time coefficients to ISO datetime stamp yyyy-MM-dd'T'HH:mm:ssZ (e.g: 2008-01-01T00:00:00Z)
     *
     * @param coeffs   time coefficients from time axis (NOTE: added with the SubsetLow of start date)
     * @param crsDefinition contains information of Time CRS
     * @return string list of time coefficients (days, seconds) to ISO datetime stamp
     * @throws petascope.exceptions.PetascopeException
     */
    public static List<String> toISODate(List<BigDecimal> coeffs, CrsDefinition crsDefinition) throws PetascopeException {
        List<String> isoDates = new ArrayList<String>();

        // Get the UOM in milliseconds (e.g: d is 86 400 000 millis)
        Long milliSeconds = TimeUtil.getMillis(crsDefinition);
        DateTime dateTime = new DateTime(crsDefinition.getDatumOrigin());

        for (BigDecimal coeff: coeffs) {
            // formular: Origin + (Time Coefficients * UOM in milliSeconds)
            long duration = coeff.multiply(new BigDecimal(milliSeconds)).setScale(0, RoundingMode.HALF_UP).longValue();
            DateTime dt = dateTime.plus(duration);

            // Then convert the added date to ISO 8601 datetime (Z means UTC)
            // and we add the qoute to make XML parser easier as it is time value
            isoDates.add("\"" + dt.toString(DateTimeFormat.forPattern(TimeUtil.ISO_8061_FORMAT).withZoneUTC()) + "\"");
        }
        return isoDates;
    }

    /**
     * Extracts the coefficients of an axis.
     * Empty string is returned on regular ones.
     *
     * @param m
     * @param axisName
     * @return The whitespace-separated list of vector coefficients of an axis (empty string if not defined)
     * @throws WCSException
     * @throws petascope.exceptions.SecoreException
     */
    public String getCoefficients(WcpsCoverageMetadata m, String axisName) throws PetascopeException, SecoreException {
        // init
        String coefficients = "";
        List<BigDecimal> coeffs;
        if (!axisName.isEmpty()) {
            
        try {
            coeffs = metadataSource.getAllCoefficients(
                    m.getCoverageName(),
                    m.getAxisByName(axisName).getRasdamanOrder() // i-order of axis
            );
        } catch (PetascopeException ex) {
            log.error("Error while fetching the coefficients of " + m.getAxisByName(axisName).getLabel());
            throw new WCSException(ex.getExceptionCode(), ex);
        }

        // Check given axis is a time axis, then the coeffecients will need to be calculated into timestamp instead of numbers
        Axis axis = m.getAxisByName(axisName);
        if (axis.getAxisType().equals(AxisTypes.T_AXIS)) {
            String timeCrs = axis.getCrsUri();

            // in case of time axis, subset low is a start number from the origin of CRS definition
            // e.g: AnsiDate origin: 1600-12-31T00:00:00Z, start date (irr_cube_2) is: 2018-01-01T00:00:00Z, then subsetlow is: 148654 days.
            // the coefficients for the time axis (irr_cube_2) is 0 (2008-01-01T00:00:00Z), 2 (2008-01-03T00:00:00Z), 4 (2008-01-05T00:00:00Z), 7 (2008-01-08T00:00:00Z)
            coeffs = Vectors.add(coeffs, m.getAxisByName(axisName).getGeoBounds().getLowerLimit());
            CrsDefinition crsDefinition = CrsUtil.getGmlDefinition(timeCrs);
            // if axis is time axis then calculate the coefficients with the origin and uom to timestamp
            coefficients = ListUtil.printList(toISODate(coeffs, crsDefinition), " ");
        } else
            // if axis is not time axis then just get the raw coefficients
            coefficients = ListUtil.printList(coeffs, " ");
        }
        
        return coefficients;
    }

    private WcpsCoverageMetadataTranslator wcpsCoverageMetadataTranslator;
    private DbMetadataSource metadataSource;
}
