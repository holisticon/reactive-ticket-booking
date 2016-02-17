angular.module('ticketmonster').factory('SectionResource', function($resource){
	return $resource('../rest/sections/:SectionId', {SectionId: '@id'}, {
		'queryAll': {method: 'GET', isArray: true},
		'query': {method: 'GET', isArray: false},
		'save': {method: 'PUT'}
	});
});
