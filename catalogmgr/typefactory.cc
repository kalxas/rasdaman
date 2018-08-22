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
// This is -*- C++ -*-
/*************************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 *   
 *
 ***********************************************************************/

#include "config.h"
#include <vector>
#include <algorithm>
#include <map>
#include <utility>
#include <set>

#include "raslib/rminit.hh"
#include "relcatalogif/alltypes.hh"
#include "typefactory.hh"
#include "reladminif/objectbroker.hh"
#include "reladminif/adminif.hh"
#include "reladminif/databaseif.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "reladminif/dbref.hh"
#include "relcatalogif/dbnullvalues.hh"
#include "relmddif/mddid.hh"
#include "relmddif/dbmddobj.hh"
#include "relmddif/dbmddset.hh"
#include "relcatalogif/syntaxtypes.hh"
#include <logging.hh>
#include <boost/algorithm/string/predicate.hpp>
#include "raslib/error.hh"

TypeFactory* TypeFactory::myInstance = 0;

//This variable is not required since any struct
//type can now be deleted. This resulted as
//the resolution of ticket #88
const short TypeFactory::MaxBuiltInId = 11;

const char* OctetType::Name = "Octet";
const char* UShortType::Name = "UShort";
const char* ShortType::Name = "Short";
const char* ULongType::Name = "ULong";
const char* LongType::Name = "Long";
const char* BoolType::Name = "Bool";
const char* CharType::Name = "Char";
const char* FloatType::Name = "Float";
const char* DoubleType::Name = "Double";
const char* ComplexType1::Name = "Complex1";
const char* ComplexType2::Name = "Complex2";

const std::string TypeFactory::ANONYMOUS_CELL_TYPE_PREFIX = "__CELLTYPE__";

using namespace std;

const map<string, string> TypeFactory::syntaxTypeInternalTypeMap = TypeFactory::createSyntaxTypeInternalTypeMap();
const map<string, string> TypeFactory::internalTypeSyntaxTypeMap = TypeFactory::createInternalTypeSyntaxTypeMap();

map<string, string> TypeFactory::createSyntaxTypeInternalTypeMap()
{
    map<string, string> ret;

    ret.insert(std::make_pair(SyntaxType::OCTET_NAME, OctetType::Name));
    ret.insert(std::make_pair(SyntaxType::USHORT_NAME, UShortType::Name));
    ret.insert(std::make_pair(SyntaxType::UNSIGNED_SHORT_NAME, UShortType::Name));
    ret.insert(std::make_pair(SyntaxType::SHORT_NAME, ShortType::Name));
    ret.insert(std::make_pair(SyntaxType::ULONG_NAME, ULongType::Name));
    ret.insert(std::make_pair(SyntaxType::UNSIGNED_LONG_NAME, ULongType::Name));
    ret.insert(std::make_pair(SyntaxType::LONG_NAME, LongType::Name));
    ret.insert(std::make_pair(SyntaxType::BOOL_NAME, BoolType::Name));
    ret.insert(std::make_pair(SyntaxType::CHAR_NAME, CharType::Name));
    ret.insert(std::make_pair(SyntaxType::FLOAT_NAME, FloatType::Name));
    ret.insert(std::make_pair(SyntaxType::DOUBLE_NAME, DoubleType::Name));

    return ret;
}

map<string, string> TypeFactory::createInternalTypeSyntaxTypeMap()
{
    map<string, string> ret;
    map<string, string> tmp = createSyntaxTypeInternalTypeMap();

    for (auto mapIt = tmp.begin(); mapIt != tmp.end(); ++mapIt)
    {
        ret.insert(std::make_pair(mapIt->second, mapIt->first));
    }

    return ret;
}

string TypeFactory::getInternalTypeFromSyntaxType(const std::string& syntaxTypeName)
{
    string result = syntaxTypeName;
    map<string, string>::const_iterator it = syntaxTypeInternalTypeMap.find(syntaxTypeName);
    if (it != syntaxTypeInternalTypeMap.end())
    {
        result =  it->second;
    }

    return result;
}

string TypeFactory::getSyntaxTypeFromInternalType(const std::string& internalTypeName)
{
    string result = internalTypeName;
    map<string, string>::const_iterator it = internalTypeSyntaxTypeMap.find(internalTypeName);
    if (it != internalTypeSyntaxTypeMap.end())
    {
        result = it->second;
    }

    return result;
}

// all atomic types given back by mapType()
// for managing the memory of temporary types
std::vector<Type*>* TypeFactory::theTempTypes = 0;

TypeFactory*
TypeFactory::instance()
{
    if (myInstance == 0)
    {
        myInstance = new TypeFactory;
    }
    return myInstance;
}

const BaseType*
TypeFactory::mapType(const char* typeName)
{
    BaseType* resultType = 0;
    resultType = static_cast<BaseType*>(ObjectBroker::getObjectByName(OId::ATOMICTYPEOID, typeName));
    if (resultType == 0)
    {
        try
        {
            resultType = static_cast<BaseType*>(ObjectBroker::getObjectByName(OId::STRUCTTYPEOID, typeName));
        }
        catch (r_Error)
        {
            resultType = 0;
        }
    }
    return resultType;
}

const StructType*
TypeFactory::addStructType(const StructType* type)
{
    StructType* persistentType = 0;
    const StructType* retval = 0;
    if (type->isPersistent())
    {
        LTRACE << "type is persistent " << type->getName() << " " << type->getOId();
        retval = type;
    }
    else
    {
        persistentType = new StructType(const_cast<char*>(type->getTypeName()), type->getNumElems());
        for (unsigned int i = 0; i < type->getNumElems(); i++)
        {
            switch (type->getElemType(i)->getType())
            {
            case STRUCT:
                LTRACE << "element is struct type " << type->getElemName(i) << " of type " << type->getElemType(i)->getName();
                //persistentType->addElement(type->getElemName(i), addStructType(static_cast<const StructType*>(type->getElemType(i)))); 
                LERROR << "Building a struct using a user-defined struct is currently not supported.";
                throw r_Error(STRUCTOFSTRUCTSDISABLED);
                break;
            case ULONG:
            case USHORT:
            case CHAR:
            case BOOLTYPE:
            case LONG:
            case SHORT:
            case OCTET:
            case DOUBLE:
            case FLOAT:
            case COMPLEXTYPE1:
            case COMPLEXTYPE2:
                LTRACE << "element is atomic type " << type->getElemName(i) << " of type " << type->getElemType(i)->getName();
                persistentType->addElement(type->getElemName(i), static_cast<BaseType*>(ObjectBroker::getObjectByOId(type->getElemType(i)->getOId())));
                break;
            default:
                persistentType = 0;
                LTRACE << "addStructType(" << type->getTypeName() << ") unknown type " << type->getOId() << type->getOId().getType();
                break;
            }
        }
        LTRACE << "type is now persistent " << persistentType->getName() << " " << persistentType->getOId();
        persistentType->setCached(true);
        persistentType->setPersistent(true);
        ObjectBroker::registerDBObject(persistentType);
        retval = persistentType;
    }
    return retval;
}

const SetType*
TypeFactory::mapSetType(const char* typeName)
{
    // it is a user defined type
    SetType* resultType = 0;
    try
    {
        resultType = static_cast<SetType*>(ObjectBroker::getObjectByName(OId::SETTYPEOID, typeName));
    }
    catch (r_Error)
    {
        resultType = 0;
    }
    return resultType;
}

const SetType*
TypeFactory::addSetType(const SetType* type)
{
    SetType* persistentType = 0;
    const SetType* retval = 0;
    if (type->isPersistent())
    {
        LTRACE << "type is persistent " << type->getName() << " " << type->getOId();
        retval = type;
    }
    else
    {
        persistentType = new SetType(const_cast<char*>(type->getTypeName()), const_cast<MDDType*>(addMDDType(type->getMDDType())));

        DBNullvalues* interval = type->getNullValues();
        if (interval != NULL)
        {
            persistentType->setNullValues(*((r_Nullvalues*)interval));
        }

        persistentType->setPersistent(true);
        LTRACE << "type is now persistent " << type->getName() << " " << persistentType->getOId();
        ObjectBroker::registerDBObject(persistentType);
        persistentType->setCached(true);
        retval = persistentType;
    }
    return retval;
}

const MDDType*
TypeFactory::mapMDDType(const char* typeName)
{
    MDDType* resultType = 0;
    try
    {
        resultType = ObjectBroker::getMDDTypeByName(typeName);
    }
    catch (...)
    {
        resultType = 0;
    }
    return resultType;
}

const MDDType*
TypeFactory::addMDDType(const MDDType* type)
{
    MDDType* persistentType = 0;
    const MDDType* retval = 0;
    if (type->isPersistent())
    {
        LTRACE << "type is persistent " << type->getOId();
        retval = type;
    }
    else
    {
        LTRACE << "type is not persistent " << type->getOId();
        switch (type->getSubtype())
        {
        case MDDType::MDDONLYTYPE:
            LTRACE << "is MDDONLYTYPE";
            persistentType = new MDDType(type->getTypeName());
            break;
        case MDDType::MDDBASETYPE:
            LTRACE << "is MDDBASETYPE";
            persistentType = new MDDBaseType(type->getTypeName(), addStructType(static_cast<StructType*>(const_cast<BaseType*>((static_cast<MDDBaseType*>(const_cast<MDDType*>(type)))->getBaseType()))));
            break;
        case MDDType::MDDDOMAINTYPE:
            LTRACE << "is MDDDOMAINTYPE";
            persistentType = new MDDDomainType(type->getTypeName(), addStructType(static_cast<StructType*>(const_cast<BaseType*>((static_cast<MDDBaseType*>(const_cast<MDDType*>(type)))->getBaseType()))), *(static_cast<MDDDomainType*>(const_cast<MDDType*>(type)))->getDomain());
            break;
        case MDDType::MDDDIMENSIONTYPE:
            LTRACE << "is MDDDIMENSIONTYPE";
            persistentType = new MDDDimensionType(type->getTypeName(), addStructType(static_cast<StructType*>(const_cast<BaseType*>((static_cast<MDDBaseType*>(const_cast<MDDType*>(type)))->getBaseType()))), (static_cast<MDDDimensionType*>(const_cast<MDDType*>(type)))->getDimension());
            break;
        default:
            LTRACE << "addMDDType(" << type->getName() << ") mddsubtype unknown";
            break;
        }
        if (persistentType != 0)
        {
            persistentType->setPersistent(true);
            LTRACE << "adding " << persistentType->getName() << " " << persistentType->getOId();
            persistentType->setCached(true);
            ObjectBroker::registerDBObject(persistentType);
            retval = persistentType;
        }
        else
        {
            //error message was already given in switch default
        }
    }
    return retval;
}

Type*
TypeFactory::addTempType(Type* type)
{
    // put in front to avoid deletion of MDDTypes still referenced
    // by an MDDBaseType.
    theTempTypes->insert(theTempTypes->begin(), type);
    return type;
}

void
TypeFactory::initialize()
{
    // to initailize the typefactory
    if (!theTempTypes)
    {
        theTempTypes = new std::vector<Type*>;
    }
}

void
TypeFactory::freeTempTypes()
{
    // delete all temporary types
    if (theTempTypes)
    {
        for (std::vector<Type*>::iterator iter = theTempTypes->begin(); iter != theTempTypes->end(); iter++)
        {
            delete *iter;
            *iter = 0;
        }
        delete theTempTypes;
        theTempTypes = 0;
    }
}

TypeFactory::TypeFactory()
{
}

void
TypeFactory::deleteStructType(const char* typeName)
{
    const DBObject* resultType = mapType(typeName);
    if (resultType)
    {
        bool canDelete = true;
        for (TypeIterator<MDDType> miter = createMDDIter(); miter.not_done(); miter.advance())
        {
            if (miter.get_element()->getSubtype() != MDDType::MDDONLYTYPE)
            {
                if ((static_cast<MDDBaseType*>(miter.get_element().ptr()))->getBaseType() == resultType)
                {
                    LTRACE << "mdd type " << miter.get_element()->getName() << " contains " << typeName;
                    canDelete = false;
                    break;
                }
            }
        }
        if (canDelete)
        {
            DBObjectId toKill(resultType->getOId());
            toKill->setPersistent(false);
            toKill->setCached(false);
            LDEBUG << "Base type '" << typeName << "' will be deleted from the database";
        }
        else
        {
            LERROR << "Struct type '" << typeName << "' is currently in use, so it cannot be dropped.";
            throw r_Error(TYPEISINUSE);
        }
    }
    else
    {
        LWARNING << "Base type '" << typeName << "' cannot be deleted. It does not exist";
    }
}

void
TypeFactory::deleteMDDType(const char* typeName)
{
    const MDDType* resultType = mapMDDType(typeName);  //is ok because only short for find
    if (resultType)
    {
        bool canDelete = true;
        for (TypeIterator<SetType> miter = createSetIter(); miter.not_done(); miter.advance())
        {
            if (miter.get_element()->getMDDType() == resultType)
            {
                LTRACE << "set type " << miter.get_element()->getName() << " contains " << typeName;
                canDelete = false;
                break;
            }
        }
        if (canDelete)
        {
            if (resultType->getSubtype() != MDDType::MDDONLYTYPE)
            {
                //mdd only types can not be in mdd objects
                OIdSet* theList = ObjectBroker::getAllObjects(OId::MDDOID);
                for (OIdSet::iterator miter = theList->begin(); miter != theList->end(); miter++)
                {
                    if (DBMDDObjId(*miter)->getMDDBaseType() == resultType)
                    {
                        LTRACE << "mdd object " << *miter << " contains " << typeName;
                        canDelete = false;
                        break;
                    }
                }
                delete theList;
                theList = 0;
            }
            if (canDelete)
            {
                //TODO-GM: explain
                // check if the base type is an annonymous type
                if (resultType->getSubtype() != MDDType::MDDONLYTYPE)
                {
                    const BaseType* baseType = static_cast<MDDBaseType*>(const_cast<MDDType*>(resultType))->getBaseType();
                    std::string baseTypeName = std::string(baseType->getTypeName());
                    if (boost::starts_with(baseTypeName, TypeFactory::ANONYMOUS_CELL_TYPE_PREFIX))
                    {
                        DBObjectId baseTypeToKill(baseType->getOId());
                        baseTypeToKill->setPersistent(false);
                        baseTypeToKill->setCached(false);
                    }
                }

                DBObjectId toKill(resultType->getOId());
                toKill->setPersistent(false);
                toKill->setCached(false);
                LDEBUG << "MDD type '" << typeName << "' will be deleted from the database.";
            }
            else
            {
                LERROR << "MDD type '" << typeName << "' is currently in use, so it cannot be dropped.";
                throw r_Error(TYPEISINUSE);
            }
        }
        else
        {
            LERROR << "MDD type '" << typeName << "' is currently in use, so it cannot be dropped.";
            throw r_Error(TYPEISINUSE);
        }
    }
    else
    {
        LWARNING << "MDD type '" << typeName << "' cannot be deleted. It does not exist";
    }
}

void
TypeFactory::deleteSetType(const char* typeName)
{
    const DBObject* resultType = const_cast<SetType*>(mapSetType(typeName));//is ok because only short for find
    if (resultType)
    {
        bool canDelete = true;
        OIdSet* theList = ObjectBroker::getAllObjects(OId::MDDCOLLOID);
        for (OIdSet::iterator miter = theList->begin(); miter != theList->end(); miter++)
        {
            if (DBMDDSetId(*miter)->getCollType() == resultType)
            {
                LTRACE << "set object " << *miter << " contains " << typeName;
                canDelete = false;
                break;
            }
        }
        delete theList;
        theList = 0;
        if (canDelete)
        {
            DBObjectId toKill(resultType->getOId());
            toKill->setPersistent(false);
            toKill->setCached(false);
            LDEBUG << "set type '" << typeName << "' will be deleted from the database";
        }
        else
        {
            LERROR << "set type '" << typeName << "' is currently in use, so it cannot be dropped.";
            throw r_Error(TYPEISINUSE);
        }
    }
    else
    {
        LWARNING << "set type '" << typeName << "' cannot be deleted. It does not exist";
    }
}

void
TypeFactory::deleteTmpMDDType(const char* typeName)
{
    const MDDType* resultType = mapMDDType(typeName);  //is ok because only short for find
    if (resultType)
    {
        DBObjectId toKill(resultType->getOId());
        toKill->setPersistent(false);
        toKill->setCached(false);
        LTRACE << "will be deleted from db";
    }
    else
    {
        LTRACE << "is not in map";
    }
}

void
TypeFactory::deleteTmpSetType(const char* typeName)
{
    const DBObject* resultType = const_cast<SetType*>(mapSetType(typeName));//is ok because only short for find
    if (resultType)
    {
        DBObjectId toKill(resultType->getOId());
        toKill->setPersistent(false);
        toKill->setCached(false);
        LTRACE << "will be deleted from db";
    }
    else
    {
        LTRACE << "is not in map";
    }
}

const Type*
TypeFactory::ensurePersistence(Type* type)
{
    std::vector<Type*>::iterator iter;
    const Type* retval = 0;
    Type* ttype = 0;

    // deleting type if it is in the list of tempTypes
    if (theTempTypes)
    {
        for (iter = theTempTypes->begin(); iter < theTempTypes->end(); iter++)
        {
            if (*iter == type)
            {
                theTempTypes->erase(iter);
            }
        }
    }
    // check if the struct type is alread in the DBMS
    switch (type->getType())
    {
    case STRUCT:
    {
        TypeIterator<StructType> ist = createStructIter();
        while (ist.not_done())
        {
            ttype = ist.get_element();
            if (ttype->compatibleWith(type))
            {
                retval = ttype;
                break;
            }
            ist.advance();
        }
        if (!retval)
        {
            retval = addStructType(static_cast<const StructType*>(type));
        }
    }
    break;
    case MDDTYPE:
    {
        TypeIterator<MDDType> imd = createMDDIter();
        while (imd.not_done())
        {
            ttype = imd.get_element();
            if (ttype->compatibleWith(type))
            {
                retval = ttype;
                break;
            }
            imd.advance();
        }
        if (!retval)
        {
            retval = addMDDType(static_cast<const MDDType*>(type));
        }
    }
    break;
    case SETTYPE:
    {
        TypeIterator<SetType> ise = createSetIter();
        while (ise.not_done())
        {
            ttype = ise.get_element();
            if (ttype->compatibleWith(type))
            {
                retval = ttype;
                break;
            }
            ise.advance();
        }
        if (!retval)
        {
            retval = addSetType(static_cast<const SetType*>(type));
        }
    }
    break;
    case ULONG:
        retval = static_cast<Type*>(ObjectBroker::getObjectByOId(OId(ULONG, OId::ATOMICTYPEOID)));
        break;
    case USHORT:
        retval = static_cast<Type*>(ObjectBroker::getObjectByOId(OId(USHORT, OId::ATOMICTYPEOID)));
        break;
    case CHAR:
        retval = static_cast<Type*>(ObjectBroker::getObjectByOId(OId(CHAR, OId::ATOMICTYPEOID)));
        break;
    case BOOLTYPE:
        retval = static_cast<Type*>(ObjectBroker::getObjectByOId(OId(BOOLTYPE, OId::ATOMICTYPEOID)));
        break;
    case LONG:
        retval = static_cast<Type*>(ObjectBroker::getObjectByOId(OId(LONG, OId::ATOMICTYPEOID)));
        break;
    case SHORT:
        retval = static_cast<Type*>(ObjectBroker::getObjectByOId(OId(SHORT, OId::ATOMICTYPEOID)));
        break;
    case OCTET:
        retval = static_cast<Type*>(ObjectBroker::getObjectByOId(OId(OCTET, OId::ATOMICTYPEOID)));
        break;
    case DOUBLE:
        retval = static_cast<Type*>(ObjectBroker::getObjectByOId(OId(DOUBLE, OId::ATOMICTYPEOID)));
        break;
    case FLOAT:
        retval = static_cast<Type*>(ObjectBroker::getObjectByOId(OId(FLOAT, OId::ATOMICTYPEOID)));
        break;
    case COMPLEXTYPE1:
        retval = static_cast<Type*>(ObjectBroker::getObjectByOId(OId(COMPLEXTYPE1, OId::ATOMICTYPEOID)));
        break;
    case COMPLEXTYPE2:
        retval = static_cast<Type*>(ObjectBroker::getObjectByOId(OId(COMPLEXTYPE2, OId::ATOMICTYPEOID)));
        break;
    default:
        LTRACE << "ensurePersitence() is not a STRUCT/MDDTYPE/SETTYPE/ATOMIC " << type->getName();
        break;
    }
    if (!type->isPersistent())
    {
        delete type;
    }
    return retval;
}

TypeIterator<SetType>
TypeFactory::createSetIter()
{
    LTRACE << "createSetIter()";
    OIdSet* t = ObjectBroker::getAllObjects(OId::SETTYPEOID);
    TypeIterator<SetType> ti(*t);
    delete t;
    t = 0;
    return ti;
}

TypeIterator<StructType>
TypeFactory::createStructIter()
{
    LTRACE << "createStructIter()";
    OIdSet* t = ObjectBroker::getAllObjects(OId::STRUCTTYPEOID);
    TypeIterator<StructType> ti(*t);
    delete t;
    t = 0;
    return ti;
}

TypeIterator<MDDType>
TypeFactory::createMDDIter()
{
    OIdSet theMDDTypes;
    OIdSet* tempList = 0;
    OIdSet::iterator i;

    tempList = ObjectBroker::getAllObjects(OId::MDDTYPEOID);
    theMDDTypes = *tempList;
    delete tempList;

    tempList = ObjectBroker::getAllObjects(OId::MDDBASETYPEOID);
    while (!tempList->empty())
    {
        theMDDTypes.insert(*(tempList->begin()));
        tempList->erase(*(tempList->begin()));
    }
    delete tempList;

    tempList = ObjectBroker::getAllObjects(OId::MDDDIMTYPEOID);
    while (!tempList->empty())
    {
        theMDDTypes.insert(*(tempList->begin()));
        tempList->erase(*(tempList->begin()));
    }
    delete tempList;

    tempList = ObjectBroker::getAllObjects(OId::MDDDOMTYPEOID);
    while (!tempList->empty())
    {
        theMDDTypes.insert(*(tempList->begin()));
        tempList->erase(*(tempList->begin()));
    }
    delete tempList;

    return TypeIterator<MDDType>(theMDDTypes);
}
