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
 * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.gml.cis.model.rangetype;

import java.util.ArrayList;
import java.util.List;
import org.rasdaman.domain.cis.AllowedValue;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Service class to build RangeType for GMLCore.
 * 
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class RangeTypeService {
    
        // ################## Build RangeType
    
    /**
     * Build NilValues for Quantity.
     */
    private NilValues buildNilValues(RangeField rangeField) {
        List<NilValue> values = new ArrayList<>();
        
        for (org.rasdaman.domain.cis.NilValue nilValue : rangeField.getNodata()) {
            NilValue value = new NilValue(nilValue.getReason(), nilValue.getValue());
            values.add(value);
        }
        
        NilValues nilValues = new NilValues(values);       
        return nilValues;
    }
    
    /**
     * Build AllowedValues for Constraint.
     */
    private AllowedValues buildAllowedValues(RangeField rangeField) {
        List<String> intervals = new ArrayList<>();
        
        if (rangeField.getAllowedValues() != null) {
            for (AllowedValue allowedValue : rangeField.getAllowedValues()) {
                String values = allowedValue.getValues();
                intervals.add(values);
            }
        }
        
        AllowedValues allowedValues = new AllowedValues(intervals);
        return allowedValues;
    }
    
    /**
     * Build Constraint for Quantity.
     */
    private Constraint buildConstraint(RangeField rangeField) {
        AllowedValues allowedValues = this.buildAllowedValues(rangeField);
        Constraint constraint = new Constraint(allowedValues);
        
        return constraint;
    }
    
        /**
     * Build Quantity for Field.
     */
    private Quantity buildQuantity(RangeField rangeField) throws PetascopeException {
        String label = rangeField.getName();
        String description = rangeField.getDescription();
        NilValues nilValues = this.buildNilValues(rangeField);
        String uomCode = rangeField.getUomCode();        
        Constraint constraint = this.buildConstraint(rangeField);
        String dataType = rangeField.getDataType();
        
        Quantity quantity = new Quantity();
        quantity.setLabel(label);
        quantity.setDescription(description);
        quantity.setNilValues(nilValues);
        quantity.setUomCode(uomCode);
        quantity.setConstraint(constraint);
        quantity.setDataType(dataType);

        return quantity;
    }
    
    
    /**
     * Build Field for DataRecord.
     */
    private List<Field> buildFields(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        List<Field> fields = new ArrayList<>();
        
        List<RangeField> rangeFields = wcpsCoverageMetadata.getRangeFields();
        
        for (RangeField rangeField : rangeFields) {
            String name = rangeField.getName();
            Quantity quantity = this.buildQuantity(rangeField);
                    
            Field field = new Field(name, quantity);
            fields.add(field);
        }
        
        return fields;
    }
    
    /**
     * Build DataRecord for RangeType. 
     */
    private DataRecord buildDataRecord(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        List<Field> fields = this.buildFields(wcpsCoverageMetadata);        
        DataRecord dataRecord = new DataRecord(fields);
        return dataRecord;
    }
    /**
     * Build RangeType for GMLCore.
     */
    public RangeType buildRangeType(String rangeTypePrefixXML, String rangeTypeElementName,
                                    String rangeTypeNamespaceXML, WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        DataRecord dataRecord = this.buildDataRecord(wcpsCoverageMetadata);
        
        RangeType rangeType = new RangeType(rangeTypePrefixXML, rangeTypeElementName, rangeTypeNamespaceXML, dataRecord);
        return rangeType;
    }
}
