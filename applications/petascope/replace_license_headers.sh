#!/bin/bash
# ~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=
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
# Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
# ~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=
#
# SYNOPSIS
# ./replace_license_headers.sh [-f header_file]
#
# PURPOSE
# Update license header in every Petascope Java file.
#
# DESCRIPTION
# The portion of comments between the first '/*' and '*/' matches is replaced
# by the selected header replacement (./nbproject/licenseheader.txt by default)
#
# PREREQUISITES
# ---
#
# ~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=

# output formatting
bold=$( tput bold )
normal=$( tput sgr0 )

# args/synopsis/logging
ME="$( basename $0 )"
RC_OK=0
RC_ERROR=1
JROOT_ARG='--java-root-folder'
FILE_ARG='-f'
MISSING_ARG='--missing-headers'
MISSING_ARG_ALL='all'
MISSING_ARG_NONE='none'
NOSTAGE_ARG='--no-git-stage'
VERBOSE_ARG='-v'
HELP_ARG='--help'
DEFAULT_HEADER_FILE='./nbproject/licenseheader.txt'
JAVA_ROOT='./src/main/java/petascope'
USAGE="\

  ${bold}${ME}${normal} [$JROOT_ARG folder] [$FILE_ARG header_file]\
 [$MISSING_ARG {$MISSING_ARG_ALL|$MISSING_ARG_NONE}] [$NOSTAGE_ARG] [$VERBOSE_ARG] [$HELP_ARG]

    ${bold}${JROOT_ARG}${normal}
        Specify root folder where to seek the java files (default is ${bold}${JAVA_ROOT}${normal}).

    ${bold}${FILE_ARG}${normal}
        Specify path to file containing header (default is ${bold}${DEFAULT_HEADER_FILE}${normal}).

    ${bold}${MISSING_ARG}${normal} {$MISSING_ARG_ALL|$MISSING_ARG_NONE}
        Do not interact with user when headers are missing in a file: either add header for ${bold}all${normal} or ${bold}none${normal} of such files.

    ${bold}${NOSTAGE_ARG}${normal}
        Do not automatically stage changes to ${bold}git${normal} index.

    ${bold}${VERBOSE_ARG}${normal}
        Verbose output.

    ${bold}${HELP_ARG}${normal}
        Print this information.
"

# Local variables
HEADER_FILE="$DEFAULT_HEADER_FILE"
LICENSE_PATTERN='This file is part of' # additional check to verify that a license header is going to be replaced
DEBUG='eval echo >/dev/null' # off by default
MISSING_POLICY=
TMP='tmp'
GIT_ADD='git add' # on by default

# Parse command line argument(s)
$DEBUG "parsing arguments..."
while [ $# -gt 0 ]; do
       case "$1" in
          ${JROOT_ARG})   JAVA_ROOT="$2";      shift;;
          ${FILE_ARG})    HEADER_FILE="$2";    shift;;
          ${MISSING_ARG}) MISSING_POLICY="$2"; shift;;
          ${NOSTAGE_ARG}) GIT_ADD='eval echo >/dev/null';;
          ${VERBOSE_ARG}) DEBUG="echo $ME:";;
          ${HELP_ARG})    echo "$USAGE"; exit $RC_OK;;
          *) echo -e "(!) Unknown argument \"$1\".\nusage:\n$USAGE"; exit $RC_ERROR;;
        esac
        shift
done

# input check
if [ -n "$MISSING_POLICY" -a "$MISSING_POLICY" != "$MISSING_ARG_ALL" -a "$MISSING_POLICY" != "$MISSING_ARG_NONE" ]; then
    echo "$ME: illegal option \""$MISSING_POLICY"\" for argument $MISSING_ARG."
    echo "$USAGE"
    exit $RC_ERROR
fi
if [ ! -d "$JAVA_ROOT" ]; then
    echo "$ME: \"$JAVA_ROOT\" is not a valid directory."
    exit $RC_ERROR
fi

#-------------------------------#

echo "$ME: using replacement file $HEADER_FILE: \"$( cat $HEADER_FILE | tr -d "\n" | head -c 50 )\"..."

# replace headers
$DEBUG "Searching java files in $JAVA_ROOT"
for jfile in $( find "$JAVA_ROOT" -name "*.java" | sort )
do
    # reset (null tests)
    from_line=
    to_line=
    is_license=
    answer=

    # line numbers FROM/TO replacement
    from_line=$( grep -n "/\*" "$jfile" | head -n 1 | cut -d: -f1 )
      to_line=$( grep -n "\*/" "$jfile" | head -n 1 | cut -d: -f1 )
    is_license=$( sed -n "${from_line:-1},${to_line:-1}p" "$jfile" | grep "$LICENSE_PATTERN" )

    if [ -n "$from_line" -a -n "$to_line" -a -n "$is_license" ]
    then
        echo $ME: processing file $jfile.
        $DEBUG "replacing from line $from_line to line $to_line: $( sed -n "${from_line},${to_line}p" "$jfile" | tr -d "\n" | head -c 50 )..."
        # drop old header
        sed "${from_line},${to_line}d" "$jfile" > $TMP
        # insert new header (prepend to FROM line)
        awk -v TEXT="$( cat "$HEADER_FILE" )" "{
            if ( NR==${from_line} ) {
                { print TEXT }
                { print \$0 }
            } else { print \$0 }
        }" $TMP > "$jfile"
        $GIT_ADD "$jfile" # NOP if $NOSTAGE_ARG is set
    else
        # print alert
        echo "$ME: [!] license header for file $jfile seems missing."
        while [[ -z $answer && "$MISSING_POLICY" != "$MISSING_ARG_NONE" ]]
        do
            [[ "$MISSING_POLICY" = "$MISSING_ARG_ALL" ]] || read -p "$ME: should I insert it [Y/n]?" answer
            if [ -z "$answer" -o "$answer" = "y" -o "$answer" = "Y" ]
            then
                # header insertion has been requested
                cat "$HEADER_FILE" "$jfile" > "$TMP"
                mv "$TMP" "$jfile"
                $GIT_ADD "$jfile" # NOP if $NOSTAGE_ARG is set
                echo "$ME: new header has been prepended to $jfile."
                break;
            elif [ "$answer" = "n" -o "$answer" = "N" ]
            then
                # No insertion requested
                break;
            fi
            answer=
        done
    fi
done

rm $TMP
echo "$ME: done."
echo "$ME: to verify the [staged] changes see \`git diff [--cached] $JAVA_ROOT\`"
echo "$ME: to discard changes run [\`git reset HEAD $JAVA_ROOT\`] \`git checkout -- $JAVA_ROOT\`"
exit $RC_OK
