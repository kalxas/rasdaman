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

import petascope.exceptions.ExceptionCode;
import petascope.wcps2.error.managed.processing.WCPSProcessingError;
import petascope.wcps2.result.VisitorResult;
import petascope.wcps2.result.WcpsMetadataResult;
import petascope.wcps2.result.WcpsResult;

/**
 * Executor Factory for WcpsMetaExecutor and WcpsRasqlExecutor
 *
 * @author <a href="mailto:bphamhuux@jacobs-university.de">Bang Pham Huu</a>
 */
public class WcpsExecutorFactory {

    public WcpsExecutorFactory(WcpsMetaExecutor wcpsMetadataExecutor, WcpsRasqlExecutor wcpsRasqlExecutor) {
        this.wcpsMetadataExecutor = wcpsMetadataExecutor;
        this.wcpsRasqlExecutor = wcpsRasqlExecutor;
    }

    public WcpsExecutor getExecutor(VisitorResult result) {
        // Get result from meta value
        if (result instanceof WcpsMetadataResult) {
            return wcpsMetadataExecutor;
        } // Execute Rasql and get the value
        else if (result instanceof WcpsResult) {
            return wcpsRasqlExecutor;
        } else {
            throw new WCPSProcessingError("Cannot get the executor to get the result from translated tree.", ExceptionCode.WcpsError);
        }
    }

    private final WcpsMetaExecutor wcpsMetadataExecutor;
    private final WcpsRasqlExecutor wcpsRasqlExecutor;
}
