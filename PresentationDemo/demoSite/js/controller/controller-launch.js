/**
 controller-launch.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $uibModalInstance, manager, utils){
    init();

    function init(){
      setValuesToScope();
      setEventsToScope();
      setFunctionsToScope();
    }

    function setValuesToScope(){
      $scope.isMobile = utils.isMobile();
      $scope.isAndroid = utils.isAndroid();
    }

    function setEventsToScope(){
    }

    function setFunctionsToScope(){
      $scope.pressCheck = pressCheck;
      $scope.pressClose = pressClose;
    }

    function pressCheck(){
      manager.getClient().startManager();
      if(!$scope.isAndroid){
        waitAvailability({
          onavailable: function(version) {
          },
          ontimeout: function() {
          },
          onmarket: function() {
          }
        },15 * 1000);
      }
    }

    function pressClose(){
      $uibModalInstance.close();
    }

    function waitAvailability(callback, timeout){
      var interval = 250; // msec
      if (timeout <= 0) {
        callback.ontimeout();
        return;
      }
      setTimeout(function() {
        manager.checkAvailability({
          onsuccess: function(version){
            callback.onavailable(version);
          },
          onerror: function(errorCode, errorMessage) {
            switch (errorCode) {
              case dConnect.constants.ErrorCode.ACCESS_FAILED:
                setTimeout(function() {
                  window.location.href = utils.createUriForIOS();
                }, 250);
                callback.onmarket();
                return;
              default:
                break;
            }
            waitAvailability(callback, timeout - interval);
          }
        });
      }, interval);
    }

  }

  angular.module('LinkingDemo').controller('LaunchController',
  ['$scope', '$uibModalInstance', 'manager', 'utils', controller]);
})();
