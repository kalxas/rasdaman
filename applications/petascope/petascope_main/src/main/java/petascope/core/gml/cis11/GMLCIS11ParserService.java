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
package petascope.core.gml.cis11;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.rasdaman.domain.cis.GeneralGridCoverage;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.core.XMLSymbols.ATT_AXIS_LABEL;
import static petascope.core.XMLSymbols.ATT_ID;
import static petascope.core.XMLSymbols.ATT_RESOLUTION;
import static petascope.core.XMLSymbols.ATT_SRS_NAME;
import static petascope.core.XMLSymbols.LABEL_AXIS_EXTENT;
import static petascope.core.XMLSymbols.LABEL_ENVELOPE;
import static petascope.core.XMLSymbols.LABEL_GENERAL_GRID_COVERAGE;
import static petascope.core.XMLSymbols.LABEL_METADATA_CIS11;
import static petascope.core.XMLSymbols.NAMESPACE_CIS_11;
import static petascope.core.XMLSymbols.NAMESPACE_GML;
import petascope.core.gml.cis.AbstractGMLCISParserService;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSTException;
import petascope.util.XMLUtil;
import petascope.wcst.exceptions.WCSTRequiredOneElement;

/**
 * Utilities for parsing parts of a coverage, from GML format of CRS version 1.1
 *
 *  @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class GMLCIS11ParserService extends AbstractGMLCISParserService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GMLCIS11ParserService.class);
    
    public GMLCIS11ParserService() {
        this.supportedCoverageTypes.add(LABEL_GENERAL_GRID_COVERAGE);
    }

    /**
     * Parse a GML Document in CIS 1.1 to a Coverage
     */
    @Override
    public GeneralGridCoverage parse(Document gmlCoverageDocument) throws PetascopeException, SecoreException {
        Element rootElement = gmlCoverageDocument.getRootElement();
        
        GeneralGridCoverage generalGridCoverage = null;
        
        return generalGridCoverage;
    }
    
    /**
     * Parses gml:Metadata element.
    */
    private String parseExtraMetadata(Element root) {
        String ret = "";
        Elements metadata = root.getChildElements(LABEL_METADATA_CIS11, NAMESPACE_CIS_11);
        if (metadata.size() > 0 && metadata.get(0).getChildCount() > 0) {
            // since the node can contain xml sometimes, json other times, we need to return
            // the actual content of the node as string.

            // the string representation of the node
            for (int i = 0; i < metadata.get(0).getChildCount(); i++) {
                ret += metadata.get(0).getChild(i).toXML();
            }
        }
        return ret.trim();
    }

}
