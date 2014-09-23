package petascope.wcps2.parser;

import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import petascope.wcps2.error.managed.processing.InvalidAxisNameException;
import petascope.wcps2.error.managed.processing.InvalidSubsettingException;
import petascope.wcps2.metadata.CoverageRegistry;
import petascope.wcps2.metadata.Interval;
import petascope.wcps2.translator.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that implements the parsing rules described in wcps.g4
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class wcpsEvaluator extends wcpsBaseVisitor<IParseTreeNode> {

    public wcpsEvaluator(CoverageRegistry coverageRegistry) {
        super();
        this.coverageRegistry = coverageRegistry;
    }

    @Override
    public IParseTreeNode visitWcpsQueryLabel(@NotNull wcpsParser.WcpsQueryLabelContext ctx) {
        IParseTreeNode forClauseList = visit(ctx.forClauseList());
        //only visit the for clause if it exists
        IParseTreeNode whereClause = null;
        if (ctx.whereClause() != null) {
            whereClause = visit(ctx.whereClause());
        }
        IParseTreeNode returnClause = visit(ctx.returnClause());
        return new WcpsQuery(forClauseList, whereClause, returnClause);
    }

    @Override
    public IParseTreeNode visitForClauseLabel(@NotNull wcpsParser.ForClauseLabelContext ctx) {
        return new ForClause(ctx.coverageVariableName().getText(), ctx.IDENTIFIER().get(0).getText(), coverageRegistry);
    }

    @Override
    public IParseTreeNode visitForClauseListLabel(@NotNull wcpsParser.ForClauseListLabelContext ctx) {
        ArrayList<IParseTreeNode> forClauses = new ArrayList<IParseTreeNode>();
        for (wcpsParser.ForClauseContext currentClause : ctx.forClause()) {
            forClauses.add(visit(currentClause));
        }
        return new ForClauseList(forClauses);
    }

    @Override
    public IParseTreeNode visitReturnClauseLabel(@NotNull wcpsParser.ReturnClauseLabelContext ctx) {
        return new ReturnClause(visit(ctx.processingExpression()));
    }

    @Override
    public IParseTreeNode visitEncodedCoverageExpressionLabel(@NotNull wcpsParser.EncodedCoverageExpressionLabelContext ctx) {
        IParseTreeNode coverageExpression = visit(ctx.coverageExpression());
        String format = ctx.FORMAT_NAME().getText();
        String otherParams = null;
        if (ctx.STRING_LITERAL() != null) {
            otherParams = ctx.STRING_LITERAL().getText();
        }
        return new EncodedCoverage(coverageExpression, format, otherParams);
    }

    @Override
    public IParseTreeNode visitCoverageExpressionVariableNameLabel(@NotNull wcpsParser.CoverageExpressionVariableNameLabelContext ctx) {
        String coverageVariable = ctx.coverageVariableName().getText();
        return new CoverageExpressionVariableName(coverageVariable, coverageRegistry);
    }

    @Override
    public IParseTreeNode visitCoverageExpressionLogicLabel(@NotNull wcpsParser.CoverageExpressionLogicLabelContext ctx) {
        return new BinaryCoverageExpression(visit(ctx.coverageExpression(0)), ctx.booleanOperator().getText(), visit(ctx.coverageExpression(1)));
    }

    @Override
    public IParseTreeNode visitCoverageExpressionArithmeticLabel(@NotNull wcpsParser.CoverageExpressionArithmeticLabelContext ctx) {
        return new BinaryCoverageExpression(visit(ctx.coverageExpression(0)), ctx.coverageArithmeticOperator().getText(), visit(ctx.coverageExpression(1)));
    }

    @Override
    public IParseTreeNode visitBooleanConstant(@NotNull wcpsParser.BooleanConstantContext ctx) {
        return new BooleanConstant(ctx.getText());
    }

    @Override
    public IParseTreeNode visitBooleanStringComparisonScalar(@NotNull wcpsParser.BooleanStringComparisonScalarContext ctx) {
        return new BinaryScalarExpression(ctx.stringScalarExpression(0).getText(), ctx.stringOperator().getText(), ctx.stringScalarExpression(1).getText());
    }

    @Override
    public IParseTreeNode visitIntervalExpressionLabel(@NotNull wcpsParser.IntervalExpressionLabelContext ctx) {
        return new IntervalExpression(ctx.scalarExpression(0).getText(), ctx.scalarExpression(1).getText());
    }

    @Override
    public IParseTreeNode visitAxisSpecLabel(@NotNull wcpsParser.AxisSpecLabelContext ctx) {
        return new AxisSpec(ctx.axisName().getText(), (IntervalExpression) visit(ctx.intervalExpression()));
    }

    @Override
    public IParseTreeNode visitAxisIteratorLabel(@NotNull wcpsParser.AxisIteratorLabelContext ctx) {
        return new AxisIterator(ctx.IDENTIFIER().getText(), ctx.axisName().getText(), (IntervalExpression) visit(ctx.intervalExpression()));
    }

    @Override
    public IParseTreeNode visitCoverageConstructorExpressionLabel(@NotNull wcpsParser.CoverageConstructorExpressionLabelContext ctx) {
        ArrayList<AxisIterator> intervalList = new ArrayList<AxisIterator>();
        for (wcpsParser.AxisIteratorContext i : ctx.axisIterator()) {
            intervalList.add((AxisIterator) visit(i));
        }
        return new CoverageConstructor(ctx.IDENTIFIER().getText(), intervalList, visit(ctx.scalarExpression()));
    }

    @Override
    public IParseTreeNode visitUnaryCoverageArithmeticExpressionLabel(@NotNull wcpsParser.UnaryCoverageArithmeticExpressionLabelContext ctx) {
        return new UnaryArithmeticExpression(ctx.unaryArithmeticExpressionOperator().getText(), visit(ctx.coverageExpression()));
    }

    @Override
    public IParseTreeNode visitTrigonometricExpressionLabel(@NotNull wcpsParser.TrigonometricExpressionLabelContext ctx) {
        return new UnaryArithmeticExpression(ctx.trigonometricOperator().getText(), visit(ctx.coverageExpression()));
    }

    @Override
    public IParseTreeNode visitExponentialExpressionLabel(@NotNull wcpsParser.ExponentialExpressionLabelContext ctx) {
        return new UnaryArithmeticExpression(ctx.exponentialExpressionOperator().getText(), visit(ctx.coverageExpression()));
    }

    @Override
    public IParseTreeNode visitNotUnaryBooleanExpressionLabel(@NotNull wcpsParser.NotUnaryBooleanExpressionLabelContext ctx) {
        return new UnaryBooleanExpression(visit(ctx.coverageExpression()));
    }

    @Override
    public IParseTreeNode visitBitUnaryBooleanExpressionLabel(@NotNull wcpsParser.BitUnaryBooleanExpressionLabelContext ctx) {
        return new UnaryBooleanExpression(visit(ctx.coverageExpression()), visit(ctx.numericalScalarExpression()));
    }

    @Override
    public IParseTreeNode visitCastExpressionLabel(@NotNull wcpsParser.CastExpressionLabelContext ctx) {
        return new CastExpression(StringUtils.join(ctx.rangeType().IDENTIFIER(), " "), visit(ctx.coverageExpression()));
    }

    @Override
    public IParseTreeNode visitCoverageExpressionRangeSubsettingLabel(@NotNull wcpsParser.CoverageExpressionRangeSubsettingLabelContext ctx) {
        return new RangeSubsetting(ctx.fieldName().getText(), (CoverageExpression) visit(ctx.coverageExpression()));
    }

    @Override
    public IParseTreeNode visitNumericalRealNumberExpressionLabel(@NotNull wcpsParser.NumericalRealNumberExpressionLabelContext ctx) {
        return new RealNumberConstant(ctx.getText());
    }

    @Override
    public IParseTreeNode visitCoverageExpressionCoverageLabel(@NotNull wcpsParser.CoverageExpressionCoverageLabelContext ctx) {
        return new ParenthesesCoverageExpression(visit(ctx.coverageExpression()));
    }

    @Override
    public IParseTreeNode visitWhereClauseLabel(@NotNull wcpsParser.WhereClauseLabelContext ctx) {
        return new WhereClause(visit(ctx.booleanScalarExpression()));
    }

    @Override
    public IParseTreeNode visitBooleanUnaryScalarLabel(@NotNull wcpsParser.BooleanUnaryScalarLabelContext ctx) {
        return new BooleanUnaryScalarExpression(ctx.booleanUnaryOperator().getText(), visit(ctx.booleanScalarExpression()));
    }

    @Override
    public IParseTreeNode visitBooleanNumericalComparisonScalarLabel(@NotNull wcpsParser.BooleanNumericalComparisonScalarLabelContext ctx) {
        return new BooleanNumericalComparissonScalar(visit(ctx.numericalScalarExpression(0)), visit(ctx.numericalScalarExpression(1)), ctx.numericalComparissonOperator().getText());
    }

    @Override
    public IParseTreeNode visitReduceBooleanExpressionLabel(@NotNull wcpsParser.ReduceBooleanExpressionLabelContext ctx) {
        return new ReduceExpression(ctx.reduceBooleanExpressionOperator().getText(), visit(ctx.coverageExpression()));
    }

    @Override
    public IParseTreeNode visitCoverageExpressionComparissonLabel(@NotNull wcpsParser.CoverageExpressionComparissonLabelContext ctx) {
        return new BinaryCoverageExpression(visit(ctx.coverageExpression(0)), ctx.numericalComparissonOperator().getText(), visit(ctx.coverageExpression(1)));
    }

    @Override
    public IParseTreeNode visitBooleanBinaryScalarLabel(@NotNull wcpsParser.BooleanBinaryScalarLabelContext ctx) {
        return new BinaryCoverageExpression(visit(ctx.booleanScalarExpression(0)), ctx.booleanOperator().getText(), visit(ctx.booleanScalarExpression(1)));
    }

    @Override
    public IParseTreeNode visitNumericalUnaryScalarExpressionLabel(@NotNull wcpsParser.NumericalUnaryScalarExpressionLabelContext ctx) {
        return new UnaryArithmeticExpression(ctx.numericalUnaryOperation().getText(), visit(ctx.numericalScalarExpression()));
    }

    @Override
    public IParseTreeNode visitNumericalTrigonometricScalarExpressionLabel(@NotNull wcpsParser.NumericalTrigonometricScalarExpressionLabelContext ctx) {
        return new UnaryArithmeticExpression(ctx.trigonometricOperator().getText(), visit(ctx.numericalScalarExpression()));
    }

    @Override
    public IParseTreeNode visitNumericalBinaryScalarExpressionLabel(@NotNull wcpsParser.NumericalBinaryScalarExpressionLabelContext ctx) {
        return new BinaryCoverageExpression(visit(ctx.numericalScalarExpression(0)), ctx.numericalOperator().getText(), visit(ctx.numericalScalarExpression(1)));
    }

    @Override
    public IParseTreeNode visitComplexNumberConstantLabel(@NotNull wcpsParser.ComplexNumberConstantLabelContext ctx) {
        return new ComplexNumberConstant(ctx.REAL_NUMBER_CONSTANT(0).getText(), ctx.REAL_NUMBER_CONSTANT(1).getText());
    }

    @Override
    public IParseTreeNode visitReduceNumericalExpressionLabel(@NotNull wcpsParser.ReduceNumericalExpressionLabelContext ctx) {
        return new ReduceExpression(ctx.reduceNumericalExpressionOperator().getText(), visit(ctx.coverageExpression()));
    }

    @Override
    public IParseTreeNode visitGeneralCondenseExpressionLabel(@NotNull wcpsParser.GeneralCondenseExpressionLabelContext ctx) {
        ArrayList<IParseTreeNode> intervalList = new ArrayList<IParseTreeNode>();
        for (wcpsParser.AxisIteratorContext i : ctx.axisIterator()) {
            intervalList.add(visit(i));
        }
        IParseTreeNode whereClause = null;
        if (ctx.booleanScalarExpression() != null) {
            whereClause = visit(ctx.booleanScalarExpression());
        }
        return new GeneralCondenser(ctx.condenseExpressionOperator().getText(), intervalList, whereClause, visit(ctx.scalarExpression()));
    }

    @Override
    public IParseTreeNode visitTrimDimensionIntervalElementLabel(@NotNull wcpsParser.TrimDimensionIntervalElementLabelContext ctx) {
        try {
            String rawLowerBound = visit(ctx.scalarExpression(0)).toRasql();
            String rawUpperBound = visit(ctx.scalarExpression(1)).toRasql();
            String crs = null;
            if (ctx.crsName() != null) {
                crs = ctx.crsName().getText().replace("\"", "");
            }
            if (ctx.axisName() == null) {
                throw new InvalidAxisNameException("No axis given");
            }
            return new TrimDimensionInterval(ctx.axisName().getText(), crs, rawLowerBound, rawUpperBound);
        } catch (NumberFormatException e) {
            throw new InvalidSubsettingException(ctx.axisName().getText(), new Interval<String>(ctx.scalarExpression(0).getText(), ctx.scalarExpression(1).getText()));
        }
    }

    @Override
    public IParseTreeNode visitDimensionIntervalListLabel(@NotNull wcpsParser.DimensionIntervalListLabelContext ctx) {
        List<TrimDimensionInterval> intervalList = new ArrayList<TrimDimensionInterval>(ctx.dimensionIntervalElement().size());
        for (wcpsParser.DimensionIntervalElementContext elem : ctx.dimensionIntervalElement()) {
            intervalList.add((TrimDimensionInterval) visit(elem));
        }
        return new DimensionIntervalList(intervalList);
    }

    @Override
    public IParseTreeNode visitCoverageExpressionShorthandTrimLabel(@NotNull wcpsParser.CoverageExpressionShorthandTrimLabelContext ctx) {
        DimensionIntervalList dimensionIntList = (DimensionIntervalList) visit(ctx.dimensionIntervalList());
        return new TrimExpression(visit(ctx.coverageExpression()), dimensionIntList);
    }

    @Override
    public IParseTreeNode visitCoverageConstantExpressionLabel(@NotNull wcpsParser.CoverageConstantExpressionLabelContext ctx) {
        ArrayList<AxisIterator> axisIterators = new ArrayList<AxisIterator>();
        ArrayList<String> constants = new ArrayList<String>();
        //parse the axis specifications
        for (wcpsParser.AxisIteratorContext i : ctx.axisIterator()) {
            axisIterators.add((AxisIterator) visit(i));
        }
        //parse the constants
        for (wcpsParser.ConstantContext i : ctx.constant()) {
            constants.add(i.getText());
        }
        return new CoverageConstant(ctx.IDENTIFIER().getText(), axisIterators, constants);
    }


    @Override
    public IParseTreeNode visitCoverageExpressionExtendLabel(@NotNull wcpsParser.CoverageExpressionExtendLabelContext ctx) {
        return new ExtendExpression((CoverageExpression) visit(ctx.coverageExpression()), (DimensionIntervalList) visit(ctx.dimensionIntervalList()));
    }

    @Override
    public IParseTreeNode visitRangeConstructorExpressionLabel(@NotNull wcpsParser.RangeConstructorExpressionLabelContext ctx) {
        Map<String, CoverageExpression> constructor = new HashMap<String, CoverageExpression>();
        for (int i = 0; i < ctx.fieldName().size(); i++) {
            constructor.put(ctx.fieldName().get(i).getText(), (CoverageExpression) visit(ctx.coverageExpression().get(i)));
        }
        return new RangeConstructorExpression(constructor);
    }

    @Override
    public IParseTreeNode visitCoverageExpressionRangeConstructorLabel(@NotNull wcpsParser.CoverageExpressionRangeConstructorLabelContext ctx) {
        return visit(ctx.rangeConstructorExpression());
    }

    @Override
    public IParseTreeNode visitDimensionPointElementLabel(@NotNull wcpsParser.DimensionPointElementLabelContext ctx) {
        return new TrimDimensionInterval(ctx.axisName().getText(), ctx.crsName().getText().replace("\"", ""), ctx.dimensionPointExpression().getText(), ctx.dimensionPointExpression().getText());
    }

    @Override
    public IParseTreeNode visitDimensionPointListLabel(@NotNull wcpsParser.DimensionPointListLabelContext ctx) {
        List<TrimDimensionInterval> intervalList = new ArrayList<TrimDimensionInterval>(ctx.dimensionPointElement().size());
        for (wcpsParser.DimensionPointElementContext elem : ctx.dimensionPointElement()) {
            intervalList.add((TrimDimensionInterval) visit(elem));
        }
        return new DimensionIntervalList(intervalList);
    }

    @Override
    public IParseTreeNode visitSliceDimensionIntervalElementLabel(@NotNull wcpsParser.SliceDimensionIntervalElementLabelContext ctx) {
        String bound = ctx.scalarExpression().getText();
        String crs = ctx.crsName() == null ? "" : ctx.crsName().getText().replace("\"", "");
        return new TrimDimensionInterval(ctx.axisName().getText(), crs, bound, bound);
    }

    @Override
    public IParseTreeNode visitCoverageExpressionShorthandSliceLabel(@NotNull wcpsParser.CoverageExpressionShorthandSliceLabelContext ctx) {
        return new TrimExpression(visit(ctx.coverageExpression()), (DimensionIntervalList) visit(ctx.dimensionPointList()));
    }

    @Override
    public IParseTreeNode visitStringScalarExpressionLabel(@NotNull wcpsParser.StringScalarExpressionLabelContext ctx) {
        return new StringScalar(ctx.STRING_LITERAL().getText());
    }

    @Override
    public IParseTreeNode visitStarExpressionLabel(@NotNull wcpsParser.StarExpressionLabelContext ctx) {
        return new StringScalar("\"*\"");
    }

    @Override
    public IParseTreeNode visitCoverageExpressionScaleLabel(@NotNull wcpsParser.CoverageExpressionScaleLabelContext ctx) {
        return new ScaleExpression((CoverageExpression) visit(ctx.coverageExpression()), (DimensionIntervalList) visit(ctx.dimensionIntervalList()));
    }

    @Override
    public IParseTreeNode visitCoverageExpressionSliceLabel(@NotNull wcpsParser.CoverageExpressionSliceLabelContext ctx) {
        return new TrimExpression(visit(ctx.coverageExpression()), (DimensionIntervalList) visit(ctx.dimensionPointList()));
    }

    private CoverageRegistry coverageRegistry;
}