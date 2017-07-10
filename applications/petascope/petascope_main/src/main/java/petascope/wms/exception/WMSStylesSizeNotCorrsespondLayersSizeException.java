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
package petascope.wms.exception;

import petascope.exceptions.WMSException;
import org.jetbrains.annotations.NotNull;

/**
 * Exception to be thrown when in GetMap request, the number of layers is not
 * corresponding to number of styles (WMS requires each style for each layer)
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 *
 */
public class WMSStylesSizeNotCorrsespondLayersSizeException extends WMSException {

    public WMSStylesSizeNotCorrsespondLayersSizeException() {
        super("No style parameter requested.");
    }

    /**
     * Constructor for the class
     *
     * @param stylesSize
     * @param layersSize
     */
    public WMSStylesSizeNotCorrsespondLayersSizeException(Integer stylesSize, Integer layersSize) {
        super(ERROR_MESSAGE.replace("$stylesSize", stylesSize.toString()).replace("$layersSize", layersSize.toString()));
    }

    @NotNull
    @Override
    public String getExceptionCode() {
        return EXCEPTION_CODE;
    }

    private final static String EXCEPTION_CODE = "StylesNotCorrespondLayers";
    private final static String ERROR_MESSAGE = "The number of requesting styles: $stylesSize is different from requesting layers: $layersSize.";
}
