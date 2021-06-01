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
package org.rasdaman.domain.owsmetadata;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.rasdaman.config.VersionManager;
import static petascope.core.KVPSymbols.WCS_SERVICE;

/**
 *
 * Class which represents OWS Service Identification and ServiceProvider and
 * returns GML in GetCapabilities
 *
 * NOTE: These examples for all children objects are for WCS, for WMS it changes
 * the XML elementNames. So, when returns this class representing in GML, must
 * rename/remove properties correspondingly.
 *
 * Detail in OWS 05-008, Table C.7 — Corresponding parameter names
 *
 * Current only support 1 OWS Service metadata for all services (WMS, WCS)
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = OwsServiceMetadata.TABLE_NAME)
public class OwsServiceMetadata {

    public static final String TABLE_NAME = "ows_service_metadata";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @Column(name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = ServiceIdentification.COLUMN_ID)
    private ServiceIdentification serviceIdentification;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = ServiceProvider.COLUMN_ID)
    private ServiceProvider serviceProvider;

    public OwsServiceMetadata() {

    }

    public ServiceIdentification getServiceIdentification() {
        return serviceIdentification;
    }

    public void setServiceIdentification(ServiceIdentification serviceIdentification) {
        this.serviceIdentification = serviceIdentification;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }
    
    /**
     * Supported WCS versions (e.g: 2.0.1, 2.1)
     */
    public void setServiceTypeVersions() {
        this.getServiceIdentification().setServiceTypeVersions(VersionManager.getAllSupportedVersions(WCS_SERVICE));
    }
}