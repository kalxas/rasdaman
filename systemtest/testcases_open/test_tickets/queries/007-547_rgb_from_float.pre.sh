#!/bin/bash
#
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

TYPES=/tmp/types.dl

echo "struct FloatRGBPixel { float red, green, blue; };" > $TYPES
echo "typedef marray <FloatRGBPixel, 2> FloatPixelArray;" >> $TYPES
echo "typedef set <FloatPixelArray> FloatPixelSet;" >> $TYPES

rasdl -r $TYPES -i > /dev/null

rm -f $TYPES

c=test_float

$RASQL -q "create collection $c FloatPixelSet" > /dev/null
$RASQL -q "insert into $c values marray i in [0:20,0:20] values {100.0f, 50.0f, 20.0f}" > /dev/null
