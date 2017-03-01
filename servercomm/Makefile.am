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
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.   
# 
# MAKEFILE FOR:  
#   module servercomm
# 
# COMMENTS: 
# 
##################################################################

# extend the AM_CXXFLAGS since it is set in configure.ac
AM_CXXFLAGS=@AM_CXXFLAGS@ @BASEDBCXXFLAGS@
AM_LDFLAGS=@BASEDBLDFLAGS@

lib_LIBRARIES=libservercomm.a
libservercomm_a_SOURCES=../clientcomm/rpcif_xdr.c ../clientcomm/rpcif_svc.cc servercomm.cc \
                        servercomm2.cc manager.cc callbackmgr.cc httpserver.cc \
                        ../mymalloc/mymalloc_svc.cc ../mymalloc/mymalloc.h \
                        ../clientcomm/rpcif.h callbackmgr.hh httpserver.hh httpserver.icc \
                        servercomm.hh servercomm.icc \
                        ../rnprotocol/rnprotocol.cc \
                        ../rnprotocol/rnprotocol.hh \
                        ../rnprotocol/rnpembedded.cc \
                        ../rnprotocol/rnpembedded.hh \
                        ../rnprotocol/rnpcommunication.cc \
                        ../rnprotocol/rnpcommunication.hh \
                        ../rnprotocol/rnpserver.cc \
                        ../rnprotocol/rnpserver.hh \
                        ../rnprotocol/srvrasmgrcomm.cc \
                        ../rnprotocol/srvrasmgrcomm.hh \
                        ../rnprotocol/rnprasserver.cc \
                        ../rnprotocol/rnprasserver.hh \
                        ../rnprotocol/rnpservercomm.cc \
                        ../rnprotocol/rnpservercomm.hh

if RMANRASNET
    libservercomm_a_SOURCES += ../rasnetprotocol/rasnetservercomm.cc \
                        ../rasnetprotocol/rasnetservercomm.hh
    libservercomm_a_CXXFLAGS = @AM_CXXFLAGS@ @DISABLE_PROTOBUF_WARNINGS_FLAGS@
endif

libservercomm_a_CPPFLAGS=-I../rnprotocol                        

EXTRA_libservercomm_a_SOURCES = rpcif.x    
BUILT_SOURCES= rpcif_clnt.cc rpcif_xdr.c rpcif.h rpcif_svc.cc
CLEANFILES=rpcif_clnt.cc rpcif_xdr.c rpcif.h rpcif.h.tmp rpcif_clnt.c.tmp \
           rpcif_svc.cc.tmp rpcif_svc.cc rpcif_clnt.cc.tmp

rpcif.h: rpcif.x    
	$(RPCGEN) -h -o rpcif.h.tmp rpcif.x
	$(AWK) -f rpcif.h.awk rpcif.h.tmp > rpcif.h
	rm -f rpcif.h.tmp

rpcif_clnt.cc: rpcif.x
	$(RPCGEN) -l -o rpcif_clnt.cc.tmp rpcif.x
	$(AWK) -f rpcif_clnt.c.awk rpcif_clnt.cc.tmp > rpcif_clnt.cc
	rm -f rpcif_clnt.cc.tmp

rpcif_xdr.c: rpcif.x
	-rm -f rpcif_xdr.c
	$(RPCGEN) -c -o rpcif_xdr.c rpcif.x

rpcif_svc.cc: rpcif.x
	$(RPCGEN) -m -o rpcif_svc.cc.tmp rpcif.x
	$(AWK) -f rpcif_svc.c.awk rpcif_svc.cc.tmp > rpcif_svc.cc
	rm -f rpcif_svc.cc.tmp

EXTRA_DIST = rpcif*

