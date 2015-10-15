/**
 controller-aircon.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $routeParams, $location, aircon, powermeter, store, devices){

    var serviceId = $routeParams.id;
    var powermeterId = store.getPowermeterId(serviceId);
    var parentBack = $scope.back;
    var temperatureValue = 27;
    var pollingIntervalId;

    init();

    function init(){
      $('html,body').scrollTop(0);

      aircon.setServiceId(serviceId);

      setValuesToScope();
      setFunctionsToScope();

      requestAllValue();
      setupPowerPolling();
      startValuesPolling();
    }

    function setValuesToScope(){
      $scope.nav = {
        goBack: true,
        goGraph: true,
        goSetting: false,
        title: 'エアコン操作'
      };
      $scope.deviceName = devices.getDeviceName(serviceId);
      $scope.consumption = getDefaultConsumption();
      $scope.roomTemperature = 'XX℃';
      $scope.mode = 'none';
      $scope.isOn = true;
      $scope.updown = {title: '温度設定', text: 'XX℃'};
    }

    function setFunctionsToScope(){
      $scope.back = back;
      $scope.transitToGraph = transitToGraph;
      $scope.pressRun = pressRun;
      $scope.pressStop = pressStop;
      $scope.pressAuto = pressAuto;
      $scope.pressDry = pressDry;
      $scope.pressCool = pressCool;
      $scope.pressHot = pressHot;
      $scope.pressAir = pressAir;
      $scope.increment = increment;
      $scope.decrement = decrement;
    }

    function back(){
      clearInterval(pollingIntervalId);
      parentBack();
    }

    function transitToGraph() {
      clearInterval(pollingIntervalId);
      $location.path('/graph/' + serviceId);
    }

    function　pressRun(){
      $scope.isOn = true;
      aircon.airconOn(checkCallback);
    }

    function pressStop(){
      $scope.isOn = false;
      aircon.airconOff(checkCallback);
    }

    function pressAuto(){
      $scope.mode = 'Automatic';
      aircon.modeAuto(checkCallback);
    }

    function pressDry(){
      $scope.mode = 'Dehumidification';
      aircon.modeDry(checkCallback);
    }

    function pressCool(){
      $scope.mode = 'Cooling';
      aircon.modeCool(checkCallback);
    }

    function pressHot(){
      $scope.mode = 'Heating';
      aircon.modeHot(checkCallback);
    }

    function pressAir(){
      $scope.mode = 'AirCirculator';
      aircon.modeAir(checkCallback);
    }

    function increment(){
      if(temperatureValue < 50){
        temperatureValue++;
        refreshTemperature();
      }
      aircon.setTemperature(temperatureValue, checkCallback);
    }

    function decrement(){
      if(temperatureValue > 0){
        temperatureValue--;
        refreshTemperature();
      }
      aircon.setTemperature(temperatureValue, checkCallback);
    }

    function checkCallback(result){
      if(!result.isSuccess){ $scope.$emit('showErrorModal',{error:result.error}); }
    }

    function getDefaultConsumption(){
      var consumption;
      if(powermeterId){
        consumption = powermeter.getLatestValue(powermeterId);
        if(consumption){
          consumption = consumption.toFixed(2);
        } else {
          consumption = '...';
        }
      } else {
        consumption = '...';
      }
      return consumption;
    }

    function setupPowerPolling(){
      powermeter.setPollingListener(function(results){
        var found = false;
        var value;
        angular.forEach(results, function(result){
          if(found){return;}
          if(result.serviceId === powermeterId){
            found = true;
            value = result.value;
          }
        });
        if(value){
          $scope.$apply(function(){
            $scope.consumption = value.toFixed(2);
          });
        }
      });
    }

    function requestRoomTemperature(){
      aircon.getRoomTemperature(function(result){
        if(result.isSuccess){
          $scope.$apply(function(){
            refreshRoomTemperature(result.response.roomtemperature);
          });
        }
      });
    }

    function requestTemperature(){
      aircon.getTemperature(function(result){
        if(result.isSuccess){
          temperatureValue = result.response.temperaturevalue;
          $scope.$apply(function(){
            refreshTemperature();
          });
        }
      });
    }

    function requestPowerStatus(){
      aircon.getPowerStatus(function(result){
        if(result.isSuccess){
          var powerstatus = result.response.powerstatus;
          $scope.$apply(function(){
            refreshPowerStatus(powerstatus);
          });
        }
      });
    }

    function requestModeStatus(){
      aircon.getModeStatus(function(result){
        if(result.isSuccess){
          var operationmodesetting = result.response.operationmodesetting;
          $scope.$apply(function(){
            refreshOperationModeSetting(operationmodesetting);
          });
        }
      });
    }

    function requestAllValue(){
      requestRoomTemperature();
      requestTemperature();
      requestPowerStatus();
      requestModeStatus();
    }

    function startValuesPolling(){
      pollingIntervalId = setInterval(function(){
        requestAllValue();
      }, 10 * 1000);
    }

    function refreshRoomTemperature(roomTemperature){
      $scope.roomTemperature = roomTemperature + '℃';
    }

    function refreshTemperature(){
      $scope.updown.text = temperatureValue + '℃';
    }

    function refreshPowerStatus(status){
      $scope.isOn = status === 'ON';
    }

    function refreshOperationModeSetting(mode){
      $scope.mode = mode;
    }
  }

  angular.module('HomeDemo').controller('AirConditionerController',
  ['$scope', '$routeParams', '$location', 'aircon', 'powermeter', 'store', 'devices', controller]);
})();
