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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
// $ANTLR 3.2 Sep 23, 2009 12:02:23 wcps.g 2013-11-18 18:11:19
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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "FOR", "IN", "LPAREN", "RPAREN", "COMMA", "WHERE", "RETURN", "ENCODE", "STORE", "OR", "XOR", "AND", "EQUALS", "NOTEQUALS", "LT", "GT", "LTE", "GTE", "PLUS", "MINUS", "MULT", "DIVIDE", "OVERLAY", "IDENTIFIER", "IMAGECRS", "IMAGECRSDOMAIN", "CRSSET", "NULLSET", "INTERPOLATIONDEFAULT", "INTERPOLATIONSET", "DOMAIN", "ALL", "SOME", "COUNT", "ADD", "AVG", "MIN", "MAX", "CONDENSE", "OVER", "USING", "COLON", "COVERAGE", "VALUE", "LIST", "SEMICOLON", "VALUES", "SETIDENTIFIER", "SETCRSSET", "SETNULLSET", "SETINTERPOLATIONDEFAULT", "SETINTERPOLATIONSET", "LBRACE", "RBRACE", "STRUCT", "CRSTRANSFORM", "SQRT", "ABS", "RE", "IM", "EXP", "LOG", "LN", "POW", "FLOATCONSTANT", "INTEGERCONSTANT", "SIN", "COS", "TAN", "SINH", "COSH", "TANH", "ARCSIN", "ARCCOS", "ARCTAN", "NOT", "BIT", "ROUND", "STRING", "SCALE", "LBRACKET", "RBRACKET", "TRIM", "SLICE", "EXTEND", "BOOLEAN", "CHAR", "SHORT", "LONG", "FLOAT", "DOUBLE", "COMPLEX", "COMPLEX2", "UNSIGNED", "DOT", "BOOLEANCONSTANT", "NEAREST", "LINEAR", "QUADRATIC", "CUBIC", "FULL", "NONE", "HALF", "OTHER", "NAME", "VARIABLE_DOLLAR", "SWITCH", "CASE", "DEFAULT", "NULLDEFAULT", "PHI", "WHITESPACE"
    };
    public static final int IMAGECRS=28;
    public static final int LT=18;
    public static final int LN=66;
    public static final int LOG=65;
    public static final int CASE=111;
    public static final int CHAR=90;
    public static final int COMPLEX=95;
    public static final int SETINTERPOLATIONDEFAULT=54;
    public static final int COUNT=37;
    public static final int EQUALS=16;
    public static final int COSH=74;
    public static final int NOT=79;
    public static final int INTEGERCONSTANT=69;
    public static final int EOF=-1;
    public static final int SINH=73;
    public static final int LBRACKET=84;
    public static final int RPAREN=7;
    public static final int NAME=108;
    public static final int LINEAR=101;
    public static final int TANH=75;
    public static final int FULL=104;
    public static final int POW=67;
    public static final int USING=44;
    public static final int SIN=70;
    public static final int EXP=64;
    public static final int COS=71;
    public static final int TAN=72;
    public static final int RETURN=10;
    public static final int DOUBLE=94;
    public static final int NULLDEFAULT=113;
    public static final int DIVIDE=25;
    public static final int STORE=12;
    public static final int BOOLEANCONSTANT=99;
    public static final int RBRACE=57;
    public static final int SETNULLSET=53;
    public static final int SWITCH=110;
    public static final int CONDENSE=42;
    public static final int WHITESPACE=115;
    public static final int SEMICOLON=49;
    public static final int MULT=24;
    public static final int VALUE=47;
    public static final int LIST=48;
    public static final int COMPLEX2=96;
    public static final int ABS=61;
    public static final int CRSSET=30;
    public static final int SCALE=83;
    public static final int VARIABLE_DOLLAR=109;
    public static final int FLOATCONSTANT=68;
    public static final int IMAGECRSDOMAIN=29;
    public static final int NONE=105;
    public static final int OR=13;
    public static final int TRIM=86;
    public static final int GT=19;
    public static final int ROUND=81;
    public static final int QUADRATIC=102;
    public static final int ENCODE=11;
    public static final int PHI=114;
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
    public static final int FLOAT=93;
    public static final int SLICE=87;
    public static final int AND=15;
    public static final int LTE=20;
    public static final int LPAREN=6;
    public static final int EXTEND=88;
    public static final int IM=63;
    public static final int BOOLEAN=89;
    public static final int IN=5;
    public static final int COMMA=8;
    public static final int AVG=39;
    public static final int IDENTIFIER=27;
    public static final int SOME=36;
    public static final int ALL=35;
    public static final int ARCSIN=76;
    public static final int PLUS=22;
    public static final int ARCCOS=77;
    public static final int RBRACKET=85;
    public static final int DOT=98;
    public static final int ADD=38;
    public static final int SETIDENTIFIER=51;
    public static final int XOR=14;
    public static final int SETINTERPOLATIONSET=55;
    public static final int OTHER=107;
    public static final int DEFAULT=112;
    public static final int VALUES=50;
    public static final int ARCTAN=78;
    public static final int NOTEQUALS=17;
    public static final int SHORT=91;
    public static final int STRUCT=58;
    public static final int MIN=40;
    public static final int MINUS=23;
    public static final int SQRT=60;
    public static final int DOMAIN=34;
    public static final int CRSTRANSFORM=59;
    public static final int COLON=45;
    public static final int CUBIC=103;
    public static final int UNSIGNED=97;
    public static final int NULLSET=31;
    public static final int BIT=80;
    public static final int LONG=92;
    public static final int INTERPOLATIONSET=33;
    public static final int SETCRSSET=52;
    public static final int HALF=106;
    public static final int STRING=82;
    public static final int NEAREST=100;

    // delegates
    // delegators


        public wcpsParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public wcpsParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            this.state.ruleMemo = new HashMap[268+1];
             
             
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
    // wcps.g:94:1: coverageValue returns [CoverageExpr value] : (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom | e6= switchExpr );
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

        wcpsParser.switchExpr_return e6 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 15) ) { return retval; }
            // wcps.g:95:5: (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom | e6= switchExpr )
            int alt12=6;
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
                case 6 :
                    // wcps.g:100:7: e6= switchExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_switchExpr_in_coverageValue716);
                    e6=switchExpr();

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
    // wcps.g:102:1: coverageAtom returns [CoverageExpr value] : (e2= scalarExpr | e1= coverageVariable | LPAREN e7= coverageExpr RPAREN | e3= coverageConstantExpr | e4= coverageConstructorExpr | e5= setMetaDataExpr | e6= rangeConstructorExpr );
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
            // wcps.g:103:5: (e2= scalarExpr | e1= coverageVariable | LPAREN e7= coverageExpr RPAREN | e3= coverageConstantExpr | e4= coverageConstructorExpr | e5= setMetaDataExpr | e6= rangeConstructorExpr )
            int alt13=7;
            alt13 = dfa13.predict(input);
            switch (alt13) {
                case 1 :
                    // wcps.g:103:7: e2= scalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_scalarExpr_in_coverageAtom739);
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
                    // wcps.g:104:7: e1= coverageVariable
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageVariable_in_coverageAtom751);
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
                    // wcps.g:105:7: LPAREN e7= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    LPAREN20=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_coverageAtom761); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN20_tree = (Object)adaptor.create(LPAREN20);
                    adaptor.addChild(root_0, LPAREN20_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_coverageAtom765);
                    e7=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e7.getTree());
                    RPAREN21=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_coverageAtom767); if (state.failed) return retval;
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
                    // wcps.g:106:7: e3= coverageConstantExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageConstantExpr_in_coverageAtom780);
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
                    // wcps.g:107:7: e4= coverageConstructorExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageConstructorExpr_in_coverageAtom792);
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
                    // wcps.g:108:7: e5= setMetaDataExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_setMetaDataExpr_in_coverageAtom805);
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
                    // wcps.g:109:7: e6= rangeConstructorExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_rangeConstructorExpr_in_coverageAtom818);
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
    // wcps.g:111:1: scalarExpr returns [ScalarExpr value] : (e1= metaDataExpr | e2= condenseExpr | e3= booleanScalarExpr | e4= numericScalarExpr | e5= stringScalarExpr | LPAREN e6= scalarExpr RPAREN );
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
            // wcps.g:112:5: (e1= metaDataExpr | e2= condenseExpr | e3= booleanScalarExpr | e4= numericScalarExpr | e5= stringScalarExpr | LPAREN e6= scalarExpr RPAREN )
            int alt14=6;
            alt14 = dfa14.predict(input);
            switch (alt14) {
                case 1 :
                    // wcps.g:112:7: e1= metaDataExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_metaDataExpr_in_scalarExpr842);
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
                    // wcps.g:113:7: e2= condenseExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_condenseExpr_in_scalarExpr855);
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
                    // wcps.g:114:7: e3= booleanScalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_booleanScalarExpr_in_scalarExpr868);
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
                    // wcps.g:115:7: e4= numericScalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_numericScalarExpr_in_scalarExpr882);
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
                    // wcps.g:116:7: e5= stringScalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_stringScalarExpr_in_scalarExpr895);
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
                    // wcps.g:117:7: LPAREN e6= scalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    LPAREN22=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_scalarExpr906); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN22_tree = (Object)adaptor.create(LPAREN22);
                    adaptor.addChild(root_0, LPAREN22_tree);
                    }
                    pushFollow(FOLLOW_scalarExpr_in_scalarExpr910);
                    e6=scalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e6.getTree());
                    RPAREN23=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_scalarExpr912); if (state.failed) return retval;
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
    // wcps.g:119:1: metaDataExpr returns [MetaDataExpr value] : (op= IDENTIFIER LPAREN e1= coverageExpr RPAREN | op= IMAGECRS LPAREN e1= coverageExpr RPAREN | op= IMAGECRSDOMAIN LPAREN e1= coverageExpr ( COMMA e2= axisName )? RPAREN | op= CRSSET LPAREN e1= coverageExpr RPAREN | de= domainExpr | op= NULLSET LPAREN e1= coverageExpr RPAREN | op= INTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN | op= INTERPOLATIONSET LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN );
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
            // wcps.g:120:5: (op= IDENTIFIER LPAREN e1= coverageExpr RPAREN | op= IMAGECRS LPAREN e1= coverageExpr RPAREN | op= IMAGECRSDOMAIN LPAREN e1= coverageExpr ( COMMA e2= axisName )? RPAREN | op= CRSSET LPAREN e1= coverageExpr RPAREN | de= domainExpr | op= NULLSET LPAREN e1= coverageExpr RPAREN | op= INTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN | op= INTERPOLATIONSET LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN )
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
                    // wcps.g:120:7: op= IDENTIFIER LPAREN e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_metaDataExpr936); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN24=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr938); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN24_tree = (Object)adaptor.create(LPAREN24);
                    adaptor.addChild(root_0, LPAREN24_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr942);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN25=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr944); if (state.failed) return retval;
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
                    // wcps.g:121:7: op= IMAGECRS LPAREN e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,IMAGECRS,FOLLOW_IMAGECRS_in_metaDataExpr956); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN26=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr958); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN26_tree = (Object)adaptor.create(LPAREN26);
                    adaptor.addChild(root_0, LPAREN26_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr962);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN27=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr964); if (state.failed) return retval;
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
                    // wcps.g:122:7: op= IMAGECRSDOMAIN LPAREN e1= coverageExpr ( COMMA e2= axisName )? RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,IMAGECRSDOMAIN,FOLLOW_IMAGECRSDOMAIN_in_metaDataExpr976); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN28=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr978); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN28_tree = (Object)adaptor.create(LPAREN28);
                    adaptor.addChild(root_0, LPAREN28_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr982);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    // wcps.g:122:48: ( COMMA e2= axisName )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==COMMA) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // wcps.g:122:49: COMMA e2= axisName
                            {
                            COMMA29=(Token)match(input,COMMA,FOLLOW_COMMA_in_metaDataExpr985); if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                            COMMA29_tree = (Object)adaptor.create(COMMA29);
                            adaptor.addChild(root_0, COMMA29_tree);
                            }
                            pushFollow(FOLLOW_axisName_in_metaDataExpr989);
                            e2=axisName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());

                            }
                            break;

                    }

                    RPAREN30=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr993); if (state.failed) return retval;
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
                    // wcps.g:123:7: op= CRSSET LPAREN e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,CRSSET,FOLLOW_CRSSET_in_metaDataExpr1005); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN31=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr1007); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN31_tree = (Object)adaptor.create(LPAREN31);
                    adaptor.addChild(root_0, LPAREN31_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr1011);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN32=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr1013); if (state.failed) return retval;
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
                    // wcps.g:124:7: de= domainExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_domainExpr_in_metaDataExpr1025);
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
                    // wcps.g:125:7: op= NULLSET LPAREN e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,NULLSET,FOLLOW_NULLSET_in_metaDataExpr1037); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN33=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr1039); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN33_tree = (Object)adaptor.create(LPAREN33);
                    adaptor.addChild(root_0, LPAREN33_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr1043);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN34=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr1045); if (state.failed) return retval;
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
                    // wcps.g:126:7: op= INTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,INTERPOLATIONDEFAULT,FOLLOW_INTERPOLATIONDEFAULT_in_metaDataExpr1057); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN35=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr1059); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN35_tree = (Object)adaptor.create(LPAREN35);
                    adaptor.addChild(root_0, LPAREN35_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr1063);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA36=(Token)match(input,COMMA,FOLLOW_COMMA_in_metaDataExpr1065); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA36_tree = (Object)adaptor.create(COMMA36);
                    adaptor.addChild(root_0, COMMA36_tree);
                    }
                    pushFollow(FOLLOW_fieldName_in_metaDataExpr1069);
                    f1=fieldName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, f1.getTree());
                    RPAREN37=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr1071); if (state.failed) return retval;
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
                    // wcps.g:127:7: op= INTERPOLATIONSET LPAREN e1= coverageExpr COMMA f1= fieldName RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,INTERPOLATIONSET,FOLLOW_INTERPOLATIONSET_in_metaDataExpr1083); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN38=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_metaDataExpr1085); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN38_tree = (Object)adaptor.create(LPAREN38);
                    adaptor.addChild(root_0, LPAREN38_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_metaDataExpr1089);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA39=(Token)match(input,COMMA,FOLLOW_COMMA_in_metaDataExpr1091); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA39_tree = (Object)adaptor.create(COMMA39);
                    adaptor.addChild(root_0, COMMA39_tree);
                    }
                    pushFollow(FOLLOW_fieldName_in_metaDataExpr1095);
                    f1=fieldName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, f1.getTree());
                    RPAREN40=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_metaDataExpr1097); if (state.failed) return retval;
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
    // wcps.g:129:1: domainExpr returns [DomainExpr value] : DOMAIN LPAREN var= coverageVariable COMMA axis= axisName COMMA crs= crsName RPAREN ;
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
            // wcps.g:130:2: ( DOMAIN LPAREN var= coverageVariable COMMA axis= axisName COMMA crs= crsName RPAREN )
            // wcps.g:130:4: DOMAIN LPAREN var= coverageVariable COMMA axis= axisName COMMA crs= crsName RPAREN
            {
            root_0 = (Object)adaptor.nil();

            DOMAIN41=(Token)match(input,DOMAIN,FOLLOW_DOMAIN_in_domainExpr1116); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DOMAIN41_tree = (Object)adaptor.create(DOMAIN41);
            adaptor.addChild(root_0, DOMAIN41_tree);
            }
            LPAREN42=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_domainExpr1118); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN42_tree = (Object)adaptor.create(LPAREN42);
            adaptor.addChild(root_0, LPAREN42_tree);
            }
            pushFollow(FOLLOW_coverageVariable_in_domainExpr1122);
            var=coverageVariable();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, var.getTree());
            COMMA43=(Token)match(input,COMMA,FOLLOW_COMMA_in_domainExpr1124); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA43_tree = (Object)adaptor.create(COMMA43);
            adaptor.addChild(root_0, COMMA43_tree);
            }
            pushFollow(FOLLOW_axisName_in_domainExpr1128);
            axis=axisName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, axis.getTree());
            COMMA44=(Token)match(input,COMMA,FOLLOW_COMMA_in_domainExpr1130); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA44_tree = (Object)adaptor.create(COMMA44);
            adaptor.addChild(root_0, COMMA44_tree);
            }
            pushFollow(FOLLOW_crsName_in_domainExpr1134);
            crs=crsName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, crs.getTree());
            RPAREN45=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_domainExpr1136); if (state.failed) return retval;
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
    // wcps.g:132:1: condenseExpr returns [CondenseExpr value] : (e1= reduceExpr | e2= generalCondenseExpr );
    public final wcpsParser.condenseExpr_return condenseExpr() throws RecognitionException {
        wcpsParser.condenseExpr_return retval = new wcpsParser.condenseExpr_return();
        retval.start = input.LT(1);
        int condenseExpr_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.reduceExpr_return e1 = null;

        wcpsParser.generalCondenseExpr_return e2 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 20) ) { return retval; }
            // wcps.g:133:2: (e1= reduceExpr | e2= generalCondenseExpr )
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
                    // wcps.g:133:4: e1= reduceExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_reduceExpr_in_condenseExpr1153);
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
                    // wcps.g:134:4: e2= generalCondenseExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_generalCondenseExpr_in_condenseExpr1162);
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
    // wcps.g:136:1: reduceExpr returns [ReduceExpr value] : op= ( ALL | SOME | COUNT | ADD | AVG | MIN | MAX ) LPAREN e1= coverageExpr RPAREN ;
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
            // wcps.g:137:2: (op= ( ALL | SOME | COUNT | ADD | AVG | MIN | MAX ) LPAREN e1= coverageExpr RPAREN )
            // wcps.g:137:4: op= ( ALL | SOME | COUNT | ADD | AVG | MIN | MAX ) LPAREN e1= coverageExpr RPAREN
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

            LPAREN46=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_reduceExpr1195); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN46_tree = (Object)adaptor.create(LPAREN46);
            adaptor.addChild(root_0, LPAREN46_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_reduceExpr1199);
            e1=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            RPAREN47=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_reduceExpr1201); if (state.failed) return retval;
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
    // wcps.g:139:1: generalCondenseExpr returns [GeneralCondenseExpr value] : CONDENSE op= condenseOpType OVER ail= axisIteratorList ( WHERE cond= booleanScalarExpr )? USING ce= coverageExpr ;
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
            // wcps.g:140:2: ( CONDENSE op= condenseOpType OVER ail= axisIteratorList ( WHERE cond= booleanScalarExpr )? USING ce= coverageExpr )
            // wcps.g:140:4: CONDENSE op= condenseOpType OVER ail= axisIteratorList ( WHERE cond= booleanScalarExpr )? USING ce= coverageExpr
            {
            root_0 = (Object)adaptor.nil();

            CONDENSE48=(Token)match(input,CONDENSE,FOLLOW_CONDENSE_in_generalCondenseExpr1216); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CONDENSE48_tree = (Object)adaptor.create(CONDENSE48);
            adaptor.addChild(root_0, CONDENSE48_tree);
            }
            pushFollow(FOLLOW_condenseOpType_in_generalCondenseExpr1220);
            op=condenseOpType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, op.getTree());
            OVER49=(Token)match(input,OVER,FOLLOW_OVER_in_generalCondenseExpr1222); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            OVER49_tree = (Object)adaptor.create(OVER49);
            adaptor.addChild(root_0, OVER49_tree);
            }
            pushFollow(FOLLOW_axisIteratorList_in_generalCondenseExpr1226);
            ail=axisIteratorList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ail.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new GeneralCondenseExpr((op!=null?op.value:null), (ail!=null?ail.value:null)); 
            }
            // wcps.g:141:3: ( WHERE cond= booleanScalarExpr )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==WHERE) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // wcps.g:141:4: WHERE cond= booleanScalarExpr
                    {
                    WHERE50=(Token)match(input,WHERE,FOLLOW_WHERE_in_generalCondenseExpr1233); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    WHERE50_tree = (Object)adaptor.create(WHERE50);
                    adaptor.addChild(root_0, WHERE50_tree);
                    }
                    pushFollow(FOLLOW_booleanScalarExpr_in_generalCondenseExpr1237);
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

            USING51=(Token)match(input,USING,FOLLOW_USING_in_generalCondenseExpr1245); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            USING51_tree = (Object)adaptor.create(USING51);
            adaptor.addChild(root_0, USING51_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_generalCondenseExpr1249);
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
    // wcps.g:144:1: axisIteratorList returns [AxisIteratorList value] : vn= variableName an= axisName LPAREN ie= intervalExpr RPAREN ( COMMA vn2= variableName an2= axisName LPAREN ie2= intervalExpr RPAREN )* ;
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
            // wcps.g:145:2: (vn= variableName an= axisName LPAREN ie= intervalExpr RPAREN ( COMMA vn2= variableName an2= axisName LPAREN ie2= intervalExpr RPAREN )* )
            // wcps.g:145:4: vn= variableName an= axisName LPAREN ie= intervalExpr RPAREN ( COMMA vn2= variableName an2= axisName LPAREN ie2= intervalExpr RPAREN )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_variableName_in_axisIteratorList1266);
            vn=variableName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, vn.getTree());
            pushFollow(FOLLOW_axisName_in_axisIteratorList1270);
            an=axisName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, an.getTree());
            LPAREN52=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_axisIteratorList1272); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN52_tree = (Object)adaptor.create(LPAREN52);
            adaptor.addChild(root_0, LPAREN52_tree);
            }
            pushFollow(FOLLOW_intervalExpr_in_axisIteratorList1276);
            ie=intervalExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ie.getTree());
            RPAREN53=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_axisIteratorList1278); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN53_tree = (Object)adaptor.create(RPAREN53);
            adaptor.addChild(root_0, RPAREN53_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new AxisIteratorList(new AxisIterator((vn!=null?vn.value:null), (an!=null?an.value:null), (ie!=null?ie.value:null))); 
            }
            // wcps.g:147:2: ( COMMA vn2= variableName an2= axisName LPAREN ie2= intervalExpr RPAREN )*
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==COMMA) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // wcps.g:147:3: COMMA vn2= variableName an2= axisName LPAREN ie2= intervalExpr RPAREN
            	    {
            	    COMMA54=(Token)match(input,COMMA,FOLLOW_COMMA_in_axisIteratorList1286); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA54_tree = (Object)adaptor.create(COMMA54);
            	    adaptor.addChild(root_0, COMMA54_tree);
            	    }
            	    pushFollow(FOLLOW_variableName_in_axisIteratorList1290);
            	    vn2=variableName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, vn2.getTree());
            	    pushFollow(FOLLOW_axisName_in_axisIteratorList1294);
            	    an2=axisName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, an2.getTree());
            	    LPAREN55=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_axisIteratorList1296); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    LPAREN55_tree = (Object)adaptor.create(LPAREN55);
            	    adaptor.addChild(root_0, LPAREN55_tree);
            	    }
            	    pushFollow(FOLLOW_intervalExpr_in_axisIteratorList1300);
            	    ie2=intervalExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, ie2.getTree());
            	    RPAREN56=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_axisIteratorList1302); if (state.failed) return retval;
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
    // wcps.g:150:1: intervalExpr returns [IntervalExpr value] : (lo= indexExpr COLON hi= indexExpr | IMAGECRSDOMAIN LPAREN e1= coverageExpr COMMA e2= axisName RPAREN );
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

        wcpsParser.coverageExpr_return e1 = null;

        wcpsParser.axisName_return e2 = null;


        Object COLON57_tree=null;
        Object IMAGECRSDOMAIN58_tree=null;
        Object LPAREN59_tree=null;
        Object COMMA60_tree=null;
        Object RPAREN61_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 24) ) { return retval; }
            // wcps.g:151:5: (lo= indexExpr COLON hi= indexExpr | IMAGECRSDOMAIN LPAREN e1= coverageExpr COMMA e2= axisName RPAREN )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==LPAREN||LA20_0==INTEGERCONSTANT||LA20_0==ROUND) ) {
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
                    // wcps.g:151:7: lo= indexExpr COLON hi= indexExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_indexExpr_in_intervalExpr1326);
                    lo=indexExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, lo.getTree());
                    COLON57=(Token)match(input,COLON,FOLLOW_COLON_in_intervalExpr1328); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON57_tree = (Object)adaptor.create(COLON57);
                    adaptor.addChild(root_0, COLON57_tree);
                    }
                    pushFollow(FOLLOW_indexExpr_in_intervalExpr1332);
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
                    // wcps.g:153:7: IMAGECRSDOMAIN LPAREN e1= coverageExpr COMMA e2= axisName RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    IMAGECRSDOMAIN58=(Token)match(input,IMAGECRSDOMAIN,FOLLOW_IMAGECRSDOMAIN_in_intervalExpr1347); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    IMAGECRSDOMAIN58_tree = (Object)adaptor.create(IMAGECRSDOMAIN58);
                    adaptor.addChild(root_0, IMAGECRSDOMAIN58_tree);
                    }
                    LPAREN59=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_intervalExpr1349); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN59_tree = (Object)adaptor.create(LPAREN59);
                    adaptor.addChild(root_0, LPAREN59_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_intervalExpr1353);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA60=(Token)match(input,COMMA,FOLLOW_COMMA_in_intervalExpr1355); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA60_tree = (Object)adaptor.create(COMMA60);
                    adaptor.addChild(root_0, COMMA60_tree);
                    }
                    pushFollow(FOLLOW_axisName_in_intervalExpr1359);
                    e2=axisName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    RPAREN61=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_intervalExpr1361); if (state.failed) return retval;
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
    // wcps.g:156:1: coverageConstantExpr returns [CoverageConstantExpr value] : COVERAGE aname= coverageName OVER iter= axisIteratorList VALUE LIST LT values= constantList GT ;
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
            // wcps.g:157:5: ( COVERAGE aname= coverageName OVER iter= axisIteratorList VALUE LIST LT values= constantList GT )
            // wcps.g:157:7: COVERAGE aname= coverageName OVER iter= axisIteratorList VALUE LIST LT values= constantList GT
            {
            root_0 = (Object)adaptor.nil();

            COVERAGE62=(Token)match(input,COVERAGE,FOLLOW_COVERAGE_in_coverageConstantExpr1387); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COVERAGE62_tree = (Object)adaptor.create(COVERAGE62);
            adaptor.addChild(root_0, COVERAGE62_tree);
            }
            pushFollow(FOLLOW_coverageName_in_coverageConstantExpr1391);
            aname=coverageName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
            OVER63=(Token)match(input,OVER,FOLLOW_OVER_in_coverageConstantExpr1393); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            OVER63_tree = (Object)adaptor.create(OVER63);
            adaptor.addChild(root_0, OVER63_tree);
            }
            pushFollow(FOLLOW_axisIteratorList_in_coverageConstantExpr1397);
            iter=axisIteratorList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, iter.getTree());
            VALUE64=(Token)match(input,VALUE,FOLLOW_VALUE_in_coverageConstantExpr1399); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            VALUE64_tree = (Object)adaptor.create(VALUE64);
            adaptor.addChild(root_0, VALUE64_tree);
            }
            LIST65=(Token)match(input,LIST,FOLLOW_LIST_in_coverageConstantExpr1401); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LIST65_tree = (Object)adaptor.create(LIST65);
            adaptor.addChild(root_0, LIST65_tree);
            }
            LT66=(Token)match(input,LT,FOLLOW_LT_in_coverageConstantExpr1403); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LT66_tree = (Object)adaptor.create(LT66);
            adaptor.addChild(root_0, LT66_tree);
            }
            pushFollow(FOLLOW_constantList_in_coverageConstantExpr1407);
            values=constantList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, values.getTree());
            GT67=(Token)match(input,GT,FOLLOW_GT_in_coverageConstantExpr1409); if (state.failed) return retval;
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
    // wcps.g:160:1: constantList returns [ConstantList value] : c= constant ( SEMICOLON c= constant )* ;
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
            // wcps.g:161:5: (c= constant ( SEMICOLON c= constant )* )
            // wcps.g:161:7: c= constant ( SEMICOLON c= constant )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_constant_in_constantList1440);
            c=constant();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, c.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new ConstantList((c!=null?c.value:null)); 
            }
            // wcps.g:161:59: ( SEMICOLON c= constant )*
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);

                if ( (LA21_0==SEMICOLON) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // wcps.g:161:60: SEMICOLON c= constant
            	    {
            	    SEMICOLON68=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_constantList1445); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    SEMICOLON68_tree = (Object)adaptor.create(SEMICOLON68);
            	    adaptor.addChild(root_0, SEMICOLON68_tree);
            	    }
            	    pushFollow(FOLLOW_constant_in_constantList1449);
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
    // wcps.g:164:1: coverageAtomConstructor returns [CoverageExpr value] : (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom | e6= scalarExpr | e7= coverageVariable | e8= coverageConstantExpr | e9= coverageConstructorExpr | e10= setMetaDataExpr | e11= rangeConstructorExpr | e12= switchExpr );
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

        wcpsParser.switchExpr_return e12 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 27) ) { return retval; }
            // wcps.g:165:5: (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom | e6= scalarExpr | e7= coverageVariable | e8= coverageConstantExpr | e9= coverageConstructorExpr | e10= setMetaDataExpr | e11= rangeConstructorExpr | e12= switchExpr )
            int alt22=12;
            alt22 = dfa22.predict(input);
            switch (alt22) {
                case 1 :
                    // wcps.g:165:7: e5= subsetExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_subsetExpr_in_coverageAtomConstructor1475);
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
                    // wcps.g:166:7: e2= unaryInducedExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_unaryInducedExpr_in_coverageAtomConstructor1488);
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
                    // wcps.g:167:7: e4= scaleExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_scaleExpr_in_coverageAtomConstructor1500);
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
                    // wcps.g:168:7: e3= crsTransformExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_crsTransformExpr_in_coverageAtomConstructor1512);
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
                    // wcps.g:169:7: e1= coverageAtom
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageAtom_in_coverageAtomConstructor1524);
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
                    // wcps.g:170:7: e6= scalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_scalarExpr_in_coverageAtomConstructor1536);
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
                    // wcps.g:171:7: e7= coverageVariable
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageVariable_in_coverageAtomConstructor1548);
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
                    // wcps.g:172:7: e8= coverageConstantExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageConstantExpr_in_coverageAtomConstructor1560);
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
                    // wcps.g:173:7: e9= coverageConstructorExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageConstructorExpr_in_coverageAtomConstructor1572);
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
                    // wcps.g:174:7: e10= setMetaDataExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_setMetaDataExpr_in_coverageAtomConstructor1585);
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
                    // wcps.g:175:7: e11= rangeConstructorExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_rangeConstructorExpr_in_coverageAtomConstructor1598);
                    e11=rangeConstructorExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e11.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e11!=null?e11.value:null)); 
                    }

                    }
                    break;
                case 12 :
                    // wcps.g:176:7: e12= switchExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_switchExpr_in_coverageAtomConstructor1611);
                    e12=switchExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e12.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new CoverageExpr((e12!=null?e12.value:null)); 
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
    // wcps.g:178:1: coverageConstructorExpr returns [CoverageConstructorExpr value] : COVERAGE coverage= coverageName OVER ail= axisIteratorList VALUES se= coverageAtomConstructor ;
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
            // wcps.g:179:2: ( COVERAGE coverage= coverageName OVER ail= axisIteratorList VALUES se= coverageAtomConstructor )
            // wcps.g:179:4: COVERAGE coverage= coverageName OVER ail= axisIteratorList VALUES se= coverageAtomConstructor
            {
            root_0 = (Object)adaptor.nil();

            COVERAGE69=(Token)match(input,COVERAGE,FOLLOW_COVERAGE_in_coverageConstructorExpr1629); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COVERAGE69_tree = (Object)adaptor.create(COVERAGE69);
            adaptor.addChild(root_0, COVERAGE69_tree);
            }
            pushFollow(FOLLOW_coverageName_in_coverageConstructorExpr1633);
            coverage=coverageName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, coverage.getTree());
            OVER70=(Token)match(input,OVER,FOLLOW_OVER_in_coverageConstructorExpr1635); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            OVER70_tree = (Object)adaptor.create(OVER70);
            adaptor.addChild(root_0, OVER70_tree);
            }
            pushFollow(FOLLOW_axisIteratorList_in_coverageConstructorExpr1639);
            ail=axisIteratorList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, ail.getTree());
            VALUES71=(Token)match(input,VALUES,FOLLOW_VALUES_in_coverageConstructorExpr1641); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            VALUES71_tree = (Object)adaptor.create(VALUES71);
            adaptor.addChild(root_0, VALUES71_tree);
            }
            pushFollow(FOLLOW_coverageAtomConstructor_in_coverageConstructorExpr1645);
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
    // wcps.g:182:1: setMetaDataExpr returns [SetMetaDataExpr value] : (op= SETIDENTIFIER LPAREN s= stringConstant COMMA e1= coverageExpr RPAREN | op= SETCRSSET LPAREN e1= coverageExpr COMMA crs= crsList RPAREN | op= SETNULLSET LPAREN e1= coverageExpr COMMA rel= rangeExprList RPAREN | op= SETINTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA fn= fieldName COMMA im= interpolationMethod RPAREN | op= SETINTERPOLATIONSET LPAREN e1= coverageExpr COMMA fn= fieldName COMMA iml= interpolationMethodList RPAREN );
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
            // wcps.g:183:5: (op= SETIDENTIFIER LPAREN s= stringConstant COMMA e1= coverageExpr RPAREN | op= SETCRSSET LPAREN e1= coverageExpr COMMA crs= crsList RPAREN | op= SETNULLSET LPAREN e1= coverageExpr COMMA rel= rangeExprList RPAREN | op= SETINTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA fn= fieldName COMMA im= interpolationMethod RPAREN | op= SETINTERPOLATIONSET LPAREN e1= coverageExpr COMMA fn= fieldName COMMA iml= interpolationMethodList RPAREN )
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
                    // wcps.g:183:7: op= SETIDENTIFIER LPAREN s= stringConstant COMMA e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SETIDENTIFIER,FOLLOW_SETIDENTIFIER_in_setMetaDataExpr1667); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN72=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_setMetaDataExpr1669); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN72_tree = (Object)adaptor.create(LPAREN72);
                    adaptor.addChild(root_0, LPAREN72_tree);
                    }
                    pushFollow(FOLLOW_stringConstant_in_setMetaDataExpr1673);
                    s=stringConstant();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, s.getTree());
                    COMMA73=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1675); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA73_tree = (Object)adaptor.create(COMMA73);
                    adaptor.addChild(root_0, COMMA73_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_setMetaDataExpr1679);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN74=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_setMetaDataExpr1681); if (state.failed) return retval;
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
                    // wcps.g:185:7: op= SETCRSSET LPAREN e1= coverageExpr COMMA crs= crsList RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SETCRSSET,FOLLOW_SETCRSSET_in_setMetaDataExpr1694); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN75=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_setMetaDataExpr1696); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN75_tree = (Object)adaptor.create(LPAREN75);
                    adaptor.addChild(root_0, LPAREN75_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_setMetaDataExpr1700);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA76=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1702); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA76_tree = (Object)adaptor.create(COMMA76);
                    adaptor.addChild(root_0, COMMA76_tree);
                    }
                    pushFollow(FOLLOW_crsList_in_setMetaDataExpr1706);
                    crs=crsList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, crs.getTree());
                    RPAREN77=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_setMetaDataExpr1708); if (state.failed) return retval;
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
                    // wcps.g:187:7: op= SETNULLSET LPAREN e1= coverageExpr COMMA rel= rangeExprList RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SETNULLSET,FOLLOW_SETNULLSET_in_setMetaDataExpr1725); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN78=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_setMetaDataExpr1727); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN78_tree = (Object)adaptor.create(LPAREN78);
                    adaptor.addChild(root_0, LPAREN78_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_setMetaDataExpr1731);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA79=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1733); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA79_tree = (Object)adaptor.create(COMMA79);
                    adaptor.addChild(root_0, COMMA79_tree);
                    }
                    pushFollow(FOLLOW_rangeExprList_in_setMetaDataExpr1737);
                    rel=rangeExprList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rel.getTree());
                    RPAREN80=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_setMetaDataExpr1739); if (state.failed) return retval;
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
                    // wcps.g:189:7: op= SETINTERPOLATIONDEFAULT LPAREN e1= coverageExpr COMMA fn= fieldName COMMA im= interpolationMethod RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SETINTERPOLATIONDEFAULT,FOLLOW_SETINTERPOLATIONDEFAULT_in_setMetaDataExpr1756); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN81=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_setMetaDataExpr1758); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN81_tree = (Object)adaptor.create(LPAREN81);
                    adaptor.addChild(root_0, LPAREN81_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_setMetaDataExpr1762);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA82=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1764); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA82_tree = (Object)adaptor.create(COMMA82);
                    adaptor.addChild(root_0, COMMA82_tree);
                    }
                    pushFollow(FOLLOW_fieldName_in_setMetaDataExpr1768);
                    fn=fieldName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, fn.getTree());
                    COMMA83=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1770); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA83_tree = (Object)adaptor.create(COMMA83);
                    adaptor.addChild(root_0, COMMA83_tree);
                    }
                    pushFollow(FOLLOW_interpolationMethod_in_setMetaDataExpr1774);
                    im=interpolationMethod();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, im.getTree());
                    RPAREN84=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_setMetaDataExpr1776); if (state.failed) return retval;
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
                    // wcps.g:191:7: op= SETINTERPOLATIONSET LPAREN e1= coverageExpr COMMA fn= fieldName COMMA iml= interpolationMethodList RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SETINTERPOLATIONSET,FOLLOW_SETINTERPOLATIONSET_in_setMetaDataExpr1796); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN85=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_setMetaDataExpr1798); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN85_tree = (Object)adaptor.create(LPAREN85);
                    adaptor.addChild(root_0, LPAREN85_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_setMetaDataExpr1802);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA86=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1804); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA86_tree = (Object)adaptor.create(COMMA86);
                    adaptor.addChild(root_0, COMMA86_tree);
                    }
                    pushFollow(FOLLOW_fieldName_in_setMetaDataExpr1808);
                    fn=fieldName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, fn.getTree());
                    COMMA87=(Token)match(input,COMMA,FOLLOW_COMMA_in_setMetaDataExpr1810); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA87_tree = (Object)adaptor.create(COMMA87);
                    adaptor.addChild(root_0, COMMA87_tree);
                    }
                    pushFollow(FOLLOW_interpolationMethodList_in_setMetaDataExpr1814);
                    iml=interpolationMethodList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, iml.getTree());
                    RPAREN88=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_setMetaDataExpr1816); if (state.failed) return retval;
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
    // wcps.g:194:1: crsList returns [CrsList value] : LBRACE (crs= crsName ( COMMA crs= crsName )* )? RBRACE ;
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
            // wcps.g:195:5: ( LBRACE (crs= crsName ( COMMA crs= crsName )* )? RBRACE )
            // wcps.g:195:7: LBRACE (crs= crsName ( COMMA crs= crsName )* )? RBRACE
            {
            root_0 = (Object)adaptor.nil();

            LBRACE89=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_crsList1845); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE89_tree = (Object)adaptor.create(LBRACE89);
            adaptor.addChild(root_0, LBRACE89_tree);
            }
            if ( state.backtracking==0 ) {
              retval.value = new CrsList();
            }
            // wcps.g:195:40: (crs= crsName ( COMMA crs= crsName )* )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==STRING) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // wcps.g:195:41: crs= crsName ( COMMA crs= crsName )*
                    {
                    pushFollow(FOLLOW_crsName_in_crsList1852);
                    crs=crsName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, crs.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.add((crs!=null?crs.value:null)); 
                    }
                    // wcps.g:195:81: ( COMMA crs= crsName )*
                    loop24:
                    do {
                        int alt24=2;
                        int LA24_0 = input.LA(1);

                        if ( (LA24_0==COMMA) ) {
                            alt24=1;
                        }


                        switch (alt24) {
                    	case 1 :
                    	    // wcps.g:195:82: COMMA crs= crsName
                    	    {
                    	    COMMA90=(Token)match(input,COMMA,FOLLOW_COMMA_in_crsList1857); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA90_tree = (Object)adaptor.create(COMMA90);
                    	    adaptor.addChild(root_0, COMMA90_tree);
                    	    }
                    	    pushFollow(FOLLOW_crsName_in_crsList1861);
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

            RBRACE91=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_crsList1870); if (state.failed) return retval;
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
    // wcps.g:197:1: rangeExprList returns [RangeExprList value] : LBRACE (re1= rangeExpr ( COMMA re2= rangeExpr )* )? RBRACE ;
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
            // wcps.g:198:5: ( LBRACE (re1= rangeExpr ( COMMA re2= rangeExpr )* )? RBRACE )
            // wcps.g:198:7: LBRACE (re1= rangeExpr ( COMMA re2= rangeExpr )* )? RBRACE
            {
            root_0 = (Object)adaptor.nil();

            LBRACE92=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_rangeExprList1889); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE92_tree = (Object)adaptor.create(LBRACE92);
            adaptor.addChild(root_0, LBRACE92_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new RangeExprList(); 
            }
            // wcps.g:198:48: (re1= rangeExpr ( COMMA re2= rangeExpr )* )?
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==STRUCT) ) {
                alt27=1;
            }
            switch (alt27) {
                case 1 :
                    // wcps.g:198:49: re1= rangeExpr ( COMMA re2= rangeExpr )*
                    {
                    pushFollow(FOLLOW_rangeExpr_in_rangeExprList1896);
                    re1=rangeExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, re1.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.add((re1!=null?re1.value:null)); 
                    }
                    // wcps.g:198:91: ( COMMA re2= rangeExpr )*
                    loop26:
                    do {
                        int alt26=2;
                        int LA26_0 = input.LA(1);

                        if ( (LA26_0==COMMA) ) {
                            alt26=1;
                        }


                        switch (alt26) {
                    	case 1 :
                    	    // wcps.g:198:92: COMMA re2= rangeExpr
                    	    {
                    	    COMMA93=(Token)match(input,COMMA,FOLLOW_COMMA_in_rangeExprList1901); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA93_tree = (Object)adaptor.create(COMMA93);
                    	    adaptor.addChild(root_0, COMMA93_tree);
                    	    }
                    	    pushFollow(FOLLOW_rangeExpr_in_rangeExprList1905);
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

            RBRACE94=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_rangeExprList1914); if (state.failed) return retval;
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
    // wcps.g:200:1: interpolationMethodList returns [InterpolationMethodList value] : LBRACE (e= interpolationMethod ( COMMA e= interpolationMethod )* )? RBRACE ;
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
            // wcps.g:201:2: ( LBRACE (e= interpolationMethod ( COMMA e= interpolationMethod )* )? RBRACE )
            // wcps.g:201:4: LBRACE (e= interpolationMethod ( COMMA e= interpolationMethod )* )? RBRACE
            {
            root_0 = (Object)adaptor.nil();

            LBRACE95=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_interpolationMethodList1930); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE95_tree = (Object)adaptor.create(LBRACE95);
            adaptor.addChild(root_0, LBRACE95_tree);
            }
            if ( state.backtracking==0 ) {
              retval.value = new InterpolationMethodList();
            }
            // wcps.g:201:53: (e= interpolationMethod ( COMMA e= interpolationMethod )* )?
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==LPAREN) ) {
                alt29=1;
            }
            switch (alt29) {
                case 1 :
                    // wcps.g:201:54: e= interpolationMethod ( COMMA e= interpolationMethod )*
                    {
                    pushFollow(FOLLOW_interpolationMethod_in_interpolationMethodList1937);
                    e=interpolationMethod();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.add((e!=null?e.value:null)); 
                    }
                    // wcps.g:201:102: ( COMMA e= interpolationMethod )*
                    loop28:
                    do {
                        int alt28=2;
                        int LA28_0 = input.LA(1);

                        if ( (LA28_0==COMMA) ) {
                            alt28=1;
                        }


                        switch (alt28) {
                    	case 1 :
                    	    // wcps.g:201:103: COMMA e= interpolationMethod
                    	    {
                    	    COMMA96=(Token)match(input,COMMA,FOLLOW_COMMA_in_interpolationMethodList1942); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA96_tree = (Object)adaptor.create(COMMA96);
                    	    adaptor.addChild(root_0, COMMA96_tree);
                    	    }
                    	    pushFollow(FOLLOW_interpolationMethod_in_interpolationMethodList1946);
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

            RBRACE97=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_interpolationMethodList1954); if (state.failed) return retval;
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
    // wcps.g:203:1: rangeExpr returns [RangeExpr value] : STRUCT LBRACE (field= fieldName COLON expr= scalarExpr ( COMMA field= fieldName COLON expr= scalarExpr )* )? RBRACE ;
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
            // wcps.g:204:2: ( STRUCT LBRACE (field= fieldName COLON expr= scalarExpr ( COMMA field= fieldName COLON expr= scalarExpr )* )? RBRACE )
            // wcps.g:204:4: STRUCT LBRACE (field= fieldName COLON expr= scalarExpr ( COMMA field= fieldName COLON expr= scalarExpr )* )? RBRACE
            {
            root_0 = (Object)adaptor.nil();

            STRUCT98=(Token)match(input,STRUCT,FOLLOW_STRUCT_in_rangeExpr1967); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            STRUCT98_tree = (Object)adaptor.create(STRUCT98);
            adaptor.addChild(root_0, STRUCT98_tree);
            }
            LBRACE99=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_rangeExpr1969); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE99_tree = (Object)adaptor.create(LBRACE99);
            adaptor.addChild(root_0, LBRACE99_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value =new RangeExpr(); 
            }
            // wcps.g:205:2: (field= fieldName COLON expr= scalarExpr ( COMMA field= fieldName COLON expr= scalarExpr )* )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==INTEGERCONSTANT||LA31_0==STRING||LA31_0==NAME) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // wcps.g:205:3: field= fieldName COLON expr= scalarExpr ( COMMA field= fieldName COLON expr= scalarExpr )*
                    {
                    pushFollow(FOLLOW_fieldName_in_rangeExpr1977);
                    field=fieldName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, field.getTree());
                    COLON100=(Token)match(input,COLON,FOLLOW_COLON_in_rangeExpr1979); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON100_tree = (Object)adaptor.create(COLON100);
                    adaptor.addChild(root_0, COLON100_tree);
                    }
                    pushFollow(FOLLOW_scalarExpr_in_rangeExpr1983);
                    expr=scalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, expr.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value.add((field!=null?field.value:null), (expr!=null?expr.value:null)); 
                    }
                    // wcps.g:206:3: ( COMMA field= fieldName COLON expr= scalarExpr )*
                    loop30:
                    do {
                        int alt30=2;
                        int LA30_0 = input.LA(1);

                        if ( (LA30_0==COMMA) ) {
                            alt30=1;
                        }


                        switch (alt30) {
                    	case 1 :
                    	    // wcps.g:206:4: COMMA field= fieldName COLON expr= scalarExpr
                    	    {
                    	    COMMA101=(Token)match(input,COMMA,FOLLOW_COMMA_in_rangeExpr1990); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COMMA101_tree = (Object)adaptor.create(COMMA101);
                    	    adaptor.addChild(root_0, COMMA101_tree);
                    	    }
                    	    pushFollow(FOLLOW_fieldName_in_rangeExpr1994);
                    	    field=fieldName();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) adaptor.addChild(root_0, field.getTree());
                    	    COLON102=(Token)match(input,COLON,FOLLOW_COLON_in_rangeExpr1996); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    COLON102_tree = (Object)adaptor.create(COLON102);
                    	    adaptor.addChild(root_0, COLON102_tree);
                    	    }
                    	    pushFollow(FOLLOW_scalarExpr_in_rangeExpr2000);
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

            RBRACE103=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_rangeExpr2011); if (state.failed) return retval;
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
    // wcps.g:209:1: rangeConstructorExpr returns [RangeConstructorExpr value] : ( STRUCT )? LBRACE field= fieldName COLON expr= coverageExpr ( SEMICOLON field= fieldName COLON expr= coverageExpr )* RBRACE ;
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
            // wcps.g:210:5: ( ( STRUCT )? LBRACE field= fieldName COLON expr= coverageExpr ( SEMICOLON field= fieldName COLON expr= coverageExpr )* RBRACE )
            // wcps.g:210:7: ( STRUCT )? LBRACE field= fieldName COLON expr= coverageExpr ( SEMICOLON field= fieldName COLON expr= coverageExpr )* RBRACE
            {
            root_0 = (Object)adaptor.nil();

            // wcps.g:210:7: ( STRUCT )?
            int alt32=2;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==STRUCT) ) {
                alt32=1;
            }
            switch (alt32) {
                case 1 :
                    // wcps.g:210:8: STRUCT
                    {
                    STRUCT104=(Token)match(input,STRUCT,FOLLOW_STRUCT_in_rangeConstructorExpr2028); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    STRUCT104_tree = (Object)adaptor.create(STRUCT104);
                    adaptor.addChild(root_0, STRUCT104_tree);
                    }

                    }
                    break;

            }

            LBRACE105=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_rangeConstructorExpr2032); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE105_tree = (Object)adaptor.create(LBRACE105);
            adaptor.addChild(root_0, LBRACE105_tree);
            }
            pushFollow(FOLLOW_fieldName_in_rangeConstructorExpr2036);
            field=fieldName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, field.getTree());
            COLON106=(Token)match(input,COLON,FOLLOW_COLON_in_rangeConstructorExpr2038); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON106_tree = (Object)adaptor.create(COLON106);
            adaptor.addChild(root_0, COLON106_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_rangeConstructorExpr2042);
            expr=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, expr.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new RangeConstructorExpr((field!=null?field.value:null), (expr!=null?expr.value:null)); 
            }
            // wcps.g:211:9: ( SEMICOLON field= fieldName COLON expr= coverageExpr )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( (LA33_0==SEMICOLON) ) {
                    alt33=1;
                }


                switch (alt33) {
            	case 1 :
            	    // wcps.g:211:10: SEMICOLON field= fieldName COLON expr= coverageExpr
            	    {
            	    SEMICOLON107=(Token)match(input,SEMICOLON,FOLLOW_SEMICOLON_in_rangeConstructorExpr2055); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    SEMICOLON107_tree = (Object)adaptor.create(SEMICOLON107);
            	    adaptor.addChild(root_0, SEMICOLON107_tree);
            	    }
            	    pushFollow(FOLLOW_fieldName_in_rangeConstructorExpr2059);
            	    field=fieldName();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, field.getTree());
            	    COLON108=(Token)match(input,COLON,FOLLOW_COLON_in_rangeConstructorExpr2061); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COLON108_tree = (Object)adaptor.create(COLON108);
            	    adaptor.addChild(root_0, COLON108_tree);
            	    }
            	    pushFollow(FOLLOW_coverageExpr_in_rangeConstructorExpr2065);
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

            RBRACE109=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_rangeConstructorExpr2071); if (state.failed) return retval;
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
    // wcps.g:213:1: crsTransformExpr returns [CrsTransformExpr value] : CRSTRANSFORM LPAREN e1= coverageExpr COMMA dcl= dimensionCrsList COMMA fil= fieldInterpolationList RPAREN ;
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
            // wcps.g:214:9: ( CRSTRANSFORM LPAREN e1= coverageExpr COMMA dcl= dimensionCrsList COMMA fil= fieldInterpolationList RPAREN )
            // wcps.g:214:11: CRSTRANSFORM LPAREN e1= coverageExpr COMMA dcl= dimensionCrsList COMMA fil= fieldInterpolationList RPAREN
            {
            root_0 = (Object)adaptor.nil();

            CRSTRANSFORM110=(Token)match(input,CRSTRANSFORM,FOLLOW_CRSTRANSFORM_in_crsTransformExpr2094); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CRSTRANSFORM110_tree = (Object)adaptor.create(CRSTRANSFORM110);
            adaptor.addChild(root_0, CRSTRANSFORM110_tree);
            }
            LPAREN111=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_crsTransformExpr2096); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN111_tree = (Object)adaptor.create(LPAREN111);
            adaptor.addChild(root_0, LPAREN111_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_crsTransformExpr2100);
            e1=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            COMMA112=(Token)match(input,COMMA,FOLLOW_COMMA_in_crsTransformExpr2102); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA112_tree = (Object)adaptor.create(COMMA112);
            adaptor.addChild(root_0, COMMA112_tree);
            }
            pushFollow(FOLLOW_dimensionCrsList_in_crsTransformExpr2106);
            dcl=dimensionCrsList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, dcl.getTree());
            COMMA113=(Token)match(input,COMMA,FOLLOW_COMMA_in_crsTransformExpr2108); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA113_tree = (Object)adaptor.create(COMMA113);
            adaptor.addChild(root_0, COMMA113_tree);
            }
            pushFollow(FOLLOW_fieldInterpolationList_in_crsTransformExpr2112);
            fil=fieldInterpolationList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, fil.getTree());
            RPAREN114=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_crsTransformExpr2114); if (state.failed) return retval;
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
    // wcps.g:217:1: dimensionCrsList returns [DimensionCrsList value] : LBRACE elem= dimensionCrsElement ( COMMA elem= dimensionCrsElement )* RBRACE ;
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
            // wcps.g:218:5: ( LBRACE elem= dimensionCrsElement ( COMMA elem= dimensionCrsElement )* RBRACE )
            // wcps.g:218:7: LBRACE elem= dimensionCrsElement ( COMMA elem= dimensionCrsElement )* RBRACE
            {
            root_0 = (Object)adaptor.nil();

            LBRACE115=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_dimensionCrsList2155); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE115_tree = (Object)adaptor.create(LBRACE115);
            adaptor.addChild(root_0, LBRACE115_tree);
            }
            pushFollow(FOLLOW_dimensionCrsElement_in_dimensionCrsList2159);
            elem=dimensionCrsElement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, elem.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new DimensionCrsList((elem!=null?elem.value:null)); 
            }
            // wcps.g:219:9: ( COMMA elem= dimensionCrsElement )*
            loop34:
            do {
                int alt34=2;
                int LA34_0 = input.LA(1);

                if ( (LA34_0==COMMA) ) {
                    alt34=1;
                }


                switch (alt34) {
            	case 1 :
            	    // wcps.g:219:10: COMMA elem= dimensionCrsElement
            	    {
            	    COMMA116=(Token)match(input,COMMA,FOLLOW_COMMA_in_dimensionCrsList2172); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA116_tree = (Object)adaptor.create(COMMA116);
            	    adaptor.addChild(root_0, COMMA116_tree);
            	    }
            	    pushFollow(FOLLOW_dimensionCrsElement_in_dimensionCrsList2176);
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

            RBRACE117=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_dimensionCrsList2183); if (state.failed) return retval;
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
    // wcps.g:221:1: dimensionCrsElement returns [DimensionCrsElement value] : aname= axisName COLON crs= crsName ;
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
            // wcps.g:222:5: (aname= axisName COLON crs= crsName )
            // wcps.g:222:7: aname= axisName COLON crs= crsName
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_axisName_in_dimensionCrsElement2204);
            aname=axisName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new DimensionCrsElement((aname!=null?aname.value:null)); 
            }
            COLON118=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionCrsElement2208); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON118_tree = (Object)adaptor.create(COLON118);
            adaptor.addChild(root_0, COLON118_tree);
            }
            pushFollow(FOLLOW_crsName_in_dimensionCrsElement2212);
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
    // wcps.g:224:1: fieldInterpolationList returns [FieldInterpolationList value] : LBRACE (elem= fieldInterpolationElement ( COMMA elem= fieldInterpolationElement ) )* RBRACE ;
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
            // wcps.g:225:2: ( LBRACE (elem= fieldInterpolationElement ( COMMA elem= fieldInterpolationElement ) )* RBRACE )
            // wcps.g:225:4: LBRACE (elem= fieldInterpolationElement ( COMMA elem= fieldInterpolationElement ) )* RBRACE
            {
            root_0 = (Object)adaptor.nil();

            LBRACE119=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_fieldInterpolationList2230); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE119_tree = (Object)adaptor.create(LBRACE119);
            adaptor.addChild(root_0, LBRACE119_tree);
            }
            // wcps.g:225:11: (elem= fieldInterpolationElement ( COMMA elem= fieldInterpolationElement ) )*
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==INTEGERCONSTANT||LA35_0==STRING||LA35_0==NAME) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // wcps.g:225:12: elem= fieldInterpolationElement ( COMMA elem= fieldInterpolationElement )
            	    {
            	    pushFollow(FOLLOW_fieldInterpolationElement_in_fieldInterpolationList2235);
            	    elem=fieldInterpolationElement();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, elem.getTree());
            	    if ( state.backtracking==0 ) {
            	       retval.value = new FieldInterpolationList((elem!=null?elem.value:null)); 
            	    }
            	    // wcps.g:226:3: ( COMMA elem= fieldInterpolationElement )
            	    // wcps.g:226:4: COMMA elem= fieldInterpolationElement
            	    {
            	    COMMA120=(Token)match(input,COMMA,FOLLOW_COMMA_in_fieldInterpolationList2242); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA120_tree = (Object)adaptor.create(COMMA120);
            	    adaptor.addChild(root_0, COMMA120_tree);
            	    }
            	    pushFollow(FOLLOW_fieldInterpolationElement_in_fieldInterpolationList2246);
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

            RBRACE121=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_fieldInterpolationList2254); if (state.failed) return retval;
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
    // wcps.g:228:1: fieldInterpolationElement returns [FieldInterpolationElement value] : aname= fieldName method= interpolationMethod ;
    public final wcpsParser.fieldInterpolationElement_return fieldInterpolationElement() throws RecognitionException {
        wcpsParser.fieldInterpolationElement_return retval = new wcpsParser.fieldInterpolationElement_return();
        retval.start = input.LT(1);
        int fieldInterpolationElement_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.fieldName_return aname = null;

        wcpsParser.interpolationMethod_return method = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 39) ) { return retval; }
            // wcps.g:229:2: (aname= fieldName method= interpolationMethod )
            // wcps.g:229:4: aname= fieldName method= interpolationMethod
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_fieldName_in_fieldInterpolationElement2269);
            aname=fieldName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
            pushFollow(FOLLOW_interpolationMethod_in_fieldInterpolationElement2273);
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
    // wcps.g:231:1: unaryInducedExpr returns [CoverageExpr value] : (e6= fieldExpr | e1= unaryArithmeticExpr | e2= exponentialExpr | e3= trigonometricExpr | e4= booleanExpr | e5= castExpr | e7= rangeConstructorExpr );
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
            // wcps.g:232:5: (e6= fieldExpr | e1= unaryArithmeticExpr | e2= exponentialExpr | e3= trigonometricExpr | e4= booleanExpr | e5= castExpr | e7= rangeConstructorExpr )
            int alt36=7;
            alt36 = dfa36.predict(input);
            switch (alt36) {
                case 1 :
                    // wcps.g:232:7: e6= fieldExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_fieldExpr_in_unaryInducedExpr2293);
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
                    // wcps.g:233:4: e1= unaryArithmeticExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_unaryArithmeticExpr_in_unaryInducedExpr2302);
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
                    // wcps.g:234:7: e2= exponentialExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_exponentialExpr_in_unaryInducedExpr2314);
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
                    // wcps.g:235:7: e3= trigonometricExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_trigonometricExpr_in_unaryInducedExpr2326);
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
                    // wcps.g:236:7: e4= booleanExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_booleanExpr_in_unaryInducedExpr2338);
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
                    // wcps.g:237:7: e5= castExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_castExpr_in_unaryInducedExpr2350);
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
                    // wcps.g:238:7: e7= rangeConstructorExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_rangeConstructorExpr_in_unaryInducedExpr2362);
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
    // wcps.g:240:1: unaryArithmeticExpr returns [CoverageExpr value] : (op= ( MINUS | PLUS ) e1= coverageAtom | op= ( SQRT | ABS | RE | IM ) LPAREN e2= coverageExpr RPAREN );
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
            // wcps.g:241:5: (op= ( MINUS | PLUS ) e1= coverageAtom | op= ( SQRT | ABS | RE | IM ) LPAREN e2= coverageExpr RPAREN )
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
                    // wcps.g:241:7: op= ( MINUS | PLUS ) e1= coverageAtom
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

                    pushFollow(FOLLOW_coverageAtom_in_unaryArithmeticExpr2393);
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
                    // wcps.g:242:7: op= ( SQRT | ABS | RE | IM ) LPAREN e2= coverageExpr RPAREN
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

                    LPAREN122=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_unaryArithmeticExpr2415); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN122_tree = (Object)adaptor.create(LPAREN122);
                    adaptor.addChild(root_0, LPAREN122_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_unaryArithmeticExpr2419);
                    e2=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    RPAREN123=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_unaryArithmeticExpr2421); if (state.failed) return retval;
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
    // wcps.g:244:1: exponentialExpr returns [ExponentialExpr value] : (op= ( EXP | LOG | LN ) LPAREN e1= coverageExpr RPAREN | op= POW LPAREN e2= coverageExpr COMMA e3= ( FLOATCONSTANT | INTEGERCONSTANT ) RPAREN );
    public final wcpsParser.exponentialExpr_return exponentialExpr() throws RecognitionException {
        wcpsParser.exponentialExpr_return retval = new wcpsParser.exponentialExpr_return();
        retval.start = input.LT(1);
        int exponentialExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token e3=null;
        Token LPAREN124=null;
        Token RPAREN125=null;
        Token LPAREN126=null;
        Token COMMA127=null;
        Token RPAREN128=null;
        wcpsParser.coverageExpr_return e1 = null;

        wcpsParser.coverageExpr_return e2 = null;


        Object op_tree=null;
        Object e3_tree=null;
        Object LPAREN124_tree=null;
        Object RPAREN125_tree=null;
        Object LPAREN126_tree=null;
        Object COMMA127_tree=null;
        Object RPAREN128_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 42) ) { return retval; }
            // wcps.g:245:5: (op= ( EXP | LOG | LN ) LPAREN e1= coverageExpr RPAREN | op= POW LPAREN e2= coverageExpr COMMA e3= ( FLOATCONSTANT | INTEGERCONSTANT ) RPAREN )
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( ((LA38_0>=EXP && LA38_0<=LN)) ) {
                alt38=1;
            }
            else if ( (LA38_0==POW) ) {
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
                    // wcps.g:245:7: op= ( EXP | LOG | LN ) LPAREN e1= coverageExpr RPAREN
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

                    LPAREN124=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_exponentialExpr2452); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN124_tree = (Object)adaptor.create(LPAREN124);
                    adaptor.addChild(root_0, LPAREN124_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_exponentialExpr2456);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN125=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_exponentialExpr2458); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN125_tree = (Object)adaptor.create(RPAREN125);
                    adaptor.addChild(root_0, RPAREN125_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new ExponentialExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:246:7: op= POW LPAREN e2= coverageExpr COMMA e3= ( FLOATCONSTANT | INTEGERCONSTANT ) RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,POW,FOLLOW_POW_in_exponentialExpr2470); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN126=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_exponentialExpr2472); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN126_tree = (Object)adaptor.create(LPAREN126);
                    adaptor.addChild(root_0, LPAREN126_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_exponentialExpr2476);
                    e2=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    COMMA127=(Token)match(input,COMMA,FOLLOW_COMMA_in_exponentialExpr2478); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA127_tree = (Object)adaptor.create(COMMA127);
                    adaptor.addChild(root_0, COMMA127_tree);
                    }
                    e3=(Token)input.LT(1);
                    if ( (input.LA(1)>=FLOATCONSTANT && input.LA(1)<=INTEGERCONSTANT) ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, (Object)adaptor.create(e3));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    RPAREN128=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_exponentialExpr2488); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN128_tree = (Object)adaptor.create(RPAREN128);
                    adaptor.addChild(root_0, RPAREN128_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new ExponentialExpr((op!=null?op.getText():null), (e3!=null?e3.getText():null), (e2!=null?e2.value:null)); 
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
    // wcps.g:248:1: trigonometricExpr returns [TrigonometricExpr value] : op= ( SIN | COS | TAN | SINH | COSH | TANH | ARCSIN | ARCCOS | ARCTAN ) LPAREN e1= coverageExpr RPAREN ;
    public final wcpsParser.trigonometricExpr_return trigonometricExpr() throws RecognitionException {
        wcpsParser.trigonometricExpr_return retval = new wcpsParser.trigonometricExpr_return();
        retval.start = input.LT(1);
        int trigonometricExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token LPAREN129=null;
        Token RPAREN130=null;
        wcpsParser.coverageExpr_return e1 = null;


        Object op_tree=null;
        Object LPAREN129_tree=null;
        Object RPAREN130_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 43) ) { return retval; }
            // wcps.g:249:5: (op= ( SIN | COS | TAN | SINH | COSH | TANH | ARCSIN | ARCCOS | ARCTAN ) LPAREN e1= coverageExpr RPAREN )
            // wcps.g:249:7: op= ( SIN | COS | TAN | SINH | COSH | TANH | ARCSIN | ARCCOS | ARCTAN ) LPAREN e1= coverageExpr RPAREN
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

            LPAREN129=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_trigonometricExpr2531); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN129_tree = (Object)adaptor.create(LPAREN129);
            adaptor.addChild(root_0, LPAREN129_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_trigonometricExpr2535);
            e1=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            RPAREN130=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_trigonometricExpr2537); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN130_tree = (Object)adaptor.create(RPAREN130);
            adaptor.addChild(root_0, RPAREN130_tree);
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
    // wcps.g:251:1: booleanExpr returns [BooleanExpr value] : (op= NOT e1= coverageExpr | op= BIT LPAREN e1= coverageExpr COMMA e2= indexExpr RPAREN );
    public final wcpsParser.booleanExpr_return booleanExpr() throws RecognitionException {
        wcpsParser.booleanExpr_return retval = new wcpsParser.booleanExpr_return();
        retval.start = input.LT(1);
        int booleanExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token LPAREN131=null;
        Token COMMA132=null;
        Token RPAREN133=null;
        wcpsParser.coverageExpr_return e1 = null;

        wcpsParser.indexExpr_return e2 = null;


        Object op_tree=null;
        Object LPAREN131_tree=null;
        Object COMMA132_tree=null;
        Object RPAREN133_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 44) ) { return retval; }
            // wcps.g:252:5: (op= NOT e1= coverageExpr | op= BIT LPAREN e1= coverageExpr COMMA e2= indexExpr RPAREN )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==NOT) ) {
                alt39=1;
            }
            else if ( (LA39_0==BIT) ) {
                alt39=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // wcps.g:252:7: op= NOT e1= coverageExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,NOT,FOLLOW_NOT_in_booleanExpr2560); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_booleanExpr2564);
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
                    // wcps.g:253:7: op= BIT LPAREN e1= coverageExpr COMMA e2= indexExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,BIT,FOLLOW_BIT_in_booleanExpr2576); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN131=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_booleanExpr2578); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN131_tree = (Object)adaptor.create(LPAREN131);
                    adaptor.addChild(root_0, LPAREN131_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_booleanExpr2582);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COMMA132=(Token)match(input,COMMA,FOLLOW_COMMA_in_booleanExpr2584); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA132_tree = (Object)adaptor.create(COMMA132);
                    adaptor.addChild(root_0, COMMA132_tree);
                    }
                    pushFollow(FOLLOW_indexExpr_in_booleanExpr2588);
                    e2=indexExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    RPAREN133=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_booleanExpr2590); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN133_tree = (Object)adaptor.create(RPAREN133);
                    adaptor.addChild(root_0, RPAREN133_tree);
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
    // wcps.g:255:1: indexExpr returns [IndexExpr value] : e1= indexTerm (op= ( PLUS | MINUS ) e2= indexTerm )* ;
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
            // wcps.g:256:5: (e1= indexTerm (op= ( PLUS | MINUS ) e2= indexTerm )* )
            // wcps.g:256:7: e1= indexTerm (op= ( PLUS | MINUS ) e2= indexTerm )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_indexTerm_in_indexExpr2613);
            e1=indexTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:257:3: (op= ( PLUS | MINUS ) e2= indexTerm )*
            loop40:
            do {
                int alt40=2;
                int LA40_0 = input.LA(1);

                if ( ((LA40_0>=PLUS && LA40_0<=MINUS)) ) {
                    alt40=1;
                }


                switch (alt40) {
            	case 1 :
            	    // wcps.g:257:4: op= ( PLUS | MINUS ) e2= indexTerm
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

            	    pushFollow(FOLLOW_indexTerm_in_indexExpr2631);
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
    // wcps.g:259:1: indexTerm returns [IndexExpr value] : e1= indexFactor ( (op= ( MULT | DIVIDE ) e2= indexFactor ) )* ;
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
            // wcps.g:260:5: (e1= indexFactor ( (op= ( MULT | DIVIDE ) e2= indexFactor ) )* )
            // wcps.g:260:7: e1= indexFactor ( (op= ( MULT | DIVIDE ) e2= indexFactor ) )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_indexFactor_in_indexTerm2656);
            e1=indexFactor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:261:6: ( (op= ( MULT | DIVIDE ) e2= indexFactor ) )*
            loop41:
            do {
                int alt41=2;
                int LA41_0 = input.LA(1);

                if ( ((LA41_0>=MULT && LA41_0<=DIVIDE)) ) {
                    alt41=1;
                }


                switch (alt41) {
            	case 1 :
            	    // wcps.g:261:7: (op= ( MULT | DIVIDE ) e2= indexFactor )
            	    {
            	    // wcps.g:261:7: (op= ( MULT | DIVIDE ) e2= indexFactor )
            	    // wcps.g:261:8: op= ( MULT | DIVIDE ) e2= indexFactor
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

            	    pushFollow(FOLLOW_indexFactor_in_indexTerm2677);
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
            	    break loop41;
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
    // wcps.g:263:1: indexFactor returns [IndexExpr value] : (e= INTEGERCONSTANT | op= ROUND LPAREN e1= numericScalarExpr RPAREN | ( LPAREN e2= indexExpr RPAREN ) );
    public final wcpsParser.indexFactor_return indexFactor() throws RecognitionException {
        wcpsParser.indexFactor_return retval = new wcpsParser.indexFactor_return();
        retval.start = input.LT(1);
        int indexFactor_StartIndex = input.index();
        Object root_0 = null;

        Token e=null;
        Token op=null;
        Token LPAREN134=null;
        Token RPAREN135=null;
        Token LPAREN136=null;
        Token RPAREN137=null;
        wcpsParser.numericScalarExpr_return e1 = null;

        wcpsParser.indexExpr_return e2 = null;


        Object e_tree=null;
        Object op_tree=null;
        Object LPAREN134_tree=null;
        Object RPAREN135_tree=null;
        Object LPAREN136_tree=null;
        Object RPAREN137_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 47) ) { return retval; }
            // wcps.g:264:5: (e= INTEGERCONSTANT | op= ROUND LPAREN e1= numericScalarExpr RPAREN | ( LPAREN e2= indexExpr RPAREN ) )
            int alt42=3;
            switch ( input.LA(1) ) {
            case INTEGERCONSTANT:
                {
                alt42=1;
                }
                break;
            case ROUND:
                {
                alt42=2;
                }
                break;
            case LPAREN:
                {
                alt42=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 42, 0, input);

                throw nvae;
            }

            switch (alt42) {
                case 1 :
                    // wcps.g:264:7: e= INTEGERCONSTANT
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)match(input,INTEGERCONSTANT,FOLLOW_INTEGERCONSTANT_in_indexFactor2704); if (state.failed) return retval;
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
                    // wcps.g:265:7: op= ROUND LPAREN e1= numericScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,ROUND,FOLLOW_ROUND_in_indexFactor2717); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN134=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_indexFactor2719); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN134_tree = (Object)adaptor.create(LPAREN134);
                    adaptor.addChild(root_0, LPAREN134_tree);
                    }
                    pushFollow(FOLLOW_numericScalarExpr_in_indexFactor2723);
                    e1=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN135=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_indexFactor2725); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN135_tree = (Object)adaptor.create(RPAREN135);
                    adaptor.addChild(root_0, RPAREN135_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new IndexExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:266:7: ( LPAREN e2= indexExpr RPAREN )
                    {
                    root_0 = (Object)adaptor.nil();

                    // wcps.g:266:7: ( LPAREN e2= indexExpr RPAREN )
                    // wcps.g:266:8: LPAREN e2= indexExpr RPAREN
                    {
                    LPAREN136=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_indexFactor2737); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN136_tree = (Object)adaptor.create(LPAREN136);
                    adaptor.addChild(root_0, LPAREN136_tree);
                    }
                    pushFollow(FOLLOW_indexExpr_in_indexFactor2741);
                    e2=indexExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    RPAREN137=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_indexFactor2743); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN137_tree = (Object)adaptor.create(RPAREN137);
                    adaptor.addChild(root_0, RPAREN137_tree);
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
    // wcps.g:268:1: stringScalarExpr returns [StringScalarExpr value] : (op= IDENTIFIER LPAREN e1= coverageExpr RPAREN | e= STRING );
    public final wcpsParser.stringScalarExpr_return stringScalarExpr() throws RecognitionException {
        wcpsParser.stringScalarExpr_return retval = new wcpsParser.stringScalarExpr_return();
        retval.start = input.LT(1);
        int stringScalarExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token e=null;
        Token LPAREN138=null;
        Token RPAREN139=null;
        wcpsParser.coverageExpr_return e1 = null;


        Object op_tree=null;
        Object e_tree=null;
        Object LPAREN138_tree=null;
        Object RPAREN139_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 48) ) { return retval; }
            // wcps.g:270:5: (op= IDENTIFIER LPAREN e1= coverageExpr RPAREN | e= STRING )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==IDENTIFIER) ) {
                alt43=1;
            }
            else if ( (LA43_0==STRING) ) {
                alt43=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1 :
                    // wcps.g:270:7: op= IDENTIFIER LPAREN e1= coverageExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_stringScalarExpr2770); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN138=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_stringScalarExpr2772); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN138_tree = (Object)adaptor.create(LPAREN138);
                    adaptor.addChild(root_0, LPAREN138_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_stringScalarExpr2776);
                    e1=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN139=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_stringScalarExpr2778); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN139_tree = (Object)adaptor.create(RPAREN139);
                    adaptor.addChild(root_0, RPAREN139_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new StringScalarExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:271:7: e= STRING
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)match(input,STRING,FOLLOW_STRING_in_stringScalarExpr2791); if (state.failed) return retval;
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
    // wcps.g:273:1: scaleExpr returns [ScaleExpr value] : SCALE LPAREN e1= coverageExpr COMMA ( ( LBRACE dil= dimensionIntervalList RBRACE ) | (op= IMAGECRSDOMAIN LPAREN e3= coverageExpr RPAREN ) ) ( COMMA fil= fieldInterpolationList )? RPAREN ;
    public final wcpsParser.scaleExpr_return scaleExpr() throws RecognitionException {
        wcpsParser.scaleExpr_return retval = new wcpsParser.scaleExpr_return();
        retval.start = input.LT(1);
        int scaleExpr_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token SCALE140=null;
        Token LPAREN141=null;
        Token COMMA142=null;
        Token LBRACE143=null;
        Token RBRACE144=null;
        Token LPAREN145=null;
        Token RPAREN146=null;
        Token COMMA147=null;
        Token RPAREN148=null;
        wcpsParser.coverageExpr_return e1 = null;

        wcpsParser.dimensionIntervalList_return dil = null;

        wcpsParser.coverageExpr_return e3 = null;

        wcpsParser.fieldInterpolationList_return fil = null;


        Object op_tree=null;
        Object SCALE140_tree=null;
        Object LPAREN141_tree=null;
        Object COMMA142_tree=null;
        Object LBRACE143_tree=null;
        Object RBRACE144_tree=null;
        Object LPAREN145_tree=null;
        Object RPAREN146_tree=null;
        Object COMMA147_tree=null;
        Object RPAREN148_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 49) ) { return retval; }
            // wcps.g:274:2: ( SCALE LPAREN e1= coverageExpr COMMA ( ( LBRACE dil= dimensionIntervalList RBRACE ) | (op= IMAGECRSDOMAIN LPAREN e3= coverageExpr RPAREN ) ) ( COMMA fil= fieldInterpolationList )? RPAREN )
            // wcps.g:274:4: SCALE LPAREN e1= coverageExpr COMMA ( ( LBRACE dil= dimensionIntervalList RBRACE ) | (op= IMAGECRSDOMAIN LPAREN e3= coverageExpr RPAREN ) ) ( COMMA fil= fieldInterpolationList )? RPAREN
            {
            root_0 = (Object)adaptor.nil();

            SCALE140=(Token)match(input,SCALE,FOLLOW_SCALE_in_scaleExpr2809); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SCALE140_tree = (Object)adaptor.create(SCALE140);
            adaptor.addChild(root_0, SCALE140_tree);
            }
            LPAREN141=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_scaleExpr2811); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN141_tree = (Object)adaptor.create(LPAREN141);
            adaptor.addChild(root_0, LPAREN141_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_scaleExpr2824);
            e1=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            COMMA142=(Token)match(input,COMMA,FOLLOW_COMMA_in_scaleExpr2826); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA142_tree = (Object)adaptor.create(COMMA142);
            adaptor.addChild(root_0, COMMA142_tree);
            }
            // wcps.g:276:10: ( ( LBRACE dil= dimensionIntervalList RBRACE ) | (op= IMAGECRSDOMAIN LPAREN e3= coverageExpr RPAREN ) )
            int alt44=2;
            int LA44_0 = input.LA(1);

            if ( (LA44_0==LBRACE) ) {
                alt44=1;
            }
            else if ( (LA44_0==IMAGECRSDOMAIN) ) {
                alt44=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                throw nvae;
            }
            switch (alt44) {
                case 1 :
                    // wcps.g:277:12: ( LBRACE dil= dimensionIntervalList RBRACE )
                    {
                    // wcps.g:277:12: ( LBRACE dil= dimensionIntervalList RBRACE )
                    // wcps.g:277:14: LBRACE dil= dimensionIntervalList RBRACE
                    {
                    LBRACE143=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_scaleExpr2853); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LBRACE143_tree = (Object)adaptor.create(LBRACE143);
                    adaptor.addChild(root_0, LBRACE143_tree);
                    }
                    pushFollow(FOLLOW_dimensionIntervalList_in_scaleExpr2857);
                    dil=dimensionIntervalList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dil.getTree());
                    RBRACE144=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_scaleExpr2859); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RBRACE144_tree = (Object)adaptor.create(RBRACE144);
                    adaptor.addChild(root_0, RBRACE144_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new ScaleExpr((e1!=null?e1.value:null), (dil!=null?dil.value:null)); 
                    }

                    }


                    }
                    break;
                case 2 :
                    // wcps.g:278:12: (op= IMAGECRSDOMAIN LPAREN e3= coverageExpr RPAREN )
                    {
                    // wcps.g:278:12: (op= IMAGECRSDOMAIN LPAREN e3= coverageExpr RPAREN )
                    // wcps.g:278:14: op= IMAGECRSDOMAIN LPAREN e3= coverageExpr RPAREN
                    {
                    op=(Token)match(input,IMAGECRSDOMAIN,FOLLOW_IMAGECRSDOMAIN_in_scaleExpr2880); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN145=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_scaleExpr2882); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN145_tree = (Object)adaptor.create(LPAREN145);
                    adaptor.addChild(root_0, LPAREN145_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_scaleExpr2886);
                    e3=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    RPAREN146=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_scaleExpr2888); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN146_tree = (Object)adaptor.create(RPAREN146);
                    adaptor.addChild(root_0, RPAREN146_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new ScaleExpr((e1!=null?e1.value:null), new MetaDataExpr((op!=null?op.getText():null), (e3!=null?e3.value:null), null)); 
                    }

                    }


                    }
                    break;

            }

            // wcps.g:280:10: ( COMMA fil= fieldInterpolationList )?
            int alt45=2;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==COMMA) ) {
                alt45=1;
            }
            switch (alt45) {
                case 1 :
                    // wcps.g:280:11: COMMA fil= fieldInterpolationList
                    {
                    COMMA147=(Token)match(input,COMMA,FOLLOW_COMMA_in_scaleExpr2915); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA147_tree = (Object)adaptor.create(COMMA147);
                    adaptor.addChild(root_0, COMMA147_tree);
                    }
                    pushFollow(FOLLOW_fieldInterpolationList_in_scaleExpr2919);
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

            RPAREN148=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_scaleExpr2934); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN148_tree = (Object)adaptor.create(RPAREN148);
            adaptor.addChild(root_0, RPAREN148_tree);
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
    // wcps.g:283:1: subsetExpr returns [SubsetExpr value] : (e1= trimExpr | e2= sliceExpr | e3= extendExpr | e4= trimSliceExpr );
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
            // wcps.g:284:2: (e1= trimExpr | e2= sliceExpr | e3= extendExpr | e4= trimSliceExpr )
            int alt46=4;
            alt46 = dfa46.predict(input);
            switch (alt46) {
                case 1 :
                    // wcps.g:284:4: e1= trimExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_trimExpr_in_subsetExpr2949);
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
                    // wcps.g:285:4: e2= sliceExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_sliceExpr_in_subsetExpr2958);
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
                    // wcps.g:286:4: e3= extendExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_extendExpr_in_subsetExpr2967);
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
                    // wcps.g:287:7: e4= trimSliceExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_trimSliceExpr_in_subsetExpr2979);
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
    // wcps.g:289:1: trimExpr returns [TrimExpr value] : (e1= coverageAtom LBRACKET dil= dimensionIntervalList RBRACKET | TRIM LPAREN e2= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN );
    public final wcpsParser.trimExpr_return trimExpr() throws RecognitionException {
        wcpsParser.trimExpr_return retval = new wcpsParser.trimExpr_return();
        retval.start = input.LT(1);
        int trimExpr_StartIndex = input.index();
        Object root_0 = null;

        Token LBRACKET149=null;
        Token RBRACKET150=null;
        Token TRIM151=null;
        Token LPAREN152=null;
        Token COMMA153=null;
        Token LBRACE154=null;
        Token RBRACE155=null;
        Token RPAREN156=null;
        wcpsParser.coverageAtom_return e1 = null;

        wcpsParser.dimensionIntervalList_return dil = null;

        wcpsParser.coverageExpr_return e2 = null;


        Object LBRACKET149_tree=null;
        Object RBRACKET150_tree=null;
        Object TRIM151_tree=null;
        Object LPAREN152_tree=null;
        Object COMMA153_tree=null;
        Object LBRACE154_tree=null;
        Object RBRACE155_tree=null;
        Object RPAREN156_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 51) ) { return retval; }
            // wcps.g:290:2: (e1= coverageAtom LBRACKET dil= dimensionIntervalList RBRACKET | TRIM LPAREN e2= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN )
            int alt47=2;
            alt47 = dfa47.predict(input);
            switch (alt47) {
                case 1 :
                    // wcps.g:290:4: e1= coverageAtom LBRACKET dil= dimensionIntervalList RBRACKET
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageAtom_in_trimExpr2996);
                    e1=coverageAtom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    LBRACKET149=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_trimExpr2998); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LBRACKET149_tree = (Object)adaptor.create(LBRACKET149);
                    adaptor.addChild(root_0, LBRACKET149_tree);
                    }
                    pushFollow(FOLLOW_dimensionIntervalList_in_trimExpr3002);
                    dil=dimensionIntervalList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dil.getTree());
                    RBRACKET150=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_trimExpr3004); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RBRACKET150_tree = (Object)adaptor.create(RBRACKET150);
                    adaptor.addChild(root_0, RBRACKET150_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new TrimExpr((e1!=null?e1.value:null), (dil!=null?dil.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:291:6: TRIM LPAREN e2= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    TRIM151=(Token)match(input,TRIM,FOLLOW_TRIM_in_trimExpr3013); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    TRIM151_tree = (Object)adaptor.create(TRIM151);
                    adaptor.addChild(root_0, TRIM151_tree);
                    }
                    LPAREN152=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_trimExpr3015); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN152_tree = (Object)adaptor.create(LPAREN152);
                    adaptor.addChild(root_0, LPAREN152_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_trimExpr3019);
                    e2=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    COMMA153=(Token)match(input,COMMA,FOLLOW_COMMA_in_trimExpr3021); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA153_tree = (Object)adaptor.create(COMMA153);
                    adaptor.addChild(root_0, COMMA153_tree);
                    }
                    LBRACE154=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_trimExpr3023); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LBRACE154_tree = (Object)adaptor.create(LBRACE154);
                    adaptor.addChild(root_0, LBRACE154_tree);
                    }
                    pushFollow(FOLLOW_dimensionIntervalList_in_trimExpr3027);
                    dil=dimensionIntervalList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dil.getTree());
                    RBRACE155=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_trimExpr3029); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RBRACE155_tree = (Object)adaptor.create(RBRACE155);
                    adaptor.addChild(root_0, RBRACE155_tree);
                    }
                    RPAREN156=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_trimExpr3031); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN156_tree = (Object)adaptor.create(RPAREN156);
                    adaptor.addChild(root_0, RPAREN156_tree);
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
    // wcps.g:293:1: trimSliceExpr returns [TrimSliceExpr value] : e1= coverageAtom LBRACKET (el1= dimensionIntervalElement | el2= dimensionPointElement ) ( COMMA (el3= dimensionIntervalElement | el4= dimensionPointElement ) )* RBRACKET ;
    public final wcpsParser.trimSliceExpr_return trimSliceExpr() throws RecognitionException {
        wcpsParser.trimSliceExpr_return retval = new wcpsParser.trimSliceExpr_return();
        retval.start = input.LT(1);
        int trimSliceExpr_StartIndex = input.index();
        Object root_0 = null;

        Token LBRACKET157=null;
        Token COMMA158=null;
        Token RBRACKET159=null;
        wcpsParser.coverageAtom_return e1 = null;

        wcpsParser.dimensionIntervalElement_return el1 = null;

        wcpsParser.dimensionPointElement_return el2 = null;

        wcpsParser.dimensionIntervalElement_return el3 = null;

        wcpsParser.dimensionPointElement_return el4 = null;


        Object LBRACKET157_tree=null;
        Object COMMA158_tree=null;
        Object RBRACKET159_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 52) ) { return retval; }
            // wcps.g:294:2: (e1= coverageAtom LBRACKET (el1= dimensionIntervalElement | el2= dimensionPointElement ) ( COMMA (el3= dimensionIntervalElement | el4= dimensionPointElement ) )* RBRACKET )
            // wcps.g:294:4: e1= coverageAtom LBRACKET (el1= dimensionIntervalElement | el2= dimensionPointElement ) ( COMMA (el3= dimensionIntervalElement | el4= dimensionPointElement ) )* RBRACKET
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_coverageAtom_in_trimSliceExpr3048);
            e1=coverageAtom();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            LBRACKET157=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_trimSliceExpr3050); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACKET157_tree = (Object)adaptor.create(LBRACKET157);
            adaptor.addChild(root_0, LBRACKET157_tree);
            }
            if ( state.backtracking==0 ) {
               retval.value = new TrimSliceExpr((e1!=null?e1.value:null)); 
            }
            // wcps.g:295:30: (el1= dimensionIntervalElement | el2= dimensionPointElement )
            int alt48=2;
            int LA48_0 = input.LA(1);

            if ( (LA48_0==INTEGERCONSTANT||LA48_0==STRING||LA48_0==NAME) ) {
                int LA48_1 = input.LA(2);

                if ( (LA48_1==LPAREN) ) {
                    int LA48_2 = input.LA(3);

                    if ( (synpred120_wcps()) ) {
                        alt48=1;
                    }
                    else if ( (true) ) {
                        alt48=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 48, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA48_1==COLON) ) {
                    int LA48_3 = input.LA(3);

                    if ( (synpred120_wcps()) ) {
                        alt48=1;
                    }
                    else if ( (true) ) {
                        alt48=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 48, 3, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 48, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                throw nvae;
            }
            switch (alt48) {
                case 1 :
                    // wcps.g:295:32: el1= dimensionIntervalElement
                    {
                    pushFollow(FOLLOW_dimensionIntervalElement_in_trimSliceExpr3087);
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
                    // wcps.g:296:32: el2= dimensionPointElement
                    {
                    pushFollow(FOLLOW_dimensionPointElement_in_trimSliceExpr3124);
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

            // wcps.g:297:30: ( COMMA (el3= dimensionIntervalElement | el4= dimensionPointElement ) )*
            loop50:
            do {
                int alt50=2;
                int LA50_0 = input.LA(1);

                if ( (LA50_0==COMMA) ) {
                    alt50=1;
                }


                switch (alt50) {
            	case 1 :
            	    // wcps.g:297:31: COMMA (el3= dimensionIntervalElement | el4= dimensionPointElement )
            	    {
            	    COMMA158=(Token)match(input,COMMA,FOLLOW_COMMA_in_trimSliceExpr3160); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA158_tree = (Object)adaptor.create(COMMA158);
            	    adaptor.addChild(root_0, COMMA158_tree);
            	    }
            	    // wcps.g:298:30: (el3= dimensionIntervalElement | el4= dimensionPointElement )
            	    int alt49=2;
            	    int LA49_0 = input.LA(1);

            	    if ( (LA49_0==INTEGERCONSTANT||LA49_0==STRING||LA49_0==NAME) ) {
            	        int LA49_1 = input.LA(2);

		        if ( (LA49_1==LPAREN) ) {
            	            int LA49_2 = input.LA(3);

            	            if ( (synpred121_wcps()) ) {
            	                alt49=1;
            	            }
            	            else if ( (true) ) {
            	                alt49=2;
            	            }
            	            else {
            	                if (state.backtracking>0) {state.failed=true; return retval;}
            	                NoViableAltException nvae =
            	                    new NoViableAltException("", 49, 2, input);

            	                throw nvae;
            	            }
            	        }
		        else if ( (LA49_1==COLON) ) {
            	            int LA49_3 = input.LA(3);

            	            if ( (synpred121_wcps()) ) {
            	                alt49=1;
            	            }
            	            else if ( (true) ) {
            	                alt49=2;
            	            }
            	            else {
            	                if (state.backtracking>0) {state.failed=true; return retval;}
            	                NoViableAltException nvae =
            	                    new NoViableAltException("", 49, 3, input);

            	                throw nvae;
            	            }
            	        }
            	        else {
            	            if (state.backtracking>0) {state.failed=true; return retval;}
            	            NoViableAltException nvae =
            	                new NoViableAltException("", 49, 1, input);

            	            throw nvae;
            	        }
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 49, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt49) {
            	        case 1 :
            	            // wcps.g:298:32: el3= dimensionIntervalElement
            	            {
            	            pushFollow(FOLLOW_dimensionIntervalElement_in_trimSliceExpr3196);
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
            	            // wcps.g:299:32: el4= dimensionPointElement
            	            {
            	            pushFollow(FOLLOW_dimensionPointElement_in_trimSliceExpr3233);
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
            	    break loop50;
                }
            } while (true);

            RBRACKET159=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_trimSliceExpr3264); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACKET159_tree = (Object)adaptor.create(RBRACKET159);
            adaptor.addChild(root_0, RBRACKET159_tree);
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
    // wcps.g:302:1: sliceExpr returns [SliceExpr value] : (e1= coverageAtom LBRACKET dpl= dimensionPointList RBRACKET | SLICE LPAREN e2= coverageExpr COMMA LBRACE dpl= dimensionPointList RBRACE RPAREN );
    public final wcpsParser.sliceExpr_return sliceExpr() throws RecognitionException {
        wcpsParser.sliceExpr_return retval = new wcpsParser.sliceExpr_return();
        retval.start = input.LT(1);
        int sliceExpr_StartIndex = input.index();
        Object root_0 = null;

        Token LBRACKET160=null;
        Token RBRACKET161=null;
        Token SLICE162=null;
        Token LPAREN163=null;
        Token COMMA164=null;
        Token LBRACE165=null;
        Token RBRACE166=null;
        Token RPAREN167=null;
        wcpsParser.coverageAtom_return e1 = null;

        wcpsParser.dimensionPointList_return dpl = null;

        wcpsParser.coverageExpr_return e2 = null;


        Object LBRACKET160_tree=null;
        Object RBRACKET161_tree=null;
        Object SLICE162_tree=null;
        Object LPAREN163_tree=null;
        Object COMMA164_tree=null;
        Object LBRACE165_tree=null;
        Object RBRACE166_tree=null;
        Object RPAREN167_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 53) ) { return retval; }
            // wcps.g:303:2: (e1= coverageAtom LBRACKET dpl= dimensionPointList RBRACKET | SLICE LPAREN e2= coverageExpr COMMA LBRACE dpl= dimensionPointList RBRACE RPAREN )
            int alt51=2;
            alt51 = dfa51.predict(input);
            switch (alt51) {
                case 1 :
                    // wcps.g:303:4: e1= coverageAtom LBRACKET dpl= dimensionPointList RBRACKET
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_coverageAtom_in_sliceExpr3283);
                    e1=coverageAtom();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    LBRACKET160=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_sliceExpr3285); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LBRACKET160_tree = (Object)adaptor.create(LBRACKET160);
                    adaptor.addChild(root_0, LBRACKET160_tree);
                    }
                    pushFollow(FOLLOW_dimensionPointList_in_sliceExpr3289);
                    dpl=dimensionPointList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dpl.getTree());
                    RBRACKET161=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_sliceExpr3291); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RBRACKET161_tree = (Object)adaptor.create(RBRACKET161);
                    adaptor.addChild(root_0, RBRACKET161_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new SliceExpr((e1!=null?e1.value:null), (dpl!=null?dpl.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:304:4: SLICE LPAREN e2= coverageExpr COMMA LBRACE dpl= dimensionPointList RBRACE RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    SLICE162=(Token)match(input,SLICE,FOLLOW_SLICE_in_sliceExpr3298); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    SLICE162_tree = (Object)adaptor.create(SLICE162);
                    adaptor.addChild(root_0, SLICE162_tree);
                    }
                    LPAREN163=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_sliceExpr3300); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN163_tree = (Object)adaptor.create(LPAREN163);
                    adaptor.addChild(root_0, LPAREN163_tree);
                    }
                    pushFollow(FOLLOW_coverageExpr_in_sliceExpr3304);
                    e2=coverageExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    COMMA164=(Token)match(input,COMMA,FOLLOW_COMMA_in_sliceExpr3306); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COMMA164_tree = (Object)adaptor.create(COMMA164);
                    adaptor.addChild(root_0, COMMA164_tree);
                    }
                    LBRACE165=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_sliceExpr3308); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LBRACE165_tree = (Object)adaptor.create(LBRACE165);
                    adaptor.addChild(root_0, LBRACE165_tree);
                    }
                    pushFollow(FOLLOW_dimensionPointList_in_sliceExpr3312);
                    dpl=dimensionPointList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dpl.getTree());
                    RBRACE166=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_sliceExpr3314); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RBRACE166_tree = (Object)adaptor.create(RBRACE166);
                    adaptor.addChild(root_0, RBRACE166_tree);
                    }
                    RPAREN167=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_sliceExpr3316); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN167_tree = (Object)adaptor.create(RPAREN167);
                    adaptor.addChild(root_0, RPAREN167_tree);
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
    // wcps.g:306:1: extendExpr returns [ExtendExpr value] : EXTEND LPAREN e1= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN ;
    public final wcpsParser.extendExpr_return extendExpr() throws RecognitionException {
        wcpsParser.extendExpr_return retval = new wcpsParser.extendExpr_return();
        retval.start = input.LT(1);
        int extendExpr_StartIndex = input.index();
        Object root_0 = null;

        Token EXTEND168=null;
        Token LPAREN169=null;
        Token COMMA170=null;
        Token LBRACE171=null;
        Token RBRACE172=null;
        Token RPAREN173=null;
        wcpsParser.coverageExpr_return e1 = null;

        wcpsParser.dimensionIntervalList_return dil = null;


        Object EXTEND168_tree=null;
        Object LPAREN169_tree=null;
        Object COMMA170_tree=null;
        Object LBRACE171_tree=null;
        Object RBRACE172_tree=null;
        Object RPAREN173_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 54) ) { return retval; }
            // wcps.g:307:2: ( EXTEND LPAREN e1= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN )
            // wcps.g:307:4: EXTEND LPAREN e1= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN
            {
            root_0 = (Object)adaptor.nil();

            EXTEND168=(Token)match(input,EXTEND,FOLLOW_EXTEND_in_extendExpr3331); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            EXTEND168_tree = (Object)adaptor.create(EXTEND168);
            adaptor.addChild(root_0, EXTEND168_tree);
            }
            LPAREN169=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_extendExpr3333); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN169_tree = (Object)adaptor.create(LPAREN169);
            adaptor.addChild(root_0, LPAREN169_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_extendExpr3337);
            e1=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            COMMA170=(Token)match(input,COMMA,FOLLOW_COMMA_in_extendExpr3339); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA170_tree = (Object)adaptor.create(COMMA170);
            adaptor.addChild(root_0, COMMA170_tree);
            }
            LBRACE171=(Token)match(input,LBRACE,FOLLOW_LBRACE_in_extendExpr3341); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LBRACE171_tree = (Object)adaptor.create(LBRACE171);
            adaptor.addChild(root_0, LBRACE171_tree);
            }
            pushFollow(FOLLOW_dimensionIntervalList_in_extendExpr3345);
            dil=dimensionIntervalList();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, dil.getTree());
            RBRACE172=(Token)match(input,RBRACE,FOLLOW_RBRACE_in_extendExpr3347); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RBRACE172_tree = (Object)adaptor.create(RBRACE172);
            adaptor.addChild(root_0, RBRACE172_tree);
            }
            RPAREN173=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_extendExpr3349); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN173_tree = (Object)adaptor.create(RPAREN173);
            adaptor.addChild(root_0, RPAREN173_tree);
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
    // wcps.g:309:1: castExpr returns [CastExpr value] : LPAREN e1= rangeType RPAREN e2= coverageExpr ;
    public final wcpsParser.castExpr_return castExpr() throws RecognitionException {
        wcpsParser.castExpr_return retval = new wcpsParser.castExpr_return();
        retval.start = input.LT(1);
        int castExpr_StartIndex = input.index();
        Object root_0 = null;

        Token LPAREN174=null;
        Token RPAREN175=null;
        wcpsParser.rangeType_return e1 = null;

        wcpsParser.coverageExpr_return e2 = null;


        Object LPAREN174_tree=null;
        Object RPAREN175_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 55) ) { return retval; }
            // wcps.g:310:5: ( LPAREN e1= rangeType RPAREN e2= coverageExpr )
            // wcps.g:310:7: LPAREN e1= rangeType RPAREN e2= coverageExpr
            {
            root_0 = (Object)adaptor.nil();

            LPAREN174=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_castExpr3367); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN174_tree = (Object)adaptor.create(LPAREN174);
            adaptor.addChild(root_0, LPAREN174_tree);
            }
            pushFollow(FOLLOW_rangeType_in_castExpr3371);
            e1=rangeType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            RPAREN175=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_castExpr3373); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN175_tree = (Object)adaptor.create(RPAREN175);
            adaptor.addChild(root_0, RPAREN175_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_castExpr3377);
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
    // wcps.g:312:1: rangeType returns [String value] : (type= ( BOOLEAN | CHAR | SHORT | LONG | FLOAT | DOUBLE | COMPLEX | COMPLEX2 ) | UNSIGNED type= ( CHAR | SHORT | LONG ) );
    public final wcpsParser.rangeType_return rangeType() throws RecognitionException {
        wcpsParser.rangeType_return retval = new wcpsParser.rangeType_return();
        retval.start = input.LT(1);
        int rangeType_StartIndex = input.index();
        Object root_0 = null;

        Token type=null;
        Token UNSIGNED176=null;

        Object type_tree=null;
        Object UNSIGNED176_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 56) ) { return retval; }
            // wcps.g:313:5: (type= ( BOOLEAN | CHAR | SHORT | LONG | FLOAT | DOUBLE | COMPLEX | COMPLEX2 ) | UNSIGNED type= ( CHAR | SHORT | LONG ) )
            int alt52=2;
            int LA52_0 = input.LA(1);

            if ( ((LA52_0>=BOOLEAN && LA52_0<=COMPLEX2)) ) {
                alt52=1;
            }
            else if ( (LA52_0==UNSIGNED) ) {
                alt52=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 52, 0, input);

                throw nvae;
            }
            switch (alt52) {
                case 1 :
                    // wcps.g:313:7: type= ( BOOLEAN | CHAR | SHORT | LONG | FLOAT | DOUBLE | COMPLEX | COMPLEX2 )
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
                    // wcps.g:314:7: UNSIGNED type= ( CHAR | SHORT | LONG )
                    {
                    root_0 = (Object)adaptor.nil();

                    UNSIGNED176=(Token)match(input,UNSIGNED,FOLLOW_UNSIGNED_in_rangeType3426); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    UNSIGNED176_tree = (Object)adaptor.create(UNSIGNED176);
                    adaptor.addChild(root_0, UNSIGNED176_tree);
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
    // wcps.g:316:1: fieldExpr returns [SelectExpr value] : e1= coverageAtom DOT e2= fieldName ;
    public final wcpsParser.fieldExpr_return fieldExpr() throws RecognitionException {
        wcpsParser.fieldExpr_return retval = new wcpsParser.fieldExpr_return();
        retval.start = input.LT(1);
        int fieldExpr_StartIndex = input.index();
        Object root_0 = null;

        Token DOT177=null;
        wcpsParser.coverageAtom_return e1 = null;

        wcpsParser.fieldName_return e2 = null;


        Object DOT177_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 57) ) { return retval; }
            // wcps.g:317:5: (e1= coverageAtom DOT e2= fieldName )
            // wcps.g:317:7: e1= coverageAtom DOT e2= fieldName
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_coverageAtom_in_fieldExpr3459);
            e1=coverageAtom();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            DOT177=(Token)match(input,DOT,FOLLOW_DOT_in_fieldExpr3461); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DOT177_tree = (Object)adaptor.create(DOT177);
            adaptor.addChild(root_0, DOT177_tree);
            }
            pushFollow(FOLLOW_fieldName_in_fieldExpr3465);
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
    // wcps.g:321:1: booleanScalarExpr returns [BooleanScalarExpr value] : e1= booleanScalarTerm (op= ( OR | XOR ) e2= booleanScalarTerm )* ;
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
            // wcps.g:322:5: (e1= booleanScalarTerm (op= ( OR | XOR ) e2= booleanScalarTerm )* )
            // wcps.g:322:7: e1= booleanScalarTerm (op= ( OR | XOR ) e2= booleanScalarTerm )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_booleanScalarTerm_in_booleanScalarExpr3490);
            e1=booleanScalarTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:323:7: (op= ( OR | XOR ) e2= booleanScalarTerm )*
            loop53:
            do {
                int alt53=2;
                alt53 = dfa53.predict(input);
                switch (alt53) {
            	case 1 :
            	    // wcps.g:323:8: op= ( OR | XOR ) e2= booleanScalarTerm
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

            	    pushFollow(FOLLOW_booleanScalarTerm_in_booleanScalarExpr3511);
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
            	    break loop53;
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
    // wcps.g:325:1: booleanScalarTerm returns [BooleanScalarExpr value] : e1= booleanScalarNegation (op= AND e2= booleanScalarNegation )* ;
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
            // wcps.g:326:2: (e1= booleanScalarNegation (op= AND e2= booleanScalarNegation )* )
            // wcps.g:326:4: e1= booleanScalarNegation (op= AND e2= booleanScalarNegation )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_booleanScalarNegation_in_booleanScalarTerm3533);
            e1=booleanScalarNegation();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:327:4: (op= AND e2= booleanScalarNegation )*
            loop54:
            do {
                int alt54=2;
                alt54 = dfa54.predict(input);
                switch (alt54) {
            	case 1 :
            	    // wcps.g:327:5: op= AND e2= booleanScalarNegation
            	    {
            	    op=(Token)match(input,AND,FOLLOW_AND_in_booleanScalarTerm3543); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    op_tree = (Object)adaptor.create(op);
            	    adaptor.addChild(root_0, op_tree);
            	    }
            	    pushFollow(FOLLOW_booleanScalarNegation_in_booleanScalarTerm3547);
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
            	    break loop54;
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
    // wcps.g:329:1: booleanScalarNegation returns [BooleanScalarExpr value] : (e1= booleanScalarAtom | op= NOT e1= booleanScalarAtom );
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
            // wcps.g:330:2: (e1= booleanScalarAtom | op= NOT e1= booleanScalarAtom )
            int alt55=2;
            alt55 = dfa55.predict(input);
            switch (alt55) {
                case 1 :
                    // wcps.g:330:4: e1= booleanScalarAtom
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_booleanScalarAtom_in_booleanScalarNegation3568);
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
                    // wcps.g:331:4: op= NOT e1= booleanScalarAtom
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,NOT,FOLLOW_NOT_in_booleanScalarNegation3577); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    pushFollow(FOLLOW_booleanScalarAtom_in_booleanScalarNegation3581);
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
    // wcps.g:333:1: booleanScalarAtom returns [BooleanScalarExpr value] : ( LPAREN e1= booleanScalarExpr RPAREN | s1= stringScalarExpr cop= compOp s2= stringScalarExpr | n1= numericScalarExpr cop= compOp n2= numericScalarExpr | e= BOOLEANCONSTANT );
    public final wcpsParser.booleanScalarAtom_return booleanScalarAtom() throws RecognitionException {
        wcpsParser.booleanScalarAtom_return retval = new wcpsParser.booleanScalarAtom_return();
        retval.start = input.LT(1);
        int booleanScalarAtom_StartIndex = input.index();
        Object root_0 = null;

        Token e=null;
        Token LPAREN178=null;
        Token RPAREN179=null;
        wcpsParser.booleanScalarExpr_return e1 = null;

        wcpsParser.stringScalarExpr_return s1 = null;

        wcpsParser.compOp_return cop = null;

        wcpsParser.stringScalarExpr_return s2 = null;

        wcpsParser.numericScalarExpr_return n1 = null;

        wcpsParser.numericScalarExpr_return n2 = null;


        Object e_tree=null;
        Object LPAREN178_tree=null;
        Object RPAREN179_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 61) ) { return retval; }
            // wcps.g:334:2: ( LPAREN e1= booleanScalarExpr RPAREN | s1= stringScalarExpr cop= compOp s2= stringScalarExpr | n1= numericScalarExpr cop= compOp n2= numericScalarExpr | e= BOOLEANCONSTANT )
            int alt56=4;
            alt56 = dfa56.predict(input);
            switch (alt56) {
                case 1 :
                    // wcps.g:334:4: LPAREN e1= booleanScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    LPAREN178=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_booleanScalarAtom3596); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN178_tree = (Object)adaptor.create(LPAREN178);
                    adaptor.addChild(root_0, LPAREN178_tree);
                    }
                    pushFollow(FOLLOW_booleanScalarExpr_in_booleanScalarAtom3600);
                    e1=booleanScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN179=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_booleanScalarAtom3602); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN179_tree = (Object)adaptor.create(RPAREN179);
                    adaptor.addChild(root_0, RPAREN179_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = (e1!=null?e1.value:null); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:335:4: s1= stringScalarExpr cop= compOp s2= stringScalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_stringScalarExpr_in_booleanScalarAtom3611);
                    s1=stringScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, s1.getTree());
                    pushFollow(FOLLOW_compOp_in_booleanScalarAtom3615);
                    cop=compOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, cop.getTree());
                    pushFollow(FOLLOW_stringScalarExpr_in_booleanScalarAtom3619);
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
                    // wcps.g:336:4: n1= numericScalarExpr cop= compOp n2= numericScalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_numericScalarExpr_in_booleanScalarAtom3629);
                    n1=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, n1.getTree());
                    pushFollow(FOLLOW_compOp_in_booleanScalarAtom3633);
                    cop=compOp();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, cop.getTree());
                    pushFollow(FOLLOW_numericScalarExpr_in_booleanScalarAtom3637);
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
                    // wcps.g:337:4: e= BOOLEANCONSTANT
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)match(input,BOOLEANCONSTANT,FOLLOW_BOOLEANCONSTANT_in_booleanScalarAtom3647); if (state.failed) return retval;
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
    // wcps.g:339:1: numericScalarExpr returns [NumericScalarExpr value] : e1= numericScalarTerm (op= ( PLUS | MINUS ) e2= numericScalarTerm )* ;
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
            // wcps.g:340:2: (e1= numericScalarTerm (op= ( PLUS | MINUS ) e2= numericScalarTerm )* )
            // wcps.g:340:4: e1= numericScalarTerm (op= ( PLUS | MINUS ) e2= numericScalarTerm )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_numericScalarTerm_in_numericScalarExpr3664);
            e1=numericScalarTerm();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
              retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:341:4: (op= ( PLUS | MINUS ) e2= numericScalarTerm )*
            loop57:
            do {
                int alt57=2;
                alt57 = dfa57.predict(input);
                switch (alt57) {
            	case 1 :
            	    // wcps.g:341:5: op= ( PLUS | MINUS ) e2= numericScalarTerm
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

            	    pushFollow(FOLLOW_numericScalarTerm_in_numericScalarExpr3682);
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
            	    break loop57;
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
    // wcps.g:343:1: numericScalarTerm returns [NumericScalarExpr value] : e1= numericScalarFactor (op= ( MULT | DIVIDE ) e2= numericScalarFactor )* ;
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
            // wcps.g:344:2: (e1= numericScalarFactor (op= ( MULT | DIVIDE ) e2= numericScalarFactor )* )
            // wcps.g:344:4: e1= numericScalarFactor (op= ( MULT | DIVIDE ) e2= numericScalarFactor )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_numericScalarFactor_in_numericScalarTerm3701);
            e1=numericScalarFactor();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (e1!=null?e1.value:null); 
            }
            // wcps.g:345:3: (op= ( MULT | DIVIDE ) e2= numericScalarFactor )*
            loop58:
            do {
                int alt58=2;
                alt58 = dfa58.predict(input);
                switch (alt58) {
            	case 1 :
            	    // wcps.g:345:4: op= ( MULT | DIVIDE ) e2= numericScalarFactor
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

            	    pushFollow(FOLLOW_numericScalarFactor_in_numericScalarTerm3718);
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
            	    break loop58;
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
    // wcps.g:347:1: numericScalarFactor returns [NumericScalarExpr value] : ( LPAREN e1= numericScalarExpr RPAREN | op= MINUS e10= numericScalarFactor | op= ABS LPAREN e12= numericScalarExpr RPAREN | op= SQRT LPAREN e11= numericScalarExpr RPAREN | op= ROUND LPAREN e1= numericScalarExpr RPAREN | e= INTEGERCONSTANT | e= FLOATCONSTANT | e= MULT | e2= complexConstant | e3= condenseExpr | e4= variableName );
    public final wcpsParser.numericScalarFactor_return numericScalarFactor() throws RecognitionException {
        wcpsParser.numericScalarFactor_return retval = new wcpsParser.numericScalarFactor_return();
        retval.start = input.LT(1);
        int numericScalarFactor_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;
        Token e=null;
        Token LPAREN180=null;
        Token RPAREN181=null;
        Token LPAREN182=null;
        Token RPAREN183=null;
        Token LPAREN184=null;
        Token RPAREN185=null;
        Token LPAREN186=null;
        Token RPAREN187=null;
        wcpsParser.numericScalarExpr_return e1 = null;

        wcpsParser.numericScalarFactor_return e10 = null;

        wcpsParser.numericScalarExpr_return e12 = null;

        wcpsParser.numericScalarExpr_return e11 = null;

        wcpsParser.complexConstant_return e2 = null;

        wcpsParser.condenseExpr_return e3 = null;

        wcpsParser.variableName_return e4 = null;


        Object op_tree=null;
        Object e_tree=null;
        Object LPAREN180_tree=null;
        Object RPAREN181_tree=null;
        Object LPAREN182_tree=null;
        Object RPAREN183_tree=null;
        Object LPAREN184_tree=null;
        Object RPAREN185_tree=null;
        Object LPAREN186_tree=null;
        Object RPAREN187_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 64) ) { return retval; }
            // wcps.g:348:5: ( LPAREN e1= numericScalarExpr RPAREN | op= MINUS e10= numericScalarFactor | op= ABS LPAREN e12= numericScalarExpr RPAREN | op= SQRT LPAREN e11= numericScalarExpr RPAREN | op= ROUND LPAREN e1= numericScalarExpr RPAREN | e= INTEGERCONSTANT | e= FLOATCONSTANT | e= MULT | e2= complexConstant | e3= condenseExpr | e4= variableName )
            int alt59=11;
            alt59 = dfa59.predict(input);
            switch (alt59) {
                case 1 :
                    // wcps.g:348:7: LPAREN e1= numericScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    LPAREN180=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_numericScalarFactor3738); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN180_tree = (Object)adaptor.create(LPAREN180);
                    adaptor.addChild(root_0, LPAREN180_tree);
                    }
                    pushFollow(FOLLOW_numericScalarExpr_in_numericScalarFactor3742);
                    e1=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN181=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_numericScalarFactor3744); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN181_tree = (Object)adaptor.create(RPAREN181);
                    adaptor.addChild(root_0, RPAREN181_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = (e1!=null?e1.value:null); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:349:7: op= MINUS e10= numericScalarFactor
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,MINUS,FOLLOW_MINUS_in_numericScalarFactor3756); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    pushFollow(FOLLOW_numericScalarFactor_in_numericScalarFactor3760);
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
                    // wcps.g:350:7: op= ABS LPAREN e12= numericScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,ABS,FOLLOW_ABS_in_numericScalarFactor3772); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN182=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_numericScalarFactor3774); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN182_tree = (Object)adaptor.create(LPAREN182);
                    adaptor.addChild(root_0, LPAREN182_tree);
                    }
                    pushFollow(FOLLOW_numericScalarExpr_in_numericScalarFactor3778);
                    e12=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e12.getTree());
                    RPAREN183=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_numericScalarFactor3780); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN183_tree = (Object)adaptor.create(RPAREN183);
                    adaptor.addChild(root_0, RPAREN183_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((op!=null?op.getText():null), (e12!=null?e12.value:null)); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:351:7: op= SQRT LPAREN e11= numericScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,SQRT,FOLLOW_SQRT_in_numericScalarFactor3792); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN184=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_numericScalarFactor3794); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN184_tree = (Object)adaptor.create(LPAREN184);
                    adaptor.addChild(root_0, LPAREN184_tree);
                    }
                    pushFollow(FOLLOW_numericScalarExpr_in_numericScalarFactor3798);
                    e11=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e11.getTree());
                    RPAREN185=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_numericScalarFactor3800); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN185_tree = (Object)adaptor.create(RPAREN185);
                    adaptor.addChild(root_0, RPAREN185_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((op!=null?op.getText():null), (e11!=null?e11.value:null)); 
                    }

                    }
                    break;
                case 5 :
                    // wcps.g:352:7: op= ROUND LPAREN e1= numericScalarExpr RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    op=(Token)match(input,ROUND,FOLLOW_ROUND_in_numericScalarFactor3812); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    op_tree = (Object)adaptor.create(op);
                    adaptor.addChild(root_0, op_tree);
                    }
                    LPAREN186=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_numericScalarFactor3814); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN186_tree = (Object)adaptor.create(LPAREN186);
                    adaptor.addChild(root_0, LPAREN186_tree);
                    }
                    pushFollow(FOLLOW_numericScalarExpr_in_numericScalarFactor3818);
                    e1=numericScalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    RPAREN187=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_numericScalarFactor3820); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN187_tree = (Object)adaptor.create(RPAREN187);
                    adaptor.addChild(root_0, RPAREN187_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((op!=null?op.getText():null), (e1!=null?e1.value:null)); 
                    }

                    }
                    break;
                case 6 :
                    // wcps.g:353:7: e= INTEGERCONSTANT
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)match(input,INTEGERCONSTANT,FOLLOW_INTEGERCONSTANT_in_numericScalarFactor3832); if (state.failed) return retval;
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
                    // wcps.g:354:7: e= FLOATCONSTANT
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)match(input,FLOATCONSTANT,FOLLOW_FLOATCONSTANT_in_numericScalarFactor3844); if (state.failed) return retval;
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
                    // wcps.g:355:7: e= MULT
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)match(input,MULT,FOLLOW_MULT_in_numericScalarFactor3856); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    e_tree = (Object)adaptor.create(e);
                    adaptor.addChild(root_0, e_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((e!=null?e.getText():null));
                    }

                    }
                    break;
                case 9 :
                    // wcps.g:356:7: e2= complexConstant
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_complexConstant_in_numericScalarFactor3868);
                    e2=complexConstant();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e2.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((e2!=null?e2.value:null)); 
                    }

                    }
                    break;
                case 10 :
                    // wcps.g:357:7: e3= condenseExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_condenseExpr_in_numericScalarFactor3880);
                    e3=condenseExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    if ( state.backtracking==0 ) {
                       retval.value = new NumericScalarExpr((e3!=null?e3.value:null)); 
                    }

                    }
                    break;
                case 11 :
                    // wcps.g:358:7: e4= variableName
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_variableName_in_numericScalarFactor3892);
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
    // wcps.g:360:1: compOp returns [String value] : ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE );
    public final wcpsParser.compOp_return compOp() throws RecognitionException {
        wcpsParser.compOp_return retval = new wcpsParser.compOp_return();
        retval.start = input.LT(1);
        int compOp_StartIndex = input.index();
        Object root_0 = null;

        Token EQUALS188=null;
        Token NOTEQUALS189=null;
        Token LT190=null;
        Token GT191=null;
        Token LTE192=null;
        Token GTE193=null;

        Object EQUALS188_tree=null;
        Object NOTEQUALS189_tree=null;
        Object LT190_tree=null;
        Object GT191_tree=null;
        Object LTE192_tree=null;
        Object GTE193_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 65) ) { return retval; }
            // wcps.g:361:2: ( EQUALS | NOTEQUALS | LT | GT | LTE | GTE )
            int alt60=6;
            switch ( input.LA(1) ) {
            case EQUALS:
                {
                alt60=1;
                }
                break;
            case NOTEQUALS:
                {
                alt60=2;
                }
                break;
            case LT:
                {
                alt60=3;
                }
                break;
            case GT:
                {
                alt60=4;
                }
                break;
            case LTE:
                {
                alt60=5;
                }
                break;
            case GTE:
                {
                alt60=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 60, 0, input);

                throw nvae;
            }

            switch (alt60) {
                case 1 :
                    // wcps.g:361:4: EQUALS
                    {
                    root_0 = (Object)adaptor.nil();

                    EQUALS188=(Token)match(input,EQUALS,FOLLOW_EQUALS_in_compOp3910); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EQUALS188_tree = (Object)adaptor.create(EQUALS188);
                    adaptor.addChild(root_0, EQUALS188_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new String("equals"); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:362:4: NOTEQUALS
                    {
                    root_0 = (Object)adaptor.nil();

                    NOTEQUALS189=(Token)match(input,NOTEQUALS,FOLLOW_NOTEQUALS_in_compOp3917); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    NOTEQUALS189_tree = (Object)adaptor.create(NOTEQUALS189);
                    adaptor.addChild(root_0, NOTEQUALS189_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new String("notEqual"); 
                    }

                    }
                    break;
                case 3 :
                    // wcps.g:363:4: LT
                    {
                    root_0 = (Object)adaptor.nil();

                    LT190=(Token)match(input,LT,FOLLOW_LT_in_compOp3924); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LT190_tree = (Object)adaptor.create(LT190);
                    adaptor.addChild(root_0, LT190_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new String("lessThan"); 
                    }

                    }
                    break;
                case 4 :
                    // wcps.g:364:4: GT
                    {
                    root_0 = (Object)adaptor.nil();

                    GT191=(Token)match(input,GT,FOLLOW_GT_in_compOp3931); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    GT191_tree = (Object)adaptor.create(GT191);
                    adaptor.addChild(root_0, GT191_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new String("greaterThan"); 
                    }

                    }
                    break;
                case 5 :
                    // wcps.g:365:4: LTE
                    {
                    root_0 = (Object)adaptor.nil();

                    LTE192=(Token)match(input,LTE,FOLLOW_LTE_in_compOp3938); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LTE192_tree = (Object)adaptor.create(LTE192);
                    adaptor.addChild(root_0, LTE192_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new String("lessOrEqual"); 
                    }

                    }
                    break;
                case 6 :
                    // wcps.g:366:4: GTE
                    {
                    root_0 = (Object)adaptor.nil();

                    GTE193=(Token)match(input,GTE,FOLLOW_GTE_in_compOp3945); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    GTE193_tree = (Object)adaptor.create(GTE193);
                    adaptor.addChild(root_0, GTE193_tree);
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
    // wcps.g:368:1: dimensionIntervalList returns [DimensionIntervalList value] : elem= dimensionIntervalElement ( COMMA elem= dimensionIntervalElement )* ;
    public final wcpsParser.dimensionIntervalList_return dimensionIntervalList() throws RecognitionException {
        wcpsParser.dimensionIntervalList_return retval = new wcpsParser.dimensionIntervalList_return();
        retval.start = input.LT(1);
        int dimensionIntervalList_StartIndex = input.index();
        Object root_0 = null;

        Token COMMA194=null;
        wcpsParser.dimensionIntervalElement_return elem = null;


        Object COMMA194_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 66) ) { return retval; }
            // wcps.g:369:5: (elem= dimensionIntervalElement ( COMMA elem= dimensionIntervalElement )* )
            // wcps.g:369:7: elem= dimensionIntervalElement ( COMMA elem= dimensionIntervalElement )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_dimensionIntervalElement_in_dimensionIntervalList3965);
            elem=dimensionIntervalElement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, elem.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new DimensionIntervalList((elem!=null?elem.value:null)); 
            }
            // wcps.g:370:9: ( COMMA elem= dimensionIntervalElement )*
            loop61:
            do {
                int alt61=2;
                int LA61_0 = input.LA(1);

                if ( (LA61_0==COMMA) ) {
                    alt61=1;
                }


                switch (alt61) {
            	case 1 :
		    // wcps.g:370:10: COMMA elem= dimensionIntervalElement
            	    {
		    COMMA194=(Token)match(input,COMMA,FOLLOW_COMMA_in_dimensionIntervalList3978); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA194_tree = (Object)adaptor.create(COMMA194);
            	    adaptor.addChild(root_0, COMMA194_tree);
            	    }
		    pushFollow(FOLLOW_dimensionIntervalElement_in_dimensionIntervalList3982);
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
            	    break loop61;
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
    // wcps.g:372:1: dimensionIntervalElement returns [DimensionIntervalElement value] : aname= axisName ( COLON crs= crsName )? LPAREN die= dimensionIntervalExpr RPAREN ;
    public final wcpsParser.dimensionIntervalElement_return dimensionIntervalElement() throws RecognitionException {
        wcpsParser.dimensionIntervalElement_return retval = new wcpsParser.dimensionIntervalElement_return();
        retval.start = input.LT(1);
        int dimensionIntervalElement_StartIndex = input.index();
        Object root_0 = null;

        Token COLON195=null;
        Token LPAREN196=null;
        Token RPAREN197=null;
        wcpsParser.axisName_return aname = null;

        wcpsParser.crsName_return crs = null;

        wcpsParser.dimensionIntervalExpr_return die = null;


        Object COLON195_tree=null;
        Object LPAREN196_tree=null;
        Object RPAREN197_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 67) ) { return retval; }
            // wcps.g:373:5: (aname= axisName ( COLON crs= crsName )? LPAREN die= dimensionIntervalExpr RPAREN )
            // wcps.g:373:7: aname= axisName ( COLON crs= crsName )? LPAREN die= dimensionIntervalExpr RPAREN
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_axisName_in_dimensionIntervalElement4007);
            aname=axisName();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new DimensionIntervalElement((aname!=null?aname.value:null)); 
            }
            // wcps.g:373:79: ( COLON crs= crsName )?
            int alt62=2;
            int LA62_0 = input.LA(1);

            if ( (LA62_0==COLON) ) {
                alt62=1;
            }
            switch (alt62) {
                case 1 :
                    // wcps.g:373:80: COLON crs= crsName
                    {
                    COLON195=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionIntervalElement4012); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON195_tree = (Object)adaptor.create(COLON195);
                    adaptor.addChild(root_0, COLON195_tree);
                    }
                    pushFollow(FOLLOW_crsName_in_dimensionIntervalElement4016);
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

            LPAREN196=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_dimensionIntervalElement4027); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN196_tree = (Object)adaptor.create(LPAREN196);
            adaptor.addChild(root_0, LPAREN196_tree);
            }
            pushFollow(FOLLOW_dimensionIntervalExpr_in_dimensionIntervalElement4031);
            die=dimensionIntervalExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, die.getTree());
            RPAREN197=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_dimensionIntervalElement4033); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN197_tree = (Object)adaptor.create(RPAREN197);
            adaptor.addChild(root_0, RPAREN197_tree);
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
    // wcps.g:376:1: dimensionIntervalExpr returns [DimensionIntervalExpr value] : (e1= scalarExpr COLON e2= scalarExpr | DOMAIN LPAREN e3= coverageName COLON e4= axisName COLON e5= crsName RPAREN );
    public final wcpsParser.dimensionIntervalExpr_return dimensionIntervalExpr() throws RecognitionException {
        wcpsParser.dimensionIntervalExpr_return retval = new wcpsParser.dimensionIntervalExpr_return();
        retval.start = input.LT(1);
        int dimensionIntervalExpr_StartIndex = input.index();
        Object root_0 = null;

        Token COLON198=null;
        Token DOMAIN199=null;
        Token LPAREN200=null;
        Token COLON201=null;
        Token COLON202=null;
        Token RPAREN203=null;
        wcpsParser.scalarExpr_return e1 = null;

        wcpsParser.scalarExpr_return e2 = null;

        wcpsParser.coverageName_return e3 = null;

        wcpsParser.axisName_return e4 = null;

        wcpsParser.crsName_return e5 = null;


        Object COLON198_tree=null;
        Object DOMAIN199_tree=null;
        Object LPAREN200_tree=null;
        Object COLON201_tree=null;
        Object COLON202_tree=null;
        Object RPAREN203_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 68) ) { return retval; }
            // wcps.g:377:5: (e1= scalarExpr COLON e2= scalarExpr | DOMAIN LPAREN e3= coverageName COLON e4= axisName COLON e5= crsName RPAREN )
            int alt63=2;
            alt63 = dfa63.predict(input);
            switch (alt63) {
                case 1 :
                    // wcps.g:377:7: e1= scalarExpr COLON e2= scalarExpr
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_scalarExpr_in_dimensionIntervalExpr4056);
                    e1=scalarExpr();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e1.getTree());
                    COLON198=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionIntervalExpr4058); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON198_tree = (Object)adaptor.create(COLON198);
                    adaptor.addChild(root_0, COLON198_tree);
                    }
                    pushFollow(FOLLOW_scalarExpr_in_dimensionIntervalExpr4062);
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
                    // wcps.g:378:7: DOMAIN LPAREN e3= coverageName COLON e4= axisName COLON e5= crsName RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    DOMAIN199=(Token)match(input,DOMAIN,FOLLOW_DOMAIN_in_dimensionIntervalExpr4072); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    DOMAIN199_tree = (Object)adaptor.create(DOMAIN199);
                    adaptor.addChild(root_0, DOMAIN199_tree);
                    }
                    LPAREN200=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_dimensionIntervalExpr4074); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN200_tree = (Object)adaptor.create(LPAREN200);
                    adaptor.addChild(root_0, LPAREN200_tree);
                    }
                    pushFollow(FOLLOW_coverageName_in_dimensionIntervalExpr4078);
                    e3=coverageName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e3.getTree());
                    COLON201=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionIntervalExpr4080); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON201_tree = (Object)adaptor.create(COLON201);
                    adaptor.addChild(root_0, COLON201_tree);
                    }
                    pushFollow(FOLLOW_axisName_in_dimensionIntervalExpr4084);
                    e4=axisName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e4.getTree());
                    COLON202=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionIntervalExpr4086); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON202_tree = (Object)adaptor.create(COLON202);
                    adaptor.addChild(root_0, COLON202_tree);
                    }
                    pushFollow(FOLLOW_crsName_in_dimensionIntervalExpr4090);
                    e5=crsName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, e5.getTree());
                    RPAREN203=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_dimensionIntervalExpr4092); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN203_tree = (Object)adaptor.create(RPAREN203);
                    adaptor.addChild(root_0, RPAREN203_tree);
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
    // wcps.g:380:1: dimensionPointList returns [DimensionPointList value] : elem1= dimensionPointElement ( COMMA elem2= dimensionPointElement )* ;
    public final wcpsParser.dimensionPointList_return dimensionPointList() throws RecognitionException {
        wcpsParser.dimensionPointList_return retval = new wcpsParser.dimensionPointList_return();
        retval.start = input.LT(1);
        int dimensionPointList_StartIndex = input.index();
        Object root_0 = null;

        Token COMMA204=null;
        wcpsParser.dimensionPointElement_return elem1 = null;

        wcpsParser.dimensionPointElement_return elem2 = null;


        Object COMMA204_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 69) ) { return retval; }
            // wcps.g:381:5: (elem1= dimensionPointElement ( COMMA elem2= dimensionPointElement )* )
            // wcps.g:381:7: elem1= dimensionPointElement ( COMMA elem2= dimensionPointElement )*
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_dimensionPointElement_in_dimensionPointList4115);
            elem1=dimensionPointElement();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, elem1.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new DimensionPointList((elem1!=null?elem1.value:null)); 
            }
            // wcps.g:382:6: ( COMMA elem2= dimensionPointElement )*
            loop64:
            do {
                int alt64=2;
                int LA64_0 = input.LA(1);

                if ( (LA64_0==COMMA) ) {
                    alt64=1;
                }


                switch (alt64) {
            	case 1 :
		    // wcps.g:382:7: COMMA elem2= dimensionPointElement
            	    {
		    COMMA204=(Token)match(input,COMMA,FOLLOW_COMMA_in_dimensionPointList4125); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    COMMA204_tree = (Object)adaptor.create(COMMA204);
            	    adaptor.addChild(root_0, COMMA204_tree);
            	    }
		    pushFollow(FOLLOW_dimensionPointElement_in_dimensionPointList4129);
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
            	    break loop64;
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
    // wcps.g:384:1: dimensionPointElement returns [DimensionPointElement value] : (aname= axisName LPAREN dpe= dimensionPoint RPAREN | aname= axisName COLON crs= crsName LPAREN dpe= dimensionPoint RPAREN );
    public final wcpsParser.dimensionPointElement_return dimensionPointElement() throws RecognitionException {
        wcpsParser.dimensionPointElement_return retval = new wcpsParser.dimensionPointElement_return();
        retval.start = input.LT(1);
        int dimensionPointElement_StartIndex = input.index();
        Object root_0 = null;

        Token LPAREN205=null;
        Token RPAREN206=null;
        Token COLON207=null;
        Token LPAREN208=null;
        Token RPAREN209=null;
        wcpsParser.axisName_return aname = null;

        wcpsParser.dimensionPoint_return dpe = null;

        wcpsParser.crsName_return crs = null;


        Object LPAREN205_tree=null;
        Object RPAREN206_tree=null;
        Object COLON207_tree=null;
        Object LPAREN208_tree=null;
        Object RPAREN209_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 70) ) { return retval; }
            // wcps.g:385:5: (aname= axisName LPAREN dpe= dimensionPoint RPAREN | aname= axisName COLON crs= crsName LPAREN dpe= dimensionPoint RPAREN )
            int alt65=2;
            int LA65_0 = input.LA(1);

            if ( (LA65_0==INTEGERCONSTANT||LA65_0==STRING||LA65_0==NAME) ) {
                int LA65_1 = input.LA(2);

                if ( (LA65_1==COLON) ) {
                    alt65=2;
                }
                else if ( (LA65_1==LPAREN) ) {
                    alt65=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 65, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 65, 0, input);

                throw nvae;
            }
            switch (alt65) {
                case 1 :
                    // wcps.g:385:7: aname= axisName LPAREN dpe= dimensionPoint RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_axisName_in_dimensionPointElement4154);
                    aname=axisName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
                    LPAREN205=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_dimensionPointElement4156); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN205_tree = (Object)adaptor.create(LPAREN205);
                    adaptor.addChild(root_0, LPAREN205_tree);
                    }
                    pushFollow(FOLLOW_dimensionPoint_in_dimensionPointElement4160);
                    dpe=dimensionPoint();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dpe.getTree());
                    RPAREN206=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_dimensionPointElement4162); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN206_tree = (Object)adaptor.create(RPAREN206);
                    adaptor.addChild(root_0, RPAREN206_tree);
                    }
                    if ( state.backtracking==0 ) {
                       retval.value = new DimensionPointElement((aname!=null?aname.value:null), (dpe!=null?dpe.value:null)); 
                    }

                    }
                    break;
                case 2 :
                    // wcps.g:386:7: aname= axisName COLON crs= crsName LPAREN dpe= dimensionPoint RPAREN
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_axisName_in_dimensionPointElement4174);
                    aname=axisName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, aname.getTree());
                    COLON207=(Token)match(input,COLON,FOLLOW_COLON_in_dimensionPointElement4176); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    COLON207_tree = (Object)adaptor.create(COLON207);
                    adaptor.addChild(root_0, COLON207_tree);
                    }
                    pushFollow(FOLLOW_crsName_in_dimensionPointElement4180);
                    crs=crsName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, crs.getTree());
                    LPAREN208=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_dimensionPointElement4182); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    LPAREN208_tree = (Object)adaptor.create(LPAREN208);
                    adaptor.addChild(root_0, LPAREN208_tree);
                    }
                    pushFollow(FOLLOW_dimensionPoint_in_dimensionPointElement4186);
                    dpe=dimensionPoint();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, dpe.getTree());
                    RPAREN209=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_dimensionPointElement4188); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    RPAREN209_tree = (Object)adaptor.create(RPAREN209);
                    adaptor.addChild(root_0, RPAREN209_tree);
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
    // wcps.g:388:1: dimensionPoint returns [ScalarExpr value] : e1= scalarExpr ;
    public final wcpsParser.dimensionPoint_return dimensionPoint() throws RecognitionException {
        wcpsParser.dimensionPoint_return retval = new wcpsParser.dimensionPoint_return();
        retval.start = input.LT(1);
        int dimensionPoint_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.scalarExpr_return e1 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 71) ) { return retval; }
            // wcps.g:389:5: (e1= scalarExpr )
            // wcps.g:389:7: e1= scalarExpr
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_scalarExpr_in_dimensionPoint4211);
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
    // wcps.g:391:1: interpolationMethod returns [InterpolationMethod value] : LPAREN type= interpolationType COLON res= nullResistence RPAREN ;
    public final wcpsParser.interpolationMethod_return interpolationMethod() throws RecognitionException {
        wcpsParser.interpolationMethod_return retval = new wcpsParser.interpolationMethod_return();
        retval.start = input.LT(1);
        int interpolationMethod_StartIndex = input.index();
        Object root_0 = null;

        Token LPAREN210=null;
        Token COLON211=null;
        Token RPAREN212=null;
        wcpsParser.interpolationType_return type = null;

        wcpsParser.nullResistence_return res = null;


        Object LPAREN210_tree=null;
        Object COLON211_tree=null;
        Object RPAREN212_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 72) ) { return retval; }
            // wcps.g:392:2: ( LPAREN type= interpolationType COLON res= nullResistence RPAREN )
            // wcps.g:392:4: LPAREN type= interpolationType COLON res= nullResistence RPAREN
            {
            root_0 = (Object)adaptor.nil();

            LPAREN210=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_interpolationMethod4229); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN210_tree = (Object)adaptor.create(LPAREN210);
            adaptor.addChild(root_0, LPAREN210_tree);
            }
            pushFollow(FOLLOW_interpolationType_in_interpolationMethod4233);
            type=interpolationType();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, type.getTree());
            COLON211=(Token)match(input,COLON,FOLLOW_COLON_in_interpolationMethod4235); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COLON211_tree = (Object)adaptor.create(COLON211);
            adaptor.addChild(root_0, COLON211_tree);
            }
            pushFollow(FOLLOW_nullResistence_in_interpolationMethod4239);
            res=nullResistence();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, res.getTree());
            RPAREN212=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_interpolationMethod4241); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN212_tree = (Object)adaptor.create(RPAREN212);
            adaptor.addChild(root_0, RPAREN212_tree);
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
    // wcps.g:394:1: interpolationType returns [String value] : type= ( NEAREST | LINEAR | QUADRATIC | CUBIC ) ;
    public final wcpsParser.interpolationType_return interpolationType() throws RecognitionException {
        wcpsParser.interpolationType_return retval = new wcpsParser.interpolationType_return();
        retval.start = input.LT(1);
        int interpolationType_StartIndex = input.index();
        Object root_0 = null;

        Token type=null;

        Object type_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 73) ) { return retval; }
            // wcps.g:395:2: (type= ( NEAREST | LINEAR | QUADRATIC | CUBIC ) )
            // wcps.g:395:4: type= ( NEAREST | LINEAR | QUADRATIC | CUBIC )
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
    // wcps.g:397:1: nullResistence returns [String value] : resistance= ( FULL | NONE | HALF | OTHER ) ;
    public final wcpsParser.nullResistence_return nullResistence() throws RecognitionException {
        wcpsParser.nullResistence_return retval = new wcpsParser.nullResistence_return();
        retval.start = input.LT(1);
        int nullResistence_StartIndex = input.index();
        Object root_0 = null;

        Token resistance=null;

        Object resistance_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 74) ) { return retval; }
            // wcps.g:398:2: (resistance= ( FULL | NONE | HALF | OTHER ) )
            // wcps.g:398:4: resistance= ( FULL | NONE | HALF | OTHER )
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
    // wcps.g:400:1: condenseOpType returns [CondenseOperation value] : op= ( PLUS | MULT | MAX | MIN | AND | OR ) ;
    public final wcpsParser.condenseOpType_return condenseOpType() throws RecognitionException {
        wcpsParser.condenseOpType_return retval = new wcpsParser.condenseOpType_return();
        retval.start = input.LT(1);
        int condenseOpType_StartIndex = input.index();
        Object root_0 = null;

        Token op=null;

        Object op_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 75) ) { return retval; }
            // wcps.g:401:2: (op= ( PLUS | MULT | MAX | MIN | AND | OR ) )
            // wcps.g:401:4: op= ( PLUS | MULT | MAX | MIN | AND | OR )
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
    // wcps.g:403:1: fieldName returns [String value] : name ;
    public final wcpsParser.fieldName_return fieldName() throws RecognitionException {
        wcpsParser.fieldName_return retval = new wcpsParser.fieldName_return();
        retval.start = input.LT(1);
        int fieldName_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.name_return name213 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 76) ) { return retval; }
            // wcps.g:404:2: ( name )
            // wcps.g:404:4: name
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_name_in_fieldName4335);
            name213=name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, name213.getTree());
            if ( state.backtracking==0 ) {
               retval.value = new String((name213!=null?name213.value:null));
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
    // wcps.g:406:1: constant returns [String value] : (e= ( STRING | BOOLEANCONSTANT | INTEGERCONSTANT | FLOATCONSTANT ) | e1= complexConstant );
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
            // wcps.g:407:2: (e= ( STRING | BOOLEANCONSTANT | INTEGERCONSTANT | FLOATCONSTANT ) | e1= complexConstant )
            int alt66=2;
            int LA66_0 = input.LA(1);

            if ( ((LA66_0>=FLOATCONSTANT && LA66_0<=INTEGERCONSTANT)||LA66_0==STRING||LA66_0==BOOLEANCONSTANT) ) {
                alt66=1;
            }
            else if ( (LA66_0==LPAREN) ) {
                alt66=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 66, 0, input);

                throw nvae;
            }
            switch (alt66) {
                case 1 :
                    // wcps.g:407:4: e= ( STRING | BOOLEANCONSTANT | INTEGERCONSTANT | FLOATCONSTANT )
                    {
                    root_0 = (Object)adaptor.nil();

                    e=(Token)input.LT(1);
                    if ( (input.LA(1)>=FLOATCONSTANT && input.LA(1)<=INTEGERCONSTANT)||input.LA(1)==STRING||input.LA(1)==BOOLEANCONSTANT ) {
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
                    // wcps.g:408:4: e1= complexConstant
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_complexConstant_in_constant4369);
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
    // wcps.g:410:1: complexConstant returns [String value] : LPAREN re1= FLOATCONSTANT COMMA im1= FLOATCONSTANT RPAREN ;
    public final wcpsParser.complexConstant_return complexConstant() throws RecognitionException {
        wcpsParser.complexConstant_return retval = new wcpsParser.complexConstant_return();
        retval.start = input.LT(1);
        int complexConstant_StartIndex = input.index();
        Object root_0 = null;

        Token re1=null;
        Token im1=null;
        Token LPAREN214=null;
        Token COMMA215=null;
        Token RPAREN216=null;

        Object re1_tree=null;
        Object im1_tree=null;
        Object LPAREN214_tree=null;
        Object COMMA215_tree=null;
        Object RPAREN216_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 78) ) { return retval; }
            // wcps.g:411:2: ( LPAREN re1= FLOATCONSTANT COMMA im1= FLOATCONSTANT RPAREN )
            // wcps.g:411:4: LPAREN re1= FLOATCONSTANT COMMA im1= FLOATCONSTANT RPAREN
            {
            root_0 = (Object)adaptor.nil();

            LPAREN214=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_complexConstant4384); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            LPAREN214_tree = (Object)adaptor.create(LPAREN214);
            adaptor.addChild(root_0, LPAREN214_tree);
            }
            re1=(Token)match(input,FLOATCONSTANT,FOLLOW_FLOATCONSTANT_in_complexConstant4388); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            re1_tree = (Object)adaptor.create(re1);
            adaptor.addChild(root_0, re1_tree);
            }
            COMMA215=(Token)match(input,COMMA,FOLLOW_COMMA_in_complexConstant4390); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            COMMA215_tree = (Object)adaptor.create(COMMA215);
            adaptor.addChild(root_0, COMMA215_tree);
            }
            im1=(Token)match(input,FLOATCONSTANT,FOLLOW_FLOATCONSTANT_in_complexConstant4394); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            im1_tree = (Object)adaptor.create(im1);
            adaptor.addChild(root_0, im1_tree);
            }
            RPAREN216=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_complexConstant4396); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RPAREN216_tree = (Object)adaptor.create(RPAREN216);
            adaptor.addChild(root_0, RPAREN216_tree);
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
    // wcps.g:413:1: stringConstant returns [String value] : s= STRING ;
    public final wcpsParser.stringConstant_return stringConstant() throws RecognitionException {
        wcpsParser.stringConstant_return retval = new wcpsParser.stringConstant_return();
        retval.start = input.LT(1);
        int stringConstant_StartIndex = input.index();
        Object root_0 = null;

        Token s=null;

        Object s_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 79) ) { return retval; }
            // wcps.g:414:2: (s= STRING )
            // wcps.g:414:4: s= STRING
            {
            root_0 = (Object)adaptor.nil();

            s=(Token)match(input,STRING,FOLLOW_STRING_in_stringConstant4413); if (state.failed) return retval;
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
    // wcps.g:416:1: name returns [String value] : var= ( NAME | STRING | INTEGERCONSTANT ) ;
    public final wcpsParser.name_return name() throws RecognitionException {
        wcpsParser.name_return retval = new wcpsParser.name_return();
        retval.start = input.LT(1);
        int name_StartIndex = input.index();
        Object root_0 = null;

        Token var=null;

        Object var_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 80) ) { return retval; }
            // wcps.g:417:2: (var= ( NAME | STRING | INTEGERCONSTANT ) )
            // wcps.g:417:4: var= ( NAME | STRING | INTEGERCONSTANT )
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
    // wcps.g:419:1: crsName returns [String value] : s= stringConstant ;
    public final wcpsParser.crsName_return crsName() throws RecognitionException {
        wcpsParser.crsName_return retval = new wcpsParser.crsName_return();
        retval.start = input.LT(1);
        int crsName_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.stringConstant_return s = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 81) ) { return retval; }
            // wcps.g:420:2: (s= stringConstant )
            // wcps.g:420:4: s= stringConstant
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_stringConstant_in_crsName4457);
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
    // wcps.g:422:1: axisName returns [String value] : type1= name ;
    public final wcpsParser.axisName_return axisName() throws RecognitionException {
        wcpsParser.axisName_return retval = new wcpsParser.axisName_return();
        retval.start = input.LT(1);
        int axisName_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.name_return type1 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 82) ) { return retval; }
            // wcps.g:423:2: (type1= name )
            // wcps.g:423:4: type1= name
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_name_in_axisName4474);
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
    // wcps.g:425:1: variableName returns [String value] : var= VARIABLE_DOLLAR ;
    public final wcpsParser.variableName_return variableName() throws RecognitionException {
        wcpsParser.variableName_return retval = new wcpsParser.variableName_return();
        retval.start = input.LT(1);
        int variableName_StartIndex = input.index();
        Object root_0 = null;

        Token var=null;

        Object var_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 83) ) { return retval; }
            // wcps.g:426:2: (var= VARIABLE_DOLLAR )
            // wcps.g:426:4: var= VARIABLE_DOLLAR
            {
            root_0 = (Object)adaptor.nil();

            var=(Token)match(input,VARIABLE_DOLLAR,FOLLOW_VARIABLE_DOLLAR_in_variableName4491); if (state.failed) return retval;
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
    // wcps.g:428:1: coverageVariable returns [String value] : var= NAME ;
    public final wcpsParser.coverageVariable_return coverageVariable() throws RecognitionException {
        wcpsParser.coverageVariable_return retval = new wcpsParser.coverageVariable_return();
        retval.start = input.LT(1);
        int coverageVariable_StartIndex = input.index();
        Object root_0 = null;

        Token var=null;

        Object var_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 84) ) { return retval; }
            // wcps.g:429:2: (var= NAME )
            // wcps.g:429:4: var= NAME
            {
            root_0 = (Object)adaptor.nil();

            var=(Token)match(input,NAME,FOLLOW_NAME_in_coverageVariable4508); if (state.failed) return retval;
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
    // wcps.g:431:1: coverageName returns [String value] : name ;
    public final wcpsParser.coverageName_return coverageName() throws RecognitionException {
        wcpsParser.coverageName_return retval = new wcpsParser.coverageName_return();
        retval.start = input.LT(1);
        int coverageName_StartIndex = input.index();
        Object root_0 = null;

        wcpsParser.name_return name217 = null;



        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 85) ) { return retval; }
            // wcps.g:432:2: ( name )
            // wcps.g:432:4: name
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_name_in_coverageName4523);
            name217=name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, name217.getTree());
            if ( state.backtracking==0 ) {
               retval.value = (name217!=null?name217.value:null); 
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

    public static class switchExpr_return extends ParserRuleReturnScope {
        public SwitchExpr value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "switchExpr"
    // wcps.g:434:1: switchExpr returns [SwitchExpr value] : SWITCH CASE cond= coverageExpr RETURN res= coverageExpr ( CASE cond= coverageExpr RETURN res= coverageExpr )* DEFAULT RETURN def= coverageExpr ;
    public final wcpsParser.switchExpr_return switchExpr() throws RecognitionException {
        wcpsParser.switchExpr_return retval = new wcpsParser.switchExpr_return();
        retval.start = input.LT(1);
        int switchExpr_StartIndex = input.index();
        Object root_0 = null;

        Token SWITCH218=null;
        Token CASE219=null;
        Token RETURN220=null;
        Token CASE221=null;
        Token RETURN222=null;
        Token DEFAULT223=null;
        Token RETURN224=null;
        wcpsParser.coverageExpr_return cond = null;

        wcpsParser.coverageExpr_return res = null;

        wcpsParser.coverageExpr_return def = null;


        Object SWITCH218_tree=null;
        Object CASE219_tree=null;
        Object RETURN220_tree=null;
        Object CASE221_tree=null;
        Object RETURN222_tree=null;
        Object DEFAULT223_tree=null;
        Object RETURN224_tree=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 86) ) { return retval; }
            // wcps.g:435:5: ( SWITCH CASE cond= coverageExpr RETURN res= coverageExpr ( CASE cond= coverageExpr RETURN res= coverageExpr )* DEFAULT RETURN def= coverageExpr )
            // wcps.g:435:7: SWITCH CASE cond= coverageExpr RETURN res= coverageExpr ( CASE cond= coverageExpr RETURN res= coverageExpr )* DEFAULT RETURN def= coverageExpr
            {
            root_0 = (Object)adaptor.nil();

            SWITCH218=(Token)match(input,SWITCH,FOLLOW_SWITCH_in_switchExpr4541); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SWITCH218_tree = (Object)adaptor.create(SWITCH218);
            adaptor.addChild(root_0, SWITCH218_tree);
            }
            CASE219=(Token)match(input,CASE,FOLLOW_CASE_in_switchExpr4543); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            CASE219_tree = (Object)adaptor.create(CASE219);
            adaptor.addChild(root_0, CASE219_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_switchExpr4549);
            cond=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, cond.getTree());
            RETURN220=(Token)match(input,RETURN,FOLLOW_RETURN_in_switchExpr4551); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RETURN220_tree = (Object)adaptor.create(RETURN220);
            adaptor.addChild(root_0, RETURN220_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_switchExpr4557);
            res=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, res.getTree());
            if ( state.backtracking==0 ) {
              retval.value = new SwitchExpr(); retval.value.add((cond!=null?cond.value:null)); retval.value.add((res!=null?res.value:null));
            }
            // wcps.g:436:9: ( CASE cond= coverageExpr RETURN res= coverageExpr )*
            loop67:
            do {
                int alt67=2;
                int LA67_0 = input.LA(1);

                if ( (LA67_0==CASE) ) {
                    alt67=1;
                }


                switch (alt67) {
            	case 1 :
		    // wcps.g:436:10: CASE cond= coverageExpr RETURN res= coverageExpr
            	    {
		    CASE221=(Token)match(input,CASE,FOLLOW_CASE_in_switchExpr4570); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    CASE221_tree = (Object)adaptor.create(CASE221);
            	    adaptor.addChild(root_0, CASE221_tree);
            	    }
		    pushFollow(FOLLOW_coverageExpr_in_switchExpr4576);
            	    cond=coverageExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, cond.getTree());
		    RETURN222=(Token)match(input,RETURN,FOLLOW_RETURN_in_switchExpr4578); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	    RETURN222_tree = (Object)adaptor.create(RETURN222);
            	    adaptor.addChild(root_0, RETURN222_tree);
            	    }
		    pushFollow(FOLLOW_coverageExpr_in_switchExpr4584);
            	    res=coverageExpr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) adaptor.addChild(root_0, res.getTree());
            	    if ( state.backtracking==0 ) {
            	      retval.value.add((cond!=null?cond.value:null)); retval.value.add((res!=null?res.value:null));
            	    }

            	    }
            	    break;

            	default :
            	    break loop67;
                }
            } while (true);

            DEFAULT223=(Token)match(input,DEFAULT,FOLLOW_DEFAULT_in_switchExpr4598); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            DEFAULT223_tree = (Object)adaptor.create(DEFAULT223);
            adaptor.addChild(root_0, DEFAULT223_tree);
            }
            RETURN224=(Token)match(input,RETURN,FOLLOW_RETURN_in_switchExpr4600); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            RETURN224_tree = (Object)adaptor.create(RETURN224);
            adaptor.addChild(root_0, RETURN224_tree);
            }
            pushFollow(FOLLOW_coverageExpr_in_switchExpr4606);
            def=coverageExpr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) adaptor.addChild(root_0, def.getTree());
            if ( state.backtracking==0 ) {
              retval.value.add((def!=null?def.value:null));
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
            if ( state.backtracking>0 ) { memoize(input, 86, switchExpr_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "switchExpr"

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
        wcpsParser.coverageAtom_return e1 = null;


        // wcps.g:99:7: (e1= coverageAtom )
        // wcps.g:99:7: e1= coverageAtom
        {
        pushFollow(FOLLOW_coverageAtom_in_synpred25_wcps704);
        e1=coverageAtom();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred25_wcps

    // $ANTLR start synpred26_wcps
    public final void synpred26_wcps_fragment() throws RecognitionException {   
        wcpsParser.scalarExpr_return e2 = null;


        // wcps.g:103:7: (e2= scalarExpr )
        // wcps.g:103:7: e2= scalarExpr
        {
        pushFollow(FOLLOW_scalarExpr_in_synpred26_wcps739);
        e2=scalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred26_wcps

    // $ANTLR start synpred28_wcps
    public final void synpred28_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageExpr_return e7 = null;


        // wcps.g:105:7: ( LPAREN e7= coverageExpr RPAREN )
        // wcps.g:105:7: LPAREN e7= coverageExpr RPAREN
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred28_wcps761); if (state.failed) return ;
        pushFollow(FOLLOW_coverageExpr_in_synpred28_wcps765);
        e7=coverageExpr();

        state._fsp--;
        if (state.failed) return ;
        match(input,RPAREN,FOLLOW_RPAREN_in_synpred28_wcps767); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred28_wcps

    // $ANTLR start synpred29_wcps
    public final void synpred29_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageConstantExpr_return e3 = null;


        // wcps.g:106:7: (e3= coverageConstantExpr )
        // wcps.g:106:7: e3= coverageConstantExpr
        {
        pushFollow(FOLLOW_coverageConstantExpr_in_synpred29_wcps780);
        e3=coverageConstantExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred29_wcps

    // $ANTLR start synpred30_wcps
    public final void synpred30_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageConstructorExpr_return e4 = null;


        // wcps.g:107:7: (e4= coverageConstructorExpr )
        // wcps.g:107:7: e4= coverageConstructorExpr
        {
        pushFollow(FOLLOW_coverageConstructorExpr_in_synpred30_wcps792);
        e4=coverageConstructorExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred30_wcps

    // $ANTLR start synpred32_wcps
    public final void synpred32_wcps_fragment() throws RecognitionException {   
        wcpsParser.metaDataExpr_return e1 = null;


        // wcps.g:112:7: (e1= metaDataExpr )
        // wcps.g:112:7: e1= metaDataExpr
        {
        pushFollow(FOLLOW_metaDataExpr_in_synpred32_wcps842);
        e1=metaDataExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred32_wcps

    // $ANTLR start synpred33_wcps
    public final void synpred33_wcps_fragment() throws RecognitionException {   
        wcpsParser.condenseExpr_return e2 = null;


        // wcps.g:113:7: (e2= condenseExpr )
        // wcps.g:113:7: e2= condenseExpr
        {
        pushFollow(FOLLOW_condenseExpr_in_synpred33_wcps855);
        e2=condenseExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred33_wcps

    // $ANTLR start synpred34_wcps
    public final void synpred34_wcps_fragment() throws RecognitionException {   
        wcpsParser.booleanScalarExpr_return e3 = null;


        // wcps.g:114:7: (e3= booleanScalarExpr )
        // wcps.g:114:7: e3= booleanScalarExpr
        {
        pushFollow(FOLLOW_booleanScalarExpr_in_synpred34_wcps868);
        e3=booleanScalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred34_wcps

    // $ANTLR start synpred35_wcps
    public final void synpred35_wcps_fragment() throws RecognitionException {   
        wcpsParser.numericScalarExpr_return e4 = null;


        // wcps.g:115:7: (e4= numericScalarExpr )
        // wcps.g:115:7: e4= numericScalarExpr
        {
        pushFollow(FOLLOW_numericScalarExpr_in_synpred35_wcps882);
        e4=numericScalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred35_wcps

    // $ANTLR start synpred36_wcps
    public final void synpred36_wcps_fragment() throws RecognitionException {   
        wcpsParser.stringScalarExpr_return e5 = null;


        // wcps.g:116:7: (e5= stringScalarExpr )
        // wcps.g:116:7: e5= stringScalarExpr
        {
        pushFollow(FOLLOW_stringScalarExpr_in_synpred36_wcps895);
        e5=stringScalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred36_wcps

    // $ANTLR start synpred56_wcps
    public final void synpred56_wcps_fragment() throws RecognitionException {   
        wcpsParser.subsetExpr_return e5 = null;


        // wcps.g:165:7: (e5= subsetExpr )
        // wcps.g:165:7: e5= subsetExpr
        {
        pushFollow(FOLLOW_subsetExpr_in_synpred56_wcps1475);
        e5=subsetExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred56_wcps

    // $ANTLR start synpred57_wcps
    public final void synpred57_wcps_fragment() throws RecognitionException {   
        wcpsParser.unaryInducedExpr_return e2 = null;


        // wcps.g:166:7: (e2= unaryInducedExpr )
        // wcps.g:166:7: e2= unaryInducedExpr
        {
        pushFollow(FOLLOW_unaryInducedExpr_in_synpred57_wcps1488);
        e2=unaryInducedExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred57_wcps

    // $ANTLR start synpred60_wcps
    public final void synpred60_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageAtom_return e1 = null;


        // wcps.g:169:7: (e1= coverageAtom )
        // wcps.g:169:7: e1= coverageAtom
        {
        pushFollow(FOLLOW_coverageAtom_in_synpred60_wcps1524);
        e1=coverageAtom();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred60_wcps

    // $ANTLR start synpred61_wcps
    public final void synpred61_wcps_fragment() throws RecognitionException {   
        wcpsParser.scalarExpr_return e6 = null;


        // wcps.g:170:7: (e6= scalarExpr )
        // wcps.g:170:7: e6= scalarExpr
        {
        pushFollow(FOLLOW_scalarExpr_in_synpred61_wcps1536);
        e6=scalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred61_wcps

    // $ANTLR start synpred62_wcps
    public final void synpred62_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageVariable_return e7 = null;


        // wcps.g:171:7: (e7= coverageVariable )
        // wcps.g:171:7: e7= coverageVariable
        {
        pushFollow(FOLLOW_coverageVariable_in_synpred62_wcps1548);
        e7=coverageVariable();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred62_wcps

    // $ANTLR start synpred63_wcps
    public final void synpred63_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageConstantExpr_return e8 = null;


        // wcps.g:172:7: (e8= coverageConstantExpr )
        // wcps.g:172:7: e8= coverageConstantExpr
        {
        pushFollow(FOLLOW_coverageConstantExpr_in_synpred63_wcps1560);
        e8=coverageConstantExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred63_wcps

    // $ANTLR start synpred64_wcps
    public final void synpred64_wcps_fragment() throws RecognitionException {   
        wcpsParser.coverageConstructorExpr_return e9 = null;


        // wcps.g:173:7: (e9= coverageConstructorExpr )
        // wcps.g:173:7: e9= coverageConstructorExpr
        {
        pushFollow(FOLLOW_coverageConstructorExpr_in_synpred64_wcps1572);
        e9=coverageConstructorExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred64_wcps

    // $ANTLR start synpred65_wcps
    public final void synpred65_wcps_fragment() throws RecognitionException {   
        wcpsParser.setMetaDataExpr_return e10 = null;


        // wcps.g:174:7: (e10= setMetaDataExpr )
        // wcps.g:174:7: e10= setMetaDataExpr
        {
        pushFollow(FOLLOW_setMetaDataExpr_in_synpred65_wcps1585);
        e10=setMetaDataExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred65_wcps

    // $ANTLR start synpred66_wcps
    public final void synpred66_wcps_fragment() throws RecognitionException {   
        wcpsParser.rangeConstructorExpr_return e11 = null;


        // wcps.g:175:7: (e11= rangeConstructorExpr )
        // wcps.g:175:7: e11= rangeConstructorExpr
        {
        pushFollow(FOLLOW_rangeConstructorExpr_in_synpred66_wcps1598);
        e11=rangeConstructorExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred66_wcps

    // $ANTLR start synpred83_wcps
    public final void synpred83_wcps_fragment() throws RecognitionException {   
        wcpsParser.fieldExpr_return e6 = null;


        // wcps.g:232:7: (e6= fieldExpr )
        // wcps.g:232:7: e6= fieldExpr
        {
        pushFollow(FOLLOW_fieldExpr_in_synpred83_wcps2293);
        e6=fieldExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred83_wcps

    // $ANTLR start synpred84_wcps
    public final void synpred84_wcps_fragment() throws RecognitionException {   
        wcpsParser.unaryArithmeticExpr_return e1 = null;


        // wcps.g:233:4: (e1= unaryArithmeticExpr )
        // wcps.g:233:4: e1= unaryArithmeticExpr
        {
        pushFollow(FOLLOW_unaryArithmeticExpr_in_synpred84_wcps2302);
        e1=unaryArithmeticExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred84_wcps

    // $ANTLR start synpred87_wcps
    public final void synpred87_wcps_fragment() throws RecognitionException {   
        wcpsParser.booleanExpr_return e4 = null;


        // wcps.g:236:7: (e4= booleanExpr )
        // wcps.g:236:7: e4= booleanExpr
        {
        pushFollow(FOLLOW_booleanExpr_in_synpred87_wcps2338);
        e4=booleanExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred87_wcps

    // $ANTLR start synpred116_wcps
    public final void synpred116_wcps_fragment() throws RecognitionException {   
        wcpsParser.trimExpr_return e1 = null;


        // wcps.g:284:4: (e1= trimExpr )
        // wcps.g:284:4: e1= trimExpr
        {
        pushFollow(FOLLOW_trimExpr_in_synpred116_wcps2949);
        e1=trimExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred116_wcps

    // $ANTLR start synpred117_wcps
    public final void synpred117_wcps_fragment() throws RecognitionException {   
        wcpsParser.sliceExpr_return e2 = null;


        // wcps.g:285:4: (e2= sliceExpr )
        // wcps.g:285:4: e2= sliceExpr
        {
        pushFollow(FOLLOW_sliceExpr_in_synpred117_wcps2958);
        e2=sliceExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred117_wcps

    // $ANTLR start synpred120_wcps
    public final void synpred120_wcps_fragment() throws RecognitionException {   
        wcpsParser.dimensionIntervalElement_return el1 = null;


        // wcps.g:295:32: (el1= dimensionIntervalElement )
        // wcps.g:295:32: el1= dimensionIntervalElement
        {
        pushFollow(FOLLOW_dimensionIntervalElement_in_synpred120_wcps3087);
        el1=dimensionIntervalElement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred120_wcps

    // $ANTLR start synpred121_wcps
    public final void synpred121_wcps_fragment() throws RecognitionException {   
        wcpsParser.dimensionIntervalElement_return el3 = null;


        // wcps.g:298:32: (el3= dimensionIntervalElement )
        // wcps.g:298:32: el3= dimensionIntervalElement
        {
        pushFollow(FOLLOW_dimensionIntervalElement_in_synpred121_wcps3196);
        el3=dimensionIntervalElement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred121_wcps

    // $ANTLR start synpred135_wcps
    public final void synpred135_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.booleanScalarTerm_return e2 = null;


        // wcps.g:323:8: (op= ( OR | XOR ) e2= booleanScalarTerm )
        // wcps.g:323:8: op= ( OR | XOR ) e2= booleanScalarTerm
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

        pushFollow(FOLLOW_booleanScalarTerm_in_synpred135_wcps3511);
        e2=booleanScalarTerm();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred135_wcps

    // $ANTLR start synpred136_wcps
    public final void synpred136_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.booleanScalarNegation_return e2 = null;


        // wcps.g:327:5: (op= AND e2= booleanScalarNegation )
        // wcps.g:327:5: op= AND e2= booleanScalarNegation
        {
        op=(Token)match(input,AND,FOLLOW_AND_in_synpred136_wcps3543); if (state.failed) return ;
        pushFollow(FOLLOW_booleanScalarNegation_in_synpred136_wcps3547);
        e2=booleanScalarNegation();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred136_wcps

    // $ANTLR start synpred138_wcps
    public final void synpred138_wcps_fragment() throws RecognitionException {   
        wcpsParser.booleanScalarExpr_return e1 = null;


        // wcps.g:334:4: ( LPAREN e1= booleanScalarExpr RPAREN )
        // wcps.g:334:4: LPAREN e1= booleanScalarExpr RPAREN
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred138_wcps3596); if (state.failed) return ;
        pushFollow(FOLLOW_booleanScalarExpr_in_synpred138_wcps3600);
        e1=booleanScalarExpr();

        state._fsp--;
        if (state.failed) return ;
        match(input,RPAREN,FOLLOW_RPAREN_in_synpred138_wcps3602); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred138_wcps

    // $ANTLR start synpred140_wcps
    public final void synpred140_wcps_fragment() throws RecognitionException {   
        wcpsParser.numericScalarExpr_return n1 = null;

        wcpsParser.compOp_return cop = null;

        wcpsParser.numericScalarExpr_return n2 = null;


        // wcps.g:336:4: (n1= numericScalarExpr cop= compOp n2= numericScalarExpr )
        // wcps.g:336:4: n1= numericScalarExpr cop= compOp n2= numericScalarExpr
        {
        pushFollow(FOLLOW_numericScalarExpr_in_synpred140_wcps3629);
        n1=numericScalarExpr();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_compOp_in_synpred140_wcps3633);
        cop=compOp();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_numericScalarExpr_in_synpred140_wcps3637);
        n2=numericScalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred140_wcps

    // $ANTLR start synpred142_wcps
    public final void synpred142_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.numericScalarTerm_return e2 = null;


        // wcps.g:341:5: (op= ( PLUS | MINUS ) e2= numericScalarTerm )
        // wcps.g:341:5: op= ( PLUS | MINUS ) e2= numericScalarTerm
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

        pushFollow(FOLLOW_numericScalarTerm_in_synpred142_wcps3682);
        e2=numericScalarTerm();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred142_wcps

    // $ANTLR start synpred144_wcps
    public final void synpred144_wcps_fragment() throws RecognitionException {   
        Token op=null;
        wcpsParser.numericScalarFactor_return e2 = null;


        // wcps.g:345:4: (op= ( MULT | DIVIDE ) e2= numericScalarFactor )
        // wcps.g:345:4: op= ( MULT | DIVIDE ) e2= numericScalarFactor
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

        pushFollow(FOLLOW_numericScalarFactor_in_synpred144_wcps3718);
        e2=numericScalarFactor();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred144_wcps

    // $ANTLR start synpred145_wcps
    public final void synpred145_wcps_fragment() throws RecognitionException {   
        wcpsParser.numericScalarExpr_return e1 = null;


        // wcps.g:348:7: ( LPAREN e1= numericScalarExpr RPAREN )
        // wcps.g:348:7: LPAREN e1= numericScalarExpr RPAREN
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred145_wcps3738); if (state.failed) return ;
        pushFollow(FOLLOW_numericScalarExpr_in_synpred145_wcps3742);
        e1=numericScalarExpr();

        state._fsp--;
        if (state.failed) return ;
        match(input,RPAREN,FOLLOW_RPAREN_in_synpred145_wcps3744); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred145_wcps

    // $ANTLR start synpred153_wcps
    public final void synpred153_wcps_fragment() throws RecognitionException {
        wcpsParser.complexConstant_return e2 = null;


        // wcps.g:356:7: (e2= complexConstant )
        // wcps.g:356:7: e2= complexConstant
        {
        pushFollow(FOLLOW_complexConstant_in_synpred153_wcps3868);
        e2=complexConstant();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred153_wcps

    // $ANTLR start synpred162_wcps
    public final void synpred162_wcps_fragment() throws RecognitionException {
        wcpsParser.scalarExpr_return e1 = null;

        wcpsParser.scalarExpr_return e2 = null;


        // wcps.g:377:7: (e1= scalarExpr COLON e2= scalarExpr )
        // wcps.g:377:7: e1= scalarExpr COLON e2= scalarExpr
        {
        pushFollow(FOLLOW_scalarExpr_in_synpred162_wcps4056);
        e1=scalarExpr();

        state._fsp--;
        if (state.failed) return ;
        match(input,COLON,FOLLOW_COLON_in_synpred162_wcps4058); if (state.failed) return ;
        pushFollow(FOLLOW_scalarExpr_in_synpred162_wcps4062);
        e2=scalarExpr();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred162_wcps

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
    public final boolean synpred65_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred65_wcps_fragment(); // can never throw exception
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
    public final boolean synpred136_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred136_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred30_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred30_wcps_fragment(); // can never throw exception
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
    public final boolean synpred145_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred145_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred144_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred144_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred87_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred87_wcps_fragment(); // can never throw exception
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
    public final boolean synpred153_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred153_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred57_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred57_wcps_fragment(); // can never throw exception
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
    public final boolean synpred26_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred26_wcps_fragment(); // can never throw exception
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
    public final boolean synpred83_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred83_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred66_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred66_wcps_fragment(); // can never throw exception
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
    public final boolean synpred138_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred138_wcps_fragment(); // can never throw exception
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
    public final boolean synpred162_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred162_wcps_fragment(); // can never throw exception
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
    public final boolean synpred84_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred84_wcps_fragment(); // can never throw exception
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
    public final boolean synpred121_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred121_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred142_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred142_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred117_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred117_wcps_fragment(); // can never throw exception
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
    public final boolean synpred36_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred36_wcps_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred120_wcps() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred120_wcps_fragment(); // can never throw exception
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
    protected DFA46 dfa46 = new DFA46(this);
    protected DFA47 dfa47 = new DFA47(this);
    protected DFA51 dfa51 = new DFA51(this);
    protected DFA53 dfa53 = new DFA53(this);
    protected DFA54 dfa54 = new DFA54(this);
    protected DFA55 dfa55 = new DFA55(this);
    protected DFA56 dfa56 = new DFA56(this);
    protected DFA57 dfa57 = new DFA57(this);
    protected DFA58 dfa58 = new DFA58(this);
    protected DFA59 dfa59 = new DFA59(this);
    protected DFA63 dfa63 = new DFA63(this);
    static final String DFA4_eotS =
        "\31\uffff";
    static final String DFA4_eofS =
        "\31\uffff";
    static final String DFA4_minS =
        "\1\6\30\uffff";
    static final String DFA4_maxS =
        "\1\155\30\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\1\1\2\1\3\25\uffff";
    static final String DFA4_specialS =
        "\31\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\3\4\uffff\1\1\1\2\12\uffff\2\3\2\uffff\20\3\21\uffff\2\3"+
            "\6\uffff\2\3\11\uffff\1\3\1\uffff\2\3\20\uffff\1\3\11\uffff"+
            "\1\3",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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
        "\104\uffff";
    static final String DFA6_eofS =
        "\1\1\103\uffff";
    static final String DFA6_minS =
        "\1\7\10\uffff\1\0\72\uffff";
    static final String DFA6_maxS =
        "\1\160\10\uffff\1\0\72\uffff";
    static final String DFA6_acceptS =
        "\1\uffff\1\2\101\uffff\1\1";
    static final String DFA6_specialS =
        "\11\uffff\1\0\72\uffff}>";
    static final String[] DFA6_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\2\11\14\1\21\uffff\2\1\3\uffff\1\1"+
            "\7\uffff\1\1\32\uffff\1\1\15\uffff\1\1\14\uffff\2\1",
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
                        if ( (synpred8_wcps()) ) {s = 67;}

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
        "\104\uffff";
    static final String DFA7_eofS =
        "\1\1\103\uffff";
    static final String DFA7_minS =
        "\1\7\10\uffff\1\0\72\uffff";
    static final String DFA7_maxS =
        "\1\160\10\uffff\1\0\72\uffff";
    static final String DFA7_acceptS =
        "\1\uffff\1\2\101\uffff\1\1";
    static final String DFA7_specialS =
        "\11\uffff\1\0\72\uffff}>";
    static final String[] DFA7_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\2\1\1\11\13\1\21\uffff\2\1\3\uffff"+
            "\1\1\7\uffff\1\1\32\uffff\1\1\15\uffff\1\1\14\uffff\2\1",
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
                        if ( (synpred9_wcps()) ) {s = 67;}

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
        "\u011b\uffff";
    static final String DFA8_eofS =
        "\1\2\u011a\uffff";
    static final String DFA8_minS =
        "\1\7\1\0\14\uffff\5\0\u0108\uffff";
    static final String DFA8_maxS =
        "\1\160\1\0\14\uffff\5\0\u0108\uffff";
    static final String DFA8_acceptS =
        "\2\uffff\1\2\100\uffff\1\1\u00d7\uffff";
    static final String DFA8_specialS =
        "\1\uffff\1\0\14\uffff\1\1\1\2\1\3\1\4\1\5\u0108\uffff}>";
    static final String[] DFA8_transitionS = {
            "\2\2\1\uffff\1\2\2\uffff\3\2\1\1\1\16\1\17\1\20\1\21\1\22\5"+
            "\2\21\uffff\2\2\3\uffff\1\2\7\uffff\1\2\32\uffff\1\2\15\uffff"+
            "\1\2\14\uffff\2\2",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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
                        if ( (synpred15_wcps()) ) {s = 67;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA8_14 = input.LA(1);

                         
                        int index8_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_wcps()) ) {s = 67;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_14);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA8_15 = input.LA(1);

                         
                        int index8_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_wcps()) ) {s = 67;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_15);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA8_16 = input.LA(1);

                         
                        int index8_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_wcps()) ) {s = 67;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_16);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA8_17 = input.LA(1);

                         
                        int index8_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_wcps()) ) {s = 67;}

                        else if ( (true) ) {s = 2;}

                         
                        input.seek(index8_17);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA8_18 = input.LA(1);

                         
                        int index8_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred15_wcps()) ) {s = 67;}

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
        "\104\uffff";
    static final String DFA9_eofS =
        "\1\1\103\uffff";
    static final String DFA9_minS =
        "\1\7\10\uffff\1\0\72\uffff";
    static final String DFA9_maxS =
        "\1\160\10\uffff\1\0\72\uffff";
    static final String DFA9_acceptS =
        "\1\uffff\1\2\101\uffff\1\1";
    static final String DFA9_specialS =
        "\11\uffff\1\0\72\uffff}>";
    static final String[] DFA9_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\11\1\2\11\3\1\21\uffff\2\1\3\uffff"+
            "\1\1\7\uffff\1\1\32\uffff\1\1\15\uffff\1\1\14\uffff\2\1",
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
                        if ( (synpred17_wcps()) ) {s = 67;}

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
        "\104\uffff";
    static final String DFA10_eofS =
        "\1\1\103\uffff";
    static final String DFA10_minS =
        "\1\7\10\uffff\1\0\72\uffff";
    static final String DFA10_maxS =
        "\1\160\10\uffff\1\0\72\uffff";
    static final String DFA10_acceptS =
        "\1\uffff\1\2\101\uffff\1\1";
    static final String DFA10_specialS =
        "\11\uffff\1\0\72\uffff}>";
    static final String[] DFA10_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\13\1\2\11\1\1\21\uffff\2\1\3\uffff"+
            "\1\1\7\uffff\1\1\32\uffff\1\1\15\uffff\1\1\14\uffff\2\1",
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
                        if ( (synpred19_wcps()) ) {s = 67;}

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
        "\104\uffff";
    static final String DFA11_eofS =
        "\1\1\103\uffff";
    static final String DFA11_minS =
        "\1\7\10\uffff\1\0\72\uffff";
    static final String DFA11_maxS =
        "\1\160\10\uffff\1\0\72\uffff";
    static final String DFA11_acceptS =
        "\1\uffff\1\2\101\uffff\1\1";
    static final String DFA11_specialS =
        "\11\uffff\1\0\72\uffff}>";
    static final String[] DFA11_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\15\1\1\11\21\uffff\2\1\3\uffff\1\1"+
            "\7\uffff\1\1\32\uffff\1\1\15\uffff\1\1\14\uffff\2\1",
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
                        if ( (synpred20_wcps()) ) {s = 67;}

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
        "\55\uffff";
    static final String DFA12_eofS =
        "\55\uffff";
    static final String DFA12_minS =
        "\1\6\37\0\15\uffff";
    static final String DFA12_maxS =
        "\1\156\37\0\15\uffff";
    static final String DFA12_acceptS =
        "\40\uffff\1\1\2\uffff\1\2\5\uffff\1\3\1\4\1\6\1\5";
    static final String DFA12_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31"+
        "\1\32\1\33\1\34\1\35\1\36\15\uffff}>";
    static final String[] DFA12_transitionS = {
            "\1\13\17\uffff\1\43\1\15\1\23\2\uffff\1\1\1\2\1\3\1\4\1\6\1"+
            "\7\1\10\1\5\7\11\1\12\3\uffff\1\30\4\uffff\1\31\1\32\1\33\1"+
            "\34\1\35\1\37\1\uffff\1\36\1\52\1\17\1\16\6\43\1\22\1\21\11"+
            "\43\1\26\1\43\1\20\1\14\1\51\2\uffff\3\40\12\uffff\1\25\10\uffff"+
            "\1\27\1\24\1\53",
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
            "",
            "",
            "",
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
            return "94:1: coverageValue returns [CoverageExpr value] : (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom | e6= switchExpr );";
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
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}

                         
                        input.seek(index12_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA12_2 = input.LA(1);


                        int index12_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA12_3 = input.LA(1);


                        int index12_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA12_4 = input.LA(1);


                        int index12_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA12_5 = input.LA(1);


                        int index12_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA12_6 = input.LA(1);


                        int index12_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA12_7 = input.LA(1);


                        int index12_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA12_8 = input.LA(1);


                        int index12_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 :
                        int LA12_9 = input.LA(1);


                        int index12_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 :
                        int LA12_10 = input.LA(1);


                        int index12_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 :
                        int LA12_11 = input.LA(1);


                        int index12_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 :
                        int LA12_12 = input.LA(1);


                        int index12_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 :
                        int LA12_13 = input.LA(1);


                        int index12_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 :
                        int LA12_14 = input.LA(1);


                        int index12_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 :
                        int LA12_15 = input.LA(1);


                        int index12_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 :
                        int LA12_16 = input.LA(1);


                        int index12_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 :
                        int LA12_17 = input.LA(1);


                        int index12_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 :
                        int LA12_18 = input.LA(1);


                        int index12_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 :
                        int LA12_19 = input.LA(1);


                        int index12_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_19);
                        if ( s>=0 ) return s;
                        break;
                    case 19 :
                        int LA12_20 = input.LA(1);


                        int index12_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_20);
                        if ( s>=0 ) return s;
                        break;
                    case 20 :
                        int LA12_21 = input.LA(1);


                        int index12_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_21);
                        if ( s>=0 ) return s;
                        break;
                    case 21 :
                        int LA12_22 = input.LA(1);


                        int index12_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_22);
                        if ( s>=0 ) return s;
                        break;
                    case 22 :
                        int LA12_23 = input.LA(1);


                        int index12_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_23);
                        if ( s>=0 ) return s;
                        break;
                    case 23 :
                        int LA12_24 = input.LA(1);


                        int index12_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_24);
                        if ( s>=0 ) return s;
                        break;
                    case 24 :
                        int LA12_25 = input.LA(1);


                        int index12_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_25);
                        if ( s>=0 ) return s;
                        break;
                    case 25 :
                        int LA12_26 = input.LA(1);


                        int index12_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_26);
                        if ( s>=0 ) return s;
                        break;
                    case 26 :
                        int LA12_27 = input.LA(1);


                        int index12_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_27);
                        if ( s>=0 ) return s;
                        break;
                    case 27 :
                        int LA12_28 = input.LA(1);


                        int index12_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_28);
                        if ( s>=0 ) return s;
                        break;
                    case 28 :
                        int LA12_29 = input.LA(1);


                        int index12_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_29);
                        if ( s>=0 ) return s;
                        break;
                    case 29 :
                        int LA12_30 = input.LA(1);


                        int index12_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}


                        input.seek(index12_30);
                        if ( s>=0 ) return s;
                        break;
                    case 30 :
                        int LA12_31 = input.LA(1);

                         
                        int index12_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred21_wcps()) ) {s = 32;}

                        else if ( (synpred22_wcps()) ) {s = 35;}

                        else if ( (synpred25_wcps()) ) {s = 44;}

                         
                        input.seek(index12_31);
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
        "\116\uffff";
    static final String DFA13_eofS =
        "\116\uffff";
    static final String DFA13_minS =
        "\1\6\12\uffff\1\6\14\uffff\1\105\7\uffff\26\0\25\uffff\1\0\2\uffff";
    static final String DFA13_maxS =
        "\1\155\12\uffff\1\156\14\uffff\1\154\7\uffff\26\0\25\uffff\1\0\2"+
        "\uffff";
    static final String DFA13_acceptS =
        "\1\uffff\1\1\25\uffff\1\2\1\uffff\1\6\4\uffff\1\7\27\uffff\1\3\25"+
        "\uffff\1\4\1\5";
    static final String DFA13_specialS =
        "\40\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\25\uffff\1\26\2"+
        "\uffff}>";
    static final String[] DFA13_transitionS = {
            "\1\13\20\uffff\2\1\2\uffff\20\1\3\uffff\1\30\4\uffff\5\31\1"+
            "\36\1\uffff\1\36\1\uffff\2\1\6\uffff\2\1\11\uffff\1\1\1\uffff"+
            "\2\1\20\uffff\1\1\10\uffff\1\27\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\53\17\uffff\1\66\1\55\1\62\2\uffff\1\41\1\42\1\43\1\44\1"+
            "\46\1\47\1\50\1\45\7\51\1\52\3\uffff\1\66\4\uffff\6\66\1\uffff"+
            "\2\66\1\57\1\56\6\66\1\40\1\61\11\66\1\65\1\66\1\60\1\54\1\66"+
            "\2\uffff\3\66\12\uffff\1\64\10\uffff\1\66\1\63\1\66",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\113\14\uffff\1\113\31\uffff\1\113",
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
            return "102:1: coverageAtom returns [CoverageExpr value] : (e2= scalarExpr | e1= coverageVariable | LPAREN e7= coverageExpr RPAREN | e3= coverageConstantExpr | e4= coverageConstructorExpr | e5= setMetaDataExpr | e6= rangeConstructorExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
		int _s = s;
            switch ( s ) {
                    case 0 :
                        int LA13_32 = input.LA(1);

                         
                        int index13_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_32);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA13_33 = input.LA(1);

                         
                        int index13_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_33);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA13_34 = input.LA(1);

                         
                        int index13_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_34);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA13_35 = input.LA(1);

                         
                        int index13_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_35);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA13_36 = input.LA(1);

                         
                        int index13_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_36);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA13_37 = input.LA(1);

                         
                        int index13_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_37);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA13_38 = input.LA(1);

                         
                        int index13_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_38);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA13_39 = input.LA(1);

                         
                        int index13_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_39);
                        if ( s>=0 ) return s;
                        break;
                    case 8 :
                        int LA13_40 = input.LA(1);

                         
                        int index13_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_40);
                        if ( s>=0 ) return s;
                        break;
                    case 9 :
                        int LA13_41 = input.LA(1);

                         
                        int index13_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_41);
                        if ( s>=0 ) return s;
                        break;
                    case 10 :
                        int LA13_42 = input.LA(1);

                         
                        int index13_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_42);
                        if ( s>=0 ) return s;
                        break;
                    case 11 :
                        int LA13_43 = input.LA(1);

                         
                        int index13_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_43);
                        if ( s>=0 ) return s;
                        break;
                    case 12 :
                        int LA13_44 = input.LA(1);

                         
                        int index13_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_44);
                        if ( s>=0 ) return s;
                        break;
                    case 13 :
                        int LA13_45 = input.LA(1);

                         
                        int index13_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_45);
                        if ( s>=0 ) return s;
                        break;
                    case 14 :
                        int LA13_46 = input.LA(1);

                         
                        int index13_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_46);
                        if ( s>=0 ) return s;
                        break;
                    case 15 :
                        int LA13_47 = input.LA(1);

                         
                        int index13_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_47);
                        if ( s>=0 ) return s;
                        break;
                    case 16 :
                        int LA13_48 = input.LA(1);

                         
                        int index13_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_48);
                        if ( s>=0 ) return s;
                        break;
                    case 17 :
                        int LA13_49 = input.LA(1);

                         
                        int index13_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_49);
                        if ( s>=0 ) return s;
                        break;
                    case 18 :
                        int LA13_50 = input.LA(1);

                         
                        int index13_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_50);
                        if ( s>=0 ) return s;
                        break;
                    case 19 :
                        int LA13_51 = input.LA(1);

                         
                        int index13_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_51);
                        if ( s>=0 ) return s;
                        break;
                    case 20 :
                        int LA13_52 = input.LA(1);

                         
                        int index13_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_52);
                        if ( s>=0 ) return s;
                        break;
                    case 21 :
                        int LA13_53 = input.LA(1);

                         
                        int index13_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred26_wcps()) ) {s = 1;}

                        else if ( (synpred28_wcps()) ) {s = 54;}

                         
                        input.seek(index13_53);
                        if ( s>=0 ) return s;
                        break;
                    case 22 :
                        int LA13_75 = input.LA(1);

                         
                        int index13_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred29_wcps()) ) {s = 76;}

                        else if ( (synpred30_wcps()) ) {s = 77;}

                         
                        input.seek(index13_75);
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
        "\u00b3\uffff";
    static final String DFA14_eofS =
        "\14\uffff\1\60\u00a6\uffff";
    static final String DFA14_minS =
        "\2\6\7\uffff\1\6\1\15\1\6\1\7\4\6\4\0\2\uffff\17\0\7\uffff\3\0\4"+
        "\uffff\1\0\4\uffff\5\0\11\uffff\16\0\136\uffff";
    static final String DFA14_maxS =
        "\1\155\1\6\7\uffff\1\6\1\51\1\155\1\160\1\155\3\6\4\0\2\uffff\17"+
        "\0\7\uffff\3\0\4\uffff\1\0\4\uffff\5\0\11\uffff\16\0\136\uffff";
    static final String DFA14_acceptS =
        "\2\uffff\1\1\22\uffff\1\3\20\uffff\1\6\11\uffff\1\5\73\uffff\1\4"+
        "\105\uffff\1\2";
    static final String DFA14_specialS =
        "\21\uffff\1\0\1\1\1\2\1\3\2\uffff\1\4\1\5\1\6\1\7\1\10\1\11\1\12"+
        "\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\7\uffff\1\23\1\24\1\25"+
        "\4\uffff\1\26\4\uffff\1\27\1\30\1\31\1\32\1\33\11\uffff\1\34\1\35"+
        "\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\136"+
        "\uffff}>";
    static final String[] DFA14_transitionS = {
            "\1\13\20\uffff\1\15\1\23\2\uffff\1\1\7\2\7\11\1\12\21\uffff"+
            "\1\17\1\16\6\uffff\1\22\1\21\11\uffff\1\25\1\uffff\1\20\1\14"+
            "\20\uffff\1\25\11\uffff\1\24",
            "\1\27",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\30",
            "\1\31\1\uffff\1\31\6\uffff\1\31\1\uffff\1\31\17\uffff\2\31",
            "\1\33\20\uffff\1\34\1\41\2\uffff\1\45\7\46\7\42\1\43\21\uffff"+
            "\1\36\1\35\6\uffff\1\32\1\40\11\uffff\1\57\1\uffff\1\37\1\55"+
            "\20\uffff\1\56\11\uffff\1\44",
            "\2\60\1\uffff\1\60\2\uffff\3\60\1\64\1\71\1\72\1\73\1\74\1"+
            "\75\5\60\21\uffff\2\60\3\uffff\1\60\7\uffff\1\60\32\uffff\1"+
            "\60\15\uffff\1\60\14\uffff\2\60",
            "\1\107\20\uffff\1\110\1\116\12\uffff\7\117\1\120\21\uffff\1"+
            "\112\1\111\6\uffff\1\115\1\114\13\uffff\1\113\33\uffff\1\121",
            "\1\122",
            "\1\123",
            "\1\124",
            "\1\uffff",
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
            "",
            "",
            "",
            "",
            "\1\uffff",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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
            return "111:1: scalarExpr returns [ScalarExpr value] : (e1= metaDataExpr | e2= condenseExpr | e3= booleanScalarExpr | e4= numericScalarExpr | e5= stringScalarExpr | LPAREN e6= scalarExpr RPAREN );";
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
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_17);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA14_18 = input.LA(1);

                         
                        int index14_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_18);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA14_19 = input.LA(1);

                         
                        int index14_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_19);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA14_20 = input.LA(1);

                         
                        int index14_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_20);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA14_23 = input.LA(1);

                         
                        int index14_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred32_wcps()) ) {s = 2;}

                        else if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred36_wcps()) ) {s = 48;}

                         
                        input.seek(index14_23);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA14_24 = input.LA(1);

                         
                        int index14_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 178;}

                        else if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_24);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA14_25 = input.LA(1);

                         
                        int index14_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred33_wcps()) ) {s = 178;}

                        else if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}


                        input.seek(index14_25);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA14_26 = input.LA(1);


                        int index14_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                        else if ( (true) ) {s = 38;}


                        input.seek(index14_26);
                        if ( s>=0 ) return s;
                        break;
                    case 8 :
                        int LA14_27 = input.LA(1);


                        int index14_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                        else if ( (true) ) {s = 38;}


                        input.seek(index14_27);
                        if ( s>=0 ) return s;
                        break;
                    case 9 :
                        int LA14_28 = input.LA(1);


                        int index14_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                        else if ( (true) ) {s = 38;}


                        input.seek(index14_28);
                        if ( s>=0 ) return s;
                        break;
                    case 10 :
                        int LA14_29 = input.LA(1);


                        int index14_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_29);
                        if ( s>=0 ) return s;
                        break;
                    case 11 :
                        int LA14_30 = input.LA(1);

                         
                        int index14_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_30);
                        if ( s>=0 ) return s;
                        break;
                    case 12 :
                        int LA14_31 = input.LA(1);

                         
                        int index14_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_31);
                        if ( s>=0 ) return s;
                        break;
                    case 13 :
                        int LA14_32 = input.LA(1);

                         
                        int index14_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_32);
                        if ( s>=0 ) return s;
                        break;
                    case 14 :
                        int LA14_33 = input.LA(1);

                         
                        int index14_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_33);
                        if ( s>=0 ) return s;
                        break;
                    case 15 :
                        int LA14_34 = input.LA(1);

                         
                        int index14_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_34);
                        if ( s>=0 ) return s;
                        break;
                    case 16 :
                        int LA14_35 = input.LA(1);

                         
                        int index14_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_35);
                        if ( s>=0 ) return s;
                        break;
                    case 17 :
                        int LA14_36 = input.LA(1);

                         
                        int index14_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_36);
                        if ( s>=0 ) return s;
                        break;
                    case 18 :
                        int LA14_37 = input.LA(1);

                         
                        int index14_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_37);
                        if ( s>=0 ) return s;
                        break;
                    case 19 :
                        int LA14_45 = input.LA(1);

                         
                        int index14_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_45);
                        if ( s>=0 ) return s;
                        break;
                    case 20 :
                        int LA14_46 = input.LA(1);

                         
                        int index14_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_46);
                        if ( s>=0 ) return s;
                        break;
                    case 21 :
                        int LA14_47 = input.LA(1);

                         
                        int index14_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (true) ) {s = 38;}

                         
                        input.seek(index14_47);
                        if ( s>=0 ) return s;
                        break;
                    case 22 :
                        int LA14_52 = input.LA(1);

                         
                        int index14_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred36_wcps()) ) {s = 48;}

                         
                        input.seek(index14_52);
                        if ( s>=0 ) return s;
                        break;
                    case 23 :
                        int LA14_57 = input.LA(1);

                         
                        int index14_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred36_wcps()) ) {s = 48;}

                         
                        input.seek(index14_57);
                        if ( s>=0 ) return s;
                        break;
                    case 24 :
                        int LA14_58 = input.LA(1);

                         
                        int index14_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred36_wcps()) ) {s = 48;}

                         
                        input.seek(index14_58);
                        if ( s>=0 ) return s;
                        break;
                    case 25 :
                        int LA14_59 = input.LA(1);

                         
                        int index14_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred36_wcps()) ) {s = 48;}

                         
                        input.seek(index14_59);
                        if ( s>=0 ) return s;
                        break;
                    case 26 :
                        int LA14_60 = input.LA(1);

                         
                        int index14_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred36_wcps()) ) {s = 48;}

                         
                        input.seek(index14_60);
                        if ( s>=0 ) return s;
                        break;
                    case 27 :
                        int LA14_61 = input.LA(1);

                         
                        int index14_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred36_wcps()) ) {s = 48;}

                         
                        input.seek(index14_61);
                        if ( s>=0 ) return s;
                        break;
                    case 28 :
                        int LA14_71 = input.LA(1);

                         
                        int index14_71 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_71);
                        if ( s>=0 ) return s;
                        break;
                    case 29 :
                        int LA14_72 = input.LA(1);

                         
                        int index14_72 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_72);
                        if ( s>=0 ) return s;
                        break;
                    case 30 :
                        int LA14_73 = input.LA(1);

                         
                        int index14_73 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_73);
                        if ( s>=0 ) return s;
                        break;
                    case 31 :
                        int LA14_74 = input.LA(1);

                         
                        int index14_74 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_74);
                        if ( s>=0 ) return s;
                        break;
                    case 32 :
                        int LA14_75 = input.LA(1);

                         
                        int index14_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_75);
                        if ( s>=0 ) return s;
                        break;
                    case 33 :
                        int LA14_76 = input.LA(1);

                         
                        int index14_76 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_76);
                        if ( s>=0 ) return s;
                        break;
                    case 34 :
                        int LA14_77 = input.LA(1);

                         
                        int index14_77 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_77);
                        if ( s>=0 ) return s;
                        break;
                    case 35 :
                        int LA14_78 = input.LA(1);

                         
                        int index14_78 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_78);
                        if ( s>=0 ) return s;
                        break;
                    case 36 :
                        int LA14_79 = input.LA(1);

                         
                        int index14_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_79);
                        if ( s>=0 ) return s;
                        break;
                    case 37 :
                        int LA14_80 = input.LA(1);

                         
                        int index14_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_80);
                        if ( s>=0 ) return s;
                        break;
                    case 38 :
                        int LA14_81 = input.LA(1);

                         
                        int index14_81 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_81);
                        if ( s>=0 ) return s;
                        break;
                    case 39 :
                        int LA14_82 = input.LA(1);

                         
                        int index14_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_82);
                        if ( s>=0 ) return s;
                        break;
                    case 40 :
                        int LA14_83 = input.LA(1);

                         
                        int index14_83 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_83);
                        if ( s>=0 ) return s;
                        break;
                    case 41 :
                        int LA14_84 = input.LA(1);

                         
                        int index14_84 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_wcps()) ) {s = 21;}

                        else if ( (synpred35_wcps()) ) {s = 108;}

                         
                        input.seek(index14_84);
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
        "\63\uffff";
    static final String DFA22_eofS =
        "\63\uffff";
    static final String DFA22_minS =
        "\1\6\37\0\23\uffff";
    static final String DFA22_maxS =
        "\1\156\37\0\23\uffff";
    static final String DFA22_acceptS =
        "\40\uffff\1\1\2\uffff\1\2\5\uffff\1\3\1\4\1\14\1\5\1\6\1\7\1\10"+
        "\1\11\1\12\1\13";
    static final String DFA22_specialS =
        "\1\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31"+
        "\1\32\1\33\1\34\1\35\1\36\23\uffff}>";
    static final String[] DFA22_transitionS = {
            "\1\13\17\uffff\1\43\1\15\1\23\2\uffff\1\1\1\2\1\3\1\4\1\6\1"+
            "\7\1\10\1\5\7\11\1\12\3\uffff\1\30\4\uffff\1\31\1\32\1\33\1"+
            "\34\1\35\1\37\1\uffff\1\36\1\52\1\17\1\16\6\43\1\22\1\21\11"+
            "\43\1\26\1\43\1\20\1\14\1\51\2\uffff\3\40\12\uffff\1\25\10\uffff"+
            "\1\27\1\24\1\53",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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
            return "164:1: coverageAtomConstructor returns [CoverageExpr value] : (e5= subsetExpr | e2= unaryInducedExpr | e4= scaleExpr | e3= crsTransformExpr | e1= coverageAtom | e6= scalarExpr | e7= coverageVariable | e8= coverageConstantExpr | e9= coverageConstructorExpr | e10= setMetaDataExpr | e11= rangeConstructorExpr | e12= switchExpr );";
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
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}


                        input.seek(index22_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA22_2 = input.LA(1);

                         
                        int index22_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA22_3 = input.LA(1);

                         
                        int index22_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_3);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA22_4 = input.LA(1);

                         
                        int index22_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_4);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA22_5 = input.LA(1);

                         
                        int index22_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_5);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA22_6 = input.LA(1);

                         
                        int index22_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_6);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA22_7 = input.LA(1);

                         
                        int index22_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_7);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA22_8 = input.LA(1);

                         
                        int index22_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_8);
                        if ( s>=0 ) return s;
                        break;
                    case 8 :
                        int LA22_9 = input.LA(1);

                         
                        int index22_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_9);
                        if ( s>=0 ) return s;
                        break;
                    case 9 :
                        int LA22_10 = input.LA(1);

                         
                        int index22_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_10);
                        if ( s>=0 ) return s;
                        break;
                    case 10 :
                        int LA22_11 = input.LA(1);

                         
                        int index22_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_11);
                        if ( s>=0 ) return s;
                        break;
                    case 11 :
                        int LA22_12 = input.LA(1);

                         
                        int index22_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_12);
                        if ( s>=0 ) return s;
                        break;
                    case 12 :
                        int LA22_13 = input.LA(1);

                         
                        int index22_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_13);
                        if ( s>=0 ) return s;
                        break;
                    case 13 :
                        int LA22_14 = input.LA(1);

                         
                        int index22_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_14);
                        if ( s>=0 ) return s;
                        break;
                    case 14 :
                        int LA22_15 = input.LA(1);

                         
                        int index22_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_15);
                        if ( s>=0 ) return s;
                        break;
                    case 15 :
                        int LA22_16 = input.LA(1);

                         
                        int index22_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_16);
                        if ( s>=0 ) return s;
                        break;
                    case 16 :
                        int LA22_17 = input.LA(1);

                         
                        int index22_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_17);
                        if ( s>=0 ) return s;
                        break;
                    case 17 :
                        int LA22_18 = input.LA(1);

                         
                        int index22_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_18);
                        if ( s>=0 ) return s;
                        break;
                    case 18 :
                        int LA22_19 = input.LA(1);

                         
                        int index22_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_19);
                        if ( s>=0 ) return s;
                        break;
                    case 19 :
                        int LA22_20 = input.LA(1);

                         
                        int index22_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_20);
                        if ( s>=0 ) return s;
                        break;
                    case 20 :
                        int LA22_21 = input.LA(1);

                         
                        int index22_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_21);
                        if ( s>=0 ) return s;
                        break;
                    case 21 :
                        int LA22_22 = input.LA(1);

                         
                        int index22_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred61_wcps()) ) {s = 45;}

                         
                        input.seek(index22_22);
                        if ( s>=0 ) return s;
                        break;
                    case 22 :
                        int LA22_23 = input.LA(1);

                         
                        int index22_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred62_wcps()) ) {s = 46;}

                         
                        input.seek(index22_23);
                        if ( s>=0 ) return s;
                        break;
                    case 23 :
                        int LA22_24 = input.LA(1);

                         
                        int index22_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred63_wcps()) ) {s = 47;}

                        else if ( (synpred64_wcps()) ) {s = 48;}

                         
                        input.seek(index22_24);
                        if ( s>=0 ) return s;
                        break;
                    case 24 :
                        int LA22_25 = input.LA(1);

                         
                        int index22_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred65_wcps()) ) {s = 49;}

                         
                        input.seek(index22_25);
                        if ( s>=0 ) return s;
                        break;
                    case 25 :
                        int LA22_26 = input.LA(1);

                         
                        int index22_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred65_wcps()) ) {s = 49;}

                         
                        input.seek(index22_26);
                        if ( s>=0 ) return s;
                        break;
                    case 26 :
                        int LA22_27 = input.LA(1);

                         
                        int index22_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred65_wcps()) ) {s = 49;}

                         
                        input.seek(index22_27);
                        if ( s>=0 ) return s;
                        break;
                    case 27 :
                        int LA22_28 = input.LA(1);

                         
                        int index22_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred65_wcps()) ) {s = 49;}

                         
                        input.seek(index22_28);
                        if ( s>=0 ) return s;
                        break;
                    case 28 :
                        int LA22_29 = input.LA(1);

                         
                        int index22_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred65_wcps()) ) {s = 49;}

                         
                        input.seek(index22_29);
                        if ( s>=0 ) return s;
                        break;
                    case 29 :
                        int LA22_30 = input.LA(1);

                         
                        int index22_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred66_wcps()) ) {s = 50;}

                         
                        input.seek(index22_30);
                        if ( s>=0 ) return s;
                        break;
                    case 30 :
                        int LA22_31 = input.LA(1);

                         
                        int index22_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred56_wcps()) ) {s = 32;}

                        else if ( (synpred57_wcps()) ) {s = 35;}

                        else if ( (synpred60_wcps()) ) {s = 44;}

                        else if ( (synpred66_wcps()) ) {s = 50;}

                         
                        input.seek(index22_31);
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
        "\u00a2\uffff";
    static final String DFA36_eofS =
        "\u00a2\uffff";
    static final String DFA36_minS =
        "\1\6\12\uffff\1\6\1\uffff\3\6\6\uffff\1\6\7\uffff\1\70\1\105\63"+
        "\uffff\13\0\24\uffff\20\0\35\uffff\2\0\1\uffff";
    static final String DFA36_maxS =
        "\1\155\12\uffff\1\156\1\uffff\1\155\2\6\6\uffff\1\156\7\uffff\1"+
        "\70\1\154\63\uffff\13\0\24\uffff\20\0\35\uffff\2\0\1\uffff";
    static final String DFA36_acceptS =
        "\1\uffff\1\1\36\uffff\1\2\1\uffff\1\3\1\uffff\1\4\1\5\26\uffff\1"+
        "\6\144\uffff\1\7";
    static final String DFA36_specialS =
        "\123\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\24\uffff"+
        "\1\13\1\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27"+
        "\1\30\1\31\1\32\35\uffff\1\33\1\34\1\uffff}>";
    static final String[] DFA36_transitionS = {
            "\1\13\17\uffff\1\40\1\15\1\1\2\uffff\20\1\3\uffff\1\1\4\uffff"+
            "\5\1\1\37\1\uffff\1\36\1\uffff\1\17\1\16\2\40\4\42\2\1\11\44"+
            "\1\26\1\45\2\1\20\uffff\1\1\10\uffff\2\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\1\17\uffff\3\1\2\uffff\20\1\3\uffff\1\1\4\uffff\6\1\1\uffff"+
            "\32\1\2\uffff\3\1\11\74\1\uffff\1\1\10\uffff\3\1",
            "",
            "\1\123\20\uffff\1\124\1\132\2\uffff\10\40\7\133\1\134\3\uffff"+
            "\1\40\4\uffff\6\40\1\uffff\1\40\1\uffff\1\126\1\125\6\uffff"+
            "\1\131\1\130\11\uffff\1\40\1\uffff\1\127\1\40\20\uffff\1\40"+
            "\10\uffff\1\40\1\135",
            "\1\162",
            "\1\163",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\164\17\uffff\1\45\1\167\1\175\2\uffff\1\165\7\45\7\176\1"+
            "\177\3\uffff\1\45\4\uffff\6\45\1\uffff\2\45\1\171\1\170\6\45"+
            "\1\174\1\173\13\45\1\172\1\166\1\45\2\uffff\3\45\12\uffff\1"+
            "\u0081\10\uffff\1\45\1\u0080\1\45",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\u009f",
            "\1\u00a0\14\uffff\1\u00a0\31\uffff\1\u00a0",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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
            return "231:1: unaryInducedExpr returns [CoverageExpr value] : (e6= fieldExpr | e1= unaryArithmeticExpr | e2= exponentialExpr | e3= trigonometricExpr | e4= booleanExpr | e5= castExpr | e7= rangeConstructorExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA36_83 = input.LA(1);

                         
                        int index36_83 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_83);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA36_84 = input.LA(1);

                         
                        int index36_84 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_84);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA36_85 = input.LA(1);

                         
                        int index36_85 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_85);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA36_86 = input.LA(1);

                         
                        int index36_86 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_86);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA36_87 = input.LA(1);

                         
                        int index36_87 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_87);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA36_88 = input.LA(1);

                         
                        int index36_88 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_88);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA36_89 = input.LA(1);

                         
                        int index36_89 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_89);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA36_90 = input.LA(1);

                         
                        int index36_90 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_90);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA36_91 = input.LA(1);

                         
                        int index36_91 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_91);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA36_92 = input.LA(1);

                         
                        int index36_92 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_92);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA36_93 = input.LA(1);

                         
                        int index36_93 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_93);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA36_114 = input.LA(1);

                         
                        int index36_114 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_114);
                        if ( s>=0 ) return s;
                        break;
                    case 12 :
                        int LA36_115 = input.LA(1);

                         
                        int index36_115 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred84_wcps()) ) {s = 32;}

                         
                        input.seek(index36_115);
                        if ( s>=0 ) return s;
                        break;
                    case 13 :
                        int LA36_116 = input.LA(1);

                         
                        int index36_116 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}

                         
                        input.seek(index36_116);
                        if ( s>=0 ) return s;
                        break;
                    case 14 :
                        int LA36_117 = input.LA(1);

                         
                        int index36_117 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}

                         
                        input.seek(index36_117);
                        if ( s>=0 ) return s;
                        break;
                    case 15 :
                        int LA36_118 = input.LA(1);

                         
                        int index36_118 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}

                         
                        input.seek(index36_118);
                        if ( s>=0 ) return s;
                        break;
                    case 16 :
                        int LA36_119 = input.LA(1);

                         
                        int index36_119 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}

                         
                        input.seek(index36_119);
                        if ( s>=0 ) return s;
                        break;
                    case 17 :
                        int LA36_120 = input.LA(1);

                         
                        int index36_120 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}

                         
                        input.seek(index36_120);
                        if ( s>=0 ) return s;
                        break;
                    case 18 :
                        int LA36_121 = input.LA(1);

                         
                        int index36_121 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}

                         
                        input.seek(index36_121);
                        if ( s>=0 ) return s;
                        break;
                    case 19 :
                        int LA36_122 = input.LA(1);

                         
                        int index36_122 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}

                         
                        input.seek(index36_122);
                        if ( s>=0 ) return s;
                        break;
                    case 20 :
                        int LA36_123 = input.LA(1);

                         
                        int index36_123 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}

                         
                        input.seek(index36_123);
                        if ( s>=0 ) return s;
                        break;
                    case 21 :
                        int LA36_124 = input.LA(1);

                         
                        int index36_124 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}

                         
                        input.seek(index36_124);
                        if ( s>=0 ) return s;
                        break;
                    case 22 :
                        int LA36_125 = input.LA(1);

                         
                        int index36_125 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}

                         
                        input.seek(index36_125);
                        if ( s>=0 ) return s;
                        break;
                    case 23 :
                        int LA36_126 = input.LA(1);


                        int index36_126 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}


                        input.seek(index36_126);
                        if ( s>=0 ) return s;
                        break;
                    case 24 :
                        int LA36_127 = input.LA(1);


                        int index36_127 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}


                        input.seek(index36_127);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA36_128 = input.LA(1);

                         
                        int index36_128 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}

                         
                        input.seek(index36_128);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA36_129 = input.LA(1);


                        int index36_129 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (synpred87_wcps()) ) {s = 37;}


                        input.seek(index36_129);
                        if ( s>=0 ) return s;
                        break;
                    case 27 :
                        int LA36_159 = input.LA(1);


                        int index36_159 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (true) ) {s = 161;}

                         
                        input.seek(index36_159);
                        if ( s>=0 ) return s;
                        break;
                    case 28 :
                        int LA36_160 = input.LA(1);


                        int index36_160 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred83_wcps()) ) {s = 1;}

                        else if ( (true) ) {s = 161;}

                         
                        input.seek(index36_160);
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
    static final String DFA46_eotS =
        "\u00ac\uffff";
    static final String DFA46_eofS =
        "\u00ac\uffff";
    static final String DFA46_minS =
        "\12\6\1\15\1\6\1\20\4\6\4\20\1\15\1\6\1\124\1\105\5\6\1\70\1\105"+
        "\3\uffff\u0088\0\1\uffff";
    static final String DFA46_maxS =
        "\1\155\11\6\1\51\1\156\1\124\1\155\3\6\5\124\1\155\1\124\1\154\5"+
        "\6\1\70\1\154\3\uffff\u0088\0\1\uffff";
    static final String DFA46_acceptS =
        "\40\uffff\1\1\1\2\1\3\u0088\uffff\1\4";
    static final String DFA46_specialS =
        "\43\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30"+
        "\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40\1\41\1\42\1\43\1\44\1\45"+
        "\1\46\1\47\1\50\1\51\1\52\1\53\1\54\1\55\1\56\1\57\1\60\1\61\1\62"+
        "\1\63\1\64\1\65\1\66\1\67\1\70\1\71\1\72\1\73\1\74\1\75\1\76\1\77"+
        "\1\100\1\101\1\102\1\103\1\104\1\105\1\106\1\107\1\110\1\111\1\112"+
        "\1\113\1\114\1\115\1\116\1\117\1\120\1\121\1\122\1\123\1\124\1\125"+
        "\1\126\1\127\1\130\1\131\1\132\1\133\1\134\1\135\1\136\1\137\1\140"+
        "\1\141\1\142\1\143\1\144\1\145\1\146\1\147\1\150\1\151\1\152\1\153"+
        "\1\154\1\155\1\156\1\157\1\160\1\161\1\162\1\163\1\164\1\165\1\166"+
        "\1\167\1\170\1\171\1\172\1\173\1\174\1\175\1\176\1\177\1\u0080\1"+
        "\u0081\1\u0082\1\u0083\1\u0084\1\u0085\1\u0086\1\u0087\1\uffff}>";
    static final String[] DFA46_transitionS = {
            "\1\13\20\uffff\1\15\1\23\2\uffff\1\1\1\2\1\3\1\4\1\6\1\7\1\10"+
            "\1\5\7\11\1\12\3\uffff\1\30\4\uffff\1\31\1\32\1\33\1\34\1\35"+
            "\1\37\1\uffff\1\36\1\uffff\1\17\1\16\6\uffff\1\22\1\21\11\uffff"+
            "\1\26\1\uffff\1\20\1\14\3\uffff\1\40\1\41\1\42\12\uffff\1\25"+
            "\10\uffff\1\27\1\24",
            "\1\43",
            "\1\44",
            "\1\45",
            "\1\46",
            "\1\47",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\53",
            "\1\54\1\uffff\1\54\6\uffff\1\54\1\uffff\1\54\17\uffff\2\54",
            "\1\56\17\uffff\1\117\1\61\1\66\2\uffff\1\57\1\74\1\75\1\76"+
            "\1\100\1\101\1\102\1\77\7\67\1\70\3\uffff\1\104\4\uffff\1\105"+
            "\1\106\1\107\1\110\1\111\1\113\1\uffff\1\112\1\126\1\63\1\62"+
            "\2\120\3\121\1\122\1\55\1\65\11\123\1\73\1\124\1\64\1\60\1\125"+
            "\2\uffff\1\114\1\115\1\116\12\uffff\1\72\10\uffff\1\103\1\71"+
            "\1\127",
            "\1\130\1\131\1\132\1\133\1\134\1\135\76\uffff\1\136",
            "\1\137\20\uffff\1\140\1\146\12\uffff\7\147\1\150\21\uffff\1"+
            "\142\1\141\6\uffff\1\145\1\144\13\uffff\1\143\33\uffff\1\151",
            "\1\152",
            "\1\153",
            "\1\154",
            "\1\160\1\161\1\162\1\163\1\164\1\165\2\156\2\155\72\uffff\1"+
            "\157",
            "\1\170\1\171\1\172\1\173\1\174\1\175\2\167\2\166\72\uffff\1"+
            "\176",
            "\1\u0081\1\u0082\1\u0083\1\u0084\1\u0085\1\u0086\2\u0080\2"+
            "\177\72\uffff\1\u0087",
            "\1\u008a\1\u008b\1\u008c\1\u008d\1\u008e\1\u008f\2\u0089\2"+
            "\u0088\72\uffff\1\u0090",
            "\2\u0092\1\u0091\104\uffff\1\u0093",
            "\1\u0094\20\uffff\1\u0097\1\u009d\2\uffff\1\u0095\7\uffff\7"+
            "\u009e\1\u009f\21\uffff\1\u0099\1\u0098\6\uffff\1\u009c\1\u009b"+
            "\13\uffff\1\u009a\1\u0096\20\uffff\1\u00a1\11\uffff\1\u00a0",
            "\1\u00a2",
            "\1\u00a3\14\uffff\1\u00a3\31\uffff\1\u00a3",
            "\1\u00a4",
            "\1\u00a5",
            "\1\u00a6",
            "\1\u00a7",
            "\1\u00a8",
            "\1\u00a9",
            "\1\u00aa\14\uffff\1\u00aa\31\uffff\1\u00aa",
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

    static final short[] DFA46_eot = DFA.unpackEncodedString(DFA46_eotS);
    static final short[] DFA46_eof = DFA.unpackEncodedString(DFA46_eofS);
    static final char[] DFA46_min = DFA.unpackEncodedStringToUnsignedChars(DFA46_minS);
    static final char[] DFA46_max = DFA.unpackEncodedStringToUnsignedChars(DFA46_maxS);
    static final short[] DFA46_accept = DFA.unpackEncodedString(DFA46_acceptS);
    static final short[] DFA46_special = DFA.unpackEncodedString(DFA46_specialS);
    static final short[][] DFA46_transition;

    static {
        int numStates = DFA46_transitionS.length;
        DFA46_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA46_transition[i] = DFA.unpackEncodedString(DFA46_transitionS[i]);
        }
    }

    class DFA46 extends DFA {

        public DFA46(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 46;
            this.eot = DFA46_eot;
            this.eof = DFA46_eof;
            this.min = DFA46_min;
            this.max = DFA46_max;
            this.accept = DFA46_accept;
            this.special = DFA46_special;
            this.transition = DFA46_transition;
        }
        public String getDescription() {
            return "283:1: subsetExpr returns [SubsetExpr value] : (e1= trimExpr | e2= sliceExpr | e3= extendExpr | e4= trimSliceExpr );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA46_35 = input.LA(1);

                         
                        int index46_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_35);
                        if ( s>=0 ) return s;
                        break;
                    case 1 :
                        int LA46_36 = input.LA(1);

                         
                        int index46_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_36);
                        if ( s>=0 ) return s;
                        break;
                    case 2 :
                        int LA46_37 = input.LA(1);

                         
                        int index46_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_37);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA46_38 = input.LA(1);

                         
                        int index46_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_38);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA46_39 = input.LA(1);

                         
                        int index46_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_39);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA46_40 = input.LA(1);

                         
                        int index46_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_40);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA46_41 = input.LA(1);

                         
                        int index46_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_41);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA46_42 = input.LA(1);

                         
                        int index46_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_42);
                        if ( s>=0 ) return s;
                        break;
                    case 8 :
                        int LA46_43 = input.LA(1);

                         
                        int index46_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_43);
                        if ( s>=0 ) return s;
                        break;
                    case 9 :
                        int LA46_44 = input.LA(1);

                         
                        int index46_44 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_44);
                        if ( s>=0 ) return s;
                        break;
                    case 10 :
                        int LA46_45 = input.LA(1);

                         
                        int index46_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_45);
                        if ( s>=0 ) return s;
                        break;
                    case 11 :
                        int LA46_46 = input.LA(1);

                         
                        int index46_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_46);
                        if ( s>=0 ) return s;
                        break;
                    case 12 :
                        int LA46_47 = input.LA(1);

                         
                        int index46_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_47);
                        if ( s>=0 ) return s;
                        break;
                    case 13 :
                        int LA46_48 = input.LA(1);

                         
                        int index46_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_48);
                        if ( s>=0 ) return s;
                        break;
                    case 14 :
                        int LA46_49 = input.LA(1);

                         
                        int index46_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_49);
                        if ( s>=0 ) return s;
                        break;
                    case 15 :
                        int LA46_50 = input.LA(1);

                         
                        int index46_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_50);
                        if ( s>=0 ) return s;
                        break;
                    case 16 :
                        int LA46_51 = input.LA(1);

                         
                        int index46_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_51);
                        if ( s>=0 ) return s;
                        break;
                    case 17 :
                        int LA46_52 = input.LA(1);

                         
                        int index46_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_52);
                        if ( s>=0 ) return s;
                        break;
                    case 18 :
                        int LA46_53 = input.LA(1);

                         
                        int index46_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_53);
                        if ( s>=0 ) return s;
                        break;
                    case 19 :
                        int LA46_54 = input.LA(1);

                         
                        int index46_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_54);
                        if ( s>=0 ) return s;
                        break;
                    case 20 :
                        int LA46_55 = input.LA(1);

                         
                        int index46_55 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_55);
                        if ( s>=0 ) return s;
                        break;
                    case 21 :
                        int LA46_56 = input.LA(1);

                         
                        int index46_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_56);
                        if ( s>=0 ) return s;
                        break;
                    case 22 :
                        int LA46_57 = input.LA(1);

                         
                        int index46_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_57);
                        if ( s>=0 ) return s;
                        break;
                    case 23 :
                        int LA46_58 = input.LA(1);

                         
                        int index46_58 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_58);
                        if ( s>=0 ) return s;
                        break;
                    case 24 :
                        int LA46_59 = input.LA(1);

                         
                        int index46_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_59);
                        if ( s>=0 ) return s;
                        break;
                    case 25 :
                        int LA46_60 = input.LA(1);

                         
                        int index46_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_60);
                        if ( s>=0 ) return s;
                        break;
                    case 26 :
                        int LA46_61 = input.LA(1);

                         
                        int index46_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_61);
                        if ( s>=0 ) return s;
                        break;
                    case 27 :
                        int LA46_62 = input.LA(1);

                         
                        int index46_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_62);
                        if ( s>=0 ) return s;
                        break;
                    case 28 :
                        int LA46_63 = input.LA(1);

                         
                        int index46_63 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_63);
                        if ( s>=0 ) return s;
                        break;
                    case 29 :
                        int LA46_64 = input.LA(1);

                         
                        int index46_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_64);
                        if ( s>=0 ) return s;
                        break;
                    case 30 :
                        int LA46_65 = input.LA(1);

                         
                        int index46_65 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_65);
                        if ( s>=0 ) return s;
                        break;
                    case 31 :
                        int LA46_66 = input.LA(1);

                         
                        int index46_66 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_66);
                        if ( s>=0 ) return s;
                        break;
                    case 32 :
                        int LA46_67 = input.LA(1);

                         
                        int index46_67 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_67);
                        if ( s>=0 ) return s;
                        break;
                    case 33 :
                        int LA46_68 = input.LA(1);

                         
                        int index46_68 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_68);
                        if ( s>=0 ) return s;
                        break;
                    case 34 :
                        int LA46_69 = input.LA(1);

                         
                        int index46_69 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_69);
                        if ( s>=0 ) return s;
                        break;
                    case 35 :
                        int LA46_70 = input.LA(1);

                         
                        int index46_70 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_70);
                        if ( s>=0 ) return s;
                        break;
                    case 36 :
                        int LA46_71 = input.LA(1);

                         
                        int index46_71 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_71);
                        if ( s>=0 ) return s;
                        break;
                    case 37 :
                        int LA46_72 = input.LA(1);

                         
                        int index46_72 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_72);
                        if ( s>=0 ) return s;
                        break;
                    case 38 :
                        int LA46_73 = input.LA(1);

                         
                        int index46_73 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_73);
                        if ( s>=0 ) return s;
                        break;
                    case 39 :
                        int LA46_74 = input.LA(1);

                         
                        int index46_74 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_74);
                        if ( s>=0 ) return s;
                        break;
                    case 40 :
                        int LA46_75 = input.LA(1);

                         
                        int index46_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_75);
                        if ( s>=0 ) return s;
                        break;
                    case 41 :
                        int LA46_76 = input.LA(1);

                         
                        int index46_76 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_76);
                        if ( s>=0 ) return s;
                        break;
                    case 42 :
                        int LA46_77 = input.LA(1);

                         
                        int index46_77 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_77);
                        if ( s>=0 ) return s;
                        break;
                    case 43 :
                        int LA46_78 = input.LA(1);

                         
                        int index46_78 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_78);
                        if ( s>=0 ) return s;
                        break;
                    case 44 :
                        int LA46_79 = input.LA(1);

                         
                        int index46_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_79);
                        if ( s>=0 ) return s;
                        break;
                    case 45 :
                        int LA46_80 = input.LA(1);

                         
                        int index46_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_80);
                        if ( s>=0 ) return s;
                        break;
                    case 46 :
                        int LA46_81 = input.LA(1);

                         
                        int index46_81 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_81);
                        if ( s>=0 ) return s;
                        break;
                    case 47 :
                        int LA46_82 = input.LA(1);

                         
                        int index46_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_82);
                        if ( s>=0 ) return s;
                        break;
                    case 48 :
                        int LA46_83 = input.LA(1);

                         
                        int index46_83 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_83);
                        if ( s>=0 ) return s;
                        break;
                    case 49 :
                        int LA46_84 = input.LA(1);

                         
                        int index46_84 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_84);
                        if ( s>=0 ) return s;
                        break;
                    case 50 :
                        int LA46_85 = input.LA(1);

                         
                        int index46_85 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_85);
                        if ( s>=0 ) return s;
                        break;
                    case 51 :
                        int LA46_86 = input.LA(1);

                         
                        int index46_86 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_86);
                        if ( s>=0 ) return s;
                        break;
                    case 52 :
                        int LA46_87 = input.LA(1);

                         
                        int index46_87 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_87);
                        if ( s>=0 ) return s;
                        break;
                    case 53 :
                        int LA46_88 = input.LA(1);

                         
                        int index46_88 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_88);
                        if ( s>=0 ) return s;
                        break;
                    case 54 :
                        int LA46_89 = input.LA(1);

                         
                        int index46_89 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_89);
                        if ( s>=0 ) return s;
                        break;
                    case 55 :
                        int LA46_90 = input.LA(1);

                         
                        int index46_90 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_90);
                        if ( s>=0 ) return s;
                        break;
                    case 56 :
                        int LA46_91 = input.LA(1);

                         
                        int index46_91 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_91);
                        if ( s>=0 ) return s;
                        break;
                    case 57 :
                        int LA46_92 = input.LA(1);

                         
                        int index46_92 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_92);
                        if ( s>=0 ) return s;
                        break;
                    case 58 :
                        int LA46_93 = input.LA(1);

                         
                        int index46_93 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_93);
                        if ( s>=0 ) return s;
                        break;
                    case 59 :
                        int LA46_94 = input.LA(1);

                         
                        int index46_94 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_94);
                        if ( s>=0 ) return s;
                        break;
                    case 60 :
                        int LA46_95 = input.LA(1);

                         
                        int index46_95 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_95);
                        if ( s>=0 ) return s;
                        break;
                    case 61 :
                        int LA46_96 = input.LA(1);

                         
                        int index46_96 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_96);
                        if ( s>=0 ) return s;
                        break;
                    case 62 :
                        int LA46_97 = input.LA(1);

                         
                        int index46_97 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_97);
                        if ( s>=0 ) return s;
                        break;
                    case 63 :
                        int LA46_98 = input.LA(1);

                         
                        int index46_98 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_98);
                        if ( s>=0 ) return s;
                        break;
                    case 64 :
                        int LA46_99 = input.LA(1);

                         
                        int index46_99 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_99);
                        if ( s>=0 ) return s;
                        break;
                    case 65 :
                        int LA46_100 = input.LA(1);

                         
                        int index46_100 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_100);
                        if ( s>=0 ) return s;
                        break;
                    case 66 :
                        int LA46_101 = input.LA(1);

                         
                        int index46_101 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_101);
                        if ( s>=0 ) return s;
                        break;
                    case 67 :
                        int LA46_102 = input.LA(1);

                         
                        int index46_102 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_102);
                        if ( s>=0 ) return s;
                        break;
                    case 68 :
                        int LA46_103 = input.LA(1);

                         
                        int index46_103 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_103);
                        if ( s>=0 ) return s;
                        break;
                    case 69 :
                        int LA46_104 = input.LA(1);

                         
                        int index46_104 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_104);
                        if ( s>=0 ) return s;
                        break;
                    case 70 :
                        int LA46_105 = input.LA(1);

                         
                        int index46_105 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_105);
                        if ( s>=0 ) return s;
                        break;
                    case 71 :
                        int LA46_106 = input.LA(1);

                         
                        int index46_106 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_106);
                        if ( s>=0 ) return s;
                        break;
                    case 72 :
                        int LA46_107 = input.LA(1);

                         
                        int index46_107 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_107);
                        if ( s>=0 ) return s;
                        break;
                    case 73 :
                        int LA46_108 = input.LA(1);

                         
                        int index46_108 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_108);
                        if ( s>=0 ) return s;
                        break;
                    case 74 :
                        int LA46_109 = input.LA(1);

                         
                        int index46_109 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_109);
                        if ( s>=0 ) return s;
                        break;
                    case 75 :
                        int LA46_110 = input.LA(1);

                         
                        int index46_110 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_110);
                        if ( s>=0 ) return s;
                        break;
                    case 76 :
                        int LA46_111 = input.LA(1);

                         
                        int index46_111 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_111);
                        if ( s>=0 ) return s;
                        break;
                    case 77 :
                        int LA46_112 = input.LA(1);

                         
                        int index46_112 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_112);
                        if ( s>=0 ) return s;
                        break;
                    case 78 :
                        int LA46_113 = input.LA(1);

                         
                        int index46_113 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_113);
                        if ( s>=0 ) return s;
                        break;
                    case 79 :
                        int LA46_114 = input.LA(1);

                         
                        int index46_114 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_114);
                        if ( s>=0 ) return s;
                        break;
                    case 80 :
                        int LA46_115 = input.LA(1);

                         
                        int index46_115 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_115);
                        if ( s>=0 ) return s;
                        break;
                    case 81 :
                        int LA46_116 = input.LA(1);

                         
                        int index46_116 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_116);
                        if ( s>=0 ) return s;
                        break;
                    case 82 :
                        int LA46_117 = input.LA(1);

                         
                        int index46_117 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_117);
                        if ( s>=0 ) return s;
                        break;
                    case 83 :
                        int LA46_118 = input.LA(1);

                         
                        int index46_118 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_118);
                        if ( s>=0 ) return s;
                        break;
                    case 84 :
                        int LA46_119 = input.LA(1);

                         
                        int index46_119 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_119);
                        if ( s>=0 ) return s;
                        break;
                    case 85 :
                        int LA46_120 = input.LA(1);

                         
                        int index46_120 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_120);
                        if ( s>=0 ) return s;
                        break;
                    case 86 :
                        int LA46_121 = input.LA(1);

                         
                        int index46_121 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_121);
                        if ( s>=0 ) return s;
                        break;
                    case 87 :
                        int LA46_122 = input.LA(1);

                         
                        int index46_122 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_122);
                        if ( s>=0 ) return s;
                        break;
                    case 88 :
                        int LA46_123 = input.LA(1);

                         
                        int index46_123 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_123);
                        if ( s>=0 ) return s;
                        break;
                    case 89 :
                        int LA46_124 = input.LA(1);

                         
                        int index46_124 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_124);
                        if ( s>=0 ) return s;
                        break;
                    case 90 :
                        int LA46_125 = input.LA(1);

                         
                        int index46_125 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_125);
                        if ( s>=0 ) return s;
                        break;
                    case 91 :
                        int LA46_126 = input.LA(1);

                         
                        int index46_126 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_126);
                        if ( s>=0 ) return s;
                        break;
                    case 92 :
                        int LA46_127 = input.LA(1);

                         
                        int index46_127 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_127);
                        if ( s>=0 ) return s;
                        break;
                    case 93 :
                        int LA46_128 = input.LA(1);

                         
                        int index46_128 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_128);
                        if ( s>=0 ) return s;
                        break;
                    case 94 :
                        int LA46_129 = input.LA(1);

                         
                        int index46_129 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_129);
                        if ( s>=0 ) return s;
                        break;
                    case 95 :
                        int LA46_130 = input.LA(1);

                         
                        int index46_130 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_130);
                        if ( s>=0 ) return s;
                        break;
                    case 96 :
                        int LA46_131 = input.LA(1);

                         
                        int index46_131 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_131);
                        if ( s>=0 ) return s;
                        break;
                    case 97 :
                        int LA46_132 = input.LA(1);

                         
                        int index46_132 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_132);
                        if ( s>=0 ) return s;
                        break;
                    case 98 :
                        int LA46_133 = input.LA(1);

                         
                        int index46_133 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_133);
                        if ( s>=0 ) return s;
                        break;
                    case 99 :
                        int LA46_134 = input.LA(1);

                         
                        int index46_134 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_134);
                        if ( s>=0 ) return s;
                        break;
                    case 100 :
                        int LA46_135 = input.LA(1);

                         
                        int index46_135 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_135);
                        if ( s>=0 ) return s;
                        break;
                    case 101 :
                        int LA46_136 = input.LA(1);

                         
                        int index46_136 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_136);
                        if ( s>=0 ) return s;
                        break;
                    case 102 :
                        int LA46_137 = input.LA(1);

                         
                        int index46_137 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_137);
                        if ( s>=0 ) return s;
                        break;
                    case 103 :
                        int LA46_138 = input.LA(1);

                         
                        int index46_138 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_138);
                        if ( s>=0 ) return s;
                        break;
                    case 104 :
                        int LA46_139 = input.LA(1);

                         
                        int index46_139 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_139);
                        if ( s>=0 ) return s;
                        break;
                    case 105 :
                        int LA46_140 = input.LA(1);

                         
                        int index46_140 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_140);
                        if ( s>=0 ) return s;
                        break;
                    case 106 :
                        int LA46_141 = input.LA(1);

                         
                        int index46_141 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_141);
                        if ( s>=0 ) return s;
                        break;
                    case 107 :
                        int LA46_142 = input.LA(1);

                         
                        int index46_142 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_142);
                        if ( s>=0 ) return s;
                        break;
                    case 108 :
                        int LA46_143 = input.LA(1);

                         
                        int index46_143 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_143);
                        if ( s>=0 ) return s;
                        break;
                    case 109 :
                        int LA46_144 = input.LA(1);

                         
                        int index46_144 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_144);
                        if ( s>=0 ) return s;
                        break;
                    case 110 :
                        int LA46_145 = input.LA(1);

                         
                        int index46_145 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_145);
                        if ( s>=0 ) return s;
                        break;
                    case 111 :
                        int LA46_146 = input.LA(1);

                         
                        int index46_146 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_146);
                        if ( s>=0 ) return s;
                        break;
                    case 112 :
                        int LA46_147 = input.LA(1);

                         
                        int index46_147 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_147);
                        if ( s>=0 ) return s;
                        break;
                    case 113 :
                        int LA46_148 = input.LA(1);

                         
                        int index46_148 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_148);
                        if ( s>=0 ) return s;
                        break;
                    case 114 :
                        int LA46_149 = input.LA(1);

                         
                        int index46_149 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_149);
                        if ( s>=0 ) return s;
                        break;
                    case 115 :
                        int LA46_150 = input.LA(1);

                         
                        int index46_150 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_150);
                        if ( s>=0 ) return s;
                        break;
                    case 116 :
                        int LA46_151 = input.LA(1);

                         
                        int index46_151 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_151);
                        if ( s>=0 ) return s;
                        break;
                    case 117 :
                        int LA46_152 = input.LA(1);

                         
                        int index46_152 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_152);
                        if ( s>=0 ) return s;
                        break;
                    case 118 :
                        int LA46_153 = input.LA(1);

                         
                        int index46_153 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_153);
                        if ( s>=0 ) return s;
                        break;
                    case 119 :
                        int LA46_154 = input.LA(1);

                         
                        int index46_154 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_154);
                        if ( s>=0 ) return s;
                        break;
                    case 120 :
                        int LA46_155 = input.LA(1);

                         
                        int index46_155 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_155);
                        if ( s>=0 ) return s;
                        break;
                    case 121 :
                        int LA46_156 = input.LA(1);

                         
                        int index46_156 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_156);
                        if ( s>=0 ) return s;
                        break;
                    case 122 :
                        int LA46_157 = input.LA(1);

                         
                        int index46_157 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}

                         
                        input.seek(index46_157);
                        if ( s>=0 ) return s;
                        break;
                    case 123 :
                        int LA46_158 = input.LA(1);


                        int index46_158 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_158);
                        if ( s>=0 ) return s;
                        break;
                    case 124 :
                        int LA46_159 = input.LA(1);


                        int index46_159 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_159);
                        if ( s>=0 ) return s;
                        break;
                    case 125 :
                        int LA46_160 = input.LA(1);


                        int index46_160 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_160);
                        if ( s>=0 ) return s;
                        break;
                    case 126 :
                        int LA46_161 = input.LA(1);


                        int index46_161 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_161);
                        if ( s>=0 ) return s;
                        break;
                    case 127 :
                        int LA46_162 = input.LA(1);


                        int index46_162 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_162);
                        if ( s>=0 ) return s;
                        break;
                    case 128 :
                        int LA46_163 = input.LA(1);


                        int index46_163 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_163);
                        if ( s>=0 ) return s;
                        break;
                    case 129 :
                        int LA46_164 = input.LA(1);


                        int index46_164 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_164);
                        if ( s>=0 ) return s;
                        break;
                    case 130 :
                        int LA46_165 = input.LA(1);


                        int index46_165 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_165);
                        if ( s>=0 ) return s;
                        break;
                    case 131 :
                        int LA46_166 = input.LA(1);


                        int index46_166 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_166);
                        if ( s>=0 ) return s;
                        break;
                    case 132 :
                        int LA46_167 = input.LA(1);


                        int index46_167 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_167);
                        if ( s>=0 ) return s;
                        break;
                    case 133 :
                        int LA46_168 = input.LA(1);


                        int index46_168 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_168);
                        if ( s>=0 ) return s;
                        break;
                    case 134 :
                        int LA46_169 = input.LA(1);


                        int index46_169 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_169);
                        if ( s>=0 ) return s;
                        break;
                    case 135 :
                        int LA46_170 = input.LA(1);


                        int index46_170 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred116_wcps()) ) {s = 32;}

                        else if ( (synpred117_wcps()) ) {s = 33;}

                        else if ( (true) ) {s = 171;}


                        input.seek(index46_170);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 46, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA47_eotS =
        "\41\uffff";
    static final String DFA47_eofS =
        "\41\uffff";
    static final String DFA47_minS =
        "\1\6\40\uffff";
    static final String DFA47_maxS =
        "\1\155\40\uffff";
    static final String DFA47_acceptS =
        "\1\uffff\1\1\36\uffff\1\2";
    static final String DFA47_specialS =
        "\41\uffff}>";
    static final String[] DFA47_transitionS = {
            "\1\1\20\uffff\2\1\2\uffff\20\1\3\uffff\1\1\4\uffff\6\1\1\uffff"+
            "\1\1\1\uffff\2\1\6\uffff\2\1\11\uffff\1\1\1\uffff\2\1\3\uffff"+
            "\1\40\14\uffff\1\1\10\uffff\2\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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

    static final short[] DFA47_eot = DFA.unpackEncodedString(DFA47_eotS);
    static final short[] DFA47_eof = DFA.unpackEncodedString(DFA47_eofS);
    static final char[] DFA47_min = DFA.unpackEncodedStringToUnsignedChars(DFA47_minS);
    static final char[] DFA47_max = DFA.unpackEncodedStringToUnsignedChars(DFA47_maxS);
    static final short[] DFA47_accept = DFA.unpackEncodedString(DFA47_acceptS);
    static final short[] DFA47_special = DFA.unpackEncodedString(DFA47_specialS);
    static final short[][] DFA47_transition;

    static {
        int numStates = DFA47_transitionS.length;
        DFA47_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA47_transition[i] = DFA.unpackEncodedString(DFA47_transitionS[i]);
        }
    }

    class DFA47 extends DFA {

        public DFA47(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 47;
            this.eot = DFA47_eot;
            this.eof = DFA47_eof;
            this.min = DFA47_min;
            this.max = DFA47_max;
            this.accept = DFA47_accept;
            this.special = DFA47_special;
            this.transition = DFA47_transition;
        }
        public String getDescription() {
            return "289:1: trimExpr returns [TrimExpr value] : (e1= coverageAtom LBRACKET dil= dimensionIntervalList RBRACKET | TRIM LPAREN e2= coverageExpr COMMA LBRACE dil= dimensionIntervalList RBRACE RPAREN );";
        }
    }
    static final String DFA51_eotS =
        "\41\uffff";
    static final String DFA51_eofS =
        "\41\uffff";
    static final String DFA51_minS =
        "\1\6\40\uffff";
    static final String DFA51_maxS =
        "\1\155\40\uffff";
    static final String DFA51_acceptS =
        "\1\uffff\1\1\36\uffff\1\2";
    static final String DFA51_specialS =
        "\41\uffff}>";
    static final String[] DFA51_transitionS = {
            "\1\1\20\uffff\2\1\2\uffff\20\1\3\uffff\1\1\4\uffff\6\1\1\uffff"+
            "\1\1\1\uffff\2\1\6\uffff\2\1\11\uffff\1\1\1\uffff\2\1\4\uffff"+
            "\1\40\13\uffff\1\1\10\uffff\2\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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
            return "302:1: sliceExpr returns [SliceExpr value] : (e1= coverageAtom LBRACKET dpl= dimensionPointList RBRACKET | SLICE LPAREN e2= coverageExpr COMMA LBRACE dpl= dimensionPointList RBRACE RPAREN );";
        }
    }
    static final String DFA53_eotS =
        "\104\uffff";
    static final String DFA53_eofS =
        "\1\1\103\uffff";
    static final String DFA53_minS =
        "\1\7\7\uffff\1\0\73\uffff";
    static final String DFA53_maxS =
        "\1\160\7\uffff\1\0\73\uffff";
    static final String DFA53_acceptS =
        "\1\uffff\1\2\101\uffff\1\1";
    static final String DFA53_specialS =
        "\10\uffff\1\0\73\uffff}>";
    static final String[] DFA53_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\2\10\14\1\21\uffff\2\1\3\uffff\1\1"+
            "\7\uffff\1\1\32\uffff\1\1\15\uffff\1\1\14\uffff\2\1",
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
            return "()* loopback of 323:7: (op= ( OR | XOR ) e2= booleanScalarTerm )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA53_8 = input.LA(1);

                         
                        int index53_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred135_wcps()) ) {s = 67;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index53_8);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 53, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA54_eotS =
        "\104\uffff";
    static final String DFA54_eofS =
        "\1\1\103\uffff";
    static final String DFA54_minS =
        "\1\7\7\uffff\1\0\73\uffff";
    static final String DFA54_maxS =
        "\1\160\7\uffff\1\0\73\uffff";
    static final String DFA54_acceptS =
        "\1\uffff\1\2\101\uffff\1\1";
    static final String DFA54_specialS =
        "\10\uffff\1\0\73\uffff}>";
    static final String[] DFA54_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\2\1\1\10\13\1\21\uffff\2\1\3\uffff"+
            "\1\1\7\uffff\1\1\32\uffff\1\1\15\uffff\1\1\14\uffff\2\1",
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
            "",
            "",
            "",
            "",
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
            return "()* loopback of 327:4: (op= AND e2= booleanScalarNegation )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA54_8 = input.LA(1);

                         
                        int index54_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred136_wcps()) ) {s = 67;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index54_8);
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
        "\20\uffff";
    static final String DFA55_eofS =
        "\20\uffff";
    static final String DFA55_minS =
        "\1\6\17\uffff";
    static final String DFA55_maxS =
        "\1\155\17\uffff";
    static final String DFA55_acceptS =
        "\1\uffff\1\1\15\uffff\1\2";
    static final String DFA55_specialS =
        "\20\uffff}>";
    static final String[] DFA55_transitionS = {
            "\1\1\20\uffff\2\1\2\uffff\1\1\7\uffff\10\1\21\uffff\2\1\6\uffff"+
            "\2\1\11\uffff\1\17\1\uffff\2\1\20\uffff\1\1\11\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
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
            return "329:1: booleanScalarNegation returns [BooleanScalarExpr value] : (e1= booleanScalarAtom | op= NOT e1= booleanScalarAtom );";
        }
    }
    static final String DFA56_eotS =
        "\36\uffff";
    static final String DFA56_eofS =
        "\36\uffff";
    static final String DFA56_minS =
        "\2\6\15\uffff\13\0\4\uffff";
    static final String DFA56_maxS =
        "\2\155\15\uffff\13\0\4\uffff";
    static final String DFA56_acceptS =
        "\2\uffff\1\2\1\uffff\1\3\11\uffff\1\4\13\uffff\1\1\3\uffff";
    static final String DFA56_specialS =
        "\17\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\4\uffff}>";
    static final String[] DFA56_transitionS = {
            "\1\1\20\uffff\2\4\2\uffff\1\2\7\uffff\10\4\21\uffff\2\4\6\uffff"+
            "\2\4\13\uffff\1\4\1\2\20\uffff\1\16\11\uffff\1\4",
            "\1\20\20\uffff\1\21\1\26\2\uffff\1\32\7\uffff\7\27\1\30\21"+
            "\uffff\1\23\1\22\6\uffff\1\17\1\25\11\uffff\1\32\1\uffff\1\24"+
            "\1\32\20\uffff\1\32\11\uffff\1\31",
            "",
            "",
            "",
            "",
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
            return "333:1: booleanScalarAtom returns [BooleanScalarExpr value] : ( LPAREN e1= booleanScalarExpr RPAREN | s1= stringScalarExpr cop= compOp s2= stringScalarExpr | n1= numericScalarExpr cop= compOp n2= numericScalarExpr | e= BOOLEANCONSTANT );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA56_15 = input.LA(1);

                         
                        int index56_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred138_wcps()) ) {s = 26;}

                        else if ( (synpred140_wcps()) ) {s = 4;}

                         
                        input.seek(index56_15);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA56_16 = input.LA(1);

                         
                        int index56_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred138_wcps()) ) {s = 26;}

                        else if ( (synpred140_wcps()) ) {s = 4;}

                         
                        input.seek(index56_16);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA56_17 = input.LA(1);


                        int index56_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred138_wcps()) ) {s = 26;}

                        else if ( (synpred140_wcps()) ) {s = 4;}


                        input.seek(index56_17);
                        if ( s>=0 ) return s;
                        break;
                    case 3 :
                        int LA56_18 = input.LA(1);

                         
                        int index56_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred138_wcps()) ) {s = 26;}

                        else if ( (synpred140_wcps()) ) {s = 4;}

                         
                        input.seek(index56_18);
                        if ( s>=0 ) return s;
                        break;
                    case 4 :
                        int LA56_19 = input.LA(1);

                         
                        int index56_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred138_wcps()) ) {s = 26;}

                        else if ( (synpred140_wcps()) ) {s = 4;}

                         
                        input.seek(index56_19);
                        if ( s>=0 ) return s;
                        break;
                    case 5 :
                        int LA56_20 = input.LA(1);

                         
                        int index56_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred138_wcps()) ) {s = 26;}

                        else if ( (synpred140_wcps()) ) {s = 4;}

                         
                        input.seek(index56_20);
                        if ( s>=0 ) return s;
                        break;
                    case 6 :
                        int LA56_21 = input.LA(1);

                         
                        int index56_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred138_wcps()) ) {s = 26;}

                        else if ( (synpred140_wcps()) ) {s = 4;}

                         
                        input.seek(index56_21);
                        if ( s>=0 ) return s;
                        break;
                    case 7 :
                        int LA56_22 = input.LA(1);

                         
                        int index56_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred138_wcps()) ) {s = 26;}

                        else if ( (synpred140_wcps()) ) {s = 4;}

                         
                        input.seek(index56_22);
                        if ( s>=0 ) return s;
                        break;
                    case 8 :
                        int LA56_23 = input.LA(1);

                         
                        int index56_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred138_wcps()) ) {s = 26;}

                        else if ( (synpred140_wcps()) ) {s = 4;}

                         
                        input.seek(index56_23);
                        if ( s>=0 ) return s;
                        break;
                    case 9 :
                        int LA56_24 = input.LA(1);

                         
                        int index56_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred138_wcps()) ) {s = 26;}

                        else if ( (synpred140_wcps()) ) {s = 4;}

                         
                        input.seek(index56_24);
                        if ( s>=0 ) return s;
                        break;
                    case 10 :
                        int LA56_25 = input.LA(1);

                         
                        int index56_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred138_wcps()) ) {s = 26;}

                        else if ( (synpred140_wcps()) ) {s = 4;}

                         
                        input.seek(index56_25);
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
        "\104\uffff";
    static final String DFA57_eofS =
        "\1\1\103\uffff";
    static final String DFA57_minS =
        "\1\7\3\uffff\1\0\77\uffff";
    static final String DFA57_maxS =
        "\1\160\3\uffff\1\0\77\uffff";
    static final String DFA57_acceptS =
        "\1\uffff\1\2\101\uffff\1\1";
    static final String DFA57_specialS =
        "\4\uffff\1\0\77\uffff}>";
    static final String[] DFA57_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\11\1\2\4\3\1\21\uffff\2\1\3\uffff\1"+
            "\1\7\uffff\1\1\32\uffff\1\1\15\uffff\1\1\14\uffff\2\1",
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
            return "()* loopback of 341:4: (op= ( PLUS | MINUS ) e2= numericScalarTerm )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA57_4 = input.LA(1);

                         
                        int index57_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred142_wcps()) ) {s = 67;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index57_4);
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
    static final String DFA58_eotS =
        "\104\uffff";
    static final String DFA58_eofS =
        "\1\1\103\uffff";
    static final String DFA58_minS =
        "\1\7\3\uffff\1\0\77\uffff";
    static final String DFA58_maxS =
        "\1\160\3\uffff\1\0\77\uffff";
    static final String DFA58_acceptS =
        "\1\uffff\1\2\101\uffff\1\1";
    static final String DFA58_specialS =
        "\4\uffff\1\0\77\uffff}>";
    static final String[] DFA58_transitionS = {
            "\2\1\1\uffff\1\1\2\uffff\13\1\2\4\1\1\21\uffff\2\1\3\uffff\1"+
            "\1\7\uffff\1\1\32\uffff\1\1\15\uffff\1\1\14\uffff\2\1",
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
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA58_eot = DFA.unpackEncodedString(DFA58_eotS);
    static final short[] DFA58_eof = DFA.unpackEncodedString(DFA58_eofS);
    static final char[] DFA58_min = DFA.unpackEncodedStringToUnsignedChars(DFA58_minS);
    static final char[] DFA58_max = DFA.unpackEncodedStringToUnsignedChars(DFA58_maxS);
    static final short[] DFA58_accept = DFA.unpackEncodedString(DFA58_acceptS);
    static final short[] DFA58_special = DFA.unpackEncodedString(DFA58_specialS);
    static final short[][] DFA58_transition;

    static {
        int numStates = DFA58_transitionS.length;
        DFA58_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA58_transition[i] = DFA.unpackEncodedString(DFA58_transitionS[i]);
        }
    }

    class DFA58 extends DFA {

        public DFA58(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 58;
            this.eot = DFA58_eot;
            this.eof = DFA58_eof;
            this.min = DFA58_min;
            this.max = DFA58_max;
            this.accept = DFA58_accept;
            this.special = DFA58_special;
            this.transition = DFA58_transition;
        }
        public String getDescription() {
            return "()* loopback of 345:3: (op= ( MULT | DIVIDE ) e2= numericScalarFactor )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA58_4 = input.LA(1);

                         
                        int index58_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred144_wcps()) ) {s = 67;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index58_4);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 58, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA59_eotS =
        "\30\uffff";
    static final String DFA59_eofS =
        "\30\uffff";
    static final String DFA59_minS =
        "\2\6\12\uffff\1\0\13\uffff";
    static final String DFA59_maxS =
        "\2\155\12\uffff\1\0\13\uffff";
    static final String DFA59_acceptS =
        "\2\uffff\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\12\1\uffff\1\13\1\uffff"+
        "\1\1\11\uffff\1\11";
    static final String DFA59_specialS =
        "\14\uffff\1\0\13\uffff}>";
    static final String[] DFA59_transitionS = {
            "\1\1\20\uffff\1\2\1\10\12\uffff\10\11\21\uffff\1\4\1\3\6\uffff"+
            "\1\7\1\6\13\uffff\1\5\33\uffff\1\13",
            "\1\15\20\uffff\2\15\12\uffff\10\15\21\uffff\2\15\6\uffff\1"+
            "\14\1\15\13\uffff\1\15\33\uffff\1\15",
            "",
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
            "",
            ""
    };

    static final short[] DFA59_eot = DFA.unpackEncodedString(DFA59_eotS);
    static final short[] DFA59_eof = DFA.unpackEncodedString(DFA59_eofS);
    static final char[] DFA59_min = DFA.unpackEncodedStringToUnsignedChars(DFA59_minS);
    static final char[] DFA59_max = DFA.unpackEncodedStringToUnsignedChars(DFA59_maxS);
    static final short[] DFA59_accept = DFA.unpackEncodedString(DFA59_acceptS);
    static final short[] DFA59_special = DFA.unpackEncodedString(DFA59_specialS);
    static final short[][] DFA59_transition;

    static {
        int numStates = DFA59_transitionS.length;
        DFA59_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA59_transition[i] = DFA.unpackEncodedString(DFA59_transitionS[i]);
        }
    }

    class DFA59 extends DFA {

        public DFA59(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 59;
            this.eot = DFA59_eot;
            this.eof = DFA59_eof;
            this.min = DFA59_min;
            this.max = DFA59_max;
            this.accept = DFA59_accept;
            this.special = DFA59_special;
            this.transition = DFA59_transition;
        }
        public String getDescription() {
            return "347:1: numericScalarFactor returns [NumericScalarExpr value] : ( LPAREN e1= numericScalarExpr RPAREN | op= MINUS e10= numericScalarFactor | op= ABS LPAREN e12= numericScalarExpr RPAREN | op= SQRT LPAREN e11= numericScalarExpr RPAREN | op= ROUND LPAREN e1= numericScalarExpr RPAREN | e= INTEGERCONSTANT | e= FLOATCONSTANT | e= MULT | e2= complexConstant | e3= condenseExpr | e4= variableName );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA59_12 = input.LA(1);

                         
                        int index59_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred145_wcps()) ) {s = 13;}

                        else if ( (synpred153_wcps()) ) {s = 23;}

                         
                        input.seek(index59_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 59, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA63_eotS =
        "\31\uffff";
    static final String DFA63_eofS =
        "\31\uffff";
    static final String DFA63_minS =
        "\1\6\4\uffff\1\6\21\uffff\1\0\1\uffff";
    static final String DFA63_maxS =
        "\1\155\4\uffff\1\6\21\uffff\1\0\1\uffff";
    static final String DFA63_acceptS =
        "\1\uffff\1\1\26\uffff\1\2";
    static final String DFA63_specialS =
        "\27\uffff\1\0\1\uffff}>";
    static final String[] DFA63_transitionS = {
            "\1\1\20\uffff\2\1\2\uffff\7\1\1\5\10\1\21\uffff\2\1\6\uffff"+
            "\2\1\11\uffff\1\1\1\uffff\2\1\20\uffff\1\1\11\uffff\1\1",
            "",
            "",
            "",
            "",
            "\1\27",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
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

    static final short[] DFA63_eot = DFA.unpackEncodedString(DFA63_eotS);
    static final short[] DFA63_eof = DFA.unpackEncodedString(DFA63_eofS);
    static final char[] DFA63_min = DFA.unpackEncodedStringToUnsignedChars(DFA63_minS);
    static final char[] DFA63_max = DFA.unpackEncodedStringToUnsignedChars(DFA63_maxS);
    static final short[] DFA63_accept = DFA.unpackEncodedString(DFA63_acceptS);
    static final short[] DFA63_special = DFA.unpackEncodedString(DFA63_specialS);
    static final short[][] DFA63_transition;

    static {
        int numStates = DFA63_transitionS.length;
        DFA63_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA63_transition[i] = DFA.unpackEncodedString(DFA63_transitionS[i]);
        }
    }

    class DFA63 extends DFA {

        public DFA63(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 63;
            this.eot = DFA63_eot;
            this.eof = DFA63_eof;
            this.min = DFA63_min;
            this.max = DFA63_max;
            this.accept = DFA63_accept;
            this.special = DFA63_special;
            this.transition = DFA63_transition;
        }
        public String getDescription() {
            return "376:1: dimensionIntervalExpr returns [DimensionIntervalExpr value] : (e1= scalarExpr COLON e2= scalarExpr | DOMAIN LPAREN e3= coverageName COLON e4= axisName COLON e5= crsName RPAREN );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA63_23 = input.LA(1);

                         
                        int index63_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred162_wcps()) ) {s = 1;}

                        else if ( (true) ) {s = 24;}

                         
                        input.seek(index63_23);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 63, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_forClause_in_wcpsRequest63 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_whereClause_in_wcpsRequest72 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_returnClause_in_wcpsRequest83 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_forClause98 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_coverageVariable_in_forClause102 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IN_in_forClause104 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_forClause106 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_coverageList_in_forClause110 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_forClause112 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_forClause122 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_coverageVariable_in_forClause126 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IN_in_forClause128 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_forClause130 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_coverageList_in_forClause134 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_forClause136 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_WHERE_in_whereClause157 = new BitSet(new long[]{0x300007F809800040L,0x0000200800068030L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_whereClause161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_RETURN_in_returnClause176 = new BitSet(new long[]{0x300007FFF9801840L,0x0000200800068030L});
    public static final BitSet FOLLOW_processingExpr_in_returnClause180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageName_in_coverageList197 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_coverageList204 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_coverageName_in_coverageList208 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_encodedCoverageExpr_in_processingExpr230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_storeExpr_in_processingExpr242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_processingExpr254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ENCODE_in_encodedCoverageExpr272 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_encodedCoverageExpr274 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_encodedCoverageExpr278 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_encodedCoverageExpr280 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_stringConstant_in_encodedCoverageExpr284 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_COMMA_in_encodedCoverageExpr291 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_stringConstant_in_encodedCoverageExpr295 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_encodedCoverageExpr302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STORE_in_storeExpr319 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_storeExpr321 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_encodedCoverageExpr_in_storeExpr325 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_storeExpr327 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageLogicTerm_in_coverageExpr350 = new BitSet(new long[]{0x0000000000006002L});
    public static final BitSet FOLLOW_set_in_coverageExpr366 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageLogicTerm_in_coverageExpr374 = new BitSet(new long[]{0x0000000000006002L});
    public static final BitSet FOLLOW_coverageLogicFactor_in_coverageLogicTerm400 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_coverageLogicTerm416 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageLogicFactor_in_coverageLogicTerm420 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_coverageArithmeticExpr_in_coverageLogicFactor447 = new BitSet(new long[]{0x00000000003F0002L});
    public static final BitSet FOLLOW_set_in_coverageLogicFactor463 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageArithmeticExpr_in_coverageLogicFactor479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageArithmeticTerm_in_coverageArithmeticExpr507 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_set_in_coverageArithmeticExpr523 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageArithmeticTerm_in_coverageArithmeticExpr531 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_coverageArithmeticFactor_in_coverageArithmeticTerm559 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_set_in_coverageArithmeticTerm574 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageArithmeticFactor_in_coverageArithmeticTerm582 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_coverageValue_in_coverageArithmeticFactor610 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_OVERLAY_in_coverageArithmeticFactor625 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageValue_in_coverageArithmeticFactor629 = new BitSet(new long[]{0x0000000004000002L});
    public static final BitSet FOLLOW_subsetExpr_in_coverageValue655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryInducedExpr_in_coverageValue668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scaleExpr_in_coverageValue680 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_crsTransformExpr_in_coverageValue692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_coverageValue704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_switchExpr_in_coverageValue716 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_coverageAtom739 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageVariable_in_coverageAtom751 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_coverageAtom761 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_coverageAtom765 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_coverageAtom767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstantExpr_in_coverageAtom780 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstructorExpr_in_coverageAtom792 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_setMetaDataExpr_in_coverageAtom805 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rangeConstructorExpr_in_coverageAtom818 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_metaDataExpr_in_scalarExpr842 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_condenseExpr_in_scalarExpr855 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_scalarExpr868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericScalarExpr_in_scalarExpr882 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringScalarExpr_in_scalarExpr895 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_scalarExpr906 = new BitSet(new long[]{0x300007FFF9801840L,0x0000200800068030L});
    public static final BitSet FOLLOW_scalarExpr_in_scalarExpr910 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_scalarExpr912 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_metaDataExpr936 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr938 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr942 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr944 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMAGECRS_in_metaDataExpr956 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr958 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr962 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr964 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMAGECRSDOMAIN_in_metaDataExpr976 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr978 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr982 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_COMMA_in_metaDataExpr985 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_axisName_in_metaDataExpr989 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr993 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CRSSET_in_metaDataExpr1005 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr1007 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr1011 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr1013 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_domainExpr_in_metaDataExpr1025 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULLSET_in_metaDataExpr1037 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr1039 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr1043 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr1045 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTERPOLATIONDEFAULT_in_metaDataExpr1057 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr1059 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr1063 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_metaDataExpr1065 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_fieldName_in_metaDataExpr1069 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr1071 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTERPOLATIONSET_in_metaDataExpr1083 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_metaDataExpr1085 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_metaDataExpr1089 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_metaDataExpr1091 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_fieldName_in_metaDataExpr1095 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_metaDataExpr1097 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOMAIN_in_domainExpr1116 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_domainExpr1118 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_coverageVariable_in_domainExpr1122 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_domainExpr1124 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_axisName_in_domainExpr1128 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_domainExpr1130 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_crsName_in_domainExpr1134 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_domainExpr1136 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_reduceExpr_in_condenseExpr1153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalCondenseExpr_in_condenseExpr1162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_reduceExpr1179 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_reduceExpr1195 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_reduceExpr1199 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_reduceExpr1201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CONDENSE_in_generalCondenseExpr1216 = new BitSet(new long[]{0x000003000140A000L});
    public static final BitSet FOLLOW_condenseOpType_in_generalCondenseExpr1220 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_OVER_in_generalCondenseExpr1222 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_axisIteratorList_in_generalCondenseExpr1226 = new BitSet(new long[]{0x0000100000000200L});
    public static final BitSet FOLLOW_WHERE_in_generalCondenseExpr1233 = new BitSet(new long[]{0x300007F809800040L,0x0000200800068030L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_generalCondenseExpr1237 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_USING_in_generalCondenseExpr1245 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_generalCondenseExpr1249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableName_in_axisIteratorList1266 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_axisName_in_axisIteratorList1270 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_axisIteratorList1272 = new BitSet(new long[]{0x0000000020000040L,0x0000000000020020L});
    public static final BitSet FOLLOW_intervalExpr_in_axisIteratorList1276 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_axisIteratorList1278 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_axisIteratorList1286 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_variableName_in_axisIteratorList1290 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_axisName_in_axisIteratorList1294 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_axisIteratorList1296 = new BitSet(new long[]{0x0000000020000040L,0x0000000000020020L});
    public static final BitSet FOLLOW_intervalExpr_in_axisIteratorList1300 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_axisIteratorList1302 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_indexExpr_in_intervalExpr1326 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_intervalExpr1328 = new BitSet(new long[]{0x0000000000000040L,0x0000000000020020L});
    public static final BitSet FOLLOW_indexExpr_in_intervalExpr1332 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMAGECRSDOMAIN_in_intervalExpr1347 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_intervalExpr1349 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_intervalExpr1353 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_intervalExpr1355 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_axisName_in_intervalExpr1359 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_intervalExpr1361 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COVERAGE_in_coverageConstantExpr1387 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_coverageName_in_coverageConstantExpr1391 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_OVER_in_coverageConstantExpr1393 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_axisIteratorList_in_coverageConstantExpr1397 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_VALUE_in_coverageConstantExpr1399 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_LIST_in_coverageConstantExpr1401 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_LT_in_coverageConstantExpr1403 = new BitSet(new long[]{0x0000000000000040L,0x0000000800040030L});
    public static final BitSet FOLLOW_constantList_in_coverageConstantExpr1407 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GT_in_coverageConstantExpr1409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_in_constantList1440 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_SEMICOLON_in_constantList1445 = new BitSet(new long[]{0x0000000000000040L,0x0000000800040030L});
    public static final BitSet FOLLOW_constant_in_constantList1449 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_subsetExpr_in_coverageAtomConstructor1475 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryInducedExpr_in_coverageAtomConstructor1488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scaleExpr_in_coverageAtomConstructor1500 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_crsTransformExpr_in_coverageAtomConstructor1512 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_coverageAtomConstructor1524 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_coverageAtomConstructor1536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageVariable_in_coverageAtomConstructor1548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstantExpr_in_coverageAtomConstructor1560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstructorExpr_in_coverageAtomConstructor1572 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_setMetaDataExpr_in_coverageAtomConstructor1585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rangeConstructorExpr_in_coverageAtomConstructor1598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_switchExpr_in_coverageAtomConstructor1611 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COVERAGE_in_coverageConstructorExpr1629 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_coverageName_in_coverageConstructorExpr1633 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_OVER_in_coverageConstructorExpr1635 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_axisIteratorList_in_coverageConstructorExpr1639 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_VALUES_in_coverageConstructorExpr1641 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageAtomConstructor_in_coverageConstructorExpr1645 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SETIDENTIFIER_in_setMetaDataExpr1667 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_setMetaDataExpr1669 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_stringConstant_in_setMetaDataExpr1673 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1675 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_setMetaDataExpr1679 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_setMetaDataExpr1681 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SETCRSSET_in_setMetaDataExpr1694 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_setMetaDataExpr1696 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_setMetaDataExpr1700 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1702 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_crsList_in_setMetaDataExpr1706 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_setMetaDataExpr1708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SETNULLSET_in_setMetaDataExpr1725 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_setMetaDataExpr1727 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_setMetaDataExpr1731 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1733 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_rangeExprList_in_setMetaDataExpr1737 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_setMetaDataExpr1739 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SETINTERPOLATIONDEFAULT_in_setMetaDataExpr1756 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_setMetaDataExpr1758 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_setMetaDataExpr1762 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1764 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_fieldName_in_setMetaDataExpr1768 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1770 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_interpolationMethod_in_setMetaDataExpr1774 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_setMetaDataExpr1776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SETINTERPOLATIONSET_in_setMetaDataExpr1796 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_setMetaDataExpr1798 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_setMetaDataExpr1802 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1804 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_fieldName_in_setMetaDataExpr1808 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_setMetaDataExpr1810 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_interpolationMethodList_in_setMetaDataExpr1814 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_setMetaDataExpr1816 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_crsList1845 = new BitSet(new long[]{0x0200000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_crsName_in_crsList1852 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_COMMA_in_crsList1857 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_crsName_in_crsList1861 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_RBRACE_in_crsList1870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_rangeExprList1889 = new BitSet(new long[]{0x0600000000000000L});
    public static final BitSet FOLLOW_rangeExpr_in_rangeExprList1896 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_COMMA_in_rangeExprList1901 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_rangeExpr_in_rangeExprList1905 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_RBRACE_in_rangeExprList1914 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_interpolationMethodList1930 = new BitSet(new long[]{0x0200000000000040L});
    public static final BitSet FOLLOW_interpolationMethod_in_interpolationMethodList1937 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_COMMA_in_interpolationMethodList1942 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_interpolationMethod_in_interpolationMethodList1946 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_RBRACE_in_interpolationMethodList1954 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRUCT_in_rangeExpr1967 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_rangeExpr1969 = new BitSet(new long[]{0x0200000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_fieldName_in_rangeExpr1977 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_rangeExpr1979 = new BitSet(new long[]{0x300007FFF9801840L,0x0000200800068030L});
    public static final BitSet FOLLOW_scalarExpr_in_rangeExpr1983 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_COMMA_in_rangeExpr1990 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_fieldName_in_rangeExpr1994 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_rangeExpr1996 = new BitSet(new long[]{0x300007FFF9801840L,0x0000200800068030L});
    public static final BitSet FOLLOW_scalarExpr_in_rangeExpr2000 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_RBRACE_in_rangeExpr2011 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRUCT_in_rangeConstructorExpr2028 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_rangeConstructorExpr2032 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_fieldName_in_rangeConstructorExpr2036 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_rangeConstructorExpr2038 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_rangeConstructorExpr2042 = new BitSet(new long[]{0x0202000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_rangeConstructorExpr2055 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_fieldName_in_rangeConstructorExpr2059 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_rangeConstructorExpr2061 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_rangeConstructorExpr2065 = new BitSet(new long[]{0x0202000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_rangeConstructorExpr2071 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CRSTRANSFORM_in_crsTransformExpr2094 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_crsTransformExpr2096 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_crsTransformExpr2100 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_crsTransformExpr2102 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_dimensionCrsList_in_crsTransformExpr2106 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_crsTransformExpr2108 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_fieldInterpolationList_in_crsTransformExpr2112 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_crsTransformExpr2114 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_dimensionCrsList2155 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionCrsElement_in_dimensionCrsList2159 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_COMMA_in_dimensionCrsList2172 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionCrsElement_in_dimensionCrsList2176 = new BitSet(new long[]{0x0200000000000100L});
    public static final BitSet FOLLOW_RBRACE_in_dimensionCrsList2183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_axisName_in_dimensionCrsElement2204 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dimensionCrsElement2208 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_crsName_in_dimensionCrsElement2212 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACE_in_fieldInterpolationList2230 = new BitSet(new long[]{0x0200000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_fieldInterpolationElement_in_fieldInterpolationList2235 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_fieldInterpolationList2242 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_fieldInterpolationElement_in_fieldInterpolationList2246 = new BitSet(new long[]{0x0200000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_RBRACE_in_fieldInterpolationList2254 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldName_in_fieldInterpolationElement2269 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_interpolationMethod_in_fieldInterpolationElement2273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldExpr_in_unaryInducedExpr2293 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryArithmeticExpr_in_unaryInducedExpr2302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_exponentialExpr_in_unaryInducedExpr2314 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_trigonometricExpr_in_unaryInducedExpr2326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanExpr_in_unaryInducedExpr2338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_castExpr_in_unaryInducedExpr2350 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rangeConstructorExpr_in_unaryInducedExpr2362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryArithmeticExpr2385 = new BitSet(new long[]{0x35F847FFF9801840L,0x0000300800068030L});
    public static final BitSet FOLLOW_coverageAtom_in_unaryArithmeticExpr2393 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryArithmeticExpr2405 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_unaryArithmeticExpr2415 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_unaryArithmeticExpr2419 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_unaryArithmeticExpr2421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_exponentialExpr2444 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_exponentialExpr2452 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_exponentialExpr2456 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_exponentialExpr2458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_POW_in_exponentialExpr2470 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_exponentialExpr2472 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_exponentialExpr2476 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_exponentialExpr2478 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000030L});
    public static final BitSet FOLLOW_set_in_exponentialExpr2482 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_exponentialExpr2488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_trigonometricExpr2511 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_trigonometricExpr2531 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_trigonometricExpr2535 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_trigonometricExpr2537 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_booleanExpr2560 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_booleanExpr2564 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BIT_in_booleanExpr2576 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_booleanExpr2578 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_booleanExpr2582 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_booleanExpr2584 = new BitSet(new long[]{0x0000000000000040L,0x0000000000020020L});
    public static final BitSet FOLLOW_indexExpr_in_booleanExpr2588 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_booleanExpr2590 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_indexTerm_in_indexExpr2613 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_set_in_indexExpr2623 = new BitSet(new long[]{0x0000000000000040L,0x0000000000020020L});
    public static final BitSet FOLLOW_indexTerm_in_indexExpr2631 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_indexFactor_in_indexTerm2656 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_set_in_indexTerm2669 = new BitSet(new long[]{0x0000000000000040L,0x0000000000020020L});
    public static final BitSet FOLLOW_indexFactor_in_indexTerm2677 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_INTEGERCONSTANT_in_indexFactor2704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROUND_in_indexFactor2717 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_indexFactor2719 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarExpr_in_indexFactor2723 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_indexFactor2725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_indexFactor2737 = new BitSet(new long[]{0x0000000000000040L,0x0000000000020020L});
    public static final BitSet FOLLOW_indexExpr_in_indexFactor2741 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_indexFactor2743 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_stringScalarExpr2770 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_stringScalarExpr2772 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_stringScalarExpr2776 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_stringScalarExpr2778 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_stringScalarExpr2791 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SCALE_in_scaleExpr2809 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_scaleExpr2811 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_scaleExpr2824 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_scaleExpr2826 = new BitSet(new long[]{0x0100000020000000L});
    public static final BitSet FOLLOW_LBRACE_in_scaleExpr2853 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionIntervalList_in_scaleExpr2857 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_scaleExpr2859 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_IMAGECRSDOMAIN_in_scaleExpr2880 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_scaleExpr2882 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_scaleExpr2886 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_scaleExpr2888 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_COMMA_in_scaleExpr2915 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_fieldInterpolationList_in_scaleExpr2919 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_scaleExpr2934 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_trimExpr_in_subsetExpr2949 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sliceExpr_in_subsetExpr2958 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_extendExpr_in_subsetExpr2967 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_trimSliceExpr_in_subsetExpr2979 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_trimExpr2996 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_LBRACKET_in_trimExpr2998 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionIntervalList_in_trimExpr3002 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RBRACKET_in_trimExpr3004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRIM_in_trimExpr3013 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_trimExpr3015 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_trimExpr3019 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_trimExpr3021 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_trimExpr3023 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionIntervalList_in_trimExpr3027 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_trimExpr3029 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_trimExpr3031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_trimSliceExpr3048 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_LBRACKET_in_trimSliceExpr3050 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_trimSliceExpr3087 = new BitSet(new long[]{0x0000000000000100L,0x0000000000200000L});
    public static final BitSet FOLLOW_dimensionPointElement_in_trimSliceExpr3124 = new BitSet(new long[]{0x0000000000000100L,0x0000000000200000L});
    public static final BitSet FOLLOW_COMMA_in_trimSliceExpr3160 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_trimSliceExpr3196 = new BitSet(new long[]{0x0000000000000100L,0x0000000000200000L});
    public static final BitSet FOLLOW_dimensionPointElement_in_trimSliceExpr3233 = new BitSet(new long[]{0x0000000000000100L,0x0000000000200000L});
    public static final BitSet FOLLOW_RBRACKET_in_trimSliceExpr3264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_sliceExpr3283 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_LBRACKET_in_sliceExpr3285 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionPointList_in_sliceExpr3289 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_RBRACKET_in_sliceExpr3291 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SLICE_in_sliceExpr3298 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_sliceExpr3300 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_sliceExpr3304 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_sliceExpr3306 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_sliceExpr3308 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionPointList_in_sliceExpr3312 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_sliceExpr3314 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_sliceExpr3316 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXTEND_in_extendExpr3331 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_extendExpr3333 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_extendExpr3337 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_extendExpr3339 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_LBRACE_in_extendExpr3341 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionIntervalList_in_extendExpr3345 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_extendExpr3347 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_extendExpr3349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_castExpr3367 = new BitSet(new long[]{0x0000000000000000L,0x00000003FE000000L});
    public static final BitSet FOLLOW_rangeType_in_castExpr3371 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_castExpr3373 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_castExpr3377 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_rangeType3400 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_UNSIGNED_in_rangeType3426 = new BitSet(new long[]{0x0000000000000000L,0x000000001C000000L});
    public static final BitSet FOLLOW_set_in_rangeType3430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_fieldExpr3459 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
    public static final BitSet FOLLOW_DOT_in_fieldExpr3461 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_fieldName_in_fieldExpr3465 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanScalarTerm_in_booleanScalarExpr3490 = new BitSet(new long[]{0x0000000000006002L});
    public static final BitSet FOLLOW_set_in_booleanScalarExpr3503 = new BitSet(new long[]{0x300007F809800040L,0x0000200800068030L});
    public static final BitSet FOLLOW_booleanScalarTerm_in_booleanScalarExpr3511 = new BitSet(new long[]{0x0000000000006002L});
    public static final BitSet FOLLOW_booleanScalarNegation_in_booleanScalarTerm3533 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_booleanScalarTerm3543 = new BitSet(new long[]{0x300007F809800040L,0x0000200800068030L});
    public static final BitSet FOLLOW_booleanScalarNegation_in_booleanScalarTerm3547 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_booleanScalarAtom_in_booleanScalarNegation3568 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_booleanScalarNegation3577 = new BitSet(new long[]{0x300007F809800040L,0x0000200800060030L});
    public static final BitSet FOLLOW_booleanScalarAtom_in_booleanScalarNegation3581 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_booleanScalarAtom3596 = new BitSet(new long[]{0x300007F809800040L,0x0000200800068030L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_booleanScalarAtom3600 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_booleanScalarAtom3602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringScalarExpr_in_booleanScalarAtom3611 = new BitSet(new long[]{0x00000000003F0000L});
    public static final BitSet FOLLOW_compOp_in_booleanScalarAtom3615 = new BitSet(new long[]{0x0000000008000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_stringScalarExpr_in_booleanScalarAtom3619 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericScalarExpr_in_booleanScalarAtom3629 = new BitSet(new long[]{0x00000000003F0000L});
    public static final BitSet FOLLOW_compOp_in_booleanScalarAtom3633 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarExpr_in_booleanScalarAtom3637 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BOOLEANCONSTANT_in_booleanScalarAtom3647 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericScalarTerm_in_numericScalarExpr3664 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_set_in_numericScalarExpr3674 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarTerm_in_numericScalarExpr3682 = new BitSet(new long[]{0x0000000000C00002L});
    public static final BitSet FOLLOW_numericScalarFactor_in_numericScalarTerm3701 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_set_in_numericScalarTerm3710 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarFactor_in_numericScalarTerm3718 = new BitSet(new long[]{0x0000000003000002L});
    public static final BitSet FOLLOW_LPAREN_in_numericScalarFactor3738 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarExpr_in_numericScalarFactor3742 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_numericScalarFactor3744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_numericScalarFactor3756 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarFactor_in_numericScalarFactor3760 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ABS_in_numericScalarFactor3772 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_numericScalarFactor3774 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarExpr_in_numericScalarFactor3778 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_numericScalarFactor3780 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SQRT_in_numericScalarFactor3792 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_numericScalarFactor3794 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarExpr_in_numericScalarFactor3798 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_numericScalarFactor3800 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROUND_in_numericScalarFactor3812 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_numericScalarFactor3814 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarExpr_in_numericScalarFactor3818 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_numericScalarFactor3820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INTEGERCONSTANT_in_numericScalarFactor3832 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FLOATCONSTANT_in_numericScalarFactor3844 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MULT_in_numericScalarFactor3856 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_complexConstant_in_numericScalarFactor3868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_condenseExpr_in_numericScalarFactor3880 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variableName_in_numericScalarFactor3892 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_compOp3910 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOTEQUALS_in_compOp3917 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LT_in_compOp3924 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GT_in_compOp3931 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LTE_in_compOp3938 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GTE_in_compOp3945 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_dimensionIntervalList3965 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_dimensionIntervalList3978 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_dimensionIntervalList3982 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_axisName_in_dimensionIntervalElement4007 = new BitSet(new long[]{0x0000200000000040L});
    public static final BitSet FOLLOW_COLON_in_dimensionIntervalElement4012 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_crsName_in_dimensionIntervalElement4016 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_dimensionIntervalElement4027 = new BitSet(new long[]{0x300007FFF9801840L,0x0000200800068030L});
    public static final BitSet FOLLOW_dimensionIntervalExpr_in_dimensionIntervalElement4031 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_dimensionIntervalElement4033 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_dimensionIntervalExpr4056 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dimensionIntervalExpr4058 = new BitSet(new long[]{0x300007FFF9801840L,0x0000200800068030L});
    public static final BitSet FOLLOW_scalarExpr_in_dimensionIntervalExpr4062 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOMAIN_in_dimensionIntervalExpr4072 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_dimensionIntervalExpr4074 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_coverageName_in_dimensionIntervalExpr4078 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dimensionIntervalExpr4080 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_axisName_in_dimensionIntervalExpr4084 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dimensionIntervalExpr4086 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_crsName_in_dimensionIntervalExpr4090 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_dimensionIntervalExpr4092 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dimensionPointElement_in_dimensionPointList4115 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_dimensionPointList4125 = new BitSet(new long[]{0x0000000000000000L,0x0000100000040020L});
    public static final BitSet FOLLOW_dimensionPointElement_in_dimensionPointList4129 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_axisName_in_dimensionPointElement4154 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_dimensionPointElement4156 = new BitSet(new long[]{0x300007FFF9801840L,0x0000200800068030L});
    public static final BitSet FOLLOW_dimensionPoint_in_dimensionPointElement4160 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_dimensionPointElement4162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_axisName_in_dimensionPointElement4174 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_dimensionPointElement4176 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_crsName_in_dimensionPointElement4180 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_dimensionPointElement4182 = new BitSet(new long[]{0x300007FFF9801840L,0x0000200800068030L});
    public static final BitSet FOLLOW_dimensionPoint_in_dimensionPointElement4186 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_dimensionPointElement4188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_dimensionPoint4211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_interpolationMethod4229 = new BitSet(new long[]{0x0000000000000000L,0x000000F000000000L});
    public static final BitSet FOLLOW_interpolationType_in_interpolationMethod4233 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_interpolationMethod4235 = new BitSet(new long[]{0x0000000000000000L,0x00000F0000000000L});
    public static final BitSet FOLLOW_nullResistence_in_interpolationMethod4239 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_interpolationMethod4241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_interpolationType4258 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nullResistence4283 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_condenseOpType4308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_fieldName4335 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_constant4352 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_complexConstant_in_constant4369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_complexConstant4384 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_FLOATCONSTANT_in_complexConstant4388 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_complexConstant4390 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_FLOATCONSTANT_in_complexConstant4394 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_complexConstant4396 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_stringConstant4413 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_name4430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringConstant_in_crsName4457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_axisName4474 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VARIABLE_DOLLAR_in_variableName4491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_coverageVariable4508 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_coverageName4523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SWITCH_in_switchExpr4541 = new BitSet(new long[]{0x0000000000000000L,0x0000800000000000L});
    public static final BitSet FOLLOW_CASE_in_switchExpr4543 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_switchExpr4549 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_RETURN_in_switchExpr4551 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_switchExpr4557 = new BitSet(new long[]{0x0000000000000000L,0x0001800000000000L});
    public static final BitSet FOLLOW_CASE_in_switchExpr4570 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_switchExpr4576 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_RETURN_in_switchExpr4578 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_switchExpr4584 = new BitSet(new long[]{0x0000000000000000L,0x0001800000000000L});
    public static final BitSet FOLLOW_DEFAULT_in_switchExpr4598 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_RETURN_in_switchExpr4600 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_switchExpr4606 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred8_wcps366 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageLogicTerm_in_synpred8_wcps374 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_synpred9_wcps416 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageLogicFactor_in_synpred9_wcps420 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred15_wcps463 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageArithmeticExpr_in_synpred15_wcps479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred17_wcps523 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageArithmeticTerm_in_synpred17_wcps531 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred19_wcps574 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageArithmeticFactor_in_synpred19_wcps582 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_OVERLAY_in_synpred20_wcps625 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageValue_in_synpred20_wcps629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subsetExpr_in_synpred21_wcps655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryInducedExpr_in_synpred22_wcps668 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_synpred25_wcps704 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_synpred26_wcps739 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred28_wcps761 = new BitSet(new long[]{0xFDF847FFF9C01840L,0x0000700801CFFFFFL});
    public static final BitSet FOLLOW_coverageExpr_in_synpred28_wcps765 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_synpred28_wcps767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstantExpr_in_synpred29_wcps780 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstructorExpr_in_synpred30_wcps792 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_metaDataExpr_in_synpred32_wcps842 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_condenseExpr_in_synpred33_wcps855 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_synpred34_wcps868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericScalarExpr_in_synpred35_wcps882 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_stringScalarExpr_in_synpred36_wcps895 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subsetExpr_in_synpred56_wcps1475 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryInducedExpr_in_synpred57_wcps1488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageAtom_in_synpred60_wcps1524 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_synpred61_wcps1536 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageVariable_in_synpred62_wcps1548 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstantExpr_in_synpred63_wcps1560 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_coverageConstructorExpr_in_synpred64_wcps1572 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_setMetaDataExpr_in_synpred65_wcps1585 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rangeConstructorExpr_in_synpred66_wcps1598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fieldExpr_in_synpred83_wcps2293 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_unaryArithmeticExpr_in_synpred84_wcps2302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanExpr_in_synpred87_wcps2338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_trimExpr_in_synpred116_wcps2949 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_sliceExpr_in_synpred117_wcps2958 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_synpred120_wcps3087 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dimensionIntervalElement_in_synpred121_wcps3196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred135_wcps3503 = new BitSet(new long[]{0x300007F809800040L,0x0000200800068030L});
    public static final BitSet FOLLOW_booleanScalarTerm_in_synpred135_wcps3511 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AND_in_synpred136_wcps3543 = new BitSet(new long[]{0x300007F809800040L,0x0000200800068030L});
    public static final BitSet FOLLOW_booleanScalarNegation_in_synpred136_wcps3547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred138_wcps3596 = new BitSet(new long[]{0x300007F809800040L,0x0000200800068030L});
    public static final BitSet FOLLOW_booleanScalarExpr_in_synpred138_wcps3600 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_synpred138_wcps3602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_numericScalarExpr_in_synpred140_wcps3629 = new BitSet(new long[]{0x00000000003F0000L});
    public static final BitSet FOLLOW_compOp_in_synpred140_wcps3633 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarExpr_in_synpred140_wcps3637 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred142_wcps3674 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarTerm_in_synpred142_wcps3682 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_synpred144_wcps3710 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarFactor_in_synpred144_wcps3718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred145_wcps3738 = new BitSet(new long[]{0x300007F801800040L,0x0000200000020030L});
    public static final BitSet FOLLOW_numericScalarExpr_in_synpred145_wcps3742 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_synpred145_wcps3744 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_complexConstant_in_synpred153_wcps3868 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scalarExpr_in_synpred162_wcps4056 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_COLON_in_synpred162_wcps4058 = new BitSet(new long[]{0x300007FFF9801840L,0x0000200800068030L});
    public static final BitSet FOLLOW_scalarExpr_in_synpred162_wcps4062 = new BitSet(new long[]{0x0000000000000002L});

}