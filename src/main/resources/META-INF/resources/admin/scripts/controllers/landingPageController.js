"use strict";

angular.module('ticketmonster').controller('LandingPageController', function($scope,  $timeout, MetricsResource) {

    var self = this;

	$scope.nodes = [];

	$scope.history = {};

	$scope.nextEntries = {};


	$scope.lineAxes = ['right','bottom'];

	/*RealTimeData.prototype.rand = function() {
		return parseInt(Math.random() * 100) + 50;
	};

	RealTimeData.prototype.history = function(entries) {
		if (typeof(entries) != 'number' || !entries) {
			entries = 60;
		}

		var history = [];
		for (var k = 0; k < this.layers; k++) {
			history.push({ values: [] });
		}

		for (var i = 0; i < entries; i++) {
			for (var j = 0; j < this.layers; j++) {
				history[j].values.push({time: this.timestamp, y: this.rand()});
			}
			this.timestamp++;
		}

		return history;
	};

	RealTimeData.prototype.next = function() {
		var entry = [];
		for (var i = 0; i < this.layers; i++) {
			entry.push({ time: this.timestamp, y: this.rand() });
		}
		this.timestamp++;
		return entry;
	};



	$scope.getNextLiveLine = function() {
		$scope.realtimeLineFeed = liveLineData.next();
		$timeout($scope.getNextLiveLine, 1000);
	};
	$timeout($scope.getNextLiveLine, 1000);*/


	function onSSE (e) {
		var nodeMetric = JSON.parse(e.data);
		var address = nodeMetric.address;
		if ($scope.nodes.indexOf(address) == -1) {
			$scope.nodes.push(address);
		}

		if ($scope.history[address] == undefined) {
			$scope.history[address] = [ { label: "cpu-combined", values: [] }, { label: "system-load-average", values: [] }];
		}
		if ($scope.nextEntries[address] == undefined) {
			$scope.nextEntries[address] = [ { label: "cpu-combined", values: [] }, { label: "system-load-average", values: [] }];
		}

		var timestamp = nodeMetric.timestamp;

		var cpuCombined = nodeMetric.metrics.filter(function(metric) { return metric.name == "cpu-combined" }).pop().value;
		var systemLoad = nodeMetric.metrics.filter(function(metric) { return metric.name == "system-load-average"; }).pop().value;

		var unixTime = Math.round( timestamp/1000 );
		$scope.history[address][0].values.push({'time':unixTime, 'y':cpuCombined});
		$scope.history[address][1].values.push({'time':unixTime, 'y':systemLoad});
		$scope.nextEntries[address][0].values.push({'time':unixTime, 'y':cpuCombined});
		$scope.nextEntries[address][1].values.push({'time':unixTime, 'y':systemLoad});

	}

    MetricsResource.addEventListener('', onSSE);


    $scope.$on("$destroy", function() {
        MetricsResource.removeEventListener();
    });

});
