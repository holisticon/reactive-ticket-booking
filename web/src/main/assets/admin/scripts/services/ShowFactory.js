angular.module('ticketmonster').factory('ShowResource', function($resource){
	return $resource('../rest/shows/:ShowId', {ShowId: '@id'}, {
		'queryAll': {method: 'GET', isArray: true},
		'query': {method: 'GET', isArray: false},
		'save': {method: 'PUT'}
	});
});
