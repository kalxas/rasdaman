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
# Copyright 2003 - 2017 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#  test.sh
# Description
#  Command-line utility for testing embedded petascope.
#
# PRECONDITIONS
#   Rasdaman installed
#   Embedded petascope is enabled (java_server=embedded) or not enabled (java_server=external)
# Usage: ./test.sh

#------------------ Test input --------------------------------------

# script return codes
#
RC_OK=0
RC_ERROR=1


########## This one is used to test Petascope embedded manually only, no point to run it on system test with make check
if [ $# -ne 1 ]; then
    echo "$0: missing path to petascope.properties file."
    echo "$0: usage: test.sh path_to_petascope_properties_file (e.g: test.sh /home/rasdaman/install/etc/petascope.properties)"
    exit $RC_OK
fi


# get script name
MYNAME=`basename $0`

JAVA_SERVER_EMBEDDED='embedded'

# get petascope properties file as command line argument
petascopePropertiesFile=$1
# check if java-server is embedded/external
javaServer=$(cat "$petascopePropertiesFile" | grep '^java_server'  | awk -F "=" '{print $2}');  
#trim leading spaces
javaServer=$(echo $javaServer | sed -e 's/^[ \t]*//') 

# check if start_embedded_petascope = true/false to allow start embedded petascope
START_EMBEDDED_PETASCOPE="start_embedded_petascope" 

# get the pid of lasted embedded petascope (only 1 process is allowed to run in background)
# the [] is trick to avoid grep's pid is chosen instead of running embedded petascope.
pid=$(ps -ef | grep '[r]asdaman.jar' | awk '{print $2; exit}')

# get the enable/disable configuration for embedded petascope
startPetascope=$(cat "$petascopePropertiesFile" | grep ^$START_EMBEDDED_PETASCOPE  | awk -F "=" '{print $2}');
#trim leading spaces
startPetascope=$(echo $startPetascope | sed -e 's/^[ \t]*//')

#------------------------- Test cases -------------------------

# Case 1: java_server=embedded, start_embedded_petascope=true
if [ "$javaServer" == "$JAVA_SERVER_EMBEDDED" ]  && [ $startPetascope == true ]; then # embedded server
    echo $MYNAME: 'java_server=embedded, start_embedded_petascope=true in petascope.properties...Checking.';

    if [[ ! -z "$pid" ]]; then # pid is not empty then embedded petascope is working
    	echo 'Test Pass: Embedded petascope is working with pid = "'$pid'"'
    else
        echo 'Test Fail: Embedded Petascope is not working.'
        echo 'Hint: Please make sure you have executed start_rasdaman.sh before.'
        exit $RC_ERROR
    fi    

# Case 2: java_server=embedded, start_embedded_petascope=false
elif [ "$javaServer" == "$JAVA_SERVER_EMBEDDED" ] && [ $startPetascope == false ]; then
    echo $MYNAME: 'java_server=embedded", start_embedded_petascope=false in petascope.properties...Checking.';

    if [[ ! -z "$pid" ]]; then # pid is not empty then it is working
        echo 'Test Fail: Embedded Petascope is working with pid = "'$pid'"'
        echo 'Hint: Please make sure you have executed stop_rasdaman.sh before.'
        exit $RC_ERROR
    else
        echo 'Test Pass: Embedded Petascope is not working because start_embedded_petascope=false.'
        exit $RC_OK
    fi

# Case 3: java_server=external
else # external server (Tomcat - Petascope)
    echo $MYNAME: 'java_server=external in petascope.properties...Checking.';

    if [[ ! -z "$pid" ]]; then # pid is not empty then embedded petascope is working with setting 'java-server=external'
    	echo 'Test Fail: Embedded Petascope is working with pid = "'$pid'"'
        echo 'Hint: Please execute stop_rasdaman.sh to stop embedded Petascope.'
        echo '      then start_rasdaman.sh and recheck this test case.'
        exit $RC_ERROR
    else
	    echo 'Test Pass: Embedded Petascope is not working.'
        exit $RC_OK
    fi
fi

exit $RC_OK
