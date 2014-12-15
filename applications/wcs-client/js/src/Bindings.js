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

define(["knockout", "src/models/Util", "jQuery", "jQueryUI"], function (ko, Util) {
    ko.bindingHandlers.tabsBinding = {
        init: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            $(element).tabs();
        },
        update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
        }
    };

    ko.bindingHandlers.enabledTab = {
        disabledTabsAtInit: [],

        init: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            var parentControl = $(element).parent();
            var currentId = $(element).attr("id");
            if (parentControl) {
                var tabControl = parentControl.tabs("instance");
                if (tabControl) {
                    var tabs = tabControl.tabs;
                    for (var i = 0; i < tabs.length; i++) {
                        if ($(tabs[i]).attr("aria-controls") == currentId) {
                            if (valueAccessor() == false) {
                                ko.bindingHandlers.enabledTab.disabledTabsAtInit.push(i);
                            }
                            break;
                        }
                    }

                    if (i == tabs.length - 1) {
                        $(element).parent().tabs({disabled: ko.bindingHandlers.enabledTab.disabledTabsAtInit});
                    }
                }
            }
        },

        update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            var parentControl = $(element).parent();
            var currentId = $(element).attr("id");
            var index;
            var disabledTabs = ko.bindingHandlers.enabledTab.disabledTabsAtInit;
            if (parentControl) {
                var tabControl = parentControl.tabs("instance");
                if (tabControl) {
                    var tabs = tabControl.tabs;
                    for (var i = 0; i < tabs.length; i++) {
                        if ($(tabs[i]).attr("aria-controls") == currentId) {
                            if (valueAccessor() == true) {
                                index = disabledTabs.indexOf(i);
                                if (index > -1) {
                                    disabledTabs.splice(index, 1);
                                }
                            } else {
                                index = disabledTabs.indexOf(i);
                                if (index == -1) {
                                    disabledTabs.push(i);
                                }
                            }
                            break;
                        }
                    }
                    //Update the disabled tabs
                    $(element).parent().tabs({disabled: ko.bindingHandlers.enabledTab.disabledTabsAtInit});
                }
            }
        }
    };

    ko.bindingHandlers.autocompleteBox = {
        init: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            ko.utils.domNodeDisposal.addDisposeCallback(element, function () {
                $(element).autocomplete("destroy");
            });
        },
        update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            //Create the autocomplete list
            var coverageSummaries = valueAccessor();
            var autocompleteTerms = [];
            for (var i = 0; i < coverageSummaries.length; i++) {
                autocompleteTerms.push(coverageSummaries[i].coverageId);
            }

            $(element).autocomplete({
                source: Util.createAutocompleteFilter(autocompleteTerms)
            });
        }
    };

    ko.bindingHandlers.coverageBounds = {
        init: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
        },
        update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            var resultText = "";
            var envelope = valueAccessor();
            for (var i = 0; i < envelope.srsDimension; i++) {
                resultText += "<span class='important-text'>" + envelope.lowerCorner[i] + "</span>  to " + "<span class='important-text'>" + envelope.upperCorner[i] + "</span> on axis <span class='important-text'>" + envelope.axisLabels[i] + "</span>,";
            }
            resultText = resultText.substr(0, resultText.length - 2);
            $(element).html(resultText);
        }
    };

    ko.bindingHandlers.prettifyXML = {
        init: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
        },
        update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {

            $(element).removeClass("prettyprinted");
            $(element).empty();
            $(element).append("<code></code>");
            $($(element).children()[0]).text(valueAccessor());

            prettyPrint();
        }
    };


    ko.bindingHandlers.jQueryDoubleSlider = {
        init: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            ko.utils.domNodeDisposal.addDisposeCallback(element, function () {
                $(element).slider("destroy");
            });
        },
        update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            var step = 1;
            var model = valueAccessor();

            function updateValues(event, ui) {
                valueAccessor().sliderMin(ui.values[0]);
                valueAccessor().sliderMax(ui.values[1]);
            }

            if (model.upperCorner - model.lowerCorner <= 5) {
                step = (model.upperCorner - model.lowerCorner) / 10;
            }

            $(element).slider({
                range: true,
                orientation: "horizontal",
                step: step,
                min: model.lowerCorner,
                max: model.upperCorner,
                values: [model.sliderMin(), model.sliderMax()],
                animate: true,
                slide: updateValues
            });
        }
    };

    ko.bindingHandlers.jQuerySlider = {
        init: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            ko.utils.domNodeDisposal.addDisposeCallback(element, function () {
                $(element).slider("destroy");
            });
        },
        update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            var model = valueAccessor();
            var step = 1;

            function updateValues(event, ui) {
                valueAccessor().sliderMin(ui.value);
            }

            if (model.upperCorner - model.lowerCorner <= 5) {
                step = (model.upperCorner - model.lowerCorner) / 10;
            }

            $(element).slider({
                range: false,
                orientation: "horizontal",
                step: step,
                min: model.lowerCorner,
                max: model.upperCorner,
                value: model.sliderMin(),
                animate: true,
                slide: updateValues
            });

        }
    };

    ko.bindingHandlers.jQueryCollapse = {
        init: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            jQuery(element).collapse();
        },
        update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
        }
    };

    ko.bindingHandlers.myDebug = {
        init: function (element, valueAccessor, allBindings, viewModel, bindingContext) {

        },
        update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
            console.log(valueAccessor());
        }
    };

    ko.bindingHandlers.bootstrapAlert = {
        init: function (element, valueAccessor, allBindings, viewModel, bindingContext) {
        },
        update: function (element, valueAccessor, allBindings, viewModel, bindingContext) {

            var alertLevels = ["success", "info", "warning", "danger"];
            var message = "Something went wrong!";
            var alertLevel = "info";
            var id = Util.createGuid();
            var alertObject = valueAccessor();
            if (alertObject) {
                if (alertObject.message) {
                    message = alertObject.message;
                }
                if (alertObject.level && alertLevels.indexOf(alertObject.level) != -1) {
                    alertLevel = alertObject.level;
                }

                var html = '<div id="' + id + '" class="alert alert-' + alertLevel + ' alert-dismissible" role="alert">' +
                    '<button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span>' +
                    '<span class="sr-only">Close</span></button>' + message + '</div>';

                var alertDiv = jQuery(html);
                //alertDiv.fadeOut({
                //    duration:5000,
                //    complete: function () {
                //        var filter = "#"+id;
                //        window.setTimeout(function(){jQuery(filter).remove();},5000);
                //        console.log("complete");
                //    }
                //});

                jQuery(element).append(alertDiv);
            }
        }
    };
});