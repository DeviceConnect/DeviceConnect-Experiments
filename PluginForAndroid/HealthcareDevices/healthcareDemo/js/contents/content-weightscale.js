(function () {
  'use strict';

  var STATE_STOP = 0;
  var STATE_START = 1;
  var moniteringState = STATE_STOP;
  var lastWeight = 0;

  function getWeightValue(json) {
    console.log('incoming weight', weight);

    var digits = 1;
    var tmp = 0;
    var weight = json.weight;
   

    tmp = weight * Math.pow(10, digits);
    console.log('tmp', tmp);

    tmp = Math.round(tmp);
    console.log('Rounded tmp', tmp);

    tmp = tmp / Math.pow(10, digits);
    console.log('Return Value', tmp);
    
    if (0 == json.weightunit) {
      return tmp + " Kg";
    } else if (1 == json.weightunit) {
      return tmp + " lb";
    }
  }

  function registerWeightscale($scope, client, device) {
    client.addEventListener({
      "method": "PUT",
      "profile": "health",
      "attribute": "weightscale",
      "serviceId": device.id,
      "params": {},
      "onevent": function(event) {
        var json = JSON.parse(event);

        $scope.weightscale = getWeightValue(json);

        $("#weightscale-value").show();
        $("#weightscale-spinner").hide();
        $scope.$apply();
      },
      "onsuccess": function() {
        moniteringState = STATE_START;
        $scope.button = "STOP";
        $("#weightscale-value").hide();
        $("#weightscale-spinner").show();
        $scope.$apply();
      },
      "onerror": function(errorCode, errorMessage) {
        moniteringState = STATE_STOP;
        showErrorDialog($modal, 'Failed to starting of measurement.');
        $scope.button = "START";
        if (lastWeight != 0) {
          $("#weightscale-value").show();
        }
        $("#weightscale-spinner").hide();
        $scope.$apply();
      }
    });
  }

  function unregisterWeightscale($scope, client, device) {
    client.removeEventListener({
      "method": "DELETE",
      "profile": "health",
      "attribute": "weightscale",
      "serviceId": device.id,
      "params": {},
      "onsuccess": function() {
        moniteringState = STATE_STOP;
        $scope.button = "START";
        if (lastWeight != 0) {
          $("#weightscale-value").show();
        }
        $("#weightscale-spinner").hide();
        $scope.$apply();
      },
      "onerror": function(errorCode, errorMessage) {
        moniteringState = STATE_STOP;
      }
    });
  }

  function clickWeightscale($scope, client, device) {
    if (moniteringState == STATE_START) {
      unregisterWeightscale($scope, client, device);
    } else {
      registerWeightscale($scope, client, device);
    }
  }

  function showErrorDialog($modal, message) {
    var modalInstance = $modal.open({
      templateUrl: 'error-dialog-weightscale-select.html',
      controller: 'ModalInstanceCtrl',
      size: 'lg',
      resolve: {
        'title': function() {
          return 'Error';
        },
        'message': function() {
          return message;
        }
      }
    });
    modalInstance.result.then(function (result) {
    });
  }

  var WeightscaleController = function ($scope, $modal, $window, $location, demoWebClient, deviceService) {
    var device = undefined,
        list = deviceService.list('weightscale');
    $scope.title = "Weight Scale";
    $scope.button = "START";
    $scope.weightscale = "";
    $("#weightscale-value").hide();
    $("#weightscale-spinner").hide();
    if (list.devices.length > 0) {
      device = list.devices[0];
      $scope.deviceName = list.devices[0].name;
    } else {
      $scope.deviceName = "Device not Selected";
    }
    $scope.settingAll = function() {
      demoWebClient.discoverPlugins({
        onsuccess: function(plugins) {
          $scope.$apply(function() {
            $location.path('/settings/weightscale');
          });
        },
        onerror: function(errorCode, errorMessage) {
          $scope.$apply(function() {
            $location.path('/error/' + errorCode);
          });
        }
      });
    }
    $scope.back = function() {
      $location.path('/');
    }
    $scope.searchWeightscale = function() {
      $location.path('/radio/weightscale/health/weightscale');
    }
    $scope.clickWeightscale = function() {
      if (device) {
        if (!demoWebClient.isConnectedWebSocket()) {
          demoWebClient.connectWebSocket(function() {
            clickWeightscale($scope, demoWebClient, device);
          });
        } else {
          clickWeightscale($scope, demoWebClient, device);
        }
      } else {
        showErrorDialog($modal, 'Device is not selected.');
      }
    }
    $scope.$on("$routeChangeSuccess", function () {
      unregisterWeightscale($scope, demoWebClient, device);
    });
  }

  angular.module('demoweb')
    .controller('WeightscaleController',
      ['$scope', '$modal', '$window', '$location', 'demoWebClient', 'deviceService', WeightscaleController]);
})();
