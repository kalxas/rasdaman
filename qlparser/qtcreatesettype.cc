#include "qtcreatesettype.hh"
#include "qtmintervaldata.hh"
#include "qtunaryoperation.hh"
#include "relcatalogif/alltypes.hh"

const QtNode::QtNodeType QtCreateSetType::nodeType = QtNode::QT_CREATE_SET_TYPE;

QtCreateSetType::QtCreateSetType(const std::string &typeName, const std::string &mddTypeName, QtOperation *nullValuesNode)
    : typeName(typeName), mddTypeName(mddTypeName), nullValuesNode(nullValuesNode)
{
}

QtData* QtCreateSetType::evaluate()
{
    const MDDType *mddType = TypeFactory::mapMDDType(this->mddTypeName.c_str());
    SetType *setType = new SetType(this->typeName.c_str(), (MDDType*) mddType);

    if (this->nullValuesNode != NULL)
    {
        QtMintervalData* nullValues = (QtMintervalData*) this->nullValuesNode->evaluate(NULL);
        setType->setNullValues(nullValues->getMintervalData());
        delete nullValues;
    }

    TypeFactory::addSetType(setType);
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

void QtCreateSetType::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(tab).c_str() << "QtCreateSetType Object" << std::endl;
    s << SPACE_STR(tab).c_str() << "  CREATE TYPE " << typeName << " UNDER SET { " << mddTypeName << " }";
}

void QtCreateSetType::printAlgebraicExpression(std::ostream &s)
{
    s << "command<CREATE TYPE " << typeName << " UNDER SET { " << mddTypeName << " }>";
}

QtNode::QtNodeType QtCreateSetType::getNodeType() const
{
    return nodeType;
}
