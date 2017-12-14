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

///<reference path="../../../models/wcs_model/wcs/_wcs.ts"/>

module rasdaman {
    export class WCSScalingExtensionModel {
        private static DEFAULT_SCALE_FACTOR:number = 1.0;
        private static DEFAULT_AXIS_SIZE:number = 0.0;

        //0 = ScaleByFactor
        //1 = ScaleAxesByFactor
        //2 = ScaleToSize
        //3 = ScaleToExtent
        public scalingType:number;

        public scaleByFactor:wcs.ScaleByFactor;
        public scaleAxesByFactor:wcs.ScaleAxesByFactor;
        public scaleToSize:wcs.ScaleToSize;
        public scaleToExtent:wcs.ScaleToExtent;

        public constructor(private coverageDescription:wcs.CoverageDescription) {
            var i:number = 0;
            var axes:string[] = [];

            // scale by factor
            coverageDescription.boundedBy.envelope.axisLabels.forEach(label=> {
                axes.push(label);
            });
            this.scaleByFactor = new wcs.ScaleByFactor(WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR);

            // scale axes by factor
            var scaleAxis:wcs.ScaleAxis[] = [];
            for (i = 0; i < axes.length; ++i) {
                scaleAxis.push(new wcs.ScaleAxis(axes[i], WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR));
            }
            this.scaleAxesByFactor = new wcs.ScaleAxesByFactor(scaleAxis);

            // scale to size
            var targetAxisSize:wcs.TargetAxisSize[] = [];
            for (i = 0; i < axes.length; ++i) {
                targetAxisSize.push(new wcs.TargetAxisSize(axes[i], WCSScalingExtensionModel.DEFAULT_AXIS_SIZE));
            }
            this.scaleToSize = new wcs.ScaleToSize(targetAxisSize);

            // scale to axis extent
            var targetAxisExtent:wcs.TargetAxisExtent[] = [];
            for (i = 0; i < axes.length; ++i) {
                var low = coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                var high = coverageDescription.boundedBy.envelope.upperCorner.values[i];
                targetAxisExtent.push(new wcs.TargetAxisExtent(axes[i], low, high));
            }
            this.scaleToExtent = new wcs.ScaleToExtent(targetAxisExtent);

            this.scalingType = 0;
        }

        public getScaling():wcs.Scaling {
            if (0 == this.scalingType) {
                return this.getScaleByFactor();
            } else if (1 == this.scalingType) {
                return this.getScaleAxesByFactor();
            } else if (2 == this.scalingType) {
                return this.getScaleToSize();
            } else {
                return this.getScaleToExtent();
            }
        }

        public clearScaling():void {
            var i:number = 0;

            this.scaleByFactor.scaleFactor = WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR;

            for (i = 0; i < this.scaleAxesByFactor.scaleAxis.length; ++i) {
                this.scaleAxesByFactor.scaleAxis[i].scaleFactor = WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR;
            }

            for (i = 0; i < this.scaleToSize.targetAxisSize.length; ++i) {
                this.scaleToSize.targetAxisSize[i].targetSize = WCSScalingExtensionModel.DEFAULT_AXIS_SIZE;
            }

            for (i = 0; i < this.scaleToExtent.targetAxisExtent.length; ++i) {
                var low = this.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                var high = this.coverageDescription.boundedBy.envelope.upperCorner.values[i];

                this.scaleToExtent.targetAxisExtent[i].low = low;
                this.scaleToExtent.targetAxisExtent[i].high = high;
            }

            this.scalingType = 0;
        }

        private getScaleByFactor():wcs.ScaleByFactor {
            if (this.scaleByFactor.scaleFactor != WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR) {
                return this.scaleByFactor;
            } else {
                return null;
            }
        }

        private getScaleAxesByFactor():wcs.ScaleAxesByFactor {
            for (var i = 0; i < this.scaleAxesByFactor.scaleAxis.length; ++i) {
                if (this.scaleAxesByFactor.scaleAxis[i].scaleFactor != WCSScalingExtensionModel.DEFAULT_SCALE_FACTOR) {
                    return this.scaleAxesByFactor;
                }
            }

            return null;
        }

        private getScaleToSize():wcs.ScaleToSize {
            for (var i = 0; i < this.scaleToSize.targetAxisSize.length; ++i) {
                if (this.scaleToSize.targetAxisSize[i].targetSize != WCSScalingExtensionModel.DEFAULT_AXIS_SIZE) {
                    return this.scaleToSize;
                }
            }

            return null;
        }

        private getScaleToExtent():wcs.ScaleToExtent {
            for (var i = 0; i < this.scaleToExtent.targetAxisExtent.length; ++i) {
                var low = this.coverageDescription.boundedBy.envelope.lowerCorner.values[i];
                var high = this.coverageDescription.boundedBy.envelope.upperCorner.values[i];

                if (this.scaleToExtent.targetAxisExtent[i].low != low
                    || this.scaleToExtent.targetAxisExtent[i].high != high) {
                    return this.scaleToExtent;
                }
            }

            return null;
        }
    }
}