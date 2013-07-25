// $ANTLR 3.2 Sep 23, 2009 12:02:23 wcps.g 2013-07-25 16:22:35
package petascope.wcps.grammar;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.antlr.runtime.tree.*;

public class wcpsParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "FOR", "IN", "LPAREN", "RPAREN", "COMMA", "WHERE", "RETURN", "ENCODE", "STORE", "OR", "XOR", "AND", "EQUALS", "NOTEQUALS", "LT", "GT", "LTE", "GTE", "PLUS", "MINUS", "MULT", "DIVIDE", "OVERLAY", "IDENTIFIER", "IMAGECRS", "IMAGECRSDOMAIN", "CRSSET", "NULLSET", "INTERPOLATIONDEFAULT", "INTERPOLATIONSET", "DOMAIN", "ALL", "SOME", "COUNT", "ADD", "AVG", "MIN", "MAX", "CONDENSE", "OVER", "USING", "COLON", "COVERAGE", "VALUE", "LIST", "SEMICOLON", "VALUES", "SETIDENTIFIER", "SETCRSSET", "SETNULLSET", "SETINTERPOLATIONDEFAULT", "SETINTERPOLATIONSET", "LBRACE", "RBRACE", "STRUCT", "CRSTRANSFORM", "SQRT", "ABS", "RE", "IM", "EXP", "LOG", "LN", "SIN", "COS", "TAN", "SINH", "COSH", "TANH", "ARCSIN", "ARCCOS", "ARCTAN", "NOT", "BIT", "INTEGERCONSTANT", "ROUND", "STRING", "SCALE", "LBRACKET", "RBRACKET", "TRIM", "SLICE", "EXTEND", "BOOLEAN", "CHAR", "SHORT", "LONG", "FLOAT", "DOUBLE", "COMPLEX", "COMPLEX2", "UNSIGNED", "DOT", "BOOLEANCONSTANT", "FLOATCONSTANT", "NEAREST", "LINEAR", "QUADRATIC", "CUBIC", "FULL", "NONE", "HALF", "OTHER", "NAME", "VARIABLE_DOLLAR", "NULLDEFAULT", "PHI", "WHITESPACE"
    };
    public static final int IMAGECRS=28;
    public static final int LT=18;
    public static final int LN=66;
    public static final int LOG=65;
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
    public static final int NAME=107;
    public static final int LINEAR=100;
    public static final int TANH=72;
    public static final int FULL=103;
    public static final int USING=44;
    public static final int SIN=67;
    public static final int EXP=64;
    public static final int COS=68;
    public static final int TAN=69;
    public static final int RETURN=10;
    public static final int DOUBLE=92;
    public static final int NULLDEFAULT=109;
    public static final int DIVIDE=25;
    public static final int STORE=12;
    public static final int BOOLEANCONSTANT=97;
    public static final int RBRACE=57;
    public static final int SETNULLSET=53;
    public static final int CONDENSE=42;
    public static final int WHITESPACE=111;
    public static final int SEMICOLON=49;
    public static final int MULT=24;
    public static final int VALUE=47;
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
    public static final int PHI=110;
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
    public static final int XOR=14;
    public static final int SETINTERPOLATIONSET=55;
    public static final int OTHER=106;
    public static final int VALUES=50;
    public static final int ARCTAN=75;
    public static final int NOTEQUALS=17;
    public static final int STRUCT=58;
    public static final int SHORT=89;
    public static final int MIN=40;
    public static final int MINUS=23;
    public static final int SQRT=60;
    public static final int DOMAIN=34;
    public static final int CRSTRANSFORM=59;
    public static final int COLON=45;
    public static final int CUBIC=102;
    public static final int UNSIGNED=95;
    public static final int NULLSET=31;
    public static final int BIT=77;
    public static final int INTERPOLATIONSET=33;
    public static final int LONG=90;
    public static final int SETCRSSET=52;
    public static final int HALF=105;
    public static final int STRING=80;
    public static final int NEAREST=99;

    // delegates
    // delegators


        public wcpsParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public wcpsParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            this.state.ruleMemo = new HashMap[260+1];
             
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return wcpsParser.tokenNames; }
    public String getGrammarFileName() { return "wcps.g"; }


    public static class wcpsRequest_return extends ParserRuleReturnScope {
        public WCPSRequest value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "wcpsRequest"
    // wcps.g:37:1: wcpsRequest returns [WCPSRequest value] : e1= forClause (e2= whereClause )? e3= returnClause ;
    public final wcpsParser.wcpsRequest_return wcpsRequest() throws RecognitionException {
        wcpsParser.wcpsRequest_return retval = new wcpsParser.wcpsRequest_return();
        retval.start = input.LT(1);
        int wcpsRequest_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.forClause_return e1 = null;

        wcpsParser.whereClause_return e2 = null;

        wcpsParser.returnClause_return e3 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 1) ) { return retval; }
            // wcps.g:38:2: (e1= forClause (e2= whereClause )? e3= returnClause )
            // wcps.g:38:4: e1= forClause (e2= whereClause )? e3= returnClause
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_forClause_in_wcpsRequest63);
            e1=forClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new WCPSRequest((e1!=null?e1.value:null)); 
            }
            // wcps.g:39:3: (e2= whereClause )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==WHERE) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // wcps.g:39:4: e2= whereClause
                    {
                    pushFollow(FOLLOW_whereClause_in_wcpsRequest72);
                    e2=whereClause();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.setWhere((e2!=null?e2.value:null)); 
                    }

                    }
                    break;

            }

            pushFollow(FOLLOW_returnClause_in_wcpsRequest83);
            e3=returnClause();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
            if ( state.backtracking==0 ) {
               retval.value.setReturn((e3!=null?e3.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 1, wcpsRequest_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "wcpsRequest"

    public static class forClause_return extends ParserRuleReturnScope {
        public ForClauseElements value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "forClause"
    // wcps.g:42:1: forClause returns [ForClauseElements value] : FOR v= coverageVariable IN LPAREN list= coverageList RPAREN ( COMMA v= coverageVariable IN LPAREN list= coverageList RPAREN )* ;
    public final wcpsParser.forClause_return forClause() throws RecognitionException {
        wcpsParser.forClause_return retval = new wcpsParser.forClause_return();
        retval.start = input.LT(1);
        int forClause_StartIndex = input.index();
        Object root_0 = null;

        Token FOR1=null;
        Token IN2=null;
        Token LPAREN3=null;
        Token RPAREN4=null;
        Token COMMA5=null;
        Token IN6=null;
        Token LPAREN7=null;
        Token RPAREN8=null;
        wcpsParser.coverageVariable_return v = null;

        wcpsParser.coverageList_return list = null;


        Object FOR1_tree=null;
        Object IN2_tree=null;
        Object LPAREN3_tree=null;
        Object RPAREN4_tree=null;
        Object COMMA5_tree=null;
        Object IN6_tree=null;
        Object LPAREN7_tree=null;
        Object RPAREN8_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 2) ) { return retval; }
            // wcps.g:43:2: ( FOR v= coverageVariable IN LPAREN list= coverageList RPAREN ( COMMA v= coverageVariable IN LPAREN list= coverageList RPAREN )* )
            // wcps.g:43:4: FOR v= coverageVariable IN LPAREN list= coverageList RPAREN ( COMMA v= coverageVariable IN LPAREN list= coverageList RPAREN )*
            {
            root_0 = (Object)adaptor.nil();

            FOR1=(Token)match(input,FOR,FOLLOW_FOR_in_forClause98); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            FOR1_tree = (Object)adaptor.create(FOR1);
            adaptor.addChild(root_0, FOR1_tree);
            }
            pushFollow(FOLLOW_coverageVariable_in_forClause102);
            v=coverageVariable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, v.getTree());
            IN2=(Token)match(input,IN,FOLLOW_IN_in_forClause104); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            IN2_tree = (Object)adaptor.create(IN2);
            adaptor.addChild(root_0, IN2_tree);
            }
            LPAREN3=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_forClause106); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN3_tree = (Object)adaptor.create(LPAREN3);
            adaptor.addChild(root_0, LPAREN3_tree);
            }
            pushFollow(FOLLOW_coverageList_in_forClause110);
            list=coverageList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, list.getTree());
            RPAREN4=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_forClause112); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN4_tree = (Object)adaptor.create(RPAREN4);
            adaptor.addChild(root_0, RPAREN4_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new ForClauseElements((v!=null?v.value:null), (list!=null?list.value:null)); 
            }
            // wcps.g:45:4: ( COMMA v= coverageVariable IN LPAREN list= coverageList RPAREN )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==COMMA) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // wcps.g:45:5: COMMA v= coverageVariable IN LPAREN list= coverageList RPAREN
            	    {
            	    COMMA5=(Token)match(input,COMMA,FOLLOW_COMMA_in_forClause122); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA5_tree = (Object)adaptor.create(COMMA5);
            	    adaptor.addChild(root_0, COMMA5_tree);
            	    }
            	    pushFollow(FOLLOW_coverageVariable_in_forClause126);
            	    v=coverageVariable();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, v.getTree());
            	    IN6=(Token)match(input,IN,FOLLOW_IN_in_forClause128); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    IN6_tree = (Object)adaptor.create(IN6);
            	    adaptor.addChild(root_0, IN6_tree);
            	    }
            	    LPAREN7=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_forClause130); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    LPAREN7_tree = (Object)adaptor.create(LPAREN7);
            	    adaptor.addChild(root_0, LPAREN7_tree);
            	    }
            	    pushFollow(FOLLOW_coverageList_in_forClause134);
            	    list=coverageList();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, list.getTree());
            	    RPAREN8=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_forClause136); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    RPAREN8_tree = (Object)adaptor.create(RPAREN8);
            	    adaptor.addChild(root_0, RPAREN8_tree);
            	    }
            	    if ( state.backtracking==0 ) {
            	       retval.value = new ForClauseElements((v!=null?v.value:null), (list!=null?list.value:null), retval.value); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 2, forClause_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "forClause"

    public static class whereClause_return extends ParserRuleReturnScope {
        public WhereClause value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "whereClause"
    // wcps.g:48:1: whereClause returns [WhereClause value] : WHERE e1= booleanScalarExpr ;
    public final wcpsParser.whereClause_return whereClause() throws RecognitionException {
        wcpsParser.whereClause_return retval = new wcpsParser.whereClause_return();
        retval.start = input.LT(1);
        int whereClause_StartIndex = input.index();
        Object root_0 = null;

        Token WHERE9=null;
        wcpsParser.booleanScalarExpr_return e1 = null;


        Object WHERE9_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 3) ) { return retval; }
            // wcps.g:49:2: ( WHERE e1= booleanScalarExpr )
            // wcps.g:49:4: WHERE e1= booleanScalarExpr
            {
            root_0 = (Object)adaptor.nil();

            WHERE9=(Token)match(input,WHERE,FOLLOW_WHERE_in_whereClause157); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            WHERE9_tree = (Object)adaptor.create(WHERE9);
            adaptor.addChild(root_0, WHERE9_tree);
            }
            pushFollow(FOLLOW_booleanScalarExpr_in_whereClause161);
            e1=booleanScalarExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new WhereClause((e1!=null?e1.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 3, whereClause_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "whereClause"

    public static class returnClause_return extends ParserRuleReturnScope {
        public ReturnClause value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "returnClause"
    // wcps.g:51:1: returnClause returns [ReturnClause value] : RETURN e1= processingExpr ;
    public final wcpsParser.returnClause_return returnClause() throws RecognitionException {
        wcpsParser.returnClause_return retval = new wcpsParser.returnClause_return();
        retval.start = input.LT(1);
        int returnClause_StartIndex = input.index();
        Object root_0 = null;

        Token RETURN10=null;
        wcpsParser.processingExpr_return e1 = null;


        Object RETURN10_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 4) ) { return retval; }
            // wcps.g:52:2: ( RETURN e1= processingExpr )
            // wcps.g:52:4: RETURN e1= processingExpr
            {
            root_0 = (Object)adaptor.nil();

            RETURN10=(Token)match(input,RETURN,FOLLOW_RETURN_in_returnClause176); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RETURN10_tree = (Object)adaptor.create(RETURN10);
            adaptor.addChild(root_0, RETURN10_tree);
            }
            pushFollow(FOLLOW_processingExpr_in_returnClause180);
            e1=processingExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new ReturnClause((e1!=null?e1.value:null));  
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 4, returnClause_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "returnClause"

    public static class coverageList_return extends ParserRuleReturnScope {
        public CoverageList value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageList"
    // wcps.g:54:1: coverageList returns [CoverageList value] : cname= coverageName ( COMMA next= coverageName )* ;
    public final wcpsParser.coverageList_return coverageList() throws RecognitionException {
        wcpsParser.coverageList_return retval = new wcpsParser.coverageList_return();
        retval.start = input.LT(1);
        int coverageList_StartIndex = input.index();
        Object root_0 = null;

        Token COMMA11=null;
        wcpsParser.coverageName_return cname = null;

        wcpsParser.coverageName_return next = null;


        Object COMMA11_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 5) ) { return retval; }
            // wcps.g:55:2: (cname= coverageName ( COMMA next= coverageName )* )
            // wcps.g:55:4: cname= coverageName ( COMMA next= coverageName )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_coverageName_in_coverageList197);
            cname=coverageName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, cname.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new CoverageList((cname!=null?cname.value:null)); 
            }
            // wcps.g:56:3: ( COMMA next= coverageName )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==COMMA) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // wcps.g:56:4: COMMA next= coverageName
            	    {
            	    COMMA11=(Token)match(input,COMMA,FOLLOW_COMMA_in_coverageList204); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA11_tree = (Object)adaptor.create(COMMA11);
            	    adaptor.addChild(root_0, COMMA11_tree);
            	    }
            	    pushFollow(FOLLOW_coverageName_in_coverageList208);
            	    next=coverageName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, next.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new CoverageList((next!=null?next.value:null), retval.value); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 5, coverageList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageList"

    public static class processingExpr_return extends ParserRuleReturnScope {
        public ProcessingExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "processingExpr"
    // wcps.g:58:1: processingExpr returns [ProcessingExpr value] : (e1= encodedCoverageExpr | e2= storeExpr | e3= scalarExpr );
    public final wcpsParser.processingExpr_return processingExpr() throws RecognitionException {
        wcpsParser.processingExpr_return retval = new wcpsParser.processingExpr_return();
        retval.start = input.LT(1);
        int processingExpr_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.encodedCoverageExpr_return e1 = null;

        wcpsParser.storeExpr_return e2 = null;

        wcpsParser.scalarExpr_return e3 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 6) ) { return retval; }
            // wcps.g:59:5: (e1= encodedCoverageExpr | e2= storeExpr | e3= scalarExpr )
            int alt4=3;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // wcps.g:59:7: e1= encodedCoverageExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_encodedCoverageExpr_in_processingExpr230);
                    e1=encodedCoverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new ProcessingExpr((e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:60:7: e2= storeExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_storeExpr_in_processingExpr242);
                    e2=storeExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new ProcessingExpr((e2!=null?e2.value:null)); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:61:7: e3= scalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_scalarExpr_in_processingExpr254);
                    e3=scalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new ProcessingExpr((e3!=null?e3.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 6, processingExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "processingExpr"

    public static class encodedCoverageExpr_return extends ParserRuleReturnScope {
        public EncodedCoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "encodedCoverageExpr"
    // wcps.g:63:1: encodedCoverageExpr returns [EncodedCoverageExpr value] : ENCODE LPAREN cov= coverageExpr COMMA format= stringConstant ( COMMA params= stringConstant )? RPAREN ;
    public final wcpsParser.encodedCoverageExpr_return encodedCoverageExpr() throws RecognitionException {
        wcpsParser.encodedCoverageExpr_return retval = new wcpsParser.encodedCoverageExpr_return();
        retval.start = input.LT(1);
        int encodedCoverageExpr_StartIndex = input.index();
        Object root_0 = null;

        Token ENCODE12=null;
        Token LPAREN13=null;
        Token COMMA14=null;
        Token COMMA15=null;
        Token RPAREN16=null;
        wcpsParser.coverageExpr_return cov = null;

        wcpsParser.stringConstant_return format = null;

        wcpsParser.stringConstant_return params = null;


        Object ENCODE12_tree=null;
        Object LPAREN13_tree=null;
        Object COMMA14_tree=null;
        Object COMMA15_tree=null;
        Object RPAREN16_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 7) ) { return retval; }
            // wcps.g:64:2: ( ENCODE LPAREN cov= coverageExpr COMMA format= stringConstant ( COMMA params= stringConstant )? RPAREN )
            // wcps.g:64:4: ENCODE LPAREN cov= coverageExpr COMMA format= stringConstant ( COMMA params= stringConstant )? RPAREN
            {
            root_0 = (Object)adaptor.nil();

            ENCODE12=(Token)match(input,ENCODE,FOLLOW_ENCODE_in_encodedCoverageExpr272); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ENCODE12_tree = (Object)adaptor.create(ENCODE12);
            adaptor.addChild(root_0, ENCODE12_tree);
            }
            LPAREN13=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_encodedCoverageExpr274); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN13_tree = (Object)adaptor.create(LPAREN13);
            adaptor.addChild(root_0, LPAREN13_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_encodedCoverageExpr278);
            cov=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, cov.getTree());
            COMMA14=(Token)match(input,COMMA,FOLLOW_COMMA_in_encodedCoverageExpr280); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA14_tree = (Object)adaptor.create(COMMA14);
            adaptor.addChild(root_0, COMMA14_tree);
            }
            pushFollow(FOLLOW_stringConstant_in_encodedCoverageExpr284);
            format=stringConstant();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, format.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new EncodedCoverageExpr((cov!=null?cov.value:null), (format!=null?input.toString(format.start,format.stop):null)); 
            }
            // wcps.g:65:3: ( COMMA params= stringConstant )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==COMMA) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // wcps.g:65:4: COMMA params= stringConstant
                    {
                    COMMA15=(Token)match(input,COMMA,FOLLOW_COMMA_in_encodedCoverageExpr291); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA15_tree = (Object)adaptor.create(COMMA15);
                    adaptor.addChild(root_0, COMMA15_tree);
                    }
                    pushFollow(FOLLOW_stringConstant_in_encodedCoverageExpr295);
                    params=stringConstant();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, params.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.setParams((params!=null?input.toString(params.start,params.stop):null)); 
                    }

                    }
                    break;

            }

            RPAREN16=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_encodedCoverageExpr302); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN16_tree = (Object)adaptor.create(RPAREN16);
            adaptor.addChild(root_0, RPAREN16_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 7, encodedCoverageExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "encodedCoverageExpr"

    public static class storeExpr_return extends ParserRuleReturnScope {
        public StoreExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "storeExpr"
    // wcps.g:67:1: storeExpr returns [StoreExpr value] : STORE LPAREN e1= encodedCoverageExpr RPAREN ;
    public final wcpsParser.storeExpr_return storeExpr() throws RecognitionException {
        wcpsParser.storeExpr_return retval = new wcpsParser.storeExpr_return();
        retval.start = input.LT(1);
        int storeExpr_StartIndex = input.index();
        Object root_0 = null;

        Token STORE17=null;
        Token LPAREN18=null;
        Token RPAREN19=null;
        wcpsParser.encodedCoverageExpr_return e1 = null;


        Object STORE17_tree=null;
        Object LPAREN18_tree=null;
        Object RPAREN19_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 8) ) { return retval; }
            // wcps.g:68:5: ( STORE LPAREN e1= encodedCoverageExpr RPAREN )
            // wcps.g:68:7: STORE LPAREN e1= encodedCoverageExpr RPAREN
            {
            root_0 = (Object)adaptor.nil();

            STORE17=(Token)match(input,STORE,FOLLOW_STORE_in_storeExpr319); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            STORE17_tree = (Object)adaptor.create(STORE17);
            adaptor.addChild(root_0, STORE17_tree);
            }
            LPAREN18=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_storeExpr321); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN18_tree = (Object)adaptor.create(LPAREN18);
            adaptor.addChild(root_0, LPAREN18_tree);
            }
            pushFollow(FOLLOW_encodedCoverageExpr_in_storeExpr325);
            e1=encodedCoverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            RPAREN19=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_storeExpr327); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN19_tree = (Object)adaptor.create(RPAREN19);
            adaptor.addChild(root_0, RPAREN19_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new StoreExpr((e1!=null?e1.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 8, storeExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "storeExpr"

    public static class coverageExpr_return extends ParserRuleReturnScope {
        public CoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageExpr"
    // wcps.g:70:1: coverageExpr returns [CoverageExpr value] : e1= coverageLogicTerm (op= ( OR | XOR ) e2= coverageLogicTerm )* ;
    public final wcpsParser.coverageExpr_return coverageExpr() throws RecognitionException {
        wcpsParser.coverageExpr_return retval = new wcpsParser.coverageExpr_return();
        retval.start = input.LT(1);
        int coverageExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.coverageLogicTerm_return e1 = null;

        wcpsParser.coverageLogicTerm_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 9) ) { return retval; }
            // wcps.g:71:5: (e1= coverageLogicTerm (op= ( OR | XOR ) e2= coverageLogicTerm )* )
            // wcps.g:71:7: e1= coverageLogicTerm (op= ( OR | XOR ) e2= coverageLogicTerm )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_coverageLogicTerm_in_coverageExpr350);
            e1=coverageLogicTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:72:9: (op= ( OR | XOR ) e2= coverageLogicTerm )*
            loop6:
            do {
                int alt6=2;
                alt6 = dfa6.predict(input);
                switch (alt6) {
            	case 1 :
            	    // wcps.g:72:10: op= ( OR | XOR ) e2= coverageLogicTerm
            	    {
            	    op=(Token)input.LT(1);
            	    if ( (input.LA(1)>=OR && input.LA(1)<=XOR) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_coverageLogicTerm_in_coverageExpr374);
            	    e2=coverageLogicTerm();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new CoverageExpr((op!=null?op.getText():null), retval.value, (e2!=null?e2.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 9, coverageExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageExpr"

    public static class coverageLogicTerm_return extends ParserRuleReturnScope {
        public CoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageLogicTerm"
    // wcps.g:74:1: coverageLogicTerm returns [CoverageExpr value] : e1= coverageLogicFactor (op= AND e2= coverageLogicFactor )* ;
    public final wcpsParser.coverageLogicTerm_return coverageLogicTerm() throws RecognitionException {
        wcpsParser.coverageLogicTerm_return retval = new wcpsParser.coverageLogicTerm_return();
        retval.start = input.LT(1);
        int coverageLogicTerm_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.coverageLogicFactor_return e1 = null;

        wcpsParser.coverageLogicFactor_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 10) ) { return retval; }
            // wcps.g:75:5: (e1= coverageLogicFactor (op= AND e2= coverageLogicFactor )* )
            // wcps.g:75:7: e1= coverageLogicFactor (op= AND e2= coverageLogicFactor )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_coverageLogicFactor_in_coverageLogicTerm400);
            e1=coverageLogicFactor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:76:9: (op= AND e2= coverageLogicFactor )*
            loop7:
            do {
                int alt7=2;
                alt7 = dfa7.predict(input);
                switch (alt7) {
            	case 1 :
            	    // wcps.g:76:10: op= AND e2= coverageLogicFactor
            	    {
            	    op=(Token)match(input,AND,FOLLOW_AND_in_coverageLogicTerm416); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    op_tree = (Object)adaptor.create(op);
            	    adaptor.addChild(root_0, op_tree);
            	    }
            	    pushFollow(FOLLOW_coverageLogicFactor_in_coverageLogicTerm420);
            	    e2=coverageLogicFactor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new CoverageExpr((op!=null?op.getText():null), retval.value, (e2!=null?e2.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 10, coverageLogicTerm_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageLogicTerm"

    public static class coverageLogicFactor_return extends ParserRuleReturnScope {
        public CoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageLogicFactor"
    // wcps.g:78:1: coverageLogicFactor returns [CoverageExpr value] : e1= coverageArithmeticExpr (op= ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE ) e2= coverageArithmeticExpr )? ;
    public final wcpsParser.coverageLogicFactor_return coverageLogicFactor() throws RecognitionException {
        wcpsParser.coverageLogicFactor_return retval = new wcpsParser.coverageLogicFactor_return();
        retval.start = input.LT(1);
        int coverageLogicFactor_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.coverageArithmeticExpr_return e1 = null;

        wcpsParser.coverageArithmeticExpr_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 11) ) { return retval; }
            // wcps.g:79:5: (e1= coverageArithmeticExpr (op= ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE ) e2= coverageArithmeticExpr )? )
            // wcps.g:79:7: e1= coverageArithmeticExpr (op= ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE ) e2= coverageArithmeticExpr )?
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_coverageArithmeticExpr_in_coverageLogicFactor447);
            e1=coverageArithmeticExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
              retval.value = (e1!=null?e1.value:null);
            }
            // wcps.g:80:9: (op= ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE ) e2= coverageArithmeticExpr )?
            int alt8=2;
            alt8 = dfa8.predict(input);
            switch (alt8) {
                case 1 :
                    // wcps.g:80:10: op= ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE ) e2= coverageArithmeticExpr
                    {
                    op=(Token)input.LT(1);
                    if ( (input.LA(1)>=EQUALS && input.LA(1)<=GTE) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_coverageArithmeticExpr_in_coverageLogicFactor479);
                    e2=coverageArithmeticExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((op!=null?op.getText():null), (e1!=null?e1.value:null), (e2!=null?e2.value:null)); 
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 11, coverageLogicFactor_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageLogicFactor"

    public static class coverageArithmeticExpr_return extends ParserRuleReturnScope {
        public CoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageArithmeticExpr"
    // wcps.g:82:1: coverageArithmeticExpr returns [CoverageExpr value] : e1= coverageArithmeticTerm (op= ( PLUS | MINUS ) e2= coverageArithmeticTerm )* ;
    public final wcpsParser.coverageArithmeticExpr_return coverageArithmeticExpr() throws RecognitionException {
        wcpsParser.coverageArithmeticExpr_return retval = new wcpsParser.coverageArithmeticExpr_return();
        retval.start = input.LT(1);
        int coverageArithmeticExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.coverageArithmeticTerm_return e1 = null;

        wcpsParser.coverageArithmeticTerm_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 12) ) { return retval; }
            // wcps.g:83:5: (e1= coverageArithmeticTerm (op= ( PLUS | MINUS ) e2= coverageArithmeticTerm )* )
            // wcps.g:83:7: e1= coverageArithmeticTerm (op= ( PLUS | MINUS ) e2= coverageArithmeticTerm )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_coverageArithmeticTerm_in_coverageArithmeticExpr507);
            e1=coverageArithmeticTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
              retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:84:9: (op= ( PLUS | MINUS ) e2= coverageArithmeticTerm )*
            loop9:
            do {
                int alt9=2;
                alt9 = dfa9.predict(input);
                switch (alt9) {
            	case 1 :
            	    // wcps.g:84:10: op= ( PLUS | MINUS ) e2= coverageArithmeticTerm
            	    {
            	    op=(Token)input.LT(1);
            	    if ( (input.LA(1)>=PLUS && input.LA(1)<=MINUS) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_coverageArithmeticTerm_in_coverageArithmeticExpr531);
            	    e2=coverageArithmeticTerm();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new CoverageExpr((op!=null?op.getText():null), retval.value, (e2!=null?e2.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 12, coverageArithmeticExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageArithmeticExpr"

    public static class coverageArithmeticTerm_return extends ParserRuleReturnScope {
        public CoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageArithmeticTerm"
    // wcps.g:86:1: coverageArithmeticTerm returns [CoverageExpr value] : e1= coverageArithmeticFactor (op= ( MULT | DIVIDE ) e2= coverageArithmeticFactor )* ;
    public final wcpsParser.coverageArithmeticTerm_return coverageArithmeticTerm() throws RecognitionException {
        wcpsParser.coverageArithmeticTerm_return retval = new wcpsParser.coverageArithmeticTerm_return();
        retval.start = input.LT(1);
        int coverageArithmeticTerm_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.coverageArithmeticFactor_return e1 = null;

        wcpsParser.coverageArithmeticFactor_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 13) ) { return retval; }
            // wcps.g:87:5: (e1= coverageArithmeticFactor (op= ( MULT | DIVIDE ) e2= coverageArithmeticFactor )* )
            // wcps.g:87:9: e1= coverageArithmeticFactor (op= ( MULT | DIVIDE ) e2= coverageArithmeticFactor )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_coverageArithmeticFactor_in_coverageArithmeticTerm559);
            e1=coverageArithmeticFactor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
              retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:88:9: (op= ( MULT | DIVIDE ) e2= coverageArithmeticFactor )*
            loop10:
            do {
                int alt10=2;
                alt10 = dfa10.predict(input);
                switch (alt10) {
            	case 1 :
            	    // wcps.g:88:10: op= ( MULT | DIVIDE ) e2= coverageArithmeticFactor
            	    {
            	    op=(Token)input.LT(1);
            	    if ( (input.LA(1)>=MULT && input.LA(1)<=DIVIDE) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_coverageArithmeticFactor_in_coverageArithmeticTerm582);
            	    e2=coverageArithmeticFactor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new CoverageExpr((op!=null?op.getText():null), retval.value, (e2!=null?e2.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 13, coverageArithmeticTerm_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageArithmeticTerm"

    public static class coverageArithmeticFactor_return extends ParserRuleReturnScope {
        public CoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageArithmeticFactor"
    // wcps.g:90:1: coverageArithmeticFactor returns [CoverageExpr value] : e1= coverageValue (op= OVERLAY e2= coverageValue )* ;
    public final wcpsParser.coverageArithmeticFactor_return coverageArithmeticFactor() throws RecognitionException {
        wcpsParser.coverageArithmeticFactor_return retval = new wcpsParser.coverageArithmeticFactor_return();
        retval.start = input.LT(1);
        int coverageArithmeticFactor_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.coverageValue_return e1 = null;

        wcpsParser.coverageValue_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 14) ) { return retval; }
            // wcps.g:91:5: (e1= coverageValue (op= OVERLAY e2= coverageValue )* )
            // wcps.g:91:7: e1= coverageValue (op= OVERLAY e2= coverageValue )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_coverageValue_in_coverageArithmeticFactor610);
            e1=coverageValue();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:92:9: (op= OVERLAY e2= coverageValue )*
            loop11:
            do {
                int alt11=2;
                alt11 = dfa11.predict(input);
                switch (alt11) {
            	case 1 :
            	    // wcps.g:92:10: op= OVERLAY e2= coverageValue
            	    {
            	    op=(Token)match(input,OVERLAY,FOLLOW_OVERLAY_in_coverageArithmeticFactor625); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    op_tree = (Object)adaptor.create(op);
            	    adaptor.addChild(root_0, op_tree);
            	    }
            	    pushFollow(FOLLOW_coverageValue_in_coverageArithmeticFactor629);
            	    e2=coverageValue();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new CoverageExpr((op!=null?op.getText():null), retval.value, (e2!=null?e2.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 14, coverageArithmeticFactor_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageArithmeticFactor"

    public static class coverageValue_return extends ParserRuleReturnScope {
        public CoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageValue"
    // wcps.g:94:1: coverageValue returns [CoverageExpr value] : (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom );
    public final wcpsParser.coverageValue_return coverageValue() throws RecognitionException {
        wcpsParser.coverageValue_return retval = new wcpsParser.coverageValue_return();
        retval.start = input.LT(1);
        int coverageValue_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.subsetExpr_return e5 = null;

        wcpsParser.unaryInducedExpr_return e2 = null;

        wcpsParser.scaleExpr_return e4 = null;

        wcpsParser.crsTransformExpr_return e3 = null;

        wcpsParser.coverageAtom_return e1 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 15) ) { return retval; }
            // wcps.g:95:5: (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom )
            int alt12=5;
            alt12 = dfa12.predict(input);
            switch (alt12) {
                case 1 :
                    // wcps.g:95:7: e5= subsetExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_subsetExpr_in_coverageValue655);
                    e5=subsetExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e5.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e5!=null?e5.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:96:7: e2= unaryInducedExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_unaryInducedExpr_in_coverageValue668);
                    e2=unaryInducedExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = (e2!=null?e2.value:null); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:97:7: e4= scaleExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_scaleExpr_in_coverageValue680);
                    e4=scaleExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e4.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e4!=null?e4.value:null)); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:98:7: e3= crsTransformExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_crsTransformExpr_in_coverageValue692);
                    e3=crsTransformExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e3!=null?e3.value:null)); 
                    }

                    }
                    break;
                case 5 :
                    // wcps.g:99:7: e1= coverageAtom
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageAtom_in_coverageValue704);
                    e1=coverageAtom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = (e1!=null?e1.value:null); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 15, coverageValue_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageValue"

    public static class coverageAtom_return extends ParserRuleReturnScope {
        public CoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageAtom"
    // wcps.g:101:1: coverageAtom returns [CoverageExpr value] : (e2= scalarExpr | e1= coverageVariable | LPAREN e7= coverageExpr RPAREN | e3= coverageConstantExpr | e4= coverageConstructorExpr | e5= setMetaDataExpr | e6= rangeConstructorExpr );
    public final wcpsParser.coverageAtom_return coverageAtom() throws RecognitionException {
        wcpsParser.coverageAtom_return retval = new wcpsParser.coverageAtom_return();
        retval.start = input.LT(1);
        int coverageAtom_StartIndex = input.index();
        Object root_0 = null;

        Token LPAREN20=null;
        Token RPAREN21=null;
        wcpsParser.scalarExpr_return e2 = null;

        wcpsParser.coverageVariable_return e1 = null;

        wcpsParser.coverageExpr_return e7 = null;

        wcpsParser.coverageConstantExpr_return e3 = null;

        wcpsParser.coverageConstructorExpr_return e4 = null;

        wcpsParser.setMetaDataExpr_return e5 = null;

        wcpsParser.rangeConstructorExpr_return e6 = null;


        Object LPAREN20_tree=null;
        Object RPAREN21_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 16) ) { return retval; }
            // wcps.g:102:5: (e2= scalarExpr | e1= coverageVariable | LPAREN e7= coverageExpr RPAREN | e3= coverageConstantExpr | e4= coverageConstructorExpr | e5= setMetaDataExpr | e6= rangeConstructorExpr )
            int alt13=7;
            alt13 = dfa13.predict(input);
            switch (alt13) {
                case 1 :
                    // wcps.g:102:7: e2= scalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_scalarExpr_in_coverageAtom727);
                    e2=scalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e2!=null?e2.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:103:7: e1= coverageVariable
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageVariable_in_coverageAtom739);
                    e1=coverageVariable();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:104:7: LPAREN e7= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    LPAREN20=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_coverageAtom749); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN20_tree = (Object)adaptor.create(LPAREN20);
                    adaptor.addChild(root_0, LPAREN20_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_coverageAtom753);
                    e7=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e7.getTree());
                    RPAREN21=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_coverageAtom755); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN21_tree = (Object)adaptor.create(RPAREN21);
                    adaptor.addChild(root_0, RPAREN21_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e7!=null?e7.value:null)); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:105:7: e3= coverageConstantExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageConstantExpr_in_coverageAtom768);
                    e3=coverageConstantExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e3!=null?e3.value:null)); 
                    }

                    }
                    break;
                case 5 :
                    // wcps.g:106:7: e4= coverageConstructorExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageConstructorExpr_in_coverageAtom780);
                    e4=coverageConstructorExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e4.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e4!=null?e4.value:null)); 
                    }

                    }
                    break;
                case 6 :
                    // wcps.g:107:7: e5= setMetaDataExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_setMetaDataExpr_in_coverageAtom793);
                    e5=setMetaDataExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e5.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e5!=null?e5.value:null)); 
                    }

                    }
                    break;
                case 7 :
                    // wcps.g:108:7: e6= rangeConstructorExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_rangeConstructorExpr_in_coverageAtom806);
                    e6=rangeConstructorExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e6.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e6!=null?e6.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 16, coverageAtom_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageAtom"

    public static class scalarExpr_return extends ParserRuleReturnScope {
        public ScalarExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "scalarExpr"
    // wcps.g:110:1: scalarExpr returns [ScalarExpr value] : (e1= metaDataExpr | e2= condenseExpr | e3= booleanScalarExpr | e4= numericScalarExpr | e5= stringScalarExpr | LPAREN e6= scalarExpr RPAREN );
    public final wcpsParser.scalarExpr_return scalarExpr() throws RecognitionException {
        wcpsParser.scalarExpr_return retval = new wcpsParser.scalarExpr_return();
        retval.start = input.LT(1);
        int scalarExpr_StartIndex = input.index();
        Object root_0 = null;

        Token LPAREN22=null;
        Token RPAREN23=null;
        wcpsParser.metaDataExpr_return e1 = null;

        wcpsParser.condenseExpr_return e2 = null;

        wcpsParser.booleanScalarExpr_return e3 = null;

        wcpsParser.numericScalarExpr_return e4 = null;

        wcpsParser.stringScalarExpr_return e5 = null;

        wcpsParser.scalarExpr_return e6 = null;


        Object LPAREN22_tree=null;
        Object RPAREN23_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 17) ) { return retval; }
            // wcps.g:111:5: (e1= metaDataExpr | e2= condenseExpr | e3= booleanScalarExpr | e4= numericScalarExpr | e5= stringScalarExpr | LPAREN e6= scalarExpr RPAREN )
            int alt14=6;
            alt14 = dfa14.predict(input);
            switch (alt14) {
                case 1 :
                    // wcps.g:111:7: e1= metaDataExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_metaDataExpr_in_scalarExpr830);
                    e1=metaDataExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                        retval.value = new ScalarExpr((e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:112:7: e2= condenseExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_condenseExpr_in_scalarExpr843);
                    e2=condenseExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new ScalarExpr((e2!=null?e2.value:null)); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:113:7: e3= booleanScalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_booleanScalarExpr_in_scalarExpr856);
                    e3=booleanScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    if ( state.backtracking==0 ) {
                        retval.value = new ScalarExpr((e3!=null?e3.value:null)); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:114:7: e4= numericScalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_numericScalarExpr_in_scalarExpr870);
                    e4=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e4.getTree());
                    if ( state.backtracking==0 ) {
                        retval.value = new ScalarExpr((e4!=null?e4.value:null)); 
                    }

                    }
                    break;
                case 5 :
                    // wcps.g:115:7: e5= stringScalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_stringScalarExpr_in_scalarExpr883);
                    e5=stringScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e5.getTree());
                    if ( state.backtracking==0 ) {
                        retval.value = new ScalarExpr((e5!=null?e5.value:null)); 
                    }

                    }
                    break;
                case 6 :
                    // wcps.g:116:7: LPAREN e6= scalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    LPAREN22=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_scalarExpr894); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN22_tree = (Object)adaptor.create(LPAREN22);
                    adaptor.addChild(root_0, LPAREN22_tree);
                    }
                    pushFollow(FOLLOW_scalarExpr_in_scalarExpr898);
                    e6=scalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e6.getTree());
                    RPAREN23=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_scalarExpr900); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN23_tree = (Object)adaptor.create(RPAREN23);
                    adaptor.addChild(root_0, RPAREN23_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = (e6!=null?e6.value:null); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 17, scalarExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "scalarExpr"

    public static class metaDataExpr_return extends ParserRuleReturnScope {
        public MetaDataExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "metaDataExpr"
    // wcps.g:118:1: metaDataExpr returns [MetaDataExpr value] : (op= IDENTIFIER LPAREN e1= coverageExpr RPAREN | op= IMAGECRS LPAREN e1= coverageExpr RPAREN | op= IMAGECRSDOMAIN LPAREN e1= coverageExpr ( COMMA e2= axisName )? RPAREN | op= CRSSET LPAREN e1= coverageExpr RPAREN | de= domainExpr | op= NULLSET LPAREN e1= coverageExpr RPAREN | op= INTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN | op= INTERPOLATIONSET LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN );
    public final wcpsParser.metaDataExpr_return metaDataExpr() throws RecognitionException {
        wcpsParser.metaDataExpr_return retval = new wcpsParser.metaDataExpr_return();
        retval.start = input.LT(1);
        int metaDataExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token LPAREN24=null;
        Token RPAREN25=null;
        Token LPAREN26=null;
        Token RPAREN27=null;
        Token LPAREN28=null;
        Token COMMA29=null;
        Token RPAREN30=null;
        Token LPAREN31=null;
        Token RPAREN32=null;
        Token LPAREN33=null;
        Token RPAREN34=null;
        Token LPAREN35=null;
        Token COMMA36=null;
        Token RPAREN37=null;
        Token LPAREN38=null;
        Token COMMA39=null;
        Token RPAREN40=null;
        wcpsParser.coverageExpr_return e1 = null;

        wcpsParser.axisName_return e2 = null;

        wcpsParser.domainExpr_return de = null;

        wcpsParser.fieldName_return f1 = null;


        Object op_tree=null;
        Object LPAREN24_tree=null;
        Object RPAREN25_tree=null;
        Object LPAREN26_tree=null;
        Object RPAREN27_tree=null;
        Object LPAREN28_tree=null;
        Object COMMA29_tree=null;
        Object RPAREN30_tree=null;
        Object LPAREN31_tree=null;
        Object RPAREN32_tree=null;
        Object LPAREN33_tree=null;
        Object RPAREN34_tree=null;
        Object LPAREN35_tree=null;
        Object COMMA36_tree=null;
        Object RPAREN37_tree=null;
        Object LPAREN38_tree=null;
        Object COMMA39_tree=null;
        Object RPAREN40_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 18) ) { return retval; }
            // wcps.g:119:5: (op= IDENTIFIER LPAREN e1= coverageExpr RPAREN | op= IMAGECRS LPAREN e1= coverageExpr RPAREN | op= IMAGECRSDOMAIN LPAREN e1= coverageExpr ( COMMA e2= axisName )? RPAREN | op= CRSSET LPAREN e1= coverageExpr RPAREN | de= domainExpr | op= NULLSET LPAREN e1= coverageExpr RPAREN | op= INTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN | op= INTERPOLATIONSET LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN )
            int alt16=8;
            switch ( input.LA(1) ) {
            case IDENTIFIER:
                {
                alt16=1;
                }
                break;
            case IMAGECRS:
                {
                alt16=2;
                }
                break;
            case IMAGECRSDOMAIN:
                {
                alt16=3;
                }
                break;
            case CRSSET:
                {
                alt16=4;
                }
                break;
            case DOMAIN:
                {
                alt16=5;
                }
                break;
            case NULLSET:
                {
                alt16=6;
                }
                break;
            case INTERPOLATIONDEFAULT:
                {
                alt16=7;
                }
                break;
            case INTERPOLATIONSET:
                {
                alt16=8;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }

            switch (alt16) {
                case 1 :
                    // wcps.g:119:7: op= IDENTIFIER LPAREN e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_metaDataExpr924); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN24=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr926); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN24_tree = (Object)adaptor.create(LPAREN24);
                    adaptor.addChild(root_0, LPAREN24_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr930);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN25=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr932); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN25_tree = (Object)adaptor.create(RPAREN25);
                    adaptor.addChild(root_0, RPAREN25_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new MetaDataExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:120:7: op= IMAGECRS LPAREN e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,IMAGECRS,FOLLOW_IMAGECRS_in_metaDataExpr944); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN26=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr946); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN26_tree = (Object)adaptor.create(LPAREN26);
                    adaptor.addChild(root_0, LPAREN26_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr950);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN27=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr952); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN27_tree = (Object)adaptor.create(RPAREN27);
                    adaptor.addChild(root_0, RPAREN27_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new MetaDataExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:121:7: op= IMAGECRSDOMAIN LPAREN e1= coverageExpr ( COMMA e2= axisName )? RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,IMAGECRSDOMAIN,FOLLOW_IMAGECRSDOMAIN_in_metaDataExpr964); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN28=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr966); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN28_tree = (Object)adaptor.create(LPAREN28);
                    adaptor.addChild(root_0, LPAREN28_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr970);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    // wcps.g:121:48: ( COMMA e2= axisName )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==COMMA) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // wcps.g:121:49: COMMA e2= axisName
                            {
                            COMMA29=(Token)match(input,COMMA,FOLLOW_COMMA_in_metaDataExpr973); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA29_tree = (Object)adaptor.create(COMMA29);
                            adaptor.addChild(root_0, COMMA29_tree);
                            }
                            pushFollow(FOLLOW_axisName_in_metaDataExpr977);
                            e2=axisName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());

                            }
                            break;

                    }

                    RPAREN30=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr981); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN30_tree = (Object)adaptor.create(RPAREN30);
                    adaptor.addChild(root_0, RPAREN30_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new MetaDataExpr((op!=null?op.getText():null), (e1!=null?e1.value:null), (e2!=null?e2.value:null)); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:122:7: op= CRSSET LPAREN e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,CRSSET,FOLLOW_CRSSET_in_metaDataExpr993); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN31=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr995); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN31_tree = (Object)adaptor.create(LPAREN31);
                    adaptor.addChild(root_0, LPAREN31_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr999);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN32=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr1001); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN32_tree = (Object)adaptor.create(RPAREN32);
                    adaptor.addChild(root_0, RPAREN32_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new MetaDataExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 5 :
                    // wcps.g:123:7: de= domainExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_domainExpr_in_metaDataExpr1013);
                    de=domainExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, de.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new MetaDataExpr((de!=null?de.value:null)); 
                    }

                    }
                    break;
                case 6 :
                    // wcps.g:124:7: op= NULLSET LPAREN e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,NULLSET,FOLLOW_NULLSET_in_metaDataExpr1025); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN33=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr1027); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN33_tree = (Object)adaptor.create(LPAREN33);
                    adaptor.addChild(root_0, LPAREN33_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr1031);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN34=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr1033); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN34_tree = (Object)adaptor.create(RPAREN34);
                    adaptor.addChild(root_0, RPAREN34_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new MetaDataExpr((op!=null?op.getText():null),(e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 7 :
                    // wcps.g:125:7: op= INTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,INTERPOLATIONDEFAULT,FOLLOW_INTERPOLATIONDEFAULT_in_metaDataExpr1045); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN35=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr1047); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN35_tree = (Object)adaptor.create(LPAREN35);
                    adaptor.addChild(root_0, LPAREN35_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr1051);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA36=(Token)match(input,COMMA,FOLLOW_COMMA_in_metaDataExpr1053); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA36_tree = (Object)adaptor.create(COMMA36);
                    adaptor.addChild(root_0, COMMA36_tree);
                    }
                    pushFollow(FOLLOW_fieldName_in_metaDataExpr1057);
                    f1=fieldName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, f1.getTree());
                    RPAREN37=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr1059); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN37_tree = (Object)adaptor.create(RPAREN37);
                    adaptor.addChild(root_0, RPAREN37_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new MetaDataExpr((op!=null?op.getText():null), (e1!=null?e1.value:null), (f1!=null?f1.value:null)); 
                    }

                    }
                    break;
                case 8 :
                    // wcps.g:126:7: op= INTERPOLATIONSET LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,INTERPOLATIONSET,FOLLOW_INTERPOLATIONSET_in_metaDataExpr1071); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN38=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr1073); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN38_tree = (Object)adaptor.create(LPAREN38);
                    adaptor.addChild(root_0, LPAREN38_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr1077);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA39=(Token)match(input,COMMA,FOLLOW_COMMA_in_metaDataExpr1079); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA39_tree = (Object)adaptor.create(COMMA39);
                    adaptor.addChild(root_0, COMMA39_tree);
                    }
                    pushFollow(FOLLOW_fieldName_in_metaDataExpr1083);
                    f1=fieldName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, f1.getTree());
                    RPAREN40=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr1085); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN40_tree = (Object)adaptor.create(RPAREN40);
                    adaptor.addChild(root_0, RPAREN40_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new MetaDataExpr((op!=null?op.getText():null), (e1!=null?e1.value:null), (f1!=null?f1.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 18, metaDataExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "metaDataExpr"

    public static class domainExpr_return extends ParserRuleReturnScope {
        public DomainExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "domainExpr"
    // wcps.g:128:1: domainExpr returns [DomainExpr value] : DOMAIN LPAREN var= coverageVariable COMMA axis= axisName COMMA crs= crsName RPAREN ;
    public final wcpsParser.domainExpr_return domainExpr() throws RecognitionException {
        wcpsParser.domainExpr_return retval = new wcpsParser.domainExpr_return();
        retval.start = input.LT(1);
        int domainExpr_StartIndex = input.index();
        Object root_0 = null;

        Token DOMAIN41=null;
        Token LPAREN42=null;
        Token COMMA43=null;
        Token COMMA44=null;
        Token RPAREN45=null;
        wcpsParser.coverageVariable_return var = null;

        wcpsParser.axisName_return axis = null;

        wcpsParser.crsName_return crs = null;


        Object DOMAIN41_tree=null;
        Object LPAREN42_tree=null;
        Object COMMA43_tree=null;
        Object COMMA44_tree=null;
        Object RPAREN45_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 19) ) { return retval; }
            // wcps.g:129:2: ( DOMAIN LPAREN var= coverageVariable COMMA axis= axisName COMMA crs= crsName RPAREN )
            // wcps.g:129:4: DOMAIN LPAREN var= coverageVariable COMMA axis= axisName COMMA crs= crsName RPAREN
            {
            root_0 = (Object)adaptor.nil();

            DOMAIN41=(Token)match(input,DOMAIN,FOLLOW_DOMAIN_in_domainExpr1104); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DOMAIN41_tree = (Object)adaptor.create(DOMAIN41);
            adaptor.addChild(root_0, DOMAIN41_tree);
            }
            LPAREN42=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_domainExpr1106); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN42_tree = (Object)adaptor.create(LPAREN42);
            adaptor.addChild(root_0, LPAREN42_tree);
            }
            pushFollow(FOLLOW_coverageVariable_in_domainExpr1110);
            var=coverageVariable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, var.getTree());
            COMMA43=(Token)match(input,COMMA,FOLLOW_COMMA_in_domainExpr1112); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA43_tree = (Object)adaptor.create(COMMA43);
            adaptor.addChild(root_0, COMMA43_tree);
            }
            pushFollow(FOLLOW_axisName_in_domainExpr1116);
            axis=axisName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, axis.getTree());
            COMMA44=(Token)match(input,COMMA,FOLLOW_COMMA_in_domainExpr1118); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA44_tree = (Object)adaptor.create(COMMA44);
            adaptor.addChild(root_0, COMMA44_tree);
            }
            pushFollow(FOLLOW_crsName_in_domainExpr1122);
            crs=crsName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, crs.getTree());
            RPAREN45=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_domainExpr1124); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN45_tree = (Object)adaptor.create(RPAREN45);
            adaptor.addChild(root_0, RPAREN45_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new DomainExpr((var!=null?var.value:null), (axis!=null?axis.value:null), (crs!=null?crs.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 19, domainExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "domainExpr"

    public static class condenseExpr_return extends ParserRuleReturnScope {
        public CondenseExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condenseExpr"
    // wcps.g:131:1: condenseExpr returns [CondenseExpr value] : (e1= reduceExpr | e2= generalCondenseExpr );
    public final wcpsParser.condenseExpr_return condenseExpr() throws RecognitionException {
        wcpsParser.condenseExpr_return retval = new wcpsParser.condenseExpr_return();
        retval.start = input.LT(1);
        int condenseExpr_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.reduceExpr_return e1 = null;

        wcpsParser.generalCondenseExpr_return e2 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 20) ) { return retval; }
            // wcps.g:132:2: (e1= reduceExpr | e2= generalCondenseExpr )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( ((LA17_0>=ALL && LA17_0<=MAX)) ) {
                alt17=1;
            }
            else if ( (LA17_0==CONDENSE) ) {
                alt17=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // wcps.g:132:4: e1= reduceExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_reduceExpr_in_condenseExpr1141);
                    e1=reduceExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CondenseExpr((e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:133:4: e2= generalCondenseExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_generalCondenseExpr_in_condenseExpr1150);
                    e2=generalCondenseExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CondenseExpr((e2!=null?e2.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 20, condenseExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "condenseExpr"

    public static class reduceExpr_return extends ParserRuleReturnScope {
        public ReduceExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "reduceExpr"
    // wcps.g:135:1: reduceExpr returns [ReduceExpr value] : op= ( ALL | SOME | COUNT | ADD | AVG | MIN | MAX ) LPAREN e1= coverageExpr RPAREN ;
    public final wcpsParser.reduceExpr_return reduceExpr() throws RecognitionException {
        wcpsParser.reduceExpr_return retval = new wcpsParser.reduceExpr_return();
        retval.start = input.LT(1);
        int reduceExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token LPAREN46=null;
        Token RPAREN47=null;
        wcpsParser.coverageExpr_return e1 = null;


        Object op_tree=null;
        Object LPAREN46_tree=null;
        Object RPAREN47_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 21) ) { return retval; }
            // wcps.g:136:2: (op= ( ALL | SOME | COUNT | ADD | AVG | MIN | MAX ) LPAREN e1= coverageExpr RPAREN )
            // wcps.g:136:4: op= ( ALL | SOME | COUNT | ADD | AVG | MIN | MAX ) LPAREN e1= coverageExpr RPAREN
            {
            root_0 = (Object)adaptor.nil();

            op=(Token)input.LT(1);
            if ( (input.LA(1)>=ALL && input.LA(1)<=MAX) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            LPAREN46=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_reduceExpr1183); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN46_tree = (Object)adaptor.create(LPAREN46);
            adaptor.addChild(root_0, LPAREN46_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_reduceExpr1187);
            e1=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            RPAREN47=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_reduceExpr1189); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN47_tree = (Object)adaptor.create(RPAREN47);
            adaptor.addChild(root_0, RPAREN47_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new ReduceExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 21, reduceExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "reduceExpr"

    public static class generalCondenseExpr_return extends ParserRuleReturnScope {
        public GeneralCondenseExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "generalCondenseExpr"
    // wcps.g:138:1: generalCondenseExpr returns [GeneralCondenseExpr value] : CONDENSE op= condenseOpType OVER ail= axisIteratorList ( WHERE cond= booleanScalarExpr )? USING ce= coverageExpr ;
    public final wcpsParser.generalCondenseExpr_return generalCondenseExpr() throws RecognitionException {
        wcpsParser.generalCondenseExpr_return retval = new wcpsParser.generalCondenseExpr_return();
        retval.start = input.LT(1);
        int generalCondenseExpr_StartIndex = input.index();
        Object root_0 = null;

        Token CONDENSE48=null;
        Token OVER49=null;
        Token WHERE50=null;
        Token USING51=null;
        wcpsParser.condenseOpType_return op = null;

        wcpsParser.axisIteratorList_return ail = null;

        wcpsParser.booleanScalarExpr_return cond = null;

        wcpsParser.coverageExpr_return ce = null;


        Object CONDENSE48_tree=null;
        Object OVER49_tree=null;
        Object WHERE50_tree=null;
        Object USING51_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 22) ) { return retval; }
            // wcps.g:139:2: ( CONDENSE op= condenseOpType OVER ail= axisIteratorList ( WHERE cond= booleanScalarExpr )? USING ce= coverageExpr )
            // wcps.g:139:4: CONDENSE op= condenseOpType OVER ail= axisIteratorList ( WHERE cond= booleanScalarExpr )? USING ce= coverageExpr
            {
            root_0 = (Object)adaptor.nil();

            CONDENSE48=(Token)match(input,CONDENSE,FOLLOW_CONDENSE_in_generalCondenseExpr1204); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CONDENSE48_tree = (Object)adaptor.create(CONDENSE48);
            adaptor.addChild(root_0, CONDENSE48_tree);
            }
            pushFollow(FOLLOW_condenseOpType_in_generalCondenseExpr1208);
            op=condenseOpType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, op.getTree());
            OVER49=(Token)match(input,OVER,FOLLOW_OVER_in_generalCondenseExpr1210); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            OVER49_tree = (Object)adaptor.create(OVER49);
            adaptor.addChild(root_0, OVER49_tree);
            }
            pushFollow(FOLLOW_axisIteratorList_in_generalCondenseExpr1214);
            ail=axisIteratorList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ail.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new GeneralCondenseExpr((op!=null?op.value:null), (ail!=null?ail.value:null)); 
            }
            // wcps.g:140:3: ( WHERE cond= booleanScalarExpr )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==WHERE) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // wcps.g:140:4: WHERE cond= booleanScalarExpr
                    {
                    WHERE50=(Token)match(input,WHERE,FOLLOW_WHERE_in_generalCondenseExpr1221); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    WHERE50_tree = (Object)adaptor.create(WHERE50);
                    adaptor.addChild(root_0, WHERE50_tree);
                    }
                    pushFollow(FOLLOW_booleanScalarExpr_in_generalCondenseExpr1225);
                    cond=booleanScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, cond.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.setWhere((cond!=null?cond.value:null)); 
                    }

                    }
                    break;

            }

            USING51=(Token)match(input,USING,FOLLOW_USING_in_generalCondenseExpr1233); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            USING51_tree = (Object)adaptor.create(USING51);
            adaptor.addChild(root_0, USING51_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_generalCondenseExpr1237);
            ce=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ce.getTree());
            if ( state.backtracking==0 ) {
               retval.value.setUsing((ce!=null?ce.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 22, generalCondenseExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "generalCondenseExpr"

    public static class axisIteratorList_return extends ParserRuleReturnScope {
        public AxisIteratorList value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "axisIteratorList"
    // wcps.g:143:1: axisIteratorList returns [AxisIteratorList value] : vn= variableName an= axisName LPAREN ie= intervalExpr RPAREN ( COMMA vn2= variableName an2= axisName LPAREN ie2= intervalExpr RPAREN )* ;
    public final wcpsParser.axisIteratorList_return axisIteratorList() throws RecognitionException {
        wcpsParser.axisIteratorList_return retval = new wcpsParser.axisIteratorList_return();
        retval.start = input.LT(1);
        int axisIteratorList_StartIndex = input.index();
        Object root_0 = null;

        Token LPAREN52=null;
        Token RPAREN53=null;
        Token COMMA54=null;
        Token LPAREN55=null;
        Token RPAREN56=null;
        wcpsParser.variableName_return vn = null;

        wcpsParser.axisName_return an = null;

        wcpsParser.intervalExpr_return ie = null;

        wcpsParser.variableName_return vn2 = null;

        wcpsParser.axisName_return an2 = null;

        wcpsParser.intervalExpr_return ie2 = null;


        Object LPAREN52_tree=null;
        Object RPAREN53_tree=null;
        Object COMMA54_tree=null;
        Object LPAREN55_tree=null;
        Object RPAREN56_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 23) ) { return retval; }
            // wcps.g:144:2: (vn= variableName an= axisName LPAREN ie= intervalExpr RPAREN ( COMMA vn2= variableName an2= axisName LPAREN ie2= intervalExpr RPAREN )* )
            // wcps.g:144:4: vn= variableName an= axisName LPAREN ie= intervalExpr RPAREN ( COMMA vn2= variableName an2= axisName LPAREN ie2= intervalExpr RPAREN )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_variableName_in_axisIteratorList1254);
            vn=variableName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, vn.getTree());
            pushFollow(FOLLOW_axisName_in_axisIteratorList1258);
            an=axisName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, an.getTree());
            LPAREN52=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_axisIteratorList1260); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN52_tree = (Object)adaptor.create(LPAREN52);
            adaptor.addChild(root_0, LPAREN52_tree);
            }
            pushFollow(FOLLOW_intervalExpr_in_axisIteratorList1264);
            ie=intervalExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ie.getTree());
            RPAREN53=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_axisIteratorList1266); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN53_tree = (Object)adaptor.create(RPAREN53);
            adaptor.addChild(root_0, RPAREN53_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new AxisIteratorList(new AxisIterator((vn!=null?vn.value:null), (an!=null?an.value:null), (ie!=null?ie.value:null))); 
            }
            // wcps.g:146:2: ( COMMA vn2= variableName an2= axisName LPAREN ie2= intervalExpr RPAREN )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==COMMA) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // wcps.g:146:3: COMMA vn2= variableName an2= axisName LPAREN ie2= intervalExpr RPAREN
            	    {
            	    COMMA54=(Token)match(input,COMMA,FOLLOW_COMMA_in_axisIteratorList1274); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA54_tree = (Object)adaptor.create(COMMA54);
            	    adaptor.addChild(root_0, COMMA54_tree);
            	    }
            	    pushFollow(FOLLOW_variableName_in_axisIteratorList1278);
            	    vn2=variableName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, vn2.getTree());
            	    pushFollow(FOLLOW_axisName_in_axisIteratorList1282);
            	    an2=axisName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, an2.getTree());
            	    LPAREN55=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_axisIteratorList1284); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    LPAREN55_tree = (Object)adaptor.create(LPAREN55);
            	    adaptor.addChild(root_0, LPAREN55_tree);
            	    }
            	    pushFollow(FOLLOW_intervalExpr_in_axisIteratorList1288);
            	    ie2=intervalExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, ie2.getTree());
            	    RPAREN56=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_axisIteratorList1290); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    RPAREN56_tree = (Object)adaptor.create(RPAREN56);
            	    adaptor.addChild(root_0, RPAREN56_tree);
            	    }
            	    if ( state.backtracking==0 ) {
            	       retval.value = new AxisIteratorList(new AxisIterator((vn2!=null?vn2.value:null), (an2!=null?an2.value:null), (ie2!=null?ie2.value:null)), retval.value); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 23, axisIteratorList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "axisIteratorList"

    public static class intervalExpr_return extends ParserRuleReturnScope {
        public IntervalExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "intervalExpr"
    // wcps.g:149:1: intervalExpr returns [IntervalExpr value] : (lo= indexExpr COLON hi= indexExpr | IMAGECRSDOMAIN LPAREN e1= coverageName COMMA e2= axisName RPAREN );
    public final wcpsParser.intervalExpr_return intervalExpr() throws RecognitionException {
        wcpsParser.intervalExpr_return retval = new wcpsParser.intervalExpr_return();
        retval.start = input.LT(1);
        int intervalExpr_StartIndex = input.index();
        Object root_0 = null;

        Token COLON57=null;
        Token IMAGECRSDOMAIN58=null;
        Token LPAREN59=null;
        Token COMMA60=null;
        Token RPAREN61=null;
        wcpsParser.indexExpr_return lo = null;

        wcpsParser.indexExpr_return hi = null;

        wcpsParser.coverageName_return e1 = null;

        wcpsParser.axisName_return e2 = null;


        Object COLON57_tree=null;
        Object IMAGECRSDOMAIN58_tree=null;
        Object LPAREN59_tree=null;
        Object COMMA60_tree=null;
        Object RPAREN61_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 24) ) { return retval; }
            // wcps.g:150:5: (lo= indexExpr COLON hi= indexExpr | IMAGECRSDOMAIN LPAREN e1= coverageName COMMA e2= axisName RPAREN )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==LPAREN||(LA20_0>=INTEGERCONSTANT && LA20_0<=ROUND)) ) {
                alt20=1;
            }
            else if ( (LA20_0==IMAGECRSDOMAIN) ) {
                alt20=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // wcps.g:150:7: lo= indexExpr COLON hi= indexExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_indexExpr_in_intervalExpr1314);
                    lo=indexExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, lo.getTree());
                    COLON57=(Token)match(input,COLON,FOLLOW_COLON_in_intervalExpr1316); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON57_tree = (Object)adaptor.create(COLON57);
                    adaptor.addChild(root_0, COLON57_tree);
                    }
                    pushFollow(FOLLOW_indexExpr_in_intervalExpr1320);
                    hi=indexExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, hi.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new IntervalExpr((lo!=null?lo.value:null), (hi!=null?hi.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:152:7: IMAGECRSDOMAIN LPAREN e1= coverageName COMMA e2= axisName RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    IMAGECRSDOMAIN58=(Token)match(input,IMAGECRSDOMAIN,FOLLOW_IMAGECRSDOMAIN_in_intervalExpr1335); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IMAGECRSDOMAIN58_tree = (Object)adaptor.create(IMAGECRSDOMAIN58);
                    adaptor.addChild(root_0, IMAGECRSDOMAIN58_tree);
                    }
                    LPAREN59=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_intervalExpr1337); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN59_tree = (Object)adaptor.create(LPAREN59);
                    adaptor.addChild(root_0, LPAREN59_tree);
                    }
                    pushFollow(FOLLOW_coverageName_in_intervalExpr1341);
                    e1=coverageName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA60=(Token)match(input,COMMA,FOLLOW_COMMA_in_intervalExpr1343); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA60_tree = (Object)adaptor.create(COMMA60);
                    adaptor.addChild(root_0, COMMA60_tree);
                    }
                    pushFollow(FOLLOW_axisName_in_intervalExpr1347);
                    e2=axisName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    RPAREN61=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_intervalExpr1349); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN61_tree = (Object)adaptor.create(RPAREN61);
                    adaptor.addChild(root_0, RPAREN61_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new IntervalExpr((e1!=null?e1.value:null), (e2!=null?e2.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 24, intervalExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "intervalExpr"

    public static class coverageConstantExpr_return extends ParserRuleReturnScope {
        public CoverageConstantExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageConstantExpr"
    // wcps.g:155:1: coverageConstantExpr returns [CoverageConstantExpr value] : COVERAGE aname= coverageName OVER iter= axisIteratorList VALUE LIST LT values= constantList GT ;
    public final wcpsParser.coverageConstantExpr_return coverageConstantExpr() throws RecognitionException {
        wcpsParser.coverageConstantExpr_return retval = new wcpsParser.coverageConstantExpr_return();
        retval.start = input.LT(1);
        int coverageConstantExpr_StartIndex = input.index();
        Object root_0 = null;

        Token COVERAGE62=null;
        Token OVER63=null;
        Token VALUE64=null;
        Token LIST65=null;
        Token LT66=null;
        Token GT67=null;
        wcpsParser.coverageName_return aname = null;

        wcpsParser.axisIteratorList_return iter = null;

        wcpsParser.constantList_return values = null;


        Object COVERAGE62_tree=null;
        Object OVER63_tree=null;
        Object VALUE64_tree=null;
        Object LIST65_tree=null;
        Object LT66_tree=null;
        Object GT67_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 25) ) { return retval; }
            // wcps.g:156:5: ( COVERAGE aname= coverageName OVER iter= axisIteratorList VALUE LIST LT values= constantList GT )
            // wcps.g:156:7: COVERAGE aname= coverageName OVER iter= axisIteratorList VALUE LIST LT values= constantList GT
            {
            root_0 = (Object)adaptor.nil();

            COVERAGE62=(Token)match(input,COVERAGE,FOLLOW_COVERAGE_in_coverageConstantExpr1375); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COVERAGE62_tree = (Object)adaptor.create(COVERAGE62);
            adaptor.addChild(root_0, COVERAGE62_tree);
            }
            pushFollow(FOLLOW_coverageName_in_coverageConstantExpr1379);
            aname=coverageName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
            OVER63=(Token)match(input,OVER,FOLLOW_OVER_in_coverageConstantExpr1381); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            OVER63_tree = (Object)adaptor.create(OVER63);
            adaptor.addChild(root_0, OVER63_tree);
            }
            pushFollow(FOLLOW_axisIteratorList_in_coverageConstantExpr1385);
            iter=axisIteratorList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, iter.getTree());
            VALUE64=(Token)match(input,VALUE,FOLLOW_VALUE_in_coverageConstantExpr1387); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            VALUE64_tree = (Object)adaptor.create(VALUE64);
            adaptor.addChild(root_0, VALUE64_tree);
            }
            LIST65=(Token)match(input,LIST,FOLLOW_LIST_in_coverageConstantExpr1389); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LIST65_tree = (Object)adaptor.create(LIST65);
            adaptor.addChild(root_0, LIST65_tree);
            }
            LT66=(Token)match(input,LT,FOLLOW_LT_in_coverageConstantExpr1391); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LT66_tree = (Object)adaptor.create(LT66);
            adaptor.addChild(root_0, LT66_tree);
            }
            pushFollow(FOLLOW_constantList_in_coverageConstantExpr1395);
            values=constantList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, values.getTree());
            GT67=(Token)match(input,GT,FOLLOW_GT_in_coverageConstantExpr1397); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            GT67_tree = (Object)adaptor.create(GT67);
            adaptor.addChild(root_0, GT67_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new CoverageConstantExpr((aname!=null?aname.value:null), (iter!=null?iter.value:null), (values!=null?values.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 25, coverageConstantExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageConstantExpr"

    public static class constantList_return extends ParserRuleReturnScope {
        public ConstantList value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constantList"
    // wcps.g:159:1: constantList returns [ConstantList value] : c= constant ( SEMICOLON c= constant )* ;
    public final wcpsParser.constantList_return constantList() throws RecognitionException {
        wcpsParser.constantList_return retval = new wcpsParser.constantList_return();
        retval.start = input.LT(1);
        int constantList_StartIndex = input.index();
        Object root_0 = null;

        Token SEMICOLON68=null;
        wcpsParser.constant_return c = null;


        Object SEMICOLON68_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 26) ) { return retval; }
            // wcps.g:160:5: (c= constant ( SEMICOLON c= constant )* )
            // wcps.g:160:7: c= constant ( SEMICOLON c= constant )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_constant_in_constantList1428);
            c=constant();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, c.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new ConstantList((c!=null?c.value:null)); 
            }
            // wcps.g:160:59: ( SEMICOLON c= constant )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==SEMICOLON) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // wcps.g:160:60: SEMICOLON c= constant
            	    {
            	    SEMICOLON68=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_constantList1433); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    SEMICOLON68_tree = (Object)adaptor.create(SEMICOLON68);
            	    adaptor.addChild(root_0, SEMICOLON68_tree);
            	    }
            	    pushFollow(FOLLOW_constant_in_constantList1437);
            	    c=constant();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, c.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value.add((c!=null?c.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 26, constantList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "constantList"

    public static class coverageAtomConstructor_return extends ParserRuleReturnScope {
        public CoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageAtomConstructor"
    // wcps.g:163:1: coverageAtomConstructor returns [CoverageExpr value] : (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom | e6= scalarExpr | e7= coverageVariable | e8= coverageConstantExpr | e9= coverageConstructorExpr | e10= setMetaDataExpr | e11= rangeConstructorExpr );
    public final wcpsParser.coverageAtomConstructor_return coverageAtomConstructor() throws RecognitionException {
        wcpsParser.coverageAtomConstructor_return retval = new wcpsParser.coverageAtomConstructor_return();
        retval.start = input.LT(1);
        int coverageAtomConstructor_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.subsetExpr_return e5 = null;

        wcpsParser.unaryInducedExpr_return e2 = null;

        wcpsParser.scaleExpr_return e4 = null;

        wcpsParser.crsTransformExpr_return e3 = null;

        wcpsParser.coverageAtom_return e1 = null;

        wcpsParser.scalarExpr_return e6 = null;

        wcpsParser.coverageVariable_return e7 = null;

        wcpsParser.coverageConstantExpr_return e8 = null;

        wcpsParser.coverageConstructorExpr_return e9 = null;

        wcpsParser.setMetaDataExpr_return e10 = null;

        wcpsParser.rangeConstructorExpr_return e11 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 27) ) { return retval; }
            // wcps.g:164:5: (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom | e6= scalarExpr | e7= coverageVariable | e8= coverageConstantExpr | e9= coverageConstructorExpr | e10= setMetaDataExpr | e11= rangeConstructorExpr )
            int alt22=11;
            alt22 = dfa22.predict(input);
            switch (alt22) {
                case 1 :
                    // wcps.g:164:7: e5= subsetExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_subsetExpr_in_coverageAtomConstructor1463);
                    e5=subsetExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e5.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e5!=null?e5.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:165:7: e2= unaryInducedExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_unaryInducedExpr_in_coverageAtomConstructor1476);
                    e2=unaryInducedExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = (e2!=null?e2.value:null); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:166:7: e4= scaleExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_scaleExpr_in_coverageAtomConstructor1488);
                    e4=scaleExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e4.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e4!=null?e4.value:null)); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:167:7: e3= crsTransformExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_crsTransformExpr_in_coverageAtomConstructor1500);
                    e3=crsTransformExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e3!=null?e3.value:null)); 
                    }

                    }
                    break;
                case 5 :
                    // wcps.g:168:7: e1= coverageAtom
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageAtom_in_coverageAtomConstructor1512);
                    e1=coverageAtom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = (e1!=null?e1.value:null); 
                    }

                    }
                    break;
                case 6 :
                    // wcps.g:169:7: e6= scalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_scalarExpr_in_coverageAtomConstructor1524);
                    e6=scalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e6.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e6!=null?e6.value:null)); 
                    }

                    }
                    break;
                case 7 :
                    // wcps.g:170:7: e7= coverageVariable
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageVariable_in_coverageAtomConstructor1536);
                    e7=coverageVariable();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e7.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e7!=null?e7.value:null)); 
                    }

                    }
                    break;
                case 8 :
                    // wcps.g:171:7: e8= coverageConstantExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageConstantExpr_in_coverageAtomConstructor1548);
                    e8=coverageConstantExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e8.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e8!=null?e8.value:null)); 
                    }

                    }
                    break;
                case 9 :
                    // wcps.g:172:7: e9= coverageConstructorExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageConstructorExpr_in_coverageAtomConstructor1560);
                    e9=coverageConstructorExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e9.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e9!=null?e9.value:null)); 
                    }

                    }
                    break;
                case 10 :
                    // wcps.g:173:7: e10= setMetaDataExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_setMetaDataExpr_in_coverageAtomConstructor1573);
                    e10=setMetaDataExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e10.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e10!=null?e10.value:null)); 
                    }

                    }
                    break;
                case 11 :
                    // wcps.g:174:7: e11= rangeConstructorExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_rangeConstructorExpr_in_coverageAtomConstructor1586);
                    e11=rangeConstructorExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e11.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e11!=null?e11.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 27, coverageAtomConstructor_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageAtomConstructor"

    public static class coverageConstructorExpr_return extends ParserRuleReturnScope {
        public CoverageConstructorExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageConstructorExpr"
    // wcps.g:176:1: coverageConstructorExpr returns [CoverageConstructorExpr value] : COVERAGE coverage= coverageName OVER ail= axisIteratorList VALUES se= coverageAtomConstructor ;
    public final wcpsParser.coverageConstructorExpr_return coverageConstructorExpr() throws RecognitionException {
        wcpsParser.coverageConstructorExpr_return retval = new wcpsParser.coverageConstructorExpr_return();
        retval.start = input.LT(1);
        int coverageConstructorExpr_StartIndex = input.index();
        Object root_0 = null;

        Token COVERAGE69=null;
        Token OVER70=null;
        Token VALUES71=null;
        wcpsParser.coverageName_return coverage = null;

        wcpsParser.axisIteratorList_return ail = null;

        wcpsParser.coverageAtomConstructor_return se = null;


        Object COVERAGE69_tree=null;
        Object OVER70_tree=null;
        Object VALUES71_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 28) ) { return retval; }
            // wcps.g:177:2: ( COVERAGE coverage= coverageName OVER ail= axisIteratorList VALUES se= coverageAtomConstructor )
            // wcps.g:177:4: COVERAGE coverage= coverageName OVER ail= axisIteratorList VALUES se= coverageAtomConstructor
            {
            root_0 = (Object)adaptor.nil();

            COVERAGE69=(Token)match(input,COVERAGE,FOLLOW_COVERAGE_in_coverageConstructorExpr1605); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COVERAGE69_tree = (Object)adaptor.create(COVERAGE69);
            adaptor.addChild(root_0, COVERAGE69_tree);
            }
            pushFollow(FOLLOW_coverageName_in_coverageConstructorExpr1609);
            coverage=coverageName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, coverage.getTree());
            OVER70=(Token)match(input,OVER,FOLLOW_OVER_in_coverageConstructorExpr1611); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            OVER70_tree = (Object)adaptor.create(OVER70);
            adaptor.addChild(root_0, OVER70_tree);
            }
            pushFollow(FOLLOW_axisIteratorList_in_coverageConstructorExpr1615);
            ail=axisIteratorList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ail.getTree());
            VALUES71=(Token)match(input,VALUES,FOLLOW_VALUES_in_coverageConstructorExpr1617); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            VALUES71_tree = (Object)adaptor.create(VALUES71);
            adaptor.addChild(root_0, VALUES71_tree);
            }
            pushFollow(FOLLOW_coverageAtomConstructor_in_coverageConstructorExpr1621);
            se=coverageAtomConstructor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, se.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new CoverageConstructorExpr((coverage!=null?coverage.value:null), (ail!=null?ail.value:null), (se!=null?se.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 28, coverageConstructorExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageConstructorExpr"

    public static class setMetaDataExpr_return extends ParserRuleReturnScope {
        public SetMetaDataExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "setMetaDataExpr"
    // wcps.g:180:1: setMetaDataExpr returns [SetMetaDataExpr value] : (op= SETIDENTIFIER LPAREN s= stringConstant COMMA e1= coverageExpr RPAREN | op= SETCRSSET LPAREN e1= coverageExpr COMMA crs= crsList RPAREN | op= SETNULLSET LPAREN e1= coverageExpr COMMA rel= rangeExprList RPAREN | op= SETINTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA fn= fieldName COMMA im= interpolationMethod RPAREN | op= SETINTERPOLATIONSET LPAREN e1= coverageExpr COMMA fn= fieldName COMMA iml= interpolationMethodList RPAREN );
    public final wcpsParser.setMetaDataExpr_return setMetaDataExpr() throws RecognitionException {
        wcpsParser.setMetaDataExpr_return retval = new wcpsParser.setMetaDataExpr_return();
        retval.start = input.LT(1);
        int setMetaDataExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token LPAREN72=null;
        Token COMMA73=null;
        Token RPAREN74=null;
        Token LPAREN75=null;
        Token COMMA76=null;
        Token RPAREN77=null;
        Token LPAREN78=null;
        Token COMMA79=null;
        Token RPAREN80=null;
        Token LPAREN81=null;
        Token COMMA82=null;
        Token COMMA83=null;
        Token RPAREN84=null;
        Token LPAREN85=null;
        Token COMMA86=null;
        Token COMMA87=null;
        Token RPAREN88=null;
        wcpsParser.stringConstant_return s = null;

        wcpsParser.coverageExpr_return e1 = null;

        wcpsParser.crsList_return crs = null;

        wcpsParser.rangeExprList_return rel = null;

        wcpsParser.fieldName_return fn = null;

        wcpsParser.interpolationMethod_return im = null;

        wcpsParser.interpolationMethodList_return iml = null;


        Object op_tree=null;
        Object LPAREN72_tree=null;
        Object COMMA73_tree=null;
        Object RPAREN74_tree=null;
        Object LPAREN75_tree=null;
        Object COMMA76_tree=null;
        Object RPAREN77_tree=null;
        Object LPAREN78_tree=null;
        Object COMMA79_tree=null;
        Object RPAREN80_tree=null;
        Object LPAREN81_tree=null;
        Object COMMA82_tree=null;
        Object COMMA83_tree=null;
        Object RPAREN84_tree=null;
        Object LPAREN85_tree=null;
        Object COMMA86_tree=null;
        Object COMMA87_tree=null;
        Object RPAREN88_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 29) ) { return retval; }
            // wcps.g:181:5: (op= SETIDENTIFIER LPAREN s= stringConstant COMMA e1= coverageExpr RPAREN | op= SETCRSSET LPAREN e1= coverageExpr COMMA crs= crsList RPAREN | op= SETNULLSET LPAREN e1= coverageExpr COMMA rel= rangeExprList RPAREN | op= SETINTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA fn= fieldName COMMA im= interpolationMethod RPAREN | op= SETINTERPOLATIONSET LPAREN e1= coverageExpr COMMA fn= fieldName COMMA iml= interpolationMethodList RPAREN )
            int alt23=5;
            switch ( input.LA(1) ) {
            case SETIDENTIFIER:
                {
                alt23=1;
                }
                break;
            case SETCRSSET:
                {
                alt23=2;
                }
                break;
            case SETNULLSET:
                {
                alt23=3;
                }
                break;
            case SETINTERPOLATIONDEFAULT:
                {
                alt23=4;
                }
                break;
            case SETINTERPOLATIONSET:
                {
                alt23=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }

            switch (alt23) {
                case 1 :
                    // wcps.g:181:7: op= SETIDENTIFIER LPAREN s= stringConstant COMMA e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SETIDENTIFIER,FOLLOW_SETIDENTIFIER_in_setMetaDataExpr1643); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN72=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_setMetaDataExpr1645); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN72_tree = (Object)adaptor.create(LPAREN72);
                    adaptor.addChild(root_0, LPAREN72_tree);
                    }
                    pushFollow(FOLLOW_stringConstant_in_setMetaDataExpr1649);
                    s=stringConstant();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, s.getTree());
                    COMMA73=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1651); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA73_tree = (Object)adaptor.create(COMMA73);
                    adaptor.addChild(root_0, COMMA73_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_setMetaDataExpr1655);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN74=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_setMetaDataExpr1657); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN74_tree = (Object)adaptor.create(RPAREN74);
                    adaptor.addChild(root_0, RPAREN74_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new SetMetaDataExpr((op!=null?op.getText():null), (e1!=null?e1.value:null), (s!=null?s.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:183:7: op= SETCRSSET LPAREN e1= coverageExpr COMMA crs= crsList RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SETCRSSET,FOLLOW_SETCRSSET_in_setMetaDataExpr1670); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN75=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_setMetaDataExpr1672); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN75_tree = (Object)adaptor.create(LPAREN75);
                    adaptor.addChild(root_0, LPAREN75_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_setMetaDataExpr1676);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA76=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1678); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA76_tree = (Object)adaptor.create(COMMA76);
                    adaptor.addChild(root_0, COMMA76_tree);
                    }
                    pushFollow(FOLLOW_crsList_in_setMetaDataExpr1682);
                    crs=crsList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, crs.getTree());
                    RPAREN77=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_setMetaDataExpr1684); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN77_tree = (Object)adaptor.create(RPAREN77);
                    adaptor.addChild(root_0, RPAREN77_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new SetMetaDataExpr((op!=null?op.getText():null), (e1!=null?e1.value:null), (crs!=null?crs.value:null)); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:185:7: op= SETNULLSET LPAREN e1= coverageExpr COMMA rel= rangeExprList RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SETNULLSET,FOLLOW_SETNULLSET_in_setMetaDataExpr1701); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN78=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_setMetaDataExpr1703); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN78_tree = (Object)adaptor.create(LPAREN78);
                    adaptor.addChild(root_0, LPAREN78_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_setMetaDataExpr1707);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA79=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1709); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA79_tree = (Object)adaptor.create(COMMA79);
                    adaptor.addChild(root_0, COMMA79_tree);
                    }
                    pushFollow(FOLLOW_rangeExprList_in_setMetaDataExpr1713);
                    rel=rangeExprList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rel.getTree());
                    RPAREN80=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_setMetaDataExpr1715); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN80_tree = (Object)adaptor.create(RPAREN80);
                    adaptor.addChild(root_0, RPAREN80_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new SetMetaDataExpr((op!=null?op.getText():null), (e1!=null?e1.value:null), (rel!=null?rel.value:null)); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:187:7: op= SETINTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA fn= fieldName COMMA im= interpolationMethod RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SETINTERPOLATIONDEFAULT,FOLLOW_SETINTERPOLATIONDEFAULT_in_setMetaDataExpr1732); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN81=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_setMetaDataExpr1734); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN81_tree = (Object)adaptor.create(LPAREN81);
                    adaptor.addChild(root_0, LPAREN81_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_setMetaDataExpr1738);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA82=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1740); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA82_tree = (Object)adaptor.create(COMMA82);
                    adaptor.addChild(root_0, COMMA82_tree);
                    }
                    pushFollow(FOLLOW_fieldName_in_setMetaDataExpr1744);
                    fn=fieldName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, fn.getTree());
                    COMMA83=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1746); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA83_tree = (Object)adaptor.create(COMMA83);
                    adaptor.addChild(root_0, COMMA83_tree);
                    }
                    pushFollow(FOLLOW_interpolationMethod_in_setMetaDataExpr1750);
                    im=interpolationMethod();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, im.getTree());
                    RPAREN84=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_setMetaDataExpr1752); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN84_tree = (Object)adaptor.create(RPAREN84);
                    adaptor.addChild(root_0, RPAREN84_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new SetMetaDataExpr((op!=null?op.getText():null), (e1!=null?e1.value:null), (im!=null?im.value:null), (fn!=null?fn.value:null)); 
                    }

                    }
                    break;
                case 5 :
                    // wcps.g:189:7: op= SETINTERPOLATIONSET LPAREN e1= coverageExpr COMMA fn= fieldName COMMA iml= interpolationMethodList RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SETINTERPOLATIONSET,FOLLOW_SETINTERPOLATIONSET_in_setMetaDataExpr1772); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN85=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_setMetaDataExpr1774); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN85_tree = (Object)adaptor.create(LPAREN85);
                    adaptor.addChild(root_0, LPAREN85_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_setMetaDataExpr1778);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA86=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1780); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA86_tree = (Object)adaptor.create(COMMA86);
                    adaptor.addChild(root_0, COMMA86_tree);
                    }
                    pushFollow(FOLLOW_fieldName_in_setMetaDataExpr1784);
                    fn=fieldName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, fn.getTree());
                    COMMA87=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1786); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA87_tree = (Object)adaptor.create(COMMA87);
                    adaptor.addChild(root_0, COMMA87_tree);
                    }
                    pushFollow(FOLLOW_interpolationMethodList_in_setMetaDataExpr1790);
                    iml=interpolationMethodList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, iml.getTree());
                    RPAREN88=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_setMetaDataExpr1792); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN88_tree = (Object)adaptor.create(RPAREN88);
                    adaptor.addChild(root_0, RPAREN88_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new SetMetaDataExpr((op!=null?op.getText():null), (e1!=null?e1.value:null), (iml!=null?iml.value:null), (fn!=null?fn.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 29, setMetaDataExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "setMetaDataExpr"

    public static class crsList_return extends ParserRuleReturnScope {
        public CrsList value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "crsList"
    // wcps.g:192:1: crsList returns [CrsList value] : LBRACE (crs= crsName ( COMMA crs= crsName )* )? RBRACE ;
    public final wcpsParser.crsList_return crsList() throws RecognitionException {
        wcpsParser.crsList_return retval = new wcpsParser.crsList_return();
        retval.start = input.LT(1);
        int crsList_StartIndex = input.index();
        Object root_0 = null;

        Token LBRACE89=null;
        Token COMMA90=null;
        Token RBRACE91=null;
        wcpsParser.crsName_return crs = null;


        Object LBRACE89_tree=null;
        Object COMMA90_tree=null;
        Object RBRACE91_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 30) ) { return retval; }
            // wcps.g:193:5: ( LBRACE (crs= crsName ( COMMA crs= crsName )* )? RBRACE )
            // wcps.g:193:7: LBRACE (crs= crsName ( COMMA crs= crsName )* )? RBRACE
            {
            root_0 = (Object)adaptor.nil();

            LBRACE89=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_crsList1821); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE89_tree = (Object)adaptor.create(LBRACE89);
            adaptor.addChild(root_0, LBRACE89_tree);
            }
            if ( state.backtracking==0 ) {
              retval.value = new CrsList();
            }
            // wcps.g:193:40: (crs= crsName ( COMMA crs= crsName )* )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==STRING) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // wcps.g:193:41: crs= crsName ( COMMA crs= crsName )*
                    {
                    pushFollow(FOLLOW_crsName_in_crsList1828);
                    crs=crsName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, crs.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.add((crs!=null?crs.value:null)); 
                    }
                    // wcps.g:193:81: ( COMMA crs= crsName )*
                    loop24:
                    do {
                        int alt24=2;
                        int LA24_0 = input.LA(1);

                        if ( (LA24_0==COMMA) ) {
                            alt24=1;
                        }


                        switch (alt24) {
                    	case 1 :
                    	    // wcps.g:193:82: COMMA crs= crsName
                    	    {
                    	    COMMA90=(Token)match(input,COMMA,FOLLOW_COMMA_in_crsList1833); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA90_tree = (Object)adaptor.create(COMMA90);
                    	    adaptor.addChild(root_0, COMMA90_tree);
                    	    }
                    	    pushFollow(FOLLOW_crsName_in_crsList1837);
                    	    crs=crsName();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, crs.getTree());
                    	    if ( state.backtracking==0 ) {
                    	       retval.value.add((crs!=null?crs.value:null)); 
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop24;
                        }
                    } while (true);


                    }
                    break;

            }

            RBRACE91=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_crsList1846); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACE91_tree = (Object)adaptor.create(RBRACE91);
            adaptor.addChild(root_0, RBRACE91_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 30, crsList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "crsList"

    public static class rangeExprList_return extends ParserRuleReturnScope {
        public RangeExprList value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rangeExprList"
    // wcps.g:195:1: rangeExprList returns [RangeExprList value] : LBRACE (re1= rangeExpr ( COMMA re2= rangeExpr )* )? RBRACE ;
    public final wcpsParser.rangeExprList_return rangeExprList() throws RecognitionException {
        wcpsParser.rangeExprList_return retval = new wcpsParser.rangeExprList_return();
        retval.start = input.LT(1);
        int rangeExprList_StartIndex = input.index();
        Object root_0 = null;

        Token LBRACE92=null;
        Token COMMA93=null;
        Token RBRACE94=null;
        wcpsParser.rangeExpr_return re1 = null;

        wcpsParser.rangeExpr_return re2 = null;


        Object LBRACE92_tree=null;
        Object COMMA93_tree=null;
        Object RBRACE94_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 31) ) { return retval; }
            // wcps.g:196:5: ( LBRACE (re1= rangeExpr ( COMMA re2= rangeExpr )* )? RBRACE )
            // wcps.g:196:7: LBRACE (re1= rangeExpr ( COMMA re2= rangeExpr )* )? RBRACE
            {
            root_0 = (Object)adaptor.nil();

            LBRACE92=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_rangeExprList1865); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE92_tree = (Object)adaptor.create(LBRACE92);
            adaptor.addChild(root_0, LBRACE92_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new RangeExprList(); 
            }
            // wcps.g:196:48: (re1= rangeExpr ( COMMA re2= rangeExpr )* )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==STRUCT) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // wcps.g:196:49: re1= rangeExpr ( COMMA re2= rangeExpr )*
                    {
                    pushFollow(FOLLOW_rangeExpr_in_rangeExprList1872);
                    re1=rangeExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, re1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.add((re1!=null?re1.value:null)); 
                    }
                    // wcps.g:196:91: ( COMMA re2= rangeExpr )*
                    loop26:
                    do {
                        int alt26=2;
                        int LA26_0 = input.LA(1);

                        if ( (LA26_0==COMMA) ) {
                            alt26=1;
                        }


                        switch (alt26) {
                    	case 1 :
                    	    // wcps.g:196:92: COMMA re2= rangeExpr
                    	    {
                    	    COMMA93=(Token)match(input,COMMA,FOLLOW_COMMA_in_rangeExprList1877); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA93_tree = (Object)adaptor.create(COMMA93);
                    	    adaptor.addChild(root_0, COMMA93_tree);
                    	    }
                    	    pushFollow(FOLLOW_rangeExpr_in_rangeExprList1881);
                    	    re2=rangeExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, re2.getTree());
                    	    if ( state.backtracking==0 ) {
                    	       retval.value.add((re2!=null?re2.value:null)); 
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop26;
                        }
                    } while (true);


                    }
                    break;

            }

            RBRACE94=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_rangeExprList1890); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACE94_tree = (Object)adaptor.create(RBRACE94);
            adaptor.addChild(root_0, RBRACE94_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 31, rangeExprList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "rangeExprList"

    public static class interpolationMethodList_return extends ParserRuleReturnScope {
        public InterpolationMethodList value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "interpolationMethodList"
    // wcps.g:198:1: interpolationMethodList returns [InterpolationMethodList value] : LBRACE (e= interpolationMethod ( COMMA e= interpolationMethod )* )? RBRACE ;
    public final wcpsParser.interpolationMethodList_return interpolationMethodList() throws RecognitionException {
        wcpsParser.interpolationMethodList_return retval = new wcpsParser.interpolationMethodList_return();
        retval.start = input.LT(1);
        int interpolationMethodList_StartIndex = input.index();
        Object root_0 = null;

        Token LBRACE95=null;
        Token COMMA96=null;
        Token RBRACE97=null;
        wcpsParser.interpolationMethod_return e = null;


        Object LBRACE95_tree=null;
        Object COMMA96_tree=null;
        Object RBRACE97_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 32) ) { return retval; }
            // wcps.g:199:2: ( LBRACE (e= interpolationMethod ( COMMA e= interpolationMethod )* )? RBRACE )
            // wcps.g:199:4: LBRACE (e= interpolationMethod ( COMMA e= interpolationMethod )* )? RBRACE
            {
            root_0 = (Object)adaptor.nil();

            LBRACE95=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_interpolationMethodList1906); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE95_tree = (Object)adaptor.create(LBRACE95);
            adaptor.addChild(root_0, LBRACE95_tree);
            }
            if ( state.backtracking==0 ) {
              retval.value = new InterpolationMethodList();
            }
            // wcps.g:199:53: (e= interpolationMethod ( COMMA e= interpolationMethod )* )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==LPAREN) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // wcps.g:199:54: e= interpolationMethod ( COMMA e= interpolationMethod )*
                    {
                    pushFollow(FOLLOW_interpolationMethod_in_interpolationMethodList1913);
                    e=interpolationMethod();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.add((e!=null?e.value:null)); 
                    }
                    // wcps.g:199:102: ( COMMA e= interpolationMethod )*
                    loop28:
                    do {
                        int alt28=2;
                        int LA28_0 = input.LA(1);

                        if ( (LA28_0==COMMA) ) {
                            alt28=1;
                        }


                        switch (alt28) {
                    	case 1 :
                    	    // wcps.g:199:103: COMMA e= interpolationMethod
                    	    {
                    	    COMMA96=(Token)match(input,COMMA,FOLLOW_COMMA_in_interpolationMethodList1918); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA96_tree = (Object)adaptor.create(COMMA96);
                    	    adaptor.addChild(root_0, COMMA96_tree);
                    	    }
                    	    pushFollow(FOLLOW_interpolationMethod_in_interpolationMethodList1922);
                    	    e=interpolationMethod();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e.getTree());
                    	    if ( state.backtracking==0 ) {
                    	       retval.value.add((e!=null?e.value:null)); 
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop28;
                        }
                    } while (true);


                    }
                    break;

            }

            RBRACE97=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_interpolationMethodList1930); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACE97_tree = (Object)adaptor.create(RBRACE97);
            adaptor.addChild(root_0, RBRACE97_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 32, interpolationMethodList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "interpolationMethodList"

    public static class rangeExpr_return extends ParserRuleReturnScope {
        public RangeExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rangeExpr"
    // wcps.g:201:1: rangeExpr returns [RangeExpr value] : STRUCT LBRACE (field= fieldName COLON expr= scalarExpr ( COMMA field= fieldName COLON expr= scalarExpr )* )? RBRACE ;
    public final wcpsParser.rangeExpr_return rangeExpr() throws RecognitionException {
        wcpsParser.rangeExpr_return retval = new wcpsParser.rangeExpr_return();
        retval.start = input.LT(1);
        int rangeExpr_StartIndex = input.index();
        Object root_0 = null;

        Token STRUCT98=null;
        Token LBRACE99=null;
        Token COLON100=null;
        Token COMMA101=null;
        Token COLON102=null;
        Token RBRACE103=null;
        wcpsParser.fieldName_return field = null;

        wcpsParser.scalarExpr_return expr = null;


        Object STRUCT98_tree=null;
        Object LBRACE99_tree=null;
        Object COLON100_tree=null;
        Object COMMA101_tree=null;
        Object COLON102_tree=null;
        Object RBRACE103_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 33) ) { return retval; }
            // wcps.g:202:2: ( STRUCT LBRACE (field= fieldName COLON expr= scalarExpr ( COMMA field= fieldName COLON expr= scalarExpr )* )? RBRACE )
            // wcps.g:202:4: STRUCT LBRACE (field= fieldName COLON expr= scalarExpr ( COMMA field= fieldName COLON expr= scalarExpr )* )? RBRACE
            {
            root_0 = (Object)adaptor.nil();

            STRUCT98=(Token)match(input,STRUCT,FOLLOW_STRUCT_in_rangeExpr1943); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            STRUCT98_tree = (Object)adaptor.create(STRUCT98);
            adaptor.addChild(root_0, STRUCT98_tree);
            }
            LBRACE99=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_rangeExpr1945); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE99_tree = (Object)adaptor.create(LBRACE99);
            adaptor.addChild(root_0, LBRACE99_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value =new RangeExpr(); 
            }
            // wcps.g:203:2: (field= fieldName COLON expr= scalarExpr ( COMMA field= fieldName COLON expr= scalarExpr )* )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==INTEGERCONSTANT||LA31_0==STRING||LA31_0==NAME) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // wcps.g:203:3: field= fieldName COLON expr= scalarExpr ( COMMA field= fieldName COLON expr= scalarExpr )*
                    {
                    pushFollow(FOLLOW_fieldName_in_rangeExpr1953);
                    field=fieldName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, field.getTree());
                    COLON100=(Token)match(input,COLON,FOLLOW_COLON_in_rangeExpr1955); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON100_tree = (Object)adaptor.create(COLON100);
                    adaptor.addChild(root_0, COLON100_tree);
                    }
                    pushFollow(FOLLOW_scalarExpr_in_rangeExpr1959);
                    expr=scalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.add((field!=null?field.value:null), (expr!=null?expr.value:null)); 
                    }
                    // wcps.g:204:3: ( COMMA field= fieldName COLON expr= scalarExpr )*
                    loop30:
                    do {
                        int alt30=2;
                        int LA30_0 = input.LA(1);

                        if ( (LA30_0==COMMA) ) {
                            alt30=1;
                        }


                        switch (alt30) {
                    	case 1 :
                    	    // wcps.g:204:4: COMMA field= fieldName COLON expr= scalarExpr
                    	    {
                    	    COMMA101=(Token)match(input,COMMA,FOLLOW_COMMA_in_rangeExpr1966); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA101_tree = (Object)adaptor.create(COMMA101);
                    	    adaptor.addChild(root_0, COMMA101_tree);
                    	    }
                    	    pushFollow(FOLLOW_fieldName_in_rangeExpr1970);
                    	    field=fieldName();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, field.getTree());
                    	    COLON102=(Token)match(input,COLON,FOLLOW_COLON_in_rangeExpr1972); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COLON102_tree = (Object)adaptor.create(COLON102);
                    	    adaptor.addChild(root_0, COLON102_tree);
                    	    }
                    	    pushFollow(FOLLOW_scalarExpr_in_rangeExpr1976);
                    	    expr=scalarExpr();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr.getTree());
                    	    if ( state.backtracking==0 ) {
                    	       retval.value.add((field!=null?field.value:null), (expr!=null?expr.value:null)); 
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop30;
                        }
                    } while (true);


                    }
                    break;

            }

            RBRACE103=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_rangeExpr1987); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACE103_tree = (Object)adaptor.create(RBRACE103);
            adaptor.addChild(root_0, RBRACE103_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 33, rangeExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "rangeExpr"

    public static class rangeConstructorExpr_return extends ParserRuleReturnScope {
        public RangeConstructorExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rangeConstructorExpr"
    // wcps.g:207:1: rangeConstructorExpr returns [RangeConstructorExpr value] : ( STRUCT )? LBRACE field= fieldName COLON expr= coverageExpr ( SEMICOLON field= fieldName COLON expr= coverageExpr )* RBRACE ;
    public final wcpsParser.rangeConstructorExpr_return rangeConstructorExpr() throws RecognitionException {
        wcpsParser.rangeConstructorExpr_return retval = new wcpsParser.rangeConstructorExpr_return();
        retval.start = input.LT(1);
        int rangeConstructorExpr_StartIndex = input.index();
        Object root_0 = null;

        Token STRUCT104=null;
        Token LBRACE105=null;
        Token COLON106=null;
        Token SEMICOLON107=null;
        Token COLON108=null;
        Token RBRACE109=null;
        wcpsParser.fieldName_return field = null;

        wcpsParser.coverageExpr_return expr = null;


        Object STRUCT104_tree=null;
        Object LBRACE105_tree=null;
        Object COLON106_tree=null;
        Object SEMICOLON107_tree=null;
        Object COLON108_tree=null;
        Object RBRACE109_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 34) ) { return retval; }
            // wcps.g:208:5: ( ( STRUCT )? LBRACE field= fieldName COLON expr= coverageExpr ( SEMICOLON field= fieldName COLON expr= coverageExpr )* RBRACE )
            // wcps.g:208:7: ( STRUCT )? LBRACE field= fieldName COLON expr= coverageExpr ( SEMICOLON field= fieldName COLON expr= coverageExpr )* RBRACE
            {
            root_0 = (Object)adaptor.nil();

            // wcps.g:208:7: ( STRUCT )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==STRUCT) ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // wcps.g:208:8: STRUCT
                    {
                    STRUCT104=(Token)match(input,STRUCT,FOLLOW_STRUCT_in_rangeConstructorExpr2004); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    STRUCT104_tree = (Object)adaptor.create(STRUCT104);
                    adaptor.addChild(root_0, STRUCT104_tree);
                    }

                    }
                    break;

            }

            LBRACE105=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_rangeConstructorExpr2008); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE105_tree = (Object)adaptor.create(LBRACE105);
            adaptor.addChild(root_0, LBRACE105_tree);
            }
            pushFollow(FOLLOW_fieldName_in_rangeConstructorExpr2012);
            field=fieldName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, field.getTree());
            COLON106=(Token)match(input,COLON,FOLLOW_COLON_in_rangeConstructorExpr2014); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON106_tree = (Object)adaptor.create(COLON106);
            adaptor.addChild(root_0, COLON106_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_rangeConstructorExpr2018);
            expr=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, expr.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new RangeConstructorExpr((field!=null?field.value:null), (expr!=null?expr.value:null)); 
            }
            // wcps.g:209:9: ( SEMICOLON field= fieldName COLON expr= coverageExpr )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( (LA33_0==SEMICOLON) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // wcps.g:209:10: SEMICOLON field= fieldName COLON expr= coverageExpr
            	    {
            	    SEMICOLON107=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_rangeConstructorExpr2031); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    SEMICOLON107_tree = (Object)adaptor.create(SEMICOLON107);
            	    adaptor.addChild(root_0, SEMICOLON107_tree);
            	    }
            	    pushFollow(FOLLOW_fieldName_in_rangeConstructorExpr2035);
            	    field=fieldName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, field.getTree());
            	    COLON108=(Token)match(input,COLON,FOLLOW_COLON_in_rangeConstructorExpr2037); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COLON108_tree = (Object)adaptor.create(COLON108);
            	    adaptor.addChild(root_0, COLON108_tree);
            	    }
            	    pushFollow(FOLLOW_coverageExpr_in_rangeConstructorExpr2041);
            	    expr=coverageExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value.add((field!=null?field.value:null), (expr!=null?expr.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop33;
                }
            } while (true);

            RBRACE109=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_rangeConstructorExpr2047); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACE109_tree = (Object)adaptor.create(RBRACE109);
            adaptor.addChild(root_0, RBRACE109_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 34, rangeConstructorExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "rangeConstructorExpr"

    public static class crsTransformExpr_return extends ParserRuleReturnScope {
        public CrsTransformExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "crsTransformExpr"
    // wcps.g:211:1: crsTransformExpr returns [CrsTransformExpr value] : CRSTRANSFORM LPAREN e1= coverageExpr COMMA dcl= dimensionCrsList COMMA fil= fieldInterpolationList RPAREN ;
    public final wcpsParser.crsTransformExpr_return crsTransformExpr() throws RecognitionException {
        wcpsParser.crsTransformExpr_return retval = new wcpsParser.crsTransformExpr_return();
        retval.start = input.LT(1);
        int crsTransformExpr_StartIndex = input.index();
        Object root_0 = null;

        Token CRSTRANSFORM110=null;
        Token LPAREN111=null;
        Token COMMA112=null;
        Token COMMA113=null;
        Token RPAREN114=null;
        wcpsParser.coverageExpr_return e1 = null;

        wcpsParser.dimensionCrsList_return dcl = null;

        wcpsParser.fieldInterpolationList_return fil = null;


        Object CRSTRANSFORM110_tree=null;
        Object LPAREN111_tree=null;
        Object COMMA112_tree=null;
        Object COMMA113_tree=null;
        Object RPAREN114_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 35) ) { return retval; }
            // wcps.g:212:9: ( CRSTRANSFORM LPAREN e1= coverageExpr COMMA dcl= dimensionCrsList COMMA fil= fieldInterpolationList RPAREN )
            // wcps.g:212:11: CRSTRANSFORM LPAREN e1= coverageExpr COMMA dcl= dimensionCrsList COMMA fil= fieldInterpolationList RPAREN
            {
            root_0 = (Object)adaptor.nil();

            CRSTRANSFORM110=(Token)match(input,CRSTRANSFORM,FOLLOW_CRSTRANSFORM_in_crsTransformExpr2070); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CRSTRANSFORM110_tree = (Object)adaptor.create(CRSTRANSFORM110);
            adaptor.addChild(root_0, CRSTRANSFORM110_tree);
            }
            LPAREN111=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_crsTransformExpr2072); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN111_tree = (Object)adaptor.create(LPAREN111);
            adaptor.addChild(root_0, LPAREN111_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_crsTransformExpr2076);
            e1=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            COMMA112=(Token)match(input,COMMA,FOLLOW_COMMA_in_crsTransformExpr2078); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA112_tree = (Object)adaptor.create(COMMA112);
            adaptor.addChild(root_0, COMMA112_tree);
            }
            pushFollow(FOLLOW_dimensionCrsList_in_crsTransformExpr2082);
            dcl=dimensionCrsList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, dcl.getTree());
            COMMA113=(Token)match(input,COMMA,FOLLOW_COMMA_in_crsTransformExpr2084); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA113_tree = (Object)adaptor.create(COMMA113);
            adaptor.addChild(root_0, COMMA113_tree);
            }
            pushFollow(FOLLOW_fieldInterpolationList_in_crsTransformExpr2088);
            fil=fieldInterpolationList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, fil.getTree());
            RPAREN114=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_crsTransformExpr2090); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN114_tree = (Object)adaptor.create(RPAREN114);
            adaptor.addChild(root_0, RPAREN114_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new CrsTransformExpr((e1!=null?e1.value:null), (dcl!=null?dcl.value:null), (fil!=null?fil.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 35, crsTransformExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "crsTransformExpr"

    public static class dimensionCrsList_return extends ParserRuleReturnScope {
        public DimensionCrsList value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dimensionCrsList"
    // wcps.g:215:1: dimensionCrsList returns [DimensionCrsList value] : LBRACE elem= dimensionCrsElement ( COMMA elem= dimensionCrsElement )* RBRACE ;
    public final wcpsParser.dimensionCrsList_return dimensionCrsList() throws RecognitionException {
        wcpsParser.dimensionCrsList_return retval = new wcpsParser.dimensionCrsList_return();
        retval.start = input.LT(1);
        int dimensionCrsList_StartIndex = input.index();
        Object root_0 = null;

        Token LBRACE115=null;
        Token COMMA116=null;
        Token RBRACE117=null;
        wcpsParser.dimensionCrsElement_return elem = null;


        Object LBRACE115_tree=null;
        Object COMMA116_tree=null;
        Object RBRACE117_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 36) ) { return retval; }
            // wcps.g:216:5: ( LBRACE elem= dimensionCrsElement ( COMMA elem= dimensionCrsElement )* RBRACE )
            // wcps.g:216:7: LBRACE elem= dimensionCrsElement ( COMMA elem= dimensionCrsElement )* RBRACE
            {
            root_0 = (Object)adaptor.nil();

            LBRACE115=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_dimensionCrsList2131); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE115_tree = (Object)adaptor.create(LBRACE115);
            adaptor.addChild(root_0, LBRACE115_tree);
            }
            pushFollow(FOLLOW_dimensionCrsElement_in_dimensionCrsList2135);
            elem=dimensionCrsElement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, elem.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new DimensionCrsList((elem!=null?elem.value:null)); 
            }
            // wcps.g:217:9: ( COMMA elem= dimensionCrsElement )*
            loop34:
            do {
                int alt34=2;
                int LA34_0 = input.LA(1);

                if ( (LA34_0==COMMA) ) {
                    alt34=1;
                }


                switch (alt34) {
            	case 1 :
            	    // wcps.g:217:10: COMMA elem= dimensionCrsElement
            	    {
            	    COMMA116=(Token)match(input,COMMA,FOLLOW_COMMA_in_dimensionCrsList2148); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA116_tree = (Object)adaptor.create(COMMA116);
            	    adaptor.addChild(root_0, COMMA116_tree);
            	    }
            	    pushFollow(FOLLOW_dimensionCrsElement_in_dimensionCrsList2152);
            	    elem=dimensionCrsElement();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, elem.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value.add((elem!=null?elem.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop34;
                }
            } while (true);

            RBRACE117=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_dimensionCrsList2159); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACE117_tree = (Object)adaptor.create(RBRACE117);
            adaptor.addChild(root_0, RBRACE117_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 36, dimensionCrsList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "dimensionCrsList"

    public static class dimensionCrsElement_return extends ParserRuleReturnScope {
        public DimensionCrsElement value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dimensionCrsElement"
    // wcps.g:219:1: dimensionCrsElement returns [DimensionCrsElement value] : aname= axisName COLON crs= crsName ;
    public final wcpsParser.dimensionCrsElement_return dimensionCrsElement() throws RecognitionException {
        wcpsParser.dimensionCrsElement_return retval = new wcpsParser.dimensionCrsElement_return();
        retval.start = input.LT(1);
        int dimensionCrsElement_StartIndex = input.index();
        Object root_0 = null;

        Token COLON118=null;
        wcpsParser.axisName_return aname = null;

        wcpsParser.crsName_return crs = null;


        Object COLON118_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 37) ) { return retval; }
            // wcps.g:220:5: (aname= axisName COLON crs= crsName )
            // wcps.g:220:7: aname= axisName COLON crs= crsName
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_axisName_in_dimensionCrsElement2180);
            aname=axisName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new DimensionCrsElement((aname!=null?aname.value:null)); 
            }
            COLON118=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionCrsElement2184); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON118_tree = (Object)adaptor.create(COLON118);
            adaptor.addChild(root_0, COLON118_tree);
            }
            pushFollow(FOLLOW_crsName_in_dimensionCrsElement2188);
            crs=crsName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, crs.getTree());
            if ( state.backtracking==0 ) {
              retval.value.setCrs((crs!=null?crs.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 37, dimensionCrsElement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "dimensionCrsElement"

    public static class fieldInterpolationList_return extends ParserRuleReturnScope {
        public FieldInterpolationList value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fieldInterpolationList"
    // wcps.g:222:1: fieldInterpolationList returns [FieldInterpolationList value] : LBRACE (elem= fieldInterpolationElement ( COMMA elem= fieldInterpolationElement ) )* RBRACE ;
    public final wcpsParser.fieldInterpolationList_return fieldInterpolationList() throws RecognitionException {
        wcpsParser.fieldInterpolationList_return retval = new wcpsParser.fieldInterpolationList_return();
        retval.start = input.LT(1);
        int fieldInterpolationList_StartIndex = input.index();
        Object root_0 = null;

        Token LBRACE119=null;
        Token COMMA120=null;
        Token RBRACE121=null;
        wcpsParser.fieldInterpolationElement_return elem = null;


        Object LBRACE119_tree=null;
        Object COMMA120_tree=null;
        Object RBRACE121_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 38) ) { return retval; }
            // wcps.g:223:2: ( LBRACE (elem= fieldInterpolationElement ( COMMA elem= fieldInterpolationElement ) )* RBRACE )
            // wcps.g:223:4: LBRACE (elem= fieldInterpolationElement ( COMMA elem= fieldInterpolationElement ) )* RBRACE
            {
            root_0 = (Object)adaptor.nil();

            LBRACE119=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_fieldInterpolationList2206); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE119_tree = (Object)adaptor.create(LBRACE119);
            adaptor.addChild(root_0, LBRACE119_tree);
            }
            // wcps.g:223:11: (elem= fieldInterpolationElement ( COMMA elem= fieldInterpolationElement ) )*
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==INTEGERCONSTANT||LA35_0==STRING||LA35_0==NAME) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // wcps.g:223:12: elem= fieldInterpolationElement ( COMMA elem= fieldInterpolationElement )
            	    {
            	    pushFollow(FOLLOW_fieldInterpolationElement_in_fieldInterpolationList2211);
            	    elem=fieldInterpolationElement();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, elem.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new FieldInterpolationList((elem!=null?elem.value:null)); 
            	    }
            	    // wcps.g:224:3: ( COMMA elem= fieldInterpolationElement )
            	    // wcps.g:224:4: COMMA elem= fieldInterpolationElement
            	    {
            	    COMMA120=(Token)match(input,COMMA,FOLLOW_COMMA_in_fieldInterpolationList2218); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA120_tree = (Object)adaptor.create(COMMA120);
            	    adaptor.addChild(root_0, COMMA120_tree);
            	    }
            	    pushFollow(FOLLOW_fieldInterpolationElement_in_fieldInterpolationList2222);
            	    elem=fieldInterpolationElement();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, elem.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value.add((elem!=null?elem.value:null)); 
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop35;
                }
            } while (true);

            RBRACE121=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_fieldInterpolationList2230); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACE121_tree = (Object)adaptor.create(RBRACE121);
            adaptor.addChild(root_0, RBRACE121_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 38, fieldInterpolationList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "fieldInterpolationList"

    public static class fieldInterpolationElement_return extends ParserRuleReturnScope {
        public FieldInterpolationElement value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fieldInterpolationElement"
    // wcps.g:226:1: fieldInterpolationElement returns [FieldInterpolationElement value] : aname= fieldName method= interpolationMethod ;
    public final wcpsParser.fieldInterpolationElement_return fieldInterpolationElement() throws RecognitionException {
        wcpsParser.fieldInterpolationElement_return retval = new wcpsParser.fieldInterpolationElement_return();
        retval.start = input.LT(1);
        int fieldInterpolationElement_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.fieldName_return aname = null;

        wcpsParser.interpolationMethod_return method = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 39) ) { return retval; }
            // wcps.g:227:2: (aname= fieldName method= interpolationMethod )
            // wcps.g:227:4: aname= fieldName method= interpolationMethod
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_fieldName_in_fieldInterpolationElement2245);
            aname=fieldName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
            pushFollow(FOLLOW_interpolationMethod_in_fieldInterpolationElement2249);
            method=interpolationMethod();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, method.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new FieldInterpolationElement((aname!=null?aname.value:null), (method!=null?method.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 39, fieldInterpolationElement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "fieldInterpolationElement"

    public static class unaryInducedExpr_return extends ParserRuleReturnScope {
        public CoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unaryInducedExpr"
    // wcps.g:229:1: unaryInducedExpr returns [CoverageExpr value] : (e6= fieldExpr | e1= unaryArithmeticExpr | e2= exponentialExpr | e3= trigonometricExpr | e4= booleanExpr | e5= castExpr | e7= rangeConstructorExpr );
    public final wcpsParser.unaryInducedExpr_return unaryInducedExpr() throws RecognitionException {
        wcpsParser.unaryInducedExpr_return retval = new wcpsParser.unaryInducedExpr_return();
        retval.start = input.LT(1);
        int unaryInducedExpr_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.fieldExpr_return e6 = null;

        wcpsParser.unaryArithmeticExpr_return e1 = null;

        wcpsParser.exponentialExpr_return e2 = null;

        wcpsParser.trigonometricExpr_return e3 = null;

        wcpsParser.booleanExpr_return e4 = null;

        wcpsParser.castExpr_return e5 = null;

        wcpsParser.rangeConstructorExpr_return e7 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 40) ) { return retval; }
            // wcps.g:230:5: (e6= fieldExpr | e1= unaryArithmeticExpr | e2= exponentialExpr | e3= trigonometricExpr | e4= booleanExpr | e5= castExpr | e7= rangeConstructorExpr )
            int alt36=7;
            alt36 = dfa36.predict(input);
            switch (alt36) {
                case 1 :
                    // wcps.g:230:7: e6= fieldExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_fieldExpr_in_unaryInducedExpr2269);
                    e6=fieldExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e6.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e6!=null?e6.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:231:4: e1= unaryArithmeticExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_unaryArithmeticExpr_in_unaryInducedExpr2278);
                    e1=unaryArithmeticExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = (e1!=null?e1.value:null); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:232:7: e2= exponentialExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_exponentialExpr_in_unaryInducedExpr2290);
                    e2=exponentialExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e2!=null?e2.value:null)); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:233:7: e3= trigonometricExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_trigonometricExpr_in_unaryInducedExpr2302);
                    e3=trigonometricExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e3!=null?e3.value:null)); 
                    }

                    }
                    break;
                case 5 :
                    // wcps.g:234:7: e4= booleanExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_booleanExpr_in_unaryInducedExpr2314);
                    e4=booleanExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e4.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e4!=null?e4.value:null)); 
                    }

                    }
                    break;
                case 6 :
                    // wcps.g:235:7: e5= castExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_castExpr_in_unaryInducedExpr2326);
                    e5=castExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e5.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e5!=null?e5.value:null)); 
                    }

                    }
                    break;
                case 7 :
                    // wcps.g:236:7: e7= rangeConstructorExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_rangeConstructorExpr_in_unaryInducedExpr2338);
                    e7=rangeConstructorExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e7.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e7!=null?e7.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 40, unaryInducedExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "unaryInducedExpr"

    public static class unaryArithmeticExpr_return extends ParserRuleReturnScope {
        public CoverageExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "unaryArithmeticExpr"
    // wcps.g:238:1: unaryArithmeticExpr returns [CoverageExpr value] : (op= ( MINUS | PLUS ) e1= coverageAtom | op= ( SQRT | ABS | RE | IM ) LPAREN e2= coverageExpr RPAREN );
    public final wcpsParser.unaryArithmeticExpr_return unaryArithmeticExpr() throws RecognitionException {
        wcpsParser.unaryArithmeticExpr_return retval = new wcpsParser.unaryArithmeticExpr_return();
        retval.start = input.LT(1);
        int unaryArithmeticExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token LPAREN122=null;
        Token RPAREN123=null;
        wcpsParser.coverageAtom_return e1 = null;

        wcpsParser.coverageExpr_return e2 = null;


        Object op_tree=null;
        Object LPAREN122_tree=null;
        Object RPAREN123_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 41) ) { return retval; }
            // wcps.g:239:5: (op= ( MINUS | PLUS ) e1= coverageAtom | op= ( SQRT | ABS | RE | IM ) LPAREN e2= coverageExpr RPAREN )
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( ((LA37_0>=PLUS && LA37_0<=MINUS)) ) {
                alt37=1;
            }
            else if ( ((LA37_0>=SQRT && LA37_0<=IM)) ) {
                alt37=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }
            switch (alt37) {
                case 1 :
                    // wcps.g:239:7: op= ( MINUS | PLUS ) e1= coverageAtom
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)input.LT(1);
                    if ( (input.LA(1)>=PLUS && input.LA(1)<=MINUS) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    pushFollow(FOLLOW_coverageAtom_in_unaryArithmeticExpr2369);
                    e1=coverageAtom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:240:7: op= ( SQRT | ABS | RE | IM ) LPAREN e2= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)input.LT(1);
                    if ( (input.LA(1)>=SQRT && input.LA(1)<=IM) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    LPAREN122=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_unaryArithmeticExpr2391); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN122_tree = (Object)adaptor.create(LPAREN122);
                    adaptor.addChild(root_0, LPAREN122_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_unaryArithmeticExpr2395);
                    e2=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    RPAREN123=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_unaryArithmeticExpr2397); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN123_tree = (Object)adaptor.create(RPAREN123);
                    adaptor.addChild(root_0, RPAREN123_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((op!=null?op.getText():null), (e2!=null?e2.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 41, unaryArithmeticExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "unaryArithmeticExpr"

    public static class exponentialExpr_return extends ParserRuleReturnScope {
        public ExponentialExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "exponentialExpr"
    // wcps.g:242:1: exponentialExpr returns [ExponentialExpr value] : op= ( EXP | LOG | LN ) LPAREN e1= coverageExpr RPAREN ;
    public final wcpsParser.exponentialExpr_return exponentialExpr() throws RecognitionException {
        wcpsParser.exponentialExpr_return retval = new wcpsParser.exponentialExpr_return();
        retval.start = input.LT(1);
        int exponentialExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token LPAREN124=null;
        Token RPAREN125=null;
        wcpsParser.coverageExpr_return e1 = null;


        Object op_tree=null;
        Object LPAREN124_tree=null;
        Object RPAREN125_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 42) ) { return retval; }
            // wcps.g:243:5: (op= ( EXP | LOG | LN ) LPAREN e1= coverageExpr RPAREN )
            // wcps.g:243:7: op= ( EXP | LOG | LN ) LPAREN e1= coverageExpr RPAREN
            {
            root_0 = (Object)adaptor.nil();

            op=(Token)input.LT(1);
            if ( (input.LA(1)>=EXP && input.LA(1)<=LN) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            LPAREN124=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_exponentialExpr2428); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN124_tree = (Object)adaptor.create(LPAREN124);
            adaptor.addChild(root_0, LPAREN124_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_exponentialExpr2432);
            e1=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            RPAREN125=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_exponentialExpr2434); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN125_tree = (Object)adaptor.create(RPAREN125);
            adaptor.addChild(root_0, RPAREN125_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new ExponentialExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 42, exponentialExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "exponentialExpr"

    public static class trigonometricExpr_return extends ParserRuleReturnScope {
        public TrigonometricExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "trigonometricExpr"
    // wcps.g:245:1: trigonometricExpr returns [TrigonometricExpr value] : op= ( SIN | COS | TAN | SINH | COSH | TANH | ARCSIN | ARCCOS | ARCTAN ) LPAREN e1= coverageExpr RPAREN ;
    public final wcpsParser.trigonometricExpr_return trigonometricExpr() throws RecognitionException {
        wcpsParser.trigonometricExpr_return retval = new wcpsParser.trigonometricExpr_return();
        retval.start = input.LT(1);
        int trigonometricExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token LPAREN126=null;
        Token RPAREN127=null;
        wcpsParser.coverageExpr_return e1 = null;


        Object op_tree=null;
        Object LPAREN126_tree=null;
        Object RPAREN127_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 43) ) { return retval; }
            // wcps.g:246:5: (op= ( SIN | COS | TAN | SINH | COSH | TANH | ARCSIN | ARCCOS | ARCTAN ) LPAREN e1= coverageExpr RPAREN )
            // wcps.g:246:7: op= ( SIN | COS | TAN | SINH | COSH | TANH | ARCSIN | ARCCOS | ARCTAN ) LPAREN e1= coverageExpr RPAREN
            {
            root_0 = (Object)adaptor.nil();

            op=(Token)input.LT(1);
            if ( (input.LA(1)>=SIN && input.LA(1)<=ARCTAN) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            LPAREN126=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_trigonometricExpr2477); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN126_tree = (Object)adaptor.create(LPAREN126);
            adaptor.addChild(root_0, LPAREN126_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_trigonometricExpr2481);
            e1=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            RPAREN127=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_trigonometricExpr2483); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN127_tree = (Object)adaptor.create(RPAREN127);
            adaptor.addChild(root_0, RPAREN127_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new TrigonometricExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 43, trigonometricExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "trigonometricExpr"

    public static class booleanExpr_return extends ParserRuleReturnScope {
        public BooleanExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "booleanExpr"
    // wcps.g:248:1: booleanExpr returns [BooleanExpr value] : (op= NOT e1= coverageExpr | op= BIT LPAREN e1= coverageExpr COMMA e2= indexExpr RPAREN );
    public final wcpsParser.booleanExpr_return booleanExpr() throws RecognitionException {
        wcpsParser.booleanExpr_return retval = new wcpsParser.booleanExpr_return();
        retval.start = input.LT(1);
        int booleanExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token LPAREN128=null;
        Token COMMA129=null;
        Token RPAREN130=null;
        wcpsParser.coverageExpr_return e1 = null;

        wcpsParser.indexExpr_return e2 = null;


        Object op_tree=null;
        Object LPAREN128_tree=null;
        Object COMMA129_tree=null;
        Object RPAREN130_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 44) ) { return retval; }
            // wcps.g:249:5: (op= NOT e1= coverageExpr | op= BIT LPAREN e1= coverageExpr COMMA e2= indexExpr RPAREN )
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==NOT) ) {
                alt38=1;
            }
            else if ( (LA38_0==BIT) ) {
                alt38=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                throw nvae;
            }
            switch (alt38) {
                case 1 :
                    // wcps.g:249:7: op= NOT e1= coverageExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,NOT,FOLLOW_NOT_in_booleanExpr2506); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_booleanExpr2510);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new BooleanExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:250:7: op= BIT LPAREN e1= coverageExpr COMMA e2= indexExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,BIT,FOLLOW_BIT_in_booleanExpr2522); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN128=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_booleanExpr2524); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN128_tree = (Object)adaptor.create(LPAREN128);
                    adaptor.addChild(root_0, LPAREN128_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_booleanExpr2528);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA129=(Token)match(input,COMMA,FOLLOW_COMMA_in_booleanExpr2530); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA129_tree = (Object)adaptor.create(COMMA129);
                    adaptor.addChild(root_0, COMMA129_tree);
                    }
                    pushFollow(FOLLOW_indexExpr_in_booleanExpr2534);
                    e2=indexExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    RPAREN130=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_booleanExpr2536); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN130_tree = (Object)adaptor.create(RPAREN130);
                    adaptor.addChild(root_0, RPAREN130_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new BooleanExpr((op!=null?op.getText():null), (e1!=null?e1.value:null), (e2!=null?e2.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 44, booleanExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "booleanExpr"

    public static class indexExpr_return extends ParserRuleReturnScope {
        public IndexExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "indexExpr"
    // wcps.g:252:1: indexExpr returns [IndexExpr value] : e1= indexTerm (op= ( PLUS | MINUS ) e2= indexTerm )* ;
    public final wcpsParser.indexExpr_return indexExpr() throws RecognitionException {
        wcpsParser.indexExpr_return retval = new wcpsParser.indexExpr_return();
        retval.start = input.LT(1);
        int indexExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.indexTerm_return e1 = null;

        wcpsParser.indexTerm_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 45) ) { return retval; }
            // wcps.g:253:5: (e1= indexTerm (op= ( PLUS | MINUS ) e2= indexTerm )* )
            // wcps.g:253:7: e1= indexTerm (op= ( PLUS | MINUS ) e2= indexTerm )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_indexTerm_in_indexExpr2559);
            e1=indexTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:254:3: (op= ( PLUS | MINUS ) e2= indexTerm )*
            loop39:
            do {
                int alt39=2;
                int LA39_0 = input.LA(1);

                if ( ((LA39_0>=PLUS && LA39_0<=MINUS)) ) {
                    alt39=1;
                }


                switch (alt39) {
            	case 1 :
            	    // wcps.g:254:4: op= ( PLUS | MINUS ) e2= indexTerm
            	    {
            	    op=(Token)input.LT(1);
            	    if ( (input.LA(1)>=PLUS && input.LA(1)<=MINUS) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_indexTerm_in_indexExpr2577);
            	    e2=indexTerm();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new IndexExpr((op!=null?op.getText():null), retval.value, (e2!=null?e2.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop39;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 45, indexExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "indexExpr"

    public static class indexTerm_return extends ParserRuleReturnScope {
        public IndexExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "indexTerm"
    // wcps.g:256:1: indexTerm returns [IndexExpr value] : e1= indexFactor ( (op= ( MULT | DIVIDE ) e2= indexFactor ) )* ;
    public final wcpsParser.indexTerm_return indexTerm() throws RecognitionException {
        wcpsParser.indexTerm_return retval = new wcpsParser.indexTerm_return();
        retval.start = input.LT(1);
        int indexTerm_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.indexFactor_return e1 = null;

        wcpsParser.indexFactor_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 46) ) { return retval; }
            // wcps.g:257:5: (e1= indexFactor ( (op= ( MULT | DIVIDE ) e2= indexFactor ) )* )
            // wcps.g:257:7: e1= indexFactor ( (op= ( MULT | DIVIDE ) e2= indexFactor ) )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_indexFactor_in_indexTerm2602);
            e1=indexFactor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:258:6: ( (op= ( MULT | DIVIDE ) e2= indexFactor ) )*
            loop40:
            do {
                int alt40=2;
                int LA40_0 = input.LA(1);

                if ( ((LA40_0>=MULT && LA40_0<=DIVIDE)) ) {
                    alt40=1;
                }


                switch (alt40) {
            	case 1 :
            	    // wcps.g:258:7: (op= ( MULT | DIVIDE ) e2= indexFactor )
            	    {
            	    // wcps.g:258:7: (op= ( MULT | DIVIDE ) e2= indexFactor )
            	    // wcps.g:258:8: op= ( MULT | DIVIDE ) e2= indexFactor
            	    {
            	    op=(Token)input.LT(1);
            	    if ( (input.LA(1)>=MULT && input.LA(1)<=DIVIDE) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_indexFactor_in_indexTerm2623);
            	    e2=indexFactor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new IndexExpr((op!=null?op.getText():null), retval.value, (e2!=null?e2.value:null)); 
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop40;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 46, indexTerm_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "indexTerm"

    public static class indexFactor_return extends ParserRuleReturnScope {
        public IndexExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "indexFactor"
    // wcps.g:260:1: indexFactor returns [IndexExpr value] : (e= INTEGERCONSTANT | op= ROUND LPAREN e1= numericScalarExpr RPAREN | ( LPAREN e2= indexExpr RPAREN ) );
    public final wcpsParser.indexFactor_return indexFactor() throws RecognitionException {
        wcpsParser.indexFactor_return retval = new wcpsParser.indexFactor_return();
        retval.start = input.LT(1);
        int indexFactor_StartIndex = input.index();
        Object root_0 = null;

        Token e=null;
        Token op=null;
        Token LPAREN131=null;
        Token RPAREN132=null;
        Token LPAREN133=null;
        Token RPAREN134=null;
        wcpsParser.numericScalarExpr_return e1 = null;

        wcpsParser.indexExpr_return e2 = null;


        Object e_tree=null;
        Object op_tree=null;
        Object LPAREN131_tree=null;
        Object RPAREN132_tree=null;
        Object LPAREN133_tree=null;
        Object RPAREN134_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 47) ) { return retval; }
            // wcps.g:261:5: (e= INTEGERCONSTANT | op= ROUND LPAREN e1= numericScalarExpr RPAREN | ( LPAREN e2= indexExpr RPAREN ) )
            int alt41=3;
            switch ( input.LA(1) ) {
            case INTEGERCONSTANT:
                {
                alt41=1;
                }
                break;
            case ROUND:
                {
                alt41=2;
                }
                break;
            case LPAREN:
                {
                alt41=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }

            switch (alt41) {
                case 1 :
                    // wcps.g:261:7: e= INTEGERCONSTANT
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)match(input,INTEGERCONSTANT,FOLLOW_INTEGERCONSTANT_in_indexFactor2650); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    e_tree = (Object)adaptor.create(e);
                    adaptor.addChild(root_0, e_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new IndexExpr((e!=null?e.getText():null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:262:7: op= ROUND LPAREN e1= numericScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,ROUND,FOLLOW_ROUND_in_indexFactor2663); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN131=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_indexFactor2665); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN131_tree = (Object)adaptor.create(LPAREN131);
                    adaptor.addChild(root_0, LPAREN131_tree);
                    }
                    pushFollow(FOLLOW_numericScalarExpr_in_indexFactor2669);
                    e1=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN132=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_indexFactor2671); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN132_tree = (Object)adaptor.create(RPAREN132);
                    adaptor.addChild(root_0, RPAREN132_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new IndexExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:263:7: ( LPAREN e2= indexExpr RPAREN )
                    {
                    root_0 = (Object)adaptor.nil();

                    // wcps.g:263:7: ( LPAREN e2= indexExpr RPAREN )
                    // wcps.g:263:8: LPAREN e2= indexExpr RPAREN
                    {
                    LPAREN133=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_indexFactor2683); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN133_tree = (Object)adaptor.create(LPAREN133);
                    adaptor.addChild(root_0, LPAREN133_tree);
                    }
                    pushFollow(FOLLOW_indexExpr_in_indexFactor2687);
                    e2=indexExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    RPAREN134=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_indexFactor2689); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN134_tree = (Object)adaptor.create(RPAREN134);
                    adaptor.addChild(root_0, RPAREN134_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = (e2!=null?e2.value:null); 
                    }

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 47, indexFactor_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "indexFactor"

    public static class stringScalarExpr_return extends ParserRuleReturnScope {
        public StringScalarExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stringScalarExpr"
    // wcps.g:265:1: stringScalarExpr returns [StringScalarExpr value] : (op= IDENTIFIER LPAREN e1= coverageExpr RPAREN | e= STRING );
    public final wcpsParser.stringScalarExpr_return stringScalarExpr() throws RecognitionException {
        wcpsParser.stringScalarExpr_return retval = new wcpsParser.stringScalarExpr_return();
        retval.start = input.LT(1);
        int stringScalarExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token e=null;
        Token LPAREN135=null;
        Token RPAREN136=null;
        wcpsParser.coverageExpr_return e1 = null;


        Object op_tree=null;
        Object e_tree=null;
        Object LPAREN135_tree=null;
        Object RPAREN136_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 48) ) { return retval; }
            // wcps.g:267:5: (op= IDENTIFIER LPAREN e1= coverageExpr RPAREN | e= STRING )
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==IDENTIFIER) ) {
                alt42=1;
            }
            else if ( (LA42_0==STRING) ) {
                alt42=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 42, 0, input);

                throw nvae;
            }
            switch (alt42) {
                case 1 :
                    // wcps.g:267:7: op= IDENTIFIER LPAREN e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_stringScalarExpr2716); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN135=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_stringScalarExpr2718); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN135_tree = (Object)adaptor.create(LPAREN135);
                    adaptor.addChild(root_0, LPAREN135_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_stringScalarExpr2722);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN136=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_stringScalarExpr2724); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN136_tree = (Object)adaptor.create(RPAREN136);
                    adaptor.addChild(root_0, RPAREN136_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new StringScalarExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:268:7: e= STRING
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)match(input,STRING,FOLLOW_STRING_in_stringScalarExpr2737); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    e_tree = (Object)adaptor.create(e);
                    adaptor.addChild(root_0, e_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new StringScalarExpr((e!=null?e.getText():null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 48, stringScalarExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "stringScalarExpr"

    public static class scaleExpr_return extends ParserRuleReturnScope {
        public ScaleExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "scaleExpr"
    // wcps.g:270:1: scaleExpr returns [ScaleExpr value] : SCALE LPAREN e1= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE ( COMMA fil= fieldInterpolationList )? RPAREN ;
    public final wcpsParser.scaleExpr_return scaleExpr() throws RecognitionException {
        wcpsParser.scaleExpr_return retval = new wcpsParser.scaleExpr_return();
        retval.start = input.LT(1);
        int scaleExpr_StartIndex = input.index();
        Object root_0 = null;

        Token SCALE137=null;
        Token LPAREN138=null;
        Token COMMA139=null;
        Token LBRACE140=null;
        Token RBRACE141=null;
        Token COMMA142=null;
        Token RPAREN143=null;
        wcpsParser.coverageExpr_return e1 = null;

        wcpsParser.dimensionIntervalList_return dil = null;

        wcpsParser.fieldInterpolationList_return fil = null;


        Object SCALE137_tree=null;
        Object LPAREN138_tree=null;
        Object COMMA139_tree=null;
        Object LBRACE140_tree=null;
        Object RBRACE141_tree=null;
        Object COMMA142_tree=null;
        Object RPAREN143_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 49) ) { return retval; }
            // wcps.g:271:2: ( SCALE LPAREN e1= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE ( COMMA fil= fieldInterpolationList )? RPAREN )
            // wcps.g:271:4: SCALE LPAREN e1= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE ( COMMA fil= fieldInterpolationList )? RPAREN
            {
            root_0 = (Object)adaptor.nil();

            SCALE137=(Token)match(input,SCALE,FOLLOW_SCALE_in_scaleExpr2755); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SCALE137_tree = (Object)adaptor.create(SCALE137);
            adaptor.addChild(root_0, SCALE137_tree);
            }
            LPAREN138=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_scaleExpr2757); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN138_tree = (Object)adaptor.create(LPAREN138);
            adaptor.addChild(root_0, LPAREN138_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_scaleExpr2761);
            e1=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            COMMA139=(Token)match(input,COMMA,FOLLOW_COMMA_in_scaleExpr2763); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA139_tree = (Object)adaptor.create(COMMA139);
            adaptor.addChild(root_0, COMMA139_tree);
            }
            LBRACE140=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_scaleExpr2765); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE140_tree = (Object)adaptor.create(LBRACE140);
            adaptor.addChild(root_0, LBRACE140_tree);
            }
            pushFollow(FOLLOW_dimensionIntervalList_in_scaleExpr2769);
            dil=dimensionIntervalList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, dil.getTree());
            RBRACE141=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_scaleExpr2771); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACE141_tree = (Object)adaptor.create(RBRACE141);
            adaptor.addChild(root_0, RBRACE141_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new ScaleExpr((e1!=null?e1.value:null), (dil!=null?dil.value:null)); 
            }
            // wcps.g:272:13: ( COMMA fil= fieldInterpolationList )?
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==COMMA) ) {
                alt43=1;
            }
            switch (alt43) {
                case 1 :
                    // wcps.g:272:14: COMMA fil= fieldInterpolationList
                    {
                    COMMA142=(Token)match(input,COMMA,FOLLOW_COMMA_in_scaleExpr2788); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA142_tree = (Object)adaptor.create(COMMA142);
                    adaptor.addChild(root_0, COMMA142_tree);
                    }
                    pushFollow(FOLLOW_fieldInterpolationList_in_scaleExpr2792);
                    fil=fieldInterpolationList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, fil.getTree());
                    if ( state.backtracking==0 ) {
                      retval.value.addInterpolationList((fil!=null?fil.value:null)); 
                    }

                    }
                    break;

            }

            RPAREN143=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_scaleExpr2817); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN143_tree = (Object)adaptor.create(RPAREN143);
            adaptor.addChild(root_0, RPAREN143_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 49, scaleExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "scaleExpr"

    public static class subsetExpr_return extends ParserRuleReturnScope {
        public SubsetExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "subsetExpr"
    // wcps.g:275:1: subsetExpr returns [SubsetExpr value] : (e1= trimExpr | e2= sliceExpr | e3= extendExpr | e4= trimSliceExpr );
    public final wcpsParser.subsetExpr_return subsetExpr() throws RecognitionException {
        wcpsParser.subsetExpr_return retval = new wcpsParser.subsetExpr_return();
        retval.start = input.LT(1);
        int subsetExpr_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.trimExpr_return e1 = null;

        wcpsParser.sliceExpr_return e2 = null;

        wcpsParser.extendExpr_return e3 = null;

        wcpsParser.trimSliceExpr_return e4 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 50) ) { return retval; }
            // wcps.g:276:2: (e1= trimExpr | e2= sliceExpr | e3= extendExpr | e4= trimSliceExpr )
            int alt44=4;
            alt44 = dfa44.predict(input);
            switch (alt44) {
                case 1 :
                    // wcps.g:276:4: e1= trimExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_trimExpr_in_subsetExpr2834);
                    e1=trimExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new SubsetExpr((e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:277:4: e2= sliceExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_sliceExpr_in_subsetExpr2843);
                    e2=sliceExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new SubsetExpr((e2!=null?e2.value:null)); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:278:4: e3= extendExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_extendExpr_in_subsetExpr2852);
                    e3=extendExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new SubsetExpr((e3!=null?e3.value:null)); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:279:7: e4= trimSliceExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_trimSliceExpr_in_subsetExpr2864);
                    e4=trimSliceExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e4.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new SubsetExpr((e4!=null?e4.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 50, subsetExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "subsetExpr"

    public static class trimExpr_return extends ParserRuleReturnScope {
        public TrimExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "trimExpr"
    // wcps.g:281:1: trimExpr returns [TrimExpr value] : (e1= coverageAtom LBRACKET dil= dimensionIntervalList RBRACKET | TRIM LPAREN e2= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN );
    public final wcpsParser.trimExpr_return trimExpr() throws RecognitionException {
        wcpsParser.trimExpr_return retval = new wcpsParser.trimExpr_return();
        retval.start = input.LT(1);
        int trimExpr_StartIndex = input.index();
        Object root_0 = null;

        Token LBRACKET144=null;
        Token RBRACKET145=null;
        Token TRIM146=null;
        Token LPAREN147=null;
        Token COMMA148=null;
        Token LBRACE149=null;
        Token RBRACE150=null;
        Token RPAREN151=null;
        wcpsParser.coverageAtom_return e1 = null;

        wcpsParser.dimensionIntervalList_return dil = null;

        wcpsParser.coverageExpr_return e2 = null;


        Object LBRACKET144_tree=null;
        Object RBRACKET145_tree=null;
        Object TRIM146_tree=null;
        Object LPAREN147_tree=null;
        Object COMMA148_tree=null;
        Object LBRACE149_tree=null;
        Object RBRACE150_tree=null;
        Object RPAREN151_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 51) ) { return retval; }
            // wcps.g:282:2: (e1= coverageAtom LBRACKET dil= dimensionIntervalList RBRACKET | TRIM LPAREN e2= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN )
            int alt45=2;
            alt45 = dfa45.predict(input);
            switch (alt45) {
                case 1 :
                    // wcps.g:282:4: e1= coverageAtom LBRACKET dil= dimensionIntervalList RBRACKET
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageAtom_in_trimExpr2881);
                    e1=coverageAtom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    LBRACKET144=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_trimExpr2883); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LBRACKET144_tree = (Object)adaptor.create(LBRACKET144);
                    adaptor.addChild(root_0, LBRACKET144_tree);
                    }
                    pushFollow(FOLLOW_dimensionIntervalList_in_trimExpr2887);
                    dil=dimensionIntervalList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dil.getTree());
                    RBRACKET145=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_trimExpr2889); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RBRACKET145_tree = (Object)adaptor.create(RBRACKET145);
                    adaptor.addChild(root_0, RBRACKET145_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new TrimExpr((e1!=null?e1.value:null), (dil!=null?dil.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:283:6: TRIM LPAREN e2= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    TRIM146=(Token)match(input,TRIM,FOLLOW_TRIM_in_trimExpr2898); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    TRIM146_tree = (Object)adaptor.create(TRIM146);
                    adaptor.addChild(root_0, TRIM146_tree);
                    }
                    LPAREN147=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_trimExpr2900); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN147_tree = (Object)adaptor.create(LPAREN147);
                    adaptor.addChild(root_0, LPAREN147_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_trimExpr2904);
                    e2=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    COMMA148=(Token)match(input,COMMA,FOLLOW_COMMA_in_trimExpr2906); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA148_tree = (Object)adaptor.create(COMMA148);
                    adaptor.addChild(root_0, COMMA148_tree);
                    }
                    LBRACE149=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_trimExpr2908); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LBRACE149_tree = (Object)adaptor.create(LBRACE149);
                    adaptor.addChild(root_0, LBRACE149_tree);
                    }
                    pushFollow(FOLLOW_dimensionIntervalList_in_trimExpr2912);
                    dil=dimensionIntervalList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dil.getTree());
                    RBRACE150=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_trimExpr2914); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RBRACE150_tree = (Object)adaptor.create(RBRACE150);
                    adaptor.addChild(root_0, RBRACE150_tree);
                    }
                    RPAREN151=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_trimExpr2916); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN151_tree = (Object)adaptor.create(RPAREN151);
                    adaptor.addChild(root_0, RPAREN151_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new TrimExpr((e2!=null?e2.value:null), (dil!=null?dil.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 51, trimExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "trimExpr"

    public static class trimSliceExpr_return extends ParserRuleReturnScope {
        public TrimSliceExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "trimSliceExpr"
    // wcps.g:285:1: trimSliceExpr returns [TrimSliceExpr value] : e1= coverageAtom LBRACKET (el1= dimensionIntervalElement | el2= dimensionPointElement ) ( COMMA (el3= dimensionIntervalElement | el4= dimensionPointElement ) )* RBRACKET ;
    public final wcpsParser.trimSliceExpr_return trimSliceExpr() throws RecognitionException {
        wcpsParser.trimSliceExpr_return retval = new wcpsParser.trimSliceExpr_return();
        retval.start = input.LT(1);
        int trimSliceExpr_StartIndex = input.index();
        Object root_0 = null;

        Token LBRACKET152=null;
        Token COMMA153=null;
        Token RBRACKET154=null;
        wcpsParser.coverageAtom_return e1 = null;

        wcpsParser.dimensionIntervalElement_return el1 = null;

        wcpsParser.dimensionPointElement_return el2 = null;

        wcpsParser.dimensionIntervalElement_return el3 = null;

        wcpsParser.dimensionPointElement_return el4 = null;


        Object LBRACKET152_tree=null;
        Object COMMA153_tree=null;
        Object RBRACKET154_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 52) ) { return retval; }
            // wcps.g:286:2: (e1= coverageAtom LBRACKET (el1= dimensionIntervalElement | el2= dimensionPointElement ) ( COMMA (el3= dimensionIntervalElement | el4= dimensionPointElement ) )* RBRACKET )
            // wcps.g:286:4: e1= coverageAtom LBRACKET (el1= dimensionIntervalElement | el2= dimensionPointElement ) ( COMMA (el3= dimensionIntervalElement | el4= dimensionPointElement ) )* RBRACKET
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_coverageAtom_in_trimSliceExpr2933);
            e1=coverageAtom();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            LBRACKET152=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_trimSliceExpr2935); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACKET152_tree = (Object)adaptor.create(LBRACKET152);
            adaptor.addChild(root_0, LBRACKET152_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new TrimSliceExpr((e1!=null?e1.value:null)); 
            }
            // wcps.g:287:30: (el1= dimensionIntervalElement | el2= dimensionPointElement )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==INTEGERCONSTANT||LA46_0==STRING||LA46_0==NAME) ) {
                int LA46_1 = input.LA(2);

                if ( (LA46_1==COLON) ) {
                    int LA46_2 = input.LA(3);

                    if ( (synpred115_wcps()) ) {
                        alt46=1;
                    }
                    else if ( (true) ) {
                        alt46=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 46, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA46_1==LPAREN) ) {
                    int LA46_3 = input.LA(3);

                    if ( (synpred115_wcps()) ) {
                        alt46=1;
                    }
                    else if ( (true) ) {
                        alt46=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 46, 3, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 46, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // wcps.g:287:32: el1= dimensionIntervalElement
                    {
                    pushFollow(FOLLOW_dimensionIntervalElement_in_trimSliceExpr2972);
                    el1=dimensionIntervalElement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, el1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.add((el1!=null?el1.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:288:32: el2= dimensionPointElement
                    {
                    pushFollow(FOLLOW_dimensionPointElement_in_trimSliceExpr3009);
                    el2=dimensionPointElement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, el2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.add((el2!=null?el2.value:null)); 
                    }

                    }
                    break;

            }

            // wcps.g:289:30: ( COMMA (el3= dimensionIntervalElement | el4= dimensionPointElement ) )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==COMMA) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // wcps.g:289:31: COMMA (el3= dimensionIntervalElement | el4= dimensionPointElement )
            	    {
            	    COMMA153=(Token)match(input,COMMA,FOLLOW_COMMA_in_trimSliceExpr3045); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA153_tree = (Object)adaptor.create(COMMA153);
            	    adaptor.addChild(root_0, COMMA153_tree);
            	    }
            	    // wcps.g:290:30: (el3= dimensionIntervalElement | el4= dimensionPointElement )
            	    int alt47=2;
            	    int LA47_0 = input.LA(1);

            	    if ( (LA47_0==INTEGERCONSTANT||LA47_0==STRING||LA47_0==NAME) ) {
            	        int LA47_1 = input.LA(2);

            	        if ( (LA47_1==COLON) ) {
            	            int LA47_2 = input.LA(3);

            	            if ( (synpred116_wcps()) ) {
            	                alt47=1;
            	            }
            	            else if ( (true) ) {
            	                alt47=2;
            	            }
            	            else {
            	                if (state.backtracking>0) {state.failed=true; return retval;}
            	                NoViableAltException nvae =
            	                    new NoViableAltException("", 47, 2, input);

            	                throw nvae;
            	            }
            	        }
            	        else if ( (LA47_1==LPAREN) ) {
            	            int LA47_3 = input.LA(3);

            	            if ( (synpred116_wcps()) ) {
            	                alt47=1;
            	            }
            	            else if ( (true) ) {
            	                alt47=2;
            	            }
            	            else {
            	                if (state.backtracking>0) {state.failed=true; return retval;}
            	                NoViableAltException nvae =
            	                    new NoViableAltException("", 47, 3, input);

            	                throw nvae;
            	            }
            	        }
            	        else {
            	            if (state.backtracking>0) {state.failed=true; return retval;}
            	            NoViableAltException nvae =
            	                new NoViableAltException("", 47, 1, input);

            	            throw nvae;
            	        }
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 47, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt47) {
            	        case 1 :
            	            // wcps.g:290:32: el3= dimensionIntervalElement
            	            {
            	            pushFollow(FOLLOW_dimensionIntervalElement_in_trimSliceExpr3081);
            	            el3=dimensionIntervalElement();

            	            state._fsp--;
            	            if (state.failed) return retval;
            	            if ( state.backtracking==0 ) adaptor.addChild(root_0, el3.getTree());
            	            if ( state.backtracking==0 ) {
            	               retval.value.add((el3!=null?el3.value:null)); 
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // wcps.g:291:32: el4= dimensionPointElement
            	            {
            	            pushFollow(FOLLOW_dimensionPointElement_in_trimSliceExpr3118);
            	            el4=dimensionPointElement();

            	            state._fsp--;
            	            if (state.failed) return retval;
            	            if ( state.backtracking==0 ) adaptor.addChild(root_0, el4.getTree());
            	            if ( state.backtracking==0 ) {
            	               retval.value.add((el4!=null?el4.value:null)); 
            	            }

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);

            RBRACKET154=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_trimSliceExpr3149); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACKET154_tree = (Object)adaptor.create(RBRACKET154);
            adaptor.addChild(root_0, RBRACKET154_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 52, trimSliceExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "trimSliceExpr"

    public static class sliceExpr_return extends ParserRuleReturnScope {
        public SliceExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "sliceExpr"
    // wcps.g:294:1: sliceExpr returns [SliceExpr value] : (e1= coverageAtom LBRACKET dpl= dimensionPointList RBRACKET | SLICE LPAREN e2= coverageExpr COMMA LBRACE dpl= dimensionPointList RBRACE RPAREN );
    public final wcpsParser.sliceExpr_return sliceExpr() throws RecognitionException {
        wcpsParser.sliceExpr_return retval = new wcpsParser.sliceExpr_return();
        retval.start = input.LT(1);
        int sliceExpr_StartIndex = input.index();
        Object root_0 = null;

        Token LBRACKET155=null;
        Token RBRACKET156=null;
        Token SLICE157=null;
        Token LPAREN158=null;
        Token COMMA159=null;
        Token LBRACE160=null;
        Token RBRACE161=null;
        Token RPAREN162=null;
        wcpsParser.coverageAtom_return e1 = null;

        wcpsParser.dimensionPointList_return dpl = null;

        wcpsParser.coverageExpr_return e2 = null;


        Object LBRACKET155_tree=null;
        Object RBRACKET156_tree=null;
        Object SLICE157_tree=null;
        Object LPAREN158_tree=null;
        Object COMMA159_tree=null;
        Object LBRACE160_tree=null;
        Object RBRACE161_tree=null;
        Object RPAREN162_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 53) ) { return retval; }
            // wcps.g:295:2: (e1= coverageAtom LBRACKET dpl= dimensionPointList RBRACKET | SLICE LPAREN e2= coverageExpr COMMA LBRACE dpl= dimensionPointList RBRACE RPAREN )
            int alt49=2;
            alt49 = dfa49.predict(input);
            switch (alt49) {
                case 1 :
                    // wcps.g:295:4: e1= coverageAtom LBRACKET dpl= dimensionPointList RBRACKET
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageAtom_in_sliceExpr3168);
                    e1=coverageAtom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    LBRACKET155=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_sliceExpr3170); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LBRACKET155_tree = (Object)adaptor.create(LBRACKET155);
                    adaptor.addChild(root_0, LBRACKET155_tree);
                    }
                    pushFollow(FOLLOW_dimensionPointList_in_sliceExpr3174);
                    dpl=dimensionPointList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dpl.getTree());
                    RBRACKET156=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_sliceExpr3176); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RBRACKET156_tree = (Object)adaptor.create(RBRACKET156);
                    adaptor.addChild(root_0, RBRACKET156_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new SliceExpr((e1!=null?e1.value:null), (dpl!=null?dpl.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:296:4: SLICE LPAREN e2= coverageExpr COMMA LBRACE dpl= dimensionPointList RBRACE RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    SLICE157=(Token)match(input,SLICE,FOLLOW_SLICE_in_sliceExpr3183); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SLICE157_tree = (Object)adaptor.create(SLICE157);
                    adaptor.addChild(root_0, SLICE157_tree);
                    }
                    LPAREN158=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_sliceExpr3185); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN158_tree = (Object)adaptor.create(LPAREN158);
                    adaptor.addChild(root_0, LPAREN158_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_sliceExpr3189);
                    e2=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    COMMA159=(Token)match(input,COMMA,FOLLOW_COMMA_in_sliceExpr3191); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA159_tree = (Object)adaptor.create(COMMA159);
                    adaptor.addChild(root_0, COMMA159_tree);
                    }
                    LBRACE160=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_sliceExpr3193); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LBRACE160_tree = (Object)adaptor.create(LBRACE160);
                    adaptor.addChild(root_0, LBRACE160_tree);
                    }
                    pushFollow(FOLLOW_dimensionPointList_in_sliceExpr3197);
                    dpl=dimensionPointList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dpl.getTree());
                    RBRACE161=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_sliceExpr3199); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RBRACE161_tree = (Object)adaptor.create(RBRACE161);
                    adaptor.addChild(root_0, RBRACE161_tree);
                    }
                    RPAREN162=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_sliceExpr3201); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN162_tree = (Object)adaptor.create(RPAREN162);
                    adaptor.addChild(root_0, RPAREN162_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new SliceExpr((e2!=null?e2.value:null), (dpl!=null?dpl.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 53, sliceExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "sliceExpr"

    public static class extendExpr_return extends ParserRuleReturnScope {
        public ExtendExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "extendExpr"
    // wcps.g:298:1: extendExpr returns [ExtendExpr value] : EXTEND LPAREN e1= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN ;
    public final wcpsParser.extendExpr_return extendExpr() throws RecognitionException {
        wcpsParser.extendExpr_return retval = new wcpsParser.extendExpr_return();
        retval.start = input.LT(1);
        int extendExpr_StartIndex = input.index();
        Object root_0 = null;

        Token EXTEND163=null;
        Token LPAREN164=null;
        Token COMMA165=null;
        Token LBRACE166=null;
        Token RBRACE167=null;
        Token RPAREN168=null;
        wcpsParser.coverageExpr_return e1 = null;

        wcpsParser.dimensionIntervalList_return dil = null;


        Object EXTEND163_tree=null;
        Object LPAREN164_tree=null;
        Object COMMA165_tree=null;
        Object LBRACE166_tree=null;
        Object RBRACE167_tree=null;
        Object RPAREN168_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 54) ) { return retval; }
            // wcps.g:299:2: ( EXTEND LPAREN e1= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN )
            // wcps.g:299:4: EXTEND LPAREN e1= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN
            {
            root_0 = (Object)adaptor.nil();

            EXTEND163=(Token)match(input,EXTEND,FOLLOW_EXTEND_in_extendExpr3216); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EXTEND163_tree = (Object)adaptor.create(EXTEND163);
            adaptor.addChild(root_0, EXTEND163_tree);
            }
            LPAREN164=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_extendExpr3218); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN164_tree = (Object)adaptor.create(LPAREN164);
            adaptor.addChild(root_0, LPAREN164_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_extendExpr3222);
            e1=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            COMMA165=(Token)match(input,COMMA,FOLLOW_COMMA_in_extendExpr3224); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA165_tree = (Object)adaptor.create(COMMA165);
            adaptor.addChild(root_0, COMMA165_tree);
            }
            LBRACE166=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_extendExpr3226); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE166_tree = (Object)adaptor.create(LBRACE166);
            adaptor.addChild(root_0, LBRACE166_tree);
            }
            pushFollow(FOLLOW_dimensionIntervalList_in_extendExpr3230);
            dil=dimensionIntervalList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, dil.getTree());
            RBRACE167=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_extendExpr3232); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACE167_tree = (Object)adaptor.create(RBRACE167);
            adaptor.addChild(root_0, RBRACE167_tree);
            }
            RPAREN168=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_extendExpr3234); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN168_tree = (Object)adaptor.create(RPAREN168);
            adaptor.addChild(root_0, RPAREN168_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new ExtendExpr((e1!=null?e1.value:null), (dil!=null?dil.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 54, extendExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "extendExpr"

    public static class castExpr_return extends ParserRuleReturnScope {
        public CastExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "castExpr"
    // wcps.g:301:1: castExpr returns [CastExpr value] : LPAREN e1= rangeType RPAREN e2= coverageExpr ;
    public final wcpsParser.castExpr_return castExpr() throws RecognitionException {
        wcpsParser.castExpr_return retval = new wcpsParser.castExpr_return();
        retval.start = input.LT(1);
        int castExpr_StartIndex = input.index();
        Object root_0 = null;

        Token LPAREN169=null;
        Token RPAREN170=null;
        wcpsParser.rangeType_return e1 = null;

        wcpsParser.coverageExpr_return e2 = null;


        Object LPAREN169_tree=null;
        Object RPAREN170_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 55) ) { return retval; }
            // wcps.g:302:5: ( LPAREN e1= rangeType RPAREN e2= coverageExpr )
            // wcps.g:302:7: LPAREN e1= rangeType RPAREN e2= coverageExpr
            {
            root_0 = (Object)adaptor.nil();

            LPAREN169=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_castExpr3252); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN169_tree = (Object)adaptor.create(LPAREN169);
            adaptor.addChild(root_0, LPAREN169_tree);
            }
            pushFollow(FOLLOW_rangeType_in_castExpr3256);
            e1=rangeType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            RPAREN170=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_castExpr3258); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN170_tree = (Object)adaptor.create(RPAREN170);
            adaptor.addChild(root_0, RPAREN170_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_castExpr3262);
            e2=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new CastExpr((e2!=null?e2.value:null), (e1!=null?e1.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 55, castExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "castExpr"

    public static class rangeType_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rangeType"
    // wcps.g:304:1: rangeType returns [String value] : (type= ( BOOLEAN | CHAR | SHORT | LONG | FLOAT | DOUBLE | COMPLEX | COMPLEX2 ) | UNSIGNED type= ( CHAR | SHORT | LONG ) );
    public final wcpsParser.rangeType_return rangeType() throws RecognitionException {
        wcpsParser.rangeType_return retval = new wcpsParser.rangeType_return();
        retval.start = input.LT(1);
        int rangeType_StartIndex = input.index();
        Object root_0 = null;

        Token type=null;
        Token UNSIGNED171=null;

        Object type_tree=null;
        Object UNSIGNED171_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 56) ) { return retval; }
            // wcps.g:305:5: (type= ( BOOLEAN | CHAR | SHORT | LONG | FLOAT | DOUBLE | COMPLEX | COMPLEX2 ) | UNSIGNED type= ( CHAR | SHORT | LONG ) )
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( ((LA50_0>=BOOLEAN && LA50_0<=COMPLEX2)) ) {
                alt50=1;
            }
            else if ( (LA50_0==UNSIGNED) ) {
                alt50=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;
            }
            switch (alt50) {
                case 1 :
                    // wcps.g:305:7: type= ( BOOLEAN | CHAR | SHORT | LONG | FLOAT | DOUBLE | COMPLEX | COMPLEX2 )
                    {
                    root_0 = (Object)adaptor.nil();

                    type=(Token)input.LT(1);
                    if ( (input.LA(1)>=BOOLEAN && input.LA(1)<=COMPLEX2) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(type));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    if ( state.backtracking==0 ) {
                       retval.value = new String((type!=null?type.getText():null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:306:7: UNSIGNED type= ( CHAR | SHORT | LONG )
                    {
                    root_0 = (Object)adaptor.nil();

                    UNSIGNED171=(Token)match(input,UNSIGNED,FOLLOW_UNSIGNED_in_rangeType3311); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    UNSIGNED171_tree = (Object)adaptor.create(UNSIGNED171);
                    adaptor.addChild(root_0, UNSIGNED171_tree);
                    }
                    type=(Token)input.LT(1);
                    if ( (input.LA(1)>=CHAR && input.LA(1)<=LONG) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(type));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    if ( state.backtracking==0 ) {
                       retval.value = new String("unsigned " + (type!=null?type.getText():null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 56, rangeType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "rangeType"

    public static class fieldExpr_return extends ParserRuleReturnScope {
        public SelectExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fieldExpr"
    // wcps.g:308:1: fieldExpr returns [SelectExpr value] : e1= coverageAtom DOT e2= fieldName ;
    public final wcpsParser.fieldExpr_return fieldExpr() throws RecognitionException {
        wcpsParser.fieldExpr_return retval = new wcpsParser.fieldExpr_return();
        retval.start = input.LT(1);
        int fieldExpr_StartIndex = input.index();
        Object root_0 = null;

        Token DOT172=null;
        wcpsParser.coverageAtom_return e1 = null;

        wcpsParser.fieldName_return e2 = null;


        Object DOT172_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 57) ) { return retval; }
            // wcps.g:309:5: (e1= coverageAtom DOT e2= fieldName )
            // wcps.g:309:7: e1= coverageAtom DOT e2= fieldName
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_coverageAtom_in_fieldExpr3344);
            e1=coverageAtom();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            DOT172=(Token)match(input,DOT,FOLLOW_DOT_in_fieldExpr3346); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DOT172_tree = (Object)adaptor.create(DOT172);
            adaptor.addChild(root_0, DOT172_tree);
            }
            pushFollow(FOLLOW_fieldName_in_fieldExpr3350);
            e2=fieldName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new SelectExpr((e1!=null?e1.value:null), (e2!=null?e2.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 57, fieldExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "fieldExpr"

    public static class booleanScalarExpr_return extends ParserRuleReturnScope {
        public BooleanScalarExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "booleanScalarExpr"
    // wcps.g:313:1: booleanScalarExpr returns [BooleanScalarExpr value] : e1= booleanScalarTerm (op= ( OR | XOR ) e2= booleanScalarTerm )* ;
    public final wcpsParser.booleanScalarExpr_return booleanScalarExpr() throws RecognitionException {
        wcpsParser.booleanScalarExpr_return retval = new wcpsParser.booleanScalarExpr_return();
        retval.start = input.LT(1);
        int booleanScalarExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.booleanScalarTerm_return e1 = null;

        wcpsParser.booleanScalarTerm_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 58) ) { return retval; }
            // wcps.g:314:5: (e1= booleanScalarTerm (op= ( OR | XOR ) e2= booleanScalarTerm )* )
            // wcps.g:314:7: e1= booleanScalarTerm (op= ( OR | XOR ) e2= booleanScalarTerm )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_booleanScalarTerm_in_booleanScalarExpr3375);
            e1=booleanScalarTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:315:7: (op= ( OR | XOR ) e2= booleanScalarTerm )*
            loop51:
            do {
                int alt51=2;
                alt51 = dfa51.predict(input);
                switch (alt51) {
            	case 1 :
            	    // wcps.g:315:8: op= ( OR | XOR ) e2= booleanScalarTerm
            	    {
            	    op=(Token)input.LT(1);
            	    if ( (input.LA(1)>=OR && input.LA(1)<=XOR) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_booleanScalarTerm_in_booleanScalarExpr3396);
            	    e2=booleanScalarTerm();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new BooleanScalarExpr((op!=null?op.getText():null), retval.value, (e2!=null?e2.value:null));
            	    }

            	    }
            	    break;

            	default :
            	    break loop51;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 58, booleanScalarExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "booleanScalarExpr"

    public static class booleanScalarTerm_return extends ParserRuleReturnScope {
        public BooleanScalarExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "booleanScalarTerm"
    // wcps.g:317:1: booleanScalarTerm returns [BooleanScalarExpr value] : e1= booleanScalarNegation (op= AND e2= booleanScalarNegation )* ;
    public final wcpsParser.booleanScalarTerm_return booleanScalarTerm() throws RecognitionException {
        wcpsParser.booleanScalarTerm_return retval = new wcpsParser.booleanScalarTerm_return();
        retval.start = input.LT(1);
        int booleanScalarTerm_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.booleanScalarNegation_return e1 = null;

        wcpsParser.booleanScalarNegation_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 59) ) { return retval; }
            // wcps.g:318:2: (e1= booleanScalarNegation (op= AND e2= booleanScalarNegation )* )
            // wcps.g:318:4: e1= booleanScalarNegation (op= AND e2= booleanScalarNegation )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_booleanScalarNegation_in_booleanScalarTerm3418);
            e1=booleanScalarNegation();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:319:4: (op= AND e2= booleanScalarNegation )*
            loop52:
            do {
                int alt52=2;
                alt52 = dfa52.predict(input);
                switch (alt52) {
            	case 1 :
            	    // wcps.g:319:5: op= AND e2= booleanScalarNegation
            	    {
            	    op=(Token)match(input,AND,FOLLOW_AND_in_booleanScalarTerm3428); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    op_tree = (Object)adaptor.create(op);
            	    adaptor.addChild(root_0, op_tree);
            	    }
            	    pushFollow(FOLLOW_booleanScalarNegation_in_booleanScalarTerm3432);
            	    e2=booleanScalarNegation();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new BooleanScalarExpr((op!=null?op.getText():null), retval.value, (e2!=null?e2.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop52;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 59, booleanScalarTerm_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "booleanScalarTerm"

    public static class booleanScalarNegation_return extends ParserRuleReturnScope {
        public BooleanScalarExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "booleanScalarNegation"
    // wcps.g:321:1: booleanScalarNegation returns [BooleanScalarExpr value] : (e1= booleanScalarAtom | op= NOT e1= booleanScalarAtom );
    public final wcpsParser.booleanScalarNegation_return booleanScalarNegation() throws RecognitionException {
        wcpsParser.booleanScalarNegation_return retval = new wcpsParser.booleanScalarNegation_return();
        retval.start = input.LT(1);
        int booleanScalarNegation_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.booleanScalarAtom_return e1 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 60) ) { return retval; }
            // wcps.g:322:2: (e1= booleanScalarAtom | op= NOT e1= booleanScalarAtom )
            int alt53=2;
            alt53 = dfa53.predict(input);
            switch (alt53) {
                case 1 :
                    // wcps.g:322:4: e1= booleanScalarAtom
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_booleanScalarAtom_in_booleanScalarNegation3453);
                    e1=booleanScalarAtom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = (e1!=null?e1.value:null); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:323:4: op= NOT e1= booleanScalarAtom
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,NOT,FOLLOW_NOT_in_booleanScalarNegation3462); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    pushFollow(FOLLOW_booleanScalarAtom_in_booleanScalarNegation3466);
                    e1=booleanScalarAtom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new BooleanScalarExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 60, booleanScalarNegation_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "booleanScalarNegation"

    public static class booleanScalarAtom_return extends ParserRuleReturnScope {
        public BooleanScalarExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "booleanScalarAtom"
    // wcps.g:325:1: booleanScalarAtom returns [BooleanScalarExpr value] : ( LPAREN e1= booleanScalarExpr RPAREN | s1= stringScalarExpr cop= compOp s2= stringScalarExpr | n1= numericScalarExpr cop= compOp n2= numericScalarExpr | e= BOOLEANCONSTANT );
    public final wcpsParser.booleanScalarAtom_return booleanScalarAtom() throws RecognitionException {
        wcpsParser.booleanScalarAtom_return retval = new wcpsParser.booleanScalarAtom_return();
        retval.start = input.LT(1);
        int booleanScalarAtom_StartIndex = input.index();
        Object root_0 = null;

        Token e=null;
        Token LPAREN173=null;
        Token RPAREN174=null;
        wcpsParser.booleanScalarExpr_return e1 = null;

        wcpsParser.stringScalarExpr_return s1 = null;

        wcpsParser.compOp_return cop = null;

        wcpsParser.stringScalarExpr_return s2 = null;

        wcpsParser.numericScalarExpr_return n1 = null;

        wcpsParser.numericScalarExpr_return n2 = null;


        Object e_tree=null;
        Object LPAREN173_tree=null;
        Object RPAREN174_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 61) ) { return retval; }
            // wcps.g:326:2: ( LPAREN e1= booleanScalarExpr RPAREN | s1= stringScalarExpr cop= compOp s2= stringScalarExpr | n1= numericScalarExpr cop= compOp n2= numericScalarExpr | e= BOOLEANCONSTANT )
            int alt54=4;
            alt54 = dfa54.predict(input);
            switch (alt54) {
                case 1 :
                    // wcps.g:326:4: LPAREN e1= booleanScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    LPAREN173=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_booleanScalarAtom3481); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN173_tree = (Object)adaptor.create(LPAREN173);
                    adaptor.addChild(root_0, LPAREN173_tree);
                    }
                    pushFollow(FOLLOW_booleanScalarExpr_in_booleanScalarAtom3485);
                    e1=booleanScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN174=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_booleanScalarAtom3487); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN174_tree = (Object)adaptor.create(RPAREN174);
                    adaptor.addChild(root_0, RPAREN174_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = (e1!=null?e1.value:null); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:327:4: s1= stringScalarExpr cop= compOp s2= stringScalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_stringScalarExpr_in_booleanScalarAtom3496);
                    s1=stringScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, s1.getTree());
                    pushFollow(FOLLOW_compOp_in_booleanScalarAtom3500);
                    cop=compOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, cop.getTree());
                    pushFollow(FOLLOW_stringScalarExpr_in_booleanScalarAtom3504);
                    s2=stringScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, s2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new BooleanScalarExpr((cop!=null?cop.value:null), (s1!=null?s1.value:null), (s2!=null?s2.value:null)); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:328:4: n1= numericScalarExpr cop= compOp n2= numericScalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_numericScalarExpr_in_booleanScalarAtom3514);
                    n1=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, n1.getTree());
                    pushFollow(FOLLOW_compOp_in_booleanScalarAtom3518);
                    cop=compOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, cop.getTree());
                    pushFollow(FOLLOW_numericScalarExpr_in_booleanScalarAtom3522);
                    n2=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, n2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new BooleanScalarExpr((cop!=null?cop.value:null), (n1!=null?n1.value:null), (n2!=null?n2.value:null)); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:329:4: e= BOOLEANCONSTANT
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)match(input,BOOLEANCONSTANT,FOLLOW_BOOLEANCONSTANT_in_booleanScalarAtom3532); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    e_tree = (Object)adaptor.create(e);
                    adaptor.addChild(root_0, e_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new BooleanScalarExpr((e!=null?e.getText():null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 61, booleanScalarAtom_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "booleanScalarAtom"

    public static class numericScalarExpr_return extends ParserRuleReturnScope {
        public NumericScalarExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "numericScalarExpr"
    // wcps.g:331:1: numericScalarExpr returns [NumericScalarExpr value] : e1= numericScalarTerm (op= ( PLUS | MINUS ) e2= numericScalarTerm )* ;
    public final wcpsParser.numericScalarExpr_return numericScalarExpr() throws RecognitionException {
        wcpsParser.numericScalarExpr_return retval = new wcpsParser.numericScalarExpr_return();
        retval.start = input.LT(1);
        int numericScalarExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.numericScalarTerm_return e1 = null;

        wcpsParser.numericScalarTerm_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 62) ) { return retval; }
            // wcps.g:332:2: (e1= numericScalarTerm (op= ( PLUS | MINUS ) e2= numericScalarTerm )* )
            // wcps.g:332:4: e1= numericScalarTerm (op= ( PLUS | MINUS ) e2= numericScalarTerm )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_numericScalarTerm_in_numericScalarExpr3549);
            e1=numericScalarTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
              retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:333:4: (op= ( PLUS | MINUS ) e2= numericScalarTerm )*
            loop55:
            do {
                int alt55=2;
                alt55 = dfa55.predict(input);
                switch (alt55) {
            	case 1 :
            	    // wcps.g:333:5: op= ( PLUS | MINUS ) e2= numericScalarTerm
            	    {
            	    op=(Token)input.LT(1);
            	    if ( (input.LA(1)>=PLUS && input.LA(1)<=MINUS) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_numericScalarTerm_in_numericScalarExpr3567);
            	    e2=numericScalarTerm();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new NumericScalarExpr((op!=null?op.getText():null), retval.value, (e2!=null?e2.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop55;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 62, numericScalarExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "numericScalarExpr"

    public static class numericScalarTerm_return extends ParserRuleReturnScope {
        public NumericScalarExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "numericScalarTerm"
    // wcps.g:335:1: numericScalarTerm returns [NumericScalarExpr value] : e1= numericScalarFactor (op= ( MULT | DIVIDE ) e2= numericScalarFactor )* ;
    public final wcpsParser.numericScalarTerm_return numericScalarTerm() throws RecognitionException {
        wcpsParser.numericScalarTerm_return retval = new wcpsParser.numericScalarTerm_return();
        retval.start = input.LT(1);
        int numericScalarTerm_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        wcpsParser.numericScalarFactor_return e1 = null;

        wcpsParser.numericScalarFactor_return e2 = null;


        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 63) ) { return retval; }
            // wcps.g:336:2: (e1= numericScalarFactor (op= ( MULT | DIVIDE ) e2= numericScalarFactor )* )
            // wcps.g:336:4: e1= numericScalarFactor (op= ( MULT | DIVIDE ) e2= numericScalarFactor )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_numericScalarFactor_in_numericScalarTerm3586);
            e1=numericScalarFactor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:337:3: (op= ( MULT | DIVIDE ) e2= numericScalarFactor )*
            loop56:
            do {
                int alt56=2;
                alt56 = dfa56.predict(input);
                switch (alt56) {
            	case 1 :
            	    // wcps.g:337:4: op= ( MULT | DIVIDE ) e2= numericScalarFactor
            	    {
            	    op=(Token)input.LT(1);
            	    if ( (input.LA(1)>=MULT && input.LA(1)<=DIVIDE) ) {
            	        input.consume();
            	        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }

            	    pushFollow(FOLLOW_numericScalarFactor_in_numericScalarTerm3603);
            	    e2=numericScalarFactor();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new NumericScalarExpr((op!=null?op.getText():null), retval.value, (e2!=null?e2.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop56;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 63, numericScalarTerm_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "numericScalarTerm"

    public static class numericScalarFactor_return extends ParserRuleReturnScope {
        public NumericScalarExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "numericScalarFactor"
    // wcps.g:339:1: numericScalarFactor returns [NumericScalarExpr value] : ( LPAREN e1= numericScalarExpr RPAREN | op= MINUS e10= numericScalarFactor | op= ABS LPAREN e12= numericScalarExpr RPAREN | op= SQRT LPAREN e11= numericScalarExpr RPAREN | op= ROUND LPAREN e1= numericScalarExpr RPAREN | e= INTEGERCONSTANT | e= FLOATCONSTANT | e2= complexConstant | e3= condenseExpr | e4= variableName );
    public final wcpsParser.numericScalarFactor_return numericScalarFactor() throws RecognitionException {
        wcpsParser.numericScalarFactor_return retval = new wcpsParser.numericScalarFactor_return();
        retval.start = input.LT(1);
        int numericScalarFactor_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token e=null;
        Token LPAREN175=null;
        Token RPAREN176=null;
        Token LPAREN177=null;
        Token RPAREN178=null;
        Token LPAREN179=null;
        Token RPAREN180=null;
        Token LPAREN181=null;
        Token RPAREN182=null;
        wcpsParser.numericScalarExpr_return e1 = null;

        wcpsParser.numericScalarFactor_return e10 = null;

        wcpsParser.numericScalarExpr_return e12 = null;

        wcpsParser.numericScalarExpr_return e11 = null;

        wcpsParser.complexConstant_return e2 = null;

        wcpsParser.condenseExpr_return e3 = null;

        wcpsParser.variableName_return e4 = null;


        Object op_tree=null;
        Object e_tree=null;
        Object LPAREN175_tree=null;
        Object RPAREN176_tree=null;
        Object LPAREN177_tree=null;
        Object RPAREN178_tree=null;
        Object LPAREN179_tree=null;
        Object RPAREN180_tree=null;
        Object LPAREN181_tree=null;
        Object RPAREN182_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 64) ) { return retval; }
            // wcps.g:340:5: ( LPAREN e1= numericScalarExpr RPAREN | op= MINUS e10= numericScalarFactor | op= ABS LPAREN e12= numericScalarExpr RPAREN | op= SQRT LPAREN e11= numericScalarExpr RPAREN | op= ROUND LPAREN e1= numericScalarExpr RPAREN | e= INTEGERCONSTANT | e= FLOATCONSTANT | e2= complexConstant | e3= condenseExpr | e4= variableName )
            int alt57=10;
            alt57 = dfa57.predict(input);
            switch (alt57) {
                case 1 :
                    // wcps.g:340:7: LPAREN e1= numericScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    LPAREN175=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_numericScalarFactor3623); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN175_tree = (Object)adaptor.create(LPAREN175);
                    adaptor.addChild(root_0, LPAREN175_tree);
                    }
                    pushFollow(FOLLOW_numericScalarExpr_in_numericScalarFactor3627);
                    e1=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN176=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_numericScalarFactor3629); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN176_tree = (Object)adaptor.create(RPAREN176);
                    adaptor.addChild(root_0, RPAREN176_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = (e1!=null?e1.value:null); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:341:7: op= MINUS e10= numericScalarFactor
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,MINUS,FOLLOW_MINUS_in_numericScalarFactor3641); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    pushFollow(FOLLOW_numericScalarFactor_in_numericScalarFactor3645);
                    e10=numericScalarFactor();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e10.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((op!=null?op.getText():null), (e10!=null?e10.value:null)); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:342:7: op= ABS LPAREN e12= numericScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,ABS,FOLLOW_ABS_in_numericScalarFactor3657); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN177=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_numericScalarFactor3659); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN177_tree = (Object)adaptor.create(LPAREN177);
                    adaptor.addChild(root_0, LPAREN177_tree);
                    }
                    pushFollow(FOLLOW_numericScalarExpr_in_numericScalarFactor3663);
                    e12=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e12.getTree());
                    RPAREN178=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_numericScalarFactor3665); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN178_tree = (Object)adaptor.create(RPAREN178);
                    adaptor.addChild(root_0, RPAREN178_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((op!=null?op.getText():null), (e12!=null?e12.value:null)); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:343:7: op= SQRT LPAREN e11= numericScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SQRT,FOLLOW_SQRT_in_numericScalarFactor3677); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN179=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_numericScalarFactor3679); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN179_tree = (Object)adaptor.create(LPAREN179);
                    adaptor.addChild(root_0, LPAREN179_tree);
                    }
                    pushFollow(FOLLOW_numericScalarExpr_in_numericScalarFactor3683);
                    e11=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e11.getTree());
                    RPAREN180=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_numericScalarFactor3685); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN180_tree = (Object)adaptor.create(RPAREN180);
                    adaptor.addChild(root_0, RPAREN180_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((op!=null?op.getText():null), (e11!=null?e11.value:null)); 
                    }

                    }
                    break;
                case 5 :
                    // wcps.g:344:7: op= ROUND LPAREN e1= numericScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,ROUND,FOLLOW_ROUND_in_numericScalarFactor3697); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN181=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_numericScalarFactor3699); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN181_tree = (Object)adaptor.create(LPAREN181);
                    adaptor.addChild(root_0, LPAREN181_tree);
                    }
                    pushFollow(FOLLOW_numericScalarExpr_in_numericScalarFactor3703);
                    e1=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN182=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_numericScalarFactor3705); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN182_tree = (Object)adaptor.create(RPAREN182);
                    adaptor.addChild(root_0, RPAREN182_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 6 :
                    // wcps.g:345:7: e= INTEGERCONSTANT
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)match(input,INTEGERCONSTANT,FOLLOW_INTEGERCONSTANT_in_numericScalarFactor3717); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    e_tree = (Object)adaptor.create(e);
                    adaptor.addChild(root_0, e_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((e!=null?e.getText():null)); 
                    }

                    }
                    break;
                case 7 :
                    // wcps.g:346:7: e= FLOATCONSTANT
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)match(input,FLOATCONSTANT,FOLLOW_FLOATCONSTANT_in_numericScalarFactor3729); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    e_tree = (Object)adaptor.create(e);
                    adaptor.addChild(root_0, e_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((e!=null?e.getText():null)); 
                    }

                    }
                    break;
                case 8 :
                    // wcps.g:347:7: e2= complexConstant
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_complexConstant_in_numericScalarFactor3741);
                    e2=complexConstant();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((e2!=null?e2.value:null)); 
                    }

                    }
                    break;
                case 9 :
                    // wcps.g:348:7: e3= condenseExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_condenseExpr_in_numericScalarFactor3753);
                    e3=condenseExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((e3!=null?e3.value:null)); 
                    }

                    }
                    break;
                case 10 :
                    // wcps.g:349:7: e4= variableName
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_variableName_in_numericScalarFactor3765);
                    e4=variableName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e4.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr("var", (e4!=null?e4.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 64, numericScalarFactor_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "numericScalarFactor"

    public static class compOp_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "compOp"
    // wcps.g:351:1: compOp returns [String value] : ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE );
    public final wcpsParser.compOp_return compOp() throws RecognitionException {
        wcpsParser.compOp_return retval = new wcpsParser.compOp_return();
        retval.start = input.LT(1);
        int compOp_StartIndex = input.index();
        Object root_0 = null;

        Token EQUALS183=null;
        Token NOTEQUALS184=null;
        Token LT185=null;
        Token GT186=null;
        Token LTE187=null;
        Token GTE188=null;

        Object EQUALS183_tree=null;
        Object NOTEQUALS184_tree=null;
        Object LT185_tree=null;
        Object GT186_tree=null;
        Object LTE187_tree=null;
        Object GTE188_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 65) ) { return retval; }
            // wcps.g:352:2: ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE )
            int alt58=6;
            switch ( input.LA(1) ) {
            case EQUALS:
                {
                alt58=1;
                }
                break;
            case NOTEQUALS:
                {
                alt58=2;
                }
                break;
            case LT:
                {
                alt58=3;
                }
                break;
            case GT:
                {
                alt58=4;
                }
                break;
            case LTE:
                {
                alt58=5;
                }
                break;
            case GTE:
                {
                alt58=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 58, 0, input);

                throw nvae;
            }

            switch (alt58) {
                case 1 :
                    // wcps.g:352:4: EQUALS
                    {
                    root_0 = (Object)adaptor.nil();

                    EQUALS183=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_compOp3783); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EQUALS183_tree = (Object)adaptor.create(EQUALS183);
                    adaptor.addChild(root_0, EQUALS183_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new String("equals"); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:353:4: NOTEQUALS
                    {
                    root_0 = (Object)adaptor.nil();

                    NOTEQUALS184=(Token)match(input,NOTEQUALS,FOLLOW_NOTEQUALS_in_compOp3790); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NOTEQUALS184_tree = (Object)adaptor.create(NOTEQUALS184);
                    adaptor.addChild(root_0, NOTEQUALS184_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new String("notEqual"); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:354:4: LT
                    {
                    root_0 = (Object)adaptor.nil();

                    LT185=(Token)match(input,LT,FOLLOW_LT_in_compOp3797); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LT185_tree = (Object)adaptor.create(LT185);
                    adaptor.addChild(root_0, LT185_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new String("lessThan"); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:355:4: GT
                    {
                    root_0 = (Object)adaptor.nil();

                    GT186=(Token)match(input,GT,FOLLOW_GT_in_compOp3804); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    GT186_tree = (Object)adaptor.create(GT186);
                    adaptor.addChild(root_0, GT186_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new String("greaterThan"); 
                    }

                    }
                    break;
                case 5 :
                    // wcps.g:356:4: LTE
                    {
                    root_0 = (Object)adaptor.nil();

                    LTE187=(Token)match(input,LTE,FOLLOW_LTE_in_compOp3811); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LTE187_tree = (Object)adaptor.create(LTE187);
                    adaptor.addChild(root_0, LTE187_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new String("lessOrEqual"); 
                    }

                    }
                    break;
                case 6 :
                    // wcps.g:357:4: GTE
                    {
                    root_0 = (Object)adaptor.nil();

                    GTE188=(Token)match(input,GTE,FOLLOW_GTE_in_compOp3818); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    GTE188_tree = (Object)adaptor.create(GTE188);
                    adaptor.addChild(root_0, GTE188_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new String("greaterOrEqual"); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 65, compOp_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "compOp"

    public static class dimensionIntervalList_return extends ParserRuleReturnScope {
        public DimensionIntervalList value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dimensionIntervalList"
    // wcps.g:359:1: dimensionIntervalList returns [DimensionIntervalList value] : elem= dimensionIntervalElement ( COMMA elem= dimensionIntervalElement )* ;
    public final wcpsParser.dimensionIntervalList_return dimensionIntervalList() throws RecognitionException {
        wcpsParser.dimensionIntervalList_return retval = new wcpsParser.dimensionIntervalList_return();
        retval.start = input.LT(1);
        int dimensionIntervalList_StartIndex = input.index();
        Object root_0 = null;

        Token COMMA189=null;
        wcpsParser.dimensionIntervalElement_return elem = null;


        Object COMMA189_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 66) ) { return retval; }
            // wcps.g:360:5: (elem= dimensionIntervalElement ( COMMA elem= dimensionIntervalElement )* )
            // wcps.g:360:7: elem= dimensionIntervalElement ( COMMA elem= dimensionIntervalElement )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_dimensionIntervalElement_in_dimensionIntervalList3838);
            elem=dimensionIntervalElement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, elem.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new DimensionIntervalList((elem!=null?elem.value:null)); 
            }
            // wcps.g:361:9: ( COMMA elem= dimensionIntervalElement )*
            loop59:
            do {
                int alt59=2;
                int LA59_0 = input.LA(1);

                if ( (LA59_0==COMMA) ) {
                    alt59=1;
                }


                switch (alt59) {
            	case 1 :
            	    // wcps.g:361:10: COMMA elem= dimensionIntervalElement
            	    {
            	    COMMA189=(Token)match(input,COMMA,FOLLOW_COMMA_in_dimensionIntervalList3851); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA189_tree = (Object)adaptor.create(COMMA189);
            	    adaptor.addChild(root_0, COMMA189_tree);
            	    }
            	    pushFollow(FOLLOW_dimensionIntervalElement_in_dimensionIntervalList3855);
            	    elem=dimensionIntervalElement();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, elem.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value.add((elem!=null?elem.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop59;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 66, dimensionIntervalList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "dimensionIntervalList"

    public static class dimensionIntervalElement_return extends ParserRuleReturnScope {
        public DimensionIntervalElement value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dimensionIntervalElement"
    // wcps.g:363:1: dimensionIntervalElement returns [DimensionIntervalElement value] : aname= axisName ( COLON crs= crsName )? LPAREN die= dimensionIntervalExpr RPAREN ;
    public final wcpsParser.dimensionIntervalElement_return dimensionIntervalElement() throws RecognitionException {
        wcpsParser.dimensionIntervalElement_return retval = new wcpsParser.dimensionIntervalElement_return();
        retval.start = input.LT(1);
        int dimensionIntervalElement_StartIndex = input.index();
        Object root_0 = null;

        Token COLON190=null;
        Token LPAREN191=null;
        Token RPAREN192=null;
        wcpsParser.axisName_return aname = null;

        wcpsParser.crsName_return crs = null;

        wcpsParser.dimensionIntervalExpr_return die = null;


        Object COLON190_tree=null;
        Object LPAREN191_tree=null;
        Object RPAREN192_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 67) ) { return retval; }
            // wcps.g:364:5: (aname= axisName ( COLON crs= crsName )? LPAREN die= dimensionIntervalExpr RPAREN )
            // wcps.g:364:7: aname= axisName ( COLON crs= crsName )? LPAREN die= dimensionIntervalExpr RPAREN
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_axisName_in_dimensionIntervalElement3880);
            aname=axisName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new DimensionIntervalElement((aname!=null?aname.value:null)); 
            }
            // wcps.g:364:79: ( COLON crs= crsName )?
            int alt60=2;
            int LA60_0 = input.LA(1);

            if ( (LA60_0==COLON) ) {
                alt60=1;
            }
            switch (alt60) {
                case 1 :
                    // wcps.g:364:80: COLON crs= crsName
                    {
                    COLON190=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionIntervalElement3885); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON190_tree = (Object)adaptor.create(COLON190);
                    adaptor.addChild(root_0, COLON190_tree);
                    }
                    pushFollow(FOLLOW_crsName_in_dimensionIntervalElement3889);
                    crs=crsName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, crs.getTree());
                    if ( state.backtracking==0 ) {
                      retval.value.setCrs((crs!=null?crs.value:null)); 
                    }

                    }
                    break;

            }

            LPAREN191=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_dimensionIntervalElement3900); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN191_tree = (Object)adaptor.create(LPAREN191);
            adaptor.addChild(root_0, LPAREN191_tree);
            }
            pushFollow(FOLLOW_dimensionIntervalExpr_in_dimensionIntervalElement3904);
            die=dimensionIntervalExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, die.getTree());
            RPAREN192=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_dimensionIntervalElement3906); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN192_tree = (Object)adaptor.create(RPAREN192);
            adaptor.addChild(root_0, RPAREN192_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value.setIntervalExpr((die!=null?die.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 67, dimensionIntervalElement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "dimensionIntervalElement"

    public static class dimensionIntervalExpr_return extends ParserRuleReturnScope {
        public DimensionIntervalExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dimensionIntervalExpr"
    // wcps.g:367:1: dimensionIntervalExpr returns [DimensionIntervalExpr value] : (e1= scalarExpr COLON e2= scalarExpr | DOMAIN LPAREN e3= coverageName COLON e4= axisName COLON e5= crsName RPAREN );
    public final wcpsParser.dimensionIntervalExpr_return dimensionIntervalExpr() throws RecognitionException {
        wcpsParser.dimensionIntervalExpr_return retval = new wcpsParser.dimensionIntervalExpr_return();
        retval.start = input.LT(1);
        int dimensionIntervalExpr_StartIndex = input.index();
        Object root_0 = null;

        Token COLON193=null;
        Token DOMAIN194=null;
        Token LPAREN195=null;
        Token COLON196=null;
        Token COLON197=null;
        Token RPAREN198=null;
        wcpsParser.scalarExpr_return e1 = null;

        wcpsParser.scalarExpr_return e2 = null;

        wcpsParser.coverageName_return e3 = null;

        wcpsParser.axisName_return e4 = null;

        wcpsParser.crsName_return e5 = null;


        Object COLON193_tree=null;
        Object DOMAIN194_tree=null;
        Object LPAREN195_tree=null;
        Object COLON196_tree=null;
        Object COLON197_tree=null;
        Object RPAREN198_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 68) ) { return retval; }
            // wcps.g:368:5: (e1= scalarExpr COLON e2= scalarExpr | DOMAIN LPAREN e3= coverageName COLON e4= axisName COLON e5= crsName RPAREN )
            int alt61=2;
            alt61 = dfa61.predict(input);
            switch (alt61) {
                case 1 :
                    // wcps.g:368:7: e1= scalarExpr COLON e2= scalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_scalarExpr_in_dimensionIntervalExpr3929);
                    e1=scalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COLON193=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionIntervalExpr3931); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON193_tree = (Object)adaptor.create(COLON193);
                    adaptor.addChild(root_0, COLON193_tree);
                    }
                    pushFollow(FOLLOW_scalarExpr_in_dimensionIntervalExpr3935);
                    e2=scalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new DimensionIntervalExpr((e1!=null?e1.value:null), (e2!=null?e2.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:369:7: DOMAIN LPAREN e3= coverageName COLON e4= axisName COLON e5= crsName RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    DOMAIN194=(Token)match(input,DOMAIN,FOLLOW_DOMAIN_in_dimensionIntervalExpr3945); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOMAIN194_tree = (Object)adaptor.create(DOMAIN194);
                    adaptor.addChild(root_0, DOMAIN194_tree);
                    }
                    LPAREN195=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_dimensionIntervalExpr3947); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN195_tree = (Object)adaptor.create(LPAREN195);
                    adaptor.addChild(root_0, LPAREN195_tree);
                    }
                    pushFollow(FOLLOW_coverageName_in_dimensionIntervalExpr3951);
                    e3=coverageName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    COLON196=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionIntervalExpr3953); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON196_tree = (Object)adaptor.create(COLON196);
                    adaptor.addChild(root_0, COLON196_tree);
                    }
                    pushFollow(FOLLOW_axisName_in_dimensionIntervalExpr3957);
                    e4=axisName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e4.getTree());
                    COLON197=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionIntervalExpr3959); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON197_tree = (Object)adaptor.create(COLON197);
                    adaptor.addChild(root_0, COLON197_tree);
                    }
                    pushFollow(FOLLOW_crsName_in_dimensionIntervalExpr3963);
                    e5=crsName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e5.getTree());
                    RPAREN198=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_dimensionIntervalExpr3965); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN198_tree = (Object)adaptor.create(RPAREN198);
                    adaptor.addChild(root_0, RPAREN198_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new DimensionIntervalExpr((e3!=null?e3.value:null), (e4!=null?e4.value:null), (e5!=null?e5.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 68, dimensionIntervalExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "dimensionIntervalExpr"

    public static class dimensionPointList_return extends ParserRuleReturnScope {
        public DimensionPointList value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dimensionPointList"
    // wcps.g:371:1: dimensionPointList returns [DimensionPointList value] : elem1= dimensionPointElement ( COMMA elem2= dimensionPointElement )* ;
    public final wcpsParser.dimensionPointList_return dimensionPointList() throws RecognitionException {
        wcpsParser.dimensionPointList_return retval = new wcpsParser.dimensionPointList_return();
        retval.start = input.LT(1);
        int dimensionPointList_StartIndex = input.index();
        Object root_0 = null;

        Token COMMA199=null;
        wcpsParser.dimensionPointElement_return elem1 = null;

        wcpsParser.dimensionPointElement_return elem2 = null;


        Object COMMA199_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 69) ) { return retval; }
            // wcps.g:372:5: (elem1= dimensionPointElement ( COMMA elem2= dimensionPointElement )* )
            // wcps.g:372:7: elem1= dimensionPointElement ( COMMA elem2= dimensionPointElement )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_dimensionPointElement_in_dimensionPointList3988);
            elem1=dimensionPointElement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, elem1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new DimensionPointList((elem1!=null?elem1.value:null)); 
            }
            // wcps.g:373:6: ( COMMA elem2= dimensionPointElement )*
            loop62:
            do {
                int alt62=2;
                int LA62_0 = input.LA(1);

                if ( (LA62_0==COMMA) ) {
                    alt62=1;
                }


                switch (alt62) {
            	case 1 :
            	    // wcps.g:373:7: COMMA elem2= dimensionPointElement
            	    {
            	    COMMA199=(Token)match(input,COMMA,FOLLOW_COMMA_in_dimensionPointList3998); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA199_tree = (Object)adaptor.create(COMMA199);
            	    adaptor.addChild(root_0, COMMA199_tree);
            	    }
            	    pushFollow(FOLLOW_dimensionPointElement_in_dimensionPointList4002);
            	    elem2=dimensionPointElement();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, elem2.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value.add((elem2!=null?elem2.value:null)); 
            	    }

            	    }
            	    break;

            	default :
            	    break loop62;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 69, dimensionPointList_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "dimensionPointList"

    public static class dimensionPointElement_return extends ParserRuleReturnScope {
        public DimensionPointElement value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dimensionPointElement"
    // wcps.g:375:1: dimensionPointElement returns [DimensionPointElement value] : (aname= axisName LPAREN dpe= dimensionPoint RPAREN | aname= axisName COLON crs= crsName LPAREN dpe= dimensionPoint RPAREN );
    public final wcpsParser.dimensionPointElement_return dimensionPointElement() throws RecognitionException {
        wcpsParser.dimensionPointElement_return retval = new wcpsParser.dimensionPointElement_return();
        retval.start = input.LT(1);
        int dimensionPointElement_StartIndex = input.index();
        Object root_0 = null;

        Token LPAREN200=null;
        Token RPAREN201=null;
        Token COLON202=null;
        Token LPAREN203=null;
        Token RPAREN204=null;
        wcpsParser.axisName_return aname = null;

        wcpsParser.dimensionPoint_return dpe = null;

        wcpsParser.crsName_return crs = null;


        Object LPAREN200_tree=null;
        Object RPAREN201_tree=null;
        Object COLON202_tree=null;
        Object LPAREN203_tree=null;
        Object RPAREN204_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 70) ) { return retval; }
            // wcps.g:376:5: (aname= axisName LPAREN dpe= dimensionPoint RPAREN | aname= axisName COLON crs= crsName LPAREN dpe= dimensionPoint RPAREN )
            int alt63=2;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==INTEGERCONSTANT||LA63_0==STRING||LA63_0==NAME) ) {
                int LA63_1 = input.LA(2);

                if ( (LA63_1==COLON) ) {
                    alt63=2;
                }
                else if ( (LA63_1==LPAREN) ) {
                    alt63=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 63, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 63, 0, input);

                throw nvae;
            }
            switch (alt63) {
                case 1 :
                    // wcps.g:376:7: aname= axisName LPAREN dpe= dimensionPoint RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_axisName_in_dimensionPointElement4027);
                    aname=axisName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
                    LPAREN200=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_dimensionPointElement4029); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN200_tree = (Object)adaptor.create(LPAREN200);
                    adaptor.addChild(root_0, LPAREN200_tree);
                    }
                    pushFollow(FOLLOW_dimensionPoint_in_dimensionPointElement4033);
                    dpe=dimensionPoint();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dpe.getTree());
                    RPAREN201=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_dimensionPointElement4035); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN201_tree = (Object)adaptor.create(RPAREN201);
                    adaptor.addChild(root_0, RPAREN201_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new DimensionPointElement((aname!=null?aname.value:null), (dpe!=null?dpe.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:377:7: aname= axisName COLON crs= crsName LPAREN dpe= dimensionPoint RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_axisName_in_dimensionPointElement4047);
                    aname=axisName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
                    COLON202=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionPointElement4049); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON202_tree = (Object)adaptor.create(COLON202);
                    adaptor.addChild(root_0, COLON202_tree);
                    }
                    pushFollow(FOLLOW_crsName_in_dimensionPointElement4053);
                    crs=crsName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, crs.getTree());
                    LPAREN203=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_dimensionPointElement4055); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN203_tree = (Object)adaptor.create(LPAREN203);
                    adaptor.addChild(root_0, LPAREN203_tree);
                    }
                    pushFollow(FOLLOW_dimensionPoint_in_dimensionPointElement4059);
                    dpe=dimensionPoint();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dpe.getTree());
                    RPAREN204=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_dimensionPointElement4061); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN204_tree = (Object)adaptor.create(RPAREN204);
                    adaptor.addChild(root_0, RPAREN204_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new DimensionPointElement((aname!=null?aname.value:null), (crs!=null?crs.value:null), (dpe!=null?dpe.value:null)); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 70, dimensionPointElement_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "dimensionPointElement"

    public static class dimensionPoint_return extends ParserRuleReturnScope {
        public ScalarExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "dimensionPoint"
    // wcps.g:379:1: dimensionPoint returns [ScalarExpr value] : e1= scalarExpr ;
    public final wcpsParser.dimensionPoint_return dimensionPoint() throws RecognitionException {
        wcpsParser.dimensionPoint_return retval = new wcpsParser.dimensionPoint_return();
        retval.start = input.LT(1);
        int dimensionPoint_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.scalarExpr_return e1 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 71) ) { return retval; }
            // wcps.g:380:5: (e1= scalarExpr )
            // wcps.g:380:7: e1= scalarExpr
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_scalarExpr_in_dimensionPoint4084);
            e1=scalarExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 71, dimensionPoint_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "dimensionPoint"

    public static class interpolationMethod_return extends ParserRuleReturnScope {
        public InterpolationMethod value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "interpolationMethod"
    // wcps.g:382:1: interpolationMethod returns [InterpolationMethod value] : LPAREN type= interpolationType COLON res= nullResistence RPAREN ;
    public final wcpsParser.interpolationMethod_return interpolationMethod() throws RecognitionException {
        wcpsParser.interpolationMethod_return retval = new wcpsParser.interpolationMethod_return();
        retval.start = input.LT(1);
        int interpolationMethod_StartIndex = input.index();
        Object root_0 = null;

        Token LPAREN205=null;
        Token COLON206=null;
        Token RPAREN207=null;
        wcpsParser.interpolationType_return type = null;

        wcpsParser.nullResistence_return res = null;


        Object LPAREN205_tree=null;
        Object COLON206_tree=null;
        Object RPAREN207_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 72) ) { return retval; }
            // wcps.g:383:2: ( LPAREN type= interpolationType COLON res= nullResistence RPAREN )
            // wcps.g:383:4: LPAREN type= interpolationType COLON res= nullResistence RPAREN
            {
            root_0 = (Object)adaptor.nil();

            LPAREN205=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_interpolationMethod4102); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN205_tree = (Object)adaptor.create(LPAREN205);
            adaptor.addChild(root_0, LPAREN205_tree);
            }
            pushFollow(FOLLOW_interpolationType_in_interpolationMethod4106);
            type=interpolationType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, type.getTree());
            COLON206=(Token)match(input,COLON,FOLLOW_COLON_in_interpolationMethod4108); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON206_tree = (Object)adaptor.create(COLON206);
            adaptor.addChild(root_0, COLON206_tree);
            }
            pushFollow(FOLLOW_nullResistence_in_interpolationMethod4112);
            res=nullResistence();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, res.getTree());
            RPAREN207=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_interpolationMethod4114); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN207_tree = (Object)adaptor.create(RPAREN207);
            adaptor.addChild(root_0, RPAREN207_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new InterpolationMethod((type!=null?type.value:null), (res!=null?res.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 72, interpolationMethod_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "interpolationMethod"

    public static class interpolationType_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "interpolationType"
    // wcps.g:385:1: interpolationType returns [String value] : type= ( NEAREST | LINEAR | QUADRATIC | CUBIC ) ;
    public final wcpsParser.interpolationType_return interpolationType() throws RecognitionException {
        wcpsParser.interpolationType_return retval = new wcpsParser.interpolationType_return();
        retval.start = input.LT(1);
        int interpolationType_StartIndex = input.index();
        Object root_0 = null;

        Token type=null;

        Object type_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 73) ) { return retval; }
            // wcps.g:386:2: (type= ( NEAREST | LINEAR | QUADRATIC | CUBIC ) )
            // wcps.g:386:4: type= ( NEAREST | LINEAR | QUADRATIC | CUBIC )
            {
            root_0 = (Object)adaptor.nil();

            type=(Token)input.LT(1);
            if ( (input.LA(1)>=NEAREST && input.LA(1)<=CUBIC) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(type));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            if ( state.backtracking==0 ) {
               retval.value = new String((type!=null?type.getText():null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 73, interpolationType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "interpolationType"

    public static class nullResistence_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "nullResistence"
    // wcps.g:388:1: nullResistence returns [String value] : resistance= ( FULL | NONE | HALF | OTHER ) ;
    public final wcpsParser.nullResistence_return nullResistence() throws RecognitionException {
        wcpsParser.nullResistence_return retval = new wcpsParser.nullResistence_return();
        retval.start = input.LT(1);
        int nullResistence_StartIndex = input.index();
        Object root_0 = null;

        Token resistance=null;

        Object resistance_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 74) ) { return retval; }
            // wcps.g:389:2: (resistance= ( FULL | NONE | HALF | OTHER ) )
            // wcps.g:389:4: resistance= ( FULL | NONE | HALF | OTHER )
            {
            root_0 = (Object)adaptor.nil();

            resistance=(Token)input.LT(1);
            if ( (input.LA(1)>=FULL && input.LA(1)<=OTHER) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(resistance));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            if ( state.backtracking==0 ) {
               retval.value = new String((resistance!=null?resistance.getText():null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 74, nullResistence_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "nullResistence"

    public static class condenseOpType_return extends ParserRuleReturnScope {
        public CondenseOperation value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "condenseOpType"
    // wcps.g:391:1: condenseOpType returns [CondenseOperation value] : op= ( PLUS | MULT | MAX | MIN | AND | OR ) ;
    public final wcpsParser.condenseOpType_return condenseOpType() throws RecognitionException {
        wcpsParser.condenseOpType_return retval = new wcpsParser.condenseOpType_return();
        retval.start = input.LT(1);
        int condenseOpType_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;

        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 75) ) { return retval; }
            // wcps.g:392:2: (op= ( PLUS | MULT | MAX | MIN | AND | OR ) )
            // wcps.g:392:4: op= ( PLUS | MULT | MAX | MIN | AND | OR )
            {
            root_0 = (Object)adaptor.nil();

            op=(Token)input.LT(1);
            if ( input.LA(1)==OR||input.LA(1)==AND||input.LA(1)==PLUS||input.LA(1)==MULT||(input.LA(1)>=MIN && input.LA(1)<=MAX) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(op));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            if ( state.backtracking==0 ) {
               retval.value = new CondenseOperation((op!=null?op.getText():null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 75, condenseOpType_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "condenseOpType"

    public static class fieldName_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "fieldName"
    // wcps.g:394:1: fieldName returns [String value] : name ;
    public final wcpsParser.fieldName_return fieldName() throws RecognitionException {
        wcpsParser.fieldName_return retval = new wcpsParser.fieldName_return();
        retval.start = input.LT(1);
        int fieldName_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.name_return name208 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 76) ) { return retval; }
            // wcps.g:395:2: ( name )
            // wcps.g:395:4: name
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_name_in_fieldName4208);
            name208=name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, name208.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new String((name208!=null?name208.value:null));
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 76, fieldName_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "fieldName"

    public static class constant_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constant"
    // wcps.g:397:1: constant returns [String value] : (e= ( STRING | BOOLEANCONSTANT | INTEGERCONSTANT | FLOATCONSTANT ) | e1= complexConstant );
    public final wcpsParser.constant_return constant() throws RecognitionException {
        wcpsParser.constant_return retval = new wcpsParser.constant_return();
        retval.start = input.LT(1);
        int constant_StartIndex = input.index();
        Object root_0 = null;

        Token e=null;
        wcpsParser.complexConstant_return e1 = null;


        Object e_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 77) ) { return retval; }
            // wcps.g:398:2: (e= ( STRING | BOOLEANCONSTANT | INTEGERCONSTANT | FLOATCONSTANT ) | e1= complexConstant )
            int alt64=2;
            int LA64_0 = input.LA(1);

            if ( (LA64_0==INTEGERCONSTANT||LA64_0==STRING||(LA64_0>=BOOLEANCONSTANT && LA64_0<=FLOATCONSTANT)) ) {
                alt64=1;
            }
            else if ( (LA64_0==LPAREN) ) {
                alt64=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 64, 0, input);

                throw nvae;
            }
            switch (alt64) {
                case 1 :
                    // wcps.g:398:4: e= ( STRING | BOOLEANCONSTANT | INTEGERCONSTANT | FLOATCONSTANT )
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)input.LT(1);
                    if ( input.LA(1)==INTEGERCONSTANT||input.LA(1)==STRING||(input.LA(1)>=BOOLEANCONSTANT && input.LA(1)<=FLOATCONSTANT) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(e));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    if ( state.backtracking==0 ) {
                       retval.value = (e!=null?e.getText():null); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:399:4: e1= complexConstant
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_complexConstant_in_constant4242);
                    e1=complexConstant();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = (e1!=null?e1.value:null); 
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 77, constant_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "constant"

    public static class complexConstant_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "complexConstant"
    // wcps.g:401:1: complexConstant returns [String value] : LPAREN re1= FLOATCONSTANT COMMA im1= FLOATCONSTANT RPAREN ;
    public final wcpsParser.complexConstant_return complexConstant() throws RecognitionException {
        wcpsParser.complexConstant_return retval = new wcpsParser.complexConstant_return();
        retval.start = input.LT(1);
        int complexConstant_StartIndex = input.index();
        Object root_0 = null;

        Token re1=null;
        Token im1=null;
        Token LPAREN209=null;
        Token COMMA210=null;
        Token RPAREN211=null;

        Object re1_tree=null;
        Object im1_tree=null;
        Object LPAREN209_tree=null;
        Object COMMA210_tree=null;
        Object RPAREN211_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 78) ) { return retval; }
            // wcps.g:402:2: ( LPAREN re1= FLOATCONSTANT COMMA im1= FLOATCONSTANT RPAREN )
            // wcps.g:402:4: LPAREN re1= FLOATCONSTANT COMMA im1= FLOATCONSTANT RPAREN
            {
            root_0 = (Object)adaptor.nil();

            LPAREN209=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_complexConstant4257); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN209_tree = (Object)adaptor.create(LPAREN209);
            adaptor.addChild(root_0, LPAREN209_tree);
            }
            re1=(Token)match(input,FLOATCONSTANT,FOLLOW_FLOATCONSTANT_in_complexConstant4261); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            re1_tree = (Object)adaptor.create(re1);
            adaptor.addChild(root_0, re1_tree);
            }
            COMMA210=(Token)match(input,COMMA,FOLLOW_COMMA_in_complexConstant4263); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA210_tree = (Object)adaptor.create(COMMA210);
            adaptor.addChild(root_0, COMMA210_tree);
            }
            im1=(Token)match(input,FLOATCONSTANT,FOLLOW_FLOATCONSTANT_in_complexConstant4267); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            im1_tree = (Object)adaptor.create(im1);
            adaptor.addChild(root_0, im1_tree);
            }
            RPAREN211=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_complexConstant4269); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN211_tree = (Object)adaptor.create(RPAREN211);
            adaptor.addChild(root_0, RPAREN211_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new String((re1!=null?re1.getText():null) +"+i"+(im1!=null?im1.getText():null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 78, complexConstant_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "complexConstant"

    public static class stringConstant_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "stringConstant"
    // wcps.g:404:1: stringConstant returns [String value] : s= STRING ;
    public final wcpsParser.stringConstant_return stringConstant() throws RecognitionException {
        wcpsParser.stringConstant_return retval = new wcpsParser.stringConstant_return();
        retval.start = input.LT(1);
        int stringConstant_StartIndex = input.index();
        Object root_0 = null;

        Token s=null;

        Object s_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 79) ) { return retval; }
            // wcps.g:405:2: (s= STRING )
            // wcps.g:405:4: s= STRING
            {
            root_0 = (Object)adaptor.nil();

            s=(Token)match(input,STRING,FOLLOW_STRING_in_stringConstant4286); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            s_tree = (Object)adaptor.create(s);
            adaptor.addChild(root_0, s_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = (s!=null?s.getText():null); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 79, stringConstant_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "stringConstant"

    public static class name_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "name"
    // wcps.g:407:1: name returns [String value] : var= ( NAME | STRING | INTEGERCONSTANT ) ;
    public final wcpsParser.name_return name() throws RecognitionException {
        wcpsParser.name_return retval = new wcpsParser.name_return();
        retval.start = input.LT(1);
        int name_StartIndex = input.index();
        Object root_0 = null;

        Token var=null;

        Object var_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 80) ) { return retval; }
            // wcps.g:408:2: (var= ( NAME | STRING | INTEGERCONSTANT ) )
            // wcps.g:408:4: var= ( NAME | STRING | INTEGERCONSTANT )
            {
            root_0 = (Object)adaptor.nil();

            var=(Token)input.LT(1);
            if ( input.LA(1)==INTEGERCONSTANT||input.LA(1)==STRING||input.LA(1)==NAME ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(var));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            if ( state.backtracking==0 ) {
               retval.value = (var!=null?var.getText():null); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 80, name_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "name"

    public static class crsName_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "crsName"
    // wcps.g:410:1: crsName returns [String value] : s= stringConstant ;
    public final wcpsParser.crsName_return crsName() throws RecognitionException {
        wcpsParser.crsName_return retval = new wcpsParser.crsName_return();
        retval.start = input.LT(1);
        int crsName_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.stringConstant_return s = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 81) ) { return retval; }
            // wcps.g:411:2: (s= stringConstant )
            // wcps.g:411:4: s= stringConstant
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_stringConstant_in_crsName4330);
            s=stringConstant();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, s.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (s!=null?s.value:null); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 81, crsName_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "crsName"

    public static class axisName_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "axisName"
    // wcps.g:413:1: axisName returns [String value] : type1= name ;
    public final wcpsParser.axisName_return axisName() throws RecognitionException {
        wcpsParser.axisName_return retval = new wcpsParser.axisName_return();
        retval.start = input.LT(1);
        int axisName_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.name_return type1 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 82) ) { return retval; }
            // wcps.g:414:2: (type1= name )
            // wcps.g:414:4: type1= name
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_name_in_axisName4347);
            type1=name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, type1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new String((type1!=null?type1.value:null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 82, axisName_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "axisName"

    public static class variableName_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "variableName"
    // wcps.g:416:1: variableName returns [String value] : var= VARIABLE_DOLLAR ;
    public final wcpsParser.variableName_return variableName() throws RecognitionException {
        wcpsParser.variableName_return retval = new wcpsParser.variableName_return();
        retval.start = input.LT(1);
        int variableName_StartIndex = input.index();
        Object root_0 = null;

        Token var=null;

        Object var_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 83) ) { return retval; }
            // wcps.g:417:2: (var= VARIABLE_DOLLAR )
            // wcps.g:417:4: var= VARIABLE_DOLLAR
            {
            root_0 = (Object)adaptor.nil();

            var=(Token)match(input,VARIABLE_DOLLAR,FOLLOW_VARIABLE_DOLLAR_in_variableName4364); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            var_tree = (Object)adaptor.create(var);
            adaptor.addChild(root_0, var_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new String((var!=null?var.getText():null)); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 83, variableName_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "variableName"

    public static class coverageVariable_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageVariable"
    // wcps.g:419:1: coverageVariable returns [String value] : var= NAME ;
    public final wcpsParser.coverageVariable_return coverageVariable() throws RecognitionException {
        wcpsParser.coverageVariable_return retval = new wcpsParser.coverageVariable_return();
        retval.start = input.LT(1);
        int coverageVariable_StartIndex = input.index();
        Object root_0 = null;

        Token var=null;

        Object var_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 84) ) { return retval; }
            // wcps.g:420:2: (var= NAME )
            // wcps.g:420:4: var= NAME
            {
            root_0 = (Object)adaptor.nil();

            var=(Token)match(input,NAME,FOLLOW_NAME_in_coverageVariable4381); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            var_tree = (Object)adaptor.create(var);
            adaptor.addChild(root_0, var_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = (var!=null?var.getText():null); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 84, coverageVariable_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageVariable"

    public static class coverageName_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "coverageName"
    // wcps.g:422:1: coverageName returns [String value] : name ;
    public final wcpsParser.coverageName_return coverageName() throws RecognitionException {
        wcpsParser.coverageName_return retval = new wcpsParser.coverageName_return();
        retval.start = input.LT(1);
        int coverageName_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.name_return name212 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 85) ) { return retval; }
            // wcps.g:423:2: ( name )
            // wcps.g:423:4: name
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_name_in_coverageName4396);
            name212=name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, name212.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (name212!=null?name212.value:null); 
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 85, coverageName_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "coverageName"

    // $ANTLR start synpred8_wcps
    public final void synpred8_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.coverageLogicTerm_return e2 = null;


        // wcps.g:72:10: (op= ( OR | XOR ) e2= coverageLogicTerm )
        // wcps.g:72:10: op= ( OR | XOR ) e2= coverageLogicTerm
        {
        op=(Token)input.LT(1);
        if ( (input.LA(1)>=OR && input.LA(1)<=XOR) ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }

        pushFollow(FOLLOW_coverageLogicTerm_in_synpred8_wcps374);
        e2=coverageLogicTerm();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_wcps

    // $ANTLR start synpred9_wcps
    public final void synpred9_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.coverageLogicFactor_return e2 = null;


        // wcps.g:76:10: (op= AND e2= coverageLogicFactor )
        // wcps.g:76:10: op= AND e2= coverageLogicFactor
        {
        op=(Token)match(input,AND,FOLLOW_AND_in_synpred9_wcps416); if (state.failed) return ;
        pushFollow(FOLLOW_coverageLogicFactor_in_synpred9_wcps420);
        e2=coverageLogicFactor();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_wcps

    // $ANTLR start synpred15_wcps
    public final void synpred15_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.coverageArithmeticExpr_return e2 = null;


        // wcps.g:80:10: (op= ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE ) e2= coverageArithmeticExpr )
        // wcps.g:80:10: op= ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE ) e2= coverageArithmeticExpr
        {
        op=(Token)input.LT(1);
        if ( (input.LA(1)>=EQUALS && input.LA(1)<=GTE) ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }

        pushFollow(FOLLOW_coverageArithmeticExpr_in_synpred15_wcps479);
        e2=coverageArithmeticExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred15_wcps

    // $ANTLR start synpred17_wcps
    public final void synpred17_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.coverageArithmeticTerm_return e2 = null;


        // wcps.g:84:10: (op= ( PLUS | MINUS ) e2= coverageArithmeticTerm )
        // wcps.g:84:10: op= ( PLUS | MINUS ) e2= coverageArithmeticTerm
        {
        op=(Token)input.LT(1);
        if ( (input.LA(1)>=PLUS && input.LA(1)<=MINUS) ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }

        pushFollow(FOLLOW_coverageArithmeticTerm_in_synpred17_wcps531);
        e2=coverageArithmeticTerm();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred17_wcps

    // $ANTLR start synpred19_wcps
    public final void synpred19_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.coverageArithmeticFactor_return e2 = null;


        // wcps.g:88:10: (op= ( MULT | DIVIDE ) e2= coverageArithmeticFactor )
        // wcps.g:88:10: op= ( MULT | DIVIDE ) e2= coverageArithmeticFactor
        {
        op=(Token)input.LT(1);
        if ( (input.LA(1)>=MULT && input.LA(1)<=DIVIDE) ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }

        pushFollow(FOLLOW_coverageArithmeticFactor_in_synpred19_wcps582);
        e2=coverageArithmeticFactor();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_wcps

    // $ANTLR start synpred20_wcps
    public final void synpred20_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.coverageValue_return e2 = null;


        // wcps.g:92:10: (op= OVERLAY e2= coverageValue )
        // wcps.g:92:10: op= OVERLAY e2= coverageValue
        {
        op=(Token)match(input,OVERLAY,FOLLOW_OVERLAY_in_synpred20_wcps625); if (state.failed) return ;
        pushFollow(FOLLOW_coverageValue_in_synpred20_wcps629);
        e2=coverageValue();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred20_wcps

    // $ANTLR start synpred21_wcps
    public final void synpred21_wcps_fragment() throws RecognitionException {   
        wcpsParser.subsetExpr_return e5 = null;


        // wcps.g:95:7: (e5= subsetExpr )
        // wcps.g:95:7: e5= subsetExpr
        {
        pushFollow(FOLLOW_subsetExpr_in_synpred21_wcps655);
        e5=subsetExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred21_wcps

    // $ANTLR start synpred22_wcps
    public final void synpred22_wcps_fragment() throws RecognitionException {   
        wcpsParser.unaryInducedExpr_return e2 = null;


        // wcps.g:96:7: (e2= unaryInducedExpr )
        // wcps.g:96:7: e2= unaryInducedExpr
        {
        pushFollow(FOLLOW_unaryInducedExpr_in_synpred22_wcps668);
        e2=unaryInducedExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred22_wcps

    // $ANTLR start synpred25_wcps
    public final void synpred25_wcps_fragment() throws RecognitionException {   
        wcpsParser.scalarExpr_return e2 = null;


        // wcps.g:102:7: (e2= scalarExpr )
        // wcps.g:102:7: e2= scalarExpr
        {
        pushFollow(FOLLOW_scalarExpr_in_synpred25_wcps727);
        e2=scalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred25_wcps

    // $ANTLR start synpred27_wcps
    public final void synpred27_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageExpr_return e7 = null;


        // wcps.g:104:7: ( LPAREN e7= coverageExpr RPAREN )
        // wcps.g:104:7: LPAREN e7= coverageExpr RPAREN
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred27_wcps749); if (state.failed) return ;
        pushFollow(FOLLOW_coverageExpr_in_synpred27_wcps753);
        e7=coverageExpr();

        state._fsp--;
        if (state.failed) return ;
        match(input,RPAREN,FOLLOW_RPAREN_in_synpred27_wcps755); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred27_wcps

    // $ANTLR start synpred28_wcps
    public final void synpred28_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageConstantExpr_return e3 = null;


        // wcps.g:105:7: (e3= coverageConstantExpr )
        // wcps.g:105:7: e3= coverageConstantExpr
        {
        pushFollow(FOLLOW_coverageConstantExpr_in_synpred28_wcps768);
        e3=coverageConstantExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred28_wcps

    // $ANTLR start synpred29_wcps
    public final void synpred29_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageConstructorExpr_return e4 = null;


        // wcps.g:106:7: (e4= coverageConstructorExpr )
        // wcps.g:106:7: e4= coverageConstructorExpr
        {
        pushFollow(FOLLOW_coverageConstructorExpr_in_synpred29_wcps780);
        e4=coverageConstructorExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred29_wcps

    // $ANTLR start synpred31_wcps
    public final void synpred31_wcps_fragment() throws RecognitionException {   
        wcpsParser.metaDataExpr_return e1 = null;


        // wcps.g:111:7: (e1= metaDataExpr )
        // wcps.g:111:7: e1= metaDataExpr
        {
        pushFollow(FOLLOW_metaDataExpr_in_synpred31_wcps830);
        e1=metaDataExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred31_wcps

    // $ANTLR start synpred32_wcps
    public final void synpred32_wcps_fragment() throws RecognitionException {   
        wcpsParser.condenseExpr_return e2 = null;


        // wcps.g:112:7: (e2= condenseExpr )
        // wcps.g:112:7: e2= condenseExpr
        {
        pushFollow(FOLLOW_condenseExpr_in_synpred32_wcps843);
        e2=condenseExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred32_wcps

    // $ANTLR start synpred33_wcps
    public final void synpred33_wcps_fragment() throws RecognitionException {   
        wcpsParser.booleanScalarExpr_return e3 = null;


        // wcps.g:113:7: (e3= booleanScalarExpr )
        // wcps.g:113:7: e3= booleanScalarExpr
        {
        pushFollow(FOLLOW_booleanScalarExpr_in_synpred33_wcps856);
        e3=booleanScalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred33_wcps

    // $ANTLR start synpred34_wcps
    public final void synpred34_wcps_fragment() throws RecognitionException {   
        wcpsParser.numericScalarExpr_return e4 = null;


        // wcps.g:114:7: (e4= numericScalarExpr )
        // wcps.g:114:7: e4= numericScalarExpr
        {
        pushFollow(FOLLOW_numericScalarExpr_in_synpred34_wcps870);
        e4=numericScalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred34_wcps

    // $ANTLR start synpred35_wcps
    public final void synpred35_wcps_fragment() throws RecognitionException {   
        wcpsParser.stringScalarExpr_return e5 = null;


        // wcps.g:115:7: (e5= stringScalarExpr )
        // wcps.g:115:7: e5= stringScalarExpr
        {
        pushFollow(FOLLOW_stringScalarExpr_in_synpred35_wcps883);
        e5=stringScalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred35_wcps

    // $ANTLR start synpred55_wcps
    public final void synpred55_wcps_fragment() throws RecognitionException {   
        wcpsParser.subsetExpr_return e5 = null;


        // wcps.g:164:7: (e5= subsetExpr )
        // wcps.g:164:7: e5= subsetExpr
        {
        pushFollow(FOLLOW_subsetExpr_in_synpred55_wcps1463);
        e5=subsetExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred55_wcps

    // $ANTLR start synpred56_wcps
    public final void synpred56_wcps_fragment() throws RecognitionException {   
        wcpsParser.unaryInducedExpr_return e2 = null;


        // wcps.g:165:7: (e2= unaryInducedExpr )
        // wcps.g:165:7: e2= unaryInducedExpr
        {
        pushFollow(FOLLOW_unaryInducedExpr_in_synpred56_wcps1476);
        e2=unaryInducedExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred56_wcps

    // $ANTLR start synpred59_wcps
    public final void synpred59_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageAtom_return e1 = null;


        // wcps.g:168:7: (e1= coverageAtom )
        // wcps.g:168:7: e1= coverageAtom
        {
        pushFollow(FOLLOW_coverageAtom_in_synpred59_wcps1512);
        e1=coverageAtom();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred59_wcps

    // $ANTLR start synpred60_wcps
    public final void synpred60_wcps_fragment() throws RecognitionException {   
        wcpsParser.scalarExpr_return e6 = null;


        // wcps.g:169:7: (e6= scalarExpr )
        // wcps.g:169:7: e6= scalarExpr
        {
        pushFollow(FOLLOW_scalarExpr_in_synpred60_wcps1524);
        e6=scalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred60_wcps

    // $ANTLR start synpred61_wcps
    public final void synpred61_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageVariable_return e7 = null;


        // wcps.g:170:7: (e7= coverageVariable )
        // wcps.g:170:7: e7= coverageVariable
        {
        pushFollow(FOLLOW_coverageVariable_in_synpred61_wcps1536);
        e7=coverageVariable();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred61_wcps

    // $ANTLR start synpred62_wcps
    public final void synpred62_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageConstantExpr_return e8 = null;


        // wcps.g:171:7: (e8= coverageConstantExpr )
        // wcps.g:171:7: e8= coverageConstantExpr
        {
        pushFollow(FOLLOW_coverageConstantExpr_in_synpred62_wcps1548);
        e8=coverageConstantExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred62_wcps

    // $ANTLR start synpred63_wcps
    public final void synpred63_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageConstructorExpr_return e9 = null;


        // wcps.g:172:7: (e9= coverageConstructorExpr )
        // wcps.g:172:7: e9= coverageConstructorExpr
        {
        pushFollow(FOLLOW_coverageConstructorExpr_in_synpred63_wcps1560);
        e9=coverageConstructorExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred63_wcps

    // $ANTLR start synpred64_wcps
    public final void synpred64_wcps_fragment() throws RecognitionException {   
        wcpsParser.setMetaDataExpr_return e10 = null;


        // wcps.g:173:7: (e10= setMetaDataExpr )
        // wcps.g:173:7: e10= setMetaDataExpr
        {
        pushFollow(FOLLOW_setMetaDataExpr_in_synpred64_wcps1573);
        e10=setMetaDataExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred64_wcps

    // $ANTLR start synpred81_wcps
    public final void synpred81_wcps_fragment() throws RecognitionException {   
        wcpsParser.fieldExpr_return e6 = null;


        // wcps.g:230:7: (e6= fieldExpr )
        // wcps.g:230:7: e6= fieldExpr
        {
        pushFollow(FOLLOW_fieldExpr_in_synpred81_wcps2269);
        e6=fieldExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred81_wcps

    // $ANTLR start synpred82_wcps
    public final void synpred82_wcps_fragment() throws RecognitionException {   
        wcpsParser.unaryArithmeticExpr_return e1 = null;


        // wcps.g:231:4: (e1= unaryArithmeticExpr )
        // wcps.g:231:4: e1= unaryArithmeticExpr
        {
        pushFollow(FOLLOW_unaryArithmeticExpr_in_synpred82_wcps2278);
        e1=unaryArithmeticExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred82_wcps

    // $ANTLR start synpred85_wcps
    public final void synpred85_wcps_fragment() throws RecognitionException {   
        wcpsParser.booleanExpr_return e4 = null;


        // wcps.g:234:7: (e4= booleanExpr )
        // wcps.g:234:7: e4= booleanExpr
        {
        pushFollow(FOLLOW_booleanExpr_in_synpred85_wcps2314);
        e4=booleanExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred85_wcps

    // $ANTLR start synpred111_wcps
    public final void synpred111_wcps_fragment() throws RecognitionException {   
        wcpsParser.trimExpr_return e1 = null;


        // wcps.g:276:4: (e1= trimExpr )
        // wcps.g:276:4: e1= trimExpr
        {
        pushFollow(FOLLOW_trimExpr_in_synpred111_wcps2834);
        e1=trimExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred111_wcps

    // $ANTLR start synpred112_wcps
    public final void synpred112_wcps_fragment() throws RecognitionException {   
        wcpsParser.sliceExpr_return e2 = null;


        // wcps.g:277:4: (e2= sliceExpr )
        // wcps.g:277:4: e2= sliceExpr
        {
        pushFollow(FOLLOW_sliceExpr_in_synpred112_wcps2843);
        e2=sliceExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred112_wcps

    // $ANTLR start synpred115_wcps
    public final void synpred115_wcps_fragment() throws RecognitionException {   
        wcpsParser.dimensionIntervalElement_return el1 = null;


        // wcps.g:287:32: (el1= dimensionIntervalElement )
        // wcps.g:287:32: el1= dimensionIntervalElement
        {
        pushFollow(FOLLOW_dimensionIntervalElement_in_synpred115_wcps2972);
        el1=dimensionIntervalElement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred115_wcps

    // $ANTLR start synpred116_wcps
    public final void synpred116_wcps_fragment() throws RecognitionException {   
        wcpsParser.dimensionIntervalElement_return el3 = null;


        // wcps.g:290:32: (el3= dimensionIntervalElement )
        // wcps.g:290:32: el3= dimensionIntervalElement
        {
        pushFollow(FOLLOW_dimensionIntervalElement_in_synpred116_wcps3081);
        el3=dimensionIntervalElement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred116_wcps

    // $ANTLR start synpred130_wcps
    public final void synpred130_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.booleanScalarTerm_return e2 = null;


        // wcps.g:315:8: (op= ( OR | XOR ) e2= booleanScalarTerm )
        // wcps.g:315:8: op= ( OR | XOR ) e2= booleanScalarTerm
        {
        op=(Token)input.LT(1);
        if ( (input.LA(1)>=OR && input.LA(1)<=XOR) ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }

        pushFollow(FOLLOW_booleanScalarTerm_in_synpred130_wcps3396);
        e2=booleanScalarTerm();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred130_wcps

    // $ANTLR start synpred131_wcps
    public final void synpred131_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.booleanScalarNegation_return e2 = null;


        // wcps.g:319:5: (op= AND e2= booleanScalarNegation )
        // wcps.g:319:5: op= AND e2= booleanScalarNegation
        {
        op=(Token)match(input,AND,FOLLOW_AND_in_synpred131_wcps3428); if (state.failed) return ;
        pushFollow(FOLLOW_booleanScalarNegation_in_synpred131_wcps3432);
        e2=booleanScalarNegation();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred131_wcps

    // $ANTLR start synpred133_wcps
    public final void synpred133_wcps_fragment() throws RecognitionException {   
        wcpsParser.booleanScalarExpr_return e1 = null;


        // wcps.g:326:4: ( LPAREN e1= booleanScalarExpr RPAREN )
        // wcps.g:326:4: LPAREN e1= booleanScalarExpr RPAREN
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred133_wcps3481); if (state.failed) return ;
        pushFollow(FOLLOW_booleanScalarExpr_in_synpred133_wcps3485);
        e1=booleanScalarExpr();

        state._fsp--;
        if (state.failed) return ;
        match(input,RPAREN,FOLLOW_RPAREN_in_synpred133_wcps3487); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred133_wcps

    // $ANTLR start synpred135_wcps
    public final void synpred135_wcps_fragment() throws RecognitionException {   
        wcpsParser.numericScalarExpr_return n1 = null;

        wcpsParser.compOp_return cop = null;

        wcpsParser.numericScalarExpr_return n2 = null;


        // wcps.g:328:4: (n1= numericScalarExpr cop= compOp n2= numericScalarExpr )
        // wcps.g:328:4: n1= numericScalarExpr cop= compOp n2= numericScalarExpr
        {
        pushFollow(FOLLOW_numericScalarExpr_in_synpred135_wcps3514);
        n1=numericScalarExpr();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_compOp_in_synpred135_wcps3518);
        cop=compOp();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_numericScalarExpr_in_synpred135_wcps3522);
        n2=numericScalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred135_wcps

    // $ANTLR start synpred137_wcps
    public final void synpred137_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.numericScalarTerm_return e2 = null;


        // wcps.g:333:5: (op= ( PLUS | MINUS ) e2= numericScalarTerm )
        // wcps.g:333:5: op= ( PLUS | MINUS ) e2= numericScalarTerm
        {
        op=(Token)input.LT(1);
        if ( (input.LA(1)>=PLUS && input.LA(1)<=MINUS) ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }

        pushFollow(FOLLOW_numericScalarTerm_in_synpred137_wcps3567);
        e2=numericScalarTerm();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred137_wcps

    // $ANTLR start synpred139_wcps
    public final void synpred139_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.numericScalarFactor_return e2 = null;


        // wcps.g:337:4: (op= ( MULT | DIVIDE ) e2= numericScalarFactor )
        // wcps.g:337:4: op= ( MULT | DIVIDE ) e2= numericScalarFactor
        {
        op=(Token)input.LT(1);
        if ( (input.LA(1)>=MULT && input.LA(1)<=DIVIDE) ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }

        pushFollow(FOLLOW_numericScalarFactor_in_synpred139_wcps3603);
        e2=numericScalarFactor();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred139_wcps

    // $ANTLR start synpred140_wcps
    public final void synpred140_wcps_fragment() throws RecognitionException {   
        wcpsParser.numericScalarExpr_return e1 = null;


        // wcps.g:340:7: ( LPAREN e1= numericScalarExpr RPAREN )
        // wcps.g:340:7: LPAREN e1= numericScalarExpr RPAREN
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred140_wcps3623); if (state.failed) return ;
        pushFollow(FOLLOW_numericScalarExpr_in_synpred140_wcps3627);
        e1=numericScalarExpr();

        state._fsp--;
        if (state.failed) return ;
        match(input,RPAREN,FOLLOW_RPAREN_in_synpred140_wcps3629); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred140_wcps

    // $ANTLR start synpred147_wcps
    public final void synpred147_wcps_fragment() throws RecognitionException {   
        wcpsParser.complexConstant_return e2 = null;


        // wcps.g:347:7: (e2= complexConstant )
        // wcps.g:347:7: e2= complexConstant
        {
        pushFollow(FOLLOW_complexConstant_in_synpred147_wcps3741);
        e2=complexConstant();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred147_wcps

    // $ANTLR start synpred156_wcps
    public final void synpred156_wcps_fragment() throws RecognitionException {   
        wcpsParser.scalarExpr_return e1 = null;

        wcpsParser.scalarExpr_return e2 = null;


        // wcps.g:368:7: (e1= scalarExpr COLON e2= scalarExpr )
        // wcps.g:368:7: e1= scalarExpr COLON e2= scalarExpr
        {
        pushFollow(FOLLOW_scalarExpr_in_synpred156_wcps3929);
        e1=scalarExpr();

        state._fsp--;
        if (state.failed) return ;
        match(input,COLON,FOLLOW_COLON_in_synpred156_wcps3931); if (state.failed) return ;
        pushFollow(FOLLOW_scalarExpr_in_synpred156_wcps3935);
        e2=scalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred156_wcps

    // Delegated rules

    public final boolean synpred56_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred56_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred130_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred130_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred9_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred9_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred15_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred15_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred17_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred17_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred19_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred19_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred32_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred32_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred31_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred31_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred147_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred147_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred21_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred21_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred139_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred139_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred115_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred115_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred111_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred111_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred64_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred64_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred63_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred63_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred59_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred59_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred133_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred133_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred20_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred20_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred60_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred60_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred85_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred85_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred81_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred81_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred55_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred55_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred33_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred33_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred27_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred27_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred22_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred22_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred28_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred28_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred29_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred29_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred61_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred61_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred135_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred135_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred25_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred25_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred112_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred112_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred131_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred131_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred35_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred35_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred140_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred140_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred82_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred82_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred156_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred156_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred34_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred34_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred8_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred8_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred137_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred137_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred116_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred116_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred62_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred62_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA4 dfa4 = new DFA4(this);
    protected DFA6 dfa6 = new DFA6(this);
    protected DFA7 dfa7 = new DFA7(this);
    protected DFA8 dfa8 = new DFA8(this);
    protected DFA9 dfa9 = new DFA9(this);
    protected DFA10 dfa10 = new DFA10(this);
    protected DFA11 dfa11 = new DFA11(this);
    protected DFA12 dfa12 = new DFA12(this);
    protected DFA13 dfa13 = new DFA13(this);
    protected DFA14 dfa14 = new DFA14(this);
    protected DFA22 dfa22 = new DFA22(this);
    protected DFA36 dfa36 = new DFA36(this);
    protected DFA44 dfa44 = new DFA44(this);
    protected DFA45 dfa45 = new DFA45(this);
    protected DFA49 dfa49 = new DFA49(this);
    protected DFA51 dfa51 = new DFA51(this);
    protected DFA52 dfa52 = new DFA52(this);
    protected DFA53 dfa53 = new DFA53(this);
    protected DFA54 dfa54 = new DFA54(this);
    protected DFA55 dfa55 = new DFA55(this);
    protected DFA56 dfa56 = new DFA56(this);
    protected DFA57 dfa57 = new DFA57(this);
    protected DFA61 dfa61 = new DFA61(this);
    static final String DFA4_eotS =
        "\30\uffff";
    static final String DFA4_eofS =
        "\30\uffff";
    static final String DFA4_minS =
        "\1\6\27\uffff";
    static final String DFA4_maxS =
        "\1\154\27\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\1\1\2\1\3\24\uffff";
    static final String DFA4_specialS =
        "\30\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\3\4\uffff\1\1\1\2\12\uffff\1\3\3\uffff\20\3\21\uffff\2\3"+
            "\16\uffff\1\3\1\uffff\3\3\20\uffff\2\3\11\uffff\1\3",
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
            "",
            "",
            ""
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "58:1: processingExpr returns [ProcessingExpr value] : (e1= encodedCoverageExpr | e2= storeExpr | e3= scalarExpr );";
        }
    }
    static final String DFA6_eotS =
        "\77\uffff";
    static final String DFA6_eofS =
        "\1\1\76\uffff";
    static final String DFA6_minS =
        "\1\7\10\uffff\1\0\65\uffff";
    static final String DFA6_maxS =
        "\1\140\10\uffff\1\0\65\uffff";
    static final String DFA6_acceptS =
        "\1\uffff\1\2\74\uffff\1\1";
    static final String DFA6_specialS =
        "\11\uffff\1\0\65\uffff}>";
    static final String[] DFA6_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\2\11\14\1\21\uffff\2\1\3\uffff\1\1"+
            "\7\uffff\1\1\30\uffff\1\1\15\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
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
            "",
            "",
            ""
    };

    static final short[] DFA6_eot = DFA.unpackEncodedString(DFA6_eotS);
    static final short[] DFA6_eof = DFA.unpackEncodedString(DFA6_eofS);
    static final char[] DFA6_min = DFA.unpackEncodedStringToUnsignedChars(DFA6_minS);
    static final char[] DFA6_max = DFA.unpackEncodedStringToUnsignedChars(DFA6_maxS);
    static final short[] DFA6_accept = DFA.unpackEncodedString(DFA6_acceptS);
    static final short[] DFA6_special = DFA.unpackEncodedString(DFA6_specialS);
    static final short[][] DFA6_transition;

    static {
        int numStates = DFA6_transitionS.length;
        DFA6_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA6_transition[i] = DFA.unpackEncodedString(DFA6_transitionS[i]);
        }
    }

    class DFA6 extends DFA {

        public DFA6(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 6;
            this.eot = DFA6_eot;
            this.eof = DFA6_eof;
            this.min = DFA6_min;
            this.max = DFA6_max;
            this.accept = DFA6_accept;
            this.special = DFA6_special;
            this.transition = DFA6_transition;
        }
        public String getDescription() {
            return "()* loopback of 72:9: (op= ( OR | XOR ) e2= coverageLogicTerm )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA6_9 = input.LA(1);

                         
                        int index6_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index6_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 6, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA7_eotS =
        "\77\uffff";
    static final String DFA7_eofS =
        "\1\1\76\uffff";
    static final String DFA7_minS =
        "\1\7\10\uffff\1\0\65\uffff";
    static final String DFA7_maxS =
        "\1\140\10\uffff\1\0\65\uffff";
    static final String DFA7_acceptS =
        "\1\uffff\1\2\74\uffff\1\1";
    static final String DFA7_specialS =
        "\11\uffff\1\0\65\uffff}>";
    static final String[] DFA7_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\2\1\1\11\13\1\21\uffff\2\1\3\uffff"+
            "\1\1\7\uffff\1\1\30\uffff\1\1\15\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
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
            "",
            "",
            ""
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "()* loopback of 76:9: (op= AND e2= coverageLogicFactor )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA7_9 = input.LA(1);

                         
                        int index7_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index7_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 7, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA8_eotS =
        "\u0107\uffff";
    static final String DFA8_eofS =
        "\1\2\u0106\uffff";
    static final String DFA8_minS =
        "\1\7\1\0\14\uffff\5\0\u00f4\uffff";
    static final String DFA8_maxS =
        "\1\140\1\0\14\uffff\5\0\u00f4\uffff";
    static final String DFA8_acceptS =
        "\2\uffff\1\2\73\uffff\1\1\u00c8\uffff";
    static final String DFA8_specialS =
        "\1\uffff\1\0\14\uffff\1\1\1\2\1\3\1\4\1\5\u00f4\uffff}>";
    static final String[] DFA8_transitionS = {
            "\2\2\1\uffff\1\2\2\uffff\3\2\1\1\1\16\1\17\1\20\1\21\1\22\5"+
            "\2\21\uffff\2\2\3\uffff\1\2\7\uffff\1\2\30\uffff\1\2\15\uffff"+
            "\1\2",
            "\1\uffff",
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
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            "",
            "",
            "",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        public String getDescription() {
            return "80:9: (op= ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE ) e2= coverageArithmeticExpr )?";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA8_1 = input.LA(1);

                         
                        int index8_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA8_14 = input.LA(1);

                         
                        int index8_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_14);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA8_15 = input.LA(1);

                         
                        int index8_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_15);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA8_16 = input.LA(1);

                         
                        int index8_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_16);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA8_17 = input.LA(1);

                         
                        int index8_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_17);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA8_18 = input.LA(1);

                         
                        int index8_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_18);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 8, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA9_eotS =
        "\77\uffff";
    static final String DFA9_eofS =
        "\1\1\76\uffff";
    static final String DFA9_minS =
        "\1\7\10\uffff\1\0\65\uffff";
    static final String DFA9_maxS =
        "\1\140\10\uffff\1\0\65\uffff";
    static final String DFA9_acceptS =
        "\1\uffff\1\2\74\uffff\1\1";
    static final String DFA9_specialS =
        "\11\uffff\1\0\65\uffff}>";
    static final String[] DFA9_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\11\1\2\11\3\1\21\uffff\2\1\3\uffff"+
            "\1\1\7\uffff\1\1\30\uffff\1\1\15\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
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
            "",
            "",
            ""
    };

    static final short[] DFA9_eot = DFA.unpackEncodedString(DFA9_eotS);
    static final short[] DFA9_eof = DFA.unpackEncodedString(DFA9_eofS);
    static final char[] DFA9_min = DFA.unpackEncodedStringToUnsignedChars(DFA9_minS);
    static final char[] DFA9_max = DFA.unpackEncodedStringToUnsignedChars(DFA9_maxS);
    static final short[] DFA9_accept = DFA.unpackEncodedString(DFA9_acceptS);
    static final short[] DFA9_special = DFA.unpackEncodedString(DFA9_specialS);
    static final short[][] DFA9_transition;

    static {
        int numStates = DFA9_transitionS.length;
        DFA9_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA9_transition[i] = DFA.unpackEncodedString(DFA9_transitionS[i]);
        }
    }

    class DFA9 extends DFA {

        public DFA9(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 9;
            this.eot = DFA9_eot;
            this.eof = DFA9_eof;
            this.min = DFA9_min;
            this.max = DFA9_max;
            this.accept = DFA9_accept;
            this.special = DFA9_special;
            this.transition = DFA9_transition;
        }
        public String getDescription() {
            return "()* loopback of 84:9: (op= ( PLUS | MINUS ) e2= coverageArithmeticTerm )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA9_9 = input.LA(1);

                         
                        int index9_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred17_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index9_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 9, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA10_eotS =
        "\77\uffff";
    static final String DFA10_eofS =
        "\1\1\76\uffff";
    static final String DFA10_minS =
        "\1\7\10\uffff\1\0\65\uffff";
    static final String DFA10_maxS =
        "\1\140\10\uffff\1\0\65\uffff";
    static final String DFA10_acceptS =
        "\1\uffff\1\2\74\uffff\1\1";
    static final String DFA10_specialS =
        "\11\uffff\1\0\65\uffff}>";
    static final String[] DFA10_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\13\1\2\11\1\1\21\uffff\2\1\3\uffff"+
            "\1\1\7\uffff\1\1\30\uffff\1\1\15\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
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
            "",
            "",
            ""
    };

    static final short[] DFA10_eot = DFA.unpackEncodedString(DFA10_eotS);
    static final short[] DFA10_eof = DFA.unpackEncodedString(DFA10_eofS);
    static final char[] DFA10_min = DFA.unpackEncodedStringToUnsignedChars(DFA10_minS);
    static final char[] DFA10_max = DFA.unpackEncodedStringToUnsignedChars(DFA10_maxS);
    static final short[] DFA10_accept = DFA.unpackEncodedString(DFA10_acceptS);
    static final short[] DFA10_special = DFA.unpackEncodedString(DFA10_specialS);
    static final short[][] DFA10_transition;

    static {
        int numStates = DFA10_transitionS.length;
        DFA10_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA10_transition[i] = DFA.unpackEncodedString(DFA10_transitionS[i]);
        }
    }

    class DFA10 extends DFA {

        public DFA10(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 10;
            this.eot = DFA10_eot;
            this.eof = DFA10_eof;
            this.min = DFA10_min;
            this.max = DFA10_max;
            this.accept = DFA10_accept;
            this.special = DFA10_special;
            this.transition = DFA10_transition;
        }
        public String getDescription() {
            return "()* loopback of 88:9: (op= ( MULT | DIVIDE ) e2= coverageArithmeticFactor )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA10_9 = input.LA(1);

                         
                        int index10_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred19_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index10_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 10, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA11_eotS =
        "\77\uffff";
    static final String DFA11_eofS =
        "\1\1\76\uffff";
    static final String DFA11_minS =
        "\1\7\10\uffff\1\0\65\uffff";
    static final String DFA11_maxS =
        "\1\140\10\uffff\1\0\65\uffff";
    static final String DFA11_acceptS =
        "\1\uffff\1\2\74\uffff\1\1";
    static final String DFA11_specialS =
        "\11\uffff\1\0\65\uffff}>";
    static final String[] DFA11_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\15\1\1\11\21\uffff\2\1\3\uffff\1\1"+
            "\7\uffff\1\1\30\uffff\1\1\15\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
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
            "",
            "",
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "()* loopback of 92:9: (op= OVERLAY e2= coverageValue )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA11_9 = input.LA(1);

                         
                        int index11_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred20_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index11_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 11, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA12_eotS =
        "\52\uffff";
    static final String DFA12_eofS =
        "\52\uffff";
    static final String DFA12_minS =
        "\1\6\36\0\13\uffff";
    static final String DFA12_maxS =
        "\1\154\36\0\13\uffff";
    static final String DFA12_acceptS =
        "\37\uffff\1\1\2\uffff\1\2\4\uffff\1\3\1\4\1\5";
    static final String DFA12_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31"+
        "\1\32\1\33\1\34\1\35\13\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\13\17\uffff\1\42\1\15\3\uffff\1\1\1\2\1\3\1\4\1\6\1\7\1\10"+
            "\1\5\7\11\1\12\3\uffff\1\27\4\uffff\1\30\1\31\1\32\1\33\1\34"+
            "\1\36\1\uffff\1\35\1\50\1\17\1\16\16\42\1\25\1\42\1\21\1\20"+
            "\1\14\1\47\2\uffff\3\37\12\uffff\1\24\1\22\10\uffff\1\26\1\23",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            return "94:1: coverageValue returns [CoverageExpr value] : (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA12_1 = input.LA(1);

                         
                        int index12_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA12_2 = input.LA(1);

                         
                        int index12_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA12_3 = input.LA(1);

                         
                        int index12_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA12_4 = input.LA(1);

                         
                        int index12_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA12_5 = input.LA(1);

                         
                        int index12_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA12_6 = input.LA(1);

                         
                        int index12_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA12_7 = input.LA(1);

                         
                        int index12_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA12_8 = input.LA(1);

                         
                        int index12_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA12_9 = input.LA(1);

                         
                        int index12_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA12_10 = input.LA(1);

                         
                        int index12_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA12_11 = input.LA(1);

                         
                        int index12_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA12_12 = input.LA(1);

                         
                        int index12_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA12_13 = input.LA(1);

                         
                        int index12_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA12_14 = input.LA(1);

                         
                        int index12_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA12_15 = input.LA(1);

                         
                        int index12_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA12_16 = input.LA(1);

                         
                        int index12_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA12_17 = input.LA(1);

                         
                        int index12_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA12_18 = input.LA(1);

                         
                        int index12_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA12_19 = input.LA(1);

                         
                        int index12_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_19);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA12_20 = input.LA(1);

                         
                        int index12_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_20);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA12_21 = input.LA(1);

                         
                        int index12_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_21);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA12_22 = input.LA(1);

                         
                        int index12_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_22);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA12_23 = input.LA(1);

                         
                        int index12_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_23);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA12_24 = input.LA(1);

                         
                        int index12_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_24);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA12_25 = input.LA(1);

                         
                        int index12_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_25);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA12_26 = input.LA(1);

                         
                        int index12_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_26);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA12_27 = input.LA(1);

                         
                        int index12_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_27);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA12_28 = input.LA(1);

                         
                        int index12_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_28);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA12_29 = input.LA(1);

                         
                        int index12_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_29);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA12_30 = input.LA(1);

                         
                        int index12_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 31;}

                        else if ( (synpred22_wcps()) ) {s = 34;}

                        else if ( (true) ) {s = 41;}

                         
                        input.seek(index12_30);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 12, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA13_eotS =
        "\112\uffff";
    static final String DFA13_eofS =
        "\112\uffff";
    static final String DFA13_minS =
        "\1\6\12\uffff\1\6\13\uffff\1\116\7\uffff\25\0\23\uffff\1\0\2\uffff";
    static final String DFA13_maxS =
        "\1\154\12\uffff\1\154\13\uffff\1\153\7\uffff\25\0\23\uffff\1\0\2"+
        "\uffff";
    static final String DFA13_acceptS =
        "\1\uffff\1\1\24\uffff\1\2\1\uffff\1\6\4\uffff\1\7\26\uffff\1\3\23"+
        "\uffff\1\4\1\5";
    static final String DFA13_specialS =
        "\37\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\23\uffff\1\25\2\uffff}>";
    static final String[] DFA13_transitionS = {
            "\1\13\20\uffff\1\1\3\uffff\20\1\3\uffff\1\27\4\uffff\5\30\1"+
            "\35\1\uffff\1\35\1\uffff\2\1\16\uffff\1\1\1\uffff\3\1\20\uffff"+
            "\2\1\10\uffff\1\26\1\1",
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
            "\1\52\17\uffff\1\64\1\54\3\uffff\1\40\1\41\1\42\1\43\1\45\1"+
            "\46\1\47\1\44\7\50\1\51\3\uffff\1\64\4\uffff\6\64\1\uffff\2"+
            "\64\1\56\1\55\16\64\1\63\1\64\1\60\1\57\1\53\1\64\2\uffff\3"+
            "\64\12\uffff\1\62\1\37\10\uffff\1\64\1\61",
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
            "",
            "\1\107\1\uffff\1\107\32\uffff\1\107",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA13_eot = DFA.unpackEncodedString(DFA13_eotS);
    static final short[] DFA13_eof = DFA.unpackEncodedString(DFA13_eofS);
    static final char[] DFA13_min = DFA.unpackEncodedStringToUnsignedChars(DFA13_minS);
    static final char[] DFA13_max = DFA.unpackEncodedStringToUnsignedChars(DFA13_maxS);
    static final short[] DFA13_accept = DFA.unpackEncodedString(DFA13_acceptS);
    static final short[] DFA13_special = DFA.unpackEncodedString(DFA13_specialS);
    static final short[][] DFA13_transition;

    static {
        int numStates = DFA13_transitionS.length;
        DFA13_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA13_transition[i] = DFA.unpackEncodedString(DFA13_transitionS[i]);
        }
    }

    class DFA13 extends DFA {

        public DFA13(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 13;
            this.eot = DFA13_eot;
            this.eof = DFA13_eof;
            this.min = DFA13_min;
            this.max = DFA13_max;
            this.accept = DFA13_accept;
            this.special = DFA13_special;
            this.transition = DFA13_transition;
        }
        public String getDescription() {
            return "101:1: coverageAtom returns [CoverageExpr value] : (e2= scalarExpr | e1= coverageVariable | LPAREN e7= coverageExpr RPAREN | e3= coverageConstantExpr | e4= coverageConstructorExpr | e5= setMetaDataExpr | e6= rangeConstructorExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA13_31 = input.LA(1);

                         
                        int index13_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_31);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA13_32 = input.LA(1);

                         
                        int index13_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_32);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA13_33 = input.LA(1);

                         
                        int index13_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_33);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA13_34 = input.LA(1);

                         
                        int index13_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_34);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA13_35 = input.LA(1);

                         
                        int index13_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_35);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA13_36 = input.LA(1);

                         
                        int index13_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_36);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA13_37 = input.LA(1);

                         
                        int index13_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_37);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA13_38 = input.LA(1);

                         
                        int index13_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_38);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA13_39 = input.LA(1);

                         
                        int index13_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_39);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA13_40 = input.LA(1);

                         
                        int index13_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_40);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA13_41 = input.LA(1);

                         
                        int index13_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_41);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA13_42 = input.LA(1);

                         
                        int index13_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_42);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA13_43 = input.LA(1);

                         
                        int index13_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_43);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA13_44 = input.LA(1);

                         
                        int index13_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_44);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA13_45 = input.LA(1);

                         
                        int index13_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_45);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA13_46 = input.LA(1);

                         
                        int index13_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_46);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA13_47 = input.LA(1);

                         
                        int index13_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_47);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA13_48 = input.LA(1);

                         
                        int index13_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_48);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA13_49 = input.LA(1);

                         
                        int index13_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_49);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA13_50 = input.LA(1);

                         
                        int index13_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_50);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA13_51 = input.LA(1);

                         
                        int index13_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred25_wcps()) ) {s = 1;}

                        else if ( (synpred27_wcps()) ) {s = 52;}

                         
                        input.seek(index13_51);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA13_71 = input.LA(1);

                         
                        int index13_71 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred28_wcps()) ) {s = 72;}

                        else if ( (synpred29_wcps()) ) {s = 73;}

                         
                        input.seek(index13_71);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 13, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA14_eotS =
        "\u0091\uffff";
    static final String DFA14_eofS =
        "\14\uffff\1\64\u0084\uffff";
    static final String DFA14_minS =
        "\2\6\7\uffff\1\6\1\15\1\6\1\7\4\6\3\0\2\uffff\21\0\7\uffff\6\0\17"+
        "\uffff\15\0\101\uffff";
    static final String DFA14_maxS =
        "\1\154\1\6\7\uffff\1\6\1\51\1\154\1\140\1\154\3\6\3\0\2\uffff\21"+
        "\0\7\uffff\6\0\17\uffff\15\0\101\uffff";
    static final String DFA14_acceptS =
        "\2\uffff\1\1\21\uffff\1\3\22\uffff\1\6\14\uffff\1\5\60\uffff\1\4"+
        "\52\uffff\1\2";
    static final String DFA14_specialS =
        "\21\uffff\1\0\1\1\1\2\2\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12"+
        "\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\7\uffff\1\24\1\25"+
        "\1\26\1\27\1\30\1\31\17\uffff\1\32\1\33\1\34\1\35\1\36\1\37\1\40"+
        "\1\41\1\42\1\43\1\44\1\45\1\46\101\uffff}>";
    static final String[] DFA14_transitionS = {
            "\1\13\20\uffff\1\15\3\uffff\1\1\7\2\7\11\1\12\21\uffff\1\17"+
            "\1\16\16\uffff\1\24\1\uffff\1\21\1\20\1\14\20\uffff\1\24\1\22"+
            "\11\uffff\1\23",
            "\1\26",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\27",
            "\1\30\1\uffff\1\30\6\uffff\1\30\1\uffff\1\30\17\uffff\2\30",
            "\1\32\20\uffff\1\33\3\uffff\1\43\7\47\7\40\1\41\21\uffff\1"+
            "\35\1\34\16\uffff\1\46\1\uffff\1\37\1\36\1\44\20\uffff\1\45"+
            "\1\31\11\uffff\1\42",
            "\2\64\1\uffff\1\64\2\uffff\3\64\1\56\1\57\1\60\1\61\1\62\1"+
            "\63\5\64\21\uffff\2\64\3\uffff\1\64\7\uffff\1\64\30\uffff\1"+
            "\64\15\uffff\1\64",
            "\1\103\20\uffff\1\104\13\uffff\7\112\1\113\21\uffff\1\106\1"+
            "\105\20\uffff\1\110\1\107\22\uffff\1\111\11\uffff\1\114",
            "\1\115",
            "\1\116",
            "\1\117",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        public String getDescription() {
            return "110:1: scalarExpr returns [ScalarExpr value] : (e1= metaDataExpr | e2= condenseExpr | e3= booleanScalarExpr | e4= numericScalarExpr | e5= stringScalarExpr | LPAREN e6= scalarExpr RPAREN );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA14_17 = input.LA(1);

                         
                        int index14_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_17);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA14_18 = input.LA(1);

                         
                        int index14_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_18);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA14_19 = input.LA(1);

                         
                        int index14_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_19);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA14_22 = input.LA(1);

                         
                        int index14_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred31_wcps()) ) {s = 2;}

                        else if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred35_wcps()) ) {s = 52;}

                         
                        input.seek(index14_22);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA14_23 = input.LA(1);

                         
                        int index14_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred32_wcps()) ) {s = 144;}

                        else if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_23);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA14_24 = input.LA(1);

                         
                        int index14_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred32_wcps()) ) {s = 144;}

                        else if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_24);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA14_25 = input.LA(1);

                         
                        int index14_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_25);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA14_26 = input.LA(1);

                         
                        int index14_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_26);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA14_27 = input.LA(1);

                         
                        int index14_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_27);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA14_28 = input.LA(1);

                         
                        int index14_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_28);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA14_29 = input.LA(1);

                         
                        int index14_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_29);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA14_30 = input.LA(1);

                         
                        int index14_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_30);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA14_31 = input.LA(1);

                         
                        int index14_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_31);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA14_32 = input.LA(1);

                         
                        int index14_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_32);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA14_33 = input.LA(1);

                         
                        int index14_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_33);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA14_34 = input.LA(1);

                         
                        int index14_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_34);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA14_35 = input.LA(1);

                         
                        int index14_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_35);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA14_36 = input.LA(1);

                         
                        int index14_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_36);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA14_37 = input.LA(1);

                         
                        int index14_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_37);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA14_38 = input.LA(1);

                         
                        int index14_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (true) ) {s = 39;}

                         
                        input.seek(index14_38);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA14_46 = input.LA(1);

                         
                        int index14_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred35_wcps()) ) {s = 52;}

                         
                        input.seek(index14_46);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA14_47 = input.LA(1);

                         
                        int index14_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred35_wcps()) ) {s = 52;}

                         
                        input.seek(index14_47);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA14_48 = input.LA(1);

                         
                        int index14_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred35_wcps()) ) {s = 52;}

                         
                        input.seek(index14_48);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA14_49 = input.LA(1);

                         
                        int index14_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred35_wcps()) ) {s = 52;}

                         
                        input.seek(index14_49);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA14_50 = input.LA(1);

                         
                        int index14_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred35_wcps()) ) {s = 52;}

                         
                        input.seek(index14_50);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA14_51 = input.LA(1);

                         
                        int index14_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred35_wcps()) ) {s = 52;}

                         
                        input.seek(index14_51);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA14_67 = input.LA(1);

                         
                        int index14_67 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_67);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA14_68 = input.LA(1);

                         
                        int index14_68 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_68);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA14_69 = input.LA(1);

                         
                        int index14_69 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_69);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA14_70 = input.LA(1);

                         
                        int index14_70 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_70);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA14_71 = input.LA(1);

                         
                        int index14_71 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_71);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA14_72 = input.LA(1);

                         
                        int index14_72 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_72);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA14_73 = input.LA(1);

                         
                        int index14_73 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_73);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA14_74 = input.LA(1);

                         
                        int index14_74 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_74);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA14_75 = input.LA(1);

                         
                        int index14_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_75);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA14_76 = input.LA(1);

                         
                        int index14_76 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_76);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA14_77 = input.LA(1);

                         
                        int index14_77 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_77);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA14_78 = input.LA(1);

                         
                        int index14_78 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_78);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA14_79 = input.LA(1);

                         
                        int index14_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 20;}

                        else if ( (synpred34_wcps()) ) {s = 101;}

                         
                        input.seek(index14_79);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 14, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA22_eotS =
        "\60\uffff";
    static final String DFA22_eofS =
        "\60\uffff";
    static final String DFA22_minS =
        "\1\6\36\0\21\uffff";
    static final String DFA22_maxS =
        "\1\154\36\0\21\uffff";
    static final String DFA22_acceptS =
        "\37\uffff\1\1\2\uffff\1\2\4\uffff\1\3\1\4\1\5\1\6\1\7\1\10\1\11"+
        "\1\12\1\13";
    static final String DFA22_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31"+
        "\1\32\1\33\1\34\1\35\21\uffff}>";
    static final String[] DFA22_transitionS = {
            "\1\13\17\uffff\1\42\1\15\3\uffff\1\1\1\2\1\3\1\4\1\6\1\7\1\10"+
            "\1\5\7\11\1\12\3\uffff\1\27\4\uffff\1\30\1\31\1\32\1\33\1\34"+
            "\1\36\1\uffff\1\35\1\50\1\17\1\16\16\42\1\25\1\42\1\21\1\20"+
            "\1\14\1\47\2\uffff\3\37\12\uffff\1\24\1\22\10\uffff\1\26\1\23",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA22_eot = DFA.unpackEncodedString(DFA22_eotS);
    static final short[] DFA22_eof = DFA.unpackEncodedString(DFA22_eofS);
    static final char[] DFA22_min = DFA.unpackEncodedStringToUnsignedChars(DFA22_minS);
    static final char[] DFA22_max = DFA.unpackEncodedStringToUnsignedChars(DFA22_maxS);
    static final short[] DFA22_accept = DFA.unpackEncodedString(DFA22_acceptS);
    static final short[] DFA22_special = DFA.unpackEncodedString(DFA22_specialS);
    static final short[][] DFA22_transition;

    static {
        int numStates = DFA22_transitionS.length;
        DFA22_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA22_transition[i] = DFA.unpackEncodedString(DFA22_transitionS[i]);
        }
    }

    class DFA22 extends DFA {

        public DFA22(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 22;
            this.eot = DFA22_eot;
            this.eof = DFA22_eof;
            this.min = DFA22_min;
            this.max = DFA22_max;
            this.accept = DFA22_accept;
            this.special = DFA22_special;
            this.transition = DFA22_transition;
        }
        public String getDescription() {
            return "163:1: coverageAtomConstructor returns [CoverageExpr value] : (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom | e6= scalarExpr | e7= coverageVariable | e8= coverageConstantExpr | e9= coverageConstructorExpr | e10= setMetaDataExpr | e11= rangeConstructorExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA22_1 = input.LA(1);

                         
                        int index22_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA22_2 = input.LA(1);

                         
                        int index22_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA22_3 = input.LA(1);

                         
                        int index22_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA22_4 = input.LA(1);

                         
                        int index22_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA22_5 = input.LA(1);

                         
                        int index22_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA22_6 = input.LA(1);

                         
                        int index22_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA22_7 = input.LA(1);

                         
                        int index22_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA22_8 = input.LA(1);

                         
                        int index22_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA22_9 = input.LA(1);

                         
                        int index22_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA22_10 = input.LA(1);

                         
                        int index22_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA22_11 = input.LA(1);

                         
                        int index22_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA22_12 = input.LA(1);

                         
                        int index22_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA22_13 = input.LA(1);

                         
                        int index22_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA22_14 = input.LA(1);

                         
                        int index22_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA22_15 = input.LA(1);

                         
                        int index22_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA22_16 = input.LA(1);

                         
                        int index22_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA22_17 = input.LA(1);

                         
                        int index22_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA22_18 = input.LA(1);

                         
                        int index22_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA22_19 = input.LA(1);

                         
                        int index22_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_19);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA22_20 = input.LA(1);

                         
                        int index22_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_20);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA22_21 = input.LA(1);

                         
                        int index22_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred60_wcps()) ) {s = 42;}

                         
                        input.seek(index22_21);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA22_22 = input.LA(1);

                         
                        int index22_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred61_wcps()) ) {s = 43;}

                         
                        input.seek(index22_22);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA22_23 = input.LA(1);

                         
                        int index22_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred62_wcps()) ) {s = 44;}

                        else if ( (synpred63_wcps()) ) {s = 45;}

                         
                        input.seek(index22_23);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA22_24 = input.LA(1);

                         
                        int index22_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred64_wcps()) ) {s = 46;}

                         
                        input.seek(index22_24);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA22_25 = input.LA(1);

                         
                        int index22_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred64_wcps()) ) {s = 46;}

                         
                        input.seek(index22_25);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA22_26 = input.LA(1);

                         
                        int index22_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred64_wcps()) ) {s = 46;}

                         
                        input.seek(index22_26);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA22_27 = input.LA(1);

                         
                        int index22_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred64_wcps()) ) {s = 46;}

                         
                        input.seek(index22_27);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA22_28 = input.LA(1);

                         
                        int index22_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (synpred64_wcps()) ) {s = 46;}

                         
                        input.seek(index22_28);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA22_29 = input.LA(1);

                         
                        int index22_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index22_29);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA22_30 = input.LA(1);

                         
                        int index22_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred55_wcps()) ) {s = 31;}

                        else if ( (synpred56_wcps()) ) {s = 34;}

                        else if ( (synpred59_wcps()) ) {s = 41;}

                        else if ( (true) ) {s = 47;}

                         
                        input.seek(index22_30);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 22, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA36_eotS =
        "\u0099\uffff";
    static final String DFA36_eofS =
        "\u0099\uffff";
    static final String DFA36_minS =
        "\1\6\12\uffff\1\6\1\uffff\3\6\5\uffff\1\6\7\uffff\1\70\1\116\57"+
        "\uffff\12\0\24\uffff\17\0\33\uffff\2\0\1\uffff";
    static final String DFA36_maxS =
        "\1\154\12\uffff\1\154\1\uffff\1\154\2\6\5\uffff\1\154\7\uffff\1"+
        "\70\1\153\57\uffff\12\0\24\uffff\17\0\33\uffff\2\0\1\uffff";
    static final String DFA36_acceptS =
        "\1\uffff\1\1\35\uffff\1\2\1\uffff\1\3\1\4\1\5\50\uffff\1\6\113\uffff"+
        "\1\7";
    static final String DFA36_specialS =
        "\116\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\24\uffff\1"+
        "\12\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26"+
        "\1\27\1\30\33\uffff\1\31\1\32\1\uffff}>";
    static final String[] DFA36_transitionS = {
            "\1\13\17\uffff\1\37\1\15\3\uffff\20\1\3\uffff\1\1\4\uffff\5"+
            "\1\1\36\1\uffff\1\35\1\uffff\1\17\1\16\2\37\3\41\11\42\1\25"+
            "\1\43\3\1\20\uffff\2\1\10\uffff\2\1",
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
            "\1\1\17\uffff\2\1\3\uffff\20\1\3\uffff\1\1\4\uffff\6\1\1\uffff"+
            "\30\1\2\uffff\3\1\11\114\1\uffff\2\1\10\uffff\2\1",
            "",
            "\1\116\20\uffff\1\117\3\uffff\10\37\7\125\1\126\3\uffff\1\37"+
            "\4\uffff\6\37\1\uffff\1\37\1\uffff\1\121\1\120\16\uffff\1\37"+
            "\1\uffff\1\123\1\122\1\37\20\uffff\1\37\1\124\10\uffff\1\37"+
            "\1\127",
            "\1\154",
            "\1\155",
            "",
            "",
            "",
            "",
            "",
            "\1\156\17\uffff\1\43\1\161\3\uffff\1\157\7\43\7\167\1\170\3"+
            "\uffff\1\43\4\uffff\6\43\1\uffff\2\43\1\163\1\162\20\43\1\165"+
            "\1\164\1\160\1\43\2\uffff\3\43\12\uffff\1\172\1\166\10\uffff"+
            "\1\43\1\171",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u0096",
            "\1\u0097\1\uffff\1\u0097\32\uffff\1\u0097",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            ""
    };

    static final short[] DFA36_eot = DFA.unpackEncodedString(DFA36_eotS);
    static final short[] DFA36_eof = DFA.unpackEncodedString(DFA36_eofS);
    static final char[] DFA36_min = DFA.unpackEncodedStringToUnsignedChars(DFA36_minS);
    static final char[] DFA36_max = DFA.unpackEncodedStringToUnsignedChars(DFA36_maxS);
    static final short[] DFA36_accept = DFA.unpackEncodedString(DFA36_acceptS);
    static final short[] DFA36_special = DFA.unpackEncodedString(DFA36_specialS);
    static final short[][] DFA36_transition;

    static {
        int numStates = DFA36_transitionS.length;
        DFA36_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA36_transition[i] = DFA.unpackEncodedString(DFA36_transitionS[i]);
        }
    }

    class DFA36 extends DFA {

        public DFA36(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 36;
            this.eot = DFA36_eot;
            this.eof = DFA36_eof;
            this.min = DFA36_min;
            this.max = DFA36_max;
            this.accept = DFA36_accept;
            this.special = DFA36_special;
            this.transition = DFA36_transition;
        }
        public String getDescription() {
            return "229:1: unaryInducedExpr returns [CoverageExpr value] : (e6= fieldExpr | e1= unaryArithmeticExpr | e2= exponentialExpr | e3= trigonometricExpr | e4= booleanExpr | e5= castExpr | e7= rangeConstructorExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA36_78 = input.LA(1);

                         
                        int index36_78 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_78);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA36_79 = input.LA(1);

                         
                        int index36_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_79);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA36_80 = input.LA(1);

                         
                        int index36_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_80);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA36_81 = input.LA(1);

                         
                        int index36_81 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_81);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA36_82 = input.LA(1);

                         
                        int index36_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_82);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA36_83 = input.LA(1);

                         
                        int index36_83 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_83);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA36_84 = input.LA(1);

                         
                        int index36_84 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_84);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA36_85 = input.LA(1);

                         
                        int index36_85 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_85);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA36_86 = input.LA(1);

                         
                        int index36_86 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_86);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA36_87 = input.LA(1);

                         
                        int index36_87 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_87);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA36_108 = input.LA(1);

                         
                        int index36_108 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_108);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA36_109 = input.LA(1);

                         
                        int index36_109 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred82_wcps()) ) {s = 31;}

                         
                        input.seek(index36_109);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA36_110 = input.LA(1);

                         
                        int index36_110 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_110);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA36_111 = input.LA(1);

                         
                        int index36_111 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_111);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA36_112 = input.LA(1);

                         
                        int index36_112 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_112);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA36_113 = input.LA(1);

                         
                        int index36_113 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_113);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA36_114 = input.LA(1);

                         
                        int index36_114 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_114);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA36_115 = input.LA(1);

                         
                        int index36_115 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_115);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA36_116 = input.LA(1);

                         
                        int index36_116 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_116);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA36_117 = input.LA(1);

                         
                        int index36_117 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_117);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA36_118 = input.LA(1);

                         
                        int index36_118 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_118);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA36_119 = input.LA(1);

                         
                        int index36_119 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_119);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA36_120 = input.LA(1);

                         
                        int index36_120 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_120);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA36_121 = input.LA(1);

                         
                        int index36_121 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_121);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA36_122 = input.LA(1);

                         
                        int index36_122 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (synpred85_wcps()) ) {s = 35;}

                         
                        input.seek(index36_122);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA36_150 = input.LA(1);

                         
                        int index36_150 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (true) ) {s = 152;}

                         
                        input.seek(index36_150);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA36_151 = input.LA(1);

                         
                        int index36_151 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred81_wcps()) ) {s = 1;}

                        else if ( (true) ) {s = 152;}

                         
                        input.seek(index36_151);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 36, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA44_eotS =
        "\u009d\uffff";
    static final String DFA44_eofS =
        "\u009d\uffff";
    static final String DFA44_minS =
        "\12\6\1\15\1\6\1\20\4\6\3\20\1\15\1\6\1\122\1\116\5\6\1\70\1\116"+
        "\3\uffff\172\0\1\uffff";
    static final String DFA44_maxS =
        "\1\154\11\6\1\51\1\154\1\122\1\154\3\6\4\122\1\154\1\122\1\153\5"+
        "\6\1\70\1\153\3\uffff\172\0\1\uffff";
    static final String DFA44_acceptS =
        "\37\uffff\1\1\1\2\1\3\172\uffff\1\4";
    static final String DFA44_specialS =
        "\42\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30"+
        "\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45"+
        "\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62"+
        "\1\63\1\64\1\65\1\66\1\67\1\70\1\71\1\72\1\73\1\74\1\75\1\76\1\77"+
        "\1\100\1\101\1\102\1\103\1\104\1\105\1\106\1\107\1\110\1\111\1\112"+
        "\1\113\1\114\1\115\1\116\1\117\1\120\1\121\1\122\1\123\1\124\1\125"+
        "\1\126\1\127\1\130\1\131\1\132\1\133\1\134\1\135\1\136\1\137\1\140"+
        "\1\141\1\142\1\143\1\144\1\145\1\146\1\147\1\150\1\151\1\152\1\153"+
        "\1\154\1\155\1\156\1\157\1\160\1\161\1\162\1\163\1\164\1\165\1\166"+
        "\1\167\1\170\1\171\1\uffff}>";
    static final String[] DFA44_transitionS = {
            "\1\13\20\uffff\1\15\3\uffff\1\1\1\2\1\3\1\4\1\6\1\7\1\10\1\5"+
            "\7\11\1\12\3\uffff\1\27\4\uffff\1\30\1\31\1\32\1\33\1\34\1\36"+
            "\1\uffff\1\35\1\uffff\1\17\1\16\16\uffff\1\25\1\uffff\1\21\1"+
            "\20\1\14\3\uffff\1\37\1\40\1\41\12\uffff\1\24\1\22\10\uffff"+
            "\1\26\1\23",
            "\1\42",
            "\1\43",
            "\1\44",
            "\1\45",
            "\1\46",
            "\1\47",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\53\1\uffff\1\53\6\uffff\1\53\1\uffff\1\53\17\uffff\2\53",
            "\1\55\17\uffff\1\115\1\56\3\uffff\1\66\1\72\1\73\1\74\1\76"+
            "\1\77\1\100\1\75\7\63\1\64\3\uffff\1\102\4\uffff\1\103\1\104"+
            "\1\105\1\106\1\107\1\111\1\uffff\1\110\1\123\1\60\1\57\2\116"+
            "\3\117\11\120\1\71\1\121\1\62\1\61\1\67\1\122\2\uffff\1\112"+
            "\1\113\1\114\12\uffff\1\70\1\54\10\uffff\1\101\1\65",
            "\1\125\1\126\1\127\1\130\1\131\1\132\74\uffff\1\124",
            "\1\133\20\uffff\1\134\13\uffff\7\142\1\143\21\uffff\1\136\1"+
            "\135\20\uffff\1\140\1\137\22\uffff\1\141\11\uffff\1\144",
            "\1\145",
            "\1\146",
            "\1\147",
            "\1\152\1\153\1\154\1\155\1\156\1\157\2\151\2\150\70\uffff\1"+
            "\160",
            "\1\163\1\164\1\165\1\166\1\167\1\170\2\162\2\161\70\uffff\1"+
            "\171",
            "\1\175\1\176\1\177\1\u0080\1\u0081\1\u0082\2\173\2\172\70\uffff"+
            "\1\174",
            "\2\u0084\1\u0083\102\uffff\1\u0085",
            "\1\u0086\20\uffff\1\u0089\3\uffff\1\u0087\7\uffff\7\u008f\1"+
            "\u0090\21\uffff\1\u008b\1\u008a\20\uffff\1\u008d\1\u008c\1\u0088"+
            "\20\uffff\1\u0092\1\u008e\11\uffff\1\u0091",
            "\1\u0093",
            "\1\u0094\1\uffff\1\u0094\32\uffff\1\u0094",
            "\1\u0095",
            "\1\u0096",
            "\1\u0097",
            "\1\u0098",
            "\1\u0099",
            "\1\u009a",
            "\1\u009b\1\uffff\1\u009b\32\uffff\1\u009b",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            ""
    };

    static final short[] DFA44_eot = DFA.unpackEncodedString(DFA44_eotS);
    static final short[] DFA44_eof = DFA.unpackEncodedString(DFA44_eofS);
    static final char[] DFA44_min = DFA.unpackEncodedStringToUnsignedChars(DFA44_minS);
    static final char[] DFA44_max = DFA.unpackEncodedStringToUnsignedChars(DFA44_maxS);
    static final short[] DFA44_accept = DFA.unpackEncodedString(DFA44_acceptS);
    static final short[] DFA44_special = DFA.unpackEncodedString(DFA44_specialS);
    static final short[][] DFA44_transition;

    static {
        int numStates = DFA44_transitionS.length;
        DFA44_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA44_transition[i] = DFA.unpackEncodedString(DFA44_transitionS[i]);
        }
    }

    class DFA44 extends DFA {

        public DFA44(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 44;
            this.eot = DFA44_eot;
            this.eof = DFA44_eof;
            this.min = DFA44_min;
            this.max = DFA44_max;
            this.accept = DFA44_accept;
            this.special = DFA44_special;
            this.transition = DFA44_transition;
        }
        public String getDescription() {
            return "275:1: subsetExpr returns [SubsetExpr value] : (e1= trimExpr | e2= sliceExpr | e3= extendExpr | e4= trimSliceExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA44_34 = input.LA(1);

                         
                        int index44_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_34);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA44_35 = input.LA(1);

                         
                        int index44_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_35);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA44_36 = input.LA(1);

                         
                        int index44_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_36);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA44_37 = input.LA(1);

                         
                        int index44_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_37);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA44_38 = input.LA(1);

                         
                        int index44_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_38);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA44_39 = input.LA(1);

                         
                        int index44_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_39);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA44_40 = input.LA(1);

                         
                        int index44_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_40);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA44_41 = input.LA(1);

                         
                        int index44_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_41);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA44_42 = input.LA(1);

                         
                        int index44_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_42);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA44_43 = input.LA(1);

                         
                        int index44_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_43);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA44_44 = input.LA(1);

                         
                        int index44_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_44);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA44_45 = input.LA(1);

                         
                        int index44_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_45);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA44_46 = input.LA(1);

                         
                        int index44_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_46);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA44_47 = input.LA(1);

                         
                        int index44_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_47);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA44_48 = input.LA(1);

                         
                        int index44_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_48);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA44_49 = input.LA(1);

                         
                        int index44_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_49);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA44_50 = input.LA(1);

                         
                        int index44_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_50);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA44_51 = input.LA(1);

                         
                        int index44_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_51);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA44_52 = input.LA(1);

                         
                        int index44_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_52);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA44_53 = input.LA(1);

                         
                        int index44_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_53);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA44_54 = input.LA(1);

                         
                        int index44_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_54);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA44_55 = input.LA(1);

                         
                        int index44_55 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_55);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA44_56 = input.LA(1);

                         
                        int index44_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_56);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA44_57 = input.LA(1);

                         
                        int index44_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_57);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA44_58 = input.LA(1);

                         
                        int index44_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_58);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA44_59 = input.LA(1);

                         
                        int index44_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_59);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA44_60 = input.LA(1);

                         
                        int index44_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_60);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA44_61 = input.LA(1);

                         
                        int index44_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_61);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA44_62 = input.LA(1);

                         
                        int index44_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_62);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA44_63 = input.LA(1);

                         
                        int index44_63 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_63);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA44_64 = input.LA(1);

                         
                        int index44_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_64);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA44_65 = input.LA(1);

                         
                        int index44_65 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_65);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA44_66 = input.LA(1);

                         
                        int index44_66 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_66);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA44_67 = input.LA(1);

                         
                        int index44_67 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_67);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA44_68 = input.LA(1);

                         
                        int index44_68 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_68);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA44_69 = input.LA(1);

                         
                        int index44_69 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_69);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA44_70 = input.LA(1);

                         
                        int index44_70 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_70);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA44_71 = input.LA(1);

                         
                        int index44_71 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_71);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA44_72 = input.LA(1);

                         
                        int index44_72 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_72);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA44_73 = input.LA(1);

                         
                        int index44_73 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_73);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA44_74 = input.LA(1);

                         
                        int index44_74 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_74);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA44_75 = input.LA(1);

                         
                        int index44_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_75);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA44_76 = input.LA(1);

                         
                        int index44_76 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_76);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA44_77 = input.LA(1);

                         
                        int index44_77 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_77);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA44_78 = input.LA(1);

                         
                        int index44_78 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_78);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA44_79 = input.LA(1);

                         
                        int index44_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_79);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA44_80 = input.LA(1);

                         
                        int index44_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_80);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA44_81 = input.LA(1);

                         
                        int index44_81 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_81);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA44_82 = input.LA(1);

                         
                        int index44_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_82);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA44_83 = input.LA(1);

                         
                        int index44_83 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_83);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA44_84 = input.LA(1);

                         
                        int index44_84 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_84);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA44_85 = input.LA(1);

                         
                        int index44_85 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_85);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA44_86 = input.LA(1);

                         
                        int index44_86 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_86);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA44_87 = input.LA(1);

                         
                        int index44_87 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_87);
                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA44_88 = input.LA(1);

                         
                        int index44_88 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_88);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA44_89 = input.LA(1);

                         
                        int index44_89 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_89);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA44_90 = input.LA(1);

                         
                        int index44_90 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_90);
                        if ( s>=0 ) return s;
                        break;
                    case 57 : 
                        int LA44_91 = input.LA(1);

                         
                        int index44_91 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_91);
                        if ( s>=0 ) return s;
                        break;
                    case 58 : 
                        int LA44_92 = input.LA(1);

                         
                        int index44_92 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_92);
                        if ( s>=0 ) return s;
                        break;
                    case 59 : 
                        int LA44_93 = input.LA(1);

                         
                        int index44_93 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_93);
                        if ( s>=0 ) return s;
                        break;
                    case 60 : 
                        int LA44_94 = input.LA(1);

                         
                        int index44_94 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_94);
                        if ( s>=0 ) return s;
                        break;
                    case 61 : 
                        int LA44_95 = input.LA(1);

                         
                        int index44_95 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_95);
                        if ( s>=0 ) return s;
                        break;
                    case 62 : 
                        int LA44_96 = input.LA(1);

                         
                        int index44_96 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_96);
                        if ( s>=0 ) return s;
                        break;
                    case 63 : 
                        int LA44_97 = input.LA(1);

                         
                        int index44_97 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_97);
                        if ( s>=0 ) return s;
                        break;
                    case 64 : 
                        int LA44_98 = input.LA(1);

                         
                        int index44_98 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_98);
                        if ( s>=0 ) return s;
                        break;
                    case 65 : 
                        int LA44_99 = input.LA(1);

                         
                        int index44_99 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_99);
                        if ( s>=0 ) return s;
                        break;
                    case 66 : 
                        int LA44_100 = input.LA(1);

                         
                        int index44_100 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_100);
                        if ( s>=0 ) return s;
                        break;
                    case 67 : 
                        int LA44_101 = input.LA(1);

                         
                        int index44_101 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_101);
                        if ( s>=0 ) return s;
                        break;
                    case 68 : 
                        int LA44_102 = input.LA(1);

                         
                        int index44_102 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_102);
                        if ( s>=0 ) return s;
                        break;
                    case 69 : 
                        int LA44_103 = input.LA(1);

                         
                        int index44_103 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_103);
                        if ( s>=0 ) return s;
                        break;
                    case 70 : 
                        int LA44_104 = input.LA(1);

                         
                        int index44_104 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_104);
                        if ( s>=0 ) return s;
                        break;
                    case 71 : 
                        int LA44_105 = input.LA(1);

                         
                        int index44_105 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_105);
                        if ( s>=0 ) return s;
                        break;
                    case 72 : 
                        int LA44_106 = input.LA(1);

                         
                        int index44_106 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_106);
                        if ( s>=0 ) return s;
                        break;
                    case 73 : 
                        int LA44_107 = input.LA(1);

                         
                        int index44_107 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_107);
                        if ( s>=0 ) return s;
                        break;
                    case 74 : 
                        int LA44_108 = input.LA(1);

                         
                        int index44_108 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_108);
                        if ( s>=0 ) return s;
                        break;
                    case 75 : 
                        int LA44_109 = input.LA(1);

                         
                        int index44_109 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_109);
                        if ( s>=0 ) return s;
                        break;
                    case 76 : 
                        int LA44_110 = input.LA(1);

                         
                        int index44_110 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_110);
                        if ( s>=0 ) return s;
                        break;
                    case 77 : 
                        int LA44_111 = input.LA(1);

                         
                        int index44_111 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_111);
                        if ( s>=0 ) return s;
                        break;
                    case 78 : 
                        int LA44_112 = input.LA(1);

                         
                        int index44_112 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_112);
                        if ( s>=0 ) return s;
                        break;
                    case 79 : 
                        int LA44_113 = input.LA(1);

                         
                        int index44_113 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_113);
                        if ( s>=0 ) return s;
                        break;
                    case 80 : 
                        int LA44_114 = input.LA(1);

                         
                        int index44_114 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_114);
                        if ( s>=0 ) return s;
                        break;
                    case 81 : 
                        int LA44_115 = input.LA(1);

                         
                        int index44_115 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_115);
                        if ( s>=0 ) return s;
                        break;
                    case 82 : 
                        int LA44_116 = input.LA(1);

                         
                        int index44_116 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_116);
                        if ( s>=0 ) return s;
                        break;
                    case 83 : 
                        int LA44_117 = input.LA(1);

                         
                        int index44_117 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_117);
                        if ( s>=0 ) return s;
                        break;
                    case 84 : 
                        int LA44_118 = input.LA(1);

                         
                        int index44_118 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_118);
                        if ( s>=0 ) return s;
                        break;
                    case 85 : 
                        int LA44_119 = input.LA(1);

                         
                        int index44_119 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_119);
                        if ( s>=0 ) return s;
                        break;
                    case 86 : 
                        int LA44_120 = input.LA(1);

                         
                        int index44_120 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_120);
                        if ( s>=0 ) return s;
                        break;
                    case 87 : 
                        int LA44_121 = input.LA(1);

                         
                        int index44_121 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_121);
                        if ( s>=0 ) return s;
                        break;
                    case 88 : 
                        int LA44_122 = input.LA(1);

                         
                        int index44_122 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_122);
                        if ( s>=0 ) return s;
                        break;
                    case 89 : 
                        int LA44_123 = input.LA(1);

                         
                        int index44_123 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_123);
                        if ( s>=0 ) return s;
                        break;
                    case 90 : 
                        int LA44_124 = input.LA(1);

                         
                        int index44_124 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_124);
                        if ( s>=0 ) return s;
                        break;
                    case 91 : 
                        int LA44_125 = input.LA(1);

                         
                        int index44_125 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_125);
                        if ( s>=0 ) return s;
                        break;
                    case 92 : 
                        int LA44_126 = input.LA(1);

                         
                        int index44_126 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_126);
                        if ( s>=0 ) return s;
                        break;
                    case 93 : 
                        int LA44_127 = input.LA(1);

                         
                        int index44_127 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_127);
                        if ( s>=0 ) return s;
                        break;
                    case 94 : 
                        int LA44_128 = input.LA(1);

                         
                        int index44_128 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_128);
                        if ( s>=0 ) return s;
                        break;
                    case 95 : 
                        int LA44_129 = input.LA(1);

                         
                        int index44_129 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_129);
                        if ( s>=0 ) return s;
                        break;
                    case 96 : 
                        int LA44_130 = input.LA(1);

                         
                        int index44_130 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_130);
                        if ( s>=0 ) return s;
                        break;
                    case 97 : 
                        int LA44_131 = input.LA(1);

                         
                        int index44_131 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_131);
                        if ( s>=0 ) return s;
                        break;
                    case 98 : 
                        int LA44_132 = input.LA(1);

                         
                        int index44_132 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_132);
                        if ( s>=0 ) return s;
                        break;
                    case 99 : 
                        int LA44_133 = input.LA(1);

                         
                        int index44_133 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_133);
                        if ( s>=0 ) return s;
                        break;
                    case 100 : 
                        int LA44_134 = input.LA(1);

                         
                        int index44_134 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_134);
                        if ( s>=0 ) return s;
                        break;
                    case 101 : 
                        int LA44_135 = input.LA(1);

                         
                        int index44_135 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_135);
                        if ( s>=0 ) return s;
                        break;
                    case 102 : 
                        int LA44_136 = input.LA(1);

                         
                        int index44_136 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_136);
                        if ( s>=0 ) return s;
                        break;
                    case 103 : 
                        int LA44_137 = input.LA(1);

                         
                        int index44_137 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_137);
                        if ( s>=0 ) return s;
                        break;
                    case 104 : 
                        int LA44_138 = input.LA(1);

                         
                        int index44_138 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_138);
                        if ( s>=0 ) return s;
                        break;
                    case 105 : 
                        int LA44_139 = input.LA(1);

                         
                        int index44_139 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_139);
                        if ( s>=0 ) return s;
                        break;
                    case 106 : 
                        int LA44_140 = input.LA(1);

                         
                        int index44_140 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_140);
                        if ( s>=0 ) return s;
                        break;
                    case 107 : 
                        int LA44_141 = input.LA(1);

                         
                        int index44_141 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_141);
                        if ( s>=0 ) return s;
                        break;
                    case 108 : 
                        int LA44_142 = input.LA(1);

                         
                        int index44_142 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_142);
                        if ( s>=0 ) return s;
                        break;
                    case 109 : 
                        int LA44_143 = input.LA(1);

                         
                        int index44_143 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_143);
                        if ( s>=0 ) return s;
                        break;
                    case 110 : 
                        int LA44_144 = input.LA(1);

                         
                        int index44_144 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_144);
                        if ( s>=0 ) return s;
                        break;
                    case 111 : 
                        int LA44_145 = input.LA(1);

                         
                        int index44_145 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_145);
                        if ( s>=0 ) return s;
                        break;
                    case 112 : 
                        int LA44_146 = input.LA(1);

                         
                        int index44_146 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_146);
                        if ( s>=0 ) return s;
                        break;
                    case 113 : 
                        int LA44_147 = input.LA(1);

                         
                        int index44_147 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_147);
                        if ( s>=0 ) return s;
                        break;
                    case 114 : 
                        int LA44_148 = input.LA(1);

                         
                        int index44_148 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_148);
                        if ( s>=0 ) return s;
                        break;
                    case 115 : 
                        int LA44_149 = input.LA(1);

                         
                        int index44_149 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_149);
                        if ( s>=0 ) return s;
                        break;
                    case 116 : 
                        int LA44_150 = input.LA(1);

                         
                        int index44_150 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_150);
                        if ( s>=0 ) return s;
                        break;
                    case 117 : 
                        int LA44_151 = input.LA(1);

                         
                        int index44_151 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_151);
                        if ( s>=0 ) return s;
                        break;
                    case 118 : 
                        int LA44_152 = input.LA(1);

                         
                        int index44_152 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_152);
                        if ( s>=0 ) return s;
                        break;
                    case 119 : 
                        int LA44_153 = input.LA(1);

                         
                        int index44_153 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_153);
                        if ( s>=0 ) return s;
                        break;
                    case 120 : 
                        int LA44_154 = input.LA(1);

                         
                        int index44_154 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_154);
                        if ( s>=0 ) return s;
                        break;
                    case 121 : 
                        int LA44_155 = input.LA(1);

                         
                        int index44_155 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred111_wcps()) ) {s = 31;}

                        else if ( (synpred112_wcps()) ) {s = 32;}

                        else if ( (true) ) {s = 156;}

                         
                        input.seek(index44_155);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 44, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA45_eotS =
        "\40\uffff";
    static final String DFA45_eofS =
        "\40\uffff";
    static final String DFA45_minS =
        "\1\6\37\uffff";
    static final String DFA45_maxS =
        "\1\154\37\uffff";
    static final String DFA45_acceptS =
        "\1\uffff\1\1\35\uffff\1\2";
    static final String DFA45_specialS =
        "\40\uffff}>";
    static final String[] DFA45_transitionS = {
            "\1\1\20\uffff\1\1\3\uffff\20\1\3\uffff\1\1\4\uffff\6\1\1\uffff"+
            "\1\1\1\uffff\2\1\16\uffff\1\1\1\uffff\3\1\3\uffff\1\37\14\uffff"+
            "\2\1\10\uffff\2\1",
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
            ""
    };

    static final short[] DFA45_eot = DFA.unpackEncodedString(DFA45_eotS);
    static final short[] DFA45_eof = DFA.unpackEncodedString(DFA45_eofS);
    static final char[] DFA45_min = DFA.unpackEncodedStringToUnsignedChars(DFA45_minS);
    static final char[] DFA45_max = DFA.unpackEncodedStringToUnsignedChars(DFA45_maxS);
    static final short[] DFA45_accept = DFA.unpackEncodedString(DFA45_acceptS);
    static final short[] DFA45_special = DFA.unpackEncodedString(DFA45_specialS);
    static final short[][] DFA45_transition;

    static {
        int numStates = DFA45_transitionS.length;
        DFA45_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA45_transition[i] = DFA.unpackEncodedString(DFA45_transitionS[i]);
        }
    }

    class DFA45 extends DFA {

        public DFA45(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 45;
            this.eot = DFA45_eot;
            this.eof = DFA45_eof;
            this.min = DFA45_min;
            this.max = DFA45_max;
            this.accept = DFA45_accept;
            this.special = DFA45_special;
            this.transition = DFA45_transition;
        }
        public String getDescription() {
            return "281:1: trimExpr returns [TrimExpr value] : (e1= coverageAtom LBRACKET dil= dimensionIntervalList RBRACKET | TRIM LPAREN e2= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN );";
        }
    }
    static final String DFA49_eotS =
        "\40\uffff";
    static final String DFA49_eofS =
        "\40\uffff";
    static final String DFA49_minS =
        "\1\6\37\uffff";
    static final String DFA49_maxS =
        "\1\154\37\uffff";
    static final String DFA49_acceptS =
        "\1\uffff\1\1\35\uffff\1\2";
    static final String DFA49_specialS =
        "\40\uffff}>";
    static final String[] DFA49_transitionS = {
            "\1\1\20\uffff\1\1\3\uffff\20\1\3\uffff\1\1\4\uffff\6\1\1\uffff"+
            "\1\1\1\uffff\2\1\16\uffff\1\1\1\uffff\3\1\4\uffff\1\37\13\uffff"+
            "\2\1\10\uffff\2\1",
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
            ""
    };

    static final short[] DFA49_eot = DFA.unpackEncodedString(DFA49_eotS);
    static final short[] DFA49_eof = DFA.unpackEncodedString(DFA49_eofS);
    static final char[] DFA49_min = DFA.unpackEncodedStringToUnsignedChars(DFA49_minS);
    static final char[] DFA49_max = DFA.unpackEncodedStringToUnsignedChars(DFA49_maxS);
    static final short[] DFA49_accept = DFA.unpackEncodedString(DFA49_acceptS);
    static final short[] DFA49_special = DFA.unpackEncodedString(DFA49_specialS);
    static final short[][] DFA49_transition;

    static {
        int numStates = DFA49_transitionS.length;
        DFA49_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA49_transition[i] = DFA.unpackEncodedString(DFA49_transitionS[i]);
        }
    }

    class DFA49 extends DFA {

        public DFA49(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 49;
            this.eot = DFA49_eot;
            this.eof = DFA49_eof;
            this.min = DFA49_min;
            this.max = DFA49_max;
            this.accept = DFA49_accept;
            this.special = DFA49_special;
            this.transition = DFA49_transition;
        }
        public String getDescription() {
            return "294:1: sliceExpr returns [SliceExpr value] : (e1= coverageAtom LBRACKET dpl= dimensionPointList RBRACKET | SLICE LPAREN e2= coverageExpr COMMA LBRACE dpl= dimensionPointList RBRACE RPAREN );";
        }
    }
    static final String DFA51_eotS =
        "\77\uffff";
    static final String DFA51_eofS =
        "\1\1\76\uffff";
    static final String DFA51_minS =
        "\1\7\7\uffff\1\0\66\uffff";
    static final String DFA51_maxS =
        "\1\140\7\uffff\1\0\66\uffff";
    static final String DFA51_acceptS =
        "\1\uffff\1\2\74\uffff\1\1";
    static final String DFA51_specialS =
        "\10\uffff\1\0\66\uffff}>";
    static final String[] DFA51_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\2\10\14\1\21\uffff\2\1\3\uffff\1\1"+
            "\7\uffff\1\1\30\uffff\1\1\15\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
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
            "",
            "",
            "",
            ""
    };

    static final short[] DFA51_eot = DFA.unpackEncodedString(DFA51_eotS);
    static final short[] DFA51_eof = DFA.unpackEncodedString(DFA51_eofS);
    static final char[] DFA51_min = DFA.unpackEncodedStringToUnsignedChars(DFA51_minS);
    static final char[] DFA51_max = DFA.unpackEncodedStringToUnsignedChars(DFA51_maxS);
    static final short[] DFA51_accept = DFA.unpackEncodedString(DFA51_acceptS);
    static final short[] DFA51_special = DFA.unpackEncodedString(DFA51_specialS);
    static final short[][] DFA51_transition;

    static {
        int numStates = DFA51_transitionS.length;
        DFA51_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA51_transition[i] = DFA.unpackEncodedString(DFA51_transitionS[i]);
        }
    }

    class DFA51 extends DFA {

        public DFA51(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 51;
            this.eot = DFA51_eot;
            this.eof = DFA51_eof;
            this.min = DFA51_min;
            this.max = DFA51_max;
            this.accept = DFA51_accept;
            this.special = DFA51_special;
            this.transition = DFA51_transition;
        }
        public String getDescription() {
            return "()* loopback of 315:7: (op= ( OR | XOR ) e2= booleanScalarTerm )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA51_8 = input.LA(1);

                         
                        int index51_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred130_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index51_8);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 51, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA52_eotS =
        "\77\uffff";
    static final String DFA52_eofS =
        "\1\1\76\uffff";
    static final String DFA52_minS =
        "\1\7\7\uffff\1\0\66\uffff";
    static final String DFA52_maxS =
        "\1\140\7\uffff\1\0\66\uffff";
    static final String DFA52_acceptS =
        "\1\uffff\1\2\74\uffff\1\1";
    static final String DFA52_specialS =
        "\10\uffff\1\0\66\uffff}>";
    static final String[] DFA52_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\2\1\1\10\13\1\21\uffff\2\1\3\uffff"+
            "\1\1\7\uffff\1\1\30\uffff\1\1\15\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
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
            "",
            "",
            "",
            ""
    };

    static final short[] DFA52_eot = DFA.unpackEncodedString(DFA52_eotS);
    static final short[] DFA52_eof = DFA.unpackEncodedString(DFA52_eofS);
    static final char[] DFA52_min = DFA.unpackEncodedStringToUnsignedChars(DFA52_minS);
    static final char[] DFA52_max = DFA.unpackEncodedStringToUnsignedChars(DFA52_maxS);
    static final short[] DFA52_accept = DFA.unpackEncodedString(DFA52_acceptS);
    static final short[] DFA52_special = DFA.unpackEncodedString(DFA52_specialS);
    static final short[][] DFA52_transition;

    static {
        int numStates = DFA52_transitionS.length;
        DFA52_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA52_transition[i] = DFA.unpackEncodedString(DFA52_transitionS[i]);
        }
    }

    class DFA52 extends DFA {

        public DFA52(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 52;
            this.eot = DFA52_eot;
            this.eof = DFA52_eof;
            this.min = DFA52_min;
            this.max = DFA52_max;
            this.accept = DFA52_accept;
            this.special = DFA52_special;
            this.transition = DFA52_transition;
        }
        public String getDescription() {
            return "()* loopback of 319:4: (op= AND e2= booleanScalarNegation )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA52_8 = input.LA(1);

                         
                        int index52_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred131_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index52_8);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 52, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA53_eotS =
        "\17\uffff";
    static final String DFA53_eofS =
        "\17\uffff";
    static final String DFA53_minS =
        "\1\6\16\uffff";
    static final String DFA53_maxS =
        "\1\154\16\uffff";
    static final String DFA53_acceptS =
        "\1\uffff\1\1\14\uffff\1\2";
    static final String DFA53_specialS =
        "\17\uffff}>";
    static final String[] DFA53_transitionS = {
            "\1\1\20\uffff\1\1\3\uffff\1\1\7\uffff\10\1\21\uffff\2\1\16\uffff"+
            "\1\16\1\uffff\3\1\20\uffff\2\1\11\uffff\1\1",
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
            "",
            "",
            "",
            ""
    };

    static final short[] DFA53_eot = DFA.unpackEncodedString(DFA53_eotS);
    static final short[] DFA53_eof = DFA.unpackEncodedString(DFA53_eofS);
    static final char[] DFA53_min = DFA.unpackEncodedStringToUnsignedChars(DFA53_minS);
    static final char[] DFA53_max = DFA.unpackEncodedStringToUnsignedChars(DFA53_maxS);
    static final short[] DFA53_accept = DFA.unpackEncodedString(DFA53_acceptS);
    static final short[] DFA53_special = DFA.unpackEncodedString(DFA53_specialS);
    static final short[][] DFA53_transition;

    static {
        int numStates = DFA53_transitionS.length;
        DFA53_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA53_transition[i] = DFA.unpackEncodedString(DFA53_transitionS[i]);
        }
    }

    class DFA53 extends DFA {

        public DFA53(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 53;
            this.eot = DFA53_eot;
            this.eof = DFA53_eof;
            this.min = DFA53_min;
            this.max = DFA53_max;
            this.accept = DFA53_accept;
            this.special = DFA53_special;
            this.transition = DFA53_transition;
        }
        public String getDescription() {
            return "321:1: booleanScalarNegation returns [BooleanScalarExpr value] : (e1= booleanScalarAtom | op= NOT e1= booleanScalarAtom );";
        }
    }
    static final String DFA54_eotS =
        "\34\uffff";
    static final String DFA54_eofS =
        "\34\uffff";
    static final String DFA54_minS =
        "\2\6\14\uffff\2\0\2\uffff\10\0\2\uffff";
    static final String DFA54_maxS =
        "\2\154\14\uffff\2\0\2\uffff\10\0\2\uffff";
    static final String DFA54_acceptS =
        "\2\uffff\1\2\1\uffff\1\3\10\uffff\1\4\2\uffff\1\1\13\uffff";
    static final String DFA54_specialS =
        "\16\uffff\1\0\1\1\2\uffff\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\2\uffff}>";
    static final String[] DFA54_transitionS = {
            "\1\1\20\uffff\1\4\3\uffff\1\2\7\uffff\10\4\21\uffff\2\4\20\uffff"+
            "\2\4\1\2\20\uffff\1\15\1\4\11\uffff\1\4",
            "\1\17\20\uffff\1\22\3\uffff\1\20\7\uffff\7\27\1\30\21\uffff"+
            "\1\24\1\23\16\uffff\1\20\1\uffff\1\26\1\25\1\20\20\uffff\1\20"+
            "\1\16\11\uffff\1\31",
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
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            ""
    };

    static final short[] DFA54_eot = DFA.unpackEncodedString(DFA54_eotS);
    static final short[] DFA54_eof = DFA.unpackEncodedString(DFA54_eofS);
    static final char[] DFA54_min = DFA.unpackEncodedStringToUnsignedChars(DFA54_minS);
    static final char[] DFA54_max = DFA.unpackEncodedStringToUnsignedChars(DFA54_maxS);
    static final short[] DFA54_accept = DFA.unpackEncodedString(DFA54_acceptS);
    static final short[] DFA54_special = DFA.unpackEncodedString(DFA54_specialS);
    static final short[][] DFA54_transition;

    static {
        int numStates = DFA54_transitionS.length;
        DFA54_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA54_transition[i] = DFA.unpackEncodedString(DFA54_transitionS[i]);
        }
    }

    class DFA54 extends DFA {

        public DFA54(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 54;
            this.eot = DFA54_eot;
            this.eof = DFA54_eof;
            this.min = DFA54_min;
            this.max = DFA54_max;
            this.accept = DFA54_accept;
            this.special = DFA54_special;
            this.transition = DFA54_transition;
        }
        public String getDescription() {
            return "325:1: booleanScalarAtom returns [BooleanScalarExpr value] : ( LPAREN e1= booleanScalarExpr RPAREN | s1= stringScalarExpr cop= compOp s2= stringScalarExpr | n1= numericScalarExpr cop= compOp n2= numericScalarExpr | e= BOOLEANCONSTANT );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA54_14 = input.LA(1);

                         
                        int index54_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred133_wcps()) ) {s = 16;}

                        else if ( (synpred135_wcps()) ) {s = 4;}

                         
                        input.seek(index54_14);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA54_15 = input.LA(1);

                         
                        int index54_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred133_wcps()) ) {s = 16;}

                        else if ( (synpred135_wcps()) ) {s = 4;}

                         
                        input.seek(index54_15);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA54_18 = input.LA(1);

                         
                        int index54_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred133_wcps()) ) {s = 16;}

                        else if ( (synpred135_wcps()) ) {s = 4;}

                         
                        input.seek(index54_18);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA54_19 = input.LA(1);

                         
                        int index54_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred133_wcps()) ) {s = 16;}

                        else if ( (synpred135_wcps()) ) {s = 4;}

                         
                        input.seek(index54_19);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA54_20 = input.LA(1);

                         
                        int index54_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred133_wcps()) ) {s = 16;}

                        else if ( (synpred135_wcps()) ) {s = 4;}

                         
                        input.seek(index54_20);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA54_21 = input.LA(1);

                         
                        int index54_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred133_wcps()) ) {s = 16;}

                        else if ( (synpred135_wcps()) ) {s = 4;}

                         
                        input.seek(index54_21);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA54_22 = input.LA(1);

                         
                        int index54_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred133_wcps()) ) {s = 16;}

                        else if ( (synpred135_wcps()) ) {s = 4;}

                         
                        input.seek(index54_22);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA54_23 = input.LA(1);

                         
                        int index54_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred133_wcps()) ) {s = 16;}

                        else if ( (synpred135_wcps()) ) {s = 4;}

                         
                        input.seek(index54_23);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA54_24 = input.LA(1);

                         
                        int index54_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred133_wcps()) ) {s = 16;}

                        else if ( (synpred135_wcps()) ) {s = 4;}

                         
                        input.seek(index54_24);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA54_25 = input.LA(1);

                         
                        int index54_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred133_wcps()) ) {s = 16;}

                        else if ( (synpred135_wcps()) ) {s = 4;}

                         
                        input.seek(index54_25);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 54, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA55_eotS =
        "\77\uffff";
    static final String DFA55_eofS =
        "\1\1\76\uffff";
    static final String DFA55_minS =
        "\1\7\3\uffff\1\0\72\uffff";
    static final String DFA55_maxS =
        "\1\140\3\uffff\1\0\72\uffff";
    static final String DFA55_acceptS =
        "\1\uffff\1\2\74\uffff\1\1";
    static final String DFA55_specialS =
        "\4\uffff\1\0\72\uffff}>";
    static final String[] DFA55_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\11\1\2\4\3\1\21\uffff\2\1\3\uffff\1"+
            "\1\7\uffff\1\1\30\uffff\1\1\15\uffff\1\1",
            "",
            "",
            "",
            "\1\uffff",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA55_eot = DFA.unpackEncodedString(DFA55_eotS);
    static final short[] DFA55_eof = DFA.unpackEncodedString(DFA55_eofS);
    static final char[] DFA55_min = DFA.unpackEncodedStringToUnsignedChars(DFA55_minS);
    static final char[] DFA55_max = DFA.unpackEncodedStringToUnsignedChars(DFA55_maxS);
    static final short[] DFA55_accept = DFA.unpackEncodedString(DFA55_acceptS);
    static final short[] DFA55_special = DFA.unpackEncodedString(DFA55_specialS);
    static final short[][] DFA55_transition;

    static {
        int numStates = DFA55_transitionS.length;
        DFA55_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA55_transition[i] = DFA.unpackEncodedString(DFA55_transitionS[i]);
        }
    }

    class DFA55 extends DFA {

        public DFA55(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 55;
            this.eot = DFA55_eot;
            this.eof = DFA55_eof;
            this.min = DFA55_min;
            this.max = DFA55_max;
            this.accept = DFA55_accept;
            this.special = DFA55_special;
            this.transition = DFA55_transition;
        }
        public String getDescription() {
            return "()* loopback of 333:4: (op= ( PLUS | MINUS ) e2= numericScalarTerm )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA55_4 = input.LA(1);

                         
                        int index55_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred137_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index55_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 55, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA56_eotS =
        "\77\uffff";
    static final String DFA56_eofS =
        "\1\1\76\uffff";
    static final String DFA56_minS =
        "\1\7\3\uffff\1\0\72\uffff";
    static final String DFA56_maxS =
        "\1\140\3\uffff\1\0\72\uffff";
    static final String DFA56_acceptS =
        "\1\uffff\1\2\74\uffff\1\1";
    static final String DFA56_specialS =
        "\4\uffff\1\0\72\uffff}>";
    static final String[] DFA56_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\13\1\2\4\1\1\21\uffff\2\1\3\uffff\1"+
            "\1\7\uffff\1\1\30\uffff\1\1\15\uffff\1\1",
            "",
            "",
            "",
            "\1\uffff",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA56_eot = DFA.unpackEncodedString(DFA56_eotS);
    static final short[] DFA56_eof = DFA.unpackEncodedString(DFA56_eofS);
    static final char[] DFA56_min = DFA.unpackEncodedStringToUnsignedChars(DFA56_minS);
    static final char[] DFA56_max = DFA.unpackEncodedStringToUnsignedChars(DFA56_maxS);
    static final short[] DFA56_accept = DFA.unpackEncodedString(DFA56_acceptS);
    static final short[] DFA56_special = DFA.unpackEncodedString(DFA56_specialS);
    static final short[][] DFA56_transition;

    static {
        int numStates = DFA56_transitionS.length;
        DFA56_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA56_transition[i] = DFA.unpackEncodedString(DFA56_transitionS[i]);
        }
    }

    class DFA56 extends DFA {

        public DFA56(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 56;
            this.eot = DFA56_eot;
            this.eof = DFA56_eof;
            this.min = DFA56_min;
            this.max = DFA56_max;
            this.accept = DFA56_accept;
            this.special = DFA56_special;
            this.transition = DFA56_transition;
        }
        public String getDescription() {
            return "()* loopback of 337:3: (op= ( MULT | DIVIDE ) e2= numericScalarFactor )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA56_4 = input.LA(1);

                         
                        int index56_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred139_wcps()) ) {s = 62;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index56_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 56, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA57_eotS =
        "\26\uffff";
    static final String DFA57_eofS =
        "\26\uffff";
    static final String DFA57_minS =
        "\2\6\11\uffff\1\0\12\uffff";
    static final String DFA57_maxS =
        "\2\154\11\uffff\1\0\12\uffff";
    static final String DFA57_acceptS =
        "\2\uffff\1\2\1\3\1\4\1\5\1\6\1\7\1\11\1\uffff\1\12\1\uffff\1\1\10"+
        "\uffff\1\10";
    static final String DFA57_specialS =
        "\13\uffff\1\0\12\uffff}>";
    static final String[] DFA57_transitionS = {
            "\1\1\20\uffff\1\2\13\uffff\10\10\21\uffff\1\4\1\3\20\uffff\1"+
            "\6\1\5\22\uffff\1\7\11\uffff\1\12",
            "\1\14\20\uffff\1\14\13\uffff\10\14\21\uffff\2\14\20\uffff\2"+
            "\14\22\uffff\1\13\11\uffff\1\14",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA57_eot = DFA.unpackEncodedString(DFA57_eotS);
    static final short[] DFA57_eof = DFA.unpackEncodedString(DFA57_eofS);
    static final char[] DFA57_min = DFA.unpackEncodedStringToUnsignedChars(DFA57_minS);
    static final char[] DFA57_max = DFA.unpackEncodedStringToUnsignedChars(DFA57_maxS);
    static final short[] DFA57_accept = DFA.unpackEncodedString(DFA57_acceptS);
    static final short[] DFA57_special = DFA.unpackEncodedString(DFA57_specialS);
    static final short[][] DFA57_transition;

    static {
        int numStates = DFA57_transitionS.length;
        DFA57_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA57_transition[i] = DFA.unpackEncodedString(DFA57_transitionS[i]);
        }
    }

    class DFA57 extends DFA {

        public DFA57(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 57;
            this.eot = DFA57_eot;
            this.eof = DFA57_eof;
            this.min = DFA57_min;
            this.max = DFA57_max;
            this.accept = DFA57_accept;
            this.special = DFA57_special;
            this.transition = DFA57_transition;
        }
        public String getDescription() {
            return "339:1: numericScalarFactor returns [NumericScalarExpr value] : ( LPAREN e1= numericScalarExpr RPAREN | op= MINUS e10= numericScalarFactor | op= ABS LPAREN e12= numericScalarExpr RPAREN | op= SQRT LPAREN e11= numericScalarExpr RPAREN | op= ROUND LPAREN e1= numericScalarExpr RPAREN | e= INTEGERCONSTANT | e= FLOATCONSTANT | e2= complexConstant | e3= condenseExpr | e4= variableName );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA57_11 = input.LA(1);

                         
                        int index57_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred140_wcps()) ) {s = 12;}

                        else if ( (synpred147_wcps()) ) {s = 21;}

                         
                        input.seek(index57_11);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 57, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA61_eotS =
        "\30\uffff";
    static final String DFA61_eofS =
        "\30\uffff";
    static final String DFA61_minS =
        "\1\6\4\uffff\1\6\20\uffff\1\0\1\uffff";
    static final String DFA61_maxS =
        "\1\154\4\uffff\1\6\20\uffff\1\0\1\uffff";
    static final String DFA61_acceptS =
        "\1\uffff\1\1\25\uffff\1\2";
    static final String DFA61_specialS =
        "\26\uffff\1\0\1\uffff}>";
    static final String[] DFA61_transitionS = {
            "\1\1\20\uffff\1\1\3\uffff\7\1\1\5\10\1\21\uffff\2\1\16\uffff"+
            "\1\1\1\uffff\3\1\20\uffff\2\1\11\uffff\1\1",
            "",
            "",
            "",
            "",
            "\1\26",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            ""
    };

    static final short[] DFA61_eot = DFA.unpackEncodedString(DFA61_eotS);
    static final short[] DFA61_eof = DFA.unpackEncodedString(DFA61_eofS);
    static final char[] DFA61_min = DFA.unpackEncodedStringToUnsignedChars(DFA61_minS);
    static final char[] DFA61_max = DFA.unpackEncodedStringToUnsignedChars(DFA61_maxS);
    static final short[] DFA61_accept = DFA.unpackEncodedString(DFA61_acceptS);
    static final short[] DFA61_special = DFA.unpackEncodedString(DFA61_specialS);
    static final short[][] DFA61_transition;

    static {
        int numStates = DFA61_transitionS.length;
        DFA61_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA61_transition[i] = DFA.unpackEncodedString(DFA61_transitionS[i]);
        }
    }

    class DFA61 extends DFA {

        public DFA61(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 61;
            this.eot = DFA61_eot;
            this.eof = DFA61_eof;
            this.min = DFA61_min;
            this.max = DFA61_max;
            this.accept = DFA61_accept;
            this.special = DFA61_special;
            this.transition = DFA61_transition;
        }
        public String getDescription() {
            return "367:1: dimensionIntervalExpr returns [DimensionIntervalExpr value] : (e1= scalarExpr COLON e2= scalarExpr | DOMAIN LPAREN e3= coverageName COLON e4= axisName COLON e5= crsName RPAREN );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA61_22 = input.LA(1);

                         
                        int index61_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred156_wcps()) ) {s = 1;}

                        else if ( (true) ) {s = 23;}

                         
                        input.seek(index61_22);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 61, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_forClause_in_wcpsRequest63 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_whereClause_in_wcpsRequest72 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_returnClause_in_wcpsRequest83 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forClause98 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L});
    public static final BitSet FOLLOW_coverageVariable_in_forClause102 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IN_in_forClause104 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_forClause106 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_coverageList_in_forClause110 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_forClause112 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_forClause122 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L});
    public static final BitSet FOLLOW_coverageVariable_in_forClause126 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IN_in_forClause128 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_forClause130 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_coverageList_in_forClause134 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_forClause136 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_WHERE_in_whereClause157 = new BitSet(new long[]{0x300007F808800040L,0x000010060001D000L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_whereClause161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURN_in_returnClause176 = new BitSet(new long[]{0x300007FFF8801840L,0x000010060001D000L});
    public static final BitSet FOLLOW_processingExpr_in_returnClause180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageName_in_coverageList197 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_coverageList204 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_coverageName_in_coverageList208 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_encodedCoverageExpr_in_processingExpr230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_storeExpr_in_processingExpr242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_processingExpr254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ENCODE_in_encodedCoverageExpr272 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_encodedCoverageExpr274 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_encodedCoverageExpr278 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_encodedCoverageExpr280 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_stringConstant_in_encodedCoverageExpr284 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_COMMA_in_encodedCoverageExpr291 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_stringConstant_in_encodedCoverageExpr295 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_encodedCoverageExpr302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STORE_in_storeExpr319 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_storeExpr321 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_encodedCoverageExpr_in_storeExpr325 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_storeExpr327 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageLogicTerm_in_coverageExpr350 = new BitSet(new long[]{0x0000000000006002L});
    public static final BitSet FOLLOW_set_in_coverageExpr366 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageLogicTerm_in_coverageExpr374 = new BitSet(new long[]{0x0000000000006002L});
    public static final BitSet FOLLOW_coverageLogicFactor_in_coverageLogicTerm400 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_coverageLogicTerm416 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageLogicFactor_in_coverageLogicTerm420 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_coverageArithmeticExpr_in_coverageLogicFactor447 = new BitSet(new long[]{0x00000000003F0002L});
    public static final BitSet FOLLOW_set_in_coverageLogicFactor463 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageArithmeticExpr_in_coverageLogicFactor479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageArithmeticTerm_in_coverageArithmeticExpr507 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_set_in_coverageArithmeticExpr523 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageArithmeticTerm_in_coverageArithmeticExpr531 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_coverageArithmeticFactor_in_coverageArithmeticTerm559 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_set_in_coverageArithmeticTerm574 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageArithmeticFactor_in_coverageArithmeticTerm582 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_coverageValue_in_coverageArithmeticFactor610 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_OVERLAY_in_coverageArithmeticFactor625 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageValue_in_coverageArithmeticFactor629 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_subsetExpr_in_coverageValue655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryInducedExpr_in_coverageValue668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scaleExpr_in_coverageValue680 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_crsTransformExpr_in_coverageValue692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_coverageValue704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_coverageAtom727 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageVariable_in_coverageAtom739 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_coverageAtom749 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_coverageAtom753 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_coverageAtom755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstantExpr_in_coverageAtom768 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstructorExpr_in_coverageAtom780 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_setMetaDataExpr_in_coverageAtom793 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rangeConstructorExpr_in_coverageAtom806 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_metaDataExpr_in_scalarExpr830 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_condenseExpr_in_scalarExpr843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_scalarExpr856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericScalarExpr_in_scalarExpr870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringScalarExpr_in_scalarExpr883 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_scalarExpr894 = new BitSet(new long[]{0x300007FFF8801840L,0x000010060001D000L});
    public static final BitSet FOLLOW_scalarExpr_in_scalarExpr898 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_scalarExpr900 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_metaDataExpr924 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr926 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr930 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr932 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMAGECRS_in_metaDataExpr944 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr946 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr950 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr952 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMAGECRSDOMAIN_in_metaDataExpr964 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr966 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr970 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_COMMA_in_metaDataExpr973 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_axisName_in_metaDataExpr977 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr981 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CRSSET_in_metaDataExpr993 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr995 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr999 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr1001 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_domainExpr_in_metaDataExpr1013 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULLSET_in_metaDataExpr1025 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr1027 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr1031 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr1033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTERPOLATIONDEFAULT_in_metaDataExpr1045 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr1047 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr1051 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_metaDataExpr1053 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_fieldName_in_metaDataExpr1057 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr1059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTERPOLATIONSET_in_metaDataExpr1071 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr1073 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr1077 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_metaDataExpr1079 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_fieldName_in_metaDataExpr1083 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr1085 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOMAIN_in_domainExpr1104 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_domainExpr1106 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L});
    public static final BitSet FOLLOW_coverageVariable_in_domainExpr1110 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_domainExpr1112 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_axisName_in_domainExpr1116 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_domainExpr1118 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_crsName_in_domainExpr1122 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_domainExpr1124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_reduceExpr_in_condenseExpr1141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalCondenseExpr_in_condenseExpr1150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_reduceExpr1167 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_reduceExpr1183 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_reduceExpr1187 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_reduceExpr1189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONDENSE_in_generalCondenseExpr1204 = new BitSet(new long[]{0x000003000140A000L});
    public static final BitSet FOLLOW_condenseOpType_in_generalCondenseExpr1208 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_OVER_in_generalCondenseExpr1210 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_axisIteratorList_in_generalCondenseExpr1214 = new BitSet(new long[]{0x0000100000000200L});
    public static final BitSet FOLLOW_WHERE_in_generalCondenseExpr1221 = new BitSet(new long[]{0x300007F808800040L,0x000010060001D000L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_generalCondenseExpr1225 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_USING_in_generalCondenseExpr1233 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_generalCondenseExpr1237 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableName_in_axisIteratorList1254 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_axisName_in_axisIteratorList1258 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_axisIteratorList1260 = new BitSet(new long[]{0x0000000020000040L,0x000000000000C000L});
    public static final BitSet FOLLOW_intervalExpr_in_axisIteratorList1264 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_axisIteratorList1266 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_axisIteratorList1274 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_variableName_in_axisIteratorList1278 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_axisName_in_axisIteratorList1282 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_axisIteratorList1284 = new BitSet(new long[]{0x0000000020000040L,0x000000000000C000L});
    public static final BitSet FOLLOW_intervalExpr_in_axisIteratorList1288 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_axisIteratorList1290 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_indexExpr_in_intervalExpr1314 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_intervalExpr1316 = new BitSet(new long[]{0x0000000000000040L,0x000000000000C000L});
    public static final BitSet FOLLOW_indexExpr_in_intervalExpr1320 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMAGECRSDOMAIN_in_intervalExpr1335 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_intervalExpr1337 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_coverageName_in_intervalExpr1341 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_intervalExpr1343 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_axisName_in_intervalExpr1347 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_intervalExpr1349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COVERAGE_in_coverageConstantExpr1375 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_coverageName_in_coverageConstantExpr1379 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_OVER_in_coverageConstantExpr1381 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_axisIteratorList_in_coverageConstantExpr1385 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_VALUE_in_coverageConstantExpr1387 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LIST_in_coverageConstantExpr1389 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_LT_in_coverageConstantExpr1391 = new BitSet(new long[]{0x0000000000000040L,0x0000000600014000L});
    public static final BitSet FOLLOW_constantList_in_coverageConstantExpr1395 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GT_in_coverageConstantExpr1397 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_in_constantList1428 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_SEMICOLON_in_constantList1433 = new BitSet(new long[]{0x0000000000000040L,0x0000000600014000L});
    public static final BitSet FOLLOW_constant_in_constantList1437 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_subsetExpr_in_coverageAtomConstructor1463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryInducedExpr_in_coverageAtomConstructor1476 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scaleExpr_in_coverageAtomConstructor1488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_crsTransformExpr_in_coverageAtomConstructor1500 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_coverageAtomConstructor1512 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_coverageAtomConstructor1524 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageVariable_in_coverageAtomConstructor1536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstantExpr_in_coverageAtomConstructor1548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstructorExpr_in_coverageAtomConstructor1560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_setMetaDataExpr_in_coverageAtomConstructor1573 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rangeConstructorExpr_in_coverageAtomConstructor1586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COVERAGE_in_coverageConstructorExpr1605 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_coverageName_in_coverageConstructorExpr1609 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_OVER_in_coverageConstructorExpr1611 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_axisIteratorList_in_coverageConstructorExpr1615 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_VALUES_in_coverageConstructorExpr1617 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageAtomConstructor_in_coverageConstructorExpr1621 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SETIDENTIFIER_in_setMetaDataExpr1643 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_setMetaDataExpr1645 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_stringConstant_in_setMetaDataExpr1649 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1651 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_setMetaDataExpr1655 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_setMetaDataExpr1657 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SETCRSSET_in_setMetaDataExpr1670 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_setMetaDataExpr1672 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_setMetaDataExpr1676 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1678 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_crsList_in_setMetaDataExpr1682 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_setMetaDataExpr1684 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SETNULLSET_in_setMetaDataExpr1701 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_setMetaDataExpr1703 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_setMetaDataExpr1707 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1709 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_rangeExprList_in_setMetaDataExpr1713 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_setMetaDataExpr1715 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SETINTERPOLATIONDEFAULT_in_setMetaDataExpr1732 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_setMetaDataExpr1734 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_setMetaDataExpr1738 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1740 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_fieldName_in_setMetaDataExpr1744 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1746 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_interpolationMethod_in_setMetaDataExpr1750 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_setMetaDataExpr1752 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SETINTERPOLATIONSET_in_setMetaDataExpr1772 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_setMetaDataExpr1774 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_setMetaDataExpr1778 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1780 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_fieldName_in_setMetaDataExpr1784 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1786 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_interpolationMethodList_in_setMetaDataExpr1790 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_setMetaDataExpr1792 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_crsList1821 = new BitSet(new long[]{0x0200000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_crsName_in_crsList1828 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_COMMA_in_crsList1833 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_crsName_in_crsList1837 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_RBRACE_in_crsList1846 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_rangeExprList1865 = new BitSet(new long[]{0x0600000000000000L});
    public static final BitSet FOLLOW_rangeExpr_in_rangeExprList1872 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_COMMA_in_rangeExprList1877 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_rangeExpr_in_rangeExprList1881 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_RBRACE_in_rangeExprList1890 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_interpolationMethodList1906 = new BitSet(new long[]{0x0200000000000040L});
    public static final BitSet FOLLOW_interpolationMethod_in_interpolationMethodList1913 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_COMMA_in_interpolationMethodList1918 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_interpolationMethod_in_interpolationMethodList1922 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_RBRACE_in_interpolationMethodList1930 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRUCT_in_rangeExpr1943 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_rangeExpr1945 = new BitSet(new long[]{0x0200000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_fieldName_in_rangeExpr1953 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_rangeExpr1955 = new BitSet(new long[]{0x300007FFF8801840L,0x000010060001D000L});
    public static final BitSet FOLLOW_scalarExpr_in_rangeExpr1959 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_COMMA_in_rangeExpr1966 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_fieldName_in_rangeExpr1970 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_rangeExpr1972 = new BitSet(new long[]{0x300007FFF8801840L,0x000010060001D000L});
    public static final BitSet FOLLOW_scalarExpr_in_rangeExpr1976 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_RBRACE_in_rangeExpr1987 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRUCT_in_rangeConstructorExpr2004 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_rangeConstructorExpr2008 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_fieldName_in_rangeConstructorExpr2012 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_rangeConstructorExpr2014 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_rangeConstructorExpr2018 = new BitSet(new long[]{0x0202000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_rangeConstructorExpr2031 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_fieldName_in_rangeConstructorExpr2035 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_rangeConstructorExpr2037 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_rangeConstructorExpr2041 = new BitSet(new long[]{0x0202000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_rangeConstructorExpr2047 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CRSTRANSFORM_in_crsTransformExpr2070 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_crsTransformExpr2072 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_crsTransformExpr2076 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_crsTransformExpr2078 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_dimensionCrsList_in_crsTransformExpr2082 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_crsTransformExpr2084 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_fieldInterpolationList_in_crsTransformExpr2088 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_crsTransformExpr2090 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_dimensionCrsList2131 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionCrsElement_in_dimensionCrsList2135 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_COMMA_in_dimensionCrsList2148 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionCrsElement_in_dimensionCrsList2152 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_RBRACE_in_dimensionCrsList2159 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_axisName_in_dimensionCrsElement2180 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dimensionCrsElement2184 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_crsName_in_dimensionCrsElement2188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_fieldInterpolationList2206 = new BitSet(new long[]{0x0200000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_fieldInterpolationElement_in_fieldInterpolationList2211 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_fieldInterpolationList2218 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_fieldInterpolationElement_in_fieldInterpolationList2222 = new BitSet(new long[]{0x0200000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_RBRACE_in_fieldInterpolationList2230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldName_in_fieldInterpolationElement2245 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_interpolationMethod_in_fieldInterpolationElement2249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldExpr_in_unaryInducedExpr2269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryArithmeticExpr_in_unaryInducedExpr2278 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exponentialExpr_in_unaryInducedExpr2290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_trigonometricExpr_in_unaryInducedExpr2302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanExpr_in_unaryInducedExpr2314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_castExpr_in_unaryInducedExpr2326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rangeConstructorExpr_in_unaryInducedExpr2338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryArithmeticExpr2361 = new BitSet(new long[]{0x35F847FFF8801840L,0x000018060001D000L});
    public static final BitSet FOLLOW_coverageAtom_in_unaryArithmeticExpr2369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryArithmeticExpr2381 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_unaryArithmeticExpr2391 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_unaryArithmeticExpr2395 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_unaryArithmeticExpr2397 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_exponentialExpr2420 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_exponentialExpr2428 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_exponentialExpr2432 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_exponentialExpr2434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_trigonometricExpr2457 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_trigonometricExpr2477 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_trigonometricExpr2481 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_trigonometricExpr2483 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_booleanExpr2506 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_booleanExpr2510 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BIT_in_booleanExpr2522 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_booleanExpr2524 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_booleanExpr2528 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_booleanExpr2530 = new BitSet(new long[]{0x0000000000000040L,0x000000000000C000L});
    public static final BitSet FOLLOW_indexExpr_in_booleanExpr2534 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_booleanExpr2536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_indexTerm_in_indexExpr2559 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_set_in_indexExpr2569 = new BitSet(new long[]{0x0000000000000040L,0x000000000000C000L});
    public static final BitSet FOLLOW_indexTerm_in_indexExpr2577 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_indexFactor_in_indexTerm2602 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_set_in_indexTerm2615 = new BitSet(new long[]{0x0000000000000040L,0x000000000000C000L});
    public static final BitSet FOLLOW_indexFactor_in_indexTerm2623 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_INTEGERCONSTANT_in_indexFactor2650 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROUND_in_indexFactor2663 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_indexFactor2665 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarExpr_in_indexFactor2669 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_indexFactor2671 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_indexFactor2683 = new BitSet(new long[]{0x0000000000000040L,0x000000000000C000L});
    public static final BitSet FOLLOW_indexExpr_in_indexFactor2687 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_indexFactor2689 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_stringScalarExpr2716 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_stringScalarExpr2718 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_stringScalarExpr2722 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_stringScalarExpr2724 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_stringScalarExpr2737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SCALE_in_scaleExpr2755 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_scaleExpr2757 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_scaleExpr2761 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_scaleExpr2763 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_scaleExpr2765 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionIntervalList_in_scaleExpr2769 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_scaleExpr2771 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_COMMA_in_scaleExpr2788 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_fieldInterpolationList_in_scaleExpr2792 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_scaleExpr2817 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_trimExpr_in_subsetExpr2834 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sliceExpr_in_subsetExpr2843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_extendExpr_in_subsetExpr2852 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_trimSliceExpr_in_subsetExpr2864 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_trimExpr2881 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_LBRACKET_in_trimExpr2883 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionIntervalList_in_trimExpr2887 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_RBRACKET_in_trimExpr2889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRIM_in_trimExpr2898 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_trimExpr2900 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_trimExpr2904 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_trimExpr2906 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_trimExpr2908 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionIntervalList_in_trimExpr2912 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_trimExpr2914 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_trimExpr2916 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_trimSliceExpr2933 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_LBRACKET_in_trimSliceExpr2935 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_trimSliceExpr2972 = new BitSet(new long[]{0x0000000000000100L,0x0000000000080000L});
    public static final BitSet FOLLOW_dimensionPointElement_in_trimSliceExpr3009 = new BitSet(new long[]{0x0000000000000100L,0x0000000000080000L});
    public static final BitSet FOLLOW_COMMA_in_trimSliceExpr3045 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_trimSliceExpr3081 = new BitSet(new long[]{0x0000000000000100L,0x0000000000080000L});
    public static final BitSet FOLLOW_dimensionPointElement_in_trimSliceExpr3118 = new BitSet(new long[]{0x0000000000000100L,0x0000000000080000L});
    public static final BitSet FOLLOW_RBRACKET_in_trimSliceExpr3149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_sliceExpr3168 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_LBRACKET_in_sliceExpr3170 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionPointList_in_sliceExpr3174 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_RBRACKET_in_sliceExpr3176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SLICE_in_sliceExpr3183 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_sliceExpr3185 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_sliceExpr3189 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_sliceExpr3191 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_sliceExpr3193 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionPointList_in_sliceExpr3197 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_sliceExpr3199 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_sliceExpr3201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXTEND_in_extendExpr3216 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_extendExpr3218 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_extendExpr3222 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_extendExpr3224 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_extendExpr3226 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionIntervalList_in_extendExpr3230 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_extendExpr3232 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_extendExpr3234 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_castExpr3252 = new BitSet(new long[]{0x0000000000000000L,0x00000000FF800000L});
    public static final BitSet FOLLOW_rangeType_in_castExpr3256 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_castExpr3258 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_castExpr3262 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_rangeType3285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNSIGNED_in_rangeType3311 = new BitSet(new long[]{0x0000000000000000L,0x0000000007000000L});
    public static final BitSet FOLLOW_set_in_rangeType3315 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_fieldExpr3344 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
    public static final BitSet FOLLOW_DOT_in_fieldExpr3346 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_fieldName_in_fieldExpr3350 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanScalarTerm_in_booleanScalarExpr3375 = new BitSet(new long[]{0x0000000000006002L});
    public static final BitSet FOLLOW_set_in_booleanScalarExpr3388 = new BitSet(new long[]{0x300007F808800040L,0x000010060001D000L});
    public static final BitSet FOLLOW_booleanScalarTerm_in_booleanScalarExpr3396 = new BitSet(new long[]{0x0000000000006002L});
    public static final BitSet FOLLOW_booleanScalarNegation_in_booleanScalarTerm3418 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_booleanScalarTerm3428 = new BitSet(new long[]{0x300007F808800040L,0x000010060001D000L});
    public static final BitSet FOLLOW_booleanScalarNegation_in_booleanScalarTerm3432 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_booleanScalarAtom_in_booleanScalarNegation3453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_booleanScalarNegation3462 = new BitSet(new long[]{0x300007F808800040L,0x000010060001C000L});
    public static final BitSet FOLLOW_booleanScalarAtom_in_booleanScalarNegation3466 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_booleanScalarAtom3481 = new BitSet(new long[]{0x300007F808800040L,0x000010060001D000L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_booleanScalarAtom3485 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_booleanScalarAtom3487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringScalarExpr_in_booleanScalarAtom3496 = new BitSet(new long[]{0x00000000003F0000L});
    public static final BitSet FOLLOW_compOp_in_booleanScalarAtom3500 = new BitSet(new long[]{0x0000000008000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_stringScalarExpr_in_booleanScalarAtom3504 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericScalarExpr_in_booleanScalarAtom3514 = new BitSet(new long[]{0x00000000003F0000L});
    public static final BitSet FOLLOW_compOp_in_booleanScalarAtom3518 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarExpr_in_booleanScalarAtom3522 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOLEANCONSTANT_in_booleanScalarAtom3532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericScalarTerm_in_numericScalarExpr3549 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_set_in_numericScalarExpr3559 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarTerm_in_numericScalarExpr3567 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_numericScalarFactor_in_numericScalarTerm3586 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_set_in_numericScalarTerm3595 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarFactor_in_numericScalarTerm3603 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_LPAREN_in_numericScalarFactor3623 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarExpr_in_numericScalarFactor3627 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_numericScalarFactor3629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_numericScalarFactor3641 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarFactor_in_numericScalarFactor3645 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ABS_in_numericScalarFactor3657 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_numericScalarFactor3659 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarExpr_in_numericScalarFactor3663 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_numericScalarFactor3665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SQRT_in_numericScalarFactor3677 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_numericScalarFactor3679 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarExpr_in_numericScalarFactor3683 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_numericScalarFactor3685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROUND_in_numericScalarFactor3697 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_numericScalarFactor3699 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarExpr_in_numericScalarFactor3703 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_numericScalarFactor3705 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGERCONSTANT_in_numericScalarFactor3717 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOATCONSTANT_in_numericScalarFactor3729 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_complexConstant_in_numericScalarFactor3741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_condenseExpr_in_numericScalarFactor3753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableName_in_numericScalarFactor3765 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_compOp3783 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOTEQUALS_in_compOp3790 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LT_in_compOp3797 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GT_in_compOp3804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LTE_in_compOp3811 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GTE_in_compOp3818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_dimensionIntervalList3838 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_dimensionIntervalList3851 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_dimensionIntervalList3855 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_axisName_in_dimensionIntervalElement3880 = new BitSet(new long[]{0x0000200000000040L});
    public static final BitSet FOLLOW_COLON_in_dimensionIntervalElement3885 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_crsName_in_dimensionIntervalElement3889 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_dimensionIntervalElement3900 = new BitSet(new long[]{0x300007FFF8801840L,0x000010060001D000L});
    public static final BitSet FOLLOW_dimensionIntervalExpr_in_dimensionIntervalElement3904 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_dimensionIntervalElement3906 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_dimensionIntervalExpr3929 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dimensionIntervalExpr3931 = new BitSet(new long[]{0x300007FFF8801840L,0x000010060001D000L});
    public static final BitSet FOLLOW_scalarExpr_in_dimensionIntervalExpr3935 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOMAIN_in_dimensionIntervalExpr3945 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_dimensionIntervalExpr3947 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_coverageName_in_dimensionIntervalExpr3951 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dimensionIntervalExpr3953 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_axisName_in_dimensionIntervalExpr3957 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dimensionIntervalExpr3959 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_crsName_in_dimensionIntervalExpr3963 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_dimensionIntervalExpr3965 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dimensionPointElement_in_dimensionPointList3988 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_dimensionPointList3998 = new BitSet(new long[]{0x0000000000000000L,0x0000080000014000L});
    public static final BitSet FOLLOW_dimensionPointElement_in_dimensionPointList4002 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_axisName_in_dimensionPointElement4027 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_dimensionPointElement4029 = new BitSet(new long[]{0x300007FFF8801840L,0x000010060001D000L});
    public static final BitSet FOLLOW_dimensionPoint_in_dimensionPointElement4033 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_dimensionPointElement4035 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_axisName_in_dimensionPointElement4047 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dimensionPointElement4049 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_crsName_in_dimensionPointElement4053 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_dimensionPointElement4055 = new BitSet(new long[]{0x300007FFF8801840L,0x000010060001D000L});
    public static final BitSet FOLLOW_dimensionPoint_in_dimensionPointElement4059 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_dimensionPointElement4061 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_dimensionPoint4084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_interpolationMethod4102 = new BitSet(new long[]{0x0000000000000000L,0x0000007800000000L});
    public static final BitSet FOLLOW_interpolationType_in_interpolationMethod4106 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_interpolationMethod4108 = new BitSet(new long[]{0x0000000000000000L,0x0000078000000000L});
    public static final BitSet FOLLOW_nullResistence_in_interpolationMethod4112 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_interpolationMethod4114 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_interpolationType4131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nullResistence4156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_condenseOpType4181 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_fieldName4208 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_constant4225 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_complexConstant_in_constant4242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_complexConstant4257 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
    public static final BitSet FOLLOW_FLOATCONSTANT_in_complexConstant4261 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_complexConstant4263 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
    public static final BitSet FOLLOW_FLOATCONSTANT_in_complexConstant4267 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_complexConstant4269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_stringConstant4286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_name4303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringConstant_in_crsName4330 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_axisName4347 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARIABLE_DOLLAR_in_variableName4364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_coverageVariable4381 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_coverageName4396 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred8_wcps366 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageLogicTerm_in_synpred8_wcps374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_synpred9_wcps416 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageLogicFactor_in_synpred9_wcps420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred15_wcps463 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageArithmeticExpr_in_synpred15_wcps479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred17_wcps523 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageArithmeticTerm_in_synpred17_wcps531 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred19_wcps574 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageArithmeticFactor_in_synpred19_wcps582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OVERLAY_in_synpred20_wcps625 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageValue_in_synpred20_wcps629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subsetExpr_in_synpred21_wcps655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryInducedExpr_in_synpred22_wcps668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_synpred25_wcps727 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred27_wcps749 = new BitSet(new long[]{0xFDF847FFF8C01840L,0x000018060073FFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_synpred27_wcps753 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_synpred27_wcps755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstantExpr_in_synpred28_wcps768 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstructorExpr_in_synpred29_wcps780 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_metaDataExpr_in_synpred31_wcps830 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_condenseExpr_in_synpred32_wcps843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_synpred33_wcps856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericScalarExpr_in_synpred34_wcps870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringScalarExpr_in_synpred35_wcps883 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subsetExpr_in_synpred55_wcps1463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryInducedExpr_in_synpred56_wcps1476 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_synpred59_wcps1512 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_synpred60_wcps1524 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageVariable_in_synpred61_wcps1536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstantExpr_in_synpred62_wcps1548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstructorExpr_in_synpred63_wcps1560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_setMetaDataExpr_in_synpred64_wcps1573 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldExpr_in_synpred81_wcps2269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryArithmeticExpr_in_synpred82_wcps2278 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanExpr_in_synpred85_wcps2314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_trimExpr_in_synpred111_wcps2834 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sliceExpr_in_synpred112_wcps2843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_synpred115_wcps2972 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_synpred116_wcps3081 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred130_wcps3388 = new BitSet(new long[]{0x300007F808800040L,0x000010060001D000L});
    public static final BitSet FOLLOW_booleanScalarTerm_in_synpred130_wcps3396 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_synpred131_wcps3428 = new BitSet(new long[]{0x300007F808800040L,0x000010060001D000L});
    public static final BitSet FOLLOW_booleanScalarNegation_in_synpred131_wcps3432 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred133_wcps3481 = new BitSet(new long[]{0x300007F808800040L,0x000010060001D000L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_synpred133_wcps3485 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_synpred133_wcps3487 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericScalarExpr_in_synpred135_wcps3514 = new BitSet(new long[]{0x00000000003F0000L});
    public static final BitSet FOLLOW_compOp_in_synpred135_wcps3518 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarExpr_in_synpred135_wcps3522 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred137_wcps3559 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarTerm_in_synpred137_wcps3567 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred139_wcps3595 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarFactor_in_synpred139_wcps3603 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred140_wcps3623 = new BitSet(new long[]{0x300007F800800040L,0x000010040000C000L});
    public static final BitSet FOLLOW_numericScalarExpr_in_synpred140_wcps3627 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_synpred140_wcps3629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_complexConstant_in_synpred147_wcps3741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_synpred156_wcps3929 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_synpred156_wcps3931 = new BitSet(new long[]{0x300007FFF8801840L,0x000010060001D000L});
    public static final BitSet FOLLOW_scalarExpr_in_synpred156_wcps3935 = new BitSet(new long[]{0x0000000000000002L});

}