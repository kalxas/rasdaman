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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015 Peter Baumann /
 rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

///<reference path="../../models/wcs/_wcs.ts"/>

module rasdaman {
    export class ScalingExtensionModel {
        private static DEFAULT_SCALE_FACTOR:number = 1.0;
        private static DEFAULT_AXIS_SIZE:number = 0.0;

        //0 = ScaleByFactor
        //1 = ScaleAxesByFactor
        //2 = ScaleToSize
        //3 = ScaleToExtent
        public ScalingType:number;

        public ScaleByFactor:wcs.ScaleByFactor;
        public ScaleAxesByFactor:wcs.ScaleAxesByFactor;
        public ScaleToSize:wcs.ScaleToSize;
        public ScaleToExtent:wcs.ScaleToExtent;

        public constructor(private coverageDescription:wcs.CoverageDescription) {
            var i:number = 0;
            var axes:string[] = [];
            coverageDescription.BoundedBy.Envelope.AxisLabels.forEach(label=> {
                axes.push(label);
            });

            this.ScaleByFactor = new wcs.ScaleByFactor(ScalingExtensionModel.DEFAULT_SCALE_FACTOR);

            var scaleAxis:wcs.ScaleAxis[] = [];
            for (i = 0; i < axes.length; ++i) {
                scaleAxis.push(new wcs.ScaleAxis(axes[i], ScalingExtensionModel.DEFAULT_SCALE_FACTOR));
            }

            this.ScaleAxesByFactor = new wcs.ScaleAxesByFactor(scaleAxis);

            var targetAxisSize:wcs.TargetAxisSize[] = [];
            for (i = 0; i < axes.length; ++i) {
                targetAxisSize.push(new wcs.TargetAxisSize(axes[i], ScalingExtensionModel.DEFAULT_AXIS_SIZE));
            }
            this.ScaleToSize = new wcs.ScaleToSize(targetAxisSize);

            var targetAxisExtent:wcs.TargetAxisExtent[] = [];
            for (i = 0; i < axes.length; ++i) {
                var low = coverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                var high = coverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];
                targetAxisExtent.push(new wcs.TargetAxisExtent(axes[i], low, high));
            }
            this.ScaleToExtent = new wcs.ScaleToExtent(targetAxisExtent);

            this.ScalingType = 0;
        }

        public getScaling():wcs.Scaling {
            if (0 == this.ScalingType) {
                return this.getScaleByFactor();
            } else if (1 == this.ScalingType) {
                return this.getScaleAxesByFactor();
            } else if (2 == this.ScalingType) {
                return this.getScaleToSize();
            } else {
                return this.getScaleToExtent();
            }
        }

        public clearScaling():void {
            var i:number = 0;

            this.ScaleByFactor.ScaleFactor = ScalingExtensionModel.DEFAULT_SCALE_FACTOR;

            for (i = 0; i < this.ScaleAxesByFactor.ScaleAxis.length; ++i) {
                this.ScaleAxesByFactor.ScaleAxis[i].ScaleFactor = ScalingExtensionModel.DEFAULT_SCALE_FACTOR;
            }

            for (i = 0; i < this.ScaleToSize.TargetAxisSize.length; ++i) {
                this.ScaleToSize.TargetAxisSize[i].TargetSize = ScalingExtensionModel.DEFAULT_AXIS_SIZE;
            }

            for (i = 0; i < this.ScaleToExtent.TargetAxisExtent.length; ++i) {
                var low = this.coverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                var high = this.coverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];

                this.ScaleToExtent.TargetAxisExtent[i].Low = low;
                this.ScaleToExtent.TargetAxisExtent[i].High = high;
            }

            this.ScalingType = 0;
        }

        private getScaleByFactor():wcs.ScaleByFactor {
            if (this.ScaleByFactor.ScaleFactor != ScalingExtensionModel.DEFAULT_SCALE_FACTOR) {
                return this.ScaleByFactor;
            } else {
                return null;
            }
        }

        private getScaleAxesByFactor():wcs.ScaleAxesByFactor {
            for (var i = 0; i < this.ScaleAxesByFactor.ScaleAxis.length; ++i) {
                if (this.ScaleAxesByFactor.ScaleAxis[i].ScaleFactor != ScalingExtensionModel.DEFAULT_SCALE_FACTOR) {
                    return this.ScaleAxesByFactor;
                }
            }

            return null;
        }

        private getScaleToSize():wcs.ScaleToSize {
            for (var i = 0; i < this.ScaleToSize.TargetAxisSize.length; ++i) {
                if (this.ScaleToSize.TargetAxisSize[i].TargetSize != ScalingExtensionModel.DEFAULT_AXIS_SIZE) {
                    return this.ScaleToSize;
                }
            }

            return null;
        }

        private getScaleToExtent():wcs.ScaleToExtent {
            for (var i = 0; i < this.ScaleToExtent.TargetAxisExtent.length; ++i) {
                var low = this.coverageDescription.BoundedBy.Envelope.LowerCorner.Values[i];
                var high = this.coverageDescription.BoundedBy.Envelope.UpperCorner.Values[i];

                if (this.ScaleToExtent.TargetAxisExtent[i].Low != low
                    || this.ScaleToExtent.TargetAxisExtent[i].High != high) {
                    return this.ScaleToExtent;
                }
            }

            return null;
        }
    }
}