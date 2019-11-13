#include "qtcreatemarraytype.hh"
#include "relcatalogif/alltypes.hh"
#include "qtmintervaldata.hh"
#include "qtunaryoperation.hh"
#include "qtcreatecelltype.hh"
#include <vector>
const size_t QtCreateMarrayType::MAX_MARRAY_TYPE_NAME_LENGTH;

const QtNode::QtNodeType QtCreateMarrayType::nodeType = QtNode::QT_CREATE_MDD_TYPE;

QtCreateMarrayType::QtCreateMarrayType(const std::string &typeName2, const std::string cellTypeName2, QtOperation *domainNode2, const std::vector<std::string> *axisNames2)
    : typeName(typeName2), typeAttributes(NULL), domainNode(domainNode2), axisNames(axisNames2)
{
    this->cellTypeName = TypeFactory::getInternalTypeFromSyntaxType(cellTypeName2);
}

QtCreateMarrayType::QtCreateMarrayType(const std::string &typeName2, QtNode::QtOperationList *typeAttributes2, QtOperation *domainNode2, const std::vector<std::string> *axisNames2)
    : typeName(typeName2), typeAttributes(typeAttributes2), domainNode(domainNode2), axisNames(axisNames2)
{
    this->cellTypeName = TypeFactory::ANONYMOUS_CELL_TYPE_PREFIX + this->typeName;
}

QtData *QtCreateMarrayType::evaluate()
{
    QtData *returnValue = NULL;
    // at this point we know that all values are valid (they are checked in checkType)


    if (this->typeAttributes != NULL)
    {
        QtCreateCellType createCellType(this->cellTypeName, this->typeAttributes);
        createCellType.evaluate();
    }

    const BaseType *catBaseType = TypeFactory::mapType(this->cellTypeName.c_str());

    QtMintervalData *domain = static_cast<QtMintervalData *>(this->domainNode->evaluate(NULL));

    const MDDType *mddType = new MDDDomainType(this->typeName.c_str(), catBaseType, domain->getMintervalData(), axisNames);
    delete domain;

    TypeFactory::addMDDType(mddType);
    delete mddType;
    return returnValue;
}


void QtCreateMarrayType::checkType()
{
    // Check if the name is longer than 200 characters
    if (typeName.length() >= MAX_MARRAY_TYPE_NAME_LENGTH)
    {
        LERROR << "The marray type name is longer than 200 characters.";
        parseInfo.setErrorNo(MARRAY_TYPE_NAME_LENGTH_EXCEEDED);
        throw parseInfo;
    }
    // Check if type exists and throw exception if it exists
    if (TypeFactory::mapMDDType(this->typeName.c_str()) != NULL)
    {
        parseInfo.setErrorNo(969);
        parseInfo.setToken(this->typeName.c_str());
        throw parseInfo;
    }

    if (this->typeAttributes == NULL)
    {
        // Check if the cell type exists and throw exception if not
        if (TypeFactory::mapType(this->cellTypeName.c_str()) == NULL)
        {
            parseInfo.setErrorNo(971);
            parseInfo.setToken(this->cellTypeName.c_str());
            throw parseInfo;
        }
    }
    else
    {
        QtCreateCellType createCellType(this->cellTypeName, this->typeAttributes);
        createCellType.checkType();
    }

    // Check if the dimensionality is < 1 or if the specified domain is valid
    if (this->domainNode == NULL)
    {
        parseInfo.setErrorNo(972);
        throw parseInfo;
    }
    else if (this->domainNode != NULL)
    {
        this->domainNode->checkType();
    }
}

void QtCreateMarrayType::printTree(int tab, std::ostream &s, __attribute__((unused)) QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCreateMarrayType Object" << std::endl;
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  CREATE TYPE " << typeName << " UNDER MARRAY { " << cellTypeName << " }, ";
    domainNode->printTree(tab + 1, s);
}

void QtCreateMarrayType::printAlgebraicExpression(std::ostream &s)
{
    s << "command <";
    s << "CREATE TYPE " << typeName << " UNDER MARRAY { " << cellTypeName << " }, ";
    domainNode->printAlgebraicExpression(s);
    s << ">";
}

QtNode::QtNodeType QtCreateMarrayType::getNodeType() const
{
    return nodeType;
}
