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
out="$1"
oracle="$2"

# remove the "oid" line from the output and oracle as it can be different
cut -d ' ' -f1-6,8- "$out" > "$out".tmp
cut -d ' ' -f1-6,8- "$oracle" > "$oracle".tmp

LINE=$(wc -l "$out".tmp | awk '{ print $1 }')
# remove opening and closing tag
cat "$out".tmp | head -n -1 | tail -n -$((LINE-2)) > "$out".tmptmp && mv "$out".tmptmp "$out".tmp
cat "$oracle".tmp | head -n -1 | tail -n -$((LINE-2)) > "$oracle".tmptmp && mv "$oracle".tmptmp "$oracle".tmp
# sort the rows. (If they contain same rows, then they gonna get sorted in the same way)
sort -i "$out".tmp >/dev/null
sort -i "$oracle".tmp >/dev/null

# diff
diff -b "$out".tmp "$oracle".tmp > /dev/null 2>&1

rc=$?

rm -f "$out".tmp* "$oracle".tmp*
exit $rc
