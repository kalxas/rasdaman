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

package petascope.wms2.metadata;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.service.exception.error.WMSInvalidBbox;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Representation of a bounding box in WMS 1.3. According to the standard the definition is:
 * <p/>
 * WMS service metadata shall declare one or more bounding boxes (as defined in 6.7.4) for each Layer.
 * A Bounding Box metadata element may either be stated explicitly or may be inherited from a parent Layer. In XML,
 * the <BoundingBox> metadata element includes the following attributes:
 * ⎯ CRS indicates the Layer CRS that applies to this bounding box.
 * ⎯ minx, miny, maxx, maxy indicate the limits of the bounding box using the axis units and order of the specified CRS.
 * <p/>
 * A Layer may have multiple BoundingBox elements, but each one shall state a different CRS. A Layer inherits any
 * BoundingBox values defined by its parents. A BoundingBox inherited from the parent Layer for a particular CRS is
 * replaced by any declaration for the same CRS in the child Layer. A BoundingBox in the child for a new CRS not
 * already declared by the parent is added to the list of bounding boxes for the child Layer. A single Layer element
 * shall not contain more than one BoundingBox for the same CRS.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "bounding_box")
public class BoundingBox implements ISerializableMetadataObject, IPersistentMetadataObject {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, foreignAutoCreate = true, canBeNull = false)
    private Crs crs;
    @DatabaseField(canBeNull = false)
    private double minx;
    @DatabaseField(canBeNull = false)
    private double miny;
    @DatabaseField(canBeNull = false)
    private double maxx;
    @DatabaseField(canBeNull = false)
    private double maxy;
    @Nullable
    @DatabaseField(foreign = true, canBeNull = false)
    private Layer layer;

    /**
     * Constructor for the class
     *
     * @param crs  the crs of the class
     * @param minx the min on the x axis
     * @param miny the min on the y axis
     * @param maxx the max on the x axis
     * @param maxy the max on the y axis
     */
    public BoundingBox(@NotNull Crs crs, double minx, double miny, double maxx, double maxy, @Nullable Layer layer) throws WMSInvalidBbox {
        if (minx > maxx || miny > maxy) {
            throw new WMSInvalidBbox(MessageFormat.format("{0}, {1}, {2}, {3}", minx, miny, maxx, maxy));
        }
        this.crs = crs;
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
        this.layer = layer;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected BoundingBox() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(PATH_TO_TEMPLATES + "BoundingBox.tpl.xml");
    }

    /**
     * Returns for each variable allowed in the template a corresponding values. The variable name should include the $ prefix
     *
     * @return a map of form (variableKey -> variableValue)
     */
    @NotNull
    @Override
    public Map<String, String> getTemplateVariables() {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("crs", crs.getCrsName());
        ret.put("minx", String.valueOf(minx));
        ret.put("miny", String.valueOf(miny));
        ret.put("maxx", String.valueOf(maxx));
        ret.put("maxy", String.valueOf(maxy));
        return ret;
    }

    /**
     * Returns true if a given bounding box fits into this bounding box
     *
     * @param bbox the given bounding box
     * @return true if it fits, false otherwise
     */
    public boolean canContain(BoundingBox bbox) {
        boolean validXAxis = minx <= bbox.getMinx() && bbox.getMaxx() <= maxx;
        boolean validYAxis = miny <= bbox.getMiny() && bbox.getMaxy() <= maxy;
        return validXAxis && validYAxis;
    }

    /**
     * Always returns true and modifies the bbox in order to fit in the existing one.
     * This function was created to accommodate clients like OpenLayers (the majority of WMS clients) which ignore the standard and
     * request bboxes larger than the layer bbox. Once these clients start respecting the standard we can
     * switch back to the canContain standard abiding method.
     *
     * @param bbox the given bounding box
     * @return true
     */
    public boolean forceCanContain(BoundingBox bbox) {
        if (minx > bbox.getMinx()) {
            bbox.setMinx(minx);
        }
        if (miny > bbox.getMiny()) {
            bbox.setMiny(miny);
        }
        if (maxx < bbox.getMaxx()) {
            bbox.setMaxx(maxx);
        }
        if (maxy < bbox.getMaxy()) {
            bbox.setMaxy(maxy);
        }
        return true;
    }

    /**
     * Returns the min value on x axis
     *
     * @return the min value on x axis
     */

    public double getMinx() {
        return minx;
    }

    /**
     * Returns the min value on y axis
     *
     * @return the min value on y axis
     */
    public double getMiny() {
        return miny;
    }

    /**
     * Returns the max value on x axis
     *
     * @return the max value on x axis
     */
    public double getMaxx() {
        return maxx;
    }

    /**
     * Returns the max value on y axis
     *
     * @return the max value on y axis
     */
    public double getMaxy() {
        return maxy;
    }

    /**
     * Returns the crs of the bounding box
     *
     * @return the crs of the bounding box
     */
    @NotNull
    public Crs getCrs() {
        return crs;
    }

    /**
     * Setter for the minx property
     *
     * @param minx the minimum on this axis of the bbox
     */
    public void setMinx(double minx) {
        this.minx = minx;
    }

    /**
     * Setter for the miny property
     *
     * @param miny the minimum on this axis of the bbox
     */
    public void setMiny(double miny) {
        this.miny = miny;
    }

    /**
     * Setter for the maxx property
     *
     * @param maxx the minimum on this axis of the bbox
     */
    public void setMaxx(double maxx) {
        this.maxx = maxx;
    }

    /**
     * Setter for the maxy property
     *
     * @param maxy the minimum on this axis of the bbox
     */
    public void setMaxy(double maxy) {
        this.maxy = maxy;
    }

    @Override
    public String toString() {
        return "[" + "" + minx + "," + miny + "," + maxx + "," + maxy + "," + crs + ']';
    }
}
