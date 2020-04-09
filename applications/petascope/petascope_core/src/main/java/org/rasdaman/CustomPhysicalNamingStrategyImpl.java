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
  *  Copyright 2003 - 2020 Peter Baumann / rasdaman GmbH.
  *
  *  For more information please see <http://www.rasdaman.org>
  *  or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package org.rasdaman;

import java.io.Serializable;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Class to override the Hibernate 5 naming strategy, all tables, columns
 * explicit annotated with @Table, @Column should be lower case with underscore
 * between words.
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class CustomPhysicalNamingStrategyImpl extends PhysicalNamingStrategyStandardImpl implements Serializable {

    public static final long serialVersionUID = 1L;
    public static final CustomPhysicalNamingStrategyImpl INSTANCE = new CustomPhysicalNamingStrategyImpl();

    // NOTE: Used in case of creating EntityManagerFactory manually (e.g: migration application) as the configuration
    // spring.jpa.hibernate.naming.physical-strategy=org.rasdaman.CustomPhysicalNamingStrategyImpl in properties file has no usage.
    public static final String HIBERNATE_PHYSICAL_NAMING_STRATEGY_KEY = "hibernate.physical_naming_strategy";
    public static final String HIBERNATE_PHYSICAL_NAMING_STRATEGY_VALUE = "org.rasdaman.CustomPhysicalNamingStrategyImpl";

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return new Identifier(toSnakeCase(name.getText()), name.isQuoted());
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return new Identifier(toSnakeCase(name.getText()), name.isQuoted());
    }

    /**
     * Change column's name from camelCase to snake_case for Hibernate 5.
     * https://en.wikipedia.org/wiki/Snake_case
     *
     * @param name
     * @return
     */
    public static String toSnakeCase(String name) {
        final StringBuilder sb = new StringBuilder(name);
        for (int i = 1; i < sb.length() - 1; i++) {
            if (Character.isLowerCase(sb.charAt(i - 1)) && Character.isUpperCase(sb.charAt(i))
                    && Character.isLowerCase(sb.charAt(i + 1))) {
                sb.insert(i++, '_');
            }
        }
        return sb.toString().toLowerCase();
    }

}
