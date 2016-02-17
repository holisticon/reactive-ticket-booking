define([
    'utilities',
    'configuration',
    'text!../../../../templates/booking-details.html'
], function (utilities, config, BookingDetailsTemplate) {

    return Backbone.View.extend({
        render:function () {
            var self = this;
            // retrieve the details
            $.getJSON(config.baseUrl + 'rest/shows/performance/' +
                this.model.attributes.performance.id,
                function (retrievedPerformance) {
                utilities.applyTemplate($(self.el),
                                          BookingDetailsTemplate,
                                          { booking:self.model.attributes,
                                            performance:retrievedPerformance});
            });
            return this;
        }
    });

});
