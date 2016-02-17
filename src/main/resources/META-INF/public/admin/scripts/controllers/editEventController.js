(function () {
	'use strict';

	angular.module('ticketmonster').controller('EditEventController', function ($scope, $routeParams, $location, EventResource, MediaItemResource, EventCategoryResource) {
		var self = this;
		$scope.$location = $location;


		$scope.get = function () {
			var successCallback = function (data) {
				self.original = angular.copy(data);
				$scope.event = new EventResource(data);
				MediaItemResource.queryAll(function (items) {
					$scope.mediaItemSelectionList = items;
				});
				EventCategoryResource.queryAll(function (items) {
					$scope.categorySelectionList = items;
				});
			};
			var errorCallback = function () {
				$location.path("/Events");
			};

			if ($routeParams.EventId) { // its an edit
				EventResource.get({EventId: $routeParams.EventId}, successCallback, errorCallback);
				$scope.isCreate = false;
			} else {
				successCallback({});
				$scope.isCreate = true;
			}


		};

		$scope.isClean = function () {
			return angular.equals(self.original, $scope.event);
		};

		$scope.save = function () {
			var successCallback = function (savedEvent) {
				if ($scope.isCreate) $location.path('/Events/edit/' + savedEvent.id);
				else $scope.get();
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};
			EventResource.save($scope.event, successCallback, errorCallback);
		};

		$scope.cancel = function () {
			$location.path("/Events");
		};

		$scope.remove = function () {
			var successCallback = function () {
				$location.path("/Events");
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};
			$scope.event.$remove(successCallback, errorCallback);
		};



		$scope.get();
	});
})();
