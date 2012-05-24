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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.wcs2.extensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage Scaling Extension (OGC 12-039).
 * 
 * @author <a href="mailto:m.rusu@jacobs-university.de">Mihaela Rusu</a>
 */
public class ScalingExtension implements Extension {
    
    private static final Logger log = LoggerFactory.getLogger(ScalingExtension.class);
    
    @Override
    public String getExtensionIdentifier() {
        return ExtensionsRegistry.SCALING_IDENTIFIER;
    }
   
    
}