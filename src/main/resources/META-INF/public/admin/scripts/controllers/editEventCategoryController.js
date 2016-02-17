(function () {
	'use strict';

	angular.module('ticketmonster').controller('EditEventCategoryController', function ($scope, $routeParams, $location, EventCategoryResource) {
		var self = this;


		$scope.$location = $location;

		$scope.get = function () {
			var successCallback = function (data) {
				self.original = angular.copy(data);
				$scope.eventCategory = new EventCategoryResource(self.original);
			};
			var errorCallback = function () {
				$location.path("/EventCategories");
			};

			if ($routeParams.EventCategoryId) { // its an edit
				EventCategoryResource.get({EventCategoryId: $routeParams.EventCategoryId}, successCallback, errorCallback);
				$scope.isCreate = false;

			} else {
				successCallback({});
				$scope.isCreate = true;
			}
		};

		$scope.isClean = function () {
			return angular.equals(self.original, $scope.eventCategory);
		};

		$scope.save = function () {
			var successCallback = function (savedCategory) {
				if ($scope.isCreate) $location.path('/EventCategories/edit/' + savedCategory.id);
				else $scope.get();
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};

			EventCategoryResource.save($scope.eventCategory, successCallback, errorCallback);

		};

		$scope.cancel = function () {
			$location.path("/EventCategories");
		};

		$scope.remove = function () {
			var successCallback = function () {
				$location.path("/EventCategories");
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};
			$scope.eventCategory.$remove(successCallback, errorCallback);
		};


		$scope.get();
	});
})();
