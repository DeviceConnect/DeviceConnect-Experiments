/**
 controller-top.js
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $location, $routeParams, $uibModal, store, canvas, light, discovery, slideshare, vibration){
    init();

    function init(){
      setValuesToScope();
      setFunctionsToScope();
      loadSlideShare();
    }

    function setValuesToScope(){
      $scope.playing = false;
      $scope.time = '00:00';
    }

    function setFunctionsToScope(){
      $scope.pressPrev = pressPrev;
      $scope.pressNext = pressNext;
      $scope.pressStart = pressStart;
      $scope.pressEnd = pressEnd;
      $scope.pressSetting = pressSetting;
      $scope.onChangeSlider = onChangeSlider;
      $scope.onImgLoad = onImgLoad;
    }

    function loadSlideShare(){
      var userName = $routeParams.userName;
      var slideName = $routeParams.slideName;
      var slideSuffix = $routeParams.slideSuffix;
      if(userName !== undefined && slideName !== undefined){
        store.setUserName(userName);
        store.setSlideName(slideName);
        store.setSuffix(slideSuffix);
      }
      $scope.$emit('showSpinner');
      slideshare.updateSlide(store.getUserName(), store.getSlideName(), store.getSuffix(), function(){
        $scope.$emit('hideSpinner');
        $scope.$apply(function(){
          $scope.slideimg = slideshare.getCurrentImageUrl();
          $scope.currentPage = slideshare.getCurrentPageNumber();
          $scope.totalPage = slideshare.getTotalPageNumber();
        });
      });
    }

    function pressPrev(){
      if(slideshare.getCurrentPageNumber() === 1){
        return;
      }
      slideshare.prev();
      $scope.$emit('showSpinner');
      changeSlide();
    }

    function pressNext(){
      if(slideshare.getTotalPageNumber() === $scope.currentPage){
        return;
      }
      slideshare.next();
      $scope.$emit('showSpinner');
      changeSlide();
    }

    function changeSlide(){
      $scope.currentPage = slideshare.getCurrentPageNumber();
      $scope.slideimg = slideshare.getCurrentImageUrl();
      if(!$scope.playing){
        return;
      }
      getPlayers(function(players){
        angular.forEach(players, function(player){
          sendSlide(player.serviceId, $scope.slideimg);
        });
      });
    }

    function sendSlide(id, url){
      canvas.setServiceId(id);
      canvas.drawImage(url, function(result){console.log(result);});
    }

    function getPlayers(callback){
      var players = store.getPlayers();
      if(!containsAllPlay(players)){
        callback(players);
        return;
      }
      discovery.getPlayers(true, function(result){
        if(result.isSuccess){
          callback(result.devices);
        }
      });
    }

    function containsAllPlay(players){
      var allplay = false;
      angular.forEach(players, function(player){
        if(player.name === "ALL"){
          allplay = true;
        }
      });
      return allplay;
    }

    var timeoutIds = [];

    function pressStart(){
      $scope.playing = true;
      if(slideshare.getCurrentPageNumber() !== 1){
        $scope.$emit('showSpinner');
      }
      slideshare.setPageNumber(Number('1'));
      changeSlide();
      var timers = store.getTimers();
      angular.forEach(timers, function(timer){
        timeoutIds.push(startTimer(timer));
      });
      var progress = 0;
      $scope.presenTimerId = setInterval(function(){
        progress++;
        $scope.$apply(function(){
          $scope.time = convertTimeStr(progress);
        });
      },1000);
    }

    function convertTimeStr(progress){
      if(progress < 60){
        return '00:' + zeroPadding(progress);
      } else {
        var min = Math.floor(progress / 60);
        if(min > 99){
          min = 99;
        }
        var sec = progress % 60;
        return zeroPadding(min) + ':' + zeroPadding(sec);
      }
    }

    function zeroPadding(value){
      return value < 10 ? '0' + value : '' + value;
    }

    function startTimer(timer){
      timeoutId = setTimeout(function(){
        if(timer.target !== 'ALL'){
          startProcess(timer.typeName, timer.serviceId, timer.lightId);
          return;
        }
        discovery.getTargets(timer.typeName,true,function(result){
          if(!result.isSuccess){
            return;
          }
          angular.forEach(result.devices,function(target){
            startProcess(timer.typeName, target.serviceId, target.lightId);
          });
        });
      }, ((timer.delayMinute * 60) + timer.delaySecond) * 1000);
      return timeoutId;
    }

    function startProcess(typeName, serviceId, lightId){
      light.resetStatus();
      if(typeName.match(/色/)){
        light.setServiceId(serviceId);
        light.setLightId(lightId);

        if(typeName.match(/赤/)){
          light.setColor('FF0000');
        } else if(typeName.match(/緑/)){
          light.setColor('00FF00');
        } else if(typeName.match(/青/)){
          light.setColor('0000FF');
        }
        if(typeName.match(/点滅/)){
          light.setFlashing('500,500,500,500,500,500,500,500');
        }
        light.lightOn(function(result){ console.log(result); });
      }
      if(typeName.match(/バイブレーション/)){
        var pattern = "500,200,500,200,500,200,500,200";
        vibration.setServiceId(serviceId);
        vibration.start(pattern, function(result){ console.log(result); });
      }
      if(typeName.match(/消灯/)){
        light.setServiceId(serviceId);
        light.setLightId(lightId);
        light.lightOff(function(result){ console.log(result); });
      }

    }

    function pressEnd(){
      $scope.playing = false;
      angular.forEach(timeoutIds, function(timeoutId){
        clearTimeout(timeoutId);
      });
      timeoutIds = [];
      getPlayers(function(players){
        angular.forEach(players, function(player){
          canvas.setServiceId(player.serviceId);
          canvas.deleteImage(function(result){ console.log(result); });
        });
      });
      clearTimeout($scope.presenTimerId);
      $scope.time = '00:00';
    }

    function pressSetting(){
      var modalInstance = $uibModal.open({
        templateUrl: './html/setting.html',
        controller: 'SettingController',
        backdrop: 'static',
        scope: $scope
      });
      modalInstance.result.then(function(){
        loadSlideShare();
      });
    }

    function onChangeSlider(){
      slideshare.setPageNumber(Number($scope.currentPage));
      $scope.$emit('showSpinner');
      changeSlide();
    }

    function onImgLoad(event){
      $scope.$emit('hideSpinner');
    }

  }

  angular.module('LinkingDemo').controller('TopController',
  ['$scope', '$location', '$routeParams', '$uibModal', 'store', 'canvas', 'light', 'discovery', 'slideshare', 'vibration', controller]);
})();
