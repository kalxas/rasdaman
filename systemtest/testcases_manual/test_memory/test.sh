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
#


PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh
. "$SCRIPT_DIR"/../../util/rasql.sh

ingest_data()
{
    [ "$INGEST_DATA" == "false" ] && return # --no-ingest

    log "Ingesting data needed for the test..."
    log ""

    local TESTDATA_PATH="$SCRIPT_DIR/../../testcases_mandatory/test_select/testdata"
    import_rasql_data "$TESTDATA_PATH"
    import_nullvalues_data
    import_subsetting_data "$TESTDATA_PATH"
    if [ -e "$TESTDATA_PATH/complex.binary" ] ; then
        check_type Gauss2Set
        drop_colls $TEST_COMPLEX
        create_coll $TEST_COMPLEX Gauss2Set
        insert_into $TEST_COMPLEX "$TESTDATA_PATH/complex.binary" "" "" "--mddtype Gauss2Image --mdddomain [0:7,0:7]"
    fi
}

# ------------------------------------------------------------------------------

INGEST_DATA=true
for i in $*; do
  case $i in
    --no-ingest) INGEST_DATA=false;;
    *) error "unknown option: $i"
  esac
done

ingest_data

log "Running memory tests..."
log ""

pushd "$SCRIPT_DIR" > /dev/null

for testdir in test_*; do

    queriesdir="$testdir/queries"
    [ -d "$queriesdir" ] || continue # skip invalid testdirs (that have no queries)

    outputdir="$testdir/output"
    rm -rf "$outputdir"
    mkdir -p "$outputdir"

    # for eqch query file
    for queryfile in $queriesdir/*; do

        QUERY=$(cat "$queryfile")

        loge "------------------------------------------------------------------------------------"
        loge "Query  : $queryfile"

        ignoreoutput="$outputdir/tmp"
        queryname=$(basename "$queryfile")
        stdout="$outputdir/$queryname"
        loge "Output : $stdout"

        ESCAPED_QUERY=$(echo "$QUERY" | sed 's/"/\\"/g')
        ESCAPED_DIRECTQL=$(echo "$DIRECTQL" | tr -s "[[:space:]]")
        loge "Command: $VALGRIND $ESCAPED_DIRECTQL -q \"$ESCAPED_QUERY\" --out file --outfile $ignoreoutput > $stdout"
        $VALGRIND $DIRECTQL -q "$QUERY" --out file --outfile "$ignoreoutput" > "$stdout" 2>&1

        # LOG_FILE is defined in common.sh = test.log
        loge "Valgrind summary:"
        grep ' lost: ' "$stdout" | grep -v "possibly" \
             | sed 's/^==.*==/        /' | tee -a "$LOG_FILE"
        grep "ERROR SUMMARY" "$stdout" \
             | sed 's/^==.*==/        /' | tee -a "$LOG_FILE"
    done

done

popd > /dev/null

log "Done."
