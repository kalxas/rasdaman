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
# SYNOPSIS
#  test.sh
# Description
#  Test type coercion in rasql operations.
#
# Usage: ./test.sh
#

PROG=$(basename $0)

SOURCE="${BASH_SOURCE[0]}"
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
SYSTEST_DIR=$(echo "$SCRIPT_DIR" | sed 's|\(.*/systemtest\).*|\1|')
[ -d "$SYSTEST_DIR/util" ] || error "could not determine system test dir: $SYSTEST_DIR"
. "$SYSTEST_DIR/util/common.sh"

SVC_NAME=$(basename $SCRIPT_DIR | cut -d "_" -f 2)
log "Testing service: $SVC_NAME"

TESTDATA_PATH="$SCRIPT_DIR/testdata"
QUERIES_PATH="$SCRIPT_DIR/queries"
[ -d "$QUERIES_PATH" ] || error "Queries directory not found: $QUERIES_PATH"
ORACLE_PATH="$SCRIPT_DIR/oracle"
[ -d "$ORACLE_PATH" ] || error "Oracles directory not found: $ORACLE_PATH"
OUTPUT_PATH="$SCRIPT_DIR/output"
KNOWN_FAILS="$SCRIPT_DIR/known_fails"

cleanup() { print_summary; exit_script; }
trap cleanup SIGINT

#
# helpers
#

readonly types=(b c o us s ul l f d cint16 cint32 complex complexd)
readonly full_types=(bool char octet ushort short ulong long float double cint16 cint32 complex complexd)
readonly types_count=${#types[@]}

# $1 is a type from the types array above
# echo a scalar constant matching the given type.
get_cell_value() {
  local type="$1"
  local ret=
  case $type in
    b)        ret="true";;
    cint16)   ret="complex(1s,2s)";;
    cint32)   ret="complex(1,2)";;
    complex)  ret="complex(1f,2f)";;
    complexd) ret="complex(1d,2d)";;
    *)        ret="1$type";;
  esac
  echo "$ret"
}

# $expr, $i, and $j come from the main loop
instantiate_template() {
  local first=$(get_cell_value "${types[$i]}")
  local second=$(get_cell_value "${types[$j]}")
  echo "$expr" | sed -e "s|first|<[0:0] $first>|" -e "s|second|<[0:0] $second>|"
}
execute_query() {
  query=$(instantiate_template)
  restype=$(rasql -q "$query" --out file --outfile "$OUTPUT_PATH/tmp" --type 2>/dev/null | strings \
            | grep Element | sed 's/.*: //' | sed 's/marray< //' | sed 's/ *>//')
  restype=$(echo "$restype" | sed -e 's/ [0-9]//g' | tr -d '[[:space:]]' | \
    sed -e 's/struct//g' \
        -e 's/complex(short,short)/cint16/g' \
        -e 's/complex(long,long)/cint32/g' \
        -e 's/complex(float,float)/complex/g' \
        -e 's/complex(double,double)/complexd/g')
}
execute_testcase() {
  local outfile="$OUTPUT_PATH/$f"
  local orafile="$ORACLE_PATH/$f"

  expr=$(cat "$f" | tr -d '\n')

  local binary=false
  local offset="%10s"
  local resoffset="%20s"
  local queryspace="   "
  if echo "$expr" | grep -q 'second'; then
    binary=true
    printf "$offset$offset$resoffset${queryspace}query\n" first second result
  else
    binary=false
    printf "$offset$resoffset${queryspace}query\n" operand result
  fi > "$outfile"
  echo "----------------------------------------------------------------------------" >> "$outfile"

  for (( i = 0; i < $types_count; i++ )); do
    if [ $binary = true ]; then
      for (( j = 0; j < $types_count; j++ )); do
        execute_query
        printf "$offset$offset$resoffset${queryspace}\"$query\"\n" "${full_types[$i]}" "${full_types[$j]}" "${restype}"
      done
    else
      execute_query
      printf "$offset$resoffset${queryspace}\"$query\"\n" "${full_types[$i]}" "${restype}"
    fi
  done >> "$outfile"

  if [ ! -f "$orafile" ]; then
    status=$ST_COPY
    cp "$outfile" "$orafile"
  fi

  diff -b "$outfile" "$orafile" > /dev/null 2>&1
  update_result
}

#
# work
#

check_rasdaman && check_rasdaman_available

test_single_file="$1"


if [ -n "$test_single_file" ]; then
  [ -f "$QUERIES_PATH/$test_single_file" ] || error "$test_single_file not found."
else
  rm -rf "$OUTPUT_PATH"
  mkdir -p "$OUTPUT_PATH"
fi

pushd "$QUERIES_PATH" > /dev/null

total_test_no=$(ls | wc -l)
curr_test_no=0

for f in *; do

  [ -f "$f" ] || continue # skip non-files

  curr_test_no=$(($curr_test_no + 1))

  if [ -n "$test_single_file" ]; then
    [[ "$f" == "$test_single_file" ]] || continue
  fi

  start_timer
  execute_testcase
  stop_timer

  # print result of this test case
  print_testcase_result "$f" "$status" "$total_test_no" "$curr_test_no"

done

popd > /dev/null

cleanup
