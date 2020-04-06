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
package petascope.core.gml.cis.service;

import nu.xom.Document;
import nu.xom.Element;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.core.gml.cis10.GMLCIS10ParserService;
import petascope.core.gml.cis11.GMLCIS11ParserService;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.wcst.exceptions.WCSTUnsupportedCoverageTypeException;

/**
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class GMLCISParserService {

    @Autowired
    private GMLCIS10ParserService gmlCIS10ParserService;
    @Autowired
    private GMLCIS11ParserService gmlCIS11ParserService;

    /**
     * Parse a GML document to a Coverage
     */
    public GeneralGridCoverage parseDocumentToCoverage(Document gmlCoverageDocument) throws PetascopeException, SecoreException {
        Element rootElement = gmlCoverageDocument.getRootElement();
        String inputCoverageType = rootElement.getLocalName();

        GeneralGridCoverage generalGridCoverage = null;

        if (this.gmlCIS10ParserService.canParse(inputCoverageType)) {
            generalGridCoverage = this.gmlCIS10ParserService.parse(gmlCoverageDocument);
        } else if (this.gmlCIS11ParserService.canParse(inputCoverageType)) {
            generalGridCoverage = this.gmlCIS11ParserService.parse(gmlCoverageDocument);
        } else {
            throw new WCSTUnsupportedCoverageTypeException(inputCoverageType);
        }

        return generalGridCoverage;
    }
}
