(function () {
  'use strict';

  var DeviceListController = function ($scope, $window, $routeParams, $location, demoWebClient, deviceService) {
    var profileName = $routeParams.profileName;

    deviceService.searchDevices(demoWebClient, profileName, function(devices) {
      $scope.list = {
        'devices' : devices
      }
      $scope.$apply();
    });

    $scope.title = "Select Device";
    $scope.settingAll = function() {
      demoWebClient.discoverPlugins({
        onsuccess: function(plugins) {
          $scope.$apply(function() {
            $location.path('/settings');
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
      $window.history.back();
    };
    $scope.registerAll = function() {
      $('input[name=list-checkbox]').prop("checked", true);
    }
    $scope.unregisterAll = function() {
      $('input[name=list-checkbox]').prop("checked", false);
    }
    $scope.cancel = function() {
      $window.history.back();
    }
    $scope.ok = function() {
      var $checked = $('[name=list-checkbox]:checked');
      if ($checked.length == 0) {
      } else {
        deviceService.list(demoName).removeAll();
        var $checkbox = $('[name=list-checkbox]');
        var valList = $checkbox.map(function(index, el) {
          if (el.checked) {
            deviceService.list(demoName).addDevice($scope.list.devices[index]);
          }
          return $scope.list.devices[index];
        });
        $window.history.back();
      }
    }
  }

  angular.module('demoweb')
    .controller('DeviceListController',
      ['$scope', '$window', '$routeParams', '$location', 'demoWebClient', 'deviceService', DeviceListController]);
})();
