/**
 controller-tv.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $routeParams, $location, tv, powermeter, store, devices){

    var serviceId = $routeParams.id;
    var powermeterId = store.getPowermeterId(serviceId);

    init();

    function init(){
      $('html,body').scrollTop(0);

      tv.setServiceId(serviceId);

      setValuesToScope();
      setFunctionsToScope();

      setupPowerPolling();
    }

    function setValuesToScope(){
      $scope.nav = {
        goBack: true,
        goGraph: true,
        goSetting: false,
        title: 'テレビ操作'
      };
      $scope.deviceName = devices.getDeviceName(serviceId);
      $scope.consumption = getDefaultConsumption();
      $scope.mode = 'none';
    }

    function setFunctionsToScope(){
      $scope.transitToGraph = transitToGraph;
      $scope.pressPower = pressPower;
      $scope.pressDigital = pressDigital;
      $scope.pressCS = pressCS;
      $scope.pressBS = pressBS;
      $scope.pressCh = pressCh;
      $scope.pressChUp = pressChUp;
      $scope.pressChDown = pressChDown;
      $scope.pressVolUp = pressVolUp;
      $scope.pressVolDown = pressVolDown;
    }

    function transitToGraph() {
      $location.path('/graph/' + serviceId);
    }

    function pressPower(){
      tv.tvOn(checkCallback);
    }

    function pressDigital(){
      $scope.mode = 'digital';
      tv.broadcastwaveDigital(checkCallback);
    }

    function pressCS(){
      $scope.mode = 'cs';
      tv.broadcastwaveCS(checkCallback);
    }

    function pressBS(){
      $scope.mode = 'bs';
      tv.broadcastwaveBS(checkCallback);
    }

    function pressCh(number){
      tv.channelTuning(number,checkCallback);
    }

    function pressChUp(){
      tv.channelNext(checkCallback);
    }

    function pressChDown(){
      tv.channelPrevious(checkCallback);
    }

    function pressVolUp(){
      tv.volumeUp(checkCallback);
    }

    function pressVolDown(){
      tv.volumeDown(checkCallback);
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
  }

  angular.module('HomeDemo').controller('TVController',
  ['$scope', '$routeParams', '$location', 'tv', 'powermeter', 'store', 'devices', controller]);
})();
