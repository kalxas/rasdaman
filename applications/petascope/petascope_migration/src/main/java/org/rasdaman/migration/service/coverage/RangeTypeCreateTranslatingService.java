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
package org.rasdaman.migration.service.coverage;

import java.util.ArrayList;
import java.util.List;
import org.rasdaman.domain.cis.AllowedValue;
import org.rasdaman.domain.cis.DataRecord;
import org.rasdaman.domain.cis.Field;
import org.rasdaman.domain.cis.InterpolationRestriction;
import org.rasdaman.domain.cis.NilValue;
import org.rasdaman.domain.cis.Quantity;
import org.rasdaman.domain.cis.RangeType;
import org.rasdaman.domain.cis.Uom;
import org.rasdaman.migration.domain.legacy.LegacyAbstractSimpleComponent;
import org.rasdaman.migration.domain.legacy.LegacyQuantity;
import org.rasdaman.migration.domain.legacy.LegacyCoverageMetadata;
import org.rasdaman.migration.domain.legacy.LegacyNilValue;
import org.rasdaman.migration.domain.legacy.LegacyRangeElement;
import org.springframework.stereotype.Service;

/**
 * Create a RangeType object from legacy CoverageMetadata object
 *
 @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Service
public class RangeTypeCreateTranslatingService {

    public RangeType create(LegacyCoverageMetadata coverageMetadata) {
        // Old CoverageMetadata does not contains this type of restriction (Null means all interpolation is applicable)
        // empty list means no interpolation is applicable
        InterpolationRestriction interpolationRestriction = null;
        List<Field> fields = this.createFields(coverageMetadata);
        DataRecord dataRecord = new DataRecord(fields);
        RangeType rangeType = new RangeType();
        rangeType.setDataRecord(dataRecord);
        rangeType.setInterpolationRestriction(interpolationRestriction);

        return rangeType;
    }

    /**
     * Create a list of fields (bands, channels) from coverage
     *
     * @param coverageMetadata
     * @return
     */
    private List<Field> createFields(LegacyCoverageMetadata coverageMetadata) {
        List<Field> fields = new ArrayList<>();
        
        int i = 0;
        List<LegacyRangeElement> rangeElements = coverageMetadata.getRangeElements();

        for (LegacyAbstractSimpleComponent sweComponent : coverageMetadata.getSweComponents()) {
            LegacyQuantity legacyQuantity = (LegacyQuantity) sweComponent;
            
            List<NilValue> nilValues = new ArrayList<>();
            for (LegacyNilValue legacyNilValue : legacyQuantity.getNilValues()) {
                NilValue nilValue = new NilValue();
                nilValue.setReason(legacyNilValue.getReason());
                nilValue.setValue(legacyNilValue.getValue());

                nilValues.add(nilValue);
            }

            List<AllowedValue> allowedValues = new ArrayList<>();
            for (String interval : legacyQuantity.getAllowedValues()) {
                AllowedValue allowedValue = new AllowedValue();
                allowedValue.setValues(interval);

                allowedValues.add(allowedValue);
            }

            Uom uom = new Uom(legacyQuantity.getUom());
            // Each field contains 1 Quantity (for 1 band)
            Quantity quantity = new Quantity();
            quantity.setDefinition(legacyQuantity.getDefinition());
            quantity.setDescription(legacyQuantity.getDescription());
            quantity.setUom(uom);
            quantity.setNilValues(nilValues);
            quantity.setAllowedValues(allowedValues);            
            
            // Get the data type of band stored in rasdaman by the band name 
            // e.g: double, boolean, float
            String dataType = rangeElements.get(i).getType();
            quantity.setDataType(dataType);
            String fieldName = rangeElements.get(i).getName();

            Field field = new Field();
            field.setName(fieldName);
            field.setQuantity(quantity);

            fields.add(field);
            
            i++;
        }

        return fields;
    }
}
