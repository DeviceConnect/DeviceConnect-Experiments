/**
 controller-processlist.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $uibModalInstance, $location){
    init();

    $scope.processes = [];

    var lightType = "色";
    var lightOffType = "消灯";
    var vibrationType = "バイブレーション";

    var lightOptions = ["点灯","点滅"];
    var lightColors = ["赤","緑","青"];

    angular.forEach(lightColors,function(color){
      angular.forEach(lightOptions,function(option){
        $scope.processes.push({ name:color+lightType+option});
      });
    });

    $scope.processes.push({ name:lightOffType });
    $scope.processes.push({ name:vibrationType });

    function init(){
      setValuesToScope();
      setEventsToScope();
      setFunctionsToScope();
    }

    function setValuesToScope(){
    }

    function setEventsToScope(){
    }

    function setFunctionsToScope(){
      $scope.pressProcess = pressProcess;
      $scope.pressBack = pressBack;
    }

    function pressProcess(process){
      console.log('pressProcess');
      console.log(process);
      $uibModalInstance.close(process);
    }

    function pressBack(){
      console.log('pressBack');
      $uibModalInstance.close();
    }

  }

  angular.module('LinkingDemo').controller('ProcessListController',
  ['$scope', '$uibModalInstance', '$location', controller]);
})();
