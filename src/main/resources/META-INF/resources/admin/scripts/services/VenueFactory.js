angular.module('ticketmonster').factory('VenueResource', function($resource){
    var resource = $resource('../rest/venues/:VenueId',{VenueId:'@id'},{'queryAll':{method:'GET',isArray:true},'query':{method:'GET',isArray:false},'update':{method:'PUT'}});
    return resource;
});