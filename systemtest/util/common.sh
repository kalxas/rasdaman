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
# ------------------------------------------------------------------------------
#
# SYNOPSIS
#    common.sh
# Description
#    Common functionality used by test scripts, like
#   - shortcuts for commands
#   - logging functions
#   - various check functions
#   - test data import
#   - generic test case runner
#
################################################################################

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
UTIL_SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$UTIL_SCRIPT_DIR"/../conf/test.cfg


# --------------------------------------------------------
# command shortcuts; variables configured in conf/test.cfg
#
export RASQL_OPTS="--server $RASMGR_HOST --port $RASMGR_PORT --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD"
export RASQL_OPTS_GUEST="--server $RASMGR_HOST --port $RASMGR_PORT --user $RASMGR_GUEST_USER --passwd $RASMGR_GUEST_PASSWD"
export RASQL="rasql --server $RASMGR_HOST $RASQL_OPTS"
export RASQL_GUEST="rasql --server $RASMGR_HOST $RASQL_OPTS_GUEST"
export PY_RASQL="$SCRIPT_DIR/rasql.py $RASQL_OPTS --database $RASDB"
export DIRECTQL="directql --user $RASMGR_ADMIN_USER --passwd $RASMGR_ADMIN_PASSWD --connect $RASDB"
export RASCONTROL="rascontrol --host $RASMGR_HOST --port $RASMGR_PORT"

export START_RAS=start_rasdaman.sh
export STOP_RAS=stop_rasdaman.sh
export CREATE_DB=create_db.sh

export GDALINFO="gdalinfo -noct -checksum"
export VALGRIND="valgrind --tool=memcheck --leak-check=full --track-origins=yes"

# filestorage
export DB_DIR="/tmp/rasdb"
export LOG_DIR="$RMANHOME/log"
export RASMGR_CONF="$RMANHOME/etc/rasmgr.conf"
export RASSERVER_COUNT="$(grep 'define srv ' "$RASMGR_CONF" -c)"
export PETASCOPE_PROPERTIES_FILE="$RMANHOME/etc/petascope.properties"

# Run up to 8 test queries in parallel
PARALLEL_QUERIES=4
if [ $PARALLEL_QUERIES -gt "$RASSERVER_COUNT" ]; then
    PARALLEL_QUERIES=$RASSERVER_COUNT
fi
export PARALLEL_QUERIES
# if number of background jobs does not decrease within 120 seconds, the test
# will terminate with an error.
export WAIT_TIMEOUT_SEC=120


# -------------------
# script return codes
#
RC_OK=0
RC_ERROR=1
RC_SKIP=2

# ----------------
# test case status
#
ST_PASS=OK    # test passed
ST_FAIL=FAIL  # test failed
ST_SKIP=SKIP  # test failed, and also marked in known_fails so it was skipped
ST_FIX=FIX    # marked as known_fail but actually passes, i.e. it was fixed
ST_COPY=COPY  # oracle not found = copy output file to the oracle

# ------------------
# testing statistics
#
NUM_TOTAL=0 # the number of tests
NUM_FAIL=0  # the number of failed tests
NUM_SUC=0   # the number of successful tests

# ----------------------
# testing data
# TODO: move to rasql.sh
#
TEST_STRUCT=test_struct
TEST_GREY=test_grey
TEST_GREY2=test_grey2
TEST_RGB2=test_rgb2
TEST_GREY3D=test_grey3d
TEST_GREY3D_EMPTY_IN_MIDDLE=test_grey3d_empty_in_middle
TEST_GREY4D=test_grey4d
TEST_COMPLEX=test_complex
TEST_CFLOAT32=test_cfloat32
TEST_CFLOAT64=test_cfloat64
TEST_CINT16=test_cint16
TEST_CINT32=test_cint32
TEST_NULL=nulltest
TEST_NULL_FLOAT=test_nulltest_float
TEST_GREY_NULL=test_grey_null
TEST_NULL3D=test_nulltest3d
TEST_NULL3D_HOLES=test_nulltest3d_holes
TEST_SUBSETTING_1D=test_subsetting_1d
TEST_SUBSETTING=test_subsetting
TEST_SUBSETTING_SINGLE=test_subsetting_single
TEST_SUBSETTING_3D=test_subsetting_3d
TEST_SUBSETTING_HOLES=test_subsetting_holes
TEST_OVERLAP=test_overlap
TEST_OVERLAP_3D=test_overlap_3d
TEST_INSITU_BIN=test_insitu_bin
TEST_DWD_TX24=test_DWD_TX24
TEST_DWD_NIEDERSCHLAG=test_DWD_Niederschlag
# ------------------------------------------------------------------------------
# OS version; the current os can be determined with the get_os function
#
OS_UNKNOWN="unknown"
OS_CENTOS7="centos7"        # CentOS 7.x
OS_CENTOS8="centos8"        # CentOS 8.x
OS_DEBIAN8="debian8"        # Debian 8
OS_DEBIAN9="debian9"        # Debian 9
OS_DEBIAN10="debian10"      # Debian 10 (buster)
OS_DEBIAN11="debian11"      # Debian 11 (bullseye)
OS_UBUNTU1404="ubuntu1404" 
OS_UBUNTU1410="ubuntu1410"
OS_UBUNTU1504="ubuntu1504" 
OS_UBUNTU1510="ubuntu1510"
OS_UBUNTU1604="ubuntu1604" 
OS_UBUNTU1610="ubuntu1610"
OS_UBUNTU1704="ubuntu1704" 
OS_UBUNTU1710="ubuntu1710"
OS_UBUNTU1804="ubuntu1804"
OS_UBUNTU1810="ubuntu1810"
OS_UBUNTU1904="ubuntu1904"
OS_UBUNTU1910="ubuntu1910"
OS_UBUNTU2004="ubuntu2004"
OS_UBUNTU2204="ubuntu2204"

# ------------------------------------------------------------------------------
# logging
#

LOG_FILE="$SCRIPT_DIR/test.log"
FAILED_LOG_FILE="$SCRIPT_DIR/failed_cases.log"
SVC_NAME=

# ---------------------------
# terminal color escape codes
#
C_OFF="\e[0m"
C_BOLD="\e[1m"
C_UNDERLINE="\e[4m"
# color text is also bold for better visibility
C_RED="$C_BOLD\e[31m"
C_GREEN="$C_BOLD\e[32m"
C_YELLOW="$C_BOLD\e[33m"

# print the color passed as an argument only if it's a terminal
get_color() { [[ -t 0 || -t 1 || -t 2 ]] && echo "$1"; }

# colors set properly depending on whether the test is run in a terminal
# (rather than output redirected for example)
c_off=$(get_color "$C_OFF")
c_bold=$(get_color "$C_BOLD")
c_underline=$(get_color "$C_UNDERLINE")
c_green=$(get_color "$C_GREEN")
c_red=$(get_color "$C_RED")
c_yellow=$(get_color "$C_YELLOW")

# $1: test status
# stdout: $color_on and $color_on if $1 != $ST_PASS
get_status_color()
{
  [ "$1" == $ST_PASS ] && return # pass, no special color
  case "$1" in
    $ST_FAIL) echo "$c_red";;
    *)        echo "$c_yellow";;
  esac
}

# log as is to stdout, but remove colors from file output
log_colored()
{
  echo -e "$PROG: $*"
  echo -e "$PROG: $*" | sed -r "s/[[:cntrl:]]\[[0-9]{1,3}m//g" >> "$LOG_FILE"
}
log_colored_failed()
{
  echo -e "$PROG: ${c_red}$*${c_off}"
  echo -e "$PROG: $*" >> "$LOG_FILE"
}
loge_colored()
{
  echo -e "$*"
  echo -e "$*" | sed -r "s/[[:cntrl:]]\[[0-9]{1,3}m//g" >> "$LOG_FILE"
}
loge_colored_failed()
{
  echo -e "${c_red}$*${c_off}"
  echo -e "$*" >> "$LOG_FILE"
}

# normal log
log()   { echo -e "$PROG: $*" | tee -a "$LOG_FILE"; }
loge()  { echo -e "$*" | tee -a "$LOG_FILE"; }
logn()  { echo -n -e "$PROG: $*" | tee -a "$LOG_FILE"; }
error() { log_colored_failed "$*"; log_colored_failed "exiting."; exit $RC_ERROR; }
log_failed() { echo "$PROG: $*" >> "$FAILED_LOG_FILE"; }

feedback()   { if [ $? -ne 0 ]; then loge_colored_failed failed.; else loge ok.; fi; }
check_exit() {
  if [ $? -ne 0 ]; then
    log_colored_failed "failed, exiting."
    exit $RC_ERROR
  else
    loge ok.
  fi
}
# test status -> return code (e.g: OK -> 0)
get_return_code() { [ "$1" = "$ST_PASS" ] && return "$RC_OK" || return "$RC_ERROR"; }

# ---------
# setup log
#
if [ -n "$SCRIPT_DIR" ] ; then
  rm -f "$LOG_FILE" "$FAILED_LOG_FILE"
  log "starting test at $(date)"
  log ""
fi

# setup python binary
PYTHONBIN=
for b in python3 python python2; do
  if $b --version > /dev/null 2>&1; then
    PYTHONBIN=$b; break;
  fi
done
[ -n "$PYTHONBIN" ] || error "python/python2/python3 not found, please install python first."
export PYTHONBIN

# ------------------------------------------------------------------------------
# manage timing, in ms
# @global variable: timer_start
#
start_timer(){ timer_start=$(date +%s%N); }
stop_timer() { timer_stop=$(date +%s%N); }
# ms
get_time()   { echo "scale=2; ($timer_stop - $timer_start) / 1000000.0" | bc; }
# s
get_time_s() { echo "scale=2; ($timer_stop - $timer_start) / 1000000000.0" | bc; }

# set a global timestamp when this file is loaded by a test script
# @global variable: total_timer_start
#
start_timer
total_timer_start="$timer_start"


#
# Print the test result of a test with elapsed time format
# e.g: test.sh:   1/103    OK   .81s   3D_Timeseries_Regular
#
# $1 test case name
# $2 result of test case (OK/FAIL)
# $3 total number of test cases
# $4 index of test case in list of test cases
print_testcase_result() {
  local test_case_name=$1
  local status=$2
  local total_test_no=$3
  local curr_test_no=$4
  local c_on
  c_on=$(get_status_color "$status")
  local msg
  msg=$(printf "%3d/$total_test_no ${c_on}%5s${c_off} %5ss   $test_case_name\n" "$curr_test_no" "$status" "$(get_time_s)")
  log_colored "$msg"
}


# ------------------------------------------------------------------------------
# OS mgmt
#

# @global variable: OS_VERSION (one of the $OS_* values)
get_os()
{
  OS_VERSION=$OS_UNKNOWN
  if [ -f "/etc/centos-release" ]; then
    grep -q "CentOS Linux release 7" /etc/centos-release
    [ $? -eq 0 ] && OS_VERSION=$OS_CENTOS7
    grep -q "CentOS Linux release 8" /etc/centos-release
    [ $? -eq 0 ] && OS_VERSION=$OS_CENTOS8
  else
    local version
    version=$(lsb_release -a 2>&1 | grep Description \
      | sed 's/Description: *//' | tr -d '[:space:]')

    case "$version" in
      Ubuntu14.0*)       OS_VERSION=$OS_UBUNTU1404;;
      Ubuntu14.1*)       OS_VERSION=$OS_UBUNTU1410;;
      Ubuntu15.0*)       OS_VERSION=$OS_UBUNTU1504;;
      Ubuntu15.1*)       OS_VERSION=$OS_UBUNTU1510;;
      Ubuntu16.0*)       OS_VERSION=$OS_UBUNTU1604;;
      Ubuntu16.1*)       OS_VERSION=$OS_UBUNTU1610;;
      Ubuntu17.0*)       OS_VERSION=$OS_UBUNTU1704;;
      Ubuntu17.1*)       OS_VERSION=$OS_UBUNTU1710;;
      Ubuntu18.0*)       OS_VERSION=$OS_UBUNTU1804;;
      Ubuntu18.1*)       OS_VERSION=$OS_UBUNTU1810;;
      Ubuntu19.0*)       OS_VERSION=$OS_UBUNTU1904;;
      Ubuntu19.1*)       OS_VERSION=$OS_UBUNTU1910;;
      Ubuntu20.0*)       OS_VERSION=$OS_UBUNTU2004;;
      Ubuntu22.0*)       OS_VERSION=$OS_UBUNTU2204;;
      UbuntuJammy*)      OS_VERSION=$OS_UBUNTU2204;;
      Debian*8*)         OS_VERSION=$OS_DEBIAN8;;
      Debian*9*)         OS_VERSION=$OS_DEBIAN9;;
      Debian*buster*)    OS_VERSION=$OS_DEBIAN10;;
      Debian*bullseye*)  OS_VERSION=$OS_DEBIAN11;;
    esac
  fi
  if [ "$OS_VERSION" != $OS_UNKNOWN ]; then
    OS_VERSION_NUMBER="$(echo "$OS_VERSION" | sed 's/[a-z]*//g')"
    OS_NAME="$(echo "$OS_VERSION" | sed 's/[0-9]*//g')"
  fi
}

# determine $OS_VERSION on startup
get_os

# ------------------------------------------------------------------------------
# dependency checks
#
check_rasdaman()
{
  type rasmgr &> /dev/null || error "rasdaman not installed, please add rasdaman bin directory to the PATH."
  pgrep rasmgr &> /dev/null || error "rasdaman not started, please start with start_rasdaman.sh"
  $RASCONTROL -x 'list srv -all' &> /dev/null || error "no rasdaman servers started."
}
check_rasdaman_available()
{
  # check if rasdaman is running and exit if not
  if ! $RASQL -q 'select c from RAS_COLLECTIONNAMES as c' --out string &> /dev/null; then
    # retry test
    sleep 2
    if ! $RASQL -q 'select c from RAS_COLLECTIONNAMES as c' --out string &> /dev/null; then
        log "rasdaman down, exiting..."
        # cleanup if cleanup function is defined
        if declare -f "cleanup" &> /dev/null; then
            cleanup
        else
            exit $RC_ERROR
        fi
    fi
  fi
}

check_postgres()
{
  type psql &> /dev/null || error "PostgreSQL missing, please add psql to the PATH."
  if ! pgrep postgres &> /dev/null; then
    pgrep postmaster > /dev/null || error "The PostgreSQL service is not started."
  fi
}
check_curl()
{
  type curl &> /dev/null || error "curl missing, please install."
}
check_petascope()
{
  if ! curl -sL "$PETASCOPE_URL" -o /dev/null; then
    log "failed connecting to Petascope at $PETASCOPE_URL, please deploy it first."
    return 1
  fi
  return 0
}
check_secore()
{
  if ! curl -sL "$SECORE_URL/crs" -o /dev/null; then
    log "failed connecting to SECORE at $SECORE_URL, please deploy it first."
    return 1
  fi
  return 0
}
check_netcdf()
{
  type ncdump &> /dev/null || error "netcdf tools missing, please add ncdump to the PATH."
}
check_gdal()
{
  type gdal_translate &> /dev/null || error "gdal missing, please add gdal_translate to the PATH."
}

# Check if GDAL version is greater-equal than specified (M.m)
# usage: $0 <major> <minor>
# return
#   0 - if GDAL version is >= <major>.<minor>
#   1 - otherwise
check_gdal_version()
{
  GDAL_VERSION="$( $GDALINFO --version | awk -F ' |,' '{ print $2 }' | grep -o -e '[0-9]\+.[0-9]\+' )" # M.m version
  GDAL_VERSION_MAJOR=$(echo $GDAL_VERSION | awk -F '.' '{ print $1; }')
  GDAL_VERSION_MINOR=$(echo $GDAL_VERSION | awk -F '.' '{ print $2; }')
  [[ $GDAL_VERSION_MAJOR -gt 1 || ( $GDAL_VERSION_MAJOR -eq $1 && $GDAL_VERSION_MINOR -ge $2 ) ]] && echo 0 || echo 1
}

check_jpeg2000_enabled()
{
  # check if gdal supports JP2OpenJPEG then run test cases with encode in this format
  gdalinfo --formats | grep JP2OpenJPEG &> /dev/null
  if [ $? -ne 0 ]; then
    log "skipping test for GMLJP2 encoding."
    return 1
  fi
  return 0
}

# check if query should be run (e.g: JPEG2000)
check_query_runable()
{
  if [[ "$1" == *"jp2"* || "$1" == *"jpeg2000"*  || "$1" == *"jp2openjpeg"* ]]; then
    # call the function and get the return in integer by $?
    check_jpeg2000_enabled
    return $?
  fi
  return 0
}

check_filestorage_dependencies()
{
  [ -f "$RASMGR_CONF" ] || error "$RASMGR_CONF not found, RMANHOME not defined properly?"
  type sqlite3 &> /dev/null || error "sqlite3 not found, please install."
}

# Check if rasdaman was built with -DENABLE_JAVA=ON
check_java_enabled() {
  if [ "$ENABLE_JAVA" == "ON" ]; then
    return 0
  else
    log "Test cannot be executed as compilation of Java components in rasdaman is disabled."
    log "To enable it, please run cmake again with -DENABLE_JAVA=ON, followed by make and make install."
    return 1
  fi
}

# --------- properties

# return the value of key=value from properties file ($1: the key, $2: the file)
get_key_value()
{    
    key_value=$(grep -E "^$1=" "$2") && echo ${key_value#*=} || echo ""
}

export RASADMIN_USER="$(get_key_value "rasdaman_admin_user" "$PETASCOPE_PROPERTIES_FILE")"
export RASADMIN_PASS="$(get_key_value "rasdaman_admin_pass" "$PETASCOPE_PROPERTIES_FILE")"

# credentials files for wcst_import to authenticate to petascope
export RASADMIN_CREDENTIALS_FILE="/tmp/rasadmin_credentials.txt"
echo "$RASADMIN_USER:$RASADMIN_PASS" > "$RASADMIN_CREDENTIALS_FILE"
chmod 600 "$RASADMIN_CREDENTIALS_FILE"

export WCST_IMPORT="wcst_import.sh -i $RASADMIN_CREDENTIALS_FILE"
export WGET="wget --auth-no-challenge --user $RASADMIN_USER --password $RASADMIN_PASS"
export CURL="curl -u $RASADMIN_USER:$RASADMIN_PASS -s"

# ------------------------------------------------------------------------------
# print test summary
# exports $RC - return code that the caller can use in: exit $RC
#
print_summary()
{
  if [ $NUM_TOTAL -eq 0 ]; then
    NUM_TOTAL=$((NUM_FAIL + NUM_SUC))
  fi

  local test_name="$0"
  if [ -n "$SVC_NAME" ]; then
    test_name="$SVC_NAME"
  elif [ -n "$SCRIPT_DIR" ]; then
    test_name="$(basename "$SCRIPT_DIR")"
  fi

  log
  log "-------------------------------------------------------"
  log "Test summary for $test_name"
  log ""
  log "  Test finished at: $(date)"
  if [ -n "$total_timer_start" ]; then
  timer_start="$total_timer_start"
  stop_timer
  log "  Elapsed time    : $(get_time_s)s"
  fi
  log "  Total tests run : $NUM_TOTAL"
  if [ $NUM_SUC -gt 0 ]; then
  log "  Successful tests: $NUM_SUC"
  fi
  if [ $NUM_FAIL -gt 0 ]; then
  log_colored_failed "  Failed tests    : $NUM_FAIL"
  fi
  local skipped_tests=$((NUM_TOTAL - (NUM_FAIL + NUM_SUC)))
  if [ $skipped_tests -gt 0 ]; then
  log "  Skipped tests   : $skipped_tests"
  fi
  log "  Detail test log : $LOG_FILE"
  log "-------------------------------------------------------"

  if [ $NUM_FAIL -ne 0 ]; then
    RC=$RC_ERROR
  else
    RC=$RC_OK
  fi
}

#
# check if result matches expected result, automatically updating the number of
# failed/successfull tests.log
# arg 1: expected result
# arg 2: actual result
# arg 3: message to print
#
check_result()
{
  local exp="$1"
  local res="$2"
  local msg="$3"

  [ -n "$msg" ] && logn "$msg... "
  if [ "$exp" != "$res" ]; then
    NUM_FAIL=$((NUM_FAIL + 1))
    loge_colored_failed "failed, expected: '$exp', got '$res'."
  else
    NUM_SUC=$((NUM_SUC + 1))
    loge "ok."
  fi
  NUM_TOTAL=$((NUM_TOTAL + 1))
}
#
# check if result matches expected result, automatically updating the number of
# failed/successfull tests.log
# arg 1: expected result
# arg 2: actual result
# arg 3: message to print
#
check_result_fuzzy()
{
  local exp="$1"
  local res="$2"
  local msg="$3"

  [ -n "$msg" ] && logn "$msg... "
  if [[ "$res" != *"$exp"* ]]; then
    NUM_FAIL=$((NUM_FAIL + 1))
    loge_colored_failed "failed, expected: '$exp', got '$res'."
  else
    NUM_SUC=$((NUM_SUC + 1))
    loge "ok."
  fi
  NUM_TOTAL=$((NUM_TOTAL + 1))
}
# this test case is failed (and cannot check by the return of $?)
# if $1 is specified then the test is silent (doesn't print anything)
check_failed()
{
  [ -z "$1" ] && loge_colored_failed failed.
  NUM_FAIL=$((NUM_FAIL + 1))
  NUM_TOTAL=$((NUM_TOTAL + 1))
  return $RC_ERROR
}
# this test case is passed (and does not check by the return of $?)
# if $1 is specified then the test is silent (doesn't print anything)
check_passed()
{
  [ -z "$1" ] && loge ok.
  NUM_SUC=$((NUM_SUC + 1))
  NUM_TOTAL=$((NUM_TOTAL + 1))
  return $RC_OK
}
# check the result of previously executed command ($? variable)
# and print failed/ok accordingly + update NUM_* variables
check() { if [ $? -ne 0 ]; then check_failed "$1"; else check_passed "$1"; fi; }

#
# Ultilities functions
#

# $1 is a URL; return the HTTP code from the URL by curl
get_http_return_code() { $CURL -sL -w "%{http_code}\\n" "$1" -o /dev/null; }

# if "$f" is found in known_fails, then return 0, otherwise to 1
check_known_fail()
{
  local testcase="$f"
  [ -n "$1" ] && testcase="$1"

  if [[ -f "$KNOWN_FAILS" && -n "$testcase" ]]; then
    grep -F "$testcase" "$KNOWN_FAILS" --quiet
    return $? # 0 if "$f" is a known fail
  fi
  return 1
}

#
# Check return code ($?) and update variables tracking number of
# failed ($NUM_FAIL) / ($NUM_SUC) successfull tests.
# In case of known fail TEST SKIPPED is printed instead of TEST FAILED.
#
update_result()
{
  local rc=$?
  check_known_fail
  local known_fail=$?

  if [ $rc -ne 0 ]; then 
    # failed
    if [ $known_fail -eq 0 ]; then
      status=$ST_SKIP
    else
      status=$ST_FAIL
      NUM_FAIL=$((NUM_FAIL + 1))
      [[ -n "$FAILED_LOG_FILE" && -n "$f" ]] && echo "$f" >> "$FAILED_LOG_FILE"
    fi
  else
    # passed
    if [ $known_fail -eq 0 ]; then
      status=$ST_FIX
    else
      status=$ST_PASS
    fi
    NUM_SUC=$((NUM_SUC + 1))
  fi
  NUM_TOTAL=$((NUM_TOTAL + 1))
  return $rc
}

prepare_json_file()
{
  local json_file="$1"
  if [[ -n "$json_file" && -f "$json_file" ]]; then
    sed -i -e '/href/d' -e 's/ *Result element [[:digit:]]: *//' \
           "$json_file"
  fi
}

# Remove URLs and some prefixes which might break a tests during oracle comparison
prepare_xml_file()
{
  local xml_file="$1"
  if [[ -n "$xml_file" && -f "$xml_file" ]]; then
    sed -i -e 's/gml://g' \
           -e $'s/\r//g' \
           -e '/xlink:href/d' \
           -e '/identifier /d' \
           -e 's@rasdaman/def@def@g' \
           -e 's|xmlns[^"]*"[^"]*"||g' \
           -e 's|xsi:schemaLocation="[^"]*"||g' \
           -e 's#http:\/\/\(\w\|[.-]\)\+\(:[0-9]\+\)\?\/def##g' \
           -e 's|at=[^ ]*||g' \
           -e '/fileReferenceHistory/d' \
           -e '/PostCode/d' \
           -e '/PostalCode/d' \
           -e 's/Long/Lon/g' \
           -e 's/Point /Point\n/g' \
           -e 's/ReferenceableGridCoverage /ReferenceableGridCoverage\n/g' \
           -e 's/^[[:space:]]*//' \
           -e '/^[[:space:]]*$/d' \
           -e 's/[[:space:]]>/>/g' \
           -e '/<valid_range>/s/ //g' \
           -e '/_NCProperties/d' \
           -e '/version=".*"/d' \
           "$xml_file"
  fi
}

# prepare a "gdal" file for comparison
# $1: input file
# $2: tmp prefix (output/oracle)
# stdout: the prepared filepath
prepare_gdal_file()
{
  local tmpf="$1.tmp"
  # only for gdal file (e.g: tiff, png, jpeg, jpeg2000)
  # here we compare the metadata and statistic values on output and oracle files directly
  # $p removes the first different lines of gdalinfo output until "Size is"
  gdalinfo -approx_stats "$1" 2> /dev/null | sed -n \
         -e '/Size is/,$p' > "$tmpf"
  sed -i -e '/TOWGS84\[/d' \
         -e '/fileReferenceHistory/d' \
         -e '/Mask Flags/d' \
         -e 's/ColorInterp=.*//g' \
         "$tmpf"

  rm -f "$1.aux.xml" "$1" && mv "$tmpf" "$1"
}

prepare_netcdf_file()
{
  local tmpf="$1.tmp"
  ncdump -c "$1" | sed \
    -e '/fileReferenceHistory/d' \
    -e '/_NCProperties/d' \
    -e '/global attributes/d' \
    -e 's/_ /0 /g' \
    -e '/^$/d' \
    -e '/valid_range =/s/ //g' \
    -e 's/Long/Lon/g' > "$tmpf"

  rm -f "$1" && mv "$tmpf" "$1"
}


# -----------------------------------------------------------------------------
# WCS 2.0.1 utility requests

# Encode a query which contains special characters to be valid string for GET request
urlencode() {
    # urlencode <string>

    old_lc_collate=$LC_COLLATE
    LC_COLLATE=C

    local length="${#1}"
    for (( i = 0; i < length; i++ )); do
        local c="${1:$i:1}"
        case $c in
            [a-zA-Z0-9.~_-]) printf '%s' "$c" ;;
            *) printf '%%%02X' "'$c" ;;
        esac
    done

    LC_COLLATE=$old_lc_collate
}


# Get all available coverage Ids
get_coverage_ids() {
  $WGET -qO- "$PETASCOPE_URL?service=WCS&version=2.0.1&request=GetCapabilities" | \
    grep -oP "(?<=<wcs:CoverageId>)[^<]+" | \
    tr '\n' ' '
}

delete_coverage() {
  # $1 is the coverageId to be deleted
  local input_coverage_id="$1"
  # It it is set to true, make a GetCapabitilies request first before deleting it
  local check_coverage_exist="$2"

  local delete_coverage=false
  if [ "$check_coverage_exist" = "true" ]; then
    local coverage_id=
    for coverage_id in $(get_coverage_ids); do
      if [ "$coverage_id" == "$input_coverage_id" ]; then
        delete_coverage=true
        break
      fi
    done
  else
    delete_coverage=true
  fi

  if [[ "$delete_coverage" = "true" ]]; then
    local OUTPUT_DIR="$SCRIPT_DIR/output"
    mkdir -p "$OUTPUT_DIR"
    local OUTPUT_FILE="$OUTPUT_DIR/DeleteCoverage-$1.out"
    # Store the result of deleting request to a temp file
    local delete_request="$PETASCOPE_URL?service=WCS&version=2.0.1&request=DeleteCoverage&CoverageId=$input_coverage_id"
    $CURL -i "$delete_request" > "$OUTPUT_FILE"

    # Check HTTP code is 200, coverage is deleted successfully
    if ! head -n1 < "$OUTPUT_FILE" | grep "200" --quiet; then
      # In case of error, put error message from Petascope to test.log
      tail -n6 <"$OUTPUT_FILE" >> "$LOG_FILE"
      return 1
    fi
  fi

  return 0
}

# -----------------------------------------------------------------------------

# e.g: https://oapi.rasdaman.org/rasdaman/oapi/collections/mean_summer_airtemp
get_request_rest() {
  # $1 is servlet endpoint (e.g: localhost:8080/rasdaman/oapi)
  # $2 is the request contex path and KVP if any
  local url="$1"
  local context
  context=$(echo "$2" | tr -d '\n')
  $CURL -G -X GET "$url/$context" > "$3" 
}

# GET KVP request
get_request_kvp() {
  # $1 is servlet endpoint (e.g: localhost:8080/rasdaman/ows)
  # $2 is KVP parameters (e.g: service=WCS&version=2.0.1&query=....)
  # $3 is output file
  # $4 only use for SECORE as it will only GET KVP in the URL directly without encoding
  local url="$1"
  # replace the "\n" in the query to be a valid GET request without break lines
  local kvpValues
  kvpValues="$(echo "$2" | tr -d '\n')"
  if [ -z "$4" ]; then
    $CURL -G -X GET "$url" --data-urlencode "$kvpValues" > "$3"
  else
    # SECORE (just send the request as it is without encoding)
    $CURL -X GET "$url""$kvpValues" > "$3"
  fi
}

# Only use it GET parameter is too long (e.g: WCPS big query) (file name convention: *.post.test)
post_request_kvp() {
  # $1 is servlet endpoint (e.g: localhost:8080/rasdaman/ows)
  # $2 is KVP parameters (e.g: service=WCS&version=2.0.1&query=....)
  # $3 is output file
  local url="$1"
  local kvpValues
  kvpValues="$(echo "$2" | tr -d '\n')"
  $CURL -X POST --data-urlencode "$kvpValues" "$url" > "$3"
}


post_request_kvp_with_breaklines() {
  # $1 is servlet endpoint (e.g: localhost:8080/rasdaman/ows)
  # $2 is KVP parameters (e.g: service=WCS&version=2.0.1&query=for $c in \n return \n encode(...))
  # $3 is output file
  local url="$1"
  local kvpValues
  kvpValues="$(echo "$2")"
  $CURL -X POST --data-urlencode "$kvpValues" "$url" > "$3"
}

# this function will be used to send XML/SOAP request for WCS, WCPS
post_request_xml() {
  # curl -s -X POST --data-urlencode "$kvpValues" "$PETASCOPE_URL" -o "$2"
  local url="$1"
  local kvpValues
  kvpValues="$(echo "$2" | tr -d '\n')"
  $CURL -X POST --data-urlencode "$kvpValues" "$url" > "$3"
}

# this function will send a POST request to server with an upload file
# e.g: used by WCS clipping extension with POST request
post_request_file() {
  # $1 is servlet endpoint (e.g: localhost:8080/rasdaman/ows)
  # $2 is KVP parameters (e.g: service=WCS&version=2.0.1&request=GetCoverage&clip=$1...)
  # $3 is the path to the file to be uploaded to server
  # $4 is output file from HTTP response
  local url="$1"
  local kvpValues
  kvpValues="$(echo "$2" | tr -d '\n')"
  local upload_file="$3"
  $CURL -F "file=@$upload_file" "$url?$kvpValues" > "$4"
}

# check if the output produced by the test ($1) matches the expected oracle ($2)
# and update the comparison result (NUM_SUC/NUM_FAIL/NUM_TOTAL global variables)
compare_output_to_oracle() {
  local out="$1"
  local ora="$2"

  if [[ -n "$check_script" && -f "$check_script" ]]; then
    export -f prepare_xml_file
    "$check_script" "$out" "$ora"
  else
    local ora_tmp="$out.oracle_tmp"
    local out_tmp="$out.output_tmp"
    cp "$ora" "$ora_tmp"
    cp "$out" "$out_tmp"
    local orafiletype
    orafiletype=$(file "$ora" | awk -F ':' '{print $2;}')

    # normalize files to remove irrelevant differences across systems
    if gdalinfo "$ora" &> /dev/null; then
      if [[ "$orafiletype" =~ "NetCDF" || "$orafiletype" =~ "Hierarchical Data Format" ]]; then
        prepare_netcdf_file "$out_tmp"
        prepare_netcdf_file "$ora_tmp"
      else
        prepare_gdal_file "$out_tmp"
        prepare_gdal_file "$ora_tmp"
      fi
    elif [[ "$orafiletype" == *XML* ]]; then
      prepare_xml_file "$out_tmp"
      prepare_xml_file "$ora_tmp"
    elif [[ "$orafiletype" == *JSON* || "$orafiletype" == *ASCII* ]]; then
      # e.g: for file type is json
      prepare_json_file "$out_tmp"
      prepare_json_file "$ora_tmp"
    fi

    # diff comparison ignoring EOLs [see ticket #551]
    diff -b "$out_tmp" "$ora_tmp" > /dev/null 2>&1
    rc=$?

    if [[ "$rc" != 0 && "$orafiletype" == *XML* ]]; then
      # ignore random error by SECORE BaseX
      # the grep below will result in $? == 0 if the random error is found in the output, so it will pass in update_result
      grep --quiet "Your feedback is welcome" "$out_tmp"
    else
      # this is necessary in order to set $? for update_result, as the $? from diff was lost because of the if checks above
      [ "$rc" -eq 0 ]
    fi

  fi

  # any previous statement must have been a comparison (diff, check_script, ...),\
  # so that at this point the $? points to the result of that comparison.
  update_result
}


# ------------------------------------------------------------------------------
#
# run a test suite, expected global vars:
#  $SVC_NAME - service name, e.g. wcps, wcs, etc.
#  "$f"        - test file
#
run_test()
{
  local f="$1"
  if [ ! -f "$f" ]; then
    error "test case not found: $f"
  fi

  # get test type - file extension
  local test_type
  test_type=$(echo "$f" | sed 's/.*\.//')

  # various other files expected  by the run_*_test functions
  # NOTE: remove input protocol extension: all queries with the same basename
  #       shall refer to the same oracle.
  local oracle="$ORACLE_PATH/${f%\.*}.oracle"
  # If there is a special oracle file for the OS (e.g: test.oracle.ubuntu1804, then use this file as oracle)
  [ -f "$oracle.$OS_VERSION" ] && oracle="$oracle.$OS_VERSION"

  local out="$OUTPUT_PATH/$f.out"
  local err="$OUTPUT_PATH/$f.err"
  local pre_script="$QUERIES_PATH/${f%\.*}.pre.sh"
  local post_script="$QUERIES_PATH/${f%\.*}.post.sh"
  local check_script="$QUERIES_PATH/${f%\.*}.check.sh"

  #
  # run pre script if present
  #
  if [ -f "$pre_script" ]; then
    if ! $pre_script; then
      log "warning: pre script failed execution - $pre_script"
    fi
  fi

  if [[ "$f" == *.sh ]]; then

    #
    # 0. run custom test script if present
    #
    "$f" "$out" "$oracle"
    update_result

  else
    # error: if file contents has "*" then it replaces it with file name, 
    # then must turn off this feature
    local QUERY
    QUERY=$(cat "$f" | tr -d '\n')

    #
    # 1. execute test query (NOTE: rasql is actually test_rasql_servlet)
    #
    case "$SVC_NAME" in

      servlet)
              # test_rasql_servlet
              case "$test_type" in
                kvp)
                    QUERY=$(cat "$f")
                    if check_query_runable "$QUERY"; then
                      post_request_kvp "$RASQL_SERVLET" "$QUERY" "$out"
                    fi
                    ;;
                input)
                    local templateFile="$SCRIPT_DIR/queries/post-upload.template"
                    local inputFile="$SCRIPT_DIR/queries/$f"
                    # read parameters from *.input file (NOTE: need to escape special characters like: &)
                    local parameters
                    parameters=$(cat "$inputFile")
                    # replace the parameters from current .input file into templateFile
                    sed "s#PARAMETERS#$parameters#g" "$templateFile" > "$templateFile.tmp.sh"
                    # run the replaced script to upload file and rasql query to 
                    # rasql-servlet and redirect output to /out directory
                    bash "$templateFile.tmp.sh" > "$out" 2> "$err"
                    # remove the temp bash script
                    rm -f "$templateFile.tmp.sh"
              esac
              ;;

      wcps)   case "$test_type" in
                post_file)
                    # It will send a WCS POST request with a file to petascope 
                    # (e.g: for WCS clipping extension: &clip=$1 and file=FILE_PATH_TO_WKT)
                    # NOTE: $ is not valid character for curl, it must be escaped inside test request file
                    QUERY=$(sed 's/\$/%24/g' "$f")
                    
                    # File to upload to server, same name with test request file but with .file                
                    local upload_file="${f%.*}.file"
                    if check_query_runable "$QUERY"; then
                      post_request_file "$PETASCOPE_URL" "query=$QUERY" "$upload_file" "$out"
                    else
                      return
                    fi
                    ;;
                test)

                    if [[ ( "$OS_VERSION" == "$OS_CENTOS7" || "$OS_VERSION" == "$OS_UBUNTU1604" ) && "$f" == *"overview"* ]]; then
                        # NOTE: centos 7 and ubuntu 16.04 with gdal version 1.x does not support importing overview
                        return
                    fi

                    QUERY=$(cat "$f")
                    if check_query_runable "$QUERY"; then
                      post_request_kvp_with_breaklines "$PETASCOPE_URL" "query=$QUERY" "$out"
                    else
                      return
                    fi
                    ;;
                xml)
                    QUERY=$(cat "$f")
                    if check_query_runable "$QUERY"; then
                      # send POST/SOAP to petascope
                      post_request_kvp_with_breaklines "$PETASCOPE_URL" "query=$QUERY" "$out"
                    else
                      return
                    fi
                    ;;
                *)  error "unknown wcps test type: $test_type"
              esac
              ;;

      wcs)    case "$test_type" in
                post_file)
                    # It will send a WCS POST request with a file to petascope 
                    # (e.g: for WCS clipping extension: &clip=$1 and file=FILE_PATH_TO_WKT)
                    # NOTE: $ is not valid character for curl, it must be escaped inside test request file
                    QUERY=$(sed 's/\$/%24/g' "$f")
                    # File to upload to server, same name with test request file but with .file                
                    local upload_file="${f%.*}.file"
                    if check_query_runable "$QUERY"; then
                      post_request_file "$PETASCOPE_URL" "$QUERY" "$upload_file" "$out"
                    else
                      return
                    fi
                    ;;
                kvp)
                    if [[ ( "$OS_VERSION" == "$OS_CENTOS7" || "$OS_VERSION" == "$OS_UBUNTU1604" ) && "$f" == *"overview"* ]]; then
                      # NOTE: centos 7 and ubuntu 16.04 with gdal version 1.x does not support importing overview
                      return
                    fi
                    QUERY=$(cat "$f")
                    if check_query_runable "$QUERY"; then
                      post_request_kvp "$PETASCOPE_URL" "$QUERY" "$out"
                    else
                      return
                    fi
                    ;;
                xml) 
                    QUERY=$(cat "$f")
                    if check_query_runable "$QUERY"; then
                      # send POST/SOAP XML
                      post_request_xml "$PETASCOPE_URL" "query=$QUERY" "$out"
                    else
                      return
                    fi
                    ;;
                xml_wcps) 
                    # WCPS query exists in XML-wrapper
                    if [[ -f "$f" ]]; then
                      QUERY=$(cat "$f")
                      post_request_xml "$PETASCOPE_URL?service=WCS&version=2.0.1&request=ProcessCoverages" "query=$QUERY" "$out"
                    else
                      return
                    fi
                    ;;                    
                *)  error "unknown wcs test type: $test_type"
              esac
              ;;

      wms)    
              endpoint="$PETASCOPE_URL"
              if [[ "$f" =~ "insert" ]]; then                
                endpoint="$PETASCOPE_ADMIN_URL/layer/style/add"
              elif [[ "$f" =~ "update" ]]; then
                endpoint="$PETASCOPE_ADMIN_URL/layer/style/update"              
              elif [[ "$f" =~ "delete" ]]; then
                endpoint="$PETASCOPE_ADMIN_URL/layer/style/remove"
              fi
              
              post_request_kvp "$endpoint" "$QUERY" "$out"
              ;;

      wmts)    
              endpoint="$PETASCOPE_URL"
              if [[ "$f" =~ "insert" ]]; then                
                endpoint="$PETASCOPE_ADMIN_URL/layer/style/add"
              elif [[ "$f" =~ "update" ]]; then
                endpoint="$PETASCOPE_ADMIN_URL/layer/style/update"              
              elif [[ "$f" =~ "delete" ]]; then
                endpoint="$PETASCOPE_ADMIN_URL/layer/style/remove"
              fi
              
              post_request_kvp "$endpoint" "$QUERY" "$out"
              ;;              

      oapi)   
              get_request_rest "$PETASCOPE_OAPI" "$QUERY" "$out"
              ;;

      secore) 
              QUERY=$(echo "$QUERY" | sed 's|%SECORE_URL%|'$SECORE_URL'|g')
              get_request_kvp "$SECORE_URL" "$QUERY" "$out" "secore"
              ;;

      select|nullvalues|subsetting|clipping|rasdapy3)

              QUERY=$(cat "$f")

              local RASQL_CMD="$RASQL"
              [ "$SVC_NAME" = "rasdapy3" ] && RASQL_CMD="$PY_RASQL"
              local out_scalar="${out}_scalar"
              local out_stdout="${out}_stdout"

              $RASQL_CMD -q "$QUERY" --out file --outfile "$out" 2> "$err" | tee "$out_stdout" | grep "  Result " > "$out_scalar"

              # if an exception was thrown, then the err file has non-zero size
              grep -q "Warning 6: PNG" "$err"
              rc=$?
              if [[ -s "$err" && $rc -ne 0 ]]; then
                mv "$err" "$out"
              else
                # move to proper output file (e.g: output.rasql.out.uknown to output.rasql.out)
                local tmpf
                for tmpf in $(ls "$out".* 2> /dev/null); do
                    # $tmpf here is  a file in the output directory from --outfile
                    [ -f "$tmpf" ] || continue
                    mv "$tmpf" "$out"
                    break
                done

                # if the result is a scalar, there will be no tmp file by rasql,
                # here we move the Result lines in stdout to $out
                if [[ ! -f "$out" && -f "$out_scalar" ]]; then
                    mv "$out_scalar" "$out"
                fi
              fi
              ;;

      *)      error "unknown service: $SVC_NAME"
    esac

    #
    # 2a. create $oracle from $output, if missing
    #
    local outfiletype
    outfiletype=$(file "$out" | awk -F ':' '{print $2;}')
    if [ ! -f "$oracle" ]; then
      status=$ST_COPY
      if [[ "$outfiletype" == *XML* ]]; then
          prepare_xml_file "$out"
      fi
      cp "$out" "$oracle"
    fi

    #
    # 2b. check result
    #
    compare_output_to_oracle "$out" "$oracle"

  fi # end of if not sh file

  #
  # run post script if present
  #
  if [ -f "$post_script" ]; then
    if ! $post_script; then
      log "warning: post script failed execution - $post_script"
    fi
  fi
}

# ------------------------------------------------------------------------------
# exit test script with/without error code
#
exit_script() { if [ $NUM_FAIL -ne 0 ]; then exit $RC_ERROR; else exit $RC_OK; fi; }

# ------------------------------------------------------------------------------
# rasdaman administration
#

stop_rasdaman()
{
  logn "stopping rasdaman $*..."
  if pgrep rasmgr &> /dev/null; then
    $STOP_RAS $* &> /dev/null
    sleep 0.2 || sleep 1
  fi
  loge ok
}

start_rasdaman()
{
  logn "starting rasdaman $*..."
  $START_RAS $* &> /dev/null
  sleep 0.2 || sleep 1
  loge ok.
}

restart_rasdaman()
{
  stop_rasdaman "$@"
  start_rasdaman "$@"
  if [[ $# = 0 ]]; then
      # everything is restarted including petascope; wait til petascope is up
      local wait_seconds=120 # wait up to 2 minutes for petascope to start
      local waited=0
      local sleep_seconds=5
      local petascope_available=false
      while [[ $waited -lt $wait_seconds ]]; do
          sleep $sleep_seconds
          waited=$((waited + sleep_seconds))
          if check_petascope > /dev/null 2>&1; then
              petascope_available=true
              break # petascope check returned 0 (success), stop waiting
          fi
      done
      if [[ $petascope_available = false ]]; then
          log "warning: petascope failed to start and respond within $wait_seconds seconds after restarting rasdaman."
      fi
  fi
  log "rasdaman restarted."
}

# ------------------------------------------------------------------------------
# server/config management for using local SQLite / file storage database
#
get_backup_rasmgr_conf()
{
  BACKUP_RASMGR_CONF=$(mktemp -u "$RASMGR_CONF.XXXXXXX")
  while [ -f "$BACKUP_RASMGR_CONF" ]; do
    BACKUP_RASMGR_CONF=$(mktemp -u "$RASMGR_CONF.XXXXXXX")
  done
}
prepare_configuration()
{
  local server_no="$1"
  [ -n "$server_no" ] || server_no=1
  get_backup_rasmgr_conf
  logn "backing up $RASMGR_CONF to $BACKUP_RASMGR_CONF... "
  cp "$RASMGR_CONF" "$BACKUP_RASMGR_CONF"
  check
  logn "updating connect string in $RASMGR_CONF to $DB_DIR/RASBASE... "
  echo "define dbh rasdaman_host -connect $DB_DIR/RASBASE" > "$RASMGR_CONF"
  echo "define db RASBASE -dbh rasdaman_host" >> "$RASMGR_CONF"
  echo "" >> "$RASMGR_CONF"
  for i in $(seq 1 "$server_no"); do
    local port=$((i + 1))
    {
      echo "define srv N$i -host blade -type n -port 700$port -dbh rasdaman_host"
      echo "change srv N$i -countdown 200000 -autorestart on -xp --timeout 300000"
      echo ""
    } >> "$RASMGR_CONF"
  done
}
restore_configuration()
{
  if [[ -n "$BACKUP_RASMGR_CONF" && -f "$BACKUP_RASMGR_CONF" ]]; then
    logn "restoring $RASMGR_CONF from $BACKUP_RASMGR_CONF... "
    cp "$BACKUP_RASMGR_CONF" "$RASMGR_CONF"
    feedback
    rm -f "$BACKUP_RASMGR_CONF"
  fi
}
recreate_rasbase()
{
  rm -rf "$DB_DIR"; mkdir -p "$DB_DIR"
  rm -rf "${LOG_DIR:?}"/*
  logn "recreating RASBASE... "
  "$RMANHOME"/bin/create_db.sh
  check
  restart_rasdaman
}
# ------------------------------------------------------------------------------

# print the server pid (single rasserver should be running, otherwise wrong pid might be determined)
get_server_pid()
{
  local -r server_pid=$(ps aux | grep 'bin/rasserver' | grep -v grep | awk '{ print $2; }' | head -n 1)
  [ -n "$server_pid" ] && echo "$server_pid"
}

# if there are more background jobs than $PARALLEL_QUERIES, then this function
# will wait until there are less. It has a timeout of 60 seconds and will exit
# with error if the timeout is reached.
wait_for_background_jobs()
{
  local major=${BASH_VERSINFO[0]}
  local minor=${BASH_VERSINFO[1]}
  if ((major > 4 || (major = 4 && minor >= 3))); then
    # The wait bash command has option -n since Bash 4.3
    if [ "$(jobs -p | wc -l)" -gt "$PARALLEL_QUERIES" ]; then
      #echo waiting, currently "$(jobs -p | wc -l)" jobs running
      wait -n
    fi
  else
    # CentOS 7 has Bash 4.2 without wait -n; we emulate it here with sleep
    elapsed_sec=0
    while ((elapsed_sec < WAIT_TIMEOUT_SEC)); do
      if [ "$(jobs -p | wc -l)" -gt "$PARALLEL_QUERIES" ]; then
        sleep 0.2
        elapsed_sec=$((elapsed_sec + 1))
      else
        break
      fi
    done
    if ((elapsed_sec >= WAIT_TIMEOUT_SEC)); then
      error "background jobs did not terminate after waiting for $elapsed_sec seconds."
    fi
  fi
}

# Call to start measuring CPU utilization.
# Sets global variables cpu_active_prev and cpu_total_prev.
# It also calls the start_timer function.
# Note: adapted from https://stackoverflow.com/a/26791468
measure_cpu_utilization_start()
{
  # Read /proc/stat file (for first datapoint)
  read cpu user nice system idle iowait irq softirq steal guest < /proc/stat
  # compute active and total utilizations
  cpu_active_prev=$((user+system+nice+softirq+steal))
  cpu_total_prev=$((user+system+nice+softirq+steal+idle+iowait))
  cpus_active_prev=()
  cpus_total_prev=()
  cpu_count=$(nproc)
  for i in $(seq 0 $((cpu_count-1))); do
    read cpu user nice system idle iowait irq softirq steal guest < <(grep "cpu$i " /proc/stat)
    cpus_active_prev+=($((user+system+nice+softirq+steal)))
    cpus_total_prev+=($((user+system+nice+softirq+steal+idle+iowait)))
  done
  start_timer
}

# Call to end measuring CPU utilization; measure_cpu_utilization_start must have
# been called first. It also calls the stop_timer function.
# By default a report is printed with overal and per-CPU utilization as well as
# execution since measure_cpu_utilization_start was called.
# If an argument "noreport" is specified nothing is printed. 
# In both cases a global variable cpu_util is set.
measure_cpu_utilization_end()
{
  stop_timer
  # Read /proc/stat file (for first datapoint)
  read cpu user nice system idle iowait irq softirq steal guest < /proc/stat
  # compute active and total utilizations
  local cpu_active_cur=$((user+system+nice+softirq+steal))
  local cpu_total_cur=$((user+system+nice+softirq+steal+idle+iowait))
  # compute CPU utilization (%)
  cpu_util=$((100*( cpu_active_cur-cpu_active_prev ) / (cpu_total_cur-cpu_total_prev) ))

  # compute individual CPU utilization in %
  declare -a cpus_active_cur
  declare -a cpus_total_cur
  declare -a cpus_util
  for i in $(seq 0 $((cpu_count-1))); do
    read cpu user nice system idle iowait irq softirq steal guest < <(grep "cpu$i " /proc/stat)
    cpus_active_cur+=($((user+system+nice+softirq+steal)))
    cpus_total_cur+=($((user+system+nice+softirq+steal+idle+iowait)))
    cpus_util+=($((100*( cpus_active_cur[i]-cpus_active_prev[i] ) / (cpus_total_cur[i]-cpus_total_prev[i]) )))
  done

  if [ "$1" = report ]; then
    printf "Total execution time: ${c_bold}%s seconds${c_off}\n" $(get_time_s)
    printf "CPU core utilization:\n"
    for i in $(seq 0 $((cpu_count-1))); do
      printf "|%4d " "$i"
    done
    printf "| ${c_bold}Total${c_off} |\n"

    for i in $(seq 0 $((cpu_count-1))); do
      if [ "${cpus_util[$i]}" -gt 80 ]; then
        printf "|${c_red}%3d%%${c_off} " "${cpus_util[$i]}"
      elif [ "${cpus_util[$i]}" -gt 40 ]; then
        printf "|${c_yellow}%3d%%${c_off} " "${cpus_util[$i]}"
      else
        printf "|${c_green}%3d%%${c_off} " "${cpus_util[$i]}"
      fi
    done
    printf "| ${c_bold}%3d%%${c_off}  |\n" "$cpu_util"
  fi
}

#
# load all modules
#
. "$UTIL_SCRIPT_DIR"/rasql.sh
. "$UTIL_SCRIPT_DIR"/py_rasql.sh
