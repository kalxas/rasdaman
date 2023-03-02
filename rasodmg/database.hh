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
 * INCLUDE:  database.hh
 *
 * MODULE:   rasodmg
 * CLASS:    r_Database
 *
 *  COMMENTS:
 *      None
*/

#ifndef _D_DATABASE_
#define _D_DATABASE_

#include "raslib/mddtypes.hh"
#include "globals.hh"
#include <string>

// forward declarations
class r_Object;
class r_OId;
class r_Type;
class r_Ref_Any;
class ClientComm;

//@ManMemo: Module: {\bf rasodmg}

/**
  A database object must be instantiated and opened before
  starting any transaction which uses the database, and closed
  after ending these transactions.

*/

/**
  * \ingroup Rasodmgs
  */
class r_Database
{
public:
    /// possible database states
    enum access_status
    {
        not_open,
        read_write,
        read_only,
        exclusive
    };

    /// possible types define by symbolic names
    enum type_schema
    {
        CELL = 3,
        MARRAY = 2,
        COLLECTION = 1
    };

    /// default constructor
    r_Database();

    /// constructor getting the rasmgr name
    explicit r_Database(const char *name);
    /**
      One error situations can occur which raise an exception of type r_Error with
      one of the following kinds:
      r_Error_NameInvalid     && Name is NULL.\\
    */

    /// destructor
    ~r_Database();

    /// open a database
    void open(const char *database_name, access_status status = read_write);
    /**
      The method opens the database specified with <tt>database_name</tt>. Several error
      situations can occur which raise an exception of type r_Error with
      one of the following kinds:

      \latexonly
      \begin{tabular}{lll}
      r_Error_HostInvalid     && Host can not be found.\\
      r_Error_ServerInvalid   && Server can not be found.\\
      r_Error_ClientUnknown   && Client is not known by the server (earlier communication problems).\\
      r_Error_DatabaseUnknown && Database does not exist.\\
      r_Error_DatabaseOpen    && Database is already open.\\
      r_Error_TransferFailed  && Other communication problem. \\
      r_Error_NameInvalid     && Name is NULL.\\
      \end{tabular}
      \endlatexonly
    */

    /// close a database
    void close();

    /// create a database with fixed schema RasDaSchema
    void create(const char *name);
    /**
      This method works only if a server host name has been specified with
      <tt>set_servername()</tt>.
      One of error situations can occur will raise an exception of type r_Error with
      one of the following kinds:
      r_Error_NameInvalid     && Name is NULL.\\
    */

    /// destroy a database
    void destroy(const char *name);
    /**
     This method works only if a server host name has been specified with
     <tt>set_servername()</tt>.
     One of error situations can occur will raise an exception of type r_Error with
     one of the following kinds:
     r_Error_NameInvalid     && Name is NULL.\\
    */

    /// set the server name
    void set_servername(const char *name, int port = DEFAULT_PORT);
    /**
      One of error situations can occur will raise an exception of type r_Error with
      one of the following kinds:
      r_Error_NameInvalid     && Name is NULL.\\
    */
    /// set the user name and password
    void set_useridentification(const char *name, const char *plain_pass);
    /**
      One of error situations can occur will raise an exception of type r_Error with
      one of the following kinds:
      r_Error_NameInvalid     && Name is NULL.\\
    */

    /// get the actual status
    access_status get_status() const;

    /// give a name to an object (signature is not ODMG conformant because of compiler bug)
    void set_object_name(r_Object &obj, const char *name);
    /**
      The method gives the <tt>name</tt> to the object <tt>obj</tt>. The name is used for
      further retrieval of the object. Right now, names can just be given to sets
      of type <tt>r_Set</tt>.
      One of error situations can occur will raise an exception of type r_Error with
      one of the following kinds:
      r_Error_NameInvalid     && Name is NULL.\\
    */

    /// lookup named objects in a database (must be called within open database and running transaction)
    r_Ref_Any lookup_object(const char *name) const;
    /**
      The method looks up an object with <tt>name</tt>. Right now, just objects of type r_Set are
      allowed. Error kinds:

      \latexonly
      \begin{tabular}{lll}
      r_Error_ClientUnknown       && Client is not known by the server (earlier communication problems).\\
      r_Error_DatabaseClosed      && Database is not open. \\
      r_Error_TransactionNotOpen  && No transaction is active. \\
      r_Error_ObjectUnknown       && The object with <tt>name</tt> is not in the database.\\
      r_Error_TransferFailed      && Other communication problem. \\
      r_Error_NameInvalid     && Name is NULL.\\
      \end{tabular}
      \endlatexonly
    */

    /// lookup objects by oids in a database (must be called within open database and running transaction)
    r_Ref_Any lookup_object(const r_OId &oid) const;
    /**
      The method looks up an object with <tt>oid</tt>. Right now, just objects of type r_Set and
      r_GMarray are allowed.

      Error kinds:
      \latexonly
      \begin{tabular}{lll}
      r_Error_ClientUnknown       && Client is not known by the server (earlier communication problems).\\
      r_Error_DatabaseClosed      && Database is not open. \\
      r_Error_TransactionNotOpen  && No transaction is active. \\
      r_Error_ObjectUnknown       && The object with <tt>oid</tt> is not in the database.\\
      r_Error_TransferFailed      && Other communication problem. \\
      \end{tabular}
      \endlatexonly
    */

    r_Type *get_type_schema(const char *typeName, type_schema typetype);
    /**
      The method looks up the type structure with <tt>typeName</tt> as its name.  typetype is 1 for marray and 2 for collection.

      Error kinds:
      \latexonly
      \begin{tabular}{lll}
      r_Error_ClientUnknown       && Client is not known by the server (earlier communication problems).\\
      r_Error_DatabaseClosed      && Database is not open. \\
      r_Error_TransactionNotOpen  && No transaction is active. \\
      r_Error_ObjectUnknown       && The object with <tt>typeName</tt> is not in the database.\\
      r_Error_TransferFailed      && Other communication problem. \\
      r_Error_TypeInvalid         && The typetype is neither 1 nor 2. \\
      r_Error_NameInvalid         && The typeName is neither NULL or is a "\0". \\
      \end{tabular}
      \endlatexonly
    */

    /// set the transfer compression format, both for data sent from the server
    /// to the client and the other way around.
    void set_transfer_format(r_Data_Format format, const char *formatParams = NULL);
    /**
      The method sets the transfer compression used for the communications of
      this client with the server.

      Error kinds:
      \latexonly
      \begin{tabular}{lll}
      r_Error_ClientUnknown       && Client is not known by the server\\
      r_Error_DatabaseClosed      && Database is not open\\
      r_Error_FeatureNotSupported && Unsupported transfer format\\
      \end{tabular}
      \endlatexonly
    */

    /// set the storage format for newly created MDD for this client
    void set_storage_format(r_Data_Format format, const char *formatParams = NULL);
    /**
      This method sets the storage format to use for MDD created by this client
      in the RasDaMan database. The return values are identical to set_transfer_format()
    */

    /// stores a pointer to the actually opened database
    static r_Database *actual_database;

    //@Man: Methods for internal use only:
    //@{
    ///
    const r_OId get_new_oid(unsigned short objType) const;
    ///
    //@}

    // creates an empty MDD collection on the server
    void insertColl(const char *collName, const char *typeName, const r_OId &oid);

    /// removes an object from a collection
    void removeObjFromColl(const char *name, const r_OId &oid);

    ClientComm *getComm();

private:
    /// stores a pointer to a communication object, which is valid while a database is opened
    ClientComm *communication{NULL};

    /// database status
    access_status db_status{not_open};

    /// stores the RasMGR name
    std::string rasmgrName;

    /// stores the RasMGR port
    int rasmgrPort;

    /// stores the user name
    std::string userName;

    /// stores the user password (this will change!)
    std::string plainPass;
};

#endif
