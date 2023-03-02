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
 * SOURCE:   object.cc
 *
 * MODULE:   rasodmg
 * CLASS:    r_Object
 *
 * COMMENTS:
 *      None
*/

#include "object.hh"
#include "raslib/type.hh"
#include "raslib/error.hh"
#include "rasodmg/object.hh"
#include "rasodmg/transaction.hh"
#include "rasodmg/database.hh"
#include "rasodmg/ref.hh"
#include "clientcomm/clientcomm.hh"
#include "mymalloc/mymalloc.h"
#include <logging.hh>
#include <iostream>

// At the beginning, next and last object types/status are not specified.
r_Object::ObjectStatus r_Object::next_object_status = r_Object::no_status;
r_Object::ObjectType r_Object::next_object_type = r_Object::no_object;
char *r_Object::next_object_type_name = 0;
r_OId r_Object::next_object_oid = r_OId();
r_Object::ObjectType r_Object::last_object_type = r_Object::no_object;

r_Object::r_Object(r_Transaction *transactionArg)
    : transaction{transactionArg},
      object_status(next_object_status),
      oid()
{
    update_transaction();

    if (next_object_type_name)
    {
        type_name = strdup(next_object_type_name);
    }

    if (next_object_type == persistent_object)
    {
        LERROR << "Error: A peristent object is constructed with default constructor.";
    }
    else
    {
        object_type = transient_object;
    }

    internal_obj_type = 0;

    // reset next object type/status
    r_Object::next_object_type = no_object;
    r_Object::next_object_status = no_status;
    r_Object::next_object_type_name = 0;
    r_Object::next_object_oid = r_OId();
}

r_Object::r_Object(unsigned short objType, r_Transaction *transactionArg)
    : transaction{transactionArg},
      object_status(next_object_status),
      oid()
{
    update_transaction();

    if (next_object_type_name)
    {
        type_name = strdup(next_object_type_name);
    }

    if (next_object_type == persistent_object)
    {
        if (transaction == 0)
        {
            LERROR << "Tried to create a persistent object outside a transaction.";
            throw r_Error(r_Error::r_Error_TransactionNotOpen);
        }

        object_type = persistent_object;

        switch (object_status)
        {
        case created:
            // In case the object is newly created, get a new oid and assign it to the object.
            oid = transaction->getDatabase()->get_new_oid(objType);
            break;
        case read:
        case transient:
            // In case the object is read from db, use the oid stored in next_object_oid.
            oid = next_object_oid;
            break;
        default:
            LTRACE << "r_Object(objType) bad object_status " << object_status;
            break;
        }

        // Add the object to the list of persistent objects in the current transaction.
        if (oid.is_valid())
        {
            transaction->add_object_list(r_Ref<r_Object>(oid, this, transaction));
        }
        else
        {
            transaction->add_object_list(r_Ref<r_Object>(this));
        }
    }
    else
    {
        object_type = transient_object;
    }

    internal_obj_type = objType;

    // reset next object type/status
    r_Object::next_object_type = no_object;
    r_Object::next_object_status = no_status;
    r_Object::next_object_type_name = 0;
    r_Object::next_object_oid = r_OId();
}

r_Object::r_Object(const r_Object &obj, unsigned short objType, r_Transaction *transactionArg)
    : transaction{transactionArg},
      object_status(next_object_status),
      oid()
{
    update_transaction();

    if (next_object_type_name)
    {
        type_name = strdup(next_object_type_name);
    }

    if (next_object_type == persistent_object)
    {
        if (transaction == 0)
        {
            LERROR << "Error: Tried to create a persistent object outside a transaction.";
            throw r_Error(r_Error::r_Error_TransactionNotOpen);
        }

        object_type = persistent_object;

        switch (object_status)
        {
        case created:
            // In case the object is newly created, get a new oid and assign it to the object.
            oid = transaction->getDatabase()->get_new_oid(objType);
            break;
        case read:
        case transient:
            // In case the object is read from db, use the oid stored in next_object_oid.
            oid = next_object_oid;
            break;
        default:
            LTRACE << "r_Object(obj, objType) bad object_status " << object_status;
            break;
        }

        // Add the object to the list of persistent objects in the actual transaction.
        if (oid.is_valid())
        {
            transaction->add_object_list(r_Ref<r_Object>(oid, this, transaction));
        }
        else
        {
            transaction->add_object_list(r_Ref<r_Object>(this));
        }
    }
    else
    {
        object_type = transient_object;
    }

    internal_obj_type = objType;

    // reset next object type/status
    r_Object::next_object_type = no_object;
    r_Object::next_object_status = no_status;
    r_Object::next_object_type_name = 0;
    r_Object::next_object_oid = r_OId();

    if (obj.object_name)
    {
        object_name = strdup(obj.object_name);
    }

    if (obj.type_name && !type_name)
    {
        type_name = strdup(obj.type_name);
    }

    if (obj.type_structure)
    {
        type_structure = new char[strlen(obj.type_structure) + 1];
        strcpy(type_structure, obj.type_structure);
    }
}

void r_Object::set_type_schema(const r_Type *tyy)
{
    if (type_schema)
    {
        LERROR << "r_Object::set_type_schema(" << tyy->name() << ") this object has already a type";
        throw r_Error(ILLEGALARGUMENT);
    }
    type_schema = tyy->clone();
}

r_Object::~r_Object()
{
    // Free memory in the transient case. In the persistent case, r_deactivate()
    // is invoked at the commit/abort point.
    if (test_type(transient_object))
    {
        r_deactivate();
    }

    object_status = deleted;

    // store the object type for the delete operator
    r_Object::last_object_type = object_type;
}

void r_Object::r_deactivate()
{
    if (type_schema)
    {
        delete type_schema;
        type_schema = 0;
    }
    if (object_name)
    {
        free(object_name);
        object_name = 0;
    }
    if (type_name)
    {
        free(type_name);
        type_name = 0;
    }
    if (type_structure)
    {
        delete[] type_structure;
        type_structure = 0;
    }
}

/*************************************************************
 * Method name...: operator new( size_t size )
 *
 * Arguments.....:
 *   none
 * Return value..:
 *   none
 * Description...: New operator set the next_object_type and
 *                 allocates memory for the object.
 ************************************************************/
void *
r_Object::operator new(size_t size)
{
    r_Object::next_object_type = transient_object;
    r_Object::next_object_status = created;
    r_Object::next_object_type_name = 0;
    r_Object::next_object_oid = r_OId();

    void *a = mymalloc(size);
    return a;
}

/*************************************************************
 * Method name...: operator new( size_t size,
 *                               r_Database *database,
 *                               const char* type_name  )
 *
 * Arguments.....:
 *   none
 * Return value..:
 *   none
 * Description...: New operator set the next_object_type and
 *                 allocates memory for the object.
 ************************************************************/
void *
r_Object::operator new(size_t size, r_Database * /*database*/, const char *type_name)
{
    r_Object::next_object_type = persistent_object;
    r_Object::next_object_status = created;
    r_Object::next_object_type_name = const_cast<char *>(type_name);
    r_Object::next_object_oid = r_OId();

    void *a = mymalloc(size);
    return a;
}

void *
r_Object::operator new(size_t size, const char *type_name)
{
    r_Object::next_object_type = transient_object;
    r_Object::next_object_status = created;
    r_Object::next_object_type_name = const_cast<char *>(type_name);
    r_Object::next_object_oid = r_OId();

    void *a = mymalloc(size);
    return a;
}

/*************************************************************
 * Method name...: operator delete( void* obj_ptr )
 *
 * Arguments.....:
 *   none
 * Return value..:
 *   none
 * Description...: Delete operator.
 *                 Transient objects are deleted immediately from
 *                 main memory.
 *                 Persistent objects have been marked as deleted in
 *                 the destructor. Further accesses through a r_Ref raise
 *                 an exception. Main memory is freed after the transaction
 *                 commits.
 ************************************************************/
void r_Object::operator delete(void *obj_ptr)
{
    if (r_Object::last_object_type == transient_object && obj_ptr)
    {
        free(obj_ptr);
        obj_ptr = NULL;
    }

    r_Object::last_object_type = no_object;
}

bool r_Object::test_status(ObjectStatus status)
{
    return status == object_status;
}
bool r_Object::test_type(ObjectType type)
{
    return type == object_type;
}

/*************************************************************
 * Method name...: operator new( size_t       size,
 *                               r_Database   *database,
 *                               ObjectStatus status  )
 *
 * Arguments.....:
 *   none
 * Return value..:
 *   none
 * Description...: New operator set the next_object_type to
 *   persistent object and the next_object_status to the
 *   given status. Memory for the object is allocated.
 ************************************************************/
void *
r_Object::operator new(size_t size, r_Database * /*database*/, ObjectStatus status, const r_OId &oid)
{
    r_Object::next_object_type = persistent_object;
    r_Object::next_object_status = status;
    r_Object::next_object_type_name = 0;
    r_Object::next_object_oid = oid;

    void *a = mymalloc(size);
    return a;
}

const r_Type *
r_Object::get_type_schema()
{
    if (!type_schema)
    {
        update_transaction();

        // If type structure not known then try to get it from the server
        if ((type_structure == NULL) || (strlen(type_structure) == 0))
        {
            ClientComm::r_Type_Type typeType = static_cast<ClientComm::r_Type_Type>(0);

            if (transaction == NULL || transaction->get_status() != r_Transaction::active)
                return NULL;

            // we need an open database and an active transaction
            if (transaction->getDatabase() == NULL || transaction->getDatabase()->get_status() == r_Database::not_open)
                return NULL;

            // set the object type and contact the database if the type name is defined.
            if (internal_obj_type == 1)
                typeType = ClientComm::r_MDDType_Type;
            else if (internal_obj_type == 2)
                typeType = ClientComm::r_SetType_Type;

            if (type_name == NULL || strlen(type_name) == 0 || typeType == 0)
                return NULL;

            try
            {
                type_structure = transaction->getDatabase()->getComm()->getTypeStructure(type_name, typeType);
            }
            catch (r_Error &errObj)
            {
                LERROR << "Failed retriving type structure, error "
                       << errObj.get_errorno() << ": " << errObj.what();
                return NULL;
            }
        }

        if (type_structure != NULL)
            type_schema = r_Type::get_any_type(type_structure);
    }
    return type_schema;
}

void r_Object::update_obj_in_db()
{
    LWARNING << "dummy implementation";
}

void r_Object::load_obj_from_db()
{
    LWARNING << "dummy implementation";
}

void r_Object::delete_obj_from_db()
{
    if (object_name && strlen(object_name))
    {
        // delete myself from the database
        get_transaction()->getDatabase()->getComm()->deleteCollByName(object_name);
    }
    else
    {
        LWARNING << "no name - take oid ... ";

        // delete myself from the database
        if (oid.get_local_oid())
            get_transaction()->getDatabase()->getComm()->deleteObjByOId(oid);
        else
            LERROR << " no oid ... FAILED";
    }
}

void r_Object::initialize_oid(const r_OId &initOId)
{
    oid = initOId;
}

void r_Object::update_transaction()
{
    if (!transaction)
    {
        transaction = r_Transaction::actual_transaction;
    }
}

r_Transaction *r_Object::get_transaction() const
{
    return transaction != NULL ? transaction : r_Transaction::actual_transaction;
}

void r_Object::mark_modified()
{
    if (object_status == no_status ||
        object_status == read)
    {
        object_status = modified;
    }
}

void r_Object::set_object_name(const char *name)
{
    if (!name)
    {
        //null pointer
        LERROR << "r_Object::set_object_name(name) name is null!";
        throw r_Error(INVALIDOBJECTNAME);
    }

    const char *cptr = name;

    //check if the name contains only [a-zA-Z0-9_]
    while (*cptr)
    {
        if (((*cptr >= 'a') && (*cptr <= 'z')) ||
            ((*cptr >= 'A') && (*cptr <= 'Z')) ||
            ((*cptr >= '0') && (*cptr <= '9')) ||
            (*cptr == '_'))
        {
            cptr++;
        }
        else
        {
            break;
        }
    }

    if (*cptr)
    {
        //invalid character in object name
        LERROR << "r_Object::set_object_name(" << name << ") invalid name!";
        throw r_Error(INVALIDOBJECTNAME);
    }

    if (object_name)
    {
        free(object_name);
    }

    object_name = strdup(name);
}

void r_Object::set_type_by_name(const char *name)
{
    if (!name)
    {
        //null pointer
        LERROR << "r_Object::set_type_by_name(name) name is null!";
        throw r_Error(r_Error::r_Error_NameInvalid);
    }

    if (type_name)
    {
        free(type_name);
    }

    type_name = strdup(name);
}

void r_Object::set_type_structure(const char *name)
{
    if (!name)
    {
        //null pointer
        LERROR << "r_Object::type_structure(name) name is null!";
        throw r_Error(r_Error::r_Error_NameInvalid);
    }

    if (type_structure)
    {
        delete[] type_structure;
    }

    type_structure = new char[strlen(name) + 1];
    strcpy(type_structure, name);
}

const char *
r_Object::get_type_name() const
{
    return type_name;
}

const char *
r_Object::get_object_name() const
{
    return object_name;
}

const char *
r_Object::get_type_structure() const
{
    if (type_structure != NULL)
    {
        return type_structure;
    }
    else
    {
        return "";
    }
}

r_Object::ObjectStatus
r_Object::get_status() const
{
    return object_status;
}

const r_OId &
r_Object::get_oid() const
{
    return oid;
}
