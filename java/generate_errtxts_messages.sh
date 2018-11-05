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
# Description
#  This script is used to generate Rasdaman error messages from canonical source errtxts file (http://rasdaman.org/browser/bin/errtxts)
#  to a real file from the input template file (.in).
#
# Usage:
#  script.sh PATH_TO_A_TEMPLATE_FILE.in (e.g: rasj/RasErrorTexts.java.in)
#
#  NOTE: any applications (e.g: rasj, rasdapy) which needs to translate the error number with detail error message will need to use this script
#  to have a mirror array of error messages as same as which is used in Rasdaman.

# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

############ CONSTANTS ############
RC_OK=0
RC_ERROR=1

# Input file (errtxts)
ERRTXTS_FILE="$SCRIPT_DIR/../bin/errtxts"

ERRTXTS_FILE_RETRY="$SCRIPT_DIR/../../bin/errtxts"

# Output template file (e.g: rasj: RasErrorTexts.java.in file)
TEMPLATE_FILE="$1"

# Template variable which is replaced by real content from errtxts in OUTPUT file
TEMPLATE_VARIABLE="\$ARRAY_OF_ERROR_MESSAGES_FROM_ERRTXTS_FILE"

log()
{
  echo "$PROG: $*" 
}

loge()
{
  echo -e "$*"
}

logn()
{
  echo -n "$PROG: $*"
}

error()
{
  echo "$PROG: $*"
  echo "$PROG: exiting."
  exit $RC_ERROR
}


if [ ! -f "$ERRTXTS_FILE" ]; then
    # Try again to find the source error file in upper folder path
    if [ ! -f "$ERRTXTS_FILE_RETRY" ]; then
        error "errtxts file '$ERRTXTS_FILE' or '$ERRTXTS_FILE_RETRY' not found."
    fi

    ERRTXTS_FILE="$ERRTXTS_FILE_RETRY"
fi

if [ ! -f "$TEMPLATE_FILE" ]; then
    error "output template file (.in) $TEMPLATE_FILE not found."
fi

#### 1. Read errtxts file and parse it to an array of String
array_error_messages="";
while read line
do
    if [[ "$line" != "#"* && "$line" == *":"* ]]; then
        # e.g: 700^E^Admin error: General error creating RasDaMan database
        # replaced to "700:Admin error: General error creating RasDaMan database"
        line=$(sed 's/\^E\^/:/g' <<< "$line")
        array_error_messages=$array_error_messages'"'$line'",\n'
    fi     
done < "$ERRTXTS_FILE"

# Remove 3 trailing characters ",\n"
array_error_messages=${array_error_messages%???}

#### 2. From the output template file (.in), create a new file for it and replace the template string for array error messages in this file
REAL_FILE=${TEMPLATE_FILE%???}

# Copy template .in file to real file
cp "$TEMPLATE_FILE" "$REAL_FILE"

# Then, replace the array of error messages in real file (e.g: rasj: RasErrorTexts.java)
sed -i "s@$TEMPLATE_VARIABLE@$array_error_messages@g" "$REAL_FILE"
