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
AM_CXXFLAGS= @AM_CXXFLAGS@ @CLIENTCXXFLAGS@
AM_LDFLAGS=@CLIENTLDFLAGS@

TESTS = 
check_PROGRAMS = test_alignedtiling test_benchmark test_bmark_dir test_bmark_dir1 \
                 test_bmark_int test_bmark_int1 test_bmark_pet test_bmark_stat \
                 test_breakdown test_collection test_dirtiling test_gmarray \
                 test_insert test_insert3 test_interesttiling test_iterator \
                 test_lookup test_marray test_oqlquery test_query \
                 test_ref test_set test_stattiling test_storage test_transaction \
                 gen_pattern deletecollection defconv defdiff \
                 system_basic system_compare system_query system_update

test_alignedtiling_SOURCES = test_alignedtiling.cc template_inst.hh
test_benchmark_SOURCES = test_benchmark.cc template_inst.hh
test_bmark_dir_SOURCES = test_bmark_dir.cc template_inst.hh
test_bmark_dir1_SOURCES = test_bmark_dir1.cc template_inst.hh
test_bmark_int_SOURCES = test_bmark_int.cc template_inst.hh
test_bmark_int1_SOURCES = test_bmark_int1.cc template_inst.hh
test_bmark_pet_SOURCES = test_bmark_pet.cc template_inst.hh
test_bmark_stat_SOURCES = test_bmark_stat.cc template_inst.hh
test_breakdown_SOURCES = test_breakdown.cc template_inst.hh
test_collection_SOURCES = test_collection.cc template_inst.hh
test_dirtiling_SOURCES = test_dirtiling.cc template_inst.hh
test_gmarray_SOURCES = test_gmarray.cc template_inst.hh
test_insert_SOURCES = test_insert.cc template_inst.hh
test_insert3_SOURCES = test_insert3.cc template_inst.hh
test_interesttiling_SOURCES = test_interesttiling.cc template_inst.hh
test_iterator_SOURCES = test_iterator.cc template_inst.hh
test_lookup_SOURCES = test_lookup.cc template_inst.hh
test_marray_SOURCES = test_marray.cc template_inst.hh
test_oqlquery_SOURCES = test_oqlquery.cc template_inst.hh
test_query_SOURCES = test_query.cc template_inst.hh
test_ref_SOURCES = test_ref.cc template_inst.hh
test_set_SOURCES = test_set.cc template_inst.hh
test_stattiling_SOURCES = test_stattiling.cc template_inst.hh
test_storage_SOURCES = test_storage.cc template_inst.hh
test_transaction_SOURCES = test_transaction.cc template_inst.hh

gen_pattern_SOURCES = gen_pattern.cc
deletecollection_SOURCES = deletecollection.cc
defconv_SOURCES = defconv.cc template_inst.hh
defdiff_SOURCES = defdiff.cc

system_basic_CXXFLAGS = -DTESTBASIC 
system_basic_SOURCES = system_basic.cc template_inst.hh hexcodec.cc import_error.cc
system_compare_SOURCES = system_compare.cc template_inst.hh hexcodec.cc import_error.cc system_basic.cc
system_query_SOURCES = system_query.cc template_inst.hh hexcodec.cc import_error.cc system_basic.cc
system_update_SOURCES = system_update.cc template_inst.hh hexcodec.cc import_error.cc system_basic.cc


LDADD = ../../rasodmg/librasodmg.a ../../clientcomm/libclientcomm.a \
	../../raslib/libraslib.a ../../conversion/libconversion.a \
	../../raslib/libraslib.a ../../network/libnetwork.a \
	../../commline/libcommline.a ../../qlparser/libqlparser.a

SUBDIRS = ../../mymalloc
