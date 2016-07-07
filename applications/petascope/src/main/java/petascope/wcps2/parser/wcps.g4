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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
 /**
 * Grammar definition for the WCPS 2.0 standard draft.
 * The parser rules are written using the grammar provided by ANTLR4. Please refer to the documentation here <insert antlr4 docs>
 *
 * to understand the syntax and rules of it.
 *
 * If adding new grammar rules, please follow the next rules so that the grammar is kept consistent:
 *  1. Add any tokens in the wcpsLexer.g4 file, use the instructions there to define new ones.
 *  2. For each new rule added, please mention a valid expression that is targetted, and if possible a full wcps query
 *     that can be used to test it.
 *  3. Do not use inline java code, all the logic should go into the Java class called WcpsEvaluator. Please check the ANTLR4
 *     documentation if you need to understand the visitor pattern better.
 *
 * @author Alex Dumitru <alex@flanche.net>
 * @author Vlad Merticariu <vlad@flanche.net>
 */
grammar wcps;
import wcpsLexerTokens;

wcpsQuery : (forClauseList) (whereClause)? (returnClause)                                                               #WcpsQueryLabel;

/**
 * Example:
 * for $c in (cov1)
 * for $c2 in (cov2)
 *
 * Query:
 * for $c in (cov1)
 * for $c2 in (cov2)
 * return encode($c1 + $c2, "image/png")
 */
forClauseList: FOR (forClause) (COMMA forClause)*                                                                       #ForClauseListLabel;

/**
 * Example:
 * for $c in (cov1)
 */
forClause:  coverageVariableName IN
           (LEFT_PARENTHESIS)? COVERAGE_VARIABLE_NAME (COMMA COVERAGE_VARIABLE_NAME)* (RIGHT_PARENTHESIS)?                                      #ForClauseLabel;

/**
 * Example:
 *  where ($c.red > 100)
 *
 * Query:
 *  for $c in (cov1, cov2, cov3) where ($c.red + ($c.blue / 3) <= 163) return encode($c, "image/png")
 */
whereClause: WHERE (LEFT_PARENTHESIS)? booleanScalarExpression (RIGHT_PARENTHESIS)?
#WhereClauseLabel;

/**
 * Example
 *   return 42;
 * | return encode($c, "image/png")
 *
 * Query:
 *  for $c in (someCoverage) return encode($c * 2.5, "image/tiff")
 */
returnClause: RETURN (LEFT_PARENTHESIS)? processingExpression (RIGHT_PARENTHESIS)?
#ReturnClauseLabel;

/**
 * Example
 * $coverageName;
 *
 * Query:
 * for $coverageName in someCoverage return encode($coverageName, "csv");
 *
 */
coverageVariableName: COVERAGE_VARIABLE_NAME                                                                            #CoverageVariableNameLabel;


/**
 * Example:
 *   (1+1)
 * | encode($c, "image/png")
 */
processingExpression: scalarExpression
                    | encodedCoverageExpression
		    | scalarValueCoverageExpression;


/**
*  Only valid if the Rasql return value which is "Number" or "True/False" (e.g: 0, -3, 2.5345, t, f)
*
*  Example:
*
*     for $c in (mr) return c[i(0), j(0)] + avg(c) - 5 + 20 + 30 - 25 * 10 + count(c > 20)
*  or
*     for $c in (mr) return (c[i(0), j(0)] = 25 + 30 - 50)
*/
scalarValueCoverageExpression: (LEFT_PARENTHESIS)?  coverageExpression (RIGHT_PARENTHESIS)?
#scalarValueCoverageExpressionLabel;


/**
 * Example:
 *  See the examples for each of the subclasses.
 */
scalarExpression: booleanScalarExpression
                | numericalScalarExpression
                | stringScalarExpression
                | getComponentExpression
                | starExpression;

/**
 *  Example:
 *   NOT(true)
 * | 1 >= 2
 * | "stringA" = "stringB"
 * Query:
 *   for $c in (someCoverage) return (not(TRUE))
 * | for $c in (someCoverage) return ( 1+3 > 2-1 )
 */
booleanScalarExpression: reduceBooleanExpression                                                                        #BooleanReduceExpression
                       | booleanConstant                                                                                #BooleanConstantLabel
                       | booleanUnaryOperator LEFT_PARENTHESIS? booleanScalarExpression RIGHT_PARENTHESIS?              #BooleanUnaryScalarLabel
                       | booleanScalarExpression booleanOperator booleanScalarExpression                                #BooleanBinaryScalarLabel
                       | numericalScalarExpression numericalComparissonOperator  numericalScalarExpression              #BooleanNumericalComparisonScalarLabel
                       | reduceBooleanExpression                                                                        #BooleanReduceExpression
                       | stringScalarExpression stringOperator stringScalarExpression                                   #BooleanStringComparisonScalar;

booleanUnaryOperator: NOT;

booleanConstant: TRUE | FALSE;

booleanOperator: AND | XOR | OR;

numericalComparissonOperator: GREATER_THAN | GREATER_OR_EQUAL_THAN | LOWER_THAN | LOWER_OR_EQUAL_THAN | EQUAL | NOT_EQUAL;

stringOperator: EQUAL | NOT_EQUAL;

stringScalarExpression: STRING_LITERAL                                                                                  #StringScalarExpressionLabel;

starExpression: MULTIPLICATION                                                                                          #StarExpressionLabel;


/**
Not as booleanScalarExpression need both sides of boolean expression is scalar.
Boolean switch case allow both cases are coverage expression.
e.g:
for c in (rgb) return encode(switch case c > 1000 return {red: c.0, green: c.1, blue: c.2}, "png")
*/

/*
 (coverageExpression booleanOperator coverageExpression)
 e.g: (c > 2), (c > avg(c)), ( 3 = avg(c))
*/
booleanSwitchCaseCoverageExpression: (LEFT_PARENTHESIS)* coverageExpression (RIGHT_PARENTHESIS)*
						numericalComparissonOperator
				     (LEFT_PARENTHESIS)* coverageExpression (RIGHT_PARENTHESIS)*;

/*
 Combine multiple booleanSwitchCaseCoverageExpression
 e.g: (c > 2) and ( (c > 5 ) or ( 2 > 0))
*/
booleanSwitchCaseCombinedExpression:  booleanSwitchCaseCoverageExpression booleanOperator booleanSwitchCaseCoverageExpression
				    | booleanSwitchCaseCoverageExpression
				    | booleanSwitchCaseCombinedExpression booleanOperator booleanSwitchCaseCombinedExpression;

/**
 * Example:
 *   1 + 2
 * | avg(a) / avg(b)
 *
 * Query:
 * for c in (someCoverage) return ((avg(a) / avg(b) * 9/5) + 32)
 */
numericalScalarExpression: numericalUnaryOperation LEFT_PARENTHESIS numericalScalarExpression RIGHT_PARENTHESIS         #NumericalUnaryScalarExpressionLabel
                         | trigonometricOperator LEFT_PARENTHESIS numericalScalarExpression RIGHT_PARENTHESIS           #NumericalTrigonometricScalarExpressionLabel
                         | numericalScalarExpression numericalOperator numericalScalarExpression                        #NumericalBinaryScalarExpressionLabel
                         | condenseExpression                                                                           #NumericalCondenseExpressionLabel
                         | (MINUS)? REAL_NUMBER_CONSTANT      
#NumericalRealNumberExpressionLabel
                         | NAN_NUMBER_CONSTANT                                                                         #NumericalNanNumberExpressionLabel
                         | complexNumberConstant                                                                        #NumericalComplexNumberConstant
                         ;
/**
 * Example:
 *  (2,5)  //the equivalent of 2 + 5i
 */
complexNumberConstant: LEFT_PARENTHESIS (MINUS)? REAL_NUMBER_CONSTANT COMMA (MINUS)? REAL_NUMBER_CONSTANT RIGHT_PARENTHESIS               #ComplexNumberConstantLabel;
numericalOperator: PLUS | MINUS | MULTIPLICATION | DIVISION;
numericalUnaryOperation: ABSOLUTE_VALUE | SQUARE_ROOT |  REAL_PART | IMAGINARY_PART | ROUND | MINUS | PLUS;
trigonometricOperator: SIN | COS | TAN | SINH | COSH | TANH | ARCSIN | ARCCOS | ARCTAN;



/**
 * Example:
 *  See the rules below;
 */
getComponentExpression: coverageIdentifierExpression
          	      | coverageCrsSetExpression
		      | domainExpression
		      | imageCrsDomainExpression
		      | imageCrsDomainByDimensionExpression
		      | imageCrsExpression
                      | describeCoverageExpression;

/**
identifier()
Return the coverage name from coverage variable name

for $c in (someCov) return identifier($c)
return someCov
*/
coverageIdentifierExpression: IDENTIFIER LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
#CoverageIdentifierExpressionLabel;


/**
crrSet()
Return the list of dimensions and their supported CRSs (e.g: time Crs/geo-referenced CRS and GridCRS)

for $c in (eobstest) return crsset($c)
return t:http://.../Temporal?epoch="1950-01-01T00:00:00"&uom="d" http://.../Index3D,
Long:http://.../4326 http://.../Index3D,
Lat:http://.../4326 http://.../Index3D
*/
coverageCrsSetExpression: CRSSET LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS                                        #CoverageCrsSetExpressionLabel;

/*
domain()
The domain of coverage with the specific axis and its CRS (geo-referenced CRS or grid CRS)

for $c in (eobstest) return domain(c, Lat, "http://.../Index2D")
return (-25:75)
*/
domainExpression: DOMAIN LEFT_PARENTHESIS coverageExpression COMMA axisName COMMA crsName RIGHT_PARENTHESIS
#DomainExpressionLabel;


/*
imageCrsdomain($c, $axisName)
The domain of given axis in the coverage in grid interval (used with coverage iterator)
e.g: $px x(imageCrsdomain(c[Lat(0:20)], Lat)

for $c in (eobstest) return imageCrsdomain(c[Lat(0:20), Lat)
return (111:151)
*/
imageCrsDomainByDimensionExpression: IMAGECRSDOMAIN LEFT_PARENTHESIS coverageExpression COMMA axisName RIGHT_PARENTHESIS
#imageCrsDomainByDimensionExpressionLabel;


/*
imageCrsdomain($c)
The domain of each axis in the coverage in grid interval (used with scale, extend)
e.g: scale( c[i(120:150), j(120:150)],
            imageCrsDomain(c[i(10:20), j(10:20)]) )

for $c in (eobstest) return imageCrsdomain(c)
return (0:5, 0:100, 0:231) (time, long, lat) respectively
*/
imageCrsDomainExpression: IMAGECRSDOMAIN LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
#imageCrsDomainExpressionLabel;


/*
imageCrs()
The grid CRS which is depent on the axes of coverage

for $c in (mr) return imageCrs(c)
return "http://.../Index2D"
*/
imageCrsExpression: IMAGECRS LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
#imageCrsExpressionLabel;



/**
 * Example:
 * describeCoverage($c);
 *
 * Query:
 * for $c in (someCov) return describeCoverage($c)
 */
describeCoverageExpression: DESCRIBE_COVERAGE LEFT_PARENTHESIS coverageVariableName RIGHT_PARENTHESIS                   #DescribeCoverageExpressionLabel;

domainIntervals: domainExpression | imageCrsDomainExpression | imageCrsDomainByDimensionExpression;

/**
 * Example:
 *   encode($c, "image/tiff")
 * | encode($c, "image/png", "NODATA=0")
 */
encodedCoverageExpression: ENCODE LEFT_PARENTHESIS
                           coverageExpression COMMA /* FORMAT_NAME */ STRING_LITERAL (COMMA STRING_LITERAL)*
                           RIGHT_PARENTHESIS                                                                            #EncodedCoverageExpressionLabel;

/**
 * Example:
 *   decode("1,2,3,4", "csv")
 * Query:
 *   ?!
 */
decodeCoverageExpression: DECODE LEFT_PARENTHESIS
                          STRING_LITERAL COMMA /* FORMAT_NAME */ STRING_LITERAL (COMMA STRING_LITERAL)*
                          RIGHT_PARENTHESIS                                                                             #DecodedCoverageExpressionLabel;

/**
 * See subclauses
 */
coverageExpression: coverageExpression booleanOperator coverageExpression                                               #CoverageExpressionLogicLabel
		  | coverageConstructorExpression                                                                       #CoverageExpressionConstructorLabel
                  | coverageExpression coverageArithmeticOperator coverageExpression                                    #CoverageExpressionArithmeticLabel
                  | coverageExpression OVERLAY coverageExpression                                                       #CoverageExpressionOverlayLabel
                  | coverageExpression numericalComparissonOperator coverageExpression                                  #CoverageExpressionComparissonLabel
                  | coverageVariableName                                                                                #CoverageExpressionVariableNameLabel
                  | scalarExpression                                                                                    #CoverageExpressionScalarLabel
                  | coverageConstantExpression
#CoverageExpressionConstantLabel
                  | decodeCoverageExpression                                                                            #CoverageExpressionDecodeLabel
                  | coverageExpression LEFT_BRACKET dimensionPointList RIGHT_BRACKET                                    #CoverageExpressionShorthandSliceLabel
                  | SLICE LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionPointList RIGHT_BRACE
                          RIGHT_PARENTHESIS
#CoverageExpressionSliceLabel
                  | coverageExpression LEFT_BRACKET dimensionIntervalList RIGHT_BRACKET                                 #CoverageExpressionShorthandTrimLabel
                  | TRIM LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE
                    RIGHT_PARENTHESIS
#CoverageExpressionTrimCoverageLabel
                  | EXTEND LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE
                    RIGHT_PARENTHESIS
#CoverageExpressionExtendLabel
                  | EXTEND LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE domainIntervals RIGHT_BRACE
                    RIGHT_PARENTHESIS
+#CoverageExpressionExtendByDomainIntervalsLabel
                  | unaryArithmeticExpression                                                                           #CoverageExpressionUnaryArithmeticLabel
                  | trigonometricExpression                                                                             #CoverageExpressionTrigonometricLabel
                  | exponentialExpression                                                                               #CoverageExpressionExponentialLabel
		  | unaryPowerExpression
#CoverageExpressionPowerLabel
                  | unaryBooleanExpression                                                                              #CoverageExpressionUnaryBooleanLabel
                  | castExpression                                                                                      #CoverageExpressionCastLabel
                  | coverageExpression DOT fieldName                                                                    #CoverageExpressionRangeSubsettingLabel
                  | rangeConstructorExpression                                                                          #CoverageExpressionRangeConstructorLabel
                  | crsTransformExpression
#CoverageExpressionCrsTransformLabel
		  | switchCaseExpression
#CoverageExpressionSwitchCaseLabel
		  | domainIntervals
#CoverageExpressionDomainIntervalsLabel
                  | SCALE LEFT_PARENTHESIS
                    coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE (COMMA fieldInterpolationList)*
                    RIGHT_PARENTHESIS                                                                                   #CoverageExpressionScaleLabel
		  | SCALE LEFT_PARENTHESIS
		    coverageExpression COMMA LEFT_BRACE domainIntervals RIGHT_BRACE
                    RIGHT_PARENTHESIS
#CoverageExpressionScaleByDomainIntervalsLabel
                  | LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS                                               #CoverageExpressionCoverageLabel;


/**
 * Example:
 *   $c1 AND $c2
 * Query:
 *   for $c1 in cov1
     for $c2 in cov2
     return encode($c1 OR $c2, "csv")

          for c in (irr_cube_2) return encode (
             scale(
                slice(
                  (c[ansi("2008-01-01T00Z":"2008-01-01T12Z")]).b2,
                  ansi:"CRS:1"(0)
                ),
                { N:"CRS:1"(0:1), E:"CRS:1"(0:2) }
              )
          , "csv")

               for c in (irr_cube_2) return encode (
                     slice(
                       c[ansi("2008-01-01T00Z":"2008-01-01T12Z")].b2,
                       ansi:"CRS:1"(0)
                     )
               , "csv")
 */


coverageArithmeticOperator: PLUS | MULTIPLICATION | DIVISION | MINUS;

/**
 * Example:
 *   $c1 + $c2
 * Query:
 *   for $c1 in cov1
     for $c2 in cov2
     return encode($c1 * $c2, "csv")
 */




unaryArithmeticExpressionOperator: PLUS | MINUS | ABSOLUTE_VALUE | SQUARE_ROOT | REAL_PART | IMAGINARY_PART;

/**
 * Example
 *  -($coverage)
 *  sqrt($coverage)
 * Query:
 *   for $c in cov return encode(sqrt(abs($c)), "csv")
 */
unaryArithmeticExpression:  unaryArithmeticExpressionOperator  LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS    #UnaryCoverageArithmeticExpressionLabel;

/**
 * Example
 *  sin($coverage)
 * Query:
 *   for $c in cov return encode(sin(cos($c)), "csv")
 */
trigonometricExpression: trigonometricOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS                    #TrigonometricExpressionLabel;

exponentialExpressionOperator: EXP | LOG | LN;

/**
 * Example
 *  exp($coverage)
 * Query:
 *   for $c in cov return encode(ln(exp($c)), "csv")
 */
exponentialExpression: exponentialExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS              #ExponentialExpressionLabel;

/**
 *
 * Example
 *   pow($coverage, 3)
 * Query:
 *   for c in (mr) return encode( pow( c[i(100:110),j(100:110)], -0.5 ), "csv" )
*/
unaryPowerExpression: POWER LEFT_PARENTHESIS coverageExpression COMMA numericalScalarExpression RIGHT_PARENTHESIS
#UnaryPowerExpressionLabel;


/**
 * Example
 *  NOT($coverage)
 * Query:
 *   for $c in cov return encode(NOT(BIT($c, 2)), "csv")
 */
unaryBooleanExpression: NOT LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS                                       #NotUnaryBooleanExpressionLabel
                      | BIT LEFT_PARENTHESIS coverageExpression COMMA numericalScalarExpression RIGHT_PARENTHESIS       #BitUnaryBooleanExpressionLabel;


/**
 * We allow any value here so we do not pollute the parser with business logic.
 * The existence of the rangeType should be checked in the code, not here.
 */
rangeType: COVERAGE_VARIABLE_NAME (COVERAGE_VARIABLE_NAME)*;

/**
 * Example
 *  (boolean) $coverage
 * Query:
 *   for $c in cov return encode((char) $c, "csv")
 */
castExpression: LEFT_PARENTHESIS rangeType RIGHT_PARENTHESIS coverageExpression                                         #CastExpressionLabel;


fieldName: COVERAGE_VARIABLE_NAME | REAL_NUMBER_CONSTANT;

/**
 This is used for only range constructor not in switch case
 * Example
 *  struct {red: $c.blue; blue: $c.red; green: $c.green}
 * Query:
 *   for $c in cov return encode(struct {red: $c.blue; blue: $c.red; green: $c.green}, "csv")

 it is translated to:
      select  { c.red, c.green, c.blue } from COV as c
 */
rangeConstructorExpression: LEFT_BRACE
                              (fieldName COLON coverageExpression) (SEMICOLON fieldName COLON coverageExpression)*
                            RIGHT_BRACE                                                                                 #RangeConstructorExpressionLabel;

/**
 This is used in switch case which return range constructor
 e.g: for c in (mr) return encode(switch
      case c > 1000 return """"{red: 107; green:17; blue:68}""""
      default return {red: 150; green:103; blue:14, r1:20, r2:50}, "png")

 it is translated to:
       ((107) * {1c,0c,0c} + (17) * {0c,1c,0c} + (68) * {0c,0c,1c})
*/
rangeConstructorSwitchCaseExpression: LEFT_BRACE
                              (fieldName COLON coverageExpression) (SEMICOLON fieldName COLON coverageExpression)*
                            RIGHT_BRACE                                                                                 #RangeConstructorSwitchCaseExpressionLabel;



dimensionPointList: dimensionPointElement (COMMA dimensionPointElement)*                                                #DimensionPointListLabel;

dimensionPointElement: axisName (COLON crsName)? LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS                  #DimensionPointElementLabel;

dimensionIntervalList: dimensionIntervalElement (COMMA dimensionIntervalElement)*                                       #DimensionIntervalListLabel;

dimensionIntervalElement: axisName (COLON crsName)? LEFT_PARENTHESIS
                            coverageExpression COLON coverageExpression
                          RIGHT_PARENTHESIS                                                                             #TrimDimensionIntervalElementLabel
                        | axisName (COLON crsName)? LEFT_PARENTHESIS coverageExpression
                          RIGHT_PARENTHESIS                                                                             #SliceDimensionIntervalElementLabel;


/**
 * crsTransform (Use to project a coverage from CRS:A to CRS:B). Require coverage was geo-referenced, not grid.
 * and only project with 2D.
 * Syntax: crsTransform($COVERAGE_EXPRESSION, {AXIS1:"CRS", AXIS2:"CRS"}, {$INTERPOLATION}, $FORMAT_NAME)
 * $INTERPOLATION has syntax $RANGE_NAME($INTERPOLATION_TYPE, "$NODATA_VALUES"))
 *
 * Example:
 *
 * for c in (mean_summer_airtemp) return encode(crsTransform(c,
 * {Lat:"www.opengis.net/def/area/EPSG/0/4326", Long:"www.opengis.net/def/area/EPSG/0/4326"},
 * {b1(near,"0,1,2,3,NULL")}), "tiff")
 *
 * NOTE: if mean_summer_airtemp has multiple ranges (bands), e.g: b1, b2, b3 then b2 and b3 which are not
 * passed in $INTERPOLATION then will use default interpolation of Rasdaman and "NODATA=..." in encoding.
*/
crsTransformExpression: CRS_TRANSFORM LEFT_PARENTHESIS
                          coverageExpression COMMA dimensionCrsList COMMA fieldInterpolationList
                        RIGHT_PARENTHESIS                                                                               #CrsTransformExpressionLabel;


/*
 * e.g: { Lat:"http://localhost:8080/def/crs/EPSG/0/4326", Long:"http://localhost:8080/def/crs/EPSG/0/4326"}
*/
dimensionCrsList: LEFT_BRACE dimensionCrsElement (COMMA dimensionCrsElement)* RIGHT_BRACE                               #DimensionCrsListLabel;


/*
 * e.g: Lat:"http://localhost:8080/def/crs/EPSG/0/4326"
*/
dimensionCrsElement: axisName COLON crsName                                                                             #DimensionCrsElementLabel;


/*
 * e.g: interpolate red band with NODATA values is 1,2,3 or NULL
 * { red(near, "1,2,3,NULL") } OR {}
*/
fieldInterpolationList: LEFT_BRACE fieldInterpolationListElement (COMMA fieldInterpolationListElement)* RIGHT_BRACE
		     |	LEFT_BRACE RIGHT_BRACE;


fieldInterpolationListElement: fieldName LEFT_PARENTHESIS interpolationMethod RIGHT_PARENTHESIS
#FieldInterpolationListElementLabel;


/*
 * e.g: near, "1,2,3,NULL"  (interpolate with linear and if value is 1 or 2 or 3 or NULL then it is NODATA value)
*/
interpolationMethod: interpolationType COMMA nullResistance
#InterpolationMethodLabel;


/*
 * GDAL supported interpolation methods (near, bilinear, cubic, average,...)
*/
interpolationType: STRING_LITERAL                                                                                     #InterpolationTypeLabel;


/*
 * It will only apply NULL values on the specific range (band name), not other ranges (band names)
 * e.g: b1("near", "1,2,3") then if pixel value is 1 or 2 or 3 then it is set to NODATA for this pixel.
 *
*/
nullResistance: STRING_LITERAL;


coverageConstructorExpression: COVERAGE COVERAGE_VARIABLE_NAME
                               OVER axisIterator (COMMA axisIterator)*
                               VALUES coverageExpression                                                                #CoverageConstructorExpressionLabel;

/**
* $px x (Lat(0:20))
AxisIteratorLabel
* or
* $px x (imageCrsdomain(c[Lat(0:20), Long(0:20)], Lat))
AxisIteratorImageCrsDomainByDimensionLabel
* or
* $px x (imageCrsdomain(c[Long(0), Lat(0:20)))
AxisIteratorImageCrsDomainLabel
*/
axisIterator:   coverageVariableName axisName LEFT_PARENTHESIS  domainIntervals RIGHT_PARENTHESIS
#AxisIteratorDomainIntervalsLabel
		| coverageVariableName dimensionIntervalElement                                                             #AxisIteratorLabel;


intervalExpression: scalarExpression COLON scalarExpression                                                             #IntervalExpressionLabel;

coverageConstantExpression: COVERAGE COVERAGE_VARIABLE_NAME
                            OVER axisIterator (COMMA axisIterator)*
                            VALUE LIST LOWER_THAN constant (SEMICOLON constant)* GREATER_THAN                               #CoverageConstantExpressionLabel;

axisSpec: dimensionIntervalElement                                                                                         #AxisSpecLabel;

condenseExpression: reduceExpression
                  | generalCondenseExpression;

reduceBooleanExpressionOperator: ALL | SOME;

reduceNumericalExpressionOperator: COUNT | ADD | AVG | MIN | MAX;

reduceBooleanExpression: reduceBooleanExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS          #ReduceBooleanExpressionLabel;

reduceNumericalExpression: reduceNumericalExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS      #ReduceNumericalExpressionLabel;

reduceExpression: reduceBooleanExpression
                | reduceNumericalExpression;

condenseExpressionOperator: PLUS | MULTIPLICATION | MIN | MAX | AND | OR;


generalCondenseExpression: CONDENSE condenseExpressionOperator
                           OVER axisIterator (COMMA axisIterator)*
                           (whereClause)?
                           USING coverageExpression                                                                       #GeneralCondenseExpressionLabel;


/*
* Switch - Case
1. Return range constructor
* e.g: for c in (mr) return encode(
		switch case c > 1000 return {red: 107; green:17; blue:68, r1:30, r2:50}
		default return {red: 150; green:103; blue:14, r1:20, r2:50}
       , "png")
2. Return single value
* e.g: for c in (mr) return encode(
		switch case c > 10 and c < 20 return (char)5
		       case c > 30 and c < 50 return (char)12
		       case c > 70 and c < 100 return (char)5
                default return 2, "csv")
*/
switchCaseExpression: SWITCH CASE (LEFT_PARENTHESIS)*
					booleanSwitchCaseCombinedExpression
                                  (RIGHT_PARENTHESIS)*
				  RETURN rangeConstructorSwitchCaseExpression

			     (CASE (LEFT_PARENTHESIS)*
					booleanSwitchCaseCombinedExpression
				   (RIGHT_PARENTHESIS)*
			     RETURN rangeConstructorSwitchCaseExpression)*

		      DEFAULT RETURN rangeConstructorSwitchCaseExpression
#switchCaseRangeConstructorExpressionLabel
                     | SWITCH CASE (LEFT_PARENTHESIS)*
					booleanSwitchCaseCombinedExpression
				   (RIGHT_PARENTHESIS)*
				  RETURN scalarValueCoverageExpression

			    (CASE (LEFT_PARENTHESIS)*
					booleanSwitchCaseCombinedExpression
				   (RIGHT_PARENTHESIS)*
			    RETURN scalarValueCoverageExpression)*

		      DEFAULT RETURN scalarValueCoverageExpression
#switchCaseScalarValueExpressionLabel;


/**
 * Validate in code, no point to validate URIs here
 */
crsName: STRING_LITERAL;

axisName: COVERAGE_VARIABLE_NAME;

constant: STRING_LITERAL
        | TRUE | FALSE
        | (MINUS)? REAL_NUMBER_CONSTANT
        | complexNumberConstant;
