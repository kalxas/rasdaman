/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2012 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package secore.req;

/**
 * Value in a request parameter, e.g. in
 * 
 * http://opengis.net/def/crs-compound?1=http://opengis.net/def/crs/EPSG/0/4326&
 *                                     2=http://opengis.net/def/crs/EPSG/0/4440
 * 
 * parameter values are http://opengis.net/def/crs/EPSG/0/4326 and
 * http://opengis.net/def/crs/EPSG/0/4440
 *
 * @author Dimitar Misev
 */
public interface ParamValue {
  
}
