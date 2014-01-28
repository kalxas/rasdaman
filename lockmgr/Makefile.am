# -*-Makefile-*- (for Emacs)
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
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# MAKEFILE FOR:
#   module lockmgr
#
#
# COMMENTS:
#
#
##################################################################
AM_CXXFLAGS=@BASEDBCXXFLAGS@
AM_LDFLAGS=@BASEDBLDFLAGS@

.SUFFIXES= .@EMBEDDEDSQLEXT@ .@EMBEDDEDSQLOUT@
.@EMBEDDEDSQLEXT@.@EMBEDDEDSQLOUT@:
	$(EMBEDDEDSQLPRECOMPILER) $@ $<

lib_LIBRARIES=liblockmanager.a
liblockmanager_a_SOURCES=lockmanager.cc lockmanager.hh ecpg_lockmanager.cc ecpg_lockmanager.hh
EXTRA_liblockmanager_a_SOURCES=ecpg_LockManager.pgc

liblockmanager_a_LIBADD= lockmanager.$(OBJEXT)
liblockmanager_a_DEPENDENCIES= lockmanager.$(OBJEXT)

BUILT_SOURCES= ecpg_lockmanager.@EMBEDDEDSQLOUT@

CLEANFILES= ecpg_lockmanager.@EMBEDDEDSQLOUT@
