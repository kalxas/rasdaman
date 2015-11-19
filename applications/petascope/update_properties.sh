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
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.    If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# SYNOPSIS
#    update_properties.sh path_to_old_file path_to_new_file
# Description
#    Compare user's properties files with current properties files.
#    Add the new properties to user files but keep their configuration like (host, port, database name, users name, password,...)
#
################################################################################
# RETURN CODES
RC_OK=0		# everything went fine
RC_ERROR=1	# something went wrong

# get script name
PROG=$( basename $0 )

#
# logging
#
log() {
    echo "$PROG: $*"
}
logn() {
    echo -n "$PROG: $*"
}
error() {
    echo "$PROG: $*" >&2
    _cleanup # rollback to old properties file before exiting as it is an error case
    exit $RC_ERROR
}
ok() {
    echo "$PROG: Done."
    exit $RC_OK
}

# rollback old configuration if error occurs
_cleanup() {
    echo "$PROG: Exiting..." >&2
    # 1. if the script has created $OLD_BAK then has something to clean or just finish
    if [ -f "$OLD_BAK" ]; then
	logn "Note: Rollingback your old configuration file... "
        # 1. Remove old configuration as it can has some unfinished upgrade data
        rm -f "$OLD" || error "Error: Cannot remove old configuration file $OLD."

        # 2. Remove new configuration file which has been copied to $OLDDIR if it exists
        rm -f "$NEWTMP" || error "Error: Cannot remove new configuration file $NEWTMP."; # remove new configuration file in $OLDDIR

        # 3. Rename temporary backup file to $OLD configuration file and done
        mv "$OLD_BAK" "$OLD" || error "Error: Cannot rollback $OLD_BAK to $OLD."; # rollback backup file to $OLD
        echo "Done."
    fi # end check $OLD_BAK file exists
    log "Done."
    exit $RC_ERROR # escape from trap as it has rollbacked
}

trap _cleanup HUP INT QUIT KILL


log "Updating your new configuration file with old values."

# Get filepath to OLD and NEW files

if [ "$#" -eq 0 ]; then
    error "Error: no arguments (old and new file paths) were supplied."

elif [ ! "$#" -eq 2 ]; then
    error "Error: need 2 arguments (old and new file paths) to be supplied, recived: $#."
fi

# --------------------------------------------
#1. Get file name from the arguments (OLD and NEW files)
OLD="$1"
NEW="$2"

NOW=$(date "+%m-%d-%Y_%H-%M-%S") # get current date_time
OLD_BAK="$OLD.$NOW.bak" # this backup will not overwrite previous backup files as different times


# --------------------------------------------
#2 Check files are existing
#2.1 Check OLD file is existing
if [ ! -f "$OLD" ]; then
    error "Error: Old file: $OLD is not a valid file path."
fi

#2.2 Check NEW file is exist
if [ ! -f "$NEW" ]; then
    error "Error: New file: $NEW is not a valid file path."
fi
echo -e

# --------------------------------------------
#3 Backup the OLD file by renaming to OLD.bak
logn "Backing up your old configuration file to $OLD_BAK... "
cp "$OLD" "$OLD_BAK" || error "Error: Cannot backup your old configuration file."
echo "Done."

# --------------------------------------------
#4 Copy the NEW file to the directory of OLD file with the name likes $NEW.tmp
OLDDIR=$(dirname "$OLD") # get the directory of OLD file
NEWFILENAME=$(basename "$NEW") # get the file name of NEW file

NEWTMP="$OLDDIR"'/'"$NEWFILENAME"'.tmp'

cp "$NEW" "$NEWTMP" || error "Error: Cannot copy new configuration file to $OLDDIR." # copy NEW file to NEW.tmp in OLD directory
echo -e

# ---------------------------------------------
#5 Update the NEW file in OLD directory with the OLD configuration values
# Read each line in OLD files which is configured and add this value in NEW file.
log "Updating differences between new file and old file..."

k=0 # notice about changing values
while read line;do
    line=$(echo "$line" | xargs -0) # | xargs for trimming spaces in head/tail of line.
    CHAR=${line:0:1} # get first character of the line

    if [[ $CHAR != '#' && $CHAR != '' ]]; then    # if $line starts with # or spaces -> comments so ignore.

        # 5.1 Get the OLD setting (setting name) and value (setting value)
        OLD_SETTING=$(echo ${line%%=*}) # get the value before the delimiter '=', % is from ending (right to left)
        OLD_VALUE=$(echo ${line##*=}) # get the value after the delimiter '=', ## is from beginning (left to right)

        OLD_SETTING_VALUE="$OLD_SETTING"'='"$OLD_VALUE"
        OLD_SETTING_VALUE=$(echo "$OLD_SETTING_VALUE" | xargs -0 ) # trimming spaces

        # 5.2 Get the NEW setting (name and value) from NEW.tmp
        NEW_SETTING_VALUE=$(cat "$NEWTMP" | grep -E "($OLD_SETTING=)+") # i.e: grep -E '(start_embdded_petascope=true)' E is groupping regex, + for match one
        NEW_SETTING_VALUE=$(echo "$NEW_SETTING_VALUE" | xargs -0 ) # trimming spaces

        if [[ $NEW_SETTING_VALUE == '' ]]; then # if no OLD setting value in NEW file
            log "+ Note: Old setting: $OLD_SETTING_VALUE is deprecated. Nothing to do. Done.";

        elif [[ "$OLD_SETTING_VALUE" != "$NEW_SETTING_VALUE"    ]]; then
            k=1 # To check value has been changed
            # 5.3 If the OLD value is not the same as the NEW value then replace the NEW setting with the OLD setting from NEW.tmp
            sed -i "s|\($OLD_SETTING=\).*|\1$OLD_VALUE|g" $NEWTMP # Trick: replace after delimiter '=' with OLD value in NEW.tmp (special characters)
            log "+ Changing $NEW_SETTING_VALUE to $OLD_VALUE. Done.";
        fi
    fi # end check line not start with '#' in OLD file
done < "$OLD" # ending read line in files

# if nothing needs to change in NEW file
if [[ $k == 0 ]]; then
    log "Your old configuration is already up to date."
fi

echo -e

# ---------------------------------------------
#6 Remove OLD configuration file and rename NEW.tmp to the original file
logn "Removing old configuration file... "
rm "$OLD" || error "Error: Cannot remove old configuration file in $OLDDIR."; # remove old file
echo "Done."

mv "$NEWTMP" "$OLD" # rename NEW.tmp to old file.
log "The configuration file has been successfully updated."
ok
