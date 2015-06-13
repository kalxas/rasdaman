#include "qtdroptype.hh"
#include "relcatalogif/alltypes.hh"
#include "catalogmgr/typefactory.hh"

const QtNode::QtNodeType QtDropType::nodeType = QtNode::QT_DROP_TYPE;

QtDropType::QtDropType(const std::string &typeName2)
    :typeName(typeName2)
{
}

QtData* QtDropType::evaluate()
{
    // here we are sure that the type exists in the database (checkType passed)
    switch (dropType)
    {
        case CELL_TYPE:
        {
            TypeFactory::deleteStructType(this->typeName.c_str());
            break;
        }
        case MDD_TYPE:
        {
            TypeFactory::deleteMDDType(this->typeName.c_str());
            break;
        }
        case SET_TYPE:
        {
            TypeFactory::deleteSetType(this->typeName.c_str());
            break;
        }
        default:
        {
            parseInfo.setErrorNo(968);
            parseInfo.setToken(this->typeName.c_str());
            throw parseInfo;
        }
    }
}

void QtDropType::checkType()
{
    // determine the type of the type
    // if no type exists then all if() will fail
    if (TypeFactory::mapType(this->typeName.c_str()) != NULL)
    {
        dropType = CELL_TYPE;
    }
    else if (TypeFactory::mapMDDType(this->typeName.c_str()) != NULL)
    {
        dropType = MDD_TYPE;
    }
    else if (TypeFactory::mapSetType(this->typeName.c_str()) != NULL)
    {
        dropType = SET_TYPE;
    }
    else
    {
        parseInfo.setErrorNo(968);
        parseInfo.setToken(this->typeName.c_str());
        throw parseInfo;
    }
}

void QtDropType::printTree(int tab, std::ostream &s, __attribute__ ((unused)) QtChildType mode)
{
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "QtDropType Object" << std::endl;
    s << SPACE_STR(static_cast<size_t>(tab)).c_str() << "  DROP TYPE " << typeName;
}


void QtDropType::printAlgebraicExpression(std::ostream &s)
{
    s << "command <";
    s << "DROP TYPE " << typeName;
    s << ">";
}

QtNode::QtNodeType QtDropType::getNodeType() const
{
    return nodeType;
}
