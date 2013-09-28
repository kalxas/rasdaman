/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU  General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.util;

/**
 *  All String constants used by the WCPS are defined as String constants.
 * 
 * 
 * @author <a href="mailto:a.rezaeim@jacobs-university.de">Alireza Rezaei Mahdiraji</a>
 */
public interface WCPSConstants {
    
    /**
     * General messages  
     */
    public static final String MSG_A                            = "a";
    public static final String MSG_ADD                          = "add";  
    public static final String MSG_ADDING_VALUES                = "adding value";
    public static final String MSG_ADD_NEW_AXIS                 = "added new axis to list";
    public static final String MSG_ABS                          = "abs";
    public static final String MSG_ABSTRACT_SYNTAX              = "abstractSyntax";
    public static final String MSG_ADD_SUBSET                   = "add subset";
    public static final String MSG_ADD_AXIS_ITERATOR            = "over: add axis iterator";  
    public static final String MSG_ALL                          = "all";  
    public static final String MSG_ARCCOS                       = "arccos";  
    public static final String MSG_ARCSIN                       = "arcsin";  
    public static final String MSG_ARCTAN                       = "arctan";  
    public static final String MSG_AXIS                         = "axis";
    public static final String MSG_AXIS_COORDS                  = "axis coords";
    public static final String MSG_AXIS_ITERATOR                = "axisIterator";
    public static final String MSG_AXIS_LIST_COUNT              = "axis list count";
    public static final String MSG_AXIS_TYPE                    = "axisType";
    public static final String MSG_AND                          = "and";
    public static final String MSG_AS                           = "AS";
    public static final String MSG_AVG                          = "avg";  
    public static final String MSG_B                            = "b";
    public static final String MSG_BBOX_COORD_X23               = "BBOX Coordinates: X23";
    public static final String MSG_BINARY_OP                    = "binaryOp";
    public static final String MSG_BINARY                       = "binary";
    public static final String MSG_BIT                          = "bit";
    public static final String MSG_BITINDEX                     = "bitIndex";
    public static final String MSG_BINARY_OP_SUCCESS            = "binary Operation SUCCESS";
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
    public static final String MSG_BOOLEAN_SCALAR_EXPR          = "Boolean Scalar Expr SUCCESS";
    public static final String MSG_BOUNDING_BOX                 = "Bounding Box";
    public static final String MSG_BUILDING_METADATA            = "building metadata...";
    public static final String MSG_C                            = "c";
    public static final String MSG_CAST                         = "cast";
    public static final String MSG_CELL_DOMAIN                  = "cell domain";
    public static final String MSG_CELL_DOMAIN_EXTREMES         = "CellDomain extremes values: LOW:";
    public static final String MSG_CELL_DOMAIN_ELEMENT          = "CellDomainElement";
    public static final String MSG_CELLS                        = "cells";
    public static final String MSG_CHAR                         = "char";
    public static final String MSG_CHILD                        = "child";
    public static final String MSG_COMPLEX                      = "complex";
    public static final String MSG_COMPLEX_CONSTANT             = "complexConstant";
    public static final String MSG_COMPONENT                    = "component";
    public static final String MSG_CONST                        = "const";
    public static final String MSG_CONSTANT                     = "constant";
    public static final String MSG_CONDENSE                     = "condense";
    public static final String MSG_CONSTRUCT                    = "construct";
    public static final String MSG_CONVERTING_CURELY_AXIS       = "Converting {} axis {} interval to pixel coordinates ...";
    public static final String MSG_CONVERTING_CURELY_AXIS_INDX  = "Converting {} axis {} interval to pixel indices ...";
    public static final String MSG_COORD                        = "coord";
    public static final String MSG_COORDS                       = "coords";
    public static final String MSG_COS                          = "cos";
    public static final String MSG_COSH                         = "cosh";
    public static final String MSG_COVERAGE                     = "coverage";
    public static final String MSG_COVERAGE_EXPR                = "coverage expression";
    public static final String MSG_COVERAGE_NAME                = "coverageName";
    public static final String MSG_COVERAGE_ITERATOR            = "coverageIterator";
    public static final String MSG_COVERAGE_REFERENCE           = "coverage reference";
    public static final String MSG_COUNT                        = "count";
    public static final String MSG_CREATE                       = "create";
    public static final String MSG_CREATING_INTERVALEXPR        = "Creating IntervalExpr of two indexes";
    public static final String MSG_CRS                          = "crs";
    public static final String MSG_CRS_C                        = "CRS";
    public static final String MSG_CRS_COORD_ON_AXIS_X          = "CRS Coordinates on axis X: U23";
    public static final String MSG_CRS_COORD_ON_AXIS_Y          = "CRS Coordinates on axis Y:  V23";
    public static final String MSG_CRS_METADATA                 = "crs metadata";
    public static final String MSG_CRS_SET                      = "crsSet";
    public static final String MSG_CRS_SET_CAMEL                = "CrsSet";
    public static final String MSG_CRS_TRANSFORM                = "crsTransform";
    public static final String MSG_CRS_TRANSFORM_EXPR           = "crsTransform expression not implemented.";
    public static final String MSG_CUBIC                        = "cubic";
    public static final String MSG_DIV                          = "/";
    public static final String MSG_DIV_S                        = "div";
    public static final String MSG_DOMAIN_INTERVAL_ELEMENT_NOT  = "<DimensionIntervalElement Not Converted to RasQL>";
    public static final String MSG_DOMAIN                       = "domain";
    public static final String MSG_DOMAIN_ELEMENT_NAME          = "Domain Element { Name: '";
    public static final String MSG_DOMAIN_EXTREMES_COORD        = "Domain extremes coordinates";
    public static final String MSG_DOMAIN_METADATA              = "domain metadata";
    public static final String MSG_DOMAIN_METADATA_CAMEL        = "DomainMetadata";
    public static final String MSG_DOUBLE                       = "double";
    public static final String MSG_DYNAMIC_TYPE                 = "dynamic_type";
    public static final String MSG_ELEMENST                     = "elements";
    public static final String MSG_ENCODE                       = "encode";
    public static final String MSG_EQUAL                        = "=";
    public static final String MSG_EQUALS                       = "equals";
    public static final String MSG_EXP                          = "exp";
    public static final String MSG_EXTEND                       = "extend";
    public static final String MSG_EXTRA_PARAMETERS             = "extraParameters";
    public static final String MSG_EXTRA_PARAMS                 = "extra params";
    public static final String MSG_FALSE                        = "false";
    public static final String MSG_FIELD                        = "field";
    public static final String MSG_FIELD_INTERPOLATION          = "field interpolation";
    public static final String MSG_FIELD_SELECT                 = "fieldSelect";
    public static final String MSG_FLOAT                        = "float";
    public static final String MSG_FOR_COVERAGE                 = "for coverage";
    public static final String MSG_FORMAT                       = "format";
    public static final String MSG_FOUND_BITINDEX               = "Found bitIndex";
    public static final String MSG_FOUND_CRS                    = "found CRS";
    public static final String MSG_FOUND_FIELD_NAME             = "found field name";
    public static final String MSG_FOUND_SIMPLE_COVERAGE_DEF    = "Found simple coverage definition";
    public static final String MSG_FROM                         = "from";
    public static final String MSG_FULL                         = "full";
    public static final String MSG_GRID_COVERAGE                = "GridCoverage";
    public static final String MSG_HALF                         = "half";
    public static final String MSG_HAS_BEEN_RENAMED             = "has been renamed into";
    public static final String MSG_HIGH_U                       = "HIGH";
    public static final String MSG_I                            = "i";
    public static final String MSG_ID                           = "ID";
    public static final String MSG_ID_LOWERCASE                 = "id";
    public static final String MSG_IDENTIFIER                   = "identifier";
    public static final String MSG_IMAGE_CRS                    = "imageCRS";
    public static final String MSG_IMAGE_CRS2                   = "imageCrs";
    public static final String MSG_IMAGE_CRSDOMAIN              = "imageCrsDomain";
    public static final String MSG_INDEX_EXPR                   = "IndexExpr";
    public static final String MSG_INDUCED_OP_SUCCESS           = "induced Operation SUCCESS";
    public static final String MSG_INT                          = "int";
    public static final String MSG_INTERPOLATION_DEFAULT        = "interpolationDefault";
    public static final String MSG_INTERPOLATION_METHOD         = "interpolationMethod";
    public static final String MSG_INTERPOLATION_SET            = "interpolationSet";
    public static final String MSG_ITERATOR                     = "iterator";
    public static final String MSG_ITERATORVAR                  = "iteratorVar";
    public static final String MSG_ITERATOR_VARIABLE            = "iterator variable";
    public static final String MSG_LINEAR                       = "linear";
    public static final String MSG_LN                           = "ln";
    public static final String MSG_LOG                          = "log";
    public static final String MSG_LONG                         = "long";
    public static final String MSG_LOWER_BOUND                  = "lowerBound";
    public static final String MSG_NOT_EQUALS                   = "notEqual";
    public static final String MSG_MATCHED_BOOLEAN_SCALAR_EXPR  = "  matched boolean scalar expression.";
    public static final String MSG_MATCHED_METADATA_SCALAR_EXPR = "  matched metadata scalar expression.";
    public static final String MSG_MATCHED_NUMERIC_SCALAR_EXPR  = "  matched numeric scalar expression.";
    public static final String MSG_MATCHED_REDUCE_SCALAR_EXPR   = "  matched reduce scalar expression.";
    public static final String MSG_MATCHED_STRING_SCALAR_EXPR   = "  matched string scalar expression.";
    public static final String MSG_MATCHED_SCALAR_EXPR          = "Matched scalar expression.";
    public static final String MSG_MATCHED_SUBSET_OP            = "matched subset operation.";
    public static final String MSG_MATCHED_INDUCED_COVERAGE     = "matched induced coverage expression operation.";
    public static final String MSG_MATCHED_SET_METADATA         = "matched set metadata operation.";
    public static final String MSG_MATCHING_AXIS_NAME           = "matching axis name";
    public static final String MSG_MATCHING_CRS                 = "matching crs";
    public static final String MSG_MATCHING_VAR_REF             = "Matched variable reference: ";
    public static final String MSG_MARRAY                       = "marray";
    public static final String MSG_MAX                          = "max";
    public static final String MSG_METHOD_NOT_IMPL              = "Method not implemented";
    public static final String MSG_MIME                         = "mime";
    public static final String MSG_MIN                          = "min";
    public static final String MSG_MINUS                        = "-";
    public static final String MSG_MINUS_S                      = "minus";
    public static final String MSG_MULT                         = "mult";
    public static final String MSG_NAME                         = "name";
    public static final String MSG_NEAREST                      = "nearest";
    public static final String MSG_NONE                         = "none";
    public static final String MSG_NULL                         = "null";
    public static final String MSG_NULL_RESISTANCE              = "nullResistance";
    public static final String MSG_NULL_SET                     = "nullSet";
    public static final String MSG_NUMLOW                       = "NumLow";
    public static final String MSG_NUMHI                        = "NumHi";
    public static final String MSG_NUM                          = "num";
    public static final String MSG_NUMERIC_ABS                  = "numericAbs";
    public static final String MSG_NUMERIC_ADD                  = "numericAdd";
    public static final String MSG_NUMERIC_CONSTANT             = "numericConstant";
    public static final String MSG_NUMERIC_DIV                  = "numericDiv";
    public static final String MSG_NUMERIC_MINUS                = "numericMinus";
    public static final String MSG_NUMERIC_MULT                 = "numericMult";
    public static final String MSG_NUMBER_OF_DIMENSIONS         = "number of dimensions";
    public static final String MSG_NUMERIC_SCALAR_EXPR          = "NumericScalarExpr";
    public static final String MSG_NUMERIC_SCALAR_EXPR_CONDENSE = "NumericScalarExpr condense";
    public static final String MSG_NUMERIC_SQRT                 = "numericSqrt";
    public static final String MSG_NUMERIC_UNARY_MINUS          = "numericUnaryMinus";
    public static final String MSG_LESS_THAN                    = "lessThan";
    public static final String MSG_LESS_OR_EQUAL                = "lessOrEqual";
    public static final String MSG_GREATER_THAN                 = "greaterThan";
    public static final String MSG_GREATER_OR_EQUAL             = "greaterOrEqual";
    public static final String MSG_IM                           = "im";
    public static final String MSG_IN                           = "in";
    public static final String MSG_NOT                          = "not";
    public static final String MSG_OFFSETS                      = "Offsets";
    public static final String MSG_OGC                          = "ogc";
    public static final String MSG_OR                           = "or";
    public static final String MSG_OP1                          = "op1";
    public static final String MSG_OP2                          = "op2";
    public static final String MSG_OPERATOR                     = "Operator";
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
    public static final String MSG_PLUS_S                       = "plus";
    public static final String MSG_POW                          = "pow";
    public static final String MSG_QUERY                        = "query";
    public static final String MSG_XOR                          = "xor";
    public static final String MSG_PARSED_CONST_LIST            = "parsed constant list with";
    public static final String MSG_PIXEL_COORDINATES_X01        = "Pixel Coordinates: X01";
    public static final String MSG_PIXEL_COORD_ON_AXIS_X        = "Pixel Coordinates on axis X: U01";
    public static final String MSG_PIXEL_COORD_ON_AXIS_Y        = "Pixel Coordinates on axis Y: V01";
    public static final String MSG_PLUS                         = "+";
    public static final String MSG_PLUS_I                       = "+i";
    public static final String MSG_PROCESS_COVERAGE_REQUEST     = "ProcessCoveragesRequest";
    public static final String MSG_QUADRATIC                    = "quadratic";
    public static final String MSG_RANGE_CONSTRUCTOR            = "rangeConstructor";
    public static final String MSG_RANGE_RANGE_FILED_TYPE       = "  range field type: ";
    public static final String MSG_RANGE_ELEMENT                = "Range Element";
    public static final String MSG_RASSERVICE                   = "RASSERVICE";
    public static final String MSG_RAW                          = "raw";
    public static final String MSG_REDUCE                       = "reduce";
    public static final String MSG_REDUCE_OPERATION             = "  reduce operation: ";
    public static final String MSG_REFERENCE_TO                 = "reference to";
    public static final String MSG_RETURNING_ENCODE_EXPR        = "Returning EncodeExpression";
    public static final String MSG_RE                           = "re";
    public static final String MSG_SCALE                        = "scale";
    public static final String MSG_SCALAR_EXPR                  = "scalarExpr";
    public static final String MSG_SELECT                       = "select";
    public static final String MSG_SERVLET_HTMLPATH                 = "/templates/wcps-servlet.html";
    public static final String MSG_SET_IDENTIFIER               = "setIdentifier";
    public static final String MSG_SET_CRSSET                   = "setCrsSet";
    public static final String MSG_SET_INTERPOLATION_DEFAULT    = "setInterpolationDefault";
    public static final String MSG_SET_INTERPOLATION_SET        = "setInterpolationSet";
    public static final String MSG_SET_NULL_SET                 = "setNullSet";
    public static final String MSG_SHORT                        = "short";
    public static final String MSG_SIN                          = "sin";
    public static final String MSG_SINH                         = "sinh";
    public static final String MSG_SLICE                        = "slice";
    public static final String MSG_SLICE_AT_AXIS_ID             = "slice at axis id";
    public static final String MSG_SLICING_POSITION             = "slicingPosition";
    public static final String MSG_SLICING_POSITION2             = "slicing position";
    public static final String MSG_SLICE_POSITION               = "slice position";
    public static final String MSG_SOME                         = "some";
    public static final String MSG_SQRT                         = "sqrt";
    public static final String MSG_STAR                         = "*";
    public static final String MSG_STAR_LINE                    = "***********************************************";
    public static final String MSG_STORE                        = "store";
    public static final String MSG_STRLOW                       = "StrLow";
    public static final String MSG_STRHI                        = "StrHi";
    public static final String MSG_STRING_CONSTANT               = "stringConstant";
    public static final String MSG_STRING_IDENTIFIER             = "stringIdentifier";
    public static final String MSG_SRS_NAME                     = "srsName";
    public static final String MSG_SCALARS                      = "scalars";
    public static final String MSG_TAN                          = "tan";
    public static final String MSG_TANH                         = "tanh";
    public static final String MSG_TEXT                         = "text";
    public static final String MSG_TEXT_PLAIN                   = "text/plain";
    public static final String MSG_TEMP                         = "temp";
    public static final String MSG_THE_CURRENT_NODE             = "The current node is";
    public static final String MSG_TRANSLATION                  = "translation";
    public static final String MSG_TRIM                         = "trim";
    public static final String MSG_TYPE                         = "type";
    public static final String MSG_TYPE_CAMEL                   = "Type";
    public static final String MSG_TRANSFORMED_COORDS_INDX      = "Transformed coords indices";
    public static final String MSG_TRANSFORMED_REBOUNDED_COORDS = "Transformed rebounded coords indices";
    public static final String MSG_TRUE                         = "true";
    public static final String MSG_TWO_INDEXES                  = "two indexes";
    public static final String MSG_UNARY_OP                     = "unaryOp";
    public static final String MSG_UNARY_OPERATOR               = "Unary Operator";
    public static final String MSG_UNARY_PLUS                   = "unaryPlus";
    public static final String MSG_UNARY_MINUS                  = "unaryMinus";
    public static final String MSG_UNSIGNED_INT                 = "unsigned int";
    public static final String MSG_UNSIGNED_LONG                = "unsigned long";
    public static final String MSG_UNSIGNED_SHORT               = "unsigned short";
    public static final String MSG_UPPER_BOUND                  = "upperBound";
    public static final String MSG_USING                        = "using";
    public static final String MSG_USING_NATIVE_CRS             = "Using native CRS";
    public static final String MSG_VALUE                        = "value";
    public static final String MSG_VALUES                        = "values";
    public static final String MSG_VALUE_LISt                   = "value list";    
    public static final String MSG_VAR                          = "var";
    public static final String MSG_VARIABLE                     = "variable";
    public static final String MSG_VARIABLE_REF                 = "variableRef";
    public static final String MSG_WCPS                         = "wcps";
    public static final String MSG_WCPS_COVERAGES               = "wcpsProcessCoverages.xsd";
    public static final String MSG_WCPS_LOADING_PARSING_XML     = "WCPS: Loading and parsing XML Schema ...";
    public static final String MSG_WCPS_FINISHED_LOADING_SCHEMA = "WCPS: Finished loading the schema.";
    public static final String MSG_WCPS_PROCESS_COVERAGE_XSD    = "/xml/ogc/wcps/1.0.0/wcpsProcessCoverages.xsd";
    public static final String MSG_WHERE                        = "where";
    public static final String MSG_WGS84                        = "WGS84";
    public static final String MSG_X                            = "X";
    public static final String MSG_XML                          = "xml";
    public static final String MSG_XML_HEADER                   = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
    public static final String MSG_XMLNS                        = "xmlns=\"http://www.opengis.net/wcps/1.0\" service=\"WCPS\" version=\"1.0.0\"";
    public static final String MSG_XML_SYNTAX                   = "xmlSyntax";
    public static final String MSG_Y                            = "Y";
    public static final String MSG_Y01                          = "Y01";
    public static final String MSG_Y23                          = "Y23";
    public static final String MSG_SWITCH                       = "switch";
    public static final String MSG_CASE                         = "case";
    public static final String MSG_CONDITION                    = "condition";
    public static final String MSG_RESULT                       = "result";
    public static final String MSG_DEFAULT                      = "default";
    public static final String MSG_WHEN                         = "when";
    public static final String MSG_THEN                         = "then";
    public static final String MSG_ELSE                         = "else";
    public static final String MSG_END                          = "end";
       
    /**
     * DBParam messages  
     */
    public static final String DBPARAM_SETTING_PROPERTIES           = "settings.properties";
    public static final String DBPARAM_METADATA_DRIVER              = "metadata_driver";
    public static final String DBPARAM_METADATA_PASS                = "metadata_pass";
    public static final String DBPARAM_METADATA_URL                 = "metadata_url";
    public static final String DBPARAM_METADATA_USER                = "metadata_user";
    
    /**
     * Error messages  
     */
    public static final String ERRTXT_ARGUMENT_HIGH_IS_LOWER_P1    = "Argument \\\"high\\\" is lower than \\\"low\\\"";
    public static final String ERRTXT_ARGUMENT_HIGH_IS_LOWER_P2    = "(rasql error 389 would be raised: \"in case of fixed bounds, the upper one can not be smaller than the lower one\")";
    public static final String ERRTXT_AXIS_NAME_NOT_FOUND          = "Axis name not found";
    public static final String ERRTXT_CANNOT_BE_LARGER             = "cannot be larger than upper bound";
    public static final String ERRTXT_CANNOT_BUILD_COVERAGE        = "Cannot build coverage metadata";
    public static final String ERRTXT_COULD_BUILD_CONST_COVERAGE   = "Could not build constant coverage metadata.";
    public static final String ERRTXT_COULD_NOT_EVAL_RASDAMAN_Q_P1 = "Could not evaluate rasdaman query: '";
    public static final String ERRTXT_COULD_NOT_EVAL_RASDAMAN_Q_P2 = "'\n Cause: ";
    public static final String ERRTXT_COULD_NOT_FIND_ANY_VAR_REF   = "Could not find any variable reference";
    public static final String ERRTXT_COULD_NOT_FIND_AXIS          = "Could not find an axis node";
    public static final String ERRTXT_COULD_NOT_FIND_COVERAGE_P1   = "Could not find the \"";
    public static final String ERRTXT_COULD_NOT_FIND_COVERAGE_P2   = "\" axis for coverage";
    public static final String ERRTXT_COULD_NOT_FIND_SRSNAME       = "Could not find a 'srsName' node.";
    public static final String ERRTXT_COULD_NOT_FIND_NODE_QUERY    = "Could not find node <query>: ";
    public static final String ERRTXT_COULD_NOT_FIND_THE_X         = "Could not find the X or Y axis for coverage";
    public static final String ERRTXT_COULD_NOT_MATCH_COV_EXPR     = "  Could not match CoverageExpr inside RangeExpr. Next node: ";
    public static final String ERRTXT_COULD_NOT_PARSE_COMPLEX_CONST= "Could not parse Complex Constant";
    public static final String ERRTXT_COULD_NOT_PARSE_FLOAT        = "Could not parse float or integer";
    public static final String ERRTXT_COULD_NOT_UNDERSTAND_CONST   = "Could not understand constant: ";
    public static final String ERRTXT_DOMAIN_ELEMENT_DONNOT_MATCH  = "Domain element names don't match";
    public static final String ERRTXT_DOMAIN_NAME_NOT_FOUND        = "Domain name not found";
    public static final String ERRTXT_COULD_NOT_PARSE_REAL_PART    = "number for real part of complex number";
    public static final String ERRTXT_COULD_NOT_PARSE_IMG_PART     = "number for imaginary part of complex number";
    public static final String ERRTXT_CONST_DIMS_DOESNOT_MATCH     = "The number of constants in the list do not "
            + "match the dimensions specified.";
    public static final String ERRTXT_COVERAGEEXPRTYPE_PASING_ERR  = "CoverageExprType parsing error";
    public static final String ERRTXT_ERROR                        = "error";
    public static final String ERRTXT_ERROR_UNEXPECTED_NODE        = "Error, unexpected node: ";
    public static final String ERRTXT_ERROR_WHILE_LOADING_DOC      = "Error while loading the document builder interface.";
    public static final String ERRTXT_ERROR_WHILE_TRANSFORMING     = "Error while transforming geo-coordinates to pixel coordinates. The metadata is probably not valid.";
    public static final String ERRTXT_EXPECTED_AXIS_NODE           = "Expected Axis node before CRS.";
    public static final String ERRTXT_EXPECTED_AXIS_NODE_LOWERB    = "Expected <axis> node before <lowerBound>.";
    public static final String ERRTXT_EXPECTED_AXIS_NODE_UPPERB    = "Expected <lowerBound> node before <upperBound>.";
    public static final String ERRTXT_EXPECTED_AXIS_NODE_SLICINGP  = "Expected <axis> node before <slicingPosition>.";
    public static final String ERRTXT_EXPECTED_COVERAGE_NODE_GOT   = "expected coverage node, got";
    public static final String ERRTXT_FAILED_PARSE_NUM_EXPR        = "Failed to parse a numeric expression pair.";
    public static final String ERRTXT_FAILED_MATCH_VAR_REF         = "Failed to match variable reference: ";
    public static final String ERRTXT_FAILED_TO_MATCH_SUBSET       = "failed to match SubsetOperation";
    public static final String ERRTXT_FIELDNAME_TYPE_PARSING_ERR   = "FieldNameType parsing error.";
    public static final String ERRTXT_HIGH_VALUES_DONOT_MATCH      = "High values don't match";
    public static final String ERRTXT_LOW_VALUES_DONOT_MATCH       = "Low values don't match";
    public static final String ERRTXT_INCOMPATIBLE_COVERAGES       = "Incompatible coverages within the same iterator";
    public static final String ERRTXT_INTERNAL_ERROR_THIS          = "Internal error: this should have been a variable name";
    public static final String ERRTXT_INVALID_BOUNDING_BOX         = "Invalid bounding box: null element encountered.";
    public static final String ERRTXT_INVALID_CAST_FROM_Part1      = "Invalid cast from";
    public static final String ERRTXT_INVALID_CAST_FROM_PART2      = "XML node to CoverageIterator node";
    public static final String ERRTXT_INVALID_CELL_DOMAIN          = "Invalid cell domain element: Bounds may not be null";
    public static final String ERRTXT_INVALID_CELL_DOMAIN_LOWER    = "Invalid cell domain element: Lower bound";
    public static final String ERRTXT_INVALID_COVERAGE_EXPR        = "Invalid coverage Expression, next node";
    public static final String ERRTXT_INVALID_DOMAIN_ELEMENT_NULL  = "Invalid domain element: Element name and type cannot be null";
    public static final String ERRTXT_INVALID_DOMAIN_ELEMENT_EMPTY = "Invalid domain element: Element name cannot be empty";
    public static final String ERRTXT_INVALID_DOMAIN_ELEMENT_TYP_P1= "Invalid domain element: Invalid element type: ";
    public static final String ERRTXT_INVALID_DOMAIN_ELEMENT_TYP_P2= ". Allowed element types are: ";
    public static final String ERRTXT_INVALID_DOM_LOWER_INT_BOUND  = "Invalid domain element: Lower integer bound cannot be larger than upper integer bound";
    public static final String ERRTXT_INVALID_DOM_STRING_BOUND     = "Invalid domain element: String bounds cannot be empty";
    public static final String ERRTXT_INVALID_DOM_INT_BOUND_BOTH   = "Invalid domain element: Integer bounds must both be non-null if string bounds are null, and vice versa at ";
    public static final String ERRTXT_INVALID_DOM_SPATIAL_AXIS     = "Invalid domain element: A spatial axis must have integer extent";
    public static final String ERRTXT_INVALID_DOM_T_AXIS           = "Invalid domain element: A \\\"t\\\" axis must have integer extent and optionally, string extent";
    public static final String ERRTXT_INVALID_INDUCED_COV_EXPR     = "Invalid induced coverage expression, next node";
    public static final String ERRTXT_INVALID_INTERPOLATION_MET_P1 = "Invalid interpolation method: ";
    public static final String ERRTXT_INVALID_INTERPOLATION_MET_P2 = " is not a legal interpolation type";
    public static final String ERRTXT_INVALID_INTERPOLATION_MET_P3 = " is not a legal null resistance";
    public static final String ERRTXT_INVALID_METADATA             = "Invalid metadata while cloning CellDomainElement. This is a software bug in WCPS.";
    public static final String ERRTXT_INVALID_METADATA_CLONING_INT = "Invalid metadata while cloning InterpolationMethod. This is a software bug in WCPS.";
    public static final String ERRTXT_INVALID_METADATA_CLONING_RAN = "Invalid metadata while cloning RangeElement. This is a software bug in WCPS.";
    public static final String ERRTXT_INVALID_NUMBER_AS_BITINDEX   = "Invalid Number as bitIndex";
    public static final String ERRTXT_INVALID_FFSET                = "Invalid offsets: null element encountered.";
    public static final String ERRTXT_INVALID_OFFSET               = "Invalid offsets: null element encountered.";
    public static final String ERRTXT_INVALID_CRS                  = "Invalid CRS name: null element encountered";
    public static final String ERRTXT_INVALID_METADAT_WHILE_CLONE  = "Invalid metadata while cloning DomainElement. This is a software bug in WCPS.";
    public static final String ERRTXT_INVALID_RANGE_TYPE_P1        = "Invalid range element: ";
    public static final String ERRTXT_INVALID_RANGE_TYPE_P2        = "Element name and type cannot be null.";
    public static final String ERRTXT_INVALID_RANGE_TYPE_P3        = "Element name cannot be empty.";
    public static final String ERRTXT_INVALID_RANGE_TYPE_P4        = "Invalid element type:";
    public static final String ERRTXT_INVALID_REDUCE_SCALAR_EXPR   = "invalid ReduceScalarExprType node: ";
    public static final String ERRTXT_INVALID_WGS84_BBOX           = "Invalid WGS 84 bounding box: null element encountered.";
    public static final String ERRTXT_IS_NOT_GEOREFERENCED         = "is not georeferenced.";
    public static final String ERRTXT_IS_NOT_RECOGNIZED            = "is not recognized.";
    public static final String ERRTXT_METHOD_NOT_IMPL              = "Method not implemented. pcExecute";
    public static final String ERRTXT_NO_INTERPOLATION             = "Currently, our implementation does not allow specifying interpolation methods or "
            + "null resistances for the scale operation.";
    public static final String ERRTXT_NO_METADATA_NODE                  = "No metadata node: ";
    public static final String ERRTXT_NOT_DEFINED                  = "not defined";
    public static final String ERRTXT_NOT_INT_FLOAT_COMPLEX        = "is not an integer, float, or complex constant..";
    public static final String ERRTXT_NOT_VAR_REF_FOUND            = "no variable reference found";
    public static final String ERRTXT_NUMBER_DIM_DOES_NOT_MATCH    = "The number of dimensions doesn't match";
    public static final String ERRTXT_RANGE_FIELD_TYPE             = "RangeFieldType parsing error.";
    public static final String ERRTXT_THIS_WAS_NO_DIM              = "This was no Dimension Interval ELement";
    public static final String ERRTXT_UNEXPECTED_BINARY            = "Unexpected binary operation";
    public static final String ERRTXT_UNEXPECTED_BINARY_EXPR_NODE  = "Unexpected Binary Expression node";
    public static final String ERRTXT_UNEXPETCTED_NODE             = "Unexpected node";
    public static final String ERRTXT_UNEXPECTED_NULL_NODE         = "Unexpected null node";
    public static final String ERRTXT_UNEXPECTED_NUM_SCALAR_EXPR   = "Unexpected Numeric Scalar Expression node : ";
    public static final String ERRTXT_UNKNOWN_CONDENSE_OP          = "Unknown condense operation";
    public static final String ERRTXT_UNKNOWN_COVERAGE             = "unknown coverage";
    public static final String ERRTXT_UNKNOWN_METADATAEXPR         = "Unknown MetadataExpr operation";
    public static final String ERRTXT_UNKNOWN_NODE                 = "Unknown node in AxisIterator";
    public static final String ERRTXT_UNKNOWN_NODE_EXTENDCOVERAGE  = "Unknown node for ExtendCoverage expression:";
    public static final String ERRTXT_UNKNOWN_NODE_CONST_LIST      = "Unknown node in constant list";
    public static final String ERRTXT_UNKNOWN_NODE_FOR_SCALE_COV   = "Unknown node for ScaleCoverageExpr expression:";
    public static final String ERRTXT_UNKNOWN_NODE_FOR_SLICE_COV   = "Unknown node for SliceCoverage expression";
    public static final String ERRTXT_UNKNOWN_NODE_FOR_TRIM_COV    = "Unknown node for TrimCoverage expression";
    public static final String ERRTXT_UNKNOWN_SET_METADATA_EXPR    = "Unknown SetMetadataExpr operation";
    public static final String ERRTXT_UNKNOWN_STRING_NODE_EXPR     = "Unknown String expr node";
    public static final String ERRTXT_UNKNOWN_UNARY_OP             = "Unknown unary operation";
    public static final String ERRTXT_SUBSET_OP_COV_EXPR           = "SubsetOperationCoverageExpr parsing error.";
    public static final String ERRTXT_THE_DOC_UNRECOG_NODE         = "The document contains an unrecognized node : ";
    public static final String ERRTXT_UNKNOWN_PROC_COMPLEX_CONST   = "Unknown node while processing complex constant";
    public static final String ERRTXT_MISSING_SWITCH_DEFAULT       = "The default branch of the switch is missing.";

    /**
     * Debug messages  
     */
    public static final String DEBUGTXT_CONVERTED_ABSTRACT_SYNTAX_QUERY= "Coverted the Abstract syntax query to an XML query:";
    public static final String DEBUGTXT_FINALMETADATA_RESULT           = "Final metadata result: ";
    public static final String DEBUGTXT_FINAL_RASQL_QUERY              = "Final RasQL query: ";
    public static final String DEBUGTXT_FOUND_ABSTRACT_SSYNTAX_QUERY   = "Found Abstract Syntax query: ";
    public static final String DEBUGTXT_FOUND_XML_SYTANX_QUERY         = "Found XML Syntax query";
    public static final String DEBUGTXT_PROCESSING_XML_REQUEST         = "Processing XML Request";
    public static final String DEBUGTXT_REQUESTED_SUBSETTING           = "[Transformed] requested subsettingCrs is '{}', should match now native CRS is '{}'";
    
    /**
     * Warning messages  
     */
    public static final String WARNTXT_NO_NATIVE_CRS_P1                = "No native CRS specified for axis";
    public static final String WARNTXT_NO_NATIVE_CRS_P2                = "assuming pixel coordinates.";
    
}
