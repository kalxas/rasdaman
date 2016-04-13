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
package petascope.wcps2.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import petascope.core.CoverageMetadata;
import petascope.wcps.metadata.CoverageInfo;

/**
 * Simple wrapper around the coverage metadata petascope class
 *
 * @author <a href="mailto:alex@flanche.net">Alex Dumitru</a>
 * @author <a href="mailto:vlad@flanche.net">Vlad Merticariu</a>
 */
public class Coverage {

    /**
     * Constructor for the class for coverages that sit in the database
     *
     * @param coverageName the name of the coverage
     * @param coverageInfo the wcps info related to the  coverage
     * @param metadata     the metadata of the coverage
     */
    public Coverage(@NotNull String coverageName, @NotNull CoverageInfo coverageInfo, @NotNull CoverageMetadata metadata) {
        this.coverageName = coverageName;
        this.coverageInfo = coverageInfo;
        this.coverageMetadata = metadata;
    }

    public Coverage(@NotNull String coverageName) {
        this.coverageName = coverageName;
    }

    /**
     * Returns the name of the coverage
     *
     * @return the name of the coverage
     */
    @NotNull
    public String getCoverageName() {
        return coverageName;
    }

    /**
     * Returns the metadata of the coverage
     *
     * @return the coverage metadata
     */
    @Nullable
    public CoverageInfo getCoverageInfo() {
        return coverageInfo;
    }

    public CoverageMetadata getCoverageMetadata() {
        return coverageMetadata;
    }

    /**
     * Determines if the coverage is compatible with a new given coverage
     *
     * @param coverage the coverage to be compared with
     * @return true if compatible, false otherwise
     */
    public boolean isCompatibleWith(@NotNull Coverage coverage) {
        if (this == DEFAULT_COVERAGE || coverage == DEFAULT_COVERAGE) {
            return true;
        }
        return coverageInfo.isCompatible(coverage.getCoverageInfo());
    }

    private String coverageName;
    private String coverageCrs;
    private CoverageInfo coverageInfo;
    private CoverageMetadata coverageMetadata;
    public static Coverage DEFAULT_COVERAGE = new Coverage("$$DEFAULT_COVERAGE$$");
}
