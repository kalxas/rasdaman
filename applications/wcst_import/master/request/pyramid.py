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
 * Copyright 2003 - 2021 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
from collections import OrderedDict

from config_manager import ConfigManager
from master.request.admin import AdminRequest


class CreatePyramidMemberRequest(AdminRequest):

    def __init__(self, base_coverage_id, pyramid_member_coverage_id, scale_factors, interpolation=None):
        """
        Class to request to create a downscaled level coverage and add this coverage to a base coverage's pyramid

        e.g. Example: /rasdaman/admin/coverage/pyramid/CreatePyramidMember
                                     & COVERAGEID=S2
                                     & MEMBER=S2_4
                                     & SCALEVECTOR=4,4,1
                                     & INTERPOLATION=linear,linear,nearest
        """
        super(CreatePyramidMemberRequest, self).__init__()

        self.context_path = ConfigManager.admin_service + "/coverage/pyramid/create"

        self.base_coverage_id = base_coverage_id
        self.pyramid_member_coverage_id = pyramid_member_coverage_id
        # e.g: 1,2,2
        self.scale_factors = ",".join(str(x) for x in scale_factors)
        # e.g: nearest
        self.interpolation = ",".join(interpolation) if interpolation is not None else None

    def _get_request_type_parameters(self):
        request_kvp = OrderedDict()
        request_kvp["COVERAGEID"] = self.base_coverage_id
        request_kvp["MEMBER"] = self.pyramid_member_coverage_id
        request_kvp["SCALEVECTOR"] = self.scale_factors

        if self.interpolation is not None:
            request_kvp["INTERPOLATION"] = self.interpolation

        return request_kvp


class AddPyramidMemberRequest(AdminRequest):

    def __init__(self, base_coverage_id, pyramid_member_coverage_id, harvesting=False):
        """
        Class to request to add a downscaled level coverage to a base coverage's pyramid

        e.g. /rasdaman/admin/coverage/pyramid/add?
                            & COVERAGEID=Sentinel2_10m
                            & MEMBERS=Sentinel2_20m
                            & HARVESTING=true (default is false)
        """
        super(AddPyramidMemberRequest, self).__init__()

        self.context_path = self.context_path + "/coverage/pyramid/add"

        self.base_coverage_id = base_coverage_id
        self.pyramid_member_coverage_id = pyramid_member_coverage_id
        self.harvesting = harvesting

    def _get_request_type_parameters(self):
        request_kvp = OrderedDict()
        request_kvp["COVERAGEID"] = self.base_coverage_id
        request_kvp["MEMBERS"] = self.pyramid_member_coverage_id
        request_kvp["HARVESTING"] = self.harvesting

        return request_kvp


class ListPyramidMembersRequest(AdminRequest):

    def __init__(self, base_coverage_id):
        """
        Class to request to list the list pyramid members of a base coverage

        e.g. /rasdaman/admin/coverage/pyramid/list?COVERAGEID=test_wms_4326
        """
        super(ListPyramidMembersRequest, self).__init__()
        self.context_path = self.context_path + "/coverage/pyramid/list"

        self.base_coverage_id = base_coverage_id

    def _get_request_type_parameters(self):
        request_kvp = OrderedDict()
        request_kvp["COVERAGEID"] = self.base_coverage_id

        return request_kvp
