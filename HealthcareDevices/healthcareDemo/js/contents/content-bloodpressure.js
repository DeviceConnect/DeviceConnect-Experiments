(function () {
  'use strict';

  var STATE_STOP = 0;
  var STATE_START = 1;
  var moniteringState = STATE_STOP;
  var lastBloodpressure = 0;

  function getSystolic (json) {
    console.log('Systolic Pressure',json.systolicpressure);

    var digits = 1;
    var tmp = 0;
    var systolic = json.systolicpressure;
    
    console.log('incoming systolic', systolic);

    tmp = systolic * Math.pow(10, digits);
    console.log('tmp', tmp);

    tmp = Math.round(tmp);
    console.log('Rounded tmp', tmp);

    tmp = tmp / Math.pow(10, digits);
    console.log('Return Value', tmp);
    
    if (0 == json.pressureunit) {
      return tmp + "mmHg";
    } else if (1 == json.pressureunit) {
      return tmp + "kPa";
    }
  }

  function getDiastolic (json) {
    console.log('Deastolic Pressure',json.diastolicpressure);

    var digits = 1;
    var tmp = 0;
    var diastolic = json.diastolicpressure;
    
    console.log('incoming diastolic', diastolic);

    tmp = diastolic * Math.pow(10, digits);
    console.log('tmp', tmp);

    tmp = Math.round(tmp);
    console.log('Rounded tmp', tmp);

    tmp = tmp / Math.pow(10, digits);
    console.log('Return Value', tmp);
    
    if (0 == json.pressureunit) {
      return tmp + "mmHg";
    } else if (1 == json.poressureunit) {
      return tmp + "kPa";
    }
  }

  function getMeanarterial (json) {
    console.log('Meanarterial Pressure',json.meanarterial);

    var digits = 1;
    var tmp = 0;
    var meanarterial = json.meanarterial;
    
    console.log('incoming meanarterial', meanarterial);

    tmp = meanarterial * Math.pow(10, digits);
    console.log('tmp', tmp);

    tmp = Math.round(tmp);
    console.log('Rounded tmp', tmp);

    tmp = tmp / Math.pow(10, digits);
    console.log('Return Value', tmp);
    
    if (0 == json.pressureunit) {
      return tmp + "mmHg";
    } else if (1 == json.pressureunit) {
      return tmp + "kPa";
    }
  }
   
  function registerBloodPressure($scope, client, device) {
    client.addEventListener({
      "method": "PUT",
      "profile": "health",
      "attribute": "bloodpressure",
      "serviceId": device.id,
      "params": {},
      "onevent": function(event) {
        var json = JSON.parse(event);

        $scope.systolic = getSystolic(json);
        $scope.diastolic = getDiastolic(json);
        $scope.meanarterial = getMeanarterial(json);
        
        $("#bloodpressure-spinner").hide();
        $scope.$apply();
      },
      "onsuccess": function() {
        moniteringState = STATE_START;
        $scope.button = "STOP";
        $("#bloodpressure-spinner").show();
        $scope.$apply();
      },
      "onerror": function(errorCode, errorMessage) {
        moniteringState = STATE_STOP;
        showErrorDialog($modal, 'Failed to starting of measurement.');
        $scope.button = "START";
        if (lastBloodpressure != 0) {
        }
        $("#bloodpressure-spinner").hide();
        $scope.$apply();
      }
    });
  }

  function unregisterBloodpressure($scope, client, device) {
    client.removeEventListener({
      "method": "DELETE",
      "profile": "health",
      "attribute": "bloodpressure",
      "serviceId": device.id,
      "params": {},
      "onsuccess": function() {
        moniteringState = STATE_STOP;
        $scope.button = "START";
        if (lastBloodpressure != 0) {
        }
        $("#bloodpressure-spinner").hide();
        $scope.$apply();
      },
      "onerror": function(errorCode, errorMessage) {
        moniteringState = STATE_STOP;
      }
    });
  }

  function clickBloodpressure($scope, client, device) {
    if (moniteringState == STATE_START) {
      unregisterBloodpressure($scope, client, device);
    } else {
      $scope.systolic = "------";
      $scope.diastolic = "------";
      $scope.meanarterial = "------";
      registerBloodPressure($scope, client, device);
    }
  }

  function showErrorDialog($modal, message) {
    var modalInstance = $modal.open({
      templateUrl: 'error-dialog-bloodpressure-select.html',
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

  var BloodpressureController = function ($scope, $modal, $window, $location, demoWebClient, deviceService) {
    var device = undefined,
        list = deviceService.list('bloodpressure');
    $scope.title = "Blood Pressure";
    $scope.button = "START";
    $scope.systolic = "------";
    $scope.diastolic = "------";
    $scope.meanarterial = "------";
    $("#bloodpressure-spinner").hide();
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
            $location.path('/settings/bloodpressure');
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
    $scope.searchBloodpressure = function() {
      $location.path('/radio/bloodpressure/health/bloodpressure');
    }
    $scope.clickBloodpressure = function() {
      if (device) {
        if (!demoWebClient.isConnectedWebSocket()) {
          demoWebClient.connectWebSocket(function() {
            clickBloodpressure($scope, demoWebClient, device);
          });
        } else {
          clickBloodpressure($scope, demoWebClient, device);
        }
      } else {
        showErrorDialog($modal, 'Device is not selected.');
      }
    }
    $scope.$on("$routeChangeSuccess", function () {
      unregisterBloodpressure($scope, demoWebClient, device);
    });
  }

  angular.module('demoweb')
    .controller('BloodpressureController',
      ['$scope', '$modal', '$window', '$location', 'demoWebClient', 'deviceService', BloodpressureController]);
})();
