/**
 * Module for the Venue model
 */
define([
    'configuration',
    'backbone'
], function (config) {

    /**
     * The Venue model class definition
     * Used for CRUD operations against individual events
     */
    return Backbone.Model.extend({
        urlRoot: config.baseUrl + 'rest/venues'
    });

});