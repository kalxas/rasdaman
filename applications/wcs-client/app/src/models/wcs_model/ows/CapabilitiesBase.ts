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
///<reference path="ServiceIdentification.ts"/>
///<reference path="ServiceProvider.ts"/>
///<reference path="OperationsMetadata.ts"/>
///<reference path="Languages.ts"/>

module ows {
    export class CapabilitiesBase {
        public version:string;
        public updateSequence:string;
        public serviceIdentification:ServiceIdentification;
        public serviceProvider:ServiceProvider;
        public operationsMetadata:OperationsMetadata;
        public languages:Languages;

        public constructor(source:rasdaman.common.ISerializedObject) {
            rasdaman.common.ArgumentValidator.isNotNull(source, "source");

            this.version = source.getAttributeAsString("version");

            if (source.doesAttributeExist("updateSequence")) {
                this.updateSequence = source.getAttributeAsString("updateSequence");
            }

            if (source.doesElementExist("ows:ServiceIdentification")) {
                this.serviceIdentification = new ServiceIdentification(source.getChildAsSerializedObject("ows:ServiceIdentification"));
            }

            if (source.doesElementExist("ows:ServiceProvider")) {
                this.serviceProvider = new ServiceProvider(source.getChildAsSerializedObject("ows:ServiceProvider"));
            }

            if (source.doesElementExist("ows:OperationsMetadata")) {
                this.operationsMetadata = new OperationsMetadata(source.getChildAsSerializedObject("ows:OperationsMetadata"));
            }

            if (source.doesElementExist("Languages")) {
                this.languages = new Languages(source.getChildAsSerializedObject("Languages"));
            }
        }
    }
}