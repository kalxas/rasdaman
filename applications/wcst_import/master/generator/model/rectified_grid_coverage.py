"""
 *
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""

from master.generator.model.gml_coverage import GMLCoverage


class RectifiedGridCoverage(GMLCoverage):
    def __init__(self, id, boundedBy, domainSet, rangeSet, rangeType, coverageMetadata=None, overview_index=None):
        """
        Model class for a rectified grid coverage
        :param str id: the coverage id
        :param BoundedBy boundedBy: the boundedby element
        :param DomainSetRegular domainSet: the domain set
        :param RangeSet rangeSet: the range set
        :param RangeType rangeType: the range type
        :param CoverageMetadata coverageMetadata: the extra metadata of the coverage
        """
        super(RectifiedGridCoverage, self).__init__(self.__COVERAGE_TYPE, id, boundedBy, domainSet, rangeSet, rangeType,
                                                    coverageMetadata, overview_index)

    __COVERAGE_TYPE = "RectifiedGridCoverage"
