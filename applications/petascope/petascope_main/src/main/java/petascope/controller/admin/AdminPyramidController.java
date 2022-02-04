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

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.rasdaman.admin.pyramid.service.AdminAddPyramidMemberService;
import org.rasdaman.admin.pyramid.service.AdminCreatePyramidMemberService;
import org.rasdaman.admin.pyramid.service.AdminListPyramidMemberService;
import org.rasdaman.admin.pyramid.service.AdminRemovePyramidMemberService;
import static org.rasdaman.config.ConfigManager.ADMIN;
import static org.rasdaman.config.ConfigManager.COVERAGE;
import static org.rasdaman.config.ConfigManager.PYRAMID;
import org.rasdaman.config.VersionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import petascope.controller.AbstractController;
import petascope.controller.RequestHandlerInterface;
import petascope.core.KVPSymbols;
import petascope.core.response.Response;
import petascope.exceptions.PetascopeException;
import petascope.util.ExceptionUtil;

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
        this.handleListPyramidMembers(httpServletRequest);
    }

    @RequestMapping(path = LIST_PYRAMID_MEMBERS_PATH,  method = RequestMethod.POST)
    protected void handleListPyramidMembersPost(HttpServletRequest httpServletRequest) throws Exception {
        this.handleListPyramidMembers(httpServletRequest);
    }
    
    private void handleListPyramidMembers(HttpServletRequest httpServletRequest) throws IOException, Exception {
        Map<String, String[]> kvpParameters = this.parseKvpParametersFromRequest(httpServletRequest);
        
        RequestHandlerInterface requestHandlerInterface = () -> {
            try {
                Response response = this.listPyramidMemberService.handle(httpServletRequest, kvpParameters);
                this.writeResponseResult(response);
            } catch (Exception ex) {
                ExceptionUtil.handle(VersionManager.getLatestVersion(KVPSymbols.WCS_SERVICE), ex, this.injectedHttpServletResponse);
            }
        };
        
        super.handleRequest(kvpParameters, requestHandlerInterface);        
    }
    
    // --- 2. handle Add pyramid members request
    
    @RequestMapping(path = ADD_PYRAMID_MEMBERS_PATH,  method = RequestMethod.GET)
    protected void handleAddPyramidMembersGet(HttpServletRequest httpServletRequest) throws Exception {
        this.handleAddPyramidMemebers(httpServletRequest);
    }

    @RequestMapping(path = ADD_PYRAMID_MEMBERS_PATH,  method = RequestMethod.POST)
    protected void handleAddPyramidMembersPost(HttpServletRequest httpServletRequest) throws Exception {
        this.handleAddPyramidMemebers(httpServletRequest);
    }
    
    private void handleAddPyramidMemebers(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, String[]> kvpParameters = this.parseKvpParametersFromRequest(httpServletRequest);
        
        RequestHandlerInterface requestHandlerInterface = () -> {
            try {
                this.validateWriteRequestFromIP(httpServletRequest);

                this.addPyramidMemberService.handle(httpServletRequest, kvpParameters);
            } catch (Exception ex) {
                ExceptionUtil.handle(VersionManager.getLatestVersion(KVPSymbols.WCS_SERVICE), ex, this.injectedHttpServletResponse);
            }
        };
        
        super.handleRequest(kvpParameters, requestHandlerInterface);
    }
    
    // --- 3. handle remove pyramid members request
    
    @RequestMapping(path = REMOVE_PYRAMID_MEMBERS_PATH,  method = RequestMethod.GET)
    protected void handleRemovePyramidMembersGet(HttpServletRequest httpServletRequest) throws Exception {
        this.handleRemovePyramidMembers(httpServletRequest);
    }

    @RequestMapping(path = REMOVE_PYRAMID_MEMBERS_PATH,  method = RequestMethod.POST)
    protected void handleRemovePyramidMembersPost(HttpServletRequest httpServletRequest) throws Exception {
        this.handleRemovePyramidMembers(httpServletRequest);
    }
    
    private void handleRemovePyramidMembers(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, String[]> kvpParameters = this.parseKvpParametersFromRequest(httpServletRequest);
        
        RequestHandlerInterface requestHandlerInterface = () -> {
            try {
                this.validateWriteRequestFromIP(httpServletRequest);

                this.removePyramidMemberService.handle(httpServletRequest, kvpParameters);
            } catch (Exception ex) {
                ExceptionUtil.handle(VersionManager.getLatestVersion(KVPSymbols.WCS_SERVICE), ex, this.injectedHttpServletResponse);
            }
        };
        
        super.handleRequest(kvpParameters, requestHandlerInterface);        
    }
    
    // --- 4. handle Create pyramid members request
    
    @RequestMapping(path = CREATE_PYRAMID_MEMBERS_PATH,  method = RequestMethod.GET)
    protected void handleCreatePyramidMembersGet(HttpServletRequest httpServletRequest) throws Exception {
        this.handleCreatePyramidMembers(httpServletRequest);
    }

    @RequestMapping(path = CREATE_PYRAMID_MEMBERS_PATH,  method = RequestMethod.POST)
    protected void handleCreatePyramidMembersPost(HttpServletRequest httpServletRequest) throws Exception {
        this.handleCreatePyramidMembers(httpServletRequest);
    }
    
    private void handleCreatePyramidMembers(HttpServletRequest httpServletRequest) throws Exception {
        Map<String, String[]> kvpParameters = this.parseKvpParametersFromRequest(httpServletRequest);
        
        RequestHandlerInterface requestHandlerInterface = () -> {
            try {
                this.validateWriteRequestFromIP(httpServletRequest);

                this.createPyramidMemberService.handle(httpServletRequest, kvpParameters);
            } catch (Exception ex) {
                ExceptionUtil.handle(VersionManager.getLatestVersion(KVPSymbols.WCS_SERVICE), ex, this.injectedHttpServletResponse);
            }
        };
        
        super.handleRequest(kvpParameters, requestHandlerInterface);
    }

            
    @Override
    protected void handleGet(HttpServletRequest httpServletRequest) throws Exception {
    }
    

    @Override
    protected void requestDispatcher(HttpServletRequest httpServletRequest, Map<String, String[]> kvpParameters) throws PetascopeException {
    }
    
}
