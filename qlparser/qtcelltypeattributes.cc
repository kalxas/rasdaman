#include "qtcelltypeattributes.hh"

const QtNode::QtNodeType QtCellTypeAttributes::nodeType = QtNode::QT_CELL_TYPE_ATTRIBUTES;

QtCellTypeAttributes::QtCellTypeAttributes(const std::string &name, const std::string &type)
    :attributeName(name), attributeType(type)
{
}

QtData* QtCellTypeAttributes::evaluate()
{
    // this tree node is just a container for the attribute type and attribute name
    // thus there is no need for evaluate
    return NULL;
}

void QtCellTypeAttributes::checkType()
{
    // this tree node is just a container for the attribute type and attribute name
    // thus there is no need for typeCheck
}

void QtCellTypeAttributes::printTree(int tab, std::ostream &s, QtChildType mode)
{
    s << SPACE_STR(tab).c_str() << "QtCellTypeAttributes (" << attributeName << " "
      << attributeType << ")" << std::endl;
}

std::string QtCellTypeAttributes::getAttributeName()
{
    return this->attributeName;
}

std::string QtCellTypeAttributes::getAttributeType()
{
    return this->attributeType;
}

QtNode::QtNodeType QtCellTypeAttributes::getNodeType() const
{
    return nodeType;
}
