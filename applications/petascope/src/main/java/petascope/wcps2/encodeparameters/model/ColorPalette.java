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
 * Copyright 2003 - 2016 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/
package petascope.wcps2.encodeparameters.model;

import java.util.List;

/**
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class ColorPalette {
    public ColorPalette() {
        
    }
    public ColorPalette(String paletteInterp, List<String> colorInterp, List<List<Integer>> colorTable) {
        this.paletteInterp = paletteInterp;
        this.colorInterp = colorInterp;
        this.colorTable = colorTable;
    }
    public void setPaletteInterp(String paletteInterp) {
        this.paletteInterp = paletteInterp;
    }
    public String getPaletteInterp() {
        return this.paletteInterp;
    }
    
    public void setColorInterp(List<String> colorInterp) {
        this.colorInterp = colorInterp;
    }
    public List<String> getColorInterp() {
        return this.colorInterp;
    }
    
    public void setColorTable(List<List<Integer>> colorTable) {
        this.colorTable = colorTable;
    }
    public List<List<Integer>> getColorTable() {
        return this.colorTable;
    }
    
    private String paletteInterp;
    private List<String> colorInterp;
    private List<List<Integer>> colorTable;
}
