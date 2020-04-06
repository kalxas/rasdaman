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
 * Copyright 2003 - 2017 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.domain.cis;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

// Example with both allowed values and NILs of a field (band)
//
// <swe:Quantity definition="http://sweet.jpl.nasa.gov/2.0/physRadiation.owl#IonizingRadiation">
//   <swe:label>Radiation Dose</swe:label>
//   <swe:description>Radiation dose measured by Gamma detector</swe:description>
//   <swe:nilValues>
//     <swe:NilValues>
//       <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/BelowDetectionRange">-INF</swe:nilValue>
//       <swe:nilValue reason="http://www.opengis.net/def/nil/OGC/0/AboveDetectionRange">INF</swe:nilValue>
//     </swe:NilValues>
//   </swe:nilValues>
//   <swe:uom code="uR"/>
//   <swe:constraint>
//     <swe:AllowedValues>
//       <swe:interval>-180 0</swe:interval>
//       <swe:interval>1 180</swe:interval>
//     </swe:AllowedValues>
//   </swe:constraint>
// </swe:Quantity>
/**
 *
 * Quantity is used to define a band of coverage (with band_name, description,
 * list of nilValues, uom and list of allowedValues)
 */
@Entity
@Table(name = Quantity.TABLE_NAME)
public class Quantity implements Serializable {

    public static final String TABLE_NAME = "quantity";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = COLUMN_ID)
    private long id;

    @Column(name = "definition")
    @Lob
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String definition;

    @Column(name = "description")
    @Lob
    // NOTE: As this could be long text, so varchar(255) is not enough
    private String description;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = Quantity.COLUMN_ID)
    @OrderColumn
    private List<NilValue> nilValues;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn
    @JoinColumn(name = Quantity.COLUMN_ID)
    private List<AllowedValue> allowedValues;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = Uom.COLUMN_ID)
    private Uom uom;
    
    @Column(name = "data_type")
    // This does not exist in CIS 1.1, a convenient to know the dataType of the band stored in rasdaman
    // e.g: double, char, float,...
    // NOTE: right now, when parsing GML from wcst_import, all data types use "unsigned char"
    // @TODO: need to get the dataTypes of bands when "guessing" collectionType in InsertCoverageHandler
    private String dataType;

    public Quantity() {

    }

    public Quantity(String definition, String description, List<AllowedValue> allowedValues, List<NilValue> nilValues, Uom uom, String dataType) {
        this.definition = definition;
        this.description = description;
        this.allowedValues = allowedValues;
        this.nilValues = nilValues;
        this.uom = uom;
        this.dataType = dataType;
    }
    

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<NilValue> getNilValuesList() {
        return nilValues;
    }

    public void setNilValues(List<NilValue> nilValues) {
        this.nilValues = nilValues;
    }

    public Uom getUom() {
        return uom;
    }

    public void setUom(Uom uom) {
        this.uom = uom;
    }

    public List<AllowedValue> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<AllowedValue> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
