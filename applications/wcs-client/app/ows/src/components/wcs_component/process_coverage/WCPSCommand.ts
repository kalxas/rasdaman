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
///<reference path="WidgetConfiguration.ts"/>

module rasdaman {
    export class WCPSCommand {
        public query:string;
        public widgetConfiguration:WidgetConfiguration;
        // e.g: wwd(20,30,50,60)>> then parameters = [20, 30, 50, 60]
        public widgetParameters:string[] = [];

        public constructor(command:string) {
            rasdaman.common.ArgumentValidator.isNotNull(command, "command");

            if (command.indexOf(">>") == -1) {
                this.widgetConfiguration = null;
                this.query = command;
            } else {
                var commandParts = command.split(">>");

                var widget:WidgetConfiguration = {
                    type: commandParts[0],
                    parameters: null
                };

                if (commandParts[0].indexOf("(") != -1) {
                    var widgetParams:string[] = commandParts[0].substring(commandParts[0].indexOf("(") + 1, commandParts[0].indexOf(")")).split(",");
                    this.widgetParameters = widgetParams;
                    var params = {};
                    widgetParams.forEach((param:string)=> {
                        var parts = param.split("=");
                        params[parts[0]] = parts[1];
                    });

                    widget.type = commandParts[0].substring(0, commandParts[0].indexOf("("));
                    widget.parameters = params;
                }

                this.widgetConfiguration = widget;                
                this.query = commandParts[1];
            }
        }
    }
}