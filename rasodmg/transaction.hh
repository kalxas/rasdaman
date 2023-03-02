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

//@ManMemo: Module: {\bf rasodmg}

#ifndef D_TRANSACTION_HH
#define D_TRANSACTION_HH

#include "rasodmg/set.hh"
#include "rasodmg/object.hh"
#include "rasodmg/genreftype.hh"

class r_OId;
class r_Database;
class r_Ref_Any;
template <typename T>
class r_Ref;

/**
  Transactions can be started, committed, aborted, and checkpointed.
  It is important to note that all access, creation, modification,
  and deletion of persistent objects must be done within a transaction.
  Right now, only one transaction can be active at a time.
*/

/**
  * \ingroup Rasodmgs
  */
class r_Transaction
{
public:
    /// possible states of the transaction
    enum r_TAStatus
    {
        active,
        inactive,
        comiting,
        aborting
    };

    /// possible transaction modes
    enum r_TAMode
    {
        read_write,
        read_only
    };

    /// create a new transaction object; if the db object is not provided, this
    /// class not thread-safe.
    r_Transaction(r_Database *db = NULL);

    /// destructor, an active transaction is aborted
    ~r_Transaction();

    /// start the transaction
    void begin(r_TAMode mode = read_write);
    /**
      By default, a transaction is started in write mode. If the read_only
      mode is specified, no write operations are allowed within the transaction
      anymore.
      If any write operation occurs in read_only mode, the exception r_Error with
      kind <tt>r_Error_TransactionReadOnly</tt> will be raised and the transaction will
      be aborted.
      In order to achieve maximal performance, read-only transactions should be used
      whenever posssible, i.e., when no update operations occur within this transaction.
    */

    /// commit transaction and make changes persistent
    void commit();
    /**
      The transaction is committed and changes are made persistent
      in the database.
      While committing, the following errors can occur:

      \latexonly
      \begin{tabular}{lll}
      r_Error_TransferFailed && Server communication problem.\\
      r_Error_ObjectUnknown && Name of object is unknown.\\
      r_Error_DatabaseClassUndefined && Type name of object not known by the database.\\
      r_Error_CollectionElementTypeMismatch && Collection and MDD type mismatch.\\
      \end{tabular}
      \endlatexonly
    */

    /// abort transaction and forget changes within transaction
    void abort();

    /// returns the current state
    r_TAStatus get_status() const;

    /// returns current mode
    r_TAMode get_mode() const;

    //@Man: Methods and types for internal use only:
    //@{
    ///

    /// store a pointer to the actual transaction
    static r_Transaction *actual_transaction;

    /// load an object (internal use only)
    r_Ref_Any load_object(const r_OId &oid);

    /// adds a non-r_Object to the list of persistent objects
    void add_object_list(GenRefType type, void *ref);

    /// sets the database reference that this transaction is using.
    /// if none is provided the default static database is used
    /// NOTE: The setDatabase method should be called before any other operation that uses the database
    void setDatabase(r_Database *database);

    /// returns the database used by this transaction
    r_Database *getDatabase();

    ///
    //@}

private:
    /// adds an object of type \Ref{r_Object} to the list of persistent objects
    void add_object_list(const r_Ref<r_Object> &);

    /// current transaction state
    r_TAStatus ta_state;

    /// current transaction mode (just valid if transaction is active)
    r_TAMode ta_mode;

    /// list of \Ref{r_Object} references which have been created within the transaction
    r_Set<r_Ref<r_Object>> object_list;

    /// list of non \Ref{r_Object} references which have been created within the transaction
    r_Set<GenRefElement *> non_object_list;

    friend class r_Object;

    /// reference to the database used by this transaction
    r_Database *database;
};

#endif
