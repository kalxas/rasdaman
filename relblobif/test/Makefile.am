# -*-Makefile-*-
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
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# MAKEFILE FOR:
#   rasdaman insitu test
#
#
# COMMENTS:
#
##################################################################

AM_CXXFLAGS=@CLIENTCXXFLAGS@ @BASEDBCXXFLAGS@
AM_LDFLAGS=@CLIENTLDFLAGS@

check_PROGRAMS=test_tilecache
test_tilecache_SOURCES= test_tilecache.cc testing.cc testing.hh ../../mymalloc/mymalloc_cln.cc ../../mymalloc/mymalloc_cln.h \
		  ../../debug/debug-clt.hh ../../debug/debug.hh ../../debug/debug-srv.hh \
                  ../../include/bool.h ../../include/globals.hh ../../include/rasdaman.hh \
                  ../../include/stdexcept.h

test_tilecache_LDADD= ../../rasodmg/librasodmg.a ../../clientcomm/libclientcomm.a \
              ../../raslib/libraslib.a \
              ../../conversion/libconversion.a ../../commline/libcommline.a \
              ../../network/libnetwork.a ../../relcatalogif/librelcatalogif.a \
              ../../relindexif/librelindexif.a ../../reladminif/libreladminif.a \
              ../../catalogmgr/libcatalogmgr.a ../../relcatalogif/librelcatalogif.a \
              ../../relstorageif/librelstorageif.a ../../storagemgr/libstoragemgr.a \
              ../../relindexif/librelindexif.a ../../indexmgr/libindexmgr.a \
              ../../tilemgr/libtilemgr.a ../../relblobif/librelblobif.a \
              ../../relcatalogif/librelcatalogif.a ../../servercomm/libservercomm.a \
               ../../mddmgr/libmddmgr.a ../../relmddif/librelmddif.a \
               ../../indexmgr/libindexmgr.a ../../qlparser/libqlparser.a ../../conversion/libconversion.a \
               ../../catalogmgr/libcatalogmgr.a \
               ../../raslib/libraslib.a ../../tilemgr/libtilemgr.a @GDAL_LDFLAGS@ @CLIENTLDFLAGS@ @BASEDBLDFLAGS@
