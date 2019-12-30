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
 * SOURCE:   transaction.cc
 *
 * MODULE:   rasodmg
 * CLASS:    r_Transaction
 *
 * COMMENTS:
 *          None
*/

#include "rasodmg/transaction.hh"
#include "rasodmg/ref.hh"
#include "rasodmg/database.hh"
#include "rasodmg/iterator.hh"
#include "rasodmg/genreftype.hh"
#include "raslib/error.hh"
#include "raslib/minterval.hh"
#include "raslib/scalar.hh"
#include "raslib/oid.hh"

#include "clientcomm/clientcomm.hh"

#include <logging.hh>

// Initially there is no transaction active.
r_Transaction *r_Transaction::actual_transaction = 0;

r_Transaction::r_Transaction(r_Database *db)
    : ta_state(inactive), ta_mode(read_write), database{db}
{
}

r_Transaction::~r_Transaction()
{
    if (ta_state == active)
        abort();
}

void
r_Transaction::begin(r_Transaction::r_TAMode mode)
{
    if (!this->database)
        this->database = r_Database::actual_database;
    if (ta_state != inactive)
        throw r_Error(r_Error::r_Error_TransactionOpen);
    if (this->database == 0)
        throw r_Error(r_Error::r_Error_DatabaseClosed);

    ta_state = active;
    // if a database is opened, a communication object is existing
    this->database->getComm()->openTA(mode == read_only ? 1 : 0);
    this->database->getComm()->setTransaction(this);

    if (actual_transaction == NULL)
        actual_transaction = this;
    ta_mode  = mode;
}

void
r_Transaction::commit()
{
    if (ta_state != active)
    {
        throw r_Error(r_Error::r_Error_TransactionNotOpen);
    }
    else
    {
        LDEBUG << "Commit Log:";

        //
        // Commit list of r_Object references.
        //

        r_Iterator<r_Ref<r_Object>> iter = object_list.create_iterator();

        for (iter.reset(); iter.not_done(); iter++)
        {
            if ((*iter)->get_oid().is_valid())
            {
                LDEBUG << "  Object " << (*iter)->get_oid() << "  ";

                switch ((*iter)->get_status())
                {
                case r_Object::deleted:
                    LDEBUG << "state DELETED,  deleting ... ";
                    (*iter)->r_Object::delete_obj_from_db();
                    LDEBUG << "OK";
                    break;

                case r_Object::created:
                    LDEBUG << "state CREATED,  writing  ... ";
                    (*iter)->insert_obj_into_db();
                    LDEBUG << "OK";
                    break;

                case r_Object::modified:
                    LDEBUG << "state MODIFIED, modifying ... ";
                    (*iter)->update_obj_in_db();
                    break;

                case r_Object::read:
                    LDEBUG << "state READ,     OK";
                    break;

                case r_Object::transient:
                    LDEBUG << "state TRANSIENT,     OK";
                    break;

                default:
                    LERROR << "state UNKNOWN";
                    break;
                }
            }
            else
            {
                LDEBUG << "  Object with no oid, state TRANSIENT query result";
            }
        }

        // Don't do the r_deactivate() in the first loop because if a collection is not
        // before its elements in the list, it tries to save non existing objects.

        for (iter.reset(); iter.not_done(); iter++)
        {
            if (!(*iter)->test_status(r_Object::deleted))
                (*iter)->r_deactivate();

            (*iter)->r_Object::r_deactivate();
            free((*iter).get_memory_ptr());
        }
        object_list.remove_all();

        //
        // Commit list of non-r_Object references.
        //

        r_Iterator<GenRefElement *> iter2 = non_object_list.create_iterator();
        for (iter2.reset(); iter2.not_done(); iter2++)
        {
            LDEBUG << "  Value ";
            switch ((*iter2)->type)
            {
            case POINT:
                LDEBUG << "transient Point DELETED";
                delete (static_cast<r_Point *>((*iter2)->ref));
                break;

            case SINTERVAL:
                LDEBUG << "transient Sinterval DELETED";
                delete (static_cast<r_Sinterval *>((*iter2)->ref));
                break;

            case MINTERVAL:
                LDEBUG << "transient Minterval DELETED";
                delete (static_cast<r_Minterval *>((*iter2)->ref));
                break;

            case OID:
                LDEBUG << "transient OId DELETED";
                delete (static_cast<r_OId *>((*iter2)->ref));
                break;

            default:
                LDEBUG << "transient Scalar DELETED";
                delete (static_cast<r_Scalar *>((*iter2)->ref));
                break;
            }
            delete *iter2;
        }
        non_object_list.remove_all();

        //
        // commit transaction on the server
        //

        this->database->getComm()->commitTA();
        ta_state = inactive;
        if (this == actual_transaction)
            actual_transaction = 0;
    }
}

void
r_Transaction::abort()
{
    if (ta_state != active)
    {
        throw r_Error(r_Error::r_Error_TransactionNotOpen);
    }
    else
    {
        LDEBUG << "Abort Log:";

        //
        // Abort list of r_Object references.
        //

        r_Iterator<r_Ref<r_Object>> iter = object_list.create_iterator();
        for (iter.reset(); iter.not_done(); iter++)
        {
            LDEBUG << "  Object DELETED";

            if (!(*iter)->test_status(r_Object::deleted))
                (*iter)->r_deactivate();

            (*iter)->r_Object::r_deactivate();
            free((*iter).get_memory_ptr());
        }
        object_list.remove_all();

        //
        // Abort list of non-r_Object references.
        //

        r_Iterator<GenRefElement *> iter2 = non_object_list.create_iterator();
        for (iter2.reset(); iter2.not_done(); iter2++)
        {
            switch ((*iter2)->type)
            {
            case POINT:
                LDEBUG << "  Transient Point DELETED";
                delete (static_cast<r_Point *>((*iter2)->ref));
                break;

            case SINTERVAL:
                LDEBUG << "  Transient Sinterval DELETED";
                delete (static_cast<r_Sinterval *>((*iter2)->ref));
                break;

            case MINTERVAL:
                LDEBUG << "  Transient Minterval DELETED";
                delete (static_cast<r_Minterval *>((*iter2)->ref));
                break;

            case OID:
                LDEBUG << "  Transient OId DELETED";
                delete (static_cast<r_OId *>((*iter2)->ref));
                break;

            default:
                LDEBUG << "  Transient Scalar DELETED";
                delete (static_cast<r_Scalar *>((*iter2)->ref));
                break;
            }
            delete *iter2;
        }
        non_object_list.remove_all();

        //
        // Abort transaction on the server.
        //
        if (this->database && this->database->getComm())
            this->database->getComm()->abortTA();
        else
            LDEBUG << "Database was already closed. Please abort every transaction before closing the database.";

        ta_state = inactive;
        if (this == actual_transaction)
            actual_transaction = 0;
    }
}

r_Ref_Any
r_Transaction::load_object(const r_OId &oid)
{
    // check, if object is already loaded
    bool found = false;
    r_Iterator<r_Ref<r_Object>> iter = object_list.create_iterator();

    iter.reset();
    while (iter.not_done() && !found)
    {
        found = ((*iter)->get_oid() == oid);
        if (!found)
            iter++;
    }

    if (found)
    {
        // return reference of loaded object
        LTRACE << "load_object( oid ) - object already loaded";
        return *iter;
    }
    else
    {
        // load object and return reference
        LTRACE << "load_object( oid ) - load object";
        return this->database->lookup_object(oid);
    }
}

void
r_Transaction::add_object_list(const r_Ref<r_Object> &obj)
{
    object_list.insert_element(obj);
}

void
r_Transaction::add_object_list(GenRefType type, void *ref)
{
    GenRefElement *element = new GenRefElement;

    element->type = type;
    element->ref  = ref;

    non_object_list.insert_element(element);
}

void
r_Transaction::setDatabase(r_Database *databaseArg)
{
    this->database = databaseArg;
}

r_Database *
r_Transaction::getDatabase()
{
    return database != NULL ? database : r_Database::actual_database;
}

r_Transaction::r_TAStatus
r_Transaction::get_status() const
{
    return ta_state;
}

r_Transaction::r_TAMode
r_Transaction::get_mode() const
{
    return ta_mode;
}
