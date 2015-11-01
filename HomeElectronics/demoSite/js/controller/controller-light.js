/**
 controller-light.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $routeParams, $location, $document, devices, light, powermeter, store){

    var id;
    var ids;
    var serviceId;
    var lightId;
    var powermeterId;
    var brightnessLevel = [0, 0.25, 0.5, 0.75, 1];
    var currentLevel = 4;//0~4
    var picker;

    init();

    function init(){
      $('html,body').scrollTop(0);

      id = $routeParams.id;
      ids = id.split("?demo_lightId:");
      serviceId = ids[0];
      lightId = ids[1];
      powermeterId = store.getPowermeterId(id);

      light.resetStatus();
      light.fetchName(serviceId, lightId, function(result){});
      picker = new ColorPicker(function(color){
        light.setColor(color);
        light.changeStatus(checkCallback);
      });

      setValuesToScope();
      setFunctionsToScope();

      setupPowerPolling();
    }

    function setValuesToScope(){
      $scope.nav = {
        goBack: true,
        goGraph: true,
        goSetting: false,
        title: '照明操作'
      };
      $scope.deviceName = devices.getDeviceName(id);
      $scope.consumption = getDefaultConsumption();
      $scope.updown = {title: '明るさ', text: 'レベル5' };
    }

    function setFunctionsToScope(){
      $scope.transitToGraph = transitToGraph;
      $scope.pressOn = pressOn;
      $scope.pressOff = pressOff;
      $scope.increment = increment;
      $scope.decrement = decrement;
      $scope.onUpdownLoaded = onUpdownLoaded;
    }

    function checkCallback(result){
      if(!result.isSuccess){
        if(result.error.code !== -101){
          $scope.$emit('showErrorModal',{error:result.error}); }
      }
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

    function transitToGraph() {
      $location.path('/graph/' + id);
    }

    function pressOn(){
      light.lightOn(checkCallback);
    }

    function pressOff(){
      light.lightOff(checkCallback);
    }

    function increment(){
      if(currentLevel < 4){
        currentLevel++;
        $scope.updown.text = 'レベル' + (currentLevel + 1);
        light.setBrightness(brightnessLevel[currentLevel]);
        light.changeStatus(checkCallback);
      }
    }

    function decrement(){
      if(currentLevel > 0){
        currentLevel--;
        $scope.updown.text = 'レベル' + (currentLevel + 1);
        light.setBrightness(brightnessLevel[currentLevel]);
        light.changeStatus(checkCallback);
      }
    }

    function onUpdownLoaded(){
      picker.drawPicker();
    }
  }

  angular.module('HomeDemo').controller('LightController',
  ['$scope', '$routeParams', '$location', '$document', 'devices', 'light', 'powermeter', 'store', controller]);
})();
