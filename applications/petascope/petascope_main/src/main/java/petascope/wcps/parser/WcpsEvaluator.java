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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.parser;

import petascope.wcps.metadata.service.CrsUtility;
import petascope.wcps.metadata.service.AxisIteratorAliasRegistry;
import petascope.wcps.handler.SubsetExpressionHandler;
import petascope.wcps.handler.BinaryScalarExpressionHandler;
import petascope.wcps.handler.CoverageVariableNameHandler;
import petascope.wcps.handler.ImageCrsDomainExpressionByDimensionExpressionHandler;
import petascope.wcps.handler.ForClauseHandler;
import petascope.wcps.handler.ComplexNumberConstantHandler;
import petascope.wcps.handler.ImageCrsDomainExpressionHandler;
import petascope.wcps.handler.WcsScaleExpressionByScaleAxesHandler;
import petascope.wcps.handler.ImageCrsExpressionHandler;
import petascope.wcps.handler.RootHandler;
import petascope.wcps.handler.UnaryBooleanExpressionHandler;
import petascope.wcps.handler.BooleanNumericalComparisonScalarHandler;
import petascope.wcps.handler.ExtendExpressionHandler;
import petascope.wcps.handler.CoverageConstructorHandler;
import petascope.wcps.handler.CoverageCrsSetHandler;
import petascope.wcps.handler.RangeConstructorHandler;
import petascope.wcps.handler.UnaryPowerExpressionHandler;
import petascope.wcps.handler.RangeSubsettingHandler;
import petascope.wcps.handler.EncodeCoverageHandler;
import petascope.wcps.handler.ReturnClauseHandler;
import petascope.wcps.handler.GeneralCondenserHandler;
import petascope.wcps.handler.WcsScaleExpressionByScaleSizeHandler;
import petascope.wcps.handler.DomainExpressionHandler;
import petascope.wcps.handler.ReduceExpressionHandler;
import petascope.wcps.handler.CrsTransformHandler;
import petascope.wcps.handler.WcsScaleExpressionByFactorHandler;
import petascope.wcps.handler.ParenthesesCoverageExpressionHandler;
import petascope.wcps.handler.ScaleExpressionByDimensionIntervalsHandler;
import petascope.wcps.handler.StringScalarHandler;
import petascope.wcps.handler.UnaryArithmeticExpressionHandler;
import petascope.wcps.handler.CoverageConstantHandler;
import petascope.wcps.handler.BooleanUnaryScalarExpressionHandler;
import petascope.wcps.handler.NanScalarHandler;
import petascope.wcps.handler.ForClauseListHandler;
import petascope.wcps.handler.CastExpressionHandler;
import petascope.wcps.handler.SwitchCaseExpressionHandler;
import petascope.wcps.handler.CoverageIdentifierHandler;
import petascope.wcps.handler.ScaleExpressionByImageCrsDomainHandler;
import petascope.wcps.handler.WcsScaleExpressionByScaleExtentHandler;
import petascope.wcps.handler.RealNumberConstantHandler;
import petascope.wcps.handler.WhereClauseHandler;
import petascope.wcps.handler.BooleanConstantHandler;
import petascope.wcps.handler.BinaryCoverageExpressionHandler;
import petascope.wcps.handler.ExtendExpressionByImageCrsDomainHandler;
import petascope.wcps.exception.processing.InvalidAxisNameException;
import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang3.StringUtils;

import static petascope.wcs2.parsers.subsets.SlicingSubsetDimension.ASTERISK;

import java.util.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.util.ListUtil;
import petascope.util.StringUtil;
import petascope.wcps.handler.AxisIteratorDomainIntervalsHandler;
import petascope.wcps.handler.AxisIteratorHandler;
import petascope.wcps.handler.AxisSpecHandler;
import petascope.wcps.handler.CellCountHandler;
import petascope.wcps.handler.ClipCorridorExpressionHandler;
import petascope.wcps.handler.ClipCurtainExpressionHandler;
import petascope.wcps.handler.ClipWKTExpressionHandler;
import petascope.wcps.handler.CoverageIsNullHandler;
import petascope.wcps.handler.CrsTransformShorthandHandler;
import petascope.wcps.handler.CrsTransformTargetGeoXYBoundingBoxHandler;
import petascope.wcps.handler.CrsTransformTargetGeoXYResolutionsHandler;
import petascope.wcps.handler.DecodeCoverageHandler;
import petascope.wcps.handler.DescribeCoverageHandler;
import petascope.wcps.handler.DimensionIntervalListHandler;
import petascope.wcps.handler.DimensionPointElementHandler;
import petascope.wcps.handler.DimensionPointListElementHandler;
import petascope.wcps.handler.DomainIntervalsHandler;
import static petascope.wcps.handler.DomainIntervalsHandler.DOMAIN_PORPERTY_UPPER_BOUND;
import static petascope.wcps.handler.DomainIntervalsHandler.DOMAIN_PROPERTY_LOWER_BOUND;
import static petascope.wcps.handler.DomainIntervalsHandler.DOMAIN_PROPERTY_RESOLUTION;
import petascope.wcps.handler.FlipExpressionHandler;
import petascope.wcps.handler.Handler;
import petascope.wcps.handler.IntervalExpressionHandler;
import petascope.wcps.handler.LetClauseHandler;
import petascope.wcps.handler.LetClauseListHandler;
import petascope.wcps.handler.RangeConstructorElementHandler;
import petascope.wcps.handler.RangeConstructorElementListHandler;
import petascope.wcps.handler.ScaleDimensionIntervalListHandler;
import petascope.wcps.handler.ShortHandSubsetWithLetClauseVariableHandler;
import petascope.wcps.handler.ShorthandSubsetHandler;
import petascope.wcps.handler.SliceDimensionIntervalElementHandler;
import petascope.wcps.handler.SliceScaleDimensionIntervalElement;
import petascope.wcps.handler.UnaryModExpressionHandler;
import petascope.wcps.metadata.service.LetClauseAliasRegistry;
import petascope.wcps.handler.SortExpressionHandler;
import petascope.wcps.handler.SwitchCaseDefaultValueHandler;
import petascope.wcps.handler.SwitchCaseElementHandler;
import petascope.wcps.handler.SwitchCaseElementListHandler;
import petascope.wcps.handler.TrimDimensionIntervalElementHandler;
import petascope.wcps.handler.TrimScaleDimensionIntervalElement;
import petascope.wcps.handler.WKTCompoundPointHandler;
import petascope.wcps.handler.WKTCompoundPointsListHandler;
import petascope.wcps.handler.WKTLineStringHandler;
import petascope.wcps.handler.WKTMultiPolygonHandler;
import petascope.wcps.handler.WKTPolygonHandler;
import petascope.wcps.metadata.service.SortedAxisIteratorAliasRegistry;
import petascope.wcps.metadata.service.UsingCondenseRegistry;

import petascope.wcps.handler.TrimDimensionIntervalByImageCrsDomainElementHandler;


/**
 * Class that implements the parsing rules described in wcps.g4
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
public class WcpsEvaluator extends wcpsBaseVisitor<Handler> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WcpsEvaluator.class);
    
    @Autowired private
    LetClauseAliasRegistry letClauseAliasRegistry;
    @Autowired private
    AxisIteratorAliasRegistry axisIteratorAliasRegistry;
    @Autowired private 
    UsingCondenseRegistry usingCondenseRegistry;
    @Autowired private
    SortedAxisIteratorAliasRegistry sortedAxisIteratorAliasRegistry;
    
    
    // Class handlers
    @Autowired private
    RootHandler rootHandler;
    @Autowired private
    ForClauseHandler forClauseHandler;
    @Autowired private
    ForClauseListHandler forClauseListHandler;
    @Autowired private
    ReturnClauseHandler returnClauseHandler;
    @Autowired private
    DescribeCoverageHandler describeCoverageHandler;
    @Autowired private
    EncodeCoverageHandler encodeCoverageHandler;
    @Autowired private
    DecodeCoverageHandler decodeCoverageHandler;
    
    @Autowired private
    ClipWKTExpressionHandler clipWKTExpressionHandler;
    @Autowired private
    ClipCurtainExpressionHandler clipCurtainExpressionHandler;
    @Autowired private
    ClipCorridorExpressionHandler clipCorridorExpressionHandler;
    
    @Autowired private
    FlipExpressionHandler flipExpressionHandler;
    @Autowired private
    SortExpressionHandler sortExpressionHandler;
    
    @Autowired private
    CrsTransformHandler crsTransformHandler;
    @Autowired private
    CrsTransformShorthandHandler crsTransformShorthandHandler;
    @Autowired private
    CrsTransformTargetGeoXYResolutionsHandler crsTransformTargetGeoXYResolutionsHandler;
    @Autowired private
    CrsTransformTargetGeoXYBoundingBoxHandler crsTransformTargetGeoXYBoundingBoxHandler;
    
    @Autowired private
    CoverageVariableNameHandler coverageVariableNameHandler;
    @Autowired private
    BinaryCoverageExpressionHandler binaryCoverageExpressionHandler;
    @Autowired private
    BooleanConstantHandler booleanConstantHandler;
    @Autowired private
    BinaryScalarExpressionHandler binaryScalarExpressionHandler;
    @Autowired private
    CoverageConstructorHandler coverageConstructorHandler;
    @Autowired private
    UnaryArithmeticExpressionHandler unaryArithmeticExpressionHandler;
    
    @Autowired private
    UnaryPowerExpressionHandler unaryPowerExpressionHandler;
    @Autowired private
    UnaryModExpressionHandler unaryModPowerExpressionHandler;
    @Autowired private
    UnaryBooleanExpressionHandler unaryBooleanExpressionHandler;
    
    @Autowired private
    CastExpressionHandler castExpressionHandler;
    @Autowired private
    RangeSubsettingHandler rangeSubsettingHandler;
    @Autowired private
    RealNumberConstantHandler realNumberConstantHandler;
    @Autowired private
    NanScalarHandler nanScalarHandler;
    @Autowired private
    ParenthesesCoverageExpressionHandler parenthesesCoverageExpressionHandler;
    
    @Autowired private
    LetClauseHandler letClauseHandler;
    @Autowired private
    LetClauseListHandler letClauseListHandler;
    @Autowired private
    ShortHandSubsetWithLetClauseVariableHandler shortHandSubsetWithLetClauseVariableHandler;
    
    @Autowired private
    WhereClauseHandler whereClauseHandler;
    @Autowired private
    BooleanUnaryScalarExpressionHandler booleanUnaryScalarExpressionHandler;
    @Autowired private
    BooleanNumericalComparisonScalarHandler booleanNumericalComparisonScalarHandler;
    @Autowired private
    ReduceExpressionHandler reduceExpressionHandler;
    @Autowired private
    ComplexNumberConstantHandler complexNumberConstantHandler;
    
    @Autowired private
    GeneralCondenserHandler generalCondenserHandler;    
    @Autowired private
    SubsetExpressionHandler subsetExpressionHandler;
    @Autowired private
    CoverageConstantHandler coverageConstantHandler;
    @Autowired private
    ExtendExpressionHandler extendExpressionHandler;
    @Autowired private
    ExtendExpressionByImageCrsDomainHandler extendExpressionByDomainIntervalsHandler;
    @Autowired private
    RangeConstructorHandler rangeConstructorHandler;
    @Autowired private
    RangeConstructorElementHandler rangeConstructorElementHandler;
    @Autowired private
    RangeConstructorElementListHandler rangeConstructorElementListHandler;
    
    @Autowired private
    StringScalarHandler stringScalarHandler;
    @Autowired private
    CoverageIdentifierHandler coverageIdentifierHandler;
    @Autowired private
    CellCountHandler cellCountHandler;
    
    @Autowired private
    CoverageCrsSetHandler coverageCrsSetHandler;
    // Scale Extension
    // WCPS standard
    @Autowired private
    ScaleExpressionByDimensionIntervalsHandler scaleExpressionByDimensionIntervalsHandler;
    @Autowired private
    ScaleExpressionByImageCrsDomainHandler scaleExpressionByImageCrsDomainHandler;
    // Made up to handle WCS -> WCPS scale
    @Autowired private
    WcsScaleExpressionByFactorHandler scaleExpressionByFactorHandler;
    @Autowired private
    WcsScaleExpressionByScaleAxesHandler scaleExpressionByScaleAxesHandler;
    @Autowired private
    WcsScaleExpressionByScaleSizeHandler scaleExpressionByScaleSizeHandler;
    @Autowired private
    WcsScaleExpressionByScaleExtentHandler scaleExpressionByScaleExtentHandler;
    
    @Autowired private
    SliceScaleDimensionIntervalElement sliceScaleDimensionIntervalElement;
    @Autowired private
    TrimScaleDimensionIntervalElement trimScaleDimensionIntervalElement;
    @Autowired private
    ScaleDimensionIntervalListHandler scaleDimensionIntervalListHandler;
    
    @Autowired private
    DomainIntervalsHandler domainIntervalsHandler;
    @Autowired private
    ImageCrsExpressionHandler imageCrsExpressionHandler;
    @Autowired private
    ImageCrsDomainExpressionHandler imageCrsDomainExpressionHandler;
    @Autowired private
    ImageCrsDomainExpressionByDimensionExpressionHandler imageCrsDomainExpressionByDimensionExpressionHandler;
    @Autowired private
    DomainExpressionHandler domainExpressionHandler;
    
    @Autowired private
    SwitchCaseElementHandler switchCaseElementHandler;
    @Autowired private
    SwitchCaseElementListHandler switchCaseElementListHandler;
    @Autowired private
    SwitchCaseDefaultValueHandler switchCaseDefaultValueHandler;
    @Autowired private
    SwitchCaseExpressionHandler switchCaseExpressionHandler;
    
    @Autowired private
    CoverageIsNullHandler coverageIsNullHandler;
    
    @Autowired private
    DimensionPointElementHandler dimensionPointElementHandler;
    @Autowired private
    DimensionPointListElementHandler dimensionPointListElementHandler;
    @Autowired private
    IntervalExpressionHandler intervalExpressionHandler;
    @Autowired private
    DimensionIntervalListHandler dimensionIntervalListHandler;
    @Autowired private
    ShorthandSubsetHandler shorthandSubsetHandler;
    
    @Autowired private
    SliceDimensionIntervalElementHandler sliceDimensionIntervalElementHandler;
    @Autowired private
    TrimDimensionIntervalElementHandler trimDimensionIntervalElementHandler;
    
    @Autowired private
    TrimDimensionIntervalByImageCrsDomainElementHandler trimDimensionIntervalByImageCrsDomainElementHandler;
    
    @Autowired private
    AxisSpecHandler axisSpecHandler;
    @Autowired private
    AxisIteratorHandler axisIteratorHandler;
    @Autowired private
    AxisIteratorDomainIntervalsHandler axisIteratorDomainIntervalsHandler;
    
    @Autowired private
    WKTCompoundPointHandler wktCompoundPointHandler;
    @Autowired private
    WKTCompoundPointsListHandler wktCompoundPointsListHandler;
    
    @Autowired private
    WKTLineStringHandler wktLineStringHandler;
    @Autowired private
    WKTPolygonHandler wktPolygonHandler;
    @Autowired private
    WKTMultiPolygonHandler wktMultiPolygonHandler;
    
    /**
     * Class constructor. This object is created for each incoming Wcps query.
     */
    public WcpsEvaluator() {
        super();        
    }

    // VISITOR HANDLERS
    /* --------------------- Visit each nodes then parse ------------------ */
    @Override
    public Handler visitWcpsQueryLabel(@NotNull wcpsParser.WcpsQueryLabelContext ctx) { 
        // ROOT node
        
        Handler forClauseListHandler = visit(ctx.forClauseList());
        Handler letClauseListHandler = null;
        if (ctx.letClauseList() != null) {
            letClauseListHandler = visit(ctx.letClauseList());
        }
        //only visit the where clause if it exists
        Handler whereClauseHandler = null;
        if (ctx.whereClause() != null) {
            whereClauseHandler = visit(ctx.whereClause());
        }
        Handler returnClauseHandler = visit(ctx.returnClause());
        
        Handler result = this.rootHandler.create(forClauseListHandler, letClauseListHandler, whereClauseHandler, returnClauseHandler);
        
        return result;
    }

    @Override
    public Handler visitForClauseLabel(@NotNull wcpsParser.ForClauseLabelContext ctx) {
        // $c
        String coverageIterator = ctx.coverageVariableName().getText();
        // e.g. test_mr, test_mr2
        List<Handler> coverageIdHandlers = new ArrayList<>();
        Handler decodeCoverageHandler = null;
        
        for (wcpsParser.CoverageIdForClauseContext context : ctx.coverageIdForClause()) {
            if (context.COVERAGE_VARIABLE_NAME() != null) {
                // coverage Id is persited coverage, e.g: $c in (test_mr)
                Handler coverageIdHandler = this.stringScalarHandler.create(context.COVERAGE_VARIABLE_NAME().getText());
                coverageIdHandlers.add(coverageIdHandler);
            } else if (context.decodeCoverageExpression() != null && context.decodeCoverageExpression().getText() != null) {
                // coverage Id is created from postional parameter (e.g: $c in (decode($1))
                decodeCoverageHandler = visit(context.decodeCoverageExpression());
            }
        }

        Handler result = this.forClauseHandler.create(this.stringScalarHandler.create(coverageIterator),
                                            decodeCoverageHandler,
                                            coverageIdHandlers);
        return result;
    }

    @Override
    public Handler visitForClauseListLabel(@NotNull wcpsParser.ForClauseListLabelContext ctx) {
        List<Handler> childHandlers = new ArrayList<>();
        
        for (wcpsParser.ForClauseContext currentClause : ctx.forClause()) {
            Handler childHandler = visit(currentClause);
            childHandlers.add(childHandler);
        }
        
        Handler result = this.forClauseListHandler.create(childHandlers);
        return result;
    }    
    
    @Override
    public Handler visitLetClauseListLabel(@NotNull wcpsParser.LetClauseListLabelContext ctx) {
        List<Handler> childHandlers = new ArrayList<>();
        
        for (wcpsParser.LetClauseContext currentClause : ctx.letClause()) {
            Handler childHandler = visit(currentClause);
            childHandlers.add(childHandler);
        }

        Handler result = this.letClauseListHandler.create(childHandlers);
        return result;
    }
    
    @Override
    public Handler visitLetClauseCoverageExpressionLabel(@NotNull wcpsParser.LetClauseCoverageExpressionLabelContext ctx) {
        // coverageVariableName EQUAL coverageExpression
        // e.g: $a := $c[Lat(20:30), Long(40:50)]
        
        Handler coverageExpressionHandler = null;
        
        if (ctx.wktExpression() != null) {
            coverageExpressionHandler = (Handler) visit(ctx.wktExpression());
        } else {
            coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        }
        
        String coverageVariableName = ctx.coverageVariableName().getText();
        Handler result = this.letClauseHandler.create(this.stringScalarHandler.create(coverageVariableName), coverageExpressionHandler);
        return result;
    }
  
    @Override
    public Handler visitLetClauseDimensionIntervalListLabel(@NotNull wcpsParser.LetClauseDimensionIntervalListLabelContext ctx) {
        // coverageVariableName COLON EQUAL LEFT_BRACKET dimensionIntervalList RIGHT_BRACKET
        // e.g: $a := [Lat(20:30), Long(40:50)]
        Handler dimensionIntervalListHandler = visit(ctx.letClauseDimensionIntervalList().dimensionIntervalList());
        String coverageVariableName = ctx.letClauseDimensionIntervalList().coverageVariableName().getText();
        Handler result = this.letClauseHandler.create(this.stringScalarHandler.create(coverageVariableName), dimensionIntervalListHandler);
        return result;
    }
    
    @Override
    public Handler visitCoverageExpressionShortHandSubsetWithLetClauseVariableLabel(@NotNull wcpsParser.CoverageExpressionShortHandSubsetWithLetClauseVariableLabelContext ctx) {
        //  coverageExpression LEFT_BRACKET letClauseDimensionIntervalList RIGHT_BRACKET
        // e.g: c[$a] with $a := [Lat(0:20), Long(0:30)]
        
        // e.g. c
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        // e.g. $a
        Handler letClauseVariableHandler = visit(ctx.coverageVariableName());

        Handler result = this.shortHandSubsetWithLetClauseVariableHandler.create(coverageExpressionHandler, letClauseVariableHandler);
        return result;
    }    

    @Override
    public Handler visitReturnClauseLabel(@NotNull wcpsParser.ReturnClauseLabelContext ctx) {
        Handler childHandler = visit(ctx.processingExpression());

        Handler result = this.returnClauseHandler.create(Arrays.asList(childHandler));
        return result;
    }
    
    @Override
    public Handler visitDescribeCoverageExpressionLabel(@NotNull wcpsParser.DescribeCoverageExpressionLabelContext ctx) {
        // describe(coverage expression, "format", "extra parameters")
        // e.g: describe($c + 5, "gml", "{\"outputType\":\"GeneralGridCoverage\"}")
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        // e.g: gml / json
        String formatType = StringUtil.stripFirstAndLastQuotes(ctx.STRING_LITERAL().getText());
        
        // e.g: {\"outputType\":\"GeneralGridCoverage\"}
        String extraParams = "";
        if (ctx.extraParams() != null) {
            extraParams = StringUtil.stripFirstAndLastQuotes(ctx.extraParams().getText()).replace("\\", "");
        }
        
        Handler result = this.describeCoverageHandler.create(coverageExpressionHandler, 
                                                            this.stringScalarHandler.create(formatType),
                                                            this.stringScalarHandler.create(extraParams));
        
        return result;
    }

    @Override
    public Handler visitEncodedCoverageExpressionLabel(@NotNull wcpsParser.EncodedCoverageExpressionLabelContext ctx) {
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        // e.g: tiff
        String formatType = StringUtil.stripFirstAndLastQuotes(ctx.STRING_LITERAL().getText());
        // NOTE: extraParam here can be:
        // + Old style: e.g: "nodata=0"
        // + JSON style: e.g: "{\"nodata\": [0]}" -> {"nodata": [0]}
        String extraParams = "";
        if (ctx.extraParams() != null) {
            extraParams = StringUtil.stripFirstAndLastQuotes(ctx.extraParams().getText()).replace("\\", "");
        }
        
        Handler result = this.encodeCoverageHandler.create(coverageExpressionHandler, this.stringScalarHandler.create(formatType), this.stringScalarHandler.create(extraParams));
        return result;
    }
    
    @Override
    public Handler visitDecodedCoverageExpressionLabel(@NotNull wcpsParser.DecodedCoverageExpressionLabelContext ctx) {
        // e.g: decode($1, "$2") with $1 is an uploaded file, $2 is an extra parameter
        String positionalParameter = ctx.positionalParamater().getText();
        String extraParamters = "";
        if (ctx.extraParams() != null) {
            extraParamters = StringUtil.stripFirstAndLastQuotes(ctx.extraParams().getText());
        }
        
        Handler result = this.decodeCoverageHandler.create(this.stringScalarHandler.create(positionalParameter), this.stringScalarHandler.create(extraParamters));
        return result;
    }
    
    // -- clip
    
    @Override
    public Handler visitClipWKTExpressionLabel(@NotNull wcpsParser.ClipWKTExpressionLabelContext ctx) { 
        // Handle clipWKTExpression: CLIP LEFT_PARENTHESIS coverageExpression COMMA wktExpression (COMMA crsName)? RIGHT_PARENTHESIS
        // e.g: clip(c[i(0:20), j(0:20)], Polygon((0 10, 20 20, 20 10, 0 10)), "http://opengis.net/def/CRS/EPSG/3857")
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        Handler wktShapeHandler = visit(ctx.wktExpression());
        
        // NOTE: This one is optional parameter, if specified, XY coordinates in WKT will be translated from this CRS to coverage's native CRS for XY axes.
        Handler wktCRSHandler = null;
        String wktCRS = null;
        if (ctx.crsName() != null) {
            wktCRS = StringUtil.stripFirstAndLastQuotes(ctx.crsName().getText());
            wktCRSHandler = this.stringScalarHandler.create(wktCRS);
        }        
        
        // Optional parameter for encoding in JSON/CSV to show grid coordinates and their values (e.g: "x1 y1 value1", "x2 y2 value2", ...)
        Handler withCoordinateHandler = null;
        if (ctx.WITH_COORDINATES() != null) {
            withCoordinateHandler = this.stringScalarHandler.create(Boolean.TRUE.toString());
        }
        
        Handler result = clipWKTExpressionHandler.create(coverageExpressionHandler, 
                                                            wktShapeHandler, 
                                                            wktCRSHandler,
                                                            withCoordinateHandler);
        return result;
    }
  
    @Override
    public Handler visitClipCurtainExpressionLabel(@NotNull wcpsParser.ClipCurtainExpressionLabelContext ctx) {
        // Handle clipCurtainExpression: 
//        CLIP LEFT_PARENTHESIS coverageExpression
//                              COMMA CURTAIN LEFT_PARENTHESIS
//                                                PROJECTION LEFT_PARENTHESIS curtainProjectionAxisLabel1 COMMA curtainProjectionAxisLabel2 RIGHT_PARENTHESIS
//                                                COMMA wktExpression
//                                            RIGHT_PARENTHESIS
//                              (COMMA crsName)?
//            RIGHT_PARENTHESIS
        // e.g: clip( c, curtain( projection(Lat, Lon), Polygon((0 10, 20 20, 20 10, 0 10)) ) )
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        Handler wktShapeHandler = visit(ctx.wktExpression());
        
        String curtainProjectionAxisLabel1 = ctx.curtainProjectionAxisLabel1().getText().trim();
        String curtainProjectionAxisLabel2 = ctx.curtainProjectionAxisLabel2().getText().trim();
        
        if (curtainProjectionAxisLabel1.equals(curtainProjectionAxisLabel2)) {
            throw new WCPSException(ExceptionCode.InvalidRequest, "Axis names in curtain's projection must be unique, given same name '" + curtainProjectionAxisLabel1 + "'.");
        }
        
        // NOTE: This one is optional parameter, if specified, XY coordinates in WKT will be translated from this CRS to coverage's native CRS for XY axes.
        Handler wktCRSHandler = null;
        String wktCRS = null;
        if (ctx.crsName() != null) {
            wktCRS = StringUtil.stripFirstAndLastQuotes(ctx.crsName().getText());
            wktCRSHandler = this.stringScalarHandler.create(wktCRS);
        }     
        
        Handler result = clipCurtainExpressionHandler.create(coverageExpressionHandler,
                                                        this.stringScalarHandler.create(curtainProjectionAxisLabel1),
                                                        this.stringScalarHandler.create(curtainProjectionAxisLabel2),
                                                        wktShapeHandler,
                                                        wktCRSHandler);
        return result;
    }
    
    
    @Override
    public Handler visitClipCorridorExpressionLabel(@NotNull wcpsParser.ClipCorridorExpressionLabelContext ctx) {
        // Handle clip corridor expression
//        CLIP LEFT_PARENTHESIS coverageExpression
//                              COMMA CORRIDOR LEFT_PARENTHESIS
//                                                 PROJECTION LEFT_PARENTHESIS corridorProjectionAxisLabel1 COMMA corridorProjectionAxisLabel2 RIGHT_PARENTHESIS
//                                                 COMMA wktLineString 
//                                                 COMMA wktExpression 
//                                                 (COMMA DISCRETE)?
//                                             RIGHT_PARENTHESIS
//                              (COMMA crsName)?
//            RIGHT_PARENTHESIS
        // e.g: clip( c, corridor( projection(Lat, Lon), LineString("1950-01-01" 1 1, "1950-01-02" 5 5), Polygon((0 10, 20 20, 20 10, 0 10)), discrete ) )
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        Handler wktLineStringHandler = visit(ctx.corridorWKTLabel1());
        Handler wktShapeHandler = visit(ctx.corridorWKTLabel2());
        
        String corridorProjectionAxisLabel1 = ctx.corridorProjectionAxisLabel1().getText().trim();
        String corridorProjectionAxisLabel2 = ctx.corridorProjectionAxisLabel2().getText().trim();
        
        if (corridorProjectionAxisLabel1.equals(corridorProjectionAxisLabel2)) {
            throw new WCPSException("Axis names in corridor's projection must be unique, given same name '" + corridorProjectionAxisLabel1 + "'.");
        }
        
        Handler discreteHandler = null;
        if (ctx.DISCRETE() != null) {
            discreteHandler = this.stringScalarHandler.create(Boolean.TRUE.toString());
        }
        
        // NOTE: This one is optional parameter, if specified, XY coordinates in WKT will be translated from this CRS to coverage's native CRS for XY axes.
        Handler wktCRSHandler = null;
        if (ctx.crsName() != null) {
            String crs = StringUtil.stripFirstAndLastQuotes(ctx.crsName().getText());
            wktCRSHandler = this.stringScalarHandler.create(crs);
        }
        
        Handler result = this.clipCorridorExpressionHandler.create(coverageExpressionHandler,
                                                                   this.stringScalarHandler.create(corridorProjectionAxisLabel1),
                                                                   this.stringScalarHandler.create(corridorProjectionAxisLabel2),
                                                                   wktLineStringHandler,
                                                                   wktShapeHandler, discreteHandler,
                                                                   wktCRSHandler);
        return result;
    }
    
    
    
    
    
    @Override 
    public Handler visitFlipExpressionLabel(@NotNull wcpsParser.FlipExpressionLabelContext ctx) {
        // Handle FLIP $COVERAGE_EXPRESSION ALONG $AXIS_LABEL
        
        // e.g. $c + 5
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        String axisLabel = ctx.axisName().getText();
        
        Handler result = this.flipExpressionHandler.create(coverageExpressionHandler, 
                                                        this.stringScalarHandler.create(axisLabel));
        return result;
    }
    
    @Override 
    public Handler visitSortExpressionLabel(@NotNull wcpsParser.SortExpressionLabelContext ctx) {
        // Handle SORT $COVERAGE_EXPRESSION ALONG $AXIS_LABEL BY $COVERAGE_EXPRESSION
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression(0));
        
        // e.g. sorted by Long axis
        String sortedAxisLabel = ctx.axisName().getText();
        
        String sortingOrder = null;
        if (ctx.sortingOrder() == null) {
            sortingOrder = SortExpressionHandler.DEFAULT_SORTING_ORDER;
        } else {
            // e.g. desc
            sortingOrder = ctx.sortingOrder().getText();
        }
        
        Handler cellExpressionHandler = (Handler) visit(ctx.coverageExpression(1));
        
        Handler result = this.sortExpressionHandler.create(coverageExpressionHandler, 
                                                this.stringScalarHandler.create(sortedAxisLabel),
                                                this.stringScalarHandler.create(sortingOrder),
                                                cellExpressionHandler);
        return result;
    }
    

    @Override
    public Handler visitCrsTransformExpressionLabel(@NotNull wcpsParser.CrsTransformExpressionLabelContext ctx) {
        // Handle crsTransform($COVERAGE_EXPRESSION, {$DOMAIN_CRS_2D}, {$INTERPOLATION}, {dimensionGeoXYResolutionsList})
        // e.g: crsTransform(c, {Lat:"www.opengis.net/def/crs/EPSG/0/4327", Long:"www.opengis.net/def/crs/EPSG/0/4327"}, {}, {Lat:30.5, Lon:30/6})
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        
        // { Axis_CRS_1 , Axis_CRS_2 } (e.g: Lat:"http://localhost:8080/def/crs/EPSG/0/4326")
        wcpsParser.DimensionCrsElementLabelContext ctxCRSAxisX = (wcpsParser.DimensionCrsElementLabelContext) ctx.dimensionCrsList().getChild(1);
        String axisNameX = ctxCRSAxisX.axisName().getText();
        String crsX = StringUtil.stripFirstAndLastQuotes(ctxCRSAxisX.crsName().getText());
        
        wcpsParser.DimensionCrsElementLabelContext ctxCRSAxisY = (wcpsParser.DimensionCrsElementLabelContext) ctx.dimensionCrsList().getChild(3);
        String axisNameY = ctxCRSAxisY.axisName().getText();
        String crsY = StringUtil.stripFirstAndLastQuotes(ctxCRSAxisY.crsName().getText());
        
        String interpolationType = null;

        if (ctx.interpolationType() != null) {
            interpolationType = ctx.interpolationType().getText();
        }
        
        CrsTransformTargetGeoXYResolutionsHandler targetGeoXYResolutionsHandler = null;
        
        CrsTransformTargetGeoXYBoundingBoxHandler targetGeoXYBBoxHandler = null;
        if (ctx.dimensionIntervalList() != null || ctx.domainExpression() != null) {
            targetGeoXYBBoxHandler = this.createCrsTransformTargetGeoXYBoundingBoxHandler(ctx.dimensionIntervalList(),
                                                                                        ctx.domainExpression());
        }
        
        if (ctx.dimensionGeoXYResolutionsList() != null) {
            if (ctx.dimensionGeoXYResolutionsList().getChildCount() > 1) {
                targetGeoXYResolutionsHandler = this.createCrsTransformTargetGeoXYResolutionsHandler(ctx.dimensionGeoXYResolutionsList());
            } else {
                targetGeoXYBBoxHandler = this.createCrsTransformTargetGeoXYBoundingBoxHandler((wcpsParser.DimensionGeoXYResolutionContext) ctx.dimensionGeoXYResolutionsList().getChild(0));
            }
        }
        
        Handler result = this.crsTransformHandler.create(coverageExpressionHandler,
                                                        this.stringScalarHandler.create(axisNameX), this.stringScalarHandler.create(crsX),
                                                        this.stringScalarHandler.create(axisNameY), this.stringScalarHandler.create(crsY),
                                                        this.stringScalarHandler.create(interpolationType),
                                                        targetGeoXYResolutionsHandler,
                                                        targetGeoXYBBoxHandler);
        return result;
    }
    
  
    @Override 
    public Handler visitCrsTransformShorthandExpressionLabel(@NotNull wcpsParser.CrsTransformShorthandExpressionLabelContext ctx) { 
        // Handle crsTransform($COVERAGE_EXPRESSION, "CRS", {$INTERPOLATION}, {dimensionGeoXYResolutionsList})
        // e.g: crsTransform(c, "EPSG:4326", { near }, {Lat:30.5, Lon:30/6})
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        
        String outputCRSAuthorityCode = StringUtil.stripFirstAndLastQuotes(ctx.crsName().getText());
        String interpolationType = null;

        if (ctx.interpolationType() != null) {
            interpolationType = ctx.interpolationType().getText();
        }
        
        CrsTransformTargetGeoXYResolutionsHandler targetGeoXYResolutionsHandler = null;
        
        CrsTransformTargetGeoXYBoundingBoxHandler targetGeoXYBBoxHandler = null;
        if (ctx.dimensionIntervalList() != null || ctx.domainExpression() != null) {
            targetGeoXYBBoxHandler = this.createCrsTransformTargetGeoXYBoundingBoxHandler(ctx.dimensionIntervalList(),
                                                                                        ctx.domainExpression());
        }
        
        if (ctx.dimensionGeoXYResolutionsList() != null) {
            if (ctx.dimensionGeoXYResolutionsList().getChildCount() > 1) {
                targetGeoXYResolutionsHandler = this.createCrsTransformTargetGeoXYResolutionsHandler(ctx.dimensionGeoXYResolutionsList());
            } else {
                targetGeoXYBBoxHandler = this.createCrsTransformTargetGeoXYBoundingBoxHandler((wcpsParser.DimensionGeoXYResolutionContext) ctx.dimensionGeoXYResolutionsList().getChild(0));
            }
        }
        
        Handler result = this.crsTransformShorthandHandler.create(coverageExpressionHandler,
                                        this.stringScalarHandler.create(outputCRSAuthorityCode),
                                        this.stringScalarHandler.create(interpolationType),
                                        targetGeoXYResolutionsHandler,
                                        targetGeoXYBBoxHandler);
        return result;

    }
    

    /**
     * Create a handler for CrsTransform target geo XY resolutions, e.g. {Lat:30, Lat:domain($c, Lat).resolution}
     * 
     */
    private CrsTransformTargetGeoXYResolutionsHandler createCrsTransformTargetGeoXYResolutionsHandler(wcpsParser.DimensionGeoXYResolutionsListContext listContext) {        
        CrsTransformTargetGeoXYResolutionsHandler targetGeoXYResolutionsHandler = null;
        
        if (listContext != null) {
            StringScalarHandler geoResolutionAxisLabelX = null, geoResolutionAxisLabelY = null;
            Handler scalarExpressionHandlerX = null, scalarExpressionHandlerY = null;                
            wcpsParser.DimensionGeoXYResolutionContext ctxAxisX = (wcpsParser.DimensionGeoXYResolutionContext) listContext.getChild(0);
            if (ctxAxisX.COVERAGE_VARIABLE_NAME() == null) {
                scalarExpressionHandlerX = visit(ctxAxisX.coverageExpression());
                // NOTE: in this case, it has e.g. Lat:domain($c, Lat).resolution - 1
                String axisLabelX = ctxAxisX.getText().split(":")[0].trim();
                geoResolutionAxisLabelX = this.stringScalarHandler.create(axisLabelX);
            } else {
                scalarExpressionHandlerX = visit(ctxAxisX.coverageExpression());
                geoResolutionAxisLabelX = this.stringScalarHandler.create(ctxAxisX.COVERAGE_VARIABLE_NAME().getText());
            }
            
            wcpsParser.DimensionGeoXYResolutionContext ctxAxisY = (wcpsParser.DimensionGeoXYResolutionContext) listContext.getChild(2);
            if (ctxAxisY.COVERAGE_VARIABLE_NAME() == null) {
                scalarExpressionHandlerY = visit(ctxAxisY.coverageExpression());
                // NOTE: in this case, it has e.g. Lon:domain($c, Lon).resolution
                String axisLabelY = ctxAxisX.getText().split(":")[0].trim();
                geoResolutionAxisLabelY = this.stringScalarHandler.create(axisLabelY);
            } else {
                scalarExpressionHandlerY = visit(ctxAxisY.coverageExpression());
                geoResolutionAxisLabelY = this.stringScalarHandler.create(ctxAxisY.COVERAGE_VARIABLE_NAME().getText());
            }            
            
            targetGeoXYResolutionsHandler =  this.crsTransformTargetGeoXYResolutionsHandler.create(geoResolutionAxisLabelX, scalarExpressionHandlerX, 
                                                                                                    geoResolutionAxisLabelY, scalarExpressionHandlerY);
            
        }
        
        return targetGeoXYResolutionsHandler;
    }      
    

    /**
     * Create a handler for CrsTransform target geo XY bounding box, e.g. {Lat(30:50), Lon(60:70)}  or {domain($c)}
     */
    private CrsTransformTargetGeoXYBoundingBoxHandler createCrsTransformTargetGeoXYBoundingBoxHandler(wcpsParser.DimensionIntervalListContext listContext,
                                                                                                      wcpsParser.DomainExpressionContext domainExpressionContext) {
        
        Handler dimensionIntervalListHandlerTmp = null, domainExpressionHandlerTmp = null;
                
        
        if (listContext != null) {
            dimensionIntervalListHandlerTmp = visit(listContext);
        } else if (domainExpressionContext != null) {
            domainExpressionHandlerTmp = visit(domainExpressionContext);
        }
        
        // Handler for crsTransform's target geo XY BBOX {Lat(0:5), Lon(0.5)}
        CrsTransformTargetGeoXYBoundingBoxHandler result = this.crsTransformTargetGeoXYBoundingBoxHandler.create(dimensionIntervalListHandlerTmp, 
                                                                                                                domainExpressionHandlerTmp);
        
        return result;
    }     
    
    
    /**
     * Handler for crsTransform's target geo XY BBOX {domain($c)} without target geoXY resolutions
     * encode(crsTransform($c, "EPSG:4326", { bilinear }, { domain($d) })
     */
    private CrsTransformTargetGeoXYBoundingBoxHandler createCrsTransformTargetGeoXYBoundingBoxHandler(wcpsParser.DimensionGeoXYResolutionContext ctx) {
        Handler domainExpressionHandler = visit(ctx);
        CrsTransformTargetGeoXYBoundingBoxHandler result = this.crsTransformTargetGeoXYBoundingBoxHandler.create(null, 
                                                                                                                domainExpressionHandler);
        
        return result; 
    }
    
    
    
    @Override
    public Handler visitCoverageVariableNameLabel(@NotNull wcpsParser.CoverageVariableNameLabelContext ctx) {
        // Identifier, e.g: $c or c
        // NOTE: axisIterator and coverage variable name can be the same syntax (e.g: $c, $px)
        String coverageVariable = ctx.COVERAGE_VARIABLE_NAME().getText();
        
        Handler result = this.coverageVariableNameHandler.create(this.stringScalarHandler.create(coverageVariable));
        return result;

    }

    @Override
    public Handler visitCoverageExpressionLogicLabel(@NotNull wcpsParser.CoverageExpressionLogicLabelContext ctx) {
        // coverageExpression booleanOperator coverageExpression  (e.g: (c + 1) and (c + 1))
        Handler leftCoverageExpr = (Handler) visit(ctx.coverageExpression(0));
        String operand = ctx.booleanOperator().getText();
        Handler rightCoverageExpr = (Handler) visit(ctx.coverageExpression(1));

        Handler result = binaryCoverageExpressionHandler.create(leftCoverageExpr, 
                                                                this.stringScalarHandler.create(operand),
                                                                rightCoverageExpr);
        return result;
    }

    @Override
    public Handler visitCoverageExpressionArithmeticLabel(@NotNull wcpsParser.CoverageExpressionArithmeticLabelContext ctx) {
        // coverageExpression (+, -, *, /) coverageExpression (e.g: c + 5 or 2 - 3) as BinarycoverageExpression
        Handler firstCoverageExpressionChildHandler = (Handler) visit(ctx.coverageExpression(0));
        String operand = ctx.coverageArithmeticOperator().getText();
        Handler secondCoverageExpressionChildHandler = (Handler) visit(ctx.coverageExpression(1));
        
        Handler result = this.binaryCoverageExpressionHandler.create(firstCoverageExpressionChildHandler, 
                                                                    this.stringScalarHandler.create(operand),
                                                                    secondCoverageExpressionChildHandler
                                                        );
        return result;
    }

    @Override
    public Handler visitCoverageExpressionOverlayLabel(@NotNull wcpsParser.CoverageExpressionOverlayLabelContext ctx) {
        // coverageExpression OVERLAY coverageExpression (e.g: c overlay d)
        // invert the order of the operators since WCPS overlay order is the opposite of the one in rasql
        Handler leftCoverageExpression = (Handler) visit(ctx.coverageExpression(1));
        String overlay = ctx.OVERLAY().getText();
        Handler rightCoverageExpression = (Handler) visit(ctx.coverageExpression(0));

        Handler result = this.binaryCoverageExpressionHandler.create(leftCoverageExpression,
                                                                     this.stringScalarHandler.create(overlay),
                                                                     rightCoverageExpression);
        return result;
    }

    @Override
    public Handler visitBooleanConstant(@NotNull wcpsParser.BooleanConstantContext ctx) {
        // TRUE | FALSE (e.g: true or false)
        String value = ctx.getText();
        
        Handler result = this.booleanConstantHandler.create(value);
        return result;
    }

    @Override
    public Handler visitBooleanStringComparisonScalar(@NotNull wcpsParser.BooleanStringComparisonScalarContext ctx) {
        // stringScalarExpression stringOperator stringScalarExpression  (e.g: c = d) or (c != 2))
        String leftScalarStr = ctx.stringScalarExpression(0).getText();
        String operand = ctx.stringOperator().getText();
        String rightScalarStr = ctx.stringScalarExpression(1).getText();

        Handler result = binaryScalarExpressionHandler.create(this.stringScalarHandler.create(leftScalarStr),
                                                              this.stringScalarHandler.create(operand),
                                                              this.stringScalarHandler.create(rightScalarStr));
        return result;
    }

    @Override
    public Handler visitUnaryCoverageArithmeticExpressionLabel(@NotNull wcpsParser.UnaryCoverageArithmeticExpressionLabelContext ctx) {
        // unaryArithmeticExpressionOperator  LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: sqrt(c)
        String operator = ctx.unaryArithmeticExpressionOperator().getText();
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());

        Handler result = unaryArithmeticExpressionHandler.create(this.stringScalarHandler.create(operator), coverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitTrigonometricExpressionLabel(@NotNull wcpsParser.TrigonometricExpressionLabelContext ctx) {
        // trigonometricOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: sin(c), cos(c), tan(c)
        String operator = ctx.trigonometricOperator().getText();
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());

        Handler result = unaryArithmeticExpressionHandler.create(this.stringScalarHandler.create(operator), coverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitExponentialExpressionLabel(@NotNull wcpsParser.ExponentialExpressionLabelContext ctx) {
        // exponentialExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: exp(c), log(c), ln(c)
        String operator = ctx.exponentialExpressionOperator().getText();
        Handler coverageExpr = (Handler) visit(ctx.coverageExpression());

        Handler result = unaryArithmeticExpressionHandler.create(this.stringScalarHandler.create(operator), coverageExpr);
        return result;
    }
    
    @Override
    public Handler visitMinBinaryExpressionLabel(@NotNull wcpsParser.MinBinaryExpressionLabelContext ctx) {
        // MIN LEFT_PARENTHESIS coverageExpression COMMA coverageExpression RIGHT_PARENTHESIS
        // e.g: min(c, c1) with c and c1 are coverages
        Handler leftCoverageExpressionHandler = (Handler) visit(ctx.coverageExpression(0));        
        Handler rightCoverageExpressionHandler = (Handler) visit(ctx.coverageExpression(1));

        Handler result = this.binaryCoverageExpressionHandler.create(leftCoverageExpressionHandler,
                                                                    this.stringScalarHandler.create("MIN"),
                                                                    rightCoverageExpressionHandler);
        return result;
    }
    
    @Override
    public Handler visitMaxBinaryExpressionLabel(@NotNull wcpsParser.MaxBinaryExpressionLabelContext ctx) {
        // MAX LEFT_PARENTHESIS coverageExpression COMMA coverageExpression RIGHT_PARENTHESIS
        // e.g: max(c, c1) with c and c1 are coverages
        Handler leftCoverageExpressionHandler = (Handler) visit(ctx.coverageExpression(0));        
        Handler rightCoverageExpressionHandler = (Handler) visit(ctx.coverageExpression(1));

        Handler result = this.binaryCoverageExpressionHandler.create(leftCoverageExpressionHandler,
                                                                    this.stringScalarHandler.create("MAX"),
                                                                    rightCoverageExpressionHandler
                                                                    );
        return result;
    }

    @Override
    public Handler visitUnaryPowerExpressionLabel(@NotNull wcpsParser.UnaryPowerExpressionLabelContext ctx) {
        // POWER LEFT_PARENTHESIS coverageExpression COMMA numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: pow(c, -0.5) or pow(c, avg(c))
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        Handler scalarExpressionHandler = (Handler) visit(ctx.numericalScalarExpression());

        Handler result = unaryPowerExpressionHandler.create(coverageExpressionHandler, scalarExpressionHandler);
        return result;
    }
    
    @Override
    public Handler visitUnaryModExpressionLabel(@NotNull wcpsParser.UnaryModExpressionLabelContext ctx) {
        // MOD LEFT_PARENTHESIS coverageExpression COMMA numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: pow(c, -0.5) or pow(c, avg(c))
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        Handler scalarExpr = (Handler) visit(ctx.numericalScalarExpression());

        Handler result = unaryModPowerExpressionHandler.create(coverageExpressionHandler, scalarExpr);
        return result;
    }

    @Override
    public Handler visitNotUnaryBooleanExpressionLabel(@NotNull wcpsParser.NotUnaryBooleanExpressionLabelContext ctx) {
        // NOT LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: not(c)
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());

        Handler result = unaryBooleanExpressionHandler.create(coverageExpressionHandler, null);
        return result;
    }

    @Override
    public Handler visitBitUnaryBooleanExpressionLabel(@NotNull wcpsParser.BitUnaryBooleanExpressionLabelContext ctx) {
        // BIT LEFT_PARENTHESIS coverageExpression COMMA numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: big(c, 2)
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        Handler scalarExpression = (Handler) visit(ctx.numericalScalarExpression());

        Handler result = unaryBooleanExpressionHandler.create(coverageExpressionHandler, scalarExpression);
        return result;
    }

    @Override
    public Handler visitCastExpressionLabel(@NotNull wcpsParser.CastExpressionLabelContext ctx) {
        // LEFT_PARENTHESIS rangeType RIGHT_PARENTHESIS coverageExpression
        // e.g: (char)(c + 5)
        String castType = StringUtils.join(ctx.rangeType().COVERAGE_VARIABLE_NAME(), " ");
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());

        Handler result = castExpressionHandler.create(this.stringScalarHandler.create(castType), coverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitCoverageExpressionRangeSubsettingLabel(@NotNull wcpsParser.CoverageExpressionRangeSubsettingLabelContext ctx) {
        // coverageExpression DOT fieldName  (e.g: c.red or (c + 1).red)
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        String fieldName = ctx.fieldName().getText();

        Handler result = rangeSubsettingHandler.create(coverageExpressionHandler, this.stringScalarHandler.create(fieldName));
        return result;
    }

    @Override
    public Handler visitNumericalRealNumberExpressionLabel(@NotNull wcpsParser.NumericalRealNumberExpressionLabelContext ctx) {
        // REAL_NUMBER_CONSTANT
        // e.g: 2, 3
        String number = ctx.getText();
        Handler result = this.realNumberConstantHandler.create(number);
        return result;
    }

    @Override
    public Handler visitNumericalNanNumberExpressionLabel(@NotNull wcpsParser.NumericalNanNumberExpressionLabelContext ctx) {
        // NAN_NUMBER_CONSTANT
        // e.g: c = nan
        Handler result = nanScalarHandler.create(ctx.getText());
        return result;
    }

    @Override
    public Handler visitCoverageExpressionCoverageLabel(@NotNull wcpsParser.CoverageExpressionCoverageLabelContext ctx) {
        // LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: used when a coverageExpression is surrounded by ().
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        Handler result = this.parenthesesCoverageExpressionHandler.create(coverageExpressionHandler);
        
        return result;
    }

    @Override
    public Handler visitWhereClauseLabel(@NotNull wcpsParser.WhereClauseLabelContext ctx) {
        // WHERE (LEFT_PARENTHESIS)? booleanScalarExpression (RIGHT_PARENTHESIS)?
        // e.g: where (c > 2)
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());

        Handler result = whereClauseHandler.create(coverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitBooleanUnaryScalarLabel(@NotNull wcpsParser.BooleanUnaryScalarLabelContext ctx) {
        // booleanUnaryOperator LEFT_PARENTHESIS? booleanScalarExpression RIGHT_PARENTHESIS?
        // e.g: not(1 > 2)
        String operator = ctx.booleanUnaryOperator().getText();
        Handler coverageExpressionHandler = (Handler) visit(ctx.booleanScalarExpression());

        Handler result = booleanUnaryScalarExpressionHandler.create(this.stringScalarHandler.create(operator), coverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitBooleanNumericalComparisonScalarLabel(@NotNull wcpsParser.BooleanNumericalComparisonScalarLabelContext ctx) {
        // numericalScalarExpression numericalComparissonOperator numericalScalarExpression
        // e.g: 1 >= avg(c) or (avg(c) = 2)
        Handler leftCoverageExpressionHandler = (Handler) visit(ctx.numericalScalarExpression(0));
        Handler rightCoverageExpressionHandler = (Handler) visit(ctx.numericalScalarExpression(1));
        String operator = ctx.numericalComparissonOperator().getText();

        Handler result = booleanNumericalComparisonScalarHandler.create(leftCoverageExpressionHandler, 
                                                                        this.stringScalarHandler.create(operator),
                                                                        rightCoverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitReduceBooleanExpressionLabel(@NotNull wcpsParser.ReduceBooleanExpressionLabelContext ctx) {
        // reduceBooleanExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: some(c), all(c)
        String operator = ctx.reduceBooleanExpressionOperator().getText();
        Handler coverageExpression = (Handler) visit(ctx.coverageExpression());

        Handler result = reduceExpressionHandler.create(this.stringScalarHandler.create(operator), coverageExpression);
        return result;
    }

    @Override
    public Handler visitCoverageExpressionComparissonLabel(@NotNull wcpsParser.CoverageExpressionComparissonLabelContext ctx) {
        // coverageExpression numericalComparissonOperator coverageExpression
        // e.g: ( (c + 1) > (c - 1) )
        Handler leftCoverageExpression = (Handler) visit(ctx.coverageExpression(0));
        String operator = ctx.numericalComparissonOperator().getText();
        Handler rightCoverageExpression = (Handler) visit(ctx.coverageExpression(1));

        Handler result = binaryCoverageExpressionHandler.create(leftCoverageExpression, 
                                                                this.stringScalarHandler.create(operator),
                                                                rightCoverageExpression);
        return result;
    }

    @Override
    public Handler visitBooleanBinaryScalarLabel(@NotNull wcpsParser.BooleanBinaryScalarLabelContext ctx) {
        // booleanScalarExpression booleanOperator booleanScalarExpression
        // Only use if both sides are scalar (e.g: (avg (c) > 5) or ( 3 > 2)
        Handler leftCoverageExpression = (Handler) visit(ctx.booleanScalarExpression(0));
        String operator = ctx.booleanOperator().getText();
        Handler rightCoverageExpression = (Handler) visit(ctx.booleanScalarExpression(1));

        Handler result = binaryCoverageExpressionHandler.create(leftCoverageExpression,
                                                                   this.stringScalarHandler.create(operator),
                                                                   rightCoverageExpression);
        return result;
    }

    @Override
    public Handler visitBooleanSwitchCaseCoverageExpression(@NotNull wcpsParser.BooleanSwitchCaseCoverageExpressionContext ctx) {
        // coverageExpression numericalComparissonOperator coverageExpression
        // NOTE: used in switch case (e.g: switch case c > 100 or c < 100 or c > c or c = c)
        
        Handler result = null;
        
        if (ctx.IS() != null) {
            // e.g c is NULL
            
            Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression().get(0));
            boolean isNull = true;        
            if (ctx.NOT() != null) {
                isNull = false;
            }
            
            String value = String.valueOf(isNull);
            result = coverageIsNullHandler.create(coverageExpressionHandler, this.stringScalarHandler.create(value));
        } else {
            // e.g c > 50
            
            Handler leftCoverageExpressionHandler = (Handler) visit(ctx.coverageExpression().get(0));
            String operand = ctx.numericalComparissonOperator().getText();
            Handler rightCoverageExpressionHandler = (Handler) visit(ctx.coverageExpression().get(1));

            result = binaryCoverageExpressionHandler.create(leftCoverageExpressionHandler,
                                                            this.stringScalarHandler.create(operand),
                                                            rightCoverageExpressionHandler);
        }
        
        return result;
    }

    @Override
    public Handler visitNumericalUnaryScalarExpressionLabel(@NotNull wcpsParser.NumericalUnaryScalarExpressionLabelContext ctx) {
        // numericalUnaryOperation LEFT_PARENTHESIS numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: abs(avg(c)), sqrt(avg(c + 1))
        String operator = ctx.numericalUnaryOperation().getText();
        Handler coverageExpressionHandler = (Handler) visit(ctx.numericalScalarExpression());

        Handler result = unaryArithmeticExpressionHandler.create(this.stringScalarHandler.create(operator), coverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitNumericalTrigonometricScalarExpressionLabel(@NotNull wcpsParser.NumericalTrigonometricScalarExpressionLabelContext ctx) {
        // trigonometricOperator LEFT_PARENTHESIS numericalScalarExpression RIGHT_PARENTHESIS
        // e.g: sin(avg(5))
        String operator = ctx.trigonometricOperator().getText();
        Handler coverageExpressionHandler = (Handler) visit(ctx.numericalScalarExpression());

        Handler result = unaryArithmeticExpressionHandler.create(this.stringScalarHandler.create(operator), coverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitNumericalBinaryScalarExpressionLabel(@NotNull wcpsParser.NumericalBinaryScalarExpressionLabelContext ctx) {
        // numericalScalarExpression numericalOperator numericalScalarExpression
        // e.g: avg(c) + 2, 5 + 3
        Handler firstCoverageExpressionHandler = (Handler) visit(ctx.numericalScalarExpression(0));
        String operator = ctx.numericalOperator().getText();
        Handler secondCoverageExpressionHandler = (Handler) visit(ctx.numericalScalarExpression(1));

        Handler result = this.binaryCoverageExpressionHandler.create(firstCoverageExpressionHandler,
                                                                    this.stringScalarHandler.create(operator),
                                                                    secondCoverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitComplexNumberConstantLabel(@NotNull wcpsParser.ComplexNumberConstantLabelContext ctx) {
        //  LEFT_PARENTHESIS REAL_NUMBER_CONSTANT COMMA REAL_NUMBER_CONSTANT RIGHT_PARENTHESIS
        // e.g: (2,5) = 2 + 5i
        String realNumberStr = ctx.REAL_NUMBER_CONSTANT(0).getText();
        String imagineNumberStr = ctx.REAL_NUMBER_CONSTANT(1).getText();

        Handler result = complexNumberConstantHandler.create(this.stringScalarHandler.create(realNumberStr),
                                                            this.stringScalarHandler.create(imagineNumberStr));
        return result;
    }

    @Override
    public Handler visitReduceNumericalExpressionLabel(@NotNull wcpsParser.ReduceNumericalExpressionLabelContext ctx) {
        // reduceNumericalExpressionOperator LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: count(c + 3), min(c), max(c - 2)
        String operator = ctx.reduceNumericalExpressionOperator().getText();
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());

        Handler result = reduceExpressionHandler.create(this.stringScalarHandler.create(operator), coverageExpressionHandler);
        return result;
    }
    
    // -- coverage constructor
    
    @Override
    public Handler visitCoverageConstructorExpressionLabel(@NotNull wcpsParser.CoverageConstructorExpressionLabelContext ctx) {
        // COVERAGE IDENTIFIER  
        // OVER axisIterator (COMMA axisIterator)* 
        // VALUES coverageExpression
        // e.g: coverage cov 
        //      over $px x(0:20), 
        //           $px y(0:20)
        //      values avg(c)
        String coverageName = ctx.COVERAGE_VARIABLE_NAME().getText();

        List<Handler> axisIteratorHandlers = new ArrayList<>();
        
        for (wcpsParser.AxisIteratorContext element : ctx.axisIterator()) {
            Handler axisIteratorHandler = visit(element);
            axisIteratorHandlers.add(axisIteratorHandler);
        }

        Handler valuesCoverageExpressionHandler = visit(ctx.coverageExpression());
        
        Handler result = this.coverageConstructorHandler.create(this.stringScalarHandler.create(coverageName), axisIteratorHandlers, valuesCoverageExpressionHandler);
        return result;
    }
    
    
    // -- coverage constant
    
    @Override
    public Handler visitCoverageConstantExpressionLabel(@NotNull wcpsParser.CoverageConstantExpressionLabelContext ctx) {
        // COVERAGE IDENTIFIER
        // OVER axisIterator (COMMA axisIterator)*
        // VALUE LIST LOWER_THAN   constant (SEMICOLON constant)*   GREATER_THAN
        // e.g: coverage cov 
        //      over $px x(0:20), 
        //           $py y(0:30) 
        //      values list<-1,0,1,2,2>
        String coverageName = ctx.COVERAGE_VARIABLE_NAME().getText();

        List<Handler> axisIteratorHandlers = new ArrayList<>();
        List<String> constants = new ArrayList<>();

        // parse the axis specifications
        for (wcpsParser.AxisIteratorContext element : ctx.axisIterator()) {
            Handler axisIteratorHandler = visit(element);
            axisIteratorHandlers.add(axisIteratorHandler);
        }

        //parse the constants (e.g: <-1,....1>)
        for (wcpsParser.ConstantContext element : ctx.constant()) {
            constants.add(element.getText());
        }
        
        Handler result = this.coverageConstantHandler.create(this.stringScalarHandler.create(coverageName),
                                                            axisIteratorHandlers,
                                                            this.stringScalarHandler.create(ListUtil.join(constants, ",")));
        return result;
    }


    
    // -- general condenser
    
    @Override
    public Handler visitGeneralCondenseExpressionLabel(@NotNull wcpsParser.GeneralCondenseExpressionLabelContext ctx) {
        //   CONDENSE condenseExpressionOperator
        //   OVER axisIterator (COMMA axisIterator)*
        //  (whereClause)?
        //   USING coverageExpression
        // e.g: condense +
        //      over $px x(0:100), 
        //           $py y(0:100)
        //      where ( max(c) < 100 )
        //      using c[i($x),j($y)]

        String operator = ctx.condenseExpressionOperator().getText();
        
        List<Handler> axisIteratorHandlers = new ArrayList<>();

        // to build the IndexCRS for axis iterator
        for (wcpsParser.AxisIteratorContext element : ctx.axisIterator()) {
            Handler axisIteratorHandler = visit(element);
            axisIteratorHandlers.add(axisIteratorHandler);
        }

        Handler whereClauseHandler = null;
        if (ctx.whereClause() != null) {
            whereClauseHandler = visit(ctx.whereClause());
        }

        Handler usingExpressionHandler = visit(ctx.coverageExpression());

        Handler result = this.generalCondenserHandler.create(this.stringScalarHandler.create(operator),
                                    axisIteratorHandlers,
                                    whereClauseHandler,
                                    usingExpressionHandler);            
        return result;
    }
    

    @Override
    public Handler visitCoverageExpressionShorthandSubsetLabel(@NotNull wcpsParser.CoverageExpressionShorthandSubsetLabelContext ctx) {
        //  coverageExpression LEFT_BRACKET dimensionIntervalList RIGHT_BRACKET
        // e.g: c[Lat(0:20), Long(0:30)] - Trim
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        Handler dimensionIntervalListHandler = visit(ctx.dimensionIntervalList());

        Handler result = this.shorthandSubsetHandler.create(coverageExpressionHandler, dimensionIntervalListHandler);
        return result;
    }
    
    @Override
    public Handler visitCoverageExpressionTrimCoverageLabel(@NotNull wcpsParser.CoverageExpressionTrimCoverageLabelContext ctx) {
        // TRIM LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE RIGHT_PARENTHESIS
        // e.g: trim(c, {Lat(0:20)})
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        Handler dimensionIntervalListHandler = visit(ctx.dimensionIntervalList());

        Handler result = this.shorthandSubsetHandler.create(coverageExpressionHandler, dimensionIntervalListHandler);
        return result;
    }

    @Override
    public Handler visitCoverageExpressionShorthandSliceLabel(@NotNull wcpsParser.CoverageExpressionShorthandSliceLabelContext ctx) {
        // coverageExpression LEFT_BRACKET dimensionPointList RIGHT_BRACKET
        // e.g: c[Lat(0)]
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        Handler dimensionIntervalListHandler = visit(ctx.dimensionPointList());
        
        Handler result = this.shorthandSubsetHandler.create(coverageExpressionHandler, dimensionIntervalListHandler);
        return result;
    }

    @Override
    public Handler visitCoverageExpressionSliceLabel(@NotNull wcpsParser.CoverageExpressionSliceLabelContext ctx) {
        // SLICE  LEFT_PARENTHESIS  coverageExpression  COMMA   LEFT_BRACE    dimensionPointList    RIGHT_BRACE   RIGHT_PARENTHESIS
        // e.g: slice(c, Lat(0))
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        Handler dimensionIntervalListHandler = visit(ctx.dimensionPointList());

        Handler result = this.shorthandSubsetHandler.create(coverageExpressionHandler, dimensionIntervalListHandler);
        return result;
    }


    @Override
    public Handler visitCoverageExpressionExtendLabel(@NotNull wcpsParser.CoverageExpressionExtendLabelContext ctx) {
        // EXTEND LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE RIGHT_PARENTHESIS
        // extend($c, {intervalList})
        // e.g: extend(c[t(0)], {Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)}
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        Handler dimensionIntervalListHandler = visit(ctx.dimensionIntervalList());

        Handler result = this.extendExpressionHandler.create(coverageExpressionHandler, dimensionIntervalListHandler);
        return result;
    }

    
    @Override
    public Handler visitCoverageExpressionExtendByDomainIntervalsLabel(@NotNull wcpsParser.CoverageExpressionExtendByDomainIntervalsLabelContext ctx) {
        // EXTEND LEFT_PARENTHESIS coverageExpression COMMA LEFT_BRACE domainIntervals RIGHT_BRACE RIGHT_PARENTHESIS
        // extend($c, {domain() or imageCrsdomain()})
        // e.g: extend(c[t(0)], { imageCrsdomain(Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)) })
        // NOTE: imageCrsdomain() or domain() will return metadata value not Rasql
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        Handler wcpsMetadataResult = visit(ctx.domainIntervals());

        Handler result = this.extendExpressionByDomainIntervalsHandler.create(coverageExpressionHandler, wcpsMetadataResult);
        return result;
    }
    
    @Override 
    public Handler visitRangeConstructorElementLabel(@NotNull wcpsParser.RangeConstructorElementLabelContext ctx) {
        // fieldName COLON coverageExpression
        String fieldName = ctx.fieldName().getText();
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        
        Handler resutl = this.rangeConstructorElementHandler.create(this.stringScalarHandler.create(fieldName), coverageExpressionHandler);
        return resutl;
    }
    
    @Override 
    public Handler visitRangeConstructorElementListLabel(@NotNull wcpsParser.RangeConstructorElementListLabelContext ctx) {
        // fieldName COLON coverageExpression
        List<Handler> childHandlers = new ArrayList<>();
        
        for (int i = 0; i < ctx.rangeConstructorElement().size(); i++) {
            Handler childHandler = visit(ctx.rangeConstructorElement().get(i));
            childHandlers.add(childHandler);
        }
        
        Handler result = this.rangeConstructorElementListHandler.create(childHandlers);
        return result;
    }

    
    @Override
    public Handler visitRangeConstructorExpressionLabel(@NotNull wcpsParser.RangeConstructorExpressionLabelContext ctx) {
        // LEFT_BRACE  (fieldName COLON coverageExpression) (SEMICOLON fieldName COLON coverageExpression)* RIGHT_BRACE
        // NOT used in switch case
        // e.g: {red: c.0, green: c.1, blue: c.2}
        Handler handler = visit(ctx.rangeConstructorElementList());
        Handler result = this.rangeConstructorHandler.create(handler);
        return result;
    }

    @Override
    public Handler visitScalarValueCoverageExpressionLabel(@NotNull wcpsParser.ScalarValueCoverageExpressionLabelContext ctx) {
        // scalarValueCoverageExpression: (LEFT_PARENTHESIS)?  coverageExpression (RIGHT_PARENTHESIS)?
        // e.g: for $c in (mr) return (c[i(0), j(0)] = 25 + 30 - 50)
        Handler result = (Handler) visit(ctx.coverageExpression());
        return result;
    }

    @Override
    public Handler visitStringScalarExpressionLabel(@NotNull wcpsParser.StringScalarExpressionLabelContext ctx) {
        // STRING_LITERAL
        // e.g: 1, c, x, y
        String value = ctx.STRING_LITERAL().getText();

        Handler result = this.stringScalarHandler.create(value);
        return result;
    }

    @Override
    public Handler visitCoverageIdentifierExpressionLabel(@NotNull wcpsParser.CoverageIdentifierExpressionLabelContext ctx) {
        // IDENTIFIER LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // identifier(), e.g: identifier($c) -> mr
        Handler coverageExpressionChildHandler = (Handler) visit(ctx.coverageExpression());

        Handler result = this.coverageIdentifierHandler.create(coverageExpressionChildHandler);
        return result;
    }
    
    @Override
    public Handler visitCellCountExpressionLabel(@NotNull wcpsParser.CellCountExpressionLabelContext ctx) {
        // CELLCOUNT LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // cellCount($c) -> 200 pixels
        Handler coverageExpressionChildHandler = (Handler) visit(ctx.coverageExpression());

        Handler result = this.cellCountHandler.create(coverageExpressionChildHandler);
        return result;
    }    
    

    @Override
    public Handler visitCoverageCrsSetExpressionLabel(@NotNull wcpsParser.CoverageCrsSetExpressionLabelContext ctx) {
        // CRSSET LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // crsSet(), e.g: crsSet($c) -> Lat:"http://...4326",  "CRS:1",
        //                              Long:"http://...4326", "CRS:1"

        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());

        Handler result = coverageCrsSetHandler.create(coverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitStarExpressionLabel(@NotNull wcpsParser.StarExpressionLabelContext ctx) {
        // MULTIPLICATION
        // e.g: c[Lat(*)]
        Handler result = stringScalarHandler.create(ASTERISK);
        return result;
    }

    
    // -- scale

    @Override
    public Handler visitCoverageExpressionScaleByFactorLabel(@NotNull wcpsParser.CoverageExpressionScaleByFactorLabelContext ctx) {
        // SCALE LEFT_PARENTHESIS
        //        coverageExpression COMMA number
        // RIGHT_PARENTHESIS
        // e.g: scale(c[t(0)], 2.5) with c is 3D coverage which means 2D output will be 
        // downscaled to 2.5 by each dimension (e.g: grid pixel is: 100 then the result is 100 / 2.5)
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        String factorNumber = ctx.number().getText();        
        
        Handler result = scaleExpressionByFactorHandler.create(coverageExpressionHandler, this.stringScalarHandler.create(factorNumber));        
        return result;
    }
    
    @Override
    public Handler visitCoverageExpressionScaleByAxesLabel(@NotNull wcpsParser.CoverageExpressionScaleByAxesLabelContext ctx) {
        // SCALE_AXES LEFT_PARENTHESIS
        //        coverageExpression COMMA scaleDimensionIntervalList
        // RIGHT_PARENTHESIS
        // e.g: scaleaxes(c[t(0)], [Lat(2.5), Long(2.5)]) with c is 3D coverage which means 2D output will be 
        // downscaled to 2.5 by each dimension (e.g: grid pixel is: 100 then the result is 100 / 2.5)
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        Handler scaleAxesDimensionListHandler = visit(ctx.scaleDimensionIntervalList());
        
        Handler result = scaleExpressionByScaleAxesHandler.create(coverageExpressionHandler, scaleAxesDimensionListHandler);        
        return result;
    }
    
    @Override
    public Handler visitCoverageExpressionScaleBySizeLabel(@NotNull wcpsParser.CoverageExpressionScaleBySizeLabelContext ctx) {
        // SCALE_SIZE LEFT_PARENTHESIS
        //        coverageExpression COMMA scaleDimensionIntervalList
        // RIGHT_PARENTHESIS
        // e.g: scalesize(c[t(0)], [Lat(25), Long(25)]) with c is 3D coverage which means 2D output will have grid domain: 0:24, 0:24 (25 pixesl for each dimension)
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        Handler scaleDimensionIntervalListHandler = visit(ctx.scaleDimensionIntervalList());
        
        Handler result = scaleExpressionByScaleSizeHandler.create(coverageExpressionHandler, scaleDimensionIntervalListHandler);        
        return result;
    }
  
    @Override
    public Handler visitCoverageExpressionScaleByExtentLabel(@NotNull wcpsParser.CoverageExpressionScaleByExtentLabelContext ctx) {
        // SCALE_EXTENT LEFT_PARENTHESIS
        //        coverageExpression COMMA scaleDimensionIntervalList
        // RIGHT_PARENTHESIS
        // e.g: scaleextent(c[t(0)], [Lat(25:30), Long(25:30)]) with c is 3D coverage which means 2D output will have grid domain: 25:30, 25:30 (6 pixesl for each dimension)
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        Handler scaleAxesDimensionListHandler = visit(ctx.scaleDimensionIntervalList());
        
        Handler result = scaleExpressionByScaleExtentHandler.create(coverageExpressionHandler, scaleAxesDimensionListHandler);
        return result;
    }

    @Override
    public Handler visitCoverageExpressionScaleByDimensionIntervalsLabel(@NotNull wcpsParser.CoverageExpressionScaleByDimensionIntervalsLabelContext ctx) {
        // SCALE LEFT_PARENTHESIS
        //          coverageExpression COMMA LEFT_BRACE dimensionIntervalList RIGHT_BRACE (COMMA fieldInterpolationList)*
        //       RIGHT_PARENTHESIS
        // scale($c, {intervalList})
        // e.g: scale(c[t(0)], {Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)}

        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        Handler dimensionIntervalListHandler = visit(ctx.dimensionIntervalList());

        Handler result = scaleExpressionByDimensionIntervalsHandler.create(coverageExpressionHandler, dimensionIntervalListHandler);
        return result;
    }
    
    @Override
    public Handler visitCoverageExpressionScaleByImageCrsDomainLabel(@NotNull wcpsParser.CoverageExpressionScaleByImageCrsDomainLabelContext ctx) {
        // SCALE LEFT_PARENTHESIS
        //        coverageExpression COMMA LEFT_BRACE domainIntervals RIGHT_BRACE
        // RIGHT_PARENTHESIS
        // scale($c, { imageCrsDomain() or domain() }) - domain() can only be 1D
        // e.g: scale(c[t(0)], { imageCrsdomain(Lat:"CRS:1"(0:200), Long:"CRS:1"(0:300)) })
        // NOTE: imageCrsdomain() or domain() will return metadata value not Rasql
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        Handler domainIntervalsHandler = visit(ctx.domainIntervals());

        Handler handler = scaleExpressionByImageCrsDomainHandler.create(coverageExpressionHandler, domainIntervalsHandler);
        return handler;
    }

    @Override
    public Handler visitImageCrsExpressionLabel(@NotNull wcpsParser.ImageCrsExpressionLabelContext ctx) {
        // IMAGECRS LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // imageCrs() - return coverage's grid axis
        // e.g: for c in (mr) return imageCrs(c) (imageCrs is the grid CRS of coverage)
        // return: CRS:1
        Handler coverageExressionHandler = (Handler) visit(ctx.coverageExpression());

        Handler result = imageCrsExpressionHandler.create(coverageExressionHandler);
        return result;
    }

    @Override
    public Handler visitImageCrsDomainExpressionLabel(@NotNull wcpsParser.ImageCrsDomainExpressionLabelContext ctx) {
        // IMAGECRSDOMAIN LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // imageCrsDomain($c) - can be 2D, 3D,.. depend on coverageExpression
        // e.g: c[t(0), Lat(0:20), Long(0:30)] is 2D
        // return (0:5,0:100,0:231), used with scale, extend (scale(, { imageCrsdomain() })
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());

        Handler result = imageCrsDomainExpressionHandler.create(coverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitImageCrsDomainByDimensionExpressionLabel(@NotNull wcpsParser.ImageCrsDomainByDimensionExpressionLabelContext ctx) {
        // IMAGECRSDOMAIN LEFT_PARENTHESIS coverageExpression COMMA axisName RIGHT_PARENTHESIS
        // imageCrsDomain($c, axisName) - 1D
        // return (0:5), used with axis iterator ($px x ( imageCrsdomain() ))
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        String axisName = ctx.axisName().getText();

        Handler result = imageCrsDomainExpressionByDimensionExpressionHandler.create(coverageExpressionHandler, this.stringScalarHandler.create(axisName));
        return result;
    }

    @Override
    public Handler visitDomainExpressionLabel(@NotNull wcpsParser.DomainExpressionLabelContext ctx) {
        // IMAGECRSDOMAIN LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // domain() - 1D
        // e.g: for c in (mean_summer_airtemp) return domain(c[Lat(0:20)], Lat, "http://.../4326")
        // return: (0:20) as domain of inpurt coverage in Lat is 0:20
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        String axisName = null;
        if (ctx.axisName() != null) {
            axisName = ctx.axisName().getText();
        }
        
        String crsName = null;
        if (ctx.crsName() != null) {
            // NOTE: need to strip bounding quotes of crs (e.g: ""http://.../4326"")
            crsName = CrsUtility.stripBoundingQuotes(ctx.crsName().getText());
        }

        Handler result = domainExpressionHandler.create(coverageExpressionHandler, 
                                                    this.stringScalarHandler.create(axisName),
                                                    this.stringScalarHandler.create(crsName));
        return result;
    }
    
    
    @Override 
    public Handler visitGeoXYAxisLabelAndDomainResolutionLabel(@NotNull wcpsParser.GeoXYAxisLabelAndDomainResolutionLabelContext ctx) { 
        Handler coverageExpressionHandler = (Handler) visit(ctx.coverageExpression());
        if (ctx.axisName() == null) {
            throw new WCPSException(ExceptionCode.InvalidAxisLabel, "axisLabel in domain(coverageExpression, axisLabel).resolution must not be null.");
        } 
        
        String axisName = ctx.axisName().getText();
        
        String crsName = null;
        
        Handler childHandler = domainExpressionHandler.create(coverageExpressionHandler, 
                                                    this.stringScalarHandler.create(axisName),
                                                    this.stringScalarHandler.create(crsName));
        Handler result = this.domainIntervalsHandler.create(childHandler, this.stringScalarHandler.create(DomainIntervalsHandler.DOMAIN_PROPERTY_RESOLUTION));
        return result;
    } 
    
    @Override 
    public Handler visitDomainIntervals(@NotNull wcpsParser.DomainIntervalsContext ctx) {
        // (domainExpression | imageCrsDomainExpression | imageCrsDomainByDimensionExpression) (sdomExtraction)?
        // e.g: imageCrsdomain(c, Lat).lo or imageCrsdomain(c, Lat).hi
        String propertyValue = null;
        
        if (ctx.domainPropertyValueExtraction()!= null) {
            if (ctx.domainPropertyValueExtraction().LOWER_BOUND() != null) {
                propertyValue = DOMAIN_PROPERTY_LOWER_BOUND;
            } else if (ctx.domainPropertyValueExtraction().UPPER_BOUND() != null) {
                propertyValue = DOMAIN_PORPERTY_UPPER_BOUND;
            } else if (ctx.domainPropertyValueExtraction().RESOLUTION() != null) {
                propertyValue = DOMAIN_PROPERTY_RESOLUTION;
            }
        }
        
        Handler childHandler = null;
        if (ctx.domainExpression() != null) {
            childHandler = visit(ctx.domainExpression());
        } else if (ctx.imageCrsDomainExpression() != null) {
            childHandler = visit(ctx.imageCrsDomainExpression());
        } else if (ctx.imageCrsDomainByDimensionExpression()!= null) {
            childHandler = visit(ctx.imageCrsDomainByDimensionExpression());
        }
        
        Handler result = this.domainIntervalsHandler.create(childHandler, this.stringScalarHandler.create(propertyValue));
        return result;
    }
    
    
    // -- switch case    
    
    @Override
    public Handler visitBooleanSwitchCaseCombinedExpression(@NotNull wcpsParser.BooleanSwitchCaseCombinedExpressionContext ctx) {
        // Handle combined boolean expressions in a case expression,
        // e.g: case (($c.red <= 100) and ($c.red > 50) )
        Handler result = null;
        if (ctx.booleanOperator() != null) {
            Handler leftOperandHandler = null;
            Handler rightOperandHandler = null;
            
            if (ctx.booleanSwitchCaseCombinedExpression().size() > 0) {
                leftOperandHandler = (Handler) visit(ctx.booleanSwitchCaseCombinedExpression().get(0));
                rightOperandHandler = (Handler) visit(ctx.booleanSwitchCaseCombinedExpression().get(1));
            } else {
                leftOperandHandler = (Handler) visit(ctx.booleanSwitchCaseCoverageExpression().get(0));
                rightOperandHandler = (Handler) visit(ctx.booleanSwitchCaseCoverageExpression().get(1));
            }
            
            String operator = ctx.booleanOperator().getText();
            result = binaryCoverageExpressionHandler.create(leftOperandHandler, this.stringScalarHandler.create(operator), rightOperandHandler);
        } else {
            result = (Handler) visit(ctx.booleanSwitchCaseCoverageExpression().get(0));
        }
        
        return result;
    }
    
    @Override
    public Handler visitSwitchCaseElement(@NotNull wcpsParser.SwitchCaseElementContext ctx) {
        // switchCaseElement: CASE booleanSwitchCaseCombinedExpression RETURN coverageExpression;
        Handler booleanCoverageExpressionHandler = visit(ctx.booleanSwitchCaseCombinedExpression());
        Handler returnValueCoverageExpressionHandler = visit(ctx.coverageExpression());
        
        Handler result = this.switchCaseElementHandler.create(booleanCoverageExpressionHandler, returnValueCoverageExpressionHandler);
        return result;
    }
    
    @Override
    public Handler visitSwitchCaseElementList(@NotNull wcpsParser.SwitchCaseElementListContext ctx) { 
        // switchCaseElement (switchCaseElement)*;
        List<Handler> handlers = new ArrayList<>();
        for (int i = 0; i < ctx.switchCaseElement().size(); i++) {
            Handler handler = visit(ctx.switchCaseElement().get(i));
            handlers.add(handler);
        }
        
        Handler result = this.switchCaseElementListHandler.create(handlers);
        return result;
    }
    
    @Override
    public Handler visitSwitchCaseDefaultElement(@NotNull wcpsParser.SwitchCaseDefaultElementContext ctx) {
        // DEFAULT RETURN coverageExpression
        Handler handler = visit(ctx.coverageExpression());
        Handler result = this.switchCaseDefaultValueHandler.create(handler);
        
        return result;
    }

    @Override
    public Handler visitSwitchCaseExpressionLabel(@NotNull wcpsParser.SwitchCaseExpressionLabelContext ctx) {
        // switch case which returns scalar values / range constructor values
        // e.g: e.g: for c in (mr) return encode(
        //               switch case c > 10 and c < 20 return (char)5
        //               default return 2
        //           ,"csv")
        Handler switchCaseElementListHandler = visit(ctx.switchCaseElementList());
        Handler defaultValueElementHandler = visit(ctx.switchCaseDefaultElement());

        Handler result = this.switchCaseExpressionHandler.create(switchCaseElementListHandler, defaultValueElementHandler);
        return result;
    }
    
    @Override
    public Handler visitCoverageIsNullExpression(@NotNull wcpsParser.CoverageIsNullExpressionContext ctx) {
        // coverageExpression IS NULL
        // e.g: encode(c is null, "csv") then if c's nodata = 0, then all the pixels of c with 0 values will return true, others return false.
        Handler coverageExpression = (Handler)visit(ctx.coverageExpression());
        boolean isNull = true;        
        if (ctx.NOT() != null) {
            isNull = false;
        }
        String booleanValue = String.valueOf(isNull);
        Handler result = coverageIsNullHandler.create(coverageExpression, this.stringScalarHandler.create(booleanValue));
        return result;
    }  

    // PARAMETERS
    /* ----------- Parameters objects for nodes ----------- */
    
    // -- subset
    
    @Override
    public Handler visitDimensionPointElementLabel(@NotNull wcpsParser.DimensionPointElementLabelContext ctx) {
        // axisName (COLON crsName)? LEFT_PARENTHESIS coverageExpression RIGHT_PARENTHESIS
        // e.g: i(5) - Slicing point
        String axisName = ctx.axisName().getText();
        String crs = null;
        if (ctx.crsName() != null) {
            crs = StringUtil.stripFirstAndLastQuotes(ctx.crsName().getText());
        }
        
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        Handler result = this.dimensionPointElementHandler.create(this.stringScalarHandler.create(axisName),
                                                                  this.stringScalarHandler.create(crs), 
                                                                  coverageExpressionHandler);
        return result;
    }

    @Override
    public Handler visitDimensionPointListLabel(@NotNull wcpsParser.DimensionPointListLabelContext ctx) {
        // dimensionPointElement (COMMA dimensionPointElement)*
        // e.g: i(0), j(0) - List of Slicing points
        List<Handler> childHandlers = new ArrayList<>(ctx.dimensionPointElement().size());
        for (wcpsParser.DimensionPointElementContext element : ctx.dimensionPointElement()) {
            childHandlers.add((Handler) visit(element));
        }

        Handler result = this.dimensionPointListElementHandler.create(childHandlers);
        return result;
    }
    
    @Override
    public Handler visitSliceDimensionIntervalElementLabel(@NotNull wcpsParser.SliceDimensionIntervalElementLabelContext ctx) {
        // axisName (COLON crsName)?  LEFT_PARENTHESIS   coverageExpression   RIGHT_PARENTHESIS
        // e.g: i(0)

        if (ctx.axisName() == null) {
            throw new InvalidAxisNameException("No axis given");
        }        
        String axisName = ctx.axisName().getText();
        
        String crs = ctx.crsName() == null ? "" : StringUtil.stripFirstAndLastQuotes(ctx.crsName().getText());
        Handler coverageExpressionHandler = visit(ctx.coverageExpression());
        
        Handler result = this.sliceDimensionIntervalElementHandler.create(this.stringScalarHandler.create(axisName),
                                                                        this.stringScalarHandler.create(crs),
                                                                        coverageExpressionHandler);
        return result;
    }
    
    
    @Override
    public Handler visitTrimDimensionIntervalElementLabel(@NotNull wcpsParser.TrimDimensionIntervalElementLabelContext ctx) {
        // axisName (COLON crsName)? LEFT_PARENTHESIS  coverageExpression   COLON coverageExpression    RIGHT_PARENTHESIS
        // e.g: i:"CRS:1"(2:3)
        if (ctx.axisName() == null) {
            throw new InvalidAxisNameException("No axis given");
        }
        String axisName = ctx.axisName().getText();
        
        String crs = null;
        if (ctx.crsName() != null) {
            crs = StringUtil.stripFirstAndLastQuotes(ctx.crsName().getText());
        }
        
        Handler lowerBoundCoveragExpression = ((Handler)visit(ctx.coverageExpression(0)));
        Handler upperBoundCoveragExpression = ((Handler)visit(ctx.coverageExpression(1)));
        
        Handler result = this.trimDimensionIntervalElementHandler.create(this.stringScalarHandler.create(axisName),
                                                                    this.stringScalarHandler.create(crs),
                                                                    lowerBoundCoveragExpression, upperBoundCoveragExpression);
        return result;
        
        
    }    

    @Override
    public Handler visitTrimDimensionIntervalByImageCrsDomainElementLabel(@NotNull wcpsParser.TrimDimensionIntervalByImageCrsDomainElementLabelContext ctx) {
        // axisName (COLON crsName)? LEFT_PARENTHESIS  imageCrsDomainByDimensionExpression  RIGHT_PARENTHESIS
        // e.g: i:"CRS:1"( imageCrsdomain(c[i(30:50)], i) )
        if (ctx.axisName() == null) {
            throw new InvalidAxisNameException("No axis given");
        }
        String axisName = ctx.axisName().getText();
        
        String crs = null;
        if (ctx.crsName() != null) {
            crs = StringUtil.stripFirstAndLastQuotes(ctx.crsName().getText());
        }
        
        Handler gridBoundCoveragExpression = ((Handler)visit(ctx.imageCrsDomainByDimensionExpression()));
        
        Handler result = this.trimDimensionIntervalByImageCrsDomainElementHandler.create(
                                                                    this.stringScalarHandler.create(axisName),
                                                                    this.stringScalarHandler.create(crs),
                                                                    gridBoundCoveragExpression);
        return result;
        
        
    }

    @Override
    public Handler visitDimensionIntervalListLabel(@NotNull wcpsParser.DimensionIntervalListLabelContext ctx) {
        // dimensionIntervalElement (COMMA dimensionIntervalElement)*
        // e.g: c[i(0:20),j(0:30)]
        List<Handler> childHandlers = new ArrayList<>();
        for (wcpsParser.DimensionIntervalElementContext element : ctx.dimensionIntervalElement()) {
            childHandlers.add((Handler) visit(element));
        }
        
        Handler result = this.dimensionIntervalListHandler.create(childHandlers);
        return result;
    }

    @Override
    public Handler visitIntervalExpressionLabel(@NotNull wcpsParser.IntervalExpressionLabelContext ctx) {
        // scalarExpression COLON scalarExpression  (e.g: 5:10)
        String lowIntervalStr = ctx.scalarExpression(0).getText();
        String highIntervalStr = ctx.scalarExpression(1).getText();
        
        Handler result = this.intervalExpressionHandler.create(this.stringScalarHandler.create(lowIntervalStr), 
                                                               this.stringScalarHandler.create(highIntervalStr));
        return result;
    }
    
    // -- scale
  
    @Override
    public Handler visitSliceScaleDimensionIntervalElementLabel(@NotNull wcpsParser.SliceScaleDimensionIntervalElementLabelContext ctx) {
        // axisName LEFT_PARENTHESIS   number   RIGHT_PARENTHESIS
        // e.g: i(0.5) and is used for scaleaxes, scalesize expression, e.g: scale(c, [i(0.5)])
        String axisLabel = ctx.axisName().getText();
        String scaleFactor = ctx.number().getText();
        
        Handler result = this.sliceScaleDimensionIntervalElement.create(this.stringScalarHandler.create(axisLabel),
                                                                        this.stringScalarHandler.create(scaleFactor));
        return result;
    }
    
    @Override
    public Handler visitTrimScaleDimensionIntervalElementLabel(@NotNull wcpsParser.TrimScaleDimensionIntervalElementLabelContext ctx) {
        // axisName LEFT_PARENTHESIS   number   RIGHT_PARENTHESIS
        // e.g: i(20:30) and is used for scaleextent expression, e.g: scale(c, [i(20:30)])
        String axisLabel = ctx.axisName().getText();
        
        String lowerBound = "";
        String upperBound = "";
        
        if (ctx.number().size() > 0) {
            lowerBound = ctx.number().get(0).getText();
            upperBound = ctx.number().get(1).getText();
        } else {
            lowerBound = ctx.STRING_LITERAL().get(0).getText();
            upperBound = ctx.STRING_LITERAL().get(1).getText();
        }
        
        Handler result = this.trimScaleDimensionIntervalElement.create(
                                                        this.stringScalarHandler.create(axisLabel),
                                                        this.stringScalarHandler.create(lowerBound),
                                                        this.stringScalarHandler.create(upperBound));
        return result;
    }
    
    @Override
    public Handler visitScaleDimensionIntervalListLabel(@NotNull wcpsParser.ScaleDimensionIntervalListLabelContext ctx) {
        // scaleDimensionIntervalElement (COMMA scaleDimensionIntervalElement)* 
        // e.g: [i(0.5),j(0.5)]
        List<Handler> handlers = new ArrayList<>();
        for (wcpsParser.ScaleDimensionIntervalElementContext element : ctx.scaleDimensionIntervalElement()) {
            handlers.add(visit(element));
        }
        
        Handler result = this.scaleDimensionIntervalListHandler.create(handlers);
        return result;
    }    

    @Override
    public Handler visitAxisSpecLabel(@NotNull wcpsParser.AxisSpecLabelContext ctx) {
        // dimensionIntervalElement (e.g: i(0:20) or j:"CRS:1"(0:30))
        Handler subsetDimensionHandler = visit(ctx.dimensionIntervalElement());
        Handler result = this.axisSpecHandler.create(subsetDimensionHandler);
        return result;
    }

    @Override
    public Handler visitAxisIteratorLabel(@NotNull wcpsParser.AxisIteratorLabelContext ctx) {
        // coverageVariableName dimensionIntervalElement (e.g: $px x(Lat(0:20)) )
        String coverageVariableName = ctx.coverageVariableName().getText();
        Handler subsetDimensionHandler = visit(ctx.dimensionIntervalElement());

        Handler result = this.axisIteratorHandler.create(this.stringScalarHandler.create(coverageVariableName), subsetDimensionHandler);
        return result;
    }

    @Override
    public Handler visitAxisIteratorDomainIntervalsLabel(@NotNull wcpsParser.AxisIteratorDomainIntervalsLabelContext ctx) {
        // coverageVariableName axisName LEFT_PARENTHESIS  domainIntervals RIGHT_PARENTHESIS
        // e.g: $px x (imageCrsdomain(c[Lat(0:20)]), Lat)
        // e.g: $px x (imageCrsdomain(c[Long(0)], Lat[(0:20)]))
        // e.g: $px x (domain(c[Lat(0:20)], Lat, "http://.../4326"))
        // return x in (50:80)

        String coverageVariableName = ctx.coverageVariableName().getText();
        Handler domainIntervalsHandler = visit(ctx.domainIntervals());
        
        Handler result = this.axisIteratorDomainIntervalsHandler.create(this.stringScalarHandler.create(coverageVariableName), domainIntervalsHandler);
        return result;
    }
    
    // -- clip
    
    @Override
    public Handler visitWktPointsLabel(@NotNull wcpsParser.WktPointsLabelContext ctx) {
        // Handle wktPoints (coordinates inside WKT): constant (constant)*) (COMMA constant (constant)*)*
        // e.g: 20 30 "2017-01-01T02:35:50", 30 40 "2017-01-05T02:35:50", 50 60 "2017-01-07T02:35:50" 
        int numberOfCompoundPoints = ctx.constant().size();
        int numberOfDimensions = numberOfCompoundPoints / (ctx.COMMA().size() + 1);
        int count = 0;
        List<String> listTmp = new ArrayList<>();
        String point = "";
        // NOTE: there is a huge bottle neck if using normal for loop with counter i in ANTLR4
        // (e.g: with 100 000 elements, it can take minutes to just iterate) with foreach it takes ms.
        for (wcpsParser.ConstantContext constant : ctx.constant()) {
            String pointTmp = constant.getText();
            point = point + " " + pointTmp;
            if (count < numberOfDimensions - 1) {                
                count++;
            } else {
                listTmp.add(point.trim());
                point = "";
                count = 0;
            }            
        }   
        
        // e.g. 10 30,40 50,60 70
        String compoundPoint = ListUtil.join(listTmp, ",");
        Handler result = this.wktCompoundPointHandler.create(this.stringScalarHandler.create(compoundPoint),
                                                            this.stringScalarHandler.create(String.valueOf(numberOfDimensions)));
        return result;
    }
  
    @Override
    public Handler visitWKTPointElementListLabel(@NotNull wcpsParser.WKTPointElementListLabelContext ctx) {
        // Handle LEFT_PARENTHESIS wktPoints RIGHT_PARENTHESIS (COMMA LEFT_PARENTHESIS wktPoints RIGHT_PARENTHESIS)*
        // e.g: (20 30, 40 50), (40 60, 70 80) are considered as 2 WKTCompoundPoints (1 is 20 30 40 50, 2 is 40 60 70 80)
        List<Handler> compoundPoints = new ArrayList<>();
        for (wcpsParser.WktPointsContext element : ctx.wktPoints()) {            
            compoundPoints.add(visit(element));
        }
        
        Handler result = this.wktCompoundPointsListHandler.create(compoundPoints);
        return result;
    }
  
    @Override
    public Handler visitWKTLineStringLabel(@NotNull wcpsParser.WKTLineStringLabelContext ctx) {
        // Handle LINESTRING wktPointElementList
        // e.g: LineString(20 30, 40 50)
        Handler wktCompoundPointsHandler = visit(ctx.wktPointElementList());         
        Handler result = this.wktLineStringHandler.create(wktCompoundPointsHandler);
        return result;
    }
  
    @Override
    public Handler visitWKTPolygonLabel(@NotNull wcpsParser.WKTPolygonLabelContext ctx) {
        // Handle POLYGON LEFT_PARENTHESIS wktPointElementList RIGHT_PARENTHESIS
        // e.g: POLYGON((20 30, 40 50), (60 70, 70 80))
        Handler wktCompoundPointsHandler = visit(ctx.wktPointElementList());        
        Handler result = this.wktPolygonHandler.create(wktCompoundPointsHandler);
        return result;
    }
  
    @Override
    public Handler visitWKTMultipolygonLabel(@NotNull wcpsParser.WKTMultipolygonLabelContext ctx) {
        // Handle MULTIPOLYGON LEFT_PARENTHESIS 
        //                         LEFT_PARENTHESIS wktPointElementList RIGHT_PARENTHESIS
        //                         (COMMA LEFT_PARENTHESIS wktPointElementList RIGHT_PARENTHESIS)* 
        //                     RIGHT_PARENTHESIS
        // e.g: Multipolygon( ((20 30, 40 50, 60 70)), ((20 30, 40 50), (60 70, 80 90)) )
        List<Handler> wktCompoundPointsHandlers = new ArrayList();
        for (wcpsParser.WktPointElementListContext element : ctx.wktPointElementList()) {            
            wktCompoundPointsHandlers.add(visit(element));
        }
        
        Handler result = this.wktMultiPolygonHandler.create(wktCompoundPointsHandlers);        
        return result;
    }
    
    @Override
    public Handler visitWKTCoverageExpressionLabel(@NotNull wcpsParser.WKTCoverageExpressionLabelContext ctx) { 
        // Handle LET clause expression
        // Used only for LET clause, e.g. let $wkt := POLYGON((...))), then here clip($c, $wkt)
        return visitChildren(ctx);
    }
  
    @Override
    public Handler visitWKTExpressionLabel(@NotNull wcpsParser.WKTExpressionLabelContext ctx) {
        // Handle WKT expression in clip() operator. WKT can be linestring, polygon, multipolygon.
        Handler result = null;
        if (ctx.wktLineString() != null) {
            result = visit(ctx.wktLineString());
        } else if (ctx.wktPolygon() != null) {
            result = visit(ctx.wktPolygon());
        } else if (ctx.wktMultipolygon() != null) {
            result = visit(ctx.wktMultipolygon());
        } else if (ctx.coverageExpression() != null) {
            result = visit(ctx.coverageExpression());
        }
        
        return result;
    }
}
