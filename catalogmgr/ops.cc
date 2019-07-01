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
static const char rcsid[] = "@(#)catalogif,ops.cc: $Header: /home/rasdev/CVS-repository/rasdaman/catalogmgr/ops.cc,v 1.67 2003/12/20 23:41:27 rasdev Exp $";

#include "config.h"
#include <limits.h>
#include <string.h> // memcpy()
#ifdef __APPLE__
#include <sys/malloc.h> // malloc()
#include <float.h>
#else
#include <malloc.h> // malloc()
#include <values.h>
#include <cmath>
#endif
#include "ops.hh"
#include "relcatalogif/alltypes.hh"
#include "reladminif/objectbroker.hh"
#include "typefactory.hh"
#include "raslib/point.hh"
#include <logging.hh>

//-----------------------------------------------
//  getUnaryOp
//-----------------------------------------------
UnaryOp *Ops::getUnaryOp(Ops::OpType op, const BaseType *resType, const BaseType *opType, size_t resOff, size_t opOff)
{
    const auto typeOp = opType->getType();
    const auto typeRes = resType->getType();
#ifndef NO_OPT_OPS
/////////////////////////////////////////////
    if (typeRes != STRUCT && typeRes == typeOp)
    {
        if (op == Ops::OP_IDENTITY)
        {
            switch (resType->getSize())
            {
            case 1:
                return new OpIDENTITYChar(resType, opType, resOff, opOff);
            case 2:
                return new OpIDENTITYShort(resType, opType, resOff, opOff);
            case 4:
                return new OpIDENTITYLong(resType, opType, resOff, opOff);
            default:
                break;
            }
            if (typeRes == COMPLEXTYPE1 || typeRes == COMPLEXTYPE2 || typeRes == CINT16 || typeRes == CINT32)
            {
                return new OpIDENTITYComplex(resType, opType, resOff, opOff);
            }
        }
        else if (op == Ops::OP_UPDATE)
        {
            switch (typeRes)
            {
            case OCTET:
                return new OpUpdateOctet(resType, opType, resOff, opOff);
            case BOOLTYPE:
            case CHAR:
                return new OpUpdateChar(resType, opType, resOff, opOff);
            case SHORT:
                return new OpUpdateShort(resType, opType, resOff, opOff);
            case USHORT:
                return new OpUpdateUShort(resType, opType, resOff, opOff);
            case LONG:
                return new OpUpdateLong(resType, opType, resOff, opOff);
            case ULONG:
                return new OpUpdateULong(resType, opType, resOff, opOff);
            case FLOAT:
                return new OpUpdateFloat(resType, opType, resOff, opOff);
            case DOUBLE:
                return new OpUpdateDouble(resType, opType, resOff, opOff);
            default:
                break;
            }
            if (typeRes == COMPLEXTYPE1 || typeRes == COMPLEXTYPE2 || typeRes == CINT16 || typeRes == CINT32)
            {
                return new OpUpdateComplex(resType, opType, resOff, opOff);
            }
        }
    }
/////////////////////////////////////////////
#endif

    // cast operations
    if (op > Ops::OP_CAST_BEGIN && op < Ops::OP_CAST_END)
    {
        if (typeOp <= NUMERICAL_TYPES_END)
        {
            return new OpCAST(resType, opType, resOff, opOff);
        }
        else if (typeOp == STRUCT && typeRes == STRUCT &&
                 (static_cast<StructType *>(const_cast<BaseType *>(resType)))->getNumElems() ==
                 (static_cast<StructType *>(const_cast<BaseType *>(opType)))->getNumElems())
        {
            return new OpUnaryStruct(resType, opType, op, resOff, opOff);
        }
        else
        {
            return 0;
        }
    }

    if (typeRes == BOOLTYPE && op == Ops::OP_IS_NULL)
    {
        if (typeOp >= ULONG && typeOp <= BOOLTYPE)
        {
            return new OpISNULLCULong(resType, opType, resOff, opOff);
        }
        else if (typeOp >= LONG && typeOp <= OCTET)
        {
            return new OpISNULLCLong(resType, opType, resOff, opOff);
        }
        else if (typeOp >= DOUBLE && typeOp <= FLOAT)
        {
            return new OpISNULLCDouble(resType, opType, resOff, opOff);
        }
        else
        {
            return 0;
        }
    }

    // all Char
    if (typeRes == BOOLTYPE && typeOp == BOOLTYPE && op == Ops::OP_NOT)
    {
        return new OpNOTBool(resType, opType, resOff, opOff);
    }

    // result and op are ULONG, USHORT or CHAR and
    if ((typeRes >= ULONG && typeRes <= BOOLTYPE) && (typeOp >= ULONG && typeOp <= OCTET))
    {
        switch (op)
        {
        case Ops::OP_NOT:
            return new OpNOTCULong(resType, opType, resOff, opOff);
        case Ops::OP_IDENTITY:
            return new OpIDENTITYCULong(resType, opType, resOff, opOff);
        case Ops::OP_UPDATE:
            return new OpUpdateCULong(resType, opType, resOff, opOff);
        default:
            return 0;
        }
    }
    // result and op are LONG, SHORT or OCTET
    if ((typeRes >= LONG && typeRes <= OCTET) && (typeOp >= ULONG && typeOp <= OCTET))
    {
        switch (op)
        {
        case Ops::OP_NOT:
            return new OpNOTCLong(resType, opType, resOff, opOff);
        case Ops::OP_IDENTITY:
            return new OpIDENTITYCLong(resType, opType, resOff, opOff);
        case Ops::OP_UPDATE:
            return new OpUpdateCLong(resType, opType, resOff, opOff);
	case Ops::OP_REALPARTINT:
            return new OpRealPartInt(resType, opType, resOff, opOff);
        case Ops::OP_IMAGINARPARTINT:
            return new OpImaginarPartInt(resType, opType, resOff, opOff);
        default:
            return 0;
        }
    }

    // result is FLOAT or DOUBLE and op is any type
    if (typeRes == FLOAT || (typeRes == DOUBLE && typeOp >= ULONG && typeOp <= FLOAT))
    {
        switch (op)
        {
        case Ops::OP_IDENTITY:
            return new OpIDENTITYCDouble(resType, opType, resOff, opOff);
        case Ops::OP_UPDATE:
            return new OpUpdateCDouble(resType, opType, resOff, opOff);
        case Ops::OP_SQRT:
            return new OpSQRTCDouble(resType, opType, resOff, opOff);
        case Ops::OP_POW:
            return new OpPOWCDouble(resType, opType, resOff, opOff);
        case Ops::OP_ABS:
            return new OpABSCDouble(resType, opType, resOff, opOff);
        case Ops::OP_EXP:
            return new OpEXPCDouble(resType, opType, resOff, opOff);
        case Ops::OP_LOG:
            return new OpLOGCDouble(resType, opType, resOff, opOff);
        case Ops::OP_LN:
            return new OpLNCDouble(resType, opType, resOff, opOff);
        case Ops::OP_SIN:
            return new OpSINCDouble(resType, opType, resOff, opOff);
        case Ops::OP_COS:
            return new OpCOSCDouble(resType, opType, resOff, opOff);
        case Ops::OP_TAN:
            return new OpTANCDouble(resType, opType, resOff, opOff);
        case Ops::OP_SINH:
            return new OpSINHCDouble(resType, opType, resOff, opOff);
        case Ops::OP_COSH:
            return new OpCOSHCDouble(resType, opType, resOff, opOff);
        case Ops::OP_TANH:
            return new OpTANHCDouble(resType, opType, resOff, opOff);
        case Ops::OP_ARCSIN:
            return new OpARCSINCDouble(resType, opType, resOff, opOff);
        case Ops::OP_ARCCOS:
            return new OpARCCOSCDouble(resType, opType, resOff, opOff);
        case Ops::OP_ARCTAN:
            return new OpARCTANCDouble(resType, opType, resOff, opOff);
        case Ops::OP_REALPART:
            return new OpRealPart(resType, opType, resOff, opOff);
        case Ops::OP_IMAGINARPART:
            return new OpImaginarPart(resType, opType, resOff, opOff);
        default:
            return 0;
        }
    }

    // retriving real or imaginar parts of a complex argument
    if (typeRes == DOUBLE && (typeOp == COMPLEXTYPE1 || typeOp == COMPLEXTYPE2))
    {
        switch (op)
        {
        case Ops::OP_REALPART:
            return new OpRealPart(resType, opType, resOff, opOff);
        case Ops::OP_IMAGINARPART:
            return new OpImaginarPart(resType, opType, resOff, opOff);
        default:
            return 0;
        }
    }

    if (typeRes == LONG && (typeOp == CINT16 || typeOp == CINT32))
    {
        switch (op)
        {
        case Ops::OP_REALPARTINT:
            return new OpRealPartInt(resType, opType, resOff, opOff);
        case Ops::OP_IMAGINARPARTINT:
            return new OpImaginarPartInt(resType, opType, resOff, opOff);
        default:
            return 0;
        }
    }

    if (typeRes == STRUCT)
    {
#ifndef NO_OPT_IDENTITY_STRUCT
        if (op == Ops::OP_IDENTITY)
        {
            if (resType->compatibleWith(opType))
            {
                return new OpIDENTITYStruct(resType, opType, resOff, opOff);
            }
            else
            {
                return 0;
            }
        }
        else if (op == Ops::OP_UPDATE)
        {
            if (resType->compatibleWith(opType))
            {
                return new OpUpdateStruct(resType, opType, resOff, opOff);
            }
            else
            {
                return 0;
            }
        }
#endif
        return new OpUnaryStruct(resType, opType, op, resOff, opOff);
    }
    return 0;
}

BinaryOp *
Ops::getBinaryOp(Ops::OpType op, const BaseType *resType, const BaseType *op1Type,
                 const BaseType *op2Type, size_t resOff,
                 size_t op1Off, size_t op2Off)
{
    const auto type1 = op1Type->getType();
    const auto type2 = op2Type->getType();
    const auto typeRes = resType->getType();
// if this flag is set, optimized operation execution for Char
// is turned off.
#ifndef NO_OPT_OPS
    // all Char: +, -, max, min, /, intdiv, mod, *
    if (typeRes == CHAR && type1 == CHAR && type2 == CHAR)
    {
        switch (op)
        {
        case Ops::OP_PLUS:
            return new OpPLUSChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MINUS:
            return new OpMINUSChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MAX_BINARY:
            return new OpMAX_BINARYChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MIN_BINARY:
            return new OpMIN_BINARYChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_DIV:
            return new OpDIVChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_INTDIV:
            return new OpDIVChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MOD:
            return new OpMODChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MULT:
            return new OpMULTChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        default:
            break;
        }
    }
#endif
    // OVERLAY
    if ((typeRes == type1) && (typeRes == type2) && (op == Ops::OP_OVERLAY))
    {
        return new OpOVERLAY(resType, op1Type, op2Type, resType->getSize(), OpOVERLAY::nullPattern, resOff, op1Off, op2Off);
    }

    // all Bool: and, or, xor
    if (typeRes == BOOLTYPE && type1 == BOOLTYPE && type2 == BOOLTYPE)
    {
        switch (op)
        {
        case Ops::OP_AND:
            return new OpANDBool(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_OR:
            return new OpORBool(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_XOR:
            return new OpXORBool(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        default:
            break;
        }
    }
    // result is unsigned integer
    // op1 and op2 are signed or unsigned integers
    // ops: +, -, max, min, /, intdiv, mod, *, and, or, xor
    if ((typeRes >= ULONG && typeRes <= BOOLTYPE) &&
            (type1 >= ULONG && type1 <= OCTET) && (type2 >= ULONG && type2 <= OCTET))
    {
        switch (op)
        {
        case Ops::OP_PLUS:
            return new OpPLUSCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MINUS:
            return new OpMINUSCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MAX_BINARY:
            return new OpMAX_BINARYCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MIN_BINARY:
            return new OpMIN_BINARYCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_DIV:
            return new OpDIVCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_INTDIV:
            return new OpDIVCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MOD:
            return new OpMODCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MULT:
            return new OpMULTCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_AND:
            return new OpANDCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_OR:
            return new OpORCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_XOR:
            return new OpXORCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        default:
            break;
        }
    }
    // result is signed integer
    // op1 and op2 are signed or unsigned integers
    // ops: +, -, max, min, /, intdiv, mod, *, and, or, xor
    if ((typeRes >= LONG && typeRes <= OCTET) &&
            (type1 >= ULONG && type1 <= OCTET) && (type2 >= ULONG && type2 <= OCTET))
    {
        switch (op)
        {
        case Ops::OP_PLUS:
            return new OpPLUSCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MINUS:
            return new OpMINUSCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MAX_BINARY:
            return new OpMAX_BINARYCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MIN_BINARY:
            return new OpMIN_BINARYCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_DIV:
            return new OpDIVCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_INTDIV:
            return new OpDIVCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MOD:
            return new OpMODCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MULT:
            return new OpMULTCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_AND:
            return new OpANDCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_OR:
            return new OpORCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_XOR:
            return new OpXORCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        default:
            break;
        }
    }
    // result is float or double
    // op1 and op2 are any primitive type
    // ops: +, -, max, min, /, *
    if ((typeRes == FLOAT || typeRes == DOUBLE) &&
            (type1 >= ULONG && type1 <= FLOAT) && (type2 >= ULONG && type2 <= FLOAT))
    {
        switch (op)
        {
        case Ops::OP_PLUS:
            return new OpPLUSCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MINUS:
            return new OpMINUSCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MAX_BINARY:
            return new OpMAX_BINARYCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MIN_BINARY:
            return new OpMIN_BINARYCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_DIV:
            return new OpDIVCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_MULT:
            return new OpMULTCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        default:
            break;
        }
    }
    // result is complex float or double
    // ops: +, -, max, min, /, *, construct complex
    if (typeRes == COMPLEXTYPE1 || typeRes == COMPLEXTYPE2)
    {
        BinaryOp::ScalarFlag scalarFlag = BinaryOp::NONE;
        if (type1 < COMPLEXTYPE1)
        {
            scalarFlag = BinaryOp::FIRST;
        }
        else if (type2 < COMPLEXTYPE1)
        {
            scalarFlag = BinaryOp::SECOND;
        }

        switch (op)
        {
        case Ops::OP_PLUS:
            return new OpPLUSComplex(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_MINUS:
            return new OpMINUSComplex(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_MAX_BINARY:
            return new OpMAX_BINARYComplex(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_MIN_BINARY:
            return new OpMIN_BINARYComplex(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_DIV:
            return new OpDIVComplex(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_MULT:
            return new OpMULTComplex(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_CONSTRUCT_COMPLEX:
            return new OpConstructComplex(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        default:
            break;
        }
    }

if (typeRes == CINT16 || typeRes == CINT32)
    {
        BinaryOp::ScalarFlag scalarFlag = BinaryOp::NONE;
        if (type1 < COMPLEXTYPE1)
        {
            scalarFlag = BinaryOp::FIRST;
        }
        else if (type2 < COMPLEXTYPE1)
        {
            scalarFlag = BinaryOp::SECOND;
        }

        switch (op)
        {
        case Ops::OP_PLUS:
            return new OpPLUSComplexInt(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_MINUS:
            return new OpMINUSComplexInt(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_MAX_BINARY:
            return new OpMAX_BINARYComplexInt(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_MIN_BINARY:
            return new OpMIN_BINARYComplexInt(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_DIV:
            return new OpDIVComplexInt(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_MULT:
            return new OpMULTComplexInt(resType, op1Type, op2Type, resOff, op1Off, op2Off, scalarFlag);
        case Ops::OP_CONSTRUCT_COMPLEX:
            return new OpConstructComplexInt(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        default:
            break;
        }
    }
#ifndef NO_OPT_OPS
    // result is bool, operands are Chars
    // ops: =, <, <=, !=, >, >=
    if ((typeRes == BOOLTYPE) &&
            (type1 == CHAR) && (type2 == CHAR))
    {
        switch (op)
        {
        case Ops::OP_EQUAL:
            return new OpEQUALChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_LESS:
            return new OpLESSChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_LESSEQUAL:
            return new OpLESSEQUALChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_NOTEQUAL:
            return new OpNOTEQUALChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_GREATER:
            return new OpGREATERChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_GREATEREQUAL:
            return new OpGREATEREQUALChar(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        default:
            break;
        }
    }
#endif
    // result is bool, operands are unsigned integer
    // ops: =, <, <=, !=, >, >=
    if ((typeRes == BOOLTYPE) &&
            (type1 >= ULONG && type1 <= BOOLTYPE) && (type2 >= ULONG && type2 <= BOOLTYPE))
    {
        switch (op)
        {
        case Ops::OP_EQUAL:
            return new OpEQUALCCharCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_LESS:
            return new OpLESSCCharCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_LESSEQUAL:
            return new OpLESSEQUALCCharCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_NOTEQUAL:
            return new OpNOTEQUALCCharCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_GREATER:
            return new OpGREATERCCharCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_GREATEREQUAL:
            return new OpGREATEREQUALCCharCULong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        default:
            break;
        }
    }
    // result is bool, operands are any integer
    // ops: =, <, <=, !=, >, >=
    if ((typeRes == BOOLTYPE) &&
            (type1 >= ULONG && type1 <= OCTET) && (type2 >= ULONG && type2 <= OCTET))
    {
        switch (op)
        {
        case Ops::OP_EQUAL:
            return new OpEQUALCCharCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_LESS:
            return new OpLESSCCharCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_LESSEQUAL:
            return new OpLESSEQUALCCharCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_NOTEQUAL:
            return new OpNOTEQUALCCharCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_GREATER:
            return new OpGREATERCCharCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_GREATEREQUAL:
            return new OpGREATEREQUALCCharCLong(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_BIT:
            return new OpBIT(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        default:
            break;
        }
    }
    // result is bool, operands are float
    // ops: =, <, <=, !=, >, >=
    if ((typeRes == BOOLTYPE) &&
            (type1 >= ULONG && type1 <= FLOAT) && (type2 >= ULONG && type2 <= FLOAT))
    {
        switch (op)
        {
        case Ops::OP_EQUAL:
            return new OpEQUALCCharCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_LESS:
            return new OpLESSCCharCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_LESSEQUAL:
            return new OpLESSEQUALCCharCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_NOTEQUAL:
            return new OpNOTEQUALCCharCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_GREATER:
            return new OpGREATERCCharCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        case Ops::OP_GREATEREQUAL:
            return new OpGREATEREQUALCCharCDouble(resType, op1Type, op2Type, resOff, op1Off, op2Off);
        default:
            break;
        }
    }
    // result is bool, operands are structs
    // ops: =, !=
    if (typeRes == BOOLTYPE)
    {
        if ((type1 == STRUCT && type2 == STRUCT) ||
                (type1 == STRUCT && type2 <= FLOAT) ||
                (type1 <= FLOAT && type2 == STRUCT))
        {
            if (op == Ops::OP_EQUAL)
            {
                return new OpEQUALStruct(resType, op1Type, op2Type, resOff, op1Off, op2Off);
            }
            else if (op == Ops::OP_NOTEQUAL)
            {
                return new OpNOTEQUALStruct(resType, op1Type, op2Type, resOff, op1Off, op2Off);
            }
            else if (op == Ops::OP_GREATER || op == Ops::OP_GREATEREQUAL ||
                     op == Ops::OP_LESS || op == Ops::OP_LESSEQUAL)
            {
                return new OpComparisonStruct(op, resType, op1Type, op2Type, resOff, op1Off, op2Off);
            }
        }
    }
    // result is Struct, two or one operands are structs
    // ops: -, +, max, min, /, *, intdiv, mod, is, and, or, overlay, bit, xor
    if (typeRes == STRUCT)
    {
        if (type1 == STRUCT && type2 == STRUCT)
        {
            if (op >= OP_MINUS && op <= OP_CONSTRUCT_COMPLEX)
            {
                return new OpBinaryStruct(resType, op, op1Type, op2Type, resOff, op1Off, op2Off);
            }
        }
        else if (type1 == STRUCT)
        {
            if (op >= OP_MINUS && op <= OP_CONSTRUCT_COMPLEX && isApplicableOnStructConst(op, op1Type, op2Type))
            {
                return new OpBinaryStructConst(resType, op1Type, op2Type, op, resOff, op1Off, op2Off);
            }
        }
        else if (type2 == STRUCT)
        {
            if (op >= OP_MINUS && op <= OP_CONSTRUCT_COMPLEX && isApplicableOnStructConst(op, op2Type, op1Type))
            {
                return new OpBinaryConstStruct(resType, op1Type, op2Type, op, resOff, op1Off, op2Off);
            }
        }
    }
    return 0;
}


//-----------------------------------------------------------------
//  getCondenseOp
//-----------------------------------------------------------------

CondenseOp *
Ops::getCondenseOp(Ops::OpType op, const BaseType *resType, const BaseType *opType, size_t resOff, size_t opOff)
{
    if (resType->getType() == BOOLTYPE)
    {
        switch (op)
        {
        case Ops::OP_SOME:
            return new OpSOMECChar(resType, opType, resOff, opOff);
        case Ops::OP_ALL:
            return new OpALLCChar(resType, opType, resOff, opOff);
        case Ops::OP_MAX:
            return new OpMAXCLong(resType, opType, resOff, opOff);
        case Ops::OP_MIN:
            return new OpMINCLong(resType, opType, resOff, opOff);
        default:
            break;
        }
    }

    else if (resType->getType() == ULONG && opType->getType() == BOOLTYPE)
    {
        switch (op)
        {
        case Ops::OP_COUNT:
            return new OpCOUNTCChar(resType, opType, resOff, opOff);
        case Ops::OP_SUM:
            return new OpSUMCULong(resType, opType, resOff, opOff);
        default:
            break;
        }
    }

    else if ((resType->getType() >= ULONG && resType->getType() <= BOOLTYPE) &&
             (opType->getType() >= ULONG && opType->getType() <= BOOLTYPE))
    {
        switch (op)
        {
        case Ops::OP_MAX:
            return new OpMAXCULong(resType, opType, resOff, opOff);
        case Ops::OP_MIN:
            return new OpMINCULong(resType, opType, resOff, opOff);
        case Ops::OP_SUM:
            return new OpSUMCULong(resType, opType, resOff, opOff);
        default:
            break;
        }
    }

    else if ((resType->getType() == LONG || resType->getType() == SHORT ||
              resType->getType() == OCTET) && (opType->getType() >= ULONG && opType->getType() <= OCTET))
    {
        switch (op)
        {
        case Ops::OP_MAX:
            return new OpMAXCLong(resType, opType, resOff, opOff);
        case Ops::OP_MIN:
            return new OpMINCLong(resType, opType, resOff, opOff);
        case Ops::OP_SUM:
            return new OpSUMCLong(resType, opType, resOff, opOff);
        default:
            break;
        }
    }
    else if ((resType->getType() == FLOAT || resType->getType() == DOUBLE) &&
             (opType->getType() >= ULONG && opType->getType() <= FLOAT))
    {
        switch (op)
        {
        case Ops::OP_MAX:
            return new OpMAXCDouble(resType, opType, resOff, opOff);
        case Ops::OP_MIN:
            return new OpMINCDouble(resType, opType, resOff, opOff);
        case Ops::OP_SUM:
            return new OpSUMCDouble(resType, opType, resOff, opOff);
        case Ops::OP_SQSUM:
            return new OpSQSUMCDouble(resType, opType, resOff, opOff);
        default:
            break;
        }
    }
    else if (resType->getType() == STRUCT)
    {
        // res and op are structs with same structure.
        return new OpCondenseStruct(resType, opType, op, resOff, opOff);
    }
    else if (resType->getType() == COMPLEXTYPE1 || resType->getType() == COMPLEXTYPE2)
    {
        switch (op)
        {
        case Ops::OP_MAX:
            return new OpMAXComplex(resType, opType, resOff, opOff);
        case Ops::OP_MIN:
            return new OpMINComplex(resType, opType, resOff, opOff);
        case Ops::OP_SUM:
            return new OpSUMComplex(resType, opType, resOff, opOff);
        default:
            break;
        }
    }
    else if (resType->getType() == CINT16 || resType->getType() == CINT32)
    {
        switch (op)
        {
        case Ops::OP_MAX:
            return new OpMAXComplexInt(resType, opType, resOff, opOff);
        case Ops::OP_MIN:
            return new OpMINComplexInt(resType, opType, resOff, opOff);
        case Ops::OP_SUM:
            return new OpSUMComplexInt(resType, opType, resOff, opOff);
        default:
            break;
        }
    }
    else if (resType->getType() == FLOAT || resType->getType() == DOUBLE)
    {
        switch (op)
        {
        case Ops::OP_SQSUM:
            return new OpSQSUMCDouble(resType, opType, resOff, opOff);
        default:
            break;
        }
    }

    return 0;
}


CondenseOp *
Ops::getCondenseOp(Ops::OpType op, const BaseType *resType, char *newAccu,
                   const BaseType *opType, size_t resOff, size_t opOff)
{
    if (resType->getType() == BOOLTYPE)
    {
        switch (op)
        {
        case Ops::OP_SOME:
            return new OpSOMECChar(resType, newAccu, opType, resOff, opOff);
        case Ops::OP_ALL:
            return new OpALLCChar(resType, newAccu, opType, resOff, opOff);
        default:
            break;
        }
    }
    else if (resType->getType() == ULONG && opType->getType() == BOOLTYPE)
    {
        switch (op)
        {
        case Ops::OP_COUNT:
            return new OpCOUNTCChar(resType, newAccu, opType, resOff, opOff);
        default:
            break;
        }
    }
    else if ((resType->getType() >= ULONG && resType->getType() <= BOOLTYPE) &&
             (opType->getType() >= ULONG && opType->getType() <= BOOLTYPE))
    {
        switch (op)
        {
        case Ops::OP_MAX:
            return new OpMAXCULong(resType, newAccu, opType, resOff, opOff);
        case Ops::OP_MIN:
            return new OpMINCULong(resType, newAccu, opType, resOff, opOff);
        case Ops::OP_SUM:
            return new OpSUMCULong(resType, newAccu, opType, resOff, opOff);
        default:
            break;
        }
    }
    else if ((resType->getType() == LONG || resType->getType() == SHORT ||
              resType->getType() == OCTET) &&
             (opType->getType() >= ULONG && opType->getType() <= OCTET))
    {
        switch (op)
        {
        case Ops::OP_MAX:
            return new OpMAXCLong(resType, newAccu, opType, resOff, opOff);
        case Ops::OP_MIN:
            return new OpMINCLong(resType, newAccu, opType, resOff, opOff);
        case Ops::OP_SUM:
            return new OpSUMCLong(resType, newAccu, opType, resOff, opOff);
        default:
            break;
        }
    }
    else if ((resType->getType() == FLOAT || resType->getType() == DOUBLE) &&
             (opType->getType() >= ULONG && opType->getType() <= FLOAT))
    {
        switch (op)
        {
        case Ops::OP_MAX:
            return new OpMAXCDouble(resType, newAccu, opType, resOff, opOff);
        case Ops::OP_MIN:
            return new OpMINCDouble(resType, newAccu, opType, resOff, opOff);
        case Ops::OP_SUM:
            return new OpSUMCDouble(resType, newAccu, opType, resOff, opOff);
        case Ops::OP_SQSUM:
            return new OpSQSUMCDouble(resType, newAccu, opType, resOff, opOff);
        default:
            break;
        }
    }
    else if (resType->getType() == STRUCT)
    {
        // res and op are structs with same structure.
        return new OpCondenseStruct(resType, newAccu, opType, op, resOff, opOff);
    }
    return 0;
}

//-----------------------------------------------
//  isApplicable
//-----------------------------------------------
// perhaps a better approach would be to check if the ops are null, after assigning them.
//                                                                                    -BB
int Ops::isApplicable(Ops::OpType op, const BaseType *op1Type, const BaseType *op2Type)
{
    UnaryOp *myUnaryOp;
    BinaryOp *myBinaryOp;
    CondenseOp *myCondenseOp;

    // currently utilizing getResultType( op, op1Type, op2Type )
    // this introduces circular dependency between the two functions. -BB
    const BaseType *resType = getResultType(op, op1Type, op2Type);
    // unary or condense operations
    if (op2Type == 0)
    {
        myUnaryOp = getUnaryOp(op, resType, const_cast<BaseType *>(op1Type));
        if (myUnaryOp != 0)
        {
            delete myUnaryOp;
            return 1; // found an unary op
        }
        myCondenseOp = getCondenseOp(op, const_cast<BaseType *>(resType), const_cast<BaseType *>(op1Type));
        if (myCondenseOp != 0)
        {
            delete myCondenseOp;
            return 1;   // found a condense op
        }
        else
        {
            return 0;    // found neither
        }
    }
    else
    {
        myBinaryOp = getBinaryOp(op, resType, const_cast<BaseType *>(op1Type), const_cast<BaseType *>(op2Type));
        if (myBinaryOp != 0)
        {
            delete myBinaryOp;
            return 1;   // found a binary op

        }
        else
        {
            return 0;
        }
    }
}

const BaseType *Ops::getStructResultType(Ops::OpType op, const BaseType *op1, const BaseType *op2)
{
    if (!op1)
    {
        return 0;
    }
    if (!op2)
    {
        if (op1->getType() == STRUCT)
        {
            StructType *op1StructType = dynamic_cast<StructType *>(const_cast<BaseType *>(op1));
            StructType *resStructType = new StructType("res_struct_type", op1StructType->getNumElems());
            TypeFactory::addTempType(resStructType);
            for (size_t i = 0; i < op1StructType->getNumElems(); ++i)
            {
                const BaseType *resType = getResultType(op, op1StructType->getElemType(i));
                if (!resType)
                {
                    return 0;
                }
                resStructType->addElement(op1StructType->getElemName(i), resType);
            }
            return (BaseType *) resStructType;
        }
        else
        {
            return 0;
        }
    }
    else if (op1->getType() == STRUCT && op2->getType() == STRUCT)
    {
        StructType *op1StructType = dynamic_cast<StructType *>(const_cast<BaseType *>(op1));
        StructType *op2StructType = dynamic_cast<StructType *>(const_cast<BaseType *>(op2));
        //ensure that both structs have the same number of elements before proceeding
        if (op1StructType->getNumElems() != op2StructType->getNumElems())
        {
            return 0;
        }
        StructType *resStructType = new StructType("res_struct_type", op1StructType->getNumElems());
        TypeFactory::addTempType(resStructType);
        for (size_t i = 0; i < op1StructType->getNumElems(); ++i)
        {
            const BaseType *resType = getResultType(op, op1StructType->getElemType(i), op2StructType->getElemType(i));
            if (!resType)
            {
                return 0;
            }
            resStructType->addElement(op1StructType->getElemName(i), resType);
        }
        return (BaseType *) resStructType;
    }
    else if (op1->getType() == STRUCT)
    {
        StructType *op1StructType = dynamic_cast<StructType *>(const_cast<BaseType *>(op1));
        StructType *resStructType = new StructType("res_struct_type", op1StructType->getNumElems());
        TypeFactory::addTempType(resStructType);
        for (size_t i = 0; i < op1StructType->getNumElems(); ++i)
        {
            const BaseType *resType = getResultType(op, op1StructType->getElemType(i), op2);
            if (!resType)
            {
                return 0;
            }
            resStructType->addElement(op1StructType->getElemName(i), resType);
        }
        return (BaseType *) resStructType;
    }
    else if (op2->getType() == STRUCT)
    {
        StructType *op2StructType = dynamic_cast<StructType *>(const_cast<BaseType *>(op2));
        StructType *resStructType = new StructType("res_struct_type", op2StructType->getNumElems());
        TypeFactory::addTempType(resStructType);
        for (size_t i = 0; i < op2StructType->getNumElems(); ++i)
        {
            const BaseType *resType = getResultType(op, op1, op2StructType->getElemType(i));
            if (!resType)
            {
                return 0;
            }
            resStructType->addElement(op2StructType->getElemName(i), resType);
        }
        return (BaseType *) resStructType;
    }
    else
    {
        return 0;
    }
}



//-----------------------------------------------
//  getResultType
//-----------------------------------------------
const BaseType *Ops::getResultType(Ops::OpType op, const BaseType *op1, const BaseType *op2)
{
    if (!op1)
    {
        return NULL;
    }
    auto type1 = op1->getType();
    auto type2 = op2 ? op2->getType() : INVALID_TYPE;
    // overlay between composite types defined only on identical types
    if (op == OP_OVERLAY)
    {
        if (op1->compatibleWith(op2))
        {
            return op1;
        }
        else
        {
            return getStructResultType(op, op1, op2);
        }
    }

    // operation BIT returns bool or struct {bool, ...}
    if (op == OP_BIT)
    {
        const auto *res = getStructResultType(op, op1, op2);
        return (!res && op1->getType() <= OCTET) ? TypeFactory::mapType("Bool") : res;
    }

    if (op == OP_IS_NULL)
    {
        return TypeFactory::mapType("Bool");
    }

    // the condense operation COUNT always returns an unsigned long
    if (op == Ops::OP_COUNT)
    {
        const auto *res = getStructResultType(op, op1, op2);
        return (res) ? res : TypeFactory::mapType("ULong");
    }

    // SQRT & similar ops return DOUBLE
    // for structs, each band will return double (if it is well-defined!)
    if (op > Ops::OP_UFUNC_BEGIN && op < Ops::OP_UFUNC_END)
    {
        const auto *res = getStructResultType(op, op1, op2);
        return (res) ? res : TypeFactory::mapType("Double");
    }

    if (op > Ops::OP_CAST_BEGIN && op < Ops::OP_CAST_END && op != OP_CAST_GENERAL)
    {
        const auto *res = getStructResultType(op, op1, op2);
        if (!res)
        {
            const char *typeName[] =
            {
                "Bool", "Char", "Octet", "Short", "UShort",
                "Long", "ULong", "Float", "Double"
            };
            return TypeFactory::mapType(typeName[op - OP_CAST_BEGIN - 1]);
        }
        else
        {
            return res;
        }

    }

    // if we have division of integers, the result should be a real -- DM
    if (op == OP_DIV)
    {
        // considering the ordering, we only care that there is a natural
        // inclusion map from the 2nd op's type to the complex numbers, and that
        // op1 is, at worst, also complex
        if (type2 <= CINT32 && type1 <= CINT32)
        {
            const auto *res = getStructResultType(op, op1, op2);
            if (!res)
            {
                int x = std::max(type2, DOUBLE);
                switch (x)
                {
                case DOUBLE:
                    return TypeFactory::mapType("Double");
                case FLOAT:
                    return TypeFactory::mapType("Float");
                case COMPLEXTYPE1:
                    return TypeFactory::mapType("Complex");
                case COMPLEXTYPE2:
                    return TypeFactory::mapType("Complexd");
		case CINT16:
                    return TypeFactory::mapType("CInt16");
		case CINT32:
                    return TypeFactory::mapType("CInt32");
                default:
                    return TypeFactory::mapType("Double");
                    break;
                }
            }
            else
            {
                return res;
            }
        }
    }

    // the condense operation ADD_CELLS returns maximal type
    // (i.e. long/ulong or double)
    if (!op2 && op == Ops::OP_SUM)
    {
        const auto *res = getStructResultType(op, op1, op2);
        if (!res)
        {
            if (type1 <= BOOLTYPE)
            {
                return TypeFactory::mapType("ULong");
            }
            if (type1 <= OCTET)
            {
                return TypeFactory::mapType("Long");
            }
            else if (type1 <= FLOAT)
            {
                return TypeFactory::mapType("Double");
            }
            else if (type1 == COMPLEXTYPE1)
            {
                return TypeFactory::mapType("Complex");
            }
            else if (type1 == COMPLEXTYPE2)
            {
                return TypeFactory::mapType("Complexd");
            }
	    else if (type1 == CINT16)
            {
                return TypeFactory::mapType("Cint16");
            }
	    else if (type1 == CINT32)
            {
                return TypeFactory::mapType("CInt32");
            }
            else
            {
                return 0;
            }
        }
        else
        {
            return res;
        }
    }

    if (!op2 && op == Ops::OP_SQSUM)
    {
        const auto *res = getStructResultType(op, op1, op2);
        return (res) ? res : TypeFactory::mapType("Double");
    }

    // some :-) unary and condense operations return the same type
    if (op == OP_REALPART || op == OP_IMAGINARPART)
    {
        if (type1 == COMPLEXTYPE1)
        {
            return TypeFactory::mapType("Float");
        }
        else if (type1 == COMPLEXTYPE2)
        {
            return TypeFactory::mapType("Double");
        }
	else if (type1 == CINT16)
        {
            return TypeFactory::mapType("Cint16");
        }
	else if (type1 == CINT32)
        {
            return TypeFactory::mapType("CInt32");
        }
    }
    if (op == OP_CONSTRUCT_COMPLEX && type1 != STRUCT && type2 != STRUCT)
    {
        if (type1 == DOUBLE || type2 == DOUBLE)
        {
            return TypeFactory::mapType("Complexd");
        }
        else if (type1 == FLOAT || type2 == FLOAT)
        {
            return TypeFactory::mapType("Complex");
        }
	else if (type1 == LONG || type2 == LONG)
        {
            return TypeFactory::mapType("CInt32");
        }
	else if (type1 == SHORT || type2 == SHORT)
        {
            return TypeFactory::mapType("Cint16");
        }
        else
        {
            return TypeFactory::mapType("Complexd");
        }
    }
    if (op2 == 0)
    {
        return const_cast<BaseType *>(op1);
    }
    // both struct
    if (type1 == STRUCT && type2 == STRUCT)
    {
        const auto *res = getStructResultType(op, op1, op2);
        if (res && op >= OP_EQUAL && op <= OP_GREATEREQUAL)
        {
            return TypeFactory::mapType("Bool");
        }
        return res;
    }
    // one struct, other primitive
    if ((type1 == STRUCT && type2 <= FLOAT) ||
            (type1 <= FLOAT && type2 == STRUCT))
    {
        if (op == OP_EQUAL || op == OP_NOTEQUAL)
        {
            return TypeFactory::mapType("Bool");
        }
        else if (op > OP_EQUAL && op <= OP_GREATEREQUAL && op != OP_NOTEQUAL)
        {
            return NULL;
        }
        else
        {
            return getStructResultType(op, op1, op2);
        }
    }
    // comparison operators always return bool
    if ((op >= OP_EQUAL && op <= OP_GREATEREQUAL))
    {
        return TypeFactory::mapType("Bool");
    }


    // all the other binary functions return "strongest" type
    // if only one of operand is signed, result also has to be signed.
    if (isSignedType(op1) && !isSignedType(op2))
    {
        // swap it, action is in next if clause
        std::swap(op1, op2);
        std::swap(type1, type2);
    }
    if (!isSignedType(op1) && isSignedType(op2))
    {
        // get the thing with the highest precision and make sure it is signed.
        if (type2 == COMPLEXTYPE1 || type2 == COMPLEXTYPE2 ||
                type2 == FLOAT || type2 == DOUBLE || type2 == LONG || type2 == CINT16 || type2 == CINT32)
        {
            return const_cast<BaseType *>(op2);
        }
        if (type1 == USHORT)
        {
            return TypeFactory::mapType("Short");
        }
        if (type2 == SHORT)
        {
            return const_cast<BaseType *>(op2);
        }
        return TypeFactory::mapType("Octet");
    }
    // return the stronger type
    if (type1 == COMPLEXTYPE2 || type2 == COMPLEXTYPE2)
    {
        return TypeFactory::mapType("Complexd");
    }
    if (type1 == COMPLEXTYPE1 || type2 == COMPLEXTYPE1)
    {
        return TypeFactory::mapType("Complex");
    }
    if (type1 == CINT32 || type2 == CINT32)
    {
        return TypeFactory::mapType("CInt32");
    }
    if (type1 == CINT16 || type2 == CINT16)
    {
        return TypeFactory::mapType("CInt16");
    }
    if (type1 == DOUBLE || type2 == DOUBLE)
    {
        return TypeFactory::mapType("Double");
    }
    if (type1 == FLOAT || type2 == FLOAT)
    {
        return TypeFactory::mapType("Float");
    }
    if (type1 <= type2)
    {
        return const_cast<BaseType *>(op1);
    }
    else
    {
        return const_cast<BaseType *>(op2);
    }
}

int
Ops::isApplicableOnStruct(Ops::OpType op, const BaseType *opType)
{
    size_t i = 0;
    StructType *myStructType = dynamic_cast<StructType *>(const_cast<BaseType *>(opType));
    size_t numElems = myStructType->getNumElems();

    for (i = 0; i < numElems; i++)
    {
        if (!isApplicable(op, myStructType->getElemType(i),
                          myStructType->getElemType(i)))
        {
            return 0;
        }
    }
    return 1;
}

int
Ops::isApplicableOnStructConst(Ops::OpType op, const BaseType *op1Type,
                               const BaseType *op2Type)
{
    size_t i = 0;
    StructType *myStructType = dynamic_cast<StructType *>(const_cast<BaseType *>(op1Type));
    size_t numElems = myStructType->getNumElems();

    for (i = 0; i < numElems; i++)
    {
        if (!isApplicable(op, myStructType->getElemType(i), op2Type))
        {
            return 0;
        }
    }
    return 1;
}

int
Ops::isSignedType(const BaseType *type)
{
    return (type->getType() >= LONG && type->getType() <= CINT32);
}

int
Ops::isCondenseOp(Ops::OpType op)
{
    return (op >= OP_SOME && op <= OP_ALL);
}

int
Ops::isUnaryOp(Ops::OpType op)
{
    return (op >= OP_NOT && op <= OP_UPDATE);
}

int
Ops::isBinaryOp(Ops::OpType op)
{
    return (op >= OP_MINUS && op <= OP_GREATEREQUAL);
}

void
Ops::execUnaryConstOp(Ops::OpType op, const BaseType *resType,
                      const BaseType *opType, char *res,
                      const char *op1, size_t resOff,
                      size_t opOff, double param)
{
    UnaryOp *myOp = Ops::getUnaryOp(op, resType, opType, resOff, opOff);

    if (!myOp)
    {
        LERROR << "Ops::execUnaryConstOp: no operation for result type "
               << resType->getName() << ", argument type "
               << opType->getName() << ", operation " << (int)op;
        throw r_Error(367);
    }

    // set exponent for pow operations
    if (op == Ops::OP_POW)
    {
        ((OpPOWCDouble *)myOp)->setExponent(param);
    }
    try
    {
        (*myOp)(res, op1);
    }
    catch (...)
    {
        delete myOp;  // cleanup
        throw;
    }

    delete myOp;
}

void
Ops::execBinaryConstOp(Ops::OpType op, const BaseType *resType,
                       const BaseType *op1Type,
                       const BaseType *op2Type, char *res,
                       const char *op1, const char *op2,
                       size_t resOff, size_t op1Off,
                       size_t op2Off)
{
    BinaryOp *myOp = Ops::getBinaryOp(op, resType, op1Type, op2Type,
                                      resOff, op1Off, op2Off);
    (*myOp)(res, op1, op2);
    delete myOp;
}

UnaryOp::UnaryOp(const BaseType *newResType, const BaseType *newOpType,
                 size_t newResOff, size_t newOpOff)
    : NullValuesHandler(), opType(newOpType), resType(newResType), resOff(newResOff), opOff(newOpOff)

{
}

OpIDENTITYStruct::OpIDENTITYStruct(const BaseType *newResType, const BaseType *newOpType,
                                   size_t newResOff,
                                   size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpIDENTITYStruct::operator()(char *res, const char *op)
{
    memcpy((void *)(res + resOff), (void *)(const_cast<char *>(op) + opOff), resType->getSize());
}

OpUpdateStruct::OpUpdateStruct(const BaseType *newResType, const BaseType *newOpType,
                               size_t newResOff,
                               size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
    StructType *resStructType = dynamic_cast<StructType *>(const_cast<BaseType *>(newResType));
    StructType *opStructType = dynamic_cast<StructType *>(const_cast<BaseType *>(newOpType));
    numElems = resStructType->getNumElems();
    assignmentOps = new UnaryOp*[numElems];
    for (size_t i = 0; i < numElems; i++)
    {
        assignmentOps[i] = Ops::getUnaryOp(Ops::OP_UPDATE, resStructType->getElemType(i),
                                           opStructType->getElemType(i),
                                           newResOff + resStructType->getOffset(i),
                                           newOpOff + opStructType->getOffset(i));
    }
}

OpUpdateStruct::~OpUpdateStruct()
{
    for (size_t i = 0; i < numElems; i++)
        if (assignmentOps[i])
        {
            delete assignmentOps[i];
        }
    delete[] assignmentOps;
}

void OpUpdateStruct::setNullValues(r_Nullvalues *newNullValues)
{
    nullValues = newNullValues;
    for (size_t i = 0; i < numElems; i++)
    {
        assignmentOps[i]->setNullValues(newNullValues);
    }
}

void
OpUpdateStruct::operator()(char *res, const char *op)
{
    // there are null values, they will be handled correctly for each band in this way
    for (size_t i = 0; i < numElems; i++)
    {
        (*assignmentOps[i])(res, op);
    }
}

OpNOTCULong::OpNOTCULong(const BaseType *newResType, const BaseType *newOpType,
                         size_t newResOff, size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpNOTCULong::operator()(char *res, const char *op)
{
    r_ULong longOp;
    r_ULong longRes;

    longOp = *(opType->convertToCULong(op + opOff, &longOp));

    if (isNull(longOp))
    {
        longRes = longOp;
    }
    else
    {
        longRes = longOp ^ std::numeric_limits<r_ULong>::max();
    }

    resType->makeFromCULong(res + resOff, &longRes);
}


OpIDENTITYCULong::OpIDENTITYCULong(const BaseType *newResType, const BaseType *newOpType,
                                   size_t newResOff,
                                   size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpIDENTITYCULong::operator()(char *res, const char *op)
{
    r_ULong longOp;

    // !!!! HP specific, assumes 4 Byte long and MSB..LSB
    // byte order
    resType->makeFromCULong(res + resOff,
                            opType->convertToCULong(op + opOff, &longOp));
}


OpUpdateCULong::OpUpdateCULong(const BaseType *newResType, const BaseType *newOpType,
                               size_t newResOff,
                               size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpUpdateCULong::operator()(char *res, const char *op)
{
    r_ULong longOp;
    r_ULong *longOpVal = opType->convertToCULong(op + opOff, &longOp);

    if (!isNull(longOp))
    {
        resType->makeFromCULong(res + resOff, longOpVal);
    }
}

OpNOTCLong::OpNOTCLong(const BaseType *newResType, const BaseType *newOpType,
                       size_t newResOff, size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpNOTCLong::operator()(char *res, const char *op)
{
    r_Long longOp;
    r_Long longRes;

    longOp = *(opType->convertToCLong(op + opOff, &longOp));

    if (isNull(longOp))
    {
        longRes = longOp;
    }
    else
    {
        longRes = longOp ^ std::numeric_limits<r_Long>::max();
    }

    resType->makeFromCLong(res + resOff, &longRes);
}

OpIDENTITYCLong::OpIDENTITYCLong(const BaseType *newResType, const BaseType *newOpType,
                                 size_t newResOff,
                                 size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpIDENTITYCLong::operator()(char *res, const char *op)
{
    r_Long longOp;

    resType->makeFromCLong(res + resOff,
                           opType->convertToCLong(op + opOff, &longOp));
}

OpIDENTITYCDouble::OpIDENTITYCDouble(const BaseType *newResType, const BaseType *newOpType,
                                     size_t newResOff,
                                     size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpIDENTITYCDouble::operator()(char *res, const char *op)
{
    double doubleOp;

    // !!!! HP specific, assumes 4 Byte double and MSB..LSB
    // byte order
    resType->makeFromCDouble(res + resOff,
                             opType->convertToCDouble(op + opOff, &doubleOp));
}


OpUpdateCLong::OpUpdateCLong(const BaseType *newResType, const BaseType *newOpType,
                             size_t newResOff,
                             size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpUpdateCLong::operator()(char *res, const char *op)
{
    r_Long longOp;
    r_Long *longOpVal = opType->convertToCLong(op + opOff, &longOp);

    if (!isNull(longOp))
    {
        resType->makeFromCLong(res + resOff, longOpVal);
    }
}

OpUpdateCDouble::OpUpdateCDouble(const BaseType *newResType, const BaseType *newOpType,
                                 size_t newResOff,
                                 size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpUpdateCDouble::operator()(char *res, const char *op)
{
    double doubleOp;
    r_Double *doubleOpVal = opType->convertToCDouble(op + opOff, &doubleOp);

    if (!isNull(doubleOp))
    {
        resType->makeFromCDouble(res + resOff, doubleOpVal);
    }
}


OpNOTBool::OpNOTBool(const BaseType *newResType, const BaseType *newOpType,
                     size_t newResOff, size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpNOTBool::operator()(char *res, const char *op)
{
    // special case for bools, because bitwise not is not
    // equivalent to logical not
    if (isNull(*(op + opOff)))
    {
        *(res + resOff) = *(op + opOff);
    }
    else
    {
        *(res + resOff) = !(*(op + opOff));
    }
}

BinaryOp::BinaryOp(const BaseType *newResType, const BaseType *newOp1Type,
                   const BaseType *newOp2Type, size_t newResOff,
                   size_t newOp1Off, size_t newOp2Off)
    :  NullValuesHandler(), op1Type(newOp1Type), op2Type(newOp2Type), resType(newResType),
       resOff(newResOff), op1Off(newOp1Off), op2Off(newOp2Off)
{
}

void
BinaryOp::getCondenseInit(__attribute__((unused)) char *init)
{
    //Since this is the default, is it necessary to instantiate the type here? keep in mind for later.
    init = 0;
    // perhaps should also raise exception as operation cannot be used
    // as condenser.
}

void
OpBinaryStruct::getCondenseInit(char *init)
{
    //takes the necessary initial value for each operation and adds it to the array of initial values.
    //each function has its own initial values (depending on the fxn), and the default is 0.
    for (size_t i = 0; i < numElems; i++)
    {
        //int x = myStructType->getOffset(i);
        elemOps[i]->getCondenseInit(init + myStructType->getOffset(i));
    }
}

OpPLUSCULong::OpPLUSCULong(const BaseType *newResType, const BaseType *newOp1Type,
                           const BaseType *newOp2Type, size_t newResOff,
                           size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpPLUSCULong::operator()(char *res, const char *op1, const char *op2)
{
    r_ULong longOp1 = 0;
    r_ULong longOp2 = 0;
    r_ULong longRes = 0;

    longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 + longOp2;
    }

    resType->makeFromCULong(res + resOff, &longRes);
}

void
OpPLUSCULong::getCondenseInit(char *init)
{
    r_ULong dummy = 0;

    resType->makeFromCULong(init, &dummy);
}

OpPLUSULong::OpPLUSULong(const BaseType *newResType, const BaseType *newOp1Type,
                         const BaseType *newOp2Type, size_t newResOff,
                         size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpPLUSULong::operator()(char *res, const char *op1, const char *op2)
{
    if (isNull(*(r_ULong *)(const_cast<char *>(op1) + op1Off)))
    {
        *(r_ULong *)(res + resOff) = *(r_ULong *)(const_cast<char *>(op1) + op1Off);
    }
    else if (isNull(*(r_ULong *)(const_cast<char *>(op2) + op2Off)))
    {
        *(r_ULong *)(res + resOff) = *(r_ULong *)(const_cast<char *>(op2) + op2Off);
    }
    else
    {
        *(r_ULong *)(res + resOff) = *(r_ULong *)(const_cast<char *>(op1) + op1Off)
                                     + *(r_ULong *)(const_cast<char *>(op2) + op2Off);
    }
}

void
OpPLUSULong::getCondenseInit(char *init)
{
    r_ULong dummy = 0;

    resType->makeFromCULong(init, &dummy);
}



OpMAX_BINARYCULong::OpMAX_BINARYCULong(const BaseType *newResType, const BaseType *newOp1Type,
                                       const BaseType *newOp2Type, size_t newResOff,
                                       size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMAX_BINARYCULong::operator()(char *res, const char *op1, const char *op2)
{
    r_ULong longOp1 = 0;
    r_ULong longOp2 = 0;
    r_ULong longRes = 0;

    if (*(op1Type->convertToCULong(op1 + op1Off, &longOp1)) > *(op2Type->convertToCULong(op2 + op2Off, &longOp2)))
    {
        longRes = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    }
    else
    {
        longRes = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));
    }
    resType->makeFromCULong(res + resOff, &longRes);
}

void
OpMAX_BINARYCULong::getCondenseInit(char *init)
{
    r_ULong dummy = 0;

    resType->makeFromCULong(init, &dummy);
}

OpMAX_BINARYULong::OpMAX_BINARYULong(const BaseType *newResType, const BaseType *newOp1Type,
                                     const BaseType *newOp2Type, size_t newResOff,
                                     size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMAX_BINARYULong::operator()(char *res, const char *op1, const char *op2)
{
    if (*(r_ULong *)(const_cast<char *>(op1) + op1Off) > *(r_ULong *)(const_cast<char *>(op2) + op2Off))
    {
        *(r_ULong *)(res + resOff) = *(r_ULong *)(const_cast<char *>(op1) + op1Off);
    }
    else
    {
        *(r_ULong *)(res + resOff) = *(r_ULong *)(const_cast<char *>(op2) + op2Off);
    }
}

void
OpMAX_BINARYULong::getCondenseInit(char *init)
{
    r_ULong dummy = 0;

    resType->makeFromCULong(init, &dummy);
}




OpMIN_BINARYCULong::OpMIN_BINARYCULong(const BaseType *newResType, const BaseType *newOp1Type,
                                       const BaseType *newOp2Type, size_t newResOff,
                                       size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMIN_BINARYCULong::operator()(char *res, const char *op1, const char *op2)
{
    r_ULong longOp1 = 0;
    r_ULong longOp2 = 0;
    r_ULong longRes = 0;

    if (*(op1Type->convertToCULong(op1 + op1Off, &longOp1)) < * (op2Type->convertToCULong(op2 + op2Off, &longOp2)))
    {
        longRes = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    }
    else
    {
        longRes = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));
    }
    resType->makeFromCULong(res + resOff, &longRes);
}

void
OpMIN_BINARYCULong::getCondenseInit(char *init)
{
    r_ULong dummy = std::numeric_limits<r_ULong>::max();

    resType->makeFromCULong(init, &dummy);
}

OpMIN_BINARYULong::OpMIN_BINARYULong(const BaseType *newResType, const BaseType *newOp1Type,
                                     const BaseType *newOp2Type, size_t newResOff,
                                     size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMIN_BINARYULong::operator()(char *res, const char *op1, const char *op2)
{
    std::cout << "Hier krachts?" << endl;
    if (*(r_ULong *)(const_cast<char *>(op1) + op1Off) < * (r_ULong *)(const_cast<char *>(op2) + op2Off))
    {
        *(r_ULong *)(res + resOff) = *(r_ULong *)(const_cast<char *>(op1) + op1Off);
    }
    else
    {
        *(r_ULong *)(res + resOff) = *(r_ULong *)(const_cast<char *>(op2) + op2Off);
    }
}

void
OpMIN_BINARYULong::getCondenseInit(char *init)
{
    r_ULong dummy = std::numeric_limits<r_ULong>::max();

    resType->makeFromCULong(init, &dummy);
}


OpMINUSCULong::OpMINUSCULong(const BaseType *newResType, const BaseType *newOp1Type,
                             const BaseType *newOp2Type, size_t newResOff,
                             size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMINUSCULong::operator()(char *res, const char *op1, const char *op2)
{
    r_ULong longOp1 = 0;
    r_ULong longOp2 = 0;
    r_ULong longRes = 0;

    longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 - longOp2;
    }

    resType->makeFromCULong(res + resOff, &longRes);
}

OpDIVCULong::OpDIVCULong(const BaseType *newResType, const BaseType *newOp1Type,
                         const BaseType *newOp2Type, size_t newResOff,
                         size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpDIVCULong::operator()(char *res, const char *op1, const char *op2)
{
    r_ULong longOp1 = 0;
    r_ULong longOp2 = 0;
    r_ULong longRes = 0;

    longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        if (longOp2 == 0)
        {
            throw DIVISION_BY_ZERO;
        }
        else
        {
            longRes = longOp1 / longOp2;
        }
    }

    resType->makeFromCULong(res + resOff, &longRes);
}

OpMODCULong::OpMODCULong(const BaseType *newResType, const BaseType *newOp1Type,
                         const BaseType *newOp2Type, size_t newResOff,
                         size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMODCULong::operator()(char *res, const char *op1, const char *op2)
{
    r_ULong longOp1 = 0;
    r_ULong longOp2 = 0;
    r_ULong longRes = 0;

    op1Type->convertToCULong(op1 + op1Off, &longOp1);
    op2Type->convertToCULong(op2 + op2Off, &longOp2);

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        if (longOp2 == 0)
        {
            throw DIVISION_BY_ZERO;
        }
        else
        {
            longRes = longOp1 % longOp2;
        }
    }

    resType->makeFromCULong(res + resOff, &longRes);
}

OpMULTCULong::OpMULTCULong(const BaseType *newResType, const BaseType *newOp1Type,
                           const BaseType *newOp2Type, size_t newResOff,
                           size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMULTCULong::operator()(char *res, const char *op1, const char *op2)
{
    r_ULong longOp1 = 0;
    r_ULong longOp2 = 0;
    r_ULong longRes = 0;

    longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 * longOp2;
    }

    resType->makeFromCULong(res + resOff, &longRes);
}

void
OpMULTCULong::getCondenseInit(char *init)
{
    r_ULong dummy = 1;

    resType->makeFromCULong(init, &dummy);
}

OpANDCULong::OpANDCULong(const BaseType *newResType, const BaseType *newOp1Type,
                         const BaseType *newOp2Type, size_t newResOff,
                         size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpANDCULong::operator()(char *res, const char *op1, const char *op2)
{
    r_ULong longOp1 = 0;
    r_ULong longOp2 = 0;
    r_ULong longRes = 0;

    longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 & longOp2;
    }

    resType->makeFromCULong(res + resOff, &longRes);
}

void
OpANDCULong::getCondenseInit(char *init)
{
    unsigned char dummy[8] = { 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF };

    memcpy(init, dummy, resType->getSize());
}

OpANDBool::OpANDBool(const BaseType *newResType, const BaseType *newOp1Type,
                     const BaseType *newOp2Type, size_t newResOff,
                     size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpANDBool::operator()(char *res, const char *op1, const char *op2)
{

    if (isNull(*(op1 + op1Off)))
    {
        *(res + resOff) = *(op1 + op1Off);
    }
    else if (isNull(*(op2 + op2Off)))
    {
        *(res + resOff) = *(op2 + op2Off);
    }
    else
    {
        *(res + resOff) = *(op1 + op1Off) && *(op2 + op2Off);
    }

}

void
OpANDBool::getCondenseInit(char *init)
{
    *init = 1;
}

OpORCULong::OpORCULong(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff,
                       size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpORCULong::operator()(char *res, const char *op1, const char *op2)
{
    r_ULong longOp1 = 0;
    r_ULong longOp2 = 0;
    r_ULong longRes = 0;

    longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 | longOp2;
    }

    resType->makeFromCULong(res + resOff, &longRes);
}

void
OpORCULong::getCondenseInit(char *init)
{
    char dummy[8] = { 0, 0, 0, 0, 0, 0, 0, 0 };

    memcpy(init, dummy, resType->getSize());
}

OpORBool::OpORBool(const BaseType *newResType, const BaseType *newOp1Type,
                   const BaseType *newOp2Type, size_t newResOff,
                   size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpORBool::operator()(char *res, const char *op1, const char *op2)
{

    if (isNull(*(op1 + op1Off)))
    {
        *(res + resOff) = *(op1 + op1Off);
    }
    else if (isNull(*(op2 + op2Off)))
    {
        *(res + resOff) = *(op2 + op2Off);
    }
    else
    {
        *(res + resOff) = *(op1 + op1Off) || *(op2 + op2Off);
    }

}

void
OpORBool::getCondenseInit(char *init)
{
    *init = 0;
}

OpXORCULong::OpXORCULong(const BaseType *newResType, const BaseType *newOp1Type,
                         const BaseType *newOp2Type, size_t newResOff,
                         size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpXORCULong::operator()(char *res, const char *op1, const char *op2)
{
    r_ULong longOp1 = 0;
    r_ULong longOp2 = 0;
    r_ULong longRes = 0;

    longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 ^ longOp2;
    }

    resType->makeFromCULong(res + resOff, &longRes);
}

OpXORBool::OpXORBool(const BaseType *newResType, const BaseType *newOp1Type,
                     const BaseType *newOp2Type, size_t newResOff,
                     size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpXORBool::operator()(char *res, const char *op1, const char *op2)
{

    if (isNull(*(op1 + op1Off)))
    {
        *(res + resOff) = *(op1 + op1Off);
    }
    else if (isNull(*(op2 + op2Off)))
    {
        *(res + resOff) = *(op2 + op2Off);
    }
    else
    {
        *(res + resOff) = !(*(op1 + op1Off) == *(op2 + op2Off));
    }

}

OpPLUSCLong::OpPLUSCLong(const BaseType *newResType, const BaseType *newOp1Type,
                         const BaseType *newOp2Type, size_t newResOff,
                         size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpPLUSCLong::operator()(char *res, const char *op1, const char *op2)
{
    r_Long longOp1 = 0;
    r_Long longOp2 = 0;
    r_Long longRes = 0;

    longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 + longOp2;
    }

    resType->makeFromCLong(res + resOff, &longRes);
}

void
OpPLUSCLong::getCondenseInit(char *init)
{
    r_Long dummy = 0;

    resType->makeFromCLong(init, &dummy);
}

OpMAX_BINARYCLong::OpMAX_BINARYCLong(const BaseType *newResType, const BaseType *newOp1Type,
                                     const BaseType *newOp2Type, size_t newResOff,
                                     size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMAX_BINARYCLong::operator()(char *res, const char *op1, const char *op2)
{
    r_Long longOp1 = 0;
    r_Long longOp2 = 0;
    r_Long longRes = 0;

    if (*(op1Type->convertToCLong(op1 + op1Off, &longOp1)) > *(op2Type->convertToCLong(op2 + op2Off, &longOp2)))
    {
        longRes = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    }
    else
    {
        longRes = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));
    }
    resType->makeFromCLong(res + resOff, &longRes);
}

void
OpMAX_BINARYCLong::getCondenseInit(char *init)
{
    r_Long dummy = std::numeric_limits<r_Long>::lowest();

    resType->makeFromCLong(init, &dummy);
}




OpMIN_BINARYCLong::OpMIN_BINARYCLong(const BaseType *newResType, const BaseType *newOp1Type,
                                     const BaseType *newOp2Type, size_t newResOff,
                                     size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMIN_BINARYCLong::operator()(char *res, const char *op1, const char *op2)
{
    r_Long longOp1 = 0;
    r_Long longOp2 = 0;
    r_Long longRes = 0;

    if (*(op1Type->convertToCLong(op1 + op1Off, &longOp1)) < * (op2Type->convertToCLong(op2 + op2Off, &longOp2)))
    {
        longRes = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    }
    else
    {
        longRes = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));
    }
    resType->makeFromCLong(res + resOff, &longRes);
}

void
OpMIN_BINARYCLong::getCondenseInit(char *init)
{
    r_Long dummy = std::numeric_limits<r_Long>::max();

    resType->makeFromCLong(init, &dummy);
}

OpMINUSCLong::OpMINUSCLong(const BaseType *newResType, const BaseType *newOp1Type,
                           const BaseType *newOp2Type, size_t newResOff,
                           size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMINUSCLong::operator()(char *res, const char *op1, const char *op2)
{
    r_Long longOp1 = 0;
    r_Long longOp2 = 0;
    r_Long longRes = 0;

    longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 - longOp2;
    }

    resType->makeFromCLong(res + resOff, &longRes);
}

OpDIVCLong::OpDIVCLong(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff,
                       size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpDIVCLong::operator()(char *res, const char *op1, const char *op2)
{
    r_Long longOp1 = 0;
    r_Long longOp2 = 0;
    r_Long longRes = 0;

    longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        if (longOp2 == 0)
        {
            throw DIVISION_BY_ZERO;
        }
        else
        {
            longRes = longOp1 / longOp2;
        }
    }

    resType->makeFromCLong(res + resOff, &longRes);
}

OpMODCLong::OpMODCLong(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff,
                       size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMODCLong::operator()(char *res, const char *op1, const char *op2)
{
    r_Long longOp1 = 0;
    r_Long longOp2 = 0;
    r_Long longRes = 0;

    op1Type->convertToCLong(op1 + op1Off, &longOp1);
    op2Type->convertToCLong(op2 + op2Off, &longOp2);

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        if (longOp2 == 0)
        {
            throw DIVISION_BY_ZERO;
        }
        else
        {
            longRes = longOp1 % longOp2;
        }
    }

    resType->makeFromCLong(res + resOff, &longRes);
}

OpMULTCLong::OpMULTCLong(const BaseType *newResType, const BaseType *newOp1Type,
                         const BaseType *newOp2Type, size_t newResOff,
                         size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMULTCLong::operator()(char *res, const char *op1, const char *op2)
{
    r_Long longOp1 = 0;
    r_Long longOp2 = 0;
    r_Long longRes = 0;

    longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 * longOp2;
    }

    resType->makeFromCLong(res + resOff, &longRes);
}

void
OpMULTCLong::getCondenseInit(char *init)
{
    r_Long dummy = 1;

    resType->makeFromCLong(init, &dummy);
}

OpANDCLong::OpANDCLong(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff,
                       size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpANDCLong::operator()(char *res, const char *op1, const char *op2)
{
    r_Long longOp1 = 0;
    r_Long longOp2 = 0;
    r_Long longRes = 0;

    longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 & longOp2;
    }

    resType->makeFromCLong(res + resOff, &longRes);
}

void
OpANDCLong::getCondenseInit(char *init)
{
    unsigned char dummy[8] = { 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF };

    memcpy(init, dummy, resType->getSize());
}

OpORCLong::OpORCLong(const BaseType *newResType, const BaseType *newOp1Type,
                     const BaseType *newOp2Type, size_t newResOff,
                     size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpORCLong::operator()(char *res, const char *op1, const char *op2)
{
    r_Long longOp1 = 0;
    r_Long longOp2 = 0;
    r_Long longRes = 0;

    longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 | longOp2;
    }

    resType->makeFromCLong(res + resOff, &longRes);
}

void
OpORCLong::getCondenseInit(char *init)
{
    char dummy[8] = { 0, 0, 0, 0, 0, 0, 0, 0 };

    memcpy(init, dummy, resType->getSize());
}

OpXORCLong::OpXORCLong(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff,
                       size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpXORCLong::operator()(char *res, const char *op1, const char *op2)
{
    r_Long longOp1 = 0;
    r_Long longOp2 = 0;
    r_Long longRes = 0;

    longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1))
    {
        longRes = longOp1;
    }
    else if (isNull(longOp2))
    {
        longRes = longOp2;
    }
    else
    {
        longRes = longOp1 ^ longOp2;
    }

    resType->makeFromCLong(res + resOff, &longRes);
}

OpPLUSCDouble::OpPLUSCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                             const BaseType *newOp2Type, size_t newResOff,
                             size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpPLUSCDouble::operator()(char *res, const char *op1, const char *op2)
{
    double doubleOp1 = 0;
    double doubleOp2 = 0;
    double doubleRes = 0;

    doubleOp1 = *(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1));
    doubleOp2 = *(op2Type->convertToCDouble(op2 + op2Off, &doubleOp2));

    if (isNull(doubleOp1))
    {
        doubleRes = doubleOp1;
    }
    else if (isNull(doubleOp2))
    {
        doubleRes = doubleOp2;
    }
    else
    {
        doubleRes = doubleOp1 + doubleOp2;
    }

    resType->makeFromCDouble(res + resOff, &doubleRes);
}

void
OpPLUSCDouble::getCondenseInit(char *init)
{
    double dummy = 0.0;

    resType->makeFromCDouble(init, &dummy);
}

OpMAX_BINARYCDouble::OpMAX_BINARYCDouble(const BaseType *newResType, const BaseType *newOp1Type,
        const BaseType *newOp2Type, size_t newResOff,
        size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMAX_BINARYCDouble::operator()(char *res, const char *op1, const char *op2)
{
    double doubleOp1 = 0;
    double doubleOp2 = 0;
    double doubleRes = 0;

    if (*(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1)) > *(op2Type->convertToCDouble(op2 + op2Off, &doubleOp2)))
    {
        doubleRes = *(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1));
    }
    else
    {
        doubleRes = *(op2Type->convertToCDouble(op2 + op2Off, &doubleOp2));
    }
    resType->makeFromCDouble(res + resOff, &doubleRes);
}

void
OpMAX_BINARYCDouble::getCondenseInit(char *init)
{
    double dummy = std::numeric_limits<double>::lowest();

    resType->makeFromCDouble(init, &dummy);
}




OpMIN_BINARYCDouble::OpMIN_BINARYCDouble(const BaseType *newResType, const BaseType *newOp1Type,
        const BaseType *newOp2Type, size_t newResOff,
        size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMIN_BINARYCDouble::operator()(char *res, const char *op1, const char *op2)
{
    double doubleOp1 = 0;
    double doubleOp2 = 0;
    double doubleRes = 0;

    if (*(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1)) < * (op2Type->convertToCDouble(op2 + op2Off, &doubleOp2)))
    {
        doubleRes = *(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1));
    }
    else
    {
        doubleRes = *(op2Type->convertToCDouble(op2 + op2Off, &doubleOp2));
    }
    resType->makeFromCDouble(res + resOff, &doubleRes);
}

void
OpMIN_BINARYCDouble::getCondenseInit(char *init)
{
    double dummy = std::numeric_limits<double>::max();

    resType->makeFromCDouble(init, &dummy);
}

OpMINUSCDouble::OpMINUSCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                               const BaseType *newOp2Type, size_t newResOff,
                               size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMINUSCDouble::operator()(char *res, const char *op1, const char *op2)
{
    double doubleOp1 = 0;
    double doubleOp2 = 0;
    double doubleRes = 0;

    doubleOp1 = *(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1));
    doubleOp2 = *(op2Type->convertToCDouble(op2 + op2Off, &doubleOp2));

    if (isNull(doubleOp1))
    {
        doubleRes = doubleOp1;
    }
    else if (isNull(doubleOp2))
    {
        doubleRes = doubleOp2;
    }
    else
    {
        doubleRes = doubleOp1 - doubleOp2;
    }

    resType->makeFromCDouble(res + resOff, &doubleRes);
}

OpDIVCDouble::OpDIVCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                           const BaseType *newOp2Type, size_t newResOff,
                           size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpDIVCDouble::operator()(char *res, const char *op1, const char *op2)
{
    double doubleOp1 = 0;
    double doubleOp2 = 0;
    double doubleRes = 0;

    doubleOp1 = *(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1));
    doubleOp2 = *(op2Type->convertToCDouble(op2 + op2Off, &doubleOp2));

    if (isNull(doubleOp1))
    {
        doubleRes = doubleOp1;
    }
    else if (isNull(doubleOp2))
    {
        doubleRes = doubleOp2;
    }
    else
    {
        // Do not handle division by zero, return +-inf or nan as specified in IEEE 754
        doubleRes = doubleOp1 / doubleOp2;
    }

    resType->makeFromCDouble(res + resOff, &doubleRes);
}

OpMULTCDouble::OpMULTCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                             const BaseType *newOp2Type, size_t newResOff,
                             size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMULTCDouble::operator()(char *res, const char *op1, const char *op2)
{
    double doubleOp1 = 0;
    double doubleOp2 = 0;
    double doubleRes = 0;

    doubleOp1 = *(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1));
    doubleOp2 = *(op2Type->convertToCDouble(op2 + op2Off, &doubleOp2));

    if (isNull(doubleOp1))
    {
        doubleRes = doubleOp1;
    }
    else if (isNull(doubleOp2))
    {
        doubleRes = doubleOp2;
    }
    else
    {
        doubleRes = doubleOp1 * doubleOp2;
    }

    resType->makeFromCDouble(res + resOff, &doubleRes);
}

void
OpMULTCDouble::getCondenseInit(char *init)
{
    double dummy = 1.0;

    resType->makeFromCDouble(init, &dummy);
}

OpEQUALCCharCULong::OpEQUALCCharCULong(const BaseType *newResType,
                                       const BaseType *newOp1Type,
                                       const BaseType *newOp2Type,
                                       size_t newResOff,
                                       size_t newOp1Off,
                                       size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpEQUALCCharCULong::operator()(char *res, const char *op1,
                               const char *op2)
{
    r_ULong longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    r_ULong longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1) || isNull(longOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = longOp1 == longOp2;
    }
}

OpLESSCCharCULong::OpLESSCCharCULong(const BaseType *newResType,
                                     const BaseType *newOp1Type,
                                     const BaseType *newOp2Type,
                                     size_t newResOff,
                                     size_t newOp1Off,
                                     size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpLESSCCharCULong::operator()(char *res, const char *op1,
                              const char *op2)
{
    r_ULong longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    r_ULong longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1) || isNull(longOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = longOp1 < longOp2;
    }
}

OpLESSEQUALCCharCULong::OpLESSEQUALCCharCULong(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpLESSEQUALCCharCULong::operator()(char *res, const char *op1,
                                   const char *op2)
{
    r_ULong longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    r_ULong longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1) || isNull(longOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = longOp1 <= longOp2;
    }
}

OpNOTEQUALCCharCULong::OpNOTEQUALCCharCULong(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpNOTEQUALCCharCULong::operator()(char *res, const char *op1,
                                  const char *op2)
{
    r_ULong longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    r_ULong longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1) || isNull(longOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = longOp1 != longOp2;
    }
}

OpGREATERCCharCULong::OpGREATERCCharCULong(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpGREATERCCharCULong::operator()(char *res, const char *op1,
                                 const char *op2)
{
    r_ULong longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    r_ULong longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1) || isNull(longOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = longOp1 > longOp2;
    }
}

OpGREATEREQUALCCharCULong::OpGREATEREQUALCCharCULong(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpGREATEREQUALCCharCULong::operator()(char *res, const char *op1,
                                      const char *op2)
{
    r_ULong longOp1 = *(op1Type->convertToCULong(op1 + op1Off, &longOp1));
    r_ULong longOp2 = *(op2Type->convertToCULong(op2 + op2Off, &longOp2));

    if (isNull(longOp1) || isNull(longOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = longOp1 >= longOp2;
    }
}

OpEQUALCCharCLong::OpEQUALCCharCLong(const BaseType *newResType,
                                     const BaseType *newOp1Type,
                                     const BaseType *newOp2Type,
                                     size_t newResOff,
                                     size_t newOp1Off,
                                     size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpEQUALCCharCLong::operator()(char *res, const char *op1,
                              const char *op2)
{
    r_Long longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    r_Long longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1) || isNull(longOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = longOp1 == longOp2;
    }
}

OpLESSCCharCLong::OpLESSCCharCLong(const BaseType *newResType,
                                   const BaseType *newOp1Type,
                                   const BaseType *newOp2Type,
                                   size_t newResOff,
                                   size_t newOp1Off,
                                   size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpLESSCCharCLong::operator()(char *res, const char *op1,
                             const char *op2)
{
    r_Long longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    r_Long longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1) || isNull(longOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = longOp1 < longOp2;
    }
}

OpLESSEQUALCCharCLong::OpLESSEQUALCCharCLong(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpLESSEQUALCCharCLong::operator()(char *res, const char *op1,
                                  const char *op2)
{
    r_Long longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    r_Long longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1) || isNull(longOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = longOp1 <= longOp2;
    }
}

OpNOTEQUALCCharCLong::OpNOTEQUALCCharCLong(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpNOTEQUALCCharCLong::operator()(char *res, const char *op1,
                                 const char *op2)
{
    r_Long longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    r_Long longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1) || isNull(longOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = longOp1 != longOp2;
    }
}

OpGREATERCCharCLong::OpGREATERCCharCLong(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpGREATERCCharCLong::operator()(char *res, const char *op1,
                                const char *op2)
{
    r_Long longOp1;
    r_Long longOp2;

    *(res + resOff) = *(op1Type->convertToCLong(op1 + op1Off, &longOp1)) >
                      *(op2Type->convertToCLong(op2 + op2Off, &longOp2));
}

OpGREATEREQUALCCharCLong::OpGREATEREQUALCCharCLong(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpGREATEREQUALCCharCLong::operator()(char *res, const char *op1,
                                     const char *op2)
{
    r_Long longOp1 = *(op1Type->convertToCLong(op1 + op1Off, &longOp1));
    r_Long longOp2 = *(op2Type->convertToCLong(op2 + op2Off, &longOp2));

    if (isNull(longOp1) || isNull(longOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = longOp1 >= longOp2;
    }
}

OpEQUALCCharCDouble::OpEQUALCCharCDouble(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpEQUALCCharCDouble::operator()(char *res, const char *op1,
                                const char *op2)
{
    double doubleOp1;
    double doubleOp2;

    op1Type->convertToCDouble(op1 + op1Off, &doubleOp1);
    op2Type->convertToCDouble(op2 + op2Off, &doubleOp2);
    *(res + resOff) = (std::isnan(doubleOp1) && std::isnan(doubleOp2)) || doubleOp1 == doubleOp2;
}

OpLESSCCharCDouble::OpLESSCCharCDouble(const BaseType *newResType,
                                       const BaseType *newOp1Type,
                                       const BaseType *newOp2Type,
                                       size_t newResOff,
                                       size_t newOp1Off,
                                       size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpLESSCCharCDouble::operator()(char *res, const char *op1,
                               const char *op2)
{
    double doubleOp1 = *(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1));
    double doubleOp2 = *(op2Type->convertToCDouble(op2 + op2Off, &doubleOp2));

    if (isNull(doubleOp1) || isNull(doubleOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = doubleOp1 < doubleOp2;
    }
}

OpLESSEQUALCCharCDouble::OpLESSEQUALCCharCDouble(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpLESSEQUALCCharCDouble::operator()(char *res, const char *op1,
                                    const char *op2)
{
    double doubleOp1 = *(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1));
    double doubleOp2 = *(op2Type->convertToCDouble(op2 + op2Off, &doubleOp2));

    if (isNull(doubleOp1) || isNull(doubleOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = doubleOp1 <= doubleOp2;
    }
}

OpNOTEQUALCCharCDouble::OpNOTEQUALCCharCDouble(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpNOTEQUALCCharCDouble::operator()(char *res, const char *op1,
                                   const char *op2)
{
    double doubleOp1;
    double doubleOp2;

    op1Type->convertToCDouble(op1 + op1Off, &doubleOp1);
    op2Type->convertToCDouble(op2 + op2Off, &doubleOp2);

    if (isNull(doubleOp1) || isNull(doubleOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        bool isNan1 = std::isnan(doubleOp1);
        bool isNan2 = std::isnan(doubleOp2);
        *(res + resOff) = (isNan1 != isNan2) || (!(isNan1 || isNan2) && doubleOp1 != doubleOp2);
    }
}

OpGREATERCCharCDouble::OpGREATERCCharCDouble(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpGREATERCCharCDouble::operator()(char *res, const char *op1,
                                  const char *op2)
{
    double doubleOp1 = *(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1));
    double doubleOp2 = *(op2Type->convertToCDouble(op2 + op2Off, &doubleOp2));

    if (isNull(doubleOp1) || isNull(doubleOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = doubleOp1 > doubleOp2;
    }
}

OpGREATEREQUALCCharCDouble::OpGREATEREQUALCCharCDouble(const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff,
        size_t newOp1Off,
        size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpGREATEREQUALCCharCDouble::operator()(char *res, const char *op1,
                                       const char *op2)
{
    double doubleOp1 = *(op1Type->convertToCDouble(op1 + op1Off, &doubleOp1));
    double doubleOp2 = *(op2Type->convertToCDouble(op2 + op2Off, &doubleOp2));

    if (isNull(doubleOp1) || isNull(doubleOp2))
    {
        *(res + resOff) = false;
    }
    else
    {
        *(res + resOff) = doubleOp1 >= doubleOp2;
    }
}

CondenseOp::CondenseOp(const BaseType *newResType, const BaseType *newOpType,
                       size_t newResOff, size_t newOpOff)
    : NullValuesHandler(), accu(0), opType(newOpType), resType(newResType), resOff(newResOff), opOff(newOpOff),
      initialized(false), nullAccu(false)
{
    nullValues = NULL;
}

CondenseOp::CondenseOp(const BaseType *newResType, char *newAccu,
                       const BaseType *newOpType, size_t newResOff,
                       size_t newOpOff)
    : NullValuesHandler(), opType(newOpType), resType(newResType), resOff(newResOff), opOff(newOpOff),
      initialized(false), nullAccu(false)
{
    nullValues = NULL;
    accu = new char[resType->getSize()];
    memcpy(accu, newAccu, resType->getSize());
}

char *
CondenseOp::getAccuVal()
{
    return accu;
}

CondenseOp::~CondenseOp()
{
    delete [] accu;
}


OpSOMECChar::OpSOMECChar(const BaseType *newResType, const BaseType *newOpType,
                         size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    // initialising with neutral value
    accu = new char[1];
    // result is always char
    *accu = 0;
}

OpSOMECChar::OpSOMECChar(const BaseType *newResType, char *newAccu,
                         const BaseType *newOpType, size_t newResOff,
                         size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpSOMECChar::operator()(const char *op, char *init)
{
    *(unsigned char *)(init + resOff) = *(unsigned char *)(init + resOff) || *(unsigned char *)(const_cast<char *>(op) + opOff);
    return init;
}

char *
OpSOMECChar::operator()(const char *op)
{
    *(unsigned char *)(accu + resOff) = *(unsigned char *)(accu + resOff) || *(unsigned char *)(const_cast<char *>(op) + opOff);
    return accu;
}

OpALLCChar::OpALLCChar(const BaseType *newResType, const BaseType *newOpType,
                       size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    // initialising with neutral value
    accu = new char[1];
    // result is always char
    *accu = 1;
}

OpALLCChar::OpALLCChar(const BaseType *newResType, char *newAccu,
                       const BaseType *newOpType, size_t newResOff,
                       size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpALLCChar::operator()(const char *op, char *init)
{
    *(unsigned char *)(init + resOff) = *(unsigned char *)(init + resOff) &&
                                        *(unsigned char *)(const_cast<char *>(op) + opOff);
    return init;
}

char *
OpALLCChar::operator()(const char *op)
{
    *(unsigned char *)(accu + resOff) = *(unsigned char *)(accu + resOff) &&
                                        *(unsigned char *)(const_cast<char *>(op) + opOff);
    return accu;
}

OpCOUNTCChar::OpCOUNTCChar(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    // initialising with neutral value
    accu = new char[4];
    // result is always r_ULong
    *(r_ULong *)accu = 0;

}

OpCOUNTCChar::OpCOUNTCChar(const BaseType *newResType, char *newAccu,
                           const BaseType *newOpType, size_t newResOff,
                           size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpCOUNTCChar::operator()(const char *op, char *init)
{
    *(r_ULong *)(init + resOff) = *(r_ULong *)(init + resOff) +
                                  *(unsigned char *)(const_cast<char *>(op) + opOff);
    return init;
}

char *
OpCOUNTCChar::operator()(const char *op)
{
    *(r_ULong *)(accu + resOff) = *(r_ULong *)(accu + resOff) +
                                  *(unsigned char *)(const_cast<char *>(op) + opOff);
    return accu;
}

OpMAXCULong::OpMAXCULong(const BaseType *newResType, const BaseType *newOpType,
                         size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    r_ULong myVal = 0;
    // initialising with neutral value
    accu = new char[resType->getSize()];
    resType->makeFromCULong(accu, &myVal);
}

OpMAXCULong::OpMAXCULong(const BaseType *newResType, char *newAccu,
                         const BaseType *newOpType, size_t newResOff,
                         size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpMAXCULong::operator()(const char *op, char *init)
{
    r_ULong longOp = 0;
    r_ULong longRes = 0;

    longOp = *(opType->convertToCULong(op + opOff, &longOp));
    longRes = *(resType->convertToCULong(init + resOff, &longRes));

    if (!initialized)
    {
        if (isNullOnly(longOp))
        {
            resType->makeFromCULong(accu, &longOp);
            nullAccu = true;
        }
        initialized = true;
    }

    if (!isNull(longOp) && (nullAccu || longOp > longRes))
    {
        resType->makeFromCULong(init + resOff, &longOp);
        nullAccu = false;
    }

    return init;
}

char *
OpMAXCULong::operator()(const char *op)
{
    return OpMAXCULong::operator()(op, accu);
}

OpMAXCLong::OpMAXCLong(const BaseType *newResType, const BaseType *newOpType,
                       size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    r_Long myVal = std::numeric_limits<r_Long>::lowest();
    // initialising with neutral value
    accu = new char[resType->getSize()];
    resType->makeFromCLong(accu, &myVal);
}

OpMAXCLong::OpMAXCLong(const BaseType *newResType, char *newAccu,
                       const BaseType *newOpType, size_t newResOff,
                       size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpMAXCLong::operator()(const char *op, char *init)
{
    r_Long longOp = 0;
    r_Long longRes = 0;

    longOp = *(opType->convertToCLong(op + opOff, &longOp));
    longRes = *(resType->convertToCLong(init + resOff, &longRes));

    if (!initialized)
    {
        if (isNullOnly(longOp))
        {
            resType->makeFromCLong(accu, &longOp);
            nullAccu = true;
        }
        initialized = true;
    }

    if (!isNull(longOp) && (nullAccu || longOp > longRes))
    {
        resType->makeFromCLong(init + resOff, &longOp);
        nullAccu = false;
    }

    return init;
}

char *
OpMAXCLong::operator()(const char *op)
{
    return OpMAXCLong::operator()(op, accu);
}

OpMAXCDouble::OpMAXCDouble(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    double myVal = std::numeric_limits<double>::lowest();
    // initialising with neutral value
    accu = new char[resType->getSize()];
    // make sure accu contains a legal float
    memset(accu, 0, resType->getSize());
    resType->makeFromCDouble(accu, &myVal);
}

OpMAXCDouble::OpMAXCDouble(const BaseType *newResType, char *newAccu,
                           const BaseType *newOpType, size_t newResOff,
                           size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpMAXCDouble::operator()(const char *op, char *init)
{
    double longOp = 0;
    double longRes = 0;

    longOp = *(opType->convertToCDouble(op + opOff, &longOp));
    longRes = *(resType->convertToCDouble(init + resOff, &longRes));

    if (!initialized)
    {
        if (isNullOnly(longOp))
        {
            resType->makeFromCDouble(accu, &longOp);
            nullAccu = true;
        }
        initialized = true;
    }

    if (!isNull(longOp) && (nullAccu || longOp > longRes))
    {
        resType->makeFromCDouble(init + resOff, &longOp);
        nullAccu = false;
    }

    return init;
}

char *
OpMAXCDouble::operator()(const char *op)
{
    return OpMAXCDouble::operator()(op, accu);
}

OpMINCULong::OpMINCULong(const BaseType *newResType, const BaseType *newOpType,
                         size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    r_ULong myVal = std::numeric_limits<r_ULong>::max();
    // initialising with neutral value
    accu = new char[resType->getSize()];
    resType->makeFromCULong(accu, &myVal);
}

OpMINCULong::OpMINCULong(const BaseType *newResType, char *newAccu,
                         const BaseType *newOpType, size_t newResOff,
                         size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpMINCULong::operator()(const char *op, char *init)
{
    r_ULong longOp = 0;
    r_ULong longRes = 0;

    longOp = *(opType->convertToCULong(op + opOff, &longOp));
    longRes = *(resType->convertToCULong(init + resOff, &longRes));

    if (!initialized)
    {
        if (isNullOnly(longOp))
        {
            resType->makeFromCULong(accu, &longOp);
            nullAccu = true;
        }
        initialized = true;
    }

    if (!isNull(longOp) && (nullAccu || longOp < longRes))
    {
        resType->makeFromCULong(init + resOff, &longOp);
        nullAccu = false;
    }

    return init;
}

char *
OpMINCULong::operator()(const char *op)
{
    return OpMINCULong::operator()(op, accu);
}

OpMINCLong::OpMINCLong(const BaseType *newResType, const BaseType *newOpType,
                       size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    r_Long myVal = (int) std::numeric_limits<r_Long>::max();
    // initialising with neutral value
    accu = new char[resType->getSize()];
    resType->makeFromCLong(accu, &myVal);
}

OpMINCLong::OpMINCLong(const BaseType *newResType, char *newAccu,
                       const BaseType *newOpType, size_t newResOff,
                       size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpMINCLong::operator()(const char *op, char *init)
{
    r_Long longOp = 0;
    r_Long longRes = 0;

    longOp = *(opType->convertToCLong(op + opOff, &longOp));
    longRes = *(resType->convertToCLong(init + resOff, &longRes));

    if (!initialized)
    {
        if (isNullOnly(longOp))
        {
            resType->makeFromCLong(accu, &longOp);
            nullAccu = true;
        }
        initialized = true;
    }

    if (!isNull(longOp) && (nullAccu || longOp < longRes))
    {
        resType->makeFromCLong(init + resOff, &longOp);
        nullAccu = false;
    }

    return init;
}

char *
OpMINCLong::operator()(const char *op)
{
    return OpMINCLong::operator()(op, accu);
}

OpMINCDouble::OpMINCDouble(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    double myVal = std::numeric_limits<double>::max();
    // initialising with neutral value
    accu = new char[resType->getSize()];
    // make sure accu contains a legal float
    memset(accu, 0, resType->getSize());
    resType->makeFromCDouble(accu, &myVal);
}


OpMINCDouble::OpMINCDouble(const BaseType *newResType, char *newAccu,
                           const BaseType *newOpType, size_t newResOff,
                           size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpMINCDouble::operator()(const char *op, char *init)
{
    double longOp = 0;
    double longRes = 0;

    longOp = *(opType->convertToCDouble(op + opOff, &longOp));
    longRes = *(resType->convertToCDouble(init + resOff, &longRes));

    if (!initialized)
    {
        if (isNullOnly(longOp))
        {
            resType->makeFromCDouble(accu, &longOp);
            nullAccu = true;
        }
        initialized = true;
    }

    if (!isNull(longOp) && (nullAccu || longOp < longRes))
    {
        resType->makeFromCDouble(init + resOff, &longOp);
        nullAccu = false;
    }

    return init;
}

char *
OpMINCDouble::operator()(const char *op)
{
    return OpMINCDouble::operator()(op, accu);
}

OpSUMCULong::OpSUMCULong(const BaseType *newResType, const BaseType *newOpType,
                         size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    r_ULong myVal = 0;
    // initialising with neutral value
    accu = new char[resType->getSize()];
    resType->makeFromCULong(accu, &myVal);
}

OpSUMCULong::OpSUMCULong(const BaseType *newResType, char *newAccu,
                         const BaseType *newOpType, size_t newResOff,
                         size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpSUMCULong::operator()(const char *op, char *init)
{
    r_ULong longOp = 0;
    r_ULong longRes = 0;

    opType->convertToCULong(op + opOff, &longOp);
    resType->convertToCULong(init + resOff, &longRes);

    if (!initialized)
    {
        if (isNullOnly(longOp))
        {
            //LWARNING << "longOp is null: " << longOp;
            resType->makeFromCULong(accu, &longOp);
            nullAccu = true;
        }
        initialized = true;
    }

    if (!isNull(longOp))
    {
        if (nullAccu)
        {
            longRes = longOp;
            nullAccu = false;
        }
        else
        {
            longRes += longOp;
        }

        resType->makeFromCULong(init + resOff, &longRes);
    }

    return init;
}

char *
OpSUMCULong::operator()(const char *op)
{
    return OpSUMCULong::operator()(op, accu);
}

OpSUMCLong::OpSUMCLong(const BaseType *newResType, const BaseType *newOpType,
                       size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    r_Long myVal = 0;
    // initialising with neutral value
    accu = new char[resType->getSize()];
    resType->makeFromCLong(accu, &myVal);
}

OpSUMCLong::OpSUMCLong(const BaseType *newResType, char *newAccu,
                       const BaseType *newOpType, size_t newResOff,
                       size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpSUMCLong::operator()(const char *op, char *init)
{
    r_Long longOp = 0;
    r_Long longRes = 0;

    longOp = *(opType->convertToCLong(op + opOff, &longOp));
    longRes = *(resType->convertToCLong(init + resOff, &longRes));

    if (!initialized)
    {
        if (isNullOnly(longOp))
        {
            resType->makeFromCLong(accu, &longOp);
            nullAccu = true;
        }
        initialized = true;
    }

    if (!isNull(longOp))
    {
        if (nullAccu)
        {
            longRes = longOp;
            nullAccu = false;
        }
        else
        {
            longRes += longOp;
        }

        resType->makeFromCLong(init + resOff, &longRes);
    }

    return init;
}

char *
OpSUMCLong::operator()(const char *op)
{
    return OpSUMCLong::operator()(op, accu);
}

OpSUMCDouble::OpSUMCDouble(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    double myVal = 0.0;
    // initialising with neutral value
    accu = new char[resType->getSize()];
    resType->makeFromCDouble(accu, &myVal);
}

OpSUMCDouble::OpSUMCDouble(const BaseType *newResType, char *newAccu,
                           const BaseType *newOpType, size_t newResOff,
                           size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpSUMCDouble::operator()(const char *op, char *init)
{
    double longOp = 0;
    double longRes = 0;

    longOp = *(opType->convertToCDouble(op + opOff, &longOp));
    longRes = *(resType->convertToCDouble(init + resOff, &longRes));

    if (!initialized)
    {
        if (isNullOnly(longOp))
        {
            resType->makeFromCDouble(accu, &longOp);
            nullAccu = true;
        }
        initialized = true;
    }

    if (!isNull(longOp))
    {
        if (nullAccu)
        {
            longRes = longOp;
            nullAccu = false;
        }
        else
        {
            longRes += longOp;
        }

        resType->makeFromCDouble(init + resOff, &longRes);
    }

    return init;
}

char *
OpSUMCDouble::operator()(const char *op)
{
    return OpSUMCDouble::operator()(op, accu);
}

/// OpSQSUM

OpSQSUMCDouble::OpSQSUMCDouble(const BaseType *newResType, const BaseType *newOpType,
                               size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    double myVal = 0.0;
    // initialising with neutral value
    accu = new char[resType->getSize()];
    resType->makeFromCDouble(accu, &myVal);
}

OpSQSUMCDouble::OpSQSUMCDouble(const BaseType *newResType, char *newAccu,
                               const BaseType *newOpType, size_t newResOff,
                               size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
{
}

char *
OpSQSUMCDouble::operator()(const char *op, char *init)
{
    double longOp = 0;
    double longRes = 0;

    longOp = *(opType->convertToCDouble(op + opOff, &longOp));
    longRes = *(resType->convertToCDouble(init + resOff, &longRes));

    if (!initialized)
    {
        if (isNullOnly(longOp))
        {
            resType->makeFromCDouble(accu, &longOp);
            nullAccu = true;
        }
        initialized = true;
    }

    if (!isNull(longOp))
    {
        if (nullAccu)
        {
            longRes = longOp * longOp;
            nullAccu = false;
        }
        else
        {
            longRes += longOp * longOp;
        }

        resType->makeFromCDouble(init + resOff, &longRes);
    }

    return init;
}

char *
OpSQSUMCDouble::operator()(const char *op)
{
    return OpSQSUMCDouble::operator()(op, accu);
}


///

OpCondenseStruct::OpCondenseStruct(
    const BaseType *newResType,
    const BaseType *newOpType,
    Ops::OpType op,
    size_t newResOff,
    size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    size_t i = 0;

    myResType = dynamic_cast<StructType *>(const_cast<BaseType *>(newResType));
    myOpType = dynamic_cast<StructType *>(const_cast<BaseType *>(newOpType));
    numElems = myOpType->getNumElems();
    elemOps = new CondenseOp*[numElems];
    for (i = 0; i < numElems; i++)
    {
        elemOps[i] = Ops::getCondenseOp(
                         op,
                         myResType->getElemType(i),
                         myOpType->getElemType(i),
                         newResOff + myResType->getOffset(i),
                         newOpOff + myOpType->getOffset(i)
                     );
    }

    accu = new char[resType->getSize()];
    for (i = 0; i < numElems; i++)
    {
        memcpy(accu + myResType->getOffset(i), elemOps[i]->getAccuVal(),
               myResType->getElemType(i)->getSize());
    }

}

//--------------------------------------------
//  OpCondenseStruct
//--------------------------------------------

OpCondenseStruct::OpCondenseStruct(
    const BaseType *newResType,
    char *newAccu,
    const BaseType *newOpType,
    Ops::OpType op,
    size_t newResOff,
    size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
{
    size_t i = 0;

    myResType = dynamic_cast<StructType *>(const_cast<BaseType *>(newResType));
    myOpType = dynamic_cast<StructType *>(const_cast<BaseType *>(newOpType));
    numElems = myOpType->getNumElems();
    elemOps = new CondenseOp*[numElems];
    for (i = 0; i < numElems; i++)
    {
        elemOps[i] = Ops::getCondenseOp(
                         op,
                         myResType->getElemType(i),
                         myOpType->getElemType(i),
                         newResOff + myResType->getOffset(i),
                         newOpOff + myOpType->getOffset(i)
                     );
    }

    accu = new char[resType->getSize()];
    memcpy(accu, newAccu, resType->getSize());
}

OpCondenseStruct::~OpCondenseStruct()
{
    size_t i;

    for (i = 0; i < numElems; i++)
    {
        delete elemOps[i];
    }
    delete[] elemOps;
}

char *
OpCondenseStruct::operator()(const char *op, char *init)
{
    size_t i;

    for (i = 0; i < numElems; i++)
    {
        (*elemOps[i])(op, init);
    }
    return init;
}

char *
OpCondenseStruct::operator()(const char *op)
{
    size_t i;

    for (i = 0; i < numElems; i++)
    {
        (*elemOps[i])(op, accu);
    }
    return accu;
}



//--------------------------------------------
//  OpBinaryStruct
//--------------------------------------------

static Ops::OpType _operation;

OpBinaryStruct::OpBinaryStruct(const BaseType *newStructType, Ops::OpType op,
                               const BaseType *op1typeArg, const BaseType *op2typeArg,
                               size_t newResOff, size_t newOp1Off,
                               size_t newOp2Off)
    : BinaryOp(newStructType, newStructType, newStructType, newResOff,
               newOp1Off, newOp2Off)
{
    size_t i = 0;

    _operation = op;
    BaseType *boolType = ((BaseType *)ObjectBroker::getObjectByName(OId::ATOMICTYPEOID, "Bool"));


    myStructType = dynamic_cast<StructType *>(const_cast<BaseType *>(newStructType));
    StructType *op1type = dynamic_cast<StructType *>(const_cast<BaseType *>(op1typeArg));
    StructType *op2type = dynamic_cast<StructType *>(const_cast<BaseType *>(op2typeArg));
    numElems = myStructType->getNumElems();
    elemOps = new BinaryOp*[numElems];
    equalOps = new BinaryOp*[numElems];
    lessOps = new BinaryOp*[numElems];
    assignmentOps = new UnaryOp*[numElems];
    for (i = 0; i < numElems; i++)
    {
        equalOps[i] = NULL;
        lessOps[i] = NULL;
        assignmentOps[i] = NULL;

        elemOps[i] = Ops::getBinaryOp(op, myStructType->getElemType(i),
                                      op1type->getElemType(i),
                                      op2type->getElemType(i),
                                      newResOff + myStructType->getOffset(i),
                                      newOp1Off + op1type->getOffset(i),
                                      newOp2Off + op2type->getOffset(i));
        if (elemOps[i] == NULL)
        {
            throw r_Error(CELLBINARYOPUNAVAILABLE);
        }
        if (op == Ops::OP_MIN_BINARY || op == Ops::OP_MAX_BINARY)
        {
            lessOps[i] = Ops::getBinaryOp(Ops::OP_LESS, boolType,
                                          op1type->getElemType(i),
                                          op2type->getElemType(i),
                                          0,
                                          newOp1Off + op1type->getOffset(i),
                                          newOp2Off + op2type->getOffset(i));
            equalOps[i] = Ops::getBinaryOp(Ops::OP_EQUAL, boolType,
                                           op1type->getElemType(i),
                                           op2type->getElemType(i),
                                           0,
                                           newOp1Off + op1type->getOffset(i),
                                           newOp2Off + op2type->getOffset(i));
            assignmentOps[i] = Ops::getUnaryOp(Ops::OP_IDENTITY, myStructType->getElemType(i),
                                               myStructType->getElemType(i),
                                               newResOff + myStructType->getOffset(i),
                                               newOp1Off + myStructType->getOffset(i));
            if (lessOps[i] == NULL || equalOps[i] == NULL || assignmentOps[i] == NULL)
            {
                throw r_Error(CELLBINARYOPUNAVAILABLE);
            }
        }
    }
}

OpBinaryStruct::~OpBinaryStruct()
{
    for (size_t i = 0; i < numElems; i++)
    {
        if (elemOps[i])
        {
            delete elemOps[i];
        }
        if (lessOps[i])
        {
            delete lessOps[i];
        }
        if (equalOps[i])
        {
            delete equalOps[i];
        }
        if (assignmentOps[i])
        {
            delete assignmentOps[i];
        }
    }
    delete[] elemOps;
    delete[] lessOps;
    delete[] equalOps;
    delete[] assignmentOps;
}

void
OpBinaryStruct::operator()(char *res, const char *op1,
                           const char *op2)
{
    size_t i;

    if (_operation == Ops::OP_OVERLAY)
    {
        for (i = 0; i < numElems; ++i)
            if (*(op2 + op2Off) && !isNull(*(op2 + op2Off)))
            {
                for (size_t j = 0; j < numElems; ++j)
                {
                    *(res + resOff) = *(op2 + op2Off);
                }
                return;
            }
    }
    if (_operation == Ops::OP_MIN_BINARY || _operation == Ops::OP_MAX_BINARY)
    {
        bool op1Min = true;
        for (i = 0; i < numElems; ++i)
        {
            (*lessOps[i])(boolRes, op1, op2);
            bool op1LessThanOp2 = static_cast<bool>(boolRes[0]);
            if (!op1LessThanOp2)
            {
                (*equalOps[i])(boolRes, op1, op2);
                bool op1EqualToOp2 = static_cast<bool>(boolRes[0]);
                if (!op1EqualToOp2)
                {
                    op1Min = false;
                    break;
                }
            }
        }
        const char *op = op1;
        if ((_operation == Ops::OP_MIN_BINARY && !op1Min) ||
                (_operation == Ops::OP_MAX_BINARY && op1Min))
        {
            op = op2;
        }
        for (i = 0; i < numElems; i++)
        {
            (*assignmentOps[i])(res, op);
        }
    }
    else
    {
        for (i = 0; i < numElems; i++)
        {
            (*elemOps[i])(res, op1, op2);
        }
    }

}

//--------------------------------------------
//  OpBinaryStructConst
//--------------------------------------------

OpBinaryStructConst::OpBinaryStructConst(
    const BaseType *res,
    const BaseType *op1,
    const BaseType *op2,
    Ops::OpType op,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off)
    : BinaryOp(res, op1, op2, newResOff,
               newOp1Off, newOp2Off)
{
    size_t i = 0;

    resStructType = dynamic_cast<StructType *>(const_cast<BaseType *>(resType));
    opStructType = dynamic_cast<StructType *>(const_cast<BaseType *>(op1Type));
    numElems = opStructType->getNumElems();
    elemOps = new BinaryOp*[numElems];
    for (i = 0; i < numElems; i++)
    {
        elemOps[i] = Ops::getBinaryOp(op,
                                      resStructType->getElemType(i),
                                      opStructType->getElemType(i),
                                      op2Type,
                                      newResOff + resStructType->getOffset(i),
                                      newOp1Off + opStructType->getOffset(i),
                                      newOp2Off);
    }
}

OpBinaryStructConst::~OpBinaryStructConst()
{
    size_t i;

    for (i = 0; i < numElems; i++)
    {
        delete elemOps[i];
    }
    delete[] elemOps;
}

void
OpBinaryStructConst::operator()(char *res, const char *op1,
                                const char *op2)
{
    size_t i;

    for (i = 0; i < numElems; i++)
    {
        (*elemOps[i])(res, op1, op2);
    }
}

//--------------------------------------------
//  OpBinaryConstStruct
//--------------------------------------------

OpBinaryConstStruct::OpBinaryConstStruct(
    const BaseType *res,
    const BaseType *op1,
    const BaseType *op2,
    Ops::OpType op,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off)
    : BinaryOp(res, op1, op2, newResOff,
               newOp1Off, newOp2Off)
{
    size_t i = 0;

    resStructType = dynamic_cast<StructType *>(const_cast<BaseType *>(resType));
    opStructType = dynamic_cast<StructType *>(const_cast<BaseType *>(op2Type));
    numElems = opStructType->getNumElems();
    elemOps = new BinaryOp*[numElems];
    for (i = 0; i < numElems; i++)
    {
        elemOps[i] = Ops::getBinaryOp(op,
                                      resStructType->getElemType(i),
                                      op1Type,
                                      opStructType->getElemType(i),
                                      newResOff + resStructType->getOffset(i),
                                      newOp1Off,
                                      newOp2Off + opStructType->getOffset(i));
    }
}

OpBinaryConstStruct::~OpBinaryConstStruct()
{
    size_t i;

    for (i = 0; i < numElems; i++)
    {
        delete elemOps[i];
    }
    delete[] elemOps;
}

void
OpBinaryConstStruct::operator()(char *res, const char *op1,
                                const char *op2)
{
    size_t i;

    for (i = 0; i < numElems; i++)
    {
        (*elemOps[i])(res, op1, op2);
    }
}

//--------------------------------------------
//  OpEQUALStruct
//--------------------------------------------

OpEQUALStruct::OpEQUALStruct(const BaseType *newResType,
                             const BaseType *newOp1Type,
                             const BaseType *newOp2Type,
                             size_t newResOff,
                             size_t newOp1Off,
                             size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off)
{
    bool struct1 = op1Type->getType() == STRUCT;
    bool struct2 = op2Type->getType() == STRUCT;
    numElems = dynamic_cast<StructType *>(const_cast<BaseType *>(struct1 ? op1Type : op2Type))->getNumElems();
    elemOps = new BinaryOp*[numElems];
    for (size_t i = 0; i < numElems; i++)
    {
        auto *type1 = op1Type;
        size_t offset1 = 0;
        if (struct1)
        {
            type1 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op1Type)))->getElemType(i);
            offset1 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op1Type)))->getOffset(i);
        }
        auto *type2 = op2Type;
        size_t offset2 = 0;
        if (struct2)
        {
            type2 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op2Type)))->getElemType(i);
            offset2 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op2Type)))->getOffset(i);
        }
        elemOps[i] = Ops::getBinaryOp(Ops::OP_EQUAL, resType, type1, type2,
                                      newResOff, newOp1Off + offset1, newOp2Off + offset2);
    }
}

OpEQUALStruct::~OpEQUALStruct()
{
    for (size_t i = 0; i < numElems; i++)
    {
        delete elemOps[i];
    }
    delete[] elemOps;
}

void
OpEQUALStruct::operator()(char *res, const char *op1,
                          const char *op2)
{
    char dummy = 1;
    for (size_t i = 0; i < numElems; i++)
    {
        (*elemOps[i])(res, op1, op2);
        dummy = *res && dummy;
    }
    *res = dummy;
}

//--------------------------------------------
//  OpNOTEQUALStruct
//--------------------------------------------

OpNOTEQUALStruct::OpNOTEQUALStruct(const BaseType *newResType,
                                   const BaseType *newOp1Type,
                                   const BaseType *newOp2Type,
                                   size_t newResOff,
                                   size_t newOp1Off,
                                   size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off)
{
    bool struct1 = op1Type->getType() == STRUCT;
    bool struct2 = op2Type->getType() == STRUCT;
    numElems = dynamic_cast<StructType *>(const_cast<BaseType *>(struct1 ? op1Type : op2Type))->getNumElems();
    elemOps = new BinaryOp*[numElems];
    for (size_t i = 0; i < numElems; i++)
    {
        auto *type1 = op1Type;
        size_t offset1 = 0;
        if (struct1)
        {
            type1 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op1Type)))->getElemType(i);
            offset1 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op1Type)))->getOffset(i);
        }
        auto *type2 = op2Type;
        size_t offset2 = 0;
        if (struct2)
        {
            type2 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op2Type)))->getElemType(i);
            offset2 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op2Type)))->getOffset(i);
        }
        elemOps[i] = Ops::getBinaryOp(Ops::OP_NOTEQUAL, resType, type1, type2,
                                      newResOff, newOp1Off + offset1, newOp2Off + offset2);
    }
}

OpNOTEQUALStruct::~OpNOTEQUALStruct()
{
    for (size_t i = 0; i < numElems; i++)
    {
        delete elemOps[i];
    }
    delete[] elemOps;
}

void
OpNOTEQUALStruct::operator()(char *res, const char *op1,
                             const char *op2)
{
    char dummy = 0;
    for (size_t i = 0; i < numElems; i++)
    {
        (*elemOps[i])(res, op1, op2);
        dummy = *res || dummy;
    }
    *res = dummy;
}

//--------------------------------------------
//  OpComparisonStruct (<, >, <=, >=)
//--------------------------------------------

OpComparisonStruct::OpComparisonStruct(Ops::OpType op,
                                       const BaseType *newResType,
                                       const BaseType *newOp1Type,
                                       const BaseType *newOp2Type,
                                       size_t newResOff,
                                       size_t newOp1Off,
                                       size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off)
{
    bool struct1 = op1Type->getType() == STRUCT;
    bool struct2 = op2Type->getType() == STRUCT;
    numElems = dynamic_cast<StructType *>(const_cast<BaseType *>(struct1 ? op1Type : op2Type))->getNumElems();
    elemOps = new BinaryOp*[numElems];
    equalOps = new BinaryOp*[numElems];
    for (size_t i = 0; i < numElems; i++)
    {
        auto *type1 = op1Type;
        size_t offset1 = 0;
        if (struct1)
        {
            type1 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op1Type)))->getElemType(i);
            offset1 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op1Type)))->getOffset(i);
        }
        auto *type2 = op2Type;
        size_t offset2 = 0;
        if (struct2)
        {
            type2 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op2Type)))->getElemType(i);
            offset2 = (dynamic_cast<StructType *>(const_cast<BaseType *>(op2Type)))->getOffset(i);
        }
        elemOps[i] = Ops::getBinaryOp(op, resType, type1, type2,
                                      newResOff, newOp1Off + offset1, newOp2Off + offset2);
        equalOps[i] = Ops::getBinaryOp(Ops::OP_EQUAL, resType, type1, type2,
                                       newResOff, newOp1Off + offset1, newOp2Off + offset2);
    }
}

OpComparisonStruct::~OpComparisonStruct()
{
    for (size_t i = 0; i < numElems; i++)
    {
        delete elemOps[i];
        delete equalOps[i];
    }
    delete[] elemOps;
    delete[] equalOps;
}

void
OpComparisonStruct::operator()(char *res, const char *op1,
                               const char *op2)
{
    *res = 0;
    for (size_t i = 0; i < numElems; i++)
    {
        (*elemOps[i])(res, op1, op2);
        if (*res)
        {
            // as soon as result is true we're done
            break;
        }
        else
        {
            // otherwise check operands are equal
            (*equalOps[i])(res, op1, op2);
            if (!*res)
            {
                // not equal, result is false
                break;
            }
        }
    }
}

//--------------------------------------------
//  OpUnaryStruct
//--------------------------------------------

OpUnaryStruct::OpUnaryStruct(
    const BaseType *newResType,
    const BaseType *newOpType,
    Ops::OpType op,
    size_t newResOff,
    size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
    size_t i = 0;

    myResType = dynamic_cast<StructType *>(const_cast<BaseType *>(newResType));
    myOpType = dynamic_cast<StructType *>(const_cast<BaseType *>(newOpType));
    numElems = myOpType->getNumElems();
    elemOps = new UnaryOp*[numElems];
    for (i = 0; i < numElems; i++)
    {
        elemOps[i] = Ops::getUnaryOp(
                         op,
                         myResType->getElemType(i),
                         myOpType->getElemType(i),
                         newResOff + myResType->getOffset(i),
                         newOpOff + myOpType->getOffset(i)
                     );
        if (elemOps[i] == NULL)
        {
            throw r_Error(CELLUNARYOPUNAVAILABLE);
        }
    }
}

OpUnaryStruct::~OpUnaryStruct()
{
    size_t i;

    for (i = 0; i < numElems; i++)
    {
        delete elemOps[i];
    }
    delete[] elemOps;
}

void
OpUnaryStruct::operator()(char *result, const char *op)
{
    size_t i;

    for (i = 0; i < numElems; i++)
    {
        try
        {
            (*elemOps[i])(result, op);
        }
        catch (...)
        {
            // cleanup
            for (i = 0; i < numElems; i++)
            {
                delete elemOps[i];
            }
            delete[] elemOps;
            throw;
        }
    }
}

//--------------------------------------------
//  OpPLUSChar
//--------------------------------------------

OpPLUSChar::OpPLUSChar(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff,
                       size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpPLUSChar::operator()(char *res, const char *op1, const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1) + op1Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1)  + op1Off);
    }
    else if (isNull(*(unsigned char *)(const_cast<char *>(op2)  + op2Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
    else
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1) + op1Off)
                                           + *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
}

void
OpPLUSChar::getCondenseInit(char *init)
{
    *init = 0;
}

//--------------------------------------------
//  OpMAX_BINARYChar
//--------------------------------------------

OpMAX_BINARYChar::OpMAX_BINARYChar(const BaseType *newResType, const BaseType *newOp1Type,
                                   const BaseType *newOp2Type, size_t newResOff,
                                   size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMAX_BINARYChar::operator()(char *res, const char *op1, const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1) + op1Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1)  + op1Off);
    }
    else if (isNull(*(unsigned char *)(const_cast<char *>(op2)  + op2Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
    else if (*(unsigned char *)(const_cast<char *>(op1)  + op1Off) > *(unsigned char *)(const_cast<char *>(op2)  + op2Off))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1)  + op1Off);
    }
    else
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
}

void
OpMAX_BINARYChar::getCondenseInit(char *init)
{
    *(r_Char *)init = std::numeric_limits<r_Char>::lowest();
}

//--------------------------------------------
//  OpMIN_BINARYChar
//--------------------------------------------

OpMIN_BINARYChar::OpMIN_BINARYChar(const BaseType *newResType, const BaseType *newOp1Type,
                                   const BaseType *newOp2Type, size_t newResOff,
                                   size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMIN_BINARYChar::operator()(char *res, const char *op1, const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1) + op1Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1)  + op1Off);
    }
    else if (isNull(*(unsigned char *)(const_cast<char *>(op2)  + op2Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
    else if (*(unsigned char *)(const_cast<char *>(op1)  + op1Off) < * (unsigned char *)(const_cast<char *>(op2)  + op2Off))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1)  + op1Off);
    }
    else
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
}

void
OpMIN_BINARYChar::getCondenseInit(char *init)
{
    *(r_Char *)init = std::numeric_limits<r_Char>::max();
}

//--------------------------------------------
//  OpMINUSChar
//--------------------------------------------

OpMINUSChar::OpMINUSChar(const BaseType *newResType, const BaseType *newOp1Type,
                         const BaseType *newOp2Type, size_t newResOff,
                         size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMINUSChar::operator()(char *res, const char *op1, const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1)  + op1Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1) + op1Off);
    }
    else if (isNull(*(unsigned char *)(const_cast<char *>(op2)  + op2Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
    else
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1) + op1Off)
                                           - *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
}

//--------------------------------------------
//  OpDIVChar
//--------------------------------------------

OpDIVChar::OpDIVChar(const BaseType *newResType, const BaseType *newOp1Type,
                     const BaseType *newOp2Type, size_t newResOff,
                     size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpDIVChar::operator()(char *res, const char *op1, const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1) + op1Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1) + op1Off);
    }
    else if (isNull(*(unsigned char *)(const_cast<char *>(op2)  + op2Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
    else
    {
        if (*(unsigned char *)(const_cast<char *>(op2)  + op2Off) == 0)
        {
            throw DIVISION_BY_ZERO;
        }
        else
        {
            *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op2)  + op1Off)
                                               / *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
        }
    }
}

//--------------------------------------------
//  OpMODChar
//--------------------------------------------

OpMODChar::OpMODChar(const BaseType *newResType, const BaseType *newOp1Type,
                     const BaseType *newOp2Type, size_t newResOff,
                     size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMODChar::operator()(char *res, const char *op1, const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1) + op1Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1) + op1Off);
    }
    else if (isNull(*(unsigned char *)(const_cast<char *>(op2) + op2Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op2) + op2Off);
    }
    else
    {
        if (*(unsigned char *)(const_cast<char *>(op2) + op2Off) == 0)
        {
            throw DIVISION_BY_ZERO;
        }
        else
        {
            *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1) + op1Off) %
                                               *(unsigned char *)(const_cast<char *>(op2) + op2Off);
        }
    }
}

//--------------------------------------------
//  OpMULTChar
//--------------------------------------------

OpMULTChar::OpMULTChar(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff,
                       size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpMULTChar::operator()(char *res, const char *op1, const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1) + op1Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1) + op1Off);
    }
    else if (isNull(*(unsigned char *)(const_cast<char *>(op2) + op2Off)))
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op2) + op2Off);
    }
    else
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1) + op1Off)
                                           * *(unsigned char *)(const_cast<char *>(op2) + op2Off);
    }
}

void
OpMULTChar::getCondenseInit(char *init)
{
    *init = 1;
}

//--------------------------------------------
//  OpEQUALChar
//--------------------------------------------

OpEQUALChar::OpEQUALChar(const BaseType *newResType,
                         const BaseType *newOp1Type,
                         const BaseType *newOp2Type,
                         size_t newResOff,
                         size_t newOp1Off,
                         size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpEQUALChar::operator()(char *res, const char *op1,
                        const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1) + op1Off)) || isNull(*(unsigned char *)(const_cast<char *>(op2) + op2Off)))
    {
        *(unsigned char *)(res + resOff) = false;
    }
    else
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1) + op1Off) ==
                                           *(unsigned char *)(const_cast<char *>(op2) + op2Off);
    }
}

//--------------------------------------------
//  OpLESSChar
//--------------------------------------------

OpLESSChar::OpLESSChar(const BaseType *newResType,
                       const BaseType *newOp1Type,
                       const BaseType *newOp2Type,
                       size_t newResOff,
                       size_t newOp1Off,
                       size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpLESSChar::operator()(char *res, const char *op1,
                       const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1) + op1Off)) || isNull(*(unsigned char *)(const_cast<char *>(op2) + op2Off)))
    {
        *(unsigned char *)(res + resOff) = false;
    }
    else
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1) + op1Off) <
                                           *(unsigned char *)(const_cast<char *>(op2) + op2Off);
    }
}

//--------------------------------------------
//  OpLESSEQUALChar
//--------------------------------------------

OpLESSEQUALChar::OpLESSEQUALChar(const BaseType *newResType,
                                 const BaseType *newOp1Type,
                                 const BaseType *newOp2Type,
                                 size_t newResOff,
                                 size_t newOp1Off,
                                 size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpLESSEQUALChar::operator()(char *res, const char *op1,
                            const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1)  + op1Off)) || isNull(*(unsigned char *)(const_cast<char *>(op2) + op2Off)))
    {
        *(unsigned char *)(res + resOff) = false;
    }
    else
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1)  + op1Off) <=
                                           *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
}

//--------------------------------------------
//  OpNOTEQUALChar
//--------------------------------------------

OpNOTEQUALChar::OpNOTEQUALChar(const BaseType *newResType,
                               const BaseType *newOp1Type,
                               const BaseType *newOp2Type,
                               size_t newResOff,
                               size_t newOp1Off,
                               size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpNOTEQUALChar::operator()(char *res, const char *op1,
                           const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1) + op1Off)) || isNull(*(unsigned char *)(const_cast<char *>(op2) + op2Off)))
    {
        *(unsigned char *)(res + resOff) = false;
    }
    else
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1)  + op1Off) !=
                                           *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
}

//--------------------------------------------
//  OpISNULL
//--------------------------------------------

OpISNULLCLong::OpISNULLCLong(const BaseType *newResType, const BaseType *newOpType,
                             size_t newResOff, size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpISNULLCLong::operator()(char *result, const char *op)
{
    r_Long longOp;
    longOp  = *(opType->convertToCLong(op + opOff, &longOp));
    *(result + resOff) = isNull(longOp);
}

OpISNULLCULong::OpISNULLCULong(const BaseType *newResType, const BaseType *newOpType,
                               size_t newResOff, size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpISNULLCULong::operator()(char *result, const char *op)
{
    r_ULong longOp;
    longOp  = *(opType->convertToCULong(op + opOff, &longOp));
    *(result + resOff) = isNull(longOp);
}

OpISNULLCDouble::OpISNULLCDouble(const BaseType *newResType, const BaseType *newOpType,
                                 size_t newResOff, size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpISNULLCDouble::operator()(char *result, const char *op)
{
    double doubleOp;
    doubleOp  = *(opType->convertToCDouble(op + opOff, &doubleOp));
    *(result + resOff) = isNull(doubleOp) || std::isnan(doubleOp);
}

//--------------------------------------------
//  OpGREATERChar
//--------------------------------------------

OpGREATERChar::OpGREATERChar(const BaseType *newResType,
                             const BaseType *newOp1Type,
                             const BaseType *newOp2Type,
                             size_t newResOff,
                             size_t newOp1Off,
                             size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpGREATERChar::operator()(char *res, const char *op1,
                          const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1)  + op1Off)) || isNull(*(unsigned char *)(const_cast<char *>(op2)  + op2Off)))
    {
        *(unsigned char *)(res + resOff) = false;
    }
    else
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1)  + op1Off) >
                                           *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
}



//--------------------------------------------
//  OpGREATEREQUALChar
//--------------------------------------------

OpGREATEREQUALChar::OpGREATEREQUALChar(const BaseType *newResType,
                                       const BaseType *newOp1Type,
                                       const BaseType *newOp2Type,
                                       size_t newResOff,
                                       size_t newOp1Off,
                                       size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff,
               newOp1Off, newOp2Off)
{
}

void
OpGREATEREQUALChar::operator()(char *res, const char *op1,
                               const char *op2)
{
    if (isNull(*(unsigned char *)(const_cast<char *>(op1)  + op1Off)) || isNull(*(unsigned char *)(const_cast<char *>(op2)  + op2Off)))
    {
        *(unsigned char *)(res + resOff) = false;
    }
    else
    {
        *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op1)  + op1Off) >=
                                           *(unsigned char *)(const_cast<char *>(op2)  + op2Off);
    }
}

//--------------------------------------------
//  OpIDENTITYChar
//--------------------------------------------

OpIDENTITYChar::OpIDENTITYChar(const BaseType *newResType, const BaseType *newOpType,
                               size_t newResOff,
                               size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpIDENTITYChar::operator()(char *res, const char *op)
{
    *(unsigned char *)(res + resOff) = *(unsigned char *)(const_cast<char *>(op) + opOff);
}

OpUpdateChar::OpUpdateChar(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff,
                           size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpUpdateChar::operator()(char *res, const char *op)
{
    auto opVal = *(unsigned char *)(const_cast<char *>(op) + opOff);
    if (!isNull(opVal))
    {
        *(unsigned char *)(res + resOff) = opVal;
    }
}

OpUpdateOctet::OpUpdateOctet(const BaseType *newResType, const BaseType *newOpType,
                             size_t newResOff,
                             size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpUpdateOctet::operator()(char *res, const char *op)
{
    auto opVal = *(const signed char *)(op + opOff);
    if (!isNull(opVal))
    {
        *(signed char *)(res + resOff) = opVal;
    }
}

//--------------------------------------------
//
//--------------------------------------------

OpIDENTITYShort::OpIDENTITYShort(const BaseType *newResType, const BaseType *newOpType,
                                 size_t newResOff,
                                 size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpIDENTITYShort::operator()(char *res, const char *op)
{
    *(r_UShort *)(res + resOff) = *(const r_UShort *)(op + opOff);
}

OpUpdateShort::OpUpdateShort(const BaseType *newResType, const BaseType *newOpType,
                             size_t newResOff,
                             size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpUpdateShort::operator()(char *res, const char *op)
{
    auto opVal = *(const r_Short *)(op + opOff);
    if (!isNull(opVal))
    {
        *(r_Short *)(res + resOff) = opVal;
    }
}

OpUpdateUShort::OpUpdateUShort(const BaseType *newResType, const BaseType *newOpType,
                               size_t newResOff,
                               size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpUpdateUShort::operator()(char *res, const char *op)
{
    auto opVal = *(const r_UShort *)(op + opOff);
    if (!isNull(opVal))
    {
        *(r_UShort *)(res + resOff) = opVal;
    }
}

//--------------------------------------------
//  OpIDENTITYLong
//--------------------------------------------

OpIDENTITYLong::OpIDENTITYLong(const BaseType *newResType, const BaseType *newOpType,
                               size_t newResOff,
                               size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpIDENTITYLong::operator()(char *res, const char *op)
{
    *(r_ULong *)(res + resOff) = *(const r_ULong *)(op + opOff);
}

OpUpdateLong::OpUpdateLong(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff,
                           size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpUpdateLong::operator()(char *res, const char *op)
{
    auto opVal = *(const r_Long *)(op + opOff);
    if (!isNull(opVal))
    {
        *(r_Long *)(res + resOff) = opVal;
    }
}

OpUpdateULong::OpUpdateULong(const BaseType *newResType, const BaseType *newOpType,
                             size_t newResOff,
                             size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpUpdateULong::operator()(char *res, const char *op)
{
    auto opVal = *(const r_ULong *)(op + opOff);
    if (!isNull(opVal))
    {
        *reinterpret_cast<r_Long*>(res + resOff) = opVal;
    }
}

OpUpdateFloat::OpUpdateFloat(const BaseType *newResType, const BaseType *newOpType,
                             size_t newResOff,
                             size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpUpdateFloat::operator()(char *res, const char *op)
{
    auto opVal = *(const r_Float *)(op + opOff);
    if (!isNull(opVal))
    {
        *(r_Float *)(res + resOff) = opVal;
    }
}

OpUpdateDouble::OpUpdateDouble(const BaseType *newResType, const BaseType *newOpType,
                               size_t newResOff,
                               size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
}

void
OpUpdateDouble::operator()(char *res, const char *op)
{
    auto opVal = *(const r_Double *)(op + opOff);
    if (!isNull(opVal))
    {
        *(r_Double *)(res + resOff) = opVal;
    }
}

//--------------------------------------------
//  MarrayOp
//--------------------------------------------

MarrayOp::MarrayOp(const BaseType *newResType, size_t newResOff)
    : resType(newResType), resOff(newResOff)
{
}

void
MarrayOp::operator()(char *result, const r_Point &p)
{
    r_ULong sum = 0;

    for (size_t i = 0; i < p.dimension(); i++)
    {
        sum += p[i];
    }

    resType->makeFromCULong(result, &sum);
}

//--------------------------------------------
//  GenCondenseOp
//--------------------------------------------

GenCondenseOp::GenCondenseOp(const BaseType *newResType, size_t newResOff,
                             BinaryOp *newAccuOp, char *newInitVal)
    : resType(newResType), resOff(newResOff), accuOp(newAccuOp), myInitVal(false), initVal(NULL)
{
    if (newInitVal == NULL || resType->getType() == STRUCT)
    {
        initVal = new char[resType->getSize()];
        memset(initVal, '\0', resType->getSize());
        accuOp->getCondenseInit(initVal);
        myInitVal = true;
    }
    else
    {
        initVal = newInitVal;
    }
}

GenCondenseOp::~GenCondenseOp()
{
    if (myInitVal)
    {
        delete [] initVal;
    }
}


void
GenCondenseOp::operator()(const r_Point &p)
{
    r_ULong sum = 0;
    char buf[8];
    memset(buf, '\0', 8);

    for (size_t i = 0; i < p.dimension(); i++)
    {
        sum += p[i];
    }

    resType->makeFromCULong(buf, &sum);

    (*accuOp)(initVal, initVal, buf);
}

BinaryOp *
GenCondenseOp::getAccuOp()
{
    return accuOp;
}

const BaseType *
GenCondenseOp::getResultType()
{
    return resType;
}

size_t
GenCondenseOp::getResultOff()
{
    return resOff;
}

char *
GenCondenseOp::getAccuVal()
{
    return initVal;
}


//--------------------------------------------
//  Complex
//--------------------------------------------

// *** PLUS ***

OpPLUSComplex::OpPLUSComplex(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}



void OpPLUSComplex::operator()(char *res, const char *op1, const char *op2)
{
    double op1Re = 0;
    double op2Re = 0;
    double op1Im = 0;
    double op2Im = 0;
    double resRe, resIm;

    op1Re = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
    op2Re = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));

    if (isNull(op1Re))
    {
        resRe = op1Re;
    }
    else if (isNull(op2Re))
    {
        resRe = op2Re;
    }
    else
    {
        resRe = op1Re + op2Re;
    }

    if (scalarFlag == BinaryOp::FIRST)
    {
        resIm = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));
    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        resIm = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
    }
    else
    {
        op1Im = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
        op2Im = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));
        if (isNull(op1Im))
        {
            resIm = op1Im;
        }
        else if (isNull(op2Im))
        {
            resIm = op2Im;
        }
        else
        {
            resIm = op1Im + op2Im;
        }
    }

    resType->makeFromCDouble(res + resOff + resReOff, &resRe);
    resType->makeFromCDouble(res + resOff + resImOff, &resIm);
}

void OpPLUSComplex::getCondenseInit(char *init)
{
    double dummyRe = 0.0;
    double dummyIm = 0.0;
    resType->makeFromCDouble(init + resReOff, &dummyRe);
    resType->makeFromCDouble(init + resImOff, &dummyIm);
}

//Cint plus
OpPLUSComplexInt::OpPLUSComplexInt(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void OpPLUSComplexInt::operator()(char *res, const char *op1, const char *op2)
{
    r_Long op1Re = 0;
    r_Long op2Re = 0;
    r_Long op1Im = 0;
    r_Long op2Im = 0;
    r_Long resRe, resIm;

    op1Re = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
    op2Re = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));

    if (isNull(op1Re))
    {
        resRe = op1Re;
    }
    else if (isNull(op2Re))
    {
        resRe = op2Re;
    }
    else
    {
        resRe = op1Re + op2Re;
    }

    if (scalarFlag == BinaryOp::FIRST)
    {
        resIm = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));
    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        resIm = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
    }
    else
    {
        op1Im = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
        op2Im = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));
        if (isNull(op1Im))
        {
            resIm = op1Im;
        }
        else if (isNull(op2Im))
        {
            resIm = op2Im;
        }
        else
        {
            resIm = op1Im + op2Im;
        }
    }

    resType->makeFromCLong(res + resOff + resReOff, &resRe);
    resType->makeFromCLong(res + resOff + resImOff, &resIm);
}

void OpPLUSComplexInt::getCondenseInit(char *init)
{
    r_Long dummyRe = 0;
    r_Long dummyIm = 0;
    resType->makeFromCLong(init + resReOff, &dummyRe);
    resType->makeFromCLong(init + resImOff, &dummyIm);
}
// *** MAX_BINARY ***

OpMAX_BINARYComplex::OpMAX_BINARYComplex(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void OpMAX_BINARYComplex::operator()(char *res, const char *op1, const char *op2)
{
    double op1Re = 0;
    double op2Re = 0;
    double op1Im = 0;
    double op2Im = 0;
    double resRe, resIm;

    if (scalarFlag == BinaryOp::FIRST)
    {
        if (*(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re)) > *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));
        }

        resIm = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));
    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        if (*(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re)) > *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));
        }

        resIm = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
    }
    else
    {
        if (*(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re)) > *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));
        }

        if (*(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im)) > *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im)))
        {
            resIm = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
        }
        else
        {
            resIm = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));
        }
    }

    resType->makeFromCDouble(res + resOff + resReOff, &resRe);
    resType->makeFromCDouble(res + resOff + resImOff, &resIm);
}

void OpMAX_BINARYComplex::getCondenseInit(char *init)
{
    double dummyRe = std::numeric_limits<double>::lowest();
    double dummyIm = std::numeric_limits<double>::lowest();
    resType->makeFromCDouble(init + resReOff, &dummyRe);
    resType->makeFromCDouble(init + resImOff, &dummyIm);
}

//Cint MAX_BINARY

OpMAX_BINARYComplexInt::OpMAX_BINARYComplexInt(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void OpMAX_BINARYComplexInt::operator()(char *res, const char *op1, const char *op2)
{
    r_Long op1Re = 0;
    r_Long op2Re = 0;
    r_Long op1Im = 0;
    r_Long op2Im = 0;
    r_Long resRe, resIm;

    if (scalarFlag == BinaryOp::FIRST)
    {
        if (*(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re)) > *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));
        }

        resIm = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));
    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        if (*(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re)) > *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));
        }

        resIm = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
    }
    else
    {
        if (*(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re)) > *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));
        }

        if (*(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im)) > *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im)))
        {
            resIm = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
        }
        else
        {
            resIm = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));
        }
    }

    resType->makeFromCLong(res + resOff + resReOff, &resRe);
    resType->makeFromCLong(res + resOff + resImOff, &resIm);
}

void OpMAX_BINARYComplexInt::getCondenseInit(char *init)
{
    r_Long dummyRe = std::numeric_limits<r_Long>::lowest();
    r_Long dummyIm = std::numeric_limits<r_Long>::lowest();
    resType->makeFromCLong(init + resReOff, &dummyRe);
    resType->makeFromCLong(init + resImOff, &dummyIm);
}

// *** MIN_BINARY ***

OpMIN_BINARYComplex::OpMIN_BINARYComplex(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void OpMIN_BINARYComplex::operator()(char *res, const char *op1, const char *op2)
{
    double op1Re = 0;
    double op2Re = 0;
    double op1Im = 0;
    double op2Im = 0;
    double resRe, resIm;

    if (scalarFlag == BinaryOp::FIRST)
    {
        if (*(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re)) < * (op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));
        }

        resIm = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));
    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        if (*(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re)) < * (op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));
        }

        resIm = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
    }
    else
    {
        if (*(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re)) < * (op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));
        }

        if (*(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im)) < * (op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im)))
        {
            resIm = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
        }
        else
        {
            resIm = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));
        }
    }

    resType->makeFromCDouble(res + resOff + resReOff, &resRe);
    resType->makeFromCDouble(res + resOff + resImOff, &resIm);
}

void OpMIN_BINARYComplex::getCondenseInit(char *init)
{
    double dummyRe = std::numeric_limits<double>::max();
    double dummyIm = std::numeric_limits<double>::max();
    resType->makeFromCDouble(init + resReOff, &dummyRe);
    resType->makeFromCDouble(init + resImOff, &dummyIm);
}

//Cint Minus Binary

OpMIN_BINARYComplexInt::OpMIN_BINARYComplexInt(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void OpMIN_BINARYComplexInt::operator()(char *res, const char *op1, const char *op2)
{
    r_Long op1Re = 0;
    r_Long op2Re = 0;
    r_Long op1Im = 0;
    r_Long op2Im = 0;
    r_Long resRe, resIm;

    if (scalarFlag == BinaryOp::FIRST)
    {
        if (*(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re)) < * (op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));
        }

        resIm = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));
    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        if (*(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re)) < * (op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));
        }

        resIm = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
    }
    else
    {
        if (*(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re)) < * (op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re)))
        {
            resRe = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        }
        else
        {
            resRe = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));
        }

        if (*(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im)) < * (op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im)))
        {
            resIm = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
        }
        else
        {
            resIm = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));
        }
    }

    resType->makeFromCLong(res + resOff + resReOff, &resRe);
    resType->makeFromCLong(res + resOff + resImOff, &resIm);
}

void OpMIN_BINARYComplexInt::getCondenseInit(char *init)
{
    r_Long dummyRe = std::numeric_limits<r_Long>::max();
    r_Long dummyIm = std::numeric_limits<r_Long>::max();
    resType->makeFromCLong(init + resReOff, &dummyRe);
    resType->makeFromCLong(init + resImOff, &dummyIm);
}
// *** MINUS ***


OpMINUSComplex::OpMINUSComplex(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void OpMINUSComplex::operator()(char *res, const char *op1, const char *op2)
{
    double op1Re = 0;
    double op2Re = 0;
    double op1Im = 0;
    double op2Im = 0;
    double resRe, resIm;

    op1Re = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
    op2Re = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));

    if (isNull(op1Re))
    {
        resRe = op1Re;
    }
    else if (isNull(op2Re))
    {
        resRe = op2Re;
    }
    else
    {
        resRe = op1Re - op2Re;
    }

    if (scalarFlag == BinaryOp::FIRST)
    {
        resIm = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));
    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        resIm = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
    }
    else
    {
        op1Im = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
        op2Im = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));
        if (isNull(op1Im))
        {
            resIm = op1Im;
        }
        else if (isNull(op2Im))
        {
            resIm = op2Im;
        }
        else
        {
            resIm = op1Im - op2Im;
        }
    }

    resType->makeFromCDouble(res + resOff + resReOff, &resRe);
    resType->makeFromCDouble(res + resOff + resImOff, &resIm);
}

// Cint MINUS ***


OpMINUSComplexInt::OpMINUSComplexInt(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void OpMINUSComplexInt::operator()(char *res, const char *op1, const char *op2)
{
    r_Long op1Re = 0;
    r_Long op2Re = 0;
    r_Long op1Im = 0;
    r_Long op2Im = 0;
    r_Long resRe, resIm;

    op1Re = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
    op2Re = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));

    if (isNull(op1Re))
    {
        resRe = op1Re;
    }
    else if (isNull(op2Re))
    {
        resRe = op2Re;
    }
    else
    {
        resRe = op1Re - op2Re;
    }

    if (scalarFlag == BinaryOp::FIRST)
    {
        resIm = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));
    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        resIm = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
    }
    else
    {
        op1Im = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
        op2Im = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));
        if (isNull(op1Im))
        {
            resIm = op1Im;
        }
        else if (isNull(op2Im))
        {
            resIm = op2Im;
        }
        else
        {
            resIm = op1Im - op2Im;
        }
    }

    resType->makeFromCLong(res + resOff + resReOff, &resRe);
    resType->makeFromCLong(res + resOff + resImOff, &resIm);
}
// *** DIV ***

OpDIVComplex::OpDIVComplex(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void OpDIVComplex::operator()(char *res, const char *op1, const char *op2)
{
    double op1Re = 0;
    double op2Re = 0;
    double op1Im = 0;
    double op2Im = 0;
    double resRe, resIm;

    if (scalarFlag == BinaryOp::FIRST)
    {
        double x1 = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        double x2 = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));
        double y2 = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));

        if (isNull(x1))
        {
            resRe = x1;
            resIm = 0;
        }
        else if (isNull(x2) || isNull(y2))
        {
            resRe = x2;
            resIm = y2;
        }
        else
        {
            // Do not handle division by zero, return +-nan or inf as specified in IEEE 754
            resRe = x1 * x2 / (x2 * x2 + y2 * y2);
            resIm = - x1 * y2 / (x2 * x2 + y2 * y2);
        }

    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        double x1 = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        double y1 = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
        double x2 = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));

        if (isNull(x1) || isNull(y1))
        {
            resRe = x1;
            resIm = y1;
        }
        else if (isNull(x2))
        {
            resRe = x2;
            resIm = 0;
        }
        else
        {
            // Do not handle division by zero, return +-nan or inf as specified in IEEE 754
            resRe = x1 / x2;
            resIm = y1 / x2;
        }

    }
    else   // BinaryOp::NONE
    {
        double x1 = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        double y1 = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
        double x2 = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));
        double y2 = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));

        if (isNull(x1) || isNull(y1))
        {
            resRe = x1;
            resIm = y1;
        }
        else if (isNull(x2) || isNull(y2))
        {
            resRe = x2;
            resIm = y2;
        }
        else
        {
            // Do not handle division by zero, return +-nan or inf as specified in IEEE 754
            resRe = (x1 * x2 + y1 * y2) / (x2 * x2 + y2 * y2);
            resIm = (y1 * x2 - x1 * y2) / (x2 * x2 + y2 * y2);
        }

    }

    resType->makeFromCDouble(res + resOff + resReOff, &resRe);
    resType->makeFromCDouble(res + resOff + resImOff, &resIm);
}

// Cint DIV ***

OpDIVComplexInt::OpDIVComplexInt(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void OpDIVComplexInt::operator()(char *res, const char *op1, const char *op2)
{
    r_Long op1Re = 0;
    r_Long op2Re = 0;
    r_Long op1Im = 0;
    r_Long op2Im = 0;
    r_Long resRe, resIm;

    if (scalarFlag == BinaryOp::FIRST)
    {
        r_Long x1 = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        r_Long x2 = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));
        r_Long y2 = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));

        if (isNull(x1))
        {
            resRe = x1;
            resIm = 0;
        }
        else if (isNull(x2) || isNull(y2))
        {
            resRe = x2;
            resIm = y2;
        }
        else
        {
            // Do not handle division by zero, return +-nan or inf as specified in IEEE 754
            resRe = x1 * x2 / (x2 * x2 + y2 * y2);
            resIm = - x1 * y2 / (x2 * x2 + y2 * y2);
        }

    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        r_Long x1 = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        r_Long y1 = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
        r_Long x2 = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));

        if (isNull(x1) || isNull(y1))
        {
            resRe = x1;
            resIm = y1;
        }
        else if (isNull(x2))
        {
            resRe = x2;
            resIm = 0;
        }
        else
        {
            // Do not handle division by zero, return +-nan or inf as specified in IEEE 754
            resRe = x1 / x2;
            resIm = y1 / x2;
        }

    }
    else   // BinaryOp::NONE
    {
        r_Long x1 = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        r_Long y1 = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
        r_Long x2 = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));
        r_Long y2 = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));

        if (isNull(x1) || isNull(y1))
        {
            resRe = x1;
            resIm = y1;
        }
        else if (isNull(x2) || isNull(y2))
        {
            resRe = x2;
            resIm = y2;
        }
        else
        {
            // Do not handle division by zero, return +-nan or inf as specified in IEEE 754
            resRe = (x1 * x2 + y1 * y2) / (x2 * x2 + y2 * y2);
            resIm = (y1 * x2 - x1 * y2) / (x2 * x2 + y2 * y2);
        }

    }

    resType->makeFromCLong(res + resOff + resReOff, &resRe);
    resType->makeFromCLong(res + resOff + resImOff, &resIm);
}
// *** MULT ***

OpMULTComplex::OpMULTComplex(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void OpMULTComplex::operator()(char *res, const char *op1, const char *op2)
{
    double op1Re = 0;
    double op2Re = 0;
    double op1Im = 0;
    double op2Im = 0;
    double resRe, resIm;


    if (scalarFlag == BinaryOp::FIRST)
    {
        double x1 = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        double x2 = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));
        double y2 = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));

        if (isNull(x1))
        {
            resRe = x1;
            resIm = 0;
        }
        else if (isNull(x2) || isNull(y2))
        {
            resRe = x2;
            resIm = y2;
        }
        else
        {
            resRe = x1 * x2;
            resIm = x1 * y2;
        }

    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        double x1 = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        double y1 = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
        double x2 = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));

        if (isNull(x1) || isNull(y1))
        {
            resRe = x1;
            resIm = y1;
        }
        else if (isNull(x2))
        {
            resRe = x2;
            resIm = 0;
        }
        else
        {
            resRe = x1 * x2;
            resIm = y1 * x2;
        }

    }
    else   // BinaryOp::NONE
    {
        double x1 = *(op1Type->convertToCDouble(op1 + op1Off + op1ReOff, &op1Re));
        double y1 = *(op1Type->convertToCDouble(op1 + op1Off + op1ImOff, &op1Im));
        double x2 = *(op2Type->convertToCDouble(op2 + op2Off + op2ReOff, &op2Re));
        double y2 = *(op2Type->convertToCDouble(op2 + op2Off + op2ImOff, &op2Im));

        if (isNull(x1) || isNull(y1))
        {
            resRe = x1;
            resIm = y1;
        }
        else if (isNull(x2) || isNull(y2))
        {
            resRe = x2;
            resIm = y2;
        }
        else
        {
            resRe = x1 * x2 - y1 * y2;
            resIm = x1 * y2 + x2 * y1;
        }

    }

    resType->makeFromCDouble(res + resOff + resReOff, &resRe);
    resType->makeFromCDouble(res + resOff + resImOff, &resIm);
}

void OpMULTComplex::getCondenseInit(char *init)
{
    double dummyRe = 1.0;
    double dummyIm = 0.0;
    resType->makeFromCDouble(init + resReOff, &dummyRe);
    resType->makeFromCDouble(init + resImOff, &dummyIm);
}

// Cint MULT ***

OpMULTComplexInt::OpMULTComplexInt(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off,
    BinaryOp::ScalarFlag flag)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), scalarFlag(flag)
{
    op1ReOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getReOffset();
    op1ImOff = scalarFlag == BinaryOp::FIRST  ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp1Type)))->getImOffset();
    op2ReOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getReOffset();
    op2ImOff = scalarFlag == BinaryOp::SECOND ? 0 : (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOp2Type)))->getImOffset();

    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void OpMULTComplexInt::operator()(char *res, const char *op1, const char *op2)
{
    r_Long op1Re = 0;
    r_Long op2Re = 0;
    r_Long op1Im = 0;
    r_Long op2Im = 0;
    r_Long resRe, resIm;


    if (scalarFlag == BinaryOp::FIRST)
    {
        r_Long x1 = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        r_Long x2 = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));
        r_Long y2 = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));

        if (isNull(x1))
        {
            resRe = x1;
            resIm = 0;
        }
        else if (isNull(x2) || isNull(y2))
        {
            resRe = x2;
            resIm = y2;
        }
        else
        {
            resRe = x1 * x2;
            resIm = x1 * y2;
        }

    }
    else if (scalarFlag == BinaryOp::SECOND)
    {
        r_Long x1 = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        r_Long y1 = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
        r_Long x2 = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));

        if (isNull(x1) || isNull(y1))
        {
            resRe = x1;
            resIm = y1;
        }
        else if (isNull(x2))
        {
            resRe = x2;
            resIm = 0;
        }
        else
        {
            resRe = x1 * x2;
            resIm = y1 * x2;
        }

    }
    else   // BinaryOp::NONE
    {
        r_Long x1 = *(op1Type->convertToCLong(op1 + op1Off + op1ReOff, &op1Re));
        r_Long y1 = *(op1Type->convertToCLong(op1 + op1Off + op1ImOff, &op1Im));
        r_Long x2 = *(op2Type->convertToCLong(op2 + op2Off + op2ReOff, &op2Re));
        r_Long y2 = *(op2Type->convertToCLong(op2 + op2Off + op2ImOff, &op2Im));

        if (isNull(x1) || isNull(y1))
        {
            resRe = x1;
            resIm = y1;
        }
        else if (isNull(x2) || isNull(y2))
        {
            resRe = x2;
            resIm = y2;
        }
        else
        {
            resRe = x1 * x2 - y1 * y2;
            resIm = x1 * y2 + x2 * y1;
        }

    }

    resType->makeFromCLong(res + resOff + resReOff, &resRe);
    resType->makeFromCLong(res + resOff + resImOff, &resIm);
}

void OpMULTComplexInt::getCondenseInit(char *init)
{
    r_Long dummyRe = 1;
    r_Long dummyIm = 0;
    resType->makeFromCLong(init + resReOff, &dummyRe);
    resType->makeFromCLong(init + resImOff, &dummyIm);
}
// *** IDENTITY ***

OpIDENTITYComplex::OpIDENTITYComplex(
    const BaseType *newResType,
    const BaseType *newOpType,
    size_t newResOff,
    size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{}

void OpIDENTITYComplex::operator()(char *res, const char *op)
{
    memcpy((void *)(res + resOff), (void *)(const_cast<char *>(op) + opOff), resType->getSize());
}


OpUpdateComplex::OpUpdateComplex(
    const BaseType *newResType,
    const BaseType *newOpType,
    size_t newResOff,
    size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{}

void OpUpdateComplex::operator()(char *res, const char *op)
{
    memcpy((void *)(res + resOff), (void *)(const_cast<char *>(op) + opOff), resType->getSize());
}

// *** MAX ***

OpMAXComplex::OpMAXComplex(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
    , maxBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
    const GenericComplexType *type = (const GenericComplexType *) resType;
    size_t reOff = type->getReOffset();
    size_t imOff = type->getImOffset();

    double myVal = std::numeric_limits<double>::lowest();
    accu = new char[resType->getSize()];
    memset(accu, 0, resType->getSize());
    resType->makeFromCDouble(accu + reOff, &myVal);
    resType->makeFromCDouble(accu + imOff, &myVal);
}

OpMAXComplex::OpMAXComplex(const BaseType *newResType, char *newAccu,
                           const BaseType *newOpType, size_t newResOff,
                           size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
    , maxBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
}

char *
OpMAXComplex::operator()(const char *op, char *init)
{
    maxBinary(init, op, init);
    return init;
}

char *
OpMAXComplex::operator()(const char *op)
{
    return OpMAXComplex::operator()(op, accu);
}

// Cint MAX ***

OpMAXComplexInt::OpMAXComplexInt(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
    , maxBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
    const GenericComplexType *type = (const GenericComplexType *) resType;
    size_t reOff = type->getReOffset();
    size_t imOff = type->getImOffset();

    r_Long myVal = std::numeric_limits<r_Long>::lowest();
    accu = new char[resType->getSize()];
    memset(accu, 0, resType->getSize());
    resType->makeFromCLong(accu + reOff, &myVal);
    resType->makeFromCLong(accu + imOff, &myVal);
}

OpMAXComplexInt::OpMAXComplexInt(const BaseType *newResType, char *newAccu,
                           const BaseType *newOpType, size_t newResOff,
                           size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
    , maxBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
}

char *
OpMAXComplexInt::operator()(const char *op, char *init)
{
    maxBinary(init, op, init);
    return init;
}

char *
OpMAXComplexInt::operator()(const char *op)
{
    return OpMAXComplexInt::operator()(op, accu);
}

// *** MIN ***

OpMINComplex::OpMINComplex(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
    , minBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
    const GenericComplexType *type = (const GenericComplexType *) resType;
    size_t reOff = type->getReOffset();
    size_t imOff = type->getImOffset();

    double myVal = std::numeric_limits<double>::max();
    accu = new char[resType->getSize()];
    memset(accu, 0, resType->getSize());
    resType->makeFromCDouble(accu + reOff, &myVal);
    resType->makeFromCDouble(accu + imOff, &myVal);
}

OpMINComplex::OpMINComplex(const BaseType *newResType, char *newAccu,
                           const BaseType *newOpType, size_t newResOff,
                           size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
    , minBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
}

char *
OpMINComplex::operator()(const char *op, char *init)
{
    minBinary(init, op, init);
    return init;
}

char *
OpMINComplex::operator()(const char *op)
{
    return OpMINComplex::operator()(op, accu);
}

// Cint MIN ***

OpMINComplexInt::OpMINComplexInt(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
    , minBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
    const GenericComplexType *type = (const GenericComplexType *) resType;
    size_t reOff = type->getReOffset();
    size_t imOff = type->getImOffset();

    r_Long myVal = std::numeric_limits<r_Long>::max();
    accu = new char[resType->getSize()];
    memset(accu, 0, resType->getSize());
    resType->makeFromCLong(accu + reOff, &myVal);
    resType->makeFromCLong(accu + imOff, &myVal);
}

OpMINComplexInt::OpMINComplexInt(const BaseType *newResType, char *newAccu,
                           const BaseType *newOpType, size_t newResOff,
                           size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
    , minBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
}

char *
OpMINComplexInt::operator()(const char *op, char *init)
{
    minBinary(init, op, init);
    return init;
}

char *
OpMINComplexInt::operator()(const char *op)
{
    return OpMINComplexInt::operator()(op, accu);
}
// *** SUM ***

OpSUMComplex::OpSUMComplex(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
    , plusBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
    const GenericComplexType *type = (const GenericComplexType *) resType;
    size_t resReOff = type->getReOffset();
    size_t resImOff = type->getImOffset();

    double myVal = 0.0;
    // initialising with neutral value
    accu = new char[resType->getSize()];
    resType->makeFromCDouble(accu + resReOff, &myVal);
    resType->makeFromCDouble(accu + resImOff, &myVal);
}

OpSUMComplex::OpSUMComplex(const BaseType *newResType, char *newAccu,
                           const BaseType *newOpType, size_t newResOff,
                           size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
    , plusBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
}

char *
OpSUMComplex::operator()(const char *op, char *init)
{
    plusBinary(init, op, init);
    return init;
}

char *
OpSUMComplex::operator()(const char *op)
{
    return OpSUMComplex::operator()(op, accu);
}


// Cint SUM ***

OpSUMComplexInt::OpSUMComplexInt(const BaseType *newResType, const BaseType *newOpType,
                           size_t newResOff, size_t newOpOff)
    : CondenseOp(newResType, newOpType, newResOff, newOpOff)
    , plusBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
    const GenericComplexType *type = (const GenericComplexType *) resType;
    size_t resReOff = type->getReOffset();
    size_t resImOff = type->getImOffset();

    r_Long myVal = 0.0;
    // initialising with neutral value
    accu = new char[resType->getSize()];
    resType->makeFromCLong(accu + resReOff, &myVal);
    resType->makeFromCLong(accu + resImOff, &myVal);
}

OpSUMComplexInt::OpSUMComplexInt(const BaseType *newResType, char *newAccu,
                           const BaseType *newOpType, size_t newResOff,
                           size_t newOpOff)
    : CondenseOp(newResType, newAccu, newOpType, newResOff, newOpOff)
    , plusBinary(newResType, newOpType, newResType, newResOff, newOpOff, newResOff, BinaryOp::NONE)
{
}

char *
OpSUMComplexInt::operator()(const char *op, char *init)
{
    plusBinary(init, op, init);
    return init;
}

char *
OpSUMComplexInt::operator()(const char *op)
{
    return OpSUMComplexInt::operator()(op, accu);
}
// *** CONSTRUCTING COMPLEX ***

OpConstructComplex::OpConstructComplex(const BaseType *newResType, const BaseType *newOp1Type,
                                       const BaseType *newOp2Type, size_t newResOff, size_t newOp1Off,
                                       size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off)
{
    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void
OpConstructComplex::operator()(char *res, const char *op1, const char *op2)
{
    double resRe = *(op1Type->convertToCDouble(op1 + op1Off, &resRe));
    double resIm = *(op2Type->convertToCDouble(op2 + op2Off, &resIm));

    resType->makeFromCDouble(res + resOff + resReOff, &resRe);
    resType->makeFromCDouble(res + resOff + resImOff, &resIm);
}

// Cint CONSTRUCTING COMPLEX ***

OpConstructComplexInt::OpConstructComplexInt(const BaseType *newResType, const BaseType *newOp1Type,
                                       const BaseType *newOp2Type, size_t newResOff, size_t newOp1Off,
                                       size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off)
{
    resReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getReOffset();
    resImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newResType)))->getImOffset();
}

void
OpConstructComplexInt::operator()(char *res, const char *op1, const char *op2)
{
    r_Long resRe = *(op1Type->convertToCLong(op1 + op1Off, &resRe));
    r_Long resIm = *(op2Type->convertToCLong(op2 + op2Off, &resIm));

    resType->makeFromCLong(res + resOff + resReOff, &resRe);
    resType->makeFromCLong(res + resOff + resImOff, &resIm);
}
// *** REAL PART ***

OpRealPart::OpRealPart(
    const BaseType *newResType,
    const BaseType *newOpType,
    size_t newResOff,
    size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
    opReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOpType)))->getReOffset();
}


void OpRealPart::operator()(char *res, const char *op)
{
    double result;

    opType->convertToCDouble(op + opOff + opReOff, &result);
    resType->makeFromCDouble(res + resOff, &result);
}


// Cint REAL PART ***

OpRealPartInt::OpRealPartInt(
    const BaseType *newResType,
    const BaseType *newOpType,
    size_t newResOff,
    size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
    opReOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOpType)))->getReOffset();
}


void OpRealPartInt::operator()(char *res, const char *op)
{
    r_Long result;

    opType->convertToCLong(op + opOff + opReOff, &result);
    resType->makeFromCLong(res + resOff, &result);
}
// *** IMAGINAR PART ***

OpImaginarPart::OpImaginarPart(
    const BaseType *newResType,
    const BaseType *newOpType,
    size_t newResOff,
    size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
    opImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOpType)))->getImOffset();
}


void OpImaginarPart::operator()(char *res, const char *op)
{
    double result;

    opType->convertToCDouble(op + opOff + opImOff, &result);
    resType->makeFromCDouble(res + resOff, &result);
}


// Cint IMAGINAR PART ***

OpImaginarPartInt::OpImaginarPartInt(
    const BaseType *newResType,
    const BaseType *newOpType,
    size_t newResOff,
    size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff)
{
    opImOff = (static_cast<GenericComplexType *>(const_cast<BaseType *>(newOpType)))->getImOffset();
}


void OpImaginarPartInt::operator()(char *res, const char *op)
{
    r_Long result;

    opType->convertToCLong(op + opOff + opImOff, &result);
    resType->makeFromCLong(res + resOff, &result);
}

//--------------------------------------------
//  OpCAST
//--------------------------------------------

OpCAST::OpCAST(
    const BaseType *newResType,
    const BaseType *newOpType,
    size_t newResOff,
    size_t newOpOff)
    : UnaryOp(newResType, newOpType, newResOff, newOpOff) {}

void OpCAST::operator()(char *res, const char *op)
{

    if (resType->getType() == FLOAT || resType->getType() == DOUBLE)
    {
        // floating point types
        double dblOp = *(opType->convertToCDouble(op + opOff, &dblOp));
        if (isNull(dblOp))
        {
            dblOp = NAN;
        }
        resType->makeFromCDouble(res + resOff, &dblOp);
    }
    else
    {
        // all integral types
        r_Long lngOp = *(opType->convertToCLong(op + opOff, &lngOp));
        if (isNull(lngOp))
        {
            lngOp = 0;
        }
        resType->makeFromCLong(res + resOff, &lngOp);
    }
}


//--------------------------------------------
//  OpOVERLAY
//--------------------------------------------

OpOVERLAY::OpOVERLAY(const BaseType *newResType, const BaseType *newOp1Type, const BaseType *newOp2Type, size_t typeSize, const char *transparentPattern, size_t newResOff, size_t newOp1Off, size_t newOp2Off)
    : BinaryOp(newResType, newOp1Type, newOp2Type, newResOff, newOp1Off, newOp2Off), length(typeSize), pattern(transparentPattern)
{
    if ((pattern == nullPattern) && (length > 16))
    {
        LERROR << "OpOVERLAY overlay with types larger than 16 bytes not supported yet";
        throw r_Error(OVERLAYPATTERNTOOSMALL);
    }
}

void OpOVERLAY::operator()(char *res, const char *op1, const char *op2)
{
    if ((memcmp(pattern, op1 + op1Off, length) == 0) || isNull(*(op1 + op1Off)))
    {
        //match
        memcpy(res + resOff, op2 + op2Off, length);
    }
    else     //no match
    {
        memcpy(res + resOff, op1 + op1Off, length);
    }
}

const char *
OpOVERLAY::nullPattern = "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0";

//--------------------------------------------
//  OpBIT
//--------------------------------------------

OpBIT::OpBIT(
    const BaseType *newResType,
    const BaseType *newOp1Type,
    const BaseType *newOp2Type,
    size_t newResOff,
    size_t newOp1Off,
    size_t newOp2Off)

    : BinaryOp(newResType,
               newOp1Type, newOp2Type,
               newResOff, newOp1Off,
               newOp2Off)
{}


void OpBIT::operator()(char *res, const char *op1, const char *op2)
{
    r_ULong lngOp1, lngOp2, lngRes;

    op1Type->convertToCULong(op1 + op1Off, &lngOp1);
    op2Type->convertToCULong(op2 + op2Off, &lngOp2);
    if (isNull(lngOp1))
    {
        lngRes = lngOp1;
    }
    else
    {
        lngRes = lngOp1 >> lngOp2 & 0x1L;
    }
    resType->makeFromCULong(res + resOff, &lngRes);
}




#include "autogen_ops.cc"
