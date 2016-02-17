(function () {
	'use strict';

	angular.module('ticketmonster').controller('EditShowController', function ($scope, $routeParams, $location, ShowResource, EventResource, VenueResource, TicketCategoryResource) {
		var self = this;
		$scope.disabled = false;
		$scope.$location = $location;

		$scope.get = function () {
			var successCallback = function (data) {
				self.original = angular.copy(data);
				$scope.show = new ShowResource(self.original);

			};
			var errorCallback = function () {
				$location.path("/Shows");
			};

			EventResource.queryAll(function (items) {
				$scope.eventSelectionList = items;
			});

			VenueResource.queryAll(function (items) {
				$scope.venueSelectionList = items;
			});

			TicketCategoryResource.queryAll(function (items) {
				$scope.ticketCategorySelectionList = items;
			});

			if ($routeParams.ShowId) { // its an edit
				ShowResource.get({ShowId: $routeParams.ShowId}, successCallback, errorCallback);
				$scope.isCreate = false;
			} else {
				successCallback({ticketPrices:[], performances:[]});
				$scope.isCreate = true;
			}


		};

		$scope.newPerformance = function() {
			$scope.show.performances.push({});
		};

		$scope.deletePerformance = function(performance) {
			$scope.show.performances.splice($scope.show.performances.indexOf(performance), 1);
		};

		$scope.newTicketPrice = function() {
			$scope.show.ticketPrices.push({});
		};

		$scope.deleteTicketPrice = function(ticketPrice) {
			$scope.show.ticketPrices.splice($scope.show.ticketPrices.indexOf(ticketPrice), 1);
		};

		$scope.isClean = function () {
			return angular.equals(self.original, $scope.show);
		};

		$scope.save = function () {
			var successCallback = function (savedShow) {
				if ($scope.isCreate) $location.path('/Shows/edit/' + savedShow.id);
				else $scope.get();
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};
			ShowResource.save($scope.show, successCallback, errorCallback);
		};

		$scope.cancel = function () {
			$location.path("/Shows");
		};

		$scope.remove = function () {
			var successCallback = function () {
				$location.path("/Shows");
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};
			$scope.show.$remove(successCallback, errorCallback);
		};




		$scope.get();
	});
})();
