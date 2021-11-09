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

nout="$out.output_tmp"
nora="$out.oracle_tmp"

# create tmp files
cp "$out" "$nout"
cp "$oracle" "$nora"

# remove variable lines
for file in "$nout" "$nora"
do
  prepare_xml_file "$file"
  sed -i -e '/<wcs:CoverageSummary>/,/<\/wcs:CoverageSummary>/d' \
         -e 's/<\(wcs:\)\?formatSupported>.*<\/\(wcs:\)\?formatSupported>/%formatSupported%/g' \
         -e '/<ows:HTTP>/,/<\/ows:HTTP>/d' \
         -e '/<ows:ExtendedCapabilities>/,/<\/ows:ExtendedCapabilities>/d' \
         "$file"
done

diff -b "$nout" "$nora" > /dev/null 2>&1
exit $?
