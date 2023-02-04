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
/**
 * SOURCE:   oqlquery.cc
 *
 * MODULE:   rasodmg
 * CLASS:    r_OQL_Query
 * FUNCTION: r_oql_execute()
 *
 * COMMENTS:
 *      None
*/

#include "rasodmg/oqlquery.hh"
#include "rasodmg/database.hh"
#include "rasodmg/transaction.hh"
#include "rasodmg/set.hh"
#include "rasodmg/gmarray.hh"
#include "rasodmg/ref.hh"
#include "rasodmg/iterator.hh"
#include "raslib/error.hh"
#include "raslib/type.hh"

#include "clientcomm/clientcomm.hh"

#include <cstring>
#include <cctype>     // isdigit()
#include <sstream>
#include <cassert>
#include <algorithm>
#include <string>

r_OQL_Query::r_OQL_Query(const char *s)
{
    parameterizedQueryString = new char[strlen(s) + 1];
    strcpy(parameterizedQueryString, s);
    reset_query();
}

r_OQL_Query::r_OQL_Query(const r_OQL_Query &q)
{
    if (q.queryString)
    {
        queryString = new char[strlen(q.queryString) + 1];
        strcpy(queryString, q.queryString);
    }
    if (q.parameterizedQueryString)
    {
        parameterizedQueryString = new char[strlen(q.parameterizedQueryString) + 1];
        strcpy(parameterizedQueryString, q.parameterizedQueryString);
    }
    if (q.mddConstants)
    {
        mddConstants = new r_Set<r_GMarray *>(*(q.mddConstants));
    }
}


r_OQL_Query::~r_OQL_Query()
{
    delete[] queryString;
    queryString = 0;
    delete[] parameterizedQueryString;
    parameterizedQueryString = 0;
    delete mddConstants;
    mddConstants = 0;
}


const r_OQL_Query &
r_OQL_Query::operator=(const r_OQL_Query &q)
{
    if (this != &q)
    {
        // clean up and copy the query string
        if (queryString)
        {
            delete[] queryString;
            queryString = 0;
        }
        if (q.queryString)
        {
            queryString = new char[strlen(q.queryString) + 1];
            strcpy(queryString, q.queryString);
        }

        if (mddConstants)
        {
            delete mddConstants;
            mddConstants = 0;
        }
        if (q.mddConstants)
        {
            mddConstants = new r_Set<r_GMarray *>(*(q.mddConstants));
        }

        // clean up and copy the parameterized query string
        if (parameterizedQueryString)
        {
            delete[] parameterizedQueryString;
            parameterizedQueryString = 0;
        }
        if (q.parameterizedQueryString)
        {
            parameterizedQueryString = new char[strlen(q.parameterizedQueryString) + 1];
            strcpy(parameterizedQueryString, q.parameterizedQueryString);
        }
    }

    return *this;
}

r_OQL_Query &
r_OQL_Query::operator<<(const char *s)
{
    replaceNextArgument(s);
    return *this;
}

r_OQL_Query &
r_OQL_Query::operator<<(r_Char c)
{
    char valueString[2] = {static_cast<char>(c), '\0'};
    replaceNextArgument(valueString);
    return *this;
}

r_OQL_Query &
r_OQL_Query::operator<<(r_Short v)
{
    auto str = std::to_string(v);
    replaceNextArgument(str.c_str());
    return *this;
}


r_OQL_Query &
r_OQL_Query::operator<<(r_UShort v)
{
    auto str = std::to_string(static_cast<int>(v));
    replaceNextArgument(str.c_str());
    return *this;
}


r_OQL_Query &
r_OQL_Query::operator<<(r_Long v)
{
    auto str = std::to_string(v);
    replaceNextArgument(str.c_str());
    return *this;
}


r_OQL_Query &
r_OQL_Query::operator<<(r_ULong v)
{
    auto str = std::to_string(v);
    replaceNextArgument(str.c_str());
    return *this;
}


r_OQL_Query &
r_OQL_Query::operator<<(r_Point pt)
{
    std::ostringstream valueStream;
    valueStream << pt;
    std::string str = valueStream.str();
    replaceNextArgument(str.c_str());
    return *this;
}


r_OQL_Query &
r_OQL_Query::operator<<(r_Sinterval in)
{
    std::ostringstream valueStream;
    valueStream << in;
    std::string str = valueStream.str();
    replaceNextArgument(str.c_str());
    return *this;
}


r_OQL_Query &
r_OQL_Query::operator<<(r_Minterval in)
{
    std::ostringstream valueStream;
    valueStream << in;
    std::string str = valueStream.str();
    replaceNextArgument(str.c_str());
    return *this;
}


r_OQL_Query &
r_OQL_Query::operator<<(r_GMarray &in)
{
    // determine number of next mdd (starting with 0)
    unsigned long mddNo = 0;
    if (mddConstants)
        mddNo = mddConstants->cardinality();

    std::ostringstream valueStream;
    valueStream << "#MDD" << mddNo << "#";
    std::string str = valueStream.str();
    replaceNextArgument(str.c_str());

    // save reference to in
    if (!mddConstants)
        mddConstants = new r_Set<r_GMarray *>();
    mddConstants->insert_element(&in);

    return *this;
}


bool
r_OQL_Query::startsWith(const char *s, const char *prefix) const
{
    if (!s)
        return false;

    assert(prefix);
    
    while (s[0] != '\0' && prefix[0] != '\0')
    {
        if (isspace(s[0]))
            ++s;
        else
        {
            assert(islower(prefix[0]));

            if (tolower(s[0]) != prefix[0])
                return false;
            else
            {
                ++s;
                ++prefix;
            }
        }
    }
    return true;
}

int
r_OQL_Query::is_update_query() const
{
    return !is_retrieval_query() && !is_insert_query();
}

int
r_OQL_Query::is_retrieval_query() const
{
    int returnValue = 0;

    if (parameterizedQueryString)
    {
        // convert string to upper case
        std::string q(parameterizedQueryString);
        std::transform(q.begin(), q.end(), q.begin(), ::tolower);

        // it is retrieval if it's a SELECT but not SELECT INTO expression
        returnValue = (startsWith(q.c_str(), "select") && q.find(" into ") == std::string::npos)
                      || startsWith(q.c_str(), "list") || startsWith(q.c_str(), "define");
    }

    return returnValue;
}


int
r_OQL_Query::is_insert_query() const
{
    return startsWith(parameterizedQueryString, "insert");
}

void
r_OQL_Query::reset_query()
{
    if (queryString)
        delete[] queryString;
    
    queryString = new char[strlen(parameterizedQueryString) + 1];
    strcpy(queryString, parameterizedQueryString);

    if (mddConstants)
    {
        delete mddConstants;
        mddConstants = 0;
    }
}


void
r_OQL_Query::replaceNextArgument(const char *valueString)
{
    char *argumentBegin = NULL;
    char *argumentEnd = NULL;
    char *argumentVal = NULL;

    // locate the next argument in the query string

    argumentBegin = argumentEnd = strchr(queryString, '$');
    if (!argumentBegin)
        throw r_Error(r_Error::r_Error_QueryParameterCountInvalid);
    argumentEnd++;

    //is digit or invalid argument format
    if (!isdigit(*argumentEnd))
        throw  r_Error(QUERYPARAMETERINVALID);

    while (isdigit(*argumentEnd) && *argumentEnd != ' ' && *argumentEnd != '\0')
        argumentEnd++;

    auto argumentLength = argumentEnd - argumentBegin;
    argumentVal    = new char[ argumentLength + 1];
    strncpy(argumentVal, argumentBegin, static_cast<size_t>(argumentLength));
    argumentVal[argumentLength] = '\0';

    while (true)
    {
        auto length = strlen(queryString);
        // allocate a new query string and fill it
        *argumentBegin = '\0';
        std::ostringstream queryStream;

        queryStream << queryString << valueString << argumentEnd;
        delete[] queryString;
        queryString = 0;

        auto tmpStr = queryStream.str();
        queryString = new char[tmpStr.length() + 1];
        memcpy(queryString, tmpStr.c_str(), tmpStr.length());
        queryString[tmpStr.length()] = '\0';

        //update the reference
        auto offset = length + strlen(valueString);
        if (offset > strlen(queryString))
            break;

        argumentEnd = queryString + offset;
        //search again for this parameter
        argumentEnd = argumentBegin = strstr(argumentEnd, argumentVal);

        //end string?
        if (argumentBegin == NULL)
            break;

        //skip $
        argumentEnd++;

        //is digit or invalid argument format
        if (!isdigit(*argumentEnd))
        {
            delete [] argumentVal;
            throw r_Error(QUERYPARAMETERINVALID);
        }

        //skip digits
        while (isdigit(*argumentEnd) && *argumentEnd != ' ' && *argumentEnd != '\0')
            argumentEnd++;
    }

    delete[] argumentVal;
}

void r_oql_execute(r_OQL_Query &query, r_Set<r_Ref_Any> &result, r_Transaction *transaction)
{
    if (transaction == NULL)
        transaction = r_Transaction::actual_transaction;
    if (transaction == NULL || transaction->get_status() != r_Transaction::active)
        throw r_Error(r_Error::r_Error_TransactionNotOpen);

    auto *database = transaction->getDatabase();
    if (database == NULL || database->get_status() == r_Database::not_open)
        throw r_Error(r_Error::r_Error_DatabaseClosed);

    transaction->setDatabase(database);
    database->getComm()->executeQuery(query, result);
    query.reset_query();
}



void r_oql_execute(r_OQL_Query &query, r_Set<r_Ref<r_GMarray>> &result, r_Transaction *transaction)
{
    if (transaction == NULL)
        transaction = r_Transaction::actual_transaction;
    if (transaction == NULL || transaction->get_status() != r_Transaction::active)
        throw r_Error(r_Error::r_Error_TransactionNotOpen);

    auto *database = transaction->getDatabase();
    if (database == NULL || database->get_status() == r_Database::not_open)
        throw r_Error(r_Error::r_Error_DatabaseClosed);

    r_Set<r_Ref_Any> genericSet;
    transaction->setDatabase(database);
    database->getComm()->executeQuery(query, genericSet);

    if (!genericSet.is_empty())
    {
        const r_Type *typeSchema = genericSet.get_element_type_schema();
        if (!typeSchema || typeSchema->type_id() != r_Type::MARRAYTYPE)
            throw r_Error(r_Error::r_Error_TypeInvalid);

        // iterate through the generic set and build a specific one
        result.set_type_by_name(genericSet.get_type_name());
        result.set_type_structure(genericSet.get_type_structure());

        for (auto iter = genericSet.create_iterator(); iter.not_done(); iter++)
            result.insert_element(r_Ref<r_GMarray>(*iter));
    }

    // reset the arguments of the query object
    query.reset_query();
}

// insert query returning OID
void r_oql_execute(r_OQL_Query &query, r_Set<r_Ref_Any> &result, int dummy, r_Transaction *transaction)
{
    if (transaction == NULL)
        transaction = r_Transaction::actual_transaction;
    if (transaction == NULL || transaction->get_status() != r_Transaction::active)
        throw r_Error(r_Error::r_Error_TransactionNotOpen);

    auto *database = transaction->getDatabase();
    if (database == NULL || database->get_status() == r_Database::not_open)
        throw r_Error(r_Error::r_Error_DatabaseClosed);

    transaction->setDatabase(database);
    database->getComm()->executeQuery(query, result, dummy);
    query.reset_query();
}


// update and delete and insert (< v9.1)
void r_oql_execute(r_OQL_Query &query, r_Transaction *transaction)
{
    if (transaction == NULL)
        transaction = r_Transaction::actual_transaction;
    if (transaction == NULL || transaction->get_status() != r_Transaction::active)
        throw r_Error(r_Error::r_Error_TransactionNotOpen);

    auto *database = transaction->getDatabase();
    if (database == NULL || database->get_status() == r_Database::not_open)
        throw r_Error(r_Error::r_Error_DatabaseClosed);

    transaction->setDatabase(database);
    database->getComm()->executeQuery(query);
    query.reset_query();
}

const char *
r_OQL_Query::get_query() const
{
    return static_cast<const char *>(queryString);
}

const r_Set< r_GMarray * > *
r_OQL_Query::get_constants() const
{
    return mddConstants;
}

const char *
r_OQL_Query::get_parameterized_query() const
{
    return static_cast<const char *>(parameterizedQueryString);
}
