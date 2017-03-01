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
package petascope.exceptions.wcst;

import petascope.exceptions.ExceptionCode;

/**
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class WCSTAxisNumberMismatchException extends WCSTException {
    public WCSTAxisNumberMismatchException(int currentCoverageAxesSize, int inputCoverageAxesSize) {
        super(ExceptionCode.InconsistentChange, EXCEPTION_TEXT.replace("$currentCoverageAxesSize", String.valueOf(currentCoverageAxesSize))
              .replace("$inputCoverageAxesSize", String.valueOf(inputCoverageAxesSize)));
    }

    private final static String EXCEPTION_TEXT = "In case of complete replacement, the number of axes of the target coverage (found $currentCoverageAxesSize) must match the " +
            "number of axes of the input coverage (found $inputCoverageAxesSize). Use a subset parameter if partial replacement is intended.";
}
