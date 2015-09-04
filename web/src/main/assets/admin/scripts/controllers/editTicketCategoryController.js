(function () {
	'use strict';

	angular.module('ticketmonster').controller('EditTicketCategoryController', function ($scope, $routeParams, $location, TicketCategoryResource) {
		var self = this;
		$scope.$location = $location;

		$scope.get = function () {
			var successCallback = function (data) {
				self.original = angular.copy(data);
				$scope.ticketCategory = new TicketCategoryResource(self.original);
			};
			var errorCallback = function () {
				$location.path("/TicketCategories");
			};

			if ($routeParams.TicketCategoryId) { // its an edit
				TicketCategoryResource.get({TicketCategoryId: $routeParams.TicketCategoryId}, successCallback, errorCallback);
				$scope.isCreate = false;
			} else {
				successCallback({});
				$scope.isCreate = true;
			}


		};

		$scope.isClean = function () {
			return angular.equals(self.original, $scope.ticketCategory);
		};

		$scope.save = function () {
			var successCallback = function (savedCategory) {
				if ($scope.isCreate) $location.path('/TicketCategories/edit/' + savedCategory.id);
				else $scope.get();
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};

			TicketCategoryResource.save($scope.ticketCategory, successCallback, errorCallback);
		};

		$scope.cancel = function () {
			$location.path("/TicketCategories");
		};

		$scope.remove = function () {
			var successCallback = function () {
				$location.path("/TicketCategories");
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};
			$scope.ticketCategory.$remove(successCallback, errorCallback);
		};


		$scope.get();
	});
})();
