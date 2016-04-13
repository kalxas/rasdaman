#include "qtcreatecelltype.hh"
#include "qtcelltypeattributes.hh"
#include "catalogmgr/typefactory.hh"
#include "relcatalogif/alltypes.hh"

const QtNode::QtNodeType QtCreateCellType::nodeType = QtNode::QT_CREATE_CELL_TYPE;

QtCreateCellType::QtCreateCellType(const std::string &typeName2, QtNode::QtOperationList *typeAttributes2)
    :typeName(typeName2), typeAttributes(typeAttributes2)
{
}

QtData* QtCreateCellType::evaluate()
{
    QtData* returnValue = NULL;
    StructType* structType = new StructType(this->typeName.c_str(), this->typeAttributes->size());
    for (std::vector<QtOperation*>::iterator it = typeAttributes->begin(); it != typeAttributes->end(); ++it)
    {
        // Here we are sure that all attribute types are valid sice they were checked in checkType()
        QtCellTypeAttributes* typeAttribute = static_cast<QtCellTypeAttributes*>(*it);
        std::string attributeTypeType = TypeFactory::getInternalTypeFromSyntaxType(typeAttribute->getAttributeType());
        const BaseType* attributeType = TypeFactory::mapType(attributeTypeType.c_str());
        structType->addElement(typeAttribute->getAttributeName().c_str(), attributeType);
    }

    TypeFactory::addStructType(structType);
    return returnValue;
}

void QtCreateCellType::checkType()
{
    // Check if the type already exists and throw error if necessary
    if (TypeFactory::mapType(this->typeName.c_str()))
    {
        parseInfo.setErrorNo(969);
        parseInfo.setToken(this->typeName.c_str());
        throw parseInfo;
    }

    // Check if all the bands have a valid type
    for (std::vector<QtOperation*>::iterator it = typeAttributes->begin(); it != typeAttributes->end(); ++it)
    {
        QtCellTypeAttributes* typeAttribute = static_cast<QtCellTypeAttributes*>(*it);

        // map the attribute type to internal representation
        std::string attributeTypeType = TypeFactory::getInternalTypeFromSyntaxType(typeAttribute->getAttributeType());
        const BaseType* attributeType = TypeFactory::mapType(attributeTypeType.c_str());

        // if an attribute type is invalid, throw exception
        if (attributeType == NULL)
        {
            parseInfo.setErrorNo(970);
            parseInfo.setToken(attributeTypeType.c_str());
            throw parseInfo;
        }
    }

}

void QtCreateCellType::printTree(int tab, std::ostream &s, __attribute__ ((unused)) QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCreateCellType Object" << std::endl;
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  CREATE TYPE " << typeName << " UNDER STRUCT { ";
    bool isFirst = true;
    for (std::vector<QtOperation*>::iterator it = typeAttributes->begin(); it != typeAttributes->end(); ++it)
    {
        QtCellTypeAttributes* typeAttribute = static_cast<QtCellTypeAttributes*>(*it);
        std::string attributeTypeType = TypeFactory::getInternalTypeFromSyntaxType(typeAttribute->getAttributeType());
        std::string attributeName = typeAttribute->getAttributeName();

        if (!isFirst)
        {
            s << ", ";
        }
        s << attributeName << " " << attributeTypeType;
        isFirst = false;
    }
}


void QtCreateCellType::printAlgebraicExpression(std::ostream &s)
{
    s << "command <";
    s << "  CREATE TYPE " << typeName << " UNDER STRUCT { ";
    bool isFirst = true;
    for (std::vector<QtOperation*>::iterator it = typeAttributes->begin(); it != typeAttributes->end(); ++it)
    {
        QtCellTypeAttributes* typeAttribute = static_cast<QtCellTypeAttributes*>(*it);
        std::string attributeTypeType = TypeFactory::getInternalTypeFromSyntaxType(typeAttribute->getAttributeType());
        std::string attributeName = typeAttribute->getAttributeName();

        if (!isFirst)
        {
            s << ", ";
        }
        s << attributeName << " " << attributeTypeType;
        isFirst = false;
    }
    s << ">";
}

QtNode::QtNodeType QtCreateCellType::getNodeType() const
{
    return nodeType;
}
