/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009,2010,2011,2012,2013,2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

// For any third party dependencies, like jQuery, place them in the lib folder.

// Configure loading modules from the lib directory,
// except for 'app' ones, which are in a sibling
// directory.
requirejs.config({
    baseUrl: '../js',
    paths: {
        "jQuery": "lib/jquery/jquery-1.11.1.min",
        "jQueryUI": "lib/jquery/jquery-ui.min",
        "jQueryCollapse": "lib/jquery.collapse/jquery.collapse",
        "knockout": "lib/knockout/knockout-3.2.0",
        "knockout.validation": "lib/knockout/knockout.validation",
        "bootstrap": "lib/bootstrap/bootstrap.min.js"
    },
    shim: {
        "jQueryUI": {
            export: "$",
            deps: ['jQuery']
        },
        "jQueryCollapse": {
            export: "$",
            deps: ['jQuery']
        },
        "bootstrap": {
            "deps": ['jQuery']
        },
        "knockout.validation": {
            "deps": ['knockout']
        }
    }
});

// Start loading the main app file. Put all of
// your application logic in there.
//ALWAYS ADD jQuery at the end
requirejs(["src/viewmodels/MainViewModel", "knockout", "knockout.validation", "src/Bindings", "jQueryCollapse", "jQuery", "jQueryUI"], function (MainViewModel, ko, validation, bindings, collapse) {
    ko.validation.rules.pattern.message = 'Invalid.';


    ko.validation.configure({
        registerExtenders: true,
        messagesOnModified: true,
        insertMessages: true,
        parseInputAttributes: true,
        messageTemplate: null
    });

    ko.applyBindings(new MainViewModel());
});
