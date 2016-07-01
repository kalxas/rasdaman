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
package petascope.wcps2.result;

import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcs2.extensions.FormatExtension;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WcpsMetadataResult implements VisitorResult {

    private String result;
    private String mimeType;
     //the metadata resulting from the evaluation
    private WcpsCoverageMetadata metadata;

    public WcpsMetadataResult(WcpsCoverageMetadata metadata, String result) {
        this.metadata = metadata;
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public WcpsCoverageMetadata getMetadata() {
        return metadata;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    // Normally mimeType is null as this WCPS query does not have "encoding()"
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
        if (this.mimeType == null) {
            this.mimeType = FormatExtension.MIME_TEXT;
        }
    }

}
