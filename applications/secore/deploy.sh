#!/bin/bash

# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#

# PURPOSE:
#   (Re)deploy secore servlet to Tomcat's webapps directory.
#
# PRECONDITIONS:
# - CATALINA_HOME variable has to be set properly
# - effective user id of script allows writing into Tomcat webapps/
#
# WRITTEN BY:
# - Dimitar Misev, Mihaela Rusu
#
###########################################################

ME=`basename $0`
echo $ME: deploying rasdaman secore servlet...

# - definitions -------------------------------------------
# error handler
panic(){
  echo $ME: $1
  exit 1
}

# name of deployment target;
# we want /def/... URLs but do not want to modify Tomcat config
TARGET_SERVICE=def


# input arguments
# $1 is secore.war file path (e.g: /home/rasdaman/build/applications/secore/def.war)
BUILDFILE="$1"

if [ ! -f "$BUILDFILE" ]; then
	panic "File path to built def.war does not exist."
fi


# $2 is the deploy path (e.g: /var/lib/tomcat/webapps)
# optional argument passed to the script, indicating where should
# the war file be deployed. The Makefile passes this argument.
WARDIR="$2"

# deployment targets: take first argument to script if given,
# otherwise use CATALINA_HOME.
# (no prob if CATALINA_HOME undefined, will catch later)
if [ -n "$WARDIR" ]
then
  WEBAPPS="$WARDIR"
else
  WEBAPPS=$CATALINA_HOME/webapps
fi
TARGET_WAR=$WEBAPPS/$TARGET_SERVICE.war

# - plausi checks -----------------------------------------

# webapps directory exists?
if [ ! -d $WEBAPPS ]
then
	panic "The given deployment directory does not exist: $WEBAPPS"
fi
if [ ! -w "$WEBAPPS" ]
then
  panic "User $USER can not write to the deployment directory: $WEBAPPS"
fi

# war file or directory preexisting? -> backup
if [ -f $TARGET_WAR ]
then
	echo "existing servlet $TARGET_WAR will be moved to $TARGET_WAR.bak"
	mv $TARGET_WAR $TARGET_WAR.bak
fi

# - prepare -----------------------------------------------
echo -n $ME: building...
mvn package || panic "cannot build SECORE"
echo " ok."

# - deploy ------------------------------------------------
echo -n $ME: deploying war file...
cp $BUILDFILE $TARGET_WAR || panic "failed"
echo " ok."

echo $ME: done.
exit 0

