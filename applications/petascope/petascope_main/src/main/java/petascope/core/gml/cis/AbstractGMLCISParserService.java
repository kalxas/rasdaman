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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.gml.cis;

import java.util.HashSet;
import java.util.Set;
import nu.xom.Document;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;

/**
 * Abstract class for GMLCIS*ParserService subclasses to parse GML to Coverage model.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public abstract class AbstractGMLCISParserService {
    
    protected Set<String> supportedCoverageTypes = new HashSet<>();
    
    
    /**
     * Check if a GMLCISParserService can parse a coverageType (e.g: GeneralGridCoverage)
     */
    public boolean canParse(String inputCoverageType) {
        return supportedCoverageTypes.contains(inputCoverageType);
    }
    
    /**
     * Parse a GML Document to a Coverage in subclasses
     */
    public abstract GeneralGridCoverage parse(Document gmlCoverageDocument) throws PetascopeException, SecoreException;
}
