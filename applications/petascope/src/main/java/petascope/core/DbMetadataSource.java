/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser  General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hsqldb.lib.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.RasdamanException;
import petascope.util.CrsUtil;
import petascope.util.Pair;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;
import petascope.wcps.server.core.*;
import petascope.wcs2.parsers.GetCoverageRequest;
import petascope.wcs2.templates.Templates;

/**
 * The DbMetadataSource is a IMetadataSource that uses a relational database. It
 * keeps a global connection which is reused on future requests, as well as between
 * threads. Before each read, the connection is verified to be valid, and
 * recreated if necessary. This IMetadataSource is not particularly efficient,
 * because it accesses the database at least once for every read. To increase
 * efficiency, wrap a CachedMetadataSource around this one.
 *
 */
public class DbMetadataSource implements IMetadataSource {
    
    private static Logger log = LoggerFactory.getLogger(DbMetadataSource.class);
    
    /* Petascopedb Tables and Fields */
    public static final String TABLE_COVERAGE = "ps_coverage";
    public static final String COVERAGE_ID                = "id";
    public static final String COVERAGE_NAME              = "name";
    public static final String COVERAGE_NULLVALUE         = "nullvalue";
    public static final String COVERAGE_NULLDEFAULT       = "nulldefault";
    public static final String COVERAGE_NULLRESDEFAULT    = "nullresistancedefault";
    public static final String COVERAGE_INTERPTYPEDEFAULT = "interpolationtypedefault";
    public static final String COVERAGE_TYPE              = "type";
    public static final String COVERAGE_CRS               = "crs";
    public static final String COVERAGE_VISIBLE           = "visible";
    
    public static final String TABLE_CRS = "ps_crs";
    public static final String CRS_ID   = "id";
    public static final String CRS_NAME = "name";
    
    public static final String TABLE_DATATYPE = "ps_datatype";
    public static final String DATATYPE_ID       = "id";
    public static final String DATATYPE_DATATYPE = "datatype";
    
    public static final String TABLE_DESCRIPTIONS = "ps_descriptions";
    public static final String DESCRIPTIONS_ID       = "id";
    public static final String DESCRIPTIONS_COVERAGE = "coverage";
    public static final String DESCRIPTIONS_TITLE    = "title";
    public static final String DESCRIPTIONS_ABSTRACT = "abstract";
    public static final String DESCRIPTIONS_KEYWORDS = "keywords";
    
    public static final String TABLE_DOMAIN = "ps_domain";
    public static final String DOMAIN_ID           = "id";
    public static final String DOMAIN_COVERAGE     = "coverage";
    public static final String DOMAIN_I            = "i";
    public static final String DOMAIN_MINVALUE     = "minvalue";
    public static final String DOMAIN_MAXVALUE     = "maxvalue";
    public static final String DOMAIN_ISIRREGULAR  = "isirregular";
    
    public static final String TABLE_IRRSERIES = "ps_irrseries";
    public static final String IRRSERIES_ID         = "id";
    public static final String IRRSERIES_AXIS       = "axis";
    public static final String IRRSERIES_STARTVALUE = "startvalue";
    public static final String IRRSERIES_ENDVALUE   = "endvalue";
    public static final String IRRSERIES_CELL       = "cell";
    
    public static final String TABLE_FORMAT = "ps_format";
    public static final String FORMAT_ID          = "id";
    public static final String FORMAT_NAME        = "name";
    public static final String FORMAT_MIMETYPE    = "mimetype";
    public static final String FORMAT_GDALID      = "gdalid";
    public static final String FORMAT_DESCRIPTION = "description";

    public static final String TABLE_METADATA = "ps_metadata";
    public static final String METADATA_ID       = "id";
    public static final String METADATA_COVERAGE = "coverage";
    public static final String METADATA_METADATA = "metadata";

    public static final String TABLE_INTERPSET = "ps_interpolationset";
    public static final String INTERPSET_ID             = "id";
    public static final String INTERPSET_COVERAGE       = "coverage";
    public static final String INTERPSET_INTERPTYPE     = "interpolationtype";
    public static final String INTERPSET_NULLRESISTANCE = "nullresistance";
    
    public static final String TABLE_INTERPTYPE = "ps_interpolationtype";
    public static final String INTERPTYPE_ID         = "id";
    public static final String INTERPTYPE_INTERPTYPE = "interpolationtype";
    
    public static final String TABLE_NULLRESISTANCE = "ps_nullresistance";
    public static final String NULLRESISTANCE_ID             = "id";
    public static final String NULLRESISTANCE_NULLRESISTANCE = "nullresistance";
    
    public static final String TABLE_NULLSET = "ps_nullset";
    public static final String NULLSET_ID        = "id";
    public static final String NULLSET_COVERAGE  = "coverage";
    public static final String NULLSET_NULLVALUE = "nullvalue";
    
    public static final String TABLE_RANGE = "ps_range";
    public static final String RANGE_ID       = "id";
    public static final String RANGE_COVERAGE = "coverage";
    public static final String RANGE_I        = "i";
    public static final String RANGE_NAME     = "name";
    public static final String RANGE_TYPE     = "type";
    public static final String RANGE_UOM      = "uom";
    
    public static final String TABLE_UOM = "ps_uom";
    public static final String UOM_ID   = "id";
    public static final String UOM_UOM  = "uom";
    public static final String UOM_LINK = "link";
    
    /* PSQL useful keywords */
    public static final String KEYWORD_MIN = "min";
    public static final String KEYWORD_MAX = "max";
    public static final String KEYWORD_TRUE = "true";
    // Datatypes full list: http://www.postgresql.org/docs/8.4/static/datatype.html
    public static final String TYPE_FLOAT8    = "float8";    // for integers and decimals
    
    /* Status variables */
    private boolean initializing;
    private boolean checkAtInit;
    
    /* Contents of static tables */
    private Map<Integer, String> crss;
    private Map<Integer, String> dataTypes;
    private Map<Integer, String> interpolationTypes;
    private Map<Integer, String> nullResistances;
    private Map<String, String> supportedFormats;
    private Map<Integer, String>  supportedRangeUoms;
    private Map<String, String> gdalFormatsIds; // GDAL code -> format name

    /* Contents of static tables (reversed, for easy access if you
     * know the something's name and want to find out its id) */
    private Map<String, Integer> revCrss;
    private Map<String, Integer> revDataTypes;
    private Map<String, Integer> revInterpolationTypes;
    private Map<String, Integer> revNullResistances;
    private Map<String, String> revSupportedFormats;    // Not used
    private Map<String, Integer> revSupportedRangeUoms;
    private Map<String, String> revGdalFormatsIds; // format name -> GDAL code

    /* Database access info */
    private static final String pgDriver = "org.postgresql.Driver";
    private String driver;
    private String pass;
    private String url;
    private String user;
    
    /* Global database key variables */
    private Connection conn;
    private Savepoint savepoint;
    private String query;
    
    /* Cache */
    private Map<String, CoverageMetadata> cache = new HashMap<String, CoverageMetadata>();
    
    /*------------------------------------------------*/
    
    public DbMetadataSource(String driver, String url, String user, String pass) throws PetascopeException {
        this(driver, url, user, pass, true);
    }
    
    public DbMetadataSource(String driver, String url, String user, String pass, boolean checkAtInit) throws PetascopeException {
        try {
            this.driver = driver;
            Class.forName(driver).newInstance();
        } catch (ClassNotFoundException e) {
            throw new PetascopeException(ExceptionCode.ResourceError,
                    "Metadata database error: Could not find JDBC driver: " + driver, e);
        } catch (InstantiationException e) {
            throw new PetascopeException(ExceptionCode.ResourceError,
                    "Metadata database error: Could not instantiate JDBC driver: " + driver, e);
        } catch (IllegalAccessException e) {
            throw new PetascopeException(ExceptionCode.ResourceError,
                    "Metadata database error: Access denied to JDBC driver: " + driver, e);
        }
        
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.pass = pass;
        Statement s = null;
        
        try {
            
            /* Read contents of static metadata tables */
            ensureConnection();
            s = conn.createStatement();
            
            /* PS_DATATYPES */
            dataTypes = new HashMap<Integer, String>();
            revDataTypes = new HashMap<String, Integer>();
            ResultSet r = s.executeQuery("SELECT " + DATATYPE_ID + ", " + DATATYPE_DATATYPE + " FROM " + TABLE_DATATYPE);
            while (r.next()) {
                dataTypes.put(r.getInt(DATATYPE_ID), r.getString(DATATYPE_DATATYPE));
                revDataTypes.put(r.getString(DATATYPE_DATATYPE), r.getInt(DATATYPE_ID));
            }
            
            /* PS_INTERPOLATIONTYPE */
            interpolationTypes = new HashMap<Integer, String>();
            revInterpolationTypes = new HashMap<String, Integer>();
            r = s.executeQuery("SELECT " + INTERPTYPE_ID + ", " + INTERPTYPE_INTERPTYPE + " FROM " + TABLE_INTERPTYPE);
            while (r.next()) {
                interpolationTypes.put(r.getInt(INTERPTYPE_ID), r.getString(INTERPTYPE_INTERPTYPE));
                revInterpolationTypes.put(r.getString(INTERPTYPE_INTERPTYPE), r.getInt(INTERPTYPE_ID));
            }
            
            /* PS_NULLRESISTANCE */
            nullResistances = new HashMap<Integer, String>();
            revNullResistances = new HashMap<String, Integer>();
            r = s.executeQuery("SELECT " + NULLRESISTANCE_ID + ", " + NULLRESISTANCE_NULLRESISTANCE + " FROM " + TABLE_NULLRESISTANCE);
            while (r.next()) {
                nullResistances.put(r.getInt(NULLRESISTANCE_ID), r.getString(NULLRESISTANCE_NULLRESISTANCE));
                revNullResistances.put(r.getString(NULLRESISTANCE_NULLRESISTANCE), r.getInt(NULLRESISTANCE_ID));
            }
            
            /* PS_CRS */
            // NOTE: here only the CRS URI are stored, afterwards the parser will create ad-hoc objects.
            crss = new HashMap<Integer, String>();
            revCrss = new HashMap<String, Integer>();
            r = s.executeQuery("SELECT " + CRS_ID + ", " + CRS_NAME + " FROM " + TABLE_CRS);
            while (r.next()) {
                crss.put(r.getInt(CRS_ID), r.getString(CRS_NAME));
                revCrss.put(r.getString(CRS_NAME), r.getInt(CRS_ID));
            }
            
            /* PS_FORMAT */
            gdalFormatsIds = new HashMap<String, String>();
            revGdalFormatsIds = new HashMap<String, String>();
            supportedFormats = new HashMap<String, String>();
            revSupportedFormats = new HashMap<String, String>();
            r = s.executeQuery("SELECT " + 
                    FORMAT_NAME     + ", " + 
                    FORMAT_MIMETYPE + ", " + 
                    FORMAT_GDALID   + " FROM " + TABLE_FORMAT);
            while (r.next()) {
                supportedFormats.put(r.getString(FORMAT_NAME), r.getString(FORMAT_MIMETYPE));
                revSupportedFormats.put(r.getString(FORMAT_MIMETYPE), r.getString(FORMAT_NAME));
                gdalFormatsIds.put(r.getString(FORMAT_GDALID), r.getString(FORMAT_NAME));
                revGdalFormatsIds.put(r.getString(FORMAT_NAME), r.getString(FORMAT_GDALID));
            }
            
            /* PS_UOM : for ps_range UoMs*/
            supportedRangeUoms = new HashMap<Integer, String>();
            revSupportedRangeUoms = new HashMap<String, Integer>();
            r = s.executeQuery("SELECT " + UOM_ID + "," + UOM_UOM + " FROM " + TABLE_UOM);
            while (r.next()) {
                supportedRangeUoms.put(r.getInt(UOM_ID), r.getString(UOM_UOM));
                revSupportedRangeUoms.put(r.getString(UOM_UOM), r.getInt(UOM_ID));
            }
            s.close();
            
            /* Check CoverageMetadata consistency at startup, if needed */
            this.checkAtInit = checkAtInit;
            
            if (checkAtInit) {
                initializing = true;
                boolean coveragesOk = false;
                
                while (!coveragesOk) {
                    Iterator<String> coverages = coverages().iterator();
                    while (coverages.hasNext()) {
                        read(coverages.next());
                    }
                    coveragesOk = true;
                }
                initializing = false;
            }
            
        } catch (SQLException sqle) {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException e) {
                }
            }
            
            close();
            
            throw new PetascopeException(ExceptionCode.ResourceError,
                    "Metadata database error", sqle);
        }
        
    }
    
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqle) {
            }
            conn = null;
        }
    }
    
    /* List all available coverages */
    @Override
    public Set<String> coverages() throws PetascopeException {
        Statement s = null;
        Set<String> coverages;
        
        try {
            ensureConnection();
            s = conn.createStatement();
            
            ResultSet r = s.executeQuery(
                    "SELECT " + COVERAGE_NAME + 
                    " FROM "  + TABLE_COVERAGE +
                    " WHERE " + COVERAGE_VISIBLE + "= " + KEYWORD_TRUE);
            coverages = new HashSet<String>(r.getFetchSize());
            
            while (r.next()) {
                coverages.add(r.getString(COVERAGE_NAME));
            }
            s.close();
            
            return coverages;
        } catch (SQLException sqle) {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException f) {
                }
            }
            
            close();
            
            throw new PetascopeException(ExceptionCode.ResourceError,
                    "Metadata database error", sqle);
        }
    }
    
    /**
     * Fetches Coverage Data from Non-Raster Coverage created in PetascopeDB
     * @param schemaName
     * @param coverageID
     * @param coverageName
     * @param cellDomainList
     * @return
     * @throws PetascopeException
     */
    public String coverageData(String schemaName, String coverageID, String coverageName,
            List<CellDomainElement> cellDomainList) throws PetascopeException {
        
        Statement s = null;
        int pointCount = 0;
        
        try {
            ensureConnection();
            s = conn.createStatement();
            
            String genQuery = "SELECT * FROM " + schemaName + " WHERE coverage=" + coverageID;
            
            ResultSet r = s.executeQuery(genQuery);
            String pointMembers = "";
            
            while (r.next()) {
                
                String tuple = "";
                boolean isInSubset = true;
                int count = 0;
                
                for (CellDomainElement cell : cellDomainList){
                    
                    double columnValue = r.getDouble(count+3);
                    Double low = Double.parseDouble(cell.getLo().toString());
                    Double high = Double.parseDouble(cell.getHi().toString());
                    GetCoverageRequest.DimensionSubset subset = cell.getSubsetElement();
                    
                    if(columnValue >= low && columnValue <= high){
                        /* ignore the dimension for the slicePoint if subsetting operation is slicing */
                        if(subset instanceof GetCoverageRequest.DimensionTrim || low.compareTo(high) != 0){
                            if((count+1) != cellDomainList.size()) {
                                tuple += Double.toString(columnValue) + " ";
                            } else {
                                tuple += Double.toString(columnValue);
                            }
                        }
                    } else {
                        isInSubset = false;
                    }
                    count++;
                }
                
                if(isInSubset){
                    
                    String rowID = "p" + (++pointCount) + "_" + coverageName;
                    
                    if(pointMembers!=null) {                        
                        pointMembers += Templates.getTemplate(Templates.MULTIPOINT_POINTMEMBERS,
                                Pair.of("\\{gmlPointId\\}", rowID),
                                Pair.of("\\{gmlPos\\}", tuple));
                    } else {                        
                        pointMembers = Templates.getTemplate(Templates.MULTIPOINT_POINTMEMBERS,
                                Pair.of("\\{gmlPointId\\}", rowID),
                                Pair.of("\\{gmlPos\\}", tuple));
                    }
                    pointMembers += "\n";
                }
            }
            
            s.close();
            return pointMembers;
            
        } catch (SQLException sqle) {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException f) {
                }
            }
            
            close();
            
            throw new PetascopeException(ExceptionCode.ResourceError,
                    "Metadata database error", sqle);
        }
    }
    
    public String coverageType(String coverageId) throws PetascopeException {
        Statement s = null;
        try {
            ensureConnection();
            s = conn.createStatement();
            ResultSet r = s.executeQuery(
                    " SELECT " + COVERAGE_TYPE +
                    " FROM "   + TABLE_COVERAGE +
                    " WHERE "  + COVERAGE_NAME + "='" + coverageId + "'");
            if (r.next()) {
                return r.getString(COVERAGE_TYPE);
            }
            throw new PetascopeException(ExceptionCode.NoSuchCoverage.locator(coverageId),
                    "Error getting coverage type.");
        } catch (SQLException sqle) {
            throw new PetascopeException(ExceptionCode.ResourceError,
                    "Error retrieving type for coverage " + coverageId, sqle);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException f) {
                }
            }
            close();
        }
    }
    
    /**
     * Given the CoverageName; returns the CoverageID
     * @param coverageId
     * @return
     * @throws PetascopeException
     */
    public String coverageID(String coverageName) throws PetascopeException {
        Statement s = null;
        try {
            ensureConnection();
            s = conn.createStatement();
            ResultSet r = s.executeQuery(
                    " SELECT " + COVERAGE_ID +
                    " FROM "   + TABLE_COVERAGE +
                    " WHERE "  + COVERAGE_NAME + "='" + coverageName + "'");
            if (r.next()) {
                return r.getString("id");
            }
            throw new PetascopeException(ExceptionCode.NoSuchCoverage.locator(coverageName),
                    "Error getting coverageID.");
        } catch (SQLException sqle) {
            throw new PetascopeException(ExceptionCode.ResourceError,
                    "Error retrieving ID for coverage " + coverageName, sqle);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException f) {
                }
            }
            close();
        }
    }
    
    @Override
    public String formatToMimetype(String format) {
        return supportedFormats.get(format);
    }
    
    @Override
    public CoverageMetadata read(String coverageName) throws PetascopeException {
        log.debug("Reading metadata for coverage '{}'", coverageName);
        
        if ((coverageName == null) || coverageName.equals("")) {
            throw new PetascopeException(ExceptionCode.InvalidRequest,
                    "Cannot retrieve coverage with null or empty name");
        }
        if (cache.containsKey(coverageName)) {
            log.trace("Returning cached coverage metadata.");
            return cache.get(coverageName);
        }
        
        Statement s = null;
        
        try {
            ensureConnection();
            s = conn.createStatement();
            
            /* PS_COVERAGE */
            ResultSet r = s.executeQuery(
                    " SELECT " +
                    COVERAGE_ID + ", " +
                    COVERAGE_TYPE + ", " +
                    COVERAGE_NULLDEFAULT + ", " +
                    COVERAGE_INTERPTYPEDEFAULT + ", " +
                    COVERAGE_NULLRESDEFAULT + ", " +
                    COVERAGE_CRS +
                    " FROM "  + TABLE_COVERAGE +
                    " WHERE " + COVERAGE_NAME + "='" + coverageName + "'");
            if (!r.next()) {
                throw new PetascopeException(ExceptionCode.InvalidRequest,
                        "Coverage '" + coverageName + "' is not served by this server");
            }
            
            int coverage                    = r.getInt(COVERAGE_ID);
            String coverageType             = r.getString(COVERAGE_TYPE);
            String nullDefault              = r.getString(COVERAGE_NULLDEFAULT);
            String interpolationTypeDefault = interpolationTypes.get(r.getInt(COVERAGE_INTERPTYPEDEFAULT));
            String nullResistanceDefault    = nullResistances.get(r.getInt(COVERAGE_NULLRESDEFAULT));
            InterpolationMethod interpDef   = new InterpolationMethod(interpolationTypeDefault, nullResistanceDefault);
            String crsUri                   = crss.get(r.getInt(COVERAGE_CRS));
            
            /* If not previously cached, fetch CRS definition(s) from URI */
            /* PURPOSE: axes semantic and order are not available from the database,
             * but only from CRS definition (robustness).
             * NOTE1: it can be a Compound-CRS: (important) transfer all axis of different CRS into a one ordered array (List<CrsDefinition.Axis>).
             * NOTE2: CRS URI equivalence needs to be checked (SOAP/KVP).
             */
            List<CrsDefinition> crsDefs   = new ArrayList<CrsDefinition>();
            List<String> atomicUris       = CrsUtil.CrsUri.decomposeUri(crsUri);
            List<CrsDefinition.Axis> axes = new ArrayList<CrsDefinition.Axis>(); // while I decode the (C)CRS I keep trace of the number of axis: consistency check.
            List<String> axesCrs          = new ArrayList<String>(); // To ease the axis association of CRS: each CRS can have a different dimension.
            for (String uri : atomicUris) {
                // Decode URI from either resolver query or cache:
                log.info("Decoding " + uri + " ...");
                crsDefs.add(CrsUtil.parseGmlDefinition(uri));
                axes.addAll(crsDefs.get(crsDefs.size()-1).getAxes());
                for (CrsDefinition.Axis axis : axes) {
                    axesCrs.add(uri); // Eg both X and Y axis are associated to the same CRS.
                }
            }

            log.trace("(!) Coverage " + coverageName + " CRS decoded: it has " + axes.size() + (axes.size()>1?" axes":" axis") + ".");
            
            /* PS_DOMAIN */
            // Now transfer the CRS data to each axis of the coverage.
            // NOTE1: load them following the `i' order so as to match CRS axis order.
            // NOTE2: axes semantic is then read from CrsDefinition, not from database.
            r = s.executeQuery("SELECT " +
                    DOMAIN_ID + ", " +
                    DOMAIN_MINVALUE      + ", " + DOMAIN_MAXVALUE     + ", " +
                    DOMAIN_ISIRREGULAR   +
                    " FROM "     + TABLE_DOMAIN +
                    " WHERE "    + DOMAIN_COVERAGE + "='" + coverage + "'" +
                    " ORDER BY " + DOMAIN_I);
            
            CellDomainElement cell;
            DomainElement     dom;
            List<CellDomainElement> cellDomain = new ArrayList<CellDomainElement>(r.getFetchSize());
            List<DomainElement>     domain     = new ArrayList<DomainElement>(r.getFetchSize());
            
            // Consistency check: decoded axes from CRS definition MUST match the number of axes in the database.
            if (axes.size() != r.getFetchSize()) {
                //throw new PetascopeException(ExceptionCode.InvalidMetadata,
                //    "Coverage" + coverageName + " has " + r.getFetchSize() +
                //    " but its CRS (" + crsUri + ") defines " + axes.size() + " axis instead!");
                // NOTE: getFetchSize() always returns '0'! Don't rely on it, and use `while(r.next())'.
            }            
            
            // Now create the Java objects:
            int ind = 0;
            Pair<String, String> cellDimensions;
            while(r.next()) {
                
                // Fetch pixel dimensions from rasdaman
                cellDimensions = getImageDomain(coverageName, ind);
                
                // CellDomain
                cell = new CellDomainElement(
                        BigInteger.valueOf(Integer.parseInt(cellDimensions.fst)),
                        BigInteger.valueOf(Integer.parseInt(cellDimensions.snd)),
                        axes.get(ind),
                        ind);
                // Note: need direction as well for a (-1) factor on pixel index translation (e.g. height/depth).
                cellDomain.add(cell);
                log.debug("Added cellDomain element: " + cell);
                
                // Check regularity of the axis:
                boolean isIrregular = r.getBoolean(DOMAIN_ISIRREGULAR);
                
                // GML pending issues: http://212.201.49.163/trac/wiki/OgcStandardsIssues
                // Temporarily inhibit the use of irregular axes:
                //if (isIrregular) {
                //    throw new PetascopeException(ExceptionCode.InvalidMetadata,
                //            "Axis " + axes.get(ind).getType() + " of coverage " + coverageName +
                //            " is irregular, but irregular axes are still inhibited for GML related issues.");
                //}
                
                // domain
                dom = new DomainElement(
                        r.getString(DOMAIN_MINVALUE),
                        r.getString(DOMAIN_MAXVALUE),
                        axes.get(ind),
                        axesCrs.get(ind),
                        ind,
                        Integer.parseInt(cellDimensions.snd)-Integer.parseInt(cellDimensions.fst)+1,
                        isIrregular);
                domain.add(dom);
                log.debug("Added domain element: " + dom);
                
                ind += 1;
            }
            
            /* PS_RANGE */
            // TODO: ps_uom table is still empty.
            r = s.executeQuery(
                    " SELECT " + RANGE_NAME + "," + RANGE_TYPE + "," + RANGE_UOM +
                    " FROM "   + TABLE_RANGE + " WHERE " + RANGE_COVERAGE + "='" + coverage + "' ORDER BY " + RANGE_I + " ASC");
            List<RangeElement> range = new ArrayList<RangeElement>(r.getFetchSize());
            while (r.next()) {
                range.add(new RangeElement(r.getString(RANGE_NAME), dataTypes.get(r.getInt(RANGE_TYPE)), supportedRangeUoms.get(r.getInt(RANGE_UOM))));
            }
            
            /* PS_INTERPOLATIONSET */
            r = s.executeQuery(
                    " SELECT " + INTERPSET_INTERPTYPE + "," + INTERPSET_NULLRESISTANCE +
                    " FROM "   + TABLE_INTERPSET +
                    " WHERE "  + INTERPSET_COVERAGE + "='" + coverage + "'");
            Set<InterpolationMethod> interpolationSet = new HashSet<InterpolationMethod>(r.getFetchSize());
            while (r.next()) {
                interpolationSet.add(new InterpolationMethod(
                        interpolationTypes.get(r.getInt(INTERPSET_INTERPTYPE)),
                        nullResistances.get(r.getInt(INTERPSET_NULLRESISTANCE))));
            }
            
            /* PS_NULLSET */
            r = s.executeQuery(
                    " SELECT " + NULLSET_NULLVALUE + " FROM " + TABLE_NULLSET +
                    " WHERE "  + NULLSET_COVERAGE + "='" + coverage + "'");
            Set<String> nullSet = new HashSet<String>(r.getFetchSize());
            while (r.next()) {
                nullSet.add(r.getString(NULLSET_NULLVALUE));
            }
            
            /* PS_DESCRIPTIONS */
            String abstr = new String();
            String title = new String();
            String keywords = new String();
            r = s.executeQuery(
                    "SELECT " +
                    DESCRIPTIONS_TITLE + "," +
                    DESCRIPTIONS_ABSTRACT + "," +
                    DESCRIPTIONS_KEYWORDS +
                    " FROM " + TABLE_DESCRIPTIONS + " WHERE " + DESCRIPTIONS_COVERAGE + "='" + coverage + "'");
            if (r.next()) {
                abstr    = r.getString(DESCRIPTIONS_ABSTRACT);
                title    = r.getString(DESCRIPTIONS_TITLE);
                keywords = r.getString(DESCRIPTIONS_KEYWORDS);
            }
            
            r = s.executeQuery("SELECT metadata FROM ps_metadata WHERE coverage = " + coverage);
            String metadata = "";
            if (r.next()) {
                metadata = r.getString("metadata");
            }

            /* Done with SQL statements */
            s.close();
            
            /* Build the complete metadata object */
            // NOTE: create a Bbox object with list of axis/extents or just let CoverageMetadata have a getBbox method?
            // `domain' object has the required metadata to deduce the Bbox of the coverage.
            CoverageMetadata covMeta = new CoverageMetadata(
                    coverageName,    coverageType,    crsUri,          domain,            cellDomain,
                    range,           nullSet,         nullDefault,     interpolationSet,  interpDef,
                    title,           abstr,           keywords);
            covMeta.setCoverageId(coverage);
            covMeta.setMetadata(metadata);
            log.trace("Caching coverage metadata.");
            cache.put(coverageName, covMeta);
            return covMeta;
        } catch (PetascopeException ime) {
            log.error("Failed reading metadata", ime);
            if (checkAtInit && !initializing) {
                throw new PetascopeException(ExceptionCode.ResourceError,
                        "Previously valid metadata is now invalid. The metadata for coverage '" + coverageName + "' has been modified incorrectly.", ime);
            } else {
                throw new PetascopeException(ExceptionCode.InvalidRequest,
                        "Coverage '" + coverageName + "' has invalid metadata", ime);
            }
        } catch (SQLException sqle) {
            log.error("Failed reading metadata", sqle);
            if (s != null) {
                try {
                    s.close();
                } catch (SQLException f) {
                }
            }
            close();
            throw new PetascopeException(ExceptionCode.ResourceError,
                    "Metadata database error", sqle);
        }
    }
    
    /** Write a coverage's metadata to the database. This function can be used
     * for both inserting new coverages and updating coverage metadata.
     *
     * @param meta CoverageMetadata container for the information to be stored in the metadata database
     * @param commit Boolean value, specifying if we want to commit immediately or not
     */
    private void write(CoverageMetadata meta, boolean commit) throws PetascopeException {
        String coverageName = meta.getCoverageName();
        if (existsCoverageName(coverageName)) {
            updateCoverageMetadata(meta, commit);
        } else {
            insertNewCoverageMetadata(meta, commit);
        }
    }
    
    public void delete(CoverageMetadata meta, boolean commit) throws PetascopeException {
        String coverageName = meta.getCoverageName();
        if (existsCoverageName(coverageName) == false) {
            throw new PetascopeException(ExceptionCode.ResourceError,
                    "Cannot delete inexistent coverage: " + coverageName);
        }
        
        /* Delete main coverage entry from "PS_Coverage". Auxiliary metadata are
         * automatically deleted by the DB (via CASCADING) on
         * deletion of the main entry in ps_coverage */
        Statement s = null;
        try {
            s = conn.createStatement();
            setQuery("DELETE FROM " + TABLE_COVERAGE + " WHERE " + COVERAGE_NAME + "='" + coverageName + "'");
            int count = s.executeUpdate(query);
            log.trace("Affected rows: " + count);
            s.close();
            
            if (commit) {
                commitAndClose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                s.close();
            } catch (Exception e) {
            }
        }
    }
    
    /**
     * Check if there is metadata available for a given coverage name
     * @param name coverage name
     * @return true is coverage already exists
     */
    public boolean existsCoverageName(String name) {
        boolean result = false;
        Statement s = null;
        try {
            ensureConnection();
            s = conn.createStatement();
            setQuery(
                    "SELECT * FROM " + TABLE_COVERAGE + 
                    " WHERE " + COVERAGE_NAME + "='" + name + "'" +
                    " AND "   + COVERAGE_VISIBLE + "= " + KEYWORD_TRUE);
            ResultSet r = s.executeQuery(query);
            if (r.next()) {
                result = true;
            } else {
                result = false;
            }
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                s.close();
            } catch (Exception e) {
            }
        }
        return result;
    }
    
    /**
     * Check if there exists a coverage with a given ID in the metadata database.
     * @param id coverage id
     * @return true is coverage already exists
     */
    private boolean existsCoverageId(int id) {
        boolean result = false;
        Statement s = null;
        try {
            s = conn.createStatement();
            ResultSet r = s.executeQuery(
                    "SELECT * FROM " + TABLE_COVERAGE + 
                    " WHERE " + COVERAGE_ID      + "='" + id + "'" +
                    " AND "   + COVERAGE_VISIBLE + "= " + KEYWORD_TRUE);
            if (r.next()) {
                result = true;
            } else {
                result = false;
            }
            s.close();
        } catch (SQLException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                s.close();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        log.trace("Coverage with ID " + id + (result == false ? " does not " : "") + " exists.");
        return result;
    }
    
    /** Update metadata for an existing coverage. All information may change (including
     * name), but the ID of the tuple in PS_Coverage will stay the same.
     *
     * @param meta CoverageMetadata object, container of the new information.
     * @param commit True if we want to commit immediately, false to delay commit indefinitely
     */
    public void updateCoverageMetadata(CoverageMetadata meta, boolean commit) throws PetascopeException {
        Statement s = null;
        int coverageId = meta.getCoverageId();
        int count = -1;
        String coverageName = meta.getCoverageName();
        
        try {
            ensureConnection();
            s = conn.createStatement();
            
            String name = coverageName;
            String nulldefault = meta.getNullDefault();
            int interpolatiotypendefault = revInterpolationTypes.get(meta.getInterpolationDefault());
            int nullresistancedefault = revNullResistances.get(meta.getNullResistanceDefault());
            int crsId = revCrss.get(meta.getCrsUri()); // manca il public String getCrs();
            
            // Table PS_COVERAGE
            setQuery("UPDATE " + TABLE_COVERAGE + " SET " + "(" +
                    COVERAGE_NAME + "," +
                    COVERAGE_NULLDEFAULT + "," +
                    COVERAGE_INTERPTYPEDEFAULT + "," +
                    COVERAGE_NULLRESDEFAULT +
                    COVERAGE_CRS + ") = ('" +
                    name + "', '" +
                    nulldefault + "', '" +
                    interpolatiotypendefault + "', '" +
                    nullresistancedefault + "', " +
                    crsId +
                    "') WHERE " + COVERAGE_ID + "='" + coverageId + "'");
            count = s.executeUpdate(query);
            if (count <= 0) {
                throw new SQLException("Could not update table " + TABLE_COVERAGE + ".");
            }
            
            // Table PS_DOMAIN
            /* Delete old data */
            setQuery("DELETE FROM " + TABLE_DOMAIN + " WHERE " + DOMAIN_COVERAGE + "='" + coverageId + "'");
            count = s.executeUpdate(query);
            if (count <= 0) {
                throw new SQLException("Could not delete old entries from table " + TABLE_DOMAIN + ".");
            }
            /* Insert new data */
            Iterator<CellDomainElement> cellIt = meta.getCellDomainIterator();
            Iterator<DomainElement> domIt = meta.getDomainIterator();
            while (domIt.hasNext()) { // = while (cellIt.hasNext())
                DomainElement dom = domIt.next();
                CellDomainElement cell = cellIt.next();
                
                setQuery("INSERT INTO " + TABLE_DOMAIN + " (" +
                        DOMAIN_COVERAGE + ", " +
                        DOMAIN_I + ", " +
                        DOMAIN_MINVALUE + ", " +
                        DOMAIN_MAXVALUE + ") VALUES ('" +
                        coverageId + "', '" +
                        dom.getOrder() + "', '" +
                        dom.getMinValue() + "', '" +
                        dom.getMaxValue() + "')");
                if (s.executeUpdate(query) <= 0) {
                    throw new SQLException("Could not insert axis " + dom.getOrder() + " for coverage " +
                            coverageName + "(id " + coverageId + ") into table " + TABLE_DOMAIN + ".");
                }
            }
            
            // Table PS_RANGE
            /* Delete old data */
            setQuery("DELETE FROM " + TABLE_RANGE + " WHERE " + RANGE_COVERAGE + "='" + coverageId + "'");
            count = s.executeUpdate(query);
            if (count <= 0) {
                throw new SQLException("Could not delete old entries from table " + TABLE_RANGE + ".");
            }
            /* Insert new data */
            Iterator<RangeElement> rangeIt = meta.getRangeIterator();
            int i = 0;
            while (rangeIt.hasNext()) {
                RangeElement range = rangeIt.next();
                int dataType = revDataTypes.get(range.getType());
                int uomId = revSupportedRangeUoms.get(range.getUom());
                
                setQuery("INSERT INTO " + TABLE_RANGE + " (" +
                        RANGE_COVERAGE + ", " +
                        RANGE_I + ", " +
                        RANGE_NAME + ", " +
                        RANGE_TYPE + ", " +
                        RANGE_UOM + ") VALUES ('" +
                        coverageId + "', '" +
                        i + "', '" +
                        range.getName() + "', '" +
                        dataType + "', '" +
                        uomId + "')");
                if (s.executeUpdate(query) <= 0) {
                    throw new SQLException("Could not insert range " + range.getName()
                            + "(id " + i + ") for coverage "
                            + coverageName + "( id " + coverageId + ") into table" + TABLE_RANGE + ".");
                }
                i++;
            }
            
            // Table PS_INTERPOLATIONSET
            /* Delete old data */
            setQuery("DELETE FROM " + TABLE_INTERPSET + " WHERE " + INTERPSET_COVERAGE + "='" + coverageId + "'");
            count = s.executeUpdate(query);
            if (count <= 0) {
                throw new SQLException("Could not delete old entries from table " + TABLE_INTERPSET + ".");
            }
            /* Insert new data */
            Iterator<InterpolationMethod> methodIt = meta.getInterpolationMethodIterator();
            while (methodIt.hasNext()) {
                InterpolationMethod method = methodIt.next();
                int interp = revInterpolationTypes.get(method.getInterpolationType());
                int nullRes = revNullResistances.get(method.getNullResistance());
                setQuery("INSERT INTO " + TABLE_INTERPSET + " (" +
                        INTERPSET_COVERAGE + ", " +
                        INTERPSET_INTERPTYPE + ", " +
                        INTERPSET_NULLRESISTANCE + ") VALUES ('" +
                        coverageId + "', '" +
                        interp + "', '" +
                        nullRes + "')");
                if (s.executeUpdate(query) <= 0) {
                    throw new SQLException("Could not insert interpolation method (" + method.getInterpolationType()
                            + ", " + method.getNullResistance() + " ) for coverage "
                            + coverageName + "(id " + coverageId + ") into table " + TABLE_INTERPSET + ".");
                }
            }
            
            // Table PS_NULLSET
            /* Delete old data */
            setQuery("DELETE FROM " + TABLE_NULLSET + " WHERE " + NULLSET_COVERAGE + "='" + coverageId + "'");
            count = s.executeUpdate(query);
            if (count <= 0) {
                throw new SQLException("Could not delete old entries from table " + TABLE_NULLSET + ".");
            }
            /* Insert new data */
            Iterator<String> nullIt = meta.getNullSetIterator();
            while (nullIt.hasNext()) {
                String nullValue = nullIt.next();
                setQuery("INSERT INTO " + TABLE_NULLSET + " (" +
                        NULLSET_COVERAGE + ", " +
                        NULLSET_NULLVALUE + ") VALUES ('" +
                        coverageId + "', '" +
                        nullValue + "')");
                if (s.executeUpdate(query) <= 0) {
                    throw new SQLException("Could not insert null value '" + nullValue
                            + "' for coverage " + coverageName + "(id " + coverageId
                            + ") into table " + TABLE_NULLSET + ".");
                }
            }
            
            
            // Table PS_DESCRIPTIONS
            /* Delete old data */
            setQuery("DELETE FROM " + TABLE_DESCRIPTIONS + " WHERE " + DESCRIPTIONS_COVERAGE + "='" + coverageId + "'");
            count = s.executeUpdate(query);
            if (count <= 0) {
                // Coverage descriptions are not essential, do not throw an error if missing:
                //throw new SQLException("Could not delete old data from table " + TABLE_DESCRIPTIONS + ".");
                log.warn("Missing descriptions from coverage " + coverageName + "(id " + coverageId + ").");
            }
            /* Insert new data */
            String title    = meta.getTitle();
            String abstr    = meta.getAbstract();
            String keywords = meta.getKeywords();
            if (title != null)    { title    = "'" + title    + "'"; }
            if (abstr != null)    { abstr    = "'" + abstr    + "'"; }
            if (keywords != null) { keywords = "'" + keywords + "'"; }
            
            setQuery("INSERT INTO " + TABLE_DESCRIPTIONS + " (" +
                    DESCRIPTIONS_COVERAGE + ", " +
                    DESCRIPTIONS_TITLE + ", " +
                    DESCRIPTIONS_ABSTRACT + ", " +
                    DESCRIPTIONS_KEYWORDS + ") VALUES ('" +
                    coverageId + "', '" +
                    title + "', '" +
                    abstr + "', '" +
                    keywords + "')");
            if (s.executeUpdate(query) <= 0) {
                throw new SQLException("Could not insert descriptive metadata in table " + TABLE_DESCRIPTIONS +
                        " for coverage " + coverageName + " (id " + coverageId + ")");
            }
            // End of coverage metadata update
            
            s.close();
            if (commit) {
                commitAndClose();
            }
        } catch (SQLException sqle) {
            /* Abort this transaction */
            try {
                if (s != null) {
                    s.close();
                }
                abortAndClose();
            } catch (SQLException f) {
                log.warn(f.getMessage());
            }
            throw new PetascopeException(ExceptionCode.ResourceError,
                    "Metadata database error", sqle);
        }
    }
    
    /** Insert metadata for a new coverage.
     *
     * @param meta CoverageMetadata object, container of information
     * @param commit Boolean value, specifying if we want to commit immediately or not
     */
    public void insertNewCoverageMetadata(CoverageMetadata meta, boolean commit) throws PetascopeException {
        Statement s = null;
        String coverageName = meta.getCoverageName();
        int coverageId = -1;
        
        try {
            ensureConnection();
            s = conn.createStatement();
            
            String name = coverageName;
            /*  FIXME: Table PS_COVERAGE: nullValue not USED !!! */
            //            String nullvalue = "";
            String nulldefault = meta.getNullDefault();
            int interpolatiotypendefault = revInterpolationTypes.get(meta.getInterpolationDefault());
            int nullresistancedefault = revNullResistances.get(meta.getNullResistanceDefault());
            String type = meta.getCoverageType();
            String crs = meta.getCrsUri();
            
            // Table PS_COVERAGE
            String sqlQuery = "INSERT INTO " + TABLE_COVERAGE + " (" +
                    COVERAGE_NAME + ", " +
                    COVERAGE_NULLDEFAULT + ", " +
                    COVERAGE_INTERPTYPEDEFAULT + ", " +
                    COVERAGE_NULLRESDEFAULT + ", " +
                    COVERAGE_TYPE + ", " +
                    COVERAGE_CRS + ") VALUES ('" +
                    name + "', '" +
                    nulldefault + "', '" +
                    interpolatiotypendefault + "', '" +
                    nullresistancedefault + "', '" +
                    type + "', '" +
                    crs + "')";
            /* Need to get ID of the newly inserted tuple. Postgres has a cool construct (RETURNING),
             * but we can also fall-back to another generic JDBC driver */
            if (driver.equals(pgDriver)) {
                /* RETURNING clause is not standard SQL, only PostgreSQL understands it*/
                setQuery(sqlQuery + " RETURNING " + COVERAGE_ID);
                
                ResultSet r = s.executeQuery(query);
                if (r.next() == false) {
                    throw new SQLException("Could not insert new coverage in table " + TABLE_COVERAGE + ".");
                }
                /* Retrieve the ID of the newly inserted tuple (PS_COVERAGE) */
                coverageId = r.getInt(COVERAGE_ID);
            } else {
                /* Fallback to specific driver support on returning autogenerated keys. */
                String[] keys = new String[1];
                keys[0] = COVERAGE_ID;
                int c = s.executeUpdate(sqlQuery, keys);
                if (c <= 0) {
                    throw new SQLException("Could not insert new coverage in table " + TABLE_COVERAGE + ".");
                }
                /* Retrieve the ID of the newly inserted tuple (PS_Coverage) */
                ResultSet rr = s.getGeneratedKeys();
                if (rr.next() == false) {
                    throw new SQLException("Could not retrieve ID of the newly inserted tuple in table " + TABLE_COVERAGE + ".");
                }
                coverageId = rr.getInt(COVERAGE_ID);
            }
            /* check ID */
            if (coverageId < 0) {
                throw new SQLException("Generated ID (" + coverageId
                        + ") for the tuple in " + TABLE_COVERAGE + " is not valid !");
            }
            
            // Table PS_DOMAIN
            Iterator<CellDomainElement> cellIt = meta.getCellDomainIterator();
            Iterator<DomainElement> domIt = meta.getDomainIterator();
            while (domIt.hasNext()) { // = while (cellIt.hasNext())
                DomainElement dom = domIt.next();
                CellDomainElement cell = cellIt.next();
                
                setQuery("INSERT INTO " + TABLE_DOMAIN + " (" +
                        DOMAIN_COVERAGE + ", " +
                        DOMAIN_I + ", " +
                        DOMAIN_MINVALUE + ", " +
                        DOMAIN_MAXVALUE + ") VALUES ('" +
                        coverageId + "', '" +
                        dom.getOrder() + "', '" +
                        dom.getMinValue() + "', '" +
                        dom.getMaxValue() + "')");
                if (s.executeUpdate(query) <= 0) {
                    throw new SQLException("Could not insert axis " + dom.getOrder() + " for coverage "
                            + coverageName + "(id " + coverageId + ") into table " + TABLE_DOMAIN + ".");
                }
            }
            
            // Table PS_RANGE
            Iterator<RangeElement> rangeIt = meta.getRangeIterator();
            int i = 0;
            while (rangeIt.hasNext()) {
                RangeElement range = rangeIt.next();
                int dataType = revDataTypes.get(range.getType());
                int uomId = revSupportedRangeUoms.get(range.getUom());
                
                setQuery("INSERT INTO " + TABLE_RANGE + " (" +
                        RANGE_COVERAGE + ", " +
                        RANGE_I + ", " +
                        RANGE_NAME + ", " +
                        RANGE_TYPE + ", " +
                        RANGE_UOM + ") VALUES ('" +
                        coverageId + "', '" +
                        i + "', '" +
                        range.getName() + "', '" +
                        dataType + "', '" +
                        uomId + "')");
                if (s.executeUpdate(query) <= 0) {
                    throw new SQLException("Could not insert range " + range.getName()
                            + "( id " + i + " ) for coverage "
                            + coverageName + "( id " + coverageId + ") into table " + TABLE_RANGE + ".");
                }
                i++;
            }
            
            // Table PS_INTERPOLATIONSET
            Iterator<InterpolationMethod> methodIt = meta.getInterpolationMethodIterator();
            while (methodIt.hasNext()) {
                InterpolationMethod method = methodIt.next();
                int interp = revInterpolationTypes.get(method.getInterpolationType());
                int nullRes = revNullResistances.get(method.getNullResistance());
                
                setQuery("INSERT INTO " + TABLE_INTERPSET + " (" +
                        INTERPSET_COVERAGE + ", " +
                        INTERPSET_INTERPTYPE + ", " +
                        INTERPSET_NULLRESISTANCE + ") VALUES ('" +
                        coverageId + "', '" +
                        interp + "', '" +
                        nullRes + "')");
                if (s.executeUpdate(query) <= 0) {
                    throw new SQLException("Could not insert interpolation method (" + method.getInterpolationType()
                            + ", " + method.getNullResistance() + " ) for coverage "
                            + coverageName + "(id " + coverageId + ") into table " + TABLE_INTERPSET + ".");
                }
            }
            
            // Table PS_NULLSET
            Iterator<String> nullIt = meta.getNullSetIterator();
            while (nullIt.hasNext()) {
                String nullValue = nullIt.next();
                setQuery("INSERT INTO " + TABLE_NULLSET + " (" +
                        NULLSET_COVERAGE + ", " +
                        NULLSET_NULLVALUE + ") VALUES ('" +
                        coverageId + "', '" +
                        nullValue + "')");
                if (s.executeUpdate(query) <= 0) {
                    throw new SQLException("Could not insert null value '" + nullValue
                            + "' for coverage " + coverageName + "(id " + coverageId
                            + ") into table " + TABLE_NULLSET + ".");
                }
            }
            
            // Table PS_DESCRIPTIONS
            String title = meta.getTitle();
            String abstr = meta.getAbstract();
            String keywords = meta.getKeywords();
            if (title != null) {
                title = "'" + title + "'";
            }
            if (abstr != null) {
                abstr = "'" + abstr + "'";
            }
            if (keywords != null) {
                keywords = "'" + keywords + "'";
            }
            setQuery("INSERT INTO " + TABLE_DESCRIPTIONS + " (" +
                    DESCRIPTIONS_COVERAGE + ", " +
                    DESCRIPTIONS_TITLE + ", " +
                    DESCRIPTIONS_ABSTRACT + ", " +
                    DESCRIPTIONS_KEYWORDS + ") VALUES ('" +
                    coverageId + "', '" +
                    title + "', '" +
                    abstr + "', '" +
                    keywords + "')");
            if (s.executeUpdate(query) <= 0) {
                throw new SQLException("Could not insert descriptive metadata in table " + TABLE_DESCRIPTIONS +
                        " for coverage " + coverageName + " (id " + coverageId + ")");
            }
            // End of new metadata insertion
            
            s.close();
            if (commit) {
                commitAndClose();
            }
        } catch (SQLException sqle) {
            /* Abort this transaction */
            try {
                if (s != null) {
                    s.close();
                }
                abortAndClose();
            } catch (SQLException f) {
                log.warn(f.getMessage());
            }
            throw new PetascopeException(ExceptionCode.InvalidRequest,
                    "Metadata database error", sqle);
        }
    }
    
    public void ensureConnection() throws SQLException {
        synchronized (this) {
            //          if( connection == null || !connection.isValid( CONNECTION_TIMEOUT ) ) { // Not implemented by PostgreSQL yet.
            if ((conn == null) || conn.isClosed()) {
                //                log.trace("*** Opening new DB connection...");
                close();
                openConnection();
                //                log.trace("*** ok.");
            }
        }
    }
    
    public void openConnection() throws SQLException {
        conn = DriverManager.getConnection(url, user, pass);
        conn.setAutoCommit(false);
        savepoint = conn.setSavepoint();
    }
    
    public void abortAndClose() throws SQLException {
        if (conn != null) {
            conn.rollback(savepoint);
            conn.close();
            conn = null;
        }
    }
    
    public void commitAndClose() throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.close();
            conn = null;
        }
    }
    
    /* Logging function for SQL queries. */
    private void setQuery(String q) {
        this.query = q;
        log.trace("SQL Query: {}", q);
    }
    
    /* Returns the available formatToMimetype formats, as stored in the metadata database */
    public String[] getMimetypesList() {
        return supportedFormats.values().toArray(new String[1]);
    }
    
    /* Translate a mime-type to a format name, if known to rasdaman. */
    @Override
    public String mimetypeToFormat(String mime) {
        return revSupportedFormats.get(mime);
    }

    @Override
    public String formatToGdalid(String format) {
        return revGdalFormatsIds.get(format);
    }

    @Override
    public String gdalidToFormat(String gdalid) {
        return gdalFormatsIds.get(gdalid);
    }

    /**
     * @return the dataTypes
     */
    public Collection<String> getDataTypes() {
        return dataTypes.values();
    }
    
    /**
     * @return the interpolationTypes
     */
    public Collection<String> getInterpolationTypes() {
        return interpolationTypes.values();
    }
    
    /**
     * @return the nullResistances
     */
    public Collection<String> getNullResistances() {
        return nullResistances.values();
    }
    
    /**
     * Clear the internal cache of coverage metadata objects.
     */
    public void clearCache() {
        cache.clear();
    }
    
    public Collection<String> getAxisNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /*
     * Method to retrieve subset pixel indexes of an irregularly spaced axis.
     * In case of timestamps, make sure they are in a PSQL-supported format:
     * http://www.postgresql.org/docs/8.4/static/datatype-datetime.html
     * @param  covName             Coverage human-readable name
     * @param  iOrder              Order of the axis in the CRS: to indentify it in case the coverage has 2+ irregular axes.
     * @param  stringLo            The lower bound of the subset
     * @param  stringHi            The upper bound of the subset
     * @return ArrayList<int>   The subset pixel indexes
     */
    public int[] getCellFromIrregularAxis(String covName, int iOrder, String stringLo, String stringHi, String castType)
            throws PetascopeException {
        
        int[] outCells = new int[2];
        Statement s = null;
        
        try {
            ensureConnection();
            s = conn.createStatement();
            
            ResultSet r = s.executeQuery(
                    " SELECT MIN(" + TABLE_IRRSERIES + "." + IRRSERIES_CELL +
                    "), MAX(" + TABLE_IRRSERIES + "." + IRRSERIES_CELL +
                    ") FROM " + TABLE_COVERAGE + ", " + TABLE_DOMAIN + ", " + TABLE_IRRSERIES +
                    " WHERE " + TABLE_IRRSERIES + "." + IRRSERIES_AXIS +
                    " = "   + TABLE_DOMAIN    + "." + DOMAIN_ID +
                    " AND " + TABLE_DOMAIN   + "." + DOMAIN_COVERAGE +
                    " = " + TABLE_COVERAGE + "." + COVERAGE_ID +
                    " AND " + TABLE_COVERAGE + "." + COVERAGE_NAME + "='" + covName + "'" +
                    " AND " + TABLE_DOMAIN   + "." + DOMAIN_I + "=" + iOrder +
                    " AND CAST(" + TABLE_IRRSERIES + "." + IRRSERIES_ENDVALUE +
                    " AS " + castType + ") >= '" + stringLo + "'" +
                    " AND CAST(" + TABLE_IRRSERIES + "." + IRRSERIES_STARTVALUE +
                    //" AS " + castType + ") < '" + stringHi + "'");    // [a,b) subsets
                    " AS " + castType + ") <= '" + stringHi + "'");     // [a,b] subsets
            if (r.next()) {
                outCells[0] = r.getInt(KEYWORD_MIN);
                outCells[1] = r.getInt(KEYWORD_MAX);
            } else {
                throw new PetascopeException(ExceptionCode.InternalComponentError,
                        "No tuples returned from " + TABLE_IRRSERIES + ": check the metadata.");
            }
                
            
            s.close();
            return outCells;
            
        } catch (SQLException sqle) {
            /* Abort this transaction */
            try {
                if (s != null) {
                    s.close();
                }
                abortAndClose();
            } catch (SQLException f) {
                log.warn(f.getMessage());
            }
            throw new PetascopeException(ExceptionCode.InvalidRequest,
                    "Metadata database error", sqle);
        }
    }
    
    /**
     * Get the lower and upper bound of the specified coverage's dimension in pixel coordinates.
     * PURPOSE: remove redundant pixel-domain dimensions info in the metadata database.
     * @param collName     The coverage name.
     * @param dimType      The dimension of collName of which the extent.
     * @return             The minimum and maximum pixel values of the array.
     * @throws PetascopeException 
     */
    public Pair<String, String> getImageDomain(String collName, int axisOrder) throws PetascopeException {
                
        // Run RasQL query
        Object obj = null;
        String rasQuery = "select sdom(c)[" + axisOrder + "] from " + collName + " as c";
        try {
            obj = RasUtil.executeRasqlQuery(rasQuery);
        } catch (RasdamanException ex) {
            throw new PetascopeException(ExceptionCode.InternalComponentError, "Error while executing RasQL query", ex);
        }
        
        // Parse the result        
        if (obj != null) {
            RasQueryResult res = new RasQueryResult(obj);
            if (!res.getScalars().isEmpty()) {
                // TODO: can be done better with Minterval instead of sdom2bounds
                Pair<String, String> bounds = Pair.of(
                        StringUtil.split(res.getScalars().get(0), ":")[0],
                        StringUtil.split(res.getScalars().get(0), ":")[1]);
                return bounds;
            }
        }
        
        return null;
    }
}
