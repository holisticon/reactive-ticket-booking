angular.module('ticketmonster').factory('SectionAllocationResource', function($resource){
	return $resource('../rest/sectionallocations/:SectionAllocationId', {SectionAllocationId: '@id'}, {
		'queryAll': {
			method: 'GET',
			isArray: true
		}, 'query': {method: 'GET', isArray: false}, 'save': {method: 'PUT'}
	});
});
