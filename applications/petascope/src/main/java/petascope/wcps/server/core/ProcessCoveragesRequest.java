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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
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
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
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
    private String mime;
    private XmlQuery xmlQuery;

    public ProcessCoveragesRequest(String url, String database, Node node, IDynamicMetadataSource source, Wcps wcps)
            throws WCPSException, SAXException, IOException, PetascopeException, SecoreException {
        super();
        this.source = source;
        this.url = url;
        this.database = database;
        this.wcps = wcps;
        Node child = node.getFirstChild();
        this.rasqlQuery = null;

        if (child.getNodeName().equals(WcpsConstants.MSG_PROCESS_COVERAGE_REQUEST) == false) {
            throw new WCPSException(WcpsConstants.ERRTXT_THE_DOC_UNRECOG_NODE
                    + child.getNodeName());
        }

        child = child.getFirstChild();
        while (child.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            child = child.getNextSibling();
        }

        if (child.getNodeName().equals(WcpsConstants.MSG_QUERY) == false) {
            throw new WCPSException(WcpsConstants.ERRTXT_COULD_NOT_FIND_NODE_QUERY + child.getNodeName());
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
            log.debug(WcpsConstants.DEBUGTXT_FOUND_XML_SYTANX_QUERY);
            this.xmlQuery = new XmlQuery(this.source);
            try {
                xmlQuery.startParsing(queryNode);
            } catch (WCPSException ex) {
                throw ex;
            }
        } else if (queryNode.getNodeName().equals(WcpsConstants.MSG_ABSTRACT_SYNTAX)) {
            String abstractQuery = queryNode.getFirstChild().getNodeValue();
            log.debug(WcpsConstants.DEBUGTXT_FOUND_ABSTRACT_SSYNTAX_QUERY + abstractQuery);
            String xmlString = RasUtil.abstractWCPStoXML(abstractQuery);
            InputSource xmlStringSource = new InputSource(new StringReader(xmlString));
            log.debug(WcpsConstants.DEBUGTXT_CONVERTED_ABSTRACT_SYNTAX_QUERY);
            log.debug(WcpsConstants.MSG_STAR_LINE);
            log.debug(xmlString);
            log.debug(WcpsConstants.MSG_STAR_LINE);
            ProcessCoveragesRequest newRequest = wcps.pcPrepare(url, database, xmlStringSource);
            this.xmlQuery = newRequest.getXmlRequestStructure();
        } else {
            throw new WCPSException(WcpsConstants.ERRTXT_ERROR_UNEXPECTED_NODE + queryNode.getNodeName());
        }

        // If everything went well, we now have a proper value for "xmlQuery"
        this.rasqlQuery = xmlQuery.toRasQL();
        if (isRasqlQuery()) {
            log.debug(WcpsConstants.DEBUGTXT_FINAL_RASQL_QUERY + rasqlQuery);
        } else {
            log.debug(WcpsConstants.DEBUGTXT_FINALMETADATA_RESULT + rasqlQuery);
        }
        this.mime = xmlQuery.getMimeType();
    }
    
    public boolean isRasqlQuery() {
        return rasqlQuery != null && rasqlQuery.trim().startsWith("select");
    }

    public String getMime() {
        return mime;
    }

    private XmlQuery getXmlRequestStructure() {
        return xmlQuery;
    }

    public String getRasqlQuery() {
        return this.rasqlQuery;
    }

    public Object execute() throws WCPSException {
        try {
            return RasUtil.executeRasqlQuery(rasqlQuery);
        } catch (RasdamanException ex) {
            throw new WCPSException(ExceptionCode.ResourceError, WcpsConstants.ERRTXT_COULD_NOT_EVAL_RASDAMAN_Q_P1
                        + getRasqlQuery() + WcpsConstants.ERRTXT_COULD_NOT_EVAL_RASDAMAN_Q_P2 + ex.getMessage(), ex);
        }
    }
}
