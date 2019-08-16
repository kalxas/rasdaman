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
  * INCLUDE: nullvalues.hh
 *
 * MODULE:  rasodmg
 * CLASS:   NullValuesHandler
 *
 * COMMENTS:
 *      None
*/

#include "nullvalues.hh"
#include <stack>
#include "debug/debug-srv.hh"
#include "raslib/nullvalues.hh"
#include <logging.hh>

NullValuesHandler::NullValuesHandler()
    : nullValues(NULL), nullValuesCount(0)
{
}

NullValuesHandler::NullValuesHandler(r_Nullvalues *newNullValues)
    : nullValues(newNullValues), nullValuesCount(0)
{
}

NullValuesHandler::~NullValuesHandler() noexcept(false)
{
}

r_Nullvalues *
NullValuesHandler::getNullValues() const
{
//    if (nullValues != NULL)
//    {
//        LDEBUG << "returning null values " << nullValues->toString();
//    }
    return nullValues;
}

r_Double
NullValuesHandler::getNullValue() const
{
    if (nullValues != NULL)
    {
        const auto &nulls = nullValues->getNullvalues();
        if (!nulls.empty())
        {
            return nulls[0].first;
        }
    }
    return r_Double{};
}

void
NullValuesHandler::setNullValues(r_Nullvalues *newNullValues)
{
        nullValues = newNullValues;
}

unsigned long
NullValuesHandler::getNullValuesCount() const
{
    return nullValuesCount;
}

void
NullValuesHandler::setNullValuesCount(unsigned long count)
{
    nullValuesCount = count;
}

void
NullValuesHandler::cloneNullValues(const NullValuesHandler *obj)
{
    if (this != obj)
    {
        nullValues = obj->nullValues;
        nullValuesCount = obj->nullValuesCount;
    }
}

bool compareInterval(std::pair<r_Double, r_Double> i1, std::pair<r_Double, r_Double> i2) 
{ 
    return (i1.first < i2.first); 
}

r_Nullvalues *
NullValuesHandler::unionNullValues(r_Nullvalues *nullValues1, r_Nullvalues *nullValues2)
{
    if (!nullValues1&&!nullValues2)
    {
        return NULL;
    }
    else if (!nullValues1)
    {
        return nullValues2;
    }
    else if (!nullValues2)
    {
        return nullValues1;
    }
    auto tempNullValuesData = nullValues1->getNullvalues();
    auto tempNullValues2Data = nullValues2->getNullvalues();
    std::stack<std::pair<r_Double,r_Double>> stackNullValues;
    std::vector<std::pair<r_Double,r_Double>> resNullValuesData;
    r_Nullvalues *resNullValues = NULL;
    tempNullValuesData.insert(tempNullValuesData.end(),tempNullValues2Data.begin(),tempNullValues2Data.end());
    sort(tempNullValuesData.begin(),tempNullValuesData.end(),compareInterval);
    stackNullValues.push(tempNullValuesData[0]);
    for (int i =1; i < tempNullValuesData.size();i++)
    {
        std::pair<r_Double,r_Double> top = stackNullValues.top();
        if (top.second < tempNullValuesData[i].first)
        {
            stackNullValues.push(tempNullValuesData[i]);
        }
        else if (top.second < tempNullValuesData[i].second)
        {
            top.second = tempNullValuesData[i].second;
            stackNullValues.pop();
            stackNullValues.push(top);
        }
    }
    while(!stackNullValues.empty())
    {
        std::pair<r_Double,r_Double> tempPair = stackNullValues.top();
        stackNullValues.pop();
        resNullValuesData.push_back(tempPair);
    }
    return new r_Nullvalues(std::move(resNullValuesData));
    

}