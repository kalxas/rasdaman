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

using namespace std;

#include "server/createinitmdd.hh"
#include <iomanip>
#include "reladminif/objectbroker.hh"
#include "storagemgr/sstoragelayout.hh"
#include "raslib/mitera.hh"
#include "mymalloc/mymalloc.h"
#include "relcatalogif/basetype.hh"

#include <logging.hh>

//#include <akgtime.hh>

FastCollectionCreator::FastCollectionCreator(const char *collName, const char *collTypeName)
{
    collectionName = collName;

    collectionTypeName = collTypeName;
}

r_OId FastCollectionCreator::createCollection()
{
    verifyName(collectionName);

    // allocate a new OId
    EOId eOId;
    EOId::allocateEOId(eOId, OId::MDDCOLLOID);
    r_OId oid = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());

    CollectionType *collType = static_cast<CollectionType *>(const_cast<SetType *>(TypeFactory::mapSetType(collectionTypeName)));

    LINFO << "Creating collection " << collectionName << " with type " << collectionTypeName << "...";
    if (collType)
    {
        try
        {
            MDDColl *coll = MDDColl::createMDDCollection(collectionName, OId(oid.get_local_oid()), collType);
            delete coll;
            LINFO << "OK";
        }
        catch (r_Error &obj)
        {
            if (obj.get_kind() == r_Error::r_Error_NameNotUnique)
            {
                LERROR << "collection name exists already... FAILED";
            }
            else
            {
                LERROR << obj.get_errorno() << " " << obj.what();
            }
            throw;
        }
    }
    else
    {
        LERROR << "collection type not found... FAILED";
        throw r_Error(COLLTYPE_NULL);
    }

    return oid;
}

void FastCollectionCreator::verifyName(const char *name)
{
    if (!name)
    {
        LERROR << "FastCollectionCreator::verifyName() name is null!";
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
        LERROR << "FastCollectionCreator::verifyName(" << name << ") invalid name!";
        throw r_Error(INVALIDOBJECTNAME);
    }
}

//###################################################################################################

FastMDDCreator::FastMDDCreator()
{
    comprData = 0;

    storageFormat = r_TMC;
    formatParams = NULL;
}

FastMDDCreator::~FastMDDCreator()
{
    if (comprData)
    {
        delete[](comprData);
        comprData = nullptr;
    }
}

void FastMDDCreator::setCollectionName(const char *collName)
{
    collectionName = collName;
}

void FastMDDCreator::setMDDTypeName(const char *_mddTypeName)
{
    mddTypeName = _mddTypeName;
}

void FastMDDCreator::verifyCompatibility(MDDColl *collection)
{
    if (collection->isPersistent())
    {
        //LINFO << "OK, colection exists and is  persistent";
    }
    else
    {
        throw r_Error(SYSTEM_COLLECTION_NOT_WRITABLE);
    }

    auto collTypeStructure = collection->getCollectionType()->getTypeStructure();
    //LINFO << "collTypeStructure=" << collTypeStructure;

    const MDDType *mddType = TypeFactory::mapMDDType(mddTypeName.c_str());
    if (mddType == NULL)
    {
        throw r_Error(MDDTYPE_NULL);
    }

    auto mddTypeStructure = mddType->getTypeStructure();
    //LINFO << "mddTypeStructure=" << mddTypeStructure;

    if (mddType->compatibleWithDomain(&definitionInterval))
    {
        //LINFO << "compatibil with domain: " << definitionInterval;
    }
    else
    {
        throw r_Error(r_Error::r_Error_CollectionElementTypeMismatch);
    }
    //LERROR << "incompatibil with domain";

    if (collection->getCollectionType()->compatibleWith(mddType))
    {
        //LINFO << "compatibil with collection";
    }
    else
    {
        throw r_Error(r_Error::r_Error_CollectionElementTypeMismatch);
    }
    //LERROR << "incompatibil with collection";
}

r_OId FastMDDCreator::createMDD(const char *domain)
{
    definitionInterval = r_Minterval(domain);

    MDDColl *collection = MDDColl::getMDDCollection(collectionName.c_str());

    verifyCompatibility(collection);

    const MDDType *mddType = TypeFactory::mapMDDType(mddTypeName.c_str());
    MDDBaseType *mddBaseType = static_cast<MDDBaseType *>(const_cast<MDDType *>(mddType));

    //allocate oid-ul;
    EOId eOId;
    EOId::allocateEOId(eOId, OId::MDDOID);

    mddOId = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());

    StorageLayout ms;
    ms.setTileSize(StorageLayout::DefaultTileSize);
    ms.setIndexType(StorageLayout::DefaultIndexType);
    ms.setTilingScheme(StorageLayout::DefaultTilingScheme);
    if (definitionInterval.dimension() == StorageLayout::DefaultTileConfiguration.dimension())
    {
        ms.setTileConfiguration(StorageLayout::DefaultTileConfiguration);
    }

    mymdd = new MDDObj(mddBaseType, definitionInterval, eOId.getOId(), ms);

    cellSize = static_cast<int>(mymdd->getCellType()->getSize());

    collection->insert(mymdd);

    collection->releaseAll();

    delete collection;

    return mddOId;
}

r_OId FastMDDCreator::createRCxMDD(const char *domain, const char *tileDomain)
{
    definitionInterval = r_Minterval(domain);
    r_Minterval tileInterval = r_Minterval(tileDomain);

    MDDColl *collection = MDDColl::getMDDCollection(collectionName.c_str());

    verifyCompatibility(collection);

    const MDDType *mddType = TypeFactory::mapMDDType(mddTypeName.c_str());
    MDDBaseType *mddBaseType = static_cast<MDDBaseType *>(const_cast<MDDType *>(mddType));

    //allocate oid-ul;
    EOId eOId;
    EOId::allocateEOId(eOId, OId::MDDOID);

    mddOId = r_OId(eOId.getSystemName(), eOId.getBaseName(), eOId.getOId());

    StorageLayout ms;
    ms.setTileSize(tileInterval.cell_count() * mddBaseType->getBaseType()->getSize());
    ms.setDataFormat(r_ZLib);
    ms.setIndexType(r_Reg_Computed_Index);
    ms.setTilingScheme(r_RegularTiling);
    ms.setTileConfiguration(tileInterval);
    mymdd = new MDDObj(mddBaseType, definitionInterval, eOId.getOId(), ms);

    cellSize = static_cast<int>(mymdd->getCellType()->getSize());

    collection->insert(mymdd);

    collection->releaseAll();

    delete collection;
    return mddOId;
}

vector<r_Minterval> FastMDDCreator::getTileDomains(r_OId mddOId2, const char *stripeDomain)
{
    mymdd = new MDDObj(OId(mddOId2.get_local_oid()));

    r_Minterval stripeInterval(stripeDomain);

    auto *tiles = mymdd->intersect(stripeInterval);

    vector<r_Minterval> result;

    for (unsigned int i = 0; i < tiles->size(); i++)
        result.push_back((*tiles)[i]->getDomain());

    delete tiles;
    delete mymdd;
    return result;
}

void FastMDDCreator::addStripe(r_OId _mddOId, const char *stripeDomain, const char *tileDomain)
{
    mddOId = _mddOId;

    r_Minterval stripeInterval(stripeDomain);
    r_Minterval tileInterval(tileDomain);

    mymdd = new MDDObj(OId(mddOId.get_local_oid()));
    cellSize = static_cast<int>(mymdd->getCellType()->getSize());
    const BaseType *baseType = mymdd->getMDDBaseType()->getBaseType();

    r_MiterArea iter(&tileInterval, &stripeInterval);

    while (!iter.isDone())
    {
        //iterate through the partitions in the search domain
        r_Minterval currentSlInterval = iter.nextArea();
        //LINFO << "inserting tile: " << currentSlInterval;

        createCompressedTileData(currentSlInterval, baseType);

        Tile *tile = new Tile(currentSlInterval, baseType, comprData, static_cast<r_Bytes>(comprDataSize), storageFormat);
        tile->setPersistent(true);

        mymdd->insertTile(tile);
    }

    delete mymdd;
}

void FastMDDCreator::createCompressedTileData(r_Minterval &tileInterval, __attribute__((unused)) const BaseType *baseType)
{
    static r_Area lastSize = 0;
    auto uncompressedSize = tileInterval.cell_count() * static_cast<r_Area>(cellSize);

    if (comprData)
    {
        if (lastSize == uncompressedSize)
        {
            return;
        }
        else
        {
            delete[](comprData);
            comprData = 0;
        }
    }

    r_Data_Format comprMode = storageFormat;
    ;

    char *dataPtr = new char[uncompressedSize];
    memset(dataPtr, 0, uncompressedSize);

    r_ULong newSize = static_cast<r_ULong>(uncompressedSize);
    comprData = dataPtr;
    comprDataSize = static_cast<int>(newSize);
    delete[](dataPtr);

    lastSize = uncompressedSize;
}
