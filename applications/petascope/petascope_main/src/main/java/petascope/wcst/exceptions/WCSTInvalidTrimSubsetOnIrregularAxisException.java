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
package petascope.wcst.exceptions;

import petascope.exceptions.WCSTException;
import petascope.exceptions.ExceptionCode;

/**
 * @author <a href="mailto:merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class WCSTInvalidTrimSubsetOnIrregularAxisException extends WCSTException {
    public WCSTInvalidTrimSubsetOnIrregularAxisException(String subset, String axisName) {
        super(ExceptionCode.InvalidSubsetting, EXCEPTION_TEXT.replace("$subset", subset).replace("$axisName", axisName));
    }

    private static final String EXCEPTION_TEXT = "Can not apply update on interval $subset on irregular axis $axisName. Only slice " +
            "updates are currently supported for irregular axes.";
}
