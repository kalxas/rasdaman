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
package petascope.wcps.server.core;

import petascope.exceptions.PetascopeException;
import petascope.exceptions.RasdamanException;
import petascope.exceptions.WCPSException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import petascope.ConfigManager;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.util.ras.RasUtil;
import petascope.core.IDynamicMetadataSource;
import petascope.exceptions.SecoreException;
import petascope.util.WcpsConstants;

/** A WCPS ProcessCoveragesRequest request provides a (just one) rasdaman query, that it executes.
 *
 * Internally, it relies on XmlRequest, which computes the RasQL query.
 *
 */
public class ProcessCoveragesRequest {

    private static Logger log = LoggerFactory.getLogger(ProcessCoveragesRequest.class);

    private String database;
    private IDynamicMetadataSource source;
    private String url;
    private Wcps wcps;
    private String rasqlQuery;
    private String postgisQuery;
    private String mime;
    private XmlQuery xmlQuery;

    public ProcessCoveragesRequest(String url, String database, Node node, IDynamicMetadataSource source, Wcps wcps)
            throws WCPSException, SAXException, IOException, PetascopeException, SecoreException, SQLException {

        super();
        this.source = source;
        this.url = url;
        this.database = database;
        this.wcps = wcps;
        Node child = node.getFirstChild();
        this.rasqlQuery = null;

        if (child.getNodeName().equals(WcpsConstants.MSG_PROCESS_COVERAGE_REQUEST) == false) {
            throw new WCPSException("The document contains an unrecognized node : " + child.getNodeName());
        }

        child = child.getFirstChild();
        while (child.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            child = child.getNextSibling();
        }

        if (child.getNodeName().equals(WcpsConstants.MSG_QUERY) == false) {
            throw new WCPSException("Could not find node <query>: " + child.getNodeName());
        }

        // "child" is now the node <query>.
        Node queryNode = child.getFirstChild();
        while (queryNode.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            queryNode = queryNode.getNextSibling();
        }

        /**
         * The following code is essential. It handles the two cases:
         * 1) the xml contains an <xmlSyntax> request
         * 2) the xml contains an <abstractSyntax> request
         */
        if (queryNode.getNodeName().equals(WcpsConstants.MSG_XML_SYNTAX)) {
            log.debug("Found XML Syntax query.");
            this.xmlQuery = new XmlQuery(this.source);
            try {
                xmlQuery.startParsing(queryNode);
            } catch (WCPSException ex) {
                throw ex;
            }
        } else if (queryNode.getNodeName().equals(WcpsConstants.MSG_ABSTRACT_SYNTAX)) {
            String abstractQuery = queryNode.getFirstChild().getNodeValue();
            log.debug("Found Abstract Syntax query: " + abstractQuery);
            String xmlString = RasUtil.abstractWCPStoXML(abstractQuery);
            InputSource xmlStringSource = new InputSource(new StringReader(xmlString));
            log.debug("Coverted the Abstract syntax query to an XML query:");
            log.debug("***********************************************");
            log.debug(xmlString);
            log.debug("***********************************************");
            ProcessCoveragesRequest newRequest = wcps.pcPrepare(url, database, xmlStringSource);
            this.xmlQuery = newRequest.getXmlRequestStructure();
        } else {
            throw new WCPSException("Error, unexpected node: " + queryNode.getNodeName());
        }

        // If everything went well, we now have a proper value for "xmlQuery"

        String coverage_name = null;
        Iterator<CoverageIterator> it = this.xmlQuery.getCoverageIterator().iterator();
        Iterator<String> coverageNamesIt = it.next().getCoverages();
        //coverage_name = coverageNamesIt.next();
        while( coverageNamesIt.hasNext() ){
            coverage_name = coverageNamesIt.next();
        }



        // store itereator in a var then store the names of coverages and loop throught (while iterator.next())
        // and check types if there is raster and multi threw exception PetascopeException InvalidRequest
        // Can overalay be used to overaly diff coverages types

        // Get coverage subtype using coverage name
        String coverageType = source.read(coverage_name).getCoverageType();

        if(coverageType.equals(WcpsConstants.MSG_MULTIPOINT_COVERAGE)){
            this.postgisQuery = xmlQuery.toPostGISQuery();
        }else{
            this.rasqlQuery = this.xmlQuery.toRasQL();
        }

        //this.rasqlQuery = xmlQuery.toRasQL();

        if (isRasqlQuery()) {
            log.debug("Final RasQL query: " + rasqlQuery);
        } else {
            log.debug("Final metadata result: " + rasqlQuery);
        }
        this.mime = xmlQuery.getMimeType();
    }

    public boolean isRasqlQuery() {
        return rasqlQuery != null && rasqlQuery.trim().startsWith("select");
    }

    public boolean isPostGISQuery() {
        return postgisQuery != null && (postgisQuery.contains("BOX3D") || postgisQuery.contains("ST_MakeEnvelope")
                || postgisQuery.contains("St_X")
                || postgisQuery.contains("St_Y")
                || postgisQuery.contains("St_Z"));
    }

    public String getMime() {
        return mime;
    }

    private XmlQuery getXmlRequestStructure() {
        return xmlQuery;
    }

    public String getPostGISQuery() {
        return this.postgisQuery;
    }

    public String getRasqlQuery() {
        return this.rasqlQuery;
    }

    public Object execute() throws WCPSException, PetascopeException, SecoreException, SQLException {

        try {
            if(isPostGISQuery()){
                DbMetadataSource meta = new DbMetadataSource(ConfigManager.METADATA_DRIVER,
                    ConfigManager.METADATA_URL,
                    ConfigManager.METADATA_USER,
                    ConfigManager.METADATA_PASS, false);
                return meta.executePostGISQuery(postgisQuery);

            } else if (isRasqlQuery()){
                return RasUtil.executeRasqlQuery(rasqlQuery);
            }
        } catch (RasdamanException ex) {
            throw new WCPSException(ExceptionCode.ResourceError, "Could not evaluate rasdaman query: '"
                        + getRasqlQuery() + "'\n Cause: " + ex.getMessage(), ex);
        }
        return null;
    }
}
