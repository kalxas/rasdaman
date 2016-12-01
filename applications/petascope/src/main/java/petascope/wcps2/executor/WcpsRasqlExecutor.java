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

package petascope.wcps2.executor;

import java.nio.charset.Charset;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCSException;
import petascope.util.ras.RasQueryResult;
import petascope.util.ras.RasUtil;
import petascope.wcps2.result.WcpsResult;

/**
 * Execute the Rasql query and return result.
 * @author <a href="mailto:bphamhuux@jacobs-university.de">Bang Pham Huu</a>
 */
public class WcpsRasqlExecutor implements WcpsExecutor<WcpsResult> {

    public WcpsRasqlExecutor() {}

    /**
     * Execute the Rasql query and return result.
     * @param wcpsResult
     * @return
     */
    public byte[] execute(WcpsResult wcpsResult) throws WCSException {
        byte[] result = new byte[0];

        RasQueryResult res = null;
        try {
            res = new RasQueryResult(RasUtil.executeRasqlQuery(wcpsResult.getRasql()));

            if (!res.getMdds().isEmpty() || !res.getScalars().isEmpty()) {
                for (String s : res.getScalars()) {
                    result = s.getBytes(Charset.forName("UTF-8"));
                }
                for (byte[] bs : res.getMdds()) {
                    result = bs;
                }
            } else {

            }
        } catch (Exception ex) {
            throw new WCSException(ExceptionCode.SemanticError, ex);
        }
        return result;
    }
}
