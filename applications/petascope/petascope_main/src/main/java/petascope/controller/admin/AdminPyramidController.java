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
package petascope.controller.admin;

import com.rasdaman.accesscontrol.service.AuthenticationService;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.admin.pyramid.service.AdminAddPyramidMemberService;
import org.rasdaman.admin.pyramid.service.AdminCreatePyramidMemberService;
import org.rasdaman.admin.pyramid.service.AdminListPyramidMemberService;
import org.rasdaman.admin.pyramid.service.AdminRemovePyramidMemberService;
import static org.rasdaman.config.ConfigManager.ADMIN;
import static org.rasdaman.config.ConfigManager.COVERAGE;
import static org.rasdaman.config.ConfigManager.PYRAMID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import petascope.controller.AbstractController;
import static petascope.core.RoleSymbols.PRIV_OWS_WCS_INSERT_COV;
import static petascope.core.RoleSymbols.PRIV_OWS_WCS_UPDATE_COV;
import petascope.core.response.Response;

/**
 * Endpoint to handle request related to pyramid
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@RestController
public class AdminPyramidController extends AbstractController {
    
    private static final String LIST_PYRAMID_MEMBERS_PATH = ADMIN + "/" + COVERAGE + "/" + PYRAMID + "/list"; 
    private static final String ADD_PYRAMID_MEMBERS_PATH = ADMIN + "/" + COVERAGE + "/" + PYRAMID + "/add";
    private static final String REMOVE_PYRAMID_MEMBERS_PATH = ADMIN + "/" + COVERAGE + "/" + PYRAMID + "/remove"; 
    private static final String CREATE_PYRAMID_MEMBERS_PATH = ADMIN + "/" + COVERAGE + "/" + PYRAMID + "/create";
    
    @Autowired
    private AdminListPyramidMemberService listPyramidMemberService;
    @Autowired
    private AdminAddPyramidMemberService addPyramidMemberService;
    @Autowired
    private AdminRemovePyramidMemberService removePyramidMemberService;
    @Autowired
    private AdminCreatePyramidMemberService createPyramidMemberService;
    
    // --- 1. handle List pyramid members request
    
    @RequestMapping(path = LIST_PYRAMID_MEMBERS_PATH,  method = RequestMethod.GET)
    protected void handleListPyramidMembersGet(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        Response response = this.listPyramidMemberService.handle(httpServletRequest, kvpParameters);
        this.writeResponseResult(response);
    }

    @RequestMapping(path = LIST_PYRAMID_MEMBERS_PATH,  method = RequestMethod.POST)
    protected void handleListPyramidMembersPost(HttpServletRequest httpServletRequest) throws Exception {
        String postBody = this.getPOSTRequestBody(httpServletRequest);
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        Response response = this.listPyramidMemberService.handle(httpServletRequest, kvpParameters);
        this.writeResponseResult(response);
    }
    
    // --- 2. handle Add pyramid members request
    
    @RequestMapping(path = ADD_PYRAMID_MEMBERS_PATH,  method = RequestMethod.GET)
    protected void handleAddPyramidMembersGet(HttpServletRequest httpServletRequest) throws Exception {
        AuthenticationService.validateWriteRequestFromAdminOrRoleOrAllowedIP(httpServletRequest, PRIV_OWS_WCS_UPDATE_COV);
        
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());        
        this.addPyramidMemberService.handle(httpServletRequest, kvpParameters);
    }

    @RequestMapping(path = ADD_PYRAMID_MEMBERS_PATH,  method = RequestMethod.POST)
    protected void handleAddPyramidMembersPost(HttpServletRequest httpServletRequest) throws Exception {
        AuthenticationService.validateWriteRequestFromAdminOrRoleOrAllowedIP(httpServletRequest, PRIV_OWS_WCS_UPDATE_COV);
        
        String postBody = this.getPOSTRequestBody(httpServletRequest);
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        this.addPyramidMemberService.handle(httpServletRequest, kvpParameters);
    }
    
    // --- 3. handle remove pyramid members request
    
    @RequestMapping(path = REMOVE_PYRAMID_MEMBERS_PATH,  method = RequestMethod.GET)
    protected void handleRemovePyramidMembersGet(HttpServletRequest httpServletRequest) throws Exception {
        AuthenticationService.validateWriteRequestFromAdminOrRoleOrAllowedIP(httpServletRequest, PRIV_OWS_WCS_UPDATE_COV);
        
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        this.removePyramidMemberService.handle(httpServletRequest, kvpParameters);
    }

    @RequestMapping(path = REMOVE_PYRAMID_MEMBERS_PATH,  method = RequestMethod.POST)
    protected void handleRemovePyramidMembersPost(HttpServletRequest httpServletRequest) throws Exception {
        AuthenticationService.validateWriteRequestFromAdminOrRoleOrAllowedIP(httpServletRequest, PRIV_OWS_WCS_UPDATE_COV);
        
        String postBody = this.getPOSTRequestBody(httpServletRequest);
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        this.removePyramidMemberService.handle(httpServletRequest, kvpParameters);
    }
    
    // --- 4. handle Create pyramid members request
    
    @RequestMapping(path = CREATE_PYRAMID_MEMBERS_PATH,  method = RequestMethod.GET)
    protected void handleCreatePyramidMembersGet(HttpServletRequest httpServletRequest) throws Exception {
        AuthenticationService.validateWriteRequestFromAdminOrRoleOrAllowedIP(httpServletRequest, PRIV_OWS_WCS_INSERT_COV);
        
        Map<String, String[]> kvpParameters = this.buildGetRequestKvpParametersMap(httpServletRequest.getQueryString());
        this.createPyramidMemberService.handle(httpServletRequest, kvpParameters);
    }

    @RequestMapping(path = CREATE_PYRAMID_MEMBERS_PATH,  method = RequestMethod.POST)
    protected void handleCreatePyramidMembersPost(HttpServletRequest httpServletRequest) throws Exception {
        AuthenticationService.validateWriteRequestFromAdminOrRoleOrAllowedIP(httpServletRequest, PRIV_OWS_WCS_INSERT_COV);
        
        String postBody = this.getPOSTRequestBody(httpServletRequest);
        Map<String, String[]> kvpParameters = this.buildPostRequestKvpParametersMap(postBody);
        this.createPyramidMemberService.handle(httpServletRequest, kvpParameters);
    }

            
    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
        
    }
    

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws Exception {
    }
    
}
