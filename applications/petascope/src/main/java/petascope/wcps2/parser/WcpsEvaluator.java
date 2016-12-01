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
package petascope.wcps2.parser;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import petascope.wcps2.error.managed.processing.*;
import petascope.wcps2.metadata.service.*;
import petascope.wcps2.metadata.model.ParsedSubset;
import petascope.wcps2.result.VisitorResult;

import petascope.wcps2.handler.*;
import static petascope.wcs2.parsers.subsets.DimensionSlice.ASTERISK;

import java.util.*;
import org.slf4j.LoggerFactory;
import petascope.exceptions.PetascopeException;
import petascope.util.CrsUtil;
import petascope.wcps2.metadata.model.RangeField;
import petascope.wcps2.result.WcpsMetadataResult;
import petascope.wcps2.result.WcpsResult;
import petascope.wcps2.result.parameters.*;

/**
 * Class that implements the parsing rules described in wcps.g4
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class WcpsEvaluator extends wcpsBaseVisitor<VisitorResult> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WcpsEvaluator.class);

    private final CoverageAliasRegistry coverageAliasRegistry;
    private final CoverageRegistry coverageRegistry;
    private final AxisIteratorAliasRegistry axisIteratorAliasRegistry;
    private final WcpsCoverageMetadataService wcpsCoverageMetadataService;
    private final RasqlTranslationService rasqlTranslationService;
    private final SubsetParsingService subsetParsingService;
    private String mimeType = "";

    /**
     * Class constructor.
     * This object is created for each incoming Wcps query.
     * @param coverageRegistry service able to serve coverage metadata.
     * @param wcpsCoverageMetadataService
     * @param rasqlTranslationService
     * @param subsetParsingService
     */
    public WcpsEvaluator(CoverageRegistry coverageRegistry, WcpsCoverageMetadataService wcpsCoverageMetadataService,
                         RasqlTranslationService rasqlTranslationService, SubsetParsingService subsetParsingService) {
        super();
        //the coverage registry is created at application start
        this.coverageRegistry = coverageRegistry;
        this.wcpsCoverageMetadataService = wcpsCoverageMetadataService;
        this.rasqlTranslationService = rasqlTranslationService;
        this.subsetParsingService = subsetParsingService;
        //a new coverage alias registry is created for each query
        this.coverageAliasRegistry = new CoverageAliasRegistry();
        //a new axis iterator alias registry is created for each query
        this.axisIteratorAliasRegistry = new AxisIteratorAliasRegistry();
    }

    // VISITOR HANDLERS
    /* --------------------- Visit each nodes then parse ------------------ */
    @Override
    public VisitorResult visitWcpsQueryLabel(@NotNull wcpsParser.WcpsQueryLabelContext ctx) {
        WcpsResult forClauseList = (WcpsResult) visit(ctx.forClauseList());
        //only visit the where clause if it exists
        WcpsResult whereClause = null;
        if (ctx.whereClause() != null) {
            whereClause = (WcpsResult) visit(ctx.whereClause());
        }
        VisitorResult returnClause = visit(ctx.returnClause());
        VisitorResult result = null;
        if (returnClause instanceof WcpsResult) {
            result = WcpsQueryHandler.handle(forClauseList, whereClause, (WcpsResult) returnClause);
        } else {
            result = (WcpsMetadataResult) returnClause;
        }

        return result;
    }

    @Override
    public VisitorResult visitForClauseLabel(@NotNull wcpsParser.ForClauseLabelContext ctx) {
        List<TerminalNode> coverageNames = ctx.COVERAGE_VARIABLE_NAME();
        List<String> coverageNamesStr = new ArrayList<String>();

        for (int i = 0; i < coverageNames.size(); i++) {
            coverageNamesStr.add(coverageNames.get(i).getText());
        }

        WcpsResult result = ForClauseHandler.handle(ctx.coverageVariableName().getText(),
                            coverageNamesStr, coverageAliasRegistry);
        return result;
    }

    @Override
    public VisitorResult visitForClauseListLabel(@NotNull wcpsParser.ForClauseListLabelContext ctx) {
        List<WcpsResult> forClauses = new ArrayList();
        for (wcpsParser.ForClauseContext currentClause : ctx.forClause()) {
            forClauses.add((WcpsResult) visit(currentClause));
        }
        WcpsResult result = ForClauseListHandler.handle(forClauses);
        return result;
    }

    @Override
    public VisitorResult visitReturnClauseLabel(@NotNull wcpsParser.ReturnClauseLabelContext ctx) {
        VisitorResult processingExpr = visit(ctx.processingExpression());
        VisitorResult result = null;
        if (processingExpr instanceof WcpsResult) {
            result = ReturnClauseHandler.handle((WcpsResult) processingExpr);
        } else if (processingExpr instanceof WcpsMetadataResult) {
            //if metadata just pass it up
            result = (WcpsMetadataResult) processingExpr;
        }

        return result;
    }

    @Override
    public VisitorResult visitEncodedCoverageExpressionLabel(@NotNull wcpsParser.EncodedCoverageExpressionLabelContext ctx) {
        WcpsResult coverageExpression = (WcpsResult) visit(ctx.coverageExpression());
        List<TerminalNode> params = ctx.STRING_LITERAL();
        /**
         * Changed for backwards compatibility with WCPS1.
         * The change consisted in accepting a string literal as format name.
         * In order to revert the change, uncomment the code below.
         */

        //code removed for backwards compatibility
        //String format = ctx.FORMAT_NAME().getText();
        //end code removed for backwards compatibility

        //code added for backwards compatibility with WCPS1, where format name can be a STRING_LITERAL
        String format = params.get(0).getText();
        //end code added for backwards compatibility

        List<String> otherParams = null;
        if (params.size() > 1) {
            otherParams = new ArrayList();
            for (Integer i = 1; i < params.size(); i++) {
                otherParams.add(params.get(i).getText());
            }
        }

        // each WCPS query only return 1 MIME type
        this.mimeType = this.coverageRegistry.getMetadataSource().formatToMimetype(format.replace("\"", ""));

        WcpsResult result = null;
        try {
            result = EncodedCoverageHandler.handle(coverageExpression, format, otherParams, this.coverageRegistry);
        } catch (PetascopeException e) {
            log.error(e.getMessage(), e);
            throw new MetadataSerializationException();
        }
        return result;
    }

    @Override
    public WcpsResult visitCrsTransformExpressionLabel(@NotNull wcpsParser.CrsTransformExpressionLabelContext ctx) {
        // Handle crsTransform($COVERAGE_EXPRESSION, {$DOMAIN_CRS_2D}, {$INTERPOLATION})
        // e.g: crsTransform(c, {Lat:"www.opengis.net/def/crs/EPSG/0/4327", Long:"www.opengis.net/def/crs/EPSG/0/4327"}, {}
        WcpsResult coverageExpression = (WcpsResult)visit(ctx.coverageExpression());
        HashMap<String, String> axisCrss = new LinkedHashMap<String, String>();

        // { Axis_CRS_1 , Axis_CRS_2 } (e.g: Lat:"http://localhost:8080/def/crs/EPSG/0/4326")
        wcpsParser.DimensionCrsElementLabelContext crsX = (wcpsParser.DimensionCrsElementLabelContext)ctx.dimensionCrsList().getChild(1);
        axisCrss.put(crsX.axisName().getText(), crsX.crsName().getText().replace("\"", ""));

        wcpsParser.DimensionCrsElementLabelContext crsY = (wcpsParser.DimensionCrsElementLabelContext)ctx.dimensionCrsList().getChild(3);
        axisCrss.put(crsY.axisName().getText(), crsY.crsName().getText().replace("\"", ""));

        // Store the interpolation objects (rangeName, method -> nodata values)
        HashMap<String, HashMap<String, String>> rangeInterpolations = new LinkedHashMap<String, HashMap<String, String>>();

        // get interpolation parameters
        if (ctx.fieldInterpolationList().fieldInterpolationListElement().size() > 0) {
            // Iterate the interpolation list to get the range (band name) and its parameters (if it is available)
            for (wcpsParser.FieldInterpolationListElementContext element : ctx.fieldInterpolationList().fieldInterpolationListElement()) {

                // e.g: b1(A, B)
                String rangeName = element.getChild(0).getText();
                wcpsParser.InterpolationMethodContext intMethodObj = (wcpsParser.InterpolationMethodContext)element.getChild(2);
                // e.g: A = "near"
                String interpolationMethod = intMethodObj.getChild(0).getText().replace("\"", "");
                // e.g: B = "1,2,3"
                String nullValues = intMethodObj.getChild(2).getText().replace("\"", "");

                // e.g: "near" -> "1,2,3"
                HashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put(interpolationMethod, nullValues);

                rangeInterpolations.put(rangeName, map);
            }
        }


        WcpsResult result = CrsTransformHandler.handle(coverageExpression, axisCrss, rangeInterpolations, wcpsCoverageMetadataService);
        return result;

    }

    @Override
    public VisitorResult visitCoverageVariableNameLabel(@NotNull wcpsParser.CoverageVariableNameLabelContext ctx) {
        // Identifier, e.g: $c or c
        // NOTE: axisIterator and coverage variable name can be the same syntax (e.g: $c, $px)
        String coverageVariable = ctx.COVERAGE_VARIABLE_NAME().getText();
        WcpsResult result = CoverageVariableNameHandler.handle(coverageVariable, coverageAliasRegistry, coverageRegistry, axisIteratorAliasRegistry);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionLogicLabel(@NotNull wcpsParser.CoverageExpressionLogicLabelContext ctx) {
        // coverageExpression booleanOperator coverageExpression  (e.g: (c + 1) and (c + 1))
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.coverageExpression(0));
        String operand = ctx.booleanOperator().getText();
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.coverageExpression(1));

        WcpsResult result = BinaryCoverageExpressionHandler.handle(leftCoverageExpr, operand, rightCoverageExpr, wcpsCoverageMetadataService);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionArithmeticLabel(@NotNull wcpsParser.CoverageExpressionArithmeticLabelContext ctx) {
        // coverageExpression (+, -, *, /) coverageExpression (e.g: c + 5 or 2 - 3) as BinarycoverageExpression
        WcpsResult leftCoverageExpression = (WcpsResult) visit(ctx.coverageExpression(0));
        String operand = ctx.coverageArithmeticOperator().getText();
        WcpsResult rightCoverageExpression = (WcpsResult) visit(ctx.coverageExpression(1));

        WcpsResult result = BinaryCoverageExpressionHandler.handle(leftCoverageExpression, operand,
                            rightCoverageExpression, wcpsCoverageMetadataService);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionOverlayLabel(@NotNull wcpsParser.CoverageExpressionOverlayLabelContext ctx) {
        // coverageExpression OVERLAY coverageExpression (e.g: c overlay d)
        // invert the order of the operators since WCPS overlay order is the opposite of the one in rasql
        WcpsResult leftCoverageExpression = (WcpsResult) visit(ctx.coverageExpression(1));
        String overlay = ctx.OVERLAY().getText();
        WcpsResult rightCoverageExpression = (WcpsResult) visit(ctx.coverageExpression(0));

        WcpsResult result = BinaryCoverageExpressionHandler.handle(leftCoverageExpression, overlay, rightCoverageExpression, wcpsCoverageMetadataService);
        return result;
    }

    @Override
    public VisitorResult visitBooleanConstant(@NotNull wcpsParser.BooleanConstantContext ctx) {
        // TRUE | FALSE (e.g: true or false)
        WcpsResult result = BooleanConstantHandler.handle(ctx.getText());
        return result;
    }

    @Override
    public VisitorResult visitBooleanStringComparisonScalar(@NotNull wcpsParser.BooleanStringComparisonScalarContext ctx) {
        // stringScalarExpression stringOperator stringScalarExpression  (e.g: c = d) or (c != 2))
        String leftScalarStr = ctx.stringScalarExpression(0).getText();
        String operand = ctx.stringOperator().getText();
        String rightScalarStr = ctx.stringScalarExpression(1).getText();

        WcpsResult result = BinaryScalarExpressionHandler.handle(leftScalarStr, operand, rightScalarStr);
        return result;
    }

    @Override
    public VisitorResult visitCoverageConstructorExpressionLabel(@NotNull wcpsParser.CoverageConstructorExpressionLabelContext ctx) {
        // COVERAGE IDENTIFIER  OVER axisIterator (COMMA axisIterator)* VALUES coverageExpression
        // e.g: coverage cov over $px x(0:20), $px y(0:20) values avg(c)
        ArrayList<AxisIterator> axisIterators = new ArrayList<AxisIterator>();

        String coverageName = ctx.COVERAGE_VARIABLE_NAME().getText();

        String rasqlAliasName = "";
        String aliasName = "";
        int count = 0;

        // to build the IndexCRS for axis iterator
        int numberOfAxis = axisIterators.size();
        String crsUri = CrsUtil.OPENGIS_INDEX_ND_PATTERN.replace("%d", String.valueOf(numberOfAxis));

        for (wcpsParser.AxisIteratorContext i : ctx.axisIterator()) {
            AxisIterator axisIterator = (AxisIterator) visit(i);
            aliasName = axisIterator.getAliasName();
            if (rasqlAliasName.isEmpty()) {
                rasqlAliasName = aliasName.replace(SubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN, "");
            }
            axisIterator.getSubsetDimension().setCrs(crsUri);
            axisIterator.setRasqlAliasName(rasqlAliasName);
            axisIterator.setAxisIteratorOrder(count);

            // Add the axis iterator to the axis iterators alias registry
            axisIteratorAliasRegistry.addAxisIteratorAliasMapping(aliasName, axisIterator);
            axisIterators.add(axisIterator);
            // the order of axis iterator in the coverage
            count++;
        }

        WcpsResult valuesExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult wcpsResult = CoverageConstructorHandler.handle(coverageName, axisIterators, valuesExpr,
                                axisIteratorAliasRegistry,
                                wcpsCoverageMetadataService, rasqlTranslationService, subsetParsingService);
        return wcpsResult;
    }

    @Override
    public VisitorResult visitUnaryCoverageArithmeticExpressionLabel(@NotNull wcpsParser.UnaryCoverageArithmeticExpressionLabelContext ctx) {
        // unaryArithmeticExpressionOperator  LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: sqrt(c)
        String operator = ctx.unaryArithmeticExpressionOperator().getText();
        WcpsResult coverageExpr = (WcpsResult)visit(ctx.coverageExpression());

        WcpsResult result = UnaryArithmeticExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitTrigonometricExpressionLabel(@NotNull wcpsParser.TrigonometricExpressionLabelContext ctx) {
        // trigonometricOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: sin(c), cos(c), tan(c)
        String operator = ctx.trigonometricOperator().getText();
        WcpsResult coverageExpr = (WcpsResult)visit(ctx.coverageExpression());

        WcpsResult result = UnaryArithmeticExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitExponentialExpressionLabel(@NotNull wcpsParser.ExponentialExpressionLabelContext ctx) {
        // exponentialExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: exp(c), log(c), ln(c)
        String operand = ctx.exponentialExpressionOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = UnaryArithmeticExpressionHandler.handle(operand, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitUnaryPowerExpressionLabel(@NotNull wcpsParser.UnaryPowerExpressionLabelContext ctx) {
        // POWER LEFT_PARENTHESIS coverageExpression COMMA numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: pow(c, -0.5) or pow(c, avg(c))
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        WcpsResult scalarExpr = (WcpsResult) visit(ctx.numericalScalarExpression());

        WcpsResult result = UnaryPowerExpressionHandler.handle(coverageExpr, scalarExpr);
        return result;
    }

    @Override
    public VisitorResult visitNotUnaryBooleanExpressionLabel(@NotNull wcpsParser.NotUnaryBooleanExpressionLabelContext ctx) {
        // NOT LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: not(c)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = UnaryBooleanExpressionHandler.handle(coverageExpr, null);
        return result;
    }

    @Override
    public VisitorResult visitBitUnaryBooleanExpressionLabel(@NotNull wcpsParser.BitUnaryBooleanExpressionLabelContext ctx) {
        // BIT LEFT_PARENTHESIS coverageExpression COMMA numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: big(c, 2)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        WcpsResult scalarExpr = (WcpsResult) visit(ctx.numericalScalarExpression());

        WcpsResult result = UnaryBooleanExpressionHandler.handle(coverageExpr, scalarExpr);
        return result;
    }

    @Override
    public VisitorResult visitCastExpressionLabel(@NotNull wcpsParser.CastExpressionLabelContext ctx) {
        // LEFT_PARENTHESIS rangeType RIGHT_PARENTHESIS coverageExpression
        // e.g: (char)(c + 5)
        String castType = StringUtils.join(ctx.rangeType().COVERAGE_VARIABLE_NAME(), " ");
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = CastExpressionHandler.handle(castType, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionRangeSubsettingLabel(@NotNull wcpsParser.CoverageExpressionRangeSubsettingLabelContext ctx) {
        // coverageExpression DOT fieldName  (e.g: c.red or (c + 1).red)
        WcpsResult coverageExpression = (WcpsResult) visit(ctx.coverageExpression());
        String fieldName = ctx.fieldName().getText();

        WcpsResult result = RangeSubsettingHandler.handle(fieldName, coverageExpression, wcpsCoverageMetadataService);
        return result;
    }

    @Override
    public VisitorResult visitNumericalRealNumberExpressionLabel(@NotNull wcpsParser.NumericalRealNumberExpressionLabelContext ctx) {
        // REAL_NUMBER_CONSTANT
        // e.g: 2, 3
        WcpsResult result = RealNumberConstantHandler.handle(ctx.getText());
        return result;
    }

    @Override
    public VisitorResult visitNumericalNanNumberExpressionLabel(@NotNull wcpsParser.NumericalNanNumberExpressionLabelContext ctx) {
        // NAN_NUMBER_CONSTANT
        // e.g: c = nan
        WcpsResult result = NanScalarHandler.handle(ctx.getText());
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionCoverageLabel(@NotNull wcpsParser.CoverageExpressionCoverageLabelContext ctx) {
        // LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: used when a coverageExpression is surrounded by ().
        WcpsResult coverageExpression = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = ParenthesesCoverageExpressionHandler.handle(coverageExpression);
        return result;
    }

    @Override
    public VisitorResult visitWhereClauseLabel(@NotNull wcpsParser.WhereClauseLabelContext ctx) {
        // WHERE (LEFT_PARENTHESIS)? booleanScalarExpression (RIGHT_PARENTHESIS)?
        // e.g: where (c > 2)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.booleanScalarExpression());

        WcpsResult result = WhereClauseHandler.handle(coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitBooleanUnaryScalarLabel(@NotNull wcpsParser.BooleanUnaryScalarLabelContext ctx) {
        // booleanUnaryOperator LEFT_PARENTHESIS? booleanScalarExpression RIGHT_PARENTHESIS?
        // e.g: not(1 > 2)
        String operator = ctx.booleanUnaryOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.booleanScalarExpression());

        WcpsResult result = BooleanUnaryScalarExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitBooleanNumericalComparisonScalarLabel(@NotNull wcpsParser.BooleanNumericalComparisonScalarLabelContext ctx) {
        // numericalScalarExpression numericalComparissonOperator numericalScalarExpression
        // e.g: 1 >= avg(c) or (avg(c) = 2)
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression(0));
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression(1));
        String operator = ctx.numericalComparissonOperator().getText();

        WcpsResult result = BooleanNumericalComparisonScalarHandler.handle(leftCoverageExpr, rightCoverageExpr, operator);
        return result;
    }

    @Override
    public VisitorResult visitReduceBooleanExpressionLabel(@NotNull wcpsParser.ReduceBooleanExpressionLabelContext ctx) {
        // reduceBooleanExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: some(c), all(c)
        String operator = ctx.reduceBooleanExpressionOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = ReduceExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionComparissonLabel(@NotNull wcpsParser.CoverageExpressionComparissonLabelContext ctx) {
        // coverageExpression numericalComparissonOperator coverageExpression
        // e.g: ( (c + 1) > (c - 1) )
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.coverageExpression(0));
        String operand = ctx.numericalComparissonOperator().getText();
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.coverageExpression(1));

        WcpsResult result = BinaryCoverageExpressionHandler.handle(leftCoverageExpr, operand, rightCoverageExpr, wcpsCoverageMetadataService);
        return result;
    }

    @Override
    public VisitorResult visitBooleanBinaryScalarLabel(@NotNull wcpsParser.BooleanBinaryScalarLabelContext ctx) {
        // booleanScalarExpression booleanOperator booleanScalarExpression
        // Only use if both sides are scalar (e.g: (avg (c) > 5) or ( 3 > 2)
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.booleanScalarExpression(0));
        String operand = ctx.booleanOperator().getText();
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.booleanScalarExpression(1));

        WcpsResult result = BinaryCoverageExpressionHandler.handle(leftCoverageExpr, operand, rightCoverageExpr, wcpsCoverageMetadataService);
        return result;
    }

    @Override
    public VisitorResult visitBooleanSwitchCaseCoverageExpression(@NotNull wcpsParser.BooleanSwitchCaseCoverageExpressionContext ctx) {
        // coverageExpression numericalComparissonOperator coverageExpression
        // NOTE: used in switch case (e.g: switch case c > 100 or c < 100 or c > c or c = c)
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.coverageExpression().get(0));
        String operand = ctx.numericalComparissonOperator().getText();
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.coverageExpression().get(1));

        WcpsResult result = BooleanSwitchCaseCoverageExpressionHandler.handle(leftCoverageExpr, operand,
                            rightCoverageExpr, wcpsCoverageMetadataService);
        return result;
    }

    @Override
    public VisitorResult visitNumericalUnaryScalarExpressionLabel(@NotNull wcpsParser.NumericalUnaryScalarExpressionLabelContext ctx) {
        // numericalUnaryOperation LEFT_PARENTHESIS numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: abs(avg(c)), sqrt(avg(c + 1))
        String operator = ctx.numericalUnaryOperation().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression());

        WcpsResult result = UnaryArithmeticExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitNumericalTrigonometricScalarExpressionLabel(@NotNull wcpsParser.NumericalTrigonometricScalarExpressionLabelContext ctx) {
        // trigonometricOperator LEFT_PARENTHESIS numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: sin(avg(5))
        String operator = ctx.trigonometricOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression());

        WcpsResult result = UnaryArithmeticExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitNumericalBinaryScalarExpressionLabel(@NotNull wcpsParser.NumericalBinaryScalarExpressionLabelContext ctx) {
        // numericalScalarExpression numericalOperator numericalScalarExpression
        // e.g: avg(c) + 2, 5 + 3
        WcpsResult leftCoverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression(0));
        String operator = ctx.numericalOperator().getText();
        WcpsResult rightCoverageExpr = (WcpsResult) visit(ctx.numericalScalarExpression(1));

        WcpsResult result = BinaryCoverageExpressionHandler.handle(leftCoverageExpr, operator, rightCoverageExpr, wcpsCoverageMetadataService);
        return result;
    }

    @Override
    public VisitorResult visitComplexNumberConstantLabel(@NotNull wcpsParser.ComplexNumberConstantLabelContext ctx) {
        //  LEFT_PARENTHESIS REAL_NUMBER_CONSTANT COMMA REAL_NUMBER_CONSTANT RIGHT_PARENTHESIS
        // e.g: (2,5) = 2 + 5i
        String realNumberStr = ctx.REAL_NUMBER_CONSTANT(0).getText();
        String imagineNumberStr = ctx.REAL_NUMBER_CONSTANT(1).getText();

        WcpsResult result = ComplexNumberConstantHandler.handle(realNumberStr, imagineNumberStr);
        return result;
    }

    @Override
    public VisitorResult visitReduceNumericalExpressionLabel(@NotNull wcpsParser.ReduceNumericalExpressionLabelContext ctx) {
        // reduceNumericalExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: count(c + 3), min(c), max(c - 2)
        String operator = ctx.reduceNumericalExpressionOperator().getText();
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = ReduceExpressionHandler.handle(operator, coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitGeneralCondenseExpressionLabel(@NotNull wcpsParser.GeneralCondenseExpressionLabelContext ctx) {
        //   CONDENSE condenseExpressionOperator
        //   OVER axisIterator (COMMA axisIterator)*
        //  (whereClause)?
        //   USING coverageExpression
        // e.g: condense + over $px x(0:100), $py y(0:100) where ( max(c) < 100 ) using c[i($x),j($y)]

        String operator = ctx.condenseExpressionOperator().getText();

        ArrayList<AxisIterator> axisIterators = new ArrayList<AxisIterator>();
        String rasqlAliasName = "";
        String aliasName = "";
        int count = 0;

        // to build the IndexCRS for axis iterator
        int numberOfAxis = axisIterators.size();
        String crsUri = CrsUtil.OPENGIS_INDEX_ND_PATTERN.replace("%d", String.valueOf(numberOfAxis));

        for (wcpsParser.AxisIteratorContext i : ctx.axisIterator()) {
            AxisIterator axisIterator = (AxisIterator) visit(i);
            aliasName = axisIterator.getAliasName();
            if (rasqlAliasName.isEmpty()) {
                rasqlAliasName = aliasName.replace(SubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN, "");
            }
            axisIterator.getSubsetDimension().setCrs(crsUri);
            axisIterator.setRasqlAliasName(rasqlAliasName);
            axisIterator.setAxisIteratorOrder(count);

            // Add the axis iterator to the axis iterators alias registry
            axisIteratorAliasRegistry.addAxisIteratorAliasMapping(aliasName, axisIterator);
            axisIterators.add(axisIterator);
            // the order of axis iterator in the coverage
            count++;
        }

        WcpsResult whereClause = null;
        if (ctx.whereClause() != null) {
            whereClause = (WcpsResult) visit(ctx.whereClause());
        }

        WcpsResult usingExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = GeneralCondenserHandler.handle(operator, axisIterators, whereClause,
                            usingExpr,
                            axisIteratorAliasRegistry,
                            wcpsCoverageMetadataService, rasqlTranslationService, subsetParsingService);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionShorthandTrimLabel(@NotNull wcpsParser.CoverageExpressionShorthandTrimLabelContext ctx) {
        //  coverageExpression LEFT_BRACKET dimensionIntervalList RIGHT_BRACKET
        // e.g: c[Lat(0:20)] - Trim
        DimensionIntervalList dimensionIntList = (DimensionIntervalList) visit(ctx.dimensionIntervalList());
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult wcpsResult = null;
        try {
            wcpsResult = TrimExpressionHandler.handle(coverageExpr, dimensionIntList,
                         axisIteratorAliasRegistry,
                         wcpsCoverageMetadataService, rasqlTranslationService, subsetParsingService);
        } catch (PetascopeException ex) {
            // It cannot fetch the coefficient for the regular axis
            throw new IrregularAxisFetchingFailedException(ex);
        }

        return wcpsResult;
    }

    @Override
    public VisitorResult visitCoverageExpressionTrimCoverageLabel(@NotNull wcpsParser.CoverageExpressionTrimCoverageLabelContext ctx) {
        // TRIM LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE RIGHT_PARENTHESIS
        // e.g: trim(c, {Lat(0:20)})
        DimensionIntervalList dimensionIntList = (DimensionIntervalList) visit(ctx.dimensionIntervalList());
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult wcpsResult = null;
        try {
            wcpsResult = TrimExpressionHandler.handle(coverageExpr, dimensionIntList,
                         axisIteratorAliasRegistry,
                         wcpsCoverageMetadataService, rasqlTranslationService, subsetParsingService);
        } catch (PetascopeException ex) {
            // It cannot fetch the coefficient for the regular axis
            throw new IrregularAxisFetchingFailedException(ex);
        }

        return wcpsResult;
    }


    @Override
    public VisitorResult visitCoverageExpressionShorthandSliceLabel(@NotNull wcpsParser.CoverageExpressionShorthandSliceLabelContext ctx) {
        // coverageExpression LEFT_BRACKET dimensionPointList RIGHT_BRACKET
        // e.g: c[Lat(0)]
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        DimensionIntervalList dimensionIntervalList = (DimensionIntervalList) visit(ctx.dimensionPointList());

        WcpsResult wcpsResult = null;
        try {
            wcpsResult = SliceExpressionHandler.handle(coverageExpr, dimensionIntervalList,
                         axisIteratorAliasRegistry,
                         wcpsCoverageMetadataService, rasqlTranslationService, subsetParsingService);
        } catch (PetascopeException ex) {
            // It cannot fetch the coefficient for the regular axis
            throw new IrregularAxisFetchingFailedException(ex);
        }

        return wcpsResult;
    }

    @Override
    public VisitorResult visitCoverageExpressionSliceLabel(@NotNull wcpsParser.CoverageExpressionSliceLabelContext ctx) {
        // SLICE  LEFT_PARENTHESIS  coverageExpression  COMMA   LEFT_BRACE    dimensionPointList    RIGHT_BRACE   RIGHT_PARENTHESIS
        // e.g: slice(c, Lat(0))
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        DimensionIntervalList dimensionIntervalList = (DimensionIntervalList) visit(ctx.dimensionPointList());

        WcpsResult wcpsResult = null;

        try {
            wcpsResult = SliceExpressionHandler.handle(coverageExpr, dimensionIntervalList,
                         axisIteratorAliasRegistry,
                         wcpsCoverageMetadataService, rasqlTranslationService, subsetParsingService);
        } catch (PetascopeException ex) {
            // It cannot fetch the coefficient for the regular axis
            throw new IrregularAxisFetchingFailedException(ex);
        }

        return wcpsResult;
    }


    @Override
    public VisitorResult visitCoverageConstantExpressionLabel(@NotNull wcpsParser.CoverageConstantExpressionLabelContext ctx) {
        // COVERAGE IDENTIFIER
        // OVER axisIterator (COMMA axisIterator)*
        // VALUE LIST LOWER_THAN   constant (SEMICOLON constant)*   GREATER_THAN
        // e.g: coverage cov over $px x(0:20), $py(0:30) values list<-1,0,1,2,2>
        String identifier = ctx.COVERAGE_VARIABLE_NAME().getText();

        ArrayList<AxisIterator> axisIterators = new ArrayList<AxisIterator>();
        ArrayList<String> constants = new ArrayList<String>();

        // to build the IndexCRS for axis iterator
        int numberOfAxis = axisIterators.size();
        String crsUri = CrsUtil.OPENGIS_INDEX_ND_PATTERN.replace("%d", String.valueOf(numberOfAxis));

        //parse the axis specifications
        for (wcpsParser.AxisIteratorContext i : ctx.axisIterator()) {
            AxisIterator axisIterator = (AxisIterator) visit(i);
            axisIterator.getSubsetDimension().setCrs(crsUri);
            axisIterators.add(axisIterator);
        }

        //parse the constants (e.g: <-1,....1>)
        for (wcpsParser.ConstantContext i : ctx.constant()) {
            constants.add(i.getText());
        }

        WcpsResult result = CoverageConstantHandler.handle(identifier, axisIterators, constants,
                            wcpsCoverageMetadataService, rasqlTranslationService, subsetParsingService);
        return result;
    }


    @Override
    public VisitorResult visitCoverageExpressionExtendLabel(@NotNull wcpsParser.CoverageExpressionExtendLabelContext ctx) {
        // EXTEND LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE RIGHT_PARENTHESIS
        // extend($c, {intervalList})
        // e.g: extend(c[t(0)], {Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)}
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        DimensionIntervalList dimensionIntervalList = (DimensionIntervalList) visit(ctx.dimensionIntervalList());

        WcpsResult wcpsResult = null;

        try {
            wcpsResult = ExtendExpressionHandler.handle(coverageExpr, dimensionIntervalList, wcpsCoverageMetadataService,
                         rasqlTranslationService, subsetParsingService);
        } catch (PetascopeException ex) {
            // It cannot fetch the coefficient for the regular axis
            throw new IrregularAxisFetchingFailedException(ex);
        }

        return wcpsResult;
    }

    @Override
    public VisitorResult visitCoverageExpressionExtendByDomainIntervalsLabel(@NotNull wcpsParser.CoverageExpressionExtendByDomainIntervalsLabelContext ctx) {
        // EXTEND LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE domainIntervals RIGHT_BRACE RIGHT_PARENTHESIS
        // extend($c, {domain() or imageCrsdomain()})
        // e.g: extend(c[t(0)], { imageCrsdomain(Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)) })
        // NOTE: imageCrsdomain() or domain() will return metadata value not Rasql
        WcpsMetadataResult wcpsMetadataResult = (WcpsMetadataResult)visit(ctx.domainIntervals());

        String domainIntervalsRasql = wcpsMetadataResult.getResult().replace("(", "").replace(")", "");
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = ExtendExpressionByDomainIntervalsHandler.handle(coverageExpr, wcpsMetadataResult, domainIntervalsRasql);
        return result;
    }

    @Override
    public VisitorResult visitRangeConstructorExpressionLabel(@NotNull wcpsParser.RangeConstructorExpressionLabelContext ctx) {
        // LEFT_BRACE  (fieldName COLON coverageExpression) (SEMICOLON fieldName COLON coverageExpression)* RIGHT_BRACE
        // NOT used in switch case
        // e.g: {red: c.0, green: c.1, blue: c.2}
        Map<String, WcpsResult> rangeConstructor = new LinkedHashMap();
        // this share same metadata between each range element (e.g: {red:, green:,...}
        for (int i = 0; i < ctx.fieldName().size(); i++) {
            // this is a coverage expression
            WcpsResult wcpsResult = (WcpsResult) visit(ctx.coverageExpression().get(i));
            rangeConstructor.put(ctx.fieldName().get(i).getText(), wcpsResult);
        }

        WcpsResult result = RangeConstructorHandler.handle(rangeConstructor);
        return result;
    }

    @Override
    public VisitorResult visitRangeConstructorSwitchCaseExpressionLabel(@NotNull wcpsParser.RangeConstructorSwitchCaseExpressionLabelContext ctx) {
        // LEFT_BRACE  (fieldName COLON coverageExpression) (SEMICOLON fieldName COLON coverageExpression)*  RIGHT_BRACE
        // USED in switch case
        // e.g: {red: 15, green: 12, blue: 13} is used in switch case
        Map<String, WcpsResult> rangeConstructor = new LinkedHashMap();
        for (int i = 0; i < ctx.fieldName().size(); i++) {
            // this is a scalar value
            WcpsResult wcpsResult = (WcpsResult) visit(ctx.coverageExpression().get(i));
            rangeConstructor.put(ctx.fieldName().get(i).getText(), wcpsResult);
        }

        WcpsResult result = RangeConstructorSwitchCaseHandler.handle(rangeConstructor);
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionRangeConstructorLabel(@NotNull wcpsParser.CoverageExpressionRangeConstructorLabelContext ctx) {
        // rangeConstructorExpression
        WcpsResult result = (WcpsResult)visit(ctx.rangeConstructorExpression());
        return result;
    }


    @Override
    public VisitorResult visitScalarValueCoverageExpressionLabel(@NotNull wcpsParser.ScalarValueCoverageExpressionLabelContext ctx) {
        // scalarValueCoverageExpression: (LEFT_PARENTHESIS)?  coverageExpression (RIGHT_PARENTHESIS)?
        // e.g: for $c in (mr) return (c[i(0), j(0)] = 25 + 30 - 50)
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = coverageExpr;
        return result;
    }

    @Override
    public VisitorResult visitStringScalarExpressionLabel(@NotNull wcpsParser.StringScalarExpressionLabelContext ctx) {
        // STRING_LITERAL
        // e.g: 1, c, x, y
        String str = ctx.STRING_LITERAL().getText();

        WcpsResult result = StringScalarHandler.handle(str);
        return result;
    }

    @Override
    public VisitorResult visitCoverageIdentifierExpressionLabel(@NotNull wcpsParser.CoverageIdentifierExpressionLabelContext ctx) {
        // IDENTIFIER LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // identifier(), e.g: identifier($c) -> mr
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsMetadataResult result = CoverageIdentifierHandler.handle(coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitCoverageCrsSetExpressionLabel(@NotNull wcpsParser.CoverageCrsSetExpressionLabelContext ctx) {
        // CRSSET LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // crsSet(), e.g: crsSet($c) -> Lat:"http://...4326",  "CRS:1",
        //                              Long:"http://...4326", "CRS:1"

        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsMetadataResult result = CoverageCrsSetHandler.handle(coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitStarExpressionLabel(@NotNull wcpsParser.StarExpressionLabelContext ctx) {
        // MULTIPLICATION
        // e.g: c[Lat(*)]
        WcpsResult result = StringScalarHandler.handle("\"" + ASTERISK + "\"");
        return result;
    }

    @Override
    public VisitorResult visitCoverageExpressionScaleLabel(@NotNull wcpsParser.CoverageExpressionScaleLabelContext ctx) {
        // SCALE LEFT_PARENTHESIS
        //          coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE (COMMA fieldInterpolationList)*
        //       RIGHT_PARENTHESIS
        // scale($c, {intervalList})
        // e.g: scale(c[t(0)], {Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)}

        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());
        DimensionIntervalList dimensionIntervalList = (DimensionIntervalList) visit(ctx.dimensionIntervalList());

        WcpsResult wcpsResult = null;

        try {
            wcpsResult = ScaleExpressionHandler.handle(coverageExpr, dimensionIntervalList,
                         wcpsCoverageMetadataService, rasqlTranslationService, subsetParsingService);
        } catch (PetascopeException ex) {
            // It cannot fetch the coefficient for the regular axis
            throw new IrregularAxisFetchingFailedException(ex);
        }

        return wcpsResult;
    }

    @Override
    public VisitorResult visitCoverageExpressionScaleByDomainIntervalsLabel(@NotNull wcpsParser.CoverageExpressionScaleByDomainIntervalsLabelContext ctx) {
        // SCALE LEFT_PARENTHESIS
        //        coverageExpression COMMA LEFT_BRACE domainIntervals RIGHT_BRACE
        // RIGHT_PARENTHESIS
        // scale($c, { imageCrsDomain() or domain() }) - domain() can only be 1D
        // e.g: scale(c[t(0)], { imageCrsdomain(Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)) })
        // NOTE: imageCrsdomain() or domain() will return metadata value not Rasql
        WcpsMetadataResult wcpsMetadataResult = (WcpsMetadataResult)visit(ctx.domainIntervals());
        String domainIntervalsRasql = wcpsMetadataResult.getResult().replace("(", "").replace(")", "");
        WcpsResult coverageExpr = (WcpsResult) visit(ctx.coverageExpression());

        WcpsResult result = ScaleExpressionByDomainIntervalsHandler.handle(coverageExpr, wcpsMetadataResult, domainIntervalsRasql);
        return result;
    }

    @Override
    public VisitorResult visitImageCrsExpressionLabel(@NotNull wcpsParser.ImageCrsExpressionLabelContext ctx) {
        // IMAGECRS LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // imageCrs() - return coverage's grid axis
        // e.g: for c in (mr) return imageCrs(c) (imageCrs is the grid CRS of coverage)
        // return: CRS:1
        WcpsResult coverageExpr = (WcpsResult)visit(ctx.coverageExpression());

        WcpsMetadataResult result = ImageCrsExpressionHandler.handle(coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitImageCrsDomainExpressionLabel(@NotNull wcpsParser.ImageCrsDomainExpressionLabelContext ctx) {
        // IMAGECRSDOMAIN LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // imageCrsDomain($c) - can be 2D, 3D,.. depend on coverageExpression
        // e.g: c[t(0), Lat(0:20), Long(0:30)] is 2D
        // return (0:5,0:100,0:231), used with scale, extend (scale(, { imageCrsdomain() })
        WcpsResult coverageExpr = (WcpsResult)visit(ctx.coverageExpression());

        WcpsMetadataResult result = ImageCrsDomainExpressionHandler.handle(coverageExpr);
        return result;
    }

    @Override
    public VisitorResult visitImageCrsDomainByDimensionExpressionLabel(@NotNull wcpsParser.ImageCrsDomainByDimensionExpressionLabelContext ctx) {
        // IMAGECRSDOMAIN LEFT_PARENTHESIS coverageExpression COMMA axisName RIGHT_PARENTHESIS
        // imageCrsDomain($c, axisName) - 1D
        // return (0:5), used with axis iterator ($px x ( imageCrsdomain() ))
        WcpsResult coverageExpr = (WcpsResult)visit(ctx.coverageExpression());
        String axisName = ctx.axisName().getText();

        WcpsMetadataResult result = ImageCrsDomainExpressionByDimensionExpressionHandler.handle(coverageExpr, axisName);
        return result;
    }

    @Override
    public VisitorResult visitDomainExpressionLabel(@NotNull wcpsParser.DomainExpressionLabelContext ctx) {
        // IMAGECRSDOMAIN LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // domain() - 1D
        // e.g: for c in (mean_summer_airtemp) return domain(c[Lat(0:20)], Lat, "http://.../4326")
        // return: (0:20) as domain of inpurt coverage in Lat is 0:20
        WcpsResult coverageExpr = (WcpsResult)visit(ctx.coverageExpression());
        String axisName =  ctx.axisName().getText();
        // NOTE: need to strip bounding quotes of crs (e.g: ""http://.../4326"")
        String crsName = CrsUtility.stripBoundingQuotes(ctx.crsName().getText());

        WcpsMetadataResult result = DomainExpressionHandler.handle(coverageExpr, axisName, crsName);
        return result;
    }

    @Override
    public VisitorResult visitSwitchCaseRangeConstructorExpressionLabel(@NotNull wcpsParser.SwitchCaseRangeConstructorExpressionLabelContext ctx) {
        // switch case which returns range constructor
        // e.g: for c in (mr) return encode(
        //        switch case c > 1000 return {red: 107; green:17; blue:68}
        //               default return {red: 150; green:103; blue:14}
        //        , "png")

        List<WcpsResult> booleanResults = new ArrayList<WcpsResult>();
        List<WcpsResult> rangeResults = new ArrayList<WcpsResult>();

        List<RangeField> firstRangeFields = new ArrayList<RangeField>();

        // cases return
        for (int i = 0; i < ctx.CASE().size(); i++) {
            // Handle each rangeConstructor (case)
            WcpsResult wcpsBooleanExpressionResult = (WcpsResult) visit(ctx.booleanSwitchCaseCombinedExpression().get(i));
            WcpsResult wcpsRangeConstructorResult = (WcpsResult) visit(ctx.rangeConstructorSwitchCaseExpression().get(i));

            List<RangeField> rangeFields = wcpsRangeConstructorResult.getMetadata().getRangeFields();

            if (firstRangeFields.isEmpty()) {
                firstRangeFields.addAll(rangeFields);
            } else {
                // validate range fields list
                RangeFieldService.validateRangeFields(firstRangeFields, rangeFields);
            }

            booleanResults.add(wcpsBooleanExpressionResult);
            rangeResults.add(wcpsRangeConstructorResult);
        }

        // default return also returns a range constructor (cases size = ranges size - 1)
        int casesSize = ctx.CASE().size();
        WcpsResult wcpsDefaultRangeConstructorResult = (WcpsResult) visit(ctx.rangeConstructorSwitchCaseExpression().get(casesSize));
        List<RangeField> rangeFields = wcpsDefaultRangeConstructorResult.getMetadata().getRangeFields();
        // check if the next case expression has the same band names and band numbers
        RangeFieldService.validateRangeFields(firstRangeFields, rangeFields);
        rangeResults.add(wcpsDefaultRangeConstructorResult);

        WcpsResult result = SwitchCaseRangeConstructorExpression.handle(booleanResults, rangeResults);
        return result;
    }

    @Override
    public VisitorResult visitSwitchCaseScalarValueExpressionLabel(@NotNull wcpsParser.SwitchCaseScalarValueExpressionLabelContext ctx) {
        // switch case which returns scalar value (mostly is numerical)
        // e.g: e.g: for c in (mr) return encode(
        //               switch case c > 10 and c < 20 return (char)5
        //               default return 2
        //           ,"csv")
        List<WcpsResult> booleanResults = new ArrayList<WcpsResult>();
        List<WcpsResult> scalarResults = new ArrayList<WcpsResult>();

        for (int i = 0; i < ctx.CASE().size(); i++) {
            // Handle each rangeConstructor (case)
            WcpsResult wcpsBooleanExpressionResult = (WcpsResult) visit(ctx.booleanSwitchCaseCombinedExpression().get(i));
            WcpsResult wcpsScalarValueResult = (WcpsResult) visit(ctx.scalarValueCoverageExpression().get(i));

            booleanResults.add(wcpsBooleanExpressionResult);
            scalarResults.add(wcpsScalarValueResult);
        }

        // default return also returns a range constructor (cases size = ranges size - 1)
        int casesSize = ctx.CASE().size();
        WcpsResult wcpsRangeConstructorResult = (WcpsResult) visit(ctx.scalarValueCoverageExpression().get(casesSize));
        scalarResults.add(wcpsRangeConstructorResult);

        WcpsResult result = SwitchCaseScalarValueExpression.handle(booleanResults, scalarResults);
        return result;
    }

    // PARAMETERS
    /* ----------- Parameters objects for nodes ----------- */
    @Override
    public VisitorResult visitDimensionPointElementLabel(@NotNull wcpsParser.DimensionPointElementLabelContext ctx) {
        // axisName (COLON crsName)? LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: i(5) - Slicing point
        String axisName = ctx.axisName().getText();
        String crs = null;
        if (ctx.crsName() != null) {
            crs = ctx.crsName().getText().replace("\"", "");
        }
        String bound = ctx.coverageExpression().getText();

        SliceSubsetDimension sliceSubsetDimension = new SliceSubsetDimension(axisName, crs, bound);
        return sliceSubsetDimension;
    }

    @Override
    public VisitorResult visitDimensionPointListLabel(@NotNull wcpsParser.DimensionPointListLabelContext ctx) {
        // dimensionPointElement (COMMA dimensionPointElement)*
        // e.g: i(0), j(0) - List of Slicing points
        List<SubsetDimension> intervalList = new ArrayList<SubsetDimension>(ctx.dimensionPointElement().size());
        for (wcpsParser.DimensionPointElementContext elem : ctx.dimensionPointElement()) {
            intervalList.add((SubsetDimension) visit(elem));
        }

        DimensionIntervalList dimensionIntervalList = new DimensionIntervalList(intervalList);
        return dimensionIntervalList;
    }

    @Override
    public VisitorResult visitSliceDimensionIntervalElementLabel(@NotNull wcpsParser.SliceDimensionIntervalElementLabelContext ctx) {
        // axisName (COLON crsName)?  LEFT_PARENTHESIS   coverageExpression   RIGHT_PARENTHESIS
        // e.g: i(0)
        String bound = ctx.coverageExpression().getText();
        String crs = ctx.crsName() == null ? "" : ctx.crsName().getText().replace("\"", "");

        SliceSubsetDimension sliceSubsetDimension = null;
        try {
            sliceSubsetDimension = new SliceSubsetDimension(ctx.axisName().getText(), crs, bound);
        } catch (InvalidSlicingException ex) {
            throw ex;
        }

        return sliceSubsetDimension;
    }

    @Override
    public VisitorResult visitTrimDimensionIntervalElementLabel(@NotNull wcpsParser.TrimDimensionIntervalElementLabelContext ctx) {
        // axisName (COLON crsName)? LEFT_PARENTHESIS  coverageExpression   COLON coverageExpression    RIGHT_PARENTHESIS
        // e.g: i:"CRS:1"(2:3)
        try {
            String rawLowerBound = ctx.coverageExpression(0).getText();
            String rawUpperBound = ctx.coverageExpression(1).getText();
            String crs = null;
            if (ctx.crsName() != null) {
                crs = ctx.crsName().getText().replace("\"", "");
            }
            if (ctx.axisName() == null) {
                throw new InvalidAxisNameException("No axis given");
            }
            String axisName = ctx.axisName().getText();

            TrimSubsetDimension trimSubsetDimension = new TrimSubsetDimension(axisName, crs, rawLowerBound, rawUpperBound);

            return trimSubsetDimension;
        } catch (NumberFormatException e) {
            throw new InvalidSubsettingException(ctx.axisName().getText(), new ParsedSubset(ctx.coverageExpression(0).getText(), ctx.coverageExpression(1).getText()));
        }
    }

    @Override
    public VisitorResult visitDimensionIntervalListLabel(@NotNull wcpsParser.DimensionIntervalListLabelContext ctx) {
        // dimensionIntervalElement (COMMA dimensionIntervalElement)*
        // e.g: c[i(0:20),j(0:30)]
        List<SubsetDimension> intervalList = new ArrayList<SubsetDimension>();
        for (wcpsParser.DimensionIntervalElementContext elem : ctx.dimensionIntervalElement()) {
            intervalList.add((SubsetDimension) visit(elem));
        }
        DimensionIntervalList dimensionIntervalList = new DimensionIntervalList(intervalList);
        return dimensionIntervalList;
    }

    @Override
    public VisitorResult visitIntervalExpressionLabel(@NotNull wcpsParser.IntervalExpressionLabelContext ctx) {
        // scalarExpression COLON scalarExpression  (e.g: 5:10)
        String lowIntervalStr = ctx.scalarExpression(0).getText();
        String highIntervalStr = ctx.scalarExpression(1).getText();

        IntervalExpression intervalExpression = new IntervalExpression(lowIntervalStr, highIntervalStr);
        return intervalExpression;
    }

    @Override
    public VisitorResult visitAxisSpecLabel(@NotNull wcpsParser.AxisSpecLabelContext ctx) {
        // dimensionIntervalElement (e.g: i(0:20) or j:"CRS:1"(0:30))
        SubsetDimension subsetDimension = (SubsetDimension) visit(ctx.dimensionIntervalElement());

        AxisSpec axisSpec = new AxisSpec(subsetDimension);
        return axisSpec;
    }

    @Override
    public VisitorResult visitAxisIteratorLabel(@NotNull wcpsParser.AxisIteratorLabelContext ctx) {
        // coverageVariableName dimensionIntervalElement (e.g: $px x(Lat(0:20)) )
        SubsetDimension subsetDimension = (SubsetDimension) visit(ctx.dimensionIntervalElement());
        String coverageVariableName = ctx.coverageVariableName().getText();

        AxisIterator axisIterator = new AxisIterator(coverageVariableName, subsetDimension);
        return axisIterator;
    }

    @Override
    public VisitorResult visitAxisIteratorDomainIntervalsLabel(@NotNull wcpsParser.AxisIteratorDomainIntervalsLabelContext ctx) {
        // coverageVariableName axisName LEFT_PARENTHESIS  domainIntervals RIGHT_PARENTHESIS
        // e.g: $px x (imageCrsdomain(c[Lat(0:20)]), Lat)
        // e.g: $px x (imageCrsdomain(c[Long(0)], Lat[(0:20)]))
        // e.g: $px x (domain(c[Lat(0:20)], Lat, "http://.../4326"))
        // return x in (50:80)

        WcpsMetadataResult wcpsMetadataResult = (WcpsMetadataResult)visit(ctx.domainIntervals());

        // NOTE: it expects that "domainIntervals" will only return 1 trimming domain in this case (so only 1D)
        SubsetDimension trimSubsetDimension = null;

        String coverageVariableName = ctx.coverageVariableName().getText();
        int axesBBoxSize = wcpsMetadataResult.getMetadata().getAxesBBox().size();

        if (axesBBoxSize > 1) {
            throw new InvalidDomainIntervalsForAxisIteratorException(coverageVariableName, axesBBoxSize);
        } else {
            // Only has 1 D domain, then it is valid to create a TrimSubsetDimension from the metadata result
            trimSubsetDimension = rasqlTranslationService.constructRasqlDomainFromWcpsMetadataDomainInterval(wcpsMetadataResult);
        }

        AxisIterator axisIterator = new AxisIterator(coverageVariableName, trimSubsetDimension);
        return axisIterator;
    }


    // ULTILITY
    /* ----------------- Ultility Handlers------------------ */

    public String getMimeType() {
        // Get the mimeType for one WCPS query
        return mimeType;
    }

    // store the alias: e.g: $c -> (mr, rgb), $d -> (mr1, rgb1) which is used for handling multipart
    public CoverageAliasRegistry getCoverageAliasRegistry() {
        return this.coverageAliasRegistry;
    }
}