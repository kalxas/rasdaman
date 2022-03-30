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
 /*
 * JOMDoc - A Java library for OMDoc documents (http://omdoc.org/jomdoc).
 *
 * Original author    Dimitar Misev <d.misev@jacobs-university.de>
 * Web                http://kwarc.info/dmisev/
 * Created            Apr 4, 2008, 5:18:39 PM
 *
 * Filename           $Id: XMLUtil.java 1976 2010-07-31 12:07:20Z dmisev $
 * Revision           $Revision: 1976 $
 * Last modified on   $Date: 2010-07-31 14:07:20 +0200 (Sat, 31 Jul 2010) $
 *               by   $Author: dmisev $
 *
 * Copyright (C) 2007,2008 the KWARC group (http://kwarc.info)
 * Licensed under the GNU  Public License v3 (GPL3).
 * For other licensing contact Michael Kohlhase <m.kohlhase@jacobs-university.de>
 */
package petascope.util;

import petascope.core.KVPSymbols;
import petascope.core.XMLSymbols;
import com.eaio.uuid.UUID;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Comment;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParentNode;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.Text;
import nu.xom.XPathContext;
import nu.xom.converters.DOMConverter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.rasdaman.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import static petascope.core.XMLSymbols.ATT_SCHEMA_LOCATION;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.WCSException;
import static petascope.core.XMLSymbols.ATT_SERVICE;
import static petascope.core.XMLSymbols.ATT_VERSION;
import static petascope.core.XMLSymbols.NAMESPACE_XSI;
import static petascope.core.XMLSymbols.PREDEFINED_XML_DECLARATION_BEGIN;
import static petascope.core.XMLSymbols.PREDEFINED_XML_DECLARATION_END;
import static petascope.core.XMLSymbols.PREFIX_XMLNS;
import static petascope.core.XMLSymbols.PREFIX_XSI;

/**
 * Common utility methods for working with XML.
 *
 * @author <a href="mailto:d.misev@jacobs-university.de">Dimitar Misev</a>
 */
public class XMLUtil {

    private static Logger log = LoggerFactory.getLogger(XMLUtil.class);
    private static final XmlMapper xmlMapper = new XmlMapper();
    
    private static final String XML_DECLERATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /**
     * Given a XML text, e.g: <a><b>123</b></a> and one element (open and close tags, e.g: <b>  123   </b>)
     * return <a><b><![CDATA[  123  ]]></b></a>
     * @param xml
     * @param openElement
     * @param closeElement
     * @return 
     */
    public static String addCdataInsideElement(String xml, String openElement, String closeElement) {
        int openElementIndex = xml.indexOf(openElement);
        int closeElementIndex = xml.indexOf(closeElement);
        
        String before = xml.substring(0, openElementIndex + openElement.length());
        String after = xml.substring(closeElementIndex, xml.length());
        
        // the 123 value which need to enquoted by cdata
        String middle = xml.substring(openElementIndex + openElement.length(), closeElementIndex);
        middle = XMLUtil.enquoteCDATA(middle);
        
        String result = before + middle + after;
        
        return result;
    }

    static class MyBuilder extends ThreadLocal<Builder> {

        boolean validating;

        public MyBuilder(boolean validating) {
            this.validating = validating;
        }

        @Override
        protected Builder initialValue() {
            factory.setValidating(validating);
            return newBuilder(!validating);
        }
    }
    /**
     * Separator, for debugging
     */
    public static String FSEP = "\n-------------------------------------\n";
    public static String HSEP = "\n-------------------\n";
    /**
     * Setup for building documents
     */
    private static SchemaFactory schemaFactory;
    private static SAXParserFactory factory;
    private static MyBuilder builder;
    private static File wcsSchema;
    public static final String XML_STD_ENCODING = "UTF-8";
    public static final String WCS_SCHEMA = "xml/ogc/wcs/2.0.0/wcsAll.xsd";
    private static final String SET_FEATURE_XXE_FALSE = "http://xml.org/sax/features/external-general-entities";

    public static final String WCS_SCHEMA_URL = "http://schemas.opengis.net/wcs/2.0/wcsAll.xsd";

    static {
        init();
    }

    public static void init() {
        if (factory != null) {
            return;
        }
        System.setProperty("javax.xml.parsers.SAXParserFactory",
                "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        factory = SAXParserFactory.newInstance();

        try {
            factory.setFeature(SET_FEATURE_XXE_FALSE, false);
        } catch (ParserConfigurationException ex) {
            //If feature does not exist, then no XXE support anyway so there is nothing to do
            log.warn("Set feature XXE False: " + ex.getMessage());
            log.debug(ExceptionUtils.getStackTrace(ex));
        } catch (SAXNotRecognizedException ex) {
            //If feature does not exist then,no XXE support anyway so there is nothing to do
            log.warn("Set feature XXE False: " + ex.getMessage());
            log.debug(ExceptionUtils.getStackTrace(ex));
        } catch (SAXNotSupportedException ex) {
            //If feature does not exist,then no XXE support anyway so there is nothing to do
            log.warn("Set feature XXE False: " + ex.getMessage());
            log.debug(ExceptionUtils.getStackTrace(ex));
        }

        factory.setNamespaceAware(true);
        factory.setValidating(true);
        builder = new MyBuilder(false);
        builder.get();
    }

    private static Builder newBuilder(boolean ignoreDTD) {
        XMLReader xmlReader = null;
        try {
            xmlReader = factory.newSAXParser().getXMLReader();
            if (ignoreDTD) {
                xmlReader.setEntityResolver(new EntityResolver() {

                    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                        return new InputSource(new StringReader(""));
                    }
                });
                xmlReader.setErrorHandler(new ErrorHandler() {

                    @Override
                    public void warning(SAXParseException saxpe) throws SAXException {
                        log.warn("XML parser warning: ", saxpe.getMessage());
                    }

                    @Override
                    public void error(SAXParseException saxpe) throws SAXException {
                        throw saxpe;
                    }

                    @Override
                    public void fatalError(SAXParseException saxpe) throws SAXException {
                        throw saxpe;
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new Builder(xmlReader);
    }

    /**
     * Build XOM Document from XML file.
     *
     * @param file input XML file
     * @return XOM Document
     * @throws IOException
     * @throws ParsingException
     * @throws petascope.exceptions.PetascopeException
     */
    public static Document buildDocument(File file) throws IOException, ParsingException, PetascopeException {
        return buildDocument(file.toURI().toString(), new FileInputStream(file));
    }

    /**
     * Build XOM Document from an XML string.
     *
     * @param baseURI
     * @param document input XML string
     * @return XOM Document
     */
    public static Document buildDocument(String baseURI, String document) throws PetascopeException {
        try {
            InputStream in = new ByteArrayInputStream(document.getBytes(XML_STD_ENCODING));
            return buildDocument(baseURI, in);
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError, "Cannot build XOM document from XML string: " + document + ". Reason: " + ex.getMessage());
        }
    }

    /**
     * Creates a {@link Document} given a input stream.
     * <p>
     * <i>Note</i>: If the input stream to parse contains a <code>DOCTYPE</code>
     * definition, but the parser can't find the referenced <code>DTD</code>,
     * then a <code>FileNotFound</code> exception is raised discarding the
     * parsing process.
     *
     * @param baseURI
     * @param in an input stream
     * @return the document
     */
    public static Document buildDocument(String baseURI, InputStream in) throws PetascopeException {
        Document doc = null;

        try {
            doc = builder.get().build(in, baseURI);
        } catch (ParsingException ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Error while building XML document '" + baseURI + "'. error '" + ex.getMessage() + "', line '" + ex.getLineNumber() + "', column '" + ex.getColumnNumber() + "'.");
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError, 
                    "Error while building XML document: " + baseURI + ". Error reading from the input stream. Reason: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError, "Error while building XML document: " + baseURI + ". Reason: " + ex.getMessage(), ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                throw new PetascopeException(ExceptionCode.RuntimeError, "Error while closing inputstream after building XML document. Reason: " + ex.getMessage(), ex);
            }
        }

        if (baseURI != null && doc != null) {
            doc.setBaseURI(baseURI);
        }

        return doc;
    }

    /**
     * Build a collection of documents
     *
     * @param files the XML files to build
     * @return the Documents
     * @throws java.io.IOException
     * @throws IOException
     * @throws petascope.exceptions.PetascopeException
     * @throws ParsingException
     */
    public static List<Document> buildDocuments(Collection<File> files) throws IOException, ParsingException, PetascopeException {
        List<Document> ret = new ArrayList<Document>();
        for (File file : files) {
            ret.add(buildDocument(file));
        }
        return ret;
    }

    /**
     * Serialize a XOM Document.
     *
     * @param xomDocument the XOM Document to be serialized
     * @throws IOException
     */
    public static String serialize(Document xomDocument) throws IOException {
        return serialize(xomDocument, false);
    }

    /**
     * Serialize a XOM Document.
     *
     * @param xomDocument the XOM Document to be serialized
     * @param noPrettyPrint
     * @throws IOException
     */
    public static String serialize(Document xomDocument, boolean noPrettyPrint) throws IOException {
        OutputStream os = new ByteArrayOutputStream();
        serialize(xomDocument, os, noPrettyPrint);
        return os.toString();
    }

    /**
     * Serialize a XOM Document.
     *
     * @param xomDocument the XOM Document to be serialized
     * @param os stream where to write the result
     * @throws IOException
     */
    public static void serialize(Document xomDocument, OutputStream os) throws IOException {
        serialize(xomDocument, os, false);
    }

    /**
     * Serialize a XOM Document without pretty printing the result.
     *
     * @param xomDocument the XOM Document to be serialized
     * @param os stream where to write the result
     * @param noPrettyPrint
     * @throws IOException
     */
    public static void serialize(Document xomDocument, OutputStream os, boolean noPrettyPrint) throws IOException {
        nu.xom.Serializer serializer = new nu.xom.Serializer(os);
        if (!noPrettyPrint) {
            serializer.setIndent(2);
        }
        serializer.write(xomDocument);
    }

    /**
     * Serialize a XOM DOcument
     *
     * @param xomDocument the XOM Document to be serialized
     * @param file the file to which to serialize
     * @throws IOException
     */
    public static void serialize(Document xomDocument, File file) throws IOException {
        serialize(xomDocument, file, false);
    }

    /**
     * Serialize a XOM Document without pretty printing the result.
     *
     * @param xomDocument the XOM Document to be serialized
     * @param file
     * @param noPrettyPrint
     * @throws IOException
     */
    public static void serialize(Document xomDocument, File file, boolean noPrettyPrint) throws IOException {
        serialize(xomDocument, new FileOutputStream(file), noPrettyPrint);
    }

    /**
     * @param doc
     * @return the document name
     */
    public static String docName(Document doc) {
        if (doc == null || doc.getBaseURI() == null) {
            return null;
        }
        String ret = doc.getBaseURI();
        if (ret == null) {
            return null;
        }
        int ind = ret.lastIndexOf(File.separator);
        if (ind != -1) {
            ret = ret.substring(ind + 1);
        }
        return ret;
    }

    /**
     * @param e
     */
    public static String getBaseURI(Element e) {
        if (e == null) {
            return null;
        }
        String ret = getBaseURI(e.getDocument());
        if (ret == null) {
            ParentNode p = e;
            while (true) {
                if (p == null) {
                    return null;
                }
                p = p.getParent();
            }
        }
        return ret;
    }

    /**
     * @param doc
     */
    public static String getBaseURI(Document doc) {
        if (doc != null) {
            return doc.getBaseURI();
        }
        return null;
    }

    /**
     * @param e
     * @return the depth of <code>e</code> in the XML tree
     */
    public static int depth(Element e) {
        int ret = 0;
        ParentNode p = e.getParent();
        while (p != null) {
            p = p.getParent();
            ++ret;
        }
        return ret;
    }

    /**
     * Shortcut method for creating a XOM Attribute in the XML namespace.
     */
    public static Attribute createXMLAttribute(String namespace, String prefix, String attributeName, String attributeValue) {
        return new Attribute(prefix + ":" + attributeName, namespace, attributeValue);
    }

    /**
     * Deep copy one element to another.
     *
     * @param from the element to be copied
     * @param to the element to be replaced
     */
    public static void copyElement(Element from, Element to) {
        if (from == null || to == null) {
            return;
        }

        Element e = (Element) from.copy();
        to.removeChildren();
        while (to.getAttribute(0) != null) {
            to.removeAttribute(to.getAttribute(0));
        }
        while (e.getAttribute(0) != null) {
            Attribute a = e.getAttribute(0);
            a.detach();
            to.addAttribute(a);
        }
        if (e.getChildCount() > 0) {
            while (e.getChild(0) != null) {
                Node n = e.getChild(0);
                n.detach();
                to.appendChild(n);
            }
        }
        to.setLocalName(e.getLocalName());
        to.setBaseURI(e.getBaseURI());
        to.setNamespacePrefix(e.getNamespacePrefix());
        to.setNamespaceURI(e.getNamespaceURI());
    }

    /**
     * Return new element with the same name and attributes as the given
     * element. Note that we don't care about the prefix/namespaces here.
     *
     * @param e
     * @param ignoreAttributes set of attributes to ignore when copying
     */
    public static Element copyTag(Element e, Set<String> ignoreAttributes) {
        Element ret = null;
        if (e.getNamespacePrefix() != null) {
            ret = new Element(e.getQualifiedName(), e.getNamespaceURI());
        } else {
            ret = new Element(e.getLocalName());
        }
        for (int i = 0; i < e.getAttributeCount(); i++) {
            Attribute a = (Attribute) e.getAttribute(i).copy();
            if (ignoreAttributes != null && ignoreAttributes.contains(a.getLocalName())) {
                continue;
            }
            ret.addAttribute(a);
        }
        return ret;
    }

    /**
     * cp(<label att_1="val_1"...att_n="val_n">ch</label>) -->
     * <label att_1="val_1"...att_n="val_n">ch'</label>, where ch'!=Element
     *
     * @param n
     */
    public static Node cp(Node n) {
        if (n instanceof Element) {
            Element e = (Element) n;
            Element ne = copyTag(e, null);
            if (firstChild(e) == null) {
                String v = e.getValue();
                if (!"".equals(v)) {
                    ne.appendChild(v);
                }
            }
            return ne;
        }

        if (!(n instanceof Text)) {
            n.detach();
            return n.copy();
        } else {
            return (Text) n.copy();
        }
    }

    /**
     * @param xml
     * @param tag
     */
    public static boolean isFirstTag(String xml, String tag) {
        return tag.equals(getRootElementName(xml));
    }

    /**
     * @param xml
     */
    public static String getRootElementName(String xml) {
        int start = 0;
        while (start < xml.length()) {
            start = xml.indexOf("<", start);
            if (start == -1) {
                return null;
            }
            if (xml.charAt(start + 1) != '?') {
                int end = start + 1;
                String charTmp = String.valueOf(xml.charAt(end)).trim();
                
                while (end < xml.length() && !charTmp.equals("") && !charTmp.equals(">")) {
                    charTmp = String.valueOf(xml.charAt(end)).trim();
                    if (charTmp.equals(":")) {
                        start = end;
                    }
                    ++end;
                }
                if (end == -1) {
                    return null;
                }
                ++start;
                return xml.substring(start, end).trim();
            } else {
                ++start;
            }
        }
        return null;
    }

    /**
     * @param xml
     */
    public static String removeXmlDecl(String xml) {
        if (xml.startsWith("<?xml")) {
            xml = xml.substring(xml.indexOf("<", 1));
        }
        return xml;
    }

    /**
     * Return the text that some node contains.
     *
     * @param node
     */
    public static String getText(Element node) {
        if (node == null) {
            return null;
        }
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < node.getChildCount(); i++) {
            ret.append(node.getChild(i).toXML());
        }
        return ret.toString().trim();
    }

    public static String getXMLID(Element e) {
        if (e == null) {
            return null;
        }
        String ret = e.getAttributeValue("id", XMLSymbols.NAMESPACE_XML);
        if (ret == null) {
            ret = e.getAttributeValue("id");
        }
        return ret;
    }

    public static String getXMLIDOrName(Element e) {
        if (e == null) {
            return null;
        }
        String ret = getXMLID(e);
        if (ret == null) {
            ret = e.getAttributeValue("name");
        }
        return ret;
    }

    /**
     * Collect all children of <code>e</code> with the given names.
     *
     * @return a list of the collected elements
     */
    public static List<Element> collectAll(Element e, String... names) {
        return collectAllExcept(e, null, true, names);
    }

    public static List<Element> collectAllExcept(Element e, String except, boolean recurse, String... names) {
        List<Element> ret = new ArrayList<>();
        Set<String> namesSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        namesSet.addAll(Arrays.asList(names));
        collectAllExcept(ret, e, except, recurse, namesSet);
        return ret;
    }

    private static void collectAllExcept(List<Element> ret, Element e, String except, boolean recurse, Set<String> names) {
        String elname = e.getLocalName();
        if (names.contains(elname)) {
            ret.add(e);
            if (!recurse) {
                return;
            }
        }
        int n = e.getChildCount();
        for (int i = 0; i < n; i++) {
            Node node = e.getChild(i);
            // Relax the string checking (e.g: describeCoverage or DescribeCoverage is ok)
            if (node instanceof Element && !((Element) node).getLocalName().equalsIgnoreCase(except)) {
                collectAllExcept(ret, (Element) node, except, recurse, names);
            }
        }
    }

    /**
     * Collect all children of <code>e</code> with the given name.
     *
     * @param e
     * @param prefix
     * @param name
     * @param ctx
     * @return a list of the collected elements
     */
    public static List<Element> collectAll(Element e, String prefix, String name, XPathContext ctx) {
        List<Element> ret = new ArrayList<Element>();
        Nodes notations = null;
        if (ctx != null) {
            if (prefix != null) {
                notations = e.query("//" + prefix + ":" + name, ctx);
            } else {
                notations = e.query("//" + name, ctx);
            }
        }
        if (notations != null) {
            for (int i = 0; i < notations.size(); i++) {
                ret.add((Element) notations.get(i));
            }
        }
        return ret;
    }

    /**
     * @return the first child of <code>e</code> with the given <code>id</code>
     * when searched with DFS.
     */
    public static Element childWithId(Element e, String id) {
        String value = getXMLIDOrName(e);
        if (value != null && value.equals(id)) {
            return e;
        } else {
            int n = e.getChildCount();
            Element ret = null;
            for (int i = 0; i < n; i++) {
                Node c = e.getChild(i);
                if (c instanceof Element) {
                    ret = childWithId((Element) c, id);
                    if (ret != null) {
                        return ret;
                    }
                }
            }
            return ret;
        }
    }

    /**
     * Replaces an <code>o</code> with a new child node. If <code>o</code> does
     * not have a parent node, then a <code>NoSuchChildException</code> is
     * thrown.
     *
     * @param <T> the type parameter to specify the node type
     * @param o the old node
     * @param n the new node
     * @return the new node
     */
    public static <T extends Node> T substitute(T o, T n) {
        ParentNode p = o.getParent();
        if (p == null) {
            return n;
        }
        if (n != null) {
            n.detach();
            p.replaceChild(o, n);
        } else {
            p.removeChild(o);
        }
        return n;
    }

    /**
     * Remove all qualifiers in an XML code.
     *
     * @param xml the XML document
     * @return the same document with unqualified element names
     */
    public static String removeQualifiers(String xml) {
        // remove from elements
        xml = xml.replaceAll("<[^/:> ]+:", "<").replaceAll("</[^:> ]+:", "</");
        // remove namespace declarations
        xml = xml.replaceAll("xmlns:\\w+ *= *\"[^\"]*\"", "");
        // remove from attributes
        xml = xml.replaceAll("\" *\\w+ *: *(\\w+ *= *\")", "\" $1");
        xml = xml.replaceAll("(<\\w+ +)\\w+ *: *(\\w+ *= *\")", "$1$2");
        return xml;
    }

    /**
     * Remove all attributes from the given <code>xml</code> string
     */
    public static String removeAttributes(String xml, String[] attributes) {
        for (String att : attributes) {
            xml = xml.replaceAll(" *(\\w+:)?" + att + " *= *\"[^\"]*\" *", " ");
        }
        return xml;
    }

    /**
     * Wrap the given list of nodes in a root element
     *
     * @param n nodes to wrap
     * @param copy whether to append copies of the nodes. If false then the
     * given nodes are detached!
     * @param prefix root element prefix
     * @param name root element name
     * @param namespace root element namespace
     * @return the root element
     */
    public static <T extends Node> Element wrap(boolean copy, String prefix, String name, String namespace, T... n) {
        Element ret = new Element(prefix + ":" + name, namespace);
        if (copy) {
            Node x = null;
            for (Node node : n) {
                x = node.copy();
                x.detach();
                ret.appendChild(node);
            }
        } else {
            for (Node node : n) {
                node.detach();
                ret.appendChild(node);
            }
        }
        return ret;
    }

    /**
     * Retrieve a type 4 (pseudo randomly generated) UUID.
     *
     * @return a randomly generated UUID.
     */
    public static String newUUID() {
        return "a" + (new UUID());
    }

    /**
     * Get the first child <code>Element</code> of <code>e</code>
     */
    public static Element firstChild(Element e) {
        return firstChild(e, null);
    }

    public static Element firstChild(Element e, String name) {
        Node n = null;
        for (int i = 0; i < e.getChildCount(); i++) {
            n = e.getChild(i);
            if (n instanceof Element) {
                // Relax the string checking (e.g: <describeCoverage> or <DescribeCoverage> is ok)
                if (name == null || ((Element) n).getLocalName().equalsIgnoreCase(name)) {
                    return (Element) n;
                }
            }
        }
        return null;
    }

    /**
     * Returns first child element whose name matches a certain pattern.
     *
     * @param e Root element
     * @param pattern The pattern to match
     * @return The first child element which matches the pattern (if it exists)
     */
    public static Element firstChildPattern(Element e, String pattern) {
        Node n = null;
        for (int i = 0; i < e.getChildCount(); i++) {
            n = e.getChild(i);
            if (n instanceof Element) {
                if (pattern == null || ((Element) n).getLocalName().matches(pattern)) {
                    return (Element) n;
                }
            }
        }
        return null;
    }

    public static Element firstChildRecursive(Element e, String name) {
        Node n = null;
        for (int i = 0; i < e.getChildCount(); i++) {
            n = e.getChild(i);
            if (n instanceof Element) {
                if (name == null || ((Element) n).getLocalName().equals(name)) {
                    return (Element) n;
                }
                Element ret = firstChildRecursive((Element) n, name);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    /**
     * Returns first child element whose name matches a certain pattern,
     * recursively through the XML nodes.
     *
     * @param e Root element
     * @param pattern The pattern to match
     * @return The first child element which matches the pattern (if it exists)
     */
    public static Element firstChildRecursivePattern(Element e, String pattern) {
        Node n = null;
        for (int i = 0; i < e.getChildCount(); i++) {
            n = e.getChild(i);
            if (n instanceof Element) {
                if (pattern == null || ((Element) n).getLocalName().matches(pattern)) {
                    return (Element) n;
                }
                Element ret = firstChildRecursivePattern((Element) n, pattern);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return null;
    }

    /**
     * ch (e) --> [c_1,...,c_n], where parent(c_i)=e for all c_i
     */
    public static List<Node> ch(Node n) {
        List<Node> ret = new ArrayList<Node>();
        for (int i = 0; i < n.getChildCount(); i++) {
            Node c = n.getChild(i);

            if (c instanceof Text) {
                if (!"".equals(c.toXML().trim())) {
                    ret.add(c);
                }
            } else if (!(c instanceof Comment)) {
                ret.add(c);
            }
        }
        return ret;
    }

    /**
     * ch (e, n) --> [c_1,...,c_n], where parent(c_i)=e and name(c_i)=n for all
     * c_i
     */
    public static List<Node> ch(Node n, String name) {
        if (n instanceof Element) {
            return ListUtil.<Node, Element>cast(getChildElements((Element) n, name));
        } else {
            return Collections.<Node>emptyList();
        }
    }

    /**
     * ch (e) --> [c_1,...,c_n], where parent(c_i)=e for all c_i
     */
    public static List<Element> getChildElements(Element e) {
        return getChildElements(e, (String) null);
    }

    /**
     * ch (e, n) --> [c_1,...,c_n], where parent(c_i)=e and name(c_i)=n for all
     * c_i
     */
    public static List<Element> getChildElements(Element e, String name) {
        List<Element> l = new LinkedList<>();
        Element el = null;
        for (int i = 0; i < e.getChildCount(); i++) {
            if (e.getChild(i) instanceof Element) {
                el = (Element) e.getChild(i);
                // Relax the string checking (e.g: <describeCoverage> or <DescribeCoverage> is ok)
                if (name == null || el.getLocalName().equalsIgnoreCase(name)) {
                    l.add(el);
                }
            }
        }
        return l;
    }

    /**
     * ch (e, n) --> [c_1,...,c_n], where parent(c_i)=e and name(c_i)=n for all
     * c_i
     */
    public static List<Element> children(Element e, String... names) {
        List<Element> l = new LinkedList<Element>();
        Element el = null;
        for (int i = 0; i < e.getChildCount(); i++) {
            if (e.getChild(i) instanceof Element) {
                el = (Element) e.getChild(i);
                if (names != null) {
                    String tmp = el.getLocalName();
                    for (String n : names) {
                        if (n.equals(tmp)) {
                            l.add(el);
                            break;
                        }
                    }
                } else {
                    l.add(el);
                }
            }
        }
        return l;
    }

    /**
     * ch (e) --> [c_1,...,c_n], where parent(c_i)=e for all c_i
     */
    public static List<Node> elch(Node n) {
        List<Node> ret = new ArrayList<Node>();
        for (int i = 0; i < n.getChildCount(); i++) {
            Node c = n.getChild(i);
            if (c instanceof Element) {
                ret.add(c);
            }
        }
        return ret;
    }

    /**
     * Extract elements.
     *
     * chex (e, n) --> [c_1,...,c_n], where parent(c_i)=e and name(c_i)!=n for
     * all c_i
     */
    public static List<Node> chex(Node n, String name) {
        if (n instanceof Element) {
            Node child = null;
            Element e = null;
            List<Node> l = new LinkedList<Node>();
            for (int i = 0; i < n.getChildCount(); i++) {
                child = n.getChild(i);
                if (child instanceof Element) {
                    e = (Element) child;
                    if (!e.getLocalName().equals(name)) {
                        l.add(child);
                    }
                }
            }
            return l;
        } else {
            return Collections.<Node>emptyList();
        }
    }

    /**
     * Get the first parent element <code>name</code> of <code>el</code>
     *
     * @param el this element
     * @param names the name of the parent element
     * @return the parent element
     */
    public static Element getParent(Node el, String... names) {
        ParentNode p = el.getParent();
        while (p != null) {
            if (p instanceof Element) {
                Element e = (Element) p;
                String name = e.getLocalName();
                for (String s : names) {
                    if (name.equals(s)) {
                        return e;
                    }
                }
            }
            p = p.getParent();
        }
        return null;
    }

    /**
     * Get the first parent element <code>name</code> of <code>el</code>
     *
     * @param el this element
     * @param name the name of the parent element
     * @return the parent element
     */
    public static Element getParent(Element el, String name) {
        ParentNode p = el;
        while (p != null) {
            if (p instanceof Element) {
                Element e = (Element) p;
                if (e.getLocalName().equals(name)) {
                    return e;
                }
            }
            p = p.getParent();
        }
        return null;
    }

    public static org.w3c.dom.Document convert(Document n) {
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return DOMConverter.convert(n, db.getDOMImplementation());
        } catch (ParserConfigurationException exc) {
            System.err.println(exc.getMessage());
            return null;
        }
    }

    /**
     * Converts a DOM NodeList to a list of XOM nodes
     *
     * @param l the DOM NodeList
     * @return a list of XOM nodes
     */
    /* (cl) */
    public static List<Node> convert(org.w3c.dom.NodeList l) {
        List<Node> result = new ArrayList<Node>();
        for (int index = 0; index < l.getLength(); index++) {
            org.w3c.dom.Node n = l.item(index);
            if (n.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                continue;
            }
            result.add(DOMConverter.convert((org.w3c.dom.Element) l.item(index)));
        }
        return result;
    }

    /**
     * Converts a list of XOM nodes to an equivalent DOM NodeList
     *
     * @param l the list of XOM nodes
     * @return a DOM NodeList equivalent to l
     */
    /* (cl) */
    public static org.w3c.dom.NodeList convert(List<Node> l) {
        Element dummyRoot = new Element("DummyRoot");
        for (Node n : l) {
            dummyRoot.appendChild(n);
        }
        Document dummyDoc = new Document(dummyRoot);

        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final org.w3c.dom.Document doc = DOMConverter.convert(dummyDoc, db.getDOMImplementation());

            /*
             * TODO test this again once we use a newer version of Saxon For some strange reason, this does not work if convert is called by a Saxon XSLT extension function. If we return
             * this as instance of something that's more powerful than NodeList, Saxon 8.9 recognizes that and starts output at the DummyRoot element.
             */
            // return doc.getDocumentElement().chn();
            return new org.w3c.dom.NodeList() {

                public int getLength() {
                    return doc.getDocumentElement().getChildNodes().getLength();
                }

                public org.w3c.dom.Node item(int index) {
                    return doc.getDocumentElement().getChildNodes().item(index);
                }
            };
        } catch (ParserConfigurationException exc) {
            log.error("Error converting", exc);
            return null;
        }
    }

    /**
     * Collect all ids in <code>e</code>
     *
     * @param e the given element
     * @return a set of all ids
     */
    public static Set<String> collectIds(Element e) {
        Set<String> ret = new HashSet<String>();
        Nodes res = e.query("//@xml:id", XMLSymbols.CTX_XML);
        for (int i = 0; i < res.size(); i++) {
            Node n = res.get(i);
            if (n instanceof Attribute) {
                ret.add(((Attribute) n).getValue());
            }
        }
        return ret;
    }

    /**
     * Append an XML fragment to an XOM element.
     *
     * @param fragment
     * @return Element of the fragment, with no parents.
     */
    public static Element parseXmlFragment(String fragment) throws PetascopeException {
        fragment = fragment.replace("&", "&amp;");

        Builder docBuilder = new Builder();
        Element fragmentNode = null;
                
        try {
            fragmentNode = docBuilder.build(new StringReader(fragment)).getRootElement();
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Cannot parse XML fragment '" + fragment + "'. Reason: " + ex.getMessage(), ex);
        }
        return (Element) fragmentNode.copy();
    }
    
    
    public static Element parseXmlFragmentWithoutReplacingAmpersand(String fragment) throws PetascopeException {

        Builder docBuilder = new Builder();
        Element fragmentNode = null;
                
        try {
            fragmentNode = docBuilder.build(new StringReader(fragment)).getRootElement();
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Cannot parse XML fragment '" + fragment + "'. Reason: " + ex.getMessage(), ex);
        }
        return (Element) fragmentNode.copy();
    }

    /**
     * Extract the WCS request from the SOAP message.
     *
     * @param request SOAP request.
     * @return the embedded WCS request
     * @throws Exception in case of error when parsing the SOAP message, or
     * serializing the WCS request to XML
     */
    public static String extractWcsRequest(String request) throws PetascopeException {
        // Substring any parameter before the XML element characeter "<?xml..."
        // e.g: query=<?xml....>
        request = request.substring(request.indexOf("<"));
        Document doc = XMLUtil.buildDocument(null, request);
        Element body = ListUtil.head(
                XMLUtil.collectAll(doc.getRootElement(), XMLSymbols.LABEL_BODY));
        if (body == null) {
            throw new PetascopeException(ExceptionCode.InvalidEncodingSyntax,
                    "Missing Body from SOAP request.");
        }
        Element wcsRequest = XMLUtil.firstChild(body);
        wcsRequest.detach();
        
        try {
            return XMLUtil.serialize(new Document(wcsRequest));
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.RuntimeError, 
                    "Cannot serialize XOM document for WCS request in XML: " + request + ". Reason: " + ex.getMessage());
        }
    }

    /**
     * Transform a non-formated XML output to formated XML with 0 indentation
     * for OGC CITE test. NOTE: OCT CITE wcs2:get-kvp-core-req42 the different
     * in deep-equal is DescribeCoverage has boundedBy element below 2 parents
     * (wcs:CoverageDescriptions and wcs:CoverageDescription) while GetCoverage
     * only has 1 parent, e.g: gmlcov:RectifiedGridCoverage so the identations
     * are different, then must remove any prefix indentations to compare.
     *
     *
     * @param inputXML
     * @return
     */
    public static String formatXMLForOGCCITE(String inputXML) {
        /*try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            // NOTE: set to 0 due to OGC cite cannot check indetation different between 2 outputs.
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(parseXml(inputXML));
            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;*/
        return inputXML.replaceAll(">\\s*<", "><");
    }
    
    public static String formatXML(Element rootElement) throws PetascopeException {
        Document document = new Document(rootElement);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Serializer serializer = new Serializer(baos);
        serializer.setIndent(4);
        try {
            serializer.write(document);
        } catch (IOException ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Failed writing XML document. Reason: " + ex.getMessage());
        }
        
        // indented XML
        String result = new String(baos.toByteArray());
        
        if (ConfigManager.OGC_CITE_OUTPUT_OPTIMIZATION) {
            return formatXMLForOGCCITE(result);
        }
        
        return result;
    }

    /**
     * Format a string in XML with indentation
     *
     * @param inputXML
     * @return
     */
    public static String formatXML(String inputXML) {
        
        // Add XML declaration if not exist
        if (!inputXML.startsWith(XML_DECLERATION)) {
            inputXML = XML_DECLERATION + inputXML;
        }
        
        if (ConfigManager.OGC_CITE_OUTPUT_OPTIMIZATION) {
            return formatXMLForOGCCITE(inputXML);
        } else {
            // Not for testing OGC CITE, then return the pretty XML
            try {
                final InputSource src = new InputSource(new StringReader(inputXML));
                final org.w3c.dom.Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
                final Boolean keepDeclaration = Boolean.valueOf(inputXML.startsWith("<?xml"));

                //May need this: System.setProperty(DOMImplementationRegistry.PROPERTY,"com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");
                final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
                final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
                LSOutput lsOutput = impl.createLSOutput();
                // NOTE: QGIS cannot read UTF-16 from WMS GetCapabilities
                lsOutput.setEncoding("UTF-8");
                final LSSerializer writer = impl.createLSSerializer();

                writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); // Set this to true if the output needs to be beautified.
                writer.getDomConfig().setParameter("xml-declaration", keepDeclaration); // Set this to true if the declaration is needed to be outputted.
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                lsOutput.setByteStream(byteArrayOutputStream);
                writer.write(document, lsOutput);

                return byteArrayOutputStream.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Parse the input XML and return as XML Document Object to write to String.
     *
     * @param in
     * @return
     */
    public static org.w3c.dom.Document parseXml(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringElementContentWhitespace(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Strip spaces between XML element tags (e.g: <var>XML_Text </var> or
     * <var> XML_Text1 XML_Text2 </var>) which needs to be stripped leading and
     * trailing spaces before writing to output stream.
     *
     * @param input
     * @return
     */
    public static String trimSpaceBetweenElements(String input) {
        return input.replaceAll(">\\s*", ">").replaceAll("\\s*<", "<");
    }

    /**
     * Concatenate the children elements of a element node and return as a
     * String e.g: a,b,c,d
     *
     * @param e
     * @return
     */
    public static String childrenToString(Element e) {
        if (e == null) {
            return null;
        }
        String ret = "";
        for (int i = 0; i < e.getChildCount(); i++) {
            Node n = e.getChild(i);
            if (n instanceof Element) {
                ret += getText((Element) n) + ",";
            }
        }
        return ret.substring(0, ret.length() - 1);
    }
    
    /**
     * Parse XML String to XML document and return the root node to traverse later
     */
    public static Element parseXML(String input) throws PetascopeException {
        Element rootElement = null;
        try {
            Document doc = XMLUtil.buildDocument(null, input);
            rootElement = doc.getRootElement();
        } catch (Exception ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Cannot parse XML string '" + input + "' to XML document. Reason: " + ex.getMessage(), ex);
        }
        
        return rootElement;
    }

    /**
     * Parse a WCS requestBody in XML to a XML document with root element
     *
     * @param input
     * @return
     * @throws WCSException
     */
    public static Element parseInput(String input) throws WCSException, PetascopeException {
        Document doc = XMLUtil.buildDocument(null, input);
        Element rootElement = doc.getRootElement();

        // Validate the request which must contain the serviceType and the version
        String service = rootElement.getAttributeValue(ATT_SERVICE);
        String version = rootElement.getAttributeValue(ATT_VERSION);
        if ((null == service) || (!service.equals(KVPSymbols.WCS_SERVICE))
                || (version != null && !version.matches(KVPSymbols.WCS_VERSION_PATTERN))) {
            throw new WCSException(ExceptionCode.VersionNegotiationFailed, "Service/Version not supported.");
        }

        return rootElement;
    }

    /**
     * Simple check if an input string is in XML format (e.g: <a> .... </a> or
     * text: for c in (...))
     *
     * @param input
     * @return
     */
    public static boolean isXmlString(String input) {
        return input.startsWith("<");
    }

    /**
     * When XML string is replaced with "&lt; for <" and "&gt; for >", replace
     * them all.
     *
     * @param input
     * @return
     */
    public static String replaceEnquotes(String input) {
        return input.replaceAll("&lt;", "<").replaceAll("&amp;lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;gt;", ">")
                     .replaceAll("&amp;amp;", "&");
    }

    /**
     * Enquote the XML string in <![CDATA[ ]]> to keep special characters
     *
     * @param input
     * @return
     */
    public static String enquoteCDATA(String input) {
        return "<![CDATA[" + input + "]]>";
    }
    
    /**
     * Remove the <![CDATA[ ]]> which enquotes the input string.
     * @param input
     * @return 
     */
    public static String dequoteCDATA(String input) {
        return input.replace("<![CDATA[", "").replace("]]>", "");
    }
    
    /**
     * Remove all the spaces between XML elements of the input string
     *
     * @param inputXML
     * @return
     */
    public static String removeSpaceBetweenElements(String inputXML) {
        return inputXML.replaceAll(">\\s+<", "><").trim();
    }
    
    /**
     * A simple check that input contains XML content (the XML does not need to start with valid XML header elements)
     */
    public static boolean containsXMLContent(String input) {
        return input.startsWith("<");
    }
    
    /**
     * Serialize an object to XML string by Jackson
     */
    public static String serializeObjectToXMLString(Object obj) throws JsonProcessingException {
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String xml = xmlMapper.writer().writeValueAsString(obj);
        return xml;
    }
    
    /**
     * Return XML label combined from namespace and label, e.g: wcs:CoverageDescriptions
     */
    public static String createXMLLabel(String namespace, String label) {
        String result = namespace + ":" + label;
        return result;
    }
    
    /**
     * Create a XML string from <prefix:label>content</prefix:label>
     */
    public static String createXMLString(String namespace, String prefix, String label, String content) {
        String result = MessageFormat.format("<{0}:{1} xmlns:{0}=\"{2}\">{3}</{0}:{1}>", prefix, label, namespace, content);
        return result;
    }
    
        public static void addXMLNameSpacesOnRootElement(Map<String, String> xmlNameSpacesMap, Element rootElement) {
        
        for (Map.Entry<String, String> entry : xmlNameSpacesMap.entrySet()) {
            String prefix = entry.getKey();
            String namespace = entry.getValue();
            rootElement.addNamespaceDeclaration(prefix, namespace);
        }
    }
    
    /**
     * Add all possible XML schemaLocations to root element.
     */
    public static void addXMLSchemaLocationsOnRootElement(Set<String> schemaLocations, Element rootElement) {
        
        String attribute = "";
        for (String schemaLocation : schemaLocations) {
            attribute += schemaLocation + " ";
        }
        Attribute schemaLocationAttribute = XMLUtil.createXMLAttribute(NAMESPACE_XSI, PREFIX_XSI, ATT_SCHEMA_LOCATION, attribute.trim());
        rootElement.addAttribute(schemaLocationAttribute);
    }
    
    /**
     * Get XML NameSpace by prefix, e.g: gml -> http://www.opengis.net/gml/3.2
     * @param prefix
     * @return 
     */
    public static String getNameSpaceByPrefix(String prefix) {
        if (prefix.equals(XMLSymbols.PREFIX_GMLRGRID)) {
            return XMLSymbols.NAMESPACE_GMLRGRID;
        } else {
            return XMLSymbols.NAMESPACE_GML;
        }
    }
    
    /**
     * Replace escaped characters, e.g: &lt; and &gt; to < and >
     */
    public static String unescapeXML(String input) {
        String result = StringUtils.replace(input, "&lt;", "<");
        result = StringUtils.replace(result, "&gt;", ">");
        return result;        
    }
    
    /**
     * Strip xml declaration <?xml version="1.0" encoding="UTF-8"?>
     * from an input xml string
     */
    public static String stripXMLDeclaration(String xml) {
        xml = xml.trim();
        String result = xml;
        
        if (xml.startsWith(PREDEFINED_XML_DECLARATION_BEGIN)) {
            int index = xml.indexOf(PREDEFINED_XML_DECLARATION_END);
            result = xml.substring(index + 2, xml.length());
        }
        
        return result;
    }
    
    /**
     * Check if an input XML string has a namespace at root element
     */
    public static boolean hasXMLNameSpaceAtRootElement(String xml) {
        int firstIndex = xml.indexOf("<");
        if (firstIndex == -1) {
            // metadata is not XML string
            return false;
        }
        
        int lastIndex = xml.indexOf(">");
        
        // e.g: <ns:x xmlns:ns="http://..." ...>
        String subset = xml.substring(firstIndex, lastIndex);
       
        return subset.contains(PREFIX_XMLNS);
    }
}
