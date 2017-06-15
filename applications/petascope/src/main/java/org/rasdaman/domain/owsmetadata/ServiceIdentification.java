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

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 * OWS 05-008
 *
 * Return ServiceIdentification element in service metadata document of
 * GetCapabilities Request. Metadata about this specific server. The schema of
 * this section shall be the same for all OWSs.
 *
 * Detail: 7.4.3 ServiceIdentification section contents
 *
 * Example:
 *
 * <ows:ServiceIdentification>
 * <ows:Title>Web Coverage Service</ows:Title>
 * <ows:Abstract>
 * This server implements the WCS specification 1.0 and 1.1.1, it's reference
 * implementation of WCS 1.1.1. All layers published by this service are
 * available on WMS also.
 * </ows:Abstract>
 * <ows:Keywords>
 * <ows:Keyword>WCS</ows:Keyword>
 * <ows:Keyword>WMS</ows:Keyword>
 * <ows:Keyword>GEOSERVER</ows:Keyword>
 * </ows:Keywords>
 * <ows:ServiceType>OGC WCS</ows:ServiceType>
 * <ows:ServiceTypeVersion>2.0.1</ows:ServiceTypeVersion>
 * <ows:ServiceTypeVersion>1.1.1</ows:ServiceTypeVersion>
 * <ows:ServiceTypeVersion>1.1.0</ows:ServiceTypeVersion>
 * <ows:Fees>NONE</ows:Fees>
 * <ows:AccessConstraints>NONE</ows:AccessConstraints>
 *
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
@Entity
@Table(name = ServiceIdentification.TABLE_NAME)
public class ServiceIdentification {

    public static final String TABLE_NAME = "service_identification";
    public static final String COLUMN_ID = TABLE_NAME + "_id";

    @Id
    @Column(name = COLUMN_ID)
    @GeneratedValue(strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "service_type")
    // One, mandatory
    private String serviceType;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    // One or more, mandatory
    private List<String> serviceTypeVersions = new ArrayList<>();

    @Column(name = "service_title")
    // One, mandatory
    private String serviceTitle;

    @Column(name = "service_abstract")
    @Lob
    // NOTE: As this could be long text, so varchar(255) is not enough
    // Zero or one, optional
    private String serviceAbstract;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    // Zero or more, optional
    private List<String> keywords = new ArrayList<>();

    @Column(name = "fees")
    // Zero or one, optional
    private String fees;

    @ElementCollection(fetch = FetchType.EAGER)
    @OrderColumn
    // Zero or more, optional
    private List<String> accessConstraints = new ArrayList<>();

    public ServiceIdentification() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public List<String> getServiceTypeVersions() {
        return serviceTypeVersions;
    }

    public void setServiceTypeVersions(List<String> serviceTypeVersions) {
        this.serviceTypeVersions = serviceTypeVersions;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public void setServiceTitle(String serviceTitle) {
        this.serviceTitle = serviceTitle;
    }

    public String getServiceAbstract() {
        return serviceAbstract;
    }

    public void setServiceAbstract(String serviceAbstract) {
        this.serviceAbstract = serviceAbstract;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public List<String> getAccessConstraints() {
        return accessConstraints;
    }

    public void setAccessConstraints(List<String> accessConstraints) {
        this.accessConstraints = accessConstraints;
    }

}
