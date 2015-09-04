/**
 * Shortcut alias definitions - will come in handy when declaring dependencies
 * Also, they allow you to keep the code free of any knowledge about library
 * locations and versions
 */
requirejs.config({
    baseUrl: "js",
    paths: {
        jquery:'../webjars/jquery/2.1.4/dist/jquery.min',
        underscore:'../webjars/underscore/1.6.0/underscore',
        text:'../webjars/requirejs-text/2.0.14/text',
        bootstrap: '../webjars/bootstrap/3.3.5/dist/js/bootstrap.min',
        backbone: '../webjars/backbone/1.1.2/backbone',
        utilities: 'app/utilities',
        router:'app/router/router'
    },
    // We shim Backbone.js and Underscore.js since they don't declare AMD modules
    shim: {
        'backbone': {
            deps: ['jquery', 'underscore'],
            exports: 'Backbone'
        },

        'underscore': {
			exports: '_'
        }
    }
});

define("initializer", ["jquery"], function ($) {
    // Configure jQuery to append timestamps to requests, to bypass browser caches
    // Important for MSIE
	$.ajaxSetup({cache:false});
    $('head').append('<link rel="stylesheet" href="webjars/bootstrap/3.3.5/dist/css/bootstrap.min.css" type="text/css" media="all"/>');
    $('head').append('<link rel="stylesheet" href="webjars/bootstrap/3.3.5/dist/css/bootstrap-theme.min.css" type="text/css" media="all"/>');
    $('head').append('<link rel="stylesheet" href="css/screen.min.css" type="text/css" media="all"/>');
    $('head').append('<link rel="stylesheet" href="webjars/font-awesome/4.4.0/css/font-awesome.min.css" type="text/css" media="all"/>');
    $('head').append('<link href="http://fonts.googleapis.com/css?family=Rokkitt" rel="stylesheet" type="text/css">');
});

// Now we declare all the dependencies
// This loads and runs the 'initializer' and 'router' modules.
require([
    'initializer',
    'router'
], function(){
});

define("configuration", {
    baseUrl : ""
});
