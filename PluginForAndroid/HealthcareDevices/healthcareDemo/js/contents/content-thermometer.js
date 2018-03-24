(function () {
  'use strict';

  var STATE_STOP = 0;
  var STATE_START = 1;
  var moniteringState = STATE_STOP;
  var lastThemperature = 0;

  function getThermometerValue(temperature) {
    var digits = 1;
    var tmp = 0;
    
    console.log('incoming temperature', temperature);

    tmp = temperature * Math.pow(10, digits);
    console.log('tmp', tmp);

    tmp = Math.round(tmp);
    console.log('Rounded tmp', tmp);

    tmp = tmp / Math.pow(10, digits);
    console.log('Return Value', tmp);

    return tmp + "℃";
  }

  function getThermometerValueColor(temperature) {
    var color;
    if (temperature < 36.5) {
      color = "color:#6FB606";
    } else if (temperature >= 36.5 && temperature < 37.5) {
      color = "color:#F3C000";
    } else if (temperature >= 37.5 && temperature < 38.5) {
      color = "color:#FF8C19";
    } else if (temperature >= 38.5) {
      color = "color:#FF2F19";
    }
    return color;
  }

  function registerThermometer($scope, client, device) {
    client.addEventListener({
      "method": "PUT",
      "profile": "health",
      "attribute": "thermometer",
      "serviceId": device.id,
      "params": {},
      "onevent": function(event) {
        var json = JSON.parse(event);
        var tempValue = json.temperature;

        $scope.thermometer = getThermometerValue(tempValue);
        $scope.thermometer_color = getThermometerValueColor(tempValue);
        $("#thermometer-value").show();
        $("#thermometer-spinner").hide();
        $scope.$apply();
      },
      "onsuccess": function() {
        moniteringState = STATE_START;
        $scope.button = "STOP";
        $("#thermometer-value").hide();
        $("#thermometer-spinner").show();
        $scope.$apply();
      },
      "onerror": function(errorCode, errorMessage) {
        moniteringState = STATE_STOP;
        showErrorDialog($modal, 'Failed to starting of measurement.');
        $scope.button = "START";
        if (lastThemperature != 0) {
          $("#thermometer-value").show();
        }
        $("#thermometer-spinner").hide();
        $scope.$apply();
      }
    });
  }

  function unregisterThermometer($scope, client, device) {
    client.removeEventListener({
      "method": "DELETE",
      "profile": "health",
      "attribute": "thermometer",
      "serviceId": device.id,
      "params": {},
      "onsuccess": function() {
        moniteringState = STATE_STOP;
        $scope.button = "START";
        if (lastThemperature != 0) {
          $("#thermometer-value").show();
        }
        $("#thermometer-spinner").hide();
        $scope.$apply();
      },
      "onerror": function(errorCode, errorMessage) {
        moniteringState = STATE_STOP;
      }
    });
  }

  function clickThermometer($scope, client, device) {
    if (moniteringState == STATE_START) {
      unregisterThermometer($scope, client, device);
    } else {
      registerThermometer($scope, client, device);
    }
  }

  function showErrorDialog($modal, message) {
    var modalInstance = $modal.open({
      templateUrl: 'error-dialog-thermometer-select.html',
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

  var ThermometerController = function ($scope, $modal, $window, $location, demoWebClient, deviceService) {
    var device = undefined,
        list = deviceService.list('thermometer');
    $scope.title = "Thermometer";
    $scope.button = "START";
    $scope.thermometer = "";
    $("#thermometer-value").hide();
    $("#thermometer-spinner").hide();
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
            $location.path('/settings/thermometer');
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
    $scope.searchThermometer = function() {
      $location.path('/radio/thermometer/health/thermometer');
    }
    $scope.clickThermometer = function() {
      if (device) {
        if (!demoWebClient.isConnectedWebSocket()) {
          demoWebClient.connectWebSocket(function() {
            clickThermometer($scope, demoWebClient, device);
          });
        } else {
          clickThermometer($scope, demoWebClient, device);
        }
      } else {
        showErrorDialog($modal, 'Device is not selected.');
      }
    }
    $scope.$on("$routeChangeSuccess", function () {
      unregisterThermometer($scope, demoWebClient, device);
    });
  }

  angular.module('demoweb')
    .controller('ThermometerController',
      ['$scope', '$modal', '$window', '$location', 'demoWebClient', 'deviceService', ThermometerController]);
})();
