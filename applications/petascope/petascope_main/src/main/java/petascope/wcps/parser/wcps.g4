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

wcpsQuery : (forClauseList) 
            (whereClause)? 
            (letClauseList)? 
            (returnClause)
#WcpsQueryLabel;

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
forClauseList: FOR (forClause) (COMMA forClause)*
#ForClauseListLabel;

coverageIdForClause : COVERAGE_VARIABLE_NAME | decodeCoverageExpression;

/**
 * Example:
 * for $c in (cov1)
 */
forClause:  coverageVariableName IN
           (LEFT_PARENTHESIS)? coverageIdForClause (COMMA coverageIdForClause)* (RIGHT_PARENTHESIS)?
#ForClauseLabel;


/**
 * Example:
 * Let $a := $c[Lat(20:30), Long(40:45)],
 *     $b := $c + 2
 * return encode($a + $b, "png")
 *
*/
letClauseList: LET (letClause) (COMMA letClause)*
#LetClauseListLabel;

letClauseDimensionIntervalList: coverageVariableName COLON EQUAL LEFT_BRACKET dimensionIntervalList RIGHT_BRACKET;

letClause: letClauseDimensionIntervalList
           #letClauseDimensionIntervalListLabel
           | coverageVariableName COLON EQUAL coverageExpression
           #letClauseCoverageExpressionLabel;

/**
 * Example:
 *  where ($c.red > 100)
 *
 * Query:
 *  for $c in (cov1, cov2, cov3) where ($c.red + ($c.blue / 3) <= 163) return encode($c, "image/png")
 */
whereClause: WHERE (LEFT_PARENTHESIS)? coverageExpression (RIGHT_PARENTHESIS)?
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
coverageVariableName: COVERAGE_VARIABLE_NAME
#CoverageVariableNameLabel;


/**
 * Example:
 *   (1+1)
 * | encode($c, "image/png")
 */
processingExpression: getComponentExpression
                    | scalarExpression
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
booleanScalarExpression: reduceBooleanExpression
                         #BooleanReduceExpression
                       | booleanConstant
                         #BooleanConstantLabel
                       | booleanUnaryOperator LEFT_PARENTHESIS? booleanScalarExpression RIGHT_PARENTHESIS?
                         #BooleanUnaryScalarLabel
                       | booleanScalarExpression booleanOperator booleanScalarExpression
                         #BooleanBinaryScalarLabel
                       | numericalScalarExpression numericalComparissonOperator  numericalScalarExpression
                         #BooleanNumericalComparisonScalarLabel
                       | reduceBooleanExpression
                         #BooleanReduceExpression
                       | stringScalarExpression stringOperator stringScalarExpression
                         #BooleanStringComparisonScalar;

booleanUnaryOperator: NOT;

booleanConstant: TRUE | FALSE;

booleanOperator: AND | XOR | OR;

numericalComparissonOperator: GREATER_THAN | GREATER_OR_EQUAL_THAN | LOWER_THAN | LOWER_OR_EQUAL_THAN | EQUAL | NOT_EQUAL;

stringOperator: EQUAL | NOT_EQUAL;

stringScalarExpression: STRING_LITERAL
#StringScalarExpressionLabel;

starExpression: MULTIPLICATION
#StarExpressionLabel;


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
                         | number     
#NumericalRealNumberExpressionLabel
                         | NAN_NUMBER_CONSTANT
                           #NumericalNanNumberExpressionLabel
                         | complexNumberConstant
                           #NumericalComplexNumberConstant
                         ;
/**
 * Example:
 *  (2,5)  //the equivalent of 2 + 5i
 */
complexNumberConstant: LEFT_PARENTHESIS REAL_NUMBER_CONSTANT COMMA REAL_NUMBER_CONSTANT RIGHT_PARENTHESIS               #ComplexNumberConstantLabel;
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

/**
e.g: imageCrsdomain(c) returns (0:5,0:20,0:60)
imageCrsdomain(c, ansi) returns (0:5)
imageCrsdomain(c, Lat).lo returns 0
imageCrsdomain(c, Long).hi returns 60
**/
sdomExtraction: (LOWER_BOUND | UPPER_BOUND);

/*
domain()
The domain of coverage with the specific axis and its CRS (geo-referenced CRS or grid CRS)

for $c in (eobstest) return domain(c, Lat, "http://.../Index2D")
return (-25:75)
*/
domainExpression: DOMAIN LEFT_PARENTHESIS coverageExpression COMMA axisName (COMMA crsName)? RIGHT_PARENTHESIS
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

domainIntervals: (domainExpression | imageCrsDomainExpression | imageCrsDomainByDimensionExpression) (sdomExtraction)?; 

positionalParamater: POSITIONAL_PARAMETER;
extraParams: STRING_LITERAL | EXTRA_PARAMS;

/**
 * Example:
 *   encode($c, "image/tiff")
 * | encode($c, "image/png", "NODATA=0")
 */
encodedCoverageExpression: ENCODE LEFT_PARENTHESIS
                           coverageExpression COMMA STRING_LITERAL (COMMA extraParams)?
                           RIGHT_PARENTHESIS                                                                            #EncodedCoverageExpressionLabel;

/**
 * Example:
 *   decode("1,2,3,4", "csv")
 * Query:
 *   ?!
 */
decodeCoverageExpression: DECODE LEFT_PARENTHESIS
                          positionalParamater (COMMA extraParams)?
                          RIGHT_PARENTHESIS                                                                             #DecodedCoverageExpressionLabel;

/**
 * See subclauses
 */
coverageExpression: coverageExpression booleanOperator coverageExpression
                    #CoverageExpressionLogicLabel
		          | domainIntervals
                    #CoverageExpressionDomainIntervalsLabel
		          | coverageConstructorExpression
                    #CoverageExpressionConstructorLabel
                  | coverageExpression coverageArithmeticOperator coverageExpression
                    #CoverageExpressionArithmeticLabel
                  | coverageExpression numericalComparissonOperator coverageExpression
                    #CoverageExpressionComparissonLabel
                  | coverageVariableName
                    #CoverageExpressionVariableNameLabel
                  | coverageConstantExpression
                    #CoverageExpressionConstantLabel
                  | decodeCoverageExpression
                    #CoverageExpressionDecodeLabel
                  | coverageExpression LEFT_BRACKET dimensionPointList RIGHT_BRACKET
                    #CoverageExpressionShorthandSliceLabel
                  | SLICE LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionPointList RIGHT_BRACE
                          RIGHT_PARENTHESIS
                    #CoverageExpressionSliceLabel
                  | coverageExpression LEFT_BRACKET dimensionIntervalList RIGHT_BRACKET
                    #CoverageExpressionShorthandSubsetLabel
                  | coverageExpression LEFT_BRACKET coverageVariableName RIGHT_BRACKET
                    #coverageXpressionShortHandSubsetWithLetClauseVariableLabel
                  | TRIM LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE
                    RIGHT_PARENTHESIS
                    #CoverageExpressionTrimCoverageLabel
                  | EXTEND LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE
                    RIGHT_PARENTHESIS
                    #CoverageExpressionExtendLabel
                  | EXTEND LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE domainIntervals RIGHT_BRACE
                    RIGHT_PARENTHESIS
                    #CoverageExpressionExtendByDomainIntervalsLabel
                  | unaryArithmeticExpression
                    #CoverageExpressionUnaryArithmeticLabel
                  | trigonometricExpression
                    #CoverageExpressionTrigonometricLabel
                  | exponentialExpression
                    #CoverageExpressionExponentialLabel
                  | minBinaryExpression                    
  		            #CoverageExpressionMinBinaryLabel
                  | maxBinaryExpression                    
  		            #CoverageExpressionMaxBinaryLabel
		          | unaryPowerExpression
                    #CoverageExpressionPowerLabel
                  | unaryBooleanExpression
                    #CoverageExpressionUnaryBooleanLabel
                  | castExpression
                    #CoverageExpressionCastLabel
                  | coverageExpression DOT fieldName
                    #CoverageExpressionRangeSubsettingLabel
                  | rangeConstructorExpression
                    #CoverageExpressionRangeConstructorLabel
                  | clipWKTExpression
                    #CoverageExpressionClipWKTLabel
                  | clipCurtainExpression
                    #CoverageExpressionClipCurtainLabel
                  | clipCorridorExpression
                    #CoverageExpressionClipCorridorLabel
                  | crsTransformExpression
                    #CoverageExpressionCrsTransformLabel
		          | switchCaseExpression
                    #CoverageExpressionSwitchCaseLabel
                  | SCALE LEFT_PARENTHESIS
                        coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE
                    RIGHT_PARENTHESIS
                    #CoverageExpressionScaleByDimensionIntervalsLabel
		          | SCALE LEFT_PARENTHESIS
		                coverageExpression COMMA LEFT_BRACE domainIntervals RIGHT_BRACE
                    RIGHT_PARENTHESIS
                    #CoverageExpressionScaleByImageCrsDomainLabel
                  | LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
                    #CoverageExpressionCoverageLabel
                  | SCALE_FACTOR LEFT_PARENTHESIS
                        coverageExpression COMMA number
                    RIGHT_PARENTHESIS
                    #CoverageExpressionScaleByFactorLabel
                  | SCALE_AXES LEFT_PARENTHESIS
                        coverageExpression COMMA LEFT_BRACKET scaleDimensionIntervalList RIGHT_BRACKET
                    RIGHT_PARENTHESIS
                    #CoverageExpressionScaleByAxesLabel
                  | SCALE_SIZE LEFT_PARENTHESIS
                        coverageExpression COMMA LEFT_BRACKET scaleDimensionIntervalList RIGHT_BRACKET
                    RIGHT_PARENTHESIS
                    #CoverageExpressionScaleBySizeLabel
                  | SCALE_EXTENT LEFT_PARENTHESIS
                        coverageExpression COMMA LEFT_BRACKET scaleDimensionIntervalList RIGHT_BRACKET
                    RIGHT_PARENTHESIS
                    #CoverageExpressionScaleByExtentLabel
                  | scalarExpression
                    #CoverageExpressionScalarLabel
  		          | coverageExpression IS (NOT)? NULL
		            #CoverageIsNullExpression
                  | coverageExpression OVERLAY coverageExpression
                    #CoverageExpressionOverlayLabel;
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




unaryArithmeticExpressionOperator: ABSOLUTE_VALUE | SQUARE_ROOT | REAL_PART | IMAGINARY_PART;

/**
 * Example
 *  -($coverage)
 *  sqrt($coverage)
 * Query:
 *   for $c in cov return encode(sqrt(abs($c)), "csv")
 */
unaryArithmeticExpression:  unaryArithmeticExpressionOperator  LEFT_PARENTHESIS? coverageExpression RIGHT_PARENTHESIS?    #UnaryCoverageArithmeticExpressionLabel;

/**
 * Example
 *  sin($coverage)
 * Query:
 *   for $c in cov return encode(sin(cos($c)), "csv")
 */
trigonometricExpression: trigonometricOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
#TrigonometricExpressionLabel;

exponentialExpressionOperator: EXP | LOG | LN;

/**
 * Example
 *  exp($coverage)
 * Query:
 *   for $c in cov return encode(ln(exp($c)), "csv")
 */
exponentialExpression: exponentialExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
#ExponentialExpressionLabel;

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
encode(max(c, c1), "csv") from test_mr as c, test_mr as c1
**/
minBinaryExpression: MIN LEFT_PARENTHESIS coverageExpression COMMA coverageExpression RIGHT_PARENTHESIS
#minBinaryExpressionLabel;

maxBinaryExpression: MAX LEFT_PARENTHESIS coverageExpression COMMA coverageExpression RIGHT_PARENTHESIS
#maxBinaryExpressionLabel;


/**
 * Example
 *  NOT($coverage)
 * Query:
 *   for $c in cov return encode(NOT(BIT($c, 2)), "csv")
 */
unaryBooleanExpression: NOT LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
                        #NotUnaryBooleanExpressionLabel
                      | BIT LEFT_PARENTHESIS coverageExpression COMMA numericalScalarExpression RIGHT_PARENTHESIS
                        #BitUnaryBooleanExpressionLabel;


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
castExpression: LEFT_PARENTHESIS rangeType RIGHT_PARENTHESIS coverageExpression
#CastExpressionLabel;


fieldName: COVERAGE_VARIABLE_NAME | INTEGER;

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



dimensionPointList: dimensionPointElement (COMMA dimensionPointElement)*
#DimensionPointListLabel;

dimensionPointElement: axisName (COLON crsName)? LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
#DimensionPointElementLabel;

dimensionIntervalList: dimensionIntervalElement (COMMA dimensionIntervalElement)*
#DimensionIntervalListLabel;

/*
 Used by WCS scaling extension, e.g: GetCoverage&coverageId=test_mr&scaleaxes=i(0.5),j(0.5)
 then the grid pixels of i and j axes are: number / 0.5
 WCPS: scale(c, [i(0.5), j(0.5)])
*/
scaleDimensionIntervalList: scaleDimensionIntervalElement (COMMA scaleDimensionIntervalElement)* 
#ScaleDimensionIntervalListLabel;  

/*
e.g: i(20:30)
which means scale to the grid interval 20:30
used only for scaleextent
*/
scaleDimensionIntervalElement: axisName LEFT_PARENTHESIS
                            (number | STRING_LITERAL) COLON (number | STRING_LITERAL)
                          RIGHT_PARENTHESIS                                                                             #TrimScaleDimensionIntervalElementLabel
/*
e.g: i(0.5)
used for scaleaxes, scalesize
*/
                        | axisName LEFT_PARENTHESIS number
                          RIGHT_PARENTHESIS                                                                             #SliceScaleDimensionIntervalElementLabel;
                          
                          
/*
Use for trimming, slicing of coverage (e.g: Lat:"CRS:1"(0:20))
*/
dimensionIntervalElement: axisName (COLON crsName)? LEFT_PARENTHESIS
                            coverageExpression COLON coverageExpression
                          RIGHT_PARENTHESIS                                                                             #TrimDimensionIntervalElementLabel
                        | axisName (COLON crsName)? LEFT_PARENTHESIS coverageExpression
                          RIGHT_PARENTHESIS                                                                             #SliceDimensionIntervalElementLabel;

/* e.g: 20 30, 30 40, 50 60 */
wktPoints: (constant (constant)*) (COMMA constant (constant)*)*
#wktPointsLabel;

/* e.g: (20 30, 30 40, 50 60), (20 30, 30 40, 50 60) */
wktPointElementList: LEFT_PARENTHESIS wktPoints RIGHT_PARENTHESIS (COMMA LEFT_PARENTHESIS wktPoints RIGHT_PARENTHESIS)*
#WKTPointElementListLabel;

/* e.g: Linestring (20 30, 30 40, 50 60) */
wktLineString: LINESTRING wktPointElementList
#WKTLineStringLabel;

/* e.g: Polygon ( (20 30, 30 40, 50 60) -- exterior, (20 30, 30 40, 50 60) --interior ) */
wktPolygon: POLYGON LEFT_PARENTHESIS wktPointElementList RIGHT_PARENTHESIS
#WKTPolygonLabel;

/* e.g: Multipolygon ( ((20 30, 30 40, 50 60)) --polygon 1, ((20 30, 30 40, 50 60), (20 30, 30 40, 50 60)) --polygon 2 )   */
wktMultipolygon: MULTIPOLYGON LEFT_PARENTHESIS LEFT_PARENTHESIS wktPointElementList RIGHT_PARENTHESIS (COMMA LEFT_PARENTHESIS wktPointElementList RIGHT_PARENTHESIS)* RIGHT_PARENTHESIS
#WKTMultipolygonLabel;

wktExpression: (wktPolygon | wktLineString | wktMultipolygon)
#WKTExpressionLabel;


curtainProjectionAxisLabel1: COVERAGE_VARIABLE_NAME;
curtainProjectionAxisLabel2: COVERAGE_VARIABLE_NAME;

/*
  clip( coverageExpression, curtain( project(axis1, axis2), WKT), CRS ) 
*/
clipCurtainExpression: CLIP LEFT_PARENTHESIS coverageExpression
                                             COMMA CURTAIN LEFT_PARENTHESIS
                                                PROJECTION LEFT_PARENTHESIS curtainProjectionAxisLabel1 COMMA curtainProjectionAxisLabel2 RIGHT_PARENTHESIS
                                                COMMA wktExpression 
                                             RIGHT_PARENTHESIS
					     (COMMA crsName)?
			    RIGHT_PARENTHESIS
#ClipCurtainExpressionLabel;


corridorProjectionAxisLabel1: COVERAGE_VARIABLE_NAME;
corridorProjectionAxisLabel2: COVERAGE_VARIABLE_NAME;

/*
  clip( coverageExpression, corridor( project(axis1, axis2), LineString, WKT, discrete), CRS ) 
*/
clipCorridorExpression: CLIP LEFT_PARENTHESIS coverageExpression
                                              COMMA CORRIDOR LEFT_PARENTHESIS
                                                   PROJECTION LEFT_PARENTHESIS corridorProjectionAxisLabel1 COMMA corridorProjectionAxisLabel2 RIGHT_PARENTHESIS
                                                   COMMA wktLineString 
                                                   COMMA wktExpression 
                                                   (COMMA DISCRETE)?
                                                RIGHT_PARENTHESIS
					                          (COMMA crsName)?
			                 RIGHT_PARENTHESIS
#ClipCorridorExpressionLabel;

/*
  clip(coverageExpression, WKT) is used to clip a coverage with 1D (linestring), 2D (polygon, multipolygons), 3D+ (curtain queries)
  example:  for c in (test_rgb) return encode(clip(c[i(0:20), j(0:20)], Polygon((0 10, 20 20, 20 10, 0 10)), "png")
  Only support 1 clip operator for each query and it should be applied second last before crsTransform() when parsing a WCPS queries.
  invalid query, e.g: for c in (test_rgb) return encode (clip(clip(c[i(0:20), j(0:20)], Polygon((0 10, 20 20, 20 10, 0 10)), Polygon((0 10, 20 20, 20 10, 0 10)), "png")
  
  A geo CRS (e.g: http://opengis.net/def/CRS/EPSG/0/4326) can be input parameter for clip operator and the XY coordinates in WKT will be transformed 
  from this CRS to coverage's native CRS for XY axes (e.g: EPSG:3857). The output clipped coverage will keep native CRS EPSG:3857.
*/
clipWKTExpression: CLIP LEFT_PARENTHESIS coverageExpression COMMA wktExpression (COMMA crsName)? RIGHT_PARENTHESIS (WITH_COORDINATES)?
#ClipWKTExpressionLabel;




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
 * {near}), "tiff")
 *
 * NOTE: if mean_summer_airtemp has multiple ranges (bands), e.g: b1, b2, b3 then b2 and b3 which are not
 * passed in $INTERPOLATION then will use default interpolation of Rasdaman and "NODATA=..." in encoding.
*/
crsTransformExpression: CRS_TRANSFORM LEFT_PARENTHESIS
                          coverageExpression COMMA dimensionCrsList
                          (COMMA LEFT_BRACE interpolationType? RIGHT_BRACE)?
                        RIGHT_PARENTHESIS
#CrsTransformExpressionLabel;


/*
 * e.g: { Lat:"http://localhost:8080/def/crs/EPSG/0/4326", Long:"http://localhost:8080/def/crs/EPSG/0/4326"}
*/
dimensionCrsList: LEFT_BRACE dimensionCrsElement (COMMA dimensionCrsElement)* RIGHT_BRACE
#DimensionCrsListLabel;


/*
 * e.g: Lat:"http://localhost:8080/def/crs/EPSG/0/4326"
*/
dimensionCrsElement: axisName COLON crsName
#DimensionCrsElementLabel;

/*
 * GDAL supported interpolation methods (near, bilinear, cubic, average,...)
*/
interpolationType: COVERAGE_VARIABLE_NAME
#InterpolationTypeLabel;


coverageConstructorExpression: COVERAGE COVERAGE_VARIABLE_NAME
                               OVER axisIterator (COMMA axisIterator)*
                               VALUES coverageExpression
#CoverageConstructorExpressionLabel;

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
		          | coverageVariableName dimensionIntervalElement
                #AxisIteratorLabel;


intervalExpression: scalarExpression COLON scalarExpression
#IntervalExpressionLabel;

coverageConstantExpression: COVERAGE COVERAGE_VARIABLE_NAME
                            OVER axisIterator (COMMA axisIterator)*
                            VALUE LIST LOWER_THAN constant (SEMICOLON constant)* GREATER_THAN
#CoverageConstantExpressionLabel;

axisSpec: dimensionIntervalElement
#AxisSpecLabel;

condenseExpression: reduceExpression
                  | generalCondenseExpression;

reduceBooleanExpressionOperator: ALL | SOME;

reduceNumericalExpressionOperator: COUNT | ADD | AVG | MIN | MAX;

reduceBooleanExpression: reduceBooleanExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
#ReduceBooleanExpressionLabel;

reduceNumericalExpression: reduceNumericalExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS      #ReduceNumericalExpressionLabel;

reduceExpression: reduceBooleanExpression
                | reduceNumericalExpression;

condenseExpressionOperator: PLUS | MULTIPLICATION | MIN | MAX | AND | OR;


generalCondenseExpression: CONDENSE condenseExpressionOperator
                           OVER axisIterator (COMMA axisIterator)*
                           (whereClause)?
                           USING coverageExpression
#GeneralCondenseExpressionLabel;


/*
* Switch - Case
1. Return range constructor
* e.g: for c in (mr) return encode(
		switch case c > 1000 return {red: 107; green:17; blue:68}
		default return {red: 150; green:103; blue:14}
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

number:   (MINUS)? INTEGER
        | (MINUS)? REAL_NUMBER_CONSTANT
        | (MINUS)? SCIENTIFIC_NUMBER_CONSTANT;

constant: STRING_LITERAL
        | TRUE | FALSE
        | (MINUS)? number
        | complexNumberConstant;
