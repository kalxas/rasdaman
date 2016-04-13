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


package petascope.util.ras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import rasj.RasGMArray;

/**
 * Simple implementation which stores the mdds/scalars from the result as lists.
 *
 * @author Dimitar Misev
 */
public class RasQueryResult extends AbstractRasQueryResult {

    private final List<String> scalars;
    private final List<byte[]> mdds;

    public RasQueryResult(Object result) {
        super(result);
        scalars = new ArrayList<String>();
        mdds = new ArrayList<byte[]>();
        inspect();
    }

    public void mdd(RasGMArray res) {
        mdds.add(res.getArray());
    }

    public void scalar(Object res) {
        scalars.add(res.toString());
    }

    public List<byte[]> getMdds() {
        return mdds;
    }

    public List<String> getScalars() {
        return scalars;
    }

    @Override
    public String toString() {

        String out = "";
        StringBuilder sb = new StringBuilder(out);

        if (!getMdds().isEmpty()) {
            for (byte[] mdd : getMdds()) {
                sb.append(new String(mdd)); // don't use mdd.toString()
            }
        } else if (!getScalars().isEmpty()) {
            for (String scalar : getScalars()) {
                sb.append(scalar);
            }
        }
        return sb.toString();
    }
}
