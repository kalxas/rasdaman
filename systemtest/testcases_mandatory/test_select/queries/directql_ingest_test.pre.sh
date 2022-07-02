#!/bin/bash
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
x=test_dql_ingest

datadir=$RASDATA
[ -z "$RASDATA" ] && datadir=$RMANHOME/data

DIRECTQL="$RMANHOME/bin/rasserver --user rasadmin --passwd rasadmin"
$DIRECTQL -q "create collection $x GreySet3" > /dev/null
$DIRECTQL -q "insert into $x values \$1" -f "../testdata/50k.bin" \
                                         --mdddomain "[0:99,0:99,0:4]" \
                                         --mddtype GreyCube > /dev/null
