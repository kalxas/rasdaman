// $ANTLR 3.2 Sep 23, 2009 12:02:23 wcps.g 2013-11-25 16:01:04
package petascope.wcps.grammar;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class wcpsLexer extends Lexer {
    public static final int IMAGECRS=28;
    public static final int LT=18;
    public static final int LN=66;
    public static final int LOG=65;
    public static final int CASE=110;
    public static final int CHAR=88;
    public static final int COMPLEX=93;
    public static final int SETINTERPOLATIONDEFAULT=54;
    public static final int COUNT=37;
    public static final int EQUALS=16;
    public static final int COSH=71;
    public static final int NOT=76;
    public static final int INTEGERCONSTANT=78;
    public static final int EOF=-1;
    public static final int SINH=70;
    public static final int LBRACKET=82;
    public static final int RPAREN=7;
    public static final int TANH=72;
    public static final int LINEAR=100;
    public static final int NAME=107;
    public static final int FULL=103;
    public static final int SIN=67;
    public static final int USING=44;
    public static final int EXP=64;
    public static final int COS=68;
    public static final int TAN=69;
    public static final int RETURN=10;
    public static final int DOUBLE=92;
    public static final int NULLDEFAULT=112;
    public static final int STORE=12;
    public static final int DIVIDE=25;
    public static final int BOOLEANCONSTANT=97;
    public static final int RBRACE=57;
    public static final int SETNULLSET=53;
    public static final int SWITCH=109;
    public static final int CONDENSE=42;
    public static final int WHITESPACE=114;
    public static final int SEMICOLON=49;
    public static final int VALUE=47;
    public static final int MULT=24;
    public static final int LIST=48;
    public static final int COMPLEX2=94;
    public static final int ABS=61;
    public static final int CRSSET=30;
    public static final int SCALE=81;
    public static final int VARIABLE_DOLLAR=108;
    public static final int FLOATCONSTANT=98;
    public static final int IMAGECRSDOMAIN=29;
    public static final int NONE=104;
    public static final int OR=13;
    public static final int TRIM=84;
    public static final int GT=19;
    public static final int ROUND=79;
    public static final int QUADRATIC=101;
    public static final int ENCODE=11;
    public static final int PHI=113;
    public static final int OVER=43;
    public static final int COVERAGE=46;
    public static final int WHERE=9;
    public static final int RE=62;
    public static final int OVERLAY=26;
    public static final int GTE=21;
    public static final int LBRACE=56;
    public static final int MAX=41;
    public static final int INTERPOLATIONDEFAULT=32;
    public static final int FOR=4;
    public static final int FLOAT=91;
    public static final int SLICE=85;
    public static final int AND=15;
    public static final int LTE=20;
    public static final int LPAREN=6;
    public static final int EXTEND=86;
    public static final int IM=63;
    public static final int BOOLEAN=87;
    public static final int IN=5;
    public static final int COMMA=8;
    public static final int AVG=39;
    public static final int IDENTIFIER=27;
    public static final int SOME=36;
    public static final int ALL=35;
    public static final int ARCSIN=73;
    public static final int PLUS=22;
    public static final int ARCCOS=74;
    public static final int RBRACKET=83;
    public static final int DOT=96;
    public static final int ADD=38;
    public static final int SETIDENTIFIER=51;
    public static final int SETINTERPOLATIONSET=55;
    public static final int XOR=14;
    public static final int OTHER=106;
    public static final int DEFAULT=111;
    public static final int VALUES=50;
    public static final int ARCTAN=75;
    public static final int NOTEQUALS=17;
    public static final int SHORT=89;
    public static final int STRUCT=58;
    public static final int MIN=40;
    public static final int SQRT=60;
    public static final int MINUS=23;
    public static final int CRSTRANSFORM=59;
    public static final int DOMAIN=34;
    public static final int COLON=45;
    public static final int CUBIC=102;
    public static final int UNSIGNED=95;
    public static final int NULLSET=31;
    public static final int BIT=77;
    public static final int LONG=90;
    public static final int INTERPOLATIONSET=33;
    public static final int SETCRSSET=52;
    public static final int HALF=105;
    public static final int STRING=80;
    public static final int NEAREST=99;

    // delegates
    // delegators

    public wcpsLexer() {;} 
    public wcpsLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public wcpsLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "wcps.g"; }

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:434:5: ( '+' )
            // wcps.g:434:8: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:435:6: ( '-' )
            // wcps.g:435:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "DIVIDE"
    public final void mDIVIDE() throws RecognitionException {
        try {
            int _type = DIVIDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:436:7: ( '/' )
            // wcps.g:436:9: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DIVIDE"

    // $ANTLR start "MULT"
    public final void mMULT() throws RecognitionException {
        try {
            int _type = MULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:437:5: ( '*' )
            // wcps.g:437:7: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MULT"

    // $ANTLR start "EQUALS"
    public final void mEQUALS() throws RecognitionException {
        try {
            int _type = EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:438:7: ( '=' )
            // wcps.g:438:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUALS"

    // $ANTLR start "NOTEQUALS"
    public final void mNOTEQUALS() throws RecognitionException {
        try {
            int _type = NOTEQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:439:10: ( '!=' )
            // wcps.g:439:12: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOTEQUALS"

    // $ANTLR start "LT"
    public final void mLT() throws RecognitionException {
        try {
            int _type = LT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:440:3: ( '<' )
            // wcps.g:440:5: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LT"

    // $ANTLR start "GT"
    public final void mGT() throws RecognitionException {
        try {
            int _type = GT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:441:3: ( '>' )
            // wcps.g:441:5: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GT"

    // $ANTLR start "LTE"
    public final void mLTE() throws RecognitionException {
        try {
            int _type = LTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:442:4: ( '<=' )
            // wcps.g:442:6: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LTE"

    // $ANTLR start "GTE"
    public final void mGTE() throws RecognitionException {
        try {
            int _type = GTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:443:4: ( '>=' )
            // wcps.g:443:6: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GTE"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:444:4: ( '.' )
            // wcps.g:444:6: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:445:7: ( '(' )
            // wcps.g:445:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAREN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:446:7: ( ')' )
            // wcps.g:446:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAREN"

    // $ANTLR start "LBRACKET"
    public final void mLBRACKET() throws RecognitionException {
        try {
            int _type = LBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:447:9: ( '[' )
            // wcps.g:447:11: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACKET"

    // $ANTLR start "RBRACKET"
    public final void mRBRACKET() throws RecognitionException {
        try {
            int _type = RBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:448:9: ( ']' )
            // wcps.g:448:11: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRACKET"

    // $ANTLR start "LBRACE"
    public final void mLBRACE() throws RecognitionException {
        try {
            int _type = LBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:449:7: ( '{' )
            // wcps.g:449:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACE"

    // $ANTLR start "RBRACE"
    public final void mRBRACE() throws RecognitionException {
        try {
            int _type = RBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:450:7: ( '}' )
            // wcps.g:450:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRACE"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:451:6: ( ',' )
            // wcps.g:451:8: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:452:6: ( ':' )
            // wcps.g:452:8: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "SEMICOLON"
    public final void mSEMICOLON() throws RecognitionException {
        try {
            int _type = SEMICOLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:453:10: ( ';' )
            // wcps.g:453:12: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SEMICOLON"

    // $ANTLR start "FOR"
    public final void mFOR() throws RecognitionException {
        try {
            int _type = FOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:454:4: ( ( 'f' | 'F' ) ( 'o' | 'O' ) ( 'r' | 'R' ) )
            // wcps.g:454:6: ( 'f' | 'F' ) ( 'o' | 'O' ) ( 'r' | 'R' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FOR"

    // $ANTLR start "IN"
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:455:3: ( ( 'i' | 'I' ) ( 'n' | 'N' ) )
            // wcps.g:455:5: ( 'i' | 'I' ) ( 'n' | 'N' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IN"

    // $ANTLR start "WHERE"
    public final void mWHERE() throws RecognitionException {
        try {
            int _type = WHERE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:456:6: ( ( 'w' | 'W' ) ( 'h' | 'H' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'e' | 'E' ) )
            // wcps.g:456:8: ( 'w' | 'W' ) ( 'h' | 'H' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WHERE"

    // $ANTLR start "RETURN"
    public final void mRETURN() throws RecognitionException {
        try {
            int _type = RETURN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:457:7: ( ( 'r' | 'R' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'u' | 'U' ) ( 'r' | 'R' ) ( 'n' | 'N' ) )
            // wcps.g:457:9: ( 'r' | 'R' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'u' | 'U' ) ( 'r' | 'R' ) ( 'n' | 'N' )
            {
            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RETURN"

    // $ANTLR start "STORE"
    public final void mSTORE() throws RecognitionException {
        try {
            int _type = STORE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:458:6: ( ( 's' | 'S' ) ( 't' | 'T' ) ( 'o' | 'O' ) ( 'r' | 'R' ) ( 'e' | 'E' ) )
            // wcps.g:458:8: ( 's' | 'S' ) ( 't' | 'T' ) ( 'o' | 'O' ) ( 'r' | 'R' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STORE"

    // $ANTLR start "ENCODE"
    public final void mENCODE() throws RecognitionException {
        try {
            int _type = ENCODE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:459:7: ( ( 'e' | 'E' ) ( 'n' | 'N' ) ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'd' | 'D' ) ( 'e' | 'E' ) )
            // wcps.g:459:9: ( 'e' | 'E' ) ( 'n' | 'N' ) ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'd' | 'D' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ENCODE"

    // $ANTLR start "SQRT"
    public final void mSQRT() throws RecognitionException {
        try {
            int _type = SQRT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:460:5: ( ( 's' | 'S' ) ( 'q' | 'Q' ) ( 'r' | 'R' ) ( 't' | 'T' ) )
            // wcps.g:460:7: ( 's' | 'S' ) ( 'q' | 'Q' ) ( 'r' | 'R' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='Q'||input.LA(1)=='q' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SQRT"

    // $ANTLR start "SIN"
    public final void mSIN() throws RecognitionException {
        try {
            int _type = SIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:461:4: ( ( 's' | 'S' ) ( 'i' | 'I' ) ( 'n' | 'N' ) )
            // wcps.g:461:6: ( 's' | 'S' ) ( 'i' | 'I' ) ( 'n' | 'N' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SIN"

    // $ANTLR start "COS"
    public final void mCOS() throws RecognitionException {
        try {
            int _type = COS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:462:4: ( ( 'c' | 'C' ) ( 'o' | 'O' ) ( 's' | 'S' ) )
            // wcps.g:462:6: ( 'c' | 'C' ) ( 'o' | 'O' ) ( 's' | 'S' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COS"

    // $ANTLR start "TAN"
    public final void mTAN() throws RecognitionException {
        try {
            int _type = TAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:463:4: ( ( 't' | 'T' ) ( 'a' | 'A' ) ( 'n' | 'N' ) )
            // wcps.g:463:6: ( 't' | 'T' ) ( 'a' | 'A' ) ( 'n' | 'N' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TAN"

    // $ANTLR start "SINH"
    public final void mSINH() throws RecognitionException {
        try {
            int _type = SINH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:464:5: ( ( 's' | 'S' ) ( 'i' | 'I' ) ( 'n' | 'N' ) ( 'h' | 'H' ) )
            // wcps.g:464:7: ( 's' | 'S' ) ( 'i' | 'I' ) ( 'n' | 'N' ) ( 'h' | 'H' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SINH"

    // $ANTLR start "COSH"
    public final void mCOSH() throws RecognitionException {
        try {
            int _type = COSH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:465:5: ( ( 'c' | 'C' ) ( 'o' | 'O' ) ( 's' | 'S' ) ( 'h' | 'H' ) )
            // wcps.g:465:7: ( 'c' | 'C' ) ( 'o' | 'O' ) ( 's' | 'S' ) ( 'h' | 'H' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COSH"

    // $ANTLR start "TANH"
    public final void mTANH() throws RecognitionException {
        try {
            int _type = TANH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:466:5: ( ( 't' | 'T' ) ( 'a' | 'A' ) ( 'n' | 'N' ) ( 'h' | 'H' ) )
            // wcps.g:466:7: ( 't' | 'T' ) ( 'a' | 'A' ) ( 'n' | 'N' ) ( 'h' | 'H' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TANH"

    // $ANTLR start "ARCSIN"
    public final void mARCSIN() throws RecognitionException {
        try {
            int _type = ARCSIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:467:7: ( ( 'a' | 'A' ) ( 'r' | 'R' ) ( 'c' | 'C' ) ( 's' | 'S' ) ( 'i' | 'I' ) ( 'n' | 'N' ) )
            // wcps.g:467:9: ( 'a' | 'A' ) ( 'r' | 'R' ) ( 'c' | 'C' ) ( 's' | 'S' ) ( 'i' | 'I' ) ( 'n' | 'N' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ARCSIN"

    // $ANTLR start "ARCCOS"
    public final void mARCCOS() throws RecognitionException {
        try {
            int _type = ARCCOS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:468:7: ( ( 'a' | 'A' ) ( 'r' | 'R' ) ( 'c' | 'C' ) ( 'c' | 'C' ) ( 'o' | 'O' ) ( 's' | 'S' ) )
            // wcps.g:468:9: ( 'a' | 'A' ) ( 'r' | 'R' ) ( 'c' | 'C' ) ( 'c' | 'C' ) ( 'o' | 'O' ) ( 's' | 'S' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ARCCOS"

    // $ANTLR start "ARCTAN"
    public final void mARCTAN() throws RecognitionException {
        try {
            int _type = ARCTAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:469:7: ( ( 'a' | 'A' ) ( 'r' | 'R' ) ( 'c' | 'C' ) ( 't' | 'T' ) ( 'a' | 'A' ) ( 'n' | 'N' ) )
            // wcps.g:469:9: ( 'a' | 'A' ) ( 'r' | 'R' ) ( 'c' | 'C' ) ( 't' | 'T' ) ( 'a' | 'A' ) ( 'n' | 'N' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ARCTAN"

    // $ANTLR start "EXP"
    public final void mEXP() throws RecognitionException {
        try {
            int _type = EXP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:470:4: ( ( 'e' | 'E' ) ( 'x' | 'X' ) ( 'p' | 'P' ) )
            // wcps.g:470:6: ( 'e' | 'E' ) ( 'x' | 'X' ) ( 'p' | 'P' )
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXP"

    // $ANTLR start "LN"
    public final void mLN() throws RecognitionException {
        try {
            int _type = LN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:471:3: ( ( 'l' | 'L' ) ( 'n' | 'N' ) )
            // wcps.g:471:5: ( 'l' | 'L' ) ( 'n' | 'N' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LN"

    // $ANTLR start "LOG"
    public final void mLOG() throws RecognitionException {
        try {
            int _type = LOG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:472:4: ( ( 'l' | 'L' ) ( 'o' | 'O' ) ( 'g' | 'G' ) )
            // wcps.g:472:6: ( 'l' | 'L' ) ( 'o' | 'O' ) ( 'g' | 'G' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LOG"

    // $ANTLR start "ROUND"
    public final void mROUND() throws RecognitionException {
        try {
            int _type = ROUND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:473:6: ( ( 'r' | 'R' ) ( 'o' | 'O' ) ( 'u' | 'U' ) ( 'n' | 'N' ) ( 'd' | 'D' ) )
            // wcps.g:473:8: ( 'r' | 'R' ) ( 'o' | 'O' ) ( 'u' | 'U' ) ( 'n' | 'N' ) ( 'd' | 'D' )
            {
            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ROUND"

    // $ANTLR start "ABS"
    public final void mABS() throws RecognitionException {
        try {
            int _type = ABS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:474:4: ( ( 'a' | 'A' ) ( 'b' | 'B' ) ( 's' | 'S' ) )
            // wcps.g:474:6: ( 'a' | 'A' ) ( 'b' | 'B' ) ( 's' | 'S' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ABS"

    // $ANTLR start "OVERLAY"
    public final void mOVERLAY() throws RecognitionException {
        try {
            int _type = OVERLAY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:475:8: ( ( 'o' | 'O' ) ( 'v' | 'V' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'l' | 'L' ) ( 'a' | 'A' ) ( 'y' | 'Y' ) )
            // wcps.g:475:10: ( 'o' | 'O' ) ( 'v' | 'V' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'l' | 'L' ) ( 'a' | 'A' ) ( 'y' | 'Y' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OVERLAY"

    // $ANTLR start "STRUCT"
    public final void mSTRUCT() throws RecognitionException {
        try {
            int _type = STRUCT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:476:7: ( ( 's' | 'S' ) ( 't' | 'T' ) ( 'r' | 'R' ) ( 'u' | 'U' ) ( 'c' | 'C' ) ( 't' | 'T' ) )
            // wcps.g:476:9: ( 's' | 'S' ) ( 't' | 'T' ) ( 'r' | 'R' ) ( 'u' | 'U' ) ( 'c' | 'C' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRUCT"

    // $ANTLR start "RE"
    public final void mRE() throws RecognitionException {
        try {
            int _type = RE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:477:3: ( ( 'r' | 'R' ) ( 'e' | 'E' ) )
            // wcps.g:477:5: ( 'r' | 'R' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RE"

    // $ANTLR start "IM"
    public final void mIM() throws RecognitionException {
        try {
            int _type = IM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:478:3: ( ( 'i' | 'I' ) ( 'm' | 'M' ) )
            // wcps.g:478:5: ( 'i' | 'I' ) ( 'm' | 'M' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IM"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:479:4: ( ( 'a' | 'A' ) ( 'n' | 'N' ) ( 'd' | 'D' ) )
            // wcps.g:479:6: ( 'a' | 'A' ) ( 'n' | 'N' ) ( 'd' | 'D' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:480:3: ( ( 'o' | 'O' ) ( 'r' | 'R' ) )
            // wcps.g:480:5: ( 'o' | 'O' ) ( 'r' | 'R' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "XOR"
    public final void mXOR() throws RecognitionException {
        try {
            int _type = XOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:481:4: ( ( 'x' | 'X' ) ( 'o' | 'O' ) ( 'r' | 'R' ) )
            // wcps.g:481:6: ( 'x' | 'X' ) ( 'o' | 'O' ) ( 'r' | 'R' )
            {
            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XOR"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:482:4: ( ( 'n' | 'N' ) ( 'o' | 'O' ) ( 't' | 'T' ) )
            // wcps.g:482:6: ( 'n' | 'N' ) ( 'o' | 'O' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "IDENTIFIER"
    public final void mIDENTIFIER() throws RecognitionException {
        try {
            int _type = IDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:483:11: ( ( 'i' | 'I' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'f' | 'F' ) ( 'i' | 'I' ) ( 'e' | 'E' ) ( 'r' | 'R' ) )
            // wcps.g:483:13: ( 'i' | 'I' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'f' | 'F' ) ( 'i' | 'I' ) ( 'e' | 'E' ) ( 'r' | 'R' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IDENTIFIER"

    // $ANTLR start "IMAGECRS"
    public final void mIMAGECRS() throws RecognitionException {
        try {
            int _type = IMAGECRS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:484:9: ( ( 'i' | 'I' ) ( 'm' | 'M' ) ( 'a' | 'A' ) ( 'g' | 'G' ) ( 'e' | 'E' ) ( 'c' | 'C' ) ( 'r' | 'R' ) ( 's' | 'S' ) )
            // wcps.g:484:11: ( 'i' | 'I' ) ( 'm' | 'M' ) ( 'a' | 'A' ) ( 'g' | 'G' ) ( 'e' | 'E' ) ( 'c' | 'C' ) ( 'r' | 'R' ) ( 's' | 'S' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IMAGECRS"

    // $ANTLR start "IMAGECRSDOMAIN"
    public final void mIMAGECRSDOMAIN() throws RecognitionException {
        try {
            int _type = IMAGECRSDOMAIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:485:15: ( ( 'i' | 'I' ) ( 'm' | 'M' ) ( 'a' | 'A' ) ( 'g' | 'G' ) ( 'e' | 'E' ) ( 'c' | 'C' ) ( 'r' | 'R' ) ( 's' | 'S' ) ( 'd' | 'D' ) ( 'o' | 'O' ) ( 'm' | 'M' ) ( 'a' | 'A' ) ( 'i' | 'I' ) ( 'n' | 'N' ) )
            // wcps.g:485:17: ( 'i' | 'I' ) ( 'm' | 'M' ) ( 'a' | 'A' ) ( 'g' | 'G' ) ( 'e' | 'E' ) ( 'c' | 'C' ) ( 'r' | 'R' ) ( 's' | 'S' ) ( 'd' | 'D' ) ( 'o' | 'O' ) ( 'm' | 'M' ) ( 'a' | 'A' ) ( 'i' | 'I' ) ( 'n' | 'N' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IMAGECRSDOMAIN"

    // $ANTLR start "CRSSET"
    public final void mCRSSET() throws RecognitionException {
        try {
            int _type = CRSSET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:486:7: ( ( 'c' | 'C' ) ( 'r' | 'R' ) ( 's' | 'S' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) )
            // wcps.g:486:9: ( 'c' | 'C' ) ( 'r' | 'R' ) ( 's' | 'S' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CRSSET"

    // $ANTLR start "DOMAIN"
    public final void mDOMAIN() throws RecognitionException {
        try {
            int _type = DOMAIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:487:7: ( ( 'd' | 'D' ) ( 'o' | 'O' ) ( 'm' | 'M' ) ( 'a' | 'A' ) ( 'i' | 'I' ) ( 'n' | 'N' ) )
            // wcps.g:487:9: ( 'd' | 'D' ) ( 'o' | 'O' ) ( 'm' | 'M' ) ( 'a' | 'A' ) ( 'i' | 'I' ) ( 'n' | 'N' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOMAIN"

    // $ANTLR start "NULLSET"
    public final void mNULLSET() throws RecognitionException {
        try {
            int _type = NULLSET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:488:8: ( ( 'n' | 'N' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 'l' | 'L' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) )
            // wcps.g:488:10: ( 'n' | 'N' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 'l' | 'L' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NULLSET"

    // $ANTLR start "NULLDEFAULT"
    public final void mNULLDEFAULT() throws RecognitionException {
        try {
            int _type = NULLDEFAULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:489:12: ( ( 'n' | 'N' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 'l' | 'L' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 't' | 'T' ) )
            // wcps.g:489:14: ( 'n' | 'N' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 'l' | 'L' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NULLDEFAULT"

    // $ANTLR start "INTERPOLATIONDEFAULT"
    public final void mINTERPOLATIONDEFAULT() throws RecognitionException {
        try {
            int _type = INTERPOLATIONDEFAULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:490:21: ( ( 'i' | 'I' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'p' | 'P' ) ( 'o' | 'O' ) ( 'l' | 'L' ) ( 'a' | 'A' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 't' | 'T' ) )
            // wcps.g:490:23: ( 'i' | 'I' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'p' | 'P' ) ( 'o' | 'O' ) ( 'l' | 'L' ) ( 'a' | 'A' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INTERPOLATIONDEFAULT"

    // $ANTLR start "INTERPOLATIONSET"
    public final void mINTERPOLATIONSET() throws RecognitionException {
        try {
            int _type = INTERPOLATIONSET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:491:17: ( ( 'i' | 'I' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'p' | 'P' ) ( 'o' | 'O' ) ( 'l' | 'L' ) ( 'a' | 'A' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) )
            // wcps.g:491:19: ( 'i' | 'I' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'p' | 'P' ) ( 'o' | 'O' ) ( 'l' | 'L' ) ( 'a' | 'A' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INTERPOLATIONSET"

    // $ANTLR start "SETIDENTIFIER"
    public final void mSETIDENTIFIER() throws RecognitionException {
        try {
            int _type = SETIDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:492:14: ( ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'f' | 'F' ) ( 'i' | 'I' ) ( 'e' | 'E' ) ( 'r' | 'R' ) )
            // wcps.g:492:16: ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'f' | 'F' ) ( 'i' | 'I' ) ( 'e' | 'E' ) ( 'r' | 'R' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SETIDENTIFIER"

    // $ANTLR start "SETNULLSET"
    public final void mSETNULLSET() throws RecognitionException {
        try {
            int _type = SETNULLSET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:493:11: ( ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'n' | 'N' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 'l' | 'L' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) )
            // wcps.g:493:13: ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'n' | 'N' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 'l' | 'L' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SETNULLSET"

    // $ANTLR start "SETINTERPOLATIONDEFAULT"
    public final void mSETINTERPOLATIONDEFAULT() throws RecognitionException {
        try {
            int _type = SETINTERPOLATIONDEFAULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:494:24: ( ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'p' | 'P' ) ( 'o' | 'O' ) ( 'l' | 'L' ) ( 'a' | 'A' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 't' | 'T' ) )
            // wcps.g:494:26: ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'p' | 'P' ) ( 'o' | 'O' ) ( 'l' | 'L' ) ( 'a' | 'A' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SETINTERPOLATIONDEFAULT"

    // $ANTLR start "SETINTERPOLATIONSET"
    public final void mSETINTERPOLATIONSET() throws RecognitionException {
        try {
            int _type = SETINTERPOLATIONSET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:495:20: ( ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'p' | 'P' ) ( 'o' | 'O' ) ( 'l' | 'L' ) ( 'a' | 'A' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) )
            // wcps.g:495:21: ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'n' | 'N' ) ( 't' | 'T' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'p' | 'P' ) ( 'o' | 'O' ) ( 'l' | 'L' ) ( 'a' | 'A' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SETINTERPOLATIONSET"

    // $ANTLR start "SETCRSSET"
    public final void mSETCRSSET() throws RecognitionException {
        try {
            int _type = SETCRSSET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:496:10: ( ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'c' | 'C' ) ( 'r' | 'R' ) ( 's' | 'S' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) )
            // wcps.g:496:12: ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' ) ( 'c' | 'C' ) ( 'r' | 'R' ) ( 's' | 'S' ) ( 's' | 'S' ) ( 'e' | 'E' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SETCRSSET"

    // $ANTLR start "TRIM"
    public final void mTRIM() throws RecognitionException {
        try {
            int _type = TRIM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:497:5: ( ( 't' | 'T' ) ( 'r' | 'R' ) ( 'i' | 'I' ) ( 'm' | 'M' ) )
            // wcps.g:497:7: ( 't' | 'T' ) ( 'r' | 'R' ) ( 'i' | 'I' ) ( 'm' | 'M' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TRIM"

    // $ANTLR start "SLICE"
    public final void mSLICE() throws RecognitionException {
        try {
            int _type = SLICE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:498:6: ( ( 's' | 'S' ) ( 'l' | 'L' ) ( 'i' | 'I' ) ( 'c' | 'C' ) ( 'e' | 'E' ) )
            // wcps.g:498:8: ( 's' | 'S' ) ( 'l' | 'L' ) ( 'i' | 'I' ) ( 'c' | 'C' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SLICE"

    // $ANTLR start "EXTEND"
    public final void mEXTEND() throws RecognitionException {
        try {
            int _type = EXTEND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:499:7: ( ( 'e' | 'E' ) ( 'x' | 'X' ) ( 't' | 'T' ) ( 'e' | 'E' ) ( 'n' | 'N' ) ( 'd' | 'D' ) )
            // wcps.g:499:9: ( 'e' | 'E' ) ( 'x' | 'X' ) ( 't' | 'T' ) ( 'e' | 'E' ) ( 'n' | 'N' ) ( 'd' | 'D' )
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXTEND"

    // $ANTLR start "SCALE"
    public final void mSCALE() throws RecognitionException {
        try {
            int _type = SCALE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:500:6: ( ( 's' | 'S' ) ( 'c' | 'C' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 'e' | 'E' ) )
            // wcps.g:500:8: ( 's' | 'S' ) ( 'c' | 'C' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SCALE"

    // $ANTLR start "CRSTRANSFORM"
    public final void mCRSTRANSFORM() throws RecognitionException {
        try {
            int _type = CRSTRANSFORM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:501:13: ( ( 'c' | 'C' ) ( 'r' | 'R' ) ( 's' | 'S' ) ( 't' | 'T' ) ( 'r' | 'R' ) ( 'a' | 'A' ) ( 'n' | 'N' ) ( 's' | 'S' ) ( 'f' | 'F' ) ( 'o' | 'O' ) ( 'r' | 'R' ) ( 'm' | 'M' ) )
            // wcps.g:501:15: ( 'c' | 'C' ) ( 'r' | 'R' ) ( 's' | 'S' ) ( 't' | 'T' ) ( 'r' | 'R' ) ( 'a' | 'A' ) ( 'n' | 'N' ) ( 's' | 'S' ) ( 'f' | 'F' ) ( 'o' | 'O' ) ( 'r' | 'R' ) ( 'm' | 'M' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CRSTRANSFORM"

    // $ANTLR start "COUNT"
    public final void mCOUNT() throws RecognitionException {
        try {
            int _type = COUNT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:502:6: ( ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'u' | 'U' ) ( 'n' | 'N' ) ( 't' | 'T' ) )
            // wcps.g:502:8: ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'u' | 'U' ) ( 'n' | 'N' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COUNT"

    // $ANTLR start "ADD"
    public final void mADD() throws RecognitionException {
        try {
            int _type = ADD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:503:4: ( ( 'a' | 'A' ) ( 'd' | 'D' ) ( 'd' | 'D' ) )
            // wcps.g:503:6: ( 'a' | 'A' ) ( 'd' | 'D' ) ( 'd' | 'D' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ADD"

    // $ANTLR start "AVG"
    public final void mAVG() throws RecognitionException {
        try {
            int _type = AVG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:504:4: ( ( 'a' | 'A' ) ( 'v' | 'V' ) ( 'g' | 'G' ) )
            // wcps.g:504:6: ( 'a' | 'A' ) ( 'v' | 'V' ) ( 'g' | 'G' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AVG"

    // $ANTLR start "MAX"
    public final void mMAX() throws RecognitionException {
        try {
            int _type = MAX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:505:4: ( ( 'm' | 'M' ) ( 'a' | 'A' ) ( 'x' | 'X' ) )
            // wcps.g:505:6: ( 'm' | 'M' ) ( 'a' | 'A' ) ( 'x' | 'X' )
            {
            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MAX"

    // $ANTLR start "MIN"
    public final void mMIN() throws RecognitionException {
        try {
            int _type = MIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:506:4: ( ( 'm' | 'M' ) ( 'i' | 'I' ) ( 'n' | 'N' ) )
            // wcps.g:506:6: ( 'm' | 'M' ) ( 'i' | 'I' ) ( 'n' | 'N' )
            {
            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MIN"

    // $ANTLR start "SOME"
    public final void mSOME() throws RecognitionException {
        try {
            int _type = SOME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:507:5: ( ( 's' | 'S' ) ( 'o' | 'O' ) ( 'm' | 'M' ) ( 'e' | 'E' ) )
            // wcps.g:507:7: ( 's' | 'S' ) ( 'o' | 'O' ) ( 'm' | 'M' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SOME"

    // $ANTLR start "ALL"
    public final void mALL() throws RecognitionException {
        try {
            int _type = ALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:508:4: ( ( 'a' | 'A' ) ( 'l' | 'L' ) ( 'l' | 'L' ) )
            // wcps.g:508:6: ( 'a' | 'A' ) ( 'l' | 'L' ) ( 'l' | 'L' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ALL"

    // $ANTLR start "COVERAGE"
    public final void mCOVERAGE() throws RecognitionException {
        try {
            int _type = COVERAGE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:509:9: ( ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'v' | 'V' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'a' | 'A' ) ( 'g' | 'G' ) ( 'e' | 'E' ) )
            // wcps.g:509:11: ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'v' | 'V' ) ( 'e' | 'E' ) ( 'r' | 'R' ) ( 'a' | 'A' ) ( 'g' | 'G' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COVERAGE"

    // $ANTLR start "OVER"
    public final void mOVER() throws RecognitionException {
        try {
            int _type = OVER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:510:5: ( ( 'o' | 'O' ) ( 'v' | 'V' ) ( 'e' | 'E' ) ( 'r' | 'R' ) )
            // wcps.g:510:7: ( 'o' | 'O' ) ( 'v' | 'V' ) ( 'e' | 'E' ) ( 'r' | 'R' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OVER"

    // $ANTLR start "VALUE"
    public final void mVALUE() throws RecognitionException {
        try {
            int _type = VALUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:511:6: ( ( 'v' | 'V' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 'u' | 'U' ) ( 'e' | 'E' ) )
            // wcps.g:511:8: ( 'v' | 'V' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 'u' | 'U' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "VALUE"

    // $ANTLR start "VALUES"
    public final void mVALUES() throws RecognitionException {
        try {
            int _type = VALUES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:512:7: ( ( 'v' | 'V' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 'u' | 'U' ) ( 'e' | 'E' ) ( 's' | 'S' ) )
            // wcps.g:512:9: ( 'v' | 'V' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 'u' | 'U' ) ( 'e' | 'E' ) ( 's' | 'S' )
            {
            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "VALUES"

    // $ANTLR start "LIST"
    public final void mLIST() throws RecognitionException {
        try {
            int _type = LIST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:513:5: ( ( 'l' | 'L' ) ( 'i' | 'I' ) ( 's' | 'S' ) ( 't' | 'T' ) )
            // wcps.g:513:7: ( 'l' | 'L' ) ( 'i' | 'I' ) ( 's' | 'S' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LIST"

    // $ANTLR start "CONDENSE"
    public final void mCONDENSE() throws RecognitionException {
        try {
            int _type = CONDENSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:514:9: ( ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'n' | 'N' ) ( 's' | 'S' ) ( 'e' | 'E' ) )
            // wcps.g:514:11: ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'n' | 'N' ) ( 's' | 'S' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CONDENSE"

    // $ANTLR start "USING"
    public final void mUSING() throws RecognitionException {
        try {
            int _type = USING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:515:6: ( ( 'u' | 'U' ) ( 's' | 'S' ) ( 'i' | 'I' ) ( 'n' | 'N' ) ( 'g' | 'G' ) )
            // wcps.g:515:8: ( 'u' | 'U' ) ( 's' | 'S' ) ( 'i' | 'I' ) ( 'n' | 'N' ) ( 'g' | 'G' )
            {
            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "USING"

    // $ANTLR start "NEAREST"
    public final void mNEAREST() throws RecognitionException {
        try {
            int _type = NEAREST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:516:8: ( ( 'n' | 'N' ) ( 'e' | 'E' ) ( 'a' | 'A' ) ( 'r' | 'R' ) ( 'e' | 'E' ) ( 's' | 'S' ) ( 't' | 'T' ) )
            // wcps.g:516:10: ( 'n' | 'N' ) ( 'e' | 'E' ) ( 'a' | 'A' ) ( 'r' | 'R' ) ( 'e' | 'E' ) ( 's' | 'S' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NEAREST"

    // $ANTLR start "LINEAR"
    public final void mLINEAR() throws RecognitionException {
        try {
            int _type = LINEAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:517:7: ( ( 'l' | 'L' ) ( 'i' | 'I' ) ( 'n' | 'N' ) ( 'e' | 'E' ) ( 'a' | 'A' ) ( 'r' | 'R' ) )
            // wcps.g:517:9: ( 'l' | 'L' ) ( 'i' | 'I' ) ( 'n' | 'N' ) ( 'e' | 'E' ) ( 'a' | 'A' ) ( 'r' | 'R' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LINEAR"

    // $ANTLR start "QUADRATIC"
    public final void mQUADRATIC() throws RecognitionException {
        try {
            int _type = QUADRATIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:518:10: ( ( 'q' | 'Q' ) ( 'u' | 'U' ) ( 'a' | 'A' ) ( 'd' | 'D' ) ( 'r' | 'R' ) ( 'a' | 'A' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'c' | 'C' ) )
            // wcps.g:518:12: ( 'q' | 'Q' ) ( 'u' | 'U' ) ( 'a' | 'A' ) ( 'd' | 'D' ) ( 'r' | 'R' ) ( 'a' | 'A' ) ( 't' | 'T' ) ( 'i' | 'I' ) ( 'c' | 'C' )
            {
            if ( input.LA(1)=='Q'||input.LA(1)=='q' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "QUADRATIC"

    // $ANTLR start "CUBIC"
    public final void mCUBIC() throws RecognitionException {
        try {
            int _type = CUBIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:519:6: ( ( 'c' | 'C' ) ( 'u' | 'U' ) ( 'b' | 'B' ) ( 'i' | 'I' ) ( 'c' | 'C' ) )
            // wcps.g:519:8: ( 'c' | 'C' ) ( 'u' | 'U' ) ( 'b' | 'B' ) ( 'i' | 'I' ) ( 'c' | 'C' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CUBIC"

    // $ANTLR start "FULL"
    public final void mFULL() throws RecognitionException {
        try {
            int _type = FULL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:520:5: ( ( 'f' | 'F' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 'l' | 'L' ) )
            // wcps.g:520:7: ( 'f' | 'F' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 'l' | 'L' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FULL"

    // $ANTLR start "NONE"
    public final void mNONE() throws RecognitionException {
        try {
            int _type = NONE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:521:5: ( ( 'n' | 'N' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 'e' | 'E' ) )
            // wcps.g:521:7: ( 'n' | 'N' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NONE"

    // $ANTLR start "HALF"
    public final void mHALF() throws RecognitionException {
        try {
            int _type = HALF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:522:5: ( ( 'h' | 'H' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 'f' | 'F' ) )
            // wcps.g:522:7: ( 'h' | 'H' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 'f' | 'F' )
            {
            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HALF"

    // $ANTLR start "OTHER"
    public final void mOTHER() throws RecognitionException {
        try {
            int _type = OTHER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:523:6: ( ( 'o' | 'O' ) ( 't' | 'T' ) ( 'h' | 'H' ) ( 'e' | 'E' ) ( 'r' | 'R' ) )
            // wcps.g:523:8: ( 'o' | 'O' ) ( 't' | 'T' ) ( 'h' | 'H' ) ( 'e' | 'E' ) ( 'r' | 'R' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OTHER"

    // $ANTLR start "PHI"
    public final void mPHI() throws RecognitionException {
        try {
            int _type = PHI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:524:4: ( ( 'p' | 'P' ) ( 'h' | 'H' ) ( 'i' | 'I' ) )
            // wcps.g:524:6: ( 'p' | 'P' ) ( 'h' | 'H' ) ( 'i' | 'I' )
            {
            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PHI"

    // $ANTLR start "BIT"
    public final void mBIT() throws RecognitionException {
        try {
            int _type = BIT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:525:4: ( ( 'b' | 'B' ) ( 'i' | 'I' ) ( 't' | 'T' ) )
            // wcps.g:525:6: ( 'b' | 'B' ) ( 'i' | 'I' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BIT"

    // $ANTLR start "UNSIGNED"
    public final void mUNSIGNED() throws RecognitionException {
        try {
            int _type = UNSIGNED;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:526:9: ( ( 'u' | 'U' ) ( 'n' | 'N' ) ( 's' | 'S' ) ( 'i' | 'I' ) ( 'g' | 'G' ) ( 'n' | 'N' ) ( 'e' | 'E' ) ( 'd' | 'D' ) )
            // wcps.g:526:11: ( 'u' | 'U' ) ( 'n' | 'N' ) ( 's' | 'S' ) ( 'i' | 'I' ) ( 'g' | 'G' ) ( 'n' | 'N' ) ( 'e' | 'E' ) ( 'd' | 'D' )
            {
            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UNSIGNED"

    // $ANTLR start "BOOLEAN"
    public final void mBOOLEAN() throws RecognitionException {
        try {
            int _type = BOOLEAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:527:8: ( ( 'b' | 'B' ) ( 'o' | 'O' ) ( 'o' | 'O' ) ( 'l' | 'L' ) ( 'e' | 'E' ) ( 'a' | 'A' ) ( 'n' | 'N' ) )
            // wcps.g:527:10: ( 'b' | 'B' ) ( 'o' | 'O' ) ( 'o' | 'O' ) ( 'l' | 'L' ) ( 'e' | 'E' ) ( 'a' | 'A' ) ( 'n' | 'N' )
            {
            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BOOLEAN"

    // $ANTLR start "CHAR"
    public final void mCHAR() throws RecognitionException {
        try {
            int _type = CHAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:528:5: ( ( 'c' | 'C' ) ( 'h' | 'H' ) ( 'a' | 'A' ) ( 'r' | 'R' ) )
            // wcps.g:528:7: ( 'c' | 'C' ) ( 'h' | 'H' ) ( 'a' | 'A' ) ( 'r' | 'R' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CHAR"

    // $ANTLR start "SHORT"
    public final void mSHORT() throws RecognitionException {
        try {
            int _type = SHORT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:529:6: ( ( 's' | 'S' ) ( 'h' | 'H' ) ( 'o' | 'O' ) ( 'r' | 'R' ) ( 't' | 'T' ) )
            // wcps.g:529:8: ( 's' | 'S' ) ( 'h' | 'H' ) ( 'o' | 'O' ) ( 'r' | 'R' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SHORT"

    // $ANTLR start "LONG"
    public final void mLONG() throws RecognitionException {
        try {
            int _type = LONG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:530:5: ( ( 'l' | 'L' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 'g' | 'G' ) )
            // wcps.g:530:7: ( 'l' | 'L' ) ( 'o' | 'O' ) ( 'n' | 'N' ) ( 'g' | 'G' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LONG"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:531:6: ( ( 'f' | 'F' ) ( 'l' | 'L' ) ( 'o' | 'O' ) ( 'a' | 'A' ) ( 't' | 'T' ) )
            // wcps.g:531:8: ( 'f' | 'F' ) ( 'l' | 'L' ) ( 'o' | 'O' ) ( 'a' | 'A' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "DOUBLE"
    public final void mDOUBLE() throws RecognitionException {
        try {
            int _type = DOUBLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:532:7: ( ( 'd' | 'D' ) ( 'o' | 'O' ) ( 'u' | 'U' ) ( 'b' | 'B' ) ( 'l' | 'L' ) ( 'e' | 'E' ) )
            // wcps.g:532:9: ( 'd' | 'D' ) ( 'o' | 'O' ) ( 'u' | 'U' ) ( 'b' | 'B' ) ( 'l' | 'L' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOUBLE"

    // $ANTLR start "COMPLEX"
    public final void mCOMPLEX() throws RecognitionException {
        try {
            int _type = COMPLEX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:533:8: ( ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'm' | 'M' ) ( 'p' | 'P' ) ( 'l' | 'L' ) ( 'e' | 'E' ) ( 'x' | 'X' ) )
            // wcps.g:533:10: ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'm' | 'M' ) ( 'p' | 'P' ) ( 'l' | 'L' ) ( 'e' | 'E' ) ( 'x' | 'X' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPLEX"

    // $ANTLR start "COMPLEX2"
    public final void mCOMPLEX2() throws RecognitionException {
        try {
            int _type = COMPLEX2;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:534:9: ( ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'm' | 'M' ) ( 'p' | 'P' ) ( 'l' | 'L' ) ( 'e' | 'E' ) ( 'x' | 'X' ) '2' )
            // wcps.g:534:11: ( 'c' | 'C' ) ( 'o' | 'O' ) ( 'm' | 'M' ) ( 'p' | 'P' ) ( 'l' | 'L' ) ( 'e' | 'E' ) ( 'x' | 'X' ) '2'
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            match('2'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMPLEX2"

    // $ANTLR start "SWITCH"
    public final void mSWITCH() throws RecognitionException {
        try {
            int _type = SWITCH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:535:7: ( ( 's' | 'S' ) ( 'w' | 'W' ) ( 'i' | 'I' ) ( 't' | 'T' ) ( 'c' | 'C' ) ( 'h' | 'H' ) )
            // wcps.g:535:9: ( 's' | 'S' ) ( 'w' | 'W' ) ( 'i' | 'I' ) ( 't' | 'T' ) ( 'c' | 'C' ) ( 'h' | 'H' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SWITCH"

    // $ANTLR start "CASE"
    public final void mCASE() throws RecognitionException {
        try {
            int _type = CASE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:536:5: ( ( 'c' | 'C' ) ( 'a' | 'A' ) ( 's' | 'S' ) ( 'e' | 'E' ) )
            // wcps.g:536:7: ( 'c' | 'C' ) ( 'a' | 'A' ) ( 's' | 'S' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CASE"

    // $ANTLR start "DEFAULT"
    public final void mDEFAULT() throws RecognitionException {
        try {
            int _type = DEFAULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:537:8: ( ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 't' | 'T' ) )
            // wcps.g:537:10: ( 'd' | 'D' ) ( 'e' | 'E' ) ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'u' | 'U' ) ( 'l' | 'L' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DEFAULT"

    // $ANTLR start "BOOLEANCONSTANT"
    public final void mBOOLEANCONSTANT() throws RecognitionException {
        try {
            int _type = BOOLEANCONSTANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:538:16: ( ( ( 't' | 'T' ) ( 'r' | 'R' ) ( 'u' | 'U' ) ( 'e' | 'E' ) ) | ( ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 's' | 'S' ) ( 'e' | 'E' ) ) )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='T'||LA1_0=='t') ) {
                alt1=1;
            }
            else if ( (LA1_0=='F'||LA1_0=='f') ) {
                alt1=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // wcps.g:538:18: ( ( 't' | 'T' ) ( 'r' | 'R' ) ( 'u' | 'U' ) ( 'e' | 'E' ) )
                    {
                    // wcps.g:538:18: ( ( 't' | 'T' ) ( 'r' | 'R' ) ( 'u' | 'U' ) ( 'e' | 'E' ) )
                    // wcps.g:538:19: ( 't' | 'T' ) ( 'r' | 'R' ) ( 'u' | 'U' ) ( 'e' | 'E' )
                    {
                    if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }


                    }
                    break;
                case 2 :
                    // wcps.g:538:57: ( ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 's' | 'S' ) ( 'e' | 'E' ) )
                    {
                    // wcps.g:538:57: ( ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 's' | 'S' ) ( 'e' | 'E' ) )
                    // wcps.g:538:58: ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 's' | 'S' ) ( 'e' | 'E' )
                    {
                    if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BOOLEANCONSTANT"

    // $ANTLR start "INTEGERCONSTANT"
    public final void mINTEGERCONSTANT() throws RecognitionException {
        try {
            int _type = INTEGERCONSTANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:539:16: ( ( PLUS | MINUS )? ( '0' .. '9' )+ )
            // wcps.g:539:19: ( PLUS | MINUS )? ( '0' .. '9' )+
            {
            // wcps.g:539:19: ( PLUS | MINUS )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='+'||LA2_0=='-') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // wcps.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            // wcps.g:539:33: ( '0' .. '9' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
		    // wcps.g:539:34: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INTEGERCONSTANT"

    // $ANTLR start "FLOATCONSTANT"
    public final void mFLOATCONSTANT() throws RecognitionException {
        try {
            int _type = FLOATCONSTANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:540:14: ( INTEGERCONSTANT ( '.' ) ( ( '0' .. '9' )+ ) ( ( 'e' | 'E' ) ( ( '-' | '+' )? ) ( ( '0' .. '9' )+ ) )? )
            // wcps.g:540:16: INTEGERCONSTANT ( '.' ) ( ( '0' .. '9' )+ ) ( ( 'e' | 'E' ) ( ( '-' | '+' )? ) ( ( '0' .. '9' )+ ) )?
            {
            mINTEGERCONSTANT(); 
            // wcps.g:540:32: ( '.' )
            // wcps.g:540:33: '.'
            {
            match('.'); 

            }

            // wcps.g:540:37: ( ( '0' .. '9' )+ )
            // wcps.g:540:38: ( '0' .. '9' )+
            {
            // wcps.g:540:38: ( '0' .. '9' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
		    // wcps.g:540:38: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);


            }

            // wcps.g:540:48: ( ( 'e' | 'E' ) ( ( '-' | '+' )? ) ( ( '0' .. '9' )+ ) )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='E'||LA7_0=='e') ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // wcps.g:540:49: ( 'e' | 'E' ) ( ( '-' | '+' )? ) ( ( '0' .. '9' )+ )
                    {
                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // wcps.g:540:58: ( ( '-' | '+' )? )
                    // wcps.g:540:59: ( '-' | '+' )?
                    {
                    // wcps.g:540:59: ( '-' | '+' )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0=='+'||LA5_0=='-') ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // wcps.g:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();

                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }


                    }

                    // wcps.g:540:70: ( ( '0' .. '9' )+ )
                    // wcps.g:540:71: ( '0' .. '9' )+
                    {
                    // wcps.g:540:71: ( '0' .. '9' )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( ((LA6_0>='0' && LA6_0<='9')) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
			    // wcps.g:540:71: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt6 >= 1 ) break loop6;
                                EarlyExitException eee =
                                    new EarlyExitException(6, input);
                                throw eee;
                        }
                        cnt6++;
                    } while (true);


                    }


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOATCONSTANT"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:541:7: ( '\"' ( options {greedy=false; } : . )* '\"' )
            // wcps.g:541:9: '\"' ( options {greedy=false; } : . )* '\"'
            {
            match('\"'); 
            // wcps.g:541:13: ( options {greedy=false; } : . )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0=='\"') ) {
                    alt8=2;
                }
                else if ( ((LA8_0>='\u0000' && LA8_0<='!')||(LA8_0>='#' && LA8_0<='\uFFFF')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
		    // wcps.g:541:41: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            match('\"'); 
            setText(getText().substring(1, getText().length()-1));

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "NAME"
    public final void mNAME() throws RecognitionException {
        try {
            int _type = NAME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:542:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* ) )
            // wcps.g:542:7: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // wcps.g:542:30: ( ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // wcps.g:542:31: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            // wcps.g:542:31: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( ((LA9_0>='0' && LA9_0<='9')||(LA9_0>='A' && LA9_0<='Z')||LA9_0=='_'||(LA9_0>='a' && LA9_0<='z')) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // wcps.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NAME"

    // $ANTLR start "VARIABLE_DOLLAR"
    public final void mVARIABLE_DOLLAR() throws RecognitionException {
        try {
            int _type = VARIABLE_DOLLAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:543:16: ( '$' ( ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* ) )
            // wcps.g:543:18: '$' ( ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            {
            match('$'); 
            // wcps.g:543:21: ( ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // wcps.g:543:22: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            // wcps.g:543:22: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>='0' && LA10_0<='9')||(LA10_0>='A' && LA10_0<='Z')||LA10_0=='_'||(LA10_0>='a' && LA10_0<='z')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // wcps.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);


            }

            setText(getText().substring(1, getText().length())); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "VARIABLE_DOLLAR"

    // $ANTLR start "WHITESPACE"
    public final void mWHITESPACE() throws RecognitionException {
        try {
            int _type = WHITESPACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // wcps.g:544:11: ( ( ' ' | '\\t' | '\\r' | '\\n' | '\\u000C' )+ )
            // wcps.g:544:13: ( ' ' | '\\t' | '\\r' | '\\n' | '\\u000C' )+
            {
            // wcps.g:544:13: ( ' ' | '\\t' | '\\r' | '\\n' | '\\u000C' )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>='\t' && LA11_0<='\n')||(LA11_0>='\f' && LA11_0<='\r')||LA11_0==' ') ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // wcps.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt11 >= 1 ) break loop11;
                        EarlyExitException eee =
                            new EarlyExitException(11, input);
                        throw eee;
                }
                cnt11++;
            } while (true);

             skip(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WHITESPACE"

    public void mTokens() throws RecognitionException {
        // wcps.g:1:8: ( PLUS | MINUS | DIVIDE | MULT | EQUALS | NOTEQUALS | LT | GT | LTE | GTE | DOT | LPAREN | RPAREN | LBRACKET | RBRACKET | LBRACE | RBRACE | COMMA | COLON | SEMICOLON | FOR | IN | WHERE | RETURN | STORE | ENCODE | SQRT | SIN | COS | TAN | SINH | COSH | TANH | ARCSIN | ARCCOS | ARCTAN | EXP | LN | LOG | ROUND | ABS | OVERLAY | STRUCT | RE | IM | AND | OR | XOR | NOT | IDENTIFIER | IMAGECRS | IMAGECRSDOMAIN | CRSSET | DOMAIN | NULLSET | NULLDEFAULT | INTERPOLATIONDEFAULT | INTERPOLATIONSET | SETIDENTIFIER | SETNULLSET | SETINTERPOLATIONDEFAULT | SETINTERPOLATIONSET | SETCRSSET | TRIM | SLICE | EXTEND | SCALE | CRSTRANSFORM | COUNT | ADD | AVG | MAX | MIN | SOME | ALL | COVERAGE | OVER | VALUE | VALUES | LIST | CONDENSE | USING | NEAREST | LINEAR | QUADRATIC | CUBIC | FULL | NONE | HALF | OTHER | PHI | BIT | UNSIGNED | BOOLEAN | CHAR | SHORT | LONG | FLOAT | DOUBLE | COMPLEX | COMPLEX2 | SWITCH | CASE | DEFAULT | BOOLEANCONSTANT | INTEGERCONSTANT | FLOATCONSTANT | STRING | NAME | VARIABLE_DOLLAR | WHITESPACE )
        int alt12=111;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1 :
                // wcps.g:1:10: PLUS
                {
                mPLUS(); 

                }
                break;
            case 2 :
                // wcps.g:1:15: MINUS
                {
                mMINUS(); 

                }
                break;
            case 3 :
                // wcps.g:1:21: DIVIDE
                {
                mDIVIDE(); 

                }
                break;
            case 4 :
                // wcps.g:1:28: MULT
                {
                mMULT(); 

                }
                break;
            case 5 :
                // wcps.g:1:33: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 6 :
                // wcps.g:1:40: NOTEQUALS
                {
                mNOTEQUALS(); 

                }
                break;
            case 7 :
                // wcps.g:1:50: LT
                {
                mLT(); 

                }
                break;
            case 8 :
                // wcps.g:1:53: GT
                {
                mGT(); 

                }
                break;
            case 9 :
                // wcps.g:1:56: LTE
                {
                mLTE(); 

                }
                break;
            case 10 :
                // wcps.g:1:60: GTE
                {
                mGTE(); 

                }
                break;
            case 11 :
                // wcps.g:1:64: DOT
                {
                mDOT(); 

                }
                break;
            case 12 :
                // wcps.g:1:68: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 13 :
                // wcps.g:1:75: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 14 :
                // wcps.g:1:82: LBRACKET
                {
                mLBRACKET(); 

                }
                break;
            case 15 :
                // wcps.g:1:91: RBRACKET
                {
                mRBRACKET(); 

                }
                break;
            case 16 :
                // wcps.g:1:100: LBRACE
                {
                mLBRACE(); 

                }
                break;
            case 17 :
                // wcps.g:1:107: RBRACE
                {
                mRBRACE(); 

                }
                break;
            case 18 :
                // wcps.g:1:114: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 19 :
                // wcps.g:1:120: COLON
                {
                mCOLON(); 

                }
                break;
            case 20 :
                // wcps.g:1:126: SEMICOLON
                {
                mSEMICOLON(); 

                }
                break;
            case 21 :
                // wcps.g:1:136: FOR
                {
                mFOR(); 

                }
                break;
            case 22 :
                // wcps.g:1:140: IN
                {
                mIN(); 

                }
                break;
            case 23 :
                // wcps.g:1:143: WHERE
                {
                mWHERE(); 

                }
                break;
            case 24 :
                // wcps.g:1:149: RETURN
                {
                mRETURN(); 

                }
                break;
            case 25 :
                // wcps.g:1:156: STORE
                {
                mSTORE(); 

                }
                break;
            case 26 :
                // wcps.g:1:162: ENCODE
                {
                mENCODE(); 

                }
                break;
            case 27 :
                // wcps.g:1:169: SQRT
                {
                mSQRT(); 

                }
                break;
            case 28 :
                // wcps.g:1:174: SIN
                {
                mSIN(); 

                }
                break;
            case 29 :
                // wcps.g:1:178: COS
                {
                mCOS(); 

                }
                break;
            case 30 :
                // wcps.g:1:182: TAN
                {
                mTAN(); 

                }
                break;
            case 31 :
                // wcps.g:1:186: SINH
                {
                mSINH(); 

                }
                break;
            case 32 :
                // wcps.g:1:191: COSH
                {
                mCOSH(); 

                }
                break;
            case 33 :
                // wcps.g:1:196: TANH
                {
                mTANH(); 

                }
                break;
            case 34 :
                // wcps.g:1:201: ARCSIN
                {
                mARCSIN(); 

                }
                break;
            case 35 :
                // wcps.g:1:208: ARCCOS
                {
                mARCCOS(); 

                }
                break;
            case 36 :
                // wcps.g:1:215: ARCTAN
                {
                mARCTAN(); 

                }
                break;
            case 37 :
                // wcps.g:1:222: EXP
                {
                mEXP(); 

                }
                break;
            case 38 :
                // wcps.g:1:226: LN
                {
                mLN(); 

                }
                break;
            case 39 :
                // wcps.g:1:229: LOG
                {
                mLOG(); 

                }
                break;
            case 40 :
                // wcps.g:1:233: ROUND
                {
                mROUND(); 

                }
                break;
            case 41 :
                // wcps.g:1:239: ABS
                {
                mABS(); 

                }
                break;
            case 42 :
                // wcps.g:1:243: OVERLAY
                {
                mOVERLAY(); 

                }
                break;
            case 43 :
                // wcps.g:1:251: STRUCT
                {
                mSTRUCT(); 

                }
                break;
            case 44 :
                // wcps.g:1:258: RE
                {
                mRE(); 

                }
                break;
            case 45 :
                // wcps.g:1:261: IM
                {
                mIM(); 

                }
                break;
            case 46 :
                // wcps.g:1:264: AND
                {
                mAND(); 

                }
                break;
            case 47 :
                // wcps.g:1:268: OR
                {
                mOR(); 

                }
                break;
            case 48 :
                // wcps.g:1:271: XOR
                {
                mXOR(); 

                }
                break;
            case 49 :
                // wcps.g:1:275: NOT
                {
                mNOT(); 

                }
                break;
            case 50 :
                // wcps.g:1:279: IDENTIFIER
                {
                mIDENTIFIER(); 

                }
                break;
            case 51 :
                // wcps.g:1:290: IMAGECRS
                {
                mIMAGECRS(); 

                }
                break;
            case 52 :
                // wcps.g:1:299: IMAGECRSDOMAIN
                {
                mIMAGECRSDOMAIN(); 

                }
                break;
            case 53 :
                // wcps.g:1:314: CRSSET
                {
                mCRSSET(); 

                }
                break;
            case 54 :
                // wcps.g:1:321: DOMAIN
                {
                mDOMAIN(); 

                }
                break;
            case 55 :
                // wcps.g:1:328: NULLSET
                {
                mNULLSET(); 

                }
                break;
            case 56 :
                // wcps.g:1:336: NULLDEFAULT
                {
                mNULLDEFAULT(); 

                }
                break;
            case 57 :
                // wcps.g:1:348: INTERPOLATIONDEFAULT
                {
                mINTERPOLATIONDEFAULT(); 

                }
                break;
            case 58 :
                // wcps.g:1:369: INTERPOLATIONSET
                {
                mINTERPOLATIONSET(); 

                }
                break;
            case 59 :
                // wcps.g:1:386: SETIDENTIFIER
                {
                mSETIDENTIFIER(); 

                }
                break;
            case 60 :
                // wcps.g:1:400: SETNULLSET
                {
                mSETNULLSET(); 

                }
                break;
            case 61 :
                // wcps.g:1:411: SETINTERPOLATIONDEFAULT
                {
                mSETINTERPOLATIONDEFAULT(); 

                }
                break;
            case 62 :
                // wcps.g:1:435: SETINTERPOLATIONSET
                {
                mSETINTERPOLATIONSET(); 

                }
                break;
            case 63 :
                // wcps.g:1:455: SETCRSSET
                {
                mSETCRSSET(); 

                }
                break;
            case 64 :
                // wcps.g:1:465: TRIM
                {
                mTRIM(); 

                }
                break;
            case 65 :
                // wcps.g:1:470: SLICE
                {
                mSLICE(); 

                }
                break;
            case 66 :
                // wcps.g:1:476: EXTEND
                {
                mEXTEND(); 

                }
                break;
            case 67 :
                // wcps.g:1:483: SCALE
                {
                mSCALE(); 

                }
                break;
            case 68 :
                // wcps.g:1:489: CRSTRANSFORM
                {
                mCRSTRANSFORM(); 

                }
                break;
            case 69 :
                // wcps.g:1:502: COUNT
                {
                mCOUNT(); 

                }
                break;
            case 70 :
                // wcps.g:1:508: ADD
                {
                mADD(); 

                }
                break;
            case 71 :
                // wcps.g:1:512: AVG
                {
                mAVG(); 

                }
                break;
            case 72 :
                // wcps.g:1:516: MAX
                {
                mMAX(); 

                }
                break;
            case 73 :
                // wcps.g:1:520: MIN
                {
                mMIN(); 

                }
                break;
            case 74 :
                // wcps.g:1:524: SOME
                {
                mSOME(); 

                }
                break;
            case 75 :
                // wcps.g:1:529: ALL
                {
                mALL(); 

                }
                break;
            case 76 :
                // wcps.g:1:533: COVERAGE
                {
                mCOVERAGE(); 

                }
                break;
            case 77 :
                // wcps.g:1:542: OVER
                {
                mOVER(); 

                }
                break;
            case 78 :
                // wcps.g:1:547: VALUE
                {
                mVALUE(); 

                }
                break;
            case 79 :
                // wcps.g:1:553: VALUES
                {
                mVALUES(); 

                }
                break;
            case 80 :
                // wcps.g:1:560: LIST
                {
                mLIST(); 

                }
                break;
            case 81 :
                // wcps.g:1:565: CONDENSE
                {
                mCONDENSE(); 

                }
                break;
            case 82 :
                // wcps.g:1:574: USING
                {
                mUSING(); 

                }
                break;
            case 83 :
                // wcps.g:1:580: NEAREST
                {
                mNEAREST(); 

                }
                break;
            case 84 :
                // wcps.g:1:588: LINEAR
                {
                mLINEAR(); 

                }
                break;
            case 85 :
                // wcps.g:1:595: QUADRATIC
                {
                mQUADRATIC(); 

                }
                break;
            case 86 :
                // wcps.g:1:605: CUBIC
                {
                mCUBIC(); 

                }
                break;
            case 87 :
                // wcps.g:1:611: FULL
                {
                mFULL(); 

                }
                break;
            case 88 :
                // wcps.g:1:616: NONE
                {
                mNONE(); 

                }
                break;
            case 89 :
                // wcps.g:1:621: HALF
                {
                mHALF(); 

                }
                break;
            case 90 :
                // wcps.g:1:626: OTHER
                {
                mOTHER(); 

                }
                break;
            case 91 :
                // wcps.g:1:632: PHI
                {
                mPHI(); 

                }
                break;
            case 92 :
                // wcps.g:1:636: BIT
                {
                mBIT(); 

                }
                break;
            case 93 :
                // wcps.g:1:640: UNSIGNED
                {
                mUNSIGNED(); 

                }
                break;
            case 94 :
                // wcps.g:1:649: BOOLEAN
                {
                mBOOLEAN(); 

                }
                break;
            case 95 :
                // wcps.g:1:657: CHAR
                {
                mCHAR(); 

                }
                break;
            case 96 :
                // wcps.g:1:662: SHORT
                {
                mSHORT(); 

                }
                break;
            case 97 :
                // wcps.g:1:668: LONG
                {
                mLONG(); 

                }
                break;
            case 98 :
                // wcps.g:1:673: FLOAT
                {
                mFLOAT(); 

                }
                break;
            case 99 :
                // wcps.g:1:679: DOUBLE
                {
                mDOUBLE(); 

                }
                break;
            case 100 :
                // wcps.g:1:686: COMPLEX
                {
                mCOMPLEX(); 

                }
                break;
            case 101 :
                // wcps.g:1:694: COMPLEX2
                {
                mCOMPLEX2(); 

                }
                break;
            case 102 :
                // wcps.g:1:703: SWITCH
                {
                mSWITCH();

                }
                break;
            case 103 :
                // wcps.g:1:710: CASE
                {
                mCASE();

                }
                break;
            case 104 :
                // wcps.g:1:715: DEFAULT
                {
                mDEFAULT();

                }
                break;
            case 105 :
                // wcps.g:1:723: BOOLEANCONSTANT
                {
                mBOOLEANCONSTANT();

                }
                break;
            case 106 :
                // wcps.g:1:739: INTEGERCONSTANT
                {
                mINTEGERCONSTANT();

                }
                break;
            case 107 :
                // wcps.g:1:755: FLOATCONSTANT
                {
                mFLOATCONSTANT();

                }
                break;
            case 108 :
                // wcps.g:1:769: STRING
                {
                mSTRING();

                }
                break;
            case 109 :
                // wcps.g:1:776: NAME
                {
                mNAME();

                }
                break;
            case 110 :
                // wcps.g:1:781: VARIABLE_DOLLAR
                {
                mVARIABLE_DOLLAR();

                }
                break;
            case 111 :
                // wcps.g:1:797: WHITESPACE
                {
                mWHITESPACE(); 

                }
                break;

        }

    }


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
        "\1\uffff\1\55\1\56\4\uffff\1\60\1\62\12\uffff\25\52\1\153\12\uffff"+
        "\4\52\1\162\1\164\2\52\1\170\31\52\1\u0099\3\52\1\u009f\21\52\2"+
        "\uffff\1\u00b3\4\52\1\uffff\1\52\1\uffff\3\52\1\uffff\4\52\1\u00c1"+
        "\7\52\1\u00cb\1\52\1\u00ce\10\52\1\u00d9\3\52\1\u00df\1\u00e0\1"+
        "\u00e1\1\u00e2\1\u00e3\1\uffff\1\u00e4\4\52\1\uffff\1\52\1\u00ea"+
        "\1\u00eb\6\52\1\u00f2\1\u00f3\5\52\1\u00f9\1\u00fa\1\52\1\uffff"+
        "\1\u00fc\12\52\1\u0107\1\u0108\1\uffff\5\52\1\u010f\3\52\1\uffff"+
        "\1\52\1\u0114\1\uffff\7\52\1\u011c\1\u011d\1\u011e\1\uffff\1\u011f"+
        "\1\u0120\3\52\6\uffff\1\u0124\1\u0125\1\52\1\u0128\1\52\2\uffff"+
        "\1\u012a\5\52\2\uffff\4\52\1\u0135\2\uffff\1\52\1\uffff\1\u0137"+
        "\1\u0120\3\52\1\u013b\1\52\1\u013d\1\u013e\1\52\2\uffff\4\52\1\u0144"+
        "\1\u0145\1\uffff\1\u0146\3\52\1\uffff\1\u014a\5\52\1\u0150\5\uffff"+
        "\3\52\2\uffff\2\52\1\uffff\1\u0156\1\uffff\6\52\1\u015e\1\u015f"+
        "\2\52\1\uffff\1\52\1\uffff\3\52\1\uffff\1\u0166\2\uffff\1\u0167"+
        "\4\52\3\uffff\1\u016c\1\u016d\1\u016e\1\uffff\3\52\1\u0172\1\52"+
        "\1\uffff\1\u0174\1\u0175\1\u0176\1\u0177\1\52\1\uffff\3\52\1\u017c"+
        "\1\u017d\1\52\1\u017f\2\uffff\6\52\2\uffff\4\52\3\uffff\2\52\1\u018d"+
        "\1\uffff\1\52\4\uffff\1\u018f\1\u0190\1\52\1\u0192\2\uffff\1\u0193"+
        "\1\uffff\2\52\1\u0196\1\52\1\u0199\5\52\1\u019f\1\u01a0\1\u01a1"+
        "\1\uffff\1\52\2\uffff\1\52\2\uffff\1\u01a4\1\52\1\uffff\2\52\1\uffff"+
        "\4\52\1\u01ac\3\uffff\2\52\1\uffff\1\u01af\2\52\1\u01b2\2\52\1\u01b5"+
        "\1\uffff\2\52\1\uffff\2\52\1\uffff\2\52\1\uffff\1\52\1\u01bd\4\52"+
        "\1\u01c2\1\uffff\2\52\1\u01c6\1\52\1\uffff\2\52\1\u01ca\1\uffff"+
        "\3\52\1\uffff\2\52\1\u01d0\2\52\1\uffff\7\52\1\u01db\1\u01dc\1\52"+
        "\2\uffff\2\52\1\u01e0\1\uffff";
    static final String DFA12_eofS =
        "\u01e1\uffff";
    static final String DFA12_minS =
        "\1\11\2\60\4\uffff\2\75\12\uffff\1\101\1\104\1\110\1\105\1\103\1"+
        "\116\2\101\1\102\1\111\1\122\1\117\2\105\2\101\1\116\1\125\1\101"+
        "\1\110\1\111\1\56\12\uffff\1\122\1\114\1\117\1\114\2\60\2\105\1"+
        "\60\1\125\1\117\1\122\1\116\1\124\1\111\1\101\1\115\1\117\1\111"+
        "\1\103\1\120\1\115\1\123\1\102\1\101\1\123\1\116\1\111\1\103\1\123"+
        "\2\104\1\107\1\114\1\60\1\107\1\116\1\105\1\60\1\110\1\122\1\116"+
        "\1\114\1\101\1\115\1\106\1\130\1\116\1\114\1\111\1\123\1\101\1\114"+
        "\1\111\1\124\1\117\2\uffff\1\60\1\114\1\101\1\123\1\105\1\uffff"+
        "\1\107\1\uffff\1\116\1\122\1\125\1\uffff\1\116\1\122\1\125\1\124"+
        "\1\60\2\103\1\114\1\105\1\122\1\124\1\117\1\60\1\105\1\60\1\116"+
        "\1\105\1\104\1\120\1\123\1\111\1\122\1\105\1\60\1\115\1\105\1\103"+
        "\5\60\1\uffff\1\60\1\107\1\124\1\105\1\122\1\uffff\1\105\2\60\1"+
        "\105\1\114\1\122\1\101\1\102\1\101\2\60\1\125\1\116\1\111\1\104"+
        "\1\106\2\60\1\114\1\uffff\1\60\1\124\1\105\1\122\1\105\1\124\1\105"+
        "\1\122\1\104\1\105\1\103\2\60\1\uffff\1\104\1\125\1\122\2\105\1"+
        "\60\1\124\1\103\1\104\1\uffff\1\116\1\60\1\uffff\1\124\1\122\1\105"+
        "\1\114\1\105\1\122\1\103\3\60\1\uffff\2\60\1\111\1\117\1\101\6\uffff"+
        "\2\60\1\101\1\60\1\122\2\uffff\1\60\1\104\1\105\1\111\1\114\1\125"+
        "\2\uffff\1\105\2\107\1\122\1\60\2\uffff\1\105\1\uffff\2\60\1\120"+
        "\1\103\1\111\1\60\1\116\2\60\1\124\2\uffff\1\105\1\124\1\114\1\123"+
        "\2\60\1\uffff\1\60\1\110\1\105\1\104\1\uffff\1\60\1\101\1\116\1"+
        "\105\1\124\1\101\1\60\5\uffff\1\116\1\123\1\116\2\uffff\1\122\1"+
        "\101\1\uffff\1\60\1\uffff\2\105\1\123\1\116\1\105\1\114\2\60\1\116"+
        "\1\101\1\uffff\1\101\1\uffff\1\117\1\122\1\106\1\uffff\1\60\2\uffff"+
        "\1\60\1\116\1\105\1\114\1\123\3\uffff\3\60\1\uffff\1\107\1\123\1"+
        "\130\1\60\1\116\1\uffff\4\60\1\131\1\uffff\1\124\1\106\1\124\2\60"+
        "\1\124\1\60\2\uffff\1\105\1\124\1\116\1\114\1\123\1\111\2\uffff"+
        "\1\124\1\122\1\123\1\105\3\uffff\2\105\1\60\1\uffff\1\123\4\uffff"+
        "\2\60\1\101\1\60\2\uffff\1\60\1\uffff\1\104\1\111\1\60\1\101\1\60"+
        "\1\105\1\111\1\120\1\105\1\124\3\60\1\uffff\1\106\2\uffff\1\125"+
        "\2\uffff\1\60\1\103\1\uffff\1\124\1\117\1\uffff\1\122\1\106\1\117"+
        "\1\124\1\60\3\uffff\1\117\1\114\1\uffff\1\60\1\111\1\115\1\60\1"+
        "\111\1\114\1\60\1\uffff\1\122\1\124\1\uffff\1\117\1\101\1\uffff"+
        "\1\105\1\101\1\uffff\1\115\1\60\1\116\1\111\1\122\1\124\1\60\1\uffff"+
        "\1\104\1\116\1\60\1\111\1\uffff\2\105\1\60\1\uffff\1\117\1\106\1"+
        "\124\1\uffff\1\116\1\101\1\60\1\104\1\125\1\uffff\2\105\1\114\1"+
        "\106\2\124\1\101\2\60\1\125\2\uffff\1\114\1\124\1\60\1\uffff";
    static final String DFA12_maxS =
        "\1\175\2\71\4\uffff\2\75\12\uffff\1\165\1\156\1\150\1\157\1\167"+
        "\1\170\1\165\1\162\1\166\1\157\1\166\1\157\1\165\1\157\1\151\1\141"+
        "\1\163\1\165\1\141\1\150\1\157\1\71\12\uffff\1\162\1\154\1\157\1"+
        "\154\2\172\2\145\1\172\1\165\2\162\1\156\1\164\1\151\1\141\1\155"+
        "\1\157\1\151\1\143\1\164\1\166\1\163\1\142\1\141\1\163\1\156\1\165"+
        "\1\143\1\163\2\144\1\147\1\154\1\172\1\156\1\163\1\145\1\172\1\150"+
        "\1\162\1\164\1\154\1\141\1\165\1\146\1\170\1\156\1\154\1\151\1\163"+
        "\1\141\1\154\1\151\1\164\1\157\2\uffff\1\172\1\154\1\141\1\163\1"+
        "\145\1\uffff\1\147\1\uffff\1\156\1\162\1\165\1\uffff\1\156\1\162"+
        "\1\165\1\164\1\172\1\156\1\143\1\154\1\145\1\162\1\164\1\157\1\172"+
        "\1\145\1\172\1\156\1\145\1\144\1\160\1\164\1\151\1\162\1\145\1\172"+
        "\1\155\1\145\1\164\5\172\1\uffff\1\172\1\147\1\164\1\145\1\162\1"+
        "\uffff\1\145\2\172\1\145\1\154\1\162\1\141\1\142\1\141\2\172\1\165"+
        "\1\156\1\151\1\144\1\146\2\172\1\154\1\uffff\1\172\1\164\1\145\1"+
        "\162\1\145\1\164\1\145\1\162\1\144\1\145\1\143\2\172\1\uffff\1\156"+
        "\1\165\1\162\2\145\1\172\1\164\1\143\1\144\1\uffff\1\156\1\172\1"+
        "\uffff\1\164\1\162\1\145\1\154\1\145\1\162\1\143\3\172\1\uffff\2"+
        "\172\1\151\1\157\1\141\6\uffff\2\172\1\141\1\172\1\162\2\uffff\1"+
        "\172\1\163\1\145\1\151\1\154\1\165\2\uffff\1\145\2\147\1\162\1\172"+
        "\2\uffff\1\145\1\uffff\2\172\1\160\1\143\1\151\1\172\1\156\2\172"+
        "\1\164\2\uffff\1\145\1\164\1\154\1\163\2\172\1\uffff\1\172\1\150"+
        "\1\145\1\144\1\uffff\1\172\1\141\1\156\1\145\1\164\1\141\1\172\5"+
        "\uffff\1\156\1\163\1\156\2\uffff\1\162\1\141\1\uffff\1\172\1\uffff"+
        "\2\145\1\163\1\156\1\145\1\154\2\172\1\156\1\141\1\uffff\1\141\1"+
        "\uffff\1\157\1\162\1\146\1\uffff\1\172\2\uffff\1\172\1\156\1\145"+
        "\1\154\1\163\3\uffff\3\172\1\uffff\1\147\1\163\1\170\1\172\1\156"+
        "\1\uffff\4\172\1\171\1\uffff\1\164\1\146\1\164\2\172\1\164\1\172"+
        "\2\uffff\1\145\1\164\1\156\1\154\1\163\1\151\2\uffff\1\164\1\162"+
        "\1\163\1\145\3\uffff\2\145\1\172\1\uffff\1\163\4\uffff\2\172\1\141"+
        "\1\172\2\uffff\1\172\1\uffff\1\144\1\151\1\172\1\141\1\172\1\145"+
        "\1\151\1\160\1\145\1\164\3\172\1\uffff\1\146\2\uffff\1\165\2\uffff"+
        "\1\172\1\143\1\uffff\1\164\1\157\1\uffff\1\162\1\146\1\157\1\164"+
        "\1\172\3\uffff\1\157\1\154\1\uffff\1\172\1\151\1\155\1\172\1\151"+
        "\1\154\1\172\1\uffff\1\162\1\164\1\uffff\1\157\1\141\1\uffff\1\145"+
        "\1\141\1\uffff\1\155\1\172\1\156\1\151\1\162\1\164\1\172\1\uffff"+
        "\1\163\1\156\1\172\1\151\1\uffff\2\145\1\172\1\uffff\1\157\1\146"+
        "\1\164\1\uffff\1\156\1\141\1\172\1\163\1\165\1\uffff\2\145\1\154"+
        "\1\146\2\164\1\141\2\172\1\165\2\uffff\1\154\1\164\1\172\1\uffff";
    static final String DFA12_acceptS =
        "\3\uffff\1\3\1\4\1\5\1\6\2\uffff\1\13\1\14\1\15\1\16\1\17\1\20\1"+
        "\21\1\22\1\23\1\24\26\uffff\1\154\1\155\1\156\1\157\1\1\1\2\1\11"+
        "\1\7\1\12\1\10\70\uffff\1\152\1\153\5\uffff\1\26\1\uffff\1\55\3"+
        "\uffff\1\54\40\uffff\1\46\5\uffff\1\57\23\uffff\1\25\15\uffff\1"+
        "\34\11\uffff\1\45\2\uffff\1\35\12\uffff\1\36\5\uffff\1\51\1\56\1"+
        "\106\1\107\1\113\1\47\5\uffff\1\60\1\61\6\uffff\1\110\1\111\5\uffff"+
        "\1\133\1\134\1\uffff\1\127\12\uffff\1\33\1\37\6\uffff\1\112\4\uffff"+
        "\1\40\7\uffff\1\137\1\147\1\41\1\100\1\151\3\uffff\1\141\1\120\2"+
        "\uffff\1\115\1\uffff\1\130\12\uffff\1\131\1\uffff\1\142\3\uffff"+
        "\1\27\1\uffff\1\50\1\31\5\uffff\1\101\1\103\1\140\3\uffff\1\105"+
        "\5\uffff\1\126\5\uffff\1\132\7\uffff\1\116\1\122\6\uffff\1\30\1"+
        "\53\4\uffff\1\146\1\32\1\102\3\uffff\1\65\1\uffff\1\42\1\43\1\44"+
        "\1\124\4\uffff\1\66\1\143\1\uffff\1\117\15\uffff\1\144\1\uffff\1"+
        "\52\1\67\1\uffff\1\123\1\150\2\uffff\1\136\2\uffff\1\63\5\uffff"+
        "\1\114\1\121\1\145\2\uffff\1\135\7\uffff\1\77\2\uffff\1\125\2\uffff"+
        "\1\62\2\uffff\1\74\7\uffff\1\70\4\uffff\1\104\3\uffff\1\73\3\uffff"+
        "\1\64\5\uffff\1\72\12\uffff\1\76\1\71\3\uffff\1\75";
    static final String DFA12_specialS =
        "\u01e1\uffff}>";
    static final String[] DFA12_transitionS = {
            "\2\54\1\uffff\2\54\22\uffff\1\54\1\6\1\51\1\uffff\1\53\3\uffff"+
            "\1\12\1\13\1\4\1\1\1\20\1\2\1\11\1\3\12\50\1\21\1\22\1\7\1\5"+
            "\1\10\2\uffff\1\33\1\47\1\31\1\40\1\30\1\23\1\52\1\45\1\24\2"+
            "\52\1\34\1\41\1\37\1\35\1\46\1\44\1\26\1\27\1\32\1\43\1\42\1"+
            "\25\1\36\2\52\1\14\1\uffff\1\15\1\uffff\1\52\1\uffff\1\33\1"+
            "\47\1\31\1\40\1\30\1\23\1\52\1\45\1\24\2\52\1\34\1\41\1\37\1"+
            "\35\1\46\1\44\1\26\1\27\1\32\1\43\1\42\1\25\1\36\2\52\1\16\1"+
            "\uffff\1\17",
            "\12\50",
            "\12\50",
            "",
            "",
            "",
            "",
            "\1\57",
            "\1\61",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\66\12\uffff\1\65\2\uffff\1\63\5\uffff\1\64\13\uffff\1\66"+
            "\12\uffff\1\65\2\uffff\1\63\5\uffff\1\64",
            "\1\71\10\uffff\1\70\1\67\25\uffff\1\71\10\uffff\1\70\1\67",
            "\1\72\37\uffff\1\72",
            "\1\73\11\uffff\1\74\25\uffff\1\73\11\uffff\1\74",
            "\1\102\1\uffff\1\100\2\uffff\1\104\1\77\2\uffff\1\101\2\uffff"+
            "\1\103\1\uffff\1\76\2\uffff\1\75\2\uffff\1\105\13\uffff\1\102"+
            "\1\uffff\1\100\2\uffff\1\104\1\77\2\uffff\1\101\2\uffff\1\103"+
            "\1\uffff\1\76\2\uffff\1\75\2\uffff\1\105",
            "\1\106\11\uffff\1\107\25\uffff\1\106\11\uffff\1\107",
            "\1\114\6\uffff\1\113\6\uffff\1\110\2\uffff\1\111\2\uffff\1"+
            "\112\13\uffff\1\114\6\uffff\1\113\6\uffff\1\110\2\uffff\1\111"+
            "\2\uffff\1\112",
            "\1\115\20\uffff\1\116\16\uffff\1\115\20\uffff\1\116",
            "\1\120\1\uffff\1\122\7\uffff\1\124\1\uffff\1\121\3\uffff\1"+
            "\117\3\uffff\1\123\13\uffff\1\120\1\uffff\1\122\7\uffff\1\124"+
            "\1\uffff\1\121\3\uffff\1\117\3\uffff\1\123",
            "\1\127\4\uffff\1\125\1\126\31\uffff\1\127\4\uffff\1\125\1\126",
            "\1\131\1\uffff\1\132\1\uffff\1\130\33\uffff\1\131\1\uffff\1"+
            "\132\1\uffff\1\130",
            "\1\133\37\uffff\1\133",
            "\1\136\11\uffff\1\134\5\uffff\1\135\17\uffff\1\136\11\uffff"+
            "\1\134\5\uffff\1\135",
            "\1\140\11\uffff\1\137\25\uffff\1\140\11\uffff\1\137",
            "\1\141\7\uffff\1\142\27\uffff\1\141\7\uffff\1\142",
            "\1\143\37\uffff\1\143",
            "\1\145\4\uffff\1\144\32\uffff\1\145\4\uffff\1\144",
            "\1\146\37\uffff\1\146",
            "\1\147\37\uffff\1\147",
            "\1\150\37\uffff\1\150",
            "\1\151\5\uffff\1\152\31\uffff\1\151\5\uffff\1\152",
            "\1\154\1\uffff\12\50",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\155\37\uffff\1\155",
            "\1\156\37\uffff\1\156",
            "\1\157\37\uffff\1\157",
            "\1\160\37\uffff\1\160",
            "\12\52\7\uffff\23\52\1\161\6\52\4\uffff\1\52\1\uffff\23\52"+
            "\1\161\6\52",
            "\12\52\7\uffff\1\163\31\52\4\uffff\1\52\1\uffff\1\163\31\52",
            "\1\165\37\uffff\1\165",
            "\1\166\37\uffff\1\166",
            "\12\52\7\uffff\23\52\1\167\6\52\4\uffff\1\52\1\uffff\23\52"+
            "\1\167\6\52",
            "\1\171\37\uffff\1\171",
            "\1\172\2\uffff\1\173\34\uffff\1\172\2\uffff\1\173",
            "\1\174\37\uffff\1\174",
            "\1\175\37\uffff\1\175",
            "\1\176\37\uffff\1\176",
            "\1\177\37\uffff\1\177",
            "\1\u0080\37\uffff\1\u0080",
            "\1\u0081\37\uffff\1\u0081",
            "\1\u0082\37\uffff\1\u0082",
            "\1\u0083\37\uffff\1\u0083",
            "\1\u0084\37\uffff\1\u0084",
            "\1\u0085\3\uffff\1\u0086\33\uffff\1\u0085\3\uffff\1\u0086",
            "\1\u008b\1\u008a\4\uffff\1\u0087\1\uffff\1\u0088\1\u0089\26"+
            "\uffff\1\u008b\1\u008a\4\uffff\1\u0087\1\uffff\1\u0088\1\u0089",
            "\1\u008c\37\uffff\1\u008c",
            "\1\u008d\37\uffff\1\u008d",
            "\1\u008e\37\uffff\1\u008e",
            "\1\u008f\37\uffff\1\u008f",
            "\1\u0090\37\uffff\1\u0090",
            "\1\u0091\13\uffff\1\u0092\23\uffff\1\u0091\13\uffff\1\u0092",
            "\1\u0093\37\uffff\1\u0093",
            "\1\u0094\37\uffff\1\u0094",
            "\1\u0095\37\uffff\1\u0095",
            "\1\u0096\37\uffff\1\u0096",
            "\1\u0097\37\uffff\1\u0097",
            "\1\u0098\37\uffff\1\u0098",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u009a\6\uffff\1\u009b\30\uffff\1\u009a\6\uffff\1\u009b",
            "\1\u009d\4\uffff\1\u009c\32\uffff\1\u009d\4\uffff\1\u009c",
            "\1\u009e\37\uffff\1\u009e",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u00a0\37\uffff\1\u00a0",
            "\1\u00a1\37\uffff\1\u00a1",
            "\1\u00a3\5\uffff\1\u00a2\31\uffff\1\u00a3\5\uffff\1\u00a2",
            "\1\u00a4\37\uffff\1\u00a4",
            "\1\u00a5\37\uffff\1\u00a5",
            "\1\u00a6\7\uffff\1\u00a7\27\uffff\1\u00a6\7\uffff\1\u00a7",
            "\1\u00a8\37\uffff\1\u00a8",
            "\1\u00a9\37\uffff\1\u00a9",
            "\1\u00aa\37\uffff\1\u00aa",
            "\1\u00ab\37\uffff\1\u00ab",
            "\1\u00ac\37\uffff\1\u00ac",
            "\1\u00ad\37\uffff\1\u00ad",
            "\1\u00ae\37\uffff\1\u00ae",
            "\1\u00af\37\uffff\1\u00af",
            "\1\u00b0\37\uffff\1\u00b0",
            "\1\u00b1\37\uffff\1\u00b1",
            "\1\u00b2\37\uffff\1\u00b2",
            "",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u00b4\37\uffff\1\u00b4",
            "\1\u00b5\37\uffff\1\u00b5",
            "\1\u00b6\37\uffff\1\u00b6",
            "\1\u00b7\37\uffff\1\u00b7",
            "",
            "\1\u00b8\37\uffff\1\u00b8",
            "",
            "\1\u00b9\37\uffff\1\u00b9",
            "\1\u00ba\37\uffff\1\u00ba",
            "\1\u00bb\37\uffff\1\u00bb",
            "",
            "\1\u00bc\37\uffff\1\u00bc",
            "\1\u00bd\37\uffff\1\u00bd",
            "\1\u00be\37\uffff\1\u00be",
            "\1\u00bf\37\uffff\1\u00bf",
            "\12\52\7\uffff\7\52\1\u00c0\22\52\4\uffff\1\52\1\uffff\7\52"+
            "\1\u00c0\22\52",
            "\1\u00c4\5\uffff\1\u00c2\4\uffff\1\u00c3\24\uffff\1\u00c4\5"+
            "\uffff\1\u00c2\4\uffff\1\u00c3",
            "\1\u00c5\37\uffff\1\u00c5",
            "\1\u00c6\37\uffff\1\u00c6",
            "\1\u00c7\37\uffff\1\u00c7",
            "\1\u00c8\37\uffff\1\u00c8",
            "\1\u00c9\37\uffff\1\u00c9",
            "\1\u00ca\37\uffff\1\u00ca",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u00cc\37\uffff\1\u00cc",
            "\12\52\7\uffff\7\52\1\u00cd\22\52\4\uffff\1\52\1\uffff\7\52"+
            "\1\u00cd\22\52",
            "\1\u00cf\37\uffff\1\u00cf",
            "\1\u00d0\37\uffff\1\u00d0",
            "\1\u00d1\37\uffff\1\u00d1",
            "\1\u00d2\37\uffff\1\u00d2",
            "\1\u00d3\1\u00d4\36\uffff\1\u00d3\1\u00d4",
            "\1\u00d5\37\uffff\1\u00d5",
            "\1\u00d6\37\uffff\1\u00d6",
            "\1\u00d7\37\uffff\1\u00d7",
            "\12\52\7\uffff\7\52\1\u00d8\22\52\4\uffff\1\52\1\uffff\7\52"+
            "\1\u00d8\22\52",
            "\1\u00da\37\uffff\1\u00da",
            "\1\u00db\37\uffff\1\u00db",
            "\1\u00dd\17\uffff\1\u00dc\1\u00de\16\uffff\1\u00dd\17\uffff"+
            "\1\u00dc\1\u00de",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u00e5\37\uffff\1\u00e5",
            "\1\u00e6\37\uffff\1\u00e6",
            "\1\u00e7\37\uffff\1\u00e7",
            "\1\u00e8\37\uffff\1\u00e8",
            "",
            "\1\u00e9\37\uffff\1\u00e9",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u00ec\37\uffff\1\u00ec",
            "\1\u00ed\37\uffff\1\u00ed",
            "\1\u00ee\37\uffff\1\u00ee",
            "\1\u00ef\37\uffff\1\u00ef",
            "\1\u00f0\37\uffff\1\u00f0",
            "\1\u00f1\37\uffff\1\u00f1",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u00f4\37\uffff\1\u00f4",
            "\1\u00f5\37\uffff\1\u00f5",
            "\1\u00f6\37\uffff\1\u00f6",
            "\1\u00f7\37\uffff\1\u00f7",
            "\1\u00f8\37\uffff\1\u00f8",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u00fb\37\uffff\1\u00fb",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u00fd\37\uffff\1\u00fd",
            "\1\u00fe\37\uffff\1\u00fe",
            "\1\u00ff\37\uffff\1\u00ff",
            "\1\u0100\37\uffff\1\u0100",
            "\1\u0101\37\uffff\1\u0101",
            "\1\u0102\37\uffff\1\u0102",
            "\1\u0103\37\uffff\1\u0103",
            "\1\u0104\37\uffff\1\u0104",
            "\1\u0105\37\uffff\1\u0105",
            "\1\u0106\37\uffff\1\u0106",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\u0109\11\uffff\1\u010a\25\uffff\1\u0109\11\uffff\1\u010a",
            "\1\u010b\37\uffff\1\u010b",
            "\1\u010c\37\uffff\1\u010c",
            "\1\u010d\37\uffff\1\u010d",
            "\1\u010e\37\uffff\1\u010e",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0110\37\uffff\1\u0110",
            "\1\u0111\37\uffff\1\u0111",
            "\1\u0112\37\uffff\1\u0112",
            "",
            "\1\u0113\37\uffff\1\u0113",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\u0115\37\uffff\1\u0115",
            "\1\u0116\37\uffff\1\u0116",
            "\1\u0117\37\uffff\1\u0117",
            "\1\u0118\37\uffff\1\u0118",
            "\1\u0119\37\uffff\1\u0119",
            "\1\u011a\37\uffff\1\u011a",
            "\1\u011b\37\uffff\1\u011b",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0121\37\uffff\1\u0121",
            "\1\u0122\37\uffff\1\u0122",
            "\1\u0123\37\uffff\1\u0123",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0126\37\uffff\1\u0126",
            "\12\52\7\uffff\13\52\1\u0127\16\52\4\uffff\1\52\1\uffff\13"+
            "\52\1\u0127\16\52",
            "\1\u0129\37\uffff\1\u0129",
            "",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u012c\16\uffff\1\u012b\20\uffff\1\u012c\16\uffff\1\u012b",
            "\1\u012d\37\uffff\1\u012d",
            "\1\u012e\37\uffff\1\u012e",
            "\1\u012f\37\uffff\1\u012f",
            "\1\u0130\37\uffff\1\u0130",
            "",
            "",
            "\1\u0131\37\uffff\1\u0131",
            "\1\u0132\37\uffff\1\u0132",
            "\1\u0133\37\uffff\1\u0133",
            "\1\u0134\37\uffff\1\u0134",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "",
            "\1\u0136\37\uffff\1\u0136",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0138\37\uffff\1\u0138",
            "\1\u0139\37\uffff\1\u0139",
            "\1\u013a\37\uffff\1\u013a",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u013c\37\uffff\1\u013c",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u013f\37\uffff\1\u013f",
            "",
            "",
            "\1\u0140\37\uffff\1\u0140",
            "\1\u0141\37\uffff\1\u0141",
            "\1\u0142\37\uffff\1\u0142",
            "\1\u0143\37\uffff\1\u0143",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0147\37\uffff\1\u0147",
            "\1\u0148\37\uffff\1\u0148",
            "\1\u0149\37\uffff\1\u0149",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u014b\37\uffff\1\u014b",
            "\1\u014c\37\uffff\1\u014c",
            "\1\u014d\37\uffff\1\u014d",
            "\1\u014e\37\uffff\1\u014e",
            "\1\u014f\37\uffff\1\u014f",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "",
            "",
            "",
            "",
            "\1\u0151\37\uffff\1\u0151",
            "\1\u0152\37\uffff\1\u0152",
            "\1\u0153\37\uffff\1\u0153",
            "",
            "",
            "\1\u0154\37\uffff\1\u0154",
            "\1\u0155\37\uffff\1\u0155",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\u0157\37\uffff\1\u0157",
            "\1\u0158\37\uffff\1\u0158",
            "\1\u0159\37\uffff\1\u0159",
            "\1\u015a\37\uffff\1\u015a",
            "\1\u015b\37\uffff\1\u015b",
            "\1\u015c\37\uffff\1\u015c",
            "\12\52\7\uffff\22\52\1\u015d\7\52\4\uffff\1\52\1\uffff\22\52"+
            "\1\u015d\7\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0160\37\uffff\1\u0160",
            "\1\u0161\37\uffff\1\u0161",
            "",
            "\1\u0162\37\uffff\1\u0162",
            "",
            "\1\u0163\37\uffff\1\u0163",
            "\1\u0164\37\uffff\1\u0164",
            "\1\u0165\37\uffff\1\u0165",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0168\37\uffff\1\u0168",
            "\1\u0169\37\uffff\1\u0169",
            "\1\u016a\37\uffff\1\u016a",
            "\1\u016b\37\uffff\1\u016b",
            "",
            "",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\u016f\37\uffff\1\u016f",
            "\1\u0170\37\uffff\1\u0170",
            "\1\u0171\37\uffff\1\u0171",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0173\37\uffff\1\u0173",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0178\37\uffff\1\u0178",
            "",
            "\1\u0179\37\uffff\1\u0179",
            "\1\u017a\37\uffff\1\u017a",
            "\1\u017b\37\uffff\1\u017b",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u017e\37\uffff\1\u017e",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "",
            "\1\u0180\37\uffff\1\u0180",
            "\1\u0181\37\uffff\1\u0181",
            "\1\u0182\37\uffff\1\u0182",
            "\1\u0183\37\uffff\1\u0183",
            "\1\u0184\37\uffff\1\u0184",
            "\1\u0185\37\uffff\1\u0185",
            "",
            "",
            "\1\u0186\37\uffff\1\u0186",
            "\1\u0187\37\uffff\1\u0187",
            "\1\u0188\37\uffff\1\u0188",
            "\1\u0189\37\uffff\1\u0189",
            "",
            "",
            "",
            "\1\u018a\37\uffff\1\u018a",
            "\1\u018b\37\uffff\1\u018b",
            "\2\52\1\u018c\7\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\u018e\37\uffff\1\u018e",
            "",
            "",
            "",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0191\37\uffff\1\u0191",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\u0194\37\uffff\1\u0194",
            "\1\u0195\37\uffff\1\u0195",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u0197\37\uffff\1\u0197",
            "\12\52\7\uffff\3\52\1\u0198\26\52\4\uffff\1\52\1\uffff\3\52"+
            "\1\u0198\26\52",
            "\1\u019a\37\uffff\1\u019a",
            "\1\u019b\37\uffff\1\u019b",
            "\1\u019c\37\uffff\1\u019c",
            "\1\u019d\37\uffff\1\u019d",
            "\1\u019e\37\uffff\1\u019e",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\u01a2\37\uffff\1\u01a2",
            "",
            "",
            "\1\u01a3\37\uffff\1\u01a3",
            "",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u01a5\37\uffff\1\u01a5",
            "",
            "\1\u01a6\37\uffff\1\u01a6",
            "\1\u01a7\37\uffff\1\u01a7",
            "",
            "\1\u01a8\37\uffff\1\u01a8",
            "\1\u01a9\37\uffff\1\u01a9",
            "\1\u01aa\37\uffff\1\u01aa",
            "\1\u01ab\37\uffff\1\u01ab",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "",
            "",
            "\1\u01ad\37\uffff\1\u01ad",
            "\1\u01ae\37\uffff\1\u01ae",
            "",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u01b0\37\uffff\1\u01b0",
            "\1\u01b1\37\uffff\1\u01b1",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u01b3\37\uffff\1\u01b3",
            "\1\u01b4\37\uffff\1\u01b4",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\u01b6\37\uffff\1\u01b6",
            "\1\u01b7\37\uffff\1\u01b7",
            "",
            "\1\u01b8\37\uffff\1\u01b8",
            "\1\u01b9\37\uffff\1\u01b9",
            "",
            "\1\u01ba\37\uffff\1\u01ba",
            "\1\u01bb\37\uffff\1\u01bb",
            "",
            "\1\u01bc\37\uffff\1\u01bc",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u01be\37\uffff\1\u01be",
            "\1\u01bf\37\uffff\1\u01bf",
            "\1\u01c0\37\uffff\1\u01c0",
            "\1\u01c1\37\uffff\1\u01c1",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\u01c3\16\uffff\1\u01c4\20\uffff\1\u01c3\16\uffff\1\u01c4",
            "\1\u01c5\37\uffff\1\u01c5",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u01c7\37\uffff\1\u01c7",
            "",
            "\1\u01c8\37\uffff\1\u01c8",
            "\1\u01c9\37\uffff\1\u01c9",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "",
            "\1\u01cb\37\uffff\1\u01cb",
            "\1\u01cc\37\uffff\1\u01cc",
            "\1\u01cd\37\uffff\1\u01cd",
            "",
            "\1\u01ce\37\uffff\1\u01ce",
            "\1\u01cf\37\uffff\1\u01cf",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u01d1\16\uffff\1\u01d2\20\uffff\1\u01d1\16\uffff\1\u01d2",
            "\1\u01d3\37\uffff\1\u01d3",
            "",
            "\1\u01d4\37\uffff\1\u01d4",
            "\1\u01d5\37\uffff\1\u01d5",
            "\1\u01d6\37\uffff\1\u01d6",
            "\1\u01d7\37\uffff\1\u01d7",
            "\1\u01d8\37\uffff\1\u01d8",
            "\1\u01d9\37\uffff\1\u01d9",
            "\1\u01da\37\uffff\1\u01da",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            "\1\u01dd\37\uffff\1\u01dd",
            "",
            "",
            "\1\u01de\37\uffff\1\u01de",
            "\1\u01df\37\uffff\1\u01df",
            "\12\52\7\uffff\32\52\4\uffff\1\52\1\uffff\32\52",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( PLUS | MINUS | DIVIDE | MULT | EQUALS | NOTEQUALS | LT | GT | LTE | GTE | DOT | LPAREN | RPAREN | LBRACKET | RBRACKET | LBRACE | RBRACE | COMMA | COLON | SEMICOLON | FOR | IN | WHERE | RETURN | STORE | ENCODE | SQRT | SIN | COS | TAN | SINH | COSH | TANH | ARCSIN | ARCCOS | ARCTAN | EXP | LN | LOG | ROUND | ABS | OVERLAY | STRUCT | RE | IM | AND | OR | XOR | NOT | IDENTIFIER | IMAGECRS | IMAGECRSDOMAIN | CRSSET | DOMAIN | NULLSET | NULLDEFAULT | INTERPOLATIONDEFAULT | INTERPOLATIONSET | SETIDENTIFIER | SETNULLSET | SETINTERPOLATIONDEFAULT | SETINTERPOLATIONSET | SETCRSSET | TRIM | SLICE | EXTEND | SCALE | CRSTRANSFORM | COUNT | ADD | AVG | MAX | MIN | SOME | ALL | COVERAGE | OVER | VALUE | VALUES | LIST | CONDENSE | USING | NEAREST | LINEAR | QUADRATIC | CUBIC | FULL | NONE | HALF | OTHER | PHI | BIT | UNSIGNED | BOOLEAN | CHAR | SHORT | LONG | FLOAT | DOUBLE | COMPLEX | COMPLEX2 | SWITCH | CASE | DEFAULT | BOOLEANCONSTANT | INTEGERCONSTANT | FLOATCONSTANT | STRING | NAME | VARIABLE_DOLLAR | WHITESPACE );";
        }
    }
 

}