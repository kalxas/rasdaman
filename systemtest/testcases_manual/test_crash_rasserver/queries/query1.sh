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
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community. If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

#
# Test update
#

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
QUERY_SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

COLL_NAME="test_updating"

#
# Executed to prepare the query execution. Always executed on a freshly created RASBASE.
#
pre_query()
{
  $RASQL -q "create collection $COLL_NAME DoubleSet3" > /dev/null
  $RASQL --quiet -q "insert into $COLL_NAME values marray i in [0:0,0:0,0:0] values 0d tiling aligned [0:9,0:9,0:4]" > /dev/null
}

#
# The actual query to be tested
#
run_query()
{
  local -r q=$(get_query)
  log "  testing query: '$q'"
  $RASQL --quiet -q "update $COLL_NAME as m set m[1,*:*,*:*] assign (double) \$1" -f "$QUERY_SCRIPT_DIR"/400k.bin --mdddomain [0:499,0:99] --mddtype DoubleImage > /dev/null
}

#
# Print the query to be tested
#
get_query()
{
  echo "update $COLL_NAME as m set m[1,*:*,*:*] assign \$1"
}
