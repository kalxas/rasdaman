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
package petascope.util;

/**
 *  All String constants used by the WCPS are defined as String constants.
 *
 * @author <a href="mailto:a.rezaeim@jacobs-university.de">Alireza Rezaei Mahdiraji</a>
 */
public interface WcpsConstants {

    /**
     * General messages
     */
    public static final String MSG_ADD                          = "add";
    public static final String MSG_ABS                          = "abs";
    public static final String MSG_ABSTRACT_SYNTAX              = "abstractSyntax";
    public static final String MSG_ALL                          = "all";
    public static final String MSG_ARCCOS                       = "arccos";
    public static final String MSG_ARCSIN                       = "arcsin";
    public static final String MSG_ARCTAN                       = "arctan";
    public static final String MSG_AXIS                         = "axis";
    public static final String MSG_AXIS_ITERATOR                = "axisIterator";
    public static final String MSG_AXIS_TYPE                    = "axisType";
    public static final String MSG_AND                          = "and";
    public static final String MSG_AS                           = "AS";
    public static final String MSG_AVG                          = "avg";
    public static final String MSG_BINARY_OP                    = "binaryOp";
    public static final String MSG_BINARY                       = "binary";
    public static final String MSG_BIT                          = "bit";
    public static final String MSG_BITINDEX                     = "bitIndex";
    public static final String MSG_BOOLEAN                      = "boolean";
    public static final String MSG_BOOLEAN_AND                  = "booleanAnd";
    public static final String MSG_BOOLEAN_OR                   = "booleanOr";
    public static final String MSG_BOOLEAN_XOR                  = "booleanXor";
    public static final String MSG_BOOLEAN_CONSTANT             = "booleanConstant";
    public static final String MSG_BOOLEAN_NOT                  = "booleanNot";
    public static final String MSG_BOOLEAN_EQUALNUMERIC         = "booleanEqualNumeric";
    public static final String MSG_BOOLEAN_EQUAL_STRING         = "booleanEqualString";
    public static final String MSG_BOOLEAN_NOT_EQUAL_STRING     = "booleanNotEqualString";
    public static final String MSG_BOOLEAN_NOTEQUALNUMERIC      = "booleanNotEqualNumeric";
    public static final String MSG_BOOLEAN_LESSTHAN             = "booleanLessThan";
    public static final String MSG_BOOLEAN_GREATERTHAN          = "booleanGreaterThan";
    public static final String MSG_BOOLEAN_LESSOREQUAL          = "booleanLessOrEqual";
    public static final String MSG_BOOLEAN_GREATEROREQUAL       = "booleanGreaterOrEqual";
    public static final String MSG_C                            = "c";
    public static final String MSG_CASE                         = "case";
    public static final String MSG_CAST                         = "cast";
    public static final String MSG_CELL_DOMAIN                  = "cell domain";
    public static final String MSG_CELL_DOMAIN_ELEMENT          = "CellDomainElement";
    public static final String MSG_CELLS                        = "cells";
    public static final String MSG_CHAR                         = "char";
    public static final String MSG_CHILD                        = "child";
    public static final String MSG_COMPLEX                      = "complex";
    public static final String MSG_COMPLEX_CONSTANT             = "complexConstant";
    public static final String MSG_COMPONENT                    = "component";
    public static final String MSG_CONDITION                    = "condition";
    public static final String MSG_CONST                        = "const";
    public static final String MSG_CONSTANT                     = "constant";
    public static final String MSG_CONDENSE                     = "condense";
    public static final String MSG_CONSTRUCT                    = "construct";
    public static final String MSG_COORD                        = "coord";
    public static final String MSG_COORDS                       = "coords";
    public static final String MSG_COS                          = "cos";
    public static final String MSG_COSH                         = "cosh";
    public static final String MSG_COVERAGE                     = "coverage";
    public static final String MSG_COVERAGE_NAME                = "coverageName";
    public static final String MSG_COVERAGE_ITERATOR            = "coverageIterator";
    public static final String MSG_COUNT                        = "count";
    public static final String MSG_CREATE                       = "create";
    public static final String MSG_CRS                          = "crs";
    public static final String MSG_CRS_METADATA                 = "crs metadata";
    public static final String MSG_CRS_SET                      = "crsSet";
    public static final String MSG_CRS_SET_CAMEL                = "CrsSet";
    public static final String MSG_CRS_TRANSFORM                = "crsTransform";
    public static final String MSG_CUBIC                        = "cubic";
    public static final String MSG_DEFAULT                      = "default";
    public static final String MSG_DIV                          = "/";
    public static final String MSG_DIV_S                        = "div";
    public static final String MSG_DOMAIN                       = "domain";
    public static final String MSG_DOMAIN_METADATA              = "domain metadata";
    public static final String MSG_DOMAIN_METADATA_CAMEL        = "DomainMetadata";
    public static final String MSG_DOUBLE                       = "double";
    public static final String MSG_DYNAMIC_TYPE                 = "dynamic_type";
    public static final String MSG_ELSE                         = "else";
    public static final String MSG_ENCODE                       = "encode";
    public static final String MSG_EQUAL                        = "=";
    public static final String MSG_EQUALS                       = "equals";
    public static final String MSG_ERROR                        = "error";
    public static final String MSG_EXP                          = "exp";
    public static final String MSG_EXTEND                       = "extend";
    public static final String MSG_EXTRA_PARAMETERS             = "extraParameters";
    public static final String MSG_FALSE                        = "false";
    public static final String MSG_FIELD                        = "field";
    public static final String MSG_FIELD_SELECT                 = "fieldSelect";
    public static final String MSG_FLOAT                        = "float";
    public static final String MSG_FORMAT                       = "format";
    public static final String MSG_FROM                         = "from";
    public static final String MSG_FULL                         = "full";
    public static final String MSG_GREATER_THAN                 = "greaterThan";
    public static final String MSG_GREATER_OR_EQUAL             = "greaterOrEqual";
    public static final String MSG_HALF                         = "half";
    public static final String MSG_I                            = "i";
    public static final String MSG_ID_LOWERCASE                 = "id";
    public static final String MSG_IDENTIFIER                   = "identifier";
    public static final String MSG_IMAGE_CRS                    = "imageCRS";
    public static final String MSG_IMAGE_CRS2                   = "imageCrs";
    public static final String MSG_IMAGE_CRSDOMAIN              = "imageCrsDomain";
    public static final String MSG_INT                          = "int";
    public static final String MSG_INTERPOLATION_DEFAULT        = "interpolationDefault";
    public static final String MSG_INTERPOLATION_METHOD         = "interpolationMethod";
    public static final String MSG_INTERPOLATION_SET            = "interpolationSet";
    public static final String MSG_ITERATOR                     = "iterator";
    public static final String MSG_ITERATORVAR                  = "iteratorVar";
    public static final String MSG_IM                           = "im";
    public static final String MSG_IN                           = "in";
    public static final String MSG_LESS_THAN                    = "lessThan";
    public static final String MSG_LESS_OR_EQUAL                = "lessOrEqual";
    public static final String MSG_LINEAR                       = "linear";
    public static final String MSG_LN                           = "ln";
    public static final String MSG_LOG                          = "log";
    public static final String MSG_LONG                         = "long";
    public static final String MSG_LOWER_BOUND                  = "lowerBound";
    public static final String MSG_MAX                          = "max";
    public static final String MSG_MIME                         = "mime";
    public static final String MSG_MIN                          = "min";
    public static final String MSG_MINUS                        = "-";
    public static final String MSG_MINUS_S                      = "minus";
    public static final String MSG_MULT                         = "mult";
    public static final String MSG_MULTIPOINT_COVERAGE          = "MultiPointCoverage";
    public static final String MSG_NAME                         = "name";
    public static final String MSG_NEAREST                      = "nearest";
    public static final String MSG_NONE                         = "none";
    public static final String MSG_NOT_EQUALS                   = "notEqual";
    public static final String MSG_NULL                         = "null";
    public static final String MSG_NULL_RESISTANCE              = "nullResistance";
    public static final String MSG_NULL_SET                     = "nullSet";
    public static final String MSG_NUMERIC_ABS                  = "numericAbs";
    public static final String MSG_NUMERIC_ADD                  = "numericAdd";
    public static final String MSG_NUMERIC_CONSTANT             = "numericConstant";
    public static final String MSG_NUMERIC_DIV                  = "numericDiv";
    public static final String MSG_NUMERIC_MINUS                = "numericMinus";
    public static final String MSG_NUMERIC_MULT                 = "numericMult";
    public static final String MSG_NUMERIC_SQRT                 = "numericSqrt";
    public static final String MSG_NUMERIC_UNARY_MINUS          = "numericUnaryMinus";
    public static final String MSG_NOT                          = "not";
    public static final String MSG_OGC                          = "ogc";
    public static final String MSG_OR                           = "or";
    public static final String MSG_OP1                          = "op1";
    public static final String MSG_OP2                          = "op2";
    public static final String MSG_OPERATION                    = "operation";
    public static final String MSG_OP_PLUS                      = "opPlus";
    public static final String MSG_OP_MULT                      = "opMult";
    public static final String MSG_OP_MAX                       = "opMax";
    public static final String MSG_OP_MIN                       = "opMin";
    public static final String MSG_OP_AND                       = "opAnd";
    public static final String MSG_OP_OR                        = "opOr";
    public static final String MSG_OTHER                        = "other";
    public static final String MSG_OVER                         = "over";
    public static final String MSG_OVERLAY                      = "overlay";
    public static final String MSG_PARAM                        = "param";
    public static final String MSG_POW                          = "pow";
    public static final String MSG_PLUS_S                       = "plus";
    public static final String MSG_QUERY                        = "query";
    public static final String MSG_XOR                          = "xor";
    public static final String MSG_PLUS                         = "+";
    public static final String MSG_PLUS_I                       = "+i";
    public static final String MSG_PROCESS_COVERAGE_REQUEST     = "ProcessCoveragesRequest";
    public static final String MSG_QUADRATIC                    = "quadratic";
    public static final String MSG_RANGE_CONSTRUCTOR            = "rangeConstructor";
    public static final String MSG_RASSERVICE                   = "RASSERVICE";
    public static final String MSG_RAW                          = "raw";
    public static final String MSG_RE                           = "re";
    public static final String MSG_REDUCE                       = "reduce";
    public static final String MSG_RESULT                       = "result";
    public static final String MSG_SCALE                        = "scale";
    public static final String MSG_SCALAR_EXPR                  = "scalarExpr";
    public static final String MSG_SELECT                       = "select";
    public static final String MSG_SERVLET_HTMLPATH             = "/templates/wcps-servlet.html";
    public static final String MSG_SET_IDENTIFIER               = "setIdentifier";
    public static final String MSG_SET_CRSSET                   = "setCrsSet";
    public static final String MSG_SET_INTERPOLATION_DEFAULT    = "setInterpolationDefault";
    public static final String MSG_SET_INTERPOLATION_SET        = "setInterpolationSet";
    public static final String MSG_SET_NULL_SET                 = "setNullSet";
    public static final String MSG_SHORT                        = "short";
    public static final String MSG_SIN                          = "sin";
    public static final String MSG_SINH                         = "sinh";
    public static final String MSG_SLICE                        = "slice";
    public static final String MSG_SLICING_POSITION             = "slicingPosition";
    public static final String MSG_SOME                         = "some";
    public static final String MSG_SQRT                         = "sqrt";
    public static final String MSG_STAR                         = "*";
    public static final String MSG_STORE                        = "store";
    public static final String MSG_STRLOW                       = "StrLow";
    public static final String MSG_STRHI                        = "StrHi";
    public static final String MSG_STRING_CONSTANT              = "stringConstant";
    public static final String MSG_STRING_IDENTIFIER            = "stringIdentifier";
    public static final String MSG_SRS_NAME                     = "srsName";
    public static final String MSG_SCALARS                      = "scalars";
    public static final String MSG_SWITCH                       = "switch";
    public static final String MSG_TAN                          = "tan";
    public static final String MSG_TANH                         = "tanh";
    public static final String MSG_TEXT                         = "text";
    public static final String MSG_TEXT_PLAIN                   = "text/plain";
    public static final String MSG_TEMP                         = "temp";
    public static final String MSG_TRANSLATION                  = "translation";
    public static final String MSG_TRIM                         = "trim";
    public static final String MSG_TYPE                         = "type";
    public static final String MSG_TRUE                         = "true";
    public static final String MSG_TWO_INDEXES                  = "two indexes";
    public static final String MSG_UNARY_OP                     = "unaryOp";
    public static final String MSG_UNARY_PLUS                   = "unaryPlus";
    public static final String MSG_UNARY_MINUS                  = "unaryMinus";
    public static final String MSG_UNSIGNED_INT                 = "unsigned int";
    public static final String MSG_UNSIGNED_LONG                = "unsigned long";
    public static final String MSG_UNSIGNED_SHORT               = "unsigned short";
    public static final String MSG_UPPER_BOUND                  = "upperBound";
    public static final String MSG_USING                        = "using";
    public static final String MSG_VALUE                        = "value";
    public static final String MSG_VALUES                       = "values";
    public static final String MSG_VAR                          = "var";
    public static final String MSG_VARIABLE                     = "variable";
    public static final String MSG_VARIABLE_REF                 = "variableRef";
    public static final String MSG_WCPS                         = "wcps";
    public static final String MSG_WCPS_COVERAGES               = "wcpsProcessCoverages.xsd";
    public static final String MSG_WCPS_PROCESS_COVERAGE_XSD    = "/xml/ogc/wcps/1.0.0/wcpsProcessCoverages.xsd";
    public static final String MSG_WHERE                        = "where";
    public static final String MSG_WGS84                        = "WGS84";
    public static final String MSG_XML                          = "xml";
    public static final String MSG_XML_HEADER                   = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
    public static final String MSG_XMLNS                        = "xmlns=\"http://www.opengis.net/wcps/1.0\" service=\"WCPS\" version=\"1.0.0\"";
    public static final String MSG_XML_SYNTAX                   = "xmlSyntax";
    public static final String MSG_Y                            = "Y";

    /**
     * DBParam messages
     */
    public static final String DBPARAM_SETTING_PROPERTIES           = "settings.properties";
    public static final String DBPARAM_METADATA_DRIVER              = "metadata_driver";
    public static final String DBPARAM_METADATA_PASS                = "metadata_pass";
    public static final String DBPARAM_METADATA_URL                 = "metadata_url";
    public static final String DBPARAM_METADATA_USER                = "metadata_user";

    /**
     * Exception messages
     */
    public static final String ERRTXT_UNEXPETCTED_NODE             = "Unexpected node";
    public static final String ERRTXT_MISSING_SWITCH_DEFAULT       = "The default branch of the switch is missing.";
}
