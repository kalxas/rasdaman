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
///<reference path="../../../../assets/typings/tsd.d.ts"/>
///<reference path="WCPSCommand.ts"/>
///<reference path="WCPSResultFactory.ts"/>

module rasdaman {
    //Declare the TextDecoder object so that typescript does not complain.
    declare var TextDecoder:any;
    export class WCSProcessCoverageController {

        public static $inject = [
            "$scope",
            "$log",
            "$interval",
            "Notification",
            "rasdaman.WCSService",
            "rasdaman.ErrorHandlingService"
        ];

        public constructor($scope:WCSProcessCoveragesViewModel,
                           $log:angular.ILogService,
                           $interval:angular.IIntervalService,
                           notificationService:any,
                           wcsService:rasdaman.WCSService,
                           errorHandlingService:ErrorHandlingService) {
            $scope.editorOptions = {
                extraKeys: {"Ctrl-Space": "autocomplete"},
                mode: "xquery",
                theme: "eclipse",
                lineNumbers: false
            };

            $scope.editorData = [];

            $scope.availableQueries = WCSProcessCoverageController.createExampleQueries();
            $scope.query = $scope.availableQueries[0].query;
            $scope.selectedQuery = $scope.availableQueries[0].query;

            $scope.$watch("selectedQuery", (newValue:string, oldValue:string)=> {
                $scope.query = newValue;
            });

            // Execute button's click event handler
            $scope.executeQuery = ()=> {
                try {
                    var command = new WCPSCommand($scope.query);                            
                    var waitingForResults = new WaitingForResult();

                    // Add a message that tracks the processing of the operation
                    $scope.editorData.push(waitingForResults);                    
                    var indexOfResults = $scope.editorData.length - 1;

                    // Add the query to the scope and display in the console
                    $scope.editorData[indexOfResults].query = $scope.query;
                    // Check when query finished
                    $scope.editorData[indexOfResults].finished = false;
                    // Start a time counter for the requesting WCPS query
                    var waitingForResultsPromise = $interval(()=> {
                        $scope.editorData[indexOfResults].secondsPassed++;
                    }, 1000);

                    wcsService.processCoverages(command.query)
                        .then(
                            (data:any)=> {
                				// depend on the result, it will return an object and display on the editor console or download the result as file without display.
                				var editorRow = WCPSResultFactory.getResult(errorHandlingService, command, data.data, data.headers('Content-Type'), data.headers('File-name'));
                				if (editorRow != null) {
	                                $scope.editorData.push(editorRow);
                                } else {
                                    $scope.editorData.push(new NotificationWCPSResult(command, "Downloading WCPS query's result as a file to Web Browser."));
                                }
                            },
                            (...args:any[])=> {
                                // NOTE: Check if args[0].data is arraybuffer then convert it to string or it cannot parse correctly in Error Handler
                                if (args[0].data instanceof ArrayBuffer) {
                                    var decoder = new TextDecoder("utf-8");
                                    args[0].data = decoder.decode(new Uint8Array(args[0].data));
                                }
                                errorHandlingService.handleError(args);                                
                                $log.error(args);                                
                                $scope.editorData.push(new NotificationWCPSResult(command, "Cannot execute the requested WCPS query, error '" + args[0].data + "'."));                                
                            }
                        )
                        .finally(()=> {
                            // Stop the seconds counter for the current WCPS query as it finished.
                            $scope.editorData[indexOfResults].finished = true;
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
                    // Query not finishes yet
                    return 0;
                } else if (datum instanceof RawWCPSResult) {
                    // Text result (csv, json, gml)
                    return 1;
                } else if (datum instanceof ImageWCPSResult) {
                    // 2D image result (jpeg, png) with widget image>>
                    return 2;
                } else if (datum instanceof DiagramWCPSResult) {
                    // 1D text result with widget diagram>>
                    return 3;
                } else if (datum instanceof NotificationWCPSResult) {
                    // Just return a notification to WCPS console
                    return 4;
                }

                return -1;
            };
        }

        private static createExampleQueries():QueryExample[] {
            return [
                {
                    title: '-- Select a WCPS query --',
                    query: ''
                }, {
                    title: 'No encoding',
                    query: 'for $c in (mean_summer_airtemp) return avg($c)'
                }, {
                    title: 'Encode 2D as png with widget',
                    query: 'image>>for $c in (mean_summer_airtemp) return encode($c, "png")'
                }, {
                    title: 'Encode 2D as tiff',
                    query: 'for $c in (mean_summer_airtemp) return encode($c, "tiff")'
                }, {
                    title: 'Encode 2D as netCDF',
                    query: 'for $c in (mean_summer_airtemp) return encode($c, "application/netcdf")'
                }, {
                    title: 'Encode 1D as csv with widget',
                    query: 'diagram>>for $c in (mean_summer_airtemp) return encode($c[Lat(-20)], "text/csv")'
                }, {
                    title: 'Encode 1D as json with widget',
                    query: 'diagram>>for $c in (mean_summer_airtemp) return encode($c[Lat(-20)], "application/json")'
                }, {
                    title: 'Encode 2D as gml',
                    query: 'for $c in (mean_summer_airtemp) return encode($c[Lat(-44.525:-44.5), Long(112.5:113.5)], "application/gml+xml")'
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

    interface WCSProcessCoveragesViewModel extends angular.IScope {
        query:string;
        selectedQuery:string;
        availableQueries:QueryExample[];
        executeQuery():void;

        editorOptions:CodeMirrorOptions;
        editorData:any[];
        getEditorDataType(datum:any):number;
    }

    class WaitingForResult {
        public secondsPassed:number;

        public constructor() {
            this.secondsPassed = 0;
        }
    }

    /**
     * Example queries that can be executed by the user.
     */
    interface QueryExample {
        query:string;
        title:string;
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
