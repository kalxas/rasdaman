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
####################################################################

if (GENERATE_DOCS)

    if (NOT TARGET doc)
        add_custom_target(doc ALL)
    endif()

    find_package(Sphinx)
    if (Sphinx_FOUND)

        string(TIMESTAMP CURRENT_YEAR "%Y")
        configure_file("${CMAKE_CURRENT_SOURCE_DIR}/conf.py.in"
                       "${CMAKE_CURRENT_SOURCE_DIR}/conf.py"
                       @ONLY)

        include(AddSphinxTarget)
        AddSphinxTarget(html rasdaman)

        if (GENERATE_DOCS_PDF)
            find_package(LATEX COMPONENTS PDFLATEX)
            find_package(LatexMk)
            if (LATEX_PDFLATEX_FOUND AND LATEXMK_FOUND)
                AddSphinxTarget(pdf rasdaman)
            endif()
        endif()

    endif()

endif ()
