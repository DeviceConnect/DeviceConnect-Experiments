(function () {
  'use strict';

  var RemoteSelectController = function ($scope, $modal, $window, $location, demoWebClient, deviceService) {
    var demoName = 'remote';

    deviceService.searchDevices(demoWebClient, 'remote_controller', function(devices) {
      $scope.list = {
        'devices' : devices
      }
      $scope.$apply();
    });

    $scope.title = 'リモコン選択';
    $scope.settingAll = function() {
      demoWebClient.discoverPlugins({
        onsuccess: function(plugins) {
          $scope.$apply(function() {
            $location.path('/settings/remote');
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
    };
    $scope.selectDevice = function(index) {
      deviceService.list(demoName).removeAll();
      deviceService.list(demoName).addDevice($scope.list.devices[index]);
      $location.path('/remote/controller');
    }
  };

  angular.module('demoweb')
    .controller('RemoteSelectController', 
      ['$scope', '$modal', '$window', '$location', 'demoWebClient', 'deviceService', RemoteSelectController]);
})();
