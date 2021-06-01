/*
  *  This file is part of rasdaman community.
  * 
  *  Rasdaman community is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  Rasdaman community is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *  See the GNU  General Public License for more details.
  * 
  *  You should have received a copy of the GNU  General Public License
  *  along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.wms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 *
 * Class representation of a WMS 1.3 layer. NOTE: A layer in WMS is the same
 * model as coverage metadata in WCS. It could have multiple dimensions but the
 * result is always 2D by using the overlay to combine all the slices to 1
 * result.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = Layer.TABLE_NAME)
public class Layer implements Serializable {

    public static final String TABLE_PREFIX = "wms13_";
    public static final String TABLE_NAME = TABLE_PREFIX + "_layer";
    public static final String COLUMN_ID = TABLE_NAME + "_id";
    
    // For Hibernate queries HQL, Criteria
    public static final String LAYER_CLASS_NAME = "Layer";
    public static final String LAYER_NAME_PROPERTY = "name";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    public Layer() {

    }

    // NOTE: each layer can have a parent layer and a parent layer can have multiple child layers (not used now).
    @Column(name = "parent_id")
    private int parent_id;

    // Table 7 — Inheritance of Layer properties
    // One, mandatory
    @Column(name = "name")
    private String name;

    // One, mandatory
    @Column(name = "title")
    private String title;

    // Zero or One, mandatory
    @Column(name = "layer_abstract")
    @Lob
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String layerAbstract;

    // One, but its properties are optional values (use default)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = LayerAttribute.COLUMN_ID)
    private LayerAttribute layerAttribute;

    // Zero or One, optional
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    private List<String> keywordList = new ArrayList<>();

    // Zero Or Many, mandatory
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = Layer.COLUMN_ID)
    @OrderColumn
    private List<Style> styles = new ArrayList<>();

    // One or Many, mandatory
    // List of supported CRS for the layers (e.g :<CRS>EPSG:4326</CRS> <CRS>CRS:84</CRS>)
    // so it will have 2 bounding boxes for each CRS
    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    private List<String> crss = new ArrayList<>();

    // One, mandatory
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = EXGeographicBoundingBox.COLUMN_ID)
    private EXGeographicBoundingBox exGeographicBoundingBox;

    // One or Many, mandatory
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = Layer.COLUMN_ID)
    @OrderColumn
    private List<BoundingBox> boundingBoxes = new ArrayList<>();

    // Zero Or Many, mandatory
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = Layer.COLUMN_ID)
    @OrderColumn
    private List<Dimension> dimensions = new ArrayList<>();

    // Zero Or One, optional
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = Attribution.COLUMN_ID)
    private Attribution attribution;

    // Zero Or Many, optional
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = Layer.COLUMN_ID)
    @OrderColumn
    private List<AuthorityURL> authorityURLs = new ArrayList<>();

    // Zero Or Many, optional
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = Layer.COLUMN_ID)
    @OrderColumn
    private List<Identifier> identifiers = new ArrayList<>();

    // Zero Or Many, optional
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = Layer.COLUMN_ID)
    @OrderColumn
    private List<MetadataURL> metadataURLs = new ArrayList<>();

    // Zero Or One, optional
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = DataURL.COLUMN_ID)
    private DataURL dataURL;

    // Constructor with mandatory parameters for WMS layer
    public Layer(String name, String title, String layerAbstract, LayerAttribute layerAttribute,
                 List<Style> styles, List<String> crss, 
                 EXGeographicBoundingBox exGeographicBoundingBox, List<BoundingBox> boundingBoxes, List<Dimension> dimensions) {
        this.name = name;
        this.title = title;
        this.layerAbstract = layerAbstract;
        this.layerAttribute = layerAttribute;
        this.styles = styles;
        this.crss = crss;
        this.exGeographicBoundingBox = exGeographicBoundingBox;
        this.boundingBoxes = boundingBoxes;
        this.dimensions = dimensions;
    }

    public int getParent_id() {
        return parent_id;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLayerAbstract() {
        return layerAbstract;
    }

    public void setLayerAbstract(String layerAbstract) {
        this.layerAbstract = layerAbstract;
    }

    public LayerAttribute getLayerAttribute() {
        return layerAttribute;
    }

    public void setLayerAttribute(LayerAttribute layerAttribute) {
        this.layerAttribute = layerAttribute;
    }

    public List<String> getKeywordList() {
        return keywordList;
    }

    public void setKeywordList(List<String> keywordList) {
        this.keywordList = keywordList;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }

    public List<String> getCrss() {
        return crss;
    }

    public void setCrss(List<String> crss) {
        this.crss = crss;
    }

    public EXGeographicBoundingBox getExGeographicBoundingBox() {
        return exGeographicBoundingBox;
    }

    public void setExGeographicBoundingBox(EXGeographicBoundingBox exGeographicBoundingBox) {
        this.exGeographicBoundingBox = exGeographicBoundingBox;
    }

    public List<BoundingBox> getBoundingBoxes() {
        return boundingBoxes;
    }

    public void setBoundingBoxes(List<BoundingBox> boundingBoxes) {
        this.boundingBoxes = boundingBoxes;
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    public Attribution getAttribution() {
        return attribution;
    }

    public void setAttribution(Attribution attribution) {
        this.attribution = attribution;
    }

    public List<AuthorityURL> getAuthorityURLs() {
        return authorityURLs;
    }

    public void setAuthorityURLs(List<AuthorityURL> authorityURLs) {
        this.authorityURLs = authorityURLs;
    }

    public List<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(List<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public List<MetadataURL> getMetadataURLs() {
        return metadataURLs;
    }

    public void setMetadataURLs(List<MetadataURL> metadataURLs) {
        this.metadataURLs = metadataURLs;
    }

    public DataURL getDataURL() {
        return dataURL;
    }

    public void setDataURL(DataURL dataURL) {
        this.dataURL = dataURL;
    }

    // Ultility methods
    /**
     * Check if style already existed in list styles of a layer
     *
     * @param styleName
     * @return
     */
    public Style getStyle(String styleName) {
        for (Style style : this.styles) {
            if (style.getName().equals(styleName)) {
                return style;
            }
        }

        return null;
    }
}
