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
// -*-C++-*- (for Emacs)

/*************************************************************
 *
 *
 * PURPOSE:
 *
 *
 *
 *
 * COMMENTS:
 *
 ************************************************************/

#ifndef _COMPLEXTYPE_HH_
#define _COMPLEXTYPE_HH_

#include <iosfwd>
#ifdef __APPLE__
#include <float.h>
#else
#include <values.h>
#endif
#include "realtype.hh"
#include "catalogmgr/ops.hh"

class OId;

/**
  * \ingroup Relcatalogifs
  */
class GenericComplexType : public AtomicType
{
public:
    GenericComplexType(const char *name, unsigned int newSize): AtomicType(name, newSize) {}
    GenericComplexType(const OId &id): AtomicType(id) {}
    ~GenericComplexType() override = default;
    virtual unsigned int getReOffset() const = 0;
    virtual unsigned int getImOffset() const = 0;

protected:
    void readFromDb() override = 0;
};

/**
  * \ingroup Relcatalogifs
  */
class ComplexType1 : public GenericComplexType
{
public:
    ComplexType1();
    ComplexType1(const OId &id);
    ComplexType1(const ComplexType1 &old);
    ComplexType1 &operator=(const ComplexType1 &old);
    ~ComplexType1() override;
    void printCell(std::ostream &stream, const char *cell) const override;
    unsigned int getReOffset() const override;
    unsigned int getImOffset() const override;
    const char *getTypeName() const override;
    static const char *Name;

protected:
    void readFromDb() override;

private:
    unsigned int reOffset, imOffset;

    //  static const char* complexTypeName;

    r_ULong *convertToCULong(const char *, r_ULong *) const override;
    char *makeFromCULong(char *, const r_ULong *) const override;
    r_Long *convertToCLong(const char *, r_Long *) const override;
    char *makeFromCLong(char *, const r_Long *) const override;
    double *convertToCDouble(const char *cell, double *value) const override;
    char *makeFromCDouble(char *cell, const double *value) const override;
};

/**
  * \ingroup Relcatalogifs
  */
class ComplexType2 : public GenericComplexType
{
public:
    ComplexType2();
    ComplexType2(const OId &id);
    ComplexType2(const ComplexType2 &old);
    ComplexType2 &operator=(const ComplexType2 &old);
    ~ComplexType2() override;
    void printCell(std::ostream &stream, const char *cell) const override;
    unsigned int getReOffset() const override;
    unsigned int getImOffset() const override;
    const char *getTypeName() const override;
    static const char *Name;

protected:
    void readFromDb() override;

private:
    unsigned int reOffset, imOffset;

    //  static const char* complexTypeName;

    r_ULong *convertToCULong(const char *, r_ULong *) const override;
    char *makeFromCULong(char *, const r_ULong *) const override;
    r_Long *convertToCLong(const char *, r_Long *) const override;
    char *makeFromCLong(char *, const r_Long *) const override;
    double *convertToCDouble(const char *cell, double *value) const override;
    char *makeFromCDouble(char *cell, const double *value) const override;
};

#include "complextype.icc"

#endif
