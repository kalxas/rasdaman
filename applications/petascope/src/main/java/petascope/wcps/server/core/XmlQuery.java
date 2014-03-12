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

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import petascope.ConfigManager;
import petascope.core.DbMetadataSource;
import static petascope.core.DbMetadataSource.*;
import petascope.core.IDynamicMetadataSource;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCPSException;
import petascope.util.Triple;
import petascope.util.WcpsConstants;
import static petascope.util.ras.RasConstants.*;

/**
 *
 * @author Andrei Aiordachioaie
 */
public class XmlQuery extends AbstractRasNode {

    private static Logger log = LoggerFactory.getLogger(XmlQuery.class);

    private String mime;
    private ArrayList<CoverageIterator> iterators;
    private BooleanScalarExpr where;
    private IRasNode coverageExpr;
    private IDynamicMetadataSource meta;
    private ArrayList<CoverageIterator> dynamicIterators;
    /* Variables used in the XML query are renamed. The renaming is explained below.
     *
     * Variables declared in the same expression (construct, const, condense)
    will be collapsed into one multidimensional variable name. For
    "construct img over $px x(1:10), $py y(1:10) values ... ", the variables could
    be translated as: $px -> "iteratorA[0]", $py -> "iteratorA[1]".
     * Variables declared in different expression will have different prefixes,
    built from "varPrefix" + "varStart".
     *

     * Used in condenser, construct and constant coverage expressions. */
    // VariableIndexCount stores the dimensionality of each renamed variable
    private HashMap<String, Integer> varDimension;
    // VariableNewName is used to translate the old var name into the multi-dim var name
    private HashMap<String, String> variableTranslator;
    private String varPrefix = WcpsConstants.MSG_I + "_";
    private char varSuffix = 'i';

    public String getMimeType() {
        return mime;
    }

    public XmlQuery(IDynamicMetadataSource source) {
        super();
        this.meta = source;
        iterators = new ArrayList<CoverageIterator>();
        dynamicIterators = new ArrayList<CoverageIterator>();
        variableTranslator = new HashMap<String, String>();
        varDimension = new HashMap<String, Integer>();
    }

    public XmlQuery(Node node) throws WCPSException, PetascopeException, SecoreException {
        iterators = new ArrayList<CoverageIterator>();
        dynamicIterators = new ArrayList<CoverageIterator>();
        variableTranslator = new HashMap<String, String>();
        varDimension = new HashMap<String, Integer>();
        this.startParsing(node);
    }

    public void startParsing(Node node) throws WCPSException, PetascopeException, SecoreException {
        log.debug("Processing XML Request: " + node.getNodeName());

        Node x = node.getFirstChild();


        while (x != null) {
            if (x.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
                x = x.getNextSibling();
                continue;
            }

            log.info("The current node is: " + x.getNodeName());

            if (x.getNodeName().equals(WcpsConstants.MSG_COVERAGE_ITERATOR)) {
                iterators.add(new CoverageIterator(x, this));
            } else if (x.getNodeName().equals(WcpsConstants.MSG_WHERE)) {
                where = new BooleanScalarExpr(x.getFirstChild(), this);
            } else if (x.getNodeName().equals(WcpsConstants.MSG_ENCODE)) {
                EncodeDataExpr encode;

                try {
                    encode = new EncodeDataExpr(x, this);
                } catch (WCPSException ex) {
                    throw ex;
                }
                coverageExpr = encode;
                mime = encode.getMime();
            } else {
                // It has to be a scalar Expr
                coverageExpr = new ScalarExpr(x, this);
                mime = WcpsConstants.MSG_TEXT_PLAIN;
            }

            x = x.getNextSibling();
        }
    }

    public Boolean isIteratorDefined(String iteratorName) {
        Iterator<CoverageIterator> it = iterators.iterator();
        while (it.hasNext()) {
            CoverageIterator tmp = it.next();
            if (iteratorName.equals(tmp.getIteratorName())) {
                return true;
            }
        }

        it = dynamicIterators.iterator();
        while (it.hasNext()) {
            CoverageIterator tmp = it.next();
            if (iteratorName.equals(tmp.getIteratorName())) {
                return true;
            }
        }

        return false;
    }

    /* Stores information about dynamically created iterators, as metadata.
     * For example, from a Construct Coverage expression.
     */
    public void addDynamicCoverageIterator(CoverageIterator i) {
        dynamicIterators.add(i);
    }

    public Iterator<String> getCoverages(String iteratorName) throws WCPSException {
        for (int i = 0; i < iterators.size(); ++i) {
            if (iterators.get(i).getIteratorName().equals(iteratorName)) {
                return iterators.get(i).getCoverages();
            }
        }

        for (int i = 0; i < dynamicIterators.size(); ++i) {
            if (dynamicIterators.get(i).getIteratorName().equals(iteratorName)) {
                return dynamicIterators.get(i).getCoverages();
            }
        }

        throw new WCPSException(WcpsConstants.MSG_ITERATOR + " " + iteratorName + " not defined");
    }

    public boolean isDynamicCoverage(String coverageName) {
        for (int i = 0; i < dynamicIterators.size(); ++i) {
            Iterator<String> iterator =
                    ((CoverageIterator) dynamicIterators.get(i)).getCoverages();
            while (iterator.hasNext()) {
                if (iterator.next().equals(coverageName)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** Creates a new (translated) variable name for an expression that
     * has referenceable variables.
     * @return String a new variable name assigned
     */
    public String registerNewExpressionWithVariables() {
        String name = varPrefix + varSuffix;
        varDimension.put(name, 0);
        varSuffix++;
        return name;
    }

    /** Remember a variable that can be referenced in the future. This function
     * assigns it a index code, that should then be used to reference that variable
     * in the RasQL query.
     *
     * If the variable is already referenced, then this function does nothing.
     * @param name Variable name
     */
    public boolean addReferenceVariable(String name, String translatedName) {
        if (varDimension.containsKey(translatedName) == false) {
            return false;
        }

        Integer index = varDimension.get(translatedName);
        Integer newIndex = index + 1;
        varDimension.put(translatedName, newIndex);
        variableTranslator.put(name, translatedName + "[" + index + "]");

        return true;
    }

    /** Retrieve the translated name assigned to a specific reference (scalar) variable */
    public String getReferenceVariableName(String name) throws WCPSException {
        String newName = variableTranslator.get(name);
        return newName;
    }

    public String toRasQL() {
        String result = "";
        boolean whereIsNull = true;

        if (coverageExpr instanceof ScalarExpr &&
            ((ScalarExpr)coverageExpr).isMetadataExpr()) {
            // in this case we shouldn't make any rasql query
            result = coverageExpr.toRasQL();
        } else {
            // rasql query
            result = RASQL_SELECT + " " + coverageExpr.toRasQL() + " " + RASQL_FROM + " ";
            Iterator<CoverageIterator> it = iterators.iterator();
            boolean first = true;

            // Compose list of coverages (FROM clause) and fetch the corresponendt OID
            // rasdamanColls = {{OID, name, alias},...}
            List<Triple<BigInteger,String,String>> rasdamanColls = new ArrayList<Triple<BigInteger,String,String>>();
            while (it.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    result += ", ";
                }

                CoverageIterator cNext = it.next();
                // The COVERAGE name not necessarily coincide with the COLLECTION name
                // Need to fetch coll-name and OID:
                try {
                rasdamanColls.add(Triple.of(
                        meta.read(cNext.getCoverages().next()).getRasdamanCollection().fst,
                        meta.read(cNext.getCoverages().next()).getRasdamanCollection().snd,
                        cNext.getIteratorName()
                        ));
                } catch (PetascopeException ex) {
                    log.error("Cannot read metadata of coverage " + cNext.getCoverages().next() + ": dynamic coverage?");
                    log.error(ex.getMessage());
                } catch (SecoreException ex) {
                    log.error("Problem with SECORE resolver: " + ex.getMessage());
                }

                // Append ``collection'' name (+ alias) to RasQL `FROM'
                result += rasdamanColls.get(rasdamanColls.size()-1).snd + " " + RASQL_AS + " " + cNext.getIteratorName();
            }

            // Add embedded WHERE conditions
            if (null != where) {
                result += " where " + where.toRasQL();
                whereIsNull = false;
            }

            // Add/append OID constraints (1 W*S coverage = 1 MDD) in the WHERE clause
            for (Triple<BigInteger,String,String> rasdamanColl : rasdamanColls) {
                result += (whereIsNull)
                        ? " " + RASQL_WHERE + " " + RASQL_OID + "(" + rasdamanColl.trd + ")=" + rasdamanColl.fst
                        : " " + RASQL_AND   + " " + RASQL_OID + "(" + rasdamanColl.trd + ")=" + rasdamanColl.fst
                        ;
                whereIsNull = false;
            }
        }
        return result;
    }

    public IDynamicMetadataSource getMetadataSource() {
        return meta;
    }

    public ArrayList<CoverageIterator> getCoverageIterator(){
        return iterators;
    }

    public String toPostGISQuery() throws PetascopeException, SecoreException, SQLException {

        Iterator<CoverageIterator> it = iterators.iterator();
        CoverageIterator cNext = it.next();
        String coverageName = cNext.getCoverages().next();

        String result = "";
        // Get bbox parameters
        int bracketOpen = coverageExpr.toRasQL().indexOf("[");
        int bracketClose = coverageExpr.toRasQL().indexOf("]");
        String[] trimParams = coverageExpr.toRasQL().substring(bracketOpen + 1, bracketClose).split(",");

        String xmin = trimParams[0].split(":")[0];
        String ymin = trimParams[1].split(":")[0];
        String zmin = trimParams[2].split(":")[0];
        String xmax = "";
        String ymax = "";
        String zmax = "";

        if ( trimParams[0].split(":").length == 2 ){
            xmax = trimParams[0].split(":")[1];
        } else if ( trimParams[0].split(":").length == 1 ){
            xmax = trimParams[0].split(":")[0];
        }
        if ( trimParams[1].split(":").length == 2 ){
            ymax = trimParams[1].split(":")[1];
        } else if ( trimParams[1].split(":").length == 1 ) {
            ymax = trimParams[1].split(":")[0];
        }
        if ( trimParams[2].split(":").length == 2 ){
            zmax = trimParams[2].split(":")[1];
        } else if ( trimParams[2].split(":").length == 1 ) {
            zmax = trimParams[2].split(":")[0];
        }

        DbMetadataSource meta = new DbMetadataSource(ConfigManager.METADATA_DRIVER,
                ConfigManager.METADATA_URL,
                ConfigManager.METADATA_USER,
                ConfigManager.METADATA_PASS, false);
        String query = "";
        ResultSet res = null;
        if (xmin.equals(WcpsConstants.MSG_STAR)) {
            query = "SELECT min(St_X(" + MULTIPOINT_COORDINATE + ")) FROM " + TABLE_MULTIPOINT;
            res = meta.executePostGISQuery(query);
            while(res.next()){
                xmin = res.getString(1);
            }
        }
        if (ymin.equals(WcpsConstants.MSG_STAR)) {
            query = "SELECT min(St_Y(" + MULTIPOINT_COORDINATE + ")) FROM " + TABLE_MULTIPOINT;
            res = meta.executePostGISQuery(query);
            while(res.next()){
                ymin = res.getString(1);
            }
        }
        if (zmin.equals(WcpsConstants.MSG_STAR)) {
            query = "SELECT min(St_Z(" + MULTIPOINT_COORDINATE + ")) FROM " + TABLE_MULTIPOINT;
            res = meta.executePostGISQuery(query);
            while(res.next()){
                zmin = res.getString(1);
            }
        }
        if (xmax.equals(WcpsConstants.MSG_STAR)){
            query = "SELECT max(St_X(" + MULTIPOINT_COORDINATE + ")) FROM " + TABLE_MULTIPOINT;
            res = meta.executePostGISQuery(query);
            while(res.next()){
                xmax = res.getString(1);
            }
        } else if (xmax.equals("")){
            xmax = xmin;
        }

        if (ymax.equals(WcpsConstants.MSG_STAR)){
            query = "SELECT max(St_Y(" + MULTIPOINT_COORDINATE + ")) FROM " + TABLE_MULTIPOINT;
            res = meta.executePostGISQuery(query);
            while(res.next()){
                ymax = res.getString(1);
            }
        } else if (ymax.equals("")){
            ymax = ymin;
        }

        if (zmax.equals(WcpsConstants.MSG_STAR)){
            query = "SELECT max(St_Z(" + MULTIPOINT_COORDINATE + ")) FROM " + TABLE_MULTIPOINT;
            res = meta.executePostGISQuery(query);
            while(res.next()){
                zmax = res.getString(1);
            }
        } else if (zmax.equals("")){
            zmax = zmin;
        }

        // Handling Slicing
        String selectClause = " SELECT " + TABLE_MULTIPOINT + "." + MULTIPOINT_VALUE + ",";
        String whereClause = " WHERE "
                 + TABLE_COVERAGE + "." + COVERAGE_NAME + "='" + coverageName + "' AND "
                 + TABLE_COVERAGE + "." + COVERAGE_ID  + " = " + TABLE_MULTIPOINT + "." + MULTIPOINT_COVERAGE_ID;

        if ( xmin.equals(xmax) ) {
            selectClause +=     " St_Y(" + MULTIPOINT_COORDINATE + ") || ',' || St_Z(" + MULTIPOINT_COORDINATE + ") AS " + MULTIPOINT_COORDINATE;
            whereClause  += " AND St_X(" + MULTIPOINT_COORDINATE + ")=" + xmin;
        } else if ( ymin.equals(ymax) ) {
            selectClause +=     " St_X(" + MULTIPOINT_COORDINATE + ") || ',' || St_Z(" + MULTIPOINT_COORDINATE + ") AS " + MULTIPOINT_COORDINATE;
            whereClause  += " AND St_Y(" + MULTIPOINT_COORDINATE + ")=" + ymin;
        } else if ( zmin.equals(zmax) ) {
            selectClause +=     " St_X(" + MULTIPOINT_COORDINATE + ") || ',' || St_Y(" + MULTIPOINT_COORDINATE + ") AS " + MULTIPOINT_COORDINATE;
            whereClause  += " AND St_Z(" + MULTIPOINT_COORDINATE + ")=" + zmin;
        } else {
            selectClause += " St_X(" + MULTIPOINT_COORDINATE + ") || ',' || St_Y(" + MULTIPOINT_COORDINATE + ") || ',' "
                        + "|| St_Z(" + MULTIPOINT_COORDINATE + ") AS " + MULTIPOINT_COORDINATE;
            whereClause += " AND " + TABLE_MULTIPOINT + "." + MULTIPOINT_COORDINATE + " && "
                       + "'BOX3D(" + xmin + " " + ymin + " " + zmin + "," + xmax + " " + ymax + " " + zmax + ")'::box3d ";
        }

        result = selectClause +
                " FROM " + TABLE_COVERAGE + "," + TABLE_MULTIPOINT +
                whereClause + " ORDER BY " + TABLE_MULTIPOINT + "." + MULTIPOINT_ID;

        return result;
    }
}
