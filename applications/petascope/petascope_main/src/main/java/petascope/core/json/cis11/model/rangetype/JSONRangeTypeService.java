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
 * Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope.core.json.cis11.model.rangetype;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import petascope.exceptions.PetascopeException;
import petascope.util.ras.TypeResolverUtil;
import petascope.wcps.metadata.model.RangeField;
import petascope.wcps.metadata.model.WcpsCoverageMetadata;

/**
 * Service class to build RangeType for JSON CIS 1.1.
 *
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
@Service
public class JSONRangeTypeService {
    
    public static final String OPENGIS_DATA_TYPE_PREFIX = "ogcType:";

    /**
     * Build NilValues for Quantity.
     */
    private NilValues buildNilValues(RangeField rangeField) {
        List<NilValue> values = new ArrayList<>();

        for (org.rasdaman.domain.cis.NilValue nilValue : rangeField.getNodata()) {
            NilValue value = new NilValue(nilValue.getReason(), nilValue.getValue());
            values.add(value);
        }

        if (!values.isEmpty()) {
            NilValues nilValues = new NilValues(values);
            return nilValues;
        }
        
        return null;
    }

    /**
     * Build Quantity for Field.
     */
    private Quantity buildQuantity(RangeField rangeField) throws PetascopeException {
        String label = rangeField.getName();
        String description = rangeField.getDescription();
        NilValues nilValues = this.buildNilValues(rangeField);
        String uomCode = rangeField.getUomCode();
        UoM uom = new UoM(uomCode);
        // e.g: unsignedInt
        String rasdamanDataType = rangeField.getDataType();
        String opengisDataType = TypeResolverUtil.RAS_TYPES_TO_OPENGIS_TYPES.get(rasdamanDataType);
        String definition = OPENGIS_DATA_TYPE_PREFIX + opengisDataType;

        Quantity quantity = new Quantity(label, description, definition, nilValues, uom);
        return quantity;
    }

    /**
     * Build list of quantities
     */
    private List<Quantity> buildQuantities(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {
        List<Quantity> quantities = new ArrayList<>();

        List<RangeField> rangeFields = wcpsCoverageMetadata.getRangeFields();

        for (RangeField rangeField : rangeFields) {
            Quantity quantity = this.buildQuantity(rangeField);
            quantities.add(quantity);
        }

        return quantities;
    }

    /**
     * Build RangeType for GMLCore.
     */
    public RangeType buildRangeType(WcpsCoverageMetadata wcpsCoverageMetadata) throws PetascopeException {

        List<Quantity> quantities = this.buildQuantities(wcpsCoverageMetadata);
        RangeType rangeType = new RangeType(quantities);

        return rangeType;
    }
}
