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
# Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#

# Source: LLVM 7.0, with some modifications
#
# CMake find_package() Module for Sphinx documentation generator
# http://sphinx-doc.org/
#
# Example usage:
#
# find_package(Sphinx)
#
# If successful the following variables will be defined
# SPHINX_FOUND
# SPHINX_EXECUTABLE

find_program(SPHINX_EXECUTABLE
             NAMES sphinx-build sphinx-build2
             HINTS $ENV{SPHINX_DIR}
             PATH_SUFFIXES bin
             DOC "Path to sphinx-build executable")

# Handle REQUIRED and QUIET arguments
# this will also set SPHINX_FOUND to true if SPHINX_EXECUTABLE exists
include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Sphinx DEFAULT_MSG
                                  SPHINX_EXECUTABLE)

# Provide options for controlling different types of output
option(SPHINX_OUTPUT_HTML "Output standalone HTML documentation" ON)
option(SPHINX_OUTPUT_PDF "Output standalone PDF documentation" ON)
option(SPHINX_OUTPUT_MAN "Output standalone MAN documentation" OFF)
option(SPHINX_WARNINGS_AS_ERRORS "When building documentation treat warnings as errors" ON)

mark_as_advanced(SPHINX_EXECUTABLE)
