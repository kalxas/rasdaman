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
package petascope.wcs2.handlers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.ConfigManager;
import static petascope.ConfigManager.BBOX_IN_COVSUMMARY;
import static petascope.ConfigManager.DESCRIPTION_IN_COVSUMMARY;
import static petascope.ConfigManager.METADATA_IN_COVSUMMARY;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.core.ServiceMetadata;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.ows.Description;
import petascope.ows.ServiceIdentification;
import petascope.ows.ServiceProvider;
import petascope.util.ListUtil;
import petascope.util.Pair;
import static petascope.util.XMLSymbols.*;
import petascope.util.WcsUtil;
import petascope.util.XMLUtil;
import petascope.wcps.metadata.Bbox;
import petascope.wcs2.Wcs2Servlet;
import petascope.wcs2.extensions.Extension;
import petascope.wcs2.extensions.ExtensionsRegistry;
import static petascope.wcs2.extensions.ExtensionsRegistry.INTERPOLATION_IDENTIFIER;
import petascope.wcs2.extensions.FormatExtension;
import petascope.wcs2.extensions.InterpolationExtension;
import petascope.wcs2.parsers.GetCapabilitiesRequest;
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
    public Response handle(GetCapabilitiesRequest request) throws WCSException, SecoreException {
        Document ret = constructDocument(LABEL_CAPABILITIES, NAMESPACE_WCS);


        // Fetch the metadata of the service from petascopedb
        ServiceMetadata sMeta = meta.getServiceMetadata();

        Element root = ret.getRootElement();
        root.addAttribute(new Attribute(ATT_VERSION, sMeta.getIdentification().getTypeVersions().get(0)));

        //
        // Service Identification
        //
        Element serviceIdentification = Templates.getXmlTemplate(Templates.SERVICE_IDENTIFICATION,
                Pair.of("\\{" + Templates.KEY_URL + "\\}", ConfigManager.PETASCOPE_SERVLET_URL != null
                ? ConfigManager.PETASCOPE_SERVLET_URL
                : Wcs2Servlet.LOCAL_SERVLET_ADDRESS)
                );
        if (serviceIdentification != null) {
            // Data from petascopedb tables
            ServiceIdentification sId = sMeta.getIdentification();
            Description sDescr        = sId.getDescription();
            Element fatherEl;
            // insert title(s) on top of template-coded ServiceType and ServiceTypeVersion
            insertOwsTitles(sDescr, serviceIdentification, 1);
            // insert abstract(s)
            insertOwsAbstracts(sDescr, serviceIdentification, serviceIdentification.getChildCount());
            // insert keywords
            insertOwsKeywords(sDescr, serviceIdentification, serviceIdentification.getChildCount());

            // ows:ServiceType and ows:ServiceTypeVersion
            fatherEl = new Element(PREFIX_OWS + ":" + LABEL_SERVICE_TYPE, NAMESPACE_OWS);
            fatherEl.appendChild(sId.getType());
            serviceIdentification.appendChild(fatherEl);
            for (String version : sId.getTypeVersions()) {
                fatherEl = new Element(PREFIX_OWS + ":" + LABEL_SERVICE_TYPE_VERSION, NAMESPACE_OWS);
                fatherEl.appendChild(version);
                serviceIdentification.appendChild(fatherEl);
            }

            // Profiles
            for (String id : ExtensionsRegistry.getExtensionIds()) {
                if (!id.trim().equals("")) {
                    fatherEl = new Element(PREFIX_OWS + ":" + LABEL_PROFILE, NAMESPACE_OWS);
                    fatherEl.appendChild(id);
                    serviceIdentification.appendChild(fatherEl);
                }
            }

            // Fees and constraints
            if (!sId.getFees().isEmpty()) {
                fatherEl = new Element(PREFIX_OWS + ":" + LABEL_FEES, NAMESPACE_OWS);
                fatherEl.appendChild(sId.getFees());
                serviceIdentification.appendChild(fatherEl);
            }
            for (String constraint : sId.getAccessConstraints()) {
                fatherEl = new Element(PREFIX_OWS + ":" + LABEL_ACCESS_CONSTRAINTS, NAMESPACE_OWS);
                fatherEl.appendChild(constraint);
                serviceIdentification.appendChild(fatherEl);
            }

            // Add to capabilities
            root.appendChild(serviceIdentification.copy());
        }

        //
        // Service Provider
        //
        Element serviceProvider = Templates.getXmlTemplate(Templates.SERVICE_PROVIDER,
                Pair.of("\\{" + Templates.KEY_URL + "\\}", ConfigManager.PETASCOPE_SERVLET_URL != null
                ? ConfigManager.PETASCOPE_SERVLET_URL
                : Wcs2Servlet.LOCAL_SERVLET_ADDRESS));
        if (serviceProvider != null) {
            // Data from petascopedb tables
            ServiceProvider sPro = sMeta.getProvider();
            Element c;
            // name: mandatory
            c = new Element(PREFIX_OWS + ":" + LABEL_PROVIDER_NAME, NAMESPACE_OWS);
            c.appendChild(sPro.getName());
            serviceProvider.appendChild(c);
            // optional site
            if (!sPro.getSite().isEmpty()) {
                c = new Element(PREFIX_OWS + ":" + LABEL_PROVIDER_SITE, NAMESPACE_OWS);
                c.addAttribute(new Attribute(PREFIX_XLINK + ":" + ATT_HREF, NAMESPACE_XLINK, sPro.getSite()));
                serviceProvider.appendChild(c);
            }
            // mandatory service contact
            c = new Element(PREFIX_OWS + ":" + LABEL_SERVICE_CONTACT, NAMESPACE_OWS);
            Element childEl;
            // name
            if (!sPro.getContact().getIndividualName().isEmpty()) {
                childEl = new Element(PREFIX_OWS + ":" + LABEL_INDIVIDUAL_NAME, NAMESPACE_OWS);
                childEl.appendChild(sPro.getContact().getIndividualName());
                c.appendChild(childEl);
            }
            // position
            if (!sPro.getContact().getPositionName().isEmpty()) {
                childEl = new Element(PREFIX_OWS + ":" + LABEL_POSITION_NAME, NAMESPACE_OWS);
                childEl.appendChild(sPro.getContact().getPositionName());
                c.appendChild(childEl);
            }
            //
            childEl = new Element(PREFIX_OWS + ":" + LABEL_CONTACT_INFO, NAMESPACE_OWS);
            Element grandChildEl;
            // phone
            if (!sPro.getContact().getContactInfo().getVoicePhones().isEmpty() ||
                !sPro.getContact().getContactInfo().getFacsimilePhones().isEmpty()) {
                grandChildEl = new Element(PREFIX_OWS + ":" + LABEL_PHONE, NAMESPACE_OWS);
                Element greatGrandChildEl;
                // voice [0..*]
                for (String voiceNumber : sPro.getContact().getContactInfo().getVoicePhones()) {
                    greatGrandChildEl = new Element(PREFIX_OWS + ":" + LABEL_VOICE, NAMESPACE_OWS);
                    greatGrandChildEl.appendChild(voiceNumber);
                    grandChildEl.appendChild(greatGrandChildEl);
                }
                // facsimile [0..*]
                for (String facsimileNumber : sPro.getContact().getContactInfo().getFacsimilePhones()) {
                    greatGrandChildEl = new Element(PREFIX_OWS + ":" + LABEL_FACSIMILE, NAMESPACE_OWS);
                    greatGrandChildEl.appendChild(facsimileNumber);
                    grandChildEl.appendChild(greatGrandChildEl);
                }
                //
                childEl.appendChild(grandChildEl);
            }
            // address
            Element address = new Element(PREFIX_OWS + ":" + LABEL_ADDRESS, NAMESPACE_OWS);
            for (Pair<String,String> xmlValue : sPro.getContact().getContactInfo().getAddress().getAddressMetadata()) {
                grandChildEl = new Element(PREFIX_OWS + ":" + xmlValue.fst, NAMESPACE_OWS);
                grandChildEl.appendChild(xmlValue.snd);
                address.appendChild(grandChildEl);
            }
            childEl.appendChild(address);
            // hours of service
            if (!sPro.getContact().getContactInfo().getHoursOfService().isEmpty()) {
                grandChildEl = new Element(PREFIX_OWS + ":" + LABEL_HOURS_OF_SERVICE, NAMESPACE_OWS);
                grandChildEl.appendChild(sPro.getContact().getContactInfo().getHoursOfService());
                childEl.appendChild(grandChildEl);
            }
            // contact instructions
            if (!sPro.getContact().getContactInfo().getInstructions().isEmpty()) {
                grandChildEl = new Element(PREFIX_OWS + ":" + LABEL_CONTACT_INSTRUCTIONS, NAMESPACE_OWS);
                grandChildEl.appendChild(sPro.getContact().getContactInfo().getInstructions());
                childEl.appendChild(grandChildEl);
            }
            // Add ContactInfo to ServiceContact (mandatory)
            c.appendChild(childEl);
            // role
            if (!sPro.getContact().getRole().isEmpty()) {
                childEl = new Element(PREFIX_OWS + ":" + LABEL_ROLE, NAMESPACE_OWS);
                childEl.appendChild(sPro.getContact().getRole());
                c.appendChild(childEl);
            }

            // Add ServiceContact
            serviceProvider.appendChild(c);

            // Add to capabilities
            root.appendChild(serviceProvider.copy());
        }

        //
        // Operations Metadata
        //
        Element operationsMetadata = Templates.getXmlTemplate(Templates.OPERATIONS_METADATA,
                Pair.of("\\{" + Templates.KEY_URL + "\\}", ConfigManager.PETASCOPE_SERVLET_URL != null
                ? ConfigManager.PETASCOPE_SERVLET_URL
                : Wcs2Servlet.LOCAL_SERVLET_ADDRESS));
        if (operationsMetadata != null) {
            root.appendChild(operationsMetadata.copy());
        }

        //
        // ServiceMetadata
        //
        Element serviceMetadata = Templates.getXmlTemplate(Templates.SERVICE_METADATA);
        if (serviceMetadata != null) {
            // SERVICE METADATA EXTENSIONS
            // add supported INTERPOLATION types [OGC 12-049]
            //: requirements #2, #3, #4.
            Element interpolationMetadata = new Element(PREFIX_INT + ":" + LABEL_INTERPOLATION_METADATA, NAMESPACE_INTERPOLATION);
            InterpolationExtension intExtension = (InterpolationExtension)ExtensionsRegistry.getExtension(INTERPOLATION_IDENTIFIER);
            for (String supportedInt : intExtension.getSupportedInterpolation()) {
                Element interpolationSupported = new Element(PREFIX_INT + ":" + LABEL_INTERPOLATION_SUPPORTED, NAMESPACE_INTERPOLATION);
                interpolationSupported.appendChild(supportedInt);
                interpolationMetadata.appendChild(interpolationSupported);
            }
            Element wcsExtension = serviceMetadata.getFirstChildElement(LABEL_EXTENSION, NAMESPACE_WCS);
            // Check if an extension is already defined in the template, otherwise create a new one
            if (null == wcsExtension) {
                // Add the new child element
                wcsExtension = new Element(PREFIX_WCS + ":" + LABEL_EXTENSION, NAMESPACE_WCS);
                wcsExtension.appendChild(interpolationMetadata);
                // Insert it on top, in case the template already contains some other fixed content
                serviceMetadata.insertChild(wcsExtension, 0);
            } else {
                // Just update the child element
                wcsExtension.appendChild(interpolationMetadata);
            }
            //:~

            // add supported FORMATS
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
                // Insert it on top, in case the template already contains some other fixed content
                serviceMetadata.insertChild(formatSupported, 0);
            }
            //:~
            root.appendChild(serviceMetadata.copy());
        }

        //
        // Contents
        //
        Element contents = new Element(LABEL_CONTENTS, NAMESPACE_WCS);
        Iterator<String> it;
        try {
            it = meta.coverages().iterator();
            while (it.hasNext()) {
                Element covSummaryEl = new Element(LABEL_COVERAGE_SUMMARY, NAMESPACE_WCS);
                Element c;
                Element cc;

                // get coverage name
                c = new Element(LABEL_COVERAGE_ID, NAMESPACE_WCS);
                String coverageName = it.next();

                /** Insert coverage description (title/abstract/keywords) **/
                if (DESCRIPTION_IN_COVSUMMARY) { // configuration switch
                    Description covDescr = meta.read(coverageName).getDescription();
                    // insert title(s) on top of template-coded ServiceType and ServiceTypeVersion
                    insertOwsTitles(covDescr, covSummaryEl, covSummaryEl.getChildCount());
                    // insert abstract(s)
                    insertOwsAbstracts(covDescr, covSummaryEl, covSummaryEl.getChildCount());
                    // insert keywords
                    insertOwsKeywords(covDescr, covSummaryEl, covSummaryEl.getChildCount());

                }

                // coverage id and type
                c.appendChild(coverageName);
                covSummaryEl.appendChild(c);
                c = new Element(LABEL_COVERAGE_SUBTYPE, NAMESPACE_WCS);
                String covType = meta.coverageType(meta.coverageID(coverageName));
                c.appendChild(covType);
                covSummaryEl.appendChild(c);
                // Add hierarchy of parent types
                String parentCovType = meta.getParentCoverageType(covType);
                if (!parentCovType.isEmpty()) {
                    covSummaryEl.appendChild(WcsUtil.addSubTypeParents(parentCovType, meta));
                }
                contents.appendChild(covSummaryEl);

                /** Append Native Bbox **/
                if (BBOX_IN_COVSUMMARY) { // configuration switch
                    CoverageMetadata m = meta.read(coverageName);
                    Bbox bbox = m.getBbox();
                    if (null != bbox) {
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
                        covSummaryEl.appendChild(c);
                    }
                }

                // OWS Metadata (if disabled from petascope.properties, no OWS metadata is seen here: see DbMetadataSource.read())
                if (METADATA_IN_COVSUMMARY) { // configuration switch
                    Set<String> owsMetadata = meta.read(coverageName).getExtraMetadata(PREFIX_OWS);
                    for (String metadataValue : owsMetadata) {
                        c = new Element(LABEL_OWSMETADATA, NAMESPACE_OWS);
                        cc = XMLUtil.parseXmlFragment(metadataValue); // contains farther XML child elements: do not escape predefined entities (up to the user)
                        // Check if cc is an ows:Metadata element or if it is an independent fragment to be appended:
                        if (!(NAMESPACE_OWS.equals(cc.getNamespaceURI()) && (PREFIX_OWS + ":" + LABEL_OWSMETADATA).equals(cc.getQualifiedName()))) {
                            c.appendChild(cc);
                        } else {
                            c = cc;
                        }
                        covSummaryEl.appendChild(c);
                    }
                }
            }
        } catch (SecoreException sEx) {
            log.error("SECORE error", sEx);
            throw new SecoreException(sEx.getExceptionCode(), sEx);
        } catch (PetascopeException pEx) {
            log.error("Petascope error", pEx);
            throw new WCSException(pEx.getExceptionCode(), pEx.getExceptionText());
        } catch (IOException ex) {
            throw new WCSException(ExceptionCode.IOConnectionError,
                    "Error building capabilities document", ex);
        }  catch (ParsingException ex) {
            throw new WCSException(ExceptionCode.InvalidCoverageConfiguration,
                    "Error building capabilities document: invalid OWS metadata inserted?", ex);
        }
        root.appendChild(contents);

        try {
            return new Response(null, new String[] { XMLUtil.serialize(ret) }, FormatExtension.MIME_XML);
        } catch (IOException ex) {
            throw new WCSException(ExceptionCode.IOConnectionError,
                    "Error serializing constructed document", ex);
        }
    }

    /**
     * Inserts an XML element containing the titles of an input Description object,
     * at the specified position of the root element.
     * @todo handle xml:lang attribute.
     * @param descr
     * @return The XML fragment of OWS titles.
     */
    private void insertOwsTitles(Description descr, Element root, int position) {
        Element el;
        for (String title : ListUtil.reverse(descr.getTitles())) {
            el = new Element(PREFIX_OWS + ":" + LABEL_TITLE, NAMESPACE_OWS);
            el.appendChild(title);
            root.insertChild(el, position);
        }
    }

    /**
     * Inserts an XML element containing the abstracts of an input Description object,
     * at the specified position of the root element.
     * @todo handle xml:lang attribute.
     * @param descr
     * @return The XML fragment of OWS titles.
     */
    private void insertOwsAbstracts(Description descr, Element root, int position) {
        Element el;
        for (String title : ListUtil.reverse(descr.getAbstracts())) {
            el = new Element(PREFIX_OWS + ":" + LABEL_ABSTRACT, NAMESPACE_OWS);
            el.appendChild(title);
            root.insertChild(el, position);
        }
    }

    /**
     * Inserts an XML element containing the keywords of an input Description object,
     * at the specified position of the root element.
     * @todo handle xml:lang attribute.
     * @param descr
     * @return The XML fragment of OWS titles.
     */
    private void insertOwsKeywords(Description descr, Element root, int position) {
        Element el;
        // Keywords
        for (Description.KeywordsGroup kGroup : ListUtil.reverse(descr.getKeywordGroups())) {
            Element keywords = new Element(PREFIX_OWS + ":" + LABEL_KEYWORDS, NAMESPACE_OWS);
            for (Pair<String,String> key : kGroup.getValues()) {
                // Keyword
                el = new Element(PREFIX_OWS + ":" + LABEL_KEYWORD, NAMESPACE_OWS);
                // Value
                el.appendChild(key.fst);
                if (!key.snd.isEmpty()) {
                    // Add language attribute
                    el.addAttribute(new Attribute(PREFIX_XML + ":" + ATT_LANG, NAMESPACE_XML, key.snd));
                }
                keywords.appendChild(el);
            }
            // Type [+codeSpace]
            if (!kGroup.getType().isEmpty()) {
                el = new Element(PREFIX_OWS + ":" + LABEL_TYPE, NAMESPACE_OWS);
                el.appendChild(kGroup.getType());
                if (!kGroup.getTypeCodeSpace().isEmpty()) {
                    el.addAttribute(new Attribute(ATT_CODESPACE, kGroup.getTypeCodeSpace()));
                }
                keywords.appendChild(el);
            }
            // Add the ows:Keywords element to the service identification
            root.insertChild(keywords, position);
        }
    }
}