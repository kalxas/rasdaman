#!/bin/bash
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
x=test_dql_ingest

directql -q "create collection $x GreySet3" --user rasadmin --passwd rasadmin -d $RMANHOME/data/RASBASE > /dev/null
directql -q "insert into $x values \$1" -f "../testdata/50k.bin" --mdddomain "[0:99,0:99,0:4]" --mddtype GreyCube --user rasadmin --passwd rasadmin -d $RMANHOME/data/RASBASE > /dev/null