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
forClauseList: FOR (forClause)+                                                                                             #ForClauseListLabel;

/**
 * Example:
 * for $c in (cov1)
 */
forClause:  coverageVariableName IN
           (LEFT_PARANTHESIS)? IDENTIFIER (COMMA IDENTIFIER)* (RIGHT_PARANTHESIS)?                                                       #ForClauseLabel;

/**
 * Example:
 *  where ($c.red > 100)
 *
 * Query:
 *  for $c in (cov1, cov2, cov3) where ($c.red + ($c.blue / 3) <= 163) return encode($c, "image/png")
 */
whereClause: WHERE booleanScalarExpression                                                                              #WhereClauseLabel;

/**
 * Example
 *   return 42;
 * | return encode($c, "image/png")
 *
 * Query:
 *  for $c in (someCoverage) return encode($c * 2.5, "image/tiff")
 */
returnClause: RETURN processingExpression                                                                               #ReturnClauseLabel;

/**
 * Example
 * $coverageName;
 *
 * Query:
 * for $coverageName in someCoverage return encode($coverageName, "csv");
 *
 * Replaced COVERAGE_VARIABLE_NAME with IDENTIFIER for backwards compatibility
 */
coverageVariableName: IDENTIFIER                                                                            #CoverageVariableNameLabel;


/**
 * Example:
 *   (1+1)
 * | encode($c, "image/png")
 */
processingExpression: scalarExpression
                    | encodedCoverageExpression;

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
                       | booleanUnaryOperator LEFT_PARANTHESIS? booleanScalarExpression RIGHT_PARANTHESIS?              #BooleanUnaryScalarLabel
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
 * Example:
 *   1 + 2
 * | avg(a) / avg(b)
 *
 * Query:
 * for c in (someCoverage) return ((avg(a) / avg(b) * 9/5) + 32)
 */
numericalScalarExpression: numericalUnaryOperation LEFT_PARANTHESIS numericalScalarExpression RIGHT_PARANTHESIS         #NumericalUnaryScalarExpressionLabel
                         | trigonometricOperator LEFT_PARANTHESIS numericalScalarExpression RIGHT_PARANTHESIS           #NumericalTrigonometricScalarExpressionLabel
                         | numericalScalarExpression numericalOperator numericalScalarExpression                        #NumericalBinaryScalarExpressionLabel
                         | condenseExpression                                                                           #NumericalCondenseExpressionLabel
                         | REAL_NUMBER_CONSTANT                                                                         #NumericalRealNumberExpressionLabel
                         | complexNumberConstant                                                                        #NumericalComplexNumberConstant
                         ;
/**
 * Example:
 *  (2,5)  //the equivalent of 2 + 5i
 */
complexNumberConstant: LEFT_PARANTHESIS REAL_NUMBER_CONSTANT COMMA REAL_NUMBER_CONSTANT RIGHT_PARANTHESIS               #ComplexNumberConstantLabel;
numericalOperator: PLUS | MINUS | MULTIPLICATION | DIVISION;
numericalUnaryOperation: ABSOLUTE_VALUE | SQUARE_ROOT |  REAL_PART | IMAGINARY_PART | ROUND | MINUS | PLUS;
trigonometricOperator: SIN | COS | TAN | SINH | COSH | TANH | ARCSIN | ARCCOS | ARCTAN;



/**
 * Example:
 *  See the rules below;
 */
getComponentExpression: coverageIdExpression
                      | describeCoverageExpression;

/**
 * Example:
 * id($c);
 *
 * Query:
 * for $c in (someCov) return id($c)
 */
coverageIdExpression: ID LEFT_PARANTHESIS coverageVariableName RIGHT_PARANTHESIS                                        #CoverageIdExpressionLabel;

/**
 * Example:
 * describeCoverage($c);
 *
 * Query:
 * for $c in (someCov) return describeCoverage($c)
 */
describeCoverageExpression: DESCRIBE_COVERAGE LEFT_PARANTHESIS coverageVariableName RIGHT_PARANTHESIS                   #DescribeCoverageExpressionLabel;

/**
 * Example:
 *   encode($c, "image/tiff")
 * | encode($c, "image/png", "NODATA=0")
 */
encodedCoverageExpression: ENCODE LEFT_PARANTHESIS
                           coverageExpression COMMA FORMAT_NAME (COMMA)? (STRING_LITERAL)?
                           RIGHT_PARANTHESIS                                                                            #EncodedCoverageExpressionLabel;

/**
 * Example:
 *   decode("1,2,3,4", "csv)
 * Query:
 *   ?!
 */
decodeCoverageExpression: DECODE LEFT_PARANTHESIS
                          STRING_LITERAL COMMA FORMAT_NAME (COMMA)? (STRING_LITERAL)?
                          RIGHT_PARANTHESIS                                                                             #DecodedCoverageExpressionLabel;




/**
 * See subclauses
 */
coverageExpression: coverageExpression booleanOperator coverageExpression                                               #CoverageExpressionLogicLabel
                  | coverageExpression coverageArithmeticOperator coverageExpression                                    #CoverageExpressionArithmeticLabel
                  | coverageExpression numericalComparissonOperator coverageExpression                                  #CoverageExpressionComparissonLabel
                  | coverageVariableName                                                                                #CoverageExpressionVariableNameLabel
                  | scalarExpression                                                                                    #CoverageExpressionScalarLabel
                  | coverageConstantExpression                                                                          #CoverageExpressionConstantLabel
                  | coverageConstructorExpression                                                                       #CoverageExpressionConstructorLabel
                  | decodeCoverageExpression                                                                            #CoverageExpressionDecodeLabel
                  | coverageExpression LEFT_BRACKET dimensionIntervalList RIGHT_BRACKET                                 #CoverageExpressionShorthandTrimLabel
                  | TRIM LEFT_PARANTHESIS coverageExpression COMMA dimensionIntervalList RIGHT_PARANTHESIS              #CoverageExpressionTrimCoverageLabel
                  | coverageExpression LEFT_BRACKET dimensionPointList RIGHT_BRACKET                                    #CoverageExpressionShorthandSliceLabel
                  | SLICE LEFT_PARANTHESIS coverageExpression COMMA LEFT_BRACE dimensionPointList RIGHT_BRACE RIGHT_PARANTHESIS                #CoverageExpressionSliceLabel
                  | EXTEND LEFT_PARANTHESIS coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE RIGHT_PARANTHESIS            #CoverageExpressionExtendLabel
                  | unaryArithmeticExpression                                                                           #CoverageExpressionUnaryArithmeticLabel
                  | trigonometricExpression                                                                             #CoverageExpressionTrigonometricLabel
                  | exponentialExpression                                                                               #CoverageExpressionExponentialLabel
                  | unaryBooleanExpression                                                                              #CoverageExpressionUnaryBooleanLabel
                  | castExpression                                                                                      #CoverageExpressionCastLabel
                  | coverageExpression DOT fieldName                                                                    #CoverageExpressionRangeSubsettingLabel
                  | rangeConstructorExpression                                                                          #CoverageExpressionRangeConstructorLabel
                  | crsTransformExpression                                                                              #CoverageExpressionCrsTransformLabel
                  | SCALE LEFT_PARANTHESIS
                     coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE (COMMA fieldInterpolationList)*
                    RIGHT_PARANTHESIS                                                                                   #CoverageExpressionScaleLabel
                  | LEFT_PARANTHESIS coverageExpression RIGHT_PARANTHESIS                                               #CoverageExpressionCoverageLabel;


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


coverageArithmeticOperator: PLUS | MULTIPLICATION | DIVISION | MINUS | OVERLAY;

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
unaryArithmeticExpression:  unaryArithmeticExpressionOperator  LEFT_PARANTHESIS coverageExpression RIGHT_PARANTHESIS    #UnaryCoverageArithmeticExpressionLabel;

/**
 * Example
 *  sin($coverage)
 * Query:
 *   for $c in cov return encode(sin(cos($c)), "csv")
 */
trigonometricExpression: trigonometricOperator LEFT_PARANTHESIS coverageExpression RIGHT_PARANTHESIS                    #TrigonometricExpressionLabel;

exponentialExpressionOperator: EXP | LOG | LN;

/**
 * Example
 *  exp($coverage)
 * Query:
 *   for $c in cov return encode(ln(exp($c)), "csv")
 */
exponentialExpression: exponentialExpressionOperator LEFT_PARANTHESIS coverageExpression RIGHT_PARANTHESIS              #ExponentialExpressionLabel;

/**
 * Example
 *  NOT($coverage)
 * Query:
 *   for $c in cov return encode(NOT(BIT($c, 2)), "csv")
 */
unaryBooleanExpression: NOT LEFT_PARANTHESIS coverageExpression RIGHT_PARANTHESIS                                       #NotUnaryBooleanExpressionLabel
                      | BIT LEFT_PARANTHESIS coverageExpression COMMA numericalScalarExpression RIGHT_PARANTHESIS       #BitUnaryBooleanExpressionLabel;


/**
 * We allow any value here so we do not pollute the parser with business logic.
 * The existence of the rangeType should be checked in the code, not here.
 */
rangeType: IDENTIFIER (IDENTIFIER)*;

/**
 * Example
 *  (boolean) $coverage
 * Query:
 *   for $c in cov return encode((char) $c, "csv")
 */
castExpression: LEFT_PARANTHESIS rangeType RIGHT_PARANTHESIS coverageExpression                                         #CastExpressionLabel;


fieldName: IDENTIFIER | REAL_NUMBER_CONSTANT;

/**
 * Example
 *  struct {red: $c.blue; blue: $c.red; green: $c.green}
 * Query:
 *   for $c in cov return encode(struct {red: $c.blue; blue: $c.red; green: $c.green}, "csv")
 */
rangeConstructorExpression: LEFT_BRACE
                              (fieldName COLON coverageExpression) (SEMICOLON fieldName COLON coverageExpression)*
                            RIGHT_BRACE                                                                                 #RangeConstructorExpressionLabel;


crsTransformExpression: CRS_TRANSFORM LEFT_PARANTHESIS
                          coverageExpression COMMA dimensionCrsList COMMA fieldInterpolationList
                        RIGHT_PARANTHESIS                                                                               #CrsTransformExpressionLabel;

dimensionPointList: dimensionPointElement (COMMA dimensionPointElement)*                                                #DimensionPointListLabel;

dimensionPointElement: axisName (COLON crsName)? LEFT_PARANTHESIS dimensionPointExpression RIGHT_PARANTHESIS            #DimensionPointElementLabel;

dimensionPointExpression: STRING_LITERAL | TRUE | FALSE | REAL_NUMBER_CONSTANT                                          #DimensionPointExpressionLabel;

dimensionIntervalList: dimensionIntervalElement (COMMA dimensionIntervalElement)*                                       #DimensionIntervalListLabel;

dimensionIntervalElement: axisName (COLON crsName)? LEFT_PARANTHESIS
                            scalarExpression COLON scalarExpression
                          RIGHT_PARANTHESIS                                                                             #TrimDimensionIntervalElementLabel
                        | axisName (COLON crsName)? LEFT_PARANTHESIS scalarExpression
                          RIGHT_PARANTHESIS                                                                             #SliceDimensionIntervalElementLabel;

dimensionCrsList: LEFT_BRACE dimensionCrsElement (COMMA dimensionCrsElement)* RIGHT_BRACE                               #DimensionCrsListLabel;

dimensionCrsElement: axisName COLON crsName                                                                             #DimensionCrsElementLabel;

fieldInterpolationList: LEFT_BRACE fieldInterpolationListElement (COMMA fieldInterpolationListElement)* RIGHT_BRACE     #FieldInterpolationListLabel;

fieldInterpolationListElement: fieldName interpolationMethod                                                            #FieldInterpolationListElementLabel;

interpolationMethod: LEFT_PARANTHESIS interpolationType COLON nullResistance RIGHT_PARANTHESIS                          #InterpolationMethodLabel;

nullResistance: TRUE | FALSE;

interpolationType: STRING_LITERAL                                                                                       #InterpolationTypeLabel;

coverageConstructorExpression: COVERAGE IDENTIFIER
                               OVER axisIterator (COMMA axisIterator)*
                               VALUES scalarExpression                                                                  #CoverageConstructorExpressionLabel;

axisIterator: IDENTIFIER axisName LEFT_PARANTHESIS intervalExpression RIGHT_PARANTHESIS                                 #AxisIteratorLabel;

intervalExpression: scalarExpression COLON scalarExpression                                                             #IntervalExpressionLabel
                  | IMGCRSDOMAIN LEFT_PARANTHESIS coverageVariableName COMMA axisName RIGHT_PARANTHESIS                 #CRSIntervalExpressionLabel;

coverageConstantExpression: COVERAGE IDENTIFIER
                            OVER axisIterator (COMMA axisIterator)*
                            VALUE LIST LOWER_THAN constant (SEMICOLON constant)* GREATER_THAN                               #CoverageConstantExpressionLabel;

axisSpec: axisName LEFT_PARANTHESIS intervalExpression RIGHT_PARANTHESIS                                                #AxisSpecLabel;

condenseExpression: reduceExpression
                  | generalCondenseExpression;

reduceBooleanExpressionOperator: ALL | SOME;

reduceNumericalExpressionOperator: COUNT | ADD | AVG | MIN | MAX;

reduceBooleanExpression: reduceBooleanExpressionOperator LEFT_PARANTHESIS coverageExpression RIGHT_PARANTHESIS          #ReduceBooleanExpressionLabel;

reduceNumericalExpression: reduceNumericalExpressionOperator LEFT_PARANTHESIS coverageExpression RIGHT_PARANTHESIS      #ReduceNumericalExpressionLabel;

reduceExpression: reduceBooleanExpression
                | reduceNumericalExpression;

condenseExpressionOperator: PLUS | MULTIPLICATION | MIN | MAX | AND | OR;

generalCondenseExpression: CONDENSE condenseExpressionOperator
                           OVER axisIterator (COMMA axisIterator)*
                           (WHERE booleanScalarExpression)?
                           USING scalarExpression                                                                       #GeneralCondenseExpressionLabel;

/**
 * Validate in code, no point to validate URIs here
 */
crsName: STRING_LITERAL;

axisName: IDENTIFIER;

constant: STRING_LITERAL
        | TRUE | FALSE
        | REAL_NUMBER_CONSTANT
        | complexNumberConstant;
