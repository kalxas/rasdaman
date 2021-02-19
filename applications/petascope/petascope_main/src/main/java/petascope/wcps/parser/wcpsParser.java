// Generated from wcps.g4 by ANTLR 4.1
package petascope.wcps.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class wcpsParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		FOR=1, ABSOLUTE_VALUE=2, ADD=3, ALL=4, AND=5, ARCSIN=6, ARCCOS=7, ARCTAN=8, 
		AVG=9, BIT=10, CASE=11, CLIP=12, COLON=13, COMMA=14, CONDENSE=15, COS=16, 
		COSH=17, COUNT=18, CURTAIN=19, CORRIDOR=20, COVERAGE=21, COVERAGE_VARIABLE_NAME_PREFIX=22, 
		CRS_TRANSFORM=23, DECODE=24, DEFAULT=25, DISCRETE=26, DESCRIBE_COVERAGE=27, 
		DIVISION=28, DOT=29, ENCODE=30, EQUAL=31, EXP=32, EXTEND=33, FALSE=34, 
		GREATER_THAN=35, GREATER_OR_EQUAL_THAN=36, IMAGINARY_PART=37, IDENTIFIER=38, 
		CRSSET=39, IMAGECRSDOMAIN=40, IMAGECRS=41, IS=42, DOMAIN=43, IN=44, LEFT_BRACE=45, 
		LEFT_BRACKET=46, LEFT_PARENTHESIS=47, LET=48, LN=49, LIST=50, LOG=51, 
		LOWER_BOUND=52, LOWER_THAN=53, LOWER_OR_EQUAL_THAN=54, MAX=55, MIN=56, 
		MINUS=57, MULTIPLICATION=58, NOT=59, NOT_EQUAL=60, NAN_NUMBER_CONSTANT=61, 
		NULL=62, OR=63, OVER=64, OVERLAY=65, QUOTE=66, ESCAPED_QUOTE=67, PLUS=68, 
		POWER=69, REAL_PART=70, ROUND=71, RETURN=72, RIGHT_BRACE=73, RIGHT_BRACKET=74, 
		RIGHT_PARENTHESIS=75, SCALE=76, SCALE_FACTOR=77, SCALE_AXES=78, SCALE_SIZE=79, 
		SCALE_EXTENT=80, SEMICOLON=81, SIN=82, SINH=83, SLICE=84, SOME=85, SQUARE_ROOT=86, 
		STRUCT=87, SWITCH=88, TAN=89, TANH=90, TRIM=91, TRUE=92, USING=93, UPPER_BOUND=94, 
		VALUE=95, VALUES=96, WHERE=97, XOR=98, POLYGON=99, LINESTRING=100, MULTIPOLYGON=101, 
		PROJECTION=102, WITH_COORDINATES=103, INTEGER=104, REAL_NUMBER_CONSTANT=105, 
		SCIENTIFIC_NUMBER_CONSTANT=106, POSITIONAL_PARAMETER=107, COVERAGE_VARIABLE_NAME=108, 
		STRING_LITERAL=109, WS=110, EXTRA_PARAMS=111;
	public static final String[] tokenNames = {
		"<INVALID>", "FOR", "ABSOLUTE_VALUE", "ADD", "ALL", "AND", "ARCSIN", "ARCCOS", 
		"ARCTAN", "AVG", "BIT", "CASE", "CLIP", "':'", "','", "CONDENSE", "COS", 
		"COSH", "COUNT", "CURTAIN", "CORRIDOR", "COVERAGE", "'$'", "CRS_TRANSFORM", 
		"DECODE", "DEFAULT", "DISCRETE", "DESCRIBE_COVERAGE", "'/'", "'.'", "ENCODE", 
		"'='", "EXP", "EXTEND", "FALSE", "'>'", "'>='", "IMAGINARY_PART", "IDENTIFIER", 
		"CRSSET", "IMAGECRSDOMAIN", "IMAGECRS", "IS", "DOMAIN", "IN", "'{'", "'['", 
		"'('", "LET", "LN", "LIST", "LOG", "LOWER_BOUND", "'<'", "'<='", "MAX", 
		"MIN", "'-'", "'*'", "NOT", "'!='", "NAN_NUMBER_CONSTANT", "NULL", "OR", 
		"OVER", "OVERLAY", "'\"'", "'\\\"'", "'+'", "POWER", "REAL_PART", "ROUND", 
		"RETURN", "'}'", "']'", "')'", "SCALE", "SCALE_FACTOR", "SCALE_AXES", 
		"SCALE_SIZE", "SCALE_EXTENT", "';'", "SIN", "SINH", "SLICE", "SOME", "SQUARE_ROOT", 
		"STRUCT", "SWITCH", "TAN", "TANH", "TRIM", "TRUE", "USING", "UPPER_BOUND", 
		"VALUE", "VALUES", "WHERE", "XOR", "POLYGON", "LINESTRING", "MULTIPOLYGON", 
		"PROJECTION", "WITH_COORDINATES", "INTEGER", "REAL_NUMBER_CONSTANT", "SCIENTIFIC_NUMBER_CONSTANT", 
		"POSITIONAL_PARAMETER", "COVERAGE_VARIABLE_NAME", "STRING_LITERAL", "WS", 
		"EXTRA_PARAMS"
	};
	public static final int
		RULE_wcpsQuery = 0, RULE_forClauseList = 1, RULE_coverageIdForClause = 2, 
		RULE_forClause = 3, RULE_letClauseList = 4, RULE_letClauseDimensionIntervalList = 5, 
		RULE_letClause = 6, RULE_whereClause = 7, RULE_returnClause = 8, RULE_coverageVariableName = 9, 
		RULE_processingExpression = 10, RULE_scalarValueCoverageExpression = 11, 
		RULE_scalarExpression = 12, RULE_booleanScalarExpression = 13, RULE_booleanUnaryOperator = 14, 
		RULE_booleanConstant = 15, RULE_booleanOperator = 16, RULE_numericalComparissonOperator = 17, 
		RULE_stringOperator = 18, RULE_stringScalarExpression = 19, RULE_starExpression = 20, 
		RULE_booleanSwitchCaseCoverageExpression = 21, RULE_booleanSwitchCaseCombinedExpression = 22, 
		RULE_numericalScalarExpression = 23, RULE_complexNumberConstant = 24, 
		RULE_numericalOperator = 25, RULE_numericalUnaryOperation = 26, RULE_trigonometricOperator = 27, 
		RULE_getComponentExpression = 28, RULE_coverageIdentifierExpression = 29, 
		RULE_coverageCrsSetExpression = 30, RULE_sdomExtraction = 31, RULE_domainExpression = 32, 
		RULE_imageCrsDomainByDimensionExpression = 33, RULE_imageCrsDomainExpression = 34, 
		RULE_imageCrsExpression = 35, RULE_describeCoverageExpression = 36, RULE_domainIntervals = 37, 
		RULE_positionalParamater = 38, RULE_extraParams = 39, RULE_encodedCoverageExpression = 40, 
		RULE_decodeCoverageExpression = 41, RULE_coverageExpression = 42, RULE_coverageArithmeticOperator = 43, 
		RULE_unaryArithmeticExpressionOperator = 44, RULE_unaryArithmeticExpression = 45, 
		RULE_trigonometricExpression = 46, RULE_exponentialExpressionOperator = 47, 
		RULE_exponentialExpression = 48, RULE_unaryPowerExpression = 49, RULE_minBinaryExpression = 50, 
		RULE_maxBinaryExpression = 51, RULE_unaryBooleanExpression = 52, RULE_rangeType = 53, 
		RULE_castExpression = 54, RULE_fieldName = 55, RULE_rangeConstructorExpression = 56, 
		RULE_rangeConstructorSwitchCaseExpression = 57, RULE_dimensionPointList = 58, 
		RULE_dimensionPointElement = 59, RULE_dimensionIntervalList = 60, RULE_scaleDimensionIntervalList = 61, 
		RULE_scaleDimensionIntervalElement = 62, RULE_dimensionIntervalElement = 63, 
		RULE_wktPoints = 64, RULE_wktPointElementList = 65, RULE_wktLineString = 66, 
		RULE_wktPolygon = 67, RULE_wktMultipolygon = 68, RULE_wktExpression = 69, 
		RULE_curtainProjectionAxisLabel1 = 70, RULE_curtainProjectionAxisLabel2 = 71, 
		RULE_clipCurtainExpression = 72, RULE_corridorProjectionAxisLabel1 = 73, 
		RULE_corridorProjectionAxisLabel2 = 74, RULE_clipCorridorExpression = 75, 
		RULE_clipWKTExpression = 76, RULE_crsTransformExpression = 77, RULE_dimensionCrsList = 78, 
		RULE_dimensionCrsElement = 79, RULE_interpolationType = 80, RULE_coverageConstructorExpression = 81, 
		RULE_axisIterator = 82, RULE_intervalExpression = 83, RULE_coverageConstantExpression = 84, 
		RULE_axisSpec = 85, RULE_condenseExpression = 86, RULE_reduceBooleanExpressionOperator = 87, 
		RULE_reduceNumericalExpressionOperator = 88, RULE_reduceBooleanExpression = 89, 
		RULE_reduceNumericalExpression = 90, RULE_reduceExpression = 91, RULE_condenseExpressionOperator = 92, 
		RULE_generalCondenseExpression = 93, RULE_switchCaseExpression = 94, RULE_crsName = 95, 
		RULE_axisName = 96, RULE_number = 97, RULE_constant = 98;
	public static final String[] ruleNames = {
		"wcpsQuery", "forClauseList", "coverageIdForClause", "forClause", "letClauseList", 
		"letClauseDimensionIntervalList", "letClause", "whereClause", "returnClause", 
		"coverageVariableName", "processingExpression", "scalarValueCoverageExpression", 
		"scalarExpression", "booleanScalarExpression", "booleanUnaryOperator", 
		"booleanConstant", "booleanOperator", "numericalComparissonOperator", 
		"stringOperator", "stringScalarExpression", "starExpression", "booleanSwitchCaseCoverageExpression", 
		"booleanSwitchCaseCombinedExpression", "numericalScalarExpression", "complexNumberConstant", 
		"numericalOperator", "numericalUnaryOperation", "trigonometricOperator", 
		"getComponentExpression", "coverageIdentifierExpression", "coverageCrsSetExpression", 
		"sdomExtraction", "domainExpression", "imageCrsDomainByDimensionExpression", 
		"imageCrsDomainExpression", "imageCrsExpression", "describeCoverageExpression", 
		"domainIntervals", "positionalParamater", "extraParams", "encodedCoverageExpression", 
		"decodeCoverageExpression", "coverageExpression", "coverageArithmeticOperator", 
		"unaryArithmeticExpressionOperator", "unaryArithmeticExpression", "trigonometricExpression", 
		"exponentialExpressionOperator", "exponentialExpression", "unaryPowerExpression", 
		"minBinaryExpression", "maxBinaryExpression", "unaryBooleanExpression", 
		"rangeType", "castExpression", "fieldName", "rangeConstructorExpression", 
		"rangeConstructorSwitchCaseExpression", "dimensionPointList", "dimensionPointElement", 
		"dimensionIntervalList", "scaleDimensionIntervalList", "scaleDimensionIntervalElement", 
		"dimensionIntervalElement", "wktPoints", "wktPointElementList", "wktLineString", 
		"wktPolygon", "wktMultipolygon", "wktExpression", "curtainProjectionAxisLabel1", 
		"curtainProjectionAxisLabel2", "clipCurtainExpression", "corridorProjectionAxisLabel1", 
		"corridorProjectionAxisLabel2", "clipCorridorExpression", "clipWKTExpression", 
		"crsTransformExpression", "dimensionCrsList", "dimensionCrsElement", "interpolationType", 
		"coverageConstructorExpression", "axisIterator", "intervalExpression", 
		"coverageConstantExpression", "axisSpec", "condenseExpression", "reduceBooleanExpressionOperator", 
		"reduceNumericalExpressionOperator", "reduceBooleanExpression", "reduceNumericalExpression", 
		"reduceExpression", "condenseExpressionOperator", "generalCondenseExpression", 
		"switchCaseExpression", "crsName", "axisName", "number", "constant"
	};

	@Override
	public String getGrammarFileName() { return "wcps.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public wcpsParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class WcpsQueryContext extends ParserRuleContext {
		public WcpsQueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wcpsQuery; }
	 
		public WcpsQueryContext() { }
		public void copyFrom(WcpsQueryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class WcpsQueryLabelContext extends WcpsQueryContext {
		public LetClauseListContext letClauseList() {
			return getRuleContext(LetClauseListContext.class,0);
		}
		public ForClauseListContext forClauseList() {
			return getRuleContext(ForClauseListContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public ReturnClauseContext returnClause() {
			return getRuleContext(ReturnClauseContext.class,0);
		}
		public WcpsQueryLabelContext(WcpsQueryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitWcpsQueryLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WcpsQueryContext wcpsQuery() throws RecognitionException {
		WcpsQueryContext _localctx = new WcpsQueryContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_wcpsQuery);
		int _la;
		try {
			_localctx = new WcpsQueryLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(198); forClauseList();
			}
			setState(200);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(199); whereClause();
				}
			}

			setState(203);
			_la = _input.LA(1);
			if (_la==LET) {
				{
				setState(202); letClauseList();
				}
			}

			{
			setState(205); returnClause();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ForClauseListContext extends ParserRuleContext {
		public ForClauseListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forClauseList; }
	 
		public ForClauseListContext() { }
		public void copyFrom(ForClauseListContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ForClauseListLabelContext extends ForClauseListContext {
		public List<ForClauseContext> forClause() {
			return getRuleContexts(ForClauseContext.class);
		}
		public TerminalNode FOR() { return getToken(wcpsParser.FOR, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public ForClauseContext forClause(int i) {
			return getRuleContext(ForClauseContext.class,i);
		}
		public ForClauseListLabelContext(ForClauseListContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitForClauseListLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForClauseListContext forClauseList() throws RecognitionException {
		ForClauseListContext _localctx = new ForClauseListContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_forClauseList);
		int _la;
		try {
			_localctx = new ForClauseListLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(207); match(FOR);
			{
			setState(208); forClause();
			}
			setState(213);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(209); match(COMMA);
				setState(210); forClause();
				}
				}
				setState(215);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CoverageIdForClauseContext extends ParserRuleContext {
		public TerminalNode COVERAGE_VARIABLE_NAME() { return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, 0); }
		public DecodeCoverageExpressionContext decodeCoverageExpression() {
			return getRuleContext(DecodeCoverageExpressionContext.class,0);
		}
		public CoverageIdForClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coverageIdForClause; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageIdForClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoverageIdForClauseContext coverageIdForClause() throws RecognitionException {
		CoverageIdForClauseContext _localctx = new CoverageIdForClauseContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_coverageIdForClause);
		try {
			setState(218);
			switch (_input.LA(1)) {
			case COVERAGE_VARIABLE_NAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(216); match(COVERAGE_VARIABLE_NAME);
				}
				break;
			case DECODE:
				enterOuterAlt(_localctx, 2);
				{
				setState(217); decodeCoverageExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ForClauseContext extends ParserRuleContext {
		public ForClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forClause; }
	 
		public ForClauseContext() { }
		public void copyFrom(ForClauseContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ForClauseLabelContext extends ForClauseContext {
		public TerminalNode IN() { return getToken(wcpsParser.IN, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public CoverageIdForClauseContext coverageIdForClause(int i) {
			return getRuleContext(CoverageIdForClauseContext.class,i);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<CoverageIdForClauseContext> coverageIdForClause() {
			return getRuleContexts(CoverageIdForClauseContext.class);
		}
		public CoverageVariableNameContext coverageVariableName() {
			return getRuleContext(CoverageVariableNameContext.class,0);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public ForClauseLabelContext(ForClauseContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitForClauseLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForClauseContext forClause() throws RecognitionException {
		ForClauseContext _localctx = new ForClauseContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_forClause);
		int _la;
		try {
			int _alt;
			_localctx = new ForClauseLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(220); coverageVariableName();
			setState(221); match(IN);
			setState(223);
			_la = _input.LA(1);
			if (_la==LEFT_PARENTHESIS) {
				{
				setState(222); match(LEFT_PARENTHESIS);
				}
			}

			setState(225); coverageIdForClause();
			setState(230);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					{
					{
					setState(226); match(COMMA);
					setState(227); coverageIdForClause();
					}
					} 
				}
				setState(232);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			setState(234);
			_la = _input.LA(1);
			if (_la==RIGHT_PARENTHESIS) {
				{
				setState(233); match(RIGHT_PARENTHESIS);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LetClauseListContext extends ParserRuleContext {
		public LetClauseListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_letClauseList; }
	 
		public LetClauseListContext() { }
		public void copyFrom(LetClauseListContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class LetClauseListLabelContext extends LetClauseListContext {
		public TerminalNode LET() { return getToken(wcpsParser.LET, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<LetClauseContext> letClause() {
			return getRuleContexts(LetClauseContext.class);
		}
		public LetClauseContext letClause(int i) {
			return getRuleContext(LetClauseContext.class,i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public LetClauseListLabelContext(LetClauseListContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitLetClauseListLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LetClauseListContext letClauseList() throws RecognitionException {
		LetClauseListContext _localctx = new LetClauseListContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_letClauseList);
		int _la;
		try {
			_localctx = new LetClauseListLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(236); match(LET);
			{
			setState(237); letClause();
			}
			setState(242);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(238); match(COMMA);
				setState(239); letClause();
				}
				}
				setState(244);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LetClauseDimensionIntervalListContext extends ParserRuleContext {
		public DimensionIntervalListContext dimensionIntervalList() {
			return getRuleContext(DimensionIntervalListContext.class,0);
		}
		public TerminalNode EQUAL() { return getToken(wcpsParser.EQUAL, 0); }
		public TerminalNode LEFT_BRACKET() { return getToken(wcpsParser.LEFT_BRACKET, 0); }
		public TerminalNode COLON() { return getToken(wcpsParser.COLON, 0); }
		public CoverageVariableNameContext coverageVariableName() {
			return getRuleContext(CoverageVariableNameContext.class,0);
		}
		public TerminalNode RIGHT_BRACKET() { return getToken(wcpsParser.RIGHT_BRACKET, 0); }
		public LetClauseDimensionIntervalListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_letClauseDimensionIntervalList; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitLetClauseDimensionIntervalList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LetClauseDimensionIntervalListContext letClauseDimensionIntervalList() throws RecognitionException {
		LetClauseDimensionIntervalListContext _localctx = new LetClauseDimensionIntervalListContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_letClauseDimensionIntervalList);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245); coverageVariableName();
			setState(246); match(COLON);
			setState(247); match(EQUAL);
			setState(248); match(LEFT_BRACKET);
			setState(249); dimensionIntervalList();
			setState(250); match(RIGHT_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LetClauseContext extends ParserRuleContext {
		public LetClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_letClause; }
	 
		public LetClauseContext() { }
		public void copyFrom(LetClauseContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class LetClauseDimensionIntervalListLabelContext extends LetClauseContext {
		public LetClauseDimensionIntervalListContext letClauseDimensionIntervalList() {
			return getRuleContext(LetClauseDimensionIntervalListContext.class,0);
		}
		public LetClauseDimensionIntervalListLabelContext(LetClauseContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitLetClauseDimensionIntervalListLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LetClauseCoverageExpressionLabelContext extends LetClauseContext {
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode EQUAL() { return getToken(wcpsParser.EQUAL, 0); }
		public TerminalNode COLON() { return getToken(wcpsParser.COLON, 0); }
		public CoverageVariableNameContext coverageVariableName() {
			return getRuleContext(CoverageVariableNameContext.class,0);
		}
		public LetClauseCoverageExpressionLabelContext(LetClauseContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitLetClauseCoverageExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LetClauseContext letClause() throws RecognitionException {
		LetClauseContext _localctx = new LetClauseContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_letClause);
		try {
			setState(258);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				_localctx = new LetClauseDimensionIntervalListLabelContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(252); letClauseDimensionIntervalList();
				}
				break;

			case 2:
				_localctx = new LetClauseCoverageExpressionLabelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(253); coverageVariableName();
				setState(254); match(COLON);
				setState(255); match(EQUAL);
				setState(256); coverageExpression(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WhereClauseContext extends ParserRuleContext {
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
	 
		public WhereClauseContext() { }
		public void copyFrom(WhereClauseContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class WhereClauseLabelContext extends WhereClauseContext {
		public TerminalNode WHERE() { return getToken(wcpsParser.WHERE, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public WhereClauseLabelContext(WhereClauseContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitWhereClauseLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_whereClause);
		int _la;
		try {
			_localctx = new WhereClauseLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(260); match(WHERE);
			setState(262);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(261); match(LEFT_PARENTHESIS);
				}
				break;
			}
			setState(264); coverageExpression(0);
			setState(266);
			_la = _input.LA(1);
			if (_la==RIGHT_PARENTHESIS) {
				{
				setState(265); match(RIGHT_PARENTHESIS);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReturnClauseContext extends ParserRuleContext {
		public ReturnClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returnClause; }
	 
		public ReturnClauseContext() { }
		public void copyFrom(ReturnClauseContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ReturnClauseLabelContext extends ReturnClauseContext {
		public ProcessingExpressionContext processingExpression() {
			return getRuleContext(ProcessingExpressionContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode RETURN() { return getToken(wcpsParser.RETURN, 0); }
		public ReturnClauseLabelContext(ReturnClauseContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitReturnClauseLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReturnClauseContext returnClause() throws RecognitionException {
		ReturnClauseContext _localctx = new ReturnClauseContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_returnClause);
		int _la;
		try {
			_localctx = new ReturnClauseLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(268); match(RETURN);
			setState(270);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(269); match(LEFT_PARENTHESIS);
				}
				break;
			}
			setState(272); processingExpression();
			setState(274);
			_la = _input.LA(1);
			if (_la==RIGHT_PARENTHESIS) {
				{
				setState(273); match(RIGHT_PARENTHESIS);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CoverageVariableNameContext extends ParserRuleContext {
		public CoverageVariableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coverageVariableName; }
	 
		public CoverageVariableNameContext() { }
		public void copyFrom(CoverageVariableNameContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CoverageVariableNameLabelContext extends CoverageVariableNameContext {
		public TerminalNode COVERAGE_VARIABLE_NAME() { return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, 0); }
		public CoverageVariableNameLabelContext(CoverageVariableNameContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageVariableNameLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoverageVariableNameContext coverageVariableName() throws RecognitionException {
		CoverageVariableNameContext _localctx = new CoverageVariableNameContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_coverageVariableName);
		try {
			_localctx = new CoverageVariableNameLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(276); match(COVERAGE_VARIABLE_NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ProcessingExpressionContext extends ParserRuleContext {
		public GetComponentExpressionContext getComponentExpression() {
			return getRuleContext(GetComponentExpressionContext.class,0);
		}
		public EncodedCoverageExpressionContext encodedCoverageExpression() {
			return getRuleContext(EncodedCoverageExpressionContext.class,0);
		}
		public DescribeCoverageExpressionContext describeCoverageExpression() {
			return getRuleContext(DescribeCoverageExpressionContext.class,0);
		}
		public ScalarValueCoverageExpressionContext scalarValueCoverageExpression() {
			return getRuleContext(ScalarValueCoverageExpressionContext.class,0);
		}
		public ScalarExpressionContext scalarExpression() {
			return getRuleContext(ScalarExpressionContext.class,0);
		}
		public ProcessingExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_processingExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitProcessingExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProcessingExpressionContext processingExpression() throws RecognitionException {
		ProcessingExpressionContext _localctx = new ProcessingExpressionContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_processingExpression);
		try {
			setState(283);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(278); getComponentExpression();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(279); scalarExpression();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(280); encodedCoverageExpression();
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(281); scalarValueCoverageExpression();
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(282); describeCoverageExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScalarValueCoverageExpressionContext extends ParserRuleContext {
		public ScalarValueCoverageExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scalarValueCoverageExpression; }
	 
		public ScalarValueCoverageExpressionContext() { }
		public void copyFrom(ScalarValueCoverageExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ScalarValueCoverageExpressionLabelContext extends ScalarValueCoverageExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public ScalarValueCoverageExpressionLabelContext(ScalarValueCoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitScalarValueCoverageExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScalarValueCoverageExpressionContext scalarValueCoverageExpression() throws RecognitionException {
		ScalarValueCoverageExpressionContext _localctx = new ScalarValueCoverageExpressionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_scalarValueCoverageExpression);
		try {
			_localctx = new ScalarValueCoverageExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(285); match(LEFT_PARENTHESIS);
				}
				break;
			}
			setState(288); coverageExpression(0);
			setState(290);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				setState(289); match(RIGHT_PARENTHESIS);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScalarExpressionContext extends ParserRuleContext {
		public NumericalScalarExpressionContext numericalScalarExpression() {
			return getRuleContext(NumericalScalarExpressionContext.class,0);
		}
		public StarExpressionContext starExpression() {
			return getRuleContext(StarExpressionContext.class,0);
		}
		public StringScalarExpressionContext stringScalarExpression() {
			return getRuleContext(StringScalarExpressionContext.class,0);
		}
		public BooleanScalarExpressionContext booleanScalarExpression() {
			return getRuleContext(BooleanScalarExpressionContext.class,0);
		}
		public ScalarExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scalarExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitScalarExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScalarExpressionContext scalarExpression() throws RecognitionException {
		ScalarExpressionContext _localctx = new ScalarExpressionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_scalarExpression);
		try {
			setState(296);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(292); booleanScalarExpression(0);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(293); numericalScalarExpression(0);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(294); stringScalarExpression();
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(295); starExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanScalarExpressionContext extends ParserRuleContext {
		public int _p;
		public BooleanScalarExpressionContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public BooleanScalarExpressionContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_booleanScalarExpression; }
	 
		public BooleanScalarExpressionContext() { }
		public void copyFrom(BooleanScalarExpressionContext ctx) {
			super.copyFrom(ctx);
			this._p = ctx._p;
		}
	}
	public static class BooleanReduceExpressionContext extends BooleanScalarExpressionContext {
		public ReduceBooleanExpressionContext reduceBooleanExpression() {
			return getRuleContext(ReduceBooleanExpressionContext.class,0);
		}
		public BooleanReduceExpressionContext(BooleanScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBooleanReduceExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BooleanNumericalComparisonScalarLabelContext extends BooleanScalarExpressionContext {
		public List<NumericalScalarExpressionContext> numericalScalarExpression() {
			return getRuleContexts(NumericalScalarExpressionContext.class);
		}
		public NumericalScalarExpressionContext numericalScalarExpression(int i) {
			return getRuleContext(NumericalScalarExpressionContext.class,i);
		}
		public NumericalComparissonOperatorContext numericalComparissonOperator() {
			return getRuleContext(NumericalComparissonOperatorContext.class,0);
		}
		public BooleanNumericalComparisonScalarLabelContext(BooleanScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBooleanNumericalComparisonScalarLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BooleanStringComparisonScalarContext extends BooleanScalarExpressionContext {
		public StringScalarExpressionContext stringScalarExpression(int i) {
			return getRuleContext(StringScalarExpressionContext.class,i);
		}
		public StringOperatorContext stringOperator() {
			return getRuleContext(StringOperatorContext.class,0);
		}
		public List<StringScalarExpressionContext> stringScalarExpression() {
			return getRuleContexts(StringScalarExpressionContext.class);
		}
		public BooleanStringComparisonScalarContext(BooleanScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBooleanStringComparisonScalar(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BooleanUnaryScalarLabelContext extends BooleanScalarExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public BooleanUnaryOperatorContext booleanUnaryOperator() {
			return getRuleContext(BooleanUnaryOperatorContext.class,0);
		}
		public BooleanScalarExpressionContext booleanScalarExpression() {
			return getRuleContext(BooleanScalarExpressionContext.class,0);
		}
		public BooleanUnaryScalarLabelContext(BooleanScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBooleanUnaryScalarLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BooleanConstantLabelContext extends BooleanScalarExpressionContext {
		public BooleanConstantContext booleanConstant() {
			return getRuleContext(BooleanConstantContext.class,0);
		}
		public BooleanConstantLabelContext(BooleanScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBooleanConstantLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class BooleanBinaryScalarLabelContext extends BooleanScalarExpressionContext {
		public BooleanOperatorContext booleanOperator() {
			return getRuleContext(BooleanOperatorContext.class,0);
		}
		public BooleanScalarExpressionContext booleanScalarExpression(int i) {
			return getRuleContext(BooleanScalarExpressionContext.class,i);
		}
		public List<BooleanScalarExpressionContext> booleanScalarExpression() {
			return getRuleContexts(BooleanScalarExpressionContext.class);
		}
		public BooleanBinaryScalarLabelContext(BooleanScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBooleanBinaryScalarLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanScalarExpressionContext booleanScalarExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BooleanScalarExpressionContext _localctx = new BooleanScalarExpressionContext(_ctx, _parentState, _p);
		BooleanScalarExpressionContext _prevctx = _localctx;
		int _startState = 26;
		enterRecursionRule(_localctx, RULE_booleanScalarExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(318);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				_localctx = new BooleanReduceExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(299); reduceBooleanExpression();
				}
				break;

			case 2:
				{
				_localctx = new BooleanConstantLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(300); booleanConstant();
				}
				break;

			case 3:
				{
				_localctx = new BooleanUnaryScalarLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(301); booleanUnaryOperator();
				setState(303);
				switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
				case 1:
					{
					setState(302); match(LEFT_PARENTHESIS);
					}
					break;
				}
				setState(305); booleanScalarExpression(0);
				setState(307);
				switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
				case 1:
					{
					setState(306); match(RIGHT_PARENTHESIS);
					}
					break;
				}
				}
				break;

			case 4:
				{
				_localctx = new BooleanNumericalComparisonScalarLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(309); numericalScalarExpression(0);
				setState(310); numericalComparissonOperator();
				setState(311); numericalScalarExpression(0);
				}
				break;

			case 5:
				{
				_localctx = new BooleanReduceExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(313); reduceBooleanExpression();
				}
				break;

			case 6:
				{
				_localctx = new BooleanStringComparisonScalarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(314); stringScalarExpression();
				setState(315); stringOperator();
				setState(316); stringScalarExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(326);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new BooleanBinaryScalarLabelContext(new BooleanScalarExpressionContext(_parentctx, _parentState, _p));
					pushNewRecursionContext(_localctx, _startState, RULE_booleanScalarExpression);
					setState(320);
					if (!(4 >= _localctx._p)) throw new FailedPredicateException(this, "4 >= $_p");
					setState(321); booleanOperator();
					setState(322); booleanScalarExpression(0);
					}
					} 
				}
				setState(328);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class BooleanUnaryOperatorContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(wcpsParser.NOT, 0); }
		public BooleanUnaryOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanUnaryOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBooleanUnaryOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanUnaryOperatorContext booleanUnaryOperator() throws RecognitionException {
		BooleanUnaryOperatorContext _localctx = new BooleanUnaryOperatorContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_booleanUnaryOperator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(329); match(NOT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanConstantContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(wcpsParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(wcpsParser.FALSE, 0); }
		public BooleanConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanConstant; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBooleanConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanConstantContext booleanConstant() throws RecognitionException {
		BooleanConstantContext _localctx = new BooleanConstantContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_booleanConstant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(331);
			_la = _input.LA(1);
			if ( !(_la==FALSE || _la==TRUE) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanOperatorContext extends ParserRuleContext {
		public TerminalNode XOR() { return getToken(wcpsParser.XOR, 0); }
		public TerminalNode AND() { return getToken(wcpsParser.AND, 0); }
		public TerminalNode OR() { return getToken(wcpsParser.OR, 0); }
		public BooleanOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBooleanOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanOperatorContext booleanOperator() throws RecognitionException {
		BooleanOperatorContext _localctx = new BooleanOperatorContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_booleanOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(333);
			_la = _input.LA(1);
			if ( !(_la==AND || _la==OR || _la==XOR) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumericalComparissonOperatorContext extends ParserRuleContext {
		public TerminalNode GREATER_THAN() { return getToken(wcpsParser.GREATER_THAN, 0); }
		public TerminalNode LOWER_THAN() { return getToken(wcpsParser.LOWER_THAN, 0); }
		public TerminalNode GREATER_OR_EQUAL_THAN() { return getToken(wcpsParser.GREATER_OR_EQUAL_THAN, 0); }
		public TerminalNode EQUAL() { return getToken(wcpsParser.EQUAL, 0); }
		public TerminalNode LOWER_OR_EQUAL_THAN() { return getToken(wcpsParser.LOWER_OR_EQUAL_THAN, 0); }
		public TerminalNode NOT_EQUAL() { return getToken(wcpsParser.NOT_EQUAL, 0); }
		public NumericalComparissonOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericalComparissonOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumericalComparissonOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericalComparissonOperatorContext numericalComparissonOperator() throws RecognitionException {
		NumericalComparissonOperatorContext _localctx = new NumericalComparissonOperatorContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_numericalComparissonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(335);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQUAL) | (1L << GREATER_THAN) | (1L << GREATER_OR_EQUAL_THAN) | (1L << LOWER_THAN) | (1L << LOWER_OR_EQUAL_THAN) | (1L << NOT_EQUAL))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringOperatorContext extends ParserRuleContext {
		public TerminalNode EQUAL() { return getToken(wcpsParser.EQUAL, 0); }
		public TerminalNode NOT_EQUAL() { return getToken(wcpsParser.NOT_EQUAL, 0); }
		public StringOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitStringOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringOperatorContext stringOperator() throws RecognitionException {
		StringOperatorContext _localctx = new StringOperatorContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_stringOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(337);
			_la = _input.LA(1);
			if ( !(_la==EQUAL || _la==NOT_EQUAL) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringScalarExpressionContext extends ParserRuleContext {
		public StringScalarExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringScalarExpression; }
	 
		public StringScalarExpressionContext() { }
		public void copyFrom(StringScalarExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class StringScalarExpressionLabelContext extends StringScalarExpressionContext {
		public TerminalNode STRING_LITERAL() { return getToken(wcpsParser.STRING_LITERAL, 0); }
		public StringScalarExpressionLabelContext(StringScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitStringScalarExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringScalarExpressionContext stringScalarExpression() throws RecognitionException {
		StringScalarExpressionContext _localctx = new StringScalarExpressionContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_stringScalarExpression);
		try {
			_localctx = new StringScalarExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(339); match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StarExpressionContext extends ParserRuleContext {
		public StarExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_starExpression; }
	 
		public StarExpressionContext() { }
		public void copyFrom(StarExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class StarExpressionLabelContext extends StarExpressionContext {
		public TerminalNode MULTIPLICATION() { return getToken(wcpsParser.MULTIPLICATION, 0); }
		public StarExpressionLabelContext(StarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitStarExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StarExpressionContext starExpression() throws RecognitionException {
		StarExpressionContext _localctx = new StarExpressionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_starExpression);
		try {
			_localctx = new StarExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(341); match(MULTIPLICATION);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanSwitchCaseCoverageExpressionContext extends ParserRuleContext {
		public TerminalNode LEFT_PARENTHESIS(int i) {
			return getToken(wcpsParser.LEFT_PARENTHESIS, i);
		}
		public List<TerminalNode> LEFT_PARENTHESIS() { return getTokens(wcpsParser.LEFT_PARENTHESIS); }
		public TerminalNode RIGHT_PARENTHESIS(int i) {
			return getToken(wcpsParser.RIGHT_PARENTHESIS, i);
		}
		public List<TerminalNode> RIGHT_PARENTHESIS() { return getTokens(wcpsParser.RIGHT_PARENTHESIS); }
		public NumericalComparissonOperatorContext numericalComparissonOperator() {
			return getRuleContext(NumericalComparissonOperatorContext.class,0);
		}
		public List<CoverageExpressionContext> coverageExpression() {
			return getRuleContexts(CoverageExpressionContext.class);
		}
		public CoverageExpressionContext coverageExpression(int i) {
			return getRuleContext(CoverageExpressionContext.class,i);
		}
		public BooleanSwitchCaseCoverageExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanSwitchCaseCoverageExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBooleanSwitchCaseCoverageExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanSwitchCaseCoverageExpressionContext booleanSwitchCaseCoverageExpression() throws RecognitionException {
		BooleanSwitchCaseCoverageExpressionContext _localctx = new BooleanSwitchCaseCoverageExpressionContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_booleanSwitchCaseCoverageExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(346);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					{
					{
					setState(343); match(LEFT_PARENTHESIS);
					}
					} 
				}
				setState(348);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			}
			setState(349); coverageExpression(0);
			setState(353);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==RIGHT_PARENTHESIS) {
				{
				{
				setState(350); match(RIGHT_PARENTHESIS);
				}
				}
				setState(355);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(356); numericalComparissonOperator();
			setState(360);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					{
					{
					setState(357); match(LEFT_PARENTHESIS);
					}
					} 
				}
				setState(362);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			}
			setState(363); coverageExpression(0);
			setState(367);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					{
					{
					setState(364); match(RIGHT_PARENTHESIS);
					}
					} 
				}
				setState(369);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BooleanSwitchCaseCombinedExpressionContext extends ParserRuleContext {
		public int _p;
		public BooleanOperatorContext booleanOperator() {
			return getRuleContext(BooleanOperatorContext.class,0);
		}
		public BooleanSwitchCaseCoverageExpressionContext booleanSwitchCaseCoverageExpression(int i) {
			return getRuleContext(BooleanSwitchCaseCoverageExpressionContext.class,i);
		}
		public List<BooleanSwitchCaseCoverageExpressionContext> booleanSwitchCaseCoverageExpression() {
			return getRuleContexts(BooleanSwitchCaseCoverageExpressionContext.class);
		}
		public List<BooleanSwitchCaseCombinedExpressionContext> booleanSwitchCaseCombinedExpression() {
			return getRuleContexts(BooleanSwitchCaseCombinedExpressionContext.class);
		}
		public BooleanSwitchCaseCombinedExpressionContext booleanSwitchCaseCombinedExpression(int i) {
			return getRuleContext(BooleanSwitchCaseCombinedExpressionContext.class,i);
		}
		public BooleanSwitchCaseCombinedExpressionContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public BooleanSwitchCaseCombinedExpressionContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_booleanSwitchCaseCombinedExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBooleanSwitchCaseCombinedExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BooleanSwitchCaseCombinedExpressionContext booleanSwitchCaseCombinedExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BooleanSwitchCaseCombinedExpressionContext _localctx = new BooleanSwitchCaseCombinedExpressionContext(_ctx, _parentState, _p);
		BooleanSwitchCaseCombinedExpressionContext _prevctx = _localctx;
		int _startState = 44;
		enterRecursionRule(_localctx, RULE_booleanSwitchCaseCombinedExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(376);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				{
				setState(371); booleanSwitchCaseCoverageExpression();
				setState(372); booleanOperator();
				setState(373); booleanSwitchCaseCoverageExpression();
				}
				break;

			case 2:
				{
				setState(375); booleanSwitchCaseCoverageExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(384);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new BooleanSwitchCaseCombinedExpressionContext(_parentctx, _parentState, _p);
					pushNewRecursionContext(_localctx, _startState, RULE_booleanSwitchCaseCombinedExpression);
					setState(378);
					if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
					setState(379); booleanOperator();
					setState(380); booleanSwitchCaseCombinedExpression(0);
					}
					} 
				}
				setState(386);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class NumericalScalarExpressionContext extends ParserRuleContext {
		public int _p;
		public NumericalScalarExpressionContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public NumericalScalarExpressionContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_numericalScalarExpression; }
	 
		public NumericalScalarExpressionContext() { }
		public void copyFrom(NumericalScalarExpressionContext ctx) {
			super.copyFrom(ctx);
			this._p = ctx._p;
		}
	}
	public static class NumericalBinaryScalarExpressionLabelContext extends NumericalScalarExpressionContext {
		public List<NumericalScalarExpressionContext> numericalScalarExpression() {
			return getRuleContexts(NumericalScalarExpressionContext.class);
		}
		public NumericalScalarExpressionContext numericalScalarExpression(int i) {
			return getRuleContext(NumericalScalarExpressionContext.class,i);
		}
		public NumericalOperatorContext numericalOperator() {
			return getRuleContext(NumericalOperatorContext.class,0);
		}
		public NumericalBinaryScalarExpressionLabelContext(NumericalScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumericalBinaryScalarExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NumericalUnaryScalarExpressionLabelContext extends NumericalScalarExpressionContext {
		public NumericalScalarExpressionContext numericalScalarExpression() {
			return getRuleContext(NumericalScalarExpressionContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public NumericalUnaryOperationContext numericalUnaryOperation() {
			return getRuleContext(NumericalUnaryOperationContext.class,0);
		}
		public NumericalUnaryScalarExpressionLabelContext(NumericalScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumericalUnaryScalarExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NumericalCondenseExpressionLabelContext extends NumericalScalarExpressionContext {
		public CondenseExpressionContext condenseExpression() {
			return getRuleContext(CondenseExpressionContext.class,0);
		}
		public NumericalCondenseExpressionLabelContext(NumericalScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumericalCondenseExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NumericalTrigonometricScalarExpressionLabelContext extends NumericalScalarExpressionContext {
		public NumericalScalarExpressionContext numericalScalarExpression() {
			return getRuleContext(NumericalScalarExpressionContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TrigonometricOperatorContext trigonometricOperator() {
			return getRuleContext(TrigonometricOperatorContext.class,0);
		}
		public NumericalTrigonometricScalarExpressionLabelContext(NumericalScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumericalTrigonometricScalarExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NumericalRealNumberExpressionLabelContext extends NumericalScalarExpressionContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public NumericalRealNumberExpressionLabelContext(NumericalScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumericalRealNumberExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NumericalNanNumberExpressionLabelContext extends NumericalScalarExpressionContext {
		public TerminalNode NAN_NUMBER_CONSTANT() { return getToken(wcpsParser.NAN_NUMBER_CONSTANT, 0); }
		public NumericalNanNumberExpressionLabelContext(NumericalScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumericalNanNumberExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NumericalComplexNumberConstantContext extends NumericalScalarExpressionContext {
		public ComplexNumberConstantContext complexNumberConstant() {
			return getRuleContext(ComplexNumberConstantContext.class,0);
		}
		public NumericalComplexNumberConstantContext(NumericalScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumericalComplexNumberConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericalScalarExpressionContext numericalScalarExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		NumericalScalarExpressionContext _localctx = new NumericalScalarExpressionContext(_ctx, _parentState, _p);
		NumericalScalarExpressionContext _prevctx = _localctx;
		int _startState = 46;
		enterRecursionRule(_localctx, RULE_numericalScalarExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(402);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				{
				_localctx = new NumericalUnaryScalarExpressionLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(388); numericalUnaryOperation();
				setState(389); match(LEFT_PARENTHESIS);
				setState(390); numericalScalarExpression(0);
				setState(391); match(RIGHT_PARENTHESIS);
				}
				break;

			case 2:
				{
				_localctx = new NumericalTrigonometricScalarExpressionLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(393); trigonometricOperator();
				setState(394); match(LEFT_PARENTHESIS);
				setState(395); numericalScalarExpression(0);
				setState(396); match(RIGHT_PARENTHESIS);
				}
				break;

			case 3:
				{
				_localctx = new NumericalCondenseExpressionLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(398); condenseExpression();
				}
				break;

			case 4:
				{
				_localctx = new NumericalRealNumberExpressionLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(399); number();
				}
				break;

			case 5:
				{
				_localctx = new NumericalNanNumberExpressionLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(400); match(NAN_NUMBER_CONSTANT);
				}
				break;

			case 6:
				{
				_localctx = new NumericalComplexNumberConstantContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(401); complexNumberConstant();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(410);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new NumericalBinaryScalarExpressionLabelContext(new NumericalScalarExpressionContext(_parentctx, _parentState, _p));
					pushNewRecursionContext(_localctx, _startState, RULE_numericalScalarExpression);
					setState(404);
					if (!(5 >= _localctx._p)) throw new FailedPredicateException(this, "5 >= $_p");
					setState(405); numericalOperator();
					setState(406); numericalScalarExpression(0);
					}
					} 
				}
				setState(412);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ComplexNumberConstantContext extends ParserRuleContext {
		public ComplexNumberConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_complexNumberConstant; }
	 
		public ComplexNumberConstantContext() { }
		public void copyFrom(ComplexNumberConstantContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ComplexNumberConstantLabelContext extends ComplexNumberConstantContext {
		public List<TerminalNode> REAL_NUMBER_CONSTANT() { return getTokens(wcpsParser.REAL_NUMBER_CONSTANT); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode REAL_NUMBER_CONSTANT(int i) {
			return getToken(wcpsParser.REAL_NUMBER_CONSTANT, i);
		}
		public ComplexNumberConstantLabelContext(ComplexNumberConstantContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitComplexNumberConstantLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComplexNumberConstantContext complexNumberConstant() throws RecognitionException {
		ComplexNumberConstantContext _localctx = new ComplexNumberConstantContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_complexNumberConstant);
		try {
			_localctx = new ComplexNumberConstantLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(413); match(LEFT_PARENTHESIS);
			setState(414); match(REAL_NUMBER_CONSTANT);
			setState(415); match(COMMA);
			setState(416); match(REAL_NUMBER_CONSTANT);
			setState(417); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumericalOperatorContext extends ParserRuleContext {
		public TerminalNode MULTIPLICATION() { return getToken(wcpsParser.MULTIPLICATION, 0); }
		public TerminalNode DIVISION() { return getToken(wcpsParser.DIVISION, 0); }
		public TerminalNode MINUS() { return getToken(wcpsParser.MINUS, 0); }
		public TerminalNode PLUS() { return getToken(wcpsParser.PLUS, 0); }
		public NumericalOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericalOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumericalOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericalOperatorContext numericalOperator() throws RecognitionException {
		NumericalOperatorContext _localctx = new NumericalOperatorContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_numericalOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(419);
			_la = _input.LA(1);
			if ( !(((((_la - 28)) & ~0x3f) == 0 && ((1L << (_la - 28)) & ((1L << (DIVISION - 28)) | (1L << (MINUS - 28)) | (1L << (MULTIPLICATION - 28)) | (1L << (PLUS - 28)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumericalUnaryOperationContext extends ParserRuleContext {
		public TerminalNode ABSOLUTE_VALUE() { return getToken(wcpsParser.ABSOLUTE_VALUE, 0); }
		public TerminalNode SQUARE_ROOT() { return getToken(wcpsParser.SQUARE_ROOT, 0); }
		public TerminalNode IMAGINARY_PART() { return getToken(wcpsParser.IMAGINARY_PART, 0); }
		public TerminalNode MINUS() { return getToken(wcpsParser.MINUS, 0); }
		public TerminalNode REAL_PART() { return getToken(wcpsParser.REAL_PART, 0); }
		public TerminalNode PLUS() { return getToken(wcpsParser.PLUS, 0); }
		public TerminalNode ROUND() { return getToken(wcpsParser.ROUND, 0); }
		public NumericalUnaryOperationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericalUnaryOperation; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumericalUnaryOperation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumericalUnaryOperationContext numericalUnaryOperation() throws RecognitionException {
		NumericalUnaryOperationContext _localctx = new NumericalUnaryOperationContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_numericalUnaryOperation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(421);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ABSOLUTE_VALUE) | (1L << IMAGINARY_PART) | (1L << MINUS))) != 0) || ((((_la - 68)) & ~0x3f) == 0 && ((1L << (_la - 68)) & ((1L << (PLUS - 68)) | (1L << (REAL_PART - 68)) | (1L << (ROUND - 68)) | (1L << (SQUARE_ROOT - 68)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TrigonometricOperatorContext extends ParserRuleContext {
		public TerminalNode ARCSIN() { return getToken(wcpsParser.ARCSIN, 0); }
		public TerminalNode TANH() { return getToken(wcpsParser.TANH, 0); }
		public TerminalNode ARCTAN() { return getToken(wcpsParser.ARCTAN, 0); }
		public TerminalNode TAN() { return getToken(wcpsParser.TAN, 0); }
		public TerminalNode SIN() { return getToken(wcpsParser.SIN, 0); }
		public TerminalNode SINH() { return getToken(wcpsParser.SINH, 0); }
		public TerminalNode ARCCOS() { return getToken(wcpsParser.ARCCOS, 0); }
		public TerminalNode COSH() { return getToken(wcpsParser.COSH, 0); }
		public TerminalNode COS() { return getToken(wcpsParser.COS, 0); }
		public TrigonometricOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trigonometricOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitTrigonometricOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TrigonometricOperatorContext trigonometricOperator() throws RecognitionException {
		TrigonometricOperatorContext _localctx = new TrigonometricOperatorContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_trigonometricOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(423);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ARCSIN) | (1L << ARCCOS) | (1L << ARCTAN) | (1L << COS) | (1L << COSH))) != 0) || ((((_la - 82)) & ~0x3f) == 0 && ((1L << (_la - 82)) & ((1L << (SIN - 82)) | (1L << (SINH - 82)) | (1L << (TAN - 82)) | (1L << (TANH - 82)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GetComponentExpressionContext extends ParserRuleContext {
		public CoverageCrsSetExpressionContext coverageCrsSetExpression() {
			return getRuleContext(CoverageCrsSetExpressionContext.class,0);
		}
		public DomainExpressionContext domainExpression() {
			return getRuleContext(DomainExpressionContext.class,0);
		}
		public ImageCrsDomainExpressionContext imageCrsDomainExpression() {
			return getRuleContext(ImageCrsDomainExpressionContext.class,0);
		}
		public ImageCrsDomainByDimensionExpressionContext imageCrsDomainByDimensionExpression() {
			return getRuleContext(ImageCrsDomainByDimensionExpressionContext.class,0);
		}
		public ImageCrsExpressionContext imageCrsExpression() {
			return getRuleContext(ImageCrsExpressionContext.class,0);
		}
		public CoverageIdentifierExpressionContext coverageIdentifierExpression() {
			return getRuleContext(CoverageIdentifierExpressionContext.class,0);
		}
		public GetComponentExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_getComponentExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitGetComponentExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GetComponentExpressionContext getComponentExpression() throws RecognitionException {
		GetComponentExpressionContext _localctx = new GetComponentExpressionContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_getComponentExpression);
		try {
			setState(431);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(425); coverageIdentifierExpression();
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(426); coverageCrsSetExpression();
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(427); domainExpression();
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(428); imageCrsDomainExpression();
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(429); imageCrsDomainByDimensionExpression();
				}
				break;

			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(430); imageCrsExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CoverageIdentifierExpressionContext extends ParserRuleContext {
		public CoverageIdentifierExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coverageIdentifierExpression; }
	 
		public CoverageIdentifierExpressionContext() { }
		public void copyFrom(CoverageIdentifierExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CoverageIdentifierExpressionLabelContext extends CoverageIdentifierExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode IDENTIFIER() { return getToken(wcpsParser.IDENTIFIER, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public CoverageIdentifierExpressionLabelContext(CoverageIdentifierExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageIdentifierExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoverageIdentifierExpressionContext coverageIdentifierExpression() throws RecognitionException {
		CoverageIdentifierExpressionContext _localctx = new CoverageIdentifierExpressionContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_coverageIdentifierExpression);
		try {
			_localctx = new CoverageIdentifierExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(433); match(IDENTIFIER);
			setState(434); match(LEFT_PARENTHESIS);
			setState(435); coverageExpression(0);
			setState(436); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CoverageCrsSetExpressionContext extends ParserRuleContext {
		public CoverageCrsSetExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coverageCrsSetExpression; }
	 
		public CoverageCrsSetExpressionContext() { }
		public void copyFrom(CoverageCrsSetExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CoverageCrsSetExpressionLabelContext extends CoverageCrsSetExpressionContext {
		public TerminalNode CRSSET() { return getToken(wcpsParser.CRSSET, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public CoverageCrsSetExpressionLabelContext(CoverageCrsSetExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageCrsSetExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoverageCrsSetExpressionContext coverageCrsSetExpression() throws RecognitionException {
		CoverageCrsSetExpressionContext _localctx = new CoverageCrsSetExpressionContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_coverageCrsSetExpression);
		try {
			_localctx = new CoverageCrsSetExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(438); match(CRSSET);
			setState(439); match(LEFT_PARENTHESIS);
			setState(440); coverageExpression(0);
			setState(441); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SdomExtractionContext extends ParserRuleContext {
		public TerminalNode UPPER_BOUND() { return getToken(wcpsParser.UPPER_BOUND, 0); }
		public TerminalNode LOWER_BOUND() { return getToken(wcpsParser.LOWER_BOUND, 0); }
		public SdomExtractionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sdomExtraction; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitSdomExtraction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SdomExtractionContext sdomExtraction() throws RecognitionException {
		SdomExtractionContext _localctx = new SdomExtractionContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_sdomExtraction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(443);
			_la = _input.LA(1);
			if ( !(_la==LOWER_BOUND || _la==UPPER_BOUND) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DomainExpressionContext extends ParserRuleContext {
		public DomainExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_domainExpression; }
	 
		public DomainExpressionContext() { }
		public void copyFrom(DomainExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class DomainExpressionLabelContext extends DomainExpressionContext {
		public CrsNameContext crsName() {
			return getRuleContext(CrsNameContext.class,0);
		}
		public TerminalNode DOMAIN() { return getToken(wcpsParser.DOMAIN, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public DomainExpressionLabelContext(DomainExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitDomainExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DomainExpressionContext domainExpression() throws RecognitionException {
		DomainExpressionContext _localctx = new DomainExpressionContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_domainExpression);
		int _la;
		try {
			_localctx = new DomainExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(445); match(DOMAIN);
			setState(446); match(LEFT_PARENTHESIS);
			setState(447); coverageExpression(0);
			setState(448); match(COMMA);
			setState(449); axisName();
			setState(452);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(450); match(COMMA);
				setState(451); crsName();
				}
			}

			setState(454); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImageCrsDomainByDimensionExpressionContext extends ParserRuleContext {
		public ImageCrsDomainByDimensionExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_imageCrsDomainByDimensionExpression; }
	 
		public ImageCrsDomainByDimensionExpressionContext() { }
		public void copyFrom(ImageCrsDomainByDimensionExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ImageCrsDomainByDimensionExpressionLabelContext extends ImageCrsDomainByDimensionExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode IMAGECRSDOMAIN() { return getToken(wcpsParser.IMAGECRSDOMAIN, 0); }
		public ImageCrsDomainByDimensionExpressionLabelContext(ImageCrsDomainByDimensionExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitImageCrsDomainByDimensionExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImageCrsDomainByDimensionExpressionContext imageCrsDomainByDimensionExpression() throws RecognitionException {
		ImageCrsDomainByDimensionExpressionContext _localctx = new ImageCrsDomainByDimensionExpressionContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_imageCrsDomainByDimensionExpression);
		try {
			_localctx = new ImageCrsDomainByDimensionExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(456); match(IMAGECRSDOMAIN);
			setState(457); match(LEFT_PARENTHESIS);
			setState(458); coverageExpression(0);
			setState(459); match(COMMA);
			setState(460); axisName();
			setState(461); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImageCrsDomainExpressionContext extends ParserRuleContext {
		public ImageCrsDomainExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_imageCrsDomainExpression; }
	 
		public ImageCrsDomainExpressionContext() { }
		public void copyFrom(ImageCrsDomainExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ImageCrsDomainExpressionLabelContext extends ImageCrsDomainExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode IMAGECRSDOMAIN() { return getToken(wcpsParser.IMAGECRSDOMAIN, 0); }
		public ImageCrsDomainExpressionLabelContext(ImageCrsDomainExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitImageCrsDomainExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImageCrsDomainExpressionContext imageCrsDomainExpression() throws RecognitionException {
		ImageCrsDomainExpressionContext _localctx = new ImageCrsDomainExpressionContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_imageCrsDomainExpression);
		try {
			_localctx = new ImageCrsDomainExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(463); match(IMAGECRSDOMAIN);
			setState(464); match(LEFT_PARENTHESIS);
			setState(465); coverageExpression(0);
			setState(466); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImageCrsExpressionContext extends ParserRuleContext {
		public ImageCrsExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_imageCrsExpression; }
	 
		public ImageCrsExpressionContext() { }
		public void copyFrom(ImageCrsExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ImageCrsExpressionLabelContext extends ImageCrsExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode IMAGECRS() { return getToken(wcpsParser.IMAGECRS, 0); }
		public ImageCrsExpressionLabelContext(ImageCrsExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitImageCrsExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ImageCrsExpressionContext imageCrsExpression() throws RecognitionException {
		ImageCrsExpressionContext _localctx = new ImageCrsExpressionContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_imageCrsExpression);
		try {
			_localctx = new ImageCrsExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(468); match(IMAGECRS);
			setState(469); match(LEFT_PARENTHESIS);
			setState(470); coverageExpression(0);
			setState(471); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DescribeCoverageExpressionContext extends ParserRuleContext {
		public DescribeCoverageExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_describeCoverageExpression; }
	 
		public DescribeCoverageExpressionContext() { }
		public void copyFrom(DescribeCoverageExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class DescribeCoverageExpressionLabelContext extends DescribeCoverageExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(wcpsParser.STRING_LITERAL, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode DESCRIBE_COVERAGE() { return getToken(wcpsParser.DESCRIBE_COVERAGE, 0); }
		public ExtraParamsContext extraParams() {
			return getRuleContext(ExtraParamsContext.class,0);
		}
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public DescribeCoverageExpressionLabelContext(DescribeCoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitDescribeCoverageExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DescribeCoverageExpressionContext describeCoverageExpression() throws RecognitionException {
		DescribeCoverageExpressionContext _localctx = new DescribeCoverageExpressionContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_describeCoverageExpression);
		int _la;
		try {
			_localctx = new DescribeCoverageExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(473); match(DESCRIBE_COVERAGE);
			setState(474); match(LEFT_PARENTHESIS);
			setState(475); coverageExpression(0);
			setState(476); match(COMMA);
			setState(477); match(STRING_LITERAL);
			setState(480);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(478); match(COMMA);
				setState(479); extraParams();
				}
			}

			setState(482); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DomainIntervalsContext extends ParserRuleContext {
		public DomainExpressionContext domainExpression() {
			return getRuleContext(DomainExpressionContext.class,0);
		}
		public ImageCrsDomainExpressionContext imageCrsDomainExpression() {
			return getRuleContext(ImageCrsDomainExpressionContext.class,0);
		}
		public ImageCrsDomainByDimensionExpressionContext imageCrsDomainByDimensionExpression() {
			return getRuleContext(ImageCrsDomainByDimensionExpressionContext.class,0);
		}
		public SdomExtractionContext sdomExtraction() {
			return getRuleContext(SdomExtractionContext.class,0);
		}
		public DomainIntervalsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_domainIntervals; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitDomainIntervals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DomainIntervalsContext domainIntervals() throws RecognitionException {
		DomainIntervalsContext _localctx = new DomainIntervalsContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_domainIntervals);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(487);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				setState(484); domainExpression();
				}
				break;

			case 2:
				{
				setState(485); imageCrsDomainExpression();
				}
				break;

			case 3:
				{
				setState(486); imageCrsDomainByDimensionExpression();
				}
				break;
			}
			setState(490);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				{
				setState(489); sdomExtraction();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PositionalParamaterContext extends ParserRuleContext {
		public TerminalNode POSITIONAL_PARAMETER() { return getToken(wcpsParser.POSITIONAL_PARAMETER, 0); }
		public PositionalParamaterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_positionalParamater; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitPositionalParamater(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PositionalParamaterContext positionalParamater() throws RecognitionException {
		PositionalParamaterContext _localctx = new PositionalParamaterContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_positionalParamater);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(492); match(POSITIONAL_PARAMETER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExtraParamsContext extends ParserRuleContext {
		public TerminalNode EXTRA_PARAMS() { return getToken(wcpsParser.EXTRA_PARAMS, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(wcpsParser.STRING_LITERAL, 0); }
		public ExtraParamsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extraParams; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitExtraParams(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExtraParamsContext extraParams() throws RecognitionException {
		ExtraParamsContext _localctx = new ExtraParamsContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_extraParams);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(494);
			_la = _input.LA(1);
			if ( !(_la==STRING_LITERAL || _la==EXTRA_PARAMS) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EncodedCoverageExpressionContext extends ParserRuleContext {
		public EncodedCoverageExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_encodedCoverageExpression; }
	 
		public EncodedCoverageExpressionContext() { }
		public void copyFrom(EncodedCoverageExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class EncodedCoverageExpressionLabelContext extends EncodedCoverageExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(wcpsParser.STRING_LITERAL, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public ExtraParamsContext extraParams() {
			return getRuleContext(ExtraParamsContext.class,0);
		}
		public TerminalNode ENCODE() { return getToken(wcpsParser.ENCODE, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public EncodedCoverageExpressionLabelContext(EncodedCoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitEncodedCoverageExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EncodedCoverageExpressionContext encodedCoverageExpression() throws RecognitionException {
		EncodedCoverageExpressionContext _localctx = new EncodedCoverageExpressionContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_encodedCoverageExpression);
		int _la;
		try {
			_localctx = new EncodedCoverageExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(496); match(ENCODE);
			setState(497); match(LEFT_PARENTHESIS);
			setState(498); coverageExpression(0);
			setState(499); match(COMMA);
			setState(500); match(STRING_LITERAL);
			setState(503);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(501); match(COMMA);
				setState(502); extraParams();
				}
			}

			setState(505); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DecodeCoverageExpressionContext extends ParserRuleContext {
		public DecodeCoverageExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_decodeCoverageExpression; }
	 
		public DecodeCoverageExpressionContext() { }
		public void copyFrom(DecodeCoverageExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class DecodedCoverageExpressionLabelContext extends DecodeCoverageExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public PositionalParamaterContext positionalParamater() {
			return getRuleContext(PositionalParamaterContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public ExtraParamsContext extraParams() {
			return getRuleContext(ExtraParamsContext.class,0);
		}
		public TerminalNode DECODE() { return getToken(wcpsParser.DECODE, 0); }
		public DecodedCoverageExpressionLabelContext(DecodeCoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitDecodedCoverageExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DecodeCoverageExpressionContext decodeCoverageExpression() throws RecognitionException {
		DecodeCoverageExpressionContext _localctx = new DecodeCoverageExpressionContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_decodeCoverageExpression);
		int _la;
		try {
			_localctx = new DecodedCoverageExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(507); match(DECODE);
			setState(508); match(LEFT_PARENTHESIS);
			setState(509); positionalParamater();
			setState(512);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(510); match(COMMA);
				setState(511); extraParams();
				}
			}

			setState(514); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CoverageExpressionContext extends ParserRuleContext {
		public int _p;
		public CoverageExpressionContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public CoverageExpressionContext(ParserRuleContext parent, int invokingState, int _p) {
			super(parent, invokingState);
			this._p = _p;
		}
		@Override public int getRuleIndex() { return RULE_coverageExpression; }
	 
		public CoverageExpressionContext() { }
		public void copyFrom(CoverageExpressionContext ctx) {
			super.copyFrom(ctx);
			this._p = ctx._p;
		}
	}
	public static class CoverageExpressionScalarLabelContext extends CoverageExpressionContext {
		public ScalarExpressionContext scalarExpression() {
			return getRuleContext(ScalarExpressionContext.class,0);
		}
		public CoverageExpressionScalarLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionScalarLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionMinBinaryLabelContext extends CoverageExpressionContext {
		public MinBinaryExpressionContext minBinaryExpression() {
			return getRuleContext(MinBinaryExpressionContext.class,0);
		}
		public CoverageExpressionMinBinaryLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionMinBinaryLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionLogicLabelContext extends CoverageExpressionContext {
		public BooleanOperatorContext booleanOperator() {
			return getRuleContext(BooleanOperatorContext.class,0);
		}
		public List<CoverageExpressionContext> coverageExpression() {
			return getRuleContexts(CoverageExpressionContext.class);
		}
		public CoverageExpressionContext coverageExpression(int i) {
			return getRuleContext(CoverageExpressionContext.class,i);
		}
		public CoverageExpressionLogicLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionLogicLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionUnaryBooleanLabelContext extends CoverageExpressionContext {
		public UnaryBooleanExpressionContext unaryBooleanExpression() {
			return getRuleContext(UnaryBooleanExpressionContext.class,0);
		}
		public CoverageExpressionUnaryBooleanLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionUnaryBooleanLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionOverlayLabelContext extends CoverageExpressionContext {
		public TerminalNode OVERLAY() { return getToken(wcpsParser.OVERLAY, 0); }
		public List<CoverageExpressionContext> coverageExpression() {
			return getRuleContexts(CoverageExpressionContext.class);
		}
		public CoverageExpressionContext coverageExpression(int i) {
			return getRuleContext(CoverageExpressionContext.class,i);
		}
		public CoverageExpressionOverlayLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionOverlayLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageIsNullExpressionContext extends CoverageExpressionContext {
		public TerminalNode NOT() { return getToken(wcpsParser.NOT, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode IS() { return getToken(wcpsParser.IS, 0); }
		public TerminalNode NULL() { return getToken(wcpsParser.NULL, 0); }
		public CoverageIsNullExpressionContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageIsNullExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionMaxBinaryLabelContext extends CoverageExpressionContext {
		public MaxBinaryExpressionContext maxBinaryExpression() {
			return getRuleContext(MaxBinaryExpressionContext.class,0);
		}
		public CoverageExpressionMaxBinaryLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionMaxBinaryLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionTrigonometricLabelContext extends CoverageExpressionContext {
		public TrigonometricExpressionContext trigonometricExpression() {
			return getRuleContext(TrigonometricExpressionContext.class,0);
		}
		public CoverageExpressionTrigonometricLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionTrigonometricLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageXpressionShortHandSubsetWithLetClauseVariableLabelContext extends CoverageExpressionContext {
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode LEFT_BRACKET() { return getToken(wcpsParser.LEFT_BRACKET, 0); }
		public CoverageVariableNameContext coverageVariableName() {
			return getRuleContext(CoverageVariableNameContext.class,0);
		}
		public TerminalNode RIGHT_BRACKET() { return getToken(wcpsParser.RIGHT_BRACKET, 0); }
		public CoverageXpressionShortHandSubsetWithLetClauseVariableLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageXpressionShortHandSubsetWithLetClauseVariableLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionUnaryArithmeticLabelContext extends CoverageExpressionContext {
		public UnaryArithmeticExpressionContext unaryArithmeticExpression() {
			return getRuleContext(UnaryArithmeticExpressionContext.class,0);
		}
		public CoverageExpressionUnaryArithmeticLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionUnaryArithmeticLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionExtendLabelContext extends CoverageExpressionContext {
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public DimensionIntervalListContext dimensionIntervalList() {
			return getRuleContext(DimensionIntervalListContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode EXTEND() { return getToken(wcpsParser.EXTEND, 0); }
		public CoverageExpressionExtendLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionExtendLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionVariableNameLabelContext extends CoverageExpressionContext {
		public CoverageVariableNameContext coverageVariableName() {
			return getRuleContext(CoverageVariableNameContext.class,0);
		}
		public CoverageExpressionVariableNameLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionVariableNameLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionConstantLabelContext extends CoverageExpressionContext {
		public CoverageConstantExpressionContext coverageConstantExpression() {
			return getRuleContext(CoverageConstantExpressionContext.class,0);
		}
		public CoverageExpressionConstantLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionConstantLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionScaleByAxesLabelContext extends CoverageExpressionContext {
		public ScaleDimensionIntervalListContext scaleDimensionIntervalList() {
			return getRuleContext(ScaleDimensionIntervalListContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode SCALE_AXES() { return getToken(wcpsParser.SCALE_AXES, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode LEFT_BRACKET() { return getToken(wcpsParser.LEFT_BRACKET, 0); }
		public TerminalNode RIGHT_BRACKET() { return getToken(wcpsParser.RIGHT_BRACKET, 0); }
		public CoverageExpressionScaleByAxesLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionScaleByAxesLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionCoverageLabelContext extends CoverageExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public CoverageExpressionCoverageLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionCoverageLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionScaleBySizeLabelContext extends CoverageExpressionContext {
		public ScaleDimensionIntervalListContext scaleDimensionIntervalList() {
			return getRuleContext(ScaleDimensionIntervalListContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode SCALE_SIZE() { return getToken(wcpsParser.SCALE_SIZE, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode LEFT_BRACKET() { return getToken(wcpsParser.LEFT_BRACKET, 0); }
		public TerminalNode RIGHT_BRACKET() { return getToken(wcpsParser.RIGHT_BRACKET, 0); }
		public CoverageExpressionScaleBySizeLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionScaleBySizeLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionSwitchCaseLabelContext extends CoverageExpressionContext {
		public SwitchCaseExpressionContext switchCaseExpression() {
			return getRuleContext(SwitchCaseExpressionContext.class,0);
		}
		public CoverageExpressionSwitchCaseLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionSwitchCaseLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionComparissonLabelContext extends CoverageExpressionContext {
		public NumericalComparissonOperatorContext numericalComparissonOperator() {
			return getRuleContext(NumericalComparissonOperatorContext.class,0);
		}
		public List<CoverageExpressionContext> coverageExpression() {
			return getRuleContexts(CoverageExpressionContext.class);
		}
		public CoverageExpressionContext coverageExpression(int i) {
			return getRuleContext(CoverageExpressionContext.class,i);
		}
		public CoverageExpressionComparissonLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionComparissonLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionScaleByExtentLabelContext extends CoverageExpressionContext {
		public ScaleDimensionIntervalListContext scaleDimensionIntervalList() {
			return getRuleContext(ScaleDimensionIntervalListContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode SCALE_EXTENT() { return getToken(wcpsParser.SCALE_EXTENT, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode LEFT_BRACKET() { return getToken(wcpsParser.LEFT_BRACKET, 0); }
		public TerminalNode RIGHT_BRACKET() { return getToken(wcpsParser.RIGHT_BRACKET, 0); }
		public CoverageExpressionScaleByExtentLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionScaleByExtentLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionShorthandSliceLabelContext extends CoverageExpressionContext {
		public DimensionPointListContext dimensionPointList() {
			return getRuleContext(DimensionPointListContext.class,0);
		}
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode LEFT_BRACKET() { return getToken(wcpsParser.LEFT_BRACKET, 0); }
		public TerminalNode RIGHT_BRACKET() { return getToken(wcpsParser.RIGHT_BRACKET, 0); }
		public CoverageExpressionShorthandSliceLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionShorthandSliceLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionDomainIntervalsLabelContext extends CoverageExpressionContext {
		public DomainIntervalsContext domainIntervals() {
			return getRuleContext(DomainIntervalsContext.class,0);
		}
		public CoverageExpressionDomainIntervalsLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionDomainIntervalsLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionScaleByImageCrsDomainLabelContext extends CoverageExpressionContext {
		public TerminalNode SCALE() { return getToken(wcpsParser.SCALE, 0); }
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public DomainIntervalsContext domainIntervals() {
			return getRuleContext(DomainIntervalsContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public CoverageExpressionScaleByImageCrsDomainLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionScaleByImageCrsDomainLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionPowerLabelContext extends CoverageExpressionContext {
		public UnaryPowerExpressionContext unaryPowerExpression() {
			return getRuleContext(UnaryPowerExpressionContext.class,0);
		}
		public CoverageExpressionPowerLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionPowerLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionExtendByDomainIntervalsLabelContext extends CoverageExpressionContext {
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public DomainIntervalsContext domainIntervals() {
			return getRuleContext(DomainIntervalsContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode EXTEND() { return getToken(wcpsParser.EXTEND, 0); }
		public CoverageExpressionExtendByDomainIntervalsLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionExtendByDomainIntervalsLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionConstructorLabelContext extends CoverageExpressionContext {
		public CoverageConstructorExpressionContext coverageConstructorExpression() {
			return getRuleContext(CoverageConstructorExpressionContext.class,0);
		}
		public CoverageExpressionConstructorLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionConstructorLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionShorthandSubsetLabelContext extends CoverageExpressionContext {
		public DimensionIntervalListContext dimensionIntervalList() {
			return getRuleContext(DimensionIntervalListContext.class,0);
		}
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode LEFT_BRACKET() { return getToken(wcpsParser.LEFT_BRACKET, 0); }
		public TerminalNode RIGHT_BRACKET() { return getToken(wcpsParser.RIGHT_BRACKET, 0); }
		public CoverageExpressionShorthandSubsetLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionShorthandSubsetLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionClipWKTLabelContext extends CoverageExpressionContext {
		public ClipWKTExpressionContext clipWKTExpression() {
			return getRuleContext(ClipWKTExpressionContext.class,0);
		}
		public CoverageExpressionClipWKTLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionClipWKTLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionRangeSubsettingLabelContext extends CoverageExpressionContext {
		public TerminalNode DOT() { return getToken(wcpsParser.DOT, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public FieldNameContext fieldName() {
			return getRuleContext(FieldNameContext.class,0);
		}
		public CoverageExpressionRangeSubsettingLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionRangeSubsettingLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionClipCurtainLabelContext extends CoverageExpressionContext {
		public ClipCurtainExpressionContext clipCurtainExpression() {
			return getRuleContext(ClipCurtainExpressionContext.class,0);
		}
		public CoverageExpressionClipCurtainLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionClipCurtainLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionScaleByDimensionIntervalsLabelContext extends CoverageExpressionContext {
		public TerminalNode SCALE() { return getToken(wcpsParser.SCALE, 0); }
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public DimensionIntervalListContext dimensionIntervalList() {
			return getRuleContext(DimensionIntervalListContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public CoverageExpressionScaleByDimensionIntervalsLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionScaleByDimensionIntervalsLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionCrsTransformLabelContext extends CoverageExpressionContext {
		public CrsTransformExpressionContext crsTransformExpression() {
			return getRuleContext(CrsTransformExpressionContext.class,0);
		}
		public CoverageExpressionCrsTransformLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionCrsTransformLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionExponentialLabelContext extends CoverageExpressionContext {
		public ExponentialExpressionContext exponentialExpression() {
			return getRuleContext(ExponentialExpressionContext.class,0);
		}
		public CoverageExpressionExponentialLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionExponentialLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionCastLabelContext extends CoverageExpressionContext {
		public CastExpressionContext castExpression() {
			return getRuleContext(CastExpressionContext.class,0);
		}
		public CoverageExpressionCastLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionCastLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionClipCorridorLabelContext extends CoverageExpressionContext {
		public ClipCorridorExpressionContext clipCorridorExpression() {
			return getRuleContext(ClipCorridorExpressionContext.class,0);
		}
		public CoverageExpressionClipCorridorLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionClipCorridorLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionSliceLabelContext extends CoverageExpressionContext {
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public DimensionPointListContext dimensionPointList() {
			return getRuleContext(DimensionPointListContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode SLICE() { return getToken(wcpsParser.SLICE, 0); }
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public CoverageExpressionSliceLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionSliceLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionScaleByFactorLabelContext extends CoverageExpressionContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode SCALE_FACTOR() { return getToken(wcpsParser.SCALE_FACTOR, 0); }
		public CoverageExpressionScaleByFactorLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionScaleByFactorLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionTrimCoverageLabelContext extends CoverageExpressionContext {
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public DimensionIntervalListContext dimensionIntervalList() {
			return getRuleContext(DimensionIntervalListContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode TRIM() { return getToken(wcpsParser.TRIM, 0); }
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public CoverageExpressionTrimCoverageLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionTrimCoverageLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionArithmeticLabelContext extends CoverageExpressionContext {
		public CoverageArithmeticOperatorContext coverageArithmeticOperator() {
			return getRuleContext(CoverageArithmeticOperatorContext.class,0);
		}
		public List<CoverageExpressionContext> coverageExpression() {
			return getRuleContexts(CoverageExpressionContext.class);
		}
		public CoverageExpressionContext coverageExpression(int i) {
			return getRuleContext(CoverageExpressionContext.class,i);
		}
		public CoverageExpressionArithmeticLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionArithmeticLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionRangeConstructorLabelContext extends CoverageExpressionContext {
		public RangeConstructorExpressionContext rangeConstructorExpression() {
			return getRuleContext(RangeConstructorExpressionContext.class,0);
		}
		public CoverageExpressionRangeConstructorLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionRangeConstructorLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CoverageExpressionDecodeLabelContext extends CoverageExpressionContext {
		public DecodeCoverageExpressionContext decodeCoverageExpression() {
			return getRuleContext(DecodeCoverageExpressionContext.class,0);
		}
		public CoverageExpressionDecodeLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionDecodeLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoverageExpressionContext coverageExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		CoverageExpressionContext _localctx = new CoverageExpressionContext(_ctx, _parentState, _p);
		CoverageExpressionContext _prevctx = _localctx;
		int _startState = 84;
		enterRecursionRule(_localctx, RULE_coverageExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(629);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				_localctx = new CoverageExpressionDomainIntervalsLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(517); domainIntervals();
				}
				break;

			case 2:
				{
				_localctx = new CoverageExpressionConstructorLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(518); coverageConstructorExpression();
				}
				break;

			case 3:
				{
				_localctx = new CoverageExpressionVariableNameLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(519); coverageVariableName();
				}
				break;

			case 4:
				{
				_localctx = new CoverageExpressionConstantLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(520); coverageConstantExpression();
				}
				break;

			case 5:
				{
				_localctx = new CoverageExpressionDecodeLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(521); decodeCoverageExpression();
				}
				break;

			case 6:
				{
				_localctx = new CoverageExpressionSliceLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(522); match(SLICE);
				setState(523); match(LEFT_PARENTHESIS);
				setState(524); coverageExpression(0);
				setState(525); match(COMMA);
				setState(526); match(LEFT_BRACE);
				setState(527); dimensionPointList();
				setState(528); match(RIGHT_BRACE);
				setState(529); match(RIGHT_PARENTHESIS);
				}
				break;

			case 7:
				{
				_localctx = new CoverageExpressionTrimCoverageLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(531); match(TRIM);
				setState(532); match(LEFT_PARENTHESIS);
				setState(533); coverageExpression(0);
				setState(534); match(COMMA);
				setState(535); match(LEFT_BRACE);
				setState(536); dimensionIntervalList();
				setState(537); match(RIGHT_BRACE);
				setState(538); match(RIGHT_PARENTHESIS);
				}
				break;

			case 8:
				{
				_localctx = new CoverageExpressionExtendLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(540); match(EXTEND);
				setState(541); match(LEFT_PARENTHESIS);
				setState(542); coverageExpression(0);
				setState(543); match(COMMA);
				setState(544); match(LEFT_BRACE);
				setState(545); dimensionIntervalList();
				setState(546); match(RIGHT_BRACE);
				setState(547); match(RIGHT_PARENTHESIS);
				}
				break;

			case 9:
				{
				_localctx = new CoverageExpressionExtendByDomainIntervalsLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(549); match(EXTEND);
				setState(550); match(LEFT_PARENTHESIS);
				setState(551); coverageExpression(0);
				setState(552); match(COMMA);
				setState(553); match(LEFT_BRACE);
				setState(554); domainIntervals();
				setState(555); match(RIGHT_BRACE);
				setState(556); match(RIGHT_PARENTHESIS);
				}
				break;

			case 10:
				{
				_localctx = new CoverageExpressionUnaryArithmeticLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(558); unaryArithmeticExpression();
				}
				break;

			case 11:
				{
				_localctx = new CoverageExpressionTrigonometricLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(559); trigonometricExpression();
				}
				break;

			case 12:
				{
				_localctx = new CoverageExpressionExponentialLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(560); exponentialExpression();
				}
				break;

			case 13:
				{
				_localctx = new CoverageExpressionMinBinaryLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(561); minBinaryExpression();
				}
				break;

			case 14:
				{
				_localctx = new CoverageExpressionMaxBinaryLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(562); maxBinaryExpression();
				}
				break;

			case 15:
				{
				_localctx = new CoverageExpressionPowerLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(563); unaryPowerExpression();
				}
				break;

			case 16:
				{
				_localctx = new CoverageExpressionUnaryBooleanLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(564); unaryBooleanExpression();
				}
				break;

			case 17:
				{
				_localctx = new CoverageExpressionCastLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(565); castExpression();
				}
				break;

			case 18:
				{
				_localctx = new CoverageExpressionRangeConstructorLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(566); rangeConstructorExpression();
				}
				break;

			case 19:
				{
				_localctx = new CoverageExpressionClipWKTLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(567); clipWKTExpression();
				}
				break;

			case 20:
				{
				_localctx = new CoverageExpressionClipCurtainLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(568); clipCurtainExpression();
				}
				break;

			case 21:
				{
				_localctx = new CoverageExpressionClipCorridorLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(569); clipCorridorExpression();
				}
				break;

			case 22:
				{
				_localctx = new CoverageExpressionCrsTransformLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(570); crsTransformExpression();
				}
				break;

			case 23:
				{
				_localctx = new CoverageExpressionSwitchCaseLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(571); switchCaseExpression();
				}
				break;

			case 24:
				{
				_localctx = new CoverageExpressionScaleByDimensionIntervalsLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(572); match(SCALE);
				setState(573); match(LEFT_PARENTHESIS);
				setState(574); coverageExpression(0);
				setState(575); match(COMMA);
				setState(576); match(LEFT_BRACE);
				setState(577); dimensionIntervalList();
				setState(578); match(RIGHT_BRACE);
				setState(579); match(RIGHT_PARENTHESIS);
				}
				break;

			case 25:
				{
				_localctx = new CoverageExpressionScaleByImageCrsDomainLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(581); match(SCALE);
				setState(582); match(LEFT_PARENTHESIS);
				setState(583); coverageExpression(0);
				setState(584); match(COMMA);
				setState(585); match(LEFT_BRACE);
				setState(586); domainIntervals();
				setState(587); match(RIGHT_BRACE);
				setState(588); match(RIGHT_PARENTHESIS);
				}
				break;

			case 26:
				{
				_localctx = new CoverageExpressionCoverageLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(590); match(LEFT_PARENTHESIS);
				setState(591); coverageExpression(0);
				setState(592); match(RIGHT_PARENTHESIS);
				}
				break;

			case 27:
				{
				_localctx = new CoverageExpressionScaleByFactorLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(594); match(SCALE_FACTOR);
				setState(595); match(LEFT_PARENTHESIS);
				setState(596); coverageExpression(0);
				setState(597); match(COMMA);
				setState(598); number();
				setState(599); match(RIGHT_PARENTHESIS);
				}
				break;

			case 28:
				{
				_localctx = new CoverageExpressionScaleByAxesLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(601); match(SCALE_AXES);
				setState(602); match(LEFT_PARENTHESIS);
				setState(603); coverageExpression(0);
				setState(604); match(COMMA);
				setState(605); match(LEFT_BRACKET);
				setState(606); scaleDimensionIntervalList();
				setState(607); match(RIGHT_BRACKET);
				setState(608); match(RIGHT_PARENTHESIS);
				}
				break;

			case 29:
				{
				_localctx = new CoverageExpressionScaleBySizeLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(610); match(SCALE_SIZE);
				setState(611); match(LEFT_PARENTHESIS);
				setState(612); coverageExpression(0);
				setState(613); match(COMMA);
				setState(614); match(LEFT_BRACKET);
				setState(615); scaleDimensionIntervalList();
				setState(616); match(RIGHT_BRACKET);
				setState(617); match(RIGHT_PARENTHESIS);
				}
				break;

			case 30:
				{
				_localctx = new CoverageExpressionScaleByExtentLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(619); match(SCALE_EXTENT);
				setState(620); match(LEFT_PARENTHESIS);
				setState(621); coverageExpression(0);
				setState(622); match(COMMA);
				setState(623); match(LEFT_BRACKET);
				setState(624); scaleDimensionIntervalList();
				setState(625); match(RIGHT_BRACKET);
				setState(626); match(RIGHT_PARENTHESIS);
				}
				break;

			case 31:
				{
				_localctx = new CoverageExpressionScalarLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(628); scalarExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(672);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,39,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(670);
					switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
					case 1:
						{
						_localctx = new CoverageExpressionOverlayLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(631);
						if (!(1 >= _localctx._p)) throw new FailedPredicateException(this, "1 >= $_p");
						setState(632); match(OVERLAY);
						setState(633); coverageExpression(2);
						}
						break;

					case 2:
						{
						_localctx = new CoverageExpressionLogicLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(634);
						if (!(40 >= _localctx._p)) throw new FailedPredicateException(this, "40 >= $_p");
						setState(635); booleanOperator();
						setState(636); coverageExpression(0);
						}
						break;

					case 3:
						{
						_localctx = new CoverageExpressionArithmeticLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(638);
						if (!(37 >= _localctx._p)) throw new FailedPredicateException(this, "37 >= $_p");
						setState(639); coverageArithmeticOperator();
						setState(640); coverageExpression(0);
						}
						break;

					case 4:
						{
						_localctx = new CoverageExpressionComparissonLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(642);
						if (!(36 >= _localctx._p)) throw new FailedPredicateException(this, "36 >= $_p");
						setState(643); numericalComparissonOperator();
						setState(644); coverageExpression(0);
						}
						break;

					case 5:
						{
						_localctx = new CoverageExpressionShorthandSliceLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(646);
						if (!(32 >= _localctx._p)) throw new FailedPredicateException(this, "32 >= $_p");
						setState(647); match(LEFT_BRACKET);
						setState(648); dimensionPointList();
						setState(649); match(RIGHT_BRACKET);
						}
						break;

					case 6:
						{
						_localctx = new CoverageExpressionShorthandSubsetLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(651);
						if (!(30 >= _localctx._p)) throw new FailedPredicateException(this, "30 >= $_p");
						setState(652); match(LEFT_BRACKET);
						setState(653); dimensionIntervalList();
						setState(654); match(RIGHT_BRACKET);
						}
						break;

					case 7:
						{
						_localctx = new CoverageXpressionShortHandSubsetWithLetClauseVariableLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(656);
						if (!(29 >= _localctx._p)) throw new FailedPredicateException(this, "29 >= $_p");
						setState(657); match(LEFT_BRACKET);
						setState(658); coverageVariableName();
						setState(659); match(RIGHT_BRACKET);
						}
						break;

					case 8:
						{
						_localctx = new CoverageExpressionRangeSubsettingLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(661);
						if (!(17 >= _localctx._p)) throw new FailedPredicateException(this, "17 >= $_p");
						setState(662); match(DOT);
						setState(663); fieldName();
						}
						break;

					case 9:
						{
						_localctx = new CoverageIsNullExpressionContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(664);
						if (!(2 >= _localctx._p)) throw new FailedPredicateException(this, "2 >= $_p");
						setState(665); match(IS);
						setState(667);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(666); match(NOT);
							}
						}

						setState(669); match(NULL);
						}
						break;
					}
					} 
				}
				setState(674);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,39,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class CoverageArithmeticOperatorContext extends ParserRuleContext {
		public TerminalNode MULTIPLICATION() { return getToken(wcpsParser.MULTIPLICATION, 0); }
		public TerminalNode DIVISION() { return getToken(wcpsParser.DIVISION, 0); }
		public TerminalNode MINUS() { return getToken(wcpsParser.MINUS, 0); }
		public TerminalNode PLUS() { return getToken(wcpsParser.PLUS, 0); }
		public CoverageArithmeticOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coverageArithmeticOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageArithmeticOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoverageArithmeticOperatorContext coverageArithmeticOperator() throws RecognitionException {
		CoverageArithmeticOperatorContext _localctx = new CoverageArithmeticOperatorContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_coverageArithmeticOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(675);
			_la = _input.LA(1);
			if ( !(((((_la - 28)) & ~0x3f) == 0 && ((1L << (_la - 28)) & ((1L << (DIVISION - 28)) | (1L << (MINUS - 28)) | (1L << (MULTIPLICATION - 28)) | (1L << (PLUS - 28)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnaryArithmeticExpressionOperatorContext extends ParserRuleContext {
		public TerminalNode ABSOLUTE_VALUE() { return getToken(wcpsParser.ABSOLUTE_VALUE, 0); }
		public TerminalNode SQUARE_ROOT() { return getToken(wcpsParser.SQUARE_ROOT, 0); }
		public TerminalNode IMAGINARY_PART() { return getToken(wcpsParser.IMAGINARY_PART, 0); }
		public TerminalNode REAL_PART() { return getToken(wcpsParser.REAL_PART, 0); }
		public UnaryArithmeticExpressionOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryArithmeticExpressionOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitUnaryArithmeticExpressionOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryArithmeticExpressionOperatorContext unaryArithmeticExpressionOperator() throws RecognitionException {
		UnaryArithmeticExpressionOperatorContext _localctx = new UnaryArithmeticExpressionOperatorContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_unaryArithmeticExpressionOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(677);
			_la = _input.LA(1);
			if ( !(_la==ABSOLUTE_VALUE || _la==IMAGINARY_PART || _la==REAL_PART || _la==SQUARE_ROOT) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnaryArithmeticExpressionContext extends ParserRuleContext {
		public UnaryArithmeticExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryArithmeticExpression; }
	 
		public UnaryArithmeticExpressionContext() { }
		public void copyFrom(UnaryArithmeticExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class UnaryCoverageArithmeticExpressionLabelContext extends UnaryArithmeticExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public UnaryArithmeticExpressionOperatorContext unaryArithmeticExpressionOperator() {
			return getRuleContext(UnaryArithmeticExpressionOperatorContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public UnaryCoverageArithmeticExpressionLabelContext(UnaryArithmeticExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitUnaryCoverageArithmeticExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryArithmeticExpressionContext unaryArithmeticExpression() throws RecognitionException {
		UnaryArithmeticExpressionContext _localctx = new UnaryArithmeticExpressionContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_unaryArithmeticExpression);
		try {
			_localctx = new UnaryCoverageArithmeticExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(679); unaryArithmeticExpressionOperator();
			setState(681);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				{
				setState(680); match(LEFT_PARENTHESIS);
				}
				break;
			}
			setState(683); coverageExpression(0);
			setState(685);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				{
				setState(684); match(RIGHT_PARENTHESIS);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TrigonometricExpressionContext extends ParserRuleContext {
		public TrigonometricExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_trigonometricExpression; }
	 
		public TrigonometricExpressionContext() { }
		public void copyFrom(TrigonometricExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TrigonometricExpressionLabelContext extends TrigonometricExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TrigonometricOperatorContext trigonometricOperator() {
			return getRuleContext(TrigonometricOperatorContext.class,0);
		}
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TrigonometricExpressionLabelContext(TrigonometricExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitTrigonometricExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TrigonometricExpressionContext trigonometricExpression() throws RecognitionException {
		TrigonometricExpressionContext _localctx = new TrigonometricExpressionContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_trigonometricExpression);
		try {
			_localctx = new TrigonometricExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(687); trigonometricOperator();
			setState(688); match(LEFT_PARENTHESIS);
			setState(689); coverageExpression(0);
			setState(690); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExponentialExpressionOperatorContext extends ParserRuleContext {
		public TerminalNode LN() { return getToken(wcpsParser.LN, 0); }
		public TerminalNode LOG() { return getToken(wcpsParser.LOG, 0); }
		public TerminalNode EXP() { return getToken(wcpsParser.EXP, 0); }
		public ExponentialExpressionOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exponentialExpressionOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitExponentialExpressionOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExponentialExpressionOperatorContext exponentialExpressionOperator() throws RecognitionException {
		ExponentialExpressionOperatorContext _localctx = new ExponentialExpressionOperatorContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_exponentialExpressionOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(692);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EXP) | (1L << LN) | (1L << LOG))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExponentialExpressionContext extends ParserRuleContext {
		public ExponentialExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exponentialExpression; }
	 
		public ExponentialExpressionContext() { }
		public void copyFrom(ExponentialExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ExponentialExpressionLabelContext extends ExponentialExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public ExponentialExpressionOperatorContext exponentialExpressionOperator() {
			return getRuleContext(ExponentialExpressionOperatorContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public ExponentialExpressionLabelContext(ExponentialExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitExponentialExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExponentialExpressionContext exponentialExpression() throws RecognitionException {
		ExponentialExpressionContext _localctx = new ExponentialExpressionContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_exponentialExpression);
		try {
			_localctx = new ExponentialExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(694); exponentialExpressionOperator();
			setState(695); match(LEFT_PARENTHESIS);
			setState(696); coverageExpression(0);
			setState(697); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnaryPowerExpressionContext extends ParserRuleContext {
		public UnaryPowerExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryPowerExpression; }
	 
		public UnaryPowerExpressionContext() { }
		public void copyFrom(UnaryPowerExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class UnaryPowerExpressionLabelContext extends UnaryPowerExpressionContext {
		public NumericalScalarExpressionContext numericalScalarExpression() {
			return getRuleContext(NumericalScalarExpressionContext.class,0);
		}
		public TerminalNode POWER() { return getToken(wcpsParser.POWER, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public UnaryPowerExpressionLabelContext(UnaryPowerExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitUnaryPowerExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryPowerExpressionContext unaryPowerExpression() throws RecognitionException {
		UnaryPowerExpressionContext _localctx = new UnaryPowerExpressionContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_unaryPowerExpression);
		try {
			_localctx = new UnaryPowerExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(699); match(POWER);
			setState(700); match(LEFT_PARENTHESIS);
			setState(701); coverageExpression(0);
			setState(702); match(COMMA);
			setState(703); numericalScalarExpression(0);
			setState(704); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MinBinaryExpressionContext extends ParserRuleContext {
		public MinBinaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minBinaryExpression; }
	 
		public MinBinaryExpressionContext() { }
		public void copyFrom(MinBinaryExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class MinBinaryExpressionLabelContext extends MinBinaryExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode MIN() { return getToken(wcpsParser.MIN, 0); }
		public List<CoverageExpressionContext> coverageExpression() {
			return getRuleContexts(CoverageExpressionContext.class);
		}
		public CoverageExpressionContext coverageExpression(int i) {
			return getRuleContext(CoverageExpressionContext.class,i);
		}
		public MinBinaryExpressionLabelContext(MinBinaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitMinBinaryExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MinBinaryExpressionContext minBinaryExpression() throws RecognitionException {
		MinBinaryExpressionContext _localctx = new MinBinaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_minBinaryExpression);
		try {
			_localctx = new MinBinaryExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(706); match(MIN);
			setState(707); match(LEFT_PARENTHESIS);
			setState(708); coverageExpression(0);
			setState(709); match(COMMA);
			setState(710); coverageExpression(0);
			setState(711); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MaxBinaryExpressionContext extends ParserRuleContext {
		public MaxBinaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_maxBinaryExpression; }
	 
		public MaxBinaryExpressionContext() { }
		public void copyFrom(MaxBinaryExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class MaxBinaryExpressionLabelContext extends MaxBinaryExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public List<CoverageExpressionContext> coverageExpression() {
			return getRuleContexts(CoverageExpressionContext.class);
		}
		public TerminalNode MAX() { return getToken(wcpsParser.MAX, 0); }
		public CoverageExpressionContext coverageExpression(int i) {
			return getRuleContext(CoverageExpressionContext.class,i);
		}
		public MaxBinaryExpressionLabelContext(MaxBinaryExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitMaxBinaryExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MaxBinaryExpressionContext maxBinaryExpression() throws RecognitionException {
		MaxBinaryExpressionContext _localctx = new MaxBinaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_maxBinaryExpression);
		try {
			_localctx = new MaxBinaryExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(713); match(MAX);
			setState(714); match(LEFT_PARENTHESIS);
			setState(715); coverageExpression(0);
			setState(716); match(COMMA);
			setState(717); coverageExpression(0);
			setState(718); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class UnaryBooleanExpressionContext extends ParserRuleContext {
		public UnaryBooleanExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryBooleanExpression; }
	 
		public UnaryBooleanExpressionContext() { }
		public void copyFrom(UnaryBooleanExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class BitUnaryBooleanExpressionLabelContext extends UnaryBooleanExpressionContext {
		public NumericalScalarExpressionContext numericalScalarExpression() {
			return getRuleContext(NumericalScalarExpressionContext.class,0);
		}
		public TerminalNode BIT() { return getToken(wcpsParser.BIT, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public BitUnaryBooleanExpressionLabelContext(UnaryBooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitBitUnaryBooleanExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NotUnaryBooleanExpressionLabelContext extends UnaryBooleanExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode NOT() { return getToken(wcpsParser.NOT, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public NotUnaryBooleanExpressionLabelContext(UnaryBooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNotUnaryBooleanExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryBooleanExpressionContext unaryBooleanExpression() throws RecognitionException {
		UnaryBooleanExpressionContext _localctx = new UnaryBooleanExpressionContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_unaryBooleanExpression);
		try {
			setState(732);
			switch (_input.LA(1)) {
			case NOT:
				_localctx = new NotUnaryBooleanExpressionLabelContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(720); match(NOT);
				setState(721); match(LEFT_PARENTHESIS);
				setState(722); coverageExpression(0);
				setState(723); match(RIGHT_PARENTHESIS);
				}
				break;
			case BIT:
				_localctx = new BitUnaryBooleanExpressionLabelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(725); match(BIT);
				setState(726); match(LEFT_PARENTHESIS);
				setState(727); coverageExpression(0);
				setState(728); match(COMMA);
				setState(729); numericalScalarExpression(0);
				setState(730); match(RIGHT_PARENTHESIS);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RangeTypeContext extends ParserRuleContext {
		public TerminalNode COVERAGE_VARIABLE_NAME(int i) {
			return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, i);
		}
		public List<TerminalNode> COVERAGE_VARIABLE_NAME() { return getTokens(wcpsParser.COVERAGE_VARIABLE_NAME); }
		public RangeTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rangeType; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitRangeType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangeTypeContext rangeType() throws RecognitionException {
		RangeTypeContext _localctx = new RangeTypeContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_rangeType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(734); match(COVERAGE_VARIABLE_NAME);
			setState(738);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COVERAGE_VARIABLE_NAME) {
				{
				{
				setState(735); match(COVERAGE_VARIABLE_NAME);
				}
				}
				setState(740);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CastExpressionContext extends ParserRuleContext {
		public CastExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castExpression; }
	 
		public CastExpressionContext() { }
		public void copyFrom(CastExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CastExpressionLabelContext extends CastExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public RangeTypeContext rangeType() {
			return getRuleContext(RangeTypeContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public CastExpressionLabelContext(CastExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCastExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CastExpressionContext castExpression() throws RecognitionException {
		CastExpressionContext _localctx = new CastExpressionContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_castExpression);
		try {
			_localctx = new CastExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(741); match(LEFT_PARENTHESIS);
			setState(742); rangeType();
			setState(743); match(RIGHT_PARENTHESIS);
			setState(744); coverageExpression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldNameContext extends ParserRuleContext {
		public TerminalNode COVERAGE_VARIABLE_NAME() { return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, 0); }
		public TerminalNode INTEGER() { return getToken(wcpsParser.INTEGER, 0); }
		public FieldNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitFieldName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldNameContext fieldName() throws RecognitionException {
		FieldNameContext _localctx = new FieldNameContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_fieldName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(746);
			_la = _input.LA(1);
			if ( !(_la==INTEGER || _la==COVERAGE_VARIABLE_NAME) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RangeConstructorExpressionContext extends ParserRuleContext {
		public RangeConstructorExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rangeConstructorExpression; }
	 
		public RangeConstructorExpressionContext() { }
		public void copyFrom(RangeConstructorExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class RangeConstructorExpressionLabelContext extends RangeConstructorExpressionContext {
		public List<TerminalNode> SEMICOLON() { return getTokens(wcpsParser.SEMICOLON); }
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(wcpsParser.SEMICOLON, i);
		}
		public TerminalNode COLON(int i) {
			return getToken(wcpsParser.COLON, i);
		}
		public List<CoverageExpressionContext> coverageExpression() {
			return getRuleContexts(CoverageExpressionContext.class);
		}
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public CoverageExpressionContext coverageExpression(int i) {
			return getRuleContext(CoverageExpressionContext.class,i);
		}
		public FieldNameContext fieldName(int i) {
			return getRuleContext(FieldNameContext.class,i);
		}
		public List<FieldNameContext> fieldName() {
			return getRuleContexts(FieldNameContext.class);
		}
		public List<TerminalNode> COLON() { return getTokens(wcpsParser.COLON); }
		public RangeConstructorExpressionLabelContext(RangeConstructorExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitRangeConstructorExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangeConstructorExpressionContext rangeConstructorExpression() throws RecognitionException {
		RangeConstructorExpressionContext _localctx = new RangeConstructorExpressionContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_rangeConstructorExpression);
		int _la;
		try {
			_localctx = new RangeConstructorExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(748); match(LEFT_BRACE);
			{
			setState(749); fieldName();
			setState(750); match(COLON);
			setState(751); coverageExpression(0);
			}
			setState(760);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMICOLON) {
				{
				{
				setState(753); match(SEMICOLON);
				setState(754); fieldName();
				setState(755); match(COLON);
				setState(756); coverageExpression(0);
				}
				}
				setState(762);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(763); match(RIGHT_BRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RangeConstructorSwitchCaseExpressionContext extends ParserRuleContext {
		public RangeConstructorSwitchCaseExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rangeConstructorSwitchCaseExpression; }
	 
		public RangeConstructorSwitchCaseExpressionContext() { }
		public void copyFrom(RangeConstructorSwitchCaseExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class RangeConstructorSwitchCaseExpressionLabelContext extends RangeConstructorSwitchCaseExpressionContext {
		public List<TerminalNode> SEMICOLON() { return getTokens(wcpsParser.SEMICOLON); }
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(wcpsParser.SEMICOLON, i);
		}
		public TerminalNode COLON(int i) {
			return getToken(wcpsParser.COLON, i);
		}
		public List<CoverageExpressionContext> coverageExpression() {
			return getRuleContexts(CoverageExpressionContext.class);
		}
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public CoverageExpressionContext coverageExpression(int i) {
			return getRuleContext(CoverageExpressionContext.class,i);
		}
		public FieldNameContext fieldName(int i) {
			return getRuleContext(FieldNameContext.class,i);
		}
		public List<FieldNameContext> fieldName() {
			return getRuleContexts(FieldNameContext.class);
		}
		public List<TerminalNode> COLON() { return getTokens(wcpsParser.COLON); }
		public RangeConstructorSwitchCaseExpressionLabelContext(RangeConstructorSwitchCaseExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitRangeConstructorSwitchCaseExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangeConstructorSwitchCaseExpressionContext rangeConstructorSwitchCaseExpression() throws RecognitionException {
		RangeConstructorSwitchCaseExpressionContext _localctx = new RangeConstructorSwitchCaseExpressionContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_rangeConstructorSwitchCaseExpression);
		int _la;
		try {
			_localctx = new RangeConstructorSwitchCaseExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(765); match(LEFT_BRACE);
			{
			setState(766); fieldName();
			setState(767); match(COLON);
			setState(768); coverageExpression(0);
			}
			setState(777);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMICOLON) {
				{
				{
				setState(770); match(SEMICOLON);
				setState(771); fieldName();
				setState(772); match(COLON);
				setState(773); coverageExpression(0);
				}
				}
				setState(779);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(780); match(RIGHT_BRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DimensionPointListContext extends ParserRuleContext {
		public DimensionPointListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dimensionPointList; }
	 
		public DimensionPointListContext() { }
		public void copyFrom(DimensionPointListContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class DimensionPointListLabelContext extends DimensionPointListContext {
		public List<DimensionPointElementContext> dimensionPointElement() {
			return getRuleContexts(DimensionPointElementContext.class);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public DimensionPointElementContext dimensionPointElement(int i) {
			return getRuleContext(DimensionPointElementContext.class,i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public DimensionPointListLabelContext(DimensionPointListContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitDimensionPointListLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DimensionPointListContext dimensionPointList() throws RecognitionException {
		DimensionPointListContext _localctx = new DimensionPointListContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_dimensionPointList);
		int _la;
		try {
			_localctx = new DimensionPointListLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(782); dimensionPointElement();
			setState(787);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(783); match(COMMA);
				setState(784); dimensionPointElement();
				}
				}
				setState(789);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DimensionPointElementContext extends ParserRuleContext {
		public DimensionPointElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dimensionPointElement; }
	 
		public DimensionPointElementContext() { }
		public void copyFrom(DimensionPointElementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class DimensionPointElementLabelContext extends DimensionPointElementContext {
		public CrsNameContext crsName() {
			return getRuleContext(CrsNameContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode COLON() { return getToken(wcpsParser.COLON, 0); }
		public DimensionPointElementLabelContext(DimensionPointElementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitDimensionPointElementLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DimensionPointElementContext dimensionPointElement() throws RecognitionException {
		DimensionPointElementContext _localctx = new DimensionPointElementContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_dimensionPointElement);
		int _la;
		try {
			_localctx = new DimensionPointElementLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(790); axisName();
			setState(793);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(791); match(COLON);
				setState(792); crsName();
				}
			}

			setState(795); match(LEFT_PARENTHESIS);
			setState(796); coverageExpression(0);
			setState(797); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DimensionIntervalListContext extends ParserRuleContext {
		public DimensionIntervalListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dimensionIntervalList; }
	 
		public DimensionIntervalListContext() { }
		public void copyFrom(DimensionIntervalListContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class DimensionIntervalListLabelContext extends DimensionIntervalListContext {
		public DimensionIntervalElementContext dimensionIntervalElement(int i) {
			return getRuleContext(DimensionIntervalElementContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<DimensionIntervalElementContext> dimensionIntervalElement() {
			return getRuleContexts(DimensionIntervalElementContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public DimensionIntervalListLabelContext(DimensionIntervalListContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitDimensionIntervalListLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DimensionIntervalListContext dimensionIntervalList() throws RecognitionException {
		DimensionIntervalListContext _localctx = new DimensionIntervalListContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_dimensionIntervalList);
		int _la;
		try {
			_localctx = new DimensionIntervalListLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(799); dimensionIntervalElement();
			setState(804);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(800); match(COMMA);
				setState(801); dimensionIntervalElement();
				}
				}
				setState(806);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScaleDimensionIntervalListContext extends ParserRuleContext {
		public ScaleDimensionIntervalListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scaleDimensionIntervalList; }
	 
		public ScaleDimensionIntervalListContext() { }
		public void copyFrom(ScaleDimensionIntervalListContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ScaleDimensionIntervalListLabelContext extends ScaleDimensionIntervalListContext {
		public ScaleDimensionIntervalElementContext scaleDimensionIntervalElement(int i) {
			return getRuleContext(ScaleDimensionIntervalElementContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<ScaleDimensionIntervalElementContext> scaleDimensionIntervalElement() {
			return getRuleContexts(ScaleDimensionIntervalElementContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public ScaleDimensionIntervalListLabelContext(ScaleDimensionIntervalListContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitScaleDimensionIntervalListLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScaleDimensionIntervalListContext scaleDimensionIntervalList() throws RecognitionException {
		ScaleDimensionIntervalListContext _localctx = new ScaleDimensionIntervalListContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_scaleDimensionIntervalList);
		int _la;
		try {
			_localctx = new ScaleDimensionIntervalListLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(807); scaleDimensionIntervalElement();
			setState(812);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(808); match(COMMA);
				setState(809); scaleDimensionIntervalElement();
				}
				}
				setState(814);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ScaleDimensionIntervalElementContext extends ParserRuleContext {
		public ScaleDimensionIntervalElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scaleDimensionIntervalElement; }
	 
		public ScaleDimensionIntervalElementContext() { }
		public void copyFrom(ScaleDimensionIntervalElementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TrimScaleDimensionIntervalElementLabelContext extends ScaleDimensionIntervalElementContext {
		public List<NumberContext> number() {
			return getRuleContexts(NumberContext.class);
		}
		public TerminalNode STRING_LITERAL(int i) {
			return getToken(wcpsParser.STRING_LITERAL, i);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public List<TerminalNode> STRING_LITERAL() { return getTokens(wcpsParser.STRING_LITERAL); }
		public NumberContext number(int i) {
			return getRuleContext(NumberContext.class,i);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COLON() { return getToken(wcpsParser.COLON, 0); }
		public TrimScaleDimensionIntervalElementLabelContext(ScaleDimensionIntervalElementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitTrimScaleDimensionIntervalElementLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SliceScaleDimensionIntervalElementLabelContext extends ScaleDimensionIntervalElementContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public SliceScaleDimensionIntervalElementLabelContext(ScaleDimensionIntervalElementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitSliceScaleDimensionIntervalElementLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScaleDimensionIntervalElementContext scaleDimensionIntervalElement() throws RecognitionException {
		ScaleDimensionIntervalElementContext _localctx = new ScaleDimensionIntervalElementContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_scaleDimensionIntervalElement);
		try {
			setState(833);
			switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
			case 1:
				_localctx = new TrimScaleDimensionIntervalElementLabelContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(815); axisName();
				setState(816); match(LEFT_PARENTHESIS);
				setState(819);
				switch (_input.LA(1)) {
				case MINUS:
				case INTEGER:
				case REAL_NUMBER_CONSTANT:
				case SCIENTIFIC_NUMBER_CONSTANT:
					{
					setState(817); number();
					}
					break;
				case STRING_LITERAL:
					{
					setState(818); match(STRING_LITERAL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(821); match(COLON);
				setState(824);
				switch (_input.LA(1)) {
				case MINUS:
				case INTEGER:
				case REAL_NUMBER_CONSTANT:
				case SCIENTIFIC_NUMBER_CONSTANT:
					{
					setState(822); number();
					}
					break;
				case STRING_LITERAL:
					{
					setState(823); match(STRING_LITERAL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(826); match(RIGHT_PARENTHESIS);
				}
				break;

			case 2:
				_localctx = new SliceScaleDimensionIntervalElementLabelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(828); axisName();
				setState(829); match(LEFT_PARENTHESIS);
				setState(830); number();
				setState(831); match(RIGHT_PARENTHESIS);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DimensionIntervalElementContext extends ParserRuleContext {
		public DimensionIntervalElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dimensionIntervalElement; }
	 
		public DimensionIntervalElementContext() { }
		public void copyFrom(DimensionIntervalElementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class TrimDimensionIntervalElementLabelContext extends DimensionIntervalElementContext {
		public CrsNameContext crsName() {
			return getRuleContext(CrsNameContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public TerminalNode COLON(int i) {
			return getToken(wcpsParser.COLON, i);
		}
		public List<CoverageExpressionContext> coverageExpression() {
			return getRuleContexts(CoverageExpressionContext.class);
		}
		public CoverageExpressionContext coverageExpression(int i) {
			return getRuleContext(CoverageExpressionContext.class,i);
		}
		public List<TerminalNode> COLON() { return getTokens(wcpsParser.COLON); }
		public TrimDimensionIntervalElementLabelContext(DimensionIntervalElementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitTrimDimensionIntervalElementLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SliceDimensionIntervalElementLabelContext extends DimensionIntervalElementContext {
		public CrsNameContext crsName() {
			return getRuleContext(CrsNameContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode COLON() { return getToken(wcpsParser.COLON, 0); }
		public SliceDimensionIntervalElementLabelContext(DimensionIntervalElementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitSliceDimensionIntervalElementLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DimensionIntervalElementContext dimensionIntervalElement() throws RecognitionException {
		DimensionIntervalElementContext _localctx = new DimensionIntervalElementContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_dimensionIntervalElement);
		int _la;
		try {
			setState(855);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				_localctx = new TrimDimensionIntervalElementLabelContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(835); axisName();
				setState(838);
				_la = _input.LA(1);
				if (_la==COLON) {
					{
					setState(836); match(COLON);
					setState(837); crsName();
					}
				}

				setState(840); match(LEFT_PARENTHESIS);
				setState(841); coverageExpression(0);
				setState(842); match(COLON);
				setState(843); coverageExpression(0);
				setState(844); match(RIGHT_PARENTHESIS);
				}
				break;

			case 2:
				_localctx = new SliceDimensionIntervalElementLabelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(846); axisName();
				setState(849);
				_la = _input.LA(1);
				if (_la==COLON) {
					{
					setState(847); match(COLON);
					setState(848); crsName();
					}
				}

				setState(851); match(LEFT_PARENTHESIS);
				setState(852); coverageExpression(0);
				setState(853); match(RIGHT_PARENTHESIS);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WktPointsContext extends ParserRuleContext {
		public WktPointsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wktPoints; }
	 
		public WktPointsContext() { }
		public void copyFrom(WktPointsContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class WktPointsLabelContext extends WktPointsContext {
		public ConstantContext constant(int i) {
			return getRuleContext(ConstantContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<ConstantContext> constant() {
			return getRuleContexts(ConstantContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public WktPointsLabelContext(WktPointsContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitWktPointsLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WktPointsContext wktPoints() throws RecognitionException {
		WktPointsContext _localctx = new WktPointsContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_wktPoints);
		int _la;
		try {
			_localctx = new WktPointsLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(857); constant();
			setState(861);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FALSE) | (1L << LEFT_PARENTHESIS) | (1L << MINUS))) != 0) || ((((_la - 92)) & ~0x3f) == 0 && ((1L << (_la - 92)) & ((1L << (TRUE - 92)) | (1L << (INTEGER - 92)) | (1L << (REAL_NUMBER_CONSTANT - 92)) | (1L << (SCIENTIFIC_NUMBER_CONSTANT - 92)) | (1L << (STRING_LITERAL - 92)))) != 0)) {
				{
				{
				setState(858); constant();
				}
				}
				setState(863);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
			setState(874);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(864); match(COMMA);
				setState(865); constant();
				setState(869);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FALSE) | (1L << LEFT_PARENTHESIS) | (1L << MINUS))) != 0) || ((((_la - 92)) & ~0x3f) == 0 && ((1L << (_la - 92)) & ((1L << (TRUE - 92)) | (1L << (INTEGER - 92)) | (1L << (REAL_NUMBER_CONSTANT - 92)) | (1L << (SCIENTIFIC_NUMBER_CONSTANT - 92)) | (1L << (STRING_LITERAL - 92)))) != 0)) {
					{
					{
					setState(866); constant();
					}
					}
					setState(871);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				setState(876);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WktPointElementListContext extends ParserRuleContext {
		public WktPointElementListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wktPointElementList; }
	 
		public WktPointElementListContext() { }
		public void copyFrom(WktPointElementListContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class WKTPointElementListLabelContext extends WktPointElementListContext {
		public List<WktPointsContext> wktPoints() {
			return getRuleContexts(WktPointsContext.class);
		}
		public TerminalNode LEFT_PARENTHESIS(int i) {
			return getToken(wcpsParser.LEFT_PARENTHESIS, i);
		}
		public List<TerminalNode> LEFT_PARENTHESIS() { return getTokens(wcpsParser.LEFT_PARENTHESIS); }
		public TerminalNode RIGHT_PARENTHESIS(int i) {
			return getToken(wcpsParser.RIGHT_PARENTHESIS, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<TerminalNode> RIGHT_PARENTHESIS() { return getTokens(wcpsParser.RIGHT_PARENTHESIS); }
		public WktPointsContext wktPoints(int i) {
			return getRuleContext(WktPointsContext.class,i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public WKTPointElementListLabelContext(WktPointElementListContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitWKTPointElementListLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WktPointElementListContext wktPointElementList() throws RecognitionException {
		WktPointElementListContext _localctx = new WktPointElementListContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_wktPointElementList);
		try {
			int _alt;
			_localctx = new WKTPointElementListLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(877); match(LEFT_PARENTHESIS);
			setState(878); wktPoints();
			setState(879); match(RIGHT_PARENTHESIS);
			setState(887);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					{
					{
					setState(880); match(COMMA);
					setState(881); match(LEFT_PARENTHESIS);
					setState(882); wktPoints();
					setState(883); match(RIGHT_PARENTHESIS);
					}
					} 
				}
				setState(889);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,59,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WktLineStringContext extends ParserRuleContext {
		public WktLineStringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wktLineString; }
	 
		public WktLineStringContext() { }
		public void copyFrom(WktLineStringContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class WKTLineStringLabelContext extends WktLineStringContext {
		public TerminalNode LINESTRING() { return getToken(wcpsParser.LINESTRING, 0); }
		public WktPointElementListContext wktPointElementList() {
			return getRuleContext(WktPointElementListContext.class,0);
		}
		public WKTLineStringLabelContext(WktLineStringContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitWKTLineStringLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WktLineStringContext wktLineString() throws RecognitionException {
		WktLineStringContext _localctx = new WktLineStringContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_wktLineString);
		try {
			_localctx = new WKTLineStringLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(890); match(LINESTRING);
			setState(891); wktPointElementList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WktPolygonContext extends ParserRuleContext {
		public WktPolygonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wktPolygon; }
	 
		public WktPolygonContext() { }
		public void copyFrom(WktPolygonContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class WKTPolygonLabelContext extends WktPolygonContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode POLYGON() { return getToken(wcpsParser.POLYGON, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public WktPointElementListContext wktPointElementList() {
			return getRuleContext(WktPointElementListContext.class,0);
		}
		public WKTPolygonLabelContext(WktPolygonContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitWKTPolygonLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WktPolygonContext wktPolygon() throws RecognitionException {
		WktPolygonContext _localctx = new WktPolygonContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_wktPolygon);
		try {
			_localctx = new WKTPolygonLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(893); match(POLYGON);
			setState(894); match(LEFT_PARENTHESIS);
			setState(895); wktPointElementList();
			setState(896); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WktMultipolygonContext extends ParserRuleContext {
		public WktMultipolygonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wktMultipolygon; }
	 
		public WktMultipolygonContext() { }
		public void copyFrom(WktMultipolygonContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class WKTMultipolygonLabelContext extends WktMultipolygonContext {
		public TerminalNode LEFT_PARENTHESIS(int i) {
			return getToken(wcpsParser.LEFT_PARENTHESIS, i);
		}
		public List<TerminalNode> LEFT_PARENTHESIS() { return getTokens(wcpsParser.LEFT_PARENTHESIS); }
		public TerminalNode RIGHT_PARENTHESIS(int i) {
			return getToken(wcpsParser.RIGHT_PARENTHESIS, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<TerminalNode> RIGHT_PARENTHESIS() { return getTokens(wcpsParser.RIGHT_PARENTHESIS); }
		public TerminalNode MULTIPOLYGON() { return getToken(wcpsParser.MULTIPOLYGON, 0); }
		public List<WktPointElementListContext> wktPointElementList() {
			return getRuleContexts(WktPointElementListContext.class);
		}
		public WktPointElementListContext wktPointElementList(int i) {
			return getRuleContext(WktPointElementListContext.class,i);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public WKTMultipolygonLabelContext(WktMultipolygonContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitWKTMultipolygonLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WktMultipolygonContext wktMultipolygon() throws RecognitionException {
		WktMultipolygonContext _localctx = new WktMultipolygonContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_wktMultipolygon);
		int _la;
		try {
			_localctx = new WKTMultipolygonLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(898); match(MULTIPOLYGON);
			setState(899); match(LEFT_PARENTHESIS);
			setState(900); match(LEFT_PARENTHESIS);
			setState(901); wktPointElementList();
			setState(902); match(RIGHT_PARENTHESIS);
			setState(910);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(903); match(COMMA);
				setState(904); match(LEFT_PARENTHESIS);
				setState(905); wktPointElementList();
				setState(906); match(RIGHT_PARENTHESIS);
				}
				}
				setState(912);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(913); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WktExpressionContext extends ParserRuleContext {
		public WktExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_wktExpression; }
	 
		public WktExpressionContext() { }
		public void copyFrom(WktExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class WKTExpressionLabelContext extends WktExpressionContext {
		public WktPolygonContext wktPolygon() {
			return getRuleContext(WktPolygonContext.class,0);
		}
		public WktMultipolygonContext wktMultipolygon() {
			return getRuleContext(WktMultipolygonContext.class,0);
		}
		public WktLineStringContext wktLineString() {
			return getRuleContext(WktLineStringContext.class,0);
		}
		public WKTExpressionLabelContext(WktExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitWKTExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WktExpressionContext wktExpression() throws RecognitionException {
		WktExpressionContext _localctx = new WktExpressionContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_wktExpression);
		try {
			_localctx = new WKTExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(918);
			switch (_input.LA(1)) {
			case POLYGON:
				{
				setState(915); wktPolygon();
				}
				break;
			case LINESTRING:
				{
				setState(916); wktLineString();
				}
				break;
			case MULTIPOLYGON:
				{
				setState(917); wktMultipolygon();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CurtainProjectionAxisLabel1Context extends ParserRuleContext {
		public TerminalNode COVERAGE_VARIABLE_NAME() { return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, 0); }
		public CurtainProjectionAxisLabel1Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_curtainProjectionAxisLabel1; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCurtainProjectionAxisLabel1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CurtainProjectionAxisLabel1Context curtainProjectionAxisLabel1() throws RecognitionException {
		CurtainProjectionAxisLabel1Context _localctx = new CurtainProjectionAxisLabel1Context(_ctx, getState());
		enterRule(_localctx, 140, RULE_curtainProjectionAxisLabel1);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(920); match(COVERAGE_VARIABLE_NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CurtainProjectionAxisLabel2Context extends ParserRuleContext {
		public TerminalNode COVERAGE_VARIABLE_NAME() { return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, 0); }
		public CurtainProjectionAxisLabel2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_curtainProjectionAxisLabel2; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCurtainProjectionAxisLabel2(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CurtainProjectionAxisLabel2Context curtainProjectionAxisLabel2() throws RecognitionException {
		CurtainProjectionAxisLabel2Context _localctx = new CurtainProjectionAxisLabel2Context(_ctx, getState());
		enterRule(_localctx, 142, RULE_curtainProjectionAxisLabel2);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(922); match(COVERAGE_VARIABLE_NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClipCurtainExpressionContext extends ParserRuleContext {
		public ClipCurtainExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_clipCurtainExpression; }
	 
		public ClipCurtainExpressionContext() { }
		public void copyFrom(ClipCurtainExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ClipCurtainExpressionLabelContext extends ClipCurtainExpressionContext {
		public TerminalNode CURTAIN() { return getToken(wcpsParser.CURTAIN, 0); }
		public TerminalNode CLIP() { return getToken(wcpsParser.CLIP, 0); }
		public TerminalNode LEFT_PARENTHESIS(int i) {
			return getToken(wcpsParser.LEFT_PARENTHESIS, i);
		}
		public WktExpressionContext wktExpression() {
			return getRuleContext(WktExpressionContext.class,0);
		}
		public CurtainProjectionAxisLabel1Context curtainProjectionAxisLabel1() {
			return getRuleContext(CurtainProjectionAxisLabel1Context.class,0);
		}
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public CrsNameContext crsName() {
			return getRuleContext(CrsNameContext.class,0);
		}
		public List<TerminalNode> LEFT_PARENTHESIS() { return getTokens(wcpsParser.LEFT_PARENTHESIS); }
		public TerminalNode RIGHT_PARENTHESIS(int i) {
			return getToken(wcpsParser.RIGHT_PARENTHESIS, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<TerminalNode> RIGHT_PARENTHESIS() { return getTokens(wcpsParser.RIGHT_PARENTHESIS); }
		public CurtainProjectionAxisLabel2Context curtainProjectionAxisLabel2() {
			return getRuleContext(CurtainProjectionAxisLabel2Context.class,0);
		}
		public TerminalNode PROJECTION() { return getToken(wcpsParser.PROJECTION, 0); }
		public ClipCurtainExpressionLabelContext(ClipCurtainExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitClipCurtainExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClipCurtainExpressionContext clipCurtainExpression() throws RecognitionException {
		ClipCurtainExpressionContext _localctx = new ClipCurtainExpressionContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_clipCurtainExpression);
		int _la;
		try {
			_localctx = new ClipCurtainExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(924); match(CLIP);
			setState(925); match(LEFT_PARENTHESIS);
			setState(926); coverageExpression(0);
			setState(927); match(COMMA);
			setState(928); match(CURTAIN);
			setState(929); match(LEFT_PARENTHESIS);
			setState(930); match(PROJECTION);
			setState(931); match(LEFT_PARENTHESIS);
			setState(932); curtainProjectionAxisLabel1();
			setState(933); match(COMMA);
			setState(934); curtainProjectionAxisLabel2();
			setState(935); match(RIGHT_PARENTHESIS);
			setState(936); match(COMMA);
			setState(937); wktExpression();
			setState(938); match(RIGHT_PARENTHESIS);
			setState(941);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(939); match(COMMA);
				setState(940); crsName();
				}
			}

			setState(943); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CorridorProjectionAxisLabel1Context extends ParserRuleContext {
		public TerminalNode COVERAGE_VARIABLE_NAME() { return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, 0); }
		public CorridorProjectionAxisLabel1Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_corridorProjectionAxisLabel1; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCorridorProjectionAxisLabel1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CorridorProjectionAxisLabel1Context corridorProjectionAxisLabel1() throws RecognitionException {
		CorridorProjectionAxisLabel1Context _localctx = new CorridorProjectionAxisLabel1Context(_ctx, getState());
		enterRule(_localctx, 146, RULE_corridorProjectionAxisLabel1);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(945); match(COVERAGE_VARIABLE_NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CorridorProjectionAxisLabel2Context extends ParserRuleContext {
		public TerminalNode COVERAGE_VARIABLE_NAME() { return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, 0); }
		public CorridorProjectionAxisLabel2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_corridorProjectionAxisLabel2; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCorridorProjectionAxisLabel2(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CorridorProjectionAxisLabel2Context corridorProjectionAxisLabel2() throws RecognitionException {
		CorridorProjectionAxisLabel2Context _localctx = new CorridorProjectionAxisLabel2Context(_ctx, getState());
		enterRule(_localctx, 148, RULE_corridorProjectionAxisLabel2);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(947); match(COVERAGE_VARIABLE_NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClipCorridorExpressionContext extends ParserRuleContext {
		public ClipCorridorExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_clipCorridorExpression; }
	 
		public ClipCorridorExpressionContext() { }
		public void copyFrom(ClipCorridorExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ClipCorridorExpressionLabelContext extends ClipCorridorExpressionContext {
		public TerminalNode CLIP() { return getToken(wcpsParser.CLIP, 0); }
		public TerminalNode LEFT_PARENTHESIS(int i) {
			return getToken(wcpsParser.LEFT_PARENTHESIS, i);
		}
		public WktExpressionContext wktExpression() {
			return getRuleContext(WktExpressionContext.class,0);
		}
		public WktLineStringContext wktLineString() {
			return getRuleContext(WktLineStringContext.class,0);
		}
		public TerminalNode CORRIDOR() { return getToken(wcpsParser.CORRIDOR, 0); }
		public CorridorProjectionAxisLabel2Context corridorProjectionAxisLabel2() {
			return getRuleContext(CorridorProjectionAxisLabel2Context.class,0);
		}
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public CorridorProjectionAxisLabel1Context corridorProjectionAxisLabel1() {
			return getRuleContext(CorridorProjectionAxisLabel1Context.class,0);
		}
		public CrsNameContext crsName() {
			return getRuleContext(CrsNameContext.class,0);
		}
		public List<TerminalNode> LEFT_PARENTHESIS() { return getTokens(wcpsParser.LEFT_PARENTHESIS); }
		public TerminalNode RIGHT_PARENTHESIS(int i) {
			return getToken(wcpsParser.RIGHT_PARENTHESIS, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<TerminalNode> RIGHT_PARENTHESIS() { return getTokens(wcpsParser.RIGHT_PARENTHESIS); }
		public TerminalNode PROJECTION() { return getToken(wcpsParser.PROJECTION, 0); }
		public TerminalNode DISCRETE() { return getToken(wcpsParser.DISCRETE, 0); }
		public ClipCorridorExpressionLabelContext(ClipCorridorExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitClipCorridorExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClipCorridorExpressionContext clipCorridorExpression() throws RecognitionException {
		ClipCorridorExpressionContext _localctx = new ClipCorridorExpressionContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_clipCorridorExpression);
		int _la;
		try {
			_localctx = new ClipCorridorExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(949); match(CLIP);
			setState(950); match(LEFT_PARENTHESIS);
			setState(951); coverageExpression(0);
			setState(952); match(COMMA);
			setState(953); match(CORRIDOR);
			setState(954); match(LEFT_PARENTHESIS);
			setState(955); match(PROJECTION);
			setState(956); match(LEFT_PARENTHESIS);
			setState(957); corridorProjectionAxisLabel1();
			setState(958); match(COMMA);
			setState(959); corridorProjectionAxisLabel2();
			setState(960); match(RIGHT_PARENTHESIS);
			setState(961); match(COMMA);
			setState(962); wktLineString();
			setState(963); match(COMMA);
			setState(964); wktExpression();
			setState(967);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(965); match(COMMA);
				setState(966); match(DISCRETE);
				}
			}

			setState(969); match(RIGHT_PARENTHESIS);
			setState(972);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(970); match(COMMA);
				setState(971); crsName();
				}
			}

			setState(974); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ClipWKTExpressionContext extends ParserRuleContext {
		public ClipWKTExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_clipWKTExpression; }
	 
		public ClipWKTExpressionContext() { }
		public void copyFrom(ClipWKTExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ClipWKTExpressionLabelContext extends ClipWKTExpressionContext {
		public TerminalNode CLIP() { return getToken(wcpsParser.CLIP, 0); }
		public CrsNameContext crsName() {
			return getRuleContext(CrsNameContext.class,0);
		}
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public WktExpressionContext wktExpression() {
			return getRuleContext(WktExpressionContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode WITH_COORDINATES() { return getToken(wcpsParser.WITH_COORDINATES, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public ClipWKTExpressionLabelContext(ClipWKTExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitClipWKTExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ClipWKTExpressionContext clipWKTExpression() throws RecognitionException {
		ClipWKTExpressionContext _localctx = new ClipWKTExpressionContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_clipWKTExpression);
		int _la;
		try {
			_localctx = new ClipWKTExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(976); match(CLIP);
			setState(977); match(LEFT_PARENTHESIS);
			setState(978); coverageExpression(0);
			setState(979); match(COMMA);
			setState(980); wktExpression();
			setState(983);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(981); match(COMMA);
				setState(982); crsName();
				}
			}

			setState(985); match(RIGHT_PARENTHESIS);
			setState(987);
			switch ( getInterpreter().adaptivePredict(_input,66,_ctx) ) {
			case 1:
				{
				setState(986); match(WITH_COORDINATES);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CrsTransformExpressionContext extends ParserRuleContext {
		public CrsTransformExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_crsTransformExpression; }
	 
		public CrsTransformExpressionContext() { }
		public void copyFrom(CrsTransformExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CrsTransformExpressionLabelContext extends CrsTransformExpressionContext {
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode CRS_TRANSFORM() { return getToken(wcpsParser.CRS_TRANSFORM, 0); }
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public DimensionCrsListContext dimensionCrsList() {
			return getRuleContext(DimensionCrsListContext.class,0);
		}
		public InterpolationTypeContext interpolationType() {
			return getRuleContext(InterpolationTypeContext.class,0);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public CrsTransformExpressionLabelContext(CrsTransformExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCrsTransformExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CrsTransformExpressionContext crsTransformExpression() throws RecognitionException {
		CrsTransformExpressionContext _localctx = new CrsTransformExpressionContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_crsTransformExpression);
		int _la;
		try {
			_localctx = new CrsTransformExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(989); match(CRS_TRANSFORM);
			setState(990); match(LEFT_PARENTHESIS);
			setState(991); coverageExpression(0);
			setState(992); match(COMMA);
			setState(993); dimensionCrsList();
			setState(1000);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(994); match(COMMA);
				setState(995); match(LEFT_BRACE);
				setState(997);
				_la = _input.LA(1);
				if (_la==COVERAGE_VARIABLE_NAME) {
					{
					setState(996); interpolationType();
					}
				}

				setState(999); match(RIGHT_BRACE);
				}
			}

			setState(1002); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DimensionCrsListContext extends ParserRuleContext {
		public DimensionCrsListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dimensionCrsList; }
	 
		public DimensionCrsListContext() { }
		public void copyFrom(DimensionCrsListContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class DimensionCrsListLabelContext extends DimensionCrsListContext {
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public DimensionCrsElementContext dimensionCrsElement(int i) {
			return getRuleContext(DimensionCrsElementContext.class,i);
		}
		public List<DimensionCrsElementContext> dimensionCrsElement() {
			return getRuleContexts(DimensionCrsElementContext.class);
		}
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public DimensionCrsListLabelContext(DimensionCrsListContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitDimensionCrsListLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DimensionCrsListContext dimensionCrsList() throws RecognitionException {
		DimensionCrsListContext _localctx = new DimensionCrsListContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_dimensionCrsList);
		int _la;
		try {
			_localctx = new DimensionCrsListLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1004); match(LEFT_BRACE);
			setState(1005); dimensionCrsElement();
			setState(1010);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1006); match(COMMA);
				setState(1007); dimensionCrsElement();
				}
				}
				setState(1012);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1013); match(RIGHT_BRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DimensionCrsElementContext extends ParserRuleContext {
		public DimensionCrsElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dimensionCrsElement; }
	 
		public DimensionCrsElementContext() { }
		public void copyFrom(DimensionCrsElementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class DimensionCrsElementLabelContext extends DimensionCrsElementContext {
		public CrsNameContext crsName() {
			return getRuleContext(CrsNameContext.class,0);
		}
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode COLON() { return getToken(wcpsParser.COLON, 0); }
		public DimensionCrsElementLabelContext(DimensionCrsElementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitDimensionCrsElementLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DimensionCrsElementContext dimensionCrsElement() throws RecognitionException {
		DimensionCrsElementContext _localctx = new DimensionCrsElementContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_dimensionCrsElement);
		try {
			_localctx = new DimensionCrsElementLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1015); axisName();
			setState(1016); match(COLON);
			setState(1017); crsName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InterpolationTypeContext extends ParserRuleContext {
		public InterpolationTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interpolationType; }
	 
		public InterpolationTypeContext() { }
		public void copyFrom(InterpolationTypeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class InterpolationTypeLabelContext extends InterpolationTypeContext {
		public TerminalNode COVERAGE_VARIABLE_NAME() { return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, 0); }
		public InterpolationTypeLabelContext(InterpolationTypeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitInterpolationTypeLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterpolationTypeContext interpolationType() throws RecognitionException {
		InterpolationTypeContext _localctx = new InterpolationTypeContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_interpolationType);
		try {
			_localctx = new InterpolationTypeLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1019); match(COVERAGE_VARIABLE_NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CoverageConstructorExpressionContext extends ParserRuleContext {
		public CoverageConstructorExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coverageConstructorExpression; }
	 
		public CoverageConstructorExpressionContext() { }
		public void copyFrom(CoverageConstructorExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CoverageConstructorExpressionLabelContext extends CoverageConstructorExpressionContext {
		public TerminalNode OVER() { return getToken(wcpsParser.OVER, 0); }
		public AxisIteratorContext axisIterator(int i) {
			return getRuleContext(AxisIteratorContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<AxisIteratorContext> axisIterator() {
			return getRuleContexts(AxisIteratorContext.class);
		}
		public TerminalNode COVERAGE() { return getToken(wcpsParser.COVERAGE, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode COVERAGE_VARIABLE_NAME() { return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, 0); }
		public TerminalNode VALUES() { return getToken(wcpsParser.VALUES, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public CoverageConstructorExpressionLabelContext(CoverageConstructorExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageConstructorExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoverageConstructorExpressionContext coverageConstructorExpression() throws RecognitionException {
		CoverageConstructorExpressionContext _localctx = new CoverageConstructorExpressionContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_coverageConstructorExpression);
		int _la;
		try {
			_localctx = new CoverageConstructorExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1021); match(COVERAGE);
			setState(1022); match(COVERAGE_VARIABLE_NAME);
			setState(1023); match(OVER);
			setState(1024); axisIterator();
			setState(1029);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1025); match(COMMA);
				setState(1026); axisIterator();
				}
				}
				setState(1031);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1032); match(VALUES);
			setState(1033); coverageExpression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AxisIteratorContext extends ParserRuleContext {
		public AxisIteratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_axisIterator; }
	 
		public AxisIteratorContext() { }
		public void copyFrom(AxisIteratorContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class AxisIteratorDomainIntervalsLabelContext extends AxisIteratorContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public DomainIntervalsContext domainIntervals() {
			return getRuleContext(DomainIntervalsContext.class,0);
		}
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageVariableNameContext coverageVariableName() {
			return getRuleContext(CoverageVariableNameContext.class,0);
		}
		public AxisIteratorDomainIntervalsLabelContext(AxisIteratorContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitAxisIteratorDomainIntervalsLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AxisIteratorLabelContext extends AxisIteratorContext {
		public DimensionIntervalElementContext dimensionIntervalElement() {
			return getRuleContext(DimensionIntervalElementContext.class,0);
		}
		public CoverageVariableNameContext coverageVariableName() {
			return getRuleContext(CoverageVariableNameContext.class,0);
		}
		public AxisIteratorLabelContext(AxisIteratorContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitAxisIteratorLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AxisIteratorContext axisIterator() throws RecognitionException {
		AxisIteratorContext _localctx = new AxisIteratorContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_axisIterator);
		try {
			setState(1044);
			switch ( getInterpreter().adaptivePredict(_input,71,_ctx) ) {
			case 1:
				_localctx = new AxisIteratorDomainIntervalsLabelContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1035); coverageVariableName();
				setState(1036); axisName();
				setState(1037); match(LEFT_PARENTHESIS);
				setState(1038); domainIntervals();
				setState(1039); match(RIGHT_PARENTHESIS);
				}
				break;

			case 2:
				_localctx = new AxisIteratorLabelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1041); coverageVariableName();
				setState(1042); dimensionIntervalElement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntervalExpressionContext extends ParserRuleContext {
		public IntervalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intervalExpression; }
	 
		public IntervalExpressionContext() { }
		public void copyFrom(IntervalExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class IntervalExpressionLabelContext extends IntervalExpressionContext {
		public ScalarExpressionContext scalarExpression(int i) {
			return getRuleContext(ScalarExpressionContext.class,i);
		}
		public TerminalNode COLON() { return getToken(wcpsParser.COLON, 0); }
		public List<ScalarExpressionContext> scalarExpression() {
			return getRuleContexts(ScalarExpressionContext.class);
		}
		public IntervalExpressionLabelContext(IntervalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitIntervalExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalExpressionContext intervalExpression() throws RecognitionException {
		IntervalExpressionContext _localctx = new IntervalExpressionContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_intervalExpression);
		try {
			_localctx = new IntervalExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1046); scalarExpression();
			setState(1047); match(COLON);
			setState(1048); scalarExpression();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CoverageConstantExpressionContext extends ParserRuleContext {
		public CoverageConstantExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coverageConstantExpression; }
	 
		public CoverageConstantExpressionContext() { }
		public void copyFrom(CoverageConstantExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CoverageConstantExpressionLabelContext extends CoverageConstantExpressionContext {
		public ConstantContext constant(int i) {
			return getRuleContext(ConstantContext.class,i);
		}
		public List<TerminalNode> SEMICOLON() { return getTokens(wcpsParser.SEMICOLON); }
		public TerminalNode SEMICOLON(int i) {
			return getToken(wcpsParser.SEMICOLON, i);
		}
		public AxisIteratorContext axisIterator(int i) {
			return getRuleContext(AxisIteratorContext.class,i);
		}
		public TerminalNode GREATER_THAN() { return getToken(wcpsParser.GREATER_THAN, 0); }
		public List<AxisIteratorContext> axisIterator() {
			return getRuleContexts(AxisIteratorContext.class);
		}
		public TerminalNode LOWER_THAN() { return getToken(wcpsParser.LOWER_THAN, 0); }
		public TerminalNode LIST() { return getToken(wcpsParser.LIST, 0); }
		public List<ConstantContext> constant() {
			return getRuleContexts(ConstantContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public TerminalNode OVER() { return getToken(wcpsParser.OVER, 0); }
		public TerminalNode VALUE() { return getToken(wcpsParser.VALUE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode COVERAGE() { return getToken(wcpsParser.COVERAGE, 0); }
		public TerminalNode COVERAGE_VARIABLE_NAME() { return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, 0); }
		public CoverageConstantExpressionLabelContext(CoverageConstantExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageConstantExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoverageConstantExpressionContext coverageConstantExpression() throws RecognitionException {
		CoverageConstantExpressionContext _localctx = new CoverageConstantExpressionContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_coverageConstantExpression);
		int _la;
		try {
			_localctx = new CoverageConstantExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1050); match(COVERAGE);
			setState(1051); match(COVERAGE_VARIABLE_NAME);
			setState(1052); match(OVER);
			setState(1053); axisIterator();
			setState(1058);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1054); match(COMMA);
				setState(1055); axisIterator();
				}
				}
				setState(1060);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1061); match(VALUE);
			setState(1062); match(LIST);
			setState(1063); match(LOWER_THAN);
			setState(1064); constant();
			setState(1069);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMICOLON) {
				{
				{
				setState(1065); match(SEMICOLON);
				setState(1066); constant();
				}
				}
				setState(1071);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1072); match(GREATER_THAN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AxisSpecContext extends ParserRuleContext {
		public AxisSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_axisSpec; }
	 
		public AxisSpecContext() { }
		public void copyFrom(AxisSpecContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class AxisSpecLabelContext extends AxisSpecContext {
		public DimensionIntervalElementContext dimensionIntervalElement() {
			return getRuleContext(DimensionIntervalElementContext.class,0);
		}
		public AxisSpecLabelContext(AxisSpecContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitAxisSpecLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AxisSpecContext axisSpec() throws RecognitionException {
		AxisSpecContext _localctx = new AxisSpecContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_axisSpec);
		try {
			_localctx = new AxisSpecLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1074); dimensionIntervalElement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CondenseExpressionContext extends ParserRuleContext {
		public ReduceExpressionContext reduceExpression() {
			return getRuleContext(ReduceExpressionContext.class,0);
		}
		public GeneralCondenseExpressionContext generalCondenseExpression() {
			return getRuleContext(GeneralCondenseExpressionContext.class,0);
		}
		public CondenseExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condenseExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCondenseExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CondenseExpressionContext condenseExpression() throws RecognitionException {
		CondenseExpressionContext _localctx = new CondenseExpressionContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_condenseExpression);
		try {
			setState(1078);
			switch (_input.LA(1)) {
			case ADD:
			case ALL:
			case AVG:
			case COUNT:
			case MAX:
			case MIN:
			case SOME:
				enterOuterAlt(_localctx, 1);
				{
				setState(1076); reduceExpression();
				}
				break;
			case CONDENSE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1077); generalCondenseExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReduceBooleanExpressionOperatorContext extends ParserRuleContext {
		public TerminalNode SOME() { return getToken(wcpsParser.SOME, 0); }
		public TerminalNode ALL() { return getToken(wcpsParser.ALL, 0); }
		public ReduceBooleanExpressionOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reduceBooleanExpressionOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitReduceBooleanExpressionOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReduceBooleanExpressionOperatorContext reduceBooleanExpressionOperator() throws RecognitionException {
		ReduceBooleanExpressionOperatorContext _localctx = new ReduceBooleanExpressionOperatorContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_reduceBooleanExpressionOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1080);
			_la = _input.LA(1);
			if ( !(_la==ALL || _la==SOME) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReduceNumericalExpressionOperatorContext extends ParserRuleContext {
		public TerminalNode COUNT() { return getToken(wcpsParser.COUNT, 0); }
		public TerminalNode AVG() { return getToken(wcpsParser.AVG, 0); }
		public TerminalNode MIN() { return getToken(wcpsParser.MIN, 0); }
		public TerminalNode MAX() { return getToken(wcpsParser.MAX, 0); }
		public TerminalNode ADD() { return getToken(wcpsParser.ADD, 0); }
		public ReduceNumericalExpressionOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reduceNumericalExpressionOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitReduceNumericalExpressionOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReduceNumericalExpressionOperatorContext reduceNumericalExpressionOperator() throws RecognitionException {
		ReduceNumericalExpressionOperatorContext _localctx = new ReduceNumericalExpressionOperatorContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_reduceNumericalExpressionOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1082);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ADD) | (1L << AVG) | (1L << COUNT) | (1L << MAX) | (1L << MIN))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReduceBooleanExpressionContext extends ParserRuleContext {
		public ReduceBooleanExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reduceBooleanExpression; }
	 
		public ReduceBooleanExpressionContext() { }
		public void copyFrom(ReduceBooleanExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ReduceBooleanExpressionLabelContext extends ReduceBooleanExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public ReduceBooleanExpressionOperatorContext reduceBooleanExpressionOperator() {
			return getRuleContext(ReduceBooleanExpressionOperatorContext.class,0);
		}
		public ReduceBooleanExpressionLabelContext(ReduceBooleanExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitReduceBooleanExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReduceBooleanExpressionContext reduceBooleanExpression() throws RecognitionException {
		ReduceBooleanExpressionContext _localctx = new ReduceBooleanExpressionContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_reduceBooleanExpression);
		try {
			_localctx = new ReduceBooleanExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1084); reduceBooleanExpressionOperator();
			setState(1085); match(LEFT_PARENTHESIS);
			setState(1086); coverageExpression(0);
			setState(1087); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReduceNumericalExpressionContext extends ParserRuleContext {
		public ReduceNumericalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reduceNumericalExpression; }
	 
		public ReduceNumericalExpressionContext() { }
		public void copyFrom(ReduceNumericalExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ReduceNumericalExpressionLabelContext extends ReduceNumericalExpressionContext {
		public TerminalNode LEFT_PARENTHESIS() { return getToken(wcpsParser.LEFT_PARENTHESIS, 0); }
		public ReduceNumericalExpressionOperatorContext reduceNumericalExpressionOperator() {
			return getRuleContext(ReduceNumericalExpressionOperatorContext.class,0);
		}
		public TerminalNode RIGHT_PARENTHESIS() { return getToken(wcpsParser.RIGHT_PARENTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public ReduceNumericalExpressionLabelContext(ReduceNumericalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitReduceNumericalExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReduceNumericalExpressionContext reduceNumericalExpression() throws RecognitionException {
		ReduceNumericalExpressionContext _localctx = new ReduceNumericalExpressionContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_reduceNumericalExpression);
		try {
			_localctx = new ReduceNumericalExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1089); reduceNumericalExpressionOperator();
			setState(1090); match(LEFT_PARENTHESIS);
			setState(1091); coverageExpression(0);
			setState(1092); match(RIGHT_PARENTHESIS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReduceExpressionContext extends ParserRuleContext {
		public ReduceBooleanExpressionContext reduceBooleanExpression() {
			return getRuleContext(ReduceBooleanExpressionContext.class,0);
		}
		public ReduceNumericalExpressionContext reduceNumericalExpression() {
			return getRuleContext(ReduceNumericalExpressionContext.class,0);
		}
		public ReduceExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reduceExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitReduceExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReduceExpressionContext reduceExpression() throws RecognitionException {
		ReduceExpressionContext _localctx = new ReduceExpressionContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_reduceExpression);
		try {
			setState(1096);
			switch (_input.LA(1)) {
			case ALL:
			case SOME:
				enterOuterAlt(_localctx, 1);
				{
				setState(1094); reduceBooleanExpression();
				}
				break;
			case ADD:
			case AVG:
			case COUNT:
			case MAX:
			case MIN:
				enterOuterAlt(_localctx, 2);
				{
				setState(1095); reduceNumericalExpression();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CondenseExpressionOperatorContext extends ParserRuleContext {
		public TerminalNode MULTIPLICATION() { return getToken(wcpsParser.MULTIPLICATION, 0); }
		public TerminalNode AND() { return getToken(wcpsParser.AND, 0); }
		public TerminalNode OR() { return getToken(wcpsParser.OR, 0); }
		public TerminalNode MIN() { return getToken(wcpsParser.MIN, 0); }
		public TerminalNode MAX() { return getToken(wcpsParser.MAX, 0); }
		public TerminalNode PLUS() { return getToken(wcpsParser.PLUS, 0); }
		public CondenseExpressionOperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condenseExpressionOperator; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCondenseExpressionOperator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CondenseExpressionOperatorContext condenseExpressionOperator() throws RecognitionException {
		CondenseExpressionOperatorContext _localctx = new CondenseExpressionOperatorContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_condenseExpressionOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1098);
			_la = _input.LA(1);
			if ( !(((((_la - 5)) & ~0x3f) == 0 && ((1L << (_la - 5)) & ((1L << (AND - 5)) | (1L << (MAX - 5)) | (1L << (MIN - 5)) | (1L << (MULTIPLICATION - 5)) | (1L << (OR - 5)) | (1L << (PLUS - 5)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GeneralCondenseExpressionContext extends ParserRuleContext {
		public GeneralCondenseExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_generalCondenseExpression; }
	 
		public GeneralCondenseExpressionContext() { }
		public void copyFrom(GeneralCondenseExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class GeneralCondenseExpressionLabelContext extends GeneralCondenseExpressionContext {
		public TerminalNode OVER() { return getToken(wcpsParser.OVER, 0); }
		public TerminalNode CONDENSE() { return getToken(wcpsParser.CONDENSE, 0); }
		public AxisIteratorContext axisIterator(int i) {
			return getRuleContext(AxisIteratorContext.class,i);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode USING() { return getToken(wcpsParser.USING, 0); }
		public List<AxisIteratorContext> axisIterator() {
			return getRuleContexts(AxisIteratorContext.class);
		}
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public CondenseExpressionOperatorContext condenseExpressionOperator() {
			return getRuleContext(CondenseExpressionOperatorContext.class,0);
		}
		public GeneralCondenseExpressionLabelContext(GeneralCondenseExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitGeneralCondenseExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GeneralCondenseExpressionContext generalCondenseExpression() throws RecognitionException {
		GeneralCondenseExpressionContext _localctx = new GeneralCondenseExpressionContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_generalCondenseExpression);
		int _la;
		try {
			_localctx = new GeneralCondenseExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(1100); match(CONDENSE);
			setState(1101); condenseExpressionOperator();
			setState(1102); match(OVER);
			setState(1103); axisIterator();
			setState(1108);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1104); match(COMMA);
				setState(1105); axisIterator();
				}
				}
				setState(1110);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1112);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(1111); whereClause();
				}
			}

			setState(1114); match(USING);
			setState(1115); coverageExpression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SwitchCaseExpressionContext extends ParserRuleContext {
		public SwitchCaseExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchCaseExpression; }
	 
		public SwitchCaseExpressionContext() { }
		public void copyFrom(SwitchCaseExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class SwitchCaseRangeConstructorExpressionLabelContext extends SwitchCaseExpressionContext {
		public TerminalNode LEFT_PARENTHESIS(int i) {
			return getToken(wcpsParser.LEFT_PARENTHESIS, i);
		}
		public TerminalNode DEFAULT() { return getToken(wcpsParser.DEFAULT, 0); }
		public List<TerminalNode> LEFT_PARENTHESIS() { return getTokens(wcpsParser.LEFT_PARENTHESIS); }
		public TerminalNode RETURN(int i) {
			return getToken(wcpsParser.RETURN, i);
		}
		public TerminalNode RIGHT_PARENTHESIS(int i) {
			return getToken(wcpsParser.RIGHT_PARENTHESIS, i);
		}
		public List<TerminalNode> RIGHT_PARENTHESIS() { return getTokens(wcpsParser.RIGHT_PARENTHESIS); }
		public List<RangeConstructorSwitchCaseExpressionContext> rangeConstructorSwitchCaseExpression() {
			return getRuleContexts(RangeConstructorSwitchCaseExpressionContext.class);
		}
		public List<BooleanSwitchCaseCombinedExpressionContext> booleanSwitchCaseCombinedExpression() {
			return getRuleContexts(BooleanSwitchCaseCombinedExpressionContext.class);
		}
		public TerminalNode CASE(int i) {
			return getToken(wcpsParser.CASE, i);
		}
		public List<TerminalNode> RETURN() { return getTokens(wcpsParser.RETURN); }
		public List<TerminalNode> CASE() { return getTokens(wcpsParser.CASE); }
		public TerminalNode SWITCH() { return getToken(wcpsParser.SWITCH, 0); }
		public RangeConstructorSwitchCaseExpressionContext rangeConstructorSwitchCaseExpression(int i) {
			return getRuleContext(RangeConstructorSwitchCaseExpressionContext.class,i);
		}
		public BooleanSwitchCaseCombinedExpressionContext booleanSwitchCaseCombinedExpression(int i) {
			return getRuleContext(BooleanSwitchCaseCombinedExpressionContext.class,i);
		}
		public SwitchCaseRangeConstructorExpressionLabelContext(SwitchCaseExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitSwitchCaseRangeConstructorExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SwitchCaseScalarValueExpressionLabelContext extends SwitchCaseExpressionContext {
		public TerminalNode LEFT_PARENTHESIS(int i) {
			return getToken(wcpsParser.LEFT_PARENTHESIS, i);
		}
		public TerminalNode DEFAULT() { return getToken(wcpsParser.DEFAULT, 0); }
		public List<TerminalNode> LEFT_PARENTHESIS() { return getTokens(wcpsParser.LEFT_PARENTHESIS); }
		public TerminalNode RETURN(int i) {
			return getToken(wcpsParser.RETURN, i);
		}
		public TerminalNode RIGHT_PARENTHESIS(int i) {
			return getToken(wcpsParser.RIGHT_PARENTHESIS, i);
		}
		public List<TerminalNode> RIGHT_PARENTHESIS() { return getTokens(wcpsParser.RIGHT_PARENTHESIS); }
		public List<BooleanSwitchCaseCombinedExpressionContext> booleanSwitchCaseCombinedExpression() {
			return getRuleContexts(BooleanSwitchCaseCombinedExpressionContext.class);
		}
		public TerminalNode CASE(int i) {
			return getToken(wcpsParser.CASE, i);
		}
		public ScalarValueCoverageExpressionContext scalarValueCoverageExpression(int i) {
			return getRuleContext(ScalarValueCoverageExpressionContext.class,i);
		}
		public List<TerminalNode> RETURN() { return getTokens(wcpsParser.RETURN); }
		public List<TerminalNode> CASE() { return getTokens(wcpsParser.CASE); }
		public TerminalNode SWITCH() { return getToken(wcpsParser.SWITCH, 0); }
		public List<ScalarValueCoverageExpressionContext> scalarValueCoverageExpression() {
			return getRuleContexts(ScalarValueCoverageExpressionContext.class);
		}
		public BooleanSwitchCaseCombinedExpressionContext booleanSwitchCaseCombinedExpression(int i) {
			return getRuleContext(BooleanSwitchCaseCombinedExpressionContext.class,i);
		}
		public SwitchCaseScalarValueExpressionLabelContext(SwitchCaseExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitSwitchCaseScalarValueExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SwitchCaseExpressionContext switchCaseExpression() throws RecognitionException {
		SwitchCaseExpressionContext _localctx = new SwitchCaseExpressionContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_switchCaseExpression);
		int _la;
		try {
			int _alt;
			setState(1203);
			switch ( getInterpreter().adaptivePredict(_input,88,_ctx) ) {
			case 1:
				_localctx = new SwitchCaseRangeConstructorExpressionLabelContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(1117); match(SWITCH);
				setState(1118); match(CASE);
				setState(1122);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,78,_ctx);
				while ( _alt!=2 && _alt!=-1 ) {
					if ( _alt==1 ) {
						{
						{
						setState(1119); match(LEFT_PARENTHESIS);
						}
						} 
					}
					setState(1124);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,78,_ctx);
				}
				setState(1125); booleanSwitchCaseCombinedExpression(0);
				setState(1129);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==RIGHT_PARENTHESIS) {
					{
					{
					setState(1126); match(RIGHT_PARENTHESIS);
					}
					}
					setState(1131);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1132); match(RETURN);
				setState(1133); rangeConstructorSwitchCaseExpression();
				setState(1153);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CASE) {
					{
					{
					setState(1134); match(CASE);
					setState(1138);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
					while ( _alt!=2 && _alt!=-1 ) {
						if ( _alt==1 ) {
							{
							{
							setState(1135); match(LEFT_PARENTHESIS);
							}
							} 
						}
						setState(1140);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,80,_ctx);
					}
					setState(1141); booleanSwitchCaseCombinedExpression(0);
					setState(1145);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==RIGHT_PARENTHESIS) {
						{
						{
						setState(1142); match(RIGHT_PARENTHESIS);
						}
						}
						setState(1147);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(1148); match(RETURN);
					setState(1149); rangeConstructorSwitchCaseExpression();
					}
					}
					setState(1155);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1156); match(DEFAULT);
				setState(1157); match(RETURN);
				setState(1158); rangeConstructorSwitchCaseExpression();
				}
				break;

			case 2:
				_localctx = new SwitchCaseScalarValueExpressionLabelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(1160); match(SWITCH);
				setState(1161); match(CASE);
				setState(1165);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
				while ( _alt!=2 && _alt!=-1 ) {
					if ( _alt==1 ) {
						{
						{
						setState(1162); match(LEFT_PARENTHESIS);
						}
						} 
					}
					setState(1167);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,83,_ctx);
				}
				setState(1168); booleanSwitchCaseCombinedExpression(0);
				setState(1172);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==RIGHT_PARENTHESIS) {
					{
					{
					setState(1169); match(RIGHT_PARENTHESIS);
					}
					}
					setState(1174);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1175); match(RETURN);
				setState(1176); scalarValueCoverageExpression();
				setState(1196);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==CASE) {
					{
					{
					setState(1177); match(CASE);
					setState(1181);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,85,_ctx);
					while ( _alt!=2 && _alt!=-1 ) {
						if ( _alt==1 ) {
							{
							{
							setState(1178); match(LEFT_PARENTHESIS);
							}
							} 
						}
						setState(1183);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,85,_ctx);
					}
					setState(1184); booleanSwitchCaseCombinedExpression(0);
					setState(1188);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==RIGHT_PARENTHESIS) {
						{
						{
						setState(1185); match(RIGHT_PARENTHESIS);
						}
						}
						setState(1190);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(1191); match(RETURN);
					setState(1192); scalarValueCoverageExpression();
					}
					}
					setState(1198);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(1199); match(DEFAULT);
				setState(1200); match(RETURN);
				setState(1201); scalarValueCoverageExpression();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CrsNameContext extends ParserRuleContext {
		public TerminalNode STRING_LITERAL() { return getToken(wcpsParser.STRING_LITERAL, 0); }
		public CrsNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_crsName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCrsName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CrsNameContext crsName() throws RecognitionException {
		CrsNameContext _localctx = new CrsNameContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_crsName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1205); match(STRING_LITERAL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AxisNameContext extends ParserRuleContext {
		public TerminalNode COVERAGE_VARIABLE_NAME() { return getToken(wcpsParser.COVERAGE_VARIABLE_NAME, 0); }
		public AxisNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_axisName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitAxisName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AxisNameContext axisName() throws RecognitionException {
		AxisNameContext _localctx = new AxisNameContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_axisName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1207); match(COVERAGE_VARIABLE_NAME);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumberContext extends ParserRuleContext {
		public TerminalNode REAL_NUMBER_CONSTANT() { return getToken(wcpsParser.REAL_NUMBER_CONSTANT, 0); }
		public TerminalNode MINUS() { return getToken(wcpsParser.MINUS, 0); }
		public TerminalNode SCIENTIFIC_NUMBER_CONSTANT() { return getToken(wcpsParser.SCIENTIFIC_NUMBER_CONSTANT, 0); }
		public TerminalNode INTEGER() { return getToken(wcpsParser.INTEGER, 0); }
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumber(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_number);
		int _la;
		try {
			setState(1221);
			switch ( getInterpreter().adaptivePredict(_input,92,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1210);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(1209); match(MINUS);
					}
				}

				setState(1212); match(INTEGER);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1214);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(1213); match(MINUS);
					}
				}

				setState(1216); match(REAL_NUMBER_CONSTANT);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1218);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(1217); match(MINUS);
					}
				}

				setState(1220); match(SCIENTIFIC_NUMBER_CONSTANT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstantContext extends ParserRuleContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public TerminalNode TRUE() { return getToken(wcpsParser.TRUE, 0); }
		public TerminalNode MINUS() { return getToken(wcpsParser.MINUS, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(wcpsParser.STRING_LITERAL, 0); }
		public ComplexNumberConstantContext complexNumberConstant() {
			return getRuleContext(ComplexNumberConstantContext.class,0);
		}
		public TerminalNode FALSE() { return getToken(wcpsParser.FALSE, 0); }
		public ConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_constant);
		try {
			setState(1231);
			switch (_input.LA(1)) {
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(1223); match(STRING_LITERAL);
				}
				break;
			case TRUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1224); match(TRUE);
				}
				break;
			case FALSE:
				enterOuterAlt(_localctx, 3);
				{
				setState(1225); match(FALSE);
				}
				break;
			case MINUS:
			case INTEGER:
			case REAL_NUMBER_CONSTANT:
			case SCIENTIFIC_NUMBER_CONSTANT:
				enterOuterAlt(_localctx, 4);
				{
				setState(1227);
				switch ( getInterpreter().adaptivePredict(_input,93,_ctx) ) {
				case 1:
					{
					setState(1226); match(MINUS);
					}
					break;
				}
				setState(1229); number();
				}
				break;
			case LEFT_PARENTHESIS:
				enterOuterAlt(_localctx, 5);
				{
				setState(1230); complexNumberConstant();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 13: return booleanScalarExpression_sempred((BooleanScalarExpressionContext)_localctx, predIndex);

		case 22: return booleanSwitchCaseCombinedExpression_sempred((BooleanSwitchCaseCombinedExpressionContext)_localctx, predIndex);

		case 23: return numericalScalarExpression_sempred((NumericalScalarExpressionContext)_localctx, predIndex);

		case 42: return coverageExpression_sempred((CoverageExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean coverageExpression_sempred(CoverageExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3: return 1 >= _localctx._p;

		case 4: return 40 >= _localctx._p;

		case 5: return 37 >= _localctx._p;

		case 6: return 36 >= _localctx._p;

		case 7: return 32 >= _localctx._p;

		case 8: return 30 >= _localctx._p;

		case 9: return 29 >= _localctx._p;

		case 10: return 17 >= _localctx._p;

		case 11: return 2 >= _localctx._p;
		}
		return true;
	}
	private boolean booleanScalarExpression_sempred(BooleanScalarExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0: return 4 >= _localctx._p;
		}
		return true;
	}
	private boolean numericalScalarExpression_sempred(NumericalScalarExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2: return 5 >= _localctx._p;
		}
		return true;
	}
	private boolean booleanSwitchCaseCombinedExpression_sempred(BooleanSwitchCaseCombinedExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1: return 1 >= _localctx._p;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3q\u04d4\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4"+
		"`\t`\4a\ta\4b\tb\4c\tc\4d\td\3\2\3\2\5\2\u00cb\n\2\3\2\5\2\u00ce\n\2\3"+
		"\2\3\2\3\3\3\3\3\3\3\3\7\3\u00d6\n\3\f\3\16\3\u00d9\13\3\3\4\3\4\5\4\u00dd"+
		"\n\4\3\5\3\5\3\5\5\5\u00e2\n\5\3\5\3\5\3\5\7\5\u00e7\n\5\f\5\16\5\u00ea"+
		"\13\5\3\5\5\5\u00ed\n\5\3\6\3\6\3\6\3\6\7\6\u00f3\n\6\f\6\16\6\u00f6\13"+
		"\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\5\b\u0105\n\b\3"+
		"\t\3\t\5\t\u0109\n\t\3\t\3\t\5\t\u010d\n\t\3\n\3\n\5\n\u0111\n\n\3\n\3"+
		"\n\5\n\u0115\n\n\3\13\3\13\3\f\3\f\3\f\3\f\3\f\5\f\u011e\n\f\3\r\5\r\u0121"+
		"\n\r\3\r\3\r\5\r\u0125\n\r\3\16\3\16\3\16\3\16\5\16\u012b\n\16\3\17\3"+
		"\17\3\17\3\17\3\17\5\17\u0132\n\17\3\17\3\17\5\17\u0136\n\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\5\17\u0141\n\17\3\17\3\17\3\17\3\17"+
		"\7\17\u0147\n\17\f\17\16\17\u014a\13\17\3\20\3\20\3\21\3\21\3\22\3\22"+
		"\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\7\27\u015b\n\27\f\27\16"+
		"\27\u015e\13\27\3\27\3\27\7\27\u0162\n\27\f\27\16\27\u0165\13\27\3\27"+
		"\3\27\7\27\u0169\n\27\f\27\16\27\u016c\13\27\3\27\3\27\7\27\u0170\n\27"+
		"\f\27\16\27\u0173\13\27\3\30\3\30\3\30\3\30\3\30\3\30\5\30\u017b\n\30"+
		"\3\30\3\30\3\30\3\30\7\30\u0181\n\30\f\30\16\30\u0184\13\30\3\31\3\31"+
		"\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\5\31"+
		"\u0195\n\31\3\31\3\31\3\31\3\31\7\31\u019b\n\31\f\31\16\31\u019e\13\31"+
		"\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36"+
		"\3\36\3\36\3\36\3\36\5\36\u01b2\n\36\3\37\3\37\3\37\3\37\3\37\3 \3 \3"+
		" \3 \3 \3!\3!\3\"\3\"\3\"\3\"\3\"\3\"\3\"\5\"\u01c7\n\"\3\"\3\"\3#\3#"+
		"\3#\3#\3#\3#\3#\3$\3$\3$\3$\3$\3%\3%\3%\3%\3%\3&\3&\3&\3&\3&\3&\3&\5&"+
		"\u01e3\n&\3&\3&\3\'\3\'\3\'\5\'\u01ea\n\'\3\'\5\'\u01ed\n\'\3(\3(\3)\3"+
		")\3*\3*\3*\3*\3*\3*\3*\5*\u01fa\n*\3*\3*\3+\3+\3+\3+\3+\5+\u0203\n+\3"+
		"+\3+\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3"+
		",\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3"+
		",\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3"+
		",\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3"+
		",\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\5"+
		",\u0278\n,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3"+
		",\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\3,\5,\u029e\n,\3,\7,\u02a1"+
		"\n,\f,\16,\u02a4\13,\3-\3-\3.\3.\3/\3/\5/\u02ac\n/\3/\3/\5/\u02b0\n/\3"+
		"\60\3\60\3\60\3\60\3\60\3\61\3\61\3\62\3\62\3\62\3\62\3\62\3\63\3\63\3"+
		"\63\3\63\3\63\3\63\3\63\3\64\3\64\3\64\3\64\3\64\3\64\3\64\3\65\3\65\3"+
		"\65\3\65\3\65\3\65\3\65\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3\66\3"+
		"\66\3\66\3\66\5\66\u02df\n\66\3\67\3\67\7\67\u02e3\n\67\f\67\16\67\u02e6"+
		"\13\67\38\38\38\38\38\39\39\3:\3:\3:\3:\3:\3:\3:\3:\3:\3:\7:\u02f9\n:"+
		"\f:\16:\u02fc\13:\3:\3:\3;\3;\3;\3;\3;\3;\3;\3;\3;\3;\7;\u030a\n;\f;\16"+
		";\u030d\13;\3;\3;\3<\3<\3<\7<\u0314\n<\f<\16<\u0317\13<\3=\3=\3=\5=\u031c"+
		"\n=\3=\3=\3=\3=\3>\3>\3>\7>\u0325\n>\f>\16>\u0328\13>\3?\3?\3?\7?\u032d"+
		"\n?\f?\16?\u0330\13?\3@\3@\3@\3@\5@\u0336\n@\3@\3@\3@\5@\u033b\n@\3@\3"+
		"@\3@\3@\3@\3@\3@\5@\u0344\n@\3A\3A\3A\5A\u0349\nA\3A\3A\3A\3A\3A\3A\3"+
		"A\3A\3A\5A\u0354\nA\3A\3A\3A\3A\5A\u035a\nA\3B\3B\7B\u035e\nB\fB\16B\u0361"+
		"\13B\3B\3B\3B\7B\u0366\nB\fB\16B\u0369\13B\7B\u036b\nB\fB\16B\u036e\13"+
		"B\3C\3C\3C\3C\3C\3C\3C\3C\7C\u0378\nC\fC\16C\u037b\13C\3D\3D\3D\3E\3E"+
		"\3E\3E\3E\3F\3F\3F\3F\3F\3F\3F\3F\3F\3F\7F\u038f\nF\fF\16F\u0392\13F\3"+
		"F\3F\3G\3G\3G\5G\u0399\nG\3H\3H\3I\3I\3J\3J\3J\3J\3J\3J\3J\3J\3J\3J\3"+
		"J\3J\3J\3J\3J\3J\3J\5J\u03b0\nJ\3J\3J\3K\3K\3L\3L\3M\3M\3M\3M\3M\3M\3"+
		"M\3M\3M\3M\3M\3M\3M\3M\3M\3M\3M\3M\5M\u03ca\nM\3M\3M\3M\5M\u03cf\nM\3"+
		"M\3M\3N\3N\3N\3N\3N\3N\3N\5N\u03da\nN\3N\3N\5N\u03de\nN\3O\3O\3O\3O\3"+
		"O\3O\3O\3O\5O\u03e8\nO\3O\5O\u03eb\nO\3O\3O\3P\3P\3P\3P\7P\u03f3\nP\f"+
		"P\16P\u03f6\13P\3P\3P\3Q\3Q\3Q\3Q\3R\3R\3S\3S\3S\3S\3S\3S\7S\u0406\nS"+
		"\fS\16S\u0409\13S\3S\3S\3S\3T\3T\3T\3T\3T\3T\3T\3T\3T\5T\u0417\nT\3U\3"+
		"U\3U\3U\3V\3V\3V\3V\3V\3V\7V\u0423\nV\fV\16V\u0426\13V\3V\3V\3V\3V\3V"+
		"\3V\7V\u042e\nV\fV\16V\u0431\13V\3V\3V\3W\3W\3X\3X\5X\u0439\nX\3Y\3Y\3"+
		"Z\3Z\3[\3[\3[\3[\3[\3\\\3\\\3\\\3\\\3\\\3]\3]\5]\u044b\n]\3^\3^\3_\3_"+
		"\3_\3_\3_\3_\7_\u0455\n_\f_\16_\u0458\13_\3_\5_\u045b\n_\3_\3_\3_\3`\3"+
		"`\3`\7`\u0463\n`\f`\16`\u0466\13`\3`\3`\7`\u046a\n`\f`\16`\u046d\13`\3"+
		"`\3`\3`\3`\7`\u0473\n`\f`\16`\u0476\13`\3`\3`\7`\u047a\n`\f`\16`\u047d"+
		"\13`\3`\3`\3`\7`\u0482\n`\f`\16`\u0485\13`\3`\3`\3`\3`\3`\3`\3`\7`\u048e"+
		"\n`\f`\16`\u0491\13`\3`\3`\7`\u0495\n`\f`\16`\u0498\13`\3`\3`\3`\3`\7"+
		"`\u049e\n`\f`\16`\u04a1\13`\3`\3`\7`\u04a5\n`\f`\16`\u04a8\13`\3`\3`\3"+
		"`\7`\u04ad\n`\f`\16`\u04b0\13`\3`\3`\3`\3`\5`\u04b6\n`\3a\3a\3b\3b\3c"+
		"\5c\u04bd\nc\3c\3c\5c\u04c1\nc\3c\3c\5c\u04c5\nc\3c\5c\u04c8\nc\3d\3d"+
		"\3d\3d\5d\u04ce\nd\3d\3d\5d\u04d2\nd\3d\2e\2\4\6\b\n\f\16\20\22\24\26"+
		"\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|"+
		"~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094\u0096"+
		"\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac\u00ae"+
		"\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u00c2\u00c4\u00c6"+
		"\2\21\4\2$$^^\5\2\7\7AAdd\6\2!!%&\678>>\4\2!!>>\5\2\36\36;<FF\b\2\4\4"+
		"\'\';;FFHIXX\6\2\b\n\22\23TU[\\\4\2\66\66``\4\2ooqq\6\2\4\4\'\'HHXX\5"+
		"\2\"\"\63\63\65\65\4\2jjnn\4\2\6\6WW\6\2\5\5\13\13\24\249:\7\2\7\79:<"+
		"<AAFF\u050a\2\u00c8\3\2\2\2\4\u00d1\3\2\2\2\6\u00dc\3\2\2\2\b\u00de\3"+
		"\2\2\2\n\u00ee\3\2\2\2\f\u00f7\3\2\2\2\16\u0104\3\2\2\2\20\u0106\3\2\2"+
		"\2\22\u010e\3\2\2\2\24\u0116\3\2\2\2\26\u011d\3\2\2\2\30\u0120\3\2\2\2"+
		"\32\u012a\3\2\2\2\34\u0140\3\2\2\2\36\u014b\3\2\2\2 \u014d\3\2\2\2\"\u014f"+
		"\3\2\2\2$\u0151\3\2\2\2&\u0153\3\2\2\2(\u0155\3\2\2\2*\u0157\3\2\2\2,"+
		"\u015c\3\2\2\2.\u017a\3\2\2\2\60\u0194\3\2\2\2\62\u019f\3\2\2\2\64\u01a5"+
		"\3\2\2\2\66\u01a7\3\2\2\28\u01a9\3\2\2\2:\u01b1\3\2\2\2<\u01b3\3\2\2\2"+
		">\u01b8\3\2\2\2@\u01bd\3\2\2\2B\u01bf\3\2\2\2D\u01ca\3\2\2\2F\u01d1\3"+
		"\2\2\2H\u01d6\3\2\2\2J\u01db\3\2\2\2L\u01e9\3\2\2\2N\u01ee\3\2\2\2P\u01f0"+
		"\3\2\2\2R\u01f2\3\2\2\2T\u01fd\3\2\2\2V\u0277\3\2\2\2X\u02a5\3\2\2\2Z"+
		"\u02a7\3\2\2\2\\\u02a9\3\2\2\2^\u02b1\3\2\2\2`\u02b6\3\2\2\2b\u02b8\3"+
		"\2\2\2d\u02bd\3\2\2\2f\u02c4\3\2\2\2h\u02cb\3\2\2\2j\u02de\3\2\2\2l\u02e0"+
		"\3\2\2\2n\u02e7\3\2\2\2p\u02ec\3\2\2\2r\u02ee\3\2\2\2t\u02ff\3\2\2\2v"+
		"\u0310\3\2\2\2x\u0318\3\2\2\2z\u0321\3\2\2\2|\u0329\3\2\2\2~\u0343\3\2"+
		"\2\2\u0080\u0359\3\2\2\2\u0082\u035b\3\2\2\2\u0084\u036f\3\2\2\2\u0086"+
		"\u037c\3\2\2\2\u0088\u037f\3\2\2\2\u008a\u0384\3\2\2\2\u008c\u0398\3\2"+
		"\2\2\u008e\u039a\3\2\2\2\u0090\u039c\3\2\2\2\u0092\u039e\3\2\2\2\u0094"+
		"\u03b3\3\2\2\2\u0096\u03b5\3\2\2\2\u0098\u03b7\3\2\2\2\u009a\u03d2\3\2"+
		"\2\2\u009c\u03df\3\2\2\2\u009e\u03ee\3\2\2\2\u00a0\u03f9\3\2\2\2\u00a2"+
		"\u03fd\3\2\2\2\u00a4\u03ff\3\2\2\2\u00a6\u0416\3\2\2\2\u00a8\u0418\3\2"+
		"\2\2\u00aa\u041c\3\2\2\2\u00ac\u0434\3\2\2\2\u00ae\u0438\3\2\2\2\u00b0"+
		"\u043a\3\2\2\2\u00b2\u043c\3\2\2\2\u00b4\u043e\3\2\2\2\u00b6\u0443\3\2"+
		"\2\2\u00b8\u044a\3\2\2\2\u00ba\u044c\3\2\2\2\u00bc\u044e\3\2\2\2\u00be"+
		"\u04b5\3\2\2\2\u00c0\u04b7\3\2\2\2\u00c2\u04b9\3\2\2\2\u00c4\u04c7\3\2"+
		"\2\2\u00c6\u04d1\3\2\2\2\u00c8\u00ca\5\4\3\2\u00c9\u00cb\5\20\t\2\u00ca"+
		"\u00c9\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\u00cd\3\2\2\2\u00cc\u00ce\5\n"+
		"\6\2\u00cd\u00cc\3\2\2\2\u00cd\u00ce\3\2\2\2\u00ce\u00cf\3\2\2\2\u00cf"+
		"\u00d0\5\22\n\2\u00d0\3\3\2\2\2\u00d1\u00d2\7\3\2\2\u00d2\u00d7\5\b\5"+
		"\2\u00d3\u00d4\7\20\2\2\u00d4\u00d6\5\b\5\2\u00d5\u00d3\3\2\2\2\u00d6"+
		"\u00d9\3\2\2\2\u00d7\u00d5\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8\5\3\2\2\2"+
		"\u00d9\u00d7\3\2\2\2\u00da\u00dd\7n\2\2\u00db\u00dd\5T+\2\u00dc\u00da"+
		"\3\2\2\2\u00dc\u00db\3\2\2\2\u00dd\7\3\2\2\2\u00de\u00df\5\24\13\2\u00df"+
		"\u00e1\7.\2\2\u00e0\u00e2\7\61\2\2\u00e1\u00e0\3\2\2\2\u00e1\u00e2\3\2"+
		"\2\2\u00e2\u00e3\3\2\2\2\u00e3\u00e8\5\6\4\2\u00e4\u00e5\7\20\2\2\u00e5"+
		"\u00e7\5\6\4\2\u00e6\u00e4\3\2\2\2\u00e7\u00ea\3\2\2\2\u00e8\u00e6\3\2"+
		"\2\2\u00e8\u00e9\3\2\2\2\u00e9\u00ec\3\2\2\2\u00ea\u00e8\3\2\2\2\u00eb"+
		"\u00ed\7M\2\2\u00ec\u00eb\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed\t\3\2\2\2"+
		"\u00ee\u00ef\7\62\2\2\u00ef\u00f4\5\16\b\2\u00f0\u00f1\7\20\2\2\u00f1"+
		"\u00f3\5\16\b\2\u00f2\u00f0\3\2\2\2\u00f3\u00f6\3\2\2\2\u00f4\u00f2\3"+
		"\2\2\2\u00f4\u00f5\3\2\2\2\u00f5\13\3\2\2\2\u00f6\u00f4\3\2\2\2\u00f7"+
		"\u00f8\5\24\13\2\u00f8\u00f9\7\17\2\2\u00f9\u00fa\7!\2\2\u00fa\u00fb\7"+
		"\60\2\2\u00fb\u00fc\5z>\2\u00fc\u00fd\7L\2\2\u00fd\r\3\2\2\2\u00fe\u0105"+
		"\5\f\7\2\u00ff\u0100\5\24\13\2\u0100\u0101\7\17\2\2\u0101\u0102\7!\2\2"+
		"\u0102\u0103\5V,\2\u0103\u0105\3\2\2\2\u0104\u00fe\3\2\2\2\u0104\u00ff"+
		"\3\2\2\2\u0105\17\3\2\2\2\u0106\u0108\7c\2\2\u0107\u0109\7\61\2\2\u0108"+
		"\u0107\3\2\2\2\u0108\u0109\3\2\2\2\u0109\u010a\3\2\2\2\u010a\u010c\5V"+
		",\2\u010b\u010d\7M\2\2\u010c\u010b\3\2\2\2\u010c\u010d\3\2\2\2\u010d\21"+
		"\3\2\2\2\u010e\u0110\7J\2\2\u010f\u0111\7\61\2\2\u0110\u010f\3\2\2\2\u0110"+
		"\u0111\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0114\5\26\f\2\u0113\u0115\7"+
		"M\2\2\u0114\u0113\3\2\2\2\u0114\u0115\3\2\2\2\u0115\23\3\2\2\2\u0116\u0117"+
		"\7n\2\2\u0117\25\3\2\2\2\u0118\u011e\5:\36\2\u0119\u011e\5\32\16\2\u011a"+
		"\u011e\5R*\2\u011b\u011e\5\30\r\2\u011c\u011e\5J&\2\u011d\u0118\3\2\2"+
		"\2\u011d\u0119\3\2\2\2\u011d\u011a\3\2\2\2\u011d\u011b\3\2\2\2\u011d\u011c"+
		"\3\2\2\2\u011e\27\3\2\2\2\u011f\u0121\7\61\2\2\u0120\u011f\3\2\2\2\u0120"+
		"\u0121\3\2\2\2\u0121\u0122\3\2\2\2\u0122\u0124\5V,\2\u0123\u0125\7M\2"+
		"\2\u0124\u0123\3\2\2\2\u0124\u0125\3\2\2\2\u0125\31\3\2\2\2\u0126\u012b"+
		"\5\34\17\2\u0127\u012b\5\60\31\2\u0128\u012b\5(\25\2\u0129\u012b\5*\26"+
		"\2\u012a\u0126\3\2\2\2\u012a\u0127\3\2\2\2\u012a\u0128\3\2\2\2\u012a\u0129"+
		"\3\2\2\2\u012b\33\3\2\2\2\u012c\u012d\b\17\1\2\u012d\u0141\5\u00b4[\2"+
		"\u012e\u0141\5 \21\2\u012f\u0131\5\36\20\2\u0130\u0132\7\61\2\2\u0131"+
		"\u0130\3\2\2\2\u0131\u0132\3\2\2\2\u0132\u0133\3\2\2\2\u0133\u0135\5\34"+
		"\17\2\u0134\u0136\7M\2\2\u0135\u0134\3\2\2\2\u0135\u0136\3\2\2\2\u0136"+
		"\u0141\3\2\2\2\u0137\u0138\5\60\31\2\u0138\u0139\5$\23\2\u0139\u013a\5"+
		"\60\31\2\u013a\u0141\3\2\2\2\u013b\u0141\5\u00b4[\2\u013c\u013d\5(\25"+
		"\2\u013d\u013e\5&\24\2\u013e\u013f\5(\25\2\u013f\u0141\3\2\2\2\u0140\u012c"+
		"\3\2\2\2\u0140\u012e\3\2\2\2\u0140\u012f\3\2\2\2\u0140\u0137\3\2\2\2\u0140"+
		"\u013b\3\2\2\2\u0140\u013c\3\2\2\2\u0141\u0148\3\2\2\2\u0142\u0143\6\17"+
		"\2\3\u0143\u0144\5\"\22\2\u0144\u0145\5\34\17\2\u0145\u0147\3\2\2\2\u0146"+
		"\u0142\3\2\2\2\u0147\u014a\3\2\2\2\u0148\u0146\3\2\2\2\u0148\u0149\3\2"+
		"\2\2\u0149\35\3\2\2\2\u014a\u0148\3\2\2\2\u014b\u014c\7=\2\2\u014c\37"+
		"\3\2\2\2\u014d\u014e\t\2\2\2\u014e!\3\2\2\2\u014f\u0150\t\3\2\2\u0150"+
		"#\3\2\2\2\u0151\u0152\t\4\2\2\u0152%\3\2\2\2\u0153\u0154\t\5\2\2\u0154"+
		"\'\3\2\2\2\u0155\u0156\7o\2\2\u0156)\3\2\2\2\u0157\u0158\7<\2\2\u0158"+
		"+\3\2\2\2\u0159\u015b\7\61\2\2\u015a\u0159\3\2\2\2\u015b\u015e\3\2\2\2"+
		"\u015c\u015a\3\2\2\2\u015c\u015d\3\2\2\2\u015d\u015f\3\2\2\2\u015e\u015c"+
		"\3\2\2\2\u015f\u0163\5V,\2\u0160\u0162\7M\2\2\u0161\u0160\3\2\2\2\u0162"+
		"\u0165\3\2\2\2\u0163\u0161\3\2\2\2\u0163\u0164\3\2\2\2\u0164\u0166\3\2"+
		"\2\2\u0165\u0163\3\2\2\2\u0166\u016a\5$\23\2\u0167\u0169\7\61\2\2\u0168"+
		"\u0167\3\2\2\2\u0169\u016c\3\2\2\2\u016a\u0168\3\2\2\2\u016a\u016b\3\2"+
		"\2\2\u016b\u016d\3\2\2\2\u016c\u016a\3\2\2\2\u016d\u0171\5V,\2\u016e\u0170"+
		"\7M\2\2\u016f\u016e\3\2\2\2\u0170\u0173\3\2\2\2\u0171\u016f\3\2\2\2\u0171"+
		"\u0172\3\2\2\2\u0172-\3\2\2\2\u0173\u0171\3\2\2\2\u0174\u0175\b\30\1\2"+
		"\u0175\u0176\5,\27\2\u0176\u0177\5\"\22\2\u0177\u0178\5,\27\2\u0178\u017b"+
		"\3\2\2\2\u0179\u017b\5,\27\2\u017a\u0174\3\2\2\2\u017a\u0179\3\2\2\2\u017b"+
		"\u0182\3\2\2\2\u017c\u017d\6\30\3\3\u017d\u017e\5\"\22\2\u017e\u017f\5"+
		".\30\2\u017f\u0181\3\2\2\2\u0180\u017c\3\2\2\2\u0181\u0184\3\2\2\2\u0182"+
		"\u0180\3\2\2\2\u0182\u0183\3\2\2\2\u0183/\3\2\2\2\u0184\u0182\3\2\2\2"+
		"\u0185\u0186\b\31\1\2\u0186\u0187\5\66\34\2\u0187\u0188\7\61\2\2\u0188"+
		"\u0189\5\60\31\2\u0189\u018a\7M\2\2\u018a\u0195\3\2\2\2\u018b\u018c\5"+
		"8\35\2\u018c\u018d\7\61\2\2\u018d\u018e\5\60\31\2\u018e\u018f\7M\2\2\u018f"+
		"\u0195\3\2\2\2\u0190\u0195\5\u00aeX\2\u0191\u0195\5\u00c4c\2\u0192\u0195"+
		"\7?\2\2\u0193\u0195\5\62\32\2\u0194\u0185\3\2\2\2\u0194\u018b\3\2\2\2"+
		"\u0194\u0190\3\2\2\2\u0194\u0191\3\2\2\2\u0194\u0192\3\2\2\2\u0194\u0193"+
		"\3\2\2\2\u0195\u019c\3\2\2\2\u0196\u0197\6\31\4\3\u0197\u0198\5\64\33"+
		"\2\u0198\u0199\5\60\31\2\u0199\u019b\3\2\2\2\u019a\u0196\3\2\2\2\u019b"+
		"\u019e\3\2\2\2\u019c\u019a\3\2\2\2\u019c\u019d\3\2\2\2\u019d\61\3\2\2"+
		"\2\u019e\u019c\3\2\2\2\u019f\u01a0\7\61\2\2\u01a0\u01a1\7k\2\2\u01a1\u01a2"+
		"\7\20\2\2\u01a2\u01a3\7k\2\2\u01a3\u01a4\7M\2\2\u01a4\63\3\2\2\2\u01a5"+
		"\u01a6\t\6\2\2\u01a6\65\3\2\2\2\u01a7\u01a8\t\7\2\2\u01a8\67\3\2\2\2\u01a9"+
		"\u01aa\t\b\2\2\u01aa9\3\2\2\2\u01ab\u01b2\5<\37\2\u01ac\u01b2\5> \2\u01ad"+
		"\u01b2\5B\"\2\u01ae\u01b2\5F$\2\u01af\u01b2\5D#\2\u01b0\u01b2\5H%\2\u01b1"+
		"\u01ab\3\2\2\2\u01b1\u01ac\3\2\2\2\u01b1\u01ad\3\2\2\2\u01b1\u01ae\3\2"+
		"\2\2\u01b1\u01af\3\2\2\2\u01b1\u01b0\3\2\2\2\u01b2;\3\2\2\2\u01b3\u01b4"+
		"\7(\2\2\u01b4\u01b5\7\61\2\2\u01b5\u01b6\5V,\2\u01b6\u01b7\7M\2\2\u01b7"+
		"=\3\2\2\2\u01b8\u01b9\7)\2\2\u01b9\u01ba\7\61\2\2\u01ba\u01bb\5V,\2\u01bb"+
		"\u01bc\7M\2\2\u01bc?\3\2\2\2\u01bd\u01be\t\t\2\2\u01beA\3\2\2\2\u01bf"+
		"\u01c0\7-\2\2\u01c0\u01c1\7\61\2\2\u01c1\u01c2\5V,\2\u01c2\u01c3\7\20"+
		"\2\2\u01c3\u01c6\5\u00c2b\2\u01c4\u01c5\7\20\2\2\u01c5\u01c7\5\u00c0a"+
		"\2\u01c6\u01c4\3\2\2\2\u01c6\u01c7\3\2\2\2\u01c7\u01c8\3\2\2\2\u01c8\u01c9"+
		"\7M\2\2\u01c9C\3\2\2\2\u01ca\u01cb\7*\2\2\u01cb\u01cc\7\61\2\2\u01cc\u01cd"+
		"\5V,\2\u01cd\u01ce\7\20\2\2\u01ce\u01cf\5\u00c2b\2\u01cf\u01d0\7M\2\2"+
		"\u01d0E\3\2\2\2\u01d1\u01d2\7*\2\2\u01d2\u01d3\7\61\2\2\u01d3\u01d4\5"+
		"V,\2\u01d4\u01d5\7M\2\2\u01d5G\3\2\2\2\u01d6\u01d7\7+\2\2\u01d7\u01d8"+
		"\7\61\2\2\u01d8\u01d9\5V,\2\u01d9\u01da\7M\2\2\u01daI\3\2\2\2\u01db\u01dc"+
		"\7\35\2\2\u01dc\u01dd\7\61\2\2\u01dd\u01de\5V,\2\u01de\u01df\7\20\2\2"+
		"\u01df\u01e2\7o\2\2\u01e0\u01e1\7\20\2\2\u01e1\u01e3\5P)\2\u01e2\u01e0"+
		"\3\2\2\2\u01e2\u01e3\3\2\2\2\u01e3\u01e4\3\2\2\2\u01e4\u01e5\7M\2\2\u01e5"+
		"K\3\2\2\2\u01e6\u01ea\5B\"\2\u01e7\u01ea\5F$\2\u01e8\u01ea\5D#\2\u01e9"+
		"\u01e6\3\2\2\2\u01e9\u01e7\3\2\2\2\u01e9\u01e8\3\2\2\2\u01ea\u01ec\3\2"+
		"\2\2\u01eb\u01ed\5@!\2\u01ec\u01eb\3\2\2\2\u01ec\u01ed\3\2\2\2\u01edM"+
		"\3\2\2\2\u01ee\u01ef\7m\2\2\u01efO\3\2\2\2\u01f0\u01f1\t\n\2\2\u01f1Q"+
		"\3\2\2\2\u01f2\u01f3\7 \2\2\u01f3\u01f4\7\61\2\2\u01f4\u01f5\5V,\2\u01f5"+
		"\u01f6\7\20\2\2\u01f6\u01f9\7o\2\2\u01f7\u01f8\7\20\2\2\u01f8\u01fa\5"+
		"P)\2\u01f9\u01f7\3\2\2\2\u01f9\u01fa\3\2\2\2\u01fa\u01fb\3\2\2\2\u01fb"+
		"\u01fc\7M\2\2\u01fcS\3\2\2\2\u01fd\u01fe\7\32\2\2\u01fe\u01ff\7\61\2\2"+
		"\u01ff\u0202\5N(\2\u0200\u0201\7\20\2\2\u0201\u0203\5P)\2\u0202\u0200"+
		"\3\2\2\2\u0202\u0203\3\2\2\2\u0203\u0204\3\2\2\2\u0204\u0205\7M\2\2\u0205"+
		"U\3\2\2\2\u0206\u0207\b,\1\2\u0207\u0278\5L\'\2\u0208\u0278\5\u00a4S\2"+
		"\u0209\u0278\5\24\13\2\u020a\u0278\5\u00aaV\2\u020b\u0278\5T+\2\u020c"+
		"\u020d\7V\2\2\u020d\u020e\7\61\2\2\u020e\u020f\5V,\2\u020f\u0210\7\20"+
		"\2\2\u0210\u0211\7/\2\2\u0211\u0212\5v<\2\u0212\u0213\7K\2\2\u0213\u0214"+
		"\7M\2\2\u0214\u0278\3\2\2\2\u0215\u0216\7]\2\2\u0216\u0217\7\61\2\2\u0217"+
		"\u0218\5V,\2\u0218\u0219\7\20\2\2\u0219\u021a\7/\2\2\u021a\u021b\5z>\2"+
		"\u021b\u021c\7K\2\2\u021c\u021d\7M\2\2\u021d\u0278\3\2\2\2\u021e\u021f"+
		"\7#\2\2\u021f\u0220\7\61\2\2\u0220\u0221\5V,\2\u0221\u0222\7\20\2\2\u0222"+
		"\u0223\7/\2\2\u0223\u0224\5z>\2\u0224\u0225\7K\2\2\u0225\u0226\7M\2\2"+
		"\u0226\u0278\3\2\2\2\u0227\u0228\7#\2\2\u0228\u0229\7\61\2\2\u0229\u022a"+
		"\5V,\2\u022a\u022b\7\20\2\2\u022b\u022c\7/\2\2\u022c\u022d\5L\'\2\u022d"+
		"\u022e\7K\2\2\u022e\u022f\7M\2\2\u022f\u0278\3\2\2\2\u0230\u0278\5\\/"+
		"\2\u0231\u0278\5^\60\2\u0232\u0278\5b\62\2\u0233\u0278\5f\64\2\u0234\u0278"+
		"\5h\65\2\u0235\u0278\5d\63\2\u0236\u0278\5j\66\2\u0237\u0278\5n8\2\u0238"+
		"\u0278\5r:\2\u0239\u0278\5\u009aN\2\u023a\u0278\5\u0092J\2\u023b\u0278"+
		"\5\u0098M\2\u023c\u0278\5\u009cO\2\u023d\u0278\5\u00be`\2\u023e\u023f"+
		"\7N\2\2\u023f\u0240\7\61\2\2\u0240\u0241\5V,\2\u0241\u0242\7\20\2\2\u0242"+
		"\u0243\7/\2\2\u0243\u0244\5z>\2\u0244\u0245\7K\2\2\u0245\u0246\7M\2\2"+
		"\u0246\u0278\3\2\2\2\u0247\u0248\7N\2\2\u0248\u0249\7\61\2\2\u0249\u024a"+
		"\5V,\2\u024a\u024b\7\20\2\2\u024b\u024c\7/\2\2\u024c\u024d\5L\'\2\u024d"+
		"\u024e\7K\2\2\u024e\u024f\7M\2\2\u024f\u0278\3\2\2\2\u0250\u0251\7\61"+
		"\2\2\u0251\u0252\5V,\2\u0252\u0253\7M\2\2\u0253\u0278\3\2\2\2\u0254\u0255"+
		"\7O\2\2\u0255\u0256\7\61\2\2\u0256\u0257\5V,\2\u0257\u0258\7\20\2\2\u0258"+
		"\u0259\5\u00c4c\2\u0259\u025a\7M\2\2\u025a\u0278\3\2\2\2\u025b\u025c\7"+
		"P\2\2\u025c\u025d\7\61\2\2\u025d\u025e\5V,\2\u025e\u025f\7\20\2\2\u025f"+
		"\u0260\7\60\2\2\u0260\u0261\5|?\2\u0261\u0262\7L\2\2\u0262\u0263\7M\2"+
		"\2\u0263\u0278\3\2\2\2\u0264\u0265\7Q\2\2\u0265\u0266\7\61\2\2\u0266\u0267"+
		"\5V,\2\u0267\u0268\7\20\2\2\u0268\u0269\7\60\2\2\u0269\u026a\5|?\2\u026a"+
		"\u026b\7L\2\2\u026b\u026c\7M\2\2\u026c\u0278\3\2\2\2\u026d\u026e\7R\2"+
		"\2\u026e\u026f\7\61\2\2\u026f\u0270\5V,\2\u0270\u0271\7\20\2\2\u0271\u0272"+
		"\7\60\2\2\u0272\u0273\5|?\2\u0273\u0274\7L\2\2\u0274\u0275\7M\2\2\u0275"+
		"\u0278\3\2\2\2\u0276\u0278\5\32\16\2\u0277\u0206\3\2\2\2\u0277\u0208\3"+
		"\2\2\2\u0277\u0209\3\2\2\2\u0277\u020a\3\2\2\2\u0277\u020b\3\2\2\2\u0277"+
		"\u020c\3\2\2\2\u0277\u0215\3\2\2\2\u0277\u021e\3\2\2\2\u0277\u0227\3\2"+
		"\2\2\u0277\u0230\3\2\2\2\u0277\u0231\3\2\2\2\u0277\u0232\3\2\2\2\u0277"+
		"\u0233\3\2\2\2\u0277\u0234\3\2\2\2\u0277\u0235\3\2\2\2\u0277\u0236\3\2"+
		"\2\2\u0277\u0237\3\2\2\2\u0277\u0238\3\2\2\2\u0277\u0239\3\2\2\2\u0277"+
		"\u023a\3\2\2\2\u0277\u023b\3\2\2\2\u0277\u023c\3\2\2\2\u0277\u023d\3\2"+
		"\2\2\u0277\u023e\3\2\2\2\u0277\u0247\3\2\2\2\u0277\u0250\3\2\2\2\u0277"+
		"\u0254\3\2\2\2\u0277\u025b\3\2\2\2\u0277\u0264\3\2\2\2\u0277\u026d\3\2"+
		"\2\2\u0277\u0276\3\2\2\2\u0278\u02a2\3\2\2\2\u0279\u027a\6,\5\3\u027a"+
		"\u027b\7C\2\2\u027b\u02a1\5V,\2\u027c\u027d\6,\6\3\u027d\u027e\5\"\22"+
		"\2\u027e\u027f\5V,\2\u027f\u02a1\3\2\2\2\u0280\u0281\6,\7\3\u0281\u0282"+
		"\5X-\2\u0282\u0283\5V,\2\u0283\u02a1\3\2\2\2\u0284\u0285\6,\b\3\u0285"+
		"\u0286\5$\23\2\u0286\u0287\5V,\2\u0287\u02a1\3\2\2\2\u0288\u0289\6,\t"+
		"\3\u0289\u028a\7\60\2\2\u028a\u028b\5v<\2\u028b\u028c\7L\2\2\u028c\u02a1"+
		"\3\2\2\2\u028d\u028e\6,\n\3\u028e\u028f\7\60\2\2\u028f\u0290\5z>\2\u0290"+
		"\u0291\7L\2\2\u0291\u02a1\3\2\2\2\u0292\u0293\6,\13\3\u0293\u0294\7\60"+
		"\2\2\u0294\u0295\5\24\13\2\u0295\u0296\7L\2\2\u0296\u02a1\3\2\2\2\u0297"+
		"\u0298\6,\f\3\u0298\u0299\7\37\2\2\u0299\u02a1\5p9\2\u029a\u029b\6,\r"+
		"\3\u029b\u029d\7,\2\2\u029c\u029e\7=\2\2\u029d\u029c\3\2\2\2\u029d\u029e"+
		"\3\2\2\2\u029e\u029f\3\2\2\2\u029f\u02a1\7@\2\2\u02a0\u0279\3\2\2\2\u02a0"+
		"\u027c\3\2\2\2\u02a0\u0280\3\2\2\2\u02a0\u0284\3\2\2\2\u02a0\u0288\3\2"+
		"\2\2\u02a0\u028d\3\2\2\2\u02a0\u0292\3\2\2\2\u02a0\u0297\3\2\2\2\u02a0"+
		"\u029a\3\2\2\2\u02a1\u02a4\3\2\2\2\u02a2\u02a0\3\2\2\2\u02a2\u02a3\3\2"+
		"\2\2\u02a3W\3\2\2\2\u02a4\u02a2\3\2\2\2\u02a5\u02a6\t\6\2\2\u02a6Y\3\2"+
		"\2\2\u02a7\u02a8\t\13\2\2\u02a8[\3\2\2\2\u02a9\u02ab\5Z.\2\u02aa\u02ac"+
		"\7\61\2\2\u02ab\u02aa\3\2\2\2\u02ab\u02ac\3\2\2\2\u02ac\u02ad\3\2\2\2"+
		"\u02ad\u02af\5V,\2\u02ae\u02b0\7M\2\2\u02af\u02ae\3\2\2\2\u02af\u02b0"+
		"\3\2\2\2\u02b0]\3\2\2\2\u02b1\u02b2\58\35\2\u02b2\u02b3\7\61\2\2\u02b3"+
		"\u02b4\5V,\2\u02b4\u02b5\7M\2\2\u02b5_\3\2\2\2\u02b6\u02b7\t\f\2\2\u02b7"+
		"a\3\2\2\2\u02b8\u02b9\5`\61\2\u02b9\u02ba\7\61\2\2\u02ba\u02bb\5V,\2\u02bb"+
		"\u02bc\7M\2\2\u02bcc\3\2\2\2\u02bd\u02be\7G\2\2\u02be\u02bf\7\61\2\2\u02bf"+
		"\u02c0\5V,\2\u02c0\u02c1\7\20\2\2\u02c1\u02c2\5\60\31\2\u02c2\u02c3\7"+
		"M\2\2\u02c3e\3\2\2\2\u02c4\u02c5\7:\2\2\u02c5\u02c6\7\61\2\2\u02c6\u02c7"+
		"\5V,\2\u02c7\u02c8\7\20\2\2\u02c8\u02c9\5V,\2\u02c9\u02ca\7M\2\2\u02ca"+
		"g\3\2\2\2\u02cb\u02cc\79\2\2\u02cc\u02cd\7\61\2\2\u02cd\u02ce\5V,\2\u02ce"+
		"\u02cf\7\20\2\2\u02cf\u02d0\5V,\2\u02d0\u02d1\7M\2\2\u02d1i\3\2\2\2\u02d2"+
		"\u02d3\7=\2\2\u02d3\u02d4\7\61\2\2\u02d4\u02d5\5V,\2\u02d5\u02d6\7M\2"+
		"\2\u02d6\u02df\3\2\2\2\u02d7\u02d8\7\f\2\2\u02d8\u02d9\7\61\2\2\u02d9"+
		"\u02da\5V,\2\u02da\u02db\7\20\2\2\u02db\u02dc\5\60\31\2\u02dc\u02dd\7"+
		"M\2\2\u02dd\u02df\3\2\2\2\u02de\u02d2\3\2\2\2\u02de\u02d7\3\2\2\2\u02df"+
		"k\3\2\2\2\u02e0\u02e4\7n\2\2\u02e1\u02e3\7n\2\2\u02e2\u02e1\3\2\2\2\u02e3"+
		"\u02e6\3\2\2\2\u02e4\u02e2\3\2\2\2\u02e4\u02e5\3\2\2\2\u02e5m\3\2\2\2"+
		"\u02e6\u02e4\3\2\2\2\u02e7\u02e8\7\61\2\2\u02e8\u02e9\5l\67\2\u02e9\u02ea"+
		"\7M\2\2\u02ea\u02eb\5V,\2\u02ebo\3\2\2\2\u02ec\u02ed\t\r\2\2\u02edq\3"+
		"\2\2\2\u02ee\u02ef\7/\2\2\u02ef\u02f0\5p9\2\u02f0\u02f1\7\17\2\2\u02f1"+
		"\u02f2\5V,\2\u02f2\u02fa\3\2\2\2\u02f3\u02f4\7S\2\2\u02f4\u02f5\5p9\2"+
		"\u02f5\u02f6\7\17\2\2\u02f6\u02f7\5V,\2\u02f7\u02f9\3\2\2\2\u02f8\u02f3"+
		"\3\2\2\2\u02f9\u02fc\3\2\2\2\u02fa\u02f8\3\2\2\2\u02fa\u02fb\3\2\2\2\u02fb"+
		"\u02fd\3\2\2\2\u02fc\u02fa\3\2\2\2\u02fd\u02fe\7K\2\2\u02fes\3\2\2\2\u02ff"+
		"\u0300\7/\2\2\u0300\u0301\5p9\2\u0301\u0302\7\17\2\2\u0302\u0303\5V,\2"+
		"\u0303\u030b\3\2\2\2\u0304\u0305\7S\2\2\u0305\u0306\5p9\2\u0306\u0307"+
		"\7\17\2\2\u0307\u0308\5V,\2\u0308\u030a\3\2\2\2\u0309\u0304\3\2\2\2\u030a"+
		"\u030d\3\2\2\2\u030b\u0309\3\2\2\2\u030b\u030c\3\2\2\2\u030c\u030e\3\2"+
		"\2\2\u030d\u030b\3\2\2\2\u030e\u030f\7K\2\2\u030fu\3\2\2\2\u0310\u0315"+
		"\5x=\2\u0311\u0312\7\20\2\2\u0312\u0314\5x=\2\u0313\u0311\3\2\2\2\u0314"+
		"\u0317\3\2\2\2\u0315\u0313\3\2\2\2\u0315\u0316\3\2\2\2\u0316w\3\2\2\2"+
		"\u0317\u0315\3\2\2\2\u0318\u031b\5\u00c2b\2\u0319\u031a\7\17\2\2\u031a"+
		"\u031c\5\u00c0a\2\u031b\u0319\3\2\2\2\u031b\u031c\3\2\2\2\u031c\u031d"+
		"\3\2\2\2\u031d\u031e\7\61\2\2\u031e\u031f\5V,\2\u031f\u0320\7M\2\2\u0320"+
		"y\3\2\2\2\u0321\u0326\5\u0080A\2\u0322\u0323\7\20\2\2\u0323\u0325\5\u0080"+
		"A\2\u0324\u0322\3\2\2\2\u0325\u0328\3\2\2\2\u0326\u0324\3\2\2\2\u0326"+
		"\u0327\3\2\2\2\u0327{\3\2\2\2\u0328\u0326\3\2\2\2\u0329\u032e\5~@\2\u032a"+
		"\u032b\7\20\2\2\u032b\u032d\5~@\2\u032c\u032a\3\2\2\2\u032d\u0330\3\2"+
		"\2\2\u032e\u032c\3\2\2\2\u032e\u032f\3\2\2\2\u032f}\3\2\2\2\u0330\u032e"+
		"\3\2\2\2\u0331\u0332\5\u00c2b\2\u0332\u0335\7\61\2\2\u0333\u0336\5\u00c4"+
		"c\2\u0334\u0336\7o\2\2\u0335\u0333\3\2\2\2\u0335\u0334\3\2\2\2\u0336\u0337"+
		"\3\2\2\2\u0337\u033a\7\17\2\2\u0338\u033b\5\u00c4c\2\u0339\u033b\7o\2"+
		"\2\u033a\u0338\3\2\2\2\u033a\u0339\3\2\2\2\u033b\u033c\3\2\2\2\u033c\u033d"+
		"\7M\2\2\u033d\u0344\3\2\2\2\u033e\u033f\5\u00c2b\2\u033f\u0340\7\61\2"+
		"\2\u0340\u0341\5\u00c4c\2\u0341\u0342\7M\2\2\u0342\u0344\3\2\2\2\u0343"+
		"\u0331\3\2\2\2\u0343\u033e\3\2\2\2\u0344\177\3\2\2\2\u0345\u0348\5\u00c2"+
		"b\2\u0346\u0347\7\17\2\2\u0347\u0349\5\u00c0a\2\u0348\u0346\3\2\2\2\u0348"+
		"\u0349\3\2\2\2\u0349\u034a\3\2\2\2\u034a\u034b\7\61\2\2\u034b\u034c\5"+
		"V,\2\u034c\u034d\7\17\2\2\u034d\u034e\5V,\2\u034e\u034f\7M\2\2\u034f\u035a"+
		"\3\2\2\2\u0350\u0353\5\u00c2b\2\u0351\u0352\7\17\2\2\u0352\u0354\5\u00c0"+
		"a\2\u0353\u0351\3\2\2\2\u0353\u0354\3\2\2\2\u0354\u0355\3\2\2\2\u0355"+
		"\u0356\7\61\2\2\u0356\u0357\5V,\2\u0357\u0358\7M\2\2\u0358\u035a\3\2\2"+
		"\2\u0359\u0345\3\2\2\2\u0359\u0350\3\2\2\2\u035a\u0081\3\2\2\2\u035b\u035f"+
		"\5\u00c6d\2\u035c\u035e\5\u00c6d\2\u035d\u035c\3\2\2\2\u035e\u0361\3\2"+
		"\2\2\u035f\u035d\3\2\2\2\u035f\u0360\3\2\2\2\u0360\u036c\3\2\2\2\u0361"+
		"\u035f\3\2\2\2\u0362\u0363\7\20\2\2\u0363\u0367\5\u00c6d\2\u0364\u0366"+
		"\5\u00c6d\2\u0365\u0364\3\2\2\2\u0366\u0369\3\2\2\2\u0367\u0365\3\2\2"+
		"\2\u0367\u0368\3\2\2\2\u0368\u036b\3\2\2\2\u0369\u0367\3\2\2\2\u036a\u0362"+
		"\3\2\2\2\u036b\u036e\3\2\2\2\u036c\u036a\3\2\2\2\u036c\u036d\3\2\2\2\u036d"+
		"\u0083\3\2\2\2\u036e\u036c\3\2\2\2\u036f\u0370\7\61\2\2\u0370\u0371\5"+
		"\u0082B\2\u0371\u0379\7M\2\2\u0372\u0373\7\20\2\2\u0373\u0374\7\61\2\2"+
		"\u0374\u0375\5\u0082B\2\u0375\u0376\7M\2\2\u0376\u0378\3\2\2\2\u0377\u0372"+
		"\3\2\2\2\u0378\u037b\3\2\2\2\u0379\u0377\3\2\2\2\u0379\u037a\3\2\2\2\u037a"+
		"\u0085\3\2\2\2\u037b\u0379\3\2\2\2\u037c\u037d\7f\2\2\u037d\u037e\5\u0084"+
		"C\2\u037e\u0087\3\2\2\2\u037f\u0380\7e\2\2\u0380\u0381\7\61\2\2\u0381"+
		"\u0382\5\u0084C\2\u0382\u0383\7M\2\2\u0383\u0089\3\2\2\2\u0384\u0385\7"+
		"g\2\2\u0385\u0386\7\61\2\2\u0386\u0387\7\61\2\2\u0387\u0388\5\u0084C\2"+
		"\u0388\u0390\7M\2\2\u0389\u038a\7\20\2\2\u038a\u038b\7\61\2\2\u038b\u038c"+
		"\5\u0084C\2\u038c\u038d\7M\2\2\u038d\u038f\3\2\2\2\u038e\u0389\3\2\2\2"+
		"\u038f\u0392\3\2\2\2\u0390\u038e\3\2\2\2\u0390\u0391\3\2\2\2\u0391\u0393"+
		"\3\2\2\2\u0392\u0390\3\2\2\2\u0393\u0394\7M\2\2\u0394\u008b\3\2\2\2\u0395"+
		"\u0399\5\u0088E\2\u0396\u0399\5\u0086D\2\u0397\u0399\5\u008aF\2\u0398"+
		"\u0395\3\2\2\2\u0398\u0396\3\2\2\2\u0398\u0397\3\2\2\2\u0399\u008d\3\2"+
		"\2\2\u039a\u039b\7n\2\2\u039b\u008f\3\2\2\2\u039c\u039d\7n\2\2\u039d\u0091"+
		"\3\2\2\2\u039e\u039f\7\16\2\2\u039f\u03a0\7\61\2\2\u03a0\u03a1\5V,\2\u03a1"+
		"\u03a2\7\20\2\2\u03a2\u03a3\7\25\2\2\u03a3\u03a4\7\61\2\2\u03a4\u03a5"+
		"\7h\2\2\u03a5\u03a6\7\61\2\2\u03a6\u03a7\5\u008eH\2\u03a7\u03a8\7\20\2"+
		"\2\u03a8\u03a9\5\u0090I\2\u03a9\u03aa\7M\2\2\u03aa\u03ab\7\20\2\2\u03ab"+
		"\u03ac\5\u008cG\2\u03ac\u03af\7M\2\2\u03ad\u03ae\7\20\2\2\u03ae\u03b0"+
		"\5\u00c0a\2\u03af\u03ad\3\2\2\2\u03af\u03b0\3\2\2\2\u03b0\u03b1\3\2\2"+
		"\2\u03b1\u03b2\7M\2\2\u03b2\u0093\3\2\2\2\u03b3\u03b4\7n\2\2\u03b4\u0095"+
		"\3\2\2\2\u03b5\u03b6\7n\2\2\u03b6\u0097\3\2\2\2\u03b7\u03b8\7\16\2\2\u03b8"+
		"\u03b9\7\61\2\2\u03b9\u03ba\5V,\2\u03ba\u03bb\7\20\2\2\u03bb\u03bc\7\26"+
		"\2\2\u03bc\u03bd\7\61\2\2\u03bd\u03be\7h\2\2\u03be\u03bf\7\61\2\2\u03bf"+
		"\u03c0\5\u0094K\2\u03c0\u03c1\7\20\2\2\u03c1\u03c2\5\u0096L\2\u03c2\u03c3"+
		"\7M\2\2\u03c3\u03c4\7\20\2\2\u03c4\u03c5\5\u0086D\2\u03c5\u03c6\7\20\2"+
		"\2\u03c6\u03c9\5\u008cG\2\u03c7\u03c8\7\20\2\2\u03c8\u03ca\7\34\2\2\u03c9"+
		"\u03c7\3\2\2\2\u03c9\u03ca\3\2\2\2\u03ca\u03cb\3\2\2\2\u03cb\u03ce\7M"+
		"\2\2\u03cc\u03cd\7\20\2\2\u03cd\u03cf\5\u00c0a\2\u03ce\u03cc\3\2\2\2\u03ce"+
		"\u03cf\3\2\2\2\u03cf\u03d0\3\2\2\2\u03d0\u03d1\7M\2\2\u03d1\u0099\3\2"+
		"\2\2\u03d2\u03d3\7\16\2\2\u03d3\u03d4\7\61\2\2\u03d4\u03d5\5V,\2\u03d5"+
		"\u03d6\7\20\2\2\u03d6\u03d9\5\u008cG\2\u03d7\u03d8\7\20\2\2\u03d8\u03da"+
		"\5\u00c0a\2\u03d9\u03d7\3\2\2\2\u03d9\u03da\3\2\2\2\u03da\u03db\3\2\2"+
		"\2\u03db\u03dd\7M\2\2\u03dc\u03de\7i\2\2\u03dd\u03dc\3\2\2\2\u03dd\u03de"+
		"\3\2\2\2\u03de\u009b\3\2\2\2\u03df\u03e0\7\31\2\2\u03e0\u03e1\7\61\2\2"+
		"\u03e1\u03e2\5V,\2\u03e2\u03e3\7\20\2\2\u03e3\u03ea\5\u009eP\2\u03e4\u03e5"+
		"\7\20\2\2\u03e5\u03e7\7/\2\2\u03e6\u03e8\5\u00a2R\2\u03e7\u03e6\3\2\2"+
		"\2\u03e7\u03e8\3\2\2\2\u03e8\u03e9\3\2\2\2\u03e9\u03eb\7K\2\2\u03ea\u03e4"+
		"\3\2\2\2\u03ea\u03eb\3\2\2\2\u03eb\u03ec\3\2\2\2\u03ec\u03ed\7M\2\2\u03ed"+
		"\u009d\3\2\2\2\u03ee\u03ef\7/\2\2\u03ef\u03f4\5\u00a0Q\2\u03f0\u03f1\7"+
		"\20\2\2\u03f1\u03f3\5\u00a0Q\2\u03f2\u03f0\3\2\2\2\u03f3\u03f6\3\2\2\2"+
		"\u03f4\u03f2\3\2\2\2\u03f4\u03f5\3\2\2\2\u03f5\u03f7\3\2\2\2\u03f6\u03f4"+
		"\3\2\2\2\u03f7\u03f8\7K\2\2\u03f8\u009f\3\2\2\2\u03f9\u03fa\5\u00c2b\2"+
		"\u03fa\u03fb\7\17\2\2\u03fb\u03fc\5\u00c0a\2\u03fc\u00a1\3\2\2\2\u03fd"+
		"\u03fe\7n\2\2\u03fe\u00a3\3\2\2\2\u03ff\u0400\7\27\2\2\u0400\u0401\7n"+
		"\2\2\u0401\u0402\7B\2\2\u0402\u0407\5\u00a6T\2\u0403\u0404\7\20\2\2\u0404"+
		"\u0406\5\u00a6T\2\u0405\u0403\3\2\2\2\u0406\u0409\3\2\2\2\u0407\u0405"+
		"\3\2\2\2\u0407\u0408\3\2\2\2\u0408\u040a\3\2\2\2\u0409\u0407\3\2\2\2\u040a"+
		"\u040b\7b\2\2\u040b\u040c\5V,\2\u040c\u00a5\3\2\2\2\u040d\u040e\5\24\13"+
		"\2\u040e\u040f\5\u00c2b\2\u040f\u0410\7\61\2\2\u0410\u0411\5L\'\2\u0411"+
		"\u0412\7M\2\2\u0412\u0417\3\2\2\2\u0413\u0414\5\24\13\2\u0414\u0415\5"+
		"\u0080A\2\u0415\u0417\3\2\2\2\u0416\u040d\3\2\2\2\u0416\u0413\3\2\2\2"+
		"\u0417\u00a7\3\2\2\2\u0418\u0419\5\32\16\2\u0419\u041a\7\17\2\2\u041a"+
		"\u041b\5\32\16\2\u041b\u00a9\3\2\2\2\u041c\u041d\7\27\2\2\u041d\u041e"+
		"\7n\2\2\u041e\u041f\7B\2\2\u041f\u0424\5\u00a6T\2\u0420\u0421\7\20\2\2"+
		"\u0421\u0423\5\u00a6T\2\u0422\u0420\3\2\2\2\u0423\u0426\3\2\2\2\u0424"+
		"\u0422\3\2\2\2\u0424\u0425\3\2\2\2\u0425\u0427\3\2\2\2\u0426\u0424\3\2"+
		"\2\2\u0427\u0428\7a\2\2\u0428\u0429\7\64\2\2\u0429\u042a\7\67\2\2\u042a"+
		"\u042f\5\u00c6d\2\u042b\u042c\7S\2\2\u042c\u042e\5\u00c6d\2\u042d\u042b"+
		"\3\2\2\2\u042e\u0431\3\2\2\2\u042f\u042d\3\2\2\2\u042f\u0430\3\2\2\2\u0430"+
		"\u0432\3\2\2\2\u0431\u042f\3\2\2\2\u0432\u0433\7%\2\2\u0433\u00ab\3\2"+
		"\2\2\u0434\u0435\5\u0080A\2\u0435\u00ad\3\2\2\2\u0436\u0439\5\u00b8]\2"+
		"\u0437\u0439\5\u00bc_\2\u0438\u0436\3\2\2\2\u0438\u0437\3\2\2\2\u0439"+
		"\u00af\3\2\2\2\u043a\u043b\t\16\2\2\u043b\u00b1\3\2\2\2\u043c\u043d\t"+
		"\17\2\2\u043d\u00b3\3\2\2\2\u043e\u043f\5\u00b0Y\2\u043f\u0440\7\61\2"+
		"\2\u0440\u0441\5V,\2\u0441\u0442\7M\2\2\u0442\u00b5\3\2\2\2\u0443\u0444"+
		"\5\u00b2Z\2\u0444\u0445\7\61\2\2\u0445\u0446\5V,\2\u0446\u0447\7M\2\2"+
		"\u0447\u00b7\3\2\2\2\u0448\u044b\5\u00b4[\2\u0449\u044b\5\u00b6\\\2\u044a"+
		"\u0448\3\2\2\2\u044a\u0449\3\2\2\2\u044b\u00b9\3\2\2\2\u044c\u044d\t\20"+
		"\2\2\u044d\u00bb\3\2\2\2\u044e\u044f\7\21\2\2\u044f\u0450\5\u00ba^\2\u0450"+
		"\u0451\7B\2\2\u0451\u0456\5\u00a6T\2\u0452\u0453\7\20\2\2\u0453\u0455"+
		"\5\u00a6T\2\u0454\u0452\3\2\2\2\u0455\u0458\3\2\2\2\u0456\u0454\3\2\2"+
		"\2\u0456\u0457\3\2\2\2\u0457\u045a\3\2\2\2\u0458\u0456\3\2\2\2\u0459\u045b"+
		"\5\20\t\2\u045a\u0459\3\2\2\2\u045a\u045b\3\2\2\2\u045b\u045c\3\2\2\2"+
		"\u045c\u045d\7_\2\2\u045d\u045e\5V,\2\u045e\u00bd\3\2\2\2\u045f\u0460"+
		"\7Z\2\2\u0460\u0464\7\r\2\2\u0461\u0463\7\61\2\2\u0462\u0461\3\2\2\2\u0463"+
		"\u0466\3\2\2\2\u0464\u0462\3\2\2\2\u0464\u0465\3\2\2\2\u0465\u0467\3\2"+
		"\2\2\u0466\u0464\3\2\2\2\u0467\u046b\5.\30\2\u0468\u046a\7M\2\2\u0469"+
		"\u0468\3\2\2\2\u046a\u046d\3\2\2\2\u046b\u0469\3\2\2\2\u046b\u046c\3\2"+
		"\2\2\u046c\u046e\3\2\2\2\u046d\u046b\3\2\2\2\u046e\u046f\7J\2\2\u046f"+
		"\u0483\5t;\2\u0470\u0474\7\r\2\2\u0471\u0473\7\61\2\2\u0472\u0471\3\2"+
		"\2\2\u0473\u0476\3\2\2\2\u0474\u0472\3\2\2\2\u0474\u0475\3\2\2\2\u0475"+
		"\u0477\3\2\2\2\u0476\u0474\3\2\2\2\u0477\u047b\5.\30\2\u0478\u047a\7M"+
		"\2\2\u0479\u0478\3\2\2\2\u047a\u047d\3\2\2\2\u047b\u0479\3\2\2\2\u047b"+
		"\u047c\3\2\2\2\u047c\u047e\3\2\2\2\u047d\u047b\3\2\2\2\u047e\u047f\7J"+
		"\2\2\u047f\u0480\5t;\2\u0480\u0482\3\2\2\2\u0481\u0470\3\2\2\2\u0482\u0485"+
		"\3\2\2\2\u0483\u0481\3\2\2\2\u0483\u0484\3\2\2\2\u0484\u0486\3\2\2\2\u0485"+
		"\u0483\3\2\2\2\u0486\u0487\7\33\2\2\u0487\u0488\7J\2\2\u0488\u0489\5t"+
		";\2\u0489\u04b6\3\2\2\2\u048a\u048b\7Z\2\2\u048b\u048f\7\r\2\2\u048c\u048e"+
		"\7\61\2\2\u048d\u048c\3\2\2\2\u048e\u0491\3\2\2\2\u048f\u048d\3\2\2\2"+
		"\u048f\u0490\3\2\2\2\u0490\u0492\3\2\2\2\u0491\u048f\3\2\2\2\u0492\u0496"+
		"\5.\30\2\u0493\u0495\7M\2\2\u0494\u0493\3\2\2\2\u0495\u0498\3\2\2\2\u0496"+
		"\u0494\3\2\2\2\u0496\u0497\3\2\2\2\u0497\u0499\3\2\2\2\u0498\u0496\3\2"+
		"\2\2\u0499\u049a\7J\2\2\u049a\u04ae\5\30\r\2\u049b\u049f\7\r\2\2\u049c"+
		"\u049e\7\61\2\2\u049d\u049c\3\2\2\2\u049e\u04a1\3\2\2\2\u049f\u049d\3"+
		"\2\2\2\u049f\u04a0\3\2\2\2\u04a0\u04a2\3\2\2\2\u04a1\u049f\3\2\2\2\u04a2"+
		"\u04a6\5.\30\2\u04a3\u04a5\7M\2\2\u04a4\u04a3\3\2\2\2\u04a5\u04a8\3\2"+
		"\2\2\u04a6\u04a4\3\2\2\2\u04a6\u04a7\3\2\2\2\u04a7\u04a9\3\2\2\2\u04a8"+
		"\u04a6\3\2\2\2\u04a9\u04aa\7J\2\2\u04aa\u04ab\5\30\r\2\u04ab\u04ad\3\2"+
		"\2\2\u04ac\u049b\3\2\2\2\u04ad\u04b0\3\2\2\2\u04ae\u04ac\3\2\2\2\u04ae"+
		"\u04af\3\2\2\2\u04af\u04b1\3\2\2\2\u04b0\u04ae\3\2\2\2\u04b1\u04b2\7\33"+
		"\2\2\u04b2\u04b3\7J\2\2\u04b3\u04b4\5\30\r\2\u04b4\u04b6\3\2\2\2\u04b5"+
		"\u045f\3\2\2\2\u04b5\u048a\3\2\2\2\u04b6\u00bf\3\2\2\2\u04b7\u04b8\7o"+
		"\2\2\u04b8\u00c1\3\2\2\2\u04b9\u04ba\7n\2\2\u04ba\u00c3\3\2\2\2\u04bb"+
		"\u04bd\7;\2\2\u04bc\u04bb\3\2\2\2\u04bc\u04bd\3\2\2\2\u04bd\u04be\3\2"+
		"\2\2\u04be\u04c8\7j\2\2\u04bf\u04c1\7;\2\2\u04c0\u04bf\3\2\2\2\u04c0\u04c1"+
		"\3\2\2\2\u04c1\u04c2\3\2\2\2\u04c2\u04c8\7k\2\2\u04c3\u04c5\7;\2\2\u04c4"+
		"\u04c3\3\2\2\2\u04c4\u04c5\3\2\2\2\u04c5\u04c6\3\2\2\2\u04c6\u04c8\7l"+
		"\2\2\u04c7\u04bc\3\2\2\2\u04c7\u04c0\3\2\2\2\u04c7\u04c4\3\2\2\2\u04c8"+
		"\u00c5\3\2\2\2\u04c9\u04d2\7o\2\2\u04ca\u04d2\7^\2\2\u04cb\u04d2\7$\2"+
		"\2\u04cc\u04ce\7;\2\2\u04cd\u04cc\3\2\2\2\u04cd\u04ce\3\2\2\2\u04ce\u04cf"+
		"\3\2\2\2\u04cf\u04d2\5\u00c4c\2\u04d0\u04d2\5\62\32\2\u04d1\u04c9\3\2"+
		"\2\2\u04d1\u04ca\3\2\2\2\u04d1\u04cb\3\2\2\2\u04d1\u04cd\3\2\2\2\u04d1"+
		"\u04d0\3\2\2\2\u04d2\u00c7\3\2\2\2a\u00ca\u00cd\u00d7\u00dc\u00e1\u00e8"+
		"\u00ec\u00f4\u0104\u0108\u010c\u0110\u0114\u011d\u0120\u0124\u012a\u0131"+
		"\u0135\u0140\u0148\u015c\u0163\u016a\u0171\u017a\u0182\u0194\u019c\u01b1"+
		"\u01c6\u01e2\u01e9\u01ec\u01f9\u0202\u0277\u029d\u02a0\u02a2\u02ab\u02af"+
		"\u02de\u02e4\u02fa\u030b\u0315\u031b\u0326\u032e\u0335\u033a\u0343\u0348"+
		"\u0353\u0359\u035f\u0367\u036c\u0379\u0390\u0398\u03af\u03c9\u03ce\u03d9"+
		"\u03dd\u03e7\u03ea\u03f4\u0407\u0416\u0424\u042f\u0438\u044a\u0456\u045a"+
		"\u0464\u046b\u0474\u047b\u0483\u048f\u0496\u049f\u04a6\u04ae\u04b5\u04bc"+
		"\u04c0\u04c4\u04c7\u04cd\u04d1";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}