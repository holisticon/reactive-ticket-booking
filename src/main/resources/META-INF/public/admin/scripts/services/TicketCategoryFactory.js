angular.module('ticketmonster').factory('TicketCategoryResource', function($resource){
	return $resource('../rest/ticketcategories/:TicketCategoryId', {TicketCategoryId: '@id'}, {
		'queryAll': { method: 'GET', isArray: true },
		'query': {method: 'GET', isArray: false},
		'save': {method: 'PUT'}
	});
});
