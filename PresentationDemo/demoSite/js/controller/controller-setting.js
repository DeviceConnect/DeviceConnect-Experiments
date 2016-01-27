/**
 controller-setting.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $uibModal, $uibModalInstance, $location, store, manager){
    init();

    function init(){
      setValuesToScope();
      setEventsToScope();
      setFunctionsToScope();
    }

    function setValuesToScope(){
      $scope.hostName = store.getHostName();
      $scope.userName = store.getUserName();
      $scope.slideName = store.getSlideName();
      $scope.slideSuffix = store.getSuffix();
      $scope.players = [];
      var players = store.getPlayers();
      if(players){
        $scope.players = players;
      }

      $scope.timers = [];
      var timers = store.getTimers();
      if(timers){
        $scope.timers = timers;
      }
      $scope.managerStatus = '不明';
    }

    function setEventsToScope(){
    }

    function setFunctionsToScope(){
      $scope.pressBack = pressBack;

      $scope.pressAddPlayer = pressAddPlayer;
      $scope.pressRemovePlayer = pressRemovePlayer;
      $scope.pressPlayer = pressPlayer;

      $scope.pressAddTimer = pressAddTimer;
      $scope.pressRemoveTimer = pressRemoveTimer;
      $scope.pressProcessType = pressProcessType;
      $scope.pressProcessTarget = pressProcessTarget;
      $scope.changeDelay = changeDelay;

      $scope.changeHostName = changeHostName;
      $scope.changeUserName = changeUserName;
      $scope.changeSlideName = changeSlideName;
      $scope.changeSlideSuffix = changeSlideSuffix;

      $scope.pressClearCookie = pressClearCookie;
      $scope.pressCheckManager = pressCheckManager;
    }

    function pressBack(){
      $uibModalInstance.close();
    }

    /* player */

    function pressAddPlayer(){
      openDeviceList({scopes:['canvas']},function(device){
        if(!device){
          return;
        }
        $scope.players.push({
          id:createPlayerId(),
          name:device.name,
          serviceId:device.id
        });
        updateData();
      });
    }

    function pressRemovePlayer(player){
      $scope.players = $scope.players.filter(function(p){
        return p.id != player.id;
      });
      updateData();
    }

    function pressPlayer(player){
      openDeviceList({scopes:['canvas']},function(device){
        if(!device){
          return;
        }
        player.name = device.name;
        player.serviceId = device.id;
        updateData();
      });
    }

    /* timer */

    function pressAddTimer(){
      openProcessList(null,function(process){
        if(!process){
          return;
        }
        $scope.timers.push({
          id:createTimerId(),
          delay:0,
          typeName:process.name,
          target:'ALL',
          serviceId:undefined
        });
        updateData();
      });
    }

    function pressRemoveTimer(timer){
      $scope.timers = $scope.timers.filter(function(t){
        return t.id != timer.id;
      });
      updateData();
    }

    function pressProcessType(timer){
      openProcessList({},function(device){
        if(!device){
          return;
        }
        timer.typeName = device.name;
        timer.target = 'ALL';
        timer.serviceId = undefined;
        updateData();
      });
    }

    function pressProcessTarget(timer){
      var scopes = makeScopes(timer.typeName);
      openDeviceList({scopes:scopes},function(device){
        if(!device){
          return;
        }
        console.log(device);
        timer.target = device.name;
        timer.serviceId = device.id;
        timer.lightId = device.lightId;
        console.log(timer);

        updateData();
      });
    }

    function changeDelay(tindex, timer){
      if(timer.delay === undefined || timer.delay === null){
        timer.delay = 0;
      }else{
        //Remove first digit when it is zero.
        var target = $("#timerlist").children()[tindex];
        $(target).find('input').val(timer.delay);
      }
      updateData();
    }

    function changeHostName(){
      store.setHostName($scope.hostName);
      manager.setHostName($scope.hostName);
    }

    function changeUserName(){ store.setUserName($scope.userName); }
    function changeSlideName(){ store.setSlideName($scope.slideName); }
    function changeSlideSuffix(){ store.setSuffix($scope.slideSuffix); }

    /* other */

    function pressClearCookie(){
      openConfirmDialog('削除','cookieに保存された情報を全て削除しますか？');
    }

    function pressCheckManager(){
      $scope.$emit('showSpinner');
      $scope.managerStatus = '確認中...';
      manager.checkAvailability({
        onsuccess : function(version){
          $scope.$emit('hideSpinner');
          $scope.$apply(function(){
            $scope.managerStatus = '動作中(ver ' + version + ')';
          });
        },
        onerror : function(code, message){
          $scope.$emit('hideSpinner');
          $scope.$apply(function(){
            if(code === -1){
              openLaunchDialog();
            } else {
              openError({isSuccess:false,errorCode:code,errorMessage:message});
              $scope.managerStatus = '不明';
            }
          });
        }
      });
    }

    function openDeviceList(param, callback){
      var modalInstance = $uibModal.open({
        templateUrl: './html/device-list.html',
        controller: 'DeviceListController',
        backdrop: 'static',
        scope: $scope,
        resolve: {inname:function(){return param;}}
      });
      modalInstance.result.then(
        function(device){
          callback(device);
        }
      );
    }

    function openProcessList(param, callback){
      var modalInstance = $uibModal.open({
        templateUrl: './html/process-list.html',
        controller: 'ProcessListController',
        backdrop: 'static',
        scope: $scope,
        resolve: {inname:function(){return param;}}
      });
      modalInstance.result.then(
        function(process){
          callback(process);
        }
      );
    }

    function makeScopes(typeName){
      var scopes = [];
      if(typeName.match(/色/)){
        scopes.push('light');
      }
      if(typeName.match(/消灯/)){
        scopes.push('light');
      }
      if(typeName.match(/バイブレーション/)){
        scopes.push('vibration');
      }
      return scopes;
    }

    function createPlayerId(){
      return createId($scope.players);
    }

    function createTimerId(){
      return createId($scope.timers);
    }

    function createId(array){
      var max = 0;
      angular.forEach(array, function(item){
        max = Math.max(max, item.id);
      });
      return max + 1;
    }

    function updateData(){
      console.log($scope.timers);
      store.setTimers($scope.timers);
      store.setPlayers($scope.players);
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

    function openLaunchDialog(){
      var modalInstance = $uibModal.open({
        templateUrl: './html/launch.html',
        controller: 'LaunchController',
        backdrop: 'static',
        scope: $scope
      });
      modalInstance.result.then(function(){
        $scope.managerStatus = '不明';
      });
    }

    function openConfirmDialog(title, message){
      var modalInstance = $uibModal.open({
        templateUrl: './html/simple-dialog.html',
        controller: 'SimpleDialogController',
        backdrop: 'static',
        scope: $scope,
        resolve: {params:function(){return {
          title:title,
          message:message
        };}}
      });
      modalInstance.result.then(function(results){
        if(!results || !results.isOK){
          return;
        }
        store.clearAll();
        setValuesToScope();
      });
    }

  }

  angular.module('LinkingDemo').controller('SettingController',
  ['$scope', '$uibModal', '$uibModalInstance', '$location', 'store', 'manager', controller]);
})();
