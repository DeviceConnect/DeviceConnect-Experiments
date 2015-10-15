/**
 controller-setting.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $location, $routeParams, devices, store){

    var deviceId = $routeParams.id;

    init();

    function init(){
      setValuesToScope();
      setEventsToScope();
      setFunctionsToScope();
    }

    function setValuesToScope(){
      $scope.nav = {
        goBack: true,
        goGraph: false,
        goSetting: false,
        title: '機器設定変更'
      };
      $scope.deviceId = deviceId;
      $scope.deviceName = devices.getDeviceName(deviceId);
      $scope.powerMeterName = getPowerMeterName();
    }

    function setFunctionsToScope(){
      $scope.transitToPowerMeter = transitToPowerMeter;
    }

    function setEventsToScope(){
      $scope.$watch('deviceName', watchDeviceName);
    }

    function transitToPowerMeter(){
      $location.path('/setting/powermeter/' + deviceId);
    }

    function getPowerMeterName(){
      var powerMeterName = devices.getDeviceName(devices.getSelectedPowerMeterId(deviceId));
      if(powerMeterName === '不明'){
        powerMeterName = '未設定';
      }
      return powerMeterName;
    }

    function watchDeviceName(){
      store.setDeviceName(deviceId,$scope.deviceName);
    }

  }
  angular.module('HomeDemo').controller('SettingController',
  ['$scope', '$location', '$routeParams', 'devices', 'store', controller]);
})();
