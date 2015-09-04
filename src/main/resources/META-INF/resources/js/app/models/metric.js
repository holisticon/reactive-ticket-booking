/**
 * Module for the Metric model
 */
define([
    // Configuration is a dependency
    'configuration',
    'backbone'
], function (config) {

    /**
     * The Metric model class definition
     * Used for CRUD operations against individual Metric
     */
    return Backbone.Model.extend({
        idAttribute: "show"
    });

});