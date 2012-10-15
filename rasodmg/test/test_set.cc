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
/
/**
 * SOURCE: test_set.cc
 *
 * MODULE: rasodmg
 *
 * COMMENTS:
 *
*/

#include "config.h"

/// RASDAMAN includes
#ifdef EARLY_TEMPLATE
#define __EXECUTABLE__
#ifdef __GNUG__
#include "raslib/template_inst.hh"
#endif
#endif

#include <iostream>
#include "rasodmg/set.hh"

int main()
{
    r_Minterval domainV(1);
    domainV << r_Sinterval((r_Range) 0, (r_Range) 100);
    r_GMarray* v = new r_GMarray(domainV, 1);
    
    r_Minterval domainX(1);
    domainX << r_Sinterval((r_Range) 0, (r_Range) 200);
    r_GMarray* x = new r_GMarray(domainX, 1);
    
    r_Minterval domainY(1);
    domainY << r_Sinterval((r_Range) 0, (r_Range) 300);
    r_GMarray* y = new r_GMarray(domainY, 1);
    
    r_Minterval domainZ(1);
    domainZ << r_Sinterval((r_Range) 0, (r_Range) 400);
    r_GMarray* z = new r_GMarray(domainZ, 1);
    
    r_GMarray* next = new r_GMarray();

    cout << endl << endl;
    cout << "Set Examples" << endl;
    cout << "=============" << endl << endl;

    cout << "Creating r_Set of type int." << endl;
    r_Set<r_GMarray*> a;

    cout << "Cardinality of empty set 'a': " << a.cardinality() << endl << endl;

    cout << "Now inserting four elements:" << endl << "v = 100" << endl;
    a.insert_element(v);

    cout << "x = 200" << endl;
    a.insert_element(x);

    cout << "y = 100 (should fail in sets)" << endl;
    a.insert_element(y);

    cout << "z = 300" << endl;
    a.insert_element(z);

    cout << "Cardinality of collection 'a' after four inserts: " << a.cardinality() << endl << endl;

    cout << "Does 'a' contain element '100' (1=TRUE/0=FALSE)? " << a.contains_element(x) << endl;

    cout << "Does 'a' contain element '500' (1=TRUE/0=FALSE)? " << a.contains_element(next) << endl << endl;

    cout << "Now removing element 'x=200' from 'a'." << endl;
    a.remove_element(x);
    cout << "Cardinality of 'a' now: " << a.cardinality() << endl << endl;

    cout << "Now removing element '100' from 'a'." << endl;
    a.remove_element(x);
    cout << "Cardinality of 'a' now: " << a.cardinality() << endl << endl;

    cout << "Now removing (non-existing) element '500' from 'a'." << endl;
    a.remove_element(next);
    cout << "Cardinality of 'a' now: " << a.cardinality() << endl << endl;

    cout << "Testing assignment operator on r_Sets." << endl << "(creating r_Set 'b' that is equal to 'a'.)" <<endl;
    r_Set<r_GMarray*> b;
    b = a;
    cout << "Cardinality of 'b': " << b.cardinality() << endl << endl;

    cout << "Testing copy constructor of r_Set." << endl << "(creating r_Set 'c' that is equal to 'a'.)" <<endl;
    r_Set<r_GMarray*> c(a);
    cout << "Cardinality of 'c': " << c.cardinality() << endl << endl;

    cout << "Now removing all elements from 'a'." << endl;
    a.remove_all();
    cout << "Cardinality of 'a' now: " << a.cardinality() << endl;
    cout << "Cardinality of 'b' is still: " << b.cardinality() << endl;
    cout << "Cardinality of 'c' is still: " << c.cardinality() << endl << endl;

    return 0;
}
