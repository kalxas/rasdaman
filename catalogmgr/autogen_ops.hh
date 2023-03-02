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
 *
 *
 * COMMENTS:    Automaticaly generated
 *
 ************************************************************/

class BaseType;

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpABSCDouble : public UnaryOp
{
public:
    OpABSCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpABSCLong : public UnaryOp
{
public:
    OpABSCLong(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

/**
  * \ingroup Catalogmgrs
  */
class OpABSCULong : public UnaryOp
{
public:
    OpABSCULong(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpSQRTCDouble : public UnaryOp
{
public:
    OpSQRTCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpPOWCDouble : public UnaryOp
{
public:
    OpPOWCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
    void setExponent(double exponent);

private:
    double exponent;
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpEXPCDouble : public UnaryOp
{
public:
    OpEXPCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpLOGCDouble : public UnaryOp
{
public:
    OpLOGCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpLNCDouble : public UnaryOp
{
public:
    OpLNCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpSINCDouble : public UnaryOp
{
public:
    OpSINCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpCOSCDouble : public UnaryOp
{
public:
    OpCOSCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpTANCDouble : public UnaryOp
{
public:
    OpTANCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpSINHCDouble : public UnaryOp
{
public:
    OpSINHCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpCOSHCDouble : public UnaryOp
{
public:
    OpCOSHCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpTANHCDouble : public UnaryOp
{
public:
    OpTANHCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpARCSINCDouble : public UnaryOp
{
public:
    OpARCSINCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpARCCOSCDouble : public UnaryOp
{
public:
    OpARCCOSCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};

//@ManMemo: Module: {\bf catalogmgr}

/*@Doc:

*/

// class declaration
/**
  * \ingroup Catalogmgrs
  */
class OpARCTANCDouble : public UnaryOp
{
public:
    OpARCTANCDouble(
        const BaseType *newResType,
        const BaseType *newOpType,
        size_t newResOff = 0,
        size_t newOpOff = 0);
    virtual void operator()(char *result, const char *op);
};
