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

#ifndef _D_ERROR_
#define _D_ERROR_

//@ManMemo: Module: {\bf raslib}

/*@Doc:

 This class implements partially the r_Error class of the
 C++ binding of ODMG-93 v.1.2. It extends exception
 handling through deriving special classes for MDD specific
 errors.

 In future, r_Error should be derived from the class exception
 defined in the C++ standard.

 The class allows the specification of an error number. The error number
 is used as an index to a generic textual description of the error which
 is read by <tt>setErrorTextOnNumber()</tt>. Error text is loaded from a
 text file by the friend method <tt>initTextTable()</tt> which has to be
 invoked at the beginning of the application. The table can be freed again
 using <tt>freeTextTable()</tt>.
 The parameters in the generic text are substituted using <tt>setTextParameter()</tt>.

 If no error number is specified, the error kind is used as error text.

 Attention: The content of an error object is not supposed to be changed after
 creation because the error text is initialized only in the constructor. Therefore,
 just read methods for error parameters are supported.

 A standard error text file is read by <tt>initTextTable()</tt>. The location and file
 name expected is defined here. Ideally all programs using this mechanism should
 include error.hh to use the same settings.
*/

#include "raslib/mddtypes.hh"
#include <exception>
#include <string>

class r_Error : public std::exception
{
public:

    /// error kinds
    enum kind { r_EGeneral,
                r_Error_General,
                r_Error_DatabaseClassMismatch,
                r_Error_DatabaseClassUndefined,
                r_Error_DatabaseClosed,
                r_Error_DatabaseOpen,
                r_Error_DateInvalid,
                r_Error_IteratorExhausted,
                r_Error_NameNotUnique,
                r_Error_QueryParameterCountInvalid,
                r_Error_QueryParameterTypeInvalid,
                r_Error_RefInvalid,
                r_Error_RefNull,
                r_Error_TimeInvalid,
                r_Error_TimestampInvalid,
                r_Error_TransactionOpen,
                r_Error_TransactionNotOpen,
                r_Error_TypeInvalid,
                r_Error_FileNotFound,
                r_Error_OIdInvalid,
                r_Error_OIdNotUnique,

                r_Error_DatabaseUnknown,
                r_Error_TransferFailed,
                r_Error_HostInvalid,
                r_Error_ServerInvalid,
                r_Error_RpcInterfaceIncompatible,
                r_Error_ClientUnknown,
                r_Error_ObjectUnknown,
                r_Error_ObjectInvalid,

                r_Error_QueryExecutionFailed,
                r_Error_BaseDBMSFailed,
                r_Error_CollectionElementTypeMismatch,
                r_Error_CreatingOIdFailed,
                r_Error_TransactionReadOnly,

                r_Error_LimitsMismatch,
                r_Error_NameInvalid,
                r_Error_FeatureNotSupported,
                // used for subclasses which can be serialised
                // as strings for client / server transfer
                r_Error_SerialisableException,

                r_Error_AccesDenied,
                r_Error_SystemOverloaded,

                r_Error_MemoryAllocation,

                r_Error_InvalidOptimizationLevel,
                r_Error_InvalidBoundsStringContents,
                r_Error_RuntimeProjectionError,
                r_Error_InvalidSourceCRS,
                r_Error_InvalidTargetCRS,
                r_Error_InvalidProjectionResultGridExtents,
                r_Error_FileTileStructureInconsistent,
                r_Error_RasFedMessageParsingFailed,

                r_Error_UDFInstallationDirectoryNotDefined,

                r_Error_TileCannotBeLocked,

                r_Error_Conversion,
                r_Error_RasfedConnectionFailed,
                r_Error_RasfedConnectionTimeout,
                r_Error_RasfedUnknownPeerHostname
              };

    r_Error();

    /// constructor getting the kind
    explicit r_Error(kind theKindArg, unsigned int newErrorNo = 0);

    /// constructor getting the kind and additional error details
    explicit r_Error(kind theKindArg, std::string errorParam);

    /// constructor getting an error number
    explicit r_Error(unsigned int errorno);

    /// constructor getting an error number and additional error details
    explicit r_Error(unsigned int errorno, std::string errorDetails);

    /// constructor getting an error text
    r_Error(const char *what);

    ~r_Error() noexcept override = default;

    const char         *what() const noexcept override;
    virtual const std::string  &what_str() const noexcept;
    kind                get_kind() const;
    unsigned long       get_errorno() const;
    const std::string  &get_errorparam() const;

    void set_what(const char* what);

    /// used to transfer exceptions of kind r_Error_SerialisableException to the client.
    virtual std::string serialiseError() const;

    /// replace the specified parameter by the integer value
    void setTextParameter(const char *parameterName, long long value);

    /// replace the specified parameter by the string value
    void setTextParameter(const char *parameterName, const char *value);

    /// read error text file into text table
    static void initTextTable();

protected:
    /// set error text according to the actual error kind
    void setErrorTextOnKind();

    /// set error text according to the actual error number
    void setErrorTextOnNumber();

    /// The virtual method is redefined in each subclass which supports text parameters.
    /// Usually it is invoked in the constructor of the subclass.
    virtual void resetErrorText();

    /// Update the standard error text with the extra error information if any was
    /// specified.
    void updateWithErrorDetails();

    /// attribute storing the error description text
    std::string errorText;

    /// attribute storing the number of the error
    unsigned int errorNo{0u};

    /// attribute storing the error kind
    kind theKind{r_Error_General};

    /// additional information for errors that can be parameterized
    std::string errorDetails;
};

/// General error with no error number/kind.
class r_EGeneral : public r_Error
{
public:
    r_EGeneral(const std::string &errorText);
};

/// The specified index is not within the bounds.
class r_Eindex_violation : public r_Error
{
public:
    /// constructor getting lower and upper bound, and the index
    r_Eindex_violation(r_Range dlow, r_Range dhigh, r_Range dindex);
    r_Eindex_violation(r_Range dlow, r_Range dhigh, r_Range dindex, const std::string &details);
protected:
    /// reset error text
    void resetErrorText() override;
private:
    /// lower bound
    r_Range low;
    /// upper bound
    r_Range high;
    /// index which caused the error
    r_Range index;
};

/// dimensions of two objects do not match.
class r_Edim_mismatch : public r_Error
{
public:
    /// constructor getting two dimensionalities
    r_Edim_mismatch(r_Dimension pdim1, r_Dimension pdim2);
    r_Edim_mismatch(r_Dimension pdim1, r_Dimension pdim2, const std::string &details);
protected:
    /// reset error text
    void resetErrorText() override;
private:
    /// first dimensionality
    r_Dimension dim1;
    /// second dimensionality
    r_Dimension dim2;
};

/**
 * initialization overflow occurred, e.g. if the
 * stream input operator is invoked more often than the object has dimensions.
 */
class r_Einit_overflow : public r_Error
{
public:
    /// default constructor
    r_Einit_overflow();
};

/**
 * The class is used for errors occuring through query execution. In most cases, the position which
 * caused the error can be fixed. This position is specified by line number, column number, and
 * the token which is involved. Additionally, the class is generic concerning the error type.
 * Different error types can be specified by stating the error number.
 */
class r_Equery_execution_failed : public r_Error
{
public:
    /// default constructor
    r_Equery_execution_failed(unsigned int errorno, unsigned int lineno,
                              unsigned int columnno, const char *token);

    unsigned int get_lineno() const;
    unsigned int get_columnno() const;
    const char  *get_token() const;
protected:
    /// reset error text
    void resetErrorText() override;
private:
    /// line number in which the error is caused
    unsigned int lineNo;
    /// column number which caused the error or is near to the error position
    unsigned int columnNo;
    /// token which caused the error or is near to the error position
    std::string  token;
};

/// The limits reported on the same array by two sources do not match (at least in one end).
class r_Elimits_mismatch : public r_Error
{
public:
    /// constructor getting two limits on the same interval
    r_Elimits_mismatch(r_Range lim1, r_Range lim2);
protected:
    /// reset error text
    void resetErrorText() override;
private:
    /// first interval
    r_Range i1;
    /// second interval
    r_Range i2;
};

/// Lower bound > upper bound.
class r_Einvalid_interval_bounds : public r_Error
{
public:
    r_Einvalid_interval_bounds(r_Range lim1, r_Range lim2);
protected:
    void resetErrorText() override;
private:
    r_Range lim1;
    r_Range lim2;
};

/**
  This class represents an error in the base DBMS. It stores the error
  number in the base DBMS and the error text of the base DBMS. The
  interpretation of the error is specific for the base DBMS. The
  errtxt mechanism of RasDaMan is not used, instead what() returns the
  error of the base DBMS.
*/
class r_Ebase_dbms : public r_Error
{
public:
    r_Ebase_dbms();
    r_Ebase_dbms(long newDbmsErrNum, const char *newErrTxt);
protected:
    /// reset error text
    void resetErrorText() override;
private:
    /// error number of the base DBMS.
    long dbmsErrNum;
    /// error text of the base DBMS.
    std::string dbmsErrTxt;
};

class r_Eno_permission : public r_Error
{
public:
    r_Eno_permission();
};

class r_Ecapability_refused : public r_Error
{
public:
    r_Ecapability_refused();
};

class r_Ememory_allocation: public r_Error
{
public:
    r_Ememory_allocation();
};

// ----------------------------------------------------------------------------------------------
// constants for errors in bin/errtxts
// ----------------------------------------------------------------------------------------------


#define MEMMORYALLOCATIONERROR              66
#define INTERNALDLPARSEERROR                100
#define NOPOINT                             200
#define NOINTERVAL                          201
#define INDEXVIOLATION                      202
#define DIMENSIONMISMATCH                   203
#define DIMOVERFLOW                         204
#define RESULTISNOCELL                      205
#define BASEDBMSERROR                       206
#define INTERNALCLIENTEXCEPTION             207//not used
#define ACCESSTYPEINCOMPATIBLEWITHBASETYPE  208//not used
#define RASTYPEUNKNOWN                      209
#define BASETYPENOTSUPPORTED                210
#define DATABASE_CLOSED                     211
#define RPCCOMMUNICATIONFAILURE             212//not used
#define URLFORMATWRONG                      213//not used

#define ILLEGALJAVALONGVALUE                214//not used
#define ILLEGALJAVAINTVALUE                 215//not used

#define SYSTEM_COLLECTION_NOT_WRITABLE      216
#define SYSTEM_COLLECTION_HAS_NO_OID        217//not used
#define CONVERSIONFORMATNOTSUPPORTED        218
#define TILESIZETOOSMALL                    219
#define STORAGERLAYOUTINCOMPATIBLEWITHGMARRAY   220
#define DOMAINUNINITIALISED                 221
#define NOTANMARRAYTYPE                     222
#define RCINDEXWITHINCOMPATIBLEMARRAYTYPE   223
#define TILECONFIGMARRAYINCOMPATIBLE        224
#define RCINDEXWITHOUTREGULARTILING         225
#define UDFBODYTOOLARGE                     226//not used - also not in bin/errtxts
#define POLYGONWRONGPOINTDIMENSION          227
#define POLYGONWRONGINITSTRING              228
#define QUERYPARAMETERINVALID               229
#define ILLEGALARGUMENT                     230
#define MARRAYHASNOBASETYPE                 231//not used
#define INTERVALOPEN                        232
#define INTERVALSWITHDIFFERENTDIMENSION     233
#define TILINGPARAMETERNOTCORRECT           234
#define CONNECTIONCLOSED                    235
#define COMPRESSIONFAILED                   236//not used
#define CLIENTCOMMUICATIONFAILURE           237//not used
#define BASETYPENOTSUPPORTEDBYOPERATION     238
#define OVERLAYPATTERNTOOSMALL              239
#define INSERTINTORCINDEX                   240
#define NOTILINGDEFINED                     241//not used
#define INVALIDFORMATPARAMETER              242
#define TYPEISINUSE                         243
#define STRUCTOFSTRUCTSDISABLED             244
#define SERVEROCCUPIEDWITHOTHERCLIENT       245

#define INVALIDINTERVALBOUNDS               246
#define MISMATCHINGMINTERVALS               247
#define INVALIDOFFSETINMINTERVAL            248

//300 -303
#define PARSER_UNEXPECTEDTOKEN              300//used
#define PARSER_MDDCELLTYPEMUSTBEUNIFORM     301//used
#define PARSER_CELLNUMMISMATCHWITHSDOM      302//used
#define PARSER_OIDINVALID                   303//used

//308 -314
#define PARSER_UNEXPECTEDQUERYEND           308
#define PARSER_UNKNOWNERROR                 309
#define LEXICALANALYSIS_UNEXPECTEDTOKENCHAR     310//not used
#define PARSER_COMPLEXCONSTRUCTORTYPEMISMATCH   311
#define PARSER_VARIABLEALREADYDEFINED       312//used - but incorrectly in: (oql.yy; mintervalExp)
#define PARSER_ONLYCONSTANTBOUNDSALLOWED    313//used - but not sure if correctly (oql.yy; mddExp)
#define PARSER_TOOFEWARGUMENTS              314//not used

#define SORT_NUMBEROFRANKSMISMATCH          315
#define AXIS_OUTOFBOUNDS                    316

//330 -333
#define PREPROCESSING_UNEXPECTEDTOKEN       330//not used
#define PREPROCESSING_FUNCTIONREDEFINE      331//not used
#define PREPROCESSING_WRONGNUMBEROFARGUMENTS    332//not used
#define PREPROCESSING_FUNCTIONNAMEUNQUALIFIED   333//not used

//343 -372
#define BININDUCE_SDOM_MISMATCH             343//used
#define DOMAINOP_SUBSETOUTOFBOUNDS          344//used
#define DOMAINOP_TOOMANYAXES                345//used
#define DOMAINOP_AXESNAMESMUSTBEUNIQUE      346//used
#define DOMAINOP_INVALIDAXISNAME            347//used
#define REMOTEOBJECTNOTUSED                 348//not used
#define OPERANDOUTOFRANGE                   349//not used
#define GENERALEXECUTIONERROR               350//used
#define RANGE_DOMAINSINCOMPATIBLE           351//used
#define CONCAT_OPERANDTYPESINCOMPATIBLE     352//used
#define QUANTIFIEROPERANDNOTMULTIDIMENSIONAL    353//used
#define OPS_QUANTIFIEROPERANDNOTBOOLEAN     354//used
//355
//in file QtInsert.cc this is used incorrectly. line 190. i kept err 355. no other suitable err code found.
#define COLLECTIONNAMEUNKNOWN               355//used
#define DOMAINDOESNOTINTERSECT              356//used
#define VARIABLEUNKNOWN                     357//used
#define PROJECTION_WRONGOPERANDTYPE         358//not used
#define WHERECLAUSE_RESULTNOTBOOLEAN        359//used
#define OPERANDTYPENOTSUPPORTED             360//used
#define MULTIPLEQUERYTARGETSNOTSUPPORTED    361//used
#define DIMENSIONALITYMISMATCH              362//used
//363 and 434 duplicated (same err msg)
#define BININDUCE_BASETYPESINCOMPATIBLE     363//used
#define BININDUCE_BASETYPEANDSCALARTYPEINCOMPATIBLE   364//used
#define SCALARTYPESINCOMPATIBLE             365//not used
//366 and 435 duplicated (same err msg)
#define UNARY_INDUCE_BASETYPENOTSUPPORTED   366//used
#define UNARY_SCALARTYPENOTSUPPORTED        367//used
#define UNARY_INDUCE_BASETYPEMUSTBECOMPLEX  368//used
#define SCALARTYPEMUSTBECOMPLEX             369//not used
#define UNARY_INDUCE_STRUCTSELECTORINVALID  370//used
#define QT_SELECTSTATEMENTMISSING           371//used
#define QT_UPDATEQUERYINCOMPLETE            372//used
#define UNSATISFIEDMDDCONSTANT              373//used

//374 -376
#define OPS_OPERANDMISSING                  374//used
#define OPS_ONEBOOLEXPECTED                 375//used
//replace in ops.cc. using this, instead of 377
#define OPS_TWOBOOLSEXPECTED                376//used

//379 -391
#define INFO_PRINTTILESNOTSUPPORTED         379//used
#define CONVERSION_FORMATINCOMPATIBLE       380//used
#define CONVERSION_CONVERTORERROR           381//used
#define CONVERSION_FORMATUNKNOWN            382//used
#define OID_PARAMETERINVALID                383//used
//duplicated with 303?
#define OID_OIDINVALID                      384//used
//385
//FIX - used 3 times incorrectly (qtunaryinduce.cc). no other suitable err code found to replace them.
#define STRINGSNOTSUPPORTED                 385//used
#define OIDBASENAMEMISMATCH                 386//used
#define OIDSYSTEMNAMEMISMATCH               387//not used
#define INTERVAL_BOUNDINVALID               388//used
//389
//double check ERR 388 differences (seems the same).
#define INTERVAL_INVALID                    389//used
#define MINTERVAL_DIMENSIONINVALID          390//used
#define DOMAINOP_SPATIALOPINVALID           391//used


//393 -404
#define LOHI_ARGUMENTNOTINTERVAL            393//used
#define LOHI_OPENBOUNDNOTSUPPORTED          394//used
#define SDOM_WRONGOPERANDTYPE               395//used
#define SELECT_WRONGOPERANDTYPE             396//used
#define MINTERVALSEL_WRONGOPERANDTYPE       397//used
#define MINTERVALSEL_INDEXVIOLATION         398//used
#define POINTSEL_WRONGOPERANDTYPE           399//used
#define MDDCONSTRUCTOR_DOMAINUNDEFINED      400//used
#define DOMAINEVALUATIONERROR               401//used
#define PROJECTEDCELLUNDEFINED              402//not used
#define BINARYOP_WRONGOPERANDTYPES          403//used
#define CELLEXP_WRONGOPERANDTYPE            404//used


#define MDDARGREQUIRED                      405//used
//406 -413
//NEEDS FIX - QtExtend::optimizeLoad() needs to change err codes.
#define SHIFT_POINTREQUIRED                 406//used
#define SHIFT_DIMENSIONALITYMISMATCH        407//used
#define SHIFT_CONSTEXPREQUIRED              408//used
#define SHIFT_OPENBOUNDSINCOMPATIBLE        409//used
//410 //NEEDS FIX - why used in qtgeometryop.cc? -left unchanged.
#define POINTEXP_WRONGOPERANDTYPE           410//used
#define POINTSEL_INDEXVIOLATION             411//used
#define VALUEEXP_WRONGOPERANDTYPE           412//used
#define CONDITIONEXP_WRONGOPERANDTYPE       413//used


//415 -429
#define COUNTCELLS_WRONGOPERANDTYPE         415//used
#define SCALE_MDDARGREQUIRED                416//used
#define SCALE_INDICATORINVALID              417//used
//418
//in qtbinaryfunc.cc: QtScale::evaluate() err code 418 is used incorrectly. needs its own code.
//suggested as exists: "dimensionalities of MDD and scale expression are not matching."
#define BIT_WRONGOPERANDTYPE                418//used
#define DOMAINSCALEFAILED                   419//used
#define EXTEND_OPENBOUNDSNOTSUPPORTED       420//used
#define EXTEND_TARGETINTERVALINVALID        421//used
#define EXTEND_MINTERVALREQUIRED            422//used
#define CONCAT_WRONGOPERANDTYPES            423//used
#define CONCAT_DIMENSIONMISMATCH            424//used
#define CONCAT_MINTERVALSNOTMERGEABLE       425//used
#define CASE_DOMAINMISMATCH                 426//used
#define CASE_TILINGMISMATCH                 427//used
//induced case
#define CASE_INDUCED_NOTBOOLEANMDDCONDITION     428//used
#define CASE_NOTSCALARORMDDRESULT           429//used
#define CASE_RESULTTYPESINCOMPATIBLE        430//used
//not induced case
#define CASE_NOTBOOLEANCONDITION            431//used
//was 430 - now 458
#define INFO_OPERANDNOTPERSISTENT           458//used
//was 431 - now 459
#define INFO_PERSISTENTOBJINVALID           459//used
#define INFO_OBJINFOFAIL                    432//used

#define DIVISION_BY_ZERO                    433//used
//434 - used wrongly in QtUpdate::evaluateTuple()? maybe change there to 952.
#define CELLBINARYOPUNAVAILABLE             434//used
#define CELLUNARYOPUNAVAILABLE              435//used
#define GEOMETRYARGREQUIRED                 436//used

//437 -456
//FIX - 436 and 447 are redundant in bin/errtxts file. kept for now.
//FIX - UDF err codes probably not necessary in community edition.
#define UDF_OPTIONSFORMATINVALID            437
#define UDF_UNKNOWNOPERANDTYPE              438
#define UDF_UNKNOWNCOMMANDTYPE              439
#define UDF_ALREADYEXISTS                   440
#define UDF_FUNCTIONNOTFOUND                441
#define UDF_ARGUMENTTYPEMISMATCH            442
#define UDF_ARGUMENTSNUMBERMISMATCH         443
#define SHAREDLIB_COMPILEFAIL               444
#define SHAREDLIB_FAILEDOPEN                445
#define UDF_FUNCTIONMISSING                 446
#define GEOMETRYARGREQUIRED_2               447
#define UDF_GENERALEXECUTIONERROR           448

#define INFO_TILEINFOSVGEXPORTERROR         449
#define CONDENSE_OPERATORINVALID            450
#define CONDENSE_OPERATORINVALIDFORCONSTTYPE    451//not used
#define PROJECT_X_INVALID                   452
#define PROJECT_Y_INVALID                   453
#define PROJECT_XY_INVALID                  454
#define OPS_COMPLEXTYPENOTSUPPORTED         455
#define OPS_MORETHANONEOPERANDEXPECTED      456

#define COLORPALETTEFORNONCHAR              457

#define SORT_RANKSOPINVALID                 460

#define FEATURENOTSUPPORTED                 499

#define NEEDTWOORMOREVERTICES               500
#define INCORRECTPOLYGON                    501
#define FACEDIMENSIONMISMATCH               502//not used
#define VERTEXDIMENSIONMISMATCH             503
#define POLYTOPEDIMENSIONTOOLARGE           504//not used
#define GRIDPOINTSONLY                      505
#define SUBSPACENOINTERSECTION              506
#define POINTDIMENSIONDIFFERS               507
#define GRIDVERTICESNOTCOPLANAR             508
#define NONATURALEMBEDDING                  509
#define SUBSPACEDIMSAMEASMDDOBJ             515
#define CURTAINRANGEINCORRECT               516//not used
#define CURTAINDOMAINDIMENSIONERROR         517//not used
#define CURTAINDOMAININTERSECTERROR         518
#define BRESENHAMSEGMENTMOREVERTICES        519//not used
#define CLIPERRORUNDEFINED                  520
#define CURTAINLINESTRINGDIMENSIONMISMATCH  521//not used
#define ALLPOLYGONSOUTSIDEMDDOBJ            522
#define POLYGONHOLEINEXTERIOR               523
#define SINGLETONPROJECTIONCOORDS           524
#define AXISNUMBERSMUSTEXIST                525
#define PROJDIMNOTMATCHINGMASKDIM           526
#define MASKNOTALIGNEDWITHLINESTRING        527
#define LINESTRINGDIFFERENTPOINTS           528


//510 -511
#define FUNCTIONARGUMENTOVERFLOW            510//not used
#define FUNCTIONRESULTOVERFLOW              511//not used


//700 -707 - ALL UNUSED
#define ADMIN_GENERALDBCREATIONERROR        700
#define ADMIN_CREATETABLEFAIL_SCHEMA        701
#define ADMIN_INSERTTABLEFAIL_COUNTERS      702
#define ADMIN_CREATETABLEFAIL_BLOB          703
#define ADMIN_CREATEINDEXFAIL_INDEX         704
#define ADMIN_INSERTTABLEFAIL_BTN           705
#define ADMIN_CREATETABLEFAIL_DEFAULT       706
#define ADMIN_COMMITFAIL                    707

//700 -722 - ALL UNUSED
#define DATABASE_EXISTS                     708
#define FILEDATADIR_NOTFOUND                709
#define FAILEDWRITINGTODISK                 710
#define FAILEDCREATINGDIR                   711
#define FAILEDOPENFORWRITING                712
#define BLOBFILENOTFOUND                    713
#define FAILEDOPENFORREADING                714
#define FAILEDREADINGFROMDISK               715
#define FAILEDREMOVINGFILE                  716
#define FILEDATADIR_NOTWRITABLE             717
#define FAILEDOPENFORUPDATING               718
#define FAILEDIOOPERATION                   719
#define EMPTYBLOBFILE                       720
#define FILEDATADIR_NOTABSOLUTE             721
#define FILENAMETOBLOBIDFAILED              722


//800 -804
#define RASMANAGER_RASSERVERCONNECTIONFAIL  800//not used
#define RASMANAGER_SYSTEMOVERLOAD           801//not used
#define INCORRECT_USER_PASSWORD             802//not used
#define NO_PERMISSION_FOR_OPERATION         803//used
#define CAPABILITY_REFUSED                  804//used

//805 -808 - ALL UNUSED
#define RASMANAGER_NOSUITABLESERVERSSTARTED     805
#define RASMANAGER_WRITEINPROGRESS          806
#define RASMANAGER_DBUNKNOWN                807
#define RASMANAGER_FORMATERROR              808

//820 -822 - ALL UNUSED
#define RNP_CLIENTIDEXPECTED                820
#define RNP_CLIENTIDINVALID                 821
#define RNP_CLIENTREQUEST_UNKNOWNCMD        822

//830 -832 - ALL USED
#define DATABASE_CONNECT_FAILED             830
#define DATABASE_NOTFOUND                   831
#define DATABASE_EXISTS_ALREADY             832

//900 -909 - ALL UNUSED
#define TYPEDEF_UNSUPPORTEDTYPE             900
#define TEMPLATETYPE_REFERENCEEXPECTED      901
#define TYPEREFERENCE_NOTFOUND              902
#define MDDBASETYPE_INVALID                 903
#define MDDTYPE_DOMAINSPEC_MISSING          904
#define STRUCT_TYPENAME_ALREADYEXISTS       905
#define MDDTYPE_NAME_ALREADYEXISTS          906
#define SETTYPE_NAME_ALREADYEXISTS          907
#define QUERY_OVERLAPPINGTILES              908
#define DOMAINSNOTMERGEABLE                 909//similar to 425


//950 -973 - ALL USED except tagged unused
//these are grouped under 'update error' but not all are for the update operation

#define UPDATE_TARGET_ITERATOREXPECTED      950
//951
//also used for QtDelete. maybe should have its own err code.
#define UPDATE_SOURCE_INVALID               951
#define UPDATE_BASETYPEMISMATCH             952
#define UPDATE_DOMAIN_INCOMPATIBLE          953
#define UPDATE_TARGETEXP_INVALID            954
#define COMMAND_COLLNAME_ALREADYEXISTS      955
#define COMMAND_COLLTYPE_UNKNOWN            956
#define COMMAND_COLLNAME_UNKNOWN            957
#define OID_NEWOIDALLOCATIONFAILED          958
#define MDDANDCOLLECTIONTYPESINCOMPATIBLE   959
#define INSERT_INVALIDTYPE                  960
#define UPDATE_DOMAIN_INVALIDTYPE           961
#define UPDATE_INVALIDNUMBEROFINTERVALS     962
#define UPDATE_INVALIDDIMENSIONALITY        963
#define TYPENOTPERSISTENT                   964//not used
#define MDDTYPEUNKNOWN                      965
#define MDDTYPEMISSING                      966
#define UPDATE_DOMAININVALID                967
//968
//difference with 965? this one considers types of any type(mdd, set, cell)
//err. 965 i assume is for mddtypes only.
#define TYPE_NAMEUNKNOWN                    968
#define TYPE_ALREADYEXISTS                  969
#define ATTRIBUTETYPE_INVALID               970
#define CELLTYPE_INVALID                    971
#define DIMENSIONALITY_INVALID              972
#define MARRAYTYPE_INVALID                  973

//all used properly
#define COLLECTION_NAME_LENGTH_EXCEEDED     974
#define MARRAY_TYPE_NAME_LENGTH_EXCEEDED    975
#define SET_TYPE_NAME_LENGTH_EXCEEDED       976
#define CELL_TYPE_NAME_LENGTH_EXCEEDED      977
#define REFERENCED_FILE_NOT_FOUND           993
#define REFERENCED_FILE_EMPTY               994
#define EXPAND_POSITION_INVALID             995
#define EXPAND_DIRECTION_INVALID            996
//997 - same as 974? -ignored.
#define COLLECTION_NAME_TOOLONG             997
//998 - use similar to 993? -ignored.
#define REFERENCEDFILE_NOTFOUND             998
#define REFERENCEDPATH_NOTABSOLUTE          999

//all used except marked unused
#define DATABASE_INCONSISTENT               1000
#define DATABASE_INCOMPATIBLE               1001
#define ZERO_LENGTH_BLOB                    1002//not used
#define TILE_CONTAINER_NOT_FOUND            1003
#define INDEX_OF_MDD_IS_NULL                1004
#define STORAGE_OF_MDD_IS_NULL              1005
#define UNKNOWN_INDEX_TYPE                  1006
#define ILLEGAL_INDEX_TYPE                  1007
#define COLLTYPE_NULL                       1008
#define MDD_NOT_VALID                       1009
#define MDDTYPE_NULL                        1010
#define ILLEGALSTATEREACHED                 1011//not used
#define COLLECTIONTYPEISNULL                1012
#define TYPENAMEISTOOLONG                   1013
#define INVALIDOBJECTNAME                   1014
#define FEATURENOTENABLED                   1015

#define DATABASE_OPEN                       2000
#define INVALID_OIDTYPE                     2001
#define STRUCTTYPE_ELEMENT_UNKNOWN          2002
#define STRUCTTYPE_ELEMENT_OUT_OF_BOUNDS    2003
#define TRANSIENT_INDEX_USED_AS_PERSISTENT  2004//not used
#define TILE_MULTIPLE_TIMES_RETRIEVED       2005
#define TILE_NOT_INSERTED_INTO_INDEX        2006
#define TRANSIENT_INDEX_OUT_OF_BOUNDS       2007
#define MDD_EXISTS_MULTIPLE_TIMES           2008
#define DATA_NOT_INSERTED_COMPLETELY        2009//not used
#define CONVERSION_RETURNED_WRONG_TYPE      2010//not used
#define COLLECTIONTYPEHASNOELEMENTTYPE      2011
#define MARRAYTYPEHASNOELEMENTTYPE          2012
#define PROPERTYTYPEHASNOELEMENTTYPE        2013
#define SCALARWASPASSEDNULLTYPE             2014
#define INDEXNOTFOUNDINPARENT               2015
#define INDEXEXHAUSTEDAREA                  2016
#define LAYOUTALGORITHMPROBLEM              2017
#define OBJECTDOESNOTSUPPORTSWAPING         2018//not used
#define ERRORDURINGSWAPING                  2019//not used
#define BINARYEXPORTNOTSUPPORTEDFOROBJECT   2020
#define BINARYIMPORTNOTSUPPORTEDFOROBJECT   2021
#define OPERANDSRESULTTYPESNOMATCH          2022
#define TRYINGTOINFERHOOKFROMNULLNODE       2023//not used
#define QTNODETYPEPARENTDOESNOTEXIST        2024

//3000 -3001
//was used as: "E_DEM_EMPTY" in: conversion/dem.hh and dem.cc . replaced.
#define FORMATCONV_DEMAREA_VALUESNULL       3000
#define TRANSPOSEPARAMETERSINVALID          3001//not used

//4000
#define LOCKMANAGER_TILELOCKED              4000//used

//10000
#define INTERNALSERVERERROR                 10000//used

#endif
