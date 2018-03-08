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

# create tmp files
cp "$out" "$out".tmp
cp "$oracle" "$oracle".tmp

# remove variable lines
for file in "$out".tmp "$oracle".tmp
do
  sed -i '/<wcs:CoverageSummary>/,/<\/wcs:CoverageSummary>/d' "$file"
  sed -i 's/<\(wcs:\)\?formatSupported>.*<\/\(wcs:\)\?formatSupported>/%formatSupported%/g' "$file" # it's a Set in Java, order is not fixed.
  sed -i '/<ows:HTTP>/,/<\/ows:HTTP>/d' "$file"
done
sort "$out".tmp > "$out".tmp2
sort "$oracle".tmp > "$oracle".tmp2

diff -b "$out".tmp2 "$oracle".tmp2 > /dev/null 2>&1
rc=$?
cp "$out".tmp "$out" # for post-test manual verifications
rm -f "$out".tmp* "$oracle".tmp*

exit $rc
