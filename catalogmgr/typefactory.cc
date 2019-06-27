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

#include "typefactory.hh"
#include "reladminif/dbobject.hh"                  // for DBObjectId, DBObject
#include "reladminif/dbref.hh"                     // for DBRef
#include "reladminif/lists.h"                      // for OIdSet
#include "reladminif/objectbroker.hh"              // for ObjectBroker
#include "reladminif/oidif.hh"                     // for OId, operator<<, OId...
#include "relmddif/dbmddobj.hh"                    // for DBMDDObj
#include "relmddif/dbmddset.hh"                    // for DBMDDSet
#include "relmddif/mddid.hh"                       // for DBMDDObjId, DBMDDSetId
#include "relcatalogif/syntaxtypes.hh"             // for BOOL_NAME, CHAR_NAME
#include "relcatalogif/basetype.hh"                // for BaseType
#include "relcatalogif/booltype.hh"                // for BoolType
#include "relcatalogif/chartype.hh"                // for CharType
#include "relcatalogif/collectiontype.hh"          // for CollectionType
#include "relcatalogif/complextype.hh"             // for ComplexType1, Comple...
#include "relcatalogif/dbnullvalues.hh"            // for DBNullvalues
#include "relcatalogif/doubletype.hh"              // for DoubleType
#include "relcatalogif/floattype.hh"               // for FloatType
#include "relcatalogif/longtype.hh"                // for LongType
#include "relcatalogif/mddbasetype.hh"             // for MDDBaseType
#include "relcatalogif/mdddimensiontype.hh"        // for MDDDimensionType
#include "relcatalogif/mdddomaintype.hh"           // for MDDDomainType
#include "relcatalogif/mddtype.hh"                 // for MDDType, MDDType::MD...
#include "relcatalogif/octettype.hh"               // for OctetType
#include "relcatalogif/settype.hh"                 // for SetType
#include "relcatalogif/shorttype.hh"               // for ShortType
#include "relcatalogif/structtype.hh"              // for StructType
#include "relcatalogif/type.hh"                    // for Type
#include "relcatalogif/ulongtype.hh"               // for ULongType
#include "relcatalogif/ushorttype.hh"              // for UShortType
#include "raslib/error.hh"                         // for r_Error
#include "raslib/odmgtypes.hh"                     // for BOOLTYPE, CHAR, COMP...
#include "raslib/structuretype.hh"
#include <logging.hh>                              // for Writer, CTRACE, LTRACE

#include <boost/algorithm/string/predicate.hpp>    // for starts_with
#include <map>                                     // for map, _Rb_tree_const_...
#include <utility>                                 // for pair, make_pair
#include <vector>                                  // for vector, vector<>::it...

using namespace std;

TypeFactory *TypeFactory::myInstance = nullptr;

// This variable is not required since any struct
// type can now be deleted. This resulted as
// the resolution of ticket #88
const short TypeFactory::MaxBuiltInId = 11;

const char *OctetType::Name = "Octet";
const char *UShortType::Name = "UShort";
const char *ShortType::Name = "Short";
const char *ULongType::Name = "ULong";
const char *LongType::Name = "Long";
const char *BoolType::Name = "Bool";
const char *CharType::Name = "Char";
const char *FloatType::Name = "Float";
const char *DoubleType::Name = "Double";
const char *ComplexType1::Name = "Complex";
const char *ComplexType2::Name = "Complexd";

const std::string TypeFactory::ANONYMOUS_CELL_TYPE_PREFIX = "__CELLTYPE__";

const map<string, string> TypeFactory::syntaxTypeInternalTypeMap =
    TypeFactory::createSyntaxTypeInternalTypeMap();
const map<string, string> TypeFactory::internalTypeSyntaxTypeMap =
    TypeFactory::createInternalTypeSyntaxTypeMap();

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
    ret.insert(std::make_pair(SyntaxType::COMPLEXTYPE1, ComplexType1::Name));
    ret.insert(std::make_pair(SyntaxType::COMPLEXTYPE2, ComplexType2::Name));

    return ret;
}

map<string, string> TypeFactory::createInternalTypeSyntaxTypeMap()
{
    map<string, string> ret;
    map<string, string> syntaxMap = createSyntaxTypeInternalTypeMap();

    for (auto mapIt = syntaxMap.begin(); mapIt != syntaxMap.end(); ++mapIt)
    {
        ret.insert(std::make_pair(mapIt->second, mapIt->first));
    }

    return ret;
}

string TypeFactory::getInternalTypeFromSyntaxType(const std::string &syntaxTypeName)
{
    string result = syntaxTypeName;
    auto it = syntaxTypeInternalTypeMap.find(syntaxTypeName);
    if (it != syntaxTypeInternalTypeMap.end())
    {
        result = it->second;
    }

    return result;
}

string TypeFactory::getSyntaxTypeFromInternalType(const std::string &internalTypeName)
{
    string result = internalTypeName;
    auto it = internalTypeSyntaxTypeMap.find(internalTypeName);
    if (it != internalTypeSyntaxTypeMap.end())
    {
        result = it->second;
    }

    return result;
}

// all atomic types given back by mapType()
// for managing the memory of temporary types
std::vector<Type *> *TypeFactory::theTempTypes = nullptr;

TypeFactory *TypeFactory::instance()
{
    if (myInstance == nullptr)
    {
        myInstance = new TypeFactory;
    }
    return myInstance;
}

const BaseType *TypeFactory::mapType(const char *typeName)
{
    BaseType *resultType = nullptr;
    resultType = static_cast<BaseType *>(
                     ObjectBroker::getObjectByName(OId::ATOMICTYPEOID, typeName));
    if (resultType == nullptr)
    {
        try
        {
            resultType = static_cast<BaseType *>(
                             ObjectBroker::getObjectByName(OId::STRUCTTYPEOID, typeName));
        }
        catch (const r_Error &)
        {
            resultType = nullptr;
        }
    }
    return resultType;
}

const StructType *TypeFactory::addStructType(const StructType *type)
{
    StructType *persistentType = nullptr;
    const StructType *retval = nullptr;
    if (type->isPersistent())
    {
        LTRACE << "type is persistent " << type->getName() << " " << type->getOId();
        retval = type;
    }
    else
    {
        persistentType = new StructType(const_cast<char *>(type->getTypeName()), type->getNumElems());
        for (unsigned int i = 0; i < type->getNumElems(); i++)
        {
            switch (type->getElemType(i)->getType())
            {
            case STRUCT:
                LTRACE << "element is struct type " << type->getElemName(i)
                       << " of type " << type->getElemType(i)->getName();
                LERROR << "Building a struct using a user-defined struct is currently not supported.";
                throw r_Error(STRUCTOFSTRUCTSDISABLED);
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
                LTRACE << "element is atomic type " << type->getElemName(i)
                       << " of type " << type->getElemType(i)->getName();
                persistentType->addElement(
                    type->getElemName(i),
                    static_cast<BaseType *>(ObjectBroker::getObjectByOId(
                                                type->getElemType(i)->getOId())));
                break;
            default:
                persistentType = nullptr;
                LTRACE << "addStructType(" << type->getTypeName() << ") unknown type "
                       << type->getOId() << type->getOId().getType();
                break;
            }
        }
        LTRACE << "type is now persistent " << persistentType->getName() << " "
               << persistentType->getOId();
        persistentType->setCached(true);
        persistentType->setPersistent(true);
        ObjectBroker::registerDBObject(persistentType);
        retval = persistentType;
    }
    return retval;
}

const SetType *TypeFactory::mapSetType(const char *typeName)
{
    // it is a user defined type
    SetType *resultType = nullptr;
    try
    {
        resultType = static_cast<SetType *>(
                         ObjectBroker::getObjectByName(OId::SETTYPEOID, typeName));
    }
    catch (const r_Error &)
    {
        resultType = nullptr;
    }
    return resultType;
}

const SetType *TypeFactory::addSetType(const SetType *type)
{
    SetType *persistentType = nullptr;
    const SetType *retval = nullptr;
    if (type->isPersistent())
    {
        LTRACE << "type is persistent " << type->getName() << " " << type->getOId();
        retval = type;
    }
    else
    {
        persistentType = new SetType(type->getTypeName(), const_cast<MDDType *>(addMDDType(type->getMDDType())));

        auto *interval = type->getNullValues();
        if (interval != nullptr)
        {
            persistentType->setNullValues(*((r_Nullvalues *)interval));
        }

        persistentType->setPersistent(true);
        LTRACE << "type is now persistent " << type->getName() << " "
               << persistentType->getOId();
        ObjectBroker::registerDBObject(persistentType);
        persistentType->setCached(true);
        retval = persistentType;
    }
    return retval;
}

const MDDType *TypeFactory::mapMDDType(const char *typeName)
{
    MDDType *resultType = nullptr;
    try
    {
        resultType = ObjectBroker::getMDDTypeByName(typeName);
    }
    catch (...)
    {
        resultType = nullptr;
    }
    return resultType;
}

const MDDType *TypeFactory::addMDDType(const MDDType *type)
{
    MDDType *persistentType = nullptr;
    const MDDType *retval = nullptr;
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
            persistentType = new MDDType(type->getTypeName());
            break;
        case MDDType::MDDBASETYPE:
            persistentType = new MDDBaseType(type->getTypeName(),
                addStructType(static_cast<const StructType *>(static_cast<const MDDBaseType *>(type)->getBaseType())));
            break;
        case MDDType::MDDDOMAINTYPE:
            persistentType = new MDDDomainType(type->getTypeName(),
                addStructType(static_cast<const StructType *>(static_cast<const MDDBaseType *>(type)->getBaseType())),
                *static_cast<const MDDDomainType *>(type)->getDomain());
            break;
        case MDDType::MDDDIMENSIONTYPE:
            persistentType = new MDDDimensionType(type->getTypeName(),
                addStructType(static_cast<const StructType *>(static_cast<const MDDBaseType *>(type)->getBaseType())),
                static_cast<const MDDDimensionType *>(type)->getDimension());
            break;
        default:
            LWARNING << "MDD sub-type '" << type->getName() << "' unknown.";
            break;
        }
        if (persistentType != nullptr)
        {
            persistentType->setPersistent(true);
            LTRACE << "adding " << persistentType->getName() << " "
                   << persistentType->getOId();
            persistentType->setCached(true);
            ObjectBroker::registerDBObject(persistentType);
            retval = persistentType;
        }
        else
        {
            // error message was already given in switch default
        }
    }
    return retval;
}

Type *TypeFactory::addTempType(Type *type)
{
    // put in front to avoid deletion of MDDTypes still referenced
    // by an MDDBaseType.
    theTempTypes->insert(theTempTypes->begin(), type);
    return type;
}

void TypeFactory::initialize()
{
    // to initailize the typefactory
    if (!theTempTypes)
    {
        theTempTypes = new std::vector<Type *>;
    }
}

void TypeFactory::freeTempTypes()
{
    // delete all temporary types
    if (theTempTypes)
    {
        for (auto iter = theTempTypes->begin(); iter != theTempTypes->end(); iter++)
        {
            delete *iter;
            *iter = nullptr;
        }
        delete theTempTypes;
        theTempTypes = nullptr;
    }
}

TypeFactory::TypeFactory() = default;

bool TypeFactory::deleteStructType(const char *typeName)
{
    const DBObject *resultType = mapType(typeName);
    if (resultType)
    {
        for (auto miter = createMDDIter(); miter.not_done(); miter.advance())
        {
            if (miter.get_element()->getSubtype() != MDDType::MDDONLYTYPE)
            {
                if ((static_cast<MDDBaseType *>(miter.get_element().ptr()))->getBaseType() == resultType)
                {
                    LERROR << "base type " << typeName << " cannot be deleted, in use by "
                           << "an existing MDD object " << miter.get_element()->getName();
                    throw r_Error(TYPEISINUSE);
                }
            }
        }

        DBObjectId toKill(resultType->getOId());
        toKill->setPersistent(false);
        toKill->setCached(false);
        LDEBUG << "Base type " << typeName << " will be deleted from the database";
        return true;
    }
    else
    {
        LWARNING << "base type " << typeName << " cannot be deleted: not existing";
        return false;
    }
}

bool TypeFactory::deleteMDDType(const char *typeName)
{
    // is ok because only short for find
    const MDDType *resultType = mapMDDType(typeName);
    if (resultType)
    {
        for (auto miter = createSetIter(); miter.not_done(); miter.advance())
        {
            if (miter.get_element()->getMDDType() == resultType)
            {
                LERROR << "MDD type " << typeName << " cannot be deleted, in use by "
                       << "existing collection " << miter.get_element()->getName();
                throw r_Error(TYPEISINUSE);
            }
        }
        if (resultType->getSubtype() != MDDType::MDDONLYTYPE)
        {
            // mdd only types can not be in mdd objects
            auto objs = unique_ptr<OIdSet>(ObjectBroker::getAllObjects(OId::MDDOID));
            for (auto miter = objs->begin(); miter != objs->end(); miter++)
            {
                if (DBMDDObjId(*miter)->getMDDBaseType() == resultType)
                {
                    LERROR << "MDD type " << typeName << " cannot be deleted, in use by "
                           << "an existing MDD object " << *miter;
                    throw r_Error(TYPEISINUSE);
                }
            }
        }
        // TODO-GM: explain
        // check if the base type is an annonymous type
        if (resultType->getSubtype() != MDDType::MDDONLYTYPE)
        {
            const auto *baseType =
                static_cast<const MDDBaseType *>(resultType)->getBaseType();
            std::string btname(baseType->getTypeName());
            if (boost::starts_with(btname, TypeFactory::ANONYMOUS_CELL_TYPE_PREFIX))
            {
                DBObjectId baseTypeToKill(baseType->getOId());
                baseTypeToKill->setPersistent(false);
                baseTypeToKill->setCached(false);
            }
        }

        DBObjectId toKill(resultType->getOId());
        toKill->setPersistent(false);
        toKill->setCached(false);
        LDEBUG << "MDD type: " << typeName << " will be deleted from db";
        return true;
    }
    else
    {
        LWARNING << "MDD type: " << typeName << " cannot be deleted: not existing";
        return false;
    }
}

bool TypeFactory::deleteSetType(const char *typeName)
{
    const DBObject *resultType = mapSetType(typeName);
    if (resultType)
    {
        auto objs = unique_ptr<OIdSet>(ObjectBroker::getAllObjects(OId::MDDCOLLOID));
        for (auto miter = objs->begin(); miter != objs->end(); miter++)
        {
            if (DBMDDSetId(*miter)->getCollType() == resultType)
            {
                LERROR << "set type " << typeName << " cannot be deleted, in use by "
                       << "existing collection " << *miter;
                throw r_Error(TYPEISINUSE);
            }
        }
        DBObjectId toKill(resultType->getOId());
        toKill->setPersistent(false);
        toKill->setCached(false);
        LDEBUG << "set type: " << typeName << " will be deleted from db";
        return true;
    }
    else
    {
        LWARNING << "set type: " << typeName << " cannot be deleted: not existing";
        return false;
    }
}

void TypeFactory::deleteTmpMDDType(const char *typeName)
{
    const MDDType *resultType = mapMDDType(typeName);
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

void TypeFactory::deleteTmpSetType(const char *typeName)
{
    const DBObject *resultType = const_cast<SetType *>(mapSetType(typeName));
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

const Type *TypeFactory::ensurePersistence(Type *type)
{
    std::vector<Type *>::iterator iter;
    const Type *retval = nullptr;
    Type *ttype = nullptr;

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
            retval = addStructType(static_cast<const StructType *>(type));
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
            retval = addMDDType(static_cast<const MDDType *>(type));
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
            retval = addSetType(static_cast<const SetType *>(type));
        }
    }
    break;
    case ULONG:
        retval = static_cast<Type *>(ObjectBroker::getObjectByOId(OId(ULONG, OId::ATOMICTYPEOID)));
        break;
    case USHORT:
        retval = static_cast<Type *>(ObjectBroker::getObjectByOId(OId(USHORT, OId::ATOMICTYPEOID)));
        break;
    case CHAR:
        retval = static_cast<Type *>(ObjectBroker::getObjectByOId(OId(CHAR, OId::ATOMICTYPEOID)));
        break;
    case BOOLTYPE:
        retval = static_cast<Type *>(ObjectBroker::getObjectByOId(OId(BOOLTYPE, OId::ATOMICTYPEOID)));
        break;
    case LONG:
        retval = static_cast<Type *>(ObjectBroker::getObjectByOId(OId(LONG, OId::ATOMICTYPEOID)));
        break;
    case SHORT:
        retval = static_cast<Type *>(ObjectBroker::getObjectByOId(OId(SHORT, OId::ATOMICTYPEOID)));
        break;
    case OCTET:
        retval = static_cast<Type *>(ObjectBroker::getObjectByOId(OId(OCTET, OId::ATOMICTYPEOID)));
        break;
    case DOUBLE:
        retval = static_cast<Type *>(ObjectBroker::getObjectByOId(OId(DOUBLE, OId::ATOMICTYPEOID)));
        break;
    case FLOAT:
        retval = static_cast<Type *>(ObjectBroker::getObjectByOId(OId(FLOAT, OId::ATOMICTYPEOID)));
        break;
    case COMPLEXTYPE1:
        retval = static_cast<Type *>(ObjectBroker::getObjectByOId(OId(COMPLEXTYPE1, OId::ATOMICTYPEOID)));
        break;
    case COMPLEXTYPE2:
        retval = static_cast<Type *>(ObjectBroker::getObjectByOId(OId(COMPLEXTYPE2, OId::ATOMICTYPEOID)));
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

TypeIterator<SetType> TypeFactory::createSetIter()
{
    OIdSet *t = ObjectBroker::getAllObjects(OId::SETTYPEOID);
    TypeIterator<SetType> ti(*t);
    delete t;
    t = nullptr;
    return ti;
}

TypeIterator<StructType> TypeFactory::createStructIter()
{
    OIdSet *t = ObjectBroker::getAllObjects(OId::STRUCTTYPEOID);
    TypeIterator<StructType> ti(*t);
    delete t;
    t = nullptr;
    return ti;
}

TypeIterator<MDDType> TypeFactory::createMDDIter()
{
    OIdSet theMDDTypes;
    OIdSet *tempList = nullptr;
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

const Type *TypeFactory::fromRaslibType(const r_Type *type)
{
    if (!type->isBaseType() && !type->isPrimitiveType() && !type->isStructType())
    {
        LERROR << "cannot convert non-base type " << type->type_id();
        throw r_Error(r_Error::r_Error_General);
    }

    if (type->isPrimitiveType())
    {
        switch (type->type_id())
        {
        case r_Type::BOOL:
            return TypeFactory::mapType(BoolType::Name);
        case r_Type::OCTET:
            return TypeFactory::mapType(OctetType::Name);
        case r_Type::CHAR:
            return TypeFactory::mapType(CharType::Name);
        case r_Type::SHORT:
            return TypeFactory::mapType(ShortType::Name);
        case r_Type::USHORT:
            return TypeFactory::mapType(UShortType::Name);
        case r_Type::LONG:
            return TypeFactory::mapType(LongType::Name);
        case r_Type::ULONG:
            return TypeFactory::mapType(ULongType::Name);
        case r_Type::FLOAT:
            return TypeFactory::mapType(FloatType::Name);
        case r_Type::DOUBLE:
            return TypeFactory::mapType(DoubleType::Name);
        case r_Type::COMPLEXTYPE1:
            return TypeFactory::mapType(ComplexType1::Name);
        case r_Type::COMPLEXTYPE2:
            return TypeFactory::mapType(ComplexType2::Name);
        default:
            LERROR << "unknown type " << type->type_id();
            throw r_Error(r_Error::r_Error_General);
        }
    }
    else if (type->isStructType())
    {
        const r_Structure_Type *structType = (const r_Structure_Type *) type;
        std::vector<const Type *> attributeTypes(structType->count_elements());
        for (size_t i = 0; i < structType->count_elements(); ++i)
        {
            attributeTypes[i] = fromRaslibType(&((*structType)[i].type_of()));
        }
        StructType *resultType = new StructType("tmp", structType->count_elements());
        for (size_t i = 0; i < structType->count_elements(); ++i)
        {
            resultType->addElement((*structType)[i].name(), (const BaseType *) attributeTypes[i]);
        }
        return TypeFactory::addTempType(resultType);
    }
    else
    {
        LERROR << "unknown type " << type->type_id();
        throw r_Error(r_Error::r_Error_General);
    }
}

