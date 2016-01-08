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

# extend the AM_CXXFLAGS since it is set in configure.ac
AM_CXXFLAGS=@AM_CXXFLAGS@ @CLIENTCXXFLAGS@ @BASEDBCXXFLAGS@ -Wno-write-strings
AM_LDFLAGS=

if BASEDB_SQLITE
TESTS=test_blobfstransactionlock test_blobfstransaction test_blobfs test_blobfile

check_PROGRAMS=test_blobfs test_blobfstransactionlock test_blobfstransaction test_blobfile
test_blobfs_SOURCES= test_blobfs.cc
test_blobfstransaction_SOURCES= test_blobfstransaction.cc
test_blobfstransactionlock_SOURCES= test_blobfstransactionlock.cc
test_blobfile_SOURCES= test_blobfile.cc
endif

if BASEDB_PGSQL
check_PROGRAMS=test_tilecache
test_tilecache_SOURCES= test_tilecache.cc testing.cc testing.h
endif

LDADD= ../../rasodmg/librasodmg.a ../../clientcomm/libclientcomm.a \
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
               ../../raslib/libraslib.a ../../tilemgr/libtilemgr.a ../../server/rasserver_config.o \
               ../../commline/cmlparser.o ../../reladminif/libreladminif.a testing.o @GDAL_LDFLAGS@ @CLIENTLDFLAGS@ @BASEDBLDFLAGS@

if RMANRASNET
    LDADD += ../../rasserver_x/src/librasnetserver.a ../../rasnetprotocol/librasnetprotocol.a ../../rasnet/librasnet.a ../../common/src/libcommon.a @GRPC_LIBS@ @PROTOBUF_LIBS@ @BOOST_LDFLAGS@ -lboost_thread -lboost_regex -lboost_program_options -lboost_system
endif
