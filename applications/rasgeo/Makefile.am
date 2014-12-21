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
#   rasgeo
#
#
# COMMENTS:
#
##################################################################

AM_CXXFLAGS=@CLIENTCXXFLAGS@ @BASEDBCXXFLAGS@ @GDAL_CXXFLAGS@
AM_LDFLAGS=@CLIENTLDFLAGS@

bin_PROGRAMS=rasimport raserase
rasimport_SOURCES= rasimport.cc rasimport.hh RasdamanConnector.cc RasdamanHelper2.cc rasgeo_template_inst.hh RasdamanConnector.hh RasdamanHelper2.hh ../../debug/debug-clt.hh
rasimport_LDADD = ../../rasodmg/librasodmg.a ../../clientcomm/libclientcomm.a \
              ../../raslib/libraslib.a \
              ../../conversion/libconversion.a ../../commline/libcommline.a \
              ../../network/libnetwork.a \
              @GDAL_LDFLAGS@

if RMANRASNET
    rasimport_LDADD +=  ../../rasnet/src/librasnet.a ../../common/src/libcommon.a -L/usr/local/lib -lprotobuf -lzmq -lboost_thread -lboost_regex -lboost_program_options -lboost_system
endif

raserase_SOURCES= raserase.cc raserase.hh RasdamanConnector.cc RasdamanHelper2.cc rasgeo_template_inst.hh RasdamanConnector.hh RasdamanHelper2.hh ../../debug/debug-clt.hh
raserase_LDADD = ../../rasodmg/librasodmg.a ../../clientcomm/libclientcomm.a \
              ../../raslib/libraslib.a \
              ../../conversion/libconversion.a ../../commline/libcommline.a \
              ../../network/libnetwork.a \
              @GDAL_LDFLAGS@

if RMANRASNET
    raserase_LDADD += ../../rasnet/src/librasnet.a ../../common/src/libcommon.a -L/usr/local/lib -lprotobuf -lzmq -lboost_thread -lboost_regex -lboost_program_options -lboost_system
endif

SUBDIRS= ../../rasodmg ../../clientcomm \
         ../../raslib ../../conversion ../../commline ../../network \
	 wms-import/importtools wms-import/inittools wms-import/utilities
         
connectfile:
	if [ ! -d $(HOME)/.rasdaman ]; then \
		mkdir $(HOME)/.rasdaman; \
	fi
	cp rasconnect $(HOME)/.rasdaman/
