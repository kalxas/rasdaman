# -*-Makefile-*-
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
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# MAKEFILE FOR:
#   directql query utility
#
#
# COMMENTS:
#
##################################################################

AM_CXXFLAGS=@CLIENTCXXFLAGS@ @BASEDBCXXFLAGS@
AM_LDFLAGS=

bin_PROGRAMS=directql
directql_SOURCES= directql.cc directql_error.cc directql_error.hh \
               directql_signal.cc directql_signal.hh
directql_LDADD = ../../rasodmg/librasodmg.a ../../clientcomm/libclientcomm.a \
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
               ../../catalogmgr/libcatalogmgr.a ../../lockmgr/liblockmanager.a \
               ../../raslib/libraslib.a ../../tilemgr/libtilemgr.a ../../server/rasserver_config.o ../../reladminif/libreladminif.a @GDAL_LDFLAGS@ @CLIENTLDFLAGS@ @BASEDBLDFLAGS@

if RMANRASNET
  directql_LDADD += ../../rasnet/librasnet.a ../../common/src/libcommon.a @GRPC_LIBS@ @PROTOBUF_LIBS@ @BOOST_LDFLAGS@ -lboost_thread -lboost_regex -lboost_program_options -lboost_system
endif


SUBDIRS= ../../rasodmg ../../clientcomm \
         ../../raslib ../../conversion ../../commline ../../network

$(RECURSIVE_CLEAN_TARGETS):
	@$(MAKE) $(AM_MAKEFLAGS) `echo $@ | sed s/-recursive/-am/`

