/**
 * The About view
 */
define([
    'utilities',
    'text!../../../../templates/home.html'
], function (utilities, HomeTemplate) {

    var HomeView = Backbone.View.extend({
        render:function () {
            utilities.applyTemplate($(this.el),HomeTemplate,{});
            return this;
        }
    });

    return HomeView;
});
