%{
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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
*
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

/*
 * COMMENTS:
 * - token BY unused
 * 
 ************************************************************/

#pragma GCC diagnostic ignored "-Wsign-conversion"
#pragma GCC diagnostic ignored "-Wstrict-overflow"

#include "config.h"
#include "qlparser/qtcollection.hh"
#include "qlparser/qtconversion.hh"
#include "qlparser/qtmarrayop.hh"
#include "qlparser/qtcondense.hh"
#include "qlparser/qtbinaryinduce2.hh"
#include "qlparser/qtbinaryfunc.hh"
#include "qlparser/qtoid.hh"
#include "qlparser/qtcondenseop.hh"
#include "qlparser/qtstringdata.hh"
#include "qlparser/qtconst.hh"
#include "qlparser/qtintervalop.hh"
#include "qlparser/qtmintervalop.hh"
#include "qlparser/qtnullvaluesop.hh"
#include "qlparser/qtunaryfunc.hh"
#include "qlparser/qtupdate.hh"
#include "qlparser/qtproject.hh"
#include "qlparser/qtinsert.hh"
#include "qlparser/qtdelete.hh"
#include "qlparser/qtjoiniterator.hh"
#include "qlparser/qtselectioniterator.hh"
#include "qlparser/qtoperationiterator.hh"
#include "qlparser/qtcommand.hh"
#include "qlparser/qtunaryinduce.hh"
#include "qlparser/qtiterator.hh"
#include "qlparser/qtcomplexdata.hh"
#include "qlparser/qtmddaccess.hh"
#include "qlparser/querytree.hh"
#include "servercomm/servercomm.hh"
#include "qlparser/parseinfo.hh"
#include "qlparser/qtmddcfgop.hh"
#include "qlparser/qtconcat.hh"
#include "qlparser/qtcaseop.hh"
#include "qlparser/qtcaseequality.hh"
#include "qlparser/qtsimplecaseop.hh"
#include "rasodmg/dirdecompose.hh"
#include "qlparser/qtinfo.hh"
#include "qlparser/qtemptystream.hh"
#include "qlparser/qtrangeconstructor.hh"
#include "qlparser/qtcreatecelltype.hh"
#include "qlparser/qtcreatemarraytype.hh"
#include "qlparser/qtcreatesettype.hh"
#include "qlparser/qtdroptype.hh"
#include "qlparser/qtcelltypeattributes.hh"
#include "relcatalogif/syntaxtypes.hh"

#include "qlparser/qtclippingfunc.hh"
#include "qlparser/qtmshapedata.hh"
#include "qlparser/qtmshapeop.hh"
#include "qlparser/qtgeometryop.hh"
#include "qlparser/qtgeometrydata.hh"
#include "servercomm/cliententry.hh"

#include <vector>

#undef EQUAL
#undef ABS

extern ClientTblElt* currentClientTblElt;
extern ParseInfo *currInfo;

void   yyerror(void* mflag, const char* s );

extern int  yylex();
extern unsigned int lineNo;
extern unsigned int columnNo;
extern char*        yytext;

//defined in oql.l
extern QueryTree* parseQueryTree;

ParseInfo* parseError = NULL;

struct QtUpdateSpecElement
{
  QtOperation* iterator;
  QtOperation* domain;
};

#define FREESTACK( ARG )                              \
          parseQueryTree->removeDynamicObject( ARG.info );  \
          delete ARG.info;                            \
          ARG.info=NULL;

// simple context dependancy for marray
#define YYPARSE_PARAM mflag
#define MF_IN_CONTEXT (void *)1
#define MF_NO_CONTEXT (void *)0

%}

// definition section

%union {

  struct {
    bool         value;
    ParseInfo*   info;
  } booleanToken;

  struct {
    char         value;
    ParseInfo*   info;
  } characterToken;

  struct {
    unsigned short negative; // 1 = signed value, 0 = unsigned value
    r_Long         svalue;   // stores the signed   value -> negative = 1
    r_ULong        uvalue;   // stores the unsigned value -> negative = 0;
    unsigned short bytes;    // stores the length in bytes (1,2,3,4)
    ParseInfo*     info;
  } integerToken;

  struct {
    double         value;
    unsigned short bytes;    // stores the length in bytes (4,8)
    ParseInfo*     info;
  } floatToken;

  struct {
    char*        value;
    ParseInfo*   info;
  } stringToken;

   struct {
    int		     value;
    ParseInfo*   info;
  } typeToken;


  struct {
    int          value;
    ParseInfo*   info;
  } commandToken;

  struct {
    char*        value;
    ParseInfo*   info;
  } identifierToken;

//--------------------------------------------------
  QtMarrayOp2::mddIntervalType     * mddIntervalType;
  QtMarrayOp2::mddIntervalListType * mddIntervalListType;
//---------------------------------------------------

  r_Sinterval*                      Sinterval;
  
  QtNode*                           qtNodeValue;
  QtOperation*                      qtOperationValue;
  QtUnaryOperation*                 qtUnaryOperationValue;
  QtDomainOperation*                qtDomainValue;
  QtMDDAccess*                      qtMDDAccessValue;

  QtData*                           qtDataValue;
  QtScalarData*                     qtScalarDataValue;
  QtAtomicData*                     qtAtomicDataValue;
  QtComplexData*                    qtComplexDataValue;

  QtIterator::QtONCStreamList*      qtONCStreamListValue;
  QtComplexData::QtScalarDataList*  qtScalarDataListValue;
  QtNode::QtOperationList*          qtOperationListValue;
  QtNode::QtOperationList*          qtOperationListValue2;

  QtUpdateSpecElement               qtUpdateSpecElement;

  vector< QtGeometryOp* >*          multiPoly;

  Ops::OpType                       operationValue;
  int                               dummyValue;
  int                               resampleAlgValue;
  
  struct {
      QtScalarData* low;
      QtScalarData* high; 
  } qtNullvalueInterval;
  
  QtNullvaluesOp::QtNullvaluesList* qtNullvalueIntervalList;

  
  struct {
        const char*      value;
        ParseInfo* info;
  }	castTypes;

  struct {
  	int indexType;
  	ParseInfo *info;
  }	indexType;

  struct {
  	int tilingType;
    QtOperation* tileCfg;
    int tileSize;
    int borderThreshold;
    float interestThreshold;
    QtNode::QtOperationList* bboxList;
    std::vector<r_Dir_Decompose>* dirDecomp;
  	ParseInfo *info;
  }	tilingType;

  struct {
	QtCollection* value;
	ParseInfo *info;
  } qtCollection;

}

%token <identifierToken> Identifier TypeName
%token <booleanToken>    BooleanLit
%token <characterToken>  CharacterLit
%token <integerToken>    IntegerLit
%token <floatToken>      FloatLit
%token <stringToken>     StringLit
%token <typeToken>       TUNSIG TBOOL TOCTET TCHAR TSHORT TUSHORT TLONG TULONG TFLOAT TDOUBLE TCOMPLEX1 TCOMPLEX2 TCINT16 TCINT32
%token <commandToken>    SELECT FROM WHERE AS RESTRICT TO EXTEND BY PROJECT AT DIMENSION ALL SOME
                         COUNTCELLS ADDCELLS AVGCELLS MINCELLS MAXCELLS VAR_POP VAR_SAMP STDDEV_POP STDDEV_SAMP SDOM OVER USING LO HI UPDATE
                         SET ASSIGN MARRAY MDARRAY CONDENSE IN DOT COMMA IS NOT AND OR XOR PLUS MINUS MAX_BINARY MIN_BINARY MULT
                         DIV INTDIV MOD EQUAL LESS GREATER LESSEQUAL GREATEREQUAL NOTEQUAL COLON SEMICOLON LEPAR
                         REPAR LRPAR RRPAR LCPAR RCPAR INSERT INTO VALUES DELETE DROP CREATE COLLECTION TYPE
                         MDDPARAM OID SHIFT CLIP CURTAIN CORRIDOR POLYGON LINESTRING MULTIPOLYGON MULTILINESTRING RANGE SCALE SQRT ABS EXP 
                         LOGFN LN SIN COS TAN SINH COSH TANH ARCSIN ASIN SUBSPACE DISCRETE COORDINATES
                         ARCCOS ACOS ARCTAN ATAN POW POWER OVERLAY BIT UNKNOWN FASTSCALE MEMBERS ADD ALTER LIST PROJECTION
			 INDEX RC_INDEX TC_INDEX A_INDEX D_INDEX RD_INDEX RPT_INDEX RRPT_INDEX IT_INDEX AUTO
			 TILING ALIGNED REGULAR DIRECTIONAL NULLKEY
			 WITH SUBTILING AREA OF INTEREST STATISTIC TILE SIZE BORDER THRESHOLD
			 STRCT COMPLEX RE IM TIFF BMP HDF NETCDF CSV JPEG PNG VFF TOR DEM INV_TIFF INV_BMP INV_HDF INV_NETCDF
			 INV_JPEG INV_PNG INV_VFF INV_CSV INV_TOR INV_DEM INV_GRIB ENCODE DECODE CONCAT ALONG DBINFO
                         CASE WHEN THEN ELSE END COMMIT RAS_VERSION P_REGROUP P_REGROUP_AND_SUBTILING P_NO_LIMIT
/* resampling algorithms */
                         RA_NEAR RA_BILINEAR RA_CUBIC RA_CUBIC_SPLINE RA_LANCZOS RA_AVERAGE RA_MODE RA_MED RA_QFIRST RA_QTHIRD

%left LINESTRING POLYGON PROJECTION CURTAIN CORRIDOR MULTIPOLYGON MULTILINESTRING DISCRETE
%left COLON VALUES USING WHERE
%left OVERLAY
%left CONCAT
%left OR XOR
%left AND
%left NOT
%left IS
%left EQUAL LESS GREATER LESSEQUAL GREATEREQUAL NOTEQUAL
%left PLUS MINUS
%left MAX_BINARY MIN_BINARY
%left MULT DIV INTDIV MOD
%left UNARYOP BIT
%left DOT LEPAR SDOM
%left COMMA
%left RRPAR

// The LEPAR precedence is for the trimming operation. Context dependent
// precedence would be better in that case but it did not work. 

%type <qtUpdateSpecElement>   updateSpec
%type <qtMDDAccessValue>      iteratedCollection
%type <qtONCStreamListValue>  collectionList
%type <qtUnaryOperationValue> reduceIdent structSelection trimExp 
%type <qtOperationValue>      mddExp inductionExp generalExp resultList reduceExp  functionExp spatialOp 
                              integerExp mintervalExp nullvaluesList nullvaluesExp addNullvaluesExp namedMintervalExp intervalExp namedIntervalExp 
                              condenseExp variable mddConfiguration mintervalList  concatExp rangeConstructorExp
                              caseExp typeAttribute projectExp
%type <resampleAlgValue>      resampleAlg
%type <tilingType>            tilingAttributes  tileTypes tileCfg statisticParameters tilingSize
                              borderCfg interestThreshold dirdecompArray dirdecomp dirdecompvals intArray
%type <indexType>             indexingAttributes indexTypes
// %type <stgType>            storageAttributes storageTypes comp compType zLibCfg rLECfg waveTypes
%type <qtOperationListValue>  spatialOpList namedSpatialOpList spatialOpList2 namedSpatialOpList2 bboxList mddList caseCond caseCondList 
                              caseEnd generalExpList typeAttributeList parentheticalLinestring vertex vertexList positiveGenusPolygon polygonVector
//%type <multiPoly>             polygonVector
%type <qtNullvalueIntervalList> nullvalueIntervalList
%type <qtNullvalueInterval>     nullvalueIntervalExp
%type <integerToken>          intLitExp
%type <operationValue>        condenseOpLit 
%type <castTypes>             castType
%type <dummyValue>            qlfile query selectExp createExp insertExp deleteExp updateExp dropExp selectIntoExp commitExp tileSizeControl 
                              createType dropType alterExp

%type <qtCollection>          namedCollection

%type <identifierToken>       collectionIterator typeName attributeIdent createTypeName
                              marrayVariable condenseVariable hostName

// literal data
%type <qtDataValue>           generalLit mddLit oidLit
%type <qtScalarDataValue>     scalarLit
%type <qtAtomicDataValue>     atomicLit
%type <qtComplexDataValue>    complexLit
%type <qtScalarDataListValue> scalarLitList dimensionLitList
%type <floatToken>            floatLitExp

// vectorized data

// marray2 with multiple intervals
%type <mddIntervalListType>   ivList
%type <mddIntervalType>	      iv marray_head
%parse-param {void * YYPARSE_PARAM}
%%  // rules section
/*--------------------------------------------------------------------
 *				Grammar starts here 
 *--------------------------------------------------------------------
 */

qlfile: query 
	{ 
	  // clear all symbols in table at the end of parsing
	  QueryTree::symtab.wipe(); 
	};
query: createExp
     | dropExp
     | selectExp
     | selectIntoExp
     | updateExp
     | insertExp
     | deleteExp
     | commitExp
     | createType
     | dropType
     | alterExp
     ;

commitExp: COMMIT
{
	  try {
	    accessControl.wantToWrite();
	  }
	    catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $1.info->getToken().c_str(),
                                        $1.info->getLineNo(), $1.info->getColumnNo() );
	    FREESTACK($1)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }

	  // create the command node
	  QtCommand* commandNode = new QtCommand( QtCommand::QT_COMMIT, QtCollection("dummy") );
	  commandNode->setParseInfo( *($1.info) );
	  
	  // set insert node  as root of the Query Tree
	  parseQueryTree->setRoot( commandNode );
	  
	  FREESTACK($1)
}

createExp: CREATE COLLECTION namedCollection typeName
	{
	  try {
	    accessControl.wantToWrite();
	  }
	    catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                        $2.info->getLineNo(), $2.info->getColumnNo() );
	    FREESTACK($1)
	    FREESTACK($2)
		delete $3.value;
	    FREESTACK($4)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }

	  // create the command node
	  QtCommand* commandNode = new QtCommand( QtCommand::QT_CREATE_COLLECTION, *($3.value), $4.value );
	  commandNode->setParseInfo( *($1.info) );
	  
	  // set insert node  as root of the Query Tree
	  parseQueryTree->setRoot( commandNode );
	  
	  FREESTACK($1)
	  FREESTACK($2)
	  delete $3.value;
	  FREESTACK($4)
	};

dropExp: DROP COLLECTION namedCollection
	{
	  try {
	    accessControl.wantToWrite();
  	  }
	  catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                        $2.info->getLineNo(), $2.info->getColumnNo() );
            FREESTACK($1)
            FREESTACK($2)
		    delete $3.value;
	    QueryTree::symtab.wipe();
            YYABORT;
	  }

	  // create the command node
	  QtCommand* commandNode = new QtCommand( QtCommand::QT_DROP_COLLECTION, *($3.value) );
	  commandNode->setParseInfo( *($1.info) );
	  
	  // set insert node  as root of the Query Tree
	  parseQueryTree->setRoot( commandNode );
	  	  
	  FREESTACK($1)
	  FREESTACK($2)
		delete $3.value;;
	};

selectIntoExp:
	SELECT generalExp INTO namedCollection FROM collectionList WHERE generalExp     
	{
	  try {
	    accessControl.wantToWrite();
  	  }
	  catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError )
                delete parseError;
            parseError = new ParseInfo( 803, $1.info->getToken().c_str(),
                                        $1.info->getLineNo(), $1.info->getColumnNo() );
            FREESTACK($1)
            FREESTACK($3)
            FREESTACK($5)
		    delete $4.value;
            FREESTACK($7)
            QueryTree::symtab.wipe();
            YYABORT;
	  }

	  for( QtIterator::QtONCStreamList::iterator iter=$6->begin(); iter!=$6->end(); iter++ )
	    parseQueryTree->removeDynamicObject( *iter );
	    
	  // create a JoinIterator
	  QtJoinIterator* ji = new QtJoinIterator();
	  ji->setStreamInputs( $6 );
	  parseQueryTree->removeDynamicObject( $6 );
	  
	  // create a QtONCStreamList and add the Join Iterator
	  QtIterator::QtONCStreamList* inputListS = new QtIterator::QtONCStreamList(1);
	  (*inputListS)[0] = ji;
	  
	  // create a SelectionIterator
	  QtSelectionIterator* si = new QtSelectionIterator();
	  si->setStreamInputs( inputListS );
	  si->setParseInfo( *($7.info) );
	  si->setConditionTree( $8 );
	  parseQueryTree->removeDynamicObject( $8 );
	  
	  // create a QtONCStreamList and add the Selection Iterator
	  QtIterator::QtONCStreamList* inputListO = new QtIterator::QtONCStreamList(1);
	  (*inputListO)[0] = si;
	  
	  // create a OperationIterator and set its inputs
	  QtOperationIterator* oi = new QtOperationIterator();
	  oi->setStreamInputs( inputListO );
	  oi->setParseInfo( *($1.info) );
	  oi->setOperationTree( $2 );
	  parseQueryTree->removeDynamicObject( $2 );

	  // And finally create a QtCommand that creates the final collection
	  QtCommand* commandNode = new QtCommand( QtCommand::QT_CREATE_COLLECTION_FROM_QUERY_RESULT, *($4.value), oi );

	  commandNode->setParseInfo( *($3.info) );
	  
	  // set QtCommand create node as root of the Query Tree
	  parseQueryTree->setRoot( commandNode );
	  
	  FREESTACK($1)
	  FREESTACK($3)
      delete $4.value;
	  FREESTACK($5)
	  FREESTACK($7)
	}
	| 
	SELECT generalExp INTO namedCollection FROM collectionList
	{
	  try {
	    accessControl.wantToWrite();
  	  }
	  catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError )
                delete parseError;
            parseError = new ParseInfo( 803, $1.info->getToken().c_str(),
                                        $1.info->getLineNo(), $1.info->getColumnNo() );
            FREESTACK($1)
            FREESTACK($3)
            delete $4.value;
            FREESTACK($5)
            QueryTree::symtab.wipe();
            YYABORT;
	  }
	
	  for( QtIterator::QtONCStreamList::iterator iter=$6->begin(); iter!=$6->end(); iter++ )
	    parseQueryTree->removeDynamicObject( *iter );
	  
	  // create a JoinIterator
	  QtJoinIterator* ji = new QtJoinIterator();
	  ji->setStreamInputs( $6 );
	  parseQueryTree->removeDynamicObject( $6 );
	  
	  // create a QtONCStreamList and add the Join Iterator
	  QtIterator::QtONCStreamList* inputList = new QtIterator::QtONCStreamList(1);
	  (*inputList)[0] = ji;
	  
	  // create a OperationIterator and set its inputs
	  QtOperationIterator* oi = new QtOperationIterator();
	  oi->setStreamInputs( inputList );
	  oi->setParseInfo( *($1.info) );
	  oi->setOperationTree( $2 );
	  parseQueryTree->removeDynamicObject( $2 );
	  
	  // And finally create a QtCommand that creates the final collection
	  QtCommand* commandNode = new QtCommand( QtCommand::QT_CREATE_COLLECTION_FROM_QUERY_RESULT, *($4.value), oi );
	  commandNode->setParseInfo( *($3.info) );
	  
	  // set QtCommand create node  as root of the Query Tree
	  parseQueryTree->setRoot( commandNode );
	  
	  FREESTACK($1)
	  FREESTACK($3)
      delete $4.value;
	  FREESTACK($5)
	};

selectExp: SELECT resultList FROM collectionList WHERE generalExp     
	{
	  try {
	    accessControl.wantToRead();
  	  }
	  catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $1.info->getToken().c_str(),
                                        $1.info->getLineNo(), $1.info->getColumnNo() );
            FREESTACK($1)
            FREESTACK($3)
	    FREESTACK($5)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }

	  for( QtIterator::QtONCStreamList::iterator iter=$4->begin(); iter!=$4->end(); iter++ )
	    parseQueryTree->removeDynamicObject( *iter );
	    
	  // create a JoinIterator
	  QtJoinIterator* ji = new QtJoinIterator();
	  ji->setStreamInputs( $4 );
	  parseQueryTree->removeDynamicObject( $4 );
	  
	  // create a QtONCStreamList and add the Join Iterator
	  QtIterator::QtONCStreamList* inputListS = new QtIterator::QtONCStreamList(1);
	  (*inputListS)[0] = ji;
	  
	  // create a SelectionIterator
	  QtSelectionIterator* si = new QtSelectionIterator();
	  si->setStreamInputs( inputListS );
	  si->setParseInfo( *($5.info) );
	  si->setConditionTree( $6 );
	  parseQueryTree->removeDynamicObject( $6 );
	  
	  // create a QtONCStreamList and add the Selection Iterator
	  QtIterator::QtONCStreamList* inputListO = new QtIterator::QtONCStreamList(1);
	  (*inputListO)[0] = si;
	  
	  // create an OperationIterator and set its inputs
	  QtOperationIterator* oi = new QtOperationIterator();
	  oi->setStreamInputs( inputListO );
	  oi->setParseInfo( *($1.info) );
	  oi->setOperationTree( $2 );
	  parseQueryTree->removeDynamicObject( $2 );
	  
	  // set the OperationIterator as root of the Query Tree
	  parseQueryTree->setRoot( oi );
	  
	  FREESTACK($1)
	  FREESTACK($3)
	  FREESTACK($5)
	}
	| SELECT resultList FROM collectionList 
	{
	  try {
	    accessControl.wantToRead();
  	  }
	  catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $1.info->getToken().c_str(),
                                        $1.info->getLineNo(), $1.info->getColumnNo() );
            FREESTACK($1)
            FREESTACK($3)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }
	
	  for( QtIterator::QtONCStreamList::iterator iter=$4->begin(); iter!=$4->end(); iter++ )
	    parseQueryTree->removeDynamicObject( *iter );
	  
	  // create a JoinIterator
	  QtJoinIterator* ji = new QtJoinIterator();
	  ji->setStreamInputs( $4 );
	  parseQueryTree->removeDynamicObject( $4 );
	  
	  // create a QtONCStreamList and add the Join Iterator
	  QtIterator::QtONCStreamList* inputList = new QtIterator::QtONCStreamList(1);
	  (*inputList)[0] = ji;
	  
	  // create a OperationIterator and set its inputs
	  QtOperationIterator* oi = new QtOperationIterator();
	  oi->setStreamInputs( inputList );
	  oi->setParseInfo( *($1.info) );
	  oi->setOperationTree( $2 );
	  parseQueryTree->removeDynamicObject( $2 );

	  // set the OperationIterator as root of the Query Tree
	  parseQueryTree->setRoot( oi );
	  
	  FREESTACK($1)
	  FREESTACK($3)
	}
	| SELECT resultList
	{
	  try {
	    accessControl.wantToRead();
	  }
	  catch(...) {
	    // save the parse error info and stop the parser
	    if ( parseError ) delete parseError;
	    parseError = new ParseInfo( 803, $1.info->getToken().c_str(),
	                                $1.info->getLineNo(), $1.info->getColumnNo() );
	    FREESTACK($1)
	    QueryTree::symtab.wipe();
	    YYABORT;
	  }

	  QtIterator::QtONCStreamList* inputList = new QtIterator::QtONCStreamList(1);
	  (*inputList)[0] = new QtEmptyStream();

	  // create a OperationIterator and set its inputs
	  QtOperationIterator* oi = new QtOperationIterator();
	  oi->setStreamInputs( inputList );
	  oi->setParseInfo( *($1.info) );
	  oi->setOperationTree( $2 );
	  parseQueryTree->removeDynamicObject( $2 );

	  // set the OperationIterator as root of the Query Tree
	  parseQueryTree->setRoot( oi );

	  FREESTACK($1)
	}
	| SELECT RAS_VERSION LRPAR RRPAR
	{
	  parseQueryTree->setRoot( NULL );
	  parseQueryTree->setInfoType( QueryTree::QT_INFO_VERSION );
	  FREESTACK($3)
	  FREESTACK($4)
    };
	
updateExp:
        UPDATE iteratedCollection SET updateSpec ASSIGN generalExp WHERE generalExp         
        {
          try {
            accessControl.wantToWrite();
          }
          catch(...) {
            // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $1.info->getToken().c_str(),
                                        $1.info->getLineNo(), $1.info->getColumnNo() );
            FREESTACK($1)
            FREESTACK($3)
            FREESTACK($5)
            FREESTACK($7)
            QueryTree::symtab.wipe();
            YYABORT;
          }

          // create a QtONCStreamList and add the QtAccess object of collection Spec
          QtIterator::QtONCStreamList* streamList = new QtIterator::QtONCStreamList(1);
          (*streamList)[0] = $2;
          parseQueryTree->removeDynamicObject( $2 );

          // create a SelectionIterator
          QtSelectionIterator* si = new QtSelectionIterator();
          si->setStreamInputs( streamList );
          si->setConditionTree( $8 );
          si->setParseInfo( *($7.info) );
          parseQueryTree->removeDynamicObject( $8 );

          // create an update node
          QtUpdate* update = new QtUpdate( $4.iterator, $4.domain, $6 );
          update->setStreamInput( si );
          update->setParseInfo( *($1.info) );
          parseQueryTree->removeDynamicObject( $4.iterator );
          parseQueryTree->removeDynamicObject( $4.domain );
          parseQueryTree->removeDynamicObject( $6 );

          // set the update node  as root of the Query Tree
          parseQueryTree->setRoot( update );

          FREESTACK($1)
          FREESTACK($3)
          FREESTACK($5)
          FREESTACK($7)
        }
        | UPDATE iteratedCollection SET updateSpec ASSIGN generalExp
        {
          try {
            accessControl.wantToWrite();
          }
          catch(...) {
            // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $1.info->getToken().c_str(),
                                        $1.info->getLineNo(), $1.info->getColumnNo() );
            FREESTACK($1)
            FREESTACK($3)
            FREESTACK($5)
            QueryTree::symtab.wipe();
            YYABORT;
          }

          // create an update node
          QtUpdate* update = new QtUpdate( $4.iterator, $4.domain, $6 );
          update->setStreamInput( $2 );
          update->setParseInfo( *($1.info) );
          parseQueryTree->removeDynamicObject( $2 );
          parseQueryTree->removeDynamicObject( $4.iterator );
          parseQueryTree->removeDynamicObject( $4.domain );
          parseQueryTree->removeDynamicObject( $6 );

          // set the update node  as root of the Query Tree
          parseQueryTree->setRoot( update );

          FREESTACK($1)
          FREESTACK($3)
          FREESTACK($5)

        }
        | UPDATE iteratedCollection SET updateSpec ASSIGN generalExp FROM collectionList
        {
          // write access
          try {
            accessControl.wantToWrite();
          }
          catch(...) {
            // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $1.info->getToken().c_str(),
                                        $1.info->getLineNo(), $1.info->getColumnNo() );
            FREESTACK($1)
            FREESTACK($3)
            FREESTACK($5)
            FREESTACK($7)
            QueryTree::symtab.wipe();
            YYABORT;
          }
  
          //is this needed?
          //append the update target to the collection list
          $8->push_back($2);
          
          //create a join for the collectionList
          for( QtIterator::QtONCStreamList::iterator iter=$8->begin(); iter!=$8->end(); iter++ )
	    parseQueryTree->removeDynamicObject( *iter );
  
          // create a JoinIterator
          QtJoinIterator* ji = new QtJoinIterator();
          ji->setStreamInputs( $8 );
          parseQueryTree->removeDynamicObject( $8 );
         
          // create an update node
          QtUpdate* update = new QtUpdate( $4.iterator, $4.domain, $6 );
          // stream all inputs to the update request
          update->setStreamInput( ji );
          update->setParseInfo( *($1.info) );
          //parseQueryTree->removeDynamicObject( $2 );
          parseQueryTree->removeDynamicObject( $4.iterator );
          parseQueryTree->removeDynamicObject( $4.domain );
          parseQueryTree->removeDynamicObject( $6 );

          // set the update node  as root of the Query Tree
          parseQueryTree->setRoot( update );

          FREESTACK($1)
          FREESTACK($3)
          FREESTACK($5)
          FREESTACK($7)
        }
        | UPDATE iteratedCollection SET updateSpec ASSIGN generalExp FROM collectionList WHERE generalExp
        {
          // write access
          try {
            accessControl.wantToWrite();
          }
          catch(...) {
            // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $1.info->getToken().c_str(),
                                        $1.info->getLineNo(), $1.info->getColumnNo() );
            FREESTACK($1)
            FREESTACK($3)
            FREESTACK($5)
            FREESTACK($7)
            FREESTACK($9)                    
            QueryTree::symtab.wipe();
            YYABORT;
          }

          $8->push_back($2);
          
          //create a join for the collectionList
          for( QtIterator::QtONCStreamList::iterator iter=$8->begin(); iter!=$8->end(); iter++ )
	    parseQueryTree->removeDynamicObject( *iter );
  
          // create a JoinIterator
          QtJoinIterator* ji = new QtJoinIterator();
          ji->setStreamInputs( $8 );
          parseQueryTree->removeDynamicObject( $8 );

	  // create a QtONCStreamList and add the Join Iterator
	  QtIterator::QtONCStreamList* inputListS = new QtIterator::QtONCStreamList(1);
	  (*inputListS)[0] = ji;          
          
          // create a SelectionIterator
          QtSelectionIterator* si = new QtSelectionIterator();
          si->setStreamInputs( inputListS );
          si->setConditionTree( $10 );
          si->setParseInfo( *($9.info) );
          parseQueryTree->removeDynamicObject( $10 );          

          // create an update node
          QtUpdate* update = new QtUpdate( $4.iterator, $4.domain, $6 );
          // stream all inputs to the update request
          update->setStreamInput( si );
          update->setParseInfo( *($1.info) );
          //parseQueryTree->removeDynamicObject( $2 );
          parseQueryTree->removeDynamicObject( $4.iterator );
          parseQueryTree->removeDynamicObject( $4.domain );
          parseQueryTree->removeDynamicObject( $6 );

          // set the update node  as root of the Query Tree
          parseQueryTree->setRoot( update );

          FREESTACK($1)
          FREESTACK($3)
          FREESTACK($5)
          FREESTACK($7)
          FREESTACK($9)
        };

alterExp: ALTER COLLECTION namedCollection SET TYPE typeName
    {
      try {
        accessControl.wantToWrite();
      }
      catch(...) {
        // save the parse error info and stop the parser
        if ( parseError ) delete parseError;
        parseError = new ParseInfo( 803, $1.info->getToken().c_str(),
                                    $1.info->getLineNo(), $1.info->getColumnNo() );
        FREESTACK($1)
        FREESTACK($2)
        delete $3.value;
        FREESTACK($4)
        FREESTACK($5)
        QueryTree::symtab.wipe();
        YYABORT;
      }

      // create the command node
      QtCommand* commandNode = new QtCommand( QtCommand::QT_ALTER_COLLECTION, *($3.value), $6.value );
      commandNode->setParseInfo( *($1.info) );
      
      // set insert node  as root of the Query Tree
      parseQueryTree->setRoot( commandNode );
      
      FREESTACK($1)
      FREESTACK($2)
      delete $3.value;
      FREESTACK($4)
      FREESTACK($5)
    };
 
insertExp: INSERT INTO namedCollection VALUES generalExp
	{
	  try {
	    accessControl.wantToWrite();
  	  }
	  catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                        $2.info->getLineNo(), $2.info->getColumnNo() );
	    FREESTACK($1)
            FREESTACK($2)
        delete $3.value;
	    FREESTACK($4)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }

	  // create an update node
	  QtInsert* insert = new QtInsert( *($3.value), $5 );
	  insert->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $5 );
	  
	  // set insert node  as root of the Query Tree
	  parseQueryTree->setRoot( insert );
	  
	  FREESTACK($1)
	  FREESTACK($2)
      delete $3.value;
	  FREESTACK($4)
	}
	|
	INSERT INTO namedCollection VALUES generalExp mddConfiguration
	{
	  try {
	    accessControl.wantToWrite();
  	  }
	  catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                        $2.info->getLineNo(), $2.info->getColumnNo() );
	    FREESTACK($1)
	    FREESTACK($2)
        delete $3.value;
	    FREESTACK($4)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }

	  // create an update node
	  QtInsert* insert = new QtInsert( *($3.value), $5 ,$6);
	  insert->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $5 );

	  // set insert node  as root of the Query Tree
	  parseQueryTree->setRoot( insert );

	  FREESTACK($1)
	  FREESTACK($2)
      delete $3.value;
	  FREESTACK($4)
	}

/*{
	  try {
	    accessControl.wantToWrite();
  	  }
	  catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                        $2.info->getLineNo(), $2.info->getColumnNo() );
	    FREESTACK($1)
	    FREESTACK($2)
	    FREESTACK($3)
	    
	    QueryTree::symtab.wipe();
            YYABORT;
	  }
	  //Creating a new collection
//	  QtCommand* commandNode = new QtCommand( QtCommand::QT_CREATE_COLLECTION, $3.value, "GreySet" );
//	  commandNode->setParseInfo( *($1.info) );
	  
	  // create an update node
	  QtInsert* insert = new QtInsert( $3.value, $5, $6);
	  insert->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $6 );
	  
	  // set insert node  as root of the Query Tree
	  parseQueryTree->setRoot( insert );
	  
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($3)
	  
	}*/
;
 
deleteExp: DELETE FROM iteratedCollection WHERE generalExp
	{
	  try {
	    accessControl.wantToWrite();
  	  }
	  catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                        $2.info->getLineNo(), $2.info->getColumnNo() );
	    FREESTACK($1)
	    FREESTACK($2)
	    FREESTACK($4)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }

	  // create a QtONCStreamList and add the QtAccess object of collection Spec
	  QtIterator::QtONCStreamList* streamList = new QtIterator::QtONCStreamList(1);
	  (*streamList)[0] = $3;
	  parseQueryTree->removeDynamicObject( $3 );
	  
	  // create a SelectionIterator
	  QtSelectionIterator* si = new QtSelectionIterator();
	  si->setStreamInputs( streamList );
	  si->setConditionTree( $5 );
	  si->setParseInfo( *($4.info) );
	  parseQueryTree->removeDynamicObject( $5 );
	  
	  // create delete node
	  QtDelete* delNode = new QtDelete();
	  delNode->setStreamInput( si );
	  delNode->setParseInfo( *($1.info) );
	  
	  // set insert node  as root of the Query Tree
	  parseQueryTree->setRoot( delNode );
	  
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
/* // doesn't work yet, somewhere later the server crashes -- PB 2006-jan-03
 * uncommented and fixed, ticket 336 -- DM 2013-jul-18
 */
	| DELETE FROM iteratedCollection
	{
	  try {
	    accessControl.wantToWrite();
  	  }
	  catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                        $2.info->getLineNo(), $2.info->getColumnNo() );
	    FREESTACK($1)
	    FREESTACK($2)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }

	  // create a QtONCStreamList and add the QtAccess object of collection Spec
	  QtIterator::QtONCStreamList* streamList = new QtIterator::QtONCStreamList(1);
	  (*streamList)[0] = $3;
	  parseQueryTree->removeDynamicObject( $3 );
	  
	  // create a SelectionIterator
	  QtSelectionIterator* si = new QtSelectionIterator();
	  si->setStreamInputs( streamList );

	  // create delete node
	  QtDelete* delNode = new QtDelete();
	  delNode->setStreamInput( $3 );
	  delNode->setParseInfo( *($1.info) );
	  
	  // set insert node  as root of the Query Tree
	  parseQueryTree->setRoot( delNode );
	  
	  FREESTACK($1)
	  FREESTACK($2)
	}
	;

createType: CREATE TYPE createTypeName AS LRPAR typeAttributeList RRPAR
            {
                try
                {
                  accessControl.wantToWrite();
                }
                catch(...)
                {
                  // save the parse error info and stop the parser
                  if ( parseError )
                  {
                    delete parseError;
                  }
                  parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                              $2.info->getLineNo(), $2.info->getColumnNo() );
                  FREESTACK($1)
                  FREESTACK($2)
                  FREESTACK($3)
                  FREESTACK($4)
                  FREESTACK($5)
                  FREESTACK($7)

                  QueryTree::symtab.wipe();
                  YYABORT;
                }

                QtCreateCellType* cellTypeNode = new QtCreateCellType($3.value, $6);
                cellTypeNode->setParseInfo( *($1.info) );

                parseQueryTree->setRoot( cellTypeNode );

                FREESTACK($1);
                FREESTACK($2);
                FREESTACK($4);
                FREESTACK($5);
                FREESTACK($7);
            }
          | CREATE TYPE createTypeName AS createTypeName MDARRAY namedMintervalExp
            {
                try
                {
                  accessControl.wantToWrite();
                }
                catch(...)
                {
                  // save the parse error info and stop the parser
                  if ( parseError )
                  {
                    delete parseError;
                  }
                  parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                              $2.info->getLineNo(), $2.info->getColumnNo() );
                  FREESTACK($1)
                  FREESTACK($2)
                  FREESTACK($3)
                  FREESTACK($4)
                  FREESTACK($5)
                  FREESTACK($6)
                  QueryTree::symtab.wipe();
                  YYABORT;
                }

                QtCreateMarrayType* mddTypeNode = new QtCreateMarrayType($3.value, $5.value, $7);
                mddTypeNode->setParseInfo( *($1.info));

                parseQueryTree->setRoot( mddTypeNode );

                FREESTACK($1);
                FREESTACK($2);
                FREESTACK($4);
                FREESTACK($6);
            }
          | CREATE TYPE createTypeName AS LRPAR typeAttributeList RRPAR MDARRAY namedMintervalExp
            {
                try
                {
                  accessControl.wantToWrite();
                }
                  catch(...) {
                  // save the parse error info and stop the parser
                  if ( parseError )
                  {
                    delete parseError;
                  }

                  parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                              $2.info->getLineNo(), $2.info->getColumnNo() );
                  FREESTACK($1)
                  FREESTACK($2)
                  FREESTACK($3)
                  FREESTACK($4)
                  FREESTACK($5)
                  FREESTACK($7)
                  FREESTACK($8)
                  QueryTree::symtab.wipe();
                  YYABORT;
                }

                QtCreateMarrayType* mddTypeNode = new QtCreateMarrayType($3.value, $6, $9);
                mddTypeNode->setParseInfo( *($1.info));

                parseQueryTree->setRoot( mddTypeNode );

                FREESTACK($1);
                FREESTACK($2);
                FREESTACK($4);
                FREESTACK($5);
                FREESTACK($7);
                FREESTACK($8);
            }
          | CREATE TYPE createTypeName AS SET LRPAR createTypeName RRPAR
            {
                try
                {
                  accessControl.wantToWrite();
                }
                catch(...) {
                  // save the parse error info and stop the parser
                  if ( parseError )
                  {
                    delete parseError;
                  }
                  parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                              $2.info->getLineNo(), $2.info->getColumnNo() );
                  FREESTACK($1)
                  FREESTACK($2)
                  FREESTACK($3)
                  FREESTACK($4)
                  FREESTACK($5)
                  FREESTACK($6)
                  FREESTACK($7)
                  FREESTACK($8)
                  QueryTree::symtab.wipe();
                  YYABORT;
                }

                QtCreateSetType* setTypeNode = new QtCreateSetType($3.value, $7.value);
                setTypeNode->setParseInfo( *($1.info) );

                parseQueryTree->setRoot( setTypeNode);
                FREESTACK($1)
                FREESTACK($2)
                FREESTACK($4)
                FREESTACK($5)
                FREESTACK($6)
                FREESTACK($8)
            }
          | CREATE TYPE createTypeName AS SET LRPAR createTypeName nullvaluesExp RRPAR
            {
                try
                {
                    accessControl.wantToWrite();
                }
                catch(...)
                {
                  // save the parse error info and stop the parser
                  if ( parseError )
                  {
                    delete parseError;
                  }
                  parseError = new ParseInfo( 803, $2.info->getToken().c_str(),
                                              $2.info->getLineNo(), $2.info->getColumnNo() );
                  FREESTACK($1)
                  FREESTACK($2)
                  FREESTACK($4)
                  FREESTACK($5)
                  FREESTACK($6)
                  FREESTACK($9)
                  QueryTree::symtab.wipe();
                  YYABORT;
                }

                QtCreateSetType* setTypeNode = new QtCreateSetType($3.value, $7.value, $8);
                setTypeNode->setParseInfo( *($1.info) );

                parseQueryTree->setRoot( setTypeNode);
                FREESTACK($1)
                FREESTACK($2)
                FREESTACK($4)
                FREESTACK($5)
                FREESTACK($6)
                FREESTACK($9)
            }
            ;

typeAttributeList: typeAttributeList COMMA typeAttribute
            {
                $1->push_back( $3 );
                $$ = $1;
                FREESTACK($2);
            }
            | typeAttribute
            {
                $$ = new QtNode::QtOperationList(1);
                (*$$)[0] = $1;
            }
            ;

typeAttribute: Identifier createTypeName
            {
                $$ = new QtCellTypeAttributes($1.value, $2.value);
                parseQueryTree->addDynamicObject( $$ );
            }
            ;

dropType: DROP TYPE createTypeName
            {
                QtDropType* qtDropType = new QtDropType($3.value);
                qtDropType->setParseInfo( *($1.info));

                parseQueryTree->setRoot( qtDropType );

                FREESTACK($1);
                FREESTACK($2);
            }
            ;

createTypeName: Identifier
              | TypeName
              | castType
              ;

updateSpec: variable                 
	{
	  $$.iterator = $1;
	  $$.domain   = 0;
	}
	| variable mintervalExp
	{
	  $$.iterator = $1;
	  $$.domain   = $2;
	};

resultList: resultList COMMA generalExp	
	{
	  $$ = $3;
	  FREESTACK($2)
	}
	| generalExp
	{
	  $$ = $1;
	}

generalExp: 
	  caseExp                           { $$ = $1; } 
	| mddExp                            { $$ = $1; }
	| trimExp                           { $$ = $1; }
	| reduceExp                         { $$ = $1; }
	| inductionExp                      { $$ = $1; }
	| functionExp                       { $$ = $1; }
	| integerExp                        { $$ = $1; }
	| concatExp                         { $$ = $1; }
	| condenseExp                       { $$ = $1; }
	| variable                          { $$ = $1; }
	| mintervalExp                      { $$ = $1; }
	| intervalExp                       { $$ = $1; }
	| projectExp	                      { $$ = $1; }
	| generalLit
	{
	  $$ = new QtConst( $1 );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->addDynamicObject( $$ );
	}
	| rangeConstructorExp					      { $$ = $1; }
	| addNullvaluesExp  					      { $$ = $1; };

caseCond: WHEN generalExp THEN generalExp
        {
          $$ = new QtNode::QtOperationList(2);
          (*$$)[0] = $2;
          (*$$)[1] = $4;
          FREESTACK($1)
          FREESTACK($3)
        };

caseCondList: 
        {
          $$ = new QtNode::QtOperationList();
        }
        |
        caseCondList caseCond
        {
          $1->push_back( (*$2)[0] );
          $1->push_back( (*$2)[1] );
          delete $2;
          $$ = $1;
        }
        |
        caseCond
        {
          $$ = $1;
        };
        
caseEnd: ELSE generalExp END
        {
          $$ = new QtNode::QtOperationList(1);
          (*$$)[0] = $2;
          FREESTACK($1);
          FREESTACK($3);
        };
        
caseExp: CASE caseCondList caseEnd
        {
          QtNode::QtOperationList* result = new QtNode::QtOperationList();
          result->reserve($2->size() + $3->size());
          result->insert(result->end(), $2->begin(), $2->end());
          result->insert(result->end(), $3->begin(), $3->end()); 
          $$ = new QtCaseOp(result);
          QtNode::QtOperationList::iterator iter;
          
          for(iter = $2->begin(); iter != $2->end(); iter++){
              parseQueryTree->removeDynamicObject( *iter );
          }
          delete $2;
          
          for(iter = $3->begin(); iter != $3->end(); iter++){
              parseQueryTree->removeDynamicObject( *iter );
          }
          delete $3;
          
          $$->setParseInfo( *($1.info) );
          parseQueryTree->addDynamicObject( $$ );
          FREESTACK($1);
        }
        | CASE generalExp caseCondList caseEnd
        {
          QtNode::QtOperationList::iterator iter;
          QtNode::QtOperationList* result = new QtNode::QtOperationList();
          result->reserve($3->size() + $4->size());
          int pos = 0;
          for(iter = $3->begin(); iter != $3->end(); iter++){
              if( !(pos%2) ){
                  result->push_back(new QtCaseEquality($2, *iter));
              } else{
                  result->push_back(*iter);
              }
              pos++;
          }
          result->insert(result->end(), $4->begin(), $4->end()); 
          $$ = new QtSimpleCaseOp(result);
          
          parseQueryTree->removeDynamicObject($2);
          
          for(iter = $3->begin(); iter != $3->end(); iter++){
              parseQueryTree->removeDynamicObject( *iter );
          }
          
          for(iter = $4->begin(); iter != $4->end(); iter++){
              parseQueryTree->removeDynamicObject( *iter );
          }
          
          $$->setParseInfo( *($1.info) );
          parseQueryTree->addDynamicObject( $$ );
          FREESTACK($1);
        };

rangeConstructorExp: LCPAR generalExpList RCPAR
	{
	   if ($2->empty()) {
		  yyerror(mflag, "empty list for range constructor");
	   } else {
		  $$ = new QtRangeConstructor($2);
	   }
	  QtNode::QtOperationList::iterator iter;
	  for( iter=$2->begin(); iter!=$2->end(); ++iter )
	      parseQueryTree->removeDynamicObject( *iter );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($3)
	};

generalExpList: generalExpList COMMA generalExp
	{
	  $1->push_back( $3 );
	  $$ = $1;
	  FREESTACK($2)
	}
	| generalExp
	{
	  $$ = new QtNode::QtOperationList();
	  $$->push_back($1);
	};


concatExp: CONCAT mddList ALONG intLitExp
	{
	  if( $4.negative )
	    if( $4.svalue < 0 )
	      yyerror(mflag, "non negative integer expected");
	    else
	      $$ = new QtConcat( $2, (unsigned int)$4.svalue );
	  else
	    $$ = new QtConcat( $2, (unsigned int)$4.uvalue );
	  $$->setParseInfo( *($1.info) );
	  QtNode::QtOperationList::iterator iter;
	  for( iter=$2->begin(); iter!=$2->end(); ++iter )
	      parseQueryTree->removeDynamicObject( *iter );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($3)
	  FREESTACK($4)
	};	
	
mddList : mddList WITH generalExp
  {
	  $1->push_back( $3 );
	  $$ = $1;
	  FREESTACK($2)  
  }
  | generalExp WITH generalExp
  {
	  $$ = new QtNode::QtOperationList(2);
	  (*$$)[0] = $1;
	  (*$$)[1] = $3;  
	  FREESTACK($2)  
  };

integerExp: generalExp DOT LO
	{
	  $$ = new QtIntervalLoOp( $1 );
	  $$->setParseInfo( *($3.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	  FREESTACK($3)
	}
	| generalExp DOT HI
	{
	  $$ = new QtIntervalHiOp( $1 );
	  $$->setParseInfo( *($3.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	  FREESTACK($3)
	};

mintervalList: mintervalList mintervalExp
	{
	  //apply trim
	  QtDomainOperation *dop = new QtDomainOperation( $2 );
	  dop->setInput( $1 );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $2 );
	  parseQueryTree->addDynamicObject( dop );
	  $$ = dop;
	  if (mflag == MF_IN_CONTEXT)
	    parseQueryTree->addDomainObject( dop );
	}
	| mintervalExp
	{
	  //delegate to mintervalExp
	  $$=$1;
	};

mintervalExp: LEPAR spatialOpList REPAR           
	{
	  if (($2->size() > 1) || 
	      ($2->size() == 1 && (*$2)[0]->getNodeType() == QtNode::QT_INTERVALOP)) 
	  {
	    // Check if the list consists of integers only and
	    // create a point operation in this case.
	    int isPoint = 1;
	    QtNode::QtOperationList::iterator iter;
	    
	    for( iter=$2->begin(); iter!=$2->end(); ++iter )
	      isPoint &= (*iter)->getNodeType() != QtNode::QT_INTERVALOP;
	    
	    for( iter=$2->begin(); iter!=$2->end(); ++iter )
	      parseQueryTree->removeDynamicObject( *iter );
	      
	    if( isPoint )
	      $$ = new QtPointOp( $2 );
	    else
	      $$ = new QtMintervalOp( $2 );
	      
	    $$->setParseInfo( *($1.info) );
	    parseQueryTree->addDynamicObject( $$ );
	  }
	  else 
	  if ($2->size() == 1)
	  {    
	    // take the single element
	    $$ = (*$2)[0];
	    (*$2)[0] = 0;
	    delete $2;
	  }
	  else
	  {	    
	     RMInit::logOut << "MINTERVAL error: empty expression between brackets encountered!" << std::endl;
	     // save the parse error info and stop the parser
             if ( parseError ) 
	       delete parseError;
             // TODO: Define an error number for this one!!!! 312 is not correct.
             parseError = new ParseInfo( 312, $1.info->getToken().c_str(),
                                              $1.info->getLineNo(), 
				              $1.info->getColumnNo() );
	     FREESTACK($1)
	     FREESTACK($3)	  
	     QueryTree::symtab.wipe();
             YYABORT;
	  }
	  FREESTACK($1)
	  FREESTACK($3)
	}
	| SDOM LRPAR generalExp RRPAR
	{
	  $$ = new QtSDom( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	};

spatialOpList: 
	{
	  $$ = new QtNode::QtOperationList();
	}
	| spatialOpList2
	{
	  $$ = $1;
	};
 
spatialOpList2: spatialOpList2 COMMA spatialOp
	{
	  $1->push_back( $3 );
	  $$ = $1;
	  FREESTACK($2)
	}
	| spatialOp
	{
	  $$ = new QtNode::QtOperationList(1);
	  (*$$)[0] = $1;
	};

spatialOp: generalExp        { $$ = $1; };

//vector of polygons
polygonVector: LRPAR positiveGenusPolygon RRPAR
        {
            $$ = new QtNode::QtOperationList();
            QtGeometryOp* polyOp = new QtGeometryOp($2, QtGeometryData::QtGeometryType::GEOM_POLYGON);
            $$->emplace_back(polyOp);
            FREESTACK($1)
            FREESTACK($3)
        }
        | polygonVector COMMA LRPAR positiveGenusPolygon RRPAR
        {
            $$ = $1;
            QtGeometryOp* polyOp = new QtGeometryOp($4, QtGeometryData::QtGeometryType::GEOM_POLYGON);
            $$->emplace_back(polyOp);
            FREESTACK($2)
            FREESTACK($3)
            FREESTACK($5)
        };

//polygon with interior-defined polygons
positiveGenusPolygon: parentheticalLinestring
        {
            $$ = new QtNode::QtOperationList();
            QtMShapeOp* currentPoly = new QtMShapeOp( $1 );
            $$->emplace_back(currentPoly);
        }
        | positiveGenusPolygon COMMA parentheticalLinestring
        {
            $$ = $1;
            QtMShapeOp* currentPoly = new QtMShapeOp( $3 );
            $$->emplace_back(currentPoly);
            FREESTACK($2)
        };

// vertexList, separated by parends
// used primarily for quicker parsing of multipolygons and polygons with interiors
parentheticalLinestring: LRPAR vertexList RRPAR
        {
            $$ = $2;
            FREESTACK($1)
            FREESTACK($3)
        };

// vertexList -- can define a single polygon
vertexList: vertex
        {
            $$ = new QtNode::QtOperationList();
            QtPointOp* pt = new QtPointOp( $1 );
            $$->push_back ( pt );
            parseQueryTree->addDynamicObject( pt );
            for(auto iter = $$->begin(); iter != $$->end(); iter++)
                parseQueryTree->removeDynamicObject( dynamic_cast<QtPointOp*>(*iter) );

        }
        | vertexList COMMA vertex
        {
            QtPointOp* pt = new QtPointOp( $3 );
            parseQueryTree->addDynamicObject( pt );
            $1->push_back ( pt );
            $$ = $1;
            for(auto iter = $1->begin(); iter != $1->end(); iter++)
                parseQueryTree->removeDynamicObject( dynamic_cast<QtPointOp*>(*iter) );
            for(auto iter = $3->begin(); iter != $3->end(); iter++)
                parseQueryTree->removeDynamicObject( dynamic_cast<QtPointOp*>(*iter) );
            FREESTACK( $2 )
        };

// vertex defines a point in the sdom 
vertex: 
        vertex scalarLit
        {
            QtConst* thisScalar = new QtConst( $2 );
            $1->push_back( thisScalar );
            $$ = $1;
            parseQueryTree->removeDynamicObject( $2 );
        }
        | vertex LRPAR generalExp RRPAR 
        {
            //append the coordinate value to the front of the vector
            $1->push_back($3);
            $$ = $1;
            parseQueryTree->removeDynamicObject( $3 );
            FREESTACK($2)
            FREESTACK($4)
        }
        | scalarLit
        {
            $$ = new QtNode::QtOperationList();
            QtConst* thisScalar = new QtConst( $1 );
            $$->push_back( thisScalar );
            parseQueryTree->removeDynamicObject( $1 );
        }
        | LRPAR generalExp RRPAR
        { 
            $$ = new QtNode::QtOperationList();
            $$->push_back( $2 );
            parseQueryTree->removeDynamicObject( $2 );
            FREESTACK($1)
            FREESTACK($3)
        };

intervalExp: generalExp COLON generalExp
	{
          $$ = new QtIntervalOp( $1, $3 );
          $$->setParseInfo( *($2.info) );
          parseQueryTree->removeDynamicObject( $1 );
          parseQueryTree->removeDynamicObject( $3 );
          parseQueryTree->addDynamicObject( $$ );
          FREESTACK($2)
        }
        | MULT COLON generalExp
        {
          QtConst* const1 = new QtConst( new QtStringData("*") );
          const1->setParseInfo( *($1.info) );
          $$ = new QtIntervalOp( const1, $3 );
          $$->setParseInfo( *($2.info) );
          parseQueryTree->removeDynamicObject( $3 );
          parseQueryTree->addDynamicObject( $$ );
          FREESTACK($1)
          FREESTACK($2)
        }
        | generalExp COLON MULT
        {
          QtConst* const1 = new QtConst( new QtStringData("*") );
          const1->setParseInfo( *($3.info) );
          $$ = new QtIntervalOp( $1, const1 );
          $$->setParseInfo( *($2.info) );
          parseQueryTree->removeDynamicObject( $1 );
          parseQueryTree->addDynamicObject( $$ );
          FREESTACK($2)
          FREESTACK($3)
        }
        | MULT COLON MULT
        {
          QtConst* const1 = new QtConst( new QtStringData("*") );
          const1->setParseInfo( *($1.info) );
          QtConst* const2 = new QtConst( new QtStringData("*") );
          const2->setParseInfo( *($3.info) );
          $$ = new QtIntervalOp( const1, const2 );
          $$->setParseInfo( *($2.info) );
          parseQueryTree->addDynamicObject( $$ );
          FREESTACK($1)
          FREESTACK($2)
          FREESTACK($3)
        };                        

/* NEW TYPE BEGIN - TODO-GM: refactor*/
namedMintervalExp: LEPAR namedSpatialOpList REPAR
        {
          QtNode::QtOperationList::iterator iter;
          for( iter=$2->begin(); iter!=$2->end(); ++iter )
            parseQueryTree->removeDynamicObject( *iter );
          $$ = new QtMintervalOp( $2 );
          $$->setParseInfo( *($1.info) );
          parseQueryTree->addDynamicObject( $$ );
        };

namedSpatialOpList:
        {
            $$ = new QtNode::QtOperationList();
        }
        | namedSpatialOpList2
        {
            $$ = $1;
        };

namedSpatialOpList2: namedSpatialOpList2 COMMA namedIntervalExp
        {
          $1->push_back( $3 );
          $$ = $1;
          FREESTACK($2)
        }
        | namedIntervalExp
        {
          $$ = new QtNode::QtOperationList(1);
          (*$$)[0] = $1;
        };

namedIntervalExp: Identifier LRPAR generalExp COLON generalExp RRPAR
        {
          $$ = new QtIntervalOp( $3, $5 );
          $$->setParseInfo( *($2.info) );
          parseQueryTree->removeDynamicObject( $3 );
          parseQueryTree->removeDynamicObject( $5 );
          parseQueryTree->addDynamicObject( $$ );
          FREESTACK($2)
          FREESTACK($4)
          FREESTACK($6)
        }
        | Identifier LRPAR MULT COLON generalExp RRPAR
        {
          QtConst* const1 = new QtConst( new QtStringData("*") );
          const1->setParseInfo( *($3.info) );
          $$ = new QtIntervalOp( const1, $5 );
          $$->setParseInfo( *($2.info) );
          parseQueryTree->removeDynamicObject( $5 );
          parseQueryTree->addDynamicObject( $$ );

          FREESTACK($2)
          FREESTACK($3)
          FREESTACK($4)
          FREESTACK($6)
        }
        | Identifier LRPAR generalExp COLON MULT RRPAR
        {
          QtConst* const1 = new QtConst( new QtStringData("*") );
          const1->setParseInfo( *($5.info) );
          $$ = new QtIntervalOp( $3, const1 );
          $$->setParseInfo( *($1.info) );
          parseQueryTree->removeDynamicObject( $3 );
          parseQueryTree->addDynamicObject( $$ );
          FREESTACK($2)
          FREESTACK($4)
          FREESTACK($5)
          FREESTACK($6)
        }
        | Identifier LRPAR MULT COLON MULT RRPAR
        {
          QtConst* const1 = new QtConst( new QtStringData("*") );
          const1->setParseInfo( *($3.info) );
          QtConst* const2 = new QtConst( new QtStringData("*") );
          const2->setParseInfo( *($5.info) );
          $$ = new QtIntervalOp( const1, const2 );
          $$->setParseInfo( *($2.info) );
          parseQueryTree->addDynamicObject( $$ );
          FREESTACK($2)
          FREESTACK($3)
          FREESTACK($4)
          FREESTACK($5)
          FREESTACK($6)
        }
        | Identifier
        {
            QtConst* const1 = new QtConst( new QtStringData("*") );
            const1->setParseInfo( *($1.info) );
            QtConst* const2 = new QtConst( new QtStringData("*") );
            const2->setParseInfo( *($1.info) );
            $$ = new QtIntervalOp( const1, const2 );
            $$->setParseInfo( *($1.info) );
            parseQueryTree->addDynamicObject( $$ );
        };

/* NEW TYPE END - TODO-GM: refactor*/

condenseExp: CONDENSE condenseOpLit OVER condenseVariable IN generalExp WHERE generalExp USING generalExp
	{
	  $$ = new QtCondenseOp( $2, $4.value, $6, $10, $8 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $6 );
	  parseQueryTree->removeDynamicObject( $8 );
	  parseQueryTree->removeDynamicObject( $10 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($3)
	  FREESTACK($4)
	  FREESTACK($5)
	  FREESTACK($7)
	  FREESTACK($9)
	}
	| CONDENSE condenseOpLit OVER condenseVariable IN generalExp USING generalExp               
	{
	  $$ = new QtCondenseOp( $2, $4.value, $6, $8 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $6 );
	  parseQueryTree->removeDynamicObject( $8 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($3)
	  FREESTACK($4)
	  FREESTACK($5)
	  FREESTACK($7)
	};

condenseOpLit: PLUS
        {
            $$ = Ops::OP_PLUS;
            FREESTACK($1)
        }
        | MINUS
        { 
            $$ = Ops::OP_MINUS;
            FREESTACK($1)
        }
        | MULT
        {
            $$ = Ops::OP_MULT;
            FREESTACK($1)
        }
        | DIV
        {
            $$ = Ops::OP_DIV;
            FREESTACK($1)
        }
        | AND
        {
            $$ = Ops::OP_AND;
            FREESTACK($1)
        }
        | OR
        {
            $$ = Ops::OP_OR;
            FREESTACK($1)
        }
        | MAX_BINARY
        {
            $$ = Ops::OP_MAX_BINARY;
            FREESTACK($1)
        }
        | MIN_BINARY
        {
            $$ = Ops::OP_MIN_BINARY;
            FREESTACK($1)
	}; 

functionExp: OID LRPAR collectionIterator RRPAR    
    {
        QtVariable* var = new QtVariable( $3.value );
        var->setParseInfo( *($3.info) );
        $$ = new QtOId( var );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($3)
        FREESTACK($4)
    }
    | SHIFT LRPAR generalExp COMMA generalExp RRPAR
    {
        $$ = new QtShift( $3, $5 );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->removeDynamicObject( $5 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }

    | CLIP LRPAR generalExp COMMA SUBSPACE parentheticalLinestring RRPAR
    {
        QtNode::QtOperationList* concatOpList = new QtNode::QtOperationList();
        concatOpList->reserve(2);

        QtMShapeOp* projOp = new QtMShapeOp( $6 );
        concatOpList->emplace_back( projOp );

        //final geometry
        QtGeometryOp* geomOp = new QtGeometryOp( concatOpList, QtGeometryData::QtGeometryType::GEOM_SUBSPACE );

        //generate the result mdd containing the curtain-clipped values
        $$ = new QtClipping( $3, geomOp);
        $$->setParseInfo( *($1.info) );

        //cleanup mdd arg
        parseQueryTree->removeDynamicObject( $3 );

	QtNode::QtOperationList::iterator iter;
	for( iter=$6->begin(); iter!=$6->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }

        //add this object to the query tree
        parseQueryTree->addDynamicObject( $$ );

        //cleanup tokens
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($5)
        FREESTACK($7)
    }
    | CLIP LRPAR generalExp COMMA LINESTRING parentheticalLinestring RRPAR
    {
        QtNode::QtOperationList* concatOpList = new QtNode::QtOperationList();
        concatOpList->reserve(2);

        QtMShapeOp* projOp = new QtMShapeOp( $6 );
        concatOpList->emplace_back( projOp );

        //final geometry
        QtGeometryOp* geomOp = new QtGeometryOp( concatOpList, QtGeometryData::QtGeometryType::GEOM_LINESTRING );

        //generate the result mdd containing the curtain-clipped values
        $$ = new QtClipping( $3, geomOp);
        $$->setParseInfo( *($1.info) );

        //cleanup mdd arg
        parseQueryTree->removeDynamicObject( $3 );

        QtNode::QtOperationList::iterator iter;
        for( iter=$6->begin(); iter!=$6->end(); ++iter )
        {
            parseQueryTree->removeDynamicObject( *iter );
        }

        //add this object to the query tree
        parseQueryTree->addDynamicObject( $$ );

        //cleanup tokens
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($5)
        FREESTACK($7)
    }
    // same as previous, but attach coordinates
    | CLIP LRPAR generalExp COMMA LINESTRING parentheticalLinestring RRPAR WITH COORDINATES
    {
        QtNode::QtOperationList* concatOpList = new QtNode::QtOperationList();
        concatOpList->reserve(2);

        QtMShapeOp* projOp = new QtMShapeOp( $6 );
        concatOpList->emplace_back( projOp );

        //final geometry
        QtGeometryOp* geomOp = new QtGeometryOp( concatOpList, QtGeometryData::QtGeometryType::GEOM_LINESTRING );

        //generate the result mdd containing the curtain-clipped values
        auto *res = new QtClipping( $3, geomOp);
        res->setWithCoordinates(true);
        $$ = res;
        $$->setParseInfo( *($1.info) );

        //cleanup mdd arg
        parseQueryTree->removeDynamicObject( $3 );

        QtNode::QtOperationList::iterator iter;
        for( iter=$6->begin(); iter!=$6->end(); ++iter )
        {
            parseQueryTree->removeDynamicObject( *iter );
        }

        //add this object to the query tree
        parseQueryTree->addDynamicObject( $$ );

        //cleanup tokens
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($5)
        FREESTACK($7)
    }
    //| CLIP LRPAR generalExp COMMA MULTILINESTRING parentheticalLinestring RRPAR
    //{
    //}
    | CLIP LRPAR generalExp COMMA POLYGON LRPAR positiveGenusPolygon RRPAR RRPAR
    {
        //final geometry
        QtGeometryOp* geomOp = new QtGeometryOp( $7, QtGeometryData::QtGeometryType::GEOM_POLYGON );

        //generate the result mdd containing the curtain-clipped values
        $$ = new QtClipping( $3, geomOp);
        $$->setParseInfo( *($1.info) );

        //cleanup mdd arg
        parseQueryTree->removeDynamicObject( $3 );

	QtNode::QtOperationList::iterator iter;
	for( iter=$7->begin(); iter!=$7->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }

        //add this object to the query tree
        parseQueryTree->addDynamicObject( $$ );

        //cleanup tokens
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($5)
        FREESTACK($6)
        FREESTACK($8)
        FREESTACK($9)
    }
    | CLIP LRPAR generalExp COMMA MULTIPOLYGON LRPAR polygonVector RRPAR RRPAR
    {
        //final geometry of the multipolygon
        QtGeometryOp* geomOp = new QtGeometryOp( $7, QtGeometryData::QtGeometryType::GEOM_MULTIPOLYGON );

        //generate the result mdd containing the curtain-clipped values
        $$ = new QtClipping( $3, geomOp);
        $$->setParseInfo( *($1.info) );

        //cleanup mdd arg
        parseQueryTree->removeDynamicObject( $3 );

	QtNode::QtOperationList::iterator iter;
	for( iter=$7->begin(); iter!=$7->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }

        //add this object to the query tree
        parseQueryTree->addDynamicObject( $$ );

        //cleanup tokens
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($5)
        FREESTACK($6)
        FREESTACK($8)
        FREESTACK($9)
    }
    | CLIP LRPAR generalExp COMMA CURTAIN LRPAR PROJECTION parentheticalLinestring COMMA POLYGON LRPAR positiveGenusPolygon RRPAR RRPAR RRPAR
    {
        QtNode::QtOperationList* concatOpList = new QtNode::QtOperationList();
        concatOpList->reserve(2);

        QtMShapeOp* projOp = new QtMShapeOp( $8 );
        concatOpList->emplace_back( projOp );

        QtGeometryOp* lsOp = new QtGeometryOp($12, QtGeometryData::QtGeometryType::GEOM_POLYGON);
        concatOpList->emplace_back( lsOp );

        //final geometry
        QtGeometryOp* geomOp = new QtGeometryOp( concatOpList, QtGeometryData::QtGeometryType::GEOM_CURTAIN_POLYGON );

        //generate the result mdd containing the curtain-clipped values
        $$ = new QtClipping( $3, geomOp);
        $$->setParseInfo( *($1.info) );

        //cleanup mdd arg
        parseQueryTree->removeDynamicObject( $3 );
	QtNode::QtOperationList::iterator iter;
	for( iter=$8->begin(); iter!=$8->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }
        for( iter=$12->begin(); iter!=$12->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }

        //add this object to the query tree
        parseQueryTree->addDynamicObject( $$ );

        //cleanup tokens
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($5)
        FREESTACK($6)
        FREESTACK($7)
        FREESTACK($9)
        FREESTACK($10)
        FREESTACK($11)
        FREESTACK($13)
        FREESTACK($14)
        FREESTACK($15)
    }    
    | CLIP LRPAR generalExp COMMA CURTAIN LRPAR PROJECTION parentheticalLinestring COMMA LINESTRING parentheticalLinestring RRPAR RRPAR
    {
        QtNode::QtOperationList* concatOpList = new QtNode::QtOperationList();
        concatOpList->reserve(2);

        QtMShapeOp* projOp = new QtMShapeOp( $8 );
        concatOpList->emplace_back( projOp );

        QtMShapeOp* lsOp = new QtMShapeOp( $11 );
        concatOpList->emplace_back( lsOp );

        // final geometry
        QtGeometryOp* geomOp = new QtGeometryOp( concatOpList, QtGeometryData::QtGeometryType::GEOM_CURTAIN_LINESTRING_EMBEDDED );

        //generate the result mdd containing the curtain-clipped values
        $$ = new QtClipping( $3, geomOp);
        $$->setParseInfo( *($1.info) );

        //cleanup mdd arg
        parseQueryTree->removeDynamicObject( $3 );
	QtNode::QtOperationList::iterator iter;
	for( iter=$8->begin(); iter!=$8->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }
        for( iter=$11->begin(); iter!=$11->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }

        //add this object to the query tree
        parseQueryTree->addDynamicObject( $$ );

        //cleanup tokens
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($5)
        FREESTACK($6)
        FREESTACK($7)
        FREESTACK($9)
        FREESTACK($10)
        FREESTACK($12)
        FREESTACK($13)
    }
    | CLIP LRPAR generalExp COMMA CORRIDOR LRPAR PROJECTION parentheticalLinestring COMMA LINESTRING parentheticalLinestring COMMA POLYGON LRPAR positiveGenusPolygon RRPAR RRPAR RRPAR
        {
        QtNode::QtOperationList* concatOpList = new QtNode::QtOperationList();
        concatOpList->reserve(3);

        QtMShapeOp* projOp = new QtMShapeOp($8);
        concatOpList->emplace_back( projOp );

        QtMShapeOp* lsOp = new QtMShapeOp( $11 );
        concatOpList->emplace_back( lsOp );

        QtGeometryOp* polyOp = new QtGeometryOp( $15, QtGeometryData::QtGeometryType::GEOM_POLYGON );
        concatOpList->emplace_back( polyOp );

        // final geometry
        QtGeometryOp* geomOp = new QtGeometryOp( concatOpList, QtGeometryData::QtGeometryType::GEOM_CORRIDOR_POLYGON );

        //generate the result mdd containing the curtain-clipped values
        $$ = new QtClipping( $3, geomOp);
        $$->setParseInfo( *($1.info) );

        //cleanup mdd arg
        parseQueryTree->removeDynamicObject( $3 );
	QtNode::QtOperationList::iterator iter;
	for( iter=$8->begin(); iter!=$8->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }
        for( iter=$11->begin(); iter!=$11->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }
        for( iter=$15->begin(); iter!=$15->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }

        //add this object to the query tree
        parseQueryTree->addDynamicObject( $$ );

        //cleanup tokens
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($5)
        FREESTACK($6)
        FREESTACK($7)
        FREESTACK($9)
        FREESTACK($10)
        FREESTACK($12)
        FREESTACK($13)
        FREESTACK($14)
        FREESTACK($16)
        FREESTACK($17)
        FREESTACK($18)
    }
    | CLIP LRPAR generalExp COMMA CORRIDOR LRPAR PROJECTION parentheticalLinestring COMMA LINESTRING parentheticalLinestring COMMA LINESTRING positiveGenusPolygon RRPAR RRPAR
    {
        QtNode::QtOperationList* concatOpList = new QtNode::QtOperationList();
        concatOpList->reserve(2);

        QtMShapeOp* projOp = new QtMShapeOp($8);
        concatOpList->emplace_back( projOp );

        QtMShapeOp* lsOp = new QtMShapeOp($11);
        concatOpList->emplace_back( lsOp );

        QtMShapeOp* lsMaskOp = new QtMShapeOp($14);
        concatOpList->emplace_back( lsMaskOp);

        //generate the class containing the projection dimension vector
        QtGeometryOp* geomOp = new QtGeometryOp( concatOpList, QtGeometryData::QtGeometryType::GEOM_CORRIDOR_LINESTRING_EMBEDDED );

        //generate the result mdd containing the curtain-clipped values
        $$ = new QtClipping( $3, geomOp);
        $$->setParseInfo( *($1.info) );

        //cleanup mdd arg
        parseQueryTree->removeDynamicObject( $3 );
	QtNode::QtOperationList::iterator iter;
	for( iter=$8->begin(); iter!=$8->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }
        for( iter=$11->begin(); iter!=$11->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }
        for( iter=$14->begin(); iter!=$14->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }

        //add this object to the query tree
        parseQueryTree->addDynamicObject( $$ );

        //cleanup tokens
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($5)
        FREESTACK($6)
        FREESTACK($7)
        FREESTACK($9)
        FREESTACK($10)
        FREESTACK($12)
        FREESTACK($13)
        FREESTACK($15)
        FREESTACK($16)
    }
    | CLIP LRPAR generalExp COMMA CORRIDOR LRPAR PROJECTION parentheticalLinestring COMMA LINESTRING parentheticalLinestring COMMA POLYGON LRPAR positiveGenusPolygon RRPAR COMMA DISCRETE RRPAR RRPAR
    {
        QtNode::QtOperationList* concatOpList = new QtNode::QtOperationList();
        concatOpList->reserve(3);

        QtMShapeOp* projOp = new QtMShapeOp($8);
        concatOpList->emplace_back( projOp );

        QtMShapeOp* lsOp = new QtMShapeOp( $11 );
        concatOpList->emplace_back( lsOp );

        QtGeometryOp* polyOp = new QtGeometryOp( $15, QtGeometryData::QtGeometryType::GEOM_POLYGON );
        concatOpList->emplace_back( polyOp );

        // final geometry
        QtGeometryOp* geomOp = new QtGeometryOp( concatOpList, 
                                                 QtGeometryData::QtGeometryType::GEOM_CORRIDOR_POLYGON, 
                                                 QtGeometryData::QtGeometryFlag::DISCRETEPATH );

        //generate the result mdd containing the curtain-clipped values
        $$ = new QtClipping( $3, geomOp);
        $$->setParseInfo( *($1.info) );

        //cleanup mdd arg
        parseQueryTree->removeDynamicObject( $3 );
	QtNode::QtOperationList::iterator iter;
	for( iter=$8->begin(); iter!=$8->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }
        for( iter=$11->begin(); iter!=$11->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }
        for( iter=$15->begin(); iter!=$15->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }

        //add this object to the query tree
        parseQueryTree->addDynamicObject( $$ );

        //cleanup tokens
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($5)
        FREESTACK($6)
        FREESTACK($7)
        FREESTACK($9)
        FREESTACK($10)
        FREESTACK($12)
        FREESTACK($13)
        FREESTACK($14)
        FREESTACK($16)
        FREESTACK($17)
        FREESTACK($18)
        FREESTACK($19)
        FREESTACK($20)
    }
    | CLIP LRPAR generalExp COMMA CORRIDOR LRPAR PROJECTION parentheticalLinestring COMMA LINESTRING parentheticalLinestring COMMA LINESTRING positiveGenusPolygon COMMA DISCRETE RRPAR RRPAR
    {
        QtNode::QtOperationList* concatOpList = new QtNode::QtOperationList();
        concatOpList->reserve(2);

        QtMShapeOp* projOp = new QtMShapeOp($8);
        concatOpList->emplace_back( projOp );

        QtMShapeOp* lsOp = new QtMShapeOp($11);
        concatOpList->emplace_back( lsOp );

        QtMShapeOp* lsMaskOp = new QtMShapeOp($14);
        concatOpList->emplace_back( lsMaskOp);

        //generate the class containing the projection dimension vector
        QtGeometryOp* geomOp = new QtGeometryOp( concatOpList, 
                                                 QtGeometryData::QtGeometryType::GEOM_CORRIDOR_LINESTRING_EMBEDDED, 
                                                 QtGeometryData::QtGeometryFlag::DISCRETEPATH );

        //generate the result mdd containing the curtain-clipped values
        $$ = new QtClipping( $3, geomOp);
        $$->setParseInfo( *($1.info) );

        //cleanup mdd arg
        parseQueryTree->removeDynamicObject( $3 );
	QtNode::QtOperationList::iterator iter;
	for( iter=$8->begin(); iter!=$8->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }
        for( iter=$11->begin(); iter!=$11->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }
        for( iter=$14->begin(); iter!=$14->end(); ++iter )
        {
	    parseQueryTree->removeDynamicObject( *iter );
        }

        //add this object to the query tree
        parseQueryTree->addDynamicObject( $$ );

        //cleanup tokens
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($5)
        FREESTACK($6)
        FREESTACK($7)
        FREESTACK($9)
        FREESTACK($10)
        FREESTACK($12)
        FREESTACK($13)
        FREESTACK($15)
        FREESTACK($16)
        FREESTACK($17)
        FREESTACK($18)
    }
    // added -- PB 2005-jun-18
    | EXTEND LRPAR generalExp COMMA generalExp RRPAR
    {
        $$ = new QtExtend( $3, $5 );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->removeDynamicObject( $5 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    // added -- DM 2012-dec-07
    | DBINFO LRPAR collectionIterator RRPAR
    {
        QtVariable* var = new QtVariable( $3.value );
        var->setParseInfo( *($3.info) );
        $$ = new QtInfo( var );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($3)
        FREESTACK($4)
    }
    | DBINFO LRPAR collectionIterator COMMA StringLit RRPAR
    {
        QtVariable* var = new QtVariable( $3.value );
        var->setParseInfo( *($3.info) );
        $$ = new QtInfo( var, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($3)
        FREESTACK($4)
        FREESTACK($6)
    }
    | SCALE LRPAR generalExp COMMA generalExp RRPAR
    {
        $$ = new QtScale( $3, $5 );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->removeDynamicObject( $5 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | BIT LRPAR generalExp COMMA generalExp RRPAR
    {
        $$ = new QtBit( $3, $5 );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->removeDynamicObject( $5 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | TIFF LRPAR generalExp COMMA StringLit RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_TOTIFF, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | TIFF LRPAR generalExp  RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_TOTIFF );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | BMP LRPAR generalExp COMMA StringLit RRPAR
    {
        std::string format{"BMP"};
        $$ = new QtConversion( $3, QtConversion::QT_TOBMP, format, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | BMP LRPAR generalExp  RRPAR
    {
        std::string format{"BMP"};
        $$ = new QtConversion( $3, QtConversion::QT_TOBMP, format, NULL );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }		
    | HDF LRPAR generalExp COMMA StringLit RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_TOHDF, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | HDF LRPAR generalExp  RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_TOHDF );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | CSV LRPAR generalExp COMMA StringLit RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_TOCSV, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | CSV LRPAR generalExp  RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_TOCSV );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | JPEG LRPAR generalExp COMMA StringLit RRPAR
    {
        std::string format{"JPEG"};
        $$ = new QtConversion( $3, QtConversion::QT_TOJPEG, format, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | JPEG LRPAR generalExp  RRPAR
    {
        std::string format{"JPEG"};
        $$ = new QtConversion( $3, QtConversion::QT_TOJPEG, format, NULL );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | PNG LRPAR generalExp COMMA StringLit RRPAR
    {
        std::string format{"PNG"};
        $$ = new QtConversion( $3, QtConversion::QT_TOPNG, format, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | PNG LRPAR generalExp  RRPAR
    {
        std::string format{"PNG"};
        $$ = new QtConversion( $3, QtConversion::QT_TOPNG, format, NULL );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | DEM LRPAR generalExp COMMA StringLit RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_TODEM, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | DEM LRPAR generalExp  RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_TODEM );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | NETCDF LRPAR generalExp COMMA StringLit RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_TONETCDF, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | NETCDF LRPAR generalExp  RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_TONETCDF );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | INV_TIFF LRPAR generalExp COMMA StringLit RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMTIFF, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | INV_TIFF LRPAR generalExp  RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMTIFF );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | INV_BMP LRPAR generalExp COMMA StringLit RRPAR
    {
        std::string format{"BMP"};
        $$ = new QtConversion( $3, QtConversion::QT_FROMBMP, format, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | INV_BMP LRPAR generalExp  RRPAR
    {
        std::string format{"BMP"};
        $$ = new QtConversion( $3, QtConversion::QT_FROMBMP, format, NULL );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }		
    | INV_HDF LRPAR generalExp COMMA StringLit RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMHDF, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | INV_HDF LRPAR generalExp  RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMHDF );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | INV_CSV LRPAR generalExp COMMA StringLit RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMCSV, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | INV_CSV LRPAR generalExp  RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMCSV );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | INV_JPEG LRPAR generalExp COMMA StringLit RRPAR
    {
        std::string format{"JPEG"};
        $$ = new QtConversion( $3, QtConversion::QT_FROMJPEG, format, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | INV_JPEG LRPAR generalExp  RRPAR
    {
        std::string format{"JPEG"};
        $$ = new QtConversion( $3, QtConversion::QT_FROMJPEG, format, NULL );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | INV_PNG LRPAR generalExp COMMA StringLit RRPAR
    {
        std::string format{"PNG"};
        $$ = new QtConversion( $3, QtConversion::QT_FROMPNG, format, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | INV_PNG LRPAR generalExp  RRPAR
    {
        std::string format{"PNG"};
        $$ = new QtConversion( $3, QtConversion::QT_FROMPNG, format, NULL );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | INV_DEM LRPAR generalExp COMMA StringLit RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMDEM, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | INV_DEM LRPAR generalExp  RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMDEM );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | INV_NETCDF LRPAR generalExp COMMA StringLit RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMNETCDF, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | INV_NETCDF LRPAR generalExp  RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMNETCDF );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | INV_GRIB LRPAR generalExp COMMA StringLit RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMGRIB, $5.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | ENCODE LRPAR generalExp COMMA StringLit RRPAR
    {
        std::string format($5.value);
        $$ = new QtConversion( $3, QtConversion::QT_TOGDAL, format, NULL );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    }
    | ENCODE LRPAR generalExp COMMA StringLit COMMA StringLit RRPAR
    {
        std::string format($5.value);
        $$ = new QtConversion( $3, QtConversion::QT_TOGDAL, format, $7.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
        FREESTACK($8)
    }
    | DECODE LRPAR generalExp RRPAR
    {
        $$ = new QtConversion( $3, QtConversion::QT_FROMGDAL );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
    }
    | DECODE LRPAR generalExp COMMA StringLit COMMA StringLit RRPAR
    {
        std::string format($5.value);
        $$ = new QtConversion( $3, QtConversion::QT_FROMGDAL, format, $7.value );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->removeDynamicObject( $3 );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
        FREESTACK($8)
    };
	

structSelection: DOT attributeIdent                
	{
	  $$ = new QtDot( $2.value );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	}
	| DOT intLitExp
	{
	  if( $2.negative )
	    if( $2.svalue < 0 )
        {
           yyerror(mflag, "non negative integer expected");
           FREESTACK($1)
           FREESTACK($2)
           QueryTree::symtab.wipe();
           YYABORT;
        }
	    else
	      $$ = new QtDot( (unsigned int)$2.svalue );
	  else
	    $$ = new QtDot( (unsigned int)$2.uvalue );
	  parseQueryTree->addDynamicObject( $$ );
	  $$->setParseInfo( *($1.info) );
	  FREESTACK($1)
	  FREESTACK($2)
	};

inductionExp: SQRT LRPAR generalExp RRPAR         
	{
	  $$ = new QtSqrt( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
 	}
	| POW LRPAR generalExp COMMA intLitExp RRPAR
	{
	  $$ = new QtPow( $3, (double) $5.svalue );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	  FREESTACK($6)
	}
	| POW LRPAR generalExp COMMA floatLitExp RRPAR
	{
	  $$ = new QtPow( $3, $5.value );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	  FREESTACK($6)
	}
	| POWER LRPAR generalExp COMMA intLitExp RRPAR
	{
	  $$ = new QtPow( $3, (double) $5.svalue );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	  FREESTACK($6)
	}
	| POWER LRPAR generalExp COMMA floatLitExp RRPAR
	{
	  $$ = new QtPow( $3, $5.value );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	  FREESTACK($6)
	}
	| ABS LRPAR generalExp RRPAR         
	{
	  $$ = new QtAbs( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| EXP LRPAR generalExp RRPAR         
	{
	  $$ = new QtExp( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| LOGFN LRPAR generalExp RRPAR
	{
	  $$ = new QtLog( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| LN LRPAR generalExp RRPAR         
	{
	  $$ = new QtLn( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| SIN LRPAR generalExp RRPAR         
	{
	  $$ = new QtSin( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| COS LRPAR generalExp RRPAR         
	{
	  $$ = new QtCos( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| TAN LRPAR generalExp RRPAR         
	{
	  $$ = new QtTan( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| SINH LRPAR generalExp RRPAR         
	{
	  $$ = new QtSinh( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| COSH LRPAR generalExp RRPAR         
	{
	  $$ = new QtCosh( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| TANH LRPAR generalExp RRPAR         
	{
	  $$ = new QtTanh( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| ARCSIN LRPAR generalExp RRPAR
	{
	  $$ = new QtArcsin( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| ASIN LRPAR generalExp RRPAR
	{
	  $$ = new QtArcsin( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| ARCCOS LRPAR generalExp RRPAR         
	{
	  $$ = new QtArccos( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
  | ACOS LRPAR generalExp RRPAR         
	{
	  $$ = new QtArccos( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
	| ARCTAN LRPAR generalExp RRPAR         
	{
	  $$ = new QtArctan( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
  | ATAN LRPAR generalExp RRPAR         
	{
	  $$ = new QtArctan( $3 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	}
  
        | generalExp DOT RE
        {
          $$ = new QtRealPartOp( $1 );
          $$->setParseInfo( *($3.info) );
          parseQueryTree->removeDynamicObject( $1 );
          parseQueryTree->addDynamicObject( $$ );
          FREESTACK($2)
          FREESTACK($3)
        }
        | generalExp DOT IM
        {
          $$ = new QtImaginarPartOp( $1 );
          $$->setParseInfo( *($3.info) );
          parseQueryTree->removeDynamicObject( $1 );
          parseQueryTree->addDynamicObject( $$ );
          FREESTACK($2)
          FREESTACK($3)
        }
	| NOT generalExp
	{
	  $$ = new QtNot( $2 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $2 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
	| generalExp OVERLAY generalExp
	{
	  $$ = new QtOverlay ( $3, $1 );
	  $$->setParseInfo( *($2.info) );
	  FREESTACK($2)
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	}
	| generalExp IS NULLKEY
	{
	  $$ = new QtIsNull( $1 );
	  $2.info->setToken("is null");
	  $$->setParseInfo( *($2.info) );

	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	  FREESTACK($3)
	}
	| generalExp IS NOT NULLKEY
	{
	  QtOperation* tmp = new QtIsNull($1);
	  $2.info->setToken("is not null");
	  tmp->setParseInfo( *($2.info) );
	  $$ = new QtNot(tmp);
	  $$->setParseInfo( tmp->getParseInfo() );

	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	  FREESTACK($3)
	  FREESTACK($4)
	}
	| generalExp IS generalExp
	{
	  $$ = new QtIs ( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp AND generalExp
	{
	  $$ = new QtAnd( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp OR generalExp
	{
	  $$ = new QtOr ( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp XOR generalExp
	{
	  $$ = new QtXor( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp PLUS generalExp
	{
	  $$ = new QtPlus ( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp MINUS generalExp
	{
	  $$ = new QtMinus( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp MAX_BINARY generalExp
	{
	  $$ = new QtMax_binary( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp MIN_BINARY generalExp
	{
	  $$ = new QtMin_binary( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp MULT generalExp
	{
	  $$ = new QtMult ( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp DIV generalExp
	{
	  $$ = new QtDiv  ( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| INTDIV LRPAR generalExp COMMA generalExp RRPAR
	{
	  $$ = new QtIntDiv  ( $3, $5 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->removeDynamicObject( $5 );
	  parseQueryTree->addDynamicObject( $$ );
      FREESTACK($1);
	  FREESTACK($2);
      FREESTACK($4);
      FREESTACK($6);
	}
	| MOD LRPAR generalExp COMMA generalExp RRPAR
	{
	  $$ = new QtMod  ( $3, $5 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->removeDynamicObject( $5 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1);
      FREESTACK($2);
      FREESTACK($4);
      FREESTACK($6);
	}
    | generalExp EQUAL generalExp
	{
	  $$ = new QtEqual( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  FREESTACK($2)
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	}
	| generalExp LESS generalExp
	{
	  $$ = new QtLess( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp GREATER generalExp
	{
	  $$ = new QtLess( $3, $1 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
 	}
	| generalExp LESSEQUAL generalExp
	{
	  $$ = new QtLessEqual( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp GREATEREQUAL generalExp
	{
	  $$ = new QtLessEqual( $3, $1 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
	}
	| generalExp NOTEQUAL generalExp
	{
	  $$ = new QtNotEqual ( $1, $3 );
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($2)
 	}
	| PLUS  generalExp %prec UNARYOP
	{
	  $$ = $2;
	  FREESTACK($1)
	}
	| MINUS generalExp %prec UNARYOP
	{
	  $$ = new QtMult( $2, new QtConst( new QtAtomicData( (r_Long)-1, 1 ) ) );
	  parseQueryTree->removeDynamicObject( $2 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
	| LRPAR castType RRPAR generalExp %prec UNARYOP
	{
          $$ = new QtCast($4, $2.value);
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject($4);
	  parseQueryTree->addDynamicObject($$);
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($3)
	}
	| LRPAR TypeName RRPAR generalExp %prec UNARYOP
	{
	  $$ = new QtCast($4, $2.value);
	  $$->setParseInfo( *($2.info) );
	  parseQueryTree->removeDynamicObject($4);
	  parseQueryTree->addDynamicObject($$);
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($3)
	}                  	
	| LRPAR generalExp RRPAR
	{
	  $$ = $2;
	  FREESTACK($1)
	  FREESTACK($3)
	}
	| generalExp structSelection
	{
	  $2->setInput($1);
	  $$ = $2;
	  parseQueryTree->removeDynamicObject( $1 );
	}
    | COMPLEX LRPAR generalExp COMMA generalExp RRPAR
    {
        $$ = new QtConstructComplex($3, $5);
        $$->setParseInfo(*($1.info));
        parseQueryTree->removeDynamicObject($3);
        parseQueryTree->removeDynamicObject($5);
        parseQueryTree->addDynamicObject($$);
        FREESTACK($1)
        FREESTACK($2)
        FREESTACK($4)
        FREESTACK($6)
    };
	
castType: TBOOL			{ $$.info = $1.info; $$.value = SyntaxType::BOOL_NAME.c_str(); }
        | TCHAR			{ $$.info = $1.info; $$.value = SyntaxType::CHAR_NAME.c_str(); }
        | TOCTET		{ $$.info = $1.info; $$.value = SyntaxType::OCTET_NAME.c_str(); }
        | TSHORT		{ $$.info = $1.info; $$.value = SyntaxType::SHORT_NAME.c_str(); }
        | TUSHORT		{ $$.info = $1.info; $$.value = SyntaxType::USHORT_NAME.c_str(); }
        | TLONG			{ $$.info = $1.info; $$.value = SyntaxType::LONG_NAME.c_str(); }
        | TULONG		{ $$.info = $1.info; $$.value = SyntaxType::ULONG_NAME.c_str(); }
        | TFLOAT		{ $$.info = $1.info; $$.value = SyntaxType::FLOAT_NAME.c_str(); }
        | TDOUBLE		{ $$.info = $1.info; $$.value = SyntaxType::DOUBLE_NAME.c_str(); }
        | TUNSIG TSHORT	        { $$.info = $1.info; $$.value = SyntaxType::UNSIGNED_SHORT_NAME.c_str(); }
        | TUNSIG TLONG	        { $$.info = $1.info; $$.value = SyntaxType::UNSIGNED_LONG_NAME.c_str(); };
        | TCOMPLEX1 { $$.info = $1.info; $$.value = SyntaxType::COMPLEXTYPE1.c_str(); }
        | TCOMPLEX2 { $$.info = $1.info; $$.value = SyntaxType::COMPLEXTYPE2.c_str(); }
	      | TCINT16 { $$.info = $1.info; $$.value = SyntaxType::CINT16.c_str(); }
	      | TCINT32 { $$.info = $1.info; $$.value = SyntaxType::CINT32.c_str(); }

collectionList: collectionList COMMA iteratedCollection 
	{
	  // add the QtMDDAccess object and give back the list
	  $1->push_back($3);
	  $$ = $1;
	  FREESTACK($2)
	}
	| iteratedCollection
	{
	  // create a new list and add the QtMDDAccess object
	  $$ = new QtIterator::QtONCStreamList();
	  $$->push_back($1);
	  parseQueryTree->addDynamicObject( $$ );
	};

iteratedCollection: namedCollection AS collectionIterator
	{
	  $$ = new QtMDDAccess( *($1.value), $3.value );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  

      delete $1.value;
	  FREESTACK($2)
	  FREESTACK($3)
	}
	| namedCollection collectionIterator
	{
	  $$ = new QtMDDAccess( *($1.value), $2.value );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );

      delete $1.value;
	  FREESTACK($2)
	}
	| namedCollection   
	{
	  $$ = new QtMDDAccess( *($1.value), $1.value->getCollectionName() );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );

      delete $1.value;
	};

variable: Identifier                          
	{
	  $$ = new QtVariable( $1.value );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	};



namedCollection: hostName COLON IntegerLit COLON Identifier
	{
	  $$.value = new QtCollection($1.value,$3.svalue,$5.value);
	  $$.info = $1.info;

	  string parsedToken = $1.info->getToken() + "\":" + $3.info->getToken() + ":" + $5.info->getToken();
	  $$.info->setToken(parsedToken);

	  FREESTACK($2)
	  FREESTACK($3)
	  FREESTACK($4)
	  FREESTACK($5)
	}
	| Identifier
	{
	  $$.value = new QtCollection($1.value);
	  $$.info = $1.info;
	}

hostName: Identifier
	{
	  $$ = $1;
	}
	| StringLit			
	{
	  $$.value = $1.value; 
	  $$.info = $1.info; 
	};
	       

collectionIterator: Identifier;

attributeIdent: Identifier;

typeName: Identifier
        | TypeName;

marrayVariable: Identifier;

condenseVariable: Identifier;

reduceExp: reduceIdent LRPAR generalExp RRPAR
	{
	  $1->setInput( $3 );
	  $$ = $1;
	  parseQueryTree->removeDynamicObject( $3 );
	  FREESTACK($2)
	  FREESTACK($4)
	};

reduceIdent: ALL                                 
	{
	  $$ = new QtAll();
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
	| SOME
	{
	  $$ = new QtSome();
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
	| COUNTCELLS
	{
	  $$ = new QtCountCells();
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
	| ADDCELLS
	{
	  $$ = new QtAddCells();
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
	| AVGCELLS                            
	{
	  $$ = new QtAvgCells();
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
	| MINCELLS
	{
	  $$ = new QtMinCells();
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
	| MAXCELLS
	{
	  $$ = new QtMaxCells();
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
  | VAR_POP
  {
    $$ = new QtStdDevVar(QtNode::QT_VARPOP);
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
  }
  | VAR_SAMP
  {
    $$ = new QtStdDevVar(QtNode::QT_VARSAMP);
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
  }
  | STDDEV_POP
  {
    $$ = new QtStdDevVar(QtNode::QT_STDDEVPOP);
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)     
   }
   | STDDEV_SAMP
   {
    $$ = new QtStdDevVar(QtNode::QT_STDDEVSAMP);
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
   };

intLitExp: IntegerLit
    {
      $$ = $1;
    }
    | MINUS intLitExp %prec UNARYOP
    {
      $$ = $2;
      if ($$.negative) {
        $$.svalue = -$$.svalue;
      } else {
        $$.negative = 1;
        $$.svalue = -static_cast<r_Long>($$.uvalue);
      }
    };

floatLitExp: FloatLit
    {
        $$ = $1;
    }
    | MINUS floatLitExp %prec UNARYOP
    {
        $$ = $2;
        $$.value = - $$.value;
    };

generalLit: scalarLit                          { $$ = $1; }  
	| mddLit                               { $$ = $1; }
	| StringLit                           
	{
	  $$ = new QtStringData( std::string($1.value) );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
	| oidLit                              { $$ = $1; };

oidLit: LESS StringLit GREATER              
	{
	  r_OId oid;
	  try {
	    oid = r_OId( $2.value );
	  }
	  catch(...) {
	    // save the parse error info and stop the parser
            if ( parseError ) delete parseError;
            parseError = new ParseInfo( 303, $2.info->getToken().c_str(),
                                        $2.info->getLineNo(), $2.info->getColumnNo() );
            FREESTACK($1)
            FREESTACK($2)
            FREESTACK($3)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }
        
	  // test if database match the current one
	  int mismatch = oid.get_base_name() == 0;
	  
	  if( !mismatch ) {
	    // check for question mark
	    char* baseName = strdup( oid.get_base_name() );
	    char* end = strchr( baseName, '?' );
	    if( end ) 
              *end = '\0';
	    mismatch = strcmp( baseName, currentClientTblElt->database.getName() ) !=0;
	    free( baseName );
            baseName = 0;
	  }
	  
	  if( mismatch ) {
	    // save the parse error info and stop the parser
	    if( parseError ) delete parseError;
	    parseError = new ParseInfo( 386, $2.info->getToken().c_str(),
                                        $2.info->getLineNo(), $2.info->getColumnNo() );
	    FREESTACK($1)
	    FREESTACK($2)
	    FREESTACK($3)
	    QueryTree::symtab.wipe();
	    YYABORT;
	  }

	  // take the local oid
	  $$ = new QtAtomicData( oid.get_local_oid(), 8 );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
 	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($3)
	};

mddLit: LESS mintervalExp dimensionLitList GREATER
	{
	  // create a QtMDD object representing the literal
	  try {
	    $$ = new QtMDD( $2, $3 );
	  }
   	  catch( ParseInfo& obj ) {
	    delete $3;
            
	    // save the parse error info and stop the parser
            if( parseError ) delete parseError;
            parseError = new ParseInfo( obj.getErrorNo(), $1.info->getToken().c_str(),
	                                $1.info->getLineNo(), $1.info->getColumnNo() );
            FREESTACK($1)
            FREESTACK($4)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }
	  $$->setParseInfo( *($1.info) );

	  for( std::list<QtScalarData*>::iterator iter=$3->begin(); iter!=$3->end(); iter++ ) {
	    delete *iter;
            parseQueryTree->removeDynamicObject( *iter );
	  }
	  
	  delete $2;
	  delete $3;
	  parseQueryTree->removeDynamicObject( $2 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($4)
	}
	| MDDPARAM                            
	{
	  try {
            $$ = new QtMDD( $1.value );
	  }
	  catch( ParseInfo& obj ) {
	    // save the parse error info and stop the parser
            if( parseError ) delete parseError;
            parseError = new ParseInfo( obj.getErrorNo(), $1.info->getToken().c_str(),
                                        $1.info->getLineNo(), $1.info->getColumnNo() );
            FREESTACK($1)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }
	  parseQueryTree->addDynamicObject( $$ );
	  $$->setParseInfo( *($1.info) );
	  FREESTACK($1)
	};

dimensionLitList: dimensionLitList SEMICOLON scalarLitList
	{
	  // concatenate the lists
	  $1->splice( $1->end(), *$3 );
	  $$ = $1;
	  delete $3;
	  FREESTACK($2)
	}
	| scalarLitList
	{
	  // simply take the list
	  $$ = $1;
	};

scalarLit: complexLit
	{
	  $$ = $1;
	}
	| atomicLit
	{
	  $$ = $1;
	};

atomicLit: BooleanLit                          
	{
	  $$ = new QtAtomicData( $1.value ); 
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
	| intLitExp
	{
	  if( $1.negative )
	    $$ = new QtAtomicData( $1.svalue, $1.bytes );
	  else
	    $$ = new QtAtomicData( $1.uvalue, $1.bytes );
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
	| floatLitExp
	{
	  $$ = new QtAtomicData( $1.value, $1.bytes ); 
	  $$->setParseInfo( *($1.info) );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	}
  | COMPLEX LRPAR intLitExp COMMA intLitExp RRPAR
	{
	  // this should construct a complex type
	  // for both float and double cell type
	  if($3.bytes+$5.bytes== 2u * sizeof(int) || $3.bytes + $5.bytes == 2u * sizeof(short) || $3.bytes + $5.bytes == 2u * sizeof(long)) {
	    $$ = new QtAtomicData($3.svalue, $5.svalue, $3.bytes + $5.bytes);
	  } else {
	    if(parseError) delete parseError;
	    parseError = new ParseInfo(311, $2.info->getToken().c_str(),
	        $2.info->getLineNo(), $2.info->getColumnNo());
	    FREESTACK($1)
	    FREESTACK($2)
	    FREESTACK($4)
	    FREESTACK($6)
	    QueryTree::symtab.wipe();
	    YYABORT;
	  }
	  $$->setParseInfo(*($3.info));
	  parseQueryTree->addDynamicObject($$);
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	  FREESTACK($6)
	}
	| COMPLEX LRPAR floatLitExp COMMA floatLitExp RRPAR
	{
	  // this should construct a complex type
	  // for both float and double cell type
	  if($3.bytes+$5.bytes== 2u * sizeof(int) || $3.bytes + $5.bytes == 2u * sizeof(float) || $3.bytes + $5.bytes == 2u * sizeof(double)) {
	    $$ = new QtAtomicData($3.value, $5.value, $3.bytes + $5.bytes);
	  } else {
	    if(parseError) delete parseError;
	    parseError = new ParseInfo(311, $2.info->getToken().c_str(),
	        $2.info->getLineNo(), $2.info->getColumnNo());
	    FREESTACK($1)
	    FREESTACK($2)
	    FREESTACK($4)
	    FREESTACK($6)
	    QueryTree::symtab.wipe();
	    YYABORT;
	  }
	  $$->setParseInfo(*($3.info));
	  parseQueryTree->addDynamicObject($$);
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	  FREESTACK($6)
	};

complexLit: LCPAR scalarLitList RCPAR           
	{
	  for( std::list<QtScalarData*>::iterator iter=$2->begin(); iter!=$2->end(); iter++ )
	    parseQueryTree->removeDynamicObject( *iter );
	  $$ = new QtComplexData( $2 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($3)
	}
	| STRCT LCPAR scalarLitList RCPAR           
	{
	  for( std::list<QtScalarData*>::iterator iter=$3->begin(); iter!=$3->end(); iter++ )
	    parseQueryTree->removeDynamicObject( *iter );
	  $$ = new QtComplexData( $3 );
	  parseQueryTree->addDynamicObject( $$ );
	  FREESTACK($1)
	  FREESTACK($2)
	  FREESTACK($4)
	};

scalarLitList: scalarLitList COMMA scalarLit       
	{
	  // add the literal element and give back the list
	  $1->push_back($3);
	  $$ = $1;
	  FREESTACK($2)
	}
	| scalarLit
	{
	  // create a new list and add the literal element
	  $$ = new QtComplexData::QtScalarDataList();
	  $$->push_back($1);
	}; 

addNullvaluesExp: generalExp nullvaluesExp
    {
        QtOperation* op = static_cast<QtOperation*>($1);
        QtNullvaluesOp* nullvaluesOp = static_cast<QtNullvaluesOp*>($2);
        $$ = new QtAddNullvalues(op, nullvaluesOp);
        parseQueryTree->removeDynamicObject( $1 );
        parseQueryTree->removeDynamicObject( $2 );
        parseQueryTree->addDynamicObject( $$ );
    };
        
// NULL VALUES '[' NULL1 [ ':' NULL2 ] ',' ... ']'
nullvaluesExp: NULLKEY VALUES nullvaluesList
    {
        $$ = $3;
        FREESTACK($1)
        FREESTACK($2)
    };

//
// Null values list: '[' NULL1 [ ':' NULL2 ] ',' ... ']'
//
// Similar to mintervalExp but allows any scalarLit for NULL1, NULL2, etc.
//
nullvaluesList: LEPAR nullvalueIntervalList REPAR
    {
        $$ = new QtNullvaluesOp( $2 );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($3)
    }
    |
    LEPAR REPAR
    {
        $$ = new QtNullvaluesOp( new QtNullvaluesOp::QtNullvaluesList() );
        $$->setParseInfo( *($1.info) );
        parseQueryTree->addDynamicObject( $$ );
        FREESTACK($1)
        FREESTACK($2)
    };

nullvalueIntervalList: nullvalueIntervalList COMMA nullvalueIntervalExp
    {
        $1->emplace_back($3.low, $3.high);
        $$ = $1;
        FREESTACK($2)
    }
    | nullvalueIntervalExp
    {
        $$ = new QtNullvaluesOp::QtNullvaluesList();
        $$->emplace_back($1.low, $1.high);
    };

nullvalueIntervalExp: scalarLit COLON scalarLit
    {
        $$.low = $1;
        $$.high = $3;
        FREESTACK($2)
    }
    | scalarLit COLON MULT
    {
        $$.low = $1;
        $$.high = new QtAtomicData(std::numeric_limits<double>::max(), 8);
        parseQueryTree->addDynamicObject( $$.high );
        FREESTACK($2)
        FREESTACK($3)
    }
    | MULT COLON scalarLit
    {
        $$.low = new QtAtomicData(std::numeric_limits<double>::lowest(), 8);
        parseQueryTree->addDynamicObject( $$.low );
        $$.high = $3;
        FREESTACK($1)
        FREESTACK($2)
    } 
    | scalarLit
    {
        $$.low = $1;
        $$.high = $1;
    };


projectExp:
	  PROJECT LRPAR generalExp COMMA StringLit COMMA StringLit COMMA StringLit RRPAR
	{		
	  $$ = new QtProject( (QtOperation *)$3, $5.value, $7.value, $9.value );
	  parseQueryTree->addDynamicObject($$);

	  FREESTACK($1);
	  FREESTACK($2);
	  parseQueryTree->removeDynamicObject($3);
  	  FREESTACK($4)
	  FREESTACK($5)
	  FREESTACK($6)
	  FREESTACK($7)
 	  FREESTACK($8)
	  FREESTACK($9)
	  FREESTACK($10)
	}
/*
	| PROJECT LRPAR generalExp COMMA StringLit COMMA StringLit COMMA StringLit COMMA StringLit RRPAR
	{
	  $$ = new QtProject( (QtOperation *)$3, $5.value, $7.value, $9.value );
	  parseQueryTree->addDynamicObject($$);

	  FREESTACK($1);
	  FREESTACK($2);
	  parseQueryTree->removeDynamicObject($3);
  	  FREESTACK($4)
	  FREESTACK($5)
	  FREESTACK($6)
	  FREESTACK($7)
 	  FREESTACK($8)
	  FREESTACK($9)
	  FREESTACK($10)
	}
*/
	| PROJECT LRPAR generalExp COMMA StringLit COMMA StringLit COMMA StringLit COMMA resampleAlg RRPAR
	{
	  $$ = new QtProject( (QtOperation *)$3, $5.value, $7.value, $9.value, $11 );
	  parseQueryTree->addDynamicObject($$);

	  FREESTACK($1);
	  FREESTACK($2);
	  parseQueryTree->removeDynamicObject($3);
  	  FREESTACK($4)
	  FREESTACK($5)
	  FREESTACK($6)
	  FREESTACK($7)
 	  FREESTACK($8)
	  FREESTACK($9)
	  FREESTACK($10)
	  FREESTACK($12)
	}

	| PROJECT LRPAR generalExp COMMA StringLit COMMA StringLit COMMA StringLit COMMA StringLit
	                           COMMA IntegerLit COMMA IntegerLit RRPAR
	{
	  $$ = new QtProject( (QtOperation *)$3, $5.value, $7.value, $9.value, $11.value, $13.svalue, $15.svalue );
	  parseQueryTree->addDynamicObject($$);

	  parseQueryTree->removeDynamicObject($3);
	  FREESTACK($1)  FREESTACK($2)  FREESTACK($4)  FREESTACK($5)  FREESTACK($6)
	  FREESTACK($7)  FREESTACK($8)  FREESTACK($9)  FREESTACK($10) FREESTACK($11)
	  FREESTACK($12) FREESTACK($13) FREESTACK($14) FREESTACK($15) FREESTACK($16)
	}
/*
	| PROJECT LRPAR generalExp COMMA StringLit COMMA StringLit COMMA StringLit COMMA StringLit
	                           COMMA IntegerLit COMMA IntegerLit COMMA resampleAlg RRPAR
	{
	  $$ = new QtProject( (QtOperation *)$3, $5.value, $7.value, $9.value, $11.value, $13.svalue, $15.svalue, $17 );
	  parseQueryTree->addDynamicObject($$);

	  parseQueryTree->removeDynamicObject($3);
	  FREESTACK($1)  FREESTACK($2)  FREESTACK($4)  FREESTACK($5)  FREESTACK($6)
	  FREESTACK($7)  FREESTACK($8)  FREESTACK($9)  FREESTACK($10) FREESTACK($11)
	  FREESTACK($12) FREESTACK($13) FREESTACK($14) FREESTACK($15) FREESTACK($16)
	  FREESTACK($18)
	}
*/
	| PROJECT LRPAR generalExp COMMA StringLit COMMA StringLit COMMA StringLit COMMA StringLit
	                           COMMA IntegerLit COMMA IntegerLit COMMA resampleAlg COMMA FloatLit RRPAR
	{
	  $$ = new QtProject( (QtOperation *)$3, $5.value, $7.value, $9.value, $11.value, $13.svalue, $15.svalue, $17, $19.value );
	  parseQueryTree->addDynamicObject($$);

	  parseQueryTree->removeDynamicObject($3);
	  FREESTACK($1)  FREESTACK($2)  FREESTACK($4)  FREESTACK($5)  FREESTACK($6)
	  FREESTACK($7)  FREESTACK($8)  FREESTACK($9)  FREESTACK($10) FREESTACK($11)
	  FREESTACK($12) FREESTACK($13) FREESTACK($14) FREESTACK($15) FREESTACK($16)
	  FREESTACK($18) FREESTACK($19) FREESTACK($20)
	}
	;

resampleAlg:
  RA_NEAR           { $$ = common::RA_NearestNeighbour; }
| RA_BILINEAR       { $$ = common::RA_Bilinear;         }
| RA_CUBIC          { $$ = common::RA_Cubic;            }
| RA_CUBIC_SPLINE   { $$ = common::RA_CubicSpline;      }
| RA_LANCZOS        { $$ = common::RA_Lanczos;          }
| RA_AVERAGE        { $$ = common::RA_Average;          }
| RA_MODE           { $$ = common::RA_Mode;             }
| MAX_BINARY        { $$ = common::RA_Max;              }
| MIN_BINARY        { $$ = common::RA_Min;              }
| RA_MED            { $$ = common::RA_Med;              }
| RA_QFIRST         { $$ = common::RA_Q1;               }
| RA_QTHIRD         { $$ = common::RA_Q3;               };


trimExp: generalExp mintervalExp           
	{
	  QtDomainOperation *dop = new QtDomainOperation( $2 );
	  dop->setInput( $1 );     // e.g. variable name
	  parseQueryTree->removeDynamicObject( $1 );
	  parseQueryTree->removeDynamicObject( $2 );
	  parseQueryTree->addDynamicObject( dop );
	  $$ = dop;
		$$->setParseInfo( $2->getParseInfo() );
	  if (mflag == MF_IN_CONTEXT)
	    parseQueryTree->addDomainObject( dop );	  
	};    

marray_head:
        MARRAY 
	{ 
	  mflag = MF_IN_CONTEXT; 
	  QueryTree::symtab.initScope(); 
	} 
	iv
	{
          $$ = $3;
	  $$->parseInfo = *($1.info);	  	
	  FREESTACK($1)
	};

mddExp: marray_head VALUES generalExp
	{
	  // create a new list and add the element
	  QtMarrayOp2::mddIntervalListType *dlist = new QtMarrayOp2::mddIntervalListType();
	  dlist->push_back(*($1));

	  // concatenate intervals and variable names, then do a domain rewrite
	  QtMarrayOp2 *qma = new QtMarrayOp2( dlist, $3 );	  	    
          qma->setOldMarray(true);
	  qma->rewriteVars( );

          char *stra = strdup( $1->variable.c_str() );
	  $$ = new QtMarrayOp( stra, $1->tree, qma->getInput());
          parseQueryTree->addCString(stra);

	  QueryTree::symtab.exitScope();
	  mflag = MF_NO_CONTEXT;

	  // release memory
          while (!(dlist->empty())) 
          {
            dlist->erase(dlist->begin());
          } 
          delete dlist;
          delete qma;
          delete $1;
	  parseQueryTree->removeDynamicObject( $3 );
	  parseQueryTree->addDynamicObject( $$ );	  
	  FREESTACK($2)
	}
	| marray_head COMMA ivList VALUES generalExp
	{	  	            
	  // create a new list and add the element
	  QtMarrayOp2::mddIntervalListType *dlist = new QtMarrayOp2::mddIntervalListType();
	  dlist->push_back(*($1));
	  
	  // concatenate the lists
	  dlist->insert( 
	    dlist->end(), 
	    $3->begin(), 
	    $3->end()
	  );
	  	  	 	  	  	  
	  // concatenate intervals and variable names, then do a domain rewrite
	  QtMarrayOp2 *qma = new QtMarrayOp2( dlist, $5 );	    
          qma->setOldMarray(false);
	  qma->rewriteVars( );
	  if (!(qma->concatenateIntervals())) 
	  {	    
	    // TODO: change error code!
	    // save the parse error info and stop the parser
            if ( parseError ) 
	      delete parseError;
            parseError = new ParseInfo( 313, ($1->parseInfo).getToken().c_str(),
                                             ($1->parseInfo).getLineNo(), 
					     ($1->parseInfo).getColumnNo() );
	    QueryTree::symtab.exitScope();
	    mflag = MF_NO_CONTEXT;

	    // release memory
            while (!(dlist->empty())) 
            {
              dlist->erase(dlist->begin());
            } 
            delete dlist;
            delete qma;
            delete $1;
	    parseQueryTree->removeDynamicObject( $5 );
	    FREESTACK($2)	  
	    FREESTACK($4)
            YYABORT;
	  }  	  	  
	  
	  r_Minterval *dinterval = new r_Minterval(qma->greatDomain);
	  std::string      *dvariable = new      std::string(qma->greatIterator); 
	  parseQueryTree->rewriteDomainObjects(dinterval, dvariable, dlist);
	  
          // initialize old good QtMarray with the translated data
	  QtMintervalData *mddIntervalData = new QtMintervalData(*dinterval);
	  $$ = new QtMarrayOp( dvariable->c_str(), new QtConst(mddIntervalData), qma->getInput());
//	  $$->setParseInfo( *($1.info) );
         
	  QueryTree::symtab.exitScope();
	  mflag = MF_NO_CONTEXT;

	  // release memory
          while (!(dlist->empty())) 
          {
            dlist->erase(dlist->begin());
          } 
          delete dlist;
          delete qma;
          delete $1;
	  parseQueryTree->removeDynamicObject( $5 );
	  parseQueryTree->addDynamicObject( $$ );	  
	  FREESTACK($2)	  
	  FREESTACK($4)
	};	

ivList: ivList COMMA iv
	{
	  // add the element and give back the list
	  $1->push_back(*($3));
	  $$ = $1;
          delete $3;
	  FREESTACK($2)
	}
	| iv
	{
	  // create a new list and add the element
	  $$ = new QtMarrayOp2::mddIntervalListType();
	  $$->push_back(*($1));
          delete $1;
	};
			
iv: marrayVariable IN mintervalList
//iv: marrayVariable IN generalExp
	{         
	  if (!QueryTree::symtab.putSymbol($1.value, 1)) // instead of 1 put the dimensionality
	  {	    
	    // save the parse error info and stop the parser
            if ( parseError ) 
	      delete parseError;
            parseError = new ParseInfo( 312, $1.info->getToken().c_str(),
                                             $1.info->getLineNo(), 
					     $1.info->getColumnNo() );
	    parseQueryTree->removeDynamicObject( $3 );
            FREESTACK($2)
	    QueryTree::symtab.wipe();
            YYABORT;
	  }  	  	  
	  $$ = new QtMarrayOp2::mddIntervalType(); 	  
	  $$->variable = $1.value; 
	  $$->tree = $3;
	  $$->parseInfo = *($1.info);	  
	  parseQueryTree->removeDynamicObject( $3 );
	  FREESTACK($2);			   
	};

mddConfiguration: 
	tilingAttributes indexingAttributes
	{
	  $$=new QtMddCfgOp( $1.tilingType, $1.tileSize, $1.borderThreshold, $1.interestThreshold , $1.tileCfg, $1.bboxList,$1.dirDecomp, $2.indexType );
	}
	| indexingAttributes
	{
	  $$=new QtMddCfgOp($1.indexType);
	}
	| tilingAttributes
	{
	  $$=new QtMddCfgOp($1.tilingType, $1.tileSize, $1.borderThreshold, $1.interestThreshold , $1.tileCfg, $1.bboxList,$1.dirDecomp);
	}
	;
indexingAttributes: INDEX indexTypes{$$=$2;};

indexTypes : RC_INDEX
	{
	  $$.info = $1.info;
	  $$.indexType = QtMDDConfig::r_RC_INDEX;
	}
	| TC_INDEX
	{
	  $$.info = $1.info;
	  $$.indexType = QtMDDConfig::r_TC_INDEX;
	}
	| A_INDEX
	{
	  $$.info = $1.info;
	  $$.indexType = QtMDDConfig::r_A_INDEX;
	}
	| D_INDEX
	{
	  $$.info = $1.info;
	  $$.indexType = QtMDDConfig::r_D_INDEX;
	}
	| RD_INDEX
	{
	  $$.info = $1.info;
	  $$.indexType = QtMDDConfig::r_RD_INDEX;
	}
	| RPT_INDEX
	{
	  $$.info = $1.info;
	  $$.indexType = QtMDDConfig::r_RPT_INDEX;
	}
	| RRPT_INDEX
	{
	  $$.info = $1.info;
	  $$.indexType = QtMDDConfig::r_RRPT_INDEX;
	}
	| IT_INDEX
	{
	  $$.info = $1.info;
	  $$.indexType = QtMDDConfig::r_IT_INDEX;
	}
;

tilingAttributes: TILING tileTypes
	{
	  $$=$2;
	}
;

tileTypes: REGULAR tileCfg
	{
	  $$.tilingType = QtMDDConfig::r_REGULAR_TLG;
	  $$.tileCfg = $2.tileCfg;
	  $$.tileSize = static_cast<int>(StorageLayout::DefaultTileSize);
	}
	| REGULAR tileCfg tilingSize
	{
	  $$.tilingType = QtMDDConfig::r_REGULAR_TLG;
	  $$.tileCfg = $2.tileCfg;
	  $$.tileSize = $3.tileSize;
	}
	| ALIGNED tileCfg
	{
	  $$.tilingType = QtMDDConfig::r_ALIGNED_TLG;
	  $$.tileCfg = $2.tileCfg;
	  $$.tileSize = static_cast<int>(StorageLayout::DefaultTileSize);
	}
	| ALIGNED tileCfg tilingSize
	{
	  $$.tilingType = QtMDDConfig::r_ALIGNED_TLG;
	  $$.tileCfg = $2.tileCfg;
	  $$.tileSize = $3.tileSize;
	}
	| DIRECTIONAL dirdecompArray
	{
	  $$.tilingType = QtMDDConfig::r_DRLDECOMP_TLG;
	  $$.tileSize = static_cast<int>(StorageLayout::DefaultTileSize);
	  $$.dirDecomp = $2.dirDecomp;
	}
	| DIRECTIONAL dirdecompArray WITH SUBTILING
	{
	  $$.tilingType = QtMDDConfig::r_DRLDECOMPSUBTILE_TLG;
	  $$.tileSize = static_cast<int>(StorageLayout::DefaultTileSize);
	  $$.dirDecomp = $2.dirDecomp;
	}
	| DIRECTIONAL dirdecompArray tilingSize
	{
	  $$.tilingType = QtMDDConfig::r_DRLDECOMP_TLG;
	  $$.tileSize = $3.tileSize;
	  $$.dirDecomp = $2.dirDecomp;
	}
	| DIRECTIONAL dirdecompArray WITH SUBTILING tilingSize
	{
	  $$.tilingType = QtMDDConfig::r_DRLDECOMPSUBTILE_TLG;
	  $$.tileSize = $5.tileSize;
	  $$.dirDecomp = $2.dirDecomp;
	}
	| AREA OF INTEREST bboxList
	{
	  $$.tilingType = QtMDDConfig::r_AREAOFINTEREST_TLG;
	  $$.bboxList = $4;
	  $$.tileSize = static_cast<int>(StorageLayout::DefaultTileSize);
	}
	| AREA OF INTEREST bboxList tilingSize
	{
	  $$.tilingType = QtMDDConfig::r_AREAOFINTEREST_TLG;
	  $$.bboxList = $4;
	  $$.tileSize = $5.tileSize;
	}
	| AREA OF INTEREST bboxList WITH tileSizeControl
  {
    $$.tilingType = $6;
    $$.bboxList = $4;
    $$.tileSize = static_cast<int>(StorageLayout::DefaultTileSize);
  }
  | AREA OF INTEREST bboxList WITH tileSizeControl tilingSize
  {
    $$.tilingType = $6;
    $$.bboxList = $4;
    $$.tileSize = $7.tileSize;
  }
	| STATISTIC bboxList statisticParameters
	{
	  $$ = $3;
	  $$.bboxList = $2;
	}
	| STATISTIC bboxList
	{
	  $$.tilingType = QtMDDConfig::r_STATISTICS_TLG;
	  $$.bboxList = $2;
	  $$.tileSize = static_cast<int>(StorageLayout::DefaultTileSize);
	}
;
	
tileSizeControl:
  P_NO_LIMIT
  {
	$$ = QtMDDConfig::r_AREAOFINTERESTNOLIMIT_TLG;
  }
  | P_REGROUP
  {
    $$ = QtMDDConfig::r_AREAOFINTERESTREGROUP_TLG;
  }
  | SUBTILING
  {
    $$ = QtMDDConfig::r_AREAOFINTERESTSUBTILING_TLG;
  }
  | P_REGROUP_AND_SUBTILING
  {
    $$ = QtMDDConfig::r_AREAOFINTERESTREGROUPANDSUBTILING_TLG;
  }
;

bboxList: mintervalExp
	{
	  $$ = new QtNode::QtOperationList(1);
	  (*$$)[0] = $1;
	}
	| mintervalExp COMMA bboxList
	{
	  $3->push_back( $1 );
	  $$ = $3;
	}
;

tileCfg: mintervalExp
	{
	  $$.tileCfg=$1;
	}
;

statisticParameters: tilingSize borderCfg interestThreshold
	{
	  $$=$1;
	  $$.tilingType = QtMDDConfig::r_STATISTICSPARAM_TLG;
	  $$.borderThreshold = $2.borderThreshold;
	  $$.interestThreshold = $3.interestThreshold;
	}
	| tilingSize borderCfg
	{
	  $$=$1;
	  $$.borderThreshold = $2.borderThreshold;
	  $$.tilingType = QtMDDConfig::r_STATISTICSPARAM_TLG;
	  $$.interestThreshold = -1;
	}
	| tilingSize interestThreshold
	{
	  $$=$1;
	  $$.interestThreshold = $2.interestThreshold;
	  $$.tilingType = QtMDDConfig::r_STATISTICSPARAM_TLG;
	  $$.borderThreshold=-1;
	}
	| borderCfg interestThreshold
	{
	  $$=$1;
	  $$.interestThreshold = $2.interestThreshold;
	  $$.tilingType = QtMDDConfig::r_STATISTICSPARAM_TLG;
	  $$.tileSize = static_cast<int>(StorageLayout::DefaultTileSize);
	}
	| tilingSize
	{
	  $$=$1;
	  $$.interestThreshold = -1;
 	  $$.borderThreshold = -1;
	  $$.tilingType = QtMDDConfig::r_STATISTICSPARAM_TLG;
	}
	| interestThreshold
	{
	  $$=$1;
	  $$.borderThreshold = -1;
	  $$.tileSize = static_cast<int>(StorageLayout::DefaultTileSize);
	  $$.tilingType = QtMDDConfig::r_STATISTICSPARAM_TLG;
	}
	| borderCfg
	{
	  $$=$1;
	  $$.interestThreshold = -1;
	  $$.tileSize = static_cast<int>(StorageLayout::DefaultTileSize);
	  $$.tilingType = QtMDDConfig::r_STATISTICSPARAM_TLG;
	}
;
	
tilingSize: TILE SIZE intLitExp
	{
	  $$.tileSize = $3.svalue;
	}
;

borderCfg: BORDER THRESHOLD intLitExp
	{
	  $$.borderThreshold = $3.svalue;
	}
;

interestThreshold: INTEREST THRESHOLD floatLitExp
	{
	  $$.interestThreshold = $3.value;
	}
;

dirdecompArray : dirdecomp
	{
	  $$ = $1;
	}
	| dirdecomp COMMA dirdecompArray
	{
	  $$ = $1;
	  for(unsigned int i = 0 ; i < $3.dirDecomp->size() ; ++i)
	  {
	    $$.dirDecomp->push_back($3.dirDecomp->at(i));
	  }
	}
;

dirdecomp : LEPAR dirdecompvals REPAR
	{
	  $$ = $2;
	}
;

dirdecompvals : MULT
	{
	  r_Dir_Decompose temp;
	  if($$.dirDecomp == NULL)
	  {
	    $$.dirDecomp = new std::vector<r_Dir_Decompose>(1);
	    $$.dirDecomp->at(0) = temp;
	  }
	  else
	    $$.dirDecomp->push_back(temp);
	}
	| intArray
	{
	  $$=$1;
	}
;

intArray : intLitExp
	{
	  r_Dir_Decompose temp;
	  temp<<$1.svalue;
	  if($$.dirDecomp == NULL)
	  {
	    $$.dirDecomp = new std::vector<r_Dir_Decompose>(1);
	    $$.dirDecomp->at(0) = temp;
	  }
	else
	    $$.dirDecomp->push_back(temp);
	}
	| intLitExp COMMA intArray
	{
	  $$.dirDecomp = $3.dirDecomp;
	  r_Dir_Decompose temp = $$.dirDecomp->at($$.dirDecomp->size()-1);
	  
	  // the values are inserted in the wrong order when appending,
    // so changed bellow to prepend -- DM 2012-dec-16
    //temp<<$1.svalue;
    $$.dirDecomp->at($$.dirDecomp->size()-1) = temp.prepend($1.svalue);
	}
;

/*--------------------------------------------------------------------
 *				Grammar ends here 
 *--------------------------------------------------------------------
 */	
%%  // C code section

void yyerror(void* /*mflag*/, const char* /*s*/)
{
  if( !parseError ) {

   if( yytext[0] == '\0' ) {
    // unexpected end of query
    parseError = new ParseInfo( 308, yytext, lineNo, columnNo - strlen(yytext) );
   }
   else {
    // general parse error
    parseError = new ParseInfo( 300, yytext, lineNo, columnNo - strlen(yytext) );
   }
  }
}
#pragma GCC diagnostic warning "-Wsign-conversion"
#pragma GCC diagnostic warning "-Wstrict-overflow"
