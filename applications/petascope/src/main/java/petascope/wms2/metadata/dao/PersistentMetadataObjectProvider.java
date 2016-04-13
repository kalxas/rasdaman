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

package petascope.wms2.metadata.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.wms2.metadata.*;

import java.sql.SQLException;

/**
 * This class provides access to the persistence objects attributed to each metadata object. This persistence objects
 * are the only connection the metadata objects have with their storage. Each such persistence object can be used
 * to retrieve, insert or update a new metadata object in the storage that was chosen.
 * <p/>
 * Persistence objects can be expensive to create, depending on their storage type so we initialize them only once, when
 * first requested.
 *
 * @author <a href="mailto:dumitru@rasdaman.com">Alex Dumitru</a>
 */
public class PersistentMetadataObjectProvider {


    /**
     * Constructor for the class
     *
     * @param connection the jdbc connection to a known database
     */
    public PersistentMetadataObjectProvider(@NotNull JdbcConnectionSource connection) {
        this.connection = connection;
    }

    /**
     * Returns the attribution persistence object. This object can be used to retrieve / update / create new attribution metadata objects
     *
     * @return the attribution persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<Attribution, Integer> getAttribution() throws SQLException {
        if (attribution == null) {
            //The explicit argument is needed here and in the other methods due to a bug in the java6 compiler.
            //Java7 fixed this, so when we drop support for java6 this should be removed
            attribution = DaoManager.<Dao<Attribution, Integer>, Attribution>createDao(connection, Attribution.class);
        }
        return attribution;
    }


    /**
     * Returns the AuthorityURL persistence object. This object can be used to retrieve / update / create new AuthorityURL metadata objects
     *
     * @return the AuthorityURL persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<AuthorityURL, Integer> getAuthorityURL() throws SQLException {
        if (authorityURL == null) {
            authorityURL = DaoManager.<Dao<AuthorityURL, Integer>, AuthorityURL>createDao(connection, AuthorityURL.class);
        }
        return authorityURL;
    }

    /**
     * Returns the BoundingBox persistence object. This object can be used to retrieve / update / create new BoundingBox metadata objects
     *
     * @return the BoundingBox persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<BoundingBox, Integer> getBoundingBox() throws SQLException {
        if (boundingBox == null) {
            boundingBox = DaoManager.<Dao<BoundingBox, Integer>, BoundingBox>createDao(connection, BoundingBox.class);
        }
        return boundingBox;
    }

    /**
     * Returns the ContactInformation persistence object. This object can be used to retrieve / update / create new ContactInformation metadata objects
     *
     * @return the ContactInformation persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<ContactInformation, Integer> getContactInformation() throws SQLException {
        if (contactInformation == null) {
            contactInformation = DaoManager.<Dao<ContactInformation, Integer>, ContactInformation>createDao(connection, ContactInformation.class);
        }
        return contactInformation;
    }

    /**
     * Returns the Crs persistence object. This object can be used to retrieve / update / create new Crs metadata objects
     *
     * @return the Crs persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<Crs, Integer> getCrs() throws SQLException {
        if (crs == null) {
            crs = DaoManager.<Dao<Crs, Integer>, Crs>createDao(connection, Crs.class);
        }
        return crs;
    }

    /**
     * Returns the DataURL persistence object. This object can be used to retrieve / update / create new DataURL metadata objects
     *
     * @return the DataURL persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<DataURL, Integer> getDataURL() throws SQLException {
        if (dataURL == null) {
            dataURL = DaoManager.<Dao<DataURL, Integer>, DataURL>createDao(connection, DataURL.class);
        }
        return dataURL;
    }

    /**
     * Returns the EX_GeographicBoundingBox persistence object. This object can be used to retrieve / update / create new EX_GeographicBoundingBox metadata objects
     *
     * @return the EX_GeographicBoundingBox persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<EXGeographicBoundingBox, Integer> getExGeographicBoundingBox() throws SQLException {
        if (ex_GeographicBoundingBox == null) {
            ex_GeographicBoundingBox = DaoManager.<Dao<EXGeographicBoundingBox, Integer>, EXGeographicBoundingBox>createDao(connection, EXGeographicBoundingBox.class);
        }
        return ex_GeographicBoundingBox;
    }

    /**
     * Returns the ExceptionFormat persistence object. This object can be used to retrieve / update / create new ExceptionFormat metadata objects
     *
     * @return the ExceptionFormat persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<ExceptionFormat, String> getExceptionFormat() throws SQLException {
        if (exceptionFormat == null) {
            exceptionFormat = DaoManager.<Dao<ExceptionFormat, String>, ExceptionFormat>createDao(connection, ExceptionFormat.class);
        }
        return exceptionFormat;
    }

    /**
     * Returns the GetCapabilitiesFormat persistence object. This object can be used to retrieve / update / create new GetCapabilitiesFormat metadata objects
     *
     * @return the GetCapabilitiesFormat persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<GetCapabilitiesFormat, String> getGetCapabilitiesFormat() throws SQLException {
        if (getCapabilitiesFormat == null) {
            getCapabilitiesFormat = DaoManager.<Dao<GetCapabilitiesFormat, String>, GetCapabilitiesFormat>createDao(connection, GetCapabilitiesFormat.class);
        }
        return getCapabilitiesFormat;
    }

    /**
     * Returns the GetMapFormat persistence object. This object can be used to retrieve / update / create new GetMapFormat metadata objects
     *
     * @return the GetMapFormat persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<GetMapFormat, String> getGetMapFormat() throws SQLException {
        if (getMapFormat == null) {
            getMapFormat = DaoManager.<Dao<GetMapFormat, String>, GetMapFormat>createDao(connection, GetMapFormat.class);
        }
        return getMapFormat;
    }

    /**
     * Returns the Layer persistence object. This object can be used to retrieve / update / create new Layer metadata objects
     *
     * @return the Layer persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<Layer, Integer> getLayer() throws SQLException {
        if (layer == null) {
            layer = DaoManager.<Dao<Layer, Integer>, Layer>createDao(connection, Layer.class);
        }
        return layer;
    }

    /**
     * Returns the LegendURL persistence object. This object can be used to retrieve / update / create new LegendURL metadata objects
     *
     * @return the LegendURL persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<LegendURL, Integer> getLegendURL() throws SQLException {
        if (legendURL == null) {
            legendURL = DaoManager.<Dao<LegendURL, Integer>, LegendURL>createDao(connection, LegendURL.class);
        }
        return legendURL;
    }

    /**
     * Returns the MetadataURL persistence object. This object can be used to retrieve / update / create new MetadataURL metadata objects
     *
     * @return the MetadataURL persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<MetadataURL, Integer> getMetadataURL() throws SQLException {
        if (metadataURL == null) {
            metadataURL = DaoManager.<Dao<MetadataURL, Integer>, MetadataURL>createDao(connection, MetadataURL.class);
        }
        return metadataURL;
    }

    /**
     * Returns the Service persistence object. This object can be used to retrieve / update / create new Service metadata objects
     *
     * @return the Service persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<Service, Integer> getService() throws SQLException {
        if (service == null) {
            service = DaoManager.<Dao<Service, Integer>, Service>createDao(connection, Service.class);
        }
        return service;
    }

    /**
     * Returns the ServiceKeyword persistence object. This object can be used to retrieve / update / create new ServiceKeyword metadata objects
     *
     * @return the ServiceKeyword persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<ServiceKeyword, Integer> getServiceKeyword() throws SQLException {
        if (serviceKeyword == null) {
            serviceKeyword = DaoManager.<Dao<ServiceKeyword, Integer>, ServiceKeyword>createDao(connection, ServiceKeyword.class);
        }
        return serviceKeyword;
    }

    /**
     * Closes the persistence layer
     */
    public void closePersistence() {
        connection.closeQuietly();
    }

    /**
     * Returns the Style persistence object. This object can be used to retrieve / update / create new Style metadata objects
     *
     * @return the Style persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<Style, String> getStyle() throws SQLException {
        if (style == null) {
            style = DaoManager.<Dao<Style, String>, Style>createDao(connection, Style.class);
        }
        return style;
    }

    /**
     * Returns the Rasdaman Layer persistence object. This object can be used to retrieve / update / create new Style metadata objects
     *
     * @return the Style persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<RasdamanLayer, Integer> getRasdamanLayer() throws SQLException {
        if (rasdamanLayer == null) {
            rasdamanLayer = DaoManager.<Dao<RasdamanLayer, Integer>, RasdamanLayer>createDao(connection, RasdamanLayer.class);
        }
        return rasdamanLayer;
    }

    /**
     * Returns the Dimension persistence object. This object can be used to retrieve / update / create new Style metadata objects
     *
     * @return the Style persistence object.
     * @throws SQLException
     */
    @NotNull
    public Dao<Dimension, Integer> getDimension() throws SQLException {
        if (dimension == null) {
            dimension = DaoManager.<Dao<Dimension, Integer>, Dimension>createDao(connection, Dimension.class);
        }
        return dimension;
    }

    /**
     * Returns a persistence object for a metadata class identified by name. You should prefer the named methods for
     * individual requests as they are cached and provided overall better code quality.
     * However this method is preferred for iterating over persistence objects
     *
     * @param className the name of the metadata class
     * @param <D>       the return type of the method
     * @param <T>       the class type of the class name
     * @return a persistence object for the given class
     * @throws SQLException
     */
    public <D extends Dao<T, ?>, T> D getMetadataClassByName(Class<T> className) throws SQLException {
        return DaoManager.<D, T>createDao(connection, className);
    }

    @Nullable
    private Dao<Attribution, Integer> attribution = null;
    @Nullable
    private Dao<AuthorityURL, Integer> authorityURL = null;
    @Nullable
    private Dao<BoundingBox, Integer> boundingBox = null;
    @Nullable
    private Dao<ContactInformation, Integer> contactInformation = null;
    @Nullable
    private Dao<Crs, Integer> crs = null;
    @Nullable
    private Dao<DataURL, Integer> dataURL = null;
    @Nullable
    private Dao<EXGeographicBoundingBox, Integer> ex_GeographicBoundingBox = null;
    @Nullable
    private Dao<ExceptionFormat, String> exceptionFormat = null;
    @Nullable
    private Dao<GetCapabilitiesFormat, String> getCapabilitiesFormat = null;
    @Nullable
    private Dao<GetMapFormat, String> getMapFormat = null;
    @Nullable
    private Dao<Layer, Integer> layer = null;
    @Nullable
    private Dao<LegendURL, Integer> legendURL = null;
    @Nullable
    private Dao<MetadataURL, Integer> metadataURL = null;
    @Nullable
    private Dao<Service, Integer> service = null;
    @Nullable
    private Dao<ServiceKeyword, Integer> serviceKeyword = null;
    @Nullable
    private Dao<Style, String> style = null;
    @Nullable
    private Dao<RasdamanLayer, Integer> rasdamanLayer = null;
    @Nullable
    private Dao<Dimension, Integer> dimension = null;
    @NotNull
    private final JdbcConnectionSource connection;

}
