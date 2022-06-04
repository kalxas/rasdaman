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
#include "qtcreatesettype.hh"
#include "qtmintervaldata.hh"
#include "qtunaryoperation.hh"
#include "qtnullvaluesdata.hh"
#include "relcatalogif/alltypes.hh"
#include "relcatalogif/typefactory.hh"

const size_t QtCreateSetType::MAX_SET_TYPE_NAME_LENGTH;

const QtNode::QtNodeType QtCreateSetType::nodeType = QtNode::QT_CREATE_SET_TYPE;

QtCreateSetType::QtCreateSetType(const std::string &typeName2, const std::string &mddTypeName2, QtOperation *nullValuesNode2)
    : typeName(typeName2), mddTypeName(mddTypeName2), nullValuesNode(nullValuesNode2)
{
}

QtData *QtCreateSetType::evaluate()
{
    QtData *returnValue = NULL;
    const MDDType *mddType = TypeFactory::mapMDDType(this->mddTypeName.c_str());
    SetType *setType = new SetType(this->typeName.c_str(), const_cast<MDDType *>(mddType));

    if (this->nullValuesNode != NULL)
    {
        QtNullvaluesData *nullValues = static_cast<QtNullvaluesData *>(this->nullValuesNode->evaluate(NULL));
        setType->setNullValues(nullValues->getNullvaluesData());
        delete nullValues;
    }

    TypeFactory::addSetType(setType);
    delete setType;
    return returnValue;
}

void QtCreateSetType::checkType()
{
    // check if the name is longer than 200 characters
    if (typeName.length() >= MAX_SET_TYPE_NAME_LENGTH)
    {
        LERROR << "The set type name is longer than 200 characters.";
        parseInfo.setErrorNo(SET_TYPE_NAME_LENGTH_EXCEEDED);
        throw parseInfo;
    }
    // check if type already exists
    if (TypeFactory::mapSetType(this->typeName.c_str()) != NULL)
    {
        parseInfo.setErrorNo(TYPE_ALREADYEXISTS);
        parseInfo.setToken(this->typeName.c_str());
        throw parseInfo;
    }

    // check if the mdd type exists
    if (TypeFactory::mapMDDType(this->mddTypeName.c_str()) == NULL)
    {
        parseInfo.setErrorNo(MARRAYTYPE_INVALID);
        parseInfo.setToken(this->mddTypeName.c_str());
        throw parseInfo;
    }

    // check if the null values are valid
    if (this->nullValuesNode != NULL)
    {
        this->nullValuesNode->checkType();
    }
}

void QtCreateSetType::printTree(int tab, std::ostream &s, __attribute__((unused)) QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCreateSetType Object" << std::endl;
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  CREATE TYPE " << typeName << " UNDER SET { " << mddTypeName << " }";
}

void QtCreateSetType::printAlgebraicExpression(std::ostream &s)
{
    s << "command<CREATE TYPE " << typeName << " UNDER SET { " << mddTypeName << " }>";
}

QtNode::QtNodeType QtCreateSetType::getNodeType() const
{
    return nodeType;
}
