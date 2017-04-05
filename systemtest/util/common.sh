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


# ------------------------------------------------------------------------------
# script return codes
#
RC_OK=0
RC_ERROR=1
RC_SKIP=2


# ------------------------------------------------------------------------------
# testing statistics
#
NUM_TOTAL=0 # the number of manipulations
NUM_FAIL=0  # the number of fail manipulations
NUM_SUC=0   # the number of success manipulations

# testing data
TEST_GREY=test_grey
TEST_GREY2=test_grey2
TEST_RGB2=test_rgb2
TEST_GREY3D=test_grey3d
TEST_COMPLEX=test_complex
TEST_NULL=nulltest
TEST_SUBSETTING_1D=test_subsetting_1d
TEST_SUBSETTING=test_subsetting
TEST_SUBSETTING_SINGLE=test_subsetting_single
TEST_SUBSETTING_3D=test_subsetting_3d


# ------------------------------------------------------------------------------
# command shortcuts
#
export RASQL="rasql --server $RASMGR_HOST --port $RASMGR_PORT --user $RASMGR_ADMIN_USER \
              --passwd $RASMGR_ADMIN_PASSWD --database $RASDB"
export RASCONTROL="rascontrol --host $RASMGR_HOST --port $RASMGR_PORT"
export RASDL="rasdl -d $RASDB"
export RASIMPORT="rasimport"

export START_RAS=start_rasdaman.sh
export STOP_RAS=stop_rasdaman.sh
export CREATE_DB=create_db.sh

export GDALINFO="gdalinfo -noct -checksum"

# filestorage
export DB_DIR="/tmp/rasdb"
export LOG_DIR="$RMANHOME/log"
export RASMGR_CONF="$RMANHOME/etc/rasmgr.conf"

# ------------------------------------------------------------------------------
# logging
#

LOG_FILE="$SCRIPT_DIR/test.log"
OLD_LOG_FILE="$LOG_FILE.save"
FAILED_LOG_FILE="$SCRIPT_DIR/failed_cases.log"
FIXED=0


log()
{
  echo "$PROG: $*" | tee -a "$LOG_FILE"
}

loge()
{
  echo -e "$*" | tee -a "$LOG_FILE"
}

logn()
{
  echo -n "$PROG: $*" | tee -a "$LOG_FILE"
}

# write the detail error in failed_cases.log
log_failed()
{
  echo "$PROG: $*" | tee -a "$FAILED_LOG_FILE"
}

feedback()
{
  if [ $? -ne 0 ]; then
    loge failed.
  else
    loge ok.
  fi
}

check()
{
  if [ $? -ne 0 ]; then
    echo "$PROG: failed, exiting." | tee -a "$LOG_FILE"
    exit $RC_ERROR
  else
    loge ok.
  fi
}

error()
{
  echo "$PROG: $*" | tee -a "$LOG_FILE"
  echo "$PROG: exiting." | tee -a "$LOG_FILE"
  exit $RC_ERROR
}

# ------------------------------------------------------------------------------
# setup log
#

if [ -n "$SCRIPT_DIR" ] ; then
  if [ -f $LOG_FILE ] ; then
    echo "Old logfile found, copying it to $OLD_LOG_FILE"
    rm -f "$OLD_LOG_FILE"
    mv "$LOG_FILE" "$OLD_LOG_FILE"
  fi

  rm -f "$FAILED_LOG_FILE"

  NOW=`date`
  log "starting test at $NOW"
  log ""
fi

#
# manage timing, in ms
#
start_timer()
{
  timer_start=$(date +%s%N)
}
stop_timer()
{
  timer_stop=$(date +%s%N)
}
get_time()
{
  echo "scale=3; ($timer_stop - $timer_start) / 1000000.0" | bc
}

# ------------------------------------------------------------------------------
# dependency checks
#
check_rasdaman()
{
  type rasmgr &> /dev/null
  if [ $? -ne 0 ]; then
    error "rasdaman not installed, please add rasdaman bin directory to the PATH."
  fi
  pgrep rasmgr &> /dev/null
  if [ $? -ne 0 ]; then
    error "rasdaman not started, please start with start_rasdaman.sh"
  fi
  $RASCONTROL -x 'list srv -all' &> /dev/null
  if [ $? -ne 0 ]; then
    error "no rasdaman servers started."
  fi
}

check_postgres()
{
  type psql  &> /dev/null
  if [ $? -ne 0 ]; then
    error "PostgreSQL missing, please add psql to the PATH."
  fi
  pgrep postgres &> /dev/null
  if [ $? -ne 0 ]; then
    pgrep postmaster > /dev/null || error "The PostgreSQL service is not started."
  fi
}

check_curl()
{
  type curl  &> /dev/null
  if [ $? -ne 0 ]; then
    error "curl missing, please install."
  fi
}

check_petascope()
{
  # already silent
  curl -sL "$PETASCOPE_URL" -o /dev/null
  if [[ $? -ne 0 ]]; then
    log "failed connecting to Petascope at $PETASCOPE_URL, please deploy it first."
    return 1
  fi
  return 0
}


check_secore()
{
  # already silent
  curl -sL "$SECORE_URL/crs" -o /dev/null
  if [ $? -ne 0 ]; then
    log "failed connecting to SECORE at $SECORE_URL, please deploy it first."
    return 1
  fi
  return 0
}

check_netcdf()
{
  type ncdump  &> /dev/null
  if [ $? -ne 0 ]; then
    error "netcdf tools missing, please add ncdump to the PATH."
  fi
}

check_gdal()
{
  type gdal_translate  &> /dev/null
  if [ $? -ne 0 ]; then
    error "gdal missing, please add gdal_translate to the PATH."
  fi
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
    if [[ $? -eq 0 ]]; then
      return 0
    else
      log "skipping test as it is not runable."
      loge
      return 1
    fi
  fi
  return 0
}


check_filestorage_dependencies()
{
  [ -f "$RMANHOME/bin/rasdl" ] || error "rasdl not found, RMANHOME not defined properly?"
  [ -f "$RASMGR_CONF" ] || error "$RASMGR_CONF not found, RMANHOME not defined properly?"
  [ $(type sqlite3  &> /dev/null) ] || error "sqlite3 not found, please install."
}


# ------------------------------------------------------------------------------
# print test summary
#
print_summary()
{
  if [ $NUM_TOTAL -eq 0 ]; then
    NUM_TOTAL=$(($NUM_FAIL + $NUM_SUC))
  fi
  NOW=`date`

  echo
  log "-------------------------------------------------------"
  log "Test summary"
  log ""
  log "  Test finished at: $NOW"
  log "  Total tests run : $NUM_TOTAL"
  log "  Successful tests: $NUM_SUC"
  log "  Failed tests    : $NUM_FAIL"
  log "  Skipped tests   : $(($NUM_TOTAL - ($NUM_FAIL + $NUM_SUC)))"
  log "  Detail test log : $LOG_FILE"
  log "-------------------------------------------------------"

  # compute return code
  if [ $NUM_FAIL -eq 0 ]; then
    RC=$RC_OK
  else
    RC=$RC_ERROR
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
  exp="$1"
  res="$2"
  msg="$3"

  logn "$msg... "
  if [ "$exp" != "$res" ]; then
    NUM_FAIL=$(($NUM_FAIL + 1))
    echo failed.
    log "expected: '$exp', got '$res'"
  else
    NUM_SUC=$(($NUM_SUC + 1))
    echo ok.
  fi
  NUM_TOTAL=$(($NUM_TOTAL + 1))
}

check()
{
  if [ $? -ne 0 ]; then
    loge failed.
    NUM_FAIL=$(($NUM_FAIL + 1))
  else
    loge ok.
    NUM_SUC=$(($NUM_SUC + 1))
  fi
  NUM_TOTAL=$(($NUM_TOTAL + 1))
}

# this test case is failed (and cannot check by the return of $?)
check_failed()
{
  echo failed.
  NUM_FAIL=$(($NUM_FAIL + 1))
  NUM_TOTAL=$(($NUM_TOTAL + 1))
}

# this test case is passed (and cannot check by the return of $?)
check_passed()
{
  echo ok.
  NUM_SUC=$(($NUM_SUC + 1))
  NUM_TOTAL=$(($NUM_TOTAL + 1))
}


#
# Ultilities functions
#

# input is a URL return the HTTP code from the URL by curl
get_http_return_code()
{
  # $1 is URL
  http_code=$(curl -sL -w "%{http_code}\\n" "$1" -o /dev/null)
  echo $http_code
}


#
# Check return code ($?) and update variables tracking number of
# failed/successfull tests.
# In case of the open tests TEST SKIPPED is printed instead of TEST FAILED.
#
update_result()
{

  local rc=$?

  grep "$f" "$KNOWN_FAILS" &> /dev/null
  local known_fail=$?

  if [ $rc != 0 ]; then
    echo "$SCRIPT_DIR" | grep "testcases_open" > /dev/null
    rc_open=$?
    echo "$f" | egrep "\.fixed$" > /dev/null
    rc_fixed=$?

    fail_result="TEST FAILED"
    if [ $rc_open -ne 0 -o $rc_fixed -eq 0 ]; then
      if [ $known_fail -ne 0 ]; then
        NUM_FAIL=$(($NUM_FAIL + 1))
        log " ->  TEST FAILED"
      else
        # Known failed case, add it to known_failed_cases.log.
        log " -> TEST SKIPPED"
        fail_result="TEST SKIPPED"
      fi
    else
      log " ->  TEST SKIPPED"
      fail_result="TEST SKIPPED"
    fi

    # Beside writing to test.log, need to write the failed cases and test results to failed_cases.log as well
    if [ -n "$FAILED_LOG_FILE" ]; then
      echo "----------------------------------------------------------------------" >> "$FAILED_LOG_FILE"
      if [ -n "$f" ]; then
        # log test case file name.
        log_failed "$f"
        log_failed ""
      fi

      # must enquote file content in "" or it will lose the new line from test case query
      log_failed "$(cat "$f")"
      log_failed ""
      log_failed "$fail_result"
    fi
  else
    NUM_SUC=$(($NUM_SUC + 1))
    log " ->  TEST PASSED"
    if [ $known_fail -eq 0 ]; then
      log " ->"
      log " -> Case known to fail has been fixed!"
      log " -> Please remove $f from $KNOWN_FAILS"
    fi
  fi
  log "--------------------------------------------------------------------------------------------"
  NUM_TOTAL=$(($NUM_TOTAL + 1))
}

# ------------------------------------------------------------------------------
#
# run rasql query test
# expects several global variables to be set:
# - $f      - input file
# - $out    - output file
# - $oracle - oracle file with expected result
# - $custom_script - bash script to be executed for special comparison of result
#                    to oracle.
#
run_rasql_test()
{
  rm -f "$out"
  local QUERY=`cat $f`
  $RASQL -q "$QUERY" --out file --outfile "$out"

  # move to proper output file
  for tmpf in `ls "$out".*  2> /dev/null`; do
    mv "$tmpf" "$out"
    break
  done

  # if the result is a scalar, there will be no tmp file by rasql,
  # here we output the Result element scalar into tmp.unknown
  if [ ! -f "$out" ]; then
    $RASQL -q "$QUERY" --out string | grep Result > $out
  fi

  cmp "$oracle" "$out"
  update_result
}


# ------------------------------------------------------------------------------
#
# Remove URLs and some prefixes which might break a tests during oracle comparison
#
prepare_xml_file()
{
  xml_file="${1}"
  echo "Preparing XML file $xml_file for oracle comparison... "
  if [ -n "${1}" ]; then
      sed -i 's/gml://g' "$xml_file"
      sed -i $'s/\r//g' "$xml_file"
      sed -i '/xlink:href/d' "$xml_file"
      sed -i '/identifier /d' "$xml_file"
      sed -i 's|xmlns:[^=]*="[^"]*"||g' "$xml_file"
      sed -i 's|xsi:schemaLocation="[^"]*"||g' "$xml_file"
      sed -i 's#http:\/\/\(\w\|[.-]\)\+\(:[0-9]\+\)\?\/def##g' "$xml_file" # not only test.cfg SECORE_URL, but also what's in ps_crs!
      sed -i 's|at=[^ ]*||g' "$xml_file"                                   # e.g. See ``/crs/OGC/0'' Vs ``/crs?authority=OGC&version=0''.
  fi
  echo "ok."
}

# trim the leading spaces in XML from oracle in each line.
trim_indentation()
{
  echo "Removing indentation to compare..."
  xml_file="${1}"
  # remove all the leading spaces in oracle and output in XML to compare
  echo $(cat "$xml_file") | sed -e 's/^[ \t]*//' > "$xml_file.tmp"
  echo "Done."
}

# -----------------------------------------------------------------------------
# GET KVP request
get_request_kvp() {
  # $1 is servlet endpoint (e.g: localhost:8080/rasdaman/ows)
  # $2 is KVP parameters (e.g: service=WCS&version=2.0.1&query=....)
  # $3 is output file
  # $4 only use for SECORE as it will only GET KVP in the URL directly without encoding
  url="$1"
  # replace the "\n" in the query to be a valid GET request without break lines
  kvpValues=`echo "$2" | tr -d '\n'`
  if [[ -z "$4" ]]; then
    echo "$url?$kvpValues"
    curl -s -X GET "$url" --data-urlencode "$kvpValues" > "$3"
  else
    # SECORE (just send the request as it is without encoding)
    echo "$url$kvpValues"
    curl -s -X GET "$url""$kvpValues" > "$3"
  fi
}

# Only use it GET parameter is too long (e.g: WCPS big query) (file name convention: *.post.test)
post_request_kvp() {
  # $1 is servlet endpoint (e.g: localhost:8080/rasdaman/ows)
  # $2 is KVP parameters (e.g: service=WCS&version=2.0.1&query=....)
  # $3 is output file
  url="$1"
  kvpValues=`echo "$2" | tr -d '\n'`

  echo "$url?$kvpValues"
  curl -s -X POST "$url" --data-urlencode "$kvpValues" > "$3"
}

# this function will be used to send XML/SOAP request for WCS, WCPS
post_request_xml() {
  # curl -X GET -d @14-get_coverage_jp2_slice_t_crs1.xml http://localhost:8080/rasdaman/ows -o error.txt
  curl -s -X POST -d @"$1" "$PETASCOPE_URL" -o "$2"
}


# ------------------------------------------------------------------------------
#
# run a test suite, expected global vars:
#  $SVC_NAME - service name, e.g. wcps, wcs, etc.
#  $f        - test file
#
run_test()
{
  if [ ! -f "$f" ]; then
    error "test case not found: $f"
  fi

  # check if rasdaman is running and exit if not
  $RASQL -q 'select c from RAS_COLLECTIONNAMES as c' --out string &> /dev/null
  if [ $? -ne 0 ]; then
    # retry test
    sleep 2
    $RASQL -q 'select c from RAS_COLLECTIONNAMES as c' --out string &> /dev/null
    if [ $? -ne 0 ]; then
        log "rasdaman down, exiting..."
        cleanup
    fi
  fi

  # if testcase marked as fixed temporarily we remove the .fixed extension
  oldf="$f"
  if [ -n "$FIXED" -a $FIXED -eq 1 ]; then
    f="$fixedf"
    FIXED=0
  fi

  # get test type - file extension
  test_type=`echo "$f" | sed 's/.*\.//'`

  # various other files expected  by the run_*_test functions
  # NOTE: remove input protocol extension: all queries with the same basename shall refer to the same oracle.
  oracle="$ORACLE_PATH/${f%\.*}.oracle"
  out="$OUTPUT_PATH/$f.out"
  err="$OUTPUT_PATH/$f.err"
  rm -f "$out"
  pre_script="$QUERIES_PATH/${f%\.*}.pre.sh"
  post_script="$QUERIES_PATH/${f%\.*}.post.sh"
  check_script="$QUERIES_PATH/${f%\.*}.check.sh"

  # restore original filename
  f="$oldf"


  # temporary files
  oracle_tmp="$OUTPUT_PATH/temporary_oracle"
  output_tmp="$OUTPUT_PATH/temporary_out"

  #
  # run pre script if present
  #
  if [ -f "$pre_script" ]; then
    log "running pre-test script..."
    $pre_script
    if [ $? -ne 0 ]; then
      log "warning: pre script failed execution - $pre_script"
    fi
  fi

  if [[ "$f" == *.sh ]]; then

    #
    # 0. run custom test script if present
    #
    $f "$out" "$oracle"
    update_result

  else
    # error: if in file has "*" then it replaces it with file name, then must turn off this feature
    QUERY=`cat $f | tr -d '\n'`

    #
    # 1. execute test query (NOTE: rasql is actually rasql_servlet test)
    #
    case "$SVC_NAME" in
      rasql)
              case "$test_type" in
                kvp)
                    QUERY=`cat $f`
                    # check if query contains "jpeg2000" and gdal supports this format, then the query should be run.
                    check_query_runable "$QUERY"
                    if [[ $? -eq 0 ]]; then
                      get_request_kvp "$RASQL_SERVLET" "$QUERY" "$out"
                    fi
                    echo "Done."
                    ;;
                input)
                    templateFile="$SCRIPT_DIR/queries/post-upload.template"
                    inputFile="$SCRIPT_DIR/queries/$f"
                    # read parameters from *.input file (NOTE: need to escape special characters like: &)
                    parameters=`cat $inputFile`
                    # replace the parameters from current .input file into templateFile
                    sed "s#PARAMETERS#$parameters#g" $templateFile > "$templateFile".tmp.sh
                    # run the replaced script to upload file and rasql query to rasql-servlet and redirect output to /out directory
                    sh "$templateFile".tmp.sh > "$OUTPUT_PATH/"$f".out"
                    # remove the temp bash script
                    rm -f "$templateFile".tmp.sh
              esac
              ;;
      wcps)   case "$test_type" in
                test)
                    QUERY=`cat $f`
                    # check if query contains "jpeg2000" and gdal supports this format, then the query should be run.
                    check_query_runable "$QUERY"
                    if [[ $? -eq 0 ]]; then

                      if [[ "$f" =~ "post.test" ]]; then
                        # big WCPS query goes with POST (prepend the KVP parameter for WCPS extension)
                        post_request_kvp "$PETASCOPE_URL" "query=$QUERY" "$out"
                      else
                        # small WCPS query goes with GET (prepend the KVP parameter for WCPS extension)
                        get_request_kvp "$PETASCOPE_URL" "query=$QUERY" "$out"
                      fi
                    else
                      continue
                    fi
                    echo "Done."
                    ;;
                xml)
                    postdata=`mktemp`
                    cat "$f" > "$postdata"

                    # check if query contains "jpeg2000" and gdal supports this format, then the query should be run.
                    check_query_runable "`cat $f`"
                    if [[ $? -eq 0 ]]; then
                      # send SOAP to petascope
                      post_request_xml "$postdata" "$out"
                    else
                        continue
                    fi
                    echo "Done."
                    rm -f "$postdata"
                    ;;
                *)   error "unknown wcs test type: $test_type"
              esac
              ;;
      wcs)    case "$test_type" in
                kvp)
                    QUERY=`cat $f`
                    # check if query contains "jpeg2000"-approx_stats and gdal supports this format, then the query should be run.
                    check_query_runable "$QUERY"
                    if [[ $? -eq 0 ]]; then
                      get_request_kvp "$PETASCOPE_URL" "$QUERY" "$out"
                    else
                      continue
                    fi
                    # SERVICE=WCS&VERSION=2.0.1&REQUEST=ProcessCoverages&query=for c in (test_mr) return avg(c)
                    # this query will need to be encoded for value of parameter "query" only
                    echo "Done."
                    ;;
                xml) postdata=`mktemp`
                    cat "$f" > "$postdata"

                    # check if query contains "jpeg2000" and gdal supports this format, then the query should be run.
                    check_query_runable "`cat $f`"
                    if [[ $? -eq 0 ]]; then
                        # send XML by POST
                        post_request_xml "$postdata" "$out"
                    else
                        continue
                    fi

                    rm -f "$postdata"
                    ;;
                *)  error "unknown wcs test type: $test_type"
              esac
              ;;
      wms)    get_request_kvp "$PETASCOPE_URL" "$QUERY" "$out"
              ;;
      secore) QUERY=`echo "$QUERY" | sed 's|%SECORE_URL%|'$SECORE_URL'|g'`
              get_request_kvp "$SECORE_URL" "$QUERY" "$out" "secore"
              ;;
      select|rasql|nullvalues|subsetting)
              QUERY=`cat $f`
              
              $RASQL -q "$QUERY" --out file --outfile "$out" > /dev/null 2> "$err"

              # if an exception was thrown, then the err file has non-zero size
              if [ -s "$err" ]; then
                mv "$err" "$out"

              else
                # move to proper output file (e.g: output.rasql.out.uknown to output.rasql.out)
                for tmpf in `ls "$out".*  2> /dev/null`; do
                    # $tmpf here is  a file in the output directory from --outfile
                    [ -f "$tmpf" ] || continue
                    mv "$tmpf" "$out"
                    break
                done

                # if the result is a scalar, there will be no tmp file by rasql,
                # here we output the Result element scalar into tmp.unknown
                if [ ! -f "$out" ]; then
                    $RASQL -q "$QUERY" --out string | grep Result > $out
                fi
              fi
              ;;
      *)      error "unknown service: $SVC_NAME"
    esac

    #
    # 2a. create $oracle from $ouput, if missing
    #
    outfiletype=`file "$out" | awk -F ':' '{print $2;}'`
    if [ ! -f "$oracle" ]; then
      log " -> NO ORACLE FOUND"
      log " -> copying $out to $oracle"
      if [[ "$outfiletype" == *XML* ]]; then
          prepare_xml_file "$out"
      fi
      cp "$out" "$oracle"
    fi

    #
    # 2b. check result
    #
    # If query has associated custom script then use this script first and compare the result (only XML)
    if [ -n "$check_script" -a -f "$check_script" ]; then
      log "custom script"
      prepare_xml_file "$out"
      "$check_script" "$out" "$oracle"
      update_result
    else
      # check the file type of oracle
      filetype=`file "$oracle" | awk -F ':' '{print $2;}'`

      # 1. If oracle does exists then check if could be read by gdal or it is in text format
      # 1.1 File exists and could be read by gdal
      # check that oracle is gdal readable (e.g: tiff, png, jpeg,..)
      gdalinfo "$oracle" &> /dev/null
      if [ $? -eq 0 ]; then
        # do image comparison
        output_tmp="$out"."output.tmp"
        oracle_tmp="$out"."oracle.tmp"
        log "image comparison, type: $filetype"

        # if oracle/output is netcdf then compare them by ncdump
        if [[ "$filetype" =~ "NetCDF" ]]; then
          ncdump -c "$out" > "$output_tmp"
          ncdump -c "$oracle" > "$oracle_tmp"

          # remove the line to 'dimensions'
          sed -i -n '/dimensions/,$p' "$output_tmp"
          sed -i -n '/dimensions/,$p' "$oracle_tmp"
        else
          # only for gdal file (e.g: tiff, png, jpeg, jpeg2000)
          # here we compare the metadata and statistic values on output and oracle files directly
          gdalinfo -approx_stats "$out" > "$output_tmp" 2> /dev/null
          gdalinfo -approx_stats "$oracle" > "$oracle_tmp" 2> /dev/null

          # remove the gdalinfo tmp file
          rm -f "$out"".aux.xml"
          rm -f "$oracle"".aux.xml"

          # then remove the first different few lines (driver, filename and filename.aux)
          sed -i -n '/Size is/,$p' "$output_tmp"
          sed -i -n '/Size is/,$p' "$oracle_tmp"
          # NOTE: some small values can be neglectable in Coordinate System to compare (e.g: TOWGS84[0,0,0,0,0,0,0],)
          sed '/TOWGS84\[/d' -i "$output_tmp"
          sed '/TOWGS84\[/d' -i "$oracle_tmp"
        fi

        cmp "$output_tmp" "$oracle_tmp" 2>&1
        update_result

      else
        # 1.2 File exists and could not be read by gdal
        # oracle must be in xml, csv, json
        # (with xml need special function to extract the URL before comparison)
        if [[ "$filetype" == *XML* ]]; then
          prepare_xml_file "$out"
          # strip indentation from $oracle -> $oracle.tmp and $out -> $out.tmp
          trim_indentation "$oracle"
          trim_indentation "$out"
          log "XML comparison"

          # diff comparison ignoring EOLs [see ticket #551]
          diff -b "$oracle.tmp" "$out.tmp" 2>&1 > /dev/null
          update_result

          # remove the temp files
          rm -f "$oracle.tmp"
          rm -f "$out.tmp"
        else
          # csv, json
          log "byte comparison"
          # diff comparison ignoring EOLs [see ticket #551]
          diff -b "$oracle" "$out" 2>&1 > /dev/null
          update_result
        fi

      fi # end of text oracle file

    fi # end of if not have custom script for test queries

  fi # end of if not sh file

  #
  # run post script if present
  #
  if [ -f "$post_script" ]; then
    log "running post-test script..."
    $post_script
    if [ $? -ne 0 ]; then
      log "warning: post script failed execution - $post_script"
    fi
  fi
}

# ------------------------------------------------------------------------------
# exit test script with/without error code
#
exit_script()
{
  if [ $NUM_FAIL -ne 0 ]; then
    exit $RC_ERROR
  else
    exit $RC_OK
  fi
}

# ------------------------------------------------------------------------------
# rasdaman administration
#

restart_rasdaman()
{
  logn "restarting rasdaman... "
  if [ $(pgrep rasmgr) ]; then
    $STOP_RAS &> /dev/null
    sleep 0.2 || sleep 1
  fi
  $START_RAS &> /dev/null
  sleep 0.2 || sleep 1
  loge ok.
}

# ------------------------------------------------------------------------------
#
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
    local port=$(($i + 1))
    echo "define srv N$i -host blade -type n -port 700$port -dbh rasdaman_host" >> "$RASMGR_CONF"
    echo "change srv N$i -countdown 200000 -autorestart on -xp --timeout 300000" >> "$RASMGR_CONF"
    echo "" >> "$RASMGR_CONF"
  done
}
restore_configuration()
{
  if [ -n "$BACKUP_RASMGR_CONF" -a -f "$BACKUP_RASMGR_CONF" ]; then
    logn "restoring $RASMGR_CONF from $BACKUP_RASMGR_CONF... "
    cp "$BACKUP_RASMGR_CONF" "$RASMGR_CONF"
    feedback
    rm -f "$BACKUP_RASMGR_CONF"
  fi
}

recreate_rasbase()
{
  rm -rf "$DB_DIR"; mkdir -p "$DB_DIR"
  rm -rf "$LOG_DIR"/*
  logn "recreating RASBASE... "
  rasdl -c --connect "$DB_DIR/RASBASE" > /dev/null && rasdl --connect "$DB_DIR/RASBASE" -r "$RMANHOME/share/rasdaman/examples/rasdl/basictypes.dl" -i > /dev/null
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

#
# load all modules
#
. "$UTIL_SCRIPT_DIR"/rasql.sh
