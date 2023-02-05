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

#include "error.hh"

#include <sys/stat.h> // for stat
#include <fstream>    // ifstream
#include <string>
#include <sstream>    // istringstream
#include <unordered_map>
#include <mutex>
#include <cassert>

#include <logging.hh>

/// error information
struct ErrorInfo
{
    std::string text;
    char kind;
};

std::unordered_map<unsigned int, ErrorInfo> loadErrorTexts();

r_Error::r_Error()
{
    resetErrorText();
}

r_Error::r_Error(kind theKindArg, unsigned int newErrorNo)
    : errorNo(newErrorNo), theKind(theKindArg)
{
  resetErrorText();
}

r_Error::r_Error(kind theKindArg, std::string errorParamArg)
  : errorNo(0), theKind(theKindArg), errorDetails{std::move(errorParamArg)}
{
  resetErrorText();
}

r_Error::r_Error(unsigned int errorno)
    : errorNo(errorno)
{
    resetErrorText();
}

r_Error::r_Error(unsigned int errorno, std::string errorParamArg)
    : errorNo(errorno), errorDetails{std::move(errorParamArg)}
{
    resetErrorText();
}

r_Error::r_Error(const char *w)
    : errorText{w}, errorNo(0u), theKind(r_EGeneral)
{
}

const char *
r_Error::what() const noexcept
{
    return errorText.c_str();
}

const std::string &
r_Error::what_str() const noexcept
{
    return errorText;
}

r_Error::kind
r_Error::get_kind() const
{
    return theKind;
}

unsigned long
r_Error::get_errorno() const
{
  return errorNo;
}

const std::string &r_Error::get_errorparam() const
{
  return errorDetails;
}

void r_Error::set_what(const char *w)
{
  errorText = w;
}

std::string
r_Error::serialiseError() const
{
    return std::to_string(static_cast<int>(theKind)) + "\t" +
           std::to_string(errorNo);
}

void r_Error::initTextTable()
{
}

void
r_Error::setErrorTextOnKind()
{
    switch (theKind)
    {
    case r_Error_General:
        errorText = "ODMG General";
        break;
    case r_Error_DatabaseClassMismatch:
        errorText = "Database Class Mismatch";
        break;
    case r_Error_DatabaseClassUndefined:
        errorText = "Database Class Undefined";
        break;
    case r_Error_DatabaseClosed:
        errorText = "Database Closed";
        break;
    case r_Error_DatabaseOpen:
        errorText = "Database Open";
        break;
    case r_Error_DateInvalid:
        errorText = "Date Invalid";
        break;
    case r_Error_IteratorExhausted:
        errorText = "Iterator Exhausted";
        break;
    case r_Error_NameNotUnique:
        errorText = "Name Not Unique";
        break;
    case r_Error_QueryParameterCountInvalid:
        errorText = "Query Parameter Count Invalid";
        break;
    case r_Error_QueryParameterTypeInvalid:
        errorText = "Query Parameter Type Invalid";
        break;
    case r_Error_RefInvalid:
        errorText = "Ref Invalid";
        break;
    case r_Error_RefNull:
        errorText = "Ref Null";
        break;
    case r_Error_TimeInvalid:
        errorText = "Time Invalid";
        break;
    case r_Error_TimestampInvalid:
        errorText = "Timestamp Invalid";
        break;
    case r_Error_TransactionOpen:
        errorText = "Transaction Open";
        break;
    case r_Error_TransactionNotOpen:
        errorText = "Transaction Not Open";
        break;
    case r_Error_TypeInvalid:
        errorText = "Type Invalid";
        break;
    case r_Error_DatabaseUnknown:
        errorText = "Database Unknown";
        break;
    case r_Error_TransferFailed:
        errorText = "Transfer Failed";
        break;
    case r_Error_HostInvalid:
        errorText = "Host Invalid";
        break;
    case r_Error_ServerInvalid:
        errorText = "Server Invalid";
        break;
    case r_Error_ClientUnknown:
        errorText = "Client Unknown";
        break;
    case r_Error_FileNotFound:
        errorText =  "Referenced file not found";
        break;
    case r_Error_ObjectUnknown:
        errorText = "Object Unknown";
        break;
    case r_Error_ObjectInvalid:
        errorText = "Object Invalid";
        break;
    case r_Error_QueryExecutionFailed:
        errorText = "Query Execution Failed";
        break;
    case r_Error_BaseDBMSFailed:
        errorText = "Base DBMS Failed";
        break;
    case r_Error_CollectionElementTypeMismatch:
        errorText = "Collection Element Type Mismatch";
        break;
    case r_Error_CreatingOIdFailed:
        errorText = "Creation of OID failed";
        break;
    case r_Error_TransactionReadOnly:
        errorText = "Transaction is read only";
        break;
    case r_Error_LimitsMismatch:
        errorText = "Limits reported to an object mismatch";
        break;
    case r_Error_NameInvalid:
        errorText = "Name Invalid";
        break;
    case r_Error_FeatureNotSupported:
        errorText = "Feature is not supported";
        break;
    case r_Error_AccesDenied:
        errorText = "Access denied";
        break;
    case r_Error_MemoryAllocation:
        errorText = "Memory allocation failed";
        break;
    case r_Error_InvalidOptimizationLevel:
        errorText = "Illegal value for optimization level";
        break;
    case r_Error_Conversion:
        errorText = "Format conversion failed";
        break;
    case r_Error_InvalidBoundsStringContents:
        errorText = "Illegal contents of the string with projection bounds";
        break;
    case r_Error_RuntimeProjectionError:
        errorText = "CRS reprojection failed at runtime";
        break;
    case r_Error_InvalidSourceCRS:
        errorText = "Cannot use source coordinate reference system, as reported by GDAL library";
        break;
    case r_Error_InvalidTargetCRS:
        errorText = "Cannot use target coordinate reference system, as reported by GDAL library";
        break;
    case r_Error_InvalidProjectionResultGridExtents:
        errorText = "Projection output must have width/height > 0";
        break;
    case r_Error_FileTileStructureInconsistent:
        errorText = "Structure of file tile is inconsistent with the original read one";
        break;
    case r_Error_RasFedMessageParsingFailed:
        errorText = "Error while parsing a message from the federation daemon";
        break;
    case r_Error_UDFInstallationDirectoryNotDefined:
        errorText = "UDF Installation Directory not found or inaccessible";
        break;
    case r_Error_RasfedConnectionFailed:
        errorText = "Connection to rasfed failed";
        break;
    case r_Error_RasfedConnectionTimeout:
        errorText = "rasfed request timeout";
        break;
    case r_Error_RasfedUnknownPeerHostname:
        errorText = "Unknown outpeer hostname";
        break;
    default:
        errorText = "Not specified";
        break;
    }
    
    updateWithErrorDetails();
}

std::unordered_map<unsigned int, ErrorInfo> loadErrorTexts()
{
    static const std::string ERRTXTFILE{"errtxts"};
    static const char ERRTXTFILE_COMMENT{'#'};
    static const char ERRTXTFILE_FIELDSEP{'^'};
    static const char ERRTXTFILE_DEFAULTKIND{'E'};

    std::unordered_map<unsigned int, ErrorInfo> errorTexts;

    auto errorFilePath = std::string{SHARE_DATA_DIR} + ERRTXTFILE;

    struct stat buffer;
    if (stat(errorFilePath.c_str(), &buffer) != 0)
    {
        std::cerr << "No error texts file found at: " << errorFilePath << std::endl;
        return errorTexts;
    }

    std::ifstream errorsFile(errorFilePath, std::ios::in);
    if (!errorsFile.good())
    {
        std::cerr << "Failed opening error texts file at: " << errorFilePath << std::endl;
        return errorTexts;
    }

    // general line format:
    //   empty line
    //   # comment
    //   errNo^error text... (errKind = E)
    //   errNo^errKind^error text...
    std::string line;
    bool firstLine{true};
    while (std::getline(errorsFile, line))
    {
        if (line.empty() || line[0] == ERRTXTFILE_COMMENT)
        {
            continue; // skip empty lines and comments
        }
        if (firstLine)
        {
            firstLine = false;
            continue;
        }
        auto separators = std::count(line.begin(), line.end(), ERRTXTFILE_FIELDSEP);
        if (separators != 2 && separators != 1)
        {
            std::cerr << "Invalid line found in " << errorFilePath << ": '" << line
                      << "'; skipping." << std::endl;
            continue;
        }

        // get the tokens
        std::istringstream tokens(line);
        std::string token;
        std::getline(tokens, token, ERRTXTFILE_FIELDSEP);
        int errNo = std::stoi(token);
        std::getline(tokens, token, ERRTXTFILE_FIELDSEP);
        char errKind = ERRTXTFILE_DEFAULTKIND;
        if (separators == 2)
        {
            if (token.size() != 1)
            {
                std::cerr << "Invalid line found in " << errorFilePath << ": '" << line
                          << "'; skipping." << std::endl;
                continue;
            }
            errKind = token[0];
            std::getline(tokens, token, ERRTXTFILE_FIELDSEP);
        }
        else
        {
            // one separator: default errKind = E, last token = errorText
        }

        // and insert
        errorTexts.emplace(errNo, ErrorInfo{std::move(token), errKind});
    }
    return errorTexts;
}

void
r_Error::setErrorTextOnNumber()
{
    static std::unordered_map<unsigned int, ErrorInfo> errorTexts;
    static std::mutex mutex;
    {
        std::lock_guard<std::mutex> guard(mutex);
        if (errorTexts.empty())
        {
            errorTexts = loadErrorTexts();
        }
    }

    if (errorTexts.empty())
    {
        errorText = "no explanation text available - cannot find/load file with standard error messages.";
        return;
    }
    auto it = errorTexts.find(errorNo);
    if (it != errorTexts.end())
    {
        errorText = it->second.text;
    }
    else
    {
        errorText = "no explanation text available for error code " + std::to_string(errorNo);
    }
    
    updateWithErrorDetails();
}

void
r_Error::setTextParameter(const char *parameterName, long long value)
{
    // convert long value to string
    std::stringstream valueStream;
    valueStream << value;
    std::string valueStr = valueStream.str();
    setTextParameter(parameterName, valueStr.c_str());
}

void
r_Error::setTextParameter(const char *parameterName, const char *value)
{
    if (errorText.empty())
    {
        LWARNING << "Cannot set text parameter '" << parameterName << "' to empty error message.";
        return;
    }
    auto nameLen = strlen(parameterName);
    auto valueLen = strlen(value);
    size_t nextIndex{};
    while (true)
    {
        nextIndex = errorText.find(parameterName, nextIndex);
        if (nextIndex == std::string::npos)
        {
            break;
        }
        errorText.replace(nextIndex, nameLen, value);
        nextIndex += valueLen;
    }
}

void
r_Error::resetErrorText()
{
    if (errorNo)
    {
        setErrorTextOnNumber();
    }
    else
    {
        setErrorTextOnKind();
    }
}

void r_Error::updateWithErrorDetails()
{
  if (errorDetails.empty())
  {
      return; // no details, nothing to do
  }
  
  if (errorText.empty())
  {
      errorText = errorDetails;
  }
  else
  {
      if (errorText.back() == '.' || errorText.back() == '!')
      {
          errorText[errorText.length() - 1] = ':';
          errorText += ' ';
      }
      else if (errorText.back() == '?')
      {
          errorText += " Details: ";
      }
      else
      {
          errorText += ": ";
      }
      errorText += errorDetails;
  }
}

// ----------------------------------------------------------------------------------------------

r_EGeneral::r_EGeneral(const std::string &errorTextArg)
    : r_Error{errorTextArg.c_str()}
{
}

// ----------------------------------------------------------------------------------------------

r_Eindex_violation::r_Eindex_violation(r_Range dlow, r_Range dhigh, r_Range dindex)
    : r_Error(INDEXVIOLATION), low(dlow), high(dhigh), index(dindex)
{
  resetErrorText();
}

r_Eindex_violation::r_Eindex_violation(r_Range dlow, r_Range dhigh, r_Range dindex, const std::string &details)
  : r_Error(INDEXVIOLATION, details), low(dlow), high(dhigh), index(dindex)
{
    resetErrorText();
}

void
r_Eindex_violation::resetErrorText()
{
    setErrorTextOnNumber();
    setTextParameter("$low",     low);
    setTextParameter("$high",   high);
    setTextParameter("$index", index);
}

// ----------------------------------------------------------------------------------------------

r_Edim_mismatch::r_Edim_mismatch(r_Dimension pdim1, r_Dimension pdim2)
    : r_Error(DIMENSIONMISMATCH), dim1(pdim1), dim2(pdim2)
{
    resetErrorText();
}

r_Edim_mismatch::r_Edim_mismatch(r_Dimension pdim1, r_Dimension pdim2, const std::string &details)
  : r_Error(DIMENSIONMISMATCH, details), dim1(pdim1), dim2(pdim2)
{
    resetErrorText();
}

void
r_Edim_mismatch::resetErrorText()
{
    setErrorTextOnNumber();
    setTextParameter("$dim1", dim1);
    setTextParameter("$dim2", dim2);
}

// ----------------------------------------------------------------------------------------------

r_Einit_overflow::r_Einit_overflow()
    : r_Error(DIMOVERFLOW)
{
    resetErrorText();
}

// ----------------------------------------------------------------------------------------------

r_Equery_execution_failed::r_Equery_execution_failed(
    unsigned int errorno, unsigned int lineno, unsigned int columnno, const char *initToken)
    : r_Error(errorno),
      lineNo(lineno),
      columnNo(columnno),
      token{initToken}
{
    resetErrorText();
}

void
r_Equery_execution_failed::resetErrorText()
{
    setErrorTextOnNumber();
    setTextParameter("$errorNo",  errorNo);
    setTextParameter("$lineNo",   lineNo);
    setTextParameter("$columnNo", columnNo);
    setTextParameter("$token",    token.c_str());
}

unsigned int
r_Equery_execution_failed::get_lineno() const
{
    return lineNo;
}

unsigned int
r_Equery_execution_failed::get_columnno() const
{
    return columnNo;
}

const char *
r_Equery_execution_failed::get_token() const
{
    return token.c_str();
}

// ----------------------------------------------------------------------------------------------

r_Elimits_mismatch::r_Elimits_mismatch(r_Range lim1, r_Range lim2)
    : r_Error(r_Error_LimitsMismatch), i1(lim1), i2(lim2)
{
    resetErrorText();
}

void
r_Elimits_mismatch::resetErrorText()
{
    setErrorTextOnNumber();
    setTextParameter("$dim1", i1);
    setTextParameter("$dim2", i2);
}

// ----------------------------------------------------------------------------------------------

r_Ebase_dbms::r_Ebase_dbms()
    : r_Error(r_Error_SerialisableException, BASEDBMSERROR)
{
}

r_Ebase_dbms::r_Ebase_dbms(long newDbmsErrNum, const char *newDbmsErrTxt)
    : r_Error(r_Error_SerialisableException, BASEDBMSERROR),
      dbmsErrNum(newDbmsErrNum),
      dbmsErrTxt{newDbmsErrTxt}
{
    resetErrorText();
}

void
r_Ebase_dbms::resetErrorText()
{
    errorText = "Base DBMS error " + std::to_string(dbmsErrNum) + ": " + dbmsErrTxt;
}

// ----------------------------------------------------------------------------------------------

r_Eno_permission::r_Eno_permission()
    : r_Error(r_Error_AccesDenied, NO_PERMISSION_FOR_OPERATION)
{
}

// ----------------------------------------------------------------------------------------------

r_Ememory_allocation::r_Ememory_allocation()
    : r_Error(r_Error_MemoryAllocation, MEMMORYALLOCATIONERROR)
{
}

r_Ecapability_refused::r_Ecapability_refused()
    : r_Error(r_Error_AccesDenied, CAPABILITY_REFUSED)
{
}


r_Einvalid_interval_bounds::r_Einvalid_interval_bounds(r_Range l1, r_Range l2)
    : r_Error(INVALIDINTERVALBOUNDS), lim1{l1}, lim2{l2}
{
    resetErrorText();
}

void r_Einvalid_interval_bounds::resetErrorText()
{
    setErrorTextOnNumber();
    setTextParameter("$lim1", lim1);
    setTextParameter("$lim2", lim2);
}
