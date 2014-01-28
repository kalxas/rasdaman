#
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
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# MAKEFILE FOR:
#
# COMMENTS:
#
##################################################################

AM_CXXFLAGS=@BASEDBCXXFLAGS@ @CLIENTCXXFLAGS@
AM_LDFLAGS=@BASEDBLDFLAGS@

.SUFFIXES= .@EMBEDDEDSQLEXT@ .@EMBEDDEDSQLOUT@
.@EMBEDDEDSQLEXT@.@EMBEDDEDSQLOUT@:
	$(EMBEDDEDSQLPRECOMPILER) $@ $<


TESTS = test_lockmgr
check_PROGRAMS = test_lockmgr

test_lockmgr_SOURCES = test_lockmgr.cc

LDADD = ../ecpg_lockmanager.o @BASEDBLDFLAGS@ @GDAL_LDFLAGS@

SUBDIRS =
