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

 This class implements partially the \Ref{r_Error} class of the
 C++ binding of ODMG-93 v.1.2. It extends exception
 handling through deriving special classes for MDD specific
 errors.

 In future, \Ref{r_Error} should be derived from the class exception
 defined in the C++ standard.

 The class allows the specification of an error number. The error number
 is used as an index to a generic textual description of the error which
 is read by {\tt setErrorTextOnNumber()}. Error text is loaded from a
 text file by the friend method {\tt initTextTable()} which has to be
 invoked at the beginning of the application. The table can be freed again
 using {\tt freeTextTable()}.
 The parameters in the generic text are substituted using {\tt setTextParameter()}.

 If no error number is specified, the error kind is used as error text.

 Attention: The content of an error object is not supposed to be changed after
 creation because the error text is initialized only in the constructor. Therefore,
 just read methods for error parameters are supported.

 A standard error text file is read by {\tt initTextTable()}. The location and file
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
                r_Error_FileTileStructureInconsistent,
                r_Error_RasFedMessageParsingFailed,

                r_Error_UDFInstallationDirectoryNotDefined,

                r_Error_TileCannotBeLocked,

                r_Error_Conversion
              };

    r_Error();

    /// constructor getting the kind
    explicit r_Error(kind theKindArg, unsigned int newErrorNo = 0);

    /// constructor getting an error number
    explicit r_Error(unsigned int errorno);

    /// constructor getting an error text
    r_Error(const char *what);

    ~r_Error() noexcept override = default;

    const char         *what() const noexcept override;
    virtual const std::string  &what_str() const noexcept;
    kind                get_kind() const;
    unsigned long       get_errorno() const;

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

    /// attribute storing the error description text
    std::string errorText;

    /// attribute storing the number of the error
    unsigned int errorNo{0u};

    /// attribute storing the error kind
    kind theKind{r_Error_General};
};

/// Result is no interval.
class r_Eno_interval : public r_Error
{
public:
    r_Eno_interval();
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
 * result is no cell, e.g. if the cast operator for casting to the base type
 * of class \Ref{r_Marray} is invoked on an object which is not 'zero-dimensional'.
 */
class r_Eno_cell : public r_Error
{
public:
    /// default constructor
    r_Eno_cell();
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
#define RASTYPEUNKNOWN                      209
#define BASETYPENOTSUPPORTED                210
#define RPCCOMMUNICATIONFAILURE             212
#define SYSTEM_COLLECTION_NOT_WRITABLE      216
#define SYSTEM_COLLECTION_HAS_NO_OID        217
#define CONVERSIONFORMATNOTSUPPORTED        218
#define TILESIZETOOSMALL                    219
#define STORAGERLAYOUTINCOMPATIBLEWITHGMARRAY   220
#define DOMAINUNINITIALISED                 221
#define NOTANMARRAYTYPE                     222
#define RCINDEXWITHINCOMPATIBLEMARRAYTYPE   223
#define TILECONFIGMARRAYINCOMPATIBLE        224
#define RCINDEXWITHOUTREGULARTILING         225
#define UDFBODYTOOLARGE                     226
#define POLYGONWRONGPOINTDIMENSION          227
#define POLYGONWRONGINITSTRING              228
#define QUERYPARAMETERINVALID               229
#define ILLEGALARGUMENT                     230
#define MARRAYHASNOBASETYPE                 231
#define INTERVALOPEN                        232
#define INTERVALSWITHDIFFERENTDIMENSION     233
#define TILINGPARAMETERNOTCORRECT           234
#define CONNECTIONCLOSED                    235
#define COMPRESSIONFAILED                   236
#define CLIENTCOMMUICATIONFAILURE           237
#define BASETYPENOTSUPPORTEDBYOPERATION     238
#define OVERLAYPATTERNTOOSMALL              239
#define INSERTINTORCINDEX                   240
#define NOTILINGDEFINED                     241
#define INVALIDFORMATPARAMETER              242
#define TYPEISINUSE                         243
#define STRUCTOFSTRUCTSDISABLED             244
#define UNSATISFIEDMDDCONSTANT              373
#define MDDARGREQUIRED                      405
#define DIVISION_BY_ZERO                    433
#define CELLBINARYOPUNAVAILABLE             434
#define CELLUNARYOPUNAVAILABLE              435
#define GEOMETRYARGREQUIRED                 436
#define COLORPALETTEFORNONCHAR              457
#define NEEDTWOORMOREVERTICES               500
#define INCORRECTPOLYGON                    501
#define FACEDIMENSIONMISMATCH               502
#define VERTEXDIMENSIONMISMATCH             503
#define POLYTOPEDIMENSIONTOOLARGE           504
#define GRIDPOINTSONLY                      505
#define SUBSPACENOINTERSECTION              506
#define POINTDIMENSIONDIFFERS               507
#define GRIDVERTICESNOTCOPLANAR             508
#define NONATURALEMBEDDING                  509
#define SUBSPACEDIMSAMEASMDDOBJ             515
#define CURTAINRANGEINCORRECT               516
#define CURTAINDOMAINDIMENSIONERROR         517
#define CURTAINDOMAININTERSECTERROR         518
#define BRESENHAMSEGMENTMOREVERTICES        519
#define CLIPERRORUNDEFINED                  520
#define CURTAINLINESTRINGDIMENSIONMISMATCH  521
#define ALLPOLYGONSOUTSIDEMDDOBJ            522
#define POLYGONHOLEINEXTERIOR               523
#define SINGLETONPROJECTIONCOORDS           524
#define AXISNUMBERSMUSTEXIST                525
#define PROJDIMNOTMATCHINGMASKDIM           526
#define MASKNOTALIGNEDWITHLINESTRING        527
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
#define INCORRECT_USER_PASSWORD             802
#define NO_PERMISSION_FOR_OPERATION         803
#define CAPABILITY_REFUSED                  804
#define DATABASE_CONNECT_FAILED             830
#define DATABASE_NOTFOUND                   831
#define DATABASE_EXISTS_ALREADY             832
#define COLLECTION_NAME_LENGTH_EXCEEDED     974
#define MARRAY_TYPE_NAME_LENGTH_EXCEEDED    975
#define SET_TYPE_NAME_LENGTH_EXCEEDED       976
#define CELL_TYPE_NAME_LENGTH_EXCEEDED      977
#define DATABASE_INCONSISTENT               1000
#define DATABASE_INCOMPATIBLE               1001
#define ZERO_LENGTH_BLOB                    1002
#define TILE_CONTAINER_NOT_FOUND            1003
#define INDEX_OF_MDD_IS_NULL                1004
#define STORAGE_OF_MDD_IS_NULL              1005
#define UNKNOWN_INDEX_TYPE                  1006
#define ILLEGAL_INDEX_TYPE                  1007
#define COLLTYPE_NULL                       1008
#define MDD_NOT_VALID                       1009
#define MDDTYPE_NULL                        1010
#define ILLEGALSTATEREACHED                 1011
#define COLLECTIONTYPEISNULL                1012
#define TYPENAMEISTOOLONG                   1013
#define INVALIDOBJECTNAME                   1014
#define DATABASE_OPEN                       2000
#define INVALID_OIDTYPE                     2001
#define STRUCTTYPE_ELEMENT_UNKNOWN          2002
#define STRUCTTYPE_ELEMENT_OUT_OF_BOUNDS    2003
#define TRANSIENT_INDEX_USED_AS_PERSISTENT  2004
#define TILE_MULTIPLE_TIMES_RETRIEVED       2005
#define TILE_NOT_INSERTED_INTO_INDEX        2006
#define TRANSIENT_INDEX_OUT_OF_BOUNDS       2007
#define MDD_EXISTS_MULTIPLE_TIMES           2008
#define DATA_NOT_INSERTED_COMPLETELY        2009
#define CONVERSION_RETURNED_WRONG_TYPE      2010
#define COLLECTIONTYPEHASNOELEMENTTYPE      2011
#define MARRAYTYPEHASNOELEMENTTYPE          2012
#define PROPERTYTYPEHASNOELEMENTTYPE        2013
#define SCALARWASPASSEDNULLTYPE             2014
#define INDEXNOTFOUNDINPARENT               2015
#define INDEXEXHAUSTEDAREA                  2016
#define LAYOUTALGORITHMPROBLEM              2017
#define OBJECTDOESNOTSUPPORTSWAPING         2018
#define ERRORDURINGSWAPING                  2019
#define BINARYEXPORTNOTSUPPORTEDFOROBJECT   2020
#define BINARYIMPORTNOTSUPPORTEDFOROBJECT   2021
#define OPERANDSRESULTTYPESNOMATCH          2022
#define TRYINGTOINFERHOOKFROMNULLNODE       2023
#define QTNODETYPEPARENTDOESNOTEXIST        2024
#define TRANSPOSEPARAMETERSINVALID          3001

#endif
