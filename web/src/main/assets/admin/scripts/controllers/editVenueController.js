(function () {
	'use strict';

	angular.module('ticketmonster').controller('EditVenueController', function ($scope, $routeParams, $location, VenueResource, MediaItemResource) {
		var self = this;
		$scope.$location = $location;

		$scope.get = function () {
			var successCallback = function (data) {
				self.original = angular.copy(data);
				$scope.venue = new VenueResource(data);

				MediaItemResource.queryAll(function (items) {
					$scope.mediaItemSelectionList = items;
				});
			};
			var errorCallback = function () {
				$location.path("/Venues");
			};

			if ($routeParams.VenueId) { // its an edit
				VenueResource.get({VenueId: $routeParams.VenueId}, successCallback, errorCallback);
				$scope.isCreate = false;
			} else {
				successCallback({ 'address':{}, 'sections':[] });
				$scope.isCreate = true;
			}

		};

		$scope.newSection = function() {
			$scope.venue.sections.push({});
		};

		$scope.deleteSection = function(section) {
			$scope.venue.sections.splice($scope.venue.sections.indexOf(section), 1);
		};

		$scope.isClean = function () {
			return angular.equals(self.original, $scope.venue);
		};

		$scope.save = function () {
			var successCallback = function (savedResource) {
				if ($scope.isCreate) $location.path('/Venues/edit/' + savedResource.id);
				else $scope.get();
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};

			VenueResource.save($scope.venue, successCallback, errorCallback);
		};

		$scope.cancel = function () {
			$location.path("/Venues");
		};

		$scope.remove = function () {
			var successCallback = function () {
				$location.path("/Venues");
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};
			$scope.venue.$remove(successCallback, errorCallback);
		};




		$scope.get();
	});

})();
