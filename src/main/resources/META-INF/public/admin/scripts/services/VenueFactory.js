angular.module('ticketmonster').factory('VenueResource', function($resource){
	return $resource('../rest/venues/:VenueId', {VenueId: '@id'}, {
		'queryAll': {method: 'GET', isArray: true},
		'query': {method: 'GET', isArray: false},
		'save': {method: 'PUT'}
	});
});
