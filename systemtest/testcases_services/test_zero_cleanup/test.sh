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
#    test.sh
# Description
#    This script will remove all imported coverages from WCST_Import so petascopedb should be empty after make check.
################################################################################
# get script name
PROG=$( basename $0 )

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ] ; do SOURCE="$(readlink "$SOURCE")"; done
  SCRIPT_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

. "$SCRIPT_DIR"/../../util/common.sh

rm -rf "$SCRIPT_DIR/output"

# This script will iterate the test data of test wcst_import and retrieve all imported coverages by folder name prefix (wcs_, wcps_, wms_)
# then will remove the coverageName with WCS DeleteCoverage service which will remove the imported coverage and correspondent WMS layers if available.
declare -a SERVICES=('error_ingest' 'wcs' 'wms' 'wcps')
DATA_FOLDER="$SCRIPT_DIR/../test_all_wcst_import/testdata"

# change directory to the DATA_FOLDER
cd "$DATA_FOLDER"

deleted_coverages=()

# list all the subdirectories of data folder
for d in */ ; do
	# check if folder name contains the services prefix
	for service in "${SERVICES[@]}"; do
		if [[ "$d" =~ "$service" ]]; then
            # get the template json file
            templateFile="$d/ingest.template.json"

            # get the coverageID from template file
            coverage_id=$(grep -Po -m 1 '"coverage_id":.*?[^\\]".*' $templateFile | awk -F'"' '{print $4}')

            # check if coverage was deleted in other folder, if not skip it
            found=0

            for deleted_coverage in "${deleted_coverages[@]}"; do
                [[ "$deleted_coverage" = "$coverage_id" ]] && found=1
            done

            if [[ "$found" -eq 0 ]]; then
                logn "Removing coverage: $coverage_id... "
                # remove the imported coverage
                delete_coverage "$coverage_id"
                check

                # add this coverage to list of deleted coverages
                deleted_coverages+=("$coverage_id")
            fi
		fi
	done
done

# mean_summer_airtemp is a demo coverage imported by petascope_insertdemo.sh and is used in WS client, tab WCS ProcessCoverages
# after the test interface for WS client, now it can be removed here as other imported test coverages
delete_coverage "mean_summer_airtemp"

log "done."
print_summary
exit_script
