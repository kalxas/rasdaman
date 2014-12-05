#!/bin/bash
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

c=test_nan

$RASQL -q "create collection $c FloatSet1" > /dev/null
$RASQL -q "insert into $c values marray i in [0:20] values NAN" > /dev/null
