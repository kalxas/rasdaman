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
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003 - 2016 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

# Report link time if it's more than 3 seconds

# reset bash internal variable for elapsed seconds
SECONDS=0
# execute link command
"$@"
rc=$?

if [ $SECONDS -gt 3 ]; then
    if [[ "$1" == *ranlib* ]]; then
        RESULT="generate index in $2"
    elif [[ "$1" == *ar* ]]; then
        RESULT="create library $3"
    else
        RESULT="link executable $(echo $@ | sed 's/.*\-o \([^ ]*\) .*/\1/')"
    fi
    echo "...... ${SECONDS}s to $RESULT"
fi

exit $rc
