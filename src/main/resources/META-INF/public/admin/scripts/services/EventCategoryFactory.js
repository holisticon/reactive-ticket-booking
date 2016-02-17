angular.module('ticketmonster').factory('EventCategoryResource', function($resource){
	return $resource('../rest/eventcategories/:EventCategoryId', {EventCategoryId: '@id'}, {
		'queryAll': { method: 'GET', isArray: true },
		'query': {method: 'GET', isArray: false},
		'save': {method: 'PUT'}
	});
});
