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
 * NAME
 *  query.cc - C++ example to execute a rasql query
 *
 * SYNOPSIS
 *  query HOST PORT DATABASE USER PASSWORD
 *
 * DESCRIPTION
 *  Sends a hardwired query to rasdaman server HOST listening
 *  at port PORT with database name DATABASE, authenticating
 *  with user USER and password PASSWORD.
 *  The collection used is 'mr'.
 *
 * EXAMPLE
 *  query localhost 7001 RASBASE rasguest rasguest
 *
 * SEE ALSO
 *  The rasql command line utility allows typing queries
 *  from the command line, it is way more flexible.
 *
 ************************************************************/

#include "raslib/minterval.hh"
#include "raslib/odmgtypes.hh"
#include "raslib/error.hh"
#include "raslib/type.hh"
#include "rasodmg/database.hh"
#include "rasodmg/transaction.hh"
#include "rasodmg/set.hh"
#include "rasodmg/ref.hh"
#include "rasodmg/gmarray.hh"
#include "rasodmg/iterator.hh"
#include "rasodmg/oqlquery.hh"

// needed to configure logging
#include "loggingutils.hh"
#include <iostream>

using namespace std;

INITIALIZE_EASYLOGGINGPP

int main(int ac, char** av)
{
    // setup logging configuration
    common::LogConfiguration logConf;
    logConf.configClientLogging();

    char rasmgrName[255];
    int  rasmgrPort;
    char baseName[255];
    char userName[255];
    char userPass[255];

    if (ac != 6)
    {
        cout << "Usage: query HOST PORT DATABASE USER PASSWORD" << endl;
        return -1;
    }

    strcpy(rasmgrName, av[1]);
    rasmgrPort = strtoul(av[2], NULL, 0);
    strcpy(baseName,   av[3]);
    strcpy(userName,   av[4]);
    strcpy(userPass,   av[5]);

    string collName             = string("mr");
    r_Minterval select_domain   = r_Minterval("[0:4,0:4]");
    r_Minterval where_domain    = r_Minterval("[8:9,8:9]");
    r_ULong     threshold_value = 0;

    r_Database database;
    r_Transaction transaction{&database};
    r_Set<r_Ref<r_GMarray>> image_set;
    r_Ref<r_GMarray> image;
    r_Iterator<r_Ref<r_GMarray>> iter;

    try
    {
        database.set_servername(rasmgrName, rasmgrPort);
        database.set_useridentification(userName, userPass);

        cout << "Opening database " << baseName
             << " on " << rasmgrName << "... " << flush;

        database.open(baseName);
        cout << "OK" << endl;

        cout << "Starting read-only transaction ... " << flush;
        transaction.begin(r_Transaction::read_only);
        cout << "OK" << endl;

        cout << "Creating the query object ..." << flush;
        r_OQL_Query query("select a$1 from $2 as a where some_cells( a$3 >= $4 )");
        cout << "OK, Query string is: " << query.get_query() << endl;

        cout << "Substituting query parameters ..." << flush;
        query << select_domain << collName.c_str() << where_domain << threshold_value;
        cout << "OK, Query string is: " << query.get_query() << endl;

        cout << "Executing the query ..." << flush;
        try
        {
            r_oql_execute(query, image_set, &transaction);
        }
        catch (r_Error& errorObj)
        {
            cout << "FAILED" << endl << errorObj.what() << endl;

            cout << "Aborting transaction ... " << flush;
            transaction.abort();
            cout << "OK" << endl;

            cout << "Closing database ... " << flush;
            database.close();
            cout << "OK" << endl;
            return -1;
        }
        cout << "OK" << endl << endl;

        cout << "Collection" << endl;
        cout << "  Oid...................: " << image_set.get_oid() << endl;
        if (image_set.get_object_name())
        {
            cout << "  Type Name.............: " << image_set.get_object_name() << endl;
        }
        cout << "  Type Structure........: "
             << (image_set.get_type_structure() ? image_set.get_type_structure() : "<nn>")
             << endl;
        cout << "  Type Schema...........: " << flush;
        if (image_set.get_type_schema())
        {
            image_set.get_type_schema()->print_status(cout);
        }
        else
        {
            cout << "<nn>" << flush;
        }
        cout << endl;
        cout << "  Number of entries.....: " << image_set.cardinality() << endl;
        cout << "  Element Type Schema...: " << flush;
        if (image_set.get_element_type_schema())
        {
            image_set.get_element_type_schema()->print_status(cout);
        }
        else
        {
            cout << "<nn>" << flush;
        }
        cout << endl << endl;

        iter = image_set.create_iterator();

        int i;
        for (i = 1, iter.reset(); iter.not_done(); iter++, i++)
        {
            cout << "Image " << i << endl;
            image = *iter;
            image->print_status(cout);
            cout << endl;
        }
        cout << endl;

        cout << "Committing transaction ... " << flush;
        transaction.commit();
        cout << "OK" << endl;

        cout << "Closing database ... " << flush;
        database.close();
        cout << "OK" << endl;
    }
    catch (r_Error& errorObj)
    {
        cerr << errorObj.what() << endl;
        return -1;
    }

    return 0;
}

/*
 * end of query.cc
 */
