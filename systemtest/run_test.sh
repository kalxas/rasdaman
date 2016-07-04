#!/bin/bash

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
# Copyright 2003-2016 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

if [ $# -ne 1 ]; then
    echo Usage: $0 "rasdaman_bin_directory.";
    exit 1;
fi

TEST_SEQUENCE=`ls -d testcases_mandatory/* testcases_open/* testcases_fixed/* testcases_services/*`
BIN_DIR=$1;

ret=0;
for test_case in $TEST_SEQUENCE; do
    PATH=$BIN_DIR:$PATH
    PROG_TEST=$test_case/*.sh;
    echo $PROG_TEST
    if [ ! -f $test_case/*.sh ]; then
        continue;
    fi;
    echo;
    echo;
    $test_case/*.sh;
    tmp=$?;
    if [ $tmp -eq 2 ]; then
        echo $PROG_TEST ... SKIPPED>>logtmp;
    elif [ $tmp -ne 0 ]; then
        ret=$tmp;
        echo $PROG_TEST ... FAIL>>logtmp;
    else
        echo $PROG_TEST ... OK>>logtmp;
    fi;
        echo "	"see detail in $test_case/log>>logtmp;
done;

echo -e "\n\nTEST SUMMARY\n";
cat logtmp;
rm logtmp;

exit $ret;