// Generated from wcps.g4 by ANTLR 4.1
package petascope.wcps2.parser;
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
		AVG=9, BIT=10, COLON=11, COMMA=12, CONDENSE=13, COS=14, COSH=15, COUNT=16,
		COVERAGE=17, COVERAGE_VARIABLE_NAME_PREFIX=18, CRS_TRANSFORM=19, DECODE=20,
		DESCRIBE_COVERAGE=21, DIVISION=22, DOT=23, ENCODE=24, EQUAL=25, EXP=26,
		EXTEND=27, FALSE=28, GREATER_THAN=29, GREATER_OR_EQUAL_THAN=30, IMAGINARY_PART=31,
		ID=32, IMGCRSDOMAIN=33, IN=34, LEFT_BRACE=35, LEFT_BRACKET=36, LEFT_PARANTHESIS=37,
		LN=38, LIST=39, LOG=40, LOWER_THAN=41, LOWER_OR_EQUAL_THAN=42, MAX=43,
		MIN=44, MINUS=45, MULTIPLICATION=46, NOT=47, NOT_EQUAL=48, OR=49, OVER=50,
		OVERLAY=51, PLUS=52, POWER=53, REAL_PART=54, ROUND=55, RETURN=56, RIGHT_BRACE=57,
		RIGHT_BRACKET=58, RIGHT_PARANTHESIS=59, SCALE=60, SEMICOLON=61, SIN=62,
		SINH=63, SLICE=64, SOME=65, SQUARE_ROOT=66, STRUCT=67, TAN=68, TANH=69,
		TRIM=70, TRUE=71, USING=72, VALUE=73, VALUES=74, WHERE=75, XOR=76, REAL_NUMBER_CONSTANT=77,
		IDENTIFIER=78, NAME=79, FORMAT_NAME=80, STRING_LITERAL=81, WS=82;
	public static final String[] tokenNames = {
		"<INVALID>", "FOR", "ABSOLUTE_VALUE", "ADD", "ALL", "AND", "ARCSIN", "ARCCOS",
		"ARCTAN", "AVG", "BIT", "':'", "','", "CONDENSE", "COS", "COSH", "COUNT",
		"COVERAGE", "'$'", "CRS_TRANSFORM", "DECODE", "DESCRIBE_COVERAGE", "'/'",
		"'.'", "ENCODE", "'='", "EXP", "EXTEND", "FALSE", "'>'", "'>='", "IMAGINARY_PART",
		"ID", "IMGCRSDOMAIN", "IN", "'{'", "'['", "'('", "LN", "LIST", "LOG",
		"'<'", "'<='", "MAX", "MIN", "'-'", "'*'", "NOT", "'!='", "OR", "OVER",
		"OVERLAY", "'+'", "POWER", "REAL_PART", "ROUND", "RETURN", "'}'", "']'",
		"')'", "SCALE", "';'", "SIN", "SINH", "SLICE", "SOME", "SQUARE_ROOT",
		"STRUCT", "TAN", "TANH", "TRIM", "TRUE", "USING", "VALUE", "VALUES", "WHERE",
		"XOR", "REAL_NUMBER_CONSTANT", "IDENTIFIER", "NAME", "FORMAT_NAME", "STRING_LITERAL",
		"WS"
	};
	public static final int
		RULE_wcpsQuery = 0, RULE_forClauseList = 1, RULE_forClause = 2, RULE_whereClause = 3,
		RULE_returnClause = 4, RULE_coverageVariableName = 5, RULE_processingExpression = 6,
		RULE_scalarExpression = 7, RULE_booleanScalarExpression = 8, RULE_booleanUnaryOperator = 9,
		RULE_booleanConstant = 10, RULE_booleanOperator = 11, RULE_numericalComparissonOperator = 12,
		RULE_stringOperator = 13, RULE_stringScalarExpression = 14, RULE_starExpression = 15,
		RULE_numericalScalarExpression = 16, RULE_complexNumberConstant = 17,
		RULE_numericalOperator = 18, RULE_numericalUnaryOperation = 19, RULE_trigonometricOperator = 20,
		RULE_getComponentExpression = 21, RULE_coverageIdExpression = 22, RULE_describeCoverageExpression = 23,
		RULE_encodedCoverageExpression = 24, RULE_decodeCoverageExpression = 25,
		RULE_coverageExpression = 26, RULE_coverageArithmeticOperator = 27, RULE_unaryArithmeticExpressionOperator = 28,
		RULE_unaryArithmeticExpression = 29, RULE_trigonometricExpression = 30,
		RULE_exponentialExpressionOperator = 31, RULE_exponentialExpression = 32,
		RULE_unaryBooleanExpression = 33, RULE_rangeType = 34, RULE_castExpression = 35,
		RULE_fieldName = 36, RULE_rangeConstructorExpression = 37, RULE_crsTransformExpression = 38,
		RULE_dimensionPointList = 39, RULE_dimensionPointElement = 40, RULE_dimensionPointExpression = 41,
		RULE_dimensionIntervalList = 42, RULE_dimensionIntervalElement = 43, RULE_dimensionCrsList = 44,
		RULE_dimensionCrsElement = 45, RULE_fieldInterpolationList = 46, RULE_fieldInterpolationListElement = 47,
		RULE_interpolationMethod = 48, RULE_nullResistance = 49, RULE_interpolationType = 50,
		RULE_coverageConstructorExpression = 51, RULE_axisIterator = 52, RULE_intervalExpression = 53,
		RULE_coverageConstantExpression = 54, RULE_axisSpec = 55, RULE_condenseExpression = 56,
		RULE_reduceBooleanExpressionOperator = 57, RULE_reduceNumericalExpressionOperator = 58,
		RULE_reduceBooleanExpression = 59, RULE_reduceNumericalExpression = 60,
		RULE_reduceExpression = 61, RULE_condenseExpressionOperator = 62, RULE_generalCondenseExpression = 63,
		RULE_crsName = 64, RULE_axisName = 65, RULE_constant = 66;
	public static final String[] ruleNames = {
		"wcpsQuery", "forClauseList", "forClause", "whereClause", "returnClause",
		"coverageVariableName", "processingExpression", "scalarExpression", "booleanScalarExpression",
		"booleanUnaryOperator", "booleanConstant", "booleanOperator", "numericalComparissonOperator",
		"stringOperator", "stringScalarExpression", "starExpression", "numericalScalarExpression",
		"complexNumberConstant", "numericalOperator", "numericalUnaryOperation",
		"trigonometricOperator", "getComponentExpression", "coverageIdExpression",
		"describeCoverageExpression", "encodedCoverageExpression", "decodeCoverageExpression",
		"coverageExpression", "coverageArithmeticOperator", "unaryArithmeticExpressionOperator",
		"unaryArithmeticExpression", "trigonometricExpression", "exponentialExpressionOperator",
		"exponentialExpression", "unaryBooleanExpression", "rangeType", "castExpression",
		"fieldName", "rangeConstructorExpression", "crsTransformExpression", "dimensionPointList",
		"dimensionPointElement", "dimensionPointExpression", "dimensionIntervalList",
		"dimensionIntervalElement", "dimensionCrsList", "dimensionCrsElement",
		"fieldInterpolationList", "fieldInterpolationListElement", "interpolationMethod",
		"nullResistance", "interpolationType", "coverageConstructorExpression",
		"axisIterator", "intervalExpression", "coverageConstantExpression", "axisSpec",
		"condenseExpression", "reduceBooleanExpressionOperator", "reduceNumericalExpressionOperator",
		"reduceBooleanExpression", "reduceNumericalExpression", "reduceExpression",
		"condenseExpressionOperator", "generalCondenseExpression", "crsName",
		"axisName", "constant"
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
			setState(134); forClauseList();
			}
			setState(136);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(135); whereClause();
				}
			}

			{
			setState(138); returnClause();
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
			setState(140); match(FOR);
			setState(142);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(141); forClause();
				}
				}
				setState(144);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==IDENTIFIER );
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
		public TerminalNode IDENTIFIER(int i) {
			return getToken(wcpsParser.IDENTIFIER, i);
		}
		public List<TerminalNode> IDENTIFIER() { return getTokens(wcpsParser.IDENTIFIER); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		enterRule(_localctx, 4, RULE_forClause);
		int _la;
		try {
			_localctx = new ForClauseLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(146); coverageVariableName();
			setState(147); match(IN);
			setState(149);
			_la = _input.LA(1);
			if (_la==LEFT_PARANTHESIS) {
				{
				setState(148); match(LEFT_PARANTHESIS);
				}
			}

			setState(151); match(IDENTIFIER);
			setState(156);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(152); match(COMMA);
				setState(153); match(IDENTIFIER);
				}
				}
				setState(158);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(160);
			_la = _input.LA(1);
			if (_la==RIGHT_PARANTHESIS) {
				{
				setState(159); match(RIGHT_PARANTHESIS);
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
		public BooleanScalarExpressionContext booleanScalarExpression() {
			return getRuleContext(BooleanScalarExpressionContext.class,0);
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
		enterRule(_localctx, 6, RULE_whereClause);
		try {
			_localctx = new WhereClauseLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(162); match(WHERE);
			setState(163); booleanScalarExpression(0);
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
		enterRule(_localctx, 8, RULE_returnClause);
		try {
			_localctx = new ReturnClauseLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(165); match(RETURN);
			setState(166); processingExpression();
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
		public TerminalNode IDENTIFIER() { return getToken(wcpsParser.IDENTIFIER, 0); }
		public CoverageVariableNameLabelContext(CoverageVariableNameContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageVariableNameLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoverageVariableNameContext coverageVariableName() throws RecognitionException {
		CoverageVariableNameContext _localctx = new CoverageVariableNameContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_coverageVariableName);
		try {
			_localctx = new CoverageVariableNameLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(168); match(IDENTIFIER);
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
		public EncodedCoverageExpressionContext encodedCoverageExpression() {
			return getRuleContext(EncodedCoverageExpressionContext.class,0);
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
		enterRule(_localctx, 12, RULE_processingExpression);
		try {
			setState(172);
			switch (_input.LA(1)) {
			case ABSOLUTE_VALUE:
			case ADD:
			case ALL:
			case ARCSIN:
			case ARCCOS:
			case ARCTAN:
			case AVG:
			case CONDENSE:
			case COS:
			case COSH:
			case COUNT:
			case DESCRIBE_COVERAGE:
			case FALSE:
			case IMAGINARY_PART:
			case ID:
			case LEFT_PARANTHESIS:
			case MAX:
			case MIN:
			case MINUS:
			case MULTIPLICATION:
			case NOT:
			case PLUS:
			case REAL_PART:
			case ROUND:
			case SIN:
			case SINH:
			case SOME:
			case SQUARE_ROOT:
			case TAN:
			case TANH:
			case TRUE:
			case REAL_NUMBER_CONSTANT:
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(170); scalarExpression();
				}
				break;
			case ENCODE:
				enterOuterAlt(_localctx, 2);
				{
				setState(171); encodedCoverageExpression();
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

	public static class ScalarExpressionContext extends ParserRuleContext {
		public NumericalScalarExpressionContext numericalScalarExpression() {
			return getRuleContext(NumericalScalarExpressionContext.class,0);
		}
		public GetComponentExpressionContext getComponentExpression() {
			return getRuleContext(GetComponentExpressionContext.class,0);
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
		enterRule(_localctx, 14, RULE_scalarExpression);
		try {
			setState(179);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(174); booleanScalarExpression(0);
				}
				break;

			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(175); numericalScalarExpression(0);
				}
				break;

			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(176); stringScalarExpression();
				}
				break;

			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(177); getComponentExpression();
				}
				break;

			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(178); starExpression();
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
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		int _startState = 16;
		enterRecursionRule(_localctx, RULE_booleanScalarExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(201);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				_localctx = new BooleanReduceExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(182); reduceBooleanExpression();
				}
				break;

			case 2:
				{
				_localctx = new BooleanConstantLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(183); booleanConstant();
				}
				break;

			case 3:
				{
				_localctx = new BooleanUnaryScalarLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(184); booleanUnaryOperator();
				setState(186);
				switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
				case 1:
					{
					setState(185); match(LEFT_PARANTHESIS);
					}
					break;
				}
				setState(188); booleanScalarExpression(0);
				setState(190);
				switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
				case 1:
					{
					setState(189); match(RIGHT_PARANTHESIS);
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
				setState(192); numericalScalarExpression(0);
				setState(193); numericalComparissonOperator();
				setState(194); numericalScalarExpression(0);
				}
				break;

			case 5:
				{
				_localctx = new BooleanReduceExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(196); reduceBooleanExpression();
				}
				break;

			case 6:
				{
				_localctx = new BooleanStringComparisonScalarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(197); stringScalarExpression();
				setState(198); stringOperator();
				setState(199); stringScalarExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(209);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new BooleanBinaryScalarLabelContext(new BooleanScalarExpressionContext(_parentctx, _parentState, _p));
					pushNewRecursionContext(_localctx, _startState, RULE_booleanScalarExpression);
					setState(203);
					if (!(4 >= _localctx._p)) throw new FailedPredicateException(this, "4 >= $_p");
					setState(204); booleanOperator();
					setState(205); booleanScalarExpression(0);
					}
					}
				}
				setState(211);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
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
		enterRule(_localctx, 18, RULE_booleanUnaryOperator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212); match(NOT);
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
		enterRule(_localctx, 20, RULE_booleanConstant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
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
		enterRule(_localctx, 22, RULE_booleanOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(216);
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
		enterRule(_localctx, 24, RULE_numericalComparissonOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(218);
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
		enterRule(_localctx, 26, RULE_stringOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(220);
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
		enterRule(_localctx, 28, RULE_stringScalarExpression);
		try {
			_localctx = new StringScalarExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(222); match(STRING_LITERAL);
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
		enterRule(_localctx, 30, RULE_starExpression);
		try {
			_localctx = new StarExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(224); match(MULTIPLICATION);
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
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		public TerminalNode REAL_NUMBER_CONSTANT() { return getToken(wcpsParser.REAL_NUMBER_CONSTANT, 0); }
		public NumericalRealNumberExpressionLabelContext(NumericalScalarExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNumericalRealNumberExpressionLabel(this);
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
		int _startState = 32;
		enterRecursionRule(_localctx, RULE_numericalScalarExpression);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(240);
			switch (_input.LA(1)) {
			case ABSOLUTE_VALUE:
			case IMAGINARY_PART:
			case MINUS:
			case PLUS:
			case REAL_PART:
			case ROUND:
			case SQUARE_ROOT:
				{
				_localctx = new NumericalUnaryScalarExpressionLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(227); numericalUnaryOperation();
				setState(228); match(LEFT_PARANTHESIS);
				setState(229); numericalScalarExpression(0);
				setState(230); match(RIGHT_PARANTHESIS);
				}
				break;
			case ARCSIN:
			case ARCCOS:
			case ARCTAN:
			case COS:
			case COSH:
			case SIN:
			case SINH:
			case TAN:
			case TANH:
				{
				_localctx = new NumericalTrigonometricScalarExpressionLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(232); trigonometricOperator();
				setState(233); match(LEFT_PARANTHESIS);
				setState(234); numericalScalarExpression(0);
				setState(235); match(RIGHT_PARANTHESIS);
				}
				break;
			case ADD:
			case ALL:
			case AVG:
			case CONDENSE:
			case COUNT:
			case MAX:
			case MIN:
			case SOME:
				{
				_localctx = new NumericalCondenseExpressionLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(237); condenseExpression();
				}
				break;
			case REAL_NUMBER_CONSTANT:
				{
				_localctx = new NumericalRealNumberExpressionLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(238); match(REAL_NUMBER_CONSTANT);
				}
				break;
			case LEFT_PARANTHESIS:
				{
				_localctx = new NumericalComplexNumberConstantContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(239); complexNumberConstant();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(248);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new NumericalBinaryScalarExpressionLabelContext(new NumericalScalarExpressionContext(_parentctx, _parentState, _p));
					pushNewRecursionContext(_localctx, _startState, RULE_numericalScalarExpression);
					setState(242);
					if (!(4 >= _localctx._p)) throw new FailedPredicateException(this, "4 >= $_p");
					setState(243); numericalOperator();
					setState(244); numericalScalarExpression(0);
					}
					}
				}
				setState(250);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
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
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode REAL_NUMBER_CONSTANT(int i) {
			return getToken(wcpsParser.REAL_NUMBER_CONSTANT, i);
		}
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public ComplexNumberConstantLabelContext(ComplexNumberConstantContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitComplexNumberConstantLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComplexNumberConstantContext complexNumberConstant() throws RecognitionException {
		ComplexNumberConstantContext _localctx = new ComplexNumberConstantContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_complexNumberConstant);
		try {
			_localctx = new ComplexNumberConstantLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(251); match(LEFT_PARANTHESIS);
			setState(252); match(REAL_NUMBER_CONSTANT);
			setState(253); match(COMMA);
			setState(254); match(REAL_NUMBER_CONSTANT);
			setState(255); match(RIGHT_PARANTHESIS);
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
		enterRule(_localctx, 36, RULE_numericalOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(257);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DIVISION) | (1L << MINUS) | (1L << MULTIPLICATION) | (1L << PLUS))) != 0)) ) {
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
		enterRule(_localctx, 38, RULE_numericalUnaryOperation);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(259);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ABSOLUTE_VALUE) | (1L << IMAGINARY_PART) | (1L << MINUS) | (1L << PLUS) | (1L << REAL_PART) | (1L << ROUND))) != 0) || _la==SQUARE_ROOT) ) {
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
		enterRule(_localctx, 40, RULE_trigonometricOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(261);
			_la = _input.LA(1);
			if ( !(((((_la - 6)) & ~0x3f) == 0 && ((1L << (_la - 6)) & ((1L << (ARCSIN - 6)) | (1L << (ARCCOS - 6)) | (1L << (ARCTAN - 6)) | (1L << (COS - 6)) | (1L << (COSH - 6)) | (1L << (SIN - 6)) | (1L << (SINH - 6)) | (1L << (TAN - 6)) | (1L << (TANH - 6)))) != 0)) ) {
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
		public CoverageIdExpressionContext coverageIdExpression() {
			return getRuleContext(CoverageIdExpressionContext.class,0);
		}
		public DescribeCoverageExpressionContext describeCoverageExpression() {
			return getRuleContext(DescribeCoverageExpressionContext.class,0);
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
		enterRule(_localctx, 42, RULE_getComponentExpression);
		try {
			setState(265);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(263); coverageIdExpression();
				}
				break;
			case DESCRIBE_COVERAGE:
				enterOuterAlt(_localctx, 2);
				{
				setState(264); describeCoverageExpression();
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

	public static class CoverageIdExpressionContext extends ParserRuleContext {
		public CoverageIdExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_coverageIdExpression; }

		public CoverageIdExpressionContext() { }
		public void copyFrom(CoverageIdExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CoverageIdExpressionLabelContext extends CoverageIdExpressionContext {
		public TerminalNode ID() { return getToken(wcpsParser.ID, 0); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public CoverageVariableNameContext coverageVariableName() {
			return getRuleContext(CoverageVariableNameContext.class,0);
		}
		public CoverageIdExpressionLabelContext(CoverageIdExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageIdExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoverageIdExpressionContext coverageIdExpression() throws RecognitionException {
		CoverageIdExpressionContext _localctx = new CoverageIdExpressionContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_coverageIdExpression);
		try {
			_localctx = new CoverageIdExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(267); match(ID);
			setState(268); match(LEFT_PARANTHESIS);
			setState(269); coverageVariableName();
			setState(270); match(RIGHT_PARANTHESIS);
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
		public TerminalNode DESCRIBE_COVERAGE() { return getToken(wcpsParser.DESCRIBE_COVERAGE, 0); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public CoverageVariableNameContext coverageVariableName() {
			return getRuleContext(CoverageVariableNameContext.class,0);
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
		enterRule(_localctx, 46, RULE_describeCoverageExpression);
		try {
			_localctx = new DescribeCoverageExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(272); match(DESCRIBE_COVERAGE);
			setState(273); match(LEFT_PARANTHESIS);
			setState(274); coverageVariableName();
			setState(275); match(RIGHT_PARANTHESIS);
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
		public TerminalNode STRING_LITERAL() { return getToken(wcpsParser.STRING_LITERAL, 0); }
		public TerminalNode FORMAT_NAME() { return getToken(wcpsParser.FORMAT_NAME, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		enterRule(_localctx, 48, RULE_encodedCoverageExpression);
		int _la;
		try {
			_localctx = new EncodedCoverageExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(277); match(ENCODE);
			setState(278); match(LEFT_PARANTHESIS);
			setState(279); coverageExpression(0);
			setState(280); match(COMMA);
			setState(281); match(FORMAT_NAME);
			setState(283);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(282); match(COMMA);
				}
			}

			setState(286);
			_la = _input.LA(1);
			if (_la==STRING_LITERAL) {
				{
				setState(285); match(STRING_LITERAL);
				}
			}

			setState(288); match(RIGHT_PARANTHESIS);
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
		public TerminalNode STRING_LITERAL(int i) {
			return getToken(wcpsParser.STRING_LITERAL, i);
		}
		public TerminalNode FORMAT_NAME() { return getToken(wcpsParser.FORMAT_NAME, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<TerminalNode> STRING_LITERAL() { return getTokens(wcpsParser.STRING_LITERAL); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public TerminalNode DECODE() { return getToken(wcpsParser.DECODE, 0); }
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public DecodedCoverageExpressionLabelContext(DecodeCoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitDecodedCoverageExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DecodeCoverageExpressionContext decodeCoverageExpression() throws RecognitionException {
		DecodeCoverageExpressionContext _localctx = new DecodeCoverageExpressionContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_decodeCoverageExpression);
		int _la;
		try {
			_localctx = new DecodedCoverageExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(290); match(DECODE);
			setState(291); match(LEFT_PARANTHESIS);
			setState(292); match(STRING_LITERAL);
			setState(293); match(COMMA);
			setState(294); match(FORMAT_NAME);
			setState(296);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(295); match(COMMA);
				}
			}

			setState(299);
			_la = _input.LA(1);
			if (_la==STRING_LITERAL) {
				{
				setState(298); match(STRING_LITERAL);
				}
			}

			setState(301); match(RIGHT_PARANTHESIS);
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
	public static class CoverageExpressionShorthandTrimLabelContext extends CoverageExpressionContext {
		public DimensionIntervalListContext dimensionIntervalList() {
			return getRuleContext(DimensionIntervalListContext.class,0);
		}
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode LEFT_BRACKET() { return getToken(wcpsParser.LEFT_BRACKET, 0); }
		public TerminalNode RIGHT_BRACKET() { return getToken(wcpsParser.RIGHT_BRACKET, 0); }
		public CoverageExpressionShorthandTrimLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionShorthandTrimLabel(this);
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
	public static class CoverageExpressionExtendLabelContext extends CoverageExpressionContext {
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public DimensionIntervalListContext dimensionIntervalList() {
			return getRuleContext(DimensionIntervalListContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
	public static class CoverageExpressionCoverageLabelContext extends CoverageExpressionContext {
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
	public static class CoverageExpressionSliceLabelContext extends CoverageExpressionContext {
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public DimensionPointListContext dimensionPointList() {
			return getRuleContext(DimensionPointListContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode SLICE() { return getToken(wcpsParser.SLICE, 0); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
	public static class CoverageExpressionTrimCoverageLabelContext extends CoverageExpressionContext {
		public DimensionIntervalListContext dimensionIntervalList() {
			return getRuleContext(DimensionIntervalListContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode TRIM() { return getToken(wcpsParser.TRIM, 0); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
	public static class CoverageExpressionScaleLabelContext extends CoverageExpressionContext {
		public TerminalNode SCALE() { return getToken(wcpsParser.SCALE, 0); }
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public List<FieldInterpolationListContext> fieldInterpolationList() {
			return getRuleContexts(FieldInterpolationListContext.class);
		}
		public DimensionIntervalListContext dimensionIntervalList() {
			return getRuleContext(DimensionIntervalListContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public FieldInterpolationListContext fieldInterpolationList(int i) {
			return getRuleContext(FieldInterpolationListContext.class,i);
		}
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public CoverageExpressionScaleLabelContext(CoverageExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageExpressionScaleLabel(this);
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

	public final CoverageExpressionContext coverageExpression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		CoverageExpressionContext _localctx = new CoverageExpressionContext(_ctx, _parentState, _p);
		CoverageExpressionContext _prevctx = _localctx;
		int _startState = 52;
		enterRecursionRule(_localctx, RULE_coverageExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(361);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				_localctx = new CoverageExpressionVariableNameLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(304); coverageVariableName();
				}
				break;

			case 2:
				{
				_localctx = new CoverageExpressionScalarLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(305); scalarExpression();
				}
				break;

			case 3:
				{
				_localctx = new CoverageExpressionConstantLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(306); coverageConstantExpression();
				}
				break;

			case 4:
				{
				_localctx = new CoverageExpressionConstructorLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(307); coverageConstructorExpression();
				}
				break;

			case 5:
				{
				_localctx = new CoverageExpressionDecodeLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(308); decodeCoverageExpression();
				}
				break;

			case 6:
				{
				_localctx = new CoverageExpressionTrimCoverageLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(309); match(TRIM);
				setState(310); match(LEFT_PARANTHESIS);
				setState(311); coverageExpression(0);
				setState(312); match(COMMA);
				setState(313); dimensionIntervalList();
				setState(314); match(RIGHT_PARANTHESIS);
				}
				break;

			case 7:
				{
				_localctx = new CoverageExpressionSliceLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(316); match(SLICE);
				setState(317); match(LEFT_PARANTHESIS);
				setState(318); coverageExpression(0);
				setState(319); match(COMMA);
				setState(320); match(LEFT_BRACE);
				setState(321); dimensionPointList();
				setState(322); match(RIGHT_BRACE);
				setState(323); match(RIGHT_PARANTHESIS);
				}
				break;

			case 8:
				{
				_localctx = new CoverageExpressionExtendLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(325); match(EXTEND);
				setState(326); match(LEFT_PARANTHESIS);
				setState(327); coverageExpression(0);
				setState(328); match(COMMA);
				setState(329); match(LEFT_BRACE);
				setState(330); dimensionIntervalList();
				setState(331); match(RIGHT_BRACE);
				setState(332); match(RIGHT_PARANTHESIS);
				}
				break;

			case 9:
				{
				_localctx = new CoverageExpressionUnaryArithmeticLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(334); unaryArithmeticExpression();
				}
				break;

			case 10:
				{
				_localctx = new CoverageExpressionTrigonometricLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(335); trigonometricExpression();
				}
				break;

			case 11:
				{
				_localctx = new CoverageExpressionExponentialLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(336); exponentialExpression();
				}
				break;

			case 12:
				{
				_localctx = new CoverageExpressionUnaryBooleanLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(337); unaryBooleanExpression();
				}
				break;

			case 13:
				{
				_localctx = new CoverageExpressionCastLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(338); castExpression();
				}
				break;

			case 14:
				{
				_localctx = new CoverageExpressionRangeConstructorLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(339); rangeConstructorExpression();
				}
				break;

			case 15:
				{
				_localctx = new CoverageExpressionCrsTransformLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(340); crsTransformExpression();
				}
				break;

			case 16:
				{
				_localctx = new CoverageExpressionScaleLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(341); match(SCALE);
				setState(342); match(LEFT_PARANTHESIS);
				setState(343); coverageExpression(0);
				setState(344); match(COMMA);
				setState(345); match(LEFT_BRACE);
				setState(346); dimensionIntervalList();
				setState(347); match(RIGHT_BRACE);
				setState(352);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(348); match(COMMA);
					setState(349); fieldInterpolationList();
					}
					}
					setState(354);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(355); match(RIGHT_PARANTHESIS);
				}
				break;

			case 17:
				{
				_localctx = new CoverageExpressionCoverageLabelContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(357); match(LEFT_PARANTHESIS);
				setState(358); coverageExpression(0);
				setState(359); match(RIGHT_PARANTHESIS);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(390);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			while ( _alt!=2 && _alt!=-1 ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(388);
					switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
					case 1:
						{
						_localctx = new CoverageExpressionLogicLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(363);
						if (!(23 >= _localctx._p)) throw new FailedPredicateException(this, "23 >= $_p");
						setState(364); booleanOperator();
						setState(365); coverageExpression(0);
						}
						break;

					case 2:
						{
						_localctx = new CoverageExpressionArithmeticLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(367);
						if (!(22 >= _localctx._p)) throw new FailedPredicateException(this, "22 >= $_p");
						setState(368); coverageArithmeticOperator();
						setState(369); coverageExpression(0);
						}
						break;

					case 3:
						{
						_localctx = new CoverageExpressionComparissonLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(371);
						if (!(21 >= _localctx._p)) throw new FailedPredicateException(this, "21 >= $_p");
						setState(372); numericalComparissonOperator();
						setState(373); coverageExpression(0);
						}
						break;

					case 4:
						{
						_localctx = new CoverageExpressionShorthandTrimLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(375);
						if (!(15 >= _localctx._p)) throw new FailedPredicateException(this, "15 >= $_p");
						setState(376); match(LEFT_BRACKET);
						setState(377); dimensionIntervalList();
						setState(378); match(RIGHT_BRACKET);
						}
						break;

					case 5:
						{
						_localctx = new CoverageExpressionShorthandSliceLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(380);
						if (!(13 >= _localctx._p)) throw new FailedPredicateException(this, "13 >= $_p");
						setState(381); match(LEFT_BRACKET);
						setState(382); dimensionPointList();
						setState(383); match(RIGHT_BRACKET);
						}
						break;

					case 6:
						{
						_localctx = new CoverageExpressionRangeSubsettingLabelContext(new CoverageExpressionContext(_parentctx, _parentState, _p));
						pushNewRecursionContext(_localctx, _startState, RULE_coverageExpression);
						setState(385);
						if (!(5 >= _localctx._p)) throw new FailedPredicateException(this, "5 >= $_p");
						setState(386); match(DOT);
						setState(387); fieldName();
						}
						break;
					}
					}
				}
				setState(392);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
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
		public TerminalNode OVERLAY() { return getToken(wcpsParser.OVERLAY, 0); }
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
		enterRule(_localctx, 54, RULE_coverageArithmeticOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(393);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DIVISION) | (1L << MINUS) | (1L << MULTIPLICATION) | (1L << OVERLAY) | (1L << PLUS))) != 0)) ) {
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
		public TerminalNode MINUS() { return getToken(wcpsParser.MINUS, 0); }
		public TerminalNode REAL_PART() { return getToken(wcpsParser.REAL_PART, 0); }
		public TerminalNode PLUS() { return getToken(wcpsParser.PLUS, 0); }
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
		enterRule(_localctx, 56, RULE_unaryArithmeticExpressionOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(395);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ABSOLUTE_VALUE) | (1L << IMAGINARY_PART) | (1L << MINUS) | (1L << PLUS) | (1L << REAL_PART))) != 0) || _la==SQUARE_ROOT) ) {
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
		public UnaryArithmeticExpressionOperatorContext unaryArithmeticExpressionOperator() {
			return getRuleContext(UnaryArithmeticExpressionOperatorContext.class,0);
		}
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		enterRule(_localctx, 58, RULE_unaryArithmeticExpression);
		try {
			_localctx = new UnaryCoverageArithmeticExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(397); unaryArithmeticExpressionOperator();
			setState(398); match(LEFT_PARANTHESIS);
			setState(399); coverageExpression(0);
			setState(400); match(RIGHT_PARANTHESIS);
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
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		enterRule(_localctx, 60, RULE_trigonometricExpression);
		try {
			_localctx = new TrigonometricExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(402); trigonometricOperator();
			setState(403); match(LEFT_PARANTHESIS);
			setState(404); coverageExpression(0);
			setState(405); match(RIGHT_PARANTHESIS);
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
		enterRule(_localctx, 62, RULE_exponentialExpressionOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(407);
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
		public ExponentialExpressionOperatorContext exponentialExpressionOperator() {
			return getRuleContext(ExponentialExpressionOperatorContext.class,0);
		}
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		enterRule(_localctx, 64, RULE_exponentialExpression);
		try {
			_localctx = new ExponentialExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(409); exponentialExpressionOperator();
			setState(410); match(LEFT_PARANTHESIS);
			setState(411); coverageExpression(0);
			setState(412); match(RIGHT_PARANTHESIS);
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
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		public TerminalNode NOT() { return getToken(wcpsParser.NOT, 0); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		enterRule(_localctx, 66, RULE_unaryBooleanExpression);
		try {
			setState(426);
			switch (_input.LA(1)) {
			case NOT:
				_localctx = new NotUnaryBooleanExpressionLabelContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(414); match(NOT);
				setState(415); match(LEFT_PARANTHESIS);
				setState(416); coverageExpression(0);
				setState(417); match(RIGHT_PARANTHESIS);
				}
				break;
			case BIT:
				_localctx = new BitUnaryBooleanExpressionLabelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(419); match(BIT);
				setState(420); match(LEFT_PARANTHESIS);
				setState(421); coverageExpression(0);
				setState(422); match(COMMA);
				setState(423); numericalScalarExpression(0);
				setState(424); match(RIGHT_PARANTHESIS);
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
		public TerminalNode IDENTIFIER(int i) {
			return getToken(wcpsParser.IDENTIFIER, i);
		}
		public List<TerminalNode> IDENTIFIER() { return getTokens(wcpsParser.IDENTIFIER); }
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
		enterRule(_localctx, 68, RULE_rangeType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(428); match(IDENTIFIER);
			setState(432);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IDENTIFIER) {
				{
				{
				setState(429); match(IDENTIFIER);
				}
				}
				setState(434);
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
		public RangeTypeContext rangeType() {
			return getRuleContext(RangeTypeContext.class,0);
		}
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		enterRule(_localctx, 70, RULE_castExpression);
		try {
			_localctx = new CastExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(435); match(LEFT_PARANTHESIS);
			setState(436); rangeType();
			setState(437); match(RIGHT_PARANTHESIS);
			setState(438); coverageExpression(0);
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
		public TerminalNode REAL_NUMBER_CONSTANT() { return getToken(wcpsParser.REAL_NUMBER_CONSTANT, 0); }
		public TerminalNode IDENTIFIER() { return getToken(wcpsParser.IDENTIFIER, 0); }
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
		enterRule(_localctx, 72, RULE_fieldName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(440);
			_la = _input.LA(1);
			if ( !(_la==REAL_NUMBER_CONSTANT || _la==IDENTIFIER) ) {
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
		enterRule(_localctx, 74, RULE_rangeConstructorExpression);
		int _la;
		try {
			_localctx = new RangeConstructorExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(442); match(LEFT_BRACE);
			{
			setState(443); fieldName();
			setState(444); match(COLON);
			setState(445); coverageExpression(0);
			}
			setState(454);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMICOLON) {
				{
				{
				setState(447); match(SEMICOLON);
				setState(448); fieldName();
				setState(449); match(COLON);
				setState(450); coverageExpression(0);
				}
				}
				setState(456);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(457); match(RIGHT_BRACE);
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
		public FieldInterpolationListContext fieldInterpolationList() {
			return getRuleContext(FieldInterpolationListContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode CRS_TRANSFORM() { return getToken(wcpsParser.CRS_TRANSFORM, 0); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public CoverageExpressionContext coverageExpression() {
			return getRuleContext(CoverageExpressionContext.class,0);
		}
		public DimensionCrsListContext dimensionCrsList() {
			return getRuleContext(DimensionCrsListContext.class,0);
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
		enterRule(_localctx, 76, RULE_crsTransformExpression);
		try {
			_localctx = new CrsTransformExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(459); match(CRS_TRANSFORM);
			setState(460); match(LEFT_PARANTHESIS);
			setState(461); coverageExpression(0);
			setState(462); match(COMMA);
			setState(463); dimensionCrsList();
			setState(464); match(COMMA);
			setState(465); fieldInterpolationList();
			setState(466); match(RIGHT_PARANTHESIS);
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
		enterRule(_localctx, 78, RULE_dimensionPointList);
		int _la;
		try {
			_localctx = new DimensionPointListLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(468); dimensionPointElement();
			setState(473);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(469); match(COMMA);
				setState(470); dimensionPointElement();
				}
				}
				setState(475);
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
		public DimensionPointExpressionContext dimensionPointExpression() {
			return getRuleContext(DimensionPointExpressionContext.class,0);
		}
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		enterRule(_localctx, 80, RULE_dimensionPointElement);
		int _la;
		try {
			_localctx = new DimensionPointElementLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(476); axisName();
			setState(479);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(477); match(COLON);
				setState(478); crsName();
				}
			}

			setState(481); match(LEFT_PARANTHESIS);
			setState(482); dimensionPointExpression();
			setState(483); match(RIGHT_PARANTHESIS);
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

	public static class DimensionPointExpressionContext extends ParserRuleContext {
		public TerminalNode REAL_NUMBER_CONSTANT() { return getToken(wcpsParser.REAL_NUMBER_CONSTANT, 0); }
		public TerminalNode TRUE() { return getToken(wcpsParser.TRUE, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(wcpsParser.STRING_LITERAL, 0); }
		public TerminalNode FALSE() { return getToken(wcpsParser.FALSE, 0); }
		public DimensionPointExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dimensionPointExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitDimensionPointExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DimensionPointExpressionContext dimensionPointExpression() throws RecognitionException {
		DimensionPointExpressionContext _localctx = new DimensionPointExpressionContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_dimensionPointExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(485);
			_la = _input.LA(1);
			if ( !(((((_la - 28)) & ~0x3f) == 0 && ((1L << (_la - 28)) & ((1L << (FALSE - 28)) | (1L << (TRUE - 28)) | (1L << (REAL_NUMBER_CONSTANT - 28)) | (1L << (STRING_LITERAL - 28)))) != 0)) ) {
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
		enterRule(_localctx, 84, RULE_dimensionIntervalList);
		int _la;
		try {
			_localctx = new DimensionIntervalListLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(487); dimensionIntervalElement();
			setState(492);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(488); match(COMMA);
				setState(489); dimensionIntervalElement();
				}
				}
				setState(494);
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
		public ScalarExpressionContext scalarExpression(int i) {
			return getRuleContext(ScalarExpressionContext.class,i);
		}
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode COLON(int i) {
			return getToken(wcpsParser.COLON, i);
		}
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public List<TerminalNode> COLON() { return getTokens(wcpsParser.COLON); }
		public List<ScalarExpressionContext> scalarExpression() {
			return getRuleContexts(ScalarExpressionContext.class);
		}
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
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public TerminalNode COLON() { return getToken(wcpsParser.COLON, 0); }
		public ScalarExpressionContext scalarExpression() {
			return getRuleContext(ScalarExpressionContext.class,0);
		}
		public SliceDimensionIntervalElementLabelContext(DimensionIntervalElementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitSliceDimensionIntervalElementLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DimensionIntervalElementContext dimensionIntervalElement() throws RecognitionException {
		DimensionIntervalElementContext _localctx = new DimensionIntervalElementContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_dimensionIntervalElement);
		int _la;
		try {
			setState(515);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				_localctx = new TrimDimensionIntervalElementLabelContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(495); axisName();
				setState(498);
				_la = _input.LA(1);
				if (_la==COLON) {
					{
					setState(496); match(COLON);
					setState(497); crsName();
					}
				}

				setState(500); match(LEFT_PARANTHESIS);
				setState(501); scalarExpression();
				setState(502); match(COLON);
				setState(503); scalarExpression();
				setState(504); match(RIGHT_PARANTHESIS);
				}
				break;

			case 2:
				_localctx = new SliceDimensionIntervalElementLabelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(506); axisName();
				setState(509);
				_la = _input.LA(1);
				if (_la==COLON) {
					{
					setState(507); match(COLON);
					setState(508); crsName();
					}
				}

				setState(511); match(LEFT_PARANTHESIS);
				setState(512); scalarExpression();
				setState(513); match(RIGHT_PARANTHESIS);
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
		enterRule(_localctx, 88, RULE_dimensionCrsList);
		int _la;
		try {
			_localctx = new DimensionCrsListLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(517); match(LEFT_BRACE);
			setState(518); dimensionCrsElement();
			setState(523);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(519); match(COMMA);
				setState(520); dimensionCrsElement();
				}
				}
				setState(525);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(526); match(RIGHT_BRACE);
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
		enterRule(_localctx, 90, RULE_dimensionCrsElement);
		try {
			_localctx = new DimensionCrsElementLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(528); axisName();
			setState(529); match(COLON);
			setState(530); crsName();
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

	public static class FieldInterpolationListContext extends ParserRuleContext {
		public FieldInterpolationListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldInterpolationList; }

		public FieldInterpolationListContext() { }
		public void copyFrom(FieldInterpolationListContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class FieldInterpolationListLabelContext extends FieldInterpolationListContext {
		public TerminalNode RIGHT_BRACE() { return getToken(wcpsParser.RIGHT_BRACE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode LEFT_BRACE() { return getToken(wcpsParser.LEFT_BRACE, 0); }
		public FieldInterpolationListElementContext fieldInterpolationListElement(int i) {
			return getRuleContext(FieldInterpolationListElementContext.class,i);
		}
		public List<FieldInterpolationListElementContext> fieldInterpolationListElement() {
			return getRuleContexts(FieldInterpolationListElementContext.class);
		}
		public TerminalNode COMMA(int i) {
			return getToken(wcpsParser.COMMA, i);
		}
		public FieldInterpolationListLabelContext(FieldInterpolationListContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitFieldInterpolationListLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldInterpolationListContext fieldInterpolationList() throws RecognitionException {
		FieldInterpolationListContext _localctx = new FieldInterpolationListContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_fieldInterpolationList);
		int _la;
		try {
			_localctx = new FieldInterpolationListLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(532); match(LEFT_BRACE);
			setState(533); fieldInterpolationListElement();
			setState(538);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(534); match(COMMA);
				setState(535); fieldInterpolationListElement();
				}
				}
				setState(540);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(541); match(RIGHT_BRACE);
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

	public static class FieldInterpolationListElementContext extends ParserRuleContext {
		public FieldInterpolationListElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldInterpolationListElement; }

		public FieldInterpolationListElementContext() { }
		public void copyFrom(FieldInterpolationListElementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class FieldInterpolationListElementLabelContext extends FieldInterpolationListElementContext {
		public FieldNameContext fieldName() {
			return getRuleContext(FieldNameContext.class,0);
		}
		public InterpolationMethodContext interpolationMethod() {
			return getRuleContext(InterpolationMethodContext.class,0);
		}
		public FieldInterpolationListElementLabelContext(FieldInterpolationListElementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitFieldInterpolationListElementLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldInterpolationListElementContext fieldInterpolationListElement() throws RecognitionException {
		FieldInterpolationListElementContext _localctx = new FieldInterpolationListElementContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_fieldInterpolationListElement);
		try {
			_localctx = new FieldInterpolationListElementLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(543); fieldName();
			setState(544); interpolationMethod();
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

	public static class InterpolationMethodContext extends ParserRuleContext {
		public InterpolationMethodContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interpolationMethod; }

		public InterpolationMethodContext() { }
		public void copyFrom(InterpolationMethodContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class InterpolationMethodLabelContext extends InterpolationMethodContext {
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public NullResistanceContext nullResistance() {
			return getRuleContext(NullResistanceContext.class,0);
		}
		public TerminalNode COLON() { return getToken(wcpsParser.COLON, 0); }
		public InterpolationTypeContext interpolationType() {
			return getRuleContext(InterpolationTypeContext.class,0);
		}
		public InterpolationMethodLabelContext(InterpolationMethodContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitInterpolationMethodLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterpolationMethodContext interpolationMethod() throws RecognitionException {
		InterpolationMethodContext _localctx = new InterpolationMethodContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_interpolationMethod);
		try {
			_localctx = new InterpolationMethodLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(546); match(LEFT_PARANTHESIS);
			setState(547); interpolationType();
			setState(548); match(COLON);
			setState(549); nullResistance();
			setState(550); match(RIGHT_PARANTHESIS);
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

	public static class NullResistanceContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(wcpsParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(wcpsParser.FALSE, 0); }
		public NullResistanceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullResistance; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitNullResistance(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NullResistanceContext nullResistance() throws RecognitionException {
		NullResistanceContext _localctx = new NullResistanceContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_nullResistance);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(552);
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
		public TerminalNode STRING_LITERAL() { return getToken(wcpsParser.STRING_LITERAL, 0); }
		public InterpolationTypeLabelContext(InterpolationTypeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitInterpolationTypeLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InterpolationTypeContext interpolationType() throws RecognitionException {
		InterpolationTypeContext _localctx = new InterpolationTypeContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_interpolationType);
		try {
			_localctx = new InterpolationTypeLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(554); match(STRING_LITERAL);
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
		public TerminalNode IDENTIFIER() { return getToken(wcpsParser.IDENTIFIER, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public List<AxisIteratorContext> axisIterator() {
			return getRuleContexts(AxisIteratorContext.class);
		}
		public TerminalNode COVERAGE() { return getToken(wcpsParser.COVERAGE, 0); }
		public TerminalNode VALUES() { return getToken(wcpsParser.VALUES, 0); }
		public ScalarExpressionContext scalarExpression() {
			return getRuleContext(ScalarExpressionContext.class,0);
		}
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
		enterRule(_localctx, 102, RULE_coverageConstructorExpression);
		int _la;
		try {
			_localctx = new CoverageConstructorExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(556); match(COVERAGE);
			setState(557); match(IDENTIFIER);
			setState(558); match(OVER);
			setState(559); axisIterator();
			setState(564);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(560); match(COMMA);
				setState(561); axisIterator();
				}
				}
				setState(566);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(567); match(VALUES);
			setState(568); scalarExpression();
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
	public static class AxisIteratorLabelContext extends AxisIteratorContext {
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(wcpsParser.IDENTIFIER, 0); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public IntervalExpressionContext intervalExpression() {
			return getRuleContext(IntervalExpressionContext.class,0);
		}
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public AxisIteratorLabelContext(AxisIteratorContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitAxisIteratorLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AxisIteratorContext axisIterator() throws RecognitionException {
		AxisIteratorContext _localctx = new AxisIteratorContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_axisIterator);
		try {
			_localctx = new AxisIteratorLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(570); match(IDENTIFIER);
			setState(571); axisName();
			setState(572); match(LEFT_PARANTHESIS);
			setState(573); intervalExpression();
			setState(574); match(RIGHT_PARANTHESIS);
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
	public static class CRSIntervalExpressionLabelContext extends IntervalExpressionContext {
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(wcpsParser.COMMA, 0); }
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public CoverageVariableNameContext coverageVariableName() {
			return getRuleContext(CoverageVariableNameContext.class,0);
		}
		public TerminalNode IMGCRSDOMAIN() { return getToken(wcpsParser.IMGCRSDOMAIN, 0); }
		public CRSIntervalExpressionLabelContext(IntervalExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCRSIntervalExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalExpressionContext intervalExpression() throws RecognitionException {
		IntervalExpressionContext _localctx = new IntervalExpressionContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_intervalExpression);
		try {
			setState(587);
			switch (_input.LA(1)) {
			case ABSOLUTE_VALUE:
			case ADD:
			case ALL:
			case ARCSIN:
			case ARCCOS:
			case ARCTAN:
			case AVG:
			case CONDENSE:
			case COS:
			case COSH:
			case COUNT:
			case DESCRIBE_COVERAGE:
			case FALSE:
			case IMAGINARY_PART:
			case ID:
			case LEFT_PARANTHESIS:
			case MAX:
			case MIN:
			case MINUS:
			case MULTIPLICATION:
			case NOT:
			case PLUS:
			case REAL_PART:
			case ROUND:
			case SIN:
			case SINH:
			case SOME:
			case SQUARE_ROOT:
			case TAN:
			case TANH:
			case TRUE:
			case REAL_NUMBER_CONSTANT:
			case STRING_LITERAL:
				_localctx = new IntervalExpressionLabelContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(576); scalarExpression();
				setState(577); match(COLON);
				setState(578); scalarExpression();
				}
				break;
			case IMGCRSDOMAIN:
				_localctx = new CRSIntervalExpressionLabelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(580); match(IMGCRSDOMAIN);
				setState(581); match(LEFT_PARANTHESIS);
				setState(582); coverageVariableName();
				setState(583); match(COMMA);
				setState(584); axisName();
				setState(585); match(RIGHT_PARANTHESIS);
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
		public TerminalNode IDENTIFIER() { return getToken(wcpsParser.IDENTIFIER, 0); }
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode COVERAGE() { return getToken(wcpsParser.COVERAGE, 0); }
		public CoverageConstantExpressionLabelContext(CoverageConstantExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitCoverageConstantExpressionLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CoverageConstantExpressionContext coverageConstantExpression() throws RecognitionException {
		CoverageConstantExpressionContext _localctx = new CoverageConstantExpressionContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_coverageConstantExpression);
		int _la;
		try {
			_localctx = new CoverageConstantExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(589); match(COVERAGE);
			setState(590); match(IDENTIFIER);
			setState(591); match(OVER);
			setState(592); axisIterator();
			setState(597);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(593); match(COMMA);
				setState(594); axisIterator();
				}
				}
				setState(599);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(600); match(VALUE);
			setState(601); match(LIST);
			setState(602); match(LOWER_THAN);
			setState(603); constant();
			setState(608);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEMICOLON) {
				{
				{
				setState(604); match(SEMICOLON);
				setState(605); constant();
				}
				}
				setState(610);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(611); match(GREATER_THAN);
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
		public AxisNameContext axisName() {
			return getRuleContext(AxisNameContext.class,0);
		}
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public IntervalExpressionContext intervalExpression() {
			return getRuleContext(IntervalExpressionContext.class,0);
		}
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
		public AxisSpecLabelContext(AxisSpecContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof wcpsVisitor ) return ((wcpsVisitor<? extends T>)visitor).visitAxisSpecLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AxisSpecContext axisSpec() throws RecognitionException {
		AxisSpecContext _localctx = new AxisSpecContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_axisSpec);
		try {
			_localctx = new AxisSpecLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(613); axisName();
			setState(614); match(LEFT_PARANTHESIS);
			setState(615); intervalExpression();
			setState(616); match(RIGHT_PARANTHESIS);
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
		enterRule(_localctx, 112, RULE_condenseExpression);
		try {
			setState(620);
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
				setState(618); reduceExpression();
				}
				break;
			case CONDENSE:
				enterOuterAlt(_localctx, 2);
				{
				setState(619); generalCondenseExpression();
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
		enterRule(_localctx, 114, RULE_reduceBooleanExpressionOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(622);
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
		enterRule(_localctx, 116, RULE_reduceNumericalExpressionOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(624);
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
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		enterRule(_localctx, 118, RULE_reduceBooleanExpression);
		try {
			_localctx = new ReduceBooleanExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(626); reduceBooleanExpressionOperator();
			setState(627); match(LEFT_PARANTHESIS);
			setState(628); coverageExpression(0);
			setState(629); match(RIGHT_PARANTHESIS);
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
		public ReduceNumericalExpressionOperatorContext reduceNumericalExpressionOperator() {
			return getRuleContext(ReduceNumericalExpressionOperatorContext.class,0);
		}
		public TerminalNode RIGHT_PARANTHESIS() { return getToken(wcpsParser.RIGHT_PARANTHESIS, 0); }
		public TerminalNode LEFT_PARANTHESIS() { return getToken(wcpsParser.LEFT_PARANTHESIS, 0); }
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
		enterRule(_localctx, 120, RULE_reduceNumericalExpression);
		try {
			_localctx = new ReduceNumericalExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(631); reduceNumericalExpressionOperator();
			setState(632); match(LEFT_PARANTHESIS);
			setState(633); coverageExpression(0);
			setState(634); match(RIGHT_PARANTHESIS);
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
		enterRule(_localctx, 122, RULE_reduceExpression);
		try {
			setState(638);
			switch (_input.LA(1)) {
			case ALL:
			case SOME:
				enterOuterAlt(_localctx, 1);
				{
				setState(636); reduceBooleanExpression();
				}
				break;
			case ADD:
			case AVG:
			case COUNT:
			case MAX:
			case MIN:
				enterOuterAlt(_localctx, 2);
				{
				setState(637); reduceNumericalExpression();
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
		enterRule(_localctx, 124, RULE_condenseExpressionOperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(640);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AND) | (1L << MAX) | (1L << MIN) | (1L << MULTIPLICATION) | (1L << OR) | (1L << PLUS))) != 0)) ) {
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
		public TerminalNode WHERE() { return getToken(wcpsParser.WHERE, 0); }
		public TerminalNode OVER() { return getToken(wcpsParser.OVER, 0); }
		public TerminalNode CONDENSE() { return getToken(wcpsParser.CONDENSE, 0); }
		public AxisIteratorContext axisIterator(int i) {
			return getRuleContext(AxisIteratorContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(wcpsParser.COMMA); }
		public TerminalNode USING() { return getToken(wcpsParser.USING, 0); }
		public List<AxisIteratorContext> axisIterator() {
			return getRuleContexts(AxisIteratorContext.class);
		}
		public BooleanScalarExpressionContext booleanScalarExpression() {
			return getRuleContext(BooleanScalarExpressionContext.class,0);
		}
		public ScalarExpressionContext scalarExpression() {
			return getRuleContext(ScalarExpressionContext.class,0);
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
		enterRule(_localctx, 126, RULE_generalCondenseExpression);
		int _la;
		try {
			_localctx = new GeneralCondenseExpressionLabelContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(642); match(CONDENSE);
			setState(643); condenseExpressionOperator();
			setState(644); match(OVER);
			setState(645); axisIterator();
			setState(650);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(646); match(COMMA);
				setState(647); axisIterator();
				}
				}
				setState(652);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(655);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(653); match(WHERE);
				setState(654); booleanScalarExpression(0);
				}
			}

			setState(657); match(USING);
			setState(658); scalarExpression();
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
		enterRule(_localctx, 128, RULE_crsName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(660); match(STRING_LITERAL);
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
		public TerminalNode IDENTIFIER() { return getToken(wcpsParser.IDENTIFIER, 0); }
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
		enterRule(_localctx, 130, RULE_axisName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(662); match(IDENTIFIER);
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
		public TerminalNode REAL_NUMBER_CONSTANT() { return getToken(wcpsParser.REAL_NUMBER_CONSTANT, 0); }
		public TerminalNode TRUE() { return getToken(wcpsParser.TRUE, 0); }
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
		enterRule(_localctx, 132, RULE_constant);
		try {
			setState(669);
			switch (_input.LA(1)) {
			case STRING_LITERAL:
				enterOuterAlt(_localctx, 1);
				{
				setState(664); match(STRING_LITERAL);
				}
				break;
			case TRUE:
				enterOuterAlt(_localctx, 2);
				{
				setState(665); match(TRUE);
				}
				break;
			case FALSE:
				enterOuterAlt(_localctx, 3);
				{
				setState(666); match(FALSE);
				}
				break;
			case REAL_NUMBER_CONSTANT:
				enterOuterAlt(_localctx, 4);
				{
				setState(667); match(REAL_NUMBER_CONSTANT);
				}
				break;
			case LEFT_PARANTHESIS:
				enterOuterAlt(_localctx, 5);
				{
				setState(668); complexNumberConstant();
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
		case 8: return booleanScalarExpression_sempred((BooleanScalarExpressionContext)_localctx, predIndex);

		case 16: return numericalScalarExpression_sempred((NumericalScalarExpressionContext)_localctx, predIndex);

		case 26: return coverageExpression_sempred((CoverageExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean coverageExpression_sempred(CoverageExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2: return 23 >= _localctx._p;

		case 3: return 22 >= _localctx._p;

		case 4: return 21 >= _localctx._p;

		case 5: return 15 >= _localctx._p;

		case 6: return 13 >= _localctx._p;

		case 7: return 5 >= _localctx._p;
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
		case 1: return 4 >= _localctx._p;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3T\u02a2\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\3\2\3\2\5\2\u008b\n\2\3\2\3"+
		"\2\3\3\3\3\6\3\u0091\n\3\r\3\16\3\u0092\3\4\3\4\3\4\5\4\u0098\n\4\3\4"+
		"\3\4\3\4\7\4\u009d\n\4\f\4\16\4\u00a0\13\4\3\4\5\4\u00a3\n\4\3\5\3\5\3"+
		"\5\3\6\3\6\3\6\3\7\3\7\3\b\3\b\5\b\u00af\n\b\3\t\3\t\3\t\3\t\3\t\5\t\u00b6"+
		"\n\t\3\n\3\n\3\n\3\n\3\n\5\n\u00bd\n\n\3\n\3\n\5\n\u00c1\n\n\3\n\3\n\3"+
		"\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n\u00cc\n\n\3\n\3\n\3\n\3\n\7\n\u00d2\n\n"+
		"\f\n\16\n\u00d5\13\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20"+
		"\3\20\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\5\22\u00f3\n\22\3\22\3\22\3\22\3\22\7\22\u00f9\n\22\f"+
		"\22\16\22\u00fc\13\22\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\25\3\25"+
		"\3\26\3\26\3\27\3\27\5\27\u010c\n\27\3\30\3\30\3\30\3\30\3\30\3\31\3\31"+
		"\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\5\32\u011e\n\32\3\32\5\32"+
		"\u0121\n\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u012b\n\33\3"+
		"\33\5\33\u012e\n\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\7\34\u0161\n\34"+
		"\f\34\16\34\u0164\13\34\3\34\3\34\3\34\3\34\3\34\3\34\5\34\u016c\n\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\7\34\u0187\n\34"+
		"\f\34\16\34\u018a\13\34\3\35\3\35\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3"+
		" \3 \3 \3 \3 \3!\3!\3\"\3\"\3\"\3\"\3\"\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#"+
		"\3#\3#\5#\u01ad\n#\3$\3$\7$\u01b1\n$\f$\16$\u01b4\13$\3%\3%\3%\3%\3%\3"+
		"&\3&\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\3\'\7\'\u01c7\n\'\f\'\16\'\u01ca"+
		"\13\'\3\'\3\'\3(\3(\3(\3(\3(\3(\3(\3(\3(\3)\3)\3)\7)\u01da\n)\f)\16)\u01dd"+
		"\13)\3*\3*\3*\5*\u01e2\n*\3*\3*\3*\3*\3+\3+\3,\3,\3,\7,\u01ed\n,\f,\16"+
		",\u01f0\13,\3-\3-\3-\5-\u01f5\n-\3-\3-\3-\3-\3-\3-\3-\3-\3-\5-\u0200\n"+
		"-\3-\3-\3-\3-\5-\u0206\n-\3.\3.\3.\3.\7.\u020c\n.\f.\16.\u020f\13.\3."+
		"\3.\3/\3/\3/\3/\3\60\3\60\3\60\3\60\7\60\u021b\n\60\f\60\16\60\u021e\13"+
		"\60\3\60\3\60\3\61\3\61\3\61\3\62\3\62\3\62\3\62\3\62\3\62\3\63\3\63\3"+
		"\64\3\64\3\65\3\65\3\65\3\65\3\65\3\65\7\65\u0235\n\65\f\65\16\65\u0238"+
		"\13\65\3\65\3\65\3\65\3\66\3\66\3\66\3\66\3\66\3\66\3\67\3\67\3\67\3\67"+
		"\3\67\3\67\3\67\3\67\3\67\3\67\3\67\5\67\u024e\n\67\38\38\38\38\38\38"+
		"\78\u0256\n8\f8\168\u0259\138\38\38\38\38\38\38\78\u0261\n8\f8\168\u0264"+
		"\138\38\38\39\39\39\39\39\3:\3:\5:\u026f\n:\3;\3;\3<\3<\3=\3=\3=\3=\3"+
		"=\3>\3>\3>\3>\3>\3?\3?\5?\u0281\n?\3@\3@\3A\3A\3A\3A\3A\3A\7A\u028b\n"+
		"A\fA\16A\u028e\13A\3A\3A\5A\u0292\nA\3A\3A\3A\3B\3B\3C\3C\3D\3D\3D\3D"+
		"\3D\5D\u02a0\nD\3D\2E\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,."+
		"\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086"+
		"\2\21\4\2\36\36II\5\2\7\7\63\63NN\6\2\33\33\37 +,\62\62\4\2\33\33\62\62"+
		"\5\2\30\30/\60\66\66\b\2\4\4!!//\66\6689DD\6\2\b\n\20\21@AFG\5\2\30\30"+
		"/\60\65\66\b\2\4\4!!//\66\6688DD\5\2\34\34((**\3\2OP\6\2\36\36IIOOSS\4"+
		"\2\6\6CC\6\2\5\5\13\13\22\22-.\7\2\7\7-.\60\60\63\63\66\66\u02a8\2\u0088"+
		"\3\2\2\2\4\u008e\3\2\2\2\6\u0094\3\2\2\2\b\u00a4\3\2\2\2\n\u00a7\3\2\2"+
		"\2\f\u00aa\3\2\2\2\16\u00ae\3\2\2\2\20\u00b5\3\2\2\2\22\u00cb\3\2\2\2"+
		"\24\u00d6\3\2\2\2\26\u00d8\3\2\2\2\30\u00da\3\2\2\2\32\u00dc\3\2\2\2\34"+
		"\u00de\3\2\2\2\36\u00e0\3\2\2\2 \u00e2\3\2\2\2\"\u00f2\3\2\2\2$\u00fd"+
		"\3\2\2\2&\u0103\3\2\2\2(\u0105\3\2\2\2*\u0107\3\2\2\2,\u010b\3\2\2\2."+
		"\u010d\3\2\2\2\60\u0112\3\2\2\2\62\u0117\3\2\2\2\64\u0124\3\2\2\2\66\u016b"+
		"\3\2\2\28\u018b\3\2\2\2:\u018d\3\2\2\2<\u018f\3\2\2\2>\u0194\3\2\2\2@"+
		"\u0199\3\2\2\2B\u019b\3\2\2\2D\u01ac\3\2\2\2F\u01ae\3\2\2\2H\u01b5\3\2"+
		"\2\2J\u01ba\3\2\2\2L\u01bc\3\2\2\2N\u01cd\3\2\2\2P\u01d6\3\2\2\2R\u01de"+
		"\3\2\2\2T\u01e7\3\2\2\2V\u01e9\3\2\2\2X\u0205\3\2\2\2Z\u0207\3\2\2\2\\"+
		"\u0212\3\2\2\2^\u0216\3\2\2\2`\u0221\3\2\2\2b\u0224\3\2\2\2d\u022a\3\2"+
		"\2\2f\u022c\3\2\2\2h\u022e\3\2\2\2j\u023c\3\2\2\2l\u024d\3\2\2\2n\u024f"+
		"\3\2\2\2p\u0267\3\2\2\2r\u026e\3\2\2\2t\u0270\3\2\2\2v\u0272\3\2\2\2x"+
		"\u0274\3\2\2\2z\u0279\3\2\2\2|\u0280\3\2\2\2~\u0282\3\2\2\2\u0080\u0284"+
		"\3\2\2\2\u0082\u0296\3\2\2\2\u0084\u0298\3\2\2\2\u0086\u029f\3\2\2\2\u0088"+
		"\u008a\5\4\3\2\u0089\u008b\5\b\5\2\u008a\u0089\3\2\2\2\u008a\u008b\3\2"+
		"\2\2\u008b\u008c\3\2\2\2\u008c\u008d\5\n\6\2\u008d\3\3\2\2\2\u008e\u0090"+
		"\7\3\2\2\u008f\u0091\5\6\4\2\u0090\u008f\3\2\2\2\u0091\u0092\3\2\2\2\u0092"+
		"\u0090\3\2\2\2\u0092\u0093\3\2\2\2\u0093\5\3\2\2\2\u0094\u0095\5\f\7\2"+
		"\u0095\u0097\7$\2\2\u0096\u0098\7\'\2\2\u0097\u0096\3\2\2\2\u0097\u0098"+
		"\3\2\2\2\u0098\u0099\3\2\2\2\u0099\u009e\7P\2\2\u009a\u009b\7\16\2\2\u009b"+
		"\u009d\7P\2\2\u009c\u009a\3\2\2\2\u009d\u00a0\3\2\2\2\u009e\u009c\3\2"+
		"\2\2\u009e\u009f\3\2\2\2\u009f\u00a2\3\2\2\2\u00a0\u009e\3\2\2\2\u00a1"+
		"\u00a3\7=\2\2\u00a2\u00a1\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3\7\3\2\2\2"+
		"\u00a4\u00a5\7M\2\2\u00a5\u00a6\5\22\n\2\u00a6\t\3\2\2\2\u00a7\u00a8\7"+
		":\2\2\u00a8\u00a9\5\16\b\2\u00a9\13\3\2\2\2\u00aa\u00ab\7P\2\2\u00ab\r"+
		"\3\2\2\2\u00ac\u00af\5\20\t\2\u00ad\u00af\5\62\32\2\u00ae\u00ac\3\2\2"+
		"\2\u00ae\u00ad\3\2\2\2\u00af\17\3\2\2\2\u00b0\u00b6\5\22\n\2\u00b1\u00b6"+
		"\5\"\22\2\u00b2\u00b6\5\36\20\2\u00b3\u00b6\5,\27\2\u00b4\u00b6\5 \21"+
		"\2\u00b5\u00b0\3\2\2\2\u00b5\u00b1\3\2\2\2\u00b5\u00b2\3\2\2\2\u00b5\u00b3"+
		"\3\2\2\2\u00b5\u00b4\3\2\2\2\u00b6\21\3\2\2\2\u00b7\u00b8\b\n\1\2\u00b8"+
		"\u00cc\5x=\2\u00b9\u00cc\5\26\f\2\u00ba\u00bc\5\24\13\2\u00bb\u00bd\7"+
		"\'\2\2\u00bc\u00bb\3\2\2\2\u00bc\u00bd\3\2\2\2\u00bd\u00be\3\2\2\2\u00be"+
		"\u00c0\5\22\n\2\u00bf\u00c1\7=\2\2\u00c0\u00bf\3\2\2\2\u00c0\u00c1\3\2"+
		"\2\2\u00c1\u00cc\3\2\2\2\u00c2\u00c3\5\"\22\2\u00c3\u00c4\5\32\16\2\u00c4"+
		"\u00c5\5\"\22\2\u00c5\u00cc\3\2\2\2\u00c6\u00cc\5x=\2\u00c7\u00c8\5\36"+
		"\20\2\u00c8\u00c9\5\34\17\2\u00c9\u00ca\5\36\20\2\u00ca\u00cc\3\2\2\2"+
		"\u00cb\u00b7\3\2\2\2\u00cb\u00b9\3\2\2\2\u00cb\u00ba\3\2\2\2\u00cb\u00c2"+
		"\3\2\2\2\u00cb\u00c6\3\2\2\2\u00cb\u00c7\3\2\2\2\u00cc\u00d3\3\2\2\2\u00cd"+
		"\u00ce\6\n\2\3\u00ce\u00cf\5\30\r\2\u00cf\u00d0\5\22\n\2\u00d0\u00d2\3"+
		"\2\2\2\u00d1\u00cd\3\2\2\2\u00d2\u00d5\3\2\2\2\u00d3\u00d1\3\2\2\2\u00d3"+
		"\u00d4\3\2\2\2\u00d4\23\3\2\2\2\u00d5\u00d3\3\2\2\2\u00d6\u00d7\7\61\2"+
		"\2\u00d7\25\3\2\2\2\u00d8\u00d9\t\2\2\2\u00d9\27\3\2\2\2\u00da\u00db\t"+
		"\3\2\2\u00db\31\3\2\2\2\u00dc\u00dd\t\4\2\2\u00dd\33\3\2\2\2\u00de\u00df"+
		"\t\5\2\2\u00df\35\3\2\2\2\u00e0\u00e1\7S\2\2\u00e1\37\3\2\2\2\u00e2\u00e3"+
		"\7\60\2\2\u00e3!\3\2\2\2\u00e4\u00e5\b\22\1\2\u00e5\u00e6\5(\25\2\u00e6"+
		"\u00e7\7\'\2\2\u00e7\u00e8\5\"\22\2\u00e8\u00e9\7=\2\2\u00e9\u00f3\3\2"+
		"\2\2\u00ea\u00eb\5*\26\2\u00eb\u00ec\7\'\2\2\u00ec\u00ed\5\"\22\2\u00ed"+
		"\u00ee\7=\2\2\u00ee\u00f3\3\2\2\2\u00ef\u00f3\5r:\2\u00f0\u00f3\7O\2\2"+
		"\u00f1\u00f3\5$\23\2\u00f2\u00e4\3\2\2\2\u00f2\u00ea\3\2\2\2\u00f2\u00ef"+
		"\3\2\2\2\u00f2\u00f0\3\2\2\2\u00f2\u00f1\3\2\2\2\u00f3\u00fa\3\2\2\2\u00f4"+
		"\u00f5\6\22\3\3\u00f5\u00f6\5&\24\2\u00f6\u00f7\5\"\22\2\u00f7\u00f9\3"+
		"\2\2\2\u00f8\u00f4\3\2\2\2\u00f9\u00fc\3\2\2\2\u00fa\u00f8\3\2\2\2\u00fa"+
		"\u00fb\3\2\2\2\u00fb#\3\2\2\2\u00fc\u00fa\3\2\2\2\u00fd\u00fe\7\'\2\2"+
		"\u00fe\u00ff\7O\2\2\u00ff\u0100\7\16\2\2\u0100\u0101\7O\2\2\u0101\u0102"+
		"\7=\2\2\u0102%\3\2\2\2\u0103\u0104\t\6\2\2\u0104\'\3\2\2\2\u0105\u0106"+
		"\t\7\2\2\u0106)\3\2\2\2\u0107\u0108\t\b\2\2\u0108+\3\2\2\2\u0109\u010c"+
		"\5.\30\2\u010a\u010c\5\60\31\2\u010b\u0109\3\2\2\2\u010b\u010a\3\2\2\2"+
		"\u010c-\3\2\2\2\u010d\u010e\7\"\2\2\u010e\u010f\7\'\2\2\u010f\u0110\5"+
		"\f\7\2\u0110\u0111\7=\2\2\u0111/\3\2\2\2\u0112\u0113\7\27\2\2\u0113\u0114"+
		"\7\'\2\2\u0114\u0115\5\f\7\2\u0115\u0116\7=\2\2\u0116\61\3\2\2\2\u0117"+
		"\u0118\7\32\2\2\u0118\u0119\7\'\2\2\u0119\u011a\5\66\34\2\u011a\u011b"+
		"\7\16\2\2\u011b\u011d\7R\2\2\u011c\u011e\7\16\2\2\u011d\u011c\3\2\2\2"+
		"\u011d\u011e\3\2\2\2\u011e\u0120\3\2\2\2\u011f\u0121\7S\2\2\u0120\u011f"+
		"\3\2\2\2\u0120\u0121\3\2\2\2\u0121\u0122\3\2\2\2\u0122\u0123\7=\2\2\u0123"+
		"\63\3\2\2\2\u0124\u0125\7\26\2\2\u0125\u0126\7\'\2\2\u0126\u0127\7S\2"+
		"\2\u0127\u0128\7\16\2\2\u0128\u012a\7R\2\2\u0129\u012b\7\16\2\2\u012a"+
		"\u0129\3\2\2\2\u012a\u012b\3\2\2\2\u012b\u012d\3\2\2\2\u012c\u012e\7S"+
		"\2\2\u012d\u012c\3\2\2\2\u012d\u012e\3\2\2\2\u012e\u012f\3\2\2\2\u012f"+
		"\u0130\7=\2\2\u0130\65\3\2\2\2\u0131\u0132\b\34\1\2\u0132\u016c\5\f\7"+
		"\2\u0133\u016c\5\20\t\2\u0134\u016c\5n8\2\u0135\u016c\5h\65\2\u0136\u016c"+
		"\5\64\33\2\u0137\u0138\7H\2\2\u0138\u0139\7\'\2\2\u0139\u013a\5\66\34"+
		"\2\u013a\u013b\7\16\2\2\u013b\u013c\5V,\2\u013c\u013d\7=\2\2\u013d\u016c"+
		"\3\2\2\2\u013e\u013f\7B\2\2\u013f\u0140\7\'\2\2\u0140\u0141\5\66\34\2"+
		"\u0141\u0142\7\16\2\2\u0142\u0143\7%\2\2\u0143\u0144\5P)\2\u0144\u0145"+
		"\7;\2\2\u0145\u0146\7=\2\2\u0146\u016c\3\2\2\2\u0147\u0148\7\35\2\2\u0148"+
		"\u0149\7\'\2\2\u0149\u014a\5\66\34\2\u014a\u014b\7\16\2\2\u014b\u014c"+
		"\7%\2\2\u014c\u014d\5V,\2\u014d\u014e\7;\2\2\u014e\u014f\7=\2\2\u014f"+
		"\u016c\3\2\2\2\u0150\u016c\5<\37\2\u0151\u016c\5> \2\u0152\u016c\5B\""+
		"\2\u0153\u016c\5D#\2\u0154\u016c\5H%\2\u0155\u016c\5L\'\2\u0156\u016c"+
		"\5N(\2\u0157\u0158\7>\2\2\u0158\u0159\7\'\2\2\u0159\u015a\5\66\34\2\u015a"+
		"\u015b\7\16\2\2\u015b\u015c\7%\2\2\u015c\u015d\5V,\2\u015d\u0162\7;\2"+
		"\2\u015e\u015f\7\16\2\2\u015f\u0161\5^\60\2\u0160\u015e\3\2\2\2\u0161"+
		"\u0164\3\2\2\2\u0162\u0160\3\2\2\2\u0162\u0163\3\2\2\2\u0163\u0165\3\2"+
		"\2\2\u0164\u0162\3\2\2\2\u0165\u0166\7=\2\2\u0166\u016c\3\2\2\2\u0167"+
		"\u0168\7\'\2\2\u0168\u0169\5\66\34\2\u0169\u016a\7=\2\2\u016a\u016c\3"+
		"\2\2\2\u016b\u0131\3\2\2\2\u016b\u0133\3\2\2\2\u016b\u0134\3\2\2\2\u016b"+
		"\u0135\3\2\2\2\u016b\u0136\3\2\2\2\u016b\u0137\3\2\2\2\u016b\u013e\3\2"+
		"\2\2\u016b\u0147\3\2\2\2\u016b\u0150\3\2\2\2\u016b\u0151\3\2\2\2\u016b"+
		"\u0152\3\2\2\2\u016b\u0153\3\2\2\2\u016b\u0154\3\2\2\2\u016b\u0155\3\2"+
		"\2\2\u016b\u0156\3\2\2\2\u016b\u0157\3\2\2\2\u016b\u0167\3\2\2\2\u016c"+
		"\u0188\3\2\2\2\u016d\u016e\6\34\4\3\u016e\u016f\5\30\r\2\u016f\u0170\5"+
		"\66\34\2\u0170\u0187\3\2\2\2\u0171\u0172\6\34\5\3\u0172\u0173\58\35\2"+
		"\u0173\u0174\5\66\34\2\u0174\u0187\3\2\2\2\u0175\u0176\6\34\6\3\u0176"+
		"\u0177\5\32\16\2\u0177\u0178\5\66\34\2\u0178\u0187\3\2\2\2\u0179\u017a"+
		"\6\34\7\3\u017a\u017b\7&\2\2\u017b\u017c\5V,\2\u017c\u017d\7<\2\2\u017d"+
		"\u0187\3\2\2\2\u017e\u017f\6\34\b\3\u017f\u0180\7&\2\2\u0180\u0181\5P"+
		")\2\u0181\u0182\7<\2\2\u0182\u0187\3\2\2\2\u0183\u0184\6\34\t\3\u0184"+
		"\u0185\7\31\2\2\u0185\u0187\5J&\2\u0186\u016d\3\2\2\2\u0186\u0171\3\2"+
		"\2\2\u0186\u0175\3\2\2\2\u0186\u0179\3\2\2\2\u0186\u017e\3\2\2\2\u0186"+
		"\u0183\3\2\2\2\u0187\u018a\3\2\2\2\u0188\u0186\3\2\2\2\u0188\u0189\3\2"+
		"\2\2\u0189\67\3\2\2\2\u018a\u0188\3\2\2\2\u018b\u018c\t\t\2\2\u018c9\3"+
		"\2\2\2\u018d\u018e\t\n\2\2\u018e;\3\2\2\2\u018f\u0190\5:\36\2\u0190\u0191"+
		"\7\'\2\2\u0191\u0192\5\66\34\2\u0192\u0193\7=\2\2\u0193=\3\2\2\2\u0194"+
		"\u0195\5*\26\2\u0195\u0196\7\'\2\2\u0196\u0197\5\66\34\2\u0197\u0198\7"+
		"=\2\2\u0198?\3\2\2\2\u0199\u019a\t\13\2\2\u019aA\3\2\2\2\u019b\u019c\5"+
		"@!\2\u019c\u019d\7\'\2\2\u019d\u019e\5\66\34\2\u019e\u019f\7=\2\2\u019f"+
		"C\3\2\2\2\u01a0\u01a1\7\61\2\2\u01a1\u01a2\7\'\2\2\u01a2\u01a3\5\66\34"+
		"\2\u01a3\u01a4\7=\2\2\u01a4\u01ad\3\2\2\2\u01a5\u01a6\7\f\2\2\u01a6\u01a7"+
		"\7\'\2\2\u01a7\u01a8\5\66\34\2\u01a8\u01a9\7\16\2\2\u01a9\u01aa\5\"\22"+
		"\2\u01aa\u01ab\7=\2\2\u01ab\u01ad\3\2\2\2\u01ac\u01a0\3\2\2\2\u01ac\u01a5"+
		"\3\2\2\2\u01adE\3\2\2\2\u01ae\u01b2\7P\2\2\u01af\u01b1\7P\2\2\u01b0\u01af"+
		"\3\2\2\2\u01b1\u01b4\3\2\2\2\u01b2\u01b0\3\2\2\2\u01b2\u01b3\3\2\2\2\u01b3"+
		"G\3\2\2\2\u01b4\u01b2\3\2\2\2\u01b5\u01b6\7\'\2\2\u01b6\u01b7\5F$\2\u01b7"+
		"\u01b8\7=\2\2\u01b8\u01b9\5\66\34\2\u01b9I\3\2\2\2\u01ba\u01bb\t\f\2\2"+
		"\u01bbK\3\2\2\2\u01bc\u01bd\7%\2\2\u01bd\u01be\5J&\2\u01be\u01bf\7\r\2"+
		"\2\u01bf\u01c0\5\66\34\2\u01c0\u01c8\3\2\2\2\u01c1\u01c2\7?\2\2\u01c2"+
		"\u01c3\5J&\2\u01c3\u01c4\7\r\2\2\u01c4\u01c5\5\66\34\2\u01c5\u01c7\3\2"+
		"\2\2\u01c6\u01c1\3\2\2\2\u01c7\u01ca\3\2\2\2\u01c8\u01c6\3\2\2\2\u01c8"+
		"\u01c9\3\2\2\2\u01c9\u01cb\3\2\2\2\u01ca\u01c8\3\2\2\2\u01cb\u01cc\7;"+
		"\2\2\u01ccM\3\2\2\2\u01cd\u01ce\7\25\2\2\u01ce\u01cf\7\'\2\2\u01cf\u01d0"+
		"\5\66\34\2\u01d0\u01d1\7\16\2\2\u01d1\u01d2\5Z.\2\u01d2\u01d3\7\16\2\2"+
		"\u01d3\u01d4\5^\60\2\u01d4\u01d5\7=\2\2\u01d5O\3\2\2\2\u01d6\u01db\5R"+
		"*\2\u01d7\u01d8\7\16\2\2\u01d8\u01da\5R*\2\u01d9\u01d7\3\2\2\2\u01da\u01dd"+
		"\3\2\2\2\u01db\u01d9\3\2\2\2\u01db\u01dc\3\2\2\2\u01dcQ\3\2\2\2\u01dd"+
		"\u01db\3\2\2\2\u01de\u01e1\5\u0084C\2\u01df\u01e0\7\r\2\2\u01e0\u01e2"+
		"\5\u0082B\2\u01e1\u01df\3\2\2\2\u01e1\u01e2\3\2\2\2\u01e2\u01e3\3\2\2"+
		"\2\u01e3\u01e4\7\'\2\2\u01e4\u01e5\5T+\2\u01e5\u01e6\7=\2\2\u01e6S\3\2"+
		"\2\2\u01e7\u01e8\t\r\2\2\u01e8U\3\2\2\2\u01e9\u01ee\5X-\2\u01ea\u01eb"+
		"\7\16\2\2\u01eb\u01ed\5X-\2\u01ec\u01ea\3\2\2\2\u01ed\u01f0\3\2\2\2\u01ee"+
		"\u01ec\3\2\2\2\u01ee\u01ef\3\2\2\2\u01efW\3\2\2\2\u01f0\u01ee\3\2\2\2"+
		"\u01f1\u01f4\5\u0084C\2\u01f2\u01f3\7\r\2\2\u01f3\u01f5\5\u0082B\2\u01f4"+
		"\u01f2\3\2\2\2\u01f4\u01f5\3\2\2\2\u01f5\u01f6\3\2\2\2\u01f6\u01f7\7\'"+
		"\2\2\u01f7\u01f8\5\20\t\2\u01f8\u01f9\7\r\2\2\u01f9\u01fa\5\20\t\2\u01fa"+
		"\u01fb\7=\2\2\u01fb\u0206\3\2\2\2\u01fc\u01ff\5\u0084C\2\u01fd\u01fe\7"+
		"\r\2\2\u01fe\u0200\5\u0082B\2\u01ff\u01fd\3\2\2\2\u01ff\u0200\3\2\2\2"+
		"\u0200\u0201\3\2\2\2\u0201\u0202\7\'\2\2\u0202\u0203\5\20\t\2\u0203\u0204"+
		"\7=\2\2\u0204\u0206\3\2\2\2\u0205\u01f1\3\2\2\2\u0205\u01fc\3\2\2\2\u0206"+
		"Y\3\2\2\2\u0207\u0208\7%\2\2\u0208\u020d\5\\/\2\u0209\u020a\7\16\2\2\u020a"+
		"\u020c\5\\/\2\u020b\u0209\3\2\2\2\u020c\u020f\3\2\2\2\u020d\u020b\3\2"+
		"\2\2\u020d\u020e\3\2\2\2\u020e\u0210\3\2\2\2\u020f\u020d\3\2\2\2\u0210"+
		"\u0211\7;\2\2\u0211[\3\2\2\2\u0212\u0213\5\u0084C\2\u0213\u0214\7\r\2"+
		"\2\u0214\u0215\5\u0082B\2\u0215]\3\2\2\2\u0216\u0217\7%\2\2\u0217\u021c"+
		"\5`\61\2\u0218\u0219\7\16\2\2\u0219\u021b\5`\61\2\u021a\u0218\3\2\2\2"+
		"\u021b\u021e\3\2\2\2\u021c\u021a\3\2\2\2\u021c\u021d\3\2\2\2\u021d\u021f"+
		"\3\2\2\2\u021e\u021c\3\2\2\2\u021f\u0220\7;\2\2\u0220_\3\2\2\2\u0221\u0222"+
		"\5J&\2\u0222\u0223\5b\62\2\u0223a\3\2\2\2\u0224\u0225\7\'\2\2\u0225\u0226"+
		"\5f\64\2\u0226\u0227\7\r\2\2\u0227\u0228\5d\63\2\u0228\u0229\7=\2\2\u0229"+
		"c\3\2\2\2\u022a\u022b\t\2\2\2\u022be\3\2\2\2\u022c\u022d\7S\2\2\u022d"+
		"g\3\2\2\2\u022e\u022f\7\23\2\2\u022f\u0230\7P\2\2\u0230\u0231\7\64\2\2"+
		"\u0231\u0236\5j\66\2\u0232\u0233\7\16\2\2\u0233\u0235\5j\66\2\u0234\u0232"+
		"\3\2\2\2\u0235\u0238\3\2\2\2\u0236\u0234\3\2\2\2\u0236\u0237\3\2\2\2\u0237"+
		"\u0239\3\2\2\2\u0238\u0236\3\2\2\2\u0239\u023a\7L\2\2\u023a\u023b\5\20"+
		"\t\2\u023bi\3\2\2\2\u023c\u023d\7P\2\2\u023d\u023e\5\u0084C\2\u023e\u023f"+
		"\7\'\2\2\u023f\u0240\5l\67\2\u0240\u0241\7=\2\2\u0241k\3\2\2\2\u0242\u0243"+
		"\5\20\t\2\u0243\u0244\7\r\2\2\u0244\u0245\5\20\t\2\u0245\u024e\3\2\2\2"+
		"\u0246\u0247\7#\2\2\u0247\u0248\7\'\2\2\u0248\u0249\5\f\7\2\u0249\u024a"+
		"\7\16\2\2\u024a\u024b\5\u0084C\2\u024b\u024c\7=\2\2\u024c\u024e\3\2\2"+
		"\2\u024d\u0242\3\2\2\2\u024d\u0246\3\2\2\2\u024em\3\2\2\2\u024f\u0250"+
		"\7\23\2\2\u0250\u0251\7P\2\2\u0251\u0252\7\64\2\2\u0252\u0257\5j\66\2"+
		"\u0253\u0254\7\16\2\2\u0254\u0256\5j\66\2\u0255\u0253\3\2\2\2\u0256\u0259"+
		"\3\2\2\2\u0257\u0255\3\2\2\2\u0257\u0258\3\2\2\2\u0258\u025a\3\2\2\2\u0259"+
		"\u0257\3\2\2\2\u025a\u025b\7K\2\2\u025b\u025c\7)\2\2\u025c\u025d\7+\2"+
		"\2\u025d\u0262\5\u0086D\2\u025e\u025f\7?\2\2\u025f\u0261\5\u0086D\2\u0260"+
		"\u025e\3\2\2\2\u0261\u0264\3\2\2\2\u0262\u0260\3\2\2\2\u0262\u0263\3\2"+
		"\2\2\u0263\u0265\3\2\2\2\u0264\u0262\3\2\2\2\u0265\u0266\7\37\2\2\u0266"+
		"o\3\2\2\2\u0267\u0268\5\u0084C\2\u0268\u0269\7\'\2\2\u0269\u026a\5l\67"+
		"\2\u026a\u026b\7=\2\2\u026bq\3\2\2\2\u026c\u026f\5|?\2\u026d\u026f\5\u0080"+
		"A\2\u026e\u026c\3\2\2\2\u026e\u026d\3\2\2\2\u026fs\3\2\2\2\u0270\u0271"+
		"\t\16\2\2\u0271u\3\2\2\2\u0272\u0273\t\17\2\2\u0273w\3\2\2\2\u0274\u0275"+
		"\5t;\2\u0275\u0276\7\'\2\2\u0276\u0277\5\66\34\2\u0277\u0278\7=\2\2\u0278"+
		"y\3\2\2\2\u0279\u027a\5v<\2\u027a\u027b\7\'\2\2\u027b\u027c\5\66\34\2"+
		"\u027c\u027d\7=\2\2\u027d{\3\2\2\2\u027e\u0281\5x=\2\u027f\u0281\5z>\2"+
		"\u0280\u027e\3\2\2\2\u0280\u027f\3\2\2\2\u0281}\3\2\2\2\u0282\u0283\t"+
		"\20\2\2\u0283\177\3\2\2\2\u0284\u0285\7\17\2\2\u0285\u0286\5~@\2\u0286"+
		"\u0287\7\64\2\2\u0287\u028c\5j\66\2\u0288\u0289\7\16\2\2\u0289\u028b\5"+
		"j\66\2\u028a\u0288\3\2\2\2\u028b\u028e\3\2\2\2\u028c\u028a\3\2\2\2\u028c"+
		"\u028d\3\2\2\2\u028d\u0291\3\2\2\2\u028e\u028c\3\2\2\2\u028f\u0290\7M"+
		"\2\2\u0290\u0292\5\22\n\2\u0291\u028f\3\2\2\2\u0291\u0292\3\2\2\2\u0292"+
		"\u0293\3\2\2\2\u0293\u0294\7J\2\2\u0294\u0295\5\20\t\2\u0295\u0081\3\2"+
		"\2\2\u0296\u0297\7S\2\2\u0297\u0083\3\2\2\2\u0298\u0299\7P\2\2\u0299\u0085"+
		"\3\2\2\2\u029a\u02a0\7S\2\2\u029b\u02a0\7I\2\2\u029c\u02a0\7\36\2\2\u029d"+
		"\u02a0\7O\2\2\u029e\u02a0\5$\23\2\u029f\u029a\3\2\2\2\u029f\u029b\3\2"+
		"\2\2\u029f\u029c\3\2\2\2\u029f\u029d\3\2\2\2\u029f\u029e\3\2\2\2\u02a0"+
		"\u0087\3\2\2\2,\u008a\u0092\u0097\u009e\u00a2\u00ae\u00b5\u00bc\u00c0"+
		"\u00cb\u00d3\u00f2\u00fa\u010b\u011d\u0120\u012a\u012d\u0162\u016b\u0186"+
		"\u0188\u01ac\u01b2\u01c8\u01db\u01e1\u01ee\u01f4\u01ff\u0205\u020d\u021c"+
		"\u0236\u024d\u0257\u0262\u026e\u0280\u028c\u0291\u029f";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}