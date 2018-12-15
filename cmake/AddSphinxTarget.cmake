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
# Copyright 2003-2016 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.

#
# Source: LLVM 7.0, with some modifications
#
# Handy function for creating the different Sphinx targets (doc-html, doc-pdf, ...)
#
# ``builder`` should be one of the supported builders used by the sphinx-build command.
# If it is set to pdf, then the doc-latex target is added and doc-pdf depends on it.
#
function (AddSphinxTarget builder project)

  set(real_builder "${builder}")
  if (builder STREQUAL pdf)
    set(real_builder "latex") # pdf target needs to run intermediate target latex
  endif()

  set(SPHINX_BUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/${real_builder}")
  set(SPHINX_DOC_TREE_DIR "${CMAKE_CURRENT_BINARY_DIR}/_doctrees-${real_builder}")
  set(SPHINX_TARGET_NAME doc-${builder})
  set(REAL_SPHINX_TARGET_NAME "doc-${real_builder}")

  set(SPHINX_WARNINGS_AS_ERRORS_FLAG "")
  if (SPHINX_WARNINGS_AS_ERRORS)
    set(SPHINX_WARNINGS_AS_ERRORS_FLAG "-W")
  endif()

  # ----------------------------------------------------------------------------
  # add target doc-html, doc-latex, etc.
  # ----------------------------------------------------------------------------
  add_custom_target(${REAL_SPHINX_TARGET_NAME}
                    COMMAND ${SPHINX_EXECUTABLE}
                            -b ${real_builder}
                            -d "${SPHINX_DOC_TREE_DIR}"
                            -q # Quiet: no output other than errors and warnings.
                            ${SPHINX_WARNINGS_AS_ERRORS_FLAG} # Treat warnings as errors if requested
                            "${CMAKE_CURRENT_SOURCE_DIR}" # Source
                            "${SPHINX_BUILD_DIR}" # Output
                    COMMENT "generating ${real_builder} documentation")

  # pdf target requires an extra make all-pdf in the latex build dir
  if (builder STREQUAL pdf)
    add_custom_target(${SPHINX_TARGET_NAME}
                    COMMAND ${SED_EXECUTABLE} -i 's/\\\[utf8\\\]/[utf8x]/g' "${SPHINX_BUILD_DIR}/${project}.tex"
                    COMMAND make -C "${SPHINX_BUILD_DIR}" all-pdf LATEXMKOPTS="-silent" LATEXOPTS="-halt-on-error"
                    COMMENT "generating ${builder} documentation (output log ${SPHINX_BUILD_DIR}/${project}.log)")
    add_dependencies(${SPHINX_TARGET_NAME} ${REAL_SPHINX_TARGET_NAME})
  endif()

  # When "clean" target is run, remove the Sphinx build directory
  set_property(DIRECTORY APPEND PROPERTY
               ADDITIONAL_MAKE_CLEAN_FILES
               "${SPHINX_BUILD_DIR}")

  # We need to remove ${SPHINX_DOC_TREE_DIR} when make clean is run
  # but we should only add this path once
  get_property(_CURRENT_MAKE_CLEAN_FILES
               DIRECTORY PROPERTY ADDITIONAL_MAKE_CLEAN_FILES)
  list(FIND _CURRENT_MAKE_CLEAN_FILES "${SPHINX_DOC_TREE_DIR}" _INDEX)
  if (_INDEX EQUAL -1)
    set_property(DIRECTORY APPEND PROPERTY
                 ADDITIONAL_MAKE_CLEAN_FILES
                 "${SPHINX_DOC_TREE_DIR}")
  endif()

  # ----------------------------------------------------------------------------
  # set dependency target doc -> doc-pdf/doc-html/.. and handle installation
  # ----------------------------------------------------------------------------

  if (GENERATE_DOCS)

    add_dependencies(doc ${SPHINX_TARGET_NAME})

    if (builder STREQUAL man)
        set(SPHINX_OUTPUT_DST_DIR "${DOC_DIR}/man1")
        # Slash indicates contents of
        set(SPHINX_OUTPUT_SRC_DIR "${SPHINX_BUILD_DIR}/")
    else()
        set(SPHINX_OUTPUT_DST_DIR "${DOC_DIR}/${builder}")

        if (builder STREQUAL html)
            # '/.' indicates: copy the contents of the directory directly into
            # the specified destination, without recreating the last component
            # of ${SPHINX_BUILD_DIR} implicitly.
            set(SPHINX_OUTPUT_SRC_DIR "${SPHINX_BUILD_DIR}/.")
        elseif (builder STREQUAL pdf)
            set(SPHINX_OUTPUT_SRC_DIR "${SPHINX_BUILD_DIR}/${project}.pdf")
        endif()
    endif()

    if (builder STREQUAL pdf)
        install(FILES "${SPHINX_OUTPUT_SRC_DIR}"
                COMPONENT "doc-${builder}"
                DESTINATION "${SPHINX_OUTPUT_DST_DIR}")
    else()
        install(DIRECTORY "${SPHINX_OUTPUT_SRC_DIR}"
                COMPONENT "doc-${builder}"
                DESTINATION "${SPHINX_OUTPUT_DST_DIR}")
    endif()

  endif()
endfunction()
