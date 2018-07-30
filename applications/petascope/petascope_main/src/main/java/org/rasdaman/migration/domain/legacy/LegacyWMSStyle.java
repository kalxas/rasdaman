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
  *  Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
  * 
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman.migration.domain.legacy;

/**
 * Legacy WMS 1.3 styles
 *
 * @author <a href="mailto:b.phamhuu@jacobs-university.de">Bang Pham Huu</a>
 */
public class LegacyWMSStyle {
    
    // Primary key
    private String name;
    private String title;
    private String styleAbstract;
    private String rasqlQueryTransformer;

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

    public String getStyleAbstract() {
        return styleAbstract;
    }

    public void setStyleAbstract(String styleAbstract) {
        this.styleAbstract = styleAbstract;
    }

    public String getRasqlQueryTransformer() {
        return rasqlQueryTransformer;
    }

    public void setRasqlQueryTransformer(String rasqlQueryTransformer) {
        this.rasqlQueryTransformer = rasqlQueryTransformer;
    }

}
