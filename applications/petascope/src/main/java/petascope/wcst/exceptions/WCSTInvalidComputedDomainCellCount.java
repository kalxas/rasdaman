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
package petascope.wcst.exceptions;

import petascope.exceptions.WCSTException;
import petascope.exceptions.ExceptionCode;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class WCSTInvalidComputedDomainCellCount extends WCSTException {
    public WCSTInvalidComputedDomainCellCount(String inputDomain, String targetDomain) {
        super(ExceptionCode.InvalidExtent, EXCEPTION_TEXT.replace("$inputDomain", inputDomain).replace("$targetDomain", targetDomain));
    }

    private static final String EXCEPTION_TEXT = "The grid domain of the update input coverage ($inputDomain) is incompatible with the computed " +
            "grid domain in the target coverage ($targetDomain)." +
            "\n\n" +
            "HINT: If using wcst_import, please try again using \"subset_correction\": true (or false with imported coverage with \"subset_correction\": true) in the ingredient file.";
}
