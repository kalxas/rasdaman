/*
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
 */
package org.rasdaman.admin.pyramid.service;

import com.rasdaman.admin.service.AbstractAdminService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.admin.pyramid.model.PyramidMember;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.domain.cis.CoveragePyramid;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static petascope.controller.AbstractController.getValueByKey;
import petascope.core.KVPSymbols;
import static petascope.core.KVPSymbols.KEY_BASE;
import static petascope.core.KVPSymbols.KEY_LIST_PYRAMID_MEMBERS;
import static petascope.core.KVPSymbols.KEY_REQUEST;
import petascope.core.response.Response;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.util.JSONUtil;
import petascope.util.MIMEUtil;
import petascope.util.SetUtil;

/**
 * Class to list the persisted pyramid member coverages of a base coverage
 *
 * /rasdaman/admin? REQUEST=ListPyramidMembers & BASE=Sentinel2_10m
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class ListPyramidMemberService extends AbstractAdminService {
    
    @Autowired
    private CoverageRepositoryService coverageRepositoryService;

    private static Set<String> VALID_PARAMETERS = SetUtil.createLowercaseHashSet(KEY_REQUEST, KEY_BASE);

    public ListPyramidMemberService() {
        this.service = KEY_LIST_PYRAMID_MEMBERS;
        this.request = KEY_LIST_PYRAMID_MEMBERS;
    }

    private void validate(Map<String, String[]> kvpParameters) throws PetascopeException {
        this.validateRequiredParameters(kvpParameters, VALID_PARAMETERS);
    }
    
    private String parseBaseCoverageId(Map<String, String[]> kvpParameters) throws PetascopeException {
        String baseCoverageId = getValueByKey(kvpParameters, KEY_BASE);
        if (!this.coverageRepositoryService.isInCache(baseCoverageId)) {
            throw new PetascopeException(ExceptionCode.InvalidRequest, "Base coverage '" + baseCoverageId + "' does not exist.");
        }
        
        return baseCoverageId;
    }

    @Override
    public Response handle(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
        this.validate(kvpParameters);
        
        String baseCoverageId = this.parseBaseCoverageId(kvpParameters);
        Coverage baseCoverage = this.coverageRepositoryService.readCoverageFullMetadataByIdFromCache(baseCoverageId);
        
        List<PyramidMember> pyramidMembers = new ArrayList<>();
        for (CoveragePyramid coveragePyramid : baseCoverage.getPyramid()) {
            PyramidMember member = new PyramidMember(coveragePyramid.getPyramidMemberCoverageId(), coveragePyramid.getScaleFactors());
            pyramidMembers.add(member);
        }
        
        String json = JSONUtil.serializeObjectToJSONString(pyramidMembers);
        List<byte[]> datas = Arrays.asList(json.getBytes());
        
        Response result = new Response(datas, MIMEUtil.MIME_JSON);
        return result;
    }

}
