#include "mymalloc/mymalloc.h"
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

#include "config.h"
#include "qlparser/qtmdd.hh"
#include "qlparser/qtscalardata.hh"
#include "qlparser/qtmintervaldata.hh"
#include "qlparser/qtoperation.hh"

#include "mddmgr/mddobj.hh"
#include "mddmgr/mddcoll.hh"
#include "tilemgr/tile.hh"
#include "relcatalogif/typefactory.hh"
#include "relcatalogif/mdddimensiontype.hh"
#include "relcatalogif/basetype.hh"
#include "relcatalogif/collectiontype.hh"

#include <iostream>
#include <string>
#include <iostream>

using namespace std;

// defined in servercomm.cc
extern MDDColl *mddConstants;

QtMDD::QtMDD(MDDObj *ptr)
    : QtData(),
      mddObject(ptr),
      fromConversion(false)
{
    if (ptr && ptr->isPersistent())
    {
        setLifetime(QtData::QT_PERSISTENT);
    }
    else
    {
        setLifetime(QtData::QT_TRANSIENT);
    }

    if (ptr)
    {
        loadDomain = ptr->getDefinitionDomain();
    }
}


QtMDD::QtMDD(MDDObj *ptr, string name)
    : QtData(name),
      mddObject(ptr),
      fromConversion(false)
{
    if (ptr && ptr->isPersistent())
    {
        setLifetime(QtData::QT_PERSISTENT);
    }
    else
    {
        setLifetime(QtData::QT_TRANSIENT);
    }

    if (ptr)
    {
        loadDomain = ptr->getCurrentDomain();
    }
}


QtMDD::QtMDD(QtOperation *mintervalOp, list<QtScalarData *> *literalList)
    : QtData(), mddObject(0), fromConversion(false)
{
    list<QtScalarData *>::iterator         elemIter;
    QtScalarData                           *scalarElem = NULL;

    //
    // evaluate domain
    //

    if (mintervalOp)
    {

        QtData *operand = mintervalOp->evaluate(NULL);

        if (operand->getDataType() != QT_MINTERVAL)
        {
            LERROR << "Error: QtMDD( QtOperation*, list<QtScalarData*>* ) - Can not evaluate domain expression to an minterval." ;
            ParseInfo errorInfo = getParseInfo();
            errorInfo.setErrorNo(DOMAINEVALUATIONERROR);
            throw errorInfo;
        }

        r_Minterval domain = (static_cast<QtMintervalData *>(operand))->getMintervalData();

        // delete old operand
        if (operand)
        {
            operand->deleteRef();
        }

        //
        // determine base type
        //

        if (literalList->size() != 0)
        {
            scalarElem = *(literalList->begin());
            const BaseType *baseType = scalarElem->getValueType();
            //used to check if the MDDs are of the same type
            char *baseStructure = baseType->getTypeStructure();

            //
            // allocate memory and fill it with cell values of the list
            //
            unsigned long cellCount = 0;
            unsigned long cellSize  = baseType->getSize();
            char *cellBuffer   = static_cast<char *>(mymalloc(domain.cell_count() * cellSize));
            char *bufferOffset = cellBuffer;

            for (elemIter = literalList->begin(); elemIter != literalList->end(); elemIter++)
            {
                scalarElem = *elemIter;
                cellCount++;

                // do not write beyond array boundary
                if (cellCount <= domain.cell_count())
                {
                    char *scalarElemTypeStructure = scalarElem->getTypeStructure();
                    if (strcmp(scalarElemTypeStructure, baseStructure) != 0)
                    {
                        LERROR << "Error: QtMDD() - All cell values of an MDD must be of the same type.";
                        free(cellBuffer);
                        free(scalarElemTypeStructure);
                        cellBuffer = NULL;
                        ParseInfo errorInfo = getParseInfo();
                        errorInfo.setErrorNo(PARSER_MDDCELLTYPEMUSTBEUNIFORM);
                        throw errorInfo;
                    }
                    free(scalarElemTypeStructure);
                    memcpy(bufferOffset, (scalarElem->getValueBuffer()), cellSize);
                    bufferOffset += cellSize;
                }
            }

            free(baseStructure);
            // delete literal list - done by caller
            //  delete literalList;

            if (cellCount != domain.cell_count())
            {
                LERROR << "Error: QtMDD() - Number of cells specified does not match the number of cells of the given spatial domain.";
                free(cellBuffer);
                cellBuffer = NULL;
                ParseInfo errorInfo = getParseInfo();
                errorInfo.setErrorNo(PARSER_CELLNUMMISMATCHWITHSDOM);
                throw errorInfo;
            }

            //
            // create transient tile
            //
            Tile *tile = new Tile(domain, baseType, true, cellBuffer, (r_Bytes)0, r_Array);

            //
            // create transiend mddObject and attach created tile
            //
            MDDDimensionType *mddDimensionType = new MDDDimensionType("tmp", baseType, domain.dimension());
            TypeFactory::addTempType(mddDimensionType);
            mddObject = new MDDObj(mddDimensionType, domain);
            mddObject->insertTile(tile);
            loadDomain = domain;
        }
        else
        {
            LERROR << "Internal Error: QtMDD( domain, literalList ) - list of literal lists is empty";
        }
    }
    else
    {
        LERROR << "Error: QtMDD( QtOperation*, list<QtScalarData*>* ) - Domain of MDD constructor has to be defined.";
        ParseInfo errorInfo = getParseInfo();
        errorInfo.setErrorNo(MDDCONSTRUCTOR_DOMAINUNDEFINED);
        throw errorInfo;
    }

}



QtMDD::QtMDD(__attribute__((unused)) int constantNo)
    : QtData(),
      mddObject(NULL),
      fromConversion(false)
{
    LTRACE << "QtMDD() - constant no " << constantNo;

    if (mddConstants)
    {

        MDDCollIter *mddIter = mddConstants->createIterator();
        //for( mddIter->reset(); mddIter->notDone(); mddIter->advance() )
        mddIter->reset();

        // take the MDD object
        mddObject = mddIter->getElement();

        // remove it from the constant list
        mddConstants->remove(mddObject);

        delete mddIter;
        mddIter = NULL;

        if (mddObject)
        {
            loadDomain = mddObject->getCurrentDomain();
        }
    }
    else
    {
        LERROR << "Error: QtMDD() - Unsatisfied MDD constant parameter.";
        ParseInfo errorInfo = getParseInfo();
        errorInfo.setErrorNo(UNSATISFIEDMDDCONSTANT);
        throw errorInfo;
    }

}



QtMDD::QtMDD(const QtMDD &obj)
    : QtData(obj),
      mddObject(obj.mddObject),
      fromConversion(false)
{
}


QtMDD::~QtMDD()
{
    //this causes problems when passing more than one trans mddobj
    if (mddObject && getLifetime() == QtData::QT_TRANSIENT)
    {
        LTRACE << "~QtMDD() - transient MDD object " << mddObject << " deleted";
        delete mddObject;
        mddObject = NULL;
    }
}

const CollectionType *
QtMDD::getCollType() const
{
    return mddObject->getCollType();
}

void
QtMDD::setCollType(const CollectionType *newCollType)
{
    mddObject->setCollType(newCollType);
}

BaseType *
QtMDD::getCellType() const
{
    return const_cast<BaseType *>(mddObject->getCellType());
}



unsigned long
QtMDD::getCellSize() const
{
    return mddObject->getCellType()->getSize();
}



QtDataType
QtMDD::getDataType() const
{
    return QT_MDD;
}



bool
QtMDD::equal(const QtData * /*obj*/) const
{
    int returnValue = false;  // not equal by initialization

    // Later on, MDD constants can be compared.

    return returnValue;
}



string
QtMDD::getSpelling() const
{
    string result;

    // no spelling right now

    return result;
}



char *QtMDD::getTypeStructure() const
{
    if (mddObject)
    {
        return mddObject->getMDDBaseType()->getTypeStructure();
    }
    else
    {
        return NULL;
    }
}



void
QtMDD::printStatus(ostream &stream) const
{
    if (mddObject)
    {
        stream << "MDD object: load domain: " << loadDomain << ", from conversion: " << fromConversion << endl;
    }
    else
    {
        stream << "<no object>" << endl;
    }

    QtData::printStatus(stream);

#ifdef DEBUG
    mddObject->printStatus(0, stream);

    auto *vec = mddObject->getTiles();
    for (unsigned int i = 0; i < vec->size(); i++)
    {
        ((*vec)[i])->printStatus();
    }
    delete vec;
    vec = NULL;
#endif
}
