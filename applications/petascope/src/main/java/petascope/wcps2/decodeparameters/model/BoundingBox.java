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
package petascope.wcps2.decodeparameters.model;

/**
 * This class represents a bounding box as understandable by the rasdaman decode function.
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class BoundingBox {

    private double xmin;
    private double ymin;
    private double xmax;
    private double ymax;

    public BoundingBox(double xmin, double ymin, double xmax, double ymax) {
        this.xmin = xmin;
        this.ymin = ymin;
        this.xmax = xmax;
        this.ymax = ymax;
    }

    public BoundingBox() {
    }

    public double getXMin() {
        return xmin;
    }

    public double getYMin() {
        return ymin;
    }

    public double getXMax() {
        return xmax;
    }

    public double getYMax() {
        return ymax;
    }

    public void setXMin(double xmin) {
        this.xmin = xmin;
    }

    public void setYMin(double ymin) {
        this.ymin = ymin;
    }

    public void setXMax(double xmax) {
        this.xmax = xmax;
    }

    public void setYMax(double ymax) {
        this.ymax = ymax;
    }
}
