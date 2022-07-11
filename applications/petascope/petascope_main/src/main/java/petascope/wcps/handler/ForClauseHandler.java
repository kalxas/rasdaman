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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.rasdaman.domain.cis.Coverage;
import org.rasdaman.repository.service.CoverageRepositoryService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.service.CoverageAliasRegistry;
import petascope.wcps.result.WcpsResult;

/**
 * Translation node from wcps to rasql for the for clause. Example:  <code>
 * for $c1 in COL1
 * </code> translates to  <code>
 * COL1 as c1
 * </code>
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
@Service
// Create a new instance of this bean for each request (so it will not use the old object with stored data)
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ForClauseHandler extends Handler {

    @Autowired
    private CoverageAliasRegistry coverageAliasRegistry;
    @Autowired
    private CoverageRepositoryService coverageRepostioryService;
    
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ForClauseHandler.class);
    
    public ForClauseHandler() {
        
    }
    
    public ForClauseHandler create(Handler coverageIteratorHandler, Handler decodeCoverageHandler, List<Handler> coverageIdHandlers) {
        ForClauseHandler result = new ForClauseHandler();
        List<Handler> childHandlers = new ArrayList<>();
        childHandlers.add(coverageIteratorHandler);
        childHandlers.add(decodeCoverageHandler);
        childHandlers.addAll(coverageIdHandlers);
        
        result.setChildren(childHandlers);
        
        result.coverageAliasRegistry = this.coverageAliasRegistry;
        result.coverageRepostioryService = this.coverageRepostioryService;
        
        return result;
    }
    
    public WcpsResult handle() throws PetascopeException {
        Handler coverageIteratorHandler = this.getFirstChild();
        String coverageIterator = ((WcpsResult)coverageIteratorHandler.handle()).getRasql();
        
        List<String> coverageIds = new ArrayList<>();
        
        Handler decodeCoverageHandler = this.getSecondChild();
        String coverageIdFromDecodeExpression = null;
        if (decodeCoverageHandler != null) {
            coverageIdFromDecodeExpression = ((WcpsResult)decodeCoverageHandler.handle()).getRasql();
            coverageIds.add(coverageIdFromDecodeExpression);
        }
        
        List<Handler> coverageIdHandlers = this.getChildren().subList(2, this.getChildren().size());
        
        for (Handler coverageIdHandler : coverageIdHandlers) {
            String coverageId = ((WcpsResult)coverageIdHandler.handle()).getRasql();
            coverageIds.add(coverageId);
        }
        
        WcpsResult tmp = this.handle(coverageIterator, coverageIds);
        return tmp;
    }
    
    private WcpsResult handle(String coverageIterator, List<String> coverageIds) throws PetascopeException {
        
        List<String> rasdamanCollectionNames = new ArrayList<>();
        
        //add the mapping in the coverageRegistry
        for (String coverageId : coverageIds) {
            Coverage coverage = this.coverageRepostioryService.readCoverageFullMetadataByIdFromCache(coverageId);
            String rasdamanCollectionName = coverage.getRasdamanRangeSet().getCollectionName();
            
            
            if (rasdamanCollectionName != null) {
                rasdamanCollectionNames.add(rasdamanCollectionName);
            }
            coverageAliasRegistry.addCoverageMapping(coverageIterator, coverageId, rasdamanCollectionName);
        }
        
        String translatedCoverageIterator = coverageIterator;
        //if the coverageVariable starts with $, remove it to make it valid rasql
        if (coverageIterator.startsWith(COVERAGE_VARIABLE_PREFIX)) {
            translatedCoverageIterator = coverageIterator.replace(COVERAGE_VARIABLE_PREFIX, "");
        }

        // when coverageNames has size() > 0 then it is multipart and it will be handle in RasqlRewriteMultipartQueriesService
        // Then need to get only the first coverageName to create a rasql first.
        String rasql = "";
        if (coverageIds.size() > 1) {
            // Multipart query
            rasql = TEMPLATE.replace("$iterator", translatedCoverageIterator)
                            .replace("$collectionName", COLLECTION_NAME + "_" + translatedCoverageIterator);
        } else {
            if (rasdamanCollectionNames.size() > 0) {
                rasql = TEMPLATE.replace("$iterator", translatedCoverageIterator)
                                .replace("$collectionName", rasdamanCollectionNames.get(0));
            }
        }
        //metadata is loaded in the return clause, no meta needed here
        WcpsResult result = new WcpsResult(null, rasql);
        return result;
    }
    
    public static final String AS = "AS";
    private static final String TEMPLATE = "$collectionName " + AS + " $iterator";
    private static final String COVERAGE_VARIABLE_PREFIX = "$";
    public static final String COLLECTION_NAME = "$collectionName";
}
