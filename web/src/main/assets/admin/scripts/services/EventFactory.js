angular.module('ticketmonster').factory('EventResource', function($resource){
	return $resource('../rest/events/:EventId', {EventId: '@id'}, {
		'queryAll': {method: 'GET', isArray: true},
		'query': {method: 'GET', isArray: false},
		'save': {method: 'PUT'}
	});
});
