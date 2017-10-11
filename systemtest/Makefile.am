# -*-Makefile-*-
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
#   perform systemtest (overall rasdaman test suite)
# 
# COMMENTS:
#
##################################################################
######################### Definitions ############################

# all test programs


########################### Targets ##############################

TESTSEQUENCE = testcases_mandatory/* testcases_open/* testcases_fixed/*

if ENABLE_JAVA
	TESTSEQUENCE = testcases_mandatory/* testcases_open/* testcases_fixed/* testcases_services/*
endif


.PHONY: all
all:
	cp "run_test.sh.in" "run_test.sh"; \
	sed -i "s|@TEST_SEQUENCE@|${TESTSEQUENCE}|g" "run_test.sh"
	

# general system test
.PHONY: check
check:
	bash run_test.sh ${bindir}
	exit $$?;

.PHONY: clean
clean:
	for testcase in testcases_*/test_*; do \
		rm -rf $${testcase}/*.log $${testcase}/failed_cases $${testcase}/output; \
	done;

######################## Dependencies ############################

