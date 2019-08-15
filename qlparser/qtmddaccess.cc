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
/*************************************************************
 *
 *
 * PURPOSE:
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#include "config.h"
#include "raslib/rmdebug.hh"

#include "qlparser/qtmddaccess.hh"
#include "qlparser/qtmdd.hh"

#include <iostream>
#ifndef CPPSTDLIB
#include <ospace/string.h> // STL<ToolKit>
#else
#include <string>
using namespace std;
#endif

#include "mddmgr/mddcoll.hh"
#include "mddmgr/mddcolliter.hh"

#include <logging.hh>

#include "servercomm/servercomm.hh"
#include "servercomm/cliententry.hh"

#include "lockmgr/lockmanager.hh"

extern ClientTblElt *currentClientTblElt;

const QtNode::QtNodeType QtMDDAccess::nodeType = QT_MDD_ACCESS;

QtMDDAccess::QtMDDAccess(const QtCollection &collectionNew)
    :  QtONCStream(),
       collection(collectionNew),
       mddColl(NULL),
       mddIter(NULL)
{
}

QtMDDAccess::QtMDDAccess(const QtCollection &collectionNew, const string &initName)
    :  QtONCStream(),
       collection(collectionNew),
       iteratorName(initName),
       mddColl(NULL),
       mddIter(NULL)
{

}

QtMDDAccess::~QtMDDAccess()
{
//just to be on the safe side
    close();
}


void
QtMDDAccess::open()
{
    startTimer("QtMDDAccess");

    // delete an existing iterator
    if (mddIter)
    {
        delete mddIter;
        mddIter = NULL;
    }

    // create the iterator
    mddIter = mddColl->createIterator();

    //for( mddIter->reset(); mddIter->notDone(); mddIter->advance() )
    //  mddIter->getElement()->printStatus();

    mddIter->reset();

    pauseTimer();
}

QtNode::QtDataList *
QtMDDAccess::next()
{
    resumeTimer();

    QtDataList *returnValue = NULL;
    MDDObj *ptr = NULL;

    if (mddColl && mddIter && mddIter->notDone())
    {
        //
        // create a list with a pointer to the next element of the mdd collection
        //

        // encapsulate the next MDDObj in an QtMDD object
        ptr =  mddIter->getElement();
        if (configuration.isLockMgrOn())
        {
            if (ptr)
            {
                LockManager *lockmanager = LockManager::Instance();
                std::vector<boost::shared_ptr<Tile>> *tiles = ptr->getTiles();
                lockmanager->lockTiles(tiles);
                delete tiles;
            }
        }

        CollectionType *collType = const_cast<CollectionType *>(mddColl->getCollectionType());
        if (collType)
        {
            auto *dbmi = collType->getNullValues();
            if (dbmi != NULL)
            {
                ptr->setNullValues(dbmi);
            }
        }
        
        QtMDD  *elem = new QtMDD(ptr, iteratorName);
        elem->setCollType(collType);

        // create the list
        QtNode::QtDataList *dataList = new QtNode::QtDataList(1); // create container to contain one element

        // insert the element into the list
        (*dataList)[0] = elem;

        // if mddColl is not persistent delete thist from
        // collection to avoid multiple destruction
        if (!mddColl->isPersistent())
        {
            mddColl->remove(ptr);
            mddIter->reset();
        }
        else
        {
            // increment the iterator
            mddIter->advance();
        }

        returnValue = dataList;
    }

    pauseTimer();

    return returnValue;
}


void
QtMDDAccess::close()
{
    // delete the mdd iterator
    if (mddIter)
    {
        delete mddIter;
        mddIter = NULL;
    }

    stopTimer();
}


void
QtMDDAccess::reset()
{
    if (mddIter)
    {
        mddIter->reset();
    }
}


void
QtMDDAccess::printTree(int tab, ostream &s, QtChildType /*mode*/)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtMDDAccess Object: type " << flush;
    dataStreamType.printStatus(s);
    s << getEvaluationTime();
    s << endl;

    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << collection.getCollectionName().c_str()
      << " <- " << iteratorName.c_str() << endl;
}



void
QtMDDAccess::printAlgebraicExpression(ostream &s)
{
    s << collection.getCollectionName().c_str() << " as " << iteratorName.c_str() << flush;
}



const QtTypeTuple &
QtMDDAccess::checkType()
{
    dataStreamType = QtTypeTuple(0);

    //
    // create the collection and add it to the list in the client table entry
    //

    if (collection.getHostname() != "" && collection.getHostname() != "localhost")
    {
        LERROR << "Non-local collection is unsupported";
        parseInfo.setErrorNo(499); //to be changed
        throw parseInfo;
    }

    try
    {
        mddColl = MDDColl::getMDDCollection(collection.getCollectionName().c_str());

        if (currentClientTblElt)
        {
            if (!currentClientTblElt->persColls)
            {
                currentClientTblElt->persColls = new vector<MDDColl *>();
            }
            currentClientTblElt->persColls->push_back(mddColl);
        }
        else
        {
            LERROR << "Internal Error in QtMDDAccess::open(): No client context available";
        }
    }
    catch (...)
    {
        LERROR << "Collection " << collection.getCollectionName() << " is unknown";
        parseInfo.setErrorNo(355);
        throw parseInfo;
    }

    const auto *collType = mddColl->getCollectionType();
    if (!collType)
    {
        LERROR << "No collection type available";
    }

    dataStreamType = QtTypeTuple(1);

    dataStreamType.tuple[0].setType(collType->getMDDType());
    dataStreamType.tuple[0].setName(iteratorName.c_str());

    return dataStreamType;
}
