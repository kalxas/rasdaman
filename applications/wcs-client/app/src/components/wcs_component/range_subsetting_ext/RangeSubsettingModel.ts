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
    export class RangeSubsettingModel {
        private availableRanges:string[];
        private isIntervals:boolean[];

        // Not allow to add more range selectors (dropdown boxes) than number of coverage's ranges
        private isMaxRanges:boolean;
        private errorMessage:string;

        public rangeSubset:wcs.RangeSubset;

        public constructor(coverageDescription:wcs.CoverageDescription) {
            this.rangeSubset = new wcs.RangeSubset();
            this.availableRanges = [];
            this.isIntervals = [];
            this.isMaxRanges = false;

            coverageDescription.rangeType.dataRecord.fields.forEach(field => {
                this.availableRanges.push(field.name);
            });
        }

        /**
         * Add 1 Range selector
         */
        public addRangeComponent():void {                     
            this.rangeSubset.rangeItem.push(new wcs.RangeComponent(this.availableRanges[0]));
            this.isIntervals.push(false);

            // Cannot add more range selectors
            if (this.isIntervals.length == this.availableRanges.length) {
                this.isMaxRanges = true;
            } else {
                // Validate the new added one
                this.validate();
            }            
        }

        /**
         * Add 1 Ranges Interval (From Range:To Range) selector
         */
        public addRangeComponentInterval():void {
            var start = new wcs.RangeComponent(this.availableRanges[0]);
            var end = new wcs.RangeComponent(this.availableRanges[this.availableRanges.length - 1]);

            this.rangeSubset.rangeItem.push(new wcs.RangeInterval(start, end));
            this.isIntervals.push(true);

            // Cannot add more range selectors
            if (this.isIntervals.length == this.availableRanges.length) {
                this.isMaxRanges = true;
            } else {
                // Validate the new added one
                this.validate();
            }
        }

        /**
         * Delete 1 Range / Ranges Interval selector         
         */
        public deleteRangeComponent(index:number):void {
            this.rangeSubset.rangeItem.splice(index, 1);
            this.isIntervals.splice(index, 1);

            this.isMaxRanges = false;

            // Also validates to hide the error message if possible
            this.validate();
        }

        /**
         * Return the index in array of ranges by name         
         */
        private getIndexByRangeName(rangeName:string):number {
            for (let i = 0; i < this.availableRanges.length; i++) {
                if (this.availableRanges[i] == rangeName) {
                    return i;
                }
            }
        }

        /**
         * Return an array of 2 selected range indexes by index from range selector
         */
        private getSelectedRangeIndexesByIndex(index:number) {
            let isInterval = this.isIntervals[index];
            let result = [];

            if (!isInterval) {
                // Single Range
                let rangeItem = this.rangeSubset.rangeItem[index] as wcs.RangeComponent;
                let rangeName = rangeItem.rangeComponent;

                let rangeIndex = this.getIndexByRangeName(rangeName);

                result.push(rangeIndex, rangeIndex);
            } else {
                // Ranges interval
                let rangeItem = this.rangeSubset.rangeItem[index] as wcs.RangeInterval;
                let fromRangeName = rangeItem.startComponent.rangeComponent;
                let endRangeName = rangeItem.endComponent.rangeComponent;

                let fromRangeIndex = this.getIndexByRangeName(fromRangeName);
                let endRangeIndex = this.getIndexByRangeName(endRangeName);

                result.push(fromRangeIndex, endRangeIndex);
            }

            return result;
        }

        /**
         * Collect all selected range indexes from range selectors
         */
        private getListOfSelectedRangeIndexes():any[] {
            let result = [];

            for (let i = 0; i < this.isIntervals.length; i++) {
                let tmpArray = this.getSelectedRangeIndexesByIndex(i);
                result.push(tmpArray);
            }

            return result;
        }

        /** 
         * Validate selected range indexes from 1 range selector index                 
         */
        private validateByIndex(index:number): boolean {
            let selectedRangeIndexesNestedArray = this.getListOfSelectedRangeIndexes();

            if (index < this.isIntervals.length) {
                let currentSelectedRangeIndexesArray = this.getSelectedRangeIndexesByIndex(index);

                for (let i = 0; i < selectedRangeIndexesNestedArray.length; i++) {
                    if (i == index) {
                        continue;
                    }
                    let selectedRangeIndexesArray = selectedRangeIndexesNestedArray[i];
                    
                    let currentStartIndex = currentSelectedRangeIndexesArray[0];
                    let currentEndIndex = currentSelectedRangeIndexesArray[1];

                    if (currentStartIndex > currentEndIndex) {
                        this.errorMessage = "Range selector " + (index + 1) + " must have lower range < upper range.";
                        return false;
                    }

                    // if 1 previous range selector select 1 Range or 1 range interval, current range selector cannot select anything within them
                    if ((currentStartIndex >= selectedRangeIndexesArray[0] && currentStartIndex <= selectedRangeIndexesArray[1])
                    || (currentEndIndex >= selectedRangeIndexesArray[0] && currentEndIndex <= selectedRangeIndexesArray[1])) {
                        this.errorMessage = "Range selector " + (index + 1) + " is duplicate or overlapping with Range selector " + (i + 1);
                        return false;
                    }
                }
            }            

            return true;
        }

        /**
         * Validate all selected ranges 
         */
        public validate():void {
            let selectedRangeIndexesNestedArray = this.getListOfSelectedRangeIndexes();

            for (let i = 0; i < this.isIntervals.length; i++) {
                let result = this.validateByIndex(i);
                if (result == false) {
                    return;
                }
            }

            // No error
            this.errorMessage = "";        
        }
    }
}