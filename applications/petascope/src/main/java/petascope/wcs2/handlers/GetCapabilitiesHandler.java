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
package petascope.wcs2.handlers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import petascope.exceptions.PetascopeException;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.ConfigManager;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import petascope.util.CrsUtil;
import petascope.util.Pair;
import petascope.util.WcsUtil;
import static petascope.util.XMLSymbols.*;
import petascope.util.XMLUtil;
import petascope.wcps.server.core.Bbox;
import petascope.wcs2.Wcs2Servlet;
import petascope.wcs2.extensions.ExtensionsRegistry;
import petascope.wcps.server.core.Bbox;
import static petascope.util.XMLSymbols.*;
import petascope.wcs2.extensions.Extension;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.parsers.BaseRequest;
import petascope.wcs2.parsers.GetCapabilitiesRequest;
import petascope.wcs2.parsers.GetCoverageMetadata;
import petascope.wcs2.parsers.GetCoverageRequest;
import petascope.wcs2.templates.Templates;

/**
 * GetCapabilities operation for The Web Coverage Service 2.0
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class GetCapabilitiesHandler extends AbstractRequestHandler<GetCapabilitiesRequest> {

    private static Logger log = LoggerFactory.getLogger(GetCapabilitiesHandler.class);

    public GetCapabilitiesHandler(DbMetadataSource meta) {
        super(meta);
    }

    @Override
    public Response handle(GetCapabilitiesRequest request) throws WCSException {
        Document ret = constructDocument(LABEL_CAPABILITIES, NAMESPACE_WCS);

        Element root = ret.getRootElement();
        root.addAttribute(new Attribute(ATT_VERSION, BaseRequest.VERSION_STRING));

        // Service Identification
        Element serviceIdentification = Templates.getXmlTemplate(Templates.SERVICE_IDENTIFICATION,
                Pair.of("\\{" + Templates.KEY_URL + "\\}", ConfigManager.PETASCOPE_SERVLET_URL != null
                ? ConfigManager.PETASCOPE_SERVLET_URL
                : Wcs2Servlet.LOCAL_SERVLET_ADDRESS));
        if (serviceIdentification != null) {
            for (String id : ExtensionsRegistry.getExtensionIds()) {
                Element profile = new Element(PREFIX_OWS + ":" + LABEL_PROFILE, NAMESPACE_OWS);
                profile.appendChild(id);
                serviceIdentification.appendChild(profile);
            }
            root.appendChild(serviceIdentification.copy());
        }

        // Service Provider
        Element serviceProvider = Templates.getXmlTemplate(Templates.SERVICE_PROVIDER,
                Pair.of("\\{" + Templates.KEY_URL + "\\}", ConfigManager.PETASCOPE_SERVLET_URL != null
                ? ConfigManager.PETASCOPE_SERVLET_URL
                : Wcs2Servlet.LOCAL_SERVLET_ADDRESS));
        if (serviceProvider != null) {
            root.appendChild(serviceProvider.copy());
        }

        // Operations Metadata
        Element operationsMetadata = Templates.getXmlTemplate(Templates.OPERATIONS_METADATA,
                Pair.of("\\{" + Templates.KEY_URL + "\\}", ConfigManager.PETASCOPE_SERVLET_URL != null
                ? ConfigManager.PETASCOPE_SERVLET_URL
                : Wcs2Servlet.LOCAL_SERVLET_ADDRESS));
        if (operationsMetadata != null) {
            root.appendChild(operationsMetadata.copy());
        }

        // ServiceMetadata
        Element serviceMetadata = Templates.getXmlTemplate(Templates.SERVICE_METADATA);
        if (serviceMetadata != null) {
            // add supported formats
            //: [Req6 /req/core/serviceMetadata-structure]
            //: [Req9 /req/core/formats-supported]
            Set<String> mimeTypes = new HashSet<String>();
            for (Extension extension : ExtensionsRegistry.getExtensions()) {
                if (extension instanceof FormatExtension) {
                    mimeTypes.add(((FormatExtension) extension).getMimeType());
                }
            }
            for (String mimeType : mimeTypes) {
                Element formatSupported = new Element(PREFIX_WCS + ":" + LABEL_FORMAT_SUPPORTED, NAMESPACE_WCS);
                formatSupported.appendChild(mimeType);
                serviceMetadata.appendChild(formatSupported);
            }
            //:~
            
            //: CRS [Req9: /req/crs/wcsServiceMetadata-outputCrs]
            Element crsExtension = new Element(PREFIX_WCS + ":" + LABEL_EXTENSION, NAMESPACE_WCS);
            Element crsMetadata  = new Element(PREFIX_CRS + ":" + LABEL_CRS_METADATA, NAMESPACE_CRS);
            Element supportedCrs = new Element(PREFIX_CRS + ":" + ATT_SUPPORTED_CRS, NAMESPACE_CRS);
            supportedCrs.appendChild(CrsUtil.CrsUriDir(CrsUtil.OPENGIS_URI_PREFIX, CrsUtil.EPSG_AUTH));
            crsMetadata.appendChild(supportedCrs);
            crsExtension.appendChild(crsMetadata);
            serviceMetadata.appendChild(crsExtension);
            //:~
            root.appendChild(serviceMetadata.copy());
        }

        // Contents
        Element contents = new Element(LABEL_CONTENTS, NAMESPACE_WCS);
        Iterator<String> it;
        try {
            it = meta.coverages().iterator();
            while (it.hasNext()) {                
                Element cs = new Element(LABEL_COVERAGE_SUMMARY, NAMESPACE_WCS);
                Element c = null;
                Element cc = null; 
                c = new Element(LABEL_COVERAGE_ID, NAMESPACE_WCS);
                String coverageId = it.next();
                GetCoverageRequest tmp = new GetCoverageRequest(coverageId);
                GetCoverageMetadata m  = new GetCoverageMetadata(tmp, meta);
                c.appendChild(coverageId);
                cs.appendChild(c);
                c = new Element(LABEL_COVERAGE_SUBTYPE, NAMESPACE_WCS);
                c.appendChild(meta.coverageType(coverageId));
                cs.appendChild(c);
                contents.appendChild(cs);
                
                /** Append Native Bbox **/
                Bbox bbox = meta.read(coverageId).getBbox();

                c = new Element(LABEL_BBOX, NAMESPACE_OWS);
                // lower-left + upper-right coords
                cc = new Element(ATT_LOWERCORNER, NAMESPACE_OWS);
                cc.appendChild(bbox.getLowerCorner());
                c.appendChild(cc);
                cc = new Element(ATT_UPPERCORNER, NAMESPACE_OWS);
                cc.appendChild(bbox.getUpperCorner());
                c.appendChild(cc);
                
                // dimensions and crs attributes
                Attribute crs = new Attribute(ATT_CRS, bbox.getCrsName());
                Attribute dimensions = new Attribute(ATT_DIMENSIONS, "" + bbox.getDimensionality());
                c.addAttribute(crs);
                c.addAttribute(dimensions);
                cs.appendChild(c);
                
                /** WGS84 Bbox **/
                // Doesn't conform to WCS 2.0.1 so commented out -- DM 2012-oct-19 
                /*if (bbox.hasWgs84Bbox()) {                    
                    c = new Element(LABEL_WGS84_BBOX, NAMESPACE_OWS);
                    // lower-left + upper-right coords
                    cc = new Element(ATT_LOWERCORNER, NAMESPACE_OWS);
                    cc.appendChild(bbox.getWgs84LowerCorner());
                    c.appendChild(cc);
                    cc = new Element(ATT_UPPERCORNER, NAMESPACE_OWS);
                    cc.appendChild(bbox.getWgs84UpperCorner());
                    c.appendChild(cc);
                    // dimensions and crs attributes
                    crs = new Attribute(ATT_CRS, bbox.getWgs84CrsName());
                    dimensions = new Attribute(ATT_DIMENSIONS, "" + bbox.getDimensionality());
                    c.addAttribute(crs);
                    c.addAttribute(dimensions);
                    cs.appendChild(c);
                }*/
            }
        } catch (PetascopeException ex) {
            log.error("Error", ex);
            throw new WCSException(ex.getExceptionCode(), ex.getExceptionText());
        }
        root.appendChild(contents);

        try {
            return new Response(null, XMLUtil.serialize(ret), FormatExtension.MIME_XML);
        } catch (IOException ex) {
            throw new WCSException(ExceptionCode.IOConnectionError,
                    "Error serializing constructed document", ex);
        }
    }
}
