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
    console.log(bindings);
    console.log(collapse);
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
