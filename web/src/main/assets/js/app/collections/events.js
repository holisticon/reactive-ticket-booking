/**
 * Module for the Events collection
 */
define([
    // The collection element type and configuration are dependencies
    'app/models/event',
    'configuration',
    'backbone'
], function (Event, config) {

    return Backbone.Collection.extend({
        url: config.baseUrl + "rest/events", // the URL for performing CRUD operations
        model: Event,
        id: "id", // the 'id' property of the model is the identifier
        comparator: function (model) {
            return model.get('category').id;
        }
    });
});