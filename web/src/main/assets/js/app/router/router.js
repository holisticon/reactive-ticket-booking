/**
 * A module for the router of the desktop application
 */
define("router", [
	'jquery',
	'underscore',
	'configuration',
	'utilities',
	'app/models/booking',
	'app/models/event',
	'app/models/venue',
	'app/collections/bookings',
	'app/collections/events',
	'app/collections/venues',
	'app/views/home',
	'app/views/events',
	'app/views/venues',
	'app/views/create-booking',
	'app/views/bookings',
	'app/views/event-detail',
	'app/views/venue-detail',
	'app/views/booking-detail',
	'text!../templates/main.html'
],function($,_,
			config,
			utilities,
			Booking,
			Event,
			Venue,
			Bookings,
			Events,
			Venues,
			HomeView,
			EventsView,
			VenuesView,
			CreateBookingView,
			BookingsView,
			EventDetailView,
			VenueDetailView,
			BookingDetailView,
			MainTemplate) {

	$(document).ready(function () {
		utilities.applyTemplate($('body'), MainTemplate);
	});

	/**
	 * The Router class contains all the routes within the application -
	 * i.e. URLs and the actions that will be taken as a result.
	 *
	 * @type {Router}
	 */

	var Router = Backbone.Router.extend({
		initialize: function () {
			//Begin dispatching routes
			Backbone.history.start();
		},
		routes: {
			"": "home",
			"about": "home",
			"events": "events",
			"events/:id": "eventDetail",
			"venues": "venues",
			"venues/:id": "venueDetail",
			"book/:showId/:performanceId": "bookTickets",
			"bookings": "listBookings",
			"bookings/:id": "bookingDetail",
			"ignore": "ignore",
			"*actions": "defaultHandler"
		},
		events: function () {
			var events = new Events();
			var eventsView = new EventsView({model: events, el: $("#content")});
			events.on("reset",
				function () {
					utilities.viewManager.showView(eventsView);
				}).fetch({
					reset: true,
					error: function () {
						utilities.displayAlert("Failed to retrieve events from the TicketMonster server.");
					}
				});
		},
		venues: function () {
			var venues = new Venues();
			var venuesView = new VenuesView({model: venues, el: $("#content")});
			venues.on("reset",
				function () {
					utilities.viewManager.showView(venuesView);
				}).fetch({
					reset: true,
					error: function () {
						utilities.displayAlert("Failed to retrieve venues from the TicketMonster server.");
					}
				});
		},
		home: function () {
			utilities.viewManager.showView(new HomeView({el: $("#content")}));
		},
		bookTickets: function (showId, performanceId) {
			var createBookingView =
				new CreateBookingView({
					model: {
						showId: showId,
						performanceId: performanceId,
						bookingRequest: {tickets: []}
					},
					el: $("#content")
				});
			utilities.viewManager.showView(createBookingView);
		},
		listBookings: function () {
			$.get(
				config.baseUrl + "rest/bookings/count",
				function (data) {
					var bookings = new Bookings();
					var bookingsView = new BookingsView({
						model: {bookings: bookings},
						el: $("#content"),
						pageSize: 10,
						page: 1,
						count: data.count
					});

					bookings.on("destroy",
						function () {
							bookingsView.refreshPage();
						});
					bookings.fetch({
						reset: true,
						error: function () {
							utilities.displayAlert("Failed to retrieve bookings from the TicketMonster server.");
						},
						data: {first: 1, maxResults: 10},
						processData: true, success: function () {
							utilities.viewManager.showView(bookingsView);
						}
					});
				}
			);

		},
		eventDetail: function (id) {
			var model = new Event({id: id});
			var eventDetailView = new EventDetailView({model: model, el: $("#content")});
			model.on("change",
				function () {
					utilities.viewManager.showView(eventDetailView);
				}).fetch({
					error: function () {
						utilities.displayAlert("Failed to retrieve the event from the TicketMonster server.");
					}
				});
		},
		venueDetail: function (id) {
			var model = new Venue({id: id});
			var venueDetailView = new VenueDetailView({model: model, el: $("#content")});
			model.on("change",
				function () {
					utilities.viewManager.showView(venueDetailView);
				}).fetch({
					error: function () {
						utilities.displayAlert("Failed to retrieve the venue from the TicketMonster server.");
					}
				});
		},
		bookingDetail: function (id) {
			var bookingModel = new Booking({id: id});
			var bookingDetailView = new BookingDetailView({model: bookingModel, el: $("#content")});
			bookingModel.on("change",
				function () {
					utilities.viewManager.showView(bookingDetailView);
				}).fetch({
					error: function () {
						utilities.displayAlert("Failed to retrieve the booking from the TicketMonster server.");
					}
				});

		}
	});

	// Create a router instance
	var router = new Router();

	return router;
});
