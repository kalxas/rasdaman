#include "qtcelltypeattributes.hh"

const QtNode::QtNodeType QtCellTypeAttributes::nodeType = QtNode::QT_CELL_TYPE_ATTRIBUTES;

QtCellTypeAttributes::QtCellTypeAttributes(const std::string &name, const std::string &type)
    :attributeName(name), attributeType(type)
{
}

void QtCellTypeAttributes::printTree(int tab, std::ostream &s, __attribute__ ((unused)) QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtCellTypeAttributes (" << attributeName << " "
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
