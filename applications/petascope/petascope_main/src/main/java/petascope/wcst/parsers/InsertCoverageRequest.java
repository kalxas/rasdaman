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

package petascope.wcst.parsers;

import java.net.URL;

/**
 * Insert Coverage request, having 3 main components:
 * - coverage in GML format
 * - url to a coverage
 * - useId flag to indicate whether the coverageId should be generated or used as is
 *
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class InsertCoverageRequest extends AbstractWCSTRequest {

    /**
     * Class constructor
     * @param GMLCoverage: a coverage in GML formal
     * @param CoverageURL: a valid url to a coverage
     * @param useId: flag to indicate if the coverageId should be existing or new
     * @param pixelDataType
     * @param tiling
     */
    public InsertCoverageRequest(String GMLCoverage, URL CoverageURL, Boolean useId, String pixelDataType, String tiling) {
        this.GMLCoverage = GMLCoverage;
        this.CoverageURL = CoverageURL;
        this.useNewId = useId;
        this.pixelDataType = pixelDataType;
        this.tiling = tiling;
    }

    public String getGMLCoverage() {
        return GMLCoverage;
    }

    public URL getCoverageURL() {
        return CoverageURL;
    }

    public Boolean isUseNewId() {
        return useNewId;
    }

    public String getPixelDataType() {
        return pixelDataType;
    }

    public String getTiling() {
        return tiling;
    }

    private final String pixelDataType;
    private final String tiling;
    private final String GMLCoverage;
    private final URL CoverageURL;
    private final Boolean useNewId;
}
