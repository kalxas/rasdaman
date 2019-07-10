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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2017 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

///<reference path="../../../common/_common.ts"/>
///<reference path="../ows/ows_all.ts"/>
///<reference path="CoverageSummary.ts"/>
///<reference path="Extension.ts"/>

module wcs {
    export class Contents extends ows.ContentsBase {
        public coverageSummaries:CoverageSummary[];
        public extension:Extension;

        // If 1 coverage is remote then this column is added to WCS GetCapabilities coverages table
        public showCoverageLocationsColumn:boolean;

        // If 1 coverage has size then this column is added to WCS GetCapabilities coverages table
        public showCoverageSizesColumn:boolean;
        
        public totalLocalCoverageSizesInGBs:String;
        public totalRemoteCoverageSizesInGBs:String;
        public totalCoverageSizesInGBs:String;

        public constructor(source:rasdaman.common.ISerializedObject) {
            super(source);

            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            this.coverageSummaries = [];
            
            let totalLocalCoverageSizesInBytes = 0;
            let totalRemoteCoverageSizesInBytes = 0;
            let totalCoverageSizesInBytes = 0;

            source.getChildrenAsSerializedObjects("wcs:CoverageSummary").forEach(o => {
                let coverageSummary = new CoverageSummary(o);
                this.coverageSummaries.push(coverageSummary);

                if (coverageSummary.customizedMetadata != null) {

                    if (coverageSummary.customizedMetadata.hostname != null) {
                        this.showCoverageLocationsColumn = true;
                    }

                    if (coverageSummary.customizedMetadata.coverageSize != "N/A") {
                        this.showCoverageSizesColumn = true;

                        if (coverageSummary.customizedMetadata.hostname === undefined) {
                            totalLocalCoverageSizesInBytes += coverageSummary.customizedMetadata.localCoverageSizeInBytes;
                        } else {
                            totalRemoteCoverageSizesInBytes += coverageSummary.customizedMetadata.remoteCoverageSizeInBytes;
                        }                        
                    }
                }
            });

            totalCoverageSizesInBytes += totalLocalCoverageSizesInBytes + totalRemoteCoverageSizesInBytes;

            // Convert Bytes to GBs for total sizes of coverages
            this.totalLocalCoverageSizesInGBs = ows.CustomizedMetadata.convertBytesToGBs(totalLocalCoverageSizesInBytes);
            this.totalRemoteCoverageSizesInGBs = ows.CustomizedMetadata.convertBytesToGBs(totalRemoteCoverageSizesInBytes);
            this.totalCoverageSizesInGBs = ows.CustomizedMetadata.convertBytesToGBs(totalCoverageSizesInBytes);

            if (source.doesElementExist("wcs:Extension")) {
                this.extension = new Extension(source.getChildAsSerializedObject("wcs:Extension"));
            }
        }
    }
}
