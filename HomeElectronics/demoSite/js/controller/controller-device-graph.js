/**
 controller-device-graph.js
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

(function() {

  function controller($scope, $routeParams, devices, powermeter, store){

    var serviceId = $routeParams.id;
    var powermeterId = store.getPowermeterId(serviceId);
    var container = $("#placeholder");
    var maximum = 15;
    var series;
    var plot;

    init();

    function init(){
      setValuesToScope();
      setupPowerPolling();
      setupGraph();
    }

    function setValuesToScope(){
      $scope.nav = {
        goBack: true,
        goGraph: false,
        goSetting: false,
        title: '機器別消費電力量グラフ'
      };
      $scope.isTotal = false;
      $scope.deviceName = devices.getDeviceName(serviceId);
      $scope.consumption = getDefaultConsumption();
    }

    function getDefaultConsumption(){
      var consumption;
      if(powermeterId){
        consumption = powermeter.getLatestValue(powermeterId);
        if(consumption){
          consumption = consumption.toFixed(2);
        } else {
          consumption = '...';
        }
      } else {
        consumption = '...';
      }
      return consumption;
    }

    function setupPowerPolling(){
      powermeter.setPollingListener(function(results){
        var found = false;
        var value;
        angular.forEach(results, function(result){
          if(found){return;}
          if(result.serviceId === powermeterId){
            found = true;
            value = result.value;
          }
        });
        if(value){
          $scope.$apply(function(){
            $scope.consumption = value.toFixed(2);
            updateData();
          });
        }
      });
    }

    function setupGraph(){
      series = [{
          data:getLatestData(),
          lines:{fill:false}
        }];
      plot = $.plot(container, series, {
        grid:{
          borderWidth:1, minBorderMargin:20, labelMargin:10,
          backgroundColor:{ colors:["#fff","#e4f4f4"] },
          margin:{ top:8, bottom:20, left:20 },
          markings:function(axes){
            var markings = [];
            var xaxis = axes.xaxis;
            for(var x = Math.floor(xaxis.min);x < xaxis.max;x+= xaxis.tickSize * 2){
              markings.push({
                xaxis:{from:x,to:x+xaxis.tickSize},
                color:"rgba(232,232,255,0.2)"
              });
            }
            return markings;
          }
        },
        xaxis:{ tickFormatter:function(){ return ""; } },
        yaxis:{ min:0, max:50},
        legend:{ show:true }
      });
    }

    function getLatestData() {
      if(!powermeterId){
        return [];
      }
      var data = powermeter.getData(powermeterId);
      var limit = maximum - data.length;
      var res = [];
      var index = 0;
      for(var i = 0; i < maximum; i++){
        if(i < limit){
          res.push([i,0]);
        } else {
          res.push([i,data[index++]]);
        }
      }
      return res;
    }

    function updateData(){
      series[0].data = getLatestData();
      plot.setData(series);
      plot.draw();
    }
  }

  angular.module('HomeDemo').controller('DeviceGraphController',
  ['$scope', '$routeParams', 'devices', 'powermeter', 'store', controller]);
})();
