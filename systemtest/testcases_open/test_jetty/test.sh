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
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#  test.sh
# Description
#  Command-line utility for testing embedded Jetty - Petascope (default on user setting in petascope.properties).
#
# PRECONDITIONS
#   Rasdaman installed
#   Embedded Jetty - Petascope is running (java_server=embedded) or not running (java_server=external)
# Usage: ./test.sh

#------------------ Test input --------------------------------------

# script return codes
#
RC_OK=0
RC_ERROR=1


# get script name
MYNAME=`basename $0`

JAVA_SERVER_EMBEDDED='embedded'

configPath=$(echo $RMANHOME)'/etc'; # default /home/rasdaman/install/etc ($RMANHOME)

installPath=$(echo ${configPath:0:-3}) # Get substring before '/etc' in file path (default is /home/rasdaman/install)
petascopePath=$installPath'/share/rasdaman/war/' # Embedded Petascope directory: /home/rasdaman/install/share/rasdaman/war/
javaServer=$(cat $installPath'etc/petascope.properties' | grep 'java_server'  | awk -F "=" '{print $2}');  # check if java-server is embedded/external
javaServer=$(echo $javaServer | sed -e 's/^[ \t]*//') #trim leading spaces

START_EMBEDDED_PETASCOPE="start_embedded_petascope" # check if start_embedded_petascope = true/false to allow start embedded jetty petascope

# get the pid of lasted embedded Jetty with Petascope (only 1 process is allowed to run in background)
# the [] is trick to avoid grep's pid is chosen instead of running Jetty.
pid=$(ps -ef | grep '[r]asdaman/war/'$JETTY_JAR | awk '{print $2; exit}')

# get the port of Jetty in setting file
portJetty=$(cat $installPath'etc/petascope.properties' | grep 'jetty_port'  | awk -F "=" '{print $2}')
portJetty=`echo $portJetty | sed -e 's/^[ \t]*//'` #trim leading spaces

# get the enable/disable embedded Jetty - Petascope when execute start_rasdaman.sh
startPetascope=$(cat $installPath'etc/petascope.properties' | grep $START_EMBEDDED_PETASCOPE  | awk -F "=" '{print $2}');
startPetascope=$(echo $startPetascope | sed -e 's/^[ \t]*//') #trim leading spaces


#---------------- Test cases -------------------------

# Case 1: java_server=embedded, start_embedded_petascope=true
if [ "$javaServer" == "$JAVA_SERVER_EMBEDDED"  ]  && [ $startPetascope == true ]; then # embedded server

    echo -e
    echo $MYNAME: '"java_server=embedded", "enabled start_embedded_petascope" in petascope.properties........Checking.';
    echo -e
    if [[ ! -z "$pid" ]]; then # pid is not empty then Jetty is working
	echo 'Test Pass: Jetty - Petascope is working. pid = "'$pid'"'

        # check if Jetty is working in the configuration port
	isPortUsed=`netstat -anpp 2>/dev/null | grep $portJetty | awk '{print $6}'` # check if port is open or not, 2>/dev/null to clean the warning: 'you must be root when using netstat'

	if [[  "$isPortUsed" =~ 'LISTEN' ]]; then # If this port is used (LISTEN) then Jetty has worked
	      echo 'Test Pass: Embedded Petascope is working with port: "'$portJetty'" in petascope.properties file.'
	      exit $RC_OK;
	else
	       # Bad case 1.1 (java_server = embedded, start_embedded_petascope=true) - Jetty is not running in jetty_port
	      echo 'Test Fail: Embedded Petascope is not working with port: "'$portJetty'" in petascope.properties file.'
	      exit $RC_ERROR

	fi

    # Bad case 1.2 (java_server = embedded, start_embedded_petascope=true) - Jetty is not running
    else
       echo 'Test Fail: Embedded Petascope is not working. '
       echo 'Hint: Please make sure you have executed start_rasdaman.sh before.'
    fi

# Case 2: java_server=embedded, start_embedded_petascope=false
elif [ "$javaServer" == "$JAVA_SERVER_EMBEDDED"  ] && [ $startPetascope == false ]; then

    echo -e
    echo $MYNAME: '"java_server=embedded", "disabled start_embedded_petascope" in petascope.properties........Checking.';
    echo -e
    if [[ ! -z "$pid" ]]; then # pid is not empty then it is working
       # Bad case 2.1 (java_server = embedded, start_embedded_petascope=false) - Jetty is running
       echo 'Test Fail: Embedded Petascope is working. pid = "'$pid'"'
       exit $RC_ERROR
    else
       echo 'Test Pass: Embedded Petascope is not working because "start_embedded_petascope=false."'
       exit $RC_OK
    fi

# Case 3: java_server=external
else # external server (Tomcat - Petascope)

    echo -e
    echo $MYNAME: '"java_server=external"........Checking.';
    echo -e

    if [[ ! -z "$pid" ]]; then # pid is not empty then Jetty is working with setting 'java-server=external'
        # Bad case 3.1 (java_server = external) - Jetty is running
	echo 'Test Fail: Embedded Petascope is working. pid = "'$pid'"'
        echo 'Hint: Please execute stop_rasdaman.sh to stop embedded Petascope.'
        echo '      then start_rasdaman.sh and recheck this test case.'
        exit $RC_ERROR
    else
	echo 'Test Pass: Embedded Petascope is not working.'
        exit $RC_OK
    fi
fi

echo -e
exit $RC_OK
