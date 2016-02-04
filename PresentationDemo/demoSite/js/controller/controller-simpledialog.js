/**
 controller-simpledialog.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $uibModalInstance, params){
    init();

    function init(){
      setValuesToScope();
      setEventsToScope();
      setFunctionsToScope();
    }

    function setValuesToScope(){
      $scope.dialogTitle = params.title;
      $scope.dialogMessage = params.message;
    }

    function setEventsToScope(){
    }

    function setFunctionsToScope(){
      $scope.pressOK = pressOK;
      $scope.pressCancel = pressCancel;
    }

    function pressOK(){
      $uibModalInstance.close({isOK:true});
    }

    function pressCancel(){
      $uibModalInstance.close({isOK:false});
    }

  }

  angular.module('LinkingDemo').controller('SimpleDialogController',
  ['$scope', '$uibModalInstance', 'params', controller]);
})();
