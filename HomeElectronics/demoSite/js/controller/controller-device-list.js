/**
 controller-device-list.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $routeParams, $location, devices, powermeter){

    init();

    function init(){
      setValuesToScope();
      setFunctionsToScope();
      setupPowerPolling();

      $scope.$emit('showSpinner');
      devices.getDevices(function(result){
        $scope.$emit('hideSpinner');
        if(result.isSuccess){
          $scope.$apply(function(){
            $scope.devices = result.devices;
          });
        } else {
          $scope.$emit('showErrorModal',{error:result.error});
        }
      });
    }

    function setValuesToScope(){
      $scope.nav = {
        goBack: false,
        goGraph: true,
        goSetting: true,
        title: '家電操作'
      };
      $scope.devices = [];
      $scope.consumption = getDefaultConsumption();
    }

    function setFunctionsToScope(){
      $scope.transitToControl = transitToControl;
    }

    function getDefaultConsumption(){
      var consumption = powermeter.getLatestValue();
      if(consumption){
        consumption = consumption.toFixed(2);
      }
      return consumption || '...';
    }

    function setupPowerPolling(){
      powermeter.setPollingListener(function(results){
        var total = 0;
        angular.forEach(results,function(result){
          total += result.value;
        });
        $scope.$apply(function(){
          $scope.consumption = total.toFixed(2);
        });
      });
    }

    function transitToControl(device){
      var path;
      var config;
      switch(device.scopes[0]){
        case 'airconditioner':
          path = 'aircon';
          break;
        case 'tv':
          path = 'tv';
          break;
        case 'light':
          path = 'light';
          break;
      }
      $location.path('/' + path + '/' + device.id);
    }
  }

  angular.module('HomeDemo').controller('DeviceListController',
  ['$scope', '$routeParams', '$location', 'devices', 'powermeter', controller]);
})();
