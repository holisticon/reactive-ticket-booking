angular.module('ticketmonster').factory('MediaItemResource', function($resource){
	return $resource('../rest/mediaitems/:MediaItemId', {MediaItemId: '@id'}, {
		'queryAll': {
			method: 'GET',
			isArray: true
		}, 'query': {method: 'GET', isArray: false}, 'save': {method: 'PUT'}
	});
});
