/**
 controller-powermeter.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $routeParams, powermeter, devicesService){

    var deviceId = $routeParams.id;
    var selectedPowerMeterId = devicesService.getSelectedPowerMeterId(deviceId);

    init();

    function init(){

      setValuesToScope();
      setFunctionsToScope();

      $scope.$emit('showSpinner');
      powermeter.getPowerMeters(function(result){
        $scope.$emit('hideSpinner');
        if(result.isSuccess){
          var devices = [];
          angular.forEach(result.devices, function(device){
            devices.push({
              id: device.id,
              name: device.name,
              active: device.id === selectedPowerMeterId
            });
          });
          $scope.$apply(function(){
            $scope.devices = devices;
          });
        } else {
          $scope.$emit('showErrorModal',{error:result.error});
        }
      });
    }

    function setValuesToScope(){
      $scope.nav = {
        goBack: true,
        goGraph: false,
        goSetting: false,
        title: '電力量メータ選択'
      };
    }

    function setFunctionsToScope(){
      $scope.setPowerMeter = setPowerMeter;
    }

    function setPowerMeter(targetPowerMeter){
      var preActiveId;
      angular.forEach($scope.devices, function(device){
        if(device.active){
          preActiveId = device.id;
        }
      });
      if(preActiveId === targetPowerMeter.id){
        targetPowerMeter.active = false;
        devicesService.setPowerMeter(deviceId, undefined);
        return;
      }
      targetPowerMeter.active = true;
      devicesService.setPowerMeter(deviceId, targetPowerMeter.id);

      //選択時に、識別の為に電力計デバイスのライトを点灯する。
      powermeter.lightOn(targetPowerMeter.id, function(result){
        if(!result.isSuccess){
          $scope.$emit('showErrorModal',{error:result.error});
        }
        if(preActiveId !== undefined){
          //直前に選択されていたデバイスのライトは消灯する。
          powermeter.lightOff(preActiveId, function(result){
            if(!result.isSuccess){
              $scope.$emit('showErrorModal',{error:result.error});
            }
          });
        }
      });
      angular.forEach($scope.devices, function(device){
        if(device.id !== targetPowerMeter.id){
          device.active = false;
        }
      });
    }
  }

  angular.module('HomeDemo').controller('PowerMeterController',
  ['$scope', '$routeParams', 'powermeter', 'devices', controller]);
})();
