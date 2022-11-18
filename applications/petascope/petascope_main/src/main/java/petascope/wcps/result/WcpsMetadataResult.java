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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcps.result;

import petascope.util.MIMEUtil;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class WcpsMetadataResult extends VisitorResult {
    
    // This object is temporary data to be stored
    private Object tmpObject;
    
    public WcpsMetadataResult(WcpsCoverageMetadata metadata, String result) {
        this.metadata = metadata;
        this.result = result;
    }
    
    public WcpsMetadataResult(Object tmpObject) {
        this.tmpObject = tmpObject;
    }

    @Override
    public String getMimeType() {
        return this.mimeType;
    }

    // Normally mimeType is null as this WCPS query does not have "encoding()"
    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
        if (this.mimeType == null) {
            this.mimeType = MIMEUtil.MIME_XML;
        }
    }

    public Object getTmpObject() {
        return tmpObject;
    }

}
