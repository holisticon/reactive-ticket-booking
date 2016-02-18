define([
    'utilities',
    'text!../../../../templates/venues.html'
], function (
    utilities,
    venuesTemplate) {

    var EventsView = Backbone.View.extend({
        events:{
            "click a":"update"
        },
        render:function () {
            var cities = _.uniq(
                _.map(this.model.models, function(model){
                    return model.get('address').city;
                }));
            utilities.applyTemplate($(this.el), venuesTemplate, {cities: cities, model:this.model});
            $(this.el).find('.item:first').addClass('active');
            $(".carousel").carousel();
            $("a[rel='popover']").popover({trigger:'hover',container:'#content'});
            return this;
        },
        update:function () {
            $("a[rel='popover']").popover('hide');
        }
    });

    return  EventsView;
});
