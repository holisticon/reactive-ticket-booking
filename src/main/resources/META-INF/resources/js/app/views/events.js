define([
    'utilities',
    'text!../../../../templates/events.html'
], function (
    utilities,
    eventsTemplate) {

    var EventsView = Backbone.View.extend({
        events:{
            "click a":"update"
        },
        render:function () {
            var categories = _.uniq(
                _.map(this.model.models, function(model){
                    return model.get('category')
                }), false, function(item){
                    return item.id
                });
            utilities.applyTemplate($(this.el), eventsTemplate, {categories:categories, model:this.model})
            $(this.el).find('.item:first').addClass('active');
            $(".carousel").carousel();
            $("a[rel='popover']").popover({trigger:'hover',container:'#content'});
            return this;
        },
        update:function () {
            $("a[rel='popover']").popover('hide')
        }
    });

    return  EventsView;
});
