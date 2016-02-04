/**
 controller-devicelist.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $uibModal, $uibModalInstance, inname, $location, manager, store, discovery){
    init();

    function init(){
      setValuesToScope();
      setEventsToScope();
      setFunctionsToScope();

      var scopes = [];
      if(inname){
        scopes = inname.scopes;
      }

      $scope.devices.push({
        serviceId:undefined,
        name:"ALL"
      });
      $scope.$emit('showSpinner');
      discovery.getDevices(scopes, false, function(result){
        $scope.$emit('hideSpinner');
        if(!result.isSuccess){
          openError(result);
          return;
        }
        $scope.$apply(function(){
          angular.forEach(result.devices, function(device){
            $scope.devices.push(device);
          });
        });
      });
    }

    function setValuesToScope(){
      $scope.devices = [];
    }

    function setEventsToScope(){
    }

    function setFunctionsToScope(){
      $scope.pressDevice = pressDevice;
      $scope.pressBack = pressBack;
    }

    function pressDevice(device){
      $uibModalInstance.close(device);
    }

    function pressBack(){
      $uibModalInstance.close();
    }

    function openError(param){
      var modalInstance = $uibModal.open({
        templateUrl: './html/error.html',
        controller: 'ErrorController',
        backdrop: 'static',
        scope: $scope,
        resolve: {inname:function(){return param;}}
      });
    }

  }

  angular.module('LinkingDemo').controller('DeviceListController',
  ['$scope', '$uibModal', '$uibModalInstance', 'inname', '$location', 'manager', 'store', 'discovery', controller]);
})();
