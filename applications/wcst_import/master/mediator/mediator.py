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
import json

from master.generator.model.bounded_by import BoundedBy
from master.generator.model.coverage_metadata import CoverageMetadata
from master.generator.model.domain_set_irregular import DomainSetIrregular
from master.generator.model.domain_set_regular import DomainSetRegular
from master.generator.model.range_set_file import RangeSetFile
from master.generator.model.range_set_tuple_list import RangeSetTupleList
from master.generator.model.range_type import RangeType
from master.generator.model.referenceable_grid_coverage import ReferenceableGridCoverage
from master.generator.model.grid_coverage import GridCoverage
from master.generator.model.rectified_grid_coverage import RectifiedGridCoverage
from master.provider.data.file_data_provider import FileDataProvider
from master.provider.data.tuple_list_data_provider import TupleListDataProvider
from master.provider.data.url_data_provider import UrlDataProvider
from master.provider.metadata.metadata_provider import MetadataProvider
from util.file_util import TmpFile, File


class Mediator:
    def __init__(self, metadata_provider, data_provider):
        """
        This class mediates the providers with the gml model
        :param MetadataProvider metadata_provider: the metadata provider
        :param DataProvider data_provider: the data provider
        :return:
        """
        self.metadata_provider = metadata_provider
        self.data_provider = data_provider

    def get_gml_coverage(self):
        """
        Returns a coverage model that can be transformed into a gml, simple check by:
        + An axis is irregular (such as Time) -> ReferenceableGridCoverage
        + A regular coverage (if grid_coverage option is true) -> GridCoverage (same as origin as in Rasdaman)
        + A regular coverage (if grid_coverage option is false) -> it is a RectifiedGridCoverage (e.g: map mosaic)

        :rtype: GMLCoverage
        """
        # ReferenceableGridCoverage
        cov_metadata = CoverageMetadata(self.metadata_provider.extra_metadata)
        if self.metadata_provider.is_coverage_irregular():
            return ReferenceableGridCoverage(self.metadata_provider.coverage_id, self._get_bounded_by(),
                                             self._get_domain_set(), self._get_range_set(), self._get_range_type(), cov_metadata,
                                             self.metadata_provider.overview_index)
        # GridCoverage
        elif self.metadata_provider.is_grid_coverage():
            return GridCoverage(self.metadata_provider.coverage_id, self._get_bounded_by(),
                                self._get_domain_set(), self._get_range_set(), self._get_range_type(), cov_metadata,
                                self.metadata_provider.overview_index)
        # RectifiedGridCoverage
        else:
            return RectifiedGridCoverage(self.metadata_provider.coverage_id, self._get_bounded_by(),
                                         self._get_domain_set(), self._get_range_set(), self._get_range_type(), cov_metadata,
                                         self.metadata_provider.overview_index)

    def get_gml_str(self):
        """
        Return the GML coverage as string
        :return: str
        """
        gml = self.get_gml_coverage().to_gml()
        return gml

    def get_gml_file(self):
        """
        Returns the file path to the file containing the coverage held by the mediator
        :rtype: File
        """
        gml = self.get_gml_str()
        return File(TmpFile().write_to_tmp_file(gml))

    def _get_bounded_by(self):
        mp = self.metadata_provider
        return BoundedBy(mp.get_crs(), mp.get_axis_labels(), mp.get_axis_uom_labels(),
                         mp.get_no_of_dimensions(), mp.get_lower_corner(), mp.get_upper_corner())

    def _get_domain_set(self):
        mp = self.metadata_provider
        if mp.is_coverage_irregular():
            return DomainSetIrregular(mp.get_no_of_dimensions(), mp.get_grid_low(), mp.get_grid_high(),
                                      mp.get_axis_labels_grid(), mp.get_axis_labels(), mp.get_crs(),
                                      mp.get_axis_uom_labels(),
                                      mp.get_origin(), mp.get_offset_vectors())
        else:
            return DomainSetRegular(mp.get_no_of_dimensions(), mp.get_grid_low(), mp.get_grid_high(),
                                    mp.get_axis_labels_grid(), mp.get_axis_labels(), mp.get_crs(),
                                    mp.get_axis_uom_labels(),
                                    mp.get_origin(), mp.get_offset_vectors(), mp.is_grid_coverage())

    def _get_range_set(self):
        dp = self.data_provider
        if isinstance(dp, FileDataProvider):
            file_structure = json.dumps(dp.get_structure()) if dp.get_structure() is not None else ""
            return RangeSetFile(dp.get_file_url(), dp.get_mimetype(), file_structure)
        if isinstance(dp, UrlDataProvider):
            return RangeSetFile(dp.get_url(), dp.get_mimetype())
        elif isinstance(dp, TupleListDataProvider):
            return RangeSetTupleList(dp.get_tuple_list())

    def _get_range_type(self):
        return RangeType(self.metadata_provider.get_range_fields())
