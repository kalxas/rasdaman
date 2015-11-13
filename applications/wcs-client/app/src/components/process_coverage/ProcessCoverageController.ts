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
///<reference path="../../../assets/typings/tsd.d.ts"/>
///<reference path="WCPSCommand.ts"/>
///<reference path="WCPSResultFactory.ts"/>

module rasdaman {
    export class ProcessCoverageController {

        public static $inject = [
            "$scope",
            "$log",
            "$interval",
            "Notification",
            "rasdaman.WCSService"
        ];

        public constructor($scope:ProcessCoveragesViewModel,
                           $log:angular.ILogService,
                           $interval:angular.IIntervalService,
                           notificationService:any,
                           wcsService:rasdaman.WCSService) {
            $scope.EditorOptions = {
                extraKeys: {"Ctrl-Space": "autocomplete"},
                mode: "xquery",
                theme: "eclipse",
                lineNumbers: false
            };

            $scope.EditorData = [];

            $scope.AvailableQueries = ProcessCoverageController.createExampleQueries();
            $scope.Query = $scope.AvailableQueries[0].Query;
            $scope.SelectedQuery = $scope.AvailableQueries[0].Query;

            $scope.$watch("SelectedQuery", (newValue:string, oldValue:string)=> {
                $scope.Query = newValue;
            });

            $scope.executeQuery = ()=> {
                try {
                    var command = new WCPSCommand($scope.Query);
                    var processCoverages = new wcs.ProcessCoverages(command.Query, []);
                    var waitingForResults = new WaitingForResult();

                    // Add a message that tracks the processing of the operation
                    $scope.EditorData.push(waitingForResults);
                    var indexOfResults = $scope.EditorData.length - 1;
                    // Start a counter for the current operation
                    var waitingForResultsPromise = $interval(()=> {
                        $scope.EditorData[indexOfResults].SecondsPassed++;
                    }, 1000);


                    var getBinaryData = command.WidgetConfiguration ? command.WidgetConfiguration.Type == "image" : false;

                    wcsService.processCoverages(processCoverages, getBinaryData)
                        .then(
                            (data:any)=> {
                                console.log(data);
                                console.log(data.data);
                                $scope.EditorData.push(WCPSResultFactory.getResult(command, data.data));
                            },
                            (...args:any[])=> {
                                notificationService.error("Could not process the request. Check the log for additional information.");
                                $log.error(args);
                            }
                        )
                        .finally(()=> {
                            // Stop the counter for the current operation
                            $interval.cancel(waitingForResultsPromise);
                        });
                }
                catch (error) {
                    notificationService.error("Failed to send ProcessCoverages request. Check the log for additional information.");
                    $log.error(error);
                }
            };

            $scope.getEditorDataType = (datum:any)=> {
                if (datum instanceof WaitingForResult) {
                    return 0;
                }

                if (datum instanceof RawWCPSResult) {
                    return 1;
                }

                if (datum instanceof ImageWCPSResult) {
                    return 2;
                }

                if (datum instanceof DiagramWCPSResult) {
                    return 3;
                }

                return -1;
            };


        }

        private static createExampleQueries():QueryExample[] {
            return [
                {
                    Title: '-- No Option --',
                    Query: ''
                },
                {
                    Title: 'Encode as PNG',
                    Query: 'for c in (mean_summer_airtemp) return encode(c, "png")'
                }
                //{
                //    Title: 'Most basic query',
                //    Query: 'for c in (AvgLandTemp) return 1'
                //},
                //{
                //    Title: 'Selecting a single value',
                //    Query: 'for c in ( AvgLandTemp ) return encode(c[Lat(53.08), Long(8.80), ansi("2014-07")], "csv")'
                //},
                //{
                //    Title: '3D->1D subset',
                //    Query: 'diagram>>for c in ( AvgLandTemp ) return encode(c[Lat(53.08), Long(8.80), ansi("2014-01":"2014-12")], "csv")'
                //},
                //{
                //    Title: '3D->2D subset',
                //    Query: 'image>>for c in ( AvgLandTemp ) return encode(c[ansi("2014-07")], "png")'
                //},
                //{
                //    Title: 'Celsius to Kelvin',
                //    Query: 'diagram>>for c in ( AvgLandTemp ) return encode(c[Lat(53.08), Long(8.80), ansi("2014-01":"2014-12")] + 273.15, "csv")'
                //},
                //{
                //    Title: 'Min',
                //    Query: 'for c in (AvgLandTemp) return encode(min(c[Lat(53.08), Long(8.80), ansi("2014-01":"2014-12")]), "csv")'
                //},
                //{
                //    Title: 'Max',
                //    Query: 'for c in (AvgLandTemp) return encode(max(c[Lat(53.08), Long(8.80), ansi("2014-01":"2014-12")]), "csv")'
                //},
                //{
                //    Title: 'Avg',
                //    Query: 'for c in (AvgLandTemp) return encode(avg(c[Lat(53.08), Long(8.80), ansi("2014-01":"2014-12")]), "csv")'
                //},
                //{
                //    Title: 'When is temp more than 15?',
                //    Query: 'for c in (AvgLandTemp) return encode(count(c[Lat(53.08), Long(8.80), ansi("2014-01":"2014-12")] > 15), "csv")'
                //},
                //{
                //    Title: 'On-the-fly colloring (switch)',
                //    Query: 'image>>for c in ( AvgLandTemp ) return encode(switch \n' +
                //    ' case c[ansi("2014-07"), Lat(35:75), Long(-20:40)] = 99999 \n return {red: 255; green: 255; blue: 255} \n' +
                //    ' case 18 > c[ansi("2014-07"), Lat(35:75), Long(-20:40)] \n  return {red: 0; green: 0; blue: 255} \n' +
                //    ' case 23 > c[ansi("2014-07"), Lat(35:75), Long(-20:40)] \n return {red: 255; green: 255; blue: 0} \n' +
                //    ' case 30 > c[ansi("2014-07"), Lat(35:75), Long(-20:40)]  \n return {red: 255; green: 140; blue: 0} \n' +
                //    ' default return {red: 255; green: 0; blue: 0} ' +
                //    ' , "png")'
                //},
                //{
                //    Title: 'Coverage constructor',
                //    Query: 'image>>for c in ( AvgLandTemp ) return encode(coverage myCoverage over $p x(0:100), $q y(0:100) values $p+$q, "png")'
                //},
            ];
        }
    }

    interface ProcessCoveragesViewModel extends angular.IScope {
        Query:string;
        SelectedQuery:string;
        AvailableQueries:QueryExample[];
        executeQuery():void;

        EditorOptions:CodeMirrorOptions;
        EditorData:any[];
        getEditorDataType(datum:any):number;
    }

    class WaitingForResult {
        public SecondsPassed:number;

        public constructor() {
            this.SecondsPassed = 0;
        }
    }

    /**
     * Example queries that can be executed by the user.
     */
    interface QueryExample {
        Query:string;
        Title:string;
    }

    /**
     * Object used to pass configuration options to the CodeMirror directive.
     */
    interface CodeMirrorOptions {
        lineNumbers:boolean;
        mode:string;
        theme:string;
        extraKeys:any;
    }
}