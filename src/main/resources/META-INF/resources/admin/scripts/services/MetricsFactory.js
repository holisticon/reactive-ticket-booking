angular.module('ticketmonster').factory('MetricsResource', function($rootScope){

    var sse = null;
    var listeners = 0;

    function initSse() {
            if (sse == null) {
                sse = new EventSource('/rest/metrics');
                sse.lastEventId = 0;
            }
        return sse
    }

    return {
        removeEventListener: function() {
            listeners -= 1;
            if (listeners == 0) {
                sse.close();
                sse = null;
            }
        },

        addEventListener: function(eventName, callback) {
            listeners += 1;
            initSse().addEventListener('message', function(e) {
                var args = arguments;
                $rootScope.$apply(function () {
                    e.data = JSON.parse(e.data);
                    callback.apply(e, args);
                });
            });
        }
    };
});
