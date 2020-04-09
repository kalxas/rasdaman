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
import java.lang.reflect.Field;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.EntityNaming;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitBasicColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitCollectionTableNameSource;
import org.hibernate.boot.model.naming.ImplicitEntityNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl;
import org.hibernate.cfg.Ejb3Column;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import static org.rasdaman.CustomPhysicalNamingStrategyImpl.toSnakeCase;

/**
 * Class to override the Hibernate 5 naming strategy, all tables, columns without annotated with @Table, @Column should be lower case with underscore between words.
 * see example: https://stackoverflow.com/a/36400014/2028440
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class CustomImplicitNamingStrategyImpl extends ImplicitNamingStrategyComponentPathImpl implements Serializable {

    public static final long serialVersionUID = 1L;
    public static final CustomImplicitNamingStrategyImpl INSTANCE = new CustomImplicitNamingStrategyImpl();
    
    // NOTE: Used in case of creating EntityManagerFactory manually (e.g: migration application) as the configuration 
    // spring.jpa.hibernate.naming.implicit-strategy=org.rasdaman.CustomImplicitNamingStrategyImpl in properties file has no usage.
    public static final String HIBERNATE_IMPLICIT_NAMING_STRATEGY_KEY = "hibernate.implicit_naming_strategy";
    public static final String HIBERNATE_IMPLICIT_NAMING_STRATEGY_VALUE = "org.rasdaman.CustomImplicitNamingStrategyImpl";            

    @Override
    protected String transformEntityName(EntityNaming entityNaming) {
        // prefer the JPA entity name, if specified...
        if (!StringUtils.isEmpty(entityNaming.getJpaEntityName())) {
            return entityNaming.getJpaEntityName();
        } else {
            // otherwise, use the Hibernate entity name
            return entityNaming.getEntityName();
        }
    }

    @Override

    public Identifier determinePrimaryTableName(ImplicitEntityNameSource source) {
        String name = transformEntityName(source.getEntityNaming());
        Identifier identifier = toIdentifier(name.toLowerCase(), source.getBuildingContext());

        return identifier;
    }

    @Override
    public Identifier determineCollectionTableName(ImplicitCollectionTableNameSource source) {
        String owningEntity = transformEntityName(source.getOwningEntityNaming());
        String name = transformAttributePath(source.getOwningAttributePath());
        String entityName;
        if (!StringUtils.isEmpty(owningEntity)) {
            entityName = owningEntity + "_" + name;
        } else {
            entityName = name;
        }
        return toIdentifier(entityName.toLowerCase(), source.getBuildingContext());
    }

    @Override
    public Identifier determineJoinColumnName(ImplicitJoinColumnNameSource source) {
        String name = source.getReferencedColumnName().toString();
        Identifier identifier = toIdentifier(name, source.getBuildingContext());

        return identifier;
    }

    @Override
    // NOTE: this one makes the implicit columns from table created by @ElementCollection to lower case
    public Identifier determineBasicColumnName(ImplicitBasicColumnNameSource source) {
        try {
            Field ejb3ColumnField = source.getClass().getDeclaredField("this$0");
            ejb3ColumnField.setAccessible(true);
            Ejb3Column ejb3Column = (Ejb3Column) ejb3ColumnField.get(source); // explicit naming oder implicit
            String tableName = ejb3Column.getPropertyHolder().getTable().getName();
            final Identifier basicColumnName = super.determineBasicColumnName(source);
            String columnName = tableName + "_" + basicColumnName.toString();
            columnName = toSnakeCase(columnName);

            return Identifier.toIdentifier(columnName);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Identifier toIdentifier(Identifier name, JdbcEnvironment context) {
        return new Identifier(name.getText().toUpperCase(), name.isQuoted());
    }
}
