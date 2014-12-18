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

import java.math.BigInteger;

/**
 * Representation of a rasdaman layer collection that acts as a data storage identifier for the style.
 * The rasdaman object can be obtained by querying using the collection and the oid provided.
 * Furthermore each rasdaman layer can also be a scaled representation of a wms layer to enable performance
 * gains through the use of pyramids of scale levels.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "rasdaman_layer")
public class RasdamanLayer implements IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param collectionName the collection name in rasdaman that corresponds to this layer
     * @param oid            the oid of the rasdaman object corresponding to this layer
     * @param scaleFactor    the scale factor assigned to this layer
     * @param xOrder         the order of the x axis in the rasdaman collection
     * @param yOrder         the order of the y axis in the rasdaman collection
     * @param width          the width of the layer in pixels
     * @param height         the height of the layer in pixels
     * @param layer          the layer to which this rasdaman layer was assigned
     */
    public RasdamanLayer(String collectionName, BigInteger oid, double scaleFactor, int xOrder, int yOrder, int width, int height, Layer layer) {
        this.collectionName = collectionName;
        this.oid = oid;
        this.scaleFactor = scaleFactor;
        this.xOrder = xOrder;
        this.yOrder = yOrder;
        this.width = width;
        this.height = height;
        this.layer = layer;
    }

    protected RasdamanLayer() {
    }

    /**
     * Returns the collection name for this layer
     *
     * @return the collection name in rasdaman
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Returns the oid of the rasdaman object
     *
     * @return the oid of the rasdaman object
     */
    public BigInteger getOid() {
        return oid;
    }

    /**
     * Returns the scale factor for this layer
     *
     * @return the scale factor
     */
    public double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Returns the x order for this layer
     *
     * @return the order of the x axis
     */
    public int getXOrder() {
        return xOrder;
    }

    /**
     * Returns the y order for this layer
     *
     * @return the order of the y axis
     */
    public int getYOrder() {
        return yOrder;
    }

    /**
     * Returns the width of the layer in pixels
     *
     * @return the width of the layer in pixels
     */
    public long getWidth() {
        return width;
    }

    /**
     * Returns the height of the layer in pixels
     *
     * @return the height of the layer in pixels
     */
    public long getHeight() {
        return height;
    }

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String collectionName;

    @DatabaseField(canBeNull = false, columnName = SCALE_FACTOR_COLUMN_NAME)
    private double scaleFactor;

    @DatabaseField(canBeNull = false)
    private BigInteger oid;


    @DatabaseField(canBeNull = false)
    private int xOrder;

    @DatabaseField(canBeNull = false)
    private int yOrder;

    @DatabaseField(canBeNull = false)
    private long width;

    @DatabaseField(canBeNull = false)
    private long height;

    @DatabaseField(foreign = true, canBeNull = false)
    private Layer layer;

    private final static String SCALE_FACTOR_COLUMN_NAME = "scaleFactor";


}
