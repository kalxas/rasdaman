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
package petascope.oapi.handlers.model;

/**
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonPropertyOrder({"id", "title", "description", "extent", "CRS", "domainSet", "rangeType", "links"})
public class Collection {
    private String id;
    private String title;
    private String description;
    private Extent extent;
    private List<String> CRS;
    private Link domainSetLink;
    private Link rangeTypeLink;
    private List<Link> links;

    public Collection(String id, String title, String description, Extent extent, List<String> crss, 
                      Link domainSetLink, Link rangeType,
                      List<Link> links) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.extent = extent;
        this.CRS = crss;
        this.domainSetLink = domainSetLink;
        this.rangeTypeLink = rangeType;
        this.links = links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Extent getExtent() {
        return extent;
    }

    @JsonProperty("CRS")
    public List<String> getCRS() {
        return CRS;
    }

    public Link getDomainSet() {
        return domainSetLink;
    }

    public Link getRangeType() {
        return rangeTypeLink;
    }

    public List<Link> getLinks() {
        return links;
    }
}
