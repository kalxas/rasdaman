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
package petascope.wcst.helpers.decodeparameters;

import org.rasdaman.domain.cis.Coverage;
import org.springframework.stereotype.Service;
import petascope.util.IOUtil;

/**
 * Factory that decides which range parameters convertor to be used.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
@Service
public class RangeParametersConvertorFactory {

    public RangeParametersConvertor getConvertor(String mimeType, String rangeParameters, Coverage coverage) {
        if (mimeType != null && mimeType.toLowerCase().contains(IOUtil.GRIB_MIMETYPE)) {
            return new GribMessageConvertor(rangeParameters, coverage);
        } else {
            return new GeneralMessageConvertor(rangeParameters);
        }
    }
}
