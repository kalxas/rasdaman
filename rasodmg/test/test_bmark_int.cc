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
 * SOURCE: test_bmark_dir.cc
 *
 * MODULE: rasodmg
 *
 * PURPOSE: benchmark interest tiling
 *
 * COMMENTS:
 *   This program is used to load the database with information
 *   for benchmarking interesting tiling
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
#include <stdio.h>
#include <stdlib.h>
#include "rasodmg/ref.hh"
#include "rasodmg/transaction.hh"
#include "rasodmg/database.hh"
#include "rasodmg/set.hh"
#include "rasodmg/marray.hh"
#include "rasodmg/tiling.hh"
#include "rasodmg/interesttiling.hh"
#include "rasodmg/alignedtiling.hh"
#include "rasodmg/dirtiling.hh"
#include "rasodmg/dirdecompose.hh"
#include "rasodmg/storagelayout.hh"
#include "RGBCube.hh"

#define S_32K               (32  * 1024L)
#define S_64K               (64  * 1024L)
#define S_128K              (128 * 1024L)
#define S_256K              (256 * 1024L)

#define TOTAL_CUBES         8
#define SIZE_X              120L
#define SIZE_Y              159L
#define SIZE_Z              119L

char* server_name;
char* dbase_name;
char* colect_name;

void parse(int argc, char* argv[])
{
    if (argc != 4)
    {
        cout << "Usage: " << argv[0] << " [server name] [db name] [colection name]"
             << endl;

        exit(0);
    }

    server_name = argv[1];
    dbase_name  = argv[2];
    colect_name = argv[3];
}

void insert_datacube()
{

    r_Ref<r_Set<r_Ref<r_Marray<r_ULong>>>>  cube_set;
    r_Minterval                                   domain;
    r_Storage_Layout*                      dsl[TOTAL_CUBES];
    r_OId                                         oid[TOTAL_CUBES];

    domain = r_Minterval(3);
    domain << r_Sinterval((r_Range)0L, (r_Range)SIZE_X)
           << r_Sinterval((r_Range)0L, (r_Range)SIZE_Y)
           << r_Sinterval((r_Range)0L, (r_Range)SIZE_Z);


    // For alligned tiling (Regular tiling)

    r_Minterval block_config(3);
    block_config << r_Sinterval((r_Range)0L, (r_Range)SIZE_X)
                 << r_Sinterval((r_Range)0L, (r_Range)SIZE_Y)
                 << r_Sinterval((r_Range)0L, (r_Range)SIZE_Z);

    r_Aligned_Tiling* til_reg_32k = new r_Aligned_Tiling(block_config, S_32K);
    r_Aligned_Tiling* til_reg_64k = new r_Aligned_Tiling(block_config, S_64K);
    r_Aligned_Tiling* til_reg_128k = new r_Aligned_Tiling(block_config, S_128K);
    r_Aligned_Tiling* til_reg_256k = new r_Aligned_Tiling(block_config, S_256K);


    // For directional tiling

    r_Minterval interest1(3);
    interest1 << r_Sinterval((r_Range)0L, (r_Range)SIZE_X)
              << r_Sinterval((r_Range)65L, (r_Range)110L)
              << r_Sinterval((r_Range)35L, (r_Range)75L);

    r_Minterval interest2(3);
    interest2 << r_Sinterval((r_Range)0L, (r_Range)SIZE_X)
              << r_Sinterval((r_Range)90L, (r_Range)150L)
              << r_Sinterval((r_Range)45L, (r_Range)105L);

    r_Minterval interest3(3);
    interest3 << r_Sinterval((r_Range)60L, (r_Range)120L)
              << r_Sinterval((r_Range)0L, (r_Range)SIZE_Y)
              << r_Sinterval((r_Range)0L, (r_Range)SIZE_Z);

    vector<r_Minterval> areas;
    areas.push_back(interest1);
    areas.push_back(interest2);
    areas.push_back(interest3);

    r_Interest_Tiling* til_int_32k =
        new r_Interest_Tiling((r_Dimension)3, areas, S_32K, r_Interest_Tiling::SUB_TILING);
    r_Interest_Tiling* til_int_64k =
        new r_Interest_Tiling((r_Dimension)3, areas, S_64K, r_Interest_Tiling::SUB_TILING);
    r_Interest_Tiling* til_int_128k =
        new r_Interest_Tiling((r_Dimension)3, areas, S_128K, r_Interest_Tiling::SUB_TILING);
    r_Interest_Tiling* til_int_256k =
        new r_Interest_Tiling((r_Dimension)3, areas, S_256K, r_Interest_Tiling::SUB_TILING);

    // Domain storage layouts

    dsl[0] = new r_Storage_Layout(til_reg_32k);
    dsl[1] = new r_Storage_Layout(til_reg_64k);
    dsl[2] = new r_Storage_Layout(til_reg_128k);
    dsl[3] = new r_Storage_Layout(til_reg_256k);

    dsl[4] = new r_Storage_Layout(til_int_32k);
    dsl[5] = new r_Storage_Layout(til_int_64k);
    dsl[6] = new r_Storage_Layout(til_int_128k);
    dsl[7] = new r_Storage_Layout(til_int_256k);


    // Create cubes

    r_Database db;
    db.set_servername(server_name);

    for (int i = 0; i < TOTAL_CUBES ; i++)
    {
        r_Transaction trans;

        r_Ref<r_Marray<r_ULong>> cube;

        try
        {
            cout << "Opening database " << dbase_name << " on " << server_name
                 << "... " << flush;

            db.open(dbase_name);

            cout << "Ok" << endl;
            cout << "Starting transaction... " << flush;

            trans.begin();

            cout << "Ok" << endl;
            cout << "Opening the set... " << flush;

            try
            {
                cube_set = db.lookup_object(colect_name);
            }
            catch (...)
            {
                cout << "*Failed*" << endl;
                cout << "Creating the set... " << flush;

                cube_set =
                    new(&db, "RGBSet3") r_Set<r_Ref<r_Marray<RGBPixel>>>;

                db.set_object_name(*cube_set, colect_name);
            }

            cout << "Ok" << endl;
            cout << "Creating the datacube... " << flush;

            cube =
                new(&db, "RGBCube") r_Marray<RGBPixel>(domain, dsl[i]);

            cube_set->insert_element(cube);

            cout << "  Cube[" << i << "]:  " << cube->get_oid() << endl;

            cout << "*" << flush;
            cout << " ... Ok" << endl;
            cout << "Commiting transaction... " << flush;

            trans.commit();

            cout << "Ok" << endl;
            cout << "Closing database... " << flush;

            db.close();
        }
        catch (r_Error& e)
        {
            cout << e.what() << endl;
            exit(0);
        }
        catch (...)
        {
            cout << "Undefined error..." << endl;
            exit(0);
        }
    }
}

int main(int argc, char* argv[])
{
    parse(argc, argv);
    insert_datacube();

    return 0;
}








