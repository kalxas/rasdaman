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
 * SOURCE: test_dirtiling.cc
 *
 * MODULE: rasodmg
 *
 * PURPOSE: test class r_Dir_Tiling.
 *
 * COMMENTS:
 *          None
*/

#include "config.h"

/// RASDAMAN includes
#ifdef EARLY_TEMPLATE
#define __EXECUTABLE__
#ifdef __GNUG__
#include "rasodmg/template_inst.hh"
#endif
#endif

#include <iostream>
#include <stdio.h>
#include "rasodmg/marray.hh"
#include "rasodmg/tiling.hh"
#include "rasodmg/dirtiling.hh"
#include "rasodmg/dirdecompose.hh"

int main(int argc, char *argv[])
{
    int tilesize = 10000;

    // Get tile size
    if (argc == 2)
    {
        tilesize = atoi(argv[1]);
    }

    // Create a domain decomposition

    vector<r_Dir_Decompose> decomp;
    r_Dir_Decompose a;
    a << 0 << 50 << 200 << 600 << 700 << 950 << 999;
    decomp.push_back(a);
    r_Dir_Decompose b;
    b << 0 << 50 << 99;
    decomp.push_back(b);

    r_Dir_Tiling tiling(2, decomp, tilesize);
    tiling.print_status(cout);

    cout << endl
         << "----- Domain decomposition -----" << endl;

    // Create a domain and an image

    r_Minterval domain(2);
    domain << r_Sinterval((r_Range)0L, (r_Range)999L)
           << r_Sinterval((r_Range)0L, (r_Range)99L);

    r_Marray<char> image(domain);

    // Compute tiles

    vector<r_Minterval> *tiles = tiling.compute_tiles(domain, sizeof(char));

    // Output the information

    cout << "Domain: " << domain << endl
         << endl;
    cout << "Tiles:  " << endl;

    vector<r_Minterval>::iterator it;
    for (it = tiles->begin(); it != tiles->end(); it++)
    {
        r_Minterval inter = *it;
        cout << "   " << inter << endl;
    }

    delete tiles;
}
