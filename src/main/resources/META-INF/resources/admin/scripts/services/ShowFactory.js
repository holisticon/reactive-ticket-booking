angular.module('ticketmonster').factory('ShowResource', function($resource){
    var resource = $resource('../rest/shows/:ShowId',{ShowId:'@id'},{'queryAll':{method:'GET',isArray:true},'query':{method:'GET',isArray:false},'update':{method:'PUT'}});
    return resource;
});