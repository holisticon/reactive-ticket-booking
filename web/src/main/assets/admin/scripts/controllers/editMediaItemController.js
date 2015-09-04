(function () {
	'use strict';

	angular.module('ticketmonster').controller('EditMediaItemController', function ($scope, $routeParams, $location, MediaItemResource) {
		var self = this;
		$scope.disabled = false;
		$scope.$location = $location;

		$scope.get = function () {
			var successCallback = function (data) {
				self.original = angular.copy(data);
				$scope.mediaItem = new MediaItemResource(self.original);
			};
			var errorCallback = function () {
				$location.path("/MediaItems");
			};


			if ($routeParams.MediaItemId) { // its an edit
				MediaItemResource.get({MediaItemId: $routeParams.MediaItemId}, successCallback, errorCallback);
				$scope.isCreate = false;
			} else {
				successCallback({});
				$scope.isCreate = true;
			}

		};

		$scope.isClean = function () {
			return angular.equals(self.original, $scope.mediaItem);
		};

		$scope.save = function () {
			var successCallback = function (savedMediaItem) {
				if ($scope.isCreate) $location.path('/MediaItems/edit/' + savedMediaItem.id);
				else $scope.get();
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};
			MediaItemResource.save($scope.mediaItem, successCallback, errorCallback);
		};

		$scope.cancel = function () {
			$location.path("/MediaItems");
		};

		$scope.remove = function () {
			var successCallback = function () {
				$location.path("/MediaItems");
				$scope.displayError = false;
			};
			var errorCallback = function () {
				$scope.displayError = true;
			};
			$scope.mediaItem.$remove(successCallback, errorCallback);
		};

		$scope.mediaTypeList = [
			"IMAGE"
		];

		$scope.get();
	});
})();
