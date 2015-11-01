/**
 controller-main.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $location, powermeter){

    init();

    function init(){
      //prefetch
      powermeter.getPowerMeters(function(result){});
      setValuesToScope();
      setEventsToScope();
      setFunctionsToScope();
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
      $scope.transitToSetting = transitToSetting;
      $scope.transitToGraph = transitToGraph;
      $scope.closeModal = closeModal;
    }

    function back() {
      history.back();
    }

    function transitToSetting() {
      $location.path('/setting');
    }

    function transitToGraph() {
      $location.path('/graph');
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
  }

  angular.module('HomeDemo').controller('MainController',
  ['$scope', '$location', 'powermeter', controller]);
})();
