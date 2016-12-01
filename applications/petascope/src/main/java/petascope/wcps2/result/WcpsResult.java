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
 * Class that encapsulates the result of the evaluation of a WCPS node.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class WcpsResult implements VisitorResult {

    //the rasql string resulting from the evaluation
    private String rasql;
    //the metadata resulting from the evaluation
    private WcpsCoverageMetadata metadata;

    private String mimeType;

    public WcpsResult(WcpsCoverageMetadata metadata, String rasql) {
        this.rasql = rasql;
        this.metadata = metadata;
    }

    // Used when create a new rasql query for multipart purpose
    public void setRasql(String rasql) {
        this.rasql = rasql;
    }

    public String getRasql() {
        return rasql;
    }

    public WcpsCoverageMetadata getMetadata() {
        return metadata;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    // rasql query can be encode in multiple types (e.g: tiff, png, csv,...)
    // then need to get the correct MIME for each type
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
        // e.g: dem() is not MIME then will return null
        if (this.mimeType == null) {
            this.mimeType = FormatExtension.MIME_TEXT;
        }
    }
}