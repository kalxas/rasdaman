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
package petascope.wcs2.wcst;

import org.junit.Assert;
import org.junit.Test;
import petascope.exceptions.PetascopeException;
import petascope.util.ras.TypeRegistry;

import java.util.ArrayList;
import java.util.List;
import petascope.swe.datamodel.NilValue;

/**
 * Integration test for type registry. Requires rasdaman running at http://localhost:7001
 */
public class WCSTTypeTest {

    @Test
    public void testTypeCreation(){
        try {
            List<String> bandBaseTypes = new ArrayList<String>();
            bandBaseTypes.add("char");
            bandBaseTypes.add("float");

            List<NilValue> nullValues = new ArrayList<NilValue>();
            nullValues.add(new NilValue("5", "Reason: test"));

            TypeRegistry typeRegistry = TypeRegistry.getInstance();
            String typeName = typeRegistry.createNewType(5, bandBaseTypes, nullValues);

            Assert.assertTrue(typeRegistry.getTypeRegistry().keySet().contains(typeName));
        } catch (PetascopeException e) {
            Assert.assertTrue(false);
            e.printStackTrace();
        }
    }
}
