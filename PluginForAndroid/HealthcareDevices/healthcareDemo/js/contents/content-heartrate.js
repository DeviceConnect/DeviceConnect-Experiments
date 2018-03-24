(function () {
  'use strict';

  var STATE_NONE = 0;
  var STATE_REGISTER = 1;
  var STATE_START = 2;
  var STATE_UNREGISTER = 3;

  var moniteringState = STATE_NONE;
  var hrState = 0;

  function getHeartRateState(heartRate) {
    if (heartRate < 60) {
      return 1;
    } else if (heartRate < 90) {
      return 2;
    } else if (heartRate < 120) {
      return 3;
    } else {
      return 4;
    }
  }

  function registerHeartRate($scope, client, device) {
    moniteringState = STATE_REGISTER;
    client.addEventListener({
      "method": "PUT",
      "profile": "health",
      "attribute": "heartrate",
      "serviceId": device.id,
      "params": {},
      "onevent": function(event) {
        var json = JSON.parse(event);
        var state = getHeartRateState(json.heartrate);
        if (state != hrState) {
          hrState = state;
          $scope.heart_image = "./img/heartrate/HeartBeat" + state + ".png";
        }
        $scope.heartrate = json.heartrate;
        $scope.$apply();
      },
      "onsuccess": function() {
        moniteringState = STATE_START;
        hrState = -1;
        $scope.button = "STOP";
        $scope.$apply();
      },
      "onerror": function(errorCode, errorMessage) {
        moniteringState = STATE_NONE;
        showErrorDialog($modal, 'Failed to starting of Heart Rate.');
      }
    });
  }

  function unregisterHeartRate($scope, client, device) {
    moniteringState = STATE_UNREGISTER;
    client.removeEventListener({
      "method": "DELETE",
      "profile": "health",
      "attribute": "heartrate",
      "serviceId": device.id,
      "params": {},
      "onsuccess": function() {
        moniteringState = STATE_NONE;
        $scope.button = "START";
        $scope.$apply();
      },
      "onerror": function(errorCode, errorMessage) {
        moniteringState = STATE_NONE;
      }
    });
  }

  function clickHeartRate($scope, client, device) {
    if (moniteringState == STATE_START) {
      unregisterHeartRate($scope, client, device);
    } else if (moniteringState == STATE_NONE) {
      registerHeartRate($scope, client, device);
    }
  }

  function showErrorDialog($modal, message) {
    var modalInstance = $modal.open({
      templateUrl: 'error-dialog-heartrate-select.html',
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

  var HeartRateController = function ($scope, $modal, $window, $location, demoWebClient, deviceService) {
    var device = undefined,
        list = deviceService.list('heartrate');
    $scope.title = "Heart Rate";
    $scope.heartrate = "-";
    $scope.button = "START";
    $scope.heart_image = "./img/heartrate/HeartBeat1.png";
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
            $location.path('/settings/heartrate');
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
    $scope.searchHeartRate = function() {
      $location.path('/radio/heartrate/health/heartRate');
    }
    $scope.clickHeartRate = function() {
      if (device) {
        if (!demoWebClient.isConnectedWebSocket()) {
          demoWebClient.connectWebSocket(function() {
            clickHeartRate($scope, demoWebClient, device);
          });
        } else {
          clickHeartRate($scope, demoWebClient, device);
        }
      } else {
        showErrorDialog($modal, 'Device is not selected.');
      }
    }
    $scope.$on("$routeChangeSuccess", function () {
      unregisterHeartRate($scope, demoWebClient, device);
    });
  }

  angular.module('demoweb')
    .controller('HeartRateController',
      ['$scope', '$modal', '$window', '$location', 'demoWebClient', 'deviceService', HeartRateController]);
})();
