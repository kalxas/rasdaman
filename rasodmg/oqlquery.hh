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

#ifndef _D_OQL_QUERY_
#define _D_OQL_QUERY_

#include "raslib/odmgtypes.hh"
#include <cstddef>  // for NULL

template <class T>
class r_Set;
template <class T>
class r_Ref;
class r_Ref_Any;
class r_Point;
class r_Sinterval;
class r_Minterval;
class r_Transaction;
class r_GMarray;

//@ManMemo: Module: {\bf rasodmg}
/**
  * \ingroup Rasodmgs
  */

/**
 The global function r_oql_execute() is used to invoke RasML
 queries. The query statement is represented through an object
 of class r_OQL_Query which is the first argument of the function.
 The constructor gets a parameterized query string where `$i`
 indicates the i-th parameter. The overloaded stream input
 operators allows to insert the parameter values to the query, at
 the same time preserving their respective types. If any of the
 `$i` are not followed by a right operant construction argument at
 the point r_oql_execute is called, a r_Error exception object
 of kind r_Error_QueryParameterCountInvalid is thrown.
 Once a query has been executed via r_oql_execute(), the arguments
 associated with the `$i` parameters are cleared and new arguments
 must be supplied.

 The copy constructor and assignment operator copy all the underlying
 data structures associated with the query, based upon the parameters
 that have been passed to the query at the point the operation is
 performed.

 The stream operators raise a r_Error exception of type
 r_Error_QueryParameterCountInvalid if the number of arguments is
 exceeded.
*/
class r_OQL_Query
{
public:
    /// default constructor
    r_OQL_Query() = default;

    /// constructor getting the query string
    explicit r_OQL_Query(const char *s);

    /// copy constructor
    r_OQL_Query(const r_OQL_Query &q);

    /// destructor
    ~r_OQL_Query();

    /// assignment operator
    const r_OQL_Query &operator=(const r_OQL_Query &q);

    //@Man: Stream input operators for every parameter type:
    //@{
    ///
    r_OQL_Query &operator<<(const char *s);
    ///
    r_OQL_Query &operator<<(r_Char c);
    ///
    r_OQL_Query &operator<<(r_Short s);
    ///
    r_OQL_Query &operator<<(r_UShort us);
    ///
    r_OQL_Query &operator<<(r_Long l);
    ///
    r_OQL_Query &operator<<(r_ULong ul);
    ///
    r_OQL_Query &operator<<(r_Point pt);
    ///
    r_OQL_Query &operator<<(r_Sinterval in);
    ///
    r_OQL_Query &operator<<(r_Minterval in);
    ///
    r_OQL_Query &operator<<(r_GMarray &in);
    ///
    //@}

    /// returns true if the current query is an update / delete one
    int is_update_query() const;

    /// returns true if the current query is an insert one
    int is_insert_query() const;

    /// returns true if the current query is an retrieval query (select)
    int is_retrieval_query() const;

    //@Man: Methods for internal use:
    //@{
    /// resets the expandation of the query string
    void reset_query();
    /// gets the expanded query string
    const char *get_query() const;
    /// get mdd constants
    const r_Set<r_GMarray *> *get_constants() const;
    /// gets the parameterized query string
    const char *get_parameterized_query() const;
    ///
    //@}

private:
    /**
     * Return true if s starts with (lower-case, no whitespace) prefix.
     * All whitespace in s is ignored.
     *
     * Copied from servercomm.hh, should be refactored into a common place.
     */
    bool startsWith(const char *s, const char *prefix) const;
    const char *skipComments(const char *s) const;

    /// method replaces the next argument with the delivered valueString
    void replaceNextArgument(const char *valueString);

    /// storage for the expanded query string
    char *queryString{NULL};

    /// storage for the parameterized query string
    char *parameterizedQueryString{NULL};

    /// list for MDD constants
    r_Set<r_GMarray *> *mddConstants{NULL};
};

//@ManMemo: Module: {\bf rasodmg}
/**
  * \ingroup Rasodmgs
  */

/**
  The free standing function r_oql_execute is called to execute a retrieval query.
  The first parameter, query, is a reference to a r_OQL_Query object specifying
  the query to execute. The second parameter, result, is used for returning the
  result of the query. The query result is of type r_Set< r_Ref_Any >.
  Important: If the transaction parameter is not provided this function is not thread-safe.

  If the function is not called within the scope of an opened database, a r_Error
  exception of kind r_Error_DatabaseClosed is raised. If it is called outside any
  transaction, the exception is of kind r_Error_TransactionNotOpen.

  A complete list of all possible error kinds is given by the following table.

  ```
  r_Error_ClientUnknown                 Client is not known by the server (earlier communication problems).
  r_Error_DatabaseClosed                No database is not opened.
  r_Error_TransactionNotOpen            Call is not within an active transaction.
  r_Error_QueryParameterCountInvalid    At least one of the query parameters is not supplied with a value.
  r_Error_TransferFailed                Other communication problem.
  r_Error_QueryExecutionFailed          The execution of the query failed (further information is available
                                        in an error object of type r_Equery_execution).
  r_Error_TypeInvalid                   Result base type doesn't match the template type.
  ```
*/
void r_oql_execute(r_OQL_Query &query, r_Set<r_Ref_Any> &result, r_Transaction *transaction = NULL);

//@ManMemo: Module: {\bf rasodmg}
/**
  * \ingroup Rasodmgs
  */

/**
  The function is used to execute retrieval queries with the result set being
  of type r_Set< r_Ref< r_GMarray > >. The function is supported for
  compatibility reasons only. We suggest to use the general function
  r_oql_execute able to maintain query results of any type.
  Important: If the transaction parameter is not provided this function is not thread-safe.
*/
void r_oql_execute(r_OQL_Query &query, r_Set<r_Ref<r_GMarray>> &result, r_Transaction *transaction = NULL);

//@ManMemo: Module: {\bf rasodmg}
/**
  * \ingroup Rasodmgs
  */
/**
  The free standing function r_oql_execute is called to execute an insert query
  that returns the OID that has been inserted.
  The first parameter, query, is a reference to a r_OQL_Query object specifying
  the query to execute. The second parameter, result, is used for returning the
  result of the query. The query result is of type r_Set< r_Ref_Any >.
  The third parameter is a dummy parameter, it is used to differentiate from retrieval queries.
  The function used the same return values as the retrieval function above.
  Important: If the transaction parameter is not provided this function is not thread-safe.

  If the function is not called within the scope of an opened database, a r_Error
  exception of kind r_Error_DatabaseClosed is raised. If it is called outside any
  transaction, the exception is of kind r_Error_TransactionNotOpen.
*/
void r_oql_execute(r_OQL_Query &query, r_Set<r_Ref_Any> &result, int dummy, r_Transaction *transaction = NULL);

//@ManMemo: Module: {\bf rasodmg}
/**
  * \ingroup Rasodmgs
  */

/**
  The free standing function r_oql_execute is called to execute an update / delete query.
  It is also used by older ( < v9.1 ) clients for insert queries.
  The first parameter, query, is a reference to a r_OQL_Query object specifying
  the query to execute.
  Important: If the transaction parameter is not provided this function is not thread-safe.

  If the function is not called within the scope of an opened database, a r_Error
  exception of kind r_Error_DatabaseClosed is raised. If it is called outside any
  transaction, the exception is of kind r_Error_TransactionNotOpen.

  A complete list of all possible error kinds is given by the following table.
  
  ```
  r_Error_ClientUnknown                 Client is not known by the server (earlier communication problems).
  r_Error_DatabaseClosed                No database is not opened.
  r_Error_TransactionNotOpen            Call is not within an active transaction.
  r_Error_QueryParameterCountInvalid    At least one of the query parameters is not supplied with a value.
  r_Error_TransferFailed                Other communication problem.
  r_Error_QueryExecutionFailed          The execution of the query failed (further information is available
                                        in an error object of type r_Equery_execution).
  ```
*/
void r_oql_execute(r_OQL_Query &query, r_Transaction *transaction = NULL);

#endif
