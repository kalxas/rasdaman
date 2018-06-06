#include "qtcreatesettype.hh"
#include "qtmintervaldata.hh"
#include "qtunaryoperation.hh"
#include "qtnullvaluesdata.hh"
#include "relcatalogif/alltypes.hh"

const QtNode::QtNodeType QtCreateSetType::nodeType = QtNode::QT_CREATE_SET_TYPE;

QtCreateSetType::QtCreateSetType(const std::string& typeName2, const std::string& mddTypeName2, QtOperation* nullValuesNode2)
    : typeName(typeName2), mddTypeName(mddTypeName2), nullValuesNode(nullValuesNode2)
{
}

QtData* QtCreateSetType::evaluate()
{
    QtData* returnValue = NULL;
    const MDDType* mddType = TypeFactory::mapMDDType(this->mddTypeName.c_str());
    SetType* setType = new SetType(this->typeName.c_str(), const_cast<MDDType*>(mddType));

    if (this->nullValuesNode != NULL)
    {
        QtNullvaluesData* nullValues = static_cast<QtNullvaluesData*>(this->nullValuesNode->evaluate(NULL));
        setType->setNullValues(nullValues->getNullvaluesData());
        delete nullValues;
    }

    TypeFactory::addSetType(setType);
    delete setType;
    return returnValue;
}

void QtCreateSetType::checkType()
{
    // check if type already exists
    if (TypeFactory::mapSetType(this->typeName.c_str()) != NULL)
    {
        parseInfo.setErrorNo(969);
        parseInfo.setToken(this->typeName.c_str());
        throw parseInfo;
    }

    // check if the mdd type exists
    if (TypeFactory::mapMDDType(this->mddTypeName.c_str()) == NULL)
    {
        parseInfo.setErrorNo(973);
        parseInfo.setToken(this->mddTypeName.c_str());
        throw parseInfo;
    }

    // check if the null values are valid
    if (this->nullValuesNode != NULL)
    {
        this->nullValuesNode->checkType();
    }
}

void QtCreateSetType::printTree(int tab, std::ostream& s, __attribute__((unused)) QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCreateSetType Object" << std::endl;
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  CREATE TYPE " << typeName << " UNDER SET { " << mddTypeName << " }";
}

void QtCreateSetType::printAlgebraicExpression(std::ostream& s)
{
    s << "command<CREATE TYPE " << typeName << " UNDER SET { " << mddTypeName << " }>";
}

QtNode::QtNodeType QtCreateSetType::getNodeType() const
{
    return nodeType;
}
