/**
 controller-error.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $uibModalInstance, inname){
    init();

    function init(){
      setValuesToScope();
      setEventsToScope();
      setFunctionsToScope();
    }

    function setValuesToScope(){
      $scope.modalTitle = "エラーが発生しました。";
      $scope.modalCode = inname.errorCode;
      $scope.modalMessage = inname.errorMessage;
    }

    function setEventsToScope(){
    }

    function setFunctionsToScope(){
      $scope.pressClose = pressClose;
    }

    function pressClose(){
      $uibModalInstance.close();
    }

  }

  angular.module('LinkingDemo').controller('ErrorController',
  ['$scope', '$uibModalInstance', 'inname', controller]);
})();
