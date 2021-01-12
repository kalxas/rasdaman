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

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import static petascope.controller.OapiController.COLLECTION;
import static petascope.controller.OapiController.COLLECTIONS;
import static petascope.controller.OapiController.COVERAGE;
import static petascope.controller.OapiController.COVERAGE_DOMAIN_SET;
import static petascope.controller.OapiController.COVERAGE_METADATA;
import static petascope.controller.OapiController.COVERAGE_RANGE_SET;
import static petascope.controller.OapiController.COVERAGE_RANGE_TYPE;
import static petascope.controller.OapiController.WCPS;

/**
 * @author Vlad Merticariu <v.merticariu@jacobs-university.de>
 * @author Bang Pham Huu <b.phamhuu@jacobs-university.de>
 */
public class Link {
    
    public static final String DATA_REL = "data";
    public static final String PROCESSING_REL = "processing";
    public static final String REL_SELF = "self";
    
    private static final String COVERAGE_REL = "http://www.opengis.net/def/rel/ogc/1.0/coverage";
    private static final String COVERAGE_RANGE_SET_REL = "http://www.opengis.net/def/rel/ogc/1.0/coverage-rangeset";
    private static final String COVERAGE_RANGE_TYPE_REL = "http://www.opengis.net/def/rel/ogc/1.0/coverage-rangetype";
    private static final String COVERAGE_DOMAIN_SET_REL = "http://www.opengis.net/def/rel/ogc/1.0/coverage-domainset";
    private static final String COVERAGE_METADATA_REL = "http://www.opengis.net/def/rel/ogc/1.0/coverage-metadata";
    
    private String rel = "self";
    private String href;

    public Link(String href) {
        this.href = href;
    }

    public Link(String rel, String href) {
        this.rel = rel;
        this.href = href;
    }

    public String getRel() {
        return rel;
    }

    public String getHref() {
        return href;
    }
    
    public static Link getSelfLink(String urlPrefix) {
        Link selfLink = new Link(urlPrefix);
        return selfLink;
    }
    
    public static Link getDataLink(String urlPrefix) {
        Link link = new Link(DATA_REL, urlPrefix + "/" + COLLECTIONS);
        return link;
    }
    
    public static Link getProcessLink(String urlPrefix) {
        Link link = new Link(PROCESSING_REL, urlPrefix + "/" + WCPS);
        return link;
    }
    
    public static Link getDomainLink(String urlPrefix, String coverageId) {
        return new Link(StringUtils.EMPTY, urlPrefix + "/" + COLLECTIONS + "/" + coverageId + "/" + COVERAGE + "/" + COVERAGE_DOMAIN_SET);
    }

    public static Link getRangeTypeLink(String urlPrefix, String coverageId) {
        return new Link(StringUtils.EMPTY, urlPrefix + "/" + COLLECTIONS + "/" + coverageId + "/" + COVERAGE + "/" + COVERAGE_RANGE_TYPE);
    }
    
    /**
     * e.g: return https://oapi.rasdaman.org/rasdaman/oapi/collections/S2_FALSE_COLOR_84
     * with urlPrefix: https://oapi.rasdaman.org/rasdaman/oapi
     * and coverageId: S2_FALSE_COLOR_84
     */
    public static String getCoverageIdUrl(String urlPrefix, String coverageId) {
        String result = urlPrefix + "/" + COLLECTIONS + "/" + coverageId;
        return result;
    }
    
    /**
     * Return the link for getCollectionInformation request
     */
    public static List<Link> getCollectionInformationLinks(String urlPrefix, String coverageId) {
        String coverageIdUrl = getCoverageIdUrl(urlPrefix, coverageId);
        return Arrays.asList(
                new Link(REL_SELF, coverageIdUrl),
                new Link(COVERAGE_REL, coverageIdUrl + "/" + COVERAGE),
                new Link(COVERAGE_DOMAIN_SET_REL, coverageIdUrl + "/" + COVERAGE + "/" + COVERAGE_DOMAIN_SET),
                new Link(COVERAGE_RANGE_TYPE_REL, coverageIdUrl + "/" + COVERAGE + "/" + COVERAGE_RANGE_TYPE),
                new Link(COVERAGE_RANGE_SET_REL, coverageIdUrl + "/" + COVERAGE + "/" + COVERAGE_RANGE_SET),
                new Link(COVERAGE_METADATA_REL, coverageIdUrl + "/" + COVERAGE + "/" + COVERAGE_METADATA)
        );
    }

}
