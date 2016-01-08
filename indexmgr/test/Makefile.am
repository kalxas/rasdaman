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
# Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
# 
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
# 
# MAKEFILE FOR:
# 
# COMMENTS: 
# 
##################################################################

# extend the AM_CXXFLAGS since it is set in configure.ac
AM_CXXFLAGS=@AM_CXXFLAGS@ @CLIENTCXXFLAGS@
AM_LDFLAGS=@CLIENTLDFLAGS@

TESTS = test_ix
check_PROGRAMS = test_ix

test_ix_SOURCES = test_ix.cc ../../mymalloc/mymalloc_cln.cc

LDADD = ../../rasodmg/librasodmg.a ../../clientcomm/libclientcomm.a \
	../../raslib/libraslib.a \
	../../conversion/libconversion.a ../../network/libnetwork.a

SUBDIRS = ../../mymalloc
