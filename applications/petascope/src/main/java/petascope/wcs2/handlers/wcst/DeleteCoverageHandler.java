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
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

package petascope.wcs2.handlers.wcst;

import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import petascope.core.CoverageMetadata;
import petascope.core.DbMetadataSource;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.PetascopeException;
import petascope.exceptions.RasdamanException;
import petascope.exceptions.SecoreException;
import petascope.exceptions.WCSException;
import petascope.util.Pair;
import petascope.util.ras.RasUtil;
import petascope.wcs2.handlers.AbstractRequestHandler;
import petascope.wcs2.handlers.Response;
import petascope.wcs2.parsers.wcst.DeleteCoverageRequest;

/**
 * Handles the deletion of a coverage.
 * @autor <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class DeleteCoverageHandler extends AbstractRequestHandler<DeleteCoverageRequest> {

    public DeleteCoverageHandler(DbMetadataSource meta) {
        super(meta);
    }

    public Response handle(DeleteCoverageRequest request) throws PetascopeException, WCSException, SecoreException {
        String[] coverageIds = request.getCoverageId().split(COVERAGE_IDS_SEPARATOR);
        //check that all the ids exist
        for(String coverageId : coverageIds){
            checkCoverageId(coverageId);
        }
        //delete all of them
        for(String coverageId : coverageIds){
            //delete metadata
            CoverageMetadata coverage = meta.read(coverageId);
            meta.delete(coverage, true);
            //delete array
            deleteFromRasdaman(coverage);
        }
        return new Response("");
    }

    private void checkCoverageId(String coverageId) throws WCSException{
        if(!meta.existsCoverageName(coverageId)){
            throw new WCSException(ExceptionCode.NoSuchCoverage, coverageId);
        }
    }

    private void deleteFromRasdaman(CoverageMetadata coverage){
        Pair<BigInteger, String> collection = coverage.getRasdamanCollection();
        try {
            RasUtil.deleteFromRasdaman(collection.fst, collection.snd);
        } catch (RasdamanException ex) {
            Logger.getLogger(DeleteCoverageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private final static String COVERAGE_IDS_SEPARATOR = ",";
}
