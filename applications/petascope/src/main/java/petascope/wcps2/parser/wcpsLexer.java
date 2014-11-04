// Generated from wcps.g4 by ANTLR 4.1
package petascope.wcps2.parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class wcpsLexer extends Lexer {
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
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"<INVALID>",
		"FOR", "ABSOLUTE_VALUE", "ADD", "ALL", "AND", "ARCSIN", "ARCCOS", "ARCTAN",
		"AVG", "BIT", "':'", "','", "CONDENSE", "COS", "COSH", "COUNT", "COVERAGE",
		"'$'", "CRS_TRANSFORM", "DECODE", "DESCRIBE_COVERAGE", "'/'", "'.'", "ENCODE",
		"'='", "EXP", "EXTEND", "FALSE", "'>'", "'>='", "IMAGINARY_PART", "ID",
		"IMGCRSDOMAIN", "IN", "'{'", "'['", "'('", "LN", "LIST", "LOG", "'<'",
		"'<='", "MAX", "MIN", "'-'", "'*'", "NOT", "'!='", "OR", "OVER", "OVERLAY",
		"'+'", "POWER", "REAL_PART", "ROUND", "RETURN", "'}'", "']'", "')'", "SCALE",
		"';'", "SIN", "SINH", "SLICE", "SOME", "SQUARE_ROOT", "STRUCT", "TAN",
		"TANH", "TRIM", "TRUE", "USING", "VALUE", "VALUES", "WHERE", "XOR", "REAL_NUMBER_CONSTANT",
		"IDENTIFIER", "NAME", "FORMAT_NAME", "STRING_LITERAL", "WS"
	};
	public static final String[] ruleNames = {
		"FOR", "ABSOLUTE_VALUE", "ADD", "ALL", "AND", "ARCSIN", "ARCCOS", "ARCTAN",
		"AVG", "BIT", "COLON", "COMMA", "CONDENSE", "COS", "COSH", "COUNT", "COVERAGE",
		"COVERAGE_VARIABLE_NAME_PREFIX", "CRS_TRANSFORM", "DECODE", "DESCRIBE_COVERAGE",
		"DIVISION", "DOT", "ENCODE", "EQUAL", "EXP", "EXTEND", "FALSE", "GREATER_THAN",
		"GREATER_OR_EQUAL_THAN", "IMAGINARY_PART", "ID", "IMGCRSDOMAIN", "IN",
		"LEFT_BRACE", "LEFT_BRACKET", "LEFT_PARANTHESIS", "LN", "LIST", "LOG",
		"LOWER_THAN", "LOWER_OR_EQUAL_THAN", "MAX", "MIN", "MINUS", "MULTIPLICATION",
		"NOT", "NOT_EQUAL", "OR", "OVER", "OVERLAY", "PLUS", "POWER", "REAL_PART",
		"ROUND", "RETURN", "RIGHT_BRACE", "RIGHT_BRACKET", "RIGHT_PARANTHESIS",
		"SCALE", "SEMICOLON", "SIN", "SINH", "SLICE", "SOME", "SQUARE_ROOT", "STRUCT",
		"TAN", "TANH", "TRIM", "TRUE", "USING", "VALUE", "VALUES", "WHERE", "XOR",
		"REAL_NUMBER_CONSTANT", "IDENTIFIER", "NAME", "FORMAT_NAME", "STRING_LITERAL",
		"WS"
	};


	public wcpsLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "wcps.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 81: WS_action((RuleContext)_localctx, actionIndex); break;
		}
	}
	private void WS_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0: skip();  break;
		}
	}

	public static final String _serializedATN =
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\2T\u023d\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\3\2\3"+
		"\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6"+
		"\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3"+
		"\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f\3\r\3\r"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\20"+
		"\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22"+
		"\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\26"+
		"\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\32"+
		"\3\32\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\35\3\35"+
		"\3\35\3\35\3\35\3\35\3\36\3\36\3\37\3\37\3\37\3 \3 \3 \3!\3!\3!\3\"\3"+
		"\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3#\3#\3#\3$\3$\3%\3%\3"+
		"&\3&\3\'\3\'\3\'\3(\3(\3(\3(\3(\3)\3)\3)\3)\3*\3*\3+\3+\3+\3,\3,\3,\3"+
		",\3-\3-\3-\3-\3.\3.\3/\3/\3\60\3\60\3\60\3\60\3\61\3\61\3\61\3\62\3\62"+
		"\3\62\3\63\3\63\3\63\3\63\3\63\3\64\3\64\3\64\3\64\3\64\3\64\3\64\3\64"+
		"\3\65\3\65\3\66\3\66\3\66\3\66\3\67\3\67\3\67\38\38\38\38\38\38\39\39"+
		"\39\39\39\39\39\3:\3:\3;\3;\3<\3<\3=\3=\3=\3=\3=\3=\3>\3>\3?\3?\3?\3?"+
		"\3@\3@\3@\3@\3@\3A\3A\3A\3A\3A\3A\3B\3B\3B\3B\3B\3C\3C\3C\3C\3C\3D\3D"+
		"\3D\3D\3D\3D\3D\3E\3E\3E\3E\3F\3F\3F\3F\3F\3G\3G\3G\3G\3G\3H\3H\3H\3H"+
		"\3H\3I\3I\3I\3I\3I\3I\3J\3J\3J\3J\3J\3J\3K\3K\3K\3K\3K\3K\3K\3L\3L\3L"+
		"\3L\3L\3L\3M\3M\3M\3M\3N\5N\u020d\nN\3N\6N\u0210\nN\rN\16N\u0211\3N\3"+
		"N\7N\u0216\nN\fN\16N\u0219\13N\5N\u021b\nN\3O\6O\u021e\nO\rO\16O\u021f"+
		"\3P\6P\u0223\nP\rP\16P\u0224\3Q\3Q\6Q\u0229\nQ\rQ\16Q\u022a\3Q\3Q\3R\3"+
		"R\6R\u0231\nR\rR\16R\u0232\3R\3R\3S\6S\u0238\nS\rS\16S\u0239\3S\3S\2T"+
		"\3\3\1\5\4\1\7\5\1\t\6\1\13\7\1\r\b\1\17\t\1\21\n\1\23\13\1\25\f\1\27"+
		"\r\1\31\16\1\33\17\1\35\20\1\37\21\1!\22\1#\23\1%\24\1\'\25\1)\26\1+\27"+
		"\1-\30\1/\31\1\61\32\1\63\33\1\65\34\1\67\35\19\36\1;\37\1= \1?!\1A\""+
		"\1C#\1E$\1G%\1I&\1K\'\1M(\1O)\1Q*\1S+\1U,\1W-\1Y.\1[/\1]\60\1_\61\1a\62"+
		"\1c\63\1e\64\1g\65\1i\66\1k\67\1m8\1o9\1q:\1s;\1u<\1w=\1y>\1{?\1}@\1\177"+
		"A\1\u0081B\1\u0083C\1\u0085D\1\u0087E\1\u0089F\1\u008bG\1\u008dH\1\u008f"+
		"I\1\u0091J\1\u0093K\1\u0095L\1\u0097M\1\u0099N\1\u009bO\1\u009dP\1\u009f"+
		"Q\1\u00a1R\1\u00a3S\1\u00a5T\2\3\2\37\4\2HHhh\4\2QQqq\4\2TTtt\4\2CCcc"+
		"\4\2DDdd\4\2UUuu\4\2FFff\4\2NNnn\4\2PPpp\4\2EEee\4\2KKkk\4\2VVvv\4\2X"+
		"Xxx\4\2IIii\4\2GGgg\4\2JJjj\4\2WWww\4\2OOoo\4\2ZZzz\4\2RRrr\4\2[[{{\4"+
		"\2YYyy\4\2SSss\3\2\62;\7\2&&\62;C\\aac|\5\2C\\c|~~\6\2\62;C\\aac|\7\2"+
		"##%&((-ac|\5\2\13\f\17\17\"\"\u0245\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2"+
		"\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23"+
		"\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2"+
		"\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2"+
		"\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3"+
		"\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2"+
		"\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2"+
		"\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2["+
		"\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3\2"+
		"\2\2\2i\3\2\2\2\2k\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2s\3\2\2\2"+
		"\2u\3\2\2\2\2w\3\2\2\2\2y\3\2\2\2\2{\3\2\2\2\2}\3\2\2\2\2\177\3\2\2\2"+
		"\2\u0081\3\2\2\2\2\u0083\3\2\2\2\2\u0085\3\2\2\2\2\u0087\3\2\2\2\2\u0089"+
		"\3\2\2\2\2\u008b\3\2\2\2\2\u008d\3\2\2\2\2\u008f\3\2\2\2\2\u0091\3\2\2"+
		"\2\2\u0093\3\2\2\2\2\u0095\3\2\2\2\2\u0097\3\2\2\2\2\u0099\3\2\2\2\2\u009b"+
		"\3\2\2\2\2\u009d\3\2\2\2\2\u009f\3\2\2\2\2\u00a1\3\2\2\2\2\u00a3\3\2\2"+
		"\2\2\u00a5\3\2\2\2\3\u00a7\3\2\2\2\5\u00ab\3\2\2\2\7\u00af\3\2\2\2\t\u00b3"+
		"\3\2\2\2\13\u00b7\3\2\2\2\r\u00bb\3\2\2\2\17\u00c2\3\2\2\2\21\u00c9\3"+
		"\2\2\2\23\u00d0\3\2\2\2\25\u00d4\3\2\2\2\27\u00d8\3\2\2\2\31\u00da\3\2"+
		"\2\2\33\u00dc\3\2\2\2\35\u00e5\3\2\2\2\37\u00e9\3\2\2\2!\u00ee\3\2\2\2"+
		"#\u00f4\3\2\2\2%\u00fd\3\2\2\2\'\u00ff\3\2\2\2)\u010c\3\2\2\2+\u0113\3"+
		"\2\2\2-\u0124\3\2\2\2/\u0126\3\2\2\2\61\u0128\3\2\2\2\63\u012f\3\2\2\2"+
		"\65\u0131\3\2\2\2\67\u0135\3\2\2\29\u013c\3\2\2\2;\u0142\3\2\2\2=\u0144"+
		"\3\2\2\2?\u0147\3\2\2\2A\u014a\3\2\2\2C\u014d\3\2\2\2E\u015a\3\2\2\2G"+
		"\u015d\3\2\2\2I\u015f\3\2\2\2K\u0161\3\2\2\2M\u0163\3\2\2\2O\u0166\3\2"+
		"\2\2Q\u016b\3\2\2\2S\u016f\3\2\2\2U\u0171\3\2\2\2W\u0174\3\2\2\2Y\u0178"+
		"\3\2\2\2[\u017c\3\2\2\2]\u017e\3\2\2\2_\u0180\3\2\2\2a\u0184\3\2\2\2c"+
		"\u0187\3\2\2\2e\u018a\3\2\2\2g\u018f\3\2\2\2i\u0197\3\2\2\2k\u0199\3\2"+
		"\2\2m\u019d\3\2\2\2o\u01a0\3\2\2\2q\u01a6\3\2\2\2s\u01ad\3\2\2\2u\u01af"+
		"\3\2\2\2w\u01b1\3\2\2\2y\u01b3\3\2\2\2{\u01b9\3\2\2\2}\u01bb\3\2\2\2\177"+
		"\u01bf\3\2\2\2\u0081\u01c4\3\2\2\2\u0083\u01ca\3\2\2\2\u0085\u01cf\3\2"+
		"\2\2\u0087\u01d4\3\2\2\2\u0089\u01db\3\2\2\2\u008b\u01df\3\2\2\2\u008d"+
		"\u01e4\3\2\2\2\u008f\u01e9\3\2\2\2\u0091\u01ee\3\2\2\2\u0093\u01f4\3\2"+
		"\2\2\u0095\u01fa\3\2\2\2\u0097\u0201\3\2\2\2\u0099\u0207\3\2\2\2\u009b"+
		"\u020c\3\2\2\2\u009d\u021d\3\2\2\2\u009f\u0222\3\2\2\2\u00a1\u0226\3\2"+
		"\2\2\u00a3\u022e\3\2\2\2\u00a5\u0237\3\2\2\2\u00a7\u00a8\t\2\2\2\u00a8"+
		"\u00a9\t\3\2\2\u00a9\u00aa\t\4\2\2\u00aa\4\3\2\2\2\u00ab\u00ac\t\5\2\2"+
		"\u00ac\u00ad\t\6\2\2\u00ad\u00ae\t\7\2\2\u00ae\6\3\2\2\2\u00af\u00b0\t"+
		"\5\2\2\u00b0\u00b1\t\b\2\2\u00b1\u00b2\t\b\2\2\u00b2\b\3\2\2\2\u00b3\u00b4"+
		"\t\5\2\2\u00b4\u00b5\t\t\2\2\u00b5\u00b6\t\t\2\2\u00b6\n\3\2\2\2\u00b7"+
		"\u00b8\t\5\2\2\u00b8\u00b9\t\n\2\2\u00b9\u00ba\t\b\2\2\u00ba\f\3\2\2\2"+
		"\u00bb\u00bc\t\5\2\2\u00bc\u00bd\t\4\2\2\u00bd\u00be\t\13\2\2\u00be\u00bf"+
		"\t\7\2\2\u00bf\u00c0\t\f\2\2\u00c0\u00c1\t\n\2\2\u00c1\16\3\2\2\2\u00c2"+
		"\u00c3\t\5\2\2\u00c3\u00c4\t\4\2\2\u00c4\u00c5\t\13\2\2\u00c5\u00c6\t"+
		"\13\2\2\u00c6\u00c7\t\3\2\2\u00c7\u00c8\t\7\2\2\u00c8\20\3\2\2\2\u00c9"+
		"\u00ca\t\5\2\2\u00ca\u00cb\t\4\2\2\u00cb\u00cc\t\13\2\2\u00cc\u00cd\t"+
		"\r\2\2\u00cd\u00ce\t\5\2\2\u00ce\u00cf\t\n\2\2\u00cf\22\3\2\2\2\u00d0"+
		"\u00d1\t\5\2\2\u00d1\u00d2\t\16\2\2\u00d2\u00d3\t\17\2\2\u00d3\24\3\2"+
		"\2\2\u00d4\u00d5\t\6\2\2\u00d5\u00d6\t\f\2\2\u00d6\u00d7\t\r\2\2\u00d7"+
		"\26\3\2\2\2\u00d8\u00d9\7<\2\2\u00d9\30\3\2\2\2\u00da\u00db\7.\2\2\u00db"+
		"\32\3\2\2\2\u00dc\u00dd\t\13\2\2\u00dd\u00de\t\3\2\2\u00de\u00df\t\n\2"+
		"\2\u00df\u00e0\t\b\2\2\u00e0\u00e1\t\20\2\2\u00e1\u00e2\t\n\2\2\u00e2"+
		"\u00e3\t\7\2\2\u00e3\u00e4\t\20\2\2\u00e4\34\3\2\2\2\u00e5\u00e6\t\13"+
		"\2\2\u00e6\u00e7\t\3\2\2\u00e7\u00e8\t\7\2\2\u00e8\36\3\2\2\2\u00e9\u00ea"+
		"\t\13\2\2\u00ea\u00eb\t\3\2\2\u00eb\u00ec\t\7\2\2\u00ec\u00ed\t\21\2\2"+
		"\u00ed \3\2\2\2\u00ee\u00ef\t\13\2\2\u00ef\u00f0\t\3\2\2\u00f0\u00f1\t"+
		"\22\2\2\u00f1\u00f2\t\n\2\2\u00f2\u00f3\t\r\2\2\u00f3\"\3\2\2\2\u00f4"+
		"\u00f5\t\13\2\2\u00f5\u00f6\t\3\2\2\u00f6\u00f7\t\16\2\2\u00f7\u00f8\t"+
		"\20\2\2\u00f8\u00f9\t\4\2\2\u00f9\u00fa\t\5\2\2\u00fa\u00fb\t\17\2\2\u00fb"+
		"\u00fc\t\20\2\2\u00fc$\3\2\2\2\u00fd\u00fe\7&\2\2\u00fe&\3\2\2\2\u00ff"+
		"\u0100\t\13\2\2\u0100\u0101\t\4\2\2\u0101\u0102\t\7\2\2\u0102\u0103\t"+
		"\r\2\2\u0103\u0104\t\4\2\2\u0104\u0105\t\5\2\2\u0105\u0106\t\n\2\2\u0106"+
		"\u0107\t\7\2\2\u0107\u0108\t\2\2\2\u0108\u0109\t\3\2\2\u0109\u010a\t\4"+
		"\2\2\u010a\u010b\t\23\2\2\u010b(\3\2\2\2\u010c\u010d\t\b\2\2\u010d\u010e"+
		"\t\20\2\2\u010e\u010f\t\13\2\2\u010f\u0110\t\3\2\2\u0110\u0111\t\b\2\2"+
		"\u0111\u0112\t\20\2\2\u0112*\3\2\2\2\u0113\u0114\t\b\2\2\u0114\u0115\t"+
		"\20\2\2\u0115\u0116\t\7\2\2\u0116\u0117\t\13\2\2\u0117\u0118\t\4\2\2\u0118"+
		"\u0119\t\f\2\2\u0119\u011a\t\6\2\2\u011a\u011b\t\20\2\2\u011b\u011c\t"+
		"\13\2\2\u011c\u011d\t\3\2\2\u011d\u011e\t\16\2\2\u011e\u011f\t\20\2\2"+
		"\u011f\u0120\t\4\2\2\u0120\u0121\t\5\2\2\u0121\u0122\t\17\2\2\u0122\u0123"+
		"\t\20\2\2\u0123,\3\2\2\2\u0124\u0125\7\61\2\2\u0125.\3\2\2\2\u0126\u0127"+
		"\7\60\2\2\u0127\60\3\2\2\2\u0128\u0129\t\20\2\2\u0129\u012a\t\n\2\2\u012a"+
		"\u012b\t\13\2\2\u012b\u012c\t\3\2\2\u012c\u012d\t\b\2\2\u012d\u012e\t"+
		"\20\2\2\u012e\62\3\2\2\2\u012f\u0130\7?\2\2\u0130\64\3\2\2\2\u0131\u0132"+
		"\t\20\2\2\u0132\u0133\t\24\2\2\u0133\u0134\t\25\2\2\u0134\66\3\2\2\2\u0135"+
		"\u0136\t\20\2\2\u0136\u0137\t\24\2\2\u0137\u0138\t\r\2\2\u0138\u0139\t"+
		"\20\2\2\u0139\u013a\t\n\2\2\u013a\u013b\t\b\2\2\u013b8\3\2\2\2\u013c\u013d"+
		"\t\2\2\2\u013d\u013e\t\5\2\2\u013e\u013f\t\t\2\2\u013f\u0140\t\7\2\2\u0140"+
		"\u0141\t\20\2\2\u0141:\3\2\2\2\u0142\u0143\7@\2\2\u0143<\3\2\2\2\u0144"+
		"\u0145\7@\2\2\u0145\u0146\7?\2\2\u0146>\3\2\2\2\u0147\u0148\t\f\2\2\u0148"+
		"\u0149\t\23\2\2\u0149@\3\2\2\2\u014a\u014b\t\f\2\2\u014b\u014c\t\b\2\2"+
		"\u014cB\3\2\2\2\u014d\u014e\t\f\2\2\u014e\u014f\t\23\2\2\u014f\u0150\t"+
		"\17\2\2\u0150\u0151\t\13\2\2\u0151\u0152\t\4\2\2\u0152\u0153\t\7\2\2\u0153"+
		"\u0154\t\b\2\2\u0154\u0155\t\3\2\2\u0155\u0156\t\23\2\2\u0156\u0157\t"+
		"\5\2\2\u0157\u0158\t\f\2\2\u0158\u0159\t\n\2\2\u0159D\3\2\2\2\u015a\u015b"+
		"\t\f\2\2\u015b\u015c\t\n\2\2\u015cF\3\2\2\2\u015d\u015e\7}\2\2\u015eH"+
		"\3\2\2\2\u015f\u0160\7]\2\2\u0160J\3\2\2\2\u0161\u0162\7*\2\2\u0162L\3"+
		"\2\2\2\u0163\u0164\t\t\2\2\u0164\u0165\t\n\2\2\u0165N\3\2\2\2\u0166\u0167"+
		"\t\t\2\2\u0167\u0168\t\f\2\2\u0168\u0169\t\7\2\2\u0169\u016a\t\r\2\2\u016a"+
		"P\3\2\2\2\u016b\u016c\t\t\2\2\u016c\u016d\t\3\2\2\u016d\u016e\t\17\2\2"+
		"\u016eR\3\2\2\2\u016f\u0170\7>\2\2\u0170T\3\2\2\2\u0171\u0172\7>\2\2\u0172"+
		"\u0173\7?\2\2\u0173V\3\2\2\2\u0174\u0175\t\23\2\2\u0175\u0176\t\5\2\2"+
		"\u0176\u0177\t\24\2\2\u0177X\3\2\2\2\u0178\u0179\t\23\2\2\u0179\u017a"+
		"\t\f\2\2\u017a\u017b\t\n\2\2\u017bZ\3\2\2\2\u017c\u017d\7/\2\2\u017d\\"+
		"\3\2\2\2\u017e\u017f\7,\2\2\u017f^\3\2\2\2\u0180\u0181\t\n\2\2\u0181\u0182"+
		"\t\3\2\2\u0182\u0183\t\r\2\2\u0183`\3\2\2\2\u0184\u0185\7#\2\2\u0185\u0186"+
		"\7?\2\2\u0186b\3\2\2\2\u0187\u0188\t\3\2\2\u0188\u0189\t\4\2\2\u0189d"+
		"\3\2\2\2\u018a\u018b\t\3\2\2\u018b\u018c\t\16\2\2\u018c\u018d\t\20\2\2"+
		"\u018d\u018e\t\4\2\2\u018ef\3\2\2\2\u018f\u0190\t\3\2\2\u0190\u0191\t"+
		"\16\2\2\u0191\u0192\t\20\2\2\u0192\u0193\t\4\2\2\u0193\u0194\t\t\2\2\u0194"+
		"\u0195\t\5\2\2\u0195\u0196\t\26\2\2\u0196h\3\2\2\2\u0197\u0198\7-\2\2"+
		"\u0198j\3\2\2\2\u0199\u019a\t\25\2\2\u019a\u019b\t\3\2\2\u019b\u019c\t"+
		"\27\2\2\u019cl\3\2\2\2\u019d\u019e\t\4\2\2\u019e\u019f\t\20\2\2\u019f"+
		"n\3\2\2\2\u01a0\u01a1\t\4\2\2\u01a1\u01a2\t\3\2\2\u01a2\u01a3\t\22\2\2"+
		"\u01a3\u01a4\t\n\2\2\u01a4\u01a5\t\b\2\2\u01a5p\3\2\2\2\u01a6\u01a7\t"+
		"\4\2\2\u01a7\u01a8\t\20\2\2\u01a8\u01a9\t\r\2\2\u01a9\u01aa\t\22\2\2\u01aa"+
		"\u01ab\t\4\2\2\u01ab\u01ac\t\n\2\2\u01acr\3\2\2\2\u01ad\u01ae\7\177\2"+
		"\2\u01aet\3\2\2\2\u01af\u01b0\7_\2\2\u01b0v\3\2\2\2\u01b1\u01b2\7+\2\2"+
		"\u01b2x\3\2\2\2\u01b3\u01b4\t\7\2\2\u01b4\u01b5\t\13\2\2\u01b5\u01b6\t"+
		"\5\2\2\u01b6\u01b7\t\t\2\2\u01b7\u01b8\t\20\2\2\u01b8z\3\2\2\2\u01b9\u01ba"+
		"\7=\2\2\u01ba|\3\2\2\2\u01bb\u01bc\t\7\2\2\u01bc\u01bd\t\f\2\2\u01bd\u01be"+
		"\t\n\2\2\u01be~\3\2\2\2\u01bf\u01c0\t\7\2\2\u01c0\u01c1\t\f\2\2\u01c1"+
		"\u01c2\t\n\2\2\u01c2\u01c3\t\21\2\2\u01c3\u0080\3\2\2\2\u01c4\u01c5\t"+
		"\7\2\2\u01c5\u01c6\t\t\2\2\u01c6\u01c7\t\f\2\2\u01c7\u01c8\t\13\2\2\u01c8"+
		"\u01c9\t\20\2\2\u01c9\u0082\3\2\2\2\u01ca\u01cb\t\7\2\2\u01cb\u01cc\t"+
		"\3\2\2\u01cc\u01cd\t\23\2\2\u01cd\u01ce\t\20\2\2\u01ce\u0084\3\2\2\2\u01cf"+
		"\u01d0\t\7\2\2\u01d0\u01d1\t\30\2\2\u01d1\u01d2\t\4\2\2\u01d2\u01d3\t"+
		"\r\2\2\u01d3\u0086\3\2\2\2\u01d4\u01d5\t\7\2\2\u01d5\u01d6\t\r\2\2\u01d6"+
		"\u01d7\t\4\2\2\u01d7\u01d8\t\22\2\2\u01d8\u01d9\t\13\2\2\u01d9\u01da\t"+
		"\r\2\2\u01da\u0088\3\2\2\2\u01db\u01dc\t\r\2\2\u01dc\u01dd\t\5\2\2\u01dd"+
		"\u01de\t\n\2\2\u01de\u008a\3\2\2\2\u01df\u01e0\t\r\2\2\u01e0\u01e1\t\5"+
		"\2\2\u01e1\u01e2\t\n\2\2\u01e2\u01e3\t\21\2\2\u01e3\u008c\3\2\2\2\u01e4"+
		"\u01e5\t\r\2\2\u01e5\u01e6\t\4\2\2\u01e6\u01e7\t\f\2\2\u01e7\u01e8\t\23"+
		"\2\2\u01e8\u008e\3\2\2\2\u01e9\u01ea\t\r\2\2\u01ea\u01eb\t\4\2\2\u01eb"+
		"\u01ec\t\22\2\2\u01ec\u01ed\t\20\2\2\u01ed\u0090\3\2\2\2\u01ee\u01ef\t"+
		"\22\2\2\u01ef\u01f0\t\7\2\2\u01f0\u01f1\t\f\2\2\u01f1\u01f2\t\n\2\2\u01f2"+
		"\u01f3\t\17\2\2\u01f3\u0092\3\2\2\2\u01f4\u01f5\t\16\2\2\u01f5\u01f6\t"+
		"\5\2\2\u01f6\u01f7\t\t\2\2\u01f7\u01f8\t\22\2\2\u01f8\u01f9\t\20\2\2\u01f9"+
		"\u0094\3\2\2\2\u01fa\u01fb\t\16\2\2\u01fb\u01fc\t\5\2\2\u01fc\u01fd\t"+
		"\t\2\2\u01fd\u01fe\t\22\2\2\u01fe\u01ff\t\20\2\2\u01ff\u0200\t\7\2\2\u0200"+
		"\u0096\3\2\2\2\u0201\u0202\t\27\2\2\u0202\u0203\t\21\2\2\u0203\u0204\t"+
		"\20\2\2\u0204\u0205\t\4\2\2\u0205\u0206\t\20\2\2\u0206\u0098\3\2\2\2\u0207"+
		"\u0208\t\24\2\2\u0208\u0209\t\3\2\2\u0209\u020a\t\4\2\2\u020a\u009a\3"+
		"\2\2\2\u020b\u020d\7/\2\2\u020c\u020b\3\2\2\2\u020c\u020d\3\2\2\2\u020d"+
		"\u020f\3\2\2\2\u020e\u0210\t\31\2\2\u020f\u020e\3\2\2\2\u0210\u0211\3"+
		"\2\2\2\u0211\u020f\3\2\2\2\u0211\u0212\3\2\2\2\u0212\u021a\3\2\2\2\u0213"+
		"\u0217\7\60\2\2\u0214\u0216\t\31\2\2\u0215\u0214\3\2\2\2\u0216\u0219\3"+
		"\2\2\2\u0217\u0215\3\2\2\2\u0217\u0218\3\2\2\2\u0218\u021b\3\2\2\2\u0219"+
		"\u0217\3\2\2\2\u021a\u0213\3\2\2\2\u021a\u021b\3\2\2\2\u021b\u009c\3\2"+
		"\2\2\u021c\u021e\t\32\2\2\u021d\u021c\3\2\2\2\u021e\u021f\3\2\2\2\u021f"+
		"\u021d\3\2\2\2\u021f\u0220\3\2\2\2\u0220\u009e\3\2\2\2\u0221\u0223\t\33"+
		"\2\2\u0222\u0221\3\2\2\2\u0223\u0224\3\2\2\2\u0224\u0222\3\2\2\2\u0224"+
		"\u0225\3\2\2\2\u0225\u00a0\3\2\2\2\u0226\u0228\7$\2\2\u0227\u0229\t\34"+
		"\2\2\u0228\u0227\3\2\2\2\u0229\u022a\3\2\2\2\u022a\u0228\3\2\2\2\u022a"+
		"\u022b\3\2\2\2\u022b\u022c\3\2\2\2\u022c\u022d\7$\2\2\u022d\u00a2\3\2"+
		"\2\2\u022e\u0230\7$\2\2\u022f\u0231\t\35\2\2\u0230\u022f\3\2\2\2\u0231"+
		"\u0232\3\2\2\2\u0232\u0230\3\2\2\2\u0232\u0233\3\2\2\2\u0233\u0234\3\2"+
		"\2\2\u0234\u0235\7$\2\2\u0235\u00a4\3\2\2\2\u0236\u0238\t\36\2\2\u0237"+
		"\u0236\3\2\2\2\u0238\u0239\3\2\2\2\u0239\u0237\3\2\2\2\u0239\u023a\3\2"+
		"\2\2\u023a\u023b\3\2\2\2\u023b\u023c\bS\2\2\u023c\u00a6\3\2\2\2\f\2\u020c"+
		"\u0211\u0217\u021a\u021f\u0224\u022a\u0232\u0239";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}