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
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#	test.sh
# Description
#	Command-line utility for testing rasdaman.
#	1)Testing Makefile for C++
#	2)Testing Makefile for Java
#
# Usage: ./test.sh
#
# CHANGE HISTORY
#    2018-Jul-02   D.Kamov    created

# Variables
PROG=`basename $0`

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

rm -rf $SCRIPT_DIR/output/
mkdir -p $SCRIPT_DIR/output/

logn "testing Makefile for C++... "
cd "$RMANHOME/share/rasdaman/examples/c++/"
make &>"$SCRIPT_DIR/output/make_cpp.log"
check


check_java_enabled
run_java_test=$?

if [ "$run_java_test" -eq 0 ]; then
    logn "testing Makefile for Java... "
    cd "$RMANHOME/share/rasdaman/examples/java/"

    # Compile all java files to class files 
    make all &>"$SCRIPT_DIR/output/make_java.log"
    # then run these files
    for file in *.java; do
        className="${file%.*}" # remove extension
        make $className &> "$SCRIPT_DIR/output/make_${className}.log"
        check
    done
    # remove built class files
    make clean &>"$SCRIPT_DIR/output/make_java.log"
fi

# ------------------------------------------------------------------------------
# test summary
#
print_summary
exit $RC
