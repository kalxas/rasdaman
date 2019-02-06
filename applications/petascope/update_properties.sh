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
#    Add the new properties to user files but keep their properties like (host, port, database name, users name, password,...)
#
################################################################################
# RETURN CODES
RC_OK=0		# everything went fine
RC_ERROR=1	# something went wrong

# get script name
PROG=$( basename $0 )

# if no key in properties file exists, value of this key is null
NULL_VALUE="NULL"

#
# logging
#
log()   { echo    "$PROG: $@"; }
logn()  { echo -n "$PROG: $@"; }
loge()  { echo -e "$PROG: $@"; }
error() { echo    "$PROG: $@" >&2; _cleanup; exit $RC_ERROR; }
check() { [ $? -eq 0 ] && echo ok. || echo failed.; }
ok()    { echo    "$PROG: Done."; exit $RC_OK; }

# return the value of key=value from properties file ($1: the key, $2: the file)
get_value(){    
    key_value=$(grep -E "^$1=" "$2") && echo ${key_value#*=} || echo "$NULL_VALUE"
}

# replace the value of a key from properties file ($1: the key, $2: the new value, $3: the file)
replace_value() {
    sed -i "s|\($1\).*|\1$2|g" "$3"
}

# rollback old properties if error occurs
_cleanup() {
    echo "$PROG: Exiting..." >&2
    # 1. if the script has created $old_bak then has something to clean or just finish
    if [ -f "$old_bak" ]; then
        logn "Rolling back old properties file... "
        # 1. Remove old properties as it can has some unfinished upgrade data
        rm -f "$old_file" || error "Error: Cannot remove old properties file $old_file."
        # 2. Remove new properties file which has been copied to $old_dir if it exists
        rm -f "$new_file_tmp" || error "Error: Cannot remove new properties file $new_file_tmp."; # remove new properties file in $old_dir
        # 3. Rename temporary backup file to $old_file properties file and done
        mv "$old_bak" "$old_file" || error "Error: Cannot rollback $old_bak to $old_file."; # rollback backup file to $old_file
        echo "ok."
    fi # end check $old_bak file exists
    ok
}

trap _cleanup HUP INT QUIT KILL


# Get filepath to old_file and new_file files
if [ "$#" -eq 0 ]; then
    error "Error: no arguments (old and new file paths) were supplied."
elif [ ! "$#" -eq 2 ]; then
    error "Error: need 2 arguments (old and new file paths) to be supplied, recived: $#."
fi

# --------------------------------------------
#1. Get file name from the arguments (old_file and new_file files)
old_file="$1"
new_file="$2"

log "Updating properties file $old_file..."

NOW=$(date "+%m-%d-%Y_%H-%M-%S") # get current date_time
old_bak="$old_file.$NOW.bak" # this backup will not overwrite previous backup files as different times


# --------------------------------------------
#2 Check files are existing
#2.1 Check new_file file is existing (the properties file in the source folder)
if [ ! -f "$new_file" ]; then
    error "Error: New file: $new_file is not a valid file path."
fi

#2.3 Check if old_file's directory existed
old_dir=$(dirname "$old_file")
if [ ! -d "$old_dir" ]; then
    mkdir -p "$old_dir" || error "Error: failed creating properties directory '$old_dir'."
    log "Created properties directory '$old_dir'."
fi

#2.3 Check old_file file is existing ( if new installation then just copy the source properties file to installation folder)
if [ ! -f "$old_file" ]; then
    log "Installing $new_file..."
    cp "$new_file" "$old_file" || error "Error: failed installing properties file '$new_file' to '$old_file'."
    ok
fi

#2.3 Check if new_file and old_file file are the same (no need to create a backup and do anything else)
cmp --quiet "$new_file" "$old_file"
if [[ $? -eq 0 ]]; then
   log "Existing properties file is up to date."
   ok
fi

# --------------------------------------------
#3 Backup the old_file file by renaming to old_file.bak
logn "Creating a backup at $old_bak... "
cp "$old_file" "$old_bak" || error "Error: Cannot backup existing properties file."
echo "ok."

# --------------------------------------------
#4 Copy the new_file file to the directory of old_file file with the name likes $new_file.tmp
new_file_name=$(basename "$new_file") # get the file name of new_file file
new_file_tmp="$old_dir/$new_file_name.tmp"

# copy new_file file to new_file.tmp in old_file directory
cp "$new_file" "$new_file_tmp" || error "Error: Cannot copy new properties file to $new_file_tmp." 

# ---------------------------------------------
#5 Update the new_file file in old_file directory with the old_file properties values
# Read each line in old_file files which is configured and add this value in new_file file.

trim_whitespace() {
    local var="$*"
    # remove leading whitespace characters
    var="${var#"${var%%[![:space:]]*}"}"
    # remove trailing whitespace characters
    var="${var%"${var##*[![:space:]]}"}"   
    echo -n "$var"
}

# if some old settings are removed, the backup file needs to be kept
keep_backup=0 

while read line; do
    line=$(trim_whitespace "$line")
    first_char=${line:0:1} # get first character of the line

    if [[ $first_char != '#' && $first_char != '' ]]; then # if $line starts with # or spaces -> comments so ignore.

        # 5.1 Get the old_file setting (setting name) and value (setting value)
        old_setting=$(echo ${line%%=*}) # get the value before the delimiter '=', % is from ending (right to left)
        old_value=$(echo ${line##*=}) # get the value after the delimiter '=', ## is from beginning (left to right)

        old_setting_value=$(trim_whitespace "$old_setting=$old_value")

        # 5.2 Get the new_file setting (name and value) from new_file.tmp
        new_value=$(get_value "$old_setting" "$new_file_tmp")
        new_setting_value="$old_setting=$new_value"

        if [[ "$new_value" == "$NULL_VALUE" ]]; then # if no old_file setting value in new_file file
            log "'$old_setting_value' is deprecated and removed from existing properties file."
            keep_backup=1 # Some old settings were removed, then it needs to keep the backup file
        elif [[ "$old_setting_value" != "$new_setting_value" ]]; then            
            # 5.3 If the old_file value is not the same as the new_file value 
            # then replace the new_file setting with the old_file setting from new_file.tmp
            logn "Will not update '$old_setting=$old_value'... "
            # Trick: replace after delimiter '=' with old_file value in new_file.tmp (special characters)
            sed -i "s|\($old_setting=\).*|\1$old_value|g" "$new_file_tmp"
            check
        fi
    fi # end check line not start with '#' in old_file file
done < "$old_file" # ending read line in files

### For new Petascope in 9.5, copy the username and password from old properties (version 9.4) to new Spring properties
# then user doesn't have to update this petascope.properties for current datasource for Spring.
if [ "$new_file_name" != "secore.properties" ]; then
    spring_datasource_url_value=$(get_value "spring.datasource.url" "$old_file")
    if [[ "$spring_datasource_url_value" == "$NULL_VALUE" ]]; then
        logn "Configuring Spring datasource for petascope v9.5+ ... "
        metadata_url_value=$(get_value "metadata_url" "$old_file")
        $(replace_value "spring.datasource.url=" "$metadata_url_value" "$new_file_tmp")
        metadata_user_value=$(get_value "metadata_user" "$old_file")           
        $(replace_value "spring.datasource.username=" "$metadata_user_value" "$new_file_tmp")
        metadata_pass_value=$(get_value "metadata_pass" "$old_file")           
        $(replace_value "spring.datasource.password=" "$metadata_pass_value" "$new_file_tmp")                      
        check
    fi
fi

# if no old settings were removed in old file from new file, the backup for old file is not needed
if [[ "$keep_backup" == 0 ]]; then
    # the .bak file is not needed 
    logn "Removing backup file... "
    rm -f "$old_bak"
    check
fi

# ---------------------------------------------
#6 Remove old_file properties file and rename new_file.tmp to the original file
rm -f "$old_file" && mv "$new_file_tmp" "$old_file" # rename new_file.tmp to old file.
log "Done, updating existing properties completed."
