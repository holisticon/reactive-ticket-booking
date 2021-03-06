(function () {
	'use strict';

	angular.module('ticketmonster', ['ngRoute', 'ngResource', 'ng.epoch'])
		.config(['$routeProvider', function ($routeProvider) {
			$routeProvider
				.when('/', {templateUrl: 'views/landing.html', controller: 'LandingPageController'})
				.when('/Bookings', {templateUrl: 'views/Booking/search.html', controller: 'SearchBookingController'})
				.when('/Bookings/new', {templateUrl: 'views/Booking/detail.html', controller: 'EditBookingController'})
				.when('/Bookings/edit/:BookingId', {
					templateUrl: 'views/Booking/detail.html',
					controller: 'EditBookingController'
				})
				.when('/Events', {templateUrl: 'views/Event/search.html', controller: 'SearchEventController'})
				.when('/Events/new', {templateUrl: 'views/Event/detail.html', controller: 'EditEventController'})
				.when('/Events/edit/:EventId', {
					templateUrl: 'views/Event/detail.html',
					controller: 'EditEventController'
				})
				.when('/EventCategories', {
					templateUrl: 'views/EventCategory/search.html',
					controller: 'SearchEventCategoryController'
				})
				.when('/EventCategories/new', {
					templateUrl: 'views/EventCategory/detail.html',
					controller: 'EditEventCategoryController'
				})
				.when('/EventCategories/edit/:EventCategoryId', {
					templateUrl: 'views/EventCategory/detail.html',
					controller: 'EditEventCategoryController'
				})
				.when('/MediaItems', {
					templateUrl: 'views/MediaItem/search.html',
					controller: 'SearchMediaItemController'
				})
				.when('/MediaItems/new', {
					templateUrl: 'views/MediaItem/detail.html',
					controller: 'EditMediaItemController'
				})
				.when('/MediaItems/edit/:MediaItemId', {
					templateUrl: 'views/MediaItem/detail.html',
					controller: 'EditMediaItemController'
				})
				.when('/Performances', {
					templateUrl: 'views/Performance/search.html',
					controller: 'SearchPerformanceController'
				})
				.when('/Performances/new', {
					templateUrl: 'views/Performance/detail.html',
					controller: 'EditPerformanceController'
				})
				.when('/Performances/edit/:PerformanceId', {
					templateUrl: 'views/Performance/detail.html',
					controller: 'EditPerformanceController'
				})
				.when('/Sections/new', {templateUrl: 'views/Section/detail.html', controller: 'EditSectionController'})
				.when('/Sections/edit/:SectionId', {
					templateUrl: 'views/Section/detail.html',
					controller: 'EditSectionController'
				})
				.when('/Shows', {templateUrl: 'views/Show/search.html', controller: 'SearchShowController'})
				.when('/Shows/new', {templateUrl: 'views/Show/detail.html', controller: 'EditShowController'})
				.when('/Shows/edit/:ShowId', {templateUrl: 'views/Show/detail.html', controller: 'EditShowController'})
				.when('/TicketCategories', {
					templateUrl: 'views/TicketCategory/search.html',
					controller: 'SearchTicketCategoryController'
				})
				.when('/TicketCategories/new', {
					templateUrl: 'views/TicketCategory/detail.html',
					controller: 'EditTicketCategoryController'
				})
				.when('/TicketCategories/edit/:TicketCategoryId', {
					templateUrl: 'views/TicketCategory/detail.html',
					controller: 'EditTicketCategoryController'
				})
				.when('/TicketPrices', {
					templateUrl: 'views/TicketPrice/search.html',
					controller: 'SearchTicketPriceController'
				})
				.when('/TicketPrices/new', {
					templateUrl: 'views/TicketPrice/detail.html',
					controller: 'EditTicketPriceController'
				})
				.when('/TicketPrices/edit/:TicketPriceId', {
					templateUrl: 'views/TicketPrice/detail.html',
					controller: 'EditTicketPriceController'
				})
				.when('/Venues', {templateUrl: 'views/Venue/search.html', controller: 'SearchVenueController'})
				.when('/Venues/new', {templateUrl: 'views/Venue/detail.html', controller: 'EditVenueController'})
				.when('/Venues/edit/:VenueId', {
					templateUrl: 'views/Venue/detail.html',
					controller: 'EditVenueController'
				})
				.otherwise({
					redirectTo: '/'
				});
		}])
		.controller('NavController', function NavController($scope, $location) {
			$scope.matchesRoute = function (route) {
				var path = $location.path();
				return (path === ("/" + route) || path.indexOf("/" + route + "/") === 0);
			};
		});
})();
