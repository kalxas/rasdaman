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

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.util.ConfigManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representation of a WMS 1.3 layer. According to the standard the definition is:
 * <p/>
 * A <Layer> may have zero or more of the following XML attributes: queryable, cascaded, opaque, noSubsets,
 * fixedWidth, fixedHeight. All of these attributes are optional and default to 0. Each of these attributes can be
 * inherited or replaced by subsidiary layers. The meaning of each attribute is summarized in Table 6 and detailed in
 * the following subclauses.
 * <p/>
 * = Queryable layers =
 * The Boolean attribute queryable indicates whether the server supports the GetFeatureInfo operation on that
 * Layer. A server may support GetFeatureInfo on some of its layers, but need not support it on all layers. A server
 * shall issue a service exception (code="LayerNotQueryable") if GetFeatureInfo is requested on a Layer that is not
 * queryable.
 * <p/>
 * = Cascaded layers =
 * A Layer is said to have been “cascaded” if it was obtained from an originating server and then included in the
 * service metadata of a different server. The second server may simply offer an additional access point for the
 * Layer, or may add value by offering additional output formats or reprojection to other coordinate reference
 * systems.
 * If a WMS cascades the content of another WMS, then it shall increment by 1 the value of the cascaded attribute
 * for the affected layers. If that attribute is missing from the originating server’s service metadata, then the
 * Cascading WMS shall insert the attribute and set it to 1.
 * <p/>
 * = Opaque vs. transparent layers =
 * If the optional Boolean attribute opaque is absent or false, then maps made from that Layer will generally have
 * significant no-data areas that a client may display as transparent. Vector features such as points and lines are
 * considered not to be opaque in this context (even though at some scales and symbol sizes a collection of features
 * might fill the map area). A true value for opaque indicates that the Layer represents an area-filling coverage. For
 * example, a map that represents topography and bathymetry as regions of differing colours will have no
 * transparent areas. The opaque declaration should be taken as a hint to the client to place such a Layer at the
 * bottom of a stack of maps.
 * This attribute describes only the Layer’s data content, not the picture format of the map response. Whether or not
 * a Layer is listed as opaque, a server shall still comply with 7.3.3.9 regarding the GetMap TRANSPARENT
 * parameter: that is, the server shall send an image with a transparent background if and only if the client requests
 * TRANSPARENT=TRUE and a picture FORMAT that supports transparency
 * <p/>
 * = Subsettable and resizable layers =
 * The Layer metadata may also include three optional attributes that indicate a map server that is less functional
 * than a normal WMS, because it is not able to extract a subset of a larger dataset or because it only serves maps
 * of a fixed size and cannot resize them. For example, a WMS that houses a collection of digitized images of
 * historical maps, or pre-computed browser images of satellite data, may not be able to subset or resize those
 * images. However, it can still respond to GetMap requests for complete maps in the original size.
 * Static image collections may not have a well-defined coordinate reference system, in which case the server shall
 * declare CRS=CRS:1 as described in 6.7.2.
 * When set to a true value, noSubsets indicates that the server is not able to make a map of a geographic area
 * other than the layer's bounding box.
 * When present and nonzero, fixedWidth and fixedHeight indicate that the server is not able to produce a map of
 * the layer at a width and height different from the fixed sizes indicated.
 * <p/>
 * = Inheritance of layer properties =
 * Table 7 summarizes how the properties of an enclosing parent <Layer> element are inherited by child <Layer>
 * elements. Properties may be not inherited at all, or inherited as-is, or replaced if the child redefines them, or
 * inherited and added to if the child also defines them
 * In Table 7, the number column states the number of times each element may appear in a Layer, either explicitly
 * or though inheritance. Thus, it is more restrictive than the constraints enforced by the schema in E.1. The
 * meanings of the values in this column are as follows:
 * ⎯ 1: appears exactly once in each Layer.
 * ⎯ 0/1: appears either once or not at all.
 * ⎯ 0+: appears zero or more times.
 * ⎯ 1+: appears one or more times.
 * The Inheritance column indicates whether or how the element is inherited by child Layers. The meanings of the
 * values in this column are as follows:
 * ⎯ no: Not inherited. If the element is defined to be mandatory by this International Standard, then each Layer
 * element shall include that element.
 * ⎯ replace: Value can be inherited from parent and omitted by child, but if specified by child then the parent
 * value is ignored
 * ⎯ add: Values can be inherited from parent and omitted by child. “Add” is only relevant for elements that may
 * appear more than one time. Child inherits any value(s) supplied by parent and adds any value(s) of its own to
 * the list. Any duplicated definition by the child is ignorable.
 * <p/>
 * = Format specifiers =
 * Format specifiers appear in several places in service metadata: as valid output formats for an operation, as
 * supported Exception formats, and as the format of content at external URLs. Output formats are discussed in 6.6.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
@DatabaseTable(tableName = IPersistentMetadataObject.TABLE_PREFIX + "layer")
public class Layer implements ISerializableMetadataObject, IPersistentMetadataObject {

    /**
     * Constructor for the class
     *
     * @param queryable     0 or 1 if the layer is queryable or not
     * @param cascaded      0 or 1 if the layer contains other layers or not
     * @param opaque        0 or 1 if the layer is opaque or transparent
     * @param noSubsets     0 if the layer is subsettable 1 otherwise
     * @param fixedWidth    0 if width is *not* fixed, 1 otherwise
     * @param fixedHeight   0 if height is *not* fixed, 1 otherwise
     * @param title         the title of the layer
     * @param name          the name of the layer
     * @param layerAbstract the abstract of the layer
     * @param exBoundingBox the geographic bounding box in degrees for search engine indexes
     * @param parentLayer   any parent layers this layer has
     */
    public Layer(int queryable, int cascaded, int opaque, int noSubsets, int fixedWidth, int fixedHeight,
                 @NotNull String title, @Nullable String name, @NotNull String layerAbstract,
                 @NotNull EXGeographicBoundingBox exBoundingBox, @Nullable Layer parentLayer) {
        this.queryable = queryable;
        this.cascaded = cascaded;
        this.opaque = opaque;
        this.noSubsets = noSubsets;
        this.fixedWidth = fixedWidth;
        this.fixedHeight = fixedHeight;
        this.name = name;
        this.title = title;        
        this.layerAbstract = layerAbstract;
        this.exBoundingBox = exBoundingBox;
        this.parentLayer = parentLayer;
    }

    /**
     * Empty constructor to be used by the persistence provider
     */
    protected Layer() {
    }

    /**
     * Returns the path to the template corresponding to this metadata object
     *
     * @return the path to the template
     */
    @NotNull
    @Override
    public InputStream getStreamToTemplate() {
        return this.getClass().getResourceAsStream(ConfigManager.PATH_TO_TEMPLATES + "Layer.tpl.xml");
    }

    /**
     * Returns for each variable allowed in the template a corresponding values. The variable name should NOT include the $ prefix
     *
     * @return a map of form (variableKey -> variableValue)
     */
    @NotNull
    @Override
    public Map<String, String> getTemplateVariables() throws IOException {
        MetadataObjectXMLSerializer serializer = new MetadataObjectXMLSerializer();
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("queryable", String.valueOf(queryable));
        ret.put("cascaded", String.valueOf(cascaded));
        ret.put("opaque", String.valueOf(opaque));
        ret.put("noSubsets", String.valueOf(noSubsets));
        ret.put("fixedWidth", String.valueOf(fixedWidth));
        ret.put("fixedHeight", String.valueOf(fixedHeight));
        ret.put("name", name);
        ret.put("title", title);        
        ret.put("abstract", layerAbstract);
        ret.put("crs", serializer.serializeCollection(getCrses()));
        ret.put("EX_GeographicBoundingBox", serializer.serialize(exBoundingBox));
        ret.put("BoundingBoxes", serializer.serializeCollection(boundingBoxes));
        ret.put("Styles", serializer.serializeCollection(styles));
        ret.put("Layers", serializer.serializeCollection(childLayers));
        return ret;
    }

    /**
     * Returns 1 if queryable, 0 if not
     *
     * @return the queryable property
     */
    public int getQueryable() {
        return queryable;
    }

    /**
     * Returns 1 if cascaded, 0 if not
     *
     * @return the cascaded property
     */
    public int getCascaded() {
        return cascaded;
    }

    /**
     * Returns 1 if opaque, 0 if not
     *
     * @return the opaque property
     */
    public int getOpaque() {
        return opaque;
    }

    /**
     * Returns 1 if layer has no subsets, 0 if not
     *
     * @return the layer has property
     */
    public int getNoSubsets() {
        return noSubsets;
    }

    /**
     * Returns 1 if layer has fixed width, 0 if not
     *
     * @return the layer has fixed width property
     */
    public int getFixedWidth() {
        return fixedWidth;
    }

    /**
     * Returns 1 if layer has fixed height, 0 if not
     *
     * @return the layer has fixed height property
     */
    public int getFixedHeight() {
        return fixedHeight;
    }

    /**
     * Returns the title of the layer
     *
     * @return the title of the layer
     */
    @NotNull
    public String getTitle() {
        return title;
    }

    /**
     * Returns the abstract of the layer
     *
     * @return the abstract of layer
     */
    @NotNull
    public String getLayerAbstract() {
        return layerAbstract;
    }

    /**
     * Returns the crses of the layer
     *
     * @return the crses of the layer
     */
    @NotNull
    public List<Crs> getCrses() {
        List<Crs> crses = new ArrayList<Crs>();
        Iterable<BoundingBox> bboxes = getBoundingBoxes();
        if (bboxes != null) {
            for (BoundingBox bbox : bboxes) {
                crses.add(bbox.getCrs());
            }
        }
        return crses;
    }

    /**
     * Returns the geographic bounding box of the layer
     *
     * @return the geographic bounding box of the layer
     */
    @NotNull
    public EXGeographicBoundingBox getExBoundingBox() {
        return exBoundingBox;
    }

    /**
     * Returns the bounding boxes of the layer
     *
     * @return the bounding boxes of the layer
     */
    @Nullable
    public ForeignCollection<BoundingBox> getBoundingBoxes() {
        return boundingBoxes;
    }

    /**
     * Returns the styles of the layer
     *
     * @return the styles of the layer
     */
    @Nullable
    public ForeignCollection<Style> getStyles() {
        return styles;
    }

    /**
     * Returns the child layers of this layer
     *
     * @return the child layers of the layer
     */
    @Nullable
    public ForeignCollection<Layer> getChildLayers() {
        return childLayers;
    }

    /**
     * Returns the extra dimensions of the layer
     *
     * @return the extra dimensions of the layer
     */
    @Nullable
    public ForeignCollection<Dimension> getDimensions() {
        return dimensions;
    }

    /**
     * Returns the parent layer of the layer
     *
     * @return the parent layer of the layer
     */
    @Nullable
    public Layer getParentLayer() {
        return parentLayer;
    }

    /**
     * Returns the rasdaman layer  of this layer
     *
     * @return the rasdaman layer
     */
    @NotNull
    public ForeignCollection<RasdamanLayer> getRasdamanLayers() {
        return rasdamanLayers;
    }

    /**
     * Returns the name of the layer
     * @return the name of the layer
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns the id of the layer
     *
     * @return the id of the layer
     */
    public int getId() {
        return id;
    }

    /**
     * 0, false: layer is not queryable.
     * 1, true: layer is queryable.
     */
    @DatabaseField(canBeNull = false)
    private int queryable;

    /**
     * 0: layer has not been retransmitted by a Cascading Map Server.
     * n: layer has been retransmitted n times.
     */
    @DatabaseField(canBeNull = false)
    private int cascaded;

    /**
     * 0, false: map data represents vector features that probably do not completely fill space.
     * 1, true: map data are mostly or completely opaque.
     */
    @DatabaseField(canBeNull = false)
    private int opaque;

    /**
     * 0, false: WMS can map a subset of the full bounding box.
     * 1, true: WMS can only map the entire bounding box.
     */
    @DatabaseField(canBeNull = false)
    private int noSubsets;

    /**
     * 0: WMS can produce map of arbitrary width.
     * nonzero: value is fixed map width that cannot be changed by the WMS.
     */
    @DatabaseField(canBeNull = false)
    private int fixedWidth;

    /**
     * 0: WMS can produce map of arbitrary height.
     * nonzero: value is fixed map height that cannot be changed by the WMS.
     */
    @DatabaseField(canBeNull = false)
    private int fixedHeight;

    @DatabaseField(generatedId = true)
    private int id;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String title;

    @Nullable
    @DatabaseField(columnName = NAME_COLUMN_NAME)
    private String name;

    @NotNull
    @DatabaseField(canBeNull = false)
    private String layerAbstract;

    @NotNull
    @DatabaseField(foreign = true, canBeNull = false, foreignAutoCreate = true, foreignAutoRefresh = true)
    private EXGeographicBoundingBox exBoundingBox;

    @Nullable
    @ForeignCollectionField(eager = false)
    private ForeignCollection<BoundingBox> boundingBoxes;

    @Nullable
    @ForeignCollectionField(eager = false)
    private ForeignCollection<Style> styles;

    @Nullable
    @ForeignCollectionField(eager = false)
    private ForeignCollection<Layer> childLayers;

    @Nullable
    @ForeignCollectionField(eager = false)
    private ForeignCollection<Dimension> dimensions;

    @Nullable
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Layer parentLayer;

    @Nullable
    @ForeignCollectionField(eager = false)
    private ForeignCollection<RasdamanLayer> rasdamanLayers;

    public final static String NAME_COLUMN_NAME = "name";
}
