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
/*************************************************************
 *
 *
 * PURPOSE:
 *   Ops contains an enum for identifying all possible
 *   operations.
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef _OPS_HH_
#define _OPS_HH_

#include "nullvalues.hh"
#include "typeenum.hh"

class CondenseOp;
class UnaryOp;
class BinaryOp;
class BaseType;
class StructType;
class r_Point;

//@ManMemo: Module: {\bf catalogif}.

/*@Doc:
  The class Ops is contains an enumeration type giving symbolic names
  to all implemented operations. These names are given as parameters
  to functions concerning operations in \Ref{Tile} and
  \Ref{BaseType}. The selection of operations is actually done in
  functions of this class, called by the classes mentioned above. The
  operations are implemented in subclasses of \Ref{CondenseOp},
  \Ref{UnaryOp} and \Ref{BinaryOp}.

  The operations in the following table are defined at the moment.
  They can be used in expressions like `Ops::OP_EQUAL`.

  \begin{tabular}{cl}

  symbolic name && operation \\

  && {\bf condense operations} \\
  OP_SOME && condense boolean tile with OR \\
  OP_ALL  && condense boolean tile with AND \\

  && {\bf unary operations} \\
  OP_NOT  && negation (bitwise for ints, logical for bools) \\
  OP_IS_NULL  && is null check (result Bool) \\
  OP_SQRT  && square root (for doubles) \\
  OP_IDENTITY && used for copying cells \\
  OP_UPDATE && used for updating cells, takes into account null values \\

  && {\bf binary operations} \\
  OP_MINUS && subtraction \\
  OP_PLUS && addition \\
  OP_MAX_BINARY && binary max\\
  OP_MIN_BINARY && binary min\\
  OP_MULT && multiplication \\
  OP_DIV && division \\
  OP_INTDIV && integer division \\
  OP_MOD && modulo \\
  OP_IS && not implemented yet \\
  OP_AND && bitwise resp. logical AND \\
  OP_OR && bitwise resp. logical OR \\
  OP_XOR && bitwise resp. logical XOR \\
  OP_EQUAL && equality (result Bool) \\
  OP_LESS && less than (result Bool) \\
  OP_LESSEQUAL && less than or equal (result Bool) \\
  OP_NOTEQUAL && inequality (result Bool) \\
  OP_GREATER && greater than (result Bool) \\
  OP_GREATEREQUAL && greater than or equal (result Bool) \\

  \end{tabular}
*/
/**
  * \ingroup Catalogmgrs
  */
class Ops : public NullValuesHandler
{
public:
    enum OpType
    {
        // Important: do not change order as it is relevant in ops.cc

        // UNARY
        OP_COUNT,  // condensers
        OP_MAX,
        OP_MIN,
        OP_SUM,
        OP_SQSUM,
        OP_SOME,
        OP_ALL,
        OP_UFUNC_BEGIN,  // unary arithmetic functions
        OP_ABS,
        OP_SQRT,
        OP_POW,
        OP_EXP,
        OP_LOG,
        OP_LN,
        OP_SIN,
        OP_COS,
        OP_TAN,
        OP_SINH,
        OP_COSH,
        OP_TANH,
        OP_ARCSIN,
        OP_ARCCOS,
        OP_ARCTAN,
        OP_UFUNC_END,
        OP_REALPART,  // complex part extraction
        OP_IMAGINARPART,
        OP_REALPARTINT,
        OP_IMAGINARPARTINT,
        OP_CAST_BEGIN,  // cast to new type
        OP_CAST_BOOL,
        OP_CAST_CHAR,
        OP_CAST_OCTET,
        OP_CAST_SHORT,
        OP_CAST_USHORT,
        OP_CAST_LONG,
        OP_CAST_ULONG,
        OP_CAST_FLOAT,
        OP_CAST_DOUBLE,
        OP_CAST_GENERAL,
        OP_CAST_END,
        OP_NOT,  // logical
        OP_IS_NULL,
        OP_IDENTITY,  // copy
        OP_UPDATE,

        // BINARY
        OP_MINUS,  // arithmetic
        OP_PLUS,
        OP_MULT,
        OP_INTDIV,
        OP_DIV,
        OP_MOD,
        OP_MAX_BINARY,  // internal
        OP_MIN_BINARY,
        OP_OVERLAY,
        OP_IS,  // logical
        OP_AND,
        OP_OR,
        OP_XOR,
        OP_BIT,
        OP_CONSTRUCT_COMPLEX,
        OP_EQUAL,  // comparison
        OP_NOTEQUAL,
        OP_LESS,
        OP_LESSEQUAL,
        OP_GREATER,
        OP_GREATEREQUAL
    };

    //@Man: methods for getting functions
    //@{
    /// get function object for unary operation.
    static UnaryOp *getUnaryOp(Ops::OpType op, const BaseType *restype,
                               const BaseType *optype, size_t resOff = 0,
                               size_t opOff = 0);
    /*@Doc:
      An \Ref{UnaryOp} carrying out `op` on the given types is
      returned. If `op` is not applicable to the given types,
      0 is returned.
    */
    /// get function object for binary operation.
    static BinaryOp *getBinaryOp(Ops::OpType op, const BaseType *resType,
                                 const BaseType *op1Type, const BaseType *op2Type,
                                 size_t resOff = 0,
                                 size_t op1Off = 0,
                                 size_t op2Off = 0, bool nullAsIdentity = false);
    /*@Doc:
      An \Ref{BinaryOp} carrying out `op` on the given types is
      returned. If `op` is not applicable to the given types,
      0 is returned.
    */
    static CondenseOp *getCondenseOp(Ops::OpType op, const BaseType *resType,
                                     const BaseType *opType = 0,
                                     size_t resOff = 0,
                                     size_t opOff = 0);
    /// get function object for condense operation.
    static CondenseOp *getCondenseOp(Ops::OpType op, const BaseType *resType,
                                     char *newAccu, const BaseType *opType = 0,
                                     size_t resOff = 0,
                                     size_t opOff = 0);
    /*@Doc:
      An \Ref{CondenseOp} carrying out `op` on the given types is
      returned. If `op` is not applicable to the given types,
      0 is returned.
    */
    //@}

    //@Man: methods for checking applicability of functions.
    //@{
    /// checks, if `op` is applicable on the given types.
    static int isApplicable(Ops::OpType op, const BaseType *op1Type,
                            const BaseType *op2Type = 0);
    /*@Doc:
      For unary or condense operations, just leave out #op2Type# (or
      set it to 0).
    */
    /// gives back suggested return type for `op` carried out on the given types.
    static const BaseType *getResultType(Ops::OpType op, const BaseType *op1,
                                         const BaseType *op2 = 0);
    /*@Doc:
      This usually gives back the "stronger" type of #op1Type# or #op2Type#
      (e.g. for a function like OP_PLUS). Usually the operation can also
      be applied to another type, loosing information if the type is
      "weaker". At the moment, only comparison operations (e.g. OP_EQUAL)
      have a well defined return type, which is Bool. No other return type
      can be used for these operations. If the operation is not applicable
      to the given type, 0 is returned.
    */

    /// get a struct result type if applicable, NULL otherwise
    static const BaseType *getStructResultType(Ops::OpType op, const BaseType *op1, const BaseType *op2);

    /// executes operation on a constant.
    static void execUnaryConstOp(Ops::OpType op, const BaseType *resType,
                                 const BaseType *opType, char *res,
                                 const char *op1, size_t resOff = 0,
                                 size_t opOff = 0, double param = 0);
    /// executes operation on two constants.
    static void execBinaryConstOp(Ops::OpType op, const BaseType *resType,
                                  const BaseType *op1Type,
                                  const BaseType *op2Type, char *res,
                                  const char *op1, const char *op2,
                                  size_t resOff = 0,
                                  size_t op1Off = 0,
                                  size_t op2Off = 0);
    //@}

private:
    /// checks, if `op` is applicable on two struct of type opType.
    static int isApplicableOnStruct(Ops::OpType op, const BaseType *opType);
    /*@ManMemo: checks, if `op` is applicable on struct of type op1Type
                and value of type op2Type.*/
    static int isApplicableOnStructConst(Ops::OpType op,
                                         const BaseType *op1Type,
                                         const BaseType *op2Type);
    // these functions aren't even used for the time being, but may
    // be important for better implementations of isApplicable and
    // getResultType.
    static bool isCondenseOp(Ops::OpType op);
    static bool isUnaryOp(Ops::OpType op);
    static bool isBinaryOp(Ops::OpType op);
};

//@ManMemo: Module: {\bf catalogif}.

/*@Doc:
  CondenseOp is the superclass for all condense operations. The
  operator() carries out a condense operation on one cell `op`,
  which is accumulated into `accu`. `accu` is returned as a
  result. Remember to always initialize `accu` correctly according
  to the condense operation used (e.g. 0 for \Ref{OpSOMEBool} or 1 for
  \Ref{OpALLBool}).
*/
/**
  * \ingroup Catalogmgrs
  */
class CondenseOp : public NullValuesHandler
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    CondenseOp(const BaseType *newResType, const BaseType *newOpType,
               size_t newResOff = 0, size_t newOpOff = 0);
    /*@ManMemo: constructor gets RasDaMan base type of result and operand,
      initial value, and offsets to result and operand (for structs) . */
    CondenseOp(const BaseType *newResType, char *newAccu, const BaseType *newOpType,
               size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu) = 0;
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op) = 0;
    /// operator to access value of internal accumulator.
    virtual char *getAccuVal();
    /*@ManMemo: virtual destructor because subclasse OpCondenseStruct has
                non-trivial destructor. */
    virtual ~CondenseOp();

protected:
    char *accu;
    const BaseType *opType;
    const BaseType *resType;
    size_t resOff;
    size_t opOff;
    bool initialized;
    bool nullAccu;
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_SOME on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpSOMECChar : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpSOMECChar(const BaseType *newResType, const BaseType *newOpType,
                size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpSOMECChar(const BaseType *newResType, char *newAccu,
                const BaseType *newOpType, size_t newResOff,
                size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_ALL on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpALLCChar : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpALLCChar(const BaseType *newResType, const BaseType *newOpType,
               size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpALLCChar(const BaseType *newResType, char *newAccu,
               const BaseType *newOpType, size_t newResOff,
               size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_COUNT on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpCOUNTCChar : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpCOUNTCChar(const BaseType *newResType, const BaseType *newOpType,
                 size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpCOUNTCChar(const BaseType *newResType, char *newAccu,
                 const BaseType *newOpType, size_t newResOff,
                 size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MAX on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpMAXCULong : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpMAXCULong(const BaseType *newResType, const BaseType *newOpType,
                size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpMAXCULong(const BaseType *newResType, char *newAccu,
                const BaseType *newOpType, size_t newResOff,
                size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MAX on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpMAXCLong : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpMAXCLong(const BaseType *newResType, const BaseType *newOpType,
               size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpMAXCLong(const BaseType *newResType, char *newAccu,
               const BaseType *newOpType, size_t newResOff,
               size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MAX on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpMAXCDouble : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpMAXCDouble(const BaseType *newResType, const BaseType *newOpType,
                 size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpMAXCDouble(const BaseType *newResType, char *newAccu,
                 const BaseType *newOpType, size_t newResOff,
                 size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MIN on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpMINCULong : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpMINCULong(const BaseType *newResType, const BaseType *newOpType,
                size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpMINCULong(const BaseType *newResType, char *newAccu,
                const BaseType *newOpType, size_t newResOff,
                size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MIN on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpMINCLong : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpMINCLong(const BaseType *newResType, const BaseType *newOpType,
               size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpMINCLong(const BaseType *newResType, char *newAccu,
               const BaseType *newOpType, size_t newResOff,
               size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MIN on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpMINCDouble : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpMINCDouble(const BaseType *newResType, const BaseType *newOpType,
                 size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpMINCDouble(const BaseType *newResType, char *newAccu,
                 const BaseType *newOpType, size_t newResOff,
                 size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_SUM on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpSUMCULong : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpSUMCULong(const BaseType *newResType, const BaseType *newOpType,
                size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpSUMCULong(const BaseType *newResType, char *newAccu,
                const BaseType *newOpType, size_t newResOff,
                size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_SUM on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpSUMCLong : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpSUMCLong(const BaseType *newResType, const BaseType *newOpType,
               size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpSUMCLong(const BaseType *newResType, char *newAccu,
               const BaseType *newOpType, size_t newResOff,
               size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_SUM on C type #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpSUMCDouble : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpSUMCDouble(const BaseType *newResType, const BaseType *newOpType,
                 size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpSUMCDouble(const BaseType *newResType, char *newAccu,
                 const BaseType *newOpType, size_t newResOff,
                 size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_SUM on type #double#.
/**
  * \ingroup Catalogmgrs
  */
class OpSQSUMCDouble : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpSQSUMCDouble(const BaseType *newResType, const BaseType *newOpType,
                   size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpSQSUMCDouble(const BaseType *newResType, char *newAccu,
                   const BaseType *newOpType, size_t newResOff,
                   size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: Class for carrying out condense operations on structs.

// Inherits some useless members from CondenseOp, don't want to
// change this now.
/**
  * \ingroup Catalogmgrs
  */
class OpCondenseStruct : public CondenseOp
{
public:
    /// constructor gets struct type.
    OpCondenseStruct(
        const BaseType *newResType,
        const BaseType *newOpType,
        Ops::OpType op,
        size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor gets struct type and initial value for internal accu.
    OpCondenseStruct(
        const BaseType *newResType,
        char *newAccu,
        const BaseType *newOpType,
        Ops::OpType op,
        size_t newResOff,
        size_t newOpOff);
    /// destructor.
    virtual ~OpCondenseStruct();
    /// operator to carry out operation on struct `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on struct `op` using internal accu.
    virtual char *operator()(const char *op);

protected:
    StructType *myResType;
    StructType *myOpType;
    size_t numElems;
    // array of operations on the elements.
    CondenseOp **elemOps;
};

//@ManMemo: Module: {\bf catalogif}.

/*@Doc:
  UnaryOp is the superclass for all unary operations. The
  operator() carries out a unary operation on one cell `op` and
  stores the result in the cell `result`.
*/
/**
  * \ingroup Catalogmgrs
  */
class UnaryOp : public NullValuesHandler
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    UnaryOp(const BaseType *newResType, const BaseType *newOpType,
            size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op`.
    virtual void operator()(char *result, const char *op) = 0;

    /*@ManMemo: virtual destructor because subclasse OpUnaryStruct has
                non-trivial destructor. */
    virtual ~UnaryOp(){};

protected:
    const BaseType *opType;
    const BaseType *resType;
    size_t resOff;
    size_t opOff;
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: Class for carrying out binary operations on structs.

// Inherits some useless members from UnaryOp, don't want to
// change this now.
/**
  * \ingroup Catalogmgrs
  */
class OpUnaryStruct : public UnaryOp
{
public:
    /// constructor gets struct type.
    OpUnaryStruct(
        const BaseType *newResType,
        const BaseType *newOpType,
        Ops::OpType op,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    /// destructor.
    virtual ~OpUnaryStruct();
    /// operator to carry out operation on struct `op`.
    virtual void operator()(char *result, const char *op);
    void setExponent(double newExponent);

protected:
    StructType *myResType;
    StructType *myOpType;
    size_t numElems;
    // array of operations on the elements.
    UnaryOp **elemOps;
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_IDENTITY on structs. Works, if struct types are identical.
/**
  * \ingroup Catalogmgrs
  */
class OpIDENTITYStruct : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpIDENTITYStruct(const BaseType *newResType, const BaseType *newOpType,
                     size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    void operator()(char *result, const char *op) override;
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_UPDATE on structs. Works, if struct types are identical.
/**
  * \ingroup Catalogmgrs
  */
class OpUpdateStruct : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateStruct(const BaseType *newResType, const BaseType *newOpType,
                   size_t newResOff = 0, size_t newOpOff = 0);
    ~OpUpdateStruct() override;
    /// operator to carry out operation on `op` with result `result`.
    void operator()(char *result, const char *op) override;
    void setNullValues(r_Nullvalues *newNullValues) override;

protected:
    size_t numElems;
    UnaryOp **assignmentOps;
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_NOT on C type #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpNOTCULong : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpNOTCULong(const BaseType *newResType, const BaseType *newOpType,
                size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_IDENTITY on C type #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpIDENTITYCULong : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpIDENTITYCULong(const BaseType *newResType, const BaseType *newOpType,
                     size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_UPDATE on C type #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpUpdateCULong : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateCULong(const BaseType *newResType, const BaseType *newOpType,
                   size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    void operator()(char *result, const char *op) override;
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_NOT on C type #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpNOTCLong : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpNOTCLong(const BaseType *newResType, const BaseType *newOpType,
               size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_NOT on Bools (logical NOT as opposed to bitwise NOT).
/**
  * \ingroup Catalogmgrs
  */
class OpNOTBool : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpNOTBool(const BaseType *newResType, const BaseType *newOpType,
              size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_IDENTITY on C type #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpIDENTITYCLong : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpIDENTITYCLong(const BaseType *newResType, const BaseType *newOpType,
                    size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_UPDATE on C type #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpUpdateCLong : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateCLong(const BaseType *newResType, const BaseType *newOpType,
                  size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    void operator()(char *result, const char *op) override;
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_IDENTITY on C type #double#, result #double#.
/**
  * \ingroup Catalogmgrs
  */
class OpIDENTITYCDouble : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpIDENTITYCDouble(const BaseType *newResType, const BaseType *newOpType,
                      size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_UPDATE on C type #double#, result #double#.
/**
  * \ingroup Catalogmgrs
  */
class OpUpdateCDouble : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateCDouble(const BaseType *newResType, const BaseType *newOpType,
                    size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

class OpUpdateCFloat : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateCFloat(const BaseType *newResType, const BaseType *newOpType,
                   size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
/*@Doc:
  BinaryOp is the superclass for all binary operations. The operator()
  carries out a binary operation on cells `op1` and `op2`. The
  result is stored in the cell `res`.
*/
/**
  * \ingroup Catalogmgrs
  */
class BinaryOp : public NullValuesHandler
{
public:
    // Question: which operand is scalar?
    // Answer: NONE, FIRST, SECOND
    enum ScalarFlag
    {
        NONE,
        FIRST,
        SECOND
    };

    /*@ManMemo: constructor gets RasDaMan base type of result and operands
                and offsets to result and operands (for structs). */
    BinaryOp(const BaseType *newResType, const BaseType *newOp1Type,
             const BaseType *newOp2Type, size_t newResOff = 0,
             size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2) = 0;
    /// returns initialization value for {\ref GenCondenseOp}.
    virtual void getCondenseInit(char *init);
    /*@ManMemo: virtual destructor because subclass OpBinaryStruct has
                non-trivial destructor. */
    virtual ~BinaryOp() = default;

protected:
    const BaseType *op1Type;
    const BaseType *op2Type;
    const BaseType *resType;
    size_t resOff;
    size_t op1Off;
    size_t op2Off;

    bool treatNullAsIdentity;
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: Class for carrying out binary operations on structs.

// Inherits some useless members from BinaryOp, don't want to
// change this now.
/**
  * \ingroup Catalogmgrs
  */
class OpBinaryStruct : public BinaryOp
{
public:
    /// constructor gets struct type.
    OpBinaryStruct(const BaseType *newStructType, Ops::OpType op,
                   const BaseType *op1typeArg, const BaseType *op2typeArg,
                   size_t newResOff = 0, size_t newOp1Off = 0,
                   size_t newOp2Off = 0);
    /// destructor.
    virtual ~OpBinaryStruct();
    /// operator to carry out operation on struct `op`.
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);

protected:
    StructType *myStructType;
    size_t numElems;
    // array of operations on the elements.
    BinaryOp **elemOps;
    BinaryOp **equalOps;
    BinaryOp **lessOps;
    UnaryOp **assignmentOps;
    char boolRes[1];
};

//@ManMemo: Module: {\bf catalogif}.
/*@Doc: Class for carrying out binary operations on structs where the
        second operand is a value. */
/**
  * \ingroup Catalogmgrs
  */
class OpBinaryStructConst : public BinaryOp
{
public:
    /// constructor gets struct type.
    OpBinaryStructConst(
        const BaseType *resType,
        const BaseType *op1Type,
        const BaseType *op2Type,
        Ops::OpType op, size_t newResOff = 0,
        size_t newOp1Off = 0,
        size_t newOp2Off = 0);
    /// destructor.
    virtual ~OpBinaryStructConst();
    /// operator to carry out operation on struct `op`.
    virtual void operator()(char *res, const char *op1,
                            const char *op2);

protected:
    StructType *resStructType;
    StructType *opStructType;
    size_t numElems;
    // array of operations on the elements.
    BinaryOp **elemOps;
};

//@ManMemo: Module: {\bf catalogif}.
/*@Doc: Class for carrying out binary operations on structs where the
        first operand is a value. */
/**
  * \ingroup Catalogmgrs
  */
class OpBinaryConstStruct : public BinaryOp
{
public:
    /// constructor gets struct type.
    OpBinaryConstStruct(
        const BaseType *resType,
        const BaseType *op1Type,
        const BaseType *op2Type,
        Ops::OpType op, size_t newResOff = 0,
        size_t newOp1Off = 0,
        size_t newOp2Off = 0);
    /// destructor.
    virtual ~OpBinaryConstStruct();
    /// operator to carry out operation on struct `op`.
    virtual void operator()(char *res, const char *op1,
                            const char *op2);

protected:
    StructType *resStructType;
    StructType *opStructType;
    size_t numElems;
    // array of operations on the elements.
    BinaryOp **elemOps;
};
/**
  * \ingroup Catalogmgrs
  */
class OpEQUALStruct : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpEQUALStruct(const BaseType *newResType, const BaseType *newOp1Type,
                  const BaseType *newOp2Type, size_t newResOff = 0,
                  size_t newOp1Off = 0, size_t newOp2Off = 0);
    /// destructor.
    virtual ~OpEQUALStruct();
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);

protected:
    size_t numElems;
    // array of operations on the elements.
    BinaryOp **elemOps;
};
/**
  * \ingroup Catalogmgrs
  */
class OpNOTEQUALStruct : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpNOTEQUALStruct(const BaseType *newResType, const BaseType *newOp1Type,
                     const BaseType *newOp2Type, size_t newResOff = 0,
                     size_t newOp1Off = 0,
                     size_t newOp2Off = 0);
    /// destructor.
    virtual ~OpNOTEQUALStruct();
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);

protected:
    size_t numElems;
    // array of operations on the elements.
    BinaryOp **elemOps;
};
/**
  * \ingroup Catalogmgrs
  */
class OpComparisonStruct : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpComparisonStruct(Ops::OpType op, const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff = 0,
                       size_t newOp1Off = 0, size_t newOp2Off = 0);
    /// destructor.
    virtual ~OpComparisonStruct();
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);

protected:
    size_t numElems;
    // array of operations on the elements.
    BinaryOp **elemOps;
    BinaryOp **equalOps;
    Ops::OpType op;
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_PLUS on C type #unsigned long# and #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpPLUSCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpPLUSCULong(const BaseType *newResType, const BaseType *newOp1Type,
                 const BaseType *newOp2Type, size_t newResOff = 0,
                 size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};
/**
  * \ingroup Catalogmgrs
  */
class OpPLUSULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpPLUSULong(const BaseType *newResType, const BaseType *newOp1Type,
                const BaseType *newOp2Type, size_t newResOff = 0,
                size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MAX_BINARY on C type #unsigned long# and #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpMAX_BINARYCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMAX_BINARYCULong(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff = 0,
                       size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};
/**
  * \ingroup Catalogmgrs
  */
class OpMAX_BINARYULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMAX_BINARYULong(const BaseType *newResType, const BaseType *newOp1Type,
                      const BaseType *newOp2Type, size_t newResOff = 0,
                      size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MIN_BINARY on C type #unsigned long# and #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpMIN_BINARYCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMIN_BINARYCULong(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff = 0,
                       size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};
/**
  * \ingroup Catalogmgrs
  */
class OpMIN_BINARYULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMIN_BINARYULong(const BaseType *newResType, const BaseType *newOp1Type,
                      const BaseType *newOp2Type, size_t newResOff = 0,
                      size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MINUS on C type #unsigned long# and #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpMINUSCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMINUSCULong(const BaseType *newResType, const BaseType *newOp1Type,
                  const BaseType *newOp2Type, size_t newResOff = 0,
                  size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_DIV on C type #unsigned long# and #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpDIVCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpDIVCULong(const BaseType *newResType, const BaseType *newOp1Type,
                const BaseType *newOp2Type, size_t newResOff = 0,
                size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MOD on C type #unsigned long# and #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpMODCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMODCULong(const BaseType *newResType, const BaseType *newOp1Type,
                const BaseType *newOp2Type, size_t newResOff = 0,
                size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MULT on C type #unsigned long# and #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpMULTCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMULTCULong(const BaseType *newResType, const BaseType *newOp1Type,
                 const BaseType *newOp2Type, size_t newResOff = 0,
                 size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_AND on C type #unsigned long# and #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpANDCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpANDCULong(const BaseType *newResType, const BaseType *newOp1Type,
                const BaseType *newOp2Type, size_t newResOff = 0,
                size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_AND on Bools (logical as opposed to bitwise)
/**
  * \ingroup Catalogmgrs
  */
class OpANDBool : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpANDBool(const BaseType *newResType, const BaseType *newOp1Type,
              const BaseType *newOp2Type, size_t newResOff = 0,
              size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_OR on C type #unsigned long# and #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpORCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpORCULong(const BaseType *newResType, const BaseType *newOp1Type,
               const BaseType *newOp2Type, size_t newResOff = 0,
               size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_OR on Bools (logical as opposed to bitwise)
/**
  * \ingroup Catalogmgrs
  */
class OpORBool : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpORBool(const BaseType *newResType, const BaseType *newOp1Type,
             const BaseType *newOp2Type, size_t newResOff = 0,
             size_t newOp1Off = 0, size_t newOp2Off = 0,
             bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_XOR on C type #unsigned long# and #unsigned long#, result #unsigned long#.
/**
  * \ingroup Catalogmgrs
  */
class OpXORCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpXORCULong(const BaseType *newResType, const BaseType *newOp1Type,
                const BaseType *newOp2Type, size_t newResOff = 0,
                size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_XOR on Bools (logical as opposed to bitwise)
/**
  * \ingroup Catalogmgrs
  */
class OpXORBool : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpXORBool(const BaseType *newResType, const BaseType *newOp1Type,
              const BaseType *newOp2Type, size_t newResOff = 0,
              size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_PLUS on C type #long# and #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpPLUSCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpPLUSCLong(const BaseType *newResType, const BaseType *newOp1Type,
                const BaseType *newOp2Type, size_t newResOff = 0,
                size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MAX_BINARY on C type #long# and #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpMAX_BINARYCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMAX_BINARYCLong(const BaseType *newResType, const BaseType *newOp1Type,
                      const BaseType *newOp2Type, size_t newResOff = 0,
                      size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MIN_BINARY on C type #long# and #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpMIN_BINARYCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMIN_BINARYCLong(const BaseType *newResType, const BaseType *newOp1Type,
                      const BaseType *newOp2Type, size_t newResOff = 0,
                      size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};
//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MINUS on C type #long# and #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpMINUSCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMINUSCLong(const BaseType *newResType, const BaseType *newOp1Type,
                 const BaseType *newOp2Type, size_t newResOff = 0,
                 size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_DIV on C type #long# and #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpDIVCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpDIVCLong(const BaseType *newResType, const BaseType *newOp1Type,
               const BaseType *newOp2Type, size_t newResOff = 0,
               size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MOD on C type #long# and #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpMODCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMODCLong(const BaseType *newResType, const BaseType *newOp1Type,
               const BaseType *newOp2Type, size_t newResOff = 0,
               size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MULT on C type #long# and #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpMULTCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMULTCLong(const BaseType *newResType, const BaseType *newOp1Type,
                const BaseType *newOp2Type, size_t newResOff = 0,
                size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_AND on C type #long# and #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpANDCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpANDCLong(const BaseType *newResType, const BaseType *newOp1Type,
               const BaseType *newOp2Type, size_t newResOff = 0,
               size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_OR on C type #long# and #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpORCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpORCLong(const BaseType *newResType, const BaseType *newOp1Type,
              const BaseType *newOp2Type, size_t newResOff = 0,
              size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_XOR on C type #long# and #long#, result #long#.
/**
  * \ingroup Catalogmgrs
  */
class OpXORCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpXORCLong(const BaseType *newResType, const BaseType *newOp1Type,
               const BaseType *newOp2Type, size_t newResOff = 0,
               size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_PLUS on C type #double# and #double#, result #double#.
/**
  * \ingroup Catalogmgrs
  */
class OpPLUSCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpPLUSCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                  const BaseType *newOp2Type, size_t newResOff = 0,
                  size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MAX_BINARY on C type #double# and #double#, result #double#.
/**
  * \ingroup Catalogmgrs
  */
class OpMAX_BINARYCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMAX_BINARYCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                        const BaseType *newOp2Type, size_t newResOff = 0,
                        size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MIN_BINARY on C type #double# and #double#, result #double#.
/**
  * \ingroup Catalogmgrs
  */
class OpMIN_BINARYCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMIN_BINARYCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                        const BaseType *newOp2Type, size_t newResOff = 0,
                        size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MINUS on C type #double# and #double#, result #double#.
/**
  * \ingroup Catalogmgrs
  */
class OpMINUSCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMINUSCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                   const BaseType *newOp2Type, size_t newResOff = 0,
                   size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_DIV on C type #double# and #double#, result #double#.
/**
  * \ingroup Catalogmgrs
  */
class OpDIVCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpDIVCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                 const BaseType *newOp2Type, size_t newResOff = 0,
                 size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MOD on C type #double# and #double#, result #double#.
/**
  * \ingroup Catalogmgrs
  */
class OpMODCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMODCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                 const BaseType *newOp2Type, size_t newResOff = 0,
                 size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1, const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MULT on C type #double# and #double#, result #double#.
/**
  * \ingroup Catalogmgrs
  */
class OpMULTCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMULTCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                  const BaseType *newOp2Type, size_t newResOff = 0,
                  size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_EQUAL on C type #unsigned long# and #unsigned long#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpEQUALCCharCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpEQUALCCharCULong(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff = 0,
                       size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_LESS on C type #unsigned long# and #unsigned long#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpLESSCCharCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpLESSCCharCULong(const BaseType *newResType, const BaseType *newOp1Type,
                      const BaseType *newOp2Type, size_t newResOff = 0,
                      size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_LESSEQUAL on C type #unsigned long# and #unsigned long#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpLESSEQUALCCharCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpLESSEQUALCCharCULong(const BaseType *newResType, const BaseType *newOp1Type,
                           const BaseType *newOp2Type, size_t newResOff = 0,
                           size_t newOp1Off = 0,
                           size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_NOTEQUAL on C type #unsigned long# and #unsigned long#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpNOTEQUALCCharCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpNOTEQUALCCharCULong(const BaseType *newResType, const BaseType *newOp1Type,
                          const BaseType *newOp2Type, size_t newResOff = 0,
                          size_t newOp1Off = 0,
                          size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_GREATER on C type #unsigned long# and #unsigned long#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpGREATERCCharCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpGREATERCCharCULong(const BaseType *newResType, const BaseType *newOp1Type,
                         const BaseType *newOp2Type, size_t newResOff = 0,
                         size_t newOp1Off = 0,
                         size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_GREATEREQUAL on \Ref{ULong} and \Ref{ULong}, result \Ref{Bool}.
/**
  * \ingroup Catalogmgrs
  */
class OpGREATEREQUALCCharCULong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpGREATEREQUALCCharCULong(const BaseType *newResType, const BaseType *newOp1Type,
                              const BaseType *newOp2Type, size_t newResOff = 0,
                              size_t newOp1Off = 0,
                              size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_EQUAL on C type #unsigned long# and #unsigned long#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpEQUALCCharCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpEQUALCCharCLong(const BaseType *newResType, const BaseType *newOp1Type,
                      const BaseType *newOp2Type, size_t newResOff = 0,
                      size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_LESS on C type #long# and #long#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpLESSCCharCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpLESSCCharCLong(const BaseType *newResType, const BaseType *newOp1Type,
                     const BaseType *newOp2Type, size_t newResOff = 0,
                     size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_LESSEQUAL on C type #long# and #long#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpLESSEQUALCCharCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpLESSEQUALCCharCLong(const BaseType *newResType, const BaseType *newOp1Type,
                          const BaseType *newOp2Type, size_t newResOff = 0,
                          size_t newOp1Off = 0,
                          size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_NOTEQUAL on C type #long# and #long#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpNOTEQUALCCharCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpNOTEQUALCCharCLong(const BaseType *newResType, const BaseType *newOp1Type,
                         const BaseType *newOp2Type, size_t newResOff = 0,
                         size_t newOp1Off = 0,
                         size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_GREATER on C type #long# and #long#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpGREATERCCharCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpGREATERCCharCLong(const BaseType *newResType, const BaseType *newOp1Type,
                        const BaseType *newOp2Type, size_t newResOff = 0,
                        size_t newOp1Off = 0,
                        size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_GREATEREQUAL on C type #long# and #long#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpGREATEREQUALCCharCLong : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpGREATEREQUALCCharCLong(const BaseType *newResType, const BaseType *newOp1Type,
                             const BaseType *newOp2Type, size_t newResOff = 0,
                             size_t newOp1Off = 0,
                             size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_EQUAL on C type #double# and #double#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpEQUALCCharCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpEQUALCCharCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                        const BaseType *newOp2Type, size_t newResOff = 0,
                        size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_LESS on C type #double# and #double#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpLESSCCharCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpLESSCCharCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff = 0,
                       size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_LESSEQUAL on C type #double# and #double#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpLESSEQUALCCharCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpLESSEQUALCCharCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                            const BaseType *newOp2Type, size_t newResOff = 0,
                            size_t newOp1Off = 0,
                            size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_NOTEQUAL on C type #double# and #double#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpNOTEQUALCCharCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpNOTEQUALCCharCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                           const BaseType *newOp2Type, size_t newResOff = 0,
                           size_t newOp1Off = 0,
                           size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_GREATER on C type #double# and #double#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpGREATERCCharCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpGREATERCCharCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                          const BaseType *newOp2Type, size_t newResOff = 0,
                          size_t newOp1Off = 0,
                          size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_GREATEREQUAL on C type #double# and #double#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpGREATEREQUALCCharCDouble : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpGREATEREQUALCCharCDouble(const BaseType *newResType, const BaseType *newOp1Type,
                               const BaseType *newOp2Type, size_t newResOff = 0,
                               size_t newOp1Off = 0,
                               size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};
//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_EQUAL on C type #CFloat# and #CFloat#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpEQUALComplexFloat : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpEQUALComplexFloat(const BaseType *newResType, const BaseType *newOp1Type,
                        const BaseType *newOp2Type, size_t newResOff = 0,
                        size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_NOTEQUAL on C type #CFloat# and #CFloat#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpNOTEQUALComplexFloat : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpNOTEQUALComplexFloat(const BaseType *newResType, const BaseType *newOp1Type,
                           const BaseType *newOp2Type, size_t newResOff = 0,
                           size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};
//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_EQUAL on C type #cInt# and #cInt#, result #char#.
/**
  * \ingroup Catalogmgrs
  */

class OpEQUALComplexInt : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpEQUALComplexInt(const BaseType *newResType, const BaseType *newOp1Type,
                      const BaseType *newOp2Type, size_t newResOff = 0,
                      size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_NOTEQUAL on C type #cInt# and #cInto#, result #char#.
/**
  * \ingroup Catalogmgrs
  */
class OpNOTEQUALComplexInt : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpNOTEQUALComplexInt(const BaseType *newResType, const BaseType *newOp1Type,
                         const BaseType *newOp2Type, size_t newResOff = 0,
                         size_t newOp1Off = 0,
                         size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_PLUS specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpPLUSChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpPLUSChar(const BaseType *newResType, const BaseType *newOp1Type,
               const BaseType *newOp2Type, size_t newResOff = 0,
               size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MAX_BINARY specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpMAX_BINARYChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMAX_BINARYChar(const BaseType *newResType, const BaseType *newOp1Type,
                     const BaseType *newOp2Type, size_t newResOff = 0,
                     size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MIN_BINARY specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpMIN_BINARYChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMIN_BINARYChar(const BaseType *newResType, const BaseType *newOp1Type,
                     const BaseType *newOp2Type, size_t newResOff = 0,
                     size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MINUS specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpMINUSChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMINUSChar(const BaseType *newResType, const BaseType *newOp1Type,
                const BaseType *newOp2Type, size_t newResOff = 0,
                size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MULT specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpMULTChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMULTChar(const BaseType *newResType, const BaseType *newOp1Type,
               const BaseType *newOp2Type, size_t newResOff = 0,
               size_t newOp1Off = 0, size_t newOp2Off = 0, bool nullAsIdentity = false);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
    virtual void getCondenseInit(char *init);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_DIV specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpDIVChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpDIVChar(const BaseType *newResType, const BaseType *newOp1Type,
              const BaseType *newOp2Type, size_t newResOff = 0,
              size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_MOD specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpMODChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpMODChar(const BaseType *newResType, const BaseType *newOp1Type,
              const BaseType *newOp2Type, size_t newResOff = 0,
              size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_EQUAL specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpEQUALChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpEQUALChar(const BaseType *newResType, const BaseType *newOp1Type,
                const BaseType *newOp2Type, size_t newResOff = 0,
                size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_LESS specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpLESSChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpLESSChar(const BaseType *newResType, const BaseType *newOp1Type,
               const BaseType *newOp2Type, size_t newResOff = 0,
               size_t newOp1Off = 0, size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_LESSEQUAL specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpLESSEQUALChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpLESSEQUALChar(const BaseType *newResType, const BaseType *newOp1Type,
                    const BaseType *newOp2Type, size_t newResOff = 0,
                    size_t newOp1Off = 0,
                    size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_NOTEQUAL specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpNOTEQUALChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpNOTEQUALChar(const BaseType *newResType, const BaseType *newOp1Type,
                   const BaseType *newOp2Type, size_t newResOff = 0,
                   size_t newOp1Off = 0,
                   size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_GREATER specialized for RasDaMan type Char.
/**
  * \ingroup Catalogmgrs
  */
class OpGREATERChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpGREATERChar(const BaseType *newResType, const BaseType *newOp1Type,
                  const BaseType *newOp2Type, size_t newResOff = 0,
                  size_t newOp1Off = 0,
                  size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_GREATEREQUAL specialized for RasDaMan type Char.

/**
  * \ingroup Catalogmgrs
  */
class OpGREATEREQUALChar : public BinaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operands.
    OpGREATEREQUALChar(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff = 0,
                       size_t newOp1Off = 0,
                       size_t newOp2Off = 0);
    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1,
                            const char *op2);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_IDENTITY specialized for RasDaMan type Char.

/**
  * \ingroup Catalogmgrs
  */
class OpIDENTITYChar : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpIDENTITYChar(const BaseType *newResType, const BaseType *newOpType,
                   size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpUpdateChar : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateChar(const BaseType *newResType, const BaseType *newOpType,
                 size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpUpdateOctet : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateOctet(const BaseType *newResType, const BaseType *newOpType,
                  size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_IDENTITY specialized for RasDaMan type Short.

/**
  * \ingroup Catalogmgrs
  */
class OpIDENTITYShort : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpIDENTITYShort(const BaseType *newResType, const BaseType *newOpType,
                    size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpUpdateShort : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateShort(const BaseType *newResType, const BaseType *newOpType,
                  size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpUpdateUShort : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateUShort(const BaseType *newResType, const BaseType *newOpType,
                   size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_IDENTITY specialized for RasDaMan type Long.

/**
  * \ingroup Catalogmgrs
  */
class OpIDENTITYLong : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpIDENTITYLong(const BaseType *newResType, const BaseType *newOpType,
                   size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpIDENTITYDouble : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpIDENTITYDouble(const BaseType *newResType, const BaseType *newOpType,
                     size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpUpdateLong : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateLong(const BaseType *newResType, const BaseType *newOpType,
                 size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpUpdateULong : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateULong(const BaseType *newResType, const BaseType *newOpType,
                  size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpUpdateFloat : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateFloat(const BaseType *newResType, const BaseType *newOpType,
                  size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpUpdateDouble : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpUpdateDouble(const BaseType *newResType, const BaseType *newOpType,
                   size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

//--------------------------------------------
//      OpISNULL
//--------------------------------------------
/*@Doc:
OpISNULL checks if a cell is null.
*/

/**
  * \ingroup Catalogmgrs
  */
class OpISNULLCLong : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpISNULLCLong(const BaseType *newResType, const BaseType *newOpType,
                  size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpISNULLCULong : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpISNULLCULong(const BaseType *newResType, const BaseType *newOpType,
                   size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpISNULLCDouble : public UnaryOp
{
public:
    /// constructor gets RasDaMan base type of result and operand.
    OpISNULLCDouble(const BaseType *newResType, const BaseType *newOpType,
                    size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out operation on `op` with result `result`.
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogif}.

/*@Doc:
  MarrayOp is the superclass for all marray constructors. The class
  defined here is just a dummy and will be specialized in another
  module. operator() gets an r_Point as a parameter. For a useful
  marray constructor operation() will also need to calculate an
  expression.
*/

/**
  * \ingroup Catalogmgrs
  */
class MarrayOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and its
      offset (for structs). Subclasses will have additional parameters. */
    MarrayOp(const BaseType *newResType, size_t newResOff = 0);
    /// operator to carry out operation on `p`. Has a dummy implementation.
    virtual void operator()(char *result, const r_Point &p);
    /*@ManMemo: virtual destructor because subclasses may have
      non-trivial destructor. */
    virtual ~MarrayOp(){};

protected:
    const BaseType *resType;
    size_t resOff;
};

//@ManMemo: Module: {\bf catalogif}.

/*@Doc:
  GenCondenseOp is the superclass for all general condense operations.
  The class defined here is just a dummy and will be specialized in
  another module. operator() gets an r_Point as a parameter. For a useful
  marray constructor operation() will also need to calculate an
  expression. Every GenCondenseOp has a binary operation which is
  used to accumulate the values. If an initVal (of type resType)
  is given, it is used as a basis for accumulation. Otherwise a
  default initVal is retrieved from `accuOp`.
*/

/**
  * \ingroup Catalogmgrs
  */
class GenCondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and its
      offset (for structs, 0 if no struct). A binary operation for
      accumulation is given and an optional init value. Subclasses
      will have additional parameters. Note that newInitVal has to be
      deleted by the caller! */
    GenCondenseOp(const BaseType *newResType, size_t newResOff,
                  BinaryOp *newAccuOp, char *newInitVal = 0);
    /// operator to carry out operation on `p`. Has a dummy implementation.
    virtual void operator()(const r_Point &p);
    /// returns binary accumulation op (needed in class {\ref Tile}.)
    BinaryOp *getAccuOp();
    /// returns result type (needed in class {\ref Tile}.)
    const BaseType *getResultType();
    /// returns result offset (needed in class {\ref Tile}.)
    size_t getResultOff();
    /// returns accumulated result.
    char *getAccuVal();
    /*@ManMemo: virtual destructor because subclasses may have
      non-trivial destructor. */
    virtual ~GenCondenseOp();

protected:
    const BaseType *resType;
    size_t resOff;
    BinaryOp *accuOp;
    // used to flag if destructor should delete initVal
    bool myInitVal;
    // initVal is always of RasDaMan-Type restype!
    char *initVal;
};

//--------------------------------------------
//      Complex operations
//--------------------------------------------

/**
  * \ingroup Catalogmgrs
  */
class OpPLUSComplex : public BinaryOp
{
public:
    OpPLUSComplex(
        const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff = 0,
        size_t newOp1Off = 0,
        size_t newOp2Off = 0,
        BinaryOp::ScalarFlag flag = NONE);
    virtual void operator()(char *res, const char *op1, const char *op2);
    virtual void getCondenseInit(char *init);

protected:
    size_t op1ReOff;
    size_t op1ImOff;
    size_t op2ReOff;
    size_t op2ImOff;
    size_t resReOff;
    size_t resImOff;
    BinaryOp::ScalarFlag scalarFlag;
};

class OpPLUSComplexInt : public BinaryOp
{
public:
    OpPLUSComplexInt(
        const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff = 0,
        size_t newOp1Off = 0,
        size_t newOp2Off = 0,
        BinaryOp::ScalarFlag flag = NONE);
    virtual void operator()(char *res, const char *op1, const char *op2);
    virtual void getCondenseInit(char *init);

protected:
    size_t op1ReOff;
    size_t op1ImOff;
    size_t op2ReOff;
    size_t op2ImOff;
    size_t resReOff;
    size_t resImOff;
    BinaryOp::ScalarFlag scalarFlag;
};

/**
  * \ingroup Catalogmgrs
  */
class OpMINUSComplex : public BinaryOp
{
public:
    OpMINUSComplex(
        const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff = 0,
        size_t newOp1Off = 0,
        size_t newOp2Off = 0,
        BinaryOp::ScalarFlag flag = NONE);
    virtual void operator()(char *res, const char *op1, const char *op2);

protected:
    size_t op1ReOff;
    size_t op1ImOff;
    size_t op2ReOff;
    size_t op2ImOff;
    size_t resReOff;
    size_t resImOff;
    BinaryOp::ScalarFlag scalarFlag;
};

class OpMINUSComplexInt : public BinaryOp
{
public:
    OpMINUSComplexInt(
        const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff = 0,
        size_t newOp1Off = 0,
        size_t newOp2Off = 0,
        BinaryOp::ScalarFlag flag = NONE);
    virtual void operator()(char *res, const char *op1, const char *op2);

protected:
    size_t op1ReOff;
    size_t op1ImOff;
    size_t op2ReOff;
    size_t op2ImOff;
    size_t resReOff;
    size_t resImOff;
    BinaryOp::ScalarFlag scalarFlag;
};

/**
  * \ingroup Catalogmgrs
  */
class OpDIVComplex : public BinaryOp
{
public:
    OpDIVComplex(
        const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff = 0,
        size_t newOp1Off = 0,
        size_t newOp2Off = 0,
        BinaryOp::ScalarFlag flag = NONE);
    virtual void operator()(char *res, const char *op1, const char *op2);

protected:
    size_t op1ReOff;
    size_t op1ImOff;
    size_t op2ReOff;
    size_t op2ImOff;
    size_t resReOff;
    size_t resImOff;
    BinaryOp::ScalarFlag scalarFlag;
};

class OpDIVComplexInt : public BinaryOp
{
public:
    OpDIVComplexInt(
        const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff = 0,
        size_t newOp1Off = 0,
        size_t newOp2Off = 0,
        BinaryOp::ScalarFlag flag = NONE);
    virtual void operator()(char *res, const char *op1, const char *op2);

protected:
    size_t op1ReOff;
    size_t op1ImOff;
    size_t op2ReOff;
    size_t op2ImOff;
    size_t resReOff;
    size_t resImOff;
    BinaryOp::ScalarFlag scalarFlag;
};
/**
  * \ingroup Catalogmgrs
  */
class OpMULTComplex : public BinaryOp
{
public:
    OpMULTComplex(
        const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff = 0,
        size_t newOp1Off = 0,
        size_t newOp2Off = 0,
        BinaryOp::ScalarFlag flag = NONE);
    virtual void operator()(char *res, const char *op1, const char *op2);
    virtual void getCondenseInit(char *init);

protected:
    size_t op1ReOff;
    size_t op1ImOff;
    size_t op2ReOff;
    size_t op2ImOff;
    size_t resReOff;
    size_t resImOff;
    BinaryOp::ScalarFlag scalarFlag;
};

class OpMULTComplexInt : public BinaryOp
{
public:
    OpMULTComplexInt(
        const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff = 0,
        size_t newOp1Off = 0,
        size_t newOp2Off = 0,
        BinaryOp::ScalarFlag flag = NONE);
    virtual void operator()(char *res, const char *op1, const char *op2);
    virtual void getCondenseInit(char *init);

protected:
    size_t op1ReOff;
    size_t op1ImOff;
    size_t op2ReOff;
    size_t op2ImOff;
    size_t resReOff;
    size_t resImOff;
    BinaryOp::ScalarFlag scalarFlag;
};
/**
  * \ingroup Catalogmgrs
  */
class OpIDENTITYComplex : public UnaryOp
{
public:
    OpIDENTITYComplex(const BaseType *, const BaseType *, size_t = 0, size_t = 0);
    virtual void operator()(char *result, const char *op);
};
class OpUpdateComplex : public UnaryOp
{
public:
    OpUpdateComplex(const BaseType *, const BaseType *, size_t = 0, size_t = 0);
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpSUMComplex : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpSUMComplex(const BaseType *newResType, const BaseType *newOpType,
                 size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpSUMComplex(const BaseType *newResType, char *newAccu,
                 const BaseType *newOpType, size_t newResOff,
                 size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);

private:
    OpPLUSComplex plusBinary;
};

class OpSUMComplexInt : public CondenseOp
{
public:
    /*@ManMemo: constructor gets RasDaMan base type of result and operand
                and offsets to result and operand (for structs). */
    OpSUMComplexInt(const BaseType *newResType, const BaseType *newOpType,
                    size_t newResOff = 0, size_t newOpOff = 0);
    /// constructor initializing internal accu.
    OpSUMComplexInt(const BaseType *newResType, char *newAccu,
                    const BaseType *newOpType, size_t newResOff,
                    size_t newOpOff);
    /// operator to carry out operation on `op`.
    virtual char *operator()(const char *op, char *myAccu);
    /// operator to carry out operation on `op` using internal accu.
    virtual char *operator()(const char *op);

private:
    OpPLUSComplexInt plusBinary;
};
/**
  * \ingroup Catalogmgrs
  */
class OpConstructComplex : public BinaryOp
{
public:
    OpConstructComplex(const BaseType *newResType, const BaseType *newOp1Type,
                       const BaseType *newOp2Type, size_t newResOff = 0,
                       size_t newOp1Off = 0, size_t newOp2Off = 0);
    virtual void operator()(char *res, const char *op1, const char *op2);

private:
    size_t resReOff;
    size_t resImOff;
};

class OpConstructComplexInt : public BinaryOp
{
public:
    OpConstructComplexInt(const BaseType *newResType, const BaseType *newOp1Type,
                          const BaseType *newOp2Type, size_t newResOff = 0,
                          size_t newOp1Off = 0, size_t newOp2Off = 0);
    virtual void operator()(char *res, const char *op1, const char *op2);

private:
    size_t resReOff;
    size_t resImOff;
};

/**
  * \ingroup Catalogmgrs
  */
class OpRealPart : public UnaryOp
{
public:
    OpRealPart(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);

private:
    size_t opReOff;
};

class OpRealPartInt : public UnaryOp
{
public:
    OpRealPartInt(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);

private:
    size_t opReOff;
};

/**
  * \ingroup Catalogmgrs
  */
class OpImaginarPart : public UnaryOp
{
public:
    OpImaginarPart(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);

private:
    size_t opImOff;
};

class OpImaginarPartInt : public UnaryOp
{
public:
    OpImaginarPartInt(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);

private:
    size_t opImOff;
};
//--------------------------------------------
//      OpCAST
//--------------------------------------------
/*@Doc:
OpCAST provide cast operation.
*/

/**
  * \ingroup Catalogmgrs
  */
class OpCASTDouble : public UnaryOp
{
public:
    OpCASTDouble(const BaseType *newResType, const BaseType *newOpType,
                 size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out cast operation.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpCASTLong : public UnaryOp
{
public:
    OpCASTLong(const BaseType *newResType, const BaseType *newOpType,
               size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out cast operation.
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpCASTULong : public UnaryOp
{
public:
    OpCASTULong(const BaseType *newResType, const BaseType *newOpType,
                size_t newResOff = 0, size_t newOpOff = 0);
    /// operator to carry out cast operation.
    virtual void operator()(char *result, const char *op);
};

//--------------------------------------------
//      OpOVERLAY
//--------------------------------------------
//@ManMemo: Module: {\bf catalogif}.
//@Doc: OP_OVERLAY

/**
  * \ingroup Catalogmgrs
  */
class OpOVERLAY : public BinaryOp
{
public:
    /// this pattern is only 16 bytes long and empty, if your struct is longer you need to supply your own pattern
    static const char *nullPattern;
    /// constructor gets RasDaMan base type of result and operands.
    OpOVERLAY(const BaseType *newResType,
              const BaseType *newOp1Type,
              const BaseType *newOp2Type,
              size_t typeSize,
              const char *transparentPattern = OpOVERLAY::nullPattern,
              size_t newResOff = 0,
              size_t newOp1Off = 0,
              size_t newOp2Off = 0);

    /*@ManMemo: operator to carry out operation on `op1` and
                `op2` with result `res`. */
    virtual void operator()(char *res, const char *op1, const char *op2);

private:
    size_t length;

    const char *pattern;
};

//--------------------------------------------
//      OpBIT
//--------------------------------------------
/*@Doc:
*/

/**
  * \ingroup Catalogmgrs
  */
class OpBIT : public BinaryOp
{
public:
    OpBIT(
        const BaseType *newResType,
        const BaseType *newOp1Type,
        const BaseType *newOp2Type,
        size_t newResOff = 0,
        size_t newOp1Off = 0,
        size_t newOp2Off = 0);

    /// operator to carry out bit operation
    virtual void operator()(char *res, const char *op1, const char *op2);
};

#include "autogen_ops.hh"

#endif
