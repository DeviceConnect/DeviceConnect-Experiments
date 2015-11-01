/**
 controller-setting-list.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $location, devices){

    init();

    function init(){
      setValuesToScope();
      setFunctionsToScope();

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
        goBack: true,
        goGraph: false,
        goSetting: false,
        title: '設定変更'
      };
      $scope.devices = [];
    }

    function setFunctionsToScope(){
      $scope.transitToControl = transitToControl;
    }

    function transitToControl(device){
      $location.path('/setting/device/' + device.id);
    }
  }

  angular.module('HomeDemo').controller('SettingListController',
  ['$scope', '$location', 'devices', controller]);
})();
