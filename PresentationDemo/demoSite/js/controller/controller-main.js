/**
 controller-main.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $location, store, manager){
    init();

    function init(){
      setValuesToScope();
      setEventsToScope();
      setFunctionsToScope();
      logCookies();
      manager.setHostName(store.getHostName());
    }

    function setValuesToScope(){
      $scope.modalTitle = "";
      $scope.modalMessage = "";
      $scope.modalTemporary = false;
    }

    function setEventsToScope(){
      $scope.$on('showSpinner', showSpinner);
      $scope.$on('hideSpinner', hideSpinner);
      $scope.$on('showErrorModal', showErrorModal);
    }

    function setFunctionsToScope(){
      $scope.back = back;
      $scope.closeModal = closeModal;
    }

    function back() {
      history.back();
    }

    function closeModal(){
      if(!$scope.modalTemporary){
        $location.path('/');
      }
    }

    function showSpinner() {
      $scope.showSpinner = true;
    }

    function hideSpinner() {
      $scope.showSpinner = false;
    }

    function showErrorModal(event,params){
      $scope.$apply(function(){
        $scope.modalTitle = 'エラーが発生しました';
        var message = app.errorMessage(params.error.code);
        if(params.message){
          message+='<br>'+params.message;
        }
        $scope.modalMessage = message;
        $scope.modalTemporary = app.isTemporaryError(params.error.code);
        $scope.modalCode = params.error.code;
        $('.modal').modal('show');
      });
    }

    function logCookies(){
      console.log('====Check Cookies================');
      console.log('targetLink:' + store.getTargetLink());
      console.log('hostName:' + store.getHostName());
      console.log('sessionKey:' + store.getSessionKey());
      console.log('token:' + store.getToken());

      console.log('timers');
      console.log(store.getTimers());
      console.log('players');
      console.log(store.getPlayers());
      console.log('================================');
    }

  }

  angular.module('LinkingDemo').controller('MainController',
  ['$scope', '$location', 'store', 'manager', controller]);
})();
